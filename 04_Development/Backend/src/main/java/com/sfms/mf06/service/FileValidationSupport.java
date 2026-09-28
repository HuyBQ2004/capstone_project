package com.sfms.mf06.service;

import com.sfms.mf06.error.IngError;
import com.sfms.mf06.error.IngMessages;
import com.sfms.common.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * D10-BIS.1 — whitelist upload theo NỘI DUNG THẬT của file (magic bytes), không tin đuôi
 * file/{@code Content-Type} khai báo (chặn SVG/XSS, file đổi tên).
 *
 * <p>⚠️ Trùng lặp có chủ đích với {@code ProfileService} (MF01) / {@code FileValidationSupport}
 * (MF04) — TDS-MF06 §13 TC-SEC-016 Implementation Note: MF06 là module ĐẦU TIÊN của Sóng 1 cần
 * upload nên "nên" rút thành {@code common/storage/FileValidator}. Việc tách đó KHÔNG được thực
 * hiện ở đây trong phạm vi Bước 2 (rủi ro sửa code module khác ngoài phạm vi giao); giữ nguyên
 * tiền lệ MF04 (bản sao độc lập) — ghi rõ làm nợ kỹ thuật cho lần refactor common/ kế tiếp.
 */
@Component("mf06FileValidationSupport")
public class FileValidationSupport {

    public static final long DOCUMENT_MAX_BYTES = 10 * 1024 * 1024L;

    public static final Set<String> DOCUMENT_CONTENT_TYPES =
            Set.of("application/pdf", "image/jpeg", "image/png");

    private static final byte[] MAGIC_JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] MAGIC_PNG = {(byte) 0x89, 'P', 'N', 'G'};
    private static final byte[] MAGIC_PDF = {'%', 'P', 'D', 'F'};

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw unsupportedType();
        }
        if (file.getSize() > DOCUMENT_MAX_BYTES) {
            throw new ApiException(IngError.FILE_TOO_LARGE, HttpStatus.BAD_REQUEST, IngMessages.MSG_008);
        }
        if (!DOCUMENT_CONTENT_TYPES.contains(file.getContentType()) || !hasAllowedMagicBytes(file)) {
            throw unsupportedType();
        }
    }

    private ApiException unsupportedType() {
        return new ApiException(IngError.UNSUPPORTED_FILE_TYPE, HttpStatus.BAD_REQUEST, IngMessages.MSG_009);
    }

    /** Chặn SVG (XSS) và file đổi tên — quyết định theo nội dung thật, không theo đuôi/Content-Type. */
    private boolean hasAllowedMagicBytes(MultipartFile file) {
        try {
            byte[] head = new byte[8];
            int read = file.getInputStream().read(head);
            if (read < 4) {
                return false;
            }
            if (startsWith(head, MAGIC_PDF)) {
                return DOCUMENT_CONTENT_TYPES.contains("application/pdf");
            }
            if (startsWith(head, MAGIC_JPEG)) {
                return DOCUMENT_CONTENT_TYPES.contains("image/jpeg");
            }
            if (startsWith(head, MAGIC_PNG)) {
                return DOCUMENT_CONTENT_TYPES.contains("image/png");
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
