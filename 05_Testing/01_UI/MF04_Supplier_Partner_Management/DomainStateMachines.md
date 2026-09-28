# Unit Test Case — DocumentStatusCalculator / PartnerStatus FSM (MF04 Supplier & Partner Management)

---

## Function: `DocumentStatusCalculator.statusOf()` — 180/30-day expiry thresholds (BR-047)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-004` | | Function Name | `DocumentStatusCalculator.statusOf(PartnerDocument, LocalDate)` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 22 | | Lack of test cases | 0 |
| Test requirement | Compliance document status must derive from the expiry date using the legal thresholds in BR-047: below 180 days → `EXPIRING`, below 30 days → `URGENT`, past the date → `EXPIRED`, `null` (no expiry) → `VALID`. Thresholds trace to Law on Food Safety 2010 Art. 37 (3-year certificate validity, renewal filed 6 months ahead). `Clock` is injected (FX-014, fixed at `2026-07-28`) — never `LocalDate.now()` directly. Source: MF04-TC-004, TC-COND-004. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 8 | 0 | 0 | 2 | 0 | 6 | 8 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | |
| `expiryDate` | `null` | O | | | | | | | |
| | `2026-07-27` (−1 day) | | O | | | | | | |
| | `2026-07-28` (0 day, = TODAY) | | | O | | | | | |
| | `2026-08-27` (+30 days) | | | | O | | | | |
| | `2026-08-28` (+31 days) | | | | | O | | | |
| | `2027-01-24` (+180 days) | | | | | | O | | |
| | `2027-01-25` (+181 days) | | | | | | | O | |
| | `2027-03-15` (+595 days, `DOC-GF-01`) | | | | | | | | O |
| **Confirm** | | | | | | | | | |
| Return | `VALID` ("no expiry" case) | O | | | | | | | O |
| | `EXPIRED` | | O | | | | | | |
| | `URGENT` (lower boundary — expiring today is not yet EXPIRED) | | | O | O | | | | |
| | `EXPIRING` (upper boundary of URGENT is exclusive, upper boundary of EXPIRING is inclusive) | | | | | O | O | | |
| | `VALID` (just outside EXPIRING) | | | | | | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | B | B | B | B | B | B | N | N |
| Passed/Failed | Passed | Passed | Passed | Passed | Passed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | | | |

Note: If UTCID04/UTCID06 collapse to `VALID` — the exact bug the mock FE has today, which applies a single 30-day threshold for both "expiring soon" and "urgent" — a supplier loses roughly 5 months of the legally-mandated 6-month renewal window (BR-047, Art. 37) before anyone is warned. If UTCID01 throws instead of returning `VALID`, the whole BR-047 scan job dies on the first document without an expiry date (seed `DOC-GF-02`), silently starving every document scanned afterward of its warning (see `MF04-TC-INT-009`). `Clock` must stay dependency-injected — hardcoding `LocalDate.now()` makes this suite pass or fail depending on the day it runs in CI.

---

## Function: `PartnerStatusTransition.assertAllowed()` — partner lifecycle FSM (BR-041 + SUSPENDED)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-010` | | Function Name | `PartnerStatusTransition.assertAllowed(from, to)` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 20 | | Lack of test cases | 0 |
| Test requirement | Partner lifecycle must follow the state machine `PENDING → APPROVED/REJECTED`, `APPROVED ⇄ SUSPENDED`, `REJECTED → PENDING` (resubmit) and reject every other transition (BR-041, ADR-M04-6, constraint C5 of `data_model.puml`). Source: MF04-TC-010, TC-COND-008. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 10 | 5 | 5 | 0 | 10 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 |
|---|---|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | | | |
| Transition | `PENDING → APPROVED` | O | | | | | | | | | |
| | `PENDING → REJECTED` | | O | | | | | | | | |
| | `REJECTED → PENDING` | | | O | | | | | | | |
| | `APPROVED → SUSPENDED` | | | | O | | | | | | |
| | `SUSPENDED → APPROVED` | | | | | O | | | | | |
| | `PENDING → SUSPENDED` | | | | | | O | | | | |
| | `REJECTED → APPROVED` | | | | | | | O | | | |
| | `REJECTED → SUSPENDED` | | | | | | | | O | | |
| | `APPROVED → PENDING` | | | | | | | | | O | |
| | `SUSPENDED → REJECTED` | | | | | | | | | | O |
| **Confirm** | | | | | | | | | | | |
| Return | Transition allowed, no exception | O | O | O | O | O | | | | | |
| Exception | `IllegalStateTransitionException` | | | | | | O | O | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 |
|---|---|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | N | N | N | N | N | A | A | A | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | | | | | |

Note: Today the mock updates `partners.status` through a plain `PUT /partners/:id` with any value in the body — there is no guard at all. If this test regresses to that behavior, a partner can be pushed straight from `REJECTED` to `SUSPENDED` (a transition with no business meaning), or an `APPROVED` partner can be silently walked back to `PENDING`, quietly revoking its right to deliver food (BR-041) without going through reject/reason/notification. `SUSPENDED` only has an official BR to back it (via ADR-M04-6) since 2026-08-02 — before that it depended solely on constraint C5 of `data_model.puml`.
