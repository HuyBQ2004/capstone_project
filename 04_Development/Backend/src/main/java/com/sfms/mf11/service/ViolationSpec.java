package com.sfms.mf11.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf11.domain.ViolationRecord;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code Specification} cho {@code GET /violations}. TDS-MF11_Backend.md §9.1/§16, BR-189.
 *
 * <p>Service Caterers = "Của đơn vị mình" — {@code caterersRepresentativeId} thuộc đơn vị JWT
 * (service resolve danh sách đại diện của đơn vị trước khi gọi, xem
 * {@code ReferenceResolver.currentCatererOf}/tương đương).
 */
public final class ViolationSpec {

    public static final List<String> SEARCH_FIELDS =
            List.of("subject", "recordedByLabel", "caterersRepresentativeLabel");

    private static final Map<String, String> FIELD_MAP = Map.ofEntries(
            Map.entry("status", "status"),
            Map.entry("severity", "severity"),
            Map.entry("resolution", "resolution"),
            Map.entry("violationType", "violationType"),
            Map.entry("incidentId", "relatedIncidentId"),
            Map.entry("selfReported", "selfReported")
    );

    private ViolationSpec() {
    }

    /** @param ownUnitRepresentativeIds khác {@code null} ⇒ giới hạn Service Caterers (Của đơn vị mình). */
    public static Specification<ViolationRecord> build(ListQuery query, UUID schoolId,
                                                         List<UUID> ownUnitRepresentativeIds) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            predicates.add(cb.equal(root.get("schoolId"), schoolId));
            if (ownUnitRepresentativeIds != null) {
                predicates.add(ownUnitRepresentativeIds.isEmpty()
                        ? cb.disjunction()
                        : root.get("caterersRepresentativeId").in(ownUnitRepresentativeIds));
            }

            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);

            if (query.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), query.dateFrom()));
            }
            if (query.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), query.dateTo()));
            }
            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
