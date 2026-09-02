package com.lain.ai.wechat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 微信消息加解密与 XML 解析的自测。
 *
 * <p>加解密是接入过程中最容易出错的一环，改动相关代码后请务必跑一次这个测试。
 */
class WeChatCryptoTest {

    private static final String TOKEN = "spamtest";
    private static final String AES_KEY = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG";
    private static final String RECEIVE_ID = "wx5823bf96d3bd56c7";

    private final WeChatCrypto crypto = new WeChatCrypto(TOKEN, AES_KEY, RECEIVE_ID);

    @Test
    void 加解密往返应保持一致() {
        String plain = "<xml><ToUserName><![CDATA[toUser]]></ToUserName>"
                + "<FromUserName><![CDATA[fromUser]]></FromUserName>"
                + "<CreateTime>1348831860</CreateTime>"
                + "<MsgType><![CDATA[text]]></MsgType>"
                + "<Content><![CDATA[你好，这是一条中文测试消息]]></Content>"
                + "<MsgId>1234567890123456</MsgId></xml>";

        String encrypted = crypto.encrypt(plain);
        assertEquals(plain, crypto.decrypt(encrypted), "解密结果应与原文一致");
    }

    @Test
    void 密文长度应为32字节整数倍() {
        byte[] encrypted = java.util.Base64.getDecoder().decode(crypto.encrypt("<xml/>"));
        assertEquals(0, encrypted.length % 32, "PKCS7 填充后长度应为 32 的整数倍");
    }

    @Test
    void ReceiveId不匹配时应拒绝解密() {
        String encrypted = crypto.encrypt("<xml><Content><![CDATA[hi]]></Content></xml>");
        WeChatCrypto other = new WeChatCrypto(TOKEN, AES_KEY, "wxAnotherCorpId");
        assertThrows(SecurityException.class, () -> other.decrypt(encrypted));
    }

    @Test
    void 签名生成与校验应一致() {
        String timestamp = "1409659813";
        String nonce = "1372623149";
        String encrypted = crypto.encrypt("<xml><Content><![CDATA[hi]]></Content></xml>");

        String signature = crypto.signature(timestamp, nonce, encrypted);
        assertTrue(crypto.verify(signature, timestamp, nonce, encrypted), "加密模式签名应校验通过");
        assertNotEquals(signature, crypto.signature(timestamp, "other-nonce", encrypted));
    }

    @Test
    void 明文模式签名应校验通过() {
        String timestamp = "1409659813";
        String nonce = "1372623149";
        String plainSignature = new WeChatCrypto(TOKEN, AES_KEY, RECEIVE_ID)
                .signature(timestamp, nonce, "");
        assertTrue(crypto.verify(plainSignature, timestamp, nonce, null), "明文模式签名应校验通过");
    }

    @Test
    void 应能解析回调XML并生成被动回复() {
        String xml = "<xml><ToUserName><![CDATA[toUser]]></ToUserName>"
                + "<FromUserName><![CDATA[fromUser]]></FromUserName>"
                + "<CreateTime>1348831860</CreateTime>"
                + "<MsgType><![CDATA[text]]></MsgType>"
                + "<Content><![CDATA[hello]]></Content>"
                + "<MsgId>1234567890123456</MsgId></xml>";

        WeChatXmlMessage message = WeChatXmlMessage.parse(xml);
        assertEquals("fromUser", message.getFromUserName());
        assertEquals("toUser", message.getToUserName());
        assertEquals("hello", message.getContent());
        assertTrue(message.isText());

        String reply = message.buildTextReply("你好");
        assertTrue(reply.contains("<Content><![CDATA[你好]]></Content>"));
        assertTrue(reply.contains("<ToUserName><![CDATA[fromUser]]></ToUserName>"),
                "被动回复需要把收发双方互换");
    }

    @Test
    void 加密回复应可被微信侧解密() {
        WeChatXmlMessage message = WeChatXmlMessage.parse(
                "<xml><ToUserName><![CDATA[toUser]]></ToUserName>"
                        + "<FromUserName><![CDATA[fromUser]]></FromUserName></xml>");
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = "abc123";

        String encryptedReply = message.buildEncryptedReply(
                message.buildTextReply("ok"), crypto, timestamp, nonce);

        WeChatXmlMessage envelope = WeChatXmlMessage.parse(encryptedReply);
        assertTrue(envelope.getEncrypt() != null && !envelope.getEncrypt().isBlank());
        assertTrue(crypto.verify(envelope.getEncrypt() == null ? null : signatureOf(encryptedReply),
                timestamp, nonce, envelope.getEncrypt()));
    }

    private String signatureOf(String encryptedReplyXml) {
        WeChatXmlMessage envelope = WeChatXmlMessage.parse(encryptedReplyXml);
        return crypto.signature(
                xmlField(encryptedReplyXml, "TimeStamp"),
                xmlField(encryptedReplyXml, "Nonce"),
                envelope.getEncrypt());
    }

    private static String xmlField(String xml, String tag) {
        int start = xml.indexOf("<" + tag + ">");
        int end = xml.indexOf("</" + tag + ">");
        if (start < 0 || end < 0) {
            return "";
        }
        String value = xml.substring(start + tag.length() + 2, end);
        return value.replace("<![CDATA[", "").replace("]]>", "");
    }
}
