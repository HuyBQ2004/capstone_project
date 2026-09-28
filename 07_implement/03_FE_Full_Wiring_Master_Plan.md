# 03 — Kế hoạch nối TOÀN BỘ Frontend còn lại với Backend thật

**Ngày lập:** 2026-08-05 · **Trạng thái:** ✅ XONG mục 1 (MF01→MF13). Phase 2 của
`04_Meal_Substitution_Portion_Pipeline_Design.md` đã được chủ MF06/MF07 duyệt và làm gần trọn —
chỉ còn mục (5) (phụ huynh xem trước thực đơn) tồn đọng. `05_Menu_Grade_Diet_Coverage_Dish_Media_Design.md`
(MF05 — thực đơn mẫu theo khối, ràng buộc không trùng menu mở rộng, checklist đủ nhóm chế độ ăn,
ảnh/mô tả món) đã XONG toàn bộ 05/08/2026. `06_Sent_Reports_Redesign_Design.md` (MF12 —
`sent-reports`, resource cuối cùng có gap sâu chứ không phải rename) đã XONG 05/08/2026 —
**MF12 nay 5/5**; chỉ còn `school-config` (MF02) chưa nối, lệch shape đơn giản hơn nhiều.
**Nguồn trạng thái ban đầu:** `04_Development/Frontend/src/lib/api/real-resources.ts` (nguồn sự
thật duy nhất cho resource nào đã nối thật) — đo lại tại thời điểm lập kế hoạch, sau khi MF11
hoàn tất 5/5 (xem `MF11_Incident_Emergency_Management/03_Buoc3_Progress_Report.md`).

> **Cách dùng file này:** đọc mục 1 để biết thứ tự làm, mục 2 để biết đang làm tới đâu (bảng
> trạng thái cập nhật SAU MỖI resource xong — không chờ xong cả module mới ghi), mục 3 là
> checkpoint log chi tiết (mỗi lần dừng phiên ghi 1 dòng: đã xong gì, đang dở gì, việc kế tiếp).
> Nếu một phiên bị ngắt giữa chừng, đọc mục 3 dòng cuối cùng trước khi đọc code.

---

## 1. Thứ tự thực hiện (theo phụ thuộc, không theo số MF)

| # | Module.Resource | Vì sao thứ tự này |
|---|---|---|
| 1 | ✅ MF06 — 6 resource còn lại (`inventory`, `inventory-transactions`, `deliveries`, `purchase-requests`, `receiving-sessions`, `supplier-batches`) — XONG 05/08/2026, 8/8 | Không cần quyết định kiến trúc mới — verb/role ĐÃ đúng (sửa 03/08), chỉ còn field remap (quantity chuỗi→số+đơn vị, rename...). Làm trước để có nhiều checkpoint xong sớm. |
| 2 | MF03 — `students`, `health-history` | Nền tảng — MF09 (`meal-history`/`special-meal-records`) và nhiều màn khác phụ thuộc `students`. Càng để sau càng nhiều chỗ phải sửa lại. |
| 3 | MF07 — `inspections`, `kitchen-inspections` | MF08 `samples` cần `inspections` thật để tra cứu phiên kiểm thực (BR-105). |
| 4 | ✅ MF08 — `samples`, `sample-lab-requests` — XONG 05/08/2026, 3/3 | Phụ thuộc #3. `sample-lab-requests` còn phụ thuộc MF11 `incidents` — ĐÃ XONG (05/08). |
| 5 | ✅ MF09 — `student-distributions`, `allergy-alerts`, `meal-history`, `special-meal-records` — XONG 05/08/2026, 5/5 | Phụ thuộc #2 (`students`). `student-distributions` là màn P0 (phát suất ăn) — đã thiết kế Phase 1 riêng, xem `04_Meal_Substitution_Portion_Pipeline_Design.md`. |
| 6 | ✅ MF10 — `feedbacks`, `notifications` — XONG 05/08/2026, 4/4 | Độc lập nhưng `notifications` đụng 8 màn NGOÀI MF10 (chờ tới cuối tránh xung đột với các bước trên). |
| 7 | MF12 — `sent-reports` | Cần thiết kế lại payload báo cáo — không phụ thuộc module nào ở trên. |
| 8 | MF13 — `report-schedules` | KHÔNG có màn FE nào tiêu thụ UC-173 — không có gì để nối. Bỏ qua, ghi rõ lý do, không tự dựng màn mới ngoài phạm vi "nối". |

**Quy tắc làm việc (giữ nguyên từ MF11):**
- Đọc code thật trước khi sửa — không suy đoán từ tài liệu.
- `mvn compile` / `npx tsc --noEmit` / `eslint` sạch sau MỖI resource, không dồn tới cuối module.
- Chỉ thêm resource vào `REAL_RESOURCES` khi TOÀN BỘ trang tiêu thụ resource đó đã sửa xong.
- KHÔNG commit/push — chỉ làm khi người dùng yêu cầu rõ.
- Cập nhật bảng ở mục 2 NGAY sau khi một resource chuyển trạng thái, không đợi hết phiên.

---

## 2. Bảng trạng thái (cập nhật sau mỗi resource)

| Module | Resource | Trạng thái | Ghi chú |
|---|---|---|---|
| MF06 | `inventory` | ✅ Xong (05/08) | 6 file FE sửa + 2 lỗi backend vá (xem 3.1) |
| MF06 | `inventory-transactions` | ✅ Xong (05/08) | Gộp chung đợt với `inventory` (8 file tổng, đếm ở 3.2) |
| MF06 | `deliveries` | ✅ Xong (05/08) | `POST /deliveries` từng luôn 403 — bug backend đã vá (xem checkpoint) |
| MF06 | `purchase-requests` | ✅ Xong (05/08) | Thêm `schoolId`/`school` vào response |
| MF06 | `receiving-sessions` | ✅ Xong (05/08) | Vá thêm bug `dateFrom`/`dateTo`/`search` bị bỏ qua |
| MF06 | `supplier-batches` | ✅ Xong (05/08) | Bỏ tính năng "Hủy lô" (không có ở backend thật) |
| MF03 | `students` | ✅ Xong (05/08) | 26 màn sửa + `students/import` viết lại, xem 3.3 |
| MF03 | `health-history` | ✅ Xong (05/08) | 1 màn sửa (`health-records/[id]/history`), xem 3.2 |
| MF07 | `inspections` | ✅ Xong (05/08) | Bỏ "Lưu nháp"; `useAction` thay `useUpdate`; xem 3.4 |
| MF07 | `kitchen-inspections` | ✅ Xong (05/08) | Bỏ "Biên bản kiểm tra bếp" + vá bug filter list; xem 3.4 |
| MF08 | `samples` | ✅ Xong (05/08) | `medical-staff/samples` viết lại xong — MF08 3/3, xem 3.6 |
| MF08 | `sample-lab-requests` | ✅ Xong (05/08) | `handOver()` multipart nối xong qua `medical-staff/samples` — xem 3.6 |
| MF09 | `student-distributions` | ✅ Xong (05/08) | Phase 1 xem `04_Meal_Substitution_Portion_Pipeline_Design.md`; 3 bug backend vá cùng đợt |
| MF09 | `allergy-alerts` | ✅ Xong (05/08) | 0 màn tiêu thụ — bật no-op an toàn |
| MF09 | `meal-history` | ✅ Xong (05/08) | Ride theo tiền tố `students` — không cần thêm vào `REAL_RESOURCES` |
| MF09 | `special-meal-records` | ✅ Xong (05/08) | Đổi từ lọc `student-distributions` sang endpoint thật, xem 3.7 |
| MF10 | `feedbacks` | ✅ Xong (05/08) | Viết lại luồng chọn ngày→bữa→món của 2 form submit, xem checkpoint |
| MF10 | `notifications` | ✅ Xong (05/08) | Xoá `lib/notify.ts` giả lập, viết lại `notification-bell.tsx` với taxonomy 7 giá trị thật |
| MF12 | `sent-reports` | ⏳ Chưa làm | |
| MF13 | `report-schedules` | ⛔ Bỏ qua | Không có màn FE tiêu thụ UC-173 |

---

## 3. Checkpoint log (mỗi dòng = 1 lần dừng/tiếp tục)

- **2026-08-05 (lập kế hoạch):** File này tạo mới. Chưa bắt đầu resource nào. Việc kế tiếp:
  bắt đầu MF06 `inventory` (đọc `service-caterers/receiving/new/page.tsx`,
  `medical-staff/incidents/[id]/page.tsx` chỗ quarantine, và mọi màn khác gọi `usePagedList<
  InventoryBatch>("inventory")`/`useUpdate`/`useCreate` trên resource này).

- **2026-08-05 (đo lại phạm vi thật, giữa lúc làm `inventory`):** Bắt đầu sửa `inventory` thì
  phát hiện **2 lỗi backend thật** (không liên quan FE, đã vá luôn, xem 3.1):
  1. `InventoryBatchPortImpl.markQuarantined()` (MF11 gọi sang MF06) LUÔN ghi
     `quarantine_incident_id = null`, bỏ qua tham số `incidentCode` — cột đã được V10 chừa
     sẵn nhưng chưa bao giờ ghi. Sửa: resolve mã sự cố → UUID rồi ghi thật.
  2. `InventoryService.toResponse()` trả `quarantine.incidentId` bằng `UUID.toString()` —
     sai D1 (phải là MÃ nghiệp vụ `INC-2026-xxx`, không phải UUID nội bộ), FE link sang
     `/incidents/{code}` sẽ 404. Sửa: thêm `ReferenceResolver.incidentCode(UUID)`, dùng thay
     `.toString()`. `mvn compile` sạch sau cả hai vá.

  **Đo lại phạm vi FE của MF06 (grep toàn `src/`):** không phải "sửa 1 file/resource" như bảng
  mục 2 ngụ ý — **20 file** gọi tới `inventory-transactions`/`deliveries`/`purchase-requests`/
  `receiving-sessions`/`supplier-batches` (cộng ~5 file riêng của `inventory`), vì các resource
  này liên kết chặt (`dashboard.tsx` đọc cả `inventory` LẪN `inventory-transactions` cùng lúc,
  `supplier/*` và `service-caterers/*` cùng đọc `purchase-requests`/`deliveries`/
  `supplier-batches` từ 2 phía một luồng nghiệp vụ). **Sửa lệch phạm vi ban đầu ước lượng ở
  mục 1 — KHÔNG phải 6 việc nhỏ, mà là 6 cụm việc lớn**, quy mô mỗi cụm gần bằng cả đợt MF11.
  Việc kế tiếp: hỏi người dùng cách ưu tiên/chia đợt trước khi cắm đầu sửa hết 20 file cùng lúc
  (rủi ro giữa chừng hết ngân sách phiên như đợt MF11 lần đầu).

- **2026-08-05 (người dùng xác nhận: "cứ tiếp tục tuần tự", không dừng hỏi thêm).**

- **2026-08-05 — `inventory` + `inventory-transactions` XONG (2/6 cụm MF06):**
  - **2 lỗi backend vá thêm** (phát hiện khi đọc code thật để nối, không phải tự đi tìm) — đã
    liệt kê ở checkpoint trước, `mvn compile` sạch.
  - **8 file FE sửa:** `service-caterers/inventory/page.tsx`,
    `service-caterers/inventory/issue/page.tsx` (viết lại — bỏ FEFO tính tại client bằng
    `mealIssuePlan()` mock, dùng `GET /inventory/issue-plan` server-side thật),
    `service-caterers/inventory-transactions/page.tsx`, `service-caterers/dashboard/page.tsx`
    (bỏ luôn cảnh báo "thiếu hàng 3 ngày tới" — `upcomingShortages()` mock đọc mảng `MENUS`
    TĨNH, đã disconnect khỏi dữ liệu thật từ trước cả đợt này, không phải lỗi mới),
    `service-caterers/preparation-log/page.tsx`, `service-caterers/inspections/[id]/page.tsx`
    (trang `inspections` CHƯA nối thật — chỉ viết adapter tại chỗ gọi, không đổi hàm mock dùng
    chung), `components/app/menu-form.tsx`, `components/app/incident-detail-view.tsx` (tiện thể
    sửa luôn phần đọc `receiving-sessions` trong `traceBatches()` vì cùng hàm, cùng file —
    `receiving-sessions` CHƯA tính "xong" vì các file khác chưa sửa), `service-caterers/
    orders/new/page.tsx` (bỏ tính năng "gợi ý NCC theo lịch sử giao hàng" — `ShortageLine` thật
    không có field đó).
  - Thêm 9 type alias vào `types.ts` (`InventoryBatchDto`, `IssueSlipDto`, `IssuePlanDto`,
    `IssuePlanLineDto`, `InventoryTransactionDto`, `DeliveryDto`, `PurchaseRequestDto`,
    `SupplierBatchDto`, `ReceivingSessionDto` — 4 cái cuối chỉ khai trước, CHƯA dùng).
  - `tsc --noEmit` sạch, `eslint` sạch, `npm run audit` sạch (54 luật + 53 bảng seed) toàn dự án
    sau mỗi bước, không dồn tới cuối. Đã thêm `"inventory"`/`"inventory-transactions"` vào
    `REAL_RESOURCES`.
  - **Đánh đổi đã biết, ghi trong code:** bảng "lô bị khoá FEFO đã bỏ qua" ở màn Xuất kho bị bỏ
    (server không trả danh sách lô bị loại, chỉ trả lô được chọn); cột "Chia theo khối" chỉ còn
    hiện nhu cầu từng khối, KHÔNG còn hiện lô nào phục vụ khối nào (server gộp phẳng theo
    nguyên liệu, không có breakdown per-dish — xem `InventoryService.issuePlan()`).
  - Việc kế tiếp: `deliveries` + `purchase-requests` + `receiving-sessions` + `supplier-batches`
    (4 cụm còn lại của MF06, ~14 file — `supplier/*` và `service-caterers/*` hai phía một luồng
    mua hàng).

