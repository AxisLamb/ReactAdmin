package com.lain.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lain.modules.sys.entity.SysMenu;
import com.lain.modules.sys.vo.RouteConfig;
import com.lain.modules.sys.vo.SysMenuVO;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    /**
     * 获取所有菜单ID
     * @return
     */
    List<Long> findAllMenuIdsByUserId();

    /**
     * 获取所有菜单
     * @return
     */
    List<SysMenuVO> listMenus(boolean b);

    /**
     * 添加菜单
     * @param sysMenuVO
     * @return
     */
    String addMenu(SysMenuVO sysMenuVO);

    /**
     * 更新菜单
     * @param sysMenuVO
     * @return
     */
    String updateMenu(SysMenuVO sysMenuVO);

    /**
     * 删除菜单
     * @param menuId
     * @return
     */
    String deleteMenu(Long menuId);

    /**
     * 获取所有菜单路由
     * @return
     */
    List<RouteConfig> getReactRoutes();
}
