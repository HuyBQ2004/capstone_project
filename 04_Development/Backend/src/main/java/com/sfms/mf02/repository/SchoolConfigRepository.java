package com.sfms.mf02.repository;

import com.sfms.mf02.domain.SchoolConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** TDS-MF02 §8.2. */
public interface SchoolConfigRepository extends JpaRepository<SchoolConfig, UUID> {

    Optional<SchoolConfig> findBySchoolId(UUID schoolId);
}
