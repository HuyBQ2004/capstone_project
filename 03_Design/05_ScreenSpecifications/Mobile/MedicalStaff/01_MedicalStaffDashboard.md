# Screen Spec — Medical Staff Dashboard

**Actor**: Medical Staff | **UC liên quan**: — (entry point, không có UC riêng — tổng hợp số liệu từ nhiều module) | **Status**: Draft
**BR liên quan**: BR-031 (xét duyệt thông tin sức khỏe), BR-066 (danh mục chế độ ăn đặc biệt), BR-146 (tự động kích hoạt điều tra sự cố), BR-100/101/104 (điểm số vệ sinh — nguồn số liệu Compliance Points)
**MSG liên quan**: không áp dụng (màn hình tổng quan, không có hành động ghi)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | — | — | Report 3 §3.3.2.3.1 |
| 2 | Icon "Add/Plus" | Icon button | — | — | Shortcut tạo nhanh bản ghi mới |
| 3 | Icon "Notification" | Icon button (có badge đỏ) | Badge hiển thị số lượng cảnh báo hiện có | BR-138 | Điều hướng sang Notification Center (xem GuestCommon) |
| 4 | Welcome Banner | Text/Card | — | — | Tên người dùng, phòng làm việc, ngày hiện tại |
| 5 | Card "Allergy pending approval" | Thống kê (số) | — | BR-031 | Điều hướng sang `02_ReviewHealthInformationScreen.md` |
| 6 | Card "Meal order needs approval" | Thống kê (số) | — | BR-066 | Điều hướng sang `08_SpecialMealRequestList.md` |
| 7 | Card "The issue is being resolved" | Thống kê (số) | — | BR-146 | Điều hướng sang `25_IncidentList.md` |
| 8 | Card "Compliance Points" | Thống kê (%) | — | BR-100, BR-101, BR-104 | Điều hướng sang `23_ComplianceDashboard.md` |
| 9 | Nút "Health Profile" | Button | — | — | Điều hướng sang `03_StudentHealthRecordList.md` |
| 10 | Nút "Student Allergy Review" | Button | — | BR-031 | Điều hướng sang `02_ReviewHealthInformationScreen.md` |
| 11 | Nút "Approve Special Meal" | Button | — | BR-066 | Điều hướng sang `08_SpecialMealRequestList.md` |
| 12 | Nút "City Safety Incident" | Button | — | — | Điều hướng sang `25_IncidentList.md` |
| 13 | Nút "Food Compliance & Monitoring Board" | Button | — | — | Điều hướng sang `23_ComplianceDashboard.md` |
| 14 | Nút "View food inspection results in 3 steps" | Button | — | BR-079 | Điều hướng sang `11_FoodInspectionRecords.md` |

### Trạng thái màn hình
- Loading: `Shimmer` cho 4 card thống kê khi tải số liệu.
- Empty: nếu 1 thống kê = 0, hiển thị "0" bình thường (không phải trạng thái Empty riêng của toàn màn).
- Error: không tải được số liệu → thẻ card hiện icon lỗi nhỏ + nút "Thử lại" trong card đó (không chặn toàn màn hình).
- Success: không áp dụng.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào ngay sau Login thành công (role Medical Staff); là màn gốc của Bottom Navigation.
**Bố cục**: Report 3 §3.3.2.3.1 — AppBar (Screen Title + icon Add + icon Notification có badge), Welcome Banner ngay dưới, lưới 4 `Card` thống kê (2×2), danh sách nút hành động dạng `ListTile`/`Card` lớn xếp dọc bên dưới. Bottom Navigation theo `Navigation_Guidelines.md` mục 2 (3–5 mục, icon kèm chữ).

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Medical Staff Dashboard, nền tảng Mobile, actor Medical Staff.
Bố cục: AppBar (title + icon Add + icon Notification badge đỏ), Welcome Banner, lưới 2x2 Card thống kê
(Allergy pending approval, Meal order needs approval, Issue being resolved, Compliance Points),
6 nút hành động lớn (Health Profile, Student Allergy Review, Approve Special Meal, City Safety Incident,
Food Compliance & Monitoring Board, View food inspection results in 3 steps).
Áp dụng Design System: seed color #0E7490, badge đỏ #B91C1C cho notification.
Trạng thái cần thể hiện: loading (Shimmer trên card số liệu).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5683–5706 (§3.3.2.3.1, field-by-field gốc).
- business_rules.md (BR-031, BR-066, BR-100, BR-101, BR-104, BR-138, BR-146).
- 03_Design/04_ScreenFlow/03_Medical_Staff_Screen_Flow.md (sơ đồ điều hướng "Dash" node).
