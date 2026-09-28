package com.sfms.mf03.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.dto.HealthFileDtos.HealthFileResponse;
import com.sfms.mf03.dto.HealthFileDtos.UpdateHealthFileRequest;
import com.sfms.mf03.dto.HealthRecordDtos.HealthRecordRequest;
import com.sfms.mf03.dto.HealthRecordDtos.HealthRecordResponse;

import java.util.UUID;

/**
 * UC-051/094/050. C8 — {@code appendClinical} chỉ sửa 4 field lâm sàng, KHÔNG đụng dị ứng/chế
 * độ ăn, KHÔNG đổi trạng thái xác minh.
 *
 * @version 1.0
 */
public interface StudentHealthFileService {

    HealthFileResponse getFile(UUID studentId, AuthUser actor);

    HealthFileResponse appendClinical(UUID studentId, UpdateHealthFileRequest request, AuthUser medical);

    /** Quyết định (đã chốt) — chưa có bản ghi ⇒ {@code 200} + {@code null}, KHÔNG 404. */
    HealthRecordResponse getHealthRecord(UUID studentId, AuthUser actor);

    HealthRecordResponse upsertHealthRecord(UUID studentId, HealthRecordRequest request, AuthUser actor);
}
