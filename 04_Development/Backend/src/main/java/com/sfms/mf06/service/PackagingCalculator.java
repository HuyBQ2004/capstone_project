package com.sfms.mf06.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Quy đổi số lượng đặt hàng theo quy cách đóng gói của hợp đồng (PROPOSED — chưa có
 * BR/UC/TDS chính thức, xem {@code 07_implement/MF04_Supplier_Partner_Management/
 * Proposal-Partnership-Catalog-Contract.md} §Phase 2).
 *
 * <p>Ba tình huống thực tế: (1) thùng chỉ là bao bì vận chuyển, NCC tự tách bán lẻ —
 * hệ thống nhận đúng số lượng yêu cầu ({@code allowPartialPackage = true}); (2) sản phẩm
 * bắt buộc bán nguyên kiện (sữa hộp, dầu ăn đóng can) — số lượng đặt phải làm tròn LÊN
 * bội số {@code packagingSize} ({@code allowPartialPackage = false}); (3) đơn vị đặt khác
 * đơn vị đóng gói nhưng NCC tự quy đổi nội bộ — cùng xử lý như (1), hệ thống không cần
 * phân biệt Case 1/3.
 */
public final class PackagingCalculator {

    private PackagingCalculator() {
    }

    /**
     * @param requestedQuantity số lượng trường thực sự cần
     * @param packagingSize     số lượng/1 kiện theo cùng đơn vị; {@code null} = không quy đổi
     * @param allowPartialPackage {@code false} = bắt buộc làm tròn lên bội số {@code packagingSize}
     * @return số lượng THỰC ĐẶT sau quy đổi — bằng {@code requestedQuantity} nếu không cần quy đổi
     */
    public static BigDecimal roundedQuantity(BigDecimal requestedQuantity, BigDecimal packagingSize,
                                              boolean allowPartialPackage) {
        if (requestedQuantity == null) {
            return null;
        }
        if (allowPartialPackage || packagingSize == null || packagingSize.signum() <= 0) {
            return requestedQuantity;
        }
        BigDecimal packages = requestedQuantity.divide(packagingSize, 0, RoundingMode.CEILING);
        return packages.multiply(packagingSize);
    }
}
