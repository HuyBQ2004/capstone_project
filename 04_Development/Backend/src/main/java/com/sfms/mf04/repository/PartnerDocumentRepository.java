package com.sfms.mf04.repository;

import com.sfms.mf04.domain.DocumentType;
import com.sfms.mf04.domain.PartnerDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartnerDocumentRepository extends JpaRepository<PartnerDocument, UUID>,
        JpaSpecificationExecutor<PartnerDocument> {

    /** UC-167. */
    List<PartnerDocument> findByPartnerId(UUID partnerId);

    /** ADR-M04-3 — cách ly theo chủ sở hữu. */
    Optional<PartnerDocument> findByIdAndPartnerId(UUID id, UUID partnerId);

    Optional<PartnerDocument> findByCode(String code);

    Optional<PartnerDocument> findByCodeAndPartnerId(String code, UUID partnerId);

    boolean existsByCode(String code);

    List<PartnerDocument> findByPartnerIdAndDocumentType(UUID partnerId, DocumentType type);

    boolean existsByPartnerIdAndDocumentTypeAndNumber(UUID partnerId, DocumentType type, String number);

    /** BR-047 — job quét tài liệu tới hạn (180/30 ngày), 1 query, không N+1. */
    @Query("""
           SELECT d FROM PartnerDocument d
           WHERE d.supersededByDocumentId IS NULL
             AND d.expiryDate IS NOT NULL
             AND d.expiryDate <= :threshold
           """)
    List<PartnerDocument> findExpiringBefore(@Param("threshold") LocalDate threshold);
}
