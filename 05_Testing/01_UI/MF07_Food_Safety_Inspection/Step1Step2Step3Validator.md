# Unit Test Case — Step1/Step2/Step3/EvidenceValidator (MF07 Food Safety Inspection & Monitoring)

---

## Function: `Step2Validator.validate()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-003` | | Function Name | `Step2Validator.validate` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 40 | | Lack of test cases | 0 |
| Test requirement | Meal core temperature must reach `>= 75°C` (BR-090, HACCP threshold). Below threshold must record `step2=FAILED` (non-conformance, BR-095) and raise `INSP-017`/HTTP 422 with `MSG-017`; the boundary uses `>=`, not `>`. (source: MF07-TC-003, MF07-TC-004) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 1 | 1 | 2 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | Step 2 payload (`FX-002`/`FX-006`) | O | O | O | O |
| `coreTemp` | `68` (well below threshold) | O | | | |
| | `74` (one below on-point) | | O | | |
| | `75` (on-point, must PASS) | | | O | |
| | `76` (above threshold) | | | | O |
| **Confirm** | | | | | |
| Return | `step2 = PASSED`, no exception | | | O | O |
| Exception | `LowCoreTempException`, `error.code == INSP-017`, HTTP 422 | O | O | | |
| Return (recorded) | `step2 = FAILED` recorded (non-conformance, BR-095); `overall = FAILED`; event `InspectionStepFailed` emitted | O | O | | |
| Log message | `MSG-017`: "Cooking temperature too low: Meal core temperature must reach 75°C." | O | O | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | A | B | B | N |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

---

## Function: `Step3Validator.validate()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-004` | | Function Name | `Step3Validator.validate` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 35 | | Lack of test cases | 0 |
| Test requirement | Food held for serving must stay `>= 60°C` (BR-091). Below threshold → `step3=FAILED` + `INSP-091`/HTTP 422; boundary is `>=`. (source: MF07-TC-005) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 1 | 0 | 2 | 3 |

**Note — Blocked test suite:** `@Disabled("Chờ CF-24/L19 — Minh bổ sung ô nhập holdingTemp ở Bước 3")`. Mock `Step3Row` has no `holdingTemp` field yet, so BR-091 has no data source. CHECK `ck_insp_holding_temp` must stay disabled until the FE field exists; the whole UTCID set below is **Blocked — pending CF-24/L19**, kept for traceability per the spec's immutable-history policy.

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | Step 3 payload with `holdingTemp` supplied | O | O | O |
| `holdingTemp` | `59` (one below on-point) | O | | |
| | `60` (on-point, must PASS) | | O | |
| | `61` (above threshold) | | | O |
| **Confirm** | | | | |
| Return | `step3 = PASSED`, no exception | | O | O |
| Exception | `INSP-091`, HTTP 422, `step3 = FAILED` | O | | |
| Log message | Warning: "BR-091 không kiểm được — thiếu holdingTemp (CF-24)" when `holdingTemp` is absent (current mock state, not blocking) | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | B | B | N |
| Passed/Failed | Untested (Blocked — pending CF-24) | Untested (Blocked — pending CF-24) | Untested (Blocked — pending CF-24) |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | CF-24/L19 | CF-24/L19 | CF-24/L19 |

---

## Function: `Step1Validator.validate()` (+ `InventoryQueryPort`)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-005` | | Function Name | `Step1Validator.validate` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 38 | | Lack of test cases | 0 |
| Test requirement | System must automatically check the expiry date of raw-material batches used in Step 1 (BR-092); client-sent flags must never be trusted. Expired batches raise `INSP-092`/HTTP 422 and record `step1=FAILED`. (source: MF07-TC-006) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 1 | 0 | 2 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | Stub `InventoryQueryPort` returns batch `BATCH-MP-8820` (FX-008); backend queries automatically, no client flag sent | O | O | O |
| `expiryDate` | `session.date − 1` (expired) | O | | |
| | `expiryDate = session.date` (expires same day — assumption pending Huy's confirmation) | | O | |
| | `session.date + 1` (still valid) | | | O |
| **Confirm** | | | | |
| Return | Batch treated as valid; step proceeds | | O | O |
| Exception | `ExpiredBatchException`, `error.code == INSP-092`, HTTP 422, message contains `BATCH-MP-8820` | O | | |
| Return (recorded) | `step1 = FAILED` recorded (same rationale as TC-003) | O | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | B | B | N |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

Note: the `expiryDate = session.date` boundary (UTCID02) assumes "still valid on the exact expiry day"; this assumption is **not yet confirmed by Huy** — revise the test if the business decision differs.

---

## Function: `EvidenceValidator.validate()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-006` | | Function Name | `EvidenceValidator.validate` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 55 | | Lack of test cases | 0 |
| Test requirement | Every "Failed" row must carry photo evidence, server-side GPS and timestamp (BR-083); client-supplied GPS/timestamp must not be trusted. Missing evidence raises `INSP-083`/HTTP 422; the 4 `fail_*` columns must be persisted (L20/CF-25). (source: MF07-TC-007) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 2 | 1 | 0 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | Row `sensoryPass=false`, `photoCount=0` (FX-007) | O | | |
| | Row `sensoryPass=false` resubmitted with photo + `failReason` | | O | |
| | Row `sensoryPass=true` (a "Passed" row), no photo | | | O |
| **Confirm** | | | | |
| Return | Step accepted; `inspection_evidences` row stored with `url` + `sha256` (ADR-M07-04), no blob stored; GPS/timestamp recorded server-side (`GeoLocationPort` fake, FX-026); 4 columns `fail_step`/`fail_reason`/`fail_severity`/`fail_witness` persisted (L20) | | O | O |
| Exception | `MissingEvidenceException`, `error.code == INSP-083`, HTTP 422, message references only the offending row | O | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | N | N |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

Note: `fail_severity`/`fail_witness` assertions are **pending Huy's confirmation** as official fields (L20); if UI treats them as temporary, split that assertion into a separate `@Disabled` case and keep only the photo/reason assertions active.
