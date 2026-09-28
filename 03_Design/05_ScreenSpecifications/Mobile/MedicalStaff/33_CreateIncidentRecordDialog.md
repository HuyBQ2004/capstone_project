# Screen Spec — Create Incident Record Dialog 🆕

**Actor**: Medical Staff | **UC liên quan**: UC-114 (mở rộng, xem `Report 3.md` dòng 4555–4569 — không có trong bản gốc, bổ sung 15/07/2026) | **Status**: Draft
**BR liên quan**: BR-146 (ngưỡng gộp), BR-154 (báo cáo sự cố chính thức — áp dụng sớm hơn ở giai đoạn triage)
**MSG liên quan**: MSG-023 (Error, tái sử dụng), MSG-100 (Success — lưu nháp), MSG-101 (Success — hoàn tất)

> **Mới 15/07/2026** — bản Mobile, dùng chung 100% business logic/field/BR/MSG với bản Web cùng tên. Bối cảnh và lý do đảo ngược quyết định 09/07/2026 (Mục 7.8): xem bản Web cùng tên và `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.3.

## Business Logic (dùng chung mọi nền tảng)

### Điểm vào
Từ tab "Chờ tiếp nhận" của `25_IncidentList.md` (Mobile): chế độ "Chọn" (long-press/icon multi-select) chọn 1+ Card `RPT-xxx` (`status = PENDING_TRIAGE`) → bấm nút nổi **"Tạo báo cáo sự cố"**.

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tiêu đề biên bản + thời gian/địa điểm lập | Text (đọc, tự động) | — | — | Giống bản Web field 1 |
| 2 | Người lập biên bản | Text (đọc, tự động) | — | — | = tài khoản Medical Staff đăng nhập |
| 3 | Các báo cáo nguồn (`RPT-xxx`) | Danh sách chip (đọc) | ≥1 | — | Chạm để xem lại "Xem chi tiết" |
| 4 | Khối context tự động (union) | Card (đọc) | — | BR-146 | Học sinh/Triệu chứng/Món ăn union, tham khảo |
| 5 | Nội dung biên bản | `Textarea` (full-width), bắt buộc | Không được để trống | BR-154 | Pre-fill gợi ý từ mô tả các RPT, toàn quyền sửa |
| 6 | Nút "Lưu bản nháp" | Button (secondary, cố định cuối màn) | Không validate rỗng | — | RPT liên quan → `status = DRAFTING`, badge "Đang soạn (nháp)" ở Card danh sách |
| 7 | Nút "Hoàn tất biên bản" | Button (primary, cố định cuối màn) | Disabled khi field 5 rỗng (MSG-023) | BR-154 | RPT liên quan → `status = DRAFTED`, chuyển sang tab "Biên bản chờ xử lý" |
| 8 | Nút "Hủy" (text link) | Button (ghost) | — | — | Không lưu |

### Trạng thái màn hình
- Loading: spinner trên nút đang bấm.
- Empty: không áp dụng.
- Error: MSG-023.
- Success: MSG-100 (nháp) / MSG-101 (hoàn tất) — Toast, quay lại danh sách.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ tab "Chờ tiếp nhận" (`25_IncidentList.md`, Mobile); Lưu bản nháp/Hoàn tất/Hủy → quay lại danh sách.
**Bố cục**: trang riêng full-screen (không phải bottom-sheet, do Textarea dài) — AppBar "Tạo báo cáo sự cố", context đọc xếp dọc, Textarea lớn, 3 nút xếp dọc cố định cuối màn: Hoàn tất biên bản (primary, trên cùng), Lưu bản nháp (secondary), Hủy (text link).

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Create Incident Record Dialog, nền tảng Mobile, actor Medical Staff.
Bố cục: trang full-screen, AppBar, context đọc (thời gian/người lập/RPT nguồn/union), Textarea lớn "Nội dung biên bản",
3 nút cố định cuối màn: Hoàn tất biên bản (primary) | Lưu bản nháp (secondary) | Hủy (text link).
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error, success (toast).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `00_Policies/specialreport2173_1.md`.
- `business_rules.md` BR-146, BR-154, Changelog v1.8.
- `error_message.md` MSG-023, MSG-100, MSG-101.
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 7.8, Mục 10.3.

**Ghi chú màn hình dùng chung**: giống bản Web — cùng field/logic, chỉ khác bố cục (trang riêng thay vì Dialog nổi).
