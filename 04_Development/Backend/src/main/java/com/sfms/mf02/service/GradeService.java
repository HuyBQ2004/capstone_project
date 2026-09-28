package com.sfms.mf02.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf02.dto.GradeDtos.CreateGradeRequest;
import com.sfms.mf02.dto.GradeDtos.GradeResponse;
import com.sfms.mf02.dto.GradeDtos.UpdateGradeRequest;

import java.util.List;
import java.util.UUID;

public interface GradeService {

    List<GradeResponse> list(AuthUser actor);

    /**
     * 11/08/2026 (theo Nam) — Parent KHÔNG có {@code schoolId} trong JWT; {@code studentCode}
     * (tuỳ chọn, D1 — mã con đang chọn trên màn) cho phép phạm vi hoá theo ĐÚNG trường của
     * con đó, thay vì mặc định trả gộp khối của MỌI trường (Parent có con ở ≥2 trường trước
     * đây sẽ tra nhầm khối nếu 2 trường trùng tên khối, VD cả hai đều có "Khối 1").
     */
    List<GradeResponse> list(AuthUser actor, String studentCode);

    GradeResponse create(CreateGradeRequest request, AuthUser actor);

    GradeResponse update(UUID id, UpdateGradeRequest request, AuthUser actor);

    void deactivate(UUID id, AuthUser actor);
}
