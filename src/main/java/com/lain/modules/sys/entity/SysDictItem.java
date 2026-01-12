// SysDictItem.java
package com.lain.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.lain.common.vo.BaseEntity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_dict_item")
public class SysDictItem extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long itemId;

    private Long dictId;

    private String itemLabel;

    private String itemValue;

    private Integer status = 1;

    private Integer orderNum = 0;

    private String remark;

}
