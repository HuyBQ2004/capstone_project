# Manual Web E2E Test — MF10 Feedback & Announcement Management

> Manual testing on a real browser, no direct API calls. Same format as `02_IT/sample.md`.

---

## Feature: Announcement Create, Publish & Revise (School Administration)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Announcement Create, Publish & Revise (Manual Web) |
| Test requirement | School Admin creates, publishes, and revises announcements at `/school-admin/announcements`, targeting one or more audience groups; every save appends a revision-history entry (BR-144); urgent announcements skip approval (BR-138). |
| Number of TCs | 6 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 6 | 0 |
| Round 2 | 0 | 0 | 6 | 0 |
| Round 3 | 0 | 0 | 6 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Create & Publish (`/school-admin/announcements/create`)** | | | | | | | | | | | | | | |
| MF10-WEB-001 | Create and publish a normal announcement to a single target group. | 1. Log in as School Admin, go to `/school-admin/announcements`.<br>2. Click "Create announcement".<br>3. Fill Title, check only "Parents — whole school" in Target Group, keep Level = "Normal", fill Content.<br>4. Click "Publish (MSG-047)". | Redirected to the announcement detail page. Toast confirms the announcement was published and sent to the selected audience. The detail page shows Status = Published, Target Group = "Parents — whole school". | Logged in as a valid School Admin managing at least one school. | Pending | | | Pending | | | Pending | | | |
| MF10-WEB-002 | Create an urgent announcement — publishes immediately without an approval step. | 1. On the create form, fill Title/Content, select Level = "Urgent — publish immediately (BR-138)".<br>2. Click "Publish (MSG-047)". | Announcement is published immediately (no pending/approval state). The detail page shows a red "Urgent announcement — published without approval (BR-138)" badge. | Logged in as School Admin. | Pending | | | Pending | | | Pending | | | |
| MF10-WEB-003 | Save an announcement as Draft, missing required fields is blocked. | 1. On the create form, leave Title empty, fill Content, click "Save draft".<br>2. Fill Title, uncheck every Target Group checkbox, click "Save draft" again. | Step 1: blocked with an inline error "Please fill in Title and Content before saving." Step 2: blocked with "At least one Target Group must be selected." No announcement created either time. | Logged in as School Admin. | Pending | | | Pending | | | Pending | | | Same validation applies to both Save draft and Publish |
| MF10-WEB-004 | Publish to multiple target groups at once (parents of one grade + teachers). | 1. On the create form, check "Parents — grade 1" and "Teacher", fill Title/Content.<br>2. Click "Publish (MSG-047)". | Publishes successfully. The detail page's "Sent to" field lists both selected groups. | Logged in as School Admin. | Pending | | | Pending | | | Pending | | | |
| **Revise & Revision History (`/school-admin/announcements/{id}/edit`)** | | | | | | | | | | | | | | |
| MF10-WEB-005 | Edit a published announcement's content and confirm a new revision-history entry is appended (BR-144). | 1. Open a published announcement's detail page, click "Edit".<br>2. Change Content, fill the required "Revision note" field, click "Save changes".<br>3. Reopen the detail page. | Toast confirms the update was saved and a revision was recorded (MSG-074, BR-144). The "Revision history" section on the detail page now shows a new entry with the revision note, timestamp, and the editor's name, in addition to any prior entries. | An already-published announcement exists (from MF10-WEB-001). | Pending | | | Pending | | | Pending | | | Field is called "revisionCount" in the admin list column ("N times") |
| MF10-WEB-006 | Try saving an edit without a revision note. | 1. Open the edit form for an existing announcement.<br>2. Change the Content field but leave "Revision note" empty, click "Save changes". | Blocked — the Revision note field is marked required (`*`); the form does not submit without it. | An announcement exists in edit mode. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Announcement Visibility Across Roles

### 1. General Information

| Field | Value |
|---|---|
| Feature | Announcement Visibility Across Roles (Manual Web) |
| Test requirement | Confirm every targeted role (Parent, Teacher, Medical Staff, Service Caterers, Supplier) can see published announcements in their own read-only `/announcements` list and detail page, without the School Admin's Edit button or revision history. |
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
| **Parent / Teacher / Medical Staff view** | | | | | | | | | | | | | | |
| MF10-WEB-007 | Parent sees a newly published announcement in their list and detail page. | 1. As Parent, go to `/parent/announcements`.<br>2. Find the announcement from MF10-WEB-001, click it. | Announcement appears in the list with title, publish date, author, and "Sent to" group. Detail page shows Title/Content/Target Group/Author/Publish date, but no "Edit" button and no revision-history section. | Announcement from MF10-WEB-001 targets "Parents — whole school", parent account belongs to that school. | Pending | | | Pending | | | Pending | | | |
| MF10-WEB-008 | Teacher and Medical Staff can each view the same published announcement list read-only. | 1. As Teacher, go to `/teacher/announcements`, open one item.<br>2. As Medical Staff, go to `/medical-staff/announcements`, open the same item. | Both roles see the announcement in their list; detail page is read-only (no Edit, no revision history) in both cases. | Announcement targets Teacher and/or is visible to all staff roles. | Pending | | | Pending | | | Pending | | | |
| MF10-WEB-009 | Urgent announcement is visually flagged for the receiving role. | 1. As Parent (or Teacher), open the urgent announcement created in MF10-WEB-002. | The list row shows an "Urgent" badge; the detail page shows the red "Urgent announcement — published without approval (BR-138)" badge. | Urgent announcement from MF10-WEB-002 exists and targets this role. | Pending | | | Pending | | | Pending | | | |
| **Search & filter** | | | | | | | | | | | | | | |
| MF10-WEB-010 | Filter the announcement list to "Urgent only" and search by title. | 1. As any receiving role, on `/*/announcements`, click "Urgent only" toggle.<br>2. Clear the toggle, type part of a known announcement title into the search box. | Toggled view shows only announcements flagged urgent. Search narrows the list to items whose title/content matches the typed text. | At least one urgent and one normal announcement exist for this role. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Supplier & Service Caterers Announcement Access

### 1. General Information

| Field | Value |
|---|---|
| Feature | Supplier & Service Caterers Announcement Access (Manual Web) |
| Test requirement | Confirm Supplier and Service Caterers accounts, which are business-partner roles rather than school staff, also receive and can read announcements targeted to them (e.g. `CATERER` audience code). |
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
| MF10-WEB-011 | Service Caterers sees an announcement targeted at "Catering unit". | 1. As School Admin, create/publish an announcement checking only "Catering unit" as Target Group.<br>2. As Service Caterers, go to `/service-caterers/announcements`, open it. | Announcement appears in the Service Caterers list and detail page, read-only, same as other roles. | School Admin account can publish; Service Caterers account is linked to the same school. | Pending | | | Pending | | | Pending | | | |
| MF10-WEB-012 | Supplier's announcement list only shows announcements actually targeted to them. | 1. As Supplier, go to `/supplier/announcements`.<br>2. Open the list, cross-check against announcements published by the school for other audiences only (e.g. "Teacher" only). | Supplier's list does not include announcements that never targeted a Supplier-relevant audience; it does include any that were targeted broadly enough to reach them. | At least one announcement targets a non-Supplier-only audience for comparison. | Pending | | | Pending | | | Pending | | | Confirm with Nam/Huy which audience code(s) map to Supplier visibility if the list looks empty |

---

