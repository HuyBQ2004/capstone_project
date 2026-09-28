# Screen Spec — School Meals Dashboard

**Actor**: Parent | **UC liên quan**: UC-055 - Monitor School Meals | **Status**: Draft
**BR liên quan**: BR-069, BR-070, BR-075
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "School Meals" | — | |
| 2 | Current & Upcoming Week Menu Summary | Card grid | Tóm tắt menu 2 tuần | BR-070 | |
| 3 | Nutrition & Allergen Highlight | Alert/Badge | Cảnh báo allergen liên quan | BR-069 | |
| 4 | View Weekly Menu Link | Button | → Weekly Menu | — | |
| 5 | View Nutrition Breakdown Link | Button | → Nutrition Detail/History | — | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Chưa có thực đơn được công bố" [Giả định].
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Bữa ăn của con" (`10_Parent_Screen_Flow_Web.md`).
**Bố cục**: Topbar + Sidebar + Content: Card grid tóm tắt menu 2 tuần + Alert allergen + 2 nút điều hướng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình School Meals Dashboard, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Card grid menu 2 tuần + Alert allergen + 2 nút điều hướng.
Thành phần bắt buộc: Current & Upcoming Week Menu Summary, Nutrition & Allergen Highlight, View Weekly Menu Link, View Nutrition Breakdown Link.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3865–3885 (UC-055).
- `business_rules.md` BR-069, BR-070, BR-075.
