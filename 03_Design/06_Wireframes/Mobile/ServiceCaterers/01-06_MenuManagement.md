# Wireframe — Menu Management (nhóm màn hình Service Caterers, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/ServiceCaterers/01,02,03,04,05,06_*.md` — 6 file Screen Specification (Status: Draft — xem cảnh báo mục 4).
**Công cụ sinh**: Thủ công — skill `wireframer` (agilek/wireframer-skill), khung phone-frame 390px, vanilla HTML/JS single-file SPA.
**File/Link**: `03_Design/06_Wireframes/Mobile/ServiceCaterers/01-06_MenuManagement.html`
**Nguồn điều hướng**: `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` — nhánh `Dash → Grid → MenuDash(02) → CreateMenuWS(03) ⇄ AIPanel(04) → SubmitTrigger(05)`, `MenuDash → UpdateMenuScr(06) → SpecialMealCfg` (SpecialMealCfg ngoài phạm vi nhóm này, thuộc nhóm Meal Service #27).

---

## 1. Danh sách 6 màn hình (4 `<div class="screen">` + 2 modal bottom-sheet)

| STT | Màn hình | File Screen Spec nguồn | Dạng trong HTML |
| --- | --- | --- | --- |
| 01 | Service Caterers Dashboard | `01_ServiceCaterersDashboard.md` | Screen `#root` (entry) |
| 02 | Menu Management Dashboard | `02_MenuManagementDashboard.md` | Screen `#menudash` |
| 03 | Create Menu Workspace | `03_CreateMenuWorkspace.md` | Screen `#create` (3 `.step-page` nội bộ) |
| 04 | View Menu Recommendations Panel | `04_ViewMenuRecommendationsPanel.md` | Modal bottom-sheet `#ai-modal` |
| 05 | Submit Menu For Approval Trigger | `05_SubmitMenuForApprovalTrigger.md` | Modal bottom-sheet `#submit-modal` |
| 06 | Update Menu Screen | `06_UpdateMenuScreen.md` | Screen `#update` (3 `.step-page` nội bộ) |

---

## 2. Đối chiếu với Screen Specification

### 01. Service Caterers Dashboard
- [x] Đủ field: lời chào + tên người dùng, widget thống kê (3 số liệu mẫu), Grid Menu Items 8 mục (chỉ "Thực đơn" clickable trong nhóm này, 7 mục còn lại `disabled` kèm `title` trỏ đúng tên file wireframe nhóm khác đã/sẽ build), BottomNavigationBar 4 mục — đúng 4 dòng Business Logic.
- [x] Grid luôn hiển thị đủ 8 mục (không dựng Empty theo đúng ghi chú Screen Spec).

### 02. Menu Management Dashboard
- [x] Đủ field: card list 3 mẫu (Draft/Rejected/Published) đủ 3 trạng thái, action Edit/Submit/Delete theo đúng điều kiện trạng thái (BR-067 — Published không có action), FAB "Tạo Thực đơn Mới" — đúng 8 dòng Business Logic.

### 03. Create Menu Workspace
- [x] Đủ field: 3 trang tuần tự (DatePicker+MealSlot; Chọn món+AI Assist+dinh dưỡng; Khai báo dị ứng+banner MSG-051), nút Tiếp theo/Quay lại cố định, nút "Lưu nháp" ở cuối trang 3 — đúng 8 dòng Business Logic. Điều hướng dùng `stepBack`/`stepNextOrSave` nội bộ (không tạo screen riêng cho mỗi trang, đúng ghi chú "Form nhiều trang riêng" nhưng gộp trong 1 `<div class="screen">` để đơn giản hoá điều hướng — nội dung/field không đổi).

### 04. View Menu Recommendations Panel
- [x] Đủ field: tóm tắt ngữ cảnh AI (tồn kho/dinh dưỡng/dị ứng/trần chi phí BR-080), 2 Option Card (1 an toàn + 1 có cờ dị ứng để minh hoạ đủ trạng thái), nút "Áp dụng", ghi chú lưu trữ hội thoại (BR-157) — đúng 6 dòng Business Logic. Kế thừa nguyên văn cảnh báo mâu thuẫn nguồn BR-072→BR-080 đã có trong Screen Spec 04.

### 05. Submit Menu For Approval Trigger
- [x] Đủ field: tóm tắt thực đơn, checklist 2 mục (BR-069/BR-061, đã đánh dấu đạt), banner MSG-051 chặn, nút "Xác nhận Nộp Phê duyệt" ở trạng thái **disabled** để minh hoạ đúng case chặn (BR-063) — đúng 4 dòng Business Logic.

### 06. Update Menu Screen
- [x] Đủ field: banner bình luận từ chối ở đầu (vì entry giả định status=Rejected), 3 trang tương tự Create (pre-filled), banner MSG-075 (giới hạn lặp món), nút AI Assist, banner MSG-051, nút "Lưu thay đổi" — đúng 8 dòng Business Logic.
- Kế thừa nguyên văn cảnh báo mâu thuẫn nguồn của Screen Spec 06 (BR-072/074 không áp dụng; MSG-075 chưa có mã BR chính thức).

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md
- Single-file SPA vanilla HTML/JS, khung `.phone-frame` (390px), 4 `<div class="screen">` + 2 modal bottom-sheet.
- Stepper nội bộ dùng `.step-page`/`.step-dot` + hàm `stepBack/stepNextOrSave` (dùng chung cho cả `create` và `update` qua biến `stepState`).
- Icon Grid Menu Items 7/8 mục ngoài "Thực đơn" dùng `stroke="var(--midgray)"` + `title` ghi rõ tên file wireframe tương ứng (đã build: Receiving/ThreeStepInspection/FoodSampleRetention; chưa build: Meal Service/Corrective Actions/Violations/Kho).

---

## 4. Cảnh báo nguồn Draft
Toàn bộ 6 Screen Specification nguồn đang **Draft**, chưa qua QA của Nguyễn Phương Thảo.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin (báo lại Thảo — QA)

1. **01_ServiceCaterersDashboard.md — widget thống kê không có mô tả field-by-field gốc**: đã ghi nhận sẵn trong chính Screen Spec ("[Giả định — cần team xác nhận]") — nhắc lại để gộp vào bảng TODO chung.
2. **06_UpdateMenuScreen.md — field #2 "Bình luận từ chối" chưa có định dạng hiển thị cụ thể**: đã ghi nhận sẵn trong chính Screen Spec ("[Giả định — cần team xác nhận]") — wireframe tạm dùng 1 dòng text trích dẫn trực tiếp, cần Thảo xác nhận định dạng chuẩn (có kèm tên người từ chối/thời điểm hay không).
3. **03_CreateMenuWorkspace.md và 06_UpdateMenuScreen.md — không nêu rõ nút "Lưu nháp"/"Lưu thay đổi" có xuất hiện ở cả 3 trang hay chỉ trang cuối**: Business Logic ghi nút này là "FilledButton (cố định cuối màn)" nhưng không rõ "cuối màn" là cuối mỗi trang hay chỉ cuối trang cuối cùng của form nhiều trang. Wireframe giả định **chỉ xuất hiện ở trang cuối** (trang 3) — cần Thảo xác nhận.
