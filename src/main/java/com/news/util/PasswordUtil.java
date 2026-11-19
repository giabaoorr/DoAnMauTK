package com.news.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Tiện ích băm và xác thực mật khẩu an toàn bằng SHA-256 kèm chuỗi Salt bí mật.
 */
public final class PasswordUtil {

    private static final String SALT = "NewsReader@DesignPattern_Salt2026!";

    private PasswordUtil() {
    }

    /**
     * Băm mật khẩu dạng văn bản thuần thành chuỗi mã hóa Hex SHA-256 kèm Salt.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest((plainPassword + SALT).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Thuật toán SHA-256 không được hỗ trợ", e);
        }
    }

    /**
     * Xác thực mật khẩu người dùng nhập vào so với chuỗi băm trong CSDL.
     */
    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        return hashPassword(rawPassword).equals(hashedPassword);
    }
}
