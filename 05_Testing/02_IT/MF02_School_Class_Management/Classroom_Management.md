# Integration Test Case — Classroom Management (MF02 School & Class Management)

---

## Feature: Classroom Management & Search

### 1. General Information

| Field | Value |
|---|---|
| Feature | Classroom Management & Search |
| Test requirement | Verify classroom creation, homeroom teacher assignment audit trails (BR-020, BR-026), 5-field pagination contract, search fields (`code`, `teacher`), business code compliance (D1), and error envelopes. |
| Number of TCs | 7 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 7 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 7 | 0 |
| Round 3 | 0 | 0 | 7 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **POST /api/v1/classrooms** | | | | | | | | | | | | | | |
| MF02-TC-INT-004 | Test classroom creation rejection when school has no active academic year. | 1. Login as School Admin of School C (`SCH-HN-0063`, no active academic year).<br>2. Send POST to `/api/v1/classrooms` with body `{"code":"1A","grade":1,"capacity":40}`. | 1. Returns HTTP 400 Bad Request.<br>2. Error envelope returned with `error.code = "SCH-004"` and `MSG-004`.<br>3. No classroom record created in DB. | School C has no active academic year. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-192 active year requirement |
| MF02-TC-INT-015 | Test client-supplied duplicate classroom code rejection (D5). | 1. Send POST to `/api/v1/classrooms` with body `{"code":"3A","grade":3,"capacity":45}` where `3A` already exists. | 1. Returns HTTP 409 Conflict.<br>2. Error envelope: `error.code = "SCH-409"`, `error.message = MSG-139` ("A record with this value already exists.").<br>3. Existing classroom `3A` data unmodified. | Classroom `3A` already exists under active year. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | D5 client ID duplicate guard |
| **GET /api/v1/classrooms** | | | | | | | | | | | | | | |
| MF02-TC-INT-009 | Test classroom list pagination contract (5 fields, page clamping). | 1. Send GET to `/api/v1/classrooms` with no params.<br>2. Send GET to `/api/v1/classrooms?page=2&pageSize=5`.<br>3. Send GET with page out of bounds `/api/v1/classrooms?page=99`. | 1. Step 1 returns HTTP 200 with 5 fields (`items`, `total`, `page`, `pageSize`, `pageCount`).<br>2. Step 3 page clamped to `pageCount`, returning last valid page.<br>3. `pageCount >= 1` even when item list is empty. | 10 classrooms seeded for active academic year. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | 5-field pagination contract |
| MF02-TC-INT-010 | Test classroom search on allowed fields (`code`, `teacher`). | 1. Send GET to `/api/v1/classrooms?search=3A`.<br>2. Send GET to `/api/v1/classrooms?search=Dao Van Quan` (teacher name).<br>3. Send GET to `/api/v1/classrooms?search=NONEXISTENT`. | 1. Step 1 matches classroom with `code = "3A"`.<br>2. Step 2 matches classroom assigned to teacher "Dao Van Quan".<br>3. Step 3 returns HTTP 200 with `items: []`, `total: 0`. | Classrooms `3A` and `4A` (assigned to Dao Van Quan) seeded. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | SEARCH_FIELDS compliance |
| **GET /api/v1/classrooms/{code}** | | | | | | | | | | | | | | |
| MF02-TC-INT-011 | Test business code path parameter format compliance (D1). | 1. Send GET to `/api/v1/classrooms/3A` using classroom code.<br>2. Send GET to `/api/v1/classrooms/<raw-uuid>`. | 1. Step 1 returns HTTP 200 OK with `id = "3A"`.<br>2. Step 2 returns HTTP 404 Not Found (`SCH-404`, `MSG-137`), confirming raw UUIDs are not exposed as path IDs. | Classroom `3A` active in DB. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | D1 business code contract |
| **PUT /api/v1/classrooms/{code}/teacher** | | | | | | | | | | | | | | |
| MF02-TC-INT-017 | Test homeroom teacher assignment audit trail and event publishing. | 1. Send PUT to `/api/v1/classrooms/3A/teacher` with body `{"teacherId":"STF-004"}` (Luu Thi Bich).<br>2. Inspect audit logs and event publisher. | 1. Returns HTTP 200 OK with `MSG-029`.<br>2. Previous teacher unlinked, `STF-004` set as new homeroom teacher.<br>3. `audit_logs` records `ASSIGN_HOMEROOM_TEACHER` with actor ID.<br>4. `HomeroomTeacherAssigned` event published. | Classroom `3A` and teacher `STF-004` active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-020 / BR-026 audit logging |
| **General Response Format** | | | | | | | | | | | | | | |
| MF02-TC-INT-013 | Test response format compliance (D2 envelope, D4 ISO-8601 dates). | 1. Send request triggering validation error.<br>2. Inspect success and error responses. | 1. Error response matches D2 envelope: `{error:{code,message,details}}`.<br>2. `details` is array, never `null`.<br>3. All date/time fields formatted as ISO-8601 strings (`yyyy-MM-dd'T'HH:mm:ss'Z'`). | Test execution environment active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | D2/D4 format verification |
