package com.sfms.mf05.repository;

import com.sfms.mf05.domain.DietGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DietGroupRepository extends JpaRepository<DietGroup, UUID> {

    Optional<DietGroup> findByCode(String code);

    List<DietGroup> findAll();
}
