package com.lain.config.oss.properties;

/**
 * 缓存类型
 *
 *
 * @date 2020/9/22 3:34 下午
 */
public enum FileClientType {
    /**
     * Aliyun OSS
     */
    ALIYUN,
    /**
     * Azure OSS
     */
    AZURE,
    /**
     * redis
     */
    MINIO;

    public boolean eq(FileClientType fileClientType) {
        return fileClientType != null && this.name().equals(fileClientType.name());
    }
}
