package com.lain.config.auth;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class SaTokenHeaderFilter implements Filter {

    @Autowired
    private AuthWhitelistConfig authWhitelistConfig;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 获取请求路径
        String requestURI = httpRequest.getRequestURI();

        // 排除不需要校验的路径（如登录、注册等公开接口）
        if (authWhitelistConfig.isWhitelisted(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // 检查是否包含 satoken header
        String saToken = httpRequest.getHeader("satoken");
        if (saToken == null || saToken.trim().isEmpty()) {
            // 未包含 satoken header，返回 401 未授权
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"code\":401,\"msg\":\"缺少 satoken 请求头\"}");
            return;
        }

        // 继续执行下一个过滤器
        chain.doFilter(request, response);
    }

}