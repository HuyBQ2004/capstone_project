# Unit Test Case — ReportRenderer (MF13 Reporting & Analytics)

---

## Function: `renderPdf` / `renderXlsx`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `RPT-FUNC-004` | | Function Name | `PdfReportRenderer.render` / `XlsxReportRenderer.render` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `95` | | Lack of test cases | `0` |
| Test requirement | `Verify PDF and XLSX report generation with required 4-field header (title, period, school name, timestamp per BR-178), student PII anonymization, and Confidential watermark embedding (BR-181).` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `2` | `1` | `1` | `0` | `2` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Target Format | PDF report rendering (`renderPdf`) | O | O |
| | XLSX report rendering (`renderXlsx`) | O | O |
| Header Data | Title, Period (`periodFrom` - `periodTo`), School Name, Timestamp | O | O |
| Content Anonymization | Student PII Whitelist enforcement (no student names/codes) | O | O |
| Watermark Requirement | "Confidential" security watermark embedded | O | O |
| **Confirm** | | | |
| PDF Content Inspection | PDFTextStripper confirms all 4 header fields present | O | |
| XLSX Content Inspection | Apache POI confirms header row contains 4 required fields | O | |
| Privacy Verification | Verification that 0 student PII strings exist in generated file | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | N | A |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | | |
