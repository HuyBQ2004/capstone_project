package com.sfms.mf11.service;

import com.sfms.common.storage.StorageService;
import com.sfms.mf11.domain.Mf11Attachment;
import com.sfms.mf11.domain.Mf11AttachmentOwner;
import com.sfms.mf11.error.IemExceptions;
import com.sfms.mf11.repository.Mf11AttachmentRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Lưu/đọc file bằng chứng dùng CHUNG cho mọi "chủ sở hữu" trong mf11 (SOURCE_REPORT,
 * CORRECTIVE_ACTION) — 1 component thay vì mỗi service tự viết lại storeFile()/sha256()
 * riêng (đã thấy lặp ở {@code ViolationServiceImpl}). Cùng cách {@code AttachmentSupport}
 * (mf08). D10-BIS — KHÔNG bao giờ trả URL công khai, chỉ trả {@code storageKey} nội bộ.
 */
@Component
public class Mf11AttachmentSupport {

    private final StorageService storageService;
    private final Mf11AttachmentRepository repository;

    public Mf11AttachmentSupport(StorageService storageService, Mf11AttachmentRepository repository) {
        this.storageService = storageService;
        this.repository = repository;
    }

    /** @return số file thực sự đã lưu (bỏ qua file rỗng/null). */
    public int store(Mf11AttachmentOwner ownerType, UUID ownerId, List<MultipartFile> files) {
        if (files == null) {
            return 0;
        }
        int stored = 0;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String key = "mf11/" + ownerType.name().toLowerCase() + "/" + ownerId + "/"
                    + UUID.randomUUID() + suffixOf(file.getOriginalFilename());
            storageService.store(key, file);

            Mf11Attachment attachment = new Mf11Attachment();
            attachment.setOwnerType(ownerType);
            attachment.setOwnerId(ownerId);
            attachment.setFileName(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
            attachment.setStorageKey(key);
            attachment.setSizeBytes(file.getSize());
            attachment.setChecksumSha256(sha256(file));
            repository.saveAndFlush(attachment);
            stored++;
        }
        return stored;
    }

    public long count(Mf11AttachmentOwner ownerType, UUID ownerId) {
        return repository.countByOwnerTypeAndOwnerId(ownerType, ownerId);
    }

    public List<Mf11Attachment> list(Mf11AttachmentOwner ownerType, UUID ownerId) {
        return repository.findByOwnerTypeAndOwnerIdOrderByCreatedAtAsc(ownerType, ownerId);
    }

    /** Tải nội dung 1 file — kiểm tra {@code ownerType}/{@code ownerId} khớp trước khi trả (không tin thẳng {@code fileId}). */
    public DownloadedFile download(Mf11AttachmentOwner ownerType, UUID ownerId, UUID fileId) {
        Mf11Attachment attachment = repository.findById(fileId)
                .filter(a -> a.getOwnerType() == ownerType && a.getOwnerId().equals(ownerId))
                .orElseThrow(IemExceptions::notFound);
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
            throw new IllegalStateException("Failed to compute mf11 attachment sha256", e);
        }
    }
}
