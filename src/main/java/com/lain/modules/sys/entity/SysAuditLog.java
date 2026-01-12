package com.lain.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.lain.common.vo.BaseEntity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_audit_log")
public class SysAuditLog extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long logId;

    private Long userId;

    private String username;

    private String operation;

    private String method;

    private String params;

    private String result;

    private String ip;

    private String userAgent;

    private Long time;

}
