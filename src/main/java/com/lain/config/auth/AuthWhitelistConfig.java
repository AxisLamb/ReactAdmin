package com.lain.config.auth;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class AuthWhitelistConfig {

    /**
     * 不需要认证的路径白名单
     */
    private static final List<String> WHITELIST_PATHS = Arrays.asList(
        "/swagger/**",
        "/swagger-ui.html",
        "/swagger-resources/**",
        "/doc.html",
        "/webjars/**",
        "/v3/**",
        "/v3/api-docs/**",
        "/v3/api-docs-ext/**",
        "/favicon.ico",
        "/auth/login",
        "/error"
    );

    /**
     * 获取白名单路径列表
     * @return 白名单路径列表
     */
    public List<String> getWhitelistPaths() {
        return WHITELIST_PATHS;
    }

    /**
     * 判断路径是否在白名单中
     * @param requestURI 请求路径
     * @return 是否在白名单中
     */
    public boolean isWhitelisted(String requestURI) {
        return WHITELIST_PATHS.stream()
                .anyMatch(pattern -> pathMatchesPattern(requestURI, pattern));
    }

    /**
     * 简单的路径匹配逻辑
     * @param path 请求路径
     * @param pattern 匹配模式
     * @return 是否匹配
     */
    private boolean pathMatchesPattern(String path, String pattern) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.contains(prefix);
        }
        return path.contains(pattern);
    }
}
