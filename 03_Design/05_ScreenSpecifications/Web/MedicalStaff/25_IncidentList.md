# Screen Spec — Incident List

**Actor**: Medical Staff (màn hình dùng chung với School Administration — file này viết theo góc nhìn Medical Staff) | **UC liên quan**: UC-114 (Report 3 §3.2, dòng 4555–4569) | **Status**: Draft

> **Viết lại toàn bộ 12/07/2026** — bản trước mô tả model 2-tab cũ ("Processing"/"Resolved"), đã lỗi thời so với thiết kế lại 09/07/2026 (`review/medical_staff/review_medicalstaff_incidentmanagement.md` §9.3, đã áp code thử) và code thật (`incidents/page.tsx`). Bản này viết lại theo đúng model **3-tab** hiện hành, đồng thời bổ sung quyết định mới 12/07/2026 (nút "Xem chi tiết" ở tab "Chờ tiếp nhận" — phát hiện qua rà soát ảnh chụp code thật, xem §9.13 review file).
> **Cập nhật 15/07/2026 — mô hình 4 tab + biên bản sự cố thủ công**: bổ sung tab mới "Biên bản chờ xử lý" (giữa "Chờ tiếp nhận" và "Đang xử lý"), đổi hành động "Xác nhận liên quan ATTP" thành mở dialog soạn biên bản (`33_CreateIncidentRecordDialog.md`) thay vì tạo `INC-xxx` ngay lập tức — đảo ngược quyết định 09/07/2026 (Mục 7.8). Đồng thời siết lại nội dung modal "Xem chi tiết" cho khớp đúng field của `Teacher/12_IncidentForm.md`. Chi tiết quyết định: `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.
> **Cập nhật 15/07/2026 (đợt 2 — Mục 11.2)**: modal "Xem chi tiết" `RPT-xxx` **phóng to theo pattern "Dialog cỡ lớn"** (cùng cỡ đã dùng ở `33_CreateIncidentRecordDialog.md`), bố cục **2 cột**, thay cho modal nhỏ trước đó. Vẫn giữ nguyên là `Dialog` (không tách route/Screen Spec riêng vì `RPT-xxx` là dữ liệu tạm, chỉ tồn tại tới khi merge/close — xem Mục 11.2 review file). Chi tiết: `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 11.2.

**BR liên quan**: BR-146 (ngưỡng gộp tự động ≥3 HS/24h), BR-147 (severity), BR-154 (báo cáo sự cố chính thức)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error), MSG-100 (Success), MSG-101 (Success)

## Business Logic (dùng chung mọi nền tảng)

### Cấu trúc 4 tab (cập nhật 15/07/2026)

| Tab | Nội dung | Cột/Field | Filter | Hành động |
| --- | --- | --- | --- | --- |
| **Chờ tiếp nhận** | Danh sách `RPT-xxx` (báo cáo thô của Teacher/Parent), `status = PENDING_TRIAGE` hoặc `DRAFTING` (đang soạn nháp, xem tab kế) | Checkbox chọn, Mã (`RPT-xxx`), Học sinh, Lớp, Triệu chứng, Bữa ăn/Món nghi vấn, Thời điểm báo cáo, Người báo cáo, **nút "Xem chi tiết" mỗi dòng**, badge "Đang soạn biên bản (nháp)" nếu `status = DRAFTING` | Theo lớp, loại triệu chứng, ngày báo cáo (**không** filter severity — chưa có ở giai đoạn này) | Thanh hành động khi ≥1 dòng chọn: **"Tạo báo cáo sự cố"** (≥1 dòng, đổi tên từ "Xác nhận liên quan ATTP" 15/07/2026 — mở `33_CreateIncidentRecordDialog.md` thay vì tạo `INC-xxx` ngay) / **"Đóng — Không liên quan ATTP"** (đúng 1 dòng — tránh đóng nhầm hàng loạt). **"Xem chi tiết"**: mở modal read-only (xem cấu trúc field ở mục riêng bên dưới) |
| **Biên bản chờ xử lý** 🆕 | Danh sách biên bản đã "Hoàn tất" (`RPT-xxx.status = DRAFTED`, từ `33_CreateIncidentRecordDialog.md`) | Mã báo cáo nguồn (chip, có thể nhiều), Nội dung biên bản (rút gọn, "Xem đầy đủ" mở lại read-only), Người lập, Thời điểm lập | Theo ngày lập | 2 nút mỗi dòng: **"Đóng — Không liên quan ATTP"** (`status = CLOSED_NOT_FOOD_SAFETY`, không tạo `INC-xxx`) / **"Mở phiên điều tra"** (tạo `INC-xxx` mới, `caseCode` gắn chung với biên bản, nội dung biên bản pre-fill sang "Tường trình ban đầu" ở `26_IncidentDetail.md`, chuyển `status = MERGED`) |
| **Đang xử lý** | Danh sách `INC-xxx` đã tạo/gộp, trạng thái 1–5 (state machine tiến độ) | Mã case & tiêu đề, Status Badge (1 trong 5 nhãn theo trạng thái thực tế: "Chờ đánh giá mức độ" / "Đang xử lý sơ cứu" / "Đang điều tra" / "Chờ duyệt CAPA" / "Đang triển khai CAPA"), Triệu chứng, Số học sinh ảnh hưởng | Severity Level, Status (BR-147) | Chọn 1 dòng → `26_IncidentDetail.md` |
| **Đã đóng** | Cả `RPT-xxx` đóng trực tiếp (không liên quan ATTP) lẫn `INC-xxx` đã đóng (điều tra hoàn tất) | + cột "Loại hồ sơ" (Báo cáo/`RPT` — Sự cố/`INC`), "Lý do đóng" (Không liên quan ATTP / Đã hoàn tất điều tra) | — | Chọn 1 dòng `INC-xxx` → `26_IncidentDetail.md` (read-only); dòng `RPT-xxx` không điều hướng, chỉ xem qua "Xem chi tiết" nếu cần |
| Nút "Export" | — | — | — | Xuất CSV/PDF danh sách đang hiển thị |

