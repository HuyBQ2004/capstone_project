package com.sfms.mf12.repository;

import com.sfms.mf12.domain.ExternalIntegration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExternalIntegrationRepository extends JpaRepository<ExternalIntegration, UUID> {

    List<ExternalIntegration> findAllByOrderByLabelAsc();

    Optional<ExternalIntegration> findByCode(String code);   // D1 — gps|gmail|firebase|gemini
}
