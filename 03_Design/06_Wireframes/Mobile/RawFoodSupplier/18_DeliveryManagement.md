# Wireframe — Delivery Management (nhóm màn hình Raw Food Supplier, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/RawFoodSupplier/` — 7 file Screen Specification (Draft).
**Công cụ sinh**: Thủ công — skill `wireframer`, vanilla HTML/JS SPA, khung phone-frame nhất quán.
**File/Link**: `03_Design/06_Wireframes/Mobile/RawFoodSupplier/18_DeliveryManagement.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md` (Dashboard → Delivery Management → Submit Delivery Information → Confirm Delivery Schedule/Update Delivery Status/View Delivery Batch Records).

---

## 1. Danh sách 7 màn hình

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 18 | Deliveries | `18_Deliveries.md` | Entry + FAB |
| 19 | Delivery List | `19_DeliveryList.md` | UC-039 |
| 20 | Delivery Detail | `20_DeliveryDetail.md` | Bundled UC-039 |
| 21 | Submit Delivery Information | `21_SubmitDeliveryInformation.md` | UC-083 |
| 22 | Confirm Delivery Schedule | `22_ConfirmDeliverySchedule.md` | UC-015 |
| 23 | Update Delivery Status | `23_UpdateDeliveryStatus.md` | UC-092 |
| 24 | View Delivery Batch Records | `24_ViewDeliveryBatchRecords.md` | UC-109 |

Tổng cộng: **7 `<div class="screen">`**, không có modal (mọi form là trang riêng, dựng SPA trang liền mạch qua `showScreen`).

---

## 2. Đối chiếu với Screen Specification

Tương ứng 1-1 với bản Web cùng nhóm (`Web/RawFoodSupplier/18_DeliveryManagement.md` mục 2) — cùng field, khác bố cục (Card 1 cột thay Table, AppBar+BottomNav thay Sidebar+Topbar, FAB "+" cho hành động Submit Delivery Information theo `Layout_Guidelines.md` §2.1).

- [x] 18. Deliveries — đủ field, bộ đếm nhanh theo trạng thái (Scheduled/In Transit/Delivered), FAB mở Submit Delivery Information.
- [x] 19. Delivery List — đủ field (Vehicle, Driver, Linked Batches, Schedule Status badge/chip).
- [x] 20. Delivery Detail — đủ field, nút Edit/Cancel Delivery/Update Status/View Batch Records.
- [x] 21. Submit Delivery Information — đủ 5 field + Submit/Cancel; cảnh báo chặn <24h (BR-071) dựng bằng alert-warning.
- [x] 22. Confirm Delivery Schedule — đủ field (Delivery Date, Estimated Arrival Time Window, Delivery Person); cảnh báo yêu cầu Face Biometric (BR-050) dựng bằng alert-warning.
- [x] 23. Update Delivery Status — đủ field (trạng thái hiện tại dạng chip, dropdown cập nhật); MSG-065 (delay >30 phút, BR-135) dựng bằng alert-warning.
- [x] 24. View Delivery Batch Records — đủ field rút gọn dạng Card (Vehicle, Dispatch Timestamp, Linked Batch Codes).

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- 7 `<div class="screen">`, FAB xuất hiện ở màn 18 (mở Submit Delivery Information) theo đúng Platform Layout — Mobile của Screen Spec 18.
- BottomNav mục "Giao hàng" active/click được, đồng bộ 5 mục (Trang chủ/Đơn hàng/Giao hàng/Lô NL/Thêm) với các nhóm Mobile trước.
- Grayscale/mono, wired-elements, Patrick Hand, sketchy border, chip theo kiểu viền (dotted=info, dashed=warning).
- Đây là nhóm lớn nhất của actor RFS (7 màn) nhưng vẫn không có GPS/camera bắt buộc — đúng ghi chú "Khác biệt so với bản Web" không tồn tại trong `06_Raw_Food_Supplier_Screen_Flow.md` (không có yêu cầu định vị thời gian thực cho vai trò Supplier).

## 4. Cảnh báo nguồn Draft

Toàn bộ Screen Specification nguồn đang Draft, chưa qua QA của Nguyễn Phương Thảo.

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin/mâu thuẫn

Giống bản Web cùng nhóm (`Web/RawFoodSupplier/18_DeliveryManagement.md` mục 5): field "Driver Details"/"Delivery Person" (màn 21/22) tham chiếu danh sách Delivery Person đã đăng ký (UC-019/UC-068/UC-028/UC-110), nhưng không có màn hình quản lý Delivery Person nào trong 26 màn chính thức của Raw Food Supplier theo `14_Screen_Authorization_Matrix.md` khối 5. Dropdown trong wireframe chỉ hiển thị dữ liệu mẫu. Cần Thảo xác nhận đây có phải khoảng trống thật của Screen Authorization Matrix hay chủ đích ngoài phạm vi 26 màn.
