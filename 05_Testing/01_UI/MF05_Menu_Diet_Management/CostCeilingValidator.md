# Unit Test Case — CostCeilingValidator (MF05 Menu & Diet Management)

---

## Function: `validate` (invoked from `MenuWorkflowService.approve()`)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MENU-FUNC-007` | | Function Name | `CostCeilingValidator.validate` |
| Created By | `Nguyễn Phương Thảo` | | Executed By | `Thao` |
| Lines of code | `40` | | Lack of test cases | `0` |
| Test requirement | `Allow a menu to exceed the school cost ceiling while still PENDING_APPROVAL (School Admin must see the warning), but block approve() if estimatedCost still exceeds costCeiling (semantic rule DA_DUYET_MA_VAN_VUOT_TRAN); estimatedCost/costCeiling are stored values, not recalculated.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `1` | `0` | `0` | `1` | `1` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Precondition | `costCeiling = 30000` (`FX-017`) | O |
| | Menu `PENDING_APPROVAL` | O |
| `estimatedCost` Input | `29999` (below ceiling) | O |
| | `30000` (exactly on ceiling) | O |
| | `30001` (above ceiling) | O |
| **Confirm** | | |
| Return | `PENDING_APPROVAL` + `estimatedCost=30001` → no exception at submit/hold stage (warning only, School Admin decides) | O |
| | `approve()` with `estimatedCost=30000` → succeeds (equal to ceiling is within limit) | O |
| | `approve()` with `estimatedCost=29999` → succeeds | O |
| | `estimatedCost`/`costCeiling` retain the exact stored value at approval time — not recalculated (C11) | O |
| Exception | `approve()` with `estimatedCost=30001` → blocked, HTTP 422 | O |
| Log message | Message references semantic rule `DA_DUYET_MA_VAN_VUOT_TRAN` (approved menu still over cost ceiling) | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | B |
| Passed/Failed | Passed |
| Executed Date | 2026-08-05 |
| Defect ID | |

> UTCID mapping: UTCID01 = `MF05-TC-017`.
