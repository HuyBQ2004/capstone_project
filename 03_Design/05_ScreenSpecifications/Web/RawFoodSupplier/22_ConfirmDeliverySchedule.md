# Screen Spec — Confirm Delivery Schedule

**Actor**: Raw Food Supplier | **UC liên quan**: UC-015 - Confirm Delivery Schedule (Report 3 §3.2, dòng 3023–3043) | **Status**: Draft
**BR liên quan**: BR-050 (xác thực sinh trắc học Delivery Person phải tồn tại), BR-071 (nộp lịch ≥24 giờ trước dispatch)
**MSG liên quan**: MSG-004, MSG-005 (chặn nếu <24 giờ theo BR-071)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Confirm Delivery Schedule" | — | UC-015 Normal Flow bước 1 |
| 2 | Danh sách Purchase Request đã Accept, chờ lên lịch | List | Đơn đã accept nhưng chưa dispatch | — | |
| 3 | Delivery Date | Date picker | Bắt buộc | BR-071 | |
| 4 | Estimated Arrival Time Window | Input (time range) | Bắt buộc | BR-071 | |
| 5 | Delivery Person (chọn từ danh sách đã đăng ký) | Select | Bắt buộc; hệ thống xác minh delivery person có hồ sơ sinh trắc học (Face Biometric) | BR-050 | Nếu chưa có face biometric → cảnh báo, chặn xác nhận (UC-015 Normal Flow bước 4) |
| 6 | "Confirm Schedule" Button | Button | Validate ≥24 giờ trước dispatch (BR-071) | BR-050, BR-071 | Nếu <24 giờ → chặn, hiển thị cảnh báo |
| 7 | "Cancel" Button | Button | Huỷ | — | |

### Trạng thái màn hình
- Loading: Skeleton danh sách PR chờ lên lịch.
- Empty: Không có PR nào đang chờ lên lịch.
- Error: Chặn xác nhận nếu <24 giờ trước dispatch, hoặc delivery person chưa có face biometric.
- Success: Cập nhật trạng thái delivery, ghi log, thông báo Service Caterers → điều hướng Delivery Detail (20).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Purchase Request Detail (11) qua nút "Accept". Tương ứng node `PRResponse["Confirm Quantity & Delivery Date"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`/`06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: Delivery Detail (20).
**Bố cục**: Form ngắn trong `Dialog`/`Sheet` (`Layout_Guidelines.md` §1.2).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Confirm Delivery Schedule, nền tảng Web, actor Raw Food Supplier.
Bố cục: Dialog/Sheet form ngắn.
Component bắt buộc dùng: Dialog, Select, DatePicker, Input (time range), Button.
Thành phần bắt buộc: Delivery Date, Estimated Arrival Time Window, Delivery Person, nút Confirm Schedule/Cancel.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: error (thiếu biometric hoặc <24h), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3023–3043 (UC-015).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-050, BR-071.
- `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md`, `06_Raw_Food_Supplier_Screen_Flow.md` (node `PRResponse`).
