package com.sfms.mf04.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf04.domain.PartnershipApplication;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** {@code GET /partnership-applications} (UC-169/157) — TDS §8.3. */
public final class ApplicationSpec {

    public static final List<String> SEARCH_FIELDS = List.of("code", "companyName");

    private static final Map<String, String> FIELD_MAP = Map.of(
            "id", "code",
            "code", "code",
            "status", "status"
    );

    private ApplicationSpec() {
    }

    /**
     * @param scopedPartnerId khi khác {@code null}, ép lọc theo partner đó (NCC — "Own")
     * @param scopedSchoolId  khi khác {@code null}, ép lọc theo trường đó (School Admin — "School")
     */
    public static Specification<PartnershipApplication> build(ListQuery query, UUID scopedPartnerId,
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
