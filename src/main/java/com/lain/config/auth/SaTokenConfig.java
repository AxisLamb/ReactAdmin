package com.lain.config.auth;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.lain.config.oss.properties.LocalObjectStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties({
    LocalObjectStorageProperties.class
})
public class SaTokenConfig implements WebMvcConfigurer {

    @Value("${file.base-path}")
    private String basePath;

    @Autowired
    private AuthWhitelistConfig authWhitelistConfig;

    // 注册Sa-Token的注解拦截器，打开注解式鉴权功能
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 注册注解拦截器，并排除不需要注解鉴权的接口地址 (与登录拦截器无关)
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 登录校验
                    StpUtil.checkLogin();
                }))
                .addPathPatterns("/**")
                .excludePathPatterns(authWhitelistConfig.getWhitelistPaths());

        registry.addInterceptor(new UploadAuthInterceptor())
                .addPathPatterns("/file/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /files/** 映射到 ./uploads/
        registry.addResourceHandler("/file/**")
                .addResourceLocations("file:" + basePath);
    }

}