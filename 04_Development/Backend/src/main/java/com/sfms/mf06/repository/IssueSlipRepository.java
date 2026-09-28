package com.sfms.mf06.repository;

import com.sfms.mf06.domain.IssueSlip;
import com.sfms.common.enums.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface IssueSlipRepository extends JpaRepository<IssueSlip, UUID>, JpaSpecificationExecutor<IssueSlip> {

    Optional<IssueSlip> findBySchoolIdAndCode(UUID schoolId, String code);

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);

    Optional<IssueSlip> findBySchoolIdAndServiceDateAndMealType(UUID schoolId, LocalDate serviceDate, MealType mealType);
}
