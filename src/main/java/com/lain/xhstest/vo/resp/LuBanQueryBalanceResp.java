package com.lain.xhstest.vo.resp;

import lombok.Data;

// {"code":0,"msg":"","balance":"10.000"}
@Data
public class LuBanQueryBalanceResp {
    private String balance;
    private String msg;
    private int code;
}
