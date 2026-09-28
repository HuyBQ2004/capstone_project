package com.sfms.mf10.repository;

import com.sfms.mf10.domain.FeedbackAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackAttachmentRepository extends JpaRepository<FeedbackAttachment, UUID> {

    List<FeedbackAttachment> findByFeedbackIdOrderByCreatedAtAsc(UUID feedbackId);

    Optional<FeedbackAttachment> findBySchoolIdAndFeedbackIdAndCode(UUID schoolId, UUID feedbackId, String code);
}
