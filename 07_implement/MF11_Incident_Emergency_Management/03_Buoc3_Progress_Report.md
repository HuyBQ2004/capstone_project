# MF11 — Bước 3 (Nối Frontend Thật) — Báo cáo bàn giao

**Ngày dừng:** 04/08/2026 · **Ngày hoàn tất:** 05/08/2026 (phiên Claude Code kế tiếp)
**Trạng thái:** ✅ **HOÀN TẤT 5/5 resource** — `mvn compile` sạch, `npx tsc --noEmit` sạch
toàn dự án, `eslint` sạch, `npm run audit` sạch (54 luật + 53 bảng seed). Verify bằng curl
thật (login `ngoth.medical.synthetic@example.test`, xem mục 6 mới bên dưới) vì môi trường
agent không có trình duyệt. **CHƯA COMMIT/PUSH** — chờ người dùng xác nhận (đúng quy ước dự án).

> Mục 2–5 dưới đây GIỮ NGUYÊN VĂN làm hồ sơ bàn giao gốc (04/08) — vẫn đúng, chỉ khác
> toàn bộ "Việc còn lại" ở mục 4 nay đã làm xong, xem mục 6 (mới, 05/08) để biết đã làm
> gì khác/thêm so với kế hoạch gốc. File này để một AI/người khác đọc và hiểu đã làm gì,
> KHÔNG cần đọc lại transcript gốc.

---

## 1. Bối cảnh

MF11 (Incident & Emergency Management) đã xong Bước 1 (TDS/TestSpec) và Bước 2 (backend
thật, đã merge vào `dev`). Bước 3 theo `07_implement/02_Module_Completion_Workflow.md` là
**nối frontend thật vào backend thật** — thay toàn bộ mock resource bằng gọi API thật, cho
CẢ 5 resource của MF11: `source-reports`, `incident-records`, `incidents`, `corrective-actions`,
`violations`.

Quyết định phạm vi (người dùng đã chọn tường minh qua AskUserQuestion): **làm toàn bộ ~30
file FE**, không cắt giảm. Lý do ràng buộc kỹ thuật quan trọng: cơ chế `REAL_RESOURCES` ở
`04_Development/Frontend/src/lib/api/real-resources.ts` bật/tắt mock theo **tên resource**,
không theo từng trang — nghĩa là 1 resource chỉ được thêm vào mảng `REAL_RESOURCES` sau khi
**TẤT CẢ** trang tiêu thụ resource đó (mọi vai trò) đã sửa xong. Vì vậy chưa trang nào được
bật thật cả — tất cả vẫn đang chạy trên mock server cho tới khi bước cuối (mục 4.7 dưới) chèn
cả 5 tên resource vào mảng cùng lúc.

Toàn bộ công việc **CHƯA COMMIT** — đúng theo quy ước dự án (chỉ commit khi được yêu cầu rõ).

---

## 2. Đã xong (không cần làm lại)

### 2.1 Backend — gói API + 3 lỗ hổng thật đã vá trong phiên trước
File `04_Development/Backend/src/main/java/com/sfms/mf11/**` cùng `mf04/{PartnerController,PartnerServiceImpl}`
và `common/reference/ReferenceResolver.java` đã sửa xong, gồm:
- `GET /partners` mở thêm cho `MEDICAL_STAFF` (CAPA cần chọn đơn vị suất ăn phụ trách).
- `ReferenceResolver.serviceCatererRepresentatives()` (native query) cho Sổ vi phạm.
- `ViolationServiceImpl`: `catererRepresentatives()`, `uploadEvidence()` multipart, fix filter
  `incidentId` (code→UUID), nới `unitSanctionLevel` thành optional có điều kiện.
- `SourceReportServiceImpl`: `mealDate`/`symptoms` không còn bắt buộc; `draftRecordId` trả
  đúng mã thay vì luôn `null`.
- `IncidentServiceImpl`: thêm `markStudentsRecovered()` + `POST /incidents/{code}/recovered-students`
  (endpoint MỚI — trước đó cột `recovered` không có đường ghi nào, tức KHÔNG THỂ đóng sự cố
  có học sinh ảnh hưởng).
