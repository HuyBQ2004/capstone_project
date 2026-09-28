# Unit Test Case — MenuRecommendationService (MF05 Menu & Diet Management)

---

## Function: `suggest`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MENU-FUNC-006` | | Function Name | `MenuRecommendationService.suggest` |
| Created By | `Nguyễn Phương Thảo` | | Executed By | `Thao` |
| Lines of code | `90` | | Lack of test cases | `0` |
| Test requirement | `Delegate to Gemini AI via AiAssistPort to suggest a menu prioritizing in-stock ingredients and staying within the cost ceiling (BR-064, BR-156); on a 10s timeout, fail gracefully with MSG-073 without rolling back the caterer's manual draft (UC-075 AF1).` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `2` | `1` | `0` | `1` | `2` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | Menu `DRAFT` already has 2 dishes manually entered by the Caterer | O | |
| | `SFMS_AI_TIMEOUT_MS=10000` (`FX-027`) | O | |
| | `InventoryQueryPort` stub → 3 ingredients in stock, 1 batch quarantined | | O |
| | `costCeiling = 30000` (`FX-017`) | | O |
| `AiAssistPort` fake Input | Fake port delays 11s (> 10s timeout, `FX-026`) | O | |
| | Fake port returns ADR-000-schema JSON after 50ms (`FX-025`) | | O |
| **Confirm** | | | |
| Return | Draft menu **not** rolled back: still `DRAFT`, still holds the 2 manually entered dishes; subsequent `update()` still works (UC-075 AF1) | O | |
| | `AiMenuSuggestionRequested` event still published (for MF12 archival, BR-157) | O | |
| | Prompt sent to `AiAssistPort` contains the in-stock ingredient list + `costCeiling=30000` (verified via `ArgumentCaptor`); quarantined batch excluded (BR-064) | | O |
| | `AiSuggestionResponse` contains `nutritionAnalysis`, `ingredients[]`, `allergyFlags[]` | | O |
| | If suggestion has < 3 nutrition groups → warning `MSG-067` returned, **no** exception thrown (BR-062 is advisory only) | | O |
| Exception | `AiUnavailableException`, `errorCode == "MENU-073"`, HTTP 503 | O | |
| Log message | `MSG-073`: Gemini AI Service is currently unavailable. Please try again later. | O | |
| | `MSG-067`: AI menu analysis: Nutritional diversity is below the required 3-group minimum. (warning, non-blocking) | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | B | N |
| Passed/Failed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | | |

> UTCID mapping: UTCID01 = `MF05-TC-014`, UTCID02 = `MF05-TC-015`.
