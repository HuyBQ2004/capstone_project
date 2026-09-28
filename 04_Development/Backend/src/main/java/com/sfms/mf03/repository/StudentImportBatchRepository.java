package com.sfms.mf03.repository;

import com.sfms.mf03.domain.StudentImportBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudentImportBatchRepository extends JpaRepository<StudentImportBatch, UUID> {

    Optional<StudentImportBatch> findByCode(String code);
}
