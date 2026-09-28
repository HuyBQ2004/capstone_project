package com.sfms.mf04.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * ADR-M04-5 / PDPD — mã hóa CCCD nhân viên giao hàng at-rest bằng AES-256-GCM.
 * {@code national_id_hash} (SHA-256) enforce UNIQUE (BR-050) mà KHÔNG cần giải mã.
 *
 * <p>Số CCCD thật KHÔNG BAO GIỜ được trả ra API — chỉ dạng che (xem
 * {@link DeliveryPersonServiceImpl#mask(String)}). {@link #decrypt} chỉ dùng nội bộ, hiện chưa
 * có endpoint nào gọi tới (không có UC nào yêu cầu hiển thị CCCD đầy đủ).
 */
@Component
public class NationalIdCipher {

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public NationalIdCipher(@Value("${sfms.mf04.national-id-key}") String base64Key) {
        this.key = new SecretKeySpec(Base64.getDecoder().decode(base64Key), "AES");
    }

    /** @return {@code IV (12 byte) || ciphertext+tag} — tự chứa, không cần cột IV riêng. */
    public byte[] encrypt(String plainText) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(cipherText, 0, out, iv.length, cipherText.length);
            return out;
        } catch (Exception e) {
            throw new IllegalStateException("Không mã hóa được CCCD", e);
        }
    }

    public String decrypt(byte[] stored) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(stored, 0, iv, 0, GCM_IV_LENGTH);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] plain = cipher.doFinal(stored, GCM_IV_LENGTH, stored.length - GCM_IV_LENGTH);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Không giải mã được CCCD", e);
        }
    }

    /** BR-050 — SHA-256 hex (64 ký tự), enforce UNIQUE mà không cần giải mã. */
    public String hash(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 không khả dụng", e);
        }
    }
}
