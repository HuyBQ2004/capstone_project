package com.sfms.mf09.repository;

import com.sfms.common.enums.MealType;
import com.sfms.mf09.domain.StudentDistribution;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentDistributionRepository
        extends JpaRepository<StudentDistribution, UUID>, JpaSpecificationExecutor<StudentDistribution> {

    Optional<StudentDistribution> findBySchoolIdAndCode(UUID schoolId, String code);

    /** C4/BR-130 — {@code SELECT ... FOR UPDATE}, chặn 2 request song song xác nhận cùng phiên. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from StudentDistribution d where d.schoolId = :schoolId and d.code = :code")
    Optional<StudentDistribution> findBySchoolIdAndCodeForUpdate(@Param("schoolId") UUID schoolId,
                                                                  @Param("code") String code);

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);

    List<StudentDistribution> findByStudentIdAndDateBetweenOrderByDateDesc(
            UUID studentId, LocalDate from, LocalDate to);

    List<StudentDistribution> findBySchoolIdAndDateAndMealType(UUID schoolId, LocalDate date, MealType mealType);

    List<StudentDistribution> findBySchoolIdAndDateAndClassName(UUID schoolId, LocalDate date, String className);

    List<StudentDistribution> findBySchoolIdAndDateAndClassNameAndMealType(
            UUID schoolId, LocalDate date, String className, MealType mealType);

    /** ADR-M09-3/BR-130 — dùng bởi {@code DistributionPlanHandler} để idempotent theo (HS, ngày, bữa). */
    boolean existsByStudentIdAndDateAndMealType(UUID studentId, LocalDate date, MealType mealType);

    /** {@code DistributionPlanHandler} — load bản ghi (nếu có) để SỬA ĐÈ menuId khi menu đúng
     *  audience của HS publish sau (menu chung/menu riêng publish trước-sau không cố định thứ tự). */
    Optional<StudentDistribution> findByStudentIdAndDateAndMealType(UUID studentId, LocalDate date, MealType mealType);
}
