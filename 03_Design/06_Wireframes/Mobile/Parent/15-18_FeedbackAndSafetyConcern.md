# Wireframe — Feedback & Safety Concern (nhóm màn hình Parent, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/Parent/` — 4 file Screen Specification (Confirmed).
**Công cụ sinh**: Thủ công — skill `wireframer`, quy tắc tại `03_Design/06_Wireframes/.agents/skills/wireframer/SKILL.md`.
**File/Link**: `03_Design/06_Wireframes/Mobile/Parent/15-18_FeedbackAndSafetyConcern.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/05_Parent_Screen_Flow.md` (Dashboard "Meal Feedback" tile → Feedback Center → 3 Card/Tile lớn → Submit Meal/Food Quality Feedback (full-screen) / Report Safety Concern; Report Safety Concern cũng vào trực tiếp từ Dashboard "Report Urgent Safety Incident").

---

## 1. Danh sách 4 màn hình

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 15 | Feedback Center | `15_FeedbackCenter.md` | AppBar + 3 Card/Tile lớn + History List + BottomNav |
| 16 | Submit Meal Feedback | `16_SubmitMealFeedback.md` | Full-screen form |
| 17 | Submit Food Quality Feedback | `17_SubmitFoodQualityFeedback.md` | Full-screen form |
| 18 | Report Food Safety Concern | `18_ReportFoodSafetyConcern.md` | AppBar đỏ, Send button nền đỏ cố định đáy |

Tổng cộng: **4 `<div class="screen">`**.

---

## 2. Đối chiếu với Screen Specification

### 15. Feedback Center
- [x] Đủ field: 3 Card/Tile lớn, My Feedback History List cuộn dọc — đúng Platform Layout Mobile.

### 16. Submit Meal Feedback
- [x] Đủ field: Dish Selection Dropdown, Rating Score, Quality Concern Description, Review Comments, Photo Attachment, Send Feedback Button cố định đáy — đúng Platform Layout Mobile.
- **Cập nhật (2026-07-01)**: field "Quality Concern" đổi từ `Checkbox/Tag group` sang `Input` (text tự do) theo yêu cầu team — phụ huynh tự mô tả vấn đề (VD "quá mặn", "nguội") thay vì chọn nhãn có sẵn. Đã đồng bộ ngược lại Screen Spec gốc (`05_ScreenSpecifications/Web+Mobile/Parent/16_SubmitMealFeedback.md`, field #3, BR-140) và wireframe Web tương ứng (`Web/Parent/15-18_FeedbackAndSafetyConcern.html`) — 2 nền tảng và Screen Spec nay đã khớp nhau, không còn là thay đổi cục bộ.

### 17. Submit Food Quality Feedback
- [x] Đủ field: Meal Session Selector, Rating Score, Quality Concern Tags, General Comments, Photo Attachment, Send Feedback Button cố định đáy.

### 18. Report Food Safety Concern
- [x] Đủ field: AppBar tiêu đề đỏ, Concern Category, Incident Details, Photo Attachment (viền đỏ, bắt buộc), Send Urgent Report Button (nền đỏ, full-width, cố định đáy) — đúng Platform Layout Mobile, bao gồm ghi chú "không có bước duyệt trung gian — BR-138".
- **Cập nhật (2026-07-27)**: gộp 3 field rời Symptoms Checkbox/Meal Date/Suspected Dish vào chung Incident Details (textarea tự do) — phụ huynh ghi trực tiếp ngày/bữa ăn nghi vấn, món ăn nghi vấn, triệu chứng của con vào ô mô tả. Lý do: kênh khẩn cấp (BR-138), giảm số ô bắt buộc để gửi báo cáo nhanh hơn. Đã đồng bộ Screen Spec Mobile + Web và code `parent/safety-concern/page.tsx`.

---

## 3. Ghi chú kỹ thuật

- Khung điện thoại 390px, full-screen cho Submit Feedback (khác modal của Web).
- Grayscale, wired-elements, Patrick Hand, sketchy border.
- Dữ liệu mẫu nhất quán bản Web.

## 4. Trạng thái nguồn

Nguồn Confirmed. Không phát hiện mâu thuẫn mới.
