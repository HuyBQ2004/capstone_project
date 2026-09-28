# Unit Test Case — ClassroomService (MF02 School & Class Management)

---

## Function: `createClassroom` / `updateClassroom` / `assignHomeroomTeacher`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SCH-FUNC-001` | | Function Name | `ClassroomService.create` / `update` / `assignTeacher` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `140` | | Lack of test cases | `0` |
| Test requirement | `Validate grade bounds [1..12] (BR-024), capacity limit <= 50 (BR-025), new capacity >= current enrollment (BR-219), active academic year existence (BR-192), and homeroom teacher uniqueness & audit trail (BR-020, BR-026).` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `6` | `1` | `3` | `2` | `6` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | |
| Precondition | School active and active Academic Year exists | O | O | O | | O | O |
| | School has NO active Academic Year | | | | O | | |
| Grade Input | Grade level = 3 (within [1..12]) | O | O | O | O | O | O |
| | Grade level = 13 (out of bounds > 12) | | | | | | |
| Capacity Input | Capacity = 45 (within <= 50 limit) | O | | | O | O | O |
| | Capacity = 51 (exceeds 50 limit) | | O | | | | |
| | Capacity = 30 (less than current enrollment 38) | | | O | | | |
| Homeroom Teacher | Teacher assigned to another class in same AY | | | | | O | |
| | Teacher unassigned or assigned to different AY | O | O | O | O | | O |
| **Confirm** | | | | | | | |
| Return | Classroom created / updated successfully | O | | | | | O |
| Exception | `SCH-024`: Grade level must be between 1 and 12 (HTTP 422) | | | | | | |
| | `SCH-010`: Capacity exceeds maximum limit of 50 (HTTP 422) | | O | | | | |
| | `SCH-012`: Capacity cannot be less than current student count (HTTP 422) | | | O | | | |
| | `SCH-004`: Cannot create classroom without active academic year (HTTP 400) | | | | O | | |
| | `SCH-011`: Teacher already assigned to another class in this AY (HTTP 409) | | | | | O | |
| State Verification | Reassigning teacher unlinks old class & creates audit log | | | | | | O |
| Log message | `MSG-032`: Classroom created successfully. | O | | | | | |
| | `MSG-109`: Grade level must be between 1 and 12. | | | | | | |
| | `MSG-010`: Classroom capacity exceeds the maximum limit of 50. | | O | | | | |
| | `MSG-140`: Capacity cannot be less than current student count. | | | O | | | |
| | `MSG-011`: Teacher is already assigned to another class. | | | | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|
| Type (N/A/B) | N | B | B | A | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | |
