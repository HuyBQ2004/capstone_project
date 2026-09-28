package com.sfms.mf10.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.storage.StorageService;
import com.sfms.mf10.domain.FeedbackAttachment;
import com.sfms.mf10.error.FbaExceptions;
import com.sfms.mf10.repository.FeedbackAttachmentRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Year;
import java.util.UUID;

/**
 * M10-D11 (D10/D10-BIS, L8/F2) — lưu ảnh/tài liệu đính kèm phản hồi qua
 * {@code common/storage/StorageService}. Ảnh RIÊNG TƯ (§4.3): lưu {@code storageKey}, JSON
 * KHÔNG bao giờ trả URL công khai. Whitelist kiểm bằng MAGIC BYTES, không tin
 * {@code Content-Type} client khai.
 */
@Component
class FeedbackAttachmentSupport {

    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;

    private final StorageService storageService;
    private final FeedbackAttachmentRepository attachmentRepository;
    private final CodeGenerator codeGenerator;

    FeedbackAttachmentSupport(StorageService storageService, FeedbackAttachmentRepository attachmentRepository,
                               CodeGenerator codeGenerator) {
        this.storageService = storageService;
        this.attachmentRepository = attachmentRepository;
        this.codeGenerator = codeGenerator;
    }

    FeedbackAttachment store(UUID schoolId, UUID feedbackId, MultipartFile file, UUID uploadedByUserId) {
        if (file == null || file.isEmpty()) {
            throw FbaExceptions.validationFailed("file", "must not be empty");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw FbaExceptions.attachmentTooLarge();
        }
        String detectedType = detectContentType(file);
        if (detectedType == null) {
            throw FbaExceptions.unsupportedMedia();
        }

        String key = "mf10/feedbacks/" + feedbackId + "/" + UUID.randomUUID();
        storageService.store(key, file);   // D10-BIS — KHÔNG dùng URL công khai trả về

        FeedbackAttachment attachment = new FeedbackAttachment();
        attachment.setCode(codeGenerator.next("mf10_feedback_attachment:" + Year.now().getValue(), "FBATT-"
                + Year.now().getValue(), 3));
        attachment.setSchoolId(schoolId);
        attachment.setFeedbackId(feedbackId);
        attachment.setStorageKey(key);
        attachment.setOriginalName(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        attachment.setContentType(detectedType);
        attachment.setSizeBytes(file.getSize());
        attachment.setUploadedByUserId(uploadedByUserId);
        attachmentRepository.saveAndFlush(attachment);
        return attachment;
    }

    /** Kiểm 4 magic-byte signature của whitelist D10; KHÔNG tin {@code file.getContentType()}. */
    private static String detectContentType(MultipartFile file) {
        byte[] header;
        try {
            header = file.getInputStream().readNBytes(12);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read attachment header", e);
        }
        if (startsWith(header, 0xFF, 0xD8, 0xFF)) {
            return "image/jpeg";
        }
        if (startsWith(header, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return "image/png";
        }
        if (header.length >= 12 && startsWith(header, 'R', 'I', 'F', 'F')
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return "image/webp";
        }
        if (startsWith(header, '%', 'P', 'D', 'F')) {
            return "application/pdf";
        }
        return null;
    }

    private static boolean startsWith(byte[] header, int... signature) {
        if (header.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((header[i] & 0xFF) != (signature[i] & 0xFF)) {
                return false;
            }
        }
        return true;
    }
}
