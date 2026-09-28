# Unit Test Case — ReportScheduleService (MF13 Reporting & Analytics)

---

## Function: `computeNextRun` / `executeSchedule`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `RPT-FUNC-003` | | Function Name | `ReportScheduleService.computeNextRun` / `executeSchedule` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `110` | | Lack of test cases | `0` |
| Test requirement | `Compute next run time based on schedule frequency (DAILY/WEEKLY/MONTHLY) and zoneId (BR-179), limit failure retries to maximum 3 attempts (BR-179), and restrict email recipients to configured school report emails.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `3` | `1` | `1` | `1` | `3` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Schedule Frequency | `WEEKLY` (`dayOfWeek = 1`, `timeOfDay = 07:30`, `zoneId = "Asia/Ho_Chi_Minh"`) | O | | |
| Schedule Failure Count | Execution attempt 3 fails (retry threshold reached) | | O | |
| Email Recipients | Recipient list derived from `school_config.report_emails` | O | | O |
| | Custom recipient email provided in request body | | | |
| **Confirm** | | | | |
| Return | `nextRunAt` timestamp correctly computed for next Monday 07:30 ICT | O | | |
| Retry Action | Run status marked `FAILED`, system admin notification triggered, no attempt 4 | | O | |
| Recipient Verification | Email dispatched ONLY to configured `report_emails` | O | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | B | A |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |
