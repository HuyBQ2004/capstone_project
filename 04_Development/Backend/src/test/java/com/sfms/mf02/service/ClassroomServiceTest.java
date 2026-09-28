package com.sfms.mf02.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.reference.ReferenceResolver.TeacherRef;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf02.domain.AcademicYearStatus;
import com.sfms.mf02.domain.Classroom;
import com.sfms.mf02.dto.ClassroomDtos.AssignHomeroomTeacherRequest;
import com.sfms.mf02.dto.ClassroomDtos.ClassroomDetailResponse;
import com.sfms.mf02.dto.ClassroomDtos.ClassroomResponse;
import com.sfms.mf02.dto.ClassroomDtos.CreateClassroomRequest;
import com.sfms.mf02.fixture.Mf02Fixtures;
import com.sfms.mf02.mapper.ClassroomMapper;
import com.sfms.mf02.repository.AcademicYearRepository;
import com.sfms.mf02.repository.ClassroomRepository;
import com.sfms.mf02.repository.GradeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MF02-TC-001..006 (Unit) — BR-020, BR-024, BR-025, BR-192 (TestSpec-MF02 §4).
 *
 * <p>
 * Oracle: {@code TDS-MF02_Backend.md} §10 (SCH-010/011/024/081), §13
 * TC-UNIT-001/002.
 */
class ClassroomServiceTest {

        private static AuthUser schoolAdmin() {
                return new AuthUser(UUID.randomUUID(), "U-0001", "toanpq@dtd.edu.vn",
                                RoleKey.SCHOOL_ADMIN, Mf02Fixtures.SCHOOL_A_ID, null);
        }

        private record Harness(ClassroomService service, ClassroomRepository classroomRepo,
                        AcademicYearRepository academicYearRepo, GradeRepository gradeRepo,
                        ReferenceResolver referenceResolver) {
        }

        private Harness harness() {
                ClassroomRepository classroomRepo = mock(ClassroomRepository.class);
                AcademicYearRepository academicYearRepo = mock(AcademicYearRepository.class);
                GradeRepository gradeRepo = mock(GradeRepository.class);
                ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
                ClassroomService service = new ClassroomService(classroomRepo, academicYearRepo, gradeRepo,
                                new ClassroomMapperStub(), referenceResolver);
                // V44 — grade hợp lệ mặc định (Khối 3, active, cùng trường); tests BR-024 override riêng.
                when(gradeRepo.findById(Mf02Fixtures.GRADE_3_ID))
                                .thenReturn(Optional.of(Mf02Fixtures.grade(g -> {
                                })));
                return new Harness(service, classroomRepo, academicYearRepo, gradeRepo, referenceResolver);
        }

        // ═══════════════════════════════════════════════════════════════════════
        // MF02-TC-001 — BR-024 grade ∉ [1,12] (UC-018/090)
        // ═══════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("MF02-TC-001 — BR-024 validate grade")
        class Tc001 {

                @Test
                @DisplayName("create(gradeId=không tồn tại) ⇒ SCH-024 (422), MSG-109")
                void unknownGradeRejected() {
                        Harness h = harness();
                        UUID unknownGradeId = UUID.randomUUID();
                        CreateClassroomRequest req = new CreateClassroomRequest(null, "3C",
                                        unknownGradeId.toString(), 40, "AY-2025");

                        assertThatThrownBy(() -> h.service.create(req, schoolAdmin()))
                                        .isInstanceOf(ApiException.class)
                                        .satisfies(ex -> {
                                                ApiException api = (ApiException) ex;
                                                assertThat(api.getCode()).isEqualTo("SCH-024");
                                                assertThat(api.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
                                        });
                }

                @Test
                @DisplayName("create(gradeId=khối trường khác) ⇒ SCH-024 (422)")
                void gradeFromAnotherSchoolRejected() {
                        Harness h = harness();
                        UUID otherSchoolGradeId = UUID.randomUUID();
                        when(h.gradeRepo.findById(otherSchoolGradeId)).thenReturn(Optional.of(
                                        Mf02Fixtures.grade(g -> {
                                                g.setId(otherSchoolGradeId);
                                                g.setSchoolId(Mf02Fixtures.SCHOOL_B_ID);
                                        })));
                        CreateClassroomRequest req = new CreateClassroomRequest(null, "3C",
                                        otherSchoolGradeId.toString(), 40, "AY-2025");

                        assertThatThrownBy(() -> h.service.create(req, schoolAdmin()))
                                        .satisfies(ex -> assertThat(((ApiException) ex).getCode())
                                                        .isEqualTo("SCH-024"));
                }
        }