- **2026-08-05 — phát hiện `POST /deliveries` HOÀN TOÀN KHÔNG CHẠY ĐƯỢC (bug nghiêm trọng nhất
  tìm thấy trong cả đợt nối FE), vá xong + verify qua curl thật:**
  - `DeliveryService.create()` dùng `Mf06Scope.requireSchoolId(actor)` — nhưng vai DUY NHẤT
    được gọi endpoint này là SUPPLIER (`@PreAuthorize("hasRole('SUPPLIER')")`), và
    `AuthUser.schoolId()` LUÔN null cho tài khoản đối tác (D9 — supplier phục vụ nhiều trường,
    không có 1 trường cố định). Mọi lời gọi đều nhận `accessDenied()`. Endpoint core của
    UC-083 (đăng ký chuyến giao) chưa từng chạy được từ khi merge.
  - Cùng lúc phát hiện `create()` bỏ qua HOÀN TOÀN `purchaseRequestId`/`driverId` của request
    (không set vào entity) và không insert `supplierBatchIds` vào bảng nối `delivery_batches`.
  - Sửa: suy `schoolId` từ chính `purchaseRequestId` (thêm `PurchaseRequestRepository
    .findByCode`, an toàn vì `codeGenerator` sinh mã KHÔNG theo từng trường — xem comment tại
    chỗ), set đủ `purchaseRequestId`/`driverId` (qua `ReferenceResolver.deliveryPersonIdByCode`,
    2 method mới), và insert `delivery_batches` (cần thêm `ingredient_id`, lấy qua
    `SupplierProductRepository`).
  - **Vá thêm 2 lỗi cùng họ (D1 — trả UUID.toString() thay vì mã nghiệp vụ):**
    `DeliveryResponse`/`PurchaseRequestResponse` thiếu hẳn `schoolId`/`school` — đây là câu hỏi
    người dùng phát hiện ra khi thấy cột "Trường" bị bỏ ở dashboard, đúng lúc đang định
    workaround ở FE thay vì sửa gốc; `SupplierBatchService.toResponse()` cũng trả
    `productId`/`ingredient` = null và `ingredientId` = UUID thô — sửa cả 3 qua
    `ReferenceResolver.ingredientCode/ingredientName` + `product.getCode()`.
  - **Verify qua curl thật** (login `sonlt.supplier.synthetic@example.test`): `GET
    /purchase-requests` trả đúng `school`/`schoolId`; `GET /supplier-batches` trả đúng
    `productId`/`ingredient`; `POST /deliveries` (trước đây luôn 403) nay trả 201 với đủ
    `schoolId`/`school`/`purchaseRequestId`/`driverId`/`linkedBatches` đúng như đã gửi.
  - `mvn compile` sạch, `npm run gen:api` lại. File FE đã sửa trước đó (dashboard, orders/*,
    purchase-requests/*) dùng đúng tên field `schoolId`/`school` ngay từ đầu — chỉ riêng cột
    "Trường" ở `supplier/dashboard/page.tsx` cần thêm LẠI (đã bỏ nhầm trước khi biết đây là bug
    sửa được, không phải giới hạn thật của backend).

- **2026-08-05 — `health-history` XONG (MF03, 1/2 cụm phiên này):**
  - Thêm `lib/health-history-type.ts` (bảng map `KHAI_BAO`/`XAC_MINH`/`GHI_CHU_LAM_SANG`/
    `SU_CO`/`CAP_NHAT_THUOC` ↔ nhãn tiếng Việt có dấu, cùng khuôn `lib/health-severity.ts` —
    backend không có schema component riêng cho enum này nên khai tay literal union, không
    derive từ `Api<...>`), thêm `HealthHistoryEventDto` alias vào `types.ts`.
  - Sửa đúng 1 màn tiêu thụ: `medical-staff/health-records/[id]/history/page.tsx` — đổi
    `usePagedList<HealthHistoryEvent>` (mock) → `usePagedList<HealthHistoryEventDto>` (thật),
    `useDetail<Student>` → `useDetail<StudentDto>`, format `at` (ISO Instant) qua
    `formatVnDateTime()`, badge/nhãn qua bảng map mới.
  - `POST/PUT/PATCH/DELETE /health-history` xác nhận vẫn là thiết kế chủ ý (BR-172, append-only,
    luôn 405 STU-172) — không phải thiếu sót, không cần vá backend.
  - `tsc --noEmit` sạch, `eslint` sạch cho các file đã sửa. **CHƯA verify qua curl thật** (backend
    không chạy sẵn trong phiên này) — khác với thông lệ MF06; rủi ro thấp vì đây là GET thuần,
    endpoint + field `studentId` (mã nghiệp vụ) đã xác nhận đúng ở đợt đo trước (02/08/2026).
  - Đã thêm `"health-history"` vào `REAL_RESOURCES`.
  - Việc kế tiếp: `students` (~26 màn tiêu thụ, xem đo phạm vi ở đầu phiên) — cụm lớn nhất
    trong MF03, cần dựng backend local để verify PATCH/health-file/import trước khi bật.

- **2026-08-05 — `students` XONG (MF03, 2/2 cụm phiên này — MF03 giờ 5/5, TRỌN VẸN):**
  - **3 điểm chặn cũ đóng cả 3:**
    1. `clazz`→`class`/PATCH silent-no-op — đã sửa từ đợt trước (`@JsonProperty("class")`
       trên `UpdateStudentRequest.classroomId`), chỉ cần đổi `useUpdate`(PUT)→`usePatch`/
       `usePatchAction` ở FE (`school-admin/students/[id]`, `academic-years/promote`).
    2. 4 field lâm sàng (`medicalConditions`/`medications`/`emergencyContact`/
       `medicalDocuments`) KHÔNG có trên `StudentResponse` — nằm ở resource con
       `GET/PATCH /students/{code}/health-file` (C8/COND-009, field lạ → 400 STU-094).
       Thêm hook mới `useSubDetail()` (`hooks.ts`, GET `/:resource/:id/:subpath`, mẫu
       chung cho mọi resource con sau này) + dùng lại `usePatchAction` để ghi. Chỉ 1 màn
       cần (`medical-staff/health-records/[id]`) — viết lại dialog UC-094 đúng 4 field
       thật (trước đó mock gộp "Ghi chú lâm sàng" tự do + 1 field khẩn cấp gộp chung,
       nay tách `medicalConditions` (ghi đè, không phải append tự do — chỉ SỰ KIỆN lịch
       sử server tự ghi là append-only) + `emergencyContact`/`emergencyInstructions`
       riêng theo đúng 2 field thật).
    3. `useUpdate` (PUT → 500) — đổi hết sang `usePatch`/`usePatchAction`.
  - **Đo lại phạm vi thật: 26 màn** (không phải ~20 như ước lượng đầu phiên) — 1 file
    `service-caterers/meal-service` đã đúng sẵn từ đợt trước, còn lại sửa mới. Nhóm theo
    độ khó: đa số chỉ đổi type `Student`(mock)→`StudentDto`(thật) + optional-guard
    (`?? []`/`?.length ?? 0` — field DTO thật đều optional, mock cũ không); 3 màn
    (`parent/{dashboard,special-meals}`, `school-admin/special-meals`) phụ thuộc
    `lib/special-meal-plans.ts` (viết cho mock `Student`, field non-optional) — thêm
    `lib/student-adapter.ts` (`studentDtoToMock`) làm ĐIỂM CHUYỂN ĐỔI DUY NHẤT, KHÔNG
    đổi hàm dùng chung (cùng nguyên tắc "adapter tại chỗ gọi" đã dùng ở MF06).
  - **`school-admin/students/import` viết lại HOÀN TOÀN** (không phải sửa field) — mock cũ
    đọc mảng `MOCK_PARSED` cứng (5 dòng, không đọc file thật) rồi tạo từng học sinh qua
    vòng lặp `useCreate`. Backend thật là quy trình 2 pha KHÔNG tương thích (ADR-M03-6):
    `GET /students/template` (tải CSV) → `POST /students/import/validate` (multipart,
    KHÔNG ghi gì, trả `importId`) → `POST /students/import/{importId}/commit` (1
    transaction, all-or-nothing). Viết lại bằng gọi `api` trực tiếp (cùng mẫu
    `useExportReport` ở `school-admin/reports/[slug]`), không có hook chung sẵn cho luồng
    2-pha-với-id-động này.
  - **`school-admin/students/page.tsx` (tạo mới) viết lại payload** — mock cũ gửi
    `{id,name,class,allergies,healthStatus,parent}` (chuỗi tên PH), thật là
    `CreateStudentRequest{id?,name,dob,classroomId,parent:{fullName,email,phone,
    relationship}}` — `dob` từ optional thành `@NotNull`, thêm field bắt buộc email PH
    (BR-030 gate `STU-030` cần `parent.email`).
  - **6 type alias mới** trong `types.ts`: `CreateStudentDto`, `ParentInfoDto`,
    `ImportPreviewDto`/`ImportRowResultDto`/`ImportResultDto`, `HealthFileDto` — riêng
    `UpdateHealthFileDto` KHÔNG derive từ `Api<...>` (backend nhận `Map<String,Object>`
    thô ở `PATCH .../health-file`, không phải DTO đánh dấu — springdoc không sinh schema
    component cho request body này).
  - **1 hook mới** `useSubDetail()` trong `hooks.ts` — `GET /:resource/:id/:subpath`, mẫu
    dùng chung cho mọi resource con dạng "chi tiết phụ gắn theo id bản ghi chính" (health-
    file là ca đầu tiên, không riêng gì MF03).
  - `contract-check.ts`: cập nhật comment — `students`/`health-history` không có dòng
    `MissingOnDto` (cùng lý do allergy-declarations/chronic-conditions: shape lệch đủ sâu
    nên toàn bộ màn tiêu thụ đã đổi hẳn sang đọc DTO thật trực tiếp).
  - `tsc --noEmit` sạch, `eslint` sạch (0 lỗi, 4 warning CÓ SẴN TỪ TRƯỚC ở file không đụng
    tới), `npm run audit` sạch (54 luật + 53 bảng seed, 0 vi phạm mới) sau TOÀN BỘ đợt.
  - **CHƯA verify qua curl thật / chưa chạy `mvn test`** (backend không dựng sẵn trong
    phiên này) — khác thông lệ nghiêm ngặt của MF06/MF11 (đọc code Java đối chiếu field
    thủ công, không suy đoán, nhưng KHÔNG có xác nhận runtime). Rủi ro cao hơn `health-
    history` vì khối lượng thay đổi lớn hơn nhiều (26 màn + 1 hook mới + 1 luồng import
    viết lại từ đầu) — **việc kế tiếp bắt buộc trước khi coi MF03 "chắc chắn xong":
    dựng backend local (`docker compose` + `mvn spring-boot:run`), test tay ít nhất
    `school-admin/students` (CRUD+import) và `medical-staff/health-records/[id]`
    (đọc/ghi health-file) qua trình duyệt thật.**
  - Đã thêm `"students"` vào `REAL_RESOURCES` — MF03 giờ 5/5, không còn resource nào mock.
  - Việc kế tiếp theo kế hoạch mục 1: MF07 (`inspections`, `kitchen-inspections`).

- **2026-08-05 — MF07 HOÀN TẤT 2/2 (`inspections`, `kitchen-inspections` — MF07 giờ 4/4,
  TRỌN VẸN):**
  - **2 quyết định LUỒNG NGHIỆP VỤ hỏi người dùng trước khi sửa** (khác field/verb đơn thuần,
    ảnh hưởng UX màn P0 an toàn thực phẩm — không tự quyết như các tính năng nhỏ hơn):
    1. Màn kiểm thực 3 bước (`service-caterers/inspections/[id]`, ~1345 dòng) — người dùng
       chọn **bỏ nút "Lưu chi tiết mẫu biểu"** (lưu nháp): backend chỉ có `POST
       /{code}/steps/{step}` (LUÔN chốt, không có khái niệm nháp phía server). Dữ liệu đang
       nhập giữ trong state React (như điền form giấy) tới khi bấm Xác nhận Đạt/Không đạt.
    2. Tính năng "Biên bản kiểm tra bếp" (`conduct-inspection/[id]/record`, in/ký tay/tải ảnh
       bản ký) — **bỏ hẳn** theo đúng tiền lệ MF06 (tính năng không backend thì không giả
       lập): 6 field `record*` không tồn tại trên `KitchenInspectionResponse`/entity, và
       `KitchenInspectionController` không có PUT/PATCH nào để ghi. Route giữ nguyên URL,
       nội dung đổi thành "Chưa hỗ trợ" (không xoá route, tránh vỡ liên kết cũ).
  - **`inspections` — 12 màn sửa** (`service-caterers/{inspections,inspections/[id],
    inspections/[id]/print,dashboard,meal-service,samples}`, `medical-staff/{inspections,
    inspections/[id],compliance,compliance/report}`, `school-admin/compliance`,
    `teacher/distribution/[id]`). `useAction` thay `useUpdate` cho 4 action-endpoint
    (`steps/1|2|3`, `medical-review`); `formHeader` đổi từ form nhập sang HIỂN THỊ CHỈ ĐỌC
    (server luôn tự tính lại từ schoolName/inspector thật/decidedAt, không nhận override).
    `date` thật ISO, `meal` enum thô ("LUNCH") — khác hẳn mock (dd/MM/yyyy, nhãn Việt); thêm
    `lib/inspection-adapter.ts` (`inspectionDtoToMock`) cho 4 hàm dẫn xuất dùng chung viết
    cho mock (`menusOfInspection`/`sampleDishOptionsOfInspection`/`inspectionForMeal`/
    `mealServingGate`) — không đổi các hàm đó, chỉ thích ứng tại chỗ gọi. **Phát hiện thêm**:
    `teacher/distribution/[id]` gọi `mealServingGate()` THIẾU tham số `sessions` (mặc định
    đọc mảng SEED `INSPECTIONS` đóng băng) — cùng lớp lỗi `menusOfInspection(session)` thiếu
    tham số đã vá ở MF06 — vá cùng đợt.
  - **`kitchen-inspections` — 6 màn sửa** (`medical-staff/conduct-inspection/{page,[id],
    [id]/record,history,checklist}`, `school-admin/dashboard`, cộng `medical-staff/
    {compliance,compliance/report}`/`school-admin/compliance` dùng chung với `inspections`).
    Form nộp phiên viết lại payload đúng `SubmitKitchenInspectionRequest{pestControlValid,
    staffHealthValid,itemResults[]}` — `score`/`inspector`/`failedItems`/`itemFindings`/
    `photosCount`/`confirmStatus` đều DẪN XUẤT server (điểm tự tính lại từ `itemResults`,
    CF-26, không tin số client). **Phát hiện thêm**: `ItemResultRequest.item` phải là MÃ
    hạng mục (`ChecklistItem.code`, tra qua `findBySchoolIdAndCode`), KHÔNG phải TÊN như
    mock cũ dùng làm key — đổi toàn bộ state `checks`/`evidence` sang key theo `item.id`
    (mã), giữ `item.name` chỉ để hiển thị.
  - **1 bug backend vá thêm**: `KitchenInspectionServiceImpl.list()` dùng lambda tự viết chỉ
    lọc `schoolId`, bỏ qua HOÀN TOÀN `dateFrom`/`dateTo`/`result` (toolbar
    `conduct-inspection/history`) — cùng lớp lỗi `DeliveryService.list()`/
    `ReceivingSessionService.list()` đã vá ở MF06. Thêm `KitchenInspectionSpec.java` (cùng
    khuôn `InspectionSpec` có sẵn), `mvn compile` sạch.
  - **2 hàm/component dùng chung nới kiểu tham số** (không đổi hành vi, chỉ chấp nhận thêm
    shape DTO thật bên cạnh mock — cùng nguyên tắc `dietGroupIdOf` ở MF03): `lib/
    special-meal-plans.ts` không đụng ở đợt này; `components/app/kitchen-confirm-badge.tsx`
    (`stateOf`/`KitchenConfirmBadge`/`kitchenConfirmSentence`/`kitchenConfirmTone`) đổi
    `Pick<KitchenInspection,"confirmStatus">` (mock) → `{confirmStatus?: string}`.
  - `tsc --noEmit` sạch, `eslint` sạch (0 lỗi, 3 warning CÓ SẴN TỪ TRƯỚC ở file không đụng
    tới — 1 warning CỦA CHÍNH `conduct-inspection/page.tsx` tự hết sau khi xoá import
    `usePagedList` không dùng), `npm run audit` sạch (54 luật + 53 bảng seed, 0 vi phạm
    mới), `mvn compile` sạch. **`mvn test -Dtest="com.sfms.mf07.**"` KHÔNG chạy được** — 4
    integration test cần Testcontainers/Docker, môi trường agent không có Docker (khác biệt
    môi trường, không phải lỗi code — xác nhận qua compile sạch + đối chiếu tay từng field
    request/response với Java source, không suy đoán).
  - **CHƯA verify qua curl thật / trình duyệt** (cùng giới hạn môi trường đã ghi ở MF03) —
    rủi ro cao nhất nằm ở luồng `conduct-inspection` (đổi key checklist từ tên sang mã) và
    luồng chốt bước kiểm thực 3 bước (BR-090 core-temp gate, BR-095 CAPA tự sinh) — nên test
    tay ưu tiên 2 luồng này trước khi coi MF07 "chắc chắn xong".
  - **Hệ quả cho MF08**: `samples`/`sample-lab-requests` từng bị chặn CHÍNH VÌ `inspections`
    chưa migrate (dropdown "phiên đã Passed" gửi mã MOCK lên backend thật → BR-105 luôn báo
    lỗi). Chặn đó nay đã gỡ — MF08 sẵn sàng cho đợt kế tiếp theo đúng thứ tự kế hoạch mục 1.
  - Đã thêm `"inspections"` + `"kitchen-inspections"` vào `REAL_RESOURCES` — MF07 giờ 4/4.
  - Việc kế tiếp theo kế hoạch mục 1: MF08 (`samples`, `sample-lab-requests`).

- **2026-08-05 — MF06 HOÀN TẤT 8/8 resource (`deliveries`, `purchase-requests`,
  `receiving-sessions`, `supplier-batches` — cụm cuối cùng):**
  - **14 file FE sửa:** `supplier/{dashboard,purchase-requests,purchase-requests/[id],
    batches,batches/[id],deliveries,deliveries/[id]}/page.tsx`, `service-caterers/{orders,
    orders/[id],orders/new,receiving,receiving/new}/page.tsx`. Phần lớn field đã đúng verb
    từ đợt 03/08 (Huy) — đợt này chủ yếu đổi TYPE mock→DTO thật + field rename
    (`productName`→`ingredient`, `certificates`→`certificateIds`, ngày ISO thay
    dd/mm/yyyy, `batchAssignments`→đọc trực tiếp `items[].supplierBatchId`).
  - **2 bug backend MỚI vá thêm** (ngoài bug `POST /deliveries` đã ghi ở checkpoint trước):
    1. `DeliveryService.list()` bỏ qua hoàn toàn `filters`/`search` — `not:status=COMPLETED`
       (FE dùng ẩn chuyến đã xong khỏi tab "Sắp nhận hàng") không có tác dụng gì. Vá bằng
       `SpecSupport.applyExactFilters`/`searchPredicate`, đúng khuôn các module khác.
    2. `ReceivingSessionService.list()` cùng lỗi — bỏ qua `dateFrom`/`dateTo`/`search`, tab
       "Lịch sử & báo cáo" lọc theo khoảng ngày không có tác dụng. Vá tương tự, so sánh
       `receivedAt` (Instant) với `dateFrom.atStartOfDay(...)`/`dateTo.plusDays(1)...`.
    3. `SupplierBatchStatus` KHÔNG có `CANCELLED` — tính năng "Hủy lô" (mock) không có chỗ
       lưu ở backend thật. BỎ tính năng (không giả lập bằng enum sai), ghi rõ trong code.
    4. `POST /supplier-batches` không nhận danh sách chứng chỉ — chứng chỉ gắn qua endpoint
       multipart RIÊNG (`POST .../certificates`, upload file mới, KHÔNG tham chiếu tài liệu
       tuân thủ có sẵn). Dialog "Nộp lô" bỏ bước chọn chứng chỉ có sẵn; dialog "Chi tiết lô"
       nối THẬT (trước đây chỉ hiện toast, không gọi API nào).
  - **Đánh đổi ghi trong code:** "gợi ý NCC theo lịch sử giao hàng" (đơn hàng mới) bỏ —
    `ShortageLine` thật không có field đó; panel "Chứng chỉ liên kết" chỉ hiện MÃ tài liệu,
    không hiện tên/HSD (không có endpoint đọc lại chi tiết tài liệu ad-hoc).
  - **Verify qua curl thật** (2 lần — lần đầu quên rebuild+restart sau khi sửa filter, `total`
    không đổi dù truyền `dateFrom` tương lai; rebuild lại thì đúng `total:0`): xác nhận cả
    `not:status` (deliveries) và `dateFrom`/`dateTo` (receiving-sessions) hoạt động thật.
    **Bài học: sau MỌI lần sửa Service, phải `mvn package` + restart lại tiến trình `java`
    đang chạy trước khi verify — sửa xong mà quên restart thì curl vẫn thấy hành vi cũ.**
  - `tsc --noEmit` sạch, `eslint` sạch, `npm run audit` sạch toàn dự án. Đã thêm cả 4 resource
    vào `REAL_RESOURCES` — **MF06 giờ 8/8, không còn resource nào ở trạng thái mock**.
  - Dọn container Postgres tạm + tiến trình `java` nền sau khi xong.
  - **Việc kế tiếp theo kế hoạch mục 1: MF03 (`students`, `health-history`).**

- **2026-08-05 — MF08 ĐANG LÀM DỞ (`samples` + `sample-lab-requests`), dừng phiên giữa chừng —
  đọc kỹ mục này trước khi làm tiếp (3.5):**
  - **1 bug backend nghiêm trọng vá xong TRƯỚC đợt này** (không lặp lại ở đây, xem code):
    `FoodSampleServiceImpl.resolveIncidentId()` băm mã sự cố thành UUID giả bằng
    `UUID.nameUUIDFromBytes()` thay vì tra thật — khiến `linkDisposedToIncident()`/
    `SampleLabRequestServiceImpl.handOver()` LUÔN 409 SMP-023 cho MỌI mã hợp lệ. Đã vá: thêm
    `IncidentStatusPort.resolveByCode(code, schoolId)`, hiện thực ở `IncidentStatusPortImpl`
    (mf11, dùng `IncidentRepository.findByCodeAndSchoolId`), sửa `resolveIncidentId` dùng port
    thật + thêm tham số `schoolId`, cập nhật 3 chỗ gọi. `mvn compile` sạch.
  - **1 bug backend nghiêm trọng thứ hai, phát hiện VÀ vá NGAY TRONG đợt này** (khi viết
    `RegisterSampleDialog`, cần `dishId` mã nghiệp vụ để gửi `register()`): `Step2RowDto`/
    `Step3RowDto` (`InspectionSessionResponse.step2Rows[].dishId`/`step3Rows[].dishId`) trả
    THẲNG `r.getDishId().toString()` — UUID nội bộ, vi phạm D1 — trong khi field `dishId` Ở
    CẤP SESSION (không phải step rows) đã đúng D1 từ đầu (dùng biến `dishCode`). Vì
    `FoodSampleServiceImpl.register()` tra `dishId` bằng `ReferenceResolver.dishIdByCode()`
    (so cột `code`, không phải `id`), gửi UUID thô từ step rows sẽ LUÔN 422 "unknown dish" —
    tức đăng ký mẫu cho bất kỳ phiên kiểm thực nào có ≥2 món (đa số ca thật) sẽ luôn hỏng ở
    bước cuối dù đã qua hết BR-105/109/203/107. Đã vá: `InspectionAssembler.toStep2Dtos`/
    `toStep3Dtos` gọi `referenceResolver.dishCode(r.getDishId())` thay vì `.toString()`.
    `mvn compile` sạch (file: `mf07/service/InspectionAssembler.java`).
  - **FE ĐÃ XONG, `tsc --noEmit` sạch cho từng file** (kiểm tra riêng lẻ, chưa chạy full-project
    sau cùng — xem việc kế tiếp):
    1. `lib/api/types.ts` — thêm 8 alias: `FoodSampleDto`, `RegisterSampleDto`,
       `UpdateStorageDto`, `DisposeSampleDto`, `IncidentLinkDto`, `Mau4LabelDto`,
       `SampleLabRequestDto`, `HandOverSamplesDto`.
    2. `service-caterers/samples/page.tsx` — viết lại hoàn toàn. Điểm khác mock đáng chú ý:
       bỏ nhập mẫu tự do KHÔNG kèm phiên kiểm thực (backend bắt buộc `inspectionId`+`dishId`
       hợp lệ); `dishId` lấy TRỰC TIẾP từ `step2Rows`/`step3Rows` của phiên đã chọn (không
       cần thêm catalogue `dishes` riêng — dùng thẳng field mới vá ở trên); nhiệt độ 2–8°C
       (BR-110) đổi từ chặn cứng (mock) sang CẢNH BÁO MỀM (đúng hành vi backend thật,
       `tempOutOfRange` không chặn `register()`); GPS bỏ qua (geofence hiện no-op khi trường
       chưa cấu hình toạ độ, không có API trình duyệt an toàn thay thế); XOÁ HẲN component
       `LabRequestTasks` (luồng BR-215 Deprecated "bếp xác nhận thu hồi rồi gửi lab" — không
       thể xảy ra với `handOver()` thật, luôn tạo phiếu ở `SENT_TO_LAB` ngay); filter
       `mealPrefix` (LIKE) → `mealType` (exact enum, đúng tên `FoodSampleSpec` hiểu); `register()`
       gọi qua `useUploadMutation` (multipart `payload`+`evidencePhotos[]`).
    3. `service-caterers/samples/[id]/page.tsx` — viết lại. `dispose()` qua `useAction`
       (`POST /{id}/dispose`), bỏ `disposedBy` khỏi payload (server tự lấy JWT, BR-114); bỏ
       nhánh trạng thái `"RETRIEVED"` (không tồn tại ở `FoodSampleStatus` thật — chỉ có
       `RETAINED`/`SENT_TO_LAB`/`DISPOSED`), thay bằng nhánh `SENT_TO_LAB` đúng thật (Y tế đã
       bàn giao qua `sample-lab-requests`, không còn thao tác gì ở màn bếp).
    4. `service-caterers/samples/[id]/print/page.tsx`, `service-caterers/samples/print/page.tsx`
       — đổi type `FoodSample`→`FoodSampleDto`; trang sổ theo ngày đổi lọc từ
       `collectedAt.startsWith(dd/MM/yyyy)` (client, mock) sang `dateFrom`/`dateTo` (server,
       `FoodSampleSpec`) vì `collectedAt` thật là ISO.
    5. `components/app/sample-law-print.tsx` — widen `Mau4Label`/`Mau5Sheet` từ `FoodSample`
       (mock) sang `PrintableSample` cục bộ (cùng nguyên tắc `kitchen-confirm-badge.tsx` MF07);
       `vnTimeWords()` chạy `formatVnDateTime()` trước khi tách câu chữ — nhận cả ISO thật lẫn
       `dd/MM/yyyy HH:mm` mock cũ.
  - **FE CHƯA LÀM — việc kế tiếp bắt buộc, đọc kỹ trước khi code (file lớn nhất còn lại,
    ~900 dòng, KHÔNG rewrite bừa — làm theo đúng thứ tự dưới, `tsc` sau mỗi bước lớn):**
    1. **`medical-staff/samples/page.tsx`** (chưa sửa 1 dòng nào, vẫn 100% mock-typed):
       - Bỏ HẲN khối "hủy yêu cầu thu hồi" (`labReqPage`/`updateLabRequest`/`cancelTarget`/
         `cancelReason`/`confirmCancelRequest` + render `RETRIEVAL_REQUESTED`) — backend không
         có PATCH/PUT/cancel nào trên `sample-lab-requests` (chỉ GET list/detail + POST
         handOver + GET retrievable).
       - Bỏ toàn bộ tính cửa sổ điều tra thủ công (`vnTime`/`withinWindow`/`inScope`/
         `INVESTIGATION_WINDOW_HOURS`/nút "Bỏ giới hạn thời gian") — thay bằng gọi thật
         `GET /sample-lab-requests/retrievable?incidentId=<mã>&includeAllDishes=<bool>`
         (`SampleLabRequestController.retrievable`, trả `FoodSampleResponse[]` trần, server tự
         tính cửa sổ 24h qua `IncidentStatusPortImpl.windowOf()` — nguồn sự thật duy nhất,
         không tính tay ở FE nữa). ⚠️ `includeAllDishes` được BACKEND CHẤP NHẬN NHƯNG CHƯA LỌC
         (xem Javadoc lớp `FoodSampleServiceImpl`) — endpoint LUÔN trả full cửa sổ bất kể cờ
         này; muốn thu hẹp về "chỉ món nghi vấn" (mặc định) vẫn phải tự lọc client-side theo
         `contextIncident.suspectedDishes.map(d => d.dishName)` so với `sample.dish` — **nhân
         tiện vá 1 bug mock-era phát hiện lúc đọc code cũ: bản gốc so `d.dishId` (một ID) với
         `s.dish` (một TÊN), lẽ ra phải là `d.dishName`.**
       - Bỏ khối "Đã bàn giao trước đó" (`alreadySentForIncident`, so `sample.incidentId ===
         incidentParam`) — `FoodSampleResponse.incidentId`/`SampleLabRequestResponse.incidentId`
         trả UUID nội bộ THÔ (giới hạn đã ghi trong Javadoc `FoodSampleServiceImpl`, §2.5 mục 2
         TDS-MF08), còn `incidentParam` (từ URL) là MÃ nghiệp vụ — so sánh này KHÔNG BAO GIỜ
         khớp, không có cách nào tra ngược đáng tin cậy từ FE hiện tại. Bỏ hẳn theo đúng
         nguyên tắc "tính năng không backend hỗ trợ đúng thì không giả lập", KHÔNG viết logic
         so khớp sai âm thầm.
       - `createLabRequest` (`useCreate` JSON) + vòng lặp `update.mutateAsync` theo từng mẫu
         → GỘP thành 1 lời gọi `useUploadMutation<SampleLabRequestDto>("sample-lab-requests")`
         (multipart `payload` = `jsonPart(HandOverSamplesDto)` + `sealPhotos`/`handoverRecords`
         file part) — `handOver()` thật tự chuyển N mẫu sang `SENT_TO_LAB` trong CÙNG 1
         transaction (ADR-M08-1), vòng lặp phía client là thừa VÀ sai (không nguyên tử).
       - `batchLab` (input tự do + datalist tên) → đổi thành chọn `authorityId` (MÃ nghiệp vụ,
         `a.id` của `ReceivingAuthorityDto`) + thêm Select `handoverType` (`LAB`/`AUTHORITY`,
         bắt buộc) — `authorityId` chỉ bắt buộc khi `AUTHORITY`, `testCriteria` chỉ bắt buộc
         khi `LAB` (BR-212, xem `SampleLabRequestServiceImpl.handOver()`).
       - `sealFiles`/`handoverFiles` hiện là `mockUpload()` giả lập (không có File thật) → đổi
         sang `<input type="file" multiple>` thật, cùng mẫu evidence photos ở
         `service-caterers/samples/page.tsx`.
       - `openLinkDisposed`/`submitLinkDisposed` (AF1) → `useAction<FoodSampleDto>("samples")`,
         action `"incident-link"`, body `{incidentId: incident.id}` (mã nghiệp vụ — backend tra
         qua `resolveByCode` đã vá).
       - Filter toolbar: `mealPrefix`→`mealType` (như `service-caterers/samples`); `dish` build
         từ TÊN món (`allSamples.items.map(s=>s.dish)`) → phải build cặp `{name, code}` từ
         `s.dish`+`s.dishId` và gửi CODE lên (`FoodSampleSpec` tra `dish` param bằng
         `dishIdByCode`, không phải theo tên).
       - Cột bảng: bọc optional (`hoursRemaining ?? 0`, `fridgeTemp ?? 0`,
         `status={... ?? ""}`) — DTO thật mọi field đều optional, mock cũ không.
       - Nút "Export" hiện toast giả — `GET /samples/export?format=xlsx|pdf` CÓ THẬT
         (`SampleRegisterExportService`, trả byte[]) nhưng CHƯA QUYẾT dùng `responseType:
         "blob"` qua `api` (axios) rồi tự trigger download, hay bỏ qua đợt này ghi rõ lý do —
         quyết định còn bỏ ngỏ, chưa hỏi người dùng.
    2. Sau khi file trên xong + `tsc`/`eslint` sạch: rà lại các màn ĐÃ sửa cho MF07 nhưng CŨNG
       đọc `samples`/`sample-lab-requests` — CHƯA kiểm tra riêng cho MF08 lần này:
       `medical-staff/compliance/page.tsx`, `medical-staff/inspections/page.tsx`,
       `medical-staff/inspections/[id]/page.tsx` (đếm "Số mẫu đã lưu"),
       `medical-staff/incidents/[id]/page.tsx` (có comment tường minh "MF08 vẫn mock" — cần
       cập nhật/xoá comment đó), `service-caterers/dashboard/page.tsx` (thống kê `samples`).
    3. Chỉ SAU KHI toàn bộ trên xong: thêm `"samples"` + `"sample-lab-requests"` vào
       `REAL_RESOURCES`, viết lại đoạn comment MF08 cũ (còn ghi "⛔ 2/3 CÒN LẠI") thành tóm tắt
       "✔ xong" (theo đúng khuôn các đoạn MF03/MF06/MF07 đã có), chạy `tsc --noEmit`/`eslint`/
       `npm run audit` TOÀN DỰ ÁN (chưa chạy full-project lần nào trong đợt này, chỉ lọc theo
       tên file), rồi cập nhật bảng mục 2 + đóng dòng checkpoint này.
  - **CHƯA verify qua curl/trình duyệt thật** cho toàn bộ phần FE đã xong ở trên (cùng giới hạn
    môi trường đã ghi từ MF03/MF07) — rủi ro cao nhất nằm ở `RegisterSampleDialog` (chuỗi
    `inspectionId`→`dishId` mới vá) và `dispose()`/`incident-link` (phụ thuộc bản vá
    `resolveIncidentId`) — nên test tay các luồng này trước khi coi MF08 "chắc chắn xong".
  - Việc kế tiếp: mục 1 (FE CHƯA LÀM) ở trên, theo đúng thứ tự liệt kê.

- **2026-08-05 — MF08 HOÀN TẤT 3/3 resource (`medical-staff/samples/page.tsx` viết lại —
  MF08 giờ TRỌN VẸN):**
  - **`medical-staff/samples/page.tsx` viết lại theo đúng thứ tự đã ghi ở checkpoint trước:**
    bỏ hẳn khối "hủy yêu cầu thu hồi" (`RETRIEVAL_REQUESTED`, BR-215 Deprecated — backend không
    có cancel nào); bỏ tính cửa sổ điều tra tay (`vnTime`/`withinWindow`/`inScope`/
    `INVESTIGATION_WINDOW_HOURS`) — thay bằng `GET /sample-lab-requests/retrievable?incidentId=
    &includeAllDishes=` (server tự tính qua `IncidentStatusPort.windowOf()`); bỏ khối "Đã bàn
    giao trước đó" (so `incidentId` UUID với mã nghiệp vụ URL — không bao giờ khớp, đã xác nhận
    qua đọc `FoodSampleAssembler`/Javadoc); gộp `createLabRequest`+vòng lặp `update` thành 1
    `useUploadMutation<SampleLabRequestDto>("sample-lab-requests")`; `batchLab` tự do → chọn
    `authorityId` (mã `ReceivingAuthorityDto.id`, xác nhận qua `ReceivingAuthorityServiceImpl` —
    field `id` = `code`) + Select `handoverType` (LAB/AUTHORITY, BR-212); `sealFiles`/
    `handoverFiles` đổi từ `mockUpload()` sang `<input type=file>` thật (đọc code
    `SampleLabRequestServiceImpl.handOver()` xác nhận `handoverRecords` BẮT BUỘC ở server —
    422 nếu rỗng, không chỉ là gợi ý UI như ghi ở checkpoint trước); `openLinkDisposed`/
    `submitLinkDisposed` (AF1) → `useAction<FoodSampleDto>("samples")` action `incident-link`;
    filter `mealPrefix`→`mealType`, `dish` build cặp `{name,dishId}` gửi CODE; Export đổi từ
    toast giả sang `GET /samples/export` thật (blob download, cùng mẫu `useExportReport` ở
    `school-admin/reports/[slug]`) — quyết định bỏ ngỏ ở checkpoint trước (dùng blob hay bỏ qua)
    tự chọn "dùng blob" vì đã có tiền lệ sẵn trong repo, không cần hỏi thêm.
  - **1 hook mới** `usePlainList` nhận thêm `opts?: {enabled}` (`hooks.ts`) — cần để gọi
    `retrievable` có điều kiện (chỉ khi có `incidentParam`) mà vẫn giữ hook luôn được gọi
    unconditionally (React rules of hooks); tương thích ngược, mọi chỗ gọi cũ không truyền
    tham số thứ 3 vẫn `enabled: true` như trước.
  - **Rà lại các màn khác đọc `samples`/`sample-lab-requests` (mục 2 checkpoint trước) — 4 file
    sửa thêm:**
    - `medical-staff/compliance/page.tsx` — `FoodSample`(mock)→`FoodSampleDto`; `collectedAt`
      giờ là ISO Instant (mock cũ `dd/MM/yyyy HH:mm`) nên `s.collectedAt.startsWith(TODAY)` /
      `.slice(0,10)`/`.slice(11)` (khối "hôm nay" + CSV nhiệt độ tủ) không còn khớp gì — thêm
      `dateVn`/`timeVn` chuẩn hoá qua `formatVnDate`/`formatVnDateTime` ngay sau fetch (cùng
      khuôn `inspections`/`kitchen` đã có sẵn trong file), đổi mọi chỗ dùng theo.
    - `medical-staff/inspections/page.tsx` + `medical-staff/inspections/[id]/page.tsx` —
      `FoodSample`→`FoodSampleDto`; guard `(f.collectedAt ?? "").startsWith(...)` (field optional
      trên DTO thật). Nhánh so khớp qua `f.meal === "<Trưa|Phụ> <dd/MM>"` giữ nguyên — đối chiếu
      `FoodSampleAssembler.toResponse()` xác nhận `meal` thật = `mealLabel(mealType) + " " +
      dayMonth(collectedAt)`, đúng khớp format này, không phải bug.
    - `medical-staff/incidents/[id]/page.tsx` — xoá comment cũ "MF08 vẫn mock", đổi
      `FoodSample`/`SampleLabRequest`→`FoodSampleDto`/`SampleLabRequestDto`. Bỏ hiển thị
      `s.labDestination`/`s.testCriteria`/`s.deliveredBy` trên TỪNG mẫu (`incidentSamples`/
      `retrievedSamples`) — field này KHÔNG tồn tại trên `FoodSampleResponse` (chỉ có trên
      `SampleLabRequestResponse`, đã hiện đủ ở khối "Phiếu thu hồi & gửi xét nghiệm" ngay dưới).
      ⚠️ Filter `incidentId=<mã>` ở 2 query `usePagedList` (`sample-lab-requests`/`samples` lọc
      theo ca) **VẪN LUÔN TRẢ RỖNG** — `FoodSampleSpec`/`SampleLabRequestSpec` so trực tiếp với
      cột UUID nội bộ, chưa có registry mã→UUID cho 2 Spec này (khác `resolveIncidentId()` dùng ở
      `register()`/`handOver()`, đã vá) — đã ghi rõ trong code, KHÔNG tự vá backend ở đây (ngoài
      phạm vi "nối FE", cần chủ MF11 quyết registry).
    - `service-caterers/dashboard/page.tsx` — `FoodSample`→`FoodSampleDto`, chỉ 1 chỗ dùng
      (đếm "Mẫu đang lưu"), không cần đổi logic.
  - Cập nhật `real-resources.ts` (đoạn comment MF08 viết lại từ "1/3, 2 blocker" thành tóm tắt
    "3/3 xong" + tóm bug đã vá, theo đúng khuôn MF03/MF06/MF07) và `contract-check.ts` (đoạn
    comment MF08 — không có dòng `MissingOnDto`, cùng lý do `students`/`inspections`: shape lệch
    đủ sâu). Đã thêm `"samples"` + `"sample-lab-requests"` vào `REAL_RESOURCES`.
  - `tsc --noEmit` sạch, `eslint` sạch (0 lỗi, 3 warning CÓ SẴN TỪ TRƯỚC ở file không đụng tới),
    `npm run audit` sạch (54 luật + 53 bảng seed, 0 vi phạm mới) — TOÀN DỰ ÁN, không chỉ file sửa.
  - **CHƯA verify qua curl/trình duyệt thật** (không có backend chạy sẵn trong phiên này, cùng
    giới hạn đã ghi từ MF03/MF07) — rủi ro cao nhất nằm ở luồng `handOver()` multipart
    (`authorityId`/`handoverType`/`testCriteria` điều kiện theo BR-212) và `retrievable` (cửa sổ
    24h + lọc `dishName` ở client) — nên test tay 2 luồng này trước khi coi MF08 "chắc chắn
    xong" ở môi trường có backend.
  - Việc kế tiếp theo kế hoạch mục 1: MF09 (`student-distributions`, `allergy-alerts`,
    `meal-history`, `special-meal-records`) — màn P0 phát suất ăn, cần thận trọng (ghi ở mục 1).

- **2026-08-05 — MF09 3/5 XONG (`allergy-alerts`, `meal-history`, `special-meal-records`) —
  `student-distributions` CHỜ NGƯỜI DÙNG QUYẾT trước khi code (P0, 3 lớp lỗi cùng lúc):**
  - **Đo lại phạm vi qua agent scoping trước khi code** (cùng cách đã làm ở MF06 đầu đợt) —
    xác nhận: `meal-records` (đã xong 03/08) là resource KHÁC `meal-history`; blocker cũ của
    `meal-history`/`special-meal-records` ("LỒNG dưới `students`, mà `students` còn mock") đã tự
    gỡ khi MF03 xong 5/5 (05/08) — không cần code gì thêm để mở khoá, chỉ cần rewrite 2 màn tiêu
    thụ. `allergy-alerts` xác nhận lại vẫn 0 màn tiêu thụ — bật ngay, no-op an toàn.
  - **`parent/meal-history/page.tsx` + `[id]/page.tsx` viết lại HOÀN TOÀN** (trước đó đọc mảng
    tĩnh `MEAL_HISTORY`, KHÔNG qua `usePagedList` nào — không phải lỗi remap, mà là CHƯA TỪNG
    nối): gọi `usePagedList`/`useDetail<MealHistoryEntryDto>(\`students/${code}/meal-history\`,
    …)` — resource string chứa path lồng, tự động đi thật vì `MOCK_ONLY_PATTERN` chỉ so tiền tố
    cấp 1 (`students`, đã real từ MF03), KHÔNG cần thêm `"meal-history"` vào `REAL_RESOURCES`.
    Bỏ cột "Năng lượng" ở list (không có trên `MealHistoryEntryResponse`/`ServingDishView`);
    ở màn chi tiết JOIN thêm `dishes` (đã nối thật, MF05) theo `dishId` (mã món, D1) để lấy lại
    `kcal`/`ingredients` — khôi phục được tính năng cũ chứ không phải bỏ. Bỏ field `servedAt`
    (giờ chính xác)/`specialMealNote` (không tồn tại trên DTO thật, chỉ có `date` không giờ).
    "Phản hồi" (BR-139, cửa sổ 24h) mất mốc giờ chính xác để tính lại đúng ở client — luôn hiện
    link, không tự đoán còn hạn hay không (server tự chặn khi hết hạn, MF10 `feedbacks` chưa nối
    nên chưa verify được, nhưng KHÔNG giả lập phép tính sai).
  - **`parent/special-meal-records/page.tsx` viết lại HOÀN TOÀN** (trước đó lọc
    `student-distributions` theo `isSpecial==="yes"` — cách này giờ có endpoint chuyên biệt
    đúng hơn): đổi sang `GET /students/{code}/special-meal-records`
    (`MealHistoryServiceImpl.specialMealRecords()`). Server trả TOÀN BỘ lịch sử nếu học sinh CÓ
    hồ sơ chế độ ăn đặc biệt (không riêng các lượt có thay thế thật) — lọc thêm ở client theo
    `dishes[].isSubstitute` để giữ đúng ý nghĩa "suất ăn đặc biệt" của màn, không phải giả định
    sai từ server. Bỏ khối cảnh báo dị ứng (`allergyWarning`/`specialDiet`/`dietTag`) —
    field này chỉ có trên `DistributionResponse` (vai Teacher), không có trên
    `MealHistoryEntryResponse` — không giả lập, ghi rõ lý do trong `Alert` hiển thị cho phụ huynh.
  - **2 type alias mới** trong `types.ts`: `MealHistoryEntryDto`, `ServingDishViewDto`.
  - Cập nhật `real-resources.ts` (đoạn MF09: `meal-history`/`special-meal-records`/
    `allergy-alerts` chuyển từ "chưa migrate" sang tóm tắt lý do đã gỡ blocker) và
    `contract-check.ts` (không có dòng `MissingOnDto` — 2 resource lồng không có mock-type
    tương đương, chưa từng tồn tại `MealHistory`/`SpecialMealRecord` interface). Đã thêm
    `"allergy-alerts"` vào `REAL_RESOURCES`.
  - `tsc --noEmit` sạch, `eslint` sạch (0 lỗi, 3 warning CÓ SẴN TỪ TRƯỚC), `npm run audit` sạch
    (54 luật + 53 bảng seed, 0 vi phạm mới) — TOÀN DỰ ÁN.
  - **CHƯA verify qua curl/trình duyệt thật** (không có backend chạy sẵn trong phiên này) — rủi
    ro cao nhất nằm ở `child.studentId` (Child Switcher, `PARENT_CHILDREN` seed TĨNH — ngoài
    phạm vi 4 resource MF09, chưa nối `/auth/me` thật) phải TRÙNG mã HS seed thật để 2 màn trên
    chạy đúng — đã xác nhận qua tiền lệ `teacher/meal-records` (cùng seed gốc) nhưng chưa tự
    kiểm lại ở phiên này.
  - **`student-distributions` — DỪNG LẠI, KHÔNG tự quyết** (đã báo người dùng, đang chờ trả lời
    trước khi code — màn P0 phát suất ăn, sai một quyết định là vỡ luồng chính):
    1. **Verb**: `teacher/distribution/page.tsx` xác nhận NHIỀU học sinh cùng lúc qua PUT từng
       dòng (`useUpdate`) — backend có sẵn `POST /confirm` (batch) đúng ý định UI, nhưng
       `ConfirmBatchRequest`/`serve()` cần `servedDishes` (BR-065 suất thay thế) mà UI hiện
       không có tham số này — cần thiết kế lại luồng xác nhận (server tự tính `servedDishes`
       hay client phải gửi lên? Chưa đọc `StudentDistributionServiceImpl.serve()`/
       `confirmBatch()` để biết).
    2. **Field**: cột "Suất ăn được phân công (BR-125)" (`assignedMeal`, preview món DỰ KIẾN
       cho phiên PENDING) không tồn tại trên DTO thật — muốn hiện lại phải tự JOIN `menus` theo
       (ngày, khối, bữa) ở client; hỏi: **giữ preview (thêm JOIN mới) hay bỏ cột (theo tiền lệ
       MF06/07 "không backend thì không giả lập")?**
    3. **Role**: `service-caterers/meal-service/page.tsx` tra `studentId` trên
       `DistributionCatererResponse` — DTO này CHỦ Ý không có PII học sinh (D11). Hỏi: **bỏ hẳn
       tính năng "suất phân công gần nhất theo học sinh" ở màn Service Caterers, hay đây cần
       một quyết định thiết kế khác (không phải phạm vi thuần FE-wiring)?**
  - Việc kế tiếp: chờ người dùng trả lời 3 điểm trên rồi mới code `student-distributions`; nếu
    người dùng muốn bỏ qua module này để làm MF10 trước, đổi thứ tự cũng được (không phụ thuộc).

- **2026-08-05 — MF09 HOÀN TẤT 5/5 (`student-distributions` XONG sau khi người dùng chốt hướng) —
  MF09 giờ TRỌN VẸN. Chi tiết đầy đủ (thiết kế + nhật ký triển khai) ở file riêng
  `04_Meal_Substitution_Portion_Pipeline_Design.md` — tóm tắt ở đây:**
  - Người dùng mô tả một luồng nghiệp vụ lớn hơn nhiều (menu theo nhóm chế độ ăn → đếm suất
    từng món cho bếp có tính món thay thế → tự điền kiểm thực → bếp giao suất theo lớp có
    check-off → giáo viên chỉ cần chia đúng món → phụ huynh xem thực đơn theo chế độ ăn của
    con). Đọc code xác nhận: thực đơn theo nhóm ăn + món thay thế theo dị ứng ĐÃ CÓ (MF05
    `audience`/`dietGroupId`, MF09 `doServe()`); đếm suất bếp theo món (kể cả thay thế) + giao
    nhận bếp→lớp theo món CHƯA CÓ (0 kết quả khớp trong `mf06`/`mf05`) — tính năng MỚI, xuyên 4
    module, không phải lỗi nối FE. Đã tách thành **Phase 1** (preview suất dự kiến, làm ngay,
    không đổi schema) và **Phase 2** (đếm suất bếp + giao nhận theo món + kiểm thực hiện món
    thay thế — TỒN ĐỌNG, cần chủ MF06/MF07 duyệt, chưa code).
  - **Phase 1 XONG**: backend thêm `GET /student-distributions/planned-dishes` (dùng lại đúng
    hàm thuần tách từ `doServe()`, không viết 2 bản luật thay món); `teacher/distribution/page.tsx`
    + `[id]/page.tsx` viết lại hoàn toàn (bỏ toàn bộ logic tính suất thay thế ở client viết cho
    mock — `personalizeBlock`/`dietGroupIdOf`/JOIN `menus`+`dishes` — thay bằng `serve()`/
    `planned-dishes` thật); `teacher/dashboard/page.tsx` đổi type; `service-caterers/meal-service
    /page.tsx` bỏ hẳn cột PII "suất phân công gần nhất" (D11 chủ ý, không phải thiếu field).
  - **3 bug backend không liên quan MF09, phát hiện + vá khi verify qua curl thật** (chi tiết ở
    `real-resources.ts` khối MF09 và file thiết kế riêng): (1) `mf04` app không boot được từ
    HEAD (2 bean `FaceRecognitionPort` xung đột + thiếu bean `RestTemplate`) — vá bằng
    `@Primary`/`@Profile("prod")`; (2) `ReferenceResolver.specialDietOfStudent()` native query
    sai tên cột (`created_at`→`requested_at`) — LUÔN 500 cho MỌI lời gọi `GET
    /student-distributions`, không phải ca hiếm; (3) `DistributionSpec` filter `date` gây lỗi
    kiểu dữ liệu Postgres — tách xử lý riêng, không sửa `SpecSupport` dùng chung.
  - Dựng lại backend local (`docker compose` + `mvn package` + `java -jar`) để verify qua curl
    thật (tài khoản `lethihuong.teacher.synthetic@example.test`) — xác nhận `planned-dishes`/
    `list`/`error-report` đúng; `confirm`/`serve()` chưa verify trọn happy path được vì seed cố
    định ở 06–07/07/2026 còn đồng hồ hệ thống thật ở 05/08/2026 (constraint `ck_dist_served_
    same_day` chặn đúng thiết kế) — đã xác nhận transaction rollback sạch khi bị chặn.
  - `tsc --noEmit`/`eslint`/`npm run audit` sạch toàn dự án sau mỗi bước. Đã thêm
    `"student-distributions"` vào `REAL_RESOURCES` — **MF09 giờ 5/5, TRỌN VẸN**.
  - Việc kế tiếp theo kế hoạch mục 1: MF10 (`feedbacks`, `notifications`). Phase 2 của thiết kế
    trên (đếm suất bếp + giao nhận theo món) để lại làm riêng, cần chủ MF06/MF07 duyệt trước.

- **2026-08-05 — MF10 HOÀN TẤT 4/4 (`feedbacks`, `notifications`) — MF10 giờ TRỌN VẸN, khép lại
  toàn bộ kế hoạch mục 1 (MF01→MF13, trừ Phase 2 tồn đọng của MF09 và report-schedules đã bỏ
  qua từ đầu):**
  - **`feedbacks` — viết lại TOÀN BỘ luồng chọn ngày→bữa→món** của `parent/feedback/meal`
    (scope=DISH) + `parent/feedback/quality` (scope=SESSION): đổi từ lọc mảng tĩnh
    `MEAL_HISTORY` theo TÊN sang `GET /feedbacks/eligible-meals?studentId=` (BR-139, trả mã
    `mealSessionId` thật). Phát hiện khi đọc code: `EligibleMealResponse.dishes` LUÔN RỖNG
    (giới hạn MVP ghi trong Javadoc `FeedbackServiceImpl` — MF09 chưa expose tra món theo
    phiên) — lấy danh sách món thật bằng cross-reference sang `GET /students/{code}/meal-history`
    (đã nối thật, MF09) theo `id` = `mealSessionId`. BR-140 (rating<3 bắt buộc tag) và BR-142
    (từ khoá bệnh lý → báo Y tế) đều tính ở SERVER — bỏ hẳn `useNotify()` giả lập ở 2 form.
    Ảnh đính kèm ở `feedback/quality` nối thật qua `POST /feedbacks/{code}/attachments`.
    `parent/feedback` (hub) + `school-admin/feedback` đổi type `Feedback`(mock)→
    `FeedbackOwnerListItemDto`/`FeedbackAnonymizedListItemDto` (D11 `oneOf` theo vai, cùng
    khuôn `student-distributions`).
  - **`notifications` — xoá hẳn lớp giả lập, không phải thêm field**: `NotificationController`
    không có `POST` (ADR-M10-4, thông báo là hệ quả server tự phát). Viết lại
    `notification-bell.tsx` — `GET /notifications` đã tự lọc theo vai + tính `read` Ở SERVER,
    bỏ filter client; đánh dấu đã đọc qua `PATCH /{code}/read`; taxonomy viết lại từ 4 giá trị
    cũ sang đúng 7 giá trị D6 thật. **Xoá hẳn `lib/notify.ts`** (`useNotify()`) và gỡ khỏi 8 màn
    từng gọi (2 màn `feedback/{meal,quality}` gỡ cùng lúc nối `feedbacks`; 6 màn còn lại
    `school-admin/{partnerships,menu-approval}[/id]`, `parent/{special-meal-requests,
    safety-concern}`, `medical-staff/special-meals`) — **xác nhận qua đọc code: không module
    nguồn nào (MF03/MF04/MF05/MF11) hiện gọi `NotificationPort.publish()` cho các hành động đó**
    (chỉ MF10 feedbacks/MF11 incidents/MF13 report-schedules publish thật) — nghĩa là `notify()`
    giả lập ở 6 màn này chưa từng tạo giá trị thật (`mock-server.ts` chỉ lưu in-memory, không
    cross-user/cross-session) — xoá đi không mất chức năng thật nào, chỉ ghi nợ rõ cho module
    chủ khi họ muốn có thông báo chủ động thật.
  - `tsc --noEmit`/`eslint`/`npm run audit` sạch toàn dự án. Đã thêm `"feedbacks"` +
    `"notifications"` vào `REAL_RESOURCES` — **MF10 giờ 4/4, TRỌN VẸN**.
  - **Verify qua curl thật**: `GET /feedbacks`, `GET /feedbacks/eligible-meals`,
    `GET /notifications` đều trả đúng cấu trúc (rỗng — dữ liệu seed cố định 06–07/07/2026 đã
    ngoài cửa sổ 24h so với đồng hồ hệ thống thật 05/08/2026, cùng giới hạn đã ghi ở MF09).
  - **Việc kế tiếp**: kế hoạch mục 1 của file này (MF01→MF13) nay đã XONG toàn bộ, trừ 2 việc
    CHỦ ĐỘNG để lại: report-schedules (MF13, không có màn FE tiêu thụ — bỏ qua từ đầu) và
    Phase 2 của `04_Meal_Substitution_Portion_Pipeline_Design.md` (đếm suất bếp + giao nhận
    theo món + kiểm thực hiện món thay thế — cần chủ MF06/MF07 duyệt trước khi code).

- **05/08/2026 (tiếp)** — Chủ MF06/MF07 đã duyệt Phase 2. Làm nốt mục (2)+(3) rồi (1)+(4) trong
  cùng phiên (xem `04_Meal_Substitution_Portion_Pipeline_Design.md` §5/§6 để biết chi tiết đầy
  đủ, tóm tắt ở đây):
  - Mục (2) MF06 xuất kho theo suất thật (kể cả món thay thế) — xong, kèm phát hiện + vá bug đơn
    vị g/ml↔kg/L có từ trước.
  - Mục (3) MF07 kiểm thực bước 2/3 hiện thêm dòng món thay thế — xong.
  - Khi làm (3), phát hiện bug an toàn thực phẩm nghiêm trọng ở lõi BR-065 (`doServe()` chưa bao
    giờ thực sự đổi món dù đã đánh cờ `isSubstitute`) — đã hỏi & được xác nhận sửa ngay trong
    phiên: chặn phát (`SRV-026`) khi dị ứng trùng món mà chưa có món thay thế cấu hình, thay vì
    âm thầm phát món gây dị ứng.
  - Mục (1)+(4) — domain mới `class_meal_deliveries` (bếp→lớp, khác `deliveries` NCC→bếp): 2 màn
    mới `service-caterers/class-deliveries` (bếp xác nhận giao) và `teacher/class-deliveries`
    (giáo viên xác nhận nhận, rồi mới sang `teacher/distribution` chia cho học sinh) — xong.
  - `tsc --noEmit`/`eslint`/`npm run audit` sạch toàn dự án. Thêm `"class-meal-deliveries"` vào
    `REAL_RESOURCES`. Verify qua curl thật (deliver/list-scoping/receive) với tài khoản synthetic
    thật, đã dọn dữ liệu test sau khi verify.
  - **Phase 2 nay chỉ còn mục (5)** (phụ huynh xem trước thực đơn/món thay thế TRƯỚC ngày ăn) —
    chưa có yêu cầu ưu tiên rõ, để lại làm sau nếu cần.
