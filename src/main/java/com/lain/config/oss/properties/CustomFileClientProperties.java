package com.lain.config.oss.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 缓存配置
 *
 *
 * @date 2019/08/06
 */
@Data
@ConfigurationProperties(prefix = CustomFileClientProperties.PREFIX)
public class CustomFileClientProperties {
    public static final String PREFIX = "os.file.client";

    /**
     * 缓存类型 default: MINIO
     */
    private FileClientType type = FileClientType.MINIO;

}