## Feature: Parent Feedback Submission (24-Hour Window)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Parent Feedback Submission (Manual Web) |
| Test requirement | Parent submits meal feedback (`/parent/feedback/meal`) and food-quality feedback (`/parent/feedback/quality`) within the 24-hour serving window (BR-139); a rating below 3 stars requires a concern tag/comment (BR-140); illness keywords are auto-flagged to Medical Staff (BR-142). |
| Number of TCs | 6 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 6 | 0 |
| Round 2 | 0 | 0 | 6 | 0 |
| Round 3 | 0 | 0 | 6 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Meal Feedback (`/parent/feedback/meal`)** | | | | | | | | | | | | | | |
| MF10-WEB-013 | Submit a positive meal feedback (rating ≥ 3) for a dish served within 24 hours. | 1. Log in as Parent, select an active child, go to `/parent/feedback` → "Meal feedback".<br>2. Pick a meal session from the dropdown (only sessions inside the 24h window appear), pick a dish, rate 5 stars.<br>3. Add an optional comment, click "Submit feedback". | Success message "Meal feedback submitted successfully... (MSG-046)" is shown. The new feedback appears in the "Feedback history" table on `/parent/feedback` with Status = "Recorded". | Parent has an active child with at least one meal session served within the last 24 hours. | Pending | | | Pending | | | Pending | | | |
| MF10-WEB-014 | Rating below 3 stars requires a mandatory concern field. | 1. On the meal feedback form, select a meal + dish, rate 2 stars.<br>2. Leave the "Quality concern *" field empty, click "Submit feedback". | The Submit button stays effectively blocked/the required "Quality concern" field appears and must be filled (BR-140) before the form can be submitted. | Eligible meal session exists. | Pending | | | Pending | | | Pending | | | |
| MF10-WEB-015 | No eligible meal sessions — form shows the 24h-window empty state. | 1. Log in as Parent whose child has no meal served in the last 24 hours, go to "Meal feedback". | Warning message "No meals left in the 24-hour feedback window (MSG-022)" is shown instead of the form. | Active child with no meal session in the last 24h. | Pending | | | Pending | | | Pending | | | |
| **Food Quality Feedback (`/parent/feedback/quality`)** | | | | | | | | | | | | | | |
| MF10-WEB-016 | Submit session-level quality feedback with an attached photo. | 1. Go to "Food quality feedback", pick a meal session, rate 4 stars.<br>2. Add a comment, attach a JPG/PNG file ≤10MB, click "Submit feedback". | Success message shown (MSG-046). Feedback recorded without needing to pick a specific dish (session-level, no dish selector on this form). | Eligible meal session exists. | Pending | | | Pending | | | Pending | | | |
| MF10-WEB-017 | Comment containing an illness keyword is auto-flagged to Medical Staff (BR-142). | 1. Submit meal or quality feedback with a comment containing an illness phrase (e.g. "my child had a stomachache").<br>2. Open the submitted item from "Feedback history". | Feedback is recorded; its Status badge shows "Forwarded to Medical Staff (BR-142)" instead of "Recorded". Detail dialog notes Medical Staff was automatically notified (MSG-058). | Parent has an eligible meal session to submit against. | Pending | | | Pending | | | Pending | | | Server recomputes the illness flag — do not rely on any client-side keyword logic |
| **Feedback History (`/parent/feedback`)** | | | | | | | | | | | | | | |
| MF10-WEB-018 | Filter submitted-feedback history by date and status. | 1. On `/parent/feedback`, submit at least two feedback items on different days (one Recorded, one Forwarded).<br>2. Filter by Date, then clear and filter Status = "Forwarded to Medical Staff".<br>3. Click "Clear filter". | Date filter narrows the table to matching rows only. Status filter shows only forwarded items. "Clear filter" resets to the full history list. | At least two feedback items with different dates/status exist for the active child. | Pending | | | Pending | | | Pending | | | |

---

## Feature: School Administration Feedback Dashboard (Anonymized)

### 1. General Information

