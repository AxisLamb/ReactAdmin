package com.lain.config.auth;

public class AuthConstant {

    public static final String AUTH_TOKEN_HEADER = "satoken";

    public static final String TOKEN_SESSION_KEY_USER_NAME = "TOKEN_SESSION_KEY_USER_NAME";
    public static final String TOKEN_SESSION_KEY_USER_ID = "TOKEN_SESSION_KEY_USER_ID";

    // 菜单类型常量 sys_menu type
    public static final Integer MENU_TYPE_DIR = 0;  // 菜单
    public static final Integer MENU_TYPE_MENU = 1; // 页面
    public static final Integer MENU_TYPE_BUTTON = 2; // 按钮
    public static final Integer MENU_TYPE_INF_DIR = 3;  // 菜单
    public static final Integer MENU_TYPE_BUS = 4; // 页面
    public static final Integer MENU_TYPE_INF = 5; // 按钮

    // 外链菜单标记 sys_menu.perms，perms=outer 表示该菜单为外部网站链接
    public static final String MENU_PERMS_OUTER = "outer";

}
