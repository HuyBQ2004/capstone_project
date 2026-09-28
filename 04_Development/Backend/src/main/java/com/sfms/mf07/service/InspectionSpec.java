package com.sfms.mf07.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf07.domain.InspectionSession;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code Specification} cho {@code GET /inspections}.
 *
 * <p>⚠️ Simplification biết trước: TDS §9.2 khai {@code SEARCH_FIELDS = {inspector, dish,
 * staffNotes}}, nhưng {@code inspector}/{@code dish} là nhãn DẪN XUẤT (join {@code
 * user_accounts}/{@code dishes} qua {@link com.sfms.common.reference.ReferenceResolver}, không
 * phải cột của {@code inspection_sessions}) — Criteria API không join thẳng qua đó được mà
 * không nạp entity của module khác (ADR-M02-5). Bước 2 chỉ tìm mờ trên {@code staffNotes};
 * mở rộng {@code inspector}/{@code dish} là việc còn treo, ghi vào TDS §2.5 khi merge.
 */
public final class InspectionSpec {

    public static final List<String> SEARCH_FIELDS = List.of("staffNotes");

    private static final Map<String, String> FIELD_MAP = Map.ofEntries(
            Map.entry("id", "code"),
            Map.entry("overall", "overall"),
            Map.entry("step1", "step1"),
            Map.entry("step2", "step2"),
            Map.entry("step3", "step3"),
            Map.entry("meal", "mealType"),
            Map.entry("mealType", "mealType")
    );

    private InspectionSpec() {
    }

    public static Specification<InspectionSession> build(ListQuery query, UUID schoolId, UUID scopePartnerId) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            if (schoolId != null) {
                // System Admin (schoolId=null trong JWT) thấy TOÀN BỘ trường — không lọc.
                predicates.add(cb.equal(root.get("schoolId"), schoolId));
            }
            if (scopePartnerId != null) {
                // BR-099 — Caterer chỉ thấy phiên của đơn vị mình.
                predicates.add(cb.equal(root.get("partnerId"), scopePartnerId));
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
