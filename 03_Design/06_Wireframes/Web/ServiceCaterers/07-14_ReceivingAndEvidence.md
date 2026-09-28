# Wireframe — Receiving & Evidence (nhóm màn hình Service Caterers, Web)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/ServiceCaterers/07_..14_*.md` — 8 file Screen Specification (Status: Draft — xem cảnh báo mục 4).
**Công cụ sinh**: Thủ công — skill `wireframer` (agilek/wireframer-skill), phong cách hand-drawn Balsamiq, vanilla HTML/JS single-file SPA (không React/router), layout Sidebar + Topbar giống mẫu đã dùng ở `Web/SchoolAdministration/`.
**File/Link**: `03_Design/06_Wireframes/Web/ServiceCaterers/07-14_ReceivingAndEvidence.html`
**Nguồn điều hướng**: `03_Design/04_ScreenFlow/09_Service_Caterers_Screen_Flow_Web.md` — nhánh `Dash → RecvDash → RecvWS → (VerifyPanel/RejectForm) → RecvDash → RecvHist`; khác biệt quan trọng so với Mobile: **Verify Supplier Delivery Panel (09), Capture Receiving Evidence (10) và Record Ingredient Receiving (13) không phải màn điều hướng riêng trên Web** — cả 3 đều được Screen Spec của chính chúng ghi rõ "Entry/Exit points: Mở từ Receive Ingredients Workspace (tab '...')", khớp với Platform Layout — Web của màn 08 ("Layout dạng Wizard theo Tabs ngang: Xác minh → Kiểm tra chất lượng → Bằng chứng → Xác nhận"). Do đó 3 màn này được bundled thành 3 tab trong cùng 1 `<div class="screen" id="workspace">`, đúng tinh thần bundling đã áp dụng cho Verification Documents ở `Web/SchoolAdministration/01_DashboardPartnershipMenuOversight.md`.

---

## 1. Danh sách 8 Screen Spec → 3 screen + 2 modal trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Dạng trong HTML |
| --- | --- | --- | --- |
| 07 | Receiving Management Dashboard | `07_ReceivingManagementDashboard.md` | Screen `#dashboard` (entry) |
| 08 | Receive Ingredients Workspace | `08_ReceiveIngredientsWorkspace.md` | Screen `#workspace` (khung Tabs + 2 banner + khối webcam) |
| 09 | Verify Supplier Delivery Panel | `09_VerifySupplierDeliveryPanel.md` | Tab "Xác minh" (`#tab-verify`) trong `#workspace` |
| 10 | Capture Receiving Evidence Screen | `10_CaptureReceivingEvidenceScreen.md` | Tab "Bằng chứng" (`#tab-evidence`) trong `#workspace` |
| 11 | Store Ingredients Form | `11_StoreIngredientsForm.md` | Modal `#store-modal` (đúng Platform Layout — Web: "Form ngắn trong Dialog") |
| 12 | Reject Ingredient Batch Form | `12_RejectIngredientBatchForm.md` | Modal `#reject-modal` (đúng Platform Layout — Web: "Dialog form ngắn") |
| 13 | Record Ingredient Receiving | `13_RecordIngredientReceiving.md` | Tab "Xác nhận" (`#tab-confirm`) trong `#workspace` |
| 14 | Receiving History & Reports | `14_ReceivingHistoryAndReports.md` | Screen `#history` |

Tổng cộng: **3 `<div class="screen">`** (dashboard/workspace/history) + **3 tab-panel** trong workspace + **2 modal overlay** (Reject/Store).

---

## 2. Đối chiếu với Screen Specification

### 07. Receiving Management Dashboard
- [x] Đủ field: Table (Nhà cung cấp/Giờ dự kiến/Trạng thái badge/Hành động), link "Xem Lịch sử", filter bar (ngày/nhà cung cấp) — đúng 5 dòng Business Logic, đủ 4 badge trạng thái mẫu.
- [x] Đủ trạng thái: Success là bảng chính; Empty/Loading/Error ghi chú state-legend.

