# AgentScope 2.0 CentOS 文件系统操作工具

## 概述

这是一个基于 AgentScope 2.0 框架开发的 CentOS 文件系统操作工具集合。该工具集允许 AI 智能体安全地执行常见的文件系统操作任务，如创建目录、读写文件、执行命令等。

## 工具列表

### 1. executeShellCommand
- **功能**: 在 CentOS 系统上执行 shell 命令
- **参数**: `command` - 要执行的 shell 命令
- **返回**: 命令执行结果

### 2. listDirectory  
- **功能**: 列出指定目录下的文件和子目录
- **参数**: `path` - 要列出的目录路径
- **返回**: 目录内容列表，包括文件类型和大小

### 3. readFile
- **功能**: 读取文件内容
- **参数**: `path` - 要读取的文件路径
- **返回**: 文件内容

### 4. writeFile
- **功能**: 写入内容到文件（如果文件不存在则创建）
- **参数**: 
  - `path` - 要写入的文件路径
  - `content` - 要写入的文件内容
- **返回**: 操作结果

### 5. createDirectory
- **功能**: 创建目录（如果父目录不存在也会一同创建）
- **参数**: `path` - 要创建的目录路径
- **返回**: 操作结果

### 6. deleteFileOrDirectory
- **功能**: 删除文件或空目录
- **参数**: `path` - 要删除的文件或目录路径
- **返回**: 操作结果

### 7. searchFiles
- **功能**: 在指定目录中搜索符合模式的文件
- **参数**:
  - `directory` - 要搜索的目录路径
  - `pattern` - 文件名模式（支持通配符，如 *.txt, *.log 等）
- **返回**: 找到的文件列表

### 8. getFileDetails
- **功能**: 获取文件的详细信息，包括大小、修改时间等
- **参数**: `path` - 文件或目录路径
- **返回**: 文件详细信息

## 使用示例

参见 `FileOperationAgent.java` 文件，其中展示了如何在 AgentScope 2.0 环境中使用这些工具。

## 安全考虑

- 所有文件操作都限制在 CentOS 系统上执行
- 命令执行使用安全的 bash 环境
- 工具仅在必要时创建目录结构
- 删除操作检查目录是否为空，避免误删

## 注意事项

- 确保系统中设置了 DASHSCOPE_API_KEY 环境变量
- 某些操作可能需要适当的文件系统权限
- 在生产环境中使用时，请仔细审查和测试所有操作

---

# AgentScope 2.0 浏览器自动化工具（Browser CLI）

## 概述

