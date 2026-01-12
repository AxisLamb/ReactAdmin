package com.lain.xhstest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * X-S-Common Header 生成器 (Java实现)
 *
 * 基于小红书 Web 端的 X-S-Common 算法实现
 *
 * 注意：部分常量值需要通过实际运行环境获取或逆向分析确定
 */
public class XSCommonGenerator {

    // ========== 常量定义（需要根据实际情况调整）==========

    /**
     * 自定义Base64字符表（非标准Base64）
     * 来源：vendor-dynamic.0ad1ffae.js 第9973行
     */
    private static final String BASE64_CHARS = "ZmserbBoHQtNP+wOcza/LpngG8yJq42KWYj0DSfdikx3VT16IlUAFM97hECvuRX5";

    /**
     * CRC32多项式
     */
    private static final int CRC32_POLYNOMIAL = 0xedb88320;

    /**
     * localStorage键名（需要通过实际运行环境获取）
     */
    private static final String STORAGE_KEY_DEVICE_ID = "device_id"; // u.q2 的实际值
    private static final String STORAGE_KEY_X0 = "x0_key"; // u.z7 的实际值
    private static final String STORAGE_KEY_X5 = "x5_key"; // u.o4 的实际值

    /**
     * 默认值（需要通过实际运行环境获取）
     */
    private static final String DEFAULT_X0 = ""; // u.fI 的实际值
    private static final String DEFAULT_X1 = ""; // u.i8 的实际值

    /**
     * 应用信息
     */
    private static final String APP_IDENTIFIER = "xhs-pc-web";
    private static final String APP_VERSION = "5.1.1";

    // ========== 存储模拟（实际应用中应使用持久化存储）==========

    private static final Map<String, String> localStorage = new HashMap<>();
    private static final Map<String, AtomicInteger> sessionStorage = new HashMap<>();

    // ========== CRC32 实现 ==========

    /**
     * CRC32查找表
     */
    private static final int[] CRC32_TABLE = new int[256];

    static {
        // 初始化CRC32查找表
        for (int i = 0; i < 256; i++) {
            int crc = i;
            for (int j = 0; j < 8; j++) {
                crc = (crc & 1) == 1 ? (crc >>> 1) ^ CRC32_POLYNOMIAL : crc >>> 1;
            }
            CRC32_TABLE[i] = crc;
        }
    }

    /**
     * 计算字符串的CRC32值
     *
     * @param input 输入字符串
     * @return CRC32哈希值（32位整数）
     */
    public static int crc32(String input) {
        int crc = -1;
        for (int i = 0; i < input.length(); i++) {
            int index = (crc ^ input.charAt(i)) & 0xFF;
            crc = (crc >>> 8) ^ CRC32_TABLE[index];
        }
        return (~crc) ^ CRC32_POLYNOMIAL;
    }

    /**
     * 计算字节数组的CRC32值
     *
     * @param input 输入字节数组
     * @return CRC32哈希值（32位整数）
     */
    public static int crc32(byte[] input) {
        int crc = -1;
        for (byte b : input) {
            int index = (crc ^ (b & 0xFF)) & 0xFF;
            crc = (crc >>> 8) ^ CRC32_TABLE[index];
        }
        return (~crc) ^ CRC32_POLYNOMIAL;
    }

    // ========== UTF-8编码实现 ==========

    /**
     * 将字符串编码为UTF-8字节数组
     * 模拟JavaScript的encodeUtf8函数
     *
     * @param input 输入字符串
     * @return UTF-8字节数组
     */
    public static byte[] encodeUtf8(String input) {
        // Java中直接使用UTF-8编码即可
        return input.getBytes(StandardCharsets.UTF_8);
    }

    // ========== 自定义Base64编码实现 ==========

