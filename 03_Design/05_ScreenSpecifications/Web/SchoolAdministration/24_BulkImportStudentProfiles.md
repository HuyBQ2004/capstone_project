# Screen Spec — Bulk Import Student Profiles

**Actor**: School Administration | **UC liên quan**: UC-010 (Report 3 §3.2 "Bulk Import Student & Parent") | **Status**: Draft
**BR liên quan**: BR-032 (nhập hàng loạt học sinh/phụ huynh), BR-012 (định dạng email/SĐT), BR-010 (xác minh email)
**MSG liên quan**: MSG-004, MSG-005, MSG-012 (mã học sinh không hợp lệ), MSG-035 (nhập hàng loạt thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Tải template" | Button (link) | — | — | |
| 2 | Input upload file | Input type="file" + preview tên file | Định dạng Excel/CSV theo template | — | |
| 3 | Nút "Tải lên & Kiểm tra" | Button (primary) | — | BR-012, BR-032 | Chạy validate: định dạng email/SĐT, trùng mã học sinh, liên kết phụ huynh |
| 4 | Bảng tóm tắt validation (hợp lệ/không hợp lệ/trùng lặp) | Table + số liệu tổng | — | — | AF1 UC-010 |
| 5 | Danh sách dòng lỗi chi tiết (kèm mã lỗi) | Table (đọc-only) | — | MSG-012 | |
| 6 | Nút "Xác nhận nhập" | Button (primary) | Chỉ enable khi có ít nhất 1 dòng hợp lệ | — | |

### Trạng thái màn hình
- Loading: Progress bar khi đang validate/import file lớn.
- Empty: Chưa upload file — hiển thị hướng dẫn tải template trước.
- Error: MSG-012 (mã học sinh không hợp lệ/đã dùng) hiển thị theo từng dòng lỗi (AF1); MSG-004/MSG-005.
- Success: MSG-035, quay về `Student Profile Management` với danh sách mới được nhập; gửi email xác minh cho tài khoản phụ huynh mới (BR-010).

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Bulk Import" trên `Student Profile Management`. Sau khi nhập xong quay về `Student Profile Management`.
**Bố cục**: Form theo Stepper ngang (Tải template → Upload → Xem kết quả validate → Xác nhận) theo `Layout_Guidelines.md` mục 1.2 (form dài chia bước).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Bulk Import Student Profiles, nền tảng Web, actor School Administration.
Bố cục: Trang riêng với Stepper ngang (Tải template → Upload → Validate → Xác nhận).
Thành phần bắt buộc: nút Tải template, input upload file, nút Tải lên & Kiểm tra, bảng tóm tắt validation, bảng chi tiết dòng lỗi, nút Xác nhận nhập.
Áp dụng Design System: dòng lỗi dùng token danger, dòng hợp lệ dùng token success.
Trạng thái cần thể hiện: error (dòng lỗi MSG-012), success (nhập hoàn tất).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1933–1953 (Table 3-30 Bulk Import Student & Parent Use Cases Specification)
- traceability_matrix.md §1.3 (UC-010 ↔ BR-032)
