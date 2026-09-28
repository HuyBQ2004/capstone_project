# Screen Spec — Parent Account Detail

**Actor**: School Administration | **UC liên quan**: bundled trong UC-025 (Report 3 §3.2 "Create Student") | **Status**: Draft
**BR liên quan**: BR-030 (liên kết học sinh-phụ huynh), BR-012 (định dạng thông tin)
**MSG liên quan**: MSG-004, MSG-005, MSG-029

> **Nguồn**: Không phải UC độc lập — khi tạo học sinh (UC-025), hệ thống tìm email phụ huynh: có sẵn → hiển thị chi tiết + liên kết; chưa có → tạo mới. File này đặc tả riêng khối "Parent Account Detail" theo tên trong Screen Authorization Matrix, dùng khi School Admin xem lại thông tin phụ huynh đã liên kết (từ `Student Profile Detail`).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Họ tên phụ huynh | Text hiển thị | — | — | |
| 2 | Email | Text hiển thị | Duy nhất trong hệ thống | BR-012 | |
| 3 | Số điện thoại | Text hiển thị | Định dạng hợp lệ | BR-012 | |
| 4 | Trạng thái xác minh email | Badge (warning=Pending, success=Verified) | — | BR-010 | |
| 5 | Danh sách học sinh liên kết | Table rút gọn (tối đa 5, BR-030/040) | — | BR-030 | Link sang `Linked Students List` để xem đầy đủ |
| 6 | Nút "Link thêm học sinh" | Button | Chặn nếu đã đạt giới hạn tối đa | BR-030 | Mở `Link Parent Account` |

### Trạng thái màn hình
- Loading: Skeleton card.
- Empty: Không áp dụng (phụ huynh luôn có ít nhất 1 học sinh liên kết khi vào màn hình này từ Student Profile Detail).
- Error: AF1 UC-025 — nếu phụ huynh đã đạt giới hạn liên kết tối đa → chặn thêm, hiển thị cảnh báo xác minh thủ công; MSG-004/MSG-005.
- Success: MSG-029 khi cập nhật thông tin.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Student Profile Detail` (link "Phụ huynh liên kết"). Điều hướng ra: `Linked Students List`, `Link Parent Account`.
**Bố cục**: Dashboard Shell — Page Header (tên phụ huynh + badge xác minh), Content: thông tin liên hệ (`col-span-6`), danh sách học sinh liên kết rút gọn (`col-span-6`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Parent Account Detail, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (badge xác minh email), 2 cột: thông tin liên hệ, danh sách học sinh liên kết rút gọn.
Thành phần bắt buộc: họ tên, email, SĐT, badge xác minh, bảng học sinh liên kết rút gọn, nút Link thêm học sinh.
Áp dụng Design System: badge warning/success cho trạng thái xác minh.
Trạng thái cần thể hiện: error (đạt giới hạn liên kết), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1955–1975 (Table 3-31 Create Student Use Cases Specification, bước 5 + AF1)
- traceability_matrix.md §1.3 (UC-025 ↔ BR-030)
