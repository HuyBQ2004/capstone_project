# Screen Spec — Special Meal Requirements

**Actor**: Teacher | **UC liên quan**: UC-143 - View Special Meal Requirements | **Status**: Draft
**BR liên quan**: BR-125 (xem yêu cầu bữa ăn đặc biệt khi phục vụ), BR-065 (kế hoạch bữa ăn đặc biệt bắt buộc), BR-066 (danh mục chế độ ăn đặc biệt), BR-082 (chế biến riêng)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: Report 3 không có wireframe riêng cho màn này (không nằm trong 5 Teacher Screens đã đặc tả field-by-field). Field suy ra trực tiếp từ UC-143 Normal Flow + BR-065/BR-066 (danh mục: Halal, Vegetarian, Gluten-Free, Nut-Free...).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "Special Meal Requirements — Class [Tên lớp]" | BR-125 | |
| 2 | Special Meal Student List | Card list (1 học sinh/card) | Họ tên, loại chế độ ăn (Halal/Vegetarian/Gluten-Free/Nut-Free/khác), lý do (dị ứng/tôn giáo) | BR-065, BR-066 | Chỉ hiển thị học sinh có `AllergyProfile`/chế độ ăn đã xác minh |
| 3 | Approval Status Badge | Badge | "Đã duyệt kế hoạch" (`success`)/"Chưa có kế hoạch" (`warning`) | BR-065 | |
| 4 | Preparation Note | Text (chỉ đọc) | Ghi chú chế biến riêng (nếu có) | BR-082 | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Lớp không có học sinh cần chế độ ăn đặc biệt".
- Error: `SnackBar` `danger` + MSG-004/MSG-005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Teacher Home (Dietary Notes Card) hoặc Student Detail (Special Diet Badge).
**Bố cục**: AppBar (back) → Screen Title Header → danh sách `Card` học sinh có chế độ ăn đặc biệt, mỗi card kèm badge trạng thái duyệt.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Special Meal Requirements, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + danh sách Card học sinh (tên, loại chế độ ăn, lý do, badge trạng thái duyệt).
Thành phần bắt buộc: Screen Title Header, Special Meal Student List, Approval Status Badge, Preparation Note.
Áp dụng Design System: success/warning theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/MSG-005).
KHÔNG thêm field/nút chỉnh sửa (Teacher chỉ đọc).
```

## Nguồn dữ liệu
- `traceability_matrix.md` §1.9 UC-143.
- `business_rules.md` BR-065, BR-066, BR-082, BR-125.
- `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md` (suy luận điều hướng).
