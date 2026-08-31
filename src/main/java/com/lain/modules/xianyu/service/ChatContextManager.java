package com.lain.modules.xianyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lain.modules.xianyu.config.XianyuProperties;
import com.lain.modules.xianyu.dao.XianyuChatBargainMapper;
import com.lain.modules.xianyu.dao.XianyuChatMessageMapper;
import com.lain.modules.xianyu.dao.XianyuItemMapper;
import com.lain.modules.xianyu.entity.XianyuChatBargain;
import com.lain.modules.xianyu.entity.XianyuChatMessage;
import com.lain.modules.xianyu.entity.XianyuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天上下文管理器
 * <p>
 * 负责存储和检索用户与商品之间的对话历史、议价次数统计、商品信息缓存。
 */
@Service
public class ChatContextManager {

    private static final Logger log = LoggerFactory.getLogger(ChatContextManager.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final XianyuChatMessageMapper chatMessageMapper;
    private final XianyuChatBargainMapper chatBargainMapper;
    private final XianyuItemMapper itemMapper;
    private final XianyuProperties properties;

    public ChatContextManager(XianyuChatMessageMapper chatMessageMapper,
                              XianyuChatBargainMapper chatBargainMapper,
                              XianyuItemMapper itemMapper,
                              XianyuProperties properties) {
        this.chatMessageMapper = chatMessageMapper;
        this.chatBargainMapper = chatBargainMapper;
        this.itemMapper = itemMapper;
        this.properties = properties;
    }

    /**
     * 保存商品信息到数据库（存在则更新）
     */
    public void saveItemInfo(String itemId, JsonNode itemData) {
        try {
            // 从商品数据中提取有用信息
            double price = itemData.path("soldPrice").asDouble(0);
            String description = itemData.path("desc").asText("");

            XianyuItem exist = itemMapper.selectById(itemId);
            XianyuItem item = new XianyuItem();
            item.setItemId(itemId);
            item.setData(MAPPER.writeValueAsString(itemData));
            item.setPrice(price > 0 ? BigDecimal.valueOf(price) : null);
            item.setDescription(description);
            item.setLastUpdated(LocalDateTime.now());

            if (exist != null) {
                itemMapper.updateById(item);
            } else {
                itemMapper.insert(item);
            }
            log.debug("商品信息已保存: {}", itemId);
        } catch (Exception e) {
            log.error("保存商品信息时出错: {}", e.getMessage());
        }
    }

    /**
     * 从数据库获取商品信息，不存在返回 null
     */
    public JsonNode getItemInfo(String itemId) {
        try {
            XianyuItem item = itemMapper.selectById(itemId);
            if (item != null && item.getData() != null) {
                return MAPPER.readTree(item.getData());
            }
            return null;
        } catch (Exception e) {
            log.error("获取商品信息时出错: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 基于会话 ID 添加新消息到对话历史，并清理超出最大条数的旧消息
     */
    public void addMessageByChat(String chatId, String userId, String itemId, String role, String content) {
        try {
            XianyuChatMessage message = new XianyuChatMessage();
            message.setChatId(chatId);
            message.setUserId(userId);
            message.setItemId(itemId);
            message.setRole(role);
            message.setContent(content);
            message.setCreateTime(LocalDateTime.now());
            chatMessageMapper.insert(message);

            // 检查是否需要清理旧消息（基于 chat_id，保留最新 max_history 条）
            List<XianyuChatMessage> all = chatMessageMapper.selectList(
                    new LambdaQueryWrapper<XianyuChatMessage>()
                            .eq(XianyuChatMessage::getChatId, chatId)
                            .orderByAsc(XianyuChatMessage::getId)
                            .select(XianyuChatMessage::getId));
            int maxHistory = properties.getMaxHistory();
            if (all.size() > maxHistory) {
                List<Long> idsToDelete = new ArrayList<>(all.size() - maxHistory);
                for (int i = 0; i < all.size() - maxHistory; i++) {
                    idsToDelete.add(all.get(i).getId());
                }
                chatMessageMapper.delete(
                        new LambdaQueryWrapper<XianyuChatMessage>()
                                .eq(XianyuChatMessage::getChatId, chatId)
                                .in(XianyuChatMessage::getId, idsToDelete));
            }
        } catch (Exception e) {
            log.error("添加消息到数据库时出错: {}", e.getMessage());
        }
    }

    /**
     * 基于会话 ID 获取对话历史（含议价次数系统消息）
     */
    public List<Map<String, String>> getContextByChat(String chatId) {
        List<Map<String, String>> messages;
        try {
            // 插入时已按 maxHistory 清理，会话内消息数不会超出限制，直接查询即可（避免跨库 LIMIT 语法差异）
            List<XianyuChatMessage> records = chatMessageMapper.selectList(
                    new LambdaQueryWrapper<XianyuChatMessage>()
                            .eq(XianyuChatMessage::getChatId, chatId)
                            .orderByAsc(XianyuChatMessage::getId));
            messages = new ArrayList<>(records.size());
            for (XianyuChatMessage record : records) {
                Map<String, String> msg = new LinkedHashMap<>(2);
                msg.put("role", record.getRole());
                msg.put("content", record.getContent());
                messages.add(msg);
            }

            // 获取议价次数并添加到上下文中
            int bargainCount = getBargainCountByChat(chatId);
            if (bargainCount > 0) {
                Map<String, String> systemMsg = new LinkedHashMap<>(2);
                systemMsg.put("role", "system");
                systemMsg.put("content", "议价次数: " + bargainCount);
                messages.add(systemMsg);
            }
        } catch (Exception e) {
            log.error("获取对话历史时出错: {}", e.getMessage());
            messages = new ArrayList<>();
        }
        return messages;
    }

    /**
     * 基于会话 ID 增加议价次数
     */
    public void incrementBargainCountByChat(String chatId) {
        try {
            XianyuChatBargain exist = chatBargainMapper.selectById(chatId);
            XianyuChatBargain bargain = new XianyuChatBargain();
            bargain.setChatId(chatId);
            bargain.setLastUpdated(LocalDateTime.now());
            if (exist != null) {
                bargain.setCount(exist.getCount() + 1);
                chatBargainMapper.updateById(bargain);
            } else {
                bargain.setCount(1);
                chatBargainMapper.insert(bargain);
            }
            log.debug("会话 {} 议价次数已增加", chatId);
        } catch (Exception e) {
            log.error("增加议价次数时出错: {}", e.getMessage());
        }
    }

    /**
     * 基于会话 ID 获取议价次数
     */
    public int getBargainCountByChat(String chatId) {
        try {
            XianyuChatBargain bargain = chatBargainMapper.selectById(chatId);
            return bargain != null ? bargain.getCount() : 0;
        } catch (Exception e) {
            log.error("获取议价次数时出错: {}", e.getMessage());
            return 0;
        }
    }
}
