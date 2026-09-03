package com.lain.ai.flow;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEndEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ToolResultEndEvent;
import io.agentscope.core.event.ToolResultStartEvent;
import io.agentscope.core.event.ToolResultTextDeltaEvent;
import io.agentscope.core.middleware.ActingInput;
import io.agentscope.core.middleware.AgentInput;
import io.agentscope.core.middleware.ModelCallInput;
import io.agentscope.core.middleware.ReasoningInput;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.ToolResultState;
import io.agentscope.core.message.ToolUseBlock;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 生命周期日志中间件冒烟测试：验证各插桩点在正常、错误两种路径下都不抛异常。
 */
class AgentLifecycleLoggerTest {

    private final AgentLifecycleLogger logger = new AgentLifecycleLogger(0L, 100, true);

    private final RuntimeContext ctx = RuntimeContext.builder()
            .sessionId("test-session")
            .userId("tester")
            .build();

    @Test
    void 各插桩点正常路径不抛异常() {
        logger.onAgent(null, ctx, new AgentInput(List.of()),
                        input -> Flux.just(new AgentEndEvent("r1")))
                .blockLast();

        logger.onReasoning(null, ctx, new ReasoningInput(List.of(), List.of(), null),
                        input -> Flux.empty())
                .blockLast();

        logger.onModelCall(null, ctx, new ModelCallInput(List.of(), List.of(), null, null),
                        input -> Flux.just(new TextBlockDeltaEvent("r1", "b1", "你好，我是助手")))
                .blockLast();

        ToolUseBlock toolUse = new ToolUseBlock("call-1", "openPage",
                Map.of("url", "https://example.com"));
        logger.onActing(null, ctx, new ActingInput(List.of(toolUse)),
                        input -> Flux.just(
                                new ToolResultStartEvent("r1", "call-1", "openPage"),
                                new ToolResultTextDeltaEvent("r1", "call-1", "openPage", "页面加载完成"),
                                new ToolResultEndEvent("r1", "call-1", "openPage", ToolResultState.SUCCESS)))
                .blockLast();

        assertNotNull(logger.onSystemPrompt(null, ctx, "你是一个助手").block());
    }

    @Test
    void 异常路径应捕获并不影响流程() {
        logger.onAgent(null, ctx, new AgentInput(List.of()),
                        input -> Flux.error(new IllegalStateException("模拟异常")))
                .onErrorResume(e -> Flux.empty())
                .blockLast();

        logger.onActing(null, ctx, new ActingInput(List.of(
                        new ToolUseBlock("call-2", "clickElement", Map.of("selector", "#btn")))),
                        input -> Flux.error(new RuntimeException("工具炸了")))
                .onErrorResume(e -> Flux.empty())
                .blockLast();
    }

    @Test
    void RuntimeContext为空时不应抛异常() {
        logger.onAgent(null, null, new AgentInput(List.of()), input -> Flux.empty()).blockLast();
        logger.onActing(null, null, new ActingInput(List.of()), input -> Flux.empty()).blockLast();
    }

    @Test
    void 工具结果文本含超时错误时仍能识别为失败() {
        // AgentScope 把工具超时包装成 SUCCESS 状态 + 错误文本，模拟该场景
        ToolUseBlock toolUse = new ToolUseBlock("call-3", "openPage",
                Map.of("url", "https://www.goofish.com/"));
        logger.onActing(null, ctx, new ActingInput(List.of(toolUse)),
                        input -> Flux.just(
                                new ToolResultStartEvent("r1", "call-3", "openPage"),
                                new ToolResultTextDeltaEvent("r1", "call-3", "openPage",
                                        "java.lang.RuntimeException: Tool execution timeout after PT5M"),
                                new ToolResultEndEvent("r1", "call-3", "openPage", ToolResultState.SUCCESS)))
                .blockLast();
    }

    @Test
    void 关闭模型IO日志开关时不应抛异常() {
        AgentLifecycleLogger quiet = new AgentLifecycleLogger(0L, 100, false, false);
        quiet.onModelCall(null, ctx, new ModelCallInput(List.of(), List.of(), null, null),
                        input -> Flux.empty())
                .blockLast();
        quiet.onActing(null, ctx, new ActingInput(List.of()), input -> Flux.empty()).blockLast();
    }
}
