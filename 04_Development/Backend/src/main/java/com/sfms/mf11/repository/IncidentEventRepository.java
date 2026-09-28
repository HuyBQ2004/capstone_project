package com.sfms.mf11.repository;

import com.sfms.mf11.domain.IncidentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IncidentEventRepository extends JpaRepository<IncidentEvent, UUID> {

    List<IncidentEvent> findByIncidentIdOrderByAtAsc(UUID incidentId);

    long countByIncidentId(UUID incidentId);
}