### Trạng thái `RPT-xxx` — enum mở rộng (15/07/2026)

`PENDING_TRIAGE` → (chọn "Tạo báo cáo sự cố") → **`DRAFTING`** (đang soạn nháp, có thể "Lưu bản nháp" nhiều lần) → **`DRAFTED`** (đã "Hoàn tất biên bản", sang tab "Biên bản chờ xử lý") → `MERGED` (mở phiên điều tra) hoặc `CLOSED_NOT_FOOD_SAFETY` (đóng). Riêng nhánh đóng trực tiếp từ tab "Chờ tiếp nhận" (không qua biên bản) vẫn giữ nguyên như cũ: `PENDING_TRIAGE` → `CLOSED_NOT_FOOD_SAFETY`. *(Ghi nhận để người dùng tự áp dụng vào class diagram — theo đúng quy ước đã dùng ở Mục 7.5 review file, không tự sửa class diagram ở đây.)*

### Cơ chế gộp N-1 (tab "Chờ tiếp nhận" → "Biên bản chờ xử lý" → "Đang xử lý", cập nhật 15/07/2026)

1. Tick chọn 1 hoặc nhiều dòng `RPT-xxx` (chỉ dòng `status = PENDING_TRIAGE`).
2. Bấm **"Tạo báo cáo sự cố"** → mở `33_CreateIncidentRecordDialog.md` (dialog soạn biên bản thủ công, không còn là modal xác nhận suông như bản cũ). Nếu N>1, khối context union hiển thị cảnh báo nếu ngày ăn không khớp giữa các RPT — không chặn cứng, Medical Staff tự quyết khi viết biên bản.
3. Trong dialog: Medical Staff tự viết "Nội dung biên bản" (field 5, `33_CreateIncidentRecordDialog.md`), có thể **"Lưu bản nháp"** nhiều lần (`status = DRAFTING`) trước khi **"Hoàn tất biên bản"** (`status = DRAFTED`).
4. Biên bản đã "Hoàn tất" xuất hiện ở tab **"Biên bản chờ xử lý"** — Medical Staff xem lại, quyết định 1 trong 2: "Đóng — Không liên quan ATTP" (`status = CLOSED_NOT_FOOD_SAFETY`, không tạo `INC-xxx`) hoặc **"Mở phiên điều tra"**.
5. "Mở phiên điều tra" → tạo `INC-xxx` mới, `sourceReportIds` = các báo cáo đã gộp, `caseCode` gắn chung với biên bản vừa soạn, nội dung biên bản trở thành pre-fill cho "Tường trình ban đầu" (`26_IncidentDetail.md`); các `RPT-xxx` liên quan chuyển `status = MERGED`. Điều hướng thẳng sang `26_IncidentDetail.md` của `INC-xxx` vừa tạo.
6. "Đóng — Không liên quan ATTP" ngay từ tab "Chờ tiếp nhận" (không qua biên bản, chỉ N=1): giữ nguyên như cũ — `status = CLOSED_NOT_FOOD_SAFETY`, không tạo biên bản, không tạo `INC-xxx`.

### Nút "Xem chi tiết" — field khớp đúng `Teacher/12_IncidentForm.md` (siết lại 15/07/2026; phóng to 2 cột — Mục 11.2)

- Vùng click tách biệt với checkbox chọn dòng (không kích hoạt lẫn nhau).
- **Dạng hiển thị (Mục 11.2)**: `Dialog` cỡ lớn (không tách route riêng — `RPT-xxx` là dữ liệu tạm), bố cục **2 cột**:
  - **Cột trái**: Học sinh (multi), Lớp (đọc), Số học sinh ảnh hưởng, Món ăn nghi vấn (chọn từ thực đơn ngày), Ngày ăn (Ingestion Date), Người báo cáo, Thời điểm báo cáo.
  - **Cột phải**: Triệu chứng phát hiện (checkbox, gồm "Other"), Mô tả sự cố (toàn văn, không cắt bớt), Ảnh đính kèm (nếu có, dạng gallery).
