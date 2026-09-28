package com.sfms.mf04.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf04.domain.Partner;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

/** {@code GET /partners} (UC-131/127/148/108) — TDS §8.3, §17.1 C1. */
public final class PartnerSpec {

    public static final List<String> SEARCH_FIELDS = List.of("name", "taxId", "address", "contact");

    private static final Map<String, String> FIELD_MAP = Map.of(
            "id", "code",
            "code", "code",
            "type", "type",
            "status", "status"
    );

    private PartnerSpec() {
    }

    public static Specification<Partner> build(ListQuery query) {
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
