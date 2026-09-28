package com.sfms.mf11.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf11.domain.Incident;
import com.sfms.mf11.domain.IncidentRecordDraft;
import com.sfms.mf11.domain.IncidentSourceReport;
import com.sfms.mf11.domain.RecordDraftStatus;
import com.sfms.mf11.domain.SourceReportStatus;
import com.sfms.mf11.dto.IncidentRecordDtos.CreateRecordRequest;
import com.sfms.mf11.dto.IncidentRecordDtos.OpenInvestigationRequest;
import com.sfms.mf11.error.IemError;
import com.sfms.mf11.repository.IncidentEventRepository;
import com.sfms.mf11.repository.IncidentRecordDraftRepository;
import com.sfms.mf11.repository.IncidentRepository;
import com.sfms.mf11.repository.IncidentSourceReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF11-TC-005/006 — {@code IncidentRecordServiceImpl.create()}/{@code openInvestigation()}.
 *
 * <p>Không có {@code IncidentRecordService.openInvestigation()} tách theo TDS mô tả — chữ ký
 * thật trả {@code String} (mã INC), không phải DTO đầy đủ; test đọc code thật trước khi viết.
 */
class IncidentRecordServiceTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID MEDICAL_ID = UUID.randomUUID();
    private static final UUID STUDENT_1 = UUID.randomUUID();
    private static final UUID STUDENT_2 = UUID.randomUUID();
    private static final UUID CLASSROOM_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-30T10:00:00Z"), ZoneOffset.UTC);

    private IncidentRecordDraftRepository repository;
    private IncidentSourceReportRepository sourceReportRepository;
    private IncidentRepository incidentRepository;
    private ReferenceResolver referenceResolver;
    private IncidentRecordServiceImpl service;

    private static AuthUser medicalStaff() {
        return new AuthUser(MEDICAL_ID, "U-0002", "yte@sch.edu.vn", RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
    }

    private static IncidentSourceReport pendingReport(String code) {
        IncidentSourceReport r = new IncidentSourceReport();
        r.setCode(code);
        r.setSchoolId(SCHOOL_ID);
        r.setStatus(SourceReportStatus.PENDING_TRIAGE);
        return r;
    }

    @BeforeEach
    void setUp() {
        repository = mock(IncidentRecordDraftRepository.class);
        sourceReportRepository = mock(IncidentSourceReportRepository.class);
        incidentRepository = mock(IncidentRepository.class);
        referenceResolver = mock(ReferenceResolver.class);
        CodeGenerator codeGenerator = mock(CodeGenerator.class);
        IncidentEventRepository eventRepository = mock(IncidentEventRepository.class);
        IncidentEventLog eventLog = new IncidentEventLog(eventRepository, CLOCK);

        when(codeGenerator.next(anyString(), anyString(), anyInt())).thenAnswer(inv -> {
            String prefix = inv.getArgument(1);
            return prefix + "-014";
        });
        when(referenceResolver.userById(any())).thenReturn(new String[]{"U-0002", "yte@sch.edu.vn", "Y tế trường"});

        service = new IncidentRecordServiceImpl(repository, sourceReportRepository, incidentRepository,
                referenceResolver, codeGenerator, eventLog, CLOCK);
    }

    // ───────────────────────── TC-005 — chỉ gộp RPT PENDING_TRIAGE ─────────────────────────

    @Test
    void mergingASourceReportThatIsNotPendingTriageIsRejectedWith409() {
        IncidentSourceReport drafting = pendingReport("RPT-2026-030");
        drafting.setStatus(SourceReportStatus.DRAFTING);
        when(sourceReportRepository.findByCodeAndSchoolId("RPT-2026-030", SCHOOL_ID)).thenReturn(Optional.of(drafting));

        assertThatThrownBy(() -> service.create(
                new CreateRecordRequest(List.of("RPT-2026-030"), "Nội dung", null, null), medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.RECORD_SOURCE_NOT_PENDING);
                    assertThat(ex.getStatus().value()).isEqualTo(409);
                });
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void creatingARecordFromPendingReportsMarksThemDrafting() {
        IncidentSourceReport r1 = pendingReport("RPT-2026-030");
        when(sourceReportRepository.findByCodeAndSchoolId("RPT-2026-030", SCHOOL_ID)).thenReturn(Optional.of(r1));

        service.create(new CreateRecordRequest(List.of("RPT-2026-030"), "Nội dung", "Bếp trường", null), medicalStaff());

        assertThat(r1.getStatus()).isEqualTo(SourceReportStatus.DRAFTING);
        verify(repository).saveAndFlush(any(IncidentRecordDraft.class));
    }

    @Test
    void openInvestigationRequiresRecordToBeCompletedFirst() {
        IncidentRecordDraft draft = new IncidentRecordDraft();
        draft.setCode("REC-2026-021");
        draft.setSchoolId(SCHOOL_ID);
        draft.setStatus(RecordDraftStatus.DRAFT);
        when(repository.findByCodeAndSchoolId("REC-2026-021", SCHOOL_ID)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.openInvestigation("REC-2026-021",
                new OpenInvestigationRequest("Sự cố", List.of(), null, null, null, null, List.of("HS-3A-001")),
                medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED));
        verify(incidentRepository, never()).saveAndFlush(any());
    }

    // ───────────────────────── TC-006 — khử trùng học sinh khi mở điều tra ─────────────────────────

    @Test
    void openInvestigationDeduplicatesAffectedStudentsByStudentIdAndSnapshotsNameClass() {
        IncidentRecordDraft draft = new IncidentRecordDraft();
        draft.setCode("REC-2026-021");
        draft.setSchoolId(SCHOOL_ID);
        draft.setStatus(RecordDraftStatus.COMPLETED);
        draft.setSourceReportIds(List.of(UUID.randomUUID(), UUID.randomUUID()));
        when(repository.findByCodeAndSchoolId("REC-2026-021", SCHOOL_ID)).thenReturn(Optional.of(draft));
        when(sourceReportRepository.findAllById(draft.getSourceReportIds())).thenReturn(List.of());

        when(referenceResolver.studentByCode("HS-3B-001", SCHOOL_ID)).thenReturn(
                Optional.of(new ReferenceResolver.StudentRef(STUDENT_1, "HS-3B-001", "Nguyễn Minh Khang",
                        CLASSROOM_ID, SCHOOL_ID)));
        when(referenceResolver.studentByCode("HS-3B-002", SCHOOL_ID)).thenReturn(
                Optional.of(new ReferenceResolver.StudentRef(STUDENT_2, "HS-3B-002", "Nguyễn Minh Khang",
                        CLASSROOM_ID, SCHOOL_ID)));
        when(referenceResolver.classroomCode(CLASSROOM_ID)).thenReturn("3B");

        String incidentCode = service.openInvestigation("REC-2026-021",
                new OpenInvestigationRequest("Sự cố nghi ngộ độc", List.of("Đau bụng"), "Cơm gà", null, null, null,
                        List.of("HS-3B-001", "HS-3B-002")),
                medicalStaff());

        assertThat(incidentCode).isEqualTo("INC-2026-014");

        var captor = org.mockito.ArgumentCaptor.forClass(Incident.class);
        verify(incidentRepository).saveAndFlush(captor.capture());
        Incident saved = captor.getValue();
        assertThat(saved.getAffectedStudents()).hasSize(2);
        // 2 học sinh KHÁC studentId nhưng TRÙNG tên vẫn phải là 2 dòng riêng (khử trùng theo id, không theo tên).
        assertThat(saved.getAffectedStudents()).extracting(s -> s.getStudentId())
                .containsExactlyInAnyOrder(STUDENT_1, STUDENT_2);
        assertThat(saved.getAffectedStudents()).allSatisfy(s -> assertThat(s.getStudentName()).isEqualTo("Nguyễn Minh Khang"));

        assertThat(draft.getStatus()).isEqualTo(RecordDraftStatus.OPENED);
    }

    @Test
    void openInvestigationWithAnUnknownAffectedStudentCodeIsRejected() {
        IncidentRecordDraft draft = new IncidentRecordDraft();
        draft.setCode("REC-2026-021");
        draft.setSchoolId(SCHOOL_ID);
        draft.setStatus(RecordDraftStatus.COMPLETED);
        draft.setSourceReportIds(List.of());
        when(repository.findByCodeAndSchoolId("REC-2026-021", SCHOOL_ID)).thenReturn(Optional.of(draft));
        when(referenceResolver.studentByCode("HS-XX", SCHOOL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.openInvestigation("REC-2026-021",
                new OpenInvestigationRequest("Sự cố", List.of(), null, null, null, null, List.of("HS-XX")),
                medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED));
        verify(incidentRepository, never()).saveAndFlush(any());
    }
}
