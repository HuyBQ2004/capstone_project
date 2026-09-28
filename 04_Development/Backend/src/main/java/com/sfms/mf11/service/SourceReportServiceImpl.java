package com.sfms.mf11.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf11.domain.IncidentSourceReport;
import com.sfms.mf11.domain.Mf11AttachmentOwner;
import com.sfms.mf11.domain.SourceReportStatus;
import com.sfms.mf11.dto.SourceReportDtos.ClusterSignalResponse;
import com.sfms.mf11.dto.SourceReportDtos.CreateSourceReportRequest;
import com.sfms.mf11.dto.SourceReportDtos.SourceReportResponse;
import com.sfms.mf11.error.IemExceptions;
import com.sfms.mf11.repository.IncidentRecordDraftRepository;
import com.sfms.mf11.repository.IncidentRepository;
import com.sfms.mf11.repository.IncidentSourceReportRepository;
import com.sfms.mf10.dto.NotificationDtos.NotificationCommand;
import com.sfms.mf10.port.NotificationPort;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SourceReportServiceImpl implements SourceReportService {

    private final IncidentSourceReportRepository repository;
    private final IncidentRepository incidentRepository;
    private final IncidentRecordDraftRepository recordRepository;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final Mf11AttachmentSupport attachmentSupport;
    private final NotificationPort notificationPort;
    private final Clock clock;

    public SourceReportServiceImpl(IncidentSourceReportRepository repository, IncidentRepository incidentRepository,
                                    IncidentRecordDraftRepository recordRepository,
                                    ReferenceResolver referenceResolver, CodeGenerator codeGenerator,
                                    Mf11AttachmentSupport attachmentSupport, NotificationPort notificationPort,
                                    Clock clock) {
        this.repository = repository;
        this.incidentRepository = incidentRepository;
        this.recordRepository = recordRepository;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.attachmentSupport = attachmentSupport;
        this.notificationPort = notificationPort;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SourceReportResponse> list(ListQuery query, AuthUser actor) {
        UUID schoolId = actor.schoolId();
        List<String> restrictClassNames = actor.role() == RoleKey.TEACHER ? teacherClassNames(actor) : null;
        Page<IncidentSourceReport> page = repository.findAll(
                SourceReportSpec.build(query, schoolId, restrictClassNames), query.toPageable());
        List<SourceReportResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public SourceReportResponse getByCode(String code, AuthUser actor) {
        return toResponse(load(code, actor));
    }

    @Override
    @Transactional
    public SourceReportResponse create(CreateSourceReportRequest request, AuthUser actor) {
        if (request.description() == null || request.description().isBlank()) {
            throw IemExceptions.incompleteReport();
        }

        UUID schoolId;
        String className;
        String reportedByLabel;

        if (actor.role() == RoleKey.PARENT) {
            if (request.studentCode() == null || request.studentCode().isBlank()) {
                throw IemExceptions.validationFailed("studentCode", "required for Parent report");
            }
            var student = referenceResolver.studentByCodeAnySchool(request.studentCode())
                    .orElseThrow(() -> IemExceptions.validationFailed("studentCode", "not found"));
            if (!referenceResolver.isParentLinkedToStudent(actor.userId(), student.id())) {
                throw IemExceptions.accessDenied();
            }
            schoolId = student.schoolId();
            className = referenceResolver.classroomCode(student.classroomId());
        } else if (actor.role() == RoleKey.TEACHER) {
            schoolId = actor.schoolId();
            if (request.className() == null || !teacherClassNames(actor).contains(request.className())) {
                throw IemExceptions.accessDenied();
            }
            className = request.className();
        } else {
            throw IemExceptions.accessDenied();
        }

        String[] actorInfo = referenceResolver.userById(actor.userId());
        reportedByLabel = actorInfo == null ? actor.email() : actorInfo[2];

        IncidentSourceReport report = new IncidentSourceReport();
        report.setCode(codeGenerator.next("mf11_source_report:" + Year.now(clock).getValue(),
                "RPT-" + Year.now(clock).getValue(), 3));
        report.setSchoolId(schoolId);
        report.setReportedById(actor.userId());
        report.setReportedByLabel(reportedByLabel);
        report.setReportedAt(Instant.now(clock));
        report.setClassName(className);
        report.setSymptoms(request.symptoms() == null ? List.of() : request.symptoms());
        report.setSuspectedMealText(request.suspectedMealText());
        // Teacher gộp "Ngày ăn" vào description tự do (27/07/2026) — không còn field cấu
        // trúc riêng; lùi về ngày báo cáo làm mốc tạm khi client không gửi.
        report.setMealDate(request.mealDate() == null ? LocalDate.now(clock) : request.mealDate());
        report.setDescription(request.description());
        report.setEvidencePhotos(Math.max(0, request.evidencePhotos()));
        report.setStatus(SourceReportStatus.PENDING_TRIAGE);
        repository.saveAndFlush(report);

        // Chưa từng gọi NotificationPort ở đây (đo 10/08/2026, theo yêu cầu Nam) — báo cáo GV/PH
        // gửi lên là TÍN HIỆU ĐẦU TIÊN của một sự cố có thể xảy ra, Y tế phải biết ngay để triage,
        // không chờ tới lúc mức độ được set HIGH (đó là bước SAU, ở `assess()`).
        notificationPort.publish(new NotificationCommand(schoolId, false, List.of("medical-staff"),
                com.sfms.common.enums.NotificationType.INCIDENT_ALERT,
                "Báo cáo mới cần triage — " + report.getCode(),
                reportedByLabel + " vừa gửi báo cáo về " + (className == null ? "một học sinh" : "lớp " + className)
                        + " — cần phân loại mức độ (BR-148).",
                "/medical-staff/incidents", "Xem báo cáo"));

        return toResponse(report);
    }

    @Override
    @Transactional
    public SourceReportResponse closeNotFoodSafety(String code, AuthUser actor) {
        IncidentSourceReport report = load(code, actor);
        report.setStatus(SourceReportStatus.CLOSED_NOT_FOOD_SAFETY);
        repository.saveAndFlush(report);
        return toResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClusterSignalResponse> clusterSignals(AuthUser actor) {
        UUID schoolId = actor.schoolId();
        Instant now = Instant.now(clock);
        Instant since = now.minus(Duration.ofHours(24));
        List<IncidentSourceReport> recent = repository.findAll((root, cq, cb) -> cb.and(
                cb.equal(root.get("schoolId"), schoolId),
                cb.equal(root.get("status"), SourceReportStatus.PENDING_TRIAGE),
                cb.greaterThanOrEqualTo(root.get("reportedAt"), since)));

        Map<String, List<IncidentSourceReport>> bySymptom = new LinkedHashMap<>();
        for (IncidentSourceReport r : recent) {
            for (String symptom : r.getSymptoms()) {
                bySymptom.computeIfAbsent(symptom, k -> new ArrayList<>()).add(r);
            }
        }

        List<ClusterSignalResponse> out = new ArrayList<>();
        bySymptom.forEach((symptom, reports) -> {
            if (reports.size() >= 3) {
                out.add(new ClusterSignalResponse(symptom,
                        reports.stream().map(IncidentSourceReport::getCode).toList(),
                        reports.size(), since, now));
            }
        });
        return out;
    }

    @Override
    @Transactional
    public SourceReportResponse uploadEvidence(String code, List<MultipartFile> files, AuthUser actor) {
        IncidentSourceReport report = load(code, actor);
        // Chỉ chính người đã gửi báo cáo được bổ sung ảnh — cùng quyền với create() (BR-146).
        if (!report.getReportedById().equals(actor.userId())) {
            throw IemExceptions.accessDenied();
        }
        attachmentSupport.store(Mf11AttachmentOwner.SOURCE_REPORT, report.getId(), files);
        // evidencePhotos giờ phản ánh SỐ FILE THẬT đã lưu, không còn là số client tự khai lúc create().
        report.setEvidencePhotos((int) attachmentSupport.count(Mf11AttachmentOwner.SOURCE_REPORT, report.getId()));
        repository.saveAndFlush(report);
        return toResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadedFile downloadEvidence(String code, String fileId, AuthUser actor) {
        IncidentSourceReport report = load(code, actor);
        return attachmentSupport.download(Mf11AttachmentOwner.SOURCE_REPORT, report.getId(), UUID.fromString(fileId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.sfms.mf11.dto.IncidentDtos.AttachmentResponse> listEvidence(String code, AuthUser actor) {
        IncidentSourceReport report = load(code, actor);
        return attachmentSupport.list(Mf11AttachmentOwner.SOURCE_REPORT, report.getId()).stream()
                .map(a -> new com.sfms.mf11.dto.IncidentDtos.AttachmentResponse(
                        a.getId().toString(), a.getFileName(), a.getSizeBytes()))
                .toList();
    }

    private List<String> teacherClassNames(AuthUser actor) {
        return referenceResolver.classroomIdsOfHomeroomTeacher(actor.userId()).stream()
                .map(referenceResolver::classroomCode)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private IncidentSourceReport load(String code, AuthUser actor) {
        IncidentSourceReport report = repository.findByCodeAndSchoolId(code, actor.schoolId())
                .orElseThrow(IemExceptions::notFound);
        if (actor.role() == RoleKey.TEACHER && !teacherClassNames(actor).contains(report.getClassName())) {
            throw IemExceptions.accessDenied();
        }
        // PARENT chỉ xem/tải được báo cáo do CHÍNH mình gửi — endpoint evidence-photos mở thêm
        // cho PARENT (khác get()/list() vốn không cho PARENT), phải chặn ở đây để không lộ
        // ảnh bằng chứng của phụ huynh khác cùng trường.
        if (actor.role() == RoleKey.PARENT && !report.getReportedById().equals(actor.userId())) {
            throw IemExceptions.accessDenied();
        }
        return report;
    }

    private SourceReportResponse toResponse(IncidentSourceReport r) {
        return new SourceReportResponse(r.getCode(), referenceResolver.userCode(r.getReportedById()),
                r.getReportedByLabel(), r.getReportedAt(), r.getClassName(), r.getSymptoms(),
                r.getSuspectedMealText(), r.getMealDate(), r.getDescription(), r.getEvidencePhotos(),
                r.getSeverityLevel() == null ? null : r.getSeverityLevel().name(), r.getStatus().name(),
                resolveIncidentCode(r.getMergedIntoIncidentId()), resolveRecordCode(r.getDraftRecordId()));
    }

    /** D1 — JSON không bao giờ lộ UUID; trả về {@code code} của Incident, không phải id nội bộ. */
    private String resolveIncidentCode(UUID incidentId) {
        return incidentId == null ? null : incidentRepository.findById(incidentId).map(i -> i.getCode()).orElse(null);
    }

    /** D1 — {@code draftRecordId} trả mã REC (không phải UUID) để FE mở đúng nháp đang soạn. */
    private String resolveRecordCode(UUID recordId) {
        return recordId == null ? null : recordRepository.findById(recordId).map(r -> r.getCode()).orElse(null);
    }
}
