package com.sfms.mf11.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf11.domain.CorrectiveAction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code Specification} cho {@code GET /corrective-actions}. TDS-MF11_Backend.md §9.1/§16.
 *
 * <p>Service Caterers = "Được giao" — {@code assigneeSupplierId} = đơn vị trong JWT.
 */
public final class CapaSpec {

    public static final List<String> SEARCH_FIELDS = List.of("description", "assigneeLabel", "sourceLabel");

    private static final Map<String, String> FIELD_MAP = Map.of("status", "status");

    private CapaSpec() {
    }

    /** @param assignedToPartnerId khác {@code null} ⇒ giới hạn Service Caterers (Được giao). */
    public static Specification<CorrectiveAction> build(ListQuery query, UUID schoolId, UUID assignedToPartnerId) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            predicates.add(cb.equal(root.get("schoolId"), schoolId));
            if (assignedToPartnerId != null) {
                predicates.add(cb.equal(root.get("assigneeSupplierId"), assignedToPartnerId));
            }

            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);

            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
