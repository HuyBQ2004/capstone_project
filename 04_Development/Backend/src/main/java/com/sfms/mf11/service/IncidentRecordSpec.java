package com.sfms.mf11.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf11.domain.IncidentRecordDraft;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** {@code Specification} cho {@code GET /incident-records}. TDS-MF11_Backend.md §9.1. */
public final class IncidentRecordSpec {

    /** {@code createdByLabel} của §9.1 ánh xạ về {@code createdBy} (email actor, BaseEntity/D9). */
    public static final List<String> SEARCH_FIELDS = List.of("code", "createdBy", "location");

    private static final Map<String, String> FIELD_MAP = Map.of("status", "status");

    private IncidentRecordSpec() {
    }

    public static Specification<IncidentRecordDraft> build(ListQuery query, UUID schoolId) {
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
