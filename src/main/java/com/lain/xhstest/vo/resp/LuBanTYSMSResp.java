package com.lain.xhstest.vo.resp;

import lombok.Data;

// 等待短信:
//{"code":400,"msg":"尚未收到短信,请稍后重试"}
//
//收到短信:
//{"code":0,"msg":"【百度】验证码xxxx,您正在进行登陆验证."}
//
//可能的错误:
//{"code":400,"msg":"不正确的apikey"}
@Data
public class LuBanTYSMSResp {

    private Integer code;
    private String msg;

}
