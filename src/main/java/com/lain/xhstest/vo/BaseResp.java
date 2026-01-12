package com.lain.xhstest.vo;

import lombok.Data;
import java.util.List;

@Data
public class BaseResp<T> {
    private Integer code;
    private List<T> msg;
    private Integer total;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;
}