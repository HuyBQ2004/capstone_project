# Screen Spec — Update Menu Screen

**Actor**: Service Caterers | **UC liên quan**: UC-096 - Update Menu (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-061, BR-062, BR-063, BR-064, BR-067, BR-069, BR-156, BR-157
**MSG liên quan**: MSG-074 (Success), MSG-051 (Warning), MSG-075 (Warning — giới hạn lặp món), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn**: giống bản Web — `Report 3.md` UC-096 trích thêm BR-072/BR-074 vốn thuộc module Ingredient Receiving, không áp dụng cho màn này (không đưa vào Screen Spec). "Giới hạn lặp món chính 2 lần/tuần" (MSG-075) hiện **chưa có mã BR chính thức khớp** trong `business_rules.md` (BR-068 hiện tại là quy trình phê duyệt, không phải dish rotation) — **[Giả định — cần team xác nhận]** giữ nội dung MSG-075 nhưng không gán số BR cho tới khi team xác nhận.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Form chỉnh sửa thực đơn (pre-filled) | Form nhiều trang | Chỉ cho phép khi status = Draft hoặc Rejected | BR-067 | UC-096 Preconditions |
| 2 | Bình luận từ chối (nếu status = Rejected) | Read-only text block | Hiển thị lý do từ chối gần nhất | — | **[Giả định — cần team xác nhận]** định dạng hiển thị |
| 3 | Ngày phục vụ / Chọn món | DatePicker + Checkbox list | Cùng ràng buộc như Create Menu Workspace | BR-061, BR-062 | UC-096 Normal Flow bước 3 |
| 4 | Cảnh báo giới hạn lặp món chính | MaterialBanner | Hiện khi 1 món chính được xếp lịch quá 2 lần/tuần | *(xem lưu ý mâu thuẫn nguồn)* | MSG-075 |
| 5 | Kiểm tra khai báo nguyên liệu | Validation tự động | Bắt buộc đầy đủ trước khi lưu | BR-069 | UC-096 Normal Flow bước 4 |
| 6 | Kết quả kiểm tra xung đột dị ứng | MaterialBanner | Tự động chạy lại sau khi sửa | BR-063 | UC-096 Normal Flow bước 5, MSG-051 |
| 7 | Nút "AI Assist" thay món xung đột | Button | Mở View Menu Recommendations Panel (UC-075) | BR-064, BR-156, BR-157 | UC-096 Alternative Flow AF1 |
| 8 | Nút "Lưu thay đổi" | FilledButton (cố định cuối màn) | Lưu, hiển thị MSG-074 | — | UC-096 Normal Flow bước 6 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải form pre-filled.
- Empty: Không áp dụng.
- Error: `AlertDialog` MSG-004/MSG-005 nếu sửa/lưu thất bại; chặn hoàn toàn nếu cố sửa menu đã Approved/Published (BR-067).
- Success: `SnackBar` MSG-074 khi lưu thành công.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Menu Management Dashboard (hành động "Edit" trên card Draft/Rejected — `04_Service_Caterers_Screen_Flow.md`). Thoát: "Lưu thay đổi" quay lại Menu Management Dashboard.
**Bố cục**: Form nhiều trang riêng giống Create Menu Workspace, pre-filled dữ liệu hiện có, banner bình luận từ chối ở đầu trang 1 nếu status = Rejected.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Update Menu Screen, nền tảng Mobile, actor Service Caterers.
Bố cục: giống Create Menu Workspace (nhiều trang tuần tự) nhưng pre-filled dữ liệu, thêm banner bình luận từ chối nếu có.
Thành phần bắt buộc: banner cảnh báo giới hạn lặp món (MSG-075), banner xung đột dị ứng (MSG-051), nút AI Assist, nút Lưu thay đổi.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn sửa menu đã khoá), success (MSG-074).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2933–2949 (UC-096 - Update Menu)
- business_rules.md BR-061, BR-062, BR-063, BR-064, BR-067, BR-069, BR-156, BR-157
- error_message.md MSG-074, MSG-051, MSG-075
