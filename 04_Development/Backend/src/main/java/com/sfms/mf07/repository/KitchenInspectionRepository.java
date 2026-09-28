package com.sfms.mf07.repository;

import com.sfms.mf07.domain.KitchenInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KitchenInspectionRepository
        extends JpaRepository<KitchenInspection, UUID>, JpaSpecificationExecutor<KitchenInspection> {

    Optional<KitchenInspection> findBySchoolIdAndCode(UUID schoolId, String code);

    /** CF-32 — snapshot điểm vệ sinh trung bình trong ngày, dùng cho ký xác nhận an toàn (UC-027). */
    @Query("SELECT AVG(k.score) FROM KitchenInspection k WHERE k.schoolId = :schoolId AND k.date = :date")
    Double averageScoreToday(UUID schoolId, LocalDate date);

    /** BR-209 (MF12 §9.4b, `ComplianceDataPort`) — nguồn báo cáo tuân thủ theo kỳ. */
    List<KitchenInspection> findBySchoolIdAndDateBetweenOrderByDateAsc(UUID schoolId, LocalDate from, LocalDate to);
}
