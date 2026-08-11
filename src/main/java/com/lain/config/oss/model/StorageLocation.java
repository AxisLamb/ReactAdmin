package com.lain.config.oss.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 文件存储位置（不可变值对象）：存储桶 + 对象名
 * <p>
 * 替代原 BucketKey：统一描述"文件最终落在哪个桶、哪个路径"，
 * 由 {@link com.lain.config.oss.strategy.StoragePathStrategy} 统一生成，
 * 各存储实现不再各自拼接路径。
 */
@Getter
@ToString
@EqualsAndHashCode
public class StorageLocation {

    /** 存储桶名称 */
    private final String bucketName;

    /** 对象名（含路径前缀，如 avatar/202608/9f8e7d6c5b4a.png） */
    private final String objectName;

    private StorageLocation(String bucketName, String objectName) {
        this.bucketName = bucketName;
        this.objectName = objectName;
    }

    public static StorageLocation of(String bucketName, String objectName) {
        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalArgumentException("bucketName 不能为空");
        }
        if (objectName == null || objectName.isBlank()) {
            throw new IllegalArgumentException("objectName 不能为空");
        }
        return new StorageLocation(bucketName.trim(), objectName.trim());
    }
}
