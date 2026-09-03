package com.lain.ai.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Windows cmd 命令执行工具：Agent 自主运行 cmd 命令并拿到完整输出
 * （标准输出 / 标准错误 / 退出码），据此判断下一步命令。
 *
 * <p>敏感操作保护：删除 / 上传下载 / 改系统配置 / 关机等命令先经
 * {@link CommandApprover} 询问人工，批准后才执行；未接入批准通道默认拒绝。
 * 极高危操作（格式化磁盘、删系统目录、diskpart 等）无论是否批准都拒绝。
 *
 * <p>编码：cmd 经管道输出的编码与系统控制台代码页（chcp）未必一致，可能与 JVM
 * UTF-8 不同。解码不盲信单一探测：先严格 UTF-8（全字节合法即采用），失败再按
 * {@code cmd /c chcp} 探测的代码页解码，探测为 UTF-8 时以 GBK 兜底。
 */
public class CmdAssistant {

    private static final Logger log = LoggerFactory.getLogger(CmdAssistant.class);

    private static final int DEFAULT_TIMEOUT_SECONDS = 120;
    private static final int MAX_STDOUT_CHARS = 6_000;
    private static final int MAX_STDERR_CHARS = 2_000;
    private static final String UNAPPROVED_SUFFIX = "如需执行，请联系管理员配置人工批准通道。";

