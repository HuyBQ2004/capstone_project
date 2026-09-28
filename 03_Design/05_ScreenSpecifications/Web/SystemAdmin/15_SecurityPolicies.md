# Screen Spec — Security Policies

**Actor**: System Admin | **UC liên quan**: UC-184 - Configure Security Policies (Report 3.md §3.2.12.13 — **UC mới**, thêm 2026-07-01 qua audit A8) | **Status**: Draft
**BR liên quan**: BR-006 (chính sách độ mạnh mật khẩu), BR-002 (khóa tài khoản sau 5 lần sai — lockout 15 phút), BR-004 (session timeout 30 phút), BR-170 (audit trail)
**MSG liên quan**: MSG-004, MSG-005, MSG-029

> **Đã xác nhận 2026-07-01 (audit A8)**: đây là 1 trong 8 màn A8 **bắt buộc phải thêm UC mới** — không có UC nào có sẵn (kể cả UC-171/172 Reporting & Analytics) phù hợp vì đây là màn hình cấu hình (configuration), không phải xem/xuất báo cáo. UC-184 - Configure Security Policies được viết mới tại Report 3.md §3.2.12.13, xác nhận System Admin được phép **xem và sửa** các tham số BR-002/BR-004/BR-006 (trước đây chỉ là hằng số cứng, chưa có UI cấu hình). **[Giả định còn lại — cần team xác nhận trước khi code hoá]**: phạm vi giá trị cho phép chỉnh sửa (khoảng min-max) chưa được xác định, xem Assumptions của UC-184.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Số lần đăng nhập sai tối đa trước khi khoá | Input (number, editable) | Mặc định 5 lần | BR-002 | UC-184 Normal Flow bước 3 xác nhận cho sửa |
| 2 | Thời gian khoá tài khoản | Input (number, phút) | Mặc định 15 phút | BR-002 | |
| 3 | Thời gian session timeout | Input (number, phút) | Mặc định 30 phút | BR-004 | |
| 4 | Chính sách độ mạnh mật khẩu (độ dài, ký tự đặc biệt, không trùng 3 mật khẩu gần nhất) | Checkbox group / Input | — | BR-006 | |
| 5 | Nút "Lưu" | Button (primary) | — | — | UC-184 Normal Flow bước 5: lưu + ghi audit trail (BR-170) + MSG-029 |

### Trạng thái màn hình
- Loading: `Skeleton` cho form.
- Empty: Không áp dụng.
- Error: MSG-004/MSG-005. Nếu giá trị ngoài khoảng cho phép: validation error tại field (UC-184 Exceptions — **[Giả định — chưa có MSG code riêng cho lỗi khoảng giá trị]**).
- Success: MSG-029 "Cập nhật hồ sơ thành công".

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Cấu hình Hệ thống" (cạnh AI Settings/External Integrations).
**Bố cục**: **[Giả định]** — Dashboard Shell, form 1 cột nhóm theo section (Đăng nhập/Khoá tài khoản, Session, Mật khẩu).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Security Policies, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell, form 1 cột nhóm theo section (Đăng nhập/Khoá tài khoản, Session, Mật khẩu), nút Lưu.
Thành phần bắt buộc: 4 field cấu hình (số lần sai tối đa, thời gian khoá, session timeout, chính sách mật khẩu) + nút Lưu.
Trạng thái cần thể hiện: loading, error (validation khoảng giá trị), success (MSG-029).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md §3.2.12.13 (UC-184 Configure Security Policies — UC mới, bổ sung 2026-07-01)
- traceability_matrix.md §1.12 (UC-184)
- business_rules.md BR-002, BR-004, BR-006, BR-170
- 14_Screen_Authorization_Matrix.md khối 2
