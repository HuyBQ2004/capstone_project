# Screen Spec — Medical Staff Dashboard

**Actor**: Medical Staff | **UC liên quan**: — (entry point, không có UC riêng) | **Status**: Draft
**BR liên quan**: BR-031 (xét duyệt thông tin sức khỏe), BR-066 (danh mục chế độ ăn đặc biệt), BR-146 (tự động kích hoạt điều tra sự cố), BR-100/101/104 (điểm số vệ sinh)
**MSG liên quan**: không áp dụng

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | — | — | Report 3 §3.3.2.3.1 (suy luận layout Web từ field Mobile) |
| 2 | Welcome Banner | Card | — | — | Tên người dùng, phòng làm việc, ngày hiện tại |
| 3 | Card "Allergy pending approval" | Thống kê (số) | — | BR-031 | Điều hướng sang `02_ReviewHealthInformationScreen.md` |
| 4 | Card "Meal order needs approval" | Thống kê (số) | — | BR-066 | Điều hướng sang `08_SpecialMealRequestList.md` |
| 5 | Card "The issue is being resolved" | Thống kê (số) | — | BR-146 | Điều hướng sang `25_IncidentList.md` |
| 6 | Card "Compliance Points" | Thống kê (%) | — | BR-100, BR-101, BR-104 | Điều hướng sang `23_ComplianceDashboard.md` |
| 7 | Shortcut "Health Profile" | Card/Button | — | — | Điều hướng sang `03_StudentHealthRecordList.md` |
| 8 | Shortcut "Student Allergy Review" | Card/Button | — | BR-031 | Điều hướng sang `02_ReviewHealthInformationScreen.md` |
| 9 | Shortcut "Approve Special Meal" | Card/Button | — | BR-066 | Điều hướng sang `08_SpecialMealRequestList.md` |
| 10 | Shortcut "City Safety Incident" | Card/Button | — | — | Điều hướng sang `25_IncidentList.md` |
| 11 | Shortcut "Food Compliance & Monitoring Board" | Card/Button | — | — | Điều hướng sang `23_ComplianceDashboard.md` |
| 12 | Shortcut "View food inspection results in 3 steps" | Card/Button | — | BR-079 | Điều hướng sang `11_FoodInspectionRecords.md` |

### Trạng thái màn hình
- Loading: `Skeleton` cho 4 card thống kê khi tải số liệu.
- Empty: giá trị 0 hiển thị bình thường.
- Error: card không tải được → icon lỗi nhỏ + nút "Thử lại" trong card đó.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào ngay sau Login thành công (role Medical Staff); là trang gốc sau khi chọn menu Sidebar (nếu có) hoặc mặc định khi đăng nhập.
**Bố cục**: `Layout_Guidelines.md` mục 1.1 (Dashboard Shell) — Sidebar 5 nhóm theo `08_Medical_Staff_Screen_Flow_Web.md`, Content Area dùng grid 12 cột: 4 Card thống kê chiếm `col-span-3` mỗi card trên 1 hàng, 6 shortcut card bên dưới dạng grid `col-span-4` (3 cột × 2 hàng).

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Medical Staff Dashboard, nền tảng Web, actor Medical Staff.
Bố cục: Sidebar 5 nhóm + Topbar, Content Area grid 12 cột: hàng 1 gồm 4 Card thống kê
(Allergy pending approval, Meal order needs approval, Issue being resolved, Compliance Points),
hàng 2-3 gồm 6 shortcut card (Health Profile, Student Allergy Review, Approve Special Meal,
City Safety Incident, Food Compliance & Monitoring Board, View food inspection results in 3 steps).
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading (Skeleton trên card số liệu).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5683–5706 (§3.3.2.3.1, field gốc Mobile — dùng chung business logic).
- business_rules.md (BR-031, BR-066, BR-100, BR-101, BR-104, BR-138, BR-146).
- 03_Design/04_ScreenFlow/08_Medical_Staff_Screen_Flow_Web.md.
