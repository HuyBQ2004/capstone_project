package com.sfms.mf03.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.port.ParentAccountPort;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.domain.StudentParent;
import com.sfms.mf03.dto.StudentDtos.CreateStudentRequest;
import com.sfms.mf03.mapper.StudentMapper;
import com.sfms.mf03.repository.AllergyDeclarationRepository;
import com.sfms.mf03.repository.ChronicConditionDeclarationRepository;
import com.sfms.mf03.repository.SpecialMealRequestRepository;
import com.sfms.mf03.repository.StudentParentRepository;
import com.sfms.mf03.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MF03-TC-002 — BR-030: mỗi học sinh phải có ≥1 phụ huynh (tạo &amp; gỡ liên
 * kết).
 *
 * <p>
 * Oracle: BR-030 "Mỗi học sinh phải được liên kết với ít nhất một tài khoản phụ
 * huynh."
 * · MSG-111 "Each student must be linked to at least one parent account." · TDS
 * §10 STU-030
 * = HTTP 422.
 */
class StudentServiceTest {

        private static AuthUser schoolAdmin() {
                return new AuthUser(UUID.randomUUID(), "U-0001", "admin@dtd.edu.vn",
                                RoleKey.SCHOOL_ADMIN, UUID.randomUUID(), null);
        }

        @Test
        @DisplayName("Act 1 — create({parent: null}) ⇒ ValidationException(STU-030), MSG-111, "
                        + "details chứa {field: \"parent.email\"}")
        void createWithoutParentIsRejected() {
                StudentServiceImpl service = new StudentServiceImpl(
                                mock(StudentRepository.class), mock(StudentParentRepository.class),
                                mock(AllergyDeclarationRepository.class),
                                mock(ChronicConditionDeclarationRepository.class),
                                mock(SpecialMealRequestRepository.class), mock(ParentLinkService.class),
                                mock(HealthHistoryService.class), mock(ReferenceResolver.class),
                                mock(StudentMapper.class), mock(CodeGenerator.class),
                                mock(ClassroomGuard.class), mock(StudentCodeAllocator.class));

                CreateStudentRequest request = new CreateStudentRequest(null, "Nguyễn Văn A (synthetic)",
                                LocalDate.parse("2019-01-01"), "3A", null);

                assertThatThrownBy(() -> service.create(request, schoolAdmin(), "127.0.0.1"))
                                .isInstanceOf(ApiException.class)
                                .satisfies(ex -> {
                                        ApiException api = (ApiException) ex;
                                        assertThat(api.getCode()).isEqualTo("STU-030");
                                        assertThat(api.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
                                        assertThat(api.getMessage()).isEqualTo(
                                                        "Each student must be linked to at least one parent account.");
                                        assertThat(api.getDetails()).anySatisfy(
                                                        d -> assertThat(d.field()).isEqualTo("parent.email"));
                                });
        }

        @Test
        @DisplayName("Act 2 — unlink(HS-3A-002, PH cuối cùng) ⇒ ValidationException(STU-030); "
                        + "dòng student_parents VẪN CÒN")
        void unlinkLastParentIsRejected() {
                StudentParentRepository studentParentRepo = mock(StudentParentRepository.class);
                UUID studentId = UUID.randomUUID();
                UUID parentUserId = UUID.randomUUID();
                when(studentParentRepo.countByStudentId(studentId)).thenReturn(1L); // chỉ 1 PH (FX-002)

                ParentLinkService service = new ParentLinkServiceImpl(studentParentRepo,
                                mock(ParentAccountPort.class), mock(ReferenceResolver.class));

                assertThatThrownBy(() -> service.unlink(studentId, parentUserId, schoolAdmin()))
                                .isInstanceOf(ApiException.class)
                                .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("STU-030"));
        }

        @Test
        @DisplayName("Act 3 — HS-3A-001 có 2 PH ⇒ unlink 1 PH thành công, còn 1 dòng")
        void unlinkOneOfTwoParentsSucceeds() {
                StudentParentRepository studentParentRepo = mock(StudentParentRepository.class);
                UUID studentId = UUID.randomUUID();
                UUID parentUserId = UUID.randomUUID();
                StudentParent link = new StudentParent();
                link.setStudentId(studentId);
                link.setParentUserId(parentUserId);

                when(studentParentRepo.countByStudentId(studentId)).thenReturn(2L); // 2 PH
                when(studentParentRepo.findByStudentId(studentId)).thenReturn(List.of(link));

                ParentLinkService service = new ParentLinkServiceImpl(studentParentRepo,
                                mock(ParentAccountPort.class), mock(ReferenceResolver.class));

                assertThatCode(() -> service.unlink(studentId, parentUserId, schoolAdmin())).doesNotThrowAnyException();
        }
}
