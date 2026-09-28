# Integration Test Case — Kitchen Hygiene Checklist (MF07 Food Safety Inspection & Monitoring)

---

## Feature: Kitchen Hygiene Checklist — Scoring & Soft-Delete

### 1. General Information

| Field | Value |
|---|---|
| Feature | Kitchen Hygiene Checklist — Scoring & Soft-Delete |
| Test requirement | Verify `POST /api/v1/kitchen-inspections` recomputes the hygiene score server-side (BR-100/BR-104) and never locks the kitchen on a low score (descoped per L6/CF-2), and that `DELETE /checklist-groups/{code}` is soft-delete only, preserving historical `checklist_item_results` (BR-210). |
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
| **POST /api/v1/kitchen-inspections — Server-Side Score Recompute, No Kitchen Lock** | | | | | | | | | | | | | | |
| MF07-TC-INT-009 | Test that a kitchen inspection with 11/20 passed items is scored FAILED with `score=55` computed server-side (ignoring a spoofed client score), warns MSG-093, and does NOT block meal serving or lock the kitchen (descoped behavior). | 1. `POST /kitchen-inspections` with `itemResults` 11/20 passed AND `score: 100` (deliberately spoofed).<br>2. Read DB + response.<br>3. `GET /inspections/serving-gate` for the same day's meal (inspection session PASSED).<br>4. Check for absence of any "kitchen lock/quarantine" table/column. | 1. HTTP 201.<br>2. DB `score = 55` (recomputed, not 100), `result = 'FAILED'`; response carries warning MSG-093; event `KitchenScoreBelowThreshold` is published.<br>3. `allowed: true` — a low kitchen score never blocks meal serving (C6, cross-ref `MF07-TC-INT-003` case b).<br>4. No column/table matching `%quarantine%` exists on `kitchen_inspections`.<br>5. `staff_health_valid = false` is persisted (BR-103) but does not auto-block anything. | Kitchen inspection checklist seeded; inspection session for the day is PASSED. | Passed | 2026-08-05 | Thao | Pending | | | Pending | | | ADR-M07-03, MSG-093; L6/CF-2 (descoped), L21/CF-26 |
| **DELETE /api/v1/checklist-groups/{code} — Soft-Delete Preserves History** | | | | | | | | | | | | | | |
| MF07-TC-INT-010 | Test that deleting a checklist group is a soft-delete (`is_active=false`) requiring confirmation, and that historical `checklist_item_results` referencing it are never cascade-deleted. | 1. Seed `FX-012` + a kitchen inspection whose `checklist_item_results` reference `CLG-3`.<br>2. `DELETE /checklist-groups/CLG-3` → assert.<br>3. Retry with `?confirmed=true` → assert.<br>4. Read the old session; create a new session. | 1. First delete attempt → HTTP 409 + MSG-094; `is_active` still `true`.<br>2. Confirmed delete → HTTP 200; `is_active = false`.<br>3. `checklist_item_results` row count for `CLG-3` is unchanged (no CASCADE DELETE).<br>4. The old session still renders every original item; a newly created session no longer shows the hidden group.<br>5. No `DELETE FROM checklist_groups` statement is ever executed (verified via SQL log). | `FX-012` checklist seeded; a kitchen inspection referencing `CLG-3` exists. | Passed | 2026-08-05 | Thao | Pending | | | Pending | | | BR-210, MSG-094, ADR-M12-06 |
