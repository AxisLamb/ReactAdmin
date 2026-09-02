package com.lain.ai.wechat;

import com.lain.ai.tools.FileAssistant;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.gateway.HarnessGateway;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.nio.file.Paths;

/**
 * 微信机器人装配。
 *
 * <p>仅当 {@code wechat.bot.enabled=true} 时生效，避免未配置时影响应用启动。
 *
 * <p>依赖关系：
 * <pre>
 * WeChatBotProperties → WeChatCrypto / WeChatApiClient
 * HarnessAgent ─┐
 * WeChatChannel ─┴→ HarnessGateway（注册渠道并绑定主 Agent）→ WeChatBotService → Controller
 * </pre>
 */
@Configuration
@EnableConfigurationProperties(WeChatBotProperties.class)
@ConditionalOnProperty(prefix = "wechat.bot", name = "enabled", havingValue = "true")
public class WeChatBotConfiguration {

    private static final Logger log = LoggerFactory.getLogger(WeChatBotConfiguration.class);

    @Autowired
    private HarnessGateway weChatGateway;

    @Bean
    public WeChatCrypto weChatCrypto(WeChatBotProperties properties) {
        return new WeChatCrypto(properties.getToken(), properties.getEncodingAesKey(), properties.getAppId());
    }

    @Bean
    public WeChatApiClient weChatApiClient(WeChatBotProperties properties) {
        return new WeChatApiClient(properties);
    }

    @Bean(destroyMethod = "close")
    public HarnessAgent weChatAgent(WeChatBotProperties properties) {
        Toolkit toolkit = new Toolkit();
        if (properties.getAgent().isEnableFileTools()) {
            log.warn("微信机器人已开启文件系统工具，请注意生产环境安全风险");
            toolkit.registerTool(new FileAssistant());
        }
        return HarnessAgent.builder()
                .name(properties.getAgent().getName())
                .sysPrompt(properties.getAgent().getSysPrompt())
                .model(properties.getAgent().getModel())
                .workspace(Paths.get(properties.getAgent().getWorkspace()))
                .toolkit(toolkit)
                .compaction(CompactionConfig.builder()
                        .triggerMessages(properties.getAgent().getCompactionTrigger())
                        .keepMessages(properties.getAgent().getCompactionKeep())
                        .build())
                .build();
    }

    @Bean
    public WeChatChannel weChatChannel(WeChatBotProperties properties, WeChatApiClient apiClient) {
        return new WeChatChannel(properties.getAgent().getName(), apiClient);
    }

    @Bean
    public HarnessGateway weChatGateway(HarnessAgent weChatAgent, WeChatChannel weChatChannel) {
        HarnessGateway gateway = HarnessGateway.create();
        gateway.bindMainAgent(weChatAgent);
        gateway.channelManager().register(weChatChannel);
        gateway.channelManager().initAll(gateway);
        gateway.channelManager().startAll();
        log.info("微信机器人已启动，渠道={}，Agent={}",
                weChatChannel.channelId(), weChatAgent.getName());
        return gateway;
    }

    @Bean(destroyMethod = "close")
    @DependsOn("weChatGateway")
    public WeChatBotService weChatBotService(WeChatBotProperties properties,
                                             WeChatApiClient apiClient,
                                             WeChatChannel weChatChannel,
                                             WeChatCrypto weChatCrypto) {
        return new WeChatBotService(properties, apiClient, weChatChannel, weChatCrypto);
    }

    @Bean
    public WeChatCallbackController weChatCallbackController(WeChatBotProperties properties,
                                                             WeChatBotService service,
                                                             WeChatCrypto weChatCrypto) {
        return new WeChatCallbackController(properties, service, weChatCrypto);
    }

    @PreDestroy
    public void shutdown() {
        if (weChatGateway != null) {
            weChatGateway.channelManager().stopAll();
        }
    }
}
