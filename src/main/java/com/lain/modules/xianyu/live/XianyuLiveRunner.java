package com.lain.modules.xianyu.live;

import com.lain.modules.xianyu.config.XianyuProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 闲鱼机器人启动器（Spring Boot 启动后自动运行，由 xianyu.enabled 开关控制）
 */
@Component
public class XianyuLiveRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(XianyuLiveRunner.class);

    private final XianyuLive xianyuLive;
    private final XianyuProperties properties;

    public XianyuLiveRunner(XianyuLive xianyuLive, XianyuProperties properties) {
        this.xianyuLive = xianyuLive;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            log.info("闲鱼机器人未启用（xianyu.enabled=false），跳过启动");
            return;
        }
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            log.warn("闲鱼机器人未配置 API_KEY，跳过启动");
            return;
        }
        if (properties.getCookiesStr() == null || properties.getCookiesStr().isBlank()) {
            log.warn("闲鱼机器人未配置 COOKIES_STR，跳过启动");
            return;
        }
        xianyuLive.start();
    }
}
