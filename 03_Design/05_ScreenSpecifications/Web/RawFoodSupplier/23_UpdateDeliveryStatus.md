# Screen Spec — Update Delivery Status

**Actor**: Raw Food Supplier | **UC liên quan**: UC-092 - Update Delivery Status (Report 3 §3.2, dòng 3309–3329) — extends UC-039 (Manage Deliveries) | **Status**: Draft
**BR liên quan**: BR-135 (thông báo trễ >30 phút), BR-071
**MSG liên quan**: MSG-004, MSG-005, MSG-029 (Success — cập nhật trạng thái), MSG-065 (Warning — giao hàng trễ so với lịch)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Mã chuyến giao hàng | — | UC-092 Normal Flow bước 1 |
| 2 | Trạng thái hiện tại (chỉ đọc) | Badge | Scheduled/Dispatched/In Transit/Delayed/Delivered | BR-071 | |
| 3 | Cập nhật trạng thái mới (dropdown) | Select | Dispatched / In Transit / Delayed / Delivered | BR-071 | |
| 4 | "Save" Button | Button | Nếu thời gian thực tế lệch >30 phút so với lịch → tự động gửi cảnh báo (MSG-065) tới Service Caterers | BR-135 | |
| 5 | "Cancel" Button | Button | Huỷ | — | |

### Trạng thái màn hình
- Loading: Skeleton form.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Warning: MSG-065 tự động kích hoạt nếu deviation >30 phút (không chặn lưu, chỉ cảnh báo/thông báo).
- Success: MSG-029, ghi audit trail → về Delivery Detail (20).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Delivery Detail (20) qua nút "Update Status". Điều hướng đi: quay lại Delivery Detail (20).
**Bố cục**: Form ngắn trong `Dialog` (`Layout_Guidelines.md` §1.2).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Update Delivery Status, nền tảng Web, actor Raw Food Supplier.
Bố cục: Dialog form ngắn.
Component bắt buộc dùng: Dialog, Badge, Select, Button.
Thành phần bắt buộc: trạng thái hiện tại, dropdown cập nhật trạng thái, nút Save/Cancel.
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: warning (MSG-065), success (MSG-029).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3309–3329 (UC-092).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071, BR-135.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-029, MSG-065.
