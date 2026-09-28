# Manual Web E2E Test — MF03 Student & Parent Management

> Manual testing on a real browser, no direct API calls. Same format as `02_IT/sample.md`.

---

## Feature: Student Profile Creation & Parent Linking (School Admin, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Student Profile Creation & Parent Linking (Manual Web) |
| Test requirement | Create a student profile and link a parent account from `/school-admin/students`, covering both the "new parent account" and "existing parent email" paths, and the mandatory/format validations on the "Add student" dialog. |
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
| **Add student — new parent email** | | | | | | | | | | | | | | |
| MF03-WEB-001 | Create a new student profile and auto-create a new parent account by email (BR-030, UC-025). | 1. Log in as School Admin, go to `/school-admin/students`.<br>2. Click "Add student".<br>3. Fill in Full name, Date of birth (dd/mm/yyyy), pick a Class, Parent full name, and a Parent email that has never been used in the system.<br>4. Click "Create profile". | Dialog closes, the new student appears in the list with an unverified health-record status. A new parent account is created for the email and receives a verification email (BR-010); the parent is linked to the student (BR-030). Toast confirms creation and mentions health declaration is pending (BR-031). | Logged in as School Admin, school has at least one classroom already created. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-002 | Create a student and link to an EXISTING parent account by email — no duplicate account created. | 1. On "Add student", fill in required fields using a Parent email that already has an account in the system (a different name is fine).<br>2. Click "Create profile". | Student created and linked to the existing parent account (matched by email, not by name) — no second parent account is created for the same email. | An existing parent account with a known email already exists (e.g. from MF03-WEB-001). | Pending | | | Pending | | | Pending | | | Matching is by email only |
| **Add student — validation** | | | | | | | | | | | | | | |
| MF03-WEB-003 | Try to submit the "Add student" form with required fields missing. | 1. Open "Add student".<br>2. Leave Full name, Date of birth, Parent name, or Parent email empty (test at least 2 combinations).<br>3. Click "Create profile". | Blocked with an inline error stating full name, date of birth, and linked parent full name + email are all required (BR-030, UC-025). No request is sent, dialog stays open. | Logged in as School Admin. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-004 | Try to submit with an invalid date-of-birth format or invalid parent email format. | 1. Open "Add student", fill all fields but enter a date of birth NOT in dd/mm/yyyy (e.g. "2018-03-12"), submit.<br>2. Fix the date, then enter a parent email without "@", submit. | Both attempts blocked with a specific inline error ("date of birth must be dd/mm/yyyy" / "invalid parent email format — BR-012"). Neither request reaches the server. | Logged in as School Admin. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Bulk Import Students (School Admin, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Bulk Import Students (Manual Web) |
| Test requirement | Two-phase bulk import (validate → commit) via the "Bulk import" dialog on `/school-admin/students`, including template download and the invalid-row preview (BR-032, MSG-035). |
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
| MF03-WEB-005 | Download the CSV import template and import a fully valid file. | 1. On `/school-admin/students`, click "Bulk import (BR-032)".<br>2. Click "Download template", confirm the CSV downloads.<br>3. Select a CSV file where every row matches the template columns and passes validation, click "Upload & Check".<br>4. Review the preview showing all rows valid, click "Confirm import N valid rows (MSG-035)". | Preview step shows a green "valid" badge count equal to the row count, 0 errors, 0 duplicates. After commit, success screen reports N students imported and X new parent accounts created (each receiving a verification email, BR-010). | Logged in as School Admin. A prepared valid CSV file matching the template exists. | Pending | | | Pending | | | Pending | | | 2-phase: validate is read-only, commit is all-or-nothing on the same importId |
| MF03-WEB-006 | Import a CSV with a mix of valid and invalid rows. | 1. Open "Bulk import", upload a CSV with some rows missing required data or malformed.<br>2. Review the preview table. | Preview shows separate counts for valid/invalid/duplicate rows; each invalid row displays its row number and an inline error message. The "Confirm import" button is enabled and labeled with the count of VALID rows only. | Logged in as School Admin. A CSV with at least 1 valid and 1 invalid row exists. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-007 | Confirm import is blocked when there are zero valid rows. | 1. Open "Bulk import", upload a CSV where every row is invalid.<br>2. Observe the preview and the "Confirm import" button. | The "Confirm import" button is disabled (0 valid rows) — the import cannot be committed until at least one row is valid. | Logged in as School Admin. A CSV with 0 valid rows exists. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Student Profile Detail & Edit (School Admin, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Student Profile Detail & Edit (Manual Web) |
| Test requirement | View a student's profile detail at `/school-admin/students/{id}` and edit name/DOB/class via "Edit profile (UC-099)", including the class-capacity gate (BR-025, MSG-010) and the read-only registration number. |
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
| MF03-WEB-008 | View a student's profile detail page, including the unverified-health-record banner. | 1. Log in as School Admin, go to `/school-admin/students`, click a student row whose health record is not yet VERIFIED. | Detail page opens showing the student's name/class, linked parent (with a link to the parent's account page), declared allergies (or "none"), special diet, and a warning banner stating the health/allergy information is not yet verified (BR-031). | Logged in as School Admin; at least one student with an unverified health record exists. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-009 | Edit a student's name, date of birth, and class successfully. | 1. On the student detail page, click "Edit profile (UC-099)".<br>2. Change Full name and Date of birth, pick a different Class with available capacity.<br>3. Click "Save (MSG-029)". | Saved successfully, dialog closes, the page reflects the new name/DOB/class immediately. The "School Registration Number" field stays disabled/read-only throughout. | Logged in as School Admin; target class has free capacity. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-010 | Try to move a student into a class that is already at full capacity. | 1. On "Edit profile", pick a Class that is already at its maximum capacity (shown as `current/capacity` in the dropdown).<br>2. Click "Save". | Blocked with an inline error stating the target class is over capacity (BR-025, MSG-010) — the student is NOT moved, dialog stays open. | Logged in as School Admin; at least one classroom is at full capacity (or capacity 0 spare). | Pending | | | Pending | | | Pending | | | |

---

## Feature: Parent Account & Linked Students (School Admin, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Parent Account & Linked Students (Manual Web) |
| Test requirement | View a parent account's detail and linked-student roster at `/school-admin/parents/{id}`, and the 5-student linkage limit (BR-030). |
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
| MF03-WEB-011 | View a parent's account page from a student's detail page. | 1. On a student's detail page, click "View parent account →".<br>2. Observe the parent account page. | Opens `/school-admin/parents/{parentId}` showing the parent's name, the count of linked students (`X / 5`), the count of children whose health record is verified, and a full table of all linked students with class + allergy + health-status columns. | Logged in as School Admin; a student with a linked parent exists. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-012 | View a parent account that has reached the 5-student linkage limit. | 1. Navigate to a parent account page for a parent already linked to 5 students (BR-201/BR-030). | The "Linked students" stat shows "5 / 5" and a warning alert states the limit is reached and no further students can be linked without manual verification. The "Link another student" button is replaced by this warning. | A parent account linked to exactly 5 students exists. | Pending | | | Pending | | | Pending | | | BR-201: max 5 students per parent account |
| MF03-WEB-013 | Search/filter the linked-students table on a parent's account page. | 1. On a parent account page with 2+ linked students, use the search box to filter by one child's name or class. | Only the matching student row(s) remain visible in the linked-students table. | Parent account with 2+ linked students in different classes. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Allergy Declaration (Parent, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Allergy Declaration (Manual Web) |
| Test requirement | Submit a new allergy declaration for the active child at `/parent/allergies`, including the mandatory medical-proof upload for SEVERE severity (BR-031) and the read-only nature of already-submitted declarations. |
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
| MF03-WEB-014 | Submit a MILD/MODERATE allergy declaration without a proof file. | 1. Log in as Parent, go to `/parent/allergies` with a child selected.<br>2. Pick an allergen category, optionally a specific name, select severity "Mild" or "Moderate".<br>3. Leave the "Medical proof from hospital" field empty.<br>4. Click "Submit declaration". | Submission succeeds without a file. Toast confirms "Allergy information saved — pending verification (MSG-036)". The new declaration appears in "Current allergy profile" with a PENDING status badge. | Logged in as Parent with at least one linked child selected as the active child. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-015 | Try to submit a SEVERE allergy declaration WITHOUT a proof file — must be blocked. | 1. On the declaration form, select severity "Severe (anaphylaxis)".<br>2. Leave the proof-file field empty.<br>3. Click "Submit declaration". | Blocked client-side with an inline error stating Severe requires a hospital medical proof attachment (BR-031). No request is sent. | Logged in as Parent, active child selected. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-016 | Submit a SEVERE allergy declaration WITH a valid proof file. | 1. On the declaration form, select severity "Severe (anaphylaxis)".<br>2. Attach a PDF/JPG/PNG file ≤10MB in the proof-file field.<br>3. Click "Submit declaration". | The file uploads first, then the declaration is submitted referencing the uploaded document's ID. Success toast (MSG-036), new PENDING declaration appears with a Severe badge. | Logged in as Parent, active child selected; a valid PDF/JPG/PNG test file ≤10MB available. | Pending | | | Pending | | | Pending | | | Upload happens before the declaration POST |
| MF03-WEB-017 | Confirm an already-submitted declaration cannot be edited by the parent. | 1. After submitting a declaration (MF03-WEB-014), look for any edit/delete control on the "Current allergy profile" list. | No edit/delete action exists on submitted declarations — only an informational note stating that changes must go through the school's Medical Staff (BR-031). | At least one declaration already submitted for the active child. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Chronic Condition Declaration (Parent, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Chronic Condition Declaration (Manual Web) |
| Test requirement | Submit a chronic condition (underlying illness) declaration for the active child at `/parent/chronic-conditions`, mirroring the allergy declaration's severity/proof rules. |
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
| MF03-WEB-018 | Submit a chronic condition declaration with notes on current medication/handling. | 1. Log in as Parent, go to `/parent/chronic-conditions` with a child selected.<br>2. Pick a condition category, name/detail, severity, and fill the "Notes (current medication, emergency handling)" field.<br>3. Click "Submit declaration". | Submission succeeds, toast confirms it is pending Medical Staff verification (MSG-036). The new item appears under "Declared chronic conditions" with a PENDING badge and the entered notes visible. | Logged in as Parent, active child selected. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-019 | Try to submit a SEVERE chronic condition without a diagnostic document — must be blocked. | 1. On the declaration form, select severity "Severe", leave the diagnostic-document field empty.<br>2. Click "Submit declaration". | Blocked with an inline error requiring a diagnostic document for Severe (BR-031). No request sent. | Logged in as Parent, active child selected. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Parent Health Overview (Read-Only, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Parent Health Overview (Manual Web) |
| Test requirement | View the consolidated, read-only health summary for the active child at `/parent/health` (health record status, allergies, chronic conditions, special meal requests), scoped strictly to the currently selected child. |
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
| MF03-WEB-020 | View the health overview page for the active child, confirming allergy/chronic/special-meal declarations submitted earlier show up here without a manual reload. | 1. Submit an allergy declaration on `/parent/allergies` (MF03-WEB-014).<br>2. Navigate to `/parent/health`. | The new allergy declaration appears in the "Declared allergy profile" table with its PENDING status, without needing a page refresh. Health-record status banner reflects the overall verification state (BR-031). | Logged in as Parent; a declaration was just submitted for the active child. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-021 | Switch the active child (if the parent has 2+ children) and confirm the health overview updates to the new child only. | 1. On any parent page with a child switcher, select a different linked child.<br>2. Go to `/parent/health`. | Every section (record status, allergies, chronic conditions, special meal requests) reloads and shows ONLY data for the newly selected child — no residual data from the previously selected child. | Logged in as Parent with at least 2 linked children who have different health data. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Health Declaration Review & Verification (Medical Staff, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Health Declaration Review & Verification (Manual Web) |
| Test requirement | Review and verify/reject Allergy and Chronic Condition declarations at `/medical-staff/health-reviews`, including the mandatory-proof gate for Severe declarations (BR-031) and the append-only "verified = view-only" rule. |
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
| **Allergy module** | | | | | | | | | | | | | | |
| MF03-WEB-022 | Approve a pending non-Severe allergy declaration. | 1. Log in as Medical Staff, go to `/medical-staff/health-reviews`, module toggle on "Allergy".<br>2. On the "Pending" tab, click "View & review" on a Mild/Moderate declaration.<br>3. Click "Approve — Verify (MSG-036)". | Declaration moves from "Pending" to "Verified" tab immediately. Toast confirms "Allergy profile updated successfully and alerts set. (MSG-036)". The record becomes view-only afterward (no re-approve control). | Logged in as Medical Staff; at least one Mild/Moderate pending allergy declaration exists. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-023 | Try to approve a Severe allergy declaration that has no attached medical proof — must be blocked. | 1. On the Pending tab, open a Severe declaration with no proof file attached.<br>2. Observe the "Approve" button. | A warning banner states the Severe declaration is missing required medical proof (BR-031). The "Approve — Verify" button is disabled (with a tooltip explaining why). | Logged in as Medical Staff; a Severe pending declaration with no proof file exists. | Pending | | | Pending | | | Pending | | | Server also enforces this gate independently of the UI |
| MF03-WEB-024 | Reject a pending declaration with a required reason, requesting the parent to supplement information. | 1. Open a pending declaration, click "Reject — needs more info".<br>2. Try clicking "Send request for more info" with the reason box empty — confirm it stays disabled.<br>3. Type a reason, then click "Send request for more info". | Step 2: the send button stays disabled with an inline note that a reason is required. Step 3: request sent successfully, toast confirms the parent will receive the request; declaration stays out of "Verified". | Logged in as Medical Staff; a pending declaration exists. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-025 | Confirm a Verified declaration is view-only (cannot be re-approved or reversed). | 1. Go to the "Verified" tab, open any verified allergy declaration. | Dialog shows the details read-only with a note "Verified — view-only mode (not re-reviewable via UI)" and only a "Close" button — no Approve/Reject controls. | At least one verified allergy declaration exists. | Pending | | | Pending | | | Pending | | | |
| **Chronic condition module** | | | | | | | | | | | | | | |
| MF03-WEB-026 | Approve a pending chronic condition declaration and confirm it becomes visible to Teacher/Kitchen roles. | 1. Switch the module toggle to "Chronic conditions".<br>2. Open a pending item, click "Verify".<br>3. Log in (separate tab) as the Teacher of the same student's class, open `/teacher/students/{id}`. | Medical Staff side: declaration moves to "Verified" tab. Teacher side: the condition's underlying data becomes reflected in the student's health status once verified (verification is the single source of truth, BR-031). | Logged in as Medical Staff; a pending chronic condition declaration exists for a student with a known homeroom teacher. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Health Record Detail & Clinical Update (Medical Staff, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Health Record Detail & Clinical Update (Manual Web) |
| Test requirement | View a student's clinical health file and update clinical notes/medication/emergency contact at `/medical-staff/health-records/{id}` (UC-094), confirming the update never silently flips the allergy verification status (BR-031/BR-172). |
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
| MF03-WEB-027 | View a student's health record detail page, including the collapsible declared-allergy/chronic-condition/special-diet panels. | 1. Log in as Medical Staff, go to `/medical-staff/health-records`, click a student row. | Detail page shows clinical fields (medical conditions, emergency medication, emergency contact, emergency instructions), plus 3 collapsible read-only panels (Allergies, Chronic conditions, Special diet) each linking to its own review screen — none of the 3 panels are editable here. | Logged in as Medical Staff; at least one student exists. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-028 | Update the clinical notes/medication/emergency contact and confirm allergy verification status is unaffected. | 1. On the detail page, note the current allergy verification badge (e.g. NOT VERIFIED).<br>2. Click "Update record", fill "Clinical notes" (required) plus medication/contact/instructions.<br>3. Click "Save (MSG-036)". | Save succeeds (MSG-036 toast), the 4 clinical fields update on the page. The allergy verification badge is UNCHANGED by this save — clinical updates never auto-verify allergy/health status (BR-031, BR-172). | Logged in as Medical Staff; student has an unverified allergy declaration. | Pending | | | Pending | | | Pending | | | Regression guard for a previously-fixed bug where saving clinical notes force-verified the record |
| MF03-WEB-029 | Open an attached medical document from the "Attached medical documents" panel. | 1. On the detail page, in "Attached medical documents", click a listed file. | The real file opens/downloads (not a placeholder) via the authenticated document endpoint — Medical Staff role only. | A student with at least one uploaded medical document exists. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Health History Timeline (Medical Staff, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Health History Timeline (Manual Web) |
| Test requirement | View the read-only, append-only health history timeline for a student at `/medical-staff/health-records/{id}/history` (UC-146, BR-172). |
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
| MF03-WEB-030 | View a student's health history timeline after verifying an allergy declaration and updating clinical notes. | 1. Verify an allergy declaration (MF03-WEB-022) and save a clinical update (MF03-WEB-028) for the same student.<br>2. Click "Health History (UC-146)" on that student's detail page. | Timeline shows both events in chronological order (newest first or oldest first, consistently), each with an event-type badge, timestamp, description, and the acting user's name. No edit/delete control exists anywhere on this page (BR-172). | Both prior actions completed for the same student. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-031 | Export the health history timeline. | 1. On the timeline page, click "Export". | Export completes (PDF/Excel per BR-178) without error; confirmation toast/message shown. | A student with at least one history event exists. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Student Roster & Detail (Teacher, Read-Only, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Student Roster & Detail (Manual Web) |
| Test requirement | View the homeroom class roster at `/teacher/students` (with profile filter tabs) and a student's read-only detail tabs at `/teacher/students/{id}`, confirming Teacher can never edit health/allergy data (BR-172). |
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
| MF03-WEB-032 | Filter the homeroom roster using the profile tabs (All / Allergy Warning / Special Diet / Normal). | 1. Log in as Teacher, go to `/teacher/students`.<br>2. Click each of the 4 tabs in turn: "All", "Allergy Warning", "Special Diet", "Normal". | The roster is restricted to the teacher's own homeroom class throughout. Each tab filters correctly: "Allergy Warning" shows only students with declared allergies, "Special Diet" only students with an approved special meal request, "Normal" only students with neither. | Logged in as Teacher (homeroom class assigned); the class has a mix of students with/without allergies and special diets. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-033 | Open a student's detail page and browse the 3 read-only tabs (Allergy / Special Meal / Meal Records). | 1. Click a student row on `/teacher/students`.<br>2. Click each of the 3 tabs: "Allergy", "Special Meal", "Meal Records". | Each tab shows a "Read-only mode (BR-172)" info banner and no editable controls anywhere on the page. Allergy tab shows allergen + severity + verification status; Special Meal tab shows diet type + justification + approval status; Meal Records tab shows the student's own meal history only. | Logged in as Teacher; a student in the homeroom class exists with allergy + special meal + meal record data. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-034 | Try to open a student detail page for a student NOT in the teacher's homeroom class by typing the URL directly. | 1. As Teacher, find/guess a student ID from another class (e.g. via the network tab or a shared code).<br>2. Navigate directly to `/teacher/students/{that-id}`. | Blocked — shows a "student not found" message and a link back to the roster; the other class's student data is never rendered, even though the ID was reachable by URL. | Logged in as Teacher; a student ID belonging to a different homeroom class is known. | Pending | | | Pending | | | Pending | | | Regression guard for client-side-only class filtering |

---

## Feature: Cross-Role Access Control & Data Scope (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Cross-Role Access Control & Data Scope (Manual Web) |
| Test requirement | Confirm, through the browser (not raw API calls), that each role's MF03 screens are scoped to only the data that role is authorized to see: Parent → own linked children only, Teacher → own homeroom class only, School Admin/Medical Staff → own school only. |
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
| MF03-WEB-035 | Confirm a Parent's child switcher and every MF03 screen only ever list the parent's own linked children — never a school-wide list. | 1. Log in as a Parent linked to 2 children.<br>2. Open the child switcher — confirm exactly the 2 linked children are listed, no more.<br>3. Visit `/parent/allergies`, `/parent/chronic-conditions`, `/parent/health` for each child in turn. | The child switcher never shows more than the parent's own linked children. Every one of the 3 pages only ever displays data for the currently selected child — no other student's data is ever visible, even briefly during a data refresh. | Logged in as Parent with exactly 2 linked children with different data. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-036 | Confirm a School Admin only ever sees students/parents belonging to their own school (no cross-school data). | 1. Log in as School Admin of School A, browse `/school-admin/students` and `/school-admin/parents/{id}`. | Every student and parent listed belongs to School A. There is no way through the UI to browse or search for a School B student or parent. | Logged in as School Admin of a specific school; the system has students belonging to at least one other school. | Pending | | | Pending | | | Pending | | | |
| MF03-WEB-037 | Confirm a logged-out or session-expired user is redirected away from any MF03 page instead of seeing stale data. | 1. Log in as any MF03 role (Parent/Teacher/Medical Staff/School Admin), open one of the MF03 pages above.<br>2. Log out (or let the session expire), then use the browser Back button to return to that same MF03 page. | Redirected to `/login` — the MF03 page's data (student names, allergies, health notes) is never shown after the session has ended. | Logged in as any MF03 role. | Pending | | | Pending | | | Pending | | | |
