# Screen Spec — Nutrition Detail

**Actor**: Parent | **UC liên quan**: UC-135 - View Menu Information (bước 4, chi tiết dinh dưỡng) | **Status**: Draft
**BR liên quan**: BR-062 (mục tiêu dinh dưỡng theo nhóm tuổi), BR-069 (khai báo đầy đủ nguyên liệu/dị ứng)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Dish Information Card | Card | Tên món, phân loại món, nhà cung cấp catering, bếp chế biến | BR-069 | Nguồn: Report 3 §3.3.2.4 Dish Details |
| 2 | Full Ingredient Log Section | Text block | Danh sách nguyên liệu đầy đủ kèm nguồn gốc | BR-069, BR-076 (truy xuất lô) | |
| 3 | Allergen Warnings Section | Alert (danger, có điều kiện) | "Contains [Allergen]" / "(Shellfish/Peanut free)" | BR-069, BR-123 | |
| 4 | Give Feedback Button | Button (primary, full-width) | → Submit Meal Feedback (cho đúng món này) | — | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: không áp dụng (luôn có dữ liệu khi vào từ Weekly Menu).
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Weekly Menu (tap món ăn). Ra: Submit Meal Feedback.
**Bố cục**: AppBar (back) → Dish Information Card → Full Ingredient Log Section → Allergen Warnings Section (nếu có) → Give Feedback Button cố định đáy.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Nutrition Detail, nền tảng Mobile, actor Parent.
Bố cục: AppBar + Dish Information Card + Full Ingredient Log + Allergen Warnings (đỏ, có điều kiện) + nút Give Feedback cố định đáy.
Thành phần bắt buộc: Dish Information Card, Full Ingredient Log Section, Allergen Warnings Section, Give Feedback Button.
Áp dụng Design System: Allergen Warnings = danger.
Trạng thái cần thể hiện: loading, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6070–6086 (Dish Details, field-by-field gốc).
- `business_rules.md` BR-062, BR-069, BR-076, BR-123.
