package com.sfms.mf04.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.domain.PartnerDocument;
import com.sfms.mf04.domain.SupplierPartnership;
import com.sfms.mf04.dto.PartnershipDtos.PartnershipResponse;
import com.sfms.mf04.mapper.PartnershipMapper;
import com.sfms.mf04.repository.PartnerDocumentRepository;
import com.sfms.mf04.repository.SupplierPartnershipRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** UC-155/159 — ADR-M04-4: {@code docStatus} dẫn xuất từ tài liệu hiện hành của partner. */
@Service
public class SupplierPartnershipServiceImpl implements SupplierPartnershipService {

    private final SupplierPartnershipRepository partnershipRepository;
    private final PartnerDocumentRepository documentRepository;
    private final PartnershipMapper partnershipMapper;
    private final ReferenceResolver referenceResolver;
    private final Clock clock;

    public SupplierPartnershipServiceImpl(SupplierPartnershipRepository partnershipRepository,
                                          PartnerDocumentRepository documentRepository,
                                          PartnershipMapper partnershipMapper,
                                          ReferenceResolver referenceResolver,
                                          Clock clock) {
        this.partnershipRepository = partnershipRepository;
        this.documentRepository = documentRepository;
        this.partnershipMapper = partnershipMapper;
        this.referenceResolver = referenceResolver;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PartnershipResponse> list(ListQuery query, AuthUser actor) {
        var spec = switch (actor.role()) {
            case SUPPLIER, SERVICE_CATERERS -> PartnershipSpec.build(query, actor.partnerId(), null);
            case SCHOOL_ADMIN -> PartnershipSpec.build(query, null, actor.schoolId());
            default -> PartnershipSpec.build(query, null, null);   // System Admin — All
        };
        Page<SupplierPartnership> page = partnershipRepository.findAll(spec, query.toPageable());
        List<PartnershipResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    private PartnershipResponse toResponse(SupplierPartnership partnership) {
        LocalDate today = LocalDate.now(clock);
        List<PartnerDocument> documents = documentRepository.findByPartnerId(partnership.getPartnerId());
        String docStatus = DocumentStatusCalculator.latestFoodSafetyCert(documents)
                .map(d -> DocumentStatusCalculator.statusOf(d, today).name())
                .orElse(DocumentStatus.VALID.name());
        return partnershipMapper.toResponse(partnership, referenceResolver.partnerCode(partnership.getPartnerId()),
                referenceResolver.schoolCode(partnership.getSchoolId()),
                referenceResolver.schoolName(partnership.getSchoolId()), docStatus);
    }
}
