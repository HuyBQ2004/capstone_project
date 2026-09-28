# Unit Test Case — IncidentRecordService & Query Specs (MF11 Incident & Emergency Management)

---

## Function: `openInvestigation`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INC-FUNC-001` | | Function Name | `IncidentRecordService.openInvestigation()` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `90` | | Lack of test cases | `0` |
| Test requirement | Enforce ADR-M11-1: an `Incident` can only be created when `incident_record_id` points to an `incident record` (`REC`) in `COMPLETED` status, merging only source reports (`RPT`) that are in `PENDING_TRIAGE`; any other combination must be rejected with `409`. State Transition Testing over `RPT`/`REC` status combinations. Covers `MF11-TC-005`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `3` | `1` | `2` | `0` | `3` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | `RPT-2026-030` is being merged while still in `DRAFTING` status (not yet `PENDING_TRIAGE`) | O | | |
| | Target `REC` is in `DRAFT` status (not yet `COMPLETED`) | | O | |
| | `REC-2026-021` is `COMPLETED`, merging 2 `RPT` both in `PENDING_TRIAGE` | | | O |
| Input | `openInvestigation(...)` merging `RPT-2026-030` while it is `DRAFTING` | O | | |
| | `openInvestigation(...)` against a `REC` that is not `COMPLETED` | | O | |
| | `openInvestigation(REC-2026-021, ...)` — valid `COMPLETED` `REC` + 2 `PENDING_TRIAGE` `RPT` | | | O |
| **Confirm** | | | | |
| Return | `201`, `incidentRecordCode` **NOT NULL**, `severity = null` (JSON renders `"PENDING"`), `status = SEVERITY_PENDING`, `traceabilityMapStatus = COMPILING` | | | O |
| | `message` = MSG-048 verbatim: `"Suspected food safety incident report logged. Investigation map generated."` | | | O |
| Exception | `ConflictException` → `code = "IEM-114"`, HTTP `409`, `details[0].field = "RPT-2026-030"`, `message` = MSG-149 verbatim: `"An incident record can only be created from a source report that is pending triage."` | O | | |
| | `ConflictException`, HTTP `409` — `REC` not `COMPLETED`; `Incident` **not** created (source states "also 409"; exact `error.code`/message for this specific branch is not spelled out in the TestSpec — see note) | | O | |
| Side-effect | Event `IncidentOpened` emitted exactly once; `incident_events` contains a `REPORTED` milestone | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | A | N |
| Passed/Failed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

> Note (UTCID02): `MF11-TC-005` states the `REC` not `COMPLETED` branch "also" returns `409` and that the `Incident` is not created, without spelling out the exact `error.code`/message for this specific branch the way it does for UTCID01 (`IEM-114`/MSG-149). Do not assert a code/message beyond HTTP `409` for this branch until the TestSpec confirms it explicitly.

---

## Function: `openInvestigation` → `uniqueAffected()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INC-FUNC-002` | | Function Name | `IncidentRecordService.openInvestigation()` → `uniqueAffected()` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `25` | | Lack of test cases | `0` |
| Test requirement | `uq_ias_inc_student UNIQUE (incident_id, student_id)`: when merging multiple `RPT` into one `Incident`, affected students must be de-duplicated by `studentId` (not by `studentName`); `studentName`/`className` are snapshotted at merge time (ADR-M11-7) and must not change if the student's profile is later edited in MF03. Covers `MF11-TC-006`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `2` | `1` | `0` | `1` | `2` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | `RPT-2026-030` lists affected students `HS-3B-001`, `HS-3B-002` | O | O |
| | `RPT-2026-032` lists affected student `HS-3B-001` (duplicate of the above) | O | O |
| | Student profile for `HS-3B-001` is renamed in MF03 **after** the merge | | O |
| Input | `openInvestigation(...)` merging `RPT-2026-030` + `RPT-2026-032` | O | O |
| | Read `affectedStudents` on the resulting `Incident` after the MF03 rename | | O |
| **Confirm** | | | |
| Return | `affectedStudents.size() == 2` — no duplicate `HS-3B-001` row | O | |
| Invariant | De-duplication is keyed by `studentId`, **not** `studentName` — two different students sharing the same name still produce two distinct rows | O | |
| | `studentName`/`className` on the `Incident`'s affected-student rows remain the **snapshot** values captured at merge time (ADR-M11-7); a later MF03 rename does **not** alter the incident's stored rows | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | N | B |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | | |

---

## Function: `IncidentSpec` / `ViolationSpec` / `SourceReportSpec` (+ `PagedResponse.of()`)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INC-FUNC-003` | | Function Name | `IncidentSpec` / `ViolationSpec` / `SourceReportSpec` + `PagedResponse.of()` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `70` | | Lack of test cases | `0` |
| Test requirement | Team Plan §2b-1 (5-field `PagedResponse` shape is invariant) and §2b-3 step 5 (`search` must run **only** on `SEARCH_FIELDS`, never `OR`-ed across every String column); TDS §9.1 corrected `SEARCH_FIELDS` list (per `CF-M11-8`/L18). Covers `MF11-TC-027` (Condition Ref `COND-21`, Constraint Ref `TDS §17.1 C1`). | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `5` | `1` | `1` | `3` | `5` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Precondition | Filter combination matches **zero** `incidents` rows | O | | | | |
| | Dataset with a known `pageCount`; query requests `page` beyond `pageCount` | | O | | | |
| | Dataset with a known `pageCount`; query requests `pageSize ≤ 0` | | O | | | |
| | An incident's `suspectedMealText` contains `"Gà rang"` (`suspectedMealText` **is** in `SEARCH_FIELDS`) | | | O | | |
| | An incident's `firstAidNotes` contains a marker string that appears **nowhere else** (`firstAidNotes` is **not** in `SEARCH_FIELDS`) | | | | O | |
| | `description` and `reporterName` do not exist as fields on the `incidents` entity (Team Plan §2b-3 named them, but TDS §9.1 corrected this — `CF-M11-8`/L18) | | | | | O |
| Input | Build `IncidentSpec` for the zero-match filter, wrap with `PagedResponse.of()` | O | | | | |
| | Query with `page` &gt; `pageCount` | | O | | | |
| | Query with `pageSize ≤ 0` | | O | | | |
| | `search = "Gà rang"` | | | O | | |
| | `search = <marker string found only in firstAidNotes>` | | | | O | |
| | Attempt to build `IncidentSpec` using `"description"` or `"reporterName"` as a search field | | | | | O |
| **Confirm** | | | | | | |
| Return | `200` (**not** `404`) — `{items: [], total: 0, page: 1, pageSize: 8, pageCount: 1}` | O | | | | |
| | `page` clamps to `pageCount`; `pageSize` clamps to `1` | | O | | | |
| | Result set includes the matching incident — `search` on a `SEARCH_FIELDS` column returns hits | | | O | | |
| | Result set is **empty** — `search` on a column outside `SEARCH_FIELDS` returns 0 results | | | | O | |
| Exception | `IllegalArgumentException` thrown at the test layer — building the spec with `"description"`/`"reporterName"` is rejected before any SQL is generated | | | | | O |
| Invariant | `PagedResponse` always exposes the same 5-field shape (`items`, `total`, `page`, `pageSize`, `pageCount`) — Team Plan §2b-1 | O | O | O | O | |
| Side-effect | No SQL is generated against a non-existent `description`/`reporterName` column | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | B | B | N | B | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

---
