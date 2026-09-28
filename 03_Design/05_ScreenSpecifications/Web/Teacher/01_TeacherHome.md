# Screen Spec — Teacher Home

**Actor**: Teacher | **UC liên quan**: — (entry point, không gắn UC cụ thể) | **Status**: Draft
**BR liên quan**: BR-025, BR-121, BR-146
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | Tiêu đề "Dashboard" + "Class in charge: [Tên lớp] · [Tên giáo viên]" | BR-025 | |
| 2 | Greeting Banner | Alert (info) | Lời chào + ngày hiện tại | — | |
| 3 | Total Students Card | Metric card | Số học sinh lớp chủ nhiệm | BR-025 | |
| 4 | Meals Pending Card | Metric card | Số suất ăn chưa phân phát | BR-120, BR-130 | |
| 5 | Dietary Notes Card | Metric card | Số học sinh có chế độ ăn đặc biệt | BR-065 | |
| 6 | Health Incidents Card | Metric card | Số báo cáo sự cố đang mở | BR-146 | |
| 7 | Distribute Meals Today Button | Button (primary) | Điều hướng Distribution List | BR-120 | Góc trên phải Page Header |
| 8 | Report New Health Incident Button | Button (secondary) | Điều hướng Incident Form | BR-146 | |
| 9 | Today's Notice Board Card | Alert list | Cảnh báo khẩn (dị ứng, thực đơn đặc biệt, sự cố mới nhất) | BR-123, BR-138 | |

### Trạng thái màn hình
- Loading: `Skeleton` cho 4 metric card.
- Empty: "Chưa được phân công lớp chủ nhiệm — liên hệ Ban giám hiệu" [Giả định — cần team xác nhận].
- Error: `Alert variant="destructive"` + MSG-004/MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sau Common Entry Flow (Login Web) — xem `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md`. Điều hướng đi: Announcement Detail, Meal Distribution, Health Incident Report Form, Incident History, Notifications.
**Bố cục**: Topbar (breadcrumb, notif, avatar) + Sidebar rút gọn (nhóm: Bảng tin / Phân phát Bữa ăn / Sự cố Sức khỏe / Cá nhân, theo `12_Teacher_Screen_Flow_Web.md`) + Content Area grid 12 cột: Greeting Banner (col-span-12), 4 Metric Card (col-span-3 mỗi card), 2 action button hàng ngang, Notice Board Card (col-span-12) — theo `Layout_Guidelines.md` mục 1.1.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Teacher Home, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar rút gọn 4 nhóm + Content grid 12 cột (Greeting Banner full-width, 4 Metric Card ngang hàng, 2 nút hành động, Notice Board Card).
Thành phần bắt buộc: Page Header, Greeting Banner, Total Students Card, Meals Pending Card, Dietary Notes Card, Health Incidents Card, Distribute Meals Today Button, Report New Health Incident Button, Today's Notice Board Card.
Áp dụng Design System: shadcn/ui "new-york", primary #0E7490, bo góc 8px.
Trạng thái cần thể hiện: loading (Skeleton), error (MSG-004/MSG-005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6314–6333 (Teacher Dashboard, bản Mobile gốc — dùng chung business logic).
- `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md`.
- `business_rules.md` BR-025, BR-120, BR-123, BR-130, BR-138, BR-146.
