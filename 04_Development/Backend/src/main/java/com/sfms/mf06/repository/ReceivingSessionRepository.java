package com.sfms.mf06.repository;

import com.sfms.mf06.domain.ReceivingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ReceivingSessionRepository extends JpaRepository<ReceivingSession, UUID>,
        JpaSpecificationExecutor<ReceivingSession> {

    Optional<ReceivingSession> findBySchoolIdAndCode(UUID schoolId, String code);

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);
}
