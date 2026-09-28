package com.sfms.mf05.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf05.domain.Menu;
import com.sfms.mf05.error.MnuExceptions;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code Specification} cho {@code GET /menus} (ADR-M05-01).
 *
 * <p>SEARCH_FIELDS chốt 2026-08-03 (TDS §9.2, TDS §2.5 mục 5): {@code {name, grade,
 * week_label}} — KHÔNG {@code createdBy} (đã thành nhãn dẫn xuất từ {@code partners.name},
 * không còn là cột của {@code menus}, TDS §2.5 mục 5).
 *
 * <p>{@code schoolId} KHÔNG có trong {@link #FIELD_MAP} theo THIẾT KẾ (TestSpec TC-SEC-006) —
 * {@code schoolId} luôn lấy từ JWT, KHÔNG BAO GIỜ từ query param.
 */
public final class MenuSpec {

    public static final List<String> SEARCH_FIELDS = List.of("name", "weekLabel");

    private static final Map<String, String> FIELD_MAP = Map.ofEntries(
            Map.entry("id", "code"),
            Map.entry("code", "code"),
            Map.entry("name", "name"),
            Map.entry("gradeId", "gradeId"),
            Map.entry("status", "status"),
            Map.entry("audience", "audience"),
            Map.entry("mealType", "mealType"),
            Map.entry("week", "weekLabel"),
            Map.entry("weekLabel", "weekLabel")
    );

    private MenuSpec() {
    }

    public static Specification<Menu> build(ListQuery query, UUID schoolId) {
        // TC-SEC-013 — allow-list: field lạ (không thuộc FIELD_MAP/CONTROL_KEYS) => 400,
        // KHÔNG âm thầm bỏ qua (khác hành vi mặc định của SpecSupport dùng chung).
        for (String key : query.filters().keySet()) {
            String jsonField = key.startsWith("not:") ? key.substring(4) : key;
            if (!FIELD_MAP.containsKey(jsonField)) {
                throw MnuExceptions.validationFailed(jsonField, "unknown filter field");
            }
        }
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            predicates.add(cb.equal(root.get("schoolId"), schoolId));

            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);

            if (query.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("menuDate"), query.dateFrom()));
            }
            if (query.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("menuDate"), query.dateTo()));
            }
            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
