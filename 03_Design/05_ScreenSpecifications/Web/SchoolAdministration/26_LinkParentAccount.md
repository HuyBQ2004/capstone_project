# Screen Spec — Link Parent Account

**Actor**: School Administration | **UC liên quan**: bundled trong UC-025 (Report 3 §3.2 "Create Student") | **Status**: Draft
**BR liên quan**: BR-030 (liên kết học sinh-phụ huynh), BR-012 (định dạng email/SĐT), BR-010 (xác minh email khi tạo mới)
**MSG liên quan**: MSG-004, MSG-005, MSG-012, MSG-031 (tạo tài khoản phụ huynh mới thành công)

> **Nguồn**: Bước 5 của Normal Flow UC-025 — "System searches for the parent email. If exists: link. If not: input parent's name/phone to create new parent account." Đây là 1 bước trong luồng tạo học sinh, không phải UC độc lập.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Email phụ huynh | Input (email) | Bắt buộc, hệ thống tự động tìm kiếm khi nhập | BR-012 | |
| 2 | Kết quả tìm kiếm: "Đã tìm thấy tài khoản" | Card hiển thị thông tin phụ huynh có sẵn | Hiển thị tên/SĐT hiện có, chỉ cần xác nhận link | BR-030 | Nhánh "email tồn tại" |
| 3 | Form tạo phụ huynh mới (nếu không tìm thấy) | Input Họ tên + Input SĐT | Bắt buộc, định dạng SĐT hợp lệ | BR-012 | Nhánh "email chưa tồn tại" |
| 4 | Cảnh báo giới hạn liên kết | Alert (warning) | Hiện khi phụ huynh đã có 5 học sinh liên kết (AF1) | BR-030 | Chặn link, yêu cầu xác minh thủ công |
| 5 | Nút "Xác nhận liên kết" | Button (primary) | Disable nếu đạt giới hạn (AF1) | BR-030 | |

### Trạng thái màn hình
- Loading: Loading khi tìm kiếm email / submit.
- Empty: Chưa nhập email — chưa hiển thị kết quả tìm kiếm.
- Error: AF1 — đạt giới hạn liên kết tối đa → chặn, hiển thị cảnh báo; MSG-012 nếu định dạng sai; MSG-004/MSG-005.
- Success: Nhánh email tồn tại → liên kết ngay; nhánh tạo mới → MSG-031 + gửi email xác minh (BR-010).

---

## Platform Layout — Web
**Entry/Exit points**: Là 1 bước trong luồng Create Student (không phải trang điều hướng độc lập) — cũng có thể mở từ `Parent Account Detail` (nút "Link thêm học sinh", theo chiều ngược: chọn học sinh để link vào phụ huynh đang xem).
**Bố cục**: Bước trong `Sheet`/Dialog của luồng Create Student — 1 cột, chuyển động (tìm kiếm → hiện kết quả/form tạo mới → xác nhận).

## AI Generation Prompt ⭐

```
Tạo wireframe cho bước Link Parent Account, nền tảng Web, actor School Administration.
Bố cục: 1 bước trong Sheet/Dialog của luồng Create Student — input email tìm kiếm, kết quả (card có sẵn hoặc form tạo mới), nút xác nhận.
Thành phần bắt buộc: input email, card kết quả tìm thấy HOẶC form tạo phụ huynh mới (Họ tên + SĐT), alert cảnh báo giới hạn liên kết, nút Xác nhận liên kết.
Áp dụng Design System: alert cảnh báo dùng token warning.
Trạng thái cần thể hiện: error (đạt giới hạn), success (2 nhánh: đã có tài khoản / tạo mới).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1955–1975 (Table 3-31 Create Student Use Cases Specification, bước 5 + AF1)
- traceability_matrix.md §1.3 (UC-025 ↔ BR-030)
