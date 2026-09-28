package com.sfms.mf03.repository;

import com.sfms.mf03.domain.StudentParent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StudentParentRepository extends JpaRepository<StudentParent, UUID> {

    long countByParentUserId(UUID parentUserId);

    long countByStudentId(UUID studentId);

    List<StudentParent> findByParentUserId(UUID parentUserId);

    List<StudentParent> findByStudentId(UUID studentId);

    boolean existsByStudentIdAndParentUserId(UUID studentId, UUID parentUserId);
}
