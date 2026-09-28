package com.sfms.mf04.service;

import com.sfms.mf04.error.SupExceptions;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * C7/D10-BIS.1 — whitelist upload theo <b>nội dung thật của file</b> (magic bytes), không tin
 * đuôi file/{@code Content-Type} khai báo. Dùng chung cho tài liệu tuân thủ (≤10MB,
 * PDF/JPG/PNG) và ảnh khuôn mặt (≤5MB, JPG/PNG).
 *
 * <p>⚠️ Trùng lặp có chủ đích với {@code ProfileService} (MF01) — việc tách thành
 * {@code common/storage/FileValidator} là "việc tồn đọng" ghi ở
 * {@code 00_Common_Conventions.md} §D10-BIS mục 4, chưa có ai làm; MF04 theo đúng tiền lệ hiện
 * tại thay vì tự ý refactor code của module khác.
 */
@Component
public class FileValidationSupport {

    public static final long DOCUMENT_MAX_BYTES = 10 * 1024 * 1024L;
    public static final long FACE_IMAGE_MAX_BYTES = 5 * 1024 * 1024L;

    public static final Set<String> DOCUMENT_CONTENT_TYPES =
            Set.of("application/pdf", "image/jpeg", "image/png");
    public static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    private static final byte[] MAGIC_JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] MAGIC_PNG = {(byte) 0x89, 'P', 'N', 'G'};
    private static final byte[] MAGIC_PDF = {'%', 'P', 'D', 'F'};

    /** @throws com.sfms.common.error.ApiException SUP-008 (413) hoặc SUP-009 (415). */
    public void validate(MultipartFile file, long maxBytes, Set<String> allowedTypes) {
        if (file == null || file.isEmpty()) {
            throw SupExceptions.unsupportedFileType();
        }
        if (file.getSize() > maxBytes) {
            throw SupExceptions.fileTooLarge();
        }
        if (!allowedTypes.contains(file.getContentType()) || !hasAllowedMagicBytes(file, allowedTypes)) {
            throw SupExceptions.unsupportedFileType();
        }
    }

    /** Chặn SVG (XSS) và file đổi tên — quyết định theo nội dung thật, không theo đuôi/Content-Type. */
    private boolean hasAllowedMagicBytes(MultipartFile file, Set<String> allowedTypes) {
        try {
            byte[] head = new byte[8];
            int read = file.getInputStream().read(head);
            if (read < 4) {
                return false;
            }
            boolean isPdf = startsWith(head, MAGIC_PDF);
            boolean isJpeg = startsWith(head, MAGIC_JPEG);
            boolean isPng = startsWith(head, MAGIC_PNG);
            if (isPdf) {
                return allowedTypes.contains("application/pdf");
            }
            if (isJpeg) {
                return allowedTypes.contains("image/jpeg");
            }
            if (isPng) {
                return allowedTypes.contains("image/png");
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

    public String suffixOf(String contentType) {
        return switch (contentType == null ? "" : contentType) {
            case "image/png" -> ".png";
            case "application/pdf" -> ".pdf";
            default -> ".jpg";
        };
    }
}
