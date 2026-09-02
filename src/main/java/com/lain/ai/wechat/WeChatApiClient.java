package com.lain.ai.wechat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * 微信开放接口客户端，负责 access_token 管理与主动消息推送。
 *
 * <p>之所以使用主动推送而不是被动回复：微信要求回调 5 秒内响应，而大模型通常需要更长时间。
 * 流程为先结束回调，Agent 完成后再通过接口把结果发回用户。
 */
public class WeChatApiClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(WeChatApiClient.class);

    private static final String WECOM_TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s";
    private static final String WECOM_SEND_URL = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=%s";
    private static final String MP_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String MP_SEND_URL = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=%s";

    private final WeChatBotProperties properties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile String accessToken;
    private volatile long tokenExpireAtMillis;

    public WeChatApiClient(WeChatBotProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * 获取 access_token，带本地缓存，到期前 5 分钟提前刷新。
     *
     * @return access_token
     */
    public synchronized String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpireAtMillis) {
            return accessToken;
        }
        boolean isWeCom = properties.getChannelType() == WeChatBotProperties.ChannelType.WECOM;
        String url = isWeCom
                ? String.format(WECOM_TOKEN_URL, properties.getAppId(), properties.getSecret())
                : String.format(MP_TOKEN_URL, properties.getAppId(), properties.getSecret());

        JsonNode json = get(url);
        String token = json.path("access_token").asText(null);
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("获取微信 access_token 失败，返回内容：" + json);
        }
        int expiresIn = json.path("expires_in").asInt(7200);
        this.accessToken = token;
        this.tokenExpireAtMillis = System.currentTimeMillis() + (expiresIn - 300L) * 1000L;
        log.info("微信 access_token 已刷新，有效期 {} 秒", expiresIn);
        return token;
    }

    /**
     * 向指定用户推送文本消息。
     *
     * @param toUser  企业微信为 UserID，公众号为 OpenID
     * @param content 文本内容
     * @return 是否发送成功
     */
    public boolean sendText(String toUser, String content) {
        if (toUser == null || toUser.isEmpty()) {
            log.warn("消息接收人为空，跳过发送");
            return false;
        }
        String text = truncate(content);
        try {
            return doSend(toUser, text, false);
        } catch (Exception e) {
            log.warn("微信消息发送失败，尝试刷新 token 后重试: {}", e.getMessage());
            this.accessToken = null;
            this.tokenExpireAtMillis = 0L;
            return doSend(toUser, text, true);
        }
    }

    private boolean doSend(String toUser, String text, boolean retried) {
        try {
            boolean isWeCom = properties.getChannelType() == WeChatBotProperties.ChannelType.WECOM;
            String url = String.format(isWeCom ? WECOM_SEND_URL : MP_SEND_URL, getAccessToken());

            Map<String, Object> body = new java.util.LinkedHashMap<>();
            body.put("touser", toUser);
            body.put("msgtype", "text");
            body.put("text", Map.of("content", text));
            if (isWeCom) {
                body.put("agentid", properties.getAgentId());
            }

            JsonNode json = post(url, objectMapper.writeValueAsString(body));
            int errCode = json.path("errcode").asInt(0);
            if (errCode == 0) {
                return true;
            }
            // 40014 / 42001 / 40001 均为 token 失效，刷新后重试一次
            if (!retried && (errCode == 40014 || errCode == 42001 || errCode == 40001)) {
                this.accessToken = null;
                this.tokenExpireAtMillis = 0L;
                return doSend(toUser, text, true);
            }
            log.error("微信消息发送失败，errcode={}, errmsg={}", errCode, json.path("errmsg").asText());
            return false;
        } catch (Exception e) {
            log.error("微信消息发送异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /** 按 UTF-8 字节截断，避免超出微信 2048 字节限制 */
    private String truncate(String content) {
        if (content == null || content.isEmpty()) {
            return "（无回复内容）";
        }
        int max = properties.getMaxReplyBytes();
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= max) {
            return content;
        }
        int end = max;
        // 避免截断到 UTF-8 多字节字符中间
        while (end > 0 && (bytes[end] & 0xC0) == 0x80) {
            end--;
        }
        return new String(bytes, 0, end, StandardCharsets.UTF_8) + "…（内容已截断）";
    }

    private JsonNode get(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readTree(response.body());
        } catch (Exception e) {
            throw new IllegalStateException("调用微信接口失败: " + e.getMessage(), e);
        }
    }

    private JsonNode post(String url, String jsonBody) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readTree(response.body());
        } catch (Exception e) {
            throw new IllegalStateException("调用微信接口失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        // HttpClient 无需显式关闭，这里保留钩子以便后续扩展
    }
}
