package com.lain.modules.xianyu.agent;

import java.util.List;

/**
 * 技术咨询 Agent（对应 Python 版 TechAgent，启用联网搜索增强）
 */
public class TechAgent extends BaseAgent {

    public TechAgent(LlmClient llmClient, String systemPrompt, SafetyFilter safetyFilter) {
        super(llmClient, systemPrompt, safetyFilter);
    }

    /**
     * 重写生成逻辑：固定温度 0.4，启用 enable_search 联网搜索
     */
    @Override
    public String generate(String userMsg, String itemDesc, String context, int bargainCount) {
        List<ChatMessage> messages = buildMessages(userMsg, itemDesc, context);
        String response = llmClient.chat(messages, 0.4, true, 500);
        return safetyFilter.apply(response);
    }
}
