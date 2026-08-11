package com.lain.config.oss.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO 连接配置
 */
@Data
@ConfigurationProperties(prefix = MinioProperties.PREFIX)
public class MinioProperties {

    public static final String PREFIX = "os.file.minio";

    /** MinIO 服务端点，如 http://127.0.0.1:9000 */
    private String endpoint;

    /** 访问密钥 */
    private String accessKey;

    /** 私密密钥 */
    private String secretKey;

    /** 预签名URL有效期(秒) */
    private Integer urlExpiry = 3600;
}
