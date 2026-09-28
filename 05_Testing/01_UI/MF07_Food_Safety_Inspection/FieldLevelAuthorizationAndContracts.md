# Unit Test Case — Field-Level Authorization & API Contracts (MF07 Food Safety Inspection & Monitoring)

---

## Function: Field-level authorization on `PUT /inspections/{code}` — `medicalReview*`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-022` | | Function Name | `MedicalReviewFieldAuthorization` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 35 | | Lack of test cases | 0 |
| Test requirement | `medicalReviewResult`/`medicalReviewReason`/`medicalReviewBy`/`medicalReviewAt` are the 4 fields ONLY Medical Staff may write on `inspections` (TDS §16 field-level table; no BR/UC source yet — CF-10). A request mixing these fields with inspection-step fields must be rejected wholesale, not partially written. (source: MF07-TC-022) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 3 | 0 | 0 | 1 | 2 | 0 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Actor role | Medical Staff | O | O | |
| | Caterer | | | O |
| Request body | Only the 4 `medicalReview*` fields | O | | O |
| | 4 `medicalReview*` fields + `step2`/`coreTemp` | | O | |
| **Confirm** | | | | |
| Return | HTTP 200; 4 fields written | O | | |
| Exception | HTTP 403; no field written at all, including the otherwise-valid 4 fields in the same request | | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | A | A |
| Passed/Failed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

---

## Function: `POST /kitchen-inspections/{code}/confirm` — `confirmStatus` field authorization

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-023` | | Function Name | `KitchenConfirmStatusAuthorization` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 32 | | Lack of test cases | 0 |
| Test requirement | `confirmStatus` may only be written by the Service Caterers unit being inspected (TDS §16, TDS §5.2 `AWAITING_CATERERS_CONFIRM → CONFIRMED`; no BR/UC source yet — CF-10). Caterer-side confirmation screen not yet coded (§9.2 #26). (source: MF07-TC-023) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 1 | 3 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Actor | Caterer of the inspected unit | O | | | O |
| | Caterer of a different unit | | O | | |
| | Medical Staff (record creator) | | | O | |
| Target transition | `AWAITING_CATERERS_CONFIRM → CONFIRMED` | O | O | O | |
| | `CONFIRMED → AWAITING_CATERERS_CONFIRM` (reverse) | | | | O |
| **Confirm** | | | | | |
| Return | HTTP 200, `confirmStatus` updated to `CONFIRMED` | O | | | |
| Exception | HTTP 403 | | O | O | |
| | HTTP 409 (invalid reverse transition) | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | A | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

---

## Function: `InspectionMapper` / `ViolationMapper` — ISO-8601 date contract (D4)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-024` | | Function Name | `DateFormatContract` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 25 | | Lack of test cases | 0 |
| Test requirement | All date/datetime JSON fields must follow ISO-8601 (`00_Common_Conventions.md` D4, which overrides the older `dd/MM/yyyy` convention in TDS §17.1 C12/CF-22, per L3); backend parses both legacy `dd/MM/yyyy` and ISO input leniently but must always emit ISO on read, and must not persist formatted strings. (source: MF07-TC-024) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 4 | 0 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| `PUT` body `date` | `"24/07/2026"` (legacy mock format) | O | | | |
| | `"2026-07-24"` (ISO) | | O | | |
| `GET` response | Read back after either write | | | O | |
| Codebase check | `@JsonFormat` annotation search under `com.sfms.mf07` | | | | O |
| **Confirm** | | | | | |
| Return | Both parse to the same `LocalDate` | O | O | | |
| | `"date": "2026-07-24"` (ISO); `occurredAt`/`signedAt`/`medicalReviewAt`/`recordPdfExportedAt`/`pdfGeneratedAt` in `Instant` ISO format; DB column typed `date`/`timestamptz`, not `varchar` | | | O | |
| Structural assertion | `grep -rn "@JsonFormat" src/main/java/com/sfms/mf07/` yields no output | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | N | N | N |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

---

## Function: `common/enums/MealType` contract (D6)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-025` | | Function Name | `MealTypeContract` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 30 | | Lack of test cases | 0 |
| Test requirement | `meal` must be one of the SCREAMING_SNAKE values `BREAKFAST`/`LUNCH`/`SNACK` from the shared `common/enums/MealType` (D6, overriding TDS §17.1 C12 per L4/CF-8); the mock's `"Bữa chiều"` label is corrected to map to `SNACK` ("Bữa phụ") with a warning log. (source: MF07-TC-025) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 5 | 3 | 2 | 0 | 5 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| `POST` body `meal` | `"Bữa trưa"` (legacy Vietnamese label) | O | | | | |
| | `"LUNCH"` (SCREAMING_SNAKE) | | O | | | |
| | `"Bữa chiều"` (mismatched legacy label) | | | O | | |
| | `"Bữa tối"` (invalid, not one of the 3 D6 values) | | | | | O |
| `GET` response | Read back after write | | | | O | |
| Codebase check | `grep -rn "enum MealType" src/main/java/com/sfms/mf07/` | | | | | |
| **Confirm** | | | | | | |
| Return | Parses to `MealType.LUNCH` | O | O | | | |
| | Maps to `MealType.SNACK`, with warning log "D6: nhãn 'Bữa chiều' đã lệch — chốt 'Bữa phụ'" | | | O | | |
| | `"meal": "LUNCH"` returned (SCREAMING_SNAKE, not Vietnamese label) | | | | O | |
| Exception | HTTP 422 (value outside the 3 D6-approved values) | | | | | O |
| Structural assertion | No `enum MealType` re-declared inside `com.sfms.mf07` (must reuse the shared `common/enums/MealType`) | O | O | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | N | N | A | N | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

---

## Function: `InspectionSessionService.openSession()` / `ViolationRecordService.create()` — client-supplied `id` policy (D5/D1)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-026` | | Function Name | `ClientSuppliedIdPolicy` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 30 | | Lack of test cases | 0 |
| Test requirement | If the request body has an `id`, it becomes the `code`; a duplicate must return 409 (`00_Common_Conventions.md` D5). If absent, the server generates the code from `code_sequences` using `INSERT ... RETURNING` to avoid races under concurrency (D1). The response always includes `id`. (source: MF07-TC-026) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 2 | 1 | 1 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Request `id` | `"INSP-20260805-L"` (first POST) | O | | | |
| | Same `"INSP-20260805-L"` (repeat POST) | | O | | |
| | Absent | | | O | |
| | Absent, 2 concurrent POSTs | | | | O |
| **Confirm** | | | | | |
| Return | HTTP 201; response `"id"` equals the client-supplied value; DB `code` matches, UUID `id` generated by JPA | O | | | |
| | HTTP 201; server-generated `code`; response includes `id` | | | O | |
| | 2 concurrent requests yield 2 distinct `code` values (via `code_sequences` + `INSERT ... RETURNING`) | | | | O |
| Exception | HTTP 409, `error.code == INSP-409`; no second record created, no overwrite | | O | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | A | N | B |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |
