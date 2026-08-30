package com.lain.modules.xianyu.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 闲鱼工具类（对应 Python 版 utils/xianyu_utils.py）
 */
public final class XianyuUtils {

    private static final Logger log = LoggerFactory.getLogger(XianyuUtils.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 签名使用的 appKey */
    private static final String SIGN_APP_KEY = "34839810";

    /** 设备ID字符集 */
    private static final String DEVICE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private XianyuUtils() {
    }

    /**
     * 解析 cookie 字符串为字典
     */
    public static Map<String, String> transCookies(String cookiesStr) {
        Map<String, String> cookies = new LinkedHashMap<>();
        if (cookiesStr == null || cookiesStr.isBlank()) {
            return cookies;
        }
        for (String cookie : cookiesStr.split("; ")) {
            int idx = cookie.indexOf('=');
            if (idx > 0) {
                cookies.put(cookie.substring(0, idx), cookie.substring(idx + 1));
            }
        }
        return cookies;
    }

    /**
     * 生成 mid
     */
    public static String generateMid() {
        int randomPart = ThreadLocalRandom.current().nextInt(1000);
        long timestamp = System.currentTimeMillis();
        return randomPart + "" + timestamp + " 0";
    }

    /**
     * 生成 uuid
     */
    public static String generateUuid() {
        long timestamp = System.currentTimeMillis();
        return "-" + timestamp + "1";
    }

    /**
     * 生成设备ID（UUID v4 风格 + 用户ID）
     */
    public static String generateDeviceId(String userId) {
        StringBuilder result = new StringBuilder(36);
        for (int i = 0; i < 36; i++) {
            if (i == 8 || i == 13 || i == 18 || i == 23) {
                result.append('-');
            } else if (i == 14) {
                result.append('4');
            } else if (i == 19) {
                int randVal = ThreadLocalRandom.current().nextInt(16);
                result.append(DEVICE_CHARS.charAt((randVal & 0x3) | 0x8));
            } else {
                int randVal = ThreadLocalRandom.current().nextInt(16);
                result.append(DEVICE_CHARS.charAt(randVal));
            }
        }
        return result + "-" + userId;
    }

    /**
     * 生成签名（MD5: token&t&appKey&data）
     */
    public static String generateSign(String t, String token, String data) {
        String msg = token + "&" + t + "&" + SIGN_APP_KEY + "&" + data;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(msg.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 算法不可用", e);
        }
    }

    /**
     * 解密函数（base64 解码 + MessagePack 解码，失败时逐级回退）
     */
    public static String decrypt(String data) {
        try {
            // 1. 清理非 base64 字符并补齐 padding
            String cleaned = data.replaceAll("[^A-Za-z0-9+/=]", "");
            while (cleaned.length() % 4 != 0) {
                cleaned += "=";
            }

            byte[] decodedBytes;
            try {
                decodedBytes = Base64.getDecoder().decode(cleaned);
            } catch (IllegalArgumentException e) {
                return toJson(Map.of("error", "Base64 decode failed: " + e.getMessage(), "raw_data", data));
            }

            // 2. 尝试 MessagePack 解码
            try {
                MessagePackDecoder decoder = new MessagePackDecoder(decodedBytes);
                Object result = decoder.decode();
                // 3. 转换为 JSON 字符串
                return MAPPER.writeValueAsString(result);
            } catch (Exception e) {
                // 4. MessagePack 解码失败，尝试直接解析为字符串
                try {
                    String textResult = new String(decodedBytes, StandardCharsets.UTF_8);
                    return MAPPER.writeValueAsString(Map.of("text", textResult));
                } catch (Exception ex) {
                    // 5. 最后的备选方案：返回十六进制表示
                    StringBuilder hex = new StringBuilder(decodedBytes.length * 2);
                    for (byte b : decodedBytes) {
                        hex.append(String.format("%02x", b));
                    }
                    return toJson(Map.of("hex", hex.toString(), "error", "Decode failed: " + e.getMessage()));
                }
            }
        } catch (Exception e) {
            return toJson(Map.of("error", "Decrypt failed: " + e.getMessage(), "raw_data", data));
        }
    }

    private static String toJson(Map<String, Object> map) {
        try {
            return MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("JSON 序列化失败: {}", e.getMessage());
            return "{}";
        }
    }
}
