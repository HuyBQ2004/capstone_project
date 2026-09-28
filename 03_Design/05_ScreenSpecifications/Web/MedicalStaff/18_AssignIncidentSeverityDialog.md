# Screen Spec — Assign Incident Severity Dialog

> **⚠️ DEPRECATED 15/07/2026** — không còn là dialog/màn hình riêng. UC-009 (Assign Incident Severity) **vẫn giữ nguyên số hiệu và toàn bộ ràng buộc BR-147/BR-148**, nhưng nay được **gộp trình bày** trực tiếp vào `26_IncidentDetail.md` (field 5-6, cùng lúc với "Tường trình ban đầu" + "Chọn món ăn nghi vấn") — bỏ mô hình "hoàn tất Step 1 dialog rồi mới hiện Step 2". Lý do: người dùng phản hồi không muốn tách rời severity thành 1 bước/dialog riêng phải hoàn tất trước khi thấy phần còn lại của màn điều tra. Xem `26_IncidentDetail.md` field 5-6 và `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.4. Nội dung field bên dưới giữ nguyên để tham chiếu lịch sử — **không xoá file, không dùng để code mới**.

**Actor**: Medical Staff | **UC liên quan**: UC-009 (Report 3 §3.2, dòng 4335–4349) | **Status**: ~~Draft~~ Deprecated
**BR liên quan**: BR-147 (tiêu chí severity: Low/Medium/High), BR-148 (cảnh báo ngay lập tức High), BR-149 (cách ly lô hàng)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Reported symptoms (context, đọc) | Text | — | — | Report 3 §3.3.2.3.6 |
| 2 | Affected student count (context, đọc) | Text | — | — | |
| 3 | Suspected dish information (context, đọc) | Text | — | — | |
| 4 | Severity level | RadioGroup | Bắt buộc chọn: Low/Medium/High | BR-147 | |
| 5 | Clinical Risk Assessment & Recommendations | Textarea | Bắt buộc khi Medium/High | BR-147 | |
| 6 | Nút "Save review & Activate process" | Button (primary) | Disabled khi chưa chọn severity | BR-147, BR-148, BR-149 | |

### Trạng thái màn hình
- Loading: spinner trên nút Save.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005, MSG-023.
- Success: severity High → cảnh báo khẩn cấp ngay (BR-148), Quarantine lô hàng (BR-149), banner `critical`; Low/Medium → lưu bình thường, quay lại `26_IncidentDetail.md`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Web); Save → quay lại Incident Detail.
**Bố cục**: `Dialog` — context đọc trên cùng, RadioGroup Severity + Textarea, nút Save/Cancel.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Mobile, "Step 1: Assess the severity"); Save → quay lại Incident Detail.
**Bố cục**: Report 3 §3.3.2.3.6 — trang riêng full-screen, context trên cùng, RadioGroup + Textarea, nút Save cố định cuối màn.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Assign Incident Severity Dialog, nền tảng Web, actor Medical Staff.
Bố cục: Dialog, context (symptoms, affected count, suspected dish), RadioGroup Severity level,
Textarea Clinical Risk Assessment, nút Save review & Activate process.
Áp dụng Design System: critical #7E22CE (High), warning #B45309 (Medium), success #15803D (Low).
Trạng thái cần thể hiện: loading, error, success (banner critical nếu High).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4335–4349 (UC-009), dòng 5808–5825 (§3.3.2.3.6).
- business_rules.md (BR-147, BR-148, BR-149).
- error_message.md (MSG-004, MSG-005, MSG-023).
