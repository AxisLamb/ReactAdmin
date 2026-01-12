package com.lain.modules.sys.vo;

import lombok.Data;
import java.util.List;

/**
 * 前端路由配置类
 */
@Data
public class RouteConfig {
    /**
     * 路由路径
     */
    private String path;

    /**
     * 对应的React组件
     */
    private String element;

    /**
     * 是否精确匹配
     */
    private Boolean exact;

    /**
     * 是否受保护（需要登录）
     */
    private Boolean protectedRoute;

    /**
     * 子路由
     */
    private List<RouteConfig> children;

    // 构造函数
    public RouteConfig() {}

    public RouteConfig(String path, String element) {
        this.path = path;
        this.element = element;
    }

    public RouteConfig(String path, String element, Boolean exact) {
        this.path = path;
        this.element = element;
        this.exact = exact;
    }
}
