# Screen Spec — Campus Config — ❌ ĐÃ LOẠI BỎ (2026-07-01)

**Trạng thái**: **KHÔNG triển khai** — theo xác nhận trực tiếp của chủ dự án: "Hệ thống của tôi không quản lý campus" (hệ thống chỉ quản lý 1 tọa độ/1 trường, không có khái niệm nhiều cơ sở/multi-campus dưới 1 trường). File này được giữ lại chỉ để lưu lịch sử quyết định, **không phải nguồn cho Wireframe/Prototype**.

> **Lý do loại bỏ**: Khái niệm "Campus Config" chưa từng có UC gốc trong Report 3/traceability_matrix — nó là suy luận riêng của nhóm thiết kế khi thấy BR-028 có nhắc "bán kính geofence" và tự đặt tên màn hình. Khi đối chiếu lại nguyên văn BR-028 (`business_rules.md`): *"School Administration có thể cấu hình bán kính này trong khoảng 50m – 500m tùy điều kiện thực tế"* — BR-028 xác nhận rõ **School Administration** là chủ sở hữu cấu hình bán kính geofence, không phải System Admin, và văn bản BR-028 hoàn toàn không có ngữ nghĩa "nhiều campus/cơ sở" — chỉ nói về geofence của **1 trường** duy nhất. Kết hợp với xác nhận của chủ dự án, màn hình "Campus Config" (ở cấp System Admin) không có lý do tồn tại.
>
> **Chuyển tới đâu**: field "bán kính geofence" nay đã được xác nhận chính thức tại `Web/SchoolAdministration/37_SchoolConfiguration.md` (School Administration, đúng theo BR-028). System Admin chỉ còn giữ vai trò cấu hình **tọa độ GPS (lat/long) + khoá/mở khoá tọa độ** trong `12_UpdateSchoolScreenDialog.md` (UC-095) — không còn field bán kính geofence ở phía System Admin.

## AI Generation Prompt ⭐

```
[KHÔNG tạo wireframe cho màn hình này — đã loại bỏ 2026-07-01, không tồn tại trong hệ thống thật. Xem 37_SchoolConfiguration.md (School Administration) cho field bán kính geofence.]
```

## Nguồn dữ liệu
- Xác nhận trực tiếp từ chủ dự án (2026-07-01): hệ thống không quản lý campus.
- `business_rules.md` BR-028 (nguyên văn xác nhận School Administration sở hữu cấu hình bán kính geofence, không có ngữ nghĩa multi-campus).
- `Web/SchoolAdministration/37_SchoolConfiguration.md` (nơi field này được chuyển tới, đã CONFIRMED).
- `Web/SystemAdmin/12_UpdateSchoolScreenDialog.md` (đã gỡ field bán kính geofence khỏi phía System Admin).
