package com.sfms.mf04.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.domain.Partner;
import com.sfms.mf04.dto.PartnerDtos.PartnerDetailResponse;
import com.sfms.mf04.dto.PartnerDtos.UpdatePartnerProfileRequest;
import com.sfms.mf04.error.SupExceptions;
import com.sfms.mf04.repository.PartnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** UC-049 — chủ thể luôn là đối tác trong JWT (giống ProfileService của MF01). */
@Service
public class PartnerProfileServiceImpl implements PartnerProfileService {

    private final PartnerRepository partnerRepository;
    private final PartnerAssembler assembler;

    public PartnerProfileServiceImpl(PartnerRepository partnerRepository, PartnerAssembler assembler) {
        this.partnerRepository = partnerRepository;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerDetailResponse getMine(AuthUser actor) {
        return assembler.toDetailResponse(require(actor));
    }

    @Override
    @Transactional
    public PartnerDetailResponse updateMine(UpdatePartnerProfileRequest req, AuthUser actor) {
        Partner partner = require(actor);

        if (req.companyName() != null && !req.companyName().isBlank()) {
            partner.setName(req.companyName());
        }
        if (req.address() != null) {
            partner.setAddress(req.address());
        }
        if (req.email() != null && !req.email().equalsIgnoreCase(partner.getEmail())) {
            if (partnerRepository.existsByEmailIgnoreCase(req.email())) {
                throw SupExceptions.emailAlreadyUsed();                         // MSG-006
            }
            partner.setEmail(req.email());
        }
        if (req.phone() != null) {
            partner.setPhone(req.phone());
        }
        if (req.description() != null) {
            partner.setDescription(req.description());
        }

        partnerRepository.saveAndFlush(partner);
        return assembler.toDetailResponse(partner);
    }

    private Partner require(AuthUser actor) {
        return partnerRepository.findById(actor.partnerId()).orElseThrow(SupExceptions::notFound);
    }
}
