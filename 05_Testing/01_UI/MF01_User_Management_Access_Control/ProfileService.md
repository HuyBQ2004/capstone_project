# Unit Test Case — ProfileService (MF01 User Management & Access Control)

---

## Function: `ProfileService.updatePhoto`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `IAM-FUNC-008` | | Function Name | `ProfileService.updatePhoto` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `145` | | Lack of test cases | `0` |
| Test requirement | `Verify avatar upload validation: the size limit is exactly 10MB inclusive per MSG-008 (not the 5MB stated in UC-097), only PDF, JPG and PNG are accepted per MSG-009, and the decision is taken from the file magic bytes rather than the declared extension or Content-Type, so a renamed executable, an SVG declared as PNG, and an empty file are all rejected with IAM-010 instead of a 500 error.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `13` | `0` | `0` | `5` | `5` | `3` | `13` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 | UTCID11 | UTCID12 | UTCID13 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | | | | | | |
| Precondition | Authenticated user `U-0001` with `avatarUrl = NULL` | O | O | O | O | O | O | O | O | O | O | O | O | O |
| File size | 10MB minus 1 byte | O | | | | | | | | | | | | |
| | Exactly 10MB (the documented maximum) | | O | | | | | | | | | | | |
| | 10MB plus 1 byte | | | O | | | | | | | | | | |
| | 6MB (above the UC-097 figure of 5MB, below MSG-008 limit of 10MB) | | | | O | | | | | | | | | |
| | 1KB (well inside the limit) | | | | | O | O | O | O | O | O | | | |
| | 2KB | | | | | | | | | | | O | | |
| | 0 byte (empty upload) | | | | | | | | | | | | | O |
| File name / declared type | `a.jpg` — `image/jpeg`, valid JPEG magic bytes | O | O | O | O | O | | | | | | | | |
| | `a.jpeg` — `image/jpeg`, valid JPEG magic bytes | | | | | | O | | | | | | | |
| | `a.png` — `image/png`, valid PNG magic bytes | | | | | | | O | | | | | | |
| | `a.pdf` — `application/pdf`, valid PDF magic bytes | | | | | | | | O | | | | | |
| | `a.gif` — `image/gif`, GIF magic bytes | | | | | | | | | O | | | | |
| | `a.svg` — `image/svg+xml`, SVG content | | | | | | | | | | O | | | |
| | `payload.jpg` — `image/jpeg` declared, but `MZ` executable magic bytes | | | | | | | | | | | O | | |
| | `x.png` — `image/png` declared, but SVG content `<svg onload=...>` | | | | | | | | | | | | O | |
| | `x.jpg` — `image/jpeg`, no content at all | | | | | | | | | | | | | O |
| **Confirm** | | | | | | | | | | | | | | |
| Return | Upload accepted, no exception thrown | O | O | | O | O | O | O | O | | | | | |
| | `ApiException` thrown, upload rejected | | | O | | | | | | O | O | O | O | O |
| Exception | `IAM-010`: File upload failed (HTTP 422) | | | O | | | | | | O | O | O | O | O |
| Log message | `MSG-008`: File upload failed: File size exceeds the maximum limit of 10MB. | | | O | | | | | | | | | | |
| | `MSG-009`: File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed. | | | | | | | | | O | O | O | | O |
| State Verification | `saveAndFlush` never invoked and `avatarUrl` stays NULL | | | O | | | | | | | | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 | UTCID11 | UTCID12 | UTCID13 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | B | B | B | N | N | N | N | N | A | A | A | A | A |
| Passed/Failed | P | P | P | P | P | P | P | P | P | P | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | | | | | | | | | | | |
