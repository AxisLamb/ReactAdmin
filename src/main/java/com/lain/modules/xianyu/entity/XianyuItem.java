package com.lain.modules.xianyu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 闲鱼商品信息缓存（对应 Python 版 items 表）
 */
@Data
@TableName("xianyu_item")
public class XianyuItem {

    /** 商品ID（主键） */
    @TableId(type = IdType.INPUT)
    private String itemId;

    /** 商品完整数据(JSON) */
    private String data;

    /** 商品价格(元) */
    private BigDecimal price;

    /** 商品描述 */
    private String description;

    /** 最后更新时间 */
    private LocalDateTime lastUpdated;
}
