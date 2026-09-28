package com.sfms.mf04.service;

import com.sfms.mf04.domain.PartnerStatus;
import com.sfms.mf04.port.PartnerLookupPort;
import com.sfms.mf04.port.PartnerSummary;
import com.sfms.mf04.repository.PartnerDocumentRepository;
import com.sfms.mf04.repository.PartnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/** ADR-M04-7 — {@code partners} là nguồn sự thật duy nhất; MF05/06/07/11/13 đọc qua đây. */
@Service
public class PartnerLookupPortImpl implements PartnerLookupPort {

    private final PartnerRepository partnerRepository;
    private final PartnerDocumentRepository documentRepository;
    private final Clock clock;

    public PartnerLookupPortImpl(PartnerRepository partnerRepository,
                                 PartnerDocumentRepository documentRepository,
                                 Clock clock) {
        this.partnerRepository = partnerRepository;
        this.documentRepository = documentRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isApproved(UUID partnerId) {
        return partnerRepository.findById(partnerId)
                .map(p -> p.getStatus() == PartnerStatus.APPROVED)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasValidFoodSafetyCert(UUID partnerId) {
        LocalDate today = LocalDate.now(clock);
        return DocumentStatusCalculator.latestFoodSafetyCert(documentRepository.findByPartnerId(partnerId))
                .map(doc -> DocumentStatusCalculator.statusOf(doc, today) != DocumentStatus.EXPIRED)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PartnerSummary> findById(UUID partnerId) {
        return partnerRepository.findById(partnerId)
                .map(p -> new PartnerSummary(p.getId(), p.getCode(), p.getName(), p.getType(),
                        p.getStatus().name()));
    }
}
