package com.sfms.mf08.repository;

import com.sfms.mf08.domain.FoodSample;
import com.sfms.mf08.domain.FoodSampleStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FoodSampleRepository extends JpaRepository<FoodSample, UUID>,
        JpaSpecificationExecutor<FoodSample> {

    Optional<FoodSample> findBySchoolIdAndCode(UUID schoolId, String code);

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);

    /** Điều 7.1 — mỗi món của một phiên kiểm thực chỉ lưu 1 mẫu (khớp {@code uq_smp_inspection_dish}). */
    boolean existsByInspectionIdAndDishId(UUID inspectionId, UUID dishId);

    /** BR-113 (bàn giao lô) — khoá ghi cả lô mẫu trong 1 transaction, tránh race giữa 2 request. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from FoodSample s where s.schoolId = :schoolId and s.code in :codes")
    List<FoodSample> lockAllBySchoolIdAndCodeIn(@Param("schoolId") UUID schoolId,
                                                 @Param("codes") Collection<String> codes);

    /** ADR-M08-6 — job quét hạn lưu (không job nhiệt độ, CF-M08-3). */
    List<FoodSample> findByStatusAndRetentionDeadlineLessThanEqual(FoodSampleStatus status, Instant at);

    /**
     * 14/08/2026 — biến thể chỉ lấy mẫu CHƯA từng báo "disposal alert" (UC-030), dùng để job
     * quét mỗi 15 phút không gửi thông báo lặp lại cho cùng một mẫu còn chưa được huỷ.
     */
    List<FoodSample> findByStatusAndRetentionDeadlineLessThanEqualAndDisposalAlertSentAtIsNull(
            FoodSampleStatus status, Instant at);

    /** UC-076 — mẫu (của một sự cố) đã gắn {@code incidentId} còn RETAINED. */
    List<FoodSample> findBySchoolIdAndIncidentIdAndStatus(UUID schoolId, UUID incidentId, FoodSampleStatus status);

    /**
     * UC-076 — mẫu đã gắn vào sự cố và còn dùng được cho điều tra: {@code RETAINED} hoặc
     * {@code HOLD} (đang bị giữ, thêm 08/08/2026 — backlog B3). Biến thể nhiều trạng thái của
     * {@link #findBySchoolIdAndIncidentIdAndStatus}: lọc đúng một {@code RETAINED} sẽ làm mẫu
     * BIẾN MẤT khỏi danh sách ngay khi y tế bấm giữ nó.
     */
    List<FoodSample> findBySchoolIdAndIncidentIdAndStatusIn(UUID schoolId, UUID incidentId,
                                                             Collection<FoodSampleStatus> statuses);

    /** UC-076 — mẫu RETAINED trong cửa sổ điều tra theo thời gian (BR-149, trước khi gắn incidentId). */
    List<FoodSample> findBySchoolIdAndStatusAndCollectedAtBetween(
            UUID schoolId, FoodSampleStatus status, Instant from, Instant to);

    List<FoodSample> findByLabRequestId(UUID labRequestId);
}
