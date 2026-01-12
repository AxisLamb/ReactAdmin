package com.lain.config.cache.vo;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.lang.NonNull;

import java.time.Duration;


/**
 * hash 缓存 key 封装
 *
 *
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CacheHashKey extends CacheKey {
    /**
     * redis hash field
     */
    private Object field;

    public CacheHashKey(@NonNull String key, final Object field) {
        super(key);
        this.field = field;
    }

    public CacheHashKey(@NonNull String key, final Object field, Duration expire) {
        super(key, expire);
        this.field = field;
    }

    public CacheKey tran() {
        return new CacheKey(StrUtil.join(StrPool.COLON, getKey(), getField()), getExpire());
    }
}
