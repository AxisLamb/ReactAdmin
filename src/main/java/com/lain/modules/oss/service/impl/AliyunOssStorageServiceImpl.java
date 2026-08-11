package com.lain.modules.oss.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.lain.common.exception.LainException;
import com.lain.config.oss.model.FileUploadRequest;
import com.lain.config.oss.model.FileUploadResult;
import com.lain.config.oss.properties.AliyunOssProperties;
import com.lain.config.oss.properties.FileClientType;
import com.lain.modules.oss.service.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

/**
 * 阿里云 OSS 对象存储服务实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "os.file", name = "type", havingValue = "ALIYUN")
public class AliyunOssStorageServiceImpl implements ObjectStorageService {

    private final OSS ossClient;
    private final AliyunOssProperties properties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadResult uploadFile(FileUploadRequest request) {
        String bucketName = request.getBucketName();
        String objectName = request.getObjectName();
        if (StrUtil.isBlank(bucketName) || StrUtil.isBlank(objectName)) {
            throw new LainException("存储位置未解析，请检查 StoragePathStrategy 配置");
        }

        createBucketIfNotExists(bucketName);

        try {
            ossClient.putObject(bucketName, objectName, request.getInputStream());
        } catch (Exception e) {
            log.error("阿里云OSS文件上传失败", e);
            throw new LainException("文件上传失败");
        }

        return FileUploadResult.builder()
                .fileId(UUID.randomUUID().toString().replace("-", ""))
                .originalName(request.getOriginalName())
                .fileSize(request.getFileSize())
                .fileType(request.getFileType())
                .bucketName(bucketName)
                .objectName(objectName)
                .filePath(getFileUrl(bucketName, objectName))
                .build();
    }

    @Override
    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            OSSObject ossObject = ossClient.getObject(bucketName, objectName);
            return ossObject.getObjectContent();
        } catch (Exception e) {
            log.error("阿里云OSS文件下载失败", e);
            throw new LainException("文件下载失败");
        }
    }

    @Override
    public void downloadFile(String bucketName, String objectName, OutputStream outputStream) {
        try (InputStream inputStream = downloadFile(bucketName, objectName)) {
            inputStream.transferTo(outputStream);
        } catch (Exception e) {
            log.error("阿里云OSS文件下载失败", e);
            throw new LainException("文件下载失败");
        }
    }

    @Override
    public String getFileUrl(String bucketName, String objectName) {
        try {
            Date expiration = new Date(System.currentTimeMillis() + properties.getUrlExpiry() * 1000L);
            URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
            return url.toString();
        } catch (Exception e) {
            log.error("阿里云OSS获取文件URL失败", e);
            throw new LainException("获取文件URL失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFile(String bucketName, String objectName) {
        try {
            ossClient.deleteObject(bucketName, objectName);
            return true;
        } catch (Exception e) {
            log.error("阿里云OSS文件删除失败", e);
            return false;
        }
    }

    @Override
    public boolean exists(String bucketName, String objectName) {
        try {
            return ossClient.doesObjectExist(bucketName, objectName);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            ossClient.createBucket(bucketName);
        } catch (Exception e) {
            log.error("阿里云OSS创建存储桶失败", e);
            throw new LainException("创建存储桶失败");
        }
    }

    @Override
    public void deleteBucket(String bucketName) {
        try {
            ossClient.deleteBucket(bucketName);
        } catch (Exception e) {
            log.error("阿里云OSS删除存储桶失败", e);
            throw new LainException("删除存储桶失败");
        }
    }

    private void createBucketIfNotExists(String bucketName) {
        try {
            if (!ossClient.doesBucketExist(bucketName)) {
                createBucket(bucketName);
            }
        } catch (Exception e) {
            log.error("阿里云OSS检查存储桶失败", e);
            throw new LainException("检查存储桶失败");
        }
    }

    @Override
    public String toString() {
        return FileClientType.ALIYUN.name();
    }
}
