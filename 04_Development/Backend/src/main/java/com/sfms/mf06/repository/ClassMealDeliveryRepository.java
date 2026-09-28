package com.sfms.mf06.repository;

import com.sfms.common.enums.MealType;
import com.sfms.mf06.domain.ClassMealDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassMealDeliveryRepository extends JpaRepository<ClassMealDelivery, UUID> {

    Optional<ClassMealDelivery> findBySchoolIdAndCode(UUID schoolId, String code);

    Optional<ClassMealDelivery> findBySchoolIdAndDateAndMealTypeAndClassroomId(
            UUID schoolId, LocalDate date, MealType mealType, UUID classroomId);

    List<ClassMealDelivery> findBySchoolIdAndDateAndMealTypeOrderByClassNameAsc(
            UUID schoolId, LocalDate date, MealType mealType);

    List<ClassMealDelivery> findBySchoolIdAndClassroomIdAndDateAndMealTypeOrderByClassNameAsc(
            UUID schoolId, UUID classroomId, LocalDate date, MealType mealType);
}
