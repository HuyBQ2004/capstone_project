# Manual Web E2E Test — MF05 Menu & Diet Management

> Manual testing on a real browser, no direct API calls. Same format as `02_IT/sample.md`.

---

## Feature: Dish Library Management (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Dish Library Management (Manual Web) |
| Test requirement | Create/edit/clone/deactivate master dishes at `/service-caterers/dishes`, including ingredient catalog binding, allergen auto-detection and default substitute dish. |
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
| **Create / Edit Dish** | | | | | | | | | | | | | | |
| MF05-WEB-001 | Create a new dish with ingredients picked from the catalog — allergens auto-detected. | 1. Log in as Service Caterer, go to `/service-caterers/dishes`.<br>2. Click "Add dish".<br>3. Enter Name, pick Category, enter kcal/protein/carb/fat.<br>4. Add ingredient rows, picking each ingredient from the catalog datalist with a quantity.<br>5. Click "Add to library". | Dish is created and appears in the table. Allergen badges shown are auto-derived from the picked ingredients (labelled "(auto)" in the dialog), no manual entry needed. | Logged in as Service Caterer, ingredient catalog has at least one allergen-bearing ingredient (e.g. shrimp). | Pending | | | Pending | | | Pending | | | |
| MF05-WEB-002 | Attempt to save a dish with an ingredient typed free-text, not selected from the catalog. | 1. Open "Add dish".<br>2. Fill required fields.<br>3. In an ingredient row, type a name that does not match any catalog entry (do not pick from the datalist), enter a quantity.<br>4. Click "Add to library". | Blocked — error message lists the off-catalog ingredient name and explains it must be chosen from the catalog. Dish is not saved. | Logged in as Service Caterer. | Pending | | | Pending | | | Pending | | | BR-076 |
| MF05-WEB-003 | Attempt to save a dish missing kcal or missing an ingredient quantity. | 1. Open "Add dish", fill Name + at least one ingredient (from catalog) but leave kcal empty, click "Add to library".<br>2. Fix kcal, then clear the quantity of the ingredient row instead, click "Add to library" again. | Both attempts are blocked with a clear inline error ("Enter kcal per serving" / "each ingredient must have a quantity") — dish is not saved either time. | Logged in as Service Caterer. | Pending | | | Pending | | | Pending | | | BR-062/BR-069 |
| MF05-WEB-004 | Clone an existing dish into a new one. | 1. On the dish table, click "Clone" on an existing dish.<br>2. Confirm the dialog opens pre-filled with the same category/kcal/ingredients and name suffixed "(copy)".<br>3. Change the name, adjust one ingredient, click "Add to library". | A new, separate dish is created with the edited data — the original dish is unchanged. | At least one dish exists in the library. | Pending | | | Pending | | | Pending | | | |
| MF05-WEB-005 | Deactivate a dish currently referenced by a DRAFT or PENDING_APPROVAL menu. | 1. Identify a dish used in at least one DRAFT/PENDING_APPROVAL menu (via `/service-caterers/menus`).<br>2. On `/service-caterers/dishes`, click "Deactivate" on that dish. | Dish is deactivated (badge "Inactive", greyed row) and no longer selectable when composing new menus/templates. A warning toast lists the still-open menu(s) referencing it so they can be fixed before submission. | A dish is used by at least one unsubmitted (DRAFT/REJECTED) or pending menu. | Pending | | | Pending | | | Pending | | | Published menus using the dish are not flagged — only open ones |
| MF05-WEB-006 | Set a default substitute dish that shares an allergen with the original dish. | 1. Edit a dish that has an auto-detected allergen (e.g. contains shrimp).<br>2. In "Default substitute dish", pick another dish that also contains the same allergen (e.g. also contains shrimp/fish sauce).<br>3. Observe the inline message, then save. | A warning is shown stating the chosen substitute still carries the same allergen and is not a safe replacement for that allergen — save is still allowed (advisory only) but the warning is visible before saving. | Two dishes exist that share at least one common allergen. | Pending | | | Pending | | | Pending | | | BR-063 (G10) |

---

