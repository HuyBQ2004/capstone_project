package com.sfms.mf10.repository;

import com.sfms.mf10.domain.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface AnnouncementRepository
        extends JpaRepository<Announcement, UUID>, JpaSpecificationExecutor<Announcement> {

    Optional<Announcement> findBySchoolIdAndCode(UUID schoolId, String code);
}
