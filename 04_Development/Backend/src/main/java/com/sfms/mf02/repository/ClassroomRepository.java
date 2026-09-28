package com.sfms.mf02.repository;

import com.sfms.mf02.domain.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** TDS-MF02 §8.2. */
public interface ClassroomRepository
        extends JpaRepository<Classroom, UUID>, JpaSpecificationExecutor<Classroom> {

    /** Cách ly cross-school (BR-022). */
    Optional<Classroom> findByIdAndSchoolId(UUID id, UUID schoolId);

    Optional<Classroom> findByCodeAndAcademicYearId(String code, UUID academicYearId);

    /**
     * D1: {@code code} chỉ UNIQUE trong phạm vi {@code academic_year_id}, không toàn trường —
     * cùng một mã lớp (VD {@code "3A"}) có thể tồn tại ở nhiều năm học khác nhau. Dùng ở
     * {@code ClassroomService.requireInScope()} để tìm bản ghi đúng năm học hiện hành
     * (xem Javadoc ở đó).
     */
    List<Classroom> findBySchoolIdAndCode(UUID schoolId, String code);

    long countByAcademicYearId(UUID academicYearId);

    /** BR-020 — 1 GV chỉ chủ nhiệm 1 lớp/năm học. */
    Optional<Classroom> findByAcademicYearIdAndHomeroomTeacherId(UUID academicYearId, UUID teacherId);

    boolean existsByAcademicYearIdAndCode(UUID academicYearId, String code);

    boolean existsByAcademicYearIdAndNameIgnoreCase(UUID academicYearId, String name);

    List<Classroom> findBySchoolIdAndAcademicYearId(UUID schoolId, UUID academicYearId);

    List<Classroom> findByAcademicYearId(UUID academicYearId);

    /** BR-021 — cascade deactivate trường: đóng băng mọi lớp của trường. */
    List<Classroom> findBySchoolId(UUID schoolId);
}
