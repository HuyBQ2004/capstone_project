# Screen Spec — Distribution Detail

**Actor**: Teacher | **UC liên quan**: UC-016 - Confirm Meal Distribution (bước 3), UC-103 - View Allergy Alerts | **Status**: Draft
**BR liên quan**: BR-120, BR-123, BR-125
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Identity Header | Text | Avatar, họ tên, lớp | — | |
| 2 | Assigned Meal Card | Card | Món ăn được phân công | BR-125 | |
| 3 | Allergy Warning Banner | Alert (destructive, nếu có) | "Warning: [Allergen] Allergy" | BR-123 | |
| 4 | Distribution Status Badge | Badge | Pending/Served | BR-120 | |
| 5 | Confirm Served Button | Button (primary) | Mở Distribution Confirmation (Dialog) | BR-120, BR-130 | |
| 6 | Report Distribution Error Button | Button (secondary/ghost) | Báo lỗi phân phát sai (UC-016 AF1) | BR-065 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Error: `Alert variant="destructive"` + MSG-004/005.
- Empty/Success: không áp dụng tại màn này.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Distribution List (action "Xem chi tiết"). Ra: Distribution Confirmation (Dialog).
**Bố cục**: Topbar + Sidebar + Content: Student Identity Header + Assigned Meal Card (col-span-6) + Allergy Warning Banner + Distribution Status Badge + 2 nút hành động.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Distribution Detail, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Student Identity Header + Assigned Meal Card + Allergy Warning Alert + Status Badge + nút Confirm Served + nút báo lỗi.
Thành phần bắt buộc: Student Identity Header, Assigned Meal Card, Allergy Warning Banner, Distribution Status Badge, Confirm Served Button, Report Distribution Error Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6335–6349, dòng 3799–3819 (UC-016).
- `business_rules.md` BR-120, BR-123, BR-125, BR-065.