- Nội dung hiển thị **đúng và chỉ** các field đã có ở `Teacher/12_IncidentForm.md`.
- **Riêng field "Mức độ nghiêm trọng"**: theo Mục 7.2 (`review_medicalstaff_incidentmanagement.md`, đã chốt 09/07/2026), Teacher không có quyền tự gán severity — modal hiển thị **"Chưa đánh giá"** cho tới khi Medical Staff chính thức đánh giá ở `26_IncidentDetail.md`. *(Ghi nhận: `12_IncidentForm.md` thực tế chưa được sửa theo quyết định này — khoảng trống riêng, ngoài phạm vi màn này.)*
- Không có hành động sửa/xoá trong modal — chỉ để Medical Staff tham khảo đầy đủ nội dung trước khi quyết định gộp/đóng.
- Lý do bổ sung: bảng chính chỉ hiển thị field rút gọn (phù hợp cho quét nhanh nhiều dòng), không đủ để đọc mô tả dài trước khi ra quyết định.

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng/card.
- Empty: "Không có báo cáo/sự cố nào" theo từng tab + illustration.
- Error: MSG-004, MSG-005, MSG-023; race condition khi 1 báo cáo đã bị người khác xử lý trong lúc đang mở modal gộp → "Một số báo cáo đã được xử lý, vui lòng tải lại danh sách".
- Success: Toast "Đã tạo phiên điều tra [mã INC-xxx]" (gộp) / "Đã đóng báo cáo [mã RPT-xxx]" (đóng).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Sự cố & Điều tra" trên Dashboard (Web); "Tạo báo cáo sự cố" → `33_CreateIncidentRecordDialog.md` (Web); "Mở phiên điều tra" hoặc chọn dòng ở tab "Đang xử lý"/"Đã đóng" (INC) → `26_IncidentDetail.md` (Web).
**Bố cục**: Page Header 4 Tab + filter, tab "Chờ tiếp nhận" dùng `Table` với cột checkbox đầu tiên + thanh hành động nổi (sticky) ở dưới cùng khi có dòng được chọn; tab "Biên bản chờ xử lý" dùng `Table` với 2 nút hành động cuối mỗi dòng; 2 tab còn lại giữ layout Table thông thường. Nút "Export" góc trên phải.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Sự cố & Điều tra" trên Dashboard (Mobile); "Tạo báo cáo sự cố" → `33_CreateIncidentRecordDialog.md` (Mobile); "Mở phiên điều tra" hoặc chọn item (INC) → `26_IncidentDetail.md` (Mobile).
**Bố cục**: AppBar, 4 Tab, tab "Chờ tiếp nhận" dùng danh sách `Card` cuộn dọc + chế độ "Chọn" (long-press/icon multi-select góc trên) thay cho checkbox permanent; tab "Biên bản chờ xử lý" dùng danh sách `Card` với 2 nút cuối mỗi Card; thanh hành động nổi ở đáy màn hình khi có card được chọn; pull-to-refresh.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Incident List, nền tảng Web, actor Medical Staff.
Bố cục: Page Header 4 Tab ("Chờ tiếp nhận" / "Biên bản chờ xử lý" / "Đang xử lý" / "Đã đóng") + filter, nút Export góc trên phải.
Tab "Chờ tiếp nhận": Table với checkbox đầu mỗi dòng, cột Mã/Học sinh/Lớp/Triệu chứng/Bữa ăn-Món nghi vấn/
Thời điểm báo cáo/Người báo cáo, nút "Xem chi tiết" cuối mỗi dòng (vùng click riêng, không lẫn checkbox),
thanh hành động nổi cuối bảng khi có dòng chọn ("Tạo báo cáo sự cố" / "Đóng — Không liên quan ATTP").
Tab "Biên bản chờ xử lý": Table các biên bản đã hoàn tất, cột Mã RPT nguồn/Nội dung rút gọn/Người lập/Thời điểm,
2 nút cuối mỗi dòng ("Đóng — Không liên quan ATTP" / "Mở phiên điều tra").
Tab "Đang xử lý": Table các sự cố INC-xxx, Status Badge theo 5 trạng thái tiến độ.
Tab "Đã đóng": Table gồm cả RPT lẫn INC đã đóng, thêm cột Loại hồ sơ + Lý do đóng.
Áp dụng Design System: warning #B45309.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4555–4569 (UC-114), dòng 5758–5782 (§3.3.2.3.4).
- business_rules.md (BR-146, BR-147, BR-154, Changelog v1.8).
- error_message.md (MSG-004, MSG-005, MSG-023, MSG-100, MSG-101).
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` §9.3 (thiết kế 3-tab, 09/07/2026), §9.13 (nút Xem chi tiết, 12/07/2026), Mục 10 (mô hình 4-tab + biên bản thủ công, 15/07/2026).

**Ghi chú màn hình dùng chung**: xem `Web/SchoolAdministration/09_IncidentList.md` cho góc nhìn School Administration — cùng dữ liệu `INC-xxx`, khác cách trình bày bộ lọc; tab "Chờ tiếp nhận" (RPT-xxx) là đặc thù riêng của Medical Staff (School Administration không xử lý báo cáo thô).
