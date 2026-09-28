package com.sfms.mf03.service;

import com.sfms.common.error.ApiException;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.port.ParentAccountPort;
import com.sfms.iam.port.ParentAccountPort.ParentAccountRef;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.dto.StudentDtos.ParentInfoRequest;
import com.sfms.mf03.repository.StudentParentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF03-TC-001 — BR-201: chặn liên kết học sinh thứ 6 vào một tài khoản phụ huynh.
 *
 * <p>Oracle: BR-201 "Một tài khoản phụ huynh được liên kết tối đa 5 hồ sơ học sinh. Khi đã đạt
 * giới hạn, hệ thống chặn thao tác liên kết thêm và yêu cầu School Administration xác minh
 * trước khi xử lý ngoại lệ." · MSG-110 "This parent account is already linked to 5 students.
 * School Administration verification is required." · TDS §10 STU-201 = HTTP 409.
 */
class ParentLinkServiceTest {

    private static AuthUser schoolAdmin() {
        return new AuthUser(UUID.randomUUID(), "U-0001", "admin@dtd.edu.vn",
                RoleKey.SCHOOL_ADMIN, UUID.randomUUID(), null);
    }

    private record Harness(ParentLinkService service, StudentParentRepository studentParentRepo,
                           ParentAccountPort parentAccountPort) {}

    private Harness harness() {
        StudentParentRepository studentParentRepo = mock(StudentParentRepository.class);
        ParentAccountPort parentAccountPort = mock(ParentAccountPort.class);
        ReferenceResolver refs = mock(ReferenceResolver.class);
        ParentLinkService service = new ParentLinkServiceImpl(studentParentRepo, parentAccountPort, refs);
        return new Harness(service, studentParentRepo, parentAccountPort);
    }

    @Test
    @DisplayName("BVA ca 6 — parentUserId=U-0901 đã có 5 liên kết ⇒ STU-201/MSG-110 (409), "
            + "KHÔNG dòng student_parents nào được tạo")
    void sixthLinkIsBlocked() {
        Harness h = harness();
        UUID parentUserId = UUID.randomUUID();
        UUID newStudentId = UUID.randomUUID();

        when(h.parentAccountPort.findParentByEmail("existing.parent@example.test"))
                .thenReturn(Optional.of(new ParentAccountRef(parentUserId, "U-0901", "PH Đã Đủ 5 HS",
                        "existing.parent@example.test")));
        when(h.studentParentRepo.countByParentUserId(parentUserId)).thenReturn(5L);   // FX-003 — đã đúng 5

        ParentInfoRequest info = new ParentInfoRequest("PH Đã Đủ 5 HS", "existing.parent@example.test",
                "0912345678", "Bố");

        assertThatThrownBy(() -> h.service.linkOrCreateParent(newStudentId, info, schoolAdmin(), "127.0.0.1"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getCode()).isEqualTo("STU-201");
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(api.getMessage()).isEqualTo(
                            "This parent account is already linked to 5 students. School Administration "
                                    + "verification is required.");
                });

        verify(h.studentParentRepo, never()).save(any());
    }

    @Test
    @DisplayName("BVA ca 5 — parentUserId=U-0902 đã có 4 liên kết ⇒ liên kết thứ 5 thành công")
    void fifthLinkSucceeds() {
        Harness h = harness();
        UUID parentUserId = UUID.randomUUID();
        UUID newStudentId = UUID.randomUUID();

        when(h.parentAccountPort.findParentByEmail("parent902@example.test"))
                .thenReturn(Optional.of(new ParentAccountRef(parentUserId, "U-0902", "PH Đã Đủ 4 HS",
                        "parent902@example.test")));
        when(h.studentParentRepo.countByParentUserId(parentUserId)).thenReturn(4L);   // FX-004
        when(h.studentParentRepo.countByStudentId(newStudentId)).thenReturn(0L);

        ParentInfoRequest info = new ParentInfoRequest("PH Đã Đủ 4 HS", "parent902@example.test",
                "0912345679", "Mẹ");

        var result = h.service.linkOrCreateParent(newStudentId, info, schoolAdmin(), "127.0.0.1");

        assertThat(result.parentUserId()).isEqualTo(parentUserId);
        verify(h.studentParentRepo).save(any());
    }
}
