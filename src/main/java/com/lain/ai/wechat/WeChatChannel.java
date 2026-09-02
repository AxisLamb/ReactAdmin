package com.lain.ai.wechat;

import io.agentscope.core.message.Msg;
import io.agentscope.harness.agent.gateway.Gateway;
import io.agentscope.harness.agent.gateway.channel.Channel;
import io.agentscope.harness.agent.gateway.channel.ChannelConfig;
import io.agentscope.harness.agent.gateway.channel.ChannelRouter;
import io.agentscope.harness.agent.gateway.channel.DmScope;
import io.agentscope.harness.agent.gateway.channel.InboundMessage;
import io.agentscope.harness.agent.gateway.channel.OutboundAddress;
import io.agentscope.harness.agent.gateway.channel.RouteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 微信渠道适配器，实现 AgentScope 2.0 的 {@link Channel} 扩展点。
 *
 * <p>职责：
 * <ul>
 *   <li>把微信用户发来的文本包装成 {@link InboundMessage}，交给 Gateway 路由到 Agent；</li>
 *   <li>Gateway 通过 {@link #deliver} 回调时，调用微信接口把结果推送给用户。</li>
 * </ul>
 *
 * <p>会话隔离通过 {@link DmScope#PER_PEER} 实现：每个微信用户（UserID / OpenID）拥有独立会话，
 * 同一用户的多轮消息由 Gateway 的 SessionTurnGate 串行处理，避免并发串扰。
 */
public class WeChatChannel implements Channel {

    private static final Logger log = LoggerFactory.getLogger(WeChatChannel.class);

    /** 渠道 ID，需与 InboundMessage 中携带的 channelId 保持一致 */
    public static final String CHANNEL_ID = "wechat";

    private final ChannelConfig config;
    private final ChannelRouter router;
    private final WeChatApiClient apiClient;

    private volatile Gateway gateway;

    public WeChatChannel(String agentId, WeChatApiClient apiClient) {
        this.config = ChannelConfig.builder(CHANNEL_ID)
                .defaultAgentId(agentId)
                .dmScope(DmScope.PER_PEER)
                .build();
        this.router = new ChannelRouter(agentId);
        this.apiClient = apiClient;
    }

    @Override
    public void init(Gateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public void start() {
        log.info("微信渠道已就绪，channelId={}", CHANNEL_ID);
    }

    @Override
    public void stop() {
        log.info("微信渠道已停止，channelId={}", CHANNEL_ID);
    }

    @Override
    public String channelId() {
        return config.channelId();
    }

    @Override
    public ChannelConfig config() {
        return config;
    }

    /**
     * 将入站消息交给 Agent 处理。
     *
     * <p>这里使用不带 {@link OutboundAddress} 的重载，由调用方决定如何回传结果，
     * 避免 Gateway 内部 deliver 与业务侧主动推送造成重复发送。
     *
     * @param inbound 入站消息
     * @return Agent 的回复
     */
    @Override
    public Mono<Msg> dispatch(InboundMessage inbound) {
        Gateway current = this.gateway;
        if (current == null) {
            return Mono.error(new IllegalStateException("微信渠道尚未绑定 Gateway"));
        }
        RouteResult route = router.resolveRoute(config, inbound);
        log.debug("微信消息路由完成，agentId={}, matchedBy={}", route.agentId(), route.matchedBy());
        return current.run(route.context(), inbound.messages());
    }

    /**
     * Gateway 侧的主动回传，调用微信开放接口把消息发给用户。
     *
     * @param address  目标地址，to 为微信用户标识
     * @param messages 待发送的消息列表
     */
    @Override
    public void deliver(OutboundAddress address, List<Msg> messages) {
        if (apiClient == null || address == null || messages == null) {
            return;
        }
        String text = messages.stream()
                .filter(Objects::nonNull)
                .map(Msg::getTextContent)
                .filter(content -> content != null && !content.isBlank())
                .collect(Collectors.joining("\n"));
        if (!text.isEmpty()) {
            apiClient.sendText(address.to(), text);
        }
    }
}
