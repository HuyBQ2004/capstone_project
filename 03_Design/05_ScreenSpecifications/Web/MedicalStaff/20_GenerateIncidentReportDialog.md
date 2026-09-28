# Screen Spec — Generate Incident Report Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-033 (Report 3 §3.2, dòng 4401–4415) | **Status**: Draft
**BR liên quan**: BR-154 (báo cáo sự cố chính thức)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error), MSG-046 (Success)

> **Cập nhật 15/07/2026 — trình bày hợp nhất với `19_RecordCorrectiveActionsReportScreen.md`**: từ `26_IncidentDetail.md`, nút riêng "Step 3: Submit the incident report" nay gộp vào điểm vào chung **"Hoàn tất sự cố"** cùng với UC-168 (`19_RecordCorrectiveActionsReportScreen.md`). Nội dung màn này trở thành **Khối B** (soạn báo cáo chính thức), chỉ mở khoá sau khi Khối A (checklist hồi phục học sinh, UC-168) submit thành công. Giữ nguyên UC-033 là 1 use case riêng, không đổi field/BR — chỉ đổi cách trình bày. Xem `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.7.
> **Cập nhật 15/07/2026 (đợt 2 — Mục 11.4.2/11.9.1)**: (1) bổ sung mục đọc "Biên bản sự cố ban đầu (tham chiếu)" (field 4b) — báo cáo cuối trước đây chỉ có Clinical notes/Lab results/Root cause/Remedial, chưa tham chiếu ngược về biên bản triage ban đầu; (2) field "Root cause determination" (field 8) **không nhập lại** — tự **kế thừa "Kết luận chính thức"** từ `17_InvestigationWorkspace.md` (root cause nay là 1 bản ghi trạng thái Sơ bộ → Chính thức, Mục 11.9.1), chỉ hiển thị đọc/cho phép rà soát, không phải điểm nhập thứ 2.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Incident code | Text (đọc) | — | — | Report 3 §3.3.2.3.8 |
| 2 | Date created | Text (đọc) | — | — | |
| 3 | Severity level | Text/Badge (đọc) | — | BR-147 | |
| 4 | Affected student list/symptoms/ngày khởi phát/suspected dishes/ingredient batches (auto-populate) | Text (đọc) | — | BR-154 | |
| 4b 🆕 | Biên bản sự cố ban đầu (tham chiếu) | Panel/link (đọc) | — | BR-154 | **Mới 15/07/2026 (Mục 11.4.2)** — mục tham chiếu ngược tới biên bản triage đã lập ở `33_CreateIncidentRecordDialog.md` (`IncidentRecordDraft.content`), đưa vào báo cáo cuối để lưu vết đầy đủ chuỗi từ triage → điều tra → báo cáo chính thức. Cùng nguồn với panel "Biên bản gốc" ở `26_IncidentDetail.md` field 4b |
| 5 | Food sample retained | Text (đọc) | — | BR-114 | |
| 6 | Clinical notes | Textarea | Bắt buộc | BR-154 | **Cập nhật 11/07/2026**: pre-fill sẵn từ "Tường trình ban đầu" (`26_IncidentDetail.md` field 5) khi mở màn này lần đầu — Medical Staff vẫn sửa/bổ sung được, không khoá cứng. Chi tiết: `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 9.12 |
| 7 | Laboratory sample test results | Textarea/File | Bắt buộc nếu có | BR-154 | |
| 8 | Root cause determination | Text (đọc, **kế thừa**) | — | BR-150, BR-154 | **Sửa 15/07/2026 (Mục 11.9.1)** — **không nhập lại**; tự kế thừa "Kết luận chính thức" từ `17_InvestigationWorkspace.md` field 10 (root cause 1 bản ghi Sơ bộ → Chính thức). Cho phép rà soát/đọc, không phải điểm nhập độc lập thứ 2 (khắc phục trùng lặp Mục 5.3) |
| 9 | Remedial measures (tóm tắt) | Text (đọc) | — | BR-152 | |
| 10 | Nút "Export & Submit Incident Reports" | Button (primary) | Disabled khi thiếu Clinical notes/Root cause | BR-154 | |
| 11 | Nút "Come back" | Button | — | — | Quay lại Investigation Workspace |

### Trạng thái màn hình
- Loading: spinner khi biên soạn PDF + gửi.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005, MSG-023.
- Success: MSG-046 (Toast), trạng thái "Investigation Completed", PDF gửi School Administration.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Web); thành công → chuyển School Administration Incident Dashboard.
**Bố cục**: `Dialog`/trang con — Incident Report Card tóm tắt, form Clinical notes/Lab results/Root cause, nút "Export & Submit Incident Reports" cuối form.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Mobile); thành công → School Administration Incident Dashboard.
**Bố cục**: Report 3 §3.3.2.3.8 — trang riêng full-screen, Incident Report Card, nút "Export & Submit Incident Reports" cố định cuối màn.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Generate Incident Report Dialog, nền tảng Web, actor Medical Staff.
Bố cục: Dialog/trang con, Incident Report Card, form Clinical notes/Lab results/Root cause,
nút Export & Submit Incident Reports.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error, success (MSG-046).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4401–4415 (UC-033), dòng 5845–5867 (§3.3.2.3.8).
- business_rules.md (BR-114, BR-150, BR-152, BR-154).
- error_message.md (MSG-004, MSG-005, MSG-023, MSG-046).
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 11.4.2 (tham chiếu Biên bản sự cố ban đầu), Mục 11.9.1 (root cause kế thừa — đợt 2 15/07/2026).
