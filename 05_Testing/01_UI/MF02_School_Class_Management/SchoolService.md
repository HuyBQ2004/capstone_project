# Unit Test Case — SchoolService (MF02 School & Class Management)

---

## Function: `register` / `update` / `unlockCoordinate`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SCH-FUNC-003` | | Function Name | `SchoolService.register` / `update` / `unlockCoordinate` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `130` | | Lack of test cases | `0` |
| Test requirement | `Validate GPS latitude/longitude bounds (BR-028), tax ID format, enforce coordinate locking rules (`coordinateLocked`), require written justification for coordinate unlock (BR-028), and compute dynamic counts at runtime without derived columns.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `5` | `1` | `3` | `1` | `5` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Latitude / Longitude Input | Valid GPS coords (`lat = 21.031200`, `lng = 105.763800`) | O | | O | O | O |
| | Invalid Latitude (`lat = 95.000000` > 90) | | O | | | |
| Coordinate Lock State | `coordinateLocked = true` | | | O | O | |
| User Role | System Admin | O | O | | O | O |
| | School Admin | | | O | | |
| Unlock Request | Provided written justification ("Relocation of main gate") | | | | O | |
| **Confirm** | | | | | | |
| Return | School registered / updated successfully | O | | | O | O |
| Exception | `SCH-013`: Latitude must be between -90 and 90 (HTTP 400) | | O | | | |
| | `SCH-013`: Coordinates are locked and cannot be updated (HTTP 403) | | | O | | |
| Audit Verification | Action `UNLOCK_COORDINATE` logged with justification | | | | O | |
| Derived Field Verification | Response `classes`, `staff`, `students` computed via SQL COUNT | O | | | | O |
| Log message | `MSG-082`: School information updated successfully. | O | | | O | O |
| | `MSG-107`: Latitude must be between -90 and 90 degrees. | | O | | | |
| | `MSG-108`: Coordinates are locked and cannot be updated. | | | O | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | N | B | A | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |
