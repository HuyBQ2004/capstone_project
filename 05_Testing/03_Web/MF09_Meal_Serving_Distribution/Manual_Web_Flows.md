# Manual Web E2E Test — MF09 Meal Serving & Distribution

> Manual testing on a real browser, no direct API calls. Same format as `02_IT/sample.md`.

---

## Feature: Kitchen Meal Preparation (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Kitchen Meal Preparation (Manual Web) |
| Test requirement | Service Caterers views the daily cook sheet (`/service-caterers/cook-sheet`) and the meal-service dashboard (`/service-caterers/meal-service`) before cooking/serving. |
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
| **`/service-caterers/cook-sheet`** | | | | | | | | | | | | | | |
| MF09-WEB-001 | View today's cook sheet with grades merged into one dish list. | 1. Log in as Service Caterers, go to `/service-caterers/cook-sheet`.<br>2. Leave "Ngày phục vụ" = today, "Buổi ăn" = "Bữa trưa", "Khối" = "Tất cả khối". | The "Món chính — nồi lớn" card lists each dish with a total merged portion count (e.g. "Cơm trắng × 1030") and the list of source grades. Badges above the table show total grades, "suất chính" and "suất riêng" counts. | At least one published menu exists for today's lunch across 2+ grades. | Pending | | | Pending | | | Pending | | | |
| MF09-WEB-002 | Filter the cook sheet by a single grade. | 1. On `/service-caterers/cook-sheet`, pick a specific grade in the "Khối" dropdown. | The dish list recalculates to only that grade's portions — merged multi-grade totals shrink to the single grade's count. | Same as MF09-WEB-001. | Pending | | | Pending | | | Pending | | | |
| MF09-WEB-003 | Special-diet/allergy-substitute portions show separately from the main pot. | 1. On `/service-caterers/cook-sheet`, pick a date/meal that has students on an allergy substitution or a special-diet template. | A separate "Suất riêng — nấu nồi riêng, dụng cụ riêng" card lists each substitute/diet dish with its own portion count and source, apart from the main dish table. | At least one allergy substitution or diet-group template is active for the chosen date/meal. | Pending | | | Pending | | | Pending | | | BR-082 — cross-contamination prevention |
| MF09-WEB-004 | Cook sheet shows a helpful empty state when no menu is published for the selected day/meal. | 1. On `/service-caterers/cook-sheet`, pick a date with no published menu for the selected meal. | An info alert is shown stating no menu is served for that date/meal, listing the actual dates that do have a published menu (or stating none exist). | A date exists with no published/live menu. | Pending | | | Pending | | | Pending | | | |
| **`/service-caterers/meal-service`** | | | | | | | | | | | | | | |
| MF09-WEB-005 | View the meal-service dashboard: planned portions, menus served, and the BR-094 serving-gate status. | 1. Go to `/service-caterers/meal-service`.<br>2. Pick date/meal, then pick a grade tab. | Three stat cards show "Suất kế hoạch" (total planned portions), "Thực đơn phục vụ" (standard/diet menu counts) and "Trạng thái phục vụ" ("Sẵn sàng" in green when the 3-step inspection passed, or "Chờ kiểm thực" in amber/red otherwise). The class table for the selected grade lists each class's homeroom teacher, headcount, allergy/special-diet count and planned portions per dish. | Classrooms and menus exist for the grade/date/meal chosen. | Pending | | | Pending | | | Pending | | | BR-094/BR-098 |
| MF09-WEB-006 | Open a class detail popup to see its allergy/special-diet students only. | 1. On `/service-caterers/meal-service`, click "Chi tiết" on a class row.<br>2. In the popup, use the diet filter dropdown. | The popup lists only students with an allergy or a special diet (no regular students) — each row shows class + student code (no student name, PDPD data-minimization), allergy badges, and diet badge. The diet filter narrows the list to the selected diet. | The chosen class has at least one student with a declared allergy or special diet. | Pending | | | Pending | | | Pending | | | Service Caterers view-only, no student name shown (PDPD) |

---

## Feature: Class Delivery Confirmation (Web) — Service Caterers

### 1. General Information

| Field | Value |
|---|---|
| Feature | Class Delivery Confirmation (Manual Web) |
| Test requirement | Service Caterers confirms the kitchen has delivered the day's portions to each class at `/service-caterers/class-deliveries`, gated by the BR-094 inspection result. |
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
| MF09-WEB-007 | View classes that have planned portions for the selected date/meal, with dish and substitution breakdown. | 1. Log in as Service Caterers, go to `/service-caterers/class-deliveries`.<br>2. Pick today's date and "Bữa trưa". | The class table lists every class with planned portions this meal, each row showing dish name × count, with "thay thế ×N" (substitute) and "chặn ×N" (blocked) badges where applicable. Stat cards show total portions, "Đã giao"/"Đã nhận" class counts. | At least one class has a published menu for today's lunch. | Pending | | | Pending | | | Pending | | | |
| MF09-WEB-008 | Confirm delivery ("Xác nhận đã giao") to a class after the inspection gate passed. | 1. Ensure the 3-step inspection for the date/meal is fully Passed (via MF07 flow).<br>2. On `/service-caterers/class-deliveries`, click "Xác nhận đã giao" for a class not yet delivered. | A success toast confirms delivery to the class. The row's status badge changes from "Chưa giao" to "Đã giao — chờ lớp nhận". The "Xác nhận đã giao" button disappears for that row. | Class has planned portions and no existing delivery record; inspection gate passed. | Pending | | | Pending | | | Pending | | | |
| MF09-WEB-009 | "Xác nhận đã giao" is blocked while the 3-step inspection has not passed. | 1. Pick a date/meal whose inspection is missing or has a failed step.<br>2. Observe the page banner and the delivery button. | A warning alert explains the inspection is not complete/passed and delivery is blocked. The "Xác nhận đã giao" button is disabled with a tooltip explaining why. | Inspection session for the date/meal is missing or has a FAILED step. | Pending | | | Pending | | | Pending | | | BR-094, stricter than gate: blocks on missing session too |

---

## Feature: Class Delivery Receipt (Web) — Teacher

### 1. General Information

| Field | Value |
|---|---|
| Feature | Class Delivery Receipt (Manual Web) |
| Test requirement | Teacher confirms receipt of the portions delivered by the kitchen at `/teacher/class-deliveries`, before splitting portions to individual students. |
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
| MF09-WEB-010 | Class delivery not yet made shows a waiting message. | 1. Log in as a homeroom Teacher, go to `/teacher/class-deliveries`.<br>2. Pick a date/meal the kitchen has not delivered yet. | A warning alert states the kitchen has not delivered for this class/meal yet, and there is nothing to confirm. | Kitchen has not yet clicked "Xác nhận đã giao" for this class/meal (see MF09-WEB-008). | Pending | | | Pending | | | Pending | | | |
| MF09-WEB-011 | Teacher confirms receipt ("Xác nhận đã nhận") of the delivered portions. | 1. Pick a date/meal the kitchen already delivered for this class.<br>2. Review the dish/portion table shown.<br>3. Click "Xác nhận đã nhận". | A success toast confirms receipt. The status badge changes to "Đã nhận lúc <time>" and the "Xác nhận đã nhận" button is replaced by "Sang chia suất cho học sinh". | Kitchen has delivered this class/meal (status not yet RECEIVED). | Pending | | | Pending | | | Pending | | | |
| MF09-WEB-012 | Navigate from delivery receipt to student distribution. | 1. After confirming receipt (MF09-WEB-011), click "Sang chia suất cho học sinh". | Browser navigates to `/teacher/distribution`, pre-scoped to the meal just received. | Delivery already confirmed RECEIVED for this class/meal. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Student Meal Distribution (Web) — Teacher

### 1. General Information

| Field | Value |
|---|---|
| Feature | Student Meal Distribution (Manual Web) |
| Test requirement | Teacher confirms individual student meal distribution at `/teacher/distribution` (list, batch confirm) and `/teacher/distribution/[id]` (detail, single confirm, error report), with the BR-094 serving gate and BR-065 allergy substitution checks. |
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
| **`/teacher/distribution` (list)** | | | | | | | | | | | | | | |
| MF09-WEB-013 | View the distribution list with status filter tabs. | 1. On `/teacher/distribution`, check the class/meal already received (per MF09-WEB-011) is shown in the page description.<br>2. Switch between the "Tất cả / Chờ phát / Đã phục vụ / Suất đặc biệt" tabs. | Each tab filters the student table accordingly. Every row shows assigned meal per BR-125, allergy warning column (BR-123) when applicable, and a status badge. | Class has a RECEIVED delivery for today's meal. | Pending | | | Pending | | | Pending | | | |
| MF09-WEB-014 | Batch-confirm distribution for several students ("Bắt đầu phân phát" → "Lưu phân phát"). | 1. Click "Bắt đầu phân phát".<br>2. Tick "Chọn phát suất" for 2-3 PENDING students without allergy blocks.<br>3. Click "Lưu phân phát (N)", confirm in the dialog. | A success toast confirms the batch. All selected students' status changes to SERVED with a timestamp. Select mode exits automatically. | At least 2 students are PENDING and not blocked. | Pending | | | Pending | | | Pending | | | BR-120, BR-130, MSG-021/MSG-045 |
| MF09-WEB-015 | A student with an unresolved allergy substitution cannot be selected for batch distribution. | 1. Enter select mode on `/teacher/distribution`.<br>2. Locate a student whose assigned meal shows the "chưa có món thay thế" badge.<br>3. Click their select control. | A red "Cần xử lý tay" badge is shown instead of a selectable button; clicking shows an error toast explaining the allergy conflict must be resolved by Medical/Kitchen first — the student is not added to the selection. | At least one student has a blocked (no-substitute) dish for the meal. | Pending | | | Pending | | | Pending | | | BR-065 |
| MF09-WEB-016 | Revoke ("Hủy phát") a distribution already marked SERVED, same day only. | 1. Outside select mode, find a SERVED student row.<br>2. Click "Hủy phát". | A success toast confirms the revoke. The row's status reverts to PENDING (or equivalent), removing the served timestamp. | A student is SERVED today, revoke window (same day) still open. | Pending | | | Pending | | | Pending | | | ADR-M09-3 — same-day-only revoke |
| **`/teacher/distribution/[id]` (detail)** | | | | | | | | | | | | | | |
| MF09-WEB-017 | Confirm a single student's distribution from the detail page, with allergy banner shown. | 1. From the list, click "Chi tiết" on a PENDING student with an allergy warning.<br>2. Review the red allergy banner and the assigned-meal card.<br>3. Click "Xác nhận đã phục vụ", confirm in the dialog. | The critical allergy banner is shown before confirming. After confirming, a success toast (MSG-045) appears, the status badge becomes SERVED with a timestamp, and the button becomes disabled/labelled "Đã phục vụ". | Student has an active allergy warning and a PENDING distribution today. | Pending | | | Pending | | | Pending | | | BR-123 |
| MF09-WEB-018 | Distribution confirm button is blocked when the BR-094 serving gate is not open. | 1. Open the detail page for a student whose date/meal has no passed inspection session (or a session still IN_PROGRESS/FAILED). | A warning/critical alert names the reason (no session / in progress / failed step). The "Xác nhận đã phục vụ" button is disabled and labelled with the block reason. | Inspection session for the date/meal is missing, in progress, or failed. | Pending | | | Pending | | | Pending | | | BR-094/BR-098 |
| MF09-WEB-019 | Duplicate confirmation on an already-SERVED student is rejected client-side. | 1. Open the detail page for a student already SERVED today.<br>2. Attempt to trigger "Xác nhận đã phục vụ" again (e.g. via the disabled control or a stale tab). | An error toast states the student was already scanned for this meal session (MSG-021) — no duplicate confirmation dialog opens. | Student's distribution status is already SERVED. | Pending | | | Pending | | | Pending | | | BR-130 |
| MF09-WEB-020 | Report a wrong distribution ("Báo lỗi phân phát sai suất"). | 1. On the detail page, click "Báo lỗi phân phát sai suất".<br>2. Enter a reason, click "Gửi báo lỗi". | A success toast confirms the kitchen has been notified to re-issue the correct portion. The dialog closes and the reason field clears. | Reason field is required — cannot submit empty. | Pending | | | Pending | | | Pending | | | UC-016 AF1, BR-065 |

---

## Feature: Meal Consumption Recording (Web) — Teacher

### 1. General Information

| Field | Value |
|---|---|
| Feature | Meal Consumption Recording (Manual Web) |
| Test requirement | Teacher logs each student's meal consumption (Fully/Partially/Not Consumed) at `/teacher/meal-records`, and reconciles served vs. planned counts across the daily flow. |
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
| MF09-WEB-021 | Batch-record consumption for several students ("Bắt đầu ghi nhận" → "Lưu ghi nhận"). | 1. Go to `/teacher/meal-records`, pick today's date.<br>2. Click "Bắt đầu ghi nhận".<br>3. For 3 students, click "Ăn hết"/"Một phần"/"Không ăn" respectively.<br>4. Click "Lưu ghi nhận (3)", confirm. | A success toast confirms the batch save. Each row's consumption badge updates to the chosen value with a recorded timestamp. Select mode exits automatically. | At least 3 students have a meal record for today, allergy/diet students included among them. | Pending | | | Pending | | | Pending | | | BR-121 |
| MF09-WEB-022 | Change a selection before saving (toggle back to the originally-saved value clears the pending change). | 1. In select mode, click a different consumption value for a student, then click the originally-saved value again.<br>2. Check the "Lưu ghi nhận (N)" counter. | The counter for that student's change is removed (does not count toward the pending-save total) since it matches the already-saved value — no needless PATCH sent on save. | At least one student already has a saved consumption value. | Pending | | | Pending | | | Pending | | | Avoids no-op PATCH calls |
| MF09-WEB-023 | Reconcile student counts across delivery → distribution → consumption for one class/meal. | 1. Note the class headcount on `/service-caterers/meal-service` (or class roster).<br>2. Note "Số suất phân phát" for the class on `/service-caterers/class-deliveries`.<br>3. Note the SERVED count on `/teacher/distribution` for the same class/meal.<br>4. Note the recorded-consumption count on `/teacher/meal-records` for the same date. | All 4 counts match the class headcount (standard + special-diet + substitute portions sum to the same total at every stage) — no student is missing or double-counted across the pipeline. | Full pipeline completed for one class/meal: delivered, received, all students distributed and recorded. | Pending | | | Pending | | | Pending | | | Cross-checks BR-125 portion assignment end-to-end |
