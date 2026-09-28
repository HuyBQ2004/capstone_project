# Unit Test Case — UpdateHealthFileRequest (MF03 Student & Parent Management)

> Source: `TestSpec-MF03_Student_Parent_Management.md` §4.1 `MF03-TC-009`. Test file: `04_Development/Backend/src/test/java/com/sfms/mf03/dto/UpdateHealthFileRequestTest.java`.

---

## Function: `UpdateHealthFileRequest` binding / schema

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `STU-FUNC-009` | | Function Name | `HealthFileDtos.UpdateHealthFileRequest` DTO |
| Created By | Bui Quang Huy | | Executed By | Huy |
| Lines of code | `47` | | Lack of test cases | `0` |
| Test requirement | Per BR-172 (TDS §17.1 C8), `PATCH /health-file` accepts **only** the 4 clinical fields (`medicalConditions`, `medications`, `emergencyContact`, `emergencyInstructions`). This unit test verifies the DTO's own invariant part: (1) a payload with exactly these 4 fields binds correctly via Jackson, and (2) the record declares **exactly** these 4 components — no `allergies`/`specialDiet`/`healthStatus`/`allergySeverity` field can be silently added back to the DTO. Rejection of an actually-unknown field with HTTP 400 `STU-094`/`MSG-115` is enforced in `StudentHealthFileController.patchFile()` (raw-JSON-map check, since Jackson silently drops unknown fields when binding directly into a record) and is covered at integration level, not by this unit test. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `2` | `0` | `0` | `2` | `0` | `0` | `2` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Payload | 4 allowed fields only: `medicalConditions`, `medications`, `emergencyContact`, `emergencyInstructions` | O | |
| Record schema | `UpdateHealthFileRequest.class.getRecordComponents()` | | O |
| **Confirm** | | | |
| Return | Binds OK; `medicalConditions()`/`medications()`/`emergencyContact()`/`emergencyInstructions()` equal the sent values | O | |
| Schema Assertion | Record component names `== {medicalConditions, medications, emergencyContact, emergencyInstructions}` exactly, none of `allergies`/`specialDiet`/`healthStatus`/`allergySeverity` | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | N | N |
| Passed/Failed | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 |
| Defect ID | | |

Note: `spring.jackson.deserialization.fail-on-unknown-properties=true` alone is not the mechanism used here — the controller reads the raw JSON map before binding the DTO, because Jackson's default record binding silently ignores unknown properties (AP-AI-015 bug class). UTCID02 is a regression guard: adding a 5th field to this record (even by accident) reopens the "silent field drop" hole and turns this test red.
