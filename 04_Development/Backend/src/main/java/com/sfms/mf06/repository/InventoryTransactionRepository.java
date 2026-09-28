package com.sfms.mf06.repository;

import com.sfms.mf06.domain.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID>,
        JpaSpecificationExecutor<InventoryTransaction> {

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);

    List<InventoryTransaction> findByBatchId(UUID batchId);

    /** ADR-M06-1 — tồn khả dụng = SUM(delta) của mọi giao dịch thuộc lô. */
    @Query("SELECT COALESCE(SUM(t.delta), 0) FROM InventoryTransaction t WHERE t.batchId = :batchId")
    BigDecimal sumDeltaByBatchId(UUID batchId);
}
