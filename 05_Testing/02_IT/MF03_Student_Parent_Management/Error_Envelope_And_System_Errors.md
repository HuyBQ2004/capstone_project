# Integration Test Case — Error Envelope & System Error Codes (MF03 Student & Parent Management)

---

## Feature: Uniform Error Envelope Across Error Paths (D2/D7)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Error Envelope & System Error Codes |
| Test requirement | Verify that every error path returns the shared envelope `{ "error": { code, message, details } }` (D2) with `details` always an array (never `null`), no flat `$.message` (mock-style shape), and every message is verbatim English with no Vietnamese diacritics (D7). Covers 13 of the 14 `STU-*` codes in TDS §10 that can be triggered through real HTTP calls without mocking internals (`STU-500` requires forcing an unmocked `DataAccessException`, not implemented in this `@SpringBootTest` class — recorded as not covered, not simulated). |
| Number of TCs | 13 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 13 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 13 | 0 |
| Round 3 | 0 | 0 | 13 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **@RestControllerAdvice — error envelope per code** | | | | | | | | | | | | | | |
| MF03-TC-INT-013-01 | No JWT → 401, actual error code is `IAM-005`, not `STU-004` (auth runs in the Spring Security filter chain, before the request reaches any MF03 controller, so the shared `iam/security/RestAuthEntryPoint` has no module context to emit `STU-004`). | `GET /students` with no `Authorization` header. | HTTP 401, `error.code == "IAM-005"`, `error.message == "Session expired. Please log in again."`, full D2 envelope. | None. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `ErrorEnvelopeIT.stu004NoJwt` — **doc correction**: previous version of this file asserted code `STU-004`; the real code is `IAM-005` (confirmed in test Javadoc, cross-referenced with MF01-TC-SEC-002) |
| MF03-TC-INT-013-02 | Parent touches an unlinked student → 403. | `GET /students/HS-3A-005` (JWT parent U-9905, not linked). | HTTP 403, `STU-005`, `"Access denied: You do not have permission to access this resource."` | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `ErrorEnvelopeIT.stu005AccessDenied` |
| MF03-TC-INT-013-03 | Classroom at its own capacity → 409. | `POST /students` targeting a classroom seeded at `capacity=1`, already full. | HTTP 409, `STU-010`, `"This classroom has reached its capacity."` | Seeded 1-capacity classroom. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `ErrorEnvelopeIT.stu010ClassroomFull` |
| MF03-TC-INT-013-04 | Duplicate `id` on create → 409. | `POST /students {"id":"HS-3A-001", …}`. | HTTP 409, `STU-012`, `"Student admission code is invalid or has already been used."` | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `ErrorEnvelopeIT.stu012DuplicateCode` |
| MF03-TC-INT-013-05 | Create student with no parent → 422. | `POST /students` with no `parent` field. | HTTP 422, `STU-030`, `"Each student must be linked to at least one parent account."` | None. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `ErrorEnvelopeIT.stu030MissingParent` |
| MF03-TC-INT-013-06 | Verify SEVERE declaration with no proof → 422. | `POST /allergy-declarations/{code}/verify` on a SEVERE, `proof_file_id=NULL` declaration. | HTTP 422, `STU-031`, `"Severe allergy declaration requires supporting medical documentation."` | `allergen='Đậu nành'` forced to SEVERE/no-proof. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `ErrorEnvelopeIT.stu031SevereNeedsProof` |
| MF03-TC-INT-013-07 | Request-info with no reason → 422. | `POST /allergy-declarations/{id}/request-info {"reason":""}`. | HTTP 422, `STU-032`, `"A reason is required for this action."` | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `ErrorEnvelopeIT.stu032ReasonRequired` |
| MF03-TC-INT-013-08 | Approve off-catalog diet (`Keto`) → 422, `NEEDS_INFO` (BR-220). | `POST /special-meals/{code}/approve` on a `Keto` PENDING request. | HTTP 422, `STU-066`, `"This dietary type is outside the standard list and requires medical justification before approval."` | Seeded `Keto` SMR. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `ErrorEnvelopeIT.stu066DietNeedsJustification` |
| MF03-TC-INT-013-09 | Unknown field on `PATCH .../health-file` → 400. | `PATCH /students/HS-3A-001/health-file {"allergies":[]}`. | HTTP 400, `STU-094`, `"This field cannot be edited through this endpoint."` | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `ErrorEnvelopeIT.stu094UnknownField` |
| MF03-TC-INT-013-10 | `PUT /health-history/{id}` → 405 (append-only). | `PUT /health-history/{id} {}`. | HTTP 405, `STU-172`, `"Health history is append-only and cannot be modified or deleted."` | Seeded history event. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `ErrorEnvelopeIT.stu172AppendOnly` |
| MF03-TC-INT-013-11 | Classroom in a `CLOSED`/read-only academic year → 409. | `POST /students` targeting a `read_only=true` classroom. | HTTP 409, `STU-193`; message source is missing in TDS §10 — asserted by `code` + `details` presence only, not by verbatim message (flagged for follow-up). | Seeded read-only classroom. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `ErrorEnvelopeIT.stu193ClassroomReadOnly` |
| MF03-TC-INT-013-12 | 6th parent link → 409. | `POST /students` for a parent already linked to 5 students. | HTTP 409, `STU-201`, `"This parent account is already linked to 5 students. School Administration verification is required."` | Seeded parent with 5 links. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `ErrorEnvelopeIT.stu201ParentLinkLimit` |
| MF03-TC-INT-013-13 | Non-existent `studentId` → 404. | `GET /students/HS-KHONG-CO`. | HTTP 404, `STU-404`, `"Resource not found."` | None. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `ErrorEnvelopeIT.stu404NotFound` |

Note: every row above also asserts the shared envelope invariants (`error.details` is an array, never `null`; no flat `$.message`; message contains no Vietnamese diacritics — D7) inside the same test method via the shared `assertEnvelope()` helper. `STU-500` (HTTP 500, unmocked `DataAccessException`) is the only TDS §10 code with no integration coverage in this class — not simulated, not asserted, explicitly out of scope per the class Javadoc. This document previously described this suite as a single `@ParameterizedTest` covering 14 rows in one method (`MF03-TC-INT-013`); the real code is 13 separate `@Test` methods covering 13 codes — corrected here to map 1:1 with `ErrorEnvelopeIT.java`.
