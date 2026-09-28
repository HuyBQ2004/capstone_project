# Unit Test Case — SpecialMenuService (MF05 Menu & Diet Management)

---

## Function: `update` / `createDraft` for `audience ≠ "ALL"` (special/supplementary meal menus)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MENU-FUNC-008` | | Function Name | `SpecialMenuService.update` / `MenuService.createDraft` (special menus) |
| Created By | `Nguyễn Phương Thảo` | | Executed By | `Thao` |
| Lines of code | `130` | | Lack of test cases | `0` |
| Test requirement | `Require a reason when a special/supplementary menu (audience != ALL) is added after the parent menu has already been published (semantic rule SUAT_BO_SUNG_KHONG_RO_NGUON_GOC); apply the correct status transition on edit (REJECTED -> PENDING_APPROVAL, unlike the parent menu); and enforce the {menuId}--{dietGroupId} id convention (ADR-M05-01).` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `3` | `1` | `2` | `0` | `3` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | Special menu `MENU-2026-1042--VEGETARIAN`, parent menu `PUBLISHED` (`FX-013`) | O | | |
| | Special menu `status=REJECTED` | | O | |
| | `FX-011` — `dietGroupId ∈ {VEGETARIAN, DIABETIC, HALAL, NUT_FREE, LACTOSE_FREE}` | | | O |
| Parent-menu status × special-menu status Input | Parent `PUBLISHED`, special `PENDING_APPROVAL`, `reason=null` | O | | |
| | Parent `PUBLISHED`, special `PENDING_APPROVAL`, `reason="Bổ sung sau khi thực đơn chung đã công bố"` | O | | |
| | Parent `DRAFT`, special `PENDING_APPROVAL`, any `reason` | O | | |
| | Parent `PUBLISHED`, special `APPROVED`, `approvedBy=null` | O | | |
| | Special menu edited while parent still `DRAFT` → expect special menu falls back to `DRAFT` | | O | |
| `id` / `dietGroupId` Input | `id="MENU-2026-1042--VEGETARIAN"`, `dietGroupId="VEGETARIAN"` (matching, correct `--` convention) | | | O |
| | `id="MENU-2026-1042-VEGETARIAN"` (single dash, malformed) | | | O |
| | `audience="VEGETARIAN"` sent with no `id` | | | O |
| | `id` matches convention but `dietGroupId` differs from the suffix after `--` | | | O |
| | `menuId` portion of `id` refers to a non-existent parent menu | | | O |
| **Confirm** | | | | |
| Return | Row (a) parent PUBLISHED/special PENDING_APPROVAL/no reason → 422; row (b) same but with reason → OK; row (c) parent DRAFT → 422; row (d) APPROVED without approvedBy → 422; status unchanged on all 422 rows | O | | |
| | Edited special menu (was `REJECTED`) → `status = PENDING_APPROVAL` (not `DRAFT`, unlike parent-menu edit behavior); parent still `DRAFT` case → special falls back to `DRAFT` | | O | |
| | (1) 201, `id` kept, `audience==dietGroupId=="VEGETARIAN"`; (3) 201 with server-generated `id` in correct `{menuId}--{dietGroupId}` form; parent menu (`audience="ALL"`) is exempt from this convention and `dietGroupId` must be `null` | | | O |
| Exception | 422 on rows (a), (c), (d) of the reason/approval decision table | O | | |
| | 422 on malformed `id` (2 dashes expected); 422 when `id` and `dietGroupId` are inconsistent (4); 404 when `menuId` portion does not exist (rule `SUAT_RIENG_TRO_THUC_DON_KHONG_TON_TAI`) | | | O |
| Log message | Message references semantic rule `SUAT_BO_SUNG_KHONG_RO_NGUON_GOC` | O | | |
| | Message references `ADR-M05-01` id convention `{menuId}--{dietGroupId}` | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | N | A |
| Passed/Failed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

> UTCID mapping: UTCID01 = `MF05-TC-018`, UTCID02 = `MF05-TC-020`, UTCID03 = `MF05-TC-026`.
