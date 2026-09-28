# Unit Test Case — SafetyMonitoringService (MF07 Food Safety Inspection & Monitoring)

---

## Function: `KitchenInspectionService` — manual storage-temperature alert

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `INSP-FUNC-021` | | Function Name | `KitchenInspectionService.recordManualTemperature` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 30 | | Lack of test cases | 0 |
| Test requirement | `MSG-055`/`MSG-059` warnings must fire only for manually-entered out-of-range storage temperature readings; no automated IoT monitoring job may exist (BR-089 deprecated 11/07/2026, L7/CF-3). (source: MF07-TC-021) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 2 | 1 | 0 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Manually entered storage temperature | Within acceptable range | O | | |
| | Out of acceptable range | | O | |
| Codebase structural check | No `@Scheduled` job referencing temperature/fridge exists under `com.sfms.mf07` | | | O |
| **Confirm** | | | | |
| Return | No warning raised | O | | |
| | HTTP 200 (warning, not an error) | | O | |
| Log message | `MSG-055`: "High temperature alert in inventory storage area: [Temperature]°C." with `[Temperature]` = entered value | | O | |
| Structural assertion | `grep -rn "@Scheduled" ... \| grep -i "temp\|fridge"` yields no output; no `fridge_temp_logs` table exists (L8/CF-4) | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | A | N |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

Note: `MSG-059` ("Retention refrigerator door left open or temperature fluctuation detected.") follows the same manual-entry rule and is covered by the same UTCID01/UTCID02 pattern for the refrigerator-door reading variant.
