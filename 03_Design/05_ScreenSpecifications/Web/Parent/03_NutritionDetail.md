# Screen Spec — Nutrition Detail

**Actor**: Parent | **UC liên quan**: UC-135 - View Menu Information (bước 4) | **Status**: Draft
**BR liên quan**: BR-062, BR-069
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Dish Information Card | Card | Tên món, phân loại, nhà cung cấp, bếp | BR-069 | |
| 2 | Full Ingredient Log Section | Text block | Danh sách nguyên liệu | BR-069, BR-076 | |
| 3 | Allergen Warnings Section | Alert (destructive, có điều kiện) | Cảnh báo allergen | BR-069, BR-123 | |
| 4 | Give Feedback Button | Button (primary) | → Submit Meal Feedback | — | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Error: `Alert variant="destructive"` + MSG-004/005.
- Empty/Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Weekly Menu (click dish card).
**Bố cục**: Topbar + Sidebar + Content: Dish Information Card (col-span-8) + Full Ingredient Log + Allergen Warnings Alert + nút Give Feedback góc dưới phải.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Nutrition Detail, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Dish Information Card + Ingredient Log + Allergen Warnings Alert + nút Give Feedback.
Thành phần bắt buộc: Dish Information Card, Full Ingredient Log Section, Allergen Warnings Section, Give Feedback Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6070–6086.
- `business_rules.md` BR-062, BR-069, BR-076, BR-123.
