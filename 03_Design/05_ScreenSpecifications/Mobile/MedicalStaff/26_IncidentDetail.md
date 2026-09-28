# Screen Spec — Incident Detail

**Actor**: Medical Staff (màn hình dùng chung với School Administration + Teacher — `14_Screen_Authorization_Matrix.md` khối 3, dòng "Incident Detail"; file này viết theo góc nhìn Medical Staff) | **UC liên quan**: UC-116, gộp trình bày UC-009 (Report 3 §3.2, dòng 4577–4591 — primary actor gốc School Administration) | **Status**: Draft
**BR liên quan**: BR-146–155 (toàn bộ vòng đời quản lý sự cố)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error)

> **Cập nhật 11/07/2026**: field 5 đổi tên "First aid was administered" → "Tường trình ban đầu", mở rộng thành Textarea nhập bắt buộc ở trạng thái TREATMENT (trước đây Text chỉ đọc) — không tạo UC/BR/màn hình mới. Chi tiết: `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 9.12.
> **Cập nhật 15/07/2026** (chi tiết đầy đủ xem bản Web cùng tên và Mục 10.4/10.5/10.7 review file): (1) gộp "Đánh giá mức độ nghiêm trọng" (trước đây dialog riêng `18_AssignIncidentSeverityDialog.md`, nay Deprecated) vào thẳng màn này; (2) "Chọn món ăn nghi vấn" mặc định lọc 48 giờ (Quyết định 39/2006/QĐ-BYT) + nút "Mở rộng phạm vi ngày"; (3) gộp "Validate CAPA" + "Step 3: Generate Incident Report" thành 1 điểm vào "Hoàn tất sự cố".

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Case code | Text (đọc) | — | — | Report 3 §3.3.2.3.5 |
| 2 | Clinical symptoms | Text (đọc) | — | — | |
| 3 | Affected students (số lượng, lớp, tên) | Text (đọc) | — | BR-146 | |
| 4 | Suspected food item + meal time | Text (đọc) | — | — | |
| 5 | Severity level | `RadioGroup`, bắt buộc | Bắt buộc chọn: Low/Medium/High | BR-147 | Gộp vào màn này 15/07/2026 — trước đây dialog riêng `18_AssignIncidentSeverityDialog.md` (nay Deprecated). Nguyên field/validation UC-009 |
| 6 | Đánh giá rủi ro lâm sàng | Textarea | Bắt buộc khi Medium/High | BR-147 | Gộp từ UC-009 |
| 7 | Tường trình ban đầu (trước đây "First aid was administered") | Textarea nhập, bắt buộc trước khi "Lưu đánh giá & Bắt đầu điều tra" | Bắt buộc | — | Mô tả đầy đủ diễn biến ban đầu. **Pre-fill từ "biên bản sự cố"** đã soạn ở `33_CreateIncidentRecordDialog.md` khi mở lần đầu (bổ sung 15/07/2026). Pre-fill sang "Clinical notes" ở `20_GenerateIncidentReportDialog.md` (UC-033), Medical Staff vẫn sửa được |
| 8 | Chọn món ăn nghi vấn | `Select` multi, bắt buộc | Bắt buộc trước khi "Lưu đánh giá & Bắt đầu điều tra"; lấy từ Menu/`MealServiceSession` của "Ngày ăn" | BR-149 | **Mặc định lọc theo món chế biến trong 48 giờ** (Quyết định 39/2006/QĐ-BYT, `00_Policies/39_2006_QD-BYT_quy_che_dieu_tra_ngo_doc_thuc_pham.md`), kèm nút "Mở rộng phạm vi ngày". Chọn xong → Quarantine kích hoạt ngay (BR-149) |
| 9 | Nút "Lưu đánh giá & Bắt đầu điều tra" | Button (primary) | Disabled khi thiếu field 5/7/8 | BR-147, BR-148, BR-149 | Gộp 15/07/2026, thay 2 hành động tách rời. Severity High → banner Emergency Response (E7) |
| 10 | Bản đồ truy xuất nguồn gốc (nếu có) | Panel/link | Biên soạn trong 4 giờ | BR-151 | Hợp nhất từ bản School Administration (mục 6, review 2026-07-01) |
| 11 | Danh sách lô hàng bị Quarantine | Danh sách rút gọn | — | BR-149 | Hợp nhất từ bản School Administration |
| 12 | Nút "Step 2: Incident Investigation Space" | Button | Chỉ Medical Staff | BR-150 | Điều hướng `17_InvestigationWorkspace.md` |
| 13 | Nút "Hoàn tất sự cố" | Button | Hiện khi CAPA đã được School Administration duyệt (E17) | BR-152, BR-153, BR-154, BR-155 | **Gộp 15/07/2026** — thay 2 nút tách rời "Validate CAPA đã Approved" (`19_RecordCorrectiveActionsReportScreen.md`/UC-168) và "Step 3: Generate Incident Report" (`20_GenerateIncidentReportDialog.md`/UC-033), trình bày thành 2 khối tuần tự trong 1 màn |
| 14 | Nút "Export" | Button | — | — | AF1 — xuất CSV/PDF |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` khi tải chi tiết sự cố.
- Empty: không áp dụng (luôn có 1 sự cố cụ thể).
- Error: MSG-004, MSG-005, MSG-023.
- Success: không áp dụng (màn hình điều hướng trung tâm, hành động ghi thực hiện ở các màn con).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ chọn 1 Card trên `25_IncidentList.md`; nút Step 2 và "Hoàn tất sự cố" điều hướng sang màn con tương ứng.
**Bố cục**: Report 3 §3.3.2.3.5 — AppBar (Back, Home, "Return to list"), Section Header "Medical Incident Details", các block thông tin xếp dọc gồm Severity RadioGroup + Đánh giá rủi ro + Tường trình ban đầu + Chọn món ăn nghi vấn (48h + nút Mở rộng phạm vi), nút "Lưu đánh giá & Bắt đầu điều tra", tiếp đó Step 2 và "Hoàn tất sự cố" xếp dọc bên dưới, nút "Export" nổi góc trên phải AppBar.

