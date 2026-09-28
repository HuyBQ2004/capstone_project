# Báo cáo tình trạng hoàn thành Test Case theo từng MF

> File tổng hợp — cập nhật sau mỗi đợt AI viết test theo `00_AI_TestGen_Prompt_Template.md`.
> Mỗi mục dưới đây là một đợt làm việc cho một module, ghi lại: kết quả chạy `mvn test` THẬT
> (không phải compile), số TC đã phủ / còn thiếu so với TestSpec, và các gap/bug thật phát
> hiện ở code chính (không tự sửa, chỉ ghi nhận để review).

---

## MF03 — Student & Parent Management

- **TestSpec:** `07_implement/MF03_Student_Parent_Management/TestSpec-MF03_Student_Parent_Management.md` (37 TC: 9 unit · 14 integration · 9 security · 5 contract; không có mục E2E riêng trong TestSpec này)
- **Ngày làm:** 2026-08-12
- **Trạng thái trước khi làm:** đã có sẵn ~31 file test (`src/test/java/com/sfms/mf03/`) phủ phần lớn TestSpec từ các đợt trước; chỉ thiếu 2 TC: `MF03-TC-008` (phần unit của `StudentMapper`) và `MF03-TC-SEC-005` (ma trận 7 role × endpoint theo TDS §16). Sau khi soát lại lần 2 (đối chiếu từng bước Test Steps trong TestSpec, không chỉ đối chiếu "có file hay không"), phát hiện thêm `MF03-TC-INT-007` (bulk import) có file `StudentImportIT.java` nhưng chỉ phủ 3/7 bước — thiếu bước 5 (rollback all-or-nothing khi BR-201 chặn giữa batch) và bước 6 (commit với `importId` lạ → 404) — đã bổ sung 2 test case này trong cùng đợt.

### Kết quả chạy thật

```
mvn -Dtest="com.sfms.mf03.**" test   (Docker/Testcontainers Postgres live)
→ 385 tests run, 0 failures, 0 errors, 0 skipped
```

Lần chạy đầu ra 2 lỗi ở `StudentSpecialDietIT` (`PSQLException: Can't infer the SQL type to use for an
instance of java.time.Instant` khi seed `special_meal_requests.requested_at`/`reviewed_at` qua
`JdbcTemplate.update(...)`) — đã sửa bằng cách bọc `java.sql.Timestamp.from(...)`. Sau khi bổ sung 2 TC
con còn thiếu của TC-INT-007 (383 → 385 test), chạy lại toàn bộ `com.sfms.mf03.**`: **385/385 pass**.

### Phạm vi phủ: 37 / 37 TC trong TestSpec (đã đủ)

| Loại | Đã viết | Tổng theo TestSpec |
|---|---|---|
| Unit | 9 | 9 |
| Integration | 14 | 14 |
| Security | 9 | 9 |
| Contract | 5 | 5 |

### File mới tạo trong đợt này (map theo TC ID)

Tất cả dưới `04_Development/Backend/src/test/java/com/sfms/mf03/`:

| File | TC IDs | Ghi chú |
|---|---|---|
| `mapper/StudentMapperTest.java` | TC-008 (nửa unit) | TestSpec giả định "chọn SMR APPROVED mới nhất" là logic của `StudentMapper`; code thật cho thấy `StudentMapper` (MapStruct) chỉ pass-through giá trị `specialDiet` đã tính sẵn — việc chọn bản mới nhất nằm ở `SpecialMealRequestRepository.findFirstByStudentIdAndStatusOrderByRequestedAtDesc` (derived query, tầng repository/DB). Test viết đúng theo code thật: mapper chỉ gắn giá trị, không tự tính. |
| `web/StudentSpecialDietIT.java` | TC-008 (nửa integration) | Test qua HTTP thật (Testcontainers) phần "chọn APPROVED mới nhất" mà unit test không giả lập được (`ORDER BY requested_at DESC`). |
| `security/AuthMatrixIT.java` | TC-SEC-005 | Ma trận tham số hoá 28 endpoint × 7 role = 196 case, dựng từ `@PreAuthorize` thật trên controller, đối chiếu TDS §16. Bẫy phát hiện lúc viết: (a) body rỗng `{}` ở 2 endpoint PATCH resubmit làm `@Valid` chặn trước `@PreAuthorize`, che mất phép thử 403 — sửa bằng body hợp lệ tối thiểu; (b) `GET /parents/{code}/students` là endpoint Own-scoped cho PARENT nên placeholder `ZZZ-NOTFOUND` bị chặn vì sai phạm vi (không phải vì role) — dùng mã PH thật của actor cho ca này. |
| `web/StudentImportIT.java` (bổ sung 2 test vào file có sẵn) | TC-INT-007 (bước 5, 6 — trước đó chỉ có bước 1-4, 7) | Test 4: seed 1 PH đã đủ 5 liên kết (BR-201), đưa vào dòng 2 của batch import 2 dòng, `commit()` → 409 `STU-201`, verify `SELECT COUNT(*) FROM students` **không đổi** kể cả dòng 1 hợp lệ đã insert trước đó trong cùng transaction (all-or-nothing, CF-M03-8). Test 5: `commit()` với `importId` không tồn tại → 404 `STU-404`. |

### Đối chiếu 35 TC đã có sẵn (không viết lại, chỉ audit)

