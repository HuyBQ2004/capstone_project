# Screen Spec — Update Menu Screen

**Actor**: Service Caterers | **UC liên quan**: UC-096 - Update Menu (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-061, BR-062, BR-063, BR-064, BR-067, BR-069, BR-156, BR-157
**MSG liên quan**: MSG-074 (Success), MSG-051 (Warning), MSG-075 (Warning — giới hạn lặp món), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn (không tự suy diễn cách xử lý)**: `Report 3.md` (UC-096 Business Rules, dòng 2949) trích thêm **BR-072, BR-074**, nhưng theo `business_rules.md` hiện hành: BR-072 = "Chụp ảnh bằng chứng bắt buộc khi nhận hàng" và BR-074 = "Xác minh vị trí nhận hàng" — cả hai đều thuộc module Ingredient Receiving, không liên quan tới sửa thực đơn. Đây là trích dẫn lỗi thời của Report 3 (đã ghi nhận cơ chế tương tự ở `00_UI_Design_Documentation_Plan.md` mục 2.4) — **không đưa 2 mã này vào Screen Spec**. Riêng quy tắc "giới hạn món chính lặp lại tối đa 2 lần/tuần" mà UC-096 và `error_message.md` MSG-075 đều gọi là "BR-068" **không khớp** với nội dung BR-068 hiện tại trong `business_rules.md` (Quy trình phê duyệt thực đơn) — đây là mâu thuẫn đã biết nhưng **chưa có mã BR chính thức thay thế cho giới hạn dish rotation**. **[Giả định — cần team xác nhận]**: giữ MSG-075 làm nguồn cảnh báo hành vi (nội dung message không đổi), nhưng không gán số BR cụ thể cho đến khi team bổ sung/sửa `business_rules.md`.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Form chỉnh sửa thực đơn (pre-filled) | Form | Chỉ cho phép khi status = Draft hoặc Rejected | BR-067 | UC-096 Preconditions |
| 2 | Bình luận từ chối (nếu status = Rejected) | Read-only text block | Hiển thị lý do từ chối gần nhất từ School Administration | — | UC-096 Normal Flow bước 2 — nguồn field cụ thể (lý do từ chối) hiện chưa có UC/BR riêng mô tả cấu trúc lưu trữ, **[Giả định — cần team xác nhận]** cho định dạng hiển thị |
| 3 | Ngày phục vụ / Chọn món | DatePicker + Multi-select | Cùng ràng buộc như Create Menu Workspace | BR-061, BR-062 | UC-096 Normal Flow bước 3 |
| 4 | Cảnh báo giới hạn lặp món chính | Alert banner | Hiện khi 1 món chính được xếp lịch quá 2 lần/tuần | *(xem lưu ý mâu thuẫn nguồn ở trên)* | MSG-075 |
| 5 | Kiểm tra khai báo nguyên liệu | Validation tự động | Bắt buộc đầy đủ trước khi lưu | BR-069 | UC-096 Normal Flow bước 4 |
| 6 | Kết quả kiểm tra xung đột dị ứng | Alert banner | Tự động chạy lại sau khi sửa | BR-063 | UC-096 Normal Flow bước 5, MSG-051 |
| 7 | Nút "AI Assist" thay món xung đột | Button | Mở View Menu Recommendations Panel (UC-075) | BR-064, BR-156, BR-157 | UC-096 Alternative Flow AF1 |
| 8 | Nút "Lưu thay đổi" | Button | Lưu, hiển thị MSG-074 | — | UC-096 Normal Flow bước 6 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải form pre-filled.
- Empty: Không áp dụng.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu sửa/lưu thất bại; chặn hoàn toàn nếu cố sửa menu đã Approved/Published (BR-067).
- Success: `Toast` MSG-074 khi lưu thành công.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Menu Management Dashboard (hành động "Edit" trên dòng Draft/Rejected — `09_Service_Caterers_Screen_Flow_Web.md`). Thoát: "Lưu thay đổi" quay lại Menu Management Dashboard.
**Bố cục**: Form dài dạng `Stepper` ngang giống Create Menu Workspace (tái dùng bố cục), pre-filled dữ liệu hiện có, banner bình luận từ chối hiển thị đầu trang nếu status = Rejected.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Update Menu Screen, nền tảng Web, actor Service Caterers.
Bố cục: giống Create Menu Workspace (Stepper ngang) nhưng pre-filled dữ liệu, thêm banner bình luận từ chối ở đầu trang nếu có.
Thành phần bắt buộc: banner cảnh báo giới hạn lặp món (MSG-075), banner xung đột dị ứng (MSG-051), nút AI Assist, nút Lưu thay đổi.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn sửa menu đã khoá), success (MSG-074).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2933–2949 (UC-096 - Update Menu)
- business_rules.md BR-061, BR-062, BR-063, BR-064, BR-067, BR-069, BR-156, BR-157
- error_message.md MSG-074, MSG-051, MSG-075
