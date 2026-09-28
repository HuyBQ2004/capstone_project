# Unit Test Case — Manual Template (based on Report5_Unit Test.xls)

> Copy the block below for each **Function** to be tested. Fill in manually following the guide at the bottom.
> Reference example: `GetListAcademicYear` (see original sample screenshot, 3 UTCIDs: normal / abnormal / boundary).

---

## Function: `<FunctionName>`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `<FunctionCode>` | | Function Name | `<FullFunctionName>` |
| Created By | `<AuthorName>` | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | `<Brief description of the requirement tested by this function>` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<# Passed>` | `<# Failed>` | `<# Untested>` | `<# Normal>` | `<# Abnormal>` | `<# Boundary>` | `<Total>` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Precondition | `<precondition 1>` | | | | | |
| | `<precondition 2>` | | | | | |
| `<InputVariable1>` (e.g. SchoolId) | `<value 1>` (e.g. non-existent GUID) | | | | | |
| | `<value 2>` | | | | | |
| `<InputVariable2>` (e.g. Id) | `<value 1>` | | | | | |
| **Confirm** | | | | | | |
| Return | `<return value 1>` (e.g. T) | | | | | |
| | `<return value 2>` (e.g. F) | | | | | |
| Exception | `<ExceptionName>` | | | | | |
| Log message | `<log message 1>` | | | | | |
| | `<log message 2>` | | | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | `<N/A/B>` | `<N/A/B>` | `<N/A/B>` | | |
| Passed/Failed | `<P/F>` | `<P/F>` | `<P/F>` | | |
| Executed Date | `<yyyy-mm-dd>` | `<yyyy-mm-dd>` | `<yyyy-mm-dd>` | | |
| Defect ID | `<if Failed, defect tracker ID>` | | | | |

---

## Filling Guide (condensed from the original Guideline sheet)

1. **Each UTCID = one test case**, formed by a specific combination of the condition values chosen in section 3.
2. **Precondition**: state(s) that must exist before running the case (e.g. system is running, user is authenticated...).
3. **Input values**: cover all 3 types — Normal (typical value), Boundary (upper/lower limit value), Abnormal (invalid/error-triggering value).
4. **Confirm**: the expected outcome used to judge Pass/Fail — can be a return value, a thrown exception, or a log message.
5. Mark **O** at the intersection of (condition/expected outcome) and (UTCID) whenever that condition/outcome applies to that case.
6. Fill section 4 **after actually running the test**: Type must match the N/A/B classification chosen in section 3, Passed/Failed is the actual outcome, Executed Date is the run date, and Defect ID should be filled in when Failed (if a tracker is used).
7. The counts in section 2 (Passed/Failed/Untested/Total) are derived from section 4 — in Excel this uses `COUNTIF` formulas; here, fill them in manually based on the actual case counts.
