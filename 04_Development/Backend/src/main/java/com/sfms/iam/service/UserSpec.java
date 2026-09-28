package com.sfms.iam.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.iam.domain.UserAccount;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code Specification} cho {@code GET /users} và {@code GET /staff}.
 *
 * <p><b>SEARCH_FIELDS đã verify</b> (Team Plan §2b, bản sửa 30/07/2026): MF01 {@code users}
 * ⇒ {@code name}, {@code email}, {@code phone}. Bảng cũ ghi {@code username}/{@code fullName}
 * — hai field đó <b>không tồn tại</b>, sinh SQL trên cột không có ⇒ lỗi runtime.
 *
 * <p>MF01-TC-INT-011 assert: user có {@code school = "TH Đoàn Thị Điểm"} <b>không</b> khớp
 * {@code search=Điểm}. Nếu khớp nghĩa là đang OR toàn bộ cột ⇒ full table scan (§2b-3 bước 5).
 */
public final class UserSpec {

    /** ⚠️ Đúng 3 cột. Thêm cột vào đây là quyết định về hiệu năng, cần Mạnh review. */
    public static final List<String> SEARCH_FIELDS = List.of("name", "email", "phone");

    /**
     * Ánh xạ tên field JSON → thuộc tính JPA.
     *
     * <p>D1: field JSON {@code id} trỏ cột {@code code}. D6: {@code role} trỏ
     * {@code role.roleKey} (kebab-case) chứ không phải {@code role.name} (nhãn hiển thị) —
     * MF01-TC-INT-011 mục (k) lọc {@code ?role=teacher}.
     */
    private static final Map<String, String> FIELD_MAP = Map.of(
            "id", "code",
            "code", "code",
            "name", "name",
            "email", "email",
            "phone", "phone",
            "status", "status",
            "role", "role.roleKey"
    );

    private UserSpec() {
    }

    /**
     * {@code GET /users} (UC-153) — chỉ hàng {@code account_scope = PLATFORM}.
     *
     * <p>Lời mời nhân viên chưa thành tài khoản nền tảng (VD {@code STF-009}) chỉ xuất hiện
     * ở {@code GET /staff}. Xem ghi chú hai-không-gian-mã trong {@code V1__init_schema.sql}.
     */
    public static Specification<UserAccount> buildPlatform(ListQuery query) {
        return (root, cq, cb) -> cb.and(
                cb.equal(root.get("accountScope"), UserAccount.SCOPE_PLATFORM),
                build(query, null).toPredicate(root, cq, cb));
    }

    /**
     * {@code GET /staff} (UC-144) — chỉ hàng có {@code staff_code}, trong phạm vi trường của JWT.
     */
    public static Specification<UserAccount> buildStaff(ListQuery query, UUID scopedSchoolId) {
        return (root, cq, cb) -> cb.and(
                cb.isNotNull(root.get("staffCode")),
                build(query, scopedSchoolId).toPredicate(root, cq, cb));
    }

    public static Specification<UserAccount> build(ListQuery query) {
        return build(query, null);
    }

    /**
     * @param scopedSchoolId khi khác {@code null}, ép lọc theo trường này và <b>bỏ qua</b> mọi
     *                       {@code schoolId} client gửi. Đây là cách MF01-TC-SEC-006 bước 2
     *                       được thỏa: {@code ?schoolId=SCH-HN-0042} từ JWT trường B không
     *                       đổi được kết quả — {@code schoolId} là CONTROL từ JWT.
     */
    public static Specification<UserAccount> build(ListQuery query, UUID scopedSchoolId) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();

            // ① Cách ly tenant — áp TRƯỚC mọi thứ, không thể ghi đè từ query string.
            if (scopedSchoolId != null) {
                predicates.add(cb.equal(root.get("schoolId"), scopedSchoolId));
            }

            // ② §2b-3 bước 1 — lọc exact / phủ định `not:<field>`
            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);

            // ③ §2b-3 bước 5 — tìm mờ, áp CUỐI, chỉ trên SEARCH_FIELDS
            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }

            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
