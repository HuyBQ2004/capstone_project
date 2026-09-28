package com.sfms.mf04.repository;

import com.sfms.mf04.domain.PartnerRegistrationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartnerRegistrationDocumentRepository extends JpaRepository<PartnerRegistrationDocument, UUID> {
    List<PartnerRegistrationDocument> findByPartnerId(UUID partnerId);
    Optional<PartnerRegistrationDocument> findByCode(String code);
}
