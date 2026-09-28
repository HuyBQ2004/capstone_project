# Review — Thiết kế lại luồng Incident Management theo nguyên tắc List → Detail (xem + sửa trực tiếp) → Export/Lưu PDF — Medical Staff

**Mục đích**: ghi lại đề xuất thiết kế lại + các quyết định đã xác nhận (18/07/2026, qua `AskUserQuestion`) để gộp `26_IncidentDetail.md` + `17_InvestigationWorkspace.md` + `19_RecordCorrectiveActionsReportScreen.md`/`20_GenerateIncidentReportDialog.md` thành **1 route Detail duy nhất**, đúng nguyên tắc List → Detail (xem + sửa trực tiếp) → Export PDF mà người dùng chỉ định. Không thêm/xoá BR, UC, MSG nào — chỉ đổi cách trình bày điều hướng (routing/component), tiếp nối đúng xu hướng đã làm ở Mục 10.4/10.7/12.8.1 của `review_medicalstaff_incidentmanagement.md`.

**Ngày biên soạn**: 18/07/2026.

**Nguồn đã đọc/tra cứu để biên soạn**:
- `03_Design/05_ScreenSpecifications/Web/MedicalStaff/25_IncidentList.md`, `26_IncidentDetail.md`, `17_InvestigationWorkspace.md`, `19_RecordCorrectiveActionsReportScreen.md`, `20_GenerateIncidentReportDialog.md`, `33_CreateIncidentRecordDialog.md`
- `02_Requirements/00_BusinessRules/business_rules.md` — BR-146 đến BR-155
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10 (gộp Step 1+2, gộp Bước 3+4), Mục 11.10 (Collapsible Panel), Mục 12.8.1 (CollapsiblePanel dùng chung), **Mục 13** (taxonomy Pattern A/B/C/D — nguồn gốc của đề xuất này)
- `Review frontend web.docx` do người dùng cung cấp (18/07/2026) — 3 ảnh Service Caterers dùng làm tham khảo nguyên tắc List→Detail→Print, không copy field/BR chéo module (xem Mục 13.1 file trên)

---

## 1. Bối cảnh & nguyên tắc mục tiêu

Người dùng chỉ định rõ nguyên tắc cho luồng Incident Management: **List → View Detail (cho sửa trực tiếp) → Xuất/Lưu file PDF**, thay vì 4-pattern taxonomy đã ghi ở Mục 13 (`review_medicalstaff_incidentmanagement.md`). Hiện trạng trước khi sửa:

| Bước | File | Vai trò hiện tại | Vấn đề so với nguyên tắc mục tiêu |
| --- | --- | --- | --- |
| 1 | `25_IncidentList.md` | List, 4 tab | Không có vấn đề — đã đúng "List" |
| 2 | `33_CreateIncidentRecordDialog.md` | Dialog soạn biên bản triage (trước khi case tồn tại) | Không có vấn đề — xảy ra trước khi có case, chưa thể là "Detail" |
| 3 | `26_IncidentDetail.md` | Route Detail chính | Đúng "Detail", nhưng chưa chứa toàn bộ nội dung điều tra/báo cáo |
| 4 | `17_InvestigationWorkspace.md` | **Route riêng** (`/investigation`), vào từ nút trên `26` | Lệch nguyên tắc: Medical Staff phải **rời trang Detail** để làm việc, không phải "sửa trực tiếp tại chỗ" |
| 5 | `19_RecordCorrectiveActionsReportScreen.md` + `20_GenerateIncidentReportDialog.md` | Đã gộp (15/07/2026) thành 1 điểm vào "Hoàn tất sự cố", mở từ `26` | Về cơ bản đã đúng hướng, nhưng chưa có gate khoá read-only rõ ràng ngay tại Detail sau khi xuất PDF |

## 2. Thiết kế đề xuất — gộp thành 1 route Detail, 4 section co giãn

Gộp `26` + `17` + (`19`+`20` đã gộp) thành **1 route duy nhất** (`/incidents/[id]`), chia thành 4 section `Collapsible Panel` (dùng lại đúng component đã chốt ở Mục 11.10/12.8.1, không tạo component mới):

| Section | Nội dung (giữ nguyên field/BR của file gốc) | Trạng thái mặc định | Khoá/mở theo |
| --- | --- | --- | --- |
| **A — Biên bản gốc** | Nguyên field 4b của `26` (đọc, trỏ `IncidentRecordDraft.content` từ `33`) | Thu gọn, luôn đọc | Không đổi theo trạng thái case |
| **B — Đánh giá & chuẩn bị điều tra** | Nguyên field 5–9 của `26` (severity, đánh giá rủi ro, tường trình, chọn món nghi vấn) | Mở khi case mới tạo | Sửa trực tiếp cho tới khi bấm "Lưu đánh giá & Bắt đầu điều tra" → khoá thành tóm tắt, tự mở Section C |
| **C — Investigation Workspace** | Nguyên toàn bộ nội dung `17` (field 0–13: banner "đang chờ ai", 4 khối Extract samples/Gathering evidence/Root cause/Remedial measures) | Mở sau khi Section B khoá | Sửa trực tiếp; khoá khi "Establish Measures" đã submit (CAPA giao xong, chờ School Admin duyệt) |
| **D — Hoàn tất sự cố** | Nguyên Block A (`19`, checklist hồi phục) + Block B (`20`, soạn báo cáo + Export) | Mở sau khi CAPA được School Admin duyệt (E17) | Sửa trực tiếp cho tới khi bấm "Export & Submit Incident Reports" |

**Không đổi**: thứ tự gate nghiệp vụ (severity+món nghi vấn bắt buộc trước điều tra — BR-147/BR-149; CAPA phải được School Admin duyệt trước khi mở Section D — BR-153; checklist hồi phục 100% trước khi xuất báo cáo — BR-155); banner "đang chờ ai" ở Section C giữ nguyên vì vẫn còn giá trị dù không rời trang (Medical Staff vẫn có thể rời sang case khác rồi quay lại).

## 3. Quyết định đã xác nhận (18/07/2026, qua `AskUserQuestion`)

1. **Gộp `17` vào route Detail (`26`) thành 1 route duy nhất** — không giữ route `/investigation` riêng. Đúng nguyên tắc "sửa trực tiếp tại chỗ", đổi lại cần sửa routing code (bỏ `investigation/page.tsx` độc lập, đưa nội dung vào `incidents/[id]/page.tsx` dưới dạng Section C).
2. **Khoá cứng ngay tại Detail sau khi Export & Submit thành công** — toàn bộ 4 section chuyển display-only ngay trên trang (gate UI mới, tường minh), không chỉ dựa vào việc case chuyển sang tab "Đã đóng" ở List như trước.
3. **`33_CreateIncidentRecordDialog.md` giữ nguyên tách biệt** ở phía List — không gộp vào Detail vì case chưa tồn tại tại thời điểm đó.
4. **Bỏ bảng field 1–2 của `19_RecordCorrectiveActionsReportScreen.md`** (danh sách "Sự cố có CAPA Approved chờ xác nhận y tế") — quyết định 18/07/2026, xem lý do ở Mục 4. Case luôn được vào qua `25` (tab "Đang xử lý", badge "Đang triển khai CAPA") → `26` → Section D, không cần 1 bảng liệt kê thứ 2 cho cùng tập case.
5. **Cơ chế khoá read-only sau khi đóng case (làm rõ Quyết định 2)** — đúng như người dùng mô tả: trong lúc case còn ở trạng thái điều tra/chưa đóng, **chỉ Medical Staff** được sửa, và mỗi lần mở lại Detail vẫn load đúng bản draft đã lưu để sửa tiếp (không mất dữ liệu). Sau khi case **đã đóng hẳn** (Export & Submit ở Section D thành công), khoá read-only áp dụng cho **mọi actor, không có cơ chế mở khoá lại** — kể cả Medical Staff lẫn School Administration. Không cần BR mới cho việc này vì chỉ là gate UI, không đổi quyền truy cập dữ liệu đã có.

## 4. Checklist thay đổi cần áp dụng theo file

| File | Thay đổi cần áp dụng | Loại thay đổi |
| --- | --- | --- |
| `26_IncidentDetail.md` | Viết lại mục "Platform Layout — Web/Mobile": mô tả 4 section co giãn (A–D) trên **cùng 1 route**, không còn nút điều hướng "Step 2: Investigation Workspace" (field 12) mở route khác — thay bằng hành động mở/khoá Section C tại chỗ. Bổ sung field mới: **cờ trạng thái "Đã xuất báo cáo — Read-only"** áp dụng cho toàn trang sau khi Section D submit thành công (Quyết định 2, Mục 3) | Sửa Screen Spec |
| `17_InvestigationWorkspace.md` | Đánh dấu **Deprecated as standalone Screen Spec** (theo đúng tiền lệ đã làm với `18_AssignIncidentSeverityDialog.md` khi gộp vào `26` — xem ghi chú trong `26` field 5) — nội dung field 0–13 **không đổi**, chỉ chuyển thành "Section C của `26_IncidentDetail.md`", giữ file lại để tham chiếu lịch sử UC-041/BR liên quan | Đổi trạng thái file (Deprecated), không đổi field |
| `19_RecordCorrectiveActionsReportScreen.md` | **Bỏ field 1–2** ("Danh sách sự cố có CAPA Approved chờ xác nhận" + "Chọn 1 dòng → xem chi tiết") — đây là mô tả còn sót lại từ **trước** lần gộp 15/07/2026 (khi UC-168 còn là màn độc lập có list riêng của nó). Quyết định 18/07/2026: vì `25_IncidentList.md` đã là List duy nhất của toàn bộ case (tab "Đang xử lý", badge "Đang triển khai CAPA" bao trùm giai đoạn này), giữ thêm 1 bảng liệt kê thứ 2 trong `19` sẽ tạo 2 nguồn List song song cho cùng 1 tập dữ liệu — ngược lại đúng nguyên tắc "chỉ 1 List" của đợt redesign này. Nội dung còn lại (field 3–5: ghi chú kiểm tra bếp, checklist hồi phục, nút Submit) giữ nguyên, trở thành Block A của Section D | Sửa Screen Spec |
| `20_GenerateIncidentReportDialog.md` | Đổi mô tả "Platform Layout — Web" từ `Dialog`/trang con thành "Block B trong Section D của `26_IncidentDetail.md`"; bổ sung ghi chú gate khoá read-only sau khi nút "Export & Submit Incident Reports" (field 10) chạy thành công (Quyết định 2, Mục 3) | Sửa Screen Spec |
| Code: `investigation/page.tsx` | Gộp nội dung vào `incidents/[id]/page.tsx` dưới dạng Section C (dùng lại `CollapsiblePanel` đã có từ Mục 12.8.1); xoá route `/investigation` độc lập | Refactor code |
| Code: `incidents/[id]/page.tsx` | Thêm Section C (từ `investigation/page.tsx`) và cờ `isLocked`/read-only toàn trang sau khi Export thành công (Quyết định 2) | Refactor code |

**Câu hỏi đã đóng (18/07/2026)**: cả 2 điểm còn mở ở bản trước đã được người dùng xác nhận trực tiếp, chốt thành Quyết định 4–5 ở Mục 3 và cập nhật vào bảng checklist trên. Không còn câu hỏi mở nào ở đợt redesign này.

**Lưu ý phạm vi**: toàn bộ đợt redesign này chỉ đổi UI/routing (`26`/`17`/`19`/`20`), không cần sửa `business_rules.md` hay `Report3.md` — các gate nghiệp vụ (BR-147/149/153/155) giữ nguyên nội dung, chỉ đổi nơi hiển thị. Ghi chú thêm: người dùng xác nhận (18/07/2026) rằng `business_rules.md`/`Report3.md` **không phải tài liệu bất biến** — có thể sửa khi có căn cứ hợp lý, không cần cứng nhắc giữ nguyên bằng mọi giá; áp dụng cho các đợt review sau nếu redesign thực sự đòi hỏi đổi nội dung BR/UC, không chỉ đổi cách trình bày.

---

