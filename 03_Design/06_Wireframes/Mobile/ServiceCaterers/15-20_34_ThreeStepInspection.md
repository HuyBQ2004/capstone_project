# Wireframe — Three-Step Inspection + Preparation/Inspection Logs (nhóm màn hình Service Caterers, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/ServiceCaterers/15,16,17,18,19,20,34_*.md` — 7 file Screen Specification (Status: Draft — xem cảnh báo mục 4).
**Công cụ sinh**: Thủ công — skill `wireframer` (agilek/wireframer-skill), khung phone-frame 390px, vanilla HTML/JS single-file SPA.
**File/Link**: `03_Design/06_Wireframes/Mobile/ServiceCaterers/15-20_34_ThreeStepInspection.html`
**Nguồn điều hướng**: `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` — nhánh `PrepIngr → CookInfo(19) → InspDash(15) → Step1(16) → Step2(17) → Step3(18) → PrepLedger(20)/InspLog(34)`.

---

## 1. Danh sách 7 màn hình (7 `<div class="screen">`, không có modal)

| STT | Màn hình | File Screen Spec nguồn |
| --- | --- | --- |
| 19 | Record Preparation Information Form | `19_RecordPreparationInformationForm.md` — entry point (trước Dashboard) |
| 15 | Three-Step Inspection Dashboard | `15_ThreeStepInspectionDashboard.md` |
| 16 | Inspect Ingredients Before Processing (Step 1) | `16_InspectIngredientsBeforeProcessingScreen.md` |
| 17 | Upload Image Cooked Food (Step 2) | `17_UploadImageCookedFoodScreen.md` |
| 18 | Inspect Meals Before Serving (Step 3) | `18_InspectMealsBeforeServingScreen.md` |
| 20 | View Preparation Records Ledger | `20_ViewPreparationRecordsLedger.md` |
| 34 | View Inspection Records Log | `34_ViewInspectionRecordsLog.md` |

---

## 2. Đối chiếu với Screen Specification

### 19. Record Preparation Information Form
- [x] Đủ field: phiên đang hoạt động (read-only), checklist hành động sơ chế (Rửa/Khử trùng/Gọt/Cắt), input nồng độ khử trùng (điều kiện), select nhân viên, nút Lưu — đúng 5 dòng Business Logic. Đúng ghi chú nguồn "UC-066 không có BR trực tiếp" — không tự thêm cảnh báo BR nào cho màn này.

### 15. Three-Step Inspection Dashboard
- [x] Đủ field: card list phiên sản xuất (2 mẫu: 1 đang Bước 2, 1 Failed ở Bước 1), chip trạng thái từng bước, 2 badge co-sign (Catering Manager/School Representative), banner cảnh báo Failed + link Corrective Action, icon điều hướng Ledger/Inspection Log trên AppBar — đúng 5 dòng Business Logic.
- **Lưu ý phạm vi**: link "Xem Corrective Action" chỉ hiển thị dạng text ghi chú (không click được) vì Corrective Actions Dashboard (32/33) thuộc nhóm màn hình khác (`32-37_CorrectiveActionsViolations`, chưa build trong lượt này) — không tự bịa nội dung màn ngoài phạm vi nhóm.

