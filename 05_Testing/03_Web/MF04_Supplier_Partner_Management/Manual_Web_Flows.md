# Manual Web E2E Test — MF04 Supplier & Partner Management

> Manual testing on a real browser, no direct API calls. Same format as `02_IT/sample.md`.

---

## Feature: Partner Self-Registration (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Partner Self-Registration (Manual Web) |
| Test requirement | Public sign-up of a Raw Food Supplier / Service Caterer account via `/register/supplier` and `/register/caterer`, before any partnership application exists. |
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
| **`/register/supplier`** | | | | | | | | | | | | | | |
| MF04-WEB-001 | Successful raw-food supplier self-registration with all required fields and documents. | 1. Open `/register/supplier` (no login required).<br>2. Fill "Company Name", "Business License No", "Address", "Contact Person", "Email", "Phone".<br>3. Attach "Business license photo" and "Valid food safety certificate" under the documents section.<br>4. Click the submit button. | Registration succeeds — a confirmation with a generated partner code (e.g. `id` returned) is shown. No login is required to reach this page (public form, UC-071). | No existing account with the same email. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-002 | Submit `/register/supplier` missing a required document (food safety certificate). | 1. Open `/register/supplier`.<br>2. Fill all text fields correctly.<br>3. Attach only "Business license photo", leave "food safety certificate" empty.<br>4. Click submit. | Blocked with a validation message asking for the missing required document — form does not submit. | None. | Pending | | | Pending | | | Pending | | | |
| **`/register/caterer`** | | | | | | | | | | | | | | |
| MF04-WEB-003 | Successful service caterer self-registration. | 1. Open `/register/caterer`.<br>2. Fill in all required company fields.<br>3. Attach required compliance documents.<br>4. Click submit. | Registration succeeds with a generated partner code, ready to submit a partnership application to a school afterward. | No existing account with the same email. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Partnership Application Workflow (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Partnership Application Workflow (Manual Web) |
| Test requirement | Supplier/Caterer submits a partnership application to a school at `/supplier/partnerships` and `/service-caterers/partnerships` (shared `PartnershipWorkspace` component), tracks status, and resubmits after rejection. |
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
| MF04-WEB-004 | Supplier submits a new partnership application to a school ("Submit new school" / "Nộp hồ sơ trường mới"). | 1. Log in as a Supplier account already self-registered, go to `/supplier/partnerships`.<br>2. Click "Nộp hồ sơ trường mới" (Submit new school).<br>3. Select an ACTIVE school, verify company name/tax ID/address are pre-filled from the profile.<br>4. Attach the business license file (PDF/JPG/PNG), tick at least one valid food-safety certificate from the compliance documents list.<br>5. Click "Nộp đơn (UC-152)" (Submit application). | Application saved with status "Pending Approval" (MSG-028), a confirmation email is sent, and the new application row appears in the workspace. | Logged in as Supplier, profile already has ≥1 non-expired uploaded certificate (see Certificates feature below), at least one ACTIVE school exists. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-005 | Submit application with an expired certificate selected — must be blocked. | 1. On the application dialog, try to tick a certificate whose status is EXPIRED (checkbox disabled). | The expired certificate's checkbox cannot be selected; if forced past validation the submit is blocked with an expired-certificate error (MSG-013). | An EXPIRED food-safety certificate exists in the supplier's compliance documents. | Pending | | | Pending | | | Pending | | | BR-047/BR-048 |
| MF04-WEB-006 | Submit application with an invalid tax ID format. | 1. On the application dialog, enter a tax ID that does not match 10 digits (optionally `-3 digits`), e.g. "abc123".<br>2. Click submit. | Blocked with a tax-ID format error (MSG-007) — application not submitted. | Logged in as Supplier/Caterer. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-007 | Service caterer tries to submit a second application while one is already Pending/Approved (exclusive partner rule). | 1. Log in as a Service Caterer account that already has an approved or pending partnership.<br>2. Go to `/service-caterers/partnerships`. | The "Submit application" button is hidden/disabled, replaced by an info banner explaining that a caterer may only serve one school at a time and must resolve the existing partnership first. | Caterer already has one partnership in APPROVED or PENDING state. | Pending | | | Pending | | | Pending | | | BR-EXCLUSIVE-CATERER |
| MF04-WEB-008 | Resubmit an application after rejection ("Resubmit" — UC-170). | 1. Log in as a Supplier with a REJECTED application.<br>2. Open the rejected application, click "Nộp lại đơn (UC-170)" (Resubmit).<br>3. Update the fields per the rejection reason shown, re-attach documents, click submit. | Resubmission succeeds, the same application code moves back to Pending Approval; the school selector is locked to the original school (cannot switch school on resubmit). | Supplier has one REJECTED application with a recorded rejection reason. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-009 | View current partnerships table showing school, delivery address, partnership start date, compliance-document status, and partnership status. | 1. Log in as Supplier, go to `/supplier/partnerships`.<br>2. Inspect the "Đối tác hiện tại" (Current partners) table. | Table shows one row per partnership with School, Delivery Address, Partnership start date, compliance-document status badge, and partnership status badge (BR-041) — matches actual backend data, not placeholder text. | Supplier has at least one existing partnership (any status). | Pending | | | Pending | | | Pending | | | |

---

## Feature: School Admin Partner Approval & Suspension (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | School Admin Partner Approval & Suspension (Manual Web) |
| Test requirement | School Admin reviews, approves, rejects, suspends, and reinstates partner applications from `/school-admin/partnerships` and the detail page `/school-admin/partnerships/[id]`. |
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
| **`/school-admin/partnerships` (list)** | | | | | | | | | | | | | | |
| MF04-WEB-010 | Filter the partnership list by partner type and status. | 1. Log in as School Admin, go to `/school-admin/partnerships`.<br>2. Set "Loại đối tác" (Partner type) filter to "Raw Food Supplier", then "Trạng thái" (Status) filter to "PENDING". | The table refreshes to show only PENDING raw-food-supplier applications; column badges match (type badge, status badge, certificate expiry + status). | At least one partner of each type/status combination exists in seed/test data. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-011 | Quick-approve a PENDING partner directly from the list row. | 1. On the list, find a PENDING row where the applicant's registration email is verified.<br>2. Click "Phê duyệt" (Approve) in that row. | Toast confirms approval (MSG-037); the row's status badge changes to APPROVED without a full page reload. | A PENDING partner exists with a verified registration email and no expired documents. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-012 | Quick-reject a PENDING partner from the list with a mandatory reason. | 1. On a PENDING row, click "Từ chối" (Reject).<br>2. Leave the reason box empty, verify "Xác nhận từ chối" (Confirm) stays disabled.<br>3. Enter a reason, click "Xác nhận từ chối". | Confirm button is disabled while the reason is empty (BR-048). After entering a reason and confirming: toast confirms rejection, the reason is recorded, row status becomes REJECTED. | A PENDING partner exists. | Pending | | | Pending | | | Pending | | | |
| **`/school-admin/partnerships/[id]` (detail)** | | | | | | | | | | | | | | |
| MF04-WEB-013 | Approve a complete PENDING application from the detail page. | 1. Click a PENDING row to open its detail page.<br>2. Verify the "Email đăng ký đã xác thực" (registration email verified) badge is green, and no expired-document alert is shown.<br>3. Click "Phê duyệt (UC-004/UC-007)" (Approve). | Approve button is enabled (verified email, no expired docs). On click: toast MSG-037, redirected back to the partnership list, partner status is APPROVED. | Applicant's registration email is verified; no document has status URGENT/EXPIRED. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-014 | Approve button is blocked when the applicant's registration email is not yet verified. | 1. Open the detail page of a PENDING partner whose registration email is unverified. | A warning alert "Chưa thể phê duyệt" is shown, explaining the applicant has not clicked the activation link; the "Phê duyệt" button is disabled with a matching tooltip (SUP-015). | Partner's `registrantEmailVerified` is false. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-015 | Approve button is blocked when a submitted document has expired. | 1. Open the detail page of a PENDING partner that has ≥1 verification document with status EXPIRED/URGENT. | A danger alert "Chứng nhận hết hạn (BR-047, MSG-013)" is shown; the "Phê duyệt" button is disabled with a matching tooltip. | Partner has an expired/urgent verification document. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-016 | Suspend an APPROVED partner with a mandatory reason. | 1. Open the detail page of an APPROVED partner.<br>2. Click "Đình chỉ hợp tác" (Suspend partnership).<br>3. Leave the reason textbox empty, verify "Xác nhận đình chỉ" (Confirm suspend) stays disabled.<br>4. Enter a reason (e.g. repeated failed receiving inspections), click confirm. | Confirm button disabled while reason is empty. After confirming: toast reports the suspension and states how many of the partner's login accounts were locked (ADR-M04-6 cascade); partner status becomes SUSPENDED; the reason is later shown as a danger alert on this page. | Partner status is APPROVED, has ≥1 linked login account. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-017 | Reinstate a SUSPENDED partner whose documents are still valid. | 1. Open the detail page of a SUSPENDED partner with no expired documents.<br>2. Click "Khôi phục hợp tác" (Reinstate partnership). | Toast confirms reinstatement and states that the partner's login account(s) still need to be manually unlocked by a System Admin at `/system-admin/users` (reinstate does not auto-unlock accounts); partner status becomes APPROVED again. | Partner status is SUSPENDED, all documents valid. | Pending | | | Pending | | | Pending | | | Reinstate does NOT auto-restore login access — confirm this is still true |
| MF04-WEB-018 | Reinstate is blocked when the partner has an expired document. | 1. Open the detail page of a SUSPENDED partner that also has an EXPIRED/URGENT document. | The "Khôi phục hợp tác" button is disabled with a tooltip citing BR-047 (expired certificate blocks reinstatement). | Partner status is SUSPENDED and has an expired document. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Partner Approval History & Registration Documents (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Partner Approval History & Registration Documents (Manual Web) |
| Test requirement | School Admin views the immutable audit trail of partner approve/reject/suspend actions, and views/downloads the documents submitted at registration time, from `/school-admin/partnerships` and its detail page. |
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
| MF04-WEB-019 | View "Partnership Approval History" table below the partner list. | 1. On `/school-admin/partnerships`, scroll to "Lịch sử phê duyệt đối tác (UC-106)".<br>2. Search the table for a recent action (e.g. by actor name). | Table shows Timestamp, Actor, Action for partner-related audit entries scoped to the current school (BR-176); a just-performed approve/reject/suspend from this session appears here. | At least one approve/reject/suspend action was performed earlier in this test cycle. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-020 | View and preview a verification document attached at application time. | 1. Open a partner's detail page.<br>2. In "Giấy tờ xác minh" (Verification documents), click "Xem" (View) on a row with a document. | An in-app preview dialog opens showing the file content (image/PDF), fetched via an authenticated request (no public URL). | Partner has ≥1 verification document with a viewable file. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-021 | View and download a document submitted at self-registration time. | 1. Open a partner's detail page.<br>2. Under "Tài liệu nộp kèm lúc đăng ký" (Documents submitted at registration), click "Tải về" (Download) on a listed file. | File downloads successfully with its original file name; file size and upload date shown match what was submitted at `/register/supplier`. | Partner self-registered with ≥1 attached document. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Supplier Compliance Documents & Certificates (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Supplier Compliance Documents & Certificates (Manual Web) |
| Test requirement | Supplier uploads, views, downloads, and renews compliance documents (food safety certificate, product origin, business license, self-declaration) at `/supplier/certificates`, with expiry tracking and blocking of expired food-safety certificates. |
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
| **Upload** | | | | | | | | | | | | | | |
| MF04-WEB-022 | Upload a new valid food safety certificate. | 1. Log in as Supplier, go to `/supplier/certificates`.<br>2. Click "Tải tài liệu mới" (Upload new document).<br>3. Select classification "Food Safety Certificate", fill Name, Number, Issuer, Issue date (dd/mm/yyyy, not in the future).<br>4. Attach a PDF/JPG/PNG file ≤10MB, set an expiry date in the future.<br>5. Click "Lưu tài liệu" (Save document). | Document is saved (MSG-029), appears in the compliance documents table with status computed by the server (e.g. VALID). | Logged in as Supplier. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-023 | Upload a food safety certificate without an expiry date — must be blocked. | 1. On the upload dialog, select "Food Safety Certificate", fill all other required fields, leave "Ngày hết hạn" (Expiry date) empty.<br>2. Click Save. | Blocked with "Chứng nhận ATTP bắt buộc có ngày hết hạn — BR-047" — document not saved. | None. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-024 | Upload a food safety certificate with an expiry date already in the past — must be blocked. | 1. On the upload dialog, select "Food Safety Certificate", fill required fields, set an expiry date before today.<br>2. Click Save. | Blocked with MSG-013 ("expired certificate cannot be uploaded", BR-048) — document not saved. | None. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-025 | Upload with an oversized or wrong-format file. | 1. On the upload dialog, attach a file >10MB, or a file with an unsupported extension (e.g. `.docx`).<br>2. Click Save. | Blocked with MSG-008 (file too large) or MSG-009 (unsupported format) shown under the file field — document not saved. | None. | Pending | | | Pending | | | Pending | | | |
| **Renewal / Update** | | | | | | | | | | | | | | |
| MF04-WEB-026 | Update (renew) an existing document with a new file and extended expiry date. | 1. On the compliance documents table, click "Cập nhật (UC-091)" (Update) on an existing document row.<br>2. Change the expiry date to a later date, attach the renewed file.<br>3. Click "Lưu cập nhật" (Save update). | Update succeeds (MSG-029/UC-091), the document row now shows the new expiry date and an up-to-date status. | Document exists and belongs to this supplier. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-027 | Update dialog blocks saving without attaching a replacement file. | 1. Open "Cập nhật" on an existing document, change only the name, leave the file field empty.<br>2. Click "Lưu cập nhật". | Blocked with "Bắt buộc: tải lên file tài liệu thay thế (UC-091)" — update not saved. | Document exists. | Pending | | | Pending | | | Pending | | | |
| **Expiry Tracking** | | | | | | | | | | | | | | |
| MF04-WEB-028 | Expiring/expired documents trigger the warning banner (MSG-053). | 1. As Supplier, ensure ≥1 document has status EXPIRING, URGENT, or EXPIRED (via seed data or a prior upload/update).<br>2. Reload `/supplier/certificates`. | A warning alert "MSG-053 — Sắp hết hạn" lists each affected document with its name and expiry date, phrased per severity (EXPIRED / <30 days urgent / <6 months). | At least one document in EXPIRING/URGENT/EXPIRED status. | Pending | | | Pending | | | Pending | | | BR-047, Article 37 Food Safety Law 2010 |
| MF04-WEB-029 | View and download a compliance document file. | 1. On any document row, click "Xem" (View) — verify a preview dialog opens.<br>2. Click "Tải về" (Download) — verify the file downloads with the correct name. | Preview opens showing the actual file content; download saves a file matching the document name/extension. | Document row has an attached file. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Delivery Staff & Biometrics Management (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Delivery Staff & Biometrics Management (Manual Web) |
| Test requirement | Supplier manages delivery personnel (name, national ID, phone, vehicle plate) and their face-biometric registration at `/supplier/delivery-staff`, plus per-person personal compliance documents (health certificate, food safety knowledge certificate). |
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
| **Add delivery person** | | | | | | | | | | | | | | |
| MF04-WEB-030 | Add a new delivery person with a live-captured face photo. | 1. Log in as Supplier, go to `/supplier/delivery-staff`.<br>2. Click "Thêm nhân viên" (Add staff member).<br>3. Fill Full name, National ID (12 digits), Phone (+84/0 format), Vehicle plate (e.g. `29C-123.45`).<br>4. Click "Bật Camera để chụp trực tiếp" (Turn on camera to capture live), allow camera permission, click "Chụp hình" (Capture), verify the preview appears.<br>5. Click "Lưu & đăng ký sinh trắc học" (Save & register biometrics). | New delivery person is created and biometrics registered in one flow (BR-050/BR-051); toast confirms; the new row shows "Đã có sinh trắc học" (Registered) badge with the registration date. | Logged in as Supplier, browser camera available/permitted. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-031 | Add delivery person with invalid national ID (not 12 digits). | 1. On the add-staff dialog, enter a national ID with fewer than 12 digits.<br>2. Fill remaining fields correctly, attempt submit. | Blocked with "Số CCCD phải gồm 12 chữ số" under the field — form not submitted. | None. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-032 | Add delivery person with invalid vehicle plate format. | 1. Enter a vehicle plate that doesn't match the expected pattern (e.g. "ABC123").<br>2. Attempt submit. | Blocked with a plate-format error showing the expected example (e.g. `29C-123.45`) — form not submitted. | None. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-033 | Add delivery person without capturing a face photo — must be blocked. | 1. Fill all text fields correctly.<br>2. Do not turn on the camera / do not capture a photo.<br>3. Click "Lưu & đăng ký sinh trắc học". | Blocked with "Vui lòng chụp ảnh khuôn mặt để đăng ký sinh trắc học (BR-051)" — no record created. | None. | Pending | | | Pending | | | Pending | | | |
| **Update biometrics / personal documents** | | | | | | | | | | | | | | |
| MF04-WEB-034 | Re-register (update) the face photo for an already-registered delivery person. | 1. On a row already showing "Đã có sinh trắc học", click "Cập nhật khuôn mặt" (Update face).<br>2. Capture a new photo via the camera, click "Đăng ký sinh trắc học" (Register biometrics). | Toast confirms the update; the person can still be assigned to deliveries; registration timestamp updates. | Delivery person already has REGISTERED biometric status. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-035 | Add a personal compliance document (health certificate) for a delivery person. | 1. On a delivery person's row, click "Hồ sơ cá nhân" (Personal documents).<br>2. Click "Thêm hồ sơ cá nhân" (Add personal document), select "Giấy khám sức khỏe" (Health certificate), fill Name/Number/Issuer/Issue date, attach a file.<br>3. Click "Lưu hồ sơ" (Save document). | Document saved and appears in the personal documents table for that person, with expiry-tracking status shared with the company-level compliance mechanism (BR-047). | Logged in as Supplier, delivery person exists. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-036 | Delivery person list shows biometric status badges correctly for NOT_REGISTERED / PENDING_REGISTRATION / REGISTERED. | 1. Go to `/supplier/delivery-staff`.<br>2. Inspect the "Sinh trắc học (BR-051)" column for rows in each of the three states. | Each row shows the correct badge/color (danger for Not registered, warning for Pending, success for Registered) and, for Registered, the registration date underneath. | Delivery persons exist in at least two different biometric states. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Downstream Partner Visibility (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Downstream Partner Visibility (Manual Web) |
| Test requirement | Read-only views of connected partners and their compliance status from Service Caterer (`/service-caterers/suppliers`) and Medical Staff (`/medical-staff/suppliers`) roles. |
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
| **Service Caterer view** | | | | | | | | | | | | | | |
| MF04-WEB-037 | Service Caterer views the list of connected raw-food suppliers only (not other caterers). | 1. Log in as Service Caterer, go to `/service-caterers/suppliers`.<br>2. Inspect the supplier cards shown. | Only partners of type "Raw Food Supplier" linked to this school are listed (no service caterer entries); each card shows a "Hợp tác" (Partnered) or status badge. | School has ≥1 approved raw-food-supplier partnership. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-038 | Service Caterer opens a supplier's product catalog and price list (read-only). | 1. On a supplier card, click "Xem danh mục sản phẩm" (View product catalog).<br>2. Inspect the dialog. | Dialog shows the supplier's contact info and a table of offered products with unit price, unit, and packaging — read-only, no edit controls present. | Supplier has ≥1 product in its catalog. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-039 | Search suppliers by name/code on `/service-caterers/suppliers`. | 1. Type a partial supplier name into the search box. | The card grid filters live to matching suppliers only; the header count updates to match. | Multiple suppliers exist. | Pending | | | Pending | | | Pending | | | |
| **Medical Staff view** | | | | | | | | | | | | | | |
| MF04-WEB-040 | Medical Staff views ALL connected partners (both raw-food suppliers and service caterers) with no edit/approve actions available. | 1. Log in as Medical Staff, go to `/medical-staff/suppliers`.<br>2. Confirm partners of both types appear in the list, and confirm there is no approve/reject/suspend/edit button anywhere on the page. | Both partner types are listed (unlike the caterer-only view for Service Caterers); the page is strictly read-only — School Admin/System Admin retain those actions. | School has both a raw-food-supplier and a service-caterer partnership. | Pending | | | Pending | | | Pending | | | |
| MF04-WEB-041 | Medical Staff views a partner's compliance documents to assess validity. | 1. On a partner card, click "Xem hồ sơ tuân thủ" (View compliance documents).<br>2. Inspect the documents table, click "Xem" (View) on a document with a file. | Dialog lists each compliance document with number, expiry date, and status badge (BR-046/047); clicking "Xem" opens an authenticated in-app preview of the file. | Partner has ≥1 uploaded compliance document. | Pending | | | Pending | | | Pending | | | |

---
