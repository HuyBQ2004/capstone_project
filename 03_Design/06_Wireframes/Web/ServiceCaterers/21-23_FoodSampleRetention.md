# Wireframe — Food Sample Retention (nhóm màn hình Service Caterers, Web)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/ServiceCaterers/21,22,23_*.md` — 3 file Screen Specification (Status: Draft — xem cảnh báo mục 4).
**Công cụ sinh**: Thủ công — skill `wireframer` (agilek/wireframer-skill), layout Sidebar + Topbar giống mẫu `Web/SchoolAdministration/`.
**File/Link**: `03_Design/06_Wireframes/Web/ServiceCaterers/21-23_FoodSampleRetention.html`
**Nguồn điều hướng**: `03_Design/04_ScreenFlow/09_Service_Caterers_Screen_Flow_Web.md` — nhánh `Dash → SampleDash(23) → SampleForm(21) → SampleDetail(22)` (khuyến nghị dùng Mobile tại bếp, Web phù hợp rà soát bàn giấy).

---

## 1. Danh sách 3 Screen Spec → 3 screen + 2 modal trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Dạng trong HTML |
| --- | --- | --- | --- |
| 23 | Food Sample Retention Dashboard | `23_FoodSampleRetentionDashboard.md` | Screen `#dashboard` (entry), Card grid theo tủ đúng gợi ý Platform Layout — Web |
| 21 | Record Sample Information Form | `21_RecordSampleInformationForm.md` | Screen `#form` |
| 22 | Food Sample Detail | `22_FoodSampleDetail.md` | Screen `#detail` (bố cục 2 cột đúng Platform Layout — Web) |

Modal `#temp-modal`/`#transfer-modal` thuộc field #2/#5 của chính Screen Spec 23 (không phải Screen Spec riêng), giống cách xử lý ở bản Mobile.

---

## 2. Đối chiếu với Screen Specification

### 23. Food Sample Retention Dashboard
- [x] Đủ field: Card grid 2 tủ (nhiệt độ, số mẫu, badge cảnh báo), nút "Ghi Nhiệt độ" (modal), Table mẫu con khi mở rộng (điều hướng Detail/Chuyển tủ), nút "Đăng ký Mẫu mới" — đúng 6 dòng Business Logic, bố cục Card grid + Table con đúng Platform Layout — Web.
- [x] Case lỗi nhiệt độ ngoài phạm vi (Tủ #2, 9°C) dựng trực tiếp làm trạng thái chính.

### 21. Record Sample Information Form
- [x] Đủ field: banner GPS, read-only bữa ăn/món/số suất, input trọng lượng (80g — Failed MSG-019), textarea hộp đựng, input nhiệt độ, time picker, khối xem trước + in nhãn, upload ảnh, chữ ký số, nút "Xác nhận Lưu mẫu" — đúng 10 dòng Business Logic, bố cục 2 cột.

### 22. Food Sample Detail
- [x] Đủ field: thông tin mẫu + badge trạng thái (cột trái, Table), ảnh + đồng hồ đếm ngược 72 giờ (cột phải), banner cảnh báo MSG-059 trên cùng — đúng 6 dòng Business Logic, bố cục 2 cột đúng Platform Layout — Web.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md
- Single-file SPA vanilla HTML/JS; Sidebar chỉ nhóm "Lưu mẫu Thực phẩm" active/click được.
- 2 Modal (`Ghi Nhiệt độ`, `Chuyển tủ`) dùng chung `.modal-overlay`/`.modal-box` như các nhóm trước.
- Đồng hồ đếm ngược `.countdown`/`.countdown-value` (44px).

---

## 4. Cảnh báo nguồn Draft
Toàn bộ 3 Screen Specification nguồn đang **Draft**, chưa qua QA của Nguyễn Phương Thảo. Wireframe bám nguyên văn Draft để kịp mốc SDS v1.4 (12/07/2026).

---

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin (báo lại Thảo — QA)

1. **Kế thừa từ bản Mobile** (`Mobile/ServiceCaterers/21-23_FoodSampleRetention.md` mục 5): field #2/#5 của 23_FoodSampleRetentionDashboard.md ("Ghi Nhiệt độ", "Chuyển tủ") không có Screen Spec riêng dù là form — cần Thảo xác nhận có cần tách Screen Spec riêng.
2. **21_RecordSampleInformationForm.md (Web) — mâu thuẫn mã MSG cho lỗi GPS**: đã ghi nhận sẵn trong chính Screen Spec ("[Giả định — cần team xác nhận]") — nhắc lại để gộp vào bảng TODO chung.
