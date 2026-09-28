# 08 — Sổ phát hiện CHƯA SỬA (backlog kỹ thuật hai luồng chính)

> **Mục đích:** giữ lại toàn bộ kết quả đợt rà soát 07/08/2026 để phiên sau **không phải rà
> lại từ đầu**. Mỗi mục có `file:line` và bằng chứng cụ thể.
>
> · Roadmap: `07_E2E_Flow_Readiness_Plan.md`
> · Đã làm tới đâu: `Handoff-E2E-Flow-Readiness.md`
> · Đợt rà trước (phân quyền/UX): `Handoff-System-Audit-Gaps.md`

## Cách đợt rà này được thực hiện (lặp lại được)

3 agent quét song song (luồng bếp · luồng sự cố · UI/UX), **rồi tự kiểm chứng từng claim P0
bằng cách đọc migration/controller/JSX và gọi API thật** — không lấy nguyên báo cáo agent làm
căn cứ. Việc kiểm chứng đó là cần thiết: agent đầu tiên kết luận sai rằng FE↔BE lệch mô hình
thực đơn; đọc lại thì lệch nằm giữa **hai nửa của chính FE**, và chỉ cần sửa một trường.

Script đo lại có ở `07_implement/audit-scripts/` (xem bảng trong `Handoff-System-Audit-Gaps.md`).

---

## A. LUỒNG BẾP — còn lại

### A1 · `step1_*_rows.batch_id` LUÔN NULL ⇒ BR-092/093 chưa từng chạy — ✅ ĐÃ SỬA BE 08/08/2026

> **Sửa 08/08/2026 (phía backend).** `InspectionServiceImpl.parseUuid()` — hàm NUỐT
> `IllegalArgumentException` rồi trả `null` — **đã xoá hẳn**, thay bằng:
> - `resolveBatchId(raw, schoolId)` → `ReferenceResolver.batchIdByCode()` (mới, tra
>   `inventory_batches.code`, vẫn nhận UUID thô). Mã sai ⇒ **422 nêu đích danh**, không im lặng.
> - `checkBatchExpiry(batchId, rawLabel)` nhận UUID đã resolve ⇒ BR-092/093 **chạy thật**.
>
> **Phát hiện kèm theo, quan trọng hơn A1:** cùng hàm `parseUuid` đó còn nuốt cả
> `dishId` của **Mẫu số 2 và Mẫu số 3** (`row.setDishId(parseUuid(dto.dishId()))`). FE gửi
> **MÃ MÓN** (`MenuMapper.toBlockDish` trả `dish.getCode()`), nên `step2_rows.dish_id` /
> `step3_rows.dish_id` vẫn NULL **kể cả sau khi Giai đoạn 1.4 sửa xong phía FE** —
> `InspectionAssembler` map ngược bằng `dishCode(r.getDishId())` ⇒ `null` ⇒ `dishChoicesOf()`
> vẫn lọc ra **mảng rỗng** ⇒ **màn "Đăng ký lưu mẫu" vẫn không có món nào**.
> Tức là **1.4 chưa thật sự thông**; nay `resolveDishId()` tra `dishIdByCode()` và ném lỗi rõ.
>
> ⬜ **Còn lại:** FE `step1RowsOfIssue()` vẫn **không set `batchId`**. Backend nay đã sẵn sàng
> nhận mã lô, nhưng chừng nào FE chưa gửi thì cột vẫn NULL (hợp lệ — không phải dòng nào cũng
> truy được về lô). Xem mục A1-FE bên dưới.

### A1-FE · `step1RowsOfIssue()` chưa gửi `batchId` ⬜

`lib/mock-data.ts` `step1RowsOfIssue()` dựng dòng Mẫu số 1 từ `inventory_transactions` type
ISSUE nhưng **không set `batchId`**, dù `Step1FreshRowDto` backend có field đó.

Kể cả nếu FE có gửi thì vẫn hỏng vì **sai kiểu**: `InventoryTransactionService.batchCodeOf()`
trả **mã lô** (`"BATCH-GF-4412"`), còn `InspectionServiceImpl.parseUuid()` bắt
`IllegalArgumentException` rồi `return null` — lỗi im lặng.

⇒ `checkBatchExpiry()` (BR-092/093 — chặn nguyên liệu hết hạn vào bếp) **chưa bao giờ chạy**:
nhận hàng hết hạn vẫn ghi "Đạt".

**Sửa cả hai đầu**, và **đừng để `parseUuid` nuốt lỗi** — đổi thành resolve mã→UUID qua
`referenceResolver`, sai thì ném lỗi rõ. **Nửa backend đã xong**, nửa FE còn nợ.

### A2 · `menu-form` gửi nhiều buổi, backend chỉ đọc buổi đầu — ✅ ĐÃ SỬA FE 08/08/2026

`MenuServiceImpl.applyBlocksData()` có `MenuBlock block = blocks.get(0);` — FE gửi
`blocksData` cả Sáng/Trưa/Xế thì **buổi 2, 3 bị vứt im lặng** (đường PUT không validate).

Đã sửa ở **FE** (`menu-form.tsx` `persist()`), **không sửa backend**: trạng thái duyệt là
per-buổi ở khắp nơi (`MenuWorkflowService`, `DietCoverageValidator`, event `MenuPublished`
mang `mealType` sang MF09) — thêm khái niệm "duyệt theo ngày" là lan sang MF09.

**Cách sửa:** `persist()` lặp trên `fullBlocks`, mỗi buổi MỘT request **tuần tự**
(`mutateAsync`), map buổi ↔ thực đơn đã có của cùng `(menuDateIso, grade, mealType)` để chọn
create hay update, gom lỗi rồi báo `"Đã lưu 2/3 buổi…"` và **không điều hướng** khi còn lỗi.
Tuần tự chứ không `Promise.all`: hai buổi cùng (ngày, khối) chạm cùng ràng buộc slot ở server.

**Phát hiện kèm theo:** màn này **chưa từng gửi `gradeId` lẫn `mealType`** — cùng lỗi đã sửa ở
lịch tuần hồi 1.1 nhưng chưa ai đi qua đường tạo mới ở đây nên không lộ. Đã thêm
`useGradeId(grade)` + chặn trước khi gửi (thiếu `gradeId` ⇒ báo rõ, không để server trả
"Validation failed").

> ⬜ **Còn nợ:** phần **thực đơn RIÊNG** (suất theo nhóm chế độ ăn) trong cùng `onSuccess` vẫn
> gửi `blocksData` nhiều buổi trong MỘT request ⇒ vẫn dính đúng `blocks.get(0)`. Chưa fan-out
> vì mã thực đơn riêng phải theo khuôn `{menuCode}--{dietGroupCode}` (C10) mà `menuCode` nay
> là per-buổi — sửa đúng là đổi cả quy ước đặt mã. Đã ghi chú tại chỗ trong code.

> Bối cảnh: `menu-form` chỉ còn đường vào qua `/menus/[id]/edit`. `/menus/create` là route
> **mồ côi** (0 link) và `components/app/meal-block-dialog.tsx` là **code chết** (0 import).

### A3 · KHÔNG có engine nổ BOM ở backend 🟠

`GET /inventory/issue-plan` nhận `demand[ingredientCode]=qty` **từ query param của client**;
Javadoc `InventoryService` nói thẳng *"đã tính sẵn ở tầng gọi… MF06 chỉ chịu trách nhiệm phần
PHÂN BỔ FEFO"*. `IssuePlanLine.dishId`/`dishName`/`perServing` **luôn null**.

⇒ Toàn bộ việc nổ BOM nằm ở FRONTEND, và **hai màn tính hai kiểu**: `orders/new` dùng
`menu.servings` (kế hoạch tĩnh), `inventory/issue` dùng `planned-dishes` (số suất thật).

