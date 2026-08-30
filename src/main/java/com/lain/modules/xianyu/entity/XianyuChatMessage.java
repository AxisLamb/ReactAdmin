package com.lain.modules.xianyu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 闲鱼机器人聊天消息（对应 Python 版 messages 表）
 * <p>
 * 注意：不继承 BaseEntity，机器人后台线程无 Sa-Token 登录上下文，时间字段手动赋值
 */
@Data
@TableName("xianyu_chat_message")
public class XianyuChatMessage {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    private String chatId;

    /** 用户ID（用户消息存真实 user_id，助手消息存卖家 ID） */
    private String userId;

    /** 商品ID */
    private String itemId;

    /** 消息角色: user/assistant/system */
    private String role;

    /** 消息内容 */
    private String content;

    /** 创建时间 */
    private LocalDateTime createTime;
}
