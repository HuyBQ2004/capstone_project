# Screen Spec — Incident Detail

**Actor**: Medical Staff (màn hình dùng chung với School Administration + Teacher — file này viết theo góc nhìn Medical Staff) | **UC liên quan**: UC-116, gộp trình bày UC-009 (Report 3 §3.2, dòng 4577–4591) | **Status**: Draft
**BR liên quan**: BR-146–155
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error)

> **Đã hợp nhất (mục 6, `00_Team_Work_Assignment_Plan.md`)**: đối chiếu với `Web/SchoolAdministration/10_IncidentDetail.md` — bổ sung field #6 "Bản đồ truy xuất nguồn gốc" (BR-151) và #7 "Danh sách lô hàng bị Quarantine" (BR-149), trước đây thiếu ở bản này. Giữ nguyên toàn bộ hành động riêng của Medical Staff (Step 1/2/3, Validate CAPA). Ghi chú: theo review, Teacher **không có** file Incident Detail riêng — xem giải thích ở bản School Administration.
> **Cập nhật 11/07/2026**: field 5 đổi tên "First aid was administered" → "Tường trình ban đầu", mở rộng thành Textarea nhập bắt buộc ở trạng thái TREATMENT (trước đây Text chỉ đọc) — quyết định sau khi rà soát cho thấy Medical Staff cần ghi lại diễn biến sớm thay vì chỉ viết Clinical notes ở cuối quy trình (UC-033). Không tạo UC/BR/màn hình mới. Chi tiết: `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 9.12.
> **Cập nhật 15/07/2026 — 3 thay đổi lớn** (xem `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.4/10.5/10.7): (1) gộp "Đánh giá mức độ nghiêm trọng" (trước đây dialog riêng `18_AssignIncidentSeverityDialog.md`/UC-009) vào thẳng màn này, cùng lúc với "Tường trình ban đầu" + "Chọn món ăn nghi vấn" — bỏ mô hình "hoàn tất Step 1 dialog rồi mới hiện Step 2"; (2) "Chọn món ăn nghi vấn" nay mặc định lọc theo món chế biến trong 48 giờ (căn cứ Quyết định 39/2006/QĐ-BYT), có nút "Mở rộng phạm vi ngày"; (3) gộp "Validate CAPA đã Approved" (UC-168) + "Step 3: Generate Incident Report" (UC-033) thành 1 điểm vào "Hoàn tất sự cố" duy nhất.
> **Cập nhật 15/07/2026 (đợt 2 — Mục 11.4/11.5/11.6/11.7)**: (1) thêm panel đọc "Biên bản gốc" (field 4b) hiển thị xuyên suốt vòng đời, trỏ `IncidentRecordDraft.content` — vì sau khi biên bản pre-fill sang "Tường trình ban đầu" (sửa được), văn bản gốc mất định danh riêng (Mục 11.4.1); (2) thêm panel đọc "Vi phạm liên quan" (field 11b) từ trạng thái `INVESTIGATING` trở đi, liệt kê `StaffViolationRecord`/`NonConformanceRecord` có `relatedIncidentId` = sự cố hiện tại, link sang `27_ViolationRegistry.md` (Mục 11.5.3); (3) giới hạn "Mở rộng phạm vi ngày" (field 8) ở đúng **72h** — ngưỡng còn khả năng thu hồi mẫu thật (BR-111/BR-112), món rơi vào trạng thái `Disposed` hiển thị mờ + "Link Disposed Sample to Incident" (Mục 11.6); (4) bản đồ truy xuất nguồn gốc (field 10) **thu gọn mặc định** + scope theo lô đã khóa (`quarantineBatchIds[]`), hiển thị **lồng nhau** — mỗi dòng lô bị khóa mở ra đúng chuỗi truy xuất của lô đó (Mục 11.7, đề xuất — cần đội dự án xác nhận khi cập nhật BR-151).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Case code | Text (đọc) | — | — | Report 3 §3.3.2.3.5 |
| 2 | Clinical symptoms | Text (đọc) | — | — | |
| 3 | Affected students | Text (đọc) | — | BR-146 | |
| 4 | Suspected food item + meal time | Text (đọc) | — | — | |
| 4b 🆕 | Biên bản gốc (tham chiếu) | Panel/link (đọc) | — | BR-154 | **Mới 15/07/2026 (Mục 11.4.1)** — hiển thị **xuyên suốt vòng đời** sự cố (không chỉ lúc pre-fill), trỏ tới `IncidentRecordDraft.content` (biên bản đã soạn ở `33_CreateIncidentRecordDialog.md`, mở phiên điều tra thành sự cố này). Mục đích: giữ khả năng mở lại đúng **nguyên văn** biên bản triage ban đầu, vì nội dung đó đã pre-fill sang "Tường trình ban đầu" (field 7, sửa được) nên văn bản gốc mất định danh riêng |
| 5 | Severity level | `RadioGroup`, bắt buộc | Bắt buộc chọn: Low/Medium/High | BR-147 | **Gộp vào màn này 15/07/2026** — trước đây là dialog riêng `18_AssignIncidentSeverityDialog.md` (nay Deprecated, xem file đó). Nguyên field/validation của UC-009, chỉ đổi vị trí trình bày |
| 6 | Đánh giá rủi ro lâm sàng | Textarea | Bắt buộc khi Medium/High | BR-147 | Gộp từ UC-009, xuất hiện cùng lúc với field 5 |
| 7 | Tường trình ban đầu (trước đây "First aid was administered") | Textarea nhập, bắt buộc trước khi "Lưu đánh giá & Bắt đầu điều tra" (đổi từ Text đọc — xem `review_medicalstaff_incidentmanagement.md` Mục 9.12, 11/07/2026) | Bắt buộc | — | Mô tả đầy đủ diễn biến ban đầu: phát hiện lúc nào, triệu chứng, đã sơ cứu gì. **Pre-fill từ nội dung "biên bản sự cố"** đã soạn ở `33_CreateIncidentRecordDialog.md` khi mở lần đầu (Mục 10.3/10.4) — trước đó chỉ pre-fill xuôi sang Clinical notes (UC-033), nay có thêm nguồn pre-fill ngược từ biên bản triage. Medical Staff vẫn sửa được. Field dùng chung với School Administration (`Web/SchoolAdministration/10_IncidentDetail.md` field 4) |
| 8 | Chọn món ăn nghi vấn | `Select` multi, bắt buộc | Bắt buộc trước khi "Lưu đánh giá & Bắt đầu điều tra"; lấy từ đúng Menu/`MealServiceSession` của "Ngày ăn" đã xác nhận | BR-149 | **Mặc định lọc theo món chế biến trong 48 giờ** tính từ thời điểm biên bản/báo cáo được tạo — căn cứ Quyết định 39/2006/QĐ-BYT (`00_Policies/39_2006_QD-BYT_quy_che_dieu_tra_ngo_doc_thuc_pham.md`), bổ sung 15/07/2026. Kèm nút **"Mở rộng phạm vi ngày"** để Medical Staff tự nới rộng khi nghi ngờ tác nhân ủ bệnh lâu hơn (không chặn cứng). **Giới hạn 15/07/2026 (đợt 2, Mục 11.6)**: "Mở rộng phạm vi ngày" chỉ nới tới **72h** — ngưỡng lưu mẫu tối thiểu BR-111 (`business_rules.md`), sau đó mẫu có thể đã bị hủy (BR-112). Món có mẫu đã `Disposed` hiển thị **mờ (disabled)** kèm hành động "Link Disposed Sample to Incident" (nhánh AF1 của UC-076) thay vì giả vờ vẫn thu hồi được. Chọn xong → Quarantine kích hoạt ngay đúng lô của (các) món được chọn (BR-149) |
| 9 | Nút "Lưu đánh giá & Bắt đầu điều tra" | Button (primary) | Disabled khi thiếu field 5/7/8 | BR-147, BR-148, BR-149 | **Gộp 15/07/2026** — thay cho 2 hành động tách rời (Step 1 dialog riêng → rồi mới thấy Step 2). Severity High → banner Emergency Response (E7) ngay khi lưu |
| 10 | Bản đồ truy xuất nguồn gốc (nếu có) | Panel `Collapsible` (thu gọn mặc định) | Biên soạn trong 4 giờ | BR-151 | Hợp nhất từ bản School Administration. **Sửa 15/07/2026 (đợt 2, Mục 11.7)**: mặc định **thu gọn** (progressive disclosure — Medical Staff cần chủ yếu để giải trình với School Admin/CDC, không phải theo dõi lâm sàng liên tục); **scope theo lô đã khóa** (`quarantineBatchIds[]` từ `suspectedDishIds[]`) thay vì toàn sự cố; hiển thị **lồng nhau** — gộp chung với field 11, mỗi dòng "Lô bị khóa" mở rộng ra đúng chuỗi truy xuất của lô đó. *(Đề xuất — cần đội dự án xác nhận khi cập nhật BR-151 vì ảnh hưởng cách tự động hoá truy xuất.)* |
| 11 | Danh sách lô hàng bị Quarantine | Table rút gọn (mỗi dòng mở rộng ra chuỗi truy xuất của lô — lồng với field 10) | — | BR-149 | Hợp nhất từ bản School Administration. Từ 15/07/2026 (đợt 2) hiển thị **lồng** với bản đồ truy xuất (Mục 11.7) thay cho 2 panel rời |
| 11b 🆕 | Vi phạm liên quan | Panel/Table đọc (hiển thị từ trạng thái `INVESTIGATING` trở đi) | — | BR-185, BR-189 | **Mới 15/07/2026 (Mục 11.5.3)** — liệt kê các `StaffViolationRecord`/`NonConformanceRecord` có `relatedIncidentId` = sự cố hiện tại (link chéo Epic 3 ↔ Epic 4, xem `22_RecordStaffViolation.md` field mới `relatedIncidentId`), mỗi dòng link sang `27_ViolationRegistry.md`. **Không** đổi luồng phê duyệt — BR-186/BR-153 vẫn 2 track độc lập, chỉ thêm tham chiếu chéo dữ liệu |
| 12 | Nút "Step 2: Incident Investigation Space" | Button (hành động riêng Medical Staff) | Chỉ Medical Staff | BR-150 | → `17_InvestigationWorkspace.md` |
| 13 | Nút "Hoàn tất sự cố" | Button (hành động riêng Medical Staff) | Hiện khi CAPA đã được School Admin duyệt (E17) | BR-152, BR-153, BR-154, BR-155 | **Gộp 15/07/2026** — thay cho 2 nút tách rời "Validate CAPA đã Approved" (`19_RecordCorrectiveActionsReportScreen.md`/UC-168) và "Step 3: Generate Incident Report" (`20_GenerateIncidentReportDialog.md`/UC-033). Mở màn hợp nhất gồm 2 khối tuần tự: (A) xác nhận khắc phục/hồi phục học sinh (UC-168), (B) soạn báo cáo chính thức (UC-033) — khối B chỉ mở khoá sau khi khối A hoàn tất. Xem ghi chú hợp nhất ở 2 file đó |
| 14 | Nút "Export" | Button | — | — | |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải chi tiết.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005, MSG-023.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ chọn dòng trên `25_IncidentList.md` (Web).
**Bố cục**: 2 cột — thông tin sự cố bên trái (`col-span-8`), action list (Step 1/2/3, Validate, Export) bên phải (`col-span-4`).

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ chọn 1 Card trên `25_IncidentList.md` (Mobile).
**Bố cục**: Report 3 §3.3.2.3.5 — AppBar, Section Header, block thông tin xếp dọc, 3 nút Step xếp dọc bên dưới.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Incident Detail, nền tảng Web, actor Medical Staff.
Bố cục: 2 cột — thông tin sự cố + severity RadioGroup + đánh giá rủi ro lâm sàng + Tường trình ban đầu +
Chọn món ăn nghi vấn (multi-select, mặc định 48h, nút Mở rộng phạm vi) bên trái, action list
(Lưu đánh giá & Bắt đầu điều tra, Step 2 Investigation, Hoàn tất sự cố, Export) bên phải.
Áp dụng Design System: primary #0E7490, critical #7E22CE (High severity).
Trạng thái cần thể hiện: loading, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4577–4591 (UC-116), dòng 5783–5807 (§3.3.2.3.5), dòng 4335–4349 (UC-009).
- business_rules.md (BR-146–155, Changelog v1.8).
- error_message.md (MSG-004, MSG-005, MSG-023).
- `00_Policies/39_2006_QD-BYT_quy_che_dieu_tra_ngo_doc_thuc_pham.md` (căn cứ khung 48h).
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.4/10.5/10.7 (gộp severity, khung 48h, gộp Bước 4 — 15/07/2026); Mục 11.4 (panel Biên bản gốc), Mục 11.5.3 (panel Vi phạm liên quan), Mục 11.6 (giới hạn 72h + Disposed), Mục 11.7 (scope + lồng ghép bản đồ truy xuất — đợt 2 15/07/2026).

**Ghi chú màn hình dùng chung**: giống bản Mobile — hợp nhất field dữ liệu ở bước review, giữ riêng hành động theo actor.