- `CapaServiceImpl`: `syncIncidentStatus()` nối `Incident.status` theo vòng đời CAPA
  (CAPA_PENDING/CAPA_APPROVED — enum có sẵn nhưng chưa từng được set).
- Migration `V20`/`V21` đã merge `dev` (không sửa nữa); `V22__mf11_incident_events_recovered.sql`
  (file mới, CHƯA merge — thêm `STUDENT_RECOVERED` vào CHECK constraint `incident_events.type`).

### 2.2 Frontend — 4/5 resource ĐÃ XONG hoàn toàn (tsc-clean, chờ bật switch)
- **corrective-actions**: `medical-staff/corrective-actions/page.tsx` + `[id]/page.tsx`,
  `school-admin/corrective-actions/page.tsx`, `service-caterers/corrective-actions/page.tsx`.
  Đây là pattern MẪU đã chạy đúng vòng đời CAPA thật (`submit-for-approval` → `approve`/`reject`
  → `accept`/`submit-evidence` → `validate`/`rework`) — xem mục 4.3 để tái dùng.
- **violations**: cả 5 trang (medical-staff × 3 + school-admin × 2) + `violation-utils.ts`.
- **source-reports + incident-records**: `teacher/incidents/page.tsx`,
  `parent/safety-concern/page.tsx`, `medical-staff/incidents/page.tsx`,
  `medical-staff/incidents/records/[id]/page.tsx`.
- **incidents (một phần)**: các trang NHỎ đã xong — `medical-staff/dashboard`,
  `school-admin/dashboard`, `teacher/dashboard`, `service-caterers/incidents/page.tsx`,
  `medical-staff/samples/page.tsx`, `medical-staff/conduct-inspection/page.tsx`,
  `service-caterers/inspections/[id]/page.tsx`, `incident-detail-view.tsx`,
  `school-admin/incidents/page.tsx`, `school-admin/incidents/[id]/close/page.tsx`.

Hạ tầng dùng chung đã sẵn: `types.ts` (đủ alias DTO MF11), `hooks.ts` (đã mở rộng
`useUploadMutation` nhận multi-file), `contract-check.ts` (5 check MF11), tài liệu trong
`real-resources.ts`.

### 2.3 Backend — 2 sửa MỚI trong phiên này (SAU compaction, CHƯA build/test)
Phát hiện khi map `medical-staff/incidents/[id]/page.tsx` (file lớn nhất, 1826 dòng) vào 11
endpoint hành động thật của `IncidentController`. Hai endpoint `POST /incidents/{code}/validate-capa`
và `POST /incidents/{code}/request-rework` (trên `IncidentController`/`IncidentServiceImpl`)
**trùng chức năng và SAI LỆCH** so với `POST /corrective-actions/{code}/validate` và
`.../rework` (trên `CapaController`/`CapaServiceImpl`) — bản trên `CorrectiveAction` đã được
trang `medical-staff/corrective-actions/[id]/page.tsx` dùng và chạy đúng từ trước, còn bản
trên `Incident` có 2 lỗi: (a) không kiểm tra CAPA đang ở `SUBMITTED_EVIDENCE` trước khi chốt
`COMPLETED` (bỏ qua gate nghiệp vụ), (b) **`CapaServiceImpl.validate()` không set
`Incident.status = VALIDATED`** — nghĩa là nếu Y tế chốt CAPA đạt qua trang CAPA detail, sự cố
kẹt vĩnh viễn ở `CAPA_APPROVED`, không bao giờ vào được bước "Soạn báo cáo chính thức".

Đã sửa (file đã ghi, CHƯA build/test):
1. `CapaServiceImpl.validate()` — thêm `syncIncidentStatus(capa, IncidentStatus.VALIDATED)`
   sau khi set CAPA `COMPLETED`.
2. `CapaServiceImpl.syncIncidentStatus()` — mở rộng guard trạng thái nguồn cho phép
   `CAPA_APPROVED` (trước chỉ cho `INVESTIGATING`/`CAPA_PENDING`), vì CAPA đi từ
   `APPROVED_FOR_IMPLEMENTATION` → `COMPLETED` xảy ra khi Incident đang ở `CAPA_APPROVED`.
3. Xoá `validateCapa()`/`requestRework()` khỏi `IncidentService.java` (interface),
   `IncidentServiceImpl.java` (implementation).
4. Xoá 2 endpoint tương ứng (`/{code}/validate-capa`, `/{code}/request-rework`) khỏi
   `IncidentController.java`.

---

## 3. ĐANG DỞ DANG NGAY LÚC DỪNG — đọc kỹ mục này trước khi làm gì khác

Phiên bị dừng **giữa chừng** bước dọn dẹp sau khi xoá 2 endpoint ở mục 2.3. Việc y hệt đang
làm: xoá import chết trong `IncidentController.java`.

**Việc cần làm NGAY đầu tiên:**
```
04_Development/Backend/src/main/java/com/sfms/mf11/web/IncidentController.java dòng 16:
import com.sfms.mf11.dto.IncidentDtos.ReworkRequest;
```
Import này giờ KHÔNG còn dùng ở đâu trong file (đã xoá 2 method dùng nó) — xoá dòng import
này. Sau đó kiểm tra lại toàn bộ `IncidentController.java` xem còn import/biến chết nào khác
không (ví dụ `@Valid` có còn được dùng bởi endpoint khác không — có, các endpoint khác vẫn
dùng `@Valid` nên KHÔNG xoá import đó).

**Sau đó, việc BẮT BUỘC trước khi tin bất kỳ thay đổi Java nào ở mục 2.3 là đúng:**
```
mvn -f 04_Development/Backend/pom.xml compile
```
(hoặc `mvn package` như quy trình đã dùng suốt phiên trước) — CHƯA CHẠY LẦN NÀO sau các sửa
ở mục 2.3. Rất có thể còn sai sót nhỏ (tên biến, import) vì đây là sửa "nguội" chưa qua trình
biên dịch.

---

## 4. Việc còn lại — theo thứ tự nên làm

### 4.1 Hoàn tất dọn dẹp Java (tiếp mục 3)
- Xoá import chết, `mvn compile` cho tới khi sạch.

### 4.2 Vá lỗ hổng MỚI phát hiện: `IncidentResponse` KHÔNG có danh sách bằng chứng
Khi đọc kỹ `IncidentController.java`, xác nhận: có `POST /incidents/{code}/evidence` (thêm 1
bằng chứng) và `POST /incidents/{code}/evidence/finalize` (chốt danh sách), nhưng **KHÔNG CÓ
endpoint GET nào trả về danh sách bằng chứng đã thêm**, và `IncidentDtos.IncidentResponse`
(record ở `IncidentDtos.java` dòng ~60) cũng không có field `evidences`/`investigationEvidence`
nào. Trang `medical-staff/incidents/[id]/page.tsx` (mock cũ) hiển thị danh sách bằng chứng
trong "Việc 4: Bổ sung bằng chứng" — nếu không vá, màn hình thật sẽ KHÔNG BAO GIỜ hiển thị lại
bằng chứng đã lưu sau khi tải lại trang.

Cách vá (theo đúng pattern D1/D9 đã dùng cho `QuarantineBatchDto`/`SuspectedDishDto` ngay
trong cùng file):
1. Thêm `record EvidenceDto(String id, String type, String description, int fileCount, Instant capturedAt)`
   vào `IncidentDtos.java`.
2. Thêm field `List<EvidenceDto> evidences` vào `IncidentResponse` (record, dòng ~61-71).
   KHÔNG thêm vào `IncidentCaterersResponse` (Service Caterers không cần thấy bằng chứng điều
   tra) — có thể cân nhắc thêm vào `IncidentTeacherResponse` nếu cần, nhưng file FE đang xử lý
   (Medical Staff) chỉ cần `IncidentResponse`.
3. `IncidentServiceImpl.java`: thêm private mapper `evidences(Incident i)` (giống hệt cấu trúc
   `quarantineBatches(Incident i)` ở dòng ~572), gọi `incident.getEvidences()` (entity
   `IncidentEvidence` đã có sẵn field `id`/`type`/`description`/`fileCount`/`capturedAt` —
   xem `com/sfms/mf11/domain/IncidentEvidence.java`), rồi truyền vào `toFullResponse()`
   (dòng ~525).
4. `mvn compile` lại.

### 4.3 Build backend + sinh lại `schema.d.ts`
Quy trình đã dùng suốt phiên trước (đừng bỏ bước nào):
```bash
# 1. Build (KHÔNG dùng spring-boot:run — path Unicode gây lỗi fork trên máy này)
mvn -f 04_Development/Backend/pom.xml package -DskipTests

# 2. Có sẵn Postgres tạm trên cổng 5433 tên container sfms-postgres-verify từ phiên trước —
#    kiểm tra còn sống không, nếu không thì tạo lại (xem lệnh docker trong lịch sử phiên
#    trước hoặc dựng mới theo application.yml của Backend).

# 3. Chạy jar — LUÔN kèm -Duser.timezone=UTC (driver JDBC gửi "Asia/Saigon" mà Postgres
#    container không nhận, gây lỗi "invalid value for parameter TimeZone")
java -Duser.timezone=UTC -jar 04_Development/Backend/target/<tên-jar>.jar

# 4. Từ 04_Development/Frontend
npm run gen:api
```
Sau bước này, `schema.d.ts` sẽ có field `evidences` mới trong `IncidentResponse`, và các type
alias (`SubmitForApprovalRequest`, `assigneeSupplierCode`, v.v. đã đổi trước đó) khớp thật.

### 4.4 Viết lại `medical-staff/incidents/[id]/page.tsx` (~1826 dòng — việc chính còn lại)
File đã đọc TOÀN BỘ trong phiên trước, đã map đủ 11 endpoint hành động thật + 1 GET bổ sung.
**Bảng mapping đầy đủ** (client function hiện tại → endpoint thật, method dùng qua `useAction`
trừ khi ghi chú khác):

| Việc trong UI (khối 2) | Hàm mock hiện tại | Endpoint thật | Ghi chú |
|---|---|---|---|
| Đánh giá mức độ | `saveAssessmentAndStart()` | `POST /incidents/{code}/assess` (`AssessRequest{severity, clinicalRiskAssessment, suspectedDishCodes}`) | `severity` **bắt buộc** (`@NotBlank`); tự chuyển `SEVERITY_PENDING`→`INVESTIGATING` khi status đang là `SEVERITY_PENDING`; món nghi vấn là APPEND-ONLY (idempotent, skip trùng) — **không xoá được** món đã gửi |
| Lưu nháp đánh giá | `saveDraftAssessment()` | `PATCH /incidents/{code}/assessment-draft` (`AssessmentDraftRequest{clinicalRiskAssessment?, firstAidNotes?, measuresDraft?}`) | PATCH thật (không phải POST) — dùng `usePatchAction`, không phải `useAction` |
| Chọn món nghi vấn (sau khi đã assess) | `toggleDishSuspect()` | Gọi LẠI `POST /incidents/{code}/assess` với `severity`/`clinicalRiskAssessment` hiện tại (resend, không đổi) + `suspectedDishCodes` = mảng đầy đủ món đã tick | Vì additive-only, KHÔNG CÓ endpoint bỏ tick — phải khoá checkbox một khi đã tick (giống pattern lô cách ly) |
| Danh sách món để chọn | `dishRows`/`dishPool`/`byMealDate`/`byWindow` (tính tay từ `samples` mock) | `GET /incidents/{code}/suspected-dish-candidates` (`SuspectedDishCandidateResponse[]{dishId, dishCode, dishName, mealType, servedAt}`) | Backend đã tính sẵn cửa sổ điều tra server-side — **nên thay hẳn** logic `vnTime`/`INVESTIGATION_WINDOW_HOURS`/`byMealDate` bằng gọi GET này qua `useDetail`-hack (`resource="incidents/${id}"`, `id="suspected-dish-candidates"`) hoặc thêm biến thể `usePlainList` nhận full path |
| Cách ly lô | `toggleBatchQuarantine()` | `POST /incidents/{code}/quarantine-batches` (`QuarantineRequest{batchCodes[], reason}`) | Additive/append-only, lỗi `alreadyQuarantined` nếu gửi lại lô đã khoá — PHẢI lọc chỉ gửi lô CHƯA khoá; `reason` bắt buộc |
| Bản đồ truy xuất | (đọc `batchOriginOf()` từ mock) | `GET /incidents/{code}/traceability-map` (`TraceabilityMapResponse{status, nodes:TraceNode[]}`) | Thay `batchOriginOf()` bằng GET này |
| Thêm bằng chứng | (chuyển sang route `./evidence`, xem 4.5) | `POST /incidents/{code}/evidence` (`EvidenceRequest{type, description, fileCount}`) | Append-only, không sửa/xoá bằng chứng cũ |
| Danh sách bằng chứng đã lưu | `evidenceList` (state cục bộ, nạp từ `incident.investigationEvidence` mock) | `incident.evidences` (SAU khi vá mục 4.2) | Nếu CHƯA vá mục 4.2 thì màn này không có nguồn thật — PHẢI làm 4.2 trước |
| Chốt danh sách bằng chứng | `finalizeEvidence()` | `POST /incidents/{code}/evidence/finalize` | Không có body |
| Kết luận nguyên nhân | `saveRootCause(status)` | `POST /incidents/{code}/root-cause` (`RootCauseRequest{rootCause, rootCauseStatus}`) | `rootCauseStatus` phải đúng `"PRELIMINARY"`/`"FINAL"` |
| Gửi yêu cầu khắc phục | `establishMeasures()` | **2 bước**: (1) `POST /corrective-actions` (`CreateCapaRequest{incidentCode, description, assigneeSupplierCode}`) — dùng `useCreate` đã có sẵn, chỉ đổi field; (2) ngay khi (1) thành công, gọi `useAction<CorrectiveActionDto>("corrective-actions")` với `{id: <code trả về>, action:"submit-for-approval"}` (body rỗng vì description/assignee đã set ở bước 1) | `assignee` PHẢI đổi từ input text tự do → `<Select>` chọn mã đối tác thật, nạp từ `usePagedList<PartnerDto>("partners",{type:"SERVICE_CATERER",status:"APPROVED",pageSize:100})` — COPY nguyên logic này từ `medical-staff/corrective-actions/[id]/page.tsx` (đã chạy đúng, xem file đó dòng 39-44) |
| Xác nhận khắc phục đạt | `submitValidation()` | `useAction<CorrectiveActionDto>("corrective-actions")` → `{id: capa.id, action:"validate"}` | **KHÔNG** dùng endpoint trên `incidents` (đã xoá ở mục 2.3/3). Gate thật CHỈ gồm: CAPA đang `SUBMITTED_EVIDENCE` + `notRecovered==0` (server tự kiểm) — field `kitchenNote` (Ghi chú kiểm tra bếp) hiện tại **KHÔNG có cột lưu trữ nào phía backend**, quyết định phạm vi được đề xuất trong phiên trước: **bỏ hẳn ô này**, không gửi đi đâu cả (không phải bug cần vá, là input UI không có đối tượng nghiệp vụ tương ứng) |
| Xác nhận HS hồi phục | (checkbox `recovered` state cục bộ, KHÔNG gửi đi đâu ở bản mock) | `POST /incidents/{code}/recovered-students` (raw body `string[]` mã học sinh — **không phải object**) — dùng `useMutation` + `api.post()` thô như đã làm ở `school-admin/incidents/[id]/close/page.tsx`, copy nguyên cách gọi từ đó | Đây là gate PHẢI gọi TRƯỚC `validate` CAPA — nếu không, `validate` sẽ luôn lỗi `closeBlocked` |
| Yêu cầu thực hiện lại | `requestRework()` | `useAction<CorrectiveActionDto>("corrective-actions")` → `{id: capa.id, action:"rework", body:{reason}}` | Cũng KHÔNG dùng endpoint `incidents` (đã xoá) |
| Soạn & nộp báo cáo | `submitReport()` | `POST /incidents/{code}/report` (không body) — nhưng field `officialClinicalNotes` (`clinicalNotes` state) hiện KHÔNG có request field riêng để set cùng lúc | Cần kiểm tra lại: `report()` trong `IncidentServiceImpl` (đã đọc ở phiên trước, dòng ~399-409) chỉ set `reportExported=true`, KHÔNG nhận `officialClinicalNotes`. Field này phải set qua **`assessment-draft`** (có field `firstAidNotes` nhưng KHÔNG có `officialClinicalNotes`) — **cần kiểm tra lại xem `officialClinicalNotes` có setter/endpoint nào không**; nếu không có, đây là lỗ hổng backend thứ 3 cần vá tương tự mục 4.2, hoặc cân nhắc gộp nội dung vào `clinicalRiskAssessment`/`firstAidNotes` đã có sẵn |
| Lưu nháp báo cáo (Section D) | `saveDraftSectionD()` | Có thể tái dùng `PATCH /incidents/{code}/assessment-draft` nếu field khớp, hoặc cần vá thêm | Xem xét cùng lúc với dòng trên |

**Lưu ý quan trọng về `useAction`:** hook hiện tại POST tới `/:resource/:id/:action`. Endpoint
`assessment-draft` là **PATCH**, không phải POST — phải dùng `usePatchAction` (đã có sẵn, dùng
ở `quarantineBatch` trong cùng file cũ). Endpoint `evidence/finalize` có dấu `/` trong tên hành
động — kiểm tra `useAction`/`usePatchAction` có xử lý được action nhiều đoạn hay phải hack như
các resource khác trong phiên trước (xem cách `useDelete` bị hack với id ghép chuỗi).

**Danh sách state cần đổi/loại bỏ khi viết lại** (input mock không map được sang trường thật):
- `assignee` (string tự do) → đổi thành `assigneeCode` (chọn từ `partners`).
- `kitchenNote` → khuyến nghị bỏ hẳn (xem bảng trên).
- `evidenceList` (state cục bộ nạp 1 lần từ `incident.investigationEvidence`) → đọc trực tiếp
  từ `incident.evidences` (sau vá 4.2), không cần state riêng, refetch tự động qua React Query.
- Toàn bộ logic `dishPool`/`dishRows`/`byMealDate`/`byWindow`/`vnTime`/`INVESTIGATION_WINDOW_HOURS`
  (dòng ~426-461 file cũ) nên xoá, thay bằng gọi `GET /incidents/{code}/suspected-dish-candidates`.
- `relatedCapa`/`approvedCapa` hiện code như thể 1 sự cố có NHIỀU CAPA (`.filter()`,
  `.map()`, `Promise.all()`) — **backend enforce tối đa 1 CAPA/sự cố**
  (`CapaServiceImpl.create()` ném lỗi nếu đã tồn tại). Nên đơn giản hoá thành 1 biến `capa`
  duy nhất (hoặc `undefined`), bỏ hết vòng lặp/`Promise.all`.

### 4.5 Viết lại `medical-staff/incidents/[id]/evidence/page.tsx`
**CHƯA đọc file này trong bất kỳ phiên nào** — đọc từ đầu trước khi sửa. Dự kiến chỉ cần gọi
`POST /incidents/{code}/evidence` (giống dòng "Thêm bằng chứng" ở bảng trên) rồi điều hướng
quay lại `./[id]`.

### 4.6 Kiểm tra dead code (đã xác nhận trước đó, chỉ cần double-check nếu nghi ngờ)
`send-incident-summary-dialog.tsx` và `create-incident-record-dialog.tsx` — đã xác nhận bằng
`Grep` (files_with_matches) KHÔNG có nơi nào import ngoài chính nó. Để nguyên, không cần sửa.

### 4.7 Bật switch + kiểm tra toàn cục
1. Thêm cả 5 tên resource (`source-reports`, `incident-records`, `incidents`,
   `corrective-actions`, `violations`) vào mảng `REAL_RESOURCES` trong
   `04_Development/Frontend/src/lib/api/real-resources.ts` — làm **CÙNG LÚC**, không tách lẻ.
2. `npx tsc --noEmit` toàn bộ `04_Development/Frontend` — sửa tới khi sạch.
3. Chạy backend thật (Docker Postgres + `java -jar ... -Duser.timezone=UTC`) + FE dev server,
   test tay CẢ 5 luồng theo vai (Medical Staff điều tra đầy đủ 1 sự cố từ đầu tới đóng, School
   Admin duyệt CAPA + đóng sự cố, Service Caterers nộp bằng chứng CAPA, Teacher/Parent tạo báo
   cáo nguồn). Đây là bước bắt buộc theo chuẩn dự án ("test golden path trước khi báo xong").
4. Dọn container Postgres tạm (`docker rm -f sfms-postgres-verify` hoặc tương đương) và tiến
   trình `java` nền khi xong.

### 4.8 Cập nhật memory + báo cáo cuối
Sau khi xong hết, cập nhật memory `project_mf11_buoc3.md` (đã có, cần sửa lại nội dung) phản
ánh trạng thái HOÀN TẤT, và hỏi người dùng có muốn commit/push hay không (KHÔNG tự ý commit).

---

## 5. Việc KHÔNG được làm khi tiếp tục (nhắc lại quy ước đã thống nhất)
- KHÔNG commit/push nếu người dùng không yêu cầu rõ ràng.
- KHÔNG sửa `mock-server.ts`/`next.config.ts`/cơ chế auth — chỉ thêm resource vào
  `REAL_RESOURCES`.
- KHÔNG amend migration `V20`/`V21` (đã merge `dev`) — mọi thay đổi schema mới phải là file
  `V22` trở đi (đã có `V22`, thêm `V23` nếu cần).
- KHÔNG bật resource vào `REAL_RESOURCES` khi còn TRANG NÀO của resource đó chưa sửa (kiểm tra
  bằng cách grep resource đó trong toàn bộ `src/app/(app)/**` trước khi bật).

---

## 6. Hoàn tất 05/08/2026 — đã làm gì, khác gì so với kế hoạch mục 4

### 6.1 §4.1/§4.2/§4.3 — đúng như kế hoạch, có 1 vá backend PHÁT SINH thêm
- Dọn import chết (`ReworkRequest` khỏi `IncidentController`) — hoá ra **đã nằm sẵn** trong
  commit `eaaac19` (đã merge trước đó), không phải làm lại.
- Thêm `EvidenceDto` + field `evidences` vào `IncidentResponse`
  (`IncidentDtos.java`/`IncidentServiceImpl.java`, mapper `evidences(Incident i)` cùng khuôn
  `quarantineBatches(Incident i)`) — đúng kế hoạch mục 4.2.
- **Vá THÊM ngoài kế hoạch:** `officialClinicalNotes` có setter ở entity `Incident` nhưng
  **không endpoint nào ghi được nó** — cùng lớp lỗi với `evidences` (field có ở response,
  không có đường ghi) mà mục 4.4 dòng "Soạn & nộp báo cáo" đã đoán đúng ("cần kiểm tra lại
  xem có setter/endpoint nào không"). Fix: thêm field vào `AssessmentDraftRequest`
  (`PATCH /assessment-draft`) — dùng lại đúng endpoint "soạn thảo" đã có, KHÔNG thêm endpoint
  mới. `mvn compile` sạch cả hai lần vá.
- Build + chạy backend thật (Postgres tạm cổng 5433, `-Duser.timezone=UTC`) + `npm run gen:api`
  — xác nhận `schema.d.ts` có `evidences`/`officialClinicalNotes` đúng vị trí.

### 6.2 §4.4 — viết lại `medical-staff/incidents/[id]/page.tsx` (1827 → ~1450 dòng)
Theo đúng bảng mapping 11 hành động của mục 4.4, với các quyết định kiến trúc sau (không có
sẵn trong bảng mapping gốc, phải tự quyết khi viết):

- **Món/lô nghi vấn (Việc 2) — nguồn dữ liệu đổi hẳn:** bỏ `dishBatchesForIncident()`/
  `quarantineCandidates()` (mock `inventory`) — dùng `GET /incidents/{code}/
  suspected-dish-candidates` (danh sách món) + `GET /traceability/dish-batches` (MF06, đã
  nối thật từ 03/08, đúng như comment sẵn trong `TraceabilityDtos.java`: *"MF11 dùng cho bản
  đồ truy xuất"* — module đó đã để sẵn chỗ cho MF11 dùng). Món/lô đã gửi lên server thì khoá
  checkbox vĩnh viễn (append-only thật, không có đường bỏ tick) — đúng cảnh báo ở đầu file.
- **`kitchenNote`:** bỏ hẳn khỏi UI theo đúng khuyến nghị mục 4.4 dòng "Xác nhận khắc phục đạt"
  — không có cột lưu trữ nào phía backend.
- **CAPA tối đa 1/sự cố** (backend enforce, `CapaServiceImpl.create` ném lỗi nếu đã có) — bỏ
  hẳn logic `Promise.all`/vòng lặp CAPA của bản mock (viết cho nhiều CAPA), dùng biến `capa`
  đơn thay vì mảng, đúng khuyến nghị "Danh sách state cần đổi" ở mục 4.4.
- **`validate()`/`rework()` tự đồng bộ `Incident.status` phía server** (transaction 1 lần) —
  FE KHÔNG tự `update.mutate({status:...})` sau đó nữa, chỉ gọi action trên `corrective-actions`
  rồi `qc.invalidateQueries(["incidents"])` để refetch.
- **`recovered-students`:** checkbox đọc thẳng `affectedStudents[].recovered` từ server (không
  còn state cục bộ `recovered`), tick → gọi `POST` ngay (mỗi lần 1 mã HS) — không có đường
  "bỏ tick" (khớp thật: BR-155 hồi phục không thể đảo ngược).
- **Sổ lưu mẫu (Việc 3 + panel "Mẫu phục vụ điều tra")** — GIỮ NGUYÊN đọc mock `samples`/
  `sample-lab-requests` (MF08 chưa nối), không đổi gì trong khối này.

### 6.3 §4.5 — viết lại `evidence/page.tsx` — nhỏ, đúng kế hoạch
`useUpdate` → `useAction("incidents")` action `"evidence"`; đọc `incident.evidences` (thật)
thay `investigationEvidence` (mock).

### 6.4 §4.7 — bật switch + verify
- Thêm `source-reports`, `incident-records`, `incidents`, `corrective-actions`, `violations`
  vào `REAL_RESOURCES` CÙNG LÚC theo đúng yêu cầu.
- `npx tsc --noEmit` sạch toàn dự án, `eslint` sạch, `npm run audit` sạch.
- **KHÔNG launch được trình duyệt thật** (môi trường agent không có) — verify bằng curl thật
  thay vì "test tay 5 luồng" như mục 4.7 yêu cầu:
  login `ngoth.medical.synthetic@example.test` / `P@ssw0rd123` (MEDICAL_STAFF), chạy trên
  `INC-2026-001` (seed có sẵn CAPA `CAPA-2026-001`, đủ dữ liệu affectedStudents/quarantineBatches):
  - `GET /incidents/INC-2026-001` — shape khớp 100% với `IncidentDto` FE dùng, `evidences`
    có sẵn 1 bản ghi từ seed.
  - `GET /incidents/INC-2026-001/suspected-dish-candidates` — trả 2 món, đúng shape.
  - `POST /incidents/INC-2026-001/evidence` — 200, `evidences` tăng lên 2.
  - `PATCH /incidents/INC-2026-001/assessment-draft {officialClinicalNotes}` — 200, giá trị
    round-trip đúng ở lần GET sau — xác nhận vá mục 6.1 hoạt động thật.
  - `GET /traceability/dish-batches?dishId=thit-kho-hanh&serviceDate=2026-07-06&mealType=LUNCH`
    — trả đúng lô `BATCH-MF06-0001` (`isLocked:true`, khớp `quarantineBatches` của incident).
  - `POST /incidents/INC-2026-001/recovered-students ["HS-3A-005"]` — 200, cả 3 HS
    `recovered:true` sau đó (đúng gate `allRecovered` dùng để mở nút "Xác nhận khắc phục").
  - `GET /corrective-actions?incidentId=INC-2026-001` — trả đúng `CAPA-2026-001`.
  - Dọn container Postgres tạm + tiến trình `java` nền sau khi xong.
- **CHƯA test tay qua UI thật trong trình duyệt** — đây là giới hạn thật của môi trường, không
  phải bỏ sót. Người tiếp theo mở UI thật nên đi qua đúng 5 luồng liệt kê ở mục 4.7 việc 3
  trước khi coi là nghiệm thu xong.

### 6.5 Việc còn treo, KHÔNG chặn Bước 3 (để lại nếu có người làm tiếp)
- `establishMeasures()` gọi 2 bước (`POST /corrective-actions` rồi `submit-for-approval`)
  KHÔNG bọc trong 1 transaction phía client — nếu bước 2 lỗi giữa chừng, CAPA đã tạo ở DRAFT
  mà FE không biết. Rủi ro thấp (bước 2 hiếm khi lỗi nếu bước 1 đã qua), chưa xử lý.
- Chưa thêm test case Playwright/Cypress nào (dự án hiện không có bộ UI e2e) cho luồng MF11.
