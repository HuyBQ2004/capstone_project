# Unit Test Case — GeofenceValidatorService (MF06 Ingredient & Inventory Management)

---

## Function: `GeofenceValidator.verify(lat, lng, schoolConfig)`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF06-FUNC-008` | | Function Name | `GeofenceValidator.verify()` |
| Created By | `Bui Quang Huy` | | Executed By | `Huy` |
| Lines of code | `TBD` | | Lack of test cases | `TBD` |
| Test requirement | `Receiving must be verified to occur within the school's authorized receiving-area radius (BR-074, BR-073); the radius is read from school_config.geofence_radius (default 200m per BR-028/MF02, not the 50m figure in the UC file — CF-M06-2) and must never be accepted from the client request. The 200m boundary is inclusive (PASS at exactly 200m, blocked at 201m); when the feature flag sfms.geofence.enforced=false the check is a no-op (dev convenience, ADR-M06-7). A blocked attempt returns 422 ING-015 with literal message MSG-015: "Receiving action blocked: GPS location verification failed (out of tolerance)."` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `1` | `0` | `0` | `1` | `1` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Precondition | `FX-SCHOOL-GEO`: `school_config.geofence_radius = 200m` for `TH Doan Thi Diem` | O |
| | `FX-ENV`: `sfms.geofence.enforced=true` | O |
| Operation Input | `verify(lat, lng, schoolConfig)` at distance `199m` from school coordinates | O |
| | `verify(...)` at distance `200m` (boundary, inclusive) | O |
| | `verify(...)` at distance `201m` | O |
| | `verify(...)` at distance `640m` | O |
| | `verify(...)` with `sfms.geofence.enforced=false` (any distance) | O |
| **Confirm** | | |
| Return | `199m` → PASS; `200m` → PASS (inclusive boundary); `enforced=false` → no-op PASS regardless of distance | O |
| Exception | `201m` and `640m` → blocked: `422 ING-015` | O |
| Log message | `MSG-015`: *"Receiving action blocked: GPS location verification failed (out of tolerance)."* (asserted literally — MSG-015 is in TDS-02 test basis, no ORACLE GAP) | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | B |
| Passed/Failed | Untested |
| Executed Date | 2026-08-05 |
| Defect ID | |

> UTCID mapping: UTCID01 = `M06-TC-020`.
