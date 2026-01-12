package com.lain.xhstest.vo.resp;

import lombok.Data;

@Data
public class LuBanServiceResp {
    private String serviceId;
    private String countryNameZh;
    private String countryNameEn;
    private String serviceName;
    private String provider;
    private String getNum;
    private String successNum;
    private Double cost;
}