package com.lain.modules.xianyu.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 闲鱼智能回复机器人（对应 Python 版 XianyuAgent.py 的 XianyuReplyBot）
 * <p>
 * 多专家协同决策：意图分类 → 领域 Agent 分发（议价/技术/默认），支持上下文感知对话。
 */
@Component
public class XianyuReplyBot {

    private static final Logger log = LoggerFactory.getLogger(XianyuReplyBot.class);

    /** 议价次数提取正则 */
    private static final Pattern BARGAIN_COUNT_PATTERN = Pattern.compile("议价次数[:：]\\s*(\\d+)");

    /** 不对外开放的内部 Agent */
    private static final String INTERNAL_INTENT = "classify";

    /** 各领域 Agent */
    private final Map<String, BaseAgent> agents = new LinkedHashMap<>();

    private final LlmClient llmClient;
    private final PromptLoader promptLoader;
    private final IntentRouter router;

    /** 安全过滤词 */
    private static final List<String> BLOCKED_PHRASES = List.of("微信", "QQ", "支付宝", "银行卡", "线下");

    /** 记录最后一次意图 */
    private volatile String lastIntent;

    public XianyuReplyBot(LlmClient llmClient, PromptLoader promptLoader) {
        this.llmClient = llmClient;
        this.promptLoader = promptLoader;
        initAgents();
        this.router = new IntentRouter((ClassifyAgent) agents.get("classify"));
    }

    /**
     * 初始化各领域 Agent（对应 Python 版 _init_agents）
     */
    private void initAgents() {
        agents.put("classify", new ClassifyAgent(llmClient, promptLoader.load("classify_prompt"), this::safeFilter));
        agents.put("price", new PriceAgent(llmClient, promptLoader.load("price_prompt"), this::safeFilter));
        agents.put("tech", new TechAgent(llmClient, promptLoader.load("tech_prompt"), this::safeFilter));
        agents.put("default", new DefaultAgent(llmClient, promptLoader.load("default_prompt"), this::safeFilter));
        log.info("成功加载所有提示词并初始化各领域 Agent");
    }

    /**
     * 安全过滤模块：命中违规词时替换为安全提醒
     */
    private String safeFilter(String text) {
        for (String phrase : BLOCKED_PHRASES) {
            if (text.contains(phrase)) {
                return "[安全提醒]请通过平台沟通";
            }
        }
        return text;
    }

    /**
     * 格式化对话历史，返回完整的对话记录（过滤系统消息，仅保留 user/assistant）
     */
    public String formatHistory(List<Map<String, String>> context) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, String> msg : context) {
            String role = msg.get("role");
            if ("user".equals(role) || "assistant".equals(role)) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(role).append(": ").append(msg.get("content"));
            }
        }
        return sb.toString();
    }

    /**
     * 生成回复主流程
     *
     * @param userMsg    用户消息
     * @param itemDesc   商品描述
     * @param context    对话历史
     * @return 回复内容；"-" 表示无需回复
     */
    public String generateReply(String userMsg, String itemDesc, List<Map<String, String>> context) {
        String formattedContext = formatHistory(context);

        // 1. 路由决策
        String detectedIntent = router.detect(userMsg, itemDesc, formattedContext);

        // 2. 获取对应 Agent
        BaseAgent agent;
        if ("no_reply".equals(detectedIntent)) {
            log.info("意图识别完成: no_reply - 无需回复");
            lastIntent = "no_reply";
            return "-"; // 返回特殊标记，表示无需回复
        } else if (agents.containsKey(detectedIntent) && !INTERNAL_INTENT.equals(detectedIntent)) {
            agent = agents.get(detectedIntent);
            log.info("意图识别完成: {}", detectedIntent);
            lastIntent = detectedIntent;
        } else {
            agent = agents.get("default");
            log.info("意图识别完成: default");
            lastIntent = "default";
        }

        // 3. 获取议价次数
        int bargainCount = extractBargainCount(context);
        log.info("议价次数: {}", bargainCount);

        // 4. 生成回复
        return agent.generate(userMsg, itemDesc, formattedContext, bargainCount);
    }

    /**
     * 从上下文中提取议价次数信息，未找到返回 0
     */
    private int extractBargainCount(List<Map<String, String>> context) {
        for (Map<String, String> msg : context) {
            if ("system".equals(msg.get("role")) && msg.get("content") != null
                    && msg.get("content").contains("议价次数")) {
                try {
                    Matcher matcher = BARGAIN_COUNT_PATTERN.matcher(msg.get("content"));
                    if (matcher.find()) {
                        return Integer.parseInt(matcher.group(1));
                    }
                } catch (Exception e) {
                    // 解析失败忽略
                }
            }
        }
        return 0;
    }

    /**
     * 重新加载所有提示词
     */
    public void reloadPrompts() {
        log.info("正在重新加载提示词...");
        promptLoader.reload();
        initAgents();
        log.info("提示词重新加载完成");
    }

    public String getLastIntent() {
        return lastIntent;
    }
}
