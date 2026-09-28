# Screen Spec — Submit Delivery Information

**Actor**: Raw Food Supplier | **UC liên quan**: UC-083 - Submit Delivery Information (Report 3 §3.2, dòng 3287–3307) | **Status**: Draft
**BR liên quan**: BR-071 (chứng nhận lô hàng bắt buộc — nộp trước tối thiểu 24 giờ)
**MSG liên quan**: MSG-004, MSG-005 (chặn nếu nộp <24 giờ trước giờ giao dự kiến, theo BR-071)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Submit Delivery Information" | — | UC-083 Normal Flow bước 1 |
| 2 | Purchase Request đích (chọn từ danh sách đã Accept) | Select | Bắt buộc, chỉ hiện các PR đã Accept | — | Liên kết tới Purchase Request Detail (11) |
| 3 | Dispatch Date | Date picker | Bắt buộc, phải ≥24 giờ trước thời điểm giao dự kiến | BR-071 | Nếu <24 giờ → chặn submit |
| 4 | Vehicle License Plate | Input text | Bắt buộc | BR-071 | |
| 5 | Driver Details (chọn từ Delivery Person đã đăng ký) | Select | Bắt buộc | BR-050 | Tham chiếu danh sách Delivery Person (ngoài phạm vi 26 màn RFS — quản lý ở UC-019/UC-068, không có màn riêng trong Authorization Matrix khối 5) |
| 6 | Liên kết lô nguyên liệu (Linked Batches) | Multi-select | Bắt buộc ≥1, chọn từ Batch List (13) | BR-071 | |
| 7 | "Submit" Button | Button | Validate thời gian ≥24h + đầy đủ field | BR-071 | |
| 8 | "Cancel" Button | Button | Huỷ, về Deliveries (18)/Delivery List (19) | — | |

### Trạng thái màn hình
- Loading: Skeleton form.
- Empty: N/A.
- Error: Chặn submit nếu nộp <24 giờ trước giờ giao (BR-071) — hiển thị cảnh báo trực tiếp trên field Dispatch Date.
- Success: Trạng thái chuyển 'Scheduled', Service Caterers được thông báo → điều hướng Delivery Detail (20) của chuyến vừa tạo.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Deliveries (18)/Delivery List (19) qua "Add / Plus" Icon. Điều hướng đi: Delivery Detail (20).
**Bố cục**: Form dài chia bước dùng Stepper ngang (`Layout_Guidelines.md` §1.2) — Bước 1: chọn Purchase Request + Dispatch Date; Bước 2: Vehicle/Driver; Bước 3: Liên kết lô nguyên liệu.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Submit Delivery Information, nền tảng Web, actor Raw Food Supplier.
Bố cục: Stepper ngang 3 bước — chọn Purchase Request/Dispatch Date, Vehicle/Driver, liên kết lô nguyên liệu.
Component bắt buộc dùng: Stepper, Select, DatePicker, Input, Button.
Thành phần bắt buộc: Purchase Request đích, Dispatch Date, Vehicle License Plate, Driver, liên kết lô nguyên liệu, nút Submit/Cancel.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: error (nộp <24h), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3287–3307 (UC-083).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-050, BR-071.
