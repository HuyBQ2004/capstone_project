# Screen Spec — Teacher Home

**Actor**: Teacher | **UC liên quan**: — (entry point, không gắn UC cụ thể) | **Status**: Draft
**BR liên quan**: BR-025 (sức chứa/roster lớp chủ nhiệm), BR-121 (theo dõi lịch sử bữa ăn), BR-146 (tự động kích hoạt điều tra sự cố khi ≥3 học sinh triệu chứng tương đồng/24h — hiển thị cảnh báo liên quan nếu có)
**MSG liên quan**: MSG-004 (Session expired), MSG-005 (Access denied)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | Hiển thị "Dashboard" + subtext "Class in charge: [Tên lớp] · [Tên giáo viên]" | BR-025 | Nguồn: Report 3 §3.3.2.6 Teacher Dashboard |
| 2 | Greeting Banner | Card/Info box | "Hello, [Tên giáo viên]!" + ngày hệ thống hiện tại | — | |
| 3 | Total Students Card | Metric tile | Số học sinh trong lớp chủ nhiệm | BR-025 (tối đa 50 HS/lớp) | Lấy từ roster lớp (UC-056 bước 2 — class registry assigned to Teacher) |
| 4 | Meals Pending Card | Metric tile | Số suất ăn chưa phân phát trong phiên hiện tại | BR-120, BR-130 | Liên kết Distribution List |
| 5 | Dietary Notes Card | Metric tile | Số học sinh có ghi chú chế độ ăn đặc biệt | BR-065 | Liên kết Special Meal Requirements |
| 6 | Health Incidents Card | Metric tile | Số báo cáo sự cố sức khỏe giáo viên đã gửi (đang mở) | BR-146 | Liên kết Incident History |
| 7 | Distribute Meals Today Button | Button (primary, full-width) | Điều hướng tới Distribution List | BR-120 | |
| 8 | Report New Health Incident Button | Button (secondary, full-width) | Điều hướng tới Incident Form | BR-146 | |
| 9 | Today's Notice Board Card | List/Card | Danh sách bullet cảnh báo khẩn (dị ứng, thực đơn đặc biệt, tình trạng sự cố mới nhất) | BR-123, BR-138 | Cảnh báo `critical`/BR-138 hiển thị không qua duyệt |
| 10 | Bottom Tab Navigation | Nav | 4 tab: Home (active), Students, Meals, Incidents | — | Xem `Navigation_Guidelines.md` mục 2 |

### Trạng thái màn hình
- Loading: Shimmer cho 4 metric card khi tải dashboard.
- Empty: Nếu chưa có lớp chủ nhiệm được gán (TH hiếm, lỗi phân công) → hiển thị thông báo "Chưa được phân công lớp chủ nhiệm — liên hệ Ban giám hiệu" [Giả định — cần team xác nhận, vì Report 3 không mô tả trạng thái này].
- Error: `SnackBar` màu `danger` + MSG-004/MSG-005 khi hết phiên/không có quyền.
- Success: Không áp dụng (màn hình chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Điểm vào ngay sau Common Entry Flow (Login) — xem `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md`. Điều hướng đi: Announcement Detail (Notice Board), Meal Distribution (Distribute Meals Today), Health Incident Report Form (Report New Health Incident), Incident History (View History), Notifications (bell icon).
**Bố cục**: AppBar tiêu đề "Dashboard" + icon thông báo; Content 1 cột cuộn dọc gồm Greeting Banner → 4 Metric Card (2x2 grid) → 2 action button full-width → Notice Board Card; `BottomNavigationBar` 4 mục (Home/Students/Meals/Incidents) theo `Layout_Guidelines.md` mục 2.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Teacher Home, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Greeting Banner + 4 Metric Card dạng lưới 2x2 + 2 nút hành động full-width + Notice Board Card + BottomNavigationBar 4 mục (Home/Students/Meals/Incidents).
Thành phần bắt buộc: Screen Title Header, Greeting Banner, Total Students Card, Meals Pending Card, Dietary Notes Card, Health Incidents Card, Distribute Meals Today Button, Report New Health Incident Button, Today's Notice Board Card.
Áp dụng Design System: primary #0E7490, Card Material 3, cảnh báo dùng token `danger`/`critical` theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading (shimmer), error (MSG-004/MSG-005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6314–6333 (Teacher Dashboard, field-by-field gốc, Mobile).
- `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md`, `12_Teacher_Screen_Flow_Web.md`.
- `business_rules.md` BR-025, BR-120, BR-123, BR-130, BR-138, BR-146.
- `traceability_matrix.md` §1.9 (UC-016, UC-056, UC-080), §1.11 (UC-074).
