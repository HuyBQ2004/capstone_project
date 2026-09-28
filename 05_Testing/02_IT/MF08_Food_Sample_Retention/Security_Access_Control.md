# Integration Test Cases — MF08 Food Sample Retention
## Feature: Security & Access Control

> Stack: Testcontainers PostgreSQL 16 + real Flyway migration. Timeout 120s.
> Traceability: `TestSpec-MF08_Food_Sample_Retention.md` §4 — `MF08-TC-SEC-001`…`MF08-TC-SEC-009`

---

## Feature: Security & Access Control

### 1. General Information

| Field | Value |
|---|---|
| Feature | RBAC across all MF08 endpoints, school-level tenant isolation, append-only evidence, upload content validation, unauthenticated access, information leakage, OpenAPI contract, and role field-parity |
| Test requirement | Food sample retention records are legal evidence for food-safety inspections (QĐ 1246/QĐ-BYT) and must never be alterable, exposed cross-school, or accessible by an unauthorized role. Only Medical Staff may physically hand a sample to a lab (BR-113, amended 2026-07-26, replacing the deprecated BR-215 process). |
| Number of TCs | 9 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 5 | 0 | 11 | 0 |
| Round 2 | `<#>` | `<#>` | `<#>` | `<#>` |
| Round 3 | `<#>` | `<#>` | `<#>` | `<#>` |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Function: Service Caterers cannot hand samples to a lab (BR-113, OWASP A01:2021, CWE-863)** | | | | | | | | | | | | | | |
| `MF08-TC-SEC-001` | A caterer account attempts to submit a fully valid lab hand-over request. | Log in as Service Caterers (correct school, valid samples — only the role is wrong). `POST /api/v1/sample-lab-requests` with an otherwise-valid payload. | **HTTP 403** with `error.code = "SMP-005"` — not 404, not 422. No request created; no sample changes status. The legacy `PUT /samples/{code}` status-override route also returns 403/405. | Caterer JWT (FX-014); an open incident with 2 `RETAINED` samples. | Passed | 2026-08-12 | Manh | Pending | | | Pending | | | The mock frontend still has a `LabRequestTasks` block for caterers — this test intentionally contradicts that mock; the block must be removed from the UI. Allowing this would recreate the sample-swap risk the 2026-07-26 BR-113 amendment was written to close. |
| **Function: RBAC matrix across all 9 MF08 endpoints (OWASP A01:2021)** | | | | | | | | | | | | | | |
| `MF08-TC-SEC-002` | Every (endpoint, role) combination returns the correct authorization outcome. | Parameterized run of 9 endpoints × 7 roles (System Admin, School Admin, Medical Staff, Caterer, Teacher, Parent, Supplier). | 63 combinations match the TDS §16 authorization matrix — e.g. `POST /samples` = 201 for Caterer only; `POST /sample-lab-requests` = 201 for Medical Staff only; `POST /samples/{code}/incident-link` = 200 for Medical Staff only; reads = 200 for School Admin/Medical/Caterer per resource. Every denial returns 403 with `error.code = "SMP-005"`. | — | Passed | 2026-08-12 | Manh | Pending | | | Pending | | | Watch for the "false green" trap: an endpoint that doesn't exist yet returns 404, not 403 — assert the exact status **and** `error.code`, never just "not 2xx". |
| **Function: School-level tenant isolation (BR-007/BR-022, CWE-639)** | | | | | | | | | | | | | | |
| `MF08-TC-SEC-003a` | Cross-school detail lookup is denied or hidden. | Medical Staff JWT for school B, sample belongs to school A. `GET /api/v1/samples/SMP-20260706-01`. | **404** (existence hidden) or **403 `SMP-005`** — pick one behavior and keep it consistent across the module. | JWT for school B (FX-016); sample owned by school A. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-SEC-003b` | A `schoolId` query parameter cannot be used to view another school's data. | `GET /api/v1/samples?schoolId=SCH-HN-0042` while authenticated for a different school. | **200** but `items` is **empty** — `schoolId` is not an accepted filter parameter; the school always comes from the JWT. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-SEC-003c` | A `schoolId` in the request body cannot override the JWT's school on write. | `POST /api/v1/samples` with `"schoolId": "SCH-HN-0042"` from a caterer JWT for a different school. | The saved row's `school_id` = the JWT's school, not the body value. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-SEC-003d` | A hand-over batch cannot span two schools. | `POST /sample-lab-requests` with samples belonging to 2 different schools. | **409/422** — a single hand-over request cannot mix samples from different schools. | — | Pending | | | Pending | | | Pending | | | A cross-school leak here would expose one school's food-safety compliance data to another school's medical staff, and a "valid" hand-over request could point at a box physically located at a different school. |
| **Function: Append-only evidence and retention records (OWASP A08:2021, CWE-284)** | | | | | | | | | | | | | | |
| `MF08-TC-SEC-004` | No role can delete, overwrite, or double-dispose a sample or its attachments. | For every role: (1) `DELETE /samples/{code}`, (2) `DELETE /samples/{code}/attachments/{id}`, (3) `PUT /samples/{code}/attachments/{id}` (swap the file), (4) `POST /samples/{code}/dispose` a **second** time on an already-`DISPOSED` sample. | (1)–(3): **405** (no such endpoint) or **403**. (4): **409** (blocked by the FSM). After all 4 attempts, `food_samples` and `sample_attachments` row counts are unchanged, and the attachment's `checksum_sha256` is untouched. | — | Passed | 2026-08-12 | Manh | Pending | | | Pending | | | Mẫu số 5 is a register that must be produced on inspection — any deletion path is effectively a way to erase compliance evidence after an incident. |
| **Function: Upload content validation, not trust-the-extension (OWASP A03/A08, CWE-434)** | | | | | | | | | | | | | | |
| `MF08-TC-SEC-005a` | An executable disguised with a `.jpg` extension is rejected by magic-byte inspection. | Upload `fake.jpg` whose actual content is a PE executable (`MZ` header). | **422 MSG-009** — rejected by magic bytes, not by file extension. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-SEC-005b` | Oversized upload is rejected. | Upload a 12 MB `.jpg`. | **422 MSG-008**. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-SEC-005c` | An otherwise-valid image with embedded script content is stored but never executed. | Upload a valid `.jpg` with `<?php ... ?>` inside an EXIF comment. | Stored successfully (it is a valid image); the payload is never executed anywhere; the response returns a sanitized file name. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-SEC-005d` | Path traversal via file name is impossible. | Upload with a file name of `../../etc/passwd.jpg`. | Storage key is derived from the **business code** (`"samples/SMP-...-01.jpg"`, per D10) — the user-supplied file name is never concatenated into the storage path. | — | Pending | | | Pending | | | Pending | | | Trusting the extension would let an executable end up inside `./data/uploads`, which is served through `/media/**`. |
| **Function: Unauthenticated access (OWASP A07:2021)** | | | | | | | | | | | | | | |
| `MF08-TC-SEC-006` | No MF08 endpoint is reachable without a valid session. | Call all 9 MF08 endpoints with (1) no `Authorization` header, (2) an expired token, (3) a token with an invalid signature. | All 3 cases return **401** with `error.code = "SMP-004"`, English message matching MSG-004 verbatim. No MF08 endpoint is public. | — | Passed | 2026-08-12 | Manh | Pending | | | Pending | | | Reading the retention log without authentication would expose kitchen GPS coordinates, staff names, and the full meal schedule. |
| **Function: No internal storage path leakage (OWASP A01:2021, CWE-200)** | | | | | | | | | | | | | | |
| `MF08-TC-SEC-007a` | Attachment JSON never exposes the storage key or checksum. | `GET /samples/{code}` for a sample with an attachment. | Response contains only `{name, uploadedAt, size}` — no `storageKey`, no `checksumSha256`, no absolute path. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-SEC-007b` | Debug-level logs never contain storage internals or bearer tokens. | Run the suite at `DEBUG` log level; grep logs for `data/uploads`, `r2.cloudflarestorage`, `Bearer `. | **0 matches**. | — | Pending | | | Pending | | | Pending | | | Leaking `storage_key` would let someone guess other evidence photos' keys (`samples/SMP-2026....jpg` is a predictable pattern) and fetch them directly via `/media/**`. |
| **Function: OpenAPI contract at /v3/api-docs (D8 — gated on approval)** | | | | | | | | | | | | | | |
| `MF08-TC-SEC-008` | Generated OpenAPI spec meets the D8 contract rules for MF08's 3 resource groups. | Fetch `/v3/api-docs`, filter to `/api/v1/samples`, `/api/v1/sample-lab-requests`, `/api/v1/receiving-authorities`. | No `id` field declares `format: uuid`; every 4xx/5xx response schema points to `ErrorResponse`; every enum matches the D6 table; every `@Operation.description` cites its UC ID + BR ID(s); every date field declares `format: date-time` with an ISO example. | D8 approved by the conventions owner. | Pending | | | Pending | | | Pending | | | Currently `@Disabled` pending D8 sign-off — do not mark this "Passed" until the annotation gate is lifted. |
| **Function: Role field-parity on list responses (D11)** | | | | | | | | | | | | | | |
| `MF08-TC-SEC-009` | System Admin, Medical Staff, and Caterer all see the identical field set on `GET /samples`, differing only by row scope. | `GET /api/v1/samples?page=1&pageSize=8` as 3 different roles, same school. Compare the JSON key set (names **and** types) of `items[0]` across all 3 responses. | All 3 key sets are identical; `total` is identical (same row scope, no hidden per-role filtering); `/v3/api-docs` schema name for this endpoint is `PagedResponseFoodSampleResponse` with `items` typed as `FoodSampleResponse[]` — not `{}` — and contains no `oneOf`. | 3 JWTs for the same school, different roles (FX-017). | Passed | 2026-08-12 | Manh | Pending | | | Pending | | | This is a regression guard, not a currently-failing case: MF08 deliberately does **not** use `oneOf` because role differences are row-level (via `schoolId`), not field-level. The test exists so that if someone later adds a role-specific field-trimming branch without declaring `oneOf`, it fails loudly instead of silently shipping an undocumented per-role response shape. |

---

## Filling Guide

1. Each `Function` group corresponds to one security control area from TDS §16 / the RBAC matrix; rows map to the attack-simulation steps inside `MF08-TC-SEC-001` through `MF08-TC-SEC-009`.
2. `MF08-TC-SEC-008` should remain `N/A`/blocked in the round summary until the D8 convention is formally approved — do not force a pass.
3. Update Round columns and **Note** as the suite executes; the Note column carries the specific attack scenario or compliance consequence being defended against.
