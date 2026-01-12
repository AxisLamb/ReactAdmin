package com.lain.xhstest;

import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * X-S-Common 解码和分析工具
 *
 * 用于解码和分析X-S-Common header值，显示其内部JSON结构
 */
public class XSCommonDecoder {

    /**
     * 自定义Base64字符表（与编码时使用的相同）
     */
    private static final String BASE64_CHARS = "ZmserbBoHQtNP+wOcza/LpngG8yJq42KWYj0DSfdikx3VT16IlUAFM97hECvuRX5";

    /**
     * 自定义Base64解码
     *
     * @param encoded Base64编码的字符串
     * @return 解码后的字节数组
     */
    public static byte[] customBase64Decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return new byte[0];
        }

        // 移除填充字符
        String clean = encoded.replace("=", "");
        int length = clean.length();

        // 计算输出字节数组长度
        int byteLength = (length * 3) / 4;
        byte[] result = new byte[byteLength];

        int resultIndex = 0;
        for (int i = 0; i < length; i += 4) {
            // 获取4个字符的索引值
            int val1 = BASE64_CHARS.indexOf(clean.charAt(i));
            int val2 = (i + 1 < length) ? BASE64_CHARS.indexOf(clean.charAt(i + 1)) : 0;
            int val3 = (i + 2 < length) ? BASE64_CHARS.indexOf(clean.charAt(i + 2)) : 0;
            int val4 = (i + 3 < length) ? BASE64_CHARS.indexOf(clean.charAt(i + 3)) : 0;

            if (val1 == -1 || val2 == -1 || val3 == -1 || val4 == -1) {
                throw new IllegalArgumentException("Invalid Base64 character at position " + i);
            }

            // 组合24位值
            int combined = (val1 << 18) | (val2 << 12) | (val3 << 6) | val4;

            // 提取3个字节
            if (resultIndex < byteLength) {
                result[resultIndex++] = (byte) ((combined >> 16) & 0xFF);
            }
            if (resultIndex < byteLength && i + 2 < length) {
                result[resultIndex++] = (byte) ((combined >> 8) & 0xFF);
            }
            if (resultIndex < byteLength && i + 3 < length) {
                result[resultIndex++] = (byte) (combined & 0xFF);
            }
        }

        return result;
    }

    /**
     * 解码X-S-Common值
     *
     * @param xsCommon X-S-Common header值
     * @return 解码后的JSON字符串
     */
    public static String decode(String xsCommon) {
        try {
            // 1. Base64解码（使用自定义字符表）
            byte[] decodedBytes = customBase64Decode(xsCommon);

            // 2. UTF-8解码
            String json = new String(decodedBytes, StandardCharsets.UTF_8);

            return json;
        } catch (Exception e) {
            throw new RuntimeException("解码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 分析X-S-Common值并打印详细信息
     *
     * @param xsCommon X-S-Common header值
     */
    public static void analyze(String xsCommon) {
        try {
            System.out.println("=".repeat(80));
            System.out.println("X-S-Common 解码和分析报告");
            System.out.println("=".repeat(80));
            System.out.println();

            // 1. 显示原始值（前100个字符）
            System.out.println("原始值（前100字符）:");
            String preview = xsCommon.length() > 100 ? xsCommon.substring(0, 100) + "..." : xsCommon;
            System.out.println(preview);
            System.out.println("总长度: " + xsCommon.length() + " 字符");
            System.out.println();

            // 2. 解码
            String json = decode(xsCommon);
            System.out.println("解码后的JSON:");
            System.out.println("-".repeat(80));
            System.out.println(json);
            System.out.println("-".repeat(80));
            System.out.println();

            // 3. 解析JSON并分析各个字段
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            System.out.println("字段分析:");
            System.out.println("-".repeat(80));

            // s0: 平台标识
            if (root.has("s0")) {
                System.out.println("s0 (平台标识): " + root.get("s0").asText());
            }

            // s1
            if (root.has("s1")) {
                String s1 = root.get("s1").asText();
                System.out.println("s1: " + (s1.isEmpty() ? "(空)" : s1));
            }

            // x0
            if (root.has("x0")) {
                String x0 = root.get("x0").asText();
                System.out.println("x0 (localStorage值): " + (x0.isEmpty() ? "(空)" : x0));
            }

            // x1
            if (root.has("x1")) {
                String x1 = root.get("x1").asText();
                System.out.println("x1 (常量值): " + (x1.isEmpty() ? "(空)" : x1));
            }

            // x2: 平台类型
            if (root.has("x2")) {
                System.out.println("x2 (平台类型): " + root.get("x2").asText());
            }

            // x3: 应用标识
            if (root.has("x3")) {
                System.out.println("x3 (应用标识): " + root.get("x3").asText());
            }

            // x4: 应用版本
            if (root.has("x4")) {
                System.out.println("x4 (应用版本): " + root.get("x4").asText());
            }

            // x5
            if (root.has("x5")) {
                String x5 = root.get("x5").asText();
                System.out.println("x5 (存储值): " + (x5.isEmpty() ? "(空)" : x5));
            }

            // x6
            if (root.has("x6")) {
                String x6 = root.get("x6").asText();
                System.out.println("x6 (签名相关): " + (x6.isEmpty() ? "(空)" : x6));
            }

            // x7
            if (root.has("x7")) {
                String x7 = root.get("x7").asText();
                System.out.println("x7 (签名相关): " + (x7.isEmpty() ? "(空)" : x7));
            }

            // x8: 设备ID
            if (root.has("x8")) {
                System.out.println("x8 (设备ID): " + root.get("x8").asText());
            }

            // x9: CRC32哈希值
            if (root.has("x9")) {
                System.out.println("x9 (CRC32哈希值): " + root.get("x9").asText());
            }

            // x10: 签名计数
            if (root.has("x10")) {
                System.out.println("x10 (签名计数): " + root.get("x10").asInt());
            }

            // x11: 状态
            if (root.has("x11")) {
                System.out.println("x11 (状态): " + root.get("x11").asText());
            }

            System.out.println("-".repeat(80));
            System.out.println();

            // 4. 字段统计
            System.out.println("统计信息:");
            System.out.println("-".repeat(80));
            System.out.println("JSON字符串长度: " + json.length() + " 字符");
            System.out.println("字段总数: " + root.size());
            System.out.println();

            // 5. 格式化JSON（美观输出）
            System.out.println("格式化JSON:");
            System.out.println("-".repeat(80));
            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            System.out.println(prettyJson);
            System.out.println("-".repeat(80));

        } catch (Exception e) {
            System.err.println("分析失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 主函数 - 测试和演示
     */
    public static void main(String[] args) {
        // 用户提供的X-S-Common值
        String xsCommon = """
            2UQAPsHC+aIjqArjwjHjNsQhPsHCH0rjNsQhPaHCH0c1Pjh9HjIj2eHjwjQgynEDJ74AHjIj2ePjwjQhyoPTqBPT49pjHjIj2ecjwjHMN0r1PaHVHdWMH0ijP/SjPf+S+0DFPebSPn+kJ9+dye462fTdPgm6+/Y149Wl+BpdGgGF+APMPeZIPecIP0qA+UHVHdW9H0ijHjIj2eqjwjHjNsQhwsHCHDDAwoQH8B4AyfRI8FS98g+Dpd4daLP3JFSb/BMsn0pSPM87nrldzSzQ2bPAGdb7zgQB8nph8emSy9E0cgk+zSS1qgzianYt8p+s/LzN4gzaa/+NqMS6qS4HLozoqfQnPbZEp98QyaRSp9P98pSl4oSzcgmca/P78nTTL08z/sVManD9q9z1J9p/8db8aob7JeQl4epsPrz6agW3Lr4ryaRApdz3agYDq7YM47HFqgzkanYMGLSbP9LA/bGIa/+nprSe+9LI4gzVPDbrJg+P4fprLFTALMm7+LSb4d+kpdzt/7b7wrQM498cqBzSpr8g/FSh+bzQygL9nSm7qSmM4epQ4flY/BQdqA+l4oYQ2BpAPp87arS34nMQyFSE8nkdqMD6pMzd8/4SL7bF8aRr+7+rG7mkqBpD8pSUzozQcA8Szb87PDSb/d+/qgzVJfl/4LExpdzQ4fRSy7bFP9+y+7+nJAzdaLp/2LSiz/QHJdbMagYiJdbCwB4QyFSfJ7b7yFSenS4o+A+A8BlO8p8c4A+Q4DbSPB8d8ncILdbQy/pAPFSj80QM4rbQyLTAynz98nTy/fpLLocFJDbO8p4c4FpQ4S+1G9pD8p+M4FbdwLkALMm7qFDAy7QQ2rLM/op749bl4UTU8nTinDbw8/b+/fLILoqEaL+wqM8PJ9p/GDSBanT6qM+U+7+nJD8kanTdqM8n4rMQygpDqgb7t7zl4b4QPAmSPMm7aLSiJ9LA4gclanSOq9kM4e+74gz1qMm7nrSeG9lQPFSUP04VyAQQ+nLl4gzeaLp/NFSbadPILoz1qbSQcLuIafp88DclaLpULrRc4rT6qgqAa/+O8gYl4b4z/epSyn+mqA+Iyo4QyBRAPASOqA+M4o+P4g47ag8N8n8n498Qy94A+0mgqDSea9pDJURSpM8FPFDA+9pnqg4fwrQ8qDSiLaTQP94SyM49qMzS8oPInf4SPob7JDSbLS4Q4fSVqop7nDSharRQynRSPLS/pAzd/9phLoqhanSyGLlc4MYz/LTSpMSDqAbn4ok1Loz7a/+nyLSbtA+A4gzO/Mm7qFSh+g+knjRS+S87Jdbl4bbQznlYJ7bF+DDALbkUnpi6anDAqMq7ad+r8sRSzob7qFYl4BEQyp46anDMqA+M4r+Qz/4Snp+b8DSeLMpUGUTlagGF4rSicnpk4g4xGMmFLrRl4rMQ2B+GaLpmqM8S/9pnLoqAaS+0pDSeyLIUqgc3ndbF8LDAzAbQP9QlanD3GLSicnp3LFRAPgHFpUTc4URQ4S4Qqob7+fQn4FSQyo8S2BziyrSh4/pY+A8Spb8F4DYc4rQyagQHJ98UzLS3nLG64g4EanTDqA8p2f8QPFp64b874rS3LfRH4gc7qdpFJFSi4/pQyezwaLPhG9pM4F+Qygr6anW7qM+l4URUa/W9aLpl/FSb+g+LJDkAP7b7JrS9204QzpGIJS4U8FSiJrSdJpiIag8w8/mUn0FjNsQhwaHCN/rMPerEPAWU+0HVHdWlPsHCPsIj2erlH0ijJfRUJnbVHdF=
        """;

        analyze(xsCommon.trim());

    }
}