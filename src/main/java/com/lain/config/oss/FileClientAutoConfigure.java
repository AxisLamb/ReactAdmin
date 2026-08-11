package com.lain.config.oss;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.lain.common.oss.DefaultStoragePathStrategy;
import com.lain.config.oss.properties.AliyunOssProperties;
import com.lain.config.oss.properties.CustomFileClientProperties;
import com.lain.config.oss.properties.LocalObjectStorageProperties;
import com.lain.config.oss.properties.MinioProperties;
import com.lain.config.oss.strategy.StoragePathStrategy;
import com.lain.modules.oss.service.impl.AliyunOssStorageServiceImpl;
import com.lain.modules.oss.service.impl.LocalObjectStorageServiceImpl;
import com.lain.modules.oss.service.impl.MinioStorageServiceImpl;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Slf4j
@EnableCaching
@EnableConfigurationProperties({
        CustomFileClientProperties.class, MinioProperties.class,
        AliyunOssProperties.class, LocalObjectStorageProperties.class
})
@Import({
        MinioStorageServiceImpl.class, AliyunOssStorageServiceImpl.class, LocalObjectStorageServiceImpl.class
})
@Component
public class FileClientAutoConfigure {

    /**
     * 默认存储路径策略：桶名 {os.file.bucket-prefix}-{serviceModule}，
     * 对象路径 {businessType}/{yyyyMM}/{uuid}.{ext}。
     * 业务方注册自定义 StoragePathStrategy Bean 时自动让位。
     */
    @Bean
    @ConditionalOnMissingBean(StoragePathStrategy.class)
    public StoragePathStrategy storagePathStrategy(@Value("${os.file.bucket-prefix:lain}") String bucketPrefix) {
        return new DefaultStoragePathStrategy(bucketPrefix);
    }

    /**
     * MinIO 客户端，仅在 os.file.type=MINIO 时创建
     */
    @Bean
    @ConditionalOnProperty(prefix = CustomFileClientProperties.PREFIX, name = "type", havingValue = "MINIO")
    public MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

    /**
     * 阿里云 OSS 客户端，仅在 os.file.type=ALIYUN 时创建
     */
    @Bean
    @ConditionalOnProperty(prefix = CustomFileClientProperties.PREFIX, name = "type", havingValue = "ALIYUN")
    public OSS ossClient(AliyunOssProperties properties) {
        return new OSSClientBuilder().build(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret());
    }
}
