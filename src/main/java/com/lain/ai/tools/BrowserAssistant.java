package com.lain.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.shutdown.AgentShuttingDownException;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 浏览器自动化工具（基于 Vercel agent-browser CLI）
 *
 * <p>为 AgentScope 2.0 智能体提供真实浏览器操作能力：打开网页、页面快照、
 * 截图、点击元素、填写表单、按键、下拉选择、等待加载、前进后退与关闭浏览器。
 *
 * <p>首次调用时会自动执行 {@code npm install -g agent-browser} 与
 * {@code agent-browser install} 完成 CLI 与 Chromium 的安装，之后直接复用。
 *
 * <p>agent-browser 采用常驻守护进程模型：首次 {@code open} 启动浏览器，
 * 后续命令自动连接同一会话，Cookie 与登录态在多次调用之间保持，
 * 因此任务结束时务必调用 {@link #closeBrowser()} 释放资源。
 */
public class BrowserAssistant {

    /** agent-browser 命令名 */
    private static final String CLI = "agent-browser";

    /** 环境变量：显式指定 agent-browser 可执行文件（或其 JS 入口）的完整路径 */
    private static final String ENV_AGENT_BROWSER_BIN = "AGENT_BROWSER_BIN";

    /** 环境变量：显式指定 node 可执行文件完整路径 */
    private static final String ENV_NODE_BIN = "NODE_BIN";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 常规命令默认超时（秒） */
    private static final long DEFAULT_TIMEOUT_SECONDS = 60L;

    /** 首次打开浏览器（含守护进程冷启动）超时（秒） */
    private static final long OPEN_TIMEOUT_SECONDS = 120L;

    /** 安装命令超时（秒），首次需下载 Chromium，耗时较长 */
    private static final long INSTALL_TIMEOUT_SECONDS = 600L;

    /** 截图默认输出目录 */
    private static final String DEFAULT_SCREENSHOT_DIR = ".agentscope/workspace/browser-shots";

    /** 截图文件名时间格式 */
    private static final DateTimeFormatter SHOT_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /** 用于异步排空标准错误流，避免缓冲区打满导致进程阻塞 */
    private static final ExecutorService DRAIN_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "agent-browser-stream-drain");
        t.setDaemon(true);
        return t;
    });

    /** 安装检测结果缓存，保证自动安装逻辑幂等 */
    private volatile boolean installChecked = false;

    /** 解析后的 CLI 启动命令，形如 [agent-browser] 或 [node, C:\...\agent-browser\dist\cli.js] */
    private volatile List<String> resolvedCli;

    /** 最近一次环境探测/安装失败的详情，用于给调用方（与用户）输出可诊断的信息 */
    private volatile String lastFailure;

    // ==================== 核心工具方法 ====================

    /**
     * 打开指定网页
     *
     * @param url 要打开的网页地址
     * @return 打开结果
     */
    @Tool(description = "打开指定URL的网页。首次调用会自动启动浏览器并自动安装agent-browser，后续调用会复用同一个浏览器会话（保留Cookie和登录态）")
    public String openPage(
            @ToolParam(name = "url", description = "要打开的网页地址，例如 https://example.com") String url) {
        if (isBlank(url)) {
            return "打开网页失败: url 不能为空";
        }
        return execute(OPEN_TIMEOUT_SECONDS, "open", url);
    }

    /**
     * 导航到新的网页地址
     *
     * @param url 目标网页地址
     * @return 导航结果
     */
    @Tool(description = "在当前浏览器窗口中导航到新的URL地址，不会关闭已有会话，登录态会保留")
    public String navigateTo(
            @ToolParam(name = "url", description = "要导航到的网页地址") String url) {
        if (isBlank(url)) {
            return "导航失败: url 不能为空";
        }
        return execute(DEFAULT_TIMEOUT_SECONDS, "navigate", url);
    }

    /**
     * 等待页面加载完成
     *
     * @param loadType 加载完成判定方式：networkidle / load / domcontentloaded
     * @return 等待结果
     */
    @Tool(description = "等待页面加载完成。networkidle表示网络空闲（适合动态页面），load表示资源加载完成，domcontentloaded最快。如果networkidle长时间不返回，请改用load或domcontentloaded")
    public String waitForPageLoad(
            @ToolParam(name = "loadType", description = "加载完成的判定方式，可选值：networkidle、load、domcontentloaded，默认 networkidle") String loadType) {
        String type = isBlank(loadType) ? "networkidle" : loadType.trim();
        if (!type.equals("networkidle") && !type.equals("load") && !type.equals("domcontentloaded")) {
            return "等待失败: loadType 只能是 networkidle、load 或 domcontentloaded，当前值: " + type;
        }
        return execute(DEFAULT_TIMEOUT_SECONDS, "wait", "--load", type);
    }

    /**
     * 获取页面内容快照
     *
     * @param interactiveOnly 是否仅返回可交互元素
     * @return 页面内容快照
     */
    @Tool(description = "获取当前页面的内容快照（文本形式），用于了解页面结构、读取文案和定位元素。interactiveOnly=true时只返回按钮、链接、输入框等可交互元素，更适合在点击或填写表单前使用")
    public String getPageSnapshot(
            @ToolParam(name = "interactiveOnly", description = "true仅返回可交互元素（按钮/链接/输入框），false返回页面全部内容") boolean interactiveOnly) {
        if (interactiveOnly) {
            return execute(DEFAULT_TIMEOUT_SECONDS, "snapshot", "-i");
        }
        return execute(DEFAULT_TIMEOUT_SECONDS, "snapshot");
    }

    /**
     * 截取当前页面截图
     *
     * @param fullPage 是否整页截图
     * @param outputDir 截图保存目录，可为空
     * @return 截图文件路径
     */
    @Tool(description = "截取当前浏览器页面的截图并保存为PNG文件，返回文件的绝对路径。fullPage=true时截取整个页面（含滚动区域），否则只截当前视口")
    public String takeScreenshot(
            @ToolParam(name = "fullPage", description = "是否截取整个页面，true为整页截图，false为仅当前视口") boolean fullPage,
            @ToolParam(name = "outputDir", description = "截图保存目录，留空则保存到 .agentscope/workspace/browser-shots") String outputDir) {
        Path dir = Paths.get(isBlank(outputDir) ? DEFAULT_SCREENSHOT_DIR : outputDir.trim());
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            return "截图失败: 无法创建保存目录 " + dir + "，原因: " + e.getMessage();
        }

        String fileName = "screenshot_" + LocalDateTime.now().format(SHOT_TIME_FORMAT) + ".png";
        Path file = dir.resolve(fileName).toAbsolutePath().normalize();

        String result;
        if (fullPage) {
            result = execute(DEFAULT_TIMEOUT_SECONDS, "screenshot", file.toString(), "--full");
        } else {
            result = execute(DEFAULT_TIMEOUT_SECONDS, "screenshot", file.toString());
        }

        if (Files.exists(file)) {
            return "截图已保存: " + file + "\n" + result;
        }
        return result + "\n提示: 未找到截图文件 " + file + "，请检查 agent-browser 输出信息。";
    }

    /**
     * 点击页面元素
     *
     * @param selector CSS 选择器
     * @return 点击结果
     */
    @Tool(description = "点击页面中的元素。selector 支持 CSS 选择器（如 #submit、.btn-primary）、文本选择器（text=登录）和 XPath（xpath=//button）。建议先用 getPageSnapshot 的 interactiveOnly 模式确认元素存在")
    public String clickElement(
            @ToolParam(name = "selector", description = "要点击的元素选择器，支持CSS选择器、text=文本、xpath=表达式") String selector) {
        if (isBlank(selector)) {
            return "点击失败: selector 不能为空";
        }
        return execute(DEFAULT_TIMEOUT_SECONDS, "click", selector.trim());
    }

    /**
     * 向输入框输入文本
     *
     * @param selector 输入框选择器
     * @param text 要输入的文本
     * @return 输入结果
     */
    @Tool(description = "在指定的输入框中键入文本，用于填写表单、搜索框等。会模拟真实键盘输入")
    public String typeText(
            @ToolParam(name = "selector", description = "输入框的选择器，支持CSS选择器或text=文本") String selector,
            @ToolParam(name = "text", description = "要输入的文本内容") String text) {
        if (isBlank(selector)) {
            return "输入失败: selector 不能为空";
        }
        if (text == null) {
            text = "";
        }
        return execute(DEFAULT_TIMEOUT_SECONDS, "type", selector.trim(), text);
    }

    /**
     * 按下键盘按键
     *
     * @param key 按键名称
     * @return 按键结果
     */
    @Tool(description = "按下键盘按键，例如 Enter、Tab、Escape、Control+a。常用于提交表单或关闭弹窗")
    public String pressKey(
            @ToolParam(name = "key", description = "按键名称，例如 Enter、Tab、Escape、ArrowDown、Control+a") String key) {
        if (isBlank(key)) {
            return "按键失败: key 不能为空";
        }
        return execute(DEFAULT_TIMEOUT_SECONDS, "press", key.trim());
    }

    /**
     * 下拉框选择
     *
     * @param selector 下拉框选择器
     * @param value 选项值
     * @return 选择结果
     */
    @Tool(description = "在下拉选择框中选择指定值的选项")
    public String selectOption(
            @ToolParam(name = "selector", description = "下拉框的选择器") String selector,
            @ToolParam(name = "value", description = "要选择的选项值") String value) {
        if (isBlank(selector)) {
            return "选择失败: selector 不能为空";
        }
        return execute(DEFAULT_TIMEOUT_SECONDS, "select", selector.trim(), value == null ? "" : value);
    }

    /**
     * 浏览器后退
     *
     * @return 操作结果
     */
    @Tool(description = "浏览器后退到上一个页面")
    public String goBack() {
        return execute(DEFAULT_TIMEOUT_SECONDS, "back");
    }

    /**
     * 浏览器前进
     *
     * @return 操作结果
     */
    @Tool(description = "浏览器前进到下一个页面")
    public String goForward() {
        return execute(DEFAULT_TIMEOUT_SECONDS, "forward");
    }

    /**
     * 刷新页面
     *
     * @return 操作结果
     */
    @Tool(description = "刷新当前页面")
    public String reloadPage() {
        return execute(DEFAULT_TIMEOUT_SECONDS, "reload");
    }

    /**
     * 关闭浏览器
     *
     * @return 操作结果
     */
    @Tool(description = "关闭浏览器并释放资源。所有浏览器任务完成后必须调用此方法，否则浏览器守护进程会一直驻留占用内存")
    public String closeBrowser() {
        return execute(DEFAULT_TIMEOUT_SECONDS, "close");
    }

    /**
     * 检查浏览器工具的运行环境
     *
     * @return 环境诊断报告
     */
    @Tool(description = "检查浏览器工具的运行环境，输出 node 路径、agent-browser 启动命令、版本探测结果与 PATH 检查结论。当浏览器操作报“命令不存在/未安装”时，优先调用此方法定位问题")
    public String checkBrowserEnvironment() {
        StringBuilder report = new StringBuilder();
        report.append("操作系统: ").append(System.getProperty("os.name")).append("\n");

        String node = findNode();
        report.append("node: ").append(node == null ? "未找到（请安装 Node.js 18+ 或设置 NODE_BIN）" : node).append("\n");

        List<String> command = cliCommand();
        report.append("启动命令: ").append(String.join(" ", command)).append("\n");
        report.append("是否走 node 直调: ")
                .append(command.size() > 1 && command.get(0).equals(node) ? "是（已绕开 .cmd 垫片）" : "否")
                .append("\n");

        if (node != null) {
            Path script = null;
            for (Path root : globalNodeModuleRoots(node)) {
                script = findPackageBin(root);
                if (script != null) {
                    break;
                }
            }
            report.append("CLI 入口: ").append(script == null ? "未定位到 package.json bin 入口" : script).append("\n");
        }

        Probe probe = probeCli();
        report.append("版本探测: ")
                .append(probe.available ? "可用（" + probe.detail + "）" : "不可用（" + probe.detail + "）")
                .append("\n");

        String pathEnv = System.getenv("PATH");
        boolean pathHasNode = node != null && pathEnv != null
                && pathEnv.toLowerCase(Locale.ROOT)
                        .contains(Paths.get(node).getParent().toString().toLowerCase(Locale.ROOT));
        report.append("当前进程 PATH 含 node 目录: ").append(pathHasNode ? "是" : "否（IDE 可能未重启，代码已自动补齐子进程 PATH）");

        return report.toString();
    }

    // ==================== 命令执行与自动安装 ====================

    /**
     * 执行 agent-browser 命令
     *
     * @param timeoutSeconds 超时时间（秒）
     * @param args 命令参数
     * @return 命令输出（含标准输出、错误输出与退出码）
     */
    private String execute(long timeoutSeconds, String... args) {
        if (!ensureInstalled()) {
            return "浏览器工具未就绪: agent-browser 不可用且自动安装失败。\n"
                    + failureDetail()
                    + "\n处理方式: 手动执行 'npm install -g agent-browser' 后再执行 'agent-browser install'；"
                    + "或设置环境变量 AGENT_BROWSER_BIN 指向 agent-browser 的完整路径。";
        }

        List<String> command = new ArrayList<>(cliCommand());
        command.addAll(List.of(args));

        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            augmentPath(pb.environment());
            process = pb.start();
            final Process running = process;

            Future<String> stderrFuture = DRAIN_EXECUTOR.submit(() -> drain(running.getErrorStream()));
            String stdout = drain(running.getInputStream());
            boolean finished = running.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            String stderr = "";
            try {
                stderr = stderrFuture.get(5, TimeUnit.SECONDS);
            } catch (Exception ignored) {
                // 进程异常结束时错误流可能已无法读取，忽略即可
            }

            if (!finished) {
                running.destroyForcibly();
                return "浏览器操作超时（超过 " + timeoutSeconds + " 秒）: " + String.join(" ", command)
                        + "\n建议: 若是等待页面加载导致超时，请改用 waitForPageLoad 的 load 或 domcontentloaded 模式；"
                        + "若页面已渲染完成，可直接执行 snapshot 或 screenshot。";
            }

            StringBuilder result = new StringBuilder();
            if (!stdout.isEmpty()) {
                result.append(stdout);
            }
            if (!stderr.isEmpty()) {
                if (!result.isEmpty()) {
                    result.append("\n");
                }
                result.append("ERROR: ").append(stderr);
            }

            int exitCode = running.exitValue();
            if (exitCode != 0) {
                result.append("\n命令: ").append(String.join(" ", command))
                        .append("\n退出码: ").append(exitCode);
                return "浏览器命令执行失败（退出码 " + exitCode + "）: \n" + result;
            }
            return !result.isEmpty() ? result.toString() : "操作成功: " + String.join(" ", command);
        } catch (IOException e) {
            throw new AgentShuttingDownException("执行浏览器命令失败: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AgentShuttingDownException("执行浏览器命令被中断: " + e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 确保 agent-browser 已安装，首次调用时自动安装（幂等）
     *
     * @return 命令是否可用
     */
    private boolean ensureInstalled() {
        if (installChecked) {
            return true;
        }
        Probe probe = probeCli();
        if (probe.available) {
            installChecked = true;
            return true;
        }

        // 探测失败：先尝试自动安装，再复检一次
        String installLog = installCli();
        Probe retry = probeCli();
        if (retry.available) {
            installChecked = true;
            return true;
        }

        lastFailure = "命令: " + String.join(" ", cliCommand())
                + "\n探测结果: " + probe.detail
                + "\n自动安装: " + installLog
                + "\n复检结果: " + retry.detail;
        return false;
    }

    /** 环境探测结果 */
    private record Probe(boolean available, String detail) {
    }

    /**
     * 真正执行一次 {@code agent-browser --version} 判断可用性
     *
     * @return 探测结果，包含可用性与诊断详情
     */
    private Probe probeCli() {
        List<String> command = new ArrayList<>(cliCommand());
        command.add("--version");
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            augmentPath(pb.environment());
            process = pb.start();
            final Process running = process;
            Future<String> stderrFuture = DRAIN_EXECUTOR.submit(() -> drain(running.getErrorStream()));
            String stdout = drain(running.getInputStream());
            boolean finished = running.waitFor(30, TimeUnit.SECONDS);

            String stderr = "";
            try {
                stderr = stderrFuture.get(5, TimeUnit.SECONDS);
            } catch (Exception ignored) {
                // 进程异常结束时错误流可能已无法读取，忽略即可
            }

            if (!finished) {
                running.destroyForcibly();
                return new Probe(false, "执行超时（30 秒）");
            }
            int exitCode = running.exitValue();
            if (exitCode == 0) {
                return new Probe(true, "version=" + stdout);
            }
            return new Probe(false, "退出码 " + exitCode + "，输出: " + firstNonBlank(stdout, stderr));
        } catch (IOException e) {
            // Windows 上 ProcessBuilder 无法执行 npm 生成的 .cmd 垫片，典型报错为 CreateProcess error=2
            return new Probe(false, "无法启动命令，原因: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Probe(false, "探测被中断");
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 自动安装 agent-browser CLI 与 Chromium 运行时
     */
    private String installCli() {
        String node = findNode();
        if (node == null) {
            return "未找到 node，跳过自动安装（请安装 Node.js 18+，或设置环境变量 NODE_BIN 指向 node.exe）";
        }

        // 优先用 node 直接执行 npm-cli.js，避免依赖 npm.cmd（ProcessBuilder 无法执行 .cmd）
        Path npmCli = findNpmCli(Paths.get(node));
        List<String> installPackage;
        if (npmCli != null) {
            installPackage = List.of(node, npmCli.toString(), "install", "-g", CLI);
        } else if (isWindows()) {
            installPackage = List.of("cmd", "/c", "npm install -g " + CLI);
        } else {
            installPackage = List.of("bash", "-lc", "npm install -g " + CLI);
        }
        String packageResult = runInstall(installPackage);

        // 下载 Chromium 运行时，此时 CLI 可能刚安装成功，需重新解析
        resolvedCli = null;
        List<String> command = new ArrayList<>(cliCommand());
        command.add("install");
        String runtimeResult = runInstall(command);

        return "CLI 安装[" + packageResult + "]，运行时安装[" + runtimeResult + "]";
    }

    /** 执行安装命令并返回结果摘要 */
    private String runInstall(List<String> command) {
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            augmentPath(pb.environment());
            process = pb.start();
            final Process running = process;
            Future<String> stderrFuture = DRAIN_EXECUTOR.submit(() -> drain(running.getErrorStream()));
            // 必须排空输出流，否则缓冲区打满会导致安装进程卡死
            String stdout = drain(running.getInputStream());
            boolean finished = running.waitFor(INSTALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            String stderr = "";
            try {
                stderr = stderrFuture.get(5, TimeUnit.SECONDS);
            } catch (Exception ignored) {
                // 忽略，安装失败时错误流可能已关闭
            }

            if (!finished) {
                running.destroyForcibly();
                return "超时（" + INSTALL_TIMEOUT_SECONDS + " 秒）";
            }
            String output = firstNonBlank(stdout, stderr);
            return "退出码 " + running.exitValue() + (output.isEmpty() ? "" : "，输出: " + output);
        } catch (IOException e) {
            return "启动失败: " + e.getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "失败: 被中断";
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    // ==================== CLI 命令解析（Windows .cmd 垫片规避） ====================

    /**
     * 获取 CLI 启动命令。
     *
     * <p>Windows 上 {@code npm install -g} 生成的 {@code agent-browser} 是 .cmd 批处理垫片，
     * 而 ProcessBuilder 底层使用 CreateProcess，无法直接执行 .cmd/.bat，必须经由 {@code cmd /c}；
     * 但 {@code cmd /c} 又会引入 URL 中 &amp; ^ % 等特殊字符的转义问题。
     *
     * <p>因此这里直接定位 npm 全局包的 JS 入口，用 {@code node <入口文件>} 启动，
     * 既绕开 .cmd 限制，又保持参数以数组传递、无需任何 shell 转义。
     *
     * @return 启动命令数组
     */
    private List<String> cliCommand() {
        List<String> cached = resolvedCli;
        if (cached != null) {
            return cached;
        }
        cached = resolveCliCommand();
        resolvedCli = cached;
        return cached;
    }

    private List<String> resolveCliCommand() {
        String explicit = System.getenv(ENV_AGENT_BROWSER_BIN);
        if (!isBlank(explicit)) {
            return List.of(explicit.trim());
        }
        String node = findNode();
        if (node != null) {
            for (Path root : globalNodeModuleRoots(node)) {
                Path script = findPackageBin(root);
                if (script != null) {
                    return List.of(node, script.toString());
                }
            }
        }
        return List.of(CLI);
    }

    /** 定位 node 可执行文件 */
    private String findNode() {
        String explicit = System.getenv(ENV_NODE_BIN);
        if (!isBlank(explicit) && Files.isRegularFile(Paths.get(explicit.trim()))) {
            return explicit.trim();
        }
        String fromPath = findInPath(isWindows() ? "node.exe" : "node");
        if (fromPath != null) {
            return fromPath;
        }
        for (Path candidate : candidateNodePaths()) {
            if (Files.isRegularFile(candidate)) {
                return candidate.toString();
            }
        }
        return null;
    }

    /** 在 PATH 中查找可执行文件 */
    private String findInPath(String executableName) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) {
            return null;
        }
        for (String dir : pathEnv.split(File.pathSeparator)) {
            if (dir.isBlank()) {
                continue;
            }
            Path candidate = Paths.get(dir.trim().replace("\"", "")).resolve(executableName);
            if (Files.isRegularFile(candidate)) {
                return candidate.toString();
            }
        }
        return null;
    }

    /** node 常见安装路径，用于 PATH 缺失时的兜底查找 */
    private List<Path> candidateNodePaths() {
        List<Path> candidates = new ArrayList<>();
        String userHome = System.getProperty("user.home");
        if (isWindows()) {
            candidates.add(Paths.get("C:\\Program Files\\nodejs\\node.exe"));
            candidates.add(Paths.get("C:\\Program Files (x86)\\nodejs\\node.exe"));
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                Path nvmDir = Paths.get(appData, "nvm");
                if (Files.isDirectory(nvmDir)) {
                    try (var stream = Files.list(nvmDir)) {
                        stream.filter(Files::isDirectory)
                                .map(dir -> dir.resolve("node.exe"))
                                .filter(Files::isRegularFile)
                                .sorted(Comparator.comparing(Path::toString).reversed())
                                .limit(3)
                                .forEach(candidates::add);
                    } catch (IOException ignored) {
                        // 目录不可读时忽略
                    }
                }
            }
            if (userHome != null) {
                candidates.add(Paths.get(userHome, "AppData", "Roaming", "nvm", "current", "node.exe"));
                candidates.add(Paths.get(userHome, ".nvm", "current", "node.exe"));
            }
        } else {
            candidates.add(Paths.get("/usr/local/bin/node"));
            candidates.add(Paths.get("/usr/bin/node"));
            candidates.add(Paths.get("/opt/homebrew/bin/node"));
            candidates.add(Paths.get("/opt/local/bin/node"));
            if (userHome != null) {
                candidates.add(Paths.get(userHome, ".nvm", "versions", "node", "current", "bin", "node"));
            }
        }
        return candidates;
    }

    /** npm 全局包安装目录候选列表 */
    private List<Path> globalNodeModuleRoots(String node) {
        Set<Path> roots = new LinkedHashSet<>();
        Path npmCli = findNpmCli(Paths.get(node));
        if (npmCli != null) {
            String root = execQuiet(List.of(node, npmCli.toString(), "root", "-g"));
            if (!isBlank(root)) {
                roots.add(Paths.get(root.trim().replace("\"", "")));
            }
        }
        String userHome = System.getProperty("user.home");
        if (isWindows()) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                roots.add(Paths.get(appData, "npm", "node_modules"));
            }
            if (userHome != null) {
                roots.add(Paths.get(userHome, "AppData", "Roaming", "npm", "node_modules"));
            }
        } else {
            if (userHome != null) {
                roots.add(Paths.get(userHome, ".npm-global", "lib", "node_modules"));
            }
            roots.add(Paths.get("/usr/local/lib/node_modules"));
            roots.add(Paths.get("/usr/lib/node_modules"));
            roots.add(Paths.get("/opt/homebrew/lib/node_modules"));
        }
        return new ArrayList<>(roots);
    }

    /** 找到与 node 同目录的 npm 入口脚本 */
    private Path findNpmCli(Path nodePath) {
        Path parent = nodePath.getParent();
        if (parent == null) {
            return null;
        }
        Path npmCli = parent.resolve("node_modules").resolve("npm").resolve("bin").resolve("npm-cli.js");
        return Files.isRegularFile(npmCli) ? npmCli : null;
    }

    /** 读取 agent-browser 包 package.json 的 bin 字段，解析出 CLI 的 JS 入口 */
    private Path findPackageBin(Path nodeModulesRoot) {
        Path packageDir = nodeModulesRoot.resolve(CLI);
        Path packageJson = packageDir.resolve("package.json");
        if (!Files.isRegularFile(packageJson)) {
            return null;
        }
        try {
            JsonNode root = MAPPER.readTree(Files.readString(packageJson));
            JsonNode bin = root.get("bin");
            String entry = null;
            if (bin != null && bin.isTextual()) {
                entry = bin.asText();
            } else if (bin != null && bin.isObject() && !bin.isEmpty()) {
                entry = bin.elements().next().asText();
            }
            if (entry == null || entry.isBlank()) {
                return null;
            }
            Path script = packageDir.resolve(entry).normalize();
            return Files.isRegularFile(script) ? script : null;
        } catch (IOException e) {
            return null;
        }
    }

    /** 静默执行一条命令，返回标准输出，失败返回 null */
    private String execQuiet(List<String> command) {
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            augmentPath(pb.environment());
            process = pb.start();
            final Process running = process;
            Future<String> stderrFuture = DRAIN_EXECUTOR.submit(() -> drain(running.getErrorStream()));
            String stdout = drain(running.getInputStream());
            boolean finished = running.waitFor(30, TimeUnit.SECONDS);
            try {
                stderrFuture.get(2, TimeUnit.SECONDS);
            } catch (Exception ignored) {
                // 忽略
            }
            if (!finished) {
                running.destroyForcibly();
                return null;
            }
            return running.exitValue() == 0 ? stdout : null;
        } catch (Exception e) {
            return null;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 为子进程 PATH 补充 node 与 npm 全局目录。
     *
     * <p>IDE 启动的 Java 进程继承的是 IDE 启动时刻的 PATH，安装 Node.js 后若未重启 IDE，
     * PATH 中可能缺少 node 目录，导致 CLI 自身依赖的系统命令找不到。
     *
     * @param env 子进程环境变量
     */
    private void augmentPath(Map<String, String> env) {
        Set<String> extra = new LinkedHashSet<>();
        String node = findNode();
        if (node != null) {
            Path parent = Paths.get(node).getParent();
            if (parent != null) {
                extra.add(parent.toString());
            }
        }
        if (isWindows()) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                Path npmDir = Paths.get(appData, "npm");
                if (Files.isDirectory(npmDir)) {
                    extra.add(npmDir.toString());
                }
            }
        }
        if (extra.isEmpty()) {
            return;
        }
        // Windows 环境变量名大小写不敏感，需找到实际使用的 key
        String pathKey = "PATH";
        for (String key : env.keySet()) {
            if ("PATH".equalsIgnoreCase(key)) {
                pathKey = key;
                break;
            }
        }
        String current = env.get(pathKey);
        StringBuilder prepend = new StringBuilder();
        for (String dir : extra) {
            boolean exists = current != null
                    && current.toLowerCase(Locale.ROOT).contains(dir.toLowerCase(Locale.ROOT));
            if (!exists) {
                prepend.append(dir).append(File.pathSeparator);
            }
        }
        if (!prepend.isEmpty()) {
            env.put(pathKey, prepend + (current == null ? "" : current));
        }
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    }

    private String failureDetail() {
        String detail = lastFailure;
        return isBlank(detail) ? "（无诊断信息）" : detail;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    /**
     * 排空输入流并转为字符串
     *
     * @param inputStream 输入流
     * @return 流内容
     */
    private String drain(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            sb.append("读取命令输出失败: ").append(e.getMessage());
        }
        return sb.toString().trim();
    }

    /**
     * 判断字符串是否为空
     *
     * @param value 待判断字符串
     * @return 是否为空
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
