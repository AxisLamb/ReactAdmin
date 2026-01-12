package com.lain.config.oss.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.time.Duration;

/**
 * 存储桶缓存 key 封装
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BucketKey {
    /**
     * redis key
     */
    @NonNull
    private String key;

    /**
     * 超时时间
     */
    private Duration expire;

    /**
     * 存储桶名称
     */
    @NonNull
    private String bucketName;

    /**
     * 对象名称（可选）
     */
    private String objectName;

    public BucketKey(@NonNull String key, @NonNull String bucketName) {
        this.key = key;
        this.bucketName = bucketName;
    }

    @Override
    public String toString() {
        return "BucketKey{" +
                "key='" + key + '\'' +
                ", expire=" + expire +
                ", bucketName='" + bucketName + '\'' +
                ", objectName='" + objectName + '\'' +
                '}';
    }
}
