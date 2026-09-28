package com.sfms.mf11.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf11.domain.IncidentSourceReport;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code Specification} cho {@code GET /source-reports}. TDS-MF11_Backend.md §9.1.
 *
 * <p>{@code SEARCH_FIELDS} = {@code reportedByLabel, className, suspectedMealText, description}
 * (khác bảng gợi ý cũ của kế hoạch §2b-3 — CF-M11-8, đã verify trực tiếp từ §9.1).
 */
public final class SourceReportSpec {

    public static final List<String> SEARCH_FIELDS =
            List.of("reportedByLabel", "className", "suspectedMealText", "description");

    private static final Map<String, String> FIELD_MAP = Map.ofEntries(
            Map.entry("status", "status"),
            Map.entry("className", "className"),
            Map.entry("severityLevel", "severityLevel")
    );

    private SourceReportSpec() {
    }

    /**
     * @param restrictClassNames khác {@code null} ⇒ chỉ trả RPT của các lớp này (Teacher, ADR-M11-10);
     *                           {@code null} = không giới hạn (Medical Staff/School Admin).
     */
    public static Specification<IncidentSourceReport> build(ListQuery query, UUID schoolId,
                                                              List<String> restrictClassNames) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            predicates.add(cb.equal(root.get("schoolId"), schoolId));
            if (restrictClassNames != null) {
                predicates.add(restrictClassNames.isEmpty()
                        ? cb.disjunction()
                        : root.get("className").in(restrictClassNames));
            }

            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);

            if (query.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("reportedAt").as(java.time.Instant.class),
                        query.dateFrom().atStartOfDay(java.time.ZoneOffset.UTC).toInstant()));
            }
            if (query.dateTo() != null) {
                predicates.add(cb.lessThan(root.get("reportedAt").as(java.time.Instant.class),
                        query.dateTo().plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant()));
            }
            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
