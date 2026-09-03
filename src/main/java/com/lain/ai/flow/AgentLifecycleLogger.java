package com.lain.ai.flow;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.AgentEventType;
import io.agentscope.core.event.AgentResultEvent;
import io.agentscope.core.event.ModelCallEndEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ThinkingBlockDeltaEvent;
import io.agentscope.core.event.ToolResultEndEvent;
import io.agentscope.core.event.ToolResultStartEvent;
import io.agentscope.core.event.ToolResultTextDeltaEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.ToolResultState;
import io.agentscope.core.middleware.ActingInput;
import io.agentscope.core.middleware.AgentInput;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.middleware.ModelCallInput;
import io.agentscope.core.middleware.ReasoningInput;
import io.agentscope.core.model.ChatUsage;
import io.agentscope.core.tool.Toolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Agent 生命周期日志中间件（AgentScope 2.0 Middleware）。
 *
 * <p>按官方文档，v2 用 Middleware 替代 1.x 的 Hook 做生命周期埋点，共 5 个插桩点，
 * 覆盖从一次完整 reply 下沉到模型 API 调用的全链路：
 *
 * <pre>
 * onAgent（一次完整 reply）
 *  └── 每一轮 ReAct
 *       ├── onReasoning（推理）
 *       │     ├── onSystemPrompt（组装 system prompt）
 *       │     └── onModelCall（模型 API 调用）
 *       └── onActing（本轮的工具调用）
 * </pre>
 *
 * <p>本中间件在 5 个位置全部打点，输出带耗时的阶段日志，效果示例：
 *
 * <pre>
 * [browser-operator|s-1] ▶ 开始 | 输入 1 条消息
 * [browser-operator|s-1] ├ 推理 #1 | 上下文 38 条消息 | 工具 13 个
 * [browser-operator|s-1] │ ├ 模型调用 | model=qwen3.7-plus | 输入 38 条消息
 * [browser-operator|s-1] │ ├ 模型调用结束 | 耗时 2.8s | tokens 12036/512
 * [browser-operator|s-1] ├ 推理 #1 结束 | 耗时 2.9s
 * [browser-operator|s-1] ├ 工具调用 #1 | openPage(url=https://example.com)
 * [browser-operator|s-1] ├ 工具调用 #1 结束 | 耗时 9.9s | openPage=SUCCESS
 * [browser-operator|s-1] ◀ 结束 | 耗时 45.2s | 推理 5 轮 | 工具 4 次
 * </pre>
 *
 * <p>超过 {@code slowThresholdMs} 的步骤自动升级为 WARN，方便一眼看出时间花在哪。
 *
 * <p>用法：
 * <pre>
 * HarnessAgent.builder()
 *         .middleware(new AgentLifecycleLogger())
 *         .build();
 * </pre>
 */
public class AgentLifecycleLogger implements MiddlewareBase {

    private static final Logger log = LoggerFactory.getLogger(AgentLifecycleLogger.class);

    /** 在 RuntimeContext 中存放计数器的 key */
    private static final String COUNTER_KEY = "agentLifecycleCounter";

    private final long slowThresholdMs;
    private final int maxPreviewLength;

    /** 是否在 DEBUG 级别逐个打印 AgentEvent（事件流全量追踪，排查时再开） */
    private final boolean logEvents;

    /**
     * 是否打印模型输入/输出内容与工具结果内容。
     *
     * <p>模型输入只摘要最近的用户消息（避免几十条上下文刷屏），输出与工具结果
     * 按 {@code maxPreviewLength} 截断。工具执行失败时结果文本常含原因（如
     * 超时/异常），打印它才能看到失败细节。
     */
    private final boolean logModelIO;

    public AgentLifecycleLogger() {
        this(5_000L, 300, false, true);
    }

