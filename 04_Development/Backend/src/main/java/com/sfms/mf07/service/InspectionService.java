package com.sfms.mf07.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf07.dto.InspectionDtos.CoSignRequest;
import com.sfms.mf07.dto.InspectionDtos.InspectionSessionResponse;
import com.sfms.mf07.dto.InspectionDtos.MedicalReviewRequest;
import com.sfms.mf07.dto.InspectionDtos.OpenSessionRequest;
import com.sfms.mf07.dto.InspectionDtos.ServingGateResponse;
import com.sfms.mf07.dto.InspectionDtos.SubmitStepRequest;

import java.time.LocalDate;

/** Kiểm thực ba bước (UC-052/UC-065/UC-112/UC-124) — TDS-MF07_Backend.md §8/§9. */
public interface InspectionService {

    PagedResponse<InspectionSessionResponse> list(ListQuery query, AuthUser actor);

    InspectionSessionResponse detail(String code, AuthUser actor);

    /** UC-052 — mở phiên; một bữa chỉ 1 phiên gốc còn hiệu lực (BR-079). */
    InspectionSessionResponse open(OpenSessionRequest request, AuthUser actor);

    /**
     * UC-052/UC-065 — nộp 1 bước. BR-079 (khóa tuần tự) · BR-083 (ảnh bắt buộc khi "Không đạt")
     * · BR-090/BR-091 (ngưỡng nhiệt độ) · BR-092 (lô quá hạn).
     */
    InspectionSessionResponse submitStep(String code, int step, SubmitStepRequest request, AuthUser actor);

    /** UC-052 bước 5 — đồng ký; chỉ khi cả 3 bước PASSED (BR-098). */
    InspectionSessionResponse coSign(String code, CoSignRequest request, AuthUser actor);

    /** BR-096 — tạo phiên tái kiểm tra MỚI, KHÔNG sửa phiên cũ. */
    InspectionSessionResponse reinspect(String failedCode, AuthUser actor);

    /** BR-222 — đánh giá riêng của Medical Staff, KHÔNG ghi đè {@code overall}. */
    InspectionSessionResponse medicalReview(String code, MedicalReviewRequest request, AuthUser actor);

    /** BR-094 — cổng chặn dùng bởi MF09. */
    ServingGateResponse checkGate(LocalDate date, String mealType, AuthUser actor);
}
