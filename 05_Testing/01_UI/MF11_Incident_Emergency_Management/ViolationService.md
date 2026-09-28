# Unit Test Case — ViolationService (MF11 Incident & Emergency Management)

---

## Function: `record`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `VIO-FUNC-001` | | Function Name | `ViolationService.record()` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `90` | | Lack of test cases | `0` |
| Test requirement | Enforce BR-183 (required fields: reporter, description, evidence photo, `severityLevel`, `caterersRepresentativeId`) and BR-211 ("Data fields: `locationOfIncident`, `occurredAt`, `caterersRepresentativeId`, `signedRecordPhotoUrl` (required)"), including the explicit BR-211 carve-out that a missing `signedRecordPhotoUrl` does **not** block `record()` at save time — that gate applies later, at resolution (see Function `submit`/`uploadSignedRecord` below). Condition Ref `COND-16`, Constraint Ref `C9`, `C8`. Covers `MF11-TC-022`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `6` | `0` | `5` | `1` | `6` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | |
| Precondition | Fixture: valid `ViolationRecord` request DTO satisfying BR-183/BR-211 (all required fields populated except the one field under test) | O | O | O | O | O | O |
| Input | `record(dto)` — `locationOfIncident` missing | O | | | | | |
| | `record(dto)` — `occurredAt` missing | | O | | | | |
| | `record(dto)` — `caterersRepresentativeId` missing | | | O | | | |
| | `record(dto)` — `description` missing | | | | O | | |
| | `record(dto)` — `evidenceFiles` empty (`[]`) | | | | | O | |
| | `record(dto)` — `signedRecordPhotoUrl` missing, all other required fields present | | | | | | O |
| **Confirm** | | | | | | | |
| Return | `400 Bad Request`, `error.code = "IEM-085"` | O | O | O | O | O | |
| | `201 Created` — record saved; absence of `signedRecordPhotoUrl` does **not** block `record()` (BR-211 carve-out; gate enforced later at resolution stage) | | | | | | O |
| Exception | `ValidationException IEM-085` (HTTP 400) | O | O | O | O | O | |
| Log message | `MSG-085`: Violation record submission blocked: required fields (description, evidence photo) are missing. | O | O | O | O | O | |
| Side-effect | `error.details[]` lists **every** missing required field, not only the first encountered | O | O | O | O | O | |
| | `ViolationRecord` persisted with `signedRecordPhotoUrl = null`; no gate triggered at save time | | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|
| Type (N/A/B) | A | A | A | A | A | B |
| Passed/Failed | Passed | Passed | Passed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | |

---

## Function: `check`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `VIO-FUNC-002` | | Function Name | `RepeatViolationDetector.check()` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `70` | | Lack of test cases | `0` |
| Test requirement | Enforce BR-187 (amended 2026-07-26): repeat-violation matching against another violation record of severity **≥ MEDIUM** within a **90-day** window, matched by `caterersRepresentativeId`/contractor unit — **not** by `staffName` — as a **best-effort** mechanism that **never auto-escalates**. Condition Ref `COND-17`, Constraint Ref `TDS §17.1 C9`, `AP-M11-07` (Individual sanction). Covers `MF11-TC-023`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `5` | `1` | `2` | `2` | `5` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Precondition | Prior violation record for representative `U-0003`, severity `MEDIUM` | O | O | | | |
| | Prior violation record for representative `U-0003`, severity `LOW` | | | O | | |
| | Prior violation record for a **different** representative, **same `staffName`**, severity `HIGH` | | | | O | |
| | Prior violation record for representative `U-0003`, severity `HIGH` | | | | | O |
| Input | New violation, same representative `U-0003`, `MEDIUM`, gap = **89 days** | O | | | | |
| | New violation, same representative `U-0003`, `MEDIUM`, gap = **91 days** | | O | | | |
| | New violation, same representative `U-0003`, `LOW`, gap = 10 days | | | O | | |
| | New violation, **different** representative (same `staffName`), `HIGH`, gap = 10 days | | | | O | |
| | New violation, same representative `U-0003`, `HIGH`, gap = **exactly 90 days** (boundary) | | | | | O |
| **Confirm** | | | | | | |
| Return | HTTP `201` — warning **never blocks** the save (BR-187 best-effort) | O | O | O | O | O |
| | `repeatSuspect = true`, `warnings[0].code = "MSG-088"` | O | | | | O |
| | `repeatSuspect = false`, no `warnings` entry | | O | O | O | |
| Log message | `MSG-088`: Possible repeat violation detected: staff name matches a prior violation within the lookback window. Manual verification required. (kept verbatim as-is per `error_message.md`, even though the wording says "staff name" — CF-M11-4/L14) | O | | | | O |
| Side-effect | Matching key is `caterersRepresentativeId`, **not** `staffName` — proven by the different-representative/same-`staffName` case not matching | | | | O | |
| | New record's `severity` is **not** auto-escalated; no event beyond `ViolationRecorded` is published | O | O | O | O | O |
| | Request DTO contains **no** `staffName`/`staffPosition`/`disciplinaryLevel` field (verified by boilerplate grep) | O | O | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | N | B | A | A | B |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

