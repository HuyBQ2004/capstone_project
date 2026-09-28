package com.sfms.mf04.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf04.domain.SupplierPartnership;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** {@code GET /supplier-partnerships} (UC-155/159) — TDS §8.3, §9.2 (bổ sung 2026-07-29). */
public final class PartnershipSpec {

    public static final List<String> SEARCH_FIELDS = List.of("deliveryAddress");

    private static final Map<String, String> FIELD_MAP = Map.of(
            "id", "code",
            "code", "code",
            "status", "status"
    );

    private PartnershipSpec() {
    }

    public static Specification<SupplierPartnership> build(ListQuery query, UUID scopedPartnerId,
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

            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
