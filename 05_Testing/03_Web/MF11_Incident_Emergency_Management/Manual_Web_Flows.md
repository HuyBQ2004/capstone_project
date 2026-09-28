# Manual Web E2E Test — MF11 Incident & Emergency Management

> Manual testing on a real browser, no direct API calls. Same format as `02_IT/sample.md`.

---

## Feature: Incident Reporting — Teacher & Parent (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Incident Reporting — Teacher & Parent (Manual Web) |
| Test requirement | Submit a raw food-safety source report from `/teacher/incidents` (class-level, optional evidence) and `/parent/safety-concern` (single child, mandatory evidence, "emergency channel"). Both create a `source-reports` (RPT) record that lands in Medical Staff's triage queue. |
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
| **`/teacher/incidents`** | | | | | | | | | | | | | | |
| MF11-WEB-001 | Teacher submits a source report for multiple students in their own class, no evidence photo. | 1. Log in as Teacher, open `/teacher/incidents`.<br>2. In "Học sinh liên quan *", tick 2+ students from the class list.<br>3. Leave "Ảnh minh chứng" empty.<br>4. Fill "Mô tả chi tiết *" with symptoms/observations.<br>5. Click "Gửi báo cáo sự cố". | Success toast confirming the report was recorded. Form resets (student selection, description cleared). The new report appears in "Lịch sử báo cáo" below, tab "Chờ tiếp nhận", with status badge PENDING_TRIAGE. | Logged in as Teacher assigned to a class with at least 2 students. | Pending | | | Pending | | | Pending | | | No Severity field on this form — severity is set later by Medical Staff only |
| MF11-WEB-002 | Submit with no student selected and no description — blocked client-side. | 1. On `/teacher/incidents`, leave "Học sinh liên quan" empty and "Mô tả chi tiết" empty.<br>2. Click "Gửi báo cáo sự cố". | Inline error alert asking to fill in both the related students and the detailed description. No request is sent, form stays populated. | Logged in as Teacher. | Pending | | | Pending | | | Pending | | | |
| MF11-WEB-003 | Attach an oversized or wrong-format evidence file. | 1. On `/teacher/incidents`, in the evidence upload zone pick a file > 10MB, or a format other than PDF/JPG/PNG.<br>2. Observe the attached-files list. | A file-specific error message appears (size or format), the offending file is not added to the attachment list; other valid files already picked remain attached. | Logged in as Teacher. | Pending | | | Pending | | | Pending | | | BR-013 |
| MF11-WEB-004 | Filter "Lịch sử báo cáo" by report status tabs. | 1. On `/teacher/incidents`, after having at least one report per status, click through tabs "Tất cả", "Chờ tiếp nhận", "Đã gộp vào phiên điều tra", "Đã đóng — Không liên quan ATTP". | The table content changes per tab to only the reports matching that status; "Tất cả" shows every report for the class. | Class has reports in more than one status. | Pending | | | Pending | | | Pending | | | |
| **`/parent/safety-concern`** | | | | | | | | | | | | | | |
| MF11-WEB-005 | Parent submits an emergency safety report for their active child, with a required evidence photo. | 1. Log in as Parent, open `/parent/safety-concern`.<br>2. Select "Loại sự cố *".<br>3. Fill "Mô tả chi tiết sự việc *".<br>4. Attach one PDF/JPG/PNG evidence file (required).<br>5. Click "Gửi báo cáo khẩn cấp". | Success panel replaces the form: confirms the report reached the school's Medical Staff and School Administration immediately, no approval step. The report shows up in the child's incident/report history as PENDING_TRIAGE. | Logged in as Parent with one active child selected. | Pending | | | Pending | | | Pending | | | BR-138 — emergency channel bypasses approval |
| MF11-WEB-006 | Submit the parent emergency form without an evidence photo — blocked. | 1. On `/parent/safety-concern`, fill category + description but do NOT attach any file.<br>2. Click "Gửi báo cáo khẩn cấp". | Danger alert: must fill in all required fields including the evidence photo. Form is not submitted. | Logged in as Parent. | Pending | | | Pending | | | Pending | | | Evidence is mandatory for Parent, optional for Teacher — different rule per role |

---

## Feature: Medical Staff Triage & Incident Record (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Medical Staff Triage & Incident Record (Manual Web) |
| Test requirement | Triage raw reports (`source-reports`) at `/medical-staff/incidents` — merge into a draft record, complete the record, and either close it as not-food-safety or open a full investigation (creating an `incidents` INC case). Record detail lives at `/medical-staff/incidents/records/[id]`. |
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
| MF11-WEB-007 | View a pending report's full detail in a read-only modal. | 1. On `/medical-staff/incidents`, tab "Chờ tiếp nhận".<br>2. Click "Xem chi tiết" on a PENDING_TRIAGE row. | A modal opens showing class, suspected meal/dish, meal date, reporter, report time, symptoms, description, and evidence photo thumbnails — all read-only, no create button inside the modal. | At least one report in status PENDING_TRIAGE. | Pending | | | Pending | | | Pending | | | |
| MF11-WEB-008 | Select 2+ pending reports and create a new incident record (merge). | 1. On tab "Chờ tiếp nhận", tick 2 or more rows with status PENDING_TRIAGE.<br>2. Click "Tạo biên bản sự cố". | A new draft record (REC-xxx) is created merging the ticked reports; the browser navigates to `/medical-staff/incidents/records/[id]` with the content pre-filled from each source report. The originally-ticked reports now show status "Đang soạn biên bản (nháp)" back on the list. | 2+ reports in PENDING_TRIAGE for the same or different classes. | Pending | | | Pending | | | Pending | | | BR-146 — clustering signal, merge is manual/staff-driven |
| MF11-WEB-009 | Close a single pending report as not food-safety related. | 1. On tab "Chờ tiếp nhận", tick exactly one PENDING_TRIAGE row.<br>2. Click "Đóng — Không liên quan ATTP".<br>3. Confirm in the dialog. | Report moves to tab "Đã đóng" with reason "Không liên quan ATTP". No record/investigation is created (E3). | One report in PENDING_TRIAGE. | Pending | | | Pending | | | Pending | | | Closing only works one report at a time (F1) |
| MF11-WEB-010 | Complete a draft record and export it as a reference PDF. | 1. Open a DRAFT record at `/medical-staff/incidents/records/[id]`.<br>2. Fill "Địa điểm xảy ra sự cố" and "Thời điểm xảy ra sự cố".<br>3. Edit the content in the rich-text area.<br>4. Click "Hoàn tất biên bản".<br>5. Click "Xuất PDF" (now visible). | After completing, record status becomes COMPLETED and the page turns view-only except for "Xuất PDF" / "Mở phiên điều tra" / "Đóng". PDF titled "BIÊN BẢN SỰ CỐ AN TOÀN THỰC PHẨM" downloads/opens with the "Vào hồi... tại..." opening sentence built from location + occurred time. | Record exists in DRAFT status. | Pending | | | Pending | | | Pending | | | This PDF is an internal triage document, not the official signed incident report (UC-033) |
| MF11-WEB-011 | Open a full investigation from a completed record — title and affected students required. | 1. On `/medical-staff/incidents`, tab "Biên bản chờ xử lý", find a COMPLETED record.<br>2. Click "Mở phiên điều tra".<br>3. Leave "Tiêu đề sự cố" empty, click the confirm button. | Inline error: an incident title and at least one affected student are required. Dialog stays open, no incident created. Filling both in and re-submitting creates a new incident (INC-xxx), redirects to `/medical-staff/incidents/[id]`, and the source reports are merged into it. | A record in COMPLETED status. | Pending | | | Pending | | | Pending | | | Title/affected students can't be derived automatically from the free-text report |

---

## Feature: Incident Investigation, Severity & Emergency Escalation (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Incident Investigation, Severity & Emergency Escalation (Manual Web) |
| Test requirement | On `/medical-staff/incidents/[id]`, assess severity (LOW/MEDIUM/HIGH), verify HIGH triggers an emergency escalation banner/alert, mark suspect dishes and quarantine batches with a mandatory reason, and record a root-cause conclusion (preliminary/final). |
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
| MF11-WEB-012 | Assess severity as LOW without a risk-assessment note. | 1. Open a newly-opened incident (status SEVERITY_PENDING) at `/medical-staff/incidents/[id]`.<br>2. Open task "Đánh giá mức độ nghiêm trọng".<br>3. Select severity "Low — triệu chứng nhẹ, cục bộ", leave the risk-assessment text empty.<br>4. Click "Chốt đánh giá & bắt đầu điều tra". | Submits successfully — risk assessment is optional for LOW. Incident status moves to INVESTIGATING, severity badge shows LOW, task shows as done. | An incident exists in SEVERITY_PENDING status. | Pending | | | Pending | | | Pending | | | |
| MF11-WEB-013 | Assess severity as MEDIUM/HIGH without a risk-assessment note — blocked. | 1. On a SEVERITY_PENDING incident, select "Medium" or "High".<br>2. Leave "Đánh giá rủi ro lâm sàng" empty.<br>3. Click "Chốt đánh giá & bắt đầu điều tra". | The confirm button stays disabled (or submission is blocked) until the risk-assessment text is filled — cannot finalize Medium/High severity without a clinical risk note. | An incident exists in SEVERITY_PENDING status. | Pending | | | Pending | | | Pending | | | BR-147 |
| MF11-WEB-014 | Assess severity as HIGH — emergency escalation is visibly triggered. | 1. On a SEVERITY_PENDING incident, select "High — nguy cơ nghiêm trọng, cảnh báo ngay", fill the risk-assessment note.<br>2. Click "Chốt đánh giá & bắt đầu điều tra". | An error-styled toast/alert states an emergency alert has been sent to the local health authority. A red "Sự cố mức độ HIGH — Emergency Response" banner appears on the case, stating the alert was sent and the suspect batches are being locked. | An incident exists in SEVERITY_PENDING status. | Pending | | | Pending | | | Pending | | | BR-147/BR-148 |
| MF11-WEB-015 | Save a draft severity assessment without finalizing. | 1. On the severity task, select a severity and type a partial risk-assessment note.<br>2. Click "Lưu nháp" (not the finalize button). | A "Đã lưu lúc HH:mm" confirmation appears; incident status remains SEVERITY_PENDING (not yet INVESTIGATING); reopening the task later restores the draft text. | An incident exists in SEVERITY_PENDING status. | Pending | | | Pending | | | Pending | | | |
| MF11-WEB-016 | Quarantine a suspect batch without entering a reason — blocked. | 1. On an INVESTIGATING incident, open task "Món & lô nghi vấn", tick a suspect dish.<br>2. In the resulting batch list, tick a batch's quarantine checkbox WITHOUT filling the reason field or the cause dropdown. | The checkbox does not get checked (or an error toast fires) demanding the quarantine reason and the cause classification (supplier lot defect vs. school storage) before ticking. | Incident is INVESTIGATING with at least one suspect dish returning a traced batch. | Pending | | | Pending | | | Pending | | | BR-149 |
| MF11-WEB-017 | Quarantine a batch with reason and cause filled — one-way lock confirmed. | 1. On the same batch row, fill the reason text and pick a cause ("Lỗi tại lô hàng (NCC)" or "Lỗi bảo quản tại trường").<br>2. Tick the quarantine checkbox. | Success toast confirms the batch is quarantined; the checkbox becomes permanently checked and disabled (cannot be unticked) — quarantine is a one-way action. | Same as above. | Pending | | | Pending | | | Pending | | | BR-149/BR-217 — append-only |
| MF11-WEB-018 | Record a preliminary vs. final root-cause conclusion. | 1. On an INVESTIGATING/CAPA_PENDING incident, open the root-cause task, fill the conclusion text.<br>2. Save as "Sơ bộ" (preliminary) first — verify it saves and the incident can still move to CAPA.<br>3. Return later, edit the same field, save as "Chính thức" (final). | Preliminary save succeeds and is enough to proceed with a corrective-action request. Final save succeeds and locks in as the version that flows into the closing report. | Incident is past severity assessment. | Pending | | | Pending | | | Pending | | | BR-150/BR-151 |

