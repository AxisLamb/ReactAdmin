package com.lain.ai.tools;

import io.agentscope.core.shutdown.AgentShuttingDownException;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CentOS文件系统操作工具类
 * 提供一系列用于操作CentOS文件系统的工具方法
 */
public class FileAssistant {

    /**
     * 执行shell命令
     *
     * @param command 要执行的命令
     * @return 命令执行结果
     */
    @Tool(description = "在CentOS系统上执行shell命令")
    public String executeShellCommand(
            @ToolParam(name = "command", description = "要执行的shell命令") String command) {
        StringBuilder result = new StringBuilder();
        Process process = null;

        try {
            // 使用bash执行命令，确保在CentOS环境下正常工作
            process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});

            // 读取标准输出
            BufferedReader stdoutReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = stdoutReader.readLine()) != null) {
                result.append(line).append("\n");
            }

            // 读取错误输出
            BufferedReader stderrReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
            while ((line = stderrReader.readLine()) != null) {
                result.append("ERROR: ").append(line).append("\n");
            }

            // 等待进程完成
            int exitCode = process.waitFor();
            result.append("\nExit code: ").append(exitCode);

        } catch (IOException | InterruptedException e) {
            throw new AgentShuttingDownException("执行命令失败: " + e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        return result.toString();
    }

    /**
     * 列出指定目录下的文件和子目录
     *
     * @param directoryPath 目录路径
     * @return 目录内容列表
     */
    @Tool(description = "列出指定目录下的文件和子目录")
    public String listDirectory(
            @ToolParam(name = "path", description = "要列出的目录路径") String directoryPath) {
        Path path = Paths.get(directoryPath);
        
        if (!Files.exists(path)) {
            return "目录不存在: " + directoryPath;
        }
        
        if (!Files.isDirectory(path)) {
            return "路径不是目录: " + directoryPath;
        }

        try {
            List<String> entries = Files.list(path)
                    .map(p -> {
                        String fileName = p.getFileName().toString();
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
                            String type = attrs.isDirectory() ? "[DIR]" : "[FILE]";
                            long size = attrs.size();
                            return String.format("%-5s %10d bytes %s", type, size, fileName);
                        } catch (IOException e) {
                            return String.format("[UNK] %10s %s", "-", fileName);
                        }
                    })
                    .collect(Collectors.toList());

            StringBuilder result = new StringBuilder();
            result.append("目录内容: ").append(directoryPath).append("\n");
            result.append(String.join("\n", entries));
            
            return result.toString();
        } catch (IOException e) {
            return "列出目录失败: " + e.getMessage();
        }
    }

    /**
     * 读取文件内容
     *
     * @param filePath 文件路径
     * @return 文件内容
     */
    @Tool(description = "读取文件内容")
    public String readFile(
            @ToolParam(name = "path", description = "要读取的文件路径") String filePath) {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            return "文件不存在: " + filePath;
        }
        
        if (Files.isDirectory(path)) {
            return "路径是目录，不是文件: " + filePath;
        }

        try {
            String content = Files.readString(path);
            return "文件内容 (" + filePath + "):\n" + content;
        } catch (IOException e) {
            return "读取文件失败: " + e.getMessage();
        }
    }

    /**
     * 写入内容到文件
     *
     * @param filePath 文件路径
     * @param content  要写入的内容
     * @return 操作结果
     */
    @Tool(description = "写入内容到文件，如果文件不存在则创建")
    public String writeFile(
            @ToolParam(name = "path", description = "要写入的文件路径") String filePath,
            @ToolParam(name = "content", description = "要写入的文件内容") String content) {
        Path path = Paths.get(filePath);
        
        try {
            // 确保父目录存在
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            
            // 写入文件
            Files.write(path, content.getBytes("UTF-8"), 
                       StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            
            return "成功写入文件: " + filePath;
        } catch (IOException e) {
            return "写入文件失败: " + e.getMessage();
        }
    }

    /**
     * 创建目录
     *
     * @param directoryPath 目录路径
     * @return 操作结果
     */
    @Tool(description = "创建目录，如果父目录不存在也会一同创建")
    public String createDirectory(
            @ToolParam(name = "path", description = "要创建的目录路径") String directoryPath) {
        Path path = Paths.get(directoryPath);
        
        try {
            Files.createDirectories(path);
            return "成功创建目录: " + directoryPath;
        } catch (IOException e) {
            return "创建目录失败: " + e.getMessage();
        }
    }

    /**
     * 删除文件或空目录
     *
     * @param path 文件或目录路径
     * @return 操作结果
     */
    @Tool(description = "删除文件或空目录")
    public String deleteFileOrDirectory(
            @ToolParam(name = "path", description = "要删除的文件或目录路径") String pathStr) {
        Path path = Paths.get(pathStr);
        
        if (!Files.exists(path)) {
            return "路径不存在: " + pathStr;
        }

        try {
            if (Files.isDirectory(path)) {
                // 检查目录是否为空
                try (var stream = Files.list(path)) {
                    if (stream.findAny().isPresent()) {
                        return "目录不为空，无法删除: " + pathStr;
                    }
                }
            }
            
            Files.delete(path);
            return "成功删除: " + pathStr;
        } catch (IOException e) {
            return "删除失败: " + e.getMessage();
        }
    }

    /**
     * 搜索文件
     *
     * @param directory 要搜索的目录
     * @param pattern   文件名模式（支持通配符）
     * @return 找到的文件列表
     */
    @Tool(description = "在指定目录中搜索符合模式的文件")
    public String searchFiles(
            @ToolParam(name = "directory", description = "要搜索的目录路径") String directory,
            @ToolParam(name = "pattern", description = "文件名模式（支持通配符，如 *.txt, *.log 等）") String pattern) {
        Path dirPath = Paths.get(directory);
        
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            return "目录不存在或不是目录: " + directory;
        }

        // 将通配符模式转换为正则表达式
        String regexPattern = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".?");
        
        List<String> foundFiles = new ArrayList<>();
        
        try {
            Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.getFileName().toString();
                    if (fileName.matches(regexPattern)) {
                        foundFiles.add(file.toString());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            
            if (foundFiles.isEmpty()) {
                return "在目录 " + directory + " 中未找到匹配 '" + pattern + "' 的文件";
            } else {
                return "在目录 " + directory + " 中找到 " + foundFiles.size() + " 个匹配的文件:\n" +
                       String.join("\n", foundFiles);
            }
        } catch (IOException e) {
            return "搜索文件时发生错误: " + e.getMessage();
        }
    }

    /**
     * 获取文件详细信息
     *
     * @param path 文件路径
     * @return 文件详细信息
     */
    @Tool(description = "获取文件的详细信息，包括大小、修改时间等")
    public String getFileDetails(
            @ToolParam(name = "path", description = "文件或目录路径") String pathStr) {
        Path path = Paths.get(pathStr);
        
        if (!Files.exists(path)) {
            return "路径不存在: " + pathStr;
        }

        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            
            StringBuilder result = new StringBuilder();
            result.append("路径: ").append(pathStr).append("\n");
            result.append("类型: ").append(attrs.isDirectory() ? "目录" : "文件").append("\n");
            result.append("大小: ").append(attrs.size()).append(" 字节\n");
            result.append("创建时间: ").append(attrs.creationTime()).append("\n");
            result.append("最后修改时间: ").append(attrs.lastModifiedTime()).append("\n");
            result.append("最后访问时间: ").append(attrs.lastAccessTime()).append("\n");
            result.append("是否为符号链接: ").append(attrs.isSymbolicLink()).append("\n");
            
            return result.toString();
        } catch (IOException e) {
            return "获取文件信息失败: " + e.getMessage();
        }
    }
}
