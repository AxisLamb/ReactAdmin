package com.lain.ai.wechat;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.harness.agent.gateway.channel.InboundMessage;
import io.agentscope.harness.agent.gateway.channel.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 微信机器人业务编排。
 *
 * <p>处理链路：
 * <pre>
 * 回调请求 → 签名校验 → 解密 → 去重 → Agent 执行
 *      ├─ 在 syncReplyTimeoutMs 内完成 → 直接被动回复（明文/加密 XML）
 *      └─ 超时 → 立即响应 success，Agent 完成后调用接口主动推送
 * </pre>
 *
 * <p>采用"能快则快，慢则异步"的双通道策略：简单问题秒回，复杂任务也不会因为
 * 微信 5 秒回调限制而丢失结果。
 */
public class WeChatBotService implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(WeChatBotService.class);

    private static final String NO_CONTENT = "（Agent 未返回内容）";

    private final WeChatBotProperties properties;
    private final WeChatApiClient apiClient;
    private final WeChatChannel channel;
    private final WeChatCrypto crypto;
    private final ExecutorService executor;

    /** 消息去重，微信在超时未响应时会重试推送 */
    private final Cache<String, Boolean> processed;

    public WeChatBotService(WeChatBotProperties properties,
                            WeChatApiClient apiClient,
                            WeChatChannel channel,
                            WeChatCrypto crypto) {
        this.properties = properties;
        this.apiClient = apiClient;
        this.channel = channel;
        this.crypto = crypto;
        this.executor = Executors.newFixedThreadPool(
                Math.max(1, properties.getAgent().getWorkerThreads()),
                runnable -> {
                    Thread thread = new Thread(runnable, "wechat-bot-worker");
                    thread.setDaemon(true);
                    return thread;
                });
        this.processed = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(10))
                .maximumSize(10_000)
                .build();
    }

    /**
     * 处理一条微信入站消息。
     *
     * @param message   已解析（并解密）的消息
     * @param timestamp 回调时间戳，用于生成加密回复
     * @param nonce     回调随机数，用于生成加密回复
     * @return 回调响应体，为空时由控制器返回 success
     */
    public String handleInbound(WeChatXmlMessage message, String timestamp, String nonce) {
        if (isDuplicate(message.getMsgId())) {
            log.debug("消息重复，已忽略: msgId={}", message.getMsgId());
            return "success";
        }

        String userId = message.getFromUserName();
        if (userId == null || userId.isBlank()) {
            log.warn("消息缺少 FromUserName，忽略");
            return "success";
        }

        if (message.isSubscribe() || message.isEnterAgent()) {
            return reply(message, properties.getWelcomeMessage(), timestamp, nonce);
        }
        if (!message.isText()) {
            log.debug("暂不处理的消息类型: {}", message.getMsgType());
            return reply(message, "我目前只支持文本消息，其他类型还在学习中～", timestamp, nonce);
        }

        String question = message.getContent();
        if (question == null || question.isBlank()) {
            return "success";
        }

        log.info("收到微信消息，user={}, length={}", userId, question.length());
        CompletableFuture<Msg> future =
                CompletableFuture.supplyAsync(() -> runAgent(userId, question), executor);

        try {
            Msg answer = future.get(properties.getSyncReplyTimeoutMs(), TimeUnit.MILLISECONDS);
            return reply(message, extractText(answer), timestamp, nonce);
        } catch (TimeoutException e) {
            // 超出微信回调时限，先结束本次回调，结果稍后主动推送
            log.debug("Agent 未在 {} ms 内返回，转为异步推送", properties.getSyncReplyTimeoutMs());
            future.thenAccept(answer -> push(userId, answer))
                    .exceptionally(error -> {
                        log.error("微信消息异步处理失败: {}", error.getMessage(), error);
                        return null;
                    });
            return "success";
        } catch (Exception e) {
            log.error("Agent 执行失败: {}", e.getMessage(), e);
            return reply(message, "服务开小差了，请稍后再试～", timestamp, nonce);
        }
    }

    /** 调用 AgentScope 处理消息 */
    private Msg runAgent(String userId, String question) {
        Msg userMsg = Msg.builder()
                .role(MsgRole.USER)
                .textContent(question)
                .build();
        InboundMessage inbound = InboundMessage
                .builder(WeChatChannel.CHANNEL_ID, Peer.direct(userId), List.of(userMsg))
                .senderId(userId)
                .build();
        return channel.dispatch(inbound).block(Duration.ofMinutes(5));
    }

    /** 主动推送回复 */
    private void push(String userId, Msg answer) {
        apiClient.sendText(userId, extractText(answer));
    }

    /** 生成回调响应体 */
    private String reply(WeChatXmlMessage message, String content, String timestamp, String nonce) {
        String plain = message.buildTextReply(content);
        if (properties.isEncryptMode()) {
            return message.buildEncryptedReply(plain, crypto, timestamp, nonce);
        }
        return plain;
    }

    /** 提取回复文本 */
    private String extractText(Msg msg) {
        if (msg == null) {
            return NO_CONTENT;
        }
        String text = msg.getTextContent();
        return text == null || text.isBlank() ? NO_CONTENT : text;
    }

    /** 消息去重判断 */
    private boolean isDuplicate(String msgId) {
        if (msgId == null || msgId.isBlank()) {
            return false;
        }
        return processed.asMap().putIfAbsent(msgId, Boolean.TRUE) != null;
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
