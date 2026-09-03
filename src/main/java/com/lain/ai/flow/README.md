# Agent 可观测性与调试

调试 Agent 时最常见的问题：**只看到框架自己打的日志，不知道 Agent 在干什么，一次调用 pending 很久却定位不到卡在哪。**

本文说明 AgentScope 2.0 提供了哪些生命周期节点，以及本项目如何使用它们。

---

## 一、AgentScope 2.0 的三层观测能力

| 机制 | 状态 | 覆盖点 | 适合场景 |
|------|------|--------|---------|
| **Middleware** | ✅ 2.0 主推 | 5 个插桩点 | 生命周期埋点、耗时统计、日志、限流、改写入参 |
| **streamEvents** | ✅ 推荐 | 31 种 `AgentEvent` | 交互式 UI 实时展示进度 |
| **Hook（1.x 遗留）** | ⚠️ 已废弃 | 12 种事件 | 不建议新代码使用 |

### 1. Middleware（推荐）

实现 `io.agentscope.core.middleware.MiddlewareBase`，官方在 5 个位置提供插桩，覆盖从一次完整 reply 下沉到模型 API 调用的全链路：

```
onAgent（洋葱式，包裹一次完整 reply）
  └── 每一轮 ReAct
       ├── onReasoning（洋葱式，包裹一次推理）
       │     ├── onSystemPrompt（变换式，组装 system prompt）
       │     └── onModelCall（洋葱式，包裹一次模型 API 调用）
       └── onActing（洋葱式，包裹本轮的工具调用）
```

两种类型差别：

- **Onion（洋葱式）**：`onAgent` / `onReasoning` / `onActing` / `onModelCall`。中间件包裹下一层 handler，可在 `next.apply(input)` 前后插入逻辑，`Flux<AgentEvent>` 上还能用 `doOnNext` / `map` 观察改写事件流。
- **Transformer（变换式）**：`onSystemPrompt`。多个中间件串行接力，前一个的输出作为后一个的输入。

各 hook 的入参（均为 record，位于 `io.agentscope.core.middleware`）：

| Hook | Input | 字段 |
|------|-------|------|
| `onAgent` | `AgentInput` | `msgs: List<Msg>` |
| `onReasoning` | `ReasoningInput` | `messages`、`tools`、`options` |
| `onActing` | `ActingInput` | `toolCalls: List<ToolUseBlock>` |
| `onModelCall` | `ModelCallInput` | `messages`、`tools`、`options`、`model` |
| `onSystemPrompt` | `String` | 当前 prompt |

每个 hook 的第二个参数都是本次调用的 `RuntimeContext`，可读取 `sessionId` / `userId`。

**执行顺序**：Onion 类 hook 按 `MiddlewareBase.order()` 排序，数值越大越外层，默认 `1`。

**注意**：不要把请求级状态存到中间件实例字段——实例通常被多个 agent / call 复用。要放进 `RuntimeContext`（本项目就是这么做的）。

### 2. streamEvents（实时事件流）

`agent.streamEvents(...)` 返回 `Flux<AgentEvent>`，按 `event.getType()` 分发即可。官方推荐给交互式 UI：

```java
agent.streamEvents(new UserMessage("总结一下 README 的内容。"))
        .doOnNext(event -> {
            if (event.getType() == AgentEventType.TEXT_BLOCK_DELTA) {
                System.out.print(((TextBlockDeltaEvent) event).getDelta());
            } else if (event.getType() == AgentEventType.TOOL_CALL_START) {
                System.out.println("\n[tool] " + ((ToolCallStartEvent) event).getToolCallName());
            }
        })
        .blockLast();
```

31 种事件类型见 `io.agentscope.core.event.AgentEventType`，常用的有：

| 分组 | 事件 |
|------|------|
| 生命周期 | `AGENT_START` / `AGENT_END` / `AGENT_RESULT` |
| 模型 | `MODEL_CALL_START` / `MODEL_CALL_END` |
| 流式输出 | `TEXT_BLOCK_*` / `THINKING_BLOCK_*` / `DATA_BLOCK_*` |
| 工具 | `TOOL_CALL_START/DELTA/END`、`TOOL_RESULT_START/END` |
| 异常与暂停 | `EXCEED_MAX_ITERS`、`REQUEST_STOP`、`REQUIRE_USER_CONFIRM`、`REQUIRE_EXTERNAL_EXECUTION`、`ALL_TOOLS_DENIED` |
| 子 Agent | `SUBAGENT_EXPOSED` |

### 3. Hook（已废弃，不要再用）

`io.agentscope.core.hook` 整个包在 2.0 中标记了 `@Deprecated(forRemoval = true)`，`Hook` / `HookEvent` / `RuntimeContextAware` 及各 `PreXxxEvent` 全部在内，编译会打出大量废弃警告。

**替代方案**：需要生命周期节点 → 用 Middleware；需要细粒度事件 → 用 Middleware 的 `Flux<AgentEvent>` 或 `streamEvents`（覆盖面比 Hook 更大）。

> 官方自带的 `JsonlTraceExporter`（trace 落 JSONL）也在 `io.agentscope.core.hook.recorder` 包下，同样属于废弃范围，升级时需留意。

---

## 二、本项目实现：AgentLifecycleLogger

`AgentLifecycleLogger` 实现了 `MiddlewareBase` 的全部 5 个插桩点，输出带耗时的阶段日志。

### 装配

```java
HarnessAgent agent = HarnessAgent.builder()
        .name("browser-operator")
        .model("dashscope:qwen3.7-plus")
        .toolkit(toolkit)
        .middleware(new AgentLifecycleLogger())       // 生命周期埋点
        .build();
```

构造参数：

| 参数 | 默认 | 说明 |
|------|------|------|
| `slowThresholdMs` | `5000` | 超过该耗时的步骤升级为 WARN |
| `maxPreviewLength` | `300` | 参数/结果预览的最大字符数 |
| `logEvents` | `false` | 开启后在 DEBUG 级别逐个打印 `AgentEvent` |
| `logModelIO` | `true` | 打印模型输入预览 / 输出文本 / 工具结果内容（含失败原因） |

> 只想看阶段耗时、不想要内容刷屏时，用 4 参构造关掉内容日志：
> `new AgentLifecycleLogger(5_000L, 300, false, false)`

### 输出样例

```
[browser-operator|browser-op-session] ▶ 开始 | 输入 1 条消息
[browser-operator|browser-op-session] ├ 推理 #1 | 上下文 38 条消息 | 工具 13
[browser-operator|browser-op-session] │ ├ 模型调用 | model=qwen3.7-plus | 输入 38 条消息
[browser-operator|browser-op-session] │ │ ↓ 输入预览: 帮我打开闲鱼首页看看
[browser-operator|browser-op-session] │ ├ 模型调用结束 | 耗时 2.8s | tokens 输入 12036 / 输出 512 / 合计 12548
[browser-operator|browser-op-session] ├ 推理 #1 结束 | 耗时 2.9s
[browser-operator|browser-op-session] ├ 工具调用 #1 | openPage(url=https://www.goofish.com/)
[browser-operator|browser-op-session] ├ 工具调用 #1 结束 | 耗时 9.9s | openPage=SUCCESS | 结果: 页面已打开，标题为“闲鱼”
[browser-operator|browser-op-session] ├ 工具调用 #2 | getPageSnapshot(interactiveOnly=true)
[browser-operator|browser-op-session] ├ 工具调用 #2 结束 | 耗时 3.2s | getPageSnapshot=SUCCESS | 结果: [{"tag":"div","text":"热门好物推荐"}…]
[browser-operator|browser-op-session] · 最终回复: 闲鱼首页已打开，当前页面显示……   ← AgentResultEvent
[browser-operator|browser-op-session] ◀ 结束 | 耗时 45.2s | 推理 5 轮 | 工具 4 次 | 信号 onComplete
```

工具**失败**时（含被框架包装成 SUCCESS 的错误），整行会升级为 WARN 并打出结果原文，
这通常就是失败原因——例如工具超时：

```
[browser-operator|browser-op-session] ├ 工具调用 #1 结束 | 耗时 300.1s | openPage=SUCCESS | 结果: java.lang.RuntimeException: Tool execution timeout after PT5M | 结果含错误
```

