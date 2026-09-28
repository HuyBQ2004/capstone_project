package com.sfms.mf10.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf10.domain.Feedback;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code Specification} cho {@code GET /feedbacks}. TDS-MF10_Backend.md §9.1 (CF-M10-8 —
 * SEARCH_FIELDS theo entity thật, không theo bảng gợi ý của kế hoạch).
 */
public final class FeedbackSpec {

    public static final List<String> SEARCH_FIELDS = List.of("comment", "dishName", "className");

    private static final Map<String, String> FIELD_MAP = Map.ofEntries(
            Map.entry("mealType", "mealType"),
            Map.entry("illnessKeyword", "illnessKeyword"),
            Map.entry("class", "className"),
            Map.entry("scope", "scope"),
            Map.entry("rating", "rating")
    );

    private FeedbackSpec() {
    }

    /**
     * @param submitterUserId khác {@code null} ⇒ chỉ bản ghi của người này (UC-046, chủ sở hữu);
     *                        {@code null} ⇒ toàn trường (School Admin/Caterers, ẩn danh — BR-145)
     */
    public static Specification<Feedback> build(ListQuery query, UUID schoolId, UUID submitterUserId) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            predicates.add(cb.equal(root.get("schoolId"), schoolId));
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (submitterUserId != null) {
                predicates.add(cb.equal(root.get("submitterUserId"), submitterUserId));
            }

            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);

            if (query.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("mealDate"), query.dateFrom()));
            }
            if (query.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("mealDate"), query.dateTo()));
            }
            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
