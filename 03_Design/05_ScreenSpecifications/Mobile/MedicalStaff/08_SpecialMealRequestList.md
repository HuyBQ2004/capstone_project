# Screen Spec — Special Meal Request List

**Actor**: Medical Staff | **UC liên quan**: UC-036 (Report 3 §3.2, dòng 1977–1995 — xem ghi chú lệch tên cuối file) | **Status**: Draft
**BR liên quan**: BR-031 (xét duyệt), BR-066 (danh mục chế độ ăn đặc biệt), BR-172 (log không sửa/xóa)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-036 (Success)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title ("Special Meal") | Text | — | — | Report 3 §3.3.2.3.3 |
| 2 | Tab "Awaiting approval" | Tab | — | BR-031 | Danh sách yêu cầu đang chờ duyệt |
| 3 | Tab "Approved" | Tab | — | — | Lịch sử yêu cầu đã duyệt |
| 4 | Special Meal Card (mỗi item) | Card | — | — | |
| 5 | Student Name & Class (trong Card) | Text | — | — | |
| 6 | Status Badge (trong Card) | Badge màu | "Needs Approval" (warning) | BR-031 | |
| 7 | Meal Type (trong Card) | Text | Phải thuộc danh mục BR-066 | BR-066 | VD "Purely vegetarian menu" |
| 8 | Reason (trong Card) | Text | — | — | VD "Religion / Voluntary" |
| 9 | Chọn 1 Card → xem chi tiết | Interaction | — | — | Điều hướng `07_ReviewSpecialMealRequestScreen.md` |

### Trạng thái màn hình
- Loading: `Shimmer`/`Skeleton` cho danh sách.
- Empty: "Không có yêu cầu bữa ăn đặc biệt nào" (mỗi tab) + illustration.
- Error: MSG-004, MSG-005.
- Success: không áp dụng ở màn List (MSG-036 hiển thị sau khi Approve ở dialog con).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Approve Special Meal" trên Dashboard; chọn Card → `07_ReviewSpecialMealRequestScreen.md`.
**Bố cục**: Report 3 §3.3.2.3.3 — AppBar (Back, Home, Screen Title), 2 Tab ngang (Awaiting approval/Approved), danh sách `Card` cuộn dọc, pull-to-refresh bắt buộc.

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Approve Special Meal" trên Dashboard (Web); chọn dòng → `07_ReviewSpecialMealRequestScreen.md` (Web).
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — Page Header 2 Tab, Content Area dùng `Table` (cột: Student Name & Class, Status Badge, Meal Type, Reason), phân trang dưới bảng.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Special Meal Request List, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: 2 Tab (Awaiting approval/Approved), danh sách yêu cầu
(Student Name & Class, Status Badge, Meal Type, Reason).
Áp dụng Design System: warning #B45309 cho "Needs Approval".
Trạng thái cần thể hiện: loading, empty (mỗi tab), error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1977–1995 (UC-036), dòng 5731–5757 (§3.3.2.3.3).
- business_rules.md (BR-031, BR-066, BR-172).
- error_message.md (MSG-004, MSG-005, MSG-036).

**Ghi chú lệch tên**: `14_Screen_Authorization_Matrix.md` ánh xạ màn "Special Meal Request List" với UC-036, nhưng nội dung UC-036 trong Report 3 mô tả là "View List Allergy & Special Meals" (thiên về xác minh **hồ sơ dị ứng**, không chỉ riêng special meal). Field-by-field thực tế của màn List này lấy từ Report 3 §3.3.2.3.3 "Screens Special Meal For Student" (đáng tin cậy hơn cho đúng màn hình). **[Giả định — cần team xác nhận]** UC-036 có đúng là UC nền cho màn này hay đây là 1 trong các trường hợp lệch tên đã ghi nhận ở `03_Medical_Staff_Screen_Flow.md` (mục ghi chú đầu file).
