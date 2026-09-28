package com.sfms.mf11.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.storage.StorageService;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf11.domain.ResolutionStatus;
import com.sfms.mf11.domain.ViolationFile;
import com.sfms.mf11.domain.ViolationFileKind;
import com.sfms.mf11.domain.ViolationRecord;
import com.sfms.mf11.domain.ViolationSeverity;
import com.sfms.mf11.domain.ViolationStatus;
import com.sfms.mf11.dto.ViolationDtos.ResolutionRequest;
import com.sfms.mf11.error.IemError;
import com.sfms.mf11.error.IemMessages;
import com.sfms.mf11.repository.IncidentRepository;
import com.sfms.mf11.repository.ViolationRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MF11-TC-024/025 — {@code ViolationServiceImpl}: gate ảnh biên bản ký (BR-211) + phân nhánh
 * duyệt theo severity (BR-186).
 *
 * <p>Không có {@code ViolationResolutionService}/{@code ViolationPdfService} riêng — cả hai nằm
 * trong {@code ViolationServiceImpl} ({@code exportPdf()}, {@code uploadSignedRecord()},
 * {@code submitResolution()}, {@code approveResolution()}).
 *
 * <p><b>GAP thật</b>: {@code approveResolution()}/{@code rejectResolution()} KHÔNG tự kiểm tra
 * {@code actor.role()} — {@code IemExceptions.violationApprovalForbidden()} (IEM-186/MSG-157)
 * được khai báo nhưng KHÔNG có lời gọi nào trong toàn bộ {@code ViolationServiceImpl} (grep xác
 * nhận, xem đợt 1). Chặn "chỉ School Admin được approve" nằm ở
 * {@code @PreAuthorize("hasRole('SCHOOL_ADMIN')")} trên {@code ViolationController}, tức Spring
 * Security AOP — KHÔNG unit-test được ở tầng service (gọi thẳng {@code service.approveResolution()}
 * trong test Mockito sẽ LUÔN chạy, không có gate). TC "Medical Staff cố approve → 403 IEM-186"
 * do đó chuyển sang test ở tầng Integration/Security (MockMvc, có filter chain) — xem báo cáo cuối.
 */
class ViolationResolutionTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID MEDICAL_ID = UUID.randomUUID();
    private static final UUID SCHOOL_ADMIN_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-30T10:00:00Z"), ZoneOffset.UTC);

    private ViolationRecordRepository repository;
    private ViolationServiceImpl service;

    private static AuthUser medicalStaff() {
        return new AuthUser(MEDICAL_ID, "U-0002", "yte@sch.edu.vn", RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
    }

    private static AuthUser schoolAdmin() {
        return new AuthUser(SCHOOL_ADMIN_ID, "U-0001", "bgh@sch.edu.vn", RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);
    }

    private static ViolationRecord violation(ViolationSeverity severity) {
        ViolationRecord v = new ViolationRecord();
        v.setCode("VIO-2026-008");
        v.setSchoolId(SCHOOL_ID);
        v.setSubject("Vi phạm vệ sinh");
        v.setSeverity(severity);
        v.setResolution(ResolutionStatus.PENDING);
        v.setStatus(ViolationStatus.OPEN);
        return v;
    }

    @BeforeEach
    void setUp() {
        repository = mock(ViolationRecordRepository.class);
        IncidentRepository incidentRepository = mock(IncidentRepository.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        CodeGenerator codeGenerator = mock(CodeGenerator.class);
        StorageService storageService = mock(StorageService.class);

        when(codeGenerator.next(anyString(), anyString(), anyInt())).thenReturn("VF-2026-001");
        when(referenceResolver.userById(any())).thenReturn(new String[]{"U-0002", "yte@sch.edu.vn", "Y tế"});

        service = new ViolationServiceImpl(repository, incidentRepository, referenceResolver, codeGenerator,
                storageService, CLOCK);
    }

    // ───────────────────────── TC-024 — gate ảnh biên bản ký ─────────────────────────

    @Test
    void uploadingASignedRecordBeforeThePdfIsExportedIsRejectedWith409IemPdfNotExported() {
        ViolationRecord v = violation(ViolationSeverity.MEDIUM);
        when(repository.findByCodeAndSchoolId("VIO-2026-008", SCHOOL_ID)).thenReturn(Optional.of(v));
        MockMultipartFile file = new MockMultipartFile("file", "signed.jpg", "image/jpeg", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> service.uploadSignedRecord("VIO-2026-008", file, medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.PDF_NOT_EXPORTED);
                    assertThat(ex.getStatus().value()).isEqualTo(409);
                    assertThat(ex.getMessage()).isEqualTo(IemMessages.MSG_156);
                });
    }

    @Test
    void submittingResolutionWithoutASignedRecordFileIsRejectedWith409IemSignedRecordMissing() {
        ViolationRecord v = violation(ViolationSeverity.MEDIUM);
        v.setPdfExported(true);
        when(repository.findByCodeAndSchoolId("VIO-2026-008", SCHOOL_ID)).thenReturn(Optional.of(v));

        assertThatThrownBy(() -> service.submitResolution("VIO-2026-008",
                new ResolutionRequest("Nhắc nhở bằng văn bản", "WRITTEN_REMINDER"), medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.SIGNED_RECORD_MISSING);
                    assertThat(ex.getStatus().value()).isEqualTo(409);
                });
        assertThat(v.getResolution()).isEqualTo(ResolutionStatus.PENDING);
    }

    /** BR-211 đoạn 3 — gate KHÔNG chặn việc lưu/sửa nội dung biên bản, chỉ chặn resolution. */
    @Test
    void exportingThePdfDoesNotRequireASignedRecordAndIsAlwaysAllowed() {
        ViolationRecord v = violation(ViolationSeverity.MEDIUM);
        when(repository.findByCodeAndSchoolId("VIO-2026-008", SCHOOL_ID)).thenReturn(Optional.of(v));

        service.exportPdf("VIO-2026-008", medicalStaff());

        assertThat(v.isPdfExported()).isTrue();
        assertThat(v.getPdfVersion()).isEqualTo(1);
    }

    @Test
    void submittingResolutionWithASignedRecordFilePresentSucceeds() {
        ViolationRecord v = violation(ViolationSeverity.MEDIUM);
        v.setPdfExported(true);
        ViolationFile signed = new ViolationFile();
        signed.setKind(ViolationFileKind.SIGNED_RECORD);
        v.getFiles().add(signed);
        when(repository.findByCodeAndSchoolId("VIO-2026-008", SCHOOL_ID)).thenReturn(Optional.of(v));

        var response = service.submitResolution("VIO-2026-008",
                new ResolutionRequest("Nhắc nhở bằng văn bản", "WRITTEN_REMINDER"), medicalStaff());

        assertThat(response.resolution()).isEqualTo("PENDING_APPROVAL");
    }

    // ───────────────────────── TC-025 — LOW tự đóng, MEDIUM/HIGH cần School Admin ─────────────────────────

    @Test
    void lowSeverityIsAutoApprovedAndClosedBySubmitResolutionAlone() {
        ViolationRecord v = violation(ViolationSeverity.LOW);
        v.setPdfExported(true);
        ViolationFile signed = new ViolationFile();
        signed.setKind(ViolationFileKind.SIGNED_RECORD);
        v.getFiles().add(signed);
        when(repository.findByCodeAndSchoolId("VIO-2026-008", SCHOOL_ID)).thenReturn(Optional.of(v));

        var response = service.submitResolution("VIO-2026-008", new ResolutionRequest("Nhắc nhở miệng", null),
                medicalStaff());

        assertThat(response.resolution()).isEqualTo("APPROVED");
        assertThat(response.status()).isEqualTo("CLOSED");
    }

    @Test
    void mediumSeverityWithoutUnitSanctionLevelIsRejectedWith400() {
        ViolationRecord v = violation(ViolationSeverity.MEDIUM);
        v.setPdfExported(true);
        ViolationFile signed = new ViolationFile();
        signed.setKind(ViolationFileKind.SIGNED_RECORD);
        v.getFiles().add(signed);
        when(repository.findByCodeAndSchoolId("VIO-2026-008", SCHOOL_ID)).thenReturn(Optional.of(v));

        assertThatThrownBy(() -> service.submitResolution("VIO-2026-008",
                new ResolutionRequest("Nhắc nhở bằng văn bản", null), medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED));
    }

    @Test
    void mediumSeverityStaysPendingApprovalUntilSchoolAdminApproves() {
        ViolationRecord v = violation(ViolationSeverity.MEDIUM);
        v.setResolution(ResolutionStatus.PENDING_APPROVAL);
        when(repository.findByCodeAndSchoolId("VIO-2026-008", SCHOOL_ID)).thenReturn(Optional.of(v));

        var response = service.approveResolution("VIO-2026-008", schoolAdmin());

        assertThat(response.resolution()).isEqualTo("APPROVED");
        assertThat(response.status()).isEqualTo("CLOSED");
    }

    @Test
    void approvingAViolationThatIsNotPendingApprovalIsRejected() {
        ViolationRecord v = violation(ViolationSeverity.MEDIUM);
        v.setResolution(ResolutionStatus.PENDING);
        when(repository.findByCodeAndSchoolId("VIO-2026-008", SCHOOL_ID)).thenReturn(Optional.of(v));

        assertThatThrownBy(() -> service.approveResolution("VIO-2026-008", schoolAdmin()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED));
    }

    @Test
    void unitSanctionLevelOnlyAcceptsTheFourBr185ValuesNotTheOldDisciplinaryLevelValues() {
        ViolationRecord v = violation(ViolationSeverity.MEDIUM);
        v.setPdfExported(true);
        ViolationFile signed = new ViolationFile();
        signed.setKind(ViolationFileKind.SIGNED_RECORD);
        v.getFiles().add(signed);
        when(repository.findByCodeAndSchoolId("VIO-2026-008", SCHOOL_ID)).thenReturn(Optional.of(v));

        assertThatThrownBy(() -> service.submitResolution("VIO-2026-008",
                new ResolutionRequest("Đình chỉ", "SUSPENSION"), medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED));
    }
}