        // ═══════════════════════════════════════════════════════════════════════
        // MF02-TC-002 — BR-025 capacity > 50 (UC-018/090)
        // ═══════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("MF02-TC-002 — BR-025 validate capacity")
        class Tc002 {

                @Test
                @DisplayName("create(capacity=51) ⇒ SCH-010 (409), MSG-010")
                void capacityAboveRangeRejected() {
                        Harness h = harness();
                        CreateClassroomRequest req = new CreateClassroomRequest(null, "3D",
                                        Mf02Fixtures.GRADE_3_ID.toString(), 51, "AY-2025");

                        assertThatThrownBy(() -> h.service.create(req, schoolAdmin()))
                                        .isInstanceOf(ApiException.class)
                                        .satisfies(ex -> {
                                                ApiException api = (ApiException) ex;
                                                assertThat(api.getCode()).isEqualTo("SCH-010");
                                                assertThat(api.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                                                assertThat(api.getMessage()).isEqualTo(
                                                                "Classroom capacity exceeded. Maximum allowed is 50 students.");
                                        });
                }
        }

        // ═══════════════════════════════════════════════════════════════════════
        // MF02-TC-004 / MF02-TC-INT-004 — BR-192 chặn tạo lớp khi chưa có năm Active
        // (UC-018)
        // ═══════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("MF02-TC-004 — BR-192 chặn tạo lớp khi chưa có năm Active")
        class Tc004 {

                @Test
                @DisplayName("Trường không có AcademicYear ACTIVE ⇒ SCH-081 (409), MSG-081")
                void noActiveAcademicYearBlocksCreate() {
                        Harness h = harness();
                        when(h.academicYearRepo.findBySchoolIdAndStatus(Mf02Fixtures.SCHOOL_A_ID,
                                        AcademicYearStatus.ACTIVE)).thenReturn(List.of());

                        CreateClassroomRequest req = new CreateClassroomRequest(null, "3C",
                                        Mf02Fixtures.GRADE_3_ID.toString(), 40, "AY-2026");

                        assertThatThrownBy(() -> h.service.create(req, schoolAdmin()))
                                        .isInstanceOf(ApiException.class)
                                        .satisfies(ex -> {
                                                ApiException api = (ApiException) ex;
                                                assertThat(api.getCode()).isEqualTo("SCH-081");
                                                assertThat(api.getMessage()).isEqualTo(
                                                                "Cannot create classroom: this school has no Active academic year. "
                                                                                + "Please activate an academic year first.");
                                        });
                }
        }

        // ═══════════════════════════════════════════════════════════════════════
        // MF02-TC-005 / MF02-TC-005b — BR-020 1 GVCN/lớp/năm (UC-008)
        // ═══════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("MF02-TC-005 — BR-020 GVCN chỉ chủ nhiệm 1 lớp/năm học")
        class Tc005 {

                @Test
                @DisplayName("Gán GV đang chủ nhiệm lớp khác (4A) cho 3B ⇒ SCH-011 (409)")
                void teacherAlreadyAssignedToAnotherClassroomRejected() {
                        Harness h = harness();
                        UUID teacherId = UUID.randomUUID();
                        UUID ayId = Mf02Fixtures.AY_2025_ID;

                        Classroom target = Mf02Fixtures.classroom(c -> {
                                c.setId(UUID.randomUUID());
                                c.setCode("3B");
                                c.setName("3B");
                        });
                        Classroom other = Mf02Fixtures.classroom(c -> {
                                c.setId(UUID.randomUUID());
                                c.setCode("4A");
                                c.setName("4A");
                                c.setHomeroomTeacherId(teacherId);
                        });

                        when(h.classroomRepo.findBySchoolIdAndCode(Mf02Fixtures.SCHOOL_A_ID, "3B"))
                                        .thenReturn(List.of(target));
                        when(h.academicYearRepo.findById(ayId))
                                        .thenReturn(Optional.of(Mf02Fixtures
                                                        .academicYear(ay -> ay.setStatus(AcademicYearStatus.ACTIVE))));
                        when(h.referenceResolver.teacherByCode("STF-003")).thenReturn(Optional.of(
                                        new TeacherRef(teacherId, "U-0008", "STF-003", "Đào Văn Quân", "teacher",
                                                        "ACTIVE", Mf02Fixtures.SCHOOL_A_ID)));
                        when(h.classroomRepo.findByAcademicYearIdAndHomeroomTeacherId(ayId, teacherId))
                                        .thenReturn(Optional.of(other));

                        AssignHomeroomTeacherRequest req = new AssignHomeroomTeacherRequest("STF-003");

                        assertThatThrownBy(() -> h.service.assignHomeroomTeacher("3B", req.teacherId(), schoolAdmin()))
                                        .isInstanceOf(ApiException.class)
                                        .satisfies(ex -> {
                                                ApiException api = (ApiException) ex;
                                                assertThat(api.getCode()).isEqualTo("SCH-011");
                                                assertThat(api.getMessage()).isEqualTo(
                                                                "Homeroom teacher is already assigned to another classroom.");
                                        });
                }

