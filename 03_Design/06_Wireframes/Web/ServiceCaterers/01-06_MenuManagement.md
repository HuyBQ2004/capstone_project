# Wireframe — Menu Management (nhóm màn hình Service Caterers, Web)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/ServiceCaterers/01,02,03,04,05,06_*.md` — 6 file Screen Specification (Status: Draft — xem cảnh báo mục 4).
**Công cụ sinh**: Thủ công — skill `wireframer` (agilek/wireframer-skill), layout Sidebar + Topbar giống mẫu `Web/SchoolAdministration/`.
**File/Link**: `03_Design/06_Wireframes/Web/ServiceCaterers/01-06_MenuManagement.html`
**Nguồn điều hướng**: `03_Design/04_ScreenFlow/09_Service_Caterers_Screen_Flow_Web.md` — nhánh `Dash → MenuDash(02) → CreateMenu(03) → AIPanel(04)/SubmitMenu(05)`, `MenuDash → UpdateMenu(06)`.

---

## 1. Danh sách 6 Screen Spec → 4 screen + 1 sheet + 1 modal trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Dạng trong HTML |
| --- | --- | --- | --- |
| 01 | Service Caterers Dashboard | `01_ServiceCaterersDashboard.md` | Screen `#dashboard` (entry) |
| 02 | Menu Management Dashboard | `02_MenuManagementDashboard.md` | Screen `#menudash` |
| 03 | Create Menu Workspace | `03_CreateMenuWorkspace.md` | Screen `#create` (Stepper ngang 3 bước) |
| 04 | View Menu Recommendations Panel | `04_ViewMenuRecommendationsPanel.md` | Sheet trượt phải `#ai-sheet` (đúng Platform Layout — Web: "Sheet trượt từ phải") |
| 05 | Submit Menu For Approval Trigger | `05_SubmitMenuForApprovalTrigger.md` | Modal giữa màn `#submit-modal` (đúng Platform Layout — Web: "Dialog xác nhận giữa màn hình") |
| 06 | Update Menu Screen | `06_UpdateMenuScreen.md` | Screen `#update` (Stepper ngang 3 bước, pre-filled) |

---

## 2. Đối chiếu với Screen Specification

### 01. Service Caterers Dashboard
- [x] Đủ field: lời chào, 3 KPI card thống kê, Grid Menu Items 8 mục (chỉ "Thực đơn" clickable, còn lại mờ + `title` trỏ file wireframe tương ứng) — đúng 3 dòng Business Logic + bố cục grid 12 cột đúng Platform Layout — Web.

### 02. Menu Management Dashboard
- [x] Đủ field: Table 3 dòng mẫu (Draft/Rejected/Published, đủ 3 trạng thái + action theo điều kiện BR-067), filter bar, nút "Tạo Thực đơn Mới" — đúng 8 dòng Business Logic.

### 03. Create Menu Workspace
- [x] Đủ field: Stepper ngang 3 bước (Thông tin chung/Chọn món & dinh dưỡng/Xác nhận dị ứng), nút AI Assist mở Sheet, banner MSG-051, nút "Lưu nháp" — đúng 8 dòng Business Logic.

### 04. View Menu Recommendations Panel
- [x] Đủ field: Sheet 480px, tóm tắt ngữ cảnh AI, 2 Option Card (1 an toàn + 1 có badge dị ứng), nút "Áp dụng", ghi chú lưu trữ hội thoại — đúng 6 dòng Business Logic. Kế thừa cảnh báo mâu thuẫn nguồn BR-072→BR-080 đã có trong Screen Spec 04.

### 05. Submit Menu For Approval Trigger
- [x] Đủ field: Dialog giữa màn, tóm tắt thực đơn, checklist 2 mục, banner MSG-051 chặn, nút "Xác nhận Nộp Phê duyệt" **disabled** minh hoạ case chặn (BR-063) — đúng 4 dòng Business Logic.

### 06. Update Menu Screen
- [x] Đủ field: banner bình luận từ chối, Stepper 3 bước pre-filled, banner MSG-075 (giới hạn lặp món), nút AI Assist, banner MSG-051, nút "Lưu thay đổi" — đúng 8 dòng Business Logic. Kế thừa cảnh báo mâu thuẫn nguồn của Screen Spec 06 (BR-072/074 không áp dụng; MSG-075 chưa có mã BR chính thức).

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md
- Single-file SPA vanilla HTML/JS; Sidebar chỉ nhóm "Thực đơn" active/click được.
- Stepper ngang dùng `.stepper-bar`/`.stepper-item` + hàm `showStepperPage(prefix, n)` dùng chung cho `create` và `update`.
- Sheet trượt phải dùng `.sheet-overlay`/`.sheet-panel` (480px, border trái 3px) — tách riêng khỏi `.modal-overlay` (Dialog giữa màn) để đúng 2 kiểu bố cục khác nhau theo Platform Layout — Web của 04 (Sheet) và 05 (Dialog).

---

## 4. Cảnh báo nguồn Draft
Toàn bộ 6 Screen Specification nguồn đang **Draft**, chưa qua QA của Nguyễn Phương Thảo.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin (báo lại Thảo — QA)

1. **Kế thừa từ bản Mobile** (`Mobile/ServiceCaterers/01-06_MenuManagement.md` mục 5): (a) widget thống kê 01 không có field-by-field gốc; (b) định dạng hiển thị "Bình luận từ chối" ở 06 chưa rõ; (c) vị trí nút Lưu nháp/Lưu thay đổi (mỗi trang hay chỉ trang cuối) chưa rõ trong form nhiều trang — bản Web wireframe đặt nút này ngoài Stepper (luôn hiển thị, không đổi theo trang) để tránh phải bấm Next 3 lần mới lưu được, cần Thảo xác nhận có đúng ý đồ nghiệp vụ hay không.
