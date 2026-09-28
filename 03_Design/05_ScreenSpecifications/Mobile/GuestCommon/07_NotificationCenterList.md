# Screen Spec — Notification Center/List

**Actor**: School Administration, Teacher, Service Caterers, Parent (cột `x` theo `14_Screen_Authorization_Matrix.md` khối 1, dòng "Notification Center/List") | **UC liên quan**: — (không có UC riêng; nội dung suy ra từ "Notification History" Report 3 §3.3.2.1.13 + BR-159) | **Status**: Draft
**BR liên quan**: BR-138 (cảnh báo quan trọng phát ngay), BR-158 (Quiet Hours, không áp dụng cho cảnh báo khẩn cấp), BR-159 (retry + fallback email)
**MSG liên quan**: MSG-068 (Warning)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Back" | Icon button (AppBar) | — | — | Quay lại Dashboard |
| 2 | Notification List | ListView/Card | Sắp xếp mới nhất trên đầu | BR-158 | Report 3 §3.3.2.1.13 |
| 3 | Warning Badge (mỗi item) | Chip màu (M3) | Đỏ = cảnh báo dị ứng/an toàn thực phẩm, Vàng = tin tức, Xám = hệ thống | — (quy ước màu `Design_System.md` mục 3, xem ghi chú) | Kèm icon/label, không chỉ dùng màu |
| 4 | Sent Time (mỗi item) | Text | — | — | |
| 5 | Tap vào 1 item | Interaction | — | — | Điều hướng sang `08_NotificationDetail.md` (Teacher/Parent) |

### Trạng thái màn hình
- Loading: `Shimmer` dạng list khi tải danh sách.
- Empty: illustration + "Chưa có thông báo nào" kèm gợi ý.
- Error: `SnackBar`/`AlertDialog` màu `danger` kèm nút "Thử lại".
- Success: không có SnackBar riêng (màn hình xem thuần túy).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ icon chuông trên Dashboard hoặc mục điều hướng riêng theo actor; tap item → `08_NotificationDetail.md` (Teacher/Parent).
**Bố cục**: Report 3 §3.3.2.1.13 — trang riêng full-screen, danh sách `ListTile`/Card cuộn dọc, mỗi item có Chip màu bên trái + Sent Time bên phải, pull-to-refresh bắt buộc.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Notification Center/List, nền tảng Mobile, actor ScA/Teacher/SC/Parent.
Bố cục: trang riêng full-screen, ListTile/Card danh sách thông báo, Chip Warning Badge (đỏ/vàng/xám), Sent Time, pull-to-refresh.
Áp dụng Design System: danger #B91C1C, warning #B45309, secondary #64748B.
Trạng thái cần thể hiện: loading (Shimmer), empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5475–5486 (§3.3.2.1.13).
- business_rules.md (BR-138, BR-158, BR-159); `00_Foundation/Design_System.md` mục 3 (quy ước màu badge — lưu ý: tài liệu này trích dẫn "BR-123" nhưng BR-123 **không tồn tại** trong `business_rules.md`/`traceability_matrix.md`, có khả năng là lỗi đánh số sót lại, cần team xác nhận; BR-123 là BR gần nhất về cảnh báo dị ứng).
- error_message.md (MSG-068).

**Ghi chú**: giống bản Web — không có UC-### chính thức. **[Giả định — cần team xác nhận]** hành vi filter/phân trang cụ thể.
