package com.sfms.mf08.repository;

import com.sfms.mf08.domain.AttachmentKind;
import com.sfms.mf08.domain.AttachmentOwner;
import com.sfms.mf08.domain.SampleAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SampleAttachmentRepository extends JpaRepository<SampleAttachment, UUID> {

    List<SampleAttachment> findByOwnerTypeAndOwnerIdOrderByCreatedAtAsc(AttachmentOwner ownerType, UUID ownerId);

    List<SampleAttachment> findByOwnerTypeAndOwnerIdAndKindOrderByCreatedAtAsc(
            AttachmentOwner ownerType, UUID ownerId, AttachmentKind kind);

    long countByOwnerTypeAndOwnerIdAndKind(AttachmentOwner ownerType, UUID ownerId, AttachmentKind kind);
}
