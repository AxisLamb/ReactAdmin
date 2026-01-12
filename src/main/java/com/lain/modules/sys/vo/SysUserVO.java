package com.lain.modules.sys.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统用户VO")
public class SysUserVO extends PageModel{
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "角色名")
    private String roleName;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "状态 0:禁用 1:正常")
    private Integer status;

}
