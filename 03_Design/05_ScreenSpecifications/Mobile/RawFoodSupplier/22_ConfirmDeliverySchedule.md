# Screen Spec — Confirm Delivery Schedule

**Actor**: Raw Food Supplier | **UC liên quan**: UC-015 - Confirm Delivery Schedule | **Status**: Draft
**BR liên quan**: BR-050, BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Confirm Delivery Schedule" | — | |
| 2 | Danh sách Purchase Request đã Accept, chờ lên lịch | List | — | — | |
| 3 | Delivery Date | Date picker | Bắt buộc | BR-071 | |
| 4 | Estimated Arrival Time Window | Input (time range) | Bắt buộc | BR-071 | |
| 5 | Delivery Person | Select | Bắt buộc, phải có hồ sơ sinh trắc học | BR-050 | |
| 6 | "Confirm Schedule" Button | Button | Validate ≥24 giờ trước dispatch | BR-050, BR-071 | |
| 7 | "Cancel" Button | Button | Huỷ | — | |

### Trạng thái màn hình
- Loading: Shimmer danh sách.
- Empty: Không có PR nào chờ lên lịch.
- Error: Chặn nếu <24 giờ hoặc thiếu biometric.
- Success: → điều hướng Delivery Detail (20).

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Purchase Request Detail (11) qua nút "Accept". Tương ứng node `PRResponse["Confirm Quantity & Delivery Date"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`.
**Bố cục**: Modal bottom sheet 1 trang.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Confirm Delivery Schedule, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: Modal bottom sheet form ngắn.
Component bắt buộc dùng: DropdownMenu, DatePicker (M3), TextField (time range), FilledButton.
Thành phần bắt buộc: Delivery Date, Estimated Arrival Time Window, Delivery Person, nút Confirm Schedule/Cancel.
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3023–3043 (UC-015).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-050, BR-071.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md` (node `PRResponse`).
