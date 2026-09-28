# Screen Spec — Meal History Detail

**Actor**: Parent | **UC liên quan**: UC-133 - View Meal History (bước 4-5) | **Status**: Draft
**BR liên quan**: BR-065, BR-069, BR-121
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | Ngày được chọn | — | |
| 2 | Served Dish Table | `Table`/List | Tên món, nguyên liệu, calo | BR-069 | |
| 3 | Consumption Status Badge | Badge | Fully/Partially/Not Consumed | BR-121 | |
| 4 | Serving Timestamp | Text | Giờ phục vụ | BR-121 | |
| 5 | Special Meal Applied Note | Text (điều kiện) | Ghi chú bữa ăn đặc biệt | BR-065 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Error: `Alert variant="destructive"` + MSG-004/005.
- Empty/Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Meal History (click hàng bảng/chọn ngày).
**Bố cục**: Topbar + Sidebar + Content: Page Header (ngày) + Table món ăn + Badge trạng thái + timestamp.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Meal History Detail, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Page Header + Table món ăn + Badge trạng thái tiêu thụ + timestamp.
Thành phần bắt buộc: Served Dish Table, Consumption Status Badge, Serving Timestamp, Special Meal Applied Note.
Trạng thái cần thể hiện: loading, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3975–3995 (UC-133).
- `business_rules.md` BR-065, BR-069, BR-121.
