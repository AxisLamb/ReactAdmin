package com.lain.modules.sys.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lain.config.auth.AuthConstant;
import com.lain.modules.sys.dao.SysMenuMapper;
import com.lain.modules.sys.dao.SysRoleMenuMapper;
import com.lain.modules.sys.entity.SysMenu;
import com.lain.modules.sys.entity.SysRole;
import com.lain.modules.sys.entity.SysRoleMenu;
import com.lain.modules.sys.service.SysMenuService;
import com.lain.modules.sys.service.SysRoleService;
import com.lain.modules.sys.vo.RouteConfig;
import com.lain.modules.sys.vo.SysMenuVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public List<Long> findAllMenuIdsByUserId() {
        SaSession tokenSession = StpUtil.getTokenSession();
        String userId = tokenSession.getString(AuthConstant.TOKEN_SESSION_KEY_USER_ID);

        List<SysRole> roles = sysRoleService.findRolesByUserId(Long.parseLong(userId));
        if (CollectionUtil.isNotEmpty(roles)) {
            List<Long> roleIds = roles.stream().map(SysRole::getRoleId).toList();
            return sysRoleService.findRolesPermissions(roleIds);
        } else {
            return List.of();
        }
    }

    @Override
    public List<SysMenuVO> listMenus(boolean isPersonal) {
        // 查询当前登录用户的所有MenuIds
        // 创建查询条件，查找 type 为 0(Catalog), 1(Menu), 2(Button)的菜单
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.in(SysMenu::getType,
                AuthConstant.MENU_TYPE_DIR,
                AuthConstant.MENU_TYPE_MENU,
                AuthConstant.MENU_TYPE_BUTTON);

        if (isPersonal) {
            List<Long> menuIds = findAllMenuIdsByUserId();
            queryWrapper = queryWrapper.in(SysMenu::getMenuId, menuIds);
        }

        List<SysMenu> menus = list(queryWrapper);
        if (CollectionUtil.isEmpty(menus)) {
            return List.of();
        }
        return menus.stream()
                .map(menu -> {
                    SysMenuVO menuVO = new SysMenuVO();
                    BeanUtils.copyProperties(menu, menuVO);
                    return menuVO;
                }).collect(Collectors.toList());
    }

    @Override
    public String addMenu(SysMenuVO sysMenuVO) {

        // 必填校验
        if (StrUtil.isBlank(sysMenuVO.getName())) {
            return "菜单名称不能为空";
        }
        if (sysMenuVO.getType() == null) {
            return "菜单类型不能为空";
        }

        // 唯一性校验（假设name需要唯一）
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysMenu::getName, sysMenuVO.getName());
        long count = count(queryWrapper);
        if (count > 0) {
            return "菜单名称已存在";
        }

        // 检查父菜单类型是否大于子菜单类型
        String errorMsg = validateParentChildType(sysMenuVO.getParentId(), sysMenuVO.getType());
        if (errorMsg != null) {
            return errorMsg;
        }

        SysMenu sysMenu = new SysMenu();
        BeanUtils.copyProperties(sysMenuVO, sysMenu);

        // 执行添加
        save(sysMenu);
        return "";
    }

    @Override
    public String updateMenu(SysMenuVO sysMenuVO) {
        // ID 必须存在
        if (sysMenuVO.getMenuId() == null) {
            return "菜单ID不能为空";
        }

        // 检查记录是否存在
        SysMenu existingMenu = getById(sysMenuVO.getMenuId());
        if (existingMenu == null) {
            return "要更新的菜单不存在";
        }

        // 必填校验
        if (StrUtil.isBlank(sysMenuVO.getName())) {
            return "菜单名称不能为空";
        }
        if (sysMenuVO.getType() == null) {
            return "菜单类型不能为空";
        }

        // 唯一性校验
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysMenu::getName, sysMenuVO.getName())
                .ne(SysMenu::getMenuId, sysMenuVO.getMenuId()); // 排除当前记录
        long count = count(queryWrapper);
        if (count > 0) {
            return "菜单名称已存在";
        }

        // 检查父菜单类型是否大于子菜单类型
        String errorMsg = validateParentChildType(sysMenuVO.getParentId(), sysMenuVO.getType());
        if (errorMsg != null) {
            return errorMsg;
        }

        SysMenu sysMenu = new SysMenu();
        BeanUtils.copyProperties(sysMenuVO, sysMenu);

        // 执行更新
        updateById(sysMenu);
        return "";
    }

    @Override
    public String deleteMenu(Long menuId) {
        // 参数校验
        if (menuId == null || menuId <= 0) {
            return "菜单ID不能为空或无效";
        }

        // 检查记录是否存在
        SysMenu existingMenu = getById(menuId);
        if (existingMenu == null) {
            return "要删除的菜单不存在";
        }

        // 检查是否有子菜单（防止删除有子菜单的父菜单）
        LambdaQueryWrapper<SysMenu> childQuery = new LambdaQueryWrapper<>();
        childQuery.eq(SysMenu::getParentId, menuId);
        long childCount = count(childQuery);
        if (childCount > 0) {
            return "该菜单存在子菜单，无法删除";
        }

        // 同时删除角色菜单关联数据
        LambdaQueryWrapper<SysRoleMenu> roleMenuQuery = new LambdaQueryWrapper<>();
        roleMenuQuery.eq(SysRoleMenu::getMenuId, menuId);
        sysRoleMenuMapper.delete(roleMenuQuery);

        // 执行删除
        removeById(menuId);
        return "";
    }

    @Override
    public List<RouteConfig> getReactRoutes() {

        List<Long> menuIds = findAllMenuIdsByUserId();

        // 获取用户所有菜单数据
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.in(SysMenu::getMenuId, menuIds).orderByAsc(SysMenu::getOrderNum); // 按排序字段排序
        List<SysMenu> allMenus = list(queryWrapper);

        // 构建根节点（一级菜单）
        List<SysMenu> rootMenus = new ArrayList<>();
        Map<Long, SysMenu> menuMap = new HashMap<>();

        for (SysMenu menu : allMenus) {
            menuMap.put(menu.getMenuId(), menu);

            // 一级菜单或没有父菜单的菜单作为根节点
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                rootMenus.add(menu);
            }
        }

        // 构建路由配置
        List<RouteConfig> routes = new ArrayList<>();

        // 添加公共路由（如登录页）
        RouteConfig loginRoute = new RouteConfig("/login", "Login");
        loginRoute.setExact(true);
        routes.add(loginRoute);

        // 添加受保护的主布局路由
        RouteConfig mainLayoutRoute = new RouteConfig("/", "MainLayout");
        mainLayoutRoute.setProtectedRoute(true);
        // dashboard home page 默认添加
        RouteConfig dashBoard = new RouteConfig("dashboard", "Dashboard");
        List<RouteConfig> routeConfigs = buildChildrenRoutes(rootMenus, menuMap);
        routeConfigs.add(dashBoard);
        mainLayoutRoute.setChildren(routeConfigs);
        routes.add(mainLayoutRoute);

        return routes;
    }

    /**
     * 递归构建子路由
     */
    private List<RouteConfig> buildChildrenRoutes(List<SysMenu> menus, Map<Long, SysMenu> menuMap) {
        List<RouteConfig> childrenRoutes = new ArrayList<>();

        for (SysMenu menu : menus) {
            // 只处理目录(0)和菜单(1)，按钮(2)不生成路由
            if (menu.getType() != null && menu.getType() > 1) {
                continue;
            }

            RouteConfig route = createRouteFromMenu(menu);

            // 查找子菜单
            List<SysMenu> children = findChildren(menu.getMenuId(), menuMap);
            if (!children.isEmpty()) {
                route.setChildren(buildChildrenRoutes(children, menuMap));
            }

            childrenRoutes.add(route);
        }

        return childrenRoutes;
    }

    /**
     * 根据菜单信息创建路由对象
     */
    private RouteConfig createRouteFromMenu(SysMenu menu) {
        RouteConfig route = new RouteConfig();

        // 设置路径
        if (StrUtil.isNotBlank(menu.getUrl())) {
            if (menu.getUrl().contains("/")) {
                String[] split = menu.getUrl().split("/");
                route.setPath(split[split.length - 1]);
            } else {
                route.setPath(menu.getUrl());
            }
        } else {
            route.setPath(generatePathFromName(menu.getName()));
        }

        // 设置组件
        if (StrUtil.isNotBlank(menu.getReactComponent())) {
            route.setElement(menu.getReactComponent());
        }

        // 菜单类型为目录(0)时设置为受保护路由
        if (menu.getType() != null && menu.getType() == 0) {
            route.setProtectedRoute(true);
        }

        return route;
    }

    /**
     * 查找指定菜单的直接子菜单
     */
    private List<SysMenu> findChildren(Long parentId, Map<Long, SysMenu> menuMap) {
        List<SysMenu> children = new ArrayList<>();
        for (SysMenu menu : menuMap.values()) {
            if (menu.getParentId() != null && menu.getParentId().equals(parentId)) {
                // 只包含目录和菜单，排除按钮
                if (menu.getType() == null || menu.getType() <= 1) {
                    children.add(menu);
                }
            }
        }
        // 按排序字段排序
        children.sort(Comparator.comparing(SysMenu::getOrderNum));
        return children;
    }

    /**
     * 根据菜单名称生成路径
     */
    private String generatePathFromName(String name) {
        if (StrUtil.isBlank(name)) {
            return "unknown";
        }
        return StrUtil.toSymbolCase(name, '-').toLowerCase();
    }

    /**
     * 根据菜单名称生成默认组件名
     */
    private String getDefaultComponentName(String name) {
        if (StrUtil.isBlank(name)) {
            return "Unknown";
        }
        return StrUtil.upperFirst(StrUtil.toCamelCase(name));
    }


    /**
     * 校验父菜单类型是否大于子菜单类型
     * @param parentId 父菜单ID
     * @param childType 子菜单类型
     * @return 校验结果，返回null表示校验通过，返回错误信息表示校验失败
     */
    private String validateParentChildType(Long parentId, Integer childType) {
        if (parentId != null && parentId > 0) {
            SysMenu parentMenu = getById(parentId);
            if (parentMenu == null) {
                return "父菜单不存在";
            }
            if (parentMenu.getType() >= childType) {
                return "父菜单的类型值必须小于子菜单的类型值（目录>菜单>按钮）";
            }
        }
        if (childType > AuthConstant.MENU_TYPE_BUTTON) {
            return "菜单类型值无效";
        }
        return null; // 校验通过
    }

}
