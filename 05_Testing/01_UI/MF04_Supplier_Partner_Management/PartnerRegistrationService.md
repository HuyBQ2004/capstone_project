# Unit Test Case — PartnerRegistrationService / PartnerProfileService (MF04 Supplier & Partner Management)

---

## Function: `PartnerRegistrationService.registerSupplier()` / `PartnerProfileService.updateMine()` — email uniqueness (MSG-006)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-015` | | Function Name | `PartnerRegistrationService.registerSupplier()` / `PartnerProfileService.updateMine()` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 20 | | Lack of test cases | 0 |
| Test requirement | An email already registered to a partner must block both a new registration and a profile update to that same address, with `SUP-006`/MSG-006 (*"Registration failed: The email address is already in use."*), compared case-insensitively (BR-012, UC-049 AF2). §1.4 assigns MSG-006 to UC-071/UC-067. Source: MF04-TC-015, TC-COND-013. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 2 | 0 | 0 | 0 | 2 | 0 | 2 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Flow | `registerSupplier(email="sonlt@greenfarm.vn")` — already belongs to `SUP-001` | O | |
| | `updateMine(email of another partner)` on an unrelated partner | | O |
| Case variant | Also probed with `"SONLT@GreenFarm.VN"` (mixed case) | O | O |
| **Confirm** | | | |
| Exception | `ConflictException code="SUP-006"`, message byte-for-byte MSG-006, triggers on the case-insensitive match too | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | A | A |
| Passed/Failed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | |  |

Note: If the comparison is case-sensitive, `SONLT@GreenFarm.VN` and `sonlt@greenfarm.vn` are treated as two different accounts, and MF01 ends up linking two user accounts to what should be a single partner. Because `partnerId` is the tenant-isolation key for the entire module (ADR-M04-3), a duplicated partner identity is a direct precursor to the cross-tenant data leak that `MF04-TC-SEC-001` is designed to catch.
