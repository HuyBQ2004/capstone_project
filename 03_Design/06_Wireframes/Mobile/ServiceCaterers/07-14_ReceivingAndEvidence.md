# Wireframe — Receiving & Evidence (nhóm màn hình Service Caterers, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/ServiceCaterers/07_..14_*.md` — 8 file Screen Specification (Status: Draft — xem cảnh báo mục 4).
**Công cụ sinh**: Thủ công — skill `wireframer` (agilek/wireframer-skill), phong cách hand-drawn Balsamiq, vanilla HTML/JS single-file SPA (không React/router), khung phone-frame 390px.
**File/Link**: `03_Design/06_Wireframes/Mobile/ServiceCaterers/07-14_ReceivingAndEvidence.html`
**Nguồn điều hướng**: `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` — nhánh `RecvDash → RecvWS → (VerifyPanel / CapEv / RejForm / RecordRecv) → StoreForm`, `RecvDash → RecvHist`.

---

## 1. Danh sách 8 màn hình (7 `<div class="screen">` + 1 modal bottom-sheet)

| STT | Màn hình | File Screen Spec nguồn | Dạng trong HTML |
| --- | --- | --- | --- |
| 07 | Receiving Management Dashboard | `07_ReceivingManagementDashboard.md` | Screen `#dashboard` (entry) |
| 08 | Receive Ingredients Workspace | `08_ReceiveIngredientsWorkspace.md` | Screen `#workspace` |
| 09 | Verify Supplier Delivery Panel | `09_VerifySupplierDeliveryPanel.md` | Screen `#verify` |
| 10 | Capture Receiving Evidence Screen | `10_CaptureReceivingEvidenceScreen.md` | Screen `#capture-evidence` |
| 11 | Store Ingredients Form | `11_StoreIngredientsForm.md` | Screen `#store-form` |
| 12 | Reject Ingredient Batch Form | `12_RejectIngredientBatchForm.md` | Modal bottom-sheet `#reject-modal` (đúng `showModalBottomSheet` trong Platform Layout — Mobile) |
| 13 | Record Ingredient Receiving | `13_RecordIngredientReceiving.md` | Screen `#record-receiving` |
| 14 | Receiving History & Reports | `14_ReceivingHistoryAndReports.md` | Screen `#history` |

---

## 2. Đối chiếu với Screen Specification

### 07. Receiving Management Dashboard
- [x] Đủ field: card list lịch giao hàng hôm nay (nhà cung cấp/giờ dự kiến/trạng thái chip), nút "Bắt đầu Nhận hàng" chỉ hiện ở card Dispatched, icon "Lịch sử" trên AppBar, filter chip theo ngày/nhà cung cấp — đúng 5 dòng Business Logic. Có đủ 4 mẫu trạng thái chip (Scheduled/Dispatched/In Progress/Completed).
- [x] Đúng Design System *(ghi chú: bản hand-drawn/grayscale, chip phân biệt bằng kiểu viền dotted/dashed/solid/3px thay màu)*.
- [x] Đủ trạng thái: Success là bảng chính; Empty/Error/Loading ghi chú text trong khối "state-legend" cuối màn (không dựng riêng, theo tinh thần bundling ở SchoolAdministration đã làm trước).

