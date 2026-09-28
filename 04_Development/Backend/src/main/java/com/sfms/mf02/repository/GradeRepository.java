package com.sfms.mf02.repository;

import com.sfms.mf02.domain.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GradeRepository extends JpaRepository<Grade, UUID> {

    List<Grade> findBySchoolIdAndActiveTrueOrderBySortOrder(UUID schoolId);

    List<Grade> findBySchoolIdOrderBySortOrder(UUID schoolId);

    boolean existsBySchoolIdAndNameAndIdNot(UUID schoolId, String name, UUID id);

    boolean existsBySchoolIdAndName(UUID schoolId, String name);
}