    /**
     * @param slowThresholdMs  超过该耗时的步骤按 WARN 输出
     * @param maxPreviewLength 参数/结果预览的最大字符数
     */
    public AgentLifecycleLogger(long slowThresholdMs, int maxPreviewLength) {
        this(slowThresholdMs, maxPreviewLength, false, true);
    }

    /**
     * @param slowThresholdMs  超过该耗时的步骤按 WARN 输出
     * @param maxPreviewLength 参数/结果预览的最大字符数
     * @param logEvents        是否逐个打印 AgentEvent（DEBUG 级别）
     */
    public AgentLifecycleLogger(long slowThresholdMs, int maxPreviewLength, boolean logEvents) {
        this(slowThresholdMs, maxPreviewLength, logEvents, true);
    }

    /**
     * @param slowThresholdMs  超过该耗时的步骤按 WARN 输出
     * @param maxPreviewLength 参数/结果预览的最大字符数
     * @param logEvents        是否逐个打印 AgentEvent（DEBUG 级别）
     * @param logModelIO       是否打印模型输入/输出与工具结果内容（默认 true）
     */
    public AgentLifecycleLogger(long slowThresholdMs, int maxPreviewLength, boolean logEvents, boolean logModelIO) {
        this.slowThresholdMs = slowThresholdMs;
        this.maxPreviewLength = maxPreviewLength;
        this.logEvents = logEvents;
        this.logModelIO = logModelIO;
    }

    /**
     * 包裹一次完整的 reply 流程。
     */
    @Override
    public Flux<AgentEvent> onAgent(Agent agent, RuntimeContext ctx, AgentInput input,
                                    Function<AgentInput, Flux<AgentEvent>> next) {
        String tag = tag(agent, ctx);
        Counter counter = counter(ctx);
        long start = System.nanoTime();

        log.info("[{}] ▶ 开始 | 输入 {} 条消息", tag, size(input == null ? null : input.msgs()));

        return withErrorLog(next.apply(input), tag, "reply")
                .doOnNext(event -> observeEvent(tag, event))
                .doFinally(signal -> {
                    long elapsed = elapsedMillis(start);
                    logStep(elapsed, String.format(
                            "[%s] ◀ 结束 | 耗时 %s | 推理 %d 轮 | 工具 %d 次 | 信号 %s",
                            tag, formatDuration(elapsed),
                            counter.reasoning.get(), counter.acting.get(), signal));
                });
    }

    /**
     * 包裹 ReAct 循环中的一次推理。
     */
    @Override
    public Flux<AgentEvent> onReasoning(Agent agent, RuntimeContext ctx, ReasoningInput input,
                                        Function<ReasoningInput, Flux<AgentEvent>> next) {
        String tag = tag(agent, ctx);
        int round = counter(ctx).reasoning.incrementAndGet();
        long start = System.nanoTime();

        log.info("[{}] ├ 推理 #{} | 上下文 {} 条消息 | 工具 {}",
                tag, round, size(input == null ? null : input.messages()),
                input == null || input.tools() == null ? 0 : input.tools().size());

        return withErrorLog(next.apply(input), tag, "推理 #" + round)
                .doFinally(signal -> {
                    long elapsed = elapsedMillis(start);
                    logStep(elapsed, String.format("[%s] ├ 推理 #%d 结束 | 耗时 %s",
                            tag, round, formatDuration(elapsed)));
                });
    }

