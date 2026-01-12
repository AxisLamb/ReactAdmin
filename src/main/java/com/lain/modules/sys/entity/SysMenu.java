package com.lain.modules.sys.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.lain.common.vo.BaseEntity;
import lombok.Data;

@Data
@TableName("sys_menu")
public class SysMenu extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long menuId;

    private Long parentId;

    private String name;

    private String url;

    private String reactComponent;

    private String perms;

    private Integer type;

    private String icon;

    private Integer orderNum;
}