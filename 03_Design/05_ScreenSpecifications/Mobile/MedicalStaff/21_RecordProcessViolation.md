# Screen Spec — Record Process Violation (UC-178) — `(Deprecated 11/07/2026)`

> **TRẠNG THÁI: DEPRECATED.** UC-178 - Record Process Violation không còn là màn hình riêng biệt. Dialog nhập liệu đã **gộp vào `22_RecordStaffViolation.md`** (dialog "Lập Biên bản Vi phạm" hợp nhất, mã UC sống sót = **UC-179**). File này **giữ lại làm tham chiếu lịch sử** (giữ nguyên số hiệu UC-178, không tái sử dụng cho màn hình khác) — không mô tả UI thật nữa. Nội dung giống hệt bản Web (`Web/MedicalStaff/21_RecordProcessViolation.md`).

## Vì sao deprecate

Dialog "Lập biên bản vi phạm quy trình" (UC-178) và "Lập biên bản vi phạm nhân viên" (UC-179) gần như giống hệt nhau, trong khi mẫu biên bản giấy thực tế (`00_Policies/specialreport2173_1.md`) không hề phân biệt 2 loại vi phạm. Quyết định (07/07/2026, chốt cuối 11/07/2026): **giữ tách BR-183/BR-184 ở tầng dữ liệu**, nhưng **gộp UI thành 1 dialog duy nhất**, không có toggle chọn loại vi phạm.

## Cơ chế thay thế

Khi có 1 `NonConformanceRecord` cần "nâng cấp" thành Biên bản Vi phạm theo BR-183, Medical Staff/Service Caterers mở **cùng 1 dialog** ở `22_RecordStaffViolation.md`, để trống field "Nhân viên liên quan" — hệ thống tự nhận diện đây là vi phạm quy trình thuần túy. Toàn bộ field, validation, luồng xuất PDF, gate chặn cứng `signedRecordPhotoUrl` — xem đầy đủ tại `22_RecordStaffViolation.md`.

## Đối chiếu số hiệu

| Trước (2 dialog riêng) | Sau (1 dialog gộp, 11/07/2026) |
| --- | --- |
| UC-178 Record Process Violation (màn riêng) | Deprecated — dùng chung `22_RecordStaffViolation.md`, phân biệt qua field "Nhân viên liên quan" để trống |
| UC-179 Record Staff Violation (màn riêng) | **Mã sống sót** — `22_RecordStaffViolation.md` |
| BR-183 (vi phạm quy trình/sản phẩm) | Giữ nguyên ở tầng dữ liệu, không đổi |
| BR-184 (vi phạm nhân viên) | Giữ nguyên ở tầng dữ liệu, không đổi |

## Nguồn dữ liệu
- Report 3.md, dòng 3663–3683 (UC-178 — nội dung gốc, nay chỉ tham khảo lịch sử).
- `review/medical_staff/review_screen_medical_staff.md` Mục 10.
- `22_RecordStaffViolation.md` (đặc tả đầy đủ, đang dùng thật).
