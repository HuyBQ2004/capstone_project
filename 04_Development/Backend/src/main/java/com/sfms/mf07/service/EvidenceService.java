package com.sfms.mf07.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf07.dto.EvidenceDtos.EvidenceResponse;
import com.sfms.mf07.dto.EvidenceDtos.EvidenceSummary;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Ảnh bằng chứng (BR-083/BR-207/BR-211) — D10-BIS: tài liệu RIÊNG TƯ. */
public interface EvidenceService {

    EvidenceResponse upload(String ownerType, String ownerId, MultipartFile file, String gps, AuthUser actor);

    /** {@code GET /inspections/{code}/evidences} — mọi ảnh gắn phiên, gom theo ownerType,
     *  KHÔNG phân biệt được từng dòng (xem chú thích {@link EvidenceSummary}). */
    List<EvidenceSummary> listForSession(String sessionCode, AuthUser actor);

    /**
     * Tải ảnh bằng chứng theo NGAY MÃ PHIÊN kiểm thực (không cần UUID nội bộ — D1, JSON
     * không bao giờ lộ UUID nên frontend không có cách nào tự truyền {@code ownerId} cho
     * {@link #upload}). {@code ownerId} lưu xuống = UUID nội bộ của phiên; dùng khi
     * {@code ownerType} là STEP1_FRESH/STEP1_DRY/STEP2/STEP3/SESSION — mỗi bữa kiểm thực
     * chỉ có 1 phiên nên gộp ảnh theo (phiên, loại) là đủ, không cần ownerId riêng từng dòng
     * (các dòng trong Mẫu 1/2/3 chỉ tồn tại trong JSON, không có id DB riêng để tham chiếu).
     */
    EvidenceResponse uploadForSession(String sessionCode, String ownerType, MultipartFile file, String gps,
                                       AuthUser actor);

    /**
     * Kèm {@code fileName} gốc (10/08/2026) — controller cần để suy MIME type: response
     * trước đây chỉ trả {@link Resource} trần, không set {@code Content-Type}, nên trình
     * duyệt không biết là ảnh và mở tab mới ra chuỗi byte nhị phân thay vì hiển thị ảnh.
     * {@code sessionCode} chỉ dùng để kiểm phạm vi đọc (BR-099) trước khi trả file.
     */
    record DownloadedFile(Resource resource, String fileName) {}

    DownloadedFile download(String sessionCode, String evidenceId, AuthUser actor);
}
