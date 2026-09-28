# Manual Web E2E Test — MF07 Food Safety Inspection

> Manual testing on a real browser, no direct API calls. Same format as `02_IT/sample.md`.

---

## Feature: Kitchen Safety Inspection — Conduct & Submit (Medical Staff)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Kitchen Safety Inspection Wizard (Manual Web) |
| Test requirement | Medical Staff runs a new kitchen inspection session at `/medical-staff/conduct-inspection`: per-item Pass/Fail wizard across checklist groups, evidence photo/reason gate on Fail, submit and score computation, warning banner under the 60% threshold. |
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
| **Wizard navigation** | | | | | | | | | | | | | | |
| MF07-WEB-001 | Open the kitchen inspection wizard and move between checklist groups. | 1. Log in as Medical Staff, go to `/medical-staff/conduct-inspection/history`.<br>2. Click "Create new inspection session".<br>3. Use the group dropdown at the top of the card to jump to a non-adjacent group, then click "Back".<br>4. Click "Next · x/y" to advance sequentially. | Only the currently selected group is shown (no accordion of all groups at once). Jumping via the dropdown changes the visible group but does not count items as evaluated. "Back" is disabled on the first group. | Logged in as Medical Staff, checklist groups already exist (seeded via UC-186). | Pending | | | Pending | | | Pending | | | |
| **Evaluate items & evidence gate** | | | | | | | | | | | | | | |
| MF07-WEB-002 | Mark an item "Fail" — evidence photo and reason become mandatory. | 1. In any group, click "Fail" on one checklist item.<br>2. Try clicking "Complete inspection session" without adding a photo or reason.<br>3. Add a photo via camera capture and type a reason, then retry. | After step 2: submit stays blocked and an inline warning lists the item as missing photo/reason. After step 3: the warning for that item clears once both a photo and a reason are present. | Wizard open, at least one checklist item visible. | Pending | | | Pending | | | Pending | | | BR-207 |
| MF07-WEB-003 | Mark an item "Pass" — photo/reason stay optional. | 1. Click "Pass" on a checklist item.<br>2. Leave photo and reason empty.<br>3. Proceed to the next group. | No warning is shown for this item; progress counter increases. Photo upload button still available but not required. | Wizard open. | Pending | | | Pending | | | Pending | | | |
| MF07-WEB-004 | Submit is blocked while any item is unanswered. | 1. Leave at least one checklist item in every group unanswered.<br>2. Scroll to the "Verify certification & complete session" card.<br>3. Observe the "Complete inspection session" button state. | Button is disabled; an inline warning reports how many items remain unanswered. | Wizard open, not all items answered. | Pending | | | Pending | | | Pending | | | BR-101 |
| **Submit & scoring** | | | | | | | | | | | | | | |
| MF07-WEB-005 | Submit a session scoring at or above 60% (all/most items Pass). | 1. Mark every item across all groups as Pass (or enough to stay ≥60%).<br>2. Click "Complete inspection session". | Success banner shows the saved session with score ≥60% and result "Passed" — session moves to await Service Caterers confirmation, no warning banner shown. | Wizard open, all items answered, evidence rules satisfied for any Fail items. | Pending | | | Pending | | | Pending | | | MSG-043 |
| MF07-WEB-006 | Submit a session scoring below 60% — School Administration warning fires, kitchen keeps operating. | 1. Mark enough items as Fail so the running score drops below 60%, each with a photo and reason.<br>2. Click "Complete inspection session". | A warning banner states the score is below 60%, the result is saved as Failed, and School Administration is alerted — the banner explicitly notes the kitchen continues operating normally (no service block). | Wizard open, all items answered, Fail items carry photo + reason. | Pending | | | Pending | | | Pending | | | MSG-093, descope: no serving block |

---

## Feature: Kitchen Inspection Checklist Management (Medical Staff)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Checklist Groups & Items Management (Manual Web) |
| Test requirement | Medical Staff self-manages checklist groups/items and legal-reference notes at `/medical-staff/conduct-inspection/checklist`, without System Admin approval. |
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
| MF07-WEB-007 | Add a new checklist group and item, save. | 1. Open "Manage Checklist" from the inspection history page header.<br>2. Click "Add group", type a name, save.<br>3. Select the new group, click "Add item", type a name, save.<br>4. Click "Save changes". | New group and item appear in the draft immediately; after "Save changes" they persist (page reloads from the API and the new group/item keep their names). | Logged in as Medical Staff. | Pending | | | Pending | | | Pending | | | UC-186, BR-210 |
| MF07-WEB-008 | Delete an item that is already referenced by a saved inspection session. | 1. Pick an item known to appear in a past Fail result (badge "Referenced" visible).<br>2. Click the delete icon for that item. | A confirmation dialog explains the item is used in saved sessions and that deleting only hides it from new sessions, keeping history intact. Confirming removes it from the draft. | At least one past inspection session recorded this item as Failed. | Pending | | | Pending | | | Pending | | | MSG-094 — warning only, not a hard block |
| MF07-WEB-009 | Reorder groups/items with the up/down buttons. | 1. In the checklist manager, click the down-arrow on the first group.<br>2. Click "Save changes". | The group moves to the second position in the list and keeps that order after saving/reloading. | At least two checklist groups exist. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Kitchen Inspection Record & Caterer Confirmation

### 1. General Information

| Field | Value |
|---|---|
| Feature | Kitchen Inspection Record View & Caterer Confirmation (Manual Web) |
| Test requirement | Auto-generated inspection record at `/medical-staff/conduct-inspection/[id]/record`, and Service Caterers confirming the result at `/service-caterers/kitchen-inspections/[id]` before the record locks. |
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
| MF07-WEB-010 | View and print the auto-generated inspection record. | 1. As Medical Staff, open a submitted session's detail, click "Record" (or navigate to `.../record`).<br>2. Click "Print". | The record preview shows the school name, session ID, and auto-generated content. Clicking "Print" opens a print dialog with the same content. | A kitchen inspection session has been submitted. | Pending | | | Pending | | | Pending | | | |
| MF07-WEB-011 | Service Caterers views a submitted kitchen inspection and confirms the result. | 1. Log in as Service Caterers, go to `/service-caterers/kitchen-inspections`.<br>2. Open the session awaiting confirmation.<br>3. Review the checklist items, failed-item reasons/photos, and record content.<br>4. Click "Confirm inspection result". | Before confirming: status shows "awaiting caterers confirmation". After: a success toast appears, status badge updates to "Confirmed" with confirmer name and timestamp; the record becomes read-only for edits. | A kitchen inspection session exists with status awaiting caterer confirmation. | Pending | | | Pending | | | Pending | | | BR-221 |
| MF07-WEB-012 | Service Caterers view is read-only — no edit controls for checklist or record content. | 1. As Service Caterers, open any kitchen inspection detail page.<br>2. Check for any input fields, edit buttons, or item Pass/Fail toggles besides the confirm button. | Only the "Confirm inspection result" button is interactive (and only before confirmation); all checklist items and the record content are plain read-only text. | Logged in as Service Caterers. | Pending | | | Pending | | | Pending | | | Editing checklist/record remains exclusive to Medical Staff |

---

## Feature: Three-Step Food Inspection Records & Medical Review (Medical Staff)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Three-Step Inspection Records List & Medical Review (Manual Web) |
| Test requirement | Medical Staff browses three-step inspection records at `/medical-staff/inspections`, opens a session detail, and records a separate medical review result without overwriting the auto-computed overall result. |
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
| MF07-WEB-013 | Filter the three-step inspection records list by date range and overall result. | 1. Go to `/medical-staff/inspections`.<br>2. Set "From date"/"To date".<br>3. Set the "Result" filter to "Failed". | The table only lists sessions within the date range whose overall result is Failed; the empty state message shows if none match. | At least one Failed and one Passed session exist. | Pending | | | Pending | | | Pending | | | |
| MF07-WEB-014 | Open a session detail and record a medical review result that differs from the auto-computed overall. | 1. Open a session whose overall result is "Passed".<br>2. In the "Medical review" section, click "Failed".<br>3. Try to save without a reason — observe the error.<br>4. Enter a reason and save. | Save is blocked with an error until a reason is entered for a Failed review. After saving: a separate "Medical review" badge (Failed) is shown alongside the unchanged auto-computed "Passed" overall badge — the original overall result is never overwritten. | A three-step inspection session with overall = Passed exists. | Pending | | | Pending | | | Pending | | | |
| MF07-WEB-015 | Print an official record — blocked while the caterer has not finalized the session. | 1. Open a session still "In progress" (caterer has not finalized).<br>2. Check the "Print official record" button state and tooltip. | The button is disabled with a tooltip explaining the caterer has not finalized the document yet — no official print is issued for in-progress data. | A three-step session exists with overall = "In progress". | Pending | | | Pending | | | Pending | | | |

---

## Feature: Violation Record — Create, Sign, Resolve (Medical Staff)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Violation Registry — Record Creation & Resolution Workflow (Manual Web) |
| Test requirement | Medical Staff records a caterer-unit violation at `/medical-staff/violations/new`, exports/prints the PDF, uploads the signed scan, then enters resolution — LOW severity closes directly, MEDIUM/HIGH routes to School Admin approval. |
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
| **Create record** | | | | | | | | | | | | | | |
| MF07-WEB-016 | Create a violation record from a failed kitchen/three-step inspection, following a failed inspection that raises the violation. | 1. From a Failed inspection context (or directly), go to `/medical-staff/violations/new`.<br>2. Fill Location, Occurred-at, pick a Caterer representative, Severity, write the record content (or insert the suggested template), attach at least one evidence photo.<br>3. Click "Save record". | Save is blocked until all required fields are filled (location, representative, severity, occurred-at, non-empty content, ≥1 evidence file). After saving, the browser navigates to the new record's detail page (`/violations/[id]`), not to a "raise resolution" step. | Logged in as Medical Staff; at least one approved caterer representative exists. | Pending | | | Pending | | | Pending | | | MSG-085/086, BR-183 |
| MF07-WEB-017 | Save without required fields shows the missing-fields error. | 1. On `/medical-staff/violations/new`, leave the evidence photo empty (or another required field).<br>2. Click "Save record". | An error toast lists the required fields still missing; the form does not submit. | On the new-violation form. | Pending | | | Pending | | | Pending | | | MSG-085 |
| **Sign & lock the record** | | | | | | | | | | | | | | |
| MF07-WEB-018 | Export/print the PDF, then upload the signed scan to lock the record. | 1. Open a DRAFTED violation record.<br>2. Click "Print record (PDF)" — confirm a print window opens and the progress bar advances to "PDF exported".<br>3. Click "Upload signed scan/photo" and select an image file. | After printing: progress bar shows "PDF exported" as the current step. After upload: a "Record locked" badge appears, and the step advances to "Signed uploaded" — the resolution section becomes usable. | A DRAFTED violation record exists. | Pending | | | Pending | | | Pending | | | BR-211, MSG-095 |
| **Resolve** | | | | | | | | | | | | | | |
| MF07-WEB-019 | Resolve a LOW severity violation — closes directly without School Admin approval. | 1. Open a signed/locked violation record with Severity = LOW.<br>2. Fill "Action taken", click the resolve button. | Success toast confirms the record is closed directly (LOW severity does not require School Admin approval); status updates to closed. | Signed/locked violation record, severity LOW. | Pending | | | Pending | | | Pending | | | MSG-089, BR-186 |
| MF07-WEB-020 | Resolve a MEDIUM/HIGH severity violation — sanction level required, routes to School Admin. | 1. Open a signed/locked violation record with Severity = MEDIUM or HIGH.<br>2. Fill "Action taken" but leave "Unit sanction level" empty, try to submit — observe it is blocked.<br>3. Pick a sanction level, submit. | Step 2: submit stays disabled/blocked without a sanction level. Step 3: success toast confirms the record was sent to School Administration for approval (not closed directly). | Signed/locked violation record, severity MEDIUM or HIGH. | Pending | | | Pending | | | Pending | | | MSG-087, BR-185/BR-186 |

---

## Feature: Violation Approval (School Admin)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Violation Resolution Approval (Manual Web) |
| Test requirement | School Admin reviews MEDIUM/HIGH violation resolutions pending approval at `/school-admin/violations` and Approves or Rejects with a mandatory reason. |
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
| MF07-WEB-021 | Approve a violation resolution pending approval. | 1. Log in as School Admin, go to `/school-admin/violations`.<br>2. Find a row with resolution "Pending approval", click "Approve". | Success toast confirms the resolution is approved and the disciplinary action applies; the row's resolution status updates to Approved. | A violation record exists with resolution = PENDING_APPROVAL (MEDIUM/HIGH severity). | Pending | | | Pending | | | Pending | | | MSG-089 |
| MF07-WEB-022 | Reject a violation resolution — reason mandatory, returns to Medical Staff. | 1. On the same row, click "Reject".<br>2. Try to confirm without typing a reason — observe it is blocked.<br>3. Type a reason, confirm rejection. | Step 2: confirm button stays disabled without a reason. Step 3: success toast confirms rejection with the record sent back to Medical Staff for revision. | A violation record exists with resolution = PENDING_APPROVAL. | Pending | | | Pending | | | Pending | | | AF1, BR-186 |

