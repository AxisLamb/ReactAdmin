package com.lain.modules.sys.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "系统角色VO")
public class SysRoleVO extends PageModel{

    private Long roleId;

    private String roleName;

    private String roleDesc;

    private Integer status;

    private List<Long> menuIds;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
