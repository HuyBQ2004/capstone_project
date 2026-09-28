# UC-ID Reconciliation Report

> Scope: verifies the user's claim that "bảng Use Case List sai lệch ID so với UC Description" (the Use Case List table's IDs don't match the detailed UC descriptions). All findings below are grep/read-verified against `06_Document/01_Report/Report 3.md` and `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` — none are inferred.
>
> Report 3 has **three** independent UC-ID registries that are supposed to agree and currently don't:
> 1. The "Use Cases Description" master summary table (~line 1005–1178, 166 rows).
> 2. The "UC ID and Name" header row inside each of the detailed Functional Specification sections (~line 1178–4794, 164 sections).
> 3. `traceability_matrix.md`'s own UC↔BR↔MSG tables (§1.1–1.13, 166 rows).

---

## 1. Confirmed defects — Report 3 internal (master table vs. its own detail sections)

These exist **inside Report 3 alone** — they are not cross-document disagreements, they are the document contradicting itself.

| # | ID | Master table says (line) | Detail section says (line) | Verdict |
|---|---|---|---|---|
| 1 | **UC-037** | `Manage Compliance Documents` (41) — Raw Food Supplier | Two sections both claim UC-037: `Manage Compliance Documents` (2268, ✅ matches) **and** `Manage Classrooms` (1670, ❌) | "Manage Classrooms" has **no row anywhere** in the 166-row master table under any ID — it's an orphan section that squatted on UC-037. |
| 2 | **UC-059** | `Record Corrective Actions Report` (63) — Medical Staff | Two sections both claim UC-059: `Record Corrective Actions Report` (4356, ✅ matches) **and** `Profile Manage` (1406, ❌) | "Profile Manage" has **no row anywhere** in the master table under any ID — an orphan section squatting on UC-059. |
| 3 | **UC-153** | `Track Supplier Application Status` (157) — Raw Food Supplier | Two sections both claim UC-153: `Track Supplier Application Status` (2574, ✅ matches) **and** `View User Account List` (1560, ❌) | The master table has **already renumbered** "View User Account List" to **UC-151** (line 155), but the detail section heading was never updated — it still says UC-153. |
| 4 | **UC-154** | `Update/Resubmit Supplier Application` (158) — Raw Food Supplier | Two sections both claim UC-154: `Update/Resubmit Supplier Application` (2594, ✅ matches) **and** `Activate Account & Set Password` (1582, ❌) | The master table has **already renumbered** "Activate Account & Set Password" to **UC-160** (line 164, note: written as `UC- 160` with a stray space — a second, smaller bug), but the detail section heading was never updated. |
| 5 | **UC-165 / UC-166** | UC-165 = `View List Classrooms`, UC-166 = `View Classroom Detail` (169–170) | Both detail sections are titled `View Classroom Detail` (1692 and 1714) | UC-165's detail section has the wrong title — almost certainly a copy‑paste of UC-166's heading. Needs its body checked and the heading corrected to "View List Classrooms." |
| 6 | **UC-137** | `View Policy Of School` (line 1145) — Teacher/Medical Staff/Service Caterers | **No detail section exists for UC-137 at all** | This UC has a master-table row but was never written up in the Functional Specifications. Genuine missing content, not a mislabeling. |

**Net effect**: 166 master-table rows, but only 164 detail sections, covering only 160 distinct IDs — because 4 IDs are double-claimed (rows 1–4 above) and 2 IDs (UC-151, UC-160) have no section of their own, while their real content is trapped under the wrong, already-superseded ID.

---

## 2. Confirmed defects — Report 3 vs. `traceability_matrix.md`

| # | ID(s) | Report 3 says | `traceability_matrix.md` says | Verdict |
|---|---|---|---|---|
| 7 | **UC-161 / UC-162 / UC-163** | Meal Feedback module: `View List Meal Feedback` / `View Meal Feedback Detail` / `Respond Meal Feedback` (master table + detail sections at 4046/4068/4090, all internally consistent) | Reporting & Analytics module (§1.13): `View Analytics Dashboard` / `Export Report` / `Schedule Automated Report` | **Direct ID collision between two entire feature modules.** Neither source is "wrong" in isolation — they were extended independently and landed on the same numbers. |
| 8 | **UC-164** | `View Meal Feedback Dashboard` (master table + detail section at 4112) | Does not exist | Report 3 has this UC; traceability_matrix has never been extended to cover it. |
| 9 | Reporting & Analytics UCs (Analytics Dashboard / Export Report / Schedule Report) | No Functional Specification section exists anywhere in Report 3 | Fully specified in traceability_matrix §1.13 with BR/MSG mappings | These 3 UCs exist only as a traceability_matrix table row — they were never written up as Report 3 Functional Specs. This is the mirror image of finding #6. |
| 10 | **UC-038** used for `View Policy Of School` | Master table actually assigns `View Policy Of School` to **UC-137**, consistently with no detail-section conflict | traceability_matrix §1.2 still calls it `UC-038 (Policy)` | traceability_matrix is using a stale ID here — Report 3 already moved this UC to 137 (and reused 038 for "Manage Corrective Actions" cleanly), but traceability_matrix wasn't updated to follow. |

