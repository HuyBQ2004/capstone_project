# Screen Spec — Allergy Information

**Actor**: Teacher | **UC liên quan**: UC-145 - View Student Health & Allergy Info; UC-103 - View Allergy Alerts | **Status**: Draft
**BR liên quan**: BR-030 (dữ liệu dị ứng học sinh), BR-123 (cảnh báo dị ứng bắt buộc màu đỏ — BR-123 Design System)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú**: màn hình dùng chung 3 actor (Teacher/Medical Staff/Parent — xem `14_Screen_Authorization_Matrix.md` khối 4). Bản dưới đây là bản **Teacher** — chỉ đọc (read-only), không có action chỉnh sửa (khác Parent có thể Submit/Update, khác Medical Staff có thể xác nhận/review). Field hiển thị dữ liệu cần hợp nhất với bản Medical Staff/Parent ở bước review cuối (mục 5–6 của `00_Team_Work_Assignment_Plan.md`).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Identity Header | Text | Họ tên, lớp | BR-030 | |
| 2 | Allergy Profile List | List/Card | Mỗi dòng: tên allergen, mức độ nghiêm trọng (Mild/Moderate/Severe) | BR-030 | Severe hiển thị màu `danger` bắt buộc (BR-123, `Design_System.md` mục 3) |
| 3 | Verification Status Badge | Badge | "Đã xác nhận bởi Medical Staff" (`success`) / "Chờ xác nhận" (`warning`) | BR-031 | |
| 4 | Read-only Notice | Text/Banner | "Chỉ Medical Staff và Phụ huynh được chỉnh sửa thông tin này" | — | Teacher không có quyền sửa, chỉ xem — theo phạm vi RBAC actor |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Học sinh chưa khai báo dị ứng" (icon lớn + text).
- Error: `SnackBar` `danger` + MSG-004/MSG-005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Student Detail (badge dị ứng) hoặc từ Distribution List (allergy badge tap) — xem `07_Teacher_Screen_Flow.md` ("allergy badge tap → View Student Allergy Info").
**Bố cục**: AppBar (back) → Student Identity Header → Allergy Profile List (Card mỗi allergen) → Verification Status Badge → Read-only Notice cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Allergy Information, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Student Identity Header + danh sách Card allergen (mức độ nghiêm trọng) + Badge xác nhận + banner read-only.
Thành phần bắt buộc: Student Identity Header, Allergy Profile List, Verification Status Badge, Read-only Notice.
Áp dụng Design System: Severe = danger (đỏ, BR-123), Moderate = warning, Mild = info.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/MSG-005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên. KHÔNG thêm nút chỉnh sửa (Teacher chỉ đọc).
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md` ("View Student Allergy Info (read-only)").
- `traceability_matrix.md` §1.3 UC-145, §1.9 UC-103.
- `business_rules.md` BR-030, BR-031, BR-123; `Design_System.md` mục 3 (BR-123).
