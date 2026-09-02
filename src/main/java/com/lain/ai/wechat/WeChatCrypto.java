package com.lain.ai.wechat;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * 微信消息加解密与签名校验。
 *
 * <p>企业微信与微信公众号使用的算法完全一致：
 * <ul>
 *   <li>AES 密钥 = Base64Decode(EncodingAESKey + "=")，长度 32 字节；</li>
 *   <li>算法 AES-256-CBC，IV 取密钥前 16 字节，填充方式为 PKCS7；</li>
 *   <li>明文包结构 = 16 字节随机串 + 4 字节网络序消息长度 + 消息体 + ReceiveId
 *       （企业微信为 CorpID，公众号为 AppID）；</li>
 *   <li>签名 = SHA1(字典序排序后拼接的 token、timestamp、nonce、密文)。</li>
 * </ul>
 */
public final class WeChatCrypto {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final String token;
    private final byte[] aesKey;
    private final String receiveId;

    /**
     * @param token          回调 Token
     * @param encodingAesKey 回调 EncodingAESKey（43 位）
     * @param receiveId      企业微信填 CorpID，公众号填 AppID
     */
    public WeChatCrypto(String token, String encodingAesKey, String receiveId) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("微信回调 Token 未配置");
        }
        if (encodingAesKey == null || encodingAesKey.length() != 43) {
            throw new IllegalArgumentException("EncodingAESKey 非法，应为 43 位字符，当前长度："
                    + (encodingAesKey == null ? "null" : encodingAesKey.length()));
        }
        this.token = token;
        this.aesKey = Base64.getDecoder().decode(encodingAesKey + "=");
        if (this.aesKey.length != 32) {
            throw new IllegalArgumentException("EncodingAESKey 解码后长度应为 32 字节，实际：" + this.aesKey.length);
        }
        this.receiveId = receiveId;
    }

    /**
     * 校验回调签名。
     *
     * <p>加密模式下签名包含密文，明文模式下不包含，这里两种都尝试，以兼容不同配置。
     *
     * @param signature 微信传入的签名（企业微信为 msg_signature，公众号为 signature）
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param encrypt   密文，明文模式下传 null
     * @return 校验是否通过
     */
    public boolean verify(String signature, String timestamp, String nonce, String encrypt) {
        if (signature == null || signature.isEmpty()) {
            return false;
        }
        if (encrypt != null && !encrypt.isEmpty()
                && sha1(token, timestamp, nonce, encrypt).equalsIgnoreCase(signature)) {
            return true;
        }
        return sha1(token, timestamp, nonce).equalsIgnoreCase(signature);
    }

    /** 生成签名，用于加密模式的被动回复 */
    public String signature(String timestamp, String nonce, String encrypt) {
        return sha1(token, timestamp, nonce, encrypt);
    }

    /**
     * 解密微信推送的密文。
     *
     * @param encryptedBase64 Base64 密文
     * @return 明文 XML
     */
    public String decrypt(String encryptedBase64) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"),
                    new IvParameterSpec(Arrays.copyOfRange(aesKey, 0, 16)));
            byte[] padded = cipher.doFinal(Base64.getDecoder().decode(encryptedBase64));
            byte[] plain = pkcs7Unpad(padded);

            ByteBuffer buffer = ByteBuffer.wrap(plain);
            byte[] rand = new byte[16];
            buffer.get(rand);
            int length = buffer.getInt();
            byte[] xmlBytes = new byte[length];
            buffer.get(xmlBytes);
            byte[] idBytes = new byte[buffer.remaining()];
            buffer.get(idBytes);

            if (receiveId != null && !receiveId.isEmpty()
                    && !receiveId.equals(new String(idBytes, StandardCharsets.UTF_8))) {
                throw new SecurityException("消息 ReceiveId 校验失败，请检查 appId 配置");
            }
            return new String(xmlBytes, StandardCharsets.UTF_8);
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("微信消息解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 加密明文消息。
     *
     * @param plainXml 明文 XML
     * @return Base64 密文
     */
    public String encrypt(String plainXml) {
        try {
            byte[] rand = new byte[16];
            RANDOM.nextBytes(rand);
            byte[] xmlBytes = plainXml.getBytes(StandardCharsets.UTF_8);
            byte[] idBytes = receiveId == null ? new byte[0] : receiveId.getBytes(StandardCharsets.UTF_8);

            ByteBuffer buffer = ByteBuffer.allocate(rand.length + 4 + xmlBytes.length + idBytes.length);
            buffer.put(rand).putInt(xmlBytes.length).put(xmlBytes).put(idBytes);

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"),
                    new IvParameterSpec(Arrays.copyOfRange(aesKey, 0, 16)));
            return Base64.getEncoder().encodeToString(cipher.doFinal(pkcs7Pad(buffer.array())));
        } catch (Exception e) {
            throw new IllegalStateException("微信消息加密失败: " + e.getMessage(), e);
        }
    }

    private static byte[] pkcs7Pad(byte[] data) {
        int blockSize = 32;
        int pad = blockSize - (data.length % blockSize);
        if (pad == 0) {
            pad = blockSize;
        }
        byte[] result = Arrays.copyOf(data, data.length + pad);
        Arrays.fill(result, data.length, result.length, (byte) pad);
        return result;
    }

    private static byte[] pkcs7Unpad(byte[] data) {
        int pad = data[data.length - 1] & 0xFF;
        if (pad < 1 || pad > 32 || pad > data.length) {
            throw new IllegalStateException("PKCS7 填充非法");
        }
        return Arrays.copyOf(data, data.length - pad);
    }

    private static String sha1(String... values) {
        try {
            String[] sorted = Arrays.stream(values)
                    .filter(v -> v != null && !v.isEmpty())
                    .sorted()
                    .toArray(String[]::new);
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(String.join("", sorted).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA1 计算失败", e);
        }
    }
}
