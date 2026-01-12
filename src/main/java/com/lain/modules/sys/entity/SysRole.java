package com.lain.modules.sys.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.lain.common.vo.BaseEntity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class SysRole extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long roleId;

    private String roleName;

    private String roleDesc;

    private Integer status;
}