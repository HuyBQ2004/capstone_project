# Screen Spec — Incident Form

**Actor**: Teacher | **UC liên quan**: UC-074 - Report Food Safety Incident | **Status**: Draft
**BR liên quan**: BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-155
**MSG liên quan**: MSG-023, MSG-048

> **Ghi chú mâu thuẫn nguồn**: xem giải thích đầy đủ ở bản Mobile cùng tên — wireframe gốc (Report 3 dòng 6367–6386) thiếu 3 field mà Normal Flow UC-074 yêu cầu (Number of Affected Students, Suspected Dishes, Ingestion Date), đã bổ sung theo UC.
>
> **Sửa 27/07/2026 (review/teacher)**: bỏ 2 field riêng "Ingestion Date Input" và "Detected Symptoms Checkboxes" — gộp chung vào ô "Incident Description Input" (mô tả tự do) để đơn giản hoá thao tác nhập liệu, giáo viên tự viết ngày ăn/triệu chứng vào mô tả thay vì 2 field cấu trúc riêng. Đã bỏ trích dẫn BR-154 khỏi mục BR liên quan — bản thân BR-154 (`business_rules.md`) nói về việc Medical Staff lập "báo cáo sự cố chính thức", không liên quan field Ingestion Date; trích dẫn cũ là số bị lệch từ Report 3.md, không phải quy tắc áp dụng cho form này. Hệ quả: BR-146 (auto-detect cụm triệu chứng ≥3 HS/24h) và BR-149/BR-151 (cách ly lô + truy xuất theo đúng ngày ăn) không còn dữ liệu triệu chứng/ngày ăn có cấu trúc từ báo cáo Teacher — xem chi tiết ghi chú tại UC-074 (`MF11_Incident_Emergency_Management.md`).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Health Incident Report" | — | |
| 2 | Student Input Field | `Select` (multi, autocomplete) | Bắt buộc | BR-146 | |
| 3 | Class Input Field | Input (chỉ đọc) | Pre-filled | — | |
| 4 | Number of Affected Students Input | Input (số) | Bắt buộc | BR-146 | Nguồn: Normal Flow UC-074 |
| 5 | Suspected Dishes Input | `Select` (từ thực đơn ngày) | Bắt buộc | BR-151 | Nguồn: Normal Flow UC-074 |
| 6 | Severity Level Radio Group | `RadioGroup` | Mild/Medium/Severe | BR-147 | |
| 7 | Incident Description Input | `Textarea` | Bắt buộc — bao gồm cả triệu chứng quan sát được và ngày ăn (gộp 27/07/2026) | — | MSG-023 |
| 8 | Attached Photos Component | `Input type="file"` | Tuỳ chọn — xem `11_UploadEvidence.md` | — | |
| 9 | Cancel Button | Button (ghost) | Huỷ | — | |
| 10 | Submit Incident Report Button | Button (primary) | Validate đầy đủ | BR-146, BR-149, BR-151 | MSG-023/MSG-048 |

### Trạng thái màn hình
- Loading: Spinner khi submit.
- Error: `Alert variant="destructive"` + MSG-023.
- Success: `Toast` `success` + MSG-048.
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Sự cố Sức khỏe" (theo `12_Teacher_Screen_Flow_Web.md`). Ra: Incident History sau khi submit.
**Bố cục**: Topbar + Sidebar + Content: form dài chia 2 cột (Student/Class/Symptoms bên trái, Severity/Description/Photos bên phải) — không dùng Stepper vì form không quá dài; nút Cancel/Submit cố định cuối form.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Incident Form, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + form 2 cột (Student/Class bên trái | Severity/Description/Photos bên phải) + nút Cancel/Submit.
Thành phần bắt buộc: Student Input Field, Class Input Field, Number of Affected Students Input, Suspected Dishes Input, Severity Level Radio Group, Incident Description Input (mô tả gồm cả triệu chứng và ngày ăn), Attached Photos Component, Cancel Button, Submit Incident Report Button.
Trạng thái cần thể hiện: loading, error (MSG-023), success (MSG-048).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6367–6386 (wireframe gốc), dòng 4507–4529 (UC-074 Normal/Alternative/Exception Flow).
- `business_rules.md` BR-146–151, BR-155; `error_message.md` MSG-023, MSG-048.
