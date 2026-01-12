package com.lain.modules.sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lain.modules.sys.entity.SysRole;
import com.lain.modules.sys.vo.SysRoleVO;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    /**
     * 获取角色的权限列表
     */
    List<String> getRolePermissions(Long roleId);

    /**
     * 获取用户的角色列表
     */
    List<SysRole> findRolesByUserId(Long userId);

    /**
     * 获取角色的所有菜单ID
     */
    List<Long> findRolesPermissions(List<Long> roleIds);

    /**
     * 根据用户ID获取角色数量
     */
    Long getRoleCountByUserIds(List<Long> userIds);

    /**
     * 分页查询角色列表
     */
    Page<SysRoleVO> pageList(SysRoleVO roleVO);

    /**
     * 获取所有角色列表
     */
    List<SysRoleVO> listAllRoles();

    /**
     * 新增角色
     */
    void addRole(SysRoleVO roleVO);

    /**
     * 修改角色
     */
    void updateRole(SysRoleVO roleVO);

    /**
     * 删除角色
     */
    void deleteRole(Long id);
}
