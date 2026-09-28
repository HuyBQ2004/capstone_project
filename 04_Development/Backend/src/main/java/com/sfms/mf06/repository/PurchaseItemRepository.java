package com.sfms.mf06.repository;

import com.sfms.mf06.domain.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, UUID> {

    List<PurchaseItem> findByPurchaseRequestId(UUID purchaseRequestId);

    void deleteByPurchaseRequestId(UUID purchaseRequestId);
}
