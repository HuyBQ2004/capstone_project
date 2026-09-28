# Screen Spec — Notification Center/List

**Actor**: School Administration, Teacher, Service Caterers, Parent (cột `x` theo `14_Screen_Authorization_Matrix.md` khối 1, dòng "Notification Center/List" — System Admin/Medical Staff/Raw Food Supplier KHÔNG có, chỉ có Notification Settings) | **UC liên quan**: — (không có UC riêng; nội dung suy ra từ "Notification History" Report 3 §3.3.2.1.13 + BR-159 cơ chế gửi) | **Status**: Draft
**BR liên quan**: BR-138 (cảnh báo quan trọng bỏ qua phê duyệt, phát ngay), BR-158 (Quiet Hours 22:00–06:00, không áp dụng cho cảnh báo khẩn cấp), BR-159 (retry + fallback email)
**MSG liên quan**: MSG-068 (Warning — hàng đợi push bị tắc nghẽn, hiển thị gián tiếp qua trạng thái "đang gửi")

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Back" | Button (icon/link) | — | — | Quay lại trang trước (Dashboard) |
| 2 | Notification List | List/Table (nhiều bản ghi) | Sắp xếp mới nhất trên đầu | BR-158 | Report 3 §3.3.2.1.13 (gốc gọi "Notification History") |
| 3 | Warning Badge (mỗi item) | Badge màu | Đỏ = cảnh báo dị ứng/an toàn thực phẩm, Vàng = tin tức, Xám = hệ thống | — (quy ước màu theo `Design_System.md` mục 3; BR-123 là BR gần nhất về cảnh báo dị ứng nhưng chỉ áp dụng cho ngữ cảnh quét ID học sinh, không phải quy tắc màu badge tổng quát — xem ghi chú) | Không dùng chỉ màu sắc — kèm icon/label theo `Accessibility.md` |
| 4 | Sent Time (mỗi item) | Text (giờ/ngày) | — | — | |
| 5 | Click vào 1 item | Interaction | — | — | Điều hướng sang `08_NotificationDetail.md` (chỉ actor có cột `x` ở Notification Detail: Teacher, Parent — với School Administration/Service Caterers hiện chi tiết dạng mở rộng inline/panel, xem ghi chú Platform Layout) |

### Trạng thái màn hình
- Loading: `Skeleton` dạng list khi tải danh sách thông báo.
- Empty: illustration + câu "Chưa có thông báo nào" kèm gợi ý (theo `Component_Guidelines.md` — không dùng câu chung chung).
- Error: lỗi tải danh sách → `Alert variant="destructive"` kèm nút "Thử lại".
- Success: không có Toast riêng (đây là màn hình xem, không phải hành động ghi).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ icon chuông ở Topbar (dropdown ngắn) + link "Xem tất cả" mở trang đầy đủ; School Administration/Service Caterers không có Notification Detail riêng theo Matrix → khi click item, mở rộng nội dung trực tiếp trong `Sheet`/panel bên phải thay vì điều hướng trang.
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — trang đầy đủ dùng `Table`/`DataTable` (cột: Badge mức độ, Nội dung tóm tắt, Sent Time) theo `Component_Guidelines.md` mục "Bảng dữ liệu"; có filter theo mức độ (Đỏ/Vàng/Xám) phía trên bảng.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ icon chuông trên Dashboard hoặc mục điều hướng riêng (tùy actor); click item → `08_NotificationDetail.md` (Teacher/Parent) hoặc mở rộng inline (School Administration/Service Caterers không có Detail riêng).
**Bố cục**: Report 3 §3.3.2.1.13 — trang riêng full-screen, danh sách `ListTile`/Card cuộn dọc, mỗi item có Warning Badge (Chip màu) bên trái + Sent Time bên phải, pull-to-refresh bắt buộc (`Layout_Guidelines.md` mục 2.1).

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Notification Center/List, nền tảng [Web/Mobile], actor ScA/Teacher/SC/Parent.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: danh sách thông báo, Warning Badge (đỏ/vàng/xám kèm icon/label), Sent Time.
Áp dụng Design System: danger #B91C1C cho badge đỏ, warning #B45309 cho vàng, secondary #64748B cho xám.
Trạng thái cần thể hiện: loading (Skeleton), empty (illustration + hướng dẫn), error (Alert destructive).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5475–5486 (§3.3.2.1.13, gốc gọi "Notification History").
- business_rules.md (BR-138, BR-158, BR-159); `00_Foundation/Design_System.md` mục 3 (quy ước màu badge — lưu ý: tài liệu này trích dẫn "BR-123" cho quy tắc màu đỏ cảnh báo dị ứng, nhưng BR-123 **không tồn tại** trong `business_rules.md`/`traceability_matrix.md` — có khả năng là lỗi đánh số sót lại, cần team xác nhận lại nguồn đúng, tạm dùng BR-123 làm tham chiếu gần nhất).
- error_message.md (MSG-068).

**Ghi chú**: không có UC-### chính thức nào ánh xạ trực tiếp cho màn hình xem danh sách này trong `traceability_matrix.md` — nội dung suy ra từ field gốc Report 3 §3.3.2.1.13 và các BR liên quan tới cơ chế gửi thông báo. **[Giả định — cần team xác nhận]** hành vi filter/phân trang cụ thể.
