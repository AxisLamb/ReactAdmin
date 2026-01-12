package com.lain.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lain.common.annotation.AuditLog;
import com.lain.common.vo.R;
import com.lain.config.auth.AuthConstant;
import com.lain.modules.sys.service.SysUserService;
import com.lain.modules.sys.vo.SysUserVO;
import com.lain.modules.sys.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/sys/user")
@Tag(name = "Sys User", description = "系统用户管理")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/page")
    @SaCheckPermission("sys:user:list")
    @Operation(summary = "user page list", description = "分页查询用户列表")
    public R page(@ModelAttribute SysUserVO userVO) {
        Page<SysUserVO> voPage = sysUserService.pageList(userVO);
        return R.ok(voPage);
    }


    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/info")
    @SaCheckLogin
    @Operation(summary = "get user info", description = "根据用户ID获取用户信息")
    public R info() {
        SaSession tokenSession = StpUtil.getTokenSession();
        String userId = tokenSession.getString(AuthConstant.TOKEN_SESSION_KEY_USER_ID);
        UserInfoVO userInfoVO = sysUserService.getUserInfo(userId);

        return R.ok(userInfoVO);
    }

    /**
     * 新增用户
     */
    @PostMapping("/save")
    @SaCheckPermission("sys:user:save")
    @AuditLog("新增用户")
    @Operation(summary = "save user", description = "新增用户")
    public R save(@RequestBody SysUserVO userVO) {
        return sysUserService.saveUser(userVO);
    }

    /**
     * 修改用户
     */
    @PostMapping("/update")
    @SaCheckPermission("sys:user:update")
    @Operation(summary = "update user", description = "修改用户")
    public R update(@RequestBody SysUserVO userVO) {
        return sysUserService.updateUser(userVO);
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @SaCheckPermission("sys:user:delete")
    @Operation(summary = "delete users", description = "批量删除用户")
    public R delete(@RequestBody Long[] userIds) {
        return sysUserService.deleteUserByIds(Arrays.asList(userIds));
    }
}
