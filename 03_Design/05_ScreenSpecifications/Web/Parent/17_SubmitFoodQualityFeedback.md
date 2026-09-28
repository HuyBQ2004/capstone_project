# Screen Spec — Submit Food Quality Feedback

**Actor**: Parent | **UC liên quan**: UC-085 - Submit Meal & Food Quality Feedback | **Status**: Draft
**BR liên quan**: BR-139, BR-140, BR-142, BR-143
**MSG liên quan**: MSG-004, MSG-005, MSG-022, MSG-046, MSG-058

> Biến thể không gắn 1 món cụ thể — xem ghi chú nguồn ở bản Mobile cùng tên.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Meal Session Selector | `Select` | Chọn phiên ăn trong 24h | BR-139 | |
| 2 | Rating Score Component | Star rating | Bắt buộc | — | |
| 3 | Quality Concern Tags | Checkbox group (điều kiện) | Bắt buộc nếu <3 sao | BR-140 | |
| 4 | General Comments Section | `Textarea` | Tuỳ chọn | BR-142 | |
| 5 | Photo Attachment Component | `Input type="file"` | Tuỳ chọn | — | |
| 6 | Send Feedback Button | Button (primary) | Validate | BR-139 | MSG-022/046/058 |

### Trạng thái màn hình
- Loading: Spinner.
- Error: `Alert variant="destructive"` + MSG-004/005/022.
- Success: `Toast` `success` (MSG-046); `warning` nếu MSG-058.
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Phản hồi & Sự cố", từ Feedback Center.
**Bố cục**: `Dialog`/`Sheet` ngắn — Meal Session Selector, Rating, Tags, Comments, Photo, nút Send Feedback.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Food Quality Feedback, nền tảng Web, actor Parent.
Bố cục: Dialog/Sheet với Meal Session Selector, Rating Score, Quality Concern Tags, General Comments, Photo Attachment, nút Send Feedback.
Thành phần bắt buộc: Meal Session Selector, Rating Score Component, Quality Concern Tags, General Comments Section, Photo Attachment Component, Send Feedback Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005/022), success (MSG-046), warning (MSG-058).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4109–4129 (UC-085).
- `03_Design/04_ScreenFlow/10_Parent_Screen_Flow_Web.md`.
- `business_rules.md` BR-139, BR-140, BR-142, BR-143; `error_message.md` MSG-022, MSG-046, MSG-058.
