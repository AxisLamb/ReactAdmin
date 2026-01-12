package com.lain.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lain.common.vo.R;
import com.lain.modules.sys.service.SysRoleService;
import com.lain.modules.sys.vo.SysRoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/role")
@Tag(name = "Sys Role", description = "系统角色管理")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @GetMapping("/page")
    @SaCheckPermission("sys:role:list")
    @Operation(summary = "Role page list", description = "分页查询角色列表")
    public R page(@ModelAttribute SysRoleVO roleVO) {
        Page<SysRoleVO> voPage = sysRoleService.pageList(roleVO);
        return R.ok(voPage);
    }

    @GetMapping("/roles")
    @SaCheckLogin
    @Operation(summary = "list roles", description = "获取所有角色列表（带权限）")
    public R<List<SysRoleVO>> listRoles() {
        List<SysRoleVO> rolesVO = sysRoleService.listAllRoles();
        return R.ok(rolesVO);
    }

    // 添加角色
    @PostMapping("/add")
    @SaCheckPermission("sys:role:save")
    @Operation(summary = "Add role", description = "添加角色")
    public R addRole(@RequestBody SysRoleVO roleVO) {
        sysRoleService.addRole(roleVO);
        return R.ok("新增角色成功");
    }

    // 更新角色
    @PostMapping("/update")
    @SaCheckPermission("sys:role:update")
    @Operation(summary = "Update role", description = "更新角色")
    public R updateRole(@RequestBody SysRoleVO roleVO) {
        sysRoleService.updateRole(roleVO);
        return R.ok("修改角色成功");
    }

    // 删除角色
    @PostMapping("/delete")
    @SaCheckPermission("sys:role:delete")
    @Operation(summary = "Delete role", description = "删除角色")
    public R deleteRole(Long id) {
        sysRoleService.deleteRole(id);
        return R.ok("删除角色成功");
    }

}
