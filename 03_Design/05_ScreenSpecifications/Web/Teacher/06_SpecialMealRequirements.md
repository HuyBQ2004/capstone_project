# Screen Spec — Special Meal Requirements

**Actor**: Teacher | **UC liên quan**: UC-143 - View Special Meal Requirements | **Status**: Draft
**BR liên quan**: BR-125, BR-065, BR-066, BR-082
**MSG liên quan**: MSG-004, MSG-005

> Bản Web, chỉ đọc — dùng chung field/BR/MSG với bản Mobile.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Special Meal Requirements — Class [Tên lớp]" | BR-125 | |
| 2 | Special Meal Student Table | `Table` | Cột: Họ tên, loại chế độ ăn, lý do, trạng thái duyệt | BR-065, BR-066 | |
| 3 | Approval Status Badge | Badge | success/warning | BR-065 | |
| 4 | Preparation Note | Text (chỉ đọc) | Ghi chú chế biến riêng | BR-082 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Lớp không có học sinh cần chế độ ăn đặc biệt".
- Error: `Alert variant="destructive"` + MSG-004/MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Phân phát Bữa ăn" (theo `12_Teacher_Screen_Flow_Web.md`), hoặc từ Student Detail.
**Bố cục**: Topbar + Sidebar + Content: Page Header + `Table` danh sách học sinh chế độ ăn đặc biệt.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Special Meal Requirements, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Page Header + Table (tên, loại chế độ ăn, lý do, trạng thái duyệt).
Thành phần bắt buộc: Special Meal Student Table, Approval Status Badge, Preparation Note.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/MSG-005).
KHÔNG thêm nút chỉnh sửa.
```

## Nguồn dữ liệu
- `traceability_matrix.md` §1.9 UC-143.
- `business_rules.md` BR-065, BR-066, BR-082, BR-125.
- `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md`.
