# Unit Test Case — DietCoverageValidator (MF05 Menu & Diet Management)

---

## Function: `findGaps` / `validate`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MENU-FUNC-005` | | Function Name | `DietCoverageValidator.findGaps` |
| Created By | `Nguyễn Phương Thảo` | | Executed By | `Thao` |
| Lines of code | `70` | | Lack of test cases | `0` |
| Test requirement | `Block submission when a grade has students in a diet group (e.g. Vegetarian) with no matching special menu covering them (BR-065), reading gap data via SpecialMealLookupPort (mocked, FX-014b), while not flagging diet groups that have no students at all.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `1` | `0` | `1` | `0` | `1` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Precondition | `DIET_GROUPS` seeded incl. `VEGETARIAN` (`matchDiet="Vegetarian"`, `FX-011`) | O |
| | ≥ 1 student in class 3A with `specialDiet = "Vegetarian"` (`FX-012`) | O |
| | Menu Khối 3, `mealType=LUNCH`, `audience="ALL"`, all-meat dishes, no `audience="VEGETARIAN"` menu exists for same date/meal | O |
| Coverage Input | No matching special menu for the `VEGETARIAN` group on that date/meal | O |
| | A `audience="VEGETARIAN"` menu is added for the same date/meal | O |
| | Diet group with zero students in the grade | O |
| **Confirm** | | |
| Return | `findGaps()` returns exactly 1 `DietGap`: `group.id=="VEGETARIAN"`, `mealType=="LUNCH"`, `students >= 1` | O |
| | After adding the covering `VEGETARIAN` menu, `findGaps()` returns empty and `submit()` succeeds | O |
| | Diet group with no students in the grade never produces a gap (rule `SUAT_RIENG_KHONG_CO_NGUOI_AN`) | O |
| Exception | `DietCoverageException`, `errorCode == "MENU-065"`, HTTP 422, message names `[Diet Group]` + `[Grade]` | O |
| Log message | `MENU-065` message references BR-065 mandatory special-meal-plan requirement | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | A |
| Passed/Failed | Passed |
| Executed Date | 2026-08-05 |
| Defect ID | |

> UTCID mapping: UTCID01 = `MF05-TC-012`.
