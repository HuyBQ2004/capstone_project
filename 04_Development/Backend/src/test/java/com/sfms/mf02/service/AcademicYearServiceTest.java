package com.sfms.mf02.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf02.domain.AcademicYear;
import com.sfms.mf02.domain.AcademicYearStatus;
import com.sfms.mf02.domain.Classroom;
import com.sfms.mf02.dto.AcademicYearDtos.AcademicYearResponse;
import com.sfms.mf02.fixture.Mf02Fixtures;
import com.sfms.mf02.mapper.AcademicYearMapper;
import com.sfms.mf02.repository.AcademicYearRepository;
import com.sfms.mf02.repository.ClassroomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MF02-TC-012/014 (Unit) — BR-191, BR-193 (TestSpec-MF02 §4).
 *
 * <p>Test tầng Service dùng Mockito — bất biến DB thật (partial unique index) được xác minh
 * riêng ở Integration ({@code AcademicYearActivateIT}, ★ MF02-TC-INT-002).
 */
class AcademicYearServiceTest {

    private static AuthUser schoolAdmin() {
        return new AuthUser(UUID.randomUUID(), "U-0001", "toanpq@dtd.edu.vn",
                RoleKey.SCHOOL_ADMIN, Mf02Fixtures.SCHOOL_A_ID, null);
    }

    private record Harness(AcademicYearService service, AcademicYearRepository academicYearRepo,
                           ClassroomRepository classroomRepo) {}

    private Harness harness() {
        AcademicYearRepository academicYearRepo = mock(AcademicYearRepository.class);
        ClassroomRepository classroomRepo = mock(ClassroomRepository.class);
        AcademicYearService service = new AcademicYearService(academicYearRepo, classroomRepo,
                new AcademicYearMapperStub());
        return new Harness(service, academicYearRepo, classroomRepo);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF02-TC-012 — BR-191 ≤1 ACTIVE/trường (UC-175)
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF02-TC-012 — BR-191 một năm Active/trường")
    class Tc012 {

        @Test
        @DisplayName("Trường đã có năm ACTIVE ⇒ activate() ném SCH-080 (409), MSG-080")
        void activateWhenAlreadyActiveIsRejected() {
            Harness h = harness();
            AcademicYear draft = Mf02Fixtures.academicYear(ay -> ay.setCode("AY-2026"));
            AcademicYear currentlyActive = Mf02Fixtures.academicYear(ay -> {
                ay.setCode("AY-2025");
                ay.setStatus(AcademicYearStatus.ACTIVE);
            });

            when(h.academicYearRepo.findByCodeAndSchoolId("AY-2026", Mf02Fixtures.SCHOOL_A_ID))
                    .thenReturn(Optional.of(draft));
            when(h.academicYearRepo.findBySchoolIdAndStatusForUpdate(
                    Mf02Fixtures.SCHOOL_A_ID, AcademicYearStatus.ACTIVE))
                    .thenReturn(List.of(currentlyActive));

            assertThatThrownBy(() -> h.service.activate("AY-2026", schoolAdmin()))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SCH-080");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(api.getMessage()).isEqualTo(
                                "Cannot create Active academic year: an Active academic year already "
                                        + "exists for this school. Please close it first.");
                    });