---

## Feature: Corrective Action (CAPA) — Plan & Evaluate (Medical Staff)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Corrective Action Plan Drafting & Effectiveness Evaluation (Manual Web) |
| Test requirement | Medical Staff drafts a CAPA plan (from an incident or a failed inspection) at `/medical-staff/corrective-actions/[id]`, sends it for School Admin approval, then evaluates the caterer's submitted evidence. |
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
| **Draft & submit** | | | | | | | | | | | | | | |
| MF07-WEB-023 | Draft a corrective action plan and submit for School Admin approval. | 1. Log in as Medical Staff, go to `/medical-staff/corrective-actions`, filter status "Draft".<br>2. Open a DRAFT CAPA (originating from a failed inspection step, tagged "Inspection").<br>3. Fill "Corrective & preventive content" and pick a "Responsible unit" (caterer), click "Send for School Admin approval". | Submit is blocked until both fields are filled. After submit: success toast confirms the plan was sent to School Administration; status badge updates to "Pending Admin approval". | A DRAFT CAPA exists, sourced from a failed inspection step or an incident. | Pending | | | Pending | | | Pending | | | BR-095, BR-152, BR-153 |
| MF07-WEB-024 | View a CAPA rejected previously — resubmit with the same rework flow. | 1. Open a DRAFT CAPA that carries a previous reject reason.<br>2. Confirm the reject reason banner is visible.<br>3. Edit content, resubmit. | The reject reason from School Admin is shown before resubmission. Resubmitting sends the plan for approval again. | A CAPA exists in DRAFT with a stored reject reason. | Pending | | | Pending | | | Pending | | | |
| **Evaluate effectiveness** | | | | | | | | | | | | | | |
| MF07-WEB-025 | Accept caterer-submitted evidence — CAPA completes. | 1. Open a CAPA with status "Submitted evidence".<br>2. Review the evidence note/photos.<br>3. Click "Pass — mark complete". | Success toast confirms the CAPA is marked COMPLETED; status badge updates accordingly. | A CAPA exists with status SUBMITTED_EVIDENCE. | Pending | | | Pending | | | Pending | | | BR-216 |
| MF07-WEB-026 | Reject caterer-submitted evidence — sends the CAPA back for rework. | 1. Open a CAPA with status "Submitted evidence".<br>2. Click "Not acceptable".<br>3. Try to confirm without a reason — observe it is blocked.<br>4. Enter a reason, confirm "Request rework". | Step 3: confirm stays blocked without a reason. Step 4: success toast confirms the CAPA returned to the caterer for rework without needing School Admin re-approval; status returns to "In progress" with the rework reason visible. | A CAPA exists with status SUBMITTED_EVIDENCE. | Pending | | | Pending | | | Pending | | | BR-216 |

---

## Feature: Corrective Action (CAPA) — Approve (School Admin)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Corrective Action Plan Approval (Manual Web) |
| Test requirement | School Admin reviews CAPA plans pending approval at `/school-admin/corrective-actions` and Approves or Rejects before the caterer can start work. |
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
| MF07-WEB-027 | Approve a CAPA plan pending approval — verification note required. | 1. Log in as School Admin, go to `/school-admin/corrective-actions`.<br>2. Open the "Approve plan" dialog on a row with status "Pending Admin approval".<br>3. Try "Approve plan" without a verification note — observe it is blocked.<br>4. Enter a note, click "Approve plan". | Step 3: blocked with an error requiring the verification note. Step 4: success toast confirms the plan is approved and the caterer can now accept and start the work. | A CAPA exists with status PENDING_ADMIN_APPROVAL. | Pending | | | Pending | | | Pending | | | UC-005, BR-153 |
| MF07-WEB-028 | Reject a CAPA plan pending approval — reason required, returns to Medical Staff DRAFT. | 1. On the same dialog, click "Reject plan" without typing a reason — observe it is blocked.<br>2. Type a reason, confirm rejection. | Step 1: blocked without a reason. Step 2: success toast confirms the plan is sent back to Medical Staff (status returns to DRAFT) with the reject reason attached. | A CAPA exists with status PENDING_ADMIN_APPROVAL. | Pending | | | Pending | | | Pending | | | EX1, UC-005 |

