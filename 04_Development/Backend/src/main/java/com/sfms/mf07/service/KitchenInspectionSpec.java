package com.sfms.mf07.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf07.domain.KitchenInspection;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code Specification} cho {@code GET /kitchen-inspections}.
 *
 * <p>Trước bản vá này {@code KitchenInspectionServiceImpl.list()} dùng lambda tự viết chỉ lọc
 * {@code schoolId} — {@code dateFrom}/{@code dateTo}/{@code result} (toolbar
 * `conduct-inspection/history/page.tsx`) và search đều bị BỎ QUA hoàn toàn, cùng lớp lỗi đã
 * vá ở `DeliveryService.list()`/`ReceivingSessionService.list()` (MF06, 05/08/2026).
 */
public final class KitchenInspectionSpec {

    public static final List<String> SEARCH_FIELDS = List.of();

    private static final Map<String, String> FIELD_MAP = Map.ofEntries(
            Map.entry("id", "code"),
            Map.entry("result", "result"),
            Map.entry("confirmStatus", "confirmStatus")
    );

    private KitchenInspectionSpec() {
    }

    public static Specification<KitchenInspection> build(ListQuery query, UUID schoolId) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            if (schoolId != null) {
                predicates.add(cb.equal(root.get("schoolId"), schoolId));
            }

            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);

            if (query.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), query.dateFrom()));
            }
            if (query.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), query.dateTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
