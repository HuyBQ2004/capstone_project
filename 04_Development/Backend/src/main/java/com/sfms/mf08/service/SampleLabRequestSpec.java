package com.sfms.mf08.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf08.domain.SampleLabRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** {@code Specification} cho {@code GET /sample-lab-requests}. TDS-MF08_Backend.md §9.1. */
public final class SampleLabRequestSpec {

    public static final List<String> SEARCH_FIELDS = List.of("labDestination", "requestedBy", "testCriteria");

    private static final Map<String, String> FIELD_MAP = Map.ofEntries(
            Map.entry("id", "code"),
            Map.entry("status", "status"),
            Map.entry("handoverType", "handoverType")
            // `incidentId` resolve mã→UUID ở tầng service — xem `FoodSampleSpec`.
    );

    private SampleLabRequestSpec() {
    }

    /** @param incidentIdFilter UUID đã resolve từ query param {@code incidentId} (mã nghiệp vụ), hoặc {@code null}. */
    public static Specification<SampleLabRequest> build(ListQuery query, UUID schoolId, UUID incidentIdFilter) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            predicates.add(cb.equal(root.get("schoolId"), schoolId));
            if (incidentIdFilter != null) {
                predicates.add(cb.equal(root.get("incidentId"), incidentIdFilter));
            }

            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);

            if (query.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("requestedAt"),
                        query.dateFrom().atStartOfDay(java.time.ZoneOffset.UTC).toInstant()));
            }
            if (query.dateTo() != null) {
                predicates.add(cb.lessThan(root.get("requestedAt"),
                        query.dateTo().plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant()));
            }
            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
