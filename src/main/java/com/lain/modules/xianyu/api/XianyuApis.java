package com.lain.modules.xianyu.api;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lain.common.exception.LainException;
import com.lain.modules.xianyu.utils.XianyuUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 闲鱼 H5 API 封装（对应 Python 版 XianyuApis.py）
 * <p>
 * 使用 Hutool HttpRequest 维护带 Cookie 的有状态会话：
 * 请求时携带完整 Cookie 头，响应后从 Set-Cookie 头增量更新。
 */
@Component
public class XianyuApis {

    private static final Logger log = LoggerFactory.getLogger(XianyuApis.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 获取 Token 接口地址 */
    private static final String TOKEN_URL = "https://h5api.m.goofish.com/h5/mtop.taobao.idlemessage.pc.login.token/1.0/";

    /** 商品详情接口地址 */
    private static final String ITEM_URL = "https://h5api.m.goofish.com/h5/mtop.taobao.idle.pc.detail/1.0/";

    /** 登录状态检查地址 */
    private static final String HAS_LOGIN_URL = "https://passport.goofish.com/newlogin/hasLogin.do";

    /** 默认请求头（对应 Python 版 session.headers） */
    private static final Map<String, String> DEFAULT_HEADERS = new HashMap<>() {{
        put("accept", "application/json");
        put("accept-language", "zh-CN,zh;q=0.9");
        put("cache-control", "no-cache");
        put("origin", "https://www.goofish.com");
        put("pragma", "no-cache");
        put("priority", "u=1, i");
        put("referer", "https://www.goofish.com/");
        put("sec-ch-ua", "\"Not(A:Brand\";v=\"99\", \"Google Chrome\";v=\"133\", \"Chromium\";v=\"133\"");
        put("sec-ch-ua-mobile", "?0");
        put("sec-ch-ua-platform", "\"Windows\"");
        put("sec-fetch-dest", "empty");
        put("sec-fetch-mode", "cors");
        put("sec-fetch-site", "same-site");
        put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");
    }};

    /** Set-Cookie 中的非业务属性名，解析时排除 */
    private static final Set<String> COOKIE_ATTRS = Set.of(
            "path", "domain", "expires", "max-age", "samesite", "secure", "httponly", "priority", "version");

    /** 当前会话 Cookies（并发安全，响应中的 Set-Cookie 会增量更新） */
    private final ConcurrentHashMap<String, String> cookies = new ConcurrentHashMap<>();

    /**
     * 使用 Cookie 字符串初始化会话
     */
    public void init(String cookiesStr) {
        cookies.clear();
        cookies.putAll(XianyuUtils.transCookies(cookiesStr));
        log.info("闲鱼 Cookies 初始化完成，共 {} 个", cookies.size());
    }

    /**
     * 调用 hasLogin.do 接口进行登录状态检查（重试最多 2 次）
     */
    public boolean hasLogin() {
        return hasLogin(0);
    }

    private boolean hasLogin(int retryCount) {
        if (retryCount >= 2) {
            log.error("Login检查失败，重试次数过多");
            return false;
        }
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("appName", "xianyu");
            params.put("fromSite", "77");

            Map<String, Object> data = new HashMap<>();
            data.put("hid", cookies.getOrDefault("unb", ""));
            data.put("ltl", "true");
            data.put("appName", "xianyu");
            data.put("appEntrance", "web");
            data.put("_csrf_token", cookies.getOrDefault("XSRF-TOKEN", ""));
            data.put("umidToken", "");
            data.put("hsiz", cookies.getOrDefault("cookie2", ""));
            data.put("bizParams", "taobaoBizLoginFrom=web");
            data.put("mainPage", "false");
            data.put("isMobile", "false");
            data.put("lang", "zh_CN");
            data.put("returnUrl", "");
            data.put("fromSite", "77");
            data.put("isIframe", "true");
            data.put("documentReferer", "https://www.goofish.com/");
            data.put("defaultView", "hasLogin");
            data.put("umidTag", "SERVER");
            data.put("deviceId", cookies.getOrDefault("cna", ""));

            String url = HttpUtil.urlWithForm(HAS_LOGIN_URL, params, java.nio.charset.StandardCharsets.UTF_8, true);
            HttpResponse response = HttpRequest.post(url)
                    .addHeaders(DEFAULT_HEADERS)
                    .header("Cookie", buildCookieStr())
                    .form(data)
                    .timeout(20000)
                    .execute();
            JsonNode resJson = MAPPER.readTree(response.body());
            response.close();

            JsonNode content = resJson.path("content");
            if (content.path("success").asBoolean(false)) {
                log.debug("Login成功");
                return true;
            } else {
                log.warn("Login失败: {}", resJson);
                sleep(500);
                return hasLogin(retryCount + 1);
            }
        } catch (Exception e) {
            log.error("Login请求异常: {}", e.getMessage());
            sleep(500);
            return hasLogin(retryCount + 1);
        }
    }

