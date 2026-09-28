# Screen Spec — Incident List

**Actor**: Medical Staff (màn hình dùng chung với School Administration — file này viết theo góc nhìn Medical Staff) | **UC liên quan**: UC-114 (Report 3 §3.2, dòng 4555–4569 — primary actor gốc School Administration) | **Status**: Draft

> **Viết lại toàn bộ 12/07/2026** — bản trước mô tả model 2-tab cũ ("Processing"/"Resolved"), đã lỗi thời so với thiết kế lại 09/07/2026 (`review/medical_staff/review_medicalstaff_incidentmanagement.md` §9.3, đã áp code thử) và code thật. Nội dung giống hệt bản Web (`Web/MedicalStaff/25_IncidentList.md`), chỉ khác Platform Layout.
> **Cập nhật 15/07/2026 — mô hình 4 tab + biên bản sự cố thủ công**: bổ sung tab mới "Biên bản chờ xử lý", đổi hành động "Xác nhận liên quan ATTP" thành mở dialog soạn biên bản (`33_CreateIncidentRecordDialog.md`) — chi tiết xem bản Web cùng tên và `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.

**BR liên quan**: BR-146 (ngưỡng gộp tự động ≥3 HS/24h), BR-147 (severity), BR-154 (báo cáo sự cố chính thức)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error), MSG-100 (Success), MSG-101 (Success)

## Business Logic (dùng chung mọi nền tảng)

### Cấu trúc 4 tab (cập nhật 15/07/2026)

| Tab | Nội dung | Cột/Field | Filter | Hành động |
| --- | --- | --- | --- | --- |
| **Chờ tiếp nhận** | Danh sách `RPT-xxx` (báo cáo thô của Teacher/Parent), `status = PENDING_TRIAGE` hoặc `DRAFTING` | Mã (`RPT-xxx`), Học sinh, Lớp, Triệu chứng, Bữa ăn/Món nghi vấn, Thời điểm báo cáo, Người báo cáo, chế độ "Chọn" (multi-select), **nút "Xem chi tiết" mỗi card**, badge "Đang soạn biên bản (nháp)" nếu `status = DRAFTING` | Theo lớp, loại triệu chứng, ngày báo cáo (**không** filter severity) | Thanh hành động nổi ở đáy khi ≥1 card chọn: **"Tạo báo cáo sự cố"** (≥1, đổi tên 15/07/2026 — mở `33_CreateIncidentRecordDialog.md`) / **"Đóng — Không liên quan ATTP"** (đúng 1). **"Xem chi tiết"**: mở modal/bottom sheet read-only |
| **Biên bản chờ xử lý** 🆕 | Danh sách biên bản đã "Hoàn tất" (`RPT-xxx.status = DRAFTED`) | Mã báo cáo nguồn (chip), Nội dung biên bản (rút gọn), Người lập, Thời điểm lập | Theo ngày lập | 2 nút mỗi card: **"Đóng — Không liên quan ATTP"** / **"Mở phiên điều tra"** (tạo `INC-xxx`, gắn chung `caseCode`, pre-fill "Tường trình ban đầu") |
| **Đang xử lý** | Danh sách `INC-xxx`, trạng thái 1–5 | Mã case & tiêu đề, Status Badge (5 nhãn theo trạng thái thực tế), Triệu chứng, Số học sinh ảnh hưởng | Severity Level, Status (BR-147) | Chọn 1 card → `26_IncidentDetail.md` |
| **Đã đóng** | Cả `RPT-xxx` đóng trực tiếp lẫn `INC-xxx` đã đóng | + "Loại hồ sơ" (RPT/INC), "Lý do đóng" | — | Chọn 1 card `INC-xxx` → `26_IncidentDetail.md` (read-only) |
| Nút "Export" | — | — | — | Xuất CSV/PDF danh sách đang hiển thị |

### Trạng thái `RPT-xxx` — enum mở rộng (15/07/2026)

`PENDING_TRIAGE` → (chọn "Tạo báo cáo sự cố") → `DRAFTING` (đang soạn nháp) → `DRAFTED` (đã "Hoàn tất biên bản") → `MERGED` (mở phiên điều tra) hoặc `CLOSED_NOT_FOOD_SAFETY` (đóng). Nhánh đóng trực tiếp không qua biên bản giữ nguyên: `PENDING_TRIAGE` → `CLOSED_NOT_FOOD_SAFETY`.

### Cơ chế gộp N-1 (tab "Chờ tiếp nhận" → "Biên bản chờ xử lý" → "Đang xử lý", cập nhật 15/07/2026)

1. Bật chế độ "Chọn" (long-press hoặc icon multi-select góc trên), tick chọn 1 hoặc nhiều card `RPT-xxx` (chỉ `status = PENDING_TRIAGE`).
2. **"Tạo báo cáo sự cố"** → mở `33_CreateIncidentRecordDialog.md` (dialog soạn biên bản, không còn là modal xác nhận suông). N>1: cảnh báo nếu ngày ăn không khớp, không chặn cứng.
3. Trong dialog: viết "Nội dung biên bản", có thể "Lưu bản nháp" nhiều lần (`status = DRAFTING`) trước khi "Hoàn tất biên bản" (`status = DRAFTED`).
4. Biên bản "Hoàn tất" sang tab "Biên bản chờ xử lý" — quyết định "Đóng — Không liên quan ATTP" hoặc "Mở phiên điều tra".
5. "Mở phiên điều tra" → tạo `INC-xxx` mới, `sourceReportIds` = các báo cáo đã gộp, `caseCode` gắn chung với biên bản, nội dung biên bản pre-fill "Tường trình ban đầu" (`26_IncidentDetail.md`); các `RPT-xxx` liên quan chuyển `status = MERGED`. Điều hướng thẳng sang `26_IncidentDetail.md`.
6. "Đóng — Không liên quan ATTP" ngay từ tab "Chờ tiếp nhận" (không qua biên bản, chỉ N=1): giữ nguyên như cũ.

### Nút "Xem chi tiết" — field khớp đúng `Teacher/12_IncidentForm.md` (siết lại 15/07/2026)

- Nút riêng trên mỗi card, tách biệt với vùng chạm bật chế độ "Chọn".
- Mở modal/bottom sheet read-only hiển thị **đúng và chỉ** field của `Teacher/12_IncidentForm.md`: Học sinh (multi), Lớp, Số học sinh ảnh hưởng, Món ăn nghi vấn (từ thực đơn ngày), Ngày ăn, Triệu chứng phát hiện (checkbox + Other), Mô tả sự cố (toàn văn), Ảnh đính kèm, Người báo cáo, Thời điểm báo cáo.
- **Field "Mức độ nghiêm trọng"**: hiển thị "Chưa đánh giá" (theo Mục 7.2 đã chốt — Teacher không có quyền tự gán severity) cho tới khi Medical Staff đánh giá ở `26_IncidentDetail.md`. `12_IncidentForm.md` chưa được sửa theo quyết định này — ghi nhận riêng, ngoài phạm vi màn này.
- Không có hành động sửa/xoá trong modal.
- Lý do bổ sung: card danh sách chỉ hiển thị field rút gọn, không đủ để đọc mô tả dài trước khi quyết định gộp/đóng.

### Trạng thái màn hình
- Loading: `Shimmer`/`Skeleton` cho danh sách.
- Empty: "Không có báo cáo/sự cố nào" theo từng tab + illustration.
- Error: MSG-004, MSG-005, MSG-023; race condition khi báo cáo đã bị xử lý bởi người khác trong lúc mở modal gộp.
- Success: Toast "Đã tạo phiên điều tra [mã INC-xxx]" / "Đã đóng báo cáo [mã RPT-xxx]".

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Sự cố & Điều tra" trên Dashboard; "Tạo báo cáo sự cố" → `33_CreateIncidentRecordDialog.md`; "Mở phiên điều tra" hoặc chọn item (INC) → `26_IncidentDetail.md`.
**Bố cục**: AppBar, 4 Tab, tab "Chờ tiếp nhận" dùng danh sách `Card` cuộn dọc + chế độ "Chọn" (long-press/icon multi-select) thay cho checkbox permanent; tab "Biên bản chờ xử lý" dùng `Card` với 2 nút cuối mỗi card; thanh hành động nổi ở đáy màn hình khi có card được chọn; pull-to-refresh bắt buộc.

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Sự cố & Điều tra" trên Dashboard (Web); "Tạo báo cáo sự cố" → `33_CreateIncidentRecordDialog.md` (Web); "Mở phiên điều tra" hoặc chọn dòng (INC) → `26_IncidentDetail.md` (Web).
**Bố cục**: Page Header 4 Tab + filter, tab "Chờ tiếp nhận" dùng `Table` với cột checkbox đầu tiên + thanh hành động nổi (sticky) ở dưới cùng; tab "Biên bản chờ xử lý" dùng `Table` với 2 nút cuối mỗi dòng; 2 tab còn lại Table thông thường. Nút "Export" góc trên phải.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Incident List, nền tảng Mobile, actor Medical Staff.
Bố cục: AppBar, 4 Tab ("Chờ tiếp nhận" / "Biên bản chờ xử lý" / "Đang xử lý" / "Đã đóng"), nút Export.
Tab "Chờ tiếp nhận": danh sách Card cuộn dọc (Mã/Học sinh/Lớp/Triệu chứng/Bữa ăn-Món nghi vấn/
Thời điểm báo cáo/Người báo cáo), nút "Xem chi tiết" riêng trên mỗi card, chế độ "Chọn" multi-select,
thanh hành động nổi ở đáy khi có card chọn ("Tạo báo cáo sự cố" / "Đóng — Không liên quan ATTP").
Tab "Biên bản chờ xử lý": Card các biên bản đã hoàn tất (Mã RPT nguồn/Nội dung rút gọn/Người lập/Thời điểm),
2 nút cuối mỗi card ("Đóng — Không liên quan ATTP" / "Mở phiên điều tra").
Tab "Đang xử lý": Card các sự cố INC-xxx, Status Badge theo 5 trạng thái tiến độ.
Tab "Đã đóng": Card gồm cả RPT lẫn INC đã đóng, thêm badge Loại hồ sơ + Lý do đóng.
Áp dụng Design System: warning #B45309.
Trạng thái cần thể hiện: loading, empty (mỗi tab), error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4555–4569 (UC-114), dòng 5758–5782 (§3.3.2.3.4).
- business_rules.md (BR-146, BR-147, BR-154, Changelog v1.8).
- error_message.md (MSG-004, MSG-005, MSG-023, MSG-100, MSG-101).
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` §9.3 (thiết kế 3-tab, 09/07/2026), §9.13 (nút Xem chi tiết, 12/07/2026), Mục 10 (mô hình 4-tab + biên bản thủ công, 15/07/2026).

**Ghi chú màn hình dùng chung**: xem `Web/SchoolAdministration/09_IncidentList.md` cho góc nhìn School Administration — tab "Chờ tiếp nhận" (RPT-xxx) là đặc thù riêng của Medical Staff.