                @Test
                @DisplayName("Gán GV rảnh cho 3A (đã có GVCN khác) ⇒ thành công, teacherId đổi")
                void reassignFreeTeacherSucceeds() {
                        Harness h = harness();
                        UUID newTeacherId = UUID.randomUUID();
                        UUID ayId = Mf02Fixtures.AY_2025_ID;
                        Classroom classroom = Mf02Fixtures.classroom(c -> c.setHomeroomTeacherId(UUID.randomUUID()));

                        when(h.classroomRepo.findBySchoolIdAndCode(Mf02Fixtures.SCHOOL_A_ID, "3A"))
                                        .thenReturn(List.of(classroom));
                        when(h.academicYearRepo.findById(ayId))
                                        .thenReturn(Optional.of(Mf02Fixtures
                                                        .academicYear(ay -> ay.setStatus(AcademicYearStatus.ACTIVE))));
                        when(h.referenceResolver.teacherByCode("STF-004")).thenReturn(Optional.of(
                                        new TeacherRef(newTeacherId, "U-0010", "STF-004", "Lưu Thị Bích", "teacher",
                                                        "ACTIVE", Mf02Fixtures.SCHOOL_A_ID)));
                        when(h.classroomRepo.findByAcademicYearIdAndHomeroomTeacherId(ayId, newTeacherId))
                                        .thenReturn(Optional.empty());

                        assertThatCode(() -> h.service.assignHomeroomTeacher("3A", "STF-004", schoolAdmin()))
                                        .doesNotThrowAnyException();

                        assertThat(classroom.getHomeroomTeacherId()).isEqualTo(newTeacherId);
                }

                @Test
                @DisplayName("teacherId resolve ra role Medical Staff (không phải Teacher) ⇒ SCH-404 (404)")
                void wrongRoleRejectedAsNotFound() {
                        Harness h = harness();
                        UUID ayId = Mf02Fixtures.AY_2025_ID;
                        Classroom classroom = Mf02Fixtures.classroom(c -> {
                        });

                        when(h.classroomRepo.findBySchoolIdAndCode(Mf02Fixtures.SCHOOL_A_ID, "3A"))
                                        .thenReturn(List.of(classroom));
                        when(h.academicYearRepo.findById(ayId))
                                        .thenReturn(Optional.of(Mf02Fixtures
                                                        .academicYear(ay -> ay.setStatus(AcademicYearStatus.ACTIVE))));
                        when(h.referenceResolver.teacherByCode("STF-006")).thenReturn(Optional.of(
                                        new TeacherRef(UUID.randomUUID(), "U-0012", "STF-006", "Bùi Thị Mai",
                                                        "medical-staff", "ACTIVE", Mf02Fixtures.SCHOOL_A_ID)));

                        assertThatThrownBy(() -> h.service.assignHomeroomTeacher("3A", "STF-006", schoolAdmin()))
                                        .isInstanceOf(ApiException.class)
                                        .satisfies(ex -> {
                                                ApiException api = (ApiException) ex;
                                                assertThat(api.getCode()).isEqualTo("SCH-404");
                                                assertThat(api.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                                        });
                }
        }

        /**
         * Stub thay MapStruct-generated mapper — test Unit không nạp Spring context.
         */
        private static final class ClassroomMapperStub implements ClassroomMapper {
                @Override
                public ClassroomResponse toResponse(Classroom classroom, String gradeName,
                                String academicYearCode, long students, long allergies, String teacherName,
                                String teacherPublicId) {
                        return new ClassroomResponse(classroom.getCode(), classroom.getName(), gradeName,
                                        classroom.getCapacity(), academicYearCode, students, allergies, teacherName,
                                        teacherPublicId, classroom.isReadOnly());
                }

                @Override
                public ClassroomDetailResponse toDetailResponse(Classroom classroom,
                                String gradeName, String academicYearCode, long students, long allergies,
                                String teacherName, String teacherPublicId, String schoolCode) {
                        return new ClassroomDetailResponse(classroom.getCode(), classroom.getName(),
                                        gradeName,
                                        classroom.getCapacity(), academicYearCode, students, allergies, teacherName,
                                        teacherPublicId, classroom.isReadOnly(), schoolCode);
                }
        }
}