---

## Feature: Corrective Action (CAPA) — Accept & Submit Evidence (Service Caterers)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Corrective Action Execution by Service Caterers (Manual Web) |
| Test requirement | Service Caterers accepts an approved CAPA at `/service-caterers/corrective-actions`, then submits corrective evidence (description, root cause, photos, re-inspection confirmation). |
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
| MF07-WEB-029 | Accept an approved CAPA and start work. | 1. Log in as Service Caterers, go to `/service-caterers/corrective-actions`.<br>2. On a row with status "Approved for implementation", click "Accept work". | Success toast confirms the CAPA is accepted; status updates to "In progress" and the "Submit evidence" action becomes available on that row. | A CAPA exists with status APPROVED_FOR_IMPLEMENTATION assigned to the logged-in caterer. | Pending | | | Pending | | | Pending | | | BR-216 |
| MF07-WEB-030 | Submit corrective evidence — all required fields enforced, including mandatory re-inspection. | 1. On an "In progress" CAPA row, click "Submit evidence".<br>2. Try "Submit result" leaving description, root cause, or photo empty, and with the re-inspection checkbox unchecked — observe each is blocked.<br>3. Fill description + root cause, capture ≥1 evidence photo, keep re-inspection checked, submit. | Step 2: submission is blocked with an inline error naming the missing field(s), including when re-inspection is unchecked. Step 3: success toast confirms the evidence was submitted and routed to Medical Staff for effectiveness evaluation; status updates to "Submitted evidence". | An IN_PROGRESS CAPA exists. | Pending | | | Pending | | | Pending | | | BR-096, MSG-049 |
| MF07-WEB-031 | Resubmit evidence after a rework request. | 1. Open a CAPA that shows a "rework reason" banner (returned by Medical Staff).<br>2. Click "Rework & submit".<br>3. Fill the same required fields and submit again. | The rework reason from Medical Staff is visible before resubmission. After resubmit: status returns to "Submitted evidence" for a new evaluation round. | A CAPA exists with status IN_PROGRESS and a stored rework reason. | Pending | | | Pending | | | Pending | | | |

---

## Feature: School Admin Oversight — Compliance & Read-only Registers

### 1. General Information

| Field | Value |
|---|---|
| Feature | Daily Compliance Dashboard & Read-only Inspection Registers (Manual Web) |
| Test requirement | School Admin monitors today's inspection compliance at `/school-admin/compliance`, signs off the daily safety status, and browses read-only registers at `/school-admin/inspections` and related CAPA/violation links. |
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
| MF07-WEB-032 | Sign off today's food-safety status once, second attempt blocked same day. | 1. Log in as School Admin, go to `/school-admin/compliance`.<br>2. Click "Sign off safety status" once.<br>3. Reload the page and check the button state. | After signing: success toast confirms the sign-off is saved with signer name and timestamp; a confirmation line appears at the bottom of the page. On reload: the button shows "Already signed" and is disabled — a second sign-off the same day is not allowed. | At least one inspection session recorded for today; not yet signed today. | Pending | | | Pending | | | Pending | | | UC-027, one signature per day |
| MF07-WEB-033 | Failed-step banner links to CAPA and violation lists. | 1. On `/school-admin/compliance`, with at least one Failed three-step session today, check the danger banner.<br>2. Click "Open CAPA list" then, separately, "View related violations". | Banner names the failed session IDs. Both links navigate to `/school-admin/corrective-actions` and `/school-admin/violations` respectively, in the same School Admin shell. | Today has at least one session with step1/step2/step3 = FAILED. | Pending | | | Pending | | | Pending | | | BR-097 |
| MF07-WEB-034 | Browse the read-only three-step inspection register and open a session detail. | 1. Go to `/school-admin/inspections`.<br>2. Search/click into any session row. | The list shows session ID, date, meal, step 1/2/3 badges, inspector, and overall result. Opening a row shows the full read-only Forms 1/2/3 snapshot — no edit controls anywhere on the page. | At least one three-step inspection session exists. | Pending | | | Pending | | | Pending | | | QĐ 1246/QĐ-BYT, view-only |
