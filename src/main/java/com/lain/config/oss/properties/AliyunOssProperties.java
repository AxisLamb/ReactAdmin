package com.lain.config.oss.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云 OSS 连接配置
 */
@Data
@ConfigurationProperties(prefix = AliyunOssProperties.PREFIX)
public class AliyunOssProperties {

    public static final String PREFIX = "os.file.aliyun.oss";

    /** OSS 服务端点 */
    private String endpoint;

    /** AccessKey ID */
    private String accessKeyId;

    /** AccessKey Secret */
    private String accessKeySecret;

    /** 预签名URL有效期(秒) */
    private Integer urlExpiry = 3600;
}
