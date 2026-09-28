# Integration Test Case — Pagination, Error Envelope, Public IDs, Schema Invariants & Role-Shaped Contracts (MF10 Feedback & Announcement Management)

---

## Feature: Pagination Contract, Public-ID Contract & Error Envelope (D1/D2/D5/D7)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Pagination Contract, Public-ID Contract & Error Envelope (D1/D2/D5/D7) |
| Test requirement | Verify the 3 MF10 list resources honor the Team Plan §2b pagination contract, that no UUID ever leaks into a public `id`/`*Id` field, that all 8 MF10 error codes return the standard `{code,message,details}` envelope in English, and that client-supplied `id` is honored/deduplicated correctly (D5). |
| Number of TCs | 4 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 3 | 0 | 1 | 0 |
| Round 2 | 0 | 0 | 4 | 0 |
| Round 3 | 0 | 0 | 4 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Team Plan §2b Pagination Contract — 3 Resources** | | | | | | | | | | | | | | |
| MF10-TC-INT-008 | Test that `feedbacks`, `announcements`, `notifications` list endpoints all honor the shared pagination/search/filter contract: fixed 5-field shape, page clamping, empty-result-is-200, search restricted to `SEARCH_FIELDS`, exact and negative filters, and date-range filters. | For each of the 3 resources (seed 20/12/30 rows respectively): 1. `?page=1&pageSize=8`.<br>2. `?page=99&pageSize=8`.<br>3. `?search=zzz` (no match).<br>4. `?pageSize=0` and `?pageSize=-5`.<br>5. `?search=NGUOI` on feedback.<br>6. Exact filters: `?illnessKeyword=true`, `?rating=2`, `?status=PUBLISHED`, `?urgent=true`, `?mealType=LUNCH`.<br>7. Negative filter `?not:responded=true`.<br>8. `?dateFrom=2026-06-29&dateTo=2026-07-01`.<br>9. `?search=` and `?rating=` (empty values). | 1. JSON has exactly 5 keys: `items,total,page,pageSize,pageCount` — no wrapper.<br>2. `page` clamps to `pageCount`, returns the last page (not empty).<br>3. HTTP 200, `{items:[],total:0,page:1,pageSize:8,pageCount:1}` — NOT 404.<br>4. Clamps to 1.<br>5. Matches `comment` containing "nguội" (diacritic/case-insensitive); does not match text outside `SEARCH_FIELDS`.<br>6. All exact filters return the correct subset.<br>7. Negative filter correctly excludes.<br>8. Feedback filters on `meal_date`, announcement on `published_at`, notification on `sent_at`.<br>9. Empty query values are ignored, not applied as filters. | 20 feedback, 12 announcement, 30 notification rows seeded. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | Team Plan §2b-1..7; reusable across all 12 other modules |
| **17 Endpoints — `id` Is Always the Business Code, Never a UUID (D1)** | | | | | | | | | | | | | | |
| MF10-TC-INT-009 | Test that no response field of any MF10 endpoint ever contains a raw UUID, and that path params only accept the business `code`. | 1. Call all 17 §9.1 endpoints, collect the JSON responses.<br>2. Regex-scan for UUID patterns in any field.<br>3. Check `id` fields match `^FB-`/`^ANN-`/`^NTF-`/`^FBR-`.<br>4. Check FK fields use the target resource's `code`: `dishId ~ ^DISH-`, `studentId ~ ^HS-`, `feedbackId ~ ^FB-`.<br>5. Also scan for `\d{2}/\d{2}/\d{4}` date strings (D4, checked in the same pass).<br>6. `GET /api/v1/announcements/ANN-112` vs. `GET /api/v1/announcements/<UUID of ANN-112>`. | 1–2. No UUID pattern found anywhere.<br>3. Correct prefixes.<br>4. Correct FK prefixes.<br>5. No Vietnamese-format date string anywhere.<br>6. `code` path → 200; UUID path → 404 (UUID is not a second valid entry point). | Full seed data available across all 17 endpoints. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | D1, `review.md` A1 (blocker); D4 |
| **Error Envelope — 8 MF10 Error Codes, English Messages (D2/D7)** | | | | | | | | | | | | | | |
| MF10-TC-INT-010 | Test that every MF10 error condition returns the standard `{code,message,details}` envelope with an English message, and that officially-assigned MSG codes match `error_message.md` verbatim. | Trigger each of the 8 codes in turn: `FBA-004` (no JWT), `FBA-005` (wrong role calling `POST /announcements`), `FBA-022` (feedback outside 24h), `FBA-140` (`rating=2,tags=[]`), `FBA-REQ` (missing `mealSessionId`), `FBA-DUP` (duplicate submit), `FBA-STATE` (`PUBLISHED→DRAFT`), `FBA-404` (unknown code). For each: inspect the response body. | 1. Every response has exactly `error.code`, `error.message`, `error.details`.<br>2. `error.details` is always an array, possibly empty, never `null` and never an object.<br>3. `error.message` never matches Vietnamese diacritic regex `[àáâãèéêìíòóôõùúýăđĩũơưạ-ỹ]` (D7).<br>4. The 3 codes with official MSGs (`FBA-004/005/022`) match `error_message.md` MSG-004/005/022 verbatim.<br>5. The 5 local codes assert only on `code` (message content pending MSG-104+, T1.1). | `@RestControllerAdvice` wired for all 8 codes. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | D2, D7, L5, L6 |
| **POST /api/v1/feedbacks — Client-Supplied `id` (D5)** | | | | | | | | | | | | | | |
| MF10-TC-INT-011 | Test that a client-supplied `id` is used as the `code`, a duplicate `id` is rejected with 409, an omitted `id` is server-generated from `code_sequences`, and concurrent auto-generation never collides. | 1. `POST {id:"FB-990001", …}`.<br>2. `POST {id:"FB-990001", …}` again (different meal session, to avoid colliding with `uq_fb_once`).<br>3. `POST` with no `id`.<br>4. `POST {id:"<script>"}` and `{id:"FB-"+ 200 chars}`.<br>5. Fire 2 concurrent `POST`s with no `id`. | 1. HTTP 201, `response.id == "FB-990001"`, DB `code = 'FB-990001'`.<br>2. HTTP 409 (a single consistent code chosen — `FBA-409` or `FBA-DUP`).<br>3. HTTP 201, `response.id` matches `^FB-\d+`, generated from `code_sequences`.<br>4. HTTP 422 (invalid format / length > 32).<br>5. Two distinct generated codes, no collision (`INSERT … RETURNING`, not read-then-write). | `code_sequences` table migrated. | Pending | 2026-08-05 | Manh | Pending | | | Pending | | | D5; mock's `` `FB-${Date.now()}` `` is the real-world case D5 targets |

