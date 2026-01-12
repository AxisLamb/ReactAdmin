package com.lain.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@TableName("sys_user_role")
@AllArgsConstructor
public class SysUserRole {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long roleId;
}