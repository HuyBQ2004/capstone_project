# Screen Spec — Record Corrective Actions Report Screen

**Actor**: Medical Staff | **UC liên quan**: UC-168 (Report 3 §3.2, dòng 4467–4481) | **Status**: Draft
**BR liên quan**: BR-152 (CAPA), BR-153 (phê duyệt hành động khắc phục — School Administration), BR-155 (đóng sự cố — cần Medical Staff xác nhận hồi phục 100%)
**MSG liên quan**: MSG-004 (Error — dùng lại khi còn học sinh chưa hồi phục theo EX1), MSG-005 (Error), MSG-046 (Success)

> **Cập nhật 15/07/2026 — trình bày hợp nhất với `20_GenerateIncidentReportDialog.md`**: 2 nút tách rời ở `26_IncidentDetail.md` ("Validate CAPA đã Approved"/UC-168 và "Step 3: Generate Incident Report"/UC-033) nay gộp thành 1 điểm vào "Hoàn tất sự cố" — màn này trở thành Khối A (checklist hồi phục học sinh), Khối B là nguyên nội dung `20_GenerateIncidentReportDialog.md`, chỉ mở khoá sau khi Khối A xong. Giữ nguyên UC-168, không đổi field/BR. Xem bản Web cùng tên và `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.7.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách sự cố có CAPA đã Approved chờ xác nhận y tế | List/Table | Chỉ hiển thị incident đã qua UC-005 (School Admin approve CAPA) | BR-153 | UC-168 bước 2 |
| 2 | Chọn 1 sự cố → xem chi tiết | Interaction | — | — | Hiện log sự cố, chi tiết CAPA đã duyệt, student clinical tracking log |
| 3 | Ghi chú kiểm tra bếp (verification comments) | Textarea | Bắt buộc | BR-152 | UC-168 bước 5 |
| 4 | Xác nhận học sinh hồi phục 100% | Checkbox/Toggle theo từng học sinh | Bắt buộc TẤT CẢ học sinh = đã hồi phục mới được submit | BR-155 | EX1: còn 1 học sinh chưa hồi phục → chặn |
| 5 | Nút "Submit Validation" | Button (primary) | Disabled khi chưa đủ điều kiện (bước 4) | BR-152, BR-155 | Chuyển trạng thái "Validated - Ready for Closure" |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` khi tải danh sách/chi tiết.
- Empty: "Không có sự cố nào đang chờ xác nhận y tế" + illustration.
- Error: MSG-004 (EX1 — còn học sinh chưa hồi phục 100%, BR-155), MSG-005.
- Success: MSG-046 (Toast/SnackBar), trạng thái chuyển "Validated - Ready for Closure", quay lại danh sách.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (nhánh "validate CAPA đã approved").
**Bố cục**: trang riêng full-screen, danh sách sự cố dạng `Card` cuộn dọc, chọn 1 item mở trang chi tiết validation (context CAPA + checklist hồi phục học sinh + textarea + nút Submit cố định cuối màn).

## Platform Layout — Web
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Web, nhánh validate CAPA).
**Bố cục**: `Table` danh sách sự cố chờ xác nhận, chọn dòng mở `Sheet` bên phải chứa context CAPA + checklist hồi phục + textarea + nút Submit cuối Sheet.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Record Corrective Actions Report, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: danh sách sự cố chờ xác nhận y tế, chi tiết CAPA đã duyệt, ghi chú kiểm tra bếp,
checklist xác nhận hồi phục 100% từng học sinh, nút Submit Validation.
Áp dụng Design System: success #15803D khi đủ điều kiện, danger #B91C1C khi còn học sinh chưa hồi phục.
Trạng thái cần thể hiện: loading, empty, error (MSG-004 EX1 còn triệu chứng), success (MSG-046).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4467–4481 (UC-168).
- business_rules.md (BR-152, BR-153, BR-155).
- error_message.md (MSG-004, MSG-005, MSG-046).
