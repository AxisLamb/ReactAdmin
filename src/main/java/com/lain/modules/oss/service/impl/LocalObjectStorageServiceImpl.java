package com.lain.modules.oss.service.impl;

import com.lain.config.oss.model.BucketKey;
import com.lain.config.oss.model.FileUploadRequest;
import com.lain.config.oss.model.FileUploadResult;
import com.lain.config.oss.properties.CustomFileClientProperties;
import com.lain.modules.oss.service.ObjectStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@ConditionalOnProperty(prefix = CustomFileClientProperties.PREFIX, name = "type", havingValue = "LOCAL")
@Component
public class LocalObjectStorageServiceImpl implements ObjectStorageService {

    @Value("${os.file.local.base-path:./uploads}")
    private String basePath;

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(basePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            log.info("Local storage base path initialized: {}", basePath);
        } catch (IOException e) {
            log.error("Failed to initialize local storage path: {}", basePath, e);
            throw new RuntimeException("Failed to initialize local storage", e);
        }
    }

    @Override
    public FileUploadResult uploadFile(FileUploadRequest request) {
        try {
            // 生成文件存储路径
            String bucketName = getBucketName(request);
            String objectName = generateObjectName(request);
            Path bucketPath = Paths.get(basePath, bucketName);
            Path filePath = bucketPath.resolve(objectName);

            // 创建目录
            if (!Files.exists(bucketPath)) {
                Files.createDirectories(bucketPath);
            }

            // 保存文件
            Files.copy(request.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 构造返回结果
            FileUploadResult result = FileUploadResult.builder()
                    .fileId(UUID.randomUUID().toString())
                    .bucketName(bucketName)
                    .objectName(objectName)
                    .filePath(filePath.toString())
                    .originalName(request.getOriginalName())
                    .fileSize(request.getFileSize())
                    .fileType(request.getFileType())
                    .build();

            log.info("File uploaded successfully: {}", filePath);
            return result;
        } catch (IOException e) {
            log.error("Failed to upload file: {}", request.getOriginalName(), e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public void downloadFile(BucketKey key, String objectName, OutputStream outputStream) {
        downloadFile(key.getBucketName(), objectName, outputStream);
    }

    @Override
    public void downloadFile(String bucketName, String objectName, OutputStream outputStream) {
        try {
            Path filePath = Paths.get(basePath, bucketName, objectName);
            if (!Files.exists(filePath)) {
                throw new RuntimeException("File not found: " + objectName);
            }

            Files.copy(filePath, outputStream);
            log.info("File downloaded successfully: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to download file: {}/{}", bucketName, objectName, e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    @Override
    public String getFileUrl(BucketKey key, String objectName) {
        return getFileUrl(key.getBucketName(), objectName);
    }

    @Override
    public String getFileUrl(String bucketName, String objectName) {
        return String.format("/files/%s/%s", bucketName, objectName);
    }

    @Override
    public boolean deleteFile(BucketKey key, String objectName) {
        return deleteFile(key.getBucketName(), objectName);
    }

    @Override
    public boolean deleteFile(String bucketName, String objectName) {
        try {
            Path filePath = Paths.get(basePath, bucketName, objectName);
            boolean deleted = Files.deleteIfExists(filePath);
            log.info("File deleted: {}, result: {}", filePath, deleted);
            return deleted;
        } catch (IOException e) {
            log.error("Failed to delete file: {}/{}", bucketName, objectName, e);
            return false;
        }
    }

    @Override
    public boolean exists(BucketKey key, String objectName) {
        Path filePath = Paths.get(basePath, key.getBucketName(), objectName);
        return Files.exists(filePath);
    }

    @Override
    public boolean createBucket(BucketKey key) {
        try {
            Path bucketPath = Paths.get(basePath, key.getBucketName());
            if (!Files.exists(bucketPath)) {
                Files.createDirectories(bucketPath);
                log.info("Bucket created: {}", key.getBucketName());
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("Failed to create bucket: {}", key.getBucketName(), e);
            return false;
        }
    }

    @Override
    public boolean deleteBucket(BucketKey key) {
        try {
            Path bucketPath = Paths.get(basePath, key.getBucketName());
            if (Files.exists(bucketPath)) {
                Files.delete(bucketPath);
                log.info("Bucket deleted: {}", key.getBucketName());
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("Failed to delete bucket: {}", key.getBucketName(), e);
            return false;
        }
    }

    private String getBucketName(FileUploadRequest request) {
        if (request.getBucketKey() != null) {
            return request.getBucketKey().getBucketName();
        }
        return request.getBucketName();
    }

    private String generateObjectName(FileUploadRequest request) {
        // 生成文件名格式: yyyyMMdd_uuid_originalName
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String originalName = StringUtils.cleanPath(request.getOriginalName());

        // 移除非法字符
        originalName = originalName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");

        return datePath + "_" + uuid + "_" + originalName;
    }
}
