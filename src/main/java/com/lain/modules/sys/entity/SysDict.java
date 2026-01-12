// SysDict.java
package com.lain.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.lain.common.vo.BaseEntity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_dict")
public class SysDict extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long dictId;

    private String dictName;

    private String dictType;

    private Integer status = 1;

    private String remark;

}
