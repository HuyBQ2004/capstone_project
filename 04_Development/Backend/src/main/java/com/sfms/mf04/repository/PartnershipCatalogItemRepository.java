package com.sfms.mf04.repository;

import com.sfms.mf04.domain.CatalogItemStatus;
import com.sfms.mf04.domain.PartnershipCatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface PartnershipCatalogItemRepository extends JpaRepository<PartnershipCatalogItem, UUID>,
        JpaSpecificationExecutor<PartnershipCatalogItem> {

    boolean existsByCode(String code);

    Optional<PartnershipCatalogItem> findByCode(String code);

    Optional<PartnershipCatalogItem> findByPartnershipIdAndIngredientIdAndStatus(
            UUID partnershipId, UUID ingredientId, CatalogItemStatus status);
}
