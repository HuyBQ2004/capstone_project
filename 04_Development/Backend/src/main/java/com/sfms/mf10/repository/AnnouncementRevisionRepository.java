package com.sfms.mf10.repository;

import com.sfms.mf10.domain.AnnouncementRevision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnnouncementRevisionRepository extends JpaRepository<AnnouncementRevision, UUID> {

    List<AnnouncementRevision> findByAnnouncementIdOrderByRevisedAtAsc(UUID announcementId);

    long countByAnnouncementId(UUID announcementId);
}
