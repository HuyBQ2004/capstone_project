package com.sfms.mf10.repository;

import com.sfms.mf10.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface FeedbackRepository
        extends JpaRepository<Feedback, UUID>, JpaSpecificationExecutor<Feedback> {

    Optional<Feedback> findBySchoolIdAndCodeAndDeletedAtIsNull(UUID schoolId, String code);

    boolean existsBySubmitterUserIdAndMealSessionIdAndDishIdAndStudentIdAndDeletedAtIsNull(
            UUID submitterUserId, UUID mealSessionId, UUID dishId, UUID studentId);
}
