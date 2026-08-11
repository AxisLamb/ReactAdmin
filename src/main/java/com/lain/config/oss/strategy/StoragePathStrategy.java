package com.lain.config.oss.strategy;

import com.lain.config.oss.model.FileUploadRequest;
import com.lain.config.oss.model.StorageLocation;

/**
 * 存储路径策略：决定上传文件的存储桶与对象路径
 * <p>
 * 替代原 BucketKeyBuilder。业务方如需自定义存储规则，
 * 只需实现本接口并注册为 Bean，默认策略会自动让位。
 */
public interface StoragePathStrategy {

    /**
     * 根据上传请求解析存储位置
     *
     * @param request 上传请求（含 serviceModule/businessType/originalName 等上下文）
     * @return 存储位置（桶 + 对象名）
     */
    StorageLocation resolve(FileUploadRequest request);
}
