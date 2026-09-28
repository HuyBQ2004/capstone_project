# Screen Spec — School Meals Dashboard

**Actor**: Parent | **UC liên quan**: UC-055 - Monitor School Meals | **Status**: Draft
**BR liên quan**: BR-069 (khai báo nguyên liệu/dị ứng), BR-070 (chỉ menu Published), BR-075 (FEFO/truy xuất)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: không có wireframe field-by-field riêng trong Report 3 cho màn tổng quan này — suy ra từ UC-055 Normal Flow. Đây là màn tổng quan cấp cao hơn Weekly Menu (menu tuần hiện tại + tuần tới, đối chiếu hồ sơ dị ứng của con), khác với Weekly Menu chi tiết theo ngày.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "School Meals" | — | |
| 2 | Current & Upcoming Week Menu Summary | Card list | Tóm tắt menu tuần hiện tại + tuần tới (đã Published) | BR-070 | UC-055 bước 2 |
| 3 | Nutrition & Allergen Highlight | Badge/Alert | Đánh dấu món có allergen liên quan hồ sơ dị ứng của con | BR-069 | UC-055 bước 4 |
| 4 | View Weekly Menu Link | Button | → Weekly Menu (xem chi tiết theo ngày) | — | |
| 5 | View Nutrition Breakdown Link | Button | → Nutrition Detail/History | — | UC-055 bước 5 |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Chưa có thực đơn được công bố" [Giả định — không có MSG riêng, tương tự Weekly Menu].
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Parent Dashboard ("School Meals" tile). Ra: Weekly Menu, Nutrition Detail/History.
**Bố cục**: AppBar (back) → Current & Upcoming Week Menu Summary (Card list) → Nutrition & Allergen Highlight → 2 nút điều hướng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình School Meals Dashboard, nền tảng Mobile, actor Parent.
Bố cục: AppBar + danh sách Card tóm tắt menu tuần hiện tại/tới + khối cảnh báo allergen + 2 nút điều hướng (Weekly Menu/Nutrition Breakdown).
Thành phần bắt buộc: Current & Upcoming Week Menu Summary, Nutrition & Allergen Highlight, View Weekly Menu Link, View Nutrition Breakdown Link.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3865–3885 (UC-055 Monitor School Meals — Normal Flow).
- `business_rules.md` BR-069, BR-070, BR-075.
