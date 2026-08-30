package com.lain.modules.xianyu.agent;

import java.util.List;

/**
 * 议价处理 Agent（对应 Python 版 PriceAgent，采用阶梯让步 + 动态温度策略）
 */
public class PriceAgent extends BaseAgent {

    public PriceAgent(LlmClient llmClient, String systemPrompt, SafetyFilter safetyFilter) {
        super(llmClient, systemPrompt, safetyFilter);
    }

    /**
     * 重写生成逻辑：根据议价轮次动态调整温度，并在系统提示中追加当前议价轮次
     */
    @Override
    public String generate(String userMsg, String itemDesc, String context, int bargainCount) {
        double dynamicTemp = calcTemperature(bargainCount);
        List<ChatMessage> messages = buildMessages(userMsg, itemDesc, context);
        ChatMessage system = messages.get(0);
        String enhancedSystem = system.content() + "\n▲当前议价轮次：" + bargainCount;
        messages = List.of(new ChatMessage("system", enhancedSystem), messages.get(1));

        String response = llmClient.chat(messages, dynamicTemp, false, 500);
        return safetyFilter.apply(response);
    }

    /**
     * 动态温度策略：随议价轮次递增（0.3 起步，最高 0.9）
     */
    private double calcTemperature(int bargainCount) {
        return Math.min(0.3 + bargainCount * 0.15, 0.9);
    }
}
