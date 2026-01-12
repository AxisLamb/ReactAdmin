package com.lain.modules.oss.service.impl;

import com.lain.modules.oss.service.ObjectStorageService;
import com.lain.config.oss.model.BucketKey;
import com.lain.config.oss.model.FileUploadRequest;
import com.lain.config.oss.model.FileUploadResult;
import com.lain.config.oss.properties.CustomFileClientProperties;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = CustomFileClientProperties.PREFIX, name = "type", havingValue = "MINIO")
@Slf4j
public class MinioStorageServiceImpl implements ObjectStorageService {

    @Value("${os.minio.endpoint}")
    private String endpoint;

    @Value("${os.minio.access-key}")
    private String accessKey;

    @Value("${os.minio.secret-key}")
    private String secretKey;

//    @Value("${minio.bucket-prefix:}")
//    private String bucketPrefix;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        this.minioClient = MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build();
    }

    @Override
    public FileUploadResult uploadFile(FileUploadRequest request) {
        String bucketName = getBucketName(request.getBucketKey());
        String objectName = generateObjectName(request.getOriginalName());
        String fileId = UUID.randomUUID().toString().replace("-", "");

        try {
            // 创建存储桶（如果不存在）
            createBucketIfNotExists(request.getBucketKey());

            // 上传文件
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(request.getInputStream(), request.getFileSize(), -1)
                .contentType(request.getFileType())
                .build();

            minioClient.putObject(putObjectArgs);

            // 构建访问路径
            String filePath = endpoint + "/" + bucketName + "/" + objectName;

            return FileUploadResult.builder()
                .fileId(fileId)
                .bucketName(bucketName)
                .objectName(objectName)
                .filePath(filePath)
                .originalName(request.getOriginalName())
                .fileSize(request.getFileSize())
                .fileType(request.getFileType())
                .build();

        } catch (Exception e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public void downloadFile(BucketKey key, String objectName, OutputStream outputStream) {
        String bucketName = getBucketName(key);
        downloadFile(bucketName, objectName, outputStream);
    }

    @Override
    public void downloadFile(String bucketName, String objectName, OutputStream outputStream) {
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            GetObjectResponse response = minioClient.getObject(getObjectArgs);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = response.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }

    @Override
    public String getFileUrl(BucketKey key, String objectName) {
        String bucketName = getBucketName(key);
        return getFileUrl(bucketName, objectName);
    }

    @Override
    public String getFileUrl(String bucketName, String objectName) {
        try {
            GetPresignedObjectUrlArgs urlArgs = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(7, TimeUnit.DAYS) // 7天有效期
                    .build();

            return minioClient.getPresignedObjectUrl(urlArgs);
        } catch (Exception e) {
            throw new RuntimeException("获取文件URL失败", e);
        }
    }

    @Override
    public boolean deleteFile(BucketKey key, String objectName) {
        String bucketName = getBucketName(key);
        return deleteFile(bucketName, objectName);
    }

    @Override
    public boolean deleteFile(String bucketName, String objectName) {
        try {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            minioClient.removeObject(removeObjectArgs);
            return true;
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return false;
        }
    }

    @Override
    public boolean exists(BucketKey key, String objectName) {
        try {
            StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                .bucket(getBucketName(key))
                .object(objectName)
                .build();

            minioClient.statObject(statObjectArgs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean createBucket(BucketKey key) {
        try {
            String fullBucketName = getBucketName(key);
            MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                .bucket(fullBucketName)
                .build();

            minioClient.makeBucket(makeBucketArgs);
            return true;
        } catch (Exception e) {
            log.error("创建存储桶失败", e);
            return false;
        }
    }

    @Override
    public boolean deleteBucket(BucketKey key) {
        try {
            String fullBucketName = getBucketName(key);
            RemoveBucketArgs removeBucketArgs = RemoveBucketArgs.builder()
                .bucket(fullBucketName)
                .build();

            minioClient.removeBucket(removeBucketArgs);
            return true;
        } catch (Exception e) {
            log.error("删除存储桶失败", e);
            return false;
        }
    }

    private String getBucketName(BucketKey key) {
        return key.getKey();
    }

    private String generateObjectName(String originalName) {
        String ext = "";
        if (originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        return System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + ext;
    }

    private void createBucketIfNotExists(BucketKey key) throws Exception {
        BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder()
            .bucket(key.getKey())
            .build();

        if (!minioClient.bucketExists(bucketExistsArgs)) {
            createBucket(key);
        }
    }
}

