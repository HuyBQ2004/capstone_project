# Wireframe — Supplier Onboarding &amp; Partnership (nhóm màn hình Raw Food Supplier)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/RawFoodSupplier/` — 5 file Screen Specification liệt kê ở bảng mục 1 (Draft — xem cảnh báo mục 4).
**Công cụ sinh**: Thủ công — theo skill `wireframer` (agilek/wireframer-skill), phong cách hand-drawn Balsamiq, vanilla HTML/JS single-file SPA (không React/router), quy tắc kỹ thuật tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Web/RawFoodSupplier/01_SupplierOnboardingPartnership.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md` (Dashboard → Partnership Detail → Supplier Partnership Application Form → Verification Documents Upload; Partnership Detail → Partnership Approval History). Sidebar tham chiếu Sidebar 2 cấp của chính file ScreenFlow này + `00_Foundation/Navigation_Guidelines.md` mục 1.

---

## 1. Danh sách 5 màn hình trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 01 | Supplier Dashboard | `01_SupplierDashboard.md` | Node gốc điều hướng |
| 02 | Select School Workspace | `02_SelectSchoolWorkspace.md` | **[Giả định]** — toàn màn không có UC/BR nguồn, xem cảnh báo trong chính Screen Spec |
| 03 | School Partnerships | `03_SchoolPartnerships.md` | Tương ứng "Partnership Detail" trong ScreenFlow |
| 07 | Partnership Application | `07_PartnershipApplication.md` | UC-152 |
| 08 | Application Status | `08_ApplicationStatus.md` | UC-169/UC-170/UC-155 |

Tổng cộng: **5 `<div class="screen">`**, không có modal overlay riêng (không có hành động chặn kiểu Approve/Reject ở nhóm này).

---

## 2. Đối chiếu với Screen Specification — theo từng màn hình

### 01. Supplier Dashboard
- [x] Đủ field: Breadcrumb, Log Out icon, Partner Welcome Banner, Section Header, 4 card điều hướng (Purchase Request/Delivery Management/Ingredient Batches/Food Safety Compliance Record), card School Partnerships, notification bell — đúng 10 dòng Business Logic.
- [x] Đúng Design System *(ghi chú: bản wireframe hand-drawn/grayscale theo skill wireframer, CHƯA áp Design System thật — badge success/warning phân biệt bằng kiểu viền + icon doodle thay vì màu)*.
- [x] Đủ trạng thái tối thiểu có ghi chú: **Success** là trạng thái chính render đầy đủ; **Loading** ghi chú text "skeleton từng card, tải song song"; **Error** ghi chú MSG-004/MSG-005, không dựng hình riêng; **Empty** (chưa có partnership Approved) không dựng riêng trong file này (đã dựng đủ ở nhánh Select School Workspace/School Partnerships).

### 02. Select School Workspace
- [x] Đủ field: Screen Title, danh sách trường Approved (Card + nút Chọn), nút "Nộp đơn hợp tác trường mới", workspace chip — đúng 5 dòng Business Logic. Giữ nguyên cảnh báo [Giả định] ngay trên đầu nội dung màn hình trong HTML.
- [x] Đúng Design System *(badge success viền liền + icon tick)*.
- [x] Đủ trạng thái: **Success** hiển thị 2 trường mẫu; **Empty** dựng đầy đủ (card empty-state với icon trường + text gợi ý nộp đơn); **Loading/Error**: ghi chú MSG-004/MSG-005.

### 03. School Partnerships
- [x] Đủ field: Back, Add/Plus icon, School Partner Card (School Name, Delivery address), badge trạng thái hợp tác, badge trạng thái tài liệu, nút "Xem lịch sử phê duyệt" — đúng 9 dòng Business Logic.
- [x] Đúng Design System *(badge Approved = viền liền + tick; badge Expiring Soon = viền đứt nét + tam giác cảnh báo)*.
- [x] Đủ trạng thái: **Success** là trạng thái chính; **Empty** (chưa có partnership) không dựng riêng ở đây (đã có ở màn 02); **Loading/Error**: MSG-004/MSG-005.

### 07. Partnership Application
- [x] Đủ field: Company Name, Tax ID, Business Address, Business License upload, Food Safety Certificate upload (multi) + ngày hết hạn, nút Submit/Cancel — đúng 9 dòng Business Logic.
- [x] Đúng Design System *(file upload dùng Option B `[Image Placeholder]` theo SKILL.md mục 5, DatePicker dùng `<wired-calendar>`)*.
- [x] Đủ trạng thái: **Error/blocking case** dựng đầy đủ — alert-danger MSG-013 (chứng nhận hết hạn, chặn theo BR-048); **Success**: điều hướng thẳng Application Status; **Loading/Empty**: không áp dụng (form nhập mới).

### 08. Application Status
- [x] Đủ field: Badge trạng thái đơn, bình luận/lý do từ School Administration, nút Edit/Update Application (điều kiện), ngày nộp/cập nhật, bảng lịch sử phê duyệt — đúng 6 dòng Business Logic.
- [x] Đúng Design System *(badge Action Required = viền đứt nét + tam giác; alert-info cho bình luận)*.
- [x] Đủ trạng thái: **Warning/Action Required** là trọng tâm được dựng đầy đủ (kèm nút Edit/Update Application); **Success** (Approved) không dựng riêng thêm biến thể trong file này để tránh trùng lặp — đã ghi rõ trong bảng lịch sử; **Empty** (chưa từng nộp đơn): không dựng, tham chiếu ngược Screen Spec.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- Single-file SPA vanilla HTML/JS, 5 `<div class="screen" id="...">`, điều hướng bằng hàm `showScreen(id)` thuần JS (không router).
- Grayscale/mono tuyệt đối; `wired-elements` nhúng qua CDN; font `Patrick Hand`; nền graph-paper; sketchy border trick áp dụng cho toàn bộ card/table-wrap/button.
- Nút dùng `<button class="btn-primary/btn-secondary/btn-danger">` (không dùng `<wired-button>`).
- Toàn bộ icon là SVG line-art doodle inline tự vẽ (không emoji).
- Badge success/warning/danger/info phân biệt bằng kiểu viền (liền/đứt nét/đậm 3px/chấm) + icon doodle riêng, không dùng màu.
- Images & Placeholders: chỉ dùng Option B — khối `[Image Placeholder]` cho Business License/Food Safety Certificate.
- Sidebar liệt kê đủ 6 nhóm chức năng RFS (Tổng quan/Hợp tác với Trường/Đơn Mua hàng/Giao hàng/Tuân thủ & Chứng nhận/Lô Nguyên liệu/Cá nhân); chỉ nhóm Tổng quan + Hợp tác với Trường click được trong file này, các nhóm khác là `.side-link.disabled` kèm ghi chú trỏ đúng file wireframe nhóm khác.

---

## 4. Cảnh báo nguồn Draft

**Nguồn Draft**: Toàn bộ Screen Specification nguồn (`05_ScreenSpecifications/Web/RawFoodSupplier/`) đang ở trạng thái Draft, CHƯA qua bước QA của Nguyễn Phương Thảo — Wireframe này bám sát nguyên văn Draft hiện có, cần rà lại nếu Screen Spec đổi sau QA.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin/mâu thuẫn (báo lại Thảo — QA)

1. **02_SelectSchoolWorkspace.md**: toàn màn hình được đánh dấu [Giả định] — không có UC/BR nào trong Report 3/business_rules.md xác nhận mô hình 1 Raw Food Supplier phục vụ nhiều trường cùng lúc. Cần Product Owner/BA xác nhận trước khi coi Wireframe này là chính thức.
2. **01_SupplierDashboard.md**: vị trí card "School Partnerships" trên Dashboard là suy luận từ ScreenFlow, không có trong bảng field gốc Report 3 §3.3.2.5.1 (chỉ có 4 nút). Đã giữ nguyên đánh dấu [Giả định] trong Wireframe.
3. **07_PartnershipApplication.md / traceability_matrix.md**: BR-050 (đăng ký nhân viên giao hàng) xuất hiện trong traceability_matrix.md dòng 99 cho UC-152 nhưng không liên quan nghiệp vụ tới việc nộp đơn hợp tác công ty — khả năng cao là sai sót trong traceability matrix, Wireframe không dùng BR-050 cho màn này (đã dùng bộ BR theo Normal Flow/Alternative Flow thật của UC-152).
