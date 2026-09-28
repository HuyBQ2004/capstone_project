package com.sfms.mf07.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.storage.StorageService;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf07.domain.ChecklistGroup;
import com.sfms.mf07.domain.ChecklistItem;
import com.sfms.mf07.domain.ConfirmStatus;
import com.sfms.mf07.domain.KitchenInspection;
import com.sfms.mf07.domain.StepStatus;
import com.sfms.mf07.dto.InspectionDtos.MedicalReviewRequest;
import com.sfms.mf07.dto.KitchenInspectionDtos.ItemResultRequest;
import com.sfms.mf07.dto.KitchenInspectionDtos.KitchenInspectionResponse;
import com.sfms.mf07.dto.KitchenInspectionDtos.RecordUpdateRequest;
import com.sfms.mf07.dto.KitchenInspectionDtos.SubmitKitchenInspectionRequest;
import com.sfms.mf07.repository.ChecklistGroupRepository;
import com.sfms.mf07.repository.ChecklistItemRepository;
import com.sfms.mf07.repository.ChecklistItemResultRepository;
import com.sfms.mf07.repository.InspectionEvidenceRepository;
import com.sfms.mf07.repository.KitchenInspectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF07 — TC tài liệu về chấm điểm vệ sinh bếp (BR-100/BR-104/ADR-M07-03) và checklist động
 * (BR-207/BR-210), hiện thực bằng Mockito trên {@link KitchenInspectionServiceImpl} và
 * {@link ChecklistGroupServiceImpl}. Mã nguồn thật là oracle.
 */
class KitchenHygieneAndChecklistTest {

    private static final UUID SCHOOL_ID = UUID.fromString("00000000-0000-0000-0000-0000000a0042");

    private KitchenInspectionRepository inspectionRepository;
    private ChecklistItemRepository itemRepository;
    private KitchenInspectionServiceImpl kitchenService;

    private ChecklistGroupRepository groupRepository;
    private ChecklistItemResultRepository resultRepository;
    private ChecklistGroupServiceImpl checklistService;

    private final AuthUser medical =
            new AuthUser(UUID.randomUUID(), "U-0002", "medical@sfms.vn", RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
    private final AuthUser caterer =
            new AuthUser(UUID.randomUUID(), "U-0022", "caterer@sfms.vn", RoleKey.SERVICE_CATERERS, SCHOOL_ID, null);
    private final AuthUser schoolAdmin =
            new AuthUser(UUID.randomUUID(), "U-0001", "admin@sfms.vn", RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);

    @BeforeEach
    void setUp() {
        inspectionRepository = mock(KitchenInspectionRepository.class);
        itemRepository = mock(ChecklistItemRepository.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        CodeGenerator codeGenerator = mock(CodeGenerator.class);
        EvidenceService evidenceService = mock(EvidenceService.class);
        InspectionEvidenceRepository evidenceRepository = mock(InspectionEvidenceRepository.class);
        StorageService storageService = mock(StorageService.class);
        Clock clock = Clock.fixed(Instant.parse("2026-07-30T02:00:00Z"), ZoneOffset.UTC);

        kitchenService = new KitchenInspectionServiceImpl(inspectionRepository, itemRepository, referenceResolver,
                codeGenerator, evidenceService, evidenceRepository, storageService, clock);

        groupRepository = mock(ChecklistGroupRepository.class);
        resultRepository = mock(ChecklistItemResultRepository.class);
        checklistService = new ChecklistGroupServiceImpl(groupRepository, resultRepository);

        when(codeGenerator.next(anyString(), anyString(), anyInt())).thenReturn("KIN-2026-018");
        when(inspectionRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        when(groupRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        when(referenceResolver.userById(any())).thenReturn(null);
    }

    // ═════════════════════════ Chấm điểm bếp (UC-014) ═════════════════════════

    @Test
    @DisplayName("MF07-TC-INT-009 — 11/20 hạng mục đạt ⇒ score=55 tính lại ở server, FAILED + cảnh báo MSG-093, KHÔNG khoá bếp")
    void kitchenScoreIsRecomputedServerSideAndNeverLocksTheKitchen() {
        List<ItemResultRequest> items = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            boolean passed = i <= 11;
            items.add(new ItemResultRequest("CLI-1-" + i, passed,
                    passed ? null : "Không đạt hạng mục " + i, passed ? null : 1));
            stubItem("CLI-1-" + i, "Hạng mục " + i);
        }
        // `score` do client gửi KHÔNG tồn tại trong DTO (CF-26) — không có đường nào ép điểm.
        assertThat(componentNames(SubmitKitchenInspectionRequest.class)).doesNotContain("score", "result");

        KitchenInspectionResponse response = kitchenService.submit(
                new SubmitKitchenInspectionRequest(true, false, items), medical);

        assertThat(response.score()).isEqualTo(55);                 // round(11/20*100)
        assertThat(response.result()).isEqualTo("FAILED");          // ADR-M07-03
        assertThat(response.warnings()).hasSize(1);
        assertThat(response.warnings().get(0).code()).isEqualTo("MSG-093");
        assertThat(response.confirmStatus()).isEqualTo("AWAITING_CATERERS_CONFIRM");
        assertThat(response.staffHealthValid()).isFalse();          // BR-103 — lưu, nhưng không tự chặn gì
        assertThat(response.failedItems()).hasSize(9);

        // "Không khoá bếp" (L6/CF-2 descoped): entity KHÔNG có bất kỳ field cách ly/khoá nào,
        // và điểm thấp chỉ sinh cảnh báo — không có trạng thái nào ngoài PASSED/FAILED.
        Set<String> entityFields = fieldNames(KitchenInspection.class);
        assertThat(entityFields).noneSatisfy(f ->
                assertThat(f.toLowerCase()).containsAnyOf("quarantine", "locked", "blocked", "suspend"));
        assertThat(StepStatus.valueOf(response.result())).isIn(StepStatus.PASSED, StepStatus.FAILED);
    }

    @Test
    @DisplayName("MF07-TC-SEC-004 — Service Caterers KHÔNG tự chấm điểm bếp của mình, nhưng vẫn đọc kết quả và xác nhận biên bản của mình")
    @SuppressWarnings("unchecked")
    void serviceCaterersCannotSelfScoreButCanReadAndConfirm() {
        stubItem("CLI-1-1", "Sàn bếp khô ráo");
        SubmitKitchenInspectionRequest body = new SubmitKitchenInspectionRequest(
                true, true, List.of(new ItemResultRequest("CLI-1-1", true, null, null)));

        // (1) Xung đột lợi ích — bên bị kiểm tra không được tự chấm.
        assertThatThrownBy(() -> kitchenService.submit(body, caterer))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("INSP-005");
        verify(inspectionRepository, never()).saveAndFlush(any());

        // (2) Đọc kết quả của trường mình — ĐƯỢC (không lọc theo partner, TDS §9.2 hàng 6).
        Page<KitchenInspection> empty = new PageImpl<>(List.of());
        when(inspectionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(empty);
        assertThat(kitchenService.list(ListQuery.from(Map.of()), caterer).items()).isEmpty();

        // (3) Xác nhận hai chiều biên bản của chính mình (BR-221) — ĐƯỢC.
        KitchenInspection own = kitchenInspection("KIN-2026-018");
        when(inspectionRepository.findBySchoolIdAndCode(SCHOOL_ID, "KIN-2026-018")).thenReturn(Optional.of(own));
        assertThat(kitchenService.confirmByCaterers("KIN-2026-018", caterer).confirmStatus())
                .isEqualTo("CONFIRMED");
        assertThat(own.getConfirmedBy()).isEqualTo(caterer.userId());
        assertThat(own.getConfirmedAt()).isNotNull();

        // Chiều ngược lại: Medical Staff (bên chấm điểm) KHÔNG ký thay bên bị kiểm tra.
        assertThatThrownBy(() -> kitchenService.confirmByCaterers("KIN-2026-018", medical))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("INSP-005");
    }

    @Test
    @DisplayName("MF07-TC-SEC-009 — MF07 không mở cửa nào cho Y tế tự duyệt biên bản của chính mình (BR-186/BR-221/BR-222)")
    void mf07NeverExposesAnApprovalFieldToTheRecordAuthor() {
        // `resolution`/`rejectReason` thuộc biên bản vi phạm của MF11 (M07-D07) — MF07 KHÔNG có
        // field nào như thế trong bất kỳ DTO ghi nào ⇒ Y tế không thể tự duyệt qua đường MF07.
        assertThat(componentNames(RecordUpdateRequest.class)).containsExactly("content");
        assertThat(componentNames(MedicalReviewRequest.class))
                .containsExactlyInAnyOrder("medicalReviewResult", "medicalReviewReason");
        assertThat(componentNames(SubmitKitchenInspectionRequest.class))
                .doesNotContain("resolution", "rejectReason", "status", "approvedBy");
        assertThat(fieldNames(KitchenInspection.class)).doesNotContain("resolution", "rejectReason");

        // Kiểm soát hai lớp còn lại của MF07: người VIẾT biên bản (Medical) khác người XÁC NHẬN
        // (Service Caterers), và sau khi bên kia xác nhận thì tác giả không sửa được nữa.
        KitchenInspection confirmed = kitchenInspection("KIN-2026-018");
        confirmed.setConfirmStatus(ConfirmStatus.CONFIRMED);
        when(inspectionRepository.findBySchoolIdAndCode(SCHOOL_ID, "KIN-2026-018"))
                .thenReturn(Optional.of(confirmed));
        assertThatThrownBy(() -> kitchenService.updateRecord("KIN-2026-018", "sửa lại nội dung", medical))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("INSP-079");

        // School Admin cũng không phải người sửa biên bản kiểm tra bếp.
        KitchenInspection open = kitchenInspection("KIN-2026-019");
        when(inspectionRepository.findBySchoolIdAndCode(SCHOOL_ID, "KIN-2026-019")).thenReturn(Optional.of(open));
        assertThatThrownBy(() -> kitchenService.updateRecord("KIN-2026-019", "nội dung", schoolAdmin))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("INSP-005");
    }

    // ═════════════════════════ Checklist động (BR-210) ═════════════════════════

    @Test
    @DisplayName("MF07-TC-INT-010 — xoá nhóm checklist là XOÁ MỀM có xác nhận; lịch sử checklist_item_results không bị xoá lây")
    void deletingAChecklistGroupIsSoftAndPreservesHistory() {
        ChecklistGroup group = new ChecklistGroup();
        group.setCode("CLG-3");
        group.setSchoolId(SCHOOL_ID);
        group.setName("Vệ sinh sàn");
        group.setActive(true);
        when(groupRepository.findBySchoolIdAndCode(SCHOOL_ID, "CLG-3")).thenReturn(Optional.of(group));
        when(resultRepository.existsByGroupId(group.getId())).thenReturn(true);   // đã có phiên tham chiếu

        // (1) Chưa xác nhận ⇒ 409 (CF-15/MSG-094), nhóm vẫn đang hiệu lực.
        assertThatThrownBy(() -> checklistService.delete("CLG-3", false, medical))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("INSP-409");
        assertThat(group.isActive()).isTrue();
        verify(groupRepository, never()).saveAndFlush(any());

        // (2) Có ?confirmed=true ⇒ xoá MỀM: is_active=false, KHÔNG DELETE dòng nào.
        checklistService.delete("CLG-3", true, medical);
        assertThat(group.isActive()).isFalse();
        verify(groupRepository, never()).delete(any());
        verify(groupRepository, never()).deleteById(any());
        verify(resultRepository, never()).delete(any());
        verify(resultRepository, never()).deleteAll();

        // (3) Nhóm đã ẩn không còn xuất hiện cho phiên MỚI (chỉ đọc nhóm active), trong khi
        // dòng kết quả lịch sử vẫn nằm nguyên ở bảng checklist_item_results.
        when(groupRepository.findBySchoolIdAndActiveTrueOrderByDisplayOrderAsc(SCHOOL_ID))
                .thenReturn(List.of());
        assertThat(checklistService.list(medical)).isEmpty();
        verify(groupRepository).findBySchoolIdAndActiveTrueOrderByDisplayOrderAsc(SCHOOL_ID);
    }

    // ═════════════════════════ Tiện ích ═════════════════════════

    private void stubItem(String code, String name) {
        ChecklistItem item = new ChecklistItem();
        item.setCode(code);
        item.setName(name);
        item.setSchoolId(SCHOOL_ID);
        when(itemRepository.findBySchoolIdAndCode(eq(SCHOOL_ID), eq(code))).thenReturn(Optional.of(item));
    }

    private KitchenInspection kitchenInspection(String code) {
        KitchenInspection k = new KitchenInspection();
        k.setCode(code);
        k.setSchoolId(SCHOOL_ID);
        k.setDate(java.time.LocalDate.of(2026, 7, 30));
        k.setScore(55);
        k.setResult(StepStatus.FAILED);
        k.setInspectorUserId(medical.userId());
        return k;
    }

    private static Set<String> componentNames(Class<?> recordType) {
        return Arrays.stream(recordType.getRecordComponents())
                .map(RecordComponent::getName).collect(Collectors.toSet());
    }

    private static Set<String> fieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields()).map(Field::getName).collect(Collectors.toSet());
    }
}
