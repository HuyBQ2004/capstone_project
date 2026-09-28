package com.sfms.mf11.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf11.domain.Incident;
import com.sfms.mf11.domain.IncidentAffectedStudent;
import com.sfms.mf11.domain.IncidentEventType;
import com.sfms.mf11.domain.IncidentRecordDraft;
import com.sfms.mf11.domain.IncidentSourceReport;
import com.sfms.mf11.domain.IncidentStatus;
import com.sfms.mf11.domain.RecordDraftStatus;
import com.sfms.mf11.domain.SourceReportStatus;
import com.sfms.mf11.domain.TraceMapStatus;
import com.sfms.mf11.dto.IncidentRecordDtos.CloseNotFoodSafetyRequest;
import com.sfms.mf11.dto.IncidentRecordDtos.CreateRecordRequest;
import com.sfms.mf11.dto.IncidentRecordDtos.IncidentRecordResponse;
import com.sfms.mf11.dto.IncidentRecordDtos.OpenInvestigationRequest;
import com.sfms.mf11.dto.IncidentRecordDtos.UpdateRecordRequest;
import com.sfms.mf11.error.IemExceptions;
import com.sfms.mf11.repository.IncidentRecordDraftRepository;
import com.sfms.mf11.repository.IncidentRepository;
import com.sfms.mf11.repository.IncidentSourceReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class IncidentRecordServiceImpl implements IncidentRecordService {

    private final IncidentRecordDraftRepository repository;
    private final IncidentSourceReportRepository sourceReportRepository;
    private final IncidentRepository incidentRepository;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final IncidentEventLog eventLog;
    private final Clock clock;

    public IncidentRecordServiceImpl(IncidentRecordDraftRepository repository,
                                      IncidentSourceReportRepository sourceReportRepository,
                                      IncidentRepository incidentRepository, ReferenceResolver referenceResolver,
                                      CodeGenerator codeGenerator, IncidentEventLog eventLog, Clock clock) {
        this.repository = repository;
        this.sourceReportRepository = sourceReportRepository;
        this.incidentRepository = incidentRepository;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.eventLog = eventLog;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<IncidentRecordResponse> list(ListQuery query, AuthUser actor) {
        Page<IncidentRecordDraft> page = repository.findAll(
                IncidentRecordSpec.build(query, actor.schoolId()), query.toPageable());
        List<IncidentRecordResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public IncidentRecordResponse getByCode(String code, AuthUser actor) {
        return toResponse(load(code, actor));
    }

    @Override
    @Transactional
    public IncidentRecordResponse create(CreateRecordRequest request, AuthUser actor) {
        List<IncidentSourceReport> sources = new ArrayList<>();
        for (String rptCode : request.sourceReportCodes()) {
            IncidentSourceReport rpt = sourceReportRepository.findByCodeAndSchoolId(rptCode, actor.schoolId())
                    .orElseThrow(() -> IemExceptions.validationFailed("sourceReportCodes", "not found: " + rptCode));
            if (rpt.getStatus() != SourceReportStatus.PENDING_TRIAGE) {
                throw IemExceptions.recordSourceNotPending();
            }
            sources.add(rpt);
        }

        IncidentRecordDraft record = new IncidentRecordDraft();
        record.setCode(codeGenerator.next("mf11_incident_record:" + Year.now(clock).getValue(),
                "REC-" + Year.now(clock).getValue(), 3));
        record.setSchoolId(actor.schoolId());
        record.setContent(request.content());
        record.setStatus(RecordDraftStatus.DRAFT);
        record.setCreatedById(actor.userId());
        record.setLocation(request.location());
        record.setOccurredAt(request.occurredAt());
        record.setSourceReportIds(sources.stream().map(IncidentSourceReport::getId).toList());
        repository.saveAndFlush(record);

        for (IncidentSourceReport rpt : sources) {
            rpt.setStatus(SourceReportStatus.DRAFTING);
            rpt.setDraftRecordId(record.getId());
        }
        sourceReportRepository.saveAll(sources);

        return toResponse(record);
    }

    @Override
    @Transactional
    public IncidentRecordResponse update(String code, UpdateRecordRequest request, AuthUser actor) {
        IncidentRecordDraft record = load(code, actor);
        if (record.getStatus() == RecordDraftStatus.OPENED
                || record.getStatus() == RecordDraftStatus.CLOSED_NOT_FOOD_SAFETY) {
            throw IemExceptions.validationFailed("status", "record is finalized and no longer editable");
        }
        if (request.content() != null) {
            record.setContent(request.content());
        }
        if (request.location() != null) {
            record.setLocation(request.location());
        }
        if (request.occurredAt() != null) {
            record.setOccurredAt(request.occurredAt());
        }
        repository.saveAndFlush(record);
        return toResponse(record);
    }

    @Override
    @Transactional
    public IncidentRecordResponse finalizeRecord(String code, AuthUser actor) {
        IncidentRecordDraft record = load(code, actor);
        if (record.getContent() == null || record.getContent().isBlank()) {
            throw IemExceptions.incompleteReport();
        }
        record.setStatus(RecordDraftStatus.COMPLETED);
        repository.saveAndFlush(record);
        for (IncidentSourceReport rpt : sourceReportRepository.findAllById(record.getSourceReportIds())) {
            rpt.setStatus(SourceReportStatus.DRAFTED);
        }
        return toResponse(record);
    }

    @Override
    @Transactional
    public String openInvestigation(String code, OpenInvestigationRequest request, AuthUser actor) {
        IncidentRecordDraft record = load(code, actor);
        if (record.getStatus() != RecordDraftStatus.COMPLETED) {
            throw IemExceptions.validationFailed("status", "incident record must be COMPLETED before opening investigation");
        }

        Incident incident = new Incident();
        incident.setCode(codeGenerator.next("mf11_incident:" + Year.now(clock).getValue(),
                "INC-" + Year.now(clock).getValue(), 3));
        incident.setSchoolId(actor.schoolId());
        incident.setIncidentRecordId(record.getId());
        incident.setTitle(request.title());
        incident.setReportedAt(Instant.now(clock));
        incident.setOccurredAt(request.occurredAt());
        String[] actorInfo = referenceResolver.userById(actor.userId());
        incident.setReportedByLabel(actorInfo == null ? actor.email() : actorInfo[2]);
        incident.setSymptoms(request.symptoms() == null ? List.of() : request.symptoms());
        incident.setSuspectedMealText(request.suspectedMealText());
        incident.setMealDate(request.mealDate());
        incident.setMealTimeLabel(request.mealTimeLabel());
        incident.setStatus(IncidentStatus.SEVERITY_PENDING);
        incident.setTraceabilityMapStatus(TraceMapStatus.COMPILING);

        for (String studentCode : request.affectedStudentCodes()) {
            var student = referenceResolver.studentByCode(studentCode, actor.schoolId())
                    .orElseThrow(() -> IemExceptions.validationFailed("affectedStudentCodes", "not found: " + studentCode));
            IncidentAffectedStudent affected = new IncidentAffectedStudent();
            affected.setIncident(incident);
            affected.setStudentId(student.id());
            affected.setStudentName(student.name());
            affected.setClassName(referenceResolver.classroomCode(student.classroomId()));
            incident.getAffectedStudents().add(affected);
        }

        incidentRepository.saveAndFlush(incident);

        record.setIncidentId(incident.getId());
        record.setStatus(RecordDraftStatus.OPENED);
        repository.saveAndFlush(record);

        List<IncidentSourceReport> sources = sourceReportRepository.findAllById(record.getSourceReportIds());
        for (IncidentSourceReport rpt : sources) {
            rpt.setStatus(SourceReportStatus.MERGED);
            rpt.setMergedIntoIncidentId(incident.getId());
        }
        sourceReportRepository.saveAll(sources);

        eventLog.append(incident, IncidentEventType.REPORTED,
                "Mở phiên điều tra từ biên bản " + record.getCode() + ".", incident.getReportedByLabel());

        return incident.getCode();
    }

    @Override
    @Transactional
    public IncidentRecordResponse closeNotFoodSafety(String code, CloseNotFoodSafetyRequest request, AuthUser actor) {
        IncidentRecordDraft record = load(code, actor);
        record.setStatus(RecordDraftStatus.CLOSED_NOT_FOOD_SAFETY);
        repository.saveAndFlush(record);
        List<IncidentSourceReport> sources = sourceReportRepository.findAllById(record.getSourceReportIds());
        for (IncidentSourceReport rpt : sources) {
            rpt.setStatus(SourceReportStatus.CLOSED_NOT_FOOD_SAFETY);
        }
        sourceReportRepository.saveAll(sources);
        return toResponse(record);
    }

    private IncidentRecordDraft load(String code, AuthUser actor) {
        return repository.findByCodeAndSchoolId(code, actor.schoolId()).orElseThrow(IemExceptions::notFound);
    }

    private IncidentRecordResponse toResponse(IncidentRecordDraft r) {
        String incidentCode = r.getIncidentId() == null ? null
                : incidentRepository.findById(r.getIncidentId()).map(Incident::getCode).orElse(null);
        List<String> sourceReportCodes = sourceReportRepository.findAllById(r.getSourceReportIds()).stream()
                .map(IncidentSourceReport::getCode).toList();
        return new IncidentRecordResponse(r.getCode(), r.getContent(), r.getStatus().name(), r.getCreatedBy(),
                r.getCreatedAt(), incidentCode, r.getLocation(), r.getOccurredAt(), r.isPdfExported(),
                sourceReportCodes);
    }
}
