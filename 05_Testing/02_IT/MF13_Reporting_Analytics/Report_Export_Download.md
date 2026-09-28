# Integration Test Case — Report Export & Download (MF13 Reporting & Analytics)

---

## Feature: Report Export & Download

### 1. General Information

| Field | Value |
|---|---|
| Feature | Report Export & Download |
| Test requirement | Verify PDF and XLSX export generation (BR-178), empty-period handling, 4-field header layout, SHA-256 checksum integrity, append-only immutability, and 12-month retention purge job (BR-177). |
| Number of TCs | 5 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 5 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 5 | 0 |
| Round 3 | 0 | 0 | 5 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **POST /api/v1/reports/{slug}/export** | | | | | | | | | | | | | | |
| MF13-TC-INT-004 | Test report export generation for period with zero records (empty period). | 1. Send POST to `/api/v1/reports/sample-retention/export` specifying a date range with zero database records. | 1. Returns HTTP 201 Created.<br>2. Response body contains `rowCount: 0`.<br>3. Generated PDF/XLSX file contains complete 4-field header layout (BR-178). | Authenticated session active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-178 empty period handling |
| MF13-TC-INT-006 | Test PDF and XLSX export generation with required 4-field header layout. | 1. Send POST to `/export` specifying `format = "PDF"`.<br>2. Send POST specifying `format = "XLSX"`.<br>3. Inspect generated file headers using PDFTextStripper and Apache POI. | 1. Both requests return HTTP 201 Created.<br>2. PDF and XLSX files contain all 4 mandatory headers: Report Title, Period, School Name, Generated Timestamp (BR-178). | Authenticated session active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-178 4-field header |
| **GET /api/v1/report-exports/{code}/file** | | | | | | | | | | | | | | |
| MF13-TC-INT-007 | Test report download and SHA-256 checksum verification. | 1. Export a report to obtain `code` (`RPX-2026-001`) and `checksumSha256`.<br>2. Send GET to `/api/v1/report-exports/RPX-2026-001/file`.<br>3. Compute SHA-256 hash of downloaded file bytes. | 1. Returns HTTP 200 OK with binary stream (`application/pdf` or `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`).<br>2. Computed SHA-256 hash matches stored `checksumSha256` value exactly. | Export record active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | SHA-256 checksum integrity |
| **Retention & Immutability Jobs** | | | | | | | | | | | | | | |
| MF13-TC-INT-011 | Test scheduled purge job for exports exceeding 12-month retention period (BR-177). | 1. Seed export record `RPX-EXPIRED` with `expires_at = now() - 1 day` (25 months old).<br>2. Trigger scheduled purge job (`ReportPurgeRunner`).<br>3. Attempt to download `RPX-EXPIRED`. | 1. Purge job deletes underlying physical file and updates DB record status to `PURGED`.<br>2. Step 3 returns HTTP 410 Gone with `error.code = "RPT-410"`.<br>3. Audit log records `REPORT_EXPORT_PURGED`. | Expired export record seeded in DB. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-177 purge job |
| MF13-TC-INT-014 | Test append-only database trigger immutability for export records (BR-182). | 1. Attempt SQL UPDATE statement against `report_exports` table for an existing record. | 1. Database trigger `report_exports_no_update` blocks update with SQL error.<br>2. Export record snapshot remains unmodified. | Active export record in DB. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-182 append-only immutability |
