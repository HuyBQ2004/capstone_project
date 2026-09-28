# Screen Spec — Special Meal Records

**Actor**: Parent | **UC liên quan**: UC-142 - View Special Meal Records | **Status**: Draft
**BR liên quan**: BR-065 (kế hoạch bữa ăn đặc biệt), BR-123 (xác nhận kép), BR-121 (theo dõi tiêu thụ)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: đích điều hướng của nút "Special Meals History" trên Meal History Log (Report 3 dòng 6175) — không có wireframe field-by-field riêng, suy ra từ UC-142 Normal Flow.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "Special Meal Records" | — | |
| 2 | Special Meal Record Card | Card list (1 phiên/card) | Ngày, món gốc, món thay thế, allergen tránh, badge xác nhận kép | BR-065, BR-123 | UC-142 bước 3 |
| 3 | Record Detail (khi tap card) | Expand/Sheet | Ghi chú chuẩn bị, timestamp Kitchen release, xác nhận Teacher hand-off, trạng thái tiêu thụ (Fully/Partially/Not Consumed) | BR-121, BR-123 | UC-142 bước 4-5 |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Chưa có lịch sử bữa ăn đặc biệt" (UC-142 AF1).
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Meal History ("Special Meals History" button).
**Bố cục**: AppBar (back) → danh sách `Card` phiên bữa ăn đặc biệt cuộn dọc → tap mở chi tiết dạng bottom sheet.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Special Meal Records, nền tảng Mobile, actor Parent.
Bố cục: AppBar + danh sách Card phiên bữa ăn đặc biệt (ngày, món gốc/thay thế, badge xác nhận kép) + bottom sheet chi tiết khi tap.
Thành phần bắt buộc: Special Meal Record Card, Record Detail (ghi chú, timestamp, trạng thái tiêu thụ).
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4019–4039 (UC-142 Normal/Alternative Flow).
- `03_Design/04_ScreenFlow/05_Parent_Screen_Flow.md` ("Special Meals History (≈ Special Meal Records)").
- `business_rules.md` BR-065, BR-121, BR-123.
