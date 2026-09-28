package com.sfms.mf04.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.dto.DocumentDtos.DocumentResponse;
import com.sfms.mf04.dto.DocumentDtos.UpdateDocumentRequest;
import com.sfms.mf04.dto.DocumentDtos.UploadDocumentRequest;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/** UC-167 (Manage) · UC-101/102 (Upload) · UC-091 (Update) — @version 1.0. */
public interface ComplianceDocumentService {

    PagedResponse<DocumentResponse> list(ListQuery query, AuthUser actor);

    DocumentResponse upload(UploadDocumentRequest req, MultipartFile file, AuthUser actor);

    /** UC-091 — tạo bản MỚI, bản cũ giữ nguyên với {@code supersededByDocumentId} trỏ tới bản mới. */
    DocumentResponse update(String code, UpdateDocumentRequest req, MultipartFile file, AuthUser actor);

    /** D10-BIS mục 2 — tải xuống file gốc qua endpoint riêng, KHÔNG qua CDN/URL công khai. */
    Resource download(String code, AuthUser actor);
}
