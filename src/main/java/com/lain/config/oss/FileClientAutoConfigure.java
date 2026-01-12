package com.lain.config.oss;


import com.lain.modules.oss.service.impl.AliyunOssStorageServiceImpl;
import com.lain.modules.oss.service.impl.LocalObjectStorageServiceImpl;
import com.lain.modules.oss.service.impl.MinioStorageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Slf4j
@EnableCaching
@Import({
        MinioStorageServiceImpl.class, AliyunOssStorageServiceImpl.class, LocalObjectStorageServiceImpl.class
})
@Component
public class FileClientAutoConfigure {
}
