# Wireframe — Feedback & Safety Concern (nhóm màn hình Parent, Web)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/Parent/` — 4 file Screen Specification (Confirmed).
**Công cụ sinh**: Thủ công — skill `wireframer`, quy tắc tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Web/Parent/15-18_FeedbackAndSafetyConcern.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/10_Parent_Screen_Flow_Web.md` (Sidebar "Phản hồi & Sự cố" → Feedback Center → 3 tile → Submit Meal Feedback/Submit Food Quality Feedback (modal)/Report Safety Issue (trang riêng, đặt cố định sidebar theo ghi chú "Khác biệt so với bản Mobile").

---

## 1. Danh sách 4 màn hình trong file HTML

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 15 | Feedback Center | `15_FeedbackCenter.md` | Node gốc (hub), 3-tile grid + Table lịch sử |
| 16 | Submit Meal Feedback | `16_SubmitMealFeedback.md` | Modal — gắn 1 món cụ thể |
| 17 | Submit Food Quality Feedback | `17_SubmitFoodQualityFeedback.md` | Modal — biến thể theo phiên ăn, không gắn món cụ thể |
| 18 | Report Food Safety Concern | `18_ReportFoodSafetyConcern.md` | Trang riêng (không phải Dialog), destructive toàn màn |

Tổng cộng: **2 `<div class="screen">`** (Feedback Center, Report Safety Concern) + **2 modal overlay** (Submit Meal Feedback, Submit Food Quality Feedback).

---

## 2. Đối chiếu với Screen Specification

### 15. Feedback Center
- [x] Đủ field: Page Header, 3 tile (Recent Meal Feedback/Food Quality Feedback/Report Safety Concern — destructive), My Feedback History Table — đúng 5 dòng.
- Ghi chú: giữ nguyên `[Giả định]` cho My Feedback History Table (đã liệt kê `00_TODO_GiaDinh_Master.md` Parent #7).

### 16. Submit Meal Feedback
- [x] Đủ field: Dish Selection Select (chỉ món 24h), Rating Score (star 1-5), Quality Concern Description (điều kiện <3 sao), Review Comments, Photo Attachment, Send Feedback Button — đúng 6 dòng.
- **Cập nhật (2026-07-01)**: field #3 đổi từ `Checkbox group` sang `Input` (text tự do) theo yêu cầu team — phụ huynh tự mô tả vấn đề thay vì chọn nhãn có sẵn ("Quá mặn"/"Nguội"). Đã đồng bộ Screen Spec gốc (`05_ScreenSpecifications/Web+Mobile/Parent/16_SubmitMealFeedback.md`) và wireframe Mobile tương ứng — không còn lệch giữa 2 nền tảng.

### 17. Submit Food Quality Feedback
- [x] Đủ field: Meal Session Selector (thay Dish Selection — không gắn món cụ thể, đúng ghi chú nguồn), Rating Score, Quality Concern Tags, General Comments (thay Review Comments), Photo Attachment, Send Feedback Button — đúng 6 dòng.

### 18. Report Food Safety Concern
- [x] Đủ field: Page Header (destructive), Concern Category Select, Incident Details Section, Photo Attachment Component (bắt buộc), Send Urgent Report Button (destructive) — đúng 5 dòng.
- [x] Toàn màn hình nhấn mạnh màu destructive (viền đậm 3px xuyên suốt), đúng Platform Layout.
- [x] Sidebar "Report Safety Issue" đặt cố định, nổi bật (viền đậm), đúng ghi chú "không chỉ ẩn trong menu".
- **Cập nhật (2026-07-27)**: gộp 3 field rời Symptoms Checkbox Group/Meal Date Input/Suspected Dish Input vào chung Incident Details Section (textarea tự do) — phụ huynh ghi trực tiếp ngày/bữa ăn nghi vấn, món ăn nghi vấn, triệu chứng của con vào ô mô tả, thay vì 3 ô chọn riêng. Lý do: đây là kênh khẩn cấp (BR-138 gửi ngay không qua duyệt), giảm số ô bắt buộc điền giúp phụ huynh gửi báo cáo nhanh hơn khi cần. Đã đồng bộ Screen Spec Web + Mobile (`05_ScreenSpecifications/Web+Mobile/Parent/18_ReportFoodSafetyConcern.md`) và code `parent/safety-concern/page.tsx` — không còn lệch giữa spec/wireframe/code.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- Single-file SPA, 2 `<div class="screen">` + 2 modal, điều hướng `showScreen(id)`.
- Grayscale/mono, wired-elements (`wired-select`, `wired-checkbox`, `wired-textarea`, `wired-calendar`), Patrick Hand, sketchy border.
- Rating Score dựng bằng ký tự ★ (line-art tối giản) thay vì icon component riêng, giữ đúng tinh thần hand-drawn.
- Sidebar chỉ nhóm "Phản hồi & Sự cố" active-able.
- Dữ liệu mẫu nhất quán (canh bí đỏ, phiên ăn hôm nay).

## 4. Trạng thái nguồn

Nguồn Confirmed. Không phát hiện mâu thuẫn mới ngoài `[Giả định]` đã ghi ở `00_TODO_GiaDinh_Master.md` Parent #7.