---

## Feature: Schema Invariants, Business-Rule Invariants & Audit Fields (D1/D4/D9, BR-derived)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Schema Invariants, Business-Rule Invariants & Audit Fields (D1/D4/D9, BR-derived) |
| Test requirement | Verify the Flyway-migrated schema has no derived columns, has every column `review.md` §H requires, and that 8 business invariants derived directly from BR-139/140/142/144/159 (in place of `semantic-audit.ts`, which has 0 rules touching MF10) always hold after a mix of operations. Also verify `created_by`/`updated_by` are filled by `AuditorAware`, distinct from the business field `author_user_id`/`responder_user_id`/`revised_by_user_id` (D9). |
| Number of TCs | 3 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 1 | 0 | 2 | 0 |
| Round 2 | 0 | 0 | 3 | 0 |
| Round 3 | 0 | 0 | 3 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Flyway Schema — No Derived Columns, All §H Columns Present** | | | | | | | | | | | | | | |
| MF10-TC-INT-015 | Test the migrated schema via `information_schema`: absence of derived columns, presence of the columns `review.md` §H requires, correct date types, required constraints/indexes, and the D9 audit-column split. | 1. **Assertion of premise:** confirm all 9 MF10 tables exist.<br>2. Query for absence of: `notifications.critical`, `announcements.revision_count`, `feedbacks.responded`, `feedbacks.editable_until`, `feedbacks.hours_remaining`.<br>3. Query for presence of: `feedbacks.meal_session_id` (NOT NULL), `feedbacks.dish_id`, `feedbacks.scope`, `feedbacks.portion_size`, `feedbacks.class_name`, `announcements.audience_codes` (jsonb), `announcements.audience_label`.<br>4. Query for `code VARCHAR(32)` + `UNIQUE(school_id, code)` on `feedbacks`/`announcements`/`notifications`.<br>5. Query column types for all timestamp columns.<br>6. Query for constraints: `ck_fb_low_rating_needs_tag`, `uq_fb_once`, `ck_ann_published_at`, `ck_ntf_roles`, `uq_ntfread`, RULEs `r_annrev_no_update`/`no_delete`.<br>7. Query for indexes: `ix_fb_school_date`, `ix_ann_audience` (GIN), `ix_fb_search` (trigram).<br>8. Query `feedbacks` for both `created_by`/`updated_by` (BaseEntity) and `submitter_user_id` (business field). | 1. 9 tables exist.<br>2. None of the 5 derived columns exist.<br>3. All 7 required columns exist.<br>4. `code` + unique constraint present on all 3 tables.<br>5. All date/time columns are `timestamptz`/`date`, never `varchar`.<br>6. All listed constraints/rules exist.<br>7. All listed indexes exist.<br>8. Both column groups present and distinct. | Flyway `V18`/`V19` fully applied on Testcontainers PostgreSQL 16. | Pending | 2026-08-05 | Manh | Pending | | | Pending | | | `01_MVP_Backend_Implementation_Plan.md` §3, `review.md` H4/H5/H7, D1 |
| **8 Business Invariants Derived Directly From BR (Replacing `semantic-audit.ts`)** | | | | | | | | | | | | | | |
| MF10-TC-INT-016 | Test 8 invariant SQL queries — each traced to a specific BR — all return 0 rows after seeding plus a controlled sequence of 20 business operations. | Run seed + 20 controlled operations, then run 8 invariant queries: INV-1 `submitted_at > servedAt+24h`; INV-2 Parent `rating<3` with empty `tags`; INV-3 comment matches `illness_keywords` but `illness_keyword=false`; INV-4 `illness_keyword=true` with no matching `ALLERGY_ALERT` notification; INV-5 `PUBLISHED` announcement with 0 revisions; INV-6 `PUBLISHED`/`published_at IS NULL` mismatch; INV-7 announcement with an empty `audience_codes` array; INV-8 `notification_deliveries.attempt > 3`, or `FALLBACK_EMAIL` for a user with `email_fallback=false`. | All 8 queries return 0 rows. | Seed + 20-operation scripted business sequence. | Pending | 2026-08-05 | Manh | Pending | | | Pending | | | BR-139/140/142/144/159; module has 0/54 `semantic-audit.ts` rules — this suite is the substitute (F7) |
| **BaseEntity Audit Columns — `created_by`/`updated_by` vs. Business "Who Did It" Fields (D9)** | | | | | | | | | | | | | | |
| MF10-TC-INT-017 | Test that `created_by`/`updated_by` are auto-filled by `AuditorAware` from the actor's email, remain distinct from business fields like `author_user_id`, and that the module never sets them manually. | 1. `school-admin` (`toanpq@dtd.edu.vn`) `POST /announcements`.<br>2. Inspect `author_user_id` vs. `created_by`.<br>3. `PATCH` by a different actor.<br>4. Static/ArchUnit scan of the MF10 package.<br>5. Run `WeeklyFeedbackDigestJob` outside an HTTP request context.<br>6. Inspect entity source. | 1. `created_by = 'toanpq@dtd.edu.vn'` — never `'system'` or `null` for a request-scoped write.<br>2. `author_user_id` is a UUID, a different column with a different meaning; both coexist.<br>3. `updated_by` changes; `created_by` unchanged.<br>4. No `setCreatedBy(`/`setUpdatedBy(` calls anywhere in `com.sfms.mf10`.<br>5. `created_by = 'system'`, no exception.<br>6. MF10 entities do not re-declare `createdBy`/`updatedBy` — inherited from `BaseEntity`. | `AuditorAwareConfig` wired. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | D9, L16 |

---

## Feature: Role-Shaped List Contracts & Write-DTO Field Filtering (D11/D12)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Role-Shaped List Contracts & Write-DTO Field Filtering (D11/D12) |
| Test requirement | Verify `GET /feedbacks` and `GET /announcements` change response shape by role and declare it via OpenAPI `oneOf` (D11), and that `PATCH` endpoints silently drop derived/wrong-typed fields instead of persisting them (D12) — the two newest module-completion conventions, written after the original 52 TestSpec test cases, so covered by neither. |
| Number of TCs | 2 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 2 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 2 | 0 |
| Round 3 | 0 | 0 | 2 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **GET /feedbacks & /announcements — `oneOf` Role-Shape Contract, No PII Leak (D11)** | | | | | | | | | | | | | | |
| MF10-TC-CON-001 | Test that list responses genuinely differ by role (owner sees identity, others get anonymized/collapsed shape) and that `/v3/api-docs` declares this via `oneOf` rather than an empty generic schema. | 1. Log in 4 real seeded accounts (`U-0005` parent, `U-0001` school-admin, `U-0003` service-caterers, `U-0004` teacher) via `POST /api/v1/auth/login` — NOT `@WithMockUser` (D13).<br>2. `GET /api/v1/feedbacks?page=1&pageSize=8` as parent.<br>3. Same path as school-admin and service-caterers.<br>4. `GET /api/v1/announcements?page=1&pageSize=5` as school-admin.<br>5. Same path as parent and teacher.<br>6. `GET /v3/api-docs`. | 1. 4 valid JWTs obtained.<br>2. Item HAS `studentId`, `student`.<br>3. Item has NONE of `studentId`, `student`, `submitterName`, `submitterUserId` — checked individually, not via one representative field.<br>4. Item HAS `revisionCount`; the returned set INCLUDES a `DRAFT` item.<br>5. Item has NO `revisionCount`; NO `DRAFT` item appears; parent does not see an announcement addressed only to `["TEACHER"]` (CF-M10-6).<br>6. `PagedResponseFeedbackListItem`/`PagedResponseAnnouncementListItem` exist; `items` declares `oneOf` with exactly 2 branches; `items` is not an empty `{}`; schema names retain the `PagedResponse` prefix (S7). | Container running; Flyway `V1`–`V19`; `V900__test_fixtures.sql` seeded; ≥1 feedback for `U-0005`; ≥1 `DRAFT` announcement + 1 `PUBLISHED` announcement addressed to `["TEACHER"]`. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | D11, BR-145, ADR-M10-5, S7; same class of bug as MF03 `/students` and MF09 `/student-distributions` |
| **PATCH /feedbacks & /announcements — Derived/Wrong-Type Fields Are Ignored, DB Unchanged (D12)** | | | | | | | | | | | | | | |
| MF10-TC-CON-002 | Test the Jackson "200 but nothing saved" trap: a PATCH sending derived fields (`illnessKeyword`, `submittedAt`, `student`, `dish`, `responded`) or a correctly-named-but-wrong-typed field (`audience` as a string instead of `audienceCodes` as an array) must not silently change the database, while a correctly-shaped PATCH must still work (control case). | 1. Snapshot both target rows (`select *`).<br>2. `PATCH /api/v1/feedbacks/{code}` as `U-0005` with `{"illnessKeyword":false,"submittedAt":"2020-01-01T00:00:00Z","student":"Tên Khác","dish":"Món Khác","responded":true}`.<br>3. Re-read the row.<br>4. `PATCH /api/v1/announcements/{code}` as `U-0001` with `{"audience":"Toàn trường"}`.<br>5. Re-read `audience_codes`/`audience_label`.<br>6. `PATCH` the same announcement with `{"audienceCodes":["PARENT_ALL"]}` (control case — proves the write path itself works). | 1. Snapshot captured.<br>2. HTTP 200 or 422 (either acceptable — what's forbidden is 200 WITH a DB change).<br>3. No column changed except `updated_at`/`updated_by`; specifically `illness_keyword` and `submitted_at` unchanged (L9).<br>4. HTTP 200 or 422.<br>5. `audience_codes` still `["TEACHER"]`, `audience_label` unchanged — the string `audience` field is not the same as the array `audienceCodes`.<br>6. DB DOES change and `audience_label` is recomputed — proves the endpoint can write when given the right shape; without this step the test would be "blind" and steps 3/5 would be meaningless. | A `feedbacks` row of `U-0005` still inside the 24h window (otherwise `FBA-022` masks the result — a fixture trap specific to MF10); a `PUBLISHED` `announcements` row of `U-0001` with `audience_codes=["TEACHER"]`. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | D12; same blocker class as `review.md` H3 — now locked at the write-contract layer too; step 6 is mandatory |
