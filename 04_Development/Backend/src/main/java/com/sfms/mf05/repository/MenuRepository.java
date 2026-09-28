package com.sfms.mf05.repository;

import com.sfms.mf05.domain.Menu;
import com.sfms.mf05.domain.MenuStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID>, JpaSpecificationExecutor<Menu> {

    Optional<Menu> findBySchoolIdAndCode(UUID schoolId, String code);

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);

    List<Menu> findBySchoolIdAndMenuDateAndGradeIdAndAudienceAndStatusNot(
            UUID schoolId, LocalDate menuDate, UUID gradeId, String audience, MenuStatus excludedStatus);

    List<Menu> findBySchoolIdAndGradeIdAndMenuDateBetweenAndStatus(
            UUID schoolId, UUID gradeId, LocalDate from, LocalDate to, MenuStatus status);

    long countBySchoolIdAndStatus(UUID schoolId, MenuStatus status);

    /** BR-068 dish rotation (MSG-075): đếm số lần một món xuất hiện trong tuần, loại REJECTED. */
    @Query("""
           SELECT COUNT(md) FROM Menu m JOIN m.menuDishes md
           WHERE m.schoolId = :schoolId AND m.gradeId = :gradeId
             AND m.menuDate BETWEEN :weekStart AND :weekEnd
             AND md.dish.code = :dishCode AND m.status <> com.sfms.mf05.domain.MenuStatus.REJECTED
             AND m.id <> :excludeMenuId
           """)
    long countDishOccurrencesInWeek(UUID schoolId, UUID gradeId, String dishCode,
                                     LocalDate weekStart, LocalDate weekEnd, UUID excludeMenuId);

    /** TDS §9.5 mục 11 — chặn xóa món đang được thực đơn tham chiếu. */
    @Query("SELECT COUNT(md) > 0 FROM Menu m JOIN m.menuDishes md WHERE md.dish.code = :dishCode")
    boolean existsMenuReferencingDish(String dishCode);
}
