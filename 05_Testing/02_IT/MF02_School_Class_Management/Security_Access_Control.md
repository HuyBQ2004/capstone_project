# Integration Test Case — Security & Access Control (MF02 School & Class Management)

---

## Feature: System Security & Access Control

### 1. General Information

| Field | Value |
|---|---|
| Feature | System Security & Access Control |
| Test requirement | Verify complete RBAC matrix (7 roles x 17 endpoint groups), cross-school tenant isolation (404 SCH-404 / MSG-137), JWT authentication, identity trust scoping, server-side capacity gate enforcement, and injection prevention. |
| Number of TCs | 10 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 9 | 0 | 1 | 0 |
| Round 2 | 0 | 0 | 10 | 0 |
| Round 3 | 0 | 0 | 10 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **RBAC Authorization Matrix** | | | | | | | | | | | | | | |
| MF02-TC-SEC-001 | Test full authorization matrix for 7 roles across 17 endpoint groups (119 combinations). | 1. Generate JWT tokens for all 7 roles (`system-admin`, `school-admin`, `medical-staff`, `teacher`, `service-caterers`, `supplier`, `parent`).<br>2. Execute requests against 17 endpoint groups (`/schools`, `/classrooms`, `/academic-years`, `/config`, etc.). | 1. All 119 parameterized combinations match TDS §16 matrix exactly.<br>2. Unauthorized attempts return HTTP 403 Forbidden with `error.code = "IAM-004"` and `MSG-005`.<br>3. Authorized attempts return HTTP 2xx. | Fixtures initialized for all 7 role types. | Pending | 2026-08-05 | Nam | Pending | | | Pending | | | OWASP A01 / CWE-862 |
| **Multi-Tenant Isolation** | | | | | | | | | | | | | | |
| MF02-TC-SEC-002 | Test cross-school data isolation by `schoolId` in JWT. | 1. Authenticate as School Admin of School B (`SCH-HN-0051`).<br>2. Call `GET /api/v1/classrooms/3A` (Classroom in School A `SCH-HN-0042`).<br>3. Call `GET /api/v1/academic-years/AY-2025` (Academic year in School A). | 1. Steps 2 and 3 return HTTP 404 Not Found (`SCH-404`, `MSG-137`) to avoid disclosing record existence across tenants.<br>2. Response body contains zero data fields from School A. | School A and School B multi-tenant seed data active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | OWASP A01 / Tenant Isolation |
| MF02-TC-SEC-009 | Test school policy cross-tenant read protection (BR-022). | 1. Authenticate as School Admin of School B.<br>2. Attempt to view policy of School A (`GET /api/v1/schools/SCH-HN-0042/config`). | 1. Returns HTTP 404 Not Found (`SCH-404`, `MSG-137`).<br>2. School A configuration parameters remain inaccessible. | School A configuration active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-022 policy access |
| **JWT Verification & Security Filters** | | | | | | | | | | | | | | |
| MF02-TC-SEC-003 | Test unauthenticated and expired JWT access rejection. | 1. Send request to protected endpoint `/api/v1/classrooms` without `Authorization` header.<br>2. Send request with expired JWT token. | 1. Steps 1 and 2 return HTTP 401 Unauthorized (`IAM-005`, `MSG-004`). | JwtAuthenticationFilter active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | OWASP A01 / CWE-306 |
| MF02-TC-SEC-004 | Test identity scoping from JWT claims (prevent payload parameter tampering). | 1. Authenticate as School Admin of School B.<br>2. Call `POST /api/v1/classrooms` specifying body `{"schoolId":"SCH-HN-0042"}`. | 1. `schoolId` parameter in body is ignored.<br>2. Classroom created strictly under `SCH-HN-0051` derived from JWT claim. | School Admin session active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | OWASP A01 / CWE-639 |
| **Endpoint Protection & Gate Enforcement** | | | | | | | | | | | | | | |
| MF02-TC-SEC-005 | Test public school registration rate-limiting protection. | 1. Send 20 consecutive POST requests to `/api/v1/schools/register` within 10 seconds. | 1. Initial requests within threshold succeed (201 Created).<br>2. Requests exceeding threshold return HTTP 429 Too Many Requests (`SCH-014`, `MSG-141`). | Rate-limiting filter active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | Rate-limiting defense |
| MF02-TC-SEC-006 | Test coordinate unlock requirement for System Admin role. | 1. Authenticate as System Admin.<br>2. Send POST to `/unlock-coordinate` with empty justification `{}`. | 1. Returns HTTP 422 Unprocessable Entity (`IAM-422`, `MSG-138`).<br>2. Written justification is mandatory even for System Admin. | System Admin session active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-028 unlock requirement |
| MF02-TC-SEC-007 | Test direct API server-side gate enforcement for classroom capacity (bypassing UI). | 1. Send direct HTTP POST request bypassing UI form validation with `capacity = 55`. | 1. Server rejects request with HTTP 422 Unprocessable Entity (`SCH-010`, `MSG-010`).<br>2. Server-side validation enforced regardless of client UI controls. | API direct access. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | Server-side gate enforcement |
| MF02-TC-SEC-008 | Test external Supplier & Parent role rejection across all MF02 endpoints. | 1. Authenticate as Supplier (`role=supplier`) or Parent (`role=parent`).<br>2. Attempt calls to `/classrooms`, `/academic-years`, `/schools`. | 1. All endpoints return HTTP 403 Forbidden (`IAM-004`, `MSG-005`). | Supplier and Parent JWTs active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | OWASP A01 access control |
| **Injection Defense** | | | | | | | | | | | | | | |
| MF02-TC-SEC-010 | Test SQL injection defense in classroom and school search queries. | 1. Send SQL injection payloads (`' OR 1=1 --`) in `search` parameter of `GET /classrooms`. | 1. Query handles input safely via JPA Criteria Specification.<br>2. No 500 error, no unauthorized data returned. | Search Specification active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | OWASP A03 / CWE-89 |
