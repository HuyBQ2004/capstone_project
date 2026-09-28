# Unit Test Case — AnalyticsQueryService (MF13 Reporting & Analytics)

---

## Function: `complianceSummary` / `queryReportRows`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `RPT-FUNC-002` | | Function Name | `AnalyticsQueryService.complianceSummary` / `queryRows` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `150` | | Lack of test cases | `0` |
| Test requirement | `Ensure summary metrics are calculated dynamically from source database views without hardcoded constants (BR-182), enforce student PII anonymization in query projections (BR-181), and check report access permissions by role (BR-175).` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `3` | `1` | `2` | `0` | `3` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Source Data | 48 food sample records (46 compliant, 2 overdue) | O | O | O |
| User Role | `school-admin` (Authorized for `sample-retention`) | O | O | |
| | `supplier` (Unauthorized role for analytics query) | | | O |
| Projection Fields | PII fields excluded from query projection whitelist | O | O | O |
| **Confirm** | | | | |
| Return | ComplianceSummaryDTO returned with `rate = 96` (46/48) & `alerts = 2` | O | | |
| | Query rows returned with zero student PII fields present | | O | |
| Exception | `RPT-005`: Access denied to this report type (HTTP 403) | | | O |
| Anti-Pattern Check | Verification that hardcoded `rate = 100` from mock is NOT returned | O | | |
| Log message | `MSG-005`: Access denied: You do not have permission to access this resource. | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | A | A |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |
