# Unit Test Case — PartnerMapper / SupplierPartnershipMapper Derived Fields (MF04 Supplier & Partner Management)

---

## Function: `PartnerMapper.toResponse()` / `SupplierPartnershipMapper.toResponse()` — `certStatus`/`certExpiry`/`docStatus` are derived, not stored columns

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-011` | | Function Name | `PartnerMapper.toResponse()` / `SupplierPartnershipMapper.toResponse()` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 20 | | Lack of test cases | 0 |
| Test requirement | `certStatus`, `certExpiry`, per-document `status`, and `docStatus` must all be computed at read time from `partner_documents`, never persisted as a physical column (ADR-M04-4, ADR-M02-2 — "no derived field is stored"). Evaluated against the fixed clock FX-014 (`2026-07-28`). Source: MF04-TC-011, TC-COND-009, L12. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 1 | 0 | 0 | 1 | 0 | 0 | 1 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Setup | Partner with 3 documents (FX-005): `VALID` (`2027-03-15`), `NULL` expiry, `URGENT` (`2026-07-28`) — mapped with `Clock` FX-014 | O |
| **Confirm** | | |
| Return | `certExpiry == "2027-03-15"` — the farthest still-valid `FOOD_SAFETY_CERT` date, ISO-8601 (D4) | O |
| | `certStatus == "VALID"` (derived from that same document) | O |
| | `documents[].status == ["VALID","VALID","URGENT"]` (the `NULL` entry reads as `VALID`) | O |
| | `SupplierPartnershipResponse.docStatus == "URGENT"` — worst status across the partner's documents | O |
| | `Partner` class has no `setCertStatus`/`setCertExpiry` setter (checked by reflection, after first confirming a genuine setter like `setName` exists — to avoid a false green) | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | N |
| Passed/Failed | Passed |
| Executed Date | 2026-08-05 |
| Defect ID | |

Note: If these values are read from a stored column instead of computed at request time, a certificate that expires overnight still shows `VALID` in the API until some batch job happens to run and rewrite the column — exactly the class of "frozen data" ADR-M02-2 was written to prevent. `MF04-TC-INT-016` verifies the same invariant at the schema level (`information_schema` has no `cert_status`/`doc_status` column at all), so a regression here would also be caught there.
