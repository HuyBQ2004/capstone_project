# Wireframe — Inventory Management (nhóm màn hình Service Caterers, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/ServiceCaterers/28,29,30,31_*.md` — 4 file Screen Specification (Status: Draft — xem cảnh báo mục 4).
**Công cụ sinh**: Thủ công — skill `wireframer` (agilek/wireframer-skill), khung phone-frame 390px.
**File/Link**: `03_Design/06_Wireframes/Mobile/ServiceCaterers/28-31_InventoryManagement.html`
**Nguồn điều hướng**: `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` — nhánh `Grid → InvDash(28) → MonExp(30)/IssueWS(29) → InvLog(31)`, `InvDash -->|"low stock"| CreatePR` (CreatePR ngoài phạm vi actor Service Caterers, không có Screen Spec trong dự án — dựng `disabled`).

---

## 1. Danh sách 4 màn hình (4 `<div class="screen">` + 2 modal bottom-sheet cùng nội dung)

| STT | Màn hình | File Screen Spec nguồn |
| --- | --- | --- |
| 28 | Inventory Management Dashboard | `28_InventoryManagementDashboard.md` (entry) |
| 29 | Issue Ingredients Workspace | `29_IssueIngredientsWorkspace.md` |
| 30 | Monitor Expiration Dates Panel | `30_MonitorExpirationDatesPanel.md` |
| 31 | Record Inventory Transactions Log | `31_RecordInventoryTransactionsLog.md` |

Modal "Điều chỉnh Kho" (`#adjust-modal`, `#adjust-modal-2`) là field #3 của Screen Spec 28 và field #1-5 của Screen Spec 31 — cùng 1 form, xuất hiện ở cả 2 điểm vào (Dashboard nút "Điều chỉnh Kho" và Transactions Log nút "Điều chỉnh Kho Mới") đúng Entry/Exit points đã ghi trong Screen Spec 31.

---

## 2. Đối chiếu với Screen Specification

### 28. Inventory Management Dashboard
- [x] Đủ field: banner MSG-018 thiếu kho (Failed — đúng trọng tâm AI Generation Prompt), card list 3 nguyên liệu (đủ/thiếu/expiring soon), nút Xuất kho, nút Điều chỉnh Kho (modal), nút Tạo Yêu cầu Mua hàng (disabled, ngoài phạm vi), 2 icon AppBar (Cảnh báo Hết hạn/Nhật ký Giao dịch) — đúng 8 dòng Business Logic.
- Kế thừa nguyên văn cảnh báo mâu thuẫn nguồn BR-081/082/083/089/090 → dùng BR-075/076/077/089 đã có trong Screen Spec 28.

### 29. Issue Ingredients Workspace
- [x] Đủ field: select phiên/thực đơn, card list 2 nguyên liệu + lô đề xuất FEFO + nút "Đổi lô khác", banner MSG-018 thiếu kho, nút "Xác nhận Xuất kho" — đúng 5 dòng Business Logic.

### 30. Monitor Expiration Dates Panel
- [x] Đủ field: card list 2 lô sắp hết hạn (chip Expiring Soon), sắp xếp theo mức khẩn cấp, nút "Xuất kho ngay" — đúng 4 dòng Business Logic.

### 31. Record Inventory Transactions Log
- [x] Đủ field: nút mở form điều chỉnh (chọn lô, số lượng +/-, mã lý do, xác nhận quản lý), cảnh báo chặn nếu thiếu mã lý do/sign-off, card list nhật ký 2 giao dịch mẫu (Ngày/Nguyên liệu/Số lượng/Lý do/Người thực hiện) — đúng 6 dòng Business Logic.
- Kế thừa nguyên văn cảnh báo mâu thuẫn nguồn quan trọng của Screen Spec 31 (BR-082 sai chủ đề, chưa có mã BR chính thức cho reason code/sign-off).

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md
- Single-file SPA vanilla HTML/JS, khung `.phone-frame` (390px), 4 `<div class="screen">` + 2 modal bottom-sheet dùng chung nội dung form điều chỉnh kho.
- Nút "Tạo Yêu cầu Mua hàng" dựng `disabled` kèm `title` giải thích lý do (ngoài phạm vi actor, chưa có Screen Spec `CreatePR` trong dự án) — không tự bịa nội dung màn đó.

---

## 4. Cảnh báo nguồn Draft
Toàn bộ 4 Screen Specification nguồn đang **Draft**, chưa qua QA của Nguyễn Phương Thảo.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin (báo lại Thảo — QA)

1. **28_InventoryManagementDashboard.md — nút "Tạo Yêu cầu Mua hàng" trỏ tới màn "Create Purchase Request" không tồn tại trong 37 Screen Spec của actor Service Caterers**: cả ScreenFlow (`InvDash -->|"low stock"| CreatePR`) và Business Logic field #8 đều nhắc tới màn này nhưng không có file Screen Spec nào trong `05_ScreenSpecifications/Web|Mobile/ServiceCaterers/` mô tả nó — có thể thuộc actor khác (VD School Administration — phần mua sắm/ngân sách) hoặc bị thiếu sót khi phân rã UC. Cần Thảo xác nhận đây có nằm trong phạm vi 37 màn của Minh hay cần bổ sung ở actor khác.
2. **31_RecordInventoryTransactionsLog.md — chưa có mã BR chính thức cho "mã lý do + xác nhận quản lý"**: đã ghi nhận sẵn trong chính Screen Spec ("[Giả định — cần team xác nhận, bổ sung mã BR mới]") — nhắc lại để gộp vào bảng TODO chung.
