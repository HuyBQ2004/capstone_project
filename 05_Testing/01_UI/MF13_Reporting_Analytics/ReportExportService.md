# Unit Test Case — ReportExportService (MF13 Reporting & Analytics)

---

## Function: `exportReport` / `getExport`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `RPT-FUNC-001` | | Function Name | `ReportExportService.export` / `getExport` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `125` | | Lack of test cases | `0` |
| Test requirement | `Validate supported export formats (PDF, XLSX per BR-178), retention expiration >= 12 months (BR-177), immutable export snapshot & SHA-256 checksum integrity (BR-182), and kebab-case report slug validation.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `4` | `1` | `2` | `1` | `4` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Export Format Input | Format = `PDF` or `XLSX` (supported formats) | O | O | O | |
| | Format = `CSV` (unsupported format) | | | | O |
| Report Slug Input | Valid kebab-case slug (`sample-retention`) | O | O | O | |
| | Invalid slug (`SAMPLE_RETENTION` PascalCase) | | | | O |
| Retention Expiration | `expires_at >= generated_at + 12 months` | O | O | | |
| Export Snapshot | SHA-256 checksum generated & snapshot immutable | O | | O | |
| **Confirm** | | | | | |
| Return | ReportExport record created with download URI & checksum | O | O | | |
| Exception | `RPT-138`: Validation failed - unsupported format or invalid slug (HTTP 422) | | | | O |
| Database Verification | DB trigger `report_exports_no_update` blocks update attempts | | | O | |
| Log message | `MSG-082`: Report generated successfully. | O | O | | |
| | `MSG-138`: Validation failed: one or more required fields are missing or invalid. | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | B | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |
