package com.sfms.mf11.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.storage.StorageService;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf11.domain.ResolutionStatus;
import com.sfms.mf11.domain.ViolationFile;
import com.sfms.mf11.domain.ViolationFileKind;
import com.sfms.mf11.domain.ViolationRecord;
import com.sfms.mf11.domain.ViolationSeverity;
import com.sfms.mf11.domain.ViolationStatus;
import com.sfms.mf11.domain.ViolationType;
import com.sfms.mf11.dto.ViolationDtos.CreateViolationRequest;
import com.sfms.mf11.dto.ViolationDtos.OverrideAbuseRow;
import com.sfms.mf11.dto.ViolationDtos.RejectResolutionRequest;
import com.sfms.mf11.dto.ViolationDtos.ResolutionRequest;
import com.sfms.mf11.dto.ViolationDtos.ViolationFileDto;
import com.sfms.mf11.dto.ViolationDtos.ViolationResponse;
import com.sfms.mf11.dto.ViolationDtos.ViolationSummaryResponse;
import com.sfms.mf11.error.IemExceptions;
import com.sfms.mf11.repository.IncidentRepository;
import com.sfms.mf11.repository.ViolationRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.WeekFields;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ViolationServiceImpl implements ViolationService {

    private final ViolationRecordRepository repository;
    private final IncidentRepository incidentRepository;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final StorageService storageService;
    private final Clock clock;

    public ViolationServiceImpl(ViolationRecordRepository repository, IncidentRepository incidentRepository,
                                 ReferenceResolver referenceResolver, CodeGenerator codeGenerator,
                                 StorageService storageService, Clock clock) {
        this.repository = repository;
        this.incidentRepository = incidentRepository;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.storageService = storageService;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ViolationResponse> list(ListQuery query, AuthUser actor) {
        // Của "đơn vị mình" ở đây là đại diện của CHÍNH tài khoản đang đăng nhập (đơn giản hoá:
        // 1 tài khoản = 1 đại diện, khớp seed U-9911). Đơn vị có nhiều đại diện cần Port riêng
        // liệt kê đại diện theo partnerId — ghi nợ, không chặn MVP.
        List<UUID> ownRepresentatives = actor.role() == RoleKey.SERVICE_CATERERS
                ? List.of(actor.userId()) : null;
        // `incidentId` trong query string là MÃ (D1, "INC-2026-014") nhưng cột đích
        // `relatedIncidentId` là UUID — SpecSupport chỉ so sánh chuỗi thô, phải tự dịch
        // mã → UUID TRƯỚC khi build Specification (không mã nào tồn tại ⇒ ép rỗng, không
        // ném lỗi, giống hành vi "field lạ" của SpecSupport).
        String incidentCode = query.filters().get("incidentId");
        if (incidentCode != null && !incidentCode.isBlank()) {
            String resolvedId = incidentRepository.findByCodeAndSchoolId(incidentCode, schoolId(actor))
                    .map(i -> i.getId().toString())
                    .orElse("00000000-0000-0000-0000-000000000000");
            Map<String, String> filters = new LinkedHashMap<>(query.filters());
            filters.put("incidentId", resolvedId);
            query = new ListQuery(query.page(), query.pageSize(), query.search(), query.dateFrom(),
                    query.dateTo(), query.mealPrefix(), Map.copyOf(filters));
        }
        Page<ViolationRecord> page = repository.findAll(
                ViolationSpec.build(query, schoolId(actor), ownRepresentatives), query.toPageable());
        List<ViolationResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public ViolationResponse getByCode(String code, AuthUser actor) {
        return toResponse(load(code, actor));
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.sfms.common.reference.ReferenceResolver.UserBrief> catererRepresentatives(AuthUser actor) {
        return referenceResolver.serviceCatererRepresentatives(schoolId(actor));
    }

    @Override
    @Transactional
    public ViolationResponse create(CreateViolationRequest request, AuthUser actor) {
        if (request.subject() == null || request.subject().isBlank()
                || request.description() == null || request.description().isBlank()) {
            throw IemExceptions.incompleteViolation();
        }
        var rep = referenceResolver.userIdByCode(request.caterersRepresentativeCode())
                .orElseThrow(() -> IemExceptions.validationFailed("caterersRepresentativeCode", "not found"));
        String[] repInfo = referenceResolver.userById(rep);

        ViolationSeverity severity = parseSeverity(request.severity());

        ViolationRecord violation = new ViolationRecord();
        violation.setCode(codeGenerator.next("mf11_violation:" + Year.now(clock).getValue(),
                "VIO-" + Year.now(clock).getValue(), 3));
        violation.setSchoolId(schoolId(actor));
        violation.setSubject(request.subject());
        violation.setRecordedById(actor.userId());
        String[] actorInfo = referenceResolver.userById(actor.userId());
        violation.setRecordedByLabel(actorInfo == null ? actor.email() : actorInfo[2]);
        violation.setDate(request.date());
        violation.setSeverity(severity);
        violation.setViolationType(parseViolationType(request.violationType()));
        violation.setDescription(request.description());
        violation.setOccurredAt(request.occurredAt());
        violation.setLocationOfIncident(request.locationOfIncident());
        violation.setCaterersRepresentativeId(rep);
        violation.setCaterersRepresentativeLabel(repInfo == null ? request.caterersRepresentativeCode() : repInfo[2]);
        violation.setWitnessLabel(request.witnessLabel());
        violation.setSelfReported(request.selfReported());

        // BR-187/D12 — server TỰ DÒ tái diễn, KHÔNG nhận từ client (CF-M11-16).
        boolean repeat = severity != ViolationSeverity.LOW
                && !repository.findByCaterersRepresentativeIdAndDateBetween(rep,
                        request.date().minusDays(90), request.date()).isEmpty();
        violation.setRepeatSuspect(repeat);

        if (request.relatedIncidentCode() != null) {
            incidentRepository.findByCodeAndSchoolId(request.relatedIncidentCode(), schoolId(actor))
                    .ifPresent(i -> violation.setRelatedIncidentId(i.getId()));
        }
        if (request.sourceInspectionCode() != null) {
            referenceResolver.inspectionIdByCode(request.sourceInspectionCode(), schoolId(actor))
                    .ifPresent(violation::setSourceInspectionId);
        }
        repository.saveAndFlush(violation);
        return toResponse(violation);
    }

    @Override
    @Transactional
    public ViolationResponse exportPdf(String code, AuthUser actor) {
        ViolationRecord v = load(code, actor);
        // Đơn giản hoá MVP: đánh dấu đã xuất, KHÔNG dựng PDF thật (PDFBox) — cùng tinh thần report().
        v.setPdfExported(true);
        v.setPdfGeneratedAt(Instant.now(clock));
        v.setPdfVersion(v.getPdfVersion() + 1);
        repository.saveAndFlush(v);
        return toResponse(v);
    }

    @Override
    @Transactional
    public ViolationResponse uploadSignedRecord(String code, MultipartFile file, AuthUser actor) {
        ViolationRecord v = load(code, actor);
        if (!v.isPdfExported()) {
            throw IemExceptions.pdfNotExported();
        }
        v.getFiles().add(storeFile(v, file, ViolationFileKind.SIGNED_RECORD, "-signed"));
        repository.saveAndFlush(v);
        return toResponse(v);
    }

    @Override
    @Transactional
    public ViolationResponse uploadEvidence(String code, List<MultipartFile> files, AuthUser actor) {
        ViolationRecord v = load(code, actor);
        for (MultipartFile file : files) {
            v.getFiles().add(storeFile(v, file, ViolationFileKind.EVIDENCE, "-evidence"));
        }
        repository.saveAndFlush(v);
        return toResponse(v);
    }

    private ViolationFile storeFile(ViolationRecord v, MultipartFile file, ViolationFileKind kind, String suffix) {
        String fileCode = codeGenerator.next("mf11_violation_file:" + Year.now(clock).getValue(),
                "VF-" + Year.now(clock).getValue(), 3);
        String ext = extensionOf(file.getOriginalFilename());
        String key = "violation/" + v.getCode() + suffix + "-" + fileCode + (ext.isEmpty() ? "" : "." + ext);
        storageService.store(key, file);

        ViolationFile record = new ViolationFile();
        record.setCode(fileCode);
        record.setViolation(v);
        record.setKind(kind);
        record.setName(file.getOriginalFilename() == null ? key : file.getOriginalFilename());
        record.setStorageKey(key);
        record.setSizeLabel(humanSize(file.getSize()));
        record.setUploadedAt(Instant.now(clock));
        return record;
    }

    @Override
    @Transactional
    public ViolationResponse deleteSignedRecord(String code, String fileCode, AuthUser actor) {
        ViolationRecord v = load(code, actor);
        if (v.getResolution() != ResolutionStatus.PENDING) {
            throw IemExceptions.validationFailed("resolution", "cannot remove signed record after submission");
        }
        v.getFiles().removeIf(f -> f.getCode().equals(fileCode));
        repository.saveAndFlush(v);
        return toResponse(v);
    }

    @Override
    @Transactional
    public ViolationResponse submitResolution(String code, ResolutionRequest request, AuthUser actor) {
        ViolationRecord v = load(code, actor);
        boolean hasSignedRecord = v.getFiles().stream().anyMatch(f -> f.getKind() == ViolationFileKind.SIGNED_RECORD);
        if (!hasSignedRecord) {
            throw IemExceptions.signedRecordMissing();
        }
        v.setActionTaken(request.actionTaken());
        boolean needsLevel = v.getSeverity() != ViolationSeverity.LOW;
        if (request.unitSanctionLevel() != null && !request.unitSanctionLevel().isBlank()) {
            try {
                v.setUnitSanctionLevel(com.sfms.mf11.domain.UnitSanctionLevel.valueOf(request.unitSanctionLevel()));
            } catch (Exception e) {
                throw IemExceptions.validationFailed("unitSanctionLevel", "invalid value");
            }
        } else if (needsLevel) {
            throw IemExceptions.validationFailed("unitSanctionLevel", "required when severity >= MEDIUM (BR-186)");
        }
        v.setHandledById(actor.userId());
        if (v.getSeverity() == ViolationSeverity.LOW) {
            v.setResolution(ResolutionStatus.APPROVED);
            v.setStatus(ViolationStatus.CLOSED);
        } else {
            v.setResolution(ResolutionStatus.PENDING_APPROVAL);
        }
        repository.saveAndFlush(v);
        return toResponse(v);
    }

    @Override
    @Transactional
    public ViolationResponse approveResolution(String code, AuthUser actor) {
        ViolationRecord v = load(code, actor);
        if (v.getResolution() != ResolutionStatus.PENDING_APPROVAL) {
            throw IemExceptions.validationFailed("resolution", "not pending approval");
        }
        v.setResolution(ResolutionStatus.APPROVED);
        v.setStatus(ViolationStatus.CLOSED);
        repository.saveAndFlush(v);
        return toResponse(v);
    }

    @Override
    @Transactional
    public ViolationResponse rejectResolution(String code, RejectResolutionRequest request, AuthUser actor) {
        ViolationRecord v = load(code, actor);
        if (v.getResolution() != ResolutionStatus.PENDING_APPROVAL) {
            throw IemExceptions.validationFailed("resolution", "not pending approval");
        }
        if (request.reason() == null || request.reason().isBlank()) {
            throw IemExceptions.reasonRequired("reason");
        }
        v.setResolution(ResolutionStatus.REJECTED);
        v.setRejectReason(request.reason());
        repository.saveAndFlush(v);
        return toResponse(v);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OverrideAbuseRow> overrideAbuseReport(String week, AuthUser actor) {
        // ADR-M11-6 — dẫn xuất runtime, KHÔNG bảng riêng. Gom theo tuần ISO × đại diện đơn vị.
        List<ViolationRecord> all = repository.findAll((root, cq, cb) -> cb.and(
                cb.equal(root.get("schoolId"), schoolId(actor)),
                cb.equal(root.get("violationType"), ViolationType.OVERRIDE_ABUSE)));

        Map<String, Long> counts = new LinkedHashMap<>();
        Map<String, String> labels = new LinkedHashMap<>();
        WeekFields wf = WeekFields.ISO;
        for (ViolationRecord v : all) {
            String key = v.getDate().get(wf.weekBasedYear()) + "-W" + v.getDate().get(wf.weekOfWeekBasedYear())
                    + "|" + v.getCaterersRepresentativeId();
            if (week != null && !week.isBlank()
                    && !key.startsWith(week)) {
                continue;
            }
            counts.merge(key, 1L, Long::sum);
            labels.put(key, v.getCaterersRepresentativeLabel());
        }

        List<OverrideAbuseRow> out = new java.util.ArrayList<>();
        counts.forEach((key, count) -> {
            String weekPart = key.substring(0, key.indexOf('|'));
            out.add(new OverrideAbuseRow(weekPart, labels.get(key), count, 3));
        });
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public ViolationSummaryResponse summary(LocalDate dateFrom, LocalDate dateTo, AuthUser actor) {
        List<ViolationRecord> rows = repository.findAll((root, cq, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.equal(root.get("schoolId"), schoolId(actor)));
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), dateTo));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        });
        long approved = rows.stream().filter(v -> v.getResolution() == ResolutionStatus.APPROVED).count();
        long pending = rows.stream().filter(v -> v.getResolution() == ResolutionStatus.PENDING_APPROVAL).count();
        long rejected = rows.stream().filter(v -> v.getResolution() == ResolutionStatus.REJECTED).count();
        long open = rows.stream().filter(v -> v.getStatus() == ViolationStatus.OPEN).count();
        long closed = rows.stream().filter(v -> v.getStatus() == ViolationStatus.CLOSED).count();
        return new ViolationSummaryResponse(rows.size(), approved, pending, rejected, open, closed);
    }

    private ViolationRecord load(String code, AuthUser actor) {
        return repository.findByCodeAndSchoolId(code, schoolId(actor)).orElseThrow(IemExceptions::notFound);
    }

    private ViolationSeverity parseSeverity(String raw) {
        try {
            return ViolationSeverity.valueOf(raw);
        } catch (Exception e) {
            throw IemExceptions.validationFailed("severity", "must be LOW, MEDIUM or HIGH");
        }
    }

    private ViolationType parseViolationType(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return ViolationType.valueOf(raw);
        } catch (Exception e) {
            throw IemExceptions.validationFailed("violationType", "invalid value");
        }
    }

    private static String extensionOf(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    private static String humanSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        return (bytes / 1024) + " KB";
    }

    private ViolationResponse toResponse(ViolationRecord v) {
        String relatedIncidentCode = v.getRelatedIncidentId() == null ? null
                : incidentRepository.findById(v.getRelatedIncidentId()).map(i -> i.getCode()).orElse(null);
        String sourceInspectionCode = v.getSourceInspectionId() == null ? null
                : referenceResolver.inspectionCode(v.getSourceInspectionId());
        String handledByLabel = v.getHandledById() == null ? null : label(v.getHandledById());
        List<ViolationFileDto> files = v.getFiles().stream()
                .map(f -> new ViolationFileDto(f.getCode(), f.getKind().name(), f.getName(), f.getSizeLabel(),
                        f.getUploadedAt()))
                .toList();
        return new ViolationResponse(v.getCode(), v.getSubject(), v.getRecordedByLabel(), v.getDate(),
                v.getSeverity().name(), v.getResolution().name(), v.getStatus().name(),
                v.getViolationType() == null ? null : v.getViolationType().name(), v.getDescription(),
                v.getOccurredAt(), v.getLocationOfIncident(),
                referenceResolver.userCode(v.getCaterersRepresentativeId()), v.getCaterersRepresentativeLabel(),
                v.getWitnessLabel(), v.isSelfReported(), v.isRepeatSuspect(), relatedIncidentCode,
                sourceInspectionCode, v.isPdfExported(), v.getPdfGeneratedAt(), v.getPdfVersion(),
                v.getActionTaken(), v.getUnitSanctionLevel() == null ? null : v.getUnitSanctionLevel().name(),
                handledByLabel, v.getRejectReason(), files);
    }

    private String label(UUID userId) {
        String[] info = referenceResolver.userById(userId);
        return info == null ? null : info[2];
    }

    /** Phạm vi trường — xem {@link Mf11Scope#schoolId} (tài khoản đối tác không có claim schoolId). */
    private UUID schoolId(AuthUser actor) {
        return Mf11Scope.schoolId(actor, referenceResolver);
    }

}
