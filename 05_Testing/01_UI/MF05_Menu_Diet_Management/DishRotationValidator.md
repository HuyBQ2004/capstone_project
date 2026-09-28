# Unit Test Case — DishRotationValidator (MF05 Menu & Diet Management)

---

## Function: `validate`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MENU-FUNC-003` | | Function Name | `DishRotationValidator.validate` |
| Created By | `Nguyễn Phương Thảo` | | Executed By | `Thao` |
| Lines of code | `45` | | Lack of test cases | `0` |
| Test requirement | `Block a main dish from being served more than twice a week within the same grade (MSG-075), counted via MenuRepository.countDishOccurrencesInWeek(), excluding menus with status REJECTED.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `1` | `0` | `0` | `1` | `1` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Precondition | 2 menus in the same week, same `grade="Khối 3"`, same `dishId="ca-basa-sot-ca-chua"`, `status ≠ REJECTED` | O |
| | Menu being drafted also contains the same dish (3rd occurrence) | O |
| `countDishOccurrencesInWeek()` stub Input | Returns `2` (already occurred twice) | O |
| | Returns `1` (already occurred once) | O |
| **Confirm** | | |
| Return | With 2 prior occurrences + 3rd attempt → validation fails | O |
| | With 1 prior occurrence + 2nd attempt → validation passes (no exception; "twice a week" is the valid limit) | O |
| | `REJECTED` menus excluded from the weekly count (JPQL `m.status <> 'REJECTED'`) | O |
| Exception | `DishRotationException`, `errorCode == "MENU-075"`, HTTP 409, message includes `[Dish Name] = "Cá basa sốt cà chua"` | O |
| Log message | `MSG-075`: Dish rotation limit exceeded: [Dish Name] cannot be served more than twice a week. | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | B |
| Passed/Failed | Passed |
| Executed Date | 2026-08-05 |
| Defect ID | |

> UTCID mapping: UTCID01 = `MF05-TC-010`.
