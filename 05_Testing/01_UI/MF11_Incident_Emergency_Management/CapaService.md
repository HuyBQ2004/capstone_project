# Unit Test Case — CapaService (MF11 Incident & Emergency Management)

---

## Function: `create`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `CAPA-FUNC-001` | | Function Name | `CapaService.create()` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `40` | | Lack of test cases | `0` |
| Test requirement | Enforce **BR-216** ("1 Incident = 1 CAPA — may combine several corrective actions in one record, no parallel CAPAs for the same Incident") and `uq_capa_one_per_incident` (§5.2). `Condition Ref: COND-11` · `Constraint Ref: C6`. Covers `MF11-TC-013`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `2` | `1` | `1` | `0` | `2` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | Fixture `FX-003`: incident `INC-2026-014` has **no** CAPA yet | O | |
| | Fixture `FX-003` + `FX-008`: `INC-2026-014` already has `CAPA-2026-033` (`APPROVED_FOR_IMPLEMENTATION`) | | O |
| Actor | JWT `FX-020` — medical-staff (`U-0002`), investigator drafting the plan | O | O |
| Input | `create(INC-2026-014, planDetails)` — first CAPA for this incident | O | |
| | `create(INC-2026-014, planDetails)` — second CAPA attempt while `CAPA-2026-033` already exists | | O |
| **Confirm** | | | |
| Return | `201`, `status = PENDING_ADMIN_APPROVAL`, `message` = **MSG-049** verbatim `"Corrective actions plan submitted successfully for review."` | O | |
| Exception | `ConflictException IEM-216` (HTTP 409) — BR-216, `uq_capa_one_per_incident` | | O |
| Log message | `MSG-154` verbatim: `"Invalid corrective action transition, or incident [Incident Code] already has corrective action [CAPA Code]."` | | O |
| Side-effect | `details[0].message` suggests adding actions to the **existing** CAPA instead of creating a new one | | O |
| | Second `corrective_actions` row is **never** persisted; `CAPA-2026-033` left unchanged | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | N | A |
| Passed/Failed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | | |

---

## Function: `createFromInspection`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `CAPA-FUNC-002` | | Function Name | `CapaService.createFromInspection()` (consumer of event `InspectionStepFailed`) |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `50` | | Lack of test cases | `0` |
| Test requirement | **BR-095/BR-097** (MF07, cross-cited) + **ADR-M11-4** "Exception" + `ck_capa_source CHECK ((incident_id IS NOT NULL) <> (inspection_id IS NOT NULL))` (§5.2, XOR) + partial unique index `uq_capa_one_per_incident … WHERE incident_id IS NOT NULL`. `Condition Ref: COND-11` · `Constraint Ref: C6`. Covers `MF11-TC-014`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `3` | `1` | `2` | `0` | `3` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | Fixture `FX-013`: inspection `INSP-20260630-L`, `step2=FAILED` (source for the inspection-based CAPA) | O | O | |
| | Fixture `FX-003`: incident `INC-2026-014` exists (used only for the XOR-violation case) | | O | |
| Input | `createFromInspection({inspectionCode:"INSP-20260630-L", incidentCode:null})` — called **twice** | O | | |
| | `createFromInspection({inspectionCode:"INSP-20260630-L", incidentCode:"INC-2026-014"})` — **both** sources set | | O | |
| | `createFromInspection({inspectionCode:null, incidentCode:null})` — **no** source | | | O |
| **Confirm** | | | | |
| Return | `201` ×2, both rows `status = PENDING_ADMIN_APPROVAL`, `incidentCode = null`; **no** incident auto-opened as a side effect of CAPA creation (BR-097 — a *suggestion* only, not automatic) | O | | |
| Exception | `ValidationException` HTTP `400` — XOR violation of `ck_capa_source` (exact `IEM-xxx` code **not assigned** in TestSpec/TDS §10 for this specific branch — oracle gap, see note below) | | O | O |
| Side-effect | Partial unique index `uq_capa_one_per_incident` (`WHERE incident_id IS NOT NULL`) does **not** block the two inspection-sourced rows | O | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | A | A |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