---

## Function: `submit` / `uploadSignedRecord`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `VIO-FUNC-003` | | Function Name | `ViolationResolutionService.submit()` / `ViolationPdfService.uploadSignedRecord()` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `150` | | Lack of test cases | `0` |
| Test requirement | Enforce BR-211 hard gate: the "Close record" (LOW) and "Submit to School Administration for approval" (MEDIUM/HIGH) actions are **disabled** until the violation record has a `signedRecordPhotoUrl`, and uploading the signed record itself is blocked until the violation PDF has been exported (`pdfExported = true`). Content edits/saves on the violation record remain allowed throughout the gate. Condition Ref `COND-18`, Constraint Ref `TDS §17.1 C8`, `AP-M11-06` (Signature gate bypass). Covers `MF11-TC-024`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `4` | `2` | `2` | `0` | `4` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | `VIO` severity `MEDIUM`, `pdfExported = false` | O | | | |
| | `VIO` severity `MEDIUM`, `pdfExported = true`, **0** files of type `SIGNED_RECORD` | | O | O | |
| | Same `VIO` as UTCID02/03, now with **1** `SIGNED_RECORD` file added | | | | O |
| Input | `POST /violations/{code}/signed-record` | O | | | |
| | `POST /violations/{code}/resolution` | | O | | O |
| | Save/edit violation record content (e.g. update `description`) | | | O | |
| **Confirm** | | | | | |
| Return | `409 Conflict`, `error.code = "IEM-211"` | O | | | |
| | `409 Conflict`, `error.code = "IEM-095"` | | O | | |
| | `200 OK` — content save succeeds; gate does **not** block editing the violation record (BR-211, 3rd clause) | | | O | |
| | `200 OK`, `resolution = "PENDING_APPROVAL"` (MEDIUM — BR-186) | | | | O |
| Exception | `ConflictException IEM-211` (HTTP 409) — signed copy upload blocked, PDF not yet exported | O | | | |
| | `ConflictException IEM-095` (HTTP 409) — close/submit blocked, no signed record uploaded | | O | | |
| Log message | `MSG-156`: Export and print the violation record PDF for signature before uploading the signed copy. | O | | | |
| | `MSG-095`: Cannot close/submit this violation resolution: the signed 3-party record photo has not been uploaded yet. Please print, collect signatures, and upload the signed record first. | | O | | |
| | `MSG-087`: Violation resolution submitted for approval. | | | | O |
| Side-effect | `error.details[0] = {field: "signedRecordFiles", message: "0 files — requires ≥ 1 (BR-211)"}` | | O | | |
| | Violation record content update is persisted (gate applies only to close/submit and to signed-record upload, not to content saves) | | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | A | A | N | N |
| Passed/Failed | Passed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

---

