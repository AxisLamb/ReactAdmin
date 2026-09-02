package com.lain.ai.wechat;

import lombok.Data;

/**
 * 微信机器人配置。
 *
 * <p>对应配置文件前缀 {@code wechat.bot}，示例：
 * <pre>
 * wechat:
 *   bot:
 *     enabled: true
 *     channel-type: wecom        # wecom=企业微信自建应用；official-account=微信公众号
 *     app-id: ww1234567890       # 企业微信填 CorpID；公众号填 AppID
 *     secret: xxxxxx             # 企业微信填应用 Secret；公众号填 AppSecret
 *     token: xxxxxx              # 回调 Token
 *     encoding-aes-key: xxxxxx   # 回调 EncodingAESKey（43 位）
 *     agent-id: 1000002          # 仅企业微信需要
 * </pre>
 */
@Data
public class WeChatBotProperties {

    /** 是否启用微信机器人，关闭时不会创建任何相关 Bean */
    private boolean enabled = false;

    /**
     * 渠道类型。
     * <ul>
     *   <li>{@link ChannelType#WECOM} 企业微信自建应用（推荐，个人可注册，收发消息能力完整）</li>
     *   <li>{@link ChannelType#OFFICIAL_ACCOUNT} 微信公众号（被动回复免费，主动客服消息需要已认证）</li>
     * </ul>
     */
    private ChannelType channelType = ChannelType.WECOM;

    /** 企业微信：CorpID；公众号：AppID */
    private String appId;

    /** 企业微信：自建应用 Secret；公众号：AppSecret */
    private String secret;

    /** 回调配置页面填写的 Token */
    private String token;

    /** 回调配置页面填写的 EncodingAESKey，43 位字符 */
    private String encodingAesKey;

    /** 企业微信自建应用的 AgentId，公众号无需填写 */
    private String agentId;

    /** 对外暴露的回调路径，需与微信后台填写的 URL 保持一致 */
    private String callbackPath = "/wechat/callback";

    /** 是否使用安全模式（加密模式）。明文模式请置为 false */
    private boolean encryptMode = true;

    /** 用户关注公众号 / 进入应用时的欢迎语 */
    private String welcomeMessage = "你好，我是 AI 助手，有什么可以帮你的？";

    /**
     * 同步回复等待上限（毫秒）。
     *
     * <p>微信要求回调在 5 秒内响应。在此时间内 Agent 若已给出结果，则直接用被动回复返回；
     * 超时则先响应空串结束本次回调，Agent 完成后再通过接口主动推送消息。
     */
    private long syncReplyTimeoutMs = 4500L;

    /** 单条回复最大字节数，微信文本消息上限为 2048 字节 */
    private int maxReplyBytes = 1800;

    /** Agent 相关配置 */
    private AgentProperties agent = new AgentProperties();

    public enum ChannelType {
        /** 企业微信自建应用 */
        WECOM,
        /** 微信公众号 */
        OFFICIAL_ACCOUNT
    }

    @Data
    public static class AgentProperties {

        /** Agent 名称，同时作为 Gateway 中的 agentId */
        private String name = "wechat-bot";

        /**
         * 模型标识，字符串形式由 AgentScope ModelRegistry 解析，
         * 默认 dashscope 会自动读取环境变量 DASHSCOPE_API_KEY。
         */
        private String model = "dashscope:qwen3.7-plus";

        /** 系统提示词 */
        private String sysPrompt = "你是接入微信的 AI 助手。请用简洁、自然的中文回复用户，不要输出 Markdown 语法，"
                + "不要编造事实。回答尽量控制在 500 字以内，方便在聊天窗口阅读。";

        /** AgentScope 工作目录，用于存放会话状态与工具产物 */
        private String workspace = ".agentscope/wechat";

        /** 触发上下文压缩的消息条数 */
        private int compactionTrigger = 30;

        /** 压缩后保留的消息条数 */
        private int compactionKeep = 10;

        /** 是否为 Agent 挂载文件系统操作工具（生产环境请谨慎开启） */
        private boolean enableFileTools = false;

        /** 处理微信消息的线程池大小 */
        private int workerThreads = 8;
    }
}