### 08. Receive Ingredients Workspace
- [x] Đủ field: header thông tin phiên, banner Approved (BR-070/197), banner GPS ngoài khu vực + link "xác nhận thủ công + lý do" (BR-079, MSG-015), khối camera xác thực khuôn mặt, 5 step-card con (Verify Invoice/Inspect Quality/Capture Evidence/Log Rejections/Send Receipt), badge ví dụ offline "Chờ đồng bộ", nút "Hoàn tất Phiên" cố định cuối màn — đúng 6 dòng Business Logic.
- [x] Đúng Design System *(banner chặn dùng alert-warning/alert-info viền, không token màu thật)*.
- [x] Đủ trạng thái: Error (GPS ngoài khu vực) dựng trực tiếp làm trạng thái chính của banner GPS (đây là nhóm ưu tiên SDS v1.3 nên cố ý dựng case chặn để review dễ nhất); Success (banner NCC Approved); Loading ghi chú text.
- **Lưu ý phạm vi**: step "Inspect Quality" trong danh sách hành động con (field #5) **không có Screen Spec riêng** trong nhóm 07-14 này (khác Verify Invoice/Capture Evidence/Log Rejections/Send Receipt đều map đúng 1 file). Card này được dựng ở trạng thái `disabled` kèm ghi chú "Ngoài phạm vi nhóm này" — không tự bịa nội dung màn hình cho bước này.

### 09. Verify Supplier Delivery Panel
- [x] Đủ field: card thông tin NCC + badge Approved, list chứng nhận lô hàng (số lô/SX/HD/SL/chứng chỉ), khối kết quả xác thực khuôn mặt (dựng ở trạng thái **Failed** để minh hoạ đầy đủ luồng Manual Override — lý do + ảnh bằng chứng), nút "Xác nhận Hồ sơ Hợp lệ" — đúng 4 dòng Business Logic.
- [x] Đủ trạng thái: Error (MSG-014 + Manual Override) là trọng tâm được dựng đầy đủ; MSG-013 (chứng nhận hết hạn) ghi chú text trong state-legend vì không thể dựng đồng thời 2 case lỗi khác nhau trên cùng 1 ảnh tĩnh không gây nhầm lẫn dữ liệu mẫu.

### 10. Capture Receiving Evidence Screen
- [x] Đủ field: camera view + dải thumbnail (2 ảnh mẫu + nút thêm), khối GPS/timestamp read-only (không sửa được), nút "Lưu Bằng chứng" — đúng 4 dòng Business Logic (field #3 "device info" là background system action, không hiển thị UI theo đúng ghi chú nguồn "ẩn, chỉ log audit" — không dựng vì đúng tinh thần Screen Spec).
- [x] Đủ trạng thái: Error (MSG-009 định dạng ảnh) dựng trực tiếp; Empty/MSG-008/MSG-015/Success (online/offline) ghi chú trong state-legend.

### 11. Store Ingredients Form
- [x] Đủ field: card list lô hàng chờ xếp kho, khối quét mã vạch (camera full-width), RadioListTile 3 khu vực (Dry/Chilled/Frozen), nút "Xác nhận Vị trí Lưu trữ" — đúng 4 dòng Business Logic.
- [x] Đủ trạng thái: Error (chọn Dry cho lô yêu cầu Chilled, BR-202; mã cũ ghi nhầm BR-081 — đã repoint 2026-07-01) dựng trực tiếp làm case chính vì đây là rule quan trọng nhất của màn; Empty/Loading/Success ghi chú state-legend.

### 12. Reject Ingredient Batch Form (bottom-sheet)
- [x] Đủ field: card thông tin lô hàng, checklist lý do (4 mã lý do mẫu, tối thiểu 1 lý do), camera chụp ảnh bằng chứng, switch "Từ chối một phần" + input số lượng, nút "Xác nhận Từ chối" màu danger — đúng 5 dòng Business Logic.
- [x] Ghi chú AlertDialog xác nhận trước khi lưu bằng text (không dựng modal-trong-modal để tránh phức tạp hoá không cần thiết ngoài yêu cầu).
- **Kế thừa cảnh báo mâu thuẫn nguồn đã có trong Screen Spec 12**: không có mã MSG Success riêng cho reject — dùng thông báo chung không gắn mã MSG, đúng như file nguồn đã đánh dấu "[Giả định — cần team xác nhận]".

### 13. Record Ingredient Receiving (Send Receipt)
- [x] Đủ field: 3 summary card (NCC/Chứng nhận & Kiểm tra/Bằng chứng), cảnh báo cold chain (6.2°C > 5°C, BR-080), số lô truy xuất nguồn gốc (read-only), nút "Xác nhận Hoàn tất Phiên Nhận hàng" — đúng 4 dòng Business Logic.
- [x] Điều hướng đúng: xác nhận → tự động sang Store Ingredients Form (khớp Entry/Exit points của Screen Spec 13 và 11).

### 14. Receiving History & Reports
- [x] Đủ field: filter chip (ngày/nhà cung cấp), card list lịch sử (ngày/NCC/số lô/trạng thái Received hoặc Rejected), nút xuất báo cáo — đúng 4 dòng Business Logic.
- [x] Ghi chú giới hạn phạm vi báo cáo theo BR-175/176/178 trong state-legend.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md
- Single-file SPA vanilla HTML/JS, khung `.phone-frame` (390px) mô phỏng thiết bị di động, 7 `<div class="screen">` điều hướng bằng `showScreen(id)`, 1 modal bottom-sheet bằng `openModal/closeModal`.
- Grayscale/mono tuyệt đối; `wired-elements` qua CDN; font Patrick Hand; nền graph-paper; sketchy border trick cho card/step-card/chip/modal/button.
- Nút dùng `.btn-primary/.btn-secondary/.btn-danger` (không dùng `<wired-button>`).
- Icon toàn bộ là SVG line-art doodle inline tự vẽ, không emoji.
- Trạng thái chip/badge phân biệt bằng kiểu viền (dotted/dashed/solid/3px) thay màu.
- Ảnh minh hoạ dùng Option B — khung viền đứt nét chữ "X"/"+" (không có công cụ sinh ảnh khả dụng trong phiên này).

---

## 4. Cảnh báo nguồn Draft
Toàn bộ 8 Screen Specification nguồn đang ở trạng thái **Draft**, chưa qua bước QA của Nguyễn Phương Thảo (mục 6, `05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md`). Wireframe này bám sát nguyên văn Draft hiện có để kịp mốc SDS v1.3 (05/07/2026) theo đúng thứ tự ưu tiên ở `06_Wireframes/00_Team_Work_Assignment_Plan.md` mục 2 — cần rà lại nếu Screen Spec đổi sau QA.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin (báo lại Thảo — QA)

1. **08_ReceiveIngredientsWorkspace.md — bước con "Inspect Quality" không có Screen Spec riêng**: field #5 của Business Logic liệt kê 5 hành động con (Verify Invoice, Inspect Quality, Capture Evidence, Log Rejections, Send Receipt) và ScreenFlow (`04_Service_Caterers_Screen_Flow.md`) cũng có node `InspectQ["Inspect Ingredient Quality"]`, nhưng không có file Screen Spec `07-14` nào mô tả riêng cho bước này (khác 4 bước còn lại đều có file 1-1). Cần Thảo xác nhận: (a) bổ sung Screen Spec riêng cho "Inspect Ingredient Quality", hay (b) đây là alias/trùng lặp với 1 trong 4 bước còn lại, hay (c) bước này thuộc nhóm Three-Step Inspection (16_InspectIngredientsBeforeProcessingScreen — khác UC, UC-062 không phải UC-060) và cần xoá khỏi field #5 của Screen Spec 08 nếu không áp dụng cho UC-060.
2. **12_RejectIngredientBatchForm.md — không có mã MSG Success riêng**: đã ghi nhận sẵn trong chính Screen Spec ("[Giả định — cần team xác nhận]") — nhắc lại tại đây để Thảo gộp vào bảng tổng hợp TODO chung của actor Service Caterers.
