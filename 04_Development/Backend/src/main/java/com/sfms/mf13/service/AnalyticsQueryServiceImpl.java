package com.sfms.mf13.service;

import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf13.domain.ReportSlug;
import com.sfms.mf13.dto.ReportDtos.ComplianceSummaryResponse;
import com.sfms.mf13.dto.ReportDtos.ModuleMetric;
import com.sfms.mf13.error.RptExceptions;
import com.sfms.mf13.repository.ReportViewGateway;
import com.sfms.mf13.repository.ReportViewGateway.ModuleAggregate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UC-171 §8.1. ADR-M13-6/M13-D09: cache TTL <b>240s</b> (không dùng Caffeine/Redis — chưa có
 * hạ tầng cache chung trên classpath; cache thủ công trong-process là đủ cho BR-180 "≤5 phút"
 * ở quy mô MVP, cùng tinh thần {@code ReferenceResolver} cache trong-request).
 */
@Service
public class AnalyticsQueryServiceImpl implements AnalyticsQueryService {

    /** M13-D09 — 300s là TRẦN của BR-180; 240s để lại biên an toàn 60s. */
    private static final Duration CACHE_TTL = Duration.ofSeconds(240);

    private record CacheEntry(ComplianceSummaryResponse response, Instant computedAt) {
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private final ReportViewGateway viewGateway;
    private final ReportAccessPolicy accessPolicy;
    private final ReportRowProjection projection;
    private final ReferenceResolver referenceResolver;
    private final Clock clock;

    public AnalyticsQueryServiceImpl(ReportViewGateway viewGateway, ReportAccessPolicy accessPolicy,
                                     ReportRowProjection projection, ReferenceResolver referenceResolver,
                                     Clock clock) {
        this.viewGateway = viewGateway;
        this.accessPolicy = accessPolicy;
        this.projection = projection;
        this.referenceResolver = referenceResolver;
        this.clock = clock;
    }

    @Override
    public ComplianceSummaryResponse complianceSummary(AuthUser actor, LocalDate from, LocalDate to,
                                                        boolean allSchools) {
        UUID schoolIdFilter = resolveSchoolScope(actor, allSchools);
        LocalDate periodTo = to != null ? to : LocalDate.now(clock);
        LocalDate periodFrom = from != null ? from : periodTo.minusDays(30);

        String key = (schoolIdFilter == null ? "ALL" : schoolIdFilter) + ":" + periodFrom + ":" + periodTo;
        CacheEntry cached = cache.get(key);
        Instant now = clock.instant();
        if (cached != null && Duration.between(cached.computedAt(), now).compareTo(CACHE_TTL) <= 0) {
            return cached.response();
        }

        List<ModuleMetric> modules = new java.util.ArrayList<>();
        addIfViewable(modules, actor, ReportSlug.THREE_STEP, "Kiểm thực ba bước",
                viewGateway.threeStepAggregate(schoolIdFilter, periodFrom, periodTo));
        addIfViewable(modules, actor, ReportSlug.SAMPLE_RETENTION, "Lưu mẫu thực phẩm",
                viewGateway.sampleRetentionAggregate(schoolIdFilter, periodFrom, periodTo));
        addIfViewable(modules, actor, ReportSlug.RECEIVING, "Nhận nguyên liệu",
                viewGateway.receivingAggregate(schoolIdFilter, periodFrom, periodTo));
        addIfViewable(modules, actor, ReportSlug.SUPPLIER_CERTIFICATES, "Chứng nhận nhà cung cấp",
                viewGateway.supplierCertAggregate(schoolIdFilter, periodFrom, periodTo));
        addIfViewable(modules, actor, ReportSlug.HYGIENE, "Vệ sinh bếp (UC-014)",
                viewGateway.hygieneAggregate(schoolIdFilter, periodFrom, periodTo));

        String schoolCode = schoolIdFilter == null ? null : referenceResolver.schoolCode(schoolIdFilter);
        ComplianceSummaryResponse response = new ComplianceSummaryResponse(now, periodFrom, periodTo,
                periodLabel(periodFrom, periodTo), schoolCode, modules);
        cache.put(key, new CacheEntry(response, now));
        return response;
    }

    private void addIfViewable(List<ModuleMetric> out, AuthUser actor, ReportSlug slug, String title,
                               ModuleAggregate agg) {
        if (!accessPolicy.canView(actor.role(), slug)) {
            return;
        }
        out.add(new ModuleMetric(slug, title, agg.rate(), agg.total(), agg.alerts()));
    }

    @Override
    public Page<Map<String, Object>> rows(AuthUser actor, ReportSlug slug, LocalDate from, LocalDate to,
                                          Pageable pageable) {
        if (!accessPolicy.canView(actor.role(), slug)) {
            throw RptExceptions.accessDenied();
        }
        UUID schoolId = actor.schoolId();
        if (schoolId == null && !actor.isSystemAdmin()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        long total = viewGateway.count(slug, schoolId, from, to);
        List<Map<String, Object>> raw = viewGateway.rows(slug, schoolId, from, to,
                pageable.getPageSize(), (int) pageable.getOffset());
        List<Map<String, Object>> projected = raw.stream().map(r -> projection.project(slug, r)).toList();
        return new PageImpl<>(projected, pageable, total);
    }

    /** BR-176: {@code allSchools} chỉ System Admin; role khác → 403 RPT-005. */
    private UUID resolveSchoolScope(AuthUser actor, boolean allSchools) {
        if (allSchools) {
            if (!accessPolicy.canViewAllSchools(actor.role())) {
                throw RptExceptions.accessDenied();
            }
            return null;
        }
        return actor.schoolId();
    }

    private static String periodLabel(LocalDate from, LocalDate to) {
        return from + " - " + to;
    }
}
