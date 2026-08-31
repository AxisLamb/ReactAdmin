package com.lain.modules.xianyu.agent;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 意图路由决策器
 */
public class IntentRouter {

    /** 技术类关键词（优先判定） */
    private static final List<String> TECH_KEYWORDS = List.of("参数", "规格", "型号", "连接", "对比");

    /** 技术类正则 */
    private static final List<Pattern> TECH_PATTERNS = List.of(Pattern.compile("和.+比"));

    /** 价格类关键词 */
    private static final List<String> PRICE_KEYWORDS = List.of("便宜", "价", "砍价", "少点");

    /** 价格类正则 */
    private static final List<Pattern> PRICE_PATTERNS = List.of(
            Pattern.compile("\\d+元"),
            Pattern.compile("能少\\d+"));

    private final ClassifyAgent classifyAgent;

    public IntentRouter(ClassifyAgent classifyAgent) {
        this.classifyAgent = classifyAgent;
    }

    /**
     * 三级路由策略（技术优先）
     */
    public String detect(String userMsg, String itemDesc, String context) {
        // 移除标点符号等非文本字符，仅保留字母数字下划线与中文
        String textClean = userMsg.replaceAll("[^\\w\\u4e00-\\u9fa5]", "");

        // 1. 技术类关键词优先检查
        for (String kw : TECH_KEYWORDS) {
            if (textClean.contains(kw)) {
                return "tech";
            }
        }

        // 2. 技术类正则优先检查
        for (Pattern pattern : TECH_PATTERNS) {
            if (pattern.matcher(textClean).find()) {
                return "tech";
            }
        }

        // 3. 价格类检查
        for (String kw : PRICE_KEYWORDS) {
            if (textClean.contains(kw)) {
                return "price";
            }
        }
        for (Pattern pattern : PRICE_PATTERNS) {
            if (pattern.matcher(textClean).find()) {
                return "price";
            }
        }

        // 4. 大模型兜底
        return classifyAgent.generate(userMsg, itemDesc, context, 0);
    }
}
