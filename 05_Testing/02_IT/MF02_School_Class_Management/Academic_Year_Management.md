# Integration Test Case — Academic Year Management (MF02 School & Class Management)

---

## Feature: Academic Year Management & Student Promotion

### 1. General Information

| Field | Value |
|---|---|
| Feature | Academic Year Management & Student Promotion |
| Test requirement | Verify academic year creation, activation concurrency & per-school single active year constraint (BR-191), closing academic years & read-only status (BR-193), class structure copying (BR-194), student promotion (UC-177), and archived year queries (BR-195). |
| Number of TCs | 8 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 6 | 0 | 2 | 0 |
| Round 2 | 0 | 0 | 8 | 0 |
| Round 3 | 0 | 0 | 8 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **POST /api/v1/academic-years/{code}/activate** | | | | | | | | | | | | | | |
| MF02-TC-INT-001 | Test academic year activation happy path and status transition. | 1. Send POST to `/api/v1/academic-years/AY-2026/activate` as School Admin of School A.<br>2. Inspect HTTP status and JSON response.<br>3. Verify DB status for previous year (`AY-2025`) and new year (`AY-2026`). | 1. Returns HTTP 200 OK with `MSG-083`.<br>2. `AY-2025` status transitions to `CLOSED`.<br>3. `AY-2026` status transitions to `ACTIVE`.<br>4. Domain event `AcademicYearActivated` published. | `AY-2025` (ACTIVE) and `AY-2026` (DRAFT) exist for School A. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | UC-175 activation |
| MF02-TC-INT-002 | Test per-school single ACTIVE academic year constraint (BR-191). | 1. Verify School A has `AY-2025` ACTIVE.<br>2. Send POST as School Admin of School B to `/api/v1/academic-years/AY-2026-B/activate`.<br>3. Verify DB state for School A and School B. | 1. Step 2 returns HTTP 200 OK.<br>2. School B `AY-2026-B` becomes `ACTIVE`.<br>3. School A `AY-2025` remains `ACTIVE` (BR-191 rule is per-school, not global). | School A and School B seeded with separate academic years. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-191 per-school isolation |
| MF02-TC-INT-003 | Test pessimistic lock handling during concurrent academic year activations. | 1. Trigger two parallel POST activation requests for `AY-2026` simultaneously.<br>2. Inspect response status for both threads. | 1. Exactly one request succeeds with HTTP 200 OK.<br>2. Second request handles pessimistic lock cleanly (no 500 error).<br>3. DB unique constraint `uq_ay_one_active_per_school` remains unviolated. | Parallel execution threads initialized. | Pending | 2026-08-05 | Nam | Pending | | | Pending | | | Concurrency locking |
| **POST /api/v1/academic-years/{code}/close** | | | | | | | | | | | | | | |
| MF02-TC-INT-005 | Test closing academic year and cascading read-only status to classrooms. | 1. Send POST to `/api/v1/academic-years/AY-2025/close`.<br>2. Attempt to modify classroom `3A` under `AY-2025`. | 1. Returns HTTP 200 OK with `MSG-084`.<br>2. `AY-2025` status becomes `CLOSED`.<br>3. All classrooms under `AY-2025` updated to `readOnly = true`.<br>4. Step 2 returns HTTP 400 (`SCH-014`). | Active academic year `AY-2025` with classrooms. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-193 read-only closing |
| **POST /api/v1/academic-years** | | | | | | | | | | | | | | |
| MF02-TC-INT-006 | Test academic year creation with class structure copying. | 1. Send POST to `/api/v1/academic-years` with `copyStructure = true`.<br>2. Inspect newly created classrooms for new academic year. | 1. Returns HTTP 201 Created.<br>2. Classroom templates copied with identical grade, name, capacity.<br>3. `students` count for all new classroom templates initialized to 0 (BR-194). | Existing academic year `AY-2025` with 10 classrooms. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-194 class copying |
| **POST /api/v1/academic-years/{code}/promote** | | | | | | | | | | | | | | |
| MF02-TC-INT-007 | Test explicit student promotion between academic years. | 1. Send POST to `/api/v1/academic-years/AY-2025/promote` with target grade 5 classrooms.<br>2. Exclude specific student `HS-5A-005`.<br>3. Verify student enrollment records. | 1. Returns HTTP 200 OK.<br>2. Non-excluded grade 5 students promoted to target grade 6 classrooms.<br>3. Excluded student `HS-5A-005` remains in current grade.<br>4. Student health records remain untouched. | Active year `AY-2025` and target year `AY-2026` set up. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | UC-177 student promotion |
| MF02-TC-INT-008 | Test student promotion capacity overflow rejection. | 1. Attempt to promote 10 students into target classroom `6B` with capacity 45 and current enrollment 40. | 1. Returns HTTP 422 Unprocessable Entity.<br>2. Error envelope returned with `error.code = "SCH-010"` and `MSG-010`.<br>3. No student promotions committed. | Target classroom `6B` near capacity. | Pending | 2026-08-05 | Nam | Pending | | | Pending | | | Promotion capacity guard |
| **GET /api/v1/academic-years** | | | | | | | | | | | | | | |
| MF02-TC-INT-016 | Test querying archived academic years read-only state. | 1. Send GET to `/api/v1/academic-years` filtering by `status = ARCHIVED`.<br>2. Attempt to update archived academic year record. | 1. Returns HTTP 200 OK with archived academic year items.<br>2. Step 2 returns HTTP 403 Forbidden. | Archived academic year `AY-2023` seeded in DB. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-195 archived year read-only |
