package com.sfms.mf11.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;

import java.util.UUID;

/**
 * Phạm vi TRƯỜNG của MF11 — cùng khuôn với {@code mf06/service/Mf06Scope}.
 *
 * <p><b>Vì sao cần:</b> tài khoản ĐƠN VỊ SUẤT ĂN tạo qua đường đăng ký đối tác
 * ({@code POST /partners/register/caterer}) mang claim {@code partnerId} nhưng KHÔNG có
 * {@code schoolId}. Đo bằng API thật 08/08/2026 với đúng tài khoản đó:
 *
 * <ul>
 *   <li>{@code GET /incidents} → 200 nhưng <b>total = 0</b> (im lặng, không phải 403)</li>
 *   <li>{@code GET /incidents/INC-…} → <b>404</b></li>
 *   <li>{@code GET /corrective-actions} → 200, <b>total = 0</b>; {@code GET …/CAPA-…} → 404</li>
 *   <li>{@code POST /corrective-actions/{code}/accept} → <b>404</b></li>
 * </ul>
 *
 * <p>Hệ quả nghiệp vụ: CAPA giao cho đơn vị suất ăn <b>không ai nhận được</b> ⇒
 * {@code closure-readiness.capaOpen} không bao giờ về 0 ⇒ <b>sự cố không đóng được</b> —
 * đúng thứ mà B1 định gỡ, nhưng chặn ở một nút khác.
 *
 * <p>Cách xử lý là tiền lệ có sẵn: MF05/07/08/09/10 (12 nơi) đã dùng
 * {@link ReferenceResolver#activeSchoolIdForPartner(UUID)}; hàm này chỉ trả trường có hợp
 * tác {@code APPROVED} còn hiệu lực, nên KHÔNG nới rộng phạm vi ai thấy gì.
 */
public final class Mf11Scope {

    private Mf11Scope() {
    }

    /**
     * Trường của actor: lấy từ JWT nếu có, nếu không thì suy từ hợp tác đang hiệu lực của
     * đối tác. Trả {@code null} khi không xác định được (giữ nguyên hành vi cũ — truy vấn
     * theo {@code schoolId = null} không khớp gì).
     */
    public static UUID schoolId(AuthUser actor, ReferenceResolver referenceResolver) {
        if (actor.schoolId() != null) {
            return actor.schoolId();
        }
        if (actor.role() == RoleKey.SERVICE_CATERERS && actor.partnerId() != null) {
            return referenceResolver.activeSchoolIdForPartner(actor.partnerId()).orElse(null);
        }
        return null;
    }
}
