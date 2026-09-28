package com.sfms.mf02.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.List;

/** DTO của {@code /schools/{code}/config} (UC-095). */
public final class SchoolConfigDtos {

    private SchoolConfigDtos() {
    }

    @Schema(name = "UpdateSchoolConfigRequest")
    public record UpdateSchoolConfigRequest(
            BigDecimal costCeiling,

            @Min(50) @Max(500)
            @Schema(description = "BR-028 — geofence [50,500]m, mặc định 200") Integer geofenceRadius,

            List<String> reportEmails
    ) {}

    @Schema(name = "SchoolConfigResponse")
    public record SchoolConfigResponse(
            @Schema(description = "BR-080 — miền MF05, chỉ tham chiếu") BigDecimal costCeiling,
            int geofenceRadius,
            List<String> reportEmails,
            /*
             * BR-061 — số ngày tối thiểu trước ngày phục vụ phải nộp duyệt thực đơn.
             *
             * KHÔNG lưu ở `school_config`: đây là cấu hình TRIỂN KHAI
             * (`sfms.menu.lead-time-days`, env `MENU_LEAD_TIME_DAYS`), không phải tham số
             * riêng của từng trường. Trả kèm ở đây vì FE cần đúng con số đó để khoá ô lịch,
             * và endpoint này đã mở cho SERVICE_CATERERS — vai lập thực đơn.
             *
             * Thêm 08/08/2026: trước đó FE hardcode `+3` ngay trong `menus/page.tsx`, nên
             * đổi cấu hình server KHÔNG có tác dụng gì lên UI — cùng lớp lỗi "hằng số nhân
             * đôi" đã bỏ ở `NUTRITION_TARGET`. Đặt `MENU_LEAD_TIME_DAYS=0` là tạo/nộp được
             * thực đơn NGAY TRONG NGÀY (dùng khi demo/test).
             */
            @Schema(description = "BR-061 — số ngày tối thiểu trước ngày phục vụ; 0 = cho phép nộp trong ngày",
                    example = "3")
            int menuLeadTimeDays,

            /*
             * 13/08/2026 (theo Nam) — CHỈ ĐỌC: toạ độ GPS/địa chỉ do System Admin quản lý
             * (BR-028, `PUT /schools/{code}` riêng), thêm vào đây để School Admin XEM được
             * bản đồ vị trí tại chính màn cấu hình của mình thay vì phải nhớ sang màn khác —
             * không phải field ghi, không đi qua `UpdateSchoolConfigRequest`.
             */
            @Schema(description = "Chỉ đọc — sửa ở System Admin › Hồ sơ trường (BR-028)") String address,
            @Schema(description = "Chỉ đọc — null nếu trường chưa khai toạ độ") BigDecimal lat,
            @Schema(description = "Chỉ đọc — null nếu trường chưa khai toạ độ") BigDecimal lng
    ) {}
}
