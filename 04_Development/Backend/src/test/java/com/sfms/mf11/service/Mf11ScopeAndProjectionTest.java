package com.sfms.mf11.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.port.NotificationPort;
import com.sfms.mf11.domain.CapaStatus;
import com.sfms.mf11.domain.CorrectiveAction;
import com.sfms.mf11.domain.Incident;
import com.sfms.mf11.domain.IncidentAffectedStudent;
import com.sfms.mf11.domain.IncidentEvent;
import com.sfms.mf11.domain.IncidentEventType;
import com.sfms.mf11.domain.IncidentQuarantineBatch;
import com.sfms.mf11.domain.IncidentStatus;
import com.sfms.mf11.domain.IncidentSuspectedDish;
import com.sfms.mf11.domain.TraceMapStatus;
import com.sfms.mf11.dto.IncidentDtos.AssessRequest;
import com.sfms.mf11.dto.IncidentDtos.CloseRequest;
import com.sfms.mf11.dto.IncidentDtos.IncidentCaterersResponse;
import com.sfms.mf11.dto.IncidentDtos.IncidentEventResponse;
import com.sfms.mf11.dto.IncidentDtos.IncidentResponse;
import com.sfms.mf11.dto.IncidentDtos.IncidentTeacherResponse;
import com.sfms.mf11.dto.IncidentDtos.QuarantineRequest;
import com.sfms.mf11.error.IemError;
import com.sfms.mf11.error.IemMessages;
import com.sfms.mf11.port.FoodSamplePort;
import com.sfms.mf11.port.InventoryBatchPort;
import com.sfms.mf11.port.InventoryBatchPort.BatchBrief;
import com.sfms.mf11.port.ServingDishPort;
import com.sfms.mf11.repository.CorrectiveActionRepository;
import com.sfms.mf11.repository.IncidentEventRepository;
import com.sfms.mf11.repository.IncidentRecordDraftRepository;
import com.sfms.mf11.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Phạm vi (scope) và projection theo vai của {@code IncidentServiceImpl} — phần cắt dữ liệu
 * sức khoẻ, phạm vi lớp chủ nhiệm, phạm vi đơn vị suất ăn, và cổng đóng sự cố.
 *
 * <p>Nguồn: {@code Security_Access_Control.md}, {@code Incident_Closure_And_CAPA.md},
 * {@code Incident_Investigation_And_Quarantine.md}. Chỗ nào tài liệu đoán khác code thật thì
 * assert theo code thật và ghi chú ngay tại chỗ.
 */
class Mf11ScopeAndProjectionTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID OTHER_SCHOOL_ID = UUID.randomUUID();
    private static final UUID MEDICAL_ID = UUID.randomUUID();
    private static final UUID SCHOOL_ADMIN_ID = UUID.randomUUID();
    private static final UUID CATERER_ID = UUID.randomUUID();
    private static final UUID CATERER_PARTNER_ID = UUID.randomUUID();
    private static final UUID TEACHER_3A_ID = UUID.randomUUID();
    private static final UUID TEACHER_4A_ID = UUID.randomUUID();
    private static final UUID CLASS_3A_ID = UUID.randomUUID();
    private static final UUID CLASS_4A_ID = UUID.randomUUID();
    private static final UUID BATCH_ID = UUID.randomUUID();
    private static final UUID DISH_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-30T10:00:00Z"), ZoneOffset.UTC);

    private IncidentRepository repository;
    private CorrectiveActionRepository capaRepository;
    private IncidentEventRepository eventRepository;
    private ReferenceResolver referenceResolver;
    private InventoryBatchPort inventoryBatchPort;
    private FoodSamplePort foodSamplePort;
    private ServingDishPort servingDishPort;
    private IncidentServiceImpl service;

    private static AuthUser medicalStaff() {
        return new AuthUser(MEDICAL_ID, "U-0002", "yte@sch.edu.vn", RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
    }

    private static AuthUser schoolAdmin() {
        return new AuthUser(SCHOOL_ADMIN_ID, "U-0001", "bgh@sch.edu.vn", RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);
    }

    private static AuthUser caterer() {
        return new AuthUser(CATERER_ID, "U-0003", "bep@sch.edu.vn", RoleKey.SERVICE_CATERERS,
                SCHOOL_ID, CATERER_PARTNER_ID);
    }

    private static AuthUser teacher3A() {
        return new AuthUser(TEACHER_3A_ID, "U-0004", "gv3a@sch.edu.vn", RoleKey.TEACHER, SCHOOL_ID, null);
    }

    private static AuthUser teacher4A() {
        return new AuthUser(TEACHER_4A_ID, "U-0010", "gv4a@sch.edu.vn", RoleKey.TEACHER, SCHOOL_ID, null);
    }

    private static AuthUser medicalStaffOfAnotherSchool() {
        return new AuthUser(UUID.randomUUID(), "U-0099", "yte2@sch.edu.vn", RoleKey.MEDICAL_STAFF,
                OTHER_SCHOOL_ID, null);
    }

    private static Incident openIncident() {
        Incident i = new Incident();
        i.setCode("INC-2026-014");
        i.setSchoolId(SCHOOL_ID);
        i.setTitle("Sự cố nghi ngộ độc");
        i.setReportedAt(Instant.parse("2026-06-30T04:00:00Z"));
        i.setReportedByLabel("Y tế");
        i.setStatus(IncidentStatus.INVESTIGATING);
        i.setMealDate(LocalDate.parse("2026-06-30"));
        return i;
    }

    /** Sự cố có đủ dữ liệu sức khoẻ + học sinh 2 lớp (3A/3B) để soi projection. */
    private static Incident clinicalIncident() {
        Incident i = openIncident();
        i.setSymptoms(List.of("Đau bụng", "Nôn"));
        i.setFirstAidNotes("Cho uống oresol, theo dõi 2 giờ.");
        i.setClinicalRiskAssessment("Nguy cơ mất nước mức trung bình.");
        i.setInvestigationLabResult("Salmonella spp. dương tính");
        i.setInvestigationLabResultFiles(2);
        i.setRootCause("Thịt heo lô BATCH-MP-8820 nhiễm khuẩn");
        i.getAffectedStudents().add(student("Nguyễn Minh Khang", "3A"));
        i.getAffectedStudents().add(student("Trần Bảo An", "3A"));
        i.getAffectedStudents().add(student("Lê Gia Hân", "3B"));
        i.getAffectedStudents().add(student("Phạm Tuấn Kiệt", "3B"));
        return i;
    }

    private static IncidentAffectedStudent student(String name, String className) {
        IncidentAffectedStudent s = new IncidentAffectedStudent();
        s.setStudentId(UUID.randomUUID());
        s.setStudentName(name);
        s.setClassName(className);
        s.setRecovered(true);
        s.setRecoveredAt(Instant.now(CLOCK));
        return s;
    }

    private static IncidentQuarantineBatch quarantinedBatch(Incident incident) {
        IncidentQuarantineBatch qb = new IncidentQuarantineBatch();
        qb.setIncident(incident);
        qb.setBatchId(BATCH_ID);
        qb.setBatchCode("BATCH-MP-8820");
        qb.setReason("Nghi nhiễm khuẩn");
        qb.setCause("SUPPLIER_LOT_DEFECT");
        qb.setQuarantinedAt(Instant.now(CLOCK));
        qb.setQuarantinedById(MEDICAL_ID);
        return qb;
    }

    private static CorrectiveAction capaAssignedTo(UUID partnerId, CapaStatus status) {
        CorrectiveAction capa = new CorrectiveAction();
        capa.setCode("CAPA-2026-033");
        capa.setAssigneeSupplierId(partnerId);
        capa.setStatus(status);
        return capa;
    }

    @BeforeEach
    void setUp() {
        repository = mock(IncidentRepository.class);
        IncidentRecordDraftRepository recordRepository = mock(IncidentRecordDraftRepository.class);
        capaRepository = mock(CorrectiveActionRepository.class);
        eventRepository = mock(IncidentEventRepository.class);
        referenceResolver = mock(ReferenceResolver.class);
        inventoryBatchPort = mock(InventoryBatchPort.class);
        foodSamplePort = mock(FoodSamplePort.class);
        servingDishPort = mock(ServingDishPort.class);
        NotificationPort notificationPort = mock(NotificationPort.class);
        IncidentEventLog eventLog = new IncidentEventLog(eventRepository, CLOCK);
        Mf11AttachmentSupport attachmentSupport = mock(Mf11AttachmentSupport.class);

        when(referenceResolver.userById(any())).thenReturn(new String[]{"U-0002", "yte@sch.edu.vn", "Y tế trường"});
        when(referenceResolver.classroomIdsOfHomeroomTeacher(TEACHER_3A_ID)).thenReturn(List.of(CLASS_3A_ID));
        when(referenceResolver.classroomIdsOfHomeroomTeacher(TEACHER_4A_ID)).thenReturn(List.of(CLASS_4A_ID));
        when(referenceResolver.classroomCode(CLASS_3A_ID)).thenReturn("3A");
        when(referenceResolver.classroomCode(CLASS_4A_ID)).thenReturn("4A");

        service = new IncidentServiceImpl(repository, recordRepository, capaRepository, eventRepository,
                referenceResolver, inventoryBatchPort, foodSamplePort, servingDishPort, notificationPort, eventLog,
                attachmentSupport, CLOCK);
    }

    private void given(Incident incident) {
        when(repository.findByCodeAndSchoolId(incident.getCode(), SCHOOL_ID)).thenReturn(Optional.of(incident));
    }

    private static List<String> componentNames(Class<?> record) {
        return Arrays.stream(record.getRecordComponents()).map(RecordComponent::getName).toList();
    }

    // ───────────────────────── SEC-006 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-SEC-006 — projection cho Service Caterers cắt sạch dữ liệu sức khoẻ ngay tại server")
    void serviceCaterersProjectionStripsEveryHealthFieldAtTheServerLayer() {
        Incident incident = clinicalIncident();
        given(incident);
        when(capaRepository.findByIncidentId(any()))
                .thenReturn(Optional.of(capaAssignedTo(CATERER_PARTNER_ID, CapaStatus.IN_PROGRESS)));

        Object body = service.detail("INC-2026-014", caterer());

        assertThat(body).isInstanceOf(IncidentCaterersResponse.class);
        IncidentCaterersResponse caterers = (IncidentCaterersResponse) body;

        // Không phải "field bị bỏ trống" — shape trả về KHÔNG HỀ CÓ các field này.
        assertThat(componentNames(IncidentCaterersResponse.class)).doesNotContain(
                "symptoms", "firstAidNotes", "clinicalRiskAssessment", "investigationLabResult",
                "investigationLabResultFiles", "rootCause", "affectedStudents", "severity");

        // Vẫn đủ dữ liệu để đơn vị suất ăn khắc phục: số lượng, món nghi vấn, lô cách ly.
        assertThat(caterers.affectedStudentCount()).isEqualTo(4);
        assertThat(caterers.title()).isEqualTo("Sự cố nghi ngộ độc");
        assertThat(caterers.mealDate()).isEqualTo(LocalDate.parse("2026-06-30"));
        assertThat(caterers.status()).isEqualTo("INVESTIGATING");

        // Quét cả body: không tên học sinh nào lọt ra.
        assertThat(caterers.toString())
                .doesNotContain("Nguyễn Minh Khang", "Trần Bảo An", "Lê Gia Hân", "Phạm Tuấn Kiệt")
                .doesNotContain("Salmonella")
                .doesNotContain("oresol");

        // Đối chứng dương: cùng bản ghi đó, Y tế nhận ĐỦ field — chứng minh việc cắt là theo VAI,
        // không phải vì dữ liệu trống.
        Object medicalBody = service.detail("INC-2026-014", medicalStaff());
        assertThat(medicalBody).isInstanceOfSatisfying(IncidentResponse.class, full -> {
            assertThat(full.symptoms()).containsExactly("Đau bụng", "Nôn");
            assertThat(full.clinicalRiskAssessment()).isNotBlank();
            assertThat(full.investigationLabResult()).contains("Salmonella");
            assertThat(full.affectedStudents()).hasSize(4);
        });
    }

    // ───────────────────────── SEC-007 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-SEC-007 — giáo viên chủ nhiệm chỉ đọc được sự cố có lớp mình, và chỉ thấy HS lớp mình")
    void homeroomTeacherSeesOnlyOwnClassStudentsAndIsDeniedOutsideTheirClass() {
        Incident incident = clinicalIncident();
        given(incident);

        Object body = service.detail("INC-2026-014", teacher3A());
        assertThat(body).isInstanceOf(IncidentTeacherResponse.class);
        IncidentTeacherResponse teacher = (IncidentTeacherResponse) body;

        assertThat(teacher.affectedStudents()).hasSize(2);
        assertThat(teacher.affectedStudents()).allSatisfy(s -> assertThat(s.className()).isEqualTo("3A"));
        assertThat(teacher.toString()).doesNotContain("Lê Gia Hân", "Phạm Tuấn Kiệt");

        // Giáo viên cần triệu chứng để nhận biết dấu hiệu trong lớp, nhưng không được đánh giá
        // lâm sàng / kết quả xét nghiệm / nguyên nhân gốc (ADR-M11-10).
        assertThat(teacher.symptoms()).containsExactly("Đau bụng", "Nôn");
        assertThat(componentNames(IncidentTeacherResponse.class))
                .doesNotContain("clinicalRiskAssessment", "investigationLabResult", "rootCause", "firstAidNotes");

        // Giáo viên 4A — không có HS lớp 4A trong sự cố ⇒ 403, KHÔNG phải 404.
        assertThatThrownBy(() -> service.detail("INC-2026-014", teacher4A()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.ACCESS_DENIED);
                    assertThat(ex.getStatus().value()).isEqualTo(403);
                    assertThat(ex.getMessage()).isEqualTo(IemMessages.MSG_005);
                });
    }

    // ───────────────────────── SEC-010 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-SEC-010 — danh tính/phạm vi luôn lấy từ JWT, mã không tồn tại trả 404 MSG-137")
    void actorIdentityComesFromTheJwtOnlyAndUnknownCodesReturnNotFound() {
        // Không DTO ghi nào của MF11 có chỗ để client tự khai schoolId/actorId/role — muốn giả
        // mạo cũng không có field nào nhận.
        for (Class<?> dto : List.of(AssessRequest.class, CloseRequest.class, QuarantineRequest.class,
                com.sfms.mf11.dto.CapaDtos.CreateCapaRequest.class,
                com.sfms.mf11.dto.ViolationDtos.CreateViolationRequest.class)) {
            assertThat(componentNames(dto))
                    .as("%s không được nhận danh tính từ body", dto.getSimpleName())
                    .doesNotContain("schoolId", "actorId", "role", "userId", "partnerId");
        }

        Incident incident = openIncident();
        given(incident);

        // Actor ghi vào sổ sự kiện là user của JWT, không phải nhãn client gửi lên.
        service.assess("INC-2026-014", new AssessRequest("MEDIUM", "Theo dõi", null), medicalStaff());
        verify(referenceResolver).userById(MEDICAL_ID);

        // Mã KHÔNG tồn tại ⇒ 404 IEM-404 + MSG-137 nguyên văn.
        when(repository.findByCodeAndSchoolId(eq("INC-9999-999"), any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.detail("INC-9999-999", medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.NOT_FOUND);
                    assertThat(ex.getStatus().value()).isEqualTo(404);
                    assertThat(ex.getMessage()).isEqualTo(IemMessages.MSG_137);
                });

        // LỆCH TÀI LIỆU: doc đòi tài nguyên NGOÀI phạm vi trả 403 (không phải 404). Code thật lọc
        // ngay ở repository bằng `findByCodeAndSchoolId(code, schoolId-của-JWT)`, nên sự cố của
        // trường khác không khớp dòng nào ⇒ ném 404 chứ không phải 403. Vẫn không rò rỉ sự tồn
        // tại của hồ sơ sức khoẻ (404 giống hệt mã không tồn tại), chỉ khác mã so với giả định doc.
        assertThatThrownBy(() -> service.detail("INC-2026-014", medicalStaffOfAnotherSchool()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.NOT_FOUND));
    }

    // ───────────────────────── SEC-014 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-SEC-014 — Service Caterers chỉ đọc sự cố mà đơn vị mình thực sự liên đới")
    void serviceCaterersReadOnlyIncidentsTheirUnitIsGenuinelyInvolvedIn() {
        Incident assigned = clinicalIncident();
        given(assigned);
        when(capaRepository.findByIncidentId(any()))
                .thenReturn(Optional.of(capaAssignedTo(CATERER_PARTNER_ID, CapaStatus.IN_PROGRESS)));
        assertThat(service.detail("INC-2026-014", caterer())).isInstanceOf(IncidentCaterersResponse.class);

        // Không CAPA giao cho mình, không lô nào của mình ⇒ 403.
        when(capaRepository.findByIncidentId(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.detail("INC-2026-014", caterer()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.ACCESS_DENIED);
                    assertThat(ex.getStatus().value()).isEqualTo(403);
                });

        // Bảng `incidents` KHÔNG có cột chủ sở hữu — liên đới suy ra từ CAPA/lô, không phải một
        // cột gán cứng (AP-M11-14).
        assertThat(Arrays.stream(Incident.class.getDeclaredFields()).map(Field::getName))
                .doesNotContain("catererId", "ownerId", "assignedTo", "assigneeId");

        // Nhánh 2 (CF-M11-18, doc để ngỏ): chỉ liên đới qua LÔ cách ly do đơn vị mình cung cấp.
        // Code thật giữ cả hai nhánh OR ⇒ CHO đọc; assert theo code thật.
        Incident batchOnly = clinicalIncident();
        batchOnly.getQuarantineBatches().add(quarantinedBatch(batchOnly));
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(batchOnly));
        when(inventoryBatchPort.batchIdsSuppliedBy(CATERER_PARTNER_ID, SCHOOL_ID)).thenReturn(List.of(BATCH_ID));
        assertThat(service.detail("INC-2026-014", caterer())).isInstanceOf(IncidentCaterersResponse.class);
    }

    // ───────────────────────── INT-003 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-INT-003 — cổng đóng sự cố (BR-155/C1) chặn đóng sớm, không đổi trạng thái, mở khi đủ điều kiện")
    void theClosureGateRejectsAPrematureCloseWithoutAnyStateChangeAndPassesOnceReady() {
        Incident incident = clinicalIncident();
        incident.getAffectedStudents().get(0).setRecovered(false);
        given(incident);
        when(capaRepository.findByIncidentId(any()))
                .thenReturn(Optional.of(capaAssignedTo(CATERER_PARTNER_ID, CapaStatus.IN_PROGRESS)));

        assertThatThrownBy(() -> service.close("INC-2026-014", new CloseRequest("Đóng hồ sơ"), schoolAdmin()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.CLOSE_BLOCKED);
                    assertThat(ex.getStatus().value()).isEqualTo(409);
                    assertThat(ex.getMessage()).isEqualTo(IemMessages.MSG_153);
                    // D2 — hai con số động nằm trong details, không nhét vào message.
                    assertThat(ex.getDetails()).hasSize(2);
                });

        // KHÔNG có thay đổi trạng thái nào lọt xuống DB.
        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.INVESTIGATING);
        assertThat(incident.getClosedAt()).isNull();
        assertThat(incident.getClosedById()).isNull();
        assertThat(incident.getClosingStatement()).isNull();
        verify(repository, never()).saveAndFlush(any());
        verify(eventRepository, never()).saveAndFlush(any());

        // Checklist phản ánh đúng lý do đang bị chặn.
        var readiness = service.closureReadiness("INC-2026-014", schoolAdmin());
        assertThat(readiness.ready()).isFalse();
        assertThat(readiness.capaOpen()).isEqualTo(1);
        assertThat(readiness.studentsNotRecovered()).isEqualTo(1);

        // Đủ điều kiện ⇒ đóng được.
        incident.getAffectedStudents().forEach(s -> s.setRecovered(true));
        when(capaRepository.findByIncidentId(any()))
                .thenReturn(Optional.of(capaAssignedTo(CATERER_PARTNER_ID, CapaStatus.COMPLETED)));
        service.close("INC-2026-014", new CloseRequest("Đã xử lý xong."), schoolAdmin());

        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.CLOSED);
        assertThat(incident.getClosedAt()).isEqualTo(Instant.now(CLOCK));
        assertThat(incident.getClosedById()).isEqualTo(SCHOOL_ADMIN_ID);
        verify(repository).saveAndFlush(incident);
    }

    // ───────────────────────── INT-006 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-INT-006 — cách ly đi qua InventoryBatchPort, actor ghi nhận là user thật của JWT")
    void quarantineGoesThroughTheReadOnlyBoundaryPortAndRecordsTheRealActor() {
        Incident incident = openIncident();
        given(incident);
        when(inventoryBatchPort.findByCode("BATCH-MP-8820", SCHOOL_ID))
                .thenReturn(Optional.of(new BatchBrief(BATCH_ID, "BATCH-MP-8820", "Thịt heo", "kg",
                        null, UUID.randomUUID(), "NCC A")));
        when(inventoryBatchPort.isAlreadyQuarantined(BATCH_ID)).thenReturn(false);

        service.quarantineBatches("INC-2026-014",
                new QuarantineRequest(List.of("BATCH-MP-8820"), "Nghi nhiễm", "SUPPLIER_LOT_DEFECT"), medicalStaff());

        // Đường DUY NHẤT chạm tồn kho MF06 là port — và nó nhận userId thật, không phải nhãn.
        verify(inventoryBatchPort).markQuarantined(BATCH_ID, "INC-2026-014", "Nghi nhiễm",
                "SUPPLIER_LOT_DEFECT", MEDICAL_ID);
        assertThat(incident.getQuarantineBatches()).hasSize(1);
        assertThat(incident.getQuarantineBatches().get(0).getQuarantinedById()).isEqualTo(MEDICAL_ID);

        // ADR-M11-7 — service không giữ tham chiếu nào tới tầng dữ liệu của MF06, nên không thể
        // phát sinh UPDATE thẳng lên bảng tồn kho.
        for (Field f : IncidentServiceImpl.class.getDeclaredFields()) {
            String pkg = f.getType().getPackageName();
            assertThat(pkg)
                    .as("IncidentServiceImpl.%s thuộc %s — MF11 không được import tầng dữ liệu module khác",
                            f.getName(), pkg)
                    .doesNotStartWith("com.sfms.mf06");
        }
    }

    // ───────────────────────── INT-007 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-INT-007 — bản đồ truy xuất từ COMPILING lên READY, chỉ theo lô đã cách ly")
    void theTraceabilityMapCompilesToReadyScopedOnlyToQuarantinedBatches() {
        // Mặc định của một sự cố mới là COMPILING (BR-151).
        Incident noBatch = openIncident();
        assertThat(noBatch.getTraceabilityMapStatus()).isEqualTo(TraceMapStatus.COMPILING);
        given(noBatch);

        // Không lô nào bị cách ly ⇒ KHÔNG có nhánh "phạm vi cả bữa ăn": map ở nguyên COMPILING.
        service.finalizeEvidence("INC-2026-014", medicalStaff());
        assertThat(noBatch.getTraceabilityMapStatus()).isEqualTo(TraceMapStatus.COMPILING);
        assertThat(service.traceabilityMap("INC-2026-014", medicalStaff()).nodes()).isEmpty();

        // Có lô cách ly ⇒ chốt bằng chứng đưa map lên READY và ghi mốc TRACE_READY.
        Incident withBatch = openIncident();
        withBatch.getQuarantineBatches().add(quarantinedBatch(withBatch));
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(withBatch));

        service.finalizeEvidence("INC-2026-014", medicalStaff());

        assertThat(withBatch.getTraceabilityMapStatus()).isEqualTo(TraceMapStatus.READY);
        ArgumentCaptor<IncidentEvent> event = ArgumentCaptor.forClass(IncidentEvent.class);
        verify(eventRepository, org.mockito.Mockito.atLeastOnce()).saveAndFlush(event.capture());
        assertThat(event.getAllValues()).anySatisfy(
                e -> assertThat(e.getType()).isEqualTo(IncidentEventType.TRACE_READY));

        // Phạm vi: đúng 1 node cho đúng 1 lô đã cách ly, không phải mọi nguyên liệu của bữa ăn.
        var map = service.traceabilityMap("INC-2026-014", medicalStaff());
        assertThat(map.status()).isEqualTo("READY");
        assertThat(map.nodes()).hasSize(1);
        assertThat(map.nodes().get(0).batchCode()).isEqualTo("BATCH-MP-8820");

        // LỆCH TÀI LIỆU: doc mô tả SLA 4 giờ tự biên dịch. Code thật không có bộ hẹn giờ —
        // chuyển COMPILING→READY xảy ra đồng bộ khi Y tế chốt bằng chứng, nhanh hơn SLA.
    }

    // ───────────────────────── INT-010 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-INT-010 — `id` trong JSON luôn là mã nghiệp vụ, kể cả trong mảng lồng")
    void jsonIdIsAlwaysTheBusinessCodeNeverTheInternalUuid() {
        Incident incident = clinicalIncident();
        IncidentSuspectedDish dish = new IncidentSuspectedDish();
        dish.setIncident(incident);
        dish.setDishId(DISH_ID);
        dish.setDishName("Thịt kho trứng");
        incident.getSuspectedDishes().add(dish);
        incident.getQuarantineBatches().add(quarantinedBatch(incident));
        given(incident);
        when(referenceResolver.dishCode(DISH_ID)).thenReturn("DISH-0042");
        when(referenceResolver.studentCode(any())).thenReturn("HS-3A-001");
        when(eventRepository.findByIncidentIdOrderByAtAsc(any())).thenReturn(List.of(
                new IncidentEvent(null, "IEV-2026-0007", Instant.now(CLOCK),
                        IncidentEventType.QUARANTINE, "Cách ly lô BATCH-MP-8820", "Y tế trường")));

        IncidentResponse body = (IncidentResponse) service.detail("INC-2026-014", medicalStaff());

        assertThat(body.id()).isEqualTo("INC-2026-014");
        assertThat(isUuid(body.id())).isFalse();
        assertThat(body.suspectedDishes().get(0).dishId()).isEqualTo("DISH-0042");
        assertThat(body.quarantineBatches().get(0).batchId()).isEqualTo("BATCH-MP-8820");
        assertThat(body.affectedStudents()).allSatisfy(s -> assertThat(isUuid(s.studentId())).isFalse());

        List<IncidentEventResponse> events = service.events("INC-2026-014", medicalStaff());
        assertThat(events).singleElement().satisfies(e -> {
            assertThat(e.id()).isEqualTo("IEV-2026-0007");
            assertThat(isUuid(e.id())).isFalse();
        });
    }

    // ───────────────────────── INT-020 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-INT-020 — hợp đồng danh sách/phân trang/lọc/khoảng ngày dùng chung cho 5 resource của MF11")
    void theSharedListPaginationFilterAndDateRangeContractHolds() {
        // §2b-2 — key điều khiển không được rơi xuống thành bộ lọc field.
        var query = com.sfms.common.query.ListQuery.from(java.util.Map.of(
                "page", "2", "pageSize", "5", "search", "ngộ độc",
                "dateFrom", "2026-06-01", "dateTo", "2026-06-30",
                "status", "INVESTIGATING", "blank", ""));
        assertThat(query.page()).isEqualTo(2);
        assertThat(query.pageSize()).isEqualTo(5);
        assertThat(query.search()).isEqualTo("ngộ độc");
        assertThat(query.dateFrom()).isEqualTo(LocalDate.parse("2026-06-01"));
        assertThat(query.dateTo()).isEqualTo(LocalDate.parse("2026-06-30"));
        assertThat(query.filters()).containsExactly(java.util.Map.entry("status", "INVESTIGATING"));

        Incident incident = clinicalIncident();
        // total PHẢI nhất quán với offset+content: PageImpl tự tính lại total khi
        // offset+pageSize > total truyền vào (offset=1*5=5) — 1 phần tử ở trang cuối ⇒ total=6.
        var page = new org.springframework.data.domain.PageImpl<>(List.of(incident),
                org.springframework.data.domain.PageRequest.of(1, 5), 6);
        when(repository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        var body = service.list(query, medicalStaff());

        // §2b-1 — đúng 5 khoá, không wrapper data/result.
        assertThat(componentNames(com.sfms.common.dto.PagedResponse.class))
                .containsExactly("items", "total", "page", "pageSize", "pageCount");
        assertThat(body.items()).hasSize(1);
        assertThat(body.total()).isEqualTo(6);
        assertThat(body.page()).isEqualTo(2);
        assertThat(body.pageSize()).isEqualTo(5);
        assertThat(body.pageCount()).isEqualTo(2);

        // §2b-7 — Spring Data 0-indexed ⇒ page-1; và danh sách THỜI GIAN phải sắp mới nhất trước.
        ArgumentCaptor<org.springframework.data.domain.Pageable> pageable =
                ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(repository).findAll(any(org.springframework.data.jpa.domain.Specification.class),
                pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
        assertThat(pageable.getValue().getSort().isSorted()).isTrue();
        assertThat(pageable.getValue().getSort().toString()).contains("reportedAt: DESC");

        // Cả 5 resource dùng cùng một khuôn Specification dựng từ ListQuery.
        for (Class<?> spec : List.of(IncidentSpec.class, IncidentRecordSpec.class, SourceReportSpec.class,
                CapaSpec.class, ViolationSpec.class)) {
            assertThat(Arrays.stream(spec.getDeclaredMethods()).map(java.lang.reflect.Method::getName))
                    .as("%s phải có hàm build() nhận ListQuery", spec.getSimpleName())
                    .contains("build");
        }
    }

    private static boolean isUuid(String raw) {
        if (raw == null) {
            return false;
        }
        try {
            UUID.fromString(raw);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // ───────────────────────── INT-018 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-INT-018 — mẫu lưu/phiếu lab đọc qua FoodSamplePort và cửa sổ điều tra bám ngày bữa ăn")
    void samplesAreReadThroughFoodSamplePortWithinTheMealDateWindow() {
        Incident incident = openIncident();
        given(incident);
        when(foodSamplePort.findByIncident(any())).thenReturn(List.of(
                new FoodSamplePort.SampleBrief("SMP-2026-101", "Thịt kho trứng",
                        Instant.parse("2026-06-30T04:30:00Z"), "HOLD", null)));
        when(foodSamplePort.labRequestsByIncident(any())).thenReturn(List.of(
                new FoodSamplePort.LabRequestBrief("LAB-2026-007", "CDC Hà Nội", "Salmonella",
                        "SENT", Instant.parse("2026-06-30T06:00:00Z"))));

        assertThat(service.samples("INC-2026-014", medicalStaff()))
                .singleElement().extracting(FoodSamplePort.SampleBrief::sampleCode).isEqualTo("SMP-2026-101");
        assertThat(service.labRequests("INC-2026-014", medicalStaff()))
                .singleElement().extracting(FoodSamplePort.LabRequestBrief::code).isEqualTo("LAB-2026-007");

        verify(foodSamplePort).findByIncident(any());
        verify(foodSamplePort).labRequestsByIncident(any());

        // Không có repository nào của MF08 trong service ⇒ không thể JOIN thẳng sang bảng mẫu lưu.
        for (Field f : IncidentServiceImpl.class.getDeclaredFields()) {
            assertThat(f.getType().getPackageName())
                    .as("IncidentServiceImpl.%s chạm thẳng tầng dữ liệu MF08", f.getName())
                    .doesNotStartWith("com.sfms.mf08");
        }

        // Cửa sổ tương quan (BR-149, 24 giờ) neo vào NGÀY BỮA ĂN của chính sự cố — cùng mốc mà
        // mẫu lưu phải rơi vào để coi là liên quan.
        service.suspectedDishCandidates("INC-2026-014", medicalStaff());
        ArgumentCaptor<Instant> from = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> to = ArgumentCaptor.forClass(Instant.class);
        verify(servingDishPort).dishesServedBetween(eq(SCHOOL_ID), from.capture(), to.capture());
        assertThat(to.getValue()).isEqualTo(Instant.parse("2026-06-30T00:00:00Z"));
        assertThat(from.getValue()).isEqualTo(Instant.parse("2026-06-29T00:00:00Z"));
    }
}
