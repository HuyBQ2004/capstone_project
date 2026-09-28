# Integration Test Case — Analytics Dashboard (MF13 Reporting & Analytics)

---

## Feature: Analytics Dashboard & Metric Queries

### 1. General Information

| Field | Value |
|---|---|
| Feature | Analytics Dashboard & Metric Queries |
| Test requirement | Verify 7 database views (`v_rpt_*`), live source metric calculation (BR-182), ISO-8601 date formatting (D4), dynamic cache TTL & event-driven eviction (BR-180), and report catalog listing (UC-171). |
| Number of TCs | 6 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 6 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 6 | 0 |
| Round 3 | 0 | 0 | 6 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Database View Infrastructure** | | | | | | | | | | | | | | |
| MF13-TC-INT-001 | Test infrastructure gate: verify existence and column structure of all 7 database views (`v_rpt_*`). | 1. Query database metadata for views `v_rpt_three_step`, `v_rpt_sample_retention`, `v_rpt_receiving`, `v_rpt_supplier_certificates`, `v_rpt_hygiene`, `v_rpt_traceability`, `v_rpt_system_usage`.<br>2. Execute `SELECT * FROM <view> LIMIT 1` for each view. | 1. All 7 database views exist and compile cleanly without column resolution errors.<br>2. Projections match `ReportRowProjection` whitelists. | Migration V1-V23 executed. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | ★ Infrastructure gate |
| **GET /api/v1/reports/compliance-summary** | | | | | | | | | | | | | | |
| MF13-TC-INT-002 | Test live compliance summary calculation from source database records (BR-182). | 1. Seed 48 food sample records (46 compliant, 2 overdue).<br>2. Send GET to `/api/v1/reports/compliance-summary`. | 1. Returns HTTP 200 OK.<br>2. JSON response contains `sampleRetention: {rate: 96, alerts: 2}`.<br>3. Verifies metric is dynamically calculated (rejects hardcoded mock constant `rate: 100`). | Seed data initialized. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-182 dynamic calculation |
| MF13-TC-INT-003 | Test date and time response format compliance (D4 ISO-8601). | 1. Send GET to `/api/v1/reports/compliance-summary`.<br>2. Inspect timestamp formats. | 1. `generatedAt` formatted as ISO-8601 UTC string (`yyyy-MM-dd'T'HH:mm:ss'Z'`).<br>2. `periodFrom` and `periodTo` formatted as ISO date strings (`yyyy-MM-dd`).<br>3. Zero occurrences of non-standard date formats (`dd/MM/yyyy`). | Authenticated session active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | D4 ISO-8601 compliance |
| MF13-TC-INT-005 | Test dashboard metric caching (240s TTL) and event-driven cache eviction. | 1. Send GET to `/compliance-summary` and record `generatedAt` timestamp.<br>2. Call endpoint again within 240s -> cached response returned.<br>3. Publish `FoodSampleCreated` domain event.<br>4. Call endpoint again -> cache evicted, fresh timestamp returned. | 1. Step 2 returns cached payload with identical `generatedAt`.<br>2. Step 4 returns fresh calculation with updated timestamp within 240s. | Cache manager active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | BR-180 cache eviction |
| **GET /api/v1/reports/catalog** | | | | | | | | | | | | | | |
| MF13-TC-INT-012 | Test report catalog listing filtered by user role permissions (UC-171). | 1. Login as School Admin and call `GET /api/v1/reports/catalog`.<br>2. Login as Medical Staff and call endpoint. | 1. Step 1 returns catalog items for authorized report types.<br>2. Step 2 returns filtered catalog matching Medical Staff permissions. | User sessions active. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | UC-171 catalog listing |
| **GET /api/v1/report-exports** | | | | | | | | | | | | | | |
| MF13-TC-INT-010 | Test export history listing pagination and search contract. | 1. Send GET to `/api/v1/report-exports`.<br>2. Send GET with search `/api/v1/report-exports?search=RPX-2026`. | 1. Returns 5 pagination fields (`items`, `total`, `page`, `pageSize`, `pageCount`).<br>2. Search filters on `code`, `report_title`, `period_label`. | Export history records seeded. | Passed | 2026-08-05 | Nam | Pending | | | Pending | | | 5-field pagination contract |
