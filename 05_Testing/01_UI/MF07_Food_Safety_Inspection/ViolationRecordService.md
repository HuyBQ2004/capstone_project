# Unit Test Case — ViolationRecordService (MF07 Food Safety Inspection & Monitoring)

---

## Function: `ViolationRecordService.export()` / `uploadSignedRecord()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-017` | | Function Name | `ViolationRecordService.export / uploadSignedRecord` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 65 | | Lack of test cases | 0 |
| Test requirement | The mandated sequence "export PDF → print → wet-ink sign by both parties → upload signed photo/scan" must be enforced (BR-211); PDF must be genuinely server-generated (PDFBox) for reprint consistency, and the signed photo must carry `sha256` (ADR-M07-04). (source: MF07-TC-017) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 3 | 1 | 0 | 4 |

**Note — Disabled, moved to MF11:** `@Disabled("Chuyển MF11 — M07-D07")` per TestSpec §2.1-TER (`M07-D07`/`M07-D08`). `violation_records` + `/api/v1/violations*` moved to MF11 to avoid duplicate `CREATE TABLE`/route ownership with `TDS-MF11_Backend.md`. Kept here for traceability (Policy 4.4 — immutable history; this file is the source MF11 copies from) but **not counted in MF07 `mvn verify` exit criteria**.

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | Violation `pdf_exported=false`, `signed_record_photo_url=NULL` (FX-017) | O | O | | |
| Action | `uploadSignedRecord()` called BEFORE export | O | | | |
| | `export-pdf` called | | O | O | |
| | `uploadSignedRecord()` called after export | | | O | |
| | `export-pdf` called a second time | | | | O |
| **Confirm** | | | | | |
| Return | HTTP 200; PDF has `%PDF-` header, `pdf_exported=true`, `pdf_generated_at` set, `pdf_version` incremented, contains all mandated `specialreport2173_1.md` fields | | O | | |
| | HTTP 200; `signed_record_photo_url` + `sha256` stored (ADR-M07-04) | | | O | |
| | Second PDF has identical structure (same page count, same template fields) as the first | | | | O |
| Exception | `error.code == VIO-211`, HTTP 409; no file saved | O | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | A | N | N | N |
| Passed/Failed | Untested (Disabled — moved to MF11) | Untested (Disabled — moved to MF11) | Untested (Disabled — moved to MF11) | Untested (Disabled — moved to MF11) |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | M07-D07 | M07-D07 | M07-D07 | M07-D07 |

---

## Function: `ViolationRecordRepository.findRepeatCandidates()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-018` | | Function Name | `ViolationRecordRepository.findRepeatCandidates` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 40 | | Lack of test cases | 0 |
| Test requirement | Repeat-offense detection within a 90-day window must run server-side keyed on `caterersRepresentativeId` (BR-187, post 26/07/2026 revision), ignoring any `repeatSuspect` value the client sends (L14/CF-16); only `MEDIUM`+ severity counts. Assert the `VIO-088` code and detection logic only — the `MSG-088` text ("staff name matches") is stale and not asserted. (source: MF07-TC-018) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 6 | 1 | 2 | 3 | 6 |

**Note — Disabled, moved to MF11:** `@Disabled("Chuyển MF11 — M07-D07")` per TestSpec §2.1-TER. Not counted in MF07 exit criteria; kept for traceability only.

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | |
| Precondition | Same `caterersRepresentativeId` (FX-014/FX-016), `severity=MEDIUM` | O | O | O | | O | |
| | Different `caterersRepresentativeId` | | | | | | O |
| Days since last violation | `30` | O | | | | | |
| | `89` | | O | | | | |
| | `90` (on-point) | | | O | | | |
| | `91` | | | | O | | |
| Severity | `LOW`, within 30 days | | | | | O | |
| Client payload | `repeatSuspect: false` sent despite actual repeat | | | | | | |
| **Confirm** | | | | | | | |
| Return | `repeatSuspect == true` | O | O | O | | | |
| | `repeatSuspect == false` (outside window / low severity / different rep) | | | | O | O | O |
| | Backend overrides client-sent `repeatSuspect: false` with the correct server-computed value | | | | O(4)† | | |
| Log message | Warning `VIO-088` emitted | O | O | O | | | |

