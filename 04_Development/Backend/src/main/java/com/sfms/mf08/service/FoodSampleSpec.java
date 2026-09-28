package com.sfms.mf08.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf08.domain.FoodSample;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code Specification} cho {@code GET /samples}. TDS-MF08_Backend.md §9.1, M08-D12.
 *
 * <p>⚠️ {@code SEARCH_FIELDS} chỉ 3 cột (M08-D12, sửa 2026-08-03) — TDS v1.0 có thêm
 * {@code meal_label}, nhưng {@code M08-D05} biến nó thành nhãn DẪN XUẤT (không còn cột để
 * {@code ILIKE}), cùng lớp lỗi MF07 phải rút {@code SEARCH_FIELDS} còn {@code staffNotes}.
 *
 * <p>{@code mealType} là filter EXACT trên enum (M08-D05) — thay {@code mealPrefix} của mock
 * (dựa trên {@code meal_label LIKE 'X%'}); vì {@code mealType} không nằm trong
 * {@link ListQuery#CONTROL_KEYS}, nó tự động rơi vào {@code filters} map qua {@link ListQuery#from}.
 */
public final class FoodSampleSpec {

    public static final List<String> SEARCH_FIELDS = List.of("code", "dishName", "collectedBy");

    private static final Map<String, String> FIELD_MAP = Map.ofEntries(
            Map.entry("id", "code"),
            Map.entry("status", "status"),
            Map.entry("mealType", "mealType")
            // `incidentId` KHÔNG còn ở đây (08/08/2026): nó nhận MÃ nghiệp vụ ("INC-2026-005")
            // còn cột là UUID, so chuỗi trực tiếp thì không bao giờ khớp. Nay resolve mã→UUID
            // ở tầng service (`resolveIncidentFilter`) rồi truyền xuống như `dishIdFilter`.
    );

    private FoodSampleSpec() {
    }

    /**
     * @param dishIdFilter     UUID đã resolve từ query param {@code dish} (mã dishes.code) ở tầng service, hoặc {@code null}.
     * @param incidentIdFilter UUID đã resolve từ query param {@code incidentId} (mã incidents.code), hoặc {@code null}.
     */
    public static Specification<FoodSample> build(ListQuery query, UUID schoolId,
                                                  UUID dishIdFilter, UUID incidentIdFilter) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            predicates.add(cb.equal(root.get("schoolId"), schoolId));
            if (dishIdFilter != null) {
                predicates.add(cb.equal(root.get("dishId"), dishIdFilter));
            }
            if (incidentIdFilter != null) {
                predicates.add(cb.equal(root.get("incidentId"), incidentIdFilter));
            }

            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);

            if (query.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("collectedAt"),
                        query.dateFrom().atStartOfDay(java.time.ZoneOffset.UTC).toInstant()));
            }
            if (query.dateTo() != null) {
                predicates.add(cb.lessThan(root.get("collectedAt"),
                        query.dateTo().plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant()));
            }
            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
