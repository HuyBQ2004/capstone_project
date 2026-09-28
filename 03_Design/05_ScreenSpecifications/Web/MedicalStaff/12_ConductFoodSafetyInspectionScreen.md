# Screen Spec — Conduct Food Safety Inspection Screen

**Actor**: Medical Staff | **UC liên quan**: UC-014 (Report 3 §3.2, dòng 3443–3461), **UC-186 (Manage Kitchen Inspection Checklist — mới, 06/07/2026)** | **Status**: Draft
**BR liên quan**: BR-101 (kiểm tra hàng tháng), BR-102 (kiểm soát côn trùng), BR-103 (khám sức khỏe NV bếp), BR-104 (điểm vệ sinh), BR-110 (nhiệt độ tủ lưu mẫu — kiểm tra chéo, xem ghi chú), **BR-207 (bằng chứng theo từng mục + chú thích căn cứ pháp lý — đã chốt 05/07/2026, sửa đổi 06/07/2026)**, ~~BR-208 (phân loại Định kỳ/Đột xuất)~~ **Deprecated 09/07/2026**, **BR-210 (Medical Staff tự quản lý CRUD checklist — mới, 06/07/2026)**
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG cảnh báo điểm thấp (**chưa xác định mã**, xem ghi chú), MSG-094 (xóa hạng mục đang được tham chiếu)

> **Lịch sử cập nhật**
> - **2026-07-04**: đổi field "Upload ảnh bằng chứng" (1 ảnh chung cho cả checklist) → **đánh giá Đạt/Không đạt + bằng chứng theo từng hạng mục**; đề xuất BR-206/BR-207 để thay ghost-citation `BR-105`/`BR-119` (BR-105 canonical là quy tắc lưu mẫu thực phẩm, chủ đề khác; BR-119 không tồn tại trong `business_rules.md`).
> - **2026-07-05 (đính chính)**: bản 2026-07-04 đã gỡ nhầm `BR-110` với lý do "không liên quan tới checklist bếp". Sai — `Report 3.md` (dòng 3459, đặc tả gốc UC-014) liệt kê rõ BR-110 trong dòng "Business Rules" của chính UC-014. Đã khôi phục. BR-110 (2–8°C) vốn thuộc module Food Sample Retention (Service Caterers, UC-042/069); Medical Staff dùng lại đúng mã này khi kiểm tra chéo tủ lưu mẫu trong lúc audit bếp (UC-014) — không phải quy tắc mới. Đã bổ sung hạng mục D4 tương ứng vào `MedicalStaff_KitchenInspectionChecklist_Proposal.md`.
> - **2026-07-05 (quyết định chính thức của đội dự án, đợt 1)**:
>   1. **BR-207 được tạm chốt** (per-item Pass/Fail + bằng chứng có điều kiện + chú thích căn cứ pháp lý dạng icon "ℹ") — đã merge chính thức vào `business_rules.md` §12. Không còn ở trạng thái "đề xuất".
>   2. **BR-206 bị loại bỏ** — không chính thức hóa ngưỡng "điểm <60%" thành Business Rule mới (không có căn cứ pháp luật/quy định nội bộ).
>   3. **BR-208 mới** — hợp thức hóa field "Loại kiểm tra: Định kỳ / Đột xuất".
> - **2026-07-05 (quyết định chính thức của đội dự án, đợt 2 — descope hậu quả khóa bếp)**: đội dự án quyết định **bỏ hoàn toàn** hậu quả "khóa vận hành bếp, chặn phân phát bữa ăn" mà UC-014 AF1 gốc mô tả khi điểm <60% — đây là **descope có chủ đích** so với `Report 3.md` gốc, không phải sửa lỗi trích dẫn. Lý do: (a) không có căn cứ pháp lý cho việc khóa cả bếp chỉ vì 1 điểm số nội bộ; (b) `MSG-024` mà bản gốc dùng cho cơ chế này thực chất thuộc phạm vi **Incident Management** (theo `error_message.md`, MSG-024 được xếp vào module Incident Management, dùng cho BR-149 — cách ly **lô nguyên liệu** khi có sự cố ATTP) — SRS gốc đã mượn nhầm cơ chế của module khác cho một ngưỡng kiểm tra định kỳ. **Hành vi mới**: điểm <60% → hệ thống **vẫn tự động gửi cảnh báo tới School Administration** (bỏ Service Caterers/School Board — không còn là tình huống khẩn cấp cấp-sự-cố), nhưng **bếp tiếp tục vận hành bình thường, không chặn phân phát bữa ăn**. Kết quả Pass/Failed vẫn ghi nhận đủ theo BR-104. Không tái dùng `MSG-024` (sai module + sai ngữ nghĩa "operation blocked") — **cần đội dự án cấp mã MSG mới** cho cảnh báo không-chặn này. `BR-149` (cách ly lô nguyên liệu khi có sự cố) **giữ nguyên, không liên quan** tới quyết định này. Chi tiết xem `business_rules.md` §12 (ghi chú sau BR-208) và `MedicalStaff_UserStory.md` US-08.
> - **2026-07-06 (checklist chuyển sang dynamic, quyết định người dùng)**: checklist Kitchen Safety and Sanitation Checklist (UC-014) không còn là danh sách hạng mục cố định — Medical Staff nay có toàn quyền CRUD (thêm/sửa/xóa/sắp xếp) cả nhóm và hạng mục, kể cả nội dung chú thích căn cứ pháp lý (icon "ℹ"), **không cần System Admin cấu hình, không cần phê duyệt** (BR-210 mới, thay thế điều khoản "System Admin cấu hình chú thích" trong BR-207 gốc). Nội dung 6 nhóm checklist cụ thể (thay thế Nhóm A–F cũ) chuyển sang dùng làm **dữ liệu khởi tạo mặc định** — xem `MedicalStaff_KitchenInspectionChecklist_Proposal.md` (bản cập nhật 06/07/2026) và UC-186 (màn hình quản lý checklist mới, mô tả ở mục "Quản lý Checklist" bên dưới).
> Danh sách hạng mục checklist cụ thể (6 nhóm, thay cho Nhóm A–F cũ) và nội dung chú thích pháp lý lấy từ `MedicalStaff_KitchenInspectionChecklist_Proposal.md` vẫn là **bản nháp tham khảo nội dung/dữ liệu khởi tạo** — phần *cơ chế* (BR-207/BR-210) đã chốt, nhưng *nội dung câu chữ từng hạng mục cụ thể* do Medical Staff tự điều chỉnh sau khi hệ thống vận hành (CRUD, BR-210).
> - **2026-07-09 (deprecate BR-208)**: bỏ hẳn field/bước chọn "Loại kiểm tra: Định kỳ / Đột xuất" — không còn phân biệt ở bất kỳ tầng nào (kể cả thống kê ngầm). Lý do: chỉ phục vụ thống kê tần suất, không có căn cứ pháp luật bắt buộc, không ảnh hưởng cơ chế tính điểm. Xem `03_Design/08_FrontEnd/Web/review/medical_staff/review_medicalstaff_compliance_kitcheninspection.md` Mục 3. Đã xoá field 1b + mọi tham chiếu "bước đầu chọn Loại kiểm tra" trong Platform Layout/AI Prompt bên dưới.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "New Inspection" | Button | — | — | Bắt đầu phiên kiểm tra mới |
| 2 | Kitchen Safety and Sanitation Checklist | Danh sách hạng mục (mỗi dòng = 1 hạng mục, nhóm theo Card) | Bắt buộc đánh giá đủ mọi hạng mục | BR-101, BR-104 | Danh sách hạng mục cụ thể: xem `MedicalStaff_KitchenInspectionChecklist_Proposal.md` (6 nhóm — Hồ sơ, Người tiếp xúc trực tiếp, Bảo quản, Sơ chế/chế biến, An toàn thực phẩm, Kiểm thực 3 bước/lưu mẫu; dữ liệu khởi tạo, Medical Staff có thể tự CRUD qua UC-186) |
| 2e | Nút "Quản lý Checklist" (mở màn hình/luồng UC-186) | Button (secondary, đặt cạnh "New Inspection") | — | BR-210 | Điều hướng sang luồng CRUD nhóm/hạng mục/chú thích — xem mục "Quản lý Checklist (UC-186)" bên dưới |
| 2a | Mỗi hạng mục — Toggle "Đạt / Không đạt" | Toggle/Radio | Bắt buộc chọn 1 trong 2 cho từng hạng mục | BR-207 | Mỗi hạng mục có kết quả riêng, dùng làm input tính điểm tổng (BR-104) |
| 2b | Mỗi hạng mục — Ảnh minh chứng | Input type="file" + preview | **Đạt** → tùy chọn. **Không đạt** → bắt buộc ít nhất 1 ảnh gắn đúng hạng mục | BR-207 | Thay thế field "Upload ảnh bằng chứng vệ sinh bếp" cũ (1 ảnh chung cho cả phiên) |
| 2c | Mỗi hạng mục — Lý do không đạt (comment) | Textarea | Bắt buộc khi hạng mục = Không đạt; ẩn khi = Đạt | BR-207 | Nội dung có thể tái sử dụng (pre-fill) cho "Mô tả vi phạm" ở `21_RecordProcessViolation.md` nếu phát hiện leo thang thành vi phạm |
| 2d | Mỗi hạng mục — icon "ℹ" Chú thích căn cứ pháp lý | Icon mở Popover/Tooltip (thu gọn mặc định) | Không bắt buộc tương tác | BR-207, BR-210 | Nội dung text đơn giản, không versioning/effective-date, **theo từng trường** (không còn áp dụng chung mọi trường như bản BR-207 gốc). **[SỬA 06/07/2026]** Medical Staff tự sửa trực tiếp qua màn hình "Quản lý Checklist" (UC-186), không còn do System Admin cấu hình. Thay đổi nội dung ghi qua audit log chung (BR-170). Nội dung khởi tạo tham khảo: cột "Chú thích rút gọn" trong `MedicalStaff_KitchenInspectionChecklist_Proposal.md` |
| 3 | Xác minh chứng nhận kiểm soát côn trùng (quý) | Checkbox + ngày hết hạn | Bắt buộc còn hiệu lực | BR-102 | Giữ nguyên so với bản trước |
| 4 | Xác minh khám sức khỏe nhân viên bếp (năm) | Checkbox + ngày hết hạn | Bắt buộc còn hiệu lực | BR-103 | Giữ nguyên so với bản trước |
| 5 | Nút "Submit Checklist" | Button (primary) | Disabled khi: còn hạng mục chưa đánh giá, HOẶC có hạng mục Không đạt mà thiếu ảnh/thiếu lý do | BR-101–104, BR-207 | Hệ thống tự tính điểm từ kết quả Đạt/Không đạt của từng hạng mục — **công thức/trọng số tính điểm chưa chốt** (xem Ghi chú 4, `MedicalStaff_KitchenInspectionChecklist_Proposal.md`) |

