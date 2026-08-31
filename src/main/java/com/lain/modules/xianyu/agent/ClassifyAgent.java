package com.lain.modules.xianyu.agent;

/**
 * 意图识别 Agent
 */
public class ClassifyAgent extends BaseAgent {

    public ClassifyAgent(LlmClient llmClient, String systemPrompt, SafetyFilter safetyFilter) {
        super(llmClient, systemPrompt, safetyFilter);
    }
}
