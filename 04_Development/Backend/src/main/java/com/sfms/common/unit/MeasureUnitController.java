package com.sfms.common.unit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * {@code /api/v1/measure-units} — danh mục đơn vị đo lường TOÀN HỆ THỐNG, chỉ đọc.
 *
 * <p>Thêm 08/08/2026 (backlog D5). Bảng {@code measure_units} có từ V51 và backend đã dùng nó
 * để validate ({@code MeasureUnitService.isValidUnit}), nhưng KHÔNG có controller ⇒ FE gọi
 * {@code /measure-units} nhận 404 nên phải khai cứng bảng {@code UNIT_FACTOR} chỉ 4 đơn vị
 * (g/kg/ml/lít), nhân đôi 13 dòng của DB.
 *
 * <p>Vì sao đáng sửa chứ không phải "hôm nay vẫn chạy": 9 đơn vị còn lại đều là COUNT nên chưa
 * lộ, nhưng thêm MỘT đơn vị WEIGHT/VOLUME mới vào {@code measure_units} (VD {@code tấn}) là FE
 * không biết hệ số ⇒ {@code toBaseQuantity()} trả nguyên lượng ⇒ tính sai IM LẶNG. Đây là lần
 * thứ 4 của cùng lớp lỗi "hằng số nhân đôi" (sau {@code NUTRITION_TARGET},
 * {@code menuLeadTimeDays}, {@code STANDARD_DIETS}) — cách sửa đúng đã có tiền lệ: cho FE đọc
 * bảng thật từ API.
 *
 * <p>Mở cho MỌI vai đã đăng nhập: danh mục tham chiếu tĩnh, không school-scoped, không PII —
 * cùng lập luận với {@code DietGroupController}. Không có endpoint GHI (dữ liệu cấu hình hệ
 * thống, đổi qua migration).
 */
@Tag(name = "Common — Measure Units")
@RestController
@RequestMapping("/api/v1/measure-units")
public class MeasureUnitController {

    private final MeasureUnitRepository repository;

    public MeasureUnitController(MeasureUnitRepository repository) {
        this.repository = repository;
    }

    @Schema(name = "MeasureUnitResponse", description = "Một đơn vị đo hợp lệ của hệ thống")
    public record MeasureUnitResponse(
            @Schema(description = "Mã đơn vị, dùng nguyên văn trong mọi payload — VD \"kg\", \"ml\", \"bó\"")
            String code,
            @Schema(description = "WEIGHT | VOLUME | COUNT") String family,
            // Đo thật 08/08/2026: kg=1.0, g=0.001, lít=1.0, ml=0.001 ⇒ đơn vị cơ sở là
            // kg/lít, KHÔNG phải g/ml. Khớp `catalog_ingredients.unit` (kho khai bằng kg).
            @Schema(description = "Hệ số quy đổi về đơn vị cơ sở của family (kg cho WEIGHT, lít cho VOLUME). "
                    + "NULL với COUNT — đếm KHÔNG có quy đổi phổ quát, tuỳ nhà cung cấp "
                    + "(xem SupplierProduct.conversionToBase). FE gặp NULL thì đừng đoán hệ số.")
            BigDecimal factorToBase) {}

    @Operation(summary = "Danh mục đơn vị đo lường",
            description = "Chỉ đọc, toàn hệ thống. Nguồn sự thật DUY NHẤT cho quy đổi đơn vị ở FE — "
                    + "đừng khai lại bảng hệ số trong code client.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Transactional(readOnly = true)
    public List<MeasureUnitResponse> list() {
        return repository.findAll(Sort.by("family", "code")).stream()
                .map(u -> new MeasureUnitResponse(u.getCode(), u.getFamily(), u.getFactorToBase()))
                .toList();
    }
}
