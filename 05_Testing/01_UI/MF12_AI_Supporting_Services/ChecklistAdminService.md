# Unit Test Case — ChecklistAdminService (MF12 AI & Supporting Services)

> Group (C) — Kitchen checklist administration. Layer `com.sfms.mf12.checklist.*`. Source: `TestSpec-MF12_AI_Supporting_Services.md` §4.1 `MF12-TC-019`.
>
> ⚠️ **Scope note:** `MF12-TC-017` (school-scope enforcement from JWT) and `MF12-TC-018` (soft-delete of an UNUSED checklist item) are marked `@Disabled("M12-D02 — thuộc MF07")` in the TestSpec as of 2026-08-04 — ownership of `checklist_groups`/`checklist_items` and the `/api/v1/checklist-groups` endpoint moved to MF07 (already merged there, `M07-D10`). They are intentionally **not** reproduced in this MF12 test suite; see `05_Testing/01_UI/MF07_Food_Safety_Inspection/` for their MF07 equivalents. Only `MF12-TC-019` (deleting an ALREADY-REFERENCED checklist item) remains active/unit-testable under MF12 per the TestSpec.

---

## Function: `ChecklistAdminService.delete()` — item already referenced by saved results

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-019` | | Function Name | `ChecklistAdminService.delete` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 35 | | Lack of test cases | 0 |
| Test requirement | Deleting a checklist item/group that is already referenced by saved `checklist_item_results` must warn with MSG-094/`AIS-094` (HTTP 409) and require `?confirmed=true` before proceeding; it must never hard-delete historical data (BR-210, ADR-M12-06). Must stay consistent with `MF07-TC-016`/`MF07-TC-INT-010` (L7/CF-3). (source: MF12-TC-019) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 3 | 0 | 0 | 1 | 1 | 1 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | `FX-012` checklist group `CLG-3` + `FX-014` (result rows referencing `CLI-3-8`) | O | O | O |
| Request | `DELETE /checklist-groups/CLG-3` without `confirmed` | O | | |
| | Same, retried with `?confirmed=true` | | O | |
| | Re-read old inspection session (MF07) after soft-delete | | | O |
| **Confirm** | | | | |
| Exception | HTTP 409, `$.error.code == "AIS-094"`, message = MSG-094 verbatim; `is_active` STILL `true` (warning, not a hard block) | O | | |
| Return | HTTP 200, `is_active = false` after confirmation | | O | |
| | `checklist_item_results` row count UNCHANGED (no cascade delete) | | O | O |
| | Old inspection session still renders every item; new sessions no longer offer the hidden group | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | N | B |
| Passed/Failed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

Note: `AIS-094` is a **confirmation warning**, not a permanent block — after `confirmed=true` the same request must succeed. Do not implement this as a permanent 403.
