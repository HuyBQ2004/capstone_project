package com.sfms.mf07.repository;

import com.sfms.mf07.domain.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, UUID> {

    Optional<ChecklistItem> findBySchoolIdAndCode(UUID schoolId, String code);

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);

    long countByGroup_Id(UUID groupId);
}
