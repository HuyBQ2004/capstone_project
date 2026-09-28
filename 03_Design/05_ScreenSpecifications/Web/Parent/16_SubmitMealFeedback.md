# Screen Spec — Submit Meal Feedback

**Actor**: Parent | **UC liên quan**: UC-085 - Submit Meal & Food Quality Feedback | **Status**: Draft
**BR liên quan**: BR-139, BR-140, BR-142, BR-143, BR-145
**MSG liên quan**: MSG-004, MSG-005, MSG-022, MSG-046, MSG-058

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Dish Selection Select | `Select` | Bắt buộc, chỉ món trong 24h | BR-139 | |
| 2 | Rating Score Component | Star rating (1-5) | Bắt buộc | — | |
| 3 | Quality Concern Description | `Input` (text tự do, điều kiện <3 sao) | Bắt buộc nếu <3 sao | BR-140 | Đổi từ Checkbox group sang Input theo yêu cầu team (2026-07-01) — phụ huynh tự mô tả vấn đề (VD "quá mặn", "nguội") thay vì chọn nhãn có sẵn |
| 4 | Review Comments Section | `Textarea` | Tuỳ chọn | BR-142 | |
| 5 | Photo Attachment Component | `Input type="file"` | Tuỳ chọn | — | |
| 6 | Send Feedback Button | Button (primary) | Validate, chặn ngoài 24h | BR-139 | MSG-022/046/058 |

### Trạng thái màn hình
- Loading: Spinner.
- Error: `Alert variant="destructive"` + MSG-004/005/022.
- Success: `Toast` `success` (MSG-046); `Toast` `warning` nếu MSG-058.
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Phản hồi & Sự cố", từ Feedback Center/Nutrition Detail.
**Bố cục**: `Dialog`/`Sheet` ngắn — Dish Select, Rating, Tags, Comments, Photo, nút Send Feedback.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Meal Feedback, nền tảng Web, actor Parent.
Bố cục: Dialog/Sheet với Dish Selection, Rating Score, Quality Concern Description (input, điều kiện), Review Comments, Photo Attachment, nút Send Feedback.
Thành phần bắt buộc: Dish Selection Select, Rating Score Component, Quality Concern Description Input, Review Comments Section, Photo Attachment Component, Send Feedback Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005/022), success (MSG-046), warning (MSG-058).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6123–6140, dòng 4109–4129 (UC-085).
- `business_rules.md` BR-139, BR-140, BR-142, BR-143, BR-145; `error_message.md` MSG-022, MSG-046, MSG-058.