基于 Vercel [agent-browser](https://github.com/vercel-labs/agent-browser) CLI 的浏览器自动化工具集，让 AI 智能体能够操作真实浏览器：打开网页、读取页面内容、截图、点击按钮、填写表单等。

**首次使用时会自动安装**（`npm install -g agent-browser` + `agent-browser install`），安装成功后自动缓存检测结果，后续调用零开销。

**会话模型**：agent-browser 采用常驻守护进程模式。首次 `open` 启动浏览器，后续命令自动连接同一会话，跨调用保留 Cookie 与登录态，适合完成"登录 → 操作 → 取数"这类多步骤任务。**任务结束必须调用 `closeBrowser` 关闭浏览器释放资源。**

## 环境要求

- Node.js 18+ 且 npm 已加入 PATH（仅首次自动安装时需要）
- 约 500MB 磁盘空间用于下载 Chromium
- 支持 macOS / Linux / Windows x64

## 工具列表

### 页面导航

| 工具方法 | 功能 | 参数 |
|---------|------|------|
| `openPage` | 打开指定 URL，首次调用会自动启动浏览器 | `url` - 网页地址 |
| `navigateTo` | 在当前会话中导航到新 URL（保留登录态） | `url` - 网页地址 |
| `goBack` | 浏览器后退 | 无 |
| `goForward` | 浏览器前进 | 无 |
| `reloadPage` | 刷新当前页面 | 无 |
| `closeBrowser` | 关闭浏览器并释放资源 | 无 |

### 页面读取与截图

| 工具方法 | 功能 | 参数 |
|---------|------|------|
| `getPageSnapshot` | 获取页面内容快照（文本） | `interactiveOnly` - true 仅返回可交互元素 |
| `takeScreenshot` | 截图并保存为 PNG，返回绝对路径 | `fullPage` - 是否整页截图；`outputDir` - 保存目录（可选） |
| `waitForPageLoad` | 等待页面加载完成 | `loadType` - networkidle / load / domcontentloaded |

### 页面交互

| 工具方法 | 功能 | 参数 |
|---------|------|------|
| `clickElement` | 点击元素 | `selector` - CSS / text= / xpath= 选择器 |
| `typeText` | 向输入框键入文本 | `selector` - 输入框选择器；`text` - 文本内容 |
| `pressKey` | 按下键盘按键 | `key` - 如 Enter、Tab、Escape、Control+a |
| `selectOption` | 下拉框选择 | `selector` - 下拉框选择器；`value` - 选项值 |

### 环境诊断

| 工具方法 | 功能 | 参数 |
|---------|------|------|
| `checkBrowserEnvironment` | 输出 node 路径、CLI 启动命令、版本探测结果、PATH 检查结论 | 无 |

浏览器操作报"命令不存在 / 未安装"时，让 Agent 先调用它，可直接拿到定位所需的信息。

## 使用示例

参见 `BrowserOperationAgent.java`，演示了完整的浏览器操作链路：

```java
BrowserAssistant browserAssistant = new BrowserAssistant();

Toolkit toolkit = new Toolkit();
toolkit.registerTool(browserAssistant);

HarnessAgent agent = HarnessAgent.builder()
        .name("browser-operator")
        .sysPrompt("你是一个帮助用户操作浏览器的助手...")
        .model("dashscope:qwen3.7-plus")
        .workspace(Paths.get(".agentscope/workspace"))
        .toolkit(toolkit)
        .compaction(CompactionConfig.builder()
                .triggerMessages(30)
                .keepMessages(10)
                .build())
        .build();

RuntimeContext ctx = RuntimeContext.builder()
        .sessionId("browser-op-session")
        .userId("alice")
        .build();

agent.call(Msg.builder()
        .textContent("请打开 https://example.com 这个网页，并告诉我页面的标题和主要内容是什么。")
        .build(), ctx).block();
```

**典型任务流程**：

```
openPage(打开网页) → waitForPageLoad(等待加载)
  → getPageSnapshot(interactiveOnly=true)(定位元素)
  → clickElement / typeText / pressKey(交互)
  → takeScreenshot(截图留存)
  → closeBrowser(结束任务必须关闭)
```

## 安全考虑

- 浏览器操作属于外部副作用操作，生产环境应配合 AgentScope 工具权限管控（工具组激活/停用）限制调用方
- 命令通过 `ProcessBuilder` 参数数组执行，避免 shell 命令注入
- 所有进程执行带超时保护（默认 60 秒），防止 `wait --load networkidle` 在 SPA 页面无限挂起
- 截图文件保存在 `.agentscope/workspace/browser-shots/` 目录下

## 可观测性

调试时想看清楚 Agent 每一步在做什么，`BrowserAssistant` 会输出命令级日志：

```
[browser] ▶ open | 参数: https://example.com | 超时 120s
[browser] ◀ open | 耗时 9.9s | 退出码 0 | 输出: Opened https://example.com/
```

超过 10 秒或退出码非 0 的命令自动升级为 WARN。环境不可用时会把完整诊断信息（node 路径、启动命令、探测结果、安装日志）一次性打出。

想要"推理 / 模型调用 / 工具调用"各阶段的耗时，挂载 `AgentLifecycleLogger` 中间件即可，详见 [`flow/README.md`](../flow/README.md)。

## Windows 排错

### 现象：cmd 里 `agent-browser install` 成功，但 Java 里报找不到命令

原因是 npm 在 Windows 上生成的是 **`agent-browser.cmd` 批处理垫片**，而不是 `.exe`。
`ProcessBuilder` 底层用 `CreateProcess`，**无法直接执行 `.cmd`/`.bat`**，会抛
`CreateProcess error=2, 系统找不到指定的文件`。

本类已内置规避：直接定位 npm 全局包的 JS 入口，改用
`node <npm全局目录>\node_modules\agent-browser\bin\agent-browser.js` 启动。
这样既绕开了 `.cmd` 限制，又保持参数以数组传递，无需 `cmd /c` 带来的
`&` `^` `%` 转义问题（URL 里带 `&` 时 `cmd /c` 会被截断）。

命令解析顺序：

1. 环境变量 `AGENT_BROWSER_BIN`（显式指定，优先级最高）
2. 找到 node → 询问 `npm root -g` + 常见全局目录 → 读包 `package.json` 的 `bin` 字段定位 JS 入口
3. 兜底直接执行 `agent-browser`（Unix 或 PATH 中命令可直接执行的场景）

node 的查找顺序：`NODE_BIN` 环境变量 → PATH → 常见安装目录
（`C:\Program Files\nodejs`、`%APPDATA%\nvm\<版本>`、`%USERPROFILE%\AppData\Roaming\nvm\current`、Unix 系 `/usr/local/bin` 等）。

### 现象：Java 进程 PATH 里没有 npm 目录

IDE 启动的 Java 进程继承的是 **IDE 启动那一刻**的 PATH。安装 Node.js 后若没重启 IDE，
Java 进程 PATH 中可能没有 node 目录。代码会在启动子进程时自动把 node 目录与
`%APPDATA%\npm` 补进子进程 PATH，无需重启 IDE。

### 手动兜底

```bat
:: 查看全局包根目录
npm root -g

:: 查看 CLI 入口（用上面目录拼路径）
type "%APPDATA%\npm\node_modules\agent-browser\package.json"
```

然后把入口路径写入环境变量即可强制使用：

```bat
setx AGENT_BROWSER_BIN "C:\Users\joezf\AppData\Roaming\npm\node_modules\agent-browser\bin\agent-browser.js"
setx NODE_BIN "C:\Program Files\nodejs\node.exe"
```

设置后重启 IDE 生效。