package com.sfms.mf11.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.port.NotificationPort;
import com.sfms.mf11.domain.IncidentSourceReport;
import com.sfms.mf11.domain.SourceReportStatus;
import com.sfms.mf11.dto.SourceReportDtos.ClusterSignalResponse;
import com.sfms.mf11.repository.IncidentRecordDraftRepository;
import com.sfms.mf11.repository.IncidentRepository;
import com.sfms.mf11.repository.IncidentSourceReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF11-TC-003 — BR-146, {@code SourceReportServiceImpl.clusterSignals()} (không có
 * {@code ClusterSignalService} riêng — kiến trúc thật gộp thẳng vào SourceReportServiceImpl).
 *
 * <p><b>Giới hạn thật của unit test này</b>: cửa sổ 24h được áp ở TẦNG QUERY
 * ({@code cb.greaterThanOrEqualTo(root.get("reportedAt"), since)}, {@code since = now - 24h})
 * — Mockito mock {@code repository.findAll(Specification)} trả thẳng danh sách đã "qua lọc",
 * nên biên 24h/24h+1' của TC-003 KHÔNG kiểm chứng được ở tầng unit (JPA Specification không
 * chạy thật khi repository bị mock). Test dưới đây xác nhận đúng phần LOGIC GOM NHÓM
 * ({@code bySymptom} + ngưỡng {@code >= 3}) chạy trong bộ nhớ sau khi query trả về — biên
 * thời gian thật cần một integration test chạm DB (ngoài phạm vi phiên làm việc này).
 */
class ClusterSignalTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID MEDICAL_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-30T10:00:00Z"), ZoneOffset.UTC);

    private IncidentSourceReportRepository repository;
    private SourceReportServiceImpl service;

    private static AuthUser medicalStaff() {
        return new AuthUser(MEDICAL_ID, "U-0002", "yte@sch.edu.vn", RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
    }

    private static IncidentSourceReport reportWith(String code, List<String> symptoms, Instant reportedAt) {
        IncidentSourceReport r = new IncidentSourceReport();
        r.setCode(code);
        r.setSchoolId(SCHOOL_ID);
        r.setStatus(SourceReportStatus.PENDING_TRIAGE);
        r.setSymptoms(symptoms);
        r.setReportedAt(reportedAt);
        return r;
    }

    @SuppressWarnings("unchecked")
    private void stubRecent(List<IncidentSourceReport> reports) {
        when(repository.findAll(any(Specification.class))).thenReturn(reports);
    }

    @BeforeEach
    void setUp() {
        repository = mock(IncidentSourceReportRepository.class);
        IncidentRepository incidentRepository = mock(IncidentRepository.class);
        IncidentRecordDraftRepository recordRepository = mock(IncidentRecordDraftRepository.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        CodeGenerator codeGenerator = mock(CodeGenerator.class);
        Mf11AttachmentSupport attachmentSupport = mock(Mf11AttachmentSupport.class);
        NotificationPort notificationPort = mock(NotificationPort.class);
        service = new SourceReportServiceImpl(repository, incidentRepository, recordRepository, referenceResolver,
                codeGenerator, attachmentSupport, notificationPort, CLOCK);
    }

    @Test
    void twoReportsWithSameSymptomDoNotFormACluster() {
        stubRecent(List.of(
                reportWith("RPT-1", List.of("Đau bụng"), CLOCK.instant()),
                reportWith("RPT-2", List.of("Đau bụng"), CLOCK.instant())));

        List<ClusterSignalResponse> signals = service.clusterSignals(medicalStaff());

        assertThat(signals).isEmpty();
    }

    @Test
    void threeReportsWithSameSymptomFormExactlyOneCluster() {
        stubRecent(List.of(
                reportWith("RPT-1", List.of("Đau bụng"), CLOCK.instant()),
                reportWith("RPT-2", List.of("Đau bụng"), CLOCK.instant()),
                reportWith("RPT-3", List.of("Đau bụng"), CLOCK.instant())));

        List<ClusterSignalResponse> signals = service.clusterSignals(medicalStaff());

        assertThat(signals).hasSize(1);
        ClusterSignalResponse s = signals.get(0);
        assertThat(s.symptomKey()).isEqualTo("Đau bụng");
        assertThat(s.studentCount()).isEqualTo(3);
        assertThat(s.reportCodes()).containsExactly("RPT-1", "RPT-2", "RPT-3");
    }

    @Test
    void clusterSignalNeverTriggersIncidentCreation() {
        stubRecent(List.of(
                reportWith("RPT-1", List.of("Sốt"), CLOCK.instant()),
                reportWith("RPT-2", List.of("Sốt"), CLOCK.instant()),
                reportWith("RPT-3", List.of("Sốt"), CLOCK.instant()),
                reportWith("RPT-4", List.of("Sốt"), CLOCK.instant())));

        List<ClusterSignalResponse> signals = service.clusterSignals(medicalStaff());

        assertThat(signals).hasSize(1);
        assertThat(signals.get(0).studentCount()).isEqualTo(4);
        // Không có incidentRepository.save nào được service này gọi ở bất kỳ đường nào.
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void differentSymptomsAreGroupedIndependently() {
        stubRecent(List.of(
                reportWith("RPT-1", List.of("Đau bụng"), CLOCK.instant()),
                reportWith("RPT-2", List.of("Đau bụng"), CLOCK.instant()),
                reportWith("RPT-3", List.of("Sốt"), CLOCK.instant()),
                reportWith("RPT-4", List.of("Sốt"), CLOCK.instant())));

        List<ClusterSignalResponse> signals = service.clusterSignals(medicalStaff());

        // 2 + 2, không nhóm nào đạt ngưỡng 3.
        assertThat(signals).isEmpty();
    }
}
