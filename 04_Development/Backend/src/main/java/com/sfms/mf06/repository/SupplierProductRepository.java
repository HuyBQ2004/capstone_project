package com.sfms.mf06.repository;

import com.sfms.mf06.domain.SupplierProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierProductRepository extends JpaRepository<SupplierProduct, UUID>,
        JpaSpecificationExecutor<SupplierProduct> {

    Optional<SupplierProduct> findByPartnerIdAndCode(UUID partnerId, String code);

    /** Dùng cho {@code GET /supplier-products/sourcing} — panel "Nhà cung cấp" của Phụ huynh. */
    List<SupplierProduct> findByIngredientIdIn(Collection<UUID> ingredientIds);
}
