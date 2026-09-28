# Unit Test Case — SourceReportService (MF11 Incident & Emergency Management)

---

## Function: `createByTeacher` / `createByParent`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SRC-FUNC-001` | | Function Name | `SourceReportService.createByTeacher()` / `.createByParent()` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `80` | | Lack of test cases | `0` |
| Test requirement | Enforce BR-146 (as amended 2026-07-20): filing a raw source report must **not** automatically merge reports, must **not** auto-create an `Incident`, and (per BR-149, auto-quarantine removed) must **not** trigger batch quarantine or traceability-map compilation — merging into an `Incident` is a fully manual decision made by Medical Staff. Covers `MF11-TC-001`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `1` | `1` | `0` | `0` | `1` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Precondition | Fixture `FX-023`: JWT of homeroom teacher of class 3A (`U-0004`) | O |
| | Fixture `FX-011`: `HS-3A-001` belongs to class 3A | O |
| | `incidentRepository`, `incidentRecordRepository` mocked; ports `InventoryBatchPort`, `DishQueryPort` mocked | O |
| Actor | JWT `FX-023` — homeroom teacher `U-0004` (`teacher3A`) | O |
| Input | `createByTeacher(payload, teacher3A)` — valid payload: `className="3A"`, 1 student, `symptoms=["Đau bụng"]`, `mealDate=2026-06-30`, `description` non-empty | O |
| **Confirm** | | |
| Return | New `RPT`: `status = PENDING_TRIAGE`, `code` matches `^RPT-\d{4}-\d{3}$`, `clusterSignal = null` (fewer than 3 students so far) | O |
| | `message` = MSG-048 verbatim: `"Suspected food safety incident report logged. Investigation map generated."` (per §1.11; note that the *"Investigation map generated"* clause does **not** apply literally in this context — see note below) | O |
| Side-effect | `verify(incidentRepository, never()).save(any())` — **no** `Incident` is created | O |
| | `verify(inventoryBatchPort, never()).markQuarantined(any(), any(), any())` — **no** batch is quarantined | O |
| | `verify(traceabilityMapService, never()).compile(any())` — **no** traceability map is generated | O |
| | **No** `incident_events` row of type `QUARANTINE` is written | O |
| | Exactly **1** event emitted: `SourceReportFiled`; **no** `IncidentOpened`, **no** `BatchQuarantined` | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | N |
| Passed/Failed | Passed |
| Executed Date | 2026-08-05 |
| Defect ID | |

> Note (UTCID01): MSG-048 is the shared "report logged" success message and literally includes the clause *"Investigation map generated."* — in this raw-report context that clause does **not** apply (no map is compiled); the test asserts the message text verbatim regardless (source: MF11-TC-001, note **L40**). If auto-merge or auto-quarantine were ever observed, this is a hard-reject signal (`AP-M11-01`): Medical Staff would lose the authority to decide when an investigation opens, and every mistaken parent report would spawn an undeletable investigation record.

---

## Function: `SourceReportService.create()` — DTO validation & scope guard

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SRC-FUNC-002` | | Function Name | `SourceReportService.create()` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `95` | | Lack of test cases | `0` |
| Test requirement | BR-146 / MSG-023: a source report submission missing a required field (`symptoms`, `mealDate`, `description`) must be rejected with `400 IEM-023`. BR-201 (MF03): a Parent may only file a report for a student linked to their own account (≤ 5 linked students); an unlinked student must be rejected with `403 IEM-005`, without revealing whether the target student exists. Covers `MF11-TC-002`, `MF11-TC-004`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `5` | `1` | `4` | `0` | `5` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Precondition | Otherwise-valid payload with `symptoms = []` (empty) | O | | | | |
| | Otherwise-valid payload with `mealDate = null` | | O | | | |
| | Otherwise-valid payload with `description = ""` (empty) | | | O | | |
| | Parent `U-0013` is linked to `HS-3B-001` only | | | | O | O |
| Actor | (unauthenticated validation path — DTO-level) | O | O | O | | |
| | JWT of Parent `U-0013` | | | | O | O |
| Input | `create(payload)` with `symptoms` missing | O | | | | |
| | `create(payload)` with `mealDate` missing | | O | | | |
| | `create(payload)` with `description` missing | | | O | | |
| | `createByParent(payload for HS-4A-002, U-0013)` — target student **not** linked to this Parent | | | | O | |
| | `createByParent(payload for HS-3B-001, U-0013)` — target student **is** linked to this Parent | | | | | O |
| **Confirm** | | | | | | |
| Return | `201 Created` — `RPT` created successfully for the linked student | | | | | O |
| Exception | `ValidationException` → `code = "IEM-023"`, HTTP `400` | O | O | O | | |
| | `ForbiddenException` → `code = "IEM-005"`, HTTP `403` — BR-201 scope guard | | | | O | |
| Log message | `MSG-023` verbatim: `"Incident report incomplete: Symptoms, date of ingestion, and dishes are required."` | O | O | O | | |
| | `MSG-005` verbatim: `"Access denied: You do not have permission to access this resource."` | | | | O | |
| Side-effect | `$.error.details` is an **array** with ≥ 1 element, each element having `field` + `message` | O | O | O | | |
| | `verify(sourceReportRepository, never()).save(any())` | O | O | O | O | |
| | `403`, **not** `404` — response `details` stays empty, does **not** disclose whether the target student exists | | | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | A | A | A | A | N |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

---

## Function: `ClusterSignalService.detect()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SRC-FUNC-003` | | Function Name | `ClusterSignalService.detect()` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `50` | | Lack of test cases | `0` |
| Test requirement | BR-146: the system automatically detects and displays an advisory notice when **3 or more students** report similar symptoms within a **24-hour** window; the signal is advisory only for MF10 and must **never** create an `Incident` on its own. Boundary Value Analysis on student count (2 / 3) and time window (within 24h / 24h + 1 minute). Covers `MF11-TC-003`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `3` | `0` | `0` | `3` | `3` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | 2 students report the same symptom key within 24h | O | | |
| | 3 students report the same symptom key within 24h | | O | |
| | 3 students report the same symptom key, spread across a window of 24h + 1 minute | | | O |
| Input | `detect(...)` over the 2-student/24h fixture | O | | |
| | `detect(...)` over the 3-student/24h fixture | | O | |
| | `detect(...)` over the 3-student/24h+1min fixture | | | O |
| **Confirm** | | | | |
| Return | **No** `ClusterSignal` produced (below threshold) | O | | O |
| | `ClusterSignal{symptomKey, reportCodes[], studentCount=3, window="24h"}` produced | | O | |
| Side-effect | `verify(incidentRepository, never()).save(any())` — the signal never creates an `Incident`, in any branch | O | O | O |
| | Event `SymptomClusterDetected` emitted exactly once when the threshold is exceeded; consumer is MF10 (advisory display only) | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | B | B | B |
| Passed/Failed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

> Implementation note: the "3 students / 24h" threshold is an internal project convention — BR-146 itself states *"no legal basis was found"* for the exact number, but it remains a valid oracle because the BR has been finalized; the test must **not** substitute a different number on its own.

---
