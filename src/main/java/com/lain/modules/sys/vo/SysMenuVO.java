package com.lain.modules.sys.vo;

import lombok.Data;

@Data
public class SysMenuVO {
    private Long menuId;
    private Long parentId;
    private String name;
    private String reactComponent;
    private String url;
    private String perms;
    private Integer type;
    private String icon;
    private Integer orderNum;

    // 添加用于前端路由的字段
    private String path;
    private String element;
    private Boolean protectedRoute;
    private Boolean exact;
}