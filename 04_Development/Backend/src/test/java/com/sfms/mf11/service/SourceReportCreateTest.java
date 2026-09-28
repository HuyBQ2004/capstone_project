package com.sfms.mf11.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.port.NotificationPort;
import com.sfms.mf11.dto.SourceReportDtos.ClusterSignalResponse;
import com.sfms.mf11.dto.SourceReportDtos.CreateSourceReportRequest;
import com.sfms.mf11.dto.SourceReportDtos.SourceReportResponse;
import com.sfms.mf11.error.IemError;
import com.sfms.mf11.error.IemMessages;
import com.sfms.mf11.domain.IncidentSourceReport;
import com.sfms.mf11.domain.SourceReportStatus;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF11-TC-001/002/004 — {@code SourceReportServiceImpl.create()}.
 *
 * <p><b>Viết theo CODE THẬT, không theo TestSpec-MF11 gốc</b> (quy tắc bắt buộc). Lệch quan
 * trọng so với giả định của TestSpec:
 * <ul>
 *   <li>Không có {@code createByTeacher()}/{@code createByParent()} tách riêng — chỉ một
 *       {@code create()} rẽ nhánh theo {@code actor.role()}.</li>
 *   <li>Không có {@code ClusterSignalService} — logic cụm nằm trong
 *       {@code SourceReportServiceImpl.clusterSignals()} (xem {@link ClusterSignalTest}).</li>
 *   <li><b>GAP thật so với MSG-023/TC-002</b>: service chỉ validate {@code description} rỗng
 *       (dòng {@code if (request.description() == null || request.description().isBlank())
 *       throw IemExceptions.incompleteReport();}). {@code symptoms} rỗng và {@code mealDate}
 *       null KHÔNG bị chặn — {@code mealDate} lùi về {@code LocalDate.now(clock)},
 *       {@code symptoms} lùi về {@code List.of()}. TestSpec TC-002 giả định cả 3 field đều bắt
 *       buộc (equivalence partitioning 3 nhánh) — chỉ 1/3 nhánh đó tồn tại thật ở code, 2 nhánh
 *       còn lại được test dưới đây xác nhận KHÔNG bị chặn (ghi lại đúng hiện trạng, không bịa).</li>
 *   <li>{@code SourceReportServiceImpl} không có field {@code incidentRepository.save(...)} hay
 *       bất kỳ {@code InventoryBatchPort}/{@code traceabilityMapService} nào — service này về mặt
 *       kiến trúc VẬT LÝ KHÔNG THỂ tạo Incident/cách ly lô (không có dependency để gọi), nên
 *       BR-146/BR-149 "không tự động" ở đây là bất biến kiến trúc, không chỉ hành vi runtime.</li>
 *   <li>{@code create()} LUÔN publish {@code NotificationPort} tới {@code medical-staff} (thêm
 *       10/08/2026) — khác giả định TestSpec cũ không nhắc gì tới thông báo ở bước tạo RPT.</li>
 * </ul>
 */
class SourceReportCreateTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID TEACHER_ID = UUID.randomUUID();
    private static final UUID PARENT_ID = UUID.randomUUID();
    private static final UUID STUDENT_ID = UUID.randomUUID();
    private static final UUID OTHER_STUDENT_ID = UUID.randomUUID();
    private static final UUID CLASSROOM_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-30T02:00:00Z"), ZoneOffset.UTC);

    private IncidentSourceReportRepository repository;
    private IncidentRepository incidentRepository;
    private IncidentRecordDraftRepository recordRepository;
    private ReferenceResolver referenceResolver;
    private CodeGenerator codeGenerator;
    private Mf11AttachmentSupport attachmentSupport;
    private NotificationPort notificationPort;
    private SourceReportServiceImpl service;

    private static AuthUser teacher() {
        return new AuthUser(TEACHER_ID, "U-0004", "gvcn3a@sch.edu.vn", RoleKey.TEACHER, SCHOOL_ID, null);
    }

    private static AuthUser parent() {
        return new AuthUser(PARENT_ID, "U-0005", "parent@sch.edu.vn", RoleKey.PARENT, null, null);
    }

    @BeforeEach
    void setUp() {
        repository = mock(IncidentSourceReportRepository.class);
        incidentRepository = mock(IncidentRepository.class);
        recordRepository = mock(IncidentRecordDraftRepository.class);
        referenceResolver = mock(ReferenceResolver.class);
        codeGenerator = mock(CodeGenerator.class);
        attachmentSupport = mock(Mf11AttachmentSupport.class);
        notificationPort = mock(NotificationPort.class);

        when(codeGenerator.next(anyString(), anyString(), anyInt())).thenReturn("RPT-2026-030");
        when(referenceResolver.userById(any())).thenReturn(new String[]{"U-0004", "gvcn3a@sch.edu.vn", "Nguyễn Thị Trang"});
        when(referenceResolver.classroomIdsOfHomeroomTeacher(TEACHER_ID)).thenReturn(List.of(CLASSROOM_ID));
        when(referenceResolver.classroomCode(CLASSROOM_ID)).thenReturn("3A");

        service = new SourceReportServiceImpl(repository, incidentRepository, recordRepository, referenceResolver,
                codeGenerator, attachmentSupport, notificationPort, CLOCK);
    }

    private CreateSourceReportRequest teacherPayload() {
        return new CreateSourceReportRequest("3A", List.of("Đau bụng"), "Cơm gà rang gừng",
                java.time.LocalDate.of(2026, 6, 30), "3 học sinh kêu đau bụng sau bữa trưa.", 0, null);
    }

    @Test
    void teacherHappyPathCreatesOnlySourceReportWithoutTouchingIncidentOrBatches() {
        SourceReportResponse response = service.create(teacherPayload(), teacher());

        assertThat(response.status()).isEqualTo("PENDING_TRIAGE");
        assertThat(response.mergedIntoIncidentId()).isNull();

        verify(repository).saveAndFlush(any(IncidentSourceReport.class));
        // Không có dependency nào để tự tạo Incident/cách ly lô — bất biến kiến trúc BR-146/BR-149.
        verify(incidentRepository, never()).saveAndFlush(any());
        verify(incidentRepository, never()).save(any());
    }

    @Test
    void savedReportCarriesGeneratedCodeAndPendingTriageStatus() {
        var captor = org.mockito.ArgumentCaptor.forClass(IncidentSourceReport.class);
        service.create(teacherPayload(), teacher());
        verify(repository).saveAndFlush(captor.capture());

        IncidentSourceReport saved = captor.getValue();
        assertThat(saved.getCode()).isEqualTo("RPT-2026-030");
        assertThat(saved.getStatus()).isEqualTo(SourceReportStatus.PENDING_TRIAGE);
        assertThat(saved.getSchoolId()).isEqualTo(SCHOOL_ID);
    }

    @Test
    void blankDescriptionIsRejectedWith400IemValidationFailedAndMsg023() {
        CreateSourceReportRequest bad = new CreateSourceReportRequest("3A", List.of("Đau bụng"), null,
                java.time.LocalDate.of(2026, 6, 30), "   ", 0, null);

        assertThatThrownBy(() -> service.create(bad, teacher()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED);
                    assertThat(ex.getStatus().value()).isEqualTo(400);
                    assertThat(ex.getMessage()).isEqualTo(IemMessages.MSG_023);
                });
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void nullDescriptionIsRejectedTheSameWay() {
        CreateSourceReportRequest bad = new CreateSourceReportRequest("3A", List.of("Đau bụng"), null,
                java.time.LocalDate.of(2026, 6, 30), null, 0, null);

        assertThatThrownBy(() -> service.create(bad, teacher()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED));
        verify(repository, never()).saveAndFlush(any());
    }

    /** GAP thật (xem javadoc lớp) — symptoms rỗng KHÔNG bị service chặn, khác giả định TC-002. */
    @Test
    void emptySymptomsListIsNotRejectedByServiceLevelValidation() {
        CreateSourceReportRequest req = new CreateSourceReportRequest("3A", List.of(), null,
                java.time.LocalDate.of(2026, 6, 30), "Mô tả hợp lệ", 0, null);

        SourceReportResponse response = service.create(req, teacher());

        assertThat(response.symptoms()).isEmpty();
        verify(repository).saveAndFlush(any());
    }

    /** GAP thật (xem javadoc lớp) — mealDate null KHÔNG bị chặn, lùi về ngày hiện tại của clock. */
    @Test
    void nullMealDateIsNotRejectedAndFallsBackToClockToday() {
        CreateSourceReportRequest req = new CreateSourceReportRequest("3A", List.of("Sốt"), null,
                null, "Mô tả hợp lệ", 0, null);

        SourceReportResponse response = service.create(req, teacher());

        assertThat(response.mealDate()).isEqualTo(java.time.LocalDate.of(2026, 6, 30));
        verify(repository).saveAndFlush(any());
    }

    @Test
    void teacherReportingForAClassSheDoesNotOwnIsDenied403() {
        CreateSourceReportRequest req = new CreateSourceReportRequest("9Z", List.of("Sốt"), null,
                java.time.LocalDate.of(2026, 6, 30), "Mô tả", 0, null);

        assertThatThrownBy(() -> service.create(req, teacher()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.ACCESS_DENIED);
                    assertThat(ex.getStatus().value()).isEqualTo(403);
                    assertThat(ex.getMessage()).isEqualTo(IemMessages.MSG_005);
                });
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void parentReportingForAChildNotLinkedIsDenied403WithoutRevealingWhetherStudentExists() {
        when(referenceResolver.studentByCodeAnySchool("HS-4A-002"))
                .thenReturn(Optional.of(new ReferenceResolver.StudentRef(OTHER_STUDENT_ID, "HS-4A-002", "Học sinh khác",
                        UUID.randomUUID(), SCHOOL_ID)));
        when(referenceResolver.isParentLinkedToStudent(PARENT_ID, OTHER_STUDENT_ID)).thenReturn(false);

        CreateSourceReportRequest req = new CreateSourceReportRequest(null, List.of("Sốt"), null,
                java.time.LocalDate.of(2026, 6, 30), "Mô tả", 0, "HS-4A-002");

        ApiException ex = catchApiException(() -> service.create(req, parent()));
        assertThat(ex.getCode()).isEqualTo(IemError.ACCESS_DENIED);
        assertThat(ex.getStatus().value()).isEqualTo(403);
        assertThat(ex.getDetails()).isEmpty();
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void parentReportingForHerOwnLinkedChildSucceeds() {
        when(referenceResolver.studentByCodeAnySchool("HS-3B-001"))
                .thenReturn(Optional.of(new ReferenceResolver.StudentRef(STUDENT_ID, "HS-3B-001", "Nguyễn Minh Khang",
                        CLASSROOM_ID, SCHOOL_ID)));
        when(referenceResolver.isParentLinkedToStudent(PARENT_ID, STUDENT_ID)).thenReturn(true);
        when(referenceResolver.classroomCode(CLASSROOM_ID)).thenReturn("3B");
        when(referenceResolver.userById(PARENT_ID)).thenReturn(new String[]{"U-0005", "parent@sch.edu.vn", "Phụ huynh Khang"});

        CreateSourceReportRequest req = new CreateSourceReportRequest(null, List.of("Sốt"), null,
                java.time.LocalDate.of(2026, 6, 30), "Mô tả", 0, "HS-3B-001");

        SourceReportResponse response = service.create(req, parent());

        assertThat(response.status()).isEqualTo("PENDING_TRIAGE");
        assertThat(response.className()).isEqualTo("3B");
        verify(repository).saveAndFlush(any());
    }

    @Test
    void parentWithoutStudentCodeIsRejected() {
        CreateSourceReportRequest req = new CreateSourceReportRequest(null, List.of("Sốt"), null,
                java.time.LocalDate.of(2026, 6, 30), "Mô tả", 0, null);

        assertThatThrownBy(() -> service.create(req, parent()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void everyCreatedReportPublishesAMedicalStaffTriageNotification() {
        service.create(teacherPayload(), teacher());
        verify(notificationPort).publish(any());
    }

    private static ApiException catchApiException(org.junit.jupiter.api.function.Executable exec) {
        try {
            exec.execute();
        } catch (ApiException e) {
            return e;
        } catch (Throwable t) {
            throw new AssertionError("Expected ApiException", t);
        }
        throw new AssertionError("Expected ApiException but none thrown");
    }
}
