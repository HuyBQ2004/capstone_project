# Integration Test Case — Report Scheduling & Automation (MF13 Reporting & Analytics)

---

## Feature: Report Scheduling & Automation

### 1. General Information

| Field | Value |
|---|---|
| Feature | Report Scheduling & Automation |
| Test requirement | Verify automated report schedule creation (UC-173), recurrence computation, retry limit <= 3 (BR-179), email dispatch via GreenMail, lock contention handling (`FOR UPDATE SKIP LOCKED`), and client D12 validation. |
| Number of TCs | 3 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 3 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 3 | 0 |
| Round 3 | 0 | 0 | 3 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **POST /api/v1/report-schedules** | | | | | | | | | | | | | | |
| MF13-TC-INT-008 | Test report schedule creation, recurrence calculation, and duplicate ID prevention (D5). | 1. Send POST to `/api/v1/report-schedules` with body `{"reportSlug":"hygiene","frequency":"WEEKLY","dayOfWeek":1,"timeOfDay":"07:30"}`.<br>2. Send POST with client duplicate ID `{"id":"RSC-2026-001"}`. | 1. Step 1 returns HTTP 201 Created with computed `nextRunAt`.<br>2. Step 2 returns HTTP 409 Conflict (`RPT-409`, `MSG-139`). | School Admin authenticated session. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | UC-173 schedule creation |
| MF13-TC-INT-013 | Test rejection of client-supplied read-only derived fields during schedule creation (D12). | 1. Send POST to `/api/v1/report-schedules` specifying body `{"nextRunAt":"2030-01-01T00:00:00Z","createdBy":"HACKER"}`. | 1. Server ignores client-supplied `nextRunAt` and `createdBy` fields.<br>2. `nextRunAt` calculated server-side; `createdBy` extracted from JWT. | School Admin session active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | D12 read-only field guard |
| **Scheduled Execution Runner** | | | | | | | | | | | | | | |
| MF13-TC-INT-009 | Test scheduled runner execution, 3-attempt retry limit, GreenMail email dispatch, and lock contention. | 1. Seed active schedule `RSC-2026-001` with `nextRunAt <= now()`.<br>2. Trigger `@Scheduled` runner (`ReportScheduleRunner`).<br>3. Inspect GreenMail SMTP inbox.<br>4. Simulate runner failure to verify retry attempts.<br>5. Simulate concurrent runner execution. | 1. Step 2 generates report, dispatches email to `school_config.report_emails`, updates `nextRunAt`.<br>2. Step 3 confirms GreenMail received email with attachment.<br>3. Step 4 retries up to 3 times (`attempt = 3`), then marks status `FAILED` and notifies System Admin (no 4th attempt).<br>4. Step 5 `FOR UPDATE SKIP LOCKED` prevents double-dispatch. | Active schedule seeded, GreenMail active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-179 runner & retry |