`service-caterers/cook-sheet` là màn **thuần client** (`cookSheet()` trong `mock-data.ts`),
không có backend.

### A4 · `purchase_requests.source_menu_id` / `source_meal_type` / `service_date` là CỘT CHẾT 🟠

Có trong V10 (BR-077), entity có setter, **không nơi nào gọi setter**, DTO create không có
field ⇒ đơn mua hàng **không truy được về thực đơn nào sinh ra nó**.

Prefill từ thực đơn chỉ chạy khi vào bằng deep-link `?date=&meal=`; mở màn "Tạo đơn" trực
tiếp thì Service Caterers **gõ tay từng dòng**.

### A5 · Truy xuất "món X dùng lô nào" là XẤP XỈ 🟠

- `inventory_transactions.menu_id` chỉ giữ **menu ĐẦU TIÊN** của bữa
  (`InventoryService`: `slip.getMenuIds().get(0)`) ⇒ một bữa N thực đơn khối thì N−1 mất khoá.
- `dishId` mỗi dòng xuất là **"món đầu tiên dùng nguyên liệu đó"** (FE `firstDishId`).
  `ck_tx_issue_trace` thoả về hình thức nhưng truy xuất **sai** khi nhiều món chung nguyên liệu.

### A6 · `inspection_sessions.menu_id` không bao giờ được ghi 🟠

`OpenSessionRequest` = `{date, mealType, dishId}`, `open()` không `setMenuId()`; chỉ
`reinspect()` chép lại. Phiên kiểm thực nối thực đơn bằng **khớp chuỗi `(date, meal)` ở
client**. Tương tự, **không có khoá nào giữa `issue_slips` và `inspection_sessions`**.

### A7 · Endpoint backend CÓ nhưng FE chưa dùng 🟡

| Endpoint | Ghi chú |
|---|---|
| `POST /menus/{code}/apply-template` | FE tự áp mẫu ở client. ⚠️ `applyTemplate()` **xoá sạch** `menuDishes` rồi đổ 1 mẫu — chuyển sang dùng là **regress** UX cộng dồn hiện có |
| `GET /menus/{code}/allergy-conflicts` | FE tự tính client-side |
| `GET /inventory/issue-slips` (list) | Không màn nào xem lại phiếu xuất đã lập |
| `GET /receiving-sessions/due-deliveries` | FE tự lọc `deliveries?status=DELIVERED` — **logic khác BE** (`DISPATCHED|IN_TRANSIT|DELAYED`) |
| `GET /allergy-alerts` (cảnh báo cấp LỚP) | `lib/api/contract-check.ts` tự thú: "chưa trang nào gọi" |
| `GET /student-distributions/summary` (UC-132) | 0 hit |

### A8 · Ràng buộc lỏng ở nhập kho — ✅ HAI Ý CHÍNH ĐÃ SỬA 08/08/2026

> **✅ Đã sửa:** (1) **một lô NCC chỉ nhận được MỘT lần** — nhận lại cùng `supplierBatchId`
> nay trả 409 kèm mã lô; (2) **`SupplierBatchStatus.DELIVERED` nay được gán thật** khi lô vào
> kho, kèm migration **`V57`** backfill cho dữ liệu cũ *theo bằng chứng* (chỉ đóng lô có dòng
> phiếu nhận không bị từ chối — lô bị từ chối hoặc chưa ai nhận vẫn `ACTIVE`). Đo lại: 4/4 lô
> của demo chuyển `DELIVERED`. (3) **`driverId` nay BẮT BUỘC** khi tạo chuyến giao — đo lại:
> thiếu ⇒ 422 kèm hướng dẫn khai nhân viên ở màn nào.
>
> ⚠️ **Giới hạn có chủ đích:** mô hình hiện tại coi "nhận lô" = **nhận trọn lô**, vì
> `receiving_lines` **KHÔNG có cột số lượng** — mọi dòng đều cộng nguyên
> `supplierBatch.getQuantity()`. Muốn hỗ trợ **giao nhiều đợt** cho cùng một lô thì phải thêm
> cột số lượng nhận trước, rồi đổi luật thành "tổng đã nhận ≤ lượng lô NCC đăng ký". **Đừng
> nới lỏng luật "nhận một lần" mà không thêm cột đó** — nới ra là quay lại đúng lỗi cộng khống.
>
> Ba gạch đầu dòng CÒN LẠI bên dưới (đối chiếu `receiving_lines` với `delivery_batches`,
> `issuePlan()` không loại lô cách ly, `ck_insp_holding_temp` bị comment out,
> `dish_ingredients.quantity` không validate `> 0`) **chưa sửa**.

#### Mô tả gốc (giữ nguyên để đối chiếu)


> **Gạch đầu dòng thứ hai không còn là suy đoán.** Chạy `seed_demo.py` hai lần ⇒ nhận cùng một
> `supplierBatchId` hai lần ⇒ hệ thống **cộng dồn, không chặn, không cảnh báo**: lô NCC khai
> **50 kg** nhưng kho hiện **100 kg**. Tồn kho sai gấp đôi mà không có dấu vết nào trên màn
> hình — và tồn kho là đầu vào của xuất kho FEFO lẫn truy xuất nguồn gốc khi có sự cố.
> **Cần chốt:** chặn hẳn (409 khi tổng nhận > lượng lô NCC đăng ký) hay chỉ cảnh báo cho phép
> ghi đè có lý do. Chưa sửa — chờ quyết định.


- `ReceivingSessionService.create()` **không đối chiếu** `receiving_lines` với
  `delivery_batches`/`purchase_items` ⇒ nhận được lô không thuộc chuyến giao đó.
- UPSERT lô cũ ghi `RECEIPT` bằng **toàn bộ** `supplierBatch.quantity` chứ không phải lượng
  nhận thực ⇒ dễ cộng khống tồn.
- **`SupplierBatchStatus.DELIVERED` KHÔNG được gán ở BẤT KỲ ĐÂU** (đo 08/08/2026:
  `grep -rn "SupplierBatchStatus.DELIVERED" src/main/java` → 0 kết quả) và `quantity` của lô
  NCC **không bao giờ bị trừ**. Lô chỉ đặt `ACTIVE` lúc tạo, hoặc do NCC tự sửa tay. Ngoài
  đời lô 50 kg giao 30 kg thì còn 20 kg, giao hết thì đóng lô — ở đây lô là **giếng không
  đáy**, giao bao nhiêu lần cũng còn nguyên 50 kg. CÙNG GỐC với gạch đầu dòng "cộng khống
  tồn" ở trên: không ai trừ tồn ở phía NCC.
- **`driverId` là TUỲ CHỌN** khi tạo chuyến giao (`DeliveryService`: `if (request.driverId()
  != null && !request.driverId().isBlank())`). Giao thực phẩm cho trường mà không có người
  giao đích danh thì mất truy vết — màn `supplier/delivery-staff` khai nhân viên xong cũng
  không có gì bắt buộc gán.
- `issuePlan()` không loại lô cách ly/hết hạn ⇒ kế hoạch hiện lô khoá, chỉ lỗi lúc submit.
- `ck_insp_holding_temp` (BR-091, ≥60 °C bước 3) bị **comment out** trong V12 vì FE thiếu ô
  nhập `holdingTemp` (CF-24) — chỉ enforce ở service.
- `dish_ingredients.quantity` đã `DROP NOT NULL` (V53) và BE không validate `> 0` — chỉ FE chặn.

---

## B. LUỒNG SỰ CỐ — Giai đoạn 2, **backend 4/4 chốt chặn cứng ĐÃ GỠ 08/08/2026**

> **Trạng thái:** B1·B2·B3·B4 đã sửa **ở backend** (chi tiết trong từng mục). **FE chưa nối** —
> xem `Handoff-E2E-Flow-Readiness.md` mục "Giai đoạn 2" để biết còn phải bấm được ở màn nào.
> B5·B6 chưa động tới.

