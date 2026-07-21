package com.lain.ai.flow;

import com.lain.ai.tools.FileAssistant;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;

import java.nio.file.Paths;

public class FileOperationAgent {
    public static void main(String[] args) {
        // 测试环境变量是否读取到
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        System.out.println("API Key: " + (apiKey != null ? "已设置" : "未设置"));
        if (apiKey == null) {
            System.out.println("请检查环境变量配置");
            return;
        }

        // 创建文件操作工具实例
        FileAssistant fileAssistant = new FileAssistant();
        
        // 创建工具包并注册文件操作工具
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(fileAssistant);

        HarnessAgent agent = HarnessAgent.builder()
                .name("file-operator")
                .sysPrompt("你是一个帮助用户进行文件系统操作的助手，可以使用提供的工具来操作CentOS文件系统。")
                // 字符串形式由 ModelRegistry 解析 —— 自动读取 DASHSCOPE_API_KEY；
                // 切换其他厂商时改用 "openai:gpt-5.5"、"anthropic:claude-sonnet-4-5"、
                // "gemini:gemini-2.0-flash" 或 "ollama:llama3"。
                .model("dashscope:qwen3.7-plus")
                .workspace(Paths.get(".agentscope/workspace"))
                .toolkit(toolkit) // 注册工具包
                .compaction(CompactionConfig.builder()
                        .triggerMessages(30)
                        .keepMessages(10)
                        .build())
                .build();

        RuntimeContext ctx = RuntimeContext.builder()
                .sessionId("file-op-session")
                .userId("alice")
                .build();

        // 第一轮：演示文件系统操作
        agent.call(Msg.builder()
                .textContent("请帮我创建一个名为 /tmp/test-agent 的目录，然后在这个目录下创建一个名为 info.txt 的文件，内容是 'This is a test file created by AgentScope.'")
                .build(), ctx).block();

        // 第二轮：列出目录内容
        agent.call(Msg.builder()
                .textContent("请列出 /tmp/test-agent 目录的内容，并告诉我 info.txt 文件的详细信息。")
                .build(), ctx).block();
    }
}