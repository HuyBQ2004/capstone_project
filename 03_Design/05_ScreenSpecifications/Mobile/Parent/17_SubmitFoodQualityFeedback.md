# Screen Spec — Submit Food Quality Feedback

**Actor**: Parent | **UC liên quan**: UC-085 - Submit Meal & Food Quality Feedback | **Status**: Draft
**BR liên quan**: BR-139, BR-140, BR-142, BR-143
**MSG liên quan**: MSG-004, MSG-005, MSG-022, MSG-046, MSG-058

> **Ghi chú nguồn**: không có wireframe riêng — theo `14_Screen_Authorization_Matrix.md`, đây là 1 màn tách biệt với "Submit Meal Feedback" (cùng UC-085 nhưng phạm vi khác: phản hồi chất lượng chung của bữa ăn/dịch vụ catering, không gắn với 1 món cụ thể như `16_SubmitMealFeedback.md`). Field suy ra từ cùng UC-085 Normal Flow, bỏ field "Dish Selection Dropdown" vì không áp dụng.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Meal Session Selector | Dropdown | Chọn phiên ăn (Sáng/Trưa) trong 24h gần nhất, không chọn món cụ thể | BR-139 | |
| 2 | Rating Score Component | Star rating (1-5) | Bắt buộc | — | |
| 3 | Quality Concern Tags | Checkbox/Tag group (điều kiện <3 sao) | Bắt buộc nếu <3 sao | BR-140 | |
| 4 | General Comments Section | Textarea | Tuỳ chọn — phản hồi chất lượng dịch vụ/vệ sinh chung | BR-142 | |
| 5 | Photo Attachment Component | File upload | Tuỳ chọn | — | |
| 6 | Send Feedback Button | Button (primary, full-width) | Validate, chặn ngoài 24h | BR-139 | MSG-022/046/058 |

### Trạng thái màn hình
- Loading: Spinner khi submit.
- Empty: không áp dụng.
- Error: `SnackBar` `danger` + MSG-004/005/022.
- Success: `SnackBar` `success` (MSG-046); `warning` nếu MSG-058.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Feedback Center ("food quality" tile).
**Bố cục**: AppBar (back) → Meal Session Selector → Rating Score → Quality Concern Tags (điều kiện) → General Comments → Photo Attachment → Send Feedback Button cố định đáy.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Food Quality Feedback, nền tảng Mobile, actor Parent.
Bố cục: AppBar + Meal Session Selector + Rating Score + Quality Concern Tags (điều kiện) + General Comments Textarea + Photo Attachment + nút Send Feedback cố định đáy.
Thành phần bắt buộc: Meal Session Selector, Rating Score Component, Quality Concern Tags, General Comments Section, Photo Attachment Component, Send Feedback Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005/022), success (MSG-046), warning (MSG-058).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4109–4129 (UC-085 — dùng chung, không có wireframe riêng cho biến thể "Food Quality").
- `03_Design/04_ScreenFlow/05_Parent_Screen_Flow.md` (FeedbackCenter -->|"food quality"| FoodQualityFeedback).
- `business_rules.md` BR-139, BR-140, BR-142, BR-143; `error_message.md` MSG-022, MSG-046, MSG-058.
