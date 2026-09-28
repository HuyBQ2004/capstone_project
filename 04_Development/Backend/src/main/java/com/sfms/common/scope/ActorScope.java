package com.sfms.common.scope;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;

import java.util.UUID;

/**
 * "Trường nào áp cho tài khoản này" — MỘT chỗ duy nhất, dùng cho mọi module.
 *
 * <h3>Vì sao class này tồn tại</h3>
 * Tài khoản <b>đối tác</b> (Đơn vị suất ăn / Nhà cung cấp) đăng ký qua luồng hợp tác nên JWT
 * chỉ mang {@code partnerId}; {@code AuthUser.schoolId()} của họ <b>LUÔN null</b>. Mọi đoạn
 * code giả định "đã đăng nhập thì có schoolId" đều hỏng với đúng vai vận hành bếp — và điều
 * đó đã xảy ra <b>bốn lần</b>, mỗi lần ở một module khác nhau, mỗi lần đều do người dùng phát
 * hiện chứ không phải test bắt được:
 *
 * <ol>
 *   <li>MF06 — 403 ING-005 ở toàn bộ kho/mua hàng (vá bằng {@code Mf06Scope});</li>
 *   <li>MF11 — danh sách CAPA/sự cố rỗng IM LẶNG ⇒ không đóng được sự cố (vá bằng
 *       {@code Mf11Scope});</li>
 *   <li>MF02 {@code SchoolConfigService} + FE {@code useSchoolConfig} — màn Lịch thực đơn khoá
 *       toàn bộ ô ⇒ vai lập thực đơn không tạo được thực đơn (vá bằng
 *       {@code ProfileResponse.activeSchoolId});</li>
 *   <li>MF04 {@code PartnerServiceImpl} — {@code GET /partners} trả rỗng ⇒ màn "Nhà cung cấp &
 *       Sản phẩm" của Đơn vị suất ăn không thấy nhà cung cấp nào, dù trường có hợp tác đang
 *       hiệu lực (phát hiện 08/08/2026 khi rà toàn bộ màn hình).</li>
 * </ol>
 *
 * Ba lần đầu mỗi lần vá một kiểu riêng. Class này là chỗ để lần thứ năm không phải vá lại:
 * <b>viết code đọc dữ liệu theo trường thì gọi {@link #schoolIdOf} thay vì
 * {@code actor.schoolId()}.</b>
 *
 * <p><b>Chỉ suy cho SERVICE_CATERERS</b> — BR-EXCLUSIVE-CATERER cho quan hệ 1-1 trường ↔ đơn vị
 * suất ăn nên "trường của tôi" là xác định. Nhà cung cấp phục vụ NHIỀU trường ⇒ trả {@code null};
 * nơi gọi phải tự quyết (thường là lọc theo {@code partnerId} thay vì theo trường), tuyệt đối
 * KHÔNG chọn bừa một trường cho họ.
 *
 * <p>{@code Mf06Scope}/{@code Mf11Scope} hiện vẫn tự cài lại logic này (khác nhau ở chỗ ném lỗi
 * hay trả null). Nên cho chúng ủy quyền về đây khi có dịp đụng tới — chưa gộp trong đợt này để
 * không đổi hành vi của hai module đang chạy ổn.
 */
public final class ActorScope {

    private ActorScope() {
    }

    /**
     * @return trường áp cho {@code actor}: {@code schoolId} của JWT nếu có; với Đơn vị suất ăn
     *         thì suy từ hợp tác đang hiệu lực; còn lại {@code null}.
     */
    public static UUID schoolIdOf(AuthUser actor, ReferenceResolver referenceResolver) {
        if (actor == null) {
            return null;
        }
        if (actor.schoolId() != null) {
            return actor.schoolId();
        }
        if (actor.role() == RoleKey.SERVICE_CATERERS && actor.partnerId() != null) {
            return referenceResolver.activeSchoolIdForPartner(actor.partnerId()).orElse(null);
        }
        return null;
    }
}
