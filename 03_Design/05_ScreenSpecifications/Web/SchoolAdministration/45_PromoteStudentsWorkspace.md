# Screen Spec — Promote Students Workspace

**Actor**: School Administration | **UC liên quan**: UC-177 (Report 3 §3.2 "Promote Students to Next Academic Year") | **Status**: Draft
**BR liên quan**: BR-194 (lên lớp là hành động tường minh, không tự động, hỗ trợ loại trừ cá nhân)
**MSG liên quan**: MSG-004, MSG-005, MSG-010 (vượt sức chứa lớp đích), MSG-084 (lên lớp hàng loạt thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn khối/lớp nguồn (năm học hiện tại) | Select (nhóm theo Grade Level) | — | — | |
| 2 | Chọn lớp đích (năm học mới) | Select (lớp tương ứng đã sao chép cấu trúc) | Sức chứa lớp đích không được vượt 50 (MSG-010) | BR-025, BR-194 | |
| 3 | Danh sách học sinh bị ảnh hưởng (bulk) | Table + Checkbox từng dòng | Mặc định tất cả được chọn để lên lớp | BR-194 | |
| 4 | Bỏ chọn học sinh lưu ban (loại trừ cá nhân) | Checkbox (uncheck để loại trừ) | Học sinh bị loại trừ giữ nguyên liên kết lớp cũ, cần xử lý thủ công sau (AF1, qua UC-090) | BR-194 | |
| 5 | Nút "Xác nhận lên lớp" | Button (primary) | Disable nếu lớp đích vượt sức chứa sau khi tính | BR-025 | |

### Trạng thái màn hình
- Loading: Button "Xác nhận lên lớp" loading khi xử lý hàng loạt.
- Empty: "Chưa chọn khối/lớp nguồn — chọn để xem danh sách học sinh".
- Error: MSG-010 nếu lớp đích vượt sức chứa; MSG-004/MSG-005.
- Success: MSG-084, học sinh được chọn chuyển sang lớp đích năm học mới; học sinh bị loại trừ giữ nguyên liên kết lớp cũ (xử lý thủ công sau qua `Classroom Detail`/`Update Classroom`); hồ sơ sức khỏe/dị ứng không bị ảnh hưởng (BR-194).

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Promote Students" trên `Academic Year Detail`. Sau khi xác nhận quay về `Academic Year Detail` (năm học mới).
**Bố cục**: Trang riêng (không phải Dialog, do có bảng học sinh dài) — Stepper ngang: Chọn khối/lớp nguồn-đích → Xem & loại trừ danh sách → Xác nhận, theo `Layout_Guidelines.md` mục 1.2 (form dài chia bước).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Promote Students Workspace, nền tảng Web, actor School Administration.
Bố cục: Trang riêng với Stepper ngang (Chọn khối nguồn-đích → Xem & loại trừ danh sách học sinh → Xác nhận).
Thành phần bắt buộc: select khối/lớp nguồn, select lớp đích, bảng học sinh + checkbox chọn/loại trừ, nút Xác nhận lên lớp.
Áp dụng Design System: dòng học sinh bị loại trừ hiển thị mờ/màu neutral nhạt.
Trạng thái cần thể hiện: error (vượt sức chứa lớp đích), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1909–1929 (Table 3-158 Promote Students to Next Academic Year Use Cases Specification)
- traceability_matrix.md §1.2 (UC-177 ↔ BR-194)
