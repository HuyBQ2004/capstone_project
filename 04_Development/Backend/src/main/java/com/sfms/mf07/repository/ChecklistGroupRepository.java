package com.sfms.mf07.repository;

import com.sfms.mf07.domain.ChecklistGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChecklistGroupRepository extends JpaRepository<ChecklistGroup, UUID> {

    /** BR-210 — checklist theo TỪNG TRƯỜNG, chỉ mục đang hiệu lực cho phiên MỚI. */
    List<ChecklistGroup> findBySchoolIdAndActiveTrueOrderByDisplayOrderAsc(UUID schoolId);

    List<ChecklistGroup> findBySchoolIdOrderByDisplayOrderAsc(UUID schoolId);

    Optional<ChecklistGroup> findBySchoolIdAndCode(UUID schoolId, String code);

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);
}
