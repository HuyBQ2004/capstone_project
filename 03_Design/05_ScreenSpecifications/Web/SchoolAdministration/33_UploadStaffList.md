# Screen Spec — Upload Staff List

**Actor**: School Administration | **UC liên quan**: UC-024 (Report 3 §3.2 "Create Staff", biến thể bulk) | **Status**: Draft
**BR liên quan**: BR-010 (xác minh email), BR-012 (định dạng email/SĐT), BR-013 (phân quyền tạo staff — chỉ role Teacher/Medical Staff/Catering)
**MSG liên quan**: MSG-004, MSG-005, MSG-006 (email đã tồn tại), MSG-031 (tạo thành công)

> **Nguồn**: Screen Authorization Matrix ghi "Upload Staff List | UC-024 (biến thể bulk)" — Normal Flow gốc của UC-024 (Report 3 dòng 1275–1291) mô tả tạo **1 staff đơn lẻ** qua form, không mô tả rõ luồng bulk upload file. Nội dung dưới đây áp dụng cùng validation của UC-024 (role phải là Teacher/Medical Staff/Catering, không phải admin/external) cho ngữ cảnh upload hàng loạt, tương tự cấu trúc `Bulk Import Student Profiles` — **[Giả định — cần team xác nhận]** cấu trúc file template bulk staff.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Tải template" | Button (link) | — | — | **[Giả định]** |
| 2 | Input upload file | Input type="file" | Định dạng Excel/CSV | — | |
| 3 | Nút "Tải lên & Kiểm tra" | Button (primary) | Validate: role hợp lệ (chỉ Teacher/Medical Staff/Catering — BR-013), email duy nhất (MSG-006), SĐT hợp lệ (BR-012) | BR-012, BR-013 | |
| 4 | Bảng tóm tắt validation | Table + số liệu tổng | — | — | |
| 5 | Nút "Xác nhận nhập" | Button (primary) | Chỉ enable khi có dòng hợp lệ | BR-010 | Gửi email xác minh cho từng tài khoản mới |

### Trạng thái màn hình
- Loading: Progress bar khi validate/import.
- Empty: Chưa upload file.
- Error: MSG-006 nếu email trùng; validation nếu role không hợp lệ (VD chọn "System Admin" — chặn theo BR-013); MSG-004/MSG-005.
- Success: MSG-031, quay về `Staff Management`, các tài khoản mới chuyển sang theo dõi tại `Invitation Status`.

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Upload Staff List" trên `Staff Management`. Sau khi nhập xong quay về `Staff Management`/`Invitation Status`.
**Bố cục**: Stepper ngang (Tải template → Upload → Validate → Xác nhận), tương tự `Bulk Import Student Profiles`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Upload Staff List, nền tảng Web, actor School Administration.
Bố cục: Stepper ngang (Tải template → Upload → Validate → Xác nhận).
Thành phần bắt buộc: nút Tải template, input upload file, nút Tải lên & Kiểm tra, bảng tóm tắt validation, nút Xác nhận nhập.
Áp dụng Design System: dòng lỗi dùng token danger.
Trạng thái cần thể hiện: error (email trùng, role không hợp lệ), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1271–1291 (Table 3-6 Create Staff Use Cases Specification)
- traceability_matrix.md §1.1 (UC-024 ↔ BR-010/BR-012/BR-013)
