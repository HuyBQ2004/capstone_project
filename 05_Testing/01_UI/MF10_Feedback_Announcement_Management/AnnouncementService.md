# Unit Test Case — AnnouncementService (MF10 Feedback & Announcement Management)

---

## Function: `AnnouncementService.create() / update()` — revision append-only (BR-144)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-010` | | Function Name | `AnnouncementService.create` / `AnnouncementService.update` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 36 | | Lack of test cases | 0 |
| Test requirement | Every create/update of an announcement must append exactly one revision entry with a timestamp and the acting user's ID (BR-144, UC-089 Normal Flow step 5). A status-only change (DRAFT→PUBLISHED) also counts as an update. Source: MF10-TC-010. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 3 | 1 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Operation | `create(cmd)` | O | | | |
| | `update(code, cmd with revisionNote="Bổ sung bữa phụ thứ Năm")` | | O | | |
| | `update(code, cmd WITHOUT revisionNote)` | | | O | |
| | `update(code, cmd that only changes status DRAFT→PUBLISHED)` | | | | O |
| **Confirm** | | | | | |
| `revisions.size()` | `1`, note = `"Công bố lần đầu"` (matches mock's display contract) | O | | | |
| | `2`, last entry note matches the supplied `revisionNote` | | O | | |
| | `3`, default note `"Cập nhật nội dung"` (matches mock) | | | O | |
| | `4` — status transition alone still generates a revision | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | N | N | A |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

Note: If UTCID04 fails to generate a revision, the publication timestamp — the most legally significant moment for parents — leaves no trace. The mock currently does `Object.assign(row, fields)` and only then pushes a revision, which invites the misconception that only "content edits" count.

---

## Function: `AnnouncementService.update()` — revision records the real actor and server clock (BR-144)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-011` | | Function Name | `AnnouncementService.update` (revision authorship) |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 18 | | Lack of test cases | 0 |
| Test requirement | Revision `revisedByUserId`/`revisedByName`/`revisedAt` must come from the JWT actor and the server clock, never from the request body (BR-144, C2, D4). Source: MF10-TC-011. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 1 | 0 | 1 | 0 | 1 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Request | `update(code, {editor:"Người khác", revisedAt:"2020-01-01T00:00:00Z", ...}, actor=FX-018 school-admin)` — body tries to spoof both fields | O |
| **Confirm** | | |
| Return | `revision.revisedByUserId == actor.userId()` (not the `editor` value in the body) | O |
| | `revision.revisedByName == actor.fullName()` | O |
| | `revision.revisedAt == FX-001` (server clock, not `2020-01-01`) | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | A |
| Passed/Failed | Untested |
| Executed Date | 2026-08-05 |
| Defect ID | |

Note: The mock takes `editor` straight from the request body (`mock-server.ts:173`, default `"Phạm Quốc Toản (BGH)"`). Carrying that behavior into the backend would let anyone forge the revision-history author on a document sent to thousands of parents — the single highest non-repudiation risk in this module.

---

## Function: `AnnouncementStatus` transition guard — publish is irreversible

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-012` | | Function Name | `AnnouncementService.update` (status FSM guard) |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 24 | | Lack of test cases | 0 |
| Test requirement | `PUBLISHED → DRAFT` is forbidden (TDS §6.5 invariant — once parents have received it, it cannot be "un-published"; corrections must go through a new announcement, preserving the BR-144 trail). Error code `FBA-STATE`. Source: MF10-TC-012. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 6 | 4 | 1 | 1 | 6 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | |
| Transition | *(new)* → `DRAFT` | O | | | | | |
| | *(new)* → `PUBLISHED` | | O | | | | |
| | `DRAFT` → `PUBLISHED` | | | O | | | |
| | `DRAFT` → `DRAFT` (content edit only) | | | | O | | |
| | `PUBLISHED` → `PUBLISHED` (content edit only) | | | | | O | |
| | `PUBLISHED` → `DRAFT` | | | | | | O |
| **Confirm** | | | | | | | |
| Return | `publishedAt = null` | O | | | O | | |
| | `publishedAt = now()` (MSG-047), notification dispatched | | O | O | | | |
| | `publishedAt` unchanged from its original value (does NOT drift on re-save) | | | | | O | |
| Exception | `409 FBA-STATE` | | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|
| Type (N/A/B) | N | N | N | N | B | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | |

Note: If UTCID05 re-stamps `publishedAt` on every edit, the publication date silently drifts every time a typo is fixed — parents were notified on 27/06 but the system would record 02/08. This is a silent data-correctness bug with no exception and no log line. There is no `DELETE` endpoint for announcements (TDS §6.5) — do not add one "for convenience".

---

## Function: `AnnouncementMapper` — `publishedAt` is `null` on DRAFT, never an empty string

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-013` | | Function Name | `AnnouncementMapper.toDetailResponse` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 16 | | Lack of test cases | 0 |
| Test requirement | `publishedAt` must serialize as JSON `null` for DRAFT and a valid ISO-8601 instant for PUBLISHED — never `""` (L14, `review.md` H8: mock's `todayStamp()`/`""` bug; D4; CHECK `ck_ann_published_at`). Source: MF10-TC-013. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 2 | 2 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Operation | `create({status:"DRAFT"})` → serialize | O | | | |
| | `create({status:"PUBLISHED"})` → serialize | | O | | |
| | Direct repository `save(status=DRAFT, publishedAt=now())` | | | O | |
| | Direct repository `save(status=PUBLISHED, publishedAt=null)` | | | | O |
| **Confirm** | | | | | |
| Return | `json.get("publishedAt").isNull()` — NOT `""` | O | | | |
| | `publishedAt` matches ISO-8601 regex; does NOT match `\d{2}/\d{2}/\d{4}` | | O | | |
| Exception | `ck_ann_published_at` CHECK constraint violation | | | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | N | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

Note: Returning `""` lets a truthy-check on the FE pass while `formatVnDateTime("")` later throws or shows "Invalid Date" once `lib/format.ts` is added (T3.2) — the bug surfaces after the cutover, far from its cause.

---

## Function: `AnnouncementService.create()` / `AnnouncementSpec` — `audienceCodes` filtering vs. `audienceLabel` display (L12/H7)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-014` | | Function Name | `AnnouncementService.create` / `AnnouncementSpec.filterByAudience` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 28 | | Lack of test cases | 0 |
| Test requirement | Announcements must be filtered by machine-readable `audienceCodes`, with `audienceLabel` reserved purely for display (`review.md` H7 — blocker: "without a code, every role sees every announcement"; TDS C9). At least one audience code is mandatory (UC-017 EX1). Source: MF10-TC-024. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 2 | 2 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Request | `create({audienceCodes:["PARENT_GRADE_3","TEACHER"]})` | O | | | |
| | `create({audienceCodes:[]})` | | O | | |
| | `create({audience:"Phụ huynh khối 3"})` — free-text only, no code | | | O | |
| | `spec(audienceCodes contains "TEACHER")` | | | | O |
| **Confirm** | | | | | |
| Return | `audienceLabel` generated as `"Phụ huynh khối 3, Giáo viên"` (matches mock's `join(", ")`) | O | | | |
| | Only announcements whose array contains `TEACHER` are returned, via JSONB `@>` operator, NOT `LIKE` on `audience_label` | | | | O |
| Exception | `FBA-REQ` — at least one audience code is required (rejects the mock default `"Toàn trường"`) | | O | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | A | A | N |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

Note: Defaulting to `"Toàn trường"` when a code is missing means every incompletely-composed announcement — including internal ones meant only for Medical Staff — broadcasts to the entire school. Filtering with `LIKE` on the label would make `"Phụ huynh khối 1"` match `"Phụ huynh khối 10"` once the school adds more grades.
