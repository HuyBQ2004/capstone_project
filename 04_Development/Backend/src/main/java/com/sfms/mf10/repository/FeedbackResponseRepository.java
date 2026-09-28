package com.sfms.mf10.repository;

import com.sfms.mf10.domain.FeedbackResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeedbackResponseRepository extends JpaRepository<FeedbackResponse, UUID> {

    List<FeedbackResponse> findByFeedbackIdOrderByRespondedAtAsc(UUID feedbackId);

    boolean existsByFeedbackId(UUID feedbackId);
}
