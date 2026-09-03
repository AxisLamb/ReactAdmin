package com.lain.ai.flow;

import com.lain.ai.tools.BrowserAssistant;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;

import java.nio.file.Paths;

/**
 * 浏览器操作智能体演示
 *
 * <p>将 BrowserAssistant 注册到 Toolkit 后交给 HarnessAgent，
 * 智能体即可自主完成"打开网页 → 读取内容 → 截图 → 关闭浏览器"的完整流程。
 */
public class BrowserOperationAgent {

    public static void main(String[] args) {
        // 测试环境变量是否读取到
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        System.out.println("API Key: " + (apiKey != null ? "已设置" : "未设置"));
        if (apiKey == null) {
            System.out.println("请检查环境变量配置");
            return;
        }

        // 创建浏览器工具实例
        BrowserAssistant browserAssistant = new BrowserAssistant();

        // 创建工具包并注册浏览器工具
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(browserAssistant);

        // 生命周期日志中间件：输出"推理 / 模型调用 / 工具调用"各阶段耗时，
        // 慢步骤自动升级为 WARN —— 这是定位"Agent 卡在哪"的主要手段。
        // 第三个参数开启后会在 DEBUG 级别逐个打印 AgentEvent（事件流全量追踪）。
        AgentLifecycleLogger lifecycleLogger = new AgentLifecycleLogger(5_000L, 300, false);

        HarnessAgent agent = HarnessAgent.builder()
                .name("browser-operator")
                .sysPrompt("""
                        你是一个帮助用户操作浏览器的助手，可以使用提供的工具完成网页浏览任务。
                        操作页面时请遵循以下步骤：
                        1. 先用 openPage 打开目标网页；
                        2. 需要点击或填写表单前，先用 getPageSnapshot(interactiveOnly=true) 获取可交互元素；
                        3. 再使用 clickElement、typeText、pressKey 等工具完成交互；
                        4. 需要查看页面外观时使用 takeScreenshot 截图；
                        5. 所有任务结束后，必须调用 closeBrowser 关闭浏览器释放资源。
                        """)
                // 字符串形式由 ModelRegistry 解析 —— 自动读取 DASHSCOPE_API_KEY；
                // 切换其他厂商时改用 "openai:gpt-5.5"、"anthropic:claude-sonnet-4-5"、
                // "gemini:gemini-2.0-flash" 或 "ollama:llama3"。
                .model("dashscope:qwen3.7-plus")
                .workspace(Paths.get(".agentscope/workspace"))
                .toolkit(toolkit) // 注册工具包
                .middleware(lifecycleLogger) // 生命周期埋点（2.0 主推，Hook 已废弃）
                .compaction(CompactionConfig.builder()
                        .triggerMessages(30)
                        .keepMessages(10)
                        .build())
                .build();

        RuntimeContext ctx = RuntimeContext.builder()
                .sessionId("browser-op-session")
                .userId("CC")
                .build();

        System.out.println("已注册工具: " + AgentLifecycleLogger.describeToolkit(toolkit));

        // 第一轮：打开网页并读取内容（首次调用会自动安装 agent-browser）
        agent.call(Msg.builder()
                .textContent("请打开 https://www.goofish.com/ 这个网页，并告诉我页面的标题和主要内容是什么。")
                .build(), ctx).block();

        // 第二轮：截图并提取交互元素
        agent.call(Msg.builder()
                .textContent("请截取当前页面的截图，并列出页面上所有可点击的交互元素。")
                .build(), ctx).block();

        // 第三轮：关闭浏览器释放资源
        agent.call(Msg.builder()
                .textContent("任务已完成，请关闭浏览器。")
                .build(), ctx).block();
    }
}
