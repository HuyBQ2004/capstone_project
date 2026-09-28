package com.sfms.mf06.service;

import com.sfms.mf06.domain.InventoryBatch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ADR-M06-4 — hàm THUẦN, không phụ thuộc Spring/DB: lọc lô khoá khỏi tập ứng viên rồi sắp
 * theo {@code expiryDate} tăng dần (BR-075), lấy dần tới khi đủ. Phần còn thiếu trả
 * {@code shortfall > 0} (BR-077/MSG-018).
 */
public final class FefoAllocator {

    private FefoAllocator() {
    }

    public record Allocation(InventoryBatch batch, BigDecimal take) {}

    public record Result(List<Allocation> allocations, BigDecimal shortfall) {}

    /**
     * @param candidates  lô của MỘT nguyên liệu (đã lọc theo school/ingredient)
     * @param available   hàm tính tồn khả dụng runtime của một lô (ADR-M06-1)
     * @param needed      nhu cầu cần xuất
     * @param today       "hôm nay" — LUÔN qua tham số, cấm {@code LocalDate.now()} (FX-CLOCK)
     */
    public static Result allocate(List<InventoryBatch> candidates,
                                   java.util.function.Function<InventoryBatch, BigDecimal> available,
                                   BigDecimal needed, LocalDate today) {
        List<InventoryBatch> sorted = candidates.stream()
                .filter(b -> !b.isLocked(today))
                .sorted(Comparator.comparing(InventoryBatch::getExpiryDate))
                .toList();

        List<Allocation> allocations = new ArrayList<>();
        BigDecimal remaining = needed;
        for (InventoryBatch batch : sorted) {
            if (remaining.signum() <= 0) {
                break;
            }
            BigDecimal batchAvailable = available.apply(batch);
            if (batchAvailable.signum() <= 0) {
                continue;
            }
            BigDecimal take = batchAvailable.min(remaining);
            allocations.add(new Allocation(batch, take));
            remaining = remaining.subtract(take);
        }
        BigDecimal shortfall = remaining.signum() > 0 ? remaining : BigDecimal.ZERO;
        return new Result(allocations, shortfall);
    }
}
