package com.lain.xhstest.vo.resp;

import lombok.Data;

// {"code":0,"msg":"wait","sms_msg":{"request_id":"244936588","application_id":133,"country_id":1,"number":"79587588703"}}
@Data
public class LuBanSMSResp {
    private Integer code;
    private String msg;
    private String sms_code;
    private SmsMsg smsMsg;

    @Data
    public static class SmsMsg {
        private String requestId;
        private Integer applicationId;
        private Integer countryId;
        private String number;
    }
}
