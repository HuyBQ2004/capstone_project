# Unit Test Case — MenuService (MF05 Menu & Diet Management)

---

## Function: `update` / `delete` / `createDraft` — editability, derived fields, id/date handling

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MENU-FUNC-009` | | Function Name | `MenuService.assertEditable` / `update` / `delete` / `createDraft` + `MenuMapper` |
| Created By | `Nguyễn Phương Thảo` | | Executed By | `Thao` |
| Lines of code | `260` | | Lack of test cases | `0` |
| Test requirement | `Lock APPROVED/PUBLISHED menus against edit/delete for every role (BR-067) while allowing DRAFT/REJECTED edits that fall back to DRAFT; recompute derived fields (dishes, allergyConflicts) at read time regardless of client-sent values (ADR-M05-02, C11); return dates in ISO-8601 (D4) while still parsing dd/MM/yyyy input; accept a client-supplied id with 409-on-duplicate and server auto-generation when absent (D1, D5, L11); and reject any write-vs-read DTO key mismatch (D12).` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `6` | `2` | `3` | `1` | `6` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | |
| Precondition | Menu `status ∈ {APPROVED, PUBLISHED}` (`FX-002`) | O | | | | | |
| | Menu `status = REJECTED`, `rejectReason` set | | O | | | | |
| | Payload sends `dishes: 99`, `allergyConflicts: 0`; actual menu has 4 `MenuDish` rows + 1 real allergy conflict (`FX-006`) | | | O | | | |
| | Payload has `submittedAt` in 2 formats: `"28/07/2026"` and `"2026-07-28T09:00:00Z"` | | | | O | | |
| | `POST` with `id="MENU-2026-1042"` (client-supplied) | | | | | O | |
| | Payload sends `dishes` as an array (wrong type) plus derived fields (`allergyConflicts`, `createdBy`, `approvedAt`, `costCeiling`) that should be server-controlled | | | | | | O |
| Operation Input | `update()` / `delete()` invoked, incl. by `SYS_ADMIN` role | O | | | | | |
| | `update()` with new `blocksData` | | O | | | | |
| | `createDraft()` then `findById()` read-back | | | O | | | |
| | `PUT` (parse) then `GET` (read back format) | | | | O | | |
| | Repeat `POST` with the **same** `id`; then a 3rd `POST` with no `id`; 2 concurrent `POST`s with no `id` | | | | | O | |
| | 2nd `POST` with derived/system fields spoofed in the body | | | | | | O |
| **Confirm** | | | | | | | |
| Return | — (all cases in this column are blocked, see Exception row) | O | | | | | |
| | `status` returns to `DRAFT`; `rejectReason` preserved (approval history is never erased); message `MSG-074` | | O | | | | |
| | Response `dishes==4`, `allergyConflicts==1` (recomputed, not 99/0); no `dishes`/`allergy_conflicts` column exists in `menus` table; `estimatedCost`/`costCeiling` saved exactly as sent (C11) | | | O | | | |
| | Both input formats parse to the same `Instant`; response uses ISO-8601 (`submittedAt`, `approvedAt`, `createdAt`, `date`); `menuDateIso` stays ISO; DB column `submitted_at` is `timestamptz`; no `@JsonFormat` left in source | | | | O | | |
| | 1st `POST` → 201 with `id="MENU-2026-1042"` (DB `code`); 3rd `POST` → 201 with server-generated `code` (`MENU-<year>-<n>`); 2 concurrent no-id `POST`s get 2 distinct codes (`INSERT … RETURNING`) | | | | | O | |
| | `dishes` in response = `2` (computed from `blocksData`), never the raw array; `created_by_supplier_id` from JWT, not body; `approved_at=NULL`; `cost_ceiling` copied from `school_config`, not body; every writable field has matching JSON name on both request and response DTOs | | | | | | O |
| Exception | `MenuLockedException`, `errorCode=="MENU-067"`, HTTP 409, for both `update()` and `delete()`, on every role | O | | | | | |
| | 2nd `POST` with duplicate `id` → 409 (`MENU-409`), no 2nd record created, original not overwritten | | | | | O | |
| Log message | `MSG-074`: Menu updated successfully. | | O | | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|
| Type (N/A/B) | A | N | A | N | B | A |
| Passed/Failed | Passed | Passed | Passed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | |

> UTCID mapping: UTCID01 = `MF05-TC-005`, UTCID02 = `MF05-TC-006`, UTCID03 = `MF05-TC-023`, UTCID04 = `MF05-TC-024`, UTCID05 = `MF05-TC-025`, UTCID06 = `MF05-TC-027`.