### B1 · `quarantine_disposition` không nơi nào GHI ⇒ KHÔNG BAO GIỜ đóng được sự cố — ✅ ĐÃ SỬA BE

> **Sửa 08/08/2026.** Thêm `POST /incidents/{code}/quarantine-batches/{batchCode}/disposition`
> (MEDICAL_STAFF), body `{disposition: RETURNED_TO_SUPPLIER|DISPOSED, note?}`:
> - `InventoryBatchPort.applyDisposition(batchId, disposition)` ghi
>   `quarantine_disposition` + `_at`. **MỘT CHIỀU** — câu UPDATE có
>   `and quarantine_at is not null and quarantine_disposition is null`, đã xử lý rồi thì
>   không "đổi ý" được (đúng tinh thần BR-217 / `RULE iqb_no_delete`).
> - Chỉ xử lý được lô mà **CHÍNH sự cố này** đã cách ly — không cho sự cố A gỡ treo lô của B.
> - Ghi `incident_events` loại `RELEASE` (V20 **không** chừa cột cho người XỬ LÝ, chỉ có
>   `quarantine_by_user_id` = người CÁCH LY — nên danh tính người xử lý nằm ở dòng sự kiện;
>   port cố ý **không nhận** `actorId` để không có tham số bị bỏ rơi).
> - `QuarantineBatchDto` nay trả kèm `disposition` / `dispositionAt` (trước đó response
>   **không hề có** cột này ⇒ FE không có cách nào hiện "lô nào còn treo" — tức là người dùng
>   bị chặn đóng sự cố mà không biết vì sao).

**Đã tự kiểm chứng.** Cột thêm ở `V20:326`, **chỉ được ĐỌC** ở
`mf11/port/InventoryBatchPortImpl.java:94`. Không entity field, không endpoint, không màn FE.
Mà `IncidentServiceImpl` chặn đóng khi `quarantineBatchesUnresolved > 0`.

⇒ **Mọi sự cố đã cách ly ≥1 lô là không thể đóng vĩnh viễn**, kể cả khi CAPA COMPLETED và học
sinh hồi phục hết.

**Hướng:** `POST /incidents/{code}/quarantine-batches/{batchCode}/disposition`
(`RETURNED_TO_SUPPLIER` | `DISPOSED`), MEDICAL_STAFF, ghi `quarantine_disposition` + `_at`.

### B2 · "Giữ mẫu để điều tra" ném 500 — vi phạm CHECK — ✅ ĐÃ SỬA BE

> **Sửa 08/08/2026.** `FoodSamplePortImpl.requestRetrieval()` nay set đủ:
> `lab_request_id` (subquery theo mã LABREQ vừa insert) · `sent_to_lab_at` ·
> `retrieved_at` · `incident_id`.
> - `sent_to_lab_at = greatest(:now, collected_at)` — CHECK thứ hai
>   (`ck_smp_sent_after_collect`) đòi `sent_to_lab_at >= collected_at`; sửa mỗi CHECK thứ nhất
>   là đổi 500 này lấy 500 khác khi dữ liệu có mẫu thu ở tương lai.
> - `retention_deadline = greatest(retention_deadline, now + BR-111)` — gia hạn theo quyết
>   định của Nam (xem B3).
> - `where … and status <> 'DISPOSED'`, và **0 dòng khớp ⇒ 422 nêu mã mẫu**, thay vì báo
>   thành công cho một thao tác không đụng gì.

**Đã tự kiểm chứng.** `mf11/port/FoodSamplePortImpl.java:110-118` chạy
`update food_samples set incident_id=…, status='SENT_TO_LAB'` mà **không set**
`lab_request_id` / `sent_to_lab_at` ⇒ vi phạm `ck_smp_sent_has_request`
(`V14__mf08_food_sample_retention.sql:114-115`) ⇒ **rollback toàn bộ**, phiếu LABREQ vừa
insert mất theo.

### B3 · Bếp HỦY ĐƯỢC mẫu đang bị điều tra — ✅ ĐÃ SỬA BE

> **Sửa 08/08/2026, đúng hướng Nam chốt.**
> - **Trạng thái `HOLD` mới** trong `FoodSampleStatus` — "đang bị giữ để điều tra, mẫu VẪN ở
>   trong tủ của trường". Không cần migration: `food_samples.status` là `VARCHAR(24)`
>   **không có CHECK liệt kê giá trị** (đã grep toàn bộ 55 migration để xác nhận).
> - **Endpoint mới `POST /incidents/{code}/sample-hold`** (MEDICAL_STAFF) → port
>   `holdForIncident()`: set `HOLD` + `incident_id` + **gia hạn `retention_deadline`**.
>   Tách hẳn khỏi `sample-retrieval` vì hai việc khác nhau — trước đây muốn giữ mẫu là buộc
>   phải khai khống rằng đã bàn giao đi xét nghiệm.
> - **Mốc gia hạn (ghi lại để không ai hỏi lại):** nạp lại **đúng một chu kỳ BR-111
>   (`RetentionPolicy.MIN_RETENTION_HOURS` = 24h)** kể từ thời điểm giữ, qua
>   `greatest(retention_deadline, now + 24h)`. Chọn mốc này vì nó gắn với một luật có sẵn chứ
>   không phải con số tuỳ tiện, và nó khớp đúng cửa sổ điều tra BR-149 24h — vốn là lý do sinh
>   ra vấn đề (mẫu đủ điều kiện hủy ĐÚNG LÚC điều tra bắt đầu). **Hằng số lấy từ
>   `RetentionPolicy`, không gõ lại số 24.**
> - **`dispose()` chặn thẳng `HOLD` và `SENT_TO_LAB`**, kiểm **TRƯỚC** mốc BR-111 để lý do từ
>   chối đúng nghĩa thay vì "chưa đủ 24h". Chốt chặn thật vẫn là
>   `incidentId != null && hasOpenIncidentLinked(...)`; gia hạn deadline là lớp thứ hai để
>   CHECK ở DB (`ck_smp_dispose_after_deadline`) nói cùng một câu chuyện với tầng service.
> - `retrievableForIncident()` nay lấy `RETAINED` **hoặc `HOLD`** (repo:
>   `findBySchoolIdAndIncidentIdAndStatusIn`) — lọc đúng `RETAINED` như cũ sẽ làm mẫu **biến
>   mất khỏi màn ngay sau khi bấm giữ**.
>
> ⬜ **Còn nợ:** `retrievableForIncident` vẫn **bỏ qua `suspectedDishIds`** (tự thú trong
> Javadoc `FoodSampleServiceImpl`) — chưa lọc theo món nghi ngờ.

`FoodSampleServiceImpl` **có** nhánh chặn `incidentId != null && hasOpenIncidentLinked(...)`,
nhưng đường DUY NHẤT gán `incident_id` cho mẫu còn `RETAINED` là `requestRetrieval` — vốn
đang hỏng (B2) và **không màn FE nào gọi** (`POST /incidents/{code}/sample-retrieval` chỉ tồn
tại trong `schema.d.ts`). Đường còn lại `POST /samples/{id}/incident-link` chỉ nhận mẫu **đã
DISPOSED** — chốt cửa sau khi ngựa đã chạy.

⇒ Chỉ cần qua mốc 24h là hủy được, dù `incidents.status = INVESTIGATING`.

**Nam đã chốt hướng:** khi giữ mẫu thì **gia hạn `retention_deadline`** + thêm trạng thái
`HOLD` vào `FoodSampleStatus` (hiện chỉ có RETAINED/SENT_TO_LAB/DISPOSED, không diễn đạt được
"đang bị giữ để điều tra"). BR-111 (24h) trùng đúng cửa sổ điều tra BR-149 (24h).

Kèm: `retrievableForIncident` **bỏ qua** `suspectedDishIds` — tự thú trong Javadoc.

