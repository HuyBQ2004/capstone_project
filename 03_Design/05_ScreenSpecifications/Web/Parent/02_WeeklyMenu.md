# Screen Spec — Weekly Menu

**Actor**: Parent | **UC liên quan**: UC-135 - View Menu Information | **Status**: Draft
**BR liên quan**: BR-031, BR-062, BR-069, BR-070, BR-075, BR-123
**MSG liên quan**: MSG-004, MSG-005, MSG-051

> Xem ghi chú mâu thuẫn MSG-076 ở bản Mobile cùng tên (không có MSG chính thức cho "chưa có menu published").

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Weekly Days Tab Bar | Tabs | Mon-Fri | UC-135 bước 3 | |
| 2 | Standard Meal Item Card | Card | Tên món, nguyên liệu, tag "Lunch" | BR-069 | |
| 3 | Allergy Alert Meal Card | Card (viền destructive, có điều kiện) | Tên món, allergen, tag cảnh báo | BR-123 | MSG-051 |
| 4 | View Nutrition Details Button | Button (primary) | → Nutrition History | BR-062 | |
| 5 | Download PDF Button | Button (secondary) | Xuất PDF menu | UC-135 bước 6 | [Giả định vị trí UI] |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Không có menu được công bố cho tuần này" [Giả định — không có MSG code chính thức].
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Thực đơn & Dinh dưỡng" (`10_Parent_Screen_Flow_Web.md`).
**Bố cục**: Topbar + Sidebar + Content: Weekly Days Tab Bar + Card grid món ăn (col-span-4/dish) + nút Download PDF góc trên phải + nút View Nutrition Details.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Weekly Menu, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Weekly Days Tab Bar + Card grid món ăn + nút Download PDF + nút View Nutrition Details.
Thành phần bắt buộc: Weekly Days Tab Bar, Standard Meal Item Card, Allergy Alert Meal Card, View Nutrition Details Button, Download PDF Button.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005), warning (MSG-051).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6051–6068, dòng 2999–3018 (UC-135).
- `business_rules.md` BR-031, BR-062, BR-069, BR-070, BR-075, BR-123; `error_message.md` MSG-051.
