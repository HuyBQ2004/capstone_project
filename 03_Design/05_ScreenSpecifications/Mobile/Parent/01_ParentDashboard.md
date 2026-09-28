# Screen Spec — Parent Dashboard

**Actor**: Parent | **UC liên quan**: — (entry point, không gắn UC cụ thể) | **Status**: Draft
**BR liên quan**: BR-031 (xét duyệt thông tin sức khỏe), BR-123 (cảnh báo dị ứng), BR-138 (cảnh báo quan trọng bỏ qua duyệt)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú về BR-040**: một số UC liên quan Parent trong Report 3 (UC-050, UC-093, UC-135, UC-141) trích dẫn "BR-040 (giới hạn tối đa 5 học sinh/phụ huynh)" — nhưng theo `business_rules.md` (nguồn BR chính thức đã chốt, xem `00_UI_Design_Documentation_Plan.md` mục 2.4), BR-040 thực tế là "Quy trình đăng ký nhà cung cấp", không liên quan giới hạn số con/phụ huynh. Đây là lệch số ID giữa Report 3 (cũ) và business_rules.md (đã duyệt) — không có BR nào trong business_rules.md hiện hành quy định giới hạn số học sinh/phụ huynh. **[Giả định — cần team xác nhận: giữ giới hạn 5 học sinh/phụ huynh theo UC gốc hay bỏ giới hạn cho tới khi có BR chính thức]**. Toàn bộ Screen Spec Parent trong bộ này không trích BR-040 cho quy tắc giới hạn số con.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Child Selector Dropdown | Dropdown | "Child: [Tên học sinh] (Lớp...)" | BR-031 | Chuyển đổi hồ sơ con — xem [Giả định] về giới hạn số lượng ở trên |
| 2 | Notification Bell Icon | Icon button | Mở Notification Center | — | |
| 3 | Allergy Threat Alert Box | Alert (danger, chỉ hiện khi có xung đột) | "Menu Allergy Threat Detected!" | BR-123 | Tap → Special Meal Arrangements |
| 4 | Today's Lunch Menu Card | Card | Món ăn hôm nay + link "view weekly details" | BR-070 | → Weekly Menu |
| 5 | Health Hub Navigation Tile | Button | → Student Health Information | — | |
| 6 | School Meals Navigation Tile | Button | → School Meals Dashboard | — | |
| 7 | Meal Feedback Navigation Tile | Button | → Feedback Center | — | |
| 8 | Meal History Navigation Tile | Button | → Meal History | — | |
| 9 | Report Urgent Safety Incident Button | Button (danger, dashed border, full-width) | → Report Food Safety Concern | BR-146, BR-138 | Bypass duyệt (BR-138) |
| 10 | Bottom Navigation Bar | Nav | 4 tab: Home (active), Meals, Health, Profile | — | |

### Trạng thái màn hình
- Loading: `Shimmer` toàn bộ card.
- Empty: nếu phụ huynh chưa liên kết học sinh nào → "Chưa liên kết hồ sơ học sinh" + hướng dẫn nhập mã học sinh (theo UC-135 EX2).
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Mobile
**Entry/Exit points**: Điểm vào sau Common Entry Flow (Login) — xem `05_Parent_Screen_Flow.md`. Điều hướng đi: Weekly Menu, Student Health Hub, School Meals Dashboard, Feedback Center, Meal History, Report Safety Issue, Notifications.
**Bố cục**: AppBar (Child Selector trái, Notification Bell phải) → Allergy Threat Alert Box (nếu có) → Today's Lunch Menu Card → lưới 4 Navigation Tile (2x2) → Report Urgent Safety Incident Button → `BottomNavigationBar` 4 mục.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Parent Dashboard, nền tảng Mobile, actor Parent.
Bố cục: AppBar (Child Selector + Notification Bell) + Allergy Threat Alert (đỏ, có điều kiện) + Today's Lunch Menu Card + lưới 4 Navigation Tile 2x2 + nút Report Urgent Safety Incident (viền đỏ nét đứt) + BottomNavigationBar 4 mục.
Thành phần bắt buộc: Child Selector Dropdown, Notification Bell Icon, Allergy Threat Alert Box, Today's Lunch Menu Card, Health Hub/School Meals/Meal Feedback/Meal History Navigation Tile, Report Urgent Safety Incident Button.
Áp dụng Design System: Allergy Threat = danger (BR-123), nút báo cáo khẩn = danger viền nét đứt.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5952–5971 (Parent Dashboard, field-by-field gốc).
- `03_Design/04_ScreenFlow/05_Parent_Screen_Flow.md`, `10_Parent_Screen_Flow_Web.md`.
- `business_rules.md` BR-031, BR-123, BR-138, BR-146.
- `traceability_matrix.md` §1.6 UC-135, §1.9 UC-055/UC-133/UC-141/UC-142, §1.11 UC-073.
