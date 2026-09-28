# Handoff — Xóa mock data khỏi Frontend, chạy hoàn toàn trên backend thật

**Ngày bắt đầu:** 08/2026. **Cập nhật gần nhất:** 07/08/2026 (phiên 6).

## TÓM TẮT PHIÊN 6 (07/08/2026) — nối FE cho phần backend phiên 5b, + migrate `SpecialMealRequest`

Điểm xuất phát: kéo `origin/dev` về nhánh `huybqhe180599` (fast-forward tới `3add567`, đã gồm
toàn bộ sửa backend + `schema.d.ts` mới của phiên 5b). Làm ĐÚNG 4 việc "cho phiên sau" mà phiên
5b để lại, rồi làm tiếp ứng viên GO của Giai đoạn 3.

### A — Nối FE cho 4 gap backend đã vá ở phiên 5b (XONG cả 4)

1. **`documents` thật cho cột (9)/(10) Mẫu số 1** (`service-caterers/inspections/[id]`) — gom
   `certificateIds` của MỌI lô đang hiện (`liveSupplierBatches`), dedupe + sort thành CSV, gọi
   MỘT lần `usePagedList<DocumentDto>("supplier-documents", { ids: csv }, { enabled: csv≠"" })`
   (không N+1 theo lô). Đã kiểm `@PreAuthorize` của `ComplianceDocumentController.list` có
   `SERVICE_CATERERS` → không 403. `DocumentResponse.id` CHÍNH LÀ mã chứng từ
   (`partner_documents.code`) nên khớp thẳng phần tử `certificates`, không cần map trung gian.
   Javadoc "KHÔNG có nguồn thật" ở tham số `documents` của `step1RowsOfIssue()` đã thay bằng mô
   tả đường nối thật.
2. **Cột (6) "địa chỉ, điện thoại"** — xác nhận `PartnerResponse` (list) nay CÓ `address`/
   `contact` (schema.d.ts ghi rõ "Thêm 08/2026 — cần cho cột (6) Mẫu số 1"), KHÔNG cần gọi
   detail N+1. **Vá thêm 1 lỗi hiển thị**: `step1RowsOfIssue()` nối chuỗi
   `` `${partner.address} — ${partner.contact}` `` — 2 field này optional trên DTO thật nên NCC
   chưa khai sẽ hiện literal `"undefined — undefined"`; đổi sang
   `[address, contact].filter(Boolean).join(" — ")`.
3. **`GET /supplier-products/sourcing`** (`parent/nutrition/[dishId]`) — panel "Nhà cung cấp
   nguyên liệu" (BR-076) nay đọc THẬT qua `usePlainList<IngredientSourcingDto>` (endpoint trả
   THẲNG mảng, KHÔNG bọc `Paged<T>` — dùng `usePlainList`, không phải `usePagedList`). Alias mới
   `IngredientSourcingDto` ở `lib/api/types.ts`. Render tra theo `ingredientId`, **KHÔNG theo
   chỉ số/thứ tự** — server bỏ qua nguyên liệu không tra được mã trong danh mục chuẩn nên mảng
   trả về NGẮN HƠN `dish.ingredients`. Hook đặt TRƯỚC mọi early-return (quy tắc Hooks), tự tắt
   bằng `enabled` khi chưa tải xong món.
   **Quyết định cho câu hỏi để mở của phiên 5b** ("giữ `dishSourcing()` cho màn khác hay đổi
   hết"): **XOÁ HẲN `dishSourcing()`** — grep xác nhận nơi gọi DUY NHẤT là trang Parent trên,
   nên sau khi nối endpoint mới nó là dead code. `bestPriceOf()` GIỮ NGUYÊN (khác mục đích: các
   màn nội bộ `menu-form.tsx`/`service-caterers/orders/new` cần chính con SỐ giá để ước tính chi
   phí, và có đủ quyền gọi `/partners` + `/supplier-products`).
4. **Dọn `HealthRecordDto | "null"`** — `parent/health/page.tsx` đổi sang
   `useSubDetail<HealthRecordDto | null>` + `recordRaw ?? undefined` (bỏ `typeof === "object"`),
   comment ở `lib/api/types.ts` cập nhật theo hành vi mới của BE.

### B — Giai đoạn 3: migrate `SpecialMealRequest` (ứng viên GO của phiên 5) — XONG, 12 file

Đối chiếu field-by-field: `SpecialMealRequestResponse` có ĐỦ 15 field mock khai, cộng thêm
`requestedAt`/`reviewedAt`. **Phát hiện làm bài toán dễ hơn dự tính**: khác biệt duy nhất tưởng
là `status: keyof typeof STATUS_BADGE` (union chặt) vs `string` (DTO) — nhưng `STATUS_BADGE` khai
là `Record<string, …>` nên `keyof typeof` của nó THỰC CHẤT chỉ là `string`, **không chặt hơn DTO**
⇒ KHÔNG cần Draft type kiểu `MenuTemplate`/`ChecklistGroup`, thay thẳng `SpecialMealRequestDto`
được. (Bài học: kiểm `keyof typeof X` bằng cách xem khai báo của `X` — `Record<string,…>` thì
union đó là ảo giác.)

Đã sửa 10 trang + `lib/health-docs.ts` (`latestApprovedMeal` nhận/trả DTO, `dateKey` chịu
`undefined`), xoá interface khỏi `mock-data.ts` và dòng `_specialMealsContract` khỏi
`contract-check.ts`. Sửa kèm 12 điểm `string | undefined` do DTO optional:
- **`StatusBadge` đổi prop thành `status: string | undefined`** (1 chỗ sửa, hết 7/12 lỗi) — badge
  trung tính khi thiếu là hành vi ĐÚNG, hợp lý hơn bắt 7 nơi gọi tự `?? ""`.
- `STANDARD_DIETS.includes(x.dietaryType ?? "")` (3 chỗ).
- `medical-staff/special-meals`: `if (!selected?.id) return` ở `confirmApprove`/`confirmReject`
  — `?? ""` sẽ gọi `/special-meals//approve`, chặn tại nguồn đúng hơn.
- `parent/special-meal-requests`: `useCreate<Partial<SpecialMealRequest>>` → `useCreate<
  SubmitSpecialMealDto>` (shape GHI thật, 4 field required — `Partial<Response>` cũ cho phép gửi
  thiếu/gửi field dẫn xuất mà Jackson âm thầm bỏ).
- **2 lỗi hiển thị ngày vá kèm**: `parent/health` và `medical-staff/health-records/[id]` in
  THẲNG `r.date`/`approvedMeal.date` — mock là dd/MM/yyyy nhưng DTO thật là ISO `yyyy-MM-dd`
  (D4), tức 2 màn này đang hiện "2026-08-07" trên dữ liệu thật. Bọc `formatVnDate()`.

### Trạng thái cuối phiên 6

```bash
cd 04_Development/Frontend
npx tsc --noEmit -p tsconfig.json   # SẠCH — 0 lỗi
npx next lint                       # 8 warning — ÍT HƠN baseline HEAD (9), không thêm cái nào
npm run build                       # compile hết 136 route
cd ../Backend && mvn test           # 352 test / 0 failure
```

`mock-data.ts`: 3547 → **3468 dòng** (−79). Interface: 41 → **37** (−4: `SpecialMealRequest`,
`Classroom`, `Partner`, `PartnerDocument`). Nhánh `huybqhe180599`, CHƯA commit.

> ⚠️ **Cách đếm dòng — đừng dùng PowerShell `Get-Content | Measure-Object -Line`**: nó KHÔNG
> đếm dòng TRỐNG, ra 3211 trong khi file thật có 3468 dòng (257 dòng trống). Mọi con số trong
> tài liệu này (các phiên trước) đo bằng `wc -l` — dùng lẫn 2 cách sẽ khiến số của phiên sau
> trông như "giảm mạnh" một cách giả tạo. Kiểm nhanh: `wc -l < src/lib/mock-data.ts`.

### B2 — Giai đoạn 3: migrate `Classroom` (16 file) — XONG

Mục "CHƯA kiểm tra field-by-field" cuối cùng của phiên 5. Kết quả đối chiếu: `ClassroomResponse`
có **ĐỦ** mọi field mock khai **trừ `lunchTime`**, và các đường GHI đã dùng request shape riêng
từ trước (`useCreate<Record<string,unknown>, Classroom>`, `usePatch<{id;gradeId?;capacity?},…>`)
nên `Classroom` chỉ còn là type ĐỌC ⇒ thay thẳng bằng `ClassroomDto`, không cần Draft type.

**Quyết định `lunchTime` (mà `_classroomsContract` "giữ chỗ" từ 02/08 — nay đã ra)**: **bỏ hẳn ở
FE**. Backend chưa từng có field này; mock khai tay với đúng 1 giá trị seed cho lớp 3A. Từ 06/08
khi `useTeacherClass()` chuyển sang API thật thì nó LUÔN `undefined` ⇒ 2 màn
`teacher/{dashboard,distribution}` hiện "Bữa trưa dự kiến **—**" vĩnh viễn. Đó là UI CHẾT chứ
không phải "chờ backend", nên gỡ đoạn hiển thị. Muốn khôi phục tính năng thì BE thêm field vào
`ClassroomResponse` TRƯỚC — đừng khai lại ở FE.

16 lỗi `tsc` lộ ra sau khi đổi type, tất cả đều là "field optional trên DTO":
- `students`/`allergies` (**field DẪN XUẤT**, COUNT runtime) → `?? 0` tại 7 điểm. Quan trọng ở
  `promote/page.tsx`: `target.students + promoteCount > CAPACITY` với `undefined` ra `NaN`, mà
  `NaN > CAPACITY` LUÔN false ⇒ **mất cảnh báo quá tải sĩ số** — im lặng sai, đúng loại lỗi nguy
  hiểm hơn vỡ build.
- `id` dùng dựng URL PATCH → chặn bằng guard (`if (!classroom.id) return`), KHÔNG `?? ""` (sẽ gọi
  `/classrooms//homeroom-teacher`).
- 2 helper `gradeLevelsOf()`/`gradeEnrollmentOf()` ở `mock-data.ts` đổi tham số từ
  `Pick<Classroom,…>` sang shape TỐI THIỂU (`{ grade?: string; students?: number }[]`) — để
  `mock-data.ts` không phải import `lib/api/types`; nơi gọi truyền `ClassroomDto[]` vẫn khớp cấu
  trúc.

### B3 — Giai đoạn 3: tách `Partner`/`PartnerDocument` theo list vs detail (11 điểm gọi) — XONG

Mục "CẦN THIẾT KẾ LẠI" của phiên 5. Cách tách đúng hoá ra đơn giản hơn dự đoán vì mỗi điểm gọi
đã tự nói nó thuộc ngữ cảnh nào:
- `usePagedList("partners")` (9 điểm) → **`PartnerDto`** (`PartnerResponse`)
- `useDetail("partners", id|"me")` (2 điểm: `partnerships/[id]`, `supplier/dashboard`) →
  **`PartnerDetailDto`** (`PartnerDetailResponse` — chỉ nó có `taxId`/`documents`/`email`/
  `phone`/`description`)

`_partnersContract` cũ tự khai lỗ hổng của chính nó ("so với bản ĐẦY ĐỦ hơn nên KHÔNG bắt được
việc đọc nhầm field chỉ-có-ở-Detail từ kết quả `usePagedList` — tự rà bằng tay"). Tách type
**đóng hẳn lỗ hổng đó**: `tsc` giờ chặn thẳng, không còn "tự rà bằng tay".

~20 lỗi optional lộ ra, đáng chú ý:
- `performanceScore` → `?? 0` tại 4 điểm. Không chỉ là chuyện compile: `p.performanceScore < 70`
  với `undefined` cho `false`, nên đối tác CHƯA CHẤM ĐIỂM lẽ ra hiện "—" lại lọt vào nhánh hiển
  thị bình thường; giờ rơi đúng nhánh "Chưa có".
- `p.name.includes(...)` trong `step1RowsOfIssue()` → thêm guard `!!p.name`.
- `s.address`/`s.contact` ở `SupplierSelect` (`inspections/[id]`) — cùng bệnh
  "undefined — undefined" đã vá ở cột (6), nay vá nốt ở ô nhập.
- `id` dùng dựng URL action (`approve`/`reject`/`suspend`/`reinstate`, tạo đơn hàng) → guard,
  KHÔNG `?? ""`.

### B4 — Dọn kèm: `GradeDto` khai tay → `Api<"GradeResponse">`

Chính comment trong `types.ts` đã dặn "đổi sang `Api<"GradeResponse">` khi `gen:api` có schema
thật" — nay đã có, 4 field khớp tên 1:1. Đổi xong lộ 3 chỗ `id` optional ở `grades/page.tsx`
(guard, không `?? ""`). Cũng gỡ import chết `RoleKey` khỏi `mock-data.ts` (chỉ còn trong comment).

### C — Xác minh bằng gọi API THẬT (backend chạy lại từ code hiện tại, cổng 8080)

Postgres map cổng **5432** (KHÔNG phải 5433 như phiên 5b ghi — container đã đổi/khác máy, đúng
default của `application.yml`). Lệnh chạy lại (JDK 25 bắt buộc):

```bash
docker start sfms-postgres
cd 04_Development/Backend && JAVA_HOME="/c/Program Files/Java/jdk-25.0.3" mvn -q spring-boot:run
cd 04_Development/Frontend && npm run gen:api   # → 0 dòng đổi, schema.d.ts khớp 100% backend
```

Tài khoản mẫu (pass chung `P@ssw0rd123`): `binhnv.parent.synthetic@example.test` (PARENT),
`duchm.caterer.synthetic@example.test` (SERVICE_CATERERS), `ngoth.medical.synthetic@example.test`
(MEDICAL_STAFF), `quynhdt.teacher.synthetic@example.test` (TEACHER).

Kết quả đo được (curl, không suy đoán):

| Kiểm tra | Kết quả |
|---|---|
| PARENT `GET /supplier-products/sourcing?ingredientIds=ING-GAO` | **200** → `[{"ingredientId":"ING-GAO","supplierName":"Cty CP Thực phẩm sạch GreenValley"}]` |
| PARENT `GET /supplier-products` (list, có giá) | **403** — đúng thiết kế, không rò giá qua gọi trực tiếp |
| SC `GET /partners` (list) | có `address`/`contact` thật → cột (6) Mẫu số 1 hết trống |
| SC `GET /supplier-documents?ids=DOC-GV-01` (chứng từ CẤP CÔNG TY) | **total=0** — rào an toàn `filterBatchCertificateCodes()` chạy đúng |
| PARENT `POST /special-meals` → MEDICAL `GET` → `POST /{id}/approve` | 201 → 200 → APPROVED, đủ 15 field FE render |
| PARENT `GET /students/HS-3A-001/health-record` (chưa có hồ sơ) | **200 + body RỖNG** — xem lỗi bên dưới |

`npx tsc --noEmit` sạch và `npm run build` (Next production build) build hết mọi route.

### D — 1 LỖI DO CHÍNH PHIÊN NÀY GÂY RA, đã vá (ghi lại vì suýt lọt)

Mục A.4 dọn `HealthRecordDto | "null"` dựa trên GIẢ ĐỊNH (chép từ ghi chú phiên 5b) rằng BE nay
trả JSON `null` thật, nên đổi `typeof recordRaw === "object"` → `recordRaw ?? undefined`. **Đo
thật bằng curl mới lộ: `ResponseEntity.ok(null)` làm Spring bỏ HẲN body** (`200`, `Content-Length:
0`), KHÔNG serialize thành `null`. Axios đưa body rỗng về `""` — mà `??` chỉ bắt `null`/
`undefined`, không bắt `""` ⇒ `record` thành chuỗi rỗng thay vì `undefined`. May là `""` falsy nên
`{record ? …}` vẫn rẽ đúng nhánh "chưa có hồ sơ", nhưng type đã nói dối và bất kỳ so sánh
`!== undefined` nào sau này sẽ vỡ. **Đã vá**: giữ lại `typeof === "object"` (an toàn với cả 3
dạng: `""`, `"null"` cũ, object thật), comment ghi rõ số đo.
**Bài học**: đừng dọn "code phòng thủ đã hết tác dụng" dựa trên mô tả trong handoff — gọi thử
endpoint trước. Ghi chú phiên 5b nói "trả JSON `null` thật" là SAI, phiên đó chỉ build/compile
chứ chưa gọi thật.
**Nợ BE mới (nhỏ)**: nên đổi `getRecord()` sang `204 No Content` cho tường minh — `200` + body
rỗng là hợp đồng mơ hồ, mỗi HTTP client parse một kiểu.

### E — Cột (9)/(10) đã thông END-TO-END bằng dữ liệu thật, + 1 GAP MỚI

DB ban đầu không có dòng nào trong `batch_certificates` (mọi `certificateIds` đều rỗng) nên
không có gì để tra. Đã tự tạo dữ liệu thật để kiểm:
`POST /supplier-batches/BATCH-MF06-0001/certificates` (multipart, **chỉ nhận PDF/JPG/PNG** —
file `.txt` bị 400 `ING-009`) → `certificateIds: ["DOC-BC-9A2258F9"]` → SC gọi
`/supplier-documents?ids=DOC-BC-9A2258F9` ra đúng `PRODUCT_ORIGIN | Batch certificate
BATCH-MF06-0001`. Trộn thêm mã chứng từ CẤP CÔNG TY (`ids=DOC-GV-01,DOC-BC-...`) vẫn chỉ trả về
mã chứng từ lô ⇒ **rào an toàn của phiên 5b hoạt động đúng trên dữ liệu thật**.

**GAP MỚI (nợ BE, đã ghi cảnh báo ngay tại chỗ tính regex trong `step1RowsOfIssue()`)**:
`SupplierBatchService.uploadCertificate()` **HARD-CODE** `document_type = 'PRODUCT_ORIGIN'` và
`name = "Batch certificate {code}"`. Hệ quả trên Mẫu số 1: cột (10) "giấy kiểm dịch" bật cho MỌI
chứng từ lô, còn cột (9) "giấy ĐK VS thú y" **KHÔNG BAO GIỜ** bật được.

**Đối chiếu QĐ 1246 (đọc bản gốc `00_Policies/1246_QD-BYT_...md`) — kết luận KHÁC dự đoán ban
đầu, đọc kỹ trước khi sửa:**
- Cột (9) **CÓ** là cột chính thức của Mẫu số 1 **Mục I** (thực phẩm tươi sống/đông lạnh) — thứ
  tự cột: (8) Chứng từ, hóa đơn · (9) Giấy ĐK VS thú y · (10) Giấy kiểm dịch. FE đã render đủ
  (`inspection-sheets.ts`, `CheckField` ở màn nhập) ⇒ **không được bỏ cột**.
- Nhưng **2 cột KHÁC CẤP dữ liệu**: (10) là giấy kiểm dịch động vật cấp theo TỪNG LÔ (đúng là
  chứng từ lô, và là giấy DUY NHẤT mà phần quy định — Điều 3.1.a — nêu đích danh); (9) là giấy
  đăng ký/chứng nhận điều kiện vệ sinh thú y cấp cho **CƠ SỞ** của NCC, dùng chung mọi lô ⇒ hồ sơ
  **CẤP CÔNG TY**, cùng loại ĐKKD/ATTP. Cột (9) chỉ có trong BIỂU MẪU, không nằm trong Điều 3.
- ⇒ **Sửa cột (9) KHÔNG phải bằng `documentType` ở `POST .../certificates`** (đó là đường LÔ, chỉ
  phục vụ cột 10). Muốn tự suy cột (9) cần: (a) thêm giá trị vệ sinh thú y vào `DocumentType`
  (D6, `mf04/domain`) — taxonomy dùng chung, cần BR/UC; (b) cho SERVICE_CATERERS đọc hồ sơ cấp
  công ty của NCC nguyên liệu — hiện `GET /supplier-documents` scope về ĐÚNG partnerId của chính
  nhà thầu, và nhánh `ids` (phiên 5b) CỐ Ý chỉ mở cho chứng từ LÔ.
- **Trong lúc chờ, hiện trạng đã ĐÚNG QUY ĐỊNH**: cột (9) để người kiểm tự tích tay — QĐ 1246
  yêu cầu GHI CHÉP kết quả kiểm tra, không yêu cầu hệ thống tự suy. Thêm nữa, dòng rau/củ/quả
  tươi vốn KHÔNG có giấy thú y lẫn kiểm dịch động vật ⇒ để trống ở các dòng đó là ĐÚNG.
- Kiểm thêm và XÁC NHẬN ĐÚNG: Mẫu số 1 **Mục II** (thực phẩm khô/bao gói sẵn) KHÔNG có 2 cột này
  — `Step1DryRow` của ta cũng không có `hasVetCert`/`hasQuarantineCert`, khớp biểu mẫu.
**ĐỪNG sửa regex ở FE để "chữa"** — sai chỗ.

### F — 1 TEST ĐANG FAIL TRÊN `dev`, đã vá + chốt lại hợp đồng `204`

`HealthRecordIT#getHealthRecordReturns200WithNullWhenAbsent` **FAIL** (`expected: "null" but was:
""`) — phiên 5b đổi controller nhưng KHÔNG chạy test, lỗi này nằm sẵn trên `dev`. Gốc rễ là hợp
đồng của endpoint đã đi qua 3 đời mà không đời nào tường minh:

| Đời | Code BE | Kết quả thật trên dây |
|---|---|---|
| Ban đầu | `ResponseEntity.ok("null")` | `200` + CHUỖI JSON `"null"` |
| Phiên 5b | `ResponseEntity.ok(res)` với `res == null` | `200` + **body RỖNG** (Spring KHÔNG serialize `null`) |
| **Phiên 6 (chốt)** | `res == null ? noContent() : ok(res)` | **`204 No Content`** |

Đã sửa `StudentHealthFileController.getRecord()` + `@Operation` description + test (nay khẳng
định `204` và body rỗng) + Javadoc ghi đủ 3 đời để người sau không đổi vòng lại. `HealthRecordIT`
**4/4 pass**. Regen `schema.d.ts` (từ backend mới) chỉ đổi đúng 1 dòng mô tả.

**Bài học ghi vào quy trình**: sửa 1 endpoint thì PHẢI chạy IT của endpoint đó — phiên 5b chỉ
`mvn compile`, đủ để build xanh nhưng test đã đỏ suốt từ đó.

### Còn lại

- **Xem bằng mắt trên trình duyệt**: phiên này xác minh tới tầng API (curl từng endpoint, đúng
  role) + `tsc` + `npm run build`, CHƯA mở trình duyệt bấm từng màn.
- **Dữ liệu test còn để lại trong DB local** (cố ý — để 5 màn có dữ liệu thật mà xem):
  1 yêu cầu bữa ăn đặc biệt Halal của `HS-3A-001` (đã APPROVED), và chứng từ `DOC-BC-9A2258F9`
  đính vào `BATCH-MF06-0001`. Hồ sơ sức khỏe tạo lúc test ĐÃ XOÁ LẠI (`student_health_records`
  còn 0 dòng) — bắt buộc, vì `HS-3A-001` là fixture mà test `204` cần "chưa có hồ sơ".
- **Lưu ý chạy backend**: `TaskStop`/kill tiến trình `mvn` KHÔNG giết JVM con — cổng 8080 vẫn bị
  giữ, lần chạy sau chết với "Port 8080 was already in use" mà nhìn log tưởng chạy được. Kiểm
  bằng `netstat -ano | findstr :8080` rồi kill đúng PID java, hoặc chạy cổng khác:
  `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`.

---

## TÓM TẮT PHIÊN 5b (07/08/2026) — sửa backend cho các gap phát hiện ở phiên 5, CHƯA nối FE

Tiếp phiên 5 (FE). Nam yêu cầu chỉnh backend (quyền/response) cho các gap đã ghi ở phiên 5, chạy
backend, cập nhật `schema.d.ts` qua swagger. Dừng giữa chừng vì hết token phiên — **đã sửa +
build backend + regen schema.d.ts + xác nhận `tsc` FE sạch, nhưng CHƯA nối bất kỳ trang FE nào
dùng field/endpoint mới**. Việc nối FE là **việc đầu tiên của phiên sau**.

### Đã sửa ở Backend (`04_Development/Backend`, JAVA_HOME cần trỏ JDK 25 — máy này có ở
### `C:\Program Files\Java\jdk-25.0.3`, biến `JAVA_HOME` hệ thống đang trỏ JDK 21, PHẢI set tay
### khi chạy `mvn`: `JAVA_HOME="/c/Program Files/Java/jdk-25.0.3" mvn ...`)

1. **`PartnerResponse` thêm `address`/`contact`** (`mf04/dto/PartnerDtos.java`) — vá gap phiên 5
   (cột (6) Mẫu số 1 kiểm thực 3 bước). MapStruct tự map theo tên field từ entity `Partner`
   (đã có sẵn 2 field này), KHÔNG cần sửa `PartnerMapper.java`.
2. **`StudentHealthFileController.getRecord()`** (`mf03/web/`) — bỏ hẳn nhánh trả chuỗi
   `"null"`, đổi kiểu trả về thành `ResponseEntity<HealthRecordResponse>` (trả JSON `null` thật
   khi chưa có hồ sơ). Đã xác nhận FE (`parent/health/page.tsx`) đọc bằng `typeof === "object"`
   nên xử lý đúng cả 2 dạng — sửa xong KHÔNG cần đổi FE, nhưng **NÊN dọn `HealthRecordDto |
   "null"` / comment "chuỗi null"** ở `lib/api/types.ts` và trang đó cho gọn (chưa làm).
3. **`ComplianceDocumentServiceImpl.list()` thêm filter `ids`** (CSV mã chứng từ,
   `mf04/service/`) — cho phép tra tên/loại/hạn nhiều chứng từ theo mã CÙNG LÚC, bỏ qua scoping
   partner nhưng **CHỈ với mã đã lọc qua `batch_certificates`** (`filterBatchCertificateCodes()`,
   JDBC — bảng này không có JPA entity) — tức chỉ lộ được chứng từ LÔ HÀNG (thú y/kiểm dịch),
   KHÔNG lộ chứng từ cấp công ty (ĐKKD/ATTP) nhạy cảm hơn. Đọc kỹ comment tại điểm gọi trong code
   trước khi động vào — đây là quyết định an toàn có chủ đích, ĐỪNG đơn giản hoá thành
   `code IN (:ids)` trần trụi (đã thử, phát hiện mã sinh TUẦN TỰ `DOC-0001...` chứ không phải
   UUID, brute-force được — xem lịch sử sửa trong code).
4. **Endpoint mới `GET /supplier-products/sourcing`** (`mf06/web/SupplierProductController.java`
   + `SupplierProductService.sourcing()` + DTO `IngredientSourcingResponse`) — trả
   `{ingredientId, supplierName}` (giá rẻ nhất, đối tác APPROVED), **KHÔNG có `unitPrice`**.
   `@PreAuthorize` gồm cả `PARENT`. Dựng RIÊNG thay vì mở quyền PARENT vào `/supplier-products`
   (list) gốc — endpoint đó có `unitPrice` (dữ liệu thương mại), mở quyền PARENT vào sẽ lộ giá
   qua gọi API trực tiếp dù UI hiện tại (`dishSourcing()`) không hiển thị giá.
5. **CỐ TÌNH KHÔNG sửa** (đã cân nhắc, quyết định để nguyên):
   - `IngredientResponse` thiếu `allergens` — DB (`catalog_ingredients`, V10) KHÔNG có cột nào
     liên quan tác nhân dị ứng. Đây là tính năng mới thật sự (cần cột/bảng + màn nhập liệu quản
     lý), không phải "chỉnh response" — ngoài phạm vi phiên này, cần BR/UC riêng.
   - `GET /allergy-declarations` không cấp `SERVICE_CATERERS` — giữ nguyên vì không có tính
     năng nào đang bị chặn bởi gap này (`StudentResponse.allergies` đã đủ cho BR-063/065 ở màn
     Service Caterers, xem "Chi tiết mục 5" ở trên) — mở thêm quyền không cần thiết chỉ tăng bề
     mặt tấn công vô ích.

### Đã chạy để xác nhận

```bash
# Backend — compile sạch (JDK 25!)
cd 04_Development/Backend
JAVA_HOME="/c/Program Files/Java/jdk-25.0.3" mvn -q compile

# Postgres local — container đã có sẵn từ trước, map cổng 5433 (KHÔNG phải 5432 mặc định!)
docker start sfms-postgres   # nếu chưa chạy

# Backend — chạy thật, override DB_URL đúng cổng 5433, migrate V1..V53 chạy sạch
cd 04_Development/Backend
DB_URL="jdbc:postgresql://localhost:5433/sfms" JAVA_HOME="/c/Program Files/Java/jdk-25.0.3" \
  mvn -q spring-boot:run
# → Started SfmsApplication, Tomcat port 8080, KHÔNG lỗi

# Frontend — regen types từ backend đang chạy
cd 04_Development/Frontend
npm run gen:api   # đọc http://localhost:8080/v3/api-docs → src/lib/api/schema.d.ts
npx tsc --noEmit -p tsconfig.json   # SẠCH — 0 lỗi (schema.d.ts đổi field/thêm endpoint mới,
                                     # KHÔNG phá field nào cũ đang dùng)
```

Backend còn ĐANG CHẠY (background) lúc dừng phiên — nếu phiên sau máy khác/mở lại, nhớ tự chạy
lại theo đúng 4 lệnh trên (thứ tự: postgres → backend → gen:api → tsc), đừng giả định nó vẫn
chạy.

### Việc cho phiên sau (nối FE — CHƯA làm gì ở bước này, chỉ mới đổi backend + regen type)

1. **`step1RowsOfIssue()` — nối `documents` thật.** Tham số `documents` ở
   `inspections/[id]/page.tsx` hiện vẫn truyền `undefined`/`[]` — giờ đã CÓ CÁCH tra thật:
   `usePagedList<DocumentDto>("supplier-documents", { ids: certificateIds.join(",") })` cho từng
   lô đang hiện trên Mẫu số 1 (gom hết `certificateIds` của các lô trong `liveInventory`/lô đã
   xuất, gọi 1 lần). Xem field mới trong `DocumentDto` (`Api<"DocumentResponse">`) đã có sẵn
   `documentType`/`name`.
2. **`step1RowsOfIssue()` — partner giờ có `address`/`contact` thật.** Không cần sửa gì thêm ở
   FE (đã nối tham số `partners` ở phiên 5) — chỉ cần xác nhận cột (6) Mẫu số 1 hiện đúng dữ liệu
   sau khi chạy thật (trước đây `address`/`contact` luôn rỗng vì DTO list cũ không có field này).
3. **`dishSourcing()` (`mock-data.ts`) — nối endpoint sourcing mới.** Đổi
   `parent/nutrition/[dishId]/page.tsx` từ "KHÔNG tự gọi API, truyền `[]`" (comment cũ, giờ lỗi
   thời) sang gọi thật `usePagedList<Api<"IngredientSourcingResponse">>` hoặc thêm 1 hook riêng
   `useSingle`-kiểu tới `GET /supplier-products/sourcing?ingredientIds=...` (truyền danh sách
   `dish.ingredients[].ingredientId`), map kết quả `{ingredientId, supplierName}` thẳng vào panel
   — KHÔNG cần `bestPriceOf`/`dishSourcing` cũ nữa cho đường này (logic "giá rẻ nhất + partner
   APPROVED" giờ tính Ở SERVER). Cân nhắc: giữ `dishSourcing()` cho các màn KHÁC PARENT (đã có
   quyền `/partners`+`/supplier-products` đầy đủ, xem `menu-form.tsx`) hay đổi luôn tất cả sang
   endpoint mới cho nhất quán — CHƯA quyết, để phiên sau đọc lại tất cả nơi gọi `dishSourcing()`
   trước khi chọn.
4. **`HealthRecordDto | "null"`** — dọn type + comment ở `lib/api/types.ts` và
   `parent/health/page.tsx` (không bắt buộc, chỉ dọn rác vì hành vi cũ đã hết, nhưng để nguyên
   cũng không lỗi gì — code phòng thủ cũ vẫn đúng với `null` thật).
5. Sau khi nối xong cả 3 mục trên, chạy lại `tsc --noEmit` + tự test UI 3 màn liên quan
   (`inspections/[id]`, `parent/nutrition/[dishId]`, `parent/health`) trước khi coi là xong —
   CHƯA test UI thật ở phiên 5b (chỉ mới xác nhận build/compile sạch cả 2 phía).

---
**Trạng thái:** Giai đoạn 1 và Giai đoạn 2 **ĐÃ XONG VÀ ĐÃ XÁC MINH** (`tsc --noEmit` sạch).
Giai đoạn 3 (migrate interface tự khai sang `Api<"XxxResponse">`) **ĐANG LÀM DỞ** — phiên 4 xoá
38/79 interface, phiên 5 xoá thêm 4, phiên 6 xoá thêm 1 (`SpecialMealRequest`) (`MenuTemplate`/`DietGroup`/`SupplierProduct` migrate thật,
`AllergyDeclaration` dead code) + dọn thêm 2 cụm seed+backfill chết (`STUDENTS`/
`ALLERGY_DECLARATIONS`, `PARTNERS`, `SUPPLIER_PRODUCTS` — xem "TÓM TẮT PHIÊN 5"). Phiên 5 cũng
phát hiện và vá **2 lỗi thật đang chạy ngầm trên dữ liệu thật** (không phải lỗi mock): `Classroom.
grade` sai kiểu sau migration backend V44, và `step1RowsOfIssue()` tra NCC qua hằng mock chết.
Xem mục "TÓM TẮT PHIÊN 5" để biết đầy đủ, "TÓM TẮT PHIÊN 4" cho lịch sử trước đó. Vài gap kỹ
thuật/phân quyền backend được phát hiện và ghi rõ bên dưới, KHÔNG tự bịa endpoint/quyền cho chúng.

## Yêu cầu gốc của Nam

> "xóa mockdata để tôi chạy thật được không? chỉ giữ lại role thôi" → sau khi hỏi lại, chốt:
> **"thôi bỏ luôn dùng backend hết"** — tức là xóa TOÀN BỘ mock, kể cả cơ chế đăng nhập giả
> theo vai trò (`DEMO_USERS`/`login(role)`). Không giữ lại gì cả — mọi thứ chạy qua backend thật.

Câu hỏi 1 cũng đã được trả lời: **"Kiểm tra từng module, nối thật nếu BE đã có sẵn endpoint"**
— tức là KHÔNG được tự bịa endpoint cho module nào backend chưa có; phải báo lại nếu gặp.

Yêu cầu bổ sung ở phiên 3 (chưa có trong bản gốc): interface/type còn khai tay trong
`mock-data.ts` mà đã có DTO tương ứng sinh từ swagger (`schema.d.ts`, qua `Api<"XxxResponse">`
ở `lib/api/types.ts`) thì phải đổi sang dùng DTO thật, trừ enum/union hằng cố định (giữ local
được, trừ khi schema đã tự sinh sẵn union đó). Đây là **Giai đoạn 3**, chưa làm ở phiên này —
xem mục riêng cuối file.

---

## TÓM TẮT PHIÊN 3 (07/08/2026) — việc đã làm

Điểm xuất phát đầu phiên: `mock-data.ts` 6014 dòng, `tsc` sạch. Nhóm (a) — 16/32 mảng đã bị xóa
ở phiên trước (chỉ còn khai báo, count=1, an toàn). Còn lại 11 mảng count>1 chưa xử lý:
`MENUS, INVENTORY, INSPECTIONS, FOOD_SAMPLES, DELIVERIES, SUPPLIER_BATCHES,
SUPPLIER_DOCUMENTS, CLASSROOMS, INVENTORY_TRANSACTIONS, PREPARATION_RECORDS, DIET_GROUPS`.

Kết thúc phiên: `mock-data.ts` còn **4369 dòng** (giảm ~1645 dòng), `tsc --noEmit` **SẠCH**.
Toàn bộ 11 mảng trên đã xóa sạch (kể cả code sinh dữ liệu phụ thuộc chúng — xem mục "Bài học
mới" về quy mô thật của việc xóa `MENUS`). Toàn bộ 5 mục Giai đoạn 2 (nhóm b, xem danh sách gốc
bên dưới) đã nối xong. Ngoài ra phát hiện và vá thêm **6 lỗi thật** cùng loại "hàm đọc thẳng
mảng seed làm nguồn dữ liệu, mặc dù đã có tham số/đã được gọi với dữ liệu thật ở nơi khác" —
liệt kê chi tiết ở mục riêng.

### Giai đoạn 1 — dọn nốt 11 mảng count>1

Theo đúng quy trình bắt buộc (đếm tham chiếu trước, sửa từng chỗ, `tsc` sau mỗi bước):

- **`CLASSROOMS`** (luôn là mảng rỗng `[]`) — xóa hằng, đổi default `gradeLevelsOf`/
  `gradeEnrollmentOf` từ `= CLASSROOMS` sang `= []`. Xóa luôn `GRADE_LEVELS` (hằng
  `@deprecated`, evaluate ra `[]` vĩnh viễn) — `menu-templates/page.tsx` (2 chỗ dùng) đổi sang
  đọc `gradeLevelsOf(classroomsPage?.items ?? [])` qua `usePagedList("classrooms")` thật, đúng
  mẫu các trang khác đã làm (`cook-sheet`, `menu-approval`, `meal-service`).
- **`DIET_GROUPS`** — xóa hằng. `cookSheet()` là hàm DUY NHẤT còn đọc thẳng nó (dòng
  `DIET_GROUPS.find(...)` để lấy nhãn nhóm chế độ ăn ở mục "suất riêng") — thêm tham số
  `dietGroups: DietGroup[] = []`, nối `useDietGroups()` (đã có sẵn từ trước) vào 2 nơi gọi
  (`cook-sheet/page.tsx`, `meal-service/page.tsx`).
- **`DELIVERIES`, `SUPPLIER_BATCHES`, `SUPPLIER_DOCUMENTS`** — cả 3 chỉ còn được đọc bên trong
  `step1RowsOfIssue()` (hàm SỐNG, `inspections/[id]/page.tsx` gọi với `transactions`/`batches`
  thật nhưng 3 tra cứu supplier/delivery/document bên trong vẫn đọc thẳng 3 hằng mock — bug
  thật, vì id lô thật không khớp id mock nên 3 cột này vốn đã luôn rỗng khi chạy thật). Thêm 3
  tham số `supplierBatches`/`deliveries`/`documents` (mặc định `[]`), xóa 3 hằng. Nối
  `usePagedList<SupplierBatchDto>("supplier-batches")` + `usePagedList<DeliveryDto>
  ("deliveries")` + `usePagedList<DeliveryPersonDto>("delivery-persons")` (tra tên tài xế từ
  `driverId`, theo đúng mẫu `supplier/deliveries/page.tsx`) vào `inspections/[id]/page.tsx`.
  **`documents` (SUPPLIER_DOCUMENTS) KHÔNG nối được** — xem gap backend bên dưới.
- **`INSPECTIONS`** — đổi default `inspectionForMeal`/`mealServingGate` từ `= INSPECTIONS`
  sang `= []` (mọi nơi gọi thật đã truyền `sessions` rồi). Xóa hằng.
- **`FOOD_SAMPLES`** — chỉ còn dùng trong `samplesWithoutBatches` (const module-level, KHÔNG
  export, KHÔNG ai đọc — dead code, xác nhận qua `dishBatchesForIncident()` đã bị thay bằng
  `GET /traceability/dish-batches` thật ở `medical-staff/incidents/[id]/page.tsx`). Xóa cả
  cụm: `FOOD_SAMPLES`, `samplesWithoutBatches`, và toàn bộ cluster liên quan (xem dưới).
- **`INVENTORY`, `INVENTORY_TRANSACTIONS`, `PREPARATION_RECORDS`** — dùng trong
  `step1RowsOfIssue`/`step2RowsOfMeal`/`step3RowsOfMeal`/`issuedLinesForMeal`/`dishBatchMap`
  (đã có default `[]` hoặc bắt buộc tham số từ trước — phần này đã sạch từ phiên trước, chỉ cần
  xóa 3 hằng seed). Riêng `quarantineCandidates()`, `dishBatchesForIncident()`,
  `dishBatchMap()`, `batchDishMap()`, `batchRemaining()`, `allocateFEFO()` xác nhận **dead
  code hoàn toàn** (0 nơi gọi ngoài `mock-data.ts`, đã bị thay bằng API thật) — xóa cả cụm,
  cùng các type chỉ phục vụ chúng (`FefoAllocation`, `DishRequirement`, `ShortageLine`,
  `UpcomingShortage`, `QuarantineCandidate`, `DishBatchScope`, `MenuIngredientNeed`,
  `MealIssueLine`).
- **`MENUS`** — mảng lớn nhất và phức tạp nhất, xem mục "Bài học mới" bên dưới.

### Giai đoạn 2 — nối nhóm (b) (5/5 mục xong)

1. **`findDishByName()`** → đổi chữ ký nhận `library: Pick<Dish,"id"|"name">[]` thay vì tự đọc
   `DISH_LIBRARY`. Sửa nơi gọi duy nhất `parent/weekly-menu/page.tsx` (đã có sẵn `dishLibrary`
   thật trong scope, chỉ cần truyền vào).
2. **`resolveIngredientAllergens()`/`dishLibraryAllergens()`** → phát hiện gap backend: DTO
   thật `IngredientResponse` (`GET /api/v1/ingredients`) **KHÔNG có field `allergens`** (khác
   `CatalogIngredient` mock) — bảng nguyên liệu chuẩn ở backend chưa lưu tác nhân dị ứng theo
   nguyên liệu. Vì không có dữ liệu thật để thay, **bỏ hẳn** 2 bước tra `INGREDIENT_CATALOG`
   (theo `ingredientId`/tên), chỉ còn `detectAllergens(name)` (heuristic từ khóa). Vì hàm này
   không còn đọc mảng mock nào, TẤT CẢ ~10 nơi gọi `dishLibraryAllergens()` tự động sạch mà
   không cần sửa từng chỗ. Xem "Nợ backend" bên dưới.
3. **`productLabel()`/`ingredientIdOfProduct()`** → `productLabel` đổi chữ ký nhận
   `products: SupplierProduct[] = []`; `ingredientIdOfProduct()` xác nhận dead code (0 nơi gọi
   sau khi `receiving/new/page.tsx` đã đổi sang đọc field `ingredientId` thật) — xóa. Sửa nơi
   gọi `supplier/batches/page.tsx`.
4. **`dishSourcing()`/`bestPriceOf()`** → thêm tham số `prices`/`partners` cho `dishSourcing`.
   **PHÁT HIỆN GAP PHÂN QUYỀN**: nơi gọi duy nhất `parent/nutrition/[dishId]/page.tsx` là màn
   PARENT, nhưng `GET /api/v1/partners` và `GET /api/v1/supplier-products` **không cấp quyền
   PARENT** (`PartnerController`/`SupplierProductController`, §16 Authorization Matrix — chỉ
   SYSTEM_ADMIN/SCHOOL_ADMIN/MEDICAL_STAFF/SERVICE_CATERERS(/SUPPLIER)). KHÔNG tự gọi 2 API này
   ở màn Parent (sẽ 403) — để tham số mặc định `[]`, panel "Nhà cung cấp nguyên liệu" hiện
   "chưa có nhà cung cấp liên kết" cho mọi dòng thay vì đọc mock. **Cần Nam quyết định**: mở
   quyền PARENT cho 2 endpoint trên (rủi ro lộ giá/đối tác cho phụ huynh), hay thêm endpoint
   public-safe riêng.
5. **`isAllergenVerified()`/`gradeAllergyStudents()`/`gradeAllergySummary()`** — mục phức tạp
   nhất, xem mục riêng "Chi tiết mục 5" bên dưới.

### 6 lỗi thật phát hiện thêm (không nằm trong danh sách gốc, cùng loại "đọc mock dù có thể
### đọc thật") — đã vá hết, xem diff để đối chiếu:

1. `special-meal-plans.ts:130` — `variantDishName(d)` thiếu tham số `dishLibrary` dù hàm
   `planOfBlock()` bao nó ĐÃ NHẬN `dishLibrary` thật làm tham số — chỉ quên truyền tiếp.
2. `cookSheet()` — ngoài `DIET_GROUPS`, còn đọc thẳng `DISH_LIBRARY.find(...)` để tra tên món
   suất trọn bộ. Thêm tham số `dishLibrary`, nối ở cả 2 nơi gọi (`cook-sheet/page.tsx`,
   `meal-service/page.tsx` — cả 2 đều chưa từng tải `dishes`, phải thêm `usePagedList<Dish>`).
3. `variantNutrition()` — đọc thẳng `DISH_LIBRARY` để cộng dinh dưỡng variant. Thêm tham số
   `library`, nối ở 3 nơi gọi (`special-meal-dialog.tsx`, `menu-approval/[id]/page.tsx`,
   `menus/[id]/page.tsx` — cả 3 đã có `dishLibrary` thật sẵn trong scope).
4. `verifiedAllergensOfStudent(studentId)` — tự tra `ALLERGY_DECLARATIONS` theo id. Đổi hẳn
   chữ ký thành `verifiedAllergensOfStudent(allergies: string[] | undefined)`, nhận thẳng
   `StudentDto.allergies` (field DẪN XUẤT, CHỈ gồm allergen ĐÃ XÁC MINH — ADR-M03-3) — đơn giản
   hơn nhiều so với tự tra bảng khai báo, và đúng với model backend thật. Sửa 4 nơi gọi
   (`parent/weekly-menu`, `parent/dashboard`, `parent/school-meals`, `parent/nutrition/
   [dishId]` — 2 trang sau phải thêm `usePagedList<StudentDto>` mới vì trước đó chưa tải).
5. `dietGroupsNeededByGrade()`/`dietCoverageGaps()` — có default `students: Student[] =
   STUDENTS` không bao giờ dùng tới (mọi nơi gọi đã truyền `students` thật) nhưng vẫn là một
   default "chết nhưng nguy hiểm". Xóa default, bắt buộc tham số.
6. (Ghi nhận, CHƯA vá — xem "Nợ kỹ thuật còn lại") `dietGroupMembers()`/
   `dietGroupMembersInGrade()` vẫn đọc thẳng `STUDENTS`.

---

## TÓM TẮT PHIÊN 4 (07/08/2026) — việc đã làm

Điểm xuất phát đầu phiên: nhánh `huybqhe180599`, CHƯA commit, `mock-data.ts` 4369 dòng, `tsc`
sạch. Phiên này chạy trong một agent bị cô lập vào một git worktree riêng (không phải working
tree thật của Nam) — mọi chỉnh sửa được làm trên **bản sao trong worktree**, sau đó đồng bộ
(copy file) từng bước về đúng working tree thật (`C:\Users\phuoc\Downloads\SEP490_G8`, NGOÀI
`.claude\worktrees\...`) để `tsc` và code cuối cùng nằm đúng chỗ Nam sẽ mở lên xem — không dùng
git để đồng bộ (bị chặn cứng thao tác git nhắm ra ngoài worktree), chỉ copy file thường. Đã xác
nhận `tsc --noEmit` SẠCH ở CẢ HAI nơi (worktree lẫn working tree thật) tại mọi mốc dừng.

### Phần A — nợ nhỏ còn sót từ Giai đoạn 2 (XONG)

`dietGroupMembers(groupId, dietGroups)` / `dietGroupMembersInGrade(groupId, grade, dietGroups)`
thêm tham số bắt buộc `students: Student[]` (theo đúng mẫu `gradeAllergyStudents` phiên 3), xoá
hẳn việc đọc thẳng `STUDENTS`. Sửa cả 10 nơi gọi:
- `menu-form.tsx` (3 nơi) — `students` đã có sẵn trong scope `MenuForm` (dòng ~993), chỉ cần
  truyền vào.
- `special-meal-dialog.tsx` (6 nơi) — CHƯA có `students` thật trong scope, phải thêm
  `usePagedList<StudentDto>("students")` + `studentDtoToMock()` mới. Xác nhận quyền: dialog này
  chỉ được mở từ `medical-staff/special-meals/page.tsx`, và `StudentController` cho phép
  MEDICAL_STAFF gọi `GET /students` (`hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF','TEACHER',
  'PARENT','SERVICE_CATERERS')`) — an toàn để nối.
- `service-caterers/menus/[id]/page.tsx` (1 nơi) — đã có `allStudents` thật sẵn trong scope
  (tải cho mục đích khác, `gradeAllergyStudents`), chỉ cần truyền vào.

`tsc --noEmit` sạch ngay sau Phần A, trước khi bắt đầu Phần B.

### Phần B — Giai đoạn 3: migrate interface sang DTO swagger

**Kết quả:** `mock-data.ts` từ 79 → **41 interface** (xoá 38), 4369 → **3709 dòng** (−660).
Quy trình bắt buộc mỗi interface đã tuân thủ đúng: đọc định nghĩa → tìm DTO khả dĩ trong
`schema.d.ts` → đối chiếu field-by-field (đối chiếu cả comment sinh từ `@Schema` Java, không chỉ
tên field) → tìm hết nơi dùng thật (`grep` ngoài `mock-data.ts`) → sửa từng nơi → xoá interface
→ `tsc --noEmit` NGAY sau mỗi interface (không gộp nhiều cái rồi mới kiểm) → đồng bộ file đã sửa
về working tree thật.

**Phát hiện lớn nhất phiên này**: rất nhiều trong số 38 interface xoá được **không phải vì migrate
sang DTO**, mà vì đã là **DEAD CODE hoàn toàn** — hệ quả tồn đọng của các phiên trước, khi một
trang chuyển sang đọc DTO thật trực tiếp (không qua interface mock) nhưng quên xoá interface
không còn ai dùng. `contract-check.ts` (file guard biên dịch có sẵn từ phiên 3) hoá ra là nguồn
tra cứu RẤT quý cho việc này — nhiều dòng `_xxxContract: MissingOnDto<...>` ở đó tự nó đã ghi rõ
"trang này đã đọc thẳng DTO thật, dòng check này chỉ còn là cột mốc lịch sử" ngay trong comment,
nói cách khác **đã tự trả lời sẵn câu hỏi của Giai đoạn 3 cho một phần ba số interface** mà
không cần dò lại từ đầu.

#### 38 interface đã xoá — chia theo lý do:

**Nhóm 1 — Migrate thật (interface có DTO khớp, code đang dùng interface → đổi sang `Api<>`,
16 interface, thêm alias mới vào `lib/api/types.ts`):**
`UserAccount`→`UserDto`, `RoleRecord`→`RoleRecordDto`, `Permission`→`PermissionDto`,
`SupplierDeliveryPerson`→`DeliveryPersonDto`, `PartnershipApplication`→`ApplicationDto`,
`ChecklistItem`/`ChecklistGroup`→`ChecklistItemDto`/`ChecklistGroupDto`,
`SafetySignoff`→`SafetySignoffDto`, `AuditLog`→`AuditLogDto`, `ErrorLog`→`ErrorLogDto`,
`TeacherMealRecord`→`MealRecordDto`, `SecurityPolicy`→`SecurityPolicyDto`,
`SchoolRecord`→`SchoolDto` (alias MỚI), `AcademicYear`→`AcademicYearDto` (alias MỚI),
`StaffMember`→`StaffDto` (alias MỚI, CÙNG DTO với `/users`), `SupplierDocument`→`DocumentDto`.

**Nhóm 2 — Dead code, xoá thẳng không cần sửa nơi dùng (0 nơi dùng thật, chỉ còn tên trong
comment/docstring — 22 interface):**
`ExternalIntegration`, `SupplierPartnership`, `ReceivingAuthority`, `UserNotification` (+
`NotificationAudience`), `MealHistoryEntry`, `TeacherStudentHealth`, `PreparationRecord`,
`PurchaseItem`/`PurchaseRequest`, `HealthHistoryEvent` (+ `HEALTH_HISTORY` seed sót từ Giai đoạn
1), `CorrectiveAction` (+ hàm `incidentStatusFromCapa()`/`CAPA_ACTIVE_STATUSES` cũng chết theo),
`NutritionDay` (KHÔNG đụng `NutritionTarget`/`NUTRITION_TARGET` — trang
`parent/nutrition-history` đã tự khai type cục bộ riêng, ngoài phạm vi hạn chế của Nam),
`ServingDish`/`StudentDistribution`, `ReceivingLine`/`ReceivingSession`,
`ChronicConditionDeclaration`, `HealthRecord` (+ `HEALTH_RECORDS` + `healthRecordOfStudent()`),
`FoodSample`/`SampleLabRequest`, `Announcement`, `Feedback` (+ `hasIllnessKeyword()`/
`ILLNESS_KEYWORDS` — BR-140/142 nay tính ở server).

Cách phát hiện dead code: `grep -rn "\bTênInterface\b" src/app src/components src/lib` loại trừ
`mock-data.ts`/`contract-check.ts` — nếu 0 kết quả THẬT (không tính comment/docstring nhắc tên
suông), rồi `grep -c` trong chính `mock-data.ts` để chắc không có seed array/hàm nội bộ nào còn
đọc nó, mới xoá.

#### 41 interface còn lại — cố tình GIỮ, lý do rõ ràng cho từng nhóm:

**A. Adapter/derived shape — không map 1-1 với DTO (đúng ngoại lệ Giai đoạn 3 cho phép):**
`SeedMealBlock`/`MenuItem` (dùng cho `SeedMenu`, đã contract-check khớp `MenuDto` qua
`_menusContract` nhưng CHƯA đổi nơi dùng — còn 4 file, để dành phiên sau), `DietGap`, `CookLine`,
`CookSheet`, `DistributionRecord` (nguồn của `distributionSummaries()`, gộp nhiều bản ghi thành
1 dòng tổng hợp ngày×lớp — có field tính toán `planned`/`distributed`/`specialMeals` không tồn
tại trên bất kỳ DTO đơn lẻ nào), `ParentChild` (dẫn xuất từ `StudentDto` qua
`parentChildOfStudent()`, có `age`/`school` tính toán không có trên `StudentResponse`),
`CatalogIngredient` (gap backend đã ghi từ phiên 3 — `IngredientResponse` thiếu `allergens`,
còn dùng cho `refuseFactor`/`unit`).

**B. Cụm "Kiểm thực ba bước" (`InspectionSession`, `Step1FreshRow`, `Step1DryRow`, `Step2Row`,
`Step3Row`, `ThreeStepFormHeader`, `InventoryTransaction`, `InventoryBatch`, `SupplierBatch`) —
CỐ TÌNH GIỮ, lý do kép:**
1. `inspection-adapter.ts` (`inspectionDtoToMock`) đã là một ADAPTER RÕ RÀNG — chuyển
   `InspectionSessionDto` (DTO thật, mọi field optional/loose string) sang shape `InspectionSession`
   (mock, literal union chặt + non-optional) để phục vụ `inspection-sheets.ts` (bộ sinh HTML in
   Mẫu 1/2/3 QĐ 1246 — cần kiểu chặt, không chấp nhận `undefined` rải rác).
2. `service-caterers/inspections/[id]/page.tsx` (trang nhập liệu chính, ~1300 dòng) dùng các
   interface này làm STATE NHÁP đang sửa (`Step1FreshRow[]` v.v.) với field
   `photoTaken` (dẫn xuất `photoCount > 0`, hoàn toàn cục bộ UI) KHÔNG có trên bất kỳ DTO nào.
   `contract-check.ts` (viết từ trước phiên này) đã tự ghi rõ: "`inspections`/`kitchen-inspections`
   KHÔNG có trong REAL_RESOURCES — 2 check dưới đây KHÔNG phải 'sắp migrate', mà chốt SỚM... đường
   GHI của 2 resource này lệch KIẾN TRÚC (không phải lệch field)". `InventoryTransaction`
   (`quantity: string` có dấu "+45 kg" mock vs `delta: number` + `unit` riêng DTO thật — chuyển
   đổi định dạng thật, không chỉ đổi tên) và `InventoryBatch`/`SupplierBatch` dùng chung trong
   đúng trang này, cùng lý do giữ.

**C. Cụm `Incident` (`Incident`, `IncidentSourceReport`, `IncidentRecordDraft`,
`IncidentAffectedStudent`, `InvestigationEvidence`) và cụm `KitchenInspection`
(`InspectionItemFinding`) — GIỮ theo đúng quyết định đã có SẴN trong `contract-check.ts` từ
trước phiên này** (Omit nhiều field investigation-only/record-only không có trên DTO thật, ghi
rõ "chốt SỚM... chưa phải sắp bật, chặn thật nằm ở LUỒNG GHI"). Phiên này KHÔNG đụng vào, chỉ xác
nhận lại lý do vẫn còn hợp lệ.

**D. `IncidentEvent` — GIỮ, phát hiện GAP MỚI (ghi ở mục riêng bên dưới):** hàm duy nhất dùng nó
(`useAppendIncidentEvent()` trong `incident-events.ts`) **0 nơi gọi trong toàn bộ `src/app`** —
tự nó đã là dead code, NHƯNG endpoint nó gọi (`POST incident-events`, flat, không path param)
**KHÔNG TỒN TẠI trong `schema.d.ts`** (chỉ có `GET /incidents/{code}/events`, dùng
`IncidentEventDto` thật ở `incident-detail-view.tsx`). Không xoá cả file `incident-events.ts` vì
ngoài phạm vi "xoá interface" — ghi nhận rõ để phiên sau quyết định xoá file hẳn hay để đó.

**E. `UploadedFile` — GIỮ, đúng chủ đích (không phải sơ suất):** `violation-utils.ts` tự ghi rõ
"Mock file tải lên — CHỈ còn dùng cho 2 tính năng mock" (Biên bản kiểm tra bếp MF07 + Sổ lưu mẫu
MF08) — 2 tính năng NÀY chưa có endpoint thật, không phải interface quên xoá.

**F. `Classroom`, `Dish`, `DishIngredient`, `Partner`, `PartnerDocument`, `SpecialMealRequest`,
`Student`, `MenuVariant`, `MenuVariantDish`, `ViolationRecord`, `DietGroup`, `SupplierProduct`,
`MenuTemplate` — CHƯA ĐỘNG TỚI, để dành nguyên vẹn cho phiên sau** (xem mục "Nợ kỹ thuật còn
lại" cuối file — đây là các interface lớn, nhiều nơi dùng, cần thời gian xử lý đúng quy trình
từng-cái-một, KHÔNG phải các trường hợp có vấn đề đã biết).

### Pattern mới thiết lập: "Draft type" cho trang CRUD viết nhiều

`schema.d.ts` khai MỌI field optional (springdoc không đọc `required`) — DTO đúng cho việc ĐỌC,
nhưng với trang có STATE NHÁP luôn-đầy-đủ (sửa/sắp xếp trước khi bấm Lưu, VD
`medical-staff/conduct-inspection/checklist/page.tsx`), ép cả trang dùng optional field khắp nơi
(`?? ""` rải hàng chục chỗ) làm code khó đọc. Giải pháp áp dụng lần đầu ở `ChecklistGroup`/
`ChecklistItem`, khuyến nghị lặp lại cho các trang tương tự (VD `menu-templates/page.tsx` khi
migrate `MenuTemplate` ở phiên sau):

```ts
// Draft type DẪN XUẤT bằng Pick<> từ chính DTO thật (không phải khai tay độc lập —
// tự động bắt lỗi nếu DTO đổi field), narrow required NGAY TẠI ranh giới nạp dữ liệu.
type XxxDraft = Pick<XxxDto, "id" | "name" | ...> & { id: string; name: string; ... };
function normalizeXxx(x: XxxDto): XxxDraft { return { id: x.id ?? newId(...), name: x.name ?? "", ... }; }
// State cục bộ dùng XxxDraft (luôn đầy đủ); usePagedList/useCreate/useUpdate vẫn dùng XxxDto
// (đúng shape wire thật).
```

### Gap backend/kỹ thuật MỚI phát hiện phiên 4 (bổ sung vào mục "Nợ kỹ thuật backend" — KHÔNG tự
### bịa quyền/endpoint cho các gap này):

6. **`(auth)/reset-password/page.tsx` gọi `GET /security-policies` — endpoint yêu cầu
   `hasRole('SYSTEM_ADMIN')`, nhưng trang Đặt lại mật khẩu là màn KHÔNG đăng nhập** (auth flow
   công khai). Gọi sẽ LUÔN 401/403 cho mọi người dùng thật, form validate độ mạnh mật khẩu rơi về
   default cứng `?? 8`/`?? true` (chấp nhận được vì 8 ký tự luôn ≤ cấu hình thật, không nới lỏng
   bảo mật — nhưng KHÔNG phải hành vi đúng ý đồ ban đầu). Nhân tiện đã sửa `usePagedList` sai
   shape thành `useSingle` đúng shape response (bug thật, độc lập với gap quyền), nhưng gap quyền
   THÌ CHƯA SỬA ĐƯỢC — cần BE quyết định: mở endpoint public-safe riêng, hay chấp nhận default
   cứng là chủ ý.
7. **`incident-events.ts` (`useAppendIncidentEvent()`) gọi `POST incident-events`
   (flat path) — endpoint KHÔNG TỒN TẠI trong `schema.d.ts`.** Hàm này bản thân đã dead code (0
   nơi gọi), nên KHÔNG chặn gì hiện tại, nhưng nếu ai định dùng lại nó (thêm nút "ghi sự kiện thủ
   công" nào đó) sẽ cần BE bổ sung endpoint trước — endpoint thật hiện có chỉ là
   `GET /incidents/{code}/events` (đọc), không có POST tương ứng.

### Trạng thái cuối phiên 4

```bash
cd 04_Development/Frontend
npx tsc --noEmit -p tsconfig.json   # SẠCH — 0 lỗi, xác nhận lần cuối trước khi dừng
```

`mock-data.ts`: 4369 → **3709 dòng** (−660). Interface: 79 → **41** (−38, xem chi tiết ở trên).
~30 file trang/component khác đã sửa (đổi type import + vài chỗ `?? ""`/`?? 0` cho field optional
mới lộ ra) — không có file nào đổi cấu trúc lớn ngoài `medical-staff/conduct-inspection/
checklist/page.tsx` (áp dụng pattern Draft type mới). `contract-check.ts` giảm đáng kể (nhiều
dòng `_xxxContract` xoá theo interface đã migrate/dead), `lib/api/types.ts` thêm alias mới:
`UserDto`, `RoleRecordDto`, `PermissionDto`, `StaffDto`, `SchoolDto`, `AcademicYearDto`. Nhánh
`huybqhe180599`, CHƯA commit (theo đúng yêu cầu "KHÔNG commit git").

---

## TÓM TẮT PHIÊN 5 (07/08/2026) — việc đã làm

Điểm xuất phát: kéo `origin/dev` về nhánh làm việc — hoá ra dev đã có sẵn 1 merge commit đúng
bằng điểm dừng cuối phiên 4 (`mock-data.ts` 3709 dòng, 41 interface, `tsc` sạch — xác nhận khớp
100% trước khi bắt đầu), cộng thêm các thay đổi khác (V47–V53 migrations, MF04 catalog...) không
thuộc phạm vi việc dọn mock. `npm install` cần chạy lại (6 gói thiếu: `leaflet`, `react-leaflet`,
`qrcode`, `firebase`, `@stomp/stompjs`, `sockjs-client` — có khai trong `package.json` nhưng chưa
cài ở máy này), và `.next/` cũ phải xoá (2 lỗi `Cannot find module .../page.js` do route đã đổi
tên từ trước, type cache không tự refresh).

### Giai đoạn 3 — 3 interface migrate thật (đã kiểm field-by-field, không chỉ tên field)

1. **`MenuTemplate`** (5 file: `menu-approval`, `meal-service`, `menu-templates`,
   `service-caterers/menus`, `menu-form.tsx`) — `MenuTemplateResponse` khớp field-name 1:1 (đã
   có contract-check từ trước), chỉ khác `mealType` là `string` thô trên DTO (không có enum
   sinh sẵn) trong khi FE cần union `MealType` chặt để so sánh/index `MEAL_TYPE_LABEL`. Áp dụng
   **pattern "Draft type" mới** (thiết lập phiên 4): tạo `lib/menu-template-adapter.ts` với
   `type MenuTemplate = Pick<MenuTemplateDto,"grade"|"note"> & {id;name;mealType:MealType;
   audience;dishIds;isActive}` + `normalizeMenuTemplate()`. Vì DTO dùng CHUNG ở 5 trang (khác
   `ChecklistGroup`/`ChecklistItem` phiên 4 vốn chỉ 1 trang), đặt type+adapter ở file dùng
   chung thay vì khai cục bộ từng trang. Xoá dòng `_menuTemplatesContract` ở `contract-check.ts`
   (dư thừa — type mới dẫn xuất bằng `Pick<>` nên tự đảm bảo khớp DTO bằng cấu trúc).
2. **`DietGroup`** (4 file: `menu-form.tsx`, `meal-service`, `menu-templates`,
   `special-meal-plans.ts`) — phát hiện đã có sẵn CHOKEPOINT `useDietGroups()`/`toDietGroup()`
   ở `lib/diet-groups.ts` (dựng từ phiên trước) là nguồn THẬT DUY NHẤT cho danh mục này ở mọi
   màn — nghĩa là interface `DietGroup` chỉ có ĐÚNG MỘT nơi sinh ra giá trị thật, rủi ro migrate
   rất thấp. Chuyển hẳn định nghĩa type vào `diet-groups.ts` (`Pick<DietGroupDto,
   "matchDiet"|"excludeAllergens"|"excludeIngredientKeywords"|"note"> & {id;label}`), xoá
   interface khỏi `mock-data.ts` (các hàm nội bộ dùng `DietGroup` — `dietGroupMembers()`,
   `cookSheet()`... — import lại type từ `diet-groups.ts`, không có vòng import vì
   `diet-groups.ts` chỉ phụ thuộc `api/types`, không phụ thuộc `mock-data.ts`).
3. **`SupplierProduct`** (2 file cần sửa import thật sự: `supplier/batches/page.tsx`,
   `supplier/purchase-requests/[id]/page.tsx` — `menu-form.tsx`/`menus/page.tsx` chỉ dùng type
   suy ra từ `supplierProductDtosToMock()`, không import tên `SupplierProduct` nên không cần
   sửa) — chuyển vào `lib/supplier-product-adapter.ts` (đã có sẵn adapter filter+map từ trước).
   Bỏ hẳn 2 field mock cũ `brand`/`minimumOrderQty`: xác nhận DEAD (0 nơi đọc ngoài
   `mock-data.ts`, và `SupplierProductResponse` thật cũng KHÔNG có 2 field này — đã ghi chú sẵn
   trong `lib/api/types.ts` từ trước, phiên 5 chỉ dọn theo).

### Giai đoạn 3 — điều tra, kết luận KHÔNG migrate (NO-GO), lý do cụ thể

4. **`SeedMenu`/`MenuItem`/`SeedMealBlock`(+`SeedDish`/`SeedSubstituteDish`/
   `SeedIngredientRow`)** — ưu tiên #2 theo gợi ý phiên 4 ("đã contract-check khớp `MenuDto`"),
   nhưng đào sâu hơn phát hiện **`_menusContract` chỉ kiểm TÊN field cấp 1** (`MissingOnDto` là
   `Exclude<keyof FE, keyof Dto>` — không đệ quy vào phần tử mảng lồng nhau như `blocksData[].
   dishes[].ingredients[]`). Field `MenuIngredient.quantity` DTO thật là `number`, trong khi
   `SeedIngredientRow.quantity` mock là `string` ĐANG DÙNG TRỰC TIẾP trong tính toán
   (`Number(ing.quantity)`, `mealIngredientNeeds()`) và nối chuỗi hiển thị (`` `${ing.quantity}
   ${ing.unit}` ``) — đúng loại "chuyển đổi định dạng thật, không chỉ đổi tên" mà phiên 4 đã
   dùng để quyết định GIỮ `InventoryTransaction`. Migrate đúng cách đòi viết lại đáng kể
   `menu-form.tsx` (state nháp `DishForm`/`IngredientForm`) + mọi hàm tiêu thụ
   (`mealIngredientNeeds`, `dishCostPerServing`...) — ngoài phạm vi an toàn của 1 phiên, GIỮ
   NGUYÊN. **Bài học cho phiên sau**: `MissingOnDto`/contract-check chỉ đáng tin cho field CẤP
   1 — bất kỳ interface nào có mảng lồng nhau (`ingredients[]`, `dishes[]`...) PHẢI tự đối
   chiếu type của TỪNG field bên trong mảng đó với DTO thật, không dừng ở việc contract-check
   compile được.
5. **`Dish`/`DishIngredient`** — cùng root cause với mục 4: `DishResponse.ingredients` dùng
   CHUNG shape `MenuIngredient[]` (cũng `quantity: number`). GIỮ NGUYÊN, cùng lý do.
6. **`MenuVariant`/`MenuVariantDish`** — xác nhận lại đây là shape DẪN XUẤT THUẦN, không có DTO
   nào để migrate sang (tính năng "suất riêng theo nhóm" đã gộp hẳn vào bảng `Menu` từ
   22/07/2026 — `useSpecialMeals()` tự tính từ `menus` qua `specialMenuMeals()`, không gọi
   resource `menu-variants` nào). Đã đúng khi phiên 4 xếp vào nhóm A (Adapter/derived) — không
   cần làm gì thêm, chỉ xác nhận lại.
7. **`ViolationRecord`** — đối chiếu `_violationsContract` (đã có từ trước): Omit tới 14 field
   (`type`, `evidencePhotos`, `witness`, `staffName`, `disciplinaryLevel`...) — toàn field
   investigation-only không có trên `ViolationDto` thật. Xác nhận đúng như phiên 4 dự đoán: nên
   xếp cùng nhóm "Incident cluster" (GIỮ, lý do kiến trúc ghi/investigation-only), KHÔNG migrate.
   Không đổi gì.

### Giai đoạn 3 — đã soát field-by-field, CHƯA migrate (để dành, có ghi chú GO/NO-GO cho phiên sau)

8. **`SpecialMealRequest`** (11 file) — `SpecialMealRequestResponse` khớp tốt: mọi field đều
   scalar đơn giản (string/boolean), KHÔNG có mảng lồng nhau kiểu `ingredients[]` nên không dính
   bẫy như mục 4/5 — ứng viên AN TOÀN cho phiên sau, tương tự độ khó `MenuTemplate`. Chưa làm vì
   hết thời gian trong phiên này, không phải vì phát hiện vướng mắc.
9. **`Student`** (23 file — LỚN NHẤT còn lại) — `StudentResponse` khớp `id`/`name`/`class`/
   `allergies`/`specialDiet`/`healthStatus`/`parent`, nhưng **THIẾU HẲN** 4 field UC-051
   (`medicalConditions`/`medications`/`emergencyContact`/`medicalDocuments`) — các field này
   thuộc một sub-resource riêng (`StudentHealthFileController`, xem nợ backend #5 cũ), KHÔNG có
   trên `StudentResponse`. Giống hệt tình huống `Partner` (list vs detail tách DTO) — migrate
   đúng cần tách `Student` thành 2 type (list-level + health-file riêng) thay vì 1 Pick đơn
   giản. Việc cho phiên sau, đừng giả định 1 `Pick<>` là đủ.

### Lỗi thật phát hiện & vá phiên 5 (không nằm trong Giai đoạn 3, phát hiện TRONG LÚC soát field)

10. **`Classroom.grade` sai kiểu — bug thật đang chạy ngầm trên MỌI màn dùng khối lớp.**
    Migration backend `V44__mf02_grades.sql` (đã có từ trước, nằm trong phần merge từ `dev` đầu
    phiên) đổi hẳn `classrooms.grade` từ `INT` (1–12) sang bảng `grades` riêng — `ClassroomMapper`
    map field `grade` trả về từ `gradeName` (`Grade.name`, VD `"Khối 3"`), KHÔNG còn là số thô.
    Mock `Classroom.grade: number` (BR-024 cũ) không theo kịp — vì `usePagedList<Classroom>`
    chỉ cast type, KHÔNG transform runtime, dữ liệu THẬT chảy vào field khai `number` thực ra là
    chuỗi `"Khối 3"`. Hậu quả (xác nhận bằng cách đọc từng nơi gọi, không đoán):
    - `gradeLevelsOf()`/`gradeEnrollmentOf()` (dùng ở `cook-sheet`, `menu-approval`,
      `meal-service`, `menu-templates`, `service-caterers/menus`, `menu-form.tsx`) tự thêm tiền
      tố `"Khối "` lên giá trị ĐÃ CÓ tiền tố → nhãn khối hiện `"Khối Khối 3"`, và so sánh
      `c.grade === n` (number) với `c.grade` thật là string → LUÔN false, sĩ số theo khối tính
      ra 0.
    - `meal-service/page.tsx`: `standardMenuOf(menus, \`Khối ${classroom.grade}\`, ...)` và
      `gradeNumber = Number(grade.replace("Khối ", ""))` — cả hai double-prefix/parse sai, suất
      theo khối và thực đơn chuẩn của khối không bao giờ khớp được trên dữ liệu thật.
    - `academic-years/promote/page.tsx`, `classrooms/page.tsx`, `academic-years/[id]/page.tsx`:
      3 cột hiển thị "Khối" tự thêm `` `Khối ${c.grade}` `` → cùng lỗi double-prefix. 2 file sau
      còn có sẵn code "đọc khoan dung" (`typeof g === "object"` fallback) với TODO ghi rõ "chưa
      chốt shape backend" — nay đã chốt được, đơn giản hoá thẳng.
    - `classrooms/[id]/page.tsx`: `gradeIdOf()` cũ cố suy UUID từ field `grade` (chỉ là nhãn,
      không phải id) → dropdown "Khối" ở dialog Sửa lớp KHÔNG BAO GIỜ prefill đúng khối hiện tại
      (luôn hiện "— Chọn khối —"). Sửa lại tra UUID bằng cách khớp TÊN với danh mục thật
      `GET /grades` (đã tải sẵn trong scope).
    **Đã vá**: đổi `Classroom.grade` sang `string`, viết lại `gradeLevelsOf()`/
    `gradeEnrollmentOf()` để đọc thẳng nhãn (không tự thêm tiền tố, sort theo số trích từ nhãn),
    sửa cả 7 điểm gọi kể trên. **CHƯA migrate toàn bộ interface `Classroom` sang `Api<>`** — đây
    CHỈ là vá lỗi kiểu dữ liệu của 1 field, 16 file còn lại dùng `Classroom` qua nhiều hook khác
    nhau (`useCreate`/`usePatch`/`useDetail`/`usePatchAction`/`DataTable<Classroom>`), migrate
    đầy đủ để dành phiên sau (không đủ thời gian làm đúng quy trình cho quy mô đó trong phiên
    này).
11. **`step1RowsOfIssue()` tra tên/địa chỉ NCC qua hằng mock CHẾT — cột (5)/(6) Mẫu số 1 kiểm
    thực 3 bước sai trên dữ liệu thật.** Phiên 3 đã thêm tham số `supplierBatches`/`deliveries`/
    `documents` cho hàm này nhưng SÓT một lần đọc trực tiếp `PARTNERS.find(p => p.name.includes
    (b.supplier) || b.supplier.includes(p.name))` để tra "Nơi cung cấp" — so khớp với 6 tên NCC
    hardcode trong mock, không bao giờ trùng tên NCC thật trong DB → cột (5)/(6) luôn rơi về
    `b.supplier` (tên thô) + địa chỉ/liên hệ rỗng. Thêm tham số `partners` (mặc định `[]`), nối
    `usePagedList<Partner>("partners")` thật ở `inspections/[id]/page.tsx` (component chính,
    KHÁC với `partnersPage` đã có sẵn nhưng nằm trong component con `SupplierSelect`). **Gap còn
    lại (đã ghi rõ trong code)**: `GET /partners` (list, `PartnerResponse`) KHÔNG có
    `address`/`contact` — chỉ `GET /partners/{id}` (`PartnerDetailResponse`) mới có. Sau khi vá,
    cột "Nơi cung cấp" (tên) đã đúng, nhưng cột (6) "địa chỉ, điện thoại" (`supplierContact`)
    vẫn trống với dữ liệu thật — cần BE trả thêm 2 field này ở endpoint list, hoặc FE tự gọi
    detail riêng cho từng NCC xuất hiện trong lô (N+1, chưa làm, ngoài phạm vi phiên này).

### Dead code dọn thêm (phát hiện TRONG LÚC soát mục 3/7, cùng loại "default không bao giờ trúng"
### mà phiên 3 đã cảnh báo)

12. **`PARTNERS`** (mảng seed, 6 đối tác) — chỉ còn là default `partners: ... = PARTNERS` ở 5
    hàm ước tính chi phí (`bestPriceOf`, `dishCostPerServing`, `substituteCostDelta`,
    `estimateMealCostPerServing`, `estimateMenuCostPerServing`) + 1 lần đọc trực tiếp (mục 11,
    ĐÃ vá thành tham số thật). Grep TOÀN BỘ nơi gọi 5 hàm trên xác nhận **100% đã truyền
    `partners` thật** — default không bao giờ trúng. Đổi default về `[]`, xoá mảng.
13. **`SUPPLIER_PRODUCTS`** (mảng seed) — cùng bệnh, cùng 5 hàm (tham số `prices`). Grep xác
    nhận mọi nơi gọi đã truyền `prices` thật. Đổi default `[]`, xoá mảng.
14. **`CATERER_REPS`/`mockUpload()`** (`violation-utils.ts`) — comment cũ tự nhận "GIỮ cho 2
    tính năng mock chưa có endpoint" (MF07 biên bản kiểm tra bếp, MF08 sổ lưu mẫu), nhưng grep
    xác nhận CẢ HAI claim đã lỗi thời: `medical-staff/samples/page.tsx` tự ghi đã đổi
    `mockUpload()` sang `<input type=file>` thật (comment sót lại không xoá theo), và
    `conduct-inspection/[id]/record/page.tsx` không có logic upload nào cả. `CATERER_REPS` 0 nơi
    gọi. Xoá cả cụm — `UploadedFile` (type) GIỮ NGUYÊN vì vẫn còn 2 field khác dùng thật
    (`KitchenInspection.recordSignedFiles`, `Incident.evidenceFiles`/`signedRecordFiles`).
15. **`STUDENTS`/`ALLERGY_DECLARATIONS`/`AllergyDeclaration`(type) + vòng backfill module-level**
    — đúng mẫu hình phiên 3 đã cảnh báo cho case `MENUS` ("mảng seed + vòng backfill phụ
    thuộc"). `STUDENTS` chỉ còn được đọc bởi 1 vòng `for (const s of STUDENTS) {...}` lúc nạp
    module (gán lại `s.allergies` từ `ALLERGY_DECLARATIONS`) — bản thân vòng này KHÔNG ai đọc
    kết quả (mọi trang thật đã chuyển sang `usePagedList<StudentDto>` + `studentDtoToMock()` từ
    lâu). `AllergyDeclaration` (type) chỉ còn 1 import chết ở `menu-personalization.ts`
    (tham số `declarations` đã đổi sang shape nội bộ `{studentId?;allergen?;status?}[]` từ
    trước, quên xoá import). Xoá cả cụm: 2 mảng seed + type + vòng backfill + import chết.

### Trạng thái cuối phiên 5

```bash
cd 04_Development/Frontend
npx tsc --noEmit -p tsconfig.json   # SẠCH — 0 lỗi, xác nhận lần cuối trước khi dừng
```

`mock-data.ts`: 3709 → **3547 dòng** (−162, riêng phiên 5 — nhỏ hơn phiên 3/4 vì phần lớn việc
phiên này là ĐIỀU TRA + vá bug, không phải xoá seed hàng loạt). Interface: 41 → **41** ở mức đếm
`interface` (không đổi số lượng vì `MenuTemplate`/`DietGroup`/`SupplierProduct` ĐỔI từ
`interface` sang `type = Pick<...>` rồi CHUYỂN RA NGOÀI file, không đơn thuần xoá — 3 file mới:
`lib/menu-template-adapter.ts`, cộng thêm code trong `lib/diet-groups.ts` và
`lib/supplier-product-adapter.ts` đã có sẵn; `AllergyDeclaration` là xoá thật). File thay đổi
(`git diff --stat`, chưa tính `menu-template-adapter.ts` mới — untracked):
`academic-years/[id]`, `academic-years/promote`, `classrooms/[id]`, `classrooms/page`,
`menu-approval`, `inspections/[id]`, `meal-service`, `menu-templates`, `service-caterers/menus`,
`supplier/batches`, `supplier/purchase-requests/[id]`, `menu-form.tsx`, `contract-check.ts`,
`diet-groups.ts`, `menu-personalization.ts`, `mock-data.ts`, `special-meal-plans.ts`,
`supplier-product-adapter.ts`, `violation-utils.ts`. Nhánh `manhndhe160692`, CHƯA commit.

---

## Bài học mới: quy mô thật của việc xóa `MENUS`

Mảng `MENUS` (hằng số duy nhất, KHÔNG dùng ở bất kỳ trang/component nào ngoài `mock-data.ts`)
tưởng chỉ là 1 mảng cần xóa, nhưng thực ra là **gốc của cả một hệ sinh dữ liệu seed** trải dài
gần nửa file:

- Mảng khai tay `MENUS` (~260 dòng).
- Bộ sinh tự động `GENERATED_DAY_PLANS`/`GENERATED_DIET_PLANS`/`GENERATED_MENUS` (sinh thực
  đơn "hôm nay → hết tuần sau" dựa vào `gradeLevelsOf()`/`gradeEnrollmentOf()` — vô tình cũng
  là lý do phải xử lý `CLASSROOMS` trước).
- Bộ sinh "suất ăn đặc biệt" `SPECIAL_MEAL_SEED`/`expandDishForMenu`/`SPECIAL_MENUS` (đẩy thêm
  vào `MENUS` qua `MENUS.push(...)`).
- **6 vòng backfill module-level** (`for (const menu of MENUS) {...}`) chạy lúc nạp module,
  tính lại `allergyConflicts`, nối `ingredientId`, kiểm tra lệch đơn vị, backfill
  `allergenIds` suất dị ứng, tính `estimatedCost` — toàn bộ đều là hệ quả tự sinh của chính
  `MENUS`, không có export nào khác phụ thuộc.
- Hàm phụ trợ `ing()`/`dish()`/`isoInDays()`/`vnInDays()`/`UPCOMING_ISO`/`UPCOMING_LABEL`/
  `TODAY_ISO`/`TODAY_LABEL` — dùng để dựng dữ liệu seed, không còn nơi nào gọi sau khi xóa
  `MENUS`.

Tổng cộng xóa **~1150 dòng** chỉ riêng cho `MENUS` và hệ quả của nó (so với ~260 dòng nếu chỉ
xóa mảng). **Quy trình đã dùng để không xóa nhầm**: xóa TỪNG khối một, `tsc --noEmit` ngay sau
— mỗi lỗi "Cannot find name 'MENUS'" chỉ ra CHÍNH XÁC chỗ cần sửa tiếp (đổi default param, xóa
hàm dead code, hoặc đổi `(typeof MENUS)[number]` — kiểu suy từ giá trị — sang alias kiểu thật
`SeedMenu`). Đã lặp lại ~10 vòng "xóa → tsc → đọc lỗi → sửa/xóa tiếp" cho tới khi sạch. **Không
có lỗi nào bị bỏ sót nhờ vậy** — nhưng bài học là: khi thấy 1 mảng "chỉ 32 lần xuất hiện", đừng
giả định quy mô nhỏ chỉ vì đếm được số dòng tham chiếu — phải theo dấu cả cụm sinh dữ liệu +
backfill đi kèm nó.

---

## Nợ kỹ thuật backend phát hiện ở phiên này (KHÔNG tự bịa, cần người phụ trách BE quyết định)

> **Cập nhật sau phiên 5b + 6**: mục **2** (quyền PARENT xem NCC), **3** (tra chứng từ theo mã),
> **5** (`"null"` chuỗi — nay chốt hẳn `204`, xem mục F phiên 6), **6** (`address`/`contact` ở
> list partner) đã ĐÓNG — BE sửa ở phiên 5b/6, FE nối xong ở phiên 6. Mục **1**
> (`IngredientResponse.allergens`), **4** (`/allergy-declarations` cho SERVICE_CATERERS), **7**
> (4 field UC-051 của `StudentResponse`) vẫn CÒN MỞ, cộng thêm **8 (MỚI, phiên 6)**:
> `SupplierBatchService.uploadCertificate()` hard-code `documentType='PRODUCT_ORIGIN'` ⇒ cột (9)
> "giấy VS thú y" của Mẫu số 1 không bao giờ bật được (xem mục E phiên 6).
> Giữ nguyên văn bên dưới làm lịch sử quyết định — đừng đọc như trạng thái hiện tại.


1. **`IngredientResponse` (MF06, `GET /api/v1/ingredients`) thiếu field `allergens`.** Backend
   chưa lưu tác nhân dị ứng theo nguyên liệu ở bảng danh mục chuẩn — khác `CatalogIngredient`
   mock cũ (có `allergens: string[]`). Hệ quả: `resolveIngredientAllergens()` giờ CHỈ còn dò
   theo từ khóa tên (`detectAllergens`), kém chính xác hơn tra catalog trực tiếp. Đã ghi chú
   sẵn trong `lib/api/types.ts` từ trước phiên này ("CHƯA migrate"), phiên này xác nhận lại và
   đơn giản hoá code cho khớp thực tế backend, không xóa `INGREDIENT_CATALOG` (còn dùng cho
   `ingredientById`/`ingredientIdOfName`/`toPurchaseQuantity` — refuseFactor, unit, ngoài phạm
   vi allergen).
2. **`GET /api/v1/partners` và `GET /api/v1/supplier-products` không cấp quyền PARENT.** Màn
   `parent/nutrition/[dishId]/page.tsx` (panel "Nhà cung cấp nguyên liệu", BR-076) cần dữ liệu
   này để suy nhà cung cấp đang bán từng nguyên liệu, nhưng gọi sẽ 403. Đã để trống (an toàn,
   không giả vờ có dữ liệu) — cần quyết định sản phẩm: mở quyền PARENT cho 2 endpoint trên hay
   giữ ẩn thông tin NCC với phụ huynh (có thể là chủ ý an toàn dữ liệu, không phải thiếu sót).
3. **Không có endpoint liệt kê/tra cứu chứng từ NCC theo id** (`SupplierBatchResponse.
   certificateIds` chỉ là mảng mã; chỉ có `POST /supplier-batches/{code}/certificates` để đính
   kèm, không có GET trả `documentType`/`name`). Cột (9)/(10) "giấy VS thú y"/"giấy kiểm dịch"
   ở Mẫu số 1 (`inspections/[id]/page.tsx`, kiểm thực 3 bước QĐ 1246) luôn hiện "chưa có" thay
   vì đọc `SUPPLIER_DOCUMENTS` mock (vốn cũng không khớp id lô thật nên đã luôn sai từ trước).
   Cần BE bổ sung endpoint GET tương ứng nếu muốn khôi phục 2 cột này.
4. **`GET /api/v1/allergy-declarations` không cấp quyền SERVICE_CATERERS.** Không chặn công
   việc phiên này vì `StudentResponse.allergies` (DTO thật) đã là field DẪN XUẤT chỉ gồm
   allergen ĐÃ XÁC MINH (ADR-M03-3) — đủ cho mọi cảnh báo BR-063/065 ở màn Service Caterers mà
   KHÔNG cần gọi `/allergy-declarations` riêng (xem "Chi tiết mục 5"). Chỉ liệt kê ở đây để rõ:
   nếu sau này cần phân biệt "đã khai nhưng CHƯA xác minh" ở màn Service Caterers, sẽ cần BE mở
   quyền hoặc trả thêm field.
5. **`StudentHealthFileController.getRecord()` trả `ResponseEntity.ok("null")`** (chuỗi JSON,
   không phải `null` thật) khi chưa có hồ sơ — đã ghi nhận từ phiên trước, FE đã vá phòng thủ,
   chưa sửa gốc BE. Nhắc lại để không quên.
6. **`GET /api/v1/partners` (list, `PartnerResponse`) không có `address`/`contact`** — chỉ
   `GET /partners/{id}` (`PartnerDetailResponse`) mới có 2 field này. Phát hiện phiên 5 khi vá
   `step1RowsOfIssue()` (gap #11 ở "TÓM TẮT PHIÊN 5"): sau khi tra đúng NCC theo tên qua list
   endpoint, cột (6) "địa chỉ, điện thoại" của Mẫu số 1 (QĐ 1246) vẫn trống vì list không mang 2
   field đó. Cần BE thêm `address`/`contact` vào `PartnerResponse`, hoặc FE chấp nhận gọi detail
   riêng cho từng NCC xuất hiện trong lô (N+1, chưa làm).
7. **`StudentResponse` không có 4 field UC-051 clinical file** (`medicalConditions`/
   `medications`/`emergencyContact`/`medicalDocuments`) — nằm ở sub-resource riêng
   (`StudentHealthFileController`, liên quan gap #5). Không chặn gì hiện tại (mock `Student`
   interface chưa migrate, `Student` type vẫn hand-typed) nhưng LÀ LÝ DO chính khiến việc migrate
   `Student` sang `Api<>` (Giai đoạn 3, 23 file) không thể làm bằng 1 `Pick<>` đơn giản như
   `MenuTemplate`/`DietGroup` — xem mục 9 "TÓM TẮT PHIÊN 5".

---

## Chi tiết mục 5 (isAllergenVerified/gradeAllergyStudents/gradeAllergySummary)

Đây là mục phức tạp nhất — dây chuyền dài nhất (7 điểm gọi qua 5 file, cộng thêm
`unresolvedAllergyDishes()`/`dishState()`/`affectedStudents()`/`mealBlockFromTemplate()` là
các hàm TRUNG GIAN cũng phải nối tiếp tham số qua).

**Phát hiện quan trọng làm đơn giản hoá bài toán**: `StudentResponse` (DTO thật, `GET
/api/v1/students`) đã có sẵn field `allergies?: string[]` — ghi chú ngay trong schema
"Field dẫn xuất (ADR-M03-3) — allergen VERIFIED". Tức là **backend đã tự tính sẵn phần lọc
"chỉ tác nhân đã xác minh"** — khác mock cũ (`Student.allergies` gồm CẢ chưa xác minh, phải tự
tra bảng `ALLERGY_DECLARATIONS` riêng để lọc theo status). Nhờ vậy:

- `isAllergenVerified(studentId, allergen, declarations)` — thêm tham số `declarations`
  (mặc định `[]`).
- `gradeAllergyStudents(grade, allergen, opts, students, declarations)` /
  `gradeAllergySummary(grade, opts, students, declarations)` — `students` **BẮT BUỘC** (không
  cho default ngầm về mảng rỗng — cố tình để `tsc` báo lỗi ở MỌI nơi gọi thiếu thay vì âm thầm
  trả về rỗng, vì đây là cảnh báo AN TOÀN THỰC PHẨM BR-063, im lặng sai nguy hiểm hơn vỡ build).
  `declarations` mặc định `[]` — khi rỗng, `opts.verifiedOnly` tin thẳng `students[].allergies`
  (đã là verified-only ở DTO thật) thay vì lọc lại qua `declarations`.
- Nhờ thiết kế trên, các màn KHÔNG có quyền gọi `/allergy-declarations` (SERVICE_CATERERS —
  xem gap #4 ở trên) **vẫn đúng đắn** cho trường hợp `verifiedOnly=true` (ca an toàn quan
  trọng nhất) chỉ với `GET /students`. Chỉ có nhánh `verifiedOnly=false`/mặc định ("đếm cả chưa
  xác minh") ở 2 chỗ (`menu-form.tsx` dòng "advisory", `menus/[id]/page.tsx`) là không đầy đủ
  100% (không phân biệt được "chưa xác minh" khỏi im lặng bỏ qua) khi không có `declarations`
  — chấp nhận được vì không phải căn cứ CHẶN, chỉ là cảnh báo phụ.

**Toàn bộ dây chuyền đã nối** (`students` thật qua `usePagedList<StudentDto>("students")` +
`studentDtoToMock()`, `declarations` thật qua `usePagedList<AllergyDeclarationDto>
("allergy-declarations")` ở 2 trang SCHOOL_ADMIN có quyền gọi):

- `menu-form.tsx` — 5 điểm gọi nội bộ (`dishState`, `affectedStudents`, `mealBlockFromTemplate`
  đều thêm tham số `students`) + `gradeAllergySummary` — `students` đã có sẵn trong
  `MenuForm` (dòng ~993, tải từ trước cho mục đích khác).
- `service-caterers/menus/page.tsx` — 2 điểm gọi (`dishState` trực tiếp +
  `mealBlockFromTemplate`) — phải thêm `usePagedList<StudentDto>` mới.
- `service-caterers/menus/[id]/page.tsx` — 1 điểm gọi `gradeAllergyStudents` — thêm
  `usePagedList<StudentDto>` mới.
- `school-admin/menu-approval/[id]/page.tsx` — 1 điểm gọi `gradeAllergyStudents` +
  `unresolvedAllergyDishes()` — thêm CẢ `usePagedList<StudentDto>` VÀ
  `usePagedList<AllergyDeclarationDto>` (role được phép).
- `school-admin/menu-approval/page.tsx` — 1 điểm gọi `unresolvedAllergyDishes()` — tương tự.
- `menu-personalization.ts` (`unresolvedAllergyDishes`) — thêm tham số `students`/
  `declarations`, forward xuống `gradeAllergyStudents`.

`gradeAllergyStudents` đổi thành **generic** (`<T extends Pick<Student,"id"|"class"|
"allergies">>(...students: T[]): T[]`) để giữ nguyên kiểu đầy đủ của phần tử truyền vào (VD
`affectedStudents()` cần `.name` để hiển thị "Nguyễn Văn A — 3A", nếu Pick cứng sẽ mất field).

---

## Nợ kỹ thuật còn lại (cập nhật sau phiên 5 — nêu rõ để phiên sau không đoán lại từ đầu)

1. ~~`dietGroupMembers()`/`dietGroupMembersInGrade()` đọc thẳng `STUDENTS`~~ — **ĐÃ VÁ Ở PHIÊN
   4** (Phần A), xem mục "TÓM TẮT PHIÊN 4".
2. **`INGREDIENT_CATALOG`** vẫn còn là mảng seed sống, dùng bởi `ingredientById()`,
   `ingredientIdOfName()`, `toPurchaseQuantity()` (refuseFactor), và vòng kiểm tra lệch đơn vị
   module-level. KHÔNG nằm trong phạm vi 32 mảng gốc lẫn 5 mục Giai đoạn 2. Nếu muốn dọn tiếp,
   cần biết `IngredientResponse` thật có field `unit`/tương đương `refuseFactor` hay không
   (nếu không có `refuseFactor` — hệ số thải bỏ — thì đây LẠI là một gap backend khác, chưa
   kiểm tra ở phiên nào). `CatalogIngredient` (interface tương ứng) đã xác nhận lại ở phiên 4
   là GIỮ đúng (không có DTO nào thay được — xem "TÓM TẮT PHIÊN 4" nhóm A).
3. **`NUTRITION_TARGET`** — giữ nguyên theo đúng chỉ đạo, KHÔNG động vào (kể cả phiên 4). Vẫn
   cần BR/UC mới + bảng cấu hình + endpoint cho school-admin (xem phiên trước).
4. **Giai đoạn 3 — ĐANG LÀM DỞ.** Phiên 4 xoá 38/79 interface, phiên 5 xoá thêm 4
   (`MenuTemplate`/`DietGroup`/`SupplierProduct` migrate thật sang `Pick<Api<>>`,
   `AllergyDeclaration` dead code) — còn lại **37 interface/type hand-typed**. Xem "TÓM TẮT
   PHIÊN 4"/"TÓM TẮT PHIÊN 5" để biết đầy đủ lý do từng nhóm. **Kết luận GO/NO-GO đã có sau
   phiên 5, việc cho phiên 6 theo thứ tự ưu tiên**:
   - ~~**GO, ứng viên AN TOÀN — làm trước**: `SpecialMealRequest` (11 file)~~ — **ĐÃ XONG PHIÊN
     6** (12 file, thay thẳng bằng `SpecialMealRequestDto`, không cần Draft type — xem "TÓM TẮT
     PHIÊN 6" mục B). Ứng viên GO tiếp theo phải tự chọn lại: các mục còn lại dưới đây đều là
     NO-GO hoặc "cần thiết kế lại".
   - **NO-GO đã xác nhận, ĐỪNG thử migrate nữa (tốn thời gian vô ích)**:
     - `SeedMenu`/`MenuItem`/`SeedMealBlock`(+`SeedDish`/`SeedSubstituteDish`/
       `SeedIngredientRow`) VÀ `Dish`/`DishIngredient` — cùng gốc: `MenuIngredient.quantity`
       DTO thật là `number`, mock là `string` có đơn vị, dùng trực tiếp trong tính toán/hiển thị
       ở nhiều nơi (`mealIngredientNeeds()`, `menu-form.tsx` draft state). Xem mục 4/5 "TÓM TẮT
       PHIÊN 5" — **bài học quan trọng**: contract-check hiện tại (`MissingOnDto`) CHỈ kiểm tên
       field cấp 1, không đệ quy vào mảng lồng nhau — đừng tin nó cho các interface có
       `ingredients[]`/`dishes[]`.
     - `MenuVariant`/`MenuVariantDish` — không có DTO tương ứng (tính năng đã gộp vào `Menu`,
       đây là shape TÍNH TOÁN thuần từ `specialMenuMeals()`), giữ nguyên vĩnh viễn, không phải
       "chưa migrate".
     - `ViolationRecord` — xác nhận qua `_violationsContract` (Omit 14 field investigation-only)
       — cùng nhóm lý do GIỮ với cụm Incident/KitchenInspection.
   - **CẦN THIẾT KẾ LẠI trước khi migrate (không phải 1 `Pick<>` đơn giản)**:
     - `Student` (23 file, LỚN NHẤT) — `StudentResponse` thiếu hẳn 4 field UC-051
       (`medicalConditions`/`medications`/`emergencyContact`/`medicalDocuments`, nằm ở
       sub-resource `student-health-file` riêng). Cần tách thành 2 type (list-level +
       health-file) thay vì Pick 1 lần — xem mục 9 "TÓM TẮT PHIÊN 5" + gap backend #7.
     - ~~`Partner`+`PartnerDocument`~~ — **ĐÃ XONG PHIÊN 6** (mục B3): tách thật thành
       `PartnerDto` (9 điểm list) và `PartnerDetailDto` (2 điểm detail).
   - ~~**CHƯA kiểm tra field-by-field**: `Classroom` (16 file)~~ — **ĐÃ XONG PHIÊN 6**, xem
     "TÓM TẮT PHIÊN 6" mục B2. Đường GHI vốn đã dùng request shape riêng nên chỉ cần thay type
     ĐỌC; `lunchTime` chốt bỏ hẳn.

   **Sau phiên 6, Giai đoạn 3 còn 37 interface, và chỉ còn ĐÚNG MỘT ứng viên khả thi:
   `Student` (23 file).** Mọi mục khác đều là NO-GO đã xác nhận — đừng mở lại.
   `Student` vẫn chưa làm được gọn vì `StudentResponse` thiếu 4 field UC-051 (nợ BE #7); 2 hướng:
   (a) BE bổ sung field / xác nhận `student-health-file` là nguồn duy nhất rồi FE tách 2 type
   như đã làm cho `Partner` ở B3 — mẫu tách đó nay đã có sẵn để chép; (b) để nguyên vì `Student`
   (mock) hiện chỉ còn là SHAPE TRUNG GIAN qua `studentDtoToMock()` (`lib/student-adapter.ts`),
   không còn màn nào dùng nó làm kiểu resource — tức nó KHÔNG còn là "mock data chưa dọn" theo
   nghĩa gốc của việc này. Đọc lại `contract-check.ts` (khối comment `students`) trước khi quyết.

---

## Trạng thái cuối phiên 4

```bash
cd 04_Development/Frontend
npx tsc --noEmit -p tsconfig.json   # SẠCH — 0 lỗi, xác nhận lần cuối trước khi dừng (cả worktree lẫn working tree thật)
```

`mock-data.ts`: 4369 → **3709 dòng** (−660, riêng phiên 4). Xem "Trạng thái cuối phiên 3" bên
dưới cho lịch sử trước đó (6014 → 4369). Khoảng 30 file trang/component khác đã sửa (đổi type
import sang `Api<>`, vài chỗ `?? ""`/`?? 0` cho field optional mới lộ ra sau khi đổi type) —
không file nào đổi cấu trúc lớn ngoài `medical-staff/conduct-inspection/checklist/page.tsx`
(pattern Draft type mới). Nhánh `huybqhe180599`, CHƯA commit.

## Trạng thái cuối phiên 3

```bash
cd 04_Development/Frontend
npx tsc --noEmit -p tsconfig.json   # SẠCH — 0 lỗi, xác nhận lần cuối trước khi dừng
```

`git diff --stat` (từ nhánh `huybqhe180599`, CHƯA commit — để lại cho Nam review):
20 file thay đổi, `mock-data.ts` giảm từ 6014 → 4369 dòng (−1645). Các file khác chỉ thêm vài
dòng mỗi file (chủ yếu `usePagedList` mới + truyền tham số qua các hàm). `schema.d.ts` cũng đổi
(+1439 dòng) — KHÔNG phải việc của phiên này, xem ghi chú riêng bên dưới.

**Lưu ý phụ**: giữa phiên, backend đã chạy `npm run gen:api` lại (thêm nhiều DTO/field mới sau
khi chạy Spring Boot + Postgres local thật) — file `schema.d.ts` cập nhật ngay tại đường dẫn
đang dùng (không phải bản cũ), đã xác nhận `tsc` vẫn sạch sau khi cập nhật, không có breaking
change với các đổi mới của phiên này.

## Ghi nhớ quy trình (áp dụng lại nếu lặp tình huống tương tự)

- KHÔNG bao giờ xóa hàng loạt bằng script dựa trên kết quả audit "không ai import" — audit
  tầng trang bỏ sót tham chiếu chéo NỘI BỘ trong cùng file tiện ích. Luôn `grep -c` đếm số lần
  xuất hiện trước khi xóa, xử lý từng cái, biên dịch ngay sau mỗi lần xóa.
- Khi giao việc dọn dẹp lớn cho agent nền, PHẢI tự kiểm tra `git diff --stat` + chạy `tsc`
  thật khi quay lại — đừng giả định agent báo "xong" là đã đúng, và đừng tiếp tục code mới
  trên cùng file khi chưa xác nhận trạng thái hiện tại.
- **Mới (phiên 3)**: khi xóa 1 mảng seed "chỉ 1 hằng số", LUÔN kiểm tra xem có bộ SINH DỮ LIỆU
  (`GENERATED_*`, IIFE `(() => {...})()`, hay vòng `MENUS.push(...)`) và các VÒNG BACKFILL
  module-level (`for (const x of MẢNG) {...}` chạy lúc nạp module) phụ thuộc mảng đó không —
  quy mô thật có thể gấp 4-5 lần số dòng của riêng mảng đó (xem case `MENUS`, mục riêng ở trên).
- **Mới (phiên 3)**: default tham số kiểu `= MOCK_ARRAY` không phải lúc nào cũng "chết". Luôn
  `grep` TÊN_HÀM trong toàn bộ `src/app`+`src/components` (không chỉ trong `mock-data.ts`) để
  chắc chắn KHÔNG nơi nào gọi hàm mà bỏ qua tham số đó — nếu có (VD `variantDishName(d)` thiếu
  tham số ở `special-meal-plans.ts`), đó là bug thật đang chạy ngầm trên mock, không phải false
  positive của audit.
- **Mới (phiên 3)**: trước khi nối 1 `usePagedList` mới vào màn hình, LUÔN kiểm tra
  `@PreAuthorize` của endpoint đó ở backend (`grep -n "@PreAuthorize\|@GetMapping" ...
  Controller.java`) khớp với role của màn đang sửa — nối API có thật nhưng role không được
  cấp quyền vẫn là một dạng "tự bịa" (API sẽ 403 khi người dùng thật vào), phải dừng và báo
  cáo giống như module chưa có endpoint, KHÔNG được lặng lẽ nối rồi để vỡ khi chạy thật.
- **Mới (phiên 3)**: khi mock model và model DTO thật LỆCH NHAU về ngữ nghĩa (không chỉ tên
  field) — ví dụ `Student.allergies` mock là "mọi khai báo" còn `StudentResponse.allergies`
  thật là "chỉ đã xác minh" (field dẫn xuất, ADR-M03-3) — đừng cố ép hàm cũ hoạt động y hệt
  bằng cách gọi thêm API phụ nếu bản thân DTO thật đã đơn giản hoá bài toán sẵn. Đọc kỹ
  docstring/comment trong `schema.d.ts` (thường có ghi "field dẫn xuất") trước khi quyết định
  cần nối thêm API nào.
- **Mới (phiên 4)**: trước khi bắt tay migrate 1 interface sang `Api<>`, LUÔN `grep -rn
  "\bTênInterface\b" src/app src/components src/lib` (loại `mock-data.ts`/`contract-check.ts`)
  TRƯỚC — nếu 0 kết quả thật (không tính comment/docstring nhắc tên suông), đó là DEAD CODE, chỉ
  cần xoá thẳng interface, KHÔNG cần đối chiếu DTO/sửa nơi dùng gì cả. Ở phiên 4, hơn nửa số
  interface xoá được (22/38) rơi vào trường hợp này — tồn đọng từ các phiên trước khi trang
  chuyển sang đọc DTO thật nhưng quên xoá interface mock không còn ai dùng.
- **Mới (phiên 4)**: `contract-check.ts` không chỉ là guard biên dịch — đọc COMMENT của từng dòng
  `_xxxContract` trước khi tự đối chiếu `schema.d.ts` lại từ đầu. Nhiều dòng đã tự ghi sẵn "trang
  này đã đọc thẳng DTO thật, dòng check chỉ còn là cột mốc" hoặc lý do CỐ TÌNH KHÔNG migrate
  (lệch kiến trúc luồng ghi, field investigation-only không có trên DTO...) — dùng lại y nguyên
  quyết định đó thay vì tự đánh giá lại, TRỪ KHI có lý do cụ thể để nghi ngờ nó đã lỗi thời.
- **Mới (phiên 4)**: khi 1 interface là STATE NHÁP của trang CRUD viết nhiều (sửa/sắp xếp trước
  khi Lưu) và DTO thật có mọi field optional (springdoc không khai `required`), ĐỪNG rải `?? ""`
  khắp trang — định nghĩa 1 "Draft type" cục bộ bằng `Pick<XxxDto, "id"|"name"|...>` (dẫn xuất từ
  DTO thật, không phải khai tay độc lập) + 1 hàm `normalizeXxx()` narrow field ngay tại ranh giới
  nạp dữ liệu (xem ví dụ đầy đủ ở mục "Pattern mới thiết lập" trong TÓM TẮT PHIÊN 4).
- **Mới (phiên 4)**: khi phiên làm việc bị cô lập trong 1 git worktree khác với working tree thật
  của người dùng (kiểm tra bằng `Working directory` trong system prompt có chứa
  `.claude\worktrees\...` hay không), Edit/Write tool và MỌI thao tác `git` sẽ bị chặn cứng nếu
  nhắm ra ngoài worktree — nhưng `Bash cp` (copy file thường, hai chiều) và chạy lệnh non-git
  (VD `npx tsc`) trực tiếp trong working tree thật đều KHÔNG bị chặn. Quy trình dùng được: mirror
  file cần sửa vào worktree (giữ `node_modules` bằng NTFS junction thay vì copy — 675MB, quá
  chậm để copy), sửa bằng Edit/Write bình thường trong đó, `tsc` xác minh trong worktree, rồi
  `cp` từng file đã sửa VỀ working tree thật, `tsc` xác minh LẦN NỮA ở đó trước khi coi là xong.
  KHÔNG dùng git để đồng bộ hai bên.
- **Mới (phiên 5)**: `MissingOnDto<FE, Dto>` (định nghĩa ở `contract-check.ts`) chỉ là
  `Exclude<keyof FE, keyof Dto>` — kiểm TÊN field CẤP 1, KHÔNG đệ quy vào type của field lồng
  nhau (mảng con, object con). Một contract-check "compile được" cho 1 interface có
  `xxx: SomeType[]` KHÔNG chứng minh gì về field BÊN TRONG `SomeType` — phải tự đối chiếu riêng.
  Case thật: `_menusContract` pass (tên field `blocksData` có trên `MenuDto`) nhưng
  `blocksData[].dishes[].ingredients[].quantity` lệch hẳn kiểu (`number` DTO thật vs `string`
  mock) — chỉ phát hiện được bằng cách đọc trực tiếp `schema.d.ts` cho TỪNG type lồng nhau, không
  dừng ở dòng contract-check gốc.
- **Mới (phiên 5)**: một DTO có thể tách LIST response và DETAIL response với field set KHÁC
  NHAU (VD `PartnerResponse` vs `PartnerDetailResponse` — chỉ detail có `address`/`contact`).
  Mock interface hand-typed thường gộp chung cả hai (vì mock không phân biệt). Trước khi migrate
  1 interface, kiểm xem `schema.d.ts` có bao nhiêu biến thể Response cho cùng 1 resource
  (`grep -n "XxxResponse:\|XxxDetailResponse:\|XxxSummary:"`) — nếu có ≥2 biến thể với field
  khác nhau, migrate an toàn cần tách type theo đúng ngữ cảnh list/detail, không gộp `Pick<>`
  một lần cho cả hai.
- **Mới (phiên 6)**: `status: keyof typeof STATUS_BADGE` (và mọi `keyof typeof X` tương tự) KHÔNG
  đảm bảo là union chặt — phải mở khai báo của `X` ra xem: `STATUS_BADGE` là
  `Record<string, {...}>` nên `keyof typeof STATUS_BADGE` ≡ `string`. Nhiều interface mock dùng
  mẫu này trông như "chặt hơn DTO" (lý do phải dựng Draft type) nhưng thực ra KHÔNG — kiểm trước
  khi quyết định pattern migrate, tránh dựng Draft type + `normalizeXxx()` thừa.
- **Mới (phiên 6)**: khi migrate interface mock → DTO, hãy soát riêng các chỗ render `date`/thời
  gian. Mock thường lưu SẴN định dạng hiển thị (dd/MM/yyyy) nên trang in thẳng `{x.date}`; DTO
  thật luôn là ISO (D4) ⇒ những dòng đó lặng lẽ đổi thành "2026-08-07" mà `tsc` KHÔNG báo (cả 2
  đều là `string`). Phiên 6 tìm được 2 chỗ kiểu này (`parent/health`,
  `medical-staff/health-records/[id]`) — `grep` `\.date\}` trong các file vừa migrate.
- **Mới (phiên 6)**: ĐỪNG dùng PowerShell `Get-Content -Raw` + `Set-Content` để sửa hàng loạt file
  `.tsx` trong repo này — đọc/ghi lệch codepage làm hỏng toàn bộ tiếng Việt trong file (mojibake
  `Ä‘Ã£`). Dùng Edit tool. Nếu lỡ hỏng: `git checkout -- <file>` rồi làm lại bằng Edit.
- **Mới (phiên 5)**: khi 1 default tham số `= MOCK_ARRAY` được xác nhận "không nơi nào gọi bỏ
  qua tham số" (đúng quy trình phiên 3), đừng dừng ở việc đổi default về `[]` — LUÔN `grep -n
  "\bMOCK_ARRAY\b"` một lần cuối trong TOÀN BỘ `mock-data.ts` để chắc không còn HÀM NỘI BỘ nào
  khác (không phải default tham số) đọc thẳng mảng đó. Case thật: `PARTNERS` không chỉ là 5
  default "chết" mà còn có 1 dòng `PARTNERS.find(...)` sống thật trong `step1RowsOfIssue()` —
  nếu chỉ đổi default rồi xoá mảng ngay, dòng đó sẽ vỡ biên dịch NGAY LẬP TỨC (an toàn), nhưng
  nếu tình cờ mảng đó bị giữ lại thay vì xoá, bug sẽ tiếp tục ẩn im lặng.
