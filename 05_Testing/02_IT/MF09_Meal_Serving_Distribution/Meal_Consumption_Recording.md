# Integration Test Case — Meal Consumption Recording (MF09 Meal Serving & Distribution)

---

## Feature: Meal Consumption Recording

### 1. General Information

| Field | Value |
|---|---|
| Feature | Meal Consumption Recording |
| Test requirement | Verify the write-contract safety of `PATCH /api/v1/meal-records/{code}` against derived/read-only fields (D12) and the idempotency of the BR-133 daily parent-summary job that is compiled from consumption records. |
| Number of TCs | 2 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 2 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 2 | 0 |
| Round 3 | 0 | 0 | 2 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **PATCH /api/v1/meal-records/{code} — Write Contract (D12)** | | | | | | | | | | | | | | |
| MF09-TC-INT-019 | Test that sending derived fields and a same-name-wrong-type field to the meal-record update endpoint never silently succeeds and never mutates DB columns that do not exist. | 1. Snapshot `meal_consumption_records WHERE code='MR-TD-3A-004'` (`consumption=PENDING`, `dishes_label="Cơm trắng, Canh bí đỏ nấu tôm"`).<br>2. `PATCH /api/v1/meal-records/MR-TD-3A-004` with `{"consumption":"FULLY","allergyWarning":"Tôm","isSpecial":"yes","dietTag":"CHAY","assignedMeal":"X","specialMeal":"Y"}` (1 valid field + 5 derived fields).<br>3. `PATCH` again with same-name-wrong-type: `{"dishes":[{"dishId":"com-trang"}]}` (Response's `dishes` is a string label).<br>4. `PATCH` again with read-only/already-written fields: `{"recordedAt":"2020-01-01T00:00:00Z","code":"MR-HACKED-001","id":"MR-HACKED-001"}`.<br>5. Diff the snapshot after each step. | 1. Step 2: **only** `consumption` changes to `FULLY` (+ `recorded_at`/`recorded_by_user_id`/`updated_at` auto-filled) — the 5 derived fields have no column to change, by design (ADR-M09-2).<br>2. Step 3: **400 `SRV-001`** (MSG-138), **not** a silent 200; `$.error.details` names `dishes` specifically as wrong-typed.<br>3. Step 4: `recorded_at` unchanged; `code`/`id` **unchanged** (`MR-TD-3A-004`) — D1 forbids a client overwriting the business code of an existing record.<br>4. Step 5: every diff matches exactly the list above, no extra rows/columns touched. | `FX-005` — `MR-TD-3A-004` `consumption=PENDING`. | Passed | 2026-08-05 | Minh | Pending | | | Pending | | | D12 write-contract — MF09 has 6 Response-only derived fields (`assignedMeal`,`specialMeal`,`allergyWarning`,`isSpecial`,`dietTag`,`dishes`) with no backing column |
| **Daily Parent Summary Job — BR-133** | | | | | | | | | | | | | | |
| MF09-TC-INT-015 | Test that the BR-133 daily summary job is idempotent per `(student_id, date)` and does not resend on rerun. | 1. Seed 1 day with 10 `SERVED` sessions + 10 `meal_consumption_records` (`FX-025` fake event bus).<br>2. Run `DailyParentSummaryJob`; count published events.<br>3. Rerun the job for the same day; count events again.<br>4. Inspect one event payload. | 1. Step 2: **10** `DailyMealSummaryCompiled` events (1 per student).<br>2. Step 3: **0** new events — total stays 10 (idempotent per ADR-M09-7).<br>3. Step 4: payload has `studentId`, ISO `date`, `servedDishNames[]`, runtime `totalKcal`, `consumption` (D6 enum), and an `Optional` `specialMealNote` (empty when no substitute meal).<br>4. MF09 does **not** call SMTP/push directly — that is MF10's responsibility (ADR-M09-7). | `FX-025` fake event bus; 10 `SERVED` + 10 consumption records seeded. | Passed | 2026-08-05 | Minh | Pending | | | Pending | | | BR-133 / ADR-M09-7 idempotency — a non-idempotent rerun during an incident would spam the whole school |