### 16. Step 1 — Inspect Ingredients Before Processing
- [x] Đủ field: quét mã vạch, form nguyên liệu tươi/đông lạnh (giờ giao/khối lượng/NCC/chứng nhận kiểm dịch), checklist cảm quan, cảnh báo FEFO/hết hạn (dựng ở trạng thái **Failed** để minh hoạ đúng yêu cầu "cảnh báo hết hạn/FEFO" trong AI Generation Prompt), ảnh bằng chứng, GPS/timestamp, nút "Nộp Bước 1" — đúng 8 dòng Business Logic. Form nguyên liệu khô/đóng gói (field #3) không dựng đồng thời với field #2 vì dữ liệu mẫu chọn nhóm tươi/đông lạnh — ghi chú rõ đây là 1 trong 2 nhánh có thể xuất hiện tuỳ loại nguyên liệu.

### 17. Step 2 — Upload Image Cooked Food
- [x] Đủ field: tên món/meal slot (read-only), time picker sơ chế/nấu, checklist vệ sinh, nhiệt độ lõi (dựng **68°C — Failed**, đúng yêu cầu "cảnh báo nhiệt độ chưa đạt 75°C hiển thị màu danger" trong AI Generation Prompt), ảnh món ăn, GPS/timestamp, nút "Nộp Bước 2" — đúng 7 dòng Business Logic.
- Kế thừa nguyên văn cảnh báo "khoảng trống nguồn" đã có trong Screen Spec 17 (UC-063 không có bảng đặc tả UC riêng, suy ra từ Step 2 của UC-062).

### 18. Step 3 — Inspect Meals Before Serving
- [x] Đủ field: số suất ăn, time picker chia suất, checklist dụng cụ chứa, nhiệt độ giữ ấm (dựng **55°C — Failed**, đúng AI Generation Prompt), checklist cảm quan, ảnh bằng chứng, khối chữ ký số kép (1 đã ký/1 chưa ký để minh hoạ đủ yêu cầu "cần đủ 2 chữ ký"), nút "Nộp Bước 3 & Phê duyệt Phục vụ" — đúng 8 dòng Business Logic.
- Kế thừa nguyên văn cảnh báo "khoảng trống nguồn" đã có trong Screen Spec 18 (UC-065 không có bảng đặc tả UC riêng).

### 20. View Preparation Records Ledger
- [x] Đủ field: card list phiên hoàn tất (Mẻ/Món/Nhân viên/Ngày), link số lô truy xuất nguồn gốc, chip trạng thái đồng ký số, filter chip, "Xem chi tiết" (ghi chú text, không có Screen Spec riêng cho màn Detail) — đúng 5 dòng Business Logic.

### 34. View Inspection Records Log
- [x] Đủ field: card list bản ghi kiểm tra (Ngày/Món/trạng thái Passed/Failed), filter chip, "Xem chi tiết checklist/nhiệt độ/ảnh/chữ ký" (ghi chú text), link Corrective Action nếu Failed (ghi chú text, ngoài phạm vi nhóm) — đúng 4 dòng Business Logic.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md
- Single-file SPA vanilla HTML/JS, khung `.phone-frame` (390px), 7 `<div class="screen">`, điều hướng `showScreen(id)`.
- Grayscale/mono; `wired-elements` qua CDN; font Patrick Hand; sketchy border trick cho card/temp-box/alert/sig-pad.
- Khối nhiệt độ (`temp-box`) dùng số lớn (`.temp-value`, 44px) + viền 3px khi Failed, đúng yêu cầu "nổi bật, cảnh báo ngay khi nhập" của AI Generation Prompt (Step 2/3).
- Chữ ký số minh hoạ bằng `.sig-pad` (khung viền đứt nét placeholder) — không dựng canvas ký thật.
- Icon toàn bộ SVG line-art doodle inline, không emoji.

---

## 4. Cảnh báo nguồn Draft
Toàn bộ 7 Screen Specification nguồn đang **Draft**, chưa qua QA của Nguyễn Phương Thảo. Riêng 17 và 18 còn tự ghi nhận "khoảng trống nguồn" (UC-063/UC-065 không có bảng đặc tả UC riêng trong Report 3) — wireframe kế thừa nguyên trạng giả định đã có trong Screen Spec, không tự suy diễn thêm.

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin (báo lại Thảo — QA)

1. **16_InspectIngredientsBeforeProcessingScreen.md — field #2 và #3 không nêu rõ điều kiện hiển thị loại nào**: Business Logic có 2 dòng riêng cho "nguyên liệu tươi/đông lạnh" (field #2) và "nguyên liệu khô/đóng gói" (field #3) nhưng không nói rõ đây là 2 field hiển thị đồng thời hay chọn 1 trong 2 theo loại nguyên liệu đang quét mã vạch. Wireframe giả định **chỉ hiện 1 trong 2** (dựng field #2 làm ví dụ) — cần Thảo xác nhận logic hiển thị điều kiện.
2. **20_ViewPreparationRecordsLedger.md và 34_ViewInspectionRecordsLog.md — "Xem chi tiết" không có Screen Spec riêng cho màn Detail**: cả 2 Screen Spec đều ghi field "Xem chi tiết... (điều hướng chi tiết, read-only)" nhưng không có file Screen Spec nào mô tả nội dung màn Detail đó (khác với các nhóm khác luôn có file riêng cho Detail). Wireframe chỉ dựng dạng link ghi chú, không tự bịa nội dung màn Detail — cần Thảo xác nhận có cần bổ sung Screen Spec Detail riêng hay đây là điều hướng quay lại đúng màn Step 1/2/3 gốc (read-only mode).
3. **Kế thừa từ nhóm Receiving (đã báo ở `07-14_ReceivingAndEvidence.md` mục 5.1)**: bước "Inspect Quality" trong Workspace Nhận hàng vẫn chưa rõ có phải cùng nhóm UC-062 (Three-Step Inspection) hay là 1 UC riêng — nhắc lại tại đây vì nhóm Three-Step Inspection này có thể là câu trả lời (nếu đúng, cần đổi field #5 của `08_ReceiveIngredientsWorkspace.md` để trỏ thẳng sang Three-Step Inspection Dashboard thay vì để trống).
