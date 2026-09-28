package com.sfms.mf04.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf04.domain.DeliveryPerson;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code GET /delivery-persons} (UC-110) — TDS §8.3.
 *
 * <p>⚠️ KHÔNG search trên {@code nationalId*} — đã mã hóa/hash, không tìm mờ được (ADR-M04-5).
 */
public final class DeliveryPersonSpec {

    public static final List<String> SEARCH_FIELDS = List.of("fullName", "vehiclePlate", "phone");

    private static final Map<String, String> FIELD_MAP = Map.of(
            "id", "code",
            "code", "code",
            "biometricStatus", "biometricStatus"
    );

    private DeliveryPersonSpec() {
    }

    public static Specification<DeliveryPerson> build(ListQuery query, List<UUID> allowedPartnerIds) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();

            if (allowedPartnerIds != null) {
                if (allowedPartnerIds.isEmpty()) {
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(root.get("partnerId").in(allowedPartnerIds));
                }
            }

            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);

            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
