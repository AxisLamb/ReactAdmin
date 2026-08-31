package com.lain.modules.xianyu.agent;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lain.modules.xianyu.config.XianyuProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 大模型调用客户端（OpenAI 兼容 Chat Completions 接口）
 * <p>
 * 默认对接通义千问 DashScope 兼容接口，支持 enable_search 联网搜索与动态温度参数。
 */
@Component
public class LlmClient {

    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final XianyuProperties properties;

    public LlmClient(XianyuProperties properties) {
        this.properties = properties;
    }

    /**
     * 调用大模型生成回复
     *
     * @param messages      消息链
     * @param temperature   温度参数
     * @param enableSearch  是否启用联网搜索
     * @param maxTokens     最大输出 token 数
     * @return 模型回复内容
     */
    public String chat(List<ChatMessage> messages, double temperature, boolean enableSearch, int maxTokens) {
        ObjectNode body = MAPPER.createObjectNode();
        body.put("model", properties.getModelName());
        ArrayNode messagesNode = body.putArray("messages");
        for (ChatMessage msg : messages) {
            ObjectNode node = messagesNode.addObject();
            node.put("role", msg.role());
            node.put("content", msg.content());
        }
        body.put("temperature", temperature);
        body.put("max_tokens", maxTokens);
        body.put("top_p", 0.8);
        if (enableSearch) {
            body.put("enable_search", true);
        }

        String url = properties.getModelBaseUrl().replaceAll("/+$", "") + "/chat/completions";
        try {
            HttpResponse response = HttpRequest.post(url)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .header("Content-Type", "application/json")
                    .body(MAPPER.writeValueAsString(body))
                    .timeout(60000)
                    .execute();
            String responseBody = response.body();
            response.close();

            JsonNode resJson = MAPPER.readTree(responseBody);
            JsonNode content = resJson.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode()) {
                log.error("LLM 响应缺少 content 字段: {}", responseBody);
                return "";
            }
            return content.asText();
        } catch (Exception e) {
            log.error("调用大模型失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 便捷方法：构建 system + user 两段消息
     */
    public static List<ChatMessage> buildMessages(String systemPrompt, String userMsg) {
        List<ChatMessage> messages = new ArrayList<>(2);
        messages.add(new ChatMessage("system", systemPrompt));
        messages.add(new ChatMessage("user", userMsg));
        return messages;
    }
}
