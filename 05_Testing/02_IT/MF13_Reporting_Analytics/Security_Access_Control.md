# Integration Test Case — Security & Access Control (MF13 Reporting & Analytics)

---

## Feature: System Security & Access Control

### 1. General Information

| Field | Value |
|---|---|
| Feature | System Security & Access Control |
| Test requirement | Verify complete RBAC matrix (7 roles x 9 endpoint groups), cross-school tenant isolation (404 RPT-404 / MSG-137), student PII leakage prevention (BR-181), System Admin multi-school privilege (`allSchools=true`), email recipient lockdown, and SQL injection defense. |
| Number of TCs | 9 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 9 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 9 | 0 |
| Round 3 | 0 | 0 | 9 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **RBAC Authorization Matrix** | | | | | | | | | | | | | | |
| MF13-TC-SEC-001 | Test full authorization matrix for 7 roles across 9 endpoint groups (63 combinations). | 1. Generate JWT tokens for all 7 roles (`system-admin`, `school-admin`, `medical-staff`, `teacher`, `service-caterers`, `supplier`, `parent`).<br>2. Execute requests against 9 endpoint groups (`/reports/catalog`, `/compliance-summary`, `/reports/{slug}/export`, `/report-exports`, `/report-schedules`, etc.). | 1. All 63 parameterized combinations match TDS §16 matrix exactly.<br>2. Unauthorized attempts return HTTP 403 Forbidden with `error.code = "IAM-004"` and `MSG-005`.<br>3. Authorized attempts return HTTP 2xx. | Fixtures initialized for all 7 role types. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | OWASP A01 / CWE-862 |
| **Multi-Tenant Isolation & Identity Scoping** | | | | | | | | | | | | | | |
| MF13-TC-SEC-002 | Test identity & school scoping from JWT (ignore query/body `schoolId` tampering). | 1. Authenticate as School Admin of School B (`SCH-HN-0051`).<br>2. Call `GET /api/v1/reports/compliance-summary?schoolId=SCH-HN-0042` (School A).<br>3. Call endpoint with `?school=TH Doan Thi Diem`. | 1. Query parameters `schoolId` and `school` are ignored.<br>2. Data returned is strictly for School B derived from JWT claim.<br>3. Returned `total` matches School B records. | Multi-school seed data active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | OWASP A01 / CWE-639 |
| MF13-TC-SEC-003 | Test cross-school data isolation (404 RPT-404 / MSG-137). | 1. Authenticate as School Admin of School B.<br>2. Attempt to query report rows for School A.<br>3. Attempt to download export generated for School A. | 1. Steps 2 and 3 return HTTP 404 Not Found (`RPT-404`, `MSG-137`).<br>2. Zero data fields or file URIs disclosed. | Multi-tenant seed data active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-176 / RPT-404 isolation |
| MF13-TC-SEC-006 | Test prevention of cross-school export code guessing (`RPX-…`). | 1. Authenticate as School Admin of School B.<br>2. Guess valid export code `RPX-2026-001` belonging to School A and call `/api/v1/report-exports/RPX-2026-001/file`. | 1. Returns HTTP 404 Not Found (`RPT-404`, `MSG-137`).<br>2. Binary file stream remains protected. | Export code `RPX-2026-001` active under School A. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | OWASP A01 export protection |
| MF13-TC-SEC-007 | Test System Admin multi-school overview privilege (`allSchools=true`). | 1. Authenticate as School Admin and call `GET /compliance-summary?allSchools=true`.<br>2. Authenticate as System Admin and call same request. | 1. Step 1 returns HTTP 403 Forbidden (`RPT-005`, `MSG-005`).<br>2. Step 2 returns HTTP 200 OK with aggregated platform-wide compliance summary. | System Admin and School Admin JWTs active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-176 multi-school privilege |
| **Data Privacy & PII Protection** | | | | | | | | | | | | | | |
| MF13-TC-SEC-004 | Test student PII leakage prevention across all export/query projections (BR-181). | 1. Execute query and export endpoints against seeded student records.<br>2. Scan JSON responses, PDF text, and XLSX cells for student names/codes (`HS-3A-001`). | 1. Zero occurrences of student PII found across all outputs.<br>2. All report outputs contain "Confidential" security label (BR-181). | Seed student records active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-181 PII protection |
| MF13-TC-SEC-005 | Test student PII leakage prevention in email attachments and application logs. | 1. Trigger automated schedule execution with email dispatch.<br>2. Scan GreenMail email body/attachments and application log output. | 1. Zero occurrences of student PII found in email body, attachments, or server logs. | GreenMail stub active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-181 log & email PII check |
| MF13-TC-SEC-008 | Test schedule email recipient lockdown (reject arbitrary recipient overrides). | 1. Attempt to create schedule specifying custom recipient email `hacker@external.com`. | 1. Request parameter `recipients` is ignored.<br>2. Email dispatch restricted strictly to `school_config.report_emails` (BR-179). | School config emails configured. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-179 recipient lockdown |
| **Injection Defense** | | | | | | | | | | | | | | |
| MF13-TC-SEC-009 | Test SQL injection defense in export history search parameter. | 1. Send SQL injection payloads (`' OR 1=1 --`) in `search` parameter of `GET /report-exports`. | 1. Query parameters safely sanitized via JPA Specification.<br>2. No 500 errors, no unauthorized data disclosed. | Specification search filter active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | OWASP A03 / CWE-89 |
