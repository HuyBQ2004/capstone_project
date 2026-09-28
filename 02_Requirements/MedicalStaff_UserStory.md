# User Story — Medical Staff (Nhật ký công việc hàng ngày/tuần)

> **Nguồn đối chiếu**: `02_Requirements/02_UseCases/MF03,MF07,MF08,MF11`; `02_Requirements/00_BusinessRules/business_rules.md`; `03_Design/03_Workflow/00_Business_Workflows.md` (Workflow 9, 11); `03_Design/05_ScreenSpecifications/Web(+Mobile)/MedicalStaff/*`.
> **Ngày biên soạn**: 04/07/2026 — dựa trên mô tả quy trình làm việc thực tế do người dùng cung cấp, đã đối chiếu ngược lại với SRS hiện có.
> **Cập nhật 04/07/2026 (v1.1)**: US-06/US-07 đã được review kiến trúc và **chốt hướng thiết kế** (per-item Pass/Fail + bằng chứng có điều kiện) — chuyển trạng thái sang `[DECIDED]`, kèm đề xuất Business Rule mới BR-207 ở Phụ lục A để đội dự án phê duyệt chính thức và merge vào `business_rules.md`.
> **Quy ước gắn nhãn** (bắt buộc đọc trước khi dùng tài liệu này làm căn cứ thiết kế):
> - `[SRS-CONFIRMED]` — khớp với BR/UC/Screen Spec đã có, có thể dùng làm căn cứ thiết kế ngay.
> - `[SRS-CLARIFIED]` — có trong SRS nhưng thứ tự/chi tiết trong câu chuyện gốc bị lệch; đã sửa lại theo đúng nguồn, có ghi chú khác biệt.
> - `[DECIDED — chờ merge BR/Screen Spec]` — đã được review kiến trúc và thống nhất hướng thiết kế trong phiên làm việc này; **chưa phải yêu cầu chính thức** cho tới khi đội dự án duyệt đề xuất BR ở Phụ lục A và cập nhật UC/Screen Spec tương ứng.
> - `[PROPOSED — cần quyết định]` — không có trong bất kỳ BR/UC/Screen Spec nào hiện tại; là đề xuất mới, **không được xem là yêu cầu chính thức** cho tới khi được duyệt và cấp mã BR/UC.

---

## Epic 1 — Xét duyệt Hồ sơ Sức khỏe & Dị ứng Học sinh (định kỳ ngày/tuần)

### US-01 `[SRS-CONFIRMED]`
**Là** Medical Staff, **tôi muốn** vào hệ thống kiểm tra danh sách các khai báo sức khỏe/dị ứng mới do phụ huynh nộp, **để** kịp thời xét duyệt trước khi thông tin có hiệu lực.
- Nguồn: UC-077 (Review Health Information), BR-031, Workflow 9 bước A3.
- Acceptance Criteria:
  - Dashboard hiển thị số lượng "Allergy pending approval" (`01_MedicalStaffDashboard.md`).
  - Chọn 1 hồ sơ → xem chi tiết khai báo của phụ huynh (`02_ReviewHealthInformationScreen.md`).

### US-02 `[SRS-CONFIRMED]`
**Là** Medical Staff, **tôi muốn** yêu cầu phụ huynh bổ sung/sửa thông tin khi hồ sơ chưa đủ căn cứ xác minh, **để** đảm bảo dữ liệu dị ứng chính xác trước khi kích hoạt cảnh báo cho bếp.
- Nguồn: Workflow 9, nhánh A4 (Yes) → A5 "Request Additional Information" → quay lại A3.
- Acceptance Criteria: hệ thống gửi yêu cầu bổ sung tới Parent; hồ sơ ở trạng thái "chờ bổ sung" cho tới khi Parent nộp lại.

### US-03 `[SRS-CLARIFIED]`
**Là** Medical Staff, **tôi muốn** xác minh và duyệt hồ sơ dị ứng khi thông tin đã đầy đủ, **để** hệ thống tự động kích hoạt Allergy Profile và cảnh báo cho Service Caterers/Teacher.
- Nguồn: UC-036 (View List Allergy & Special Meals), BR-031, BR-065, Workflow 9 bước A6→A10.
- **Khác biệt với mô tả gốc**: hành động của Medical Staff là **"xác minh/duyệt"**, không phải **"tạo hồ sơ sức khỏe"** — việc tạo Allergy Profile và Health Declaration Record do **System tự động thực hiện** ngay sau khi Medical Staff duyệt (Workflow 9 bước A2, A7 đều ghi actor là "System"). Nếu ý bạn là bổ sung ghi chú lâm sàng/thuốc men vào hồ sơ đã có, đó là một hành động riêng biệt (US-04).
- Acceptance Criteria: sau khi duyệt, Allergy Profile được tạo tự động; Service Caterers và Teacher nhận thông báo (BR-065, A9-A10).

### US-04 `[SRS-CONFIRMED]`
**Là** Medical Staff, **tôi muốn** bổ sung log lâm sàng/thuốc men vào hồ sơ sức khỏe học sinh đã tồn tại, **để** duy trì lịch sử y tế xuyên suốt.
- Nguồn: UC-094 (Update Health Records), BR-172.
- Ràng buộc: log lịch sử đã xác minh trước đó **không thể chỉnh sửa** (append-only, BR-172) — chỉ được bổ sung thêm, không sửa/xóa bản ghi cũ.

---

## Epic 2 — Kiểm tra An toàn Vệ sinh Bếp ăn (định kỳ ngày/tuần)

### US-05 `[SRS-CONFIRMED]`
**Là** Medical Staff, **tôi muốn** xuống bếp thực hiện kiểm tra theo Kitchen Safety and Sanitation Checklist, **để** đánh giá tuân thủ vệ sinh của bếp ăn trường học.
- Nguồn: UC-014 (Conduct Food Safety Inspection), BR-101–BR-104; Screen Spec `12_ConductFoodSafetyInspectionScreen.md`.
- Acceptance Criteria: checklist gồm đồng phục/vệ sinh cá nhân nhân viên, điều kiện khu vực chế biến/lưu trữ; xác minh chứng nhận côn trùng còn hạn (BR-102) và khám sức khỏe nhân viên bếp còn hạn (BR-103).

### US-06 `[DECIDED — chờ merge BR/Screen Spec]`
**Là** Medical Staff, **tôi muốn** đính kèm ảnh bằng chứng theo đúng mục checklist mà tôi vừa đánh giá, **để** lưu vết tình trạng vệ sinh thực tế gắn đúng vào từng điểm kiểm tra, không phải một ảnh chung chung cho cả phiên.
- Nguồn hiện tại: UC-014 bước 5, Screen Spec mục 5 ("Upload ảnh bằng chứng vệ sinh bếp — bắt buộc ít nhất 1 ảnh").
- **Thiết kế đã chốt (2026-07-04)**: ảnh gắn theo từng mục, không phải theo cả phiên. Với mục Đạt, ảnh là tùy chọn (optional); với mục Không đạt, ảnh là bắt buộc (chi tiết ở US-07/BR-207).
- Việc cần làm: cập nhật `12_ConductFoodSafetyInspectionScreen.md` (Web & Mobile) — đổi field "Upload ảnh bằng chứng vệ sinh bếp" (1 ảnh chung) thành ảnh gắn theo từng dòng checklist.

### US-07 `[DECIDED — chờ merge BR/Screen Spec]`
**Là** Medical Staff, **tôi muốn** với mỗi mục checklist có thể đánh dấu Đạt/Không đạt riêng, ảnh là tùy chọn nếu Đạt nhưng bắt buộc kèm ảnh + lý do (comment) nếu đánh dấu Không đạt, **để** truy vết chính xác mục nào không đạt, vì sao, mà không phải chụp ảnh tràn lan cho cả các mục đã đạt.
- **Tình trạng cũ**: không có BR/UC/Screen Spec nào mô tả cơ chế Pass/Fail + comment theo từng mục checklist (chỉ có 1 điểm số tổng và 1 ảnh chung).
- **Quyết định kiến trúc (2026-07-04)**: chốt theo hướng người dùng đề xuất — lý do:
  1. Nhất quán với nguyên tắc "bằng chứng gắn đúng điểm phát sinh vấn đề" đã dùng ở BR-083 (Ba Bước) và BR-183 (Biên bản Vi phạm — đều yêu cầu ảnh + mô tả tại đúng điểm lỗi, không phải 1 ảnh đại diện).
  2. UC-014 vốn đã ngầm định chấm điểm theo từng mục ("System calculates the audit score") — nên gắn ảnh/comment theo đúng granularity per-item là mở rộng tự nhiên, không phải khái niệm mới.
  3. Hệ quả của một lượt Fail ở đây khá nặng (Kitchen Quarantine, chặn toàn bộ phân phát bữa ăn, cảnh báo School Board — US-08) nên cần bằng chứng đủ mạnh để biện minh/tránh tranh chấp về sau.
  4. Giảm tải thao tác cho Medical Staff so với bắt ảnh mọi mục kể cả khi đạt — tránh "audit fatigue" làm giảm chất lượng dữ liệu.
  5. Ảnh + comment tại mục Fail có thể tái sử dụng (pre-fill) làm "Nội dung biên bản" + "Ảnh bằng chứng" của dialog Lập Biên bản Vi phạm (UC-179, dialog hợp nhất — UC-178 Deprecated 11/07/2026, BR-183) nếu phát hiện leo thang thành vi phạm chính thức — giảm nhập liệu trùng lặp.
- Business Rule đề xuất: xem **BR-207** ở Phụ lục A.
- Việc cần làm: đội dự án duyệt BR-207 → cập nhật UC-014 (Normal Flow bước 3, 5, 7) và Screen Spec `12_ConductFoodSafetyInspectionScreen.md` (Web & Mobile) theo cơ chế per-item.

### US-08 `[DECIDED — descope so với SRS gốc, 05/07/2026]`
**Là** Medical Staff, **tôi muốn** hệ thống tự động cảnh báo School Administration khi điểm chấm dưới ngưỡng 60%, **để** Ban giám hiệu biết ngay và tự quyết định hành động tiếp theo, **mà không làm gián đoạn việc phục vụ bữa ăn cho học sinh**.
- Nguồn gốc: UC-014 AF1 (`Report 3.md`, dòng 3455) — bản gốc mô tả điểm <60% sẽ **khóa vận hành bếp, chặn phân phát bữa ăn**, cảnh báo School Administration/Service Caterers/School Board (MSG-024).
- **Quyết định 05/07/2026 (đội dự án)**: loại bỏ hậu quả "khóa vận hành bếp/chặn phân phát bữa ăn" — đây là **descope có chủ đích** so với SRS gốc, không phải sửa lỗi trích dẫn. Lý do: (1) không tìm được căn cứ pháp lý nào cho việc khóa cả bếp chỉ vì 1 điểm số kiểm tra nội bộ; (2) `MSG-024`/`BR-105` mà bản gốc dùng cho cơ chế này thực chất thuộc phạm vi Incident Management (cách ly **lô nguyên liệu** khi có sự cố ATTP, BR-149) — SRS gốc đã mượn nhầm cơ chế của 1 module khác. Hành vi giữ lại: **chỉ cảnh báo School Administration** (bỏ Service Caterers/School Board — không còn là tình huống khẩn cấp cấp-sự-cố), bếp **vẫn vận hành bình thường**, kết quả Pass/Failed vẫn ghi nhận đủ (BR-104). Chi tiết xem `business_rules.md` §12 (ghi chú sau BR-208).
- Mã BR: không gán mã mới (giữ nguyên quyết định trước đó không chính thức hóa BR-206). Mã MSG cho cảnh báo mới: **MSG-093** (Warning) — xem `error_message.md` §3, `business_rules.md` §12.

### US-09 `[DECIDED — duyệt 05/07/2026]`
**Là** Medical Staff, **tôi muốn** chủ động biên soạn và gửi một báo cáo tuân thủ ATTP theo kỳ (Daily hoặc Weekly) tổng hợp kết quả kiểm tra bếp cho School Administration, **để** đảm bảo Ban giám hiệu luôn nắm được tình hình ATTP mà không cần tự vào xem dashboard.
- **Chưa có căn cứ SRS**: cơ chế hiện có là **Compliance Dashboard (UC-027)** — màn hình dùng chung, dữ liệu đổ real-time từ UC-014, nhưng theo mô hình **School Administration tự vào xem và "Ký xác nhận"** (pull), không có hành động "tạo & gửi báo cáo" chủ động từ phía Medical Staff.
- **Quyết định 05/07/2026 (đội dự án)**: chọn hướng (b) — bổ sung tính năng mới, **giữ nguyên** UC-027/Compliance Dashboard (pull) như một kênh song song, không thay thế. Lý do chọn (b) thay vì chỉ giữ (a):
  1. Rà soát phía frontend School Administration cho thấy Compliance Dashboard (UC-027) tuy đã có Screen Spec nhưng **chưa có route/trang triển khai thực tế** — nghĩa là mô hình pull hiện tại chưa vận hành được trên thực địa; bổ sung kênh chủ động (push) giúp thông tin vẫn đến được School Administration ngay cả khi màn Dashboard chưa/không được dùng thường xuyên.
  2. Không out-of-scope: tính năng tái sử dụng toàn bộ dữ liệu đã có sẵn từ UC-014 (KitchenInspection), cơ chế export chung BR-178, và cơ chế thông báo chung BR-158/159 — không cần model dữ liệu mới, không mở rộng phạm vi nghiệp vụ, chỉ thêm 1 hành động tổng hợp + gửi.
  3. Nhất quán với các cơ chế "chủ động gửi báo cáo" đã có ở module khác (UC-033 Generate Incident Report — Incident Management) — cùng pattern kiến trúc, không phải khái niệm mới trong hệ thống.
- Đổi kỳ báo cáo từ "Daily Report" (đề xuất ban đầu) thành **Daily hoặc Weekly** theo lựa chọn của Medical Staff tại thời điểm tạo báo cáo — linh hoạt hơn, không ép buộc tần suất cố định.
- Mã UC mới: **UC-185**. Mã BR mới: **BR-209**. Mã MSG mới: **MSG-091** (Success — gửi thành công), **MSG-092** (Error — không có dữ liệu trong kỳ). Chi tiết xem `business_rules.md` §12 (BR-209), `error_message.md` §1/§2, `traceability_matrix.md`.
- Việc cần làm: tạo Screen Spec `28_SendComplianceReportDialog.md` (Web & Mobile), cập nhật ScreenFlow Medical Staff (Web & Mobile) để thêm hành động này.

---

## Epic 3 — Xử lý Sự cố An toàn Thực phẩm

> Sự cố có thể được tạo tự động (≥3 học sinh báo triệu chứng tương đồng trong 24h, BR-146) hoặc do Teacher/Parent báo cáo (UC-074/UC-073) — Medical Staff luôn nhận thông báo và **mở một sự cố đã tồn tại sẵn**, không phải người khởi tạo sự cố từ đầu.

### US-10 `[SRS-CLARIFIED]`
**Là** Medical Staff, **tôi muốn** mở sự cố từ thông báo, đánh giá mức độ nghiêm trọng và sơ cứu y tế cho học sinh liên quan, **để** xử lý ưu tiên vấn đề sức khỏe trước khi điều tra sâu.
- Nguồn: UC-009 (Assign Incident Severity), UC-041, Workflow 11 bước E1–E5.
- **Khác biệt với mô tả gốc**: đây là bước **đầu tiên** trong luồng xử lý (không phải "vào sổ lưu mẫu trước") — Medical Staff phải đánh giá mức độ (Low/Medium/High) và sơ cứu **trước khi** thu hồi mẫu lưu (US-11). Nếu mức High, hệ thống lập tức cảnh báo Ban giám hiệu, Catering, và cơ quan y tế địa phương (BR-148).

### US-11 `[SRS-CLARIFIED]`
**Là** Medical Staff, **tôi muốn** tìm và thu hồi các mẫu lưu thực phẩm trong ngày liên quan để gửi xét nghiệm, đồng thời khóa mẫu không cho hủy, **để** phục vụ điều tra nguyên nhân.
- Nguồn: UC-076 (Retrieve Food Sample Records), BR-113, BR-112 (ngoại lệ chặn hủy); Screen Spec `14_RetrieveFoodSampleRecordsDialog.md`.
- **Khác biệt với mô tả gốc**: thao tác này diễn ra **sau** khi đã có Incident ID đang active (US-10) — form thu hồi mẫu bắt buộc nhập Incident ID hợp lệ, không thể thực hiện độc lập trước khi sự cố được mở trên hệ thống.
- Acceptance Criteria: tìm mẫu theo ngày/món/batch → chọn mẫu "Retained" → nhập Incident ID + người ủy quyền + phòng lab đích → Submit → mẫu bị khóa (không hủy được) + in biên nhận thu hồi.

### US-12 `[SRS-CONFIRMED]`
**Là** Medical Staff, **tôi muốn** thu thập bằng chứng, xác định nguyên nhân gốc rễ trên Investigation Workspace, **để** có căn cứ đề xuất phương án khắc phục.
- Nguồn: UC-041, BR-150, BR-151; Screen Spec `17_InvestigationWorkspace.md`.

### US-13 `[SRS-CONFIRMED]`
**Là** Medical Staff, **tôi muốn** đề xuất phương án khắc phục (CAPA) và giao cho Service Caterers thực hiện trong 12 giờ, **để** xử lý kỹ thuật nguyên nhân sự cố.
- Nguồn: UC-041 bước "Establish Measures", BR-152; Investigation Workspace.
- Acceptance Criteria: CAPA cần **School Administration phê duyệt** (UC-005, BR-153) trước khi Service Caterers triển khai — track này **độc lập, chạy song song** với track Vi phạm ở Epic 4 (không dùng chung cơ chế tự-duyệt LOW của BR-186).

### US-14 `[SRS-CONFIRMED]`
**Là** Medical Staff, **tôi muốn** xác nhận CAPA đã triển khai đầy đủ và tất cả học sinh liên quan đã hồi phục 100%, **để** sự cố đủ điều kiện đóng.
- Nguồn: UC-168 (Record Corrective Actions Report), BR-152, BR-155; Screen Spec `19_RecordCorrectiveActionsReportScreen.md`.
- Ràng buộc: chỉ hiển thị các sự cố mà CAPA **đã được School Admin duyệt** (qua UC-005); hệ thống **chặn xác nhận** nếu còn học sinh nào chưa hồi phục (EX1, MSG-004).

### US-15 `[SRS-CONFIRMED]`
**Là** Medical Staff, **tôi muốn** biên soạn Báo cáo Sự cố chính thức (triệu chứng lâm sàng, kết quả xét nghiệm, nguyên nhân gốc rễ), **để** lưu hồ sơ và gửi cho School Administration/cơ quan y tế.
- Nguồn: UC-033 (Generate Incident Report), BR-154; Screen Spec `20_GenerateIncidentReportDialog.md`.

### US-16 `[SRS-CLARIFIED]`
**Là** Medical Staff, **tôi muốn** biết ai là người có quyền đóng sự cố sau khi mọi việc xử lý xong, **để** không hiểu nhầm về phân quyền.
- Nguồn: BR-155 — chỉ **School Administration** có quyền Close Incident (UC-013).
- **Khác biệt với mô tả gốc**: Medical Staff **không tự đóng sự cố**. Vai trò của Medical Staff dừng lại ở US-14 (xác nhận CAPA + hồi phục 100%) — hành động này làm sự cố "đủ điều kiện đóng" (`Validated - Ready for Closure`), nhưng nút "Close Incident" chỉ School Administration mới có.

---

## Epic 4 — Quản lý Vi phạm (Violation Management)

> Cơ chế này **chạy song song, không thay thế** track CAPA ở Epic 3 (BR-185). Áp dụng cho vi phạm quy trình/sản phẩm (BR-183) và vi phạm hành vi cá nhân của nhân viên bếp/catering (BR-184).

### US-17 `[SRS-CONFIRMED]` — cập nhật 11/07/2026 (dialog hợp nhất)
**Là** Medical Staff, **tôi muốn** lập Biên bản Vi phạm (quy trình/sản phẩm hoặc hành vi nhân viên) ngay khi phát hiện, **để** ghi nhận đầy đủ người phát hiện, mô tả, ảnh bằng chứng, và mức độ nghiêm trọng.
- Nguồn: UC-179 (Record Staff Violation, BR-184) — **mã UC sống sót sau khi gộp UI với UC-178 (Record Process Violation, BR-183), quyết định 11/07/2026**; Screen Spec `22_RecordStaffViolation.md` (canonical); `21_RecordProcessViolation.md` (tham chiếu lịch sử, Deprecated).
- Acceptance Criteria: BR-183 hay BR-184 được **tự động suy ra** từ việc field "Nhân viên liên quan" (`staffName`, tùy chọn) có được điền hay không — không có bước chọn loại vi phạm thủ công. **Không còn field nhân chứng** (`witnessId` đã bỏ hẳn 11/07/2026) — PDF xuất ra vẫn giữ cột chữ ký thứ 3 "Người chứng kiến" để trống tĩnh theo mẫu giấy gốc. Đây luôn là **bước đầu tiên** của Epic 4.

### US-18 `[SRS-CLARIFIED]`
**Là** Medical Staff, **tôi muốn** đề xuất phương án xử lý sai phạm (hành động + mức kỷ luật) ngay sau khi Biên bản Vi phạm đã được lập, **để** quyết định hình thức xử lý phù hợp.
- Nguồn: UC-180 (Record Violation Resolution), BR-185; Screen Spec `24_RecordViolationResolution.md` — điều hướng trực tiếp từ `22_RecordStaffViolation.md` (dialog hợp nhất).
- **Khác biệt với mô tả gốc**: thứ tự đúng là **Biên bản Vi phạm (US-17) → Biên bản Xử lý Sai phạm (US-18)**, không phải ngược lại. "Phương án xử lý" không được gửi duyệt trước khi Biên bản Vi phạm tồn tại.

### US-19 `[SRS-CONFIRMED]`
**Là** Medical Staff, **tôi muốn** tự phê duyệt và đóng Biên bản Xử lý Sai phạm khi mức độ là LOW, **để** xử lý nhanh các vi phạm nhẹ mà không cần chờ School Administration.
- Nguồn: BR-186 — chỉ áp dụng cho track Vi phạm (khác với CAPA ở Epic 3, vốn luôn cần School Admin duyệt theo BR-153).

### US-20 `[SRS-CONFIRMED]`
**Là** Medical Staff, **tôi muốn** gửi Biên bản Xử lý Sai phạm cho School Administration phê duyệt khi mức độ MEDIUM/HIGH, **để** đảm bảo có sự giám sát cấp quản lý cho các vi phạm nghiêm trọng hơn.
- Nguồn: UC-182 (Approve/Reject Violation Resolution), BR-186.

### US-21 `[PROPOSED — cần quyết định]`
**Là** Medical Staff, **tôi muốn** ghi nhận việc đã đạt được sự đồng thuận với các bên liên quan (ngoài hệ thống) trước khi hoàn tất Biên bản Xử lý Sai phạm, **để** phản ánh đúng quy trình phối hợp thực tế ngoài đời trước khi chốt phương án trên hệ thống.
- **Chưa có [NGẮT — nội dung gốc bị cắt ngang tại đây từ trước, chưa xác định được phần còn thiếu; xem `review_screen_medical_staff.md` §6.2 mục 7 — vấn đề đã ghi nhận, chưa có quyết định khôi phục. Phần dưới đây (US-22) được bổ sung mới 06/07/2026, không liên quan tới US-21 bị ngắt.]**

---

## Epic 5 — Quản lý Checklist Kiểm tra Bếp (Kitchen Inspection Checklist Management)

### US-22 `[DECIDED]`
**Là** Medical Staff, **tôi muốn** tự thêm, sửa, xóa, và sắp xếp lại các nhóm và hạng mục trong Kitchen Safety and Sanitation Checklist (bao gồm cả nội dung chú thích căn cứ pháp lý ở icon "ℹ"), **để** checklist luôn phản ánh đúng quy định mới nhất của Bộ Y tế và phù hợp với đặc thù bếp ăn của trường mình, mà không phải chờ System Admin hay đội kỹ thuật can thiệp.
- Nguồn: UC-186 (Manage Kitchen Inspection Checklist), BR-210; `MedicalStaff_KitchenInspectionChecklist_Proposal.md` Mục 0.
- Acceptance Criteria:
  - Có thể thêm/xóa/đổi tên một **nhóm** (group) checklist.
  - Có thể thêm/xóa/đổi tên/sắp xếp lại thứ tự một **hạng mục** (item) trong một nhóm.
  - Có thể sửa nội dung **chú thích căn cứ pháp lý** (icon "ℹ") của từng hạng mục — văn bản đơn giản, không cần versioning.
  - **Không cần phê duyệt** từ School Administration hay System Admin cho bất kỳ thao tác nào ở trên.
  - Nếu xóa một nhóm/hạng mục **đang được tham chiếu bởi phiên kiểm tra đã lưu**: hệ thống không xóa dữ liệu lịch sử, chỉ ẩn khỏi các phiên kiểm tra mới (BR-210).
  - Mọi thao tác CRUD được ghi vào audit log chung (BR-170).
