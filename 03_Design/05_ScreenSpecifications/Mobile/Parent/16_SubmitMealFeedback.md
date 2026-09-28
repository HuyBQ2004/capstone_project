# Screen Spec — Submit Meal Feedback

**Actor**: Parent | **UC liên quan**: UC-085 - Submit Meal & Food Quality Feedback | **Status**: Draft
**BR liên quan**: BR-139 (cửa sổ 24h), BR-140 (mô tả vấn đề bắt buộc khi <3 sao), BR-142 (phát hiện từ khóa bệnh lý), BR-143 (Service Caterers phản hồi lại), BR-145 (ẩn danh dữ liệu tổng hợp)
**MSG liên quan**: MSG-004, MSG-005, MSG-022 (ngoài cửa sổ 24h), MSG-046 (thành công), MSG-058 (phát hiện từ khóa bệnh lý)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Dish Selection Dropdown | Dropdown | Chọn món đã phục vụ trong 24h gần nhất | BR-139 | Nguồn: Report 3 §3.3.2.4 Meal Feedback |
| 2 | Rating Score Component | Star rating (1-5) | Bắt buộc | — | |
| 3 | Quality Concern Description | `Input` (text tự do, chỉ hiện khi <3 sao) | Bắt buộc nhập nếu rating <3 | BR-140 | Đổi từ Checkbox/Tag group sang Input theo yêu cầu team (2026-07-01) — phụ huynh tự mô tả vấn đề (VD "quá mặn", "nguội") thay vì chọn nhãn có sẵn |
| 4 | Review Comments Section | Textarea | Tuỳ chọn | BR-142 | Hệ thống quét từ khóa bệnh lý tự động |
| 5 | Photo Attachment Component | File upload (camera/gallery) | Tuỳ chọn | — | |
| 6 | Send Feedback Button | Button (primary, full-width) | Validate, chặn nếu ngoài 24h | BR-139 | MSG-022 nếu hết hạn; MSG-046 khi thành công; MSG-058 nếu phát hiện từ khóa bệnh lý |

### Trạng thái màn hình
- Loading: Spinner khi submit.
- Empty: không áp dụng.
- Error: `SnackBar` `danger` + MSG-004/005/022.
- Success: `SnackBar` `success` (MSG-046); nếu phát hiện từ khóa bệnh lý → thêm cảnh báo `warning` (MSG-058) và thông báo Medical Staff được kích hoạt.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Feedback Center, Nutrition Detail ("Give Feedback on this Dish"), hoặc Weekly Menu. Ra: về Feedback Center.
**Bố cục**: AppBar (back) → Dish Selection Dropdown → Rating Score Component → Quality Concern Description (input, điều kiện) → Review Comments → Photo Attachment → Send Feedback Button cố định đáy.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Meal Feedback, nền tảng Mobile, actor Parent.
Bố cục: AppBar + Dish Selection Dropdown + Rating Score (5 sao) + Quality Concern Description input (điều kiện <3 sao) + Review Comments Textarea + Photo Attachment + nút Send Feedback cố định đáy.
Thành phần bắt buộc: Dish Selection Dropdown, Rating Score Component, Quality Concern Description Input, Review Comments Section, Photo Attachment Component, Send Feedback Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005/022), success (MSG-046), warning (MSG-058).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6123–6140 (Meal Feedback, field-by-field gốc), dòng 4109–4129 (UC-085 Normal/Alternative Flow).
- `business_rules.md` BR-139, BR-140, BR-142, BR-143, BR-145; `error_message.md` MSG-022, MSG-046, MSG-058.
