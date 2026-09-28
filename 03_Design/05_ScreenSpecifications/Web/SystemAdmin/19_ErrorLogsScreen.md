# Screen Spec — Error Logs Screen

**Actor**: System Admin | **UC liên quan**: UC-111 (Report 3 §3.2 "View Error Logs") | **Status**: Draft
**BR liên quan**: BR-164 (mã hóa thông tin xác thực — áp dụng khi log chứa request metadata nhạy cảm)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo Severity | Select | — | — | |
| 2 | Filter theo Date range | DatePicker (range) | — | — | |
| 3 | Bảng danh sách lỗi | Table | Cột: Timestamp, Error Message, Severity, Module/Class, Path | — | Sort theo severity/timestamp |
| 4 | Badge Severity | Badge (danger=Critical/High, warning=Medium, info=Low) | — | — | |
| 5 | Panel chi tiết lỗi (khi chọn 1 dòng) | Detail panel/Sheet | Hiển thị stack trace, request metadata, session ảnh hưởng | BR-164 | |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Không có lỗi nào trong khoảng thời gian đã chọn" (trạng thái tích cực).
- Error: MSG-004/MSG-005.
- Success: Bảng + panel chi tiết hiển thị đầy đủ khi chọn 1 lỗi.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Giám sát & Audit". Không có màn hình con — chi tiết lỗi mở dưới dạng Sheet/panel trong cùng trang.
**Bố cục**: Dashboard Shell — filter bar trên bảng, bảng `Table` bên trái/toàn bộ, panel chi tiết mở dạng `Sheet` trượt từ phải khi chọn 1 dòng (theo `Layout_Guidelines.md` mục 1.2).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Error Logs Screen, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — filter bar (Severity, Date range), bảng dữ liệu lỗi, Sheet chi tiết trượt từ phải khi chọn 1 dòng.
Thành phần bắt buộc: filter Severity/Date, bảng (Timestamp/Error Message/Severity/Module/Path), badge severity, panel chi tiết (stack trace, request metadata, session).
Áp dụng Design System: badge danger/warning/info theo severity.
Trạng thái cần thể hiện: empty (không có lỗi), success (có lỗi + chi tiết mở).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4905–4925 (Table 3-150 View Error Logs Use Cases Specification)
- traceability_matrix.md §1.12 (UC-111 ↔ BR-164)
