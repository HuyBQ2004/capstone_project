# Unit Test Cases — MF08 Food Sample Retention
## Scope: `FoodSampleService.register()` validation gate chain (BR-105, BR-107, BR-108, BR-109, BR-110)

> Source module: `com.sfms.mf08.service.FoodSampleService`
> Traceability: `TestSpec-MF08_Food_Sample_Retention.md` §4 — `MF08-TC-003`…`MF08-TC-007`
> Registration must pass gates **in order**: inspection gate (BR-105) → geofence (BR-109) → amount/container (BR-203, see `SampleAmountValidator_And_ContainerRules.md`) → evidence photo (BR-107) → server-recorded identity/GPS/timestamp (BR-108) → fridge temperature soft warning (BR-110).

---

## Function: `FoodSampleService.register()` — Inspection gate (BR-105)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-003` | | Function Name | Sample registration blocked unless inspection session PASSED 3/3 |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | BR-105 mandates that food samples may only be registered after the meal batch has passed all three inspection steps. `InspectionGatePort.isAllStepsPassed(inspectionId, schoolId)` (owned by MF07) is queried before any other validation, using `schoolId` from the JWT — not from the request body. Source: `MF08-TC-003`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 3 | 1 | 2 | 0 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | Geofence, amount, container, photo all otherwise valid | O | O | O |
| `inspectionId` | `INS-2026-0706-S` (session FAILED at step 2) | O | | |
| | `null` (missing) | | O | |
| | `INS-2026-0706-T` (PASSED 3/3) | | | O |
| **Confirm** | | | | |
| Return | Passes gate, continues to next validation | | | O |
| Exception | `BusinessException` code `SMP-105`, HTTP 409 | O | | |
| Exception | `ValidationException` (field required), HTTP 422 | | O | |
| Port call | `inspectionGatePort.isAllStepsPassed(id, schoolIdFromJwt)` invoked with schoolId from JWT, not from body | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | A | N |
| Passed/Failed | | | |
| Executed Date | | | |
| Defect ID | | | |

**Note:** This test is expected to run 🔴 RED until the frontend actually sends `inspectionId` in the register payload (currently reset on submit) — that is a legitimate RED, not a broken test. Allowing an unverified batch to be retained would produce a compliance record that contradicts the three-step inspection log (BR-105, QĐ 1246/QĐ-BYT).

---

## Function: `FoodSampleService.register()` — Geofence check (BR-109)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-004` | | Function Name | Sample must be registered inside the designated kitchen/storage area |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | BR-109 requires GPS verification against the school's kitchen/storage geofence via `GeofenceQueryPort` (owned by MF02). Rejected registrations must not fall back to MSG-015 (a different BR's message). Source: `MF08-TC-004`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 2 | 1 | 1 | 0 | 2 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | Inspection gate already passed (BR-105) | O | O |
| `gpsLat, gpsLng` | `(21.0500, 105.9000)` — outside geofence | O | |
| | `(21.0146, 105.7639)` — inside kitchen/storage geofence | | O |
| **Confirm** | | | |
| Return | Passes gate, continues to amount/container validation | | O |
| Exception | `ValidationException` code `SMP-109`, HTTP 422 | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | A | N |
| Passed/Failed | | |
| Executed Date | | |
| Defect ID | | |

**Note:** In the current mock UI, the location check is only a checkbox with no real GPS submission — this test asserts the server-side gate that closes that gap. MSG-015 belongs to a different business rule (MF06/MF07) and must never be reused here.

---

## Function: `FoodSampleService.register()` — Evidence photo required (BR-107)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-005` | | Function Name | Registration requires at least one evidence photo, stored via `StorageService` |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | BR-107 requires staff to photograph retained samples and their labels as operational evidence. The upload must go through the shared `common/storage/StorageService` (D10), not a direct filesystem write. Source: `MF08-TC-005`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 2 | 1 | 1 | 0 | 2 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | Inspection + geofence gates passed | O | O |
| `photos[]` | `[]` (empty) | O | |
| | `[mau-com-trang.jpg]` (1 valid photo) | | O |
| **Confirm** | | | |
| Exception | `ValidationException` code `SMP-107`, HTTP 422 | O | |
| Repository call | `repository.save()` never invoked | O | |
| Attachment record | Exactly 1 `sample_attachments` row, `kind = EVIDENCE_PHOTO` | | O |
| Storage call | `storageService.store(startsWith("samples/"), file)` invoked | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | A | N |
| Passed/Failed | | |
| Executed Date | | |
| Defect ID | | |

**Note:** The mock frontend currently uses a `photosAttached: boolean` checkbox with no real file upload — a compliance record could claim "photo attached" while no photo exists. This test locks the real evidentiary requirement.

---

## Function: `FoodSampleService.register()` — Server-recorded identity, GPS and timestamp (BR-108)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-006` | | Function Name | GPS/device/timestamp/collector identity are recorded by the server, not trusted from the client |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | BR-108 requires the system to automatically capture GPS, device info, and timestamp during registration; BR-114 requires `collectedBy` to be read from the authenticated JWT. CWE-807 — Reliance on Untrusted Inputs in a Security Decision. Source: `MF08-TC-006`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 1 | 1 | 0 | 0 | 1 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Precondition | All prior gates passed | O |
| Request body | Contains forged `deviceInfo`, `createdAt=2020-01-01T00:00:00Z`, `collectedBy="Nguyễn Văn X"` | O |
| JWT actor | `fullName = "Lê Thị Mai"` | O |
| **Confirm** | | |
| Return | `collectedBy` saved = `"Lê Thị Mai"` (from JWT), not the forged body value | O |
| | `deviceInfo` saved = value the server reads from `User-Agent`, not the client string | O |
| | `createdAt` saved = `Instant.now(FIXED_CLOCK)`, not the forged 2020 value | O |
| | `collectedAt` (user-entered collection time) preserved as distinct from `createdAt` (server-recorded ingestion time) | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | A |
| Passed/Failed | |
| Executed Date | |
| Defect ID | |

**Note:** `collectedAt` (when the sample was physically taken, user-entered, Mẫu số 5 column 8) and `createdAt` (when the server recorded it) are two different fields and must never be merged. If the client could set `collectedBy`, anyone calling the API directly could sign someone else's name into the legal record — voiding column 11 of Mẫu số 5.

---

## Function: `FridgeTempRange` — Storage temperature soft warning (BR-110)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-007` | | Function Name | Fridge temperature outside 2–8°C is a soft warning, never blocks registration |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | BR-110 (QĐ 1246/QĐ-BYT Điều 8.1) requires the retention fridge to stay within 2–8°C. Per the 2026-07-11 amendment, the automatic alert mechanism was removed — 2–8°C is now a manual-inspection criterion only, so a value outside range must set `tempOutOfRange = true` and emit MSG-020 as a **warning**, but must never block the save. Source: `MF08-TC-007`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 5 | 3 | 0 | 2 | 5 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Precondition | All prior gates passed | O | O | O | O | O |
| `fridgeTemp` | `1.9` (below lower boundary) | O | | | | |
| | `2.0` (lower boundary) | | O | | | |
| | `4.2` (normal) | | | O | | |
| | `8.0` (upper boundary) | | | | O | |
| | `8.1` (above upper boundary) | | | | | O |
| **Confirm** | | | | | | |
| Return | `status = RETAINED` (registration always succeeds regardless of temperature) | O | O | O | O | O |
| `tempOutOfRange` | `true` + `FoodSampleTemperatureOutOfRange` event (MSG-020) emitted once | O | | | | O |
| | `false`, no warning event | | O | O | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | B | B | N | B | B |
| Passed/Failed | | | | | |
| Executed Date | | | | | |
| Defect ID | | | | | |

**Note:** Two failure modes to watch for: (1) blocking registration on an out-of-range reading — this would violate BR-105's mandatory-retention rule and cause a bigger compliance gap than a warm fridge; (2) reintroducing a 30-minute polling job for temperature — that mechanism was explicitly removed from BR-110 and there is no sensor to feed it (see `MF08-TC-INT-014` for the scheduler-side assertion that no such job exists). Comparisons must use `BigDecimal.compareTo`, not `double`, since `4.2` is not exactly representable in binary and the `8.0` boundary is easy to miss with float comparison.
