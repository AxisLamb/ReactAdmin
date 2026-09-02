---
name: AgentScope 2.0 浏览器自动化工具
overview: 为自研 RBAC 后端项目中的 AgentScope 2.0 智能体编写一个基于 Vercel agent-browser CLI 的浏览器自动化工具（BrowserAssistant），支持打开网页、截图、点击、表单填写、页面内容提取等操作，首次使用自动安装，并附带演示 Agent 与文档。
todos:
  - id: browser-assistant
    content: 参考 [skill:agent-browser] 实现 BrowserAssistant 工具类：agent-browser CLI 自动安装检测、进程执行封装（参数数组+超时）及 open/snapshot/screenshot/click/type/press/select/wait/close 等 @Tool 方法，风格对齐 FileAssistant
    status: completed
  - id: browser-agent
    content: 实现 BrowserOperationAgent 演示 Agent：注册 BrowserAssistant 到 Toolkit，构建 HarnessAgent，演示打开网页、快照、截图、关闭的完整流程
    status: completed
    dependencies:
      - browser-assistant
  - id: update-readme
    content: 更新 tools/README.md：新增浏览器自动化工具章节，记录工具列表、参数、使用示例与安全注意事项
    status: completed
    dependencies:
      - browser-assistant
  - id: compile-verify
    content: 执行 mvn compile 验证编译通过，修复 BrowserAssistant 与 BrowserOperationAgent 的编译问题
    status: completed
    dependencies:
      - browser-agent
      - update-readme
---

## 产品概述
为现有 AgentScope 2.0 集成环境新增一个"浏览器自动化工具（Browser CLI）"，让 AI 智能体能够通过 Vercel agent-browser CLI 操作真实浏览器，完成网页交互、截图、表单填写等任务。首次调用时自动安装 agent-browser 及 Chromium 运行时，后续调用直接复用常驻浏览器守护进程。

## 核心功能
- 打开/导航网页：open、navigate、back、forward、reload
- 页面内容提取：snapshot（含交互元素模式），供智能体理解页面结构
- 浏览器截图：支持视口截图与整页截图，保存为 PNG 文件
- 页面交互：click 点击、type 输入文本、press 按键、select 下拉选择
- 等待机制：等待页面加载完成（networkidle/load/domcontentloaded）
- 会话管理：close 关闭浏览器并释放资源（任务结束时调用）
- 首次使用自动安装：检测命令不存在时自动执行 npm 全局安装 + 浏览器下载

## 交付形态
- 浏览器工具类（注册进 Toolkit，供 HarnessAgent 调用）
- 可运行的演示 Agent（main 方法，展示"打开网页 → 快照 → 截图 → 关闭"完整流程）
- 工具文档更新


## 技术栈选择
- 复用项目现有技术栈：Java 21 + Spring Boot 3.2 + AgentScope 2.0（`agentscope-harness` 2.0.0，已配置于 pom.xml）
- 外部依赖：Vercel agent-browser CLI（npm 全局安装，Windows x64 支持），由工具在运行时自动安装，无需修改 pom.xml
- 进程调用：`ProcessBuilder` 参数数组方式执行 agent-browser 命令（避免 shell 命令注入），沿用 FileAssistant 的"读 stdout+stderr+退出码"模式

## 实现方案
核心思路：编写 `BrowserAssistant` 工具类，每个 `@Tool` 方法封装一个 agent-browser CLI 命令；所有命令通过统一私有方法执行，执行前调用幂等的自动安装检测。Agent 通过 `Toolkit.registerTool(browserAssistant)` 注册后即可获得浏览器操作能力。

### 关键设计决策
1. **自动安装（首次使用）**：每次工具调用前执行 `ensureInstalled()`——运行 `agent-browser --version` 探测，若命令不可用则执行安装：Windows 用 `cmd /c "npm install -g agent-browser && agent-browser install"`，Linux/macOS 用 `bash -lc` 同串命令；已安装则零开销跳过，保证幂等。
2. **命令执行封装**：运行类命令（open/snapshot/click 等）用 `ProcessBuilder("agent-browser", arg1, arg2, ...)` 参数数组直连执行（不经过 shell，防注入）；仅安装命令需 shell 拼接。统一收集 stdout/stderr，返回"输出 + 退出码"，非零退出码作为错误信息返回给 Agent。
3. **超时保护**：所有进程执行加超时（默认 60s，`waitFor(timeout)`），超时 `destroy()` 进程并返回错误提示，防止 `wait --load networkidle` 在 SPA 页面无限挂起（agent-browser 官方已知行为）。
4. **截图处理**：`screenshot` 保存到 `.agentscope/workspace/browser-shots/`（自动创建目录，文件名带时间戳），返回文件绝对路径字符串，简单可靠；若后续需要多模态回传可升级为 `ToolResultBlock` 图片块（本次不做，保持 YAGNI）。
5. **会话模型**：agent-browser 为常驻 daemon 模式，`open` 首次启动 daemon，后续命令自动连接，跨进程调用保持 Cookie/登录态；`close` 结束 daemon。因此工具方法之间无需特殊状态管理，但 sysPrompt 中需提示 Agent"任务结束必须调用 close"。
6. **错误处理**：沿用 FileAssistant 模式——方法内部 try-catch，返回可读错误字符串而非抛异常（`IOException/InterruptedException` 捕获后返回"浏览器操作失败: 原因"），仅在进程启动即失败时抛出 `AgentShuttingDownException` 保持一致。

### 工具方法清单（全部返回 String，参数用中文 description）
- `openPage(url)`：打开/导航到 URL（启动浏览器）
- `navigateTo(url)`：导航到新 URL
- `waitForPageLoad(loadType)`：等待加载（networkidle/load/domcontentloaded，默认 networkidle）
- `getPageSnapshot(interactiveOnly)`：页面快照（false=全文，true=仅交互元素）
- `takeScreenshot(fullPage, outputDir)`：截图，支持整页模式，返回文件路径
- `clickElement(selector)`：点击元素（CSS 选择器）
- `typeText(selector, text)`：向输入框输入文本
- `pressKey(key)`：按键（Enter/Tab 等）
- `selectOption(selector, value)`：下拉框选择
- `goBack()` / `goForward()` / `reloadPage()`：导航历史操作
- `closeBrowser()`：关闭浏览器释放资源

## 实现注意事项
- **复用现有模式**：`@Tool`/`@ToolParam` 注解、`AgentShuttingDownException` 异常、main 方法先检查 `DASHSCOPE_API_KEY`，与 FileAssistant/FileOperationAgent 完全一致，不引入新依赖、不新增配置文件。
- **Windows 特殊性**：环境为 win32 + cmd.exe，命令可用性检测必须用 `where agent-browser`（Windows）/ `which agent-browser`（Unix）兼容处理；npm 全局 bin 可能不在 PATH，安装后需重新探测一次。
- **爆炸半径控制**：仅新增 2 个 Java 文件 + 更新 1 个 README，不触碰既有 RBAC 模块、不修改 pom.xml（agent-browser 为运行时外部工具，非 Maven 依赖）。
- **安全**：参数数组防注入；浏览器操作属外部副作用，文档中注明生产环境应配合 AgentScope 工具权限管控（工具组激活/停用）限制调用方。
- **日志**：与现有代码风格一致，不引入 Logger，错误信息通过返回值透传给 Agent。

## 架构设计
调用链：HarnessAgent（ReAct 决策）→ Toolkit → BrowserAssistant（@Tool 方法）→ ProcessBuilder 调用 agent-browser CLI → Chromium daemon（跨调用复用会话）。BrowserAssistant 与 BrowserOperationAgent 分层解耦，与现有 FileAssistant/FileOperationAgent 结构完全对齐。

