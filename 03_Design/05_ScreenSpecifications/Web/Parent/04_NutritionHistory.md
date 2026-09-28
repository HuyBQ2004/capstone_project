# Screen Spec — Nutrition History

**Actor**: Parent | **UC liên quan**: — (suy ra, xem ghi chú bản Mobile) | **Status**: Draft
**BR liên quan**: BR-062, BR-121, BR-133
**MSG liên quan**: MSG-004, MSG-005

> [Giả định — cần team xác nhận cấu trúc chi tiết]. Xem ghi chú nguồn đầy đủ ở bản Mobile cùng tên.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Nutrition History" | — | |
| 2 | Date Range Filter | Filter (Tuần/Tháng) | Chọn khoảng thời gian | — | [Giả định] |
| 3 | Nutrition Trend Chart | Chart (`recharts`) | Biểu đồ calo/nhóm chất theo ngày | BR-062 | |
| 4 | Nutrition Target Comparison Card | Card | So sánh thực tế vs mục tiêu | BR-062 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Chưa đủ dữ liệu bữa ăn để hiển thị biểu đồ".
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Thực đơn & Dinh dưỡng" hoặc từ Weekly Menu/Meal History.
**Bố cục**: Topbar + Sidebar + Content: Date Range Filter + Chart (col-span-8) + Nutrition Target Comparison Card (col-span-4).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Nutrition History, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Date Range Filter + Chart dinh dưỡng + Card so sánh mục tiêu.
Thành phần bắt buộc: Date Range Filter, Nutrition Trend Chart, Nutrition Target Comparison Card.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/10_Parent_Screen_Flow_Web.md`.
- `business_rules.md` BR-062, BR-121, BR-133.