    /**
     * 获取 Token（重试最多 3 次；Cookie 失效时尝试重新登录；触发风控时抛出异常终止机器人）
     */
    public JsonNode getToken(String deviceId) {
        return getToken(deviceId, 0);
    }

    private JsonNode getToken(String deviceId, int retryCount) {
        if (retryCount >= 3) {
            log.warn("获取token失败，尝试重新登陆");
            if (hasLogin()) {
                log.info("重新登录成功，重新尝试获取token");
                return getToken(deviceId, 0);
            } else {
                log.error("重新登录失败，Cookie已失效");
                log.error("🔴 机器人即将停止，请更新环境变量 COOKIES_STR 后重启应用");
                throw new LainException("闲鱼 Cookie 已失效，请更新 COOKIES_STR 后重启");
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("jsv", "2.7.2");
        params.put("appKey", "34839810");
        params.put("t", String.valueOf(System.currentTimeMillis()));
        params.put("sign", "");
        params.put("v", "1.0");
        params.put("type", "originaljson");
        params.put("accountSite", "xianyu");
        params.put("dataType", "json");
        params.put("timeout", "20000");
        params.put("api", "mtop.taobao.idlemessage.pc.login.token");
        params.put("sessionOption", "AutoLoginOnly");
        params.put("spm_cnt", "a21ybx.im.0.0");
        params.put("spm_pre", "a21ybx.item.want.1.14ad3da6ALVq3n");
        params.put("log_id", "14ad3da6ALVq3n");

        String dataVal = "{\"appKey\":\"444e9908a51d1cb236a27862abc769c9\",\"deviceId\":\"" + deviceId + "\"}";

        // 简单获取 token，信任 cookies 已清理干净
        String token = cookies.getOrDefault("_m_h5_tk", "").split("_")[0];
        params.put("sign", XianyuUtils.generateSign((String) params.get("t"), token, dataVal));

        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "h5api.m.goofish.com");
        headers.put("sec-ch-ua-platform", "\"Windows\"");
        headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36");
        headers.put("accept", "application/json");
        headers.put("sec-ch-ua", "\"Chromium\";v=\"146\", \"Not-A.Brand\";v=\"24\", \"Google Chrome\";v=\"146\"");
        headers.put("content-type", "application/x-www-form-urlencoded");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("origin", "https://www.goofish.com");
        headers.put("sec-fetch-site", "same-site");
        headers.put("sec-fetch-mode", "cors");
        headers.put("sec-fetch-dest", "empty");
        headers.put("referer", "https://www.goofish.com/");
        headers.put("accept-language", "en,zh-CN;q=0.9,zh;q=0.8,zh-TW;q=0.7,ja;q=0.6");
        headers.put("priority", "u=1, i");

        try {
            String url = HttpUtil.urlWithForm(TOKEN_URL, params, java.nio.charset.StandardCharsets.UTF_8, true);
            HttpResponse response = HttpRequest.post(url)
                    .addHeaders(headers)
                    .header("Cookie", buildCookieStr())
                    .form(Map.of("data", dataVal))
                    .timeout(20000)
                    .execute();
            String body = response.body();
            // 响应中的 Set-Cookie 增量更新本地会话
            updateCookiesFromResponse(response);
            response.close();

            JsonNode resJson = MAPPER.readTree(body);
            if (resJson.isObject()) {
                JsonNode ret = resJson.path("ret");
                boolean success = false;
                for (JsonNode item : ret) {
                    if (item.asText().contains("SUCCESS::调用成功")) {
                        success = true;
                        break;
                    }
                }
                if (!success) {
                    String errorMsg = ret.toString();
                    if (errorMsg.contains("RGV587_ERROR") || errorMsg.contains("被挤爆啦")) {
                        log.error("❌ 触发风控: {}", ret);
                        log.error("🔴 系统目前无法自动解决，请进入闲鱼网页版-点击消息-过滑块-复制最新的 Cookie 并更新环境变量 COOKIES_STR");
                        throw new LainException("闲鱼接口触发风控(RGV587_ERROR)，需人工更新 Cookie");
                    }
                    log.warn("Token API调用失败，错误信息: {}", ret);
                    sleep(500);
                    return getToken(deviceId, retryCount + 1);
                }
                log.info("Token获取成功");
                return resJson;
            } else {
                log.error("Token API返回格式异常: {}", resJson);
                return getToken(deviceId, retryCount + 1);
            }
        } catch (LainException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token API请求异常: {}", e.getMessage());
            sleep(500);
            return getToken(deviceId, retryCount + 1);
        }
    }

    /**
     * 获取商品信息，自动处理 token 失效的情况（重试最多 3 次）
     */
    public JsonNode getItemInfo(String itemId) {
        return getItemInfo(itemId, 0);
    }

    private JsonNode getItemInfo(String itemId, int retryCount) {
        if (retryCount >= 3) {
            log.error("获取商品信息失败，重试次数过多");
            return MAPPER.createObjectNode().put("error", "获取商品信息失败，重试次数过多");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("jsv", "2.7.2");
        params.put("appKey", "34839810");
        params.put("t", String.valueOf(System.currentTimeMillis()));
        params.put("sign", "");
        params.put("v", "1.0");
        params.put("type", "originaljson");
        params.put("accountSite", "xianyu");
        params.put("dataType", "json");
        params.put("timeout", "20000");
        params.put("api", "mtop.taobao.idle.pc.detail");
        params.put("sessionOption", "AutoLoginOnly");
        params.put("spm_cnt", "a21ybx.im.0.0");

        String dataVal = "{\"itemId\":\"" + itemId + "\"}";

        // 简单获取 token，信任 cookies 已清理干净
        String token = cookies.getOrDefault("_m_h5_tk", "").split("_")[0];
        params.put("sign", XianyuUtils.generateSign((String) params.get("t"), token, dataVal));

        try {
            String url = HttpUtil.urlWithForm(ITEM_URL, params, java.nio.charset.StandardCharsets.UTF_8, true);
            HttpResponse response = HttpRequest.post(url)
                    .addHeaders(DEFAULT_HEADERS)
                    .header("Cookie", buildCookieStr())
                    .form(Map.of("data", dataVal))
                    .timeout(20000)
                    .execute();
            String body = response.body();
            // 响应中的 Set-Cookie 增量更新本地会话
            updateCookiesFromResponse(response);
            response.close();

            JsonNode resJson = MAPPER.readTree(body);
            if (resJson.isObject()) {
                JsonNode ret = resJson.path("ret");
                boolean success = false;
                for (JsonNode item : ret) {
                    if (item.asText().contains("SUCCESS::调用成功")) {
                        success = true;
                        break;
                    }
                }
                if (!success) {
                    log.warn("商品信息API调用失败，错误信息: {}", ret);
                    sleep(500);
                    return getItemInfo(itemId, retryCount + 1);
                }
                log.debug("商品信息获取成功: {}", itemId);
                return resJson;
            } else {
                log.error("商品信息API返回格式异常: {}", resJson);
                return getItemInfo(itemId, retryCount + 1);
            }
        } catch (Exception e) {
            log.error("商品信息API请求异常: {}", e.getMessage());
            sleep(500);
            return getItemInfo(itemId, retryCount + 1);
        }
    }

    /**
     * 从响应头中的 Set-Cookie 增量更新本地 Cookies
     */
    private void updateCookiesFromResponse(HttpResponse response) {
        boolean updated = false;
        for (Map.Entry<String, List<String>> entry : response.headers().entrySet()) {
            if ("Set-Cookie".equalsIgnoreCase(entry.getKey())) {
                for (String setCookie : entry.getValue()) {
                    // 解析 "name=value; Path=/; Expires=..."，仅取首个分号前的键值对
                    String firstPair = setCookie.split(";")[0].trim();
                    int idx = firstPair.indexOf('=');
                    if (idx > 0) {
                        String name = firstPair.substring(0, idx).trim();
                        String value = firstPair.substring(idx + 1).trim();
                        if (!COOKIE_ATTRS.contains(name.toLowerCase())) {
                            cookies.put(name, value);
                            updated = true;
                        }
                    }
                }
            }
        }
        if (updated) {
            log.debug("检测到 Set-Cookie，已更新 Cookie");
        }
    }

    /**
     * 拼接当前 Cookies 为请求头字符串
     */
    public String buildCookieStr() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return sb.toString();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
