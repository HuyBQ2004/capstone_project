package com.sfms.mf02.repository;

import com.sfms.mf02.domain.SchoolDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchoolDocumentRepository extends JpaRepository<SchoolDocument, UUID> {
    List<SchoolDocument> findBySchoolId(UUID schoolId);
    Optional<SchoolDocument> findByCode(String code);
}
