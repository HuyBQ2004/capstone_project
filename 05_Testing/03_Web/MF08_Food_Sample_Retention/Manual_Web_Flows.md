# Manual Web E2E Test — MF08 Food Sample Retention

> Manual testing on a real browser, no direct API calls. Same format as `02_IT/sample.md`.

---

## Feature: Sample Registration (Service Caterers)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Sample Registration (Manual Web) |
| Test requirement | Register a food sample per passed inspection session at `/service-caterers/samples/register/[sessionId]`, one row per dish, each with its own evidence photos, sealed/sanitized checks, container, amount, and fridge temperature. |
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
| MF08-WEB-001 | Register a food sample for one dish of a passed inspection session — happy path. | 1. Log in as Service Caterers, go to `/service-caterers/samples`.<br>2. Under "Mẫu cần lấy theo phiên kiểm thực", pick a passed session.<br>3. Click "Đăng ký N mẫu còn thiếu", opening `/service-caterers/samples/register/[sessionId]`.<br>4. Fill collect time, storage location, fridge temp (in 2–8°C range).<br>5. For the dish row: set amount ≥100g (solid), pick a container, tick "sanitized" and "sealed", open camera and capture at least 1 evidence photo.<br>6. Click "Đăng ký mẫu (MSG-044)". | Success toast "Đã đăng ký lưu mẫu, bắt đầu tính thời gian bảo quản. (MSG-044)". Redirected to the new sample's detail page `/service-caterers/samples/[id]`, status shows "Đang lưu" (RETAINED) with a retention countdown. | Logged in as Service Caterers with at least one inspection session whose 3 steps are all Passed and has at least one dish not yet sampled. | Pending | | | Pending | | | Pending | | | |
| MF08-WEB-002 | Attempt to open the registration page for a session not yet fully Passed. | 1. Find an inspection session whose 3-step result is not "PASSED" (e.g. still pending step 3).<br>2. Navigate directly to `/service-caterers/samples/register/[sessionId]` for that session (via URL or a deep link). | Page shows a warning "Phiên chưa Đạt cả 3 bước (BR-105)" with a link back to the inspection session — no sample registration form is shown. | An inspection session exists whose overall inspection result is not PASSED. | Pending | | | Pending | | | Pending | | | BR-105 |
| MF08-WEB-003 | Attempt to submit a sample row with no evidence photo attached. | 1. On the registration page for a passed session, fill amount, container, tick sanitized+sealed for a dish row, but do NOT capture any photo.<br>2. Fill the shared collect time / storage location / fridge temp.<br>3. Click "Đăng ký mẫu". | Blocked — error message names the dish and states it has no dedicated evidence photo (BR-107), submission does not proceed. | Same session precondition as MF08-WEB-001. | Pending | | | Pending | | | Pending | | | Each dish must have its own photo, not shared with another dish |
| MF08-WEB-004 | Attempt to submit a sample row with "sanitized" or "sealed" left unchecked. | 1. On the registration page, fill amount/container/photo for a dish row but leave the "sealed" checkbox unticked.<br>2. Click "Đăng ký mẫu". | Blocked — error message states the container/seal confirmation is required for that dish, submission does not proceed. | Same session precondition as MF08-WEB-001. | Pending | | | Pending | | | Pending | | | Article 6/7.1 requirement |
| MF08-WEB-005 | Attempt to submit a sample below the minimum required weight/volume. | 1. On the registration page, choose "Thức ăn đặc" (solid) for a dish row and set amount below 100 g (e.g. 50).<br>2. Fill the rest of the row correctly, click "Đăng ký mẫu". | Blocked — error message states the amount does not meet the minimum (100 g solid / 150 ml liquid), submission does not proceed. | Same session precondition as MF08-WEB-001. | Pending | | | Pending | | | Pending | | | BR-203, MSG-019 |
| MF08-WEB-006 | Register a sample with fridge temperature outside the 2–8°C range — soft warning only. | 1. On the registration page, set the fridge temperature field to a value outside 2–8°C (e.g. 10).<br>2. Complete a valid row (amount/container/checks/photo), click "Đăng ký mẫu". | A warning text appears near the temperature field ("Ngoài dải 2–8°C… đánh dấu cảnh báo, MSG-020") but registration still succeeds — this is a soft warning, not a hard block. | Same session precondition as MF08-WEB-001. | Pending | | | Pending | | | Pending | | | BR-110 is a soft warning at backend, not a blocker |
| MF08-WEB-007 | Register samples for multiple dishes of the same session in one submission. | 1. Pick a passed session with 2+ dishes not yet sampled.<br>2. Fill each dish row individually (own amount/container/checks/photo).<br>3. Click "Đăng ký N mẫu (MSG-044)". | All dish rows are created as separate samples; success toast reports the count. Redirected to `/service-caterers/samples/print-labels?ids=...` to print the combined labels for all newly created samples. | A passed session with at least 2 unsampled dishes exists. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Label Pre-Reservation (Service Caterers)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Label Pre-Reservation (Manual Web) |
| Test requirement | Reserve sample labels ahead of time for a date range at `/service-caterers/samples/pre-register`, then print the reserved/registered labels in bulk. |
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
| MF08-WEB-008 | Pre-register labels for a date range read from the already-planned menu. | 1. Go to `/service-caterers/samples/pre-register`.<br>2. Set "Từ ngày"/"Đến ngày" to a range with a planned menu (≤14 days).<br>3. Click "Tải danh sách món theo thực đơn" — list of dishes per date/meal loads, all pre-checked.<br>4. Uncheck a few dishes if desired, click "Đặt trước N tem". | Toast confirms N labels reserved. Redirected to `/service-caterers/samples/print-labels?source=reservation&ids=...` showing the reserved labels ready to print — no `FoodSample` record is created yet at this step. | A menu is already planned for the chosen date range. | Pending | | | Pending | | | Pending | | | Reservation only creates a label code, not a real sample |
| MF08-WEB-009 | Print combined labels for samples already registered from a session. | 1. From `/service-caterers/samples`, pick a session with at least 2 registered samples.<br>2. Click "In gộp N nhãn đã lấy". | Navigates to the print-labels page listing all samples of that session; each label shows Form 4 fields (meal, dish name, collect time, collector). | At least one session has 2+ registered samples. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Sample Detail & Retention Tracking (Service Caterers)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Sample Detail & Retention Tracking (Manual Web) |
| Test requirement | View a registered sample's full Form 4/Form 5 record at `/service-caterers/samples/[id]`, including QR code, evidence photos, and the live retention countdown; filter the sample list. |
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
| MF08-WEB-010 | View a retained sample's detail page — labels, log fields, QR code, evidence photos. | 1. From `/service-caterers/samples`, click "Xem" on a RETAINED sample. | Detail page shows: status badge "Đang lưu" with hours-remaining countdown (BR-111, min 24h), fridge temp badge, sealed/sanitized badges, Form 4 label block (meal, dish, collect time, collector), Form 5 log block (12 columns incl. storage location), and the evidence photo gallery with thumbnails. QR code renders with the sample ID. | At least one RETAINED sample exists. | Pending | | | Pending | | | Pending | | | |
| MF08-WEB-011 | Filter the sample list by date range and meal type. | 1. On `/service-caterers/samples`, set "Từ ngày"/"Đến ngày" and pick a meal in the "Bữa" filter.<br>2. Observe the table. | Only samples collected within the date range and matching the selected meal type are shown; clearing the filters restores the full list. | At least 2 samples exist across different dates/meals. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Sample Disposal (Service Caterers)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Sample Disposal (Manual Web) |
| Test requirement | Dispose a retained sample only after the minimum 24-hour retention period, recording dispose time, witness, and quality note (BR-111/BR-112). |
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
| MF08-WEB-012 | Attempt to dispose a sample before the minimum 24-hour retention has elapsed. | 1. Open the detail page of a RETAINED sample whose "hours remaining" is still > 0.<br>2. Look at the "Hủy mẫu" card. | The "Thực hiện hủy mẫu" button is disabled, with a warning text showing the hours still remaining before disposal is allowed. | A RETAINED sample exists with hoursRemaining > 0. | Pending | | | Pending | | | Pending | | | BR-111 |
| MF08-WEB-013 | Dispose a sample after the retention period has elapsed — happy path. | 1. Open the detail page of a RETAINED sample whose "hours remaining" is 0 (or already past the minimum).<br>2. Click "Thực hiện hủy mẫu", opening the dispose dialog.<br>3. Set dispose time via the wheel picker, enter a witness name, enter a quality note.<br>4. Click "Xác nhận hủy mẫu". | Toast confirms the sample is disposed and Form 5 columns 9/10/12 are recorded. Dialog closes, status badge on the detail page updates to "Đã hủy" (DISPOSED), showing dispose time/witness/note. | A RETAINED sample exists with hoursRemaining ≤ 0, not linked to an open incident. | Pending | | | Pending | | | Pending | | | disposedBy is derived server-side from the logged-in user, not sent from the form |
| MF08-WEB-014 | Confirm no dispose action is available for a sample already sent to lab. | 1. Open the detail page of a sample whose status is "Đã gửi kiểm nghiệm" (SENT_TO_LAB). | An alert states the sample has already been handed over for testing and no dispose action is available; the "Hủy mẫu" card only shows explanatory text, no active button. | A sample exists in SENT_TO_LAB status. | Pending | | | Pending | | | Pending | | | BR-113 — SENT_TO_LAB is a terminal state for this screen |

---

## Feature: Sample Hand-Over to Lab / Authority (Medical Staff)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Sample Hand-Over to Lab/Authority (Manual Web) |
| Test requirement | Medical Staff retrieves retained samples and hands them over to a lab or authority during incident investigation, at `/medical-staff/samples?action=request-retrieval&incident=...`, with mandatory hand-over evidence. |
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
| MF08-WEB-015 | Hand over a batch of samples for an open incident investigation — happy path. | 1. Log in as Medical Staff, open an active incident and follow its "retrieve samples" deep link into `/medical-staff/samples?action=request-retrieval&incident=INC-xxx`.<br>2. Confirm the pre-checked list of RETAINED samples for the suspected dish(es).<br>3. Pick a hand-over type (LAB or AUTHORITY), fill the required receiving party/criteria field, upload at least one hand-over record file.<br>4. Click "Xác nhận đã bàn giao (N mẫu)". | Toast confirms N samples handed over with a request ID linked to the incident. All chosen samples' status changes to "Đã gửi kiểm nghiệm" (SENT_TO_LAB); "Hoàn tất và quay lại sự cố" button becomes active. | An active (not CLOSED) incident exists with RETAINED samples for its suspected dish(es) within the investigation window. | Pending | | | Pending | | | Pending | | | BR-113/114 — status change is final, cannot be undone |
| MF08-WEB-016 | Attempt hand-over without uploading a hand-over record file. | 1. On the same batch hand-over form, select samples and hand-over type, but do not attach any file under "Biên bản giao nhận mẫu *".<br>2. Try to click "Xác nhận đã bàn giao". | Submit button remains disabled — hand-over record upload is mandatory, form cannot be submitted without it. | Same incident precondition as MF08-WEB-015. | Pending | | | Pending | | | Pending | | | BR-212 — server also rejects with 422 if missing |
| MF08-WEB-017 | Attempt hand-over for an incident that has since been closed. | 1. Open the retrieval task deep link for an incident, then have the incident closed (e.g. by another admin) before submitting.<br>2. Fill the hand-over form fully and click "Xác nhận đã bàn giao". | Blocked with an error toast stating the incident is invalid or already closed (MSG-023) — no samples change status. | An incident starts active, then is closed before submission. | Pending | | | Pending | | | Pending | | | BR-114 |
| MF08-WEB-018 | Expand the retrievable list beyond the suspected dish using "+ Bổ sung mẫu". | 1. On the retrieval task screen, note the default list only shows samples of the suspected dish(es).<br>2. Click "+ Bổ sung mẫu". | The list expands to show all RETAINED samples within the same investigation window (not limited to suspected dishes); clicking the toggle again ("Chỉ món nghi vấn") restores the filtered list. | Same incident precondition as MF08-WEB-015, with additional RETAINED samples for other dishes in the same window. | Pending | | | Pending | | | Pending | | | Server still limits by investigation time window even when expanded |

---

## Feature: Disposed Sample Incident Linking (Medical Staff)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Disposed Sample Incident Linking (Manual Web) |
| Test requirement | Link a disposed sample's metadata to an active incident when physical retrieval is no longer possible (UC-076 AF1). |
| Number of TCs | 1 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 1 | 0 |
| Round 2 | 0 | 0 | 1 | 0 |
| Round 3 | 0 | 0 | 1 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| MF08-WEB-019 | Link a disposed sample to an active incident (metadata only, no physical retrieval). | 1. On `/medical-staff/samples`, find a sample with status "Đã hủy" (DISPOSED), click "Liên kết sự cố (AF1)".<br>2. In the dialog, pick an active (not CLOSED) incident from the dropdown.<br>3. Click "Liên kết mẫu đã huỷ với sự cố". | A warning banner in the dialog states this only links metadata, no physical retrieval happens. Toast confirms the link; the sample's status stays DISPOSED (unchanged) but is now associated with the incident. | A DISPOSED sample and an active incident both exist. | Pending | | | Pending | | | Pending | | | Closed incidents are shown disabled in the dropdown |

---

## Feature: Retention Log Oversight & Export (Medical Staff / School Admin)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Retention Log Oversight & Export (Manual Web) |
| Test requirement | Medical Staff and School Admin can view the full retention log (Form 5) with filters and export it to xlsx; School Admin's view is read-only, with no write actions available. |
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
| MF08-WEB-020 | Medical Staff exports the retention log with active filters. | 1. Log in as Medical Staff, go to `/medical-staff/samples`.<br>2. Set date range, meal, dish, and status filters.<br>3. Click "Export". | An xlsx file downloads (named `mau-so-5_<from>_<to>.xlsx`) containing only the rows matching the active filters; success toast confirms the export. | At least one sample exists matching the chosen filters. | Pending | | | Pending | | | Pending | | | BR-178 |
| MF08-WEB-021 | School Admin views the retention log as read-only, with no write actions. | 1. Log in as School Admin, go to `/school-admin/samples`.<br>2. Browse/filter the table, then click "Xuất Mẫu số 5 (xlsx)". | Table shows the same Form 5 columns as Medical Staff's view but with no row-level action buttons (no register/dispose/hand-over controls anywhere on the page). Export downloads the filtered xlsx successfully. | At least one sample exists for the school. | Pending | | | Pending | | | Pending | | | Write actions (register, dispose, hand-over) remain exclusive to Service Caterers/Medical Staff |
