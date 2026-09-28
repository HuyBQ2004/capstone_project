# Screen Spec — School Detail

**Actor**: System Admin | **UC liên quan**: UC-122 (Report 3 §3.2 "View Information School") | **Status**: Draft
**BR liên quan**: BR-022 (quyền xem chính sách trường theo vai trò), BR-025 (sức chứa lớp — hiển thị trong thống kê tổng hợp)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tên trường | Text hiển thị | — | — | |
| 2 | Địa chỉ | Text hiển thị | — | — | |
| 3 | Mã số thuế (Tax ID) | Text hiển thị | — | — | |
| 4 | Email liên hệ | Text hiển thị | — | — | |
| 5 | Toạ độ GPS (latitude/longitude) | Text hiển thị + mini-map | Khoá hiển thị `coordinateLocked` nếu trường đã Active | BR-028 | Badge "Đã khoá tọa độ" nếu `coordinateLocked = true` |
| 6 | Giờ hoạt động | Text hiển thị | — | — | |
| 7 | Liên hệ khẩn cấp | Text hiển thị | — | — | |
| 8 | Thống kê tổng hợp (số lớp, số học sinh, số nhân viên) | Card số liệu | — | BR-025 | |
| 9 | Nút "Edit" | Button | — | — | Mở `Update School Screen/Dialog` |
| 10 | Nút "Export PDF" | Button | — | — | Theo Normal Flow bước 4 |

### Trạng thái màn hình
- Loading: Skeleton card khi tải hồ sơ trường.
- Empty: Không áp dụng (trường luôn có hồ sơ tối thiểu khi đã đăng ký).
- Error: MSG-004/MSG-005.
- Success: Hiển thị đầy đủ hồ sơ trường.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `School List` (click 1 dòng). Điều hướng ra: `Update School Screen/Dialog` (Edit).
**Bố cục**: Dashboard Shell — Page Header (tên trường + nút Edit/Export góc phải), Content chia 2 khối: thông tin cơ bản (`col-span-8`) cạnh card thống kê tổng hợp (`col-span-4`), khối toạ độ GPS + mini-map bên dưới.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình School Detail, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Page Header (tên trường + nút Edit/Export), 2 cột: thông tin cơ bản + card thống kê, khối toạ độ GPS/mini-map bên dưới.
Thành phần bắt buộc: tên/địa chỉ/tax ID/email/giờ hoạt động/liên hệ khẩn cấp, toạ độ GPS kèm badge khoá, card thống kê (lớp/học sinh/nhân viên), nút Edit, nút Export PDF.
Áp dụng Design System: badge khoá tọa độ dùng token info hoặc secondary.
Trạng thái cần thể hiện: loading, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1799–1819 (Table 3-27 View Information School Use Cases Specification)
- traceability_matrix.md §1.2 (UC-122)
- business_rules.md BR-028 (coordinateLocked)
