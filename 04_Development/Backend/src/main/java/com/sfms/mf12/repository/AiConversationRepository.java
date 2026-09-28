package com.sfms.mf12.repository;

import com.sfms.mf12.domain.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiConversationRepository extends JpaRepository<AiConversation, UUID> {

    List<AiConversation> findByUserIdOrderByLastMessageAtDesc(UUID userId);

    Optional<AiConversation> findByCode(String code);   // D1

    Optional<AiConversation> findByCodeAndUserId(String code, UUID userId);
}
