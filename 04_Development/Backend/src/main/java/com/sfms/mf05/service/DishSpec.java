package com.sfms.mf05.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf05.domain.Dish;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

/** {@code Specification} cho {@code GET /dishes}. SEARCH_FIELDS = {name, category} (TDS §9.2). */
public final class DishSpec {

    public static final List<String> SEARCH_FIELDS = List.of("name", "category");

    private static final Map<String, String> FIELD_MAP = Map.of(
            "id", "code",
            "code", "code",
            "name", "name",
            "category", "category",
            "isActive", "active"
    );

    private DishSpec() {
    }

    public static Specification<Dish> build(ListQuery query) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);
            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
