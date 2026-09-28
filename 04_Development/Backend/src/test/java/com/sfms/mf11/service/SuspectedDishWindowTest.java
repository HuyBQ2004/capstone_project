package com.sfms.mf11.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.port.NotificationPort;
import com.sfms.mf11.domain.Incident;
import com.sfms.mf11.domain.IncidentAffectedStudent;
import com.sfms.mf11.port.FoodSamplePort;
import com.sfms.mf11.port.InventoryBatchPort;
import com.sfms.mf11.port.ServingDishPort;
import com.sfms.mf11.repository.CorrectiveActionRepository;
import com.sfms.mf11.repository.IncidentEventRepository;
import com.sfms.mf11.repository.IncidentRecordDraftRepository;
import com.sfms.mf11.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF11-TC-012 — BR-149, cửa sổ món nghi vấn 24h — {@code IncidentServiceImpl.investigationWindow()}
 * (private), kiểm chứng gián tiếp qua {@code suspectedDishCandidates()} (public), bắt tham số
 * truyền cho {@code ServingDishPort} bằng {@code ArgumentCaptor}.
 *
 * <p>GAP thật vs TestSpec: {@code investigationWindow()} không hỗ trợ giờ Việt Nam khi lùi về
 * {@code mealDate} như văn bản TestSpec gợi ý ("00:00 giờ VN") — code dùng thẳng
 * {@code mealDate.atStartOfDay(ZoneOffset.UTC)}, tức 00:00 UTC (= 07:00 giờ VN), không phải
 * 00:00 giờ VN. Test dưới đây xác nhận đúng hành vi UTC thật, không theo giả định "giờ VN".
 */
class SuspectedDishWindowTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID MEDICAL_ID = UUID.randomUUID();
    private static final UUID STUDENT_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-30T10:00:00Z"), ZoneOffset.UTC);

    private IncidentRepository repository;
    private ServingDishPort servingDishPort;
    private IncidentServiceImpl service;

    private static AuthUser medicalStaff() {
        return new AuthUser(MEDICAL_ID, "U-0002", "yte@sch.edu.vn", RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
    }

    @BeforeEach
    void setUp() {
        repository = mock(IncidentRepository.class);
        IncidentRecordDraftRepository recordRepository = mock(IncidentRecordDraftRepository.class);
        CorrectiveActionRepository capaRepository = mock(CorrectiveActionRepository.class);
        IncidentEventRepository eventRepository = mock(IncidentEventRepository.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        InventoryBatchPort inventoryBatchPort = mock(InventoryBatchPort.class);
        FoodSamplePort foodSamplePort = mock(FoodSamplePort.class);
        servingDishPort = mock(ServingDishPort.class);
        NotificationPort notificationPort = mock(NotificationPort.class);
        IncidentEventLog eventLog = new IncidentEventLog(eventRepository, CLOCK);
        Mf11AttachmentSupport attachmentSupport = mock(Mf11AttachmentSupport.class);

        service = new IncidentServiceImpl(repository, recordRepository, capaRepository, eventRepository,
                referenceResolver, inventoryBatchPort, foodSamplePort, servingDishPort, notificationPort, eventLog,
                attachmentSupport, CLOCK);
    }

    private static Incident incidentWithAffectedStudent(Instant occurredAt) {
        Incident i = new Incident();
        i.setCode("INC-2026-014");
        i.setSchoolId(SCHOOL_ID);
        i.setTitle("Sự cố");
        i.setReportedAt(Instant.parse("2026-06-30T04:00:00Z"));
        i.setReportedByLabel("Y tế");
        i.setOccurredAt(occurredAt);
        IncidentAffectedStudent s = new IncidentAffectedStudent();
        s.setStudentId(STUDENT_ID);
        s.setStudentName("Nguyễn Minh Khang");
        s.setClassName("3A");
        i.getAffectedStudents().add(s);
        return i;
    }

    @Test
    void windowIsExactlyTwentyFourHoursEndingAtOccurredAt() {
        Instant occurredAt = Instant.parse("2026-06-30T04:15:00Z");
        Incident incident = incidentWithAffectedStudent(occurredAt);
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        when(servingDishPort.dishesEatenByStudentsBetween(any(), any(), any(), any())).thenReturn(List.of());

        service.suspectedDishCandidates("INC-2026-014", medicalStaff());

        var fromCaptor = org.mockito.ArgumentCaptor.forClass(Instant.class);
        var toCaptor = org.mockito.ArgumentCaptor.forClass(Instant.class);
        verify(servingDishPort).dishesEatenByStudentsBetween(eq(SCHOOL_ID), eq(List.of(STUDENT_ID)),
                fromCaptor.capture(), toCaptor.capture());

        assertThat(toCaptor.getValue()).isEqualTo(occurredAt);
        assertThat(fromCaptor.getValue()).isEqualTo(occurredAt.minusSeconds(24 * 3600));
        // Không phải 48h/72h — biên đúng bằng 24h, không hơn không kém.
        assertThat(fromCaptor.getValue()).isNotEqualTo(occurredAt.minusSeconds(48 * 3600));
        assertThat(fromCaptor.getValue()).isNotEqualTo(occurredAt.minusSeconds(72 * 3600));
    }

    @Test
    void missingOccurredAtFallsBackToMealDateAtMidnightUtcNotVietnamMidnight() {
        Incident incident = incidentWithAffectedStudent(null);
        incident.setMealDate(java.time.LocalDate.of(2026, 6, 30));
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        when(servingDishPort.dishesEatenByStudentsBetween(any(), any(), any(), any())).thenReturn(List.of());

        service.suspectedDishCandidates("INC-2026-014", medicalStaff());

        var toCaptor = org.mockito.ArgumentCaptor.forClass(Instant.class);
        verify(servingDishPort).dishesEatenByStudentsBetween(any(), any(), any(), toCaptor.capture());
        assertThat(toCaptor.getValue()).isEqualTo(Instant.parse("2026-06-30T00:00:00Z"));
    }

    @Test
    void missingBothOccurredAtAndMealDateFallsBackToReportedAt() {
        Incident incident = incidentWithAffectedStudent(null);
        // mealDate cũng null.
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        when(servingDishPort.dishesEatenByStudentsBetween(any(), any(), any(), any())).thenReturn(List.of());

        service.suspectedDishCandidates("INC-2026-014", medicalStaff());

        var toCaptor = org.mockito.ArgumentCaptor.forClass(Instant.class);
        verify(servingDishPort).dishesEatenByStudentsBetween(any(), any(), any(), toCaptor.capture());
        assertThat(toCaptor.getValue()).isEqualTo(incident.getReportedAt());
    }

    @Test
    void whenNoAffectedStudentsYetFallsBackToSchoolWideDishesWithZeroAffectedCount() {
        Incident incident = new Incident();
        incident.setCode("INC-2026-014");
        incident.setSchoolId(SCHOOL_ID);
        incident.setTitle("Sự cố");
        incident.setReportedAt(Instant.parse("2026-06-30T04:00:00Z"));
        incident.setReportedByLabel("Y tế");
        incident.setOccurredAt(Instant.parse("2026-06-30T04:15:00Z"));
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        UUID dishId = UUID.randomUUID();
        when(servingDishPort.dishesServedBetween(any(), any(), any())).thenReturn(List.of(
                new ServingDishPort.ServedDishBrief(dishId, "Cơm gà", "LUNCH", Instant.parse("2026-06-29T05:00:00Z"))));

        var result = service.suspectedDishCandidates("INC-2026-014", medicalStaff());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).affectedStudentsWhoAte()).isZero();
        verify(servingDishPort, org.mockito.Mockito.never())
                .dishesEatenByStudentsBetween(any(), any(), any(), any());
    }
}