    /**
     * 包裹一次底层模型 API 调用，同时统计 token 消耗。
     */
    @Override
    public Flux<AgentEvent> onModelCall(Agent agent, RuntimeContext ctx, ModelCallInput input,
                                        Function<ModelCallInput, Flux<AgentEvent>> next) {
        String tag = tag(agent, ctx);
        String model = input == null || input.model() == null ? "unknown" : input.model().getModelName();
        long start = System.nanoTime();
        AtomicReference<ChatUsage> usageRef = new AtomicReference<>();
        StringBuilder outputText = new StringBuilder();

        List<Msg> messages = input == null ? List.of() : input.messages();
        log.info("[{}] │ ├ 模型调用 | model={} | 输入 {} 条消息",
                tag, model, size(messages));
        if (logModelIO) {
            log.info("[{}] │ │ ↓ 输入预览: {}", tag, preview(lastUserMessage(messages)));
        }

        return withErrorLog(next.apply(input), tag, "模型调用")
                .doOnNext(event -> {
                    if (event instanceof ModelCallEndEvent end && end.getUsage() != null) {
                        usageRef.set(end.getUsage());
                    } else if (event instanceof TextBlockDeltaEvent text && text.getDelta() != null) {
                        outputText.append(text.getDelta());
                    } else if (event instanceof ThinkingBlockDeltaEvent think && think.getDelta() != null
                            && outputText.isEmpty()) {
                        outputText.append("[思考] ").append(think.getDelta());
                    }
                })
                .doFinally(signal -> {
                    long elapsed = elapsedMillis(start);
                    String out = outputText.length() == 0
                            ? "" : " | 输出: " + preview(outputText.toString());
                    logStep(elapsed, String.format("[%s] │ ├ 模型调用结束 | 耗时 %s%s%s",
                            tag, formatDuration(elapsed), describeUsage(usageRef.get()), out));
                });
    }

    /**
     * 包裹一次工具调用执行，打印工具名、入参与最终状态。
     */
    @Override
    public Flux<AgentEvent> onActing(Agent agent, RuntimeContext ctx, ActingInput input,
                                     Function<ActingInput, Flux<AgentEvent>> next) {
        String tag = tag(agent, ctx);
        int seq = counter(ctx).acting.incrementAndGet();
        long start = System.nanoTime();

        log.info("[{}] ├ 工具调用 #{} | {}", tag, seq, describeToolCalls(input));

        // toolCallId -> {name, state, 结果文本}
        Map<String, ToolOutcome> outcomes = new java.util.LinkedHashMap<>();
        return withErrorLog(next.apply(input), tag, "工具调用 #" + seq)
                .doOnNext(event -> {
                    if (event instanceof ToolResultStartEvent startEvt) {
                        outcomes.computeIfAbsent(startEvt.getToolCallId(),
                                        id -> new ToolOutcome(startEvt.getToolCallName()))
                                .name = startEvt.getToolCallName();
                    } else if (event instanceof ToolResultTextDeltaEvent text) {
                        outcomes.computeIfAbsent(text.getToolCallId(),
                                        id -> new ToolOutcome(text.getToolCallName()))
                                .append(text.getDelta());
                    } else if (event instanceof ToolResultEndEvent end) {
                        outcomes.computeIfAbsent(end.getToolCallId(),
                                        id -> new ToolOutcome(end.getToolCallName()))
                                .state = end.getState();
                    }
                })
                .doFinally(signal -> {
                    long elapsed = elapsedMillis(start);
                    String summary = summarizeToolOutcomes(outcomes);
                    logStep(elapsed, String.format("[%s] ├ 工具调用 #%d 结束 | 耗时 %s | %s",
                            tag, seq, formatDuration(elapsed),
                            summary.isEmpty() ? "无结果事件" : summary),
                            anyToolFailed(outcomes));
                });
    }

    /**
     * 组装 system prompt 时触发，仅打印长度避免日志刷屏。
     */
    @Override
    public Mono<String> onSystemPrompt(Agent agent, RuntimeContext ctx, String currentPrompt) {
        log.debug("[{}] │ ├ 系统提示词 | 长度 {} 字符",
                tag(agent, ctx), currentPrompt == null ? 0 : currentPrompt.length());
        return Mono.just(currentPrompt);
    }

    // ==================== 内部实现 ====================

    /** 本轮工具调用的摘要 */
    private String describeToolCalls(ActingInput input) {
        if (input == null || input.toolCalls() == null || input.toolCalls().isEmpty()) {
            return "无工具调用";
        }
        return input.toolCalls().stream()
                .map(call -> call.getName() + "(" + preview(formatArgs(call.getInput())) + ")")
                .collect(Collectors.joining(", "));
    }

