# Screen Spec — Student Detail

**Actor**: Teacher | **UC liên quan**: UC-145 - View Student Health & Allergy Info (một phần), UC-056 | **Status**: Draft
**BR liên quan**: BR-030, BR-123
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: không có wireframe gốc Report 3 cho màn này — field suy ra từ UC-145 + điều hướng `12_Teacher_Screen_Flow_Web.md`. Xem ghi chú đầy đủ ở bản Mobile cùng tên.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Summary Card | Card | Avatar, họ tên, lớp, ngày sinh | BR-030 | [Giả định — cần team xác nhận] |
| 2 | Allergy Status Badge | Badge | "Có dị ứng"/"Không dị ứng" | BR-030, BR-123 | Link Allergy Information |
| 3 | Special Diet Badge | Badge | "Chế độ ăn đặc biệt"/ẩn | BR-065 | Link Special Meal Requirements |
| 4 | View Health Information Tab/Link | Tab/Link | Điều hướng Health Information | BR-030 | |
| 5 | View Meal Records Tab/Link | Tab/Link | Điều hướng Meal Records | BR-121 | |
| 6 | Parent Contact Info | Text (chỉ đọc) | Tên phụ huynh liên kết | BR-030 | [Giả định — cần team xác nhận] |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Error: `Alert variant="destructive"` + MSG-004/MSG-005.
- Success/Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Student List (click hàng bảng). Ra: Health Information, Allergy Information, Special Meal Requirements, Meal Records.
**Bố cục**: Topbar + Sidebar + Content: Page Header (tên học sinh) + Student Summary Card (col-span-4) + Tab điều hướng nội bộ (Health/Allergy/Special Meal/Meal Records) theo `Layout_Guidelines.md` mục 1.1.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Student Detail, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Page Header + Student Summary Card + Tab điều hướng (Health/Allergy/Special Meal/Meal Records).
Thành phần bắt buộc: Student Summary Card, Allergy Status Badge, Special Diet Badge, View Health Information Link, View Meal Records Link.
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, error (MSG-004/MSG-005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md`.
- `traceability_matrix.md` §1.3 UC-145, §1.9 UC-056.
- `business_rules.md` BR-030, BR-065, BR-123.
