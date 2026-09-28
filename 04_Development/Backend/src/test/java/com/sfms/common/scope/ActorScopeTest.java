package com.sfms.common.scope;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * "Trường nào áp cho tài khoản này" — {@link ActorScope}.
 *
 * <p><b>Vì sao bộ test này tồn tại.</b> Giả định "đã đăng nhập thì có {@code schoolId}" đã làm
 * hỏng hệ thống <b>năm lần</b> ở năm module khác nhau, và <b>không lần nào test bắt được</b> —
 * cả năm đều do người dùng phát hiện khi thao tác thật:
 *
 * <ol>
 *   <li>MF06 — 403 ING-005 ở toàn bộ kho/mua hàng;</li>
 *   <li>MF11 — danh sách CAPA rỗng IM LẶNG ⇒ không đóng được sự cố;</li>
 *   <li>MF02 — màn Lịch thực đơn khoá toàn bộ ô ⇒ vai lập thực đơn không tạo được thực đơn;</li>
 *   <li>MF04 — {@code GET /partners} rỗng ⇒ bếp không thấy nhà cung cấp nào;</li>
 *   <li>MF03 — {@code GET /students} trả 0 ⇒ <b>cảnh báo dị ứng khi phục vụ mất sạch đầu
 *       vào</b> (BR-063/BR-123).</li>
 * </ol>
 *
 * Điểm chung: tài khoản đối tác đăng ký qua hợp tác chỉ mang {@code partnerId}. Bộ test này
 * khoá hành vi lại để lần thứ sáu bị bắt ở CI thay vì ở tay người dùng.
 */
class ActorScopeTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID PARTNER_ID = UUID.randomUUID();

    private static AuthUser actor(RoleKey role, UUID schoolId, UUID partnerId) {
        return new AuthUser(UUID.randomUUID(), "U-0001", "a@b.test", role, schoolId, partnerId);
    }

    @Test
    @DisplayName("Tài khoản CỦA TRƯỜNG ⇒ lấy thẳng schoolId, KHÔNG tra hợp tác")
    void schoolStaffUsesJwtSchoolId() {
        ReferenceResolver refs = mock(ReferenceResolver.class);

        UUID result = ActorScope.schoolIdOf(actor(RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null), refs);

        assertThat(result).isEqualTo(SCHOOL_ID);
        // Có sẵn khoá thì đừng đụng DB — tra thêm là một query thừa trên MỌI request.
        verify(refs, never()).activeSchoolIdForPartner(any());
    }

    @Test
    @DisplayName("Đơn vị suất ăn KHÔNG có schoolId ⇒ suy từ hợp tác đang hiệu lực")
    void catererResolvesSchoolFromActivePartnership() {
        ReferenceResolver refs = mock(ReferenceResolver.class);
        when(refs.activeSchoolIdForPartner(PARTNER_ID)).thenReturn(Optional.of(SCHOOL_ID));

        UUID result = ActorScope.schoolIdOf(actor(RoleKey.SERVICE_CATERERS, null, PARTNER_ID), refs);

        assertThat(result).isEqualTo(SCHOOL_ID);
    }

    @Test
    @DisplayName("Đơn vị suất ăn CHƯA có hợp tác nào ⇒ null (chưa ký hợp đồng thì chưa có trường)")
    void catererWithoutPartnershipYieldsNull() {
        ReferenceResolver refs = mock(ReferenceResolver.class);
        when(refs.activeSchoolIdForPartner(PARTNER_ID)).thenReturn(Optional.empty());

        UUID result = ActorScope.schoolIdOf(actor(RoleKey.SERVICE_CATERERS, null, PARTNER_ID), refs);

        assertThat(result).isNull();
    }

    /*
     * KHÔNG suy cho Nhà cung cấp: NCC phục vụ NHIỀU trường qua nhiều hợp đồng, chọn bừa một
     * trường sẽ cho họ đọc dữ liệu của trường không liên quan. Nơi gọi phải tự lọc theo
     * `partnerId`. Test này khoá lại quyết định đó — đừng "mở cho tiện".
     */
    @Test
    @DisplayName("Nhà cung cấp ⇒ null, TUYỆT ĐỐI không chọn bừa một trường")
    void supplierIsNeverScopedToASingleSchool() {
        ReferenceResolver refs = mock(ReferenceResolver.class);

        UUID result = ActorScope.schoolIdOf(actor(RoleKey.SUPPLIER, null, PARTNER_ID), refs);

        assertThat(result).isNull();
        verify(refs, never()).activeSchoolIdForPartner(any());
    }

    @Test
    @DisplayName("actor null ⇒ null, không ném NPE")
    void nullActorIsSafe() {
        assertThat(ActorScope.schoolIdOf(null, mock(ReferenceResolver.class))).isNull();
    }
}
