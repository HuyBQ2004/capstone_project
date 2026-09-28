package com.sfms.mf04.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.scope.ActorScope;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.domain.Partner;
import com.sfms.mf04.dto.PartnerDtos.PartnerDetailResponse;
import com.sfms.mf04.dto.PartnerDtos.PartnerResponse;
import com.sfms.mf04.error.SupExceptions;
import com.sfms.mf04.repository.PartnerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** UC-131/127 (View List) · UC-148/108 (View Detail) — chỉ School Administration + System Admin. */
@Service
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;
    private final PartnerAssembler assembler;
    private final SchoolPartnerLinkResolver schoolPartnerLinkResolver;
    private final ReferenceResolver referenceResolver;

    public PartnerServiceImpl(PartnerRepository partnerRepository, PartnerAssembler assembler,
                              SchoolPartnerLinkResolver schoolPartnerLinkResolver,
                              ReferenceResolver referenceResolver) {
        this.partnerRepository = partnerRepository;
        this.assembler = assembler;
        this.schoolPartnerLinkResolver = schoolPartnerLinkResolver;
        this.referenceResolver = referenceResolver;
    }

    /*
     * KHÔNG dùng `actor.schoolId()` trực tiếp — xem `common/scope/ActorScope`.
     *
     * Tài khoản Đơn vị suất ăn không có `schoolId` trong JWT, nên bản cũ gọi
     * `partnerIdsConnectedToSchool(null)` → danh sách rỗng → `cb.disjunction()` → `GET /partners`
     * trả 0 dòng. Đo thật 08/08/2026: màn "Nhà cung cấp & Sản phẩm" của bếp ghi "Không tìm thấy
     * nhà cung cấp hợp tác nào" trong khi trường đang có hợp tác APPROVED với SUP-202.
     */
    private java.util.UUID scopeSchoolId(AuthUser actor) {
        return ActorScope.schoolIdOf(actor, referenceResolver);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PartnerResponse> list(ListQuery query, AuthUser actor) {
        Specification<Partner> spec = PartnerSpec.build(query);
        if (!actor.isSystemAdmin()) {
            List<java.util.UUID> allowed = schoolPartnerLinkResolver.partnerIdsConnectedToSchool(scopeSchoolId(actor));
            Specification<Partner> scoped = allowed.isEmpty()
                    ? (root, cq, cb) -> cb.disjunction()
                    : (root, cq, cb) -> root.get("id").in(allowed);
            spec = spec.and(scoped);
        }
        Page<Partner> page = partnerRepository.findAll(spec, query.toPageable());
        List<PartnerResponse> items = page.getContent().stream().map(assembler::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerDetailResponse getDetail(String code, AuthUser actor) {
        Partner partner = partnerRepository.findByCode(code).orElseThrow(SupExceptions::notFound);
        if (!actor.isSystemAdmin()
                && !schoolPartnerLinkResolver.partnerIdsConnectedToSchool(scopeSchoolId(actor)).contains(partner.getId())) {
            throw SupExceptions.notFound();      // M04-D10 — ngoài phạm vi ⇒ 404, không 403
        }
        return assembler.toDetailResponse(partner);
    }
}
