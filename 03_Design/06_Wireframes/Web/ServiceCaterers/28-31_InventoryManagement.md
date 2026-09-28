# Wireframe — Inventory Management (nhóm màn hình Service Caterers, Web)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/ServiceCaterers/28,29,30,31_*.md` — 4 file Screen Specification (Status: Draft — xem cảnh báo mục 4).
**Công cụ sinh**: Thủ công — skill `wireframer` (agilek/wireframer-skill), layout Sidebar + Topbar giống mẫu `Web/SchoolAdministration/`.
**File/Link**: `03_Design/06_Wireframes/Web/ServiceCaterers/28-31_InventoryManagement.html`
**Nguồn điều hướng**: `03_Design/04_ScreenFlow/09_Service_Caterers_Screen_Flow_Web.md` — nhánh `Dash → InvDash(28) → IssueWS(29)/ExpiryPanel(30)/InvLog(31)`, `InvDash -->|"low stock"| CreatePR` (ngoài phạm vi, xem mục 5).

---

## 1. Danh sách 4 Screen Spec → 4 screen + 1 modal trong file HTML

| STT | Màn hình | File Screen Spec nguồn |
| --- | --- | --- |
| 28 | Inventory Management Dashboard | `28_InventoryManagementDashboard.md` (entry) |
| 29 | Issue Ingredients Workspace | `29_IssueIngredientsWorkspace.md` |
| 30 | Monitor Expiration Dates Panel | `30_MonitorExpirationDatesPanel.md` |
| 31 | Record Inventory Transactions Log | `31_RecordInventoryTransactionsLog.md` |

Modal `#adjust-modal` (Điều chỉnh Kho) dùng chung cho cả nút ở Dashboard (28) và Transactions Log (31), đúng Entry/Exit points cả 2 Screen Spec đã ghi ("Dialog form điều chỉnh ngắn").

---

## 2. Đối chiếu với Screen Specification

### 28. Inventory Management Dashboard
- [x] Đủ field: banner MSG-018, filter/search, Table 3 nguyên liệu (đủ/thiếu/expiring soon), action Xuất kho theo dòng, nút Điều chỉnh Kho (modal), nút Tạo Yêu cầu Mua hàng (disabled) — đúng 8 dòng Business Logic.

### 29. Issue Ingredients Workspace
- [x] Đủ field: select phiên/thực đơn, Table 2 nguyên liệu + lô FEFO + "Đổi lô khác", banner MSG-018, nút "Xác nhận Xuất kho" — đúng 5 dòng Business Logic.

### 30. Monitor Expiration Dates Panel
- [x] Đủ field: Table 2 lô sắp hết hạn (badge Expiring Soon), nút "Xuất kho ngay" — đúng 4 dòng Business Logic.

### 31. Record Inventory Transactions Log
- [x] Đủ field: nút mở Dialog điều chỉnh (chọn lô/số lượng/mã lý do/xác nhận quản lý + cảnh báo chặn), Table nhật ký 2 giao dịch mẫu — đúng 6 dòng Business Logic.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md
- Single-file SPA vanilla HTML/JS; Sidebar nhóm "Nhận hàng & Kho" — 4 mục con của nhóm này active/click được (Receiving Management ở nhóm khác vẫn để disabled trong file này).
- Modal điều chỉnh kho dùng chung `.modal-overlay`/`.modal-box` cho cả 2 điểm gọi.

---

## 4. Cảnh báo nguồn Draft
Toàn bộ 4 Screen Specification nguồn đang **Draft**, chưa qua QA của Nguyễn Phương Thảo.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin (báo lại Thảo — QA)

1. **Kế thừa từ bản Mobile** (`Mobile/ServiceCaterers/28-31_InventoryManagement.md` mục 5): (a) "Create Purchase Request" được nhắc trong ScreenFlow + field #8 của 28 nhưng không có Screen Spec nào trong 37 màn actor Service Caterers — cần xác nhận thuộc actor khác hay bị thiếu; (b) 31 chưa có mã BR chính thức cho reason code/sign-off khi điều chỉnh kho.