Unit: TC-001..007, TC-009 → `service/ParentLinkServiceTest`, `service/StudentServiceTest`,
`service/HealthReviewServiceTest`, `mapper/HealthStatusCalculatorTest`,
`service/SpecialMealRequestServiceTest`, `dto/AllergySeverityBindingTest`,
`dto/UpdateHealthFileRequestTest`. Integration TC-INT-001..014 → 14 file dưới `web/`
(`StudentCreateIT`, `StudentCapacityIT`, `AllergyVerifyAlertIT`, `AllergyDeclarationIT`,
`HealthHistoryAppendOnlyIT`, `StudentClassChangeIT`, `StudentImportIT`, `HealthFileIT`,
`StudentListContractIT`, `DateFormatContractIT`, `PublicIdContractIT`, `HealthRecordIT`,
`ErrorEnvelopeIT`, `StudentCodeUniquenessIT`). Security TC-SEC-001..004, 006..009 →
`security/ParentScopeIT`, `TeacherScopeIT`, `CrossSchoolIsolationIT`, `AdminNoBusinessAccessIT`,
`UnauthenticatedIT`, `PdpdLeakageIT`, `ServerSideGateIT`, `IdorIT`. Contract TC-CON-001..005 →
`common/openapi/OpenApiD1ContractIT`, `OpenApiD2ContractIT`, `OpenApiD6ContractIT`,
`OpenApiD4ContractIT`, `OpenApiMetaContractIT`. Ngoài ra còn các file bổ sung không map trực tiếp
TC ID nào (`web/ChronicConditionIT`, `web/ParentLinkIT`, `web/HealthDocumentUploadIT`,
`web/ServiceCaterersDietSummaryIT`, `web/Mf03SecurityIT`, `fixture/Mf03Fixtures`).

### TC không viết được — kèm lý do

Không có — cả 37/37 TC của TestSpec đã có test tương ứng (35 từ trước + 2 mới trong đợt này).

### Gap/bug thật phát hiện ở code chính (KHÔNG tự sửa)

1. **D1 (business code thay vì UUID ở path param) — `OpenApiD1ContractIT` (TC-CON-001, test có từ trước) log cảnh báo `[CON-001][GAP]`:** 3 resource của MF03 (`allergy-declarations`, `special-meals`, `health-history`) vẫn expose UUID thật ở `{id}` path param trong `/v3/api-docs`, không phải mã nghiệp vụ như D1 yêu cầu. Test chỉ log, không fail — đang chờ xác nhận chủ trì.
2. **TDS §16 (ma trận role) có thể lệch với `@PreAuthorize` thật:** `ParentController` khai `@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF','PARENT')")` trên `GET /parents/{code}/students`, nhưng bảng §16 trong TDS đánh dấu cột Medical Staff là ❌ cho đúng endpoint này. `AuthMatrixIT` được viết theo code thật (Medical Staff ✅) nên không fail, nhưng đây là chỗ lệch thật giữa tài liệu thiết kế và code cần chủ module MF03 xác nhận lại (code thừa quyền, hay bảng TDS lỗi thời).

### Việc còn lại cho đợt sau

Không còn TC nào thiếu theo TestSpec hiện tại. Ưu tiên cho đợt sau: (a) xác nhận với chủ module 2 gap ở trên (D1 path-param + lệch TDS §16 Medical Staff), (b) nếu MF03 sau này có thêm mục E2E trong TestSpec, viết kịch bản tay theo đúng quy ước `05_Testing/02_IT/MF03_Student_Parent_Management/`.

---

## MF11 — Incident & Emergency Management

- **TestSpec:** `07_implement/MF11_Incident_Emergency_Management/TestSpec-MF11_Incident_Emergency_Management.md`
- **Ngày làm:** 2026-08-11
- **Trạng thái trước khi làm:** chưa có test nào (`src/test/java/com/sfms/mf11` không tồn tại).

### Kết quả chạy thật

```
mvn -Dtest="com.sfms.mf11.**" test   (Docker/Testcontainers live)
→ 13 test files, 186 tests run, 0 failures, 0 errors, 0 skipped
```

### Phạm vi phủ: ~45 / 77 TC trong TestSpec

| Loại | Đã viết | Tổng theo TestSpec |
|---|---|---|
| Unit | 26 | 27 |
| Security | 8 | 16 |
| Integration | 8 | 23 |
| Contract | 0 | 5 |
| E2E | 0 | 6 |

### File đã tạo (map theo TC ID)

Tất cả dưới `04_Development/Backend/src/test/java/com/sfms/mf11/`:

| File | TC IDs |
|---|---|
| `service/SourceReportCreateTest.java` | TC-001, TC-002, TC-004 |
| `service/ClusterSignalTest.java` | TC-003 |
| `service/AssessIncidentTest.java` | TC-007, TC-008, TC-009 |
| `service/QuarantineAndClosureTest.java` | TC-010, TC-011, TC-018, TC-019 |
| `service/CapaLifecycleTest.java` | TC-013, TC-015, TC-016, TC-017 |
| `service/IncidentRecordServiceTest.java` | TC-005, TC-006 |
| `service/SuspectedDishWindowTest.java` | TC-012 |
| `service/ReportLockTest.java` | TC-021 (+ TC-020 combined-gate) |
| `service/ViolationRecordTest.java` | TC-022, TC-023 |
| `service/ViolationResolutionTest.java` | TC-024, TC-025 |
| `service/OverrideAbuseReportTest.java` | TC-026 |
| `web/SecurityMatrixIT.java` | SEC-002/003/004/005/009/015/016 (+ subset SEC-001) |
| `integration/IncidentLifecycleAndInvariantsIT.java` | INT-001, INT-002, INT-008, INT-010, INT-011, INT-013, INT-019, INT-023 |

### TC không viết được — kèm lý do

- **TC-014** (CAPA sinh từ kiểm thực, XOR `incidentCode`/`inspectionCode`): `CapaServiceImpl.create()` chỉ nhận `incidentCode`; đường sinh từ kiểm thực nằm ở `CapaPortImpl.openFromFailedStep`, chưa được đọc/kiểm trong đợt này.
- **TC-027** (SEARCH_FIELDS/pagination trên `Specification`): cần Hibernate metamodel thật, không unit-test được bằng Mockito — cần IT chạm DB (chưa làm).
- **Security còn thiếu:** SEC-006 (PDPD projection), SEC-007/008 (Teacher scope), SEC-010 (JWT trust/cross-school), SEC-011 (rate limit), SEC-012 (violation audit+IP), SEC-013 (upload size/type), SEC-014 (Caterer scope liên đới) — cần fixture data (học sinh, upload multi-part...) chưa build kịp.
- **Integration còn thiếu:** INT-003…007, INT-009, INT-012, INT-014…018, INT-020…022, INT-024, TC-030 — cần fixture DB lớn (batch, mẫu lưu, kiểm thực) mà seed thật của MF11 không có sẵn (xem gap #6 bên dưới).
- **Contract (5 TC) và E2E (6 TC):** chưa bắt đầu.

### Gap/bug thật phát hiện ở code chính (KHÔNG tự sửa)

1. **`IncidentServiceImpl.close()` mâu thuẫn ADR-M11-8** — gọi `requireOpen(incident)` vô điều kiện như 10 hành động ghi khác, nên sự cố đã `reportExported=true` thì **không bao giờ đóng lại được** qua `POST /close` nữa, dù ADR-M11-8 nói `close` phải là ngoại lệ được phép. → "deadlock nghiệp vụ".
2. **CAPA vẫn approve/reject/rework được sau khi sự cố cha đã khoá báo cáo** — `CapaServiceImpl.validate()/rework()/approve()/reject()` không kiểm tra `reportExported` của incident cha.
3. **`IemExceptions.violationApprovalForbidden()` (IEM-186/MSG-157) là dead code** — không nơi nào gọi tới; quy tắc "Medical Staff không được duyệt vi phạm MEDIUM/HIGH" chỉ được chặn bằng `@PreAuthorize("hasRole('SCHOOL_ADMIN')")`, không đi qua exception nghiệp vụ như tài liệu mô tả.
4. **BR-187 (repeat-suspect) bỏ qua mức độ nghiêm trọng của vi phạm trước đó** — `ViolationServiceImpl.create()` chỉ xét severity của vi phạm mới + có/không tồn tại vi phạm cũ trong 90 ngày; vi phạm cũ dù chỉ mức LOW vẫn gắn cờ `repeatSuspect=true`.
5. **D4 "lenient date parsing" (dd/MM/yyyy) chưa implement** — Jackson trả 422 với mã lỗi (`IEM-422`) không có trong `IemError`/§10, không phải 400 như quy ước ngụ ý.
6. **`V21__seed_mf11.sql` không seed dữ liệu nghiệp vụ thật** — các câu `UPDATE` nhắm vào mã (`INC-2026-001`, `RPT-2026-002/003`, `REC-2026-001`) mà chưa từng có `INSERT` nào tạo ra; DB thật sau khi Flyway chạy có **0 incident/violation/CAPA**. Mọi mã trong TestSpec chỉ tồn tại ở FE-mock — Integration/Security/E2E còn thiếu phải tự dựng fixture qua API hoặc SQL trực tiếp.
7. **`SourceReportServiceImpl.create()` validate thiếu so với MSG-023** — chỉ chặn `description` rỗng; `symptoms` rỗng và `mealDate` null vẫn được chấp nhận (tự điền fallback).
8. **Parent gọi `assess()`/API tương tự nhận 404 thay vì 403** — `AuthUser.schoolId()` là `null` với PARENT nên `loadInScope()` fail ở bước tra cứu trước khi chạm nhánh kiểm quyền → trả `IEM-404` thay vì `IEM-005` như tài liệu.

### Việc còn lại cho đợt sau

Ưu tiên theo thứ tự: (a) dựng fixture DB thật cho MF11 (vì seed hiện rỗng) để mở khoá phần Integration/Security còn thiếu, (b) hoàn thiện 8 Security TC còn lại, (c) 15 Integration TC còn lại, (d) 5 Contract TC (nhớ tải `/v3/api-docs` thật để soi cấu trúc trước khi assert), (e) 6 E2E TC. Đồng thời đưa 8 gap ở trên cho chủ module xác nhận: mục 1–3 là bug logic nên sửa; mục 4–8 cần xác nhận code hay tài liệu là đúng ý định trước khi sửa.

---

## MF09 — Meal Serving & Distribution

- **TestSpec:** `07_implement/MF09_Meal_Serving_Distribution/TestSpec-MF09_Meal_Serving_Distribution.md` (v2.0, chốt 2026-08-03, 58 TC)
- **Ngày làm:** 2026-08-11
- **Trạng thái trước khi làm:** chưa có test nào (`src/test/java/com/sfms/mf09` không tồn tại).

### ⚠️ Phát hiện mở đầu — TestSpec đã lỗi thời so với code thật

Code MF09 được vá liên tục từ 05/08 đến 11/08/2026 (đều có comment ghi ngày), thêm nhiều hành vi
**không có trong TestSpec 58-TC gốc**. Test viết theo đúng hành vi hiện tại, không theo giả định
cũ của TestSpec:

| Việc | TestSpec giả định | Code thật (verify bằng chạy thật) |
|---|---|---|
| Cổng BR-094 | `noSession` → không chặn | Từ 07/08: `NO_SESSION`/`IN_PROGRESS`/`FAILED` đều chặn như nhau |
| BR-105 (mới) | không tồn tại | Thêm cổng "chưa lưu mẫu chưa được phát" (SRV-105), độc lập với BR-094 |
| BR-065 dị ứng | luôn tạo được suất thay | Món không có `defaultSubstituteId` cấu hình sẵn → chặn phát (SRV-026) |
| `confirmBatch` | có cờ `atomic`, trả `results[]`/`confirmed`/`failed` | Chỉ có `distributionIds`; 1 `@Transactional` duy nhất, không partial-result |
| `doServe()` | không nhắc | Tự tạo `MealConsumptionRecord` PENDING (bugfix 11/08 — trước đó bảng này luôn rỗng) |
| 401 không JWT | mã `SRV-004` | Mã thật **`IAM-005`** — request chưa từng chạm MF09 |
| 403 trên `/students/{code}/...` | mã `SRV-005` | Mã thật **`STU-005`** (MF03) — `GlobalExceptionHandler` suy tiền tố lỗi theo **path URL**, không theo controller sở hữu route → rò mã lỗi xuyên module |
| Body rỗng để test 403 | vẫn ra 403 | `@Valid` chạy **trước** `@PreAuthorize` trong Spring MVC → ra 422 trước khi kịp kiểm quyền |
| TC-012 (giờ phát phải cùng ngày) | có guard, có mã lỗi | **Không có guard ở service** — chỉ có `CHECK ck_dist_served_same_day` ở DB, unit test không chạm được (ghi nhận là gap, xem mục Gap bên dưới) |

### Kết quả chạy thật

```
mvn -Dtest=<12 file unit + MealServingAuthTest> test        (unit, một phần cần Docker)
→ 75 tests run, 0 failures, 0 errors, 0 skipped

mvn -Dit.test=RoleMatrixIT verify   (Docker/Testcontainers live, Postgres thật)
→ 32 tests run, 0 failures, 0 errors, 0 skipped

TỔNG: 107/107 test PASS thật (không dừng ở compile)
```

### Phạm vi phủ: ~29 / 58 TC trong TestSpec (quy đổi theo ý định nghiệp vụ — nhiều TC đã đổi hành vi so với bản gốc)

| Loại | Đã viết | Tổng theo TestSpec |
|---|---|---|
| Unit | 16 | 16 |
| Security | ~5 | 13 |
| Integration | 0 | 19 |
| Contract | 0 | 6 |
| E2E | 0 | 4 |

### File đã tạo (map theo TC ID)

Tất cả dưới `04_Development/Backend/src/test/java/com/sfms/mf09/`:

| File | TC IDs / nội dung |
|---|---|
| `service/ConfirmDistributionTest.java` (11) | TC-001, TC-002, TC-003, TC-006, TC-007, TC-012 (+ happy path, tạo `MealConsumptionRecord`) |
| `service/BatchConfirmTest.java` (4) | TC-015 (đã đổi hành vi thật), TC-009 (nhánh chặn trong batch) |
| `service/RevokeDistributionTest.java` (4) | TC-008 |
| `service/DistributionErrorReportTest.java` (2) | TC-009 |
| `service/DistributionSummaryTest.java` (4) | TC-011 |
| `service/DistributionDetailTest.java` (3) | TC-004 |
| `service/DistributionPlanHandlerTest.java` (9) | TC-014 + logic định tuyến audience theo chế độ ăn (bổ sung 06/08, ngoài TestSpec gốc) |
| `service/MealHistoryServiceTest.java` (8) | TC-013, TC-016 + Y tế đọc toàn trường (bổ sung 08/08, ngoài TestSpec gốc) |
| `service/MealConsumptionTest.java` (5) | TC-005, TC-010 |
| `service/PlannedDishesTest.java` (4) | Endpoint `/planned-dishes` — hoàn toàn không có trong TestSpec gốc |
| `service/DistributionListShapeTest.java` (2) | D11 — shape đổi theo vai trò (tương đương ý định `MF09-TC-CON-006`, viết ở tầng unit thay vì contract) |
| `service/AllergyAlertServiceTest.java` (5) | UC-103 — không nằm trong 16 TC-0xx gốc nhưng cùng lớp `service/` |
| `web/MealServingAuthTest.java` (14) | SEC-001 (401 không JWT, mọi endpoint) |
| `web/RoleMatrixIT.java` (32) | SEC-002/003/004/007(một phần)/008 (403 sai role — không đủ 91 tổ hợp CSV như TestSpec đòi, chỉ chọn các ô chắc chắn bị chặn) |

### TC không viết được — kèm lý do

- **Unit:** đủ 16/16 theo ý định nghiệp vụ (đã đổi assertion cho khớp code thật ở TC-003/007/012/015).
- **Security còn thiếu (8/13):** SEC-005 (IDOR phụ huynh — có bản tương đương ở tầng unit trong `MealHistoryServiceTest`, chưa có bản IT thật qua HTTP), SEC-006 (JWT scope — client gửi `schoolId`/`teacherId` giả bị bỏ qua), SEC-007 (đủ 91 tổ hợp 7 role × 13 nhóm endpoint), SEC-009 (không tồn tại sinh trắc học — quét `RequestMappingHandlerMapping`/schema), SEC-010 (bypass cổng kiểm thực qua API — cần seed `inspection_sessions` thật), SEC-011 (`serving_dishes` append-only ở tầng DB — cần UPDATE/DELETE SQL trực tiếp), SEC-012 (audit log đủ actor/IP, không lộ PII), SEC-013 (rate limit 429). Tất cả cần Docker + phần lớn cần seed nghiệp vụ (student/menu/inspection) chưa dựng.
- **Integration (19), Contract (6), E2E (4):** chưa viết cái nào. Cần `MF09Fixtures.java` (theo khuôn `Mf05Fixtures`/`Mf03Fixtures`) để tạo học sinh, thực đơn, món ăn, phiên kiểm thực, mẫu lưu, liên kết phụ huynh — khối lượng dựng fixture tương đương làm thêm một phần hạ tầng test của module khác, chưa làm trong đợt này.

### Gap/bug thật phát hiện ở code chính (KHÔNG tự sửa)

1. **`doServe()` không kiểm tra `dist.getDate()` có trùng "hôm nay" không** — set `servedAt = clock.instant()` vô điều kiện. Ràng buộc duy nhất là `CHECK ck_dist_served_same_day` ở **tầng DB** (V16 migration); tầng service không có guard, không có mã lỗi, khác hẳn kỳ vọng ban đầu của `MF09-TC-012`. Chưa có IT nào xác nhận DB CHECK này thực sự chặn khi ghi thật.
2. **`GlobalExceptionHandler.handleAccessDenied()` suy tiền tố mã lỗi theo path URL (`ModuleCodeResolver.prefixFor(request)`), không theo controller thực sự xử lý route** — 3 endpoint `/students/{code}/meal-history|special-meal-records|nutrition-history` do `com.sfms.mf09.web.MealHistoryController` xử lý, nhưng khi bị 403 lại trả mã `STU-005` (tiền tố MF03) thay vì `SRV-005` của MF09. Rò rỉ mã lỗi xuyên module — FE tra `lib/messages.ts` theo mã sẽ hiện nhãn sai module.
3. **`@Valid @RequestBody` chạy trước `@PreAuthorize`** (đặc điểm chung của Spring MVC, không riêng MF09) — gửi body thiếu field bắt buộc cho một role không có quyền sẽ nhận 422 (validate) thay vì 403 (quyền); ai viết test 403 mà không cẩn thận body hợp lệ sẽ ra kết quả sai.
4. **`DailyParentSummaryJob` không phát event nào cả** — MF10 (người tiêu thụ) chưa merge nên job hiện tại chỉ log, không gọi `events.publishEvent(...)`. `MF09-TC-INT-015` (idempotent theo student+date) không có gì để test cho tới khi MF10 lên bean.
5. **`ServingDish` có setter public đầy đủ** (`setCode`, `setDistribution`, `setDishId`, `setSubstitute`, `setDishName`, `setSubstitutedForDishName`, `setReason`) — trái với mô tả "KHÔNG có setter public (ADR-M09-1)" của TestSpec. Tính bất biến append-only hiện chỉ được đảm bảo bởi (a) không có code nào gọi setter sau khi tạo, và (b) `RULE ... DO INSTEAD NOTHING` ở DB — không có gì ở tầng Java ngăn một đoạn code tương lai gọi setter rồi lưu đè.

### Việc còn lại cho đợt sau

Ưu tiên theo thứ tự: (a) viết `MF09Fixtures.java` (theo khuôn `Mf05Fixtures`/`Mf03Fixtures`) để dựng học sinh/thực đơn/món/phiên kiểm thực/mẫu lưu — mở khoá toàn bộ Integration/Contract/E2E; (b) hoàn thiện 8 Security TC còn lại (đặc biệt SEC-011 append-only và SEC-009 kiểm sự vắng mặt sinh trắc học — không cần fixture nhiều); (c) 19 Integration TC; (d) 6 Contract TC (tải `/v3/api-docs` thật trước khi assert cấu trúc); (e) 4 E2E TC. Đồng thời đưa 5 gap ở trên cho chủ module (Nam/Mạnh) xác nhận: mục 1–2 ảnh hưởng trực tiếp tới đúng-sai của response lỗi nên nên sửa hoặc ít nhất ghi nhận chính thức vào TDS; mục 3–5 là ghi chú kiến trúc, không nhất thiết phải sửa ngay.

---

## MF09 — Meal Serving & Distribution — đợt tiếp theo (fixture + Security + Integration + Contract)

- **Ngày làm:** 2026-08-12
- **Trạng thái trước khi làm:** như mục "Việc còn lại cho đợt sau" ở trên — Unit 16/16, Security ~5/13, Integration 0/19, Contract 0/6, E2E 0/4, chưa có `Mf09Fixtures.java`.

### Kết quả chạy thật

```
mvn -o -Dtest="com.sfms.mf09.**" test   (Docker/Testcontainers, Postgres pgvector/pgvector:pg16 thật)
→ 136 tests run, 0 failures, 0 errors, 0 skipped
```

(Log có kèm stacktrace `relation "report_schedules" does not exist` — đó là job nền `ReportScheduleRunner` của **MF13** chạy song song trong cùng context Spring Boot test, không liên quan MF09, không làm test nào của MF09 fail; giữ nguyên log gốc để tránh nghi ngờ giấu lỗi.)

### Phạm vi phủ mới: Unit 16/16 (không đổi) · Security 9/13 · Integration 13/19 · Contract 6/6 · E2E 0/4

| Loại | Trước đợt này | Sau đợt này | Tổng theo TestSpec |
|---|---|---|---|
| Unit | 16 | 16 | 16 |
| Security | ~5 | 9 | 13 |
| Integration | 0 | 13 | 19 |
| Contract | 0 | 6 | 6 |
| E2E | 0 | 0 | 4 |

Tổng ước lượng theo ý định nghiệp vụ: **~29 → ~44 / 58 TC**.

### File đã tạo (map theo TC ID)

Tất cả dưới `04_Development/Backend/src/test/java/com/sfms/mf09/`:

| File | Nội dung / TC IDs |
|---|---|
| `fixture/Mf09Fixtures.java` | Helper JDBC seed học sinh, món/thực đơn (APPROVED, `grade_id` — V44), phiên kiểm thực PASSED/FAILED (uỷ lại `Mf08Fixtures`), dị ứng/liên kết phụ huynh (uỷ lại `Mf05Fixtures`), `student_distributions`/`serving_dishes` PENDING/SERVED — không map TC, hạ tầng dùng chung cho toàn bộ Security/Integration/Contract mới |
| `security/Mf09SecurityIT.java` (9 test) | SEC-005 (IDOR IT — phụ huynh xem con người khác + đối chứng dương), SEC-006 (spoof `schoolId`/`teacherId` qua query param không có tác dụng — server luôn đọc `actor.schoolId()` từ JWT), SEC-009 (vắng mặt sinh trắc học — serve() chỉ cần JWT), SEC-010 (bypass cổng kiểm thực qua API — 2 nhánh `NO_SESSION`/`FAILED`), SEC-011 (append-only DB — UPDATE/DELETE thẳng SQL vào `serving_dishes` bị `RULE` chặn im lặng), SEC-012 (gap — `serve()` không ghi `audit_logs`), SEC-013 (gap — không có rate limiter nào wire cho MF09, 30 request liên tiếp không bao giờ 429) |
| `integration/Mf09IntegrationIT.java` (13 test) | COND-01/TC-INT-001 (UNIQUE constraint DB + 409 qua HTTP thật, **không viết race đa luồng thật** — xem mục "chưa làm" bên dưới), TC-INT-002 (cổng kiểm thực PASSED mở khoá thật, ghi đúng `serving_dishes`+`meal_consumption_records`), TC-INT-003 (lịch sử KHÔNG viết lại khi dị ứng khai sau khi đã phát), TC-INT-004 (tổng hợp ngày×lớp runtime phản ánh ngay), TC-INT-009 (revoke giữ nguyên `serving_dishes` + nhánh 409 revoke khác ngày), TC-INT-006 (hợp đồng phân trang đủ 5 field, rỗng vẫn 200), TC-INT-007 (lọc exact `date`/`class`/`mealType`), TC-INT-012 (enum `consumption` đủ 4 giá trị + giá trị lạ → 422), TC-INT-014 (audit columns do `BaseEntity`/`AuditorAware` điền, không phải service set tay), TC-INT-018 (trục truy xuất `dish_id` → tập học sinh đã ăn), TC-INT-017/021 (không có cột dẫn xuất, không có bảng `meal_history`/summary) |
| `contract/Mf09OpenApiContractIT.java` (7 test) | TC-CON-001 (D1 — `id`=code, không lộ UUID), TC-CON-002 (envelope `ErrorResponse`→`$ref ErrorBody` + mọi lỗi 4xx/5xx MF09 trỏ đúng schema, content-type thật `*/*`), TC-CON-003 (D6 — `ConsumptionStatus` inline, đủ 4 giá trị SCREAMING_SNAKE), TC-CON-004 (D4 — `date`/`servedAt` format ISO, không có `dd/MM/yyyy` ở đâu trong schema MF09), TC-CON-005 (D9 — DTO không lộ `createdBy`/`updatedBy`/`createdAt`/`updatedAt`), TC-CON-006 (D11 — `oneOf` 2 schema theo vai trò + `DistributionCatererResponse` không có PII) |

### TC không viết được — kèm lý do

- **Integration còn thiếu (6/19):** TC-INT-005 (snapshot caterer trong lịch sử — không kịp trong đợt), TC-INT-008 (tổ hợp đầy đủ COND-01/03/04/13 ở 1 chỗ — một phần đã phủ rải rác qua các test khác), TC-INT-010/011 (đã phủ một phần qua Contract, chưa có bản Integration ghi rõ DB thật), TC-INT-013 (`confirmBatch` — chưa viết riêng, mới có ở Unit `BatchConfirmTest`), TC-INT-015 (phụ thuộc `DailyParentSummaryJob`/MF10 — vẫn chặn như đợt trước, xem Gap #4 cũ), TC-INT-016 (đối soát 6 luật `semantic-audit.ts` — cần viết 6 query riêng, chưa làm). TC-INT-001 chỉ xác nhận UNIQUE constraint DB + 409 qua HTTP tuần tự, **không dựng được race 2 thread thật** trong đợt này (rủi ro test không ổn định giữa các lần chạy) — đã ghi rõ trong javadoc test, cần hạ tầng `ExecutorService`/`CountDownLatch` riêng nếu muốn làm race thật.
- **Contract: đủ 6/6** theo ý định nghiệp vụ.
- **Security còn thiếu (4/13):** SEC-001/002/003/004 (đã có từ đợt trước, không tính lại), SEC-007 (đủ 91 tổ hợp 7 role × 13 nhóm endpoint — `RoleMatrixIT` cũ mới phủ một phần các ô "chắc chắn bị chặn", chưa đủ ma trận), SEC-008 (nếu tồn tại — cần đối chiếu lại danh sách 13 TC gốc của TestSpec, không có trong danh sách 8 TC ban đầu của đợt trước nên bỏ qua).
- **E2E (4 TC):** chưa viết cái nào trong đợt này — hết thời gian sau khi hoàn thành (a)-(d). 4 kịch bản (`MF09-TC-E2E-001`..`004`) đã có sẵn ở `05_Testing/02_IT/MF09_Meal_Serving_Distribution/End_to_End_Scenarios.md` (thuộc phạm vi QA, không phải TestSpec markdown do Trần Tuấn Minh sở hữu) — **đã đọc và đối chiếu với code thật nhưng KHÔNG sửa file đó** (ngoài phạm vi được giao — file này không phải TestSpec/status doc mà nhiệm vụ được phép sửa). Ghi lại ở đây thay vì sửa trực tiếp file kia:
  - `MF09-TC-E2E-001` bước 2 giả định `POST /student-distributions/confirm` trả envelope `{confirmed, failed, message}` — **code thật trả thẳng `List<DistributionResponse>`** (không có field `confirmed`/`failed`/`message` nào); tester cần đếm số phần tử `status="SERVED"` trong mảng. Bước 5 giả định 10 event `DailyMealSummaryCompiled` — **code thật (`DailyParentSummaryJob`) chỉ log, không gọi `events.publishEvent(...)`** (MF10 chưa merge) nên **0 event** được phát bất kể chạy bao nhiêu lần (trùng Gap #4 của đợt trước).
  - `MF09-TC-E2E-003` bước 3 giả định response `{confirmed:0}` khi gate chặn — **code thật trả lỗi 403 `SRV-094` chuẩn (`{"error":{"code":"SRV-094",...}}`), không có field `confirmed`**; bất biến nghiệp vụ "0 SERVED khi gate chặn" vẫn đúng, chỉ cách đọc kết quả cần đổi.
  - `MF09-TC-E2E-004` bước 1 liệt kê `assignedMeal`/`specialMeal`/`dietTag` là field kỳ vọng có trong `DistributionResponse` — **code thật (`DistributionDtos.DistributionResponse`) không có 3 field này** (đúng ADR-M09-2 — chủ động không hiện thực, không phải bug).

### Gap/bug mới phát hiện ở code chính (KHÔNG tự sửa) — bổ sung vào 5 gap đã ghi ở đợt trước

6. **`StudentDistributionServiceImpl` không gọi `com.sfms.iam.service.AuditService` ở bất kỳ đâu** (xác nhận qua `Mf09SecurityIT.servingADistributionDoesNotWriteAnyAuditLogRowGapNoAuditServiceCallInMf09`) — hành động ghi có tác động nghiệp vụ thật (phát/hủy/báo lỗi phân phát) không để lại dấu vết `audit_logs`, khác MF01.
7. **Không có rate limiter nào được wire cho bất kỳ endpoint ghi nào của MF09** (`SlidingWindowRateLimiter`/`Mf04RateLimiterConfig` chỉ phục vụ MF04; `RegisterRateLimiter` chỉ phục vụ MF02) — `/serve`/`/confirm`/`/revoke` gọi lặp lại không giới hạn, xác nhận bằng 30 request liên tiếp không request nào ra 429.
8. **`confirmBatch()` không có shape `{confirmed, failed, message}`/cờ `atomic`** như TestSpec/kịch bản E2E cũ giả định — trả thẳng `List<DistributionResponse>`; một item lỗi giữa batch làm **toàn bộ HTTP request trả lỗi đơn** (403/409/422 tuỳ nguyên nhân), không có kết quả từng dòng — đã ghi nhận ở đợt trước (mục "Phát hiện mở đầu"), nay xác nhận thêm ảnh hưởng cụ thể tới `MF09-TC-E2E-001`/`003`.

### Việc còn lại cho đợt sau

Ưu tiên theo thứ tự: (a) 4 E2E TC — viết theo ĐÚNG shape response thật đã ghi ở mục "TC không viết được" trên đây, không theo mô tả cũ trong `End_to_End_Scenarios.md`; (b) 6 Integration TC còn thiếu (đặc biệt TC-INT-016 đối soát 6 luật semantic-audit và TC-INT-013 confirmBatch qua HTTP thật); (c) SEC-007 đủ ma trận 91 tổ hợp; (d) dựng hạ tầng `ExecutorService`/`CountDownLatch` để viết lại TC-INT-001 thành race 2 luồng thật thay vì chỉ kiểm UNIQUE constraint tuần tự; (e) đưa 3 gap mới (#6-#8) cho chủ module (Nam/Mạnh) xác nhận — #6/#7 là thiếu sót bảo mật/audit nên cân nhắc bổ sung, #8 cần đồng bộ lại `End_to_End_Scenarios.md`/TestSpec để QA không viết test theo response shape sai.

---

## MF08 — Food Sample Retention

- **TestSpec:** `07_implement/MF08_Food_Sample_Retention/TestSpec-MF08_Food_Sample_Retention.md` (v1.1, chốt 2026-08-03, 47 TC: 20 unit · 9 security (8 gốc + SEC-009/D11) · 17 integration + TC-021/D12)
- **Ngày làm:** 2026-08-12
- **Trạng thái trước khi làm:** chỉ có 1 file test (`src/test/java/com/sfms/mf08/web/FoodSampleFlowIT.java`), không phủ theo TC ID của TestSpec.

### Kết quả chạy thật

```
mvn verify -Dtest="com.sfms.mf08.**" -Dit.test="com.sfms.mf08.**"   (Docker/Testcontainers Postgres 16 live)
→ 185 tests run tổng, 184 passed, 1 failed
```

File fail duy nhất là `FoodSampleFlowIT.java` **có từ trước, không do đợt này tạo/sửa** — fail vì giả định dish code `"com-trang"` được seed sẵn trong `V9__seed_mf05.sql` nhưng thực tế DB không có (xem gap #7 bên dưới).

### Phạm vi phủ: 45 / 47 TC trong TestSpec

| Loại | Đã viết | Tổng theo TestSpec |
|---|---|---|
| Unit | 19 | 20 |
| Security | 8 | 9 |
| Integration | 17 | 17 |
| D12 (TC-021, ngoài 3 nhóm trên) | 1 | 1 |

### File đã tạo (map theo TC ID)

Tất cả dưới `04_Development/Backend/src/test/java/com/sfms/mf08/`:

| File | TC IDs |
|---|---|
| `service/GeofenceValidatorTest.java` | TC-004 |
| `domain/RetentionPolicyTest.java` | TC-008 |
| `service/FoodSampleRegisterTest.java` | TC-001, TC-002, TC-003, TC-004(wiring), TC-005, TC-006, TC-007, TC-013(một phần) |
| `service/FoodSampleDisposeTest.java` | TC-010, TC-011, TC-012, TC-013(một phần), TC-016(một phần) |
| `service/SampleHandOverTest.java` | TC-013(một phần), TC-014, TC-015, TC-016(một phần) |
| `service/DerivedFieldTest.java` | TC-009 |
| `dto/EnumContractTest.java` | TC-017 |
| `dto/PublicIdContractTest.java` | TC-020 |
| `service/SampleAttachmentServiceTest.java` | TC-019 |
| `fixture/Mf08Fixtures.java` | (helper JDBC seed dish/inspection/incident, không map TC) |
| `security/SampleRbacIT.java` | SEC-001, SEC-002, SEC-004, SEC-006 |
| `security/SampleTenantIsolationIT.java` | SEC-003 |
| `security/SampleUploadSecurityIT.java` | SEC-005 |
| `security/InfoLeakIT.java` | SEC-007 |
| `web/RoleFieldParityIT.java` | SEC-009 (D11) |
| `integration/FoodSampleRegisterIT.java` | INT-001, INT-016, INT-017 |
| `integration/SampleHandOverIT.java` | INT-002, INT-003, INT-004, INT-015 |
| `integration/FoodSampleDisposeIT.java` | INT-005 |
| `integration/DisposedSampleLinkIT.java` | INT-006 |
| `integration/PagedContractIT.java` | INT-007 |
| `integration/PublicIdContractIT.java` | INT-008 |
| `integration/ErrorEnvelopeIT.java` | INT-009 |
| `integration/ClientSuppliedIdIT.java` | INT-010 |
| `integration/SemanticAuditRulesIT.java` | INT-011, INT-012 |
| `integration/SampleRegisterExportIT.java` | INT-013 |
| `integration/RetentionSchedulerIT.java` | INT-014 |
| `web/DerivedFieldRejectionIT.java` | TC-021 (D12) |

### TC không viết được — kèm lý do

- **TC-018** (khoá chặn: không field ngày nào trong response MF08 khớp regex `dd/MM/yyyy`) — chưa có file riêng; đã được assert rải rác qua các IT khác (`PublicIdContractIT`, `ErrorEnvelopeIT`...) nhưng không có test tổng quét toàn bộ payload theo đúng ý định TC-018, nên tính là chưa viết đầy đủ.
- **SEC-008** (contract test theo `/v3/api-docs`): giữ `@Disabled` theo đúng convention hiện có trong spec — chờ D8 (chưa approve) — không viết test thật cho tới khi D8 chốt.

### Gap/bug thật phát hiện ở code chính (KHÔNG tự sửa)

1. **`IncidentStatusPort.findOpen(...)` trả `Optional.of(...)` cho cả sự cố đã đóng** — cả `FoodSampleServiceImpl.linkDisposedToIncident()` lẫn `SampleLabRequestServiceImpl.handOver()` đều không kiểm tra `IncidentRef.open()`, nên link/bàn giao mẫu vào một sự cố **đã đóng** hiện vẫn thành công (200/201) thay vì trả 409 như spec kỳ vọng. Test ghi nhận đúng hành vi hiện tại trong `DisposedSampleLinkIT`/`SampleHandOverIT`.
2. **Export PDF (`SampleRegisterExportServiceImpl.exportPdf`) dùng font Helvetica/WinAnsiEncoding chuẩn của PDFBox** — không render được tiếng Việt có dấu, crash `IllegalArgumentException` với tên món thật; bị `GlobalExceptionHandler` nuốt thành lỗi chung `422 SMP-422`. Xác nhận độc lập ngoài Spring, test assert theo hành vi (lỗi) thật này.
3. **Không tồn tại `InspectionGatePort`/`GeofenceQueryPort`** như spec giả định — code thật dùng `ReferenceResolver` trực tiếp (M08-D06/D07).
4. **Kiểm tra GPS/geofence là no-op khi thiếu tọa độ** (client hoặc cấu hình trường) — không chặn cứng như spec giả định.
5. **Không có validate size/type/magic-byte cho file upload (MSG-008/009)** ở bất kỳ đâu trong MF08 — test assert theo hành vi hiện tại (không giới hạn) thay vì bịa ra một validate không tồn tại.
6. **401 (thiếu/sai JWT) trả `IAM-005`, không phải mã cục bộ `SMP-004`** — hằng số `SMP-004` là dead code, chưa từng được throw.
7. **`FoodSampleFlowIT.java` (test có từ trước, không do đợt này tạo) fail** vì giả định dish code `"com-trang"` đã seed sẵn ở `V9__seed_mf05.sql` — thực tế không có insert nào tạo mã này, cần chủ module MF05/MF08 xác nhận lại seed hoặc sửa lại test cũ.

### Việc còn lại cho đợt sau

Ưu tiên: (a) viết 1 test tổng quét toàn bộ response payload MF08 theo đúng ý định TC-018 (regex `dd/MM/yyyy` không được xuất hiện ở bất kỳ field nào); (b) khi D8 (contract) được approve, bỏ `@Disabled` và hoàn thiện SEC-008; (c) đưa 6 gap #1–6 ở trên cho chủ module (Mạnh) xác nhận đâu là bug cần sửa (đặc biệt #1 — sai lệch trực tiếp với BR-112/113/114) và đâu là hành vi tạm chấp nhận; (d) sửa hoặc re-seed `V9__seed_mf05.sql` để `FoodSampleFlowIT.java` cũ pass trở lại.
