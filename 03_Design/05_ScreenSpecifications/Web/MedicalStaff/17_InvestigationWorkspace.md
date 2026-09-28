# Screen Spec — Investigation Workspace

**Actor**: Medical Staff | **UC liên quan**: UC-041 (Report 3 §3.2, dòng 4445–4460) | **Status**: Draft
**BR liên quan**: BR-146 (ngưỡng tự động kích hoạt điều tra), BR-147 (phân loại severity), BR-148 (cảnh báo tức thời High), BR-149 (cách ly lô hàng), BR-150 (điều tra nguyên nhân gốc rễ), BR-151 (bản đồ truy xuất trong 4 giờ), BR-152 (CAPA), BR-154 (báo cáo sự cố chính thức)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error)

> **Cập nhật 15/07/2026 — banner "đang chờ ai"**: bổ sung field 0 (banner đầu trang), áp dụng quyết định đã có sẵn từ 09/07/2026 (`review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 7.6, "Loại B — điểm chờ hành động actor khác") nhưng trước đó chưa được đưa vào Screen Spec. Lý do: Medical Staff chắc chắn sẽ rời màn đi xử lý case khác trong lúc chờ actor khác hành động — cần biết rõ ngay khi quay lại, thay vì nhãn chung chung.
> **Cập nhật 15/07/2026 (đợt 2 — Mục 11.5/11.8/11.9/11.10)**: (1) đổi bố cục Web từ **grid 2×2** sang **full-width row + collapsible** (component mới `Collapsible Panel`, xem `00_Foundation/Component_Guidelines.md`) — 4 bước có độ dài nội dung rất khác nhau, ép vào grid gây khoảng trắng vô nghĩa (Mục 11.10); (2) khóa danh sách "Type of evidence" (field 5) thành **5 giá trị** (Mục 11.9.3); (3) root cause (field 9-10) chuyển từ 2 Textarea độc lập sang **1 bản ghi trạng thái Sơ bộ → Chính thức**, tự kế thừa sang `20_GenerateIncidentReportDialog.md` (không nhập lại lần 2) — Mục 11.9.1; (4) thêm nút "Lập biên bản vi phạm nhân viên liên quan" ở panel Root Cause, mở `22_RecordStaffViolation.md` với `relatedIncidentId` pre-fill (Mục 11.5.2); (5) card "1. Thu hồi mẫu lưu" thu gọn mặc định + 1 dòng tóm tắt trạng thái (Mục 11.8).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 0 | Banner "Đang chờ ai" 🆕 | Banner/Alert (đọc, điều kiện) | Chỉ hiện khi đang ở đúng điểm chờ | — | Hiện đúng 1 trong các nhãn theo Mục 7.6: "Đang chờ Kitchen Manager xác minh nhà cung cấp" (E11 Trace Supplier Information), "Đang chờ School Administration duyệt CAPA" (E17), "Đang chờ Kitchen Manager/School Administration triển khai & giám sát CAPA" (E18-19), "Đang chờ School Administration xem xét báo cáo sự cố" (E21) — không hiện khi Medical Staff đang là actor cần hành động (VD đang nhập bằng chứng/root cause) |
| 1 | `Collapsible Panel` "1. Extracting Stored Food Samples" | Panel thu gọn/mở (full-width row) | — | BR-150 | Mở `14_RetrieveFoodSampleRecordsDialog.md` (biến thể ngữ cảnh Incident — Mục 11.6). **Thu gọn mặc định (Mục 11.8)**: tiêu đề card luôn hiện 1 dòng tóm tắt trạng thái (VD "✓ 2 mẫu đã thu hồi (Sữa chua bữa phụ, Canh bí đỏ)" hoặc "Chưa thu hồi mẫu nào"); mở ra hiện bảng đầy đủ mẫu của `incidentId` hiện tại (mã mẫu/món/thời điểm/lab/trạng thái) |
| 2 | `Collapsible Panel` "2. Gathering Evidence" | Panel thu gọn/mở (full-width row) | — | BR-150 | Xem khối dưới |
| 3 | `Collapsible Panel` "3. Identify the Root Cause" | Panel thu gọn/mở (full-width row) | — | BR-150, BR-151 | Xem khối dưới; chứa nút "Lập biên bản vi phạm nhân viên liên quan" (field 10b) |
| 4 | `Collapsible Panel` "4. Proposed Remedial Measures" | Panel thu gọn/mở (full-width row) | — | BR-152 | Xem khối dưới |
| **Khối "Gathering Evidence" (§3.3.2.3.10)** | | | | | |
| 5 | Type of evidence | `Select` (5 giá trị cố định) | — | BR-150 | **Khóa danh sách 15/07/2026 (Mục 11.9.3)**: (1) Lời khai học sinh/giáo viên; (2) Ảnh hiện trường/món ăn còn lại; (3) Kết quả kiểm tra bếp/quy trình (liên kết `NonConformanceRecord`); (4) Giấy tờ y tế/bệnh viện; (5) Khác. Không giới hạn ở "ảnh học sinh ngộ độc tại phòng y tế" vì ca nặng thường chuyển thẳng viện |
| 6 | Brief description | Textarea | — | — | |
| 7 | Attached documents (upload) | Input type="file" + preview | — | BR-150 | |
| 8 | Nút "Save Evidence" | Button (primary) | — | — | |
| **Khối "Identify the Root Cause"** | | | | | |
| 9 | Kết quả xét nghiệm lab | Textarea/File | — | BR-150 | [Giả định — cần team xác nhận cấu trúc field, Mục 5.5 vẫn mở]. Trạng thái điển hình "đang chờ nuôi cấy 48h+" trước khi có kết quả |
| 10 | Kết luận nguyên nhân gốc rễ | Textarea, **1 bản ghi trạng thái Sơ bộ → Chính thức** | — | BR-150, BR-151, BR-152 | **Sửa 15/07/2026 (Mục 11.9.1)** — thay 2 Textarea độc lập (trùng với `20_GenerateIncidentReportDialog.md`) bằng **1 bản ghi duy nhất**: "Kết luận **sơ bộ**" nhập tại đây dựa trên kiểm tra quy trình/truy xuất, đủ căn cứ giao CAPA trong 12h (BR-152, SLA); khi có kết quả nuôi cấy lab (48h+), Medical Staff quay lại xác nhận/cập nhật thành "Kết luận **chính thức**", **tự kế thừa** sang `20_GenerateIncidentReportDialog.md` (không nhập lại lần 2). *(Root cause + CAPA cùng 1 bước theo Normal Flow UC-041; lab bổ sung là AF2, không phải điều kiện tiên quyết)* |
| 10b 🆕 | Nút "Lập biên bản vi phạm nhân viên liên quan" | Button (secondary) | — | BR-185 | **Mới 15/07/2026 (Mục 11.5.2)** — mở `22_RecordStaffViolation.md` với `relatedIncidentId` = `INC-xxx` hiện tại được pre-fill. Dùng khi điều tra xác định nguyên nhân gốc rễ là nhân viên bếp làm sai. Không đổi luồng phê duyệt vi phạm (BR-186 độc lập với CAPA) |
| **Khối "Treatment Measures" (§3.3.2.3.11)** | | | | | |
| 11 | Medical treatment/correction measures | Textarea | — | BR-152 | |
| 12 | Unit/Person in charge | Input (text) | — | BR-152 | |
| 13 | Nút "Establish Measures" | Button (primary) | — | BR-152 | Giao CAPA cho Service Caterers, hạn 12 giờ |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải dữ liệu điều tra hiện có.
- Empty: chưa có bằng chứng/kết luận → trạng thái trống cho từng panel.
- Error: MSG-004, MSG-005, MSG-023.
- Success: lưu từng khối → Toast xác nhận; "Establish Measures" → CAPA được giao, quay lại `26_IncidentDetail.md`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Web); "Establish Measures" → quay lại Incident Detail.
**Bố cục (sửa 15/07/2026, đợt 2 — Mục 11.10)**: đổi từ grid 2×2 sang **full-width row + collapsible**. Mỗi bước (1–4) là 1 row chiếm trọn chiều ngang (`Collapsible Panel`, `00_Foundation/Component_Guidelines.md`):
- Trạng thái thu gọn: chỉ tiêu đề + 1 dòng tóm tắt trạng thái (VD card 1 — Mục 11.8).
- Trạng thái mở: chiếm trọn chiều rộng, **tự chia 2 cột bên trong panel** cho field của chính nó (VD card 3 mở ra xếp "Kết quả xét nghiệm lab" | "Kết luận nguyên nhân gốc rễ" cạnh nhau) — tận dụng màn rộng ở cấp field thay vì ép cứng ở cấp 4-panel.
- **Không** giới hạn 1 panel mở tại 1 thời điểm (không phải accordion single-open) — 4 bước không khóa tuần tự cứng với nhau, Medical Staff có thể mở đồng thời "Gathering Evidence" và "Root Cause" để đối chiếu.
- Mặc định: row đang làm dở mở sẵn; row đã hoàn tất thu gọn kèm dấu ✓ + tóm tắt; row chưa tới lượt thu gọn để trống.
- Lý do bổ sung: gần với Mobile (4 Card xếp dọc → trang con), giảm điểm lệch nhất quán Web/Mobile so với grid 2×2 (không có tương đương Mobile).

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Mobile, nút "Step 2: Incident Investigation Space"); "Establish Measures" → quay lại Incident Detail.
**Bố cục**: Report 3 §3.3.2.3.7 — 4 Card xếp dọc dẫn tới 4 trang con tuần tự.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Investigation Workspace, nền tảng Web, actor Medical Staff.
Bố cục: banner "Đang chờ ai" đầu trang (điều kiện), rồi 4 Collapsible Panel full-width xếp dọc
(1. Extract Sample | 2. Gathering Evidence | 3. Root Cause | 4. Treatment Measures) — thu gọn hiện tiêu đề
+ dòng tóm tắt, mở ra chia 2 cột nội bộ; cho phép mở nhiều panel cùng lúc (không accordion single-open).
Panel Gathering Evidence: Type of evidence (Select 5 giá trị), Brief description, Attached documents, Save Evidence.
Panel Root Cause: Kết quả xét nghiệm lab, Kết luận nguyên nhân gốc rễ (trạng thái Sơ bộ→Chính thức),
nút "Lập biên bản vi phạm nhân viên liên quan".
Panel Treatment Measures: nút Establish Measures.
Áp dụng Design System: primary #0E7490, critical #7E22CE cho Quarantine.
Trạng thái cần thể hiện: loading, empty (mỗi panel), error (MSG-023).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4445–4460 (UC-041), dòng 5826–5943 (§3.3.2.3.7, 3.3.2.3.10, 3.3.2.3.11).
- business_rules.md (BR-146–152, BR-154, BR-185–189).
- error_message.md (MSG-004, MSG-005, MSG-023).
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 11.5.2 (nút biên bản vi phạm), 11.8 (card 1 thu gọn), 11.9.1 (root cause Sơ bộ→Chính thức), 11.9.3 (Type of evidence 5 giá trị), 11.10 (bố cục collapsible).
- `00_Foundation/Component_Guidelines.md` (component `Collapsible Panel`).
- `22_RecordStaffViolation.md` (đích nút "Lập biên bản vi phạm nhân viên liên quan").

**Ghi chú**: giống bản Mobile — "Collect Evidence"/"Treatment Measures" không có dòng riêng trong Matrix, gộp vào file này.