- *(Bổ sung 06/07/2026 — người dùng quyết định chuyển checklist UC-014 sang mô hình dynamic, thay thế điều khoản "System Admin cấu hình chú thích" trong BR-207 gốc. Đây là bổ sung **ngoài phạm vi `Report 3.md` gốc** (không có UC nào mô tả hành vi này trong SRS ban đầu). Cấp UC-186, BR-210 mới. Xem `business_rules.md` §12, `traceability_matrix.md`.)*

---

### US-23 `[DECIDED — duyệt 13/07/2026]`
**Là** Medical Staff, **tôi muốn** có 1 nơi (Report Center) để chủ động tổng hợp và gửi cho School Administration cả 3 loại dữ liệu ATTP mình quản lý — tình hình tuân thủ (kiểm thực 3 bước, checklist, côn trùng, sức khỏe NV), sự cố, và vi phạm — đồng thời xem lại được các báo cáo mình đã gửi trước đó, **để** không phải phụ thuộc hoàn toàn vào School Administration tự vào xem từng dashboard/danh sách, và tự tin rằng báo cáo đã gửi đi không bị "mất dấu".
- **Nguồn gốc**: mở rộng trực tiếp từ US-09 (UC-185) — người dùng phản hồi (rà soát ảnh chụp màn hình thực tế, "Hình 7") rằng UC-185 cần field chi tiết hơn + nút xuất PDF, đồng thời tự nhận định Medical Staff còn thiếu 1 trang xem lại lịch sử report đã gửi.
- **Quyết định kiến trúc** (qua 2 vòng làm rõ với người dùng):
  1. **Không** thêm nút "Xuất PDF" tách rời — PDF vẫn tự sinh khi bấm "Gửi báo cáo" (giữ nguyên cơ chế BR-178/BR-209); thay vào đó dialog hiển thị đầy đủ dữ liệu tổng hợp để soát lại trước khi gửi.
  2. Tách 3 loại báo cáo theo đúng 3 mảng dữ liệu Medical Staff quản lý, mỗi loại **chỉ rollup số liệu**, không lặp lại nội dung đã có ở nơi khác (UC-033 cho sự cố, BR-211 cho vi phạm) — tránh 2 nguồn dữ liệu cùng mô tả 1 sự việc.
  3. Có 1 màn hub chung ("ATTP Report Center") thay vì 3 điểm vào rời rạc.
  4. Có màn "Lịch sử báo cáo đã gửi" theo dõi cả 3 loại, thiết kế theo mẫu UC-107 (View Batch History), không dựa Audit Logs (Medical Staff chưa có quyền truy cập UC-106).
- Mã UC mới: **UC-187** (ATTP Report Center, hub), **UC-188** (Send Incident Summary Report), **UC-189** (Send Violation Summary Report), **UC-190** (View Sent Reports History — không cấp BR mới, hiện thực hóa BR-177 đã có sẵn). Mã BR mới: **BR-213**, **BR-214**. Mã MSG mới: **MSG-096/097** (Error), **MSG-098/099** (Success). Chi tiết xem `business_rules.md` §16 (Changelog v1.7), `error_message.md` (Changelog v1.3), `traceability_matrix.md`.
- Việc cần làm: tạo Screen Spec `29_ATTPReportCenter.md`, `30_SendIncidentSummaryDialog.md`, `31_SendViolationSummaryDialog.md`, `32_SentReportsHistory.md` (Web & Mobile); sửa `27_ViolationRegistry.md` (thêm filter khoảng ngày + Export) và `28_SendComplianceReportDialog.md` (thêm điểm vào từ hub); cập nhật `14_Screen_Authorization_Matrix.md` + ScreenFlow Medical Staff (Web & Mobile).
- *(Bổ sung 13/07/2026 — không có trong `Report 3.md` gốc. Xem `03_Design/08_FrontEnd/Web/review/medical_staff/review_screen_medical_staff.md` Mục 17 cho toàn bộ diễn giải quyết định.)*