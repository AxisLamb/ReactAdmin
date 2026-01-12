package com.lain.modules.sys.vo;

import lombok.Data;

@Data
public class UserInfoVO {

    private Long userId;
    private String userName;
    private String password;
    private String realName;
    private String email;
    private String mobile;
    private Integer status;
    private Long roleId;
    private String createTime;
    private SysRoleVO role;

}