### Trạng thái màn hình
- Loading: spinner khi tính điểm checklist sau submit.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005; hạng mục Không đạt thiếu ảnh/lý do → chặn submit, cảnh báo inline tại đúng hạng mục (**mã MSG cụ thể chưa chốt** — tạm đề xuất tái dùng MSG-085 "thiếu thông tin bắt buộc" hoặc cấp MSG mới, cần đội dự án xác nhận); điểm < 60% → hệ thống lưu kết quả **"Failed"**, hiển thị Alert (warning, không phải banner khóa cố định) và **tự động gửi cảnh báo tới School Administration** — **không khóa vận hành bếp, không chặn phân phát bữa ăn** (descope 05/07/2026 so với AF1 gốc; không gắn mã BR nào; **mã MSG chưa xác định**, không dùng MSG-024 — xem "Lịch sử cập nhật" ở trên).
- Success: điểm ≥ 60% → lưu "Passed", Toast xác nhận chung.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Conduct Food Safety Inspection" trên Dashboard (Web); thành công/thất bại → quay lại Dashboard.
**Bố cục**: `Stepper` ngang cố định trên form, mỗi bước hiển thị 1 nhóm checklist trong `Card` (đã bỏ bước chọn "Loại kiểm tra" — deprecate BR-208, 09/07/2026). Mỗi hạng mục là 1 dòng trong Card gồm: nhãn hạng mục + icon "ℹ" (mở Popover chú thích căn cứ pháp lý) + Toggle "Đạt/Không đạt" + khu vực ảnh (`Input type="file"` + preview, hiện luôn nhưng chỉ bắt buộc khi Không đạt) + Textarea lý do (chỉ hiện khi Không đạt). Nút "Submit Checklist" cố định cuối form.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Conduct Kitchen Inspection" trên Dashboard (Mobile); thành công/thất bại → quay lại Dashboard, Alert cảnh báo (không phải banner khóa) nếu Failed.
**Bố cục**: `Stepper` Flutter nhiều trang, mỗi bước 1 nhóm checklist (đã bỏ bước chọn "Loại kiểm tra" — deprecate BR-208, 09/07/2026). Mỗi hạng mục là 1 `Card` con gồm: nhãn hạng mục + icon "ℹ" (mở BottomSheet chú thích) + Toggle "Đạt/Không đạt" + nút camera (ưu tiên hiển thị đầu Card, chỉ bắt buộc chụp khi Không đạt) + ô nhập lý do (chỉ hiện khi Không đạt). Nút "Submit Checklist" ở bước cuối.

