package com.sfms.mf10.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.domain.Feedback;
import com.sfms.mf10.domain.FeedbackScope;
import com.sfms.mf10.domain.SubmitterRole;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackWeeklyAggregate;
import com.sfms.mf10.error.FbaError;
import com.sfms.mf10.repository.FeedbackRepository;
import com.sfms.mf10.repository.FeedbackResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MF10-TC-INT-007 — {@code GET /feedbacks/dashboard} (UC-164, BR-141/145): tổng hợp tới mức LỚP,
 * tuyệt đối không lộ danh tính học sinh/phụ huynh.
 *
 * <p>⚠️ Lệch tài liệu: {@code WeeklyFeedbackDigestJob} (BR-141 — gửi digest hằng tuần cho School
 * Administration và Service Caterers) CHƯA tồn tại trong {@code com.sfms.mf10} — bước 6 của tài
 * liệu không kiểm được và được tách sang phần "cần thay đổi sản phẩm".
 */
class FeedbackDashboardServiceImplTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();

    private FeedbackRepository feedbackRepository;
    private FeedbackResponseRepository responseRepository;
    private FeedbackDashboardServiceImpl service;

    private static AuthUser schoolAdmin() {
        return new AuthUser(UUID.randomUUID(), "U-9902", "admin@sfms.vn", RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);
    }

    private static AuthUser caterer() {
        return new AuthUser(UUID.randomUUID(), "U-9911", "caterer@sfms.vn", RoleKey.SERVICE_CATERERS, SCHOOL_ID, null);
    }

    private static Feedback feedback(String code, String className, int rating, boolean illness, List<String> tags) {
        Feedback fb = new Feedback();
        fb.setId(UUID.randomUUID());
        fb.setCode(code);
        fb.setSchoolId(SCHOOL_ID);
        fb.setSubmitterUserId(UUID.randomUUID());
        fb.setSubmitterRole(SubmitterRole.PARENT);
        fb.setScope(FeedbackScope.DISH);
        fb.setStudentId(UUID.randomUUID());
        fb.setClassName(className);
        fb.setMealDate(LocalDate.of(2026, 6, 30));
        fb.setMealType(MealType.LUNCH);
        fb.setRating(rating);
        fb.setTags(tags);
        fb.setComment("Con kể món hơi nguội");
        fb.setIllnessKeyword(illness);
        fb.setSubmittedAt(Instant.parse("2026-06-30T05:00:00Z"));
        return fb;
    }

    @BeforeEach
    void setUp() {
        feedbackRepository = mock(FeedbackRepository.class);
        responseRepository = mock(FeedbackResponseRepository.class);
        service = new FeedbackDashboardServiceImpl(feedbackRepository, responseRepository);
    }

    @Test
    @DisplayName("MF10-TC-INT-007 — dashboard tổng hợp đủ 7 nhóm số liệu và KHÔNG chứa bất kỳ định danh học "
            + "sinh nào khi serialize toàn bộ response")
    void dashboardAggregatesWithoutLeakingAnyStudentIdentity() throws Exception {
        List<Feedback> rows = List.of(
                feedback("FB-2026-0001", "3A", 2, true, List.of("Món ăn nguội")),
                feedback("FB-2026-0002", "3A", 5, false, List.of("Ngon miệng")),
                feedback("FB-2026-0003", "1B", 1, false, List.of("Món ăn nguội", "Khẩu phần ít")));
        when(feedbackRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(rows);
        when(responseRepository.existsByFeedbackId(any())).thenReturn(false);

        FeedbackWeeklyAggregate agg = service.weeklyAggregate(schoolAdmin(), LocalDate.of(2026, 6, 29));

        assertThat(agg.weekStart()).isEqualTo(LocalDate.of(2026, 6, 29));
        assertThat(agg.weekEnd()).isEqualTo(LocalDate.of(2026, 7, 5));
        assertThat(agg.totalFeedbacks()).isEqualTo(3);
        assertThat(agg.averageRating()).isEqualTo((2 + 5 + 1) / 3.0);
        assertThat(agg.ratingDistribution()).containsEntry(2, 1L).containsEntry(5, 1L).containsEntry(1, 1L);
        assertThat(agg.negativeCount()).isEqualTo(2);
        assertThat(agg.illnessFlaggedCount()).isEqualTo(1);
        assertThat(agg.topTags()).extracting("tag").contains("Món ăn nguội");
        assertThat(agg.byClass()).extracting("clazz").containsExactly("1B", "3A");

        String json = new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(agg);
        assertThat(json)
                .doesNotContain("studentId")
                .doesNotContain("\"student\"")
                .doesNotContain("HS-3A-")
                .doesNotContain("Nguyễn Minh Khang")
                .doesNotContain("submitterUserId");
    }

    @Test
    @DisplayName("MF10-TC-INT-007 — không phần tử nào của byClass mang danh tính cá nhân: chỉ (lớp, số lượng, "
            + "điểm trung bình)")
    void byClassCarriesOnlyClassLevelAggregates() {
        assertThat(com.sfms.mf10.dto.FeedbackDtos.ClassAggregate.class.getRecordComponents())
                .extracting(RecordComponent::getName)
                .containsExactly("clazz", "count", "averageRating");
        assertThat(FeedbackWeeklyAggregate.class.getRecordComponents())
                .extracting(RecordComponent::getName)
                .doesNotContain("studentId", "student", "submitterUserId", "submitterName");
    }

    @Test
    @DisplayName("MF10-TC-INT-007 — phản hồi đã thu hồi (soft delete) không được tính: truy vấn tổng hợp luôn "
            + "kèm điều kiện deletedAt IS NULL")
    void retractedFeedbackIsExcludedFromTheAggregate() throws Exception {
        // Không có dòng nào lọt qua bộ lọc deletedAt IS NULL ⇒ tổng bằng 0, không lỗi chia 0.
        when(feedbackRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of());

        FeedbackWeeklyAggregate agg = service.weeklyAggregate(schoolAdmin(), LocalDate.of(2026, 6, 29));

        assertThat(agg.totalFeedbacks()).isZero();
        assertThat(agg.averageRating()).isZero();
        assertThat(agg.respondedRate()).isZero();

        String source = java.nio.file.Files.readString(
                java.nio.file.Path.of("src/main/java/com/sfms/mf10/service/FeedbackDashboardServiceImpl.java"));
        assertThat(source).contains("cb.isNull(root.get(\"deletedAt\"))");
    }

    @Test
    @DisplayName("MF10-TC-INT-007 — dashboard là đặc quyền School Administration: Service Caterers bị chặn FBA-005")
    void dashboardIsSchoolAdminOnly() {
        assertThatThrownBy(() -> service.weeklyAggregate(caterer(), LocalDate.of(2026, 6, 29)))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.ACCESS_DENIED));
    }
}
