# Review — Compliance Dashboard & Kiểm tra An toàn Bếp (UC-014/UC-027/UC-112/UC-115/UC-117) — Medical Staff

**Mục đích**: ghi nhận các quyết định thiết kế lại cho cụm màn hình Compliance Dashboard, Kiểm tra an toàn bếp 3 bước, Hồ sơ kiểm thực ba bước, và các báo cáo liên quan — "Nhóm C" trong đợt rà soát 14 ảnh đa module (09/07/2026).

**Ngày biên soạn**: 09/07/2026.

**Nguồn đã đọc/tra cứu để biên soạn**:
- `02_Requirements/00_BusinessRules/business_rules.md` — BR-089, BR-100–104, BR-110, BR-207, BR-208
- `02_Requirements/02_UseCases/MF07_Food_Safety_Inspection_Monitoring.md` — UC Monitor Food Safety Dashboard, View Food Safety Compliance Report, View Food Safety Reports
- `03_Design/08_FrontEnd/Web/src/app/(app)/service-caterers/inspections/[id]/page.tsx` — cấu trúc dữ liệu thật Mẫu 1/2/3 theo QĐ 1246/QĐ-BYT
- WebSearch: thực hành ghi nhật ký kiểm soát côn trùng (pest control log) theo HACCP; xác nhận QĐ 1246/QĐ-BYT chỉ quy định kiểm thực 3 bước + lưu mẫu, không quy định kiểm soát côn trùng
- Grep toàn dự án cho `BR-089`/`BR-110` (36 file liên quan) và `BR-208` (16 file liên quan) để xác định phạm vi ảnh hưởng
- Ảnh chụp code thật do người dùng cung cấp (09/07/2026): Compliance Dashboard (UC-027), Kiểm tra an toàn bếp (UC-014), Hồ sơ kiểm thực ba bước (UC-112) + modal chi tiết

---

## 1. Kiểm soát côn trùng (BR-102) — đổi từ "trạng thái hiệu lực" sang "nhật ký sự kiện"

**Vấn đề**: Compliance Dashboard hiển thị "Kiểm soát côn trùng theo quý (BR-102)" dưới dạng badge nhị phân "Còn hiệu lực"/"Đã hết hạn" — mô phỏng như 1 chứng chỉ có thời hạn pháp lý.

**Đối chiếu**: BR-102 canonical chỉ ghi *"Hệ thống phải lưu trữ nhật ký kiểm soát côn trùng (Pest Control Log) cho bếp ăn"* — không đề cập khái niệm "hiệu lực". Tra `QĐ 1246/QĐ-BYT` (văn bản đang dùng làm căn cứ cho BR-105/BR-203) xác nhận: văn bản này **chỉ quy định kiểm thực 3 bước và lưu mẫu**, không có nội dung về kiểm soát côn trùng — nên không có căn cứ pháp lý cụ thể nào cho mô hình "hiệu lực". Theo thực hành HACCP phổ biến (nguồn: các tài liệu hướng dẫn PMP — Pest Management Program), kiểm soát côn trùng đúng bản chất là **chuỗi các lần thực hiện/kiểm tra định kỳ** (mỗi lần ghi nhận số lượng côn trùng bắt được theo loại, xu hướng theo mùa), không phải 1 trạng thái nhị phân.

**Quyết định**: đổi model từ "trạng thái hiệu lực" sang **nhật ký sự kiện**, mỗi bản ghi gồm: Ngày kiểm soát/xử lý, Đơn vị thực hiện, Loại côn trùng phát hiện (nếu có) + số lượng ước tính, Biện pháp xử lý, Ngày hẹn kiểm tra tiếp theo. Compliance Dashboard hiển thị bản ghi gần nhất + ngày hẹn tiếp theo thay cho badge hiệu lực.

**Nguồn tham khảo** (WebSearch 09/07/2026):
- [Báo cáo kiểm soát và xử lý côn trùng gây hại trong trường học](https://khutrungtoancau.vn/diet-con-trung-uy-tin/bao-cao-kiem-soat-va-xu-ly-con-trung-gay-hai-trong-truong-hoc-113.html)
- [Hồ sơ kiểm soát côn trùng dịch hại trong tiêu chuẩn HACCP](https://pcs.com.vn/doanh-nghiep/linh-vuc/che-bien-thuc-pham/ho-so-kiem-soat-con-trung-dich-hai-trong-tieu-chuan-haccp-bao-gom-nhung-gi)
- [Quy trình kiểm soát côn trùng trong nhà máy — 4 bước chuẩn HACCP](https://dietcontrungtphcm.com/quy-trinh-kiem-soat-con-trung-nha-may/)

---

## 2. BR-089 (Deprecate) & BR-110 (giữ lại, bỏ cơ chế cảnh báo IoT)

**Vấn đề**: BR-089 ("giám sát nhiệt độ tủ lạnh **liên tục**, phát cảnh báo nếu lệch phạm vi >30 phút") và BR-110 ("nhiệt độ bảo quản mẫu 2-8°C, cảnh báo nếu lệch >30 phút") đều ngụ ý cơ chế **giám sát tự động, liên tục** — chỉ khả thi với tích hợp cảm biến IoT thật. Code hiện tại tự nhận đang dùng **"Dữ liệu cảm biến real-time (mock)"** — tức giả lập, không phải dữ liệu thật.

**Quyết định đã chốt — SỬA 11/07/2026 (đảo ngược 1 phần quyết định 09/07/2026 ở trên)**: người dùng chỉ ra rằng 2 rule không đối xứng — cần xử lý khác nhau:
- **BR-089 — Deprecate hoàn toàn**: rule này không có con số "phạm vi cho phép" cụ thể ở bất kỳ đâu trong dự án (đã kiểm tra `MedicalStaff_KitchenInspectionChecklist_Proposal.md` mục 3.5 và `ServiceCaterers/28_InventoryManagementDashboard.md` — cả 2 chỉ trích mã BR, không có field/số liệu thật), và không field nào trong toàn bộ Screen Spec thực thi nó — không có gì cụ thể để giữ lại làm giới hạn tĩnh. Xác nhận qua `AskUserQuestion` 11/07/2026: người dùng chọn deprecate thay vì cho số liệu mới.
- **BR-110 — KHÔNG deprecate, chỉ sửa nội dung**: có con số rõ ràng (2°C–8°C, tủ lưu mẫu, đã có field thật trong Food Sample Retention). Giữ nguyên giới hạn 2°C–8°C, **bỏ hẳn câu "hệ thống phát cảnh báo... trong hơn 30 phút"** (đây là phần ngụ ý IoT) — chuyển thành tiêu chí kiểm tra thủ công, cùng bản chất với BR-090 (nhiệt lõi, đo tại 1 thời điểm, không phụ thuộc cảm biến).
- **`business_rules.md` đã áp dụng xong** (11/07/2026): BR-089 đánh dấu `(Deprecated 11/07/2026)`, giữ số hiệu; BR-110 sửa nội dung, giữ số hiệu, không deprecate.

**Phân biệt quan trọng — KHÔNG deprecate các rule nhiệt độ khác**: BR-090 (nhiệt lõi ≥75°C, Bước 2 kiểm thực 3 bước, QĐ 1246 Điều 4) và rule nhiệt giữ nóng ≥60°C (Bước 3) là **phép đo thủ công tại 1 thời điểm cụ thể** (nhân viên cắm nhiệt kế, ghi 1 số liệu duy nhất khi chốt kết quả bước kiểm thực) — hoàn toàn khác về bản chất so với "giám sát liên tục" gốc của BR-089/BR-110, không phụ thuộc IoT, và có căn cứ pháp lý trực tiếp (QĐ 1246 Điều 4/5). **Giữ nguyên**, không nằm trong phạm vi mục này.

**Phạm vi ảnh hưởng còn lại** (grep toàn dự án, 36 file trích dẫn `BR-089`/`BR-110`, trừ `business_rules.md` đã xong) — **chưa áp dụng, chỉ ghi nhận phạm vi để xử lý ở đợt triển khai riêng**. Lưu ý: từ 11/07/2026, việc áp dụng vào các file bên dưới **không còn đơn giản là "xoá trích dẫn"** như dự tính ban đầu — cần phân biệt đúng theo từng file: nơi nào đang mô tả BR-089 (bỏ hẳn) khác với nơi đang mô tả BR-110 (giữ, chỉ bỏ chữ "cảnh báo"/"real-time"/IoT, giữ số 2–8°C):

| Nhóm | File chính cần sửa |
| --- | --- |
| Medical Staff | `12_ConductFoodSafetyInspectionScreen.md` (Web+Mobile), `00_TongHop_MedicalStaff.md` (Web+Mobile), `15_ViewFoodSafetyReportsScreen.md` (Web+Mobile), `13_FoodSampleRecords.md` (Web+Mobile), `03_Medical_Staff_Screen_Flow.md`; code: `medical-staff/samples`, `medical-staff/reports`, `medical-staff/compliance` |
| Service Caterers | `21_RecordSampleInformationForm.md`, `22_FoodSampleDetail.md`, `23_FoodSampleRetentionDashboard.md`, `28_InventoryManagementDashboard.md` (chỉ trích BR-089 — cần đổi sang không còn BR nào, hoặc để trống lý do "kiểm tra thủ công định kỳ, chưa có mã BR riêng"), `00_TongHop_ServiceCaterers.md` (Web+Mobile mỗi file); code: `service-caterers/samples`, `samples/[id]`, `dashboard`; Wireframe `21-23_FoodSampleRetention.html` (Web+Mobile) |
| School Administration | `00_TongHop_SchoolAdministration.md`, `13_FoodSampleRetentionReports.md`; code: `school-admin/reports/[slug]`; Wireframe `13_ReportsAndCompliance.html` |
| Cross-cutting | `00_Business_Workflows.md`, `class_diagram_description.md`, `class_diagram.puml`, `review_screen_medical_staff.md` |

Đây là thay đổi **đa actor, quy mô lớn** — không sửa ngay trong đợt này. Sẽ cần 1 đợt triển khai riêng (tương tự cách tổ chức task #23-29 cho Violation Management) sau khi các Nhóm còn lại của đợt rà soát 14 ảnh hoàn tất.

---

## 3. Deprecate BR-208 (phân loại kiểm tra Định kỳ/Đột xuất)

> **TRẠNG THÁI: ĐÃ CHỐT — sẵn sàng áp dụng vào các file thật ở đợt triển khai kế tiếp** (khác với Mục 2 — BR-089/BR-110 — vẫn đang ở mức ghi nhận vì quy mô quá lớn). Danh sách bên dưới là hướng dẫn áp dụng cụ thể theo từng file, không chỉ là liệt kê tên file.

**Vấn đề**: BR-208 (cấp mã 06/07/2026) bắt Medical Staff chọn tay "Định kỳ"/"Đột xuất" ở bước đầu tiên khi tạo phiên `Kitchen Safety and Sanitation Checklist` (UC-014) — chỉ phục vụ thống kê, không ảnh hưởng cơ chế tính điểm/ngưỡng cảnh báo (đã ghi rõ trong chính BR-208).

**Quyết định đã chốt**: **Deprecate hoàn toàn** BR-208 — bỏ hẳn bước chọn loại kiểm tra, không còn phân biệt Định kỳ/Đột xuất ở bất kỳ tầng nào (không giữ lại để thống kê ngầm).

Lưu ý: quyết định này **đảo ngược quyết định BR-207/BR-208 đã chốt 06/07/2026** (task #12/#13 trong lịch sử phiên làm việc) — đã xác nhận trực tiếp với người dùng trước khi ghi nhận ở đây.

**Hướng dẫn áp dụng cụ thể theo từng file** (grep toàn dự án, 16 file trích dẫn `BR-208`):

| # | File | Thay đổi cụ thể cần áp dụng |
| --- | --- | --- |
| 1 | `02_Requirements/00_BusinessRules/business_rules.md` | Đánh dấu mục BR-208 là **`(Deprecated 09/07/2026)`** ngay dưới tiêu đề — không xoá hẳn số hiệu (giữ vết lịch sử), ghi rõ lý do + trỏ tới file review này; xoá dòng "BR-208, ..." khỏi bảng tổng hợp module "Food Safety Monitoring" (dòng 606) |
| 2 | `02_Requirements/MedicalStaff_UserStory.md` | Rà soát US liên quan UC-014 có nhắc "Định kỳ/Đột xuất" — bỏ chi tiết đó khỏi acceptance criteria nếu có |
| 3 | `03_Design/05_ScreenSpecifications/Web/MedicalStaff/12_ConductFoodSafetyInspectionScreen.md` | Xoá bước "1. Loại kiểm tra (BR-208)" khỏi luồng 8 bước hiện tại (Loại kiểm tra → Nhóm 1-6 → Xác minh & nộp) — luồng mới bắt đầu thẳng từ "Nhóm 1 — Hồ sơ"; xoá field "Loại kiểm tra" khỏi panel "Tiến độ phiên kiểm tra" |
| 4 | `03_Design/05_ScreenSpecifications/Mobile/MedicalStaff/12_ConductFoodSafetyInspectionScreen.md` | Tương tự #3 (bản Mobile) |
| 5 | `03_Design/05_ScreenSpecifications/Web/MedicalStaff/00_TongHop_MedicalStaff.md` | Đồng bộ lại theo #3 sau khi sửa file lẻ |
| 6 | `03_Design/05_ScreenSpecifications/Mobile/MedicalStaff/00_TongHop_MedicalStaff.md` | Đồng bộ lại theo #4 sau khi sửa file lẻ |
| 7 | `03_Design/05_ScreenSpecifications/Web/MedicalStaff/23_ComplianceDashboard.md` | Xoá mọi cột/filter "Loại (BR-208)" trong bảng lịch sử kiểm tra bếp trên dashboard |
| 8 | `03_Design/05_ScreenSpecifications/Mobile/MedicalStaff/23_ComplianceDashboard.md` | Tương tự #7 (bản Mobile) |
| 9 | `03_Design/05_ScreenSpecifications/Web/MedicalStaff/28_SendComplianceReportDialog.md` | Xoá nội dung thống kê theo loại kiểm tra (nếu báo cáo tuân thủ có tách Định kỳ/Đột xuất) |
| 10 | `03_Design/05_ScreenSpecifications/Mobile/MedicalStaff/28_SendComplianceReportDialog.md` | Tương tự #9 (bản Mobile) |
| 11 | `03_Design/05_ScreenSpecifications/Web/SchoolAdministration/18_ComplianceDashboard.md` | Xoá cột/filter "Loại (BR-208)" ở bản School Administration (dashboard tổng hợp dùng chung dữ liệu) |
| 12 | `03_Design/05_ScreenSpecifications/Web/SchoolAdministration/00_TongHop_SchoolAdministration.md` | Đồng bộ lại theo #11 |
| 13 | `03_Design/08_FrontEnd/Web/review/medical_staff/review_screen_medical_staff.md` | Cập nhật ghi chú BR-207/BR-208 trước đó (06/07/2026) — bổ sung dòng trỏ sang quyết định deprecate mới (09/07/2026), tránh mâu thuẫn giữa 2 file review |
| 14 | Code: `medical-staff/conduct-inspection/page.tsx` | Xoá bước chọn Loại kiểm tra khỏi UI + state; bỏ field liên quan trong payload gửi lên |
| 15 | Code: `medical-staff/compliance/page.tsx` | Xoá cột/filter Loại kiểm tra khỏi bảng lịch sử |
| 16 | Code: `mock-data.ts` | Xoá/giữ field `inspectionType` tuỳ ý (không dùng nữa) trên interface `InspectionSession`-tương-đương của Medical Staff mock; không bắt buộc xoá ngay để tránh vỡ dữ liệu cũ, chỉ ngừng hiển thị |

**Thứ tự áp dụng khuyến nghị**: business_rules.md (#1) trước tiên → Screen Spec lẻ Web/Mobile (#3, #4) → TongHop đồng bộ (#5, #6) → các màn dùng chung dữ liệu (#7-#12) → review_screen_medical_staff.md (#13) → code (#14-#16) cuối cùng.

> **Cập nhật 09/07/2026 — ĐÃ ÁP DỤNG #1-13 (loại trừ #14-16 theo yêu cầu người dùng)**:
> - #1, #3, #4, #5, #6, #13: sửa đúng như hướng dẫn.
> - #2 (`MedicalStaff_UserStory.md`): rà soát, xác nhận **không có** chi tiết "Định kỳ/Đột xuất" trong acceptance criteria — chỉ có 1 dòng trỏ vị trí "business_rules.md §12 (ghi chú sau BR-208)", vẫn hợp lệ vì giữ nguyên số hiệu BR-208 → **không cần sửa**.
> - #7, #8, #9, #10, #11, #12: rà soát thực tế phát hiện **các file này chưa từng có cột/filter/thống kê theo "Loại kiểm tra"** — chỉ có các dòng tham chiếu định vị dạng "xem business_rules.md §12, ghi chú sau BR-208", vẫn hợp lệ (không đổi số hiệu BR) → **không cần sửa nội dung**, hướng dẫn gốc ở bảng trên dựa trên giả định chưa kiểm chứng.
> - #14-16 (code): loại trừ theo yêu cầu người dùng, để xử lý ở đợt sau.

---

## 4. Tách "Lịch sử kiểm tra bếp" thành nút + màn riêng

**Vấn đề**: màn "Kiểm tra an toàn bếp (UC-014)" hiện nhúng bảng "Lịch sử kiểm tra bếp" ngay bên dưới form đang nhập — chiếm không gian, không nhất quán với pattern đã dùng ở modal Hồ sơ sức khỏe (nút "Lịch sử sức khỏe (UC-146)" riêng, xem `review_medicalstaff_healthrecord.md` Mục 2.2).

**Quyết định**: tách thành nút **"Lịch sử kiểm tra"** ở header, dẫn tới màn riêng — bổ sung filter theo khoảng ngày và theo kết quả (Đạt/Không đạt), nhất quán hướng xử lý ở Mục 5 bên dưới.

---

## 5. Đổi filter Hồ sơ kiểm thực ba bước (UC-112): "Bước kiểm tra" → "Kết quả"

**Vấn đề**: màn "Hồ sơ kiểm thực ba bước (UC-112)" hiện có filter theo "Bước kiểm tra" (Bước 1/2/3) — ít hữu ích vì Medical Staff thường quan tâm phiên nào Đạt/Không đạt hơn là đang ở bước mấy.

**Quyết định**: đổi filter chính thành **"Kết quả"** (Đạt/Không đạt/Đang thực hiện — khớp đúng field `overall` đã có trong `InspectionSession`, xem `inspections/[id]/page.tsx`). Giữ lại filter "Món" hiện có; có thể giữ thêm filter phụ theo bước nếu không tốn công triển khai, nhưng "Kết quả" là filter chính thay thế.

---

## 6. Chi tiết phiên kiểm thực (Hình 9) — hiển thị đầy đủ theo đúng Mẫu 1/2/3 QĐ 1246, tham chiếu dữ liệu thật Service Caterers

**Vấn đề**: modal "Chi tiết phiên kiểm thực" phía Medical Staff hiện chỉ tóm tắt phẳng (Bước 1/2/3: badge Đạt/Không đạt + 1 dòng ghi chú chung), trong khi dữ liệu thật phía Service Caterers (`service-caterers/inspections/[id]/page.tsx`) chi tiết hơn nhiều theo đúng Mẫu 1/2/3 Phụ lục 1, QĐ 1246/QĐ-BYT.

**Quyết định**: modal chi tiết phía Medical Staff phải **render lại đúng cấu trúc dữ liệu thật** (read-only), chia theo bảng từng bước thay vì tóm tắt:

| Bước | Cấu trúc bảng cần hiển thị |
| --- | --- |
| Bước 1 (Mẫu số 1) | **Phần I — Tươi sống/đông lạnh**: tên thực phẩm, thời gian nhập, khối lượng, NCC + liên hệ, người giao hàng, chứng từ/giấy ĐK thú y/giấy kiểm dịch (có/không), kết quả cảm quan, xét nghiệm nhanh, ghi chú/biện pháp xử lý. **Phần II — Khô/bao gói sẵn**: tương tự + cơ sở sản xuất, hạn sử dụng, điều kiện bảo quản |
| Bước 2 (Mẫu số 2) | Ca/bữa ăn, tên món, nguyên liệu chính, số suất, giờ sơ chế xong, giờ chế biến xong, vệ sinh người chế biến/thiết bị/khu vực (đạt/không), cảm quan, **nhiệt độ lõi (°C, ngưỡng ≥75°C — BR-090)**, ghi chú/biện pháp xử lý |
| Bước 3 (Mẫu số 3) | Ca/bữa ăn, tên món, số suất, giờ chia món xong, giờ bắt đầu ăn, dụng cụ chia/chứa/che đậy, cảm quan, ghi chú/biện pháp xử lý |
| Chung mỗi bước | Ảnh bằng chứng + GPS + timestamp (BR-083); nếu Không đạt: lý do, mức độ nghiêm trọng, nhân chứng (bắt buộc nếu Trung bình/Cao — BR-183) |

Đúng nguyên tắc "dùng chung 100% dữ liệu/business logic giữa Web actor" đã ghi trong `08_Medical_Staff_Screen_Flow_Web.md` — Medical Staff xem đúng dữ liệu Service Caterers đã nhập, không tự tóm tắt lại.

---

## 6b. Trạng thái áp dụng vào code Medical Staff (12/07/2026) — `tsc --noEmit` sạch

- **Mục 1**: `PestControlLogEntry` + seed 3 bản ghi (resource mock `pest-control-log`); card BR-102 trên `compliance/page.tsx` đổi từ badge hiệu lực → bản ghi nhật ký gần nhất + ngày hẹn kiểm tra tiếp theo. (Chưa cấp mã BR mới thay BR-102 — vẫn chờ xác nhận, xem Mục 7.)
- **Mục 3 (#14–16)**: `conduct-inspection/page.tsx` bỏ bước chọn Loại kiểm tra + field khỏi payload/panel tiến độ; `compliance/page.tsx` + `reports/page.tsx` bỏ hiển thị Loại; `mock-data.ts` đánh dấu `type?` deprecated (giữ optional, ngừng hiển thị); `mock-server.ts` không ghi field type khi tạo phiên.
- **Mục 4**: tách "Lịch sử kiểm tra bếp" thành route riêng `conduct-inspection/history` (nút "Lịch sử kiểm tra" ở header) với filter khoảng ngày + kết quả Đạt/Không đạt.
- **Mục 5**: `inspections/page.tsx` filter chính đổi thành "Kết quả" (field `overall`), giữ filter Món; filter theo bước giữ làm filter phụ (spec cho phép).
- **Mục 6**: modal chi tiết phiên kiểm thực render đầy đủ read-only theo đúng cấu trúc `formHeader/step1Fresh/step1Dry/step2Rows/step3Rows` (Mẫu 1/2/3) mà Service Caterers đã nhập; phiên chưa điền chi tiết hiển thị ghi chú fallback.
- **Mục 2 (BR-089/BR-110)**: KHÔNG áp dụng trong đợt này — đúng quyết định "đợt triển khai riêng, đa actor" (các trích dẫn BR-089/real-time trong code Medical Staff vẫn còn, xử lý cùng đợt sweep).

## 7. Chưa xử lý trong đợt này

- Áp dụng deprecate BR-089/BR-110 và BR-208 vào toàn bộ file thực tế (Mục 2, 3) — quy mô lớn, đa actor, cần đợt triển khai riêng.
- Cấu trúc field mới cho "nhật ký kiểm soát côn trùng" (Mục 1) mới ở mức đề xuất field, chưa cấp mã BR mới thay BR-102 — cần xác nhận có cấp BR mới hay giữ BR-102 nhưng sửa nội dung.

---

## 8. Vòng rà soát thứ 2 (26/07/2026) — Báo cáo tuân thủ 3 phần, thiết kế lại luồng UC-014 (List→Detail→Collapsible→Biên bản), liên kết UC-112↔UC-118

**Bối cảnh**: 5 ảnh chụp code thật (Compliance Dashboard UC-027, Kiểm tra an toàn bếp UC-014, Hồ sơ kiểm thực ba bước UC-112, trang in Mẫu 1/2/3) do người dùng cung cấp. Nguồn đối chiếu: `12/23/28/29_*.md` (Medical Staff), `15-20_*.md` (Service Caterers, UC-052/062/063/065/066), `00_Policies/1246_QD-BYT_...md` (đọc trực tiếp Điều 3-8), `00_Policies/specialreport2173_1.md`, cơ chế ký 2 bên/xuất PDF của `medical-staff/violations/[id]/page.tsx` (BR-211), mã nguồn `compliance/page.tsx`, `conduct-inspection/page.tsx`, `conduct-inspection/history/page.tsx`, `inspections/page.tsx`, `samples/page.tsx`.

### 8.1. Đính chính 2 điểm trước khi thiết kế

1. **Nút "Gửi báo cáo tuân thủ" không phải trùng lặp ngoài ý muốn với ATTP Report Center** — đối chiếu `28_SendComplianceReportDialog.md` (cập nhật 05/07 và 13/07/2026): dialog này **được thiết kế có chủ đích** để mở từ cả 2 nơi (`23_ComplianceDashboard.md` **và** `29_ATTPReportCenter.md`), đã ghi rõ trong "Platform Layout — Web". Vấn đề thật không phải là thừa nút, mà là **nội dung dialog hiện tại quá sơ sài** (chỉ 6 field: kỳ báo cáo, điểm vệ sinh trung bình, list phiên Failed, côn trùng, khám sức khỏe NV, 1 ô ghi chú) so với "báo cáo 3 phần" người dùng mô tả.
2. **Lưu mẫu (UC-118) KHÔNG phải bước con của Bước 3 (UC-112)** — đọc trực tiếp `1246_QD-BYT`: Chương II "HƯỚNG DẪN KIỂM THỰC BA BƯỚC" (Điều 3-5, Bước 1/2/3, Mẫu số 1/2/3) và Chương III "LƯU MẪU THỨC ĂN" (Điều 6-8, Mẫu số 4/5) là **2 chương tách biệt** của cùng 1 Quyết định — không phải quan hệ lồng nhau. Chúng diễn ra gần cùng thời điểm (Điều 7.1: "mẫu được lấy trước khi bắt đầu ăn" — trùng lúc Bước 3 "kiểm tra trước khi ăn") nhưng là 2 nghĩa vụ pháp lý độc lập, dùng mẫu biểu riêng. Quyết định: không gộp màn, chỉ liên kết chéo (Mục 8.5).

### 8.2. Quyết định đã xác nhận (26/07/2026, qua `AskUserQuestion`)

1. **`28_SendComplianceReportDialog.md` (UC-185) chuyển từ `Dialog` sang route soạn riêng** — áp dụng đúng Pattern D (Document-centric, draft/final/export) đã dùng cho Sổ vi phạm (`27_ViolationRegistry.md`, xem `review_medicalstaff_incidentmanagement.md` Mục 12.3.2) và taxonomy Mục 13 (`review_medicalstaff_incidentmanagement.md`). Giữ nguyên 2 điểm vào (nút trên `23_ComplianceDashboard.md` **và** card trên `29_ATTPReportCenter.md`, cả 2 cùng dẫn tới route soạn này thay vì mở dialog). Bổ sung hẳn 1 phần mới "Trạng thái kiểm thực 3 bước hôm nay" (chi tiết Mục 8.3).
2. **UC-014 thêm cơ chế Service Caterers xác nhận checklist trước khi đóng hẳn** — thiết kế đầy đủ cả 2 phía trong mục này, nhưng **chỉ áp code cho Medical Staff** trong đợt tới; phần Service Caterers ghi vào `review_medicalstaff_to_servicecaterers.md` theo đúng tiền lệ đã dùng cho CAPA/thu hồi mẫu (chờ đợt review riêng của Service Caterers).
3. **Biên bản kiểm tra bếp tái dùng đúng cơ chế BR-211** (export PDF + upload ảnh đã ký, `exportAndPrint()`/`signedRecordPhoto` của `violations/[id]/page.tsx`) — chỉ khác: **bỏ hẳn field "Người làm chứng"**, chỉ còn 2 bên ký (Y tế trường học = người lập, tự động điền tài khoản đăng nhập; đại diện Service Caterers = chọn tài khoản, không nhập tay tự do — đúng quyết định đã áp cho `caterersRepresentativeId` ở Sổ vi phạm, `specialreport2173_1.md`).
4. **UC-112 ↔ UC-118 chỉ thêm cột liên kết, không gộp màn** — bảng "Hồ sơ kiểm thực ba bước" (`11_FoodInspectionRecords.md`/`inspections/page.tsx`) đổi cột "Món chính" (đang thừa vì modal chi tiết đã hiển thị đủ mọi món theo từng bước) thành **"Số mẫu đã lưu"** (đếm `FoodSample` cùng ngày+bữa), bấm vào điều hướng sang `13_FoodSampleRecords.md` đã lọc sẵn theo đúng ngày/bữa đó.

### 8.3. Thiết kế cụ thể — Phần 1: Báo cáo tuân thủ (route mới thay Dialog)

Giữ nguyên field 1-2 (chọn kỳ Daily/Weekly, khoảng thời gian auto-tính) và field 3-7 hiện có (điểm vệ sinh trung bình, list phiên Failed, côn trùng, khám sức khỏe NV, ghi chú) của `28_SendComplianceReportDialog.md` — chuyển nguyên vẹn sang route mới, không đổi field cũ. **Thêm mới**:

| # | Field | Loại | Nguồn dữ liệu | Ghi chú |
| --- | --- | --- | --- | --- |
| 1a | Tổng quan kiểm thực 3 bước trong kỳ | Card số liệu (đọc) | Đếm `InspectionSession` trong kỳ | "Đã hoàn tất phiên kiểm thực 3 bước cho X/Y bữa trong kỳ" — auto-tính từ số bữa có `overall` khác `IN_PROGRESS` / tổng số bữa có lịch phục vụ trong kỳ |
| 1b | Bảng chi tiết từng bữa trong kỳ | Table (đọc, auto-map) | `InspectionSession` (`date`, `meal`, `dish`, `step1/2/3`, `overall`, `inspector`, `formHeader.location`) | Cột: Bữa/ngày, Món, Kết quả (Đạt/Không đạt/Đang thực hiện), Bước không đạt (nếu có — suy từ `step1/2/3` khác PASSED), Người thực hiện (`inspector`), Vị trí bếp (`formHeader.location`). Map trực tiếp từ dữ liệu đã có trong `InspectionSession` — **không cần field mới**, chỉ cần route mới đọc đúng field đã tồn tại |
| 1c | Lý do không đạt (mỗi dòng Không đạt) | Text (đọc, auto-map) | `step2Rows[].action`/`step3Rows[].action`/`step1Fresh[].action` (field "Biện pháp xử lý/Ghi chú" đã có trong dữ liệu Mẫu 1/2/3) | Không tạo field mới — tái dùng đúng field `action` đã có ở mỗi dòng chi tiết bước (đã render ở modal `inspections/page.tsx`) |
| 1d | Nhận định tổng hợp (fallback khi dữ liệu không đủ diễn giải) | `Textarea` | Nhập tay | Chỉ dùng khi Medical Staff muốn viết thêm nhận định không tự map được (VD xu hướng nhiều ngày) — không dùng rich-text editor (TinyMCE) vì dự án chưa dùng thư viện này ở bất kỳ đâu khác, giữ nhất quán component `Textarea` sẵn có |
| — | Nút "Lưu nháp" / "Chốt & Gửi báo cáo" | Button | — | Áp đúng cặp nút Draft/Final đã thống nhất dùng xuyên suốt dự án (Mục 5.3 điểm 7, `review_medicalstaff_healthrecord.md` §4 tương tự) thay vì 1 nút "Gửi" duy nhất như Dialog cũ |

**Không đổi cơ chế MSG-092 (chặn gửi nếu kỳ không có phiên kiểm tra nào)** — áp dụng y nguyên cho route mới.

### 8.4. Thiết kế cụ thể — Phần 2: Kiểm tra an toàn bếp (UC-014) — thiết kế lại luồng

**Đổi điểm vào chính**: `conduct-inspection/history/page.tsx` (hiện là màn phụ, vào từ nút "Lịch sử kiểm tra") trở thành **điểm vào chính** của UC-014 — đổi tên route gốc `medical-staff/conduct-inspection` chỉ còn là màn TẠO phiên mới, còn Sidebar/Dashboard shortcut "Conduct Food Safety Inspection" trỏ vào màn lịch sử trước:

1. **Màn Lịch sử** (điểm vào chính): thêm nút "Chi tiết" mỗi dòng (hiện chưa có — bảng hiện tại chỉ hiển thị, không click-through) mở xem lại **toàn bộ checklist đã nộp dạng chỉ đọc** (từng hạng mục + Đạt/Không đạt + ảnh + lý do + chú thích pháp lý đã áp dụng tại thời điểm nộp). Nút "Tạo phiên kiểm tra mới" ở góc trên phải page header, điều hướng sang màn checklist. Nút "Quản lý Checklist (UC-186)" **chuyển từ header màn checklist sang header màn Lịch sử này** (đúng đề xuất người dùng — quản lý checklist là việc cấu hình, hợp lý đặt cạnh điểm vào chính hơn là cạnh form đang nhập dở).
2. **Màn checklist (tạo phiên mới)**: bỏ Stepper ngang dạng nút số ở đầu trang — thay bằng **Collapsible Panel mỗi nhóm** (tái dùng đúng component `CollapsiblePanel` đã có sẵn từ Incident Management, `review_medicalstaff_design_pattern.md` Mục 12.8.1 — không tạo component mới). Label mỗi nhóm giữ nguyên tên nhóm + thêm chỉ báo "Nhóm X/Y" ngay trong label hoặc trong nút "Tiếp theo" (VD nút hiển thị "Tiếp theo (Nhóm 3/6) →"). Bấm "Tiếp theo" đóng nhóm hiện tại + mở nhóm kế tiếp (giữ nguyên toàn bộ validate per-item đã có — BR-207 không đổi).
3. **Nút "Hoàn thành"** ở cuối checklist (thay vì "Nộp checklist" đóng luôn): chuyển phiên sang trạng thái mới **`AWAITING_CATERERS_CONFIRM`** thay vì đóng ngay — hiển thị banner "Đang chờ Service Caterers xác nhận kết quả kiểm tra" trên phiên đó. Sau khi Caterers xác nhận (Mục 8.4, phần Caterers bên dưới) → phiên chuyển `CONFIRMED`, mở khoá nút "Tạo biên bản sự việc".
4. **Nút "Tạo biên bản sự việc"** (chỉ hiện khi phiên `CONFIRMED`): mở route soạn biên bản mới, tái dùng đúng cơ chế BR-211 (Mục 8.2, Quyết định 3). Nội dung tự động điền:
   - Số hạng mục đã kiểm tra / số hạng mục Đạt / số hạng mục Không đạt (đếm từ `checks`/`ALL_ITEMS` của phiên).
   - Danh sách hạng mục Không đạt kèm lý do (từ `evidence[item].reason`).
   - Số ảnh bằng chứng đính kèm (từ `evidence[item].photos`).
   - 2 khối chữ ký: "Y tế trường học" (tự động = tài khoản đăng nhập) và "Đại diện Service Caterers" (chọn tài khoản, không có witness — Quyết định 3).
   - Nút "Xuất PDF" → in → ký tay → upload ảnh đã ký (`signedRecordPhoto`), đúng luồng đã có ở Sổ vi phạm.

**Phần Service Caterers (CHỈ GHI NHẬN — chờ đợt review riêng, không code trong đợt này)**:
- Cần 1 màn/danh sách "Checklist kiểm tra bếp cần xác nhận" hiển thị các phiên Medical Staff đã bấm "Hoàn thành" (`AWAITING_CATERERS_CONFIRM`), xem lại toàn bộ checklist (chỉ đọc) + nút "Đồng ý với kết quả kiểm tra" → chuyển phiên `CONFIRMED`.
- Sau khi biên bản đã được ký (PDF đã upload ảnh), màn xem checklist phía Caterers bỏ nút "Đồng ý" (đã xác nhận xong), chuyển thành màn xem chi tiết + nút "Xác nhận ký vào biên bản đã tạo" (nếu biên bản cần thao tác ký số riêng phía Caterers ngoài việc chỉ xem ảnh đã ký tay — cần làm rõ thêm ở đợt review Service Caterers, xem câu hỏi mở Mục 8.6).

### 8.5. Thiết kế cụ thể — Phần 3: Hồ sơ kiểm thực (UC-112)

1. **Thêm đánh giá Đạt/Không đạt riêng của Medical Staff** vào modal chi tiết hiện có (`inspections/page.tsx`) — đây là đánh giá **tách biệt** với `overall` (vốn tự tính từ business rule nhiệt độ BR-090/093/094 do Service Caterers nhập), phản ánh nhận định tổng thể của Y tế khi xem lại (VD: dữ liệu tự tính Đạt nhưng Y tế phát hiện vấn đề khác qua ảnh/mô tả). Cần field mới trên `InspectionSession`: `medicalReviewResult` (Đạt/Không đạt/Chưa đánh giá) + `medicalReviewReason` (bắt buộc khi Không đạt, hiện Textarea giống mẫu đã dùng ở UC-014). **Không ghi đè `overall`** — hiển thị cả 2 song song để phân biệt rõ "kết quả tự tính theo bước" và "đánh giá bổ sung của Y tế".
2. **Nút "In/Lưu PDF"** trong modal chi tiết Medical Staff: dùng lại đúng route in đã có phía Service Caterers (`service-caterers/inspections/[id]/print/page.tsx`) — Medical Staff chỉ cần 1 link mở route đó (đọc), không dựng lại trang in riêng (tránh 2 bản in khác nhau cho cùng 1 phiên dữ liệu).
3. **Cột "Món chính" → "Số mẫu đã lưu"** (Quyết định 4, Mục 8.2) — đếm `FoodSample` có cùng ngày+bữa với `InspectionSession`, hiển thị dạng link "n mẫu →" điều hướng sang `13_FoodSampleRecords.md`/`samples/page.tsx` đã áp filter ngày/bữa tương ứng.

### 8.6. Câu hỏi còn mở, chưa quyết

1. Cơ chế ký biên bản kiểm tra bếp phía Service Caterers — chỉ cần xem ảnh đã ký tay (giống Sổ vi phạm hiện tại, 1 bên upload ảnh chung) hay cần thao tác "xác nhận" riêng phía Caterers trên hệ thống (khác Sổ vi phạm)? Cần chốt khi làm đợt review riêng cho Service Caterers.
2. Trạng thái `AWAITING_CATERERS_CONFIRM` có cần thông báo đẩy (notification) tới Service Caterers hay chỉ hiển thị khi họ tự vào xem danh sách? Chưa chốt cơ chế push/pull.
3. Field `medicalReviewResult` (Mục 8.5, điểm 1) có cần đồng bộ ngược vào Compliance Dashboard (card "Điểm vệ sinh gần nhất") hay chỉ hiển thị tại modal chi tiết UC-112? Chưa quyết phạm vi hiển thị.

### 8.7. Checklist thay đổi cần áp dụng theo file (CHƯA áp code, chỉ ghi nhận)

| File | Thay đổi cần áp dụng |
| --- | --- |
| `28_SendComplianceReportDialog.md` | Đổi Platform Layout từ `Dialog` sang route riêng; thêm field 1a-1d (Mục 8.3); thêm cặp nút Lưu nháp/Chốt & Gửi |
| `medical-staff/send-compliance-report-dialog.tsx` → route mới (VD `medical-staff/report-center/compliance/page.tsx`) | Chuyển component từ Dialog sang page; giữ 2 điểm vào từ `compliance/page.tsx` và `report-center/page.tsx` |
| `12_ConductFoodSafetyInspectionScreen.md` | Đổi Platform Layout: Stepper ngang → Collapsible Panel mỗi nhóm; đổi nút "Nộp checklist" thành "Hoàn thành" (trạng thái mới `AWAITING_CATERERS_CONFIRM`); thêm mục "Tạo biên bản sự việc" sau khi `CONFIRMED` |
| `conduct-inspection/page.tsx` | Thay Stepper bằng `CollapsiblePanel` dùng chung; nút cuối đổi "Nộp checklist" → "Hoàn thành"; bỏ nút "Quản lý Checklist"/"Lịch sử kiểm tra" khỏi header (chuyển sang màn Lịch sử) |
| `conduct-inspection/history/page.tsx` | Thêm nút "Chi tiết" mỗi dòng (Dialog/route chỉ đọc); thêm nút "Tạo phiên kiểm tra mới" + "Quản lý Checklist (UC-186)" ở page header (chuyển từ `conduct-inspection/page.tsx`); hiển thị badge trạng thái `AWAITING_CATERERS_CONFIRM`/`CONFIRMED`; nút "Tạo biên bản sự việc" khi `CONFIRMED` |
| `mock-data.ts` | `KitchenInspection` thêm trạng thái `AWAITING_CATERERS_CONFIRM`/`CONFIRMED` (thay đóng thẳng khi Submit); `InspectionSession` thêm `medicalReviewResult`/`medicalReviewReason` (Mục 8.5) |
| `inspections/page.tsx` | Đổi cột "Món chính" → "Số mẫu đã lưu" (link sang `samples/page.tsx` đã filter ngày/bữa); modal chi tiết thêm khối đánh giá Đạt/Không đạt riêng của Y tế + nút "In/Lưu PDF" (link route in của Service Caterers) |
| `11_FoodInspectionRecords.md` | Cập nhật cột bảng (Mục 8.5, điểm 3); thêm field đánh giá riêng Y tế (điểm 1) |
| `13_FoodSampleRecords.md` | Ghi chú điểm liên kết chéo mới từ `11_FoodInspectionRecords.md` |
| `review_medicalstaff_to_servicecaterers.md` | Thêm mục mới: màn xác nhận checklist kiểm tra bếp phía Service Caterers (Mục 8.4, phần Caterers) — chỉ ghi nhận, chưa thiết kế field chi tiết |

**Trạng thái**: Mục 8 là **ghi nhận + chốt hướng thiết kế** qua `AskUserQuestion` (26/07/2026) — ~~chưa sửa code~~ → **ĐÃ ÁP CODE 27/07/2026, xem Mục 8.8**; phần Screen Spec (`28`/`12`/`11`/`13`) và phần Service Caterers vẫn CHƯA áp, đúng phạm vi đã chốt (Mục 8.2 QĐ 2).

### 8.8. Trạng thái áp dụng CODE — 27/07/2026

Phạm vi đợt này: **chỉ code Web phía Medical Staff**. Các dòng Screen Spec của bảng 8.7 (`28_SendComplianceReportDialog.md`, `12_ConductFoodSafetyInspectionScreen.md`, `11_FoodInspectionRecords.md`, `13_FoodSampleRecords.md`) để lượt docs riêng; màn xác nhận checklist phía Service Caterers chỉ ghi nhận (đã có ở `review_medicalstaff_to_servicecaterers.md`), **không đụng code `service-caterers/*`**.

| Quyết định | File code | Chi tiết đã áp |
| --- | --- | --- |
| Mục 8.2 QĐ 1 + 8.3 | `report-center/compliance/page.tsx` (MỚI), `compliance/page.tsx`, `report-center/page.tsx`, `send-compliance-report-dialog.tsx` | UC-185 chuyển từ Dialog sang **route soạn riêng** (Pattern D): giữ nguyên field 1–7 cũ + **thêm 1a** (tổng quan "X/Y bữa đã hoàn tất kiểm thực"), **1b** (bảng chi tiết từng bữa: bữa/ngày · món · kết quả · bước không đạt · người thực hiện · vị trí bếp — auto-map từ `InspectionSession`, không field mới), **1c** (lý do không đạt tái dùng field `action` sẵn có của Mẫu 1/2/3), **1d** (nhận định tổng hợp — `Textarea`, không rich-text), cặp nút **Lưu nháp / Chốt & gửi**; giữ MSG-092 chặn kỳ rỗng. Cả 2 điểm vào (Compliance Dashboard + card Report Center) đều deep-link sang route mới; dialog cũ đánh dấu `@deprecated` |
| Mục 8.4 điểm 1 | `conduct-inspection/history/page.tsx` | Màn Lịch sử thành **điểm vào chính** UC-014: header có "Tạo phiên kiểm tra mới" + "Quản lý Checklist" (chuyển từ header màn checklist sang); mỗi dòng có nút **"Chi tiết"** mở dialog xem lại toàn bộ checklist đã nộp (chỉ đọc: từng hạng mục + Đạt/Không đạt + lý do + số ảnh + 2 chứng nhận BR-102/103); cột mới "Xác nhận của bếp" |
| Mục 8.4 điểm 2 | `conduct-inspection/page.tsx`, `collapsible-panel.tsx` | Bỏ Stepper ngang; mỗi nhóm là 1 `CollapsiblePanel` (component dùng chung) với nhãn "Nhóm x/y — <tên nhóm>" + tóm tắt tiến độ; nút **"Tiếp theo (Nhóm x/y)"** đóng nhóm hiện tại, mở nhóm kế. `CollapsiblePanel` được bổ sung **chế độ controlled** (`open`/`onOpenChange` optional) — mọi nơi đang dùng uncontrolled không phải sửa |
| Mục 8.4 điểm 3 | `conduct-inspection/page.tsx`, `mock-data.ts` | Nút cuối đổi "Nộp checklist" → **"Hoàn thành phiên kiểm tra"**: phiên lưu kèm `confirmStatus: "AWAITING_CATERERS_CONFIRM"` thay vì đóng ngay; thêm `itemResults[]` (snapshot từng hạng mục cho màn xem lại); `KitchenInspection` thêm `confirmStatus`/`confirmedBy`/`confirmedAt` |
| Mục 8.4 điểm 4 + 8.2 QĐ 3 | `conduct-inspection/[id]/record/page.tsx` (MỚI), `mock-data.ts` | Route **Biên bản kiểm tra an toàn bếp**: chỉ mở khi phiên `CONFIRMED` (chặn cả khi vào bằng URL trực tiếp); nội dung tự điền (số hạng mục đã kiểm tra/đạt/không đạt, danh sách không đạt kèm lý do, số ảnh); **2 khối ký** — Y tế (tự động) + đại diện Service Caterers (chọn tài khoản), **KHÔNG có "Người làm chứng"**; tái dùng đúng cơ chế BR-211 (xuất & in → ký tay → tải ảnh/scan bản ký, nhiều tệp, xoá được); field mới `recordCaterersRep`/`recordPdfExportedAt`/`recordPdfVersion`/`recordSignedFiles` |
| Mục 8.5 điểm 1 | `inspections/page.tsx`, `mock-data.ts` | Modal chi tiết UC-112 thêm khối **"Đánh giá của Y tế khi xem lại"** (Đạt/Không đạt + lý do bắt buộc khi Không đạt), lưu vào `medicalReviewResult`/`medicalReviewReason`/`medicalReviewBy`/`medicalReviewAt` — **KHÔNG ghi đè `overall`**; danh sách hiển thị 2 badge song song |
| Mục 8.5 điểm 2 | `inspections/page.tsx` | Nút **"In / Lưu PDF"** dùng lại route in sẵn có của Service Caterers (`/service-caterers/inspections/[id]/print`), không dựng bản in riêng |
| Mục 8.5 điểm 3 | `inspections/page.tsx`, `samples/page.tsx` | Cột "Món chính" → **"Số mẫu đã lưu"** (đếm `FoodSample` cùng ngày/bữa), bấm vào mở Sổ lưu mẫu đã lọc sẵn; `samples/page.tsx` nhận deep-link `?date=dd/MM/yyyy&meal=Trưa|Phụ` |

**Câu hỏi mở Mục 8.6 vẫn để nguyên** (cơ chế ký phía Caterers, notification push cho `AWAITING_CATERERS_CONFIRM`, phạm vi hiển thị `medicalReviewResult` trên Compliance Dashboard) — không tự quyết trong đợt áp code.

**Lưu ý kiểm thử**: đợt này **chưa chạy được `tsc --noEmit`** (sandbox Linux không khởi động được tại thời điểm áp code — cùng tình trạng đã ghi ở `review_medicalstaff_healthrecord.md` Mục 4.8); đã rà thủ công import/tham chiếu/cân bằng JSX của 9 file đã sửa. Cần chạy lại type-check khi môi trường khả dụng.

---

## 9. Vòng rà soát thứ 3 (26/07/2026) — Cơ chế "Biên bản sự việc" (Mục 8.4 điểm 4/8.8) đổi theo quyết định 2-tick mới

**Nguồn**: `Phỏng vấn trải nghiệm UI ngày 25_7.md` Mục II.8, đã xác nhận với người dùng và ghi quyết định gốc tại `review_medicalstaff_violations.md` Mục 9.2 — cơ chế ký Biên bản Vi phạm đổi từ PDF+in+ký tay+chụp ảnh (BR-211) sang **2 nút tích điện tử bắt buộc** (Y tế + Nhà thầu, không cần Người làm chứng).

**Ảnh hưởng trực tiếp tới file này**: Mục 8.2 Quyết định 3 và Mục 8.4 điểm 4/Mục 8.8 (dòng "Mục 8.4 điểm 4 + 8.2 QĐ 3") đã thiết kế **và áp code 27/07/2026** cho "biên bản sự việc" kiểm tra bếp bằng cách "tái dùng đúng cơ chế BR-211" (`conduct-inspection/[id]/record/page.tsx`: xuất PDF → in → ký tay → tải ảnh 2 bên ký). Vì BR-211 tự thân đã đổi cơ chế (Mục 9.2 file Violations), biên bản kiểm tra bếp phải đổi theo **cùng cơ chế mới**, không còn "tái dùng PDF+ký tay" như đã ghi.

**Quyết định**: đổi Mục 8.4 điểm 4 (và code đã áp ở Mục 8.8) từ "xuất PDF → in → ký tay → tải ảnh" sang **2 nút tích điện tử** — "Y tế xác nhận" (tự động tài khoản đăng nhập) và "Đại diện Service Caterers xác nhận" (chọn tài khoản). Đủ 2 tick → biên bản khóa, có giá trị lưu trữ; không bắt buộc bước in/ký tay/chụp ảnh (giữ "Xuất PDF" như tùy chọn phụ nếu cần bản giấy nộp đoàn thanh tra, không phải điều kiện khóa biên bản).

**Nội dung cần sửa lại** (đảo ngược 1 phần code đã áp 27/07/2026):
- Route `conduct-inspection/[id]/record/page.tsx`: bỏ gate `recordSignedFiles` (ảnh bản ký) làm điều kiện khóa; thay bằng 2 field `recordConfirmedByMedicalStaffAt`/`recordConfirmedByCaterersAt` — đủ cả 2 mới khóa biên bản.
- Giữ `recordPdfExportedAt`/`recordPdfVersion` như tùy chọn xuất bản giấy phụ (không đổi tên field, chỉ đổi vai trò từ bắt buộc → tùy chọn).
- Đồng bộ với `review_medicalstaff_violations.md` Mục 9.2 — dùng chung 1 tên gọi cơ chế ("2 nút tích điện tử") và chung field pattern (`confirmedBy...At`) giữa Violation Registry và Biên bản kiểm tra bếp, tránh 2 cách đặt tên khác nhau cho cùng 1 cơ chế.
- Mục 8.6 câu hỏi mở #1 ("cơ chế ký phía Caterers — xem ảnh hay thao tác xác nhận riêng") coi như **đã được trả lời** bởi quyết định này: Caterers thao tác xác nhận riêng (tick nút "Đại diện Service Caterers xác nhận" trên tài khoản của họ), không phải chỉ xem ảnh đã ký.

**Checklist cập nhật**:

| File / Component | Thay đổi | Trạng thái |
| --- | --- | --- |
| `conduct-inspection/[id]/record/page.tsx` | Đổi gate khóa biên bản từ ảnh bản ký sang 2 field xác nhận điện tử; giữ xuất PDF là tùy chọn phụ | **ĐÃ ÁP CODE 26/07/2026** |
| `mock-data.ts` | Thêm `recordConfirmedByMedicalStaffAt`/`recordConfirmedByCaterersAt` trên `KitchenInspection`/biên bản liên quan | **ĐÃ ÁP CODE 26/07/2026** |
| `12_ConductFoodSafetyInspectionScreen.md` | Cập nhật mô tả bước khóa biên bản theo cơ chế 2-tick | CHƯA áp (Screen Spec) |
| Mục 8.6 câu hỏi mở #1 | Đánh dấu **đã trả lời** — xem quyết định Mục 9 | Đã đóng |

**Trạng thái chung**: quyết định chốt 26/07/2026, **code Medical Staff ĐÃ ÁP cùng ngày** — chi tiết Mục 12.1.

> **Lưu ý phụ thuộc chéo**: `review_medicalstaff_violations.md` Mục 9.2 (cùng cơ chế 2-tick cho Biên bản Vi phạm) **vẫn CHƯA áp code** — hiện Sổ vi phạm còn chạy cơ chế PDF+ký tay+ảnh cũ, trong khi Biên bản kiểm tra bếp đã chuyển 2-tick. Đây là điểm KHÔNG NHẤT QUÁN tạm thời đã biết; tên field ở hai bên đã cố ý đặt cùng khuôn (`...ConfirmedByMedicalStaffAt`/`...ConfirmedByCaterersAt`) để đợt áp code Violations chỉ việc theo.

---

## 10. Vòng rà soát thứ 4 (26/07/2026) — Bỏ "Trung tâm Báo cáo ATTP" (hub), thiết kế lại Compliance Dashboard theo triết lý case-hub

> **TRẠNG THÁI: đã chốt qua tranh luận trực tiếp nhiều vòng (đối chiếu code thật `report-center/`, `dashboard/`, `reports/`, `compliance/` — Medical Staff; `reports/`, `compliance/`, `incidents/`, `violations/`, `feedback/`, `dashboard/` — School Administration) — CHƯA áp code, CHƯA áp Screen Spec.**

### 10.1. Bối cảnh

Đối chiếu 3 màn Medical Staff (`report-center/page.tsx`, `dashboard/page.tsx`, `reports/page.tsx`) với `compliance/page.tsx` phát hiện: 2/3 card của `report-center/page.tsx` (Tuân thủ, Vi phạm) chỉ làm nhiệm vụ `router.push()` sang route khác — không còn dialog tại chỗ từ 27/07/2026; chỉ card Sự cố còn giữ dialog thật. Đồng thời `reports/page.tsx` (UC-115/117, "Xem báo cáo ATTP") trùng lặp đáng kể dữ liệu với `compliance/page.tsx` (điểm kiểm thực, pest control, khám SK NV bếp) — khác biệt thật sự chỉ là lọc theo khoảng ngày + export. `dashboard/page.tsx` có StatCard "Kiểm thực hôm nay" **hardcode** `value="Bước 2/3"` (dòng 57) — không tính từ dữ liệu `InspectionSession` thật như Compliance đang làm, tức có nguy cơ hiển thị sai lệch với dữ liệu thật bất kỳ lúc nào.

Đối chiếu song song phía School Administration (theo yêu cầu người dùng, để xem 2 bên khớp nhau chưa): `school-admin/reports/page.tsx` (Báo cáo tuân thủ) không có card Sự cố/Vi phạm dù đây thuộc phạm vi giám sát BGH; `school-admin/incidents/page.tsx` chỉ có export danh sách lọc theo severity/status/caterer (UC-031, self-serve, pull), không có báo cáo quản trị tổng hợp theo kỳ (xu hướng, phân bố, CAPA tồn đọng). Grep toàn bộ thư mục `school-admin` cho `SentReport`/`sent-reports` → **0 kết quả** — xác nhận School Administration hiện **không có màn nào nhận/đọc** 3 loại báo cáo Medical Staff chủ động gửi (UC-185/188/189).

### 10.2. Quyết định — bỏ hẳn "Trung tâm Báo cáo ATTP" dạng hub

Bỏ màn hub 3-card (`report-center/page.tsx`) và mục nav "Trung tâm Báo cáo ATTP". Lý do: 2/3 card đã thuần là điều hướng, phần còn lại (Sự cố) sẽ bị deprecate hoàn toàn (Mục 10.5) — không còn lý do giữ 1 màn trung gian chỉ để chọn loại báo cáo rồi chuyển tiếp.

**UC-187 "ATTP Report Center" → Deprecated.** UC-185/189/190 vẫn sống nguyên vẹn (không đổi field/nội dung, chỉ đổi route/entry point). **UC-188/BR-213 (Báo cáo Tổng hợp Sự cố do Medical Staff soạn-gửi) → Deprecated** (đã áp `business_rules.md` — xem ghi chú BR-213) — vì đối chiếu code xác nhận School Administration đã có đủ dữ liệu Incident để tự làm báo cáo quản trị theo kỳ ngay tại module của họ (Mục 10.5), không cần Medical Staff soạn thêm 1 bản tổng hợp rồi gửi — tránh nhân đôi dữ liệu và thêm trạng thái "đã gửi/chưa gửi" không cần thiết. **BR-214 (Vi phạm) giữ nguyên** — không có căn cứ tương tự để bỏ.

### 10.3. Compliance Dashboard — thiết kế lại theo case-hub (cùng khuôn mẫu Mục 18 `review_medicalstaff_incidentmanagement.md`)

`compliance/page.tsx` giữ vai trò **màn vận hành ATTP trong ngày**, không phải màn báo cáo theo kỳ, không lưu lịch sử. Áp đúng cấu trúc 3 vùng đã dùng cho Incident Detail (cùng triết lý, nhất quán xuyên module):

1. **Tình trạng hôm nay**: trạng thái kiểm thực 3 bước, điểm vệ sinh gần nhất, cảnh báo bước không đạt/điểm dưới ngưỡng, trạng thái côn trùng + khám SK NV bếp — giữ nguyên nội dung hiện có, không đổi field.
2. **Việc cần xử lý**: mỗi việc 1 hàng có trạng thái + CTA riêng (VD "1 phiên kiểm thực có bước không đạt — Xem phiên", "Cần kiểm tra lại vệ sinh bếp — Mở kiểm tra") — không lặp lại cùng 1 thông tin ở card/banner/bảng nếu không đổi hành động người dùng.
3. **Lịch sử và báo cáo** (thu gọn, điểm vào phụ, không cạnh tranh với việc vận hành): "Xem theo kỳ / Xuất dữ liệu" và "Soạn báo cáo tuân thủ".

### 10.4. Route restructuring

| Route cũ | Route mới | Nội dung | Ghi chú |
| --- | --- | --- | --- |
| `/medical-staff/report-center/compliance` | `/medical-staff/compliance/report` | Soạn & gửi báo cáo tuân thủ (UC-185) | Đổi route, **giữ nguyên toàn bộ field 1a-1d đã đặc tả ở Mục 8.3** — không thiết kế lại |
| `/medical-staff/reports` (nội dung cũ: Kitchen Audits + Pest Control + Fridge Temp, 3 tab) | `/medical-staff/compliance/history` | Xem theo kỳ + export — cả 3 tab dời nguyên vẹn sang đây, không tách lẻ (kể cả tab Nhiệt độ tủ — BR-110) | Không lặp dữ liệu realtime của Dashboard — mục tiêu là phân tích lịch sử |
| `/medical-staff/report-center/history` | `/medical-staff/reports` (tái dùng path đã trống sau khi nội dung cũ dời đi) | Báo cáo đã gửi (UC-190) — audit log tập trung, lọc theo loại/kỳ/người gửi/thời điểm; chỉ tra cứu, không tạo báo cáo mới | Không tạo path mới — path `/reports` trống chỗ đúng lúc |
| `/medical-staff/violations/reports` | Không đổi | Báo cáo vi phạm định kỳ (UC-189/BR-214) | Đã đúng vị trí (module Vi phạm) từ trước |

### 10.5. Ghi nhận riêng cho School Administration (CHỈ GHI NHẬN — ngoài phạm vi Medical Staff, không sửa code trong đợt này)

> Theo đúng tiền lệ đã dùng cho Service Caterers (`review_medicalstaff_to_servicecaterers.md`) — ghi nhận việc cần làm ở actor khác, không tự sửa code `school-admin/*`.

School Administration cần bổ sung **báo cáo tổng hợp Incident theo kỳ**, đặt ngay tại module Incident hiện có (`school-admin/incidents/page.tsx`), thêm nút phụ cạnh "Xuất danh sách" hiện có: "Báo cáo tổng hợp theo kỳ". Nội dung: chọn khoảng thời gian; lọc mức độ/trạng thái/đơn vị suất ăn (tận dụng filter đã có); tổng số Incident, số High, số đang mở/chờ duyệt CAPA/đã đóng; tổng học sinh ảnh hưởng; bảng phân bố theo mức độ/trạng thái; danh sách Incident cần chú ý (bấm vào mở Incident Detail); export PDF/XLSX. Không đưa nội dung điều tra chi tiết/ảnh bằng chứng/toàn văn báo cáo từng case vào bản tổng hợp này. Chưa thêm biểu đồ hay chỉ số root cause/CAPA theo taxonomy cho tới khi xác nhận dữ liệu đã chuẩn hoá đủ tin cậy để tổng hợp.

Đây là báo cáo **School Admin tự mở/tự export**, không phải văn bản Medical Staff soạn-gửi — nhất quán với quyết định Deprecated BR-213 ở Mục 10.2 (tránh nhân đôi dữ liệu + trạng thái gửi/nhận không cần thiết).

### 10.6. Nguyên tắc chung áp dụng xuyên suốt (ghi nhận làm quy ước, áp cho mọi màn Medical Staff sau này)

1. Mỗi bản ghi nghiệp vụ có 1 nguồn thao tác chính: kiểm tra bếp → Compliance/Inspection; sự cố → Incident; vi phạm → Violation; mẫu lưu → Sample Ledger.
2. Không tạo màn trung tâm chỉ để chọn loại báo cáo rồi chuyển tiếp sang module khác.
3. Báo cáo chính thức theo từng hồ sơ tạo từ chính Detail của hồ sơ đó (báo cáo sự cố từ Incident Detail, biên bản vi phạm từ Violation Detail, biên bản kiểm tra từ Inspection Detail).
4. Báo cáo tổng hợp là dữ liệu đọc/lọc/export/audit — không thay thế hồ sơ gốc, không sao chép lại nội dung hồ sơ gốc.
5. Dashboard chỉ hiển thị tình trạng/ngoại lệ/việc cần làm — không chứa form nghiệp vụ dài hay bảng dữ liệu chi tiết.
6. Mỗi màn xác định rõ 1 CTA chính theo đúng trạng thái nghiệp vụ hiện tại.

### 10.7. Checklist thay đổi cần áp dụng theo file — CHƯA áp code

| File / Component | Thay đổi | Trạng thái |
| --- | --- | --- |
| `medical-staff/report-center/page.tsx` | Xoá hẳn route + component hub | **ĐÃ ÁP CODE 26/07/2026** — vô hiệu thành stub chuyển hướng, **không xoá file** (theo quy ước đã dùng cho `incidents/investigation`) |
| `src/lib/roles.ts` | Bỏ mục nav "Trung tâm Báo cáo ATTP" | **ĐÃ ÁP CODE 26/07/2026** |
| `medical-staff/compliance/page.tsx` | Viết lại theo 3 vùng Mục 10.3; thêm 2 link phụ (Soạn báo cáo tuân thủ → `/compliance/report`; Xem theo kỳ/Xuất dữ liệu → `/compliance/history`) | **ĐÃ ÁP CODE 26/07/2026** |
| `medical-staff/dashboard/page.tsx` | Bỏ StatCard "Kiểm thực hôm nay" hardcode (dòng 57) — không thay bằng số liệu tính lại ở Dashboard, dùng shortcut tile "Bảng giám sát tuân thủ" sẵn có làm lối vào | **ĐÃ ÁP CODE 26/07/2026** |
| `medical-staff/report-center/compliance/page.tsx` → `medical-staff/compliance/report/page.tsx` | Đổi route, giữ nguyên field 1a-1d (Mục 8.3) | **ĐÃ ÁP CODE 26/07/2026** |
| `medical-staff/reports/page.tsx` → `medical-staff/compliance/history/page.tsx` | Đổi route, giữ nguyên 3 tab (Kitchen Audits/Pest Control/Fridge Temp) | **ĐÃ ÁP CODE 26/07/2026** (tab nhiệt độ đồng thời đổi nguồn theo Mục 11) |
| `medical-staff/report-center/history/page.tsx` → `medical-staff/reports/page.tsx` | Đổi route (tái dùng path trống), nội dung UC-190 giữ nguyên | **ĐÃ ÁP CODE 26/07/2026** |
| `28_SendComplianceReportDialog.md`, `29_ATTPReportCenter.md`, `32_SentReportsHistory.md`, `15_ViewFoodSafetyReportsScreen.md` | Cập nhật Screen Spec theo route mới; `29` đánh dấu Deprecated | CHƯA áp (Screen Spec) |
| `business_rules.md` BR-213 | Deprecated | **ĐÃ ÁP 26/07/2026** |
| `02_Requirements/02_UseCases` (UC-187) | Đánh dấu Deprecated trong tài liệu UC/Traceability Matrix (chưa tìm thấy file UC-187 riêng khi grep — có thể chỉ tồn tại trong business_rules.md/Screen Spec, cần đội dự án xác nhận vị trí chính xác trước khi sửa) | CHƯA áp — cần xác nhận vị trí |
| `school-admin/incidents/page.tsx` | Thêm nút "Báo cáo tổng hợp theo kỳ" theo Mục 10.5 | CHƯA áp — **ngoài phạm vi Medical Staff**, ghi nhận cho đợt review School Administration riêng |

**Git checkpoint**: cùng quy ước đã dùng ở `review_medicalstaff_incidentmanagement.md` Mục 18.7/`review_medicalstaff_foodsamples.md` Mục 11.4 — trước khi áp code, tạo tag chốt trạng thái hiện tại (VD `pre-reportcenter-removal-260726`), áp thành commit riêng trỏ về Mục 10 này, ghi tên tag/commit vào dòng "Trạng thái áp dụng" khi đánh dấu ĐÃ ÁP CODE.

**Trạng thái chung**: toàn bộ Mục 10 đã chốt qua tranh luận trực tiếp 26/07/2026 — CHƯA áp code (trừ BR-213 đã áp trực tiếp vào `business_rules.md`); phần School Administration (Mục 10.5) ngoài phạm vi, chỉ ghi nhận.

---

## 11. Vòng rà soát thứ 5 (26/07/2026) — Nguồn dữ liệu giả cho Nhiệt độ tủ (BR-110) và Kiểm soát côn trùng (BR-102)

> **TRẠNG THÁI: đã chốt qua tranh luận trực tiếp — CHƯA áp code.** Phát hiện khi kiểm tra chéo code Service Caterers xem có màn nhập liệu thật cho 2 chỉ số này hay không (theo yêu cầu người dùng).

### 11.1. Phát hiện — verify trực tiếp trên code

Grep toàn dự án cho thao tác ghi (`useCreate`) vào resource `pest-control-log` → **0 kết quả** ở mọi actor (Caterers lẫn Medical Staff). `PEST_CONTROL_LOG` chỉ là seed tĩnh trong `mock-data.ts`, mọi màn (`compliance/page.tsx`, `reports/page.tsx`, `report-center/compliance/page.tsx`, `send-compliance-report-dialog.tsx`) chỉ **đọc**.

`FRIDGE_TEMP_LOGS` (dùng ở tab "Refrigerator Temperature Logs" của `reports/page.tsx`) là mảng hardcode, **không đăng ký qua `db.ts`** (không phải resource API thật), tách biệt hoàn toàn với dữ liệu nhiệt độ THẬT đã có sẵn: `service-caterers/samples/page.tsx` (form lưu mẫu, đúng Điều 8.1 QĐ 1246) đã có field `storageTemp` (dòng 105/157/184) — Caterers nhập nhiệt độ tủ mỗi lần lưu mẫu, validate đúng dải 2–8°C, lưu vào `FoodSample.fridgeTemp`. Đây là dữ liệu thật, có người nhập, có căn cứ luật (Điều 8.1, BR-110). Tab "Refrigerator Temperature Logs" hiển thị nhiều "Refrigerator ID" như giám sát mọi tủ bếp — phạm vi này chưa từng có căn cứ BR nào (BR-089, thứ lẽ ra bao quát việc này, đã Deprecated vì "scope creep chưa qua đặc tả" — cùng loại vấn đề lặp lại ở đây).

Đối chiếu căn cứ luật: BR-110 có trích dẫn rõ (Điều 8.1 QĐ 1246/QĐ-BYT), nhưng phạm vi đúng chỉ là **tủ lưu mẫu**, không phải mọi tủ lạnh trong bếp. BR-102 (kiểm soát côn trùng) không có trích dẫn luật cụ thể trong `business_rules.md` (đã tra `QĐ 1246/QĐ-BYT` từ trước — Mục 1 file này — xác nhận văn bản này không đề cập côn trùng); đây là điều kiện thường có trong hồ sơ cấp Giấy chứng nhận ATTP thực tế nhưng chưa được dự án trích dẫn chính xác — không chặn quyết định dưới đây, chỉ ghi nhận để có thể bổ sung trích dẫn sau nếu cần.

### 11.2. Quyết định — BR-110: đổi nguồn về dữ liệu thật, thu hẹp đúng phạm vi

Đổi khối "Nhiệt độ tủ" (cả ở `compliance/page.tsx` và `compliance/history` — Mục 10.4) sang đọc từ `FoodSample.fridgeTemp` thật (tổng hợp theo lần lưu mẫu gần nhất / theo ngày), bỏ hẳn `FRIDGE_TEMP_LOGS` và filter "Refrigerator ID" nhiều tủ. Lý do: dữ liệu thật đã tồn tại sẵn (chi phí sửa chỉ là đổi nguồn tổng hợp, không cần màn nhập liệu mới), đúng phạm vi luật (Điều 8.1 chỉ nói tủ lưu mẫu), tránh lặp lại đúng vấn đề đã xử lý ở BR-089.

### 11.3. Quyết định — BR-102: giữ hiển thị, xoá seed giả, để trạng thái rỗng trung thực

Giữ nguyên khối "Kiểm soát côn trùng" ở Compliance (card + tab lịch sử) — **không xây màn nhập liệu Caterers ở đợt này**. Lý do người dùng nêu: đây là hồ sơ Caterers nộp/cập nhật định kỳ vài tháng/lần (không phải nghiệp vụ hàng ngày), Medical Staff đi kiểm tra/xác minh định kỳ chứ không phải mỗi ngày — chưa cần màn nhập liệu chi tiết ngay.

Xử lý: **xoá seed giả `PEST_CONTROL_LOG`** khỏi `mock-data.ts` (để rỗng thật) — `compliance/page.tsx` đã có sẵn đúng nhánh xử lý rỗng (`latestPest ? ... : "Chưa có bản ghi nào."`), chỉ cần bỏ dữ liệu giả là tự động hiển thị đúng trạng thái thật, không cần sửa UI. Khi có cơ chế nộp hồ sơ định kỳ thật (đợt sau, ngoài phạm vi hiện tại), mock data sẽ được thêm lại phản ánh đúng kịch bản thật.

### 11.4. Checklist thay đổi cần áp dụng theo file — CHƯA áp code

| File / Component | Thay đổi | Trạng thái |
| --- | --- | --- |
| `medical-staff/compliance/page.tsx`, `medical-staff/compliance/history/page.tsx` (Mục 10.4) | Đổi khối "Nhiệt độ tủ" sang đọc từ `FoodSample.fridgeTemp`; bỏ `FRIDGE_TEMP_LOGS`, bỏ filter "Refrigerator ID" | **ĐÃ ÁP CODE 26/07/2026** |
| `mock-data.ts` | Xoá `FRIDGE_TEMP_LOGS`, `FridgeTempLog` (không dùng nữa); xoá seed `PEST_CONTROL_LOG` (để mảng rỗng) | **ĐÃ ÁP CODE 26/07/2026** |
| `lib/api/db.ts` | Bỏ đăng ký resource không cần nữa nếu có (kiểm tra `pest-control-log` vẫn cần giữ resource, chỉ rỗng dữ liệu — không xoá resource) | **ĐÃ KIỂM TRA 26/07/2026 — không sửa**: `pest-control-log` giữ nguyên đăng ký, chỉ dữ liệu rỗng; `FRIDGE_TEMP_LOGS` vốn chưa từng đăng ký qua `db.ts` nên không có gì để gỡ |
| `15_ViewFoodSafetyReportsScreen.md` | Cập nhật mô tả tab nhiệt độ theo đúng phạm vi tủ lưu mẫu; bỏ khái niệm nhiều "Refrigerator ID" | CHƯA áp (Screen Spec) |
| `business_rules.md` BR-102 | Cân nhắc bổ sung trích dẫn luật cụ thể sau (không chặn quyết định hiện tại) | Mở, không khẩn |

**Git checkpoint**: cùng quy ước đã dùng ở Mục 10.7/`review_medicalstaff_incidentmanagement.md` Mục 18.7 — tạo tag trước khi áp code (VD `pre-fridgetemp-pestlog-fix-260726`), commit riêng trỏ về Mục 11 này. ⚠️ **CHƯA thực hiện được** — xem Mục 12.4.

**Trạng thái chung**: chốt 26/07/2026, **code ĐÃ ÁP cùng ngày** — chi tiết Mục 12.3.

---

## 12. Trạng thái áp dụng — ĐÃ ÁP CODE Mục 9 + 10 + 11 (Web, Medical Staff), 26/07/2026

Phạm vi: **chỉ code Web phía Medical Staff**. Screen Spec và code `school-admin/*` (Mục 10.5) không đụng tới, đúng tiền lệ đã dùng cho Service Caterers.

### 12.1. Mục 9 — Biên bản kiểm tra bếp chuyển sang 2 tick điện tử

| File | Thay đổi đã áp |
| --- | --- |
| `mock-data.ts` (`KitchenInspection`) | Thêm `recordConfirmedByMedicalStaffAt` / `recordConfirmedByMedicalStaffBy` / `recordConfirmedByCaterersAt`. `recordPdfExportedAt`/`recordPdfVersion` GIỮ nguyên tên, chỉ đổi vai trò từ bắt buộc → **tùy chọn**. `recordSignedFiles` đánh dấu `@deprecated` nhưng **giữ lại** để đọc biên bản cũ đã lưu theo BR-211 gốc |
| `conduct-inspection/[id]/record/page.tsx` | Khối "Bản in & bản ký" thay bằng khối **"Xác nhận biên bản"**: 2 ô xác nhận (Y tế — tự động tài khoản đăng nhập; Đại diện Nhà thầu — chọn từ `CATERER_REPS`), badge "Đã khoá — đủ 2 xác nhận" / "Còn thiếu N xác nhận". Nút "Xuất & in bản giấy (tùy chọn)" giữ lại nhưng **không còn là điều kiện** của bất kỳ bước nào. Nút "Tải ảnh/scan bản đã ký" và hàm `uploadSigned`/`removeSigned` đã bỏ; danh sách file cũ chỉ còn hiển thị chỉ đọc. Khối "Quy trình" viết lại 4 bước theo luồng mới |
| Bản in HTML (`buildInspectionRecordHtml`) | Hai ô chữ ký đổi từ "(Ký, ghi rõ họ tên)" sang in **dấu vết xác nhận điện tử** (`Đã xác nhận điện tử <thời điểm>` / `Chưa xác nhận`) — bản giấy phản ánh đúng thứ đã xảy ra trong hệ thống, không mời ký tay lần nữa |

Gate giữ nguyên: chỉ mở biên bản khi phiên `CONFIRMED`; vẫn **chỉ 2 bên**, không có "Người làm chứng".
Nút "Nhà thầu xác nhận" đặt cùng màn để chạy thử được luồng trong bản mock — thực tế Caterers tick trên tài khoản của họ (màn phía Service Caterers ghi nhận ở `review_medicalstaff_to_servicecaterers.md`, ngoài phạm vi).

### 12.2. Mục 10 — Bỏ hub, restructure route, Compliance Dashboard case-hub

| File | Thay đổi đã áp |
| --- | --- |
| `report-center/page.tsx` | Vô hiệu thành **stub `router.replace("/medical-staff/compliance")`**, giữ file kèm docblock nêu đủ lý do bỏ + 3 lối vào thay thế. Không xoá cứng (quy ước người dùng đặt ở đợt `incidents/investigation`) |
| `report-center/compliance/page.tsx` | Stub chuyển hướng → `/medical-staff/compliance/report` |
| `report-center/history/page.tsx` | Stub chuyển hướng → `/medical-staff/reports` |
| `compliance/report/page.tsx` (MỚI) | UC-185 — toàn bộ field 1–7 và 1a–1d chuyển sang nguyên vẹn, không thiết kế lại. Nút quay lại đổi thành "Về Bảng giám sát tuân thủ" + "Báo cáo đã gửi" (bỏ link tới hub đã chết) |
| `compliance/history/page.tsx` (MỚI) | UC-115/117 — 3 tab dời nguyên vẹn; đổi tiêu đề sang "Tuân thủ ATTP theo kỳ", thêm nút "Về Bảng giám sát" |
| `reports/page.tsx` | Nội dung thay hoàn toàn bằng UC-190 (lịch sử báo cáo đã gửi). Bỏ nút "Về Trung tâm Báo cáo"; thay bằng 2 lối soạn còn hiệu lực (tuân thủ / vi phạm) trỏ về đúng module. Loại `INCIDENT_SUMMARY` giữ trong nhãn + bộ lọc kèm chú thích "(đã ngừng)" để đọc báo cáo đã gửi trước khi UC-188 bị Deprecated |
| `compliance/page.tsx` | Viết lại theo 3 vùng: **Tình trạng hôm nay** (3 bước · điểm vệ sinh · nhiệt độ tủ lưu mẫu · côn trùng + khám SK) → **Việc cần xử lý** (danh sách dựng từ dữ liệu, mỗi việc 1 hàng + CTA riêng: bước không đạt · điểm <60% · nhiệt độ ngoài ngưỡng · biên bản chưa đủ tick · phiên chờ bếp xác nhận · chưa có phiên hôm nay) → **Lịch sử và báo cáo** (3 tile phụ). 4 StatCard + 2 banner + 2 bảng của bản cũ gộp lại theo đúng nguyên tắc Mục 10.3: không lặp một thông tin nếu nó không đổi hành động người dùng |
| `dashboard/page.tsx` | Bỏ StatCard "Kiểm thực hôm nay" (hardcode `Bước 2/3`); lưới còn 3 cột. KHÔNG thay bằng số tính lại — theo nguyên tắc Mục 10.6 điểm 1 |
| `roles.ts` | Nhóm "Báo cáo & Thống kê" còn 3 mục: Bảng giám sát tuân thủ · **Tuân thủ theo kỳ** (`/compliance/history`) · **Báo cáo đã gửi** (`/reports`). Bỏ "Trung tâm Báo cáo ATTP" và "Xem báo cáo ATTP" |
| `violations/reports/page.tsx` | Link "lịch sử báo cáo" đổi từ `/report-center/history` → `/reports` |

Thành phần không còn điểm gọi nào sau đợt này, **giữ nguyên trong repo** theo quy ước không xoá cứng: `send-incident-summary-dialog.tsx` (UC-188 Deprecated), `send-compliance-report-dialog.tsx` (bản dialog cũ của UC-185).

### 12.3. Mục 11 — Nguồn dữ liệu nhiệt độ tủ và nhật ký côn trùng

| File | Thay đổi đã áp |
| --- | --- |
| `mock-data.ts` | Xoá `FridgeTempLog` + `FRIDGE_TEMP_LOGS`, thay bằng khối chú thích nêu đủ 2 lý do (mảng hardcode chưa từng đăng ký resource; seed cũ chứa "Tủ đông 01 −18.5°C: PASSED" — vừa ngoài phạm vi Điều 8.1 vừa mâu thuẫn với chính ngưỡng 2–8°C của BR-110). `PEST_CONTROL_LOG` đổi thành **mảng rỗng**, giữ `PestControlLogEntry` và giữ đăng ký resource |
| `compliance/history/page.tsx` | Tab nhiệt độ đọc từ `FoodSample.fridgeTemp`: mỗi lần lưu mẫu = 1 phép đo, cột đổi thành Ngày · Giờ · Vị trí lưu · Mẫu đo kèm · Nhiệt độ · Người ghi nhận · Kết quả; ngưỡng đạt tính tại chỗ (2–8°C), thêm badge tổng "N lần đo ngoài ngưỡng". **Bỏ filter "Refrigerator ID"**. Tab côn trùng thêm trạng thái rỗng có giải thích |
| `compliance/page.tsx` | Thêm khối "Nhiệt độ tủ lưu mẫu" ở vùng 1, nguồn `FoodSample.fridgeTemp` (mẫu hôm nay, không có thì lấy mẻ gần nhất); nhiệt độ ngoài ngưỡng tự sinh 1 dòng ở vùng "Việc cần xử lý". Khối côn trùng giữ nguyên, trạng thái rỗng ghi rõ lý do chưa có dữ liệu |
| `report-center/compliance` → `compliance/report/page.tsx` | Nhánh rỗng của khối côn trùng ghi rõ báo cáo sẽ nêu "chưa có dữ liệu" thay vì im lặng bỏ trống |

**Ghi chú lệch so với văn bản Mục 11.2**: Mục 11.2 viết "đổi khối *Nhiệt độ tủ* ở `compliance/page.tsx`", nhưng khối đó đã bị GỠ khỏi màn này từ Mục 16 (12/07/2026, cùng đợt Deprecate BR-089) nên thực tế không có gì để "đổi". Đã **thêm mới** một dòng nhiệt độ gọn ở vùng 1 lấy từ nguồn thật — bám đúng ý định của Mục 11.2 (Compliance phải thấy được chỉ số này) mà không dựng lại card scope-creep cũ.

### 12.4. Việc CHƯA làm / cần lưu ý

1. **Git tag + commit riêng CHƯA tạo được** (Mục 10.7 / 11.4): môi trường shell của phiên không khởi động (`Workspace unavailable`) nên không chạy được `git tag`/`git commit`. Cần tạo thủ công tại commit **ngay trước** đợt sửa này (`pre-reportcenter-removal-260726`, `pre-fridgetemp-pestlog-fix-260726`) rồi điền tên tag/commit vào đúng dòng của bảng 10.7/11.4.
2. **`tsc --noEmit` CHƯA chạy** (cùng lý do). Đã rà thủ công: `FRIDGE_TEMP_LOGS`/`FridgeTempLog` không còn tham chiếu code nào (chỉ còn trong chú thích); `send-compliance-report-dialog.tsx` đã có sẵn nhánh `latestPest ? …` nên mảng rỗng không làm gãy; import không dùng đã gỡ (`Camera`, `X`, `mockUpload`, `UploadedFile` ở màn biên bản; `StatCard` ở Compliance; `fridge`/`fridgeIds` ở màn theo kỳ).
3. **Mục 10.5 (School Administration — báo cáo tổng hợp Incident theo kỳ) KHÔNG áp**, đúng phạm vi đã ghi: chờ đợt review riêng cho actor đó.
4. **UC-187 trong tài liệu UC/Traceability Matrix chưa đánh Deprecated** — bảng 10.7 đã ghi cần xác nhận vị trí file trước khi sửa; chưa tìm thấy file UC-187 riêng.
5. **Phụ thuộc chéo còn hở**: `review_medicalstaff_violations.md` Mục 9.2 (2-tick cho Biên bản Vi phạm) chưa áp — xem cảnh báo ở cuối Mục 9.
6. **Screen Spec chưa cập nhật**: `12_ConductFoodSafetyInspectionScreen.md`, `15_ViewFoodSafetyReportsScreen.md`, `28_SendComplianceReportDialog.md`, `29_ATTPReportCenter.md` (cần đánh Deprecated), `32_SentReportsHistory.md`.

---

## 13. Vòng rà soát thứ 6 (27/07/2026) — Rà soát 2 màn đã áp code ("Tuân thủ ATTP theo kỳ", "Báo cáo đã gửi"); tách bạch UC-115/117 khỏi UC-185

> **TRẠNG THÁI: đã chốt qua tranh luận trực tiếp (bao gồm 1 vòng `AskUserQuestion`) — CHƯA áp code, đúng ràng buộc phiên làm việc này ("chỉ sửa file review").**

**Nguồn**: 2 ảnh chụp code thật do người dùng cung cấp (`/medical-staff/compliance/history`, `/medical-staff/reports` — cả hai đã áp code từ Mục 12.2) + đọc trực tiếp `compliance/history/page.tsx`, `reports/page.tsx`, `conduct-inspection/history/page.tsx`, `compliance/page.tsx`, `compliance/report/page.tsx`, `violations/reports/page.tsx`, `mock-data.ts` (`SentReport`, `KITCHEN_INSPECTIONS`, `FOOD_SAMPLES`); đối chiếu SRS gốc `02_Requirements/02_UseCases/MF07_Food_Safety_Inspection_Monitoring.md` (UC-115/UC-117) và `business_rules.md` (BR-177/178/208/209).

### 13.1. Phát hiện — trùng lặp dữ liệu thật trên "Tuân thủ ATTP theo kỳ" (`compliance/history`)

- Tab "Kiểm tra vệ sinh bếp": đọc cùng resource `kitchen-inspections` mà `conduct-inspection/history/page.tsx` đã hiển thị đầy đủ kèm thao tác (Chi tiết, Lập biên bản). Tab này chỉ đổi vài cột, không có bước tổng hợp nào — mâu thuẫn với chính comment đầu file (dòng 8-12) tự nhận vai trò "phân tích lịch sử", và với nguyên tắc Mục 10.6 điểm 4 ("báo cáo tổng hợp là dữ liệu đọc/lọc/export, không sao chép lại hồ sơ gốc").
- Tab "Nhiệt độ tủ lưu mẫu": nguồn dữ liệu đã đúng (Mục 11, `FoodSample.fridgeTemp` thật), nhưng bảng vẫn liệt kê lại từng dòng y hệt cột "Tủ lưu (BR-110)" đã có sẵn ở Sổ lưu mẫu (`samples/page.tsx`).
- Tab "Kiểm soát côn trùng": chưa có nguồn nhập liệu thật (Mục 11.3) — dựng khung bảng 5 cột chờ dữ liệu là thừa so với nhu cầu hiện tại.
- KPI đầu trang (`summary?.modules`, đọc từ endpoint mock `/compliance-report`): không liên kết với bộ lọc khoảng-ngày cùng trang, không có thao tác bấm vào — nguồn dữ liệu tách biệt hoàn toàn với 3 tab bên dưới, có nguy cơ lệch phạm vi thời gian giữa số ở trên và bảng ở dưới (lỗi nhất quán dữ liệu, không chỉ là vấn đề trình bày).

### 13.2. Phát hiện — 2 nút "Soạn báo cáo" và nút "Tải lại PDF" trên "Báo cáo đã gửi" (`reports/page.tsx`)

- Header màn tự nhận vai trò "audit log tập trung, chỉ tra cứu — không tạo báo cáo mới ở đây" (dòng 13 file), nhưng đặt ngay 2 nút "Soạn báo cáo tuân thủ"/"Soạn báo cáo vi phạm" — tự mâu thuẫn với vai trò vừa tuyên bố. Compliance Dashboard (Vùng 3) và Violations đã có lối vào soạn báo cáo riêng, không cần lặp lại ở màn tra cứu.
- Nút "Tải lại PDF": hành vi phía sau (tải đúng file PDF đã sinh tại thời điểm gửi, không tính lại số liệu — đúng BR-178) có ý nghĩa thật, nhưng nhãn không truyền tải được ý đó, đọc như một thao tác retry vô nghĩa.

### 13.3. Quyết định — cấu trúc lại theo document-centric, tách bạch UC-115/117 khỏi UC-185

Xác nhận qua thảo luận trực tiếp 27/07/2026:

1. **Bỏ "Tuân thủ ATTP theo kỳ" (`compliance/history`) như một màn 3-tab độc lập.** Không tách lẻ đưa từng phần đi nơi khác — gộp thẳng vào Compliance Dashboard dưới dạng khối **"Lập báo cáo theo kỳ"** (khác khối "Hôm nay" đã có — Mục 10.3 Vùng 1 — không trộn 2 phạm vi thời gian vào cùng 1 khối):
   - "Lập báo cáo theo kỳ" **chính là UC-185 đã có sẵn tại `compliance/report/page.tsx`** (Mục 8.3/12.2) — không dựng màn mới, chỉ đưa route này làm lối vào chính của "xem/soạn theo kỳ" từ Compliance Dashboard.
   - Trong "Lập báo cáo theo kỳ", không hiển thị lại bảng đầy đủ dữ liệu nguồn — chỉ hiện dòng dạng "Kiểm tra vệ sinh bếp: N phiên · M phiên không đạt → Xem phiên" (link ra `conduct-inspection/history`), "Kiểm thực ba bước: X/Y bữa hoàn tất · Z bữa có bước không đạt → Xem hồ sơ" (link ra `inspections`), "Kiểm soát côn trùng: chưa được chốt trong báo cáo kỳ" khi rỗng.

2. **`compliance/report/page.tsx` (UC-185, đã áp code Mục 12.2) cần sửa lại field 1b** — hiện đang liệt kê TOÀN BỘ `sessionsInPeriod` kể cả các bữa Đạt, vi phạm đúng nguyên tắc "chỉ giữ số liệu nêu được hành động/rủi ro" vừa chốt. Đổi bảng đầy đủ thành 1 dòng tổng hợp + link, giữ nguyên phần "Phiên kiểm tra không đạt trong kỳ" (`failedRows`, vốn đã là exception-only). Câu côn trùng khi rỗng đổi thành "chưa được chốt trong báo cáo kỳ" thay vì cách diễn đạt hiện tại ("chưa có bản ghi nào — báo cáo sẽ ghi...").

3. **Giữ lại UC-115/117 dưới dạng tối giản — 1 nút "Xuất dữ liệu tuân thủ"**, không phải màn/tab riêng. Lý do không xoá hẳn: SRS gốc (`MF07_Food_Safety_Inspection_Monitoring.md`) định nghĩa UC-115 có AF1 "Export for Health Authority" (xuất cho cơ quan y tế, không phải gửi School Admin) và UC-117 là tra cứu/xuất dữ liệu chung, không có bước "gửi" hay người nhận nào — mục đích khác hẳn UC-185 (soạn & gửi văn bản chính thức có người nhận cụ thể). Xoá hẳn đồng nghĩa cắt phạm vi so với SRS, không chỉ là gộp UI cho gọn — nên không tự quyết, đã xác nhận với người dùng qua thảo luận trực tiếp.
   - Đặt nút này tại Vùng 3 "Lịch sử và báo cáo" của Compliance Dashboard — chọn khoảng ngày → xuất, không bảng, không tab, không route/menu riêng.
   - **Nội dung xuất là dữ liệu ĐẦY ĐỦ (không lọc theo ngoại lệ)** — khác nguyên tắc exception-only vừa chốt cho UC-185 — vì mục đích của UC-115/117 là cung cấp hồ sơ đầy đủ cho audit/cơ quan bên ngoài, không phải văn bản tóm tắt gửi School Admin. Đây KHÔNG phải là mâu thuẫn với nguyên tắc "không lặp dữ liệu nguồn": nội dung đầy đủ nằm trong 1 file tải về, không phải 1 bảng lặp lại trên màn hình — không cạnh tranh không gian UI với các màn vận hành.
   - **Định dạng xuất: CSV/Excel** (không PDF) — khớp đúng UC-117 gốc (Excel) hơn PDF, phù hợp mục đích "dữ liệu thô cho audit" hơn là văn bản trình bày.
   - Đã dựng 4 file minh hoạ cấu trúc cột thật, dữ liệu lấy trực tiếp từ mock thật (`KITCHEN_INSPECTIONS`, `FOOD_SAMPLES`), không bịa số liệu — xem Mục 13.5.
   - **Lỗi đã phát hiện và sửa khi dựng file minh hoạ**: cột "Loại" (Định kỳ/Đột xuất) ban đầu có trong bảng Kiểm tra vệ sinh bếp — sai, vì BR-208 đã Deprecated 09/07/2026 (Mục 3), cột này không còn tồn tại ở bất kỳ màn nào khác trong hệ thống từ đợt đó. Đã bỏ cột này khỏi file minh hoạ; cần áp dụng đúng khi code thật (không đưa `type`/`inspectionType` vào cột xuất).

4. **`reports/page.tsx` (Báo cáo đã gửi, UC-190)**:
   - Bỏ hẳn 2 nút "Soạn báo cáo tuân thủ"/"Soạn báo cáo vi phạm" khỏi header — đúng vai trò tra cứu/audit thuần tuý đã tự khai trong chính comment đầu file. Không thay bằng nút khác.
   - Đổi nút "Tải lại PDF" → **"Xem báo cáo"**, mở route/dialog Detail chỉ đọc hiển thị đúng nội dung đã chốt tại thời điểm gửi (phạm vi dữ liệu, kết quả tổng hợp, các ngoại lệ đã nêu, ghi chú/xác nhận khi gửi), có nút "In / Xuất PDF" bên trong Detail — ở đây mới có ý nghĩa vì người dùng đang xem 1 tài liệu cụ thể đã chốt, đúng tinh thần BR-178 (không tính lại số liệu, chỉ hiển thị đúng snapshot đã lưu tại thời điểm gửi).

5. **Yêu cầu kỹ thuật bắt buộc phải làm TRƯỚC khi dựng Detail view** (Mục 13.3 điểm 4): kiểu `SentReport` trong `mock-data.ts` (dòng 2285-2295) hiện chỉ khai báo 9 field chung (`id/reportType/period/rangeLabel/date/sentAt/sentBy/recipient/notes?`), trong khi `compliance/report/page.tsx` và `violations/reports/page.tsx` khi gửi thực tế đã đính kèm nhiều field khác nhau theo từng loại báo cáo (COMPLIANCE: `avgScore`, `failedSessions`, `threeStepSummary`, `assessment`...; VIOLATION_SUMMARY: `counts`, `violationIds`, `summary`...) — các field này chỉ tồn tại nhờ payload gửi lỏng kiểu `Record<string, unknown>`, không được khai báo trong type. Cần chính thức hoá `SentReport` thành **discriminated union theo `reportType`** trước khi dựng Detail view đọc lại đúng nội dung đã gửi.

### 13.4. Checklist thay đổi cần áp dụng theo file — CHƯA áp code

| File / Component | Thay đổi | Trạng thái |
| --- | --- | --- |
| `medical-staff/compliance/history/page.tsx` | Bỏ hẳn route/màn 3-tab; nội dung "xem theo kỳ" dời vào khối mới trong `compliance/page.tsx` | **ĐÃ ÁP CODE 27/07/2026** — vô hiệu thành stub chuyển hướng, không xoá file |
| `medical-staff/compliance/page.tsx` | Thêm khối "Lập báo cáo theo kỳ" ở Vùng 3, dẫn vào `compliance/report/page.tsx` làm lối vào chính; thêm nút "Xuất dữ liệu tuân thủ" (chọn khoảng ngày → CSV/Excel, dữ liệu đầy đủ, không lọc ngoại lệ) | **ĐÃ ÁP CODE 27/07/2026** |
| `medical-staff/compliance/report/page.tsx` | Sửa field 1b: bỏ bảng đầy đủ `sessionsInPeriod`, thay bằng 1 dòng tổng hợp + link `inspections`; câu côn trùng rỗng đổi thành "chưa được chốt trong báo cáo kỳ"; giữ nguyên `failedRows` (đã exception-only) | **ĐÃ ÁP CODE 27/07/2026** |
| `medical-staff/reports/page.tsx` | Bỏ 2 nút "Soạn báo cáo..." khỏi header; đổi nút "Tải lại PDF" → "Xem báo cáo" mở Detail chỉ đọc | **ĐÃ ÁP CODE 27/07/2026** |
| `medical-staff/reports/[id]/page.tsx` (MỚI, hoặc Dialog) | Detail chỉ đọc cho từng `SentReport`: phạm vi dữ liệu, kết quả tổng hợp, ngoại lệ, ghi chú/xác nhận khi gửi, nút "In / Xuất PDF" | **ĐÃ ÁP CODE 27/07/2026** — chọn route riêng (không Dialog) |
| `mock-data.ts` (`SentReport`) | Chính thức hoá thành discriminated union theo `reportType` (COMPLIANCE/VIOLATION_SUMMARY/INCIDENT_SUMMARY mỗi loại 1 shape), khớp đúng field đã gửi thật từ `compliance/report`/`violations/reports` | **ĐÃ ÁP CODE 27/07/2026** |
| `roles.ts` | Bỏ mục nav "Tuân thủ theo kỳ" (`/compliance/history`) đã thêm ở Mục 12.2 — không còn màn riêng để trỏ tới; nhóm "Báo cáo & Thống kê" còn lại 2 mục (Bảng giám sát tuân thủ · Báo cáo đã gửi) | **ĐÃ ÁP CODE 27/07/2026** |
| `15_ViewFoodSafetyReportsScreen.md` | Cập nhật mô tả: UC-115/117 thu gọn thành 1 nút xuất dữ liệu tại Compliance Dashboard, không còn màn/tab riêng; giữ nguyên AF1 Export for Health Authority | CHƯA áp (Screen Spec) |
| `28_SendComplianceReportDialog.md` | Cập nhật field 1b theo hướng exception-only (Mục 13.3 điểm 2) | CHƯA áp (Screen Spec) |
| `32_SentReportsHistory.md` | Cập nhật: bỏ 2 nút soạn báo cáo; "Tải lại PDF" → "Xem báo cáo" + Detail view mới | CHƯA áp (Screen Spec) |
| `business_rules.md` BR-208 | Không đổi nội dung — chỉ nhắc lại khi áp code xuất dữ liệu: không đưa cột "Loại kiểm tra" vào bất kỳ export nào | Đã đúng, chỉ cần không tái phạm khi code |

**Git checkpoint**: cùng quy ước Mục 10.7/11.4/`review_medicalstaff_incidentmanagement.md` Mục 18.7 — tạo tag trước khi áp code (VD `pre-compliancehistory-removal-270726`), áp thành commit riêng trỏ về Mục 13 này, điền tên tag/commit vào đúng dòng bảng trên khi đánh dấu ĐÃ ÁP CODE. ⚠️ **CHƯA thực hiện được** — xem Mục 14.3.

**Trạng thái chung**: chốt qua tranh luận trực tiếp 27/07/2026 (bao gồm xác nhận qua `AskUserQuestion` cho điểm 3, và xác nhận trực tiếp bằng lời cho các điểm còn lại) — **code Medical Staff ĐÃ ÁP 27/07/2026**, chi tiết Mục 14. Screen Spec vẫn CHƯA áp.

### 13.5. File minh hoạ đã tạo (không phải code, chỉ để duyệt hình dạng dữ liệu xuất trước khi lập trình)

`01_tong_quan.csv`, `02_kiem_tra_ve_sinh_bep.csv`, `03_nhiet_do_tu_luu_mau.csv`, `04_kiem_soat_con_trung.csv` — dựng từ dữ liệu mock thật (`KITCHEN_INSPECTIONS` 4 bản ghi, `FOOD_SAMPLES` 11 bản ghi có `fridgeTemp`, `PEST_CONTROL_LOG` rỗng), khoảng thời gian minh hoạ 12/05/2026–06/07/2026. Đã sửa bỏ cột "Loại" sau khi phát hiện BR-208 đã Deprecated (Mục 3) — xem Mục 13.3 điểm 3.

---

## 14. Vòng rà soát thứ 7 (27/07/2026) — Viết lại Compliance Dashboard: từ "case-hub điều hướng" sang "dashboard thống kê/giám sát" — **THAY THẾ Mục 10.3**

> **TRẠNG THÁI: đã chốt qua tranh luận trực tiếp nhiều vòng, xác nhận cuối cùng 27/07/2026 — CHƯA áp code.** Mục này thay thế hoàn toàn cấu trúc 3-vùng của Mục 10.3 (không phải bổ sung song song) — nếu áp code, đọc Mục 14 làm chuẩn, Mục 10.3 chỉ còn giá trị lịch sử.

**Nguồn**: ảnh chụp code thật `compliance/page.tsx` sau khi áp Mục 10/11/12 (đã có "Việc cần xử lý" dạng danh sách tác vụ) + thảo luận trực tiếp nhiều vòng, trong đó người dùng tự phát hiện và tự sửa 2 lỗi trùng lối điều hướng trước khi tôi kịp rà hết, sau đó tự đề xuất toàn bộ wireframe thay thế.

### 14.1. Đính chính vai trò màn hình — lý do thay thế Mục 10.3

Mục 10.3 (Vòng rà soát thứ 4) áp nguyên khuôn case-hub 3 vùng ("Tình trạng hôm nay" / "Việc cần xử lý" / "Lịch sử và báo cáo") giống hệt Incident Detail (`review_medicalstaff_incidentmanagement.md` Mục 18). Rà lại theo phản biện của người dùng phát hiện khuôn này **không hợp** với đúng bản chất 2 màn:

- **Incident Detail** quản lý **một ca cụ thể đang chạy qua state machine** (SEVERITY_PENDING → ... → CLOSED) — mỗi trạng thái cần đúng 1 CTA đưa ca tiến lên bước kế tiếp. Task-routing là bản chất của màn.
- **Compliance Dashboard** không quản lý "một ca" nào — nó tổng hợp nhiều luồng vận hành độc lập (kiểm tra bếp, kiểm thực, lưu mẫu, côn trùng) đang chạy song song ở các module nguồn riêng. Nhúng "Việc cần xử lý" dạng điều hướng vào đây khiến màn biến thành "bảng lối tắt" hơn là dashboard — và vì mỗi lối tắt gần như luôn trùng đích với 1 card trạng thái đã có sẵn ở trên, tạo ra tình trạng 2 con đường tới cùng 1 nơi mà người dùng đã tự phát hiện 2 lần liên tiếp trong quá trình rà soát này (nút "Kiểm tra an toàn bếp" ở header trùng "Lịch sử kiểm tra bếp"; nút "Xem phiên" trong "Việc cần xử lý" trùng "Hồ sơ kiểm thực →" của card kiểm thực).

**Quyết định**: Compliance Dashboard đổi hẳn vai trò thành **dashboard thống kê/giám sát + điểm khởi đầu tạo báo cáo tổng hợp** — không điều hướng để thực hiện thao tác nghiệp vụ (kiểm tra/kiểm thực/lưu mẫu vẫn thực hiện tại đúng module nguồn của nó). Bốn câu hỏi màn này phải trả lời được ngay khi mở lên: (1) hôm nay các đầu việc ATTP đã hoàn thành đến đâu, (2) chỉ số ATTP trong kỳ đang tốt hay xấu, (3) có ngoại lệ nào cần chú ý, (4) có cần tạo/gửi báo cáo tổng hợp không.

### 14.2. Wireframe đã chốt

```text
BẢNG GIÁM SÁT TUÂN THỦ ATTP
02/07/2026                                    [Tạo báo cáo tổng hợp — BỎ, xem 14.3 điểm 1]
┌──────────────────────────────────────────────────────────────┐
│ TÌNH TRẠNG CÔNG VIỆC HÔM NAY                                 │
│                                                              │
│  Kiểm tra bếp       Hồ sơ kiểm thực       Lưu mẫu            │
│  Chưa thực hiện     2/3 bước hoàn thành   8/8 hợp lệ          │
│  Phiên gần nhất     Bước 3 chờ xử lý      Không có cảnh báo   │
│  24/06 · 92%                                                   │
│                                                              │
│  Hoàn thành: 2/3 đầu mục       Cần chú ý: 1                 │
└──────────────────────────────────────────────────────────────┘
                     [Hôm nay] [7 ngày] [30 ngày] [Tùy chọn kỳ]  ← chỉ áp cho 2 khối dưới + Ngoại lệ (14.3 điểm 2)
┌─────────────────────────────┐  ┌────────────────────────────┐
│ XU HƯỚNG ĐIỂM VỆ SINH BẾP   │  │ KIỂM THỰC 3 BƯỚC · KỲ ĐÃ CHỌN│
│  biểu đồ CSS thuần (14.3 đ4) │  │  Đạt / Đang thực hiện /    │
│  theo các phiên kiểm tra      │  │  Không đạt — biểu đồ cột   │
└─────────────────────────────┘  └────────────────────────────┘
┌──────────────────────────────────────────────────────────────┐
│ NGOẠI LỆ CẦN CHÚ Ý (theo kỳ đã chọn)                          │
│ ! Bước kiểm thực trước phục vụ chưa hoàn thành               │
│ ! 2 phiên kiểm thực có bước không đạt trong kỳ               │
│ ! Kiểm tra bếp hôm nay chưa có bản ghi                       │
│ (Nhiệt độ tủ lưu mẫu CHỈ xuất hiện ở đây khi ngoài ngưỡng —  │
│  bình thường thì chỉ là 1 dòng gọn trong "Lưu mẫu" ở trên)   │
└──────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────┐
│ BÁO CÁO TỔNG HỢP — lối vào duy nhất để soạn báo cáo (14.3 đ1) │
│ Kỳ báo cáo: [Tuần này ▼]                                     │
│ Báo cáo lấy snapshot từ dữ liệu đang hiển thị.                │
│                  [Xem trước báo cáo] [Chốt & gửi BGH]         │
│         → điều hướng sang compliance/report, kỳ pre-fill      │
│           (14.3 điểm 3, KHÔNG soạn inline tại dashboard)      │
│ Báo cáo đã gửi gần nhất: 12/07/2026        [Xem lịch sử]      │
└──────────────────────────────────────────────────────────────┘
```

### 14.3. Bốn điểm mở đã xác nhận (27/07/2026)

1. **Bỏ nút "Tạo báo cáo tổng hợp" ở header** — giữ duy nhất khối "BÁO CÁO TỔNG HỢP" cuối trang làm lối vào soạn báo cáo, tránh lặp lại đúng loại lỗi 2-lối-1-đích mà người dùng vừa tự sửa ở phần điều hướng kiểm tra.
2. **Bộ lọc kỳ (Hôm nay/7 ngày/30 ngày/Tùy chọn) chỉ áp cho khối biểu đồ xu hướng + khối "Ngoại lệ cần chú ý"** — đặt lại vị trí ngay trên 2 khối đó, không đặt chung ở đầu trang. Khối "Tình trạng công việc hôm nay" luôn cố định là ngày hiện tại, không đổi theo bộ lọc kỳ.
3. **"Xem trước báo cáo" / "Chốt & gửi BGH" điều hướng sang `compliance/report/page.tsx` (đã có sẵn field 1-7 + 1a-1d, Mục 8.3/12.2)**, mang theo kỳ đã chọn — không soạn inline tại dashboard (giữ đúng nguyên tắc Mục 10.6 điểm 5 "Dashboard không chứa form nghiệp vụ dài"). **"Xem trước báo cáo" dùng chung 1 component hiển thị tài liệu chỉ-đọc với "Xem báo cáo" của màn Báo cáo đã gửi (Mục 13.3 điểm 4/13.4)** — cùng 1 khuôn render tài liệu (phạm vi dữ liệu, kết quả tổng hợp, ngoại lệ, ghi chú), chỉ khác nguồn dữ liệu là bản nháp đang soạn hay bản `SentReport` đã gửi. Không dựng 2 component hiển thị tài liệu khác nhau cho 2 trạng thái của cùng 1 loại nội dung.
4. **Biểu đồ dùng CSS thuần, không cài thư viện biểu đồ mới** — đúng tiền lệ duy nhất đã có trong dự án (`system-admin/usage-statistics/page.tsx`, tự ghi chú "Chart thuần CSS vì dự án chưa cài recharts"). Grep toàn `package.json` xác nhận chưa có `recharts`/`chart.js` hay thư viện biểu đồ nào. Việc cài thư viện biểu đồ cho toàn dự án là quyết định riêng, ảnh hưởng nhiều actor khác — không tự quyết ngầm trong phạm vi màn này.

### 14.4. Nguyên tắc dữ liệu (ghi nhận làm quy ước, áp cho Compliance Dashboard và mọi dashboard tương tự sau này)

1. Không hiển thị bảng danh sách chi tiết (phiên kiểm tra, bản ghi mẫu…) ngay trên dashboard — đây là nguyên tắc đã có ở Mục 10.6 điểm 4, nhắc lại rõ ràng hơn: **kể cả dạng rút gọn cột cũng không**, chỉ số + drill-down link.
2. **Card "bình thường" không chiếm không gian cố định** — mở rộng nguyên tắc Mục 10.6 điểm 4: "Nhiệt độ tủ lưu mẫu" khi trong ngưỡng chỉ là 1 dòng gọn trong khối "Lưu mẫu" của "Tình trạng hôm nay"; chỉ khi ngoài ngưỡng mới nổi thành 1 dòng ở "Ngoại lệ cần chú ý" với chi tiết đầy đủ. Đây là hình thức "progressive disclosure" — trạng thái tốt không cần diện tích màn hình, chỉ ngoại lệ mới cần.
3. Kiểm soát côn trùng (BR-102): chưa chốt quy tắc dữ liệu/chỉ số thì **không hiển thị phần trăm, không đưa vào bất kỳ phép tính tổng điểm/tỷ lệ hoàn thành nào** — đúng tinh thần Mục 11.3 (trạng thái rỗng trung thực), siết chặt thêm ở khía cạnh tính toán.
4. Khám sức khỏe nhân viên bếp là **chỉ số định kỳ**, không phải đầu việc hằng ngày — không tính vào "Hoàn thành X/Y đầu mục hôm nay"; chỉ hiện cảnh báo khi quá hạn/không hợp lệ.
5. **Không suy ra "chưa kiểm tra hôm nay" từ một phiên gần nhất ở ngày khác** — 2 sự kiện khác nhau, phải hiển thị tách bạch (VD "Chưa thực hiện [hôm nay]" + "Phiên gần nhất 24/06 · 92%" là 2 dòng riêng, không gộp thành 1 dòng "92% (24/06)" gây hiểu lầm là kết quả hôm nay). Đối chiếu code hiện tại (`compliance/page.tsx`, biến `today`/`latestKitchen` tách riêng): **đã đúng nguyên tắc này từ trước**, chỉ cần giữ nguyên cách tách khi viết lại UI, không phải sửa lỗi mới.
6. Mọi số hiển thị phải trả lời được 1 câu hỏi cụ thể (không dùng % tổng hợp kiểu "58%" nếu không giải thích được mẫu số/dữ liệu nào bị tính/hành động tiếp theo là gì) — bảng đối chiếu:

| Chỉ số | Câu hỏi trả lời |
| --- | --- |
| Hoàn thành X/Y đầu mục hôm nay | Còn thiếu công việc vận hành nào hôm nay |
| X/Y bước kiểm thực | Bữa ăn hiện đang ở đâu trong quy trình |
| Tỷ lệ Đạt/Không đạt theo kỳ | Có xu hướng rủi ro hay không |
| Điểm vệ sinh theo các phiên (biểu đồ) | Chất lượng bếp đang cải thiện hay giảm |
| Số ngoại lệ mở | Có bao nhiêu vấn đề cần theo dõi |

Đây chính là điểm khép lại phát hiện KPI đầu trang ở Mục 13.1 (số liệu `summary?.modules` từ endpoint mock `/compliance-report` không gắn với câu hỏi cụ thể, không đồng bộ bộ lọc) — endpoint mock này **bị loại bỏ hoàn toàn** khỏi thiết kế mới, không sửa lại vì không còn chỗ dùng.

### 14.5. Luồng tạo báo cáo (tổng hợp lại, khớp Mục 13.3)

```text
Dashboard chọn kỳ (khối "Báo cáo tổng hợp")
  → "Xem trước báo cáo" (component chỉ-đọc dùng chung với "Xem báo cáo" của UC-190)
  → Medical Staff kiểm tra nội dung snapshot
  → "Chốt & gửi BGH" tại compliance/report/page.tsx (field 1-7/1a-1d giữ nguyên)
  → Báo cáo bất biến, xem lại tại "Báo cáo đã gửi" (UC-190, Mục 13)
```

### 14.6. Checklist thay đổi cần áp dụng theo file — CHƯA áp code (bổ sung/thay thế Mục 10.7, 13.4)

| File / Component | Thay đổi | Trạng thái |
| --- | --- | --- |
| `medical-staff/compliance/page.tsx` | Viết lại toàn bộ theo wireframe Mục 14.2: bỏ "Việc cần xử lý" dạng điều hướng; 3 khối "Tình trạng hôm nay" (Kiểm tra bếp/Hồ sơ kiểm thực/Lưu mẫu) chỉ hiện chỉ số + không CTA điều hướng thao tác; thêm bộ lọc kỳ riêng cho khối biểu đồ + ngoại lệ; bỏ hẳn `summary?.modules`/endpoint `/compliance-report` cũ | **ĐÃ ÁP CODE 27/07/2026** — **THAY THẾ** dòng tương ứng ở Mục 10.7 |
| `medical-staff/compliance/page.tsx` (biểu đồ) | Thêm 2 khối biểu đồ CSS thuần: xu hướng điểm vệ sinh bếp theo phiên; phân bố Đạt/Đang thực hiện/Không đạt kiểm thực 3 bước theo kỳ | **ĐÃ ÁP CODE 27/07/2026** |
| `medical-staff/compliance/page.tsx` (khối Báo cáo tổng hợp) | Thêm khối kỳ + "Xem trước báo cáo"/"Chốt & gửi BGH" (điều hướng `compliance/report`, kỳ pre-fill) + dòng trạng thái báo cáo gần nhất + link "Xem lịch sử" (`/reports`) | **ĐÃ ÁP CODE 27/07/2026** |
| Component Detail tài liệu chỉ-đọc (dùng chung) | Dùng 1 component cho cả "Xem trước báo cáo" (dữ liệu nháp) và "Xem báo cáo" của UC-190 (dữ liệu `SentReport` đã gửi) — phụ thuộc `SentReport` discriminated union đã yêu cầu ở Mục 13.3 điểm 5 | **ĐÃ ÁP CODE 27/07/2026** — `src/components/app/report-document.tsx` |
| `mock-data.ts` | Bỏ hẳn `ComplianceSummary`/endpoint `/compliance-report` (nếu có đăng ký mock-server) — không còn nơi dùng sau khi bỏ KPI cards cũ | **KHÔNG áp — có lý do**: `school-admin/reports/page.tsx` vẫn đang gọi endpoint này (grep xác nhận). Đã gỡ hết điểm dùng phía Medical Staff; xoá endpoint sẽ làm gãy màn của actor khác, nằm ngoài phạm vi đợt này. Xem Mục 16.3 điểm 3 |
| `15_ViewFoodSafetyReportsScreen.md`, `23_ComplianceDashboard.md` | Cập nhật mô tả theo cấu trúc mới Mục 14.2; ghi rõ Mục 14 thay thế Mục 10.3 | CHƯA áp (Screen Spec) |

**Git checkpoint**: cùng quy ước Mục 10.7/11.4/13.4 — tạo tag trước khi áp code (VD `pre-compliance-dashboard-rewrite-270726`), commit riêng trỏ về Mục 14. ⚠️ **CHƯA thực hiện được** — xem Mục 16.3.

**Trạng thái chung**: chốt qua tranh luận trực tiếp nhiều vòng 27/07/2026 (người dùng tự phát hiện 2 lỗi trùng lối điều hướng, tự đề xuất wireframe thay thế, 4 điểm mở đã xác nhận trực tiếp) — **code Medical Staff ĐÃ ÁP 27/07/2026**, chi tiết Mục 16. **Mục 14 thay thế Mục 10.3**; Mục 10.1/10.2/10.4/10.5/10.6 (bối cảnh, bỏ hub, route restructuring, ghi nhận School Admin, nguyên tắc chung) vẫn còn hiệu lực, không đổi.

---

## 15. Trạng thái áp dụng — ĐÃ ÁP CODE Mục 13 (Web, Medical Staff), 27/07/2026

> *Số hiệu mục đổi từ 14 → 15 ngày 27/07/2026: Vòng rà soát thứ 7 được chèn vào giữa và cũng đánh số 14, gây trùng. Nội dung không đổi.*

### 15.1. Thay đổi theo file

| File | Thay đổi đã áp |
| --- | --- |
| `mock-data.ts` (`SentReport`) | Tách thành **discriminated union** `ComplianceSentReport \| ViolationSentReport \| IncidentSummarySentReport` trên nền `SentReportBase` (9 field chung). Khai đúng field mà 2 màn soạn đang gửi thật: COMPLIANCE có `avgScore`/`failedSessions`/`threeStepSummary`/`threeStepRows`/`latestPestControl`/`staffHealthValid`/`assessment`; VIOLATION_SUMMARY có `summary`/`staffInvolved`/`counts`/`violationIds`; INCIDENT_SUMMARY đánh `@deprecated` nhưng giữ nhánh để đọc dữ liệu cũ. Thêm 2 kiểu phụ `SentReportThreeStepRow`/`SentReportFailedSession`. Seed `RPTS-2026-001` bổ sung snapshot thật (`avgScore: 90`, `threeStepSummary 5/5`, `failedSessions: []`) để Detail view có gì để hiển thị. **Làm TRƯỚC** phần Detail view đúng yêu cầu Mục 13.3 điểm 5 |
| `reports/[id]/page.tsx` (MỚI) | Detail chỉ đọc theo `reportType`: khối "Phạm vi dữ liệu" chung → kết quả tổng hợp (điểm TB · kiểm thực · khám SK · côn trùng) → "Phiên kiểm tra không đạt đã nêu" → nhận định/ghi chú. Banner đầu trang nói thẳng: số liệu là ảnh chụp lúc gửi và **không được tính lại** (BR-178). Nút **"In / Xuất PDF"** đặt tại đây, dựng bản in từ chính snapshot (`buildReportHtml`), không đọc lại resource nguồn nào. Bảng `threeStepRows` chỉ render khi báo cáo CŨ có đính kèm — có chú thích rõ đây là đường đọc dữ liệu cũ, không phải tính năng đang dùng |
| `reports/page.tsx` | Bỏ 2 nút "Soạn báo cáo tuân thủ"/"Soạn báo cáo vi phạm" khỏi header (không thay bằng nút khác). Cột cuối đổi `Tải lại PDF` → **"Xem báo cáo"** dẫn sang `/medical-staff/reports/[id]` |
| `compliance/history/page.tsx` | Vô hiệu thành **stub `router.replace("/medical-staff/compliance")`**, docblock ghi đủ 4 lý do bỏ (3 tab chép lại dữ liệu nguồn + KPI lệch bộ lọc ngày). Không xoá file |
| `compliance/page.tsx` | Vùng 3 đổi tên thành **"Lập báo cáo theo kỳ"**, gồm: tile chính "Soạn & gửi báo cáo tuân thủ" → `/compliance/report`, tile "Lịch sử kiểm tra bếp", và khối **"Xuất dữ liệu tuân thủ"** (2 ô ngày + nút Xuất CSV). Bỏ tile trỏ `/compliance/history`. Dòng việc "Nhiệt độ tủ ngoài ngưỡng" ở Vùng 2 đổi đích sang `/medical-staff/samples` (cột "Tủ lưu (BR-110)" đã có sẵn ở đó — Mục 13.1) |
| `compliance/page.tsx` (export) | Hàm `exportCompliance()` sinh **4 tệp CSV** đúng bố cục bản minh hoạ Mục 13.5: `01_tong_quan` · `02_kiem_tra_ve_sinh_bep` · `03_nhiet_do_tu_luu_mau` · `04_kiem_soat_con_trung`, tên tệp kèm khoảng ngày. Có `csvCell()` thoát dấu phẩy/nháy (tên món và biện pháp xử lý côn trùng đều chứa dấu phẩy) và **BOM UTF-8** (thiếu BOM là Excel trên Windows đọc hỏng toàn bộ tiếng Việt — mà đây là tệp nộp cho đoàn kiểm tra) |
| `compliance/report/page.tsx` | Field 1b: bảng liệt kê toàn bộ `sessionsInPeriod` **bỏ hẳn**, thay bằng khối "Dữ liệu nguồn của kỳ" — 3 dòng tổng hợp + link ra `inspections` / `conduct-inspection/history`. `threeStepRows` cũng gỡ khỏi payload gửi. Bù lại, `failedRows` của phiên kiểm thực nay gộp thêm bữa/món · bước không đạt · nhiệt lõi <75°C · ghi chú xử lý — nếu không thì các thông tin đó biến mất cùng cái bảng. Câu côn trùng khi rỗng đổi thành **"Chưa được chốt trong báo cáo kỳ."** ở cả 2 chỗ |
| `roles.ts` | Bỏ mục nav "Tuân thủ theo kỳ"; nhóm "Báo cáo & Thống kê" còn 2 mục |

### 15.2. Ràng buộc đã tuân thủ khi code (dễ tái phạm, ghi lại để kiểm tra sau)

1. **Không có cột "Loại kiểm tra"** trong bất kỳ tệp xuất nào — BR-208 Deprecated (Mục 3, Mục 13.3 điểm 3 gạch đầu dòng cuối).
2. **Export = dữ liệu đầy đủ**, không lọc ngoại lệ; ngược lại **báo cáo UC-185 = exception-only**. Hai nguyên tắc trái nhau có chủ đích vì hai mục đích khác nhau (hồ sơ audit vs. văn bản gửi Ban giám hiệu) — đã ghi ngay trong comment của cả hai file để không bị "đồng bộ hoá" nhầm về sau.
3. **Định dạng CSV, không PDF** cho UC-115/117 — khớp UC-117 gốc.
4. **Detail view không tính lại số liệu** — mọi con số đọc từ snapshot trong `SentReport`.

### 15.3. Việc CHƯA làm / cần lưu ý

1. **Git tag + commit riêng CHƯA tạo được**: môi trường shell của phiên vẫn không khởi động (`Workspace unavailable`). Cần tạo `pre-compliancehistory-removal-270726` thủ công tại commit ngay trước đợt này rồi điền vào bảng 13.4.
2. **`tsc --noEmit` CHƯA chạy** (cùng lý do). Rà thủ công: `StatusBadge`/`Table*` gỡ khỏi `compliance/report` sau khi bỏ bảng; `FileDown`/`Send`/`toast` gỡ khỏi `reports/page.tsx`; `BarChart3` gỡ khỏi `compliance/page.tsx` (vẫn dùng ở role khác trong `roles.ts`); `failedStep`/`failReason` vẫn có điểm dùng (đã chuyển sang phục vụ `failedRows`); mọi nhánh đọc field riêng của union đều nằm sau `report.reportType === …` nên TypeScript thu hẹp được kiểu.
3. **Bản minh hoạ 4 CSV (Mục 13.5)**: code sinh **đúng bố cục cột** đã duyệt, nhưng dữ liệu lấy động theo khoảng ngày người dùng chọn chứ không cố định 12/05–06/07/2026 như bản minh hoạ.
4. **Screen Spec chưa cập nhật**: `15_ViewFoodSafetyReportsScreen.md`, `28_SendComplianceReportDialog.md`, `32_SentReportsHistory.md`.
5. **Phụ thuộc chéo còn hở** (nhắc lại từ Mục 12.4): `review_medicalstaff_violations.md` Mục 9.2 — cơ chế 2-tick cho Biên bản Vi phạm vẫn chưa áp code.

---

## 16. Trạng thái áp dụng — ĐÃ ÁP CODE Mục 14 (Web, Medical Staff), 27/07/2026

### 16.1. Thay đổi theo file

| File | Thay đổi đã áp |
| --- | --- |
| `components/app/report-document.tsx` (MỚI) | Component **tài liệu báo cáo chỉ-đọc dùng chung** (Mục 14.3 điểm 3): `ReportDocument` + `buildReportDocumentHtml` + `REPORT_TYPE_LABEL`. Kiểu `ReportDocumentData` là union theo `reportType`, các field mốc gửi (`id`/`sentAt`/`sentBy`/`recipient`) để optional — thiếu chúng thì tự hiển thị nhãn "BẢN XEM TRƯỚC — CHƯA GỬI". `SentReport` thoả kiểu này về cấu trúc nên UC-190 truyền thẳng bản ghi vào |
| `reports/[id]/page.tsx` | Rút gọn: bỏ khuôn render riêng và `buildReportHtml` cục bộ, dùng `ReportDocument`/`buildReportDocumentHtml`. Giữ nguyên banner BR-178 và nút "In / Xuất PDF" |
| `compliance/report/page.tsx` | Nhận `?period=Daily\|Weekly` (pre-fill kỳ) và `?preview=1` (mở thẳng Xem trước). Thêm nút "Xem trước"/"Quay lại soạn" ở header; chế độ xem trước dựng `previewDoc` khớp **chính xác** payload mà `submit()` sẽ gửi rồi render bằng component dùng chung. Bọc `Suspense` vì dùng `useSearchParams` |
| `compliance/page.tsx` | **Viết lại toàn bộ** theo wireframe Mục 14.2. Header không còn nút hành động nào (bỏ "Tạo báo cáo tổng hợp" theo 14.3 điểm 1, bỏ "Kiểm tra an toàn bếp" theo 14.1). Khối 1 "Tình trạng công việc hôm nay" (Kiểm tra bếp · Hồ sơ kiểm thực · Lưu mẫu) cố định theo ngày hiện tại, **không có CTA điều hướng thao tác**, chân khối là "Hoàn thành X/3 · Cần chú ý N". Bộ lọc kỳ đặt **ngay trên** khối biểu đồ + ngoại lệ, không ở đầu trang. Khối 2: 2 biểu đồ **CSS thuần** (`BarColumn`) — xu hướng điểm vệ sinh theo phiên; phân bố Đạt/Đang thực hiện/Không đạt. Khối 3 "Ngoại lệ cần chú ý" dựng từ dữ liệu, mỗi dòng có drill-down link về module nguồn. Khối 4 "Báo cáo tổng hợp": chọn kỳ → "Xem trước báo cáo"/"Chốt & gửi Ban giám hiệu" (đều điều hướng sang `compliance/report`, kỳ pre-fill) + dòng "Báo cáo đã gửi gần nhất" + "Xem lịch sử"; giữ khối "Xuất dữ liệu tuân thủ" (UC-115/117, Mục 13.3 điểm 3 — Mục 14 không thu hồi). Bỏ hẳn `useQuery("/compliance-report")` và KPI `summary.modules` |

### 16.2. Các nguyên tắc dữ liệu Mục 14.4 đã cài đặt đúng ở đâu

| Nguyên tắc | Cài đặt |
| --- | --- |
| 14.4.1 Không bảng danh sách chi tiết | Toàn màn không còn `Table` nào; chỉ chỉ số + drill-down link ở khối Ngoại lệ |
| 14.4.2 Card "bình thường" không chiếm chỗ | Nhiệt độ tủ trong ngưỡng = 1 dòng phụ trong khối "Lưu mẫu"; ngoài ngưỡng mới sinh 1 dòng đầy đủ (kèm mã mẫu + °C) ở khối Ngoại lệ |
| 14.4.3 Côn trùng không vào phép tính | `dailyDone` chỉ cộng 3 đầu việc vận hành; không có % côn trùng ở đâu |
| 14.4.4 Khám SK NV bếp là chỉ số định kỳ | Không vào `dailyDone`; chỉ sinh dòng ngoại lệ khi `staffHealthValid === false` |
| 14.4.5 Không suy "chưa kiểm tra hôm nay" từ phiên ngày khác | `todayKitchen` và `latestKitchen` là 2 biến riêng, render thành 2 dòng riêng ("Chưa thực hiện" + "Phiên gần nhất 24/06 · 92%") |
| 14.4.6 Mỗi số trả lời 1 câu hỏi | Endpoint `/compliance-report` gỡ khỏi màn; các chỉ số còn lại khớp bảng đối chiếu Mục 14.4 |

### 16.3. Việc CHƯA làm / cần lưu ý

1. **Git tag + commit riêng CHƯA tạo được**: shell của phiên vẫn không khởi động. Cần tạo `pre-compliance-dashboard-rewrite-270726` thủ công tại commit ngay trước đợt này rồi điền vào bảng 14.6.
2. **`tsc --noEmit` CHƯA chạy** (cùng lý do). Rà thủ công: `compliance/page.tsx` viết mới hoàn toàn nên không còn import thừa; `reports/[id]/page.tsx` gỡ `Badge`/`Table*`/`CURRENT_SCHOOL` sau khi chuyển sang component chung; `compliance/report/page.tsx` thêm `Suspense`/`useSearchParams`/`Eye`/`Pencil`/`ReportDocument` và bọc phần soạn trong `{!previewing && (…)}` — đã đối chiếu mở/đóng ngoặc.
3. **Endpoint `/compliance-report` KHÔNG xoá** dù Mục 14.6 yêu cầu: grep xác nhận `school-admin/reports/page.tsx` vẫn gọi nó (`queryKey: ["compliance-report"]`, `refetchInterval` 5 phút theo BR-180). Xoá sẽ làm gãy màn của actor khác — ngoài phạm vi đợt Medical Staff. Phía Medical Staff đã không còn điểm dùng nào. Cần một đợt review School Administration để quyết định số phận endpoint này.
4. **Ngày vận hành vẫn gán cứng `TODAY = "02/07/2026"`** trong `compliance/page.tsx` — giữ nguyên cách làm sẵn có của bản mock (Dashboard tổng quan dùng cùng mốc) để dữ liệu seed còn ý nghĩa; không phải quyết định thiết kế mới.
5. **Screen Spec chưa cập nhật**: `23_ComplianceDashboard.md` (cấu trúc mới Mục 14.2, ghi rõ thay thế Mục 10.3), `15_ViewFoodSafetyReportsScreen.md`.
6. **Phụ thuộc chéo còn hở** (nhắc lại): `review_medicalstaff_violations.md` Mục 9.2 — 2-tick cho Biên bản Vi phạm vẫn chưa áp code.

---

## 17. Vòng rà soát thứ 9 (27/07/2026) — Bỏ "Kiểm soát côn trùng" như một hồ sơ/hiển thị độc lập; gộp vào mục sẵn có của Kiểm tra an toàn bếp hàng ngày

> **TRẠNG THÁI: đã chốt qua xác nhận trực tiếp — `business_rules.md` (BR-102/BR-209) ĐÃ ÁP; phần code/mock-data CHƯA áp trong phạm vi hội thoại này** (Mục 15/16 cho thấy các đợt trước đã có tiến độ áp code song song ngoài hội thoại này — ghi nhận để biết, không phải căn cứ để đánh dấu mục này đã áp).

**Quyết định của người dùng**: bỏ hẳn hiển thị "Kiểm soát côn trùng" như một hồ sơ/màn/card độc lập. Nếu có ghi nhận côn trùng, nó nằm **trong 1 mục sẵn có của phiên Kiểm tra an toàn bếp hàng ngày (UC-014)** nếu Medical Staff chủ động thêm vào lúc đó — nếu phiên đó không có gì để ghi, thì thôi, không hiển thị/theo dõi gì thêm.

### 17.1. Đối chiếu với checklist UC-014 đã có — không cần field mới

Đọc `CHECKLIST_GROUPS_SEED` (`mock-data.ts`) xác nhận checklist 6 nhóm cố định của UC-014 **đã có sẵn 2 mục liên quan côn trùng, có căn cứ pháp lý rõ ràng** (khác hẳn mô hình "nhật ký" độc lập ở BR-102 gốc — vốn không có căn cứ luật, xem Mục 1):
- `CLI-3-8` (Nhóm 3 — Điều kiện bảo quản thực phẩm): "Không có dấu vết của côn trùng, động vật gây hại" — Luật ATTP Điều 19.1.c, Điều 20.1.b.
- `CLI-4-6` (Nhóm 4 — Điều kiện khu vực sơ chế, chế biến): "Khu chế biến có phương tiện phòng chống côn trùng và động vật gây hại" — Luật ATTP Điều 28.5; TTLT 13/2016 Phụ lục 04 Mục IV.4.1.

Cả hai đã là mục Đạt/Không đạt bắt buộc trong mọi phiên kiểm tra bếp, và khi "Không đạt" đã có sẵn cơ chế `itemFindings` (lý do + số ảnh minh chứng, xem Mục 6) để ghi chi tiết phát hiện. **Đây chính xác là "1 mục của kiểm tra an toàn bếp hàng ngày nếu được Medical Staff thêm vào"** người dùng vừa mô tả — không cần dựng field/mục mới, chỉ cần dùng đúng cơ chế đã có: nếu phiên đó phát hiện côn trùng, Medical Staff đánh "Không đạt" ở CLI-3-8/CLI-4-6 kèm lý do + ảnh; nếu không phát hiện gì, 2 mục này "Đạt" như bình thường và không có gì thêm để hiển thị ở nơi khác.

### 17.2. Phần cần bỏ — mô hình "nhật ký côn trùng" độc lập (đảo ngược Mục 1 và Mục 11.3)

- **Đảo ngược Mục 1** (09/07/2026): mô hình "nhật ký sự kiện" (`PestControlLogEntry`: ngày kiểm soát/xử lý, đơn vị thực hiện, loại côn trùng + số lượng ước tính, biện pháp xử lý, ngày hẹn tiếp theo) — bỏ hẳn, không còn là một loại hồ sơ hệ thống theo dõi riêng.
- **Đảo ngược Mục 11.3** (26/07/2026 — quyết định trước đó là "giữ hiển thị, xoá seed giả, để trạng thái rỗng trung thực"): không giữ hiển thị dưới bất kỳ hình thức nào nữa (không card, không tab, không dòng trạng thái rỗng riêng) — vì bản thân việc duy trì một chỗ hiển thị cho dữ liệu không có nguồn nhập thật (Mục 11.1: 0 kết quả `useCreate` vào `pest-control-log` ở mọi actor) là gánh nặng không cần thiết, đúng như lý do đổi BR-102 (Mục này, xem `business_rules.md`).
- **Siết chặt thêm Mục 14.4 điểm 3** (Vòng rà soát thứ 7 — "chưa chốt quy tắc dữ liệu thì không hiển thị %, không đưa vào tổng điểm"): nay không chỉ "không hiển thị %", mà **không hiển thị gì độc lập cho côn trùng nữa** — thông tin côn trùng (nếu có) chỉ xuất hiện lồng trong kết quả/lý do của chính phiên kiểm tra bếp hôm đó (CLI-3-8/CLI-4-6), không có dòng/khối riêng ở Compliance Dashboard hay báo cáo tổng hợp.

### 17.3. `business_rules.md` — đã áp

- **BR-102** viết lại: bỏ mô hình "nhật ký kiểm soát côn trùng" độc lập; nội dung mới nêu rõ côn trùng là **mục tùy chọn lồng trong UC-014**, không có màn/hồ sơ riêng, không suy diễn khi phiên không ghi nhận gì.
- **BR-209** (nội dung báo cáo tuân thủ định kỳ, UC-185): bỏ dòng "tình trạng hiệu lực chứng nhận kiểm soát côn trùng (BR-102)" khỏi nội dung tối thiểu của báo cáo — nếu phiên trong kỳ có phát hiện côn trùng, đã nằm sẵn trong lý do/ghi chú của phiên đó (field đã gộp ở Mục 13.3 điểm 2/15.1 dòng `compliance/report/page.tsx`), không tách thành field báo cáo riêng.

### 17.4. Checklist thay đổi cần áp dụng theo file — CHƯA áp trong hội thoại này

| File / Component | Thay đổi | Trạng thái |
| --- | --- | --- |
| `business_rules.md` BR-102, BR-209 | Sửa nội dung như Mục 17.3 | **ĐÃ ÁP 27/07/2026** |
| `mock-data.ts` | Xoá hẳn `PestControlLogEntry` (interface) và `PEST_CONTROL_LOG` (mảng, dù đang rỗng); xoá field tổng hợp `pestControlValid` trên `KitchenInspection` nếu nó chỉ là bản tóm tắt riêng của CLI-3-8/CLI-4-6 (rà lại điểm dùng thật của field này trước khi xoá — có thể đang được `compliance/page.tsx`/`conduct-inspection/history/page.tsx` đọc trực tiếp làm badge nhanh, cần thay bằng cách đọc từ `itemResults`/`itemFindings` của 2 mục CLI-3-8/CLI-4-6 nếu injector đã lưu đủ theo cấu trúc checklist 6 nhóm) | **ĐÃ ÁP CODE 27/07/2026 (một phần)** — `PestControlLogEntry`/`PEST_CONTROL_LOG` xoá hẳn; `pestControlValid` **GIỮ + đánh `@deprecated`**, xem Mục 18.2 |
| `lib/api/db.ts` | Bỏ đăng ký resource `pest-control-log` (không còn nơi đọc/ghi) | **ĐÃ ÁP CODE 27/07/2026** |
| `compliance/page.tsx` | Rà lại khối "Côn trùng & khám sức khỏe NV bếp" ở Vùng 1 (Mục 16.1 ghi khối này gộp chung côn trùng + khám SK) — tách khám SK NV bếp (giữ, chỉ số định kỳ theo Mục 14.4 điểm 4) ra khỏi phần côn trùng (bỏ hẳn dòng riêng cho côn trùng, đúng Mục 17.2) | **ĐÃ ÁP CODE 27/07/2026** — xem đính chính ở Mục 18.3 điểm 1 |
| `compliance/report/page.tsx` | Xoá field `latestPestControl` khỏi payload gửi và khỏi UI soạn báo cáo (đã có dòng "Chưa được chốt trong báo cáo kỳ" theo Mục 15.1 — nay bỏ hẳn, không còn field này ở bất kỳ trạng thái nào) | **ĐÃ ÁP CODE 27/07/2026** |
| `conduct-inspection/history/page.tsx`, modal "Chi tiết phiên kiểm tra" | Rà lại `confirmBadge`/hiển thị `pestControlValid` (Mục 8.4 điểm 1 có nhắc "2 chứng nhận BR-102/103") — đổi cách đọc sang trực tiếp từ `itemResults` của CLI-3-8/CLI-4-6 nếu field tổng hợp bị xoá | **ĐÃ ÁP CODE 27/07/2026** — bỏ hẳn dòng hiển thị, không cần đọc thay từ `itemResults` |
| `15_ViewFoodSafetyReportsScreen.md`, `12_ConductFoodSafetyInspectionScreen.md`, `23_ComplianceDashboard.md` | Cập nhật mô tả: bỏ khái niệm "nhật ký côn trùng" độc lập; ghi rõ CLI-3-8/CLI-4-6 là nơi duy nhất phản ánh côn trùng | CHƯA áp (Screen Spec) |

**Git checkpoint**: cùng quy ước các Mục trước — tạo tag trước khi áp code (VD `pre-pestcontrol-merge-270726`), commit riêng trỏ về Mục 17. ⚠️ **CHƯA thực hiện được** — xem Mục 18.4.

**Trạng thái chung**: chốt qua xác nhận trực tiếp 27/07/2026 — `business_rules.md` đã áp ngay; **code/mock-data phía Medical Staff ĐÃ ÁP 27/07/2026**, chi tiết Mục 18. Screen Spec vẫn CHƯA áp.

---

## 18. Trạng thái áp dụng — ĐÃ ÁP CODE Mục 17 (Web, Medical Staff), 27/07/2026

### 18.1. Thay đổi theo file

| File | Thay đổi đã áp |
| --- | --- |
| `mock-data.ts` | Xoá hẳn `PestControlLogEntry` + `PEST_CONTROL_LOG`, thay bằng khối chú thích ghi lại **cả đường đi 3 bước** của quyết định (Mục 1 → 11.3 → 17) và chỉ rõ `CLI-3-8`/`CLI-4-6` là nơi duy nhất phản ánh côn trùng — để không ai dựng lại mô hình nhật ký lần thứ ba. `ComplianceSentReport.latestPestControl` đánh `@deprecated` (giữ để đọc báo cáo đã gửi trước ngày này); seed `RPTS-2026-001` gỡ `latestPestControl: null` |
| `lib/api/db.ts` | Bỏ đăng ký resource `pest-control-log` + import `PEST_CONTROL_LOG`; để lại comment nêu lý do |
| `compliance/page.tsx` | Gỡ query `pest-control-log`, biến `latestPest`; **bỏ tệp `04_kiem_soat_con_trung.csv`** khỏi bộ xuất (nay còn 3 tệp), bỏ dòng "Số bản ghi kiểm soát côn trùng" khỏi tệp tổng quan và **bỏ cột "Kiểm soát côn trùng hợp lệ"** khỏi tệp kiểm tra vệ sinh bếp; sửa lại nhãn "Tải về 3 tệp" |
| `compliance/report/page.tsx` | Gỡ query + `latestPest`; bỏ `latestPestControl` khỏi **payload gửi** và khỏi **`previewDoc`**; bỏ khối "Kiểm soát côn trùng — bản ghi nhật ký gần nhất" (field 5 cũ) và dòng côn trùng trong khối "Dữ liệu nguồn của kỳ" |
| `components/app/report-document.tsx` | Bỏ `latestPestControl` khỏi kiểu `ComplianceBody`, khỏi bản in (`buildReportDocumentHtml`) và khỏi UI — khuôn tài liệu dùng chung nên sửa một chỗ là cả "Xem trước" lẫn "Xem báo cáo" (UC-190) cùng đổi |
| `conduct-inspection/page.tsx` | Bỏ ô tick "Chứng nhận kiểm soát côn trùng quý còn hiệu lực (BR-102)" và state `pestControlValid`; comment giải thích đây là tàn dư của mô hình BR-102 cũ và trỏ sang CLI-3-8/CLI-4-6. Payload gửi `pestControlValid: true` cố định (lý do ở Mục 18.2) |
| `conduct-inspection/history/page.tsx` | Bỏ dòng "Kiểm soát côn trùng: Hợp lệ/Không hợp lệ" trong modal chi tiết phiên; giữ dòng khám SK NV bếp |
| `conduct-inspection/[id]/record/page.tsx` | Bỏ mệnh đề "Kiểm soát côn trùng: hợp lệ/KHÔNG hợp lệ" khỏi nội dung **bản in biên bản** |
| `components/app/send-compliance-report-dialog.tsx` | Component đã ngừng dùng từ 27/07 nhưng vẫn được biên dịch — gỡ import `PestControlLogEntry`, query, và khối hiển thị/payload côn trùng để không giữ điểm phụ thuộc vào resource đã xoá |

### 18.2. Điểm KHÔNG làm đúng như checklist — có lý do

**`pestControlValid` trên `KitchenInspection`: GIỮ, không xoá.** Mục 17.4 yêu cầu "xoá nếu nó chỉ là bản tóm tắt riêng của CLI-3-8/CLI-4-6" và yêu cầu rà điểm dùng trước. Kết quả rà:

- `school-admin/compliance/page.tsx` (2 chỗ) và `school-admin/reports/[slug]/page.tsx` (1 cột bảng) đang đọc **trực tiếp** field này.
- Xoá field sẽ làm gãy 2 màn của actor khác — nằm ngoài phạm vi đợt Medical Staff, đúng tiền lệ đã dùng cho `service-caterers/*` và `school-admin/*` ở các Mục trước.

Xử lý thay thế: field được đánh `@deprecated` kèm lý do đầy đủ; **mọi điểm hiển thị phía Medical Staff đã gỡ hết**; POST `/kitchen-inspections` vẫn nhận và lưu giá trị để dữ liệu phiên mới không thiếu field so với dữ liệu cũ. Việc dọn field thuộc đợt review School Administration.

Cũng vì lý do đó, **không** chuyển sang đọc côn trùng từ `itemResults` của CLI-3-8/CLI-4-6 như checklist gợi ý: các bản ghi seed `KITCHEN_INSPECTIONS` (KIN-2026-015…018) không có `itemResults` — field này chỉ được sinh từ phiên tạo mới sau Mục 8.4. Đọc thay từ đó sẽ ra "không xác định" với toàn bộ dữ liệu lịch sử. Mà theo đúng Mục 17.2 thì phía Medical Staff **không cần** hiển thị côn trùng ở đâu cả, nên cách đúng là bỏ hiển thị chứ không phải đổi nguồn.

### 18.3. Đính chính so với mô tả trong Mục 17.4

1. Mục 17.4 mô tả `compliance/page.tsx` còn "khối Côn trùng & khám sức khỏe NV bếp ở Vùng 1 (theo Mục 16.1)". Thực tế khối đó **đã không còn** từ đợt áp Mục 14 (Vòng 7 viết lại toàn màn theo wireframe 14.2 — 3 khối "Tình trạng hôm nay" là Kiểm tra bếp / Hồ sơ kiểm thực / Lưu mẫu, khám SK NV bếp chỉ xuất hiện ở "Ngoại lệ cần chú ý" khi không hợp lệ). Phần côn trùng còn sót lại ở màn này chỉ nằm trong **bộ xuất CSV**, và đã gỡ.
2. Bản minh hoạ 4 tệp CSV (Mục 13.5) nay còn **3 tệp** — `04_kiem_soat_con_trung.csv` không còn nguồn dữ liệu để xuất.

### 18.4. Việc CHƯA làm / cần lưu ý

1. **Git tag + commit riêng CHƯA tạo được**: shell của phiên vẫn không khởi động (`Workspace unavailable`). Cần tạo `pre-pestcontrol-merge-270726` thủ công tại commit ngay trước đợt này rồi điền vào bảng 17.4.
2. **`tsc --noEmit` CHƯA chạy** (cùng lý do). Rà thủ công: grep toàn `src` cho `PestControlLogEntry`/`PEST_CONTROL_LOG`/`pest-control-log`/`latestPest` — chỉ còn trong **chú thích**, không còn tham chiếu code; `pestControlValid` chỉ còn ở khai báo type, seed, handler POST và 3 điểm đọc của `school-admin`.
3. **Screen Spec chưa cập nhật**: `12_ConductFoodSafetyInspectionScreen.md` (bỏ ô tick chứng nhận côn trùng), `15_ViewFoodSafetyReportsScreen.md`, `23_ComplianceDashboard.md`.
4. **Cần đợt review School Administration** để quyết 2 việc đang treo cùng nhau: dọn `pestControlValid` (Mục 18.2) và số phận endpoint `/compliance-report` (Mục 16.3 điểm 3).
5. **Phụ thuộc chéo còn hở** (nhắc lại): `review_medicalstaff_violations.md` Mục 9.2 — cơ chế 2-tick cho Biên bản Vi phạm vẫn chưa áp code.

---

## 19. Vòng rà soát thứ 10 (27/07/2026) — 3 lỗi trên UC-014/UC-112 phát hiện qua ảnh chụp code thật: checklist không phải wizard, sai điểm vào, PDF nhảy sang role Caterers

> **TRẠNG THÁI: đã chốt qua ảnh chụp code thật + xác nhận trực tiếp 27/07/2026 — CHƯA áp code trong phạm vi hội thoại này.**

**Nguồn**: 5 ảnh chụp code thật do người dùng cung cấp (Kiểm tra an toàn bếp UC-014, Hồ sơ kiểm thực UC-112 modal chi tiết, Sổ lưu mẫu UC-118 ×2, Lập biên bản vi phạm) — 3 ảnh đầu thuộc phạm vi file này, 2 ảnh sau xử lý ở `review_medicalstaff_foodsamples.md` Mục 13 và `review_medicalstaff_violations.md` Mục 10.

### 19.1. UC-014 — Checklist hiển thị dạng accordion xổ hết 6 nhóm, không phải wizard từng bước

**Vấn đề**: Mục 8.4 điểm 2 (26/07/2026) chốt "bỏ Stepper ngang, thay bằng Collapsible Panel mỗi nhóm" — nhưng thực thi thành accordion **xổ hết 6 nhóm xuống trang** (mỗi nhóm 1 panel có thể mở/đóng, nhưng tất cả cùng hiển thị đồng thời), khác với ý định wizard "làm xong nhóm này mới sang nhóm kế". Ảnh chụp xác nhận: Nhóm 1 mở sẵn với đầy đủ hạng mục, Nhóm 2-6 xếp chồng bên dưới dạng dòng tiêu đề thu gọn — về bản chất là 1 accordion dài, không phải 1 wizard.

**Quyết định**: đổi thành **wizard đúng nghĩa** — chỉ 1 nhóm hiển thị đầy đủ tại một thời điểm. Wireframe đã chốt:

```
[ 1 — Hồ sơ                         ▼ ]

Bước 1/6 · 0/2 hạng mục đã đánh giá

[ Nội dung checklist của bước 1 ]

[ ← Quay lại ]                 [ Tiếp theo · 2/6 → ]
```

- Toàn màn giữ đúng **1 route**: `/medical-staff/conduct-inspection` (không tách route con) → sidebar → lịch sử → `[Tạo phiên kiểm tra mới]` → form 6 bước ở chính route này.
- Label là **dropdown chọn nhóm** (VD "1 — Hồ sơ ▾"), liệt kê đủ 6 nhóm kèm số hạng mục đã đánh giá/tổng mỗi nhóm — cho phép nhảy **không tuần tự** (VD từ Nhóm 1 sang thẳng Nhóm 3).
- **Nhảy bước chỉ đổi màn đang xem, không tính là hoàn thành nhóm đó.** Thanh tiến độ tổng (khối "Tiến độ phiên kiểm tra") tiếp tục tính theo **số hạng mục thực sự đã đánh giá** (Đạt/Không đạt đã bấm), không tính theo việc đã "ghé qua" nhóm nào.
- Nút "Tiếp theo · x/6 →" điều hướng sang đúng 1 nhóm kế tiếp theo `order`; thêm nút "← Quay lại" (hiện tại chưa có, chỉ có "Tiếp theo").
- **Sửa đúng gốc rễ ở code**: `conduct-inspection/page.tsx` hiện render **tất cả `CollapsiblePanel` đồng thời** (dù chỉ 1 panel ở trạng thái mở) — đây là lý do accordion "xổ hết". Phải đổi sang chỉ `render(currentGroup)` — 5 nhóm còn lại không mount, không chỉ là "đóng lại bằng CSS/state" như accordion hiện tại.
- Không thay đổi cơ chế validate per-item, gate "Hoàn thành phiên kiểm tra" (đủ hạng mục + ảnh/lý do, MSG-016) đã có ở Mục 8.4/12.1 — chỉ đổi cách **trình bày và render** 6 nhóm, không đổi luật đánh giá.

### 19.2. UC-014 — Sai điểm vào: bấm "Kiểm tra an toàn bếp" ở sidebar mở thẳng form tạo phiên, không phải lịch sử

**Vấn đề**: Mục 8.4 điểm 1 (26/07/2026) và Mục 14.1 (27/07/2026, khi bỏ nút header trùng lối) đều đã chốt `conduct-inspection/history` là **điểm vào chính** của UC-014. Ảnh chụp xác nhận bấm mục "Kiểm tra an toàn bếp" ở sidebar mở thẳng màn checklist tạo phiên mới (`conduct-inspection`, tiêu đề "Kiểm tra an toàn bếp (UC-014)") — sai với quyết định đã chốt 2 lần, là hồi quy (regression) chứ không phải quyết định mới.

**Quyết định**: sửa `href` của mục nav "Kiểm tra an toàn bếp" trong `roles.ts` từ `/medical-staff/conduct-inspection` sang `/medical-staff/conduct-inspection/history` — không đổi tên hiển thị, không đổi vị trí trong sidebar.

### 19.3. UC-112 — Nút "In / Lưu PDF" điều hướng chéo sang role Service Caterers

**Vấn đề**: Mục 8.5 điểm 2 (26/07/2026) chốt "dùng lại đúng route in đã có phía Service Caterers (`/service-caterers/inspections/[id]/print`)". Ảnh chụp xác nhận hệ quả không mong muốn: bấm "In / Lưu PDF" từ modal chi tiết phía Medical Staff **điều hướng thẳng vào route nằm trong layout role Service Caterers** — đổi hẳn sidebar/app shell sang giao diện Caterers, không phải một bản in/preview độc lập. Đây là lỗi kiến trúc của chính quyết định gốc (dùng deep-link route thuộc role khác cho mục đích xem chéo), không phải lỗi thực thi.

**Mục đích đúng của màn này** (theo mô tả người dùng): xem được **chi tiết các bước Service Caterers đã làm** dưới dạng snapshot chỉ đọc, và **đánh giá Đạt/Không đạt ngay tại đó** (kèm lý do bắt buộc khi Không đạt) — phần đánh giá này **đã có sẵn đúng vị trí** (khối "Đánh giá của Y tế khi xem lại", Mục 8.5 điểm 1/16.2) nên không cần thiết kế lại; vấn đề là cơ chế xem PDF và hình thức Dialog.

**Quyết định — đổi từ Dialog sang route riêng, đúng luồng đầy đủ**:

```
Danh sách hồ sơ kiểm thực
  → Detail phiên kiểm thực của Medical Staff (route /medical-staff/inspections/[id])
  → Xem bản ghi/bằng chứng Caterers đã chốt
  → Y tế đánh giá: Đạt hoặc Không đạt
  → Nếu Không đạt: bắt buộc nhập lý do
  → Lưu đánh giá
  → Xem trước / In PDF của hồ sơ đã đánh giá
```

- Đổi modal hiện tại thành **route riêng** `/medical-staff/inspections/[id]` (không phải dialog) — đủ không gian hiển thị toàn bộ snapshot thay vì thu gọn trong popup.
- Nội dung là **snapshot read-only từ Caterers**, đầy đủ: dữ liệu ba bước đã thực hiện (Mẫu 1/2/3, đúng cấu trúc đã render ở Mục 6); ảnh, GPS, nhiệt độ lõi, ghi chú của từng bước; trạng thái từng bước; bản PDF/bản xem trước do chính Caterers đã sinh; khu đánh giá Đạt/Không đạt của Medical Staff (đã có, giữ nguyên vị trí).
- **Điều kiện hiển thị nút "In hồ sơ chính thức"**: chỉ bật khi Caterers đã chốt tài liệu (`overall !== "IN_PROGRESS"`, đúng field đã có). Nếu Caterers **chưa chốt**, màn chỉ hiển thị dữ liệu nháp/trạng thái hiện tại, **không cho in hồ sơ chính thức** — tránh phát hành 1 bản PDF "chính thức" cho dữ liệu còn đang dở.
- Việc **render PDF** tách khỏi route in của Service Caterers, thành 1 hàm/component dùng chung không phụ thuộc role (VD `buildInspectionRecordHtml`-kiểu đã dùng cho biên bản kiểm tra bếp, Mục 12.1) — Medical Staff mở bản in này trong tab mới/khu xem trước ngay tại route `/medical-staff/inspections/[id]`, không đi qua bất kỳ route nào nằm trong `app/(app)/service-caterers/*`.
- Nguyên tắc chung rút ra: **không deep-link trực tiếp vào route thuộc layout của role khác cho mục đích xem chéo** — tách logic dùng chung ra `components/app/` hoặc `lib/`, mỗi role tự gọi trong route của chính mình.

### 19.4. UC-014 — "Chi tiết phiên kiểm tra" cũng phải là page riêng, view-only, hiển thị đúng tình trạng xác nhận của bếp

**Vấn đề**: cùng loại vấn đề vừa sửa ở 19.3 cho UC-112 — "Chi tiết phiên kiểm tra" của UC-014 hiện là **Dialog** (`conduct-inspection/history/page.tsx`, state `detail`/`setDetail`), không phải page riêng. Ảnh chụp bổ sung cho thấy dialog này chưa thể hiện rõ **tình trạng xác nhận hiện tại của phiên** (đã kiểm tra xong & đang chờ Caterers xác nhận / Caterers đã xác nhận kết quả) — cột "Xác nhận của bếp" đã có ở bảng danh sách (badge từ hàm `confirmBadge`, Mục 8.4 điểm 1), nhưng thông tin này cần lặp lại rõ ràng **ngay trong chính màn chi tiết**, không chỉ ở cột danh sách.

**Quyết định**: áp đúng nguyên tắc vừa chốt ở 19.3 — đổi Dialog thành route riêng `/medical-staff/conduct-inspection/[id]` (view-only, phân biệt với `/conduct-inspection/[id]/record` là route lập biên bản), nội dung:
- Toàn bộ những gì Medical Staff đã đánh giá, snapshot đầy đủ theo `itemResults` (mọi hạng mục, không chỉ hạng mục Không đạt) — đúng cấu trúc 6 nhóm đã dùng ở wizard tạo phiên (Mục 19.1).
- Khối trạng thái xác nhận đặt ngay đầu trang, tách biệt khỏi kết quả Đạt/Không đạt: **"Đã hoàn thành — đang chờ Service Caterers xác nhận"** (`AWAITING_CATERERS_CONFIRM`) hoặc **"Service Caterers đã xác nhận kết quả"** (`CONFIRMED`), dùng cùng bộ nhãn với `confirmBadge` hiện có, không tạo nhãn thứ hai.
- Nút "Lập biên bản sự việc" chỉ bật khi `CONFIRMED` (đã đúng theo Mục 8.4 điểm 4), đặt ngay trong route chi tiết này, không cần quay lại danh sách.

**Cần verify trước khi áp code — nghi vấn dữ liệu**: ảnh chụp cho thấy phiên `KIN-2026-101` (định dạng mã khác hẳn seed cũ `KIN-2026-015..018`, nhiều khả năng là phiên vừa tạo qua wizard thật) lại hiển thị badge **"Phiên cũ (trước cơ chế xác nhận)"** và "Hạng mục đã đánh giá (0)" — tức `confirmStatus`/`itemResults` trống, đúng nhánh dành cho dữ liệu **cũ hơn** cơ chế xác nhận (Mục 8.4). Grep trực tiếp `conduct-inspection/page.tsx` xác nhận code tạo phiên **đã đúng** — dòng 169 set `confirmStatus: "AWAITING_CATERERS_CONFIRM"`, dòng 171 set `itemResults: ALL_ITEMS.map(...)`. Nghĩa là 1 trong 2 khả năng: (a) `KIN-2026-101` thực ra là dữ liệu test/thủ công không đi qua đúng luồng submit, hoặc (b) có lỗi ở tầng lưu trữ mock (`db.ts`/mock-server) khiến field không được ghi lại đúng dù code gửi đi đã đúng. **Cần chạy thử tạo 1 phiên mới thật qua UI và kiểm tra ngay dữ liệu lưu được** trước khi kết luận đây là bug ở đâu — chưa đủ căn cứ để ghi vào checklist là "sửa code tạo phiên", vì code đó grep ra đã đúng.

### 19.5. Checklist thay đổi cần áp dụng theo file — CHƯA áp code

| File / Component | Thay đổi | Trạng thái |
| --- | --- | --- |
| `conduct-inspection/page.tsx` | Đổi render tất cả `CollapsiblePanel` đồng thời → chỉ `render(currentGroup)`; label bước là dropdown chọn nhóm (nhảy tự do, không tính là hoàn thành); thêm nút "← Quay lại"; tiến độ tính theo số hạng mục đã đánh giá thật, không theo nhóm đã ghé qua | **ĐÃ ÁP CODE 27/07/2026** |
| `roles.ts` | Sửa `href` mục nav "Kiểm tra an toàn bếp": `/medical-staff/conduct-inspection` → `/medical-staff/conduct-inspection/history` | **ĐÃ ÁP CODE 27/07/2026** |
| `medical-staff/inspections/[id]/page.tsx` (MỚI) | Route Detail thay modal: snapshot đầy đủ 3 bước (ảnh/GPS/nhiệt độ lõi/ghi chú/trạng thái từng bước) + bản PDF/preview do Caterers sinh + khu đánh giá Đạt/Không đạt của Y tế (chuyển từ modal cũ). Nút "In hồ sơ chính thức" chỉ bật khi Caterers đã chốt (`overall !== "IN_PROGRESS"`) | **ĐÃ ÁP CODE 27/07/2026** |
| `medical-staff/inspections/page.tsx` | Bỏ modal chi tiết cũ; hàng trong bảng điều hướng sang route `[id]` mới thay vì mở dialog | **ĐÃ ÁP CODE 27/07/2026** |
| `service-caterers/inspections/[id]/print/page.tsx` | Tách hàm render HTML/PDF ra `components/app/` hoặc `lib/` dùng chung, không gắn riêng route Caterers | **ĐÃ ÁP CODE 27/07/2026** — `src/lib/inspection-sheets.ts`; route Caterers giữ nguyên URL/hành vi, chỉ đổi nguồn render |
| `12_ConductFoodSafetyInspectionScreen.md` | Cập nhật mô tả Platform Layout: wizard 1-nhóm + dropdown nhảy bước (thay mô tả accordion cũ) | CHƯA áp (Screen Spec) |
| `11_FoodInspectionRecords.md` | Đổi mô tả Detail từ Dialog sang route riêng; ghi rõ điều kiện bật "In hồ sơ chính thức" và dùng component in dùng chung, không điều hướng chéo role | CHƯA áp (Screen Spec) |
| `medical-staff/conduct-inspection/[id]/page.tsx` (MỚI) | Route Detail view-only cho UC-014 (Mục 19.4) thay Dialog hiện tại ở `history/page.tsx`; snapshot đầy đủ `itemResults`; khối trạng thái xác nhận đặt đầu trang dùng chung nhãn với `confirmBadge`; nút "Lập biên bản sự việc" chỉ bật khi `CONFIRMED` | **ĐÃ ÁP CODE 27/07/2026** — xem Mục 21 |
| `medical-staff/conduct-inspection/history/page.tsx` | Bỏ Dialog `detail`/`setDetail`; hàng trong bảng điều hướng sang route `[id]` mới | **ĐÃ ÁP CODE 27/07/2026** |
| — | **Verify trước khi sửa code**: xác định nguyên nhân `KIN-2026-101` thiếu `confirmStatus`/`itemResults` (Mục 19.4) — tạo thử 1 phiên mới qua UI thật, kiểm tra dữ liệu lưu được, trước khi giả định lỗi nằm ở đâu | **ĐÃ VERIFY + ĐÃ SỬA 27/07/2026** — nguyên nhân là (b), xác định bằng đọc code; xem Mục 21.1 |
| `lib/api/mock-server.ts` | Bổ sung `confirmStatus`/`itemResults` vào handler `POST /kitchen-inspections` (bug tìm ra khi verify) | **ĐÃ ÁP CODE 27/07/2026** |
| `components/app/kitchen-confirm-badge.tsx` (MỚI) | Tách bộ nhãn trạng thái xác nhận dùng chung giữa cột danh sách và route Detail (yêu cầu "không tạo nhãn thứ hai" của Mục 19.4) | **ĐÃ ÁP CODE 27/07/2026** |

**Git checkpoint**: cùng quy ước các Mục trước — tạo tag trước khi áp code (VD `pre-uc014-wizard-fix-270726`), commit riêng trỏ về Mục 19. ⚠️ **CHƯA thực hiện được** — xem Mục 20.3. Phần 19.4 (Detail UC-014) phát sinh sau đợt áp code Mục 20 nên cần tag riêng khi tới lượt, VD `pre-uc014-detail-route-270726`.

**Trạng thái chung**: chốt qua ảnh chụp code thật + xác nhận trực tiếp 27/07/2026 — **code Medical Staff ĐÃ ÁP 27/07/2026**, chi tiết Mục 20. Screen Spec vẫn CHƯA áp. Mục 19.1/19.2 là sửa lỗi hồi quy so với quyết định đã chốt trước đó (Mục 8.4/14.1), không phải quyết định mới; Mục 19.3 là sửa lỗi kiến trúc phát sinh từ chính quyết định gốc Mục 8.5 điểm 2.

---

## 20. Trạng thái áp dụng — ĐÃ ÁP CODE Mục 19 (Web, Medical Staff), 27/07/2026

### 20.1. Thay đổi theo file

| File | Thay đổi đã áp |
| --- | --- |
| `conduct-inspection/page.tsx` | State `openGroup: number \| null` → `groupIndex: number`; **chỉ nhóm đang xem được mount** (5 nhóm còn lại không render, khác hẳn accordion cũ chỉ đóng bằng state). Nhóm hiện tại bọc trong `Card` thay `CollapsiblePanel`; header là `Select` liệt kê đủ 6 nhóm kèm `(đã đánh giá/tổng)` từng nhóm, cho **nhảy không tuần tự**; dòng "Bước x/6 · n/m hạng mục đã đánh giá"; chân khối có cặp nút "← Quay lại" (mới) / "Tiếp theo · x/6 →", nhóm cuối hiện chữ nhắc thay vì nút. Thêm `groupProgress` đếm theo hạng mục **thực sự đã bấm** Đạt/Không đạt — nhảy nhóm KHÔNG tính là hoàn thành nhóm đó. `safeIndex` chặn tràn khi checklist động đổi số nhóm (BR-210). Luật đánh giá, gate "Hoàn thành phiên" (MSG-016/BR-207) không đổi |
| `roles.ts` | `href` nav "Kiểm tra an toàn bếp" → `/medical-staff/conduct-inspection/history`, kèm comment nêu đây là sửa hồi quy so với Mục 8.4 điểm 1 + Mục 14.1 |
| `lib/inspection-sheets.ts` (MỚI) | Bộ render Mẫu 1/2/3 tách khỏi route Caterers, dưới dạng **hàm sinh HTML thuần** (không React/role/layout): `INSPECTION_SHEET_CSS`, `printableForms()`, `blockedForms()`, `buildInspectionSheetsHtml()`, `openInspectionSheetsPrint()`. Quy tắc "chỉ in bước ĐÃ ĐẠT" (08/07/2026) chuyển vào đây làm nguồn duy nhất |
| `medical-staff/inspections/[id]/page.tsx` (MỚI) | Route Detail: tóm tắt 3 bước + nhiệt lõi + đồng ký → 4 bảng snapshot Mẫu 1 Phần I/II, Mẫu 2, Mẫu 3 → ảnh/GPS/ghi chú → khu "Đánh giá của Y tế khi xem lại" (chuyển nguyên vẹn từ dialog, vẫn KHÔNG ghi đè `overall`). Nút **"In hồ sơ chính thức"** gọi `openInspectionSheetsPrint()` mở cửa sổ in riêng — **không đi qua route nào của `service-caterers/*`**; disabled kèm tooltip nêu đúng lý do khi Caterers chưa chốt hoặc chưa bước nào Đạt. Hai `Alert` phân biệt rõ 2 tình huống: chưa chốt hồ sơ / chốt rồi nhưng một phần mẫu chưa đủ điều kiện in |
| `medical-staff/inspections/page.tsx` | Bỏ toàn bộ `Dialog` chi tiết cùng state `detail`/`reviewResult`/`reviewReason` và `saveMedicalReview`; nút "Chi tiết" và `rowHref` cùng trỏ `/medical-staff/inspections/[id]`. Gỡ import không còn dùng (`Dialog*`, `Table*`, `Textarea`, `toast`, `useUpdate`, `Camera`/`MapPin`/`Printer`/`FlaskConical`, helper `yn`/`passFail`) |
| `service-caterers/inspections/[id]/print/page.tsx` | Giữ nguyên URL, tham số `?form=`, thanh công cụ và cảnh báo; phần mẫu biểu đổi sang `dangerouslySetInnerHTML` từ `buildInspectionSheetsHtml()`. Không còn JSX bảng biểu riêng — một nguồn render duy nhất cho cả 2 role |

### 20.2. Ghi chú phạm vi

Đợt này **có sửa 1 file trong `service-caterers/*`**, khác thông lệ các đợt trước. Lý do: bảng 19.4 liệt đích danh file đó, và mục tiêu "một bản in duy nhất cho một phiên" chỉ đạt được nếu route Caterers cũng tiêu thụ hàm dùng chung — để nguyên JSX cũ ở đó nghĩa là vẫn tồn tại 2 bộ render song song, đúng thứ Mục 19.3 muốn tránh. Thay đổi giới hạn ở việc **đổi nguồn render**, không đụng URL, tham số, hay luồng nghiệp vụ của Caterers.

### 20.3. Việc CHƯA làm / cần lưu ý

1. **Git tag + commit riêng CHƯA tạo được**: shell của phiên vẫn không khởi động. Cần tạo `pre-uc014-wizard-fix-270726` thủ công tại commit ngay trước đợt này rồi điền vào bảng 19.4.
2. **`tsc --noEmit` CHƯA chạy** (cùng lý do). Rà thủ công: `conduct-inspection/page.tsx` gỡ import `CollapsiblePanel`, thêm `Select`/`ChevronLeft`, không còn tham chiếu `openGroup`/`gi`; `inspections/page.tsx` viết lại trọn nên không còn import thừa; `inspection-sheets.ts` chỉ phụ thuộc kiểu `InspectionSession`.
3. **Bản in phía Medical Staff dùng `window.open`** nên bị chặn nếu trình duyệt khoá popup — đã bắt trường hợp này và hiện toast hướng dẫn, thay vì im lặng không có gì xảy ra.
4. **Screen Spec chưa cập nhật**: `12_ConductFoodSafetyInspectionScreen.md`, `11_FoodInspectionRecords.md`.
5. **Phụ thuộc chéo còn hở** (nhắc lại): `review_medicalstaff_violations.md` Mục 9.2 (2-tick Biên bản Vi phạm) và 2 việc chờ đợt School Administration — dọn `pestControlValid` (Mục 18.2) và endpoint `/compliance-report` (Mục 16.3 điểm 3).

---

## 21. Trạng thái áp dụng — ĐÃ ÁP CODE Mục 19.4 (Web, Medical Staff), 27/07/2026

### 21.1. Kết quả VERIFY nghi vấn `KIN-2026-101` — là giả thuyết (b), đã sửa

Mục 19.4 nêu 2 khả năng và yêu cầu verify trước khi kết luận. **Xác định được bằng đọc code, không cần chạy UI** — nên đã làm ngay thay vì để treo:

- `conduct-inspection/page.tsx` gửi payload **đúng**: có `confirmStatus: "AWAITING_CATERERS_CONFIRM"` và `itemResults: ALL_ITEMS.map(...)` (khớp đúng nhận định trong Mục 19.4).
- `lib/api/mock-server.ts`, handler `POST /kitchen-inspections`, dựng `record` theo **danh sách field cố định** (whitelist) — và danh sách đó **không có** `confirmStatus`/`itemResults`. Khi Mục 8.4 thêm 2 field vào payload phía form, handler không được cập nhật theo.

→ **Giả thuyết (b): lỗi ở tầng lưu trữ mock.** Không phải dữ liệu test thủ công. Triệu chứng khớp hoàn toàn: mọi phiên tạo qua wizard đều rơi vào nhánh "Phiên cũ (trước cơ chế xác nhận)" + "Hạng mục đã đánh giá (0)", vì 2 field bị bỏ lặng lẽ — không lỗi, không cảnh báo.

Đã sửa: bổ sung đúng 2 field vào handler, **giữ nguyên kiểu whitelist** (không `...body`) để không nhận bừa field lạ. Mặc định `confirmStatus` là `AWAITING_CATERERS_CONFIRM` khi payload không gửi, đúng ngữ nghĩa "vừa hoàn thành, chờ bếp xác nhận".

*Bài học ghi lại*: mock-server dựng record theo whitelist nên **mỗi lần thêm field vào payload phải sửa handler tương ứng** — kiểu lỗi này không biểu hiện lúc submit, chỉ lộ ra ở màn đọc lại, và rất dễ bị chẩn đoán nhầm thành "màn đọc hiển thị sai".

### 21.2. Thay đổi theo file

| File | Thay đổi đã áp |
| --- | --- |
| `lib/api/mock-server.ts` | Handler `POST /kitchen-inspections` nhận thêm `confirmStatus` (mặc định `AWAITING_CATERERS_CONFIRM`) và `itemResults` (mặc định `[]`), kèm chú thích đầy đủ nguyên nhân/triệu chứng để không tái diễn |
| `components/app/kitchen-confirm-badge.tsx` (MỚI) | Một bảng ánh xạ trạng thái → 2 dạng hiển thị: `KitchenConfirmBadge` (badge ngắn cho cột bảng) và `kitchenConfirmSentence()`/`kitchenConfirmTone()` (câu đầy đủ + tông màu cho khối trạng thái ở Detail). Cùng nguồn nên không thể lệch — đúng yêu cầu "không tạo nhãn thứ hai" |
| `conduct-inspection/[id]/page.tsx` (MỚI) | Route Detail **view-only**: khối trạng thái xác nhận là `Alert` ngay đầu trang (tách hẳn khỏi kết quả Đạt/Không đạt); tóm tắt điểm/kết quả/khám SK NV bếp; **snapshot đầy đủ `itemResults`** kèm đếm đạt/không đạt; lý do + ảnh hạng mục Không đạt (BR-207); khối trạng thái biên bản chỉ đọc khi đã có tick. Nút "Lập biên bản sự việc" ở header, disabled kèm tooltip khi chưa `CONFIRMED` |
| `conduct-inspection/history/page.tsx` | Bỏ toàn bộ `Dialog` + state `detail`/`setDetail` + hàm `confirmBadge` cục bộ (chuyển sang module dùng chung); nút "Chi tiết" và `rowHref` cùng trỏ `/medical-staff/conduct-inspection/[id]`; gỡ import `Dialog*` không còn dùng |

### 21.3. Ghi chú thiết kế

Một phiên nay có **2 route tách bạch có chủ đích**: `/conduct-inspection/[id]` (xem lại, chỉ đọc) và `/conduct-inspection/[id]/record` (lập biên bản, có thao tác ghi + 2 tick xác nhận điện tử theo Mục 9). Đã ghi rõ trong docblock của cả hai để không bị gộp nhầm về sau.

### 21.4. Việc CHƯA làm / cần lưu ý

1. **Git tag + commit riêng CHƯA tạo được**: shell của phiên vẫn không khởi động. Mục 19.5 đề nghị tag riêng `pre-uc014-detail-route-270726` cho phần 19.4 — cần tạo thủ công.
2. **`tsc --noEmit` CHƯA chạy** (cùng lý do). Rà thủ công: `history/page.tsx` viết lại trọn nên không còn import thừa; route Detail chỉ dùng type `KitchenInspection`; module badge không phụ thuộc gì ngoài `Badge` + type.
3. **Chưa chạy thử tạo phiên mới qua UI thật** để xác nhận bug fix ở tầng mock — verify vừa rồi dựa trên đọc code (đủ để xác định nguyên nhân, nhưng chưa phải kiểm chứng chạy). Nên chạy lại một lượt khi môi trường khả dụng: tạo 1 phiên qua wizard → mở Detail → kiểm tra badge hiện "Chờ Caterers xác nhận" và snapshot hạng mục có đủ dòng.
4. **Screen Spec chưa cập nhật**: `12_ConductFoodSafetyInspectionScreen.md` cần bổ sung mô tả route Detail view-only (ngoài phần wizard đã ghi ở Mục 19.1).

---

## 22. Vòng rà soát thứ 11 (27/07/2026) — ĐẢO NGƯỢC Mục 9/12.1: "Biên bản sự việc" kiểm tra bếp quay lại PDF+ký tay+ảnh (theo cascade từ Violations)

**Bối cảnh**: Mục 9 đổi cơ chế biên bản sự việc kiểm tra bếp sang 2-tick điện tử với lý do duy nhất là "tái dùng đúng cơ chế BR-211" và BR-211 tự thân đã đổi (theo `review_medicalstaff_violations.md` Mục 9.2). Nay Mục 9.2 đó **đã bị đảo ngược** (xem `review_medicalstaff_violations.md` Mục 12 — phát hiện lỗi kiến trúc: 2 nút không thể cùng nằm 1 màn vì khác role/layout, và `caterersRepresentativeId` chỉ là "chọn tài khoản" chứ không phải Nhà thầu tự xác thực độc lập; quyết định cuối quay lại PDF+in+ký tay+chụp ảnh cho Biên bản Vi phạm).

**Quyết định**: vì lý do đổi duy nhất của Mục 9 (đồng bộ theo BR-211) nay không còn, "Biên bản sự việc" kiểm tra bếp **cũng quay lại** cơ chế PDF+in+ký tay+chụp ảnh — đúng bản trước Mục 9 (Mục 8.2 Quyết định 3/Mục 8.4 điểm 4, "tái dùng đúng cơ chế BR-211 gốc"). Lưu ý bổ sung: khác với Biên bản Vi phạm (chế tài hợp đồng, đối tượng ký là Medical Staff + đại diện Nhà thầu ngoài hệ thống), biên bản sự việc kiểm tra bếp vốn chỉ là xác nhận nội bộ giữa 2 role trong cùng hệ thống (Y tế lập, Caterers xác nhận đã đọc) — không có hệ quả chế tài trực tiếp; tuy nhiên do đây là bản ghi kèm theo hồ sơ kiểm tra ATTP có thể dùng làm bằng chứng khi có tranh chấp/thanh tra, giữ nguyên lý lẽ ưu tiên bằng chứng ký tay đã áp dụng bên Violations, tránh 2 quy ước ký khác nhau trong cùng hệ thống.

**Nội dung đảo ngược cụ thể (khôi phục lại đúng cơ chế Mục 8.2/8.4 trước khi bị Mục 9/12.1 thay thế)**:
- `conduct-inspection/[id]/record/page.tsx`: gỡ khối **"Xác nhận biên bản"** (2 ô xác nhận Y tế/Đại diện Nhà thầu + badge "Đã khoá — đủ 2 xác nhận"), khôi phục khối **"Bản in & bản ký"** — xuất PDF → in → tải ảnh/scan bản đã ký (khôi phục `uploadSigned()`/`removeSigned()`, danh sách file có preview tên/kích thước/thời điểm, cho phép nhiều tệp).
- `recordSignedFiles` bỏ `@deprecated`, quay lại **bắt buộc** làm gate khoá biên bản. `recordConfirmedByMedicalStaffAt`/`recordConfirmedByMedicalStaffBy`/`recordConfirmedByCaterersAt` (thêm ở Mục 12.1) đánh `@deprecated`, **không xoá** — cùng nguyên tắc giữ dữ liệu lịch sử đã áp dụng bên Violations (Mục 12 file đó).
- `recordPdfExportedAt`/`recordPdfVersion` quay lại vai trò **bắt buộc** (điều kiện trước khi tải bản ký), không còn "tùy chọn phụ".
- Bản in HTML (`buildInspectionRecordHtml`): 2 ô chữ ký quay lại "(Ký, ghi rõ họ tên)" thay vì in dấu vết xác nhận điện tử — vì nay lại cần ký tay thật trên bản giấy trước khi chụp/scan.
- Docblock ở `conduct-inspection/[id]/page.tsx` và `conduct-inspection/[id]/record/page.tsx` (Mục 21.3) nhắc "2 tick xác nhận điện tử theo Mục 9" — cập nhật lại thành "tải ảnh bản ký theo Mục 22" để không gây hiểu nhầm khi đọc lại sau này.
- Gate mở "Lập biên bản sự việc" giữ nguyên (chỉ khi phiên `CONFIRMED`) — không đổi, đây là điều kiện của UC-014 xác nhận bếp, không liên quan cơ chế ký biên bản.

### 22.1. Checklist đảo ngược — CHƯA áp code

| File | Thay đổi cần áp dụng | Trạng thái |
| --- | --- | --- |
| `conduct-inspection/[id]/record/page.tsx` | Gỡ khối "Xác nhận biên bản" (2 tick); khôi phục khối "Bản in & bản ký" (`uploadSigned()`/`removeSigned()`); gate quay lại `recordSignedFiles` | **ĐÃ ÁP CODE 27/07/2026** |
| `mock-data.ts` (`KitchenInspection`) | `recordConfirmedByMedicalStaffAt/By`, `recordConfirmedByCaterersAt` đánh `@deprecated` (giữ field); `recordSignedFiles` bỏ `@deprecated`, quay lại bắt buộc; `recordPdfExportedAt`/`recordPdfVersion` quay lại bắt buộc | **ĐÃ ÁP CODE 27/07/2026** |
| `buildInspectionRecordHtml` | 2 ô chữ ký quay lại "(Ký, ghi rõ họ tên)" | **ĐÃ ÁP CODE 27/07/2026** |
| Docblock `conduct-inspection/[id]/page.tsx`, `.../record/page.tsx` | Sửa chú thích "2 tick xác nhận điện tử theo Mục 9" → "tải ảnh bản ký theo Mục 22" | **ĐÃ ÁP CODE 27/07/2026** — kèm sửa cả khối hiển thị trạng thái biên bản ở `[id]/page.tsx`, xem Mục 23.2 |
| `12_ConductFoodSafetyInspectionScreen.md` | Mô tả bước khoá biên bản quay lại PDF+ký tay+ảnh | CHƯA áp (Screen Spec) |

**Git checkpoint**: cùng đợt với `review_medicalstaff_violations.md` Mục 12 (2 file đụng cùng 1 cơ chế, nên áp code cùng lúc) — tag gợi ý `pre-revert-violation-signing-270726` dùng chung.

**Trạng thái chung**: quyết định cascade từ `review_medicalstaff_violations.md` Mục 12 (27/07/2026) — **code ĐÃ ÁP 27/07/2026**, chi tiết Mục 23.

---

## 23. Trạng thái áp dụng — ĐÃ ÁP CODE Mục 22 (Web, Medical Staff), 27/07/2026

> ⚠️ **Đọc kèm Mục 12.1 và Mục 21**: dòng "Mục 9 — biên bản kiểm tra bếp 2 tick điện tử" ở Mục 12.1 ghi ĐÃ ÁP CODE — phần đó nay **đã bị đảo ngược** bởi chính Mục 22. Mục 21 (route Detail view-only cho UC-014) **không bị ảnh hưởng**, chỉ 1 dòng chú thích và 1 khối hiển thị trạng thái được cập nhật theo.

### 23.1. Thay đổi theo file

| File | Thay đổi đã áp |
| --- | --- |
| `mock-data.ts` (`KitchenInspection`) | 3 field `recordConfirmedBy*` đánh `@deprecated` (**giữ, không xoá** — cùng nguyên tắc đã áp bên Violations, để biên bản lập trong khoảng cơ chế 2-tick còn hiệu lực không mất giá trị lịch sử); `recordSignedFiles` bỏ `@deprecated`, quay lại làm gate khoá; `recordPdfExportedAt`/`recordPdfVersion` quay lại vai trò bắt buộc |
| `conduct-inspection/[id]/record/page.tsx` | Gỡ khối "Xác nhận biên bản" (2 ô tick) + `confirmByMedicalStaff()`/`confirmByCaterers()`; khôi phục khối **"Bản in & bản ký"**: "Xuất & in biên bản" → "Tải ảnh/scan bản đã ký" (`uploadSigned()`/`removeSigned()`, danh sách tệp có nút xoá). `recordLocked` đổi căn cứ sang `recordSignedFiles.length > 0`. Khối "Quy trình" ở cột phải viết lại 4 bước theo luồng ký tay. Thêm 1 dòng chỉ-đọc hiện dấu vết xác nhận điện tử nếu hồ sơ có |
| `buildInspectionRecordHtml` | 2 ô chữ ký quay lại **"(Ký, ghi rõ họ tên)"** — bản in này để in ra ký thật trước khi chụp/scan, nên không còn in dấu vết xác nhận điện tử |
| `conduct-inspection/[id]/page.tsx` | Docblock sửa theo Mục 22. Khối "Biên bản sự việc" (chỉ đọc) đổi điều kiện hiển thị và nội dung: đọc theo `recordPdfExportedAt`/`recordSignedFiles` thay vì 2 mốc tick — nếu để nguyên, màn Detail sẽ không bao giờ hiện trạng thái biên bản cho hồ sơ lập theo cơ chế mới |

### 23.2. Điểm phát sinh ngoài checklist

Checklist 22.1 chỉ nêu "sửa docblock" ở `conduct-inspection/[id]/page.tsx`. Thực tế file đó còn **một khối hiển thị trạng thái biên bản** đọc trực tiếp 2 mốc tick (`recordConfirmedByMedicalStaffAt`/`recordConfirmedByCaterersAt`) làm điều kiện render. Giữ nguyên thì mọi biên bản lập theo cơ chế mới (chỉ có bản in + bản ký) sẽ **không hiện gì** ở màn Detail. Đã đổi sang đọc `recordPdfExportedAt`/`recordSignedFiles`.

### 23.3. Việc CHƯA làm / cần lưu ý

1. **Git tag CHƯA tạo được**: shell của phiên vẫn không khởi động. Tag dùng chung với Violations Mục 12: `pre-revert-violation-signing-270726`.
2. **`tsc --noEmit` CHƯA chạy** (cùng lý do). Rà thủ công: `Check` gỡ khỏi import, `Camera`/`X`/`UploadedFile`/`mockUpload` thêm lại; `canConfirm` đổi tên thành `canExport` cùng mọi điểm dùng; `legacySignedFiles` thay bằng `signedFiles`.
3. **Gate "Lập biên bản sự việc" giữ nguyên** (chỉ khi phiên `CONFIRMED`) — đúng Mục 22, đây là điều kiện của UC-014, không liên quan cơ chế ký.
4. **Screen Spec chưa cập nhật**: `12_ConductFoodSafetyInspectionScreen.md`.
