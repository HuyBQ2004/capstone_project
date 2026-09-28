package com.sfms.mf05.repository;

import com.sfms.mf05.domain.MenuTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface MenuTemplateRepository
        extends JpaRepository<MenuTemplate, UUID>, JpaSpecificationExecutor<MenuTemplate> {

    Optional<MenuTemplate> findBySchoolIdAndCode(UUID schoolId, String code);

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);
}
