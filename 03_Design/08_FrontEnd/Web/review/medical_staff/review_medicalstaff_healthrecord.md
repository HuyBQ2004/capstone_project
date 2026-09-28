# Review — Màn hình "Hồ sơ Sức khỏe & Dị ứng" (Health Record) — Medical Staff

**Mục đích**: ghi nhận các quyết định thiết kế lại cho cụm màn hình Duyệt khai báo sức khỏe (UC-077), Cập nhật hồ sơ sức khỏe (UC-094), và Yêu cầu bữa ăn đặc biệt (UC-078), phát sinh từ đợt review code thật (09/07/2026) — "Nhóm A" và "Nhóm B" trong đợt rà soát 14 ảnh đa module.

**Ngày biên soạn**: 09/07/2026.

**Nguồn đã đọc để biên soạn**:
- `02_Requirements/02_UseCases/MF03_Student_Parent_Management.md` — UC-051 (View List Student Health Records), UC-077 (Review Health Information), UC-078 (Review Special Meal Requests), UC-094 (Update Health Records), UC-146 (View Student Health History)
- `02_Requirements/00_BusinessRules/business_rules.md` — BR-172 (Bảo vệ toàn vẹn nhật ký — canonical, thay cho `BR-039` trong raw SRS, cùng pattern renumbering đã gặp ở UC-059→UC-168)
- `03_Design/04_ScreenFlow/05_Parent_Screen_Flow.md` — tham chiếu BR-063 (kiểm tra xung đột dị ứng, Workflow 9)
- Ảnh chụp code thật do người dùng cung cấp (09/07/2026): màn "Duyệt khai báo sức khỏe (UC-077)", màn "Yêu cầu bữa ăn đặc biệt" (đối chiếu pattern tab), modal "Cập nhật hồ sơ sức khỏe (UC-094)"

---

## 1. Bối cảnh 2 UC liên quan

| UC | Tên | Primary Actor | Mô tả (Report 3) |
| --- | --- | --- | --- |
| UC-051 | View List Student Health Records | Medical Staff | Tìm kiếm, truy xuất, bổ sung hồ sơ lâm sàng, thuốc, và lịch sử sức khỏe học sinh |
| UC-077 | Review Health Information | Medical Staff | Xác minh và duyệt các khai báo sức khỏe/dị ứng đang chờ xử lý |
| UC-094 | Update Health Records | Medical Staff | Bổ sung ghi chú, thêm chi tiết thuốc, hoặc sửa thông số lâm sàng trên hồ sơ sức khỏe học sinh |
| UC-146 | View Student Health History | Medical Staff | Xem/xuất lịch sử sức khỏe học sinh dạng timeline, read-only (BR-172) |

`UC-077` (màn Duyệt khai báo, Hình 1) và `UC-094` (modal Cập nhật hồ sơ, Hình 3) là 2 màn khác nhau nhưng dùng chung dữ liệu — quyết định dưới đây áp dụng riêng cho từng màn.

---

## 2. Quyết định đã chốt (09/07/2026) — Nhóm A

### 2.1. UC-077 — Thêm tab trạng thái + nút xem lại hồ sơ cho case đã duyệt

**Vấn đề**: màn "Duyệt khai báo sức khỏe" hiện hiển thị toàn bộ học sinh (đã duyệt lẫn chưa duyệt) trong 1 danh sách phẳng, chỉ có nút "Xem & duyệt" cho case `Chờ xử lý`; case `Đã xác minh` không có hành động nào để xem lại — trong khi màn "Yêu cầu bữa ăn đặc biệt" (UC-078) cùng nhóm đã có sẵn pattern tab (`Chờ duyệt`/`Đã duyệt`/`Tất cả`).

**Quyết định**:
1. Thêm tab **"Chờ xử lý" / "Đã duyệt"** cho UC-077, nhất quán với pattern đã dùng ở UC-078.
2. Với case đã duyệt, thay nút "Xem & duyệt" bằng nút **"Xem hồ sơ"** (thay vì ẩn hoàn toàn hành động) — cho phép Medical Staff xem lại bằng chứng/thông tin đã duyệt khi cần đối chiếu, không có quyền duyệt lại (tránh đảo ngược quyết định qua UI, cùng nguyên tắc với quyết định Nhóm B về khóa nút Reject sau khi Approve).

### 2.2. UC-094 — Bỏ khối "Lịch sử log đã xác minh" khỏi modal Cập nhật hồ sơ

**Vấn đề**: modal "Cập nhật hồ sơ sức khỏe" (UC-094) đang nhúng thêm 1 khối "Lịch sử log đã xác minh (read-only — BR-172)" ngay trong modal, trong khi màn cha (Hồ sơ sức khỏe chi tiết) đã có sẵn nút riêng **"Lịch sử sức khỏe (UC-146)"** dẫn tới đúng chức năng này (timeline đầy đủ, có thể export).

**Quyết định**: bỏ khối lịch sử khỏi modal UC-094 — tránh trùng lặp UI, giữ nguyên chức năng xem lịch sử qua nút UC-146 riêng ở màn cha.

### 2.3. UC-094 — Thu hẹp phạm vi field "Medications"

**Vấn đề nêu ra**: field "Medications" trong modal UC-094 bị cho là không cần thiết vì "hệ thống không quản lý thuốc". Đối chiếu lại: đây **là field có nguồn gốc SRS chính thức** — UC-051 (dòng 101) ghi "...append clinical records, **medications**, and health histories..."; UC-094 (dòng 211) ghi "...add **medication details**, or edit clinical parameters..." — không phải field tự phát sinh khi code.

