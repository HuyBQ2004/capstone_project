# Unit Test Case — IncidentClosureService (MF11 Incident & Emergency Management)

---

## Function: `CapaService.validate()` / `IncidentClosureService.close()` — recovery gate

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `ICS-FUNC-001` | | Function Name | `CapaService.validate()` / `IncidentClosureService.close()` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `60` | | Lack of test cases | `0` |
| Test requirement | **BR-155** ("after **confirming completion** of the corrective actions") + §10 → `IEM-155`, HTTP 409 + **CF-M11-15/L25** ("must **not** return MSG-004 — wrong semantics, would kick the user out to the login screen"). `Condition Ref: COND-13` · `Constraint Ref: TDS §17.1 C7` · `AP-M11-13` (Wrong MSG code). Covers `MF11-TC-018`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `2` | `1` | `1` | `0` | `2` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | Fixture `FX-003` (`INC-2026-014`, 4 students): 1 student `recovered = false`, others `true` | O | |
| | Fixture `FX-003`: **all 4** students `recovered = true`, `recovered_at` set (`ck_ias_recovered`) | | O |
| Input | `close(INC-2026-014)` / `validate()` | O | O |
| **Confirm** | | | |
| Return | Action proceeds (validation passes / `close` allowed to continue to the next gate) | | O |
| Exception | `ConflictException IEM-155` (HTTP 409) | O | |
| Log message | `MSG-153` verbatim: `"This incident cannot be closed: [Number] corrective actions are still open and [Number] affected students have not been confirmed as recovered."` | O | |
| Side-effect | `error.details` is an **array** listing **each** unrecovered student: `[{field:"HS-3A-001", message:"Nguyễn Minh Khang — chưa xác nhận hồi phục"}, …]` | O | |
| | `error.details` also carries `{field, capaOpen, studentsNotRecovered}` per **D2** | O | |
| Invariant | Explicit **negative** assert: `error.code != "IEM-004"`; HTTP status `!= 401`; `message` does **not** contain `"Session expired"` | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | A | N |
| Passed/Failed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | | |

> Note: this guard exists specifically because an earlier draft (per §1.11) returned `MSG-004`/401 here, which would have logged the user out mid-way through handling a food-safety incident with a hospitalized child (`AP-M11-13`). UTCID01's negative assertions are the regression check against that exact defect.

---

## Function: `IncidentClosureService.close()` — role guard

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `ICS-FUNC-002` | | Function Name | `IncidentClosureService.close()` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `25` | | Lack of test cases | `0` |
| Test requirement | **BR-155** verbatim: *"Only **School Administration** has the right to **Close** an incident after confirming completion of the corrective actions."* + §16 TDS + MSG-005 → `IEM-005`, HTTP 403. `Condition Ref: COND-13` · `Constraint Ref: C7` · `AP-M11-05`. Covers `MF11-TC-019`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `3` | `1` | `2` | `0` | `3` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | Incident ready to close: all gates satisfied (CAPA `COMPLETED`, students recovered, MF06 batches in a final state) | O | O | O |
| Actor | JWT `FX-020` — medical-staff (`U-0002`), the lead investigator with every other permission | O | | |
| | JWT `FX-025` — `system-admin` (§16: System Admin has **no** business write permission in MF11) | | O | |
| | JWT `FX-021` — `school-admin` (`U-0001`) | | | O |
| Input | `close(INC-code)` | O | O | O |
| **Confirm** | | | | |
| Return | `200`, `status = CLOSED`, `closedAt`/`closedById`/`closingStatement` written (`ck_inc_closed`), `message` = **MSG-161** verbatim `"Incident closed successfully. The case record is now read-only."` (supersedes MSG-029/MSG-046, per TDS §10.2) | | | O |
| Exception | `ForbiddenException IEM-005` (HTTP 403) — separation of duty, **not** a technical permission gap | O | O | |
| Log message | `MSG-005` verbatim: `"Access denied: You do not have permission to access this resource."` | O | O | |
| Side-effect | Event `IncidentClosed` published + `CLOSED` milestone added to `incident_events` | | | O |
| | `incidents.status` left unchanged (still open) | O | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | A | N |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

---

## Function: `IncidentClosureService.close()` + `.closureReadiness()` — 3-gate decision table

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `ICS-FUNC-003` | | Function Name | `IncidentClosureService.close()` / `.closureReadiness()` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `180` | | Lack of test cases | `0` |
| Test requirement | **BR-155** + Constraint **C1** (`data_model.puml`: *"An incident can only be closed once every CAPA is `COMPLETED`"*) + §10 → `IEM-155`/`IEM-217`, HTTP 409. `Condition Ref: COND-13` · `Constraint Ref: C7` + `C1`. Decision Table technique — 3 independent gates: CAPA state, student recovery, MF06 quarantine-batch final state. Covers `MF11-TC-020`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `5` | `1` | `3` | `1` | `5` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| CAPA gate | Not all CAPA `COMPLETED` (`CAPA-2026-030` = `SUBMITTED_EVIDENCE`) | O | | | | O |
| | All CAPA `COMPLETED` | | O | O | O | |
| Student-recovery gate | All students `recovered = true` | O | | O | O | |
| | 2 students still `recovered = false` | | O | | | O |
| MF06 quarantine-batch gate | Batch reached a final state | O | O | | | O |
| | Batch still `QUARANTINED` (not final) | | | O | | |
| | Batch `RETURNED_TO_SUPPLIER` (final) | | | | O | |
| Input | `close(INC-code)` | O | O | O | O | O |
| **Confirm** | | | | | | |
| Return | `200`, `status = CLOSED`, `message` = **MSG-161** verbatim `"Incident closed successfully. The case record is now read-only."` | | | | O | |
| Exception | `ConflictException IEM-155` (HTTP 409) | O | O | | | O |
| | `ConflictException IEM-217` (HTTP 409) | | | O | | |
| Log message | `MSG-153` verbatim (CAPA/student wording) | O | O | | | O |
| | `MSG-155` verbatim: `"This incident cannot be closed: quarantined batches have not reached a final state."` | | | O | | |
| Side-effect | `details[0] = {field:"CAPA-2026-030", message:"still in SUBMITTED_EVIDENCE state"}` | O | | | | |
| | `details` lists **each** unrecovered student | | O | | | O |
| | `details` do **not** stop at the first failing gate — both the CAPA list and the student list appear together in the same response | | | | | O |
| | `incidents.status` left unchanged (blocked branches) | O | O | O | | O |
| | Event `IncidentClosed` + `CLOSED` milestone recorded (success branch) | | | | O | |
| | `GET /closure-readiness` returns the **identical** blocking checklist (HTTP 200) so the FE can render a disabled Close button with the reason | O | O | O | | O |
| | `GET /closure-readiness` reports `ready = true`, no blocking items | | | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | A | A | A | N | B |
| Passed/Failed | Passed | Passed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

> Row mapping to `MF11-TC-020`'s decision table: UTCID01=row 1, UTCID02=row 2, UTCID03=row 3, UTCID04=row 4, UTCID05=row 5 (two gates failing simultaneously — CAPA gate + recovery gate — while the quarantine gate stays clear).

---

## Function: Report-lock guard (`ADR-M11-8`) across the investigation/quarantine services

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `ICS-FUNC-004` | | Function Name | Read-only guard applied across `IncidentInvestigationService` / `QuarantineService` (all write actions) |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `200` | | Lack of test cases | `0` |
| Test requirement | **ADR-M11-8** (decision 18/07/2026, decisions 2+5): *"after 'Export & Submit Incident Report' succeeds, the **entire** record becomes read-only for **every** actor, with **no** unlock mechanism"*; sole exception: *"`POST /incidents/{code}/close` … and `incident_events` writes"* + §10 → `IEM-RO`, HTTP 409. `Condition Ref: COND-14` · `Constraint Ref: TDS §17.1 C10` · `AP-M11-08` (Unlock exported report). Negative testing technique — scans all write actions. Covers `MF11-TC-021`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `3` | `2` | `1` | `0` | `3` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | Incident with `reportExported = true` (the official report has already been exported and submitted) | O | O | O |
| Actor | Every role in the system (medical-staff, school-admin, service-caterers, system-admin, teacher, parent, supplier) | O | | |
| | `school-admin` (`FX-021`) performing the close | | O | O |
| Input | All **10** blocked write actions, called in turn with `reportExported = true`: `assess`, `quarantine-batches`, `evidence`, `evidence/finalize`, `root-cause`, `assessment-draft`, `sample-retrieval`, `validate-capa`, `request-rework`, `report` (2nd submission) | O | | |
| | `close(INC-code)` | | O | O |
| **Confirm** | | | | |
| Return | `200` — `close` succeeds despite the record being locked (the sole exception in ADR-M11-8) | | O | |
| Exception | `ConflictException IEM-RO` (HTTP 409) for **all 10** actions, with **every** role including `system-admin` | O | | |
| Log message | `MSG-158` verbatim: `"This incident is read-only: the official report has already been submitted."` | O | | |
| Side-effect | `incident_events` gains a **new** `CLOSED` milestone; the append still succeeds normally despite the lock (append-only, always allowed) | | O | O |
| | Reflection/route-scan across all MF11 endpoints finds **0** endpoints that ever set `reportExported = false` (no unlock mechanism exists) | O | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | B | N |
| Passed/Failed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

> Note (regression guard): TestSpec explicitly names both failure signatures to guard against — (1) generating an unlock endpoint (`AP-M11-08`), which would let a report already submitted to the authorities be edited after the fact, destroying its legal standing (BR-154); and (2) blocking `close` along with everything else, which would make an incident permanently un-closeable once its report is exported (a procedural deadlock). UTCID02 is the regression check against both.
