package com.lain.modules.xianyu.agent;

import com.lain.modules.xianyu.config.XianyuProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提示词加载器
 * <p>
 * 加载优先级：外部目录 {prompt-dir}/{name}.txt → classpath prompts/{name}.txt → classpath prompts/{name}_example.txt
 */
@Component
public class PromptLoader {

    private static final Logger log = LoggerFactory.getLogger(PromptLoader.class);

    private final XianyuProperties properties;

    /** 提示词缓存 */
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public PromptLoader(XianyuProperties properties) {
        this.properties = properties;
    }

    /**
     * 加载提示词内容（带缓存）
     *
     * @param name 提示词名称，如 classify_prompt
     */
    public String load(String name) {
        return cache.computeIfAbsent(name, this::loadInternal);
    }

    /**
     * 清空缓存并重新加载
     */
    public void reload() {
        cache.clear();
    }

    private String loadInternal(String name) {
        String content = null;

        // 1. 优先加载外部目录（若配置了 prompt-dir）
        String promptDir = properties.getPromptDir();
        if (promptDir != null && !promptDir.isBlank()) {
            content = readFile(Paths.get(promptDir, name + ".txt"));
            if (content != null) {
                log.debug("已加载 {} 提示词，路径: {}/{}.txt", name, promptDir, name);
            }
        }

        // 2. 尝试 classpath 自定义文件 {name}.txt
        if (content == null) {
            content = readClasspath("prompts/" + name + ".txt");
            if (content != null) {
                log.debug("已加载 {} 提示词，路径: classpath:prompts/{}.txt", name, name);
            }
        }

        // 3. 兜底使用 classpath 默认文件 {name}_example.txt
        if (content == null) {
            content = readClasspath("prompts/" + name + "_example.txt");
            if (content != null) {
                log.debug("已加载 {} 提示词，路径: classpath:prompts/{}_example.txt", name, name);
            }
        }

        if (content == null) {
            throw new IllegalStateException("无法加载提示词: " + name + "（请检查 classpath:prompts/ 或外部 prompt-dir 配置）");
        }
        return content;
    }

    private String readFile(Path path) {
        try {
            if (Files.exists(path)) {
                return Files.readString(path, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.warn("读取提示词文件失败: {} - {}", path, e.getMessage());
        }
        return null;
    }

    private String readClasspath(String location) {
        try {
            ClassPathResource resource = new ClassPathResource(location);
            if (resource.exists()) {
                return resource.getContentAsString(StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.warn("读取 classpath 提示词失败: {} - {}", location, e.getMessage());
        }
        return null;
    }
}
