package com.lain.modules.sys.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lain.common.exception.LainException;
import com.lain.modules.sys.dao.SysMenuMapper;
import com.lain.modules.sys.dao.SysRoleMapper;
import com.lain.modules.sys.dao.SysRoleMenuMapper;
import com.lain.modules.sys.dao.SysUserRoleMapper;
import com.lain.modules.sys.entity.SysMenu;
import com.lain.modules.sys.entity.SysRole;
import com.lain.modules.sys.entity.SysRoleMenu;
import com.lain.modules.sys.entity.SysUserRole;
import com.lain.modules.sys.service.SysRoleService;
import com.lain.modules.sys.vo.SysRoleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Override
    public List<String> getRolePermissions(Long roleId) {
        LambdaQueryWrapper<SysRoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleMenu::getRoleId, roleId);

        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(queryWrapper);

        return roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    @Override
    public List<SysRole> findRolesByUserId(Long userId) {
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> sysUserRoles = sysUserRoleMapper.selectList(queryWrapper);
        return sysUserRoles.stream()
                .map(SysUserRole::getRoleId)
                .map(this::getById)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findRolesPermissions(List<Long> roleIds) {
        List<SysRoleMenu> sysRoleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));
        return sysRoleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
    }

    @Override
    public Long getRoleCountByUserIds(List<Long> userIds) {
        LambdaQueryWrapper<SysUserRole> userRoleQuery = new LambdaQueryWrapper<>();
        userRoleQuery.in(SysUserRole::getUserId, userIds);
        return sysUserRoleMapper.selectCount(userRoleQuery);
    }

    @Override
    public Page<SysRoleVO> pageList(SysRoleVO roleVO) {
        Page<SysRole> pageParam = new Page<>(roleVO.getCurrent(), roleVO.getSize());
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(roleVO.getRoleName())) {
            queryWrapper.like(SysRole::getRoleName, roleVO.getRoleName());
        }

        queryWrapper.orderByDesc(SysRole::getCreateTime);
        Page<SysRole> result = page(pageParam, queryWrapper);
        List<SysRoleVO> records = result.getRecords().stream().map(sysRole -> {
            SysRoleVO vo = new SysRoleVO();
            BeanUtils.copyProperties(sysRole, vo);
            List<Long> roleMenuIds = findRolesPermissions(Collections.singletonList(sysRole.getRoleId()));
            vo.setMenuIds(roleMenuIds);
            return vo;
        }).toList();

        // 构造返回结果
        Page<SysRoleVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public List<SysRoleVO> listAllRoles() {
        List<SysRoleVO> result = new ArrayList<>();
        List<SysRole> roles = list();
        for (SysRole role : roles) {
            SysRoleVO roleVO = new SysRoleVO();
            BeanUtils.copyProperties(role, roleVO);
            List<Long> roleMenuIds = findRolesPermissions(Collections.singletonList(role.getRoleId()));
            roleVO.setMenuIds(roleMenuIds);
            result.add(roleVO);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(SysRoleVO roleVO) {
        if (roleVO == null) {
            throw new LainException("参数不能为空");
        }
        if (StrUtil.isBlank(roleVO.getRoleName())) {
            throw new LainException("角色名称不能为空");
        }
        if (StrUtil.isBlank(roleVO.getRoleDesc())) {
            throw new LainException("角色描述不能为空");
        }
        // 检查角色名是否与其他角色重复
        boolean isExistRoleName = validExistRoleName(roleVO);
        if (isExistRoleName) {
            throw new LainException("角色名已存在");
        }
        SysRole sysRole = new SysRole();
        BeanUtils.copyProperties(roleVO, sysRole);
        save(sysRole);
        // 保存角色菜单权限
        saveRoleMenus(roleVO, sysRole.getRoleId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(SysRoleVO roleVO) {
        if (roleVO == null) {
            throw new LainException("参数不能为空");
        }
        if (roleVO.getRoleId() == null) {
            throw new LainException("角色ID不能为空");
        }
        SysRole isExist = this.getById(roleVO.getRoleId());
        if (isExist == null) {
            throw new LainException("角色不存在");
        }
        // 检查角色名是否与其他角色重复
        boolean isExistRoleName = validExistRoleName(roleVO);
        if (isExistRoleName) {
            throw new LainException("角色名已存在");
        }

        // 更新角色信息
        SysRole sysRole = new SysRole();
        BeanUtils.copyProperties(roleVO, sysRole);
        updateById(sysRole);

        // 先删除旧的角色菜单权限
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleVO.getRoleId()));

        saveRoleMenus(roleVO, sysRole.getRoleId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        if (id == null) {
            throw new LainException("角色ID不能为空");
        }

        // 检查角色是否存在
        SysRole sysRole = this.getById(id);
        if (sysRole == null) {
            throw new LainException("角色不存在");
        }

        // 检查角色是否关联了用户
        LambdaQueryWrapper<SysUserRole> userRoleQuery = new LambdaQueryWrapper<>();
        userRoleQuery.eq(SysUserRole::getRoleId, id);
        long userCount = sysUserRoleMapper.selectCount(userRoleQuery);
        if (userCount > 0) {
            throw new LainException("该角色已关联用户，无法删除");
        }

        // 检查角色是否关联了菜单权限
        LambdaQueryWrapper<SysRoleMenu> roleMenuQuery = new LambdaQueryWrapper<>();
        roleMenuQuery.eq(SysRoleMenu::getRoleId, id);
        long menuCount = sysRoleMenuMapper.selectCount(roleMenuQuery);
        if (menuCount > 0) {
            throw new LainException("该角色已关联菜单权限，无法删除");
        }

        // 删除角色
        removeById(id);
    }


    /**
     * 保存角色菜单权限
     *
     * @param roleVO
     * @param roleId
     */
//    private void saveRoleMenus(SysRoleVO roleVO, Long roleId) {
//        // 重新添加角色菜单权限
//        if (roleVO.getMenuIds() != null && !roleVO.getMenuIds().isEmpty()) {
//            List<SysRoleMenu> roleMenus = roleVO.getMenuIds().stream()
//                    .map(menuId -> {
//                        SysRoleMenu roleMenu = new SysRoleMenu();
//                        roleMenu.setRoleId(roleId);
//                        roleMenu.setMenuId(menuId);
//                        return roleMenu;
//                    })
//                    .collect(Collectors.toList());
//            sysRoleMenuMapper.insert(roleMenus);
//        }
//    }

    /**
     * 保存角色菜单权限
     *
     * @param roleVO
     * @param roleId
     */
    private void saveRoleMenus(SysRoleVO roleVO, Long roleId) {
        // 重新添加角色菜单权限
        if (roleVO.getMenuIds() != null && !roleVO.getMenuIds().isEmpty()) {
            // 预加载所有菜单数据，避免多次查询数据库
            Map<Long, SysMenu> menuMap = getAllMenusAsMap();

            // 获取所有菜单ID（包括选中菜单及其所有父级菜单）
            Set<Long> allMenuIds = getAllMenuIdsWithParents(roleVO.getMenuIds(), menuMap);

            List<SysRoleMenu> roleMenus = allMenuIds.stream()
                    .map(menuId -> {
                        SysRoleMenu roleMenu = new SysRoleMenu();
                        roleMenu.setRoleId(roleId);
                        roleMenu.setMenuId(menuId);
                        return roleMenu;
                    })
                    .collect(Collectors.toList());

            // 先删除原有的角色菜单权限
            sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                    .eq(SysRoleMenu::getRoleId, roleId));

            // 批量插入新的角色菜单权限
            if (!roleMenus.isEmpty()) {
                // 使用批量插入提高性能
                for (SysRoleMenu roleMenu : roleMenus) {
                    sysRoleMenuMapper.insert(roleMenu);
                }
            }
        }
    }

    /**
     * 获取所有菜单作为Map，key为菜单ID，value为菜单实体
     *
     * @return 菜单Map
     */
    private Map<Long, SysMenu> getAllMenusAsMap() {
        List<SysMenu> allMenus = sysMenuMapper.selectList( null); // 获取所有菜单
        return allMenus.stream()
                .collect(Collectors.toMap(SysMenu::getMenuId, Function.identity()));
    }

    /**
     * 获取菜单ID及其所有父级菜单ID
     *
     * @param menuIds 起始菜单ID集合
     * @param menuMap 所有菜单的Map
     * @return 包含所有菜单及其父级菜单的ID集合
     */
    private Set<Long> getAllMenuIdsWithParents(List<Long> menuIds, Map<Long, SysMenu> menuMap) {
        Set<Long> allMenuIds = new HashSet<>();

        for (Long menuId : menuIds) {
            // 添加当前菜单ID
            allMenuIds.add(menuId);
            // 添加其所有父级菜单ID
            addParentMenuIds(allMenuIds, menuId, menuMap);
        }

        return allMenuIds;
    }

    /**
     * 递归添加父级菜单ID
     *
     * @param allMenuIds 存储所有菜单ID的集合
     * @param menuId 当前菜单ID
     * @param menuMap 所有菜单的Map
     */
    private void addParentMenuIds(Set<Long> allMenuIds, Long menuId, Map<Long, SysMenu> menuMap) {
        SysMenu currentMenu = menuMap.get(menuId);

        if (currentMenu != null && currentMenu.getParentId() != null && currentMenu.getParentId() > 0) {
            Long parentId = currentMenu.getParentId();

            // 如果父级菜单ID还没有被添加，则继续向上查找
            if (allMenuIds.add(parentId)) {
                // 继续递归查找更上层的父级菜单
                addParentMenuIds(allMenuIds, parentId, menuMap);
            }
        }
    }

    /**
     * 检查角色名是否与其他角色重复
     *
     * @param roleVO
     * @return
     */
    private boolean validExistRoleName(SysRoleVO roleVO) {
        boolean isAdd = true;
        if (roleVO.getRoleId() != null) {
            isAdd = false;
        }

        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<SysRole> eq = queryWrapper.eq(SysRole::getRoleName, roleVO.getRoleName());
        if (!isAdd) {
            eq = eq.ne(SysRole::getRoleId, roleVO.getRoleId()); // 排除当前角色
        }
        long count = count(queryWrapper);
        if (count > 0) {
            return true;
        }
        return false;
    }
}
