# Manual Web E2E Test — MF02 School & Class Management

> Manual testing on a real browser, no direct API calls. Same format as `02_IT/sample.md`.

---

## Feature: Academic Year Management (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Academic Year Management (Manual Web) |
| Test requirement | Create, filter, activate and close academic years at `/school-admin/academic-years` and `/school-admin/academic-years/[id]`. |
| Number of TCs | 8 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 8 | 0 |
| Round 2 | 0 | 0 | 8 | 0 |
| Round 3 | 0 | 0 | 8 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **`/school-admin/academic-years` — Create** | | | | | | | | | | | | | | |
| MF02-WEB-001 | Create a new academic year with "Copy class structure" checked. | 1. Log in as School Admin, go to `/school-admin/academic-years`.<br>2. Click "Create academic year".<br>3. Enter Name, Start date, End date, keep "Copy class structure from previous year" checked.<br>4. Click "Create academic year". | Dialog closes, a success toast confirms creation and that the class structure was copied from the previous year. New row appears in the list with status DRAFT. | Logged in as School Admin, at least one existing academic year for the school (for the copy-structure source). | Pending | | | Pending | | | Pending | | | BR-194 |
| MF02-WEB-002 | Create academic year — required fields left empty. | 1. Click "Create academic year".<br>2. Leave Name blank, clear Start/End date.<br>3. Click "Create academic year". | Blocked with an inline error asking to fill in name, start date and end date — dialog stays open, no request sent. | Logged in as School Admin. | Pending | | | Pending | | | Pending | | | |
| MF02-WEB-003 | Create academic year — start date not before end date. | 1. Click "Create academic year".<br>2. Enter a Start date that is the same as or after the End date.<br>3. Click "Create academic year". | Blocked with an inline error stating start date must be before end date. | Logged in as School Admin. | Pending | | | Pending | | | Pending | | | |
| **`/school-admin/academic-years` — List & Filter** | | | | | | | | | | | | | | |
| MF02-WEB-004 | Filter the academic year list by status. | 1. On `/school-admin/academic-years`, use the "Filter by Status" dropdown.<br>2. Select "ACTIVE".<br>3. Select "DRAFT". | Table re-filters after each selection — only rows matching the chosen status are shown. | At least one academic year exists in more than one status (e.g. one DRAFT, one ACTIVE). | Pending | | | Pending | | | Pending | | | |
| **`/school-admin/academic-years/[id]` — Activate / Close** | | | | | | | | | | | | | | |
| MF02-WEB-005 | Activate a DRAFT academic year while another year is already ACTIVE. | 1. Open the detail page of a DRAFT academic year.<br>2. Click "Activate".<br>3. Confirm in the dialog. | Blocked with an error saying only one active academic year is allowed per school — the DRAFT year's status stays unchanged. | An ACTIVE academic year already exists for the school, and a second DRAFT year exists. | Pending | | | Pending | | | Pending | | | BR-191, MSG-080 |
| MF02-WEB-006 | Activate a DRAFT academic year successfully (no other year ACTIVE). | 1. Close/ensure no other year is ACTIVE.<br>2. Open a DRAFT year's detail page, click "Activate", confirm. | Status badge updates to ACTIVE, success toast confirms the status change, "Promote students" action becomes available in the header. | A DRAFT academic year exists and no other year for the school is currently ACTIVE. | Pending | | | Pending | | | Pending | | | MSG-083 |
| MF02-WEB-007 | Close an ACTIVE academic year and confirm the cascade warning. | 1. Open the ACTIVE year's detail page, click "Close academic year".<br>2. Read the confirmation dialog (mentions the class count going read-only).<br>3. Click "Confirm close". | Status changes to CLOSED, success toast shown. Classrooms belonging to that year become read-only (rows are no longer clickable in the classroom list on this page). | An ACTIVE academic year exists with at least one classroom. | Pending | | | Pending | | | Pending | | | BR-193 |
| MF02-WEB-008 | View a CLOSED academic year's classroom list — rows are read-only. | 1. Open the detail page of a CLOSED academic year.<br>2. Try clicking a classroom row in the "Classrooms in this academic year" table. | A "read-only" hint is shown next to the classroom list heading and clicking a row does not navigate to the classroom detail page. | A CLOSED academic year exists with at least one classroom (from MF02-WEB-007). | Pending | | | Pending | | | Pending | | | |

---

