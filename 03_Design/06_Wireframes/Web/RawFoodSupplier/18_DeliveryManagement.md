# Wireframe — Delivery Management (nhóm màn hình Raw Food Supplier)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/RawFoodSupplier/` — 7 file Screen Specification (Draft).
**Công cụ sinh**: Thủ công — skill `wireframer`, vanilla HTML/JS SPA.
**File/Link**: `03_Design/06_Wireframes/Web/RawFoodSupplier/18_DeliveryManagement.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md` (Dashboard → Delivery Management → Raw Material Shipment Form → Attach Delivery Documents/Shipment Tracking; Purchase Request Detail → Confirm Quantity & Delivery Date).

---

## 1. Danh sách 7 màn hình

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 18 | Deliveries | `18_Deliveries.md` | Entry |
| 19 | Delivery List | `19_DeliveryList.md` | UC-039 |
| 20 | Delivery Detail | `20_DeliveryDetail.md` | Bundled UC-039 |
| 21 | Submit Delivery Information | `21_SubmitDeliveryInformation.md` | UC-083 |
| 22 | Confirm Delivery Schedule | `22_ConfirmDeliverySchedule.md` | UC-015 |
| 23 | Update Delivery Status | `23_UpdateDeliveryStatus.md` | UC-092 |
| 24 | View Delivery Batch Records | `24_ViewDeliveryBatchRecords.md` | UC-109 |

Tổng cộng: **7 `<div class="screen">`**, không có modal (mọi form là trang riêng theo Screen Spec Dialog/Sheet, dựng SPA trang liền mạch).

---

## 2. Đối chiếu với Screen Specification

### 18. Deliveries
- [x] Đủ field: bộ đếm nhanh theo trạng thái (Scheduled/In Transit/Delayed/Delivered) — đúng 4 dòng Business Logic.

### 19. Delivery List
- [x] Đủ field: Add/Plus, Delivery Card (Vehicle, Driver, Linked Batches, Schedule Status), filter/search — đúng 7 dòng Business Logic.
- [x] Đúng Design System *(badge Scheduled=viền chấm/info, In Transit=viền đứt nét/warning)*.

### 20. Delivery Detail
- [x] Đủ field: Vehicle, Driver, Linked Batches, Schedule Status, nút Edit/Cancel Delivery/Update Status/View Batch Records — đúng 10 dòng Business Logic.

### 21. Submit Delivery Information
- [x] Đủ field: Purchase Request đích, Dispatch Date, Vehicle License Plate, Driver Details, liên kết lô nguyên liệu, Submit/Cancel — đúng 8 dòng Business Logic.
- [x] Cảnh báo chặn <24h (BR-071) dựng bằng alert-warning.

### 22. Confirm Delivery Schedule
- [x] Đủ field: danh sách PR chờ lên lịch, Delivery Date, Estimated Arrival Time Window, Delivery Person, Confirm/Cancel — đúng 7 dòng Business Logic.
- [x] Cảnh báo yêu cầu Face Biometric (BR-050) dựng bằng alert-warning.

### 23. Update Delivery Status
- [x] Đủ field: trạng thái hiện tại (badge), dropdown cập nhật, Save/Cancel — đúng 5 dòng Business Logic.
- [x] MSG-065 (delay >30 phút, BR-135) dựng bằng alert-warning.

### 24. View Delivery Batch Records
- [x] Đủ field: Vehicle Plaque, Driver Name, Dispatch Timestamp, Linked Batch Codes, filter khoảng ngày + trạng thái — đúng 5 dòng Business Logic.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- 7 `<div class="screen">`, sidebar chỉ nhóm "Giao hàng" active/click được.
- Grayscale/mono, wired-elements, Patrick Hand, sketchy border, badge/alert theo kiểu viền.
- Đây là nhóm lớn nhất của actor RFS (7 màn) nhưng vẫn không có GPS/camera bắt buộc — đúng ghi chú "Khác biệt so với bản Mobile" trong `06_Raw_Food_Supplier_Screen_Flow.md`.

## 4. Cảnh báo nguồn Draft

Toàn bộ Screen Specification nguồn đang Draft, chưa qua QA của Nguyễn Phương Thảo.

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin/mâu thuẫn

1. **21_SubmitDeliveryInformation.md**: field "Driver Details" tham chiếu tới danh sách Delivery Person đã đăng ký (UC-019/UC-068), nhưng **không có màn hình quản lý Delivery Person nào trong 26 màn chính thức của Raw Food Supplier** theo `14_Screen_Authorization_Matrix.md` khối 5 — nghĩa là UC-019 (Create Info Delivery Person), UC-068 (Register Face Data), UC-028 (Delete Face Data), UC-110 (View Delivery Person List) tồn tại trong Report 3 nhưng không map tới màn hình nào trong danh sách 26 màn được giao. Đã ghi chú rõ trong Wireframe (dropdown chỉ hiển thị dữ liệu mẫu, không có màn tạo/sửa Delivery Person). Cần Thảo xác nhận: đây có phải khoảng trống thật của Screen Authorization Matrix hay các UC này được cố ý bỏ ngoài phạm vi 26 màn.
