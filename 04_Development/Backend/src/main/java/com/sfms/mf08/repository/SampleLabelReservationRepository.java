package com.sfms.mf08.repository;

import com.sfms.mf08.domain.SampleLabelReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface SampleLabelReservationRepository extends JpaRepository<SampleLabelReservation, UUID>,
        JpaSpecificationExecutor<SampleLabelReservation> {

    Optional<SampleLabelReservation> findBySchoolIdAndCode(UUID schoolId, String code);

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);
}
