package com.lain.config.oss.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 本地文件存储配置
 */
@Data
@ConfigurationProperties(prefix = LocalObjectStorageProperties.PREFIX)
public class LocalObjectStorageProperties {

    public static final String PREFIX = "os.file.local";

    /** 本地存储根目录 */
    private String basePath = "./uploads";
}
