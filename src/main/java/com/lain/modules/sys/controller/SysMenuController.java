package com.lain.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.StrUtil;
import com.lain.common.vo.R;
import com.lain.modules.sys.service.SysMenuService;
import com.lain.modules.sys.vo.RouteConfig;
import com.lain.modules.sys.vo.SysMenuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/menu")
@Tag(name = "Sys Menu", description = "系统菜单管理")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    @GetMapping("/routes")
    @SaCheckLogin
    @Operation(summary = "list menus", description = "获取菜单列表")
    public R getFrontendRoutes() {
        List<RouteConfig> routes = sysMenuService.getReactRoutes();
        return R.ok(routes);
    }

    @GetMapping("/list")
    @SaCheckLogin
    @Operation(summary = "list menus", description = "获取个人菜单列表")
    public R listMenus() {
        List<SysMenuVO> menuVOS = sysMenuService.listMenus(true);
        return R.ok(menuVOS);
    }

    @GetMapping("/listAll")
    @SaCheckPermission("sys:menu:list")
    @Operation(summary = "list all menus", description = "获取所有菜单列表")
    public R listAllMenus() {
        List<SysMenuVO> menuVOS = sysMenuService.listMenus(false);
        return R.ok(menuVOS);
    }

    @PostMapping("/add")
    @SaCheckPermission("sys:menu:save")
    @Operation(summary = "add menu", description = "添加菜单")
    public R addMenu(@RequestBody SysMenuVO sysMenuVO) {
        String msg = sysMenuService.addMenu(sysMenuVO);
        if (StrUtil.isBlank(msg)) {
            return R.ok("添加菜单成功");
        } else {
            return R.error(msg);
        }
    }

    @PostMapping("/update")
    @SaCheckPermission("sys:menu:update")
    @Operation(summary = "update menu", description = "更新菜单")
    public R updateMenu(@RequestBody SysMenuVO sysMenuVO) {
        String msg = sysMenuService.updateMenu(sysMenuVO);
        if (StrUtil.isBlank(msg)) {
            return R.ok("更新菜单成功");
        } else {
            return R.error(msg);
        }
    }

    @PostMapping("/delete/{menuId}")
    @SaCheckPermission("sys:menu:delete")
    @Operation(summary = "delete menu", description = "删除菜单")
    public R deleteMenu(@PathVariable Long menuId) {
        String msg = sysMenuService.deleteMenu(menuId);
        if (StrUtil.isBlank(msg)) {
            return R.ok("删除菜单成功");
        } else {
            return R.error(msg);
        }
    }
}
