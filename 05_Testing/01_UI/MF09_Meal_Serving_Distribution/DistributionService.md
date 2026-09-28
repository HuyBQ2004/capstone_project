# Unit Test Case — DistributionService (MF09 Meal Serving & Distribution)

---

## Function: `confirmServed`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SRV-FUNC-001` | | Function Name | `DistributionService.confirmServed` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `210` | | Lack of test cases | `0` |
| Test requirement | Enforce BR-130 (block duplicate serve, no silent no-op), ADR-M09-1/C3 (never write `SERVED` with empty `serving_dishes`), BR-120 (only homeroom teacher of the class may confirm), BR-094/MF07 (inspection gate blocks or warns), and `ck_dist_served_same_day` (servedAt must be same calendar day as `date`). Covers `MF09-TC-001`, `MF09-TC-002`, `MF09-TC-006`, `MF09-TC-007`, `MF09-TC-012`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `8` | `2` | `4` | `2` | `8` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | |
| Precondition | Fixture `FX-002`: `DSTU-0706A-002` already `SERVED`, `servedAt=2026-07-06T03:47:00Z`, 4 rows in `serving_dishes` | O | | | | | | | |
| | Fixture `FX-010`: `MenuQueryPort.publishedBlock(2026-07-08, "Grade 3", LUNCH, SCH-001)` returns `Optional.empty()` | | O | | | | | | |
| | Fixture `FX-015`: `isHomeroomTeacher("3A", U-0004)=true`, `isHomeroomTeacher("3A", U-0009)=false` | | | O | O | O | O | O | O |
| | Fixture `FX-011(a)`: gate not blocked, not noSession | | | | O | | | O | O |
| | Fixture `FX-011(b)`: gate `blocked=true`, `failedSteps=["Bước 2 — trong chế biến"]`, `sessionId="INSP-20260707-L"` | | | | | O | | | |
| | Fixture `FX-011(c)`: gate `noSession=true` | | | | | | O | | |
| | Clock `FX-024` fixed `2026-07-24T03:00:00Z` (Asia/Ho_Chi_Minh) | | | | | | | O | O |
| Actor | JWT `FX-017` — homeroom teacher of class 3A (`teacher3A`) | O | O | | O | O | O | O | O |
| | JWT `FX-018` — homeroom teacher of class 3B (`teacher3B`, cross-class) | | | O | | | | | |
| Input | `confirmServed("DSTU-0706A-002", teacher3A)` — already SERVED | O | | | | | | | |
| | `confirmServed(code, teacher3A)` — date `2026-07-08` (no published menu) | | O | | | | | | |
| | `confirmServed(code, teacher3B)` — record belongs to class 3A | | | O | | | | | |
| | `confirmServed(code, teacher3A)` — gate case (a)/(b)/(c) on `2026-07-06`/`DSTU-0707A-001`/no-session date | | | | O | O | O | | |
| | `confirmServed(code, teacher3A)` — record of the current clock day | | | | | | | O | |
| | `confirmServed(code, teacher3A)` — record dated `2026-07-06` while clock is `2026-07-24` | | | | | | | | O |
| **Confirm** | | | | | | | | | |
| Return | `200`, `status=SERVED`, `serving_dishes ≥ 1`, message = MSG-045 | | | | O | | O | O | |
| | `200` with `warning` flag (no inspection session found) — **not** blocked | | | | | | O | | |
| Exception | `ConflictException SRV-021` (HTTP 409) — BR-130 duplicate serve | O | | | | | | | |
| | `ValidationException SRV-022` (HTTP 422) — ADR-M09-1/C3, no published menu | | O | | | | | | |
| | `ForbiddenException SRV-005` (HTTP 403) — BR-120, not homeroom teacher, **not** 404 (ADR-M09-6) | | | O | | | | | |
| | `ForbiddenException SRV-094` (HTTP 403) — BR-094 (MF07), inspection step failed | | | | | O | | | |
| | Exception raised for a record dated other than the current day (oracle not yet assigned — see note) | | | | | | | | O |
| Log message | `MSG-021`: Duplicate meal distribution: Student has already scanned for this session. | O | | | | | | | |
| | `MSG-130`: No approved or published menu found for [Meal] on [Date] for grade [Grade]. Meal distribution cannot be recorded. | | O | | | | | | |
| | `MSG-005`: Access denied: You do not have permission to access this resource. | | | O | | | | | |
| | `MSG-129`: Meal cannot be served: inspection step [Step Name] did not pass. | | | | | O | | | |
| Side-effect | `servedDishRepository.saveAll()` never invoked; state unchanged (`PENDING`/`servedAt` unchanged) | O | O | O | | O | | | |
| | Event `DuplicateDistributionBlocked` published | O | | | | | | | |
| | `gateFor()` invoked before `MenuQueryPort`; guard order `ClassroomPort` → `MenuQueryPort` → `InspectionGatePort` | | | O | O | | | | |
| | `servedAt`/`servedBy`/`status` all set atomically, same VN calendar day as `date` (`ck_dist_served_same_day`, `TRANG_THAI_VS_GIO_PHAT`) | | | | | | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | A | A | A | N | A | B | N | B |
| Passed/Failed | Passed | Passed | Passed | Passed | Passed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | | | |

> Note (UTCID08): TestSpec `MF09-TC-012` flags this branch "thiếu oracle mã lỗi" — TDS §10 has no error code for "serve a record dated other than today." Test asserts only that an exception is raised; do not invent a code.

---

## Function: `personalize`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SRV-FUNC-002` | | Function Name | `DistributionService.personalize(menuBlock, student)` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `65` | | Lack of test cases | `0` |
| Test requirement | BR-065: substitute meal plan mandatory for a student with a **verified** allergy/special diet, must state the replaced dish + reason; BR-031: only verified allergy profiles (status `VERIFIED`) trigger a substitution, `NEEDS_INFO`/`PENDING` must not. Covers `MF09-TC-003`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `2` | `1` | `1` | `0` | `2` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | Fixture `FX-009`: `HS-3A-001` allergen `["Tôm","Cua"]` status `VERIFIED` | O | |
| | Fixture `FX-009`: `HS-3A-005` allergen `["Sữa bò"]` status `NEEDS_INFO` (unverified) | | O |
| | Fixture `FX-010`: `MENU-2026-27` contains dish "Canh bí đỏ nấu tôm" | O | O |
| Input | `personalize(menuBlock, "HS-3A-001")` | O | |
| | `personalize(menuBlock, "HS-3A-005")` | | O |
| **Confirm** | | | |
| Return | `ServingDish{isSubstitute=true, dishName="Canh bí đỏ thịt băm", substitutedForDishName="Canh bí đỏ nấu tôm", reason` contains `"Tôm"}`; other 3 dishes `isSubstitute=false` | O | |
| | All 4 dishes `isSubstitute=false` — unverified allergy profile does **not** trigger substitution (BR-031) | | O |
| Invariant | `∀ d : d.isSubstitute ⇒ d.substitutedForDishName != null ∧ d.reason != null` (matches `ck_sdish_substitute`); every `ServingDish.dishId != null` | O | O |
| Log message | — (pure logic, no exception) | | |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | N | A |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | | |

---

## Function: `getDetail`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SRV-FUNC-003` | | Function Name | `DistributionService.getDetail` (projection) |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `55` | | Lack of test cases | `0` |
| Test requirement | BR-123: allergy warning is **derived** (not stored), auto-triggered only when the assigned menu actually touches a verified allergen; a vegetarian menu that does not touch the allergen must yield `allergyWarning=null`/`isSpecial="no"` while `allergens` remains informational. Covers `MF09-TC-004`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `2` | `1` | `0` | `1` | `2` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | Fixture `FX-001`: `DSTU-TD-3A-001`, vegetarian menu `MENU-TD-3` (no overlap with allergen) | O | |
| | Fixture `FX-003`: `DSTU-0706A-001`, menu `MENU-2026-27` containing "Canh bí đỏ nấu tôm" (overlaps allergen) | | O |
| | Fixture `FX-009`: `HS-3A-001` allergen `["Tôm","Cua"]` verified | O | O |
| | Clock `FX-024` fixed | O | O |
| Input | `getDetail("DSTU-TD-3A-001", teacher3A)` | O | |
| | `getDetail("DSTU-0706A-001", teacher3A)` | | O |
| **Confirm** | | | |
| Return | `allergyWarning == null`, `isSpecial == "no"`, `allergens == ["Tôm","Cua"]` still displayed (informational only) | O | |
| | `allergyWarning` contains `"Tôm"`, `isSpecial == "yes"`, `specialMeal` names the replaced dish | | O |
| Side-effect | No query against a `allergy_warning`/`is_special` column — repository not invoked for those columns (fields are derived, ADR-M09-2) | O | O |
| Log message | — (pure projection, no exception) | | |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | N | B |
| Passed/Failed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | | |

---

## Function: `revokeServed`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SRV-FUNC-004` | | Function Name | `DistributionService.revokeServed` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `70` | | Lack of test cases | `0` |
| Test requirement | ADR-M09-3: revoke only allowed same-day by the homeroom teacher, `serving_dishes` is append-only and must never be touched by revoke (ADR-M09-1). Covers `MF09-TC-008`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `2` | `1` | `1` | `0` | `2` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | Fixture `FX-002`: `DSTU-0706A-002` `SERVED` on `2026-07-06`, 4 rows in `serving_dishes` | O | O |
| Clock | `FX-024` set to `2026-07-06` (same day as serve) | O | |
| | `FX-024` set to `2026-07-07` (day after serve) | | O |
| Input | `revokeServed("DSTU-0706A-002", teacher3A)` | O | O |
| **Confirm** | | | |
| Return | `status → PENDING`, `servedAt = null`, `servedBy = null`; **4 rows in `serving_dishes` unchanged** | O | |
| Exception | `ConflictException SRV-024` (HTTP 409) — record kept as `SERVED` | | O |
| Log message | — | | |
| | `MSG-132`: Cannot revoke a distribution that was confirmed on a previous day. | | O |
| Side-effect | Event `MealDistributionRevoked` published with actor | O | |
| | No `delete`/`update` call reaches `ServingDishRepository` in either branch | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | N | A |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | | |

---

## Function: `reportError` / `confirmBatch`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SRV-FUNC-005` | | Function Name | `DistributionService.reportError` / `DistributionService.confirmBatch` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `120` | | Lack of test cases | `0` |
| Test requirement | UC-016 AF1: an unresolved `DistributionErrorReport` must block class-level completion (`confirmBatch`); `confirm` batch semantics — `atomic=true` (default) rolls back the entire batch on any error, `atomic=false` returns per-row results. Covers `MF09-TC-009`, `MF09-TC-015`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `4` | `2` | `1` | `1` | `4` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | 10 `PENDING` sessions for class 3A today | O | O | | |
| | `DistributionErrorReport{resolved=false}` attached to `DSTU-TD-3A-003` | O | | | |
| | Same report marked `resolved=true` | | O | | |
| | 3 codes: `DSTU-0706A-001` (`PENDING`), `DSTU-0706A-003` (`PENDING`), `DSTU-0706A-002` (already `SERVED`) | | | O | O |
| Input | `reportError("DSTU-TD-3A-003", {reason:"..."}, teacher3A)` then `confirmBatch({ids:10 codes, atomic:true}, teacher3A)` | O | | | |
| | `confirmBatch({ids:10 codes, atomic:true}, teacher3A)` after report resolved | | O | | |
| | `confirmBatch({ids:[3 codes], atomic:true})` | | | O | |
| | `confirmBatch({ids:[3 codes], atomic:false})` | | | | O |
| **Confirm** | | | | | |
| Return | Report created, event `DistributionErrorReported` sent to Service Caterers; record stays `PENDING` | O | | | |
| | `confirmBatch` succeeds, `confirmed=10` | | O | | |
| | `atomic=true`: exception thrown, **0/3** records change state (full rollback) | | | O | |
| | `atomic=false`: HTTP 200, `confirmed=2`, `failed=1`; `results[]` — 2 rows `SERVED`+ISO `servedAt`, 1 row stays `PENDING` with `error.code="SRV-021"` | | | | O |
| Exception | `ConflictException SRV-023` (HTTP 409) — `confirmBatch` blocked while report unresolved | | | | |
| Log message | `MSG-131`: Distribution completion is blocked: an unresolved distribution error was reported. | (see UTCID03 note) | | | |
| | `MSG-045`: Meal distribution list confirmed for class [Class Name]. — for the successful batch, `[Class Name]="3A"` | | O | | O |
| | `MSG-021` on the failing row of the `atomic=false` batch | | | | O |
| Default | `atomic` defaults to `true` when omitted | | | | O |

> Note UTCID03 label: this row is technically the second `confirmBatch` call inside `MF09-TC-009` (still-unresolved report) — it raises `SRV-023`/`MSG-131`; the "0/3" wording above is reused as a cross-check with `MF09-TC-015`'s atomic-rollback semantics.

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | A | N | B | N |
| Passed/Failed | Passed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

> Implementation note: TestSpec `MF09-TC-009` flags "thiếu oracle" for the rule of *who* is allowed to mark a `DistributionErrorReport` resolved — UC-016 AF1 only says the Teacher verifies the correction, not whether Service Caterers may close it. No assertion is made on this point; flagged for Huy to clarify.

---

## Function: `summary`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SRV-FUNC-006` | | Function Name | `DistributionService.summary` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `45` | | Lack of test cases | `0` |
| Test requirement | CF-M09-10: day × class summary `status` formula must be computed **at runtime** (`distributed === 0 ? "PENDING" : distributed < planned ? "IN_PROGRESS" : "COMPLETED"`), no frozen summary table. Covers `MF09-TC-011`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `4` | `0` | `0` | `4` | `4` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | Class 3A `planned=10` | O | O | O | O |
| Input | `distributed = 0` | O | | | |
| | `distributed = 1` | | O | | |
| | `distributed = 9` (planned − 1) | | | O | |
| | `distributed = 10` (planned) | | | | O |
| **Confirm** | | | | | |
| Return | `status = "PENDING"` | O | | | |
| | `status = "IN_PROGRESS"` | | O | O | |
| | `status = "COMPLETED"` | | | | O |
| | `specialMeals` counts records whose **derived** `isSpecial == "yes"` (not read from a stored column) | O | O | O | O |
| | `id` follows mock template `DST-<yyyymmdd>-<class>` (e.g. `DST-20260706-3A`) | O | O | O | O |
| Log message | — (pure computation, no exception) | | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | B | B | B | B |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |
