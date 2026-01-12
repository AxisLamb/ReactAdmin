package com.lain.xhstest;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.lain.xhstest.vo.BaseResp;
import com.lain.xhstest.vo.resp.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LuBanApi {

    public static final String API_KEY = "d26aff989f475e9f7e77949e23177503";
    public static final String QUERY_GETBALANCE = "https://lubansms.com/v2/api/getBalance?apikey=YOUR_APIKEY";
    // 获取服务列表
    // xhs:{"code":0,"msg":[{"service_id":"665375","country_name_zh":"\u4e2d\u56fd","country_name_en":"China","service_name":"\u5c0f\u7ea2\u4e66","provider":"cn7","get_num":"18096","success_num":"8302","cost":1.05}],"total":1,"total_pages":1,"current_page":1,"page_size":5}
    public static final String QUERY_SERVICE_LIST = "https://lubansms.com/v2/api/List?apikey=YOUR_APIKEY&country=countryName&service=serviceName&language=zh&page=1";
    // 请求号码
    public static final String REQUEST_NUMBER = "https://lubansms.com/v2/api/getNumber?apikey=YOUR_APIKEY&service_id=serviceId";
    // 获取短信
    public static final String GET_SMS = "https://lubansms.com/v2/api/getSms?apikey=YOUR_APIKEY&request_id=requestId";
    // 更改请求状态(REJECT轮询)
    // request_id	string	是的	通过请求号码接口得到的request_id
    // status	string	是的	值:reject 释放号码
    // 正确的结果:
    //{"code":0,"msg":"success"}
    // 可能的错误:
    //{"code":400,"msg":"不正确的apikey"}
    public static final String UPDATE_REQUEST_STATUS = "https://lubansms.com/v2/api/setStatus?apikey=YOUR_APIKEY&request_id=requestId&status=reject";


    // 通用请求号码
    // phoneNum:指定的号码，留空随机获取号码;该参数通常用于再次使用相同号码时接收短信
    // 正确的结果:
    //{"code":0,"msg":"","phone":"18888888888"}
    // 可能的错误:
    //{"code":400,"msg":"不正确的apikey"}
    public static final String TY_REQUEST_NUMBER = "https://lubansms.com/v2/api/getKeywordNumber?apikey=YOUR_APIKEY&phone=phoneNum";
    // 通用获取短信
    // phone	string	是的	手机号码,通过请求号码接口获取.
    // keyword	string	是的	短信中包含的关键词;例如短信内容为:【百度】验证码xxxx,您正在进行登陆验证,北极企鹅.;关键词可填写为:百度 或者 北极企鹅 .
    public static final String TY_GET_SMS = " https://lubansms.com/v2/api/getKeywordSms?apikey=YOUR_APIKEY&phone=phoneNum&keyword=keywordStr";

    public static String queryBalance() {
        String json = HttpUtil.get(QUERY_GETBALANCE.replaceAll("YOUR_APIKEY", API_KEY));
        LuBanQueryBalanceResp bean = JSONUtil.toBean(json, LuBanQueryBalanceResp.class);
        log.info(json);
        return bean.getBalance();
    }

    public static BaseResp<LuBanServiceResp> queryServiceList(String countryName, String serviceName) {
        String URL = QUERY_SERVICE_LIST.replaceAll("YOUR_APIKEY", API_KEY).replaceAll("countryName", countryName).replaceAll("serviceName", serviceName);
        String json = HttpUtil.get(URL);
        log.info(json);
        BaseResp<LuBanServiceResp> bean = JSONUtil.toBean(
                json,
                new TypeReference<BaseResp<LuBanServiceResp>>() {},
                false
        );
        return bean;
    }

    public static LuBanNumberResp requestNewNumber() {
        String URL = REQUEST_NUMBER.replaceAll("YOUR_APIKEY", API_KEY).replaceAll("serviceId", getXHSServiceId());
        String json = HttpUtil.get(URL);
        log.info(json);
        return JSONUtil.toBean(json, LuBanNumberResp.class);
    }

    public static LuBanSMSResp getSMS(String requestId) {
        String URL = GET_SMS.replaceAll("YOUR_APIKEY", API_KEY).replaceAll("requestId", requestId);
        String json = HttpUtil.get(URL);
        log.info(json);
        return JSONUtil.toBean(json, LuBanSMSResp.class);
    }

    public static LuBanTYSMSResp updateRequestStatus(String requestId) {
        String URL = UPDATE_REQUEST_STATUS.replaceAll("YOUR_APIKEY", API_KEY).replaceAll("requestId", requestId);
        String json = HttpUtil.get(URL);
        log.info(json);
        return JSONUtil.toBean(json, LuBanTYSMSResp.class);
    }

    public static String getXHSServiceId() {
        return "665375";
//        BaseResp<LuBanServiceResp> resp = queryServiceList("中国", "小红书");
//        List<LuBanServiceResp> msg = resp.getMsg();
//        LuBanServiceResp luBanServiceResp = msg.get(0);
//        log.info(luBanServiceResp.getServiceId());
//        return luBanServiceResp.getServiceId();
    }

    public static LuBanTYSMSResp getTySms(String phoneNum, String keywordStr) {
        String URL = TY_GET_SMS.replaceAll("YOUR_APIKEY", API_KEY).replaceAll("phoneNum", phoneNum).replaceAll("keywordStr", keywordStr);
        String json = HttpUtil.get(URL);
        log.info(json);
        return JSONUtil.toBean(json, LuBanTYSMSResp.class);
    }

    public static void main(String[] args) {
        LuBanNumberResp resp = requestNewNumber();
        String requestId = resp.getRequest_id();
        String number = resp.getNumber();
        log.info("number:" + number);
        log.info("requestId:" + requestId);

    }

}
