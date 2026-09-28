# Unit Test Case — IngredientDeclarationValidator (MF05 Menu & Diet Management)

---

## Function: `validate` (invoked from `MenuWorkflowService.submit()`)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MENU-FUNC-004` | | Function Name | `IngredientDeclarationValidator.validate` |
| Created By | `Nguyễn Phương Thảo` | | Executed By | `Thao` |
| Lines of code | `50` | | Lack of test cases | `0` |
| Test requirement | `Require every dish in a menu to have declared its full ingredient list and potential allergens before the menu can be submitted for approval (BR-069); distinguish "declared but empty allergens" from "not declared".` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `1` | `0` | `1` | `0` | `1` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Precondition | Menu `DRAFT` contains 1 dish with a complete BOM (`FX-008`) | O |
| | Menu also contains dish `dish-no-bom` (`FX-010`) with **zero** rows in `dish_ingredients` | O |
| Dish declaration Input | Dish has BOM rows but `manualAllergens` is empty (declared, just no allergen) | O |
| | Dish has no BOM rows at all (never declared, flag `allergensDeclared == false`) | O |
| **Confirm** | | |
| Return | Dish with BOM but empty `manualAllergens` does **not** trigger the exception (empty ≠ undeclared) | O |
| Exception | `IncompleteDeclarationException`, `errorCode == "MENU-069"`, HTTP 422, message names the exact missing dish (`[Dish Name]`), not the whole dish list; `status` stays `DRAFT` | O |
| Log message | `MENU-069` message references BR-069 declaration requirement | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | A |
| Passed/Failed | Passed |
| Executed Date | 2026-08-05 |
| Defect ID | |

> UTCID mapping: UTCID01 = `MF05-TC-011`.
