package com.sfms.mf07.repository;

import com.sfms.mf07.domain.SafetySignoff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface SafetySignoffRepository
        extends JpaRepository<SafetySignoff, UUID>, JpaSpecificationExecutor<SafetySignoff> {

    Optional<SafetySignoff> findBySchoolIdAndDate(UUID schoolId, LocalDate date);
}
