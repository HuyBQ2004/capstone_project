package com.sfms.mf08.service;

import com.sfms.common.storage.StorageService;
import com.sfms.mf08.domain.AttachmentKind;
import com.sfms.mf08.domain.AttachmentOwner;
import com.sfms.mf08.domain.SampleAttachment;
import com.sfms.mf08.error.SmpExceptions;
import com.sfms.mf08.repository.SampleAttachmentRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Lưu file bằng chứng dùng chung giữa {@code FoodSampleService} (evidencePhotos, BR-107) và
 * {@code SampleLabRequestService} (sealPhotos/handoverRecords/labReceipts, BR-212). D10-BIS —
 * tài liệu RIÊNG TƯ: lưu {@code storageKey}, KHÔNG bao giờ trả URL công khai.
 */
@Component
class AttachmentSupport {

    private final StorageService storageService;
    private final SampleAttachmentRepository attachmentRepository;

    AttachmentSupport(StorageService storageService, SampleAttachmentRepository attachmentRepository) {
        this.storageService = storageService;
        this.attachmentRepository = attachmentRepository;
    }

    void store(AttachmentOwner ownerType, UUID ownerId, AttachmentKind kind, List<MultipartFile> files) {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String key = "mf08/" + ownerType.name().toLowerCase() + "/" + ownerId + "/"
                    + kind.name().toLowerCase() + "/" + UUID.randomUUID() + suffixOf(file.getOriginalFilename());
            storageService.store(key, file);   // D10-BIS — KHÔNG dùng URL công khai trả về

            SampleAttachment attachment = new SampleAttachment();
            attachment.setOwnerType(ownerType);
            attachment.setOwnerId(ownerId);
            attachment.setKind(kind);
            attachment.setFileName(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
            attachment.setStorageKey(key);
            attachment.setSizeBytes(file.getSize());
            attachment.setChecksumSha256(sha256(file));
            attachmentRepository.saveAndFlush(attachment);
        }
    }

    long count(AttachmentOwner ownerType, UUID ownerId, AttachmentKind kind) {
        return attachmentRepository.countByOwnerTypeAndOwnerIdAndKind(ownerType, ownerId, kind);
    }

    List<SampleAttachment> list(AttachmentOwner ownerType, UUID ownerId) {
        return attachmentRepository.findByOwnerTypeAndOwnerIdOrderByCreatedAtAsc(ownerType, ownerId);
    }

    /** Tải nội dung 1 file — kiểm tra {@code ownerType}/{@code ownerId} khớp trước khi trả (không tin thẳng {@code fileId}). */
    DownloadedFile download(AttachmentOwner ownerType, UUID ownerId, UUID fileId) {
        SampleAttachment attachment = attachmentRepository.findById(fileId)
                .filter(a -> a.getOwnerType() == ownerType && a.getOwnerId().equals(ownerId))
                .orElseThrow(SmpExceptions::notFound);
        return new DownloadedFile(storageService.load(attachment.getStorageKey()), attachment.getFileName());
    }

    private static String suffixOf(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        return dot < 0 ? "" : originalFilename.substring(dot);
    }

    private static String sha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | java.io.IOException e) {
            throw new IllegalStateException("Failed to compute attachment sha256", e);
        }
    }
}
