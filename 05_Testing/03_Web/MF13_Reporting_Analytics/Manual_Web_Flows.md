# Manual Web E2E Test — MF13 Reporting & Analytics

> Manual testing on a real browser, no direct API calls. Same format as `02_IT/sample.md`.

---

## Feature: Compliance Reports Dashboard (School Admin, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Compliance Reports Dashboard (Manual Web) |
| Test requirement | View the compliance overview at `/school-admin/reports` — summary stat cards, per-module compliance rate bars, alert badges, and the additional-reports list. |
| Number of TCs | 4 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 4 | 0 |
| Round 2 | 0 | 0 | 4 | 0 |
| Round 3 | 0 | 0 | 4 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Dashboard Overview** | | | | | | | | | | | | | | |
| MF13-WEB-001 | View the compliance dashboard summary and per-module rate cards. | 1. Log in as School Admin.<br>2. Go to `/school-admin/reports`. | Page header shows the data period label and "Generated at" timestamp. Four stat cards are shown: three-step compliance rate, sample retention rate, receiving compliance rate, and overall compliance score. Each module in the list shows a colored progress bar (green ≥95%, yellow ≥85%, red below) matching its displayed percentage. | Logged in as School Admin, school has at least some inspection/sample/receiving data. | Pending | | | Pending | | | Pending | | | |
| MF13-WEB-002 | Module rows with active alerts show an alert badge. | 1. On `/school-admin/reports`, look for a module row that has one or more alerts (e.g. overdue samples or failed receiving sessions). | The row shows a red "N alerts" badge next to its compliance percentage badge. | At least one compliance module currently has an alert count > 0. | Pending | | | Pending | | | Pending | | | |
| MF13-WEB-003 | Click a compliance module row to open its detail report. | 1. On `/school-admin/reports`, click any module row (e.g. "Three-step inspection report"). | Navigates to `/school-admin/reports/{slug}` and shows the correct report title/description for that module. | Logged in as School Admin. | Pending | | | Pending | | | Pending | | | |
| MF13-WEB-004 | Additional reports outside the compliance modules are listed and clickable. | 1. On `/school-admin/reports`, scroll to the additional-reports section below the module list (e.g. supplier certificates, hygiene, traceability, system usage).<br>2. Click one of them. | Each additional report is listed as a clickable row with a chevron icon; clicking it opens its own detail page at `/school-admin/reports/{slug}`. | The report catalog includes at least one report outside the 3 core compliance modules. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Report Detail & PDF Export (School Admin, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Report Detail & PDF Export (Manual Web) |
| Test requirement | View a single report's detail table at `/school-admin/reports/[slug]` and export it as a downloadable PDF file. |
| Number of TCs | 4 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 4 | 0 |
| Round 2 | 0 | 0 | 4 | 0 |
| Round 3 | 0 | 0 | 4 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Report Detail Table** | | | | | | | | | | | | | | |
| MF13-WEB-005 | View a report detail page with its data table, search box, and confidentiality labels. | 1. Log in as School Admin, go to `/school-admin/reports`.<br>2. Click into the "Three-step inspection" report. | Detail page shows the report title/description, a "Confidential" badge, an "anonymized data, no student identification" badge, and a paginated data table with a search box (placeholder "Search inspection session..."). | Logged in as School Admin. | Pending | | | Pending | | | Pending | | | |
| MF13-WEB-006 | Search within a report detail table filters the rows. | 1. On any report detail page, type a known value (e.g. an inspector name or supplier name) into the search box. | The table refreshes to only show rows matching the search term; clearing the box restores the full list. | Report detail page open, table has multiple rows. | Pending | | | Pending | | | Pending | | | |
| MF13-WEB-007 | Try to open a report slug that does not exist in the catalog. | 1. Manually navigate to `/school-admin/reports/does-not-exist`. | A "not found" page/state is shown instead of a broken or blank report page. | Logged in as School Admin. | Pending | | | Pending | | | Pending | | | |
| **Export PDF** | | | | | | | | | | | | | | |
| MF13-WEB-008 | Export the currently viewed report as a PDF and confirm the file downloads. | 1. On a report detail page, click "Export PDF".<br>2. Wait for the button to show "Exporting..." then complete.<br>3. Check the browser's downloaded files. | Button shows a loading state while exporting, then a success toast confirming the export was recorded in the audit trail. A PDF file is downloaded to the browser (filename derived from the export code). | Logged in as School Admin, report has at least one row of data in the current month. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Report Email Recipients Configuration (School Admin, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Report Email Recipients Configuration (Manual Web) |
| Test requirement | Configure the email addresses that receive scheduled/automated reports, via the "Report recipient emails" field on `/school-admin/config`. |
| Number of TCs | 3 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 3 | 0 |
| Round 2 | 0 | 0 | 3 | 0 |
| Round 3 | 0 | 0 | 3 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| MF13-WEB-009 | Update the report recipient emails with a valid comma-separated list, save successfully. | 1. Log in as School Admin, go to `/school-admin/config`.<br>2. In "Report recipient emails", enter two valid emails separated by a comma.<br>3. Click "Save configuration". | Saves successfully with a confirmation toast. Reloading the page shows the same list of emails pre-filled in the field. | Logged in as School Admin who manages the school's config. | Pending | | | Pending | | | Pending | | | This list is the recipient set used by the scheduled report runner. |
| MF13-WEB-010 | Enter an invalid email address in the recipient list and try to save. | 1. On `/school-admin/config`, type a malformed value with no "@" into "Report recipient emails" (e.g. "notanemail, ops@school.com").<br>2. Click "Save configuration". | Blocked client-side with an error message about an invalid email in the list — the config is not saved. | Logged in as School Admin. | Pending | | | Pending | | | Pending | | | |
| MF13-WEB-011 | Clear the recipient list entirely and save. | 1. On `/school-admin/config`, clear the "Report recipient emails" field completely.<br>2. Click "Save configuration". | Saves successfully with an empty recipient list (no scheduled report emails will be sent until re-configured) — no client-side error for an empty list. | Logged in as School Admin. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Compliance Report Compose & Send (Medical Staff, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Compliance Report Compose & Send (Manual Web) |
| Test requirement | Compose, preview, and send a periodic compliance report to School Administration from `/medical-staff/compliance/report`, with auto-computed figures and a blocked-when-empty rule. |
| Number of TCs | 5 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 5 | 0 |
| Round 2 | 0 | 0 | 5 | 0 |
| Round 3 | 0 | 0 | 5 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Compose** | | | | | | | | | | | | | | |
| MF13-WEB-012 | Switch the report period between Daily and Weekly, figures recompute automatically. | 1. Log in as Medical Staff, go to `/medical-staff/compliance/report`.<br>2. Click "Daily", note the figures (average hygiene score, three-step summary, staff health status).<br>3. Click "Weekly". | The date range label updates for the selected period, and all summary figures (average score, inspections done, failed sessions) recompute for the new range without a manual refresh. | Logged in as Medical Staff, has inspection/kitchen-inspection data in both a daily and weekly window. | Pending | | | Pending | | | Pending | | | |
| MF13-WEB-013 | Try composing a report for a period with zero inspections. | 1. Pick a period/date range known to have no kitchen inspections or three-step sessions. | A warning banner is shown: no data in the selected period, choose another period or run an inspection first. The "Finalize & send" button is disabled. | Logged in as Medical Staff, a period with zero records exists (e.g. a fresh school). | Pending | | | Pending | | | Pending | | | MSG-092 |
| MF13-WEB-014 | Add a free-text assessment and additional notes, then preview the report. | 1. Fill "Overall assessment" and "Additional notes" text areas.<br>2. Click "Preview". | The preview renders using the same read-only document layout as a sent report, showing a "Preview — not sent yet" label, and includes the entered assessment/notes text. | Period selected has at least one inspection record. | Pending | | | Pending | | | Pending | | | Preview reuses the same component as the Sent Reports detail view. |
| MF13-WEB-015 | Save a draft (non-final) without sending. | 1. On the compose screen, click "Save draft". | Shows a confirmation toast with the save time; the report is NOT sent to School Administration and does not appear in the sent-reports history yet. | Period selected has at least one inspection record. | Pending | | | Pending | | | Pending | | | Draft state is client-side only, not persisted server-side. |
| **Send** | | | | | | | | | | | | | | |
| MF13-WEB-016 | Finalize and send a compliance report with data present. | 1. From the preview screen, click "Finalize & send report". | Success toast confirming the report was sent to School Administration, referencing the sent-reports history. Notes/assessment fields reset. The new report appears at the top of `/medical-staff/reports`. | Period selected has at least one inspection record. | Pending | | | Pending | | | Pending | | | MSG-091 |

---

## Feature: Violation Summary Report Compose & Send (Medical Staff, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Violation Summary Report Compose & Send (Manual Web) |
| Test requirement | Compose and send a periodic violation summary report at `/medical-staff/violations/reports`, with server-computed statistics that cannot be hand-edited. |
| Number of TCs | 4 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 4 | 0 |
| Round 2 | 0 | 0 | 4 | 0 |
| Round 3 | 0 | 0 | 4 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| MF13-WEB-017 | View auto-computed violation counts by severity and status for the selected period. | 1. Log in as Medical Staff, go to `/medical-staff/violations/reports`.<br>2. Click "Daily" then "Weekly" and compare the summary cards (by severity LOW/MEDIUM/HIGH, closed vs pending, repeat-suspect count). | All summary numbers and the generated summary sentence recompute automatically per period; none of these figures have an editable input field next to them. | Logged in as Medical Staff, violation records exist in the school. | Pending | | | Pending | | | Pending | | | Figures are read-only by design (no manual override). |
| MF13-WEB-018 | Add a recommendation/assessment note, then send the violation report. | 1. Fill "Medical Staff assessment/recommendation" text area.<br>2. Click "Send report". | Success toast confirming the report was sent to School Administration; the note field is cleared after sending. | Selected period has at least one violation record. | Pending | | | Pending | | | Pending | | | MSG-099 |
| MF13-WEB-019 | Try sending a violation report for a period with zero violations. | 1. Select a period/date range known to have zero violation records.<br>2. Observe the "Send report" button. | A warning banner shows that the selected period has no data and the report cannot be created; the "Send report" button is disabled. | A period with zero violations exists. | Pending | | | Pending | | | Pending | | | MSG-097 |
| MF13-WEB-020 | Click through from the in-period violation list to the source violation record. | 1. On `/medical-staff/violations/reports`, click any violation code in the "Violations in period" table. | Navigates to that violation's own detail page, confirming the report only links to source records rather than duplicating an editable copy. | Selected period has at least one violation. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Sent Reports History & Detail View (Medical Staff, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Sent Reports History & Detail View (Manual Web) |
| Test requirement | Browse and filter previously sent reports at `/medical-staff/reports`, and view/print an individual sent report as an immutable snapshot. |
| Number of TCs | 4 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 4 | 0 |
| Round 2 | 0 | 0 | 4 | 0 |
| Round 3 | 0 | 0 | 4 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **History List** | | | | | | | | | | | | | | |
| MF13-WEB-021 | View the sent-reports history list, newest first. | 1. Log in as Medical Staff, go to `/medical-staff/reports`. | A table lists all previously sent reports (report type badge, period, data range, sent-at datetime, sender), ordered newest-sent first. | At least 2 reports have been sent previously at different times. | Pending | | | Pending | | | Pending | | | |
| MF13-WEB-022 | Filter the history list by report type. | 1. On `/medical-staff/reports`, use the "Report type" dropdown to select "Compliance monitoring".<br>2. Then select "Violation summary". | The list only shows rows matching the selected report type each time; selecting "All" restores the full list. | History contains both compliance and violation-summary reports. | Pending | | | Pending | | | Pending | | | |
| **Detail & Print** | | | | | | | | | | | | | | |
| MF13-WEB-023 | Open a sent report's detail view and confirm it is read-only. | 1. On `/medical-staff/reports`, click "View report" on any row. | Opens the report detail page showing an "Already finalized — read-only" info banner stating the figures are a snapshot at send time and are not recalculated against current data. | At least one sent report exists. | Pending | | | Pending | | | Pending | | | BR-178 — figures never recompute after send. |
| MF13-WEB-024 | Print / export the sent report as PDF from the detail page. | 1. On a sent report's detail page, click "Print / Export PDF". | A print-preview window opens showing the exact same document layout as the compose-preview screen, with the report ID and sent-by/recipient in the title. | A sent report detail page is open. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Compliance CSV Export (Medical Staff, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Compliance CSV Export (Manual Web) |
| Test requirement | Export compliance monitoring data as CSV files for a chosen date range from the Compliance Dashboard at `/medical-staff/compliance`. |
| Number of TCs | 2 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 2 | 0 |
| Round 2 | 0 | 0 | 2 | 0 |
| Round 3 | 0 | 0 | 2 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| MF13-WEB-025 | Export compliance data for a valid date range as CSV. | 1. Log in as Medical Staff, go to `/medical-staff/compliance`.<br>2. Set "From date" and "To date" to a range with known records.<br>3. Click "Export CSV". | A success toast confirms the number of exported inspection sessions/temperature readings. Three CSV files are downloaded to the browser (overview, kitchen hygiene checks, fridge/sample temperature). | Logged in as Medical Staff, records exist in the chosen range. | Pending | | | Pending | | | Pending | | | |
| MF13-WEB-026 | Open one of the exported CSV files to confirm data was captured. | 1. Open the "overview" CSV file downloaded in MF13-WEB-025 in a spreadsheet app. | The file opens without garbled characters, and rows match the count reported in the success toast for the selected date range. | CSV files from MF13-WEB-025 available. | Pending | | | Pending | | | Pending | | | |

---

## Feature: System Usage Analytics Dashboard (System Admin, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | System Usage Analytics Dashboard (Manual Web) |
| Test requirement | View platform-wide usage statistics and charts at `/system-admin/usage-statistics`, with a selectable date range. |
| Number of TCs | 3 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 3 | 0 |
| Round 2 | 0 | 0 | 3 | 0 |
| Round 3 | 0 | 0 | 3 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| MF13-WEB-027 | View the usage statistics dashboard with its stat cards and charts. | 1. Log in as System Admin, go to `/system-admin/usage-statistics`. | Three stat cards show today's active users (DAU), 7-day active users (WAU), and total API requests for the selected range. A bar chart shows daily active users, and a table lists AI usage per module. | Logged in as System Admin. | Pending | | | Pending | | | Pending | | | |
| MF13-WEB-028 | Switch the date range and confirm the charts/table refresh. | 1. On `/system-admin/usage-statistics`, change the range dropdown from "Last 7 days" to "Last 30 days". | The DAU/WAU cards, the "API requests" card label, the bar chart, and the module usage table all update to reflect the newly selected 30-day range. | Logged in as System Admin, usage data spans more than 7 days. | Pending | | | Pending | | | Pending | | | |
| MF13-WEB-029 | View the dashboard with no usage data in the selected range. | 1. Change the date range to a period known to have no activity (e.g. a period before the school existed). | The bar chart area shows a "No data yet" message instead of an empty/broken chart; stat cards show 0 rather than blank or an error. | A date range with zero recorded activity is available for testing. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Role-Based Access to Reports (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Role-Based Access to Reports (Manual Web) |
| Test requirement | Confirm each role only reaches its own reporting screens and data scope — School Admin (school-wide compliance reports), Medical Staff (compose/send/history for own school), System Admin (platform-wide usage statistics), with no cross-role or cross-school leakage visible in the UI. |
| Number of TCs | 4 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 4 | 0 |
| Round 2 | 0 | 0 | 4 | 0 |
| Round 3 | 0 | 0 | 4 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| MF13-WEB-030 | Medical Staff cannot open the School Admin compliance reports dashboard. | 1. Log in as Medical Staff.<br>2. Manually navigate to `/school-admin/reports`. | Blocked from viewing the School Admin reports dashboard — redirected away or shown an access-denied page; School Admin's cross-module report catalog is never rendered for this role. | Logged in as Medical Staff. | Pending | | | Pending | | | Pending | | | |
| MF13-WEB-031 | System Admin cannot open the Medical Staff compliance report compose screen. | 1. Log in as System Admin.<br>2. Manually navigate to `/medical-staff/compliance/report`. | Blocked — redirected away or shown an access-denied page; System Admin never reaches the Medical Staff compose/send flow. | Logged in as System Admin. | Pending | | | Pending | | | Pending | | | |
| MF13-WEB-032 | School Admin only sees compliance data for their own school, never another school's rows. | 1. Log in as School Admin of School A, note the compliance stats and module data on `/school-admin/reports`.<br>2. Log out, log in as School Admin of School B, view the same page. | The two School Admin sessions show different data scoped to each one's own school; no data row, count, or export from School A is visible while logged in as School B. | Two School Admin accounts exist for two different schools, each with some report data. | Pending | | | Pending | | | Pending | | | |
| MF13-WEB-033 | Medical Staff only sees sent reports for their own school in the history list. | 1. Log in as Medical Staff of School A, note the entries on `/medical-staff/reports`.<br>2. Log out, log in as Medical Staff of School B, view the same page. | Each Medical Staff account only sees the sent-report history belonging to its own school; no report sent by School A appears in School B's list. | Two Medical Staff accounts exist for two different schools, each with at least one sent report. | Pending | | | Pending | | | Pending | | | |
