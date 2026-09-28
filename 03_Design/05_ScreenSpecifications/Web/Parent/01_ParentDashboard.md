# Screen Spec — Parent Dashboard

**Actor**: Parent | **UC liên quan**: — (entry point) | **Status**: Draft
**BR liên quan**: BR-031, BR-123, BR-138
**MSG liên quan**: MSG-004, MSG-005

> Xem ghi chú về BR-040 (giới hạn số con/phụ huynh — không tồn tại trong `business_rules.md` chính thức) ở bản Mobile cùng tên.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Child Selector | Dropdown (Topbar) | Chuyển đổi hồ sơ con | BR-031 | Không phải Dashboard riêng — dropdown ở Topbar (`10_Parent_Screen_Flow_Web.md`) |
| 2 | Notification Bell Icon | Icon button | Mở Notification Center | — | |
| 3 | Allergy Threat Alert Box | Alert (destructive, có điều kiện) | "Menu Allergy Threat Detected!" | BR-123 | |
| 4 | Today's Lunch Menu Card | Card | Món ăn hôm nay + link chi tiết tuần | BR-070 | |
| 5 | Health Hub / School Meals / Meal Feedback / Meal History Tiles | Card grid (4 ô) | Điều hướng 4 khu vực chính | — | |
| 6 | Report Urgent Safety Incident Button | Button (destructive, full-width) | → Report Food Safety Concern | BR-146, BR-138 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Chưa liên kết hồ sơ học sinh" + hướng dẫn nhập mã học sinh.
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sau Common Entry Flow (Login Web) — `10_Parent_Screen_Flow_Web.md`.
**Bố cục**: Topbar (Child Selector + Notif + Avatar) + Sidebar (5 nhóm: Thực đơn & Dinh dưỡng / Sức khỏe & Dị ứng / Bữa ăn của con / Phản hồi & Sự cố / Cá nhân) + Content: Allergy Alert (nếu có) + Today's Lunch Menu Card + Card grid 4 ô + nút báo cáo khẩn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Parent Dashboard, nền tảng Web, actor Parent.
Bố cục: Topbar (Child Selector + Notif) + Sidebar 5 nhóm + Content (Allergy Alert, Today's Lunch Menu Card, 4-card grid, nút báo cáo khẩn destructive).
Thành phần bắt buộc: Allergy Threat Alert Box, Today's Lunch Menu Card, 4 Navigation Tile, Report Urgent Safety Incident Button.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5952–5971 (bản Mobile gốc).
- `03_Design/04_ScreenFlow/10_Parent_Screen_Flow_Web.md`.
- `business_rules.md` BR-031, BR-123, BR-138, BR-146.
