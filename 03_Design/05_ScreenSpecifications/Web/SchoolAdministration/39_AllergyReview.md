# Screen Spec — Allergy Review

**Actor**: School Administration | **UC liên quan**: *(chưa có UC chính thức cho School Admin — TODO)* | **Status**: Draft — **[Giả định — cần team xác nhận]**
**BR liên quan**: BR-030 (liên kết dị ứng với hồ sơ học sinh), BR-031 (xét duyệt bởi Medical Staff), BR-175 (phân quyền xem báo cáo)
**MSG liên quan**: MSG-004, MSG-005

> **Nguồn**: Không có UC nền tảng chính thức cho School Administration. Theo `02_School_Administration_Screen_Flow.md`: "School Administration có quyền xem (không phải approver — đó là Medical Staff, UC-078/UC-036) theo BR-175 (phân quyền xem báo cáo)." Đây là màn hình **chỉ xem** (read-only), không có action Approve/Reject (khác với Medical Staff).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo Class/Grade | Select | — | — | **[Giả định]** |
| 2 | Filter theo loại dị ứng | Select | — | BR-066 | **[Giả định]** |
| 3 | Bảng danh sách hồ sơ dị ứng đã xác nhận | Table (đọc-only) | Cột: Học sinh, Lớp, Loại dị ứng, Trạng thái xác nhận (Medical Staff) | BR-030, BR-031 | Không có action Approve/Reject — đây là quyền riêng của Medical Staff (UC-078) |
| 4 | Nút "Export PDF/Excel" | Button | — | BR-175 | |

### Trạng thái màn hình
- Loading: **[Giả định]**.
- Empty: "Chưa có hồ sơ dị ứng nào được xác nhận".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ (chỉ xem).

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Quản lý Lớp/Học sinh" hoặc "Báo cáo & Thống kê".
**Bố cục**: Dashboard Shell — filter bar + bảng đọc-only.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Allergy Review, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (Class/Grade, loại dị ứng), bảng dữ liệu đọc-only.
Thành phần bắt buộc: filter, bảng (Học sinh/Lớp/Loại dị ứng/Trạng thái xác nhận), nút Export.
Áp dụng Design System: badge danger cho cảnh báo dị ứng theo `Design_System.md` mục 3 (màu đỏ bắt buộc cho cảnh báo dị ứng — lưu ý: `Design_System.md`/`Accessibility.md` trích dẫn nguồn gốc quy tắc này là "BR-123", nhưng mã này **không tồn tại** trong `business_rules.md` hiện hành — đã grep toàn văn, không có kết quả. Đây là tham chiếu sai/lỗi thời có sẵn trong tài liệu nền tảng, không phải do file này tạo ra — **[Giả định — cần team xác nhận lại nguồn BR thật cho quy t