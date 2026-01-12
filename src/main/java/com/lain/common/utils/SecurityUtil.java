package com.lain.common.utils;

import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

public class SecurityUtil {

    /**
     * BCrypt加密密码
     * @param password 明文密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * 验证密码
     * @param password 明文密码
     * @param hashedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    /**
     * 使用SHA256加密（备用方案）
     * @param password 明文密码
     * @param salt 盐值
     * @return 加密后的密码
     */
    public static String encryptPasswordWithSalt(String password, String salt) {
        Digester digester = new Digester(DigestAlgorithm.SHA256);
        return digester.digestHex(password + salt);
    }

    public static void main(String[] args) {
        System.out.println(encryptPassword("admin123"));
    }
}
