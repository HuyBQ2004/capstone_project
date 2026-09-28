# Unit Test Case — ChecklistScoringService / KitchenInspectionService / ChecklistGroupService (MF07 Food Safety Inspection & Monitoring)

---

## Function: `ChecklistScoringService.score()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-013` | | Function Name | `ChecklistScoringService.score` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 40 | | Lack of test cases | 0 |
| Test requirement | Hygiene score must be computed server-side from `itemResults`, ignoring any `score` value the client sends (BR-100, BR-104, L21/CF-26) — the score feeds the Compliance Report sent to authorities. (source: MF07-TC-011) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 1 | 2 | 0 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | Checklist 20 items (FX-012), 11/20 results = passed (FX-013) → correct score is 55 | O | O | |
| | `itemResults` empty | | | O |
| Request `score` field | Client sends `score: 100` (deliberately wrong) | O | | |
| | `score` field omitted from request | | O | |
| **Confirm** | | | | |
| Return | `score == 55` (recomputed, not 100), `result == FAILED` (55 < 60), response still contains `score` + `result` fields | O | O | |
| Exception | HTTP 422 — cannot score a session with zero items | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | N | A |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

---

## Function: `KitchenInspectionService.submit()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-014` | | Function Name | `KitchenInspectionService.submit` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 42 | | Lack of test cases | 0 |
| Test requirement | `score < 60` → `result=FAILED` and warning `MSG-093`, WITHOUT locking the kitchen (ADR-M07-03 descope, `mock-server.ts` `passed = score >= 60`, boundary uses `>=`). (source: MF07-TC-012, MF07-TC-013) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 1 | 1 | 2 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | `KIN-2026-016` (FX-009) | O | | | |
| `score` | `55` | O | | | |
| | `59` (one below on-point) | | O | | |
| | `60` (on-point, must PASS) | | | O | |
| | `61` | | | | O |
| **Confirm** | | | | | |
| Return | `result == FAILED` | O | O | | |
| | `result == PASSED` | | | O | O |
| | Kitchen operations continue as normal — no lock record created, no `MSG-024` used | O | | | |
| Log message | `MSG-093`: "Kitchen safety score below 60% threshold. Alert sent to School Administration — kitchen operations continue as normal." (HTTP 200/201, not a 4xx error) | O | O | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | A | B | B | N |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

Note (UTCID01): `MSG-093` is not yet linked to a BR/UC in the traceability matrix (L6/CF-2) — pending Huy's addition; UC-014 AF1 (kitchen lock + MSG-024) has been formally descoped.

---

## Function: `ChecklistScoringService` — checklist item evidence validation

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-015` | | Function Name | `ChecklistScoringService.validateItemResults` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 35 | | Lack of test cases | 0 |
| Test requirement | Each "Failed" checklist item must carry a reason (and, per CHECK `ck_cir_evidence`, evidence); mandatory items without a result must also be rejected. Violations raise `INSP-016`/HTTP 422 with `MSG-016`. (source: MF07-TC-015) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 1 | 3 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Item result | `passed=false`, `reason=null`, `photoCount=0` | O | | | |
| | `passed=false`, `reason` + photo supplied | | O | | |
| | `passed=true`, no reason | | | O | |
| | Mandatory item (`mandatory=true`) with no result submitted | | | | O |
| **Confirm** | | | | | |
| Return | Request accepted (HTTP 201) | | O | O | |
| Exception | `error.code == INSP-016`, HTTP 422, message references item code (e.g. `CLI-3-8`) | O | | | O |
| Log message | `MSG-016`: "Checklist validation failed: Mandatory safety checks are missing." | O | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | A | N | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

---

## Function: `ChecklistGroupService.delete()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-016` | | Function Name | `ChecklistGroupService.delete` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 38 | | Lack of test cases | 0 |
| Test requirement | Deleting a checklist group is a soft-delete (hide from new sessions only) — historical data referenced by `checklist_item_results` must never be hard-deleted (BR-210); a referenced group requires confirmation and returns `MSG-094`/HTTP 409 first. (source: MF07-TC-016) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 3 | 1 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Group referenced by existing `checklist_item_results`? | Yes, no confirmation | O | | | |
| | Yes, `?confirmed=true` | | O | | |
| | No | | | O | |
| Old session re-read after soft-delete | — | | | | O |
| **Confirm** | | | | | |
| Return | `is_active = false`, no `DELETE FROM` executed | | O | O | |
| | Old session still shows all items; `checklist_item_results` row count unchanged; new sessions no longer offer the hidden group | | | | O |
| Exception | HTTP 409 + `MSG-094`; group NOT hidden yet, awaiting confirmation | O | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | A | N | N | N |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |
