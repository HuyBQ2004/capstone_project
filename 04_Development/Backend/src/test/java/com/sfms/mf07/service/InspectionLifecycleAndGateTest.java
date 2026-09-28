package com.sfms.mf07.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf07.domain.InspectionSession;
import com.sfms.mf07.domain.Step1FreshRow;
import com.sfms.mf07.domain.StepStatus;
import com.sfms.mf07.dto.InspectionDtos.InspectionSessionResponse;
import com.sfms.mf07.dto.InspectionDtos.MedicalReviewRequest;
import com.sfms.mf07.dto.InspectionDtos.ServingGateResponse;
import com.sfms.mf07.dto.InspectionDtos.SubmitStepRequest;
import com.sfms.mf07.repository.InspectionSessionRepository;
import com.sfms.mf10.port.NotificationPort;
import com.sfms.mf11.port.CapaPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF07 — các TC của tài liệu IT (`05_Testing/02_IT/MF07_Food_Safety_Inspection/`) mà trước đây
 * chỉ có trên giấy, nay hiện thực bằng Mockito trên chính {@link InspectionServiceImpl}
 * (không cần Docker/Testcontainers ⇒ chạy được ở mọi máy).
 *
 * <p><b>Nguyên tắc:</b> mã nguồn THẬT là oracle. Chỗ nào tài liệu mô tả khác hành vi thật thì
 * assert hành vi THẬT và ghi chú một dòng ngay tại chỗ (không sửa {@code src/main}).
 */
class InspectionLifecycleAndGateTest {

    private static final UUID SCHOOL_ID = UUID.fromString("00000000-0000-0000-0000-0000000a0042");
    private static final UUID CATERER_A = UUID.fromString("00000000-0000-0000-0000-0000000b0006");
    private static final UUID CATERER_B = UUID.fromString("00000000-0000-0000-0000-0000000b0001");
    private static final UUID ACTOR_ID = UUID.fromString("00000000-0000-0000-0000-0000000c0022");
    private static final LocalDate DAY = LocalDate.of(2026, 7, 30);

    private InspectionSessionRepository sessionRepository;
    private ReferenceResolver referenceResolver;
    private CapaPort capaPort;
    private NotificationPort notificationPort;
    private InspectionServiceImpl service;

    private final AuthUser catererA =
            new AuthUser(ACTOR_ID, "U-0022", "caterer@sfms.vn", RoleKey.SERVICE_CATERERS, SCHOOL_ID, null);
    private final AuthUser medical =
            new AuthUser(UUID.randomUUID(), "U-0002", "medical@sfms.vn", RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
    private final AuthUser schoolAdmin =
            new AuthUser(UUID.randomUUID(), "U-0001", "admin@sfms.vn", RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);

    @BeforeEach
    void setUp() {
        sessionRepository = mock(InspectionSessionRepository.class);
        referenceResolver = mock(ReferenceResolver.class);
        capaPort = mock(CapaPort.class);
        notificationPort = mock(NotificationPort.class);
        CodeGenerator codeGenerator = mock(CodeGenerator.class);
        Clock clock = Clock.fixed(Instant.parse("2026-07-30T02:00:00Z"), ZoneOffset.UTC);

        // Assembler THẬT (không mock) — các TC về hợp đồng D1/`id` phải đi qua mapper thật.
        InspectionAssembler assembler = new InspectionAssembler(referenceResolver);
        service = new InspectionServiceImpl(sessionRepository, assembler, referenceResolver, codeGenerator,
                capaPort, notificationPort, clock);

        when(referenceResolver.approvedCatererPartnerId(SCHOOL_ID)).thenReturn(CATERER_A);
        when(sessionRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        when(capaPort.openFromFailedStep(any(), anyString(), any(), any(), any())).thenReturn("CAPA-2026-001");
    }

    // ═════════════════════════ Serving gate (BR-094) ═════════════════════════

    @Test
    @DisplayName("MF07-TC-INT-002 — serving gate mở (allowed=true) khi phiên ĐẠT cả 3 bước và đã đồng ký")
    void servingGateOpensForPassedAndCoSignedSession() {
        InspectionSession passed = session("INSP-20260730-L", CATERER_A);
        passed.setStep1(StepStatus.PASSED);
        passed.setStep2(StepStatus.PASSED);
        passed.setStep3(StepStatus.PASSED);
        passed.setOverall(StepStatus.PASSED);
        passed.setCoSignedAt(Instant.parse("2026-07-30T03:00:00Z"));
        when(sessionRepository.findFirstBySchoolIdAndDateAndMealTypeAndPartnerIdOrderByCreatedAtDesc(
                SCHOOL_ID, DAY, MealType.LUNCH, CATERER_A)).thenReturn(Optional.of(passed));

        ServingGateResponse gate = service.checkGate(DAY, "LUNCH", catererA);

        assertThat(gate.allowed()).isTrue();
        assertThat(gate.reasonCode()).isNull();
        // MF09 ghi lại được ĐÃ hỏi cổng nào — trả `sessionId` = mã nghiệp vụ (D1), không phải UUID.
        assertThat(gate.sessionId()).isEqualTo("INSP-20260730-L");
    }

    @Test
    @DisplayName("MF07-TC-INT-003 — serving gate chặn ở 4 trạng thái DB, mỗi trạng thái một reasonCode, không bao giờ ném lỗi")
    void servingGateBlocksWithDistinguishableReasonCodes() {
        // (a) overall = FAILED
        InspectionSession failed = session("INSP-20260730-S", CATERER_A);
        failed.setStep1(StepStatus.FAILED);
        failed.setOverall(StepStatus.FAILED);
        when(sessionRepository.findFirstBySchoolIdAndDateAndMealTypeAndPartnerIdOrderByCreatedAtDesc(
                SCHOOL_ID, DAY, MealType.SNACK, CATERER_A)).thenReturn(Optional.of(failed));
        ServingGateResponse a = service.checkGate(DAY, "SNACK", catererA);
        assertThat(a.allowed()).isFalse();
        assertThat(a.reasonCode()).isEqualTo("INSPECTION_FAILED");

        // (b) overall = IN_PROGRESS.
        // ⚠️ Lệch tài liệu: tài liệu ghi reasonCode "INSPECTION_INCOMPLETE"; code THẬT
        // (InspectionServiceImpl#checkGate) trả "IN_PROGRESS" — assert theo code thật.
        InspectionSession inProgress = session("INSP-20260730-L", CATERER_A);
        inProgress.setOverall(StepStatus.IN_PROGRESS);
        when(sessionRepository.findFirstBySchoolIdAndDateAndMealTypeAndPartnerIdOrderByCreatedAtDesc(
                SCHOOL_ID, DAY, MealType.LUNCH, CATERER_A)).thenReturn(Optional.of(inProgress));
        ServingGateResponse b = service.checkGate(DAY, "LUNCH", catererA);
        assertThat(b.allowed()).isFalse();
        assertThat(b.reasonCode()).isEqualTo("IN_PROGRESS");
        assertThat(b.sessionId()).isEqualTo("INSP-20260730-L");

        // (c) không có phiên nào cho (ngày, buổi)
        when(sessionRepository.findFirstBySchoolIdAndDateAndMealTypeAndPartnerIdOrderByCreatedAtDesc(
                SCHOOL_ID, DAY, MealType.BREAKFAST, CATERER_A)).thenReturn(Optional.empty());
        ServingGateResponse c = service.checkGate(DAY, "BREAKFAST", catererA);
        assertThat(c.allowed()).isFalse();
        assertThat(c.reasonCode()).isEqualTo("NO_SESSION");
        assertThat(c.sessionId()).isNull();

        // (d) cả 3 bước PASSED nhưng CHƯA đồng ký.
        // ⚠️ Lệch tài liệu: tài liệu chờ reasonCode "NOT_CO_SIGNED"; code THẬT chỉ đọc cột
        // `overall` (ADR-M07-02) nên cổng ĐÃ MỞ trước khi đồng ký. Assert hành vi thật.
        InspectionSession notCoSigned = session("INSP-20260730-L2", CATERER_A);
        notCoSigned.setStep1(StepStatus.PASSED);
        notCoSigned.setStep2(StepStatus.PASSED);
        notCoSigned.setStep3(StepStatus.PASSED);
        notCoSigned.setOverall(StepStatus.PASSED);
        notCoSigned.setCoSignedAt(null);
        when(sessionRepository.findFirstBySchoolIdAndDateAndMealTypeAndPartnerIdOrderByCreatedAtDesc(
                SCHOOL_ID, DAY, MealType.LUNCH, CATERER_A)).thenReturn(Optional.of(notCoSigned));
        ServingGateResponse d = service.checkGate(DAY, "LUNCH", catererA);
        assertThat(d.allowed()).isTrue();
        assertThat(d.reasonCode()).isNull();

        // Cổng là một CÂU HỎI, không phải lỗi — 4 nhánh trên đều trả object, không ném exception
        // (⇒ controller luôn 200, không bao giờ 4xx/5xx cho các trạng thái nghiệp vụ này).
        assertThat(List.of(a, b, c, d)).allSatisfy(r -> assertThat(r).isNotNull());
        // Điểm vệ sinh bếp thấp KHÔNG bao giờ là một reasonCode của cổng (C6).
        assertThat(List.of(a, b, c).stream().map(ServingGateResponse::reasonCode).toList())
                .doesNotContain("KITCHEN_SCORE_LOW", "HYGIENE_FAILED");
    }

    // ═════════════════════════ Danh sách & phạm vi dòng (BR-099) ═════════════════════════

    @Test
    @DisplayName("MF07-TC-INT-004 — GET /inspections thu hẹp theo đơn vị của chính Caterer, Medical/School Admin thấy cả trường")
    @SuppressWarnings("unchecked")
    void listScopesRowsByPartnerOnlyForServiceCaterers() {
        Page<InspectionSession> empty = new PageImpl<>(List.of());
        when(sessionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(empty);
        ListQuery query = ListQuery.from(Map.of("page", "1", "pageSize", "100"));

        service.list(query, catererA);
        // BR-099 — vai trò Caterer PHẢI tra ra partnerId của chính mình để đưa vào Specification
        // (lọc ở tầng repository/Specification, không lọc sau khi nạp).
        verify(referenceResolver, times(1)).approvedCatererPartnerId(SCHOOL_ID);

        service.list(query, medical);
        service.list(query, schoolAdmin);
        // Medical/School Admin thấy TOÀN BỘ trường ⇒ không hề tra partnerId thêm lần nào nữa.
        verify(referenceResolver, times(1)).approvedCatererPartnerId(SCHOOL_ID);

        PagedResponse<InspectionSessionResponse> page = service.list(query, medical);
        assertThat(page.items()).isEmpty();
        assertThat(page.total()).isZero();
    }

    @Test
    @DisplayName("MF07-TC-SEC-005 — Caterer A không đọc/không ghi được phiên của Caterer B (cách ly dòng, BR-099)")
    void catererCannotReachAnotherPartnersSession() {
        InspectionSession foreign = session("INSP-20260730-B", CATERER_B);
        when(sessionRepository.findBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-B"))
                .thenReturn(Optional.of(foreign));
        when(sessionRepository.findForUpdateBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-B"))
                .thenReturn(Optional.of(foreign));

        // Đọc — 404 (KHÔNG 403): code thật chọn "không lộ sự tồn tại", nhất quán ở mọi đường vào.
        assertThatThrownBy(() -> service.detail("INSP-20260730-B", catererA))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("INSP-404");

        // Ghi — cùng một hàng rào loadInScope() ⇒ cũng 404, DB không đổi.
        assertThatThrownBy(() -> service.submitStep("INSP-20260730-B", 1, step("PASSED", null), catererA))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("INSP-404");
        verify(sessionRepository, never()).saveAndFlush(any());

        // Medical Staff thấy cả trường (BR-099) — chính phiên đó đọc được bình thường.
        assertThat(service.detail("INSP-20260730-B", medical).id()).isEqualTo("INSP-20260730-B");
    }

    // ═════════════════════════ Ghi bước, CAPA, bất biến `overall` ═════════════════════════

    @Test
    @DisplayName("MF07-TC-INT-008 — Bước 2 KHÔNG ĐẠT tự sinh CAPA trong cùng transaction, không cần client gọi API nào")
    void failedStepTwoAutomaticallyOpensCapaInTheSameTransaction() {
        InspectionSession session = session("INSP-20260730-L", CATERER_A);
        session.setStep1(StepStatus.PASSED);
        when(sessionRepository.findBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L"))
                .thenReturn(Optional.of(session));
        when(sessionRepository.findForUpdateBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L"))
                .thenReturn(Optional.of(session));

        // needsNewIngredients=true — nhiệt độ lõi thấp thật sự đòi nấu lại, khớp bất biến cũ
        // (overall=FAILED) mà test này đang kiểm.
        SubmitStepRequest failed = new SubmitStepRequest(null, null, null, null, "FAILED",
                new BigDecimal("68"), null, "Nhiệt độ lõi chỉ 68°C", "HIGH", "Ngô Thị Hà", true, null, null, null);
        InspectionSessionResponse response = service.submitStep("INSP-20260730-L", 2, failed, catererA);

        assertThat(response.step2()).isEqualTo("FAILED");
        assertThat(response.overall()).isEqualTo("FAILED");
        // CAPA sinh ĐÚNG 1 lần, do chính service gọi CapaPort (M11-D08) — client không gọi gì.
        verify(capaPort, times(1)).openFromFailedStep(any(), eq("Bước 2"), eq("Nhiệt độ lõi chỉ 68°C"),
                eq(SCHOOL_ID), eq(ACTOR_ID));
        // Cùng transaction: bước trượt được ghi và CAPA mở ra trong CÙNG lời gọi service.
        assertThat(session.getStep2()).isEqualTo(StepStatus.FAILED);
        assertThat(session.getFailStep()).isEqualTo(2);
        assertThat(session.getFailSeverity().name()).isEqualTo("HIGH");
        assertThat(session.getDecidedAt()).isNotNull();
        verify(sessionRepository, times(1)).saveAndFlush(session);
        verify(notificationPort, times(1)).publish(any());
    }

    @Test
    @DisplayName("MF07-TC-INT-019 — bất biến giữa cột dẫn xuất `overall` và 3 cột bước / decided_at")
    void overallStaysConsistentWithThePerStepColumns() {
        InspectionSession session = session("INSP-20260730-L", CATERER_A);
        when(sessionRepository.findBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L"))
                .thenReturn(Optional.of(session));
        when(sessionRepository.findForUpdateBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L"))
                .thenReturn(Optional.of(session));

        // (a) Bước 1 ĐẠT — chưa đủ 3 bước ⇒ overall vẫn IN_PROGRESS, decided_at vẫn NULL.
        service.submitStep("INSP-20260730-L", 1, step("PASSED", null), catererA);
        assertThat(session.getOverall()).isEqualTo(StepStatus.IN_PROGRESS);
        assertThat(session.getDecidedAt()).isNull();

        // (b) Bước 2 ĐẠT (BR-090 — bắt buộc coreTemp >= 75).
        service.submitStep("INSP-20260730-L", 2, step("PASSED", new BigDecimal("78")), catererA);
        assertThat(session.getOverall()).isEqualTo(StepStatus.IN_PROGRESS);

        // (c) Bước 3 ĐẠT ⇒ overall = PASSED và decided_at được chốt (BR-098).
        service.submitStep("INSP-20260730-L", 3, step("PASSED", null), catererA);
        assertThat(session.getStep1()).isEqualTo(StepStatus.PASSED);
        assertThat(session.getStep2()).isEqualTo(StepStatus.PASSED);
        assertThat(session.getStep3()).isEqualTo(StepStatus.PASSED);
        assertThat(session.getOverall()).isEqualTo(StepStatus.PASSED);
        assertThat(session.getDecidedAt()).isNotNull();

        // (d) Một bước KHÔNG ĐẠT ⇒ overall PHẢI là FAILED, kèm decided_at (không có trạng thái lửng).
        InspectionSession other = session("INSP-20260730-S", CATERER_A);
        when(sessionRepository.findBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-S"))
                .thenReturn(Optional.of(other));
        when(sessionRepository.findForUpdateBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-S"))
                .thenReturn(Optional.of(other));
        // needsNewIngredients=true — cá ôi phải loại bỏ + nhập mới, không thể sửa tại chỗ.
        service.submitStep("INSP-20260730-S", 1,
                new SubmitStepRequest(null, null, null, null, "FAILED", null, null,
                        "Cá ôi", "HIGH", null, true, null, null, null), catererA);
        assertThat(other.getStep1()).isEqualTo(StepStatus.FAILED);
        assertThat(other.getOverall()).isEqualTo(StepStatus.FAILED);
        assertThat(other.getDecidedAt()).isNotNull();
    }

    @Test
    @DisplayName("13/08/2026 (Nam) — needsNewIngredients=false: bước KHÔNG đạt vẫn tạo CAPA nhưng "
            + "KHÔNG khoá overall=FAILED, bếp sửa & nộp lại đúng bước đó trong CÙNG phiên")
    void inPlaceFixDoesNotFailTheWholeSessionOrBlockTheGate() {
        InspectionSession session = session("INSP-20260730-L", CATERER_A);
        session.setStep1(StepStatus.PASSED);
        when(sessionRepository.findBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L"))
                .thenReturn(Optional.of(session));
        when(sessionRepository.findForUpdateBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L"))
                .thenReturn(Optional.of(session));

        // Lỗi vệ sinh dụng cụ, không đụng tới nguyên liệu — needsNewIngredients=false.
        SubmitStepRequest firstAttempt = new SubmitStepRequest(null, null, null, null, "FAILED",
                null, null, "Thiếu găng tay khi chia món", "LOW", null, false, null, null, null);
        InspectionSessionResponse afterFail = service.submitStep("INSP-20260730-L", 2, firstAttempt, catererA);

        assertThat(afterFail.step2()).isEqualTo("FAILED");
        // Khác hẳn needsNewIngredients=true: overall KHÔNG bị khoá FAILED — vẫn IN_PROGRESS.
        assertThat(afterFail.overall()).isEqualTo("IN_PROGRESS");
        assertThat(session.getOverall()).isEqualTo(StepStatus.IN_PROGRESS);
        // CAPA vẫn phải tạo — dấu vết corrective action không phụ thuộc việc có huỷ bữa hay không.
        verify(capaPort, times(1)).openFromFailedStep(any(), eq("Bước 2"),
                eq("Thiếu găng tay khi chia món"), eq(SCHOOL_ID), eq(ACTOR_ID));

        // Sửa xong, nộp lại ĐÚNG bước 2 trong CÙNG phiên (không cần reinspect()) — phải cho qua.
        InspectionSessionResponse afterFix =
                service.submitStep("INSP-20260730-L", 2, step("PASSED", new BigDecimal("80")), catererA);
        assertThat(afterFix.step2()).isEqualTo("PASSED");
        assertThat(afterFix.overall()).isEqualTo("IN_PROGRESS");

        // Rồi đi tiếp Bước 3 bình thường — không hề bị khoá bởi lần Bước 2 fail trước đó.
        InspectionSessionResponse afterStep3 =
                service.submitStep("INSP-20260730-L", 3, step("PASSED", null), catererA);
        assertThat(afterStep3.overall()).isEqualTo("PASSED");
    }

    @Test
    @DisplayName("13/08/2026 (Nam) — needsNewIngredients=true trên bước fail SAU: một khi overall đã "
            + "FAILED (từ lần fail cần nguyên liệu mới trước đó) thì các lần nộp sau vẫn giữ FAILED "
            + "(bất biến ADR-M07-02, không hồi sinh phiên bằng cách nộp lại)")
    void onceTrulyFailedResubmittingCannotRevivePassedGate() {
        InspectionSession session = session("INSP-20260730-L", CATERER_A);
        session.setStep1(StepStatus.PASSED);
        when(sessionRepository.findBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L"))
                .thenReturn(Optional.of(session));
        when(sessionRepository.findForUpdateBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L"))
                .thenReturn(Optional.of(session));

        SubmitStepRequest needsNew = new SubmitStepRequest(null, null, null, null, "FAILED",
                null, null, "Thịt có mùi lạ", "HIGH", null, true, null, null, null);
        service.submitStep("INSP-20260730-L", 2, needsNew, catererA);
        assertThat(session.getOverall()).isEqualTo(StepStatus.FAILED);

        InspectionSessionResponse afterRetry =
                service.submitStep("INSP-20260730-L", 2, step("PASSED", new BigDecimal("80")), catererA);
        assertThat(afterRetry.step2()).isEqualTo("PASSED");
        // Bước tự nó đổi được, nhưng overall vẫn bị khoá FAILED vĩnh viễn — bắt buộc reinspect().
        assertThat(afterRetry.overall()).isEqualTo("FAILED");
    }

    @Test
    @DisplayName("MF07-TC-INT-006 — nộp một bước KHÔNG xoá các bảng dòng của bước khác (không orphanRemoval nhầm)")
    void submittingOneStepNeverWipesTheOtherStepsRowTables() {
        InspectionSession session = session("INSP-20260730-L", CATERER_A);
        session.setStep1(StepStatus.PASSED);
        session.getStep1Fresh().add(freshRow("Cá basa phi lê", 0));
        session.getStep1Fresh().add(freshRow("Rau muống", 1));
        when(sessionRepository.findBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L"))
                .thenReturn(Optional.of(session));
        when(sessionRepository.findForUpdateBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L"))
                .thenReturn(Optional.of(session));

        // ⚠️ Lệch tài liệu: tài liệu viết kịch bản qua `PUT /inspections/{code}` — MF07 KHÔNG có
        // endpoint đó (chỉ có POST .../steps/{step}); kiểm cùng bất biến trên endpoint THẬT.
        service.submitStep("INSP-20260730-L", 2, step("PASSED", new BigDecimal("80")), catererA);

        assertThat(session.getStep1Fresh()).hasSize(2);            // Mẫu số 1 còn nguyên
        assertThat(session.getStep1Fresh().get(0).getName()).isEqualTo("Cá basa phi lê");
        assertThat(session.getStep1Dry()).isEmpty();
        assertThat(session.getStep3Rows()).isEmpty();
        assertThat(session.getStep1()).isEqualTo(StepStatus.PASSED);   // trạng thái bước cũ giữ nguyên
    }

    // ═════════════════════════ Tái kiểm tra (BR-096) ═════════════════════════

    @Test
    @DisplayName("MF07-TC-INT-014 — tái kiểm tra tạo phiên MỚI, phiên FAILED gốc giữ nguyên từng byte")
    void reinspectCreatesFreshSessionAndLeavesTheFailedOneUntouched() {
        InspectionSession failed = session("INSP-20260730-L", CATERER_A);
        failed.setStep1(StepStatus.FAILED);
        failed.setOverall(StepStatus.FAILED);
        failed.setFailStep(1);
        failed.setFailReason("Cá ôi");
        failed.setMenuId(UUID.randomUUID());
        failed.setDishId(UUID.randomUUID());
        failed.getStep1Fresh().add(freshRow("Cá basa phi lê", 0));
        when(sessionRepository.findBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L"))
                .thenReturn(Optional.of(failed));
        when(sessionRepository.findForUpdateBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L"))
                .thenReturn(Optional.of(failed));
        // Mã cũ đã tồn tại ⇒ generateSessionCode() phải né sang hậu tố -2.
        when(sessionRepository.existsBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L")).thenReturn(true);
        when(sessionRepository.existsBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L-2")).thenReturn(false);

        InspectionSessionResponse fresh = service.reinspect("INSP-20260730-L", catererA);

        ArgumentCaptor<InspectionSession> saved = ArgumentCaptor.forClass(InspectionSession.class);
        verify(sessionRepository, times(1)).saveAndFlush(saved.capture());
        InspectionSession created = saved.getValue();

        assertThat(created).isNotSameAs(failed);
        assertThat(created.getCode()).isEqualTo("INSP-20260730-L-2");
        assertThat(created.getOverall()).isEqualTo(StepStatus.IN_PROGRESS);
        assertThat(created.getStep1()).isEqualTo(StepStatus.PENDING);
        assertThat(created.getStep2()).isEqualTo(StepStatus.PENDING);
        assertThat(created.getStep3()).isEqualTo(StepStatus.PENDING);
        // Kế thừa ngày/buổi/đơn vị/thực đơn/món (không phải nhập lại phần định danh bữa).
        assertThat(created.getDate()).isEqualTo(failed.getDate());
        assertThat(created.getMealType()).isEqualTo(failed.getMealType());
        assertThat(created.getPartnerId()).isEqualTo(failed.getPartnerId());
        assertThat(created.getMenuId()).isEqualTo(failed.getMenuId());
        assertThat(created.getDishId()).isEqualTo(failed.getDishId());
        // Dòng biểu mẫu KHÔNG được sao chép — "tái kiểm tra" thật, phải nhập lại.
        assertThat(created.getStep1Fresh()).isEmpty();
        assertThat(fresh.id()).isEqualTo("INSP-20260730-L-2");

        // §4.2 bất biến — phiên gốc không bị đụng vào (kể cả một field).
        assertThat(failed.getOverall()).isEqualTo(StepStatus.FAILED);
        assertThat(failed.getFailStep()).isEqualTo(1);
        assertThat(failed.getFailReason()).isEqualTo("Cá ôi");
        assertThat(failed.getStep1Fresh()).hasSize(1);
    }

    // ═════════════════════════ Hợp đồng định danh & phân quyền theo field ═════════════════════════

    @Test
    @DisplayName("MF07-TC-INT-016 — JSON `id` là mã nghiệp vụ (code), KHÔNG bao giờ là UUID nội bộ (D1)")
    void jsonIdIsAlwaysTheBusinessCodeNeverTheInternalUuid() {
        InspectionSession session = session("INSP-20260730-L", CATERER_A);
        when(sessionRepository.findBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L"))
                .thenReturn(Optional.of(session));
        when(sessionRepository.findForUpdateBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L"))
                .thenReturn(Optional.of(session));

        InspectionSessionResponse response = service.detail("INSP-20260730-L", catererA);
        assertThat(response.id()).isEqualTo("INSP-20260730-L");
        assertThat(response.id()).doesNotMatch("^[0-9a-fA-F-]{36}$");   // không phải UUID

        // DTO trả ra KHÔNG có field `code` song song với `id` (một định danh duy nhất ra ngoài),
        // và không expose UUID khoá chính dưới bất kỳ tên nào.
        Set<String> keys = componentNames(InspectionSessionResponse.class);
        assertThat(keys).contains("id");
        assertThat(keys).doesNotContain("code", "uuid", "schoolId", "partnerId");
    }

    @Test
    @DisplayName("MF07-TC-INT-020 — liên kết ngược tới biên bản vi phạm (BR-183) hiện CHƯA có trong response phiên")
    void sessionResponseHasNoViolationsLinkYet() {
        // Tài liệu kỳ vọng `violations: [...]` trong response phiên (BR-183) VÀ đồng thời yêu cầu
        // "ghi nhận khoảng trống hiện tại". Code THẬT: `violation_records` do MF11 sở hữu
        // (M07-D07) và `InspectionSessionResponse` chưa có component nào cho liên kết ngược ⇒
        // ghi nhận khoảng trống bằng assert, KHÔNG viết test đỏ.
        Set<String> keys = componentNames(InspectionSessionResponse.class);
        assertThat(keys).doesNotContain("violations", "sourceInspectionId");
        // Ngược lại, dấu vết của bước trượt (nguồn sinh biên bản vi phạm) vẫn nằm trên entity.
        assertThat(fieldNamesOf(InspectionSession.class))
                .contains("failStep", "failReason", "failSeverity", "failWitness");
    }

    @Test
    @DisplayName("MF07-TC-SEC-008 — phân quyền theo FIELD: Caterer không ghi được kết quả đánh giá Y tế/`overall`, Y tế không ghi được field thực thi bước")
    void fieldLevelAuthorizationSeparatesInspectionFieldsFromMedicalReviewFields() {
        InspectionSession own = session("INSP-20260730-L", CATERER_A);
        when(sessionRepository.findBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L"))
                .thenReturn(Optional.of(own));
        when(sessionRepository.findForUpdateBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-L"))
                .thenReturn(Optional.of(own));
        InspectionSession foreign = session("INSP-20260730-B", CATERER_B);
        when(sessionRepository.findBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-B"))
                .thenReturn(Optional.of(foreign));
        when(sessionRepository.findForUpdateBySchoolIdAndCode(SCHOOL_ID, "INSP-20260730-B"))
                .thenReturn(Optional.of(foreign));

        // (a) Caterer (chủ phiên) ghi field thực thi bước — ĐƯỢC.
        assertThat(service.submitStep("INSP-20260730-L", 1, step("PASSED", null), catererA).step1())
                .isEqualTo("PASSED");

        // (b)(c) Caterer ghi `medicalReviewResult`/`medicalReviewBy`/`overall`: KHÔNG có đường nào —
        // các field đó không tồn tại trong payload nộp bước (chặn bằng thiết kế DTO, mạnh hơn 403).
        Set<String> submitFields = componentNames(SubmitStepRequest.class);
        assertThat(submitFields).doesNotContain("overall", "medicalReviewResult", "medicalReviewBy",
                "medicalReviewReason", "decidedAt", "coSignedAt");

        // (d) Medical Staff ghi nhóm field medicalReview* — ĐƯỢC.
        InspectionSessionResponse reviewed = service.medicalReview("INSP-20260730-L",
                new MedicalReviewRequest("PASSED", null), medical);
        assertThat(reviewed.medicalReviewResult()).isEqualTo("PASSED");
        // BR-222 — đánh giá Y tế KHÔNG ghi đè `overall`.
        assertThat(own.getOverall()).isNotEqualTo(StepStatus.FAILED);

        // (e)(f) Medical Staff nộp bước / gửi kèm coreTemp — TỪ CHỐI toàn bộ request (403), không
        // "ghi phần được phép rồi lặng lẽ bỏ phần còn lại".
        assertThatThrownBy(() -> service.submitStep("INSP-20260730-L", 2,
                step("PASSED", new BigDecimal("80")), medical))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("INSP-005");

        // (g) School Admin ghi field nội dung — TỪ CHỐI.
        assertThatThrownBy(() -> service.submitStep("INSP-20260730-L", 1, step("PASSED", null), schoolAdmin))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("INSP-005");

        // (h) Caterer KHÔNG sở hữu phiên — TỪ CHỐI (BR-099; code thật trả 404, xem MF07-TC-SEC-005).
        assertThatThrownBy(() -> service.submitStep("INSP-20260730-B", 1, step("PASSED", null), catererA))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("INSP-404");

        // Caterer cũng KHÔNG gọi được đánh giá Y tế (chiều ngược lại của cùng ranh giới).
        assertThatThrownBy(() -> service.medicalReview("INSP-20260730-L",
                new MedicalReviewRequest("FAILED", "vì lý do gì đó"), catererA))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("INSP-005");
    }

    // ═════════════════════════ Tiện ích ═════════════════════════

    private InspectionSession session(String code, UUID partnerId) {
        InspectionSession s = new InspectionSession();
        s.setCode(code);
        s.setSchoolId(SCHOOL_ID);
        s.setPartnerId(partnerId);
        s.setDate(DAY);
        s.setMealType(code.endsWith("S") ? MealType.SNACK : code.endsWith("B") ? MealType.BREAKFAST : MealType.LUNCH);
        s.setInspectorUserId(ACTOR_ID);
        s.setOverall(StepStatus.IN_PROGRESS);
        return s;
    }

    private static Step1FreshRow freshRow(String name, int order) {
        Step1FreshRow row = new Step1FreshRow();
        row.setRowOrder(order);
        row.setName(name);
        row.setReceivedAt("30/07/2026 06:10");
        row.setQuantity("10 kg");
        row.setSupplierName("HTX Green Farm");
        row.setSensoryPass(true);
        row.setQuickTest("NONE");
        row.setPhotoCount(0);
        return row;
    }

    private static SubmitStepRequest step(String result, BigDecimal coreTemp) {
        return new SubmitStepRequest(null, null, null, null, result, coreTemp, null,
                null, null, null, null, null, null, null);
    }

    private static Set<String> componentNames(Class<?> recordType) {
        return Arrays.stream(recordType.getRecordComponents())
                .map(RecordComponent::getName).collect(Collectors.toSet());
    }

    private static Set<String> fieldNamesOf(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .map(java.lang.reflect.Field::getName).collect(Collectors.toSet());
    }
}
