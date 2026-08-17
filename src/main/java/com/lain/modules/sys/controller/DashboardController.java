package com.lain.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lain.common.constant.StatusEnum;
import com.lain.common.vo.R;
import com.lain.config.auth.AuthConstant;
import com.lain.modules.sys.entity.SysDict;
import com.lain.modules.sys.entity.SysMenu;
import com.lain.modules.sys.entity.SysRole;
import com.lain.modules.sys.entity.SysUser;
import com.lain.modules.sys.service.SysDictService;
import com.lain.modules.sys.service.SysMenuService;
import com.lain.modules.sys.service.SysRoleService;
import com.lain.modules.sys.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作台数据统计
 */
@RestController
@RequestMapping("/sys/dashboard")
@Tag(name = "Sys Dashboard", description = "数据统计")
public class DashboardController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private SysDictService sysDictService;

    @GetMapping("/statistics")
    @SaCheckPermission("sys:dashboard:list")
    @Operation(summary = "statistics", description = "统计用户、角色、菜单、字典总数")
    public R statistics() {
        Map<String, Long> statistics = new HashMap<>();
        // 启用状态的用户
        statistics.put("userCount", sysUserService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, StatusEnum.ENABLE.getCode())));
        // 启用状态的角色
        statistics.put("roleCount", sysRoleService.count(new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, StatusEnum.ENABLE.getCode())));
        // 目录 + 菜单，不含按钮
        statistics.put("menuCount", sysMenuService.count(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getType, AuthConstant.MENU_TYPE_DIR, AuthConstant.MENU_TYPE_MENU)));
        // 启用状态的字典
        statistics.put("dictCount", sysDictService.count(new LambdaQueryWrapper<SysDict>().eq(SysDict::getStatus, StatusEnum.ENABLE.getCode())));
        return R.ok(statistics);
    }
}
