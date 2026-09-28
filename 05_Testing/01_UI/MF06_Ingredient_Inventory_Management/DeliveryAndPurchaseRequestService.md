# Unit Test Case — DeliveryService / PurchaseRequestService (MF06 Ingredient & Inventory Management)

---

## Function: `DeliveryService.create()` / `DeliveryService.updateStatus()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF06-FUNC-005` | | Function Name | `DeliveryService.create()` / `DeliveryService.updateStatus()` |
| Created By | `Bui Quang Huy` | | Executed By | `Huy` |
| Lines of code | `TBD` | | Lack of test cases | `TBD` |
| Test requirement | `create() must reject a delivery whose scheduledAt precedes the purchaseRequest.requestedDate — rule GIAO_TRUOC_KHI_DAT (semantic-audit.ts) — with 409 ING-409 and no row written, since a delivery dated before its own purchase order breaks time-consistency of the traceability chain (BR-071). updateStatus() must raise a DeliveryDeviationDetected event with alert MSG-065 only when the actual delivery time deviates from scheduledAt by more than the configurable threshold delivery.deviation-alert-minutes (default 30, CF-M06-6 — not backed by any BR, so it must be read as configuration, not hard-coded, and must not cite the unrelated BR-135 which governs meal-service delay in MF09's domain).` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `2` | `0` | `1` | `1` | `2` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | `FX-DLV-*`: `delivery.scheduledAt = 2026-06-30` earlier than `purchaseRequest.requestedDate = 2026-07-01` | O | |
| | `FX-DLV-SCHEDULED`: actual delivery deviates **45 min** from `scheduledAt`; threshold `delivery.deviation-alert-minutes=30` | | O |
| | Comparison case (same UTCID02 test): actual delivery deviates **25 min** — below threshold | | O |
| Operation Input | `create(deliveryRequest)` | O | |
| | `updateStatus(DELAYED)` on the 45-min-deviation delivery | | O |
| | `updateStatus(...)` on the 25-min-deviation delivery | | O |
| **Confirm** | | | |
| Return | — (rejected before write) | | |
| | 45-min case: `DeliveryDeviationDetected` event raised + alert **MSG-065**: *"Delivery delay alert: Supplier delivery time deviates from schedule."* | | O |
| | 25-min case: **no** event raised, **no** alert | | O |
| Exception | `409 ING-409`, no row written | O | |
| Log message | — | | |
| | `MSG-065` asserted literally (v1.5, in TDS-02 test basis) on the deviation-detected case | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | A | B |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | | |

> UTCID mapping: UTCID01 = `M06-TC-014`, UTCID02 = `M06-TC-016`.