> Note (UTCID02/UTCID03): `MF11-TC-014` states the `ck_capa_source` XOR violation returns HTTP 400 but the source does not name a specific `IEM-xxx`/`MSG-xxx` code for this branch (TDS §10 lists `IEM-001`/MSG-138 only for generic DTO/reference-resolution failures). Test asserts HTTP 400 and the CHECK-constraint rejection only; do not invent a code.

---

## Function: `approvePlan` / `rejectPlan` (full lifecycle)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `CAPA-FUNC-003` | | Function Name | `CapaService.approvePlan()` / `.rejectPlan()` (and lifecycle actions `submit-for-approval`, `accept`, `submit-evidence`, `validate`, `rework`) |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `150` | | Lack of test cases | `0` |
| Test requirement | **BR-153** (School Administration approves/rejects **proposed** corrective actions **before** Service Caterers execute them) + **BR-216** step 2 + **ADR-M11-4** + §10 → `IEM-216`, HTTP 409. `Condition Ref: COND-12` · `Constraint Ref: C6` · `AP-M11-04`. State Transition Testing over the full 6-state × 6-action table. Covers `MF11-TC-015`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `8` | `3` | `4` | `1` | `8` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | |
| Precondition | CAPA seeded at `DRAFT` (full-chain start) | O | | | | | | | |
| | CAPA seeded at `PENDING_ADMIN_APPROVAL` | | O | | | | | | |
| | CAPA seeded at `SUBMITTED_EVIDENCE` (evidence already submitted) | | | O | | | | | |
| | CAPA seeded at `IN_PROGRESS` | | | | O | | | | |
| | CAPA seeded at `DRAFT` (invalid `accept` target) | | | | | O | | | |
| | CAPA seeded at `APPROVED_FOR_IMPLEMENTATION` | | | | | | O | O | |
| | CAPA seeded at `COMPLETED` (terminal state) | | | | | | | | O |
| Input | Full valid sequence: `submit-for-approval` → `approve` → `accept` → `submit-evidence` → `validate` | O | | | | | | | |
| | `reject(reason)` | | O | | | | | | |
| | `requestRework(reason)` | | | O | | | | | |
| | `approve()` while `IN_PROGRESS` | | | | O | | | | |
| | `accept()` while `DRAFT` | | | | | O | | | |
| | `validate()` while `APPROVED_FOR_IMPLEMENTATION` | | | | | | O | | |
| | `submit-evidence()` before `accept` (still `APPROVED_FOR_IMPLEMENTATION`) | | | | | | | O | |
| | `approve()` (representative action) on a `COMPLETED` CAPA | | | | | | | | O |
| **Confirm** | | | | | | | | | |
| Return | Final `status = COMPLETED`; every intermediate transition persisted `DRAFT→PENDING_ADMIN_APPROVAL→APPROVED_FOR_IMPLEMENTATION→IN_PROGRESS→SUBMITTED_EVIDENCE→COMPLETED` | O | | | | | | | |
| | The `approve` step's response message = **MSG-160** verbatim `"Corrective action plan approved. The assigned unit can now begin implementation."` (supersedes MSG-029, per TDS §10.2) | O | | | | | | | |
| | `status → DRAFT` | | O | | | | | | |
| | `status → IN_PROGRESS` | | | O | | | | | |
| Exception | `ConflictException IEM-216` (HTTP 409) — BR-216/ADR-M11-4, `status` **unchanged** | | | | O | O | O | O | O |
| Log message | `MSG-154` verbatim: `"Invalid corrective action transition, or incident [Incident Code] already has corrective action [CAPA Code]."` | | | | O | O | O | O | O |
| Side-effect | Rejected transition attempt is a no-op — persisted row unchanged | | | | O | O | O | O | O |
| | `COMPLETED` accepts **no** outgoing transition — confirms the lifecycle's terminal state | | | | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | N | N | N | A | A | A | A | B |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | | | |

---

## Function: `rejectPlan` / `requestRework` — missing reason

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `CAPA-FUNC-004` | | Function Name | `CapaService.rejectPlan()` / `.requestRework()` — mandatory-reason guard |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `30` | | Lack of test cases | `0` |
| Test requirement | **BR-216** step 2 ("Not approved → back to `DRAFT`, **reason mandatory**") and step 5 ("request rework with a **mandatory** reason") + UC-168 AF1 + `ck_capa_reject` (§5.2) + §10 → `IEM-001`/MSG-138. `Condition Ref: COND-12` · `Constraint Ref: C6`. Covers `MF11-TC-016`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `3` | `0` | `2` | `1` | `3` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | CAPA seeded at `PENDING_ADMIN_APPROVAL` (for `rejectPlan`) | O | O | |
| | CAPA seeded at `SUBMITTED_EVIDENCE` (for `requestRework`) | | | O |
| Input | `rejectPlan(reason="")` | O | | |
| | `rejectPlan(reason="  ")` (whitespace only) | | O | |
| | `requestRework(reason=null)` | | | O |
| **Confirm** | | | | |
| Exception | `ValidationException IEM-001` (HTTP 400) | O | O | O |
| Log message | `MSG-138` verbatim: `"Validation failed: one or more required fields are missing or invalid."` | O | O | O |
| Side-effect | `details[0].field = "reason"` | O | O | O |
| | CAPA `status` unchanged (`PENDING_ADMIN_APPROVAL` / `SUBMITTED_EVIDENCE` respectively) | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | B | A |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

> Note: TestSpec `MF11-TC-016` covers the reason guard for both `rejectPlan()` and `.requestRework()` as the **same shared validator** (`ck_capa_reject`) — UTCID01/UTCID02 exercise it via `rejectPlan()`, UTCID03 via `.requestRework()` to confirm the guard is reused, not re-implemented per method.

---

## Function: `requestRework` — plan preserved

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `CAPA-FUNC-005` | | Function Name | `CapaService.requestRework()` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `45` | | Lack of test cases | `0` |
| Test requirement | **BR-216** step 5 verbatim: *"Not yet passed → resend the **exact plan already approved** (**no need to resubmit** to School Administration since the plan itself did not change)"* + UC-168 AF1. `Condition Ref: COND-12` · `Constraint Ref: C6`. Covers `MF11-TC-017`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `1` | `1` | `0` | `0` | `1` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Precondition | CAPA at `SUBMITTED_EVIDENCE`, previously approved: `approvedAt`, `submittedForApprovalAt`, `description` already set | O |
| Input | `requestRework(reason="...")` | O |
| **Confirm** | | |
| Return | `status → IN_PROGRESS` — explicitly **not** `PENDING_ADMIN_APPROVAL`, **not** `DRAFT` | O |
| Invariant | `approvedAt` unchanged; `submittedForApprovalAt` unchanged; `description` unchanged | O |
| Side-effect | `reworkReason` + `reworkRequestedAt` recorded; event `CapaReworkRequested` published exactly **once** | O |
| | `incident_events` gains a **new** milestone; existing milestones are **not** modified (Constraint `C4`) | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | N |
| Passed/Failed | Untested |
| Executed Date | 2026-08-05 |
| Defect ID | |

> Note (regression guard): TestSpec flags the FAIL signature explicitly — if the implementation instead pushes the CAPA back to `PENDING_ADMIN_APPROVAL`, it forces School Administration to re-approve a plan that never changed, which is exactly the procedural loop BR-216 step 5 was written to block. UTCID01's assertion on `status → IN_PROGRESS` (not `PENDING_ADMIN_APPROVAL`) is the guard against this regression.
