package com.lain.modules.sys.vo;

import lombok.Data;

@Data
public class PageModel {

    private Integer current = 1;
    private Integer size = 10;
}
