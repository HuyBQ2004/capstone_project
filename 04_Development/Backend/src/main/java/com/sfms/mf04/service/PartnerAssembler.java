package com.sfms.mf04.service;

import com.sfms.mf04.domain.Partner;
import com.sfms.mf04.domain.PartnerDocument;
import com.sfms.mf04.dto.PartnerDtos.PartnerDetailResponse;
import com.sfms.mf04.dto.PartnerDtos.PartnerDocumentSummary;
import com.sfms.mf04.dto.PartnerDtos.PartnerResponse;
import com.sfms.common.enums.RoleKey;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.mf04.mapper.DocumentMapper;
import com.sfms.mf04.mapper.PartnerMapper;
import com.sfms.mf04.repository.PartnerDocumentRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Lắp {@link Partner} + {@link PartnerDocument} thành DTO — dùng chung cho
 * {@link PartnerServiceImpl}, {@link PartnerApprovalServiceImpl}, {@link PartnerProfileServiceImpl}
 * và {@link PartnerRegistrationServiceImpl} để KHÔNG lặp lại logic dẫn xuất ADR-M04-4.
 */
@Component
public class PartnerAssembler {

    private final PartnerDocumentRepository documentRepository;
    private final PartnerMapper partnerMapper;
    private final DocumentMapper documentMapper;
    private final UserAccountRepository userAccountRepository;
    private final Clock clock;

    public PartnerAssembler(PartnerDocumentRepository documentRepository, PartnerMapper partnerMapper,
                            DocumentMapper documentMapper, UserAccountRepository userAccountRepository,
                            Clock clock) {
        this.documentRepository = documentRepository;
        this.partnerMapper = partnerMapper;
        this.documentMapper = documentMapper;
        this.userAccountRepository = userAccountRepository;
        this.clock = clock;
    }

    public PartnerResponse toResponse(Partner partner) {
        List<PartnerDocument> documents = documentRepository.findByPartnerId(partner.getId());
        LocalDate today = LocalDate.now(clock);
        LocalDate certExpiry = DocumentStatusCalculator.latestFoodSafetyCert(documents)
                .map(PartnerDocument::getExpiryDate).orElse(null);
        String certStatus = DocumentStatusCalculator.latestFoodSafetyCert(documents)
                .map(d -> DocumentStatusCalculator.statusOf(d, today).name()).orElse(null);
        return partnerMapper.toResponse(partner, certExpiry, certStatus, isRegistrantEmailVerified(partner));
    }

    public PartnerDetailResponse toDetailResponse(Partner partner) {
        List<PartnerDocument> documents = documentRepository.findByPartnerId(partner.getId());
        LocalDate today = LocalDate.now(clock);
        LocalDate certExpiry = DocumentStatusCalculator.latestFoodSafetyCert(documents)
                .map(PartnerDocument::getExpiryDate).orElse(null);
        String certStatus = DocumentStatusCalculator.latestFoodSafetyCert(documents)
                .map(d -> DocumentStatusCalculator.statusOf(d, today).name()).orElse(null);
        List<PartnerDocumentSummary> summaries = documents.stream()
                .filter(d -> d.getSupersededByDocumentId() == null)
                .map(d -> documentMapper.toSummary(d, DocumentStatusCalculator.statusOf(d, today).name()))
                .toList();
        return partnerMapper.toDetailResponse(partner, certExpiry, certStatus, summaries,
                isRegistrantEmailVerified(partner));
    }

    /** BR-010 mở rộng (11/08/2026) — mirror {@code SchoolService.isRegistrantEmailVerified()}. */
    private boolean isRegistrantEmailVerified(Partner partner) {
        return userAccountRepository.findAllByPartnerId(partner.getId()).stream()
                .anyMatch(u -> u.getRole() != null
                        && (RoleKey.SUPPLIER.key().equals(u.getRole().getRoleKey())
                            || RoleKey.SERVICE_CATERERS.key().equals(u.getRole().getRoleKey()))
                        && u.isEmailVerified());
    }
}
