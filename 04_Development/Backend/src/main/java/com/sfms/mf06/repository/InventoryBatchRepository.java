package com.sfms.mf06.repository;

import com.sfms.mf06.domain.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, UUID>,
        JpaSpecificationExecutor<InventoryBatch> {

    Optional<InventoryBatch> findBySchoolIdAndCode(UUID schoolId, String code);

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);

    /** BR-075 — FEFO: ứng viên còn hạn, khu bất kỳ, sắp theo hạn dùng tăng dần. */
    List<InventoryBatch> findBySchoolIdAndIngredientIdOrderByExpiryDateAsc(UUID schoolId, UUID ingredientId);
}
