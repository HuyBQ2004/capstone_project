package com.sfms.common.unit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Bảng tra cứu đơn vị đo lường hợp lệ, dùng chung toàn hệ thống (PROPOSED — xem plan
 * thiết kế "Đơn vị đo lường"). Dữ liệu tĩnh (seed migration), không extends
 * {@code BaseEntity} — cùng khuôn {@code code_sequences} (bảng cấu hình, không phải
 * bản ghi nghiệp vụ cần audit).
 *
 * <p>{@code factorToBase} CHỈ có giá trị cho {@code family} WEIGHT/VOLUME (quy đổi
 * CHÍNH XÁC, vật lý bất biến). {@code COUNT} luôn {@code null} — quy đổi đếm↔khối lượng
 * (VD "1 bó ≈ 300g") KHÔNG PHẢI hằng số chung, tùy nhà cung cấp — xem
 * {@code SupplierProduct.conversionToBase}.
 */
@Entity
@Table(name = "measure_units")
public class MeasureUnit {

    @Id
    @Column(length = 16)
    private String code;

    @Column(nullable = false, length = 10)
    private String family;

    @Column(name = "factor_to_base", precision = 12, scale = 6)
    private BigDecimal factorToBase;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getFamily() { return family; }
    public void setFamily(String family) { this.family = family; }
    public BigDecimal getFactorToBase() { return factorToBase; }
    public void setFactorToBase(BigDecimal factorToBase) { this.factorToBase = factorToBase; }
}
