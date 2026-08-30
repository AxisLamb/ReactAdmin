package com.lain.modules.xianyu.agent;

import java.util.List;

/**
 * Agent 基类（对应 Python 版 BaseAgent）
 */
public abstract class BaseAgent {

    protected final LlmClient llmClient;
    protected final String systemPrompt;
    protected final SafetyFilter safetyFilter;

    protected BaseAgent(LlmClient llmClient, String systemPrompt, SafetyFilter safetyFilter) {
        this.llmClient = llmClient;
        this.systemPrompt = systemPrompt;
        this.safetyFilter = safetyFilter;
    }

    /**
     * 生成回复模板方法
     */
    public String generate(String userMsg, String itemDesc, String context, int bargainCount) {
        List<ChatMessage> messages = buildMessages(userMsg, itemDesc, context);
        String response = callLlm(messages, 0.4);
        return safetyFilter.apply(response);
    }

    /**
     * 构建消息链
     */
    protected List<ChatMessage> buildMessages(String userMsg, String itemDesc, String context) {
        String systemContent = "【商品信息】" + itemDesc + "\n【你与客户对话历史】" + context + "\n" + systemPrompt;
        return List.of(new ChatMessage("system", systemContent), new ChatMessage("user", userMsg));
    }

    /**
     * 调用大模型
     */
    protected String callLlm(List<ChatMessage> messages, double temperature) {
        return llmClient.chat(messages, temperature, false, 500);
    }
}
