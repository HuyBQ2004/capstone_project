# Screen Spec — Assign Incident Severity Dialog

> **⚠️ DEPRECATED 15/07/2026** — không còn là màn hình riêng. UC-009 vẫn giữ nguyên số hiệu/BR-147/BR-148, nay gộp trình bày vào `26_IncidentDetail.md` (field 5-6). Xem bản Web cùng tên và `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.4. Giữ nguyên nội dung bên dưới để tham chiếu lịch sử — không dùng để code mới.

**Actor**: Medical Staff | **UC liên quan**: UC-009 (Report 3 §3.2, dòng 4335–4349) | **Status**: ~~Draft~~ Deprecated
**BR liên quan**: BR-147 (tiêu chí severity: Low/Medium/High), BR-148 (cảnh báo ngay lập tức High), BR-149 (cách ly lô hàng)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Reported symptoms (context, đọc) | Text | — | — | Report 3 §3.3.2.3.6, kế thừa từ Incident Detail |
| 2 | Affected student count (context, đọc) | Text | — | — | |
| 3 | Suspected dish information (context, đọc) | Text | — | — | |
| 4 | Severity level | RadioGroup/Select | Bắt buộc chọn: Low/Medium/High (BR-147) | BR-147 | Low = mild không cần khám; Medium = cần khám; High = nhập viện/bùng phát |
| 5 | Clinical Risk Assessment & Recommendations | Textarea | Bắt buộc khi chọn Medium/High | BR-147 | Ghi chú kế hoạch hành động ban đầu |
| 6 | Nút "Save review & Activate process" | Button (primary) | Disabled khi chưa chọn severity | BR-147, BR-148, BR-149 | Submit → lưu severity + kích hoạt quy trình tương ứng |

### Trạng thái màn hình
- Loading: spinner trên nút Save khi lưu + dispatch cảnh báo.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005, MSG-023 (Incident ID không hợp lệ).
- Success: severity = High → **AF1**: cảnh báo khẩn cấp gửi ngay tới School Board, Service Caterers, cơ quan y tế địa phương (BR-148), lô hàng nghi vấn chuyển Quarantine (BR-149) — banner `critical` hiển thị; severity Low/Medium → lưu bình thường, quay lại `26_IncidentDetail.md`.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (nút "Step 1: Assess the severity") hoặc từ Investigation Workspace; Save → quay lại Incident Detail.
**Bố cục**: Report 3 §3.3.2.3.6 — trang riêng full-screen, AppBar (Back/Home/Cancel), context đọc trên cùng, RadioGroup Severity level, Textarea Clinical Risk Assessment, nút "Save review & Activate process" cố định cuối màn.

## Platform Layout — Web
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Web); Save → quay lại Incident Detail.
**Bố cục**: `Dialog` (hành động quan trọng, theo `Component_Guidelines.md`) — context đọc trên cùng, RadioGroup Severity + Textarea, nút Save/Cancel cuối Dialog.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Assign Incident Severity Dialog, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: context (symptoms, affected count, suspected dish), RadioGroup Severity level
(Low/Medium/High), Textarea Clinical Risk Assessment & Recommendations, nút Save review & Activate process.
Áp dụng Design System: critical #7E22CE cho mức High, warning #B45309 cho Medium, success #15803D cho Low.
Trạng thái cần thể hiện: loading, error, success (banner critical nếu High).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4335–4349 (UC-009), dòng 5808–5825 (§3.3.2.3.6).
- business_rules.md (BR-147, BR-148, BR-149).
- error_message.md (MSG-004, MSG-005, MSG-023).