            assertThat(draft.getStatus()).isEqualTo(AcademicYearStatus.DRAFT);
        }

        @Test
        @DisplayName("Không có năm ACTIVE nào khác ⇒ activate() thành công")
        void activateWhenNoOtherActiveSucceeds() {
            Harness h = harness();
            AcademicYear draft = Mf02Fixtures.academicYear(ay -> ay.setCode("AY-2026"));

            when(h.academicYearRepo.findByCodeAndSchoolId("AY-2026", Mf02Fixtures.SCHOOL_A_ID))
                    .thenReturn(Optional.of(draft));
            when(h.academicYearRepo.findBySchoolIdAndStatusForUpdate(
                    Mf02Fixtures.SCHOOL_A_ID, AcademicYearStatus.ACTIVE))
                    .thenReturn(List.of());

            assertThatCode(() -> h.service.activate("AY-2026", schoolAdmin())).doesNotThrowAnyException();
            assertThat(draft.getStatus()).isEqualTo(AcademicYearStatus.ACTIVE);
        }

        /*
         * Thêm 08/08/2026. BR-191 chỉ ràng buộc "≤1 năm Active", KHÔNG nói gì về khoảng ngày,
         * nên trước đó kích hoạt được cả năm ĐÃ ĐÓNG SỔ. "Năm học Active" là khoá lọc của rất
         * nhiều màn (lớp, sĩ số, thực đơn) — đặt nhầm vào năm đã qua thì cả trường đọc sai dữ
         * liệu mà không có gì báo.
         *
         * Đo thật: seed chọn nhầm năm 2025–2026 (kết thúc 31/05/2026) và bị chặn đúng.
         */
        @Test
        @DisplayName("Năm học ĐÃ KẾT THÚC ⇒ activate() bị chặn (SCH-422)")
        void activateFinishedYearIsRejected() {
            Harness h = harness();
            AcademicYear finished = Mf02Fixtures.academicYear(ay -> {
                ay.setCode("AY-2020");
                ay.setStartDate(LocalDate.now().minusYears(2));
                ay.setEndDate(LocalDate.now().minusDays(1));   // kết thúc HÔM QUA
            });

            when(h.academicYearRepo.findByCodeAndSchoolId("AY-2020", Mf02Fixtures.SCHOOL_A_ID))
                    .thenReturn(Optional.of(finished));
            when(h.academicYearRepo.findBySchoolIdAndStatusForUpdate(
                    Mf02Fixtures.SCHOOL_A_ID, AcademicYearStatus.ACTIVE))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> h.service.activate("AY-2020", schoolAdmin()))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("SCH-422"));

            assertThat(finished.getStatus()).isEqualTo(AcademicYearStatus.DRAFT);
        }

        /*
         * CỐ Ý cho phép: trường phải chuẩn bị lớp/sĩ số trước khai giảng nên kích hoạt sớm là
         * hợp lệ. Phần "chưa bắt đầu" xử lý ở tầng hiển thị (dashboard ghi rõ), KHÔNG chặn ở
         * đây. Test này khoá lại quyết định đó để người sau không siết nhầm.
         */
        @Test
        @DisplayName("Năm học CHƯA BẮT ĐẦU ⇒ vẫn activate() được (chuẩn bị trước khai giảng)")
        void activateNotYetStartedYearIsAllowed() {
            Harness h = harness();
            AcademicYear future = Mf02Fixtures.academicYear(ay -> {
                ay.setCode("AY-2030");
                ay.setStartDate(LocalDate.now().plusMonths(1));
                ay.setEndDate(LocalDate.now().plusMonths(10));
            });

            when(h.academicYearRepo.findByCodeAndSchoolId("AY-2030", Mf02Fixtures.SCHOOL_A_ID))
                    .thenReturn(Optional.of(future));
            when(h.academicYearRepo.findBySchoolIdAndStatusForUpdate(
                    Mf02Fixtures.SCHOOL_A_ID, AcademicYearStatus.ACTIVE))
                    .thenReturn(List.of());

            assertThatCode(() -> h.service.activate("AY-2030", schoolAdmin())).doesNotThrowAnyException();
            assertThat(future.getStatus()).isEqualTo(AcademicYearStatus.ACTIVE);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF02-TC-014 — BR-193 đóng năm ⇒ Classroom read-only (UC-175)
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF02-TC-014 — BR-193 đóng năm học")
    class Tc014 {

        @Test
        @DisplayName("close() ⇒ status=CLOSED và mọi Classroom của năm đó readOnly=true")
        void closeMarksAllClassroomsReadOnly() {
            Harness h = harness();
            AcademicYear active = Mf02Fixtures.academicYear(ay -> {
                ay.setCode("AY-2025");
                ay.setStatus(AcademicYearStatus.ACTIVE);
            });
            Classroom c1 = Mf02Fixtures.classroom(c -> c.setCode("3A"));
            Classroom c2 = Mf02Fixtures.classroom(c -> c.setCode("3B"));

            when(h.academicYearRepo.findByCodeAndSchoolId("AY-2025", Mf02Fixtures.SCHOOL_A_ID))
                    .thenReturn(Optional.of(active));
            when(h.classroomRepo.findByAcademicYearId(active.getId())).thenReturn(List.of(c1, c2));

            h.service.close("AY-2025", schoolAdmin());

            assertThat(active.getStatus()).isEqualTo(AcademicYearStatus.CLOSED);
            assertThat(c1.isReadOnly()).isTrue();
            assertThat(c2.isReadOnly()).isTrue();
        }
    }

    /** Stub thay MapStruct-generated mapper — test Unit không nạp Spring context. */
    private static final class AcademicYearMapperStub implements AcademicYearMapper {
        @Override
        public AcademicYearResponse toResponse(AcademicYear academicYear, long classes, long students) {
            return new AcademicYearResponse(academicYear.getCode(), academicYear.getName(),
                    academicYear.getStartDate(), academicYear.getEndDate(), academicYear.getStatus(),
                    academicYear.isCopyStructure(), classes, students);
        }
    }
}
