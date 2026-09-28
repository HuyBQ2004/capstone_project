# Screen Spec — Distribution Detail

**Actor**: Teacher | **UC liên quan**: UC-016 - Confirm Meal Distribution (bước 3), UC-103 - View Allergy Alerts | **Status**: Draft
**BR liên quan**: BR-120, BR-123, BR-125
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: không có wireframe riêng trong Report 3 — suy ra từ link "Details" trên Distribution List (Report 3 dòng 6346) + UC-016 bước 3 "Teacher verifies each student received the correct meal".

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Identity Header | Text | Avatar, họ tên, lớp | — | |
| 2 | Assigned Meal Card | Card | Tên món ăn được phân công (thường/đặc biệt) | BR-125 | |
| 3 | Allergy Warning Banner | Banner (nếu có) | "Warning: [Allergen] Allergy" chữ/nền đỏ | BR-123 | Bắt buộc màu danger (BR-123) |
| 4 | Distribution Status Badge | Badge | "Pending"/"Served" | BR-120 | |
| 5 | Confirm Served Button | Button (primary) | Điều hướng Distribution Confirmation | BR-120, BR-130 | |
| 6 | Report Distribution Error Link | Link/Button (secondary) | Báo lỗi phân phát sai — theo UC-016 AF1 | BR-065 | Gửi thông báo cho Service Caterers |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: không áp dụng.
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng tại màn này (thành công hiển thị ở Distribution Confirmation).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Distribution List (tap "Details"). Ra: Distribution Confirmation.
**Bố cục**: AppBar (back) → Student Identity Header → Assigned Meal Card → Allergy Warning Banner (nếu có) → Distribution Status Badge → Confirm Served Button + Report Error Link.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Distribution Detail, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Student Identity Header + Assigned Meal Card + Allergy Warning Banner (đỏ, nếu có) + Status Badge + nút Confirm Served + link báo lỗi.
Thành phần bắt buộc: Student Identity Header, Assigned Meal Card, Allergy Warning Banner, Distribution Status Badge, Confirm Served Button, Report Distribution Error Link.
Áp dụng Design System: cảnh báo dị ứng màu danger bắt buộc.
Trạng thái cần thể hiện: loading, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6335–6349 (Meal Distribution — nguồn gián tiếp qua "Details Link"), dòng 3799–3819 (UC-016 Confirm Meal Distribution, AF1 Distribution Error).
- `business_rules.md` BR-120, BR-123, BR-125, BR-065.
