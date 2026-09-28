package com.sfms.mf04.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf04.domain.CatalogChangeRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code GET /catalog-change-requests}. PROPOSED — xem
 * {@code Proposal-Partnership-Catalog-Contract.md}, chưa có TDS §8/§9 chính thức.
 */
public final class CatalogChangeRequestSpec {

    private static final Map<String, String> FIELD_MAP = Map.of(
            "id", "code",
            "code", "code",
            "status", "status"
    );

    private CatalogChangeRequestSpec() {
    }

    public static Specification<CatalogChangeRequest> build(ListQuery query, UUID scopedPartnerId,
                                                              UUID scopedSchoolId) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();

            if (scopedPartnerId != null) {
                predicates.add(cb.equal(root.get("partnerId"), scopedPartnerId));
            }
            if (scopedSchoolId != null) {
                predicates.add(cb.equal(root.get("schoolId"), scopedSchoolId));
            }

            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
