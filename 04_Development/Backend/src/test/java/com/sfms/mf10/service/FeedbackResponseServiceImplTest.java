package com.sfms.mf10.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.domain.Feedback;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackResponseView;
import com.sfms.mf10.dto.FeedbackDtos.RespondRequest;
import com.sfms.mf10.error.FbaError;
import com.sfms.mf10.repository.FeedbackRepository;
import com.sfms.mf10.repository.FeedbackResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

/**
 * {@code FeedbackResponseServiceImpl.respond()} — BR-143/UC-163/TDS-MF10 TC-023: chỉ Service
 * Caterers trả lời được, nội dung không được rỗng (EX1), và trả lời >48h cho đánh giá tiêu cực
 * (rating&lt;3) được đánh dấu {@code lateResponse=true} mà KHÔNG chặn (EX2). Trước bản này
 * KHÔNG có unit test nào cho lớp này — {@code FeedbackAnnouncementFlowIT} chỉ phủ 1 nhánh happy
 * path (phản hồi rating=2 trả lời ngay ⇒ lateResponse=false), không chạm role guard, blank
 * content, ngưỡng 48h, hay rating>=3 không bao giờ late.
 */
class FeedbackResponseServiceImplTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID FEEDBACK_ID = UUID.randomUUID();

    private FeedbackRepository feedbackRepository;
    private FeedbackResponseRepository responseRepository;
    private FeedbackService feedbackService;
    private ReferenceResolver referenceResolver;
    private CodeGenerator codeGenerator;
    private FeedbackResponseServiceImpl service;

    private static AuthUser caterer() {
        return new AuthUser(UUID.randomUUID(), "U-9911", "caterer@sfms.vn", RoleKey.SERVICE_CATERERS, SCHOOL_ID, null);
    }

    private static AuthUser schoolAdmin() {
        return new AuthUser(UUID.randomUUID(), "U-9902", "admin@sfms.vn", RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);
    }

    private static Feedback feedback(int rating, Instant submittedAt) {
        Feedback fb = new Feedback();
        fb.setCode("FB-2026-0001");
        fb.setSchoolId(SCHOOL_ID);
        fb.setRating(rating);
        fb.setSubmittedAt(submittedAt);
        return fb;
    }

    @BeforeEach
    void setUp() {
        feedbackRepository = mock(FeedbackRepository.class);
        responseRepository = mock(FeedbackResponseRepository.class);
        feedbackService = mock(FeedbackService.class);
        referenceResolver = mock(ReferenceResolver.class);
        codeGenerator = mock(CodeGenerator.class);
        service = new FeedbackResponseServiceImpl(feedbackRepository, responseRepository, feedbackService,
                referenceResolver, codeGenerator);

        when(feedbackService.resolveSchoolId(any())).thenReturn(SCHOOL_ID);
        when(codeGenerator.next(anyString(), anyString(), eq(3))).thenReturn("FBR-2026-001");
        when(referenceResolver.userById(any())).thenReturn(new String[]{"U-9911", "", "Đơn vị suất ăn An Bình"});
    }

    @Test
    void nonCatererRoleIsDenied() {
        assertThatThrownBy(() -> service.respond("FB-2026-0001", new RespondRequest("Đã xử lý."), schoolAdmin()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.ACCESS_DENIED));

        verify(feedbackRepository, never()).findBySchoolIdAndCodeAndDeletedAtIsNull(any(), anyString());
    }

    @Test
    void blankContentIsRejected() {
        assertThatThrownBy(() -> service.respond("FB-2026-0001", new RespondRequest("   "), caterer()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.VALIDATION_FAILED));
    }

    @Test
    void nullContentIsRejected() {
        assertThatThrownBy(() -> service.respond("FB-2026-0001", new RespondRequest(null), caterer()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.VALIDATION_FAILED));
    }

    @Test
    void unknownFeedbackCodeIs404() {
        when(feedbackRepository.findBySchoolIdAndCodeAndDeletedAtIsNull(SCHOOL_ID, "FB-2026-9999"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.respond("FB-2026-9999", new RespondRequest("Đã xử lý."), caterer()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.NOT_FOUND));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("MF10-TC-INT-006 — danh tính người trả lời lấy từ JWT (không từ body), "
            + "trả lời được LƯU với mã FBR-…, và nhiều lần trả lời thì NỐI THÊM chứ không ghi đè")
    void responderIdentityComesFromTheJwtAndRepliesAppend() {
        Feedback fb = feedback(2, Instant.now().minus(java.time.Duration.ofHours(1)));
        when(feedbackRepository.findBySchoolIdAndCodeAndDeletedAtIsNull(SCHOOL_ID, "FB-2026-0001"))
                .thenReturn(Optional.of(fb));
        AuthUser actor = caterer();

        FeedbackResponseView first = service.respond("FB-2026-0001", new RespondRequest("Đã điều chỉnh."), actor);

        var saved = org.mockito.ArgumentCaptor.forClass(com.sfms.mf10.domain.FeedbackResponse.class);
        verify(responseRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getResponderUserId()).isEqualTo(actor.userId());
        assertThat(saved.getValue().getFeedbackId()).isEqualTo(fb.getId());
        assertThat(saved.getValue().getSchoolId()).isEqualTo(SCHOOL_ID);
        assertThat(first.id()).isEqualTo("FBR-2026-001");
        assertThat(first.feedbackId()).isEqualTo("FB-2026-0001");

        // Trả lời lần 2 ⇒ INSERT thêm một dòng nữa (TDS §6.6), không update dòng cũ.
        service.respond("FB-2026-0001", new RespondRequest("Bổ sung thông tin."), actor);
        verify(responseRepository, org.mockito.Mockito.times(2))
                .saveAndFlush(any(com.sfms.mf10.domain.FeedbackResponse.class));
        // ⚠️ Lệch tài liệu: code THẬT chưa gửi thông báo cho người gửi phản hồi khi có trả lời
        // (FeedbackResponseServiceImpl không phụ thuộc NotificationPort) ⇒ bước 3 của tài liệu
        // ("1 notification tới parent") chưa kiểm được, cần thay đổi sản phẩm.
    }

    @Test
    @org.junit.jupiter.api.DisplayName("MF10-TC-INT-006 — trả lời trễ >48h cho đánh giá tiêu cực được ĐÁNH DẤU "
            + "lateResponse=true nhưng KHÔNG bị chặn (UC-163 EX2)")
    void negativeRatingRespondedAfter48HoursIsFlaggedLate() {
        Feedback fb = feedback(2, Instant.now().minus(java.time.Duration.ofHours(50)));
        when(feedbackRepository.findBySchoolIdAndCodeAndDeletedAtIsNull(SCHOOL_ID, "FB-2026-0001"))
                .thenReturn(Optional.of(fb));

        FeedbackResponseView view = service.respond("FB-2026-0001", new RespondRequest("Xin lỗi vì phản hồi trễ."),
                caterer());

        // EX2 — trễ nhưng KHÔNG bị chặn, response vẫn được tạo (201 ở tầng controller).
        assertThat(view.lateResponse()).isTrue();
    }

    @Test
    void negativeRatingRespondedWithin48HoursIsNotLate() {
        Feedback fb = feedback(1, Instant.now().minus(java.time.Duration.ofHours(10)));
        when(feedbackRepository.findBySchoolIdAndCodeAndDeletedAtIsNull(SCHOOL_ID, "FB-2026-0001"))
                .thenReturn(Optional.of(fb));

        FeedbackResponseView view = service.respond("FB-2026-0001", new RespondRequest("Đã điều chỉnh."), caterer());

        assertThat(view.lateResponse()).isFalse();
    }

    @Test
    void goodRatingIsNeverLateEvenAfter48Hours() {
        // BR-143 EX2 chỉ áp cho đánh giá TIÊU CỰC (rating<3) — rating>=3 không có khái niệm trễ.
        Feedback fb = feedback(5, Instant.now().minus(java.time.Duration.ofHours(72)));
        when(feedbackRepository.findBySchoolIdAndCodeAndDeletedAtIsNull(SCHOOL_ID, "FB-2026-0001"))
                .thenReturn(Optional.of(fb));

        FeedbackResponseView view = service.respond("FB-2026-0001", new RespondRequest("Cảm ơn quý phụ huynh."),
                caterer());

        assertThat(view.lateResponse()).isFalse();
    }
}
