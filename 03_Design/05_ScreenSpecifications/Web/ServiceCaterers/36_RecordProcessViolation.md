# Screen Spec — Record Process Violation (UC-178) — `(Deprecated 11/07/2026)`

> **TRẠNG THÁI: DEPRECATED.** UC-178 - Record Process Violation không còn là màn hình riêng biệt. Dialog nhập liệu đã **gộp vào `Web/MedicalStaff/22_RecordStaffViolation.md`** (dialog "Lập Biên bản Vi phạm" hợp nhất, mã UC sống sót = **UC-179**), dùng chung bởi cả Medical Staff và Service Caterers. File này **giữ lại làm tham chiếu lịch sử** (giữ nguyên số hiệu UC-178, không tái sử dụng cho màn hình khác) — không mô tả UI thật nữa.

## Vì sao deprecate

Dialog "Lập biên bản vi phạm quy trình" (UC-178, dùng bởi Service Caterers khi 1 bước Three-Step Inspection thất bại) và "Lập biên bản vi phạm nhân viên" (UC-179, dùng bởi Medical Staff) gần như giống hệt nhau, trong khi mẫu biên bản giấy thực tế (`00_Policies/specialreport2173_1.md`) không hề phân biệt 2 loại vi phạm. Quyết định (07/07/2026, chốt cuối 11/07/2026): **giữ tách BR-183/BR-184 ở tầng dữ liệu**, nhưng **gộp UI thành 1 dialog duy nhất**, không có toggle chọn loại vi phạm.

## Cơ chế thay thế

Khi 1 bước Three-Step Inspection thất bại (`InspectionStep.fail()`) hoặc Service Caterers tự phát hiện bất thường ngoài luồng inspection, Service Caterers mở **cùng 1 dialog** ở `Web/MedicalStaff/22_RecordStaffViolation.md`, để trống field "Nhân viên liên quan" (`staffName`) — hệ thống tự nhận diện đây là vi phạm quy trình thuần túy (chỉ áp BR-183). Toàn bộ field, validation, luồng xuất PDF, gate chặn cứng `signedRecordPhotoUrl` — xem đầy đủ tại file đó. Điểm vào/ra trên nền tảng Web của Service Caterers xem `03_Design/04_ScreenFlow/09_Service_Caterers_Screen_Flow_Web.md`.

**Lưu ý field không còn trong dialog gộp**: field "Chọn Nhân chứng (Witness)" (bắt buộc khi Severity ≥ Medium) trong bản UC-178 gốc **đã bị bỏ hẳn** — hệ thống không track thông tin người chứng kiến dưới bất kỳ hình thức nào (quyết định 11/07/2026). PDF xuất ra vẫn giữ cột chữ ký thứ 3 "Người chứng kiến" (khớp mẫu giấy gốc) nhưng là khoảng trống tĩnh, không gắn field dữ liệu.

## Đối chiếu số hiệu

| Trước (2 dialog riêng) | Sau (1 dialog gộp, 11/07/2026) |
| --- | --- |
| UC-178 Record Process Violation (màn riêng, actor Service Caterers) | Deprecated — dùng chung `Web/MedicalStaff/22_RecordStaffViolation.md`, phân biệt qua field "Nhân viên liên quan" để trống |
| UC-179 Record Staff Violation (màn riêng, actor Medical Staff) | **Mã sống sót** — `Web/MedicalStaff/22_RecordStaffViolation.md` |
| BR-183 (vi phạm quy trình/sản phẩm) | Giữ nguyên ở tầng dữ liệu, không đổi |
| BR-184 (vi phạm nhân viên) | Giữ nguyên ở tầng dữ liệu, không đổi |
| Field "Chọn Nhân chứng (Witness)" | Bỏ hẳn khỏi form nhập liệu — PDF giữ cột trống tĩnh |

## Nguồn dữ liệu
- Report 3.md, dòng 3663–3683 (UC-178 — nội dung gốc, nay chỉ tham khảo lịch sử).
- `review/medical_staff/review_screen_medical_staff.md` Mục 10.
- `Web/MedicalStaff/22_RecordStaffViolation.md` (đặc tả đầy đủ, đang dùng thật).
