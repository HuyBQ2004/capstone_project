package com.sfms.mf06.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.error.IngExceptions;

import java.util.UUID;

/**
 * C2/§16 — {@code schoolId}/{@code supplierId} LUÔN đọc từ JWT, KHÔNG BAO GIỜ từ body/query.
 * Dùng chung cho mọi service MF06 để giữ MỘT điểm vào duy nhất (gom theo gợi ý §5 Red-Green
 * Tracker "gom 7 gate BR thành ReceivingGuard để SEC-010 chỉ cần 1 điểm vào" — áp rộng ra
 * toàn bộ scope check của module).
 */
public final class Mf06Scope {

    private Mf06Scope() {
    }

    /**
     * School-scoped resources: System Admin thấy All, School Admin/Service Caterers/Medical Staff
     * thấy trường mình.
     *
     * <p><b>Sửa 08/08/2026 — thêm vế đối tác ĐƠN VỊ SUẤT ĂN.</b> Đo bằng API thật: tài khoản
     * Service Caterers tạo qua đường ĐĂNG KÝ ĐỐI TÁC ({@code POST /partners/register/caterer})
     * mang claim {@code partnerId} nhưng KHÔNG có {@code schoolId} — nên bản cũ ném
     * {@code accessDenied()} và **toàn bộ MF06 trả 403 cho đúng vai được giao quản lý bếp**:
     * {@code /purchase-requests}, {@code /receiving-sessions}, {@code /deliveries},
     * {@code /inventory}, {@code /inventory/issue-slips}, {@code /inventory-transactions}.
     * Hệ quả: không mua được hàng ⇒ không có lô kho ⇒ luồng sự cố không cách ly được lô nào.
     *
     * <p>MF05/MF07/MF08/MF09/MF10 đã xử đúng ca này từ trước bằng
     * {@link ReferenceResolver#activeSchoolIdForPartner(UUID)} (12 nơi gọi) — MF06 là module
     * DUY NHẤT còn sót. Đây là áp lại tiền lệ có sẵn, không phải nới lỏng phạm vi: hàm đó chỉ
     * trả trường có hợp tác {@code APPROVED} còn hiệu lực ({@code until} chưa qua).
     *
     * <p>KHÔNG còn bản nạp chồng một tham số: mọi service MF06 đều đã inject sẵn
     * {@code ReferenceResolver}, để lại bản cũ chỉ mời gọi rơi lại đúng cái bug này.
     */
    public static UUID requireSchoolId(AuthUser actor, ReferenceResolver referenceResolver) {
        if (actor.role() == RoleKey.SYSTEM_ADMIN) {
            return null; // null = không lọc theo trường (All)
        }
        if (actor.schoolId() != null) {
            return actor.schoolId();
        }
        if (actor.role() == RoleKey.SERVICE_CATERERS && actor.partnerId() != null) {
            return referenceResolver.activeSchoolIdForPartner(actor.partnerId())
                    .orElseThrow(IngExceptions::accessDenied);
        }
        throw IngExceptions.accessDenied();
    }

    /** Own-scoped resources của Raw Food Supplier: chỉ dữ liệu partnerId của chính họ. */
    public static UUID requirePartnerId(AuthUser actor) {
        if (actor.role() == RoleKey.SYSTEM_ADMIN) {
            return null;
        }
        if (actor.partnerId() == null) {
            throw IngExceptions.accessDenied();
        }
        return actor.partnerId();
    }
}
