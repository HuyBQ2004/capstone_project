package com.sfms.mf13.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.storage.StorageService;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.service.AuditService;
import com.sfms.mf13.domain.ReportExport;
import com.sfms.mf13.domain.ReportFormat;
import com.sfms.mf13.domain.ReportSlug;
import com.sfms.mf13.dto.ReportDtos.DownloadedFile;
import com.sfms.mf13.dto.ReportDtos.ReportExportResponse;
import com.sfms.mf13.error.RptExceptions;
import com.sfms.mf13.repository.ReportExportRepository;
import com.sfms.mf13.repository.ReportViewGateway;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * UC-172 §8.1. BR-177/178/181/182. Retention ≥12 tháng (ADR-M13-3) — cấu hình mặc định 24
 * tháng qua {@code report.retention-months}, luôn kẹp về tối thiểu 12.
 */
@Service
public class ReportExportServiceImpl implements ReportExportService {

    private static final int MIN_RETENTION_MONTHS = 12;
    private static final int DEFAULT_RETENTION_MONTHS = 24;

    private final ReportExportRepository repository;
    private final ReportViewGateway viewGateway;
    private final ReportAccessPolicy accessPolicy;
    private final ReportRowProjection projection;
    private final Map<String, ReportRenderer> renderers;
    private final StorageService storageService;
    private final ReferenceResolver referenceResolver;
    private final AuditService auditService;
    private final CodeGenerator codeGenerator;
    private final Clock clock;

