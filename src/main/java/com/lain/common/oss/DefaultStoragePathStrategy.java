package com.lain.common.oss;

import com.lain.config.oss.model.FileUploadRequest;
import com.lain.config.oss.model.StorageLocation;
import com.lain.config.oss.strategy.StoragePathStrategy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 默认存储路径策略
 * <p>
 * 存储桶：按服务模块隔离，{bucketPrefix}-{serviceModule}，如 lain-sys
 * 对象路径：{businessType}/{yyyyMM}/{uuid}_{businessId}.{ext}，如 avatar/202608/9f8e7d6c5b4a3f2e_123.png
 * <p>
 * 桶与对象路径天然按 模块/业务类型 归组，与 file_biz_ref 中间表的关联维度保持一致，
 * 便于按模块、按业务做存储治理与清理。
 */
public class DefaultStoragePathStrategy implements StoragePathStrategy {

    private static final DateTimeFormatter MONTH_DIR = DateTimeFormatter.ofPattern("yyyyMM");

    /** 桶名前缀，最终桶名为 {prefix}-{serviceModule} */
    private final String bucketPrefix;

    public DefaultStoragePathStrategy(String bucketPrefix) {
        this.bucketPrefix = (bucketPrefix == null || bucketPrefix.isBlank()) ? "lain" : bucketPrefix.trim();
    }

    @Override
    public StorageLocation resolve(FileUploadRequest request) {
        // 上传时已显式指定桶名和对象名时直接沿用
        if (hasText(request.getBucketName()) && hasText(request.getObjectName())) {
            return StorageLocation.of(request.getBucketName(), request.getObjectName());
        }

        String module = normalize(request.getServiceModule(), "common");
        String businessType = normalize(request.getBusinessType(), "default");

        String bucketName = (bucketPrefix + "-" + module).toLowerCase();
        String objectName = String.join("/",
                businessType,
                LocalDate.now().format(MONTH_DIR),
                UUID.randomUUID().toString().replace("-", "") + "_" + request.getBusinessId() + extractExtension(request.getOriginalName()));

        return StorageLocation.of(bucketName, objectName);
    }

    /**
     * 将模块/业务类型规范为合法的存储路径片段：仅保留小写字母、数字、短横线、下划线
     */
    private static String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "-");
    }

    private static String extractExtension(String originalName) {
        if (originalName == null) {
            return "";
        }
        int idx = originalName.lastIndexOf('.');
        return idx > -1 ? originalName.substring(idx).toLowerCase() : "";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
