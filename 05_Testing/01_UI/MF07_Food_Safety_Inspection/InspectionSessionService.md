# Unit Test Case — InspectionSessionService (MF07 Food Safety Inspection & Monitoring)

---

## Function: `InspectionSessionService.submitStep()` → `InspectionStepFailed` event → `CorrectiveActionPort`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-007` | | Function Name | `InspectionSessionService.submitStep (CAPA auto-creation)` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 60 | | Lack of test cases | 0 |
| Test requirement | A failed step must automatically create a Corrective Action Request in the same transaction (BR-095); severity `HIGH` must also raise `CriticalInspectionFailure` (BR-097). No client call to the CAPA API should be required, and duplicate CAPA creation must be idempotent per `(sourceInspectionCode, step)`. (source: MF07-TC-008) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 2 | 2 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | `submitStep(code, 2, {coreTemp: 68})` fails, no client-side CAPA call made | O | O | | |
| | A simulated error is thrown right after the failed-step write, inside the same transaction | | O | | |
| | FE also calls `POST /corrective-actions` for the same session+step | | | O | |
| | Failed step `severity = HIGH` vs `severity = LOW/MEDIUM` | | | | O |
| **Confirm** | | | | | |
| Return | `CorrectiveActionPort.create()` invoked automatically by backend, matching `sourceInspectionCode` + `step` | O | | | |
| | Step record AND CAPA both roll back together (same transaction) | | O | | |
| | Second call returns the existing CAPA, no duplicate record created (idempotent) | | | O | |
| | `severity=HIGH` → additional event `CriticalInspectionFailure` emitted; `severity=LOW/MEDIUM` → not emitted | | | | O |
| Log message | `MSG-056`: "Re-inspection required: Failed safety check during inspection step [Step Name]." | O | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | A | A | N |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

Note: current mock still has FE calling `POST /corrective-actions` from 2 call-sites (L12/CF-14); Minh must remove the client-side calls when the backend logic ships, otherwise duplicates rely solely on the idempotency safety net.

---

## Function: `InspectionSessionService.reinspect()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-008` | | Function Name | `InspectionSessionService.reinspect` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 50 | | Lack of test cases | 0 |
| Test requirement | Re-inspection after corrective action must create a brand-new session; the original failed session is an immutable legal record and must remain unchanged (BR-096, §4.2 TDS, Luật ATTP 55/2010/QH12). (source: MF07-TC-009) — new endpoint, FE button not yet wired (L16/CF-19) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 2 | 2 | 0 | 0 | 2 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | Session `overall=FAILED` (FX-002); full row-level snapshot of old session taken before call | O | O |
| Action | `reinspect(oldCode, actor)` invoked | O | O |
| **Confirm** | | | |
| Return | New session returned: new `code`, `overall=IN_PROGRESS`, all 3 steps `PENDING`, `reinspects_code = <old code>`, inherits `date`/`meal`/`dish`/`menuId`/`partnerId` | O | |
| | Old session identical to snapshot (incl. `updated_at`, `overall` still `FAILED`, all form rows unchanged) | | O |
| Log message | `MSG-056` warning emitted | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | N | N |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | | |

---

## Function: `InspectionSessionService.update()` (PUT partial merge)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-009` | | Function Name | `InspectionSessionService.update` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 45 | | Lack of test cases | 0 |
| Test requirement | `PUT /inspections/{code}` is a partial merge (PATCH semantics), not a full replace — omitted fields/rows (`step1Fresh`, `step2Rows`, `evidenceFiles`, etc.) must be preserved, never wiped (TDS §17.1 C9, TDS §9.0). (source: MF07-TC-028) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 2 | 2 | 0 | 0 | 2 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | Session with `step1Fresh` (3 rows), `step1Dry` (2 rows), `step2Rows` (2 rows), `evidenceFiles` (2 photos), `staffNotes` | O | O |
| Request body | `PUT { "staffNotes": "..." }` only | O | |
| | `PUT { "step2": "PASSED" }` only, after UTCID01 | | O |
| **Confirm** | | | |
| Return | `staffNotes` updated; all row tables keep their counts (3/2/2), `evidenceFiles` still 2 photos, `inspector`/`dish`/`meal` not null | O | |
| | Only `step2` changes; `staffNotes` from the previous update is still present; `orphanRemoval` not triggered | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | N | N |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | | |

Note: implementation must use `Optional<T>`/`JsonNullable<T>` (or a raw `Map<String,Object>`) to distinguish "field not sent" from "field sent as null" — a plain record type cannot make this distinction and would silently wipe legal records.
