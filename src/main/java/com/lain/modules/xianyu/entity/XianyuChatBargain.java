package com.lain.modules.xianyu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 闲鱼会话议价次数（对应 Python 版 chat_bargain_counts 表）
 */
@Data
@TableName("xianyu_chat_bargain")
public class XianyuChatBargain {

    /** 会话ID（主键） */
    @TableId(type = IdType.INPUT)
    private String chatId;

    /** 议价次数 */
    private Integer count;

    /** 最后更新时间 */
    private LocalDateTime lastUpdated;
}
