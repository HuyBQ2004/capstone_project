# Screen Spec — View School Policy Screen

**Actor**: Medical Staff (dùng chung với Teacher, Service Caterers theo UC-137) | **UC liên quan**: UC-137 (Report 3 §3.2, dòng 1144 — chỉ có mô tả tóm tắt 1 dòng trong danh sách UC tổng hợp, KHÔNG có bảng đặc tả đầy đủ Normal/Alternative/Exception Flow trong Report 3) | **Status**: Draft
**BR liên quan**: BR-022 (quyền xem chính sách trường học theo vai trò — Teacher/Medical Staff/Service Caterers được xem chính sách trường mình để làm việc hàng ngày)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách chính sách/quy định của trường | List/Document viewer | Chỉ xem chính sách của trường mình (schoolId trong JWT) | BR-022 | [Giả định — cần team xác nhận cấu trúc field cụ thể, do UC-137 không có Normal Flow đầy đủ] |
| 2 | Tiêu đề + ngày ban hành (mỗi chính sách) | Text | — | — | [Giả định] |
| 3 | Nội dung chính sách (xem chi tiết) | Text/PDF viewer | Chỉ đọc | — | [Giả định] |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` khi tải danh sách chính sách.
- Empty: "Trường chưa cập nhật chính sách nào" + illustration.
- Error: MSG-004, MSG-005; truy cập chính sách trường khác → từ chối theo BR-022 (cross-school access denied).
- Success: không áp dụng (màn hình xem thuần túy).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "View School Policy" trên Compliance Dashboard (`23_ComplianceDashboard.md`).
**Bố cục**: trang riêng full-screen, danh sách `Card`/`ListTile` các chính sách, chọn 1 mục mở PDF viewer hoặc trang nội dung đầy đủ.

## Platform Layout — Web
**Entry/Exit points**: Vào từ menu "View School Policy" trên Sidebar (nhóm "Kiểm tra & Giám sát ATTP", theo `08_Medical_Staff_Screen_Flow_Web.md`).
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — danh sách chính sách dạng `Table`/Card grid bên trái, panel xem nội dung bên phải khi chọn 1 mục.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình View School Policy, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: danh sách chính sách (tiêu đề + ngày ban hành), panel/trang xem nội dung đầy đủ.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1144 (UC-137, chỉ có mô tả tóm tắt).
- business_rules.md (BR-022).
- error_message.md (MSG-004, MSG-005).

**Ghi chú quan trọng — khoảng trống dữ liệu**: UC-137 chỉ xuất hiện dưới dạng 1 dòng mô tả trong bảng tổng hợp UC (Report 3 dòng 1144: *"Allows Medical Staff to review school food safety policies and operational regulations"*), **không có bảng đặc tả Normal Flow/Alternative Flow/Exception đầy đủ** ở bất kỳ đâu trong Report 3 §3.2 (đã grep toàn file, không tìm thấy). Toàn bộ field trong bảng trên là **[Giả định — cần team xác nhận]** dựa trên mô tả tóm tắt + BR-022 — đây là khoảng trống dữ liệu nguồn cần đội ngũ bổ sung UC đầy đủ trước khi khóa spec, không phải suy diễn tùy tiện.
