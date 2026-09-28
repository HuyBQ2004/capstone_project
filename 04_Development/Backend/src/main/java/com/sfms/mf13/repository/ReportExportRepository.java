package com.sfms.mf13.repository;

import com.sfms.mf13.domain.ReportExport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportExportRepository extends JpaRepository<ReportExport, UUID>,
        JpaSpecificationExecutor<ReportExport> {

    Optional<ReportExport> findByCodeAndSchoolId(String code, UUID schoolId);

    boolean existsByCodeAndSchoolId(String code, UUID schoolId);

    /** UC-172 §9.1 #7 — bản xuất có thể ở BẤT KỲ trường nào (System Admin, BR-176 scope=all). */
    Optional<ReportExport> findByCode(String code);

    Page<ReportExport> findBySchoolIdOrderByGeneratedAtDesc(UUID schoolId, Pageable pageable);

    Page<ReportExport> findBySchoolIdAndReportSlugOrderByGeneratedAtDesc(UUID schoolId,
            com.sfms.mf13.domain.ReportSlug reportSlug, Pageable pageable);

    Page<ReportExport> findAllByOrderByGeneratedAtDesc(Pageable pageable);

    /** BR-177 — chỉ chọn bản đã quá hạn lưu trữ; job purge KHÔNG được xoá sớm hơn. */
    List<ReportExport> findByExpiresAtBefore(Instant now, Pageable batch);
}
