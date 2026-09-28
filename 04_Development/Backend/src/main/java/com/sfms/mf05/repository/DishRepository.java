package com.sfms.mf05.repository;

import com.sfms.mf05.domain.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface DishRepository extends JpaRepository<Dish, UUID>, JpaSpecificationExecutor<Dish> {

    Optional<Dish> findByCode(String code);

    boolean existsByCode(String code);
}