    /**
     * 将字节数组编码为自定义Base64字符串
     *
     * @param input 输入字节数组
     * @return Base64编码字符串
     */
    public static String customBase64Encode(byte[] input) {
        if (input == null || input.length == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        int length = input.length;
        int remainder = length % 3;
        int chunkSize = 16383; // 分块大小

        // 处理完整块
        for (int i = 0; i < length - remainder; i += chunkSize) {
            int end = Math.min(i + chunkSize, length - remainder);
            result.append(encodeChunk(input, i, end));
        }

        // 处理剩余字节
        if (remainder == 1) {
            int lastByte = input[length - 1] & 0xFF;
            result.append(BASE64_CHARS.charAt(lastByte >> 2));
            result.append(BASE64_CHARS.charAt((lastByte << 4) & 63));
            result.append("==");
        } else if (remainder == 2) {
            int byte1 = input[length - 2] & 0xFF;
            int byte2 = input[length - 1] & 0xFF;
            int combined = (byte1 << 8) | byte2;
            result.append(BASE64_CHARS.charAt(combined >> 10));
            result.append(BASE64_CHARS.charAt((combined >> 4) & 63));
            result.append(BASE64_CHARS.charAt((combined << 2) & 63));
            result.append("=");
        }

        return result.toString();
    }

    /**
     * 编码一个数据块（每3个字节一组）
     */
    private static String encodeChunk(byte[] input, int start, int end) {
        StringBuilder chunk = new StringBuilder();
        for (int i = start; i < end; i += 3) {
            int byte1 = input[i] & 0xFF;
            int byte2 = (i + 1 < end) ? (input[i + 1] & 0xFF) : 0;
            int byte3 = (i + 2 < end) ? (input[i + 2] & 0xFF) : 0;

            int combined = (byte1 << 16) | (byte2 << 8) | byte3;

            chunk.append(BASE64_CHARS.charAt((combined >> 18) & 63));
            chunk.append(BASE64_CHARS.charAt((combined >> 12) & 63));
            chunk.append(BASE64_CHARS.charAt((combined >> 6) & 63));
            chunk.append(BASE64_CHARS.charAt(combined & 63));
        }
        return chunk.toString();
    }

    // ========== 签名计数管理 ==========

    /**
     * 获取并递增签名计数
     * 模拟JavaScript的getSigCount函数
     *
     * @param hasSignature 是否存在签名
     * @return 签名计数
     */
    private static int getSigCount(boolean hasSignature) {
        String key = "sig_count"; // u.DY 的实际值
        AtomicInteger count = sessionStorage.computeIfAbsent(key, k -> new AtomicInteger(0));

        if (hasSignature) {
            return count.incrementAndGet();
        }
        return count.get();
    }

    // ========== 平台标识生成 ==========

    /**
     * 生成平台标识
     * 模拟JavaScript的 f.SW(d) 函数
     *
     * @param platform 平台类型
     * @return 平台标识字符串
     */
    private static String generatePlatformId(String platform) {
        // 实际实现需要根据 f.SW 函数的逻辑
        // 这里提供一个简单的示例
        if (platform == null || platform.isEmpty()) {
            return "PC";
        }
        return platform.toUpperCase();
    }

    // ========== 主函数：生成X-S-Common Header ==========

    /**
     * 生成X-S-Common Header值
     *
     * @param platform 平台类型（如 "PC"）
     * @param url 请求URL（用于判断是否需要特殊处理）
     * @param xSign X-Sign header值（可选）
     * @return X-S-Common header值
     */
    public static String generateXSCommon(String platform, String url, String xSign) {
        try {
            // 1. 检查URL是否需要处理（实际应使用 u.yl 和 f.hF 进行判断）
            // 这里简化处理，实际应用中需要实现URL过滤逻辑

            // 2. 获取设备ID和存储值
            String deviceId = localStorage.getOrDefault(STORAGE_KEY_DEVICE_ID, "");
            String x0 = localStorage.getOrDefault(STORAGE_KEY_X0, DEFAULT_X0);
            String x5 = localStorage.getOrDefault(STORAGE_KEY_X5, "");

            // 3. 签名相关（通常为空）
            String x6 = "";
            String x7 = "";
            boolean hasSignature = (xSign != null && !xSign.isEmpty());

            // 4. 计算签名计数
            int sigCount = getSigCount(hasSignature);

            // 5. 计算CRC32哈希值
            String hashInput = x6 + x7 + deviceId;
            int crc32Value = crc32(hashInput);
            String x9 = String.valueOf(crc32Value);

            // 6. 构建数据对象
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode data = mapper.createObjectNode();

            data.put("s0", generatePlatformId(platform));
            data.put("s1", "");
            data.put("x0", x0);
            data.put("x1", DEFAULT_X1);
            data.put("x2", platform != null ? platform : "PC");
            data.put("x3", APP_IDENTIFIER);
            data.put("x4", APP_VERSION);
            data.put("x5", x5);
            data.put("x6", x6);
            data.put("x7", x7);
            data.put("x8", deviceId);
            data.put("x9", x9);
            data.put("x10", sigCount);
            data.put("x11", "normal");

            // 7. JSON序列化
            String json = mapper.writeValueAsString(data);

            // 8. UTF-8编码
            byte[] utf8Bytes = encodeUtf8(json);

            // 9. Base64编码（使用自定义字符表）
            String base64 = customBase64Encode(utf8Bytes);

            return base64;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 简化版本：使用默认参数生成
     */
    public static String generateXSCommon(String platform) {
        return generateXSCommon(platform, "", null);
    }

    // ========== 存储管理方法 ==========

    /**
     * 设置设备ID（模拟localStorage.setItem）
     */
    public static void setDeviceId(String deviceId) {
        localStorage.put(STORAGE_KEY_DEVICE_ID, deviceId);
    }

    /**
     * 获取设备ID（模拟localStorage.getItem）
     */
    public static String getDeviceId() {
        return localStorage.getOrDefault(STORAGE_KEY_DEVICE_ID, "");
    }

    /**
     * 设置x0值
     */
    public static void setX0(String value) {
        localStorage.put(STORAGE_KEY_X0, value);
    }

    /**
     * 设置x5值
     */
    public static void setX5(String value) {
        localStorage.put(STORAGE_KEY_X5, value);
    }

    /**
     * 重置签名计数
     */
    public static void resetSigCount() {
        sessionStorage.remove("sig_count");
    }

    // ========== 测试方法 ==========

    public static void main(String[] args) {
        // 设置测试数据
        setDeviceId("test-device-id-12345");
        setX0("test-x0-value");
        setX5("test-x5-value");

        // 生成X-S-Common header
        String header = generateXSCommon("PC", "https://edith.xiaohongshu.com/api/sns/web/v1/homefeed", null);

        System.out.println("Generated X-S-Common Header:");
        System.out.println(header);
        System.out.println();

        // 测试CRC32
        String testString = "test123";
        int crc = crc32(testString);
        System.out.println("CRC32 of \"" + testString + "\": " + crc);

        // 测试Base64编码
        byte[] testBytes = "Hello World".getBytes(StandardCharsets.UTF_8);
        String base64 = customBase64Encode(testBytes);
        System.out.println("Custom Base64 of \"Hello World\": " + base64);
    }
}


