# Unit Test Case — AllergySeverityBinding (MF03 Student & Parent Management)

> Source: `TestSpec-MF03_Student_Parent_Management.md` §4.1 `MF03-TC-007`. Test file: `04_Development/Backend/src/test/java/com/sfms/mf03/dto/AllergySeverityBindingTest.java`.

---

## Function: `SubmitAllergyRequest` binding — `AllergySeverity` enum (D6)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `STU-FUNC-008` | | Function Name | `AllergyDeclarationDtos.SubmitAllergyRequest` DTO binding / `AllergySeverity` (`common/enums/`) |
| Created By | Bui Quang Huy | | Executed By | Huy |
| Lines of code | `50` | | Lack of test cases | `0` |
| Test requirement | Per **D6**, `AllergySeverity` accepts only the SCREAMING_SNAKE values `MILD`/`MODERATE`/`SEVERE` in JSON binding. Every historical/legacy vocabulary variant (`"NANG"`, mixed-case `"Severe"`/`"Moderate"`/`"Mild"`, Vietnamese labels `"Nặng"`/`"Trung bình"`/`"Nhẹ"`, empty string) must fail Jackson binding with an exception. A `null` severity must bind to a `null` field without throwing (the `@NotNull` gate is enforced separately at the Spring `@Valid` controller layer, not at plain Jackson binding). | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `12` | `0` | `0` | `3` | `8` | `1` | `12` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 | UTCID11 | UTCID12 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | | | | | |
| `severity` value sent | `"MILD"` (D6 canonical) | O | | | | | | | | | | | |
| | `"MODERATE"` (D6 canonical) | | O | | | | | | | | | | |
| | `"SEVERE"` (D6 canonical) | | | O | | | | | | | | | |
| | `"NANG"` (legacy) | | | | O | | | | | | | | |
| | `"Severe"` (mixed-case legacy) | | | | | O | | | | | | | |
| | `"Moderate"` (mixed-case legacy) | | | | | | O | | | | | | |
| | `"Mild"` (mixed-case legacy) | | | | | | | O | | | | | |
| | `"Nặng"` (Vietnamese label) | | | | | | | | O | | | | |
| | `"Trung bình"` (Vietnamese label) | | | | | | | | | O | | | |
| | `"Nhẹ"` (Vietnamese label) | | | | | | | | | | O | | |
| | `""` (empty) | | | | | | | | | | | O | |
| | `null` | | | | | | | | | | | | O |
| **Confirm** | | | | | | | | | | | | | |
| Return | Binds successfully; `req.severity()` equals the sent enum value | O | O | O | | | | | | | | | |
| | `req.severity()` is `null` (no exception; `@NotNull` deferred to controller layer) | | | | | | | | | | | | O |
| Exception | Jackson `Exception` thrown at `mapper.readValue()` | | | | O | O | O | O | O | O | O | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 | UTCID11 | UTCID12 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | N | N | N | A | A | A | A | A | A | A | A | B |
| Passed/Failed | P | P | P | P | P | P | P | P | P | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | | | | | | | | | | |

Note: `AllergySeverity` is declared exactly once in `common/enums/` (owned by Nam) — MF03 must not redeclare it (D6). No lenient `@JsonCreator` may be added to accept Vietnamese labels; leniency here is precisely the historical bug D6 was created to close. UTCID12 (`null`) confirms Jackson-level binding is permissive by design — rejection of a missing severity happens one layer up, via Bean Validation on the controller, not covered by this DTO-binding unit test.
