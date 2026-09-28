package com.sfms.mf03.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.dto.AllergyAlertDtos.AllergyAlertsResponse;
import com.sfms.mf03.dto.StudentDtos.CreateStudentRequest;
import com.sfms.mf03.dto.StudentDtos.ParentInfoRequest;
import com.sfms.mf03.dto.StudentDtos.StudentDietSummaryResponse;
import com.sfms.mf03.dto.StudentDtos.StudentResponse;
import com.sfms.mf03.dto.StudentDtos.UpdateStudentRequest;

/**
 * UC-025/099/147/145 — hồ sơ học sinh. C2: phạm vi dữ liệu enforce ở service, không chỉ
 * {@code @PreAuthorize}.
 *
 * @version 1.0 — bổ sung 3 method quản lý liên kết HS↔PH (TDS §9.1, mục tồn đọng §0-BIS #4):
 *         {@code addParent}/{@code removeParent}/{@code listByParentCode}. Trước bản sửa này
 *         {@code ParentLinkService} đã có {@code linkOrCreateParent}/{@code unlink} nhưng KHÔNG
 *         controller nào gọi tới — endpoint public chưa tồn tại dù service layer đã sẵn sàng.
 */
public interface StudentService {

    PagedResponse<StudentResponse> list(ListQuery query, AuthUser actor);

    /** Rút gọn cho SERVICE_CATERERS — an toàn thực phẩm, không lộ tên/DOB/PH (chốt 2026-08-02). */
    PagedResponse<StudentDietSummaryResponse> dietSummaryList(ListQuery query, AuthUser actor);

    StudentResponse getByCode(String code, AuthUser actor);

    /** @throws com.sfms.common.error.ApiException STU-010 (BR-025) · STU-030 (BR-030) */
    StudentResponse create(CreateStudentRequest request, AuthUser actor, String ip);

    StudentResponse update(String code, UpdateStudentRequest request, AuthUser actor);

    /** BR-123 — chỉ trả khai báo dị ứng {@code VERIFIED}. */
    AllergyAlertsResponse allergyAlerts(String code, AuthUser actor);

    /**
     * {@code POST /students/{code}/parents} — thêm liên kết PH thứ 2+ (BR-030 nói "ít nhất
     * một", không giới hạn 1; BR-201 chặn ở PH thứ 6).
     *
     * @throws com.sfms.common.error.ApiException STU-201 nếu PH đã đạt 5 liên kết
     */
    StudentResponse addParent(String code, ParentInfoRequest request, AuthUser actor, String ip);

    /**
     * {@code DELETE /students/{code}/parents/{parentCode}} — {@code parentCode} là
     * {@code user_accounts.code} (D1, VD {@code "U-0005"}), không phải UUID.
     *
     * @throws com.sfms.common.error.ApiException STU-030 nếu đây là liên kết cuối cùng (BR-030)
     */
    void removeParent(String code, String parentCode, AuthUser actor);

    /** {@code GET /parents/{parentCode}/students} (UC-050) — cấu trúc đối xứng {@link #list}. */
    PagedResponse<StudentResponse> listByParentCode(String parentCode, ListQuery query, AuthUser actor);
}
