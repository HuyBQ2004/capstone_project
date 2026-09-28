# Integration Test Cases — MF08 Food Sample Retention
## Feature: Listing/Pagination Contract, Public Identifiers, Error Envelope, Client-Supplied ID (UC-118)

> Stack: Testcontainers PostgreSQL 16 + real Flyway migration. Timeout 120s.
> Traceability: `TestSpec-MF08_Food_Sample_Retention.md` §4 — `MF08-TC-INT-007`…`MF08-TC-INT-010`

---

## Feature: Listing/Pagination Contract, Public Identifiers, Error Envelope, Client-Supplied ID

### 1. General Information

| Field | Value |
|---|---|
| Feature | Cross-cutting API contract for MF08's 3 list resources (`/samples`, `/sample-lab-requests`, `/receiving-authorities`) — pagination shape, public business-code identifiers (D1), error envelope (D2/D7), and client-supplied `id` handling (D5) |
| Test requirement | Team Plan §2b pagination contract must hold on all 3 resources. `id` in every response must be the business code, never a UUID (D1). Every 4xx/5xx must return the `{error:{code,message,details}}` envelope with English messages (D2/D7). A client-supplied `id` on `POST /samples` becomes the `code`, with 409 on collision (D5). |
| Number of TCs | 14 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | `<#>` | `<#>` | 14 | `<#>` |
| Round 2 | `<#>` | `<#>` | `<#>` | `<#>` |
| Round 3 | `<#>` | `<#>` | `<#>` | `<#>` |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Function: GET /api/v1/samples — Pagination contract (Team Plan §2b)** | | | | | | | | | | | | | | |
| `MF08-TC-INT-007a` | Standard page returns the 5-field paged shape. | `GET /samples?page=1&pageSize=8`. | **200**; body has exactly the 5 fields `items/total/page/pageSize/pageCount`, no extra wrapper. | Seed FX-002…FX-006. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-007b` | Requesting a page beyond the last one clamps instead of 404ing. | `GET /samples?page=99&pageSize=8`. | `page == pageCount` (clamped), **not** 404. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-007c` | A filter combination with zero matches returns 200 with an empty list. | `GET /samples?status=DISPOSED&dateFrom=2030-01-01`. | **200**; `{items:[], total:0, page:1, pageSize:8, pageCount:1}` — `pageCount ≥ 1` even when empty. | — | Pending | | | Pending | | | Pending | | | The mock frontend currently shows a blank page instead of "no results" on this exact case. |
| `MF08-TC-INT-007d` | `pageSize=0` clamps to the minimum. | `GET /samples?pageSize=0`. | `pageSize` clamps to **1**. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-007e` | Date range filters on `collected_at`, per §2b-5. | `GET /samples?dateFrom=2026-07-06&dateTo=2026-07-06`. | Returns exactly the 4 lunch samples collected on 06/07. | Seed includes 4 samples collected on 2026-07-06. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-007f` | Meal-type filter uses the D6 enum value, not the legacy `mealPrefix`. | `GET /samples?mealType=LUNCH`. | Returns only `LUNCH` samples. | — | Pending | | | Pending | | | Pending | | | Replaces the mock's `mealPrefix` string-prefix filter. |
| `MF08-TC-INT-007g` | `search` only matches the declared `SEARCH_FIELDS`, not arbitrary columns. | `GET /samples?search=inox` (a container description string). | `total == 0` — `container` is not part of `SEARCH_FIELDS`. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-007h` | `search` matches `collected_by`, exposed to the client as `collectedBy`. | `GET /samples?search=Mai`. | Returns the sample collected by "Lê Thị Mai". | — | Pending | | | Pending | | | Pending | | | Confirms the field-name mapping from finding F7. |
| `MF08-TC-INT-007i` | Negated filter syntax works. | `GET /samples?not:status=DISPOSED`. | Returns only non-`DISPOSED` samples, per §2b-2. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-007j` | Empty filter values are ignored, not turned into a predicate. | `GET /samples?dishName=x&status=`. | The empty `status=` parameter generates no predicate; result reflects only `dishName=x`. | — | Pending | | | Pending | | | Pending | | | |
| **Function: Public identifiers are business codes, not UUIDs (D1)** | | | | | | | | | | | | | | |
| `MF08-TC-INT-008a` | Detail lookup by business code succeeds. | `GET /api/v1/samples/SMP-20260706-01`. | **200**. | Seed FX-002. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-008b` | Detail lookup by the internal UUID fails. | `GET /api/v1/samples/{internal-uuid-of-same-row}`. | **404** — UUID is not a valid public identifier. | — | Pending | | | Pending | | | Pending | | | If this ever returns 200, every frontend detail screen for MF08 that calls `useDetail("samples", code)` breaks the moment the `baseURL` changes. |
| `MF08-TC-INT-008c` | No UUID pattern appears anywhere in any read-endpoint response body. | Scan all 6 read endpoints' JSON for the UUID regex `[0-9a-f]{8}-[0-9a-f]{4}-...`. | **0 matches**. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-008d` | Foreign keys in JSON are business codes. | Inspect `incidentId`, `labRequestId`, `dishId`, `inspectionId` in a detail response. | All 4 are business codes (`INC-2026-014`, `LABREQ-2026-001`, dish code, inspection code) — none are UUIDs. | — | Pending | | | Pending | | | Pending | | | |
| **Function: Error envelope and English messages (D2/D7)** | | | | | | | | | | | | | | |
| `MF08-TC-INT-009a` | Every MF08 error response has the full `{error:{code,message,details}}` shape with a non-null `details` array. | Trigger 12 MF08 error codes (`SMP-004`, `SMP-005`, `SMP-019`, `SMP-030`, `SMP-105`, `SMP-107`, `SMP-109`, `SMP-111`, `SMP-112`, `SMP-113`, `SMP-203`, `SMP-212`) in turn. | Every response has `code`, `message`, `details`; `details` is `[]` when empty — **never `null`**. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-009b` | Codes with an officially assigned MSG match it byte-for-byte in English. | Assert message text for `SMP-004`, `SMP-005`, `SMP-019`, and the BR-derived codes against `error_message.md`. | Message text matches the English source exactly; regex for Vietnamese diacritics does not match. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-009c` | `SMP-020` (fridge temperature) is a warning, not a hard error. | Register a sample with `fridgeTemp` outside 2–8°C. | **200**, with the warning surfaced in a `warnings[]` array — **not** in the `error` object. | — | Pending | | | Pending | | | Pending | | | |
| **Function: Client-supplied `id` on POST /api/v1/samples (D5)** | | | | | | | | | | | | | | |
| `MF08-TC-INT-010a` | Client supplies a valid, unused `id`. | `POST /samples` with `"id": "SMP-20260707-99"`. | **201**; `response.id == "SMP-20260707-99"`; DB `code` stores that exact value. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-010b` | Client supplies an `id` that already exists. | `POST /samples` again with the same `"id": "SMP-20260707-99"`. | **409 `SMP-409`**; no second record created. | `INT-010a` executed. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-010c` | Client omits `id`; server generates one. | `POST /samples` with no `id` field. | **201**; `code` matches `^SMP-\d{8}-\d{2}$` and is unique per `(school_id, code)`. | — | Pending | | | Pending | | | Pending | | | If the server-generated code differs from what the client expected, the frontend's immediate `router.push` after submit lands on a 404 — the exact reason D5 exists. |
| `MF08-TC-INT-010d` | Client supplies a malformed `id`. | `POST /samples` with `"id": "../../etc"` or a 200-character string. | **422** — the code must match the expected pattern, not accept arbitrary strings. | — | Pending | | | Pending | | | Pending | | | |

---

## Filling Guide

1. Group headers correspond to distinct contract concerns; rows map to the scenario tables inside `MF08-TC-INT-007` through `MF08-TC-INT-010` of the TDD test specification.
2. §2b pagination assertions in the first group are structurally identical across almost every module's list endpoint — reuse this file's shape when writing the equivalent suite for other modules.
3. Update Round columns and **Note** as the suite executes.
