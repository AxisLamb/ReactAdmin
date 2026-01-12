package com.lain.modules.oss.service.impl;

import com.aliyun.core.utils.IOUtils;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.lain.modules.oss.service.ObjectStorageService;
import com.lain.config.oss.model.BucketKey;
import com.lain.config.oss.model.FileUploadRequest;
import com.lain.config.oss.model.FileUploadResult;
import com.lain.config.oss.properties.CustomFileClientProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = CustomFileClientProperties.PREFIX, name = "type", havingValue = "ALIYUN")
@Slf4j
public class AliyunOssStorageServiceImpl implements ObjectStorageService {

    @Value("${os.aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${os.aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${os.aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${os.aliyun.oss.bucket-prefix:}")
    private String bucketPrefix;

    private OSS ossClient;

    @PostConstruct
    public void init() {
        this.ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
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
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, request.getInputStream());
            ossClient.putObject(putObjectRequest);

            // 构建访问路径
            String filePath = "https://" + bucketName + "." + endpoint + "/" + objectName;

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
            OSSObject ossObject = ossClient.getObject(bucketName, objectName);
            IOUtils.copy(ossObject.getObjectContent(), outputStream);
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
            Date expiration = new Date(new Date().getTime() + 3600 * 1000 * 24 * 7); // 7天后过期
            URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
            return url.toString();
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
            ossClient.deleteObject(bucketName, objectName);
            return true;
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return false;
        }
    }

    @Override
    public boolean exists(BucketKey key, String objectName) {
        try {
            return ossClient.doesObjectExist(getBucketName(key), objectName);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean createBucket(BucketKey key) {
        try {
            String fullBucketName = getBucketName(key);
            if (!ossClient.doesBucketExist(fullBucketName)) {
                ossClient.createBucket(fullBucketName);
            }
            return true;
        } catch (Exception e) {
            log.error("创建存储桶失败", e);
            return false;
        }
    }

    @Override
    public boolean deleteBucket(BucketKey key) {
        try {
            ossClient.deleteBucket(getBucketName(key));
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

    private void createBucketIfNotExists(BucketKey key) {
        if (!ossClient.doesBucketExist(key.getKey())) {
            createBucket(key);
        }
    }
}
