# Screen Spec — Distribution List

**Actor**: Teacher | **UC liên quan**: UC-016 - Confirm Meal Distribution (bước 1-2) | **Status**: Draft
**BR liên quan**: BR-120, BR-121, BR-123, BR-130
**MSG liên quan**: MSG-004, MSG-005, MSG-021, MSG-045

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Meal Distribution" | — | |
| 2 | Status Filter Tabs | Tabs | All (N)/Pending/Served/Special Meal | BR-120 | |
| 3 | Student Meal Status Table | `Table` | Avatar, họ tên, tag chế độ ăn, cảnh báo dị ứng (đỏ), status, action "Xem chi tiết" | BR-123, BR-125 | |
| 4 | Confirm All Button | Button (primary, góc trên phải) | Xác nhận toàn lớp đã phục vụ | BR-120 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Chưa có phiên phân phát bữa ăn hôm nay".
- Error: `Alert variant="destructive"` + MSG-004/005; MSG-021 khi trùng lặp.
- Success: `Toast` `success` (MSG-045).

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Phân phát Bữa ăn" (theo `12_Teacher_Screen_Flow_Web.md`). Ra: Distribution Detail, Distribution Confirmation.
**Bố cục**: Topbar + Sidebar + Content: Page Header (action "Confirm All" góc trên phải) + Status Filter Tabs + `Table` học sinh.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Distribution List, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Page Header (nút Confirm All) + Status Filter Tabs + Table học sinh (avatar, tên, tag chế độ ăn, cảnh báo dị ứng, status, action).
Thành phần bắt buộc: Status Filter Tabs, Student Meal Status Table, Confirm All Button.
Áp dụng Design System: cảnh báo dị ứng màu danger (BR-123, BR-123).
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005/021), success (MSG-045).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6335–6349 (bản Mobile gốc).
- `traceability_matrix.md` §1.9 UC-016.
- `business_rules.md` BR-120, BR-121, BR-123, BR-130.
