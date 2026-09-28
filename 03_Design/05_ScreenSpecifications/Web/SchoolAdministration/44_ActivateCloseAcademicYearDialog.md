# Screen Spec — Activate/Close Academic Year Dialog

**Actor**: School Administration | **UC liên quan**: UC-175 (Report 3 §3.2 "Activate/Close Academic Year") | **Status**: Draft
**BR liên quan**: BR-191 (1 năm học Active duy nhất/trường), BR-193 (đóng năm học → Classroom read-only)
**MSG liên quan**: MSG-004, MSG-005, MSG-080 (đã có năm học Active), MSG-083 (cập nhật trạng thái thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin năm học (Year Label, trạng thái hiện tại → mới) | Text + Badge | — | — | |
| 2 | Cảnh báo hệ quả (nhánh Activate) | Alert (info) | Hiển thị: sẽ chặn nếu còn năm học Active khác | BR-191 | |
| 3 | Cảnh báo hệ quả (nhánh Close) | Alert (warning) | Hiển thị: toàn bộ Classroom thuộc năm học sẽ chuyển read-only, dữ liệu lịch sử vẫn giữ nguyên | BR-193 | AF1 — cảnh báo nếu còn thao tác đang chờ xử lý (pending operations) |
| 4 | Nút "Xác nhận" | Button (primary cho Activate / destructive cho Close) | Chặn với MSG-080 nếu Activate khi đã có năm Active khác | BR-191 | |
| 5 | Nút "Huỷ" | Button (ghost) | — | — | |

### Trạng thái màn hình
- Loading: Button "Xác nhận" loading.
- Empty: Không áp dụng.
- Error: MSG-080 nếu Activate bị chặn; MSG-004/MSG-005.
- Success: MSG-083, cập nhật trạng thái năm học, ghi audit log.

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Activate"/"Close" trên `Academic Year Detail`. Đóng lại trả về `Academic Year Detail`.
**Bố cục**: `AlertDialog` (hành động thay đổi trạng thái ảnh hưởng cascade tới Classroom — đặc biệt nhánh Close có rủi ro tương tự Active/Deactivate School), không điều hướng trang riêng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Activate/Close Academic Year Dialog, nền tảng Web, actor School Administration.
Bố cục: AlertDialog giữa màn hình, hiển thị thông tin năm học + cảnh báo hệ quả (khác nhau theo nhánh Activate/Close) + 2 nút Xác nhận/Huỷ.
Thành phần bắt buộc: Year Label, badge trạng thái trước/sau, alert cảnh báo (info cho Activate, warning cho Close), nút Xác nhận, nút Huỷ.
Áp dụng Design System: alert info cho Activate, alert warning + nút destructive cho Close.
Trạng thái cần thể hiện: error (chặn Activate khi đã có năm Active khác — MSG-080), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1865–1885 (Table 3-156 Activate/Close Academic Year Use Cases Specification)
- traceability_matrix.md §1.2 (UC-175 ↔ BR-191/BR-193)
- error_message.md MSG-080, MSG-083