### 08. Receive Ingredients Workspace (khung Tabs)
- [x] Đủ field: header thông tin phiên, banner Approved (BR-070/197), banner GPS ngoài khu vực (BR-079, MSG-015) kèm link nhập/xác nhận tọa độ thủ công (đúng khác biệt Web ghi trong ScreenFlow Web), khối webcam xác thực khuôn mặt (đánh dấu "thứ yếu — khuyến nghị Mobile"), 4 Tab (Xác minh/Kiểm tra chất lượng/Bằng chứng/Xác nhận), nút Log Rejections riêng mở Dialog — đúng 6 dòng Business Logic field #1-2-3-4-5, field #6 ("Hoàn tất Phiên") thực chất chính là hành động "Xác nhận Hoàn tất..." trong tab Xác nhận (map field #4 của Screen Spec 13) nên không lặp thêm nút riêng ngoài tab, tránh trùng 2 nút cùng chức năng.
- **Lưu ý phạm vi**: tab "Kiểm tra chất lượng" (tương ứng field #5 "Inspect Quality") **không có Screen Spec riêng**, dựng ở trạng thái `disabled` — giống cách xử lý bên bản Mobile, cùng 1 khoảng trống cần Thảo xác nhận (xem mục 5).

### 09. Verify Supplier Delivery Panel (tab "Xác minh")
- [x] Đủ field: card thông tin NCC + badge Approved, table chứng nhận lô hàng, khối kết quả xác thực khuôn mặt (dựng trạng thái Failed để minh hoạ đầy đủ Manual Override — lý do + ảnh), nút "Xác nhận Hồ sơ Hợp lệ" — đúng 4 dòng Business Logic.
- [x] Bố cục 2 cột (`col-6`/`col-6`) đúng gợi ý "Panel/Dialog trong Workspace, 2 khối" của Platform Layout — Web.

### 10. Capture Receiving Evidence Screen (tab "Bằng chứng")
- [x] Đủ field: input file + preview thumbnail lưới (không có camera, đúng khác biệt Web "upload file thay chụp trực tiếp"), khối GPS/timestamp (ghi rõ "đọc lại từ Geolocation API / nhập thủ công"), nút "Lưu Bằng chứng" — đúng 4 dòng Business Logic. Ghi chú rõ "secondary path, độ tin cậy thấp hơn Mobile" đúng nguyên văn ScreenFlow Web.
- [x] Offline không áp dụng trên Web (BR-197 luôn yêu cầu kết nối) — không dựng badge "Chờ đồng bộ" ở bản Web, đúng khác biệt đã ghi trong `09_Service_Caterers_Screen_Flow_Web.md`.

### 11. Store Ingredients Form (Modal)
- [x] Đủ field: Table lô hàng chờ xếp kho (cột trái), input mã vạch + Select/RadioGroup khu vực lưu trữ (cột phải), nút "Xác nhận Vị trí Lưu trữ" — đúng 4 dòng Business Logic, bố cục Dialog 2 cột đúng gợi ý Platform Layout — Web.
- [x] Case lỗi BR-202 (khu vực không khớp nhiệt độ; mã cũ ghi nhầm BR-081 — đã repoint 2026-07-01, xem `11_StoreIngredientsForm.md`) dựng trực tiếp làm trạng thái chính của Dialog.

### 12. Reject Ingredient Batch Form (Modal)
- [x] Đủ field: card thông tin lô hàng, `wired-listbox` lý do từ chối (multi-choice), upload ảnh, checkbox "Từ chối một phần" + input số lượng, nút "Xác nhận Từ chối" (destructive) — đúng 5 dòng Business Logic.
- **Kế thừa cảnh báo mâu thuẫn nguồn đã có trong Screen Spec 12 (bản Web)**: không có mã MSG Success riêng cho reject — dùng Toast chung không gắn mã MSG cụ thể, đúng "[Giả định — cần team xác nhận]" đã ghi trong file nguồn.

### 13. Record Ingredient Receiving (tab "Xác nhận")
- [x] Đủ field: 3 Card tóm tắt (NCC/Chứng nhận & Kiểm tra/Bằng chứng), cảnh báo cold chain (6.2°C > 5°C, BR-080), số lô truy xuất nguồn gốc (read-only), nút "Xác nhận Hoàn tất Phiên Nhận hàng" mở tiếp Modal Store Ingredients Form — đúng Entry/Exit points của cả 2 Screen Spec 13 và 11 ("sau khi xác nhận, điều hướng tự động sang Store Ingredients Form").

### 14. Receiving History & Reports
- [x] Đủ field: Page Header + filter bar (từ ngày/đến ngày/nhà cung cấp) + Table (Ngày/NCC/Số lô/Trạng thái/Xem chi tiết) + nút "Xuất Báo cáo" — đúng 4 dòng Business Logic, đủ 2 badge Received/Rejected mẫu.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md
- Single-file SPA vanilla HTML/JS, layout Sidebar (9 nhóm, chỉ nhóm "Nhận hàng & Kho" active/click được — các nhóm khác `.side-link.disabled` kèm ghi chú "(nhóm khác)", đúng pattern đã dùng ở SchoolAdministration) + Topbar breadcrumb.
- Điều hướng: `showScreen(id)` cho 3 screen chính, `showTab(id)` cho 3 tab trong Workspace, `openModal/closeModal` cho 2 Dialog.
- Grayscale/mono tuyệt đối; `wired-elements` qua CDN; font Patrick Hand; sketchy border trick cho toàn bộ card/table-wrap/modal/tab/button.
- Badge phân biệt bằng kiểu viền (dotted/dashed/solid/3px), không dùng màu.
- Images & Placeholders: Option B — khung viền đứt nét chữ "X" (không có công cụ sinh ảnh khả dụng trong phiên này).

---

## 4. Cảnh báo nguồn Draft
Toàn bộ 8 Screen Specification nguồn đang ở trạng thái **Draft**, chưa qua bước QA của Nguyễn Phương Thảo. Wireframe bám sát nguyên văn Draft hiện có để kịp mốc SDS v1.3 (05/07/2026) theo `06_Wireframes/00_Team_Work_Assignment_Plan.md` mục 2 — cần rà lại nếu Screen Spec đổi sau QA.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin (báo lại Thảo — QA)

1. **08_ReceiveIngredientsWorkspace.md (Web) — tab "Kiểm tra chất lượng" không có Screen Spec riêng**: giống phát hiện ở bản Mobile (xem `Mobile/ServiceCaterers/07-14_ReceivingAndEvidence.md` mục 5.1) — field #5 liệt kê "Inspect Quality" như 1 hành động con nhưng không có file `07-14` mô tả nội dung tab này. Cần Thảo xác nhận cùng 1 quyết định cho cả 2 platform (bổ sung Screen Spec riêng / alias với bước khác / thuộc nhóm Three-Step Inspection và cần xoá khỏi field #5).
2. **08_ReceiveIngredientsWorkspace.md (Web) — field #6 "Nút Hoàn tất Phiên Nhận hàng" trùng chức năng với field #4 của 13_RecordIngredientReceiving.md ("Xác nhận Hoàn tất Phiên Nhận hàng")**: cả 2 Screen Spec đều mô tả 1 nút tương tự nhưng không nói rõ đây là cùng 1 nút (đặt trong tab Xác nhận) hay 2 nút riêng biệt cần xuất hiện đồng thời trên UI. Wireframe này giả định là **cùng 1 nút** (đặt trong tab Xác nhận, theo đúng bố cục Wizard đã mô tả) để tránh trùng lặp hành động gây nhầm lẫn — cần Thảo xác nhận lại.
3. **12_RejectIngredientBatchForm.md (Web) — không có mã MSG Success riêng**: đã ghi nhận sẵn trong chính Screen Spec ("[Giả định — cần team xác nhận]") — nhắc lại tại đây để Thảo gộp vào bảng tổng hợp TODO chung.
