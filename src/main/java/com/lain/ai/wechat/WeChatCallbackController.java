package com.lain.ai.wechat;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 微信回调端点。
 *
 * <p>需要在微信后台把 URL 配置为 {@code https://你的域名/wechat/callback}，
 * 且该路径必须在鉴权白名单中放行（微信服务器不会携带本站登录态）。
 */
@RestController
@RequestMapping("${wechat.bot.callback-path:/wechat/callback}")
public class WeChatCallbackController {

    private static final Logger log = LoggerFactory.getLogger(WeChatCallbackController.class);

    private final WeChatBotProperties properties;
    private final WeChatBotService service;
    private final WeChatCrypto crypto;

    public WeChatCallbackController(WeChatBotProperties properties,
                                    WeChatBotService service,
                                    WeChatCrypto crypto) {
        this.properties = properties;
        this.service = service;
        this.crypto = crypto;
    }

    /**
     * 回调配置校验：微信后台点击"保存"时会发起 GET 请求。
     *
     * @param signature    公众号签名参数
     * @param msgSignature 企业微信签名参数
     * @param timestamp    时间戳
     * @param nonce        随机数
     * @param echostr      随机字符串，加密模式下为密文
     * @return 解密后的 echostr 明文
     */
    @GetMapping(produces = "text/plain;charset=UTF-8")
    public String verify(@RequestParam(name = "signature", required = false) String signature,
                         @RequestParam(name = "msg_signature", required = false) String msgSignature,
                         @RequestParam(name = "timestamp", required = false) String timestamp,
                         @RequestParam(name = "nonce", required = false) String nonce,
                         @RequestParam(name = "echostr", required = false) String echostr) {
        String sign = pick(signature, msgSignature);
        if (!crypto.verify(sign, timestamp, nonce, echostr)) {
            log.warn("微信回调校验失败，签名不匹配，timestamp={}", timestamp);
            return "signature mismatch";
        }
        if (properties.isEncryptMode() && echostr != null && !echostr.isBlank()) {
            try {
                return crypto.decrypt(echostr);
            } catch (Exception e) {
                log.error("echostr 解密失败: {}", e.getMessage());
                return "decrypt failed";
            }
        }
        return echostr == null ? "" : echostr;
    }

    /**
     * 接收微信推送的消息。
     *
     * @param request HTTP 请求，微信以 text/xml 提交
     * @return 被动回复 XML，或 success 表示已收到且无需回复
     */
    @PostMapping(produces = "text/plain;charset=UTF-8")
    public String callback(HttpServletRequest request,
                           @RequestParam(name = "signature", required = false) String signature,
                           @RequestParam(name = "msg_signature", required = false) String msgSignature,
                           @RequestParam(name = "timestamp", required = false) String timestamp,
                           @RequestParam(name = "nonce", required = false) String nonce) {
        String body;
        try {
            body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取微信回调请求体失败: {}", e.getMessage());
            return "read body failed";
        }

        try {
            WeChatXmlMessage envelope = WeChatXmlMessage.parse(body);
            String encrypt = envelope.getEncrypt();
            String sign = pick(signature, msgSignature);

            if (!crypto.verify(sign, timestamp, nonce, encrypt)) {
                log.warn("微信消息签名校验失败，来源可疑，已丢弃");
                return "signature mismatch";
            }

            WeChatXmlMessage message = envelope;
            if (encrypt != null && !encrypt.isBlank()) {
                message = WeChatXmlMessage.parse(crypto.decrypt(encrypt));
            }

            String response = service.handleInbound(message, timestamp, nonce);
            return response == null || response.isBlank() ? "success" : response;
        } catch (Exception e) {
            // 出错也必须返回 HTTP 200，否则微信会认为推送失败并重试
            log.error("处理微信消息异常: {}", e.getMessage(), e);
            return "success";
        }
    }

    private static String pick(String signature, String msgSignature) {
        return msgSignature != null && !msgSignature.isBlank() ? msgSignature : signature;
    }
}
