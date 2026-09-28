package com.sfms.mf02.repository;

import com.sfms.mf02.domain.AcademicYear;
import com.sfms.mf02.domain.AcademicYearStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** TDS-MF02 §8.2. */
public interface AcademicYearRepository
        extends JpaRepository<AcademicYear, UUID>, JpaSpecificationExecutor<AcademicYear> {

    /** Cách ly cross-school (BR-022) — dùng ở mọi thao tác ghi/đọc chi tiết. */
    Optional<AcademicYear> findByIdAndSchoolId(UUID id, UUID schoolId);

    Optional<AcademicYear> findByCodeAndSchoolId(String code, UUID schoolId);

    /** Không cách ly trường — chỉ dùng cho System Admin (đọc mọi trường, §16). */
    Optional<AcademicYear> findByCode(String code);

    boolean existsByCodeAndSchoolId(String code, UUID schoolId);

    boolean existsBySchoolIdAndName(UUID schoolId, String name);

    /**
     * BR-191 — {@code FOR UPDATE} trong phạm vi trường; ADR-M02-4: enforce trong
     * {@code @Transactional} + {@code PESSIMISTIC_WRITE} để chống race 2 request activate.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ay from AcademicYear ay where ay.schoolId = :schoolId and ay.status = :status")
    List<AcademicYear> findBySchoolIdAndStatusForUpdate(UUID schoolId, AcademicYearStatus status);

    List<AcademicYear> findBySchoolIdAndStatus(UUID schoolId, AcademicYearStatus status);

    /** Năm liền trước theo {@code startDate} — nguồn copyStructure (BR-194). */
    Optional<AcademicYear> findFirstBySchoolIdAndStartDateLessThanOrderByStartDateDesc(
            UUID schoolId, java.time.LocalDate before);
}
