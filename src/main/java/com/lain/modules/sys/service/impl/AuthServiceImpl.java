package com.lain.modules.sys.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lain.common.cache.TestCacheKeyBuilder;
import com.lain.common.utils.SecurityUtil;
import com.lain.common.vo.R;
import com.lain.config.auth.AuthConstant;
import com.lain.config.cache.redis2.CacheResult;
import com.lain.config.cache.repository.CacheOps;
import com.lain.config.cache.vo.CacheHashKey;
import com.lain.modules.sys.dao.SysMenuMapper;
import com.lain.modules.sys.dao.SysRoleMapper;
import com.lain.modules.sys.dao.SysRoleMenuMapper;
import com.lain.modules.sys.dao.SysUserRoleMapper;
import com.lain.modules.sys.entity.*;
import com.lain.modules.sys.service.AuthService;
import com.lain.modules.sys.service.SysUserService;
import com.lain.modules.sys.vo.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Autowired
    private CacheOps cacheOps;

    @Override
    public R login(LoginParam loginParam) {
        // 根据用户名查找用户
        SysUser user = sysUserService.findByUsername(loginParam.getUsername().trim());
        if (user == null) {
            return R.error("用户名或密码错误");
        }

        // 验证密码
        if (!SecurityUtil.verifyPassword(loginParam.getPassword().trim(), user.getPassword())) {
            return R.error("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            return R.error("账户已被禁用");
        }

        // 登录授权
        StpUtil.login(user.getUserId());
        SaSession tokenSession = StpUtil.getTokenSession();
        tokenSession.set(AuthConstant.TOKEN_SESSION_KEY_USER_NAME, user.getUsername());
        tokenSession.set(AuthConstant.TOKEN_SESSION_KEY_USER_ID, String.valueOf(user.getUserId()));
        tokenSession.setLoginId(String.valueOf(user.getUserId()));

        return R.ok(StpUtil.getTokenValue());
    }


    @Override
    public List<String> getUserRoles(Long userId) {
        // 查询用户角色关联
        LambdaQueryWrapper<SysUserRole> userRoleQuery = new LambdaQueryWrapper<>();
        userRoleQuery.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(userRoleQuery);

        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取角色ID列表
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        // 查询角色信息
        LambdaQueryWrapper<SysRole> roleQuery = new LambdaQueryWrapper<>();
        roleQuery.in(SysRole::getRoleId, roleIds);
        List<SysRole> roles = sysRoleMapper.selectList(roleQuery);

        // 返回角色名称列表
        return roles.stream()
                .map(SysRole::getRoleName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        // 查询用户角色关联
        LambdaQueryWrapper<SysUserRole> userRoleQuery = new LambdaQueryWrapper<>();
        userRoleQuery.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(userRoleQuery);

        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取角色ID列表
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        // 查询角色菜单关联，获取菜单权限
        LambdaQueryWrapper<SysRoleMenu> roleMenuQuery = new LambdaQueryWrapper<>();
        roleMenuQuery.in(SysRoleMenu::getRoleId, roleIds);
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(roleMenuQuery);

        if (roleMenus.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取菜单ID列表
        List<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());

        // 查询菜单信息，获取权限标识
        LambdaQueryWrapper<SysMenu> menuQuery = new LambdaQueryWrapper<>();
        menuQuery.in(SysMenu::getMenuId, menuIds);
        menuQuery.isNotNull(SysMenu::getPerms);
        menuQuery.ne(SysMenu::getPerms, "");
        List<SysMenu> menus = sysMenuMapper.selectList(menuQuery);

        // 返回权限标识列表，过滤空值并去重
        return menus.stream()
                .map(SysMenu::getPerms)
                .filter(perms -> perms != null && !perms.isEmpty())
                .flatMap(perms -> Arrays.stream(perms.split(",")))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public R test() {
        CacheHashKey key = TestCacheKeyBuilder.build();
        CacheResult<String> result = cacheOps.get(key, k -> {
            return "hello";
        });
        return R.ok(result.getValue());
    }

}
