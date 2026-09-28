# Screen Spec — Special Meal Arrangements

**Actor**: Parent | **UC liên quan**: UC-141 - View Special Meal Arrangements | **Status**: Draft
**BR liên quan**: BR-031, BR-065, BR-123
**MSG liên quan**: MSG-004, MSG-005, MSG-051

> Hợp nhất với "Allergy Conflict Alert" — xem ghi chú đầy đủ ở bản Mobile cùng tên.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Special Meal Arrangements" | — | |
| 2 | Allergen Warning Card | Alert (destructive) | Mô tả xung đột | BR-123 | MSG-051 |
| 3 | Special Meal Detail Table | `Table`/Card | Allergen, mức độ, món thay thế, ghi chú | BR-065 | |
| 4 | Dual-Confirmation Status | Badge x2 | Kitchen Released / Teacher Hand-off | BR-123 | |
| 5 | Parental Advisory Note | Text (italic) | Ghi chú khuyến cáo | — | |
| 6 | Submit Allergy Information Link | Link (điều kiện) | → Submit Allergy Information | — | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Học sinh chưa có kế hoạch bữa ăn đặc biệt nào" + link khai báo.
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Sức khỏe & Dị ứng" (`10_Parent_Screen_Flow_Web.md`), từ Dashboard/Weekly Menu.
**Bố cục**: Topbar + Sidebar + Content: Allergen Warning Alert (full-width) + Table chi tiết bữa ăn đặc biệt + 2 Badge xác nhận kép.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Special Meal Arrangements, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Allergen Warning Alert + Table chi tiết + 2 Badge xác nhận kép + ghi chú khuyến cáo.
Thành phần bắt buộc: Allergen Warning Card, Special Meal Detail Table, Dual-Confirmation Status, Parental Advisory Note.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005), warning (MSG-051).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4001–4017 (UC-141), dòng 6088–6103.
- `business_rules.md` BR-031, BR-065, BR-123; `error_message.md` MSG-051.