## Feature: Menu Template Management (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Menu Template Management (Manual Web) |
| Test requirement | Create/edit/clone/deactivate reusable menu templates at `/service-caterers/menu-templates` (one meal's dish set, tagged by meal type, audience/diet group, optional grade). |
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
| MF05-WEB-007 | Create a menu template for a meal + audience with dishes from the library. | 1. Go to `/service-caterers/menu-templates`, click "Create template".<br>2. Enter a template Name, pick a Meal ("Lunch"), pick "Standard" as audience.<br>3. Search and add 2–3 dishes from the library.<br>4. Click "Create template". | Template is created and appears in the table with the picked meal/audience, dish list and total kcal. Selectable from the "Add menu" picker on the weekly menu calendar for the same meal/audience. | Logged in as Service Caterer, at least 2 active dishes exist in the library. | Pending | | | Pending | | | Pending | | | |
| MF05-WEB-008 | Attempt to create a template with a name that already exists. | 1. Click "Create template".<br>2. Enter the exact same name as an existing template (case-insensitive), fill the rest, click "Create template". | Blocked with an inline error naming the clashing template and suggesting to edit that one instead — no duplicate created. | At least one template already exists. | Pending | | | Pending | | | Pending | | | |
| MF05-WEB-009 | Attempt to save a template with a dish that conflicts with the chosen diet group. | 1. Click "Create template", pick a diet-group audience (e.g. "Vegetarian").<br>2. Add a dish known to contain meat/fish.<br>3. Click "Create template". | Blocked — inline error names the conflicting dish and the reason it violates the chosen diet group; template not saved. | At least one diet group and one non-compliant dish exist. | Pending | | | Pending | | | Pending | | | |
| MF05-WEB-010 | Clone a template into a new one. | 1. On the template table, click "Clone" on an existing template.<br>2. Confirm the dialog is pre-filled with the same meal/audience/dishes and name suffixed "(copy)".<br>3. Adjust the name/dish list, save. | A new, separate template is created — the original is unchanged. | At least one template exists. | Pending | | | Pending | | | Pending | | | |
| MF05-WEB-011 | Deactivate a template and confirm it disappears from the "Add menu" picker. | 1. On `/service-caterers/menu-templates`, click "Deactivate" on a template.<br>2. Go to `/service-caterers/menus`, click "Add menu" on an empty cell matching that template's meal/audience. | Template is marked "Inactive" on the template list (still visible with the "Show inactive" filter). It no longer appears in the template picker dialog on the weekly menu calendar. Any cell that had already used it before deactivation keeps its content unchanged. | Template being tested is not yet used by any DRAFT cell (to isolate the check). | Pending | | | Pending | | | Pending | | | |

---

## Feature: Weekly Menu Composition & Submission (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Weekly Menu Composition & Submission (Manual Web) |
| Test requirement | Build the weekly menu grid at `/service-caterers/menus` (day × meal, one entry per audience) and submit the week for School Admin approval. |
| Number of TCs | 7 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 7 | 0 |
| Round 2 | 0 | 0 | 7 | 0 |
| Round 3 | 0 | 0 | 7 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Add menu to a cell** | | | | | | | | | | | | | | |
| MF05-WEB-012 | Add a standard menu to an empty cell by picking a template. | 1. Go to `/service-caterers/menus`, pick a Grade tab and a week ≥ the lead-time window.<br>2. On an empty cell (e.g. Lunch × a weekday), click "Add menu".<br>3. In the audience picker, choose "Standard".<br>4. In the template picker, choose a template for that meal.<br>5. Confirm. | The cell shows a "Standard" chip with the template's dish list, status DRAFT. Clicking the entry name opens a read-only dish detail dialog. | At least one active "Lunch/Standard" template exists. | Pending | | | Pending | | | Pending | | | |
| MF05-WEB-013 | Attempt to add a menu for a date inside the minimum lead-time window (BR-061). | 1. Navigate to the current week (or a week whose days fall within the configured lead-time from today).<br>2. Observe the cells for days closer than the configured lead-time. | "Add menu"/"Add menu detail" controls are disabled/hidden for those cells, showing the minimum lead-time requirement (e.g. "≥N days") instead of the add button. | School's `menuLeadTimeDays` configuration is a known positive number. | Pending | | | Pending | | | Pending | | | BR-061 |
| MF05-WEB-014 | Add a special-diet menu to a cell that still needs coverage for that diet group. | 1. On a cell that already has a Standard menu not suitable for a diet group (e.g. contains meat), click "Add menu again".<br>2. In the audience picker, confirm the diet group needing coverage is listed with its affected student count.<br>3. Pick that group, then pick a template built for it. | A second chip appears in the same cell labelled with the diet group, separate from the "Standard" chip. The audience picker no longer offers an audience already covered/present in the cell. | At least one student in the grade has an approved diet group not covered by the Standard menu of that meal. | Pending | | | Pending | | | Pending | | | BR-065 |
| MF05-WEB-015 | Attempt to add the same template twice to the same cell/audience. | 1. On a cell that already has a template applied, click "Add menu again" for the same audience.<br>2. In the template picker, click the template already present in the cell. | Rejected with a toast stating the template is already in this meal — no duplicate dish entries added. | Cell already has at least one template applied for the audience being tested. | Pending | | | Pending | | | Pending | | | |
| MF05-WEB-016 | Remove a dish from an editable (DRAFT/REJECTED) menu entry. | 1. Click a menu entry chip in a DRAFT cell to open its detail dialog.<br>2. Remove one dish from the list.<br>3. Close the dialog and confirm the cell content updated. | The dish is removed from the entry; remaining dishes and kcal total update accordingly. Same action is NOT available (no remove control) once the menu is PENDING_APPROVAL or PUBLISHED. | Cell has a DRAFT menu entry with 2+ dishes. | Pending | | | Pending | | | Pending | | | |
| **Submit week for approval** | | | | | | | | | | | | | | |
| MF05-WEB-017 | Submit the week — all DRAFT/REJECTED entries move to Chờ duyệt (PENDING_APPROVAL). | 1. Populate at least one full day (all 3 meals, Standard audience) with no allergy conflicts.<br>2. Click "Submit week" at the top of the page.<br>3. Review the confirmation dialog listing each entry with a "Ready"/"Blocked" badge, confirm submission. | Every "Ready" entry changes status to Chờ duyệt (PENDING_APPROVAL); the dialog reports the submitted count on success. Entries now appear on School Admin's approval calendar for that grade/week. | At least one DRAFT or REJECTED menu with no unresolved allergy conflicts exists for the week being viewed. | Pending | | | Pending | | | Pending | | | UC-087 |
| MF05-WEB-018 | Submit the week when one day still has an unresolved allergy conflict (BR-063). | 1. Prepare a week where one day's menu contains an allergen-touching dish without a resolved substitute, and another day is clean.<br>2. Click "Submit week", open the confirmation dialog. | The conflicting day is shown with a "Blocked" badge and the conflict count; it is excluded from the submit count and NOT submitted. The clean day submits normally when confirmed. | At least one day has an unresolved allergy conflict, at least one other day does not. | Pending | | | Pending | | | Pending | | | BR-063 |

---

## Feature: Menu Approval (School Admin Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Menu Approval (Manual Web) |
| Test requirement | Approve/reject submitted menus per grade/day at `/school-admin/menu-approval` and its detail page, including electronic signature and allergy-conflict approval gate. |
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
| **Approve / Reject decisions** | | | | | | | | | | | | | | |
| MF05-WEB-019 | View the weekly approval calendar for a grade and confirm the pending day shows decision controls. | 1. Log in as School Admin, go to `/school-admin/menu-approval`.<br>2. Pick the Grade tab and week where a menu was submitted (MF05-WEB-017).<br>3. Observe the "Duyệt" row of the pending day. | The "Duyệt" row for that day shows "Approve" and "Reject" buttons; the 3 meal rows below list the submitted entries by name (clicking a name opens the dish detail dialog). | A menu is in PENDING_APPROVAL for a grade/week visible to this School Admin. | Pending | | | Pending | | | Pending | | | |
| MF05-WEB-020 | Approve a pending day — all its meals and any attached special-diet menus are published together. | 1. On the pending day, click "Approve".<br>2. Confirm a success toast appears. | All pending meal entries of that day (Standard + any attached diet-group entries) change to PUBLISHED with an electronic signature recorded; the toast reports how many meals (and special menus, if any) were approved. | Day is pending with no BR-063 blockers. | Pending | | | Pending | | | Pending | | | BR-066/BR-068 |
| MF05-WEB-021 | Attempt to reject a day without entering a reason. | 1. On a pending day, click "Reject" (X icon) to open the reject dialog.<br>2. Leave the reason field empty, click "Confirm reject". | "Confirm reject" stays disabled (or the action is blocked) until a non-empty reason is entered — nothing is rejected. | Day is pending. | Pending | | | Pending | | | Pending | | | BR-048 |
| MF05-WEB-022 | Reject a pending day with a reason — reason is visible back to the caterer. | 1. On a pending day, click "Reject", enter a reason, confirm.<br>2. Log out, log in as Service Caterer, open `/service-caterers/menus` for the same grade/week. | Day status becomes REJECTED; the rejection reason is shown directly under the day's status both on the School Admin calendar and the Caterer's weekly grid. The Caterer can now edit and resubmit that day. | Day is pending. | Pending | | | Pending | | | Pending | | | BR-048 |
| MF05-WEB-023 | Attempt to approve a day that still contains an unresolved allergy-conflict dish. | 1. Prepare a pending day whose menu has a dish touching a verified student allergy with no substitute defined.<br>2. Observe the "Duyệt" cell for that day. | "Approve" is disabled; a critical badge names the conflicting dish(es) and states they still need a dedicated portion. Reject remains available. | At least one verified allergy in the grade touches a dish with no resolved substitute in the submitted menu. | Pending | | | Pending | | | Pending | | | BR-063 (MSG-051) |
| **Menu detail page** | | | | | | | | | | | | | | |
| MF05-WEB-024 | Open the full record detail page and verify nutrition + ingredient/allergen trace before approving. | 1. From the calendar, click "View full record" on a pending day's dialog (or navigate to `/school-admin/menu-approval/{id}`).<br>2. Review the dish/ingredient list, per-meal kcal, and the approval history panel.<br>3. Approve from this page. | Each meal card lists its dishes with ingredients and any auto-derived allergens; kcal per meal and per day is shown against the grade's nutrition target. After approving, the "Approval history" table shows a new row with actor and signature code. | A PENDING_APPROVAL menu with dish/ingredient detail exists. | Pending | | | Pending | | | Pending | | | BR-062/BR-066/BR-069 |

---

## Feature: Special Meal Compliance Monitoring (School Admin Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Special Meal Compliance Monitoring (Manual Web) |
| Test requirement | Read-only oversight of special-diet meal plan coverage at `/school-admin/special-meals`, plus approval of late-added special-diet menus. |
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
| MF05-WEB-025 | View the list of students eligible for a special meal plan but not yet covered. | 1. Log in as School Admin, go to `/school-admin/special-meals`.<br>2. Review the "Students without a special meal plan" table. | Every student with a verified allergy or declared diet and no matching published menu substitute is listed, with the required reason (allergen/diet) shown per row; a summary stat card shows the "covered / eligible" ratio. | At least one eligible student has no matching plan. | Pending | | | Pending | | | Pending | | | BR-065 |
| MF05-WEB-026 | Approve a pending late-added special-diet menu (added after the standard menu was already published). | 1. On `/school-admin/special-meals`, find the "pending late addition" card for a diet group.<br>2. Click "Approve". | The card's related menu status changes to APPROVED with the approver name/time recorded; a success toast confirms the kitchen may now prepare it. | At least one special-diet menu was added to an already-published day and is PENDING_APPROVAL. | Pending | | | Pending | | | Pending | | | |
| MF05-WEB-027 | Reject a pending late-added special-diet menu. | 1. On the same pending card, click "Reject" instead. | Menu status changes to REJECTED; an info toast confirms the rejection. | Same as MF05-WEB-026. | Pending | | | Pending | | | Pending | | | |
| MF05-WEB-028 | View the allergen-overlap check result on an active special meal plan. | 1. Scroll to "Active special meal plans" table.<br>2. Locate a row and read its "Allergen overlap check" column. | Each plan shows PASSED (substitute dish does not touch the student's allergen) or FAILED (substitute still touches it, with the overlapping allergen(s) named) — FAILED rows are visually flagged (danger badge) and also surfaced in the "still touches allergy" alert above the table. | At least one plan exists (ideally one PASSED and one FAILED, if available). | Pending | | | Pending | | | Pending | | | BR-063 |

---

## Feature: Parent-Facing Weekly Menu & School Meals (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Parent-Facing Weekly Menu & School Meals (Manual Web) |
| Test requirement | View the published, personalized weekly menu and special meal arrangements for the active child at `/parent/weekly-menu`, `/parent/school-meals`, `/parent/special-meals`. |
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
| MF05-WEB-029 | View today's menu highlighted, personalized for a child with a verified allergy. | 1. Log in as Parent with an active child who has a verified allergy that is present in today's published menu.<br>2. Go to `/parent/weekly-menu`. | An info banner states the active child's verified allergen(s)/diet. The "Today" card shows the substitute dish (green "Suất riêng" badge, "Thay cho ..." note) in place of the original dish for the affected meal — not the original allergen-bearing dish. | Child has a verified allergy that a published menu dish touches, and a substitute plan exists for it. | Pending | | | Pending | | | Pending | | | BR-065/BR-082 |
| MF05-WEB-030 | View the full week grid of published menus, scoped to the child's own grade. | 1. On `/parent/weekly-menu`, scroll to "Thực đơn cả tuần".<br>2. Confirm every card's grade badge matches the active child's grade. | Only published menus for the active child's grade are listed — no cards for other grades appear, and the current day's card is visually marked ("Hôm nay"). | Active child is linked to a grade with at least one published menu for the week. | Pending | | | Pending | | | Pending | | | |
| MF05-WEB-031 | Switch the active child and confirm the menu view updates to the new child's grade/diet. | 1. On any parent menu page, switch the active child selector to a second child in a different grade or with a different diet.<br>2. Observe the page content. | Title/description update to the new child's grade and name; the dish list re-personalizes according to the new child's own allergy/diet profile (no leftover data from the previous child). | Parent account has 2+ linked children in different grades or with different diet profiles. | Pending | | | Pending | | | Pending | | | |
| MF05-WEB-032 | View the "School Meals" dashboard split into Published vs Pending Approval. | 1. Go to `/parent/school-meals`. | Two columns are shown: "Đã công bố" (published menus, with a highlight of the lunch dishes and kcal) and "Chờ Ban giám hiệu duyệt" (pending menus, content still shown but flagged as subject to change). Counts in each column header match the underlying data. | Child's grade has both published and pending menus in the same period. | Pending | | | Pending | | | Pending | | | BR-070 |
| MF05-WEB-033 | View the allergy-conflict alert on the "School Meals" dashboard when a published menu still has an unresolved conflict. | 1. Prepare (or locate) a published menu for the child's grade containing a dish touching the child's verified allergen with no substitute resolved.<br>2. Go to `/parent/school-meals`. | A red "Allergy highlight" banner names the number of affected days and the allergen(s); the affected day's row in "Đã công bố" carries a red allergen badge. A link to "Xem suất thay thế đã lên kế hoạch" (`/parent/special-meals`) is present. | Published menu has an unresolved conflict for the active child. | Pending | | | Pending | | | Pending | | | BR-069 |
| MF05-WEB-034 | View "Special Meal Arrangements" detail and the empty state for a child without any allergy/diet declared. | 1. Go to `/parent/special-meals` with a child that HAS a plan — verify the table shows Date/meal, Reason badge, "Original dish → Substitute dish", and prep note.<br>2. Switch to a child with no allergy/diet declared, reload the page. | Step 1: table lists each plan row correctly, with a critical badge on any row where the allergen-overlap check FAILED. Step 2: an info message states there is no special meal plan for this child and links to "Khai báo dị ứng" (`/parent/allergies`). | One child has an active special meal plan; a second child has no allergy/diet declared. | Pending | | | Pending | | | Pending | | | UC-141 |