---

## Quản lý Checklist (UC-186, mới 06/07/2026)

> **BR liên quan**: BR-210. Đây là luồng riêng, tách khỏi luồng "thực hiện kiểm tra" (form chính ở trên) — vào từ nút "Quản lý Checklist" (field 2e).

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| M1 | Danh sách nhóm (group) checklist, dạng cây/accordion | Accordion + drag handle | — | BR-210 | Mỗi nhóm hiển thị tên + danh sách hạng mục con, kéo-thả để sắp xếp lại thứ tự |
| M2 | Nút "Thêm nhóm mới" | Button | Bắt buộc nhập tên nhóm, không để trống | BR-210 | Tạo nhóm rỗng, thêm hạng mục sau |
| M3 | Nút "Sửa tên nhóm" / "Xóa nhóm" (trên mỗi nhóm) | Icon button | Xóa nhóm có hạng mục → cảnh báo xác nhận trước (dialog) | BR-210 | Xóa không ảnh hưởng dữ liệu lịch sử đã lưu (chỉ ẩn khỏi phiên kiểm tra mới) |
| M4 | Nút "Thêm hạng mục" (trong mỗi nhóm) | Button | Bắt buộc nhập tên hạng mục | BR-210 | Hạng mục mới mặc định chưa có chú thích căn cứ pháp lý (có thể để trống) |
| M5 | Mỗi hạng mục — Nút "Sửa" / "Xóa" / kéo-thả sắp xếp | Icon button + drag handle | Xóa hạng mục **đang được tham chiếu bởi phiên kiểm tra đã lưu** → cảnh báo (MSG-094), vẫn cho phép xóa nhưng dữ liệu lịch sử được giữ nguyên, chỉ ẩn khỏi phiên mới | BR-210 | — |
| M6 | Mỗi hạng mục — Textarea "Chú thích căn cứ pháp lý" | Textarea | Không bắt buộc | BR-207, BR-210 | Text đơn giản, không versioning; hiển thị lại đúng nội dung này ở icon "ℹ" trong màn hình thực hiện kiểm tra (field 2d) |
| M7 | Nút "Lưu thay đổi" | Button (primary) | — | BR-170 | Mọi thay đổi (thêm/sửa/xóa/sắp xếp) ghi vào audit log chung khi lưu |

