package com.sfms.mf12.repository;

import com.sfms.mf12.domain.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ErrorLogRepository
        extends JpaRepository<ErrorLog, UUID>, JpaSpecificationExecutor<ErrorLog> {

    Optional<ErrorLog> findByCode(String code);   // D1
}
