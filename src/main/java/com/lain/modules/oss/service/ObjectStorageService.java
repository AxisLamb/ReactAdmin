package com.lain.modules.oss.service;

import com.lain.config.oss.model.FileUploadRequest;
import com.lain.config.oss.model.FileUploadResult;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 对象存储服务接口：MinIO / 阿里云 OSS / 本地存储均实现此接口
 * <p>
 * 存储位置（桶 + 对象路径）由 StoragePathStrategy 在上传前解析并写入请求，
 * 各实现只负责按 request 中给定的 bucketName/objectName 执行读写。
 */
public interface ObjectStorageService {

    /**
     * 上传文件（request 中 bucketName/objectName 必须已由 StoragePathStrategy 解析）
     */
    FileUploadResult uploadFile(FileUploadRequest request);

    /**
     * 下载文件，返回输入流
     */
    InputStream downloadFile(String bucketName, String objectName);

    /**
     * 下载文件，直接写入输出流
     */
    void downloadFile(String bucketName, String objectName, OutputStream outputStream);

    /**
     * 获取文件访问URL
     */
    String getFileUrl(String bucketName, String objectName);

    /**
     * 删除文件
     */
    boolean deleteFile(String bucketName, String objectName);

    /**
     * 判断文件是否存在
     */
    boolean exists(String bucketName, String objectName);

    /**
     * 创建存储桶
     */
    void createBucket(String bucketName);

    /**
     * 删除存储桶
     */
    void deleteBucket(String bucketName);
}
