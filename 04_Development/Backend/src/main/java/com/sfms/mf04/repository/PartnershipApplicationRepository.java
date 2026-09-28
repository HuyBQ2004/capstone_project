package com.sfms.mf04.repository;

import com.sfms.mf04.domain.ApplicationStatus;
import com.sfms.mf04.domain.PartnershipApplication;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface PartnershipApplicationRepository extends JpaRepository<PartnershipApplication, UUID>,
        JpaSpecificationExecutor<PartnershipApplication> {

    Optional<PartnershipApplication> findByCode(String code);

    Optional<PartnershipApplication> findByCodeAndPartnerId(String code, UUID partnerId);

    /** {@code FOR UPDATE} khi duyệt — chống 2 School Admin duyệt song song (BR-040/041). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PartnershipApplication> findByPartnerIdAndSchoolIdAndStatusIn(
            UUID partnerId, UUID schoolId, Collection<ApplicationStatus> statuses);

    /** UC-152/156 precondition — "does not have an active application". */
    boolean existsByPartnerIdAndSchoolIdAndStatusIn(
            UUID partnerId, UUID schoolId, Collection<ApplicationStatus> statuses);

    boolean existsByCode(String code);
}
