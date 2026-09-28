# Screen Spec — Nutrition History

**Actor**: Parent | **UC liên quan**: — (không có UC riêng trong `traceability_matrix.md`; suy ra từ nút "View Nutrition Details"/"Weekly Charts") | **Status**: Draft
**BR liên quan**: BR-062 (mục tiêu dinh dưỡng theo nhóm tuổi), BR-121 (theo dõi lịch sử bữa ăn), BR-133 (tóm tắt bữa ăn hàng ngày)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: không có wireframe field-by-field riêng trong Report 3 cho "Nutrition History" — theo `05_Parent_Screen_Flow.md`, đây là màn đích của nút "Weekly Charts" trên Meal History Log (ghi chú "≈ Nutrition History") và nút "View Nutrition Details" trên Weekly Menu. Field dưới đây suy ra từ mục đích hiển thị biểu đồ dinh dưỡng tuần/tháng dựa trên BR-062, không có field-by-field gốc. **[Giả định — cần team xác nhận cấu trúc chi tiết]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "Nutrition History" | — | |
| 2 | Date Range Filter | Filter (Tuần/Tháng) | Chọn khoảng thời gian xem biểu đồ | — | [Giả định] |
| 3 | Nutrition Trend Chart | Chart (`fl_chart`) | Biểu đồ calo/nhóm chất theo ngày, đối chiếu mục tiêu dinh dưỡng theo tuổi | BR-062 | [Giả định — cần xác nhận loại biểu đồ] |
| 4 | Nutrition Target Comparison Card | Card | So sánh thực tế vs mục tiêu (calo, đạm, chất xơ...) | BR-062 | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Chưa đủ dữ liệu bữa ăn để hiển thị biểu đồ".
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Weekly Menu ("View Nutrition Details") hoặc Meal History ("Weekly Charts") — xem `05_Parent_Screen_Flow.md`.
**Bố cục**: AppBar (back) → Date Range Filter → Nutrition Trend Chart → Nutrition Target Comparison Card.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Nutrition History, nền tảng Mobile, actor Parent.
Bố cục: AppBar + Date Range Filter + biểu đồ xu hướng dinh dưỡng + Card so sánh mục tiêu.
Thành phần bắt buộc: Date Range Filter, Nutrition Trend Chart, Nutrition Target Comparison Card.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/05_Parent_Screen_Flow.md` ("Weekly Charts (≈ Nutrition History)").
- `business_rules.md` BR-062, BR-121, BR-133 (không có UC riêng — xem ghi chú nguồn ở trên).