    private static final ExecutorService IO = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "cmd-io");
        t.setDaemon(true);
        return t;
    });

    /** chcp 输出中的代码页数字（如 936），中文/英文系统输出均含该数字 */
    private static final Pattern CODE_PAGE = Pattern.compile("\\b\\d{3,4}\\b");

    /** 人工批准回调：敏感命令执行前询问用户。传 null 表示未接入批准通道，敏感命令一律拒绝 */
    @FunctionalInterface
    public interface CommandApprover {
        /** @return true 批准执行 */
        boolean approve(String command, String reason);
    }

    private final CommandApprover approver;

    /** 单条判定规则 */
    private static final class Rule {
        final Pattern pattern;
        final boolean hard; // true=高危，批准也不放行
        final String reason;

        Rule(String regex, boolean hard, String reason) {
            this.pattern = Pattern.compile(regex);
            this.hard = hard;
            this.reason = reason;
        }
    }

    private static final List<Rule> RULES = List.of(
            // ===== 高危：任何情况都拒绝 =====
            new Rule("(?i)(^|[\\n&|;])\\s*format\\s+[a-z]:\\s*($|[\\n&|;])", true, "格式化磁盘会清空整个分区"),
            new Rule("(?i)(^|[\\n&|;])\\s*(rd|rmdir)\\s+(/s\\s+)?(/q\\s+)?[a-z]:\\\\\\s*($|[\\n&|;])", true, "递归删除盘符根目录"),
            new Rule("(?i)(^|[\\n&|;])\\s*(rd|rmdir)\\s+[^\\n&|;]*:\\\\("
                    + "windows|system32|program\\s+?files)(\\\\|[\\s\\n&|;])", true, "递归删除系统关键目录"),
            new Rule("(?i)(^|[\\n&|;])\\s*diskpart(\\s|$)", true, "diskpart 直接操作磁盘分区"),
            new Rule("(?i)\\bcipher\\s+/w\\b", true, "cipher /w 反复覆写磁盘空闲空间"),
            new Rule("(?i)(^|[\\n&|;])\\s*del\\s+[^\\n&|;]*:\\\\windows(\\\\|[\\s\\n&|;])", true, "删除系统目录文件"),
            // ===== 敏感：询问人工 =====
            new Rule("(?i)(^|[\\n&|;])\\s*(del|erase|rmdir|rd)\\b", false, "删除文件或目录"),
            new Rule("(?i)(^|[\\n&|;])\\s*taskkill\\s+/f\\b", false, "强制终止进程"),
            new Rule("(?i)(^|[\\n&|;])\\s*shutdown(\\s|-)", false, "关机 / 重启 / 注销"),
            new Rule("(?i)\\breg\\s+(add|delete|copy|restore|import)\\b", false, "修改 Windows 注册表"),
            new Rule("(?i)\\bsc\\s+(create|delete|stop|start|config)\\b", false, "增删改 Windows 服务"),
            new Rule("(?i)\\bnet\\s+(user|localgroup|share)\\b", false, "修改用户 / 共享配置"),
            new Rule("(?i)\\bbcdedit\\b", false, "修改系统启动配置"),
            new Rule("(?i)(^|[\\n&|;])\\s*setx\\b", false, "永久修改环境变量"),
            new Rule("(?i)(^|[\\n&|;])\\s*curl\\b", false, "curl 发起网络请求（可能上传或下载数据）"),
            new Rule("(?i)(^|[\\n&|;])\\s*wget\\b", false, "wget 下载网络内容"),
            new Rule("(?i)\\b(pscp|scp|sftp|ftp|wput|rsync)\\b", false, "通过 ftp/scp 等协议传输文件"),
            new Rule("(?i)\\bcertutil\\s+-urlcache\\b", false, "certutil 下载网络内容"),
            new Rule("(?i)\\bbitsadmin\\b", false, "bitsadmin 后台传输"),
            new Rule("(?i)\\bpowershell\\b[^\\n&|;]*\\b(invoke-(webrequest|restmethod)|"
                    + "start-bits|webclient|bitsadmin|(down|up)load)\\b", false, "PowerShell 发起网络请求"),
            new Rule("(?i)\\bpowershell\\b[^\\n&|;]*\\b(remove-item|rm\\b|delete-)\\b", false, "PowerShell 删除文件或对象"),
            new Rule("(?i)\\bgit\\s+push\\b", false, "git push 上传代码到远程仓库"));

    public CmdAssistant() {
        this(null);
    }

    /** @param approver 敏感命令人工批准回调；null=未接入批准通道，敏感命令一律拒绝 */
    public CmdAssistant(CommandApprover approver) {
        this.approver = approver;
    }

    /**
     * 在 Windows 上执行一条 cmd 命令并返回完整输出。
     */
    @Tool(description = "在 Windows 上执行 cmd 命令，返回标准输出、标准错误与退出码，供你观察结果并决定下一步。" +
            "命令若包含删除、格式化、上传下载、改系统配置等敏感操作，会自动暂停询问用户批准，未经批准不会执行。" +
            "需要执行多条命令时请分步调用：先用 dir、type、findstr 等只读命令探查现状，再执行修改类命令。" +
            "每条命令是独立进程、工作目录不延续：需要固定目录请用 workingDirectory 参数，或在命令里先 cd /d <路径> && 再执行")
    public String runCmd(
            @ToolParam(name = "command", description = "要执行的 cmd 命令，可含管道(|)、连接符(&、&&)、重定向(>)") String command,
            @ToolParam(name = "workingDirectory", description = "命令工作目录（绝对路径），默认当前工程目录", required = false) String workingDirectory,
            @ToolParam(name = "timeoutSeconds", description = "执行超时秒数，默认 120，超过将强制终止", required = false) Integer timeoutSeconds,
            @ToolParam(name = "stdin", description = "可选：写入命令标准输入的内容（供需要交互输入的程序使用）", required = false) String stdin) {
        if (command == null || command.isBlank()) {
            return "命令为空，未执行任何操作。";
        }
        String trimmed = command.trim();
        Rule hit = null;
        for (String seg : trimmed.split("(?i)\\s*(&&|\\|\\||&|\\||;)\\s*|\\r?\\n")) {
            String s = seg.trim();
            if (s.isEmpty()) {
                continue;
            }
            for (Rule rule : RULES) {
                if (rule.pattern.matcher(s).find()) {
                    hit = rule;
                    break;
                }
            }
            if (hit != null) {
                break;
            }
        }

        if (hit != null && hit.hard) {
            log.warn("[cmd] ✖ 高危命令被拒绝（批准也不放行）| 原因: {} | 命令: {}", hit.reason, preview(trimmed));
            return "命令未执行：该操作风险过高（" + hit.reason + "），无论是否人工批准都不会执行。"
                    + "请改用其它方式达成目标（可先用 dir / type 只读命令确认现状）。";
        }
        if (hit != null) {
            if (approver == null) {
                log.warn("[cmd] ✖ 敏感命令被拒绝（未接入人工批准通道）| 原因: {} | 命令: {}", hit.reason, preview(trimmed));
                return "命令未执行：检测到敏感操作（" + hit.reason + "），但当前未接入人工批准通道，已按安全默认拒绝。"
                        + UNAPPROVED_SUFFIX + "建议先用只读命令（dir、type、findstr）探查，再决定修改动作。";
            }
            log.info("[cmd] ⏸ 敏感命令待人工批准 | 原因: {} | 命令: {}", hit.reason, preview(trimmed));
            boolean approved;
            try {
                approved = approver.approve(trimmed, hit.reason);
            } catch (Exception e) {
                log.warn("[cmd] 人工批准过程异常，按拒绝处理: {}", e.toString());
                approved = false;
            }
            if (!approved) {
                log.warn("[cmd] ✖ 敏感命令被用户拒绝 | 原因: {} | 命令: {}", hit.reason, preview(trimmed));
                return "命令未执行：用户未批准该操作（" + hit.reason + "）。"
                        + "建议：向用户说明必要性与影响后再请求批准重试，或改用无需批准的只读命令。";
            }
            log.warn("[cmd] ✔ 敏感命令已获用户批准 | 命令: {}", preview(trimmed));
        }

        long timeout = timeoutSeconds == null || timeoutSeconds <= 0 ? DEFAULT_TIMEOUT_SECONDS : timeoutSeconds;
        return execute(trimmed, workingDirectory, timeout, stdin, hit != null);
    }

    // ==================== 执行 ====================

    private String execute(String command, String workingDirectory, long timeoutSeconds,
                           String stdin, boolean approved) {
        long startNanos = System.nanoTime();
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/d", "/s", "/c", command);
            if (workingDirectory != null && !workingDirectory.isBlank()) {
                pb.directory(new File(workingDirectory));
            }
            process = pb.start();
            final Process running = process;

            // 输出/输入流全部在后台线程排空：必须在 waitFor 之前启动，
            // 否则进程输出灌满管道会卡死，且超时永远等不到（drain 会先阻塞到进程退出）。
            Charset charset = shellCharset();
            Future<String> stdoutFuture = IO.submit(() -> drain(running.getInputStream(), charset));
            Future<String> stderrFuture = IO.submit(() -> drain(running.getErrorStream(), charset));
            if (stdin != null && !stdin.isEmpty()) {
                final String input = stdin;
                IO.submit(() -> {
                    try (OutputStream os = running.getOutputStream()) {
                        os.write(input.getBytes(shellCharset()));
                    } catch (IOException ignored) {
                        // 命令可能无需输入或已退出
                    }
                    return null;
                });
            }

            boolean finished = running.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                running.destroyForcibly();
                // 强制终止后仍尽量取回已产生的输出（最多再等 3 秒）
                String partial = safeGet(stdoutFuture, 3, TimeUnit.SECONDS);
                String msg = "命令执行超时（超过 " + timeoutSeconds + " 秒，已强制终止）\n命令: " + command
                        + (partial.isEmpty() ? "" : "\n超时前输出（截断）:\n" + preview(partial, MAX_STDOUT_CHARS));
                log.warn("[cmd] ✖ 超时终止 | 命令: {} | 耗时 {}s", preview(command), elapsedSeconds(startNanos));
                return msg;
            }

            String stdout = safeGet(stdoutFuture, 5, TimeUnit.SECONDS);
            String stderr = safeGet(stderrFuture, 5, TimeUnit.SECONDS);
            StringBuilder sb = new StringBuilder();
            if (approved) {
                sb.append("该命令已获用户批准后执行。\n");
            }
            sb.append("退出码: ").append(running.exitValue()).append('\n');
            appendSection(sb, "标准输出", stdout, MAX_STDOUT_CHARS);
            appendSection(sb, "标准错误", stderr, MAX_STDERR_CHARS);
            log.info("[cmd] ◀ 完成 | 退出码: {} | 耗时 {}s | 命令: {}", running.exitValue(),
                    elapsedSeconds(startNanos), preview(command));
            return sb.toString();
        } catch (IOException e) {
            log.warn("[cmd] ✖ 启动失败 | 原因: {} | 命令: {}", e.getMessage(), preview(command));
            return "命令启动失败: " + e.getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "命令执行被中断: " + e.getMessage();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static String safeGet(Future<String> future, long timeout, TimeUnit unit) {
        try {
            return future.get(timeout, unit);
        } catch (Exception e) {
            return "";
        }
    }

    // ==================== 子进程字符集探测 ====================

    private static volatile Charset shellCharset;

    /**
     * 探测一次系统代码页，供输出解码回退与 stdin 写入参考。
     * 注意：chcp 反映的是控制台代码页，与 cmd 管道实际输出编码未必一致，
     * 因此 {@link #drain(InputStream, Charset)} 不会盲信该结果（UTF-8 严格解码优先）。
     */
    private static Charset shellCharset() {
        Charset cached = shellCharset;
        if (cached != null) {
            return cached;
        }
        synchronized (CmdAssistant.class) {
            if (shellCharset != null) {
                return shellCharset;
            }
            Charset detected = StandardCharsets.UTF_8;
            try {
                Process p = new ProcessBuilder("cmd.exe", "/d", "/c", "chcp").start();
                // 直接读字节（chcp 秒退），不能走 drain()——那会再次进入本方法造成无限递归
                String out;
                try (InputStream is = p.getInputStream()) {
                    out = new String(is.readAllBytes(), StandardCharsets.ISO_8859_1);
                }
                boolean done = p.waitFor(5, TimeUnit.SECONDS);
                if (done && p.exitValue() == 0) {
                    Matcher m = CODE_PAGE.matcher(out);
                    if (m.find()) {
                        detected = Charset.forName("cp" + m.group(1));
                    }
                }
            } catch (Exception ignored) {
                // 探测失败退回默认
            }
            shellCharset = detected;
            return detected;
        }
    }

    private static void appendSection(StringBuilder sb, String title, String content, int maxChars) {
        sb.append("── ").append(title).append('\n');
        if (content == null || content.isEmpty()) {
            sb.append("(空)\n");
        } else if (content.length() <= maxChars) {
            sb.append(content);
            if (!content.endsWith("\n")) {
                sb.append('\n');
            }
        } else {
            sb.append(content, 0, maxChars).append("\n…(输出过长已截断，共 ")
                    .append(content.length()).append(" 字符；可配合 findstr /n /c:关键词 或 more 分段查看)\n");
        }
    }

    /**
     * 排空输入流并解码。cmd 经管道输出的编码在不同环境不一致：chcp 探测到的是
     * 控制台代码页，与管道实际编码未必一致（实测中文 Windows 上可能探测到 65001
     * 但管道仍是 GBK，反之亦然），因此不能盲信单一探测结果。
     *
     * <p>解码策略：先做 UTF-8 <b>严格</b>解码——UTF-8 格式严格，全部字节合法即
     * 基本可断定真是 UTF-8（GBK 双字节文本几乎不可能恰好整体构成合法 UTF-8 序列，
     * 宽松解码的乱码也不产生替换字符、无法区分）；严格解码失败再按探测代码页解码；
     * 若探测值恰好是 UTF-8（管道实际非 UTF-8），再用中文 Windows 的 OEM 代码页
     * GBK 兜底，比较替换字符数取更优者。
     */
    private static String drain(InputStream inputStream, Charset charset) {
        try (InputStream in = inputStream) {
            byte[] raw = in.readAllBytes();
            String utf8 = tryStrictDecode(raw);
            if (utf8 != null) {
                return utf8;
            }
            if (charset.equals(StandardCharsets.UTF_8)) {
                // 探测到 UTF-8 但字节非法：管道实际多为系统 OEM 代码页（中文 Windows=GBK）
                String gbk = new String(raw, Charset.forName("GBK"));
                String utf8Loose = new String(raw, StandardCharsets.UTF_8);
                return countReplacement(gbk) <= countReplacement(utf8Loose) ? gbk : utf8Loose;
            }
            return new String(raw, charset);
        } catch (IOException e) {
            // 子进程退出导致流关闭是正常现象
            return "";
        }
    }

    /** UTF-8 严格解码：遇任何非法字节序列即返回 null（宽松解码会静默产生乱码，无法用于判定） */
    private static String tryStrictDecode(byte[] raw) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(raw))
                    .toString();
        } catch (CharacterCodingException e) {
            return null;
        }
    }

    private static int countReplacement(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\uFFFD') {
                count++;
            }
        }
        return count;
    }

    private static String preview(String text) {
        return preview(text, 120);
    }

    private static String preview(String text, int max) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String flat = text.replaceAll("\\s+", " ");
        return flat.length() <= max ? flat : flat.substring(0, max) + "…";
    }

    private static long elapsedSeconds(long startNanos) {
        return TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startNanos);
    }
}
