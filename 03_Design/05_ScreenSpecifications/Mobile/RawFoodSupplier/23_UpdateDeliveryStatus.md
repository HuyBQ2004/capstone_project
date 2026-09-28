# Screen Spec — Update Delivery Status

**Actor**: Raw Food Supplier | **UC liên quan**: UC-092 - Update Delivery Status — extends UC-039 | **Status**: Draft
**BR liên quan**: BR-135, BR-071
**MSG liên quan**: MSG-004, MSG-005, MSG-029, MSG-065

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Mã chuyến giao hàng | — | |
| 2 | Trạng thái hiện tại (chỉ đọc) | Chip | Scheduled/Dispatched/In Transit/Delayed/Delivered | BR-071 | |
| 3 | Cập nhật trạng thái mới | Dropdown | Dispatched/In Transit/Delayed/Delivered | BR-071 | |
| 4 | "Save" Button | Button | Deviation >30 phút → gửi MSG-065 | BR-135 | |
| 5 | "Cancel" Button | Button | Huỷ | — | |

### Trạng thái màn hình
- Loading: Shimmer form.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Warning: MSG-065.
- Success: MSG-029 → về Delivery Detail (20).

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Delivery Detail (20) qua nút "Update Status".
**Bố cục**: Modal bottom sheet form ngắn — ưu tiên thao tác nhanh 1 tay ngoài hiện trường (tài xế cập nhật trạng thái khi đang di chuyển).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Update Delivery Status, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: Modal bottom sheet, form ngắn, thao tác nhanh 1 tay.
Component bắt buộc dùng: Chip, DropdownMenu (M3), FilledButton.
Thành phần bắt buộc: trạng thái hiện tại, dropdown cập nhật trạng thái, nút Save/Cancel.
Áp dụng Design System: chip theo Design_System.md mục 3.
Trạng thái cần thể hiện: warning (MSG-065), success (MSG-029).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3309–3329 (UC-092).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071, BR-135.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-029, MSG-065.