    public ReportExportServiceImpl(ReportExportRepository repository, ReportViewGateway viewGateway,
                                   ReportAccessPolicy accessPolicy, ReportRowProjection projection,
                                   Map<String, ReportRenderer> renderers,
                                   StorageService storageService, ReferenceResolver referenceResolver,
                                   AuditService auditService, CodeGenerator codeGenerator, Clock clock) {
        this.repository = repository;
        this.viewGateway = viewGateway;
        this.accessPolicy = accessPolicy;
        this.projection = projection;
        this.renderers = renderers;
        this.storageService = storageService;
        this.referenceResolver = referenceResolver;
        this.auditService = auditService;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ReportExportResponse export(AuthUser actor, ReportSlug slug, ReportFormat format, LocalDate from,
                                       LocalDate to, String periodLabel, Map<String, Object> filters) {
        if (!accessPolicy.canExport(actor.role(), slug)) {
            throw RptExceptions.accessDenied();
        }
        UUID schoolId = actor.schoolId();
        if (schoolId == null) {
            throw RptExceptions.accessDenied();
        }
        ScheduledExportResult result = doExport(schoolId, actor.userId(), slug, format, from, to, periodLabel,
                filters);
        auditService.success(actor.email(), null, "EXPORT_REPORT", "report-exports");
        return result.response();
    }

    /** ADR-M13-7 — điểm gọi CỦA {@link ReportScheduleRunner}, KHÔNG qua {@link ReportAccessPolicy}
     *  (không phải HTTP actor — schoolId/user đã xác định từ chính {@code report_schedules}). */
    @Transactional
    public ScheduledExportResult exportForSchedule(UUID schoolId, UUID triggeredByUserId, ReportSlug slug,
                                                    ReportFormat format, LocalDate from, LocalDate to,
                                                    String periodLabel) {
        return doExport(schoolId, triggeredByUserId, slug, format, from, to, periodLabel, Map.of());
    }

    private ScheduledExportResult doExport(UUID schoolId, UUID byUserId, ReportSlug slug, ReportFormat format,
                                           LocalDate from, LocalDate to, String periodLabel,
                                           Map<String, Object> filters) {
        if (format == null) {
            throw RptExceptions.validationFailed("format", "must be PDF or XLSX");
        }
        if (from == null || to == null || from.isAfter(to)) {
            throw RptExceptions.validationFailed("dateFrom", "dateFrom must be before or equal to dateTo");
        }

        // BR-176/181: dữ liệu nguồn qua view + whitelist, KHÔNG SELECT * thẳng ra ngoài.
        List<Map<String, Object>> rawRows = viewGateway.rows(slug, schoolId, from, to, Integer.MAX_VALUE, 0);
        List<Map<String, Object>> rows = rawRows.stream().map(r -> projection.project(slug, r)).toList();

        String reportTitle = titleOf(slug);
        String schoolName = referenceResolver.schoolName(schoolId);
        String label = periodLabel != null ? periodLabel : from + " - " + to;
        Instant generatedAt = clock.instant();

        ReportRenderer renderer = renderers.get(format == ReportFormat.PDF ? "pdf" : "xlsx");
        byte[] bytes = renderer.render(reportTitle, label, schoolName, generatedAt, rows);
        String checksum = sha256(bytes);

        int year = generatedAt.atZone(java.time.ZoneOffset.UTC).getYear();
        String code = codeGenerator.next("report_exports_" + year, "RPX-" + year, 3);
        String fileName = code + "." + renderer.fileExtension();
        String storageKey = "report-exports/" + fileName;
        storageService.store(storageKey, new ByteArrayMultipartFile(fileName, renderer.contentType(), bytes));

        int retentionMonths = Math.max(MIN_RETENTION_MONTHS, DEFAULT_RETENTION_MONTHS);
        // Instant chỉ nhận đơn vị dựa trên duration (giây/nano) — Period.ofMonths() ném
        // UnsupportedTemporalTypeException nếu cộng thẳng vào Instant, phải qua ZonedDateTime.
        Instant expiresAt = generatedAt.atZone(java.time.ZoneOffset.UTC)
                .plus(Period.ofMonths(retentionMonths)).toInstant();

        ReportExport entity = new ReportExport(code, schoolId, slug, reportTitle, from, to, label, format,
                filters == null ? Map.of() : filters, rows.size(), true, storageKey, bytes.length, checksum,
                generatedAt, byUserId, expiresAt);
        repository.save(entity);

        return new ScheduledExportResult(toResponse(entity, null), bytes);
    }

    /** Kết quả xuất kèm nội dung file — {@link ReportScheduleRunner} cần bytes để đính kèm email. */
    public record ScheduledExportResult(ReportExportResponse response, byte[] bytes) {
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportExportResponse> history(AuthUser actor, ReportSlug slugOrNull, boolean allSchools,
                                              Pageable pageable) {
        UUID schoolId = resolveScope(actor, allSchools);
        Page<ReportExport> page;
        if (schoolId == null) {
            page = repository.findAllByOrderByGeneratedAtDesc(pageable);
        } else if (slugOrNull != null) {
            page = repository.findBySchoolIdAndReportSlugOrderByGeneratedAtDesc(schoolId, slugOrNull, pageable);
        } else {
            page = repository.findBySchoolIdOrderByGeneratedAtDesc(schoolId, pageable);
        }
        return page.map(e -> toResponse(e, null));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReportExportResponse> findByCode(AuthUser actor, String code) {
        ReportExport entity = lookup(actor, code);
        return Optional.of(toResponse(entity, null));
    }

    @Override
    @Transactional
    public DownloadedFile download(AuthUser actor, String code) {
        ReportExport entity = lookup(actor, code);
        if (entity.getExpiresAt().isBefore(clock.instant())) {
            throw RptExceptions.expired();
        }
        byte[] bytes = readAll(entity.getFileUri());
        String checksum = sha256(bytes);
        if (!checksum.equalsIgnoreCase(entity.getChecksumSha256())) {
            throw new IllegalStateException("Checksum mismatch for " + code + " — file corrupted (BR-182)");
        }
        auditService.success(actor.email(), null, "DOWNLOAD_REPORT", "report-exports");
        String contentType = entity.getFormat() == ReportFormat.PDF ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        return new DownloadedFile(code + "." + (entity.getFormat() == ReportFormat.PDF ? "pdf" : "xlsx"),
                contentType, bytes);
    }

    private byte[] readAll(String key) {
        try {
            return storageService.load(key).getInputStream().readAllBytes();
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    /** BR-176: cross-school ⇒ 404 (M13-D04), không phân biệt "không tồn tại" vs "trường khác". */
    private ReportExport lookup(AuthUser actor, String code) {
        if (actor.isSystemAdmin()) {
            return repository.findByCode(code).orElseThrow(RptExceptions::notFound);
        }
        UUID schoolId = actor.schoolId();
        if (schoolId == null) {
            throw RptExceptions.notFound();
        }
        return repository.findByCodeAndSchoolId(code, schoolId).orElseThrow(RptExceptions::notFound);
    }

    private UUID resolveScope(AuthUser actor, boolean allSchools) {
        if (allSchools) {
            if (!accessPolicy.canViewAllSchools(actor.role())) {
                throw RptExceptions.accessDenied();
            }
            return null;
        }
        return actor.schoolId();
    }

    private ReportExportResponse toResponse(ReportExport e, String generatedByFallback) {
        String[] user = referenceResolver.userById(e.getGeneratedByUserId());
        // D1 — trả mã nghiệp vụ người dùng (userCode), không phải UUID nội bộ.
        String generatedBy = user != null ? user[0] : generatedByFallback;
        return new ReportExportResponse(e.getCode(), e.getReportSlug(), e.getReportTitle(), e.getFormat(),
                e.getPeriodFrom(), e.getPeriodTo(), e.getPeriodLabel(), e.getRowCount(), e.isConfidential(),
                e.getFileSizeBytes(), e.getChecksumSha256(), e.getGeneratedAt(), generatedBy, e.getExpiresAt(),
                "/api/v1/report-exports/" + e.getCode() + "/download");
    }

    private static String titleOf(ReportSlug slug) {
        return switch (slug) {
            case THREE_STEP -> "Báo cáo Kiểm thực ba bước";
            case SAMPLE_RETENTION -> "Báo cáo Lưu mẫu thực phẩm";
            case RECEIVING -> "Báo cáo Nhận nguyên liệu";
            case SUPPLIER_CERTIFICATES -> "Báo cáo Chứng nhận nhà cung cấp";
            case HYGIENE -> "Báo cáo Vệ sinh bếp";
            case TRACEABILITY -> "Báo cáo Truy xuất nguồn gốc";
            case SYSTEM_USAGE -> "Mức độ sử dụng hệ thống";
        };
    }

    private static String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] raw = digest.digest(bytes);
            StringBuilder hex = new StringBuilder(raw.length * 2);
            for (byte b : raw) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
