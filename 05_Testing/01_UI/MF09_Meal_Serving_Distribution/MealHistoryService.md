# Unit Test Case — MealHistoryService (MF09 Meal Serving & Distribution)

---

## Function: `listForChild`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SRV-FUNC-008` | | Function Name | `MealHistoryService.listForChild` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `90` | | Lack of test cases | `0` |
| Test requirement | BR-201: a parent may only read data for students linked to their account (max 5); ADR-M09-6 — out-of-scope access returns **403**, never 404 (to avoid leaking record existence, PDPD); `kcal` (total and per-dish) must be computed **at runtime** from `Dish` (MF05) via the frozen `dish_id`, never stored as a column (ADR-M09-2, C3). Covers `MF09-TC-013`, `MF09-TC-016`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `4` | `2` | `1` | `1` | `4` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | Fixture `FX-014`: Parent `P1` linked to `HS-3A-001` + `HS-1B-007` (2/5, BR-201), **not** linked to `HS-3B-002` | O | O | O | |
| | Fixture `FX-016`: kcal per `dishId` (`com-trang=260`, `ca-basa-sot-ca-chua=190`, `canh-rau-ngot-thit-bam=75`, `gia-xao=60`) | | | | O |
| | 1 `SERVED` session with 4 dishes | | | | O |
| Input | `listForChild("HS-1B-007", filter, parentP1)` — linked child | O | | | |
| | `listForChild("HS-3B-002", filter, parentP1)` — child exists but not linked | | O | | |
| | `listForChild("HS-KHONG-TON-TAI", filter, parentP1)` — student code does not exist | | | O | |
| | `listForChild("HS-3A-001", ..., parentP1)` — kcal projection | | | | O |
| **Confirm** | | | | | |
| Return | `200`; every item has `studentId = "HS-1B-007"` | O | | | |
| | `items[0].kcal == 695` (260+190+75+60); each `dishes[]` element carries its own `kcal` and `dishId != null` | | | | O |
| | Changing the kcal stub for `com-trang` and calling again changes the total accordingly (proves runtime computation, not a stored column) | | | | O |
| Exception | `ForbiddenException SRV-005` (HTTP 403) — child not linked to this parent | | O | | |
| | `ForbiddenException SRV-005` (HTTP 403) — **identical** to UTCID02 (not 404/MSG-137): from the outside, a non-existent student code is indistinguishable from one that exists but is not linked | | | O | |
| Log message | `MSG-005`: Access denied: You do not have permission to access this resource. | | O | O | |
| Side-effect | `dishes[]` come from the frozen `serving_dishes` snapshot, **not** recomputed from the current menu | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | A | B | N |
| Passed/Failed | Passed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

> Note (UTCID03): This is the critical anti-IDOR-oracle assertion — if this ever returns 404/MSG-137 while UTCID02 returns 403, the discrepancy becomes an oracle to enumerate real student codes school-wide (child PII leak). Both must return the exact same 403 body.
