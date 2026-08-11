package com.lain.modules.oss.service.impl;

import cn.hutool.core.util.StrUtil;
import com.lain.common.exception.LainException;
import com.lain.config.oss.model.FileUploadRequest;
import com.lain.config.oss.model.FileUploadResult;
import com.lain.config.oss.properties.FileClientType;
import com.lain.config.oss.properties.LocalObjectStorageProperties;
import com.lain.modules.oss.service.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 本地文件存储服务实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "os.file", name = "type", havingValue = "LOCAL")
public class LocalObjectStorageServiceImpl implements ObjectStorageService {

    private final LocalObjectStorageProperties properties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadResult uploadFile(FileUploadRequest request) {
        String bucketName = request.getBucketName();
        String objectName = request.getObjectName();
        if (StrUtil.isBlank(bucketName) || StrUtil.isBlank(objectName)) {
            throw new LainException("存储位置未解析，请检查 StoragePathStrategy 配置");
        }

        // 对象名可能包含多级路径，需确保父目录全部存在
        Path filePath = Paths.get(properties.getBasePath(), bucketName, objectName);
        try {
            Files.createDirectories(filePath.getParent());
            Files.copy(request.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("本地文件上传失败", e);
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
            Path filePath = Paths.get(properties.getBasePath(), bucketName, objectName);
            if (!Files.exists(filePath)) {
                throw new LainException("文件不存在");
            }
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            log.error("本地文件下载失败", e);
            throw new LainException("文件下载失败");
        }
    }

    @Override
    public void downloadFile(String bucketName, String objectName, OutputStream outputStream) {
        try (InputStream inputStream = downloadFile(bucketName, objectName)) {
            inputStream.transferTo(outputStream);
        } catch (Exception e) {
            log.error("本地文件下载失败", e);
            throw new LainException("文件下载失败");
        }
    }

    @Override
    public String getFileUrl(String bucketName, String objectName) {
        return "/uploads/" + bucketName + "/" + objectName;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFile(String bucketName, String objectName) {
        try {
            Path filePath = Paths.get(properties.getBasePath(), bucketName, objectName);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("本地文件删除失败", e);
            return false;
        }
    }

    @Override
    public boolean exists(String bucketName, String objectName) {
        Path filePath = Paths.get(properties.getBasePath(), bucketName, objectName);
        return Files.exists(filePath);
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            Path bucketPath = Paths.get(properties.getBasePath(), bucketName);
            Files.createDirectories(bucketPath);
        } catch (IOException e) {
            log.error("本地存储创建目录失败", e);
            throw new LainException("创建存储目录失败");
        }
    }

    @Override
    public void deleteBucket(String bucketName) {
        try {
            Path bucketPath = Paths.get(properties.getBasePath(), bucketName);
            if (Files.exists(bucketPath)) {
                Files.walk(bucketPath)
                        .sorted((a, b) -> b.compareTo(a))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.warn("删除文件失败: {}", path, e);
                            }
                        });
            }
        } catch (IOException e) {
            log.error("本地存储删除目录失败", e);
            throw new LainException("删除存储目录失败");
        }
    }

    @Override
    public String toString() {
        return FileClientType.LOCAL.name();
    }
}
