package com.sfms.mf10.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf10.domain.Announcement;
import com.sfms.mf10.domain.AnnouncementStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** {@code Specification} cho {@code GET /announcements}. TDS-MF10_Backend.md §9.1 (CF-M10-6). */
public final class AnnouncementSpec {

    public static final List<String> SEARCH_FIELDS = List.of("title", "content", "audienceLabel");

    private static final Map<String, String> FIELD_MAP = Map.of(
            "status", "status",
            "urgent", "urgent");

    private AnnouncementSpec() {
    }

    /**
     * @param onlyPublished CF-M10-6 — {@code true} với mọi vai KHÁC School Administration:
     *                      chỉ {@code status=PUBLISHED}, {@code false} School Admin thấy cả DRAFT.
     * @param audienceCode  khác {@code null} ⇒ chỉ thông báo có audienceCodes chứa mã này
     *                      (JSONB containment — lọc thêm ở tầng Java, xem AnnouncementServiceImpl).
     */
    public static Specification<Announcement> build(ListQuery query, UUID schoolId, boolean onlyPublished) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            predicates.add(cb.equal(root.get("schoolId"), schoolId));
            if (onlyPublished) {
                predicates.add(cb.equal(root.get("status"), AnnouncementStatus.PUBLISHED));
            }

            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);

            if (query.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("publishedAt"),
                        query.dateFrom().atStartOfDay(java.time.ZoneOffset.UTC).toInstant()));
            }
            if (query.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("publishedAt"),
                        query.dateTo().atTime(23, 59, 59).atZone(java.time.ZoneOffset.UTC).toInstant()));
            }
            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