## Function: `submit` / `approve`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `VIO-FUNC-004` | | Function Name | `ViolationResolutionService.submit()` / `ViolationResolutionService.approve()` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `130` | | Lack of test cases | `0` |
| Test requirement | Enforce BR-186 severity-based approval routing: **LOW** violations may be self-approved/closed directly by **Medical Staff**; **MEDIUM/HIGH** violations require **School Administration** approval before closing, and a Medical Staff attempt to approve MEDIUM/HIGH is forbidden. Also enforce BR-185/`ck_vio_resolution`: any branch where `resolution ≠ PENDING` must carry `action_taken` + `handled_by_id` + `unitSanctionLevel`, restricted to the 4 BR-185 values (`WRITTEN_REMINDER`, `CONTRACT_WARNING`, `CONTRACT_PENALTY`, `TERMINATION_PROPOSAL`) — the legacy `disciplinaryLevel` values are rejected. Condition Ref `COND-19`, Constraint Ref `C9`. Covers `MF11-TC-025`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `5` | `3` | `2` | `0` | `5` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Precondition | `VIO` severity `LOW` | O | | | | |
| | `VIO` severity `MEDIUM`/`HIGH` | | O | O | O | O |
| Actor | Medical Staff | O | O | O | | |
| | School Administration | | | | O | |
| Input | `submit()`/`approve()` with valid `unitSanctionLevel` (one of the 4 BR-185 values) | O | O | | O | |
| | `approve()` attempted directly by Medical Staff | | | O | | |
| | `submit()`/`approve()` with `unitSanctionLevel = "SUSPENSION"` (legacy `disciplinaryLevel` value, not in BR-185 enum) | | | | | O |
| **Confirm** | | | | | | |
| Return | `resolution = "APPROVED"`, `status = "CLOSED"` | O | | | O | |
| | `resolution = "PENDING_APPROVAL"`, `status = "OPEN"` | | O | | | |
| | `400 Bad Request` — `unitSanctionLevel` rejected, not one of the 4 BR-185 values | | | | | O |
| Exception | `ForbiddenException IEM-186` (HTTP 403) — Medical Staff cannot approve MEDIUM/HIGH | | | O | | |
| Log message | `MSG-089`: Violation resolution approved. Disciplinary action applied. | O | | | O | |
| | `MSG-087`: Violation resolution submitted for approval. | | O | | | |
| | `MSG-157`: Violations rated MEDIUM or HIGH must be approved by School Administration. | | | O | | |
| Side-effect | `action_taken` + `handled_by_id` + `unit_sanction_level` all persisted together (`ck_vio_resolution`) whenever `resolution ≠ PENDING` | O | O | | O | |
| | `unitSanctionLevel` accepts only `WRITTEN_REMINDER` / `CONTRACT_WARNING` / `CONTRACT_PENALTY` / `TERMINATION_PROPOSAL`; legacy `disciplinaryLevel` values (`SUSPENSION`, `WRITTEN_WARNING`) are rejected | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | N | N | A | N | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

---

## Function: `weekly`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `VIO-FUNC-005` | | Function Name | `OverrideAbuseReportService.weekly()` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `60` | | Lack of test cases | `0` |
| Test requirement | Enforce BR-190 (amended 2026-07-26): weekly auto-compiled override-abuse report, expressed as a contractor-unit-level `ViolationRecord` (`violationType = OVERRIDE_ABUSE`), and ADR-M11-6: the report is **derived** (no persisted report table), **grouped by representative/contractor unit — not by `staffName`**. Condition Ref `COND-20`, Constraint Ref `C12`, `AP-M11-10`. Covers `MF11-TC-026`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `3` | `1` | `1` | `1` | `3` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | 2 `VIO` `OVERRIDE_ABUSE` records, same representative `U-0003`, both in ISO week `2026-W27` | O | | |
| | 1 `VIO` `OVERRIDE_ABUSE` record from a **different** representative, same week `2026-W27` | | O | |
| | 1 `VIO` `OVERRIDE_ABUSE` record dated in a **different** ISO week | | | O |
| Input | `weekly()` / `GET ?week=2026-W27` | O | O | O |
| **Confirm** | | | | |
| Return | **1** merged row, `overrideCount = 2`, `threshold = 3` | O | | |
| | **Separate** row per representative — grouping is by representative/contractor unit, **not** by `staffName` (legacy seed `OVR-W27-01` keyed by `staffName` was incorrect, L20) | | O | |
| | Record from the different week is **excluded** from the `?week=2026-W27` result | | | O |
| Side-effect | Response keeps the `PagedResponse` 5-field shape (§2b-1) | O | O | O |
| | `OverrideAbuseWeeklyJob` generates `ViolationRecord` rows only — **no** separate report table is written | O | O | O |
| | Event `OverrideAbuseWeeklyCompiled` is published once per week | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | A | B |
| Passed/Failed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |
