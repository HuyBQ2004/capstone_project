# Screen Spec — View School Policy Screen

**Actor**: Medical Staff (dùng chung với Teacher, Service Caterers theo UC-137) | **UC liên quan**: UC-137 (Report 3 §3.2, dòng 1144 — chỉ có mô tả tóm tắt 1 dòng, KHÔNG có bảng đặc tả Normal/Alternative/Exception Flow đầy đủ) | **Status**: Draft
**BR liên quan**: BR-022 (quyền xem chính sách trường học theo vai trò)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách chính sách/quy định của trường | List/Document viewer | Chỉ xem chính sách của trường mình | BR-022 | [Giả định — cần team xác nhận] |
| 2 | Tiêu đề + ngày ban hành (mỗi chính sách) | Text | — | — | [Giả định] |
| 3 | Nội dung chính sách (xem chi tiết) | Text/PDF viewer | Chỉ đọc | — | [Giả định] |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải danh sách chính sách.
- Empty: "Trường chưa cập nhật chính sách nào" + illustration.
- Error: MSG-004, MSG-005; truy cập chính sách trường khác → từ chối theo BR-022.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ menu "View School Policy" trên Sidebar (nhóm "Kiểm tra & Giám sát ATTP").
**Bố cục**: danh sách chính sách dạng `Table`/Card grid bên trái, panel xem nội dung bên phải khi chọn 1 mục.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "View School Policy" trên Compliance Dashboard (Mobile).
**Bố cục**: trang riêng full-screen, danh sách `Card`/`ListTile`, chọn 1 mục mở PDF viewer hoặc trang nội dung.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình View School Policy, nền tảng Web, actor Medical Staff.
Bố cục: danh sách chính sách bên trái, panel xem nội dung bên phải.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1144 (UC-137).
- business_rules.md (BR-022).
- error_message.md (MSG-004, MSG-005).

**Ghi chú quan trọng — khoảng trống dữ liệu**: giống bản Mobile, UC-137 không có bảng đặc tả đầy đủ trong Report 3. **[Giả định — cần team xác nhận]** trước khi khóa spec.
