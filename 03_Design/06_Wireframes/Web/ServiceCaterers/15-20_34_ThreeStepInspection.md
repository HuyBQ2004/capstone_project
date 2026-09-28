# Wireframe — Three-Step Inspection + Preparation/Inspection Logs (nhóm màn hình Service Caterers, Web)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/ServiceCaterers/15,16,17,18,19,20,34_*.md` — 7 file Screen Specification (Status: Draft — xem cảnh báo mục 4).
**Công cụ sinh**: Thủ công — skill `wireframer` (agilek/wireframer-skill), layout Sidebar + Topbar giống mẫu `Web/SchoolAdministration/`.
**File/Link**: `03_Design/06_Wireframes/Web/ServiceCaterers/15-20_34_ThreeStepInspection.html`
**Nguồn điều hướng**: `03_Design/04_ScreenFlow/09_Service_Caterers_Screen_Flow_Web.md` — nhánh `Dash → InspDash → Step1/2/3 → PrepLedger/InspLog`. Khác biệt quan trọng so với Mobile: **Step 1 (16), Step 2 (17), Step 3 (18) không phải màn điều hướng riêng trên Web** — Platform Layout — Web của cả 3 Screen Spec đều ghi "Form 1 trang trong Tabs/Stepper của Inspection Dashboard", nên được bundled thành 3 tab trong 1 `<div class="screen" id="session">` riêng (mở từ nút "Tiếp tục Bước..." trên Dashboard), giống cách bundling đã áp dụng cho nhóm Receiving Workspace.

---

## 1. Danh sách 7 Screen Spec → 4 screen + 1 modal trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Dạng trong HTML |
| --- | --- | --- | --- |
| 15 | Three-Step Inspection Dashboard | `15_ThreeStepInspectionDashboard.md` | Screen `#dashboard` (entry) |
| 16 | Step 1 — Inspect Ingredients Before Processing | `16_InspectIngredientsBeforeProcessingScreen.md` | Tab "Bước 1" trong Screen `#session` |
| 17 | Step 2 — Upload Image Cooked Food | `17_UploadImageCookedFoodScreen.md` | Tab "Bước 2" trong Screen `#session` |
| 18 | Step 3 — Inspect Meals Before Serving | `18_InspectMealsBeforeServingScreen.md` | Tab "Bước 3" trong Screen `#session` |
| 19 | Record Preparation Information Form | `19_RecordPreparationInformationForm.md` | Modal `#recordprep-modal` (đúng Platform Layout — Web: "Form ngắn trong Dialog") |
| 20 | View Preparation Records Ledger | `20_ViewPreparationRecordsLedger.md` | Screen `#ledger` |
| 34 | View Inspection Records Log | `34_ViewInspectionRecordsLog.md` | Screen `#insplog` |

Tổng cộng: **4 `<div class="screen">`** (dashboard/session/ledger/insplog) + **3 tab-panel** trong session + **1 modal**.

---

## 2. Đối chiếu với Screen Specification

### 15. Three-Step Inspection Dashboard
- [x] Đủ field: Table phiên sản xuất (Mẻ/Bước hiện tại/Trạng thái/Co-sign/Hành động — 2 dòng mẫu, 1 Đang thực hiện + 1 Failed), banner cảnh báo Failed + link Corrective Action, nút "Ghi chép Sơ chế mới" mở Modal 19 — đúng 5 dòng Business Logic.

### 16/17/18. Inspection Session (Stepper Bước 1/2/3)
- [x] Bước 1: quét/nhập mã vạch, form nguyên liệu tươi/đông lạnh, checklist cảm quan, cảnh báo FEFO (Failed — đúng AI Generation Prompt), upload ảnh — đúng field Business Logic của 16.
- [x] Bước 2: time picker, upload ảnh món ăn, nhiệt độ lõi (68°C — Failed, đúng AI Generation Prompt "cảnh báo nhiệt độ chưa đạt 75°C"), checklist vệ sinh — đúng field Business Logic của 17.
- [x] Bước 3: số suất ăn, time picker, dụng cụ chứa, nhiệt độ giữ ấm (55°C — Failed, đúng AI Generation Prompt), cảm quan, upload ảnh, chữ ký số kép (1 đã ký/1 chưa) — đúng field Business Logic của 18.
- [x] Đúng ghi chú Web "upload ảnh thay chụp trực tiếp, secondary path" cho cả 3 bước.

### 19. Record Preparation Information Form (Modal)
- [x] Đủ field: phiên đang hoạt động (read-only), checklist hành động sơ chế, input nồng độ khử trùng (điều kiện), select nhân viên, nút Lưu — đúng 5 dòng Business Logic, bố cục Dialog đúng Platform Layout — Web.

### 20. View Preparation Records Ledger
- [x] Đủ field: Table (Mẻ/Món/Nhân viên/Ngày/Trạng thái ký/Xem chi tiết), filter bar — đúng 5 dòng Business Logic.

### 34. View Inspection Records Log
- [x] Đủ field: Table (Ngày/Món ăn/Trạng thái badge Passed-Failed/Hành động), filter bar, link Corrective Action nếu Failed (ghi chú text, ngoài phạm vi nhóm) — đúng 4 dòng Business Logic.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md
- Single-file SPA vanilla HTML/JS; Sidebar chỉ nhóm "Kiểm tra 3 Bước" active/click được, còn lại `.side-link.disabled`.
- `showScreen(id)` cho 4 screen chính, `showTab(id)` cho 3 tab trong session, `openModal/closeModal` cho Modal 19.
- Khối nhiệt độ dùng `.temp-box`/`.temp-value` (44px) + viền 3px khi Failed.
- Chữ ký số minh hoạ bằng `.sig-pad` khung viền đứt nét placeholder.

---

## 4. Cảnh báo nguồn Draft
Toàn bộ 7 Screen Specification nguồn đang **Draft**, chưa qua QA của Nguyễn Phương Thảo. 17 và 18 tự ghi nhận "khoảng trống nguồn" (UC-063/UC-065 không có bảng đặc tả UC riêng) — kế thừa nguyên trạng.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin (báo lại Thảo — QA)

1. **Kế thừa từ bản Mobile** (`Mobile/ServiceCaterers/15-20_34_ThreeStepInspection.md` mục 5): (a) field #2/#3 của 16 không rõ điều kiện hiển thị tươi/đông lạnh vs khô/đóng gói; (b) "Xem chi tiết" ở 20/34 không có Screen Spec Detail riêng; (c) liên kết chưa rõ giữa "Inspect Quality" (nhóm Receiving) và Three-Step Inspection.
2. **15_ThreeStepInspectionDashboard.md (Web) — không nêu rõ nút "Ghi chép Sơ chế mới" có thuộc Business Logic của chính màn 15 hay không**: Business Logic của 15 chỉ có 5 field, không field nào đề cập tạo mới Record Preparation Information — nút này được suy ra từ ScreenFlow (`CookInfo → InspDash`, chiều ngược lại ngầm định phải có điểm vào). Wireframe đặt nút này ở Dashboard để đảm bảo màn 19 có điểm vào trên Web (không có Sidebar riêng cho 19) — cần Thảo xác nhận vị trí entry point chính xác trên Web (có thể nên đặt ở màn khác như Menu/Prepare Ingredients nếu tồn tại).
