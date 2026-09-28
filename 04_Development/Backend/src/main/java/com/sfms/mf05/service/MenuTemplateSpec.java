package com.sfms.mf05.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf05.domain.MenuTemplate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** {@code Specification} cho {@code GET /menu-templates}. SEARCH_FIELDS = {name} (TDS §9.2). */
public final class MenuTemplateSpec {

    public static final List<String> SEARCH_FIELDS = List.of("name");

    private static final Map<String, String> FIELD_MAP = Map.of(
            "id", "code",
            "code", "code",
            "name", "name",
            "mealType", "mealType",
            "audience", "audience",
            "gradeId", "gradeId",
            "isActive", "active"
    );

    private MenuTemplateSpec() {
    }

    public static Specification<MenuTemplate> build(ListQuery query, UUID schoolId) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            predicates.add(cb.equal(root.get("schoolId"), schoolId));
            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);
            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
