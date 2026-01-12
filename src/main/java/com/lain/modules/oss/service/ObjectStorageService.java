package com.lain.modules.oss.service;


import com.lain.config.oss.model.BucketKey;
import com.lain.config.oss.model.FileUploadRequest;
import com.lain.config.oss.model.FileUploadResult;

import java.io.OutputStream;

/**
 * OSS Service
 * 
 */
public interface ObjectStorageService {

    /**
     * 上传文件
     */
    FileUploadResult uploadFile(FileUploadRequest request);

    /**
     * 下载文件
     */
    void downloadFile(BucketKey key, String objectName, OutputStream outputStream);
    void downloadFile(String bucketName, String objectName, OutputStream outputStream);

    /**
     * 获取文件访问URL
     */
    String getFileUrl(BucketKey key, String objectName);
    String getFileUrl(String bucketName, String objectName);

    /**
     * 删除文件
     */
    boolean deleteFile(BucketKey key, String objectName);
    boolean deleteFile(String bucketName, String objectName);

    /**
     * 检查文件是否存在
     */
    boolean exists(BucketKey key, String objectName);

    /**
     * 创建存储桶
     */
    boolean createBucket(BucketKey key);

    /**
     * 删除存储桶
     */
    boolean deleteBucket(BucketKey key);

}