| 日志片段 | 含义 |
|----------|------|
| `│ │ ↓ 输入预览: …` | 发给模型的消息里，最近一条用户消息的文本（过长截断） |
| `模型调用结束 … 输出: …` | 模型回复的文本（工具调用类回复无文本行，改看下一行工具调用） |
| `工具调用 #N 结束 … 结果: …` | 工具返回的内容预览（含错误堆栈/超时原因） |
| `· 最终回复: …` | 整个 Agent 最终产出的文本（`AgentResultEvent`） |

配合 `BrowserAssistant` 自己的命令级日志（`[browser] ▶ / ◀`），可以精确到"这一次工具调用里，CLI 实际执行了哪条命令、花了多久"：

```
[browser] ▶ open | 参数: https://www.goofish.com/ | 超时 120s
[browser] ◀ open | 耗时 9.9s | 退出码 0 | 输出: Opened https://www.goofish.com/
```

### 关键事件提示

中间件会把影响进度的事件提升到 INFO / WARN：

| 日志 | 含义 |
|------|------|
| `⏸ 等待用户确认工具调用` | 权限系统返回 ASK，Agent **真的在等人**，不是卡死 |
| `⏸ 等待外部执行结果` | 调用了外部执行工具，等待外部回传 |
| `⚠ 达到最大迭代次数` | `maxIters` 用尽，Agent 被强制结束 |
| `⚠ 本轮工具调用被全部拒绝` | 权限系统拒绝了全部工具调用 |
| `⏹ 收到停止请求` | 被 `interrupt()` 中断 |
| `✖ xxx 异常` | 阶段内抛出异常 |

### 如何定位"卡在哪"

1. 看 `◀ 结束` 那行的总耗时与轮次分布，判断是**轮次太多**还是**单步太慢**。
2. 找带 `（耗时超过 5000 ms）` 的 WARN 行，那就是瓶颈步骤。
3. 若瓶颈在 `模型调用`，看 tokens 是否过大（上下文膨胀），考虑调小 `compaction.triggerMessages` 或精简工具输出。
4. 若瓶颈在 `工具调用`，对照紧随其后的 `[browser] ▶/◀` 行，确认是哪条 CLI 命令慢（超过 10 秒会标 WARN）。
5. 若迟迟没有新日志且出现 `⏸`，说明在等待人工确认或外部执行，不是卡死。

---

## 三、日志输出位置

- **直接运行 `main` 方法**：Logback 使用默认配置，root 级别 DEBUG 输出到控制台，日志直接可见。
- **Spring Boot 环境**：`logback-spring.xml` 已把 `com.lain` 设为 DEBUG（dev / test），prod 为 ERROR。
- 只想看生命周期日志时，可临时加一条 logger 配置：

```xml
<logger name="com.lain.ai.flow.AgentLifecycleLogger" level="INFO"/>
<logger name="com.lain.ai.tools.BrowserAssistant" level="INFO"/>
<logger name="io.agentscope" level="WARN"/>   <!-- 压掉框架自身日志 -->
```

---

## 四、进阶：接入 OpenTelemetry

框架内置 `io.agentscope.core.tracing.OtelTracingMiddleware`，在 `onAgent` / `onModelCall` / `onActing` 三个位置生成嵌套 span（`invoke_agent` → `chat` → `execute_tool`），属性含 agent 名、session、模型名、token 数、工具名与入参。未配置 OTel SDK 时会短路，几乎零开销。

需要额外引入依赖（版本需与框架一致，官方示例为 `1.61.0`）：

```xml
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-sdk</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>
```

装配：

```java
SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
        .addSpanProcessor(BatchSpanProcessor.builder(OtlpHttpSpanExporter.builder()
                .setEndpoint("http://localhost:4318/v1/traces")
                .build())
                .build())
        .build();
OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal();

HarnessAgent agent = HarnessAgent.builder()
        .middleware(new OtelTracingMiddleware())
        .build();
```

> 必须在 middleware 工作前注册 SDK；应用关闭时调用 `tracerProvider.close()` 刷新未导出的 span。

---

## 五、文件清单

| 文件 | 职责 |
|------|------|
| `flow/AgentLifecycleLogger.java` | Middleware 实现，5 个插桩点全量埋点 + 耗时统计 + 关键事件提示 |
| `flow/BrowserOperationAgent.java` | 演示装配方式 |
| `tools/BrowserAssistant.java` | 浏览器命令级日志（命令、耗时、退出码、输出摘要） |
