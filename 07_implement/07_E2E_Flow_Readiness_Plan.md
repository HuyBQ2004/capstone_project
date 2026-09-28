# 07 — Kế hoạch thông hai luồng nghiệp vụ chính trước demo

> **Đây là KẾ HOẠCH (roadmap), không phải nhật ký tiến độ.**
> Tiến độ thực tế: `Handoff-E2E-Flow-Readiness.md`.
> Lập 07/08/2026, Nam duyệt. Nhánh `namnvhe180863`.

## Context

Nam cần demo cho khách một sản phẩm chạy liền mạch. Hai luồng bắt buộc phải thông:

- **Luồng bếp**: tạo món → mẫu thực đơn → lịch thực đơn (ngày × bữa × khối) → mua hàng → nhập kho → xuất kho → nấu (kiểm thực 3 bước + lưu mẫu) → phân phát (dị ứng + suất đặc biệt).
- **Luồng sự cố**: báo cáo → y tế điều tra (tra suất ăn của trẻ) → khoanh vùng nghi vấn + giữ mẫu (bếp không được hủy) → CAPA → trường duyệt → bên thực hiện nộp bằng chứng → đóng.

Đợt rà soát này (3 agent quét + tôi tự kiểm chứng từng claim P0 bằng cách đọc migration/controller/JSX) tìm ra **cả hai luồng đều đang đứt ở mắt xích cuối**, dù xương sống đã dựng ~90%. Bốn chỗ chặn cứng: **không tạo được thực đơn**, **không lưu được mẫu thực phẩm**, **không giữ được mẫu khi điều tra**, **không đóng được sự cố**.

Kèm theo: các việc đã chốt ở đầu phiên (chuẩn dinh dưỡng theo trường × khối, quyền `/diet-groups`, dọn hardcode) và hai việc nền tảng đã bàn (độ mịn danh mục nguyên liệu "sữa dâu vs sữa nho", tách seed khỏi migration production).

> **Việc dở trong phiên**: đã tạo `V54__mf02_nutrition_targets.sql` và `mf02/domain/NutritionTarget.java` (chưa commit, chưa build). Giai đoạn 0 làm nốt.

> **Quy mô**: đây là nhiều phiên làm việc, không phải một. Mỗi giai đoạn là một commit độc lập, dừng ở đâu cũng có bản chạy được. **Lằn ranh demo tối thiểu = hết Giai đoạn 2.**

---

## Nguyên tắc xuyên suốt

1. **Không giả lập.** Chỗ nào backend chưa có thì ghi rõ "chưa hỗ trợ", không bịa dữ liệu (theo tiền lệ `medical-staff/conduct-inspection/[id]/record`).
2. **Xác minh bằng API thật**, không suy từ code — dùng lại script ở `07_implement/audit-scripts/`.
3. **Mỗi giai đoạn ghi vào doc** trước khi sang giai đoạn sau (Nam yêu cầu: thoải mái thêm tính năng miễn là ghi chú lại).
4. Không trộn refactor lớn vào commit sửa lỗi — vỡ demo thì phải `git bisect` được.

---

## Giai đoạn 0 — Làm nốt chuẩn dinh dưỡng theo trường × khối

Hiện `NUTRITION_TARGET` là hằng số toàn hệ thống, khai trùng ở `MealHistoryServiceImpl.java:85` (`private static final`) và `Frontend/src/lib/mock-data.ts:1892`. Thực đơn gắn `grade_id` nhưng mọi khối bị chấm cùng một thước ±10%.

- **BE**: hoàn thiện `NutritionTargetRepository` / `NutritionTargetService(+Impl)` / `NutritionTargetDtos` / `NutritionTargetController` theo đúng khuôn `mf02` `Grade*` (đọc `GradeServiceImpl.java` — school-scope qua `actor.schoolId()`, ngoài phạm vi ⇒ 404 không 403).
  - `GET /api/v1/nutrition-targets` → mọi khối của trường, khối chưa khai trả giá trị mặc định kèm cờ `configured:false`.
  - `PATCH /api/v1/nutrition-targets/{gradeId}` → `hasRole('SCHOOL_ADMIN')` (PATCH, không PUT — quy ước dự án).
  - `DEFAULT_TARGET = 715/28/22/95` đặt **một chỗ duy nhất** trong service; xoá hằng ở `MealHistoryServiceImpl`.
- **BE**: `MealHistoryServiceImpl.nutritionHistory()` tra chuẩn theo khối của học sinh thay vì hằng số.
- **FE**: `lib/nutrition-target.ts` (khuôn `lib/diet-groups.ts`) + mục cấu hình trong `school-admin/config/page.tsx` (cùng chỗ `cost_ceiling`); `school-admin/menu-approval/[id]/page.tsx` và `components/app/special-meal-dialog.tsx` đọc chuẩn theo `menu.grade`; xoá hằng `NUTRITION_TARGET` khỏi `mock-data.ts`.

**Kèm 3 việc nhỏ đã chốt:**
- `mf05/web/DietGroupController.java:32` — thêm `PARENT`,`TEACHER` vào `@PreAuthorize` (5 màn đang 403 im lặng, cột "ghi chú chế độ ăn" của giáo viên luôn hiện `—`).
- Dọn hardcode: `MEALS_7D` (`service-caterers/dashboard/page.tsx:51-59`, biểu đồ 7 ngày bịa) + 7 giá trị cứng (`school-admin/dashboard:143` "96%", `:134` "2 nhà cung cấp"; `medical-staff/dashboard:85` "3", `:61` "FB-8809"; `service-caterers/dashboard:354,359,361`; `service-caterers/receiving:226` "100%"). Nối được thì nối, không nối được thì **bỏ ô**, không để số giả.
- `lib/violation-utils.ts:145` — bỏ `CURRENT_SCHOOL`, lấy tên trường từ phiên đăng nhập (biên bản vi phạm là văn bản pháp lý).

---

## Giai đoạn 1 — Thông luồng bếp (P0)

### 1.1 Khôi phục "tạo được thực đơn" — rẻ hơn tưởng rất nhiều

Đã xác minh: `menus/page.tsx:544-557` (lịch tuần) **đã gửi đúng `mealType` và đúng 1 block**, chỉ thiếu `gradeId` — dòng `:550` gửi `grade` (nhãn "Khối 3") trong khi `MenuDtos.java:101` đòi `@NotBlank String gradeId` (UUID, thêm ở V44). **Sửa một trường là tạo được thực đơn trở lại.**

- Thêm `lib/grades.ts` theo khuôn 1:1 của `lib/diet-groups.ts`: `useGrades()` + `useGradeId(label)`. `GET /grades` đã mở cho SERVICE_CATERERS.
- **Giữ nhãn làm khoá state/URL, chỉ đổi sang UUID tại ranh giới gửi request** — nhãn đang là khoá của `?grade=`, `gradeEnrollmentOf()`, `dishState()`, `gradeAllergySummary()`; đổi hết sang UUID là refactor xuyên 6 màn, không làm trước demo.
- Ở 3 màn GHI, đổi nguồn dropdown từ `gradeLevelsOf(classrooms)` sang `useGrades()` để mọi nhãn chọn được đều resolve ra UUID.
- 11 điểm đổi, tập trung ở: `service-caterers/menus/page.tsx` (`:550` ghi, `:169`/`:262` diet-coverage, `:626` tabs), `service-caterers/menu-templates/page.tsx` (`:209` ghi, `:138`/`:477` grade-summary), `components/app/menu-form.tsx` (`:1390`, `:1465`, `:929`).
- **Không đổi** các màn chỉ đọc nhãn từ response (`menu-approval`, `cook-sheet`, `meal-service`, `special-meal-dialog`) — đúng chủ ý D1 không lộ UUID.

Việc này đồng thời sửa luôn: `menu-templates` hết lưu `grade_id = NULL` im lặng, và `diet-coverage`/`grade-summary` (đang **luôn 400**, chưa từng hiển thị) bắt đầu chạy.

### 1.2 `menu-form.tsx` — fan-out theo buổi

`menu-form` là di tích của mô hình cũ "1 thực đơn = 1 ngày": gửi `blocksData` nhiều buổi, mà `MenuServiceImpl.java:581` chỉ đọc `blocks.get(0)` ⇒ buổi 2, 3 bị vứt im lặng. Sửa ở FE (`persist()`, `menu-form.tsx:1362-1507`): lặp trên `fullBlocks`, mỗi buổi một create/update tuần tự, gom lỗi, toast tổng kết `"Đã lưu 2/3 buổi — bữa trưa lỗi: …"`, không điều hướng khi có buổi lỗi. Bắt riêng 409 `MENU-409` (ô đã tồn tại) → chuyển sang PUT.

**Không sửa backend**: trạng thái duyệt là per-buổi ở khắp nơi (`MenuWorkflowService`, `DietCoverageValidator`, event `MenuPublished` mang `mealType` sang MF09) — thêm khái niệm "duyệt theo ngày" là lan sang MF09, quá rủi ro trước demo.

### 1.3 Sửa nút "gỡ thực đơn khỏi bữa" đang nói dối

`MenuMapper.toBlocksData()` (`MenuMapper.java:58-63`) dựng `MenuBlock(mealType, servings, **null**, dishDtos)` — mất `templates[]`; `MenuBlockDish` không có `templateId`. Hệ quả đã xác minh: ô lịch chỉ hiện 1 mục dù thêm nhiều mẫu, chống trùng vô hiệu, và `removeTemplate()` (`menus/page.tsx:564-581`) **không gỡ gì nhưng vẫn toast thành công**.

- Trước demo: ẩn/đổi nút gỡ thành "Xoá cả thực đơn của ô".
- Ngay sau: migration thêm `menu_dishes.template_id UUID NULL` (**không phải `menus.template_id`** — một ô chứa N mẫu, arity 1-1 sai), thêm `templateId` vào `MenuBlockDish`, ghi ở `applyBlocksData`, đọc lại ở `toBlockDish`.

### 1.4 Khôi phục "lưu được mẫu thực phẩm"

Đã xác minh: BE `Step2RowDto`/`Step3RowDto` **có** `dishId`, nhưng FE `interface Step2Row` (`mock-data.ts:412-428`) **không có** field đó, và `step2RowsOfMeal()`/`step3RowsOfMeal()` không set. Ép kiểu `as unknown as SubmitStepDto["step2Rows"]` (`inspections/[id]/page.tsx:1290`) che mất khỏi TypeScript ⇒ `dish_id` luôn NULL ⇒ `dishChoicesOf()` (`samples/page.tsx:128-137`) lọc `r.dishId` ra mảng rỗng ⇒ dialog "Đăng ký lưu mẫu" không có món nào, mà `RegisterSampleRequest.dishId` là `@NotBlank`.

Thêm `dishId` vào `Step2Row`/`Step3Row` + hai hàm dựng dòng, **bỏ ép kiểu `as unknown`** để TS bắt lỗi này về sau.

### 1.5 Khôi phục truy xuất lô ở kiểm thực Bước 1 (BR-092/093)

`step1RowsOfIssue()` (`mock-data.ts:3080-3113`) không set `batchId`; kể cả set thì FE có **mã lô** (`"BATCH-GF-4412"`) còn `InspectionServiceImpl.parseUuid()` (`:496-505`) nuốt `IllegalArgumentException` → NULL. ⇒ `checkBatchExpiry` (BR-092/093, chặn nguyên liệu hết hạn) **chưa từng chạy**.

Sửa cả hai đầu: FE gửi `batchId`; BE resolve mã lô → UUID qua `referenceResolver` thay vì `parseUuid` nuốt lỗi (hoặc thống nhất truyền UUID). **Không để `parseUuid` nuốt lỗi im lặng** — đổi thành ném lỗi rõ.

### 1.6 Bịt cổng BR-094 (fail-open)

`GET /inspections/serving-gate` chặn cả 3 (`NO_SESSION`/`IN_PROGRESS`/`INSPECTION_FAILED`), nhưng chốt chặn thật `StudentDistributionServiceImpl.doServe():152-159` dùng `ReferenceResolver.inspectionGate():845-870` **chỉ chặn khi có step FAILED**; không có phiên ⇒ `Optional.empty()` ⇒ **không chặn**. FE đã phải né theo (`teacher/distribution/[id]/page.tsx:105-121`).

Chốt ngữ nghĩa **một lần** rồi cho hai đường dùng chung một hàm. Kèm sửa `inspectionGate()` không lọc `partner_id` và `results.get(0)` không `ORDER BY` (trường có ≥2 nhà thầu hoặc có phiên tái kiểm ⇒ lấy phiên ngẫu nhiên).

**Nam đã chốt: siết đủ** — `NO_SESSION`, `IN_PROGRESS`, `INSPECTION_FAILED` đều chặn phát suất. Cho hai đường (`serving-gate` và `doServe`) dùng chung **một** hàm để không bao giờ lệch lại. Bỏ đoạn né ở `teacher/distribution/[id]/page.tsx:105-121`, disable nút theo đúng `allowed:false` và hiện `reasonCode` cho giáo viên biết phải làm gì.

> ⚠️ Hệ quả cho kịch bản demo: **quên mở phiên kiểm thực là tắc ở bước phát suất**. Kịch bản demo phải đi đúng thứ tự, và cần một câu thoại giải thích đây là chốt chặn ATTP cố ý — đó chính là điểm bán hàng.

### 1.7 Sửa bug đơn vị ×1000 ở mua hàng

`orders/new/page.tsx:145` — `(Number(ing.quantity) || 0) * servings` không quy đổi g→kg, trong khi màn xuất kho có (`inventory/issue/page.tsx:52` `toBulkUnit()`). `dish_ingredients.unit` mặc định `'g'`, `catalog_ingredients.unit` là `'kg'` ⇒ nhu cầu đặt hàng **sai 1000 lần**. Dùng chung `toBaseQuantity()`.

---

## Giai đoạn 2 — Thông luồng sự cố (P0)

### 2.1 Gỡ bế tắc "không bao giờ đóng được sự cố"

Đã xác minh: `quarantine_disposition` (thêm ở `V20:326`) **chỉ được ĐỌC** ở `InventoryBatchPortImpl.java:94`, **không nơi nào GHI** — không entity field, không endpoint, không màn. Mà `IncidentServiceImpl.java:401` chặn đóng khi `quarantineBatchesUnresolved > 0`. ⇒ **mọi sự cố đã cách ly ≥1 lô là không thể đóng vĩnh viễn**, kể cả khi CAPA xong và học sinh hồi phục hết.

Thêm endpoint xử lý lô cách ly: `POST /incidents/{code}/quarantine-batches/{batchCode}/disposition` (`RETURNED_TO_SUPPLIER` | `DISPOSED`) — `hasRole('MEDICAL_STAFF')`, ghi `quarantine_disposition` + `_at`. Nối vào màn `medical-staff/incidents/[id]` ngay cạnh chỗ tick cách ly.

### 2.2 Sửa "giữ mẫu để điều tra" — hiện ném 500

`FoodSamplePortImpl.java:112-121` chạy `update food_samples set incident_id=…, status='SENT_TO_LAB'` mà **không set** `lab_request_id`/`sent_to_lab_at` ⇒ vi phạm CHECK `ck_smp_sent_has_request` (`V14:114-115`) ⇒ rollback toàn bộ, phiếu LABREQ vừa insert mất theo.

Sửa câu update set đủ 3 cột. **Và** thêm trạng thái/cờ diễn đạt được "đang bị giữ để điều tra" — hiện `FoodSampleStatus` chỉ có `RETAINED`/`SENT_TO_LAB`/`DISPOSED`, không có `HOLD`. Đây là điều kiện để 2.3 chạy.

### 2.3 Chặn bếp hủy mẫu đang bị điều tra

`FoodSampleServiceImpl.java:227` **có** nhánh chặn `sample.getIncidentId() != null && hasOpenIncidentLinked(...)`, nhưng đường duy nhất gán `incident_id` cho mẫu còn `RETAINED` là `requestRetrieval` — vốn đang hỏng (2.2) và **không màn FE nào gọi**. Đường còn lại `POST /samples/{id}/incident-link` chỉ nhận mẫu **đã DISPOSED** — chốt cửa sau khi ngựa đã chạy.

⇒ Sau khi sửa 2.2, thêm màn cho y tế gọi `POST /incidents/{code}/sample-retrieval` (BE đã có, `IncidentController.java:181`), đặt trong `medical-staff/incidents/[id]`. Kèm: `retrievableForIncident` hiện **bỏ qua** `suspectedDishIds` (tự thú trong Javadoc `:45-47`) — lọc theo món nghi ngờ thật.

**Nam đã chốt: gia hạn `retention_deadline` khi giữ mẫu.** BR-111 (lưu 24h) trùng đúng cửa sổ điều tra BR-149 (24h) nên mẫu đủ điều kiện hủy đúng lúc điều tra bắt đầu. Khi y tế yêu cầu giữ mẫu cho một sự cố:

- đẩy `retention_deadline` ra xa (mốc gắn với vòng đời sự cố, không phải một hằng số tuỳ tiện);
- đặt trạng thái `HOLD` (giá trị mới của `FoodSampleStatus`) — diễn đạt được "đang bị giữ để điều tra", điều mà 3 trạng thái hiện tại không nói được;
- bếp không hủy được tới khi sự cố đóng; `ck_smp_dispose_after_deadline` vẫn giữ nguyên ý nghĩa (không mâu thuẫn) vì deadline đã được đẩy ra thật.

Cần một migration cho `HOLD` + nới `CHECK` trạng thái, và ghi rõ mốc gia hạn vào doc.

### 2.4 Cho y tế tra được suất ăn của đúng đứa trẻ

Đây là **lỗ nghiệp vụ lớn nhất** của luồng 2. DB đã tối ưu sẵn (`ix_dist_student_date`, `ix_sdish_dish` với comment ghi thẳng *"em nào đã ăn món X? (MF11)"*), port `studentsWhoAte()` đã viết xong (`ServingDishPortImpl.java:27`) — **nhưng không controller nào gọi, và `GET /students/{code}/meal-history` chỉ mở cho `PARENT`,`TEACHER`** (`MealHistoryController.java:46`) ⇒ y tế 403. Không có màn `medical-staff/students*` nào.

- Mở `meal-history` cho `MEDICAL_STAFF` (đọc trong phạm vi trường).
- Thêm endpoint dùng `studentsWhoAte()` (đang là dead code) cho chiều ngược: món → học sinh đã ăn.
- Thêm màn điều tra trong `medical-staff/incidents/[id]`: chọn học sinh bị ảnh hưởng → suất ăn N ngày qua → giao nhau giữa các trẻ ⇒ ra món nghi ngờ.
- Sửa `suspectedDishCandidates()` (`IncidentServiceImpl.java:163-170`) — hiện trả **toàn bộ món cả trường** trong cửa sổ, không giao với `incident_affected_students`.

### 2.5 Làm bản đồ truy xuất có nghĩa

`GET /incidents/{code}/traceability-map` (BR-151) hiện chỉ echo lô y tế đã tự tay cách ly, với `batchId`/`ingredientName`/`supplierId` **luôn null** (`IncidentServiceImpl.java:273-279`) — không hề có món. Trong khi mắt xích thật đã có ở MF06 `GET /traceability/dish-batches` (đã mở cho MEDICAL_STAFF).

Nối `traceability-map` đi qua `dish → inventory_transactions(ISSUE) → inventory_batches → partners` thay vì đọc danh sách cách ly thủ công.

> Phụ thuộc: BR-076 chỉ chạy nếu bếp **có lập phiếu xuất kho**. `preparation_records` đã bị DROP ở V43 nên đó là đường duy nhất. Kịch bản demo bắt buộc phải đi qua bước xuất kho.

### 2.6 Thêm 2 màn chi tiết CAPA còn thiếu

`school-admin/corrective-actions/` và `service-caterers/corrective-actions/` chỉ có `page.tsx`, **không có `[id]/page.tsx`** ⇒ School Admin duyệt kế hoạch mà không xem được chi tiết; bên thực hiện nộp bằng chứng trong dialog trên danh sách. Backend đã đủ endpoint.

---

## Giai đoạn 3 — Độ mịn danh mục nguyên liệu

Vấn đề "món sữa dâu nhưng khai nguyên liệu là `ING-SUA`, NCC bán cả sữa nho": cả `supplier_products`, `inventory_batches`, `FefoAllocator` đều chỉ phân biệt bằng `catalog_ingredients`. FEFO là lời khẳng định *"mọi lô cùng `ingredient_id` thay thế được cho nhau"* — hai vị khác nhau chung một mã ⇒ FEFO **không còn đúng**, và không tham số nào cứu được.

- **Quy tắc ghi vào `00_Common_Conventions.md`**: hai thứ dùng chung một mã danh mục khi và chỉ khi bếp lấy cái nào cũng được mà **món không đổi, hồ sơ dị ứng không đổi, chế độ ăn không đổi**. Khác vị/khác dị nguyên ⇒ hai mã. Khác NCC/giá/quy cách ⇒ cùng mã, phân biệt ở `supplier_products`.
- Tách seed danh mục theo quy tắc đó (đợt này chỉ sửa dữ liệu, **không đụng schema**).
- **Chặn `ingredient_code` NULL khi duyệt thực đơn** (MF05): `V8:105` cho phép NULL = "ngoài danh mục"; món khai chữ tự do rơi khỏi cả chuỗi mua hàng/xuất kho mà không cảnh báo. Trả 422 kèm tên dòng thiếu. Kèm validate `quantity > 0` ở BE (hiện chỉ FE chặn, `V53` đã `DROP NOT NULL`).

**Không** làm bảng biến thể: thêm trục định danh thứ hai buộc cả 4 nơi (đặt hàng, nhận hàng, FEFO, giải dị nguyên) phải tôn trọng, và `FefoAllocator` sẽ cần một vị từ "tương thích" mà không ai định nghĩa dứt khoát được.

---

## Giai đoạn 4 — Tách seed khỏi migration production

Cơ chế **đã có sẵn**: `src/test/resources/application-test.yml:12` dùng `locations: classpath:db/migration,classpath:db/testseed`; production `application.yml:23` chỉ `db/migration`. Chỉ cần thêm một nhánh nữa cho demo/dev.

Phân loại ~520 `INSERT` đang nằm trong 21 migration:

| Loại | Ví dụ | Xử lý |
|---|---|---|
| Cấu hình hệ thống | roles/permissions (V2), đơn vị đo (V51), checklist mẫu (V13), report views (V30), integrations (V24) | **Giữ** trong `db/migration`, viết idempotent |
| Danh mục nghiệp vụ | `catalog_ingredients`, `dishes`, `diet_groups` (V9, V11) | Ra khỏi migration → màn quản trị + import (sau demo) |
| Dữ liệu demo | **V39 (399 INSERT)**, V5, V17, V21, V47 — học sinh, sự cố, phiên kiểm thực | Chuyển sang `db/demo`, chỉ nạp ở profile dev/demo |

> ⚠️ **Thời điểm**: chừng nào V39 còn trong lịch sử migration đã deploy, xoá về sau sẽ vỡ checksum mọi DB hiện có (phải `flyway repair`/baseline lại). Rẻ nhất là làm **trước lần deploy thật đầu tiên**. Đổi lại, giai đoạn này đụng cấu hình chạy app ⇒ phải báo cả nhóm trước khi merge, và làm **sau** khi hai luồng đã xanh.

---

## Giai đoạn 5 — Dọn UI

Tin tốt từ audit: **Card lồng nhau gần như không có** (đo thật: 1 chỗ depth 2, phần còn lại là false positive của skeleton `<Card />` self-closing), **0 chỗ Dialog lồng Dialog**, và 47 màn dùng `DataTable` đã có empty state chuẩn. Vấn đề thật nằm chỗ khác:

**Rẻ, làm ngay:**
1. **Nút "Đăng xuất" ở sidebar không logout** — `sidebar.tsx:108-114` là `<Link href="/login">` thuần; logout thật nằm ở `user-menu.tsx:227`. Khách chắc chắn bấm thử.
2. **7 file chết**: `service-caterers/special-meals/`, `medical-staff/report-center/{,compliance,history}/`, `medical-staff/compliance/history/`, `components/app/meal-block-dialog.tsx` (0 import), `service-caterers/menus/create/` (0 link).
3. **Dashboard Y tế bảng trắng khi rỗng** — màn mở đầu của một vai.
4. **Màn tạo/sửa thực đơn không có `PageHeader`** — luồng demo chính, vào không biết đang ở đâu, không có nút Back.
5. **Lưu ngày đã format vào payload** — `menus/page.tsx:601`, `menus/[id]/page.tsx:162` `toLocaleDateString("vi-VN")`, đúng lỗi mà `lib/format.ts:9-13` sinh ra để ngăn.
6. **Footer dialog không sticky** — 18 dialog cuộn, nút Lưu trôi khỏi màn hình. Áp pattern `flex flex-col` đã có ở `system-admin/roles/page.tsx:127` vào `components/ui/dialog.tsx`.
7. **Sidebar Đơn vị Suất ăn 20 mục / 7 nhóm** — chính comment `sidebar.tsx:70-71` thừa nhận phải nới khoảng cách để không tràn 1080p. Gom nhóm hoặc thêm collapsible.

**Cơ học, làm theo kịch bản demo trước:**
8. **40 màn tự chế Badge trạng thái** thay vì `StatusBadge` — cùng trạng thái hiện khác màu giữa các màn.
9. **49/146 màn không dùng `actions` của `PageHeader`** — nút chính mỗi màn một chỗ.

**Hoãn (chi phí cao):**
10. Hai màn khổng lồ không có stepper: `service-caterers/inspections/[id]` (1641 dòng, JSX bắt đầu ở 1348) và `medical-staff/incidents/[id]` (1571 dòng, 14 handler quy trình, 0 tab). Trước demo chỉ thêm **progress indicator + tabs** cho màn sự cố; refactor sau.
11. `components/app/menu-form.tsx` 2813 dòng — tách sau khi hợp đồng đã xanh, theo ranh giới tự nhiên: `137-263` (type/hằng, đang bị 4 file import chỉ để lấy type từ một component `"use client"`), `266-356` (transform thuần), `624-836` (`IngredientEditor` + `AllergenPicker`).

---

## Verification

**Sau mỗi giai đoạn** — `mvn -q test` (38 test BE), `npx tsc --noEmit`, `npm run build`, `next lint` (baseline: **8 warning**, không được thêm).

**Baseline trước khi sửa** (Giai đoạn 1): `curl` POST `/api/v1/menus` bằng token SERVICE_CATERERS với body y hệt `menus/page.tsx:544-556` → xác nhận 400 và field bị chặn; `curl GET /api/v1/grades` → xác nhận `name` khớp đúng chuỗi `ClassroomResponse.grade` trả về. **Nếu hai chuỗi lệch nhau thì đổi dropdown sang `useGrades()` là bắt buộc, không còn tuỳ chọn.**

**Kịch bản E2E luồng bếp** (bấm tay trên UI, backend thật, 7 tài khoản `.synthetic@example.test` / `P@ssw0rd123`):
tạo món có nguyên liệu → mẫu thực đơn gắn khối → lịch tuần: chọn ngày × bữa × khối, áp mẫu → nộp duyệt → BGH duyệt + công bố → kiểm `student_distributions` tự sinh → tạo đơn mua hàng từ thực đơn (kiểm **đơn vị không sai ×1000**) → NCC báo giá → duyệt giá → NCC xác nhận lịch + gán lô → nhận hàng → xuất kho FEFO → mở phiên kiểm thực, nhập Mẫu 1/2/3 (kiểm **Bước 1 có mã lô**) → **đăng ký lưu mẫu** (kiểm danh sách món **không rỗng**) → giáo viên phát suất (kiểm cảnh báo dị ứng + suất đặc biệt + cổng BR-094).

**Kịch bản E2E luồng sự cố**:
phụ huynh báo → y tế tiếp nhận → tạo hồ sơ → **tra suất ăn của trẻ N ngày qua** → giao nhau ra món nghi ngờ → **giữ mẫu (kiểm bếp KHÔNG hủy được)** → cách ly lô → xem bản đồ truy xuất (kiểm **có món + có NCC**, không phải toàn null) → tạo CAPA → trường duyệt → bên thực hiện nộp bằng chứng → y tế nghiệm thu → **xử lý lô cách ly** → đóng sự cố (kiểm **đóng được**).

**Kiểm hồi quy hợp đồng**: thêm case vào `MenuWriteContractIT` — gửi nhãn `"Khối 3"` vào `gradeId` → 400; 3 POST cùng (ngày, khối) khác `mealType` → 3× 201, POST thứ 4 trùng slot → 409.

**Ghi doc**: cập nhật `07_implement/Handoff-System-Audit-Gaps.md` (mục 3/5 đã xong ở phiên trước) và tạo `07_implement/Handoff-E2E-Flow-Readiness.md` ghi từng giai đoạn: đã sửa gì, còn gì, và **danh sách "đã kiểm, không phải gap"** để phiên sau khỏi điều tra lại.
