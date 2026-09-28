package com.sfms.mf09.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf09.domain.MealConsumptionRecord;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** {@code Specification} cho {@code GET /meal-records}. TDS-MF09_Backend.md §9.1 (CF-M09-6). */
public final class MealRecordSpec {

    public static final List<String> SEARCH_FIELDS = List.of("studentName", "className", "dishesLabel");

    private static final Map<String, String> FIELD_MAP = Map.ofEntries(
            Map.entry("class", "className"),
            Map.entry("consumption", "consumption"),
            Map.entry("mealType", "mealType")
    );

    private MealRecordSpec() {
    }

    public static Specification<MealConsumptionRecord> build(ListQuery query, UUID schoolId) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            predicates.add(cb.equal(root.get("schoolId"), schoolId));

            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);

            if (query.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), query.dateFrom()));
            }
            if (query.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), query.dateTo()));
            }
            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
