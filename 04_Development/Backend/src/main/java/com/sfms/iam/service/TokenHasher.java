package com.sfms.iam.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Sinh và băm token opaque (refresh / activation / reset).
 *
 * <p>Ba token này <b>chỉ lưu hash</b> trong DB (ADR-M02). Khác với mật khẩu (dùng BCrypt,
 * cố tình chậm), token là chuỗi ngẫu nhiên ≥256-bit nên SHA-256 là đủ và cho phép tra
 * bằng {@code WHERE token_hash = ?} — dùng BCrypt sẽ buộc quét toàn bảng.
 *
 * <p>MF01-TC-009 / TC-019 assert: {@code SELECT count(*) WHERE token_hash = :rawToken} ⇒ 0.
 */
@Component
public class TokenHasher {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;   // 256-bit (ADR-M01)

    /** Chuỗi thô — chỉ đi tới client (response/email), không bao giờ vào DB hay log. */
    public String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** SHA-256 → Base64 (≤ 88 ký tự, khớp {@code VARCHAR(88)} của V1). */
    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] out = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(out);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 không khả dụng trên JVM này", e);
        }
    }
}
