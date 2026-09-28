# Screen Spec — Record Corrective Actions Report Screen

**Actor**: Medical Staff | **UC liên quan**: UC-168 (Report 3 §3.2, dòng 4467–4481) | **Status**: Draft
**BR liên quan**: BR-152 (CAPA), BR-153 (phê duyệt hành động khắc phục — School Administration), BR-155 (đóng sự cố — cần Medical Staff xác nhận hồi phục 100%)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-046 (Success)

> **Cập nhật 15/07/2026 — trình bày hợp nhất với `20_GenerateIncidentReportDialog.md`**: từ `26_IncidentDetail.md`, 2 nút tách rời "Validate CAPA đã Approved" (UC-168, màn này) và "Step 3: Generate Incident Report" (UC-033) nay gộp thành **1 điểm vào duy nhất "Hoàn tất sự cố"**. Màn hợp nhất gồm 2 khối tuần tự: **Khối A = nguyên nội dung màn này** (field 1-5 dưới đây, checklist hồi phục học sinh); **Khối B = nguyên nội dung `20_GenerateIncidentReportDialog.md`** (Clinical notes/Lab results/Root cause/Export & Submit) — Khối B chỉ mở khoá sau khi Khối A submit thành công (đúng thứ tự nghiệp vụ: phải xác nhận khắc phục xong mới soạn báo cáo đóng case). **Giữ nguyên UC-168 là 1 use case riêng, không đổi field/BR** — chỉ đổi cách trình bày (không còn điều hướng sang trang/dialog riêng). Xem `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.7.
> *(Ghi nhận tiện thể, không thuộc phạm vi sửa lần này)*: `20_GenerateIncidentReportDialog.md` hiện trích dẫn MSG-046 làm success message, nhưng nội dung MSG-046 thực tế là "Meal feedback submitted successfully" (thuộc module Feedback, không liên quan Incident) — có khả năng là lỗi trích dẫn từ trước, nên rà soát riêng khi có dịp.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách sự cố có CAPA đã Approved chờ xác nhận y tế | Table | Chỉ hiển thị incident đã qua UC-005 | BR-153 | UC-168 bước 2 |
| 2 | Chọn 1 dòng → xem chi tiết | Interaction | — | — | Log sự cố, chi tiết CAPA đã duyệt, student clinical tracking log |
| 3 | Ghi chú kiểm tra bếp | Textarea | Bắt buộc | BR-152 | |
| 4 | Xác nhận học sinh hồi phục 100% | Checkbox theo từng học sinh | Bắt buộc TẤT CẢ = đã hồi phục | BR-155 | EX1: chặn nếu còn triệu chứng |
| 5 | Nút "Submit Validation" | Button (primary) | Disabled khi chưa đủ điều kiện | BR-152, BR-155 | |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải danh sách/chi tiết.
- Empty: "Không có sự cố nào đang chờ xác nhận y tế" + illustration.
- Error: MSG-004 (EX1), MSG-005.
- Success: MSG-046 (Toast), trạng thái "Validated - Ready for Closure".

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Web, nhánh validate CAPA).
**Bố cục**: `Table` danh sách sự cố chờ xác nhận, chọn dòng mở `Sheet` bên phải (context CAPA + checklist hồi phục + textarea + nút Submit).

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Mobile, nhánh validate CAPA).
**Bố cục**: danh sách `Card` cuộn dọc, chọn item mở trang chi tiết validation.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Record Corrective Actions Report, nền tảng Web, actor Medical Staff.
Bố cục: Table danh sách sự cố chờ xác nhận, Sheet chi tiết CAPA + checklist hồi phục + nút Submit Validation.
Áp dụng Design System: success #15803D khi đủ điều kiện, danger #B91C1C khi còn triệu chứng.
Trạng thái cần thể hiện: loading, empty, error (MSG-004 EX1), success (MSG-046).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4467–4481 (UC-168).
- business_rules.md (BR-152, BR-153, BR-155).
- error_message.md (MSG-004, MSG-005, MSG-046).
