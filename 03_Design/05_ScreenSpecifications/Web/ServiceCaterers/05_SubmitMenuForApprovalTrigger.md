# Screen Spec — Submit Menu For Approval Trigger

**Actor**: Service Caterers | **UC liên quan**: UC-087 - Submit Menu For Approval (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-061, BR-063, BR-069
**MSG liên quan**: MSG-051 (Warning/Error chặn — xung đột dị ứng), MSG-039 (Success), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tóm tắt thực đơn (Menu Summary) | Read-only card | Hiển thị tuần, danh sách món, tổng dinh dưỡng | — | UC-087 Normal Flow bước 2 |
| 2 | Checklist kiểm tra trước khi nộp (Pre-submission validation checklist) | Checklist read-only tự động đánh dấu | Xác nhận đã khai báo đầy đủ nguyên liệu (BR-069), nộp tối thiểu 3 ngày trước tuần dùng (BR-061) | BR-061, BR-069 | UC-087 Normal Flow bước 2–3 |
| 3 | Kết quả kiểm tra xung đột dị ứng cuối cùng | Alert banner | Chạy lại lần cuối trước khi đổi trạng thái | BR-063 | UC-087 Normal Flow bước 4, MSG-051 |
| 4 | Nút "Xác nhận Nộp Phê duyệt" | Button (primary) | Bị khoá/disabled nếu còn xung đột dị ứng nghiêm trọng chưa xử lý | BR-063 | UC-087 Exceptions: chặn submit nếu phát hiện xung đột nghiêm trọng |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải tóm tắt thực đơn.
- Empty: Không áp dụng.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu submit thất bại; MSG-051 dạng chặn (blocking) nếu còn xung đột dị ứng nghiêm trọng — nút "Xác nhận Nộp" chuyển `disabled` kèm tooltip lý do.
- Success: `Toast` MSG-039, chuyển trạng thái thực đơn sang "Pending Approval", quay về Menu Management Dashboard.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Menu Management Dashboard (hành động "Submit" trên dòng Draft) hoặc từ cuối Create Menu Workspace (`09_Service_Caterers_Screen_Flow_Web.md`: `SubmitMenu -.-> SchoolAdminMenu`). Thoát: sau khi xác nhận, chuyển giao cross-actor sang School Administration (Web) để phê duyệt.
**Bố cục**: `Dialog` xác nhận giữa màn hình (form ngắn, theo `Layout_Guidelines.md` mục 1.2) chứa tóm tắt + checklist + banner cảnh báo + nút hành động.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Menu For Approval Trigger, nền tảng Web, actor Service Caterers.
Bố cục: Dialog giữa màn hình gồm tóm tắt thực đơn, checklist tự động, banner cảnh báo dị ứng, nút Xác nhận Nộp Phê duyệt.
Thành phần bắt buộc: checklist trạng thái đạt/chưa đạt, banner MSG-051 nếu có xung đột, nút hành động disabled khi bị chặn.
Áp dụng Design System: banner cảnh báo dùng token `danger` khi chặn, `warning` khi chỉ cảnh báo không chặn (xem Design_System.md).
Trạng thái cần thể hiện: loading, error (chặn submit), success (MSG-039).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2911–2929 (UC-087 - Submit Menu For Approval)
- business_rules.md BR-061, BR-063, BR-069
- error_message.md MSG-051, MSG-039
