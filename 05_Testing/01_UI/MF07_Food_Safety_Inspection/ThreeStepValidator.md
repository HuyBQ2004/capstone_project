# Unit Test Case — ThreeStepValidator (MF07 Food Safety Inspection & Monitoring)

---

## Function: `InspectionSessionService.submitStep()` × 3 → `ServingApprovalService.approve()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-001` | | Function Name | `InspectionSessionService.submitStep() / ServingApprovalService.approve()` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 140 | | Lack of test cases | 0 |
| Test requirement | The 3 inspection steps (Điều 3–5 QĐ 1246) must all reach `PASSED` and be co-signed before `overall` becomes `PASSED`; each individual step completion alone must NOT flip `overall` early (BR-079, BR-098, BR-099, ADR-M07-02). (source: MF07-TC-001) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 4 | 0 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | Session `IN_PROGRESS`, all 3 steps `PENDING` (FX-003) | O | O | O | O |
| | Caterer JWT `SUP-001`, owner of session (FX-020) | O | O | O | O |
| | Valid menu + dishId (BR-093) | O | O | O | O |
| | All form rows valid, "Failed" rows have photo evidence | O | O | O | O |
| Step submitted | Step 1 payload submitted | O | | | |
| | Step 2 payload submitted (`coreTemp: 82`) | | O | | |
| | Step 3 payload submitted | | | O | |
| | Co-sign action performed (`approve(code, coSigner)`) | | | | O |
| **Confirm** | | | | | |
| Return | `step1=PASSED`, `overall` still `IN_PROGRESS` | O | | | |
| | `step2=PASSED`, `overall` still `IN_PROGRESS` | | O | | |
| | `step3=PASSED`, `overall` still `IN_PROGRESS` (not yet co-signed) | | | O | |
| | `overall=PASSED`, `co_signed_at` and `decided_at` set (ADR-M07-02) | | | | O |
| Log message | `MSG-043`: "Food safety inspection checklist submitted successfully." | O | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | N | N | N |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

---

## Function: `ThreeStepValidator.assertStepOrder()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-002` | | Function Name | `ThreeStepValidator.assertStepOrder` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 45 | | Lack of test cases | 0 |
| Test requirement | Step 3 must stay locked until Step 2 is `PASSED`, and Step 2 must stay locked until Step 1 is `PASSED` (BR-079, QĐ 1246 Điều 4). Violations must raise `INSP-079` / HTTP 409 without writing any row. (source: MF07-TC-002) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 1 | 3 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | Session `FX-003`: `step1=PASSED`, `step2=PENDING` | O | O | O | |
| | Session: `step1=PENDING` | | | | O |
| Step / status submitted | Submit Step 3 while `step2=PENDING` | O | | | |
| | Submit Step 3 while `step2=FAILED` | | O | | |
| | Submit Step 3 while `step2=PASSED` | | | O | |
| | Submit Step 2 while `step1=PENDING` | | | | O |
| **Confirm** | | | | | |
| Return | Step 3 submitted successfully | | | O | |
| Exception | `IllegalStepOrderException`, `error.code == INSP-079`, HTTP 409; target step remains `PENDING`, no row written | O | O | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | A | A | N | A |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |
