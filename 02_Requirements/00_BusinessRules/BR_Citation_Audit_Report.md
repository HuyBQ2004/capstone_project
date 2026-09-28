# BR Citation Audit — Report 3 UC Descriptions vs. Canonical `business_rules.md`

> Status: **Findings only — no files modified.** This answers "are the current UCs correctly linked to the new canonical BR/MSG tables yet?" The answer is **no**. This report quantifies the gap and proposes a resolution per code; it is meant for team review before any file is edited.

## 1. Scope and method

Every `| Business Rules |` row across Report 3's 164 detailed UC description sections was extracted and each cited `BR-xxx` code checked against the 129 codes actually defined in `02_Requirements/00_BusinessRules/business_rules.md` (the confirmed canonical source). `| Exceptions |` rows (MSG citations) were checked the same way against `error_message.md`'s 76 codes.

## 2. Headline numbers

- **164** UC sections have a Business Rules row.
- **65 sections (39.6%)** cite at least one BR code that does not exist in canonical `business_rules.md`.
- **37 distinct "ghost" BR codes** are in circulation across those 65 sections (BR-015, 016–019, 033–039, 042–044, 049, 052–054, 058, 084–088, 115, 117–119, 122, 126–129, 131, 132, 134).
- **MSG citations are clean** — 0 non-existent MSG codes found in any Exceptions row.

## 3. Where the ghost codes came from

Report 3 itself contains an older, deprecated "Requirement Appendix → Business Rules" table (~line 6223, already flagged as non-authoritative). That old table still defines BR-001 through BR-165 under a **different numbering/consolidation scheme** than the canonical `business_rules.md`. The 37 ghost codes above are exactly the old numbers that never got reconciled when the canonical file was created — the UC description sections (and in a few cases traceability_matrix's reverse-BR-map) were never updated to point at the new numbers.

Cross-referencing the old appendix's definition of each ghost code against canonical `business_rules.md`'s definitions sorts them into three buckets:

## 4. Bucket A — Clean rename (old code ≡ new code, same rule, safe to auto-replace)

| Old code (cited in Report 3) | Old definition (Report 3's deprecated appendix) | → New canonical code | UC sections affected |
|---|---|---|---|
| BR-015 | Password history; cannot reuse last 3 passwords | **BR-006** (2nd bullet folds this in; canonical itself has a dangling internal "(BR-015)" reference — worth cleaning up too) | UC-011, UC-032 |
| BR-019 | Classroom capacity max 50 students | **BR-025** | UC-018, UC-037, UC-165, UC-166, UC-090, UC-025 |
| BR-035 | Allergy declarations verified/approved by Medical Staff | **BR-031** | UC-036, UC-051, UC-077, UC-078, UC-084, UC-093, UC-094, UC-104 |
| BR-042 | Dietary categories: Halal/Vegetarian/Gluten-Free/Nut-Free | **BR-066** | UC-036, UC-078, UC-084, UC-023 |
| BR-044 | Bulk import validates email/phone/duplicate IDs | **BR-032** | UC-010 |
| BR-087 | Delivery batch records: lot #, packaging date, cert links | **BR-071** | UC-015, UC-039, UC-043, UC-060, UC-081, UC-083, UC-100, UC-107 |
| BR-115 | Retention records linked to inspection log and batches | **BR-114** | UC-069, UC-076, UC-118 |
| BR-117 | Photo evidence of labeled/sealed samples at registration | **BR-107** | UC-069, UC-118 |
| BR-118 | GPS verification for sample registration in kitchen | **BR-109** | UC-069 |
| BR-122 | Students with allergies served custom labeled meals | **BR-065** | UC-016, UC-103, UC-132, UC-141, UC-142, UC-143 |
| BR-127 | Classroom meal distribution confirmed by homeroom teacher (mobile) | **BR-120** | UC-016, UC-132 |
| BR-128 | Student meal consumption rates recorded for special-diet students | **BR-121** | UC-016, UC-053, UC-056, UC-080 |

**12 codes, high confidence — these can likely be mechanically renamed once approved.**

## 5. Bucket B — Approximate match (related canonical rule exists, but scope/wording differs — needs a judgment call, not a mechanical rename)

| Old code | Old definition | Closest canonical candidate | Why it's not a clean match |
|---|---|---|---|
| BR-034 | Severe allergy requires medical documentation upload | BR-031 (health info review) | Canonical BR-031 doesn't mention a documentation *requirement* tied to severity — narrower claim than the old rule. |
| BR-038 | Allergy alerts highlighted red on teacher's screen | BR-123 (visual+audio alert on ID scan) | Same intent (warn about allergy) but different trigger/UI detail; canonical version is scan-based, old is a persistent screen highlight. |
| BR-039 | Verified health history logs cannot be deleted, only appended | BR-172 (log integrity, system-wide) | Canonical rule is generic audit-log immutability, not specific to student health records. |
| BR-054 | Delivery schedule deviations >30 min trigger alerts | BR-135 (meal-serving delay, not delivery) | Old rule is about supplier *delivery* timing; canonical BR-135 is about *meal-serving* delay. Different process. MSG-065 ("Delivery delay") currently also cites BR-135 — same ambiguity, same root cause. |
| BR-084 | Purchase requests auto-calculate qty from stock vs. menu need | BR-077 (low-stock alert) | Related but not the same claim (auto-calculation vs. alerting). |
| BR-086 | Suppliers submit digital delivery schedule/batch details 24h prior | BR-071 (batch certification) | Canonical doesn't mention the 24h advance-notice timing requirement. |
| BR-119 | Weekly food sample storage audits by medical staff | BR-110 (fridge temp monitoring/alerts) | Canonical is about continuous temperature monitoring, not a weekly audit cadence. |
| BR-132 | Allergy-safe meals require dual confirmation scans | BR-123 (allergy alert on scan) | Canonical doesn't mention a "dual confirmation" (kitchen + teacher) requirement. |
| BR-134 | Dining area/kitchen sanitation checklist after serving hours | BR-100 (daily hygiene score) | Canonical is a score metric, not a post-service checklist procedure. |

**9 codes — someone with product context should decide: tighten canonical BR-xxx's wording to cover the old scope, write a new BR, or accept the citation as approximate.**

## 6. Bucket C — Rule appears genuinely dropped (no canonical equivalent at all)

| Old code | Old definition | UC sections still citing it |
|---|---|---|
| BR-016 | School registration requires tax ID, permit number, official email | UC-070 |
| BR-017 | New schools default to "Pending" status | UC-070, UC-067, UC-071, UC-152, UC-170, UC-156, UC-158 |
| BR-018 | School profile modifications require review/approval | UC-095 |
| BR-033 | Student health declarations updated yearly | UC-036, UC-050, UC-077, UC-084, UC-093 |
| BR-036 | Unique school registration number per student | UC-025, UC-099, UC-147 |
| BR-037 | Special dietary plan changes submitted 24h prior | UC-036, UC-078, UC-084 |
| BR-043 | Emergency medical info accessible to medical staff offline | UC-036, UC-051, UC-084, UC-094, UC-104, UC-146 |
| BR-049 | Catering site inspection rating ≥80% required for approval | UC-004, UC-067, UC-156, UC-157, UC-158 |
| BR-052 | Supplier performance index calculated monthly | UC-108, UC-127, UC-131, UC-148 |
| BR-053 | Suppliers <70% index for 2 months flagged for suspension | UC-108, UC-127, UC-131, UC-148 |
| BR-058 | Only 1 Service Caterer contracted per school at a time | UC-004 |
| BR-085 | Purchase requests >$5,000 require dual authorization | UC-021, UC-139 |
| BR-088 | Physical stock audits conducted weekly | UC-109 |
| BR-126 | Cold foods served below 10°C | UC-047 |
| BR-129 | Unserved meals counted/logged as waste | UC-047 |

**15 codes.** Note MSG-060 ("Supplier monthly performance index score has dropped below 70%") already has no BR reference in the canonical MSG table either — it's the same underlying gap as BR-052/BR-053. Someone needs to decide, per rule: was this intentionally cut from scope, or does it need to be re-added to `business_rules.md` as a new BR (e.g. BR-183+)?

## 7. Bucket D — Actual contradiction, not just a numbering gap (highest priority)

| Old code | Old definition | Conflicts with |
|---|---|---|
| BR-131 | "If barcode is unavailable, biometric face verification must be used to authorize **student** meal distribution." (cited in UC-016 Confirm Meal Distribution) | **BR-173** (canonical): "Children's (students') biometric data **must not be collected**, for personal data protection reasons" — and BR-051 makes the same point for delivery staff specifically excluding children. |

This is not a citation typo — it's a leftover requirement from an earlier design that directly contradicts a rule the team has since adopted (no biometric collection from minors, PDPD compliance). UC-016's Normal Flow should be re-checked to confirm it doesn't actually describe biometric scanning of students anywhere in its body text (only the BR citation was checked in this pass). **Recommend resolving this explicitly with the team before touching UC-016**, since it may mean the fallback-to-biometric flow described needs to be redesigned (e.g., fallback to manual teacher confirmation instead).

## 8. Execution log (2026-07-01, per team decision "theo BR mới")

Per instruction, all four buckets were resolved by adopting canonical `business_rules.md` everywhere:

- **Bucket A (12 codes)** — renamed across all citations in Report 3.md (structured Business Rules rows + inline prose) and traceability_matrix.md.
- **Bucket B (9 codes)** — renamed to their closest canonical match (same mechanism as Bucket A, since the team chose to follow canonical over preserving old scope nuance).
- **Bucket C (16 codes, incl. BR-045 found in a later full-file re-scan)** — citations removed from structured fields and inline prose; where an entire "System Function" row's only citation was a dropped code (Supplier Deactivation Cascade, BR-059), the citation was replaced with an inline note pointing back to this report instead of silently deleting the described behavior.
- **Bucket D (BR-131)** — citation removed from UC-016 (Confirm Meal Distribution). Its Normal Flow/Alternative Flows body text was checked directly and contains no description of biometric authentication for students at all (nor does UC-047 Manage Meal Service) — the old BR-131 citation had no corresponding body content to reconcile, so removal was sufficient.

**Verified**: zero invalid BR codes remain anywhere in Report 3.md before the deprecated appendix (line 6223) or anywhere in traceability_matrix.md, except inside historical changelog/footnote text that documents the fix itself.

**Also discovered and fixed during execution** (beyond the original 37-code list, found via a full-file re-scan): BR-045 (dropped, "parent notification channels configured during onboarding" — no canonical equivalent) and confirmed BR-084 was Bucket B (→BR-077), not Bucket C as initially miscategorized in a draft pass — corrected to match this report's original classification.
