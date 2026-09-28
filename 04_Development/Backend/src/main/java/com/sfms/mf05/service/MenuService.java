package com.sfms.mf05.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.MealType;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf05.dto.MenuDtos.AllergyConflict;
import com.sfms.mf05.dto.MenuDtos.ApplyTemplateRequest;
import com.sfms.mf05.dto.MenuDtos.CreateMenuRequest;
import com.sfms.mf05.dto.MenuDtos.CreateResult;
import com.sfms.mf05.dto.MenuDtos.DietCoverageResponse;
import com.sfms.mf05.dto.MenuDtos.GradeSummaryResponse;
import com.sfms.mf05.dto.MenuDtos.MenuPublicResponse;
import com.sfms.mf05.dto.MenuDtos.MenuResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** UC-020/048/096/128/134 — TDS-MF05_Backend.md §8.1. */
public interface MenuService {

    /** D11 — Caterer/SchoolAdmin/Medical/SysAdmin, đủ field. */
    PagedResponse<MenuResponse> list(ListQuery query, AuthUser actor);

    /** D11 — Parent/Teacher, rút gọn + chỉ PUBLISHED. */
    PagedResponse<MenuPublicResponse> listPublic(ListQuery query, AuthUser actor);

    /**
     * 11/08/2026 (theo Nam) — {@code studentCode} (tuỳ chọn, D1) cho phép Parent có con ở
     * ≥2 trường lọc ĐÚNG trường của con đang chọn, thay vì luôn khoá vào trường ĐẦU TIÊN
     * theo {@code student_parents} (hành vi cũ của {@link #listPublic(ListQuery, AuthUser)}).
     */
    PagedResponse<MenuPublicResponse> listPublic(ListQuery query, AuthUser actor, String studentCode);

    MenuResponse detail(String code, AuthUser actor);

    MenuPublicResponse detailPublic(String code, AuthUser actor);

    /** UC-020 — MSG-071; trả kèm warnings (MSG-051/MSG-067) nhưng KHÔNG chặn lưu nháp. */
    CreateResult createDraft(CreateMenuRequest request, AuthUser actor);

    /**
     * UC-096 — {@code PUT} merge MỘT PHẦN (C8). {@code body} là raw JSON key→value; chỉ field
     * CÓ MẶT mới được ghi (Object.assign semantics). Phân quyền THEO FIELD (C12/§16).
     */
    MenuResponse update(String code, Map<String, Object> body, AuthUser actor);

    /** UC-048 — chỉ xóa được DRAFT/REJECTED (BR-067). */
    void delete(String code, AuthUser actor);

    /** UC-096 AF — áp Thực đơn mẫu vào ô; tự đổ {@code defaultSubstituteId} của món. */
    MenuResponse applyTemplate(String code, ApplyTemplateRequest request, AuthUser actor);

    List<AllergyConflict> allergyConflicts(String code, AuthUser actor);

    /** GET /menus/grade-summary — dị ứng/chế độ ăn của một khối (05_...design.md mục 1). */
    GradeSummaryResponse gradeSummary(java.util.UUID gradeId, AuthUser actor);

    /** GET /menus/diet-coverage — checklist nhóm chế độ ăn cần menu cho (khối, ngày, buổi). */
    DietCoverageResponse dietCoverage(java.util.UUID gradeId, LocalDate menuDateIso, MealType mealType, AuthUser actor);
}
