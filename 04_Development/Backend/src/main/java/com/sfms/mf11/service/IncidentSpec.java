package com.sfms.mf11.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf11.domain.CorrectiveAction;
import com.sfms.mf11.domain.Incident;
import com.sfms.mf11.domain.IncidentAffectedStudent;
import com.sfms.mf11.domain.IncidentQuarantineBatch;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code Specification} cho {@code GET /incidents}. TDS-MF11_Backend.md §9.1/§16, ADR-M11-10.
 *
 * <p>Phạm vi đọc DẪN XUẤT tại query, KHÔNG thêm cột chủ sở hữu (C15):
 * <ul>
 *   <li>Teacher — {@code EXISTS} trên {@code incident_affected_students.class_name}</li>
 *   <li>Service Caterers — {@code EXISTS} CAPA giao đơn vị mình <b>HOẶC</b> lô cách ly
 *   thuộc NCC/đơn vị mình (M11-D19, cả 2 vế `OR` — Nam chốt 04/08)</li>
 * </ul>
 */
public final class IncidentSpec {

    public static final List<String> SEARCH_FIELDS = List.of("title", "reportedByLabel", "suspectedMealText");

    private static final Map<String, String> FIELD_MAP = Map.ofEntries(
            Map.entry("status", "status"),
            Map.entry("severity", "severity")
    );

    private IncidentSpec() {
    }

    /**
     * @param teacherClassNames      khác {@code null} ⇒ giới hạn Teacher (Own class); {@code null} = không áp
     * @param catererPartnerId       khác {@code null} ⇒ giới hạn Service Caterers (Liên đới); {@code null} = không áp
     * @param catererSuppliedBatchIds lô nguyên liệu do {@code catererPartnerId} cung cấp (đã resolve qua
     *                                {@code InventoryBatchPort} trước khi gọi hàm này)
     */
    public static Specification<Incident> build(ListQuery query, UUID schoolId,
                                                  List<String> teacherClassNames,
                                                  UUID catererPartnerId,
                                                  List<UUID> catererSuppliedBatchIds) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            predicates.add(cb.equal(root.get("schoolId"), schoolId));

            if (teacherClassNames != null) {
                Subquery<UUID> sub = cq.subquery(UUID.class);
                var subRoot = sub.from(IncidentAffectedStudent.class);
                sub.select(subRoot.get("incident").get("id"));
                Predicate classIn = teacherClassNames.isEmpty()
                        ? cb.disjunction()
                        : subRoot.get("className").in(teacherClassNames);
                sub.where(cb.equal(subRoot.get("incident").get("id"), root.get("id")), classIn);
                predicates.add(cb.exists(sub));
            }

            if (catererPartnerId != null) {
                Subquery<UUID> capaSub = cq.subquery(UUID.class);
                var capaRoot = capaSub.from(CorrectiveAction.class);
                capaSub.select(capaRoot.get("incidentId"));
                capaSub.where(cb.equal(capaRoot.get("incidentId"), root.get("id")),
                        cb.equal(capaRoot.get("assigneeSupplierId"), catererPartnerId));

                Subquery<UUID> batchSub = cq.subquery(UUID.class);
                var batchRoot = batchSub.from(IncidentQuarantineBatch.class);
                batchSub.select(batchRoot.get("incident").get("id"));
                Predicate batchIn = (catererSuppliedBatchIds == null || catererSuppliedBatchIds.isEmpty())
                        ? cb.disjunction()
                        : batchRoot.get("batchId").in(catererSuppliedBatchIds);
                batchSub.where(cb.equal(batchRoot.get("incident").get("id"), root.get("id")), batchIn);

                predicates.add(cb.or(cb.exists(capaSub), cb.exists(batchSub)));
            }

            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);

            if (query.dateFrom() != null) {
                Instant from = query.dateFrom().atStartOfDay(ZoneOffset.UTC).toInstant();
                predicates.add(cb.greaterThanOrEqualTo(root.get("reportedAt").as(Instant.class), from));
            }
            if (query.dateTo() != null) {
                Instant to = query.dateTo().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
                predicates.add(cb.lessThan(root.get("reportedAt").as(Instant.class), to));
            }
            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
