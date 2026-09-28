package com.sfms.mf02.repository;

import com.sfms.mf02.domain.NutritionTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NutritionTargetRepository extends JpaRepository<NutritionTarget, UUID> {

    List<NutritionTarget> findBySchoolId(UUID schoolId);

    Optional<NutritionTarget> findBySchoolIdAndGradeId(UUID schoolId, UUID gradeId);

    /**
     * Tra chuẩn theo KHỐI mà không cần biết trường — dùng ở MF09
     * ({@code MealHistoryServiceImpl}), nơi điểm vào là học sinh chứ không phải
     * cấu hình trường. An toàn vì {@code grades.school_id} NOT NULL ⇒ một khối chỉ
     * thuộc đúng một trường, và {@code uq_nutrition_target_school_grade} đảm bảo
     * mỗi khối nhiều nhất một chuẩn.
     */
    Optional<NutritionTarget> findByGradeId(UUID gradeId);
}
