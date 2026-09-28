# Unit Test Case — ServingApprovalService (MF07 Food Safety Inspection & Monitoring)

---

## Function: `ServingApprovalService.approve()` / `POST /inspections/{code}/co-sign`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-010` | | Function Name | `ServingApprovalService.approve` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 50 | | Lack of test cases | 0 |
| Test requirement | The system must approve a meal for serving only when all 3 inspection steps are `PASSED` (BR-098, QĐ 1246 Điều 5). A `coSigner` name assigned at session creation is NOT proof of signature — `co_signed_at` must be null until the actual co-sign action runs (L15/CF-18). (source: MF07-TC-010) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 5 | 1 | 4 | 0 | 5 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| `step1` | `PASSED` | O | O | O | O | O |
| `step2` | `PASSED` | O | | O | O | O |
| | `FAILED` | | O | | | |
| | `PENDING` | | | | O | |
| `step3` | `PASSED` | O | | | | |
| | `PENDING` | | O | O | O | |
| | `FAILED` | | | | | O |
| **Confirm** | | | | | | |
| Return | HTTP 200, `overall=PASSED`, `co_signed_at` set, `decided_at` set | O | | | | |
| Exception | HTTP 409, `error.code == INSP-098`; `overall` unchanged, `co_signed_at` still null | | O | O | O | O |
| Return (L15 check) | Session with pre-assigned `coSigner` label but `co_signed_at=null` is still blocked by `serving-gate` (label ≠ signature) | O | | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | N | A | A | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

---

## Function: `ServingApprovalService` × `KitchenInspection` (serving-gate decision)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-011` | | Function Name | `ServingApprovalService.evaluateServingGate` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 55 | | Lack of test cases | 0 |
| Test requirement | Low kitchen hygiene score must NOT block serving (ADR-M07-03, descope); only the 3-step inspection `overall` and co-sign status gate serving (BR-094). `reasonCode` must distinguish `INSPECTION_FAILED` / `INSPECTION_INCOMPLETE` / `NO_SESSION` / `NOT_CO_SIGNED`. (source: MF07-TC-014) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 6 | 2 | 4 | 0 | 6 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | |
| Has inspection session? | Yes | O | O | O | O | | O |
| | No | | | | | O | |
| `overall` | `PASSED` | O | O | | | | O (no co-sign) |
| | `FAILED` | | | O | | | |
| | `IN_PROGRESS` | | | | O | | |
| Kitchen score | `92` | O | | O | O | O | O |
| | `40` | | O | | | | |
| `co_signed_at` | set | O | O | O | O | | |
| | null (pre-assigned `coSigner` only) | | | | | | O |
| **Confirm** | | | | | | | |
| Return | `allowed = true`, no `reasonCode` | O | O | | | | |
| | `allowed = false`, `reasonCode = INSPECTION_FAILED` | | | O | | | |
| | `allowed = false`, `reasonCode = INSPECTION_INCOMPLETE` | | | | O | | |
| | `allowed = false`, `reasonCode = NO_SESSION` | | | | | O | |
| | `allowed = false`, `reasonCode = NOT_CO_SIGNED` | | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|
| Type (N/A/B) | N | N | A | A | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | |

Note: UTCID02 (kitchen score 40, inspection PASSED → still `allowed=true`) is the pivotal case — it must NOT revert to the descoped "kitchen lock" behavior of ADR-M07-03.

---

## Function: MF07↔MF09 serving consistency check (`PHAT_SUAT_DU_KIEM_THUC_TRUOT`)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-012` | | Function Name | `ServingConsistencyCheck` (semantic-audit rule + serving-gate primary path) |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 30 | | Lack of test cases | 0 |
| Test requirement | Cross-check that no `SERVED` distribution exists on a day whose inspection `overall` is `FAILED` (`semantic-audit.ts §7`, BR-094); the primary serving API must be blocked by `serving-gate` so this violation cannot be produced through the normal path. (source: MF07-TC-027) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 1 | 2 | 0 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | `FX-002` session FAILED + `FX-019` `SERVED` distribution same day (deliberately violating fixture) | O | | |
| | Session `PASSED` + `SERVED` distribution same day (valid fixture) | | O | |
| | Attempt `POST` to serve via `serving-gate` (MF09 primary path) while inspection FAILED | | | O |
| **Confirm** | | | | |
| Return | Consistency query reports ≥ 1 violation row, with correct session `code` and served count | O | | |
| | Consistency query reports 0 violations | | O | |
| | Request blocked, `allowed=false`, `reasonCode=INSPECTION_FAILED` | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | N | A |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

Note: this check is a safety net (part of the Exit Criteria run before every release), not a replacement for the `serving-gate` itself.
