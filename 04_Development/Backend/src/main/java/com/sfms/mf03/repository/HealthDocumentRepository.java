package com.sfms.mf03.repository;

import com.sfms.mf03.domain.HealthDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HealthDocumentRepository extends JpaRepository<HealthDocument, UUID> {

    /** D1 — tra cứu theo MÃ NGHIỆP VỤ (`id` của JSON), không phải khoá UUID. */
    Optional<HealthDocument> findByCode(String code);
}
