package com.sfms.mf10.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.NotificationType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.domain.NotificationAudience;
import com.sfms.mf10.domain.NotificationPreference;
import com.sfms.mf10.domain.NotificationRead;
import com.sfms.mf10.domain.UserNotification;
import com.sfms.mf10.dto.NotificationDtos.NotificationPreferenceResponse;
import com.sfms.mf10.dto.NotificationDtos.NotificationResponse;
import com.sfms.mf10.dto.NotificationDtos.UpdatePreferenceRequest;
import com.sfms.mf10.error.FbaError;
import com.sfms.mf10.repository.NotificationPreferenceRepository;
import com.sfms.mf10.repository.NotificationReadRepository;
import com.sfms.mf10.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF10 — chuông thông báo & cài đặt thông báo ({@code NotificationServiceImpl}).
 *
 * <p>Phủ MF10-TC-INT-008 (hợp đồng phân trang §2b cho {@code /notifications}),
 * MF10-TC-INT-013 (trạng thái đã đọc theo TỪNG người, bảng {@code notification_reads}) và
 * MF10-TC-INT-014 (mặc định BR-158 + 3 loại CRITICAL không tắt được).
 */
class NotificationServiceImplTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID MEDICAL_ID = UUID.randomUUID();
    private static final UUID ADMIN_ID = UUID.randomUUID();

    private NotificationRepository notificationRepository;
    private NotificationReadRepository readRepository;
    private NotificationPreferenceRepository preferenceRepository;
    private NotificationServiceImpl service;

    private static AuthUser medicalStaff() {
        return new AuthUser(MEDICAL_ID, "U-9903", "yte@sfms.vn", RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
    }

    private static AuthUser schoolAdmin() {
        return new AuthUser(ADMIN_ID, "U-9902", "admin@sfms.vn", RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);
    }

    private static AuthUser caterer() {
        return new AuthUser(UUID.randomUUID(), "U-9911", "caterer@sfms.vn", RoleKey.SERVICE_CATERERS, SCHOOL_ID, null);
    }

    private static UserNotification notification(String code, NotificationType type, List<String> roles) {
        UserNotification n = new UserNotification();
        n.setId(UUID.randomUUID());
        n.setCode(code);
        n.setSchoolId(SCHOOL_ID);
        n.setAudience(roles == null ? NotificationAudience.ALL : NotificationAudience.ROLES);
        n.setRecipientRoles(roles);
        n.setType(type);
        n.setTitle("Tiêu đề " + code);
        n.setContent("Nội dung " + code);
        n.setSentAt(Instant.parse("2026-07-30T01:00:00Z"));
        return n;
    }

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        readRepository = mock(NotificationReadRepository.class);
        preferenceRepository = mock(NotificationPreferenceRepository.class);
        service = new NotificationServiceImpl(notificationRepository, readRepository, preferenceRepository,
                mock(ReferenceResolver.class));
    }

    private List<UserNotification> seedMany(int count) {
        List<UserNotification> all = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            all.add(notification(String.format("NTF-2026-%06d", i), NotificationType.MENU_PUBLISHED,
                    List.of(RoleKey.MEDICAL_STAFF.key())));
        }
        when(notificationRepository.findVisibleToSchool(SCHOOL_ID)).thenReturn(all);
        when(readRepository.findByUserIdAndNotificationIdIn(any(), anySet())).thenReturn(List.of());
        return all;
    }

    // ═══════════════════════ MF10-TC-INT-008 ═══════════════════════

    @Test
    @DisplayName("MF10-TC-INT-008 — GET /notifications trả ĐÚNG 5 khóa của hợp đồng §2b-1, không wrapper")
    void pagedResponseHasExactlyTheFiveContractFields() {
        assertThat(PagedResponse.class.getRecordComponents())
                .extracting(RecordComponent::getName)
                .containsExactly("items", "total", "page", "pageSize", "pageCount");

        seedMany(30);
        PagedResponse<NotificationResponse> page =
                service.listForCurrentUser(ListQuery.from(Map.of("page", "1", "pageSize", "8")), medicalStaff());

        assertThat(page.items()).hasSize(8);
        assertThat(page.total()).isEqualTo(30);
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.pageSize()).isEqualTo(8);
        assertThat(page.pageCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("MF10-TC-INT-008 — page vượt pageCount bị CLAMP về trang cuối và vẫn có dữ liệu (không rỗng)")
    void pageBeyondLastIsClampedToTheLastPage() {
        seedMany(30);

        PagedResponse<NotificationResponse> page =
                service.listForCurrentUser(ListQuery.from(Map.of("page", "99", "pageSize", "8")), medicalStaff());

        assertThat(page.page()).isEqualTo(4);
        assertThat(page.pageCount()).isEqualTo(4);
        assertThat(page.items()).hasSize(6).isNotEmpty();
    }

    @Test
    @DisplayName("MF10-TC-INT-008 — tập rỗng trả 200 với {items:[],total:0,page:1,pageCount:1}, KHÔNG 404")
    void emptyResultIsAValidPageNotAnError() {
        when(notificationRepository.findVisibleToSchool(SCHOOL_ID)).thenReturn(List.of());
        when(readRepository.findByUserIdAndNotificationIdIn(any(), anySet())).thenReturn(List.of());

        PagedResponse<NotificationResponse> page =
                service.listForCurrentUser(ListQuery.from(Map.of("page", "1", "pageSize", "8")), medicalStaff());

        assertThat(page.items()).isEmpty();
        assertThat(page.total()).isZero();
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.pageCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("MF10-TC-INT-008 — pageSize=0 và pageSize=-5 bị clamp về 1; giá trị query rỗng bị bỏ qua")
    void pageSizeIsClampedAndEmptyQueryValuesAreIgnored() {
        seedMany(3);

        assertThat(service.listForCurrentUser(ListQuery.from(Map.of("pageSize", "0")), medicalStaff()).pageSize())
                .isEqualTo(1);
        assertThat(service.listForCurrentUser(ListQuery.from(Map.of("pageSize", "-5")), medicalStaff()).pageSize())
                .isEqualTo(1);

        // §2b-6 — `?search=`/`?rating=` rỗng KHÔNG được biến thành bộ lọc.
        ListQuery empties = ListQuery.from(Map.of("search", "", "rating", "", "pageSize", "8"));
        assertThat(empties.hasSearch()).isFalse();
        assertThat(empties.filters()).isEmpty();
        assertThat(service.listForCurrentUser(empties, medicalStaff()).total()).isEqualTo(3);
    }

    // ═══════════════════════ MF10-TC-INT-013 ═══════════════════════

    @Test
    @DisplayName("MF10-TC-INT-013 — đánh dấu đã đọc CHỈ ảnh hưởng người bấm: ghi 1 dòng notification_reads "
            + "mang userId của actor, người nhận khác vẫn thấy read=false")
    void markReadIsPerRecipientNotGlobal() {
        UserNotification ntf = notification("NTF-2026-000900", NotificationType.ALLERGY_ALERT,
                List.of(RoleKey.MEDICAL_STAFF.key(), RoleKey.SCHOOL_ADMIN.key()));
        when(notificationRepository.findByCode("NTF-2026-000900")).thenReturn(Optional.of(ntf));
        when(notificationRepository.findVisibleToSchool(SCHOOL_ID)).thenReturn(List.of(ntf));
        when(readRepository.existsByNotificationIdAndUserId(ntf.getId(), MEDICAL_ID)).thenReturn(false);

        service.markRead("NTF-2026-000900", medicalStaff());

        ArgumentCaptor<NotificationRead> saved = ArgumentCaptor.forClass(NotificationRead.class);
        verify(readRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getUserId()).isEqualTo(MEDICAL_ID);
        assertThat(saved.getValue().getNotificationId()).isEqualTo(ntf.getId());
        assertThat(saved.getValue().getReadAt()).isNotNull();

        // Y tế đã đọc ⇒ read=true; School Admin (cùng người nhận) KHÔNG có dòng đọc ⇒ vẫn false. ⭐
        NotificationRead readRow = new NotificationRead();
        readRow.setNotificationId(ntf.getId());
        readRow.setUserId(MEDICAL_ID);
        when(readRepository.findByUserIdAndNotificationIdIn(eq(MEDICAL_ID), anySet())).thenReturn(List.of(readRow));
        when(readRepository.findByUserIdAndNotificationIdIn(eq(ADMIN_ID), anySet())).thenReturn(List.of());

        assertThat(service.listForCurrentUser(ListQuery.from(Map.of()), medicalStaff()).items().get(0).read()).isTrue();
        assertThat(service.listForCurrentUser(ListQuery.from(Map.of()), schoolAdmin()).items().get(0).read()).isFalse();
    }

    @Test
    @DisplayName("MF10-TC-INT-013 — gọi PATCH read lần 2 là idempotent: không ghi thêm dòng nào (uq_ntfread)")
    void markReadIsIdempotent() {
        UserNotification ntf = notification("NTF-2026-000900", NotificationType.ALLERGY_ALERT,
                List.of(RoleKey.MEDICAL_STAFF.key()));
        when(notificationRepository.findByCode("NTF-2026-000900")).thenReturn(Optional.of(ntf));
        when(readRepository.existsByNotificationIdAndUserId(ntf.getId(), MEDICAL_ID)).thenReturn(true);

        service.markRead("NTF-2026-000900", medicalStaff());

        verify(readRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("MF10-TC-INT-013 — người KHÔNG thuộc nhóm nhận không thấy thông báo và không đánh dấu đọc được "
            + "(FBA-005); thông báo audience=ALL thì mọi vai đều thấy")
    void nonRecipientIsExcludedWhileAudienceAllIsVisibleToEveryRole() {
        UserNotification roleScoped = notification("NTF-2026-000900", NotificationType.ALLERGY_ALERT,
                List.of(RoleKey.MEDICAL_STAFF.key(), RoleKey.SCHOOL_ADMIN.key()));
        UserNotification toAll = notification("NTF-2026-000895", NotificationType.MENU_PUBLISHED, null);
        when(notificationRepository.findByCode("NTF-2026-000900")).thenReturn(Optional.of(roleScoped));
        when(notificationRepository.findVisibleToSchool(SCHOOL_ID)).thenReturn(List.of(roleScoped, toAll));
        when(readRepository.findByUserIdAndNotificationIdIn(any(), anySet())).thenReturn(List.of());

        assertThatThrownBy(() -> service.markRead("NTF-2026-000900", caterer()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.ACCESS_DENIED));
        verify(readRepository, never()).saveAndFlush(any());

        List<NotificationResponse> catererItems =
                service.listForCurrentUser(ListQuery.from(Map.of()), caterer()).items();
        assertThat(catererItems).extracting(NotificationResponse::id).containsExactly("NTF-2026-000895");
        assertThat(catererItems.get(0).audience()).isEqualTo(NotificationAudience.ALL.name());
    }

    @Test
    @DisplayName("MF10-TC-INT-013 — trạng thái đọc nằm ở bảng notification_reads, notifications KHÔNG có cột read")
    void readStateLivesInItsOwnTableNotAsAColumn() {
        assertThat(UserNotification.class.getDeclaredFields())
                .extracting(java.lang.reflect.Field::getName)
                .doesNotContain("read", "isRead", "readAt");
        assertThat(NotificationRead.class.getDeclaredFields())
                .extracting(java.lang.reflect.Field::getName)
                .contains("notificationId", "userId", "readAt");
    }

    // ═══════════════════════ MF10-TC-INT-014 ═══════════════════════

    @Test
    @DisplayName("MF10-TC-INT-014 — GET /notification-preferences/me khi CHƯA có bản ghi trả mặc định BR-158 "
            + "(22:00–06:00, emailFallback=true) chứ không 404")
    void missingPreferenceReturnsBr158Defaults() {
        when(preferenceRepository.findById(MEDICAL_ID)).thenReturn(Optional.empty());

        NotificationPreferenceResponse pref = service.getMyPreference(medicalStaff());

        assertThat(pref.quietStart()).isEqualTo(LocalTime.of(22, 0));
        assertThat(pref.quietEnd()).isEqualTo(LocalTime.of(6, 0));
        assertThat(pref.emailFallback()).isTrue();
        assertThat(pref.criticalTypes()).containsExactly("INCIDENT_ALERT", "FOOD_RECALL", "ALLERGY_ALERT");
        // ⚠️ Lệch tài liệu: mặc định KHÔNG phải "mọi loại = true" — EXPIRY_ALERT mặc định TẮT
        // (NotificationServiceImpl.defaultTypeEnabled), các loại còn lại bật.
        assertThat(pref.typeEnabled()).containsEntry("EXPIRY_ALERT", false);
        assertThat(pref.typeEnabled()).containsEntry("MENU_PUBLISHED", true);
    }

    @Test
    @DisplayName("MF10-TC-INT-014 — PUT lưu XUỐNG DB (không giữ trong bộ nhớ như mock FE): giá trị vừa ghi "
            + "được đọc lại nguyên vẹn, gồm cả quiet hours")
    void updatedPreferenceIsPersistedAndReadBack() {
        when(preferenceRepository.findById(MEDICAL_ID)).thenReturn(Optional.empty());

        NotificationPreferenceResponse saved = service.updateMyPreference(new UpdatePreferenceRequest(
                Map.of("EXPIRY_ALERT", false), false, LocalTime.of(21, 0), LocalTime.of(7, 0)), medicalStaff());

        ArgumentCaptor<NotificationPreference> persisted = ArgumentCaptor.forClass(NotificationPreference.class);
        verify(preferenceRepository).saveAndFlush(persisted.capture());
        assertThat(persisted.getValue().getUserId()).isEqualTo(MEDICAL_ID);
        assertThat(persisted.getValue().getQuietStart()).isEqualTo(LocalTime.of(21, 0));
        assertThat(persisted.getValue().getQuietEnd()).isEqualTo(LocalTime.of(7, 0));
        assertThat(persisted.getValue().isEmailFallback()).isFalse();
        assertThat(saved.quietStart()).isEqualTo(LocalTime.of(21, 0));
        assertThat(saved.emailFallback()).isFalse();

        // Lần đọc sau (bản ghi đã tồn tại trong DB) trả đúng giá trị đã lưu.
        when(preferenceRepository.findById(MEDICAL_ID)).thenReturn(Optional.of(persisted.getValue()));
        assertThat(service.getMyPreference(medicalStaff()).quietStart()).isEqualTo(LocalTime.of(21, 0));
    }

    @Test
    @DisplayName("MF10-TC-INT-014 — người dùng tắt FOOD_RECALL vẫn được chấp nhận nhưng 3 loại CRITICAL luôn "
            + "được liệt kê là không-tắt-được (BR-138 đè cấu hình cá nhân)")
    void criticalTypesRemainCriticalEvenWhenUserDisablesThem() {
        NotificationPreference existing = new NotificationPreference();
        existing.setUserId(MEDICAL_ID);
        existing.setTypeEnabled(new java.util.LinkedHashMap<>(Map.of("FOOD_RECALL", true)));
        when(preferenceRepository.findById(MEDICAL_ID)).thenReturn(Optional.of(existing));

        NotificationPreferenceResponse after = service.updateMyPreference(
                new UpdatePreferenceRequest(Map.of("FOOD_RECALL", false), null, null, null), medicalStaff());

        assertThat(after.typeEnabled()).containsEntry("FOOD_RECALL", false);
        assertThat(after.criticalTypes()).containsExactly("INCIDENT_ALERT", "FOOD_RECALL", "ALLERGY_ALERT");
        // Cờ critical là DẪN XUẤT từ type, không đọc từ cấu hình cá nhân ⇒ vẫn true.
        assertThat(notification("NTF-2026-000901", NotificationType.FOOD_RECALL,
                List.of(RoleKey.PARENT.key())).isCritical()).isTrue();
        // ⚠️ Lệch tài liệu: pipeline gửi (quiet hours/retry/email fallback, BR-158/159) CHƯA hiện
        // thực ở MVP (NotificationPortImpl chỉ ghi bảng notifications) ⇒ bước "vẫn gửi lúc 23:30"
        // không kiểm được ở đây; case pipeline giao nhận được ghi là CẦN THAY ĐỔI SẢN PHẨM.
    }

    @Test
    @DisplayName("MF10-TC-INT-014 — quietStart/quietEnd là LocalTime nên chuỗi '25:00' bị chặn ngay ở tầng "
            + "deserialize (không có đường nào lưu giờ không hợp lệ xuống DB)")
    void invalidQuietHourStringCannotReachTheService() {
        RecordComponent quietStart = java.util.Arrays.stream(UpdatePreferenceRequest.class.getRecordComponents())
                .filter(c -> c.getName().equals("quietStart")).findFirst().orElseThrow();
        assertThat(quietStart.getType()).isEqualTo(LocalTime.class);

        assertThatThrownBy(() -> LocalTime.parse("25:00")).isInstanceOf(java.time.format.DateTimeParseException.class);
    }

    @Test
    @DisplayName("MF10-TC-INT-008 — danh sách thông báo chỉ đọc theo schoolId của JWT (thông báo toàn nền tảng "
            + "school_id IS NULL do truy vấn findVisibleToSchool gộp sẵn)")
    void listIsScopedToTheJwtSchool() {
        seedMany(2);

        service.listForCurrentUser(ListQuery.from(Map.of()), medicalStaff());

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(notificationRepository).findVisibleToSchool(captor.capture());
        assertThat(captor.getValue()).isEqualTo(SCHOOL_ID);
    }
}