## 5. Vòng rà soát thứ 2 (18/07/2026, qua 7 ảnh chụp code thật) — biên bản sự cố List→Detail, gộp Section B, liên kết chéo Violation

### 5.1. Bối cảnh

Người dùng cung cấp 7 ảnh chụp code thật (đã áp 1 phần Mục 1-4 ở trên: 4-tab List, `33` dạng Dialog, `26` đã gộp Section A/B/C). Yêu cầu vòng này: (1) chuyển `33` từ Dialog sang List→Detail+Export PDF, field gốc tham khảo `22_RecordStaffViolation.md` (Hình 2); (2) gộp "chọn món nghi vấn" với "thu hồi mẫu lưu"; (3) sửa nhãn Section B trùng lặp; (4) bỏ dropdown "Thông tin sự cố" + field "Tường trình ban đầu" dư thừa; (5) áp cặp nút "Lưu nháp/Chốt kết luận" thống nhất cho B/C/D; (6) liên kết 2 chiều Investigation ↔ Violation Registry.

Đã đối chiếu với quyết định cũ trước khi chốt: `review_medicalstaff_incidentmanagement.md` Mục 11.3.3 từng chốt biên bản sự cố (`33`) là **tài liệu triage nội bộ, không cần gate ký-ảnh BR-211** (khác biên bản vi phạm). Yêu cầu (1) có nguy cơ biến nó thành văn bản bán chính thức thứ 2 — đã hỏi lại người dùng qua `AskUserQuestion`, xác nhận **giữ nguyên quyết định cũ** (không ký, không thêm field Đại diện Nhà thầu).

### 5.2. Quyết định đã xác nhận (18/07/2026, qua `AskUserQuestion`)

1. **`33` không áp gate ký-ảnh BR-211** — vẫn là tài liệu nội bộ, PDF xuất ra chỉ để tham chiếu/lưu trữ, không có khối chữ ký 2 bên. Văn bản pháp lý chính thức duy nhất của 1 sự cố vẫn là báo cáo cuối ở Section D (UC-033, BR-154).
2. **Không thêm field "Đại diện Nhà thầu"** vào `33` — giữ nguyên field hiện có; nhà thầu liên quan (nếu có) đã được xác định gián tiếp qua món ăn nghi vấn ở Section B.
3. **Gộp "Đánh giá mức độ" + "Chọn món nghi vấn" thành 1 Section B duy nhất** — không tách thành 5 section A–E, giữ nguyên 4 section A–D.

### 5.3. Quyết định bổ sung (theo đúng yêu cầu người dùng, không phát sinh xung đột SRS nên không hỏi lại thêm)

4. **Thêm 2 field mới vào `33`**: "Địa điểm xảy ra sự cố" (text) và "Thời điểm xảy ra sự cố" (`occurredAt`, DateTimePicker, khác "Thời điểm báo cáo" đã có) — lấy cấu trúc từ `specialreport2173_1.md`/Hình 2, dùng để tự sinh câu mở đầu kiểu "Vào hồi [giờ] ngày [ngày], tại [địa điểm]..." trong nội dung PDF. Không kèm khối chữ ký (theo Quyết định 1).
5. **Thêm nút "Xuất PDF"** ở `33`, xuất hiện sau khi "Hoàn tất biên bản" (cùng nhịp với pattern Save→Export đã dùng ở `22`) — tiêu đề PDF **"BIÊN BẢN SỰ CỐ AN TOÀN THỰC PHẨM"** (không phải "BIÊN BẢN VI PHẠM"), không có khối chữ ký.
6. **Quarantine (BR-149) tách khỏi cơ chế draft/final** — tick chọn món nghi vấn ở Section B kích hoạt khoá lô **ngay lập tức**, bất kể sau đó bấm "Lưu nháp" hay "Chốt kết luận". Căn cứ: BR-149 nguyên văn "hệ thống **tự động** đặt Quarantine khi sự cố được báo cáo" — không có căn cứ nào để trì hoãn khoá lô theo trạng thái nháp/chốt của phần đánh giá.
7. **Cặp nút "Lưu nháp"/"Chốt kết luận" áp dụng thống nhất cho Section B, C, D** — khái quát hoá đúng mô hình "Sơ bộ → Chính thức" đã dùng riêng cho root cause (Mục 11.9.1 `review_medicalstaff_incidentmanagement.md`) sang toàn bộ 3 section. Chỉ cần "Lưu nháp" là đủ để mở section kế tiếp (không khoá sequential cứng); "Chốt kết luận" mới khoá section đó thành read-only.
8. **Gộp hành động "thu hồi mẫu"** vào ngay Section B (cạnh mỗi món được tick, mở `14_RetrieveFoodSampleRecordsDialog.md` tại chỗ) — panel "1. Thu hồi mẫu lưu" ở Section C chuyển thành **log tổng hợp chỉ đọc** (không còn là nơi thao tác chính, vẫn hiển thị trạng thái mẫu đã thu hồi cho case).
9. **Bỏ dropdown "Thông tin sự cố (UC-116)"** và **bỏ field "Tường trình ban đầu"** (field 7 cũ của `26`) — Section A chỉ còn 1 dòng link "Xem Biên bản gốc (PDF)" mở file đã xuất ở `33`, không hiển thị lại nội dung trong app.
10. **Liên kết 2 chiều Investigation ↔ Violation Registry**: `22_RecordStaffViolation.md`/`27_ViolationRegistry.md` khi mở với `relatedIncidentId` hiển thị nút "Quay lại điều tra [INC-xxx]"; nút "Lập biên bản vi phạm nhân viên liên quan" ở Section C đổi thành "Xem biên bản vi phạm [VIO-xxx]" nếu đã tồn tại bản ghi liên kết.

### 5.4. Hệ quả dây chuyền cần sửa kèm (do bỏ field "Tường trình ban đầu")

`20_GenerateIncidentReportDialog.md` field 6 "Clinical notes" hiện pre-fill từ "Tường trình ban đầu" (field 7 cũ của `26`, nay đã bỏ — Quyết định 9). Nguồn pre-fill duy nhất còn lại là nội dung biên bản gốc đã chốt ở `33` (field 5, "Nội dung biên bản") — cần đổi tham chiếu pre-fill sang đúng nguồn này.

### 5.5. Checklist thay đổi cần áp dụng theo file (cập nhật, nối tiếp Mục 4)

