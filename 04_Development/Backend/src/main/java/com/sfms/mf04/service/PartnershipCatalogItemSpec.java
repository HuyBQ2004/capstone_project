package com.sfms.mf04.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf04.domain.PartnershipCatalogItem;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code GET /partnerships/{code}/catalog-items}. PROPOSED — xem
 * {@code Proposal-Partnership-Catalog-Contract.md}, chưa có TDS §8/§9 chính thức.
 */
public final class PartnershipCatalogItemSpec {

    private static final Map<String, String> FIELD_MAP = Map.of(
            "id", "code",
            "code", "code",
            "status", "status"
    );

    private PartnershipCatalogItemSpec() {
    }

    public static Specification<PartnershipCatalogItem> build(ListQuery query, UUID partnershipId) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            predicates.add(cb.equal(root.get("partnershipId"), partnershipId));
            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * {@code GET /catalog-items} — TOÀN BỘ mặt hàng đang hiệu lực của MỘT TRƯỜNG, gộp mọi hợp
     * đồng hợp tác, không cần biết mã hợp đồng nào.
     *
     * <p>Vì sao cần bản này: người lập thực đơn (đơn vị suất ăn) phải biết trường ĐƯỢC PHÉP mua
     * những gì trước khi đặt món — nhưng họ không đứng tên hợp đồng nào nên không có mã
     * {@code PTN-…} để gọi bản theo hợp đồng, và endpoint đó cũng chặn vai này (đo 09/08/2026:
     * 403 IAM-004). Hệ quả đo được: gợi ý thực đơn bằng AI lấy nguyên liệu từ danh mục CHUNG
     * toàn hệ thống (6 mục) trong khi danh mục đã duyệt của trường chỉ có 4 — 2 món đề xuất ra
     * không thể mua, chặn ở cổng ING-077 mãi sau đó, lúc đã lên kế hoạch xong.
     *
     * <p>Cùng điều kiện hiệu lực với {@code ReferenceResolver.isIngredientInApprovedCatalog}
     * (ACTIVE + {@code effectiveFrom <= hôm nay}) — hai chỗ lệch nhau thì UI lại hứa thứ mà cổng
     * mua hàng từ chối.
     */
    public static Specification<PartnershipCatalogItem> forSchool(ListQuery query, UUID schoolId,
                                                                  java.time.LocalDate today) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            predicates.add(cb.equal(root.get("schoolId"), schoolId));
            predicates.add(cb.equal(root.get("status"), com.sfms.mf04.domain.CatalogItemStatus.ACTIVE));
            predicates.add(cb.or(
                    cb.isNull(root.get("effectiveFrom")),
                    cb.lessThanOrEqualTo(root.get("effectiveFrom"), today)));
            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
