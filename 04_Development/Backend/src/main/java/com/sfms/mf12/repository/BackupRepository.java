package com.sfms.mf12.repository;

import com.sfms.mf12.domain.Backup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BackupRepository extends JpaRepository<Backup, UUID> {

    List<Backup> findAllByOrderByStartedAtDesc();

    Optional<Backup> findByCode(String code);
}
