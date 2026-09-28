package com.sfms.mf06.repository;

import com.sfms.mf06.domain.CatalogIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogIngredientRepository extends JpaRepository<CatalogIngredient, UUID> {

    Optional<CatalogIngredient> findByCode(String code);

    /** GET /ingredients (M06-D04) — danh mục nhỏ (~10-vài trăm dòng), không cần phân trang. */
    List<CatalogIngredient> findByActiveTrueOrderByNameAsc();
}
