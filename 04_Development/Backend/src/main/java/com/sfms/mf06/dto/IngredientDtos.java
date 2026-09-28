package com.sfms.mf06.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** DTO của {@code /ingredients} (M06-D04) — danh mục nguyên liệu dùng chung toàn hệ thống. */
public final class IngredientDtos {

    private IngredientDtos() {
    }

    @Schema(name = "IngredientResponse")
    public record IngredientResponse(
            @Schema(description = "D1 — mã nguyên liệu, VD 'ING-042'") String id,
            String name,
            String unit,
            String storageZone,
            @Schema(description = "Tỉ lệ hao hụt sơ chế [0,1) — dùng tính lượng nhập cần mua") BigDecimal refuseFactor,
            @Schema(description = "BR-063 (V74, 13/08/2026) — tác nhân dị ứng của nguyên liệu này. "
                    + "NGUỒN SỰ THẬT để dẫn xuất allergen của MÓN; trước đây chỉ frontend đoán theo "
                    + "từ khoá trong tên nên server luôn báo ít xung đột hơn thực tế.")
            List<String> allergens
    ) {}

    @Schema(name = "IngredientCreateRequest")
    public record CreateRequest(
            /*
             * KHÔNG @NotBlank (sửa 08/08/2026). Cả ba nơi đều nói mã là TUỲ CHỌN — Javadoc
             * ngay dưới đây, nhãn ở màn `system-admin/ingredients` ("Mã nguyên liệu (tuỳ
             * chọn) — để trống tự sinh"), và nhánh `codeGenerator.next(...)` trong
             * `IngredientServiceImpl.create()`. Nhưng `@NotBlank` chặn request TRƯỚC khi tới
             * service, nên nhánh tự sinh là CODE CHẾT và System Admin buộc phải tự nghĩ mã.
             * Đo 08/08/2026: `POST /ingredients` không kèm `code` → 422 "must not be blank".
             */
            @Size(max = 64)
            @Schema(description = "Mã nguyên liệu duy nhất, VD 'ING-HANH-TAY'. Bỏ trống để tự sinh.") String code,
            @NotBlank @Size(max = 180) String name,
            @NotBlank @Size(max = 12)
            @Schema(description = "Đơn vị tính: kg, lít, quả, hộp…") String unit,
            @NotNull
            @Schema(description = "Khu lưu trữ: DRY, CHILLED, FROZEN") String storageZone,
            @DecimalMin("0.000") @DecimalMax("0.999")
            @Schema(description = "Hao hụt sơ chế [0.000–0.999], mặc định 0") BigDecimal refuseFactor,
            @Schema(description = "BR-063 — tác nhân dị ứng, mảng chuỗi khớp danh mục ALLERGENS. "
                    + "Bỏ trống = không có tác nhân nào.")
            List<String> allergens
    ) {}

    @Schema(name = "IngredientUpdateRequest")
    public record UpdateRequest(
            @Size(max = 180) String name,
            @Size(max = 12) String unit,
            @Schema(description = "Khu lưu trữ: DRY, CHILLED, FROZEN") String storageZone,
            @DecimalMin("0.000") @DecimalMax("0.999") BigDecimal refuseFactor,
            @Schema(description = "false = vô hiệu hoá (ẩn khỏi danh sách chọn, không xoá cứng)") Boolean active,
            @Schema(description = "BR-063 — tác nhân dị ứng. `null` = giữ nguyên; mảng rỗng = xoá hết.")
            List<String> allergens
    ) {}
}
