package com.lain.config.auth;

import cn.dev33.satoken.stp.StpInterface;
import com.lain.modules.sys.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SaTokenConfigure implements StpInterface {

    @Autowired
    private AuthService authService;

    /**
     * 获取用户的角色列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // loginId 是用户ID
        Long userId = Long.valueOf(loginId.toString());
        return authService.getUserRoles(userId);
    }

    /**
     * 获取用户的权限列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // loginId 是用户ID
        Long userId = Long.valueOf(loginId.toString());
        return authService.getUserPermissions(userId);
    }
}
