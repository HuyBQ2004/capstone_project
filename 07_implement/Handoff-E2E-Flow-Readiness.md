# Handoff — Thông hai luồng nghiệp vụ chính trước demo

**Bắt đầu:** 07/08/2026. **Nhánh:** `namnvhe180863`.

> **File này = NHẬT KÝ TIẾN ĐỘ.** Bộ tài liệu của đợt này gồm 3 file, đọc kèm nhau:
>
> | File | Vai trò |
> |---|---|
> | `07_E2E_Flow_Readiness_Plan.md` | **KẾ HOẠCH** 6 giai đoạn (Nam đã duyệt) |
> | **`Handoff-E2E-Flow-Readiness.md`** ← bạn đang đọc | **ĐÃ LÀM TỚI ĐÂU** + baseline đo được |
> | `08_E2E_Findings_Backlog.md` | **CÒN LẠI** — mọi phát hiện chưa sửa, kèm `file:line` |
>
> Dựng môi trường và chạy: `09_Dev_Runbook.md`. Mục lục chung: `README.md`.
>
> Đợt trước: `Handoff-System-Audit-Gaps.md` (phân quyền/UX) ·
> `Handoff-Mock-Data-Removal.md` (dọn mock).

## Mục tiêu

Hai luồng bắt buộc chạy được end-to-end để demo cho khách:

1. **Luồng bếp** — tạo món → mẫu thực đơn → lịch thực đơn (ngày × bữa × khối) → mua hàng →
   nhập kho → xuất kho → nấu (kiểm thực 3 bước + lưu mẫu) → phân phát (dị ứng + suất đặc biệt).
2. **Luồng sự cố** — báo cáo → y tế điều tra (tra suất ăn của trẻ) → khoanh vùng + giữ mẫu
   (bếp không được hủy) → CAPA → trường duyệt → bên thực hiện nộp bằng chứng → đóng.

Rà soát đầu phiên (3 agent quét + tự kiểm chứng từng claim P0) tìm ra **4 chỗ chặn cứng**:
không tạo được thực đơn · không lưu được mẫu · không giữ được mẫu khi điều tra · không đóng
được sự cố. **Lằn ranh demo tối thiểu = hết Giai đoạn 2.**

---

## GIAI ĐOẠN 0 — XONG (07/08/2026)

### ✅ Chuẩn dinh dưỡng theo TRƯỜNG × KHỐI (BR-062) — tính năng mới

Trước: `NUTRITION_TARGET` là MỘT hằng số cho toàn hệ thống, khai TRÙNG ở hai nơi không có
cách nào biết nhau đã lệch — `MealHistoryServiceImpl` (`private static final`, sửa phải build
lại) và `Frontend/src/lib/mock-data.ts`. Trong khi `menus` đã gắn `grade_id` từ V44 ⇒ thực đơn
Khối 1 và Khối 5 bị chấm cùng một thước ±10%.

| Thành phần | Đường dẫn |
|---|---|
| Migration | `V54__mf02_nutrition_targets.sql` — `(school_id, grade_id)` UNIQUE, CHECK 4 chỉ số > 0 |
| Entity/repo | `mf02/domain/NutritionTarget.java`, `mf02/repository/NutritionTargetRepository.java` |
| Service/DTO/API | `mf02/service/NutritionTargetService(+Impl)`, `mf02/dto/NutritionTargetDtos.java`, `mf02/web/NutritionTargetController.java` |
| Hằng mặc định | **`ReferenceResolver.DEFAULT_NUTRITION_TARGET`** (common) — nơi khai DUY NHẤT |
| MF09 đọc theo khối | `ReferenceResolver.nutritionTargetOfStudent()` ← `MealHistoryServiceImpl` |
| FE | `lib/nutrition-target.ts` (`useNutritionTargets` / `useNutritionTarget(gradeLabel)`) |
| Màn cấu hình | `school-admin/config/page.tsx` — card mới, bảng theo khối |
| Màn tiêu thụ | `school-admin/menu-approval/[id]`, `components/app/special-meal-dialog.tsx` |

**API**: `GET /api/v1/nutrition-targets` (SYSTEM_ADMIN/SCHOOL_ADMIN/SERVICE_CATERERS/
MEDICAL_STAFF) · `PATCH /api/v1/nutrition-targets/{gradeId}` (SCHOOL_ADMIN).

**Quyết định thiết kế cần nhớ:**
- **KHÔNG backfill.** Trường chưa khai ⇒ không có dòng ⇒ trả mặc định `715/28/22/95` kèm
  `configured:false`. Hành vi giữ NGUYÊN với mọi dữ liệu hiện có.
- **"Chưa khai" = KHÔNG CÓ DÒNG**, không phải số 0 — DB có CHECK chặn số không dương đúng vì
  lý do đó.
- **Không module nào inject service của module khác** trong dự án này (đã grep xác nhận) —
  nên MF09 đọc qua `ReferenceResolver` bằng native query, không inject `NutritionTargetService`.
- **FE không đoán giá trị mặc định.** `useNutritionTarget()` trả `undefined` khi không tìm
  thấy khối, nơi gọi hiện "chưa đặt chuẩn" thay vì bịa số — đoán lại ở FE là tái lập đúng
  cái lỗi hai-bản-sao vừa bỏ.

### ✅ `GET /diet-groups` — mở cho PARENT + TEACHER

`DietGroupController.java:32`. Trước đó 5 màn của đúng hai vai này gọi `useDietGroups()` và
nhận **403 im lặng**: `parent/{dashboard,special-meals,weekly-menu}`,
`teacher/{special-meals,students/[id]}`. Triệu chứng thấy được: cột "Ghi chú chế độ ăn" của
giáo viên LUÔN hiện `—`. Đã xác minh bằng API thật: cả hai vai nay **200**.

### ✅ Dọn dữ liệu giả còn render ra màn hình

| Chỗ | Trước | Sau |
|---|---|---|
| `service-caterers/dashboard` | `MEALS_7D` — biểu đồ 7 ngày (1802…1850) **bịa**, kèm chú thích giải thích một "sự cố ngày 30/06" không có thật | **Bỏ card + hàm.** Muốn khôi phục cần endpoint tổng hợp suất đã phát theo ngày (gộp ở server, KHÔNG gọi `/summary` 7 lần từ client) |
| `service-caterers/dashboard:354` | `description="Thứ Năm, 02/07/2026 · 1.850 suất"` | Ngày thật |
| `service-caterers/dashboard` "Kiểm thực hôm nay" | `value="Bước 2/3"` cứng | Tính từ phiên kiểm thực thật của hôm nay (`step1/2/3` đã nộp) |
| `service-caterers/dashboard` "Phiên nhận hàng" | `"1 đang mở"` + hint `"GreenFarm — 05:45"` | **Bỏ ô** — màn không đọc `receiving-sessions`, không có gì thay bằng số thật |
| `service-caterers/receiving:226` | `"Tỷ lệ GPS hợp lệ 100%"` + hint "Geofence 200m" (bán kính là CẤU HÌNH theo trường) | **Bỏ ô** |
| `school-admin/dashboard` | `"96%"` tỷ lệ kiểm thực đạt | Tính thật từ `kitchen-inspections` 30 ngày; chưa có phiên ⇒ `"—"` (không phải 0%) |
| `school-admin/dashboard` | Alert `"2 nhà cung cấp"` cứng | `partners.certStatus` EXPIRING/EXPIRED, ẩn hẳn khi = 0 |
| `medical-staff/dashboard` | Alert dẫn `FB-8809` / `INC-2026-014` — **hai mã không tồn tại** | **Bỏ.** Cần `GET /feedbacks?illnessKeyword=true` (BR-142) rồi mới hiện |
| `medical-staff/dashboard` | `value="3"` yêu cầu bữa ăn đặc biệt | Đếm thật `special-meals?status=PENDING` |
| `lib/violation-utils.ts:145` | In cứng tên một trường vào header **BIÊN BẢN VI PHẠM** (văn bản pháp lý) | `schoolName` truyền qua tham số, đúng tiền lệ `buildReportDocumentHtml(doc, schoolName)` |

`CURRENT_SCHOOL` nay **không còn chỗ nào trong code dùng** (chỉ còn nhắc trong Javadoc).

### Xác minh Giai đoạn 0

- `mvn test` — **352 test pass, BUILD SUCCESS**
- `tsc --noEmit` sạch · `npm run build` compile hết **136 route** · `next lint` **8 warning,
  0 error** (đúng baseline, không thêm cái nào)
- API thật (backend chạy, postgres `sfms-postgres` cổng 5432):
  - `GET /nutrition-targets` → 6 khối, `configured:false`, mặc định đúng 715/28/22/95
  - `PATCH /nutrition-targets/{id}` `{kcal:650,protein:26}` → `configured:true`, fat/carb giữ mặc định
  - `PATCH … {kcal:0}` → **422** (CHECK + `@DecimalMin` cùng chặn)
  - `/diet-groups` với token TEACHER và PARENT → **200**

> ⚠️ **Bẫy đã mắc, đừng lặp:** thêm hook (`useNutritionTarget`, `useAuthStore`) vào giữa
> component mà quên rằng **phía trên đã có early return** ⇒ vi phạm `rules-of-hooks`, lint
> báo Error chứ không phải Warning. Hook phải đứng trước MỌI `if (...) return`.

---

## BASELINE ĐO ĐƯỢC cho Giai đoạn 1 (quan trọng — đừng đo lại)

Đo bằng API thật ngày 07/08/2026, token SERVICE_CATERERS:

1. **`POST /menus` với payload FE hiện tại → `422 MENU-422`** *"malformed request body"*.
   Xác nhận chỗ chặn cứng: `MenuDtos.java:101` đòi `@NotBlank String gradeId` (UUID), FE gửi
   `grade` (nhãn `"Khối 3"`).
2. **`GET /grades` trả `name` = `"Khối 1"`, `"Khối 2"`, `"Khối 3"`… KHỚP ĐÚNG chuỗi mà
   `ClassroomResponse.grade` trả về** (`'Khối 3'`, `'Khối 4'`).
   ⇒ Phương án `useGradeId(label)` của kế hoạch **chạy được**; nhánh dự phòng "bắt buộc đổi
   dropdown sang `useGrades()`" **KHÔNG kích hoạt**.
3. **`ClassroomResponse.gradeId` = `null`** — lớp KHÔNG expose `gradeId`.
   ⇒ FE bắt buộc lấy UUID khối qua `GET /grades`, không lấy được từ `classrooms`.
4. `GET /inspections/serving-gate?date=2026-08-07&mealType=LUNCH` →
   `{allowed:false, reasonCode:"IN_PROGRESS", sessionId:"INSP-20260807-L"}` — cổng đọc hoạt
   động đúng; chỗ hỏng là đường THI HÀNH (`doServe`), xem Giai đoạn 1.6.

---

## CÒN LẠI — theo kế hoạch đã duyệt

| Giai đoạn | Nội dung | Trạng thái |
|---|---|---|
| 1 | Thông luồng bếp — 7 mục P0 | 🟡 **5/7 xong**, xem bên dưới |
| 2 | Thông luồng sự cố — 6 mục P0 (đóng sự cố, giữ mẫu 500, chặn hủy mẫu, tra suất ăn của trẻ, bản đồ truy xuất, 2 màn CAPA) | ⬜ |
| 3 | Độ mịn danh mục nguyên liệu + chặn `ingredient_code` NULL | ⬜ |
| 4 | Tách seed khỏi migration production | ⬜ |
| 5 | Dọn UI — 7 việc rẻ + 2 việc cơ học | ⬜ |

**Hai quyết định nghiệp vụ Nam đã chốt** (ghi lại để không hỏi lại):
- **Cổng BR-094 siết ĐỦ**: `NO_SESSION` / `IN_PROGRESS` / `INSPECTION_FAILED` đều chặn phát
  suất. Hệ quả demo: quên mở phiên kiểm thực là tắc ở bước phát suất — đó là chốt chặn ATTP
  cố ý, cần một câu thoại giải thích khi demo.
- **Giữ mẫu thì GIA HẠN `retention_deadline`** + thêm trạng thái `HOLD` (BR-111 24h trùng
  đúng cửa sổ điều tra BR-149 24h).


---

## GIAI ĐOẠN 1 — 5/7 XONG (07/08/2026)

### ✅ 1.1 Khôi phục "tạo được thực đơn" — chốt chặn lớn nhất của luồng bếp

Thêm `lib/grades.ts` (`useGrades()` / `useGradeId(label)`) theo khuôn `lib/diet-groups.ts`.
**Quy ước chốt: nhãn khối là khoá HIỂN THỊ/URL, UUID chỉ dùng ở ranh giới gửi request** —
đổi hết sang UUID là refactor xuyên 6 màn + `mock-data.ts`, không làm trước demo.

| Chỗ | Trước | Sau |
|---|---|---|
| `menus/page.tsx` tạo thực đơn | gửi `grade` nhãn ⇒ **422 MENU-422** | gửi `gradeId` ⇒ **201 (đã đo)** |
| `menus/page.tsx` ×2 `diet-coverage` | `grade` nhãn ⇒ **luôn 400**, checklist BR-065 chưa từng hiện | `gradeId` + `enabled` |
| `menu-templates` lưu mẫu | gửi nhãn vào field nullable ⇒ lưu `grade_id = NULL` **im lặng** | `gradeId` |
| `menu-templates` ×2 `grade-summary` | **luôn 400** | `gradeId` + `enabled` |
| Dropdown khối ở 2 màn GHI | `gradeLevelsOf(classrooms)` | `useGrades()` — mọi nhãn chọn được đều resolve ra UUID |

Thêm `gradeId?` vào `PersistedMenu` và `MenuTemplate`, kèm chặn trước khi gửi (thiếu
`gradeId` ⇒ toast rõ nghĩa thay vì để server trả "Validation failed").

### ✅ 1.3 Nút "gỡ thực đơn khỏi bữa" hết nói dối — V55

`templates[]` là khái niệm **chỉ có ở FE**, KHÔNG round-trip: gửi lên thì `applyBlocksData()`
bỏ qua, đọc về thì `MenuMapper` không dựng lại. Nên `d.templateId` luôn undefined ⇒ ô lịch
chỉ hiện 1 mục dù thêm nhiều mẫu · chống trùng vô hiệu · `removeTemplate()` **không gỡ gì
nhưng vẫn toast "Đã gỡ thực đơn khỏi bữa"**.

- `V55__mf05_menu_dish_template.sql` — `menu_dishes.template_id` (**không phải
  `menus.template_id`**: một ô chứa N mẫu, arity 1-1 sai), `ON DELETE SET NULL`.
- `MenuDish.templateId` · `MenuBlockDish.templateId` · ghi ở `applyBlocksData` · đọc ở
  `MenuMapper.toBlockDish`.
- `blockEntries()` **dẫn xuất chip từ chính các món** (`dishes[].templateId`) + bảng tên mẫu
  truyền từ nơi gọi; `removeTemplate()` lọc đúng món, chip `""` gỡ món ghép tay.

### ✅ 1.4 Khôi phục "lưu được mẫu thực phẩm"

`Step2Row`/`Step3Row` (FE) thiếu hẳn `dishId` mà `Step2RowDto`/`Step3RowDto` (BE) **vẫn luôn
có** ⇒ `step2_rows.dish_id` lưu NULL suốt ⇒ `dishChoicesOf()` lọc
`r.dishId` ra **mảng rỗng** ⇒ dialog "Đăng ký lưu mẫu" không có món nào, mà
`RegisterSampleRequest.dishId` là `@NotBlank`.

Thêm `dishId` vào cả hai interface + `step2RowsOfMeal()` (gom theo MÃ món thay vì tên) +
`step3RowsOfMeal()` + hai hàm dòng rỗng. **Bỏ `as unknown as SubmitStepDto[...]`** ở nơi nộp
— chính cặp ép kiểu đó đã che lỗi khỏi TypeScript; TS lập tức bắt đủ 3 chỗ còn thiếu.

### ✅ 1.6 Siết cổng BR-094 (Nam chốt siết đủ)

`ReferenceResolver.inspectionGate()` nay khớp ĐÚNG `InspectionServiceImpl.checkGate()`:
chỉ `overall = PASSED` mới cho phục vụ; `NO_SESSION` / `IN_PROGRESS` / `INSPECTION_FAILED`
đều chặn. Thêm `reasonCode` vào record để hai đường nói cùng một ngôn ngữ, và **thêm
`ORDER BY created_at DESC`** — trước đây không sắp xếp nên trường có ≥2 nhà thầu hoặc có
phiên tái kiểm sẽ lấy phiên NGẪU NHIÊN, hai đường đọc ra hai kết quả khác nhau.
`Optional.empty()` nay CHỈ nghĩa là thiếu tham số, không còn mang nghĩa "không có phiên".

FE `teacher/distribution/[id]`: bỏ đoạn né, disable nút theo đúng `allowed:false`, cảnh báo
đổi nhan đề + hướng dẫn theo từng `reasonCode`.

> ⚠️ **Chưa chạy được đường `serve` end-to-end**: DB hiện KHÔNG có bản ghi
> `student_distributions` nào (tổng 0), nên không có suất PENDING để bấm thử. Đã xác minh
> gián tiếp: compile sạch, 352 test pass, và `GET /inspections/serving-gate` trả đúng
> `IN_PROGRESS`. **Phải kiểm lại khi chạy kịch bản E2E đầy đủ.**

### ✅ 1.7 Bug đơn vị ×1000 ở mua hàng

`orders/new/page.tsx` nhân số suất mà **không quy đổi g→kg**, trong khi `dish_ingredients.unit`
mặc định `'g'` còn `catalog_ingredients.unit` là `'kg'` ⇒ nhu cầu đặt hàng bị thổi phồng
**1000 lần**. Dùng chung `toBaseQuantity()` như màn xuất kho — một bảng quy đổi duy nhất.

### ⬜ CÒN LẠI của Giai đoạn 1

- **1.5 `batchId` Bước 1 (BR-092/093)** — `step1RowsOfIssue()` không set `batchId`; kể cả set
  thì FE có **mã lô** (`"BATCH-GF-4412"`) còn `InspectionServiceImpl.parseUuid()` nuốt lỗi →
  NULL. Phải sửa CẢ HAI đầu và bỏ `parseUuid` nuốt lỗi im lặng.
- **1.2 `menu-form` fan-out theo buổi** — gửi `blocksData` nhiều buổi mà
  `MenuServiceImpl:581` chỉ đọc `blocks.get(0)`. Lưu ý: `menu-form` chỉ còn đường vào qua
  `/menus/[id]/edit`; `/menus/create` là route **mồ côi** (0 link) và
  `meal-block-dialog.tsx` là **code chết** (0 import) — dọn ở Giai đoạn 5.

### Xác minh Giai đoạn 1 (phần đã làm)

`mvn test` **352 pass** · `tsc` sạch · `npm run build` **136 route** · `next lint` **8
warning** (đúng baseline) · V55 migrate sạch · `POST /menus` với `gradeId` → **201**.


---

## GIAI ĐOẠN 2 — BACKEND XONG 4/4 CHỐT CHẶN CỨNG (08/08/2026)

**Phiên này bắt đầu bằng việc bắt buộc: chạy `mvn test` trên cây đã xoá seed nghiệp vụ.**
Kết quả: **352 test pass, BUILD SUCCESS** — *không* test nào phụ thuộc dữ liệu đã cắt.
Baseline giữ nguyên, không phải sửa gì trước khi làm việc khác.

> Bẫy môi trường gặp ngay: Docker Desktop **chưa chạy** ⇒ Testcontainers không lên được.
> `docker ps` báo `dial ... dockerDesktopLinuxEngine: The system cannot find the file specified`.
> Bật Docker Desktop rồi mới chạy được; `sfms-postgres`/`sfms-mailpit` tự bật theo.

Đã drop + create lại DB `sfms` theo đúng yêu cầu checksum Flyway (runbook mục 11).

### Bốn chốt chặn cứng — đã gỡ ở BACKEND

| # | Trước | Sau |
|---|---|---|
| **B1** | `quarantine_disposition` chỉ được ĐỌC, không nơi nào GHI ⇒ sự cố đã cách ly lô **không bao giờ đóng được** | `POST /incidents/{code}/quarantine-batches/{batchCode}/disposition` — ghi trạng thái cuối, MỘT CHIỀU, chỉ cho lô của chính sự cố đó; `QuarantineBatchDto` trả kèm `disposition`/`dispositionAt` |
| **B2** | Giữ mẫu ⇒ **500 + rollback toàn bộ** (thiếu `lab_request_id`/`sent_to_lab_at`, vỡ `ck_smp_sent_has_request`) | UPDATE set đủ 4 cột; `greatest(:now, collected_at)` để không vỡ luôn CHECK thứ hai; 0 dòng khớp ⇒ **422 nêu mã mẫu** |
| **B3** | Bếp **hủy được** mẫu đang bị điều tra | Trạng thái **`HOLD`** + endpoint `POST /incidents/{code}/sample-hold`; `dispose()` chặn `HOLD`/`SENT_TO_LAB` **trước** mốc BR-111 |
| **B4** | Y tế **403** ở `meal-history`; `studentsWhoAte()` là dead code; khoanh vùng trả **toàn bộ món cả trường** | `meal-history` mở cho `MEDICAL_STAFF`; `GET /incidents/{code}/dishes/{dishCode}/students`; `suspected-dish-candidates` **giao với `incident_affected_students`** + xếp theo số em cùng ăn |

**Quyết định thiết kế cần nhớ (đừng hỏi lại):**

- **`HOLD` không cần migration.** `food_samples.status` là `VARCHAR(24)` **không có CHECK liệt
  kê giá trị** — đã grep cả 55 file migration để xác nhận trước khi thêm giá trị enum.
- **Mốc gia hạn `retention_deadline` = nạp lại đúng một chu kỳ BR-111 (24h) kể từ lúc giữ**,
  lấy hằng số từ `RetentionPolicy.MIN_RETENTION_HOURS` chứ **không gõ lại số 24**. Lý do chọn
  mốc này: nó gắn với một luật có sẵn, và khớp đúng cửa sổ điều tra BR-149 24h — vốn chính là
  nguyên nhân sinh ra bug (mẫu đủ điều kiện hủy ĐÚNG LÚC điều tra bắt đầu).
- **Chốt chặn thật của B3 vẫn là `hasOpenIncidentLinked`**, không phải deadline. Gia hạn
  deadline là lớp thứ hai để CHECK ở DB nói cùng câu chuyện với tầng service.
- **`applyDisposition` cố ý KHÔNG nhận `actorId`** — V20 không chừa cột cho người XỬ LÝ (chỉ
  có `quarantine_by_user_id` = người CÁCH LY). Danh tính người xử lý ghi ở `incident_events`.
  Thêm tham số rồi bỏ rơi nó là cách sinh ra bug `markQuarantined` cũ.
- **Giữ mẫu (`sample-hold`) tách hẳn khỏi gửi lab (`sample-retrieval`)** — trước đây muốn giữ
  mẫu tại trường là buộc phải khai khống rằng đã bàn giao đi xét nghiệm.

### PHÁT HIỆN MỚI, QUAN TRỌNG: Giai đoạn 1.4 **chưa thật sự thông**

Rà A1 thì lộ ra: `InspectionServiceImpl.parseUuid()` — hàm nuốt lỗi mà backlog A1 chỉ nói tới
trong ngữ cảnh `batchId` — **còn được dùng cho `dishId` của Mẫu số 2 và Mẫu số 3**.

FE gửi **MÃ MÓN** (`MenuMapper.toBlockDish` trả `dish.getCode()`), `UUID.fromString("DISH-…")`
ném, `parseUuid` trả `null` ⇒ `step2_rows.dish_id` / `step3_rows.dish_id` **vẫn NULL** kể cả
sau khi phiên trước sửa xong phía FE. `InspectionAssembler` map ngược bằng
`dishCode(r.getDishId())` ⇒ `null` ⇒ `dishChoicesOf()` vẫn ra **mảng rỗng** ⇒ **màn "Đăng ký
lưu mẫu" vẫn không có món nào chọn được**.

Nói cách khác: **1.4 được ghi là ✅ nhưng đường ghi vẫn đứt.** Nay `parseUuid` đã bị **xoá
hẳn**, thay bằng `resolveBatchId()` / `resolveDishId()` — tra mã thật, sai thì **422 nêu đích
danh**, không im lặng.

> Bài học lặp lại lần nữa: **sửa một đầu của cặp FE↔BE rồi tuyên bố xong là không đủ.** Cả 1.4
> lẫn A1 đều chết ở cùng một hàm nuốt lỗi ở đầu bên kia.

### Xác minh Giai đoạn 2 — ĐO THẬT 08/08/2026

Bốn lệnh (backend chạy tay, DB `sfms` vừa drop+create, `MENU_LEAD_TIME_DAYS=0`):

| Lệnh | Kết quả |
|---|---|
| `mvn test` | **352 pass, BUILD SUCCESS** (bằng đúng baseline, trước *và* sau khi sửa) |
| `npx tsc --noEmit` | sạch |
| `npx next lint` | **7 warning, 0 Error** — ÍT hơn baseline 8, không thêm cái nào |
| `npm run build` | thành công, **181 route** |

> ⚠️ **Baseline trong doc cũ đã lệch, sửa lại ở đây:** runbook ghi "136 route" — đo lại hôm nay
> là **181**. Phiên này KHÔNG thêm route nào (chỉ sửa file có sẵn); con số 136 là của trước lần
> merge `ac03d2e`. Dùng **181** làm baseline mới.

**Đo bằng API thật** (token System Admin trên DB trắng):

| Gọi | Kết quả |
|---|---|
| `GET /measure-units` | **200**, **13 dòng** — khớp đúng `measure_units` trong DB |
| `GET /measure-units` không token | **401** |
| 4 path mới có trong `/v3/api-docs` | `measure-units` · `incidents/{code}/sample-hold` · `incidents/{code}/quarantine-batches/{batchCode}/disposition` · `incidents/{code}/dishes/{dishCode}/students` — **đủ cả 4** |
| `QuarantineBatchDto` trong OpenAPI | có `disposition`, `dispositionAt` |
| `SuspectedDishCandidateResponse` | có `affectedStudentsWhoAte` |
| `POST …/sample-hold` bằng token SYSTEM_ADMIN | **403** (chỉ MEDICAL_STAFF) |
| `POST …/disposition` bằng token SYSTEM_ADMIN | **403** |
| `GET /students/{code}/meal-history` bằng token SYSTEM_ADMIN | **403** — đúng: danh sách vai là PARENT/TEACHER/**MEDICAL_STAFF**, sysadmin không nằm trong đó |

> ⚠️ **Chưa đo được, nói thẳng:** không có tài khoản MEDICAL_STAFF nào trên DB trắng (chỉ còn 1
> tài khoản System Admin), nên **chưa gọi được `meal-history` bằng đúng token y tế** để thấy
> 200. Đã đo gián tiếp: annotation có hiệu lực (sysadmin 403, không phải "endpoint không tồn
> tại"). **Phải kiểm lại khi dựng đủ 7 vai theo runbook mục 11.**
>
> ⚠️ Tương tự, 3 endpoint ghi của luồng sự cố mới chỉ đo được **tầng phân quyền** — chưa có
> sự cố/lô/mẫu nào trong DB để chạy hết đường nghiệp vụ.

**Phát hiện phụ (không do phiên này gây ra):** gọi SAI METHOD vào endpoint POST của MF11 trả
**500** chứ không phải 405 — đo trên cả `/assess` (có từ trước) lẫn endpoint mới, nên là hành
vi sẵn có của tầng xử lý lỗi, không phải hồi quy. Đáng sửa ở đợt dọn lỗi chung.

**Đã đo và đã sửa nhờ đo:** `factorToBase` thật là `kg=1.0, g=0.001, lít=1.0, ml=0.001` ⇒ đơn
vị CƠ SỞ là **kg/lít**, không phải g/ml. Javadoc `MeasureUnitController` viết sai lúc đầu, đã
sửa — đúng kiểu lỗi mà "đọc code rồi suy" sinh ra.

### ✅ GIAI ĐOẠN 2 — ĐO THẬT XONG (08/08/2026, phiên sau)

Dựng đủ dữ liệu **chỉ bằng API thật** (script `07_implement/seed/`) rồi chạy hết đường:

- `meal-history` bằng **token MEDICAL_STAFF** → **200 kèm đủ danh sách món**.
- **Sự cố `INC-2026-002` đóng được — `status = CLOSED`**, sau đủ chuỗi khoanh vùng → giữ mẫu
  → cách ly lô → CAPA (duyệt/nhận/bằng chứng/nghiệm thu) → xử lý lô. Bấm `close` sớm trả
  đúng **409 IEM-217**.
- `suspected-dish-candidates` **khác nhau giữa 2 sự cố cùng ngày** (2 em vs 1 em).

Nhưng đo cũng lộ **4 lỗi chặn cứng chưa ai biết**, đã sửa trong phiên đó: MF06 trả 403 cho
đúng tài khoản Đơn vị suất ăn · MF11 làm CAPA không ai nhận được ⇒ sự cố không đóng được ·
`ClassCastException` khiến Bước 1 kiểm thực luôn 500 khi khai lô có thật · sai
content-type/method ⇒ 500 thay vì 415/405.

👉 **Chi tiết + số liệu: `10_E2E_API_Run_Report.md`.**

### Còn lại của Giai đoạn 2

- ✅ **B1 đã nối FE.** `medical-staff/incidents/[id]/page.tsx`: badge in cứng
  `"Chờ Caterers hoàn trả"` — trạng thái không bao giờ tiến được — nay thay bằng `disposition`
  thật, cộng 2 nút **Trả NCC** / **Huỷ bỏ**, và nhãn `"Chưa xử lý — chặn đóng sự cố"` để người
  dùng thấy đúng thứ đang chặn nút Đóng.
- ⬜ **FE chưa nối 3 endpoint còn lại:** `sample-hold` (nút "Giữ mẫu" cho y tế),
  `dishes/{dishCode}/students` (chiều ngược khoanh vùng), và màn tra `meal-history` cho y tế
  (chưa có `medical-staff/students*` nào).
- ⬜ **B5** bản đồ truy xuất rỗng nghĩa · **B6** 2 màn chi tiết CAPA.
- ⬜ `retrievableForIncident` vẫn bỏ qua `suspectedDishIds`.
- ⬜ **Chạy kịch bản E2E thật** sau khi dựng đủ 7 vai — mọi thứ trên mới chỉ đo tới tầng
  phân quyền + hợp đồng API.

---

## Kiểm commit `wip(mf02)` 598b1f6 — màn tải lên danh sách nhân sự (08/08/2026)

Commit này **không phải việc của phiên trước**; Nam commit lại để không mất khi chuyển máy và
nhờ người tiếp quản kiểm. **Kết luận: chạy được, nhưng CHƯA xong — đã sửa 5 chỗ.**

**Chặn đầu tiên, phải làm trước khi kiểm được gì:** `package.json` có `exceljs ^4.4.0` nhưng
`node_modules` chưa có ⇒ `npx tsc --noEmit` **chết 4 lỗi** (`Cannot find module 'exceljs'` ×2,
`row`/`rowNumber` implicit `any` ×2). Nam ghi "đã xác minh tsc sạch" là đúng — **trên máy đã
`npm install`**. Đã thêm cảnh báo vào runbook mục 3.

Sau `npm install`, 5 vấn đề thật:

| # | Vấn đề | Hệ quả |
|---|---|---|
| 1 | `handleFile()` **không xoá `parsed`/`step`** khi file mới hỏng | Chọn file tốt → chọn file hỏng ⇒ vẫn thấy bảng cũ, nút "Xác nhận nhập" vẫn bật ⇒ **tạo tài khoản theo file người dùng đã bỏ đi** |
| 2 | Màn kết thúc in `validRows.length` | 3/5 dòng bị server từ chối (email trùng, MSG-006) vẫn hiện **"Nhập thành công 5"**, mâu thuẫn ngay với toast bên cạnh báo 2. Thêm state `createdCount`, hiện `"x/y"` + tách rõ "lỗi định dạng" với "máy chủ từ chối" |
| 3 | `parseExcel` **bỏ dòng 1 vô điều kiện** | File người dùng tự gõ (không tải template) **mất nhân viên đầu tiên**. Nay chỉ bỏ khi dòng 1 thật sự là header |
| 4 | `file.name.endsWith(".xlsx")` phân biệt hoa thường | File `.XLSX` rơi vào nhánh CSV rồi báo "không đọc được" |
| 5 | Khối kiểm BR-012/BR-013 **nhân đôi** ở `parseExcel` và `parseCsv` (~15 dòng ×2) | Sửa luật một nhánh quên nhánh kia ⇒ Excel và CSV nhận **hai bộ luật khác nhau**. Gom về `validateRow()` + `normalizeRole()` dùng chung |

Phần còn lại (stepper, template Excel có data-validation dropdown, `POST /staff` từng dòng,
`{ handledError: true }`) **đúng và không cần đổi**.

---

## BR-061 — bỏ khoá "tạo thực đơn trước 3 ngày" khi test (08/08/2026)

**Vấn đề Nam gặp:** không tạo được thực đơn cho ngày hôm nay nên không chạy thử được luồng;
chèn thẳng vào DB thì sai lệch nhiều.

**Nguyên nhân:** backend ĐÃ cấu hình được từ trước (`sfms.menu.lead-time-days`, env
`MENU_LEAD_TIME_DAYS`, mặc định 3 — `application.yml:143`), nhưng **FE hardcode `+3`** ở
`menus/page.tsx` nên đổi cấu hình server không có tác dụng gì lên UI. Cùng lớp lỗi "hằng số
nhân đôi" đã bỏ ở `NUTRITION_TARGET`.

**Đã sửa — không xoá luật, làm cho nó cấu hình được đầu-cuối:**
- `SchoolConfigResponse.menuLeadTimeDays` — server đọc chính property mà `MenuServiceImpl`
  dùng để CHẶN, trả kèm ở `GET /schools/{code}/config` (đã mở cho SERVICE_CATERERS).
  KHÔNG lưu ở `school_config`: đây là cấu hình TRIỂN KHAI, không phải tham số từng trường.
- `SchoolConfigMapper.toResponse(config, menuLeadTimeDays)` — field không có trên entity.
- FE `lib/school-config.ts` (`useSchoolConfig` / `useMenuLeadTimeDays`); `menus/page.tsx` và
  `menu-form.tsx` đọc từ đó. Chưa tải xong ⇒ tạm khoá mọi ô, **không đoán 3**.

### Cách chạy để test trong ngày

```bash
cd 04_Development/Backend
MENU_LEAD_TIME_DAYS=0 JAVA_HOME="/c/Program Files/Java/jdk-25.0.3" mvn spring-boot:run
```

Mặc định trong `application.yml` VẪN LÀ 3 — production không đổi hành vi.

**Đã đo với `MENU_LEAD_TIME_DAYS=0`:**
- `GET /schools/{code}/config` → `{"geofenceRadius":200,"reportEmails":[],"menuLeadTimeDays":0}`
- `POST /menus` ngày **hôm nay** → **201**
- `POST /menus/{code}/submit` → **200**, `status = PENDING_APPROVAL`

> ⚠️ Nhớ bỏ biến môi trường khi demo cho khách nếu muốn thể hiện đúng BR-061.

---

## PHIÊN 4 (08/08/2026, chiều) — RÀ UI BẰNG TRÌNH DUYỆT THẬT RỒI SỬA

**Cách làm:** Playwright MCP điều khiển Chromium thật, đăng nhập **6 vai**, đi các màn chính
trên dữ liệu vừa dựng bằng `seed/`. Mọi phát hiện đều **nhìn thấy trên màn hình** rồi **đối
chiếu ngược API** để biết lỗi ở FE hay BE. Báo cáo đầy đủ: **`11_UI_Audit_Report.md`**.

**Xác minh:** `mvn test` 352 pass · `tsc --noEmit` sạch · `next lint` 7 warning / 0 error ·
`next build` sạch · rebuild compose rồi **đo lại bằng API**.

### Đã sửa hết P0 + P1 + phần lớn P2 (bảng chi tiết trong `11_UI_Audit_Report.md`)

Đo lại sau khi sửa:

```
incidents:                        INC-2026-005 → 004 → 003 → 002   (trước: 002 → 004 → 005 → 003)
samples?incidentId=INC-2026-005:  total = 1                        (trước: 0, im lặng)
inspections formHeader:           location "Thanh Đa, Phúc Thọ, Hà Nội" · checkedAt 13:34
                                  (trước: "Bếp trung tâm" · 06:34 — lệch 7 tiếng)
measure-units:                    13 đơn vị                        (FE trước khai cứng 10)
```

### ⚠️ BA LỖI GỐC — đọc kỹ, chúng KHÔNG nhìn thấy được từ màn hình

1. **`(app)` chưa từng có route guard.** `(app)/layout.tsx` render thẳng `AppShell`, `AppShell`
   render thẳng `children`. Store CÓ dọn phiên khi refresh token chết nhưng **không ai đọc
   `status`** ⇒ mất phiên hiện y hệt "dữ liệu rỗng" (đo được ở màn Kho: "Không có bản ghi
   nào."). Nay có **`components/app/auth-gate.tsx`** bọc toàn bộ `(app)`; nó cũng dập luôn cơn
   bão 7×401 mỗi lần tải trang vì `children` chỉ mount khi `authenticated`.
2. **`FoodSampleResponse.incidentId` trả UUID nội bộ** (vi phạm D1) và `FoodSampleSpec` so
   **chuỗi mã** với **cột UUID** ⇒ `GET /samples?incidentId=INC-…` **luôn rỗng, IM LẶNG**. Đây
   mới là lý do hồ sơ đang giữ mẫu vẫn ghi "Chưa gửi mẫu nào", không phải lỗi hiển thị. Đã sửa
   cả hai chiều (assembler trả mã + resolve mã→UUID ở service), áp cho cả `sample-lab-requests`.
3. **`ListQuery.toPageable()` trả `Sort.unsorted()`** — Javadoc của chính nó đã ghi "chưa có
   sign-off của Nam". Đây là gốc của "sự cố liệt kê lộn xộn". **Nam ĐÃ CHỐT (a) + kèm luật:**
   giữ `unsorted()` làm mặc định (revert sẽ vỡ MF03 ngay), nhưng **mọi danh sách người dùng đọc
   theo THỜI GIAN phải tự truyền `Sort` ở call-site**. Luật ghi trong Javadoc `ListQuery`;
   call-site đầu tiên áp dụng là `IncidentServiceImpl.list()`. **Khi thêm endpoint danh sách
   mới: hỏi "người dùng có kỳ vọng thứ tự nào không?" — có thì truyền `Sort` tường minh.**

### Yêu cầu thêm của Nam: ô có ràng buộc phải là dropdown

Rà toàn FE đối chiếu `CHECK` constraint trong migration — enum nghiệp vụ **hầu hết đã là
`Select`**. Đúng một nhóm còn hở và là nhóm tệ nhất vì server validate ngầm: **đơn vị đo**.
Nay cả 4 chỗ đọc `GET /measure-units` qua **`lib/measure-units.ts`** (`useMeasureUnits()`,
khuôn `lib/diet-groups.ts`); đã **xoá `OFF_CATALOG_UNIT_OPTIONS`**. Xem `08_…Backlog.md` mục D5.

---

### 🔴 Sửa thêm cuối phiên — Nam báo "màn Lịch thực đơn không nhập được"

URL Nam gửi: `/service-caterers/menus?grade=Khối 1&week=0`. **Không phải trang lỗi** — trang
render bình thường nhưng **mọi ô đều ghi "Đang tải cấu hình…" và không có nút "+ Thêm menu"
nào**, vĩnh viễn.

**Chuỗi nhân quả (đo từng bước, không suy):**

```
profile/me cua Don vi suat an  →  schoolId = null, partnerId = "CAT-201"
useSchoolConfig() đọc user.schoolId  →  undefined  →  query bị disable
useMenuLeadTimeDays()  →  undefined
menus/page.tsx:473   minCreate = hôm nay + (undefined ?? 36500) ngày
menus/page.tsx:750   canAdd = day >= minCreate  →  FALSE với mọi ngày
```

`?? 36500` là **cố ý** ("chưa biết lead time thì khoá, đừng đoán 3" — đúng, vì đoán chính là
lỗi vừa bỏ). Nhưng nó biến một cấu hình-không-về-được thành **khoá cứng câm lặng**.

**Hai tầng phải sửa, không phải một:**
1. `ProfileResponse.activeSchoolId` (mới) — server suy trường từ hợp tác đang hiệu lực cho
   SERVICE_CATERERS; FE `useSchoolConfig` đọc `activeSchoolId ?? schoolId`.
2. `SchoolConfigService.requireInScope()` — vẫn `belongsToSchool()` nên **kể cả có đúng mã
   trường vẫn SCH-404**. Đây là chỗ 06/08 đã thêm role vào `@PreAuthorize` mà quên phạm vi.

**Đo lại sau khi sửa:** `activeSchoolId = "SCH-0001"` · `GET /schools/SCH-0001/config` với token
Đơn vị suất ăn → `{"geofenceRadius":200,"reportEmails":[],"menuLeadTimeDays":0}` · mở trình
duyệt thật: ô Thứ 7 08/08 hiện **"+ Thêm menu"**, các ngày đã qua hiện "≥0 ngày (BR-061)" (đúng).

**Bài học ghi vào backlog mục `D1-BIS`:** đây là **lần thứ 3** của lớp lỗi "tài khoản đối tác
không có `schoolId`" (sau MF06 403 và MF11 giấu CAPA). **Việc còn lại: rà nốt** — `grep` các
chỗ còn giả định `user.schoolId` tồn tại, ở cả FE lẫn BE, trước khi người dùng vấp tiếp.

---

### 🔴 Đợt 2 — Nam rà 12 màn, 3 quyết định lớn (08/08/2026, tối)

Nam đi 12 URL thật và đặt câu hỏi; em đo từng cái bằng API + code. Kết quả và 3 quyết định:

#### (a) ĐƠN MUA HÀNG BẮT BUỘC NẰM TRONG DANH MỤC ĐÃ DUYỆT — Nam chốt, đã làm

**Đo được trước khi sửa:** `partnership_catalog_items` của PTN-201 **rỗng hoàn toàn**,
`catalog-change-requests` cũng **rỗng** — vậy mà đơn mua hàng tạo được, giao được, nhập kho
được trọn vẹn. Nguyên nhân: hệ thống có **hai danh mục song song** và
`PurchaseRequestService.create()` **không kiểm cái nào**:

| Bảng | Ai khai | Có cổng duyệt? |
|---|---|---|
| `supplier_products` (MF06) | NCC tự khai giá | **KHÔNG** |
| `partnership_catalog_items` (MF04) | qua `catalog-change-request` → trường duyệt | Có |

Toàn bộ luồng duyệt danh mục (code có sẵn từ lâu) vì thế **chưa từng chạy thật lần nào**.

**Đã làm:** `ReferenceResolver.isIngredientInApprovedCatalog()` + cổng chặn ở
`PurchaseRequestService.create()`, mã lỗi mới **`ING-077` / MSG-154**. Nguồn sự thật là
`partnership_catalog_items`, KHÔNG phải `supplier_products`.

**Đo lại sau khi sửa** (cùng NCC, cùng trường):
```
ING-GAO-TE  (chưa duyệt)  → 422 ING-077 "not in the school-approved contract catalog"
ING-CA-BASA (đã duyệt)    → 201, đơn PO-TEST-CATALOG tạo bình thường
```

> ⚠️ **Kéo theo: `seed/seed_demo.py` đã phải thêm bước duyệt danh mục** (NCC đề xuất →
> trường duyệt) trước khi đặt hàng. Không thêm thì seed đứt ở bước `POST /purchase-requests`.
> Đây là bằng chứng cổng thật sự có tác dụng.

#### (b) MẪU 3 CỘT (7) → ĐẠT/KHÔNG ĐẠT — Nam chốt, đã làm

Cột "Dụng cụ chia, chứa đựng, che đậy" đang là `VARCHAR(300)` nhập tay với gợi ý
*"VD: Khay inox có nắp"* — người kiểm gõ TÊN dụng cụ, trong khi biểu mẫu pháp lý cần biết
dụng cụ có **ĐẠT** yêu cầu vệ sinh hay không. Tên dụng cụ không trả lời được câu đó và không
so sánh/thống kê được giữa các phiên.

**Đã làm:** migration **`V56__mf07_step3_utensils_pass.sql`** thêm `utensils_pass BOOLEAN`
+ ràng buộc `ck_s3_utensils_evidence` (Không đạt ⇒ bắt buộc ảnh, cùng luật `sensory_pass`).
Cột `utensils` cũ **giữ nguyên làm ghi chú** — đây là biên bản pháp lý, không xoá dữ liệu.
Dữ liệu cũ: đã ghi chữ ⇒ suy ra Đạt (không tự kết luận "Không đạt" cho thứ mình không đo được).
FE dùng lại `CriterionBlock` (cùng khuôn cột 8/9); bản in Mẫu 3 in dấu ✓ thay vì chữ.

#### (c) ĐẶT NGUYÊN LIỆU THEO THỰC ĐƠN — đã làm

Hoá ra backend + FE **đã có sẵn 80%**: `orders/new` đã nổ BOM (định lượng món × số suất, có
quy đổi đơn vị) và gọi `GET /inventory/issue-plan` để trừ tồn FEFO. Hai thứ THIẾU:

1. **Không có đường vào** — chỉ tính được khi deep-link `?date=&meal=` từ màn Xuất kho, và
   chỉ **một** bữa. Vào thẳng màn thì không có cách nào lấy nhu cầu từ thực đơn.
2. **Sửa số lượng không để lại dấu vết.**

**Đã làm:** khối "1 · Chọn buổi cần đặt nguyên liệu" liệt kê mọi (ngày × bữa) có thực đơn đã
công bố từ hôm nay trở đi, **chọn nhiều buổi**, nhu cầu **cộng dồn** thành một đơn (mỗi
nguyên liệu một dòng). `useQueries` gọi `issue-plan` cho từng buổi rồi cộng phần thiếu — không
gộp một lời gọi vì endpoint tính FEFO **theo bữa**, gộp lại sẽ phải bịa một ngày đại diện.
Mỗi dòng hiện "Đề xuất N kg"; sửa khác đề xuất thì ô ghi chú **đổi thành "Lý do điều chỉnh *"
và bắt buộc**, lý do ghép vào `note` của `purchase_items` để bên duyệt giá đọc ngay tại dòng.

#### Sửa kèm trong đợt này

- **`supplier/certificates`** — banner *"Chứng nhận VietGAP — Rau củ hết hạn 28/07/2026 (còn
  dưới 30 ngày)"* là **chuỗi IN CỨNG**, hiện ra kể cả khi NCC chưa nộp tài liệu nào, và ngày
  đó đã quá hạn 11 ngày. Nay tính từ dữ liệu thật; rỗng thì hiện "Chưa nộp hồ sơ tuân thủ nào".
- **`service-caterers/meal-service`** — bỏ câu *"chưa được **seed** danh sách học sinh trong
  **mock**"* (từ của lập trình viên, và hệ thống không còn mock nào từ 06/08).
- **Chi tiết bữa ăn** — hiện số suất cho **món chính** (`MenuBlockDish.portions` có sẵn nhưng
  chỉ món THAY THẾ được hiện số suất; bếp mở ra không biết nấu bao nhiêu suất mỗi món).

#### Đo được, CHƯA sửa — đã ghi vào backlog

| Màn | Kết luận |
|---|---|
| `supplier/products` | `SupplierProduct` có 11 trường, bảng hiện **5** — thiếu `unit`, `conversionToBase`, `conversionNote`, `active` |
| `supplier/delivery-staff` | `driverId` là **tuỳ chọn** khi tạo chuyến giao — giao thực phẩm không có người giao đích danh thì mất truy vết |
| `supplier/batches` | **`SupplierBatchStatus.DELIVERED` KHÔNG được gán ở bất kỳ đâu**, `quantity` không bao giờ bị trừ ⇒ lô NCC là "giếng không đáy". Cùng gốc với mục A8 |
| `service-caterers/class-deliveries` | Không phải lỗi — `class_meal_deliveries` chưa có bản ghi nào, tức bước "bếp giao suất tới lớp" **chưa từng chạy thật**; seed bỏ sót mắt xích này |
| `service-caterers/menu-templates` | Không phải lỗi — "Thuc don trua 08/08 - Khoi 1" là **tên thực đơn** (`Menu.name`), không phải tên mẫu. Nhưng chip nằm dưới nhãn "TIÊU CHUẨN" nên nhìn y như thực đơn mẫu ⇒ gây hiểu nhầm |
| Kiểm thực bước 1 "không chọn được" | Không phải lỗi — phiên đã Đạt cả 3 bước ⇒ `<fieldset disabled>` khoá toàn bộ (biên bản pháp lý đã chốt thì không sửa, đúng). **Nhưng affordance sai**: vẫn vẽ ô nhập/dropdown xám thay vì hiển thị dạng chữ |

---

### ✅ Đợt 3 — xử lý nốt danh sách còn treo (08/08/2026, cuối phiên)

| Việc | Đã làm | Đo lại |
|---|---|---|
| **Lô NCC là "giếng không đáy"** | Một lô chỉ nhận MỘT lần (409 nếu nhận lại) + gán `DELIVERED` khi vào kho + migration **`V57`** backfill dữ liệu cũ theo bằng chứng | 4/4 lô demo chuyển `DELIVERED` |
| **`driverId` tuỳ chọn** | Bắt buộc, kèm câu hướng dẫn khai nhân viên ở màn nào | thiếu ⇒ 422 `driverId: required — chuyến giao phải có nhân viên giao hàng đích danh` |
| **`supplier/products` thiếu 6/11 trường** | Thêm cột **Đơn vị bán** + **Quy đổi** (hiện `conversionNote` qua tooltip) | — |
| **Seed bỏ sót "bếp giao suất tới lớp"** | Thêm `POST /class-meal-deliveries` + giáo viên `/receive` | — |
| **Chip thực đơn nhìn như thực đơn mẫu** | Tách icon: `LayoutTemplate` cho món GẮN TỪ MẪU, `UtensilsCrossed` cho món soạn trực tiếp; tooltip nói rõ | — |
| **Bước kiểm thực đã chốt nhìn như màn hỏng** | Lớp `.sfms-readonly` (globals.css) bỏ khung/nền ô nhập ⇒ đọc như CHỮ; đổi câu thông báo thành Alert "Biên bản đã chốt — chỉ xem" | — |

**Giới hạn ghi rõ để không ai nới nhầm sau này:** `receiving_lines` **không có cột số lượng**,
nên "nhận lô" = **nhận trọn lô**. Muốn giao nhiều đợt cho cùng một lô thì phải thêm cột đó
TRƯỚC, rồi mới đổi luật thành "tổng đã nhận ≤ lượng đăng ký". Nới luật mà không thêm cột là
quay lại đúng lỗi cộng khống tồn (50 kg → kho 100 kg).

**Dữ liệu demo:** đã xoá bản ghi test (`PO-TEST-*`, `CCR-TEST-*`) và duyệt đủ 4 mặt hàng vào
danh mục hợp đồng (`PCI-0001..0004`) để luồng đặt hàng chạy được trên DB hiện tại.

---

### 🔍 Đợt 4 — quét TOÀN BỘ màn hình của 6 vai (08/08/2026)

**Cách làm:** nạp từng route trong `iframe` cùng phiên rồi đọc DOM đã render — rẻ hơn chụp ảnh
từng màn và vẫn thấy được nội dung thật. Phạm vi: **toàn bộ mục điều hướng** của
`lib/roles.ts` — 15 màn System Admin · 18 BGH · 13 Y tế · 21 Đơn vị suất ăn · 8 Giáo viên ·
9 Nhà cung cấp. **Không quét được vai Phụ huynh — chưa có tài khoản test.**

**Kết quả nền:** 0 màn crash, 0 màn trắng, 0 màn bị đá về đăng nhập. Các bản sửa trong ngày
đều thấy có hiệu lực trên màn thật (giấy tờ tuân thủ, sự cố mới nhất, biên bản chỉ-xem…).

#### 🔴 Lớp lỗi "đối tác không có `schoolId`" — VẤP LẦN THỨ TƯ, đã sửa

`GET /partners` trả **rỗng** cho Đơn vị suất ăn ⇒ màn *"Nhà cung cấp & Sản phẩm"* ghi "Không
tìm thấy nhà cung cấp hợp tác nào" trong khi trường đang có hợp tác **APPROVED** với SUP-202.
Nguyên nhân y hệt ba lần trước: `PartnerServiceImpl` gọi
`partnerIdsConnectedToSchool(actor.schoolId())` → `null` → danh sách rỗng → `cb.disjunction()`.

**Lần này không vá riêng nữa** — thêm **`common/scope/ActorScope.schoolIdOf(actor, refs)`** làm
chỗ DUY NHẤT trả lời "trường nào áp cho tài khoản này", kèm Javadoc liệt kê cả bốn lần vấp để
người sau không lặp lại. `Mf06Scope`/`Mf11Scope` tạm giữ nguyên (đang chạy ổn), nên cho ủy
quyền về `ActorScope` khi có dịp đụng tới. **Đo lại:** `total = 2` (CAT-201 + SUP-202).

#### Sửa kèm: chữ "mock" lọt ra người dùng (lần thứ hai trong ngày)

- `system-admin/audit-logs`: *"mọi thao tác **mock** (tạo/cập nhật/duyệt)…"* → bỏ chữ mock.
- `medical-staff/incidents/[id]`: nhãn nút *"Upload nhiều file ảnh/PDF **(mock)**"* → "Chọn
  nhiều tệp ảnh hoặc PDF".

> Hệ thống hết mock từ 06/08/2026 nhưng chữ "mock"/"seed" vẫn còn rải rác trong **chuỗi hiển
> thị**. Nên có một lượt grep riêng cho toàn bộ text người dùng đọc được.

#### ⬜ Đo được, CHƯA sửa — cần quyết định

| Màn | Đo được | Vì sao đáng quan tâm |
|---|---|---|
| `school-admin/dashboard` | Năm học **Active** là **2027–2028** (`2027-09-05 → 2028-05-31`) trong khi hôm nay là **08/08/2026** | Không có ràng buộc nào chặn kích hoạt năm học mà khoảng ngày KHÔNG chứa hôm nay. BR-191 chỉ bắt "một năm Active", không bắt "đang trong kỳ" ⇒ mọi màn lọc theo năm học Active đang chạy trên một năm chưa bắt đầu |
| `school-admin/reports` | "Mẫu lưu đúng quy định **0%**" | Có 2 mẫu lưu hợp lệ trong DB. Cần đối chiếu công thức tính của báo cáo tuân thủ |
| `medical-staff/compliance` | Tiêu đề ghi **02/07/2026** trong khi khối ngay dưới ghi "Cố định theo ngày hiện tại" (08/08/2026) | Hai mốc thời gian mâu thuẫn trong cùng một màn giám sát |
| `service-caterers/announcements` | Dòng mô tả bắt đầu bằng `— ·` (tên trường rỗng) | Cùng chỗ này ở vai Y tế/Giáo viên hiện đúng tên trường ⇒ lại là `schoolId` của tài khoản đối tác |

---

### ✅ Đợt 5 — xử lý toàn bộ lỗi tìm được khi quét màn (08/08/2026)

| # | Lỗi | Gốc rễ | Đo lại |
|---|---|---|---|
| 1 | Dashboard BGH ghi "Năm học 2027–2028 **(Active)**" trong khi hôm nay là 08/2026 | BR-191 chỉ bắt "≤1 năm Active", KHÔNG ràng buộc khoảng ngày | BE chặn kích hoạt năm **đã kết thúc**; FE ghi rõ **"(Active — chưa bắt đầu, từ 05/09/2027)"** |
| 2 | Báo cáo tuân thủ: "Mẫu lưu đúng quy định **0%**" | `collected_at` là **timestamptz** so với `<= to` kiểu **date** ⇒ Postgres nâng vế phải thành `00:00:00` ⇒ **loại sạch** bản ghi phát sinh sau nửa đêm ngày cuối kỳ | `< (to::date + 1)` cho cả hai kiểu cột ⇒ **100%, total 2** |
| 3 | `medical-staff/compliance` ghi ngày **02/07/2026** | `const TODAY = "02/07/2026"` in cứng ở cấp module — và còn dùng để CHỌN phiên kiểm thực/mẫu "hôm nay" | Nay tính thật ⇒ tiêu đề **08/08/2026**, và khối "hôm nay" nhận ra **"Hồ sơ kiểm thực 3/3 bước hoàn thành"** (trước đó trống) |
| 4 | Màn Thông báo của bếp mở đầu bằng `— ·` | `user.school` rỗng vì tài khoản đối tác không có `schoolId` | Thêm `ProfileResponse.activeSchool` + selector dùng chung **`useSchoolLabel()`** (thay 8 chỗ `user?.school ?? "—"`) ⇒ hiện đúng tên trường |
| 5 | `service-caterers/suppliers` ghi "Hợp tác (0)" | FE lọc `type: "SUPPLIER"`, giá trị D6 thật là **`RAW_FOOD_SUPPLIER`** — server lọc EXACT nên khớp 0 dòng, **không 4xx, chỉ rỗng im lặng** | **Hợp tác (1) — Công ty Thực phẩm nguyễn Nam** |

> Lỗi #5 chỉ lộ ra vì em kiểm lại sau khi sửa `/partners`: API đã trả `total = 2` mà màn vẫn 0.
> **Sửa xong phải đo lại ĐÚNG MÀN đó, không dừng ở tầng API** — đây là lần thứ hai trong ngày.

#### Bằng chứng sự cố: FE đang bịa số lượng ảnh

`medical-staff/incidents/[id]/evidence` đếm **số lần bấm nút** (`setShots(n => n + 1)`) rồi gửi
`fileCount` lên server — hồ sơ điều tra ghi "N tệp đính kèm" trong khi **không tồn tại tệp nào**.
Đã thay bằng `<input type="file" capture>` thật nên con số phản ánh ảnh người dùng chọn.

⚠️ **Giới hạn còn lại (backend):** `POST /incidents/{code}/evidence` chỉ nhận
`{type, description, fileCount}`; **`/incidents/{code}/evidence-photos` KHÔNG tồn tại** — chỉ
`source-reports` và `corrective-actions` mới có endpoint upload. Nên ảnh bằng chứng sự cố vẫn
nằm ở máy người dùng. Việc còn lại: thêm endpoint upload cho evidence của sự cố.

#### Rà chuỗi lập trình viên lọt ra người dùng

Viết bộ quét bóc comment rồi soi text JSX + prop `title/description/placeholder/label`:
**còn đúng 1 chuỗi** ("(Mock: mỗi lần bấm…)" — chính chỗ trên, đã sửa cùng lúc). Cộng 2 chuỗi
sửa ở đợt 4 (`audit-logs`, nhãn upload) là hết.

---

### ✅ Đợt 6 — quét nốt vai PHỤ HUYNH + lưu thật ảnh bằng chứng (08/08/2026)

#### Dựng được tài khoản Phụ huynh để test

Ba tài khoản `parent{1,2,3}.e2e@example.test` đã tồn tại (sinh kèm khi tạo học sinh) nhưng ở
`PENDING_VERIFICATION`, và `account_activation_tokens` **chỉ lưu hash** nên không lấy lại được
token. Cách làm (ghi lại để lần sau khỏi mò):

```bash
docker compose -f docker-compose.yml -f docker-compose.mailpit.yml up -d backend   # SMTP → Mailpit
curl -X POST /api/v1/auth/activate/resend  -d '{"email":"parent1.e2e@example.test"}'
# đọc token từ http://localhost:8026/api/v1/messages
curl -X POST /api/v1/auth/activate -d '{"token":"…","newPassword":"ParentE2e@2026x"}'
docker compose up -d backend                                                       # trả về Gmail
```

**Tài khoản test: `parent1.e2e@example.test` / `ParentE2e@2026x`.**

#### 🔴 PHỤ HUYNH KHÔNG XEM ĐƯỢC THỰC ĐƠN — cả 3 màn, đã sửa

`/parent/weekly-menu`, `/parent/school-meals`, `/parent/dashboard` đều ghi *"Chưa có thực đơn
nào được công bố cho Khối 1"* trong khi thực đơn hôm đó **đã PUBLISHED** — và
`GET /menus` **không lọc** trả đúng 1 bản ghi cho chính token phụ huynh đó.

**Hai tầng cùng hỏng, phải sửa cả hai mới thấy dữ liệu:**

1. **FE lọc sai tên tham số** — gửi `grade=Khối 1`, nhưng `MenuSpec.FIELD_MAP` chỉ nhận
   `gradeId` (UUID) ⇒ server trả **422 MENU-422 "unknown filter field"**, TanStack Query nuốt
   thành danh sách rỗng. Cùng lỗi đã vá ở `menus/diet-coverage` hồi 07/08 (V44) nhưng ba màn
   Phụ huynh bị bỏ sót.
2. **`GET /grades` chặn vai PARENT** (403 SCH-005) — mà đó chính là chỗ đổi nhãn "Khối 1" →
   UUID. Sửa xong tầng 1 vẫn rỗng vì `gradeId` không bao giờ resolve được.

Đã sửa: ba màn dùng `useGradeId()` + `enabled: Boolean(gradeId)`; `GradeController.list` mở
thêm `PARENT` (danh mục tham chiếu, không PII — cùng lập luận `diet-groups`/`measure-units`).
**Đo lại:** *"08/08/2026 Khối 1 BỮA TRƯA Thit kho trung, Canh rau cai"* · dashboard *"Bữa trưa
hôm nay 2 món · 380 kcal"* · school-meals *"Đã công bố (1 ngày)"*.

> Bài học lặp lại lần thứ ba: **lỗi 4xx bị nuốt thành "danh sách rỗng"**. Màn hình nói "chưa có
> dữ liệu" trong khi server đang trả lỗi. Đáng cân nhắc: cho `usePagedList` hiện lỗi thay vì
> rơi về mảng rỗng khi response là 4xx.

#### ✅ Ảnh bằng chứng sự cố nay LƯU THẬT (V58)

Hoàn tất phần còn nợ ở đợt 5. `mf11_attachments` trước chỉ nhận `SOURCE_REPORT` và
`CORRECTIVE_ACTION`; **V58** nới CHECK thêm `INCIDENT_EVIDENCE`, kèm:

- `POST /incidents/{code}/evidence-photos` (multipart, `MEDICAL_STAFF`) + `GET …/{fileId}`;
- **`addEvidence()` chốt `fileCount` theo số ảnh THẬT đã lưu**, không tin số client khai —
  cùng cách `CapaServiceImpl.submitEvidence()` đã làm;
- FE upload TRƯỚC rồi mới ghi bản ghi bằng chứng (đảo thứ tự sẽ ghi ra 0 tệp).

**Đo lại:** `POST /incidents/INC-2026-004/evidence-photos` → **HTTP 200**.

#### Trạng thái quét màn: ĐỦ 7/7 VAI

98 mục điều hướng · 0 màn crash · 0 màn trắng. Vai Phụ huynh là vai cuối cùng, và cũng là vai
lộ ra lỗi nặng nhất của cả đợt — **đúng vai mà trước đó không có tài khoản để test**.

---

### ✅ Đợt 7 — dọn backlog tồn (08/08/2026)

| # | Việc | Đã làm | Đo lại |
|---|---|---|---|
| 1 | **Query hỏng đội lốt "danh sách rỗng"** | Thêm `QueryCache.onError` (trước chỉ MUTATION có lưới bắt lỗi) + `DataTable` có nhánh **"Không tải được dữ liệu — đây KHÔNG phải danh sách rỗng"** | — |
| 2 | **Rà nốt lớp lỗi `schoolId`** | Đo bằng API 20 endpoint với token bếp ⇒ tìm ra **`GET /students` trả `total = 0`** | `total = 3` |
| 3 | Thanh tiến trình kho vẽ sai tỉ lệ | Chia theo **tổng số lô**, không theo lô nhiều nhất; thêm % | 2 lô = 50%, không còn 100% |
| 4 | Cột "Còn lại / 24h" hiện `28h` | Đổi nhãn thành **"Còn lại trong hạn lưu"** — 24h là mức SÀN (BR-111), mẫu bị GIỮ được đẩy hạn dài ra | — |
| 5 | Học sinh mới tạo đã "Đã xác minh" | Thêm giá trị dẫn xuất **`NO_DECLARATION`** → nhãn "Chưa khai báo" | `healthStatus = NO_DECLARATION` |
| 6 | `404 /favicon.ico` mọi trang | Thêm `src/app/icon.svg` | HTTP 200 |
| 7 | "đang biên soạn" | → "đang tổng hợp" | — |
| 8 | **D5-FE `UNIT_FACTOR`** — mảnh cuối | `useMeasureUnits()` nạp bảng hệ số THẬT vào biến module mà `toBaseQuantity()` đọc | — |
| 9 | **A1-FE `batchId`** | `Step1FreshRow`/`Step1DryRow` thêm `batchId`, `step1RowsOfIssue()` điền từ mã lô đã xuất, **bỏ nốt `as unknown as`** | `"batchId": "SB-THIT-HEO"` / `"SB-GAO-TE"` |

#### 🔴 Bếp không thấy học sinh nào — lớp lỗi `schoolId` lần thứ 5

Rà bằng cách gọi **20 endpoint đọc** với token Đơn vị suất ăn thay vì đọc code, ra đúng một cái
sai: `GET /students` → **`total = 0`** trong khi trường có 3 học sinh.

Javadoc của `StudentServiceImpl.listDietSummaries` ghi thẳng giả định sai:
*"SERVICE_CATERERS gắn 1 trường cố định như Teacher/Medical Staff"* — nên dùng
`actor.schoolId()`. Tài khoản đăng ký qua hợp tác không có `schoolId`.

**Vì sao nặng:** đây là đầu vào của **cảnh báo dị ứng khi phục vụ (BR-063/BR-123)**. Màn "Phục
vụ bữa ăn" của bếp ghi "Lớp 1A chưa có hồ sơ học sinh nào" — đợt trước em sửa CÂU CHỮ của
thông báo đó mà không biết nguyên nhân thật nằm ở đây. Nay đường ĐỌC đi qua
`ActorScope.schoolIdOf()`; đường GHI giữ `actor.schoolId()` (School Admin luôn có).

#### Về `A1-FE`: `tsc` mới là thứ giữ hai bên khớp

Bỏ được `as unknown as SubmitStepDto[...]` ở step1 — và **tsc pass ngay**, tức shape FE đã khớp
DTO. Chính cặp ép kiểu đó đã che `batchId` suốt, làm `step1_*_rows.batch_id` NULL và **BR-092/093
chưa từng chạy**. Cùng mô-típ với việc ép kiểu che `dishId` ở step2/step3 hồi 07/08.

#### ⏸ CÒN LẠI: gộp tầng bảng `SimpleTable` + phân trang thật

**Chưa làm, cố ý.** Hai việc này đụng **47 màn** và **mọi màn danh sách**; nhét vào cuối một
phiên đã sửa 9 nhóm lỗi thì không còn kiểm chứng được từng bước. Đề nghị tách phiên riêng, làm
theo đợt: dựng `SimpleTable` → chuyển 2 màn `<table>` thô → chuyển từng nhóm dashboard → cuối
cùng mới đổi phân trang.

> Ghi chú: `QueryCache.onError` vừa thêm ở mục 1 **làm việc chuyển đổi đó an toàn hơn nhiều** —
> màn nào gọi sai resource/param sau khi đổi sẽ kêu ngay thay vì hiện bảng rỗng.

---

### ✅ Đợt 8 — tầng bảng dùng chung `SimpleTable` (08/08/2026)

#### Gốc của "bảng cuộn ngang" KHÔNG phải thiếu component

Đo ra rồi mới thấy: `ui/table.tsx` áp **`min-w-[46rem]` cho MỌI bảng** (=736px). Hợp lý với
bảng nhiều cột ở vùng nội dung rộng, nhưng bảng **2–4 cột nhét trong card nửa màn hình** thì
736px là quá rộng ⇒ cuộn ngang và cắt chữ ("08/08/2…") dù card còn trống mênh mông.

`Table` nay nhận `minWidthClass` (mặc định giữ nguyên `min-w-[46rem]` để không đổi hành vi
chỗ khác); 4 bảng dashboard của BGH/NCC truyền `min-w-0`.

#### `components/app/simple-table.tsx` — tầng còn thiếu

| | `DataTable` (46 file) | **`SimpleTable`** (mới) | primitives `ui/table` (47 file) |
|---|---|---|---|
| Nguồn dữ liệu | tự gọi API theo `resource` | **nhận sẵn `rows`** | tự lo |
| Phân trang / tìm kiếm | có | không (dữ liệu đã có sẵn) | tự lo |
| Skeleton · rỗng · **lỗi** | có | **có** | tự viết, và hầu hết KHÔNG có nhánh lỗi |
| Bề ngang tối thiểu | cố định | **suy theo số cột** | cố định 46rem |

Nhóm 47 file **không sai** khi tránh `DataTable`: nó khoá theo `resource` và **tự fetch**, nên
không dùng được cho bảng nhỏ mà dữ liệu đã nằm sẵn trong trang. Cái thiếu là tầng ở giữa.

**Đã chuyển:** `school-admin/grades` và `system-admin/ingredients` (hai màn `<table>` thô cuối
cùng) + 4 bảng dashboard bỏ ép rộng.

**Đo lại trên trình duyệt thật — 17 màn, `scrollWidth > clientWidth`: 0 bảng tràn.**
(Trước đó dashboard BGH và NCC đều tràn.)

#### Còn lại của việc này

45 file vẫn dùng primitives trực tiếp. Chúng **không tràn** (đã đo) và phần lớn nằm ở vùng nội
dung rộng nên `min-w-[46rem]` đúng. Giá trị của việc chuyển tiếp là **thống nhất trạng thái
rỗng/lỗi**, không phải bố cục — nên là việc dọn dần, không gấp. Chuyển màn nào thì đổi
`<Table>` → `<SimpleTable>` và truyền thêm `isError`/`error` từ hook.

> `QueryCache.onError` (đợt 7) đã phủ phần nguy hiểm nhất: query hỏng nay **luôn** toast, kể cả
> ở 45 màn chưa chuyển.

#### ⏸ Phân trang thật — vẫn chưa làm

`students?pageSize=300`, `allergy-declarations?pageSize=500`, `dishes?pageSize=200` còn nguyên.
Đây là việc riêng, đụng mọi màn danh sách và cả cách FE lọc/gộp phía client — không gộp vào
đợt bảng.

---

### ✅ Đợt 9 — test cho các cổng vừa thêm (08/08/2026)

**352 → 371 test, tất cả pass.** Trước đợt này **0/7** thay đổi hành vi backend trong ngày có
test phủ — và cả 5 lần vấp lớp lỗi `schoolId` đều do người dùng phát hiện khi thao tác thật,
không lần nào test bắt được.

| File | Phủ | Số test |
|---|---|---|
| `common/scope/ActorScopeTest` | "trường nào áp cho tài khoản này" — 5 nhánh | 5 |
| `mf06/service/PurchaseRequestCatalogGateTest` | cổng danh mục hợp đồng ING-077 | 3 |
| `mf06/service/DeliveryDriverRequiredTest` | `driverId` bắt buộc | 4 |
| `mf06/service/ReceivingBatchOnceTest` | một lô chỉ nhận một lần + đặt `DELIVERED` | 2 |
| `mf02/service/AcademicYearServiceTest` (+) | năm học đã kết thúc / chưa bắt đầu | +2 |
| `mf03/mapper/HealthStatusCalculatorTest` (+) | `NO_DECLARATION` ≠ `VERIFIED` | +3 |

#### Test khoá lại QUYẾT ĐỊNH, không chỉ khoá code

Ba test cố ý ghi lại những chỗ **dễ bị nới cho "chạy được"**:

- `supplierIsNeverScopedToASingleSchool` — NCC phục vụ nhiều trường, **tuyệt đối không chọn
  bừa** một trường. Mở ra cho tiện là cho NCC đọc dữ liệu trường không liên quan.
- `activateNotYetStartedYearIsAllowed` — năm học chưa bắt đầu **vẫn kích hoạt được** (trường
  chuẩn bị lớp trước khai giảng). Khoá lại để người sau không siết nhầm thành "phải đang diễn ra".
- `receivingSameBatchTwiceIsRejected` — kèm cảnh báo trong Javadoc: muốn giao nhiều đợt thì
  **thêm cột số lượng vào `receiving_lines` TRƯỚC**, nới luật mà không thêm cột là quay lại lỗi
  cộng khống tồn kho.

#### Gỡ một bom hẹn giờ trong bộ test

`Mf02Fixtures.academicYear()` ghi cứng `2026-09-05 → 2027-05-31`. Sau **31/05/2027** mọi test
dùng fixture này sẽ vỡ vì cổng "năm đã kết thúc" — và người sửa sẽ tưởng cổng mới bị lỗi. Đã
neo theo `LocalDate.now()` nên fixture luôn là năm học ĐANG DIỄN RA, đúng ý nghĩa mặc định.

#### Còn thiếu test

`utensilsPass` (Mẫu 3) và `INCIDENT_EVIDENCE` (upload ảnh bằng chứng) chưa có test riêng —
nhưng cả hai **đã được seed chạy thật kiểm chứng** end-to-end (seed đứt đúng ở `utensilsPass`
khi thiếu field, và upload trả HTTP 200). Ưu tiên thấp hơn 5 cổng ở trên.

---

## PHIÊN SAU LÀM GÌ — theo thứ tự, đã có đủ `file:line` trong backlog

| # | Việc | Ở đâu | Trạng thái |
|---|---|---|---|
| 1 | **Tầng bảng dùng chung** — bóc `SimpleTable` khỏi `DataTable` (nhận sẵn `rows`), chuyển dần 47 màn primitives | `08_…Backlog.md` mục **C** (bullet "47 màn tự dựng `<Table>`") | ⏸ **chờ Nam duyệt** |
| 1.7 | **Nhận hàng NHIỀU ĐỢT cho cùng một lô** — cần thêm cột số lượng vào `receiving_lines` trước, xem cảnh báo ở mục A8 | mục **A8** | ⬜ |
| 2 | **Phân trang thật ở FE** — bỏ `pageSize=300/500`, tìm kiếm phía server | `08_…Backlog.md` mục **D2-BIS** | ⬜ |
| 3 | **Nhập kho: chặn nhận vượt lượng lô NCC** (đo được 50 kg → kho 100 kg) | `08_…Backlog.md` mục **A8** | ⬜ cần chốt: chặn hay cảnh báo |
| 5 | **B5**, **B6** — tồn từ phiên 3 (A1-FE đã xong đợt 7) | `08_…Backlog.md` mục B5 / B6 | ⬜ |

> **Muốn rà UI lại như phiên 4:** cần Playwright MCP (`claude mcp add playwright -- npx
> @playwright/mcp@latest`) và **khởi động lại phiên** thì MCP mới nạp. Dựng dữ liệu bằng
> `07_implement/seed/seed_demo.py` (chỉ API, không SQL) — xem `09_Dev_Runbook.md` mục 11-BIS.
> Tài khoản 6 vai nằm trong `seed/.env.seed` (gitignored).
