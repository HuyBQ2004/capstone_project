package com.sfms.mf06.repository;

import com.sfms.mf06.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID>, JpaSpecificationExecutor<Delivery> {

    Optional<Delivery> findBySchoolIdAndCode(UUID schoolId, String code);

    Optional<Delivery> findByCode(String code);

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);
}
