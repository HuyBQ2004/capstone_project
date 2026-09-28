# Screen Spec — Distribution List

**Actor**: Teacher | **UC liên quan**: UC-016 - Confirm Meal Distribution (bước 1-2) | **Status**: Draft
**BR liên quan**: BR-120 (xác nhận phân phát theo lớp), BR-121 (theo dõi lịch sử bữa ăn), BR-123 (cảnh báo dị ứng khi quét ID — hiển thị nổi bật), BR-130 (ngăn phân phát trùng lặp)
**MSG liên quan**: MSG-004, MSG-005, MSG-021, MSG-045

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Home Shortcut Button | Button | Về Teacher Home | — | Nguồn: Report 3 §3.3.2.6 "Meal Distribution" |
| 2 | Screen Title Header | Text | "Meal Distribution" | — | |
| 3 | Status Filter Tabs | Tabs | "All (N)" / "Pending" / "Served" / "Special Meal" | BR-120 | |
| 4 | Student Meal Status Card | Card list (1 học sinh/card) | Avatar initials, họ tên, tag chế độ ăn ("Vegetarian Meal"/"Normal Meal"), cảnh báo dị ứng chữ đỏ ("Warning: [Allergen] Allergy") nếu có, Status Indicator, link "Details" | BR-123, BR-125 | Tap "Details" → Distribution Detail |
| 5 | Bottom Tab Navigation | Nav | 4 tab: Home, Students, Meals (active), Incidents | — | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Chưa có phiên phân phát bữa ăn hôm nay".
- Error: `SnackBar` `danger` + MSG-004/005; MSG-021 nếu phát hiện quét/xác nhận trùng lặp.
- Success: `SnackBar` `success` (MSG-045) sau khi xác nhận toàn bộ lớp đã phục vụ.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Teacher Home ("Distribute Meals Today"). Ra: Distribution Detail (tap "Details"), Distribution Confirmation (xác nhận từng học sinh/cả lớp).
**Bố cục**: AppBar (Home shortcut + tiêu đề) → Status Filter Tabs → danh sách `Card` học sinh cuộn dọc → `BottomNavigationBar`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Distribution List, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Status Filter Tabs (All/Pending/Served/Special Meal) + danh sách Card học sinh (avatar, tên, tag chế độ ăn, cảnh báo dị ứng đỏ, status, link Details) + BottomNavigationBar.
Thành phần bắt buộc: Status Filter Tabs, Student Meal Status Card.
Áp dụng Design System: cảnh báo dị ứng bắt buộc màu danger/đỏ (BR-123, BR-123).
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005/021), success (MSG-045).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6335–6349 (Meal Distribution, field-by-field gốc).
- `traceability_matrix.md` §1.9 UC-016.
- `business_rules.md` BR-120, BR-121, BR-123, BR-130.
