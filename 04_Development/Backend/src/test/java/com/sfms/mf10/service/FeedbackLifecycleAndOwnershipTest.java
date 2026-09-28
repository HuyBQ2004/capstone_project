package com.sfms.mf10.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.NotificationType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.storage.StorageService;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.domain.Feedback;
import com.sfms.mf10.domain.FeedbackScope;
import com.sfms.mf10.domain.SubmitterRole;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackOwnerResponse;
import com.sfms.mf10.dto.FeedbackDtos.SubmitFeedbackRequest;
import com.sfms.mf10.dto.FeedbackDtos.UpdateFeedbackRequest;
import com.sfms.mf10.dto.NotificationDtos.NotificationCommand;
import com.sfms.mf10.error.FbaError;
import com.sfms.mf10.error.FbaMessages;
import com.sfms.mf10.port.NotificationPort;
import com.sfms.mf10.repository.FeedbackAttachmentRepository;
import com.sfms.mf10.repository.FeedbackRepository;
import com.sfms.mf10.repository.FeedbackResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * MF10 — vòng đời phản hồi (gửi / cửa sổ 24h / trùng / thu hồi) và quyền sở hữu, ở tầng
 * {@code FeedbackServiceImpl} (nơi TOÀN BỘ luật nghiệp vụ nằm; controller chỉ là lớp mỏng
 * {@code @PreAuthorize} + mapping).
 *
 * <p>Phủ các case tài liệu MF10-TC-INT-001/002/004/005, MF10-TC-SEC-001/003. Các bước của
 * tài liệu cần DB thật (INSERT thẳng qua JDBC, CHECK constraint, race 2 thread) KHÔNG nằm ở
 * đây — xem ghi chú từng test.
 */
class FeedbackLifecycleAndOwnershipTest {

    private static final Instant NOW = Instant.parse("2026-07-30T02:00:00Z");
    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID OTHER_SCHOOL_ID = UUID.randomUUID();
    private static final UUID SESSION_ID = UUID.randomUUID();
    private static final UUID CLASSROOM_ID = UUID.randomUUID();
    private static final UUID STUDENT_ID = UUID.randomUUID();
    private static final UUID DISH_ID = UUID.randomUUID();
    private static final UUID PARENT_ID = UUID.randomUUID();
    private static final UUID OTHER_PARENT_ID = UUID.randomUUID();

    private FeedbackRepository feedbackRepository;
    private FeedbackResponseRepository feedbackResponseRepository;
    private FeedbackAttachmentRepository attachmentRepository;
    private ReferenceResolver referenceResolver;
    private CodeGenerator codeGenerator;
    private IllnessKeywordDetector illnessKeywordDetector;
    private NotificationPort notificationPort;
    private FeedbackServiceImpl service;

    private static AuthUser parent(UUID userId) {
        return new AuthUser(userId, "U-9905", "parent@sfms.vn", RoleKey.PARENT, SCHOOL_ID, null);
    }

    private static AuthUser schoolAdmin() {
        return new AuthUser(UUID.randomUUID(), "U-9902", "admin@sfms.vn", RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);
    }

    @BeforeEach
    void setUp() {
        feedbackRepository = mock(FeedbackRepository.class);
        feedbackResponseRepository = mock(FeedbackResponseRepository.class);
        attachmentRepository = mock(FeedbackAttachmentRepository.class);
        referenceResolver = mock(ReferenceResolver.class);
        codeGenerator = mock(CodeGenerator.class);
        illnessKeywordDetector = mock(IllnessKeywordDetector.class);
        notificationPort = mock(NotificationPort.class);
        StorageService storageService = mock(StorageService.class);
        FeedbackWindowPolicy windowPolicy =
                new FeedbackWindowPolicy(referenceResolver, Clock.fixed(NOW, ZoneOffset.UTC));

        service = new FeedbackServiceImpl(feedbackRepository, feedbackResponseRepository, attachmentRepository,
                mock(FeedbackAttachmentSupport.class), referenceResolver, codeGenerator, windowPolicy,
                illnessKeywordDetector, notificationPort, storageService);

        when(codeGenerator.next(anyString(), anyString(), eq(4))).thenReturn("FB-2026-0001");
        when(illnessKeywordDetector.detect(any())).thenReturn(false);
        when(feedbackResponseRepository.findByFeedbackIdOrderByRespondedAtAsc(any())).thenReturn(List.of());
        when(attachmentRepository.findByFeedbackIdOrderByCreatedAtAsc(any())).thenReturn(List.of());
    }

    /** Bữa ăn phục vụ {@code hoursAgo} giờ trước, học sinh HS-3A-001 gắn với {@link #PARENT_ID}. */
    private void seedServedMeal(String sessionCode, long hoursAgo) {
        when(referenceResolver.distributionIdByCode(sessionCode, SCHOOL_ID)).thenReturn(Optional.of(SESSION_ID));
        when(referenceResolver.servedAtOf(SESSION_ID)).thenReturn(Optional.of(NOW.minus(Duration.ofHours(hoursAgo))));
        when(referenceResolver.distributionCode(SESSION_ID)).thenReturn(sessionCode);
        var studentRef = new ReferenceResolver.StudentRef(STUDENT_ID, "HS-3A-001", "Nguyễn Minh Khang",
                CLASSROOM_ID, SCHOOL_ID);
        when(referenceResolver.studentByCode("HS-3A-001", SCHOOL_ID)).thenReturn(Optional.of(studentRef));
        when(referenceResolver.isParentLinkedToStudent(PARENT_ID, STUDENT_ID)).thenReturn(true);
        when(referenceResolver.classroomCode(CLASSROOM_ID)).thenReturn("3A");
        when(referenceResolver.dishIdByCode("com-trang")).thenReturn(Optional.of(DISH_ID));
        when(referenceResolver.dishName(DISH_ID)).thenReturn("Cơm trắng");
        when(referenceResolver.dishCode(DISH_ID)).thenReturn("com-trang");
    }

    private static SubmitFeedbackRequest dishFeedback(String sessionCode, String dishCode, int rating,
                                                       List<String> tags, String comment) {
        return new SubmitFeedbackRequest(sessionCode, "HS-3A-001", "DISH", null, LocalDate.of(2026, 7, 30),
                "LUNCH", dishCode, rating, tags, comment, null);
    }

    private Feedback ownedFeedback(UUID ownerId) {
        Feedback fb = new Feedback();
        fb.setCode("FB-2026-0009");
        fb.setSchoolId(SCHOOL_ID);
        fb.setSubmitterUserId(ownerId);
        fb.setSubmitterRole(SubmitterRole.PARENT);
        fb.setScope(FeedbackScope.DISH);
        fb.setMealSessionId(SESSION_ID);
        fb.setMealDate(LocalDate.of(2026, 7, 30));
        fb.setMealType(com.sfms.common.enums.MealType.LUNCH);
        fb.setRating(4);
        fb.setTags(List.of("Món ăn nguội"));
        fb.setClassName("3A");
        fb.setSubmittedAt(NOW.minus(Duration.ofHours(1)));
        return fb;
    }

    // ═══════════════════════ MF10-TC-INT-001 ═══════════════════════

    @Test
    @DisplayName("MF10-TC-INT-001 — phản hồi rating thấp có từ khóa bệnh lý: illnessKeyword do SERVER tính "
            + "và phát cảnh báo ALLERGY_ALERT (MSG-058) tới Medical Staff + School Admin")
    void lowRatingWithIllnessKeywordSavesAndDispatchesAllergyAlert() {
        seedServedMeal("DSTU-2026-0001", 2);
        when(illnessKeywordDetector.detect("Con về kêu đau bụng nhẹ")).thenReturn(true);
        when(feedbackRepository.existsBySubmitterUserIdAndMealSessionIdAndDishIdAndStudentIdAndDeletedAtIsNull(
                any(), any(), any(), any())).thenReturn(false);

        FeedbackOwnerResponse response = service.submit(
                dishFeedback("DSTU-2026-0001", "com-trang", 2, List.of("Món ăn nguội"), "Con về kêu đau bụng nhẹ"),
                parent(PARENT_ID));

        ArgumentCaptor<Feedback> saved = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().isIllnessKeyword()).isTrue();
        assertThat(saved.getValue().getMealSessionId()).isEqualTo(SESSION_ID);
        assertThat(saved.getValue().getSubmittedAt()).isNotNull();
        assertThat(response.id()).matches("^FB-\\d{4}-\\d+$");
        // editableUntil = servedAt + 24h (BR-139).
        assertThat(response.editableUntil()).isEqualTo(NOW.minus(Duration.ofHours(2)).plus(FeedbackWindowPolicy.WINDOW));

        ArgumentCaptor<NotificationCommand> cmd = ArgumentCaptor.forClass(NotificationCommand.class);
        verify(notificationPort).publish(cmd.capture());
        assertThat(cmd.getValue().type()).isEqualTo(NotificationType.ALLERGY_ALERT);
        assertThat(cmd.getValue().content()).isEqualTo(FbaMessages.MSG_058);
        assertThat(cmd.getValue().recipientRoles())
                .containsExactlyInAnyOrder(RoleKey.MEDICAL_STAFF.key(), RoleKey.SCHOOL_ADMIN.key());
        // ⚠️ Lệch tài liệu: code THẬT chỉ phát 1 thông báo (MSG-058). Thông báo MSG-057 ("rating<3")
        // và "FeedbackSubmitted" tới caterer CHƯA được gửi — CF-M10-9 ghi rõ D6 không có
        // NotificationType nào mang nghĩa "phản hồi tiêu cực", nên chủ ý treo.
        verify(notificationPort, times(1)).publish(any());
    }

    @Test
    @DisplayName("MF10-TC-INT-001 — client KHÔNG thể tự khai illnessKeyword: DTO ghi vào không có field đó, "
            + "server luôn tính lại từ comment (L9)")
    void illnessKeywordIsDerivedNotAcceptedFromClient() {
        assertThat(SubmitFeedbackRequest.class.getRecordComponents())
                .extracting(java.lang.reflect.RecordComponent::getName)
                .doesNotContain("illnessKeyword", "submittedAt", "responded", "editableUntil");
        assertThat(UpdateFeedbackRequest.class.getRecordComponents())
                .extracting(java.lang.reflect.RecordComponent::getName)
                .containsExactlyInAnyOrder("rating", "tags", "comment");
    }

    // ═══════════════════════ MF10-TC-INT-002 ═══════════════════════

    @Test
    @DisplayName("MF10-TC-INT-002 — bữa phục vụ quá 24h bị chặn ở tầng service (FBA-022, message MSG-022 "
            + "nguyên văn) và KHÔNG ghi dòng nào xuống DB")
    void submitOutsideTwentyFourHourWindowIsBlockedAndNothingPersisted() {
        seedServedMeal("DSTU-2026-0002", 34);

        assertThatThrownBy(() -> service.submit(
                dishFeedback("DSTU-2026-0002", "com-trang", 4, List.of(), "Muộn rồi"), parent(PARENT_ID)))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(FbaError.WINDOW_CLOSED);
                    assertThat(ex.getStatus().value()).isEqualTo(409);
                    assertThat(ex.getMessage()).isEqualTo(FbaMessages.MSG_022);
                    assertThat(ex.getDetails()).isNotNull().isEmpty();
                });

        verify(feedbackRepository, never()).saveAndFlush(any());
        verifyNoInteractions(notificationPort);
    }

    @Test
    @DisplayName("MF10-TC-INT-002 — mealSessionId không tồn tại trả FBA-404, phân biệt rõ với 409 cửa sổ đóng")
    void submitWithUnknownMealSessionIsNotFound() {
        when(referenceResolver.distributionIdByCode("MS-9999", SCHOOL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.submit(
                dishFeedback("MS-9999", "com-trang", 4, List.of(), "Ngon"), parent(PARENT_ID)))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(FbaError.NOT_FOUND);
                    assertThat(ex.getStatus().value()).isEqualTo(404);
                });
    }

    // ═══════════════════════ MF10-TC-INT-004 ═══════════════════════

    @Test
    @DisplayName("MF10-TC-INT-004 — trùng (người × phiên ăn × món × học sinh) bị chặn bằng FBA-DUP; "
            + "khác món thì vẫn gửi được")
    void duplicateKeyIsRejectedWhileDifferentDishSucceeds() {
        seedServedMeal("DSTU-2026-0003", 2);
        when(feedbackRepository.existsBySubmitterUserIdAndMealSessionIdAndDishIdAndStudentIdAndDeletedAtIsNull(
                PARENT_ID, SESSION_ID, DISH_ID, STUDENT_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.submit(
                dishFeedback("DSTU-2026-0003", "com-trang", 5, List.of(), "Ngon"), parent(PARENT_ID)))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(FbaError.DUPLICATE_FEEDBACK);
                    assertThat(ex.getStatus().value()).isEqualTo(409);
                });
        verify(feedbackRepository, never()).saveAndFlush(any());

        UUID otherDishId = UUID.randomUUID();
        when(referenceResolver.dishIdByCode("canh-mong-toi")).thenReturn(Optional.of(otherDishId));
        when(feedbackRepository.existsBySubmitterUserIdAndMealSessionIdAndDishIdAndStudentIdAndDeletedAtIsNull(
                PARENT_ID, SESSION_ID, otherDishId, STUDENT_ID)).thenReturn(false);

        FeedbackOwnerResponse ok = service.submit(
                dishFeedback("DSTU-2026-0003", "canh-mong-toi", 5, List.of(), "Ngon"), parent(PARENT_ID));

        assertThat(ok.id()).isEqualTo("FB-2026-0001");
        verify(feedbackRepository).saveAndFlush(any());
        // ⚠️ Bước "2 thread đồng thời" của tài liệu cần DB thật (UNIQUE uq_fb_once) — không mô phỏng
        // được ở tầng unit; ở đây khóa hành vi của nhánh kiểm-trước-khi-ghi.
    }

    // ═══════════════════════ MF10-TC-INT-005 ═══════════════════════

    @Test
    @DisplayName("MF10-TC-INT-005 — thu hồi phản hồi là SOFT DELETE: chỉ set deleted_at, không gọi delete()")
    void retractIsSoftDeleteNotHardDelete() {
        Feedback fb = ownedFeedback(PARENT_ID);
        when(referenceResolver.servedAtOf(SESSION_ID)).thenReturn(Optional.of(NOW.minus(Duration.ofHours(2))));
        when(feedbackRepository.findBySchoolIdAndCodeAndDeletedAtIsNull(SCHOOL_ID, "FB-2026-0009"))
                .thenReturn(Optional.of(fb));

        service.retract("FB-2026-0009", parent(PARENT_ID));

        assertThat(fb.getDeletedAt()).isNotNull();
        verify(feedbackRepository).saveAndFlush(fb);
        // Cast tường minh: Spring Data 4 thêm overload delete(DeleteSpecification<T>) nên
        // delete(any(Feedback.class)) trở nên nhập nhằng lúc biên dịch.
        verify(feedbackRepository, never()).delete((Feedback) any());
        verify(feedbackRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("MF10-TC-INT-005 — thu hồi lần 2 trả FBA-404 (truy vấn luôn kèm deletedAtIsNull nên bản ghi "
            + "đã xóa mềm không đọc lại được)")
    void retractingTwiceIsNotFound() {
        when(feedbackRepository.findBySchoolIdAndCodeAndDeletedAtIsNull(SCHOOL_ID, "FB-2026-0009"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retract("FB-2026-0009", parent(PARENT_ID)))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.NOT_FOUND));
    }

    @Test
    @DisplayName("MF10-TC-INT-005 — thu hồi NGOÀI cửa sổ 24h vẫn bị BR-139 chặn (FBA-022), dữ liệu giữ nguyên")
    void retractOutsideWindowIsBlocked() {
        Feedback fb = ownedFeedback(PARENT_ID);
        when(referenceResolver.servedAtOf(SESSION_ID)).thenReturn(Optional.of(NOW.minus(Duration.ofHours(30))));
        when(feedbackRepository.findBySchoolIdAndCodeAndDeletedAtIsNull(SCHOOL_ID, "FB-2026-0009"))
                .thenReturn(Optional.of(fb));

        assertThatThrownBy(() -> service.retract("FB-2026-0009", parent(PARENT_ID)))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.WINDOW_CLOSED));

        assertThat(fb.getDeletedAt()).isNull();
        verify(feedbackRepository, never()).saveAndFlush(any());
    }

    // ═══════════════════════ MF10-TC-SEC-001 (IDOR) ═══════════════════════

    @Test
    @DisplayName("MF10-TC-SEC-001 — Parent gửi phản hồi cho con NHÀ KHÁC bị từ chối FBA-005: quan hệ "
            + "phụ huynh–học sinh được tra ở server, không tin studentId trong body")
    void parentCannotSubmitForAnotherParentsChild() {
        seedServedMeal("DSTU-2026-0004", 2);
        var otherChild = new ReferenceResolver.StudentRef(UUID.randomUUID(), "HS-3A-002", "Học sinh khác",
                CLASSROOM_ID, SCHOOL_ID);
        when(referenceResolver.studentByCode("HS-3A-002", SCHOOL_ID)).thenReturn(Optional.of(otherChild));
        when(referenceResolver.isParentLinkedToStudent(PARENT_ID, otherChild.id())).thenReturn(false);

        var req = new SubmitFeedbackRequest("DSTU-2026-0004", "HS-3A-002", "DISH", null,
                LocalDate.of(2026, 7, 30), "LUNCH", "com-trang", 5, List.of(), "Ngon", null);

        assertThatThrownBy(() -> service.submit(req, parent(PARENT_ID)))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(FbaError.ACCESS_DENIED);
                    assertThat(ex.getStatus().value()).isEqualTo(403);
                });
        verify(feedbackRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("MF10-TC-SEC-001 — Parent KHÔNG sửa/xóa được phản hồi của phụ huynh khác: quyền xét theo "
            + "submitter_user_id trong DB, không theo tham số request")
    void parentCannotEditOrDeleteAnotherParentsFeedback() {
        Feedback foreign = ownedFeedback(OTHER_PARENT_ID);
        when(feedbackRepository.findBySchoolIdAndCodeAndDeletedAtIsNull(SCHOOL_ID, "FB-2026-0009"))
                .thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.update("FB-2026-0009",
                new UpdateFeedbackRequest(1, List.of("Món ăn nguội"), "Sửa trộm"), parent(PARENT_ID)))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.ACCESS_DENIED));

        assertThatThrownBy(() -> service.retract("FB-2026-0009", parent(PARENT_ID)))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.ACCESS_DENIED));

        verify(feedbackRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("MF10-TC-SEC-001 — Parent đọc CHI TIẾT phản hồi của người khác bị chặn FBA-005 (chọn 403, "
            + "nhất quán, không lúc 403 lúc 404)")
    void parentCannotReadAnotherParentsFeedbackDetail() {
        Feedback foreign = ownedFeedback(OTHER_PARENT_ID);
        when(feedbackRepository.findBySchoolIdAndCodeAndDeletedAtIsNull(SCHOOL_ID, "FB-2026-0009"))
                .thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.getDetail("FB-2026-0009", parent(PARENT_ID)))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.ACCESS_DENIED));
    }

    // ═══════════════════════ MF10-TC-SEC-003 (cách ly theo trường) ═══════════════════════

    @Test
    @DisplayName("MF10-TC-SEC-003 — schoolId luôn lấy từ JWT: truy vấn phản hồi dùng đúng school của actor, "
            + "bản ghi trường khác trả 404 (không lộ sự tồn tại)")
    void feedbackReadsAreScopedToTheJwtSchool() {
        AuthUser admin = schoolAdmin();
        when(feedbackRepository.findBySchoolIdAndCodeAndDeletedAtIsNull(any(), anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDetail("FB-2026-0009", admin))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.NOT_FOUND));

        ArgumentCaptor<UUID> schoolCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(feedbackRepository).findBySchoolIdAndCodeAndDeletedAtIsNull(schoolCaptor.capture(), eq("FB-2026-0009"));
        assertThat(schoolCaptor.getValue()).isEqualTo(SCHOOL_ID).isNotEqualTo(OTHER_SCHOOL_ID);
    }

    @Test
    @DisplayName("MF10-TC-SEC-003 — resolveSchoolId chỉ đọc claim của JWT, không nhận schoolId từ query/body")
    void resolveSchoolIdComesFromTheTokenOnly() {
        assertThat(service.resolveSchoolId(parent(PARENT_ID))).isEqualTo(SCHOOL_ID);

        // Parent không mang claim schoolId ⇒ tra ngược qua ReferenceResolver, vẫn KHÔNG lấy từ request.
        AuthUser parentWithoutSchool = new AuthUser(PARENT_ID, "U-9905", "parent@sfms.vn", RoleKey.PARENT, null, null);
        when(referenceResolver.firstSchoolIdForParent(PARENT_ID)).thenReturn(Optional.of(OTHER_SCHOOL_ID));
        assertThat(service.resolveSchoolId(parentWithoutSchool)).isEqualTo(OTHER_SCHOOL_ID);
    }
}