### Trạng thái màn hình
- Empty: chưa có nhóm nào → hiển thị gợi ý "Thêm nhóm đầu tiên", có thể nạp sẵn 6 nhóm mặc định từ `MedicalStaff_KitchenInspectionChecklist_Proposal.md` khi khởi tạo trường mới.
- Error: xóa hạng mục đang dùng → cảnh báo xác nhận (MSG-094), không phải lỗi chặn cứng — Medical Staff vẫn có thể xác nhận xóa (dữ liệu lịch sử được giữ, chỉ ẩn khỏi phiên mới).
- Success: Toast xác nhận đã lưu thay đổi checklist.

### Platform Layout — Web (Quản lý Checklist)
**Entry/Exit points**: vào từ nút "Quản lý Checklist" (field 2e) trên màn hình chính UC-014; thoát quay lại màn hình chính, checklist mới áp dụng ngay cho phiên kiểm tra tiếp theo.
**Bố cục**: 2 cột — cột trái là danh sách nhóm (Accordion, kéo-thả), cột phải là chi tiết hạng mục của nhóm đang chọn (danh sách hạng mục + Textarea chú thích cho hạng mục đang focus). Nút "Lưu thay đổi" cố định cuối trang.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Conduct Food Safety Inspection, nền tảng Web, actor Medical Staff.
Bố cục: Stepper ngang, mỗi bước 1 Card nhóm checklist (KHÔNG có bước chọn Loại kiểm tra — đã
deprecate BR-208, 09/07/2026). Mỗi hạng mục trong Card gồm: nhãn hạng mục, icon "ℹ" chú thích căn cứ pháp lý (Popover
thu gọn), Toggle Đạt/Không đạt, khu vực upload ảnh (bắt buộc chỉ khi Không đạt), Textarea lý do
(chỉ hiện khi Không đạt). Ngoài checklist còn có xác nhận chứng nhận côn trùng/khám sức khỏe NV bếp
còn hạn, nút Submit Checklist cuối form.
Áp dụng Design System: warning #B45309 cho Alert điểm <60% (không phải banner khóa cố định — bếp vẫn vận hành).
Trạng thái cần thể hiện: loading, error (thiếu ảnh/lý do tại hạng mục Không đạt; điểm <60% → Alert cảnh báo School Administration, không khóa bếp), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3443–3461 (UC-014 — bao gồm dòng "Business Rules": BR-101, BR-102, BR-103, BR-104, BR-105, BR-110; Postconditions/AF1 gốc mô tả khóa bếp — **đã descope 05/07/2026**, xem "Lịch sử cập nhật").
- `business_rules.md` §12 (BR-101, BR-102, BR-103, BR-104, BR-110, BR-207 — đã chính thức; ~~BR-208~~ Deprecated 09/07/2026; BR-210 — mới 06/07/2026; ghi chú cuối §12 về việc không chính thức hóa BR-206 và quyết định descope khóa bếp).
- `MedicalStaff_UserStory.md` (US-05–US-08, US-07b — US-08 đã cập nhật theo quyết định descope; US-22 — mới 06/07/2026, Epic 5, quản lý checklist).
- `MedicalStaff_KitchenInspectionChecklist_Proposal.md` (danh sách hạng mục 6 nhóm — thay Nhóm A–F cũ, 06/07/2026; nội dung chú thích căn cứ pháp lý — **bản nháp tham khảo/dữ liệu khởi tạo**, cơ chế CRUD đã chốt qua BR-210).
- `error_message.md` (MSG-004, MSG-005; MSG-024 không còn áp dụng cho màn này — thuộc Incident Management; MSG-094 — mới 06/07/2026, xóa hạng mục đang dùng).
- `traceability_matrix.md` (UC-186 — mới 06/07/2026).
