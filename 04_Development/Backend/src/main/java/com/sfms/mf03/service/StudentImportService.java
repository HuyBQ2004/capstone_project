package com.sfms.mf03.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.dto.StudentImportDtos.ImportPreviewResponse;
import com.sfms.mf03.dto.StudentImportDtos.ImportResultResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * UC-010, BR-032 — ADR-M03-6: import 2 pha (validate → commit), commit chạy MỘT transaction
 * all-or-nothing trên tập hợp lệ. Trước bản sửa này module chỉ có comment "TODO"; đây là service
 * MỚI (§0-BIS việc tồn đọng #4).
 *
 * @version 1.0
 */
public interface StudentImportService {

    /** Parse + validate file (multipart), KHÔNG ghi {@code students}. Lưu kết quả để commit đọc lại. */
    ImportPreviewResponse validate(MultipartFile file, AuthUser actor);

    /**
     * MỘT transaction, all-or-nothing trên tập hợp lệ của đợt import (ADR-M03-6).
     *
     * @param importCode D1 — mã nghiệp vụ trả về ở {@code validate()} (VD {@code "IMP-2026-0007"}),
     *                   KHÔNG phải UUID nội bộ.
     */
    ImportResultResponse commit(String importCode, AuthUser actor);

    /**
     * UC-010 bước 1 — template Excel (.xlsx, header đúng cột {@code validate()} đọc). Cột lớp
     * có dropdown chọn từ mã lớp thật của trường (13/08/2026, theo Nam) — tránh gõ sai mã lớp
     * rồi bị {@code validate()} từ chối ở bước sau.
     */
    Resource downloadTemplate(AuthUser actor);
}
