package com.sfms.mf07.repository;

import com.sfms.mf07.domain.InspectionEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InspectionEvidenceRepository extends JpaRepository<InspectionEvidence, UUID> {

    List<InspectionEvidence> findByOwnerTypeAndOwnerId(String ownerType, UUID ownerId);

    long countByOwnerTypeAndOwnerId(String ownerType, UUID ownerId);

    /** Mọi ảnh gắn 1 phiên kiểm thực — SESSION/STEP1_FRESH/STEP1_DRY/STEP2/STEP3 đều dùng
     *  {@code ownerId = session.id} (xem {@code EvidenceServiceImpl.uploadForSession}). */
    List<InspectionEvidence> findByOwnerIdOrderByUploadedAtDesc(UUID ownerId);

    /**
     * V68/11-08-2026 — ảnh KITCHEN_ITEM gắn theo TỪNG {@code ChecklistItemResult.resultId}
     * (một hạng mục = một owner_id riêng), khác kiểu "cả phiên 1 owner_id" ở trên — 1 phiên
     * kiểm tra bếp có NHIỀU hạng mục nên cần truy nhiều owner_id cùng lúc.
     */
    List<InspectionEvidence> findByOwnerTypeAndOwnerIdInOrderByUploadedAtDesc(String ownerType, List<UUID> ownerIds);
}
