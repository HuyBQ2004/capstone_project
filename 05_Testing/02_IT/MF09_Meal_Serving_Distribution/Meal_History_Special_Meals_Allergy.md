# Integration Test Case — Meal History, Special Meals & Allergy Alerts (MF09 Meal Serving & Distribution)

---

## Feature: Meal History, Special Meals & Allergy Alerts

### 1. General Information

| Field | Value |
|---|---|
| Feature | Meal History, Special Meals & Allergy Alerts |
| Test requirement | Verify that parent meal history is a frozen, non-rewritable projection (ADR-M09-1/ADR-M09-2) with no backing `meal_history` table, and that the MF11 investigation query ("which students ate dish X?") returns exactly the right set of students via `serving_dishes.dish_id`. |
| Number of TCs | 3 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 3 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 3 | 0 |
| Round 3 | 0 | 0 | 3 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **GET /api/v1/students/{code}/meal-history — Frozen Projection** | | | | | | | | | | | | | | |
| MF09-TC-INT-003 | Test that meal history is not retroactively rewritten when a student's allergy profile changes after a meal was already served. | 1. `FX-002`: `DSTU-0706A-002` already `SERVED`, includes dish "Canh bí đỏ nấu tôm"; `HS-3A-002` has no allergy yet.<br>2. Publish event `AllergyDeclarationVerified(HS-3A-002, allergen="Tôm")`.<br>3. `GET /students/HS-3A-002/meal-history?dateFrom=2026-07-06&dateTo=2026-07-06`.<br>4. Read `serving_dishes` for that record.<br>5. Check a future `PENDING` session of the same student. | 1. Step 3 still shows `"Canh bí đỏ nấu tôm"` — not retroactively substituted.<br>2. Step 4: 4 `serving_dishes` rows unchanged byte-for-byte (compare `created_at` + content before/after).<br>3. Step 5: the future `PENDING` session **is** recalculated with a substitute meal (positive branch of the handler). | `FX-002` seeded; `HS-3A-002` has no allergy declared initially. | Passed | 2026-08-05 | Minh | Pending | | | Pending | | | ADR-M09-1 — frozen history is the entire reason this ADR exists |
| MF09-TC-INT-005 | Test that parent meal history is a runtime projection joined from 4 tables, and that no `meal_history` table exists. | 1. `GET /students/HS-3A-002/meal-history?dateFrom=2026-07-06&dateTo=2026-07-06`.<br>2. `SELECT to_regclass('public.meal_history')`.<br>3. Verify `CatererAssignmentPort` is not invoked during the read. | 1. Step 1: `items[0].dishes` has exactly **4** elements, each with `dishId != null`; `items[0].consumption` matches `meal_consumption_records`; `items[0].kcal` = sum of the 4 dishes' kcal (runtime); `caterer="Cty TNHH Suất ăn An Bình"`, `catererPartnerId="CAT-001"` read from the frozen snapshot columns; `date`/`servedAt` are ISO (D4); `meal = "LUNCH"` (D6, not `"Bữa trưa"`).<br>2. Step 2 returns **NULL** — `meal_history` table does not exist.<br>3. Step 3: **0** calls to the MF04 port. | `FX-002` (SERVED, 4 dishes) + `FX-007` (consumption) + `FX-016` (kcal) + `FX-026` (no `MEAL_HISTORY` array seeded). | Passed | 2026-08-05 | Minh | Pending | | | Pending | | | CF-M09-7 / ADR-M09-2 — projection, not a second source of truth |
| **MF11 Traceability Query** | | | | | | | | | | | | | | |
| MF09-TC-INT-018 | Test the "which students ate dish X on date Y?" investigation query used by MF11, and the allergy-conflict flag on today's menu. | 1. `findServedByDishId("canh-bi-do-nau-tom", 2026-07-06, 2026-07-06)`.<br>2. `EXPLAIN` the query.<br>3. `GET /allergy-alerts?date=2026-07-06&mealType=LUNCH&class=3A`. | 1. Step 1 returns **exactly 4** records (`SERVED` only, from `FX-002`+`FX-005`); `DSTU-0706A-001` (`PENDING`, has a substitute meal instead) is **excluded** — proving `serving_dishes` distinguishes who actually ate the flagged dish.<br>2. Step 2 uses index `ix_sdish_dish`, **no** sequential scan.<br>3. Step 3: `conflictWithTodayMenu = true` for `HS-3A-001`; response does **not** contain code `MSG-051` (L20 — MSG-051 is not in traceability §1.9, so only the flag is returned). | `FX-002` + `FX-005` (4 `SERVED` records with dish `canh-bi-do-nau-tom`); `DSTU-0706A-001` `PENDING` with a substitute dish. | Passed | 2026-08-05 | Minh | Pending | | | Pending | | | MF11 traceability / ADR-M09-1 — this query is the sole answer to "who ate the suspect dish" |

