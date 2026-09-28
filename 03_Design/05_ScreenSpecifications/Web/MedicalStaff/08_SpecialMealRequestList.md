# Screen Spec — Special Meal Request List

**Actor**: Medical Staff | **UC liên quan**: UC-036 (Report 3 §3.2, dòng 1977–1995 — xem ghi chú lệch tên cuối file) | **Status**: Draft
**BR liên quan**: BR-031 (xét duyệt), BR-066 (danh mục chế độ ăn đặc biệt), BR-172 (log không sửa/xóa)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-036 (Success)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header ("Special Meal") | Text | — | — | Report 3 §3.3.2.3.3 |
| 2 | Tab "Awaiting approval" | Tab | — | BR-031 | |
| 3 | Tab "Approved" | Tab | — | — | |
| 4 | Bảng danh sách yêu cầu | Table | — | — | Cột: Student Name & Class, Status Badge, Meal Type, Reason |
| 5 | Status Badge (mỗi dòng) | Badge màu | "Needs Approval" (warning) | BR-031 | |
| 6 | Chọn 1 dòng → xem chi tiết | Interaction | — | — | Điều hướng `07_ReviewSpecialMealRequestScreen.md` |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng.
- Empty: "Không có yêu cầu bữa ăn đặc biệt nào" (mỗi tab) + illustration.
- Error: MSG-004, MSG-005.
- Success: không áp dụng ở màn List.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Approve Special Meal" trên Dashboard (Web); chọn dòng → `07_ReviewSpecialMealRequestScreen.md` (Web).
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — Page Header 2 Tab, Content Area dùng `Table`, phân trang dưới bảng.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Approve Special Meal" trên Dashboard (Mobile); chọn Card → `07_ReviewSpecialMealRequestScreen.md` (Mobile).
**Bố cục**: Report 3 §3.3.2.3.3 — AppBar (Back, Home, Screen Title), 2 Tab ngang, danh sách `Card` cuộn dọc, pull-to-refresh bắt buộc.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Special Meal Request List, nền tảng Web, actor Medical Staff.
Bố cục: Page Header 2 Tab (Awaiting approval/Approved), Table (Student Name & Class, Status Badge, Meal Type, Reason).
Áp dụng Design System: warning #B45309 cho "Needs Approval".
Trạng thái cần thể hiện: loading, empty (mỗi tab), error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1977–1995 (UC-036), dòng 5731–5757 (§3.3.2.3.3).
- business_rules.md (BR-031, BR-066, BR-172).
- error_message.md (MSG-004, MSG-005, MSG-036).

**Ghi chú lệch tên**: giống bản Mobile — UC-036 trong Report 3 mô tả "View List Allergy & Special Meals", field-by-field thực tế lấy từ §3.3.2.3.3. **[Giả định — cần team xác nhận]**.
