# Screen Spec — Meal History Detail

**Actor**: Parent | **UC liên quan**: UC-133 - View Meal History (bước 4-5) | **Status**: Draft
**BR liên quan**: BR-069 (khai báo nguyên liệu/dị ứng), BR-121 (theo dõi lịch sử bữa ăn)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: không có wireframe riêng trong Report 3 — suy ra từ UC-133 bước 4 "Parent selects a specific date entry to view detailed information" và bước 5 "displays the detailed meal record with dish names, ingredients, calorie information, and serving timestamp".

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Date Header | Text | Ngày được chọn | — | |
| 2 | Served Dish List | List/Card | Tên món, nguyên liệu, thông tin calo | BR-069 | UC-133 bước 5 |
| 3 | Consumption Status Badge | Badge | Fully/Partially/Not Consumed | BR-121 | |
| 4 | Serving Timestamp | Text | Giờ phục vụ | BR-121 | |
| 5 | Special Meal Applied Note | Text (điều kiện) | Ghi chú nếu có áp dụng kế hoạch bữa ăn đặc biệt | BR-065 | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: không áp dụng (luôn có dữ liệu khi chọn từ Calendar Strip).
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Meal History (chọn ngày trên Calendar Strip).
**Bố cục**: AppBar (back, hiển thị ngày) → Served Dish List → Consumption Status Badge + Serving Timestamp → Special Meal Applied Note (nếu có).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Meal History Detail, nền tảng Mobile, actor Parent.
Bố cục: AppBar (ngày) + danh sách món ăn (tên, nguyên liệu, calo) + badge trạng thái tiêu thụ + timestamp phục vụ + ghi chú bữa ăn đặc biệt.
Thành phần bắt buộc: Date Header, Served Dish List, Consumption Status Badge, Serving Timestamp, Special Meal Applied Note.
Trạng thái cần thể hiện: loading, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3975–3995 (UC-133 Normal Flow bước 4-5).
- `business_rules.md` BR-065, BR-069, BR-121.
