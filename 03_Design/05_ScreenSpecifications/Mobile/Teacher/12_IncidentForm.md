# Screen Spec — Incident Form

**Actor**: Teacher | **UC liên quan**: UC-074 - Report Food Safety Incident | **Status**: Draft
**BR liên quan**: BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-155 (theo Business Rules field của UC-074, Report 3 dòng 4523 — đầy đủ hơn bảng BR tóm tắt ở `traceability_matrix.md` §1.11 chỉ ghi BR-146)
**MSG liên quan**: MSG-023 (báo cáo sự cố thiếu thông tin), MSG-048 (ghi nhận báo cáo sự cố thành công)

> **Ghi chú mâu thuẫn nguồn**: wireframe field-by-field gốc (Report 3 dòng 6367–6386) không liệt kê field "Suspected Dishes"/"Ingestion Date"/"Number of Affected Students" mà Normal Flow bước 3 của UC-074 (Report 3 dòng 4518) yêu cầu bắt buộc ("Teacher enters: observed symptoms, number of affected students, suspected dishes, and ingestion date"). Theo nguyên tắc mục 3 của Assignment Plan (không suy diễn nhưng cũng không được bỏ sót field UC yêu cầu), 3 field dưới đây được bổ sung thêm vào wireframe gốc — đánh dấu rõ nguồn UC, không phải nguồn wireframe.
>
> **Sửa 27/07/2026 (review/teacher)**: bỏ 2 field riêng "Ingestion Date Input" và "Detected Symptoms Checkboxes" — gộp chung vào ô "Incident Description Input" (mô tả tự do). Đã bỏ trích dẫn BR-154 khỏi mục BR liên quan vì BR-154 (`business_rules.md`) thực chất nói về "báo cáo sự cố chính thức" của Medical Staff, không liên quan Ingestion Date — trích dẫn cũ là số bị lệch từ Report 3.md. Hệ quả nghiệp vụ (mất auto-detect BR-146, giảm độ chính xác BR-149/BR-151): xem ghi chú đầy đủ tại UC-074 (`MF11_Incident_Emergency_Management.md`).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "Health Incident Report" + subtext "Record student condition" | — | Nguồn: Report 3 §3.3.2.6 |
| 2 | Student Input Field | Input (autocomplete từ Student List) | Bắt buộc — chọn học sinh (hỗ trợ chọn nhiều học sinh nếu ≥1 em bị ảnh hưởng) | BR-146 | |
| 3 | Class Input Field | Input (chỉ đọc) | Pre-filled tên lớp giáo viên | — | |
| 4 | Number of Affected Students Input | Input (số) | Bắt buộc — theo UC-074 bước 3 | BR-146 | Nguồn: Normal Flow UC-074, không có trong wireframe gốc |
| 5 | Suspected Dishes Input | Input/Select (từ thực đơn ngày) | Bắt buộc — theo UC-074 bước 3 | BR-151 | Nguồn: Normal Flow UC-074 |
| 6 | Severity Level Radio Group | Radio group | Mild (mặc định)/Medium/Severe | BR-147 | |
| 7 | Incident Description Input | Textarea | Bắt buộc, mô tả chi tiết — bao gồm cả triệu chứng và ngày ăn (gộp 27/07/2026) | — | MSG-023 nếu thiếu |
| 8 | Attached Photos Component | File upload (Take/Choose) | Tuỳ chọn — xem `11_UploadEvidence.md` | BR-196 | Cho phép offline (evidence capture) |
| 9 | Cancel Button | Button (destructive-outline) | Huỷ, quay về Teacher Home | — | |
| 10 | Submit Incident Report Button | Button (primary, full-width) | Validate đầy đủ trường bắt buộc trước khi gửi | BR-146, BR-149, BR-151 | MSG-023 nếu thiếu; MSG-048 khi thành công (tự động quarantine lô nghi vấn BR-149, sinh bản đồ truy xuất trong 4h BR-151) |

### Trạng thái màn hình
- Loading: Spinner khi đang submit.
- Empty: không áp dụng (form nhập mới).
- Error: `SnackBar` `danger` + MSG-023 (thiếu trường bắt buộc: học sinh liên quan, món ăn theo mô tả UC, hoặc mô tả chi tiết).
- Success: `SnackBar` `success` + MSG-048, điều hướng tới Incident Submitted Confirmation rồi Incident History.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Teacher Home ("Report New Health Incident"). Ra: chọn học sinh → Student List; submit → Incident Submitted Confirmation → (routed) Medical Staff Investigation Workspace.
**Bố cục**: AppBar → form 1 cột cuộn dọc theo thứ tự field trên → 2 nút Cancel/Submit cố định đáy màn hình (`Layout_Guidelines.md` mục 2.2).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Incident Form, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + form 1 cột (Student/Class input, Severity radio, Description textarea gồm cả triệu chứng/ngày ăn, Attached Photos, 2 nút Cancel/Submit cố định đáy).
Thành phần bắt buộc: Student Input Field, Class Input Field, Number of Affected Students Input, Suspected Dishes Input, Severity Level Radio Group, Incident Description Input, Attached Photos Component, Cancel Button, Submit Incident Report Button.
Trạng thái cần thể hiện: loading, error (MSG-023), success (MSG-048).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6367–6386 (Health Incident Report, field-by-field gốc), dòng 4507–4529 (UC-074 Report Food Safety Incident — Normal/Alternative/Exception Flow).
- `business_rules.md` BR-146, BR-196; `error_message.md` MSG-023, MSG-048.
