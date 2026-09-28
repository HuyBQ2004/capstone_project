package com.sfms.mf06.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Danh mục nguyên liệu dùng chung toàn hệ thống (M06-D04 — lỗi C3 §2.1 Workflow).
 * {@code code} UNIQUE toàn cục là ĐÚNG (không gắn school_id — D1 chỉ đòi UNIQUE(school_id, code)
 * cho dữ liệu THEO TRƯỜNG).
 */
@Entity
@Table(name = "catalog_ingredients", uniqueConstraints = @UniqueConstraint(name = "uq_catalog_code", columnNames = "code"))
public class CatalogIngredient extends BaseEntity {

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(nullable = false, length = 12)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_zone", nullable = false, length = 10)
    private StorageZone storageZone;

    @Column(name = "refuse_factor", nullable = false, precision = 4, scale = 3)
    private BigDecimal refuseFactor = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;

    /**
     * BR-063 (V74, 13/08/2026) — tác nhân dị ứng của nguyên liệu này.
     *
     * <p>Trước cột này, allergen chỉ tồn tại ở hai chỗ đều KHÔNG phải dữ liệu:
     * {@code dishes.manual_allergens} (chỉ là phần khai thêm bằng tay cho nguyên liệu ngoài
     * danh mục) và một bảng từ khoá hardcode ở frontend. Server vì thế luôn tính ra ít xung
     * đột hơn thực tế, và mọi màn cần BR-063 phải tải cả sổ học sinh về trình duyệt để tự tính.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> allergens = new ArrayList<>();

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public StorageZone getStorageZone() { return storageZone; }
    public void setStorageZone(StorageZone storageZone) { this.storageZone = storageZone; }
    public BigDecimal getRefuseFactor() { return refuseFactor; }
    public void setRefuseFactor(BigDecimal refuseFactor) { this.refuseFactor = refuseFactor; }
    public List<String> getAllergens() { return allergens; }
    public void setAllergens(List<String> allergens) {
        this.allergens = allergens == null ? new ArrayList<>() : allergens;
    }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
