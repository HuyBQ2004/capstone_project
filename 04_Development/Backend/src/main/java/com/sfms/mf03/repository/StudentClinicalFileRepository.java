package com.sfms.mf03.repository;

import com.sfms.mf03.domain.StudentClinicalFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudentClinicalFileRepository extends JpaRepository<StudentClinicalFile, UUID> {

    Optional<StudentClinicalFile> findByStudentId(UUID studentId);
}
