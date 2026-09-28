# Integration Test Case — Partnership Application Workflow (MF04 Supplier & Partner Management)

---

## Feature: Submit / One-Open-Application-Per-School / Resubmit

### 1. General Information

| Field | Value |
|---|---|
| Feature | Submit / One-Open-Application-Per-School / Resubmit |
| Test requirement | Verify that submitting a partnership application persists the full submitted payload (not just `{school, submittedAt, status}` as the mock does today — `CF-M04-9`) and returns both `schoolId` and `school` (L8); that only one open application (`PENDING`/`ACTION_REQUIRED`) may exist per `(partner, school)` pair (`uq_app_open_per_partner_school`); and that resubmitting a rejected application keeps its original code and clears the previous reviewer comment (UC-170/UC-158). |
| Number of TCs | 3 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 3 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 3 | 0 |
| Round 3 | 0 | 0 | 3 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Submit Application — Full Payload Persisted** | | | | | | | | | | | | | | |
| MF04-TC-INT-005 | Test that submitting a partnership application stores the complete submitted form (company name, tax ID, address, attached certificate IDs) and returns both the school's code and display name. | 1. `POST /api/v1/partnership-applications` (multipart): part `meta = {schoolId, companyName, taxId, address, certificateDocumentIds:["DOC-GF-01"]}`, part `businessLicense = cert-valid.pdf`.<br>2. Read response.<br>3. Query `partnership_applications` and `partnership_application_documents`. | 1. HTTP 201, MSG-028.<br>2. Body has **both** `"schoolId":"SCH-NBK-0001"` and `"school":"TH Nguyễn Bỉnh Khiêm"` (L8).<br>3. `partnership_applications` row has `company_name`, `tax_id`, `address`, `partner_id`, `school_id` all populated — no field dropped (CF-M04-9); `partnership_application_documents` has exactly 1 row linking the new application to `DOC-GF-01`; the license file's `file_key` is non-null (stored via `StorageService`, D10); `submitted_at` is an ISO `Instant` (D4). | Testcontainers PostgreSQL 16, Flyway migrated. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | UC-152 Normal Flow step 6, MSG-028, CF-M04-9, L8 |
| **One Open Application Per (Partner, School)** | | | | | | | | | | | | | | |
| MF04-TC-INT-006 | Test that a partner cannot open a second application at a school where one is already pending, but can open one at a different school, and can reopen one at the same school once the prior one is rejected. | 1. FX-007: `SUP-001` already has `APP-2026-04` `PENDING` with TH Nguyễn Bỉnh Khiêm.<br>2. Submit a second application to the **same** school.<br>3. Submit an application to a **different** school.<br>4. Reject `APP-2026-04`, then submit a new application to TH Nguyễn Bỉnh Khiêm again. | 1. Baseline confirmed.<br>2. HTTP 409 `SUP-409`/MSG-118.<br>3. HTTP 201 — not blocked by the other school's open application.<br>4. HTTP 201 — the partial unique index only blocks `PENDING`/`ACTION_REQUIRED`, not `REJECTED`. | FX-007. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | UC-152/UC-156 Preconditions, `uq_app_open_per_partner_school`, MSG-118; **not** the D5 client-supplied-id code (`SUP-139`) — see `MF04-TC-INT-010` in Contract_And_Schema.md for that distinct case |
| **Resubmit — Keeps the Same Application Code** | | | | | | | | | | | | | | |
| MF04-TC-INT-007 | Test that resubmitting a rejected application updates the existing record in place (same code), clears the previous review comment, and is blocked for applications that are not `REJECTED`/`ACTION_REQUIRED`. | 1. FX-007: `APP-2026-01` `REJECTED`, `comment` populated.<br>2. `POST /api/v1/partnership-applications/APP-2026-01/resubmit` with corrected payload.<br>3. Attempt resubmit on a `PENDING` application, then on an `APPROVED` one. | 1. Baseline confirmed.<br>2. HTTP 200, MSG-029; `status=PENDING`; **`code` unchanged** (`APP-2026-01`); `comment` cleared to `NULL`; `submitted_at` refreshed; `PartnershipApplicationResubmitted` event published.<br>3. Both variants → HTTP 409, no field changes. | FX-007. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | UC-170 Normal Flow step 6, UC-158, MSG-029; if a new code is minted instead (as the mock does today), the review history of one physical application is fragmented across multiple unrelated records |
