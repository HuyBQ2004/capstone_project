# Screen Spec — Manage User Screen/Dialog

**Actor**: System Admin | **UC liên quan**: UC-026 (Report 3 §3.2 "Create User") | **Status**: Draft
**BR liên quan**: BR-008 (bảo vệ role hệ thống mặc định khi gán), BR-010 (xác minh email trước đăng nhập), BR-012 (định dạng email/SĐT), BR-013 (phân quyền tạo — System Admin tạo user toàn hệ thống)
**MSG liên quan**: MSG-004, MSG-005, MSG-006 (email đã tồn tại), MSG-031 (tạo tài khoản thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Họ tên đầy đủ | Input (text) | Bắt buộc | — | |
| 2 | Email | Input (email) | Bắt buộc, đúng định dạng email, duy nhất trong hệ thống (MSG-006 nếu trùng) | BR-012 | |
| 3 | Số điện thoại | Input (tel) | Định dạng hợp lệ (+84/0xx) | BR-012 | |
| 4 | Vai trò (Role) | Select | Chọn 1 trong các role hệ thống/tuỳ chỉnh đã có | BR-008 | |
| 5 | Trường liên kết (School) | Select (điều kiện hiển thị) | **Bắt buộc nếu Role là role cấp trường** (VD School Administration, Teacher...) | — | Chỉ hiện khi Role thuộc nhóm cấp trường — theo đúng Normal Flow bước 3 |
| 6 | Nút "Tạo tài khoản" | Button (primary) | Disable tới khi field bắt buộc hợp lệ | — | |

### Trạng thái màn hình
- Loading: Button chuyển trạng thái loading khi submit, disable form.
- Empty: Không áp dụng (đây là form tạo mới).
- Error: MSG-006 dưới field Email nếu email đã tồn tại; MSG-004/MSG-005 nếu phiên hết hạn/không đủ quyền; cảnh báo validation nếu SĐT sai định dạng (theo Normal Flow bước 4, chưa có MSG code cụ thể cho lỗi SĐT — dùng banner cảnh báo chung).
- Success: MSG-031 (Toast) + đóng dialog/quay về User Account List, hệ thống gửi email xác minh (BR-010).

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Create User Account" trên màn `User Account List` (UC-153). Sau khi tạo thành công quay lại `User Account List`.
**Bố cục**: Form ngắn (<8 field) hiện trong `Dialog` giữa màn hình theo `Layout_Guidelines.md` mục 1.2 (không tạo trang riêng). Field xếp 1 cột, field điều kiện "Trường liên kết" hiện/ẩn động ngay dưới field Role.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Manage User Screen/Dialog (Create), nền tảng Web, actor System Admin.
Bố cục: Dialog giữa màn hình, form 1 cột, 6 field, nút Tạo tài khoản ở cuối dialog.
Thành phần bắt buộc: Họ tên, Email, SĐT, Role (select), School (select có điều kiện), nút Tạo tài khoản.
Áp dụng Design System: 00_Foundation/Design_System.md — lỗi field dùng token danger.
Trạng thái cần thể hiện: error (email trùng), loading (submit), success (toast MSG-031).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1293–1313 (Table 3-7 Create User Use Cases Specification)
- traceability_matrix.md §1.1 (UC-026 ↔ BR-008/010/012/013 ↔ MSG-004/005/006/031)
