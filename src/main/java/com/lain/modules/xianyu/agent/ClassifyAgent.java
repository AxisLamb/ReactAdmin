package com.lain.modules.xianyu.agent;

/**
 * 意图识别 Agent（对应 Python 版 ClassifyAgent，作为规则路由的大模型兜底）
 */
public class ClassifyAgent extends BaseAgent {

    public ClassifyAgent(LlmClient llmClient, String systemPrompt, SafetyFilter safetyFilter) {
        super(llmClient, systemPrompt, safetyFilter);
    }
}
