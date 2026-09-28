# Screen Spec — Allergy Information

**Actor**: Teacher | **UC liên quan**: UC-145 - View Student Health & Allergy Info; UC-103 - View Allergy Alerts | **Status**: Draft
**BR liên quan**: BR-030, BR-123
**MSG liên quan**: MSG-004, MSG-005

> Bản Web — chỉ đọc, dùng chung field/BR/MSG với bản Mobile cùng tên. Xem ghi chú hợp nhất đa-actor ở bản Mobile.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Identity Header | Text | Họ tên, lớp | BR-030 | |
| 2 | Allergy Profile Table/List | Table hoặc Card list | Allergen, mức độ nghiêm trọng | BR-030 | Severe = `danger` (BR-123) |
| 3 | Verification Status Badge | Badge | "Đã xác nhận"/"Chờ xác nhận" | BR-031 | |
| 4 | Read-only Notice | Alert (info) | "Chỉ Medical Staff/Phụ huynh chỉnh sửa được" | — | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Học sinh chưa khai báo dị ứng".
- Error: `Alert variant="destructive"` + MSG-004/MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Student Detail (tab Allergy). Không có action ghi (Teacher chỉ đọc).
**Bố cục**: Topbar + Sidebar + Content: Student Identity Header + bảng/list allergen (col-span-8) + badge xác nhận + banner thông báo quyền hạn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Allergy Information, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Student Identity Header + Table/Card list allergen + Badge xác nhận + Alert read-only.
Thành phần bắt buộc: Student Identity Header, Allergy Profile Table, Verification Status Badge, Read-only Notice.
Áp dụng Design System: Severe = danger, Moderate = warning, Mild = info.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/MSG-005).
KHÔNG thêm nút chỉnh sửa.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md`.
- `traceability_matrix.md` §1.3 UC-145, §1.9 UC-103.
- `business_rules.md` BR-030, BR-031, BR-123.
