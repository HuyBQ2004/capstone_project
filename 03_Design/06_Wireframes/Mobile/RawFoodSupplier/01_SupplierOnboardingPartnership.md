# Wireframe — Supplier Onboarding &amp; Partnership (nhóm màn hình Raw Food Supplier, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/RawFoodSupplier/` — 5 file Screen Specification (Draft — xem cảnh báo mục 4).
**Công cụ sinh**: Thủ công — theo skill `wireframer` (agilek/wireframer-skill), phong cách hand-drawn Balsamiq, vanilla HTML/JS single-file SPA, quy tắc kỹ thuật tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`. Bố cục mobile (AppBar + Content 1 cột + BottomNavigationBar) theo `00_Foundation/Layout_Guidelines.md` §2 — không có precedent Mobile trước đó trong dự án (2 actor Web-only chưa làm Mobile), nên khung phone-frame/AppBar/BottomNav ở đây là bản dựng nền tảng đầu tiên theo đúng SKILL.md + Layout_Guidelines, dùng nhất quán cho cả 5 nhóm Mobile của Raw Food Supplier.
**File/Link**: `03_Design/06_Wireframes/Mobile/RawFoodSupplier/01_SupplierOnboardingPartnership.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md` (Dashboard → Partnership Detail → Supplier Partnership Application Form → Verification Documents Upload; Partnership Detail → Partnership Approval History).

---

## 1. Danh sách 5 màn hình trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 01 | Supplier Dashboard | `01_SupplierDashboard.md` | Node gốc điều hướng, tab "Trang chủ" trên BottomNav |
| 02 | Select School Workspace | `02_SelectSchoolWorkspace.md` | **[Giả định]** — không có UC/BR nguồn |
| 03 | School Partnerships | `03_SchoolPartnerships.md` | Node "Partnership Detail" |
| 07 | Partnership Application | `07_PartnershipApplication.md` | UC-152 |
| 08 | Application Status | `08_ApplicationStatus.md` | UC-169/UC-170/UC-155 |

Tổng cộng: **5 `<div class="screen">`** trong khung phone-frame 400px, không có modal riêng.

---

## 2. Đối chiếu với Screen Specification — theo từng màn hình

### 01. Supplier Dashboard
- [x] Đủ field: Screen Title, Log Out icon, Partner Welcome Banner, Section Header, 4 card điều hướng, card School Partnerships, notification bell — đúng 10 dòng Business Logic.
- [x] Đúng Design System *(hand-drawn/grayscale theo skill wireframer; BottomNavigationBar 5 mục theo `Navigation_Guidelines.md` §2, icon kèm label chữ)*.
- [x] Đủ trạng thái: Loading/Error ghi chú tham chiếu (không dựng hình riêng do đã dựng đủ ở nhóm khác); Empty không áp dụng ở màn Dashboard.

### 02. Select School Workspace
- [x] Đủ field: Screen Title, danh sách trường Approved (Card + nút Chọn), nút nộp đơn mới — đúng 5 dòng Business Logic. [Giả định] giữ nguyên cảnh báo đầu màn.
- [x] Đúng Design System *(chip success viền liền)*.
- [x] Đủ trạng thái: Success dựng 2 trường mẫu; Empty không dựng lại ở Mobile (đã dựng ở bản Web, tránh trùng lặp không cần thiết theo yêu cầu tối giản nhóm Mobile).

### 03. School Partnerships
- [x] Đủ field: Back, Add/Plus, School Partner Card, chip trạng thái hợp tác, chip trạng thái tài liệu, nút xem lịch sử — đúng 9 dòng Business Logic.
- [x] Đúng Design System *(chip Expiring Soon viền đứt nét)*.
- [x] Đủ trạng thái: Success là trọng tâm; Empty/Loading/Error tham chiếu MSG-004/005.

### 07. Partnership Application
- [x] Đủ field: Company Name, Tax ID, Business Address, Business License upload, Food Safety Certificate upload + ngày hết hạn, nút Submit/Cancel — đúng 9 dòng Business Logic.
- [x] Đúng Design System *(Option B `[Image Placeholder]`, form 1 trang theo Layout_Guidelines §2.2 thay vì Stepper ngang như Web)*.
- [x] Đủ trạng thái: Error (MSG-013) dựng đầy đủ; Success điều hướng Application Status.

### 08. Application Status
- [x] Đủ field: chip trạng thái, bình luận, nút Edit/Update Application, ngày nộp/cập nhật, lịch sử phê duyệt — đúng 6 dòng Business Logic.
- [x] Đúng Design System *(chip Action Required viền đứt nét)*.
- [x] Đủ trạng thái: Warning/Action Required là trọng tâm; Success/Empty tham chiếu Screen Spec, không dựng thêm biến thể.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- Single-file SPA vanilla HTML/JS trong khung `.phone` (400px, mô phỏng thiết bị di động), 5 `<div class="screen">`, điều hướng bằng `showScreen(id)`.
- Grayscale/mono; `wired-elements` qua CDN; font `Patrick Hand`; nền graph-paper (đậm hơn nền ngoài `.phone` để tách biệt khung thiết bị); sketchy border trick cho card/button.
- AppBar (tiêu đề + icon back/action) thay Topbar/Sidebar của Web; BottomNavigationBar 5 mục (Trang chủ/Đơn hàng/Giao hàng/Lô NL/Thêm) theo `Navigation_Guidelines.md` §2 — icon kèm chữ, không icon-only.
- Không có FAB trong nhóm này (không có hành động "tạo mới" chính ở màn Dashboard/Partnership — FAB xuất hiện ở nhóm Batches/Deliveries).
- Toàn bộ icon SVG line-art doodle inline (không emoji, không react-doodle-icons vì vanilla HTML).
- Chip success/warning/danger/info phân biệt bằng kiểu viền, không dùng màu.

---

## 4. Cảnh báo nguồn Draft

Toàn bộ Screen Specification nguồn (`05_ScreenSpecifications/Mobile/RawFoodSupplier/`) đang Draft, chưa qua QA của Nguyễn Phương Thảo — Wireframe bám sát nguyên văn Draft hiện có.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin/mâu thuẫn

Giống hệt danh sách đã báo ở bản Web cùng nhóm (`Web/RawFoodSupplier/01_SupplierOnboardingPartnership.md` mục 5) — không phát sinh thêm vấn đề riêng cho Mobile trong nhóm này.
