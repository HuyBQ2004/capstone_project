package com.sfms.common.code;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * D1 — sinh mã nghiệp vụ có khóa.
 *
 * <p>Dùng {@code INSERT ... ON CONFLICT DO UPDATE ... RETURNING} thay vì đọc-rồi-ghi
 * (ADR-M11-5): hai request song song đọc cùng {@code next_value} sẽ sinh <b>trùng mã</b>,
 * và UNIQUE constraint biến nó thành lỗi 500 ngẫu nhiên rất khó tái hiện.
 *
 * <p>{@code REQUIRES_NEW}: bộ đếm tiến lên kể cả khi transaction nghiệp vụ rollback.
 * Bỏ vài mã trong dãy là chấp nhận được; cấp trùng mã thì không.
 */
@Component
public class CodeGenerator {

    @PersistenceContext
    private EntityManager em;

    /**
     * @param scope  khóa bộ đếm: {@code "users"}, {@code "staff"}, {@code "roles"}…
     * @param prefix tiền tố mã: {@code "U"}, {@code "STF"}, {@code "ROLE"}
     * @param width  số chữ số zero-pad — {@code 4} ⇒ {@code U-0011}, {@code 2} ⇒ {@code ROLE-09}
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String next(String scope, String prefix, int width) {
        Number value = (Number) em.createNativeQuery("""
                INSERT INTO code_sequences (scope, next_value)
                VALUES (:scope, 2)
                ON CONFLICT (scope) DO UPDATE SET next_value = code_sequences.next_value + 1
                RETURNING next_value - 1
                """)
                .setParameter("scope", scope)
                .getSingleResult();
        return String.format("%s-%0" + width + "d", prefix, value.longValue());
    }

    /** Khuôn mặc định của tài khoản: {@code U-0011} (4 chữ số, khớp seed mock). */
    public String nextUserCode() {
        return next("users", "U", 4);
    }

    /** Khuôn nhân viên trường: {@code STF-012}. */
    public String nextStaffCode() {
        return next("staff", "STF", 3);
    }

    /** Khuôn vai trò tùy chỉnh: {@code ROLE-09}. */
    public String nextRoleCode() {
        return next("roles", "ROLE", 2);
    }

    /**
     * Khuôn trường học khi client không gửi {@code id} (UC-070, D5): {@code SCH-0043}.
     * Seed/khai báo tay có thể dùng khuôn có địa danh ({@code SCH-HN-0042}) — đây chỉ là
     * fallback tự sinh, không ép các mã đã tồn tại về khuôn mới.
     */
    public String nextSchoolCode() {
        return next("schools", "SCH", 4);
    }
}
