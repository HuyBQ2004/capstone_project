package com.sfms.mf03.mapper;

import com.sfms.mf03.domain.Student;
import com.sfms.mf03.domain.StudentStatus;
import com.sfms.mf03.dto.StudentDtos.ParentSummary;
import com.sfms.mf03.dto.StudentDtos.StudentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MF03-TC-008 — ADR-M03-3 / CF-M03-11: {@code specialDiet} = SMR APPROVED mới nhất.
 *
 * <p>Oracle: ADR-M03-3 + CF-M03-11 ({@code Student.specialDiet} @deprecated, nguồn duy nhất là
 * {@code SpecialMealRequest} APPROVED gần nhất) · {@code src/lib/health-docs.ts}
 * {@code latestApprovedMeal()}.
 *
 * <p><b>Lệch giữa TestSpec và code thật (CRITICAL RULE — code là nguồn sự thật):</b> TestSpec đặt
 * "Feature Under Test" là {@code StudentMapper.toResponse()} và giả định việc chọn bản APPROVED
 * <i>mới nhất</i> là logic của mapper. Đọc {@code StudentMapper} thật (MapStruct, chỉ có 1
 * interface pass-through — không có method nào tính toán) và {@code StudentServiceImpl.toResponse()}
 * (private) thì việc chọn "APPROVED mới nhất" nằm ở TẦNG REPOSITORY:
 * {@code SpecialMealRequestRepository.findFirstByStudentIdAndStatusOrderByRequestedAtDesc(id,
 * APPROVED)} — một derived query {@code ORDER BY requested_at DESC LIMIT 1}, ép bằng
 * {@code Pageable} (xem Javadoc của method đó — né {@code NonUniqueResultException} thật đã gặp
 * lúc chạy Testcontainers). {@code StudentMapper} chỉ NHẬN {@code specialDiet} đã được service tính
 * sẵn làm tham số và gắn vào {@code StudentResponse} — không tự chọn gì cả.
 *
 * <p>Vì vậy lớp test này chỉ unit-test đúng phần THẬT của {@code StudentMapper}: (a) mapper gắn
 * đúng giá trị {@code specialDiet} đã tính sẵn vào response (pass-through, không đổi/không tính
 * lại), và (b) {@code Student} entity KHÔNG có field {@code specialDiet} (CF-M03-11 — dẫn xuất,
 * không lưu cột riêng). Phần "chọn bản APPROVED mới nhất, bỏ qua PENDING" (bước 1-3 của TestSpec)
 * là hành vi của {@code SpecialMealRequestRepository} + DB, được kiểm bằng integration test thật
 * qua HTTP ở {@code web.StudentSpecialDietIT} (cùng TC ID) — không giả lập được ở tầng unit vì phụ
 * thuộc {@code ORDER BY} của PostgreSQL.
 */
class StudentMapperTest {

    private final StudentMapper mapper = Mappers.getMapper(StudentMapper.class);

    private Student baselineStudent() {
        Student s = new Student();
        s.setId(UUID.randomUUID());
        s.setCode("HS-3A-001");
        s.setRegistrationNo("DTD-2026-0001");
        s.setName("Nguyễn Minh Khang (synthetic)");
        s.setDob(LocalDate.parse("2018-03-12"));
        s.setSchoolId(UUID.randomUUID());
        s.setClassroomId(UUID.randomUUID());
        s.setStatus(StudentStatus.ACTIVE);
        return s;
    }

    @Test
    @DisplayName("1 — mapper GẮN đúng specialDiet đã tính sẵn (bản APPROVED mới nhất do service/"
            + "repository chọn) vào response, KHÔNG tự tính lại")
    void mapperPassesThroughAlreadyComputedSpecialDiet() {
        Student student = baselineStudent();

        StudentResponse response = mapper.toResponse(student, "3A", "3A",
                List.of(), "Gluten-Free", "VERIFIED", null, "Nguyễn Văn Bình (synthetic)", "U-9905",
                List.of(), "no", "yes", null);

        assertThat(response.specialDiet()).isEqualTo("Gluten-Free");
        assertThat(response.hasSpecialDiet()).isEqualTo("yes");
    }

    @Test
    @DisplayName("2 — HS chỉ có SMR PENDING (không có APPROVED nào): service tính specialDiet=null "
            + "=> mapper GIỮ NGUYÊN null, hasSpecialDiet=\"no\" (PENDING KHÔNG tính)")
    void mapperKeepsNullSpecialDietWhenOnlyPendingRequestExists() {
        Student student = baselineStudent();

        StudentResponse response = mapper.toResponse(student, "3A", "3A",
                List.of(), null, "PENDING", null, "Nguyễn Văn Bình (synthetic)", "U-9905",
                List.of(), "no", "no", null);

        assertThat(response.specialDiet()).isNull();
        assertThat(response.hasSpecialDiet()).isEqualTo("no");
    }

    @Test
    @DisplayName("3 — HS không có SMR nào: specialDiet=null, hasSpecialDiet=\"no\" (đối xứng ca 2 — "
            + "mapper không phân biệt được \"không SMR\" với \"chỉ PENDING\", đúng vì cả hai đều "
            + "phải cho cùng 1 kết quả ở tầng response)")
    void mapperKeepsNullSpecialDietWhenNoRequestExists() {
        Student student = baselineStudent();

        StudentResponse response = mapper.toResponse(student, "3A", "3A",
                List.of(), null, "VERIFIED", null, "Nguyễn Văn Bình (synthetic)", "U-9905",
                List.of(), "no", "no", null);

        assertThat(response.specialDiet()).isNull();
        assertThat(response.hasSpecialDiet()).isEqualTo("no");
    }

    @Test
    @DisplayName("4 — Assert schema: Student entity KHÔNG có field specialDiet (CF-M03-11 — dẫn "
            + "xuất, không phải cột lưu)")
    void studentEntityHasNoSpecialDietField() {
        assertThat(java.util.Arrays.stream(Student.class.getDeclaredFields()))
                .noneMatch(f -> f.getName().equals("specialDiet"));
    }

    @Test
    @DisplayName("5 — mapper map đúng danh sách phụ huynh (parents) và ParentSummary — pass-through "
            + "không đổi thứ tự/nội dung")
    void mapperPassesThroughParentsUnchanged() {
        Student student = baselineStudent();
        List<ParentSummary> parents = List.of(
                new ParentSummary("U-9905", "Nguyễn Văn Bình (synthetic)", "Bố", true));

        StudentResponse response = mapper.toResponse(student, "3A", "3A",
                List.of(), null, "VERIFIED", null, "Nguyễn Văn Bình (synthetic)", "U-9905",
                parents, "no", "no", null);

        assertThat(response.parents()).hasSize(1);
        assertThat(response.parents().get(0).parentId()).isEqualTo("U-9905");
        assertThat(response.parents().get(0).isPrimary()).isTrue();
    }
}
