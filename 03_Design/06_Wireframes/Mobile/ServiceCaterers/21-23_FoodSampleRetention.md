# Wireframe — Food Sample Retention (nhóm màn hình Service Caterers, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/ServiceCaterers/21,22,23_*.md` — 3 file Screen Specification (Status: Draft — xem cảnh báo mục 4).
**Công cụ sinh**: Thủ công — skill `wireframer` (agilek/wireframer-skill), khung phone-frame 390px, vanilla HTML/JS single-file SPA.
**File/Link**: `03_Design/06_Wireframes/Mobile/ServiceCaterers/21-23_FoodSampleRetention.html`
**Nguồn điều hướng**: `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` — nhánh `Grid → SampleDash(23) → SampleForm(21) → SampleDetail(22)`.

---

## 1. Danh sách 3 màn hình (3 `<div class="screen">` + 2 modal con thuộc field của màn 23)

| STT | Màn hình | File Screen Spec nguồn |
| --- | --- | --- |
| 23 | Food Sample Retention Dashboard | `23_FoodSampleRetentionDashboard.md` (entry) |
| 21 | Record Sample Information Form | `21_RecordSampleInformationForm.md` |
| 22 | Food Sample Detail | `22_FoodSampleDetail.md` |

Modal `#temp-modal` ("Ghi Nhiệt độ") và `#transfer-modal` ("Chuyển tủ") **không phải Screen Spec riêng** — cả 2 là field #2 và #5 ngay trong Business Logic của chính Screen Spec 23 ("Button + form"/"Button... form nhập nhiệt độ"), dựng dạng bottom-sheet để không phải tự tạo Screen Spec mới ngoài phạm vi.

---

## 2. Đối chiếu với Screen Specification

### 23. Food Sample Retention Dashboard
- [x] Đủ field: card list 2 tủ mẫu (nhiệt độ hiện tại, số mẫu), nút "Ghi Nhiệt độ" (modal), banner cảnh báo MSG-020/MSG-059 khi tủ #2 lệch 9°C >30 phút, danh sách mẫu con mở rộng (điều hướng Detail), nút/form "Chuyển tủ" (modal, yêu cầu lý do), FAB "Đăng ký Mẫu mới" — đúng 6 dòng Business Logic.
- [x] Case lỗi (nhiệt độ ngoài phạm vi) dựng trực tiếp làm trạng thái chính của Tủ #2, đúng yêu cầu AI Generation Prompt.

### 21. Record Sample Information Form
- [x] Đủ field: banner xác minh GPS (BR-109), read-only bữa ăn/món/số suất, input trọng lượng (dựng **80g — Failed MSG-019** đúng AI Generation Prompt), textarea hộp đựng, input nhiệt độ tủ, time picker thu mẫu, khối in nhãn (xem trước nội dung nhãn), camera chụp ảnh hộp mẫu, chữ ký số, nút "Xác nhận Lưu mẫu" — đúng 10 dòng Business Logic.
- Kế thừa nguyên văn cảnh báo mâu thuẫn nguồn đã có trong Screen Spec 21 (MSG-072 sai ngữ cảnh trong Report 3 AF2, dùng MSG-020 thay thế — "[Giả định — cần team xác nhận]").

### 22. Food Sample Detail
- [x] Đủ field: ảnh nhãn/hộp mẫu (Option B placeholder), đồng hồ đếm ngược 72 giờ nổi bật, thông tin mẫu dạng list (tên món/ngày SX/ngày lưu/số lô/trọng lượng/nơi lưu), chip trạng thái (dựng Retained), banner cảnh báo tủ mở bất thường (MSG-059) — đúng 6 dòng Business Logic.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md
- Single-file SPA vanilla HTML/JS, khung `.phone-frame` (390px), 3 `<div class="screen">` + 2 modal bottom-sheet (`#temp-modal`, `#transfer-modal`) gắn vào Dashboard.
- Đồng hồ đếm ngược dùng `.countdown`/`.countdown-value` (35px) nổi bật đúng yêu cầu AI Generation Prompt.
- Khối in nhãn dùng `.label-preview` (viền chấm) mô phỏng bản xem trước trước khi in — không dựng máy in thật.

---

## 4. Cảnh báo nguồn Draft
Toàn bộ 3 Screen Specification nguồn đang **Draft**, chưa qua QA của Nguyễn Phương Thảo. Wireframe bám nguyên văn Draft để kịp mốc SDS v1.4 (12/07/2026).

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin (báo lại Thảo — QA)

1. **23_FoodSampleRetentionDashboard.md — field #2 "Ghi Nhiệt độ" và field #5 "Chuyển tủ" không có Screen Spec riêng dù mô tả là "form"**: cả 2 field ghi rõ là hành động mở ra 1 form (không chỉ là nút đơn giản), nhưng không có file Screen Spec riêng mô tả layout/field đầy đủ của 2 form này (khác các trường hợp khác trong dự án luôn có Screen Spec 1-1 cho mỗi form). Wireframe tự suy ra nội dung form tối thiểu từ đúng câu chữ Validation/Ràng buộc đã ghi trong bảng Business Logic của chính field #2/#5 (ca Sáng/Chiều + nhiệt độ; tủ đích + lý do) — không thêm field ngoài câu chữ đã có, nhưng cần Thảo xác nhận có cần tách thành 2 Screen Spec riêng (VD "24a_LogTemperatureForm", "24b_TransferSampleForm") để chuẩn hoá.
2. **21_RecordSampleInformationForm.md — chưa xác nhận lại mã MSG đúng cho lỗi GPS**: đã ghi nhận sẵn trong chính Screen Spec ("[Giả định — cần team xác nhận]") — nhắc lại tại đây để gộp vào bảng TODO chung.
