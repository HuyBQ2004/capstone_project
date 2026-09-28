package com.sfms.mf04.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf04.domain.PartnerDocument;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** {@code GET /supplier-documents} (UC-167) — TDS §8.3, ADR-M04-3. */
public final class DocumentSpec {

    public static final List<String> SEARCH_FIELDS = List.of("name", "number", "issuedBy");

    private static final Map<String, String> FIELD_MAP = Map.of(
            "id", "code",
            "code", "code",
            "documentType", "documentType"
    );

    private DocumentSpec() {
    }

    /**
     * Tra CHÍNH XÁC theo danh sách {@code code} — dùng cho lookup-by-ids (VD FE resolve
     * {@code SupplierBatchResponse.certificateIds} thành tên/loại chứng từ cho Mẫu số 1 kiểm
     * thực 3 bước, thêm 08/2026). CỐ Ý KHÔNG áp {@code scopedPartnerId}/{@code
     * allowedPartnerIds} — điểm gọi (`ComplianceDocumentServiceImpl.list`) đã LỌC TRƯỚC danh
     * sách {@code codes} chỉ còn mã thật sự là chứng từ đính kèm lô hàng
     * ({@code batch_certificates}, qua JDBC — không có JPA entity cho bảng này) trước khi gọi
     * hàm này, nên tới đây {@code codes} đã an toàn để bỏ qua scope partner.
     */
    public static Specification<PartnerDocument> byCodes(List<String> codes) {
        return (root, cq, cb) -> codes.isEmpty() ? cb.disjunction() : root.get("code").in(codes);
    }

    /** @param scopedPartnerId khi khác {@code null}, ép lọc theo đúng partner đó (ADR-M04-3, NCC "Own"). */
    public static Specification<PartnerDocument> build(ListQuery query, UUID scopedPartnerId) {
        return build(query, scopedPartnerId, null);
    }

    /**
     * @param scopedPartnerId  NCC "Own" — ép đúng 1 partner
     * @param allowedPartnerIds School Admin "School" — chỉ đối tác đã/đang hợp tác với trường
     *                          mình (§16 ghi chú); {@code null}/rỗng ⇒ không áp thêm ràng buộc này
     */
    public static Specification<PartnerDocument> build(ListQuery query, UUID scopedPartnerId,
                                                         java.util.Collection<UUID> allowedPartnerIds) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();

            // Only list active documents that have not been superseded by a newer version
            predicates.add(cb.isNull(root.get("supersededByDocumentId")));

            if (scopedPartnerId != null) {
                predicates.add(cb.equal(root.get("partnerId"), scopedPartnerId));
            }
            if (allowedPartnerIds != null) {
                predicates.add(allowedPartnerIds.isEmpty()
                        ? cb.disjunction()
                        : root.get("partnerId").in(allowedPartnerIds));
            }

            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);

            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
