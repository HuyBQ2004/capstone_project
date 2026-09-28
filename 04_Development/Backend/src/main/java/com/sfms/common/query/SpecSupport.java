package com.sfms.common.query;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Bộ dựng {@code Specification} dùng chung — dịch <b>thứ tự áp bộ lọc §2b-3</b>:
 *
 * <pre>
 * 1. Lọc chính xác theo field (exact / not:field)   ← áp TRƯỚC để thu nhỏ tập
 * 2. Lọc khoảng ngày (dateFrom / dateTo)
 * 5. Tìm mờ search trên SEARCH_FIELDS               ← áp CUỐI, KHÔNG phải mọi cột
 * </pre>
 *
 * <p><b>Tại sao search KHÔNG chạy trên mọi cột String</b> (§2b cảnh báo): mock tìm trên
 * {@code Object.values(r)} vì in-memory; PostgreSQL với {@code OR ILIKE %q%} trên 10–20 cột
 * = <b>full table scan</b>, không dùng được index. Mỗi module khai {@code SEARCH_FIELDS}
 * cố định 3–6 cột.
 */
public final class SpecSupport {

    private SpecSupport() {
    }

    /**
     * Bước 1 của §2b-3 — lọc exact và phủ định {@code not:<field>}.
     *
     * @param jsonToColumn ánh xạ tên field JSON → tên thuộc tính JPA
     *                     (D1: field JSON {@code id} → cột {@code code})
     */
    public static void applyExactFilters(Root<?> root, CriteriaBuilder cb,
                                         Map<String, String> filters,
                                         Map<String, String> jsonToColumn,
                                         List<Predicate> out) {
        for (Map.Entry<String, String> e : filters.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            if (value == null || value.isBlank()) {
                continue;
            }
            boolean negated = key.startsWith("not:");
            String jsonField = negated ? key.substring(4) : key;
            String attribute = jsonToColumn.get(jsonField);
            if (attribute == null) {
                // Field không thuộc whitelist của module ⇒ bỏ qua, KHÔNG ném lỗi
                // (mock cũng bỏ qua; ném ở đây làm vỡ mọi link FE có param lạ).
                continue;
            }
            Path<?> path = resolve(root, attribute);
            // V44 — cột FK UUID thật (VD grade_id) KHÔNG thể so bằng path.as(String.class):
            // Postgres không có operator uuid = character varying, và Hibernate không tự
            // CAST ở đây (khác các cột VARCHAR/enum @Enumerated(STRING) vẫn hoạt động bình
            // thường qua nhánh cast-String bên dưới). So trực tiếp kiểu UUID thay vì ép String.
            if (path.getJavaType() == UUID.class) {
                UUID uuidValue;
                try {
                    uuidValue = UUID.fromString(value);
                } catch (IllegalArgumentException ex) {
                    // Giá trị lọc không phải UUID hợp lệ ⇒ không khớp bản ghi nào (KHÔNG ném
                    // 500 vì lỗi cú pháp SQL) — nhất quán với "field lạ" (bỏ qua âm thầm) ở
                    // trên, nhưng field này hợp lệ, chỉ giá trị sai định dạng.
                    out.add(negated ? cb.conjunction() : cb.disjunction());
                    continue;
                }
                @SuppressWarnings("unchecked")
                Path<Object> typed = (Path<Object>) path;
                out.add(negated ? cb.notEqual(typed, uuidValue) : cb.equal(typed, uuidValue));
                continue;
            }
            Expression<String> asString = path.as(String.class);
            out.add(negated ? cb.notEqual(asString, value) : cb.equal(asString, value));
        }
    }

    /**
     * Bước 5 của §2b-3 — tìm mờ case-insensitive trên đúng {@code SEARCH_FIELDS}.
     *
     * <p>{@code %} và {@code _} trong input được <b>escape</b> (MF01-TC-SEC-014 mục d):
     * {@code search=%} phải là ký tự thường, không phải wildcard trả toàn bộ bảng.
     */
    public static Predicate searchPredicate(Root<?> root, CriteriaBuilder cb,
                                            String rawSearch, List<String> searchFields) {
        String escaped = escapeLike(rawSearch.toLowerCase());
        String pattern = "%" + escaped + "%";
        Predicate[] ors = searchFields.stream()
                .map(f -> cb.like(cb.lower(resolve(root, f).as(String.class)), pattern, '\\'))
                .toArray(Predicate[]::new);
        return cb.or(ors);
    }

    /** Escape ký tự đặc biệt của LIKE. Dấu {@code \} phải escape TRƯỚC {@code %}/{@code _}. */
    public static String escapeLike(String input) {
        return input.replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
    }

    /** Hỗ trợ đường dẫn lồng {@code "role.name"}. */
    private static Path<?> resolve(Root<?> root, String attribute) {
        Path<?> path = root;
        for (String part : attribute.split("\\.")) {
            path = path.get(part);
        }
        return path;
    }

    public static List<Predicate> newPredicateList() {
        return new ArrayList<>();
    }
}
