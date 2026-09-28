# Unit Test Case — ComplianceDocumentService (MF04 Supplier & Partner Management)

---

## Function: `ComplianceDocumentService.upload()` — expired ATTP certificate blocks onboarding (BR-046/048)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-012` | | Function Name | `ComplianceDocumentService.upload()` — expiry gate |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 18 | | Lack of test cases | 0 |
| Test requirement | Uploading a `FOOD_SAFETY_CERT` whose `expiryDate` is already in the past must be blocked with `SUP-013`/MSG-013 ⚠️ *(message content confirmed by `error_message.md`, not yet linked to an MF04 UC in traceability §1.4 — CF-M04-2)*, and no file may reach storage. `PRODUCT_ORIGIN` documents have no such gate (UC-102 has no matching Alternative Flow). Source: MF04-TC-012, UC-101 AF1, TC-COND-010. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 2 | 0 | 0 | 1 | 0 | 1 | 2 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Upload | `documentType=FOOD_SAFETY_CERT, expiryDate=2026-07-27` (yesterday per FX-014) | O | |
| | `documentType=PRODUCT_ORIGIN, expiryDate=2026-07-27` (past date) | | O |
| **Confirm** | | | |
| Exception | `ConflictException code="SUP-013"`, HTTP 409, message byte-for-byte MSG-013 (*"Supplier food safety certificate has expired. Onboarding blocked."*) | O | |
| | `storageService.store()` never invoked | O | |
| Return | Upload succeeds (no expiry gate for this document type) | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | B | N |
| Passed/Failed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | |  |

Note: If UTCID01 fails to block, a supplier can complete onboarding with a legally void certificate at the very front door of the module — BR-046 would already be broken before a single ingredient has moved. The seed fixture `FX-003` (`SUP-004`, food-safety cert expired `2026-06-22`) exists precisely to keep this scenario reproducible end-to-end (see `MF04-TC-INT-002`).

---

## Function: `ComplianceDocumentService.upload()` — food-safety certificate requires an `expiryDate` (BR-047)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-013` | | Function Name | `ComplianceDocumentService.upload()` + `ck_doc_cert_expiry` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 10 | | Lack of test cases | 0 |
| Test requirement | A `FOOD_SAFETY_CERT` upload with `expiryDate = null` must be rejected — only documents where "no expiry" is a legitimate business state (e.g. some `PRODUCT_ORIGIN` records) may carry `null` under L4/E5; a food-safety certificate always has a 3-year term (BR-047). Source: MF04-TC-013, TC-COND-011. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 1 | 0 | 1 | 0 | 1 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Upload | `documentType=FOOD_SAFETY_CERT, expiryDate=null` | O |
| **Confirm** | | |
| Exception | `ValidationException` HTTP 422; `details` contains `{"field":"expiryDate"}` (D2) | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | A |
| Passed/Failed | Untested |
| Executed Date | 2026-08-05 |
| Defect ID | |

Note: `null` on `expiryDate` is deliberately overloaded to mean "no expiry" (L4/E5, seed `DOC-GF-02`) — if a food-safety certificate is allowed through with `null`, `MF04-TC-INT-009`'s BR-047 scan job would permanently skip it, and a 3-year certificate would effectively become perpetual in the system with no renewal reminder ever firing.

---

## Function: `ComplianceDocumentService.upload()` — `StorageService`, whitelist and size limits (MSG-008/009, D10)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-017` | | Function Name | `ComplianceDocumentService.upload()` — storage + whitelist |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 26 | | Lack of test cases | 0 |
| Test requirement | Uploads must go through `common/storage/StorageService` (D10) — never `MultipartFile.transferTo()` or a direct AWS SDK call — and enforce ≤10MB / PDF·JPG·PNG for compliance documents (MSG-008/MSG-009). Source: MF04-TC-017, TC-COND-015. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 6 | 0 | 0 | 1 | 3 | 2 | 6 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | |
| File | `cert-valid.pdf`, 200KB, magic `%PDF` | O | | | | | |
| | Exactly 10,485,760 bytes (10MB boundary, inclusive) | | O | | | | |
| | `cert-oversize.pdf`, 10,485,761 bytes | | | O | | | |
| | `face-oversize.jpg`, 6MB (5MB limit for face images) | | | | O | | |
| | `notes.txt`, `text/plain` | | | | | O | |
| | `fake.pdf`, `.pdf` extension, magic bytes `MZ` | | | | | | O |
| **Confirm** | | | | | | | |
| Return | `201`, `storageService.store()` invoked exactly once | O | O | | | | |
| Exception | `413` `SUP-008` | | | O | O | | |
| | `415` `SUP-009` | | | | | O | O |
| | `storageService.store()` never invoked | | | O | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|
| Type (N/A/B) | N | B | B | B | A | A |
| Passed/Failed | Passed | Passed | Passed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | |

Note: `D10` (`00_Common_Conventions.md`) lists the modules required to use `StorageService` and currently omits MF04 by an oversight, even though MF04 has an active upload flow (compliance documents, UC-101/102/091) — the TDS already binds MF04 to `StorageService` regardless (M04-D11), so this test locks that in place. If UTCID06 is checked only by file extension instead of magic bytes, an executable renamed to `.pdf` would be accepted and served back from storage — the same class of vulnerability probed at the security layer in `MF04-TC-SEC-004`.
