package com.sfms.mf12.repository;

import com.sfms.mf12.domain.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, UUID> {

    Optional<KnowledgeDocument> findByCode(String code);

    List<KnowledgeDocument> findAllByOrderByCreatedAtDesc();

    /** Gửi kèm mỗi lượt gọi RAG (`GeminiClient`) — tầng service tự giới hạn số lượng gửi kèm. */
    List<KnowledgeDocument> findByActiveTrueOrderByCreatedAtDesc();
}
