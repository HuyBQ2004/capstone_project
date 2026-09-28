package com.sfms.mf11.repository;

import com.sfms.mf11.domain.Mf11Attachment;
import com.sfms.mf11.domain.Mf11AttachmentOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface Mf11AttachmentRepository extends JpaRepository<Mf11Attachment, UUID> {

    List<Mf11Attachment> findByOwnerTypeAndOwnerIdOrderByCreatedAtAsc(Mf11AttachmentOwner ownerType, UUID ownerId);

    long countByOwnerTypeAndOwnerId(Mf11AttachmentOwner ownerType, UUID ownerId);
}
