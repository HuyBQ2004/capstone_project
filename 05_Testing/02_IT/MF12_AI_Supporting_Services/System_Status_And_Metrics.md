# Integration Test Case — System Status & Metrics (MF12 AI & Supporting Services)

---

## Feature: System Status, Activity & Usage Metrics

### 1. General Information

| Field | Value |
|---|---|
| Feature | System Status, Activity & Usage Metrics |
| Test requirement | Verify `GET /api/v1/system/{activities,status,usage,backups}` (UC-057/UC-149/UC-150) sources real data from Spring Boot Actuator rather than hardcoded constants (`ADR-M12-04`), aggregates `audit_logs` without leaking per-row `actor`/`ip`, and that no dedicated `system_metrics`/`usage_statistics`/`backups` table is ever created. |
| Number of TCs | 2 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 1 | 0 | 1 | 0 |
| Round 2 | 0 | 0 | 2 | 0 |
| Round 3 | 0 | 0 | 2 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **GET /api/v1/system/{activities,status,usage,backups}** | | | | | | | | | | | | | | |
| MF12-TC-INT-017 | Test that all 4 system endpoints return live data derived from Actuator/DB rather than hardcoded constants, and that `system/activities` never leaks per-row `actor`/`ip`. | Call all 4 endpoints with `FX-029` (SysAdmin); repeat with non-SysAdmin. | 1. All 4 endpoints → HTTP 200 with REAL data (not constants).<br>2. `GET /system/status` includes both external services (from `external_integrations`, cross-ref `MF12-TC-INT-016` case 5) AND internal services (from `/actuator/health`).<br>3. `GET /system/usage` groups by `RoleKey` kebab-case (cross-ref `MF12-TC-029`).<br>4. `GET /system/activities` aggregates from `audit_logs` — never returns per-row `actor`/`ip` (cross-ref `MF12-TC-SEC-009` case 2).<br>5. Non-SysAdmin → HTTP 403.<br>6. These 3 UCs are currently unaccepted (still hardcoded at FE) — the TC is flagged blocked and excluded from the coverage gate until the FE constants are replaced. | Actuator enabled and reachable; `external_integrations` seeded. | Pending | | | Pending | | | Pending | | | MEDIUM; ADR-M12-04; BR-165; blocked by L12/CF-8+CF-19 (3 UCs still FE-hardcoded) |
| MF12-TC-INT-018 | Test that no dedicated metric table (`system_metrics`, `usage_statistics`, `backups`) is ever created in the schema — metrics come only from Actuator. | `SELECT table_name FROM information_schema.tables WHERE table_name IN ('system_metrics','usage_statistics','backups')`. | 1. Query returns 0 rows.<br>2. Positive control (mandatory): the same test also asserts that `audit_logs`, `ai_requests`, `external_integrations`, `sent_reports`, `checklist_groups` DO exist — otherwise an empty DB would pass falsely.<br>3. The weekly BR-165 report is stored in `sent_reports(report_type='SYSTEM_HEALTH')`, not a dedicated table (cross-ref `MF12-TC-020`). | Full MF12 schema (`V1`-`V23`) applied. | Passed | 2026-08-12 | Thao | Pending | | | Pending | | | LOW; ADR-M12-04; TDS §17.1 C9 |