### B4 · Y tế KHÔNG tra được suất ăn của đúng đứa trẻ — ✅ ĐÃ SỬA BE

> **Sửa 08/08/2026 — cả ba nhánh:**
> 1. **`GET /students/{code}/meal-history` (+ `/{distributionCode}`) mở cho `MEDICAL_STAFF`.**
>    Không cần thêm logic phạm vi: `MealHistoryServiceImpl.resolveAndGuard()` đã có sẵn nhánh
>    `else` chặn theo TRƯỜNG (`studentByCode(code, actor.schoolId())`); chỉ PARENT (con đã
>    liên kết) và TEACHER (lớp chủ nhiệm) mới bị siết thêm. Y tế cố ý **không** giới hạn lớp —
>    học sinh bị ảnh hưởng rải khắp nhiều lớp.
> 2. **`studentsWhoAte()` hết là dead code** — `GET /incidents/{code}/dishes/{dishCode}/students`
>    (MEDICAL_STAFF, SCHOOL_ADMIN). `date` bỏ trống ⇒ lấy `incident.mealDate`. Mỗi dòng có cờ
>    `affectedByThisIncident` để phân biệt "em đã ăn" với "em đã khai trong hồ sơ sự cố".
> 3. **`suspectedDishCandidates()` nay GIAO với `incident_affected_students`** — port mới
>    `dishesEatenByStudentsBetween()` (`group by dish_id`, `order by count(distinct student_id)
>    desc`), trả kèm `affectedStudentsWhoAte`. Bản cũ trả TOÀN BỘ món cả trường trong 24h nên
>    hai sự cố khác nhau cùng ngày ra danh sách **y hệt nhau** — không khoanh vùng được gì.
>    Sự cố chưa khai HS bị ảnh hưởng ⇒ giữ hành vi cũ nhưng `affectedStudentsWhoAte = 0`, và
>    Javadoc DTO ghi rõ `0` nghĩa là **CHƯA giao được**, không phải "không em nào ăn".

Lỗ nghiệp vụ lớn nhất của luồng 2. DB **đã tối ưu sẵn** cho câu hỏi này
(`ix_dist_student_date`, `ix_sdish_dish` với comment ghi thẳng *"em nào đã ăn món X? (MF11)"*),
port `studentsWhoAte()` **đã viết xong** (`mf11/port/ServingDishPortImpl.java:27`) — nhưng:

- **không controller nào gọi** `studentsWhoAte` (dead code);
- `GET /students/{code}/meal-history` chỉ mở cho `PARENT`,`TEACHER`
  (`MealHistoryController.java:46`) ⇒ **MEDICAL_STAFF 403**;
- **không có màn** `medical-staff/students*` nào.

Cái duy nhất y tế có là `GET /incidents/{code}/suspected-dish-candidates` — trả **toàn bộ món
cả trường** trong 24h, **không giao** với `incident_affected_students`.

### B5 · `traceability-map` (BR-151) thiếu một nửa — ✅ ĐÃ SỬA 08/08/2026

> **Đã điền đủ mắt xích.** `TraceNode` nay có `ingredientName`, `supplierId` (mã D1, không phải
> UUID) và **`dishes`** — nửa còn thiếu quan trọng nhất. Bỏ field `batchId` (luôn null, FE không
> dùng, và D1 cấm lộ UUID nội bộ; `batchCode` đã là khoá nghiệp vụ).
>
> Thêm 2 method vào `InventoryBatchPort`: `ingredientNameOf()` và `dishNamesUsingBatch()` —
> cái sau đọc `inventory_transactions` (có cả `batch_id` lẫn `dish_id`), cùng nguồn mà MF06
> `GET /traceability/dish-batches` dùng theo chiều ngược lại.
>
> **Phụ thuộc đã được xử lý luôn:** bản đồ chỉ có món nếu bếp CÓ lập phiếu xuất kho — seed
> trước đây bỏ hẳn bước này nên `dishes` trả `[]`. Đã thêm **pha `issue`** vào seed, và cho seed
> cách ly đúng LÔ CỦA MÓN NGHI VẤN (trước lấy bừa `free[0]`, có lần cách ly lô Gạo tẻ trong khi
> món nghi vấn là Thịt kho trứng — bản đồ vẫn "đúng" nhưng vô nghĩa với người đọc hồ sơ).
>
> **Đo lại trên dữ liệu thật:**
> `SB-THIT-HEO → Thịt heo nạc → SUP-202 Công ty Thực phẩm Nguyễn Nam → RCV-20260808-01
> (08/08/2026) → dùng cho: **Thịt kho trứng**`

#### Mô tả gốc (giữ để đối chiếu)

> **Sửa mô tả 08/08/2026 — ĐO THẬT, không còn "rỗng nghĩa" hoàn toàn.** Gọi
> `GET /incidents/INC-2026-002/traceability-map` trên dữ liệu thật trả:
> `{"status":"COMPILING","nodes":[{"batchId":null,"batchCode":"SB-THIT-HEO",
> "ingredientName":null,"supplierId":null,"supplierName":"Công ty Thực phẩm nguyễn Nam",
> "receivingSessionCode":"RCV-20260808-01","deliveryDate":"2026-08-08"}]}` — **3 field có giá
> trị thật**. Còn null: `batchId`, `ingredientName`, `supplierId`; và **vẫn không có món**.

`IncidentServiceImpl` chỉ lặp `incident.getQuarantineBatches()` và truyền
`TraceNode(null, batchCode, null, null, …)` — `batchId`, `ingredientName`, `supplierId` **luôn
null**, và **không hề có món** trong "bản đồ truy xuất".

Mắt xích thật đã có ở MF06 `GET /traceability/dish-batches` (đã mở cho MEDICAL_STAFF), FE gọi
thủ công per-dish ở `medical-staff/incidents/[id]`.

> Phụ thuộc: BR-076 chỉ chạy nếu bếp **có lập phiếu xuất kho** — `preparation_records` đã bị
> DROP ở V43 nên đó là đường duy nhất. Kịch bản demo bắt buộc đi qua bước xuất kho.

### B6 · Thiếu 2 màn chi tiết CAPA — ✅ ĐÃ SỬA 08/08/2026

> Thêm `school-admin/corrective-actions/[id]` và `service-caterers/corrective-actions/[id]`,
> dùng chung khối hồ sơ chỉ-đọc **`components/app/capa-record-view.tsx`** (nguồn phát sinh,
> đơn vị thực hiện, kế hoạch, mốc thời gian, bằng chứng, lý do bị yêu cầu làm lại / bị từ chối)
> thay vì chép đôi 270 dòng của màn Y tế. Hai danh sách nay có `rowHref` trỏ sang.
>
> BGH duyệt/từ chối ngay trên màn chi tiết (từ chối bắt buộc ghi lý do — kế hoạch trả về Y tế
> soạn lại). Đơn vị suất ăn nhận việc ở đây; **nộp bằng chứng vẫn ở dialog của danh sách** —
> nó cần nhiều ảnh + 3 ô bắt buộc (mô tả, nguyên nhân gốc, xác nhận tái kiểm tra BR-096), đang
> chạy ổn, không có lý do dựng lại lần hai.

#### Mô tả gốc (giữ để đối chiếu)

`school-admin/corrective-actions/` và `service-caterers/corrective-actions/` chỉ có
`page.tsx`, **không có `[id]/page.tsx`** ⇒ BGH duyệt kế hoạch mà không xem được chi tiết; bên
thực hiện nộp bằng chứng trong dialog trên danh sách. **Backend đã đủ endpoint.**

### B7 · Nghi vấn ban đầu không chảy sang khoanh vùng 🟢

`incident_source_reports.suspected_meal_text` là VARCHAR thô, không binding sang `dishes`
(V20 ghi rõ "THÔ, không binding").

