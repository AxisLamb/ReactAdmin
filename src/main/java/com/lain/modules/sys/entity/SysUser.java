package com.lain.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.lain.common.vo.BaseEntity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long userId;

    private String username;

    private String password;

    private String realName;

    private String email;

    private String mobile;

    private Integer status = 1;

}
