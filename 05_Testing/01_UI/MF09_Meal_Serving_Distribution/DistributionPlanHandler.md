# Unit Test Case — DistributionPlanHandler (MF09 Meal Serving & Distribution)

---

## Function: `DistributionPlanHandler` (consumes event `MenuPublished`) / caterer snapshot read by `MealHistoryService`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SRV-FUNC-009` | | Function Name | `DistributionPlanHandler.on(MenuPublished)` + `MealHistoryService` caterer read |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `60` | | Lack of test cases | `0` |
| Test requirement | ADR-M09-8: `catererPartnerId`/`catererName` are resolved **once** at session-creation time and frozen (hard-written) onto `student_distributions` — later contract changes with a new caterer must **not** rewrite historical meal history; if the port returns empty, the two columns stay `NULL` and the response **hides** the field instead of erroring or blocking distribution. Covers `MF09-TC-014`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `2` | `1` | `0` | `1` | `2` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | Fixture `FX-013`: `CatererAssignmentPort.currentCatererOf(SCH-001, *)` returns `PartnerView("CAT-001", "Cty TNHH Suất ăn An Bình")` at the moment `MenuPublished` fires | O | |
| | Fixture `FX-013`: on a different day, the port returns `Optional.empty()` | | O |
| Input | Publish `MenuPublished` → sessions generated; then swap the stub to return `CAT-002` and call `MealHistoryService.listForChild()` | O | |
| | Publish `MenuPublished` while port returns empty | | O |
| **Confirm** | | | |
| Return | `catererPartnerId = "CAT-001"`, `catererName = "Cty TNHH Suất ăn An Bình"`; port called **exactly once**, at creation time | O | |
| | History still returns `CAT-001` after the stub is swapped to `CAT-002` — **not** rewritten to reflect the current caterer | O | |
| | `verify(catererAssignmentPort, never()).currentCatererOf(any(), any())` during the read flow (0 calls) | O | |
| | Both columns `NULL`; JSON response **omits** the `caterer`/`catererPartnerId` keys entirely (hidden field, not a literal `null`); session generation still proceeds, not blocked | | O |
| Exception | — (no error path; empty port must not block distribution) | | |
| Log message | — | | |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | N | B |
| Passed/Failed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | | |

> Implementation note: `catererPartnerId` must be `Partner.code` (prefix `CAT-`), never `SupplierPartnership.id` (prefix `PTN-`) — TDS §9.2 fixed this mismatch on 2026-07-29 (item G1). Assert the `CAT-` prefix explicitly.