---

## C. UI/UX — đo thật, chưa sửa (Giai đoạn 5)

**Tin tốt, đừng tốn công:** Card lồng nhau **gần như không có** (đo thật: 1 chỗ depth 2, phần
còn lại là false positive của skeleton `<Card />` self-closing); **0 chỗ Dialog lồng Dialog**;
47 màn dùng `DataTable` đã có empty state chuẩn.

**Rẻ, giá trị cao:**

| # | Vấn đề | Chỗ |
|---|---|---|
| 1 | **Nút "Đăng xuất" ở sidebar không logout** — `<Link href="/login">` thuần; logout thật nằm ở `user-menu.tsx:227` | `components/app/sidebar.tsx:108-114` |
| 2 | **7 file chết** | `service-caterers/special-meals/`, `medical-staff/report-center/{,compliance,history}/`, `medical-staff/compliance/history/`, `components/app/meal-block-dialog.tsx` (0 import), `service-caterers/menus/create/` (0 link) |
| 3 | Dashboard Y tế **bảng trắng khi rỗng** | `medical-staff/dashboard` |
| 4 | Màn tạo/sửa thực đơn **không có `PageHeader`** (luồng demo chính) | `service-caterers/menus/{create,[id]/edit}` |
| 5 | **Lưu ngày đã format vào payload** — đúng lỗi mà `lib/format.ts:9-13` sinh ra để ngăn | `menus/page.tsx:601`, `menus/[id]/page.tsx:162` |
| 6 | **Footer dialog không sticky** — 18 dialog cuộn, nút Lưu trôi khỏi màn hình. Pattern đúng đã có ở `system-admin/roles/page.tsx:127` | gốc `components/ui/dialog.tsx:21` |
| 7 | **Sidebar Đơn vị Suất ăn 20 mục / 7 nhóm** — chính comment `sidebar.tsx:70-71` thừa nhận phải nới khoảng cách để không tràn 1080p | `lib/roles.ts:283-348` |

**Cơ học, làm theo kịch bản demo trước:**
- **40 màn tự chế Badge trạng thái** thay vì `StatusBadge` — cùng trạng thái hiện khác màu.
- **49/146 màn không dùng `actions` của `PageHeader`** — nút chính mỗi màn một chỗ.
- **47 màn tự dựng `<Table>`** (gần 50/50 với `DataTable`) — mất sort/empty/pagination chuẩn.
  **Đo lại 08/08/2026 (phiên 4) — đây là GỐC của nhiều triệu chứng rời rạc, không phải việc
  làm đẹp:** FE có **3 tầng bảng không tương đương** — 46 file dùng `DataTable` (tự gọi API,
  phân trang, tìm kiếm, skeleton, empty state, click-vào-dòng), **47 file** dùng thẳng
  primitives `@/components/ui/table` (chỉ có khung + màu, mọi thứ khác tự viết), **2 màn**
  còn dùng `<table>` thô với class chép tay (`school-admin/grades`,
  `system-admin/ingredients`) + 1 bản in (`sample-law-print`, chính đáng).
  Triệu chứng đã đo, đều thuộc nhóm 47: bảng cuộn ngang dù màn hình còn trống (`supplier/
  dashboard`, `school-admin/dashboard`), mỗi màn một câu "không có bản ghi", và card KPI lệch
  bảng ngay dưới (bộ lọc của card và của bảng là hai đoạn code khác nhau).
  **Cách sửa đề xuất — CHƯA LÀM, chờ Nam duyệt:** bóc phần trình bày của `DataTable` thành
  `SimpleTable` **nhận sẵn `rows`** (không tự gọi API) rồi chuyển dần. Không ép cả 47 màn dùng
  `DataTable`: nó khoá theo `resource` + tự fetch, sai với bảng nhỏ đã có dữ liệu trong trang.

**Hoãn (chi phí cao):**
- `service-caterers/inspections/[id]` **1641 dòng** (JSX bắt đầu ở 1348) và
  `medical-staff/incidents/[id]` **1571 dòng** (14 handler quy trình, 0 tab) — không có
  stepper. Trước demo chỉ nên thêm progress indicator + tabs cho màn sự cố.
- `components/app/menu-form.tsx` **2813 dòng**. Ranh giới tách tự nhiên: `137-263` (type/hằng
  — 4 file đang import một component `"use client"` chỉ để lấy type), `266-356` (transform
  thuần), `624-836` (`IngredientEditor` + `AllergenPicker`).

---

## D. Nền tảng — đã bàn, chưa làm

### D1 · Độ mịn danh mục nguyên liệu ("sữa dâu vs sữa nho") — Giai đoạn 3

`supplier_products`, `inventory_batches` và `FefoAllocator` **đều chỉ phân biệt bằng
`catalog_ingredients`**. FEFO là lời khẳng định *"mọi lô cùng `ingredient_id` thay thế được
cho nhau"* — hai vị khác nhau chung một mã ⇒ FEFO **không còn đúng**, không tham số nào cứu được.

**Quy tắc phải ghi vào `00_Common_Conventions.md`:** hai thứ dùng chung một mã danh mục khi
và chỉ khi bếp lấy cái nào cũng được mà **món không đổi, hồ sơ dị ứng không đổi, chế độ ăn
không đổi**. Khác vị/khác dị nguyên ⇒ hai mã. Khác NCC/giá/quy cách ⇒ cùng mã, phân biệt ở
`supplier_products`.

**Không** làm bảng biến thể: thêm trục định danh thứ hai buộc cả 4 nơi (đặt hàng, nhận hàng,
FEFO, giải dị nguyên) phải tôn trọng, và `FefoAllocator` sẽ cần một vị từ "tương thích" mà
không ai định nghĩa dứt khoát được.

**Kèm — trạng thái chặn `ingredient_code` NULL** (`V8:105` cho phép NULL = "ngoài danh mục";
món khai chữ tự do rơi khỏi cả chuỗi mua hàng/xuất kho mà **không cảnh báo**):

- ✅ **ĐÃ LÀM 08/08/2026 — chặn khi TẠO/SỬA MÓN.** `DishServiceImpl.applyIngredients()` +
  `ReferenceResolver.ingredientCodeExists()`: thiếu mã hoặc mã không có trong danh mục ⇒ 422
  nêu ĐÍCH DANH nguyên liệu. FE đã chặn từ trước, nhưng gọi API trực tiếp thì lọt — mà đó
  không phải lỗi lý thuyết: DB thật có **10/14 dòng NULL**.
- ⬜ Chặn thêm ở khâu **duyệt thực đơn** (dữ liệu cũ tạo trước 08/08 vẫn có thể còn NULL).
- ⬜ Đặt `NOT NULL` ở DB — chốt chặn cuối, chỉ làm được sau khi chắc chắn không còn dòng NULL.

### D2 · Tách seed khỏi migration production — Giai đoạn 4

Cơ chế **đã có sẵn**: `src/test/resources/application-test.yml:12` dùng
`locations: classpath:db/migration,classpath:db/testseed`; production `application.yml:23`
chỉ `db/migration`. Chỉ cần thêm một nhánh cho demo/dev.

~520 `INSERT` trong 21 migration, ba loại khác hẳn nhau:

| Loại | Ví dụ | Xử lý |
|---|---|---|
| Cấu hình hệ thống | roles/permissions (V2), đơn vị đo (V51), checklist mẫu (V13), report views (V30) | **Giữ**, viết idempotent |
| Danh mục nghiệp vụ | `catalog_ingredients`, `dishes`, `diet_groups` (V9, V11) | Ra khỏi migration → màn quản trị + import |
| Dữ liệu demo | **V39 (399 INSERT)**, V5, V17, V21, V47 | Chuyển sang `db/demo`, chỉ nạp ở profile dev/demo |

> ⚠️ **Thời điểm:** chừng nào V39 còn trong lịch sử migration đã deploy, xoá về sau sẽ **vỡ
> checksum** mọi DB hiện có (phải `flyway repair`/baseline lại). Rẻ nhất là làm **trước lần
> deploy thật đầu tiên**. Đổi lại, việc này đụng cấu hình chạy app ⇒ báo cả nhóm trước khi
> merge, và làm **sau** khi hai luồng đã xanh.

---

## A9 · Đơn mua hàng đặt được nguyên liệu NGOÀI danh mục duyệt — ✅ ĐÃ SỬA 08/08/2026

**Đo được:** `partnership_catalog_items` của PTN-201 **rỗng**, `catalog-change-requests` cũng
**rỗng**, vậy mà đơn mua hàng tạo → giao → nhập kho trót lọt. Hệ thống có **hai danh mục song
song** — `supplier_products` (NCC tự khai, KHÔNG cổng duyệt) và `partnership_catalog_items`
(hợp đồng, có cổng duyệt) — và `PurchaseRequestService.create()` **không kiểm cái nào**, chỉ
resolve `ingredientId` trong danh mục nguyên liệu toàn hệ thống.

⇒ Đặt được **bất kỳ nguyên liệu nào từ bất kỳ NCC nào**, kể cả thứ họ chưa đăng ký bán và chưa
ai duyệt giá. Toàn bộ luồng `catalog-change-request` (code có sẵn từ lâu) **chưa từng chạy thật**.

**Đã sửa (Nam chốt "a là bắt buộc"):** `ReferenceResolver.isIngredientInApprovedCatalog()` +
cổng ở `create()`, mã lỗi **`ING-077` / MSG-154**. Đo lại: `ING-GAO-TE` (chưa duyệt) → 422
ING-077; `ING-CA-BASA` (vừa duyệt qua `catalog-change-request`) → 201.

> `seed/seed_demo.py` đã thêm bước NCC đề xuất → trường duyệt. Không thêm thì seed đứt ngay ở
> `POST /purchase-requests` — bằng chứng cổng có tác dụng thật.

---

## D1-BIS · Tài khoản ĐỐI TÁC không có `schoolId` — lớp lỗi lặp lần thứ 3 (08/08/2026)

**Ba lần, ba module, cùng một gốc.** Tài khoản Đơn vị suất ăn đăng ký qua hợp tác nên JWT chỉ
có `partnerId`, `schoolId` **LUÔN null** — đo thật: `GET /profile/me` trả
`{"role":"service-caterers","schoolId":null,"partnerId":"CAT-201"}`. Mọi đoạn code giả định
"đã đăng nhập thì có schoolId" đều hỏng với đúng vai vận hành bếp:

| Lần | Ở đâu | Triệu chứng | Đã sửa bằng |
|---|---|---|---|
| 1 | MF06 (toàn bộ) | 403 ING-005 mọi endpoint kho/mua hàng | `Mf06Scope.requireSchoolId(actor, refs)` |
| 2 | MF11 (CAPA/sự cố/vi phạm) | Danh sách rỗng IM LẶNG ⇒ **không đóng được sự cố** | `Mf11Scope.schoolId(actor, refs)` |
| 3 | MF02 `SchoolConfigService` + FE `useSchoolConfig` | **Màn Lịch thực đơn tuần khoá TOÀN BỘ ô**, mọi ô ghi "Đang tải cấu hình…" vĩnh viễn ⇒ **vai lập thực đơn không tạo được thực đơn** | `ProfileResponse.activeSchoolId` + nới `requireInScope` |

**Lần 3 nguy hiểm nhất về mặt quy trình** vì nó là "sửa một đầu rồi tuyên bố xong": ngày
06/08/2026 đã thêm `SERVICE_CATERERS` vào `@PreAuthorize` của `GET /schools/{code}/config`
(để MenuForm đọc `costCeiling`) — nhưng chỉ mở **cửa**, còn `requireInScope()` bên trong vẫn
`actor.belongsToSchool(...)` = false ⇒ **SCH-404**. Endpoint "đã mở" suốt 2 ngày mà chưa từng
trả về được gì cho vai đó.

**Cách sửa đã chọn:** thêm `ProfileResponse.activeSchoolId` — "trường mà CẤU HÌNH VẬN HÀNH áp
cho tài khoản này", tách khỏi `schoolId` ("thuộc trường nào", giữ nguyên nghĩa). Suy **chỉ cho
SERVICE_CATERERS** qua `ReferenceResolver.activeSchoolIdForPartner()` (BR-EXCLUSIVE-CATERER cho
quan hệ 1-1 nên xác định); **NULL với NCC** vì phục vụ nhiều trường — chọn bừa một trường sẽ
cho NCC đọc cấu hình của trường không liên quan.

> 🔍 **Việc CHƯA làm — rà nốt lớp lỗi này:** mới sửa 3 chỗ *đã gặp*. Cần `grep -rn "schoolId"`
> phía FE (và `actor.schoolId()`/`belongsToSchool` phía BE) để tìm những chỗ còn giả định
> tương tự **trước khi** người dùng vấp phải. Ưu tiên các màn của vai Đơn vị suất ăn và NCC.
> Ví dụ đã biết còn treo: `providers.tsx` không mở WebSocket cho tài khoản không có `schoolId`
> (comment tại chỗ thừa nhận) ⇒ Đơn vị suất ăn/NCC chỉ có polling 30s.

---

## D2-BIS · FE tải TOÀN BỘ danh sách về client — ✅ ĐÃ XỬ LÝ PHẦN NGUY HIỂM 08/08/2026

> **Đo lại phạm vi thật:** **235** lời gọi `pageSize`, trong đó **211** dùng `pageSize ≥ 100`.
> Viết lại phân trang cho từng chỗ là không khả thi trong một đợt và rủi ro cao. Đã chọn hướng
> khác, theo đúng bài học của `QueryCache.onError`: **biến lỗi im lặng thành lỗi ồn ào**, rồi
> sửa dứt điểm nhóm nguy hiểm nhất.
>
> **1. Cắt cụt nay KÊU TO.** `usePagedList` so `total` với số bản ghi nhận được; lệch thì
> `console.error` nêu đích danh resource, số nhận/tổng và `pageSize`. Trước đó phần dữ liệu
> vượt trang **biến mất không dấu vết** — không lỗi, không cảnh báo, chỉ là kết quả tính sai.
>
> **2. Thêm `useAllPages()`** — đi hết các trang, trả cùng shape với `usePagedList` (đổi qua
> lại mất một dòng). Trần cứng 50 trang; chạm trần thì kêu to thay vì lặng lẽ nã hàng trăm
> request — chạm trần nghĩa là ĐÃ ĐẾN LÚC cần endpoint tổng hợp phía server.
>
> **3. Chuyển 20 file** đang đọc `students` / `allergy-declarations` sang `useAllPages`. Đây là
> nhóm nuôi **cảnh báo dị ứng (BR-063/BR-123)**: thiếu một em là thiếu một cảnh báo, và sai
> kiểu đó không để lại dấu hiệu nào trên màn hình.
>
> **Đo thật thuật toán** (ép `pageSize=1` trên API thật, trường có 3 học sinh):
> trang 1 trả 1 bản ghi · `useAllPages` gom đủ **3/3** · `pageCount=3`.
>
> ⬜ **Còn lại:** ~190 lời gọi `pageSize` lớn khác vẫn tải-hết-về-client. Chúng KHÔNG còn im
> lặng (mục 1) và phần lớn là danh mục nhỏ (món, nguyên liệu, đối tác). Chuyển dần khi đụng
> từng màn; nơi nào bảng hiển thị thì dùng `DataTable` phân trang thay vì tải hết.

#### Mô tả gốc (giữ để đối chiếu)

Đo bằng tab Network khi vào `school-admin/menu-approval`: FE gọi `students?pageSize=300`,
`allergy-declarations?pageSize=500`, `dishes?pageSize=200`, `menu-templates`, `classrooms`…
tức **kéo cả bảng về máy khách rồi lọc bằng JS**. Hôm nay chạy được vì dữ liệu demo nhỏ.

**Vì sao phải sửa trước khi có trường thật:** vài nghìn học sinh là payload hàng MB mỗi lần
tải trang, và `pageSize=300` **âm thầm cắt cụt** ở bản ghi thứ 301 — không lỗi, không cảnh
báo, chỉ là thiếu học sinh trong danh sách dị ứng. Đó là kiểu hỏng nguy hiểm nhất ở màn duyệt
thực đơn.

> Cơn bão **7 request 401 mỗi lần tải trang** đi kèm chỗ này **đã sửa** phiên 4 (`AuthGate`
> chỉ mount `children` khi `authenticated`) — nhưng đó là số LẦN gọi, không phải KÍCH THƯỚC.
> Việc còn lại: dùng phân trang server thật + tìm kiếm phía server cho các màn danh sách lớn.

---

## D3 · BR-EXCLUSIVE-CATERER — quan hệ 1-1 trường ↔ đơn vị suất ăn (ĐÃ LÀM 08/08/2026)

**Luật Nam chốt:** một đơn vị suất ăn chỉ hợp tác với **ĐÚNG MỘT** trường, và một trường chỉ
có **ĐÚNG MỘT** đơn vị suất ăn **tại một thời điểm**. Nhà cung cấp thực phẩm KHÔNG bị ràng
buộc này (nhiều-nhiều là bình thường).

"Đang hiệu lực" = `supplier_partnerships.status = 'APPROVED'` **và** (`until` NULL hoặc còn ở
tương lai) — nhờ vế `until` mà trường đổi nhà thầu theo năm học vẫn giữ được lịch sử cũ.

Chặn ở **hai** chỗ:
- `PartnershipApplicationServiceImpl.submit()` — chặn sớm, để không ai chuẩn bị xong hồ sơ
  mới biết mình không được phép.
- `PartnerApprovalServiceImpl.approve()` — **chốt chặn thật**. Chặn lúc nộp là chưa đủ: hai
  đơn vị khác nhau có thể cùng nộp cho một trường và cùng chờ; duyệt cả hai là trường có 2
  nhà thầu cùng lúc. Đây mới là nơi sinh ra `SupplierPartnership`.

### Tài khoản đối tác KHÔNG gắn `school_id` — liên kết đi vòng

Trả lời câu hỏi hay gặp: **duyệt hợp tác KHÔNG ghi `school_id` vào `user_accounts`.**
`approve()` chỉ tạo/cập nhật `SupplierPartnership`. Tài khoản đối tác luôn `school_id = NULL`
(javadoc `AuthUser`: *"null với System Admin / tài khoản đối tác"*). Chuỗi thật:

```
user_accounts.partner_id → partners.id → supplier_partnerships(APPROVED) → school_id
```

`resolveSchoolId(actor)`: `actor.schoolId()` null ⇒ `activeSchoolIdForPartner(partnerId)`.

> **08/08/2026 — hệ quả mà mục này chưa lường hết, đo ra bằng API thật.** Vì tài khoản đối
> tác không có `school_id`, **mọi module đọc thẳng `actor.schoolId()` đều gãy** với đúng vai
> Đơn vị suất ăn (vai Nam xác nhận là người quản lý toàn bộ bên bếp — **không có vai "nhân
> viên bếp"**). Đo được: **MF06 trả 403 ở 6 endpoint** (mua hàng/nhận hàng/kho/giao hàng) và
> **MF11 trả `total = 0` + 404** cho sự cố và CAPA của chính họ ⇒ CAPA không ai nhận ⇒ **sự
> cố không đóng được**. Đã sửa bằng `Mf06Scope` / `Mf11Scope` (cùng khuôn, dùng
> `activeSchoolIdForPartner`). Chi tiết: `10_E2E_API_Run_Report.md` mục 2.1–2.2.
>
> ⇒ **Quy tắc rút ra:** trong module có endpoint cho SERVICE_CATERERS, **đừng bao giờ dùng
> thẳng `actor.schoolId()`** — luôn đi qua scope helper của module.

**Mắt xích quyết định là `partner_id`, không phải `school_id`** — và seed cũ **thiếu hẳn cột
đó** trong `INSERT INTO user_accounts`. Đó là gốc rễ của bug ghi ở `InspectionServiceImpl`
(*"mọi tài khoản service-caterers bị chặn ở open()/checkGate()/loadInScope() dù JWT/role
đúng"*), vốn đã được vá bằng cách đi vòng school → partner thay vì sửa seed. Luồng đăng ký
thật (`PartnerRegistrationServiceImpl`) **có** `user.setPartnerId(...)` — chỉ seed sai.

### Đã đồng bộ định nghĩa "đang hiệu lực" (08/08/2026)

Ba nơi tra quan hệ hợp tác nay dùng CÙNG một định nghĩa — `APPROVED` **và** (`until` NULL
hoặc còn tương lai):

| Nơi | Trước | Sau |
|---|---|---|
| `SupplierPartnershipRepository` (2 query mới) | — | có `until` |
| `ReferenceResolver.approvedCatererPartnerId()` | thiếu `until` | có |
| `ReferenceResolver.activeSchoolIdForPartner()` | thiếu `until` | có |
| `ReferenceResolver` (dòng ~1148, tra theo ngày) | **đã đúng từ trước** | giữ nguyên |

Vì sao quan trọng: sau khi enforce 1-1, **đổi nhà thầu là thao tác bình thường** (kết thúc
hợp tác cũ → duyệt đơn vị mới). Thiếu vế `until` thì đơn vị CŨ vẫn resolve về trường đó và
**ghi được dữ liệu mới**, đè lên nhà thầu mới.

`SchoolPartnerLinkResolver.partnerIdsConnectedToSchool()` **cố ý** gồm cả đơn và lịch sử
(để BGH thấy ai từng nộp) — không đụng.

> ⚠️ **CÂU HỎI NGHIỆP VỤ CHƯA CHỐT:** hợp tác kết thúc ⇒ đối tác mất luôn quyền **ĐỌC** dữ
> liệu giai đoạn mình từng phục vụ (thực đơn, kiểm thực, mẫu lưu). Đã chọn **an toàn trước**:
> không để nhà thầu đã chấm dứt ghi đè dữ liệu của nhà thầu mới. Nếu nghiệp vụ cần cho họ xem
> lại lịch sử (hồ sơ kiểm thực có nghĩa vụ lưu trữ!), phải làm bằng cơ chế **tường minh** —
> endpoint đọc lịch sử theo khoảng `since..until` — chứ KHÔNG phải nới lỏng hàm phạm vi.

> ⚠️ **Nợ:** ràng buộc chỉ ở tầng SERVICE, chưa có ở DB. Lọc theo `partners.partner_type` nên
> phải JOIN, mà Postgres không cho subquery trong index predicate ⇒ partial unique index
> không dùng được. Muốn chặt hơn thì cần TRIGGER. Rủi ro hiện tại: hai request duyệt đồng
> thời có thể lách. Quy mô một trường thì thấp, nhưng đây là bất biến nghiệp vụ nên đáng làm.

---

## D4 · Mã nguyên liệu — tự sinh (ĐÃ SỬA 08/08/2026)

**Câu hỏi:** `ingredient_code` nên tự sinh hay bắt System Admin gõ tay?

**Trả lời: đã tự sinh sẵn từ đầu — nhưng bị chặn bởi chính validation của mình.**

Ba nơi đều nói mã là TUỲ CHỌN:
- `IngredientServiceImpl.create()` có nhánh `codeGenerator.next("mf06_ingredient", "ING", 3)`
- `IngredientDtos.CreateRequest.code` — Javadoc: *"Bỏ trống để tự sinh"*
- Màn `system-admin/ingredients` — nhãn *"Mã nguyên liệu (tuỳ chọn)"*, placeholder *"để trống tự sinh"*

Nhưng cùng dòng đó có `@NotBlank` ⇒ chặn request TRƯỚC khi tới service ⇒ **nhánh tự sinh là
code chết**, và System Admin buộc phải tự nghĩ mã cho từng nguyên liệu. Đo được:
`POST /ingredients` không kèm `code` → **422 "must not be blank"**.

Đã bỏ `@NotBlank` (giữ `@Size(max=64)`). Đo lại: để trống → **201, `ING-001`**; tự đặt →
**201, `ING-CA-CHUA`**. Cả hai đường đều dùng được.

**Thiết kế còn lại đã đúng, không cần đổi:**
- `update()` **KHÔNG** cho sửa `code` (chỉ name/unit/storageZone/refuseFactor/active) — nên
  FK `fk_dish_ing_catalog` trỏ `catalog_ingredients(code)` an toàn dù không có
  `ON UPDATE CASCADE`: mã bất biến sau khi tạo.
- Cho phép tự đặt mã ngữ nghĩa (`ING-CA-CHUA`) là CÓ ÍCH, không nên bỏ: mã đọc được giúp
  ích khi dò lỗi và khi đối chiếu với bảng giá nhà cung cấp. Chỉ cần đừng BẮT BUỘC.

---

## D5 · Đơn vị tính gõ tự do có gãy luồng không? — KHÔNG, đã chặn 3 tầng

**Câu hỏi:** màn `system-admin/ingredients` cho gõ đơn vị tự do, nhập linh tinh có vỡ không?

**Đo được — an toàn, nhưng nhờ ba lớp chứ không phải một:**

1. **BE validate** ở CẢ `create()` lẫn `update()`: `measureUnitService.isValidUnit(unit)` →
   422 nếu không thuộc `measure_units`. Comment tại chỗ ghi rõ mục đích: *"chặn lỗi gõ
   Kg/KG lẫn lộn"*.
2. **Đơn vị công thức KHOÁ theo danh mục**: trong `IngredientEditor`, chọn nguyên liệu khớp
   danh mục ⇒ `unit` tự suy (kg→g, lít→ml, còn lại giữ nguyên) và ô nhập `disabled`. Comment
   tại chỗ: *"tránh lệch với đơn vị KHO ở bước xuất kho/đặt hàng (từng gây sai 1000 lần)"*.
3. **Ngoài danh mục** cũng là `<Select>` từ `OFF_CATALOG_UNIT_OPTIONS`, không gõ tự do — và
   sau khi BE chặn `ingredient_code` NULL (D1) thì nhánh này không dùng được nữa.

`measure_units` hiện 13 dòng: `g`/`kg` (WEIGHT), `ml`/`lít` (VOLUME) có `factor_to_base`;
9 đơn vị đếm (`bao`, `bó`, `hộp`, `muỗng`, `mớ`, `nải`, `quả`, `thùng`, `ổ`) `factor_to_base`
NULL — đúng, vì đếm thì không quy đổi.

> ✅ **Nửa backend ĐÃ LÀM 08/08/2026:** thêm `common/unit/MeasureUnitController.java` —
> `GET /api/v1/measure-units`, `isAuthenticated()`, chỉ đọc, trả `{code, family, factorToBase}`
> sắp theo `(family, code)`. Không có endpoint GHI (dữ liệu cấu hình, đổi qua migration).
> `factorToBase = null` với COUNT, và Javadoc DTO ghi thẳng **"FE gặp NULL thì đừng đoán hệ số"**.
>
> ✅ **Nửa FE — phần CHỌN đơn vị đã xong 08/08/2026 (phiên 4).** Thêm `lib/measure-units.ts`
> (`useMeasureUnits()`, khuôn `lib/diet-groups.ts`) và **xoá `OFF_CATALOG_UNIT_OPTIONS`**.
> Bốn chỗ nay đọc `GET /measure-units`: `system-admin/ingredients` và
> `supplier/…/catalog` (hai chỗ này trước còn là **`<Input>` nhập tay** — mục 3 phía trên mô tả
> chưa đủ, màn ingredients vẫn gõ tự do cho tới hôm nay), cộng `menu-form` và
> `inspections/[id]` (đang là `Select` nhưng đọc hằng 10 chuỗi khai tay, thiếu 3 đơn vị thật).
>
> ⬜ **Còn nợ đúng một mảnh** — `toBaseQuantity()`/`UNIT_FACTOR` (4 hệ số). `useMeasureUnits()`
> đã có sẵn `.toBase()` lấy hệ số từ server, nhưng `toBaseQuantity` là hàm THUẦN được gọi từ
> trong `mock-data.ts` (3 chỗ) lẫn từ component (3 chỗ), nên phải **truyền bảng xuống** chứ
> không gọi hook trong hàm thuần được; và có một chỗ (`mock-data.ts:2552`) cố ý tra bảng tại
> chỗ để tránh **TDZ** lúc nạp module — đọc comment ở đó trước khi sửa. Đã ghi cảnh báo ngay
> trên `UNIT_FACTOR`: **đừng thêm đơn vị mới vào đó, thêm ở DB**.
>
> ⚠️ **Mô tả gốc của chỗ hở:** FE có bảng `UNIT_FACTOR` **khai cứng** chỉ 4 đơn vị
> (g/kg/ml/lít), nhân đôi `measure_units` của DB. `toBaseQuantity()` gặp đơn vị lạ thì trả
> nguyên `{qty, unit}` — hôm nay vô hại vì 9 đơn vị còn lại đều là COUNT (không cần quy đổi).
> **Nhưng thêm một đơn vị WEIGHT/VOLUME mới** vào `measure_units` (VD `tấn`, `gói 500g`) thì
> FE không biết hệ số ⇒ tính sai **im lặng**.
> Gốc rễ: `/measure-units` **không có controller** (404) nên FE không đọc được bảng thật.
> Đây là lần thứ 4 của cùng lớp lỗi "hằng số nhân đôi" (sau `NUTRITION_TARGET`,
> `menuLeadTimeDays`, `STANDARD_DIETS`). Sửa đúng = thêm `GET /measure-units` rồi cho FE đọc.

---

## E. Đã kiểm và XÁC NHẬN KHÔNG phải gap (đừng điều tra lại)

- Mối nối **đơn hàng → nhập kho** NỐI TỐT: `receiving_sessions.delivery_id` NOT NULL →
  `deliveries.purchase_request_id` (BE bắt buộc) → `purchase_items[].supplier_batch_id`.
  FE đọc thẳng, người dùng không gõ lại gì.
- Mối nối **thực đơn công bố → phân phát** là mối nối **TỰ ĐỘNG DUY NHẤT** thực sự chạy:
  event `Mf05Events.MenuPublished` → `DistributionPlanHandler` sinh `student_distributions`
  cho từng HS, idempotent, định tuyến theo chế độ ăn.
- **Cách ly lô (BR-149) là chỗ enforce TỐT NHẤT** toàn chuỗi: ghi đủ 4 cột, chặn cách ly lại,
  DB có `RULE iqb_no_delete` một chiều, và `createIssueSlip` **chặn xuất kho lô đã cách ly**.
- `menu_dishes`/`food_samples` ràng buộc chặt: `food_samples.inspection_id` NOT NULL,
  `dish_id` NOT NULL, `UNIQUE(inspection_id, dish_id)`.
- Bước 1 (tạo món + khai nguyên liệu) là **mối nối tốt nhất nửa đầu chuỗi** — FE chặn cứng
  không cho lưu nếu thiếu `ingredientId` hoặc `quantity <= 0`.
