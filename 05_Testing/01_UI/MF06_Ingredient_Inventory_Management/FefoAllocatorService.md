# Unit Test Case — FefoAllocatorService (MF06 Ingredient & Inventory Management)

---

## Function: `FefoAllocator.allocate(ingredientId, needed, candidates, remaining, today)`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF06-FUNC-001` | | Function Name | `FefoAllocator.allocate()` + `InventoryBatch.isLocked(today)` |
| Created By | `Bui Quang Huy` | | Executed By | `Huy` |
| Lines of code | `TBD` | | Lack of test cases | `TBD` |
| Test requirement | `Allocate stock strictly by First-Expired-First-Out — the candidate with the earliest expiryDate is consumed first regardless of input order (BR-075, ADR-M06-4); any batch whose isLocked(today) is true — because it is EXPIRED (BR-205) or under quarantine != null (BR-149, ADR-M06-2) — must be excluded from the candidate pool entirely, not merely hidden in the UI, and the unmet quantity must surface as shortfall (BR-077) instead of silently drawing from a locked batch. allocate() must be a pure function that does not mutate the input candidate list.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `3` | `1` | `2` | `0` | `3` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | `FX-CLOCK`: `today = 2026-07-06` | O | O | O |
| | 3 batches of `ING-THIT-LON-VAI`, all `quarantine=null`, all unexpired: `BATCH-GF-4411` exp `2026-07-08` qty `17.2kg`, `BATCH-MP-8842` exp `2026-07-10` qty `40kg`, `L3` exp `2026-08-05` qty `30kg`, passed to `allocate()` **out of order** (`L3, L1, L2`) | O | | |
| | `FX-BATCH-EXPIRED` = `BATCH-GF-4388` (`ING-DAU-AN`, exp `2026-06-28`, expired 8 days), `40L` remaining; usable candidate `FX-BATCH-USABLE-OIL` = `BATCH-GF-4420` (exp `2027-01-01`), only `10L` remaining | | O | |
| | `FX-BATCH-QUAR-NOINC` = `BATCH-DD-1080` (`ING-TOM-SU`, `18kg`, exp `2026-09-28` — 84 days left, still unexpired), `quarantine != null`; no other shrimp batch in stock | | | O |
| Operation Input | `allocate(ING_THIT, needed=20)` on the reversed candidate list | O | | |
| | `allocate(ING_DAU_AN, needed=15)` — deliberately more than the 10L usable stock but less than the 50L total including the expired batch | | O | |
| | `allocate(ING_TOM_SU, needed=10)` on the quarantined-only candidate list | | | O |
| **Confirm** | | | | |
| Return | `lines()` = `["BATCH-GF-4411" take=17.2, "BATCH-MP-8842" take=2.8]` (expiry-ascending order, computed on the sorted copy — not input order); `shortfall()=0`; `L3` absent from `lines()` | O | | |
| | `lines()` = `["BATCH-GF-4420" take=10]`; `shortfall()=5`; `BATCH-GF-4388` absent from **both** `lines()` and any candidate list returned to the caller | | O | |
| | `lines()` is empty; `shortfall()=10` — the entire demand becomes shortfall | | | O |
| Exception | — (no exception; failure mode is `shortfall`, not a thrown error) | | | |
| Log message | — (no logging asserted at unit level; downstream `MSG-018` is asserted at `M06-TC-INT-011`) | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | A | A |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

> UTCID mapping: UTCID01 = `M06-TC-001`, UTCID02 = `M06-TC-002`, UTCID03 = `M06-TC-003`.
