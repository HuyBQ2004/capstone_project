# Unit Test Cases — MF08 Food Sample Retention
## Scope: DTO/API contract rules — enum serialization (D6), ISO-8601 dates (D4), upload storage (D10), public identifiers (D1), derived-field rejection (D12)

> Source module: `com.sfms.mf08.dto`, `com.sfms.mf08.service.SampleAttachmentService`
> Traceability: `TestSpec-MF08_Food_Sample_Retention.md` §4 — `MF08-TC-017`…`MF08-TC-021`

---

## Function: Enum contract — `MealType` / `SampleUnit` / `HandoverType` / `FoodSampleStatus`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-017` | | Function Name | Enums serialize as SCREAMING_SNAKE, reject display-label strings |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | D6 (common enum convention) requires all enums to serialize/deserialize as `SCREAMING_SNAKE` values in both JSON and DB; Vietnamese display labels (e.g. "Bữa trưa") belong only to the frontend label table and are never accepted as stored values. `MealType` is fixed to `BREAKFAST / LUNCH / SNACK`. Source: `MF08-TC-017`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 8 | 2 | 6 | 0 | 8 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | |
| `mealType` input | `"LUNCH"` | O | | | | | | | |
| | `"Bữa trưa"` (display label) | | O | | | | | | |
| | `"Trưa"` (mock's `MEAL_PREFIX`) | | | O | | | | | |
| | `"lunch"` (wrong case) | | | | O | | | | |
| | `"DINNER"` (not in D6's 3-value set) | | | | | O | | | |
| `unit` input | `"G"` / `"ML"` | | | | | | O | | |
| | `"g"` / `"gam"` | | | | | | | O | |
| Serialize `FoodSampleStatus.SENT_TO_LAB` | — | | | | | | | | O |
| **Confirm** | | | | | | | | | |
| Return | Deserializes to `MealType.LUNCH` | O | | | | | | | |
| | Deserializes to `SampleUnit.G`/`.ML` | | | | | | O | | |
| | Serializes as `"SENT_TO_LAB"` literal | | | | | | | | O |
| Exception | HTTP 422 with D2 error envelope | | O | O | O | O | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | N | A | A | A | A | N | A | B |
| Passed/Failed | | | | | | | | |
| Executed Date | | | | | | | | |
| Defect ID | | | | | | | | |

**Note:** `MealType` lives in `common/enums/` (owned by MF01) — MF08 must not redeclare it. If `common/enums` is not yet available, this test is expected to fail 🔴 legitimately rather than be adjusted to pass. Accepting the display label `"Bữa trưa"` as a stored value means any future label change (`lib/labels.ts`) silently corrupts historical data and breaks `?mealType=` filtering.

---

## Function: Date/time contract — ISO-8601 everywhere

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-018` | | Function Name | All date fields in JSON responses are ISO-8601, no `@JsonFormat("dd/MM/yyyy")` |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | D4 mandates ISO-8601 for all JSON date/time fields, overriding module-level ADRs (D4 explicitly overrides ADR-M11-5 for MF08/MF09/MF11). Source: `MF08-TC-018`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 4 | 4 | 0 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Check target | Serialize `FoodSampleResponse` + `FoodSampleDetailResponse` + `SampleLabRequestResponse` + `UploadedFileResponse` | O | O | O | |
| | Grep source for `@JsonFormat` in `com.sfms.mf08` | | | | O |
| Regex `\d{2}/\d{2}/\d{4}` anywhere in response | Must not match | O | | | |
| Fields `collectedAt`/`retentionDeadline`/`disposedAt`/`retrievedAt`/`sentToLabAt`/`requestedAt`/`uploadedAt` | Must match `^\d{4}-\d{2}-\d{2}T.*Z$` | | O | | |
| `mealShift` (display text, e.g. "Bữa trưa — 10:45") | Excluded from the ISO check (it's not a date field) | | | O | |
| **Confirm** | | | | | |
| Return | 0 regex matches for Vietnamese-style dates; all 7 date fields ISO-8601; 0 `@JsonFormat` occurrences | O | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | N | N | N |
| Passed/Failed | | | | |
| Executed Date | | | | |
| Defect ID | | | | |

**Note:** Keeping `dd/MM/yyyy` would break `new Date(...)` parsing on the frontend and cause `dateFrom`/`dateTo` filtering to silently return empty results — exactly the trap D4 was written to prevent.

---

## Function: `SampleAttachmentService.store()` — Upload routed through `StorageService` (D10)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-019` | | Function Name | Evidence/hand-over file uploads use the shared `StorageService`, with type and size whitelisting |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | D10 mandates that any module storing files (MF08's evidence photos included) must inject `common/storage/StorageService` rather than calling `MultipartFile.transferTo()` or a cloud SDK directly, using the key pattern `"<module>/<business-code>.<ext>"`. Source: `MF08-TC-019`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 4 | 2 | 2 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| File | `mau-com-trang.jpg`, 300 KB | O | | | |
| | `oversize.jpg`, 12 MB (> 10MB limit) | | O | | |
| | `note.txt` (unsupported type) | | | O | |
| | `bien-ban-giao-nhan.pdf`, kind = `HANDOVER_RECORD` | | | | O |
| **Confirm** | | | | | |
| Return | `storageService.store("samples/SMP-...-01.jpg", file)` invoked; `checksumSha256` computed and stored | O | | | O |
| Exception | MSG-008 (file > 10MB), `store()` not called | | O | | |
| | MSG-009 (unsupported file type) | | | O | |
| Architecture rule | No call to `MultipartFile.transferTo()` or `software.amazon.awssdk` anywhere in `com.sfms.mf08` | O | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | A | A | N |
| Passed/Failed | | | | |
| Executed Date | | | | |
| Defect ID | | | | |

**Note:** BR-212 allows either an image or a PDF for `HANDOVER_RECORD`, unlike `EVIDENCE_PHOTO` which is image-only. Recommend an ArchUnit rule banning `transferTo`/AWS SDK imports in `com.sfms.mf08` — far cheaper than discovering the bypass at deploy time when `sfms.storage.provider=r2` is switched on.

---

## Function: Public identifier contract — `id` is the business code, not a UUID (D1)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-020` | | Function Name | `FoodSampleResponse.id` is the business code; `SEARCH_FIELDS` maps DB columns to the exact frontend field names |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | D1 mandates that the public `id` is the business code (`code`); UUID stays an internal PK never exposed via the API. `review.md` finding F7 requires `SEARCH_FIELDS` to map through `@JsonProperty` matching the frontend's `interface FoodSample`. Source: `MF08-TC-020`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 4 | 4 | 0 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Check target | Serialize `FoodSampleResponse` for fixture `SMP-20260706-01` | O | O | | |
| | `GET /samples/{id}/label` (Mẫu số 4 fields) | | | O | |
| | `FoodSampleSpec.SEARCH_FIELDS` | | | | O |
| **Confirm** | | | | | |
| Return | JSON `"id": "SMP-20260706-01"`, no separate `"code"` field, no UUID present | O | | | |
| | `dish_name → "dish"`, `collected_by → "collectedBy"`; `"meal"` is a derived label from `mealType` + `collectedAt` (M08-D05), not a direct column map | | O | | |
| | 4 Mẫu số 4 fields present: meal, sample name, collection time, collector | | | O | |
| | `SEARCH_FIELDS` = exactly `{code, dish_name, collected_by}` — 3 columns, not an OR across every String column, and no longer includes `meal_label` (now derived, M08-D12) | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | N | N | A |
| Passed/Failed | | | | |
| Executed Date | | | | |
| Defect ID | | | | |

**Note:** A field-name mismatch here is a silent failure, not an exception: `search=Đức` returning zero results because the backend filters on `collected_by` while the frontend sends `collectedBy` looks like "no results found," not a bug — until someone traces it back to this mapping.

---

## Function: Derived-field rejection and cross-DTO consistency (D12)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-021` | | Function Name | Derived/read-only fields sent by the client are silently ignored; same-name-different-type fields never leak into persisted data |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | D12 addresses the "200 OK but nothing was actually saved/overridden" trap: Jackson silently drops unmapped fields. MF08 has two variants — (a) 6 derived fields the response emits but the request must never accept (`hoursRemaining`, `weight`, `meal`, `dish`, `retentionDeadline`, `sampleIds`); (b) same field name, different type across request/response (`weight: "120 g"` string in response vs `amount: 120` numeric + `unit: "G"` in request). Source: `MF08-TC-021`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 5 | 1 | 4 | 0 | 5 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Request | Otherwise-valid `POST /samples` payload + 6 dirty derived fields (`retentionDeadline` as free text, `hoursRemaining=72`, forged `collectedBy`, `status=SENT_TO_LAB`, `servings` as string) | O | | | | |
| | Same dirty payload sent through `dispose()` | | O | | | |
| | Same dirty payload sent through `handOver()` with a forged `sampleIds` | | | O | | |
| | Every Request/Response DTO pair for MF08 | | | | O | |
| | `information_schema.columns` after Flyway migration | | | | | O |
| **Confirm** | | | | | | |
| Return | HTTP 201 (field is ignored, not a validation error) | O | | | | |
| DB row (read directly, not via response) | `retention_deadline`, `collected_by`, `status` all reflect server-computed values, never the forged body | O | O | O | | |
| | Only `sampleIds` samples that truly belong to the batch change status; `requestedBy`/`deliveredBy` from JWT | | | O | | |
| Field pairing | Every mutable field shares the same name **and** type in both directions (`amount`, `unit`, `container`, `sanitized`, `sealed`, `fridgeTemp`, `storageLocation`, `mealShift`, `mealType`) | | | | O | |
| Schema | No `hours_remaining` or `weight` column exists | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | N | A | A | A | A |
| Passed/Failed | | | | | |
| Executed Date | | | | | |
| Defect ID | | | | | |

**Note:** This is a "heavy" D12 case, not a formality: because MF08 currently returns 201 either way, a regression here is invisible unless the test reads the database directly. Case (b) — `weight` (string) in the response vs `amount`/`unit` in the request — is the more dangerous variant, because a reviewer skimming both DTOs sees fields that *look* matched. If someone "conveniently" added `weight` to the request DTO, the BR-203 amount gate (`SMP-019`) could be bypassed entirely by calling the API directly, exactly the risk `review.md` finding F2 warned about.
