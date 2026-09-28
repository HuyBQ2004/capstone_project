package com.sfms.mf03.repository;

import com.sfms.mf03.domain.StudentHealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudentHealthRecordRepository extends JpaRepository<StudentHealthRecord, UUID> {

    Optional<StudentHealthRecord> findByStudentId(UUID studentId);
}