    /** 工具入参格式化为 k=v 形式 */
    private String formatArgs(Map<String, Object> args) {
        if (args == null || args.isEmpty()) {
            return "";
        }
        return args.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    /** token 消耗摘要 */
    private String describeUsage(ChatUsage usage) {
        if (usage == null) {
            return "";
        }
        return String.format(" | tokens 输入 %d / 输出 %d / 合计 %d",
                usage.getInputTokens(), usage.getOutputTokens(), usage.getTotalTokens());
    }

    /** 取或建计数器，状态挂在 RuntimeContext 上，避免多会话互相污染 */
    private Counter counter(RuntimeContext ctx) {
        if (ctx == null) {
            return new Counter();
        }
        Counter counter = ctx.get(COUNTER_KEY, Counter.class);
        if (counter == null) {
            counter = new Counter();
            ctx.put(COUNTER_KEY, Counter.class, counter);
        }
        return counter;
    }

    /** 日志前缀：agent 名 + 会话标识 */
    private String tag(Agent agent, RuntimeContext ctx) {
        String name = agent == null ? "agent" : agent.getName();
        String session = ctx == null ? null : ctx.getSessionId();
        return session == null || session.isBlank() ? name : name + "|" + session;
    }

    private int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * 观测事件流，把影响进度的关键事件提升到 INFO / WARN。
     *
     * <p>Middleware 的 onAgent 能拿到完整的 {@code Flux<AgentEvent>}，
     * 因此它覆盖了 1.x Hook 的全部能力（含压缩、HITL、子 Agent 等 31 种事件），
     * 这也是 2.0 用 Middleware 取代 Hook 的原因。
     *
     * @param tag   日志前缀
     * @param event 事件
     */
    private void observeEvent(String tag, AgentEvent event) {
        if (event == null) {
            return;
        }
        if (logEvents && log.isDebugEnabled()) {
            log.debug("[{}] · 事件 {} | id={}", tag, event.getType(), event.getId());
        }
        AgentEventType type = event.getType();
        if (type == AgentEventType.EXCEED_MAX_ITERS) {
            log.warn("[{}] ⚠ 达到最大迭代次数，Agent 提前结束", tag);
        } else if (type == AgentEventType.REQUIRE_USER_CONFIRM) {
            log.info("[{}] ⏸ 等待用户确认工具调用（权限系统返回 ASK）", tag);
        } else if (type == AgentEventType.REQUIRE_EXTERNAL_EXECUTION) {
            log.info("[{}] ⏸ 等待外部执行结果", tag);
        } else if (type == AgentEventType.ALL_TOOLS_DENIED) {
            log.warn("[{}] ⚠ 本轮工具调用被全部拒绝", tag);
        } else if (type == AgentEventType.REQUEST_STOP) {
            log.info("[{}] ⏹ 收到停止请求", tag);
        } else if (type == AgentEventType.SUBAGENT_EXPOSED) {
            log.info("[{}] · 子 Agent 已挂载", tag);
        } else if (type == AgentEventType.AGENT_RESULT && event instanceof AgentResultEvent result) {
            if (logModelIO) {
                Msg msg = result.getResult();
                log.info("[{}] · 最终回复: {}", tag,
                        preview(msg == null ? null : msg.getTextContent()));
            } else {
                log.debug("[{}] · 产出最终结果", tag);
            }
        }
    }

    /**
     * 捕获阶段内的异常。
     *
     * <p>2.0 的 {@code HookEventType.ERROR} 已被标记废弃，异常统一通过 Reactor
     * 的错误信号处理，这里用 doOnError 捕获后打日志并原样透传，不影响主流程重试逻辑。
     */
    private Flux<AgentEvent> withErrorLog(Flux<AgentEvent> flux, String tag, String stage) {
        return flux.doOnError(error ->
                log.error("[{}] ✖ {} 异常 | {}", tag, stage, error.toString()));
    }

    /** 按耗时选择日志级别 */
    private void logStep(long elapsedMillis, String message) {
        logStep(elapsedMillis, message, false);
    }

    /** 按耗时与是否失败选择日志级别（失败优先 WARN/ERROR） */
    private void logStep(long elapsedMillis, String message, boolean failed) {
        if (failed) {
            log.warn("{} | 结果含错误", message);
        } else if (elapsedMillis >= slowThresholdMs) {
            log.warn("{}（耗时超过 {} ms）", message, slowThresholdMs);
        } else {
            log.info("{}", message);
        }
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    /** 耗时格式化：超过 1 秒用 s，否则用 ms */
    private String formatDuration(long millis) {
        if (millis >= 1_000) {
            return String.format("%.1fs", millis / 1000.0);
        }
        return millis + "ms";
    }

    /** 截断并规整预览文本 */
    private String preview(String text) {
        if (text == null) {
            return "";
        }
        String flat = text.replaceAll("\\s+", " ");
        return flat.length() <= maxPreviewLength
                ? flat
                : flat.substring(0, maxPreviewLength) + "…";
    }

    /** 取消息列表里最近的用户消息文本（太长时截断），用于回答“模型这次收到了什么” */
    private String lastUserMessage(List<Msg> messages) {
        if (messages == null) {
            return "(无消息)";
        }
        for (int i = messages.size() - 1; i >= 0; i--) {
            Msg msg = messages.get(i);
            if (msg != null && msg.getRole() == MsgRole.USER) {
                String text = msg.getTextContent();
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        }
        return "(消息列表中没有用户文本)";
    }

    /** 汇总一次 acting 内所有工具的结果：name=state | 结果文本 */
    private String summarizeToolOutcomes(Map<String, ToolOutcome> outcomes) {
        if (outcomes.isEmpty()) {
            return "";
        }
        return outcomes.values().stream()
                .map(outcome -> outcome.name + "=" + outcome.state
                        + (logModelIO && !outcome.text.isEmpty()
                                ? " | 结果: " + preview(outcome.text.toString()) : ""))
                .collect(Collectors.joining(", "));
    }

    /**
     * 判断是否有工具执行失败：状态非 SUCCESS，或结果文本出现错误特征
     * （AgentScope 会把超时等异常包装成 SUCCESS 状态 + 错误文本，因此要同时看文本）。
     */
    private boolean anyToolFailed(Map<String, ToolOutcome> outcomes) {
        for (ToolOutcome outcome : outcomes.values()) {
            if (outcome.state != ToolResultState.SUCCESS) {
                return true;
            }
            String text = outcome.text.toString();
            if (!text.isBlank()) {
                String lower = text.toLowerCase();
                if (lower.contains("exception") || lower.contains("timeout")
                        || lower.contains(" failed") || lower.contains("error")) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 单次 reply 内的推理轮次与工具调用次数 */
    private static final class Counter {
        private final AtomicInteger reasoning = new AtomicInteger();
        private final AtomicInteger acting = new AtomicInteger();
    }

    /** 单个工具调用的执行结果（含流式累积的结果文本） */
    private static final class ToolOutcome {
        private String name;
        private ToolResultState state = ToolResultState.RUNNING;
        private final StringBuilder text = new StringBuilder();

        private ToolOutcome(String name) {
            this.name = name;
        }

        private void append(String delta) {
            if (delta != null) {
                text.append(delta);
            }
        }
    }

    /**
     * 工具清单摘要，便于确认注册是否生效。
     *
     * @param toolkit 工具包
     * @return 工具名列表
     */
    public static String describeToolkit(Toolkit toolkit) {
        if (toolkit == null) {
            return "无";
        }
        List<String> names = List.copyOf(toolkit.getToolNames());
        return names.isEmpty() ? "无" : String.join(", ", names);
    }
}
