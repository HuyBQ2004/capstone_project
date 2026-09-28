# Screen Spec — Violation Registry

**Actor**: Medical Staff (màn hình dùng chung với School Administration — Primary Actor theo UC-181 là School Administration, Medical Staff là Secondary Actor; file này viết theo góc nhìn Medical Staff, phạm vi xem có thể hẹp hơn bản School Administration) | **UC liên quan**: UC-181 - View Violation Registry (Report 3 §3.2, dòng 4683–4703 — **đã tìm thấy bảng đặc tả đầy đủ khi review chéo, đính chính lại ghi chú "chưa có UC" trước đây**) | **Status**: Draft
**BR liên quan**: BR-189 (danh sách tổng hợp mọi loại vi phạm), BR-187 (cảnh báo nghi ngờ tái diễn), BR-214 (báo cáo tổng hợp vi phạm theo kỳ), BR-178 (định dạng xuất báo cáo)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

> **Sửa 13/07/2026**: bổ sung field 4 "Filter theo khoảng ngày" và field 9 "Nút Export" — khoảng trống phát hiện khi thiết kế BR-214 (Send Violation Summary Report, UC-189): màn này trước đây chưa có Export, khác với các màn danh sách khác đã có sẵn (`25_IncidentList.md`, `15_ViewFoodSafetyReportsScreen.md`). Export tôn trọng đúng filter đang áp dụng (đồng bộ nguyên tắc Nhóm D, xem `review_screen_medical_staff.md` Mục 11). Xem Mục 17.
> **Sửa 15/07/2026 (đợt 2 — Mục 11.5.4)**: bổ sung cột **"Sự cố liên quan"** (hiển thị `INC-xxx` nếu vi phạm có `relatedIncidentId`) và **filter theo Sự cố liên quan** — hệ quả của quyết định liên kết chéo Incident ↔ Violation (Mục 11.5). Cho phép Medical Staff/School Admin lọc ra các vi phạm phát sinh từ 1 sự cố cụ thể.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo loại vi phạm (quy trình/nhân viên/đối tác) | Select | — | BR-189 | |
| 2 | Filter theo mức độ (LOW/MEDIUM/HIGH) | Select | — | BR-186 | |
| 3 | Filter theo trạng thái (Pending/Approved/Closed) | Select | — | BR-186 | |
| 4 | Filter theo khoảng ngày | Date range picker | — | — | **Mới 13/07/2026** |
| 4b 🆕 | Filter theo Sự cố liên quan | Select/Search (`INC-xxx`) | — | BR-185 | **Mới 15/07/2026 (Mục 11.5.4)** — lọc các vi phạm có `relatedIncidentId` khớp 1 sự cố cụ thể |
| 5 | Bảng danh sách vi phạm | Table | — | BR-189 | Cột: Loại vi phạm, người/đối tượng liên quan, mức độ, trạng thái, ngày lập, **Sự cố liên quan** (`INC-xxx` nếu có `relatedIncidentId`, link sang `26_IncidentDetail.md` — mới 15/07/2026, Mục 11.5.4) |
| 6 | Severity Badge (mỗi dòng) | Badge màu | Danger (HIGH)/Warning (MEDIUM)/Secondary (LOW) | BR-186 | |
| 7 | Cảnh báo nghi ngờ tái diễn (nếu có) | Badge/Icon | — | BR-187 | MSG-088 |
| 8 | Chọn 1 dòng → xem chi tiết | Interaction | — | — | Điều hướng chế độ xem tương ứng |
| 9 | Nút "Export" | Button | — | BR-178 | **Mới 13/07/2026** — xuất CSV/PDF đúng theo filter đang áp dụng (loại/mức độ/trạng thái/khoảng ngày), không xuất toàn bộ dữ liệu |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng.
- Empty: "Chưa có vi phạm nào được ghi nhận" + illustration.
- Error: MSG-004, MSG-005.
- Success: Bảng/danh sách hiển thị đầy đủ vi phạm trong phạm vi Medical Staff được xem, cảnh báo tái diễn (MSG-088) hiện dưới dạng badge/icon cạnh dòng liên quan.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Vi phạm"; chọn dòng → xem chi tiết.
**Bố cục**: thanh filter (loại/mức độ/trạng thái/khoảng ngày) phía trên `Table`, nút "Export" góc trên phải, phân trang dưới bảng.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ menu điều hướng chung (Mobile); chọn item → xem chi tiết.
**Bố cục**: 1 cột, filter `Chip`/`DropdownButton`, danh sách `Card` cuộn dọc, nút "Export" nổi AppBar.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Violation Registry, nền tảng Web, actor Medical Staff.
Bố cục: thanh filter (loại/mức độ/trạng thái/khoảng ngày/sự cố liên quan), Table danh sách vi phạm với Severity Badge
+ cột "Sự cố liên quan" (INC-xxx), nút Export góc trên phải.
Áp dụng Design System: danger #B91C1C (HIGH), warning #B45309 (MEDIUM), secondary #64748B (LOW).
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4683–4703 (Table 3-162, UC-181 View Violation Registry — Normal Flow đầy đủ, Primary Actor School Administration, Secondary Medical Staff).
- business_rules.md §18 (BR-186, BR-187, BR-189), §16 (BR-178, BR-214 — mới 13/07/2026).
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 11.5.4 (cột + filter Sự cố liên quan — đợt 2 15/07/2026).
- error_message.md (MSG-004, MSG-005, MSG-088).

**Đã hợp nhất (mục 6, `00_Team_Work_Assignment_Plan.md`)**: đối chiếu với `Web/SchoolAdministration/47_ViolationRegistry.md` — field hiển thị dữ liệu khớp nhau hợp lý (filter Type/Severity/Status, bảng danh sách, badge severity, cảnh báo tái diễn). Không phát hiện field còn thiếu. **[Giả định — cần team xác nhận]**: BR-189 không nêu rõ ràng buộc phạm vi dữ liệu Medical Staff được xem trong Registry (toàn bộ hay chỉ vi phạm tự lập) — cần team xác nhận trước khi sang bước `06_Wireframes/`.
