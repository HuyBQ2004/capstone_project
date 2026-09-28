# Screen Spec — Generate Incident Report Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-033 (Report 3 §3.2, dòng 4401–4415) | **Status**: Draft
**BR liên quan**: BR-154 (báo cáo sự cố chính thức, Medical Staff tạo để lưu hồ sơ/xuất báo cáo)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error), MSG-046 (Success)

> **Cập nhật 15/07/2026 — trình bày hợp nhất với `19_RecordCorrectiveActionsReportScreen.md`**: nút riêng "Step 3: Submit the incident report" ở `26_IncidentDetail.md` nay gộp vào điểm vào chung "Hoàn tất sự cố" cùng UC-168. Nội dung màn này là Khối B, mở khoá sau khi Khối A (UC-168) xong. Giữ nguyên UC-033, không đổi field/BR. Xem bản Web cùng tên và `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.7.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Incident code | Text (đọc) | — | — | Report 3 §3.3.2.3.8 |
| 2 | Date created | Text (đọc) | — | — | |
| 3 | Severity level | Text/Badge (đọc) | — | BR-147 | Kế thừa từ `18_AssignIncidentSeverityDialog.md` |
| 4 | Affected student list, symptoms, ngày khởi phát, suspected dishes, ingredient batches (tự động điền) | Text (đọc) | Auto-populate | BR-154 | UC-033 bước 3 |
| 5 | Food sample retained (mã + trạng thái) | Text (đọc) | — | BR-114 | |
| 6 | Clinical notes | Textarea | Bắt buộc | BR-154 | UC-033 bước 4. **Cập nhật 11/07/2026**: pre-fill sẵn từ "Tường trình ban đầu" (`26_IncidentDetail.md` field 5) khi mở màn này lần đầu — Medical Staff vẫn sửa/bổ sung được, không khoá cứng. Chi tiết: `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 9.12 |
| 7 | Laboratory sample test results | Textarea/File | Bắt buộc nếu có | BR-154 | |
| 8 | Root cause determination (Conclusion) | Textarea | Bắt buộc | BR-150, BR-154 | |
| 9 | Remedial measures (tóm tắt) | Text (đọc, kế thừa từ Investigation Workspace) | — | BR-152 | |
| 10 | Nút "Export & Submit Incident Reports" | Button (primary) | Disabled khi thiếu Clinical notes/Root cause | BR-154 | Sinh PDF, gửi School Admin |
| 11 | Nút "Come back" | Button | — | — | Quay lại Investigation Workspace |

### Trạng thái màn hình
- Loading: spinner khi hệ thống biên soạn PDF + gửi.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005, MSG-023.
- Success: MSG-046 (Toast), trạng thái sự cố chuyển "Investigation Completed", PDF được gửi tới School Administration (`Incident Dashboard`), quay lại `26_IncidentDetail.md`.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (nút "Step 3: Submit the incident report...") hoặc từ Investigation Workspace; thành công → chuyển tiếp School Administration Incident Dashboard (ngoài phạm vi actor Medical Staff, chỉ ghi nhận điểm điều hướng thoát).
**Bố cục**: Report 3 §3.3.2.3.8 — trang riêng full-screen, Incident Report Card tóm tắt trên cùng, System Info Text giải thích việc biên soạn PDF, nút "Export & Submit Incident Reports" cố định cuối màn, nút "Come back" trên AppBar.

## Platform Layout — Web
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Web); thành công → chuyển School Administration Incident Dashboard.
**Bố cục**: `Dialog`/trang con — Incident Report Card tóm tắt, form Clinical notes/Lab results/Root cause bên dưới, nút "Export & Submit Incident Reports" cuối form.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Generate Incident Report Dialog, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: Incident code, Date created, Severity level, danh sách học sinh/triệu chứng/món
nghi vấn (auto-populate), Food sample retained, Clinical notes, Laboratory test results, Root cause,
Remedial measures, nút Export & Submit Incident Reports.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error, success (MSG-046).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4401–4415 (UC-033), dòng 5845–5867 (§3.3.2.3.8).
- business_rules.md (BR-114, BR-150, BR-152, BR-154).
- error_message.md (MSG-004, MSG-005, MSG-023, MSG-046).
