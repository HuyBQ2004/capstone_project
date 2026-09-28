package com.sfms.mf04.repository;

import com.sfms.mf04.domain.DeliveryPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryPersonRepository extends JpaRepository<DeliveryPerson, UUID>,
        JpaSpecificationExecutor<DeliveryPerson> {

    /** BR-050 — CCCD duy nhất toàn hệ thống. */
    boolean existsByNationalIdHash(String hash);

    boolean existsByCode(String code);

    /** ADR-M04-3 — cách ly theo chủ sở hữu. */
    Optional<DeliveryPerson> findByCodeAndPartnerId(String code, UUID partnerId);
}