## Platform Layout — Web
**Entry/Exit points**: Vào từ chọn dòng trên `25_IncidentList.md` (Web).
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — 2 cột: thông tin sự cố + severity + tường trình + chọn món ăn bên trái (`col-span-8`), action list (Lưu đánh giá & Bắt đầu điều tra, Step 2, Hoàn tất sự cố, Export) bên phải (`col-span-4`).

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Incident Detail, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: Case code, Clinical symptoms, Affected students, Suspected food item,
Severity RadioGroup + Đánh giá rủi ro lâm sàng, Tường trình ban đầu (Textarea, bắt buộc),
Chọn món ăn nghi vấn (multi-select, mặc định 48h + nút Mở rộng phạm vi ngày),
nút "Lưu đánh giá & Bắt đầu điều tra", nút Step 2 (Investigation Space), nút "Hoàn tất sự cố", nút Export.
Áp dụng Design System: primary #0E7490, critical #7E22CE (High severity).
Trạng thái cần thể hiện: loading, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4577–4591 (UC-116), dòng 5783–5807 (§3.3.2.3.5), dòng 4335–4349 (UC-009).
- business_rules.md (BR-146–155, Changelog v1.8).
- error_message.md (MSG-004, MSG-005, MSG-023).
- `00_Policies/39_2006_QD-BYT_quy_che_dieu_tra_ngo_doc_thuc_pham.md`.
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.4/10.5/10.7 (15/07/2026).

**Ghi chú màn hình dùng chung**: bản School Administration có thêm nút phê duyệt/đóng sự cố (BR-155, UC-013 Close Incident) mà Medical Staff không có; bản Teacher chỉ xem, không có các nút hành động (Lưu đánh giá & Bắt đầu điều tra/Step 2/Hoàn tất sự cố). Phần field hiển thị dữ liệu (Case code, symptoms, affected students...) hợp nhất ở bước review (mục 5, 6 của `00_Team_Work_Assignment_Plan.md`), phần hành động giữ riêng theo actor.
