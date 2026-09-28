# 05_ScreenSpecifications — Cấu trúc thư mục & Kế hoạch chia việc cho 5 thành viên (Nam, Thảo, Minh, Mạnh, Huy — dùng Claude)

**Ngày lập**: 01/07/2026 (cập nhật: gắn tên thật + đối chiếu RACI từ Report 1/Report 2)
**Trạng thái**: Đề xuất — cần 5 thành viên xác nhận trước khi bắt đầu viết đồng loạt
**Nguồn**: `00_Foundation/Naming_Convention.md` §2 (quy ước bắt buộc), `00_UI_Design_Documentation_Plan.md` §4.5 + §6.4 (mục đích, nội dung, template), `04_ScreenFlow/14_Screen_Authorization_Matrix.md` (danh sách màn hình + actor + nền tảng), `Report 1.md` §3.1.2 (danh sách nhóm), `Report 2.md` §3.4 (RACI Matrix — dùng để gán việc theo đúng chuyên môn đã khai báo chính thức)

### Danh sách nhóm (theo `Report 1.md` Table 3‑1) & chuyên môn theo RACI (`Report 2.md` Table 3‑2)

| Tên | Vai trò (Report 1) | Chuyên môn chính theo RACI (A/R trong Report 2) |
| --- | --- | --- |
| Nguyễn Văn Nam | Leader | Requirement/SRS/Use Case (A), System Architecture, Auth, Project Setup, CI/CD (A/R) — phụ trách chung + kiến trúc/auth |
| Nguyễn Phương Thảo | Member | Project Management Plan (A/R), **Test Planning/Unit/Integration/System/UAT/Regression Testing, Defect Tracking (A/R)** — QA/Test |
| Trần Tuấn Minh | Member | **UI/UX Wireframes, Screen Flow Design, Design Mockups, Frontend Development, Flutter Mobile Development, Role-based UI Rendering (A/R)** — UI/UX + Frontend + Mobile |
| Nguyễn Đức Mạnh | Member | Database Design/ERD/Sequence Diagram, Backend Development, Database Integration, API Development, Authentication (R) — Backend/Database |
| Bùi Quang Huy | Member | Requirement gathering, Use Case Specification/Diagram (R), Test Documentation (R), User Guide/Final Documentation (A/R) — BA/Requirements/Documentation |

**Lưu ý quan trọng**: theo RACI gốc, việc **UI/UX Wireframes & Screen Flow Design chỉ chính thức gán A/R cho 1 người (Minh)**, 4 người còn lại chỉ ở mức C/I cho nhóm việc này. Khối lượng ~329 file Screen Specification là quá lớn cho 1 người làm trong khung thời gian đồ án, nên mục 2 dưới đây đề xuất **chia cả 5 người cùng viết** (ngoài phạm vi RACI gốc, cần cả nhóm đồng ý coi đây là việc chung), nhưng **gán theo actor có domain gần chuyên môn RACI của mỗi người nhất** để giữ chất lượng, và **giữ Minh + Thảo ở vai trò gần với RACI thật của họ nhất** (Minh vẫn là người ôm màn hình UI phức tạp nhất; Thảo đảm nhận thêm vai trò rà soát/QA cuối cùng, đúng đúng chuyên môn Testing/Defect Tracking của Thảo).

---

## 1. Cấu trúc thư mục (đã được `Naming_Convention.md` chốt — không tự đổi khác)

```
05_ScreenSpecifications/
├── 00_Team_Work_Assignment_Plan.md   ← file này
├── Web/
│   ├── SystemAdmin/           19 màn *(sửa 2026-07-01 từ 20 → 19, gỡ "Campus Config" — hệ thống không quản lý campus)*
│   ├── SchoolAdministration/  47 màn (38 gốc + 5 Academic Year + 4 Violation)
│   ├── ServiceCaterers/       37 màn (35 gốc + 2 Violation)
│   ├── MedicalStaff/          24 màn (20 gốc + 4 Violation)
│   ├── Parent/                18 màn
│   ├── RawFoodSupplier/       26 màn
│   ├── Teacher/               13 màn
│   └── GuestCommon/            9 màn
└── Mobile/                    (KHÔNG có SystemAdmin/SchoolAdministration — theo ADR-001, 2 actor này Web-only)
    ├── ServiceCaterers/       37 màn
    ├── MedicalStaff/          24 màn
    ├── Parent/                18 màn
    ├── RawFoodSupplier/       26 màn
    ├── Teacher/                13 màn
    └── GuestCommon/            9 màn
```

**Quy tắc đặt tên file** (bắt buộc theo `Naming_Convention.md` §2):
`<SốThứTự2Chữ số>_<TênMànHình_PascalCase>.md`, ví dụ `Web/ServiceCaterers/15_ThreeStepInspectionStep1.md`.
Bản Web và Mobile của **cùng 1 màn hình** phải **trùng số thứ tự + trùng tên file**, chỉ khác nhánh `Web/`/`Mobile/` — để dò cặp tương ứng nhanh khi maintain song song. Số thứ tự đánh theo đúng **thứ tự xuất hiện trong `14_Screen_Authorization_Matrix.md`** ở khối tương ứng của actor đó (khối 2–10), để không ai đánh số trùng/lệch nhau.

**Không tạo file cho actor không có cột `x`** — tra đúng theo `14_Screen_Authorization_Matrix.md`, không suy đoán.

### 1b. File tổng hợp (rollup) — được gộp nhiều bảng vào 1 file khi xuất, nhưng không thay thế file gốc

File 1-màn-1-file ở trên vẫn là **nguồn thật (source of truth)** — bắt buộc giữ vì `06_Wireframes/` sinh 1-1 theo từng file, và tách theo actor để 5 người không đụng file nhau. Nhưng **được phép** có thêm 1 file gộp mỗi actor để đọc nhanh/paste vào chương thiết kế của báo cáo tốt nghiệp:

```
Web/SchoolAdministration/00_TongHop_SchoolAdministration.md   ← nối toàn bộ 49 bảng theo đúng thứ tự STT
```

Quy tắc: file tổng hợp là **bản dẫn xuất (derived)** — ghi rõ dòng đầu *"File này là bản gộp để đọc nhanh/xuất báo cáo — nguồn thật nằm ở từng file lẻ trong cùng thư mục, sửa nội dung ở file lẻ rồi tái tạo lại file này, không sửa tay trực tiếp ở đây"*. Đánh số `00_` để luôn nằm đầu danh sách file trong thư mục. Có thể nhờ Claude tạo cả N file lẻ + 1 file tổng hợp trong cùng 1 lượt batch (xem mục 4b).

---

## 2. Chia việc cho 5 thành viên

Chia theo **actor** (ranh giới thư mục tự nhiên → mỗi người sửa 1 nhánh thư mục riêng, không đụng file của nhau, dễ review/merge). Cân bằng theo **tổng số file phải viết** (1 màn Web-only = 1 file, 1 màn Web+Mobile = 2 file), đồng thời ưu tiên gán actor có domain gần nhất với chuyên môn RACI của từng người (xem bảng RACI ở trên).

| # | Thành viên | Actor | Số màn | Số file (Web+Mobile) | Lý do gán (RACI + cân bằng khối lượng) |
| --- | --- | --- | --- | --- | --- |
| **1** | **Trần Tuấn Minh** | Service Caterers | 37 | **74** | RACI: chủ (A/R) UI/UX + Frontend + Mobile — giao domain UI phức tạp nhất (3-step inspection GPS/camera, inventory) đúng người có trách nhiệm chính về UI/UX toàn hệ thống |
| **2** | **Bùi Quang Huy** | Medical Staff + Guest & Common | 27 + 9 = 36 | **72** | RACI: chủ (R) Requirement gathering + Use Case Spec — Medical Staff có nhiều UC mới nhất (Violation Management UC-179/180) cần bám UC/BR chặt, đúng thế mạnh BA của Huy; làm Guest & Common trước để chốt mẫu chuẩn cho cả nhóm |
| **3** | **Nguyễn Đức Mạnh** | Raw Food Supplier | 26 | **52** | RACI: chủ (R) Database/Backend/API — Raw Food Supplier nặng về entity/document (batch, certificate, compliance) cần mô tả field/kiểu dữ liệu chính xác, khớp thế mạnh backend/DB của Mạnh |
| **4** | **Nguyễn Văn Nam** | School Administration + System Admin | 49 + 19 = 68 | **68** *(sửa 2026-07-01 từ 69 → 68, gỡ "Campus Config")* | RACI: chủ (A) SRS/Use Case + System Architecture/Auth — 2 actor Web-only, School Admin có UC mới Academic Year/Violation cần đối chiếu SRS mà Nam là người chốt (A); System Admin gắn với Auth/RBAC do Nam phụ trách kiến trúc |
| **5** | **Nguyễn Phương Thảo** | Teacher + Parent | 13 + 18 = 31 | **62** | RACI: chủ (A/R) Testing/Defect Tracking — domain đơn giản, ít rủi ro nhất, phù hợp người ưu tiên chất lượng/kiểm thử hơn là khối lượng viết mới; **đồng thời Thảo đảm nhận vai trò rà soát/QA cuối cùng cho cả 329 file** (mục 6) — đúng chuyên môn Testing/Defect Tracking (A/R) đã khai báo ở Report 2, không phải việc phát sinh ngoài RACI |

**Tổng**: 328 file *(sửa 2026-07-01 từ ước lượng ban đầu 321 → 329 — School Administration thực tế 49 màn không phải 47 (lệch đếm dòng bảng + thiếu tính "Export Incident Reports" ở ghi chú), Medical Staff thực tế 27 màn không phải 24 (thiếu tính 3 màn dùng chung với School Administration: Incident List, Incident Detail, Compliance Dashboard); sau đó 329 → 328 vì gỡ "Campus Config" (System Admin) — hệ thống không quản lý campus. Đã xác nhận khớp với `14_Screen_Authorization_Matrix.md`.)*

**Thứ tự thực hiện đề xuất**: Huy làm **Guest & Common trước tiên** (1–2 ngày) → họp nhanh cả 5 người thống nhất mẫu → 5 người làm song song phần còn lại → Thảo chạy pass QA cuối theo mục 6 sau khi 4 người kia báo hoàn tất.

---

## 3. Nguồn bắt buộc phải trích dẫn khi viết 1 Screen Specification

Không được tự suy diễn field/hành vi ngoài các nguồn sau (đây là nguyên tắc đã áp dụng xuyên suốt dự án — tránh lặp lỗi "class diagram trích BR đã lỗi thời" từng phát hiện trước đó):

1. `03_Design/04_ScreenFlow/<file actor tương ứng>.md` — màn hình nằm ở đâu trong luồng, entry point, điều hướng đến/đi, UC liên quan (đã tra sẵn ở cột "Ánh xạ UC" trong các file này và ở `14_Screen_Authorization_Matrix.md`)
2. `06_Document/01_Report/Report 3.md` §3.2 — Use Case Description đầy đủ (Normal Flow/Alternative Flow/Exception) của đúng UC-### đó — nguồn field/hành vi chính
3. `02_Requirements/00_BusinessRules/business_rules.md` — BR liên quan (tra chéo qua `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` nếu không chắc UC nào ứng với BR nào)
4. `02_Requirements/00_BusinessRules/error_message.md` — MSG liên quan (validation/error state)
5. `03_Design/00_Foundation/` — 9 file: `Design_System.md` (màu/font/token), `Component_Guidelines.md` (chọn đúng control), `Layout_Guidelines.md`/`Navigation_Guidelines.md` (bố cục theo platform), `Responsive_Guidelines.md`, `Accessibility.md` (contrast/touch target 48dp), `Naming_Convention.md`, `AI_Design_Guidelines.md`, `UI_Design_Guidelines.md`
6. `03_Design/00_UI_Design_Documentation_Plan.md` §6.4 — **template chuẩn duy nhất** phải điền đúng cấu trúc, không tự thêm/bớt section

Nếu 1 field/hành vi không tìm được nguồn rõ ràng trong UC/BR (ví dụ màn TODO chưa có UC gốc: Allergy Review — xem ghi chú TODO trong `14_Screen_Authorization_Matrix.md`), phải ghi rõ **"[Giả định — cần team xác nhận]"** ngay trong file, không tự bịa như đã là UC chính thức. *(Cập nhật 2026-07-01: Security Policies, Partnership Approval History, School Configuration đã được gán UC chính thức qua audit A8; Campus Config đã bị loại bỏ hoàn toàn — hệ thống không quản lý campus, xem `00_TODO_GiaDinh_Master.md`.)*

---

## 4. Mẫu prompt dùng Claude viết Screen Specification

**Được phép gộp nhiều màn vào 1 prompt** — mỗi file spec ngắn nên viết từng màn một lượt sẽ quá chậm với 52–74 file/người. Nhưng **không gộp phần tra nguồn** — rủi ro thật khi nhồi cả chục màn vào 1 lượt là Claude "lười" dần về cuối danh sách: copy pattern từ màn đầu thay vì tra UC/BR/MSG riêng cho từng màn, dẫn tới field/trích dẫn sai hoặc bịa ở các màn cuối. Cách xử lý: gộp phần **giao việc** (danh sách màn), giữ nguyên yêu cầu **tra nguồn riêng cho từng màn trước khi viết**.

**Khuyến nghị batch size**: 5–10 màn/lượt (không phải 1, cũng không phải nguyên 52–74 màn của cả actor trong 1 lượt). Sau mỗi batch, mở ngẫu nhiên 1–2 file vừa tạo, kiểm tra nhanh BR/MSG/UC trích dẫn có thật không rồi mới chạy batch tiếp theo — bắt lỗi sớm thay vì để tới lúc xong cả 70 file mới rà lại.

### 4a. Prompt cho 1 màn hình (dùng khi màn hình phức tạp, cần Claude tập trung tối đa — VD 3 màn 3-step inspection của Service Caterers)

```
Viết Screen Specification cho màn hình "<Tên màn hình đúng theo Screen Authorization Matrix>"
— Actor: <Actor> — Nền tảng: <Web|Mobile> — UC liên quan: <UC-### nếu đã biết, hoặc "chưa rõ, tự tra">.

Đọc các nguồn sau trước khi viết (không suy diễn ngoài các nguồn này):
1. 03_Design/04_ScreenFlow/<file tương ứng>.md
2. 06_Document/01_Report/Report 3.md §3.2 — UC-### (Normal Flow/Alternative Flow/Exception)
3. 02_Requirements/00_BusinessRules/business_rules.md + error_message.md
4. 03_Design/00_Foundation/*.md
5. 03_Design/00_UI_Design_Documentation_Plan.md mục 6.4 (template)

Yêu cầu:
- Điền đúng template ở mục 6.4, không thêm/bớt section.
- Mọi BR-xxx/MSG-xxx/UC-xxx trích dẫn phải là ID có thật — kiểm tra lại trong traceability_matrix.md.
- Field/hành vi không có nguồn rõ ràng → ghi "[Giả định — cần team xác nhận]", không bịa.
- Lưu file đúng đường dẫn: 03_Design/05_ScreenSpecifications/<Web|Mobile>/<Actor>/<STT>_<TenManHinh>.md
```

### 4b. Prompt hàng loạt (batch 5–10 màn — dùng cho phần lớn khối lượng công việc)

```
Viết Screen Specification cho <N> màn hình sau của actor <Actor>, nền tảng <Web|Mobile>
(danh sách lấy đúng theo 03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md, khối <tên khối>):

1. <Tên màn 1> — UC-### (nếu biết)
2. <Tên màn 2> — UC-### (nếu biết)
...
<N>. <Tên màn N>

Với MỖI màn hình trong danh sách trên, làm tuần tự và độc lập — KHÔNG suy ra field của màn sau
từ pattern của màn trước, mỗi màn phải tự tra lại nguồn riêng của chính nó:
1. Tra 03_Design/04_ScreenFlow/<file actor tương ứng>.md — vị trí trong luồng, entry point, UC.
2. Tra 06_Document/01_Report/Report 3.md §3.2 đúng UC-### của màn đó (Normal/Alternative/Exception).
3. Tra business_rules.md + error_message.md đúng BR/MSG của màn đó (không copy từ màn khác trong batch).
4. Áp 00_Foundation/*.md (Design System, Component, Layout/Navigation, Accessibility).
5. Điền đúng template ở 00_UI_Design_Documentation_Plan.md mục 6.4, không thêm/bớt section.

Ràng buộc:
- Mọi BR-xxx/MSG-xxx/UC-xxx phải là ID có thật, kiểm tra lại traceability_matrix.md.
- Field/hành vi không có nguồn rõ ràng → ghi "[Giả định — cần team xác nhận]", không bịa.
- Lưu mỗi file đúng đường dẫn: 03_Design/05_ScreenSpecifications/<Web|Mobile>/<Actor>/<STT>_<TenManHinh>.md
- Sau khi xong cả <N> file, liệt kê lại danh sách file đã tạo kèm UC/BR/MSG đã trích dẫn cho từng file
  để tự rà soát chéo, không chỉ báo "đã xong".
- Cuối cùng, tạo thêm 1 file gộp 00_TongHop_<Actor>.md nối nguyên văn nội dung <N> file lẻ vừa tạo
  theo đúng thứ tự STT (không tóm tắt, không cắt bớt), có dòng đầu ghi rõ đây là bản dẫn xuất.
```

**Lưu ý vận hành**:
- Mỗi thành viên nên chạy Claude trong **thư mục/nhánh git riêng theo actor mình phụ trách** (đúng ranh giới ở mục 2) để tránh 2 người sửa trùng file hoặc merge conflict.
- Với batch, luôn yêu cầu Claude **liệt kê UC/BR/MSG đã dùng cho từng file ở cuối** (như trong mẫu 4b) — đây là cách rẻ nhất để tự phát hiện màn nào bị "lười" tra nguồn (thường lộ ra ở chỗ nhiều màn liên tiếp trích cùng 1 bộ BR/MSG giống hệt nhau dù nghiệp vụ khác nhau).
- Màn hình càng có nghiệp vụ đặc thù/phức tạp (GPS, camera, offline, multi-step) càng nên tách riêng theo mẫu 4a; màn hình dạng list/detail/form đơn giản, lặp pattern (chiếm đa số ở Raw Food Supplier, System Admin) dùng mẫu 4b batch 8-10 màn/lượt là hợp lý.

---

## 5. Màn hình dùng chung nhiều actor — cho phép làm song song/trùng, chốt bản chung sau

Một số màn hình có `x` ở nhiều actor trong `14_Screen_Authorization_Matrix.md` (VD: `Menu Detail` — School Admin + Parent; `Incident Detail` — School Admin + Teacher + Medical Staff; `Allergy Information` — Teacher + Medical Staff + Parent; `Record Violation Resolution`/`Violation Registry` — School Admin + Medical Staff).

**Vẫn viết riêng 1 file theo mỗi actor** (không gộp chung 1 file cho 2 actor) vì hành động hiển thị khác nhau theo actor (VD: School Admin xem `Incident Detail` có nút phê duyệt, Teacher chỉ xem) — điểm này không đổi.

**Điểm được nới**: không bắt buộc chờ 1 người "chốt" field chuẩn trước rồi người kia mới viết. Mỗi thành viên phụ trách actor nào cứ viết bản của actor mình **độc lập, song song, không cần chờ actor kia làm trước** — kể cả khi 2 người tự tra `class_diagram_description.md`/UC ra field hơi khác nhau (thiếu/thừa 1-2 field, đặt tên field khác chút). Đây là đánh đổi hợp lý để không ai bị block chờ người khác — sẽ hợp nhất ở bước review (mục 6), không chốt ngay từ đầu.

- File thuộc thành viên nào phụ trách actor đó thì người đó viết (VD: `Incident Detail` bản School Admin → Nam; bản Teacher → Thảo; bản Medical Staff → Huy) — không viết hộ actor khác, nhưng **không cần đợi nhau**.
- Khi review cuối (mục 6), phần **field hiển thị dữ liệu** (không phải hành động) của các bản cùng 1 màn hình phải được **hợp nhất về 1 mô tả chuẩn** — bản nào bám sát `class_diagram_description.md` nhất thì lấy làm chuẩn, các bản còn lại sửa lại theo, giữ nguyên phần hành động riêng theo actor.

---

## 6. Review & tổng hợp sau khi 5 người hoàn tất phần mình

**Người phụ trách bước này: Nguyễn Phương Thảo** — đúng chuyên môn Testing/Defect Tracking (A/R) đã khai báo ở `Report 2.md` RACI Matrix, không phải việc phát sinh ngoài phân công. 4 người còn lại báo "xong" cho Thảo trước khi chuyển bước `06_Wireframes/`.

1. Đối chiếu ngược mỗi thư mục `Web/<Actor>/` và `Mobile/<Actor>/` với đúng khối actor tương ứng trong `14_Screen_Authorization_Matrix.md` — đảm bảo **không sót màn hình nào có cột `x`**.
2. Kiểm tra mọi cặp Web/Mobile cùng 1 màn hình **trùng số thứ tự + trùng tên file** (theo `Naming_Convention.md` §2).
3. Grep toàn bộ `05_ScreenSpecifications/` tìm các `BR-###`/`MSG-###`/`UC-###` được trích dẫn, đối chiếu còn tồn tại trong `business_rules.md`/`error_message.md`/`traceability_matrix.md` hay không (tránh lặp lỗi trích BR lỗi thời từng phát hiện ở `class_diagram_description.md`).
4. **Hợp nhất các màn hình dùng chung nhiều actor** (danh sách ở mục 5): so sánh phần field hiển thị dữ liệu giữa các bản do các thành viên khác nhau viết độc lập, chọn 1 bản làm chuẩn (bám `class_diagram_description.md` sát nhất), sửa các bản còn lại cho khớp — giữ nguyên phần hành động khác nhau theo actor.
5. Danh sách "[Giả định — cần team xác nhận]" gom lại thành 1 bảng TODO chung, trình team duyệt trước khi chuyển sang bước `06_Wireframes/`.
6. Tái tạo lại toàn bộ file `00_TongHop_<Actor>.md` (mục 1b) sau khi mục 4 hợp nhất xong, để bản gộp phản ánh đúng bản đã chốt.

---

## 7. Prompt khởi động — mỗi thành viên chỉ gửi 1 lần duy nhất

Không cần tự tay soạn prompt theo mẫu 4a/4b cho từng batch nữa — gửi thẳng file này (`00_Team_Work_Assignment_Plan.md`) kèm 1 tin nhắn ngắn nêu mình là ai, để Claude tự đọc toàn bộ quy tắc, tự tra `14_Screen_Authorization_Matrix.md`, tự lập todo list theo batch, và tự chạy tuần tự nhiều lượt cho tới khi xong — chỉ cần theo dõi/duyệt kết quả.

**Mỗi thành viên đổi `<Tên>` và `<Actor(s)>` đúng theo bảng mục 2 rồi gửi đúng 1 lần** (Minh → Service Caterers; Huy → Medical Staff + Guest & Common; Mạnh → Raw Food Supplier; Nam → School Administration + System Admin; Thảo → Teacher + Parent, sau đó thêm vai trò QA ở mục 6):

```
Tôi là <Tên>, phụ trách <Actor(s)> theo đúng phân công ở mục 2 của file
03_Design/05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md (đọc kỹ toàn bộ file này trước).

Hãy tự làm toàn bộ phần việc của tôi, không cần hỏi lại giữa chừng trừ khi phát hiện
mâu thuẫn dữ liệu nghiêm trọng giữa các nguồn (BR/UC/MSG/field không khớp nhau):

1. Đọc kỹ 00_Team_Work_Assignment_Plan.md — nhớ đúng: cấu trúc thư mục (mục 1),
   quy tắc file tổng hợp (mục 1b), nguồn bắt buộc trích dẫn (mục 3), mẫu prompt
   4a (màn phức tạp, làm riêng) và 4b (batch 5-10 màn), quy tắc màn dùng chung
   nhiều actor (mục 5).
2. Tra đúng khối actor của tôi trong 03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md
   để lấy danh sách đầy đủ + đúng thứ tự các màn hình phải viết (Web và/hoặc Mobile).
3. Tự lập 1 todo list chia thành các batch 5-10 màn/lượt (tách riêng từng màn cho các
   màn có nghiệp vụ đặc thù: GPS, camera, offline, multi-step — theo mẫu 4a).
4. Chạy tuần tự từng batch trong todo list. Với MỖI màn hình: tự tra
   03_Design/04_ScreenFlow/<file actor>.md + Report 3.md §3.2 đúng UC + business_rules.md +
   error_message.md + 00_Foundation/*.md, điền đúng template ở
   00_UI_Design_Documentation_Plan.md mục 6.4, lưu đúng đường dẫn
   05_ScreenSpecifications/<Web|Mobile>/<Actor>/<STT>_<TenManHinh>.md.
   Không suy diễn field màn sau từ pattern màn trước trong cùng batch.
5. Sau mỗi batch, tự rà nhanh 1-2 file vừa tạo (BR/MSG/UC trích dẫn có thật không,
   đối chiếu traceability_matrix.md) trước khi chạy batch kế tiếp.
6. Field/hành vi không có nguồn rõ ràng → ghi "[Giả định — cần team xác nhận]", không bịa.
7. Sau khi xong toàn bộ màn được giao, tạo file 00_TongHop_<Actor>.md nối toàn bộ
   theo mục 1b.
8. Đối chiếu lại lần cuối với 14_Screen_Authorization_Matrix.md để xác nhận không sót
   màn nào của actor tôi phụ trách, rồi báo cáo tóm tắt: tổng số file đã tạo, danh sách
   "[Giả định — cần xác nhận]" nếu có.

Dùng todo list/task tracking riêng để tự theo dõi tiến độ qua nhiều lượt, không cần tôi
nhắc lại giữa các batch. Bắt đầu ngay.
```

**Lưu ý**: prompt này giả định Claude đang chạy trong môi trường có quyền đọc/ghi trực tiếp file dự án (Claude Code, hoặc Cowork có kết nối thư mục `SEP490_G8`) — không dùng được nếu chỉ là bản chat thường không có công cụ đọc/ghi file.

---

## 8. Sau khi 5 người đẩy (push) file lên — quy trình tiếp theo

### 8.1. Ngay sau khi push (git + nội dung)

1. Merge theo nhánh actor (mỗi actor 1 nhánh, theo mục 2) — review chéo giữa các thành viên trước khi merge vào nhánh chính, đúng `Report 2.md` §3.2.2.2 "Documentation review: ... reviewed by team members before submission" và "Design review: ... UI designs are reviewed to identify potential issues before implementation".
2. Thảo chạy pass QA/tổng hợp theo **mục 6** của file này (đối chiếu Screen Authorization Matrix, kiểm tra cặp Web/Mobile, grep BR/MSG/UC thật, hợp nhất màn dùng chung, gom bảng "[Giả định]").
3. Chỉ sau khi Thảo báo hoàn tất mục 6 mới chuyển sang bước 8.2.

### 8.2. Bước kế tiếp trong pipeline: `06_Wireframes/`

Theo `00_UI_Design_Documentation_Plan.md` §4.6 và mục 6.5 (AI Generation Prompt): Wireframe **sinh 1-1 từ Screen Specification**, dùng AI tool (Stitch/Figma AI/ChatGPT) theo mẫu prompt ở `00_Foundation/AI_Design_Guidelines.md` mục 2, áp đúng `Design_System.md` (màu/font/token thật, không còn placeholder). **Không tự thêm field/nghiệp vụ mới** ngoài những gì đã có trong Screen Specification — nếu phát hiện thiếu, quay lại sửa Screen Specification trước, không vá trực tiếp ở Wireframe.

### 8.3. ⚠️ QUAN TRỌNG — đối chiếu lịch SDS thật trong `Report 2.md`, cần đảo thứ tự ưu tiên

`Report 2.md` mục 3.3 (Project Deliverables) đã gắn UI Wireframe vào **Report 4 – SDS v1.0, hạn 14/06/2026** — mốc này **đã qua từ trước khi `05_ScreenSpecifications` bắt đầu được viết**, tức nhóm đang chạy bù. Các mốc SDS chi tiết theo iteration tiếp theo (từ WBS/Deliverables trong `Report 2.md`) vẫn đang tới gần:

| SDS | Hạn | Module (theo Report 2) | Actor tương ứng (theo mục 2) |
| --- | --- | --- | --- |
| v1.3 | **05/07/2026 (còn 4 ngày)** | Food receiving inspection, evidence management | Raw Food Supplier (Mạnh), Service Caterers (Minh) |
| v1.4 | 12/07/2026 | Three-step inspection, food sample retention | Service Caterers (Minh) |
| v1.5 | 19/07/2026 | Student health and allergy management | Medical Staff (Huy), Teacher/Parent (Thảo) |
| v1.6 | 26/07/2026 | Food incident management, notification workflows | Medical Staff (Huy), School Administration (Nam) |
| v1.7 | 02/08/2026 | Reporting, dashboard, analytics, export | School Administration/System Admin (Nam) |

**Hệ quả**: đừng đợi cả 5 người viết xong 100% Screen Specification (~328 file) rồi mới bắt đầu Wireframe — sẽ trễ SDS v1.3 (chỉ còn 4 ngày). Đề xuất đảo lại thứ tự ưu tiên thực thi so với mục 2:

1. **Ưu tiên 1 (ngay)**: Mạnh (Raw Food Supplier) + Minh (Service Caterers, phần receiving/evidence trước) hoàn tất Screen Specification rồi chuyển thẳng sang Wireframe cho đúng nhóm màn "food receiving inspection, evidence management" — kịp SDS v1.3 05/07.
2. **Ưu tiên 2**: Minh hoàn tất phần còn lại (3-step inspection, food sample retention) cho SDS v1.4 12/07.
3. **Ưu tiên 3**: Huy (Medical Staff) + Thảo (Teacher/Parent) cho SDS v1.5 19/07.
4. **Ưu tiên 4**: Huy + Nam (phần Incident) cho SDS v1.6 26/07.
5. **Ưu tiên 5 (ít gấp nhất dù nhiều file nhất)**: Nam (School Administration/System Admin, phần Reporting/Dashboard) cho SDS v1.7 02/08.

Huy vẫn làm Guest & Common trước tiên trong 1-2 ngày đầu (mục 2) vì đây là mẫu chuẩn dùng chung, không nằm trong 5 mốc trên nên không xung đột lịch.

### 8.4. Sau Wireframe: `07_Prototype/`

Theo `00_UI_Design_Documentation_Plan.md` §4.7: nối các Wireframe theo đúng mũi tên điều hướng có sẵn trong `04_ScreenFlow` (mermaid flowchart) — không tự thêm luồng mới; áp Design System hoàn chỉnh; dùng để demo/UAT. Checklist rà soát cuối đặt tại `07_Prototype/00_Review_Checklist.md` (viết khi tới bước đó) — mỗi màn trong Prototype phải thể hiện đủ BR/MSG đã gán ở Screen Specification tương ứng.

## Nguồn dữ liệu
- `03_Design/00_Foundation/Naming_Convention.md` §2 (quy ước thư mục/tên file).
- `03_Design/00_UI_Design_Documentation_Plan.md` §4.5, §6.4 (mục đích, nội dung, template Screen Specification).
- `03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md` (toàn bộ số liệu màn hình/actor/nền tảng dùng để chia việc ở mục 2).
- `06_Document/01_Report/Report 1.md` §3.1.2 Table 3‑1 (danh sách nhóm: Nguyễn Văn Nam - Leader, Nguyễn Phương Thảo, Bùi Quang Huy, Nguyễn Đức Mạnh, Trần Tuấn Minh).
- `06_Document/01_Report/Report 2.md` §3.4 Table 3‑2 (RACI Matrix — dùng để gán actor theo đúng chuyên môn chính thức của từng người).
- `06_Document/01_Report/Report 2.md` §3.3 (Project Deliverables) + WBS mục 4 (SDS v1.0–v1.7 theo iteration) — dùng để xác định thứ tự ưu tiên thực thi ở mục 8.3.
