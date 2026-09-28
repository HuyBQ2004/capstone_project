package com.sfms.mf12.repository;

import com.sfms.mf12.domain.AiRequest;
import com.sfms.mf12.domain.AiRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiRequestRepository
        extends JpaRepository<AiRequest, UUID>, JpaSpecificationExecutor<AiRequest> {

    /** UC-119 — lịch sử của chính người dùng. SEARCH_FIELDS = {prompt, module} (`00_` §2b-3). */
    List<AiRequest> findByRequestedByOrderByRequestedAtDesc(UUID userId);

    List<AiRequest> findByConversationIdOrderByRequestedAtAsc(UUID conversationId);

    Optional<AiRequest> findByCode(String code);   // D1

    /** Thống kê độ tin cậy AI cho UC-150. */
    long countByStatusAndRequestedAtBetween(AiRequestStatus status, Instant from, Instant to);

    /** UC-150 — top use case theo module gọi AI. */
    long countByModule(String module);
}
