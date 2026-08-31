package com.lain.modules.xianyu.agent;

import java.util.List;

/**
 * 默认处理 Agent
 */
public class DefaultAgent extends BaseAgent {

    public DefaultAgent(LlmClient llmClient, String systemPrompt, SafetyFilter safetyFilter) {
        super(llmClient, systemPrompt, safetyFilter);
    }

    /**
     * 重写生成逻辑：默认回复使用 0.7 温度
     */
    @Override
    public String generate(String userMsg, String itemDesc, String context, int bargainCount) {
        List<ChatMessage> messages = buildMessages(userMsg, itemDesc, context);
        String response = llmClient.chat(messages, 0.7, false, 500);
        return safetyFilter.apply(response);
    }
}
