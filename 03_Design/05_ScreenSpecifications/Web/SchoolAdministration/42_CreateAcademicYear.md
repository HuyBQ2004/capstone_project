# Screen Spec — Create Academic Year

**Actor**: School Administration | **UC liên quan**: UC-174 (Report 3 §3.2 "Create Academic Year") | **Status**: Draft
**BR liên quan**: BR-191 (1 năm học Active duy nhất/trường), BR-192 (Classroom phải thuộc năm học Active), BR-194 (sao chép cấu trúc lớp)
**MSG liên quan**: MSG-004, MSG-005, MSG-080 (đã có năm học Active, phải đóng trước), MSG-082 (tạo năm học thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Year Label (VD "2026-2027") | Input (text) | Bắt buộc, duy nhất trong trường | — | |
| 2 | Start Date, End Date | DatePicker x2 | Bắt buộc, Start < End | — | |
| 3 | Checkbox "Sao chép cấu trúc lớp từ năm học trước" | Checkbox | Tuỳ chọn | BR-194 | AF1 — chỉ sao chép tên/cấp/sức chứa, KHÔNG chuyển học sinh |
| 4 | Nút "Tạo năm học" | Button (primary) | — | BR-191, BR-192 | |

### Trạng thái màn hình
- Loading: Button "Tạo năm học" loading.
- Empty: Không áp dụng (form tạo mới).
- Error: MSG-080 nếu cố **kích hoạt ngay** năm học mới trong khi đã có năm học Active khác (năm học mới luôn tạo ở trạng thái Draft — chỉ lỗi nếu UI cho phép tick "Activate ngay"); MSG-004/MSG-005.
- Success: MSG-082, năm học được lưu ở trạng thái **Draft** (không tự Active); nếu tick sao chép cấu trúc lớp → tạo các Classroom mới liên kết năm học mới (BR-194), không chuyển học sinh.

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Create Academic Year" trên `Academic Year List`. Sau khi tạo quay về `Academic Year List` (hoặc `Academic Year Detail` của năm học vừa tạo).
**Bố cục**: Form ngắn hiện trong `Dialog` giữa màn hình theo `Layout_Guidelines.md` mục 1.2.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Create Academic Year, nền tảng Web, actor School Administration.
Bố cục: Dialog giữa màn hình, form 1 cột.
Thành phần bắt buộc: Year Label, Start Date, End Date, checkbox Sao chép cấu trúc lớp, nút Tạo năm học.
Áp dụng Design System: lỗi field dùng token danger.
Trạng thái cần thể hiện: success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên — đặc biệt KHÔNG có tuỳ chọn "Activate ngay" vì năm học mới luôn tạo ở trạng thái Draft theo Normal Flow.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1843–1863 (Table 3-155 Create Academic Year Use Cases Specification)
- traceability_matrix.md §1.2 (UC-174 ↔ BR-191/BR-192/BR-194)