## Feature: Promote Students (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Promote Students to Next Academic Year (Manual Web) |
| Test requirement | Bulk-promote students between classes at `/school-admin/academic-years/promote`, with retained-student exclusion and capacity checks. |
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
| **`/school-admin/academic-years/promote`** | | | | | | | | | | | | | | |
| MF02-WEB-009 | Promote students in bulk, excluding one retained (held-back) student. | 1. Go to `/school-admin/academic-years/promote`.<br>2. Pick a source class, then a target class.<br>3. Uncheck one student in the list (mark as retained).<br>4. Click "Confirm promote N students". | Success message shows the promoted count. The excluded student keeps their old class link — a note explains they must be moved manually later. Allergy info column shows unaffected. | An ACTIVE academic year exists with source and target classes and at least 2 students in the source class. | Pending | | | Pending | | | Pending | | | BR-194, MSG-084 |
| MF02-WEB-010 | Promote into a target class that would exceed the 50-student capacity. | 1. Pick a source class with several students.<br>2. Pick a target class already near full capacity (so promoting all source students exceeds 50). | An over-capacity warning is shown (max 50 students), and the "Confirm promote" button is disabled. | Target classroom's current student count + source class count > 50. | Pending | | | Pending | | | Pending | | | BR-025, MSG-010 |
| MF02-WEB-011 | Confirm button disabled with zero students selected to promote. | 1. Pick a source class.<br>2. Uncheck every student in the list (exclude all).<br>3. Observe the "Confirm promote" button. | The "0 to promote" chip is shown and the confirm button is disabled — cannot submit a promote of zero students. | A source class with at least one student is selected. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Classroom Management (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Classroom Management (Manual Web) |
| Test requirement | Create/edit classrooms and assign homeroom teachers at `/school-admin/classrooms` and `/school-admin/classrooms/[id]`. |
| Number of TCs | 9 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 9 | 0 |
| Round 2 | 0 | 0 | 9 | 0 |
| Round 3 | 0 | 0 | 9 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **`/school-admin/classrooms` — Create** | | | | | | | | | | | | | | |
| MF02-WEB-012 | Create classroom blocked when there is no ACTIVE academic year. | 1. Ensure no academic year is ACTIVE for the school.<br>2. Go to `/school-admin/classrooms`, click "Create classroom". | A warning banner is shown inside the dialog stating no ACTIVE academic year exists, and the "Create classroom" submit button is disabled. | No academic year for the school is currently ACTIVE. | Pending | | | Pending | | | Pending | | | BR-192, MSG-081 |
| MF02-WEB-013 | Create classroom successfully with a homeroom teacher assigned. | 1. With an ACTIVE academic year, click "Create classroom".<br>2. Enter Class name, pick a Grade, search and pick a Teacher, set Capacity ≤50.<br>3. Click "Create classroom". | Success toast confirms both classroom creation and teacher assignment. New classroom appears in the list with the assigned teacher's name. | An ACTIVE academic year exists; at least one ACTIVE teacher account exists. | Pending | | | Pending | | | Pending | | | MSG-033, MSG-034 |
| MF02-WEB-014 | Create classroom — grade not selected. | 1. Click "Create classroom".<br>2. Fill Class name, leave Grade unselected.<br>3. Click "Create classroom". | Blocked with an inline error asking to pick a grade — dialog stays open. | An ACTIVE academic year exists. | Pending | | | Pending | | | Pending | | | BR-024 |
| MF02-WEB-015 | Create classroom — capacity greater than 50. | 1. Click "Create classroom".<br>2. Fill in Class name and Grade, set Capacity to a value above 50.<br>3. Click "Create classroom". | Blocked with an inline error stating capacity cannot exceed 50 students. | An ACTIVE academic year exists. | Pending | | | Pending | | | Pending | | | BR-025, MSG-010 |
| MF02-WEB-016 | The homeroom teacher picker only lists active teacher accounts. | 1. Open "Create classroom", inspect the teacher search dropdown.<br>2. Confirm no PENDING_VERIFICATION teacher accounts appear in the list. | Only teachers whose account status is ACTIVE appear as selectable options. | At least one teacher account exists in PENDING_VERIFICATION state, and at least one ACTIVE. | Pending | | | Pending | | | Pending | | | BR-010 |
| **`/school-admin/classrooms` — List & Filter** | | | | | | | | | | | | | | |
| MF02-WEB-017 | Filter the classroom list by grade. | 1. On `/school-admin/classrooms`, use "Filter by Grade" dropdown.<br>2. Select a specific grade. | Table re-filters to show only classrooms belonging to the selected grade. | At least two classrooms exist across different grades. | Pending | | | Pending | | | Pending | | | |
| **`/school-admin/classrooms/[id]` — Update / Reassign teacher** | | | | | | | | | | | | | | |
| MF02-WEB-018 | Reassign a homeroom teacher who is already assigned to another classroom. | 1. Open a classroom's detail page, click "Update classroom / Change homeroom teacher".<br>2. Pick a teacher who is currently homeroom of a different classroom.<br>3. Click "Save". | Blocked with an error naming the other classroom the teacher already leads, asking to unassign there first. Teacher assignment on this classroom stays unchanged. | Another classroom already has a homeroom teacher assigned. | Pending | | | Pending | | | Pending | | | BR-020, MSG-011 |
| MF02-WEB-019 | Reduce classroom capacity below its current enrolled student count. | 1. Open a classroom's detail page with existing students enrolled, click "Update classroom".<br>2. Set Capacity below the current student count shown under the field.<br>3. Click "Save". | Blocked with an error stating the new capacity cannot be lower than the current enrollment. | Classroom has at least 1 enrolled student. | Pending | | | Pending | | | Pending | | | BR-025, BR-219, MSG-010 |
| MF02-WEB-020 | Successfully reassign a homeroom teacher and confirm the audit-worthy change. | 1. Open a classroom's detail page, click "Update classroom / Change homeroom teacher".<br>2. Pick a different, unassigned ACTIVE teacher.<br>3. Click "Save". | Success toast confirms the reassignment and mentions the previous teacher was automatically unassigned. Detail page's "Homeroom teacher" stat card updates to the new name. | An unassigned ACTIVE teacher exists other than the classroom's current one. | Pending | | | Pending | | | Pending | | | BR-020, BR-026, MSG-034 |

---

## Feature: Grade Management (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Grade Management (Manual Web) |
| Test requirement | Manage the school-scoped grade catalog at `/school-admin/grades` (add, edit, deactivate, reactivate). |
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
| **`/school-admin/grades`** | | | | | | | | | | | | | | |
| MF02-WEB-021 | Add a new grade to the school's catalog. | 1. Go to `/school-admin/grades`, click "Add grade".<br>2. Enter Grade name and Sort order.<br>3. Click "Add grade". | Success toast confirms the grade was added, new row appears in the "Grade list" table with status "In use", form collapses. | Logged in as School Admin. | Pending | | | Pending | | | Pending | | | |
| MF02-WEB-022 | Add grade — name left blank. | 1. Click "Add grade".<br>2. Leave Grade name empty, click "Add grade". | Blocked with a toast error that grade name cannot be empty — no new row is added. | Logged in as School Admin. | Pending | | | Pending | | | Pending | | | |
| MF02-WEB-023 | Edit an existing grade's name and sort order. | 1. In the grade list, click the edit (pencil) icon on a row.<br>2. Change the Grade name and/or Sort order.<br>3. Click "Update". | Success toast confirms the update, the row reflects the new name/order immediately. | At least one grade exists in the list. | Pending | | | Pending | | | Pending | | | |
| MF02-WEB-024 | Deactivate a grade currently in use by a classroom. | 1. In the grade list, click the deactivate (trash) icon on a grade that a classroom currently references.<br>2. Confirm the browser confirmation dialog (states classrooms keep their existing link). | Grade status badge changes to "Deactivated" (soft-delete). The classroom that used this grade still displays it unchanged on the classroom list/detail. | A grade exists that is referenced by at least one existing classroom. | Pending | | | Pending | | | Pending | | | Soft delete — no hard deletion |
| MF02-WEB-025 | Reactivate a previously deactivated grade. | 1. Find a grade with status "Deactivated".<br>2. Click the reactivate (circular arrow) icon. | Success toast confirms reactivation, status badge changes back to "In use". | A deactivated grade exists (from MF02-WEB-024). | Pending | | | Pending | | | Pending | | | |

---

## Feature: School Record Management (Web, System Admin)

### 1. General Information

| Field | Value |
|---|---|
| Feature | School Record Management (Manual Web) |
| Test requirement | View, approve, deactivate, edit and attach documents to school records at `/system-admin/schools` and `/system-admin/schools/[id]`. |
| Number of TCs | 9 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 9 | 0 |
| Round 2 | 0 | 0 | 9 | 0 |
| Round 3 | 0 | 0 | 9 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **`/system-admin/schools` — List & Approve** | | | | | | | | | | | | | | |
| MF02-WEB-026 | Filter the school list by status. | 1. Log in as System Admin, go to `/system-admin/schools`.<br>2. Use "Filter by Status" dropdown, select "PENDING", then "ACTIVE". | Table re-filters after each selection to show only schools matching the chosen status. | At least one school in each of PENDING and ACTIVE status. | Pending | | | Pending | | | Pending | | | |
| MF02-WEB-027 | Approve a PENDING school whose registrant email is not yet verified. | 1. On the school list, find a PENDING row whose registrant has not verified their email.<br>2. Observe the row's action button. | The action button shows "Waiting for email verification" and is disabled — cannot approve until the registrant verifies their email. | A PENDING school exists whose registrant email is unverified. | Pending | | | Pending | | | Pending | | | SCH-015 |
| MF02-WEB-028 | Approve a PENDING school with a verified registrant email. | 1. On the school list, find a PENDING row whose registrant email is verified.<br>2. Click "Approve". | Success toast confirms approval and states GPS coordinates are now locked. School status changes to ACTIVE in the list. | A PENDING school exists whose registrant email is verified. | Pending | | | Pending | | | Pending | | | BR-028 |
| MF02-WEB-029 | Deactivate an ACTIVE school and confirm the cascade warning. | 1. On an ACTIVE school row, click "Deactivate".<br>2. Read the confirmation dialog (mentions class/staff/student counts). | Confirmation dialog shows the exact counts of classrooms, staff and students that will be paused. After confirming, status changes to INACTIVE and a toast confirms the cascade pause. | An ACTIVE school exists with at least one classroom, staff member and student. | Pending | | | Pending | | | Pending | | | BR-021 |
| **`/system-admin/schools/[id]` — Detail & Edit** | | | | | | | | | | | | | | |
| MF02-WEB-030 | Edit a school's basic info (name, address, email, operating hours). | 1. Open a school's detail page, click "Edit".<br>2. Change Name, Address, Email, Operating hours.<br>3. Click "Save". | Success toast confirms the update, the school detail page reflects the new values immediately. | Logged in as System Admin, a school exists. | Pending | | | Pending | | | Pending | | | MSG-029 |
| MF02-WEB-031 | Tax ID field is read-only and GPS fields are locked for an ACTIVE school. | 1. Open the "Edit" dialog for an ACTIVE school.<br>2. Try to edit the Tax ID field.<br>3. Try to edit Latitude/Longitude without unlocking. | Tax ID input is disabled/read-only. Latitude/Longitude inputs are disabled until "Unlock coordinates" is used — a "Locked" badge is shown next to the GPS section. | School status is ACTIVE (coordinates auto-locked on approval). | Pending | | | Pending | | | Pending | | | BR-028 |
| MF02-WEB-032 | Unlock GPS coordinates without providing a reason. | 1. In the Edit dialog of a locked school, click "Unlock coordinates".<br>2. Leave the reason field blank, click "Confirm unlock". | Blocked with an inline error stating the unlock reason is mandatory — coordinates remain locked. | School has locked coordinates. | Pending | | | Pending | | | Pending | | | BR-028 — reason is written to the audit log |
| MF02-WEB-033 | Upload a new legal document to a school's record. | 1. On a school's detail page, click "Upload document".<br>2. Enter a document title, choose a PDF/PNG/JPG file ≤10MB.<br>3. Click "Upload". | Success toast confirms the upload, new document appears in the "Documents & Legal Permits" list with status "Pending verification". | Logged in as System Admin, school detail page open. | Pending | | | Pending | | | Pending | | | |
| MF02-WEB-034 | Export a school's profile to PDF. | 1. On a school's detail page, click "Export PDF". | Browser's print dialog opens showing a formatted printable version of the school profile (name, tax ID, address, status). | School detail page open with data loaded. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Access Control Edge Cases (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | School & Class Management — Access Control (Manual Web) |
| Test requirement | Confirm School Admin data is scoped to their own school and System Admin-only screens are not reachable by other roles. |
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
| MF02-WEB-035 | School Admin cannot view another school's classroom by editing the URL. | 1. Log in as School Admin of School A, note a classroom ID belonging to School B (from a test dataset).<br>2. Manually navigate to `/school-admin/classrooms/<School-B-classroom-id>`. | Blocked — shows "Classroom not found" (the server scopes the classroom lookup to the caller's own school via the JWT), not School B's real data. | Two schools (A, B) exist, each with at least one classroom, and their IDs are known. | Pending | | | Pending | | | Pending | | | Server derives schoolId from JWT, never trusts the URL |
| MF02-WEB-036 | A non-System-Admin role cannot reach `/system-admin/schools`. | 1. Log in as School Admin (or Teacher).<br>2. Manually navigate to `/system-admin/schools`. | Redirected away from the page (e.g. to the role's own dashboard or an access-denied screen) — the school list for other schools is never rendered. | Logged in as a non-System-Admin role. | Pending | | | Pending | | | Pending | | | |