---

## Feature: Corrective Action Plan (CAPA) Lifecycle (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Corrective Action Plan (CAPA) Lifecycle (Manual Web) |
| Test requirement | Medical Staff drafts and submits a CAPA (`/medical-staff/incidents/[id]`), School Admin approves/rejects it at `/school-admin/corrective-actions`, and Medical Staff validates effectiveness once evidence is submitted. |
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
| MF11-WEB-019 | Medical Staff submits a corrective-action plan for approval, without a responsible unit selected — blocked. | 1. On an incident with a saved root cause, open task "Yêu cầu khắc phục".<br>2. Fill "Biện pháp khắc phục", leave the responsible unit/caterer dropdown empty.<br>3. Click "Establish Measures"/submit. | Error toast requires both the corrective measures text and the assignee unit before submitting. | Incident has a saved root cause (preliminary or final). | Pending | | | Pending | | | Pending | | | BR-152 |
| MF11-WEB-020 | Submit a valid CAPA — incident moves to CAPA pending approval. | 1. Fill measures + pick a caterer/assignee.<br>2. Submit. | Success toast confirms the CAPA (CAPA-xxx) is created and sent to School Administration for approval; incident status changes to "Chờ duyệt CAPA". | Same as above. | Pending | | | Pending | | | Pending | | | BR-216 |
| MF11-WEB-021 | School Admin approves a CAPA without a verification note — blocked. | 1. Log in as School Admin, open `/school-admin/corrective-actions`.<br>2. Click "Duyệt kế hoạch (UC-005)" on a PENDING_ADMIN_APPROVAL row.<br>3. Leave the note empty, click "Duyệt kế hoạch (MSG-043)". | Inline error requires a verification note before approving. Dialog stays open. | A CAPA exists in PENDING_ADMIN_APPROVAL status. | Pending | | | Pending | | | Pending | | | |
| MF11-WEB-022 | School Admin approves a CAPA with a note — Caterer can then work on it. | 1. On the same dialog, fill the verification note, click "Duyệt kế hoạch (MSG-043)". | Success toast confirms approval; the CAPA leaves the pending list; the service-caterers side can now accept/work on it (verify via `/service-caterers/incidents` or corrective-actions view if available). | Same as above. | Pending | | | Pending | | | Pending | | | Approving here only opens the plan for execution — effectiveness is judged later by Medical Staff, not School Admin again |

---

## Feature: Incident Closure (School Admin, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Incident Closure (Manual Web) |
| Test requirement | Verify the closure checklist and gate at `/school-admin/incidents/[id]/close` — School Administration is the only role that can close an incident, and only when CAPA is complete, all affected students are recovered, and quarantined batches have a final disposition. |
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
| MF11-WEB-023 | Attempt to close an incident with an incomplete CAPA, unrecovered students, and a locked batch — all 3 gates block. | 1. Log in as School Admin, open an incident with an unfinished CAPA and unresolved quarantine at `/school-admin/incidents/[id]/close`.<br>2. Observe the "Điều kiện đóng sự cố (BR-155)" checklist without ticking anything.<br>3. Click "Đóng sự cố (BR-155)". | Button is disabled while any condition is unmet. Checklist clearly shows 3 rows: CAPA completion, quarantined batches resolved, student recovery — each with its own status badge and remaining count. | An incident with CAPA not COMPLETED, at least one quarantined batch unresolved, and at least one affected student not recovered. | Pending | | | Pending | | | Pending | | | BR-155 |
| MF11-WEB-024 | Mark all affected students as recovered — checklist updates live. | 1. On the close page, for each affected student's row, click the "recovered" checkbox. | Each ticked student shows badge "Đã hồi phục hoàn toàn" and becomes disabled (cannot untick). Once all students are ticked, the recovery row in the checklist turns green/success. | Incident has 1+ affected students not yet recovered. | Pending | | | Pending | | | Pending | | | |
| MF11-WEB-025 | Close without a closing statement — blocked even if the 3 conditions are met. | 1. Ensure CAPA is COMPLETED, all students recovered, and no batch is still locked.<br>2. Leave "Kết luận đóng sự cố *" empty.<br>3. Click "Đóng sự cố (BR-155)". | Button remains disabled / an error states the closing statement is mandatory. Incident is not closed. | All 3 readiness conditions already satisfied. | Pending | | | Pending | | | Pending | | | |
| MF11-WEB-026 | Close a fully-ready incident with a closing statement. | 1. With all 3 conditions satisfied, fill "Kết luận đóng sự cố *".<br>2. Click "Đóng sự cố (BR-155)". | Success toast confirms closure; incident status becomes CLOSED; browser returns to `/school-admin/incidents`; reopening the incident detail shows it fully read-only for every role. | All 3 readiness conditions satisfied, statement filled. | Pending | | | Pending | | | Pending | | | Only School Admin has this action — Medical Staff/Caterers never see a close button |

---

## Feature: Cross-Role Incident Visibility & Data Masking (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Cross-Role Incident Visibility & Data Masking (Manual Web) |
| Test requirement | Confirm each role sees the correct, role-appropriate slice of the same incident: School Admin (dashboard + export), Service Caterers (masked read-only), Teacher (own-class only), across `/school-admin/incidents`, `/service-caterers/incidents`, `/teacher/incidents`. |
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
| MF11-WEB-027 | School Admin dashboard stat cards and filters reflect real incident data. | 1. Log in as School Admin, open `/school-admin/incidents`.<br>2. Check the 3 stat cards ("Sự cố đang mở", "Mức độ Cao đang mở", "CAPA chờ duyệt").<br>3. Filter the table by severity and by status. | Stat card counts match the actual open/HIGH-severity/pending-CAPA incidents; the "Mức độ Cao đang mở" card turns to a danger tone when its count > 0. Filtering the table narrows results accordingly. | At least one HIGH-severity open incident and one pending CAPA exist. | Pending | | | Pending | | | Pending | | | |
| MF11-WEB-028 | Export incident report with severity/status filters (UC-031). | 1. On `/school-admin/incidents`, click "Xuất báo cáo sự cố (UC-031)".<br>2. Pick a severity and/or status filter, click "Tạo bản xem trước".<br>3. Pick PDF or Excel, click "Xuất PDF/Excel". | Preview table lists only incidents matching the chosen filters. Export button is disabled until a preview is generated or if the preview is empty. Successful export shows a confirmation toast mentioning the export count and format. | 2+ incidents with different severities/statuses exist. | Pending | | | Pending | | | Pending | | | |
| MF11-WEB-029 | Service Caterers view of an incident hides clinical/PII fields. | 1. Log in as Service Caterers, open `/service-caterers/incidents`, click into an incident row.<br>2. Compare visible fields against what Medical Staff/School Admin see for the same incident. | The Caterer's incident detail is read-only, has no severity value, no student names/classes, no symptoms/first-aid notes — only an affected-student count and quarantine/CAPA-coordination information. No close action is available anywhere on the page. | The same incident exists and is visible to Medical Staff/School Admin. | Pending | | | Pending | | | Pending | | | BR-201/PDPD data masking for external partners |
| MF11-WEB-030 | Teacher only sees source reports from their own class in the history list. | 1. Log in as Teacher of class A, open `/teacher/incidents`.<br>2. Note the reports listed in "Lịch sử báo cáo".<br>3. Log in as Teacher of a different class B (not involved in class A's incidents), open the same page. | Teacher A only ever sees reports tied to class A; Teacher B's history never shows class A's reports, even after class A's report has been merged into a full investigation. | Reports/incidents exist for class A that class B's teacher is not part of. | Pending | | | Pending | | | Pending | | | |
