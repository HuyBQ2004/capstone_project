# Unit Test Cases — MF08 Food Sample Retention
## Scope: Minimum sample amount (BR-203) and container state (BR-203 / QĐ 1246 Điều 6-7)

> Source module: `com.sfms.mf08.domain` / `com.sfms.mf08.service`
> Traceability: `07_implement/MF08_Food_Sample_Retention/TestSpec-MF08_Food_Sample_Retention.md` §4 — `MF08-TC-001`, `MF08-TC-002`

---

## Function: `SampleAmountValidator.validate()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-001` | | Function Name | Minimum sample amount by food type |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | BR-203 requires the retained sample weight to be at least 100g (solid food) or 150ml (liquid food); the server must enforce this even when a client calls the API directly, since UI-only validation can be bypassed. Source: `MF08-TC-001`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 7 | 3 | 0 | 4 | 7 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 |
|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | |
| Precondition | Inspection session PASSED (3/3 steps), geofence check passed, at least 1 evidence photo attached | O | O | O | O | O | O | O |
| `unit` | `G` (solid) | O | O | O | | | | O |
| | `ML` (liquid) | | | | O | O | O | |
| `amount` | `99.99` (below 100g threshold) | O | | | | | | |
| | `100` (lower boundary — accepted) | | O | | | | | |
| | `120` (normal, valid) | | | O | | | O | |
| | `149.99` (below 150ml threshold) | | | | O | | | |
| | `150` (lower boundary — accepted) | | | | | O | | |
| | `120` (valid for G, invalid for ML) | | | | | | O | |
| | `0` / `-5` (invalid magnitude) | | | | | | | O |
| **Confirm** | | | | | | | | |
| Return | Passes validation, proceeds to next gate (BR-107) | | O | O | | O | | |
| Exception | `ValidationException` code `SMP-019` | O | | | O | | O | O |
| Log message | MSG-019 — "Food sample retention weight too low: Must be at least 100g." (English, verbatim) | O | | | O | | O | O |
| Repository call | `repository.save()` is **never** invoked on the rejected branch | O | | | O | | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 |
|---|---|---|---|---|---|---|---|
| Type (N/A/B) | B | B | N | B | B | A | A |
| Passed/Failed | | | | | | | |
| Executed Date | | | | | | | |
| Defect ID | | | | | | | |

**Note:** `UTCID06` is the most important abnormal case: `amount = 120` is valid when `unit = G` but invalid when `unit = ML` — this proves the validator checks the (amount, unit) pair jointly, not just the numeric magnitude in isolation. A failure here means a caterer could register an under-weight liquid sample by mislabeling the unit, silently breaking the legal evidence requirement (QĐ 1246/QĐ-BYT Điều 7.2).

---

## Function: `FoodSampleService.register()` — Container state check

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-002` | | Function Name | Container must be sanitized and individually sealed |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | BR-203 / QĐ 1246 Điều 6.2 (sanitized container) and Điều 7.1 (each dish sealed separately) require the container to be `sanitized = true` and `sealed = true` before a sample can be registered. Source: `MF08-TC-002`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 3 | 1 | 2 | 0 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | Amount/unit already valid (passed `MF08-UT-001`) | O | O | O |
| `sanitized` | `false` | O | | |
| | `true` | | O | O |
| `sealed` | `false` | | O | |
| | `true` | O | | O |
| **Confirm** | | | | |
| Return | Passes this gate, proceeds to inspection gate (BR-105) | | | O |
| Exception | `ValidationException` code `SMP-203` | O | O | |
| Repository call | `repository.save()` is never invoked | O | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | A | N |
| Passed/Failed | | | |
| Executed Date | | | |
| Defect ID | | | |

**Note:** Both `sanitized` and `sealed` are booleans that default to `false` — the CHECK constraint `ck_smp_container_state` at the database layer must back this up, because a service-level bug (e.g. a mis-mapped `@AssertTrue`) could silently accept `false`/`false` and break the chain of custody: without proof the container was sterilized and sealed per dish, nobody can prove a retained sample wasn't tampered with between collection and lab hand-off.
