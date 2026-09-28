package com.sfms.mf10.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.storage.StorageService;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.dto.FeedbackDtos.EligibleMealResponse;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackOwnerResponse;
import com.sfms.mf10.dto.FeedbackDtos.SubmitFeedbackRequest;
import com.sfms.mf10.error.FbaError;
import com.sfms.mf10.port.NotificationPort;
import com.sfms.mf10.repository.FeedbackAttachmentRepository;
import com.sfms.mf10.repository.FeedbackRepository;
import com.sfms.mf10.repository.FeedbackResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@code FeedbackServiceImpl.submit()}/{@code eligibleMeals()} — TDS-MF10 TC-004
 * ("BR-140 KHÔNG áp cho Teacher", CF-M10-3, traceability UC-086 chỉ khai BR-139) và validation
 * của {@code GET /feedbacks/eligible-meals}. Cả hai nhánh KHÔNG có test trước bản này:
 * {@code FeedbackAnnouncementFlowIT} chỉ gửi phản hồi với vai Parent, không có kịch bản Teacher
 * rating&lt;3 không kèm tag; và endpoint {@code eligible-meals} không xuất hiện trong bất kỳ
 * file test nào của MF10.
 */
class FeedbackServiceImplTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID SESSION_ID = UUID.randomUUID();
    private static final UUID CLASSROOM_ID = UUID.randomUUID();
    private static final UUID STUDENT_ID = UUID.randomUUID();

    private FeedbackRepository feedbackRepository;
    private FeedbackResponseRepository feedbackResponseRepository;
    private FeedbackAttachmentRepository attachmentRepository;
    private ReferenceResolver referenceResolver;
    private CodeGenerator codeGenerator;
    private FeedbackWindowPolicy windowPolicy;
    private IllnessKeywordDetector illnessKeywordDetector;
    private NotificationPort notificationPort;
    private StorageService storageService;
    private FeedbackServiceImpl service;

    private static AuthUser teacher(UUID userId) {
        return new AuthUser(userId, "U-9910", "teacher@sfms.vn", RoleKey.TEACHER, SCHOOL_ID, null);
    }

    private static AuthUser parent(UUID userId) {
        return new AuthUser(userId, "U-9905", "parent@sfms.vn", RoleKey.PARENT, SCHOOL_ID, null);
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
        storageService = mock(StorageService.class);
        windowPolicy = new FeedbackWindowPolicy(referenceResolver, Clock.fixed(Instant.parse("2026-07-30T02:00:00Z"),
                ZoneOffset.UTC));

        service = new FeedbackServiceImpl(feedbackRepository, feedbackResponseRepository, attachmentRepository,
                mock(FeedbackAttachmentSupport.class), referenceResolver, codeGenerator, windowPolicy,
                illnessKeywordDetector, notificationPort, storageService);

        when(codeGenerator.next(anyString(), anyString(), eq(4))).thenReturn("FB-2026-0001");
        when(illnessKeywordDetector.detect(any())).thenReturn(false);
        when(feedbackResponseRepository.findByFeedbackIdOrderByRespondedAtAsc(any())).thenReturn(List.of());
        when(attachmentRepository.findByFeedbackIdOrderByCreatedAtAsc(any())).thenReturn(List.of());
        when(referenceResolver.distributionCode(any())).thenReturn("DSTU-2026-0001");
        when(referenceResolver.classroomCode(CLASSROOM_ID)).thenReturn("3A");
    }

    @Test
    void teacherLowRatingWithoutTagsSucceedsBecauseBR140OnlyAppliesToParent() {
        Instant servedAt = Instant.now().minus(Duration.ofHours(2));
        when(referenceResolver.distributionIdByCode("DSTU-2026-0001", SCHOOL_ID)).thenReturn(Optional.of(SESSION_ID));
        when(referenceResolver.servedAtOf(SESSION_ID)).thenReturn(Optional.of(servedAt));
        when(referenceResolver.classroomIdByCode("3A", SCHOOL_ID)).thenReturn(Optional.of(CLASSROOM_ID));
        UUID teacherId = UUID.randomUUID();
        when(referenceResolver.isHomeroomTeacher(CLASSROOM_ID, teacherId)).thenReturn(true);
        when(feedbackRepository.existsBySubmitterUserIdAndMealSessionIdAndDishIdAndStudentIdAndDeletedAtIsNull(
                any(), any(), any(), any())).thenReturn(false);

        var req = new SubmitFeedbackRequest("DSTU-2026-0001", null, "CLASS", "3A", LocalDate.of(2026, 7, 30),
                "LUNCH", null, 1, List.of(), "Cả lớp ăn hơi chậm.", null);

        // Không ném FbaExceptions.lowRatingNeedsTag() dù rating=1 và tags rỗng — vì submitterRole=TEACHER.
        FeedbackOwnerResponse response = service.submit(req, teacher(teacherId));

        assertThat(response.rating()).isEqualTo(1);
        assertThat(response.tags()).isEmpty();
    }

    @Test
    void parentLowRatingWithoutTagsIsStillRejected() {
        Instant servedAt = Instant.now().minus(Duration.ofHours(2));
        when(referenceResolver.distributionIdByCode("DSTU-2026-0002", SCHOOL_ID)).thenReturn(Optional.of(SESSION_ID));
        when(referenceResolver.servedAtOf(SESSION_ID)).thenReturn(Optional.of(servedAt));
        var studentRef = new ReferenceResolver.StudentRef(STUDENT_ID, "HS-3A-001", "Nguyễn Minh Khang", CLASSROOM_ID, SCHOOL_ID);
        when(referenceResolver.studentByCode("HS-3A-001", SCHOOL_ID)).thenReturn(Optional.of(studentRef));
        UUID parentId = UUID.randomUUID();
        when(referenceResolver.isParentLinkedToStudent(parentId, STUDENT_ID)).thenReturn(true);
        when(referenceResolver.dishIdByCode("com-trang")).thenReturn(Optional.of(UUID.randomUUID()));

        var req = new SubmitFeedbackRequest("DSTU-2026-0002", "HS-3A-001", "DISH", null, LocalDate.of(2026, 7, 30),
                "LUNCH", "com-trang", 2, List.of(), "Món hơi mặn.", null);

        assertThatThrownBy(() -> service.submit(req, parent(parentId)))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.LOW_RATING_NEEDS_TAG));
    }

    @Test
    void eligibleMealsWithBlankStudentIdIsRejected() {
        assertThatThrownBy(() -> service.eligibleMeals(parent(UUID.randomUUID()), "  "))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.VALIDATION_FAILED));
    }

    @Test
    void eligibleMealsForUnlinkedStudentIsDeniedForParent() {
        UUID parentId = UUID.randomUUID();
        var studentRef = new ReferenceResolver.StudentRef(STUDENT_ID, "HS-3A-002", "Học sinh khác", CLASSROOM_ID, SCHOOL_ID);
        when(referenceResolver.studentByCode("HS-3A-002", SCHOOL_ID)).thenReturn(Optional.of(studentRef));
        when(referenceResolver.isParentLinkedToStudent(parentId, STUDENT_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.eligibleMeals(parent(parentId), "HS-3A-002"))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.ACCESS_DENIED));
    }

    @Test
    void eligibleMealsReturnsWindowBoundariesFromServedMeals() {
        UUID parentId = UUID.randomUUID();
        var studentRef = new ReferenceResolver.StudentRef(STUDENT_ID, "HS-3A-001", "Nguyễn Minh Khang", CLASSROOM_ID, SCHOOL_ID);
        when(referenceResolver.studentByCode("HS-3A-001", SCHOOL_ID)).thenReturn(Optional.of(studentRef));
        when(referenceResolver.isParentLinkedToStudent(parentId, STUDENT_ID)).thenReturn(true);
        Instant servedAt = Instant.parse("2026-07-29T04:00:00Z");
        var meal = new ReferenceResolver.ServedMealRef(SESSION_ID, "DSTU-2026-0003", LocalDate.of(2026, 7, 29),
                "LUNCH", servedAt, UUID.randomUUID());
        when(referenceResolver.servedMealsWithin(eq(STUDENT_ID), any())).thenReturn(List.of(meal));

        List<EligibleMealResponse> result = service.eligibleMeals(parent(parentId), "HS-3A-001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).closesAt()).isEqualTo(servedAt.plus(FeedbackWindowPolicy.WINDOW));
    }
}
