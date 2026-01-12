package com.lain.common.cache;

import com.lain.config.cache.vo.CacheHashKey;
import com.lain.config.cache.vo.CacheKeyBuilder;

public class TestCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheHashKey build() {
        return new TestCacheKeyBuilder().hashKey();
    }

    @Override
    public String getPrefix() {
        return "OS";
    }

    @Override
    public String getModular() {
        return "admin";
    }

    @Override
    public String getTable() { return "token";}

    @Override
    public ValueType getValueType() {
        return null;
    }

}
