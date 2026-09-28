package com.sfms.mf09.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.dto.AllergyAlertDtos.AllergyAlertResponse;
import com.sfms.mf09.error.SrvError;
import com.sfms.mf09.port.MenuQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@code AllergyAlertService.list()} — UC-103, L20: cảnh báo dị ứng dẫn xuất tại chỗ (giao giữa
 * allergen ĐÃ XÁC MINH của HS và allergen của thực đơn hôm nay), response CHỈ mang cờ
 * {@code conflictWithTodayMenu}, KHÔNG mang mã MSG-051 (BR-100 ngoài phạm vi MF09).
 */
class AllergyAlertServiceTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID CLASSROOM_ID = UUID.randomUUID();
    private static final UUID GRADE_ID = UUID.randomUUID();
    private static final UUID MENU_ID = UUID.randomUUID();

    private ReferenceResolver referenceResolver;
    private MenuQueryPort menuQueryPort;
    private AllergyAlertServiceImpl service;

    private static AuthUser teacher() {
        return new AuthUser(UUID.randomUUID(), "U-0004", "trangnt@dtd.edu.vn", RoleKey.TEACHER, SCHOOL_ID, null);
    }

    private static ListQuery query(String classCode, String mealType, String date) {
        java.util.Map<String, String> filters = new java.util.HashMap<>();
        if (classCode != null) filters.put("class", classCode);
        if (mealType != null) filters.put("mealType", mealType);
        if (date != null) filters.put("date", date);
        return new ListQuery(1, 8, "", null, null, null, Map.copyOf(filters));
    }

    @BeforeEach
    void setUp() {
        referenceResolver = mock(ReferenceResolver.class);
        menuQueryPort = mock(MenuQueryPort.class);
        service = new AllergyAlertServiceImpl(referenceResolver, menuQueryPort);
    }

    @Test
    void missingRequiredFiltersIsRejected() {
        assertThatThrownBy(() -> service.list(query(null, "LUNCH", "2026-07-24"), teacher()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(SrvError.VALIDATION_FAILED));
    }

    @Test
    void classroomNotFoundIs404() {
        when(referenceResolver.classroomIdByCode("3A", SCHOOL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.list(query("3A", "LUNCH", "2026-07-24"), teacher()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(SrvError.NOT_FOUND));
    }

    @Test
    void studentWithVerifiedAllergenMatchingTodaysMenuIsFlaggedWithoutMsg051() {
        when(referenceResolver.classroomIdByCode("3A", SCHOOL_ID)).thenReturn(Optional.of(CLASSROOM_ID));
        when(referenceResolver.classroomGrade(CLASSROOM_ID)).thenReturn(GRADE_ID);
        when(menuQueryPort.publishedBlock(LocalDate.of(2026, 7, 24), GRADE_ID, MealType.LUNCH, SCHOOL_ID))
                .thenReturn(Optional.of(new MenuQueryPort.MenuBlockView(MENU_ID, "MENU-1", List.of())));
        when(menuQueryPort.allergensOfMenu(MENU_ID)).thenReturn(List.of("Tôm"));
        ReferenceResolver.StudentRef student =
                new ReferenceResolver.StudentRef(UUID.randomUUID(), "HS-3A-001", "Nguyễn Minh Khang", CLASSROOM_ID, SCHOOL_ID);
        when(referenceResolver.studentsOfClassroom(CLASSROOM_ID)).thenReturn(List.of(student));
        when(referenceResolver.verifiedAllergensOfStudent(student.id())).thenReturn(List.of("Tôm", "Cua"));

        PagedResponse<AllergyAlertResponse> response = service.list(query("3A", "LUNCH", "2026-07-24"), teacher());

        assertThat(response.items()).hasSize(1);
        AllergyAlertResponse alert = response.items().get(0);
        assertThat(alert.conflictWithTodayMenu()).isTrue();
        assertThat(alert.allergens()).containsExactly("Tôm");
        assertThat(alert.studentId()).isEqualTo("HS-3A-001");
    }

    @Test
    void studentWithNoOverlapBetweenAllergensAndMenuIsNotFlagged() {
        when(referenceResolver.classroomIdByCode("3A", SCHOOL_ID)).thenReturn(Optional.of(CLASSROOM_ID));
        when(referenceResolver.classroomGrade(CLASSROOM_ID)).thenReturn(GRADE_ID);
        when(menuQueryPort.publishedBlock(any(), any(), any(), any()))
                .thenReturn(Optional.of(new MenuQueryPort.MenuBlockView(MENU_ID, "MENU-1", List.of())));
        when(menuQueryPort.allergensOfMenu(MENU_ID)).thenReturn(List.of("Sữa bò"));
        ReferenceResolver.StudentRef student =
                new ReferenceResolver.StudentRef(UUID.randomUUID(), "HS-3A-001", "Nguyễn Minh Khang", CLASSROOM_ID, SCHOOL_ID);
        when(referenceResolver.studentsOfClassroom(CLASSROOM_ID)).thenReturn(List.of(student));
        when(referenceResolver.verifiedAllergensOfStudent(student.id())).thenReturn(List.of("Tôm"));

        PagedResponse<AllergyAlertResponse> response = service.list(query("3A", "LUNCH", "2026-07-24"), teacher());

        assertThat(response.items()).isEmpty();
        assertThat(response.total()).isZero();
    }

    @Test
    void noPublishedMenuForTheGradeYieldsEmptyAlertsNotAnError() {
        when(referenceResolver.classroomIdByCode("3A", SCHOOL_ID)).thenReturn(Optional.of(CLASSROOM_ID));
        when(referenceResolver.classroomGrade(CLASSROOM_ID)).thenReturn(GRADE_ID);
        when(menuQueryPort.publishedBlock(any(), any(), any(), any())).thenReturn(Optional.empty());

        PagedResponse<AllergyAlertResponse> response = service.list(query("3A", "LUNCH", "2026-07-24"), teacher());

        assertThat(response.items()).isEmpty();
        assertThat(response.pageCount()).isEqualTo(1);
    }
}