```mermaid
flowchart LR
    A[HarnessAgent<br/>BrowserOperationAgent] -->|注册 Toolkit| B[BrowserAssistant<br/>@Tool 方法]
    B -->|ProcessBuilder 参数数组| C[agent-browser CLI<br/>npm 全局安装]
    C --> D[Chromium Daemon<br/>常驻会话]
    B -.首次调用.-> E[自动安装<br/>npm install + browser download]
    B -->|返回 String| A
```

## 目录结构
```
backend/
├── src/main/java/com/lain/ai/
│   ├── tools/
│   │   ├── BrowserAssistant.java   # [NEW] 浏览器自动化工具类。封装 agent-browser CLI：ensureInstalled 自动安装探测（cmd/bash 兼容）、executeAgentBrowser 统一进程执行（参数数组+超时+stdout/stderr 收集）、13 个 @Tool 方法（open/navigate/wait/snapshot/screenshot/click/type/press/select/back/forward/reload/close），截图落盘到 .agentscope/workspace/browser-shots/ 并返回绝对路径。风格对齐 FileAssistant。
│   │   └── README.md               # [MODIFY] 追加"AgentScope 2.0 浏览器自动化工具"章节：工具列表、参数说明、使用示例（BrowserOperationAgent）、安全考虑（生产环境权限管控、close 必调、daemon 会话说明）。
│   └── flow/
│       └── BrowserOperationAgent.java  # [NEW] 演示 Agent。检查 DASHSCOPE_API_KEY → new Toolkit() 注册 BrowserAssistant → HarnessAgent.builder()（name=browser-operator、model=dashscope:qwen3.7-plus、workspace、toolkit、compaction）→ RuntimeContext → 两轮 agent.call 演示"打开 example.com → 快照/截图 → close"，与 FileOperationAgent 完全同构。
```

## 关键代码结构（接口级）
BrowserAssistant 核心私有方法与公开工具方法签名（实现细节以文本描述为准）：

```java
public class BrowserAssistant {
    // 私有：探测并自动安装 agent-browser（幂等）
    private boolean ensureInstalled() { ... }
    // 私有：统一执行 agent-browser 命令，返回 stdout+stderr+退出码
    private String executeAgentBrowser(String... args) { ... }

    @Tool(description = "打开指定URL的网页，首次调用会自动启动浏览器")
    public String openPage(@ToolParam(name = "url", description = "要打开的网页地址") String url) { ... }

    @Tool(description = "在当前浏览器中截取页面截图并保存为PNG文件，返回文件路径")
    public String takeScreenshot(
            @ToolParam(name = "fullPage", description = "是否整页截图，默认false仅视口") boolean fullPage,
            @ToolParam(name = "outputDir", description = "截图保存目录，可选") String outputDir) { ... }

    @Tool(description = "获取当前页面内容快照，供分析页面结构")
    public String getPageSnapshot(
            @ToolParam(name = "interactiveOnly", description = "true仅返回可交互元素，false返回全部内容") boolean interactiveOnly) { ... }

    @Tool(description = "点击页面中的元素，selector为CSS选择器")
    public String clickElement(@ToolParam(name = "selector", description = "CSS选择器") String selector) { ... }

    // 其余 @Tool 方法：navigateTo / waitForPageLoad / typeText / pressKey /
    // selectOption / goBack / goForward / reloadPage / closeBrowser，签名同上模式
}
```



## Agent 扩展
### Skill
- **agent-browser**
  - 用途：在实现 BrowserAssistant 时确认 agent-browser CLI 的精确命令语法（open/snapshot/screenshot/click/type/press/select/wait/close 及参数选项）、Windows 安装与 PATH 注意事项、daemon 会话模型与故障排查要点，确保工具封装与官方 CLI 行为一致。
  - 预期产出：工具类中每个 @Tool 方法对应的 CLI 命令拼接准确无误，自动安装逻辑覆盖 Windows cmd 场景，close 生命周期提示写入 Agent sysPrompt 与 README。
