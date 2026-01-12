package com.lain.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.lain.common.annotation.AuditLog;
import com.lain.common.vo.R;
import com.lain.modules.sys.service.AuthService;
import com.lain.modules.sys.vo.LoginParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
@Tag(name = "认证接口", description = "用户认证相关接口")
public class AuthController {

    @Autowired
    private AuthService authService;

    @SaIgnore
    @Operation(summary = "用户登录", description = "用户登录接口")
    @PostMapping("/login")
    public R login(@Valid @RequestBody LoginParam loginDTO) {

        // 返回token信息
        return authService.login(loginDTO);
    }

    @SaCheckLogin
    @Operation(summary = "用户退出", description = "用户退出登录")
    @PostMapping("/logout")
    public R logout() {
        StpUtil.logout();
        return R.ok(true);
    }

    @SaIgnore
    @PostMapping("/test")
    @Operation(summary = "test", description = "test")
    public R test() {
        return authService.test();
    }
}