## 3. Already self-documented (traceability_matrix's own Gap list, cross-checked and confirmed correct)

`traceability_matrix.md` already carries a "Ghi chú — Các Gap còn tồn tại" table (GAP-001–006). Cross-checking it against the canonical `business_rules.md` / `error_message.md` (the task just completed for the new EN tables) confirms every one of these is real and still unresolved:

- GAP-001/GAP-002/GAP-003/GAP-004 — the same MSG-075/MSG-019/MSG-024/MSG-015 BR-citation errors independently found while building `Report3_SystemMessages_EN.md` (see notes ①–⑥ in that file). Good corroboration — two independent passes agree.
- GAP-005 — BR-019 ambiguity: confirmed BR-019 **does not exist** in canonical `business_rules.md` at all (dropped some time after traceability_matrix v1.0 was written), so GAP-005 should now read "delete the BR-019 references in traceability_matrix §1.2/§2, they refer to a rule that was removed," not "clarify whether it's a separate rule."
- GAP-006 — UC-059B needing an official number: finding #2 above shows the collision GAP-006 wanted to prevent has **already happened** (Report 3 assigned plain "UC-059" to Record Corrective Actions Report), so this gap is more urgent than its current wording suggests.

---

## 4. What this means for the user's three asks

1. **New EN BR/MSG tables** — done, see `Report3_BusinessRules_EN.md` and `Report3_SystemMessages_EN.md` in this folder. Both are row-count-verified against the canonical sources (129 BR, 76 MSG).
2. **Re-tagging BR/MSG references inside the 166 UC descriptions correctly** — see methodology in §5 below. This cannot be done safely *before* the ID collisions in §1–2 are resolved, because "which BR applies to UC-059" is meaningless while UC-059 refers to two different use cases.
3. **Fixing the Use Case List summary table** — the master table is actually **more correct** than the detail sections in 4 of 6 internal defects (rows 1–4: it already has the right final ID, the detail section is what's stale). So the fix direction is mostly "update detail-section headings to match the master table," plus two real content gaps (UC-137, UC-164/161-163 collision) that need a team decision, not a mechanical fix.

---

## 5b. Resolution executed 2026-07-01

Per team decision, **the UC ID printed in each detail section's "UC ID and Name" row (Report 3 Functional Specifications) is now the single source of truth** — not the master summary table, not traceability_matrix's own prior numbering. Applied:

| Content | Old ID(s) | New/kept ID | Rule applied |
|---|---|---|---|
| Manage Classrooms | UC-037 (contested) | **UC-037** (kept — earliest occurrence) | |
| Manage Compliance Documents | UC-037 (contested) | **UC-167** (renumbered) | |
| Profile Manage | UC-059 (contested) | **UC-059** (kept — earliest occurrence) | |
| Record Corrective Actions Report | UC-059 (contested) / UC-059B (traceability) | **UC-168** (renumbered) | |
| View User Account List | UC-153 (contested) / UC-151 (traceability) | **UC-153** (kept — earliest occurrence) | |
| Activate Account & Set Password | UC-154 (contested) / UC-160 (traceability) | **UC-154** (kept — earliest occurrence) | |
| Track Supplier Application Status | UC-153 (contested) | **UC-169** (renumbered) | |
| Update/Resubmit Supplier Application | UC-154 (contested) | **UC-170** (renumbered) | |
| View Classroom Detail | mislabeled "UC-165 heading said List, UC-166 body was actually List content" | **UC-165** (body content = detail, kept) | content-swap fix, see below |
| View List Classrooms | same cluster | **UC-166** (body content = list, name corrected) | content-swap fix |
| View Policy Of School | UC-038 (Policy) per traceability | **UC-137** (matches Report 3 master table; no detail section exists yet) | |
| View Analytics Dashboard / Export Report / Schedule Automated Report | UC-161/162/163 (traceability, Reporting & Analytics) | **UC-171/172/173** | freed 161-163, which Report 3's UC description already legitimately uses for Meal Feedback |
| View List/Detail Meal Feedback, Respond, Dashboard | existed only in Report 3, missing from traceability | **UC-161/162/163/164** (added to traceability §1.10) | |

**Content-swap note (UC-165/166)**: both sections' *bodies* were checked directly. UC-165's body genuinely describes viewing one classroom's detail (matches its own ID row), but its H4 heading/table caption wrongly said "View List Classroom" — heading fixed to match. UC-166's ID row said "View Classroom Detail" but its body was a verbatim copy of "Manage Classrooms" (list/portal content) — body content, not the ID, was taken as ground truth here since two sections cannot legitimately share one declared name; UC-166 was renamed to "View List Classrooms" to match what its body actually describes.

**Files edited**: `06_Document/01_Report/Report 3.md` (master table + 4 duplicate headings + UC-165/166 heading/caption swap + one internal cross-reference at the "Manage Supplier Application" flow), `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` (all corresponding ID updates, 3 new rows added to §1.2, 4 new rows added to §1.10, §1.13 renumbered, GAP list updated, module coverage totals recalculated: 166 → 173 UC).

**Verified**: no duplicate "UC ID and Name" headers remain in Report 3; no duplicate UC-ID rows remain in either master table or traceability_matrix. Report 3 now has 168 master-table rows and 164 detail sections — the 4-row gap is fully accounted for (UC-137, UC-171, UC-172, UC-173 have master/traceability entries but no written Functional Specification yet — pre-existing content gaps, not ID errors).

**Update 2026-07-01 (same day)**: confirmed with the team that no real code exists yet, so the implementation-layer docs were synced too. `07_implement/MF01_User_Management_Access_Control/TDS-001_User_Management.md`, `ADR-001_User_Auth_Architecture.md`, and `TestSpec-001_User_Management.md` all referenced **UC-160**/**UC-151** (state diagrams, sequence diagrams, API endpoint docs, test oracles) — all instances renamed to **UC-154**/**UC-153** respectively. Verified via repo-wide grep: no file anywhere still references UC-151, UC-160, or UC-059B (aside from this report's own history log and the traceability_matrix changelog line, both intentionally historical).

---

## 5. Original proposed methodology (superseded by §5b above, kept for history)

**Phase A — Fix the internal Report 3 defects (mechanical, low-risk, I can do this directly):**
- Rename the detail-section heading "UC-153 - View User Account List" → "UC-151 - View User Account List" (line 1560).
- Rename the detail-section heading "UC-154 - Activate Account & Set Password" → "UC-160 - Activate Account & Set Password" (line 1582).
- Fix "UC- 160" stray space in the master table (line 164).
- Rename the orphan section "UC-037 - Manage Classrooms" to a new unused ID and add a corresponding master-table row (or confirm with you whether "Manage Classrooms" is even still needed — UC-018 Create Classroom and UC-090 Update Classroom already exist; "Manage Classrooms" may be a superseded draft section that should simply be deleted).
- Same treatment for the orphan "UC-059 - Profile Manage" (likely superseded by UC-097 "Update Profile" + UC-138 "View Profile" — may be a leftover draft to delete rather than renumber).
- Fix the UC-165 heading title (`View Classroom Detail` → `View List Classrooms`) after confirming its body content actually matches "list," not "detail."

**Phase B — Decide the cross-document collisions (needs a team decision, not something I should silently resolve):**
- UC-161/162/163: either renumber traceability_matrix's Reporting & Analytics UCs to free numbers (e.g., UC-167/168/169, continuing the sequence) and write their missing Functional Spec sections in Report 3, or renumber Report 3's Meal Feedback UCs instead — whichever direction, one side has to move.
- UC-137 vs traceability_matrix's "UC-038 (Policy)": adopt UC-137 everywhere (Report 3's version looks like the more recent, conflict-free one) and write its missing Functional Spec section.
- UC-164 needs to be added to traceability_matrix.

**Phase C — Re-verify BR/MSG citations inside all 166 UC description sections against the canonical `business_rules.md`/`error_message.md`, using the *reconciled* traceability_matrix (after Phase A+B) as the cross-check baseline.** This is a large, mechanical-but-tedious pass — realistically 160+ sections to check one by one. I'd do it as: extract each section's cited BR-xxx/MSG-xxx codes → check the code exists in the canonical sources → check it's consistent with what traceability_matrix now says for that UC → flag/fix mismatches. I can batch this with a script to extract citations first, then go through corrections in one pass, but it's the single biggest remaining piece of work in this whole thread — worth confirming you want it done as one big pass rather than module-by-module.

**My recommendation**: do Phase A now (mechanical, no judgment calls beyond the two "delete vs. renumber" orphan questions), get your call on Phase B's two renumbering directions, then run Phase C.
