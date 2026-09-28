# Screen Spec — Violation Registry

**Actor**: Medical Staff (màn hình dùng chung với School Administration — Primary Actor theo UC-181 là School Administration, Medical Staff là Secondary Actor; file này viết theo góc nhìn Medical Staff, quyền hạn xem có thể hẹp hơn bản School Administration) | **UC liên quan**: UC-181 - View Violation Registry (Report 3 §3.2, dòng 4683–4703 — **đã tìm thấy bảng đặc tả đầy đủ khi review chéo, đính chính lại ghi chú "chưa có UC" trước đây**) | **Status**: Draft
**BR liên quan**: BR-189 (danh sách tổng hợp mọi loại vi phạm — quy trình, nhân viên, đối tác — theo actor/mức độ/trạng thái), BR-187 (cảnh báo nghi ngờ tái diễn), BR-214 (báo cáo tổng hợp vi phạm theo kỳ), BR-178 (định dạng xuất báo cáo)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

> **Sửa 13/07/2026**: bổ sung field 4 "Filter theo khoảng ngày" và field 9 "Nút Export" — khoảng trống phát hiện khi thiết kế BR-214 (Send Violation Summary Report, UC-189). Xem bản Web + `review_screen_medical_staff.md` Mục 17.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo loại vi phạm (quy trình/nhân viên/đối tác) | Select/Chip filter | — | BR-189 | |
| 2 | Filter theo mức độ (LOW/MEDIUM/HIGH) | Select | — | BR-186 | |
| 3 | Filter theo trạng thái (Pending/Approved/Closed) | Select | — | BR-186 | |
| 4 | Filter theo khoảng ngày | Date range picker | — | — | **Mới 13/07/2026** |
| 5 | Danh sách vi phạm | List/Table | — | BR-189 | Cột: Loại vi phạm, người/đối tượng liên quan, mức độ, trạng thái, ngày lập |
| 6 | Status/Severity Badge (mỗi item) | Badge màu | Danger (HIGH)/Warning (MEDIUM)/Secondary (LOW) | BR-186 | |
| 7 | Cảnh báo nghi ngờ tái diễn (nếu có) | Badge/Icon cảnh báo | — | BR-187 | MSG-088 |
| 8 | Chọn 1 vi phạm → xem chi tiết | Interaction | — | — | Điều hướng lại `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md`/`24_RecordViolationResolution.md` tương ứng (chế độ xem) |
| 9 | Nút "Export" | Button | — | BR-178 | **Mới 13/07/2026** — xuất CSV/PDF đúng theo filter đang áp dụng, không xuất toàn bộ dữ liệu |

### Trạng thái màn hình
- Loading: `Shimmer`/`Skeleton` cho danh sách.
- Empty: "Chưa có vi phạm nào được ghi nhận" + illustration.
- Error: MSG-004, MSG-005.
- Success: Danh sách hiển thị đầy đủ vi phạm trong phạm vi Medical Staff được xem, cảnh báo tái diễn (MSG-088) hiện dưới dạng badge/icon cạnh item liên quan.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Dashboard (nếu có shortcut riêng, hoặc từ menu điều hướng chung theo `03_Medical_Staff_Screen_Flow.md`); chọn item → xem chi tiết vi phạm tương ứng.
**Bố cục**: 1 cột, filter dạng `Chip`/`DropdownButton` (loại/mức độ/trạng thái/khoảng ngày) phía trên, danh sách `Card` cuộn dọc, mỗi Card có Severity Badge + cảnh báo tái diễn (nếu có) góc phải, nút "Export" nổi AppBar.

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Vi phạm" (`08_Medical_Staff_Screen_Flow_Web.md`); chọn dòng → xem chi tiết.
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — thanh filter (loại/mức độ/trạng thái/khoảng ngày) phía trên `Table`, nút "Export" góc trên phải, `Table` hiển thị danh sách kèm phân trang.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Violation Registry, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: filter (loại vi phạm, mức độ, trạng thái, khoảng ngày), danh sách vi phạm với
Severity Badge, cảnh báo nghi ngờ tái diễn (nếu có), nút Export.
Áp dụng Design System: danger #B91C1C (HIGH), warning #B45309 (MEDIUM), secondary #64748B (LOW).
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- business_rules.md §18 (BR-186, BR-187, BR-189), §16 (BR-178, BR-214 — mới 13/07/2026).
- error_message.md (MSG-004, MSG-005, MSG-088).
- `02_Requirements/Violation_Management_Proposal.md`.

**Đã hợp nhất (mục 6, `00_Team_Work_Assignment_Plan.md`)**: đối chiếu với `Web/SchoolAdministration/47_ViolationRegistry.md` — field hiển thị dữ liệu khớp nhau hợp lý, không phát hiện field còn thiếu. **[Giả định — cần team xác nhận]**: BR-189 không nêu rõ ràng buộc phạm vi dữ liệu Medical Staff được xem trong Registry (toàn bộ hay chỉ vi phạm tự lập) — cần team xác nhận trước khi sang bước `06_Wireframes/`.
