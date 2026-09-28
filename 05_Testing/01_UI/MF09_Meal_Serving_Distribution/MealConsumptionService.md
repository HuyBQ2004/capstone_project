# Unit Test Case — MealConsumptionService (MF09 Meal Serving & Distribution)

---

## Function: `record`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SRV-FUNC-007` | | Function Name | `MealConsumptionService.record` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `95` | | Lack of test cases | `0` |
| Test requirement | BR-121: record and track each student's meal-consumption history; D6 — `ConsumptionStatus` must accept **all 4** values including the initial state `PENDING` (not just 3); BR-121/BR-133 — a consumption record is locked (read-only) once the serving day has passed because the data has already entered the daily summary sent to parents. Covers `MF09-TC-005`, `MF09-TC-010`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `5` | `2` | `3` | `0` | `5` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Precondition | Fixture `FX-006`: `MR-TD-3A-004`, `consumption=PENDING`, same day as clock `FX-024` | O | O | O | | |
| | Fixture `FX-007`: `MR-0702-001` dated `2026-07-02`, `consumption=FULLY`; clock `FX-024=2026-07-24` | | | | O | |
| | Fixture `FX-006`: `MR-TD-3A-004` dated same day as clock | | | | | O |
| `consumption` value | `∈ {PENDING, FULLY, PARTIALLY, NOT_CONSUMED}` (parameterized, all 4) | O | | | | |
| | `"ATE_HALF"` (invalid enum literal) | | O | | | |
| | `"Ăn hết"` (Vietnamese display label, not the stored value — D6) | | | O | | |
| Target record | Past-day record (`2026-07-02`, 22 days before clock) | | | | O | |
| | Current-day record | | | | | O |
| **Confirm** | | | | | | |
| Return | All 4 values accepted; for the 3 non-`PENDING` values, `recordedAt != null` and `recordedBy = JWT.userId`; event `MealConsumptionRecorded` published per successful write | O | | | | |
| | Record editable, `recordedAt` **updated**, change audited | | | | | O |
| Exception | `ValidationException SRV-001` (HTTP 400) — `$.error.details[0].field == "consumption"`, lists all 4 valid values | | O | O | | |
| | `ConflictException SRV-025` (HTTP 409) — locked after serving day, `consumption` stays `FULLY`, `recordedAt` unchanged | | | | O | |
| Log message | `MSG-138`: Validation failed: one or more required fields are missing or invalid. | | O | O | | |
| | `MSG-133`: Consumption record is locked after the serving day. | | | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | N | A | A | A | N |
| Passed/Failed | Passed | Passed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |
