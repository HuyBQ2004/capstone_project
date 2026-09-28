# Integration Test Case — School Management (MF02 School & Class Management)

---

## Feature: School Management & Configuration

### 1. General Information

| Field | Value |
|---|---|
| Feature | School Management & Configuration |
| Test requirement | Verify school registration, coordinate lock/unlock workflows, geofence radius updates (BR-028), and school status deactivation cascade (BR-021). |
| Number of TCs | 4 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 4 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 4 | 0 |
| Round 3 | 0 | 0 | 4 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **POST /api/v1/schools/register** | | | | | | | | | | | | | | |
| MF02-TC-INT-012 | Test school registration happy path and timestamp formatting (D4). | 1. Send public POST to `/api/v1/schools/register` with body `{"code":"SCH-HN-0099","name":"TH Le Quy Don","taxId":"0109999888","lat":21.020000,"lng":105.780000}`.<br>2. Inspect response JSON timestamps. | 1. Returns HTTP 201 Created.<br>2. School `status` initialized to `PENDING`.<br>3. `registeredAt` and `createdAt` formatted as ISO-8601 UTC strings (`yyyy-MM-dd'T'HH:mm:ss'Z'`). | Public endpoint access active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | UC-070 school registration |
| **POST /api/v1/schools/{code}/unlock-coordinate** | | | | | | | | | | | | | | |
| MF02-TC-INT-014 | Test school coordinate unlocking workflow by System Admin. | 1. Login as School Admin and attempt to update locked coordinates.<br>2. Login as System Admin and POST to `/api/v1/schools/SCH-HN-0042/unlock-coordinate` with body `{"reason":"Main gate relocation"}`.<br>3. Attempt coordinate update again. | 1. Step 1 returns HTTP 403 Forbidden (`SCH-013`, `MSG-108`).<br>2. Step 2 returns HTTP 200 OK, `coordinateLocked` becomes `false`.<br>3. Step 3 succeeds with new lat/lng.<br>4. Unlock action logged in audit. | Locked school `SCH-HN-0042` seeded. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-028 coordinate lock |
| **PATCH /api/v1/schools/{code}/status** | | | | | | | | | | | | | | |
| MF02-TC-INT-017 | Test school status deactivation and cascade suspension (BR-021). | 1. Send PATCH to `/api/v1/schools/SCH-HN-0042/status` with `{"active": false}` as System Admin.<br>2. Inspect domain event publisher. | 1. Returns HTTP 200 OK.<br>2. School `status` transitions to `INACTIVE`.<br>3. `SchoolDeactivated` event published for downstream cascade suspension to user accounts and students.<br>4. Action recorded in audit log. | Active school `SCH-HN-0042` seeded. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-021 cascade deactivation |
| **PUT /api/v1/schools/{code}/config** | | | | | | | | | | | | | | |
| MF02-TC-INT-018 | Test school geofence configuration updates [50..500m]. | 1. Send PUT to `/api/v1/schools/SCH-HN-0042/config` with `{"geofenceRadius": 300}`.<br>2. Send PUT with `{"geofenceRadius": 30}` (out of bounds < 50). | 1. Step 1 returns HTTP 200 OK with `geofenceRadius = 300`.<br>2. Step 2 returns HTTP 400 Bad Request (`SCH-013`, `MSG-107`). | Active school `SCH-HN-0042` seeded. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-028 geofence config |
