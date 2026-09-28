package com.sfms.common.unit;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Tra cứu/quy đổi đơn vị đo lường — PROPOSED, xem plan thiết kế "Đơn vị đo lường".
 * Dùng chung cho mọi module cần validate {@code unit} hoặc quy đổi CHÍNH XÁC
 * (kg↔g, lít↔ml). KHÔNG xử lý quy đổi đếm↔khối lượng (bó↔gam) — việc đó thuộc về
 * NCC ({@code SupplierProduct.conversionToBase}), không phải quy đổi vật lý phổ quát.
 */
@Component
public class MeasureUnitService {

    private final MeasureUnitRepository repository;

    public MeasureUnitService(MeasureUnitRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public boolean isValidUnit(String code) {
        if (code == null || code.isBlank()) return false;
        String normalized = java.text.Normalizer.normalize(code, java.text.Normalizer.Form.NFC);
        return repository.existsById(normalized);
    }

    /**
     * Quy đổi CHÍNH XÁC {@code qty} từ {@code fromUnit} sang {@code toUnit} — chỉ khi
     * cùng {@code family} (WEIGHT hoặc VOLUME) và cả hai có {@code factorToBase}.
     *
     * @return {@code Optional.empty()} nếu không quy đổi được (đơn vị không hợp lệ,
     *         khác family, hoặc là đơn vị đếm COUNT — COUNT không có quy đổi phổ quát,
     *         xem javadoc lớp).
     */
    @Transactional(readOnly = true)
    public Optional<BigDecimal> convertExact(BigDecimal qty, String fromUnit, String toUnit) {
        if (qty == null || fromUnit == null || toUnit == null) {
            return Optional.empty();
        }
        String normFrom = java.text.Normalizer.normalize(fromUnit, java.text.Normalizer.Form.NFC);
        String normTo = java.text.Normalizer.normalize(toUnit, java.text.Normalizer.Form.NFC);
        if (normFrom.equals(normTo)) {
            return Optional.of(qty);
        }
        Optional<MeasureUnit> from = repository.findById(normFrom);
        Optional<MeasureUnit> to = repository.findById(normTo);
        if (from.isEmpty() || to.isEmpty()) {
            return Optional.empty();
        }
        if (!from.get().getFamily().equals(to.get().getFamily())) {
            return Optional.empty();
        }
        BigDecimal fromFactor = from.get().getFactorToBase();
        BigDecimal toFactor = to.get().getFactorToBase();
        if (fromFactor == null || toFactor == null) {
            return Optional.empty();
        }
        return Optional.of(qty.multiply(fromFactor).divide(toFactor, 6, RoundingMode.HALF_UP));
    }
}
