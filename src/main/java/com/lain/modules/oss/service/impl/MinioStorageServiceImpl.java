package com.lain.modules.oss.service.impl;

import cn.hutool.core.util.StrUtil;
import com.lain.common.exception.LainException;
import com.lain.config.oss.model.FileUploadRequest;
import com.lain.config.oss.model.FileUploadResult;
import com.lain.config.oss.properties.FileClientType;
import com.lain.config.oss.properties.MinioProperties;
import com.lain.modules.oss.service.ObjectStorageService;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 对象存储服务实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "os.file", name = "type", havingValue = "MINIO")
public class MinioStorageServiceImpl implements ObjectStorageService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

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
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(request.getInputStream(), request.getFileSize(), -1)
                    .contentType(request.getFileType())
                    .build());
        } catch (Exception e) {
            log.error("MinIO文件上传失败", e);
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
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("MinIO文件下载失败", e);
            throw new LainException("文件下载失败");
        }
    }

    @Override
    public void downloadFile(String bucketName, String objectName, OutputStream outputStream) {
        try (InputStream inputStream = downloadFile(bucketName, objectName)) {
            inputStream.transferTo(outputStream);
        } catch (Exception e) {
            log.error("MinIO文件下载失败", e);
            throw new LainException("文件下载失败");
        }
    }

    @Override
    public String getFileUrl(String bucketName, String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(properties.getUrlExpiry(), TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            log.error("MinIO获取文件URL失败", e);
            throw new LainException("获取文件URL失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            log.error("MinIO文件删除失败", e);
            return false;
        }
    }

    @Override
    public boolean exists(String bucketName, String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("MinIO创建存储桶失败", e);
            throw new LainException("创建存储桶失败");
        }
    }

    @Override
    public void deleteBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("MinIO删除存储桶失败", e);
            throw new LainException("删除存储桶失败");
        }
    }

    private void createBucketIfNotExists(String bucketName) {
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            boolean exists = buckets.stream()
                    .anyMatch(bucket -> bucket.name().equals(bucketName));
            if (!exists) {
                createBucket(bucketName);
            }
        } catch (Exception e) {
            log.error("MinIO检查存储桶失败", e);
            throw new LainException("检查存储桶失败");
        }
    }

    @Override
    public String toString() {
        return FileClientType.MINIO.name();
    }
}
