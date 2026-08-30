package com.lain.modules.xianyu.agent;

import java.util.List;

/**
 * 安全过滤接口（对应 Python 版 _safe_filter）
 */
@FunctionalInterface
public interface SafetyFilter {

    /**
     * 过滤回复内容中的违规信息
     */
    String apply(String text);
}
