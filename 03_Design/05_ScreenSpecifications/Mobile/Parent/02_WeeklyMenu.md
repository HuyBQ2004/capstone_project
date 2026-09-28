# Screen Spec — Weekly Menu

**Actor**: Parent | **UC liên quan**: UC-135 - View Menu Information | **Status**: Draft
**BR liên quan**: BR-031, BR-062 (mục tiêu dinh dưỡng theo nhóm tuổi), BR-069 (khai báo đầy đủ nguyên liệu/dị ứng), BR-070 (chỉ menu Published), BR-075 (FEFO — truy xuất nguyên liệu)
**MSG liên quan**: MSG-004, MSG-005, MSG-051 (cảnh báo xung đột dị ứng)

> **Ghi chú mâu thuẫn nguồn**: UC-135 (Report 3 dòng 3012, Exception EX1 "No Menu Published") trích dẫn "MSG-076" cho trường hợp không có menu công bố — nhưng theo `error_message.md` (nguồn MSG chính thức), MSG-076 thực tế là "Account temporarily locked due to too many failed login attempts" (khóa tài khoản, BR-002), không liên quan tới menu. Đây là lệch mã MSG giữa Report 3 (cũ) và `error_message.md` hiện hành — **không có MSG chính thức nào cho trạng thái "chưa có menu published"** trong bộ MSG hiện tại. Trạng thái Empty dưới đây dùng câu chữ mô tả tự do, không gán MSG code, và đánh dấu **[Giả định — cần team xác nhận/bổ sung MSG mới]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Weekly Days Tab Bar | Tabs | Mon-Fri, mặc định "Mon" | UC-135 bước 3 | |
| 2 | Standard Meal Item Card | Card | Tên món, nguyên liệu, tag "Lunch" | BR-069 | |
| 3 | Allergy Alert Meal Card | Card (viền đỏ, chỉ hiện nếu xung đột) | Tên món (đỏ), "Contains: [Allergen]", tag "Allergy Alert", link xem thay thế | BR-123 | MSG-051; tap → Special Meal Arrangements |
| 4 | View Nutrition Details Button | Button (primary, full-width) | → Nutrition History (biểu đồ dinh dưỡng tuần) | BR-062 | |
| 5 | Download PDF Button | Button (secondary) | Xuất bản in menu | UC-135 bước 6 | [Giả định vị trí UI cụ thể — cần xác nhận] |
| 6 | Child Selector (kế thừa từ Dashboard) | Dropdown | Đổi hồ sơ con → cập nhật cảnh báo dị ứng theo đúng con | BR-031 | UC-135 AF1 |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Không có menu được công bố cho tuần này" (block truy cập theo UC-135 EX1) — [Giả định — không có MSG code chính thức, xem ghi chú mâu thuẫn nguồn ở trên].
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Parent Dashboard ("Tap to view weekly details"). Ra: Nutrition Detail (tap món ăn), Special Meal Arrangements (tap Allergy Alert Card), Nutrition History.
**Bố cục**: AppBar (back) → Weekly Days Tab Bar → danh sách `Card` món ăn theo ngày → nút "View Nutrition Details" cố định đáy.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Weekly Menu, nền tảng Mobile, actor Parent.
Bố cục: AppBar + Weekly Days Tab Bar (Mon-Fri) + danh sách Card món ăn (đa số Card thường, 1 Card viền đỏ nếu xung đột dị ứng) + nút View Nutrition Details cố định đáy.
Thành phần bắt buộc: Weekly Days Tab Bar, Standard Meal Item Card, Allergy Alert Meal Card, View Nutrition Details Button.
Áp dụng Design System: Allergy Alert = danger/đỏ bắt buộc (BR-123, BR-123).
Trạng thái cần thể hiện: loading, empty (MSG-076), error (MSG-004/005), warning (MSG-051).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6051–6068 (Weekly Menu, field-by-field gốc), dòng 2999–3018 (UC-135 Normal/Alternative/Exception Flow).
- `business_rules.md` BR-031, BR-062, BR-069, BR-070, BR-075, BR-123; `error_message.md` MSG-051, MSG-076.
