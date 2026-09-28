# Screen Spec — Meal History

**Actor**: Parent | **UC liên quan**: UC-133 - View Meal History | **Status**: Draft
**BR liên quan**: BR-031, BR-069, BR-070, BR-121, BR-133
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Meal History" + Date Range Filter | — | |
| 2 | Calendar/Date Filter | `DatePicker` | Lọc theo ngày/khoảng ngày | BR-133 | |
| 3 | Meal History Table | `Table` | Ngày, tỷ lệ tiêu thụ, loại khẩu phần, trạng thái hài lòng, action "Xem chi tiết" | BR-121, BR-133 | |
| 4 | Weekly Charts Button | Button | → Nutrition History | — | |
| 5 | Special Meals History Button | Button | → Special Meal Records | — | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Không có lịch sử bữa ăn trong khoảng thời gian đã chọn".
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Bữa ăn của con" (`10_Parent_Screen_Flow_Web.md`).
**Bố cục**: Topbar + Sidebar + Content: Page Header + Date Filter + `Table` lịch sử, 2 nút điều hướng góc trên phải.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Meal History, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Page Header + Date Filter + Table lịch sử bữa ăn + 2 nút điều hướng.
Thành phần bắt buộc: Calendar/Date Filter, Meal History Table, Weekly Charts Button, Special Meals History Button.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6161–6178, dòng 3975–3995 (UC-133).
- `business_rules.md` BR-031, BR-069, BR-070, BR-121, BR-133.
