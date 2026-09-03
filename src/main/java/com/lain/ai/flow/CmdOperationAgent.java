package com.lain.ai.flow;

import com.lain.ai.tools.CmdAssistant;
import com.lain.ai.tools.CmdAssistant.CommandApprover;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * Windows cmd 命令智能体演示（CLI 多轮对话）。
 *
 * <p>让 Agent 自主执行 cmd 命令、观察输出、自行决定下一步命令；
 * 敏感操作（删除 / 上传下载 / 改系统配置 / 关机等）会暂停并在控制台询问用户批准。
 *
 * <p>启动前确保已设置环境变量 {@code DASHSCOPE_API_KEY}。
 */
public class CmdOperationAgent {

    public static void main(String[] args) throws IOException {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        System.out.println("API Key: " + (apiKey != null ? "已设置" : "未设置"));
        if (apiKey == null) {
            System.out.println("请先设置环境变量 DASHSCOPE_API_KEY");
            return;
        }

        // 敏感命令由控制台人工批准（y/N）；换程序化实现（如企业微信审批）只需替换此实现
        CmdAssistant cmdAssistant = new CmdAssistant(new ConsoleApprover());

        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(cmdAssistant);

        AgentLifecycleLogger lifecycleLogger = new AgentLifecycleLogger(5_000L, 300, false);

        HarnessAgent agent = HarnessAgent.builder()
                .name("cmd-operator")
                .sysPrompt("""
                        你是运行在用户 Windows 电脑上的命令行助手，通过 runCmd 工具执行 cmd 命令帮助用户完成任务。
                        工作准则：
                        1. 先探查、后行动：不确定现状时先用只读命令（dir、type、findstr、where、ipconfig、systeminfo 等）查看，
                           根据上一步的输出再决定下一步执行什么命令，需要多步时逐条调用 runCmd。
                        2. 每条 runCmd 都是独立进程，工作目录互不延续。需要固定目录时优先传 workingDirectory 参数，
                           或在命令开头用 "cd /d <绝对路径> && " 再拼接要执行的命令。
                        3. 危险操作不要自己尝试绕过：runCmd 已内置敏感操作检测，删除/上传下载/改系统配置/关机等
                           会自动暂停询问用户，用户批准后工具会自动执行，你不需要也无法绕开它。
                        4. 命令输出过长会被截断，可改用 findstr /n /c:关键词 或 more 分段查看，避免输出刷爆上下文。
                        5. 任务完成后，向用户总结你执行了哪些命令、结果如何。
                        """)
                .model("dashscope:qwen3.7-plus")
                .workspace(Paths.get(".agentscope/workspace"))
                .toolkit(toolkit)
                .middleware(lifecycleLogger)
                .compaction(CompactionConfig.builder()
                        .triggerMessages(30)
                        .keepMessages(10)
                        .build())
                .build();

        RuntimeContext ctx = RuntimeContext.builder()
                .sessionId("cmd-op-session")
                .userId("BB")
                .build();

        System.out.println("已注册工具: " + AgentLifecycleLogger.describeToolkit(toolkit));
        System.out.println("cmd-operator 已就绪。输入你的需求开始（输入 exit 或 quit 退出）");
        System.out.println(">>> 示例：列出当前目录有哪些文件\n");

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));
        while (true) {
            System.out.print("你> ");
            System.out.flush();
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            String input = line.trim();
            if (input.isEmpty()) {
                continue;
            }
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                System.out.println("再见！");
                break;
            }
            agent.call(Msg.builder().textContent(input).build(), ctx).block();
        }
    }

    /** 控制台人工批准实现：把待批准的敏感命令打印出来，读取用户 y/N */
    static final class ConsoleApprover implements CommandApprover {
        @Override
        public boolean approve(String command, String reason) {
            System.out.println("\n══════════ 敏感操作需要批准 ══════════");
            System.out.println("原因: " + reason);
            System.out.println("命令: " + command);
            System.out.println("──────────────────────────────────────");
            System.out.print("是否允许执行？(y/N): ");
            System.out.flush();
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(System.in, StandardCharsets.UTF_8));
                String answer = reader.readLine();
                return answer != null && (answer.trim().equalsIgnoreCase("y")
                        || answer.trim().equalsIgnoreCase("yes"));
            } catch (IOException e) {
                return false;
            }
        }
    }
}
