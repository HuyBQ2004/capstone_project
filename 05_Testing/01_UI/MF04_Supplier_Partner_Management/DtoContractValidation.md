# Unit Test Case — Enum / ISO-Date Contract (MF04 Supplier & Partner Management)

---

## Function: Enum contract — `PartnerType`/`BiometricStatus`/`RoleKey` SCREAMING_SNAKE, from `common/enums` (D6/L2)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-018` | | Function Name | `EnumContractTest` (`common/enums/{PartnerType,BiometricStatus,RoleKey}`, `com.sfms.mf04.{DocumentType,PartnerStatus,ApplicationStatus,PartnershipStatus}`) |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 24 | | Lack of test cases | 0 |
| Test requirement | Every MF04 enum serializes and stores as SCREAMING_SNAKE and rejects Vietnamese/display-case strings (D6), overriding TDS-MF04 v1.0's `"Raw Food Supplier"`-style values (`review.md` E1, blocker). `PartnerType`/`BiometricStatus`/`RoleKey` must be the same class as `common/enums` — not a module-local duplicate — while `DocumentType`/`PartnerStatus`/`ApplicationStatus`/`PartnershipStatus` are legitimately module-owned (M04-D02) since they fall outside D6's 8-enum list. `BiometricStatus` has exactly 3 values (D6/L3). Source: MF04-TC-018, TC-COND-016. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 6 | 3 | 2 | 1 | 6 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | |
| Serialize | `PartnerResponse(type=RAW_FOOD_SUPPLIER)` | O | | | | | |
| Deserialize | `{"type":"Raw Food Supplier"}` / `"Service Caterer"` / `"Supplier"` (display strings) | | O | | | | |
| Enum count | `PartnerType.values().length` | | | O | | | |
| | `BiometricStatus.values().length` | | | | O | | |
| Package check | `PartnerType`/`BiometricStatus`/`RoleKey` class package | | | | | O | |
| `RoleKey.from()` | `"School Administration"` / `"Teacher"` (BR-007 labels, not kebab-case) | | | | | | O |
| **Confirm** | | | | | | | |
| Return | JSON contains `"type":"RAW_FOOD_SUPPLIER"`, never `"Raw Food Supplier"` | O | | | | | |
| Exception | `InvalidFormatException` → HTTP 400 | | O | | | | O |
| Return | `== 2` (`RAW_FOOD_SUPPLIER`, `SERVICE_CATERER`) | | | O | | | |
| | `== 3` (`NOT_REGISTERED`, `PENDING_REGISTRATION`, `REGISTERED`) | | | | O | | |
| | `PartnerType.class.getPackageName() == "com.sfms.common.enums"` (same for `BiometricStatus`, `RoleKey`) | | | | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|
| Type (N/A/B) | N | A | N | N | A | B |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | |

Note: If the display string `"Raw Food Supplier"` is accepted, a filter call like `?type=Supplier` — a stale value still present in the FE's own `TYPE_FILTER` constant — silently returns an empty list, and the user reads that as "there are no suppliers" rather than "the filter value is wrong". Declaring `PartnerType`/`BiometricStatus`/`RoleKey` as module-local copies instead of importing `common/enums` would also mean two independent enums drift apart the first time either module changes its list — exactly the two-taxonomy failure mode called out as a blocker in `review.md` E1.

---

## Function: Date format contract — every MF04 JSON date field is ISO-8601 (D4/L4)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-019` | | Function Name | `DateFormatContractTest` (Jackson config + `PartnerResponse`, `PartnerDetailResponse`, `DocumentResponse`, `DeliveryPersonResponse`, `ApplicationResponse`, `SupplierPartnershipResponse`) |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 18 | | Lack of test cases | 0 |
| Test requirement | Every date/time field must serialize as ISO-8601 — `LocalDate` as `"2026-07-29"`, `Instant` as `"2026-07-29T02:15:00Z"` — never `dd/MM/yyyy`, overriding the 3 leftover `dd/MM/yyyy` examples in TDS-MF04 v1.0 §9.2 (`review.md` A4, L4). `expiryDate` on a "no expiry" document must serialize as JSON `null`, not a display string. Source: MF04-TC-019, TC-COND-017. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 4 | 0 | 0 | 1 | 3 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Serialize | All 6 DTOs listed above, full JSON regex scan | O | | | |
| | Document with `expiryDate = null` (no expiry, L4/E5) | | O | | |
| Code scan | `@JsonFormat(pattern="dd/MM/yyyy")` usages inside `com.sfms.mf04` | | | O | |
| Request payload | `submittedAt` supplied by the client in the request body | | | | O |
| **Confirm** | | | | | |
| Return | No field matches `\d{2}/\d{2}/\d{4}`; `Instant` fields match `\d{4}-\d{2}-\d{2}T.*Z`; `LocalDate` fields match `^\d{4}-\d{2}-\d{2}$` | O | | | |
| | `expiryDate` is JSON `null`, not `"Không thời hạn"` or `""` | | O | | |
| Structural | Zero matches — no leftover `@JsonFormat("dd/MM/yyyy")` annotation | | | O | |
| Return | Client value ignored; server clock (FX-001) used instead | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | A | A | A |
| Passed/Failed | Passed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

Note: The mock currently mixes both formats in the same JSON payload (`review.md` A4 names MF04 explicitly as "one TDS that says both"). If `dd/MM/yyyy` survives anywhere, the FE's `dateFrom`/`dateTo` filters (Team Plan §2b) silently return an empty result set instead of an error — a much harder defect to notice than a thrown exception. `MF04-TC-INT-013` re-checks the same invariant end-to-end against a live database.
