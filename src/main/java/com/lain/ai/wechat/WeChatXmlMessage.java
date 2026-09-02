package com.lain.ai.wechat;

import lombok.Data;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信回调 XML 消息的解析与生成。
 *
 * <p>同时支持企业微信与微信公众号，两者的消息结构字段基本一致。
 * 解析使用 JDK 自带的 DOM，并关闭 DTD 与外部实体，避免 XXE 攻击。
 */
@Data
public class WeChatXmlMessage {

    /** 消息接收方：企业微信为 CorpID，公众号为公众号原始 ID */
    private String toUserName;

    /** 消息发送方：企业微信为 UserID，公众号为 OpenID */
    private String fromUserName;

    /** 消息创建时间（秒级时间戳） */
    private String createTime;

    /** 消息类型：text / image / voice / event ... */
    private String msgType;

    /** 文本消息内容 */
    private String content;

    /** 消息 ID，用于去重，事件消息没有该字段 */
    private String msgId;

    /** 事件类型：subscribe / unsubscribe / click / enter_agent ... */
    private String event;

    /** 事件 Key */
    private String eventKey;

    /** 加密模式下的密文 */
    private String encrypt;

    /** 企业微信自建应用 AgentId */
    private String agentId;

    /**
     * 解析 XML。
     *
     * @param xml XML 字符串
     * @return 解析结果
     */
    public static WeChatXmlMessage parse(String xml) {
        WeChatXmlMessage message = new WeChatXmlMessage();
        Map<String, String> fields = flatten(xml);
        message.setToUserName(fields.get("ToUserName"));
        message.setFromUserName(fields.get("FromUserName"));
        message.setCreateTime(fields.get("CreateTime"));
        message.setMsgType(fields.get("MsgType"));
        message.setContent(fields.get("Content"));
        message.setMsgId(fields.get("MsgId"));
        message.setEvent(fields.get("Event"));
        message.setEventKey(fields.get("EventKey"));
        message.setEncrypt(fields.get("Encrypt"));
        message.setAgentId(fields.get("AgentID"));
        return message;
    }

    /** 是否为文本消息 */
    public boolean isText() {
        return "text".equalsIgnoreCase(msgType);
    }

    /** 是否为事件推送 */
    public boolean isEvent() {
        return "event".equalsIgnoreCase(msgType);
    }

    /** 是否为关注事件 */
    public boolean isSubscribe() {
        return isEvent() && "subscribe".equalsIgnoreCase(event);
    }

    /** 是否为进入应用事件（企业微信） */
    public boolean isEnterAgent() {
        return isEvent() && "enter_agent".equalsIgnoreCase(event);
    }

    /**
     * 生成被动回复的文本消息 XML（明文）。
     *
     * @param replyContent 回复内容
     * @return 明文 XML
     */
    public String buildTextReply(String replyContent) {
        long now = System.currentTimeMillis() / 1000;
        return "<xml>"
                + "<ToUserName><![CDATA[" + nullToEmpty(fromUserName) + "]]></ToUserName>"
                + "<FromUserName><![CDATA[" + nullToEmpty(toUserName) + "]]></FromUserName>"
                + "<CreateTime>" + now + "</CreateTime>"
                + "<MsgType><![CDATA[text]]></MsgType>"
                + "<Content><![CDATA[" + nullToEmpty(replyContent) + "]]></Content>"
                + "</xml>";
    }

    /**
     * 将被动回复包装成加密模式的 XML。
     *
     * @param plainXml  明文回复 XML
     * @param crypto    加解密工具
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @return 加密后的回复 XML
     */
    public String buildEncryptedReply(String plainXml, WeChatCrypto crypto, String timestamp, String nonce) {
        String encrypted = crypto.encrypt(plainXml);
        String signature = crypto.signature(timestamp, nonce, encrypted);
        return "<xml>"
                + "<Encrypt><![CDATA[" + encrypted + "]]></Encrypt>"
                + "<MsgSignature><![CDATA[" + signature + "]]></MsgSignature>"
                + "<TimeStamp>" + timestamp + "</TimeStamp>"
                + "<Nonce><![CDATA[" + nullToEmpty(nonce) + "]]></Nonce>"
                + "</xml>";
    }

    /** 将 XML 顶层子节点解析为 Map */
    private static Map<String, String> flatten(String xml) {
        Map<String, String> fields = new HashMap<>();
        if (xml == null || xml.isBlank()) {
            return fields;
        }
        try {
            Document document = builder().parse(
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            Element root = document.getDocumentElement();
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    fields.put(node.getNodeName(), node.getTextContent());
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("微信回调 XML 解析失败: " + e.getMessage(), e);
        }
        return fields;
    }

    /** 构建禁用外部实体的安全 DocumentBuilder */
    private static DocumentBuilder builder() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            return factory.newDocumentBuilder();
        } catch (Exception e) {
            throw new IllegalStateException("XML 解析器初始化失败", e);
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
