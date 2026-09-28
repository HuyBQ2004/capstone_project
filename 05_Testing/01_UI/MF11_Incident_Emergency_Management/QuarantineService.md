# Unit Test Case — QuarantineService (MF11 Incident & Emergency Management)

---

## Function: `quarantineBatch` — mandatory reason

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `QS-FUNC-001` | | Function Name | `QuarantineService.quarantineBatch()` (mandatory reason) |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `50` | | Lack of test cases | `0` |
| Test requirement | BR-149 (amended 2026-07-27) verbatim: *"Medical Staff selects each specific material batch individually … the system sets Quarantine status immediately upon selection, with a mandatory quarantine reason entered right at that batch row."* Enforced by §5.2 `ck_iqb_reason CHECK (length(btrim(reason)) > 0)` and §10 error table → `IEM-149`/HTTP 400. Covers `MF11-TC-010`. Condition Ref `COND-08` · Constraint Ref `TDS §17.1 C5` · `AP-M11-03`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `3` | `0` | `3` | `0` | `3` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | Batch `BATCH-MP-8820` exists, not yet quarantined | O | O | O |
| Actor | JWT — role `medical-staff` | O | O | O |
| Input | `quarantineBatch(incident, batchId, reason="")` (empty string) | O | | |
| | `quarantineBatch(incident, batchId, reason="   ")` (whitespace-only) | | O | |
| | `quarantineBatch(incident, batchId, reason=null)` | | | O |
| **Confirm** | | | |
| Return | `400`, `error.code = IEM-149`, `details[0].field = "reason"`, `message` = MSG-151 verbatim *"A quarantine reason is required before a batch can be quarantined."* | O | O | O |
| Exception | `ValidationException IEM-149` (HTTP 400) | O | O | O |
| Log message | `MSG-151`: A quarantine reason is required before a batch can be quarantined. | O | O | O |
| Side-effect | `verify(inventoryBatchPort, never()).markQuarantined(any(), any(), any())` — no batch is locked | O | O | O |
| | **No** `BatchQuarantined` event published | O | O | O |
| | **No** row written to `incident_events` | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | A | A |
| Passed/Failed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

---

## Function: `quarantineBatch` — one-way lock (BR-217)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `QS-FUNC-002` | | Function Name | `QuarantineService.quarantineBatch()` (one-way lock, BR-217) |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `55` | | Lack of test cases | `0` |
| Test requirement | BR-217 (amended 2026-07-27) — the "unlock" step (steps 5–6) was dropped: *"there is no longer a physically-locked batch to release, since it was already returned to the supplier in step 1b before test results came back."* ADR-M11-3: re-calling quarantine on an already-quarantined batch → **409 `IEM-149B`**, not a silent no-op. Uniqueness is per `(incident_id, batch_id)`, so the same batch under a **different** incident may be quarantined independently. Covers `MF11-TC-011`. Condition Ref `COND-08` · Constraint Ref `C5` · `AP-M11-03` (Release endpoint). | | | |
| Test requirement (cont'd) | **Structural assertion:** a reflection scan of `QuarantineService` and `IncidentController` confirms **0** public methods/routes matching `(?i)release\|unquarantine`, and **0** fields `releasedAt`/`releaseReason` exist on `IncidentQuarantineBatch` — no release/unquarantine capability exists anywhere in the codebase. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `4` | `2` | `1` | `1` | `4` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | Batch `BATCH-MP-8820` exists, not yet quarantined, under `Incident-A` | O | O | O | |
| | `BATCH-MP-8820` already quarantined under `Incident-A` (from UTCID01), `reason`/`quarantinedAt` recorded | | O | | |
| | `BATCH-MP-8820` already quarantined under `Incident-A` (from UTCID01) | | | O | |
| | `BATCH-MP-8820` not yet quarantined under a **different** incident, `Incident-B` | | | | O |
| Actor | JWT — role `medical-staff` | O | O | O | O |
| Input | `quarantineBatch(Incident-A, "BATCH-MP-8820", reason="...")` — 1st call | O | | | |
| | `quarantineBatch(Incident-A, "BATCH-MP-8820", reason="different reason")` — 2nd call, same incident + same batch | | O | | |
| | Reflection scan of `QuarantineService` + `IncidentController` for `release`/`unquarantine` methods/routes | | | O | |
| | `quarantineBatch(Incident-B, "BATCH-MP-8820", reason="...")` — same batch, different incident | | | | O |
| **Confirm** | | | | | |
| Return | `200`, batch quarantined, `reason`/`quarantinedAt` recorded | O | | | O |
| | `409`, `error.code = IEM-149B`, `message` = MSG-152 verbatim *"Batch [Batch Code] was already quarantined at [Timestamp]. This action cannot be undone."*, `details` cite the **1st call's** `quarantinedAt` (ISO format, D4) | | O | | |
| Exception | `ConflictException IEM-149B` (HTTP 409) | | O | | |
| Side-effect | Original `reason`/`quarantinedAt` from the 1st call are **not** overwritten by the 2nd (failed) call | | O | | |
| | Reflection: **0** public methods matching `(?i)release\|unquarantine` on `QuarantineService`/`IncidentController`; **0** fields `releasedAt`/`releaseReason` on `IncidentQuarantineBatch` | | | O | |
| | New quarantine row created independently for `(Incident-B, BATCH-MP-8820)` — uniqueness is per `(incident_id, batch_id)`, not per `batch_id` alone | | | | O |
| Log message | — | | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | A | B | N |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

> Note (UTCID03): this is a structural/reflection assertion rather than a behavioral one — it verifies no release/unquarantine capability was ever added to the codebase, consistent with BR-217's premise that the physical batch is already gone (returned to the supplier) by the time lab results arrive, so there is nothing left in the system to "unlock."