†See note below — UTCID04 combines the 91-day boundary with the client-override assertion per the source test steps.

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|
| Type (N/A/B) | N | B | B | B | A | A |
| Passed/Failed | Untested (Disabled — moved to MF11) | Untested (Disabled — moved to MF11) | Untested (Disabled — moved to MF11) | Untested (Disabled — moved to MF11) | Untested (Disabled — moved to MF11) | Untested (Disabled — moved to MF11) |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | M07-D07 | M07-D07 | M07-D07 | M07-D07 | M07-D07 | M07-D07 |

---

## Function: `ViolationRecordService.create()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-019` | | Function Name | `ViolationRecordService.create` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 48 | | Lack of test cases | 0 |
| Test requirement | Violation records must target the caterer UNIT, never an individual staff member — BR-183 (26/07/2026); deprecated personal fields (`staffName`, `staffPosition`, `disciplinaryLevel`, `type: "Nhân sự"`) must be rejected on create (422) but still returned when reading pre-existing historical records. (source: MF07-TC-019) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 2 | 2 | 0 | 4 |

**Note — Disabled, moved to MF11:** `@Disabled("Chuyển MF11 — M07-D07")` per TestSpec §2.1-TER. Not counted in MF07 exit criteria; kept for traceability only.

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Request payload | Contains `staffName` | O | | | |
| | Contains `staffPosition` / `disciplinaryLevel` / `type: "Nhân sự"` | | O | | |
| | Valid BR-183 payload (`caterersRepresentativeId`, `unitSanctionLevel`) | | | O | |
| Read path | `GET /violations/VIO-2026-007` (historical record with `staffName`, FX-015) | | | | O |
| **Confirm** | | | | | |
| Return | HTTP 201 | | | O | |
| | HTTP 200; `staffName` of the old record still returned (readable, not creatable) | | | | O |
| Exception | HTTP 422; message states the field is deprecated per BR-183 (26/07/2026) | O | O | | |
| Log message | `MSG-086`: "Violation record saved successfully." | | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | A | A | N | N |
| Passed/Failed | Untested (Disabled — moved to MF11) | Untested (Disabled — moved to MF11) | Untested (Disabled — moved to MF11) | Untested (Disabled — moved to MF11) |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | M07-D07 | M07-D07 | M07-D07 | M07-D07 |

Note: missing description or evidence photo → `VIO-085` (`MSG-085`), HTTP 422 — see `ck_s1f_evidence`-equivalent rule for violation records (referenced in TC-019 expected results, not separately columned here).

---

## Function: `ViolationRecordService` — resolution transition gate (MSG-095)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-020` | | Function Name | `ViolationRecordService.transitionResolution` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 52 | | Lack of test cases | 0 |
| Test requirement | Saving a violation draft must never be blocked by a missing signed photo, but closing/submitting for approval must be hard-gated server-side (BR-211, `MSG-095`) — the gate must not rely on the client disabling a button (L22/CF-30); locked records are append-only (ADR-M07-05). (source: MF07-TC-020) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 6 | 2 | 4 | 0 | 6 |

**Note — Disabled, moved to MF11:** `@Disabled("Chuyển MF11 — M07-D07")` per TestSpec §2.1-TER. Not counted in MF07 exit criteria; kept for traceability only.

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | |
| `signed_record_photo_url` | `NULL` | O | O | O | | | |
| | Has photo | | | | O | O | O |
| Target `resolution` | `PENDING_APPROVAL` | O | | | O | | |
| | `APPROVED` | | O | | | O | |
| | Save draft only, stays `PENDING` | | | O | | | |
| | Record already `locked`, edit `description` (content column) | | | | | | O |
| **Confirm** | | | | | | | |
| Return | HTTP 409, `error.code == VIO-095`, DB unchanged | O | O | | | | O |
| | HTTP 200, save succeeds (BR-211: saving is never blocked) | | | O | | | |
| | HTTP 200, `locked = true` | | | | O | | |
| | HTTP 200 | | | | | O | |
| | Content column NOT updated; change recorded only as an `audit_logs` row (BR-188, append-only) | | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|
| Type (N/A/B) | A | A | N | N | N | A |
| Passed/Failed | Untested (Disabled — moved to MF11) | Untested (Disabled — moved to MF11) | Untested (Disabled — moved to MF11) | Untested (Disabled — moved to MF11) | Untested (Disabled — moved to MF11) | Untested (Disabled — moved to MF11) |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | M07-D07 | M07-D07 | M07-D07 | M07-D07 | M07-D07 | M07-D07 |

Note: gate must live in the **service** layer, not the controller, because the MF11 approval flow (UC-182) also passes through this same gate (L10).
