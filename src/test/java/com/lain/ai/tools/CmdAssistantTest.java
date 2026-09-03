package com.lain.ai.tools;

import com.lain.ai.tools.CmdAssistant.CommandApprover;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * CmdAssistant 冒烟测试：验证安全命令执行、中文不乱码、敏感/高危命令拦截、批准路径、超时终止。
 * 依赖真实 Windows cmd.exe，仅在 Windows 上运行。
 */
class CmdAssistantTest {

    @BeforeAll
    static void requireWindows() {
        assumeTrue(System.getProperty("os.name", "").toLowerCase().contains("win"),
                "CmdAssistant 依赖 Windows cmd.exe，跳过非 Windows 环境");
    }

    @Test
    void 安全命令返回输出与退出码() {
        CmdAssistant assistant = new CmdAssistant();
        String result = assistant.runCmd("echo hello-cmd-test", null, 30, null);
        assertTrue(result.contains("hello-cmd-test"), "应包含命令输出，实际: " + result);
        assertTrue(result.contains("退出码: 0"), "应包含退出码 0，实际: " + result);
        assertTrue(result.contains("标准输出"), "应包含标准输出段，实际: " + result);
    }

    @Test
    void 中文输出不乱码() {
        CmdAssistant assistant = new CmdAssistant();
        String result = assistant.runCmd("echo 你好世界测试", null, 30, null);
        assertTrue(result.contains("你好世界测试"),
                "中文输出不应乱码（UTF-8/GBK 自动识别），实际: " + result);
    }

    @Test
    void 工作目录参数生效() {
        CmdAssistant assistant = new CmdAssistant();
        String result = assistant.runCmd("cd", System.getProperty("user.home"), 30, null);
        assertTrue(result.toLowerCase().contains(System.getProperty("user.home").toLowerCase()),
                "cd 应输出指定工作目录，实际: " + result);
    }

    @Test
    void 敏感删除命令无批准通道时被拒() {
        CmdAssistant assistant = new CmdAssistant(); // approver=null
        String result = assistant.runCmd("del C:\\temp\\unnecessary-file.txt", null, 30, null);
        assertTrue(result.contains("命令未执行"), "应拒绝执行，实际: " + result);
        assertTrue(result.contains("删除"), "应说明敏感原因，实际: " + result);
        assertFalse(result.contains("退出码"), "未批准时不应真正执行命令");
    }

    @Test
    void 敏感命令被用户拒绝时不执行() {
        CmdAssistant assistant = new CmdAssistant((cmd, reason) -> false);
        String result = assistant.runCmd("del C:\\temp\\unnecessary-file.txt", null, 30, null);
        assertTrue(result.contains("用户未批准"), "应报告用户拒绝，实际: " + result);
        assertFalse(result.contains("退出码"), "拒绝后不应执行命令");
    }

    @Test
    void 敏感命令获批准后执行() {
        CmdAssistant assistant = new CmdAssistant((cmd, reason) -> true);
        // del 不存在的文件是安全无害的，仅验证批准后走了真实执行路径
        String result = assistant.runCmd("del C:\\definitely-not-exist-cmd-test-xyz.txt", null, 30, null);
        assertTrue(result.contains("已获用户批准"), "批准后应标注已批准，实际: " + result);
        assertTrue(result.contains("退出码"), "批准后应真正执行，实际: " + result);
    }

    @Test
    void 高危命令即使批准也拒绝() {
        AtomicInteger asked = new AtomicInteger();
        CmdAssistant assistant = new CmdAssistant((cmd, reason) -> {
            asked.incrementAndGet();
            return true;
        });
        String result = assistant.runCmd("format c:", null, 30, null);
        assertTrue(result.contains("风险过高"), "高危命令应被拒绝，实际: " + result);
        assertTrue(result.contains("不会执行"), "应明确任何情况都不执行，实际: " + result);
        assertFalse(result.contains("已获用户批准"), "不应走到批准路径");
        assertFalse(result.contains("退出码"), "不应真正执行");
        assertTrue(asked.get() == 0, "高危命令不应询问用户");
    }

    @Test
    void 复合命令中的敏感段被拦截() {
        CmdAssistant assistant = new CmdAssistant((cmd, reason) -> false);
        String result = assistant.runCmd("dir C:\\ & del C:\\temp\\unnecessary-file.txt", null, 30, null);
        assertTrue(result.contains("命令未执行"), "含删除段的复合命令应整体拦截，实际: " + result);
        assertFalse(result.contains("退出码"), "拦截后不应执行任何一段");
    }

    @Test
    void 超时命令被强制终止() {
        CmdAssistant assistant = new CmdAssistant();
        String result = assistant.runCmd("ping -n 5 127.0.0.1", null, 1, null);
        assertTrue(result.contains("超时"), "应报告超时，实际: " + result);
        assertTrue(result.contains("强制终止"), "应说明已终止，实际: " + result);
    }
}
