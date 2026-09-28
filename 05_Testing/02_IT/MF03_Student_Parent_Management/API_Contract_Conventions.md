# Integration Test Case — API Contract Conventions (MF03 Student & Parent Management)

---

## Feature: Pagination/Search Contract (GET /students), ISO-8601 Dates, and Client-Supplied ID

### 1. General Information

| Field | Value |
|---|---|
| Feature | API Contract Conventions |
| Test requirement | Verify the pagination/search/filter envelope contract on `GET /students` (5-field shape, clamping, empty-result 200, `SEARCH_FIELDS` scoping, derived-field filters), that every date/time field is ISO-8601, and the client-supplied-`id` contract on `POST /students` (duplicate id, server-generated id, business-code path resolution, concurrent generation). |
| Number of TCs | 21 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 21 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 21 | 0 |
| Round 3 | 0 | 0 | 21 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **GET /api/v1/students — pagination / search / filter contract (StudentListContractIT)** | | | | | | | | | | | | | | |
| MF03-TC-INT-009-01 | Response has exactly the 5-field pagination envelope, no `data`/`result` wrapper. | `GET /students?page=1&pageSize=8` (school-admin). | Response keys sorted equal exactly `["items","page","pageCount","pageSize","total"]`. | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `StudentListContractIT.responseHasExactlyFiveKeys` |
| MF03-TC-INT-009-02 | `page=99` clamps to the last page. | `GET /students?page=99&pageSize=8`. | `page == pageCount`. | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `StudentListContractIT.pageClampsToLastPage` |
| MF03-TC-INT-009-03 | Empty filter result is HTTP 200, never 404. | `GET /students?classroomId=KHONG-TON-TAI`. | `total=0, pageCount=1, page=1, items=[]`, HTTP 200. | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `StudentListContractIT.emptyResultReturns200NotFound` |
| MF03-TC-INT-009-04 | Invalid `pageSize` (0, -5) clamps to 1. | `GET /students?pageSize=0`; `GET /students?pageSize=-5`. | Both responses have `pageSize >= 1`. | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `StudentListContractIT.invalidPageSizeClampsToOne` |
| MF03-TC-INT-009-05 | Search matches only `SEARCH_FIELDS` (`students.name`, `students.code`, `classrooms.name`), not every string column. | `?search=Khang`, `?search=HS-3A-001`, `?search=3A`, `?search=DTD-2026-0001` (registration_no value). | First 3 match ≥1 row; the `registration_no`-only value matches 0 rows (no blanket OR-across-all-columns). | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `StudentListContractIT.searchOnlyMatchesSearchFields` |
| MF03-TC-INT-009-06 | Search is case-insensitive (`ILIKE`). | `?search=khang` vs `?search=KHANG`. | Identical `total`. | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `StudentListContractIT.searchIsCaseInsensitive` |
| MF03-TC-INT-009-07 | Derived-field filters (`healthStatus`, `not:healthStatus`, `hasAllergy`) match the `EXISTS` formula (ADR-M03-3). | `?healthStatus=VERIFIED`, `?not:healthStatus=VERIFIED`, `?hasAllergy=yes`. | `verified + notVerified == totalAll`; `hasAllergy=yes` returns ≥1. | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `StudentListContractIT.derivedFiltersMatchExistsFormula` |
| MF03-TC-INT-009-08 | Filter by `parentId`/`classroomId` keys (business code, not UUID). | `?parentId=U-9905`; `?classroomId=3A`. | `parentId` filter returns exactly 2 students; every `classroomId=3A` item has `classroomId == "3A"`. | Full seed (`FX-002`). | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `StudentListContractIT.filterByParentIdAndClassroomId` |
| MF03-TC-INT-009-09 | Empty filter value is treated as not-passed. | `?classroomId=` vs no param. | Identical `total`. | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `StudentListContractIT.emptyFilterValueIsIgnored` |
| MF03-TC-INT-009-10 | Control keys (`dateFrom`/`dateTo`/`mealPrefix`) are never interpreted as field filters. | `?dateFrom=2020-01-01&dateTo=2030-01-01&mealPrefix=x`. | HTTP 200, no 400/422 for the unknown-looking keys. | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `StudentListContractIT.controlKeysAreNotTreatedAsFieldFilters` |
| **All GET/POST endpoints — ISO-8601 date/time (DateFormatContractIT)** | | | | | | | | | | | | | | |
| MF03-TC-INT-010-01 | `dob` is ISO `yyyy-MM-dd`, never `dd/MM/yyyy`. | `GET /students/HS-3A-001`. | `dob` matches `^\d{4}-\d{2}-\d{2}$`. | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `DateFormatContractIT.dobIsIsoNotVietnameseFormat` |
| MF03-TC-INT-010-02 | `declaredAt`/`verifiedAt` are `Instant`-parsable ISO date-times. | `GET /allergy-declarations`. | Every `declaredAt` (and non-null `verifiedAt`) parses via `Instant.parse`. | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `DateFormatContractIT.declaredAtAndVerifiedAtAreInstantParsable` |
| MF03-TC-INT-010-03 | `health-history.at` is ISO date-time, timeline sorted `DESC`. | `GET /health-history?studentId=HS-3A-001`. | Every `at` ISO-parsable; sequence strictly non-increasing. | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `DateFormatContractIT.healthHistoryAtIsIsoAndSortedDescending` |
| MF03-TC-INT-010-04 | Locale-formatted `dob` (`dd/MM/yyyy`) is rejected, not leniently parsed. | `POST /students {"dob":"12/03/2019", …}`. | HTTP 422 `STU-422` + `MSG-138` verbatim (actual behavior of shared `GlobalExceptionHandler` — not HTTP 400 as an older TestSpec draft assumed). | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `DateFormatContractIT.nonIsoDobIsRejected` |
| MF03-TC-INT-010-05 | No `@JsonFormat` annotation anywhere in MF03 source. | `grep -rn "@JsonFormat" src/main/java/com/sfms/mf03/` (via `Files.walk`). | 0 matches. | None. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `DateFormatContractIT.noJsonFormatAnnotationInMf03Source` |
| **POST /api/v1/students — client-supplied id (PublicIdContractIT)** | | | | | | | | | | | | | | |
| MF03-TC-INT-011-01 | Client-supplied `id` (not existing) is used as the business code. | `POST /students {"id":"HS-2A-011", …}`. | HTTP 201, `response.id == "HS-2A-011"`. | Full seed + a `2A-PID` classroom. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `PublicIdContractIT.clientSuppliedIdIsUsedAsCode` |
| MF03-TC-INT-011-02 | Duplicate client-supplied `id` is rejected. | `POST /students {"id":"HS-3A-001", …}` (existing). | HTTP 409 `STU-012` + `MSG-012` verbatim. | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `PublicIdContractIT.duplicateClientSuppliedIdIsRejected` |
| MF03-TC-INT-011-03 | Absent `id` triggers server-generated business code, never a UUID. | `POST /students` with no `id`. | HTTP 201, `id` matches `^HS-3A-\d{3}$`, not a UUID. | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `PublicIdContractIT.serverGeneratesIdWhenAbsent` |
| MF03-TC-INT-011-04 | Path param resolves by business code, not internal UUID. | `GET /students/HS-3A-001` vs `GET /students/<internal UUID>`. | Business-code path → 200; UUID path → 404. | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `PublicIdContractIT.pathParamUsesBusinessCodeNotUuid` |
| MF03-TC-INT-011-05 | No response field — including FK fields `classroomId`/`parentId` — is a bare UUID (CF-M03-13). | `GET /students/HS-3A-001`; inspect `id`, `classroomId`, `parentId`. | None match the UUID regex. | Full seed. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `PublicIdContractIT.noFieldInResponseIsUuidIncludingForeignKeys` |
| MF03-TC-INT-011-06 | Concurrent code generation never collides. | 20 sequential `POST /students` with no `id` (documented as sequential — MockMvc does not support true concurrent dispatch; see Note). | 20 distinct codes generated. | Full seed + `5A-CONC` classroom. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `PublicIdContractIT.concurrentCreatesGenerateUniqueCodes` — Javadoc records that a genuine multi-threaded run against `MockMvc` silent-failed (2/20 valid); this case is a sequential proxy for the invariant, true concurrency is covered by a separate `common/code` unit test |

Note: `StudentListContractIT`/`DateFormatContractIT`/`PublicIdContractIT` collectively replace the 3 combined scenario rows of the previous version of this document — each row here now maps 1:1 to a real `@Test` method so failures can be traced directly to code.
