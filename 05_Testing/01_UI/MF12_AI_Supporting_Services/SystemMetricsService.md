# Unit Test Case — SystemMetricsService (MF12 AI & Supporting Services)

> Group (B) — System Administration. Layer `com.sfms.mf12.metrics.*`. Metrics are sourced from Spring Boot Actuator, no dedicated `system_metrics` table (ADR-M12-04, C9). Source: `TestSpec-MF12_AI_Supporting_Services.md` §4.1 `MF12-TC-020`, `MF12-TC-029`.

---

## Function: Weekly system health report job (`@Scheduled`)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-020` | | Function Name | `SystemHealthReportJob` (`@Scheduled` weekly) |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 30 | | Lack of test cases | 0 |
| Test requirement | The system must auto-compose a weekly system-health + active-user report for System Admin, stored as `sent_reports(report_type='SYSTEM_HEALTH')`, sourced from Actuator + aggregated `audit_logs` — never from a dedicated metrics table (BR-165, `TDS §17.1 C9`). (source: MF12-TC-020) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 2 | 2 | 0 | 0 | 2 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | Job triggered manually (direct call to the `@Scheduled` method) | O | O |
| | Job triggered a second time within the SAME week | | O |
| **Confirm** | | | |
| Return | 1 row in `sent_reports`: `report_type='SYSTEM_HEALTH'`, `period='WEEKLY'`; payload contains BOTH system-health data AND active-user statistics | O | |
| | Cron expression matches a weekly cycle (assert the expression, not real elapsed time) | O | |
| | Second run in the same week does NOT create a duplicate row (idempotent per period) | | O |
| | Figures sourced from Actuator + `audit_logs` aggregation, not from a separate metrics table | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | N | N |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | |

---

## Function: `SystemMetricsService.usage()` — `RoleKey` kebab-case grouping

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-021` | | Function Name | `SystemMetricsService.usage` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 20 | | Lack of test cases | 0 |
| Test requirement | Usage statistics (UC-150) must group by the shared `RoleKey` enum in kebab-case (`system-admin`, `school-admin`, `teacher`, `parent`, `medical-staff`, `service-caterers`, `supplier`) from `common/enums/`, not by a locally-declared or human-readable label (`00_Common_Conventions.md` D6). (source: MF12-TC-029) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 2 | 2 | 0 | 0 | 2 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | `GET /system/usage` called | O | O |
| Check target | Set of role-group keys in the response | O | |
| | Source of the `RoleKey` type used by the module | | O |
| **Confirm** | | | |
| Return | Exactly 7 keys, all kebab-case, matching D6's `RoleKey` 100% | O | |
| | No `SCREAMING_SNAKE` label and no human-readable label (e.g. `"Medical Staff"`) used as a key | O | |
| | Module imports `common/enums/RoleKey` — no local `enum RoleKey` declared inside `com.sfms.mf12` (`grep -rn "enum RoleKey" src/main/java/com/sfms/mf12/` → no output) | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | N | N |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | |

Note: currently `⚠️ BLOCKED by L12/CF-19` in the TestSpec — the FE `usage-statistics` screen still hardcodes `ROLE_KEYS` (no backend API yet). Test is written and kept `🔴 RED` until the endpoint exists.
