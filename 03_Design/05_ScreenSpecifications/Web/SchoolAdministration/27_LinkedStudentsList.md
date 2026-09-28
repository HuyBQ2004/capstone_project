# Screen Spec — Linked Students List

**Actor**: School Administration | **UC liên quan**: bundled trong UC-025 (Report 3 §3.2 "Create Student") | **Status**: Draft
**BR liên quan**: BR-030 (liên kết học sinh-phụ huynh)
**MSG liên quan**: MSG-004, MSG-005

> **Nguồn**: Không phải UC độc lập — danh sách đầy đủ học sinh liên kết với 1 phụ huynh, mở rộng từ khối rút gọn ở `Parent Account Detail`.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Bảng danh sách học sinh liên kết đầy đủ | Table | Cột: Tên, School Reg No, Lớp, Trạng thái | BR-030 | Tối đa 5 học sinh/phụ huynh |
| 2 | Action: xem chi tiết học sinh | Row click | — | — | Mở `Student Profile Detail` |
| 3 | Action: gỡ liên kết | IconButton | Xác nhận trước khi gỡ (AlertDialog) | BR-030 | **[Giả định — cần team xác nhận]**: Normal Flow gốc UC-025 không mô tả rõ hành vi "gỡ liên kết", suy luận từ nhu cầu quản lý ngược |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Phụ huynh chưa liên kết học sinh nào" (trường hợp hiếm — thường luôn có ít nhất 1).
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ tối đa 5 học sinh.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Parent Account Detail` (link "Xem đầy đủ"). Điều hướng ra: `Student Profile Detail`.
**Bố cục**: Dashboard Shell — Page Header (tên phụ huynh), bảng `Table` đầy đủ.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Linked Students List, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header, bảng dữ liệu (Tên/School Reg No/Lớp/Trạng thái).
Thành phần bắt buộc: bảng, action xem chi tiết, action gỡ liên kết (kèm AlertDialog xác nhận).
Áp dụng Design System: không cần badge đặc biệt.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên — đặc biệt lưu ý action "gỡ liên kết" là suy luận, cần xác nhận với team.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1955–1975 (Table 3-31 Create Student Use Cases Specification)
- traceability_matrix.md §1.3 (UC-025 ↔ BR-030)
