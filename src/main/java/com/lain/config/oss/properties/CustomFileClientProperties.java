package com.lain.config.oss.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件客户端配置：通过 os.file.type 选择启用的存储实现（MINIO/ALIYUN/LOCAL）
 */
@Data
@ConfigurationProperties(prefix = CustomFileClientProperties.PREFIX)
public class CustomFileClientProperties {
    public static final String PREFIX = "os.file";

    /**
     * 文件客户端类型 default: MINIO
     */
    private FileClientType type = FileClientType.MINIO;

}
