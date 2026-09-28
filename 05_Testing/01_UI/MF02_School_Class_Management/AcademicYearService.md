# Unit Test Case — AcademicYearService (MF02 School & Class Management)

---

## Function: `activate` / `close` / `createAcademicYear`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SCH-FUNC-002` | | Function Name | `AcademicYearService.activate` / `close` / `create` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `115` | | Lack of test cases | `0` |
| Test requirement | `Enforce single ACTIVE academic year per school (BR-191), support DRAFT parallel state, copy class structure without students on creation (BR-194), set classrooms read-only upon closing (BR-193), and reject client-supplied duplicate IDs (D5).` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `4` | `1` | `3` | `0` | `4` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Target School State | School A has an existing ACTIVE Academic Year (`AY-2025`) | O | O | O | O |
| Operation | Activate another DRAFT Academic Year for School A (`AY-2026`) | O | | | |
| | Create new Academic Year with `copyStructure = true` | | O | | |
| | Close existing ACTIVE Academic Year (`AY-2025`) | | | O | |
| | Create Academic Year with duplicate client ID (`AY-2025`) | | | | O |
| **Confirm** | | | | | |
| Return | Previous year deactivated, new year activated (`status = ACTIVE`) | O | | | |
| | New DRAFT year created, class templates copied with 0 students | | O | | |
| | Academic year closed (`status = CLOSED`), all classes set `readOnly = true` | | | O | |
| Exception | `SCH-409`: A record with this value already exists (HTTP 409) | | | | O |
| DB Verification | Single ACTIVE year constraint per school maintained in DB | O | O | O | O |
| Log message | `MSG-083`: Academic year activated successfully. | O | | | |
| | `MSG-084`: Academic year closed successfully. | | | O | |
| | `MSG-139`: A record with this value already exists. | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | A | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |
