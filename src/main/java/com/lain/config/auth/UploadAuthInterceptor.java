package com.lain.config.auth;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

public class UploadAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();
        if (uri.contains("loginpage")) {
            return true;
        }

        // ① 登录校验
        if (!StpUtil.isLogin()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录，无法访问\"}");
            return false;
        }

        // ② 解析文件名中的 businessId
        String businessId = parseBusinessId(uri);
        if (businessId == null) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":400,\"msg\":\"非法文件名格式\"}");
            return false;
        }

        // ③ 归属校验
        String loginId = StpUtil.getLoginIdAsString();
        if (!loginId.equals(businessId)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"msg\":\"无权访问该图片\"}");
            return false;
        }

        return true;
    }

    /**
     * /upload/9f8e7d6c5b4a3f2e_123.png → "123"
     * /upload/sub/abc_def_456.jpg      → "456"
     */
    private String parseBusinessId(String uri) {
        // 取文件名
        int slashIdx = uri.lastIndexOf('/');
        String fileName = (slashIdx >= 0) ? uri.substring(slashIdx + 1) : uri;

        // 去扩展名
        int dotIdx = fileName.lastIndexOf('.');
        String base = (dotIdx > 0) ? fileName.substring(0, dotIdx) : fileName;

        // 取最后一个 _ 后面
        int usIdx = base.lastIndexOf('_');
        if (usIdx < 0 || usIdx >= base.length() - 1) {
            return null;
        }
        return base.substring(usIdx + 1);
    }
}