| Field | Value |
|---|---|
| Feature | School Administration Feedback Dashboard (Manual Web) |
| Test requirement | `/school-admin/feedback` aggregates parent feedback anonymized to class level (BR-145), separates illness-flagged items for Medical Staff (BR-142), and highlights low ratings requiring quality attention (BR-140). Current UI is read-only for School Admin; drafting a reply to feedback is not exposed on this screen. |
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
| MF10-WEB-019 | Dashboard summary cards reflect total feedback count, average rating, and low-rating count. | 1. Log in as School Admin, go to `/school-admin/feedback`.<br>2. Read the "Total feedback", "Average rating", and "Feedback below 3 stars" cards. | Card values match what is visible in the table below them (e.g. "Total feedback" equals the row count with no filter applied). Average rating card turns green when ≥4, amber otherwise. | At least a few parent feedback items exist for the school. | Pending | | | Pending | | | Pending | | | |
| MF10-WEB-020 | Table never shows an individual student identity, only the class (BR-145). | 1. On `/school-admin/feedback`, inspect the table columns for several rows. | Only a "Class" badge (e.g. "3A") is shown per row — no student name or ID column anywhere in the table or filters. | Feedback rows exist for more than one class. | Pending | | | Pending | | | Pending | | | Matches BR-145 aggregation-to-class-only rule |
| MF10-WEB-021 | Illness-flagged feedback is separated with a banner and links to incident cross-check. | 1. Ensure at least one illness-flagged feedback exists (from MF10-WEB-017).<br>2. Reload `/school-admin/feedback`. | A red banner "N feedback item(s) with illness indicators (BR-142)" appears above the summary cards, with a "Cross-check open incidents" link to `/school-admin/incidents`. The matching row shows a "Forwarded to Medical Staff (BR-142)" badge. | At least one illness-flagged feedback item exists. | Pending | | | Pending | | | Pending | | | |
| MF10-WEB-022 | Filter dashboard rows to "Below 3 stars" and "Illness indicators". | 1. Use the filter dropdown above the table, select "Below 3 stars".<br>2. Switch to "Illness indicators (BR-142)". | Table narrows to only rows matching the selected filter each time; row count in "Feedback list (N)" heading updates accordingly. | Both low-rating and illness-flagged rows exist. | Pending | | | Pending | | | Pending | | | Reply/response action for feedback is not present in this screen — verify with Nam/Huy whether a reply UI is planned for School Admin or stays Service Caterers-only |

---

## Feature: Notification Preferences (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Notification Preferences (Manual Web) |
| Test requirement | Confirm the per-type notification settings page (`/service-caterers/notification-settings`) and the quicker avatar-menu "Notification settings" dialog (all roles) save Push/Email preferences, that CRITICAL alert types cannot be disabled, and Quiet Hours (22:00–06:00, BR-158) is explained without blocking critical alerts (BR-138). |
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
| **`/service-caterers/notification-settings`** | | | | | | | | | | | | | | |
| MF10-WEB-023 | Toggle off a non-critical notification type and save. | 1. Log in as Service Caterers, go to `/service-caterers/notification-settings`.<br>2. Uncheck a non-critical type (e.g. "Menu approved / published"), click "Save settings". | Toast confirms the settings were saved. Reloading the page shows the toggle still unchecked (persisted). | Logged in as Service Caterers. | Pending | | | Pending | | | Pending | | | |
| MF10-WEB-024 | CRITICAL notification types cannot be disabled. | 1. On the same page, locate the three types marked "CRITICAL — always sent" (Incident alert, Food recall, Allergy alert).<br>2. Try clicking their checkboxes. | All three checkboxes are checked and disabled (cannot be unchecked) — cannot be turned off through the UI. | Logged in as Service Caterers. | Pending | | | Pending | | | Pending | | | BR-138 — critical alerts always deliver |
| MF10-WEB-025 | Toggle Email fallback and read the Quiet Hours explanation. | 1. On the same page, toggle "Auto Email fallback when push fails" off then on, click "Save settings".<br>2. Read the "Quiet Hours 22:00–06:00 (BR-158)" info card. | Setting saves without error either way. The info card states normal notifications are delayed until 06:00, while CRITICAL alerts (incident/recall/allergy) are unaffected and sent immediately regardless of Quiet Hours. | Logged in as Service Caterers. | Pending | | | Pending | | | Pending | | | BR-158/BR-159 |
| **Avatar menu "Notification settings" dialog (all roles)** | | | | | | | | | | | | | | |
| MF10-WEB-026 | Any logged-in role can open and save the quick notification dialog from the topbar. | 1. Log in as any role (e.g. Parent), click the avatar in the topbar → "Notification settings".<br>2. Toggle "Push Notifications" and "Email Notifications (fallback — BR-159)", click "Save settings". | Dialog closes without error, values persist on reopening. Available identically regardless of role. | Already logged in as any role. | Pending | | | Pending | | | Pending | | | Separate, simpler dialog from the dedicated `/service-caterers/notification-settings` page — no per-type breakdown |
