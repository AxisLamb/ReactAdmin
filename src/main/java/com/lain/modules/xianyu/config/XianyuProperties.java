package com.lain.modules.xianyu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 闲鱼自动值守机器人配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "xianyu")
public class XianyuProperties {

    /** 是否随应用启动自动运行 */
    private boolean enabled = true;

    /** 模型 API Key */
    private String apiKey;

    /** 闲鱼网页端 Cookie 字符串 */
    private String cookiesStr;

    /** 模型服务地址（默认通义千问 OpenAI 兼容接口） */
    private String modelBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /** 模型名称 */
    private String modelName = "qwen-max";

    /** 提示词目录，默认读取 classpath:prompts/，可指定外部目录覆盖 */
    private String promptDir;

    /** 心跳间隔（秒） */
    private int heartbeatInterval = 15;

    /** 心跳超时（秒） */
    private int heartbeatTimeout = 5;

    /** Token 刷新间隔（秒） */
    private int tokenRefreshInterval = 3600;

    /** Token 刷新失败重试间隔（秒） */
    private int tokenRetryInterval = 300;

    /** 人工接管超时时间（秒），超时自动恢复自动回复 */
    private int manualModeTimeout = 3600;

    /** 消息过期时间（毫秒），过滤超时消息 */
    private long messageExpireTime = 300000;

    /** 接管模式切换关键词（卖家发送该词切换人工/AI接管） */
    private String toggleKeywords = "。";

    /** 模拟人工输入延迟 */
    private boolean simulateHumanTyping = false;

    /** 每个会话保留的最大消息数 */
    private int maxHistory = 100;
}
