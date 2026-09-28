package com.sfms.mf03.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.domain.HealthDocument;
import com.sfms.mf03.dto.HealthDocumentDtos.HealthDocumentResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * {@code POST /students/{code}/health-documents} — evidence upload cho khai báo dị ứng/bệnh nền
 * mức {@code SEVERE} (BR-031). Xem javadoc {@link com.sfms.mf03.dto.HealthDocumentDtos}.
 */
public interface HealthDocumentService {

    /**
     * @throws com.sfms.common.error.ApiException STU-404 (HS không tồn tại/ngoài phạm vi) ·
     *         STU-008 (quá 10MB) · STU-009 (sai định dạng, kể cả giả mạo magic bytes)
     */
    HealthDocumentResponse upload(String studentCode, MultipartFile file, AuthUser actor);

    /**
     * Metadata tài liệu để controller đặt {@code Content-Type}/{@code Content-Disposition} —
     * tách khỏi {@link #download} vì {@link Resource} không mang tên file gốc lẫn kiểu nội dung.
     *
     * @throws com.sfms.common.error.ApiException STU-404 mã không tồn tại · STU-005 ngoài phạm vi
     */
    HealthDocument metadata(String studentCode, String documentCode, AuthUser actor);

    /**
     * Đọc nội dung tài liệu RIÊNG TƯ (D10-BIS mục 2) — qua {@code StorageService.load(key)}, KHÔNG
     * qua URL công khai. Cùng gate phạm vi với {@link #upload}: Parent chỉ con mình, Y tế/School
     * Admin chỉ trường mình (C2). Tài liệu phải thuộc đúng {@code studentCode} trên URL, nếu không
     * thì mã tài liệu của học sinh khác vẫn tải được chỉ bằng cách đổi đoạn student trên đường dẫn.
     */
    Resource download(String studentCode, String documentCode, AuthUser actor);
}
