package com.sfms.mf07.repository;

import com.sfms.common.enums.MealType;
import com.sfms.mf07.domain.InspectionSession;
import com.sfms.mf07.domain.StepStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface InspectionSessionRepository
        extends JpaRepository<InspectionSession, UUID>, JpaSpecificationExecutor<InspectionSession> {

    Optional<InspectionSession> findBySchoolIdAndCode(UUID schoolId, String code);

    /**
     * 13/08/2026 (Nam) — bản KHOÁ GHI của truy vấn trên, dùng cho {@code submitStep()}.
     *
     * <p>Chốt bước là thao tác "xoá sạch dòng mẫu biểu rồi chèn lại" ({@code applyStep1..3}).
     * Hai request chạy song song (double-click, hoặc mạng chậm nên bấm lại) đều đọc trạng thái
     * cũ, đều xoá, rồi đều chèn ⇒ sổ có HAI bộ dòng trùng nhau y hệt, cùng {@code row_order}.
     * Đo được trên production: phiên INSP-20260813-B có 18 dòng cho 9 nguyên liệu, in ra Mẫu số
     * 1 lặp mỗi thứ 2 lần.
     *
     * <p>Khoá hàng {@code inspection_sessions} khiến request thứ hai ĐỢI request đầu commit rồi
     * mới chạy — nó sẽ xoá đúng bộ dòng vừa ghi và chèn lại bộ của mình, kết quả là MỘT bộ.
     * Khoá nút ở client chỉ chặn được double-click trong CÙNG một tab, không chặn hai tab/hai
     * thiết bị hay gọi thẳng API.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InspectionSession> findForUpdateBySchoolIdAndCode(UUID schoolId, String code);

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);

    /** BR-079/BR-094 — phiên gốc còn hiệu lực (khác FAILED) của một bữa. Khớp {@code ux_insp_slot}. */
    Optional<InspectionSession> findFirstBySchoolIdAndDateAndMealTypeAndPartnerIdAndOverallNot(
            UUID schoolId, LocalDate date, MealType mealType, UUID partnerId, StepStatus overall);

    /** BR-094 — phiên MỚI NHẤT của một bữa (kể cả FAILED), dùng cho serving-gate. */
    Optional<InspectionSession> findFirstBySchoolIdAndDateAndMealTypeAndPartnerIdOrderByCreatedAtDesc(
            UUID schoolId, LocalDate date, MealType mealType, UUID partnerId);

    Optional<InspectionSession> findFirstBySchoolIdAndDateAndMealTypeOrderByCreatedAtDesc(
            UUID schoolId, LocalDate date, MealType mealType);

    /** UC-124 + BR-099 — Caterer chỉ thấy phiên của đơn vị mình. */
    boolean existsBySchoolIdAndCodeAndPartnerId(UUID schoolId, String code, UUID partnerId);

    /** CF-32 — snapshot số bước không đạt trong ngày, dùng cho ký xác nhận an toàn (UC-027). */
    long countBySchoolIdAndDateAndOverall(UUID schoolId, LocalDate date, StepStatus overall);
}