| File | Thay đổi cần áp dụng |
| --- | --- |
| `33_CreateIncidentRecordDialog.md` | Đổi từ Dialog sang route Detail riêng (`/incidents/records/[id]` hoặc tương đương); thêm field "Địa điểm xảy ra sự cố" + "Thời điểm xảy ra sự cố"; thêm nút "Xuất PDF" sau "Hoàn tất biên bản" (không khối chữ ký); DRAFTING = editable toàn trang, DRAFTED = view-only trừ nút Export/Mở phiên điều tra/Đóng; giữ nút "Thêm báo cáo nguồn" chỉ khi còn DRAFTING (Quyết định 4–5, Mục 5.3) |
| `25_IncidentList.md` | Tab "Biên bản chờ xử lý": nút "Xem chi tiết" điều hướng sang route Detail mới của `33` thay vì mở lại modal (Mục 5.3, điểm liên quan #5) |
| `26_IncidentDetail.md` | Section A: bỏ dropdown "Thông tin sự cố", chỉ còn link PDF; bỏ field "Tường trình ban đầu". Section B: gộp "Đánh giá mức độ" + "Chọn món nghi vấn" thành 1 dropdown; thêm nút "Thu hồi mẫu" cạnh mỗi món tick; quarantine kích hoạt ngay khi tick (không chờ Lưu nháp/Chốt kết luận); thêm cặp nút Lưu nháp/Chốt kết luận (Quyết định 6–9, Mục 5.3) |
| `17_InvestigationWorkspace.md` (nay là Section C của `26`) | Panel 1 "Thu hồi mẫu lưu" chuyển thành log chỉ đọc; áp cặp Lưu nháp/Chốt kết luận cho Gathering Evidence + Remedial Measures (Root Cause đã có sẵn Sơ bộ/Chính thức từ Mục 11.9.1) (Quyết định 7–8, Mục 5.3) |
| `20_GenerateIncidentReportDialog.md` | Đổi nguồn pre-fill "Clinical notes" (field 6) từ "Tường trình ban đầu" (đã bỏ) sang nội dung biên bản gốc ở `33` field 5 (Mục 5.4) |
| `22_RecordStaffViolation.md` / `27_ViolationRegistry.md` | Thêm nút "Quay lại điều tra [INC-xxx]" khi mở với `relatedIncidentId` context (Quyết định 10) |

Không phát sinh BR/UC mới ở vòng này — field mới (Địa điểm/Thời điểm xảy ra sự cố) nằm trong phạm vi BR-154 đã có (nội dung biên bản/báo cáo sự cố), không cần cấp mã BR riêng.

---

## 6. Trạng thái áp dụng VÒNG 1 (Mục 1–4) — ĐÃ ÁP CODE 18/07/2026 (`tsc --noEmit` sạch)

*(Đánh lại số từ "Mục 5" cũ ngày 19/07/2026 — trùng số với "Mục 5. Vòng rà soát thứ 2" được chèn vào trước đó.)*

2 dòng "Refactor code" của Mục 4 đã thực hiện trên mock frontend; 4 dòng Screen Spec (`26`/`17`/`19`/`20`) **CHƯA áp**, chờ lượt áp docs riêng.

| Việc | File code | Chi tiết đã áp |
| --- | --- | --- |
| Gộp `17` → Section C (QĐ 1) | `incidents/[id]/page.tsx` | Toàn bộ nội dung 4 panel + banner "đang chờ ai" của `17` chuyển vào panel "C — Không gian điều tra (UC-041)" (CollapsiblePanel dùng chung Mục 12.8.1, panel con lồng bên trong); state/hàm (`saveEvidence`/`saveRootCause`/`establishMeasures`, query mẫu theo `incidentId`) gộp về Detail; root cause/lab dùng CHUNG state với Section D (cùng 1 dữ liệu — không nhập lại, Mục 11.9.1); C tự mở khi vào INVESTIGATING, khoá (`fieldset disabled`) khi đã giao CAPA (state ≥ 4) |
| Route `/investigation` | `investigation/page.tsx` | **Điều chỉnh theo yêu cầu người dùng 18/07 (giữa phiên): KHÔNG xoá hẳn file** — chuyển thành stub `@deprecated`: ngắt toàn bộ logic/API, route chỉ còn `router.replace` về `/incidents/[id]` (Section C); khớp tiền lệ "Deprecated as standalone, giữ file tham chiếu lịch sử" của Mục 4 dòng 2 |
| Section B khoá thành tóm tắt | `incidents/[id]/page.tsx` | Sau "Lưu đánh giá & Bắt đầu điều tra": panel "B — Đánh giá & chuẩn bị điều tra (đã khoá)" hiển thị tóm tắt read-only (severity, món→lô, đánh giá rủi ro, tường trình); 2 khối B ở trạng thái sửa giữ nguyên thiết kế Mục 12.8.3 (tách trực quan, 1 nút duy nhất) |
| Section D tại chỗ | `incidents/[id]/page.tsx` | Dialog "Hoàn tất sự cố" (Mục 10.7) chuyển thành panel "D — Hoàn tất sự cố (UC-168 + UC-033)" trên cùng route: Block (A) checklist hồi phục (BR-155) → mở khoá Block (B) soạn báo cáo (BR-154); gate CAPA APPROVED giữ nguyên (#22/BR-153); bảng field 1–2 của `19` không tồn tại trong code (đã đúng QĐ 4 — chỉ 1 List qua `25`) |
| Khoá read-only sau Export (QĐ 2+5) | `mock-data.ts`, `incidents/[id]/page.tsx` | Field mới `Incident.reportExported` (cờ "Đã xuất báo cáo — Read-only", chỉ gate UI, không BR mới); "Export & Submit Incident Reports" thành công → bật cờ → badge khoá trên dải tóm tắt + banner + toàn bộ section `fieldset disabled`, KHÔNG có cơ chế mở khoá lại; đóng case chính thức vẫn thuộc School Administration (BR-155/UC-013) |
| Link ngược từ Sổ lưu mẫu | `samples/page.tsx` | Nút "Quay lại Không gian điều tra" đổi thành "Quay lại chi tiết sự cố (INC-xxx) — Section C" (Bug 2/Mục 7.6 loại A giữ nguyên context `?incident=`) |

Ghi chú hành vi: panel C khoá từ state ≥ 4 nhưng vẫn mở/đóng để XEM lại (chỉ input/nút bị disable); action list cột phải đổi hướng dẫn theo section tại chỗ (không còn nút điều hướng route riêng); `33_CreateIncidentRecordDialog` giữ nguyên tách biệt phía List (QĐ 3 — không đụng code).

---

## 7. Trạng thái áp dụng VÒNG 2 (Mục 5, QĐ 1–10) — ĐÃ ÁP CODE 19/07/2026 (`tsc --noEmit` sạch)

Phần code mock của checklist Mục 5.5 đã thực hiện; các dòng Screen Spec (`33`/`25`/`26`/`17`/`20`/`22`/`27`) **CHƯA áp docs**, chờ lượt áp docs riêng.

| QĐ | File code | Chi tiết đã áp |
| --- | --- | --- |
| 1+2+4+5 — `33` thành route Detail | `incidents/records/[id]/page.tsx` (MỚI), `mock-data.ts` | Route Detail riêng thay Dialog (List → Detail sửa trực tiếp → Export PDF): DRAFT = editable toàn trang, COMPLETED = view-only trừ Xuất PDF/Mở phiên điều tra/Đóng; 2 field mới `location`/`occurredAt` (tự sinh câu mở đầu "Vào hồi… tại…"); nút "Xuất PDF" chỉ sau Hoàn tất — tiêu đề "BIÊN BẢN SỰ CỐ AN TOÀN THỰC PHẨM", KHÔNG khối chữ ký/BR-211, KHÔNG field Đại diện Nhà thầu; "Thêm báo cáo nguồn" chỉ khi DRAFT; tick/bỏ tick nguồn giữ nguyên Mục 12.7.2 |
| Luồng List | `incidents/page.tsx`, `create-incident-record-dialog.tsx` | "Tạo báo cáo sự cố" tạo ngay bản nháp DRAFT (RPT → DRAFTING) rồi điều hướng sang route Detail; "Tiếp tục soạn" điều hướng đúng nháp; tab "Biên bản chờ xử lý" thêm nút "Xem chi tiết" → route Detail (không mở lại modal); dialog 33 đánh dấu `@deprecated`, giữ trong repo |
| 3+9 — Section A/B của `26` | `incidents/[id]/page.tsx` | Section A rút còn 1 DÒNG link "Xem Biên bản gốc (PDF)" → route 33 (bỏ dropdown "Thông tin sự cố (UC-116)", không hiển thị lại nội dung trong app); Section B GỘP "Đánh giá mức độ" + "Chọn món nghi vấn" thành 1 panel duy nhất (giữ 4 section A–D); field "Tường trình ban đầu" bỏ hẳn (cả bản tóm tắt B đã khoá) |
| 6 — Quarantine tức thì | `incidents/[id]/page.tsx` | `toggleDishQuarantine`: tick món → mutate `suspectedDishIds`+`quarantineBatches` NGAY (toast khoá/nhả lô) — tách hoàn toàn khỏi Lưu nháp/Chốt kết luận (nguyên văn BR-149 "tự động đặt Quarantine") |
| 7 — cặp nút thống nhất | `incidents/[id]/page.tsx`, `mock-data.ts` | B: "Lưu nháp" (không validate, set `assessmentDraftSaved` → Section C mở ngay cả khi chưa chốt — không khoá sequential cứng) / "Chốt kết luận — Bắt đầu điều tra" (validate, INVESTIGATING); C2: "Lưu nháp (Save Evidence)" / "Chốt kết luận" (`investigationEvidenceFinal` → fieldset khoá); C4: "Lưu nháp" (`investigationMeasuresDraft`, KHÔNG tạo CAPA) / "Chốt kết luận — Establish Measures" (CAPA + SLA 12h — thay quyết định Mục 12.8.6 cũ theo QĐ 7 mới hơn); C3 giữ nguyên Sơ bộ/Chính thức sẵn có; D: "Lưu nháp" (`officialClinicalNotes`) / "Chốt kết luận — Export & Submit" (khoá toàn trang QĐ 2) |
| 8 — thu hồi mẫu tại chỗ | `incidents/[id]/page.tsx` | Nút "Thu hồi mẫu" cạnh từng món đã tick ở Section B → dialog 14 inline (Incident ID khoá sẵn; field BR-212: lab, đại diện nhận mẫu, chỉ tiêu xét nghiệm; mutate FoodSample → RETRIEVED); panel C1 chuyển thành "Nhật ký thu hồi mẫu lưu — chỉ đọc" (bỏ nút điều hướng sang Sổ lưu mẫu) |
| 10 — liên kết 2 chiều | `incidents/[id]/page.tsx`, `violations/page.tsx` | C3: nút đổi thành "Xem biên bản vi phạm [VIO-xxx]" khi đã có bản ghi `relatedIncidentId` khớp case; Sổ vi phạm: mở với `?incident=` → nút "Quay lại điều tra [INC-xxx]" trên header |
| Hệ quả 5.4 | `incidents/[id]/page.tsx` | Clinical notes (Section D) đổi nguồn pre-fill: nháp `officialClinicalNotes` → nội dung biên bản gốc đã chốt ở 33 (strip HTML), KHÔNG còn từ "Tường trình ban đầu" (đã bỏ) |

Ghi chú hành vi: `Incident.firstAidNotes` vẫn được ghi lúc "Mở phiên điều tra" (giữ tương thích dữ liệu cũ) nhưng KHÔNG hiển thị/sửa ở đâu nữa; validation Chốt kết luận B còn: severity + đánh giá rủi ro (Med/High) + ≥1 món (bỏ điều kiện tường trình); route `/investigation` vẫn là stub redirect (vòng 1).

---

## 8. Vòng rà soát thứ 3 (19/07/2026, qua 6 ảnh chụp code thật của Vòng 1+2 đã áp) — sửa luồng tạo biên bản, 4 bug xác nhận

### 8.1. Bối cảnh

Người dùng test trực tiếp code đã áp ở Mục 6–7 và cung cấp 6 ảnh: List "Chờ tiếp nhận" (Hình 1), màn soạn biên bản đang Nháp (Hình 2), List ở trạng thái đã tick 3 dòng lẫn cả DRAFTING lẫn PENDING_TRIAGE (Hình 3), Incident Detail Section A/B/C (Hình 4), biên bản đã "Đã mở phiên điều tra" — chỉ đọc (Hình 5), panel Root Cause/CAPA với nút "Xem biên bản vi phạm VIO-117" (Hình 6). Phát hiện: 1 câu hỏi thiết kế cần quyết định lại (luồng tạo biên bản từ nhiều báo cáo), và ít nhất 1 bug thực thi **sai lệch so với chính quyết định đã ghi ở Mục 6** (không phải yêu cầu mới).

### 8.2. Câu hỏi thiết kế: tick-nhiều-rồi-tạo (Hình 1) so với vào-1-báo-cáo-rồi-thêm (Hình 2)

**Vấn đề xác nhận qua Hình 3**: cơ chế hiện tại (tick nhiều dòng bất kể trạng thái, bấm 1 nút chung "Tạo báo cáo sự cố"/"Tiếp tục soạn biên bản") bị **ambiguous** khi tick lẫn dòng đã `DRAFTING` (đã thuộc 1 nháp có sẵn) với dòng còn `PENDING_TRIAGE` (chưa thuộc nháp nào) — hệ thống không có cách nào biết dòng mới tick nên nhập vào **nháp nào** nếu đang có nhiều nháp song song.

**SỬA 19/07/2026 (đợt 2, theo phản hồi người dùng — chỉ chọn 1 cách, không giữ song song)**: bản trước đề xuất giữ cả 2 lối vào (tick-List + tạo-từ-chi-tiết-1-báo-cáo). Xét lại: một khi checkbox ở List đã giới hạn đúng phạm vi (chỉ `PENDING_TRIAGE`), thì "tick đúng 1 dòng rồi tạo" và "mở chi tiết 1 báo cáo rồi tạo" cho ra **kết quả giống hệt nhau** (1 nháp chỉ gồm báo cáo đó) — giữ 2 nút cho cùng 1 kết quả là dư thừa, không phải linh hoạt.

**Quyết định cuối (chỉ 1 lối vào)**:
1. **Checkbox ở List là cách DUY NHẤT để tạo mới**, chỉ tick được các dòng `PENDING_TRIAGE` (dòng `DRAFTING` ẩn checkbox, chỉ còn "Xem chi tiết" dẫn thẳng vào route Detail của nháp đang dở). Tick 1 hoặc nhiều dòng → "Tạo báo cáo sự cố" → luôn tạo 1 nháp mới. Lý do chọn nhánh này thay vì "vào chi tiết rồi tạo": List đã đủ cột (học sinh, lớp, triệu chứng, bữa ăn, thời điểm) để nhận diện báo cáo liên quan mà không cần mở từng cái — nhanh hơn cho đúng tình huống BR-146 mô tả (≥3 học sinh cùng lúc, vốn là nhiều báo cáo cùng lúc).
2. **Modal "Xem chi tiết" của 1 báo cáo `PENDING_TRIAGE` giữ nguyên vai trò chỉ đọc/tham khảo** (đúng vai trò gốc đã chốt ở Mục 9.13 `review_medicalstaff_incidentmanagement.md`) — **không** thêm nút "Tạo báo cáo sự cố" trong modal này, tránh có 2 nơi làm cùng 1 việc.
3. **"Thêm báo cáo liên quan"** (nút bên trong route Detail của `33`) **không phải lối vào tạo mới** — đây thuần tuý là tính năng **sửa 1 nháp đã tồn tại** (đúng cơ chế đã chốt ở Mục 12.7 `review_medicalstaff_incidentmanagement.md`: chỉ dùng được khi còn DRAFT, có nút "X" xoá từng báo cáo đã thêm).

Cách này triệt tiêu hẳn ambiguous ở Hình 3 (vì checkbox chỉ còn tác dụng trên tập `PENDING_TRIAGE`, không còn lẫn với dòng đã có nháp), đồng thời không còn 2 UI trùng chức năng.

### 8.3. Bug xác nhận (sai lệch so với quyết định đã ghi, không phải yêu cầu mới)

1. **Section C không mở lại được để xem sau khi đã chốt (Hình 4)** — mâu thuẫn trực tiếp với chính ghi chú đã có ở Mục 6: *"panel C khoá từ state ≥ 4 nhưng vẫn mở/đóng để XEM lại (chỉ input/nút bị disable)"*. Ảnh chụp thực tế cho thấy dropdown con không mở được — nhiều khả năng `fieldset disabled` đang bọc luôn phần toggle mở/đóng thay vì chỉ bọc input/nút bên trong. **Đây là bug thực thi, không phải quyết định cần đổi** — dev cần tách riêng: `disabled` chỉ áp cho input/nút hành động, phần Collapsible header (mở/đóng) không được nằm trong `fieldset`.
2. **Section A "không có biên bản gốc" ở 1 số case (Hình 4)** — khả năng mock data (`mock-data.ts`) đang seed sẵn 1 số `Incident` không đi qua đúng luồng `33` (tạo thẳng, không qua bước biên bản). Đối chiếu subtitle chính ứng dụng đã tự ghi: *"Luồng 2 bước... BR-146 là tín hiệu, gộp do Medical Staff chủ động"* — nghĩa là BR-146 (≥3 học sinh/24h) chỉ là cảnh báo, không tự động bỏ qua bước soạn biên bản. Vậy **mọi `Incident` hợp lệ đều phải có biên bản gốc liên kết** — trường hợp thiếu là lỗi dữ liệu mock/seed, không phải 1 nhánh nghiệp vụ mới cần thiết kế thêm.
3. **Nút "Về danh sách" ở màn biên bản (Hình 5) cần đổi động theo trạng thái** — hiện luôn hiển thị "Về danh sách" bất kể biên bản đã gắn `incidentId` hay chưa. Sau khi biên bản chuyển `MERGED` (đã mở phiên điều tra), nút phải đổi thành **"Về phiên điều tra [INC-xxx]"** (deep-link sang `26`) — vì lúc này bối cảnh làm việc tự nhiên của Medical Staff là quay lại điều tra, không phải quay lại danh sách báo cáo thô.
4. **"Quay lại điều tra" từ Sổ vi phạm (Hình 6) cần khôi phục đúng section đang mở** — hiện tại (Mục 7, dòng "Link ngược từ Sổ lưu mẫu") mới xử lý cho luồng thu hồi mẫu (`?incident=`), chưa xử lý cho luồng "Lập biên bản vi phạm nhân viên liên quan" ở Section C panel Root Cause. Cần: khi điều hướng sang Sổ vi phạm từ Section C, truyền kèm anchor (VD `?incident=INC-xxx&section=C`); nút "Quay lại điều tra" đọc lại anchor này để tự mở đúng Section C thay vì về trang mặc định (mọi section đóng).

### 8.4. Checklist thay đổi cần áp dụng theo file

| File / Component | Thay đổi cần áp dụng |
| --- | --- |
| `25_IncidentList.md` + `incidents/page.tsx` | Tab "Chờ tiếp nhận": ẩn checkbox ở dòng `DRAFTING` (chỉ còn "Xem chi tiết" → route Detail của nháp đó); checkbox chỉ áp cho `PENDING_TRIAGE`; "Tạo báo cáo sự cố" là **lối vào tạo mới duy nhất** (1 hoặc nhiều dòng đều được), luôn tạo nháp MỚI; modal "Xem chi tiết" giữ read-only, **không** thêm nút tạo trong đó (Mục 8.2, QĐ 1–2) |
| `33_CreateIncidentRecordDialog.md` + `incidents/records/[id]/page.tsx` | Nút "Thêm báo cáo liên quan" **không phải lối vào tạo mới** — chỉ dùng để sửa 1 nháp đã tồn tại (đúng Mục 12.7, chỉ khi DRAFT); mỗi báo cáo đã thêm có nút "X" để bỏ (Mục 8.2, QĐ 3); nút "Về danh sách" đổi động thành "Về phiên điều tra [INC-xxx]" khi `status = MERGED` (Mục 8.3, bug 3) |
| `26_IncidentDetail.md` + `incidents/[id]/page.tsx` | Fix bug: tách `disabled` ra khỏi phần toggle mở/đóng Collapsible của Section C — giữ đúng hành vi đã ghi ở Mục 6 ("khoá input, không khoá xem") (Mục 8.3, bug 1); khi điều hướng sang Sổ vi phạm từ panel Root Cause, truyền anchor `?incident=INC-xxx&section=C` (Mục 8.3, bug 4) |
| `22_RecordStaffViolation.md` / `violations/page.tsx` | Đọc anchor `section=C` từ query, nút "Quay lại điều tra" mở đúng Section C thay vì trang mặc định (Mục 8.3, bug 4) |
| `mock-data.ts` | Rà soát toàn bộ seed `Incident` — đảm bảo mọi case đều có `IncidentRecordDraft` gốc liên kết đúng qua luồng `33` (Mục 8.3, bug 2) |

Không phát sinh BR/UC mới — toàn bộ Mục 8 là sửa bug thực thi (1, 2, 4) và làm rõ lại 1 luồng UX đã có sẵn cơ chế nền (3, dựa trên Mục 12.7 cũ), không đổi field/gate nghiệp vụ nào đã chốt ở Mục 5.

---

## 9. Trạng thái áp dụng VÒNG 3 (Mục 8) — ĐÃ ÁP CODE 19/07/2026 (`tsc --noEmit` sạch)

Phần code mock của checklist Mục 8.4 đã thực hiện; các dòng Screen Spec (`25`/`33`/`26`/`22`) **CHƯA áp docs**, chờ lượt áp docs riêng.

| Mục | File code | Chi tiết đã áp |
| --- | --- | --- |
| 8.2 QĐ 1–2 — 1 lối vào tạo mới | `incidents/page.tsx` | Dòng `DRAFTING` ẨN checkbox + không click-chọn được; "Xem chi tiết" của dòng DRAFTING dẫn thẳng route Detail của nháp (`records/[draftRecordId]`); checkbox chỉ trên `PENDING_TRIAGE`; "Tạo báo cáo sự cố" LUÔN tạo nháp mới (bỏ nhánh "Tiếp tục soạn biên bản" + logic gom theo draftIds — hết ambiguous Hình 3); modal "Xem chi tiết" giữ read-only, KHÔNG thêm nút tạo |
| 8.2 QĐ 3 — Thêm báo cáo liên quan | `incidents/records/[id]/page.tsx` | Đổi nhãn "Thêm báo cáo liên quan" (rõ vai trò sửa nháp, không phải lối tạo mới); mỗi báo cáo VỪA THÊM (chưa lưu) có nút "X" bỏ lại; báo cáo nguồn đã lưu vẫn bỏ bằng untick (Mục 12.7 — trả về PENDING_TRIAGE khi lưu) |
| 8.3 bug 1 — toggle C bị khoá | `incidents/[id]/page.tsx` | Gỡ `fieldset disabled` tầng ngoài (từng bọc luôn nút toggle panel con) → thay bằng `div`; `disabled` (cLocked = state ≥ 4 ∨ isLocked) chỉ áp cho input/nút BÊN TRONG C2/C3/C4 (fieldset con); C1 chỉ đọc sẵn; link "Xem biên bản vi phạm" nằm ngoài fieldset — đúng ghi chú Mục 6 "khoá input, không khoá xem" |
| 8.3 bug 2 — thiếu biên bản gốc | `mock-data.ts` | Seed `INCIDENT_RECORDS` đủ 1 REC/1 INC (REC-2026-021/019/018, status OPENED + `incidentId` + location/occurredAt); bổ sung 2 RPT lịch sử MERGED (RPT-2026-024/018) làm nguồn cho INC-2026-013/012 + set `sourceReportIds`/`mealDate` cho 2 INC này — mọi Incident đều đi qua luồng `33`, Section A không còn "không có biên bản gốc" |
| 8.3 bug 3 — nút Về động | `incidents/records/[id]/page.tsx` | `status = OPENED` (RPT đã MERGED) → nút đổi thành "Về phiên điều tra [INC-xxx]" (deep-link `26`); các trạng thái khác giữ "Về danh sách" |
| 8.3 bug 4 — anchor section | `incidents/[id]/page.tsx`, `violations/page.tsx` | Link sang Sổ vi phạm từ panel Root Cause truyền `?incident=INC-xxx&section=C`; nút "Quay lại điều tra" đọc `section` và gắn lại `?section=C`; Incident Detail đọc `?section=C` → Section C `defaultOpen` (khôi phục đúng section đang mở) |

Ghi chú: nút "Đóng — Không liên quan ATTP" ở List giữ nguyên điều kiện 1 dòng PENDING_TRIAGE (Nhóm F1); route `/investigation` vẫn là stub redirect (vòng 1).

---

## 10. Vòng rà soát thứ 4 (19/07/2026) — sửa 2 gate an toàn bị hở (P0), gộp ý nghĩa "Export", case-workspace UX. **CHỈ ÁP DỤNG WEB — Mobile để riêng, chưa đụng.**

### 10.1. Bối cảnh

Người dùng test sâu code Vòng 1–3 (đối chiếu trực tiếp từng dòng trong `incidents/[id]/page.tsx`) và phát hiện 2 lỗi mức P0 (ảnh hưởng an toàn/tính đầy đủ bằng chứng), 2 lỗi P1 (dễ nhầm lẫn UX), cùng 1 đề xuất thiết kế lại Detail thành "case workspace có hướng dẫn" thay vì 4 accordion ngang hàng. Tôi đã verify từng dòng được trích trước khi ghi nhận.

**Nhận trách nhiệm rõ ràng**: P0-1 là hậu quả trực tiếp của **QĐ 7 (Mục 5.3, Vòng 2)** — quyết định đó tổng quát hoá mô hình "Sơ bộ → Chính thức" (vốn chỉ hợp lý cho 1 field root cause, Mục 11.9.1) thành "Lưu nháp đủ mở section kế tiếp" áp dụng đều cho cả Section B, mà không lường hết hậu quả: Section B là cổng an toàn (severity + trigger quarantine), không phải 1 field đơn lẻ. Đây là lỗi thiết kế của chính vòng review trước, không phải lỗi thực thi code.

### 10.2. Bug xác nhận (đã verify trực tiếp trong code, kèm số dòng)

1. **P0 — Section C mở được (và thao tác được) dù Section B chưa chốt**: `incidents/[id]/page.tsx` dòng 815 (`state >= 3 || Boolean(incident.assessmentDraftSaved)`) + dòng 330–344 (`saveDraftAssessment` không validate severity/món) → "Lưu nháp" mở toàn bộ Section C có thể thao tác được (vì `cLocked = state >= 4 || isLocked`, dòng 254, vẫn `false` lúc này) — vi phạm gate BR-147/149 mà chính Mục 2 (Vòng 1) yêu cầu giữ nguyên.
2. **P0 — Section C bị khoá đồng loạt theo `cLocked` (state ≥ 4), kể cả C3 Root Cause**: dòng 950 (`fieldset disabled={cLocked}`) không có cờ khoá riêng nào khác cho C3 — mâu thuẫn trực tiếp với chính Mục 11.9.1 (root cause "sơ bộ" đủ để giao CAPA trong 12h, "chính thức" chỉ có **sau** khi lab về, 48h+, tức **sau** khi CAPA đã pending). C2 (Evidence) đã có cờ khoá riêng `investigationEvidenceFinal` (dòng 891) nhưng vẫn bị `|| cLocked` ăn theo nên cũng khoá sớm không cần thiết.
3. **P1 — 2 ý nghĩa "Export" cùng tồn tại**: nút "Export" ở `PageHeader` (dòng 556–561) là tàn dư từ field 14 gốc của `26_IncidentDetail.md` (trước khi có Section D) — luôn bật bất kể trạng thái case, chỉ toast mock, không liên quan gì tới "Chốt kết luận — Export & Submit Incident Reports" (dòng 1297, hành động chính thức thật của Section D).
4. **P1 — Bỏ tick món nghi vấn nhả quarantine ngay, không xác nhận/lý do**: `toggleDishQuarantine` (dòng 308–323) xử lý tick và bỏ tick đối xứng nhau — trong khi 2 hướng có mức rủi ro khác nhau hoàn toàn (khoá lô cần nhanh vì an toàn; nhả lô cần thận trọng vì có thể nhả nhầm lô còn nghi vấn).

### 10.3. Quyết định (19/07/2026, Web only)

1. **Section B — bỏ hẳn cơ chế mở sớm Section C qua `assessmentDraftSaved`**: xoá nhánh `|| Boolean(incident.assessmentDraftSaved)` ở điều kiện render Section C — Section C chỉ xuất hiện khi `state >= 3` (tức đã bấm "Chốt đánh giá & Bắt đầu điều tra", validate đủ severity + ≥1 món). "Lưu nháp" ở Section B từ nay **chỉ persist form đang gõ dở** (severity/assessment/dish tick tạm) để khôi phục khi quay lại, **không mở/hiện thêm gì khác**. Chọn phương án đơn giản hơn (so với phương án "ô Ghi chú điều tra sơ bộ" đề xuất) vì ít trạng thái trung gian hơn, đúng nguyên văn ý định gốc Mục 2 (Vòng 1) — người dùng không phản đối khi được hỏi lại nên chọn phương án này làm quyết định chính thức.
2. **Tách granularity khoá Section C theo đúng bản chất từng panel** (không dùng chung `cLocked` cho cả 3):
   - **C2 (Gathering Evidence)**: chỉ khoá theo cờ riêng `investigationEvidenceFinal` — bỏ `|| cLocked`.
   - **C3 (Root Cause)**: chỉ khoá khi `rootStatus === "FINAL"` — bỏ hẳn phụ thuộc `cLocked`. Cho phép cập nhật root cause (kể cả sau khi CAPA đã CAPA_PENDING/APPROVED) đúng đúng luồng "Sơ bộ → Chính thức" đã đặc tả từ Mục 11.9.1.
   - **C4 (Remedial Measures/CAPA)**: **giữ nguyên** khoá theo `cLocked` (`state >= 4`) — đây là hành động đã gửi đi có SLA thật (12h, BR-152), không nên cho sửa ngược sau khi đã giao.
3. **Section D Block B — thêm điều kiện `rootStatus === "FINAL"`** vào disabled-check của nút "Chốt kết luận — Export & Submit Incident Reports" (hiện chỉ check `clinicalNotes`/`rootCause` không rỗng) — không cho xuất báo cáo chính thức khi root cause còn ở trạng thái sơ bộ, đúng đề xuất người dùng ("chỉ cho xuất khi Root Cause là FINAL").
4. **Bỏ nút "Export" ở header** (`PageHeader` actions, dòng 556–561) — không có căn cứ UC/BR riêng cho 1 nút export chung chung tách biệt khỏi Section D (tương tự lý do đã bỏ nút Export ở `27_ViolationRegistry.md`, Mục 12.2 `review_medicalstaff_incidentmanagement.md`). Nếu về sau cần "tải hồ sơ tham khảo" (khác báo cáo chính thức), thêm lại có tên rõ ràng kèm căn cứ cụ thể, không phục hồi nút mơ hồ này.
5. **Bỏ tick món nghi vấn (nhả quarantine) cần dialog xác nhận + lý do**: giữ **bất đối xứng có chủ đích** — tick (khoá lô) vẫn tức thì, không cần xác nhận (ưu tiên tốc độ an toàn thực phẩm — đã chốt ở QĐ 6 Vòng 2, giữ nguyên); bỏ tick (nhả lô) thêm `Dialog` xác nhận, bắt buộc nhập lý do điều chỉnh, hiển thị rõ (các) lô sẽ được nhả trước khi xác nhận — nội dung lý do ghi vào audit log chung (BR-170).
6. **Case-workspace UX — áp dụng theo đề xuất người dùng, có tinh chỉnh**:
   - Thêm 1 dải trạng thái cố định đầu trang (sticky): 4 giai đoạn **Đánh giá → Điều tra → CAPA → Báo cáo**, kèm 1 CTA chính duy nhất theo trạng thái hiện tại (VD "Chốt đánh giá & bắt đầu điều tra" / "Gửi CAPA chờ duyệt" / "Xem trước & nộp báo cáo"), thay cho việc để Medical Staff tự đoán nên mở panel nào.
   - **Tinh chỉnh (phản biện)**: sau khi CAPA đã gửi, Section C có 2 nhánh chạy song song (chờ School Admin duyệt CAPA, và tiếp tục bổ sung bằng chứng/nâng root cause) — nếu chỉ hiện đúng 1 CTA chính dễ khiến Medical Staff tưởng hết việc. Panel C3 (Root Cause) cần 1 badge/nhắc phụ riêng (VD "Còn ở trạng thái Sơ bộ — cập nhật khi có kết quả lab") độc lập với CTA chính ở header, không dồn hết vào 1 nút.
   - `CollapsiblePanel` (component dùng chung) luôn hiện 1 dòng tóm tắt trạng thái kể cả khi đang MỞ (hiện tại chỉ hiện summary lúc đóng) — sửa ở tầng component dùng chung `collapsible-panel.tsx`, áp dụng cho mọi nơi đang dùng component này, không riêng Incident Detail.
   - Đưa mã `BR-xxx`/`UC-xxx`/`QĐ-x` vào tooltip/khối "Căn cứ" (giữ để truy vết, nhưng không đặt trong nhãn nút chính) — nhãn nút chính dùng thuần tiếng Việt (VD "Lưu nháp", "Chốt đánh giá & bắt đầu điều tra", "Xác nhận nguyên nhân chính thức").
   - "Upload tài liệu"/"Ảnh bằng chứng" hiện là mock tăng bộ đếm (`evidenceFiles`, không lưu file thật) — **ghi nhận là giới hạn của giai đoạn mock/prototype, không phải thiếu sót thiết kế**; cần backend thật (lưu tên file/dung lượng/người tải/thời điểm) trước khi coi là đáp ứng vận hành production. Không chặn các quyết định khác của vòng này.
7. **Không đụng Mobile ở vòng này** (theo yêu cầu người dùng 19/07/2026) — `Mobile/MedicalStaff/17/18/19/20/26_*.md` và việc thiếu code Mobile cho `medical-staff/incidents` (đã nêu ở lượt review trước) vẫn là khoảng trống, để lại cho 1 vòng riêng sau khi Web ổn định.

### 10.4. Checklist thay đổi cần áp dụng theo file (Web only)

| File / Component | Thay đổi cần áp dụng |
| --- | --- |
| `26_IncidentDetail.md` | Cập nhật field Section B: "Lưu nháp" chỉ persist form, không mở Section C; Section C chỉ hiện sau "Chốt đánh giá & Bắt đầu điều tra" (validate). Section C: tách khoá theo panel (C2 theo `investigationEvidenceFinal`, C3 theo `rootStatus=FINAL`, C4 theo state≥4). Section D Block B: thêm điều kiện root cause FINAL. Bỏ field "Export" header khỏi danh sách field. Thêm dialog xác nhận + lý do khi nhả quarantine (QĐ 5, Mục 10.3) |
| `17_InvestigationWorkspace.md` | Đánh dấu chính thức **Deprecated as standalone** trong header file (hiện chỉ có ghi chú rải rác, chưa có dòng Status/Deprecated tường minh như tiền lệ `18_AssignIncidentSeverityDialog.md`) — đồng bộ với thực tế đã gộp vào `26` từ Vòng 1 |
| `19_RecordCorrectiveActionsReportScreen.md` / `20_GenerateIncidentReportDialog.md` | Tương tự — đánh dấu Deprecated as standalone tường minh, trỏ về Section D của `26` |
| `incidents/[id]/page.tsx` | Áp QĐ 1–6 Mục 10.3: bỏ nhánh `assessmentDraftSaved` (dòng 815); tách khoá C2/C3/C4 (bỏ `cLocked` khỏi C2/C3, giữ ở C4); thêm check `rootStatus === "FINAL"` vào nút submit Section D; xoá nút Export header (dòng 556-561); thêm dialog xác nhận nhả quarantine; thêm sticky status bar 4 giai đoạn + CTA chính; thêm badge phụ ở C3 khi còn PRELIMINARY |
| `components/app/collapsible-panel.tsx` | Luôn hiện dòng tóm tắt trạng thái kể cả khi `open=true` (áp dụng toàn dự án, không riêng Incident Detail) |
| `mock-data.ts` | Bổ sung field lưu lý do nhả quarantine (audit) nếu cần persist; giữ nguyên cấu trúc `FoodSample`/evidence khác |

**Trạng thái**: quyết định đã chốt (Mục 10.3) — ~~chưa áp code~~ → **ĐÃ ÁP CODE, xem Mục 11**.

---

## 11. Trạng thái áp dụng VÒNG 4 (Mục 10, Web only) — ĐÃ ÁP CODE 19/07/2026 (`tsc --noEmit` sạch)

Phần code mock của checklist Mục 10.4 đã thực hiện; các dòng Screen Spec (`26`/`17`/`19`/`20`) **CHƯA áp docs**, chờ lượt áp docs riêng. Mobile không đụng (QĐ 7).

---

## 12. Vòng rà soát thứ 5 (19/07/2026) — bố cục case-workspace: tập trung vào bước hiện tại, không phải "form dài chứa mọi bước". **Web only.**

### 12.1. Bối cảnh

Sau khi Vòng 4 sửa xong 2 gate an toàn (P0), người dùng chỉ ra vấn đề còn lại **không phải màu sắc/component mà là information architecture**: Detail đang cố hiển thị đồng thời hồ sơ + quy trình + ghi chú kỹ thuật nội bộ (mã `BR`/`UC`/`QĐ` lộ ra ngoài nhãn nút/tiêu đề), khiến 4 panel Section C mở cùng lúc trở thành 1 form dài thay vì progressive disclosure thật sự; cột phải "Quy trình xử lý" mô tả bằng đoạn văn dài thay vì checklist hành động; List và màn biên bản dùng không gian chưa hiệu quả trên màn Desktop rộng.

**Đã xác nhận trước khi ghi nhận**: đổi Section C từ "4 accordion mở đồng thời" sang "thanh điều hướng con, xem 1 mục tại 1 thời điểm" là thay đổi **thuần trình bày**, không đổi lại gate nghiệp vụ vừa chốt ở Vòng 4 (Mục 10.3) — C2/C3/C4 vẫn khoá/mở độc lập theo đúng cờ riêng (`investigationEvidenceFinal`/`rootStatus`/`cLocked`), chỉ đổi cách hiển thị (tab thay vì accordion), không tab nào bị ép thứ tự truy cập.

### 12.2. Quyết định (19/07/2026, Web only)

1. **Bố cục Detail đổi thành case-workspace 3 tầng**: (a) sticky status bar 4 giai đoạn + 1 dòng "Việc cần làm" (đã có từ Vòng 4, Mục 10.3 điểm 6); (b) dải tóm tắt gọn (severity/số HS/triệu chứng/lô khoá — đã có); (c) 2 cột chính: **trái** = vùng làm việc của bước hiện tại (Section B khi đang đánh giá, hoặc thanh tab con của Section C khi đang điều tra, hoặc Section D khi hoàn tất), **phải** = card "Việc cần hoàn tất" (checklist ngắn, có ô tick) + card "Liên quan" (mẫu đã thu hồi/vi phạm/CAPA/truy xuất, rút gọn).
2. **Section C đổi từ 4 accordion đồng thời sang thanh tab con** trong cùng route Detail: `Mẫu lưu | Bằng chứng | Nguyên nhân gốc rễ | CAPA`. Mỗi tab bấm được bất kỳ lúc nào; nội dung bên trong khoá/mở độc lập theo đúng logic đã chốt Vòng 4 (không đổi lại). Banner "đang chờ ai" (Mục 10.6 gốc) đặt phía trên thanh tab, áp dụng chung cho Section C, không gắn riêng 1 tab nào.
3. **`Truy xuất & lô bị khoá`, `Vi phạm liên quan`, `CAPA liên quan`** chuyển vào card "Liên quan" ở cột phải (rút gọn, không chiếm chỗ vùng làm việc chính) — **kèm 1 link ngữ cảnh** ngay trong tab "Nguyên nhân gốc rễ" trỏ tới đúng khối "Bản đồ truy xuất" trong card Liên quan (tránh bị bỏ sót đúng lúc cần dùng để kết luận root cause — đề xuất bổ sung, không có trong bản gốc người dùng).
4. **Mở rộng anchor URL sang cấp tab con**: `?section=C&tab=root-cause` (thay vì chỉ `?section=C` như Vòng 3) — nút "Quay lại điều tra" từ Sổ vi phạm phải khôi phục đúng cả tab đang làm dở, không chỉ mở đúng Section C rồi bắt người dùng tự bấm lại tab (đề xuất bổ sung, cần thiết để không phá cơ chế Vòng 3 Mục 8.3 bug 4).
5. **Card "Việc cần hoàn tất"** thay thế hẳn cột phải "Quy trình xử lý" (đoạn văn dài) ở Section B lẫn Section C — mỗi mục là 1 dòng checklist ngắn (VD "☐ Chọn mức độ", "☐ Nhập đánh giá lâm sàng nếu Medium/High", "☐ Chọn ít nhất 1 món nghi vấn"), tick tự động theo dữ liệu đã nhập, không phải nút bấm riêng. Đây là giải pháp thay cho đề xuất "badge phụ ở C3" của tôi ở Vòng 4 Mục 10.3 điểm 6 — checklist gọn hơn, tôi rút lại đề xuất badge cũ.
6. **Danh sách sự cố (List)**: bỏ dòng mô tả kỹ thuật "Luồng 2 bước... BR-146..." dưới tiêu đề (chuyển vào tooltip/help nếu cần giữ), thay bằng câu vận hành "Theo dõi và xử lý các báo cáo nghi ngờ sự cố an toàn thực phẩm"; tăng khung nội dung tối đa lên `max-width: 1280px`; thanh hành động khi chọn nhiều dòng ghi rõ số lượng ("Đã chọn 2 báo cáo · Tạo biên bản sự cố"); modal "Xem chi tiết" bỏ ghi chú kỹ thuật ("RPT không có màn hình riêng"), chỉ giữ nội dung báo cáo.
7. **Biên bản sự cố**: đổi bố cục 2 cột từ tỉ lệ ngang nhau sang `8/4` (editor rộng hơn bên trái, nguồn/union hẹp bên phải); thanh nút "Lưu nháp"/"Hoàn tất biên bản" chuyển thành sticky cuối trang; bỏ `UC-114 mở rộng`/`QĐ 1`/`BR-154` khỏi tiêu đề chính, chuyển vào tooltip hoặc khối "Căn cứ nghiệp vụ" riêng.
8. **Quy tắc UI áp dụng toàn module** (đã đối chiếu `00_Foundation/Design_System.md` — không mâu thuẫn, nằm trong khoảng đã định: Body 12–16sp, Label 11–14sp):
   - Cỡ chữ nội dung chính tối thiểu 14px, ghi chú tối thiểu 12px.
   - Mỗi màn chỉ 1 CTA primary nổi bật; không mở quá 2 vùng nội dung lớn cùng lúc.
   - Không hiển thị mã `BR`/`UC`/`QĐ` trong nhãn nút hoặc tiêu đề — chuyển vào tooltip/`title` attribute hoặc khối "Căn cứ".
   - Sau mỗi lần "Lưu nháp" hiện dòng "Đã lưu lúc [giờ]" thay cho đoạn văn giải thích dài.
   - Banner chờ actor khác (VD chờ School Admin duyệt CAPA) rút còn 1 dòng: "Đang chờ duyệt CAPA. Bạn vẫn có thể bổ sung bằng chứng hoặc cập nhật nguyên nhân gốc rễ." — không dùng đoạn văn dài như hiện tại.

### 12.3. Checklist thay đổi cần áp dụng theo file (Web only)

| File / Component | Thay đổi cần áp dụng |
| --- | --- |
| `26_IncidentDetail.md` | Viết lại Platform Layout: case-workspace 3 tầng (status bar + tóm tắt + 2 cột); Section C mô tả lại thành thanh tab con (không còn 4 panel đồng thời); thêm card "Việc cần hoàn tất" + "Liên quan" thay "Quy trình xử lý"; bỏ mã BR/UC/QĐ khỏi nhãn field chính |
| `17_InvestigationWorkspace.md` | Cập nhật mô tả UI (đã Deprecated as standalone từ Vòng 4) — nội dung field 0–13 không đổi, chỉ đổi từ "4 Collapsible Panel" sang "4 tab con trong Section C" |
| `incidents/[id]/page.tsx` | Đổi Section C từ accordion sang `Tabs` con (giữ nguyên state/logic khoá từng phần theo Vòng 4); thêm card "Việc cần hoàn tất" (checklist tự tick theo dữ liệu) thay cột "Quy trình xử lý"; card "Liên quan" gộp Truy xuất/Vi phạm/CAPA; link ngữ cảnh từ tab Root Cause → khối truy xuất; đọc/ghi `?section=C&tab=` thay vì chỉ `?section=`; rút gọn banner chờ actor khác còn 1 dòng; "Đã lưu lúc…" sau mỗi lưu nháp; bỏ mã BR/UC/QĐ khỏi label, chuyển `title` tooltip |
| `incidents/page.tsx` | Bỏ dòng mô tả kỹ thuật dưới tiêu đề, thay câu vận hành; `max-width: 1280px`; thanh hành động ghi rõ số lượng đã chọn; modal "Xem chi tiết" bỏ ghi chú kỹ thuật |
| `incidents/records/[id]/page.tsx` | Đổi bố cục cột `8/4`; sticky action bar cuối trang; bỏ mã kỹ thuật khỏi tiêu đề, chuyển tooltip/"Căn cứ nghiệp vụ" |
| `22_RecordStaffViolation.md` / `violations/page.tsx` | Nút "Quay lại điều tra" cần đọc thêm `tab=` từ query, không chỉ `section=` (đồng bộ điểm 4, Mục 12.2) |
| `00_Foundation/Design_System.md` hoặc `Component_Guidelines.md` | Cân nhắc ghi nhận quy tắc "không hiển thị mã BR/UC/QĐ trong nhãn UI, chỉ trong tooltip/audit" như quy ước toàn dự án (hiện chỉ áp cho module Incident Management ở đợt này) — **chưa quyết, để ngỏ cho đợt sau** |

**Trạng thái**: quyết định đã chốt (Mục 12.2) — ~~chưa áp code~~ → **ĐÃ ÁP CODE, xem Mục 13**.

*(Bảng dưới đây là bảng chi tiết của **Mục 11 — VÒNG 4** — bị tách khỏi vị trí gốc khi Mục 12 được chèn vào giữa; ghi chú lại 19/07/2026 để khỏi nhầm là bảng của Vòng 5.)*

| QĐ (Vòng 4) | File code | Chi tiết đã áp |
| --- | --- | --- |
| 1 (P0-1) | `incidents/[id]/page.tsx`, `mock-data.ts` | Bỏ nhánh `\|\| assessmentDraftSaved` ở điều kiện render Section C — C CHỈ hiện khi state ≥ 3 (đã "Chốt đánh giá" qua validate BR-147/149); "Lưu nháp" B chỉ persist severity/đánh giá rủi ro (không set cờ, không mở gì thêm); field `assessmentDraftSaved` đánh dấu `@deprecated`, giữ để không phá dữ liệu cũ |
| 2 (P0-2) | `incidents/[id]/page.tsx` | Tách khoá C theo bản chất panel: C2 chỉ theo `investigationEvidenceFinal` (bỏ `\|\| cLocked`); C3 chỉ khoá khi `rootStatus === "FINAL"` (cập nhật được cả khi CAPA pending/approved — đúng Mục 11.9.1); C4 GIỮ `cLocked` (state ≥ 4 — CAPA đã gửi, SLA 12h thật). `isLocked` toàn trang (QĐ 2 vòng 1 — sau Export/CLOSED) vẫn phủ lên tất cả |
| 3 | `incidents/[id]/page.tsx` | Nút nộp Section D (và CTA tương ứng) thêm điều kiện `investigationRootCauseStatus === "FINAL"` + dòng nhắc thay câu "nên xác nhận" cũ — không nộp được khi nguyên nhân còn Sơ bộ |
| 4 | `incidents/[id]/page.tsx` | Xoá nút "Export" mơ hồ ở PageHeader (tàn dư field 14 cũ — cùng lý do Mục 12.2 review incident); xuất chính thức duy nhất là "Chốt & nộp báo cáo" Section D |
| 5 | `incidents/[id]/page.tsx`, `mock-data.ts` | Bỏ tick món → Dialog xác nhận: liệt kê lô sẽ nhả, lý do BẮT BUỘC, ghi `Incident.quarantineReleaseLog` (audit BR-170); tick khoá lô vẫn tức thì (giữ QĐ 6 vòng 2) — bất đối xứng có chủ đích |
| 6 | `incidents/[id]/page.tsx`, `collapsible-panel.tsx` | Sticky status bar đầu trang: 4 giai đoạn Đánh giá → Điều tra → CAPA → Báo cáo (badge ✓/hiện tại) + 1 CTA chính theo trạng thái ("Chốt đánh giá & bắt đầu điều tra" / "Gửi CAPA chờ duyệt" / "Xem trước & nộp báo cáo"); state 4 hiện ghi chú nhánh song song thay CTA; badge nhắc phụ ở C3 khi còn Sơ bộ (độc lập CTA); `CollapsiblePanel` luôn hiện dòng summary kể cả khi mở (áp toàn dự án); nhãn nút chính thuần tiếng Việt, mã BR/UC/QĐ chuyển vào tooltip `title` |

Ghi chú: giới hạn mock "Upload tài liệu chỉ tăng bộ đếm" giữ nguyên như Mục 10.3 QĐ 6 đã ghi nhận (cần backend thật, không chặn vòng này); route `/investigation` vẫn là stub redirect.

---

## 13. Trạng thái áp dụng VÒNG 5 (Mục 12, Web only) — ĐÃ ÁP CODE 19/07/2026 (`tsc --noEmit` sạch)

Phần code mock của checklist Mục 12.3 đã thực hiện (thuần trình bày — gate nghiệp vụ Vòng 4 giữ nguyên, đã kiểm tra lại từng cờ khoá); các dòng Screen Spec (`26`/`17`) và câu hỏi để ngỏ (`Design_System`/`Component_Guidelines` — quy ước toàn dự án) **CHƯA áp**, chờ lượt riêng.

| QĐ | File code | Chi tiết đã áp |
| --- | --- | --- |
| 1+2 — case-workspace, tab con | `incidents/[id]/page.tsx` | Section C đổi 4 accordion → `Tabs` con (Mẫu lưu \| Bằng chứng \| Nguyên nhân gốc rễ \| CAPA) trong Card thường (không còn accordion lồng nhau); tab bấm tự do, khoá bên trong GIỮ NGUYÊN cờ Vòng 4 (`investigationEvidenceFinal`/`rootStatus=FINAL`/`cLocked`); banner "đang chờ ai" đặt trên thanh tab; cột trái tự hội tụ về bước hiện tại (B khi state 1, C khi ≥3, D khi ≥5) |
| 3 — card "Liên quan" | `incidents/[id]/page.tsx` | 3 panel "Truy xuất & Lô bị khoá"/"Vi phạm liên quan"/"CAPA liên quan" bỏ khỏi cột trái → card "Liên quan" cột phải (rút gọn: mẫu đã thu hồi, chuỗi lô→phiếu nhập→NCC, VIO links, CAPA + hạn); tab "Nguyên nhân gốc rễ" có link ngữ cảnh `#trace-map` trỏ đúng khối truy xuất |
| 4 — anchor tab con | `incidents/[id]/page.tsx`, `violations/page.tsx` | Link sang Sổ vi phạm truyền `?incident=…&section=C&tab=root-cause`; nút "Quay lại điều tra" đọc và gắn lại cả `tab=`; Detail đọc `?tab=` để chọn đúng tab con (fallback: root-cause khi state ≥ 4, evidence khi đang điều tra) |
| 5 — "Việc cần hoàn tất" | `incidents/[id]/page.tsx` | Card checklist tự tick theo dữ liệu (per-state: mức độ/đánh giá lâm sàng/món nghi vấn → mẫu/bằng chứng/root cause/CAPA → chờ duyệt + FINAL → khắc phục/FINAL/notes/nộp) THAY HẲN cột "Quy trình xử lý" đoạn văn; đồng thời thay banner nhắc "còn Sơ bộ" của Vòng 4 (đã rút lại theo đúng Mục 12.2 điểm 5) |
| 6 — List | `incidents/page.tsx` | Subtitle đổi thành câu vận hành (mô tả kỹ thuật chuyển vào comment); khung `max-w-[1280px]`; thanh chọn ghi "Đã chọn N báo cáo"; nút "Tạo biên bản sự cố"; modal Xem chi tiết bỏ ghi chú kỹ thuật |
| 7 — Biên bản | `incidents/records/[id]/page.tsx` | Bố cục đổi `8/4` (editor rộng TRÁI — CSS order, nguồn/union phải); thanh nút Lưu nháp/Hoàn tất thành sticky cuối trang; mã UC/BR/QĐ bỏ khỏi tiêu đề → dòng "Căn cứ nghiệp vụ" (tooltip đầy đủ) |
| 8 — quy tắc UI | `incidents/[id]/page.tsx` | Banner chờ actor khác rút 1 dòng ("Đang chờ duyệt CAPA. Bạn vẫn có thể…"); "Đã lưu lúc [giờ]" sau mỗi Lưu nháp (B/C4/D — inline + toast ngắn); mã BR/UC/QĐ bỏ khỏi nhãn nút/tiêu đề (vào `title` tooltip/comment); 1 CTA primary/màn — nút "Chốt đánh giá" trùng lặp cuối Section B đã bỏ (CTA duy nhất ở sticky bar); cỡ chữ giữ text-sm/text-xs (14/12px — trong khoảng Design_System) |

Ghi chú: nút hành động chính bên trong tab CAPA/Section D giữ nguyên (nơi có ngữ cảnh nhập liệu) — CTA sticky bar gọi cùng handler, không tạo hành vi khác; bảng Vòng 4 nằm ngay trên Mục 13 này thuộc Mục 11 (đã chú thích).

---

## 14. Vòng rà soát thứ 6 (19/07/2026) — tinh chỉnh tab "Nguyên nhân gốc rễ", đổi tên "CAPA", bổ sung dropdown Section C. **Web only.**

### 14.1. Bối cảnh

Người dùng gửi ảnh chụp code thật của tab "Nguyên nhân gốc rễ" (Section C, đã áp Vòng 5) và nêu 3 điểm, giao quyền quyết định cụ thể cho tôi ("tùy bạn quyết định"): (1) đổi bố cục 2 cột ngang (Lab | Root cause) sang xếp dọc, root cause lên trên; (2) đổi nhãn tab "CAPA" sang tiếng Việt dễ hiểu hơn; (3) Section C thiếu dropdown (collapsible) ở đầu so với A/B/D.

Đã verify trực tiếp code trước khi quyết định: dòng 1111 (`grid gap-3 lg:grid-cols-2`) xác nhận đúng bố cục 2 cột ngang hiện tại; dòng 1134-1148 xác nhận nút "Xác nhận nguyên nhân chính thức" **không có điều kiện chặn nào** liên quan tới `labResult` (chỉ disabled khi `update.isPending`) — tức Medical Staff bấm chốt "Chính thức" được ngay cả khi chưa nhập gì vào "Kết quả xét nghiệm lab"; dòng 966 xác nhận Section C bọc trong `<Card>` thường, không phải `<CollapsiblePanel>` như Section A (788)/B (930)/D (1241).

### 14.2. Quyết định

1. **Đổi bố cục tab "Nguyên nhân gốc rễ" sang xếp dọc, root cause lên trên**: "Kết luận nguyên nhân gốc rễ *" thành 1 hàng ngang đầy đủ chiều rộng ở trên (đây là field bắt buộc, đầu ra chính); "Kết quả xét nghiệm lab" thành 1 hàng ngang đầy đủ chiều rộng bên dưới (field hỗ trợ, thường chưa có ngay — placeholder đã ghi "đang chờ nuôi cấy 48h"). Đúng bố cục người dùng đề xuất.
2. **Thêm cảnh báo mềm (không chặn cứng) khi "Xác nhận nguyên nhân chính thức" mà `labResult` còn trống**: hiện dòng nhắc màu warning ngay dưới field lab "Chưa có kết quả lab — cân nhắc chỉ lưu Kết luận sơ bộ trước khi có kết quả." Chọn **cảnh báo, không disable nút**, vì không phải mọi sự cố đều cần xét nghiệm lab (VD `INC-2026-013` — dị vật kim loại trong canh, kết luận gốc rễ xác định qua kiểm tra quy trình/camera, không qua xét nghiệm mẫu; dị ứng có đơn bác sĩ tương tự) — chặn cứng theo `labResult` sẽ áp đặt 1 yêu cầu không có căn cứ SRS cho mọi loại sự cố.
3. **Đổi nhãn tab "CAPA" → "Yêu cầu khắc phục"**, giữ `CAPA — BR-152` trong `title` tooltip (đúng quy tắc đã chốt Mục 12.2 điểm 8, Vòng 5). Bản đầu đề xuất "Biện pháp khắc phục (giao bếp)" bị người dùng phản hồi là chưa rõ nghĩa; chọn lại "Yêu cầu khắc phục" — vẫn phân biệt rõ với Block A của Section D ("Xác nhận khắc phục" — UC-168, xác nhận **học sinh** đã hồi phục), vì "yêu cầu" (điều Medical Staff/School Admin đặt ra cho bên bếp) khác hẳn "xác nhận" (điều Medical Staff làm với tình trạng học sinh) — 2 khái niệm cùng gốc "khắc phục" nhưng chủ thể/hành động khác nhau, cần nhãn phân biệt để tránh nhầm lẫn.
4. **Bọc Section C trong `CollapsiblePanel`** (thay `<Card>` trần) — đúng nhất quán với A/B/D, thanh tab con (Mẫu lưu/Bằng chứng/Nguyên nhân gốc rễ/Biện pháp khắc phục) nằm trong nội dung panel. Mặc định mở khi `state === 3` (đang điều tra), thu gọn khi đã sang state khác (giữ đúng hành vi `defaultOpen` đã có ở Vòng 1/3, chỉ đổi từ `<Card>` sang `<CollapsiblePanel>`).

### 14.3. Checklist thay đổi cần áp dụng theo file

| File / Component | Thay đổi cần áp dụng |
| --- | --- |
| `26_IncidentDetail.md` / `17_InvestigationWorkspace.md` | Cập nhật mô tả tab "Nguyên nhân gốc rễ": field root cause xếp trước (bắt buộc), field lab xếp sau (hỗ trợ) + ghi chú cảnh báo mềm khi thiếu lab; đổi tên hiển thị "CAPA" → "Yêu cầu khắc phục", giữ mã CAPA/BR-152 trong ghi chú căn cứ; Section C mô tả là Collapsible Panel chứa tab con (không phải Card tĩnh) |
| `incidents/[id]/page.tsx` | Đổi `grid lg:grid-cols-2` (dòng 1111) thành 2 field xếp dọc, root cause trước; thêm `<p>` cảnh báo warning dưới field lab khi `labResult.trim() === "" `; đổi `TabsTrigger value="capa"` nhãn hiển thị thành "Yêu cầu khắc phục" (giữ `title="CAPA — BR-152"`); đổi `<Card>` bọc Section C (dòng 966) thành `<CollapsiblePanel>` cùng `defaultOpen`/`summary` logic đã có |

**Trạng thái**: quyết định đã chốt (Mục 14.2) — ~~chưa áp code~~ → **ĐÃ ÁP CODE, xem Mục 17**.

---

## 15. Vòng rà soát thứ 7 (19/07/2026) — đối chiếu sơ đồ Workflow 11 gốc với SRS, sửa 4 lệch pha + 1 nghiệp vụ mới ở Block A. **Ảnh hưởng cả `00_Business_Workflows.md` (dùng chung) và Web Medical Staff.**

### 15.1. Bối cảnh

Người dùng đưa ra 5 nhận xét trên sơ đồ Workflow 11 (`00_Business_Workflows.md`, dùng chung nhiều actor — không phải file riêng của Medical Staff) kèm 1 mermaid đề xuất và 1 bộ đề xuất UI case-workspace, yêu cầu phản biện có căn cứ trước khi sửa. Đã đối chiếu trực tiếp với: `MF11_Incident_Emergency_Management.md` (UC-005/009/013/033/038/041/059/076/082), `business_rules.md` BR-146–155, `MedicalStaff_Role_and_Workflows.md` §4.4/§7 (tài liệu rà soát diagram↔SRS có sẵn từ 04/07/2026, đã tự flag nhiều điểm trùng với nhận xét của người dùng nhưng chưa ai áp), và code thật `incidents/[id]/page.tsx`.

Kết quả đối chiếu từng điểm:

1. **Actor "Kitchen Manager"** (bước 11/18) — sai, không thuộc 7 vai trò canonical BR-007; đã bị `MedicalStaff_Role_and_Workflows.md` §7 (điểm 3, 04/07) tự flag nhưng chưa sửa. **Đồng ý.**
2. **Gate "Root Cause Confirmed?" chặn Define CAPA** — đồng ý, có căn cứ mới (không nằm trong 5 điểm gốc của người dùng, tôi bổ sung): BR-152/UC-038 yêu cầu CAPA nộp trong 12 giờ, trong khi BR-149 (bổ sung 15/07) ghi nhận thời gian ủ bệnh 62–87 giờ với một số tác nhân — gate cứng theo kết luận root cause **chính thức** khiến SLA 12h bất khả thi. Code thật (`establishMeasures()`) xác nhận **không hề kiểm tra `rootStatus`** — đúng pattern Sơ bộ→Chính thức đã chốt ở Mục 11.9.1 (`review_medicalstaff_incidentmanagement.md`). Sơ đồ gốc là bên lạc hậu so với cả code lẫn quyết định đã chốt.
3. **Thiếu bước Medical Staff xác minh hiệu quả sau khi Caterer thực hiện (UC-168)** — đồng ý, đã bị `MedicalStaff_Role_and_Workflows.md` §7 (điểm 2) tự flag từ 04/07, VÀ **đã được code implement sẵn** (Block A "Xác nhận khắc phục", Section D, khoá bằng `approvedCapa`, chặn EX1 theo BR-155). Sản phẩm đi trước sơ đồ.
4. **"Review Incident Report" thiếu nhánh reject/version** — người dùng tự phản biện lại chính đề xuất của mình khi được hỏi ("School Admin đã duyệt CAPA trước đó rồi, bước này còn cần không?") và kết luận: **không cần thêm**. Xác nhận đúng — UC-013 (Close Incident) đã có sẵn 1 điểm kiểm soát thứ 2 (review investigation summary + CAPA completion trước khi đóng, AF1 chặn nếu CAPA chưa xong), cộng với điểm kiểm soát thứ 1 ở UC-005 (duyệt CAPA) — thêm cơ chế reject+version cho riêng bước 21 là dư thừa, đồng thời đảo ngược QĐ 2+5 (Vòng 4/5: khoá cứng ngay sau Export & Submit, không mở khoá lại) mà không có BR/UC nào yêu cầu. **Giữ nguyên bước 21→22, không sửa.**
5. **Bước 9 "Retrieve Food Sample Logs (SFMS)" nên là hành động của Medical Staff** — đồng ý. UC-076 (Retrieve Food Sample Records) ghi rõ Primary Actor là Medical Staff; `MedicalStaff_Role_and_Workflows.md` §4.4 (bản dựng lại 04/07) đã tự sửa thành "(Medical Staff — UC-076)" nhưng chưa merge lại vào `00_Business_Workflows.md` gốc. Rút lại giả thuyết ban đầu của tôi (nghĩ đây là 2 việc tách biệt log vs mẫu vật lý) — không có căn cứ nào ủng hộ.
6. **Thiếu box "Quarantine" riêng** — hạ từ P0 xuống P2: không thiếu về chức năng (đã là postcondition của UC-009/bước 4, đã kích hoạt ngay khi tick món nghi vấn trong code từ Vòng 4) — chỉ là vấn đề trình bày sơ đồ, không sửa trong đợt này.

Riêng phần UI case-workspace người dùng đề xuất: khung layout + 1 CTA/giai đoạn về cơ bản trùng khớp Vòng 4–6 đã chốt, không phát sinh mới. Màn "School Admin duyệt CAPA" mà người dùng mô tả (Approve/Reject kèm lý do bắt buộc) **đã tồn tại sẵn** — `03_Design/05_ScreenSpecifications/Web/SchoolAdministration/11_CorrectiveActionReview.md` đã đúng field như vậy, không cần thiết kế mới, chỉ dùng lại.

Đã hỏi người dùng 3 điểm còn lại chưa có căn cứ BR/UC (nếu thêm là tạo nghiệp vụ mới, không phải sửa lỗi khớp diagram) qua `AskUserQuestion`:

- **Nhánh "Yêu cầu thực hiện lại" ở Block A (UC-168)**, ngoài EX1 hiện có (chỉ chặn theo tình trạng hồi phục học sinh) — người dùng chọn **thêm nhánh này**.
- **Nhánh Caterer "yêu cầu điều chỉnh CAPA"** (kế hoạch không khả thi, quay lại School Admin duyệt lại) — người dùng chọn **không thêm**, lý do: Caterer chỉ nhận được CAPA sau khi School Admin đã duyệt chính thức; nếu còn phải sửa thì họ chưa nhận việc, nên không cần nhánh phản hồi ngược từ phía họ.
- **Phạm vi áp 4 điểm đã có căn cứ vững** — người dùng chọn cập nhật **cả `00_Business_Workflows.md`** (không chỉ log riêng ở file Medical Staff), vì đây là sơ đồ gốc dùng chung nhiều actor/module.

### 15.2. Quyết định

1. Sửa `00_Business_Workflows.md` §11: đổi actor bước 11/18 "Kitchen Manager" → "Service Caterers (Kitchen Manager)"/"Service Caterers"; đổi actor bước 9 "(SFMS)" → "(Medical Staff — UC-076)"; đổi gate bước 14 "Root Cause Confirmed?" → "Đủ căn cứ đề xuất CAPA? (kết luận sơ bộ)", tách bước 16b "Hoàn tất Root Cause Chính thức" chạy song song, chỉ bắt buộc trước bước 20; thêm bước 18b "Xác nhận khắc phục đạt yêu cầu? (Medical Staff — UC-168)" với nhánh "Chưa đạt" quay lại bước 18. Bước 21→22 giữ nguyên. Cập nhật cả dòng chỉ mục Workflow 11 (thêm "Service Caterers" vào danh sách actor).
2. Đồng bộ `02_Requirements/MedicalStaff_Role_and_Workflows.md` §4.4 (bản trích đoạn Medical Staff của cùng sơ đồ) theo đúng thay đổi trên; cập nhật §7 (Khoảng trống & Điểm cần xác nhận): đánh dấu đã sửa điểm 2/3 (cũ), thêm điểm 6/7/8 (mới) ghi nhận gate Root Cause đã sửa, nhánh 18b "Chưa đạt" là nghiệp vụ mới chưa có mã UC/BR chính thức (cần đội dự án cấp mã), và việc "Kitchen Manager" trần vẫn còn tồn tại ở Workflow 10/Report 3/Report 1/wireframes — **ngoài phạm vi đợt này**, cần một vòng review riêng cho Workflow 10 (Meal Serving).
3. Bổ sung nhánh **"Yêu cầu thực hiện lại"** vào Block A (UC-168, Section D) của `26_IncidentDetail.md` và code `incidents/[id]/page.tsx`: thêm 1 nút phụ bên cạnh "Xác nhận khắc phục (MSG-046)", cho Medical Staff trả CAPA về trạng thái cần Caterer thực hiện lại kèm lý do bắt buộc (tương tự cơ chế Reject đã có ở UC-005), độc lập với validation EX1 hiện có (chặn khi học sinh chưa hồi phục — vẫn giữ nguyên, không thay thế). Đây là nghiệp vụ **mới**, thống nhất qua review này nhưng **chưa có mã UC/BR chính thức** — cần đội dự án xác nhận và cấp mã trước khi đưa vào Report 3 chính thức; tạm thời ghi chú trong `title` tooltip là "Bổ sung 19/07/2026 — chờ cấp mã UC/BR chính thức", không gán nhầm mã UC-168 cho nhánh reject này (UC-168 gốc chỉ mô tả nhánh xác nhận thành công + EX1).
4. Không thêm cơ chế reject/version cho báo cáo chính thức (bước 21) — giữ nguyên QĐ 2+5.
5. Không thêm nhánh Caterer "yêu cầu điều chỉnh CAPA".

### 15.3. Checklist thay đổi cần áp dụng theo file

| File / Component | Thay đổi cần áp dụng |
| --- | --- |
| `00_Business_Workflows.md` §11 | **Đã áp trong review này** (sửa trực tiếp: actor bước 9/11/18, gate bước 14→16b, thêm bước 18b + nhánh "Chưa đạt", cập nhật dòng chỉ mục Workflow 11) |
| `MedicalStaff_Role_and_Workflows.md` §4.4/§7 | **Đã áp trong review này** (đồng bộ mermaid trích đoạn + cập nhật bảng khoảng trống) |
| `26_IncidentDetail.md` (Block A, Section D) | Thêm mô tả nút phụ "Yêu cầu thực hiện lại" cạnh "Xác nhận khắc phục" — lý do bắt buộc, trả CAPA về trạng thái chờ Caterer thực hiện lại; ghi chú "chưa có mã UC/BR chính thức" |
| `incidents/[id]/page.tsx` | Thêm nút "Yêu cầu thực hiện lại" trong Block A (gần nút `submitValidation`, dòng ~1305-1310); thêm state/hàm `requestRework()` — cập nhật CAPA status về trạng thái cần thực hiện lại + lý do bắt buộc; KHÔNG đổi `submitValidation()`/EX1 hiện có |

**Trạng thái**: quyết định đã chốt (Mục 15.2); `00_Business_Workflows.md` và `MedicalStaff_Role_and_Workflows.md` **đã áp trực tiếp trong review này** (Mục 15.2 điểm 1–2); `26_IncidentDetail.md`/code (điểm 3) **CHƯA áp** — chờ lượt áp riêng, và chờ đội dự án cấp mã UC/BR chính thức cho nhánh "Yêu cầu thực hiện lại" trước khi hoàn thiện Screen Spec.

---

## 16. Chốt 4 quyết định thực thi còn treo (19/07/2026) — qua `AskUserQuestion`, sau khi kiểm kê toàn bộ nợ của module Incident Management. **CHƯA áp file nào — chỉ ghi nhận quyết định.**

Người dùng yêu cầu "giải quyết tất cả vấn đề của incident" — đã kiểm kê 2 nhóm: (Loại 1) việc chỉ cần thực thi, không cần quyết định (sửa trích dẫn MSG-024/MSG-046, thêm dòng "Deprecated as standalone" cho `17`/`19`/`20`, đồng bộ Screen Spec `26`/`17`/`19`/`20`/`33`/`25`/`22` theo nợ tích luỹ từ Vòng 4/5/8/10/12 của `review_medicalstaff_incidentmanagement.md`); (Loại 2) 4 điểm cần quyết định thật, đã hỏi và chốt như sau:

1. **Mã UC/BR cho nhánh "Yêu cầu thực hiện lại"** (Vòng 7, Mục 15.2 điểm 3) → **gộp thẳng vào UC-168 như 1 Alternative Flow mới** ("AF2 — Khắc phục chưa đạt yêu cầu") trong `MF11_Incident_Emergency_Management.md`, không cấp UC riêng, giữ nguyên BR-152/153/155 đã dẫn. **CHƯA áp** — chờ cùng đợt với các file khác.
2. **Phạm vi mở rộng ngoài Incident Management** (Kitchen Manager ở Workflow 10, Mobile parity, merge Violation Management flow, đồng bộ UC-059→UC-168 trong MF11) → **không chọn mục nào — giữ nguyên phạm vi Incident Management**, không mở rộng đợt này. 4 mục này vẫn còn mở, để dành cho các vòng review riêng sau (đã có sẵn trong `MedicalStaff_Role_and_Workflows.md` §7 điểm 1/4/8).
3. **Nhãn "Xem Biên bản gốc (PDF)"** ở Section A → **đổi thành "Xem Biên bản gốc"** (bỏ chữ PDF, route thật hiện là HTML mock, tránh hứa hẹn định dạng chưa có thật). **CHƯA áp** — chờ cùng đợt.
4. **Thứ tự thực thi** (áp code Vòng 6+7 trước hay áp Screen Spec docs nợ cũ trước) → người dùng **chưa sẵn sàng bắt đầu đợt áp dụng**, tạm dừng ở bước ghi nhận quyết định. Không tự ý áp bất kỳ file nào (Screen Spec, `MF11`, code `.tsx`) cho tới khi có tín hiệu bắt đầu.

**Trạng thái**: 3 quyết định (1, 2, 3) đã chốt nội dung — ~~chưa áp file nào~~ → **QĐ 3 (nhãn "Xem Biên bản gốc") ĐÃ ÁP CODE 19/07/2026**; QĐ 1 (AF2 UC-168 trong `MF11_Incident_Emergency_Management.md`) và phần Screen Spec vẫn **CHƯA áp docs** — xem Mục 17.

---

## 17. Trạng thái áp dụng VÒNG 6 + 7 (Mục 14/15) và QĐ 3 Mục 16 — ĐÃ ÁP CODE 19/07/2026 (`tsc --noEmit` sạch)

Người dùng phát tín hiệu bắt đầu (yêu cầu áp màn hình Medical Staff trong `03_Design/08_FrontEnd/Web`) — đúng theo Mục 16 QĐ 4, đợt này áp **phần code Web**; phần tài liệu (Screen Spec `26`/`17`/`19`/`20`/`33`/`25`/`22`, `MF11` AF2 UC-168, nợ docs tích luỹ Loại 1 Mục 16) **CHƯA áp**, chờ lượt docs riêng.

| Nguồn | File code | Chi tiết đã áp |
| --- | --- | --- |
| Vòng 6 QĐ 1–2 | `incidents/[id]/page.tsx` | Tab "Nguyên nhân gốc rễ" đổi từ 2 cột ngang sang XẾP DỌC: "Kết luận nguyên nhân gốc rễ *" đầy chiều rộng ở trên (đầu ra chính), "Kết quả xét nghiệm lab" bên dưới; thêm cảnh báo MỀM dưới field lab khi còn trống ("Chưa có kết quả lab — cân nhắc chỉ lưu Kết luận sơ bộ…") — KHÔNG disable nút chốt Chính thức (giữ đúng lý do: dị vật/dị ứng không cần xét nghiệm mẫu) |
| Vòng 6 QĐ 3 | `incidents/[id]/page.tsx` | Nhãn tab "CAPA" → **"Yêu cầu khắc phục"**, mã `CAPA — BR-152` giữ trong `title`; summary Section C cũng đổi theo ("chỉ tab Yêu cầu khắc phục khoá") — phân biệt rõ với "Xác nhận khắc phục" (Block A, UC-168) |
| Vòng 6 QĐ 4 | `incidents/[id]/page.tsx` | Section C bọc `CollapsiblePanel` (thay `Card` trần của Vòng 5) — nhất quán A/B/D; `defaultOpen` khi `state === 3` hoặc anchor `?section=C`; thanh tab con nằm trong nội dung panel (đã sửa 1 thẻ đóng `</Card>` còn sót gây lỗi biên dịch) |
| Vòng 7 (Mục 15.2 điểm 3) | `incidents/[id]/page.tsx` | Nút phụ **"Yêu cầu thực hiện lại"** cạnh "Xác nhận khắc phục" ở Block A + dialog lý do BẮT BUỘC; `requestRework()` trả toàn bộ CAPA đang APPROVED về `ASSIGNED` kèm `reworkReason`/`reworkRequestedAt` (field đã có sẵn ở `mock-data.ts`); KHÔNG đụng `submitValidation()`/EX1; tooltip ghi "Bổ sung 19/07/2026 — chờ cấp mã UC/BR chính thức (dự kiến gộp UC-168 AF2)" đúng Mục 16 QĐ 1 |
| Mục 16 QĐ 3 | `incidents/[id]/page.tsx` | Nhãn Section A đổi "Xem Biên bản gốc (PDF)" → **"Xem Biên bản gốc"** (route thật là HTML mock, không hứa định dạng chưa có) |
| Vòng 6 QĐ 8 (kèm) | `incidents/[id]/page.tsx` | Dọn thêm mã kỹ thuật trong nhãn/ghi chú tab Nguyên nhân gốc rễ và nút "Xác nhận khắc phục" (bỏ "(MSG-046)" khỏi nhãn, chuyển vào `title`) theo quy tắc Mục 12.2 điểm 8 |

Ghi chú phạm vi: Mục 16 QĐ 2 (Kitchen Manager ở Workflow 10, Mobile parity, merge Violation flow, UC-059→UC-168) giữ nguyên là **ngoài phạm vi**, không đụng ở đợt này.
