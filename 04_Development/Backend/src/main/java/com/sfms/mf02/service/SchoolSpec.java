package com.sfms.mf02.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf02.domain.School;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

/**
 * {@code Specification} cho {@code GET /schools} (ADR-M02-1).
 *
 * <p>SEARCH_FIELDS đã verify từ {@code SchoolRecord} (Team Plan §2b): {@code name},
 * {@code address}, {@code email} có thật. Bảng gợi ý cũ ghi thêm {@code principalName} —
 * field đó KHÔNG tồn tại; TDS ADR-M02-1 chốt dùng thêm {@code taxId} (cột có thật).
 */
public final class SchoolSpec {

    public static final List<String> SEARCH_FIELDS = List.of("name", "address", "email", "taxId");

    private static final Map<String, String> FIELD_MAP = Map.of(
            "id", "code",
            "code", "code",
            "name", "name",
            "email", "email",
            "taxId", "taxId",
            "status", "status"
    );

    private SchoolSpec() {
    }

    public static Specification<School> build(ListQuery query) {
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
