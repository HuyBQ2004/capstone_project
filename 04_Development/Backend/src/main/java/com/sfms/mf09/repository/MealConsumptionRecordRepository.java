package com.sfms.mf09.repository;

import com.sfms.common.enums.MealType;
import com.sfms.mf09.domain.MealConsumptionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MealConsumptionRecordRepository
        extends JpaRepository<MealConsumptionRecord, UUID>, JpaSpecificationExecutor<MealConsumptionRecord> {

    Optional<MealConsumptionRecord> findBySchoolIdAndCode(UUID schoolId, String code);

    List<MealConsumptionRecord> findByStudentIdAndDateBetweenOrderByDateDesc(
            UUID studentId, LocalDate from, LocalDate to);

    /** 11/08/2026 — chặn tạo trùng khi phát suất (uq_mcr_student_date_meal, V16). */
    boolean existsByStudentIdAndDateAndMealType(UUID studentId, LocalDate date, MealType mealType);
}