**Phân tích**: SFMS là hệ thống **quản lý an toàn thực phẩm học đường**, không phải hồ sơ bệnh án (EMR) tổng quát. Nếu hiểu "Medications" theo nghĩa rộng (mọi loại thuốc học sinh dùng, không phân biệt lý do) thì đây là tính năng vượt phạm vi bài toán ATTP. Nhưng dữ liệu ví dụ đang dùng trong code ("EpiPen Jr. dự phòng tại phòng y tế") là **thuốc cấp cứu dị ứng**, gắn trực tiếp với 2 điểm cốt lõi của hệ thống:
- BR-063 (kiểm tra xung đột dị ứng, Workflow 9, `05_Parent_Screen_Flow.md`) — biết học sinh có sẵn thuốc cấp cứu giúp đánh giá đúng mức độ rủi ro khi phát hiện dị ứng.
- Workflow 11 bước E5 "Provide Medical Treatment" (`00_Business_Workflows.md`) — khi có sự cố ATTP thật, Medical Staff cần biết ngay học sinh có thuốc cấp cứu gì để xử lý kịp thời.

**Quyết định**: **giữ lại field**, nhưng đổi khung ý nghĩa từ "Medications" (chung chung) sang **"Thuốc cấp cứu liên quan dị ứng/sự cố"** — giữ nguyên cơ chế nhập liệu (text field), chỉ sửa label + placeholder để giới hạn đúng phạm vi: ghi nhận thuốc cấp cứu cần dùng khi có phản ứng dị ứng/sự cố ATTP, không phải nơi khai báo mọi loại thuốc học sinh đang dùng.

### 2.4. Rà soát song ngữ Anh-Việt lẫn lộn

**Vấn đề**: nhiều label trong modal UC-094 đang lẫn tiếng Anh ("Clinical Annotation mới", "Medications", "Emergency Instructions / Liên hệ khẩn cấp") giữa phần còn lại là tiếng Việt.

**Quyết định**: chuyển toàn bộ label trong modal UC-094 sang tiếng Việt, ví dụ: "Clinical Annotation mới" → "Ghi chú lâm sàng mới"; "Medications" → "Thuốc cấp cứu liên quan dị ứng/sự cố" (đã quyết ở 2.3); "Emergency Instructions" → "Hướng dẫn xử lý khẩn cấp". **Phạm vi**: đây mới là rà soát cho riêng modal UC-094 (Nhóm A) — việc rà soát toàn diện các màn khác (Investigation Workspace, Compliance Dashboard...) cần làm thành 1 đợt riêng, không nằm trong Nhóm A này.

### 2.5. UC-078 — Khóa nút Reject sau khi đã Approve (Nhóm B)

**Vấn đề**: modal "Xem xét yêu cầu" (UC-078, Yêu cầu bữa ăn đặc biệt) hiện vẫn hiển thị đủ 2 nút "Reject"/"Approve" ngay cả khi mở lại 1 yêu cầu **đã ở trạng thái `Đã duyệt`** — cho phép Medical Staff bấm Reject để đảo ngược 1 quyết định đã duyệt trước đó qua cùng 1 modal.

**Quyết định**: khi yêu cầu đã ở trạng thái `Đã duyệt` hoặc `Từ chối` (đã xử lý xong), modal chuyển sang **chế độ chỉ xem** — disable cả 2 nút Reject/Approve, chỉ còn nút "Đóng". Modal chỉ cho phép Reject/Approve khi yêu cầu đang ở trạng thái `Chờ xử lý`. Áp dụng cùng nguyên tắc đã dùng ở 2.1 (UC-077) — không đảo ngược quyết định duyệt/từ chối qua lại UI, tránh sai sót thao tác.

---

## 2b. Trạng thái áp dụng vào code (12/07/2026) — ĐÃ XONG toàn bộ Mục 2, `tsc --noEmit` sạch

- §2.1: `health-reviews/page.tsx` — thêm tab "Chờ xử lý"/"Đã duyệt" (mock-server bổ sung filter phủ định `not:<field>` để tab Chờ xử lý gồm cả PENDING lẫn NEEDS_INFO); case đã duyệt có nút "Xem hồ sơ" mở dialog chỉ xem (footer chỉ còn "Đóng", không duyệt lại).
- §2.2: `health-records/[id]/page.tsx` — bỏ khối "Lịch sử log đã xác minh" khỏi modal UC-094, thay bằng 1 dòng trỏ về nút "Lịch sử sức khỏe (UC-146)" ở màn cha.
- §2.3: label + placeholder field đổi thành "Thuốc cấp cứu liên quan dị ứng/sự cố" (cả trong modal lẫn phần hiển thị read-only), giữ free text.
- §2.4: Việt hóa label modal UC-094 ("Ghi chú lâm sàng mới", "Hướng dẫn xử lý khẩn cấp / Liên hệ khẩn cấp").
- §2.5: `special-meals/page.tsx` — yêu cầu đã Đã duyệt/Từ chối → modal chỉ xem (ẩn Reject/Approve, chỉ còn Đóng + ghi chú giải thích); nhãn nút dòng đổi "Xem xét"/"Xem hồ sơ" theo trạng thái.

## 3. Chưa xử lý trong đợt này

- Rà soát song ngữ ở các module khác (Incident, Compliance Dashboard...) — ghi nhận là việc cần làm, chưa thực hiện.
- Cấu trúc field "Thuốc cấp cứu" có cần chuyển từ free text sang structured (loại thuốc, liều dùng, vị trí lưu trữ) hay giữ nguyên free text — chưa có yêu cầu cụ thể, giữ nguyên free text cho tới khi có quyết định khác.

---

## 4. Vòng rà soát thứ 2 (26/07/2026) — tách trục lọc UC-077, bổ sung Bệnh lý nền vào Hồ sơ chi tiết, sửa lỗi gate BR-031

**Bối cảnh**: người dùng cung cấp 7 ảnh chụp code thật của cụm màn "Hồ sơ Sức khỏe & Dị ứng" phía Medical Staff (`health-reviews`, `health-records/[id]`, `chronic-conditions` phía Parent), đối chiếu với phía Parent (`05/06/07/08/09_*.md`, `00_TongHop_Parent.md`) theo đúng nguyên tắc "phụ huynh khai báo gì thì Y tế phải nhận đúng thông tin đó" (field phải map 1-1 hai chiều). Phát hiện cốt lõi: từ 22/07/2026 code đã âm thầm bổ sung tính năng **Khai báo Bệnh lý nền** (`ChronicConditionDeclaration`, resource `chronic-conditions`) theo đúng **Phương án A** của `02_Requirements/PhuongAn_KhaiBao_BenhLyNen_PhuHuynh.md` (15/07/2026, còn ở trạng thái **Draft đề xuất**, chưa được chốt chính thức vào BR/UC) — nghĩa là **code đã đi trước tài liệu**: 5 Screen Spec mà chính đề xuất đó liệt kê cần sửa (`Parent/07,08,09`, `MedicalStaff/02,05`) đều **chưa được cập nhật** (mục 5.3 của đề xuất, bước 2–5 của mục 8 chưa thực hiện).

Nguồn đã đọc thêm cho vòng này: `Parent/00_TongHop_Parent.md` (05/06/08/09), `MedicalStaff/00_TongHop_MedicalStaff.md` (02/03/04/05), `02_Requirements/PhuongAn_KhaiBao_BenhLyNen_PhuHuynh.md`, `business_rules.md` (BR-030, BR-031, BR-066, BR-082, BR-123, BR-172, BR-173), mã nguồn `medical-staff/health-reviews/page.tsx`, `medical-staff/health-records/[id]/page.tsx`, `medical-staff/special-meals/page.tsx`, `parent/chronic-conditions/page.tsx`, `parent/health/page.tsx`, `lib/mock-data.ts` (interface `Student`, `ChronicConditionDeclaration`).

### 4.1. Phát hiện — 2 trục lọc bị trộn vào 1 tab bar (UC-077)

Màn "Duyệt khai báo sức khỏe" (`health-reviews/page.tsx`) hiện có 3 tab: **"Chờ xử lý"/"Đã duyệt"** (lọc theo *trạng thái*, chỉ chứa dữ liệu Dị ứng) và **"Bệnh lý nền"** (lọc theo *loại dữ liệu*, gộp mọi trạng thái vào 1 danh sách phẳng, duyệt/yêu cầu bổ sung ngay trong dòng — không có Detail modal riêng, không có "Xem hồ sơ" cho case đã xác minh như Nhóm A §2.1 đã chốt cho Dị ứng). Hai trục lọc khác bản chất (trạng thái xử lý và loại khai báo) bị đặt cùng 1 tab bar, gây khó đoán hành vi và không nhất quán với khuôn mẫu List→Detail đã dùng cho UC-078 (`08_SpecialMealRequestList.md` → `07_ReviewSpecialMealRequestScreen.md`).

Đối chiếu phía Parent xác nhận: Parent hiện khai báo qua **3 entity/màn tách biệt**, không phải 2 như giả định ban đầu của người dùng — `AllergyDeclaration` (05/06, UC-084/093), `ChronicConditionDeclaration` (Phương án A, cùng UC-084/093 nhưng entity riêng, tách khỏi `student_health_records` append-only theo đúng lý do nêu ở mục 3 của đề xuất — BR-172), và `SpecialMealRequest` (10/11, UC-141/142, review ở UC-078). Riêng **Height/Weight/Blood Type** (`08_SubmitHealthInformation.md`/`09_UpdateHealthInformation.md` — cũng thuộc UC-084/093) **chưa được cài đặt ở bất kỳ đâu trong code** (không có form phía Parent, không có field trong `Student`/mock-data, không có màn duyệt phía Medical Staff) — đây là Screen Spec đã chốt chính thức (không phải Draft) nhưng có tỉ lệ hoàn thành 0%, ngược lại với Bệnh lý nền (chưa chốt tài liệu nhưng đã cài đặt đầy đủ 2 chiều Parent↔Medical Staff).

### 4.2. Quyết định đã xác nhận (26/07/2026, qua `AskUserQuestion` + mockup minh họa)

1. **Tách hàng đợi duyệt thành 2 module song song theo đúng bản chất từng loại khai báo, dùng chung 1 khuôn List (tab Chờ xử lý/Đã duyệt) → Detail (Sheet/Dialog xem đầy đủ field + Approve/"Yêu cầu bổ sung")**:
   - Module "Duyệt khai báo dị ứng" — giữ nguyên `health-reviews/page.tsx` hiện tại, **bỏ tab "Bệnh lý nền"** ra khỏi đây.
   - Module "Duyệt khai báo bệnh lý nền" — nâng cấp `ChronicTab` hiện tại (đang là danh sách phẳng + duyệt inline) thành đúng khuôn List(2 tab Chờ xử lý/Đã duyệt) → Detail Dialog, cùng cấu trúc panel với UC-077 (tóm tắt học sinh, badge nhóm bệnh + mức độ, danh sách tài liệu y tế có preview thật, nút Approve/"Yêu cầu bổ sung" + textarea lý do bắt buộc, case đã `VERIFIED` chuyển "Xem hồ sơ" chỉ đọc — áp đúng nguyên tắc Nhóm A §2.1 đã dùng cho Dị ứng).
   - Không nhân đôi điểm vào (sidebar/Dashboard): 2 module này **dùng chung 1 route/1 mục sidebar** "Duyệt khai báo sức khỏe" như hiện tại, phân biệt bằng 1 bộ chọn module (segmented control/tabs cấp ngoài: "Dị ứng" | "Bệnh lý nền"), tab Chờ xử lý/Đã duyệt nằm **bên trong** mỗi module — tức tách 2 trục lọc thành 2 tầng điều hướng riêng thay vì trộn chung 1 tab bar phẳng như hiện tại. `08_SpecialMealRequestList.md` (UC-078) giữ nguyên là màn/route độc lập như hiện có, không gộp vào hub này.
   - Lý do chọn hướng này thay vì gộp thẳng Bệnh lý nền vào bảng Dị ứng (badge phân biệt loại): 2 loại có field hoàn toàn khác nhau (Category dị ứng + Specific Name + Severity Mild/Moderate/Severe vs. Category bệnh lý + Tên/chi tiết + Severity Nhẹ/Trung bình/Nặng — thang đo severity khác tên gọi, khác BR chi tiết dù cùng viện dẫn BR-031) nên gộp bảng sẽ phải làm cột "Loại/Mức độ" đa hình, phức tạp hơn là tách theo đúng bản chất dữ liệu.

2. **Bổ sung khối "Bệnh lý nền" vào `04_StudentHealthRecordDetail.md`/`health-records/[id]/page.tsx`** — đây là hồ sơ **tổng hợp 1 học sinh xem 1 lần** (khác hàng đợi duyệt ở Quyết định 1), nên **gộp**, không tách: thêm 1 khối đọc "Bệnh lý nền đã khai báo" cạnh khối "Dị ứng & chế độ ăn" hiện có, cùng dạng hiển thị (badge severity + tên bệnh + ghi chú + tài liệu đính kèm có preview thật). Áp dụng trình bày dạng **Collapsible/Accordion** cho cả 3 khối (Dị ứng, Bệnh lý nền, Chế độ ăn đặc biệt) theo đúng đề xuất minh họa (Hình 3 người dùng cung cấp) — tái dùng component `CollapsiblePanel` đã có sẵn trong dự án (`collapsible-panel.tsx`, dùng cho Incident Management, xem `review_medicalstaff_design_pattern.md` Mục 12.8.1), không tạo component mới.

3. **Ghi nhận lỗi gate BR-031 ở modal Cập nhật hồ sơ (UC-094) vào báo cáo, kèm khuyến nghị sửa** — xem chi tiết Mục 4.4.4.

### 4.3. Phát hiện bổ sung — field-mapping, thiếu preview thật, modal quá hẹp

Đối chiếu field-by-field 2 chiều Parent ↔ Medical Staff cho cả 3 loại khai báo, xác nhận thêm các lỗi cụ thể sau (không suy đoán — trích trực tiếp từ code):

1. **Modal duyệt quá hẹp so với Screen Spec** — `02_ReviewHealthInformationScreen.md` và `07_ReviewSpecialMealRequestScreen.md` đều quy định layout Web là **`Sheet` bên phải** (panel rộng), nhưng `health-reviews/page.tsx` (dòng 326) và `special-meals/page.tsx` (dòng 195) đều dùng `<DialogContent>` **không có `max-w-*`** → rơi về kích thước Dialog mặc định (hẹp, giữa màn hình) — đúng như phản ánh trong ảnh chụp ("quá bé, cảm giác kẹt xỉ"). Cần tối thiểu thêm `max-w-2xl` (đã áp dụng tiền lệ này cho `27_ViolationRegistry.md`, xem `review_medicalstaff_incidentmanagement.md` Mục 12.6), hoặc đổi hẳn sang `Sheet` bên phải đúng spec gốc.

2. **Nút "Xem" tài liệu y tế đính kèm không mở file thật** — xác nhận ở cả 3 nơi, đều chỉ là mock, không có preview thật nào đang hoạt động:
   - `health-reviews/page.tsx` dòng 367: `toast.info(\`Mở preview ${d} (mock)\`)`.
   - `health-records/[id]/page.tsx` dòng 182: `toast.info(\`(Mock) Mở xem tài liệu: ${doc}\`)`.
   - `special-meals/page.tsx` dòng 219: `toast.info(\`(Mock) Mở xem: ${selected.documentName}\`)`.
   - Riêng `ChronicTab` (trong `health-reviews/page.tsx`) còn thiếu cả nút bấm — tài liệu chỉ hiển thị dạng chữ tĩnh "Tài liệu y tế: {tên file}", không có hành động nào để xem, kém hơn cả 2 nơi kia.
   Khuyến nghị: thay `toast.info` mock bằng mở file thật (tab mới hoặc `<iframe>`/viewer PDF), áp dụng đồng loạt cho cả 3+1 điểm trên khi dựng lại Detail modal của Bệnh lý nền theo Quyết định 1.

3. **`Student.specialDiet` (chuỗi đơn) và `SpecialMealRequest` (entity riêng, có trạng thái duyệt UC-078) là 2 nguồn dữ liệu chồng lấn cho cùng khái niệm "chế độ ăn đặc biệt"** — `04_StudentHealthRecordDetail` hiển thị thẳng `s.specialDiet` như dữ liệu đã chốt (không kèm trạng thái duyệt), trong khi việc duyệt thật sự diễn ra ở luồng `SpecialMealRequest` (UC-078) hoàn toàn tách biệt. Ghi nhận là điểm cần đối soát thêm (ngoài phạm vi sửa ngay của vòng này) — không có bằng chứng 2 nguồn này đồng bộ tự động với nhau trong code hiện tại.

4. **Lỗi gate BR-031 — modal Cập nhật hồ sơ (UC-094) tự động xác minh khi lưu, không qua gate tài liệu bắt buộc**: `health-records/[id]/page.tsx`, hàm `saveEdit()` (dòng 69–91) gọi `update.mutate({ ..., healthStatus: "VERIFIED" })` **vô điều kiện** mỗi lần bấm "Lưu", bất kể `healthStatus` trước đó là gì. Hệ quả: Y tế chỉ cần nhập 1 ghi chú lâm sàng bất kỳ (annotation) là toàn bộ hồ sơ dị ứng của học sinh chuyển "Đã xác minh" — **kể cả khi dị ứng mức Severe vẫn chưa có tài liệu y tế đính kèm** (bỏ qua đúng gate `docsValid()` mà chính màn "Duyệt khai báo sức khỏe" (UC-077) đang dùng để khóa nút Approve, dòng 46–49 và 419–421 cùng file `health-reviews/page.tsx`). Đây là 2 đường ghi cùng 1 trạng thái (`healthStatus`) nhưng chỉ 1 đường có gate — cùng dạng lỗi "gate bị hở" đã từng ghi nhận ở Incident Management (`review_medicalstaff_design_pattern.md` Mục 10, P0-1/P0-2).
   **Khuyến nghị sửa**: bỏ việc set `healthStatus: "VERIFIED"` vô điều kiện trong `saveEdit()` của UC-094 — cập nhật ghi chú lâm sàng/thuốc cấp cứu/hướng dẫn khẩn cấp không nên tự động đổi trạng thái xác minh của hồ sơ dị ứng; trạng thái xác minh chỉ nên đổi qua đúng gate Approve/Reject của UC-077 (đã có `docsValid()`).

5. **`Student.allergies`/`allergySeverity` (field phẳng, sửa được qua UC-094) và `AllergyDeclaration[]` (entity riêng, mỗi bản ghi có `status` riêng, sửa qua UC-077) là 2 nguồn dữ liệu song song cho cùng 1 khái niệm dị ứng** — field "Dị ứng (phân tách dấu phẩy)" trong modal UC-094 (dòng 253–259) ghi thẳng vào `Student.allergies`, **không** đụng tới `AllergyDeclaration[]` mà UC-077 đang dùng làm nguồn duyệt chính thức (biến `declData`/`updateDecl`, dòng 173–185 của `health-reviews/page.tsx`) — đúng rủi ro mà comment trong chính code đã tự cảnh báo ("Xác minh phải chạm ĐÚNG TỪNG KHAI BÁO, không chỉ cờ `healthStatus` ở mức học sinh"). Ghi nhận, chưa đề xuất hướng sửa cụ thể trong vòng này (cần quyết định kiến trúc: hoặc field này ở UC-094 chuyển thành chỉ đọc/trỏ sang UC-077, hoặc route ghi qua đúng `AllergyDeclaration`) — xem câu hỏi mở Mục 4.5.

### 4.4. Nợ tài liệu (documentation debt) cần đóng lại

- `02_Requirements/PhuongAn_KhaiBao_BenhLyNen_PhuHuynh.md` vẫn ở trạng thái **Draft đề xuất** dù code đã cài đặt đầy đủ 2 chiều (Parent khai báo + Medical Staff duyệt) từ 22/07/2026 — cần đội dự án chính thức chốt Phương án A (bước 1–2 của Mục 8 trong chính đề xuất đó) rồi cập nhật `business_rules.md` (diễn giải rõ BR-030/BR-031 bao gồm bệnh lý nền), `UC-084/093/094/050/104/145`, và `traceability_matrix.md` — nếu không, hồ sơ dự án tiếp tục ở trạng thái "code có, tài liệu chính thức không có" như đã từng ghi nhận cho Announcements (`review_screen_parent_and_teacher.md` Mục 5.1).
- 5 Screen Spec đề xuất liệt kê ở Mục 5.3 của tài liệu trên (`Parent/07,08,09`, `MedicalStaff/02,05`) cần viết lại — nhưng theo Quyết định 1 (Mục 4.2), phần "MedicalStaff/02" **không nên** thêm khu vực Bệnh lý nền vào chung `02_ReviewHealthInformationScreen.md` như đề xuất gốc dự kiến, mà nên tách thành 1 file Screen Spec mới (VD `MedicalStaff/34_ReviewChronicConditionScreen.md`) đúng theo quyết định tách module — đây là 1 điểm **lệch có chủ đích** so với đề xuất gốc, cần ghi chú rõ lý do (nhất quán với khuôn UC-078) khi cập nhật tài liệu.
- `Parent/08_SubmitHealthInformation.md`/`09_UpdateHealthInformation.md` (Height/Weight/Blood Type) là Screen Spec **đã chốt chính thức**, không phải Draft, nhưng tỉ lệ cài đặt là 0% — cần đội dự án xác nhận đây là việc **chưa làm tới** (backlog) hay **chủ động bỏ** (nếu bỏ, cần cập nhật lại Screen Spec/trạng thái thay vì để im lặng trôi khỏi code).

### 4.5. Câu hỏi còn mở — ĐÃ ĐÓNG (26/07/2026, vòng xác nhận thứ 2, qua `AskUserQuestion`)

1. ~~Vị trí điều hướng bộ chọn module~~ → chốt **segmented control đầu trang**, giữ nguyên 1 route/1 mục Sidebar hiện tại (xem Mục 4.6, điểm 1).
2. ~~Hướng sửa dual-source dị ứng~~ → chốt **khóa field Dị ứng thành chỉ đọc trong UC-094** (xem Mục 4.6, điểm 2).
3. ~~`Student.specialDiet` vs `SpecialMealRequest`~~ → không để riêng nữa, đã quyết trong vòng này (xem Mục 4.6, điểm 4).
4. ~~Height/Weight/Blood Type~~ → đội dự án xác nhận **bỏ hẳn**, không phải vấn đề ưu tiên (xem Mục 4.6, điểm 3).

### 4.6. Quyết định bổ sung (26/07/2026, vòng xác nhận thứ 2)

1. **Bộ chọn module — segmented control ở đầu trang `health-reviews`**, không tách route/Sidebar riêng. Giữ nguyên 1 điểm vào Sidebar + 1 shortcut card Dashboard ("Student Allergy Review") như hiện có — bên trong trang mới rẽ nhánh theo module (Dị ứng | Bệnh lý nền), mỗi module có tab Chờ xử lý/Đã duyệt riêng. Không cần sửa cấu trúc Sidebar 5 nhóm hay `00_TongHop_MedicalStaff.md` mục Dashboard.

2. **Khóa field "Dị ứng" thành chỉ đọc trong modal Cập nhật hồ sơ (UC-094)** — bỏ hẳn `Input` "Dị ứng (phân tách dấu phẩy)" (đang sửa được, ghi thẳng `Student.allergies`) khỏi form; thay bằng 1 dòng hiển thị chỉ đọc (danh sách dị ứng hiện tại + badge mức độ) kèm link "Xem/duyệt tại module Dị ứng" trỏ sang đúng `health-reviews`. Đảm bảo **chỉ 1 nơi ghi dữ liệu dị ứng** (`AllergyDeclaration` qua UC-077), UC-094 không còn đường ghi song song nào vào `Student.allergies`.

3. **Bỏ hẳn Height/Weight/Blood Type — xác nhận theo quyết định của đội dự án**, không phải vấn đề ưu tiên hay backlog. Đối chiếu `MF03_Student_Parent_Management.md` (UC-084/UC-093 gốc): Description/Postcondition **không có mô tả literal "Height/Weight/Blood Type"** (chỉ nói "allergen details, severity, dietary restrictions, required medical documentation") — trường này chỉ xuất hiện ở tầng Screen Spec (`08/09_*.md`, trích Report 3.md dòng 6013–6030, chưa đối chiếu trực tiếp dòng này) và ở đề xuất Bệnh lý nền. Quyết định bỏ **không mâu thuẫn** với nội dung UC gốc đã đọc được — an toàn để loại bỏ mà không cần sửa `business_rules.md`/UC.

4. **`Student.specialDiet` (field rời) — bỏ, chỉ dùng `SpecialMealRequest` làm nguồn duy nhất**, áp dụng đúng nguyên tắc "1 nguồn ghi cho 1 loại khai báo có luồng duyệt riêng" đã dùng cho Dị ứng (Quyết định 2) và Bệnh lý nền (Mục 4.2, Quyết định 1). Cụ thể:
   - `04_StudentHealthRecordDetail`/`health-records/[id]/page.tsx`, khối "Chế độ ăn đặc biệt": đọc trực tiếp từ `SpecialMealRequest` lọc theo `studentId`, lấy bản ghi **đã duyệt gần nhất** (nếu có) thay vì field `s.specialDiet`; không có bản ghi đã duyệt nào → hiển thị "Không có" (đồng nhất với empty-state của khối Dị ứng/Bệnh lý nền).
   - Modal Cập nhật hồ sơ (UC-094): bỏ `Select` "Chế độ ăn đặc biệt (BR-066)" (đang sửa được) khỏi form — cùng lý do và cùng nguyên tắc với điểm 2 (chỉ duyệt qua đúng UC-078).
   - Xóa field `specialDiet` khỏi interface `Student` (`mock-data.ts`); các nơi khác đang đọc `s.specialDiet` (VD cột "Dị ứng khai báo"/context ở `health-reviews/page.tsx`, dòng 352: `Chế độ ăn: ${selected.specialDiet}`) cần đổi sang đọc từ `SpecialMealRequest` tương ứng.
   - Hệ quả: sau thay đổi này, **toàn bộ 3 loại khai báo** (Dị ứng, Bệnh lý nền, Chế độ ăn đặc biệt) đều tuân thủ đúng 1 nguyên tắc thống nhất — UC-094 (Cập nhật hồ sơ) chỉ còn sửa được Ghi chú lâm sàng mới/Thuốc cấp cứu liên quan dị ứng-sự cố/Hướng dẫn xử lý khẩn cấp; mọi field thuộc về 1 luồng khai báo có gate duyệt riêng (BR-031/BR-066) đều chỉ đọc tại đây, trỏ sang đúng màn duyệt của nó.

### 4.7. Checklist thay đổi cần áp dụng theo file (CHƯA áp code, chỉ ghi nhận — chờ lượt áp riêng)

| File | Thay đổi cần áp dụng |
| --- | --- |
| `health-reviews/page.tsx` | Bỏ tab "Bệnh lý nền" khỏi tab bar Chờ xử lý/Đã duyệt hiện tại; thêm **segmented control đầu trang** (Dị ứng \| Bệnh lý nền), giữ nguyên 1 route (Mục 4.6 điểm 1); mở rộng `DialogContent` (Approve/Reject Dị ứng) thêm `max-w-2xl`; sửa dòng 352 (`Chế độ ăn: ${selected.specialDiet}`) sang đọc từ `SpecialMealRequest` đã duyệt gần nhất (Mục 4.6 điểm 4) |
| `medical-staff/chronic-condition-reviews/page.tsx` (mới, hoặc route con trong `health-reviews`) | Dựng module Bệnh lý nền đúng khuôn List (2 tab Chờ xử lý/Đã duyệt) → Detail Dialog (field: nhóm bệnh, tên/chi tiết, mức độ, ghi chú, tài liệu có preview thật, Approve/Yêu cầu bổ sung + lý do bắt buộc, case VERIFIED → "Xem hồ sơ" chỉ đọc) |
| `health-records/[id]/page.tsx` | Thêm khối "Bệnh lý nền đã khai báo" (đọc) cạnh khối "Dị ứng & chế độ ăn"; chuyển 3 khối sang `CollapsiblePanel` dùng chung; sửa `saveEdit()` — bỏ set `healthStatus: "VERIFIED"` vô điều kiện; thay nút "Xem" tài liệu (mock) bằng preview thật; **modal UC-094**: bỏ hẳn `Input` "Dị ứng (phân tách dấu phẩy)" và `Select` "Chế độ ăn đặc biệt (BR-066)" — cả 2 đổi thành hiển thị chỉ đọc + link trỏ sang đúng màn duyệt (Mục 4.6 điểm 2, 4); khối "Chế độ ăn đặc biệt" đọc từ `SpecialMealRequest` đã duyệt gần nhất thay vì `s.specialDiet` |
| `special-meals/page.tsx` | Mở rộng `DialogContent` (Xem xét yêu cầu, UC-078) thêm `max-w-2xl`; thay nút "Xem" tài liệu minh chứng (mock) bằng preview thật |
| `mock-data.ts` | Xóa field `specialDiet` khỏi interface `Student` (Mục 4.6 điểm 4); rà soát mọi nơi đọc `s.specialDiet` để chuyển sang `SpecialMealRequest` |
| `04_StudentHealthRecordDetail.md` | Thêm field "Bệnh lý nền đã khai báo" (đọc); sửa field "Chế độ ăn đặc biệt" thành nguồn `SpecialMealRequest` (không còn field rời trên Student); ghi chú layout Collapsible 3 khối |
| `05_UpdateHealthRecordScreen.md` | Bỏ 2 field "Dị ứng (chỉnh sửa)"/"Chế độ ăn đặc biệt (chỉnh sửa)" nếu đang liệt kê — UC-094 chỉ còn Clinical Annotation/Medications/Emergency Instructions là sửa được (Mục 4.6 điểm 2, 4) |
| `MedicalStaff/34_ReviewChronicConditionScreen.md` (mới) | Viết Screen Spec cho module Bệnh lý nền, đúng khuôn `02_ReviewHealthInformationScreen.md` nhưng field riêng (nhóm bệnh, mức độ Nhẹ/Trung bình/Nặng) |
| `02_ReviewHealthInformationScreen.md` | Ghi chú phạm vi chỉ áp dụng Dị ứng (không gộp Bệnh lý nền), dẫn chiếu sang file mới ở trên |
| `Parent/08_SubmitHealthInformation.md`, `09_UpdateHealthInformation.md` | Đánh dấu **Deprecated/Removed** — Height/Weight/Blood Type bỏ hẳn theo quyết định đội dự án (Mục 4.6 điểm 3), không triển khai |
| `Parent/07_StudentHealthInformation.md` | Bỏ field #2 "Height/Weight/Blood Type/BMI Card" (metric card grid) và nút "Manage Health Details" (trỏ tới 08/09 đã deprecate) |
| `Parent/00_TongHop_Parent.md` | Cập nhật lại theo 2 thay đổi trên (bỏ 08/09, sửa 07) khi tái tạo file gộp |
| `02_Requirements/PhuongAn_KhaiBao_BenhLyNen_PhuHuynh.md` | Cập nhật trạng thái từ "Draft đề xuất" → đã cài đặt 1 phần (Phương án A); bổ sung ghi chú lệch so với Mục 5.3 gốc (tách file Screen Spec riêng thay vì gộp vào 02/05); ghi chú Height/Weight/Blood Type (nhắc tới ở UC-084/093 liên quan) nay đã bỏ theo quyết định riêng |
| `business_rules.md`, `traceability_matrix.md` | Cập nhật diễn giải BR-030/031 bao gồm bệnh lý nền; cập nhật UC-084/093/094/050/104/145 theo Mục 8 của đề xuất; ghi chú loại bỏ Height/Weight/Blood Type khỏi phạm vi UC-084/093 nếu trước đó có đối chiếu |

**Trạng thái**: Mục 4 nay đã **chốt đầy đủ hướng thiết kế** (qua 2 vòng `AskUserQuestion`, 26/07/2026) — ~~chưa sửa code~~ → **ĐÃ ÁP CODE 27/07/2026, xem Mục 4.8**; phần Screen Spec + tài liệu SRS (`02`/`04`/`05`/`34` mới, `Parent/07/08/09`, `PhuongAn_KhaiBao_BenhLyNen_PhuHuynh.md`, `business_rules.md`, `traceability_matrix.md`) vẫn CHƯA áp, chờ lượt docs riêng.

### 4.8. Trạng thái áp dụng CODE — 27/07/2026

Phạm vi đợt này: **chỉ code Web phía Medical Staff** (các dòng "Screen Spec"/"SRS" trong bảng 4.7 để lượt docs riêng, đúng quy ước các module trước).

| Quyết định | File code | Chi tiết đã áp |
| --- | --- | --- |
| Mục 4.2 QĐ 1 + 4.6 điểm 1 | `health-reviews/page.tsx` | Tách 2 trục lọc thành 2 TẦNG: segmented control đầu trang (**Dị ứng \| Bệnh lý nền**) — giữ nguyên 1 route/1 mục Sidebar; tab Chờ xử lý/Đã duyệt nằm **bên trong** mỗi module (tab "Bệnh lý nền" phẳng cũ đã bỏ khỏi tab bar trạng thái) |
| Mục 4.2 QĐ 1 (module mới) | `health-reviews/page.tsx` — `ChronicModule` (thay `ChronicTab`) | Nâng từ danh sách phẳng + duyệt inline lên đúng khuôn List (2 tab, có badge đếm) → **Detail Dialog** cùng cấu trúc panel với Dị ứng: tóm tắt học sinh, badge nhóm bệnh + mức độ, tài liệu có nút Xem (preview thật), Xác minh/Yêu cầu bổ sung + lý do bắt buộc, gate BR-031 (mức Nặng phải có tài liệu), case `VERIFIED` → "Xem hồ sơ" chỉ đọc |
| Mục 4.2 QĐ 2 | `health-records/[id]/page.tsx` | Thêm khối **"Bệnh lý nền đã khai báo"** (đọc, nguồn `chronic-conditions`, có preview tài liệu); 3 khối khai báo (Dị ứng · Bệnh lý nền · Chế độ ăn đặc biệt) chuyển sang `CollapsiblePanel` **dùng chung** (không tạo component mới) |
| Mục 4.3 điểm 1 | `health-reviews/page.tsx`, `special-meals/page.tsx` | 2 Dialog duyệt bổ sung `max-w-2xl` + `max-h-[85vh] overflow-y-auto` (spec gốc là Sheet bên phải; mock giữ Dialog nhưng đủ rộng) |
| Mục 4.3 điểm 2 | `lib/health-docs.ts` (mới), 4 điểm gọi | `openDocPreview()` mở tài liệu THẬT ở tab riêng (mock viewer có tên file + khung nội dung) — thay toàn bộ `toast.info("(mock)…")` ở duyệt Dị ứng, duyệt Bệnh lý nền (trước đây còn không có nút), Hồ sơ chi tiết, Yêu cầu bữa ăn đặc biệt |
| Mục 4.3 điểm 4 (lỗi gate BR-031) | `health-records/[id]/page.tsx` | `saveEdit()` **bỏ hẳn** `healthStatus: "VERIFIED"` vô điều kiện — cập nhật ghi chú lâm sàng không còn tự xác minh hồ sơ dị ứng; trạng thái xác minh chỉ đổi qua gate Approve/Reject của UC-077 (nơi có `docsValid()`) |
| Mục 4.6 điểm 2 | `health-records/[id]/page.tsx` | Modal UC-094 **bỏ ô nhập "Dị ứng (phân tách dấu phẩy)"** → thay bằng dòng chỉ đọc (badge dị ứng + mức độ) kèm link "Xem/duyệt tại module Dị ứng"; không còn đường ghi song song vào `Student.allergies` |
| Mục 4.6 điểm 4 | `health-records/[id]/page.tsx`, `health-records/page.tsx`, `health-reviews/page.tsx`, `dashboard/page.tsx`, `lib/health-docs.ts`, `mock-data.ts` | Modal UC-094 **bỏ `Select` "Chế độ ăn đặc biệt"**; mọi nơi phía Medical Staff đọc chế độ ăn qua `latestApprovedMeal()` (bản ghi `SpecialMealRequest` **đã duyệt gần nhất**) thay `Student.specialDiet`; field `Student.specialDiet` đánh dấu `@deprecated` **thay vì xoá cứng** — vì còn Parent/School Admin/Service Caterers và các helper thực đơn đọc nó, xoá ngay sẽ phá module ngoài phạm vi đợt này (ghi rõ trong JSDoc, chờ sweep riêng của từng actor) |

Kết quả: UC-094 nay chỉ còn sửa được **Ghi chú lâm sàng · Thuốc cấp cứu · Hướng dẫn khẩn cấp** — đúng nguyên tắc "1 nguồn ghi cho 1 loại khai báo có luồng duyệt riêng" (Dị ứng → UC-077, Bệnh lý nền → module mới, Chế độ ăn → UC-078).

**Lưu ý kiểm thử**: đợt này **chưa chạy được `tsc --noEmit`** (môi trường sandbox Linux không khởi động được tại thời điểm áp code) — đã rà thủ công import/tham chiếu/cân bằng JSX của 5 file đã sửa; cần chạy lại type-check khi môi trường khả dụng trước khi coi là hoàn tất kiểm thử.
