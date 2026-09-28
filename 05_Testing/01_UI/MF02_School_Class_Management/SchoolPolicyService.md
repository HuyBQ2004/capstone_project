# Unit Test Case — SchoolPolicyService (MF02 School & Class Management)

---

## Function: `updateGeofence`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SCH-FUNC-004` | | Function Name | `SchoolPolicyService.update` / `getPolicy` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `55` | | Lack of test cases | `0` |
| Test requirement | `Validate geofence radius bounds between 50 and 500 meters (BR-028), enforce 200 meters default radius when unspecified, and configure school meal/attendance parameters.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `3` | `1` | `1` | `1` | `3` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Geofence Radius Input | Radius = 200 meters (valid default value) | O | | |
| | Radius = 49 meters (below lower bound < 50) | | O | |
| | Radius = 501 meters (exceeds upper bound > 500) | | | O |
| **Confirm** | | | | |
| Return | School config updated, `geofence_radius = 200` | O | | |
| Exception | `SCH-013`: Geofence radius must be between 50 and 500 meters (HTTP 400) | | O | O |
| DB Verification | CHECK `geofence_radius BETWEEN 50 AND 500` satisfied | O | | |
| Log message | `MSG-082`: School information updated successfully. | O | | |
| | `MSG-107`: Geofence radius must be between 50 and 500 meters. | | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | B | B |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |
