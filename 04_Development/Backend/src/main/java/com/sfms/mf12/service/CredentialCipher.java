package com.sfms.mf12.service;

import com.sfms.mf12.error.AisExceptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * BR-164 — mã hoá envelope AES-256-GCM cho credential dịch vụ ngoài, khoá lấy từ ENV
 * {@code SFMS_CRED_MASTER_KEY} (base64, 32 byte). KHÔNG BAO GIỜ lưu plaintext.
 *
 * <p>Thiếu ENV (dev/test chưa cấu hình) ⇒ dùng khoá dev cố định, ghi rõ KHÔNG dùng cho
 * production — {@code TDS §11.1} liệt biến này là prerequisite.
 */
@Component
public class CredentialCipher {

    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public CredentialCipher(@Value("${SFMS_CRED_MASTER_KEY:MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=}")
                             String base64Key) {
        this.key = new SecretKeySpec(Base64.getDecoder().decode(base64Key), "AES");
    }

    public Encrypted encrypt(String plaintext) {
        return encryptBytes(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    public String decrypt(byte[] ciphertext, byte[] iv) {
        return new String(decryptBytes(ciphertext, iv), StandardCharsets.UTF_8);
    }

    /** BR-160/161 — cùng khoá {@code SFMS_CRED_MASTER_KEY}, dùng cho file backup (byte[] thay String). */
    public Encrypted encryptBytes(byte[] plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext);
            return new Encrypted(ciphertext, iv);
        } catch (GeneralSecurityException e) {
            throw AisExceptions.credentialDecryptFailed();
        }
    }

    public byte[] decryptBytes(byte[] ciphertext, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return cipher.doFinal(ciphertext);
        } catch (GeneralSecurityException e) {
            throw AisExceptions.credentialDecryptFailed();
        }
    }

    public record Encrypted(byte[] ciphertext, byte[] iv) {}
}
