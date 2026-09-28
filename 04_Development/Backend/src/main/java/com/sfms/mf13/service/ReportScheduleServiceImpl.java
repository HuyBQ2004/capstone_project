package com.sfms.mf13.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.service.AuditService;
import com.sfms.mf13.domain.ReportSchedule;
import com.sfms.mf13.domain.ReportScheduleRun;
import com.sfms.mf13.domain.ReportSlug;
import com.sfms.mf13.dto.ReportDtos.CreateReportScheduleRequest;
import com.sfms.mf13.dto.ReportDtos.ReportScheduleResponse;
import com.sfms.mf13.dto.ReportDtos.ReportScheduleRunResponse;
import com.sfms.mf13.dto.ReportDtos.UpdateReportScheduleRequest;
import com.sfms.mf13.error.RptExceptions;
import com.sfms.mf13.repository.ReportScheduleRepository;
import com.sfms.mf13.repository.ReportScheduleRunRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** UC-173 §8.1. BR-179 — người nhận KHÔNG lưu ở entity (§4.3 Email egress, ADR-M13-7). */
@Service
public class ReportScheduleServiceImpl implements ReportScheduleService {

    private final ReportScheduleRepository repository;
    private final ReportScheduleRunRepository runRepository;
    private final ReportAccessPolicy accessPolicy;
    private final ReferenceResolver referenceResolver;
    private final AuditService auditService;
    private final CodeGenerator codeGenerator;
    private final ReportScheduleRunner runner;
    private final Clock clock;

    public ReportScheduleServiceImpl(ReportScheduleRepository repository, ReportScheduleRunRepository runRepository,
                                     ReportAccessPolicy accessPolicy, ReferenceResolver referenceResolver,
                                     AuditService auditService, CodeGenerator codeGenerator,
                                     ReportScheduleRunner runner, Clock clock) {
        this.repository = repository;
        this.runRepository = runRepository;
        this.accessPolicy = accessPolicy;
        this.referenceResolver = referenceResolver;
        this.auditService = auditService;
        this.codeGenerator = codeGenerator;
        this.runner = runner;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ReportScheduleResponse create(AuthUser actor, CreateReportScheduleRequest req) {
        requireManage(actor);
        if (req.reportSlug() == null || req.frequency() == null || req.format() == null
                || req.timeOfDay() == null) {
            throw RptExceptions.validationFailed("reportSlug/frequency/format/timeOfDay", "required");
        }
        UUID schoolId = actor.schoolId();
        if (schoolId == null) {
            throw RptExceptions.accessDenied();
        }

        Instant now = clock.instant();
        Instant nextRunAt = NextRunCalculator.nextRun(req.frequency(), req.timeOfDay(), req.zoneId(),
                req.dayOfWeek(), req.dayOfMonth(), now);

        String code = req.id();
        if (code != null && !code.isBlank()) {
            if (repository.existsByCodeAndSchoolId(code, schoolId)) {
                throw RptExceptions.idConflict("id", code);
            }
        } else {
            int year = now.atZone(java.time.ZoneOffset.UTC).getYear();
            code = codeGenerator.next("report_schedules_" + year, "RSC-" + year, 3);
        }

        ReportSchedule entity = new ReportSchedule();
        entity.setCode(code);
        entity.setSchoolId(schoolId);
        entity.setReportSlug(req.reportSlug());
        entity.setFrequency(req.frequency());
        entity.setFormat(req.format());
        entity.setTimeOfDay(req.timeOfDay());
        entity.setDayOfWeek(req.dayOfWeek());
        entity.setDayOfMonth(req.dayOfMonth());
        entity.setZoneId(req.zoneId() == null ? "Asia/Ho_Chi_Minh" : req.zoneId());
        entity.setActive(true);
        entity.setNextRunAt(nextRunAt);
        entity.setCreatedByUserId(actor.userId());
        repository.save(entity);

        auditService.success(actor.email(), null, "CREATE_REPORT_SCHEDULE", "report-schedules");
        return toResponse(entity);
    }

    @Override
    @Transactional
    public ReportScheduleResponse update(AuthUser actor, String code, UpdateReportScheduleRequest req) {
        requireManage(actor);
        ReportSchedule entity = lookup(actor, code);

        var frequency = req.frequency() != null ? req.frequency() : entity.getFrequency();
        var format = req.format() != null ? req.format() : entity.getFormat();
        var timeOfDay = req.timeOfDay() != null ? req.timeOfDay() : entity.getTimeOfDay();
        var dayOfWeek = req.dayOfWeek() != null ? req.dayOfWeek() : entity.getDayOfWeek();
        var dayOfMonth = req.dayOfMonth() != null ? req.dayOfMonth() : entity.getDayOfMonth();
        var zoneId = req.zoneId() != null ? req.zoneId() : entity.getZoneId();
        boolean active = req.active() != null ? req.active() : entity.isActive();

        Instant nextRunAt = NextRunCalculator.nextRun(frequency, timeOfDay, zoneId, dayOfWeek, dayOfMonth,
                clock.instant());

        entity.setFrequency(frequency);
        entity.setFormat(format);
        entity.setTimeOfDay(timeOfDay);
        entity.setDayOfWeek(dayOfWeek);
        entity.setDayOfMonth(dayOfMonth);
        entity.setZoneId(zoneId);
        entity.setActive(active);
        entity.setNextRunAt(nextRunAt);
        repository.save(entity);

        auditService.success(actor.email(), null, "UPDATE_REPORT_SCHEDULE", "report-schedules");
        return toResponse(entity);
    }

    @Override
    @Transactional
    public void delete(AuthUser actor, String code) {
        requireManage(actor);
        ReportSchedule entity = lookup(actor, code);
        repository.delete(entity);
        auditService.success(actor.email(), null, "DELETE_REPORT_SCHEDULE", "report-schedules");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportScheduleResponse> list(AuthUser actor, ReportSlug slugOrNull, Pageable pageable) {
        requireManage(actor);
        UUID schoolId = actor.schoolId();
        Page<ReportSchedule> page = (schoolId == null && actor.isSystemAdmin())
                ? repository.findAllBy(pageable)
                : repository.findBySchoolId(schoolId, pageable);
        if (slugOrNull != null) {
            List<ReportScheduleResponse> filtered = page.getContent().stream()
                    .filter(s -> s.getReportSlug() == slugOrNull).map(this::toResponse).toList();
            return new org.springframework.data.domain.PageImpl<>(filtered, pageable, filtered.size());
        }
        return page.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportScheduleResponse findByCode(AuthUser actor, String code) {
        requireManage(actor);
        return toResponse(lookup(actor, code));
    }

    @Override
    @Transactional
    public ReportScheduleRunResponse runNow(AuthUser actor, String code) {
        requireManage(actor);
        ReportSchedule entity = lookup(actor, code);
        ReportScheduleRun run = runner.executeOnce(entity);
        return new ReportScheduleRunResponse(entity.getCode(), run.getScheduledFor(), run.getAttempt(),
                run.getStatus().name(), null, run.getRecipientCount(), run.getErrorMessage(), run.getStartedAt(),
                run.getFinishedAt());
    }

    private void requireManage(AuthUser actor) {
        if (!accessPolicy.canManageSchedules(actor.role())) {
            throw RptExceptions.accessDenied();
        }
    }

    /** BR-176: cross-school ⇒ 404 (M13-D04). */
    private ReportSchedule lookup(AuthUser actor, String code) {
        if (actor.isSystemAdmin() && actor.schoolId() == null) {
            // System Admin không có schoolId cố định — tìm trong toàn hệ thống theo code.
            return repository.findAllBy(org.springframework.data.domain.Pageable.unpaged()).stream()
                    .filter(s -> s.getCode().equals(code)).findFirst().orElseThrow(RptExceptions::notFound);
        }
        UUID schoolId = actor.schoolId();
        if (schoolId == null) {
            throw RptExceptions.notFound();
        }
        return repository.findByCodeAndSchoolId(code, schoolId).orElseThrow(RptExceptions::notFound);
    }

    private ReportScheduleResponse toResponse(ReportSchedule e) {
        List<String> recipients = referenceResolver.reportEmails(e.getSchoolId());
        return new ReportScheduleResponse(e.getCode(), e.getReportSlug(), e.getFrequency(), e.getFormat(),
                e.getTimeOfDay(), e.getDayOfWeek(), e.getDayOfMonth(), e.getZoneId(), e.isActive(),
                e.getNextRunAt(), recipients, e.getCreatedAt());
    }
}
