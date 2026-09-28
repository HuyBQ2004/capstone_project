# Screen Spec — Student Profile Management

**Actor**: School Administration | **UC liên quan**: UC-147 (View Student List) + action UC-025 (Create Student) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-025 (sức chứa lớp, hiển thị gián tiếp)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (tên học sinh) | Input (search) | — | — | |
| 2 | Filter theo Class/Grade | Select | — | — | |
| 3 | Bảng danh sách học sinh | Table | Cột: Tên, School Registration Number, Lớp, Liên hệ phụ huynh | — | |
| 4 | Nút "Create Student Profile" | Button (primary, góc trên phải) | — | — | Mở form Create Student (UC-025) |
| 5 | Action: xem chi tiết | Row click | — | — | Mở `Student Profile Detail` |
| 6 | Nút "Bulk Import" | Button | — | — | Mở `Bulk Import Student Profiles` |
| 7 | Nút "Export Excel/CSV" | Button | — | — | |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có học sinh nào — tạo hồ sơ đầu tiên hoặc nhập hàng loạt".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Lớp/Học sinh". Điều hướng ra: form Create Student (Dialog/Sheet), `Student Profile Detail`, `Bulk Import Student Profiles`.
**Bố cục**: Dashboard Shell — Page Header (nút Create + Bulk Import góc phải), filter bar, bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Student Profile Management, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Create Student Profile + Bulk Import), filter bar (search + class/grade), bảng dữ liệu.
Thành phần bắt buộc: search, filter, bảng (Tên/School Reg No/Lớp/Liên hệ PH), action xem chi tiết, nút Export.
Áp dụng Design System: không cần badge đặc biệt.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2245–2261 (Table 3-44 View Student List Use Cases Specification)
- traceability_matrix.md §1.3 (UC-147)
