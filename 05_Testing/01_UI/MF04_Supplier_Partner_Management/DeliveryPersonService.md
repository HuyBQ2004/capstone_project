# Unit Test Case — DeliveryPersonService (MF04 Supplier & Partner Management)

---

## Function: `DeliveryPersonService.create()` — national ID must be unique system-wide (BR-050)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-001` | | Function Name | `DeliveryPersonService.create()` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 16 | | Lack of test cases | 0 |
| Test requirement | Registering a delivery person with a national ID (CCCD) already used by another record must be blocked before any write, hashed before the uniqueness check so plaintext CCCD never reaches the query layer (BR-050, UC-019 AF1). Source: MF04-TC-001, TC-COND-001. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 1 | 0 | 0 | 0 | 1 | 0 | 1 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Request | `create({fullName:"Nguyễn Văn X", nationalId:"001085912345", ...})` where `DP-01` (FX-006) already holds the same `nationalIdHash` | O |
| **Confirm** | | |
| Exception | `ConflictException code="SUP-050"` | O |
| Return | `repository.save()` never invoked | O |
| | Hash computed **before** the existence query — plaintext CCCD never sent to the DB layer | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | A |
| Passed/Failed | Passed |
| Executed Date | 2026-08-05 |
| Defect ID | |

Note: If a second delivery person can register with the same CCCD, two different individuals share one identity and the school gate's face-recognition check (BR-051) can no longer tell them apart. The service-level check alone cannot survive a race between two concurrent requests — the DB-level `UNIQUE ux_dp_nid_hash` constraint is the real backstop, exercised separately in `MF04-TC-INT-012`.

---

## Function: `CreateDeliveryPersonRequest` — Bean Validation on national ID / phone / plate (BR-050, BR-012)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-002` | | Function Name | `CreateDeliveryPersonRequest` (Jakarta Bean Validation) |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 14 | | Lack of test cases | 0 |
| Test requirement | Full name, national ID, phone and vehicle plate are mandatory; national ID must be exactly 12 digits (BR-050), phone must match the accepted format (BR-012), plate must match the standard plate pattern. At the API layer, every violation resolves to `422 SUP-138`/MSG-138 with `details[]` listing the offending field (M04-D08). Source: MF04-TC-002, TC-COND-002. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 8 | 3 | 3 | 2 | 8 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | |
| `nationalId` | `"001099212345"` (12 digits) | O | | | | | | | |
| | `"00109921234"` (11 digits) | | O | | | | | | |
| | `"0010992123456"` (13 digits) | | | O | | | | | |
| `phone` | `"0912345678"` / `"+84912345678"` | | | | O | | | | |
| | `"12345"` | | | | | O | | | |
| `vehiclePlate` | `"29C-123.45"` | | | | | | O | | |
| | `"XX-YY"` | | | | | | | O | |
| `fullName` | `""` / `"   "` | | | | | | | | O |
| **Confirm** | | | | | | | | | |
| Return | No `ConstraintViolation` | O | | | O | | O | | |
| Exception | `ConstraintViolation`; API → `422 SUP-138`/MSG-138, `details[]` names the field | | O | O | | O | | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | N | B | B | N | A | N | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | | | |

Note: An 11- or 13-digit CCCD that slips through (UTCID02/UTCID03) still hashes to a unique value, so nothing downstream catches it — the record sits in the database indefinitely with a national ID of the wrong length, invisible until someone manually cross-checks the physical ID card at the school gate.

---

## Function: `DeliveryPersonMapper.toResponse()` — national ID encrypted at rest, always masked in API (PDPD)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-003` | | Function Name | `DeliveryPersonMapper.toResponse()` + `DeliveryPersonService.create()` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 16 | | Lack of test cases | 0 |
| Test requirement | The national ID must never leave the API in full: the response keeps only the first 7 characters followed by masking (matching the mock's existing `"0010859xxxxx"` convention), the stored ciphertext must not decode back to the plaintext, and a SHA-256 hash is kept solely to enforce BR-050's uniqueness rule without ever decrypting. Masking happens at the mapper, not the controller (PDPD, UC-110 Normal Flow step 3, C5). Source: MF04-TC-003, TC-COND-003. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 1 | 1 | 0 | 0 | 1 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Operation | `create(nationalId="001099212345")` → read back entity **and** response DTO | O |
| **Confirm** | | |
| Return | `response.nationalId() == "0010992xxxxx"` | O |
| | `entity.getNationalIdEnc()` does not contain `"001099212345"` when UTF-8 decoded | O |
| | `entity.getNationalIdHash() == SHA-256("001099212345")` | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | N |
| Passed/Failed | Untested |
| Executed Date | 2026-08-05 |
| Defect ID | |

Note: Masking at the controller instead of the mapper is a trap — any other service that reads the same DTO (audit exports, internal reports) would receive the unmasked value again. This is `CWE-311` territory: the CCCD of every delivery person, listed for every supplier, is exactly the kind of bulk PII exposure PDPD singles out.

---

## Function: `DeliveryPersonService.registerFace()` — FRS failure does not change biometric status (BR-051)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-007` | | Function Name | `DeliveryPersonService.registerFace()` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 12 | | Lack of test cases | 0 |
| Test requirement | If the Face Recognition Service (FRS) rejects a submitted image, the delivery person's `biometricStatus`/`faceId` must remain unchanged and `SUP-014`/MSG-014 (HTTP 502) ⚠️ *(message confirmed, not yet in traceability §1.4 — CF-M04-2)* must be raised (UC-068 AF1). Source: MF04-TC-007, TC-COND-006. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 1 | 0 | 1 | 0 | 1 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Setup | `DP-03` (FX-006, `PENDING_REGISTRATION`), `FaceRecognitionPort.enroll()` throws `ExternalServiceException` (FX-015 `enrollThrows()`) | O |
| **Confirm** | | |
| Exception | `ExternalServiceException code="SUP-014"`, HTTP 502 | O |
| Return | `DP-03.biometricStatus` still `PENDING_REGISTRATION`, `faceId` still `null` | O |
| | `repository.save()` never invoked with `biometricStatus == REGISTERED` | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | A |
| Passed/Failed | Untested |
| Executed Date | 2026-08-05 |
| Defect ID | |

Note: If `REGISTERED` is set before the FRS confirms the enrollment, a delivery person could be scheduled for a delivery run while the school gate is physically unable to recognize them — the biometric record and the real-world enrollment state diverge with no exception raised at the moment it happens.

---

## Function: `DeliveryPersonService.registerFace()` / `confirmEnrollment()` — two-step biometric registration (D6/L3)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-008` | | Function Name | `DeliveryPersonService.registerFace()` + `confirmEnrollment()` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 24 | | Lack of test cases | 0 |
| Test requirement | `BiometricStatus` is a 3-value enum with a 2-step transition, overriding the 2-value/1-step description in TDS-MF04 v1.0: `POST /delivery-persons/{code}/face` moves `NOT_REGISTERED → PENDING_REGISTRATION` (no `faceId` yet — FRS has not confirmed), and a successful FRS confirmation moves `PENDING_REGISTRATION → REGISTERED` with `faceId` and `biometricRegisteredAt` populated and MSG-038 emitted. `NOT_REGISTERED → REGISTERED` directly is an illegal transition (D6, M04-D03). Source: MF04-TC-008, BR-051, TC-COND-006. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 2 | 2 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Step | `registerFace("DP-03", image, actor)` from `NOT_REGISTERED` | O | | | |
| | `confirmEnrollment("DP-03", faceId)` after step 1 succeeds | | O | | |
| | `registerFace(...)` with FX-015 `enrollThrows()` | | | O | |
| | `confirmEnrollment("DP-03", faceId)` while still `NOT_REGISTERED` (skip step 1) | | | | O |
| **Confirm** | | | | | |
| Return | `biometricStatus == PENDING_REGISTRATION`, `faceId == null`, `biometricRegisteredAt == null`, no `FaceDataRegistered` event yet | O | | | |
| | `biometricStatus == REGISTERED`, `faceId != null`, `biometricRegisteredAt != null`, `FaceDataRegistered` published, MSG-038 | | O | | |
| | Rolls back to `NOT_REGISTERED`, HTTP 502 `SUP-014`, `faceId` still `null` | | | O | |
| Exception | `IllegalStateTransitionException` — direct `NOT_REGISTERED → REGISTERED` rejected | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | N | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

Note: This encodes the finalized D6/M04-D03 semantics, not the TDS-MF04 v1.0 description of a single-step jump. `faceId` is assigned by the FRS only on successful enrollment (ADR-M04-5) — asserting `faceId == null` at `PENDING_REGISTRATION` is intentional, not an oversight. Without a real `PENDING_REGISTRATION` state, seed fixture `DP-03` would be an orphaned value with no code path that ever produces it (`review.md` E4).

---

## Function: `DeliveryPersonService.deleteFace()` — calls FRS before touching the database (BR-051)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-009` | | Function Name | `DeliveryPersonService.deleteFace()` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 16 | | Lack of test cases | 0 |
| Test requirement | Deleting biometric data must call the Face Recognition Service **before** updating the database, so that an FRS failure leaves the record unchanged rather than orphaning the FRS-side template; deletion is irreversible and revokes gate access (BR-051, UC-028 AF2, Other Information). Source: MF04-TC-009, TC-COND-007. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 2 | 1 | 1 | 0 | 2 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Case | A: `FaceRecognitionPort.delete()` throws (`DP-01`, `REGISTERED`, `faceId="mock-uuid"`) | O | |
| | B: FRS delete succeeds | | O |
| **Confirm** | | | |
| Exception | `SUP-014`; `DP-01` still `REGISTERED`, `faceId` unchanged | O | |
| Return | `InOrder` confirms `frsPort.delete()` runs before `repository.save()`; afterward `biometricStatus == NOT_REGISTERED`, `faceId == null`, `biometricRegisteredAt == null`; `FaceDataDeleted` payload contains no `faceId`/image (PDPD) | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | A | N |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | |  |

Note: If the database is cleared first and the FRS call fails afterward, the DB says "not registered" while the FRS still holds a live face template — the biometric data now exists completely outside the system's control, with no `faceId` left anywhere to reference it for a follow-up deletion. This is the exact ordering bug `CWE-212` is meant to catch.
