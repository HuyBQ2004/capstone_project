# Screen Spec — Record Process Violation (UC-178) — `(Deprecated 11/07/2026)`

> **TRẠNG THÁI: DEPRECATED.** UC-178 - Record Process Violation không còn là màn hình riêng biệt. Dialog nhập liệu đã **gộp vào `22_RecordStaffViolation.md`** (dialog "Lập Biên bản Vi phạm" hợp nhất, mã UC sống sót = **UC-179**). File này **giữ lại làm tham chiếu lịch sử** (giữ nguyên số hiệu UC-178, không tái sử dụng cho màn hình khác) — không mô tả UI thật nữa.

## Vì sao deprecate

Người dùng chỉ ra dialog "Lập biên bản vi phạm quy trình" (UC-178) và "Lập biên bản vi phạm nhân viên" (UC-179) gần như giống hệt nhau, trong khi mẫu biên bản giấy thực tế (`00_Policies/specialreport2173_1.md`) không hề phân biệt 2 loại vi phạm — cùng 1 cấu trúc 3 bên, cùng 1 khối nội dung tự do, cùng 1 khối chữ ký. Quyết định (07/07/2026, chốt cuối 11/07/2026): **giữ tách BR-183 (vi phạm quy trình/sản phẩm) và BR-184 (vi phạm nhân viên) ở tầng dữ liệu** — vì nguồn gốc phát sinh khác nhau (BR-183 từ `NonConformanceRecord` tự động/audit; BR-184 từ nhập tay độc lập) và thẩm quyền xử lý thật khác nhau (nhân viên bếp/catering thuộc biên chế Nhà thầu, trường chỉ "đề xuất" kỷ luật chứ không tự quyết) — nhưng **gộp UI thành 1 dialog duy nhất**, không có toggle chọn loại vi phạm.

## Cơ chế thay thế

Khi có 1 `NonConformanceRecord` cần "nâng cấp" thành Biên bản Vi phạm theo BR-183 (ví dụ: 1 bước kiểm thực 3 bước bị đánh Không đạt), Medical Staff/Service Caterers mở **cùng 1 dialog** ở `22_RecordStaffViolation.md`, để trống field "Nhân viên liên quan" — hệ thống tự nhận diện đây là vi phạm quy trình thuần túy (chỉ áp BR-183, không kích hoạt BR-184/185/187). Toàn bộ field, validation, luồng xuất PDF, gate chặn cứng `signedRecordPhotoUrl` — xem đầy đủ tại `22_RecordStaffViolation.md`.

## Đối chiếu số hiệu

| Trước (2 dialog riêng) | Sau (1 dialog gộp, 11/07/2026) |
| --- | --- |
| UC-178 Record Process Violation (màn riêng) | Deprecated — dùng chung `22_RecordStaffViolation.md`, phân biệt qua field "Nhân viên liên quan" để trống |
| UC-179 Record Staff Violation (màn riêng) | **Mã sống sót** — `22_RecordStaffViolation.md` |
| BR-183 (vi phạm quy trình/sản phẩm) | Giữ nguyên ở tầng dữ liệu, không đổi |
| BR-184 (vi phạm nhân viên) | Giữ nguyên ở tầng dữ liệu, không đổi |

## Nguồn dữ liệu
- Report 3.md, dòng 3663–3683 (UC-178 Record Process Violation — nội dung gốc, nay chỉ tham khảo lịch sử).
- `review/medical_staff/review_screen_medical_staff.md` Mục 10 (toàn bộ lịch sử quyết định gộp dialog).
- `22_RecordStaffViolation.md` (đặc tả đầy đủ, đang dùng thật).
