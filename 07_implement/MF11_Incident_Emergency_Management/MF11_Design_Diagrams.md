# MF11 — Incident & Emergency Management — Design Diagrams

| Field | Value |
|---|---|
| **Module** | MF11 — Incident & Emergency Management (merged with Violation Management) |
| **Derived from** | `TDS-MF11_Backend.md` (v1.2, 2026-08-04) — §5 Static Modeling, §6 Dynamic Modeling, §8 Interface Specification, §9 API Specification |
| **Notation** | DBML (database), PlantUML (class + sequence diagrams) |
| **Language** | English (source TDS is Vietnamese; identifiers, comments and business-rule references are kept consistent with the source) |
| **Scope** | 13 owned tables, 9 service interfaces, 20 use cases across 6 features: Source Report & Triage (UC-073/074/114), Incident Investigation (UC-041/009/116/120), CAPA Lifecycle (UC-005/038/082/168), Incident Closure & Report (UC-013/033/031/121), Violation Management (UC-179/180/181/182), Weekly Override-Abuse Report (UC-183) |

This document restates the approved backend design (`TDS-MF11_Backend.md`) as three standard artifacts: (1) a DBML schema for the module's 13 owned tables, (2) per-feature class diagrams showing the entity/DTO/controller/service/repository/port layering, and (3) per-feature sequence diagrams for the interactions that carry real business logic — including the error paths that a food-safety incident investigation cannot afford to get wrong. Pure read/list endpoints without branching logic are covered by the class diagrams alone.

---

## 1. Database Architecture (DBML)

> MF11 owns **13 business tables** (Flyway `V20__mf11_incident.sql`, seed `V21__seed_mf11.sql`). Two design choices shape almost every table below: (1) **ADR-M11-1** — a strict 3-table pipeline `IncidentSourceReport (RPT) → IncidentRecordDraft (REC) → Incident (INC)`, never collapsed into one table, so every investigation can always be traced back to the raw reports that started it; (2) **ADR-M11-7** — every reference to another module's data (`students`, `dishes`, `inventory batches`) is a bare `UUID` column resolved through a read-only port or `ReferenceResolver`, **never** a JPA `@ManyToOne`/DB foreign key, so MF11 never locks its build order to MF03/MF05/MF06. Only `schools` (MF01), `inspection_sessions` (MF07) and `partners` (MF04) get a real FK, because those three are stable, already-merged identity tables.

```dbml
Project sfms_mf11 {
  database_type: 'PostgreSQL'
  Note: '''
    MF11 — Incident & Emergency Management (merged with Violation Management).
    Migration: V20__mf11_incident.sql (schema) + V21__seed_mf11.sql (seed).
    All 13 owned tables carry a business `code` (UNIQUE per school) exposed as
    JSON field "id" (ADR-M11-5); the UUID primary key never leaves the API.
  '''
}

// ============================================================
// Owned by MF11 — Report -> Record -> Incident pipeline (ADR-M11-1)
// ============================================================

Table incident_source_reports {
  id                      uuid         [pk, default: `gen_random_uuid()`]
  code                    varchar(24)  [not null, note: 'RPT-2026-034']
  school_id               uuid         [not null, ref: > schools.id]
  reported_by_id          uuid         [not null, note: 'MF01 user id, no FK on purpose (M11-D06)']
  reported_by_label       varchar(120) [not null, note: 'snapshot, e.g. "Dao Van Quan (Teacher 4A)"']
  reported_at             timestamptz  [not null]
  class_name              varchar(20)
  symptoms                "text[]"     [not null, default: `'{}'`]
  suspected_meal_text     varchar(300) [note: 'raw free text, NOT bound to a dish id (BR-151 never auto-links)']
  meal_date               date
  description             text         [not null]
  evidence_photos         integer      [not null, default: 0]
  severity_level          varchar(8)   [note: 'nullable, read-only after triage']
  status                  source_report_status [not null, default: 'PENDING_TRIAGE']
  merged_into_incident_id uuid         [note: 'set only when status = MERGED']
  draft_record_id         uuid

  indexes {
    (school_id, code)                [unique, name: 'uq_rpt_code']
    (school_id, status, reported_at) [name: 'ix_rpt_status']
  }

  Note: '''
    CHECK ck_rpt_merged: status = MERGED requires merged_into_incident_id NOT NULL.
    CHECK ck_rpt_after_meal: reported_at >= meal_date (a report can never predate its own meal).
    BR-146 (rev. 2026-07-20): the system only DETECTS and SUGGESTS a cluster signal
    (>=3 students, matching symptom, 24h window) — it never auto-creates an Incident,
    and never auto-quarantines anything (CF-M11-5).
  '''
}

Table incident_records {
  id            uuid        [pk, default: `gen_random_uuid()`]
  code          varchar(24) [not null, note: 'REC-2026-022']
  school_id     uuid        [not null, ref: > schools.id]
  content       text        [not null, note: 'official record body, hand-authored by Medical Staff (BR-154)']
  status        record_draft_status [not null, default: 'DRAFT']
  created_by_id uuid        [not null]
  created_at    timestamptz [not null, default: `now()`]
  updated_at    timestamptz
  incident_id   uuid        [note: 'set only when status = OPENED']
  location      varchar(200) [note: 'form field of the specialreport2173_1 template']
  occurred_at   timestamptz
  pdf_exported  boolean     [not null, default: false]

  indexes {
    (school_id, code) [unique, name: 'uq_rec_code']
  }

  Note: 'CHECK ck_rec_opened: status = OPENED requires incident_id NOT NULL. This is the middle tier of RPT -> REC -> INC (ADR-M11-1) — it can also terminate at CLOSED_NOT_FOOD_SAFETY without ever producing an Incident.'
}

Table incident_record_sources {
  record_id uuid [not null, ref: > incident_records.id]
  report_id uuid [not null, ref: > incident_source_reports.id]

  indexes {
    (record_id, report_id) [pk]
  }

  Note: 'N-1 join: several IncidentSourceReport rows are merged into one IncidentRecordDraft (ADR-M11-1). report_id is ON DELETE RESTRICT — a report that has been merged can never be deleted out from under its record.'
}

Table incidents {
  id                             uuid        [pk, default: `gen_random_uuid()`]
  code                           varchar(24) [not null, note: 'INC-2026-015']
  school_id                      uuid        [not null, ref: > schools.id]
  incident_record_id             uuid        [not null, ref: > incident_records.id, note: 'MANDATORY, UNIQUE — ADR-M11-1']
  title                          varchar(200) [not null]
  reported_at                    timestamptz [not null]
  occurred_at                    timestamptz [note: 'anchor #1 for the 24h quarantine window, BR-149']
  reported_by_label              varchar(200) [not null]
  severity                       incident_severity [note: 'NULL = not yet classified (CF-M11-12); JSON still renders "PENDING"']
  status                         incident_status [not null, default: 'SEVERITY_PENDING']
  symptoms                       "text[]"    [not null, default: `'{}'`]
  suspected_meal_text            varchar(300)
  meal_date                      date
  meal_time_label                varchar(60)
  first_aid_notes                text
  clinical_risk_assessment       text        [note: 'UC-009']
  investigation_lab_result       text
  investigation_lab_result_files integer     [not null, default: 0]
  root_cause                     text        [note: 'BR-150']
  root_cause_status              root_cause_status
  evidence_final                 boolean     [not null, default: false]
  measures_draft                 text
  official_clinical_notes        text
  traceability_map_status        trace_map_status [not null, default: 'COMPILING', note: 'BR-151, SLA COMPILING -> READY <= 4h']
  report_exported                boolean     [not null, default: false, note: 'one-way whole-record read-only lock, ADR-M11-8']
  closed_at                      timestamptz
  closed_by_id                   uuid
  closing_statement              text
  created_at                     timestamptz [not null, default: `now()`]

  indexes {
    (school_id, code)     [unique, name: 'uq_inc_code']
    (incident_record_id)  [unique, name: 'uq_inc_record']
    (school_id, status)   [name: 'ix_inc_school_status']
    (reported_at)         [name: 'ix_inc_reported_at']
    (school_id, severity) [name: 'ix_inc_severity']
  }

  Note: '''
    CHECK ck_inc_severity_set: status <> SEVERITY_PENDING requires severity NOT NULL (BR-147).
    CHECK ck_inc_closed: status = CLOSED requires closed_at, closed_by_id, closing_statement all set (BR-155).
    CHECK ck_inc_after_meal: reported_at >= meal_date.
    This is the aggregate root of the investigation; its child collections
    (affected students, suspected dishes, quarantined batches, evidence, events)
    are the four tables directly below.
  '''
}

Table incident_affected_students {
  id           uuid        [pk, default: `gen_random_uuid()`]
  incident_id  uuid        [not null, ref: > incidents.id]
  student_id   uuid        [not null, note: 'MF03 student id, NO FK on purpose (M11-D11) — legal record must survive a student being deleted']
  student_name varchar(120) [not null, note: 'snapshot (ADR-M11-7)']
  class_name   varchar(20) [not null]
  recovered    boolean     [not null, default: false, note: 'BR-155 closure gate']
  recovered_at timestamptz

  indexes {
    (incident_id, student_id) [unique, name: 'uq_ias_inc_student']
    (student_id)               [name: 'ix_ias_student']
    (class_name)               [name: 'ix_ias_class', note: 'backs the Teacher read-scope derivation, ADR-M11-10']
  }

  Note: 'CHECK ck_ias_recovered: recovered = true requires recovered_at NOT NULL.'
}

Table incident_suspected_dishes {
  id          uuid        [pk, default: `gen_random_uuid()`]
  incident_id uuid        [not null, ref: > incidents.id]
  dish_id     uuid        [not null, note: 'MF05 dish id — the real key, not a name']
  dish_name   varchar(180) [not null, note: 'snapshot']

  indexes {
    (incident_id, dish_id) [unique, name: 'uq_isd_inc_dish']
  }
}

Table incident_quarantine_batches {
  id                uuid        [pk, default: `gen_random_uuid()`]
  incident_id       uuid        [not null, ref: > incidents.id]
  batch_id          uuid        [not null, note: 'MF06 inventory batch id']
  batch_code        varchar(40) [not null]
  reason            varchar(300) [not null, note: 'MANDATORY — BR-149 rev. 2026-07-27']
  quarantined_at    timestamptz [not null, default: `now()`]
  quarantined_by_id uuid        [not null]

  indexes {
    (incident_id, batch_id) [unique, name: 'uq_iqb_inc_batch']
    (batch_id)               [name: 'ix_iqb_batch']
  }

  Note: '''
    CHECK ck_iqb_reason: reason must be non-blank.
    ONE-WAY (BR-217, ADR-M11-3): there is no released_at column, and
    `CREATE RULE iqb_no_delete ... DO INSTEAD NOTHING` blocks DELETE at the DB level.
    A caterer returns a quarantined batch straight to its supplier — there is no unlock endpoint.
  '''
}

Table incident_quarantine_release_log {
  id          uuid        [pk, default: `gen_random_uuid()`]
  incident_id uuid        [not null, ref: > incidents.id]
  batch_codes "text[]"    [not null]
  dish_label  varchar(180)
  reason      varchar(300) [not null]
  released_at timestamptz [not null]

  Note: 'HISTORICAL, read-only — only rows dated 2026-07-19 to 2026-07-27 exist. The release mechanism was withdrawn by BR-217; no new rows are ever written (CF-M11-2).'
}

Table incident_evidence {
  id          uuid        [pk, default: `gen_random_uuid()`]
  incident_id uuid        [not null, ref: > incidents.id]
  type        varchar(60) [not null]
  description varchar(500) [not null]
  file_count  integer     [not null, default: 0]
  captured_at timestamptz [not null, default: `now()`, note: 'BR-150 root-cause investigation evidence']

  indexes {
    (incident_id) [name: 'ix_iev_evidence_inc']
  }
}

Table incident_events {
  id          uuid        [pk, default: `gen_random_uuid()`]
  code        varchar(24) [not null, note: 'IEV-014-01']
  incident_id uuid        [not null, ref: > incidents.id]
  at          timestamptz [not null]
  type        incident_event_type [not null]
  description varchar(500) [not null]
  actor_label varchar(120) [not null]

  indexes {
    (incident_id, at) [name: 'ix_iev_incident_at']
  }

  Note: '''
    APPEND-ONLY (ADR-M11-2): `CREATE RULE incident_events_no_update/no_delete ... DO INSTEAD NOTHING`
    blocks both UPDATE and DELETE at the database level, not just by code discipline. Every write
    happens inside the same @Transactional method that changes the owning entity's state, so the
    investigation timeline is always evidence ("what time was the batch locked?"), never a
    reconstruction from a status column.
  '''
}

Table corrective_actions {
  id                        uuid        [pk, default: `gen_random_uuid()`]
  code                      varchar(24) [not null, note: 'CAPA-2026-033']
  school_id                 uuid        [not null, ref: > schools.id]
  source_label              varchar(200) [not null, note: 'display label only, NOT a key']
  incident_id               uuid        [ref: > incidents.id, note: 'origin 1 — BR-152']
  inspection_id             uuid        [ref: > inspection_sessions.id, note: 'origin 2 — BR-095 (MF07)']
  description               text        [not null]
  assignee_label            varchar(120) [not null]
  assignee_supplier_id      uuid        [ref: > partners.id, note: 'MF04']
  status                    capa_status [not null, default: 'DRAFT', note: 'BR-216 6-state lifecycle']
  submitted_for_approval_at timestamptz
  approved_at               timestamptz
  evidence_submitted_at     timestamptz
  reject_reason             varchar(500)
  evidence_note             text
  evidence_photos           integer     [not null, default: 0]
  rework_reason              varchar(500)
  rework_requested_at        timestamptz
  deadline                   varchar(40) [note: 'DEPRECATED (BR-216 rev. 2026-07-21 dropped the hard 12h SLA) — read-only, kept nullable so incident-detail-view.tsx does not break, CF-M11-21(d)']

  indexes {
    (school_id, code)     [unique, name: 'uq_capa_code']
    (incident_id)         [unique, name: 'uq_capa_one_per_incident', note: 'partial unique WHERE incident_id IS NOT NULL — 1 Incident = 1 CAPA, BR-216']
    (school_id, status)   [name: 'ix_capa_status']
    (assignee_supplier_id) [name: 'ix_capa_assignee', note: 'backs the Service Caterers read-scope derivation, ADR-M11-10']
  }

  Note: '''
    CHECK ck_capa_source: exactly one of (incident_id, inspection_id) is set — a CAPA has exactly
    one origin (BR-152 incident vs BR-095 failed inspection).
    CHECK ck_capa_approved: approved_at requires submitted_for_approval_at <= approved_at.
    CHECK ck_capa_evidence: status IN (SUBMITTED_EVIDENCE, COMPLETED) requires
    evidence_submitted_at NOT NULL and evidence_photos > 0.
  '''
}

Table violation_records {
  id                             uuid        [pk, default: `gen_random_uuid()`]
  code                           varchar(24) [not null, note: 'VIO-2026-010']
  school_id                      uuid        [not null, ref: > schools.id]
  type                           varchar(20) [not null, default: 'Quy trinh', note: 'fixed value — BR-183 rev. 2026-07-26']
  subject                        varchar(200) [not null]
  recorded_by_id                 uuid        [not null]
  recorded_by_label              varchar(120) [not null]
  date                           date        [not null]
  severity                       violation_severity [not null]
  resolution                     resolution_status   [not null, default: 'PENDING']
  status                         violation_status     [not null, default: 'OPEN']
  violation_type                 violation_type
  description                    text        [not null, note: 'HTML record body']
  occurred_at                    timestamptz [not null, note: 'BR-211']
  location_of_incident           varchar(200) [not null, note: 'BR-211']
  caterers_representative_id     uuid        [not null, note: 'MF04 — BR-183/187, unit-level not individual']
  caterers_representative_label  varchar(160) [not null]
  witness_label                  varchar(120)
  self_reported                  boolean     [not null, default: false, note: 'BR-189']
  repeat_suspect                 boolean     [not null, default: false, note: 'BR-187 — best-effort, non-blocking suggestion']
  related_incident_id            uuid        [ref: > incidents.id]
  source_inspection_id           uuid        [ref: > inspection_sessions.id, note: 'MF07']
  pdf_exported                   boolean     [not null, default: false, note: 'BR-211']
  pdf_generated_at               timestamptz
  pdf_version                    integer     [not null, default: 0]
  action_taken                   text        [note: 'BR-185']
  unit_sanction_level            unit_sanction_level [note: 'BR-185 rev. 2026-07-26']
  handled_by_id                  uuid
  reject_reason                  varchar(500)
  staff_name                     varchar(120) [note: 'DEPRECATED 2026-07-26 (CF-M11-4) — read-only, historical rows only, absent from every request DTO']
  staff_position                 varchar(60)  [note: 'DEPRECATED — see staff_name']
  disciplinary_level             varchar(40)  [note: 'DEPRECATED — see staff_name']

  indexes {
    (school_id, code)                            [unique, name: 'uq_vio_code']
    (school_id, date)                             [name: 'ix_vio_school_date']
    (violation_type, date)                        [name: 'ix_vio_type_date', note: 'BR-190 weekly aggregation']
    (caterers_representative_id, severity, date)  [name: 'ix_vio_rep_severity', note: 'BR-187 repeat-suspect lookback']
    (related_incident_id)                         [name: 'ix_vio_incident']
  }

  Note: '''
    CHECK ck_vio_resolution: resolution <> PENDING requires action_taken, handled_by_id and
    unit_sanction_level all set (BR-185).
    CHECK ck_vio_closed: status = CLOSED requires resolution IN (APPROVED, REJECTED).
    CHECK ck_vio_reject: resolution = REJECTED requires a non-blank reject_reason.
    BR-183 (rev. 2026-07-26) merged what used to be a staff-level record into a single
    unit-level (Service Caterers company) record — see the 3 deprecated columns above.
  '''
}

Table violation_files {
  id           uuid        [pk, default: `gen_random_uuid()`]
  violation_id uuid        [not null, ref: > violation_records.id]
  kind         file_kind   [not null, note: 'EVIDENCE | SIGNED_RECORD']
  name         varchar(200) [not null]
  storage_key  varchar(400) [not null]
  size_label   varchar(20)
  uploaded_at  timestamptz [not null, default: `now()`]

  indexes {
    (violation_id, kind) [name: 'ix_vfile_violation']
  }

  Note: 'kind = SIGNED_RECORD is the BR-211 gate: at least one such row must exist before a violation resolution may leave PENDING — see the class diagram in §6.1.'
}

// ============================================================
// Enums
// ============================================================
Enum source_report_status {
  PENDING_TRIAGE
  DRAFTING
  DRAFTED
  MERGED
  CLOSED_NOT_FOOD_SAFETY
}

Enum record_draft_status {
  DRAFT
  COMPLETED
  OPENED
  CLOSED_NOT_FOOD_SAFETY
}

Enum incident_status {
  SEVERITY_PENDING
  INVESTIGATING
  CAPA_PENDING
  CAPA_APPROVED
  VALIDATED
  CLOSED
}

Enum incident_severity {
  LOW
  MEDIUM
  HIGH
}

Enum root_cause_status {
  PRELIMINARY
  FINAL
}

Enum trace_map_status {
  COMPILING
  READY
}

Enum capa_status {
  DRAFT
  PENDING_ADMIN_APPROVAL
  APPROVED_FOR_IMPLEMENTATION
  IN_PROGRESS
  SUBMITTED_EVIDENCE
  COMPLETED
}

Enum violation_severity {
  LOW
  MEDIUM
  HIGH
}

Enum resolution_status {
  PENDING
  PENDING_APPROVAL
  APPROVED
  REJECTED
}

Enum violation_status {
  OPEN
  CLOSED
}

Enum violation_type {
  HYGIENE_VIOLATION
  PROCEDURE_SKIP
  OVERRIDE_ABUSE
  UNAUTHORIZED_ACTION
  OTHER
}

Enum unit_sanction_level {
  WRITTEN_REMINDER
  CONTRACT_WARNING
  CONTRACT_PENALTY
  TERMINATION_PROPOSAL
}

Enum incident_event_type {
  REPORTED
  SEVERITY_SET
  QUARANTINE
  RELEASE
  SAMPLE_SENT
  TRACE_READY
  CAPA_SUBMITTED
  CAPA_APPROVED
  CAPA_EVIDENCE
  CAPA_VALIDATED
  REPORT_SENT
  CLOSED
}

Enum file_kind {
  EVIDENCE
  SIGNED_RECORD
}

// ============================================================
// External tables — owned by other modules, referenced by id only
// ============================================================
Table schools {
  id uuid [pk]
  Note: 'Owned by MF01. Tenant/school scoping only.'
}

Table inspection_sessions {
  id uuid [pk]
  Note: 'Owned by MF07. Second origin of a CorrectiveAction (BR-095) and optional source of a ViolationRecord.'
}

Table partners {
  id uuid [pk]
  Note: 'Owned by MF04. assignee_supplier_id / caterers_representative_id resolve here (the "Service Caterers unit", not an individual).'
}
```

**Description.** MF11's schema is dominated by one architectural fact: **almost everything is a locked, append-only record of what happened**, not a mutable projection of the current state. `incident_source_reports → incident_records → incidents` is a strict one-directional pipeline (ADR-M11-1) — a raw report can be merged into a record, a record can be opened into an incident, but nothing ever collapses back down, and the DB enforces the forward-only shape with `ck_rpt_merged`/`ck_rec_opened` plus the `uq_inc_record` unique constraint (one record opens at most one incident). `incident_quarantine_batches` has no `released_at` column and a DB `RULE` blocking `DELETE` outright, because BR-217 turned quarantine into a one-way action — the caterer returns the batch to its supplier, there is no "undo". `incident_events` is the clearest case: a Postgres `RULE` blocks both `UPDATE` and `DELETE`, because the table's entire purpose is to answer "at what time was this batch locked?" during a poisoning investigation, a question a status column alone cannot answer (ADR-M11-2). `corrective_actions` enforces "1 Incident = 1 CAPA" with a **partial unique index** (`WHERE incident_id IS NOT NULL`) rather than a plain unique constraint, because a CAPA can also originate from a failed 3-step inspection (BR-095, MF07) with no `incident_id` at all — those rows must not collide. Finally, three columns on `violation_records` (`staff_name`, `staff_position`, `disciplinary_level`) are kept only for two historical rows after BR-183 moved violations from staff-level to unit-level on 2026-07-26 — they are nullable, absent from every write DTO, and exist purely so old records stay readable.

---

## 2. Feature 1 — Source Report & Triage Pipeline

Covers the intake side of the module: Teacher/Parent file a raw report, Medical Staff merges one or more reports into an official record, finalizes it, and — as a distinct, always-manual decision — opens a full investigation. Backs UC-073, UC-074, UC-114.

### 2.1 Class Diagram

```plantuml
@startuml MF11_F1_ClassDiagram
title Class Diagram: Source Report & Triage Pipeline

skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 55
skinparam ranksep 85
skinparam minClassWidth 130
skinparam wrapWidth 260
skinparam backgroundColor #FFFFFF

skinparam class {
    BackgroundColor #F4FBF4
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 13

    HeaderBackgroundColor #4CAF50
    HeaderFontColor #FFFFFF

    AttributeFontColor #212121
    AttributeFontName Arial
    AttributeFontSize 12
}

skinparam interface {
    BackgroundColor #f7ed93ff
    BorderColor #000000ff
    FontColor #000000ff
    FontName Arial
}

skinparam arrow {
    Color #162c00ff
    FontColor #162c00ff
    FontName Arial
    FontSize 11
}

skinparam stereotype {
    CBackgroundColor #f6eedcff
    CBorderColor #000000ff
    EBackgroundColor #f6eedcff
    EBorderColor #000000ff
    ABackgroundColor #f6eedcff
    ABorderColor #000000ff
    IBackgroundColor #f6eedcff
    IBorderColor #000000ff
}

hide empty members

package "mf11.domain" {
  class IncidentSourceReport <<Entity>> {
    code
    reportedById
    reportedAt
    className
    symptoms
    status
  }
  class IncidentRecordDraft <<Entity>> {
    code
    content
    status
    incidentId
  }
  enum SourceReportStatus {
    PENDING_TRIAGE
    DRAFTING
    DRAFTED
    MERGED
    CLOSED_NOT_FOOD_SAFETY
  }
  enum RecordDraftStatus {
    DRAFT
    COMPLETED
    OPENED
    CLOSED_NOT_FOOD_SAFETY
  }
  enum IncidentSeverity {
    LOW
    MEDIUM
    HIGH
  }
}

package "mf11.projection" {
  class ClusterSignal <<Projection>> {
    symptomKey
    reportCodes
    studentCount
  }
}

package "mf11.dto" {
  class AffectedStudentRef <<DTO>> {
    studentId
    studentName
    className
  }
  class CreateSourceReportRequest <<DTO>> {
    className
    affectedStudents
    symptoms
    suspectedMealText
    mealDate
    description
    evidencePhotos
  }
  class SourceReportResponse <<DTO>> {
    id
    status
    clusterSignal: ClusterSignal
  }
  class CreateIncidentRecordRequest <<DTO>> {
    sourceReportCodes
  }
  class SaveRecordDraftRequest <<DTO>> {
    content
    location
    occurredAt
  }
  class IncidentRecordResponse <<DTO>> {
    id
    status
    content
    location
    occurredAt
  }
  class IncidentResponse <<DTO>> {
    id
    status
    severity
    traceabilityMapStatus
  }

  SourceReportResponse "1" o-- "0..1" ClusterSignal
}

package "mf11.web" {
  class SourceReportController <<Controller>> {
    + list()
    + detail()
    + create()
    + closeNotFoodSafety()
    + clusterSignals()
  }
  class IncidentRecordController <<Controller>> {
    + list()
    + detail()
    + create()
    + saveDraft()
    + finalize()
    + openInvestigation()
    + exportPdf()
  }
}

package "mf11.service" {
  interface SourceReportService <<Service>> {
    + list()
    + detail()
    + create()
    + closeNotFoodSafety()
    + clusterSignals()
  }
  interface ClusterSignalService <<Service>> {
    + evaluate()
  }
  interface IncidentRecordService <<Service>> {
    + list()
    + detail()
    + createFromReports()
    + saveDraft()
    + finalizeRecord()
    + closeNotFoodSafety()
    + openInvestigation()
    + renderPdf()
  }
}

package "mf11.repository" {
  interface SourceReportRepository <<Repository>> {
    + findByCodeInAndStatus()
    + findRecentBySymptom()
    + save()
  }
  interface IncidentRecordRepository <<Repository>> {
    + findByCode()
    + save()
  }
  interface IncidentRepository <<Repository>> {
    + existsByRecordId()
    + save()
  }
}

package "mf11.common.reference" {
  class ReferenceResolver <<Component>> {
    + studentByCode()
    + isParentLinkedToStudent()
    + classroomIdsOfHomeroomTeacher()
  }
}

' ---------- web -> service ----------
SourceReportController ..> SourceReportService
IncidentRecordController ..> IncidentRecordService

' ---------- service -> repository / resolver ----------
SourceReportService ..> SourceReportRepository
SourceReportService ..> ClusterSignalService
SourceReportService ..> ReferenceResolver : read-scope guard
IncidentRecordService ..> IncidentRecordRepository
IncidentRecordService ..> SourceReportRepository : merge
IncidentRecordService ..> IncidentRepository : opens Incident

' ---------- repository -> domain ----------
SourceReportRepository ..> IncidentSourceReport
IncidentRecordRepository ..> IncidentRecordDraft

' ---------- domain -> enum ----------
IncidentSourceReport --> SourceReportStatus
IncidentSourceReport --> IncidentSeverity
IncidentRecordDraft --> RecordDraftStatus
IncidentSourceReport "n" --> "0..1" IncidentRecordDraft : merged into

' ---------- service -> dto / projection ----------
SourceReportService ..> CreateSourceReportRequest
SourceReportService ..> SourceReportResponse
ClusterSignalService ..> ClusterSignal
IncidentRecordService ..> CreateIncidentRecordRequest
IncidentRecordService ..> SaveRecordDraftRequest
IncidentRecordService ..> IncidentRecordResponse
IncidentRecordService ..> IncidentResponse
SourceReportResponse ..> AffectedStudentRef

' ---------- layout hints (balance width vs height) ----------
AffectedStudentRef -[hidden]down- CreateSourceReportRequest
CreateSourceReportRequest -[hidden]down- CreateIncidentRecordRequest
CreateIncidentRecordRequest -[hidden]down- SaveRecordDraftRequest
SourceReportResponse -[hidden]down- IncidentRecordResponse
IncidentRecordResponse -[hidden]down- IncidentResponse
SourceReportStatus -[hidden]down- RecordDraftStatus
@enduml
```

**Description.** `SourceReportController` and `IncidentRecordController` never touch each other's repositories directly — every write goes through the matching service. `SourceReportServiceImpl.create()` is deliberately thin: it validates, persists a `PENDING_TRIAGE` row, and asks `ClusterSignalService` for a **suggestion only** (BR-146, rev. 2026-07-20) — it never creates an `Incident`, quarantines a batch, or starts a traceability map itself, correcting an earlier reading of the use-case file that implied full automation (CF-M11-5). `IncidentRecordServiceImpl.openInvestigation()` is the one method allowed to construct an `Incident`, and only from a record already `COMPLETED`; this is the DB-enforced boundary in ADR-M11-1 (`incidents.incident_record_id NOT NULL UNIQUE`).

### 2.2 Sequence Diagram — Report a Food Safety Concern/Incident

```plantuml
@startuml MF11_F1_ReportConcern
title Sequence Diagram: Report a Food Safety Concern/Incident

skinparam linetype ortho
skinparam backgroundColor #FFFFFF

skinparam stereotype {
    CBackgroundColor #E8F5E9
    CBorderColor #2E7D32
    EBackgroundColor #E8F5E9
    EBorderColor #2E7D32
    ABackgroundColor #E8F5E9
    ABorderColor #2E7D32
    IBackgroundColor #E8F5E9
    IBorderColor #2E7D32
}

skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontName Arial
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontName Arial
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontName Arial
skinparam EntityFontSize 13

skinparam CollectionsBackgroundColor #F4FBF4
skinparam CollectionsBorderColor #2E7D32
skinparam CollectionsFontColor #212121
skinparam CollectionsFontName Arial
skinparam CollectionsFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontName Arial
skinparam DatabaseFontSize 13

skinparam sequence {
    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontName Arial
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontName Arial
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #1B5E20
    GroupHeaderBackgroundColor #C8E6C9
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Teacher / Parent"                       as T
boundary    ":SourceReportScreen"                    as UI      <<Boundary>>
control     ":SourceReportController"                as C       <<Controller>>
control     ":SourceReportServiceImpl"               as S       <<Service>>
boundary    ":ReferenceResolver"                     as REF     <<Facade>>
control     ":ClusterSignalService"                  as CS      <<Service>>
entity      ":IncidentSourceReport"                  as RPT     <<Entity>>
collections ":SourceReportRepository"                as RRepo   <<Repository>>
database    ":PostgreSQL"                            as DB
queue       ":DomainEventBus"                        as MQ      <<EventBus>>

T -> UI : fill in className, affectedStudents[], symptoms[],\nsuspectedMealText, mealDate, description
activate UI
UI -> C : POST /api/v1/source-reports\n{className, affectedStudents[], symptoms[],\n suspectedMealText, mealDate, description}
activate C
C -> S : create(req, actor)
activate S

S -> S : validate required fields\n(symptoms, mealDate, description)
alt missing required field
  S --> C : throw ValidationException(IEM-023)
  C --> UI : 400
  UI --> T : inline field errors
else fields complete
  S -> REF : Parent -> isParentLinkedToStudent(actor.userId, studentId)\nTeacher -> classroomIdsOfHomeroomTeacher(actor.userId)
  activate REF
  REF --> S : allowed?
  deactivate REF
  alt Parent reports for an unlinked student\nor Teacher reports for a class that is not their own
    S --> C : throw ForbiddenException(IEM-005)
    C --> UI : 403
    UI --> T : "You cannot report for this student/class"
  else read-scope OK
    S -> RPT : new IncidentSourceReport(code, className, symptoms[],\nsuspectedMealText, mealDate, description,\nstatus = PENDING_TRIAGE)
    activate RPT
    RPT --> S : report(PENDING_TRIAGE)
    deactivate RPT

    S -> RRepo : save(report)
    activate RRepo
    RRepo -> DB : INSERT INTO incident_source_reports\n(code=RPT-2026-034, status=PENDING_TRIAGE)
    activate DB
    DB --> RRepo : 1 row inserted
    deactivate DB
    RRepo --> S : IncidentSourceReport(RPT-2026-034)
    deactivate RRepo

    S -> CS : evaluate(schoolId, symptoms, reportedAt)
    activate CS
    CS -> RRepo : findRecentBySymptom(schoolId, symptoms, since = now()-24h)
    activate RRepo
    RRepo -> DB : SELECT ... WHERE reported_at > now()-24h\nGROUP BY symptom HAVING COUNT(DISTINCT student) >= 3
    activate DB
    DB --> RRepo : matching rows
    deactivate DB
    RRepo --> CS : List<IncidentSourceReport>
    deactivate RRepo
    CS --> S : ClusterSignal? (a SUGGESTION only)
    deactivate CS

    S -> MQ : publish(SourceReportFiled[, ClusterSignalDetected])
    activate MQ
    MQ --> S : accepted (async — Medical Staff triage screen hint)
    deactivate MQ

    S --> C : SourceReportResponse{ id:"RPT-2026-034",\n status:"PENDING_TRIAGE", clusterSignal }
    C --> UI : 201
    UI --> T : "Report submitted" confirmation
  end
end
deactivate S
deactivate C
deactivate UI
@enduml
```

**Description.** Backs UC-073 (Parent) / UC-074 (Teacher). The only two guards are field completeness and read-scope (a parent may only report for a linked child, BR-201; a teacher only for their own homeroom class). The cluster-signal check is intentionally decoupled from any incident-creating side effect — `ClusterSignal` is data the Medical Staff triage screen can display as a hint, nothing more, which is the exact correction the 2026-07-20 revision of BR-146/BR-149 made to an earlier design that auto-created and auto-quarantined.

### 2.3 Sequence Diagram — Triage: Merge Reports → Finalize Record → Open Investigation

```plantuml
@startuml MF11_F1_TriagePipeline
title Sequence Diagram: Triage — Merge Reports → Finalize Record → Open Investigation

skinparam linetype ortho
skinparam backgroundColor #FFFFFF

skinparam stereotype {
    CBackgroundColor #E8F5E9
    CBorderColor #2E7D32
    EBackgroundColor #E8F5E9
    EBorderColor #2E7D32
    ABackgroundColor #E8F5E9
    ABorderColor #2E7D32
    IBackgroundColor #E8F5E9
    IBorderColor #2E7D32
}

skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontName Arial
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontName Arial
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontName Arial
skinparam EntityFontSize 13

skinparam CollectionsBackgroundColor #F4FBF4
skinparam CollectionsBorderColor #2E7D32
skinparam CollectionsFontColor #212121
skinparam CollectionsFontName Arial
skinparam CollectionsFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontName Arial
skinparam DatabaseFontSize 13

skinparam sequence {
    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontName Arial
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontName Arial
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #1B5E20
    GroupHeaderBackgroundColor #C8E6C9
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Medical Staff"                          as M
boundary    ":TriageWorkspaceScreen"                 as UI      <<Boundary>>
control     ":IncidentRecordController"              as C       <<Controller>>
control     ":IncidentRecordServiceImpl"             as S       <<Service>>
collections ":SourceReportRepository"                as RR      <<Repository>>
collections ":IncidentRecordRepository"              as RCRepo  <<Repository>>
collections ":IncidentRepository"                    as IR      <<Repository>>
entity      ":IncidentRecordDraft"                   as REC     <<Entity>>
entity      ":Incident"                              as INC     <<Entity>>
database    ":PostgreSQL"                            as DB
queue       ":DomainEventBus"                        as MQ      <<EventBus>>

M -> UI : select "RPT-2026-030","RPT-2026-031" -> "Merge"
activate UI
UI -> C : POST /api/v1/incident-records\n{ sourceReportCodes:["RPT-2026-030","RPT-2026-031"] }
activate C
C -> S : createFromReports(codes, actor)
activate S
S -> RR : findByCodeInAndStatus(codes, PENDING_TRIAGE)
activate RR
RR -> DB : SELECT ... WHERE code IN (?) AND status = 'PENDING_TRIAGE'
activate DB
DB --> RR : matching rows
deactivate DB
RR --> S : List<IncidentSourceReport>
deactivate RR
alt any report is not PENDING_TRIAGE
  S --> C : throw ConflictException(IEM-114)
  C --> UI : 409 "Only reports still Pending Triage can be merged"
  UI --> M : conflict toast
else all eligible
  S -> REC : new IncidentRecordDraft(code, status = DRAFT)
  activate REC
  REC --> S : record(DRAFT)
  deactivate REC
  S -> RCRepo : save(record)
  activate RCRepo
  RCRepo -> DB : INSERT incident_records (REC-2026-022, DRAFT)\n+ INSERT incident_record_sources
  activate DB
  DB --> RCRepo : 1 row inserted
  deactivate DB
  RCRepo --> S : IncidentRecordDraft(REC-2026-022)
  deactivate RCRepo
  S -> RR : updateStatus(codes, DRAFTING)
  activate RR
  RR -> DB : UPDATE source_reports SET status='DRAFTING'
  activate DB
  DB --> RR : n rows updated
  deactivate DB
  RR --> S : ok
  deactivate RR
  S --> C : IncidentRecordResponse{ id:"REC-2026-022", status:"DRAFT" }
  C --> UI : 201
  UI --> M : "Reports merged into draft record"
end
deactivate S
deactivate C
deactivate UI

M -> UI : author content, location, occurredAt -> "Finalize"
activate UI
UI -> C : POST /incident-records/REC-2026-022/finalize\n{ content, location, occurredAt }
activate C
C -> S : finalizeRecord(code, req, actor)
activate S
S -> REC : setContent(content), setStatus(COMPLETED)
activate REC
REC --> S : record(COMPLETED)
deactivate REC
S -> RCRepo : save(record)
activate RCRepo
RCRepo -> DB : UPDATE incident_records SET status='COMPLETED'
activate DB
DB --> RCRepo : 1 row updated
deactivate DB
RCRepo --> S : IncidentRecordDraft(COMPLETED)
deactivate RCRepo
S -> RR : updateStatus(sourceCodes, DRAFTED)
activate RR
RR -> DB : UPDATE source_reports SET status='DRAFTED'
activate DB
DB --> RR : n rows updated
deactivate DB
RR --> S : ok
deactivate RR
S --> C : IncidentRecordResponse{ status:"COMPLETED" }
deactivate S
C --> UI : 200
deactivate C
UI --> M : "Record finalized"
deactivate UI

M -> UI : "Open Investigation"
activate UI
UI -> C : POST /incident-records/REC-2026-022/open-investigation
activate C
C -> S : openInvestigation(code, actor)
activate S
S -> RCRepo : findByCodeForUpdate(code)
activate RCRepo
RCRepo -> DB : SELECT record WHERE code=? FOR UPDATE
activate DB
DB --> RCRepo : record(status='COMPLETED')
deactivate DB
RCRepo --> S : IncidentRecordDraft
deactivate RCRepo
alt record not COMPLETED
  S --> C : throw ConflictException(IEM-114)
  C --> UI : 409
  UI --> M : conflict toast
else record COMPLETED
  S -> INC : new Incident(code=INC-2026-015, severity=NULL,\nstatus='SEVERITY_PENDING', incidentRecordId=REC-2026-022)
  activate INC
  INC --> S : incident(SEVERITY_PENDING)
  deactivate INC
  S -> IR : save(incident)
  activate IR
  IR -> DB : INSERT incidents\n(code=INC-2026-015, severity=NULL, status='SEVERITY_PENDING',\n incident_record_id=REC-2026-022)
  activate DB
  DB --> IR : 1 row inserted
  deactivate DB
  IR --> S : Incident(INC-2026-015)
  deactivate IR
  S -> DB : INSERT incident_affected_students\n(deduplicated by studentId across all merged reports)
  activate DB
  DB --> S : n rows inserted
  deactivate DB
  S -> DB : INSERT incident_events (type=REPORTED)
  activate DB
  DB --> S : 1 row inserted
  deactivate DB
  S -> RR : updateStatus(sourceCodes, MERGED, mergedIntoIncidentId=INC)
  activate RR
  RR -> DB : UPDATE source_reports SET status='MERGED', merged_into_incident_id=INC
  activate DB
  DB --> RR : n rows updated
  deactivate DB
  RR --> S : ok
  deactivate RR
  S -> RCRepo : updateStatus(code, OPENED, incidentId=INC)
  activate RCRepo
  RCRepo -> DB : UPDATE incident_records SET status='OPENED', incident_id=INC
  activate DB
  DB --> RCRepo : 1 row updated
  deactivate DB
  RCRepo --> S : ok
  deactivate RCRepo
  S -> MQ : publish(IncidentOpened) -> TraceabilityMapJob (SLA 4h)
  activate MQ
  MQ --> S : accepted
  deactivate MQ
  S --> C : IncidentResponse{ id:"INC-2026-015", status:"SEVERITY_PENDING",\n severity:"PENDING", traceabilityMapStatus:"COMPILING" }
  C --> UI : 201
  UI --> M : redirected to the new investigation workspace
end
deactivate S
deactivate C
deactivate UI
@enduml
```

**Description.** Backs UC-114 as extended on 2026-07-15: what used to be a single confirmation click is now three deliberate, separately-authorized steps — merge, finalize, open — matching the fact that the middle artifact (`REC`) is now a real hand-authored legal record (BR-154), not a rubber stamp. "Opening an investigation" is the only action that ever produces an `Incident` row, and it always resolves the affected-student list by deduplicating across every merged `RPT`, which is why a single student mentioned in two overlapping reports still appears once in `incident_affected_students`. `severity` starts `NULL` (rendered as ` "PENDING"` in JSON per CF-M11-12 / ADR-M11-5) because classification is a separate, later, Medical-Staff-only action covered in Feature 2.

### 2.4 State Chart — Source Report Lifecycle

Backs UC-073/074/114. A raw `IncidentSourceReport` moves forward only: triage either merges it (§2.3, `createFromReports`) or a Medical Staff decision closes it as unrelated to food safety, and once merged its terminal `MERGED` state carries the id of the incident it fed.

```plantuml
@startuml MF11_F1_StateChart_SourceReport
title State Diagram: Source Report Lifecycle

skinparam nodesep 90
skinparam ranksep 110
skinparam linetype ortho
skinparam backgroundColor #FFFFFF

skinparam state {
    BackgroundColor #F4FBF4
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 13
}

skinparam arrow {
    Color #2E7D32
    FontColor #1B5E20
    FontName Arial
    FontSize 12
}

skinparam note {
    BackgroundColor #E8F5E9
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 12
}

skinparam startColor #2E7D32
skinparam endColor #2E7D32

state PENDING_TRIAGE
state DRAFTING
state DRAFTED
state MERGED
state CLOSED_NOT_FOOD_SAFETY

[*] --> PENDING_TRIAGE : Teacher/Parent submits report
PENDING_TRIAGE --> DRAFTING : createFromReports() - merged into a draft record
PENDING_TRIAGE --> CLOSED_NOT_FOOD_SAFETY : closeNotFoodSafety() - Medical Staff decision
DRAFTING --> DRAFTED : finalizeRecord() on the owning draft
DRAFTED --> MERGED : openInvestigation() - Incident created
CLOSED_NOT_FOOD_SAFETY --> [*]
MERGED --> [*]
@enduml
```

### 2.5 State Chart — Incident Record Draft Lifecycle

Backs UC-114. `IncidentRecordDraft` is the deliberately hand-authored middle tier of ADR-M11-1: `DRAFT` while still being merged/edited, `COMPLETED` once Medical Staff finalizes the content, then either `OPENED` (an `Incident` now exists) or `CLOSED_NOT_FOOD_SAFETY` from either non-terminal state.

```plantuml
@startuml MF11_F1_StateChart_RecordDraft
title State Diagram: Incident Record Draft Lifecycle

skinparam nodesep 90
skinparam ranksep 110
skinparam linetype ortho
skinparam backgroundColor #FFFFFF

skinparam state {
    BackgroundColor #F4FBF4
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 13
}

skinparam arrow {
    Color #2E7D32
    FontColor #1B5E20
    FontName Arial
    FontSize 12
}

skinparam note {
    BackgroundColor #E8F5E9
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 12
}

skinparam startColor #2E7D32
skinparam endColor #2E7D32

state DRAFT
state COMPLETED
state OPENED
state CLOSED_NOT_FOOD_SAFETY

[*] --> DRAFT : createFromReports() - merge
DRAFT --> COMPLETED : finalizeRecord(content, location, occurredAt)
DRAFT --> CLOSED_NOT_FOOD_SAFETY : closeNotFoodSafety()
COMPLETED --> OPENED : openInvestigation() - Incident created
COMPLETED --> CLOSED_NOT_FOOD_SAFETY : closeNotFoodSafety()
OPENED --> [*]
CLOSED_NOT_FOOD_SAFETY --> [*]
@enduml
```

---

## 3. Feature 2 — Incident Investigation

Owns the core clinical/food-safety workflow once an `Incident` exists: severity classification with an emergency-alert side effect, one-way batch quarantine, root-cause evidence, and the derived traceability map. Backs UC-041, UC-009, UC-116, UC-120.

### 3.1 Class Diagram

```plantuml
@startuml MF11_F2_ClassDiagram
title Class Diagram: Incident Investigation

skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 55
skinparam ranksep 85
skinparam minClassWidth 130
skinparam wrapWidth 260
skinparam backgroundColor #FFFFFF

skinparam class {
    BackgroundColor #F4FBF4
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 13

    HeaderBackgroundColor #4CAF50
    HeaderFontColor #FFFFFF

    AttributeFontColor #212121
    AttributeFontName Arial
    AttributeFontSize 12
}

skinparam interface {
    BackgroundColor #f7ed93ff
    BorderColor #000000ff
    FontColor #000000ff
    FontName Arial
}

skinparam arrow {
    Color #162c00ff
    FontColor #162c00ff
    FontName Arial
    FontSize 11
}

skinparam stereotype {
    CBackgroundColor #f6eedcff
    CBorderColor #000000ff
    EBackgroundColor #f6eedcff
    EBorderColor #000000ff
    ABackgroundColor #f6eedcff
    ABorderColor #000000ff
    IBackgroundColor #f6eedcff
    IBorderColor #000000ff
}

hide empty members

package "mf11.domain" {
  class Incident <<Entity, AggregateRoot>> {
    code
    incidentRecordId
    severity
    status
    rootCauseStatus
    traceabilityMapStatus
  }
  class IncidentAffectedStudent <<Entity>> {
    studentId
    recovered
  }
  class IncidentSuspectedDish <<Entity>> {
    dishId
  }
  class IncidentQuarantineBatch <<Entity, Immutable>> {
    batchId
    reason
    quarantinedAt
  }
  class InvestigationEvidence <<Entity>> {
    type
    description
  }
  class IncidentEvent <<Entity, Append-Only>> {
    code
    at
    type
  }
  enum IncidentStatus {
    SEVERITY_PENDING
    INVESTIGATING
    CAPA_PENDING
    CAPA_APPROVED
    VALIDATED
    CLOSED
  }
  enum TraceMapStatus {
    COMPILING
    READY
  }
  enum RootCauseStatus {
    PRELIMINARY
    FINAL
  }
  enum IncidentEventType {
    REPORTED
    SEVERITY_SET
    QUARANTINE
    SAMPLE_SENT
    TRACE_READY
    CAPA_SUBMITTED
    CAPA_APPROVED
    CAPA_EVIDENCE
    CAPA_VALIDATED
    REPORT_SENT
    CLOSED
  }
}

package "mf11.projection" {
  class SuspectedDishCandidate <<Projection>> {
    dishId
    batches: List<BatchCandidate>
  }
  class TraceabilityMap <<Projection>> {
    incidentCode
    status
    nodes: List<TraceNode>
  }
}

package "mf11.dto" {
  class AssessIncidentRequest <<DTO>> {
    severity
    clinicalRiskAssessment
    suspectedDishIds
    firstAidNotes
  }
  class QuarantineBatchRequest <<DTO>> {
    batchCode
    reason
  }
  class QuarantineResult <<DTO>> {
    quarantined
    rejected
  }
  class AddEvidenceRequest <<DTO>> {
    type
    description
    files
  }
  class SaveRootCauseRequest <<DTO>> {
    rootCause
    rootCauseStatus
    evidenceFinal
  }
  class IncidentResponse <<DTO>> {
    id
    severity
    status
    emergencyAlert: EmergencyAlertResult
  }
  class IncidentCaterersResponse <<DTO>> {
    id
    status
    severity
    quarantinedBatches
  }
 IncidentCaterersResponse ..> IncidentResponse : narrower projection
  class IncidentEventResponse <<DTO>> {
    id
    at
    type
    description
    actorLabel
  }
}

package "mf11.web" {
  class IncidentController <<Controller>> {
    + list()
    + detail()
    + assess()
    + quarantineBatches()
    + addEvidence()
    + saveRootCause()
    + traceabilityMap()
    + events()
  }
}

package "mf11.service" {
  interface IncidentQueryService <<Service>> {
    + list()
    + detail()
  }
  interface IncidentInvestigationService <<Service>> {
    + suspectedDishCandidates()
    + assessAndStart()
    + saveAssessmentDraft()
    + quarantineBatches()
    + addEvidence()
    + saveRootCause()
    + traceabilityMap()
    + requestSampleRetrieval()
    + events()
  }
  class EmergencyAlertDispatcher <<Component>> {
    + dispatch()
  }
}

package "mf11.repository" {
  interface IncidentRepository <<Repository>> {
    + findByCodeForUpdate()
    + findBySchoolAndStatus()
    + save()
  }
  interface IncidentEventRepository <<Repository>> {
    + findByIncidentIdOrderByAt()
    + append()
  }
}

package "mf11.port" {
  interface InventoryBatchPort <<Port>> {
    + batchesOfDishes()
    + markQuarantined()
    + isAtTerminalState()
    + receivingSessionOf()
    + isSuppliedBy()
  }
  interface ServingDishPort <<Port>> {
    + dishesServedBetween()
    + studentsWhoAte()
  }
  interface FoodSamplePort <<Port>> {
    + findByIncident()
    + requestRetrieval()
  }
  interface NotificationPort <<Port>> {
    + notify()
  }
}

' ---------- web -> service ----------
IncidentController ..> IncidentQueryService
IncidentController ..> IncidentInvestigationService

' ---------- service -> repository / port ----------
IncidentInvestigationService ..> IncidentRepository
IncidentInvestigationService ..> IncidentEventRepository
IncidentInvestigationService ..> ServingDishPort : 24h serving window
IncidentInvestigationService ..> InventoryBatchPort : batch candidates
IncidentInvestigationService ..> FoodSamplePort
IncidentInvestigationService ..> EmergencyAlertDispatcher
IncidentQueryService ..> IncidentRepository
EmergencyAlertDispatcher ..> NotificationPort : HighSeverityAlertRaised

' ---------- repository -> domain ----------
IncidentRepository ..> Incident
IncidentEventRepository ..> IncidentEvent

' ---------- aggregate root -> children ----------
Incident "1" *-- "n" IncidentAffectedStudent
Incident "1" *-- "n" IncidentSuspectedDish
Incident "1" *-- "n" IncidentQuarantineBatch : one-way
Incident "1" *-- "n" InvestigationEvidence
Incident "1" *-- "n" IncidentEvent : append-only log

' ---------- domain -> enum ----------
Incident --> IncidentStatus
Incident --> TraceMapStatus
Incident --> RootCauseStatus
IncidentEvent --> IncidentEventType

' ---------- service -> dto / projection ----------
IncidentInvestigationService ..> AssessIncidentRequest
IncidentInvestigationService ..> QuarantineBatchRequest
IncidentInvestigationService ..> AddEvidenceRequest
IncidentInvestigationService ..> SaveRootCauseRequest
IncidentInvestigationService ..> QuarantineResult
IncidentInvestigationService ..> SuspectedDishCandidate
IncidentInvestigationService ..> TraceabilityMap
IncidentQueryService ..> IncidentResponse
IncidentQueryService ..> IncidentCaterersResponse
IncidentQueryService ..> IncidentEventResponse

' ---------- layout hints (balance width vs height) ----------
AssessIncidentRequest -[hidden]down- QuarantineBatchRequest
AddEvidenceRequest -[hidden]down- SaveRootCauseRequest
QuarantineResult -[hidden]down- IncidentResponse
IncidentCaterersResponse -[hidden]down- IncidentEventResponse
IncidentStatus -[hidden]down- TraceMapStatus
RootCauseStatus -[hidden]down- IncidentEventType
IncidentAffectedStudent -[hidden]down- IncidentSuspectedDish
IncidentQuarantineBatch -[hidden]down- InvestigationEvidence
InventoryBatchPort -[hidden]down- ServingDishPort
FoodSamplePort -[hidden]down- NotificationPort
@enduml
```

**Description.** `Incident` is the aggregate root for the entire investigation; `IncidentController` fans out to two services depending on intent — `IncidentQueryService` for read-only projections that must respect the D11 role-cut (`IncidentResponse` for Medical/School Admin vs. `IncidentCaterersResponse` for Service Caterers and Teacher, which strips `symptoms`, `firstAidNotes`, `clinicalRiskAssessment`, `rootCause` and student names — PDPD, §16 authorization matrix), and `IncidentInvestigationService` for every write. `InventoryBatchPort` and `ServingDishPort` are read-only ports over MF06/MF09 rather than `@ManyToOne` associations (ADR-M11-7), which is what lets MF11's own 5 owned tables be built without waiting on either module's internal schema.

### 3.2 Sequence Diagram — Assess Severity & Dispatch High-Severity Emergency Alert

```plantuml
@startuml MF11_F2_AssessSeverity
title Sequence Diagram: Assess Severity & Dispatch High-Severity Emergency Alert

skinparam linetype ortho
skinparam backgroundColor #FFFFFF

skinparam stereotype {
    CBackgroundColor #E8F5E9
    CBorderColor #2E7D32
    EBackgroundColor #E8F5E9
    EBorderColor #2E7D32
    ABackgroundColor #E8F5E9
    ABorderColor #2E7D32
    IBackgroundColor #E8F5E9
    IBorderColor #2E7D32
}

skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontName Arial
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontName Arial
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontName Arial
skinparam EntityFontSize 13

skinparam CollectionsBackgroundColor #F4FBF4
skinparam CollectionsBorderColor #2E7D32
skinparam CollectionsFontColor #212121
skinparam CollectionsFontName Arial
skinparam CollectionsFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontName Arial
skinparam DatabaseFontSize 13

skinparam sequence {
    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontName Arial
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontName Arial
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #1B5E20
    GroupHeaderBackgroundColor #C8E6C9
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Medical Staff"                          as M
boundary    ":IncidentInvestigationScreen"           as UI      <<Boundary>>
control     ":IncidentController"                    as C       <<Controller>>
control     ":IncidentInvestigationServiceImpl"      as S       <<Service>>
collections ":IncidentRepository"                    as R       <<Repository>>
entity      ":Incident"                              as INC     <<Entity>>
control     ":EmergencyAlertDispatcher"              as EA      <<Component>>
database    ":PostgreSQL"                            as DB
queue       ":DomainEventBus"                        as MQ      <<EventBus>>

M -> UI : set severity = HIGH, clinicalRiskAssessment,\nsuspectedDishIds -> "Confirm assessment"
activate UI
UI -> C : POST /incidents/INC-2026-015/assess\n{severity:"HIGH", clinicalRiskAssessment:"...",\n suspectedDishIds:["dish-ga-rang-gung"]}
activate C
C -> S : assessAndStart(code, req, actor)
activate S
S -> R : findByCodeForUpdate(schoolId, code)
activate R
R -> DB : SELECT ... FOR UPDATE
activate DB
DB --> R : row (SEVERITY_PENDING)
deactivate DB
R --> S : Incident(SEVERITY_PENDING)
deactivate R

S -> S : guard role = MEDICAL_STAFF
alt not Medical Staff
  S --> C : throw ForbiddenException(IEM-005)
  C --> UI : 403
  UI --> M : "Access denied"
else Medical Staff
  S -> INC : isReportExported()
  activate INC
  INC --> S : reportExported?
  deactivate INC
  alt reportExported = true
    S --> C : throw ConflictException(IEM-RO)
    C --> UI : 409 "Record already submitted — read-only"
    UI --> M : blocking banner
  else record still editable
    S -> S : validate severity in {LOW, MEDIUM, HIGH}
    alt severity not in {LOW, MEDIUM, HIGH}
      S --> C : throw ValidationException(IEM-147)
      C --> UI : 400
      UI --> M : inline field error
    else severity valid
      S -> INC : assess(severity, clinicalRiskAssessment)
      activate INC
      INC --> S : status = INVESTIGATING
      deactivate INC
      S -> R : save(incident)
      activate R
      R -> DB : UPDATE incidents SET severity, clinical_risk_assessment,\n status='INVESTIGATING'
      activate DB
      DB --> R : 1 row updated
      deactivate DB
      R --> S : Incident(INVESTIGATING)
      deactivate R
      S -> DB : INSERT incident_suspected_dishes (dish_id, dish_name)
      activate DB
      DB --> S : n rows inserted
      deactivate DB
      S -> DB : INSERT incident_events (type=SEVERITY_SET)
      activate DB
      DB --> S : 1 row inserted
      deactivate DB
      alt severity = HIGH
        S -> EA : dispatch(incident)
        activate EA
        EA -> MQ : publish(HighSeverityAlertRaised)\n-> School Board, Caterer, Local Health Authority\n(SLA < 60s, async)
        activate MQ
        MQ --> EA : accepted
        deactivate MQ
        EA --> S : dispatched:true, recipients:[...]
        deactivate EA
      end
      S --> C : IncidentResponse{ status:"INVESTIGATING", severity:"HIGH",\n emergencyAlert:{dispatched:true, recipients:[...]} }
      C --> UI : 200
      UI --> M : status badge updates to "Investigating"
    end
  end
end
deactivate S
deactivate C
deactivate UI
@enduml
```

**Description.** Backs UC-009/UC-041. Four guards gate the transition out of `SEVERITY_PENDING`: role (only Medical Staff, BR-147), the whole-record read-only lock (ADR-M11-8 — a record already submitted as an official report can never be reassessed), input validity, and — implicitly — the pessimistic row lock that prevents two concurrent `assess` calls from racing. The `HIGH`-severity alert is fired through an outbox event rather than synchronously, precisely so that a slow notification channel (email/SMS, ADR-000 SMTP stub in dev) can never delay the transaction that matters most during an active incident.

### 3.3 Sequence Diagram — Quarantine Suspected Batches (One-Way)

```plantuml
@startuml MF11_F2_Quarantine
title Sequence Diagram: Quarantine Suspected Batches (One-Way)

skinparam linetype ortho
skinparam backgroundColor #FFFFFF

skinparam stereotype {
    CBackgroundColor #E8F5E9
    CBorderColor #2E7D32
    EBackgroundColor #E8F5E9
    EBorderColor #2E7D32
    ABackgroundColor #E8F5E9
    ABorderColor #2E7D32
    IBackgroundColor #E8F5E9
    IBorderColor #2E7D32
}

skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontName Arial
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontName Arial
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontName Arial
skinparam EntityFontSize 13

skinparam CollectionsBackgroundColor #F4FBF4
skinparam CollectionsBorderColor #2E7D32
skinparam CollectionsFontColor #212121
skinparam CollectionsFontName Arial
skinparam CollectionsFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontName Arial
skinparam DatabaseFontSize 13

skinparam sequence {
    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontName Arial
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontName Arial
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #1B5E20
    GroupHeaderBackgroundColor #C8E6C9
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Medical Staff"                          as M
boundary    ":IncidentInvestigationScreen"           as UI      <<Boundary>>
control     ":IncidentController"                    as C       <<Controller>>
control     ":IncidentInvestigationServiceImpl"      as S       <<Service>>
boundary    ":ServingDishPort"                       as SP      <<Port>>
boundary    ":InventoryBatchPort"                    as BP      <<Port>>
collections ":IncidentRepository"                    as R       <<Repository>>
entity      ":IncidentQuarantineBatch"               as IQB     <<Entity>>
database    ":PostgreSQL"                            as DB
queue       ":DomainEventBus"                        as MQ      <<EventBus>>

M -> UI : open "Suspected dish candidates" panel
activate UI
UI -> C : GET /incidents/INC-2026-015/suspected-dish-candidates
activate C
C -> S : suspectedDishCandidates(code, actor)
activate S
S -> S : anchor = occurredAt ?? mealDate ?? reportedAt\nwindow = [anchor - 24h, anchor]
S -> SP : dishesServedBetween(schoolId, from, to)
activate SP
SP --> S : [{dishId, dishName, mealLabel}]
deactivate SP
S -> BP : batchesOfDishes(dishIds, mealDate, schoolId)
activate BP
BP --> S : [{batchId, batchCode, ingredientName, remainingQty, receivedAt}]
deactivate BP
S --> C : List<SuspectedDishCandidate>{ dish -> batches[] }
deactivate S
C --> UI : 200
deactivate C
UI --> M : candidate list rendered
deactivate UI

M -> UI : select "BATCH-MP-8820", enter reason -> "Quarantine"
activate UI
UI -> C : POST /incidents/INC-2026-015/quarantine-batches\n[{batchCode:"BATCH-MP-8820", reason:"Suspected contamination..."}]
activate C
C -> S : quarantineBatches(code, req, actor)
activate S
S -> S : validate reason is non-blank
alt reason is blank
  S --> C : throw ValidationException(IEM-149)
  C --> UI : 400 "Enter a quarantine reason for this batch"
  UI --> M : inline field error
else reason present
  S -> R : existsByIncidentAndBatch(incidentId, batchId)
  activate R
  R -> DB : SELECT 1 FROM incident_quarantine_batches\n WHERE incident_id=? AND batch_id=?
  activate DB
  DB --> R : exists?
  deactivate DB
  R --> S : boolean
  deactivate R
  alt batch already quarantined
    S --> C : throw ConflictException(IEM-149B)
    C --> UI : 409 "Cannot be undone"
    UI --> M : conflict toast
  else valid
    S -> IQB : new IncidentQuarantineBatch(batchId, batchCode, reason,\nquarantinedAt=now(), quarantinedById=actor.userId)
    activate IQB
    IQB --> S : quarantineBatch(reason NOT NULL)
    deactivate IQB
    S -> R : save(quarantineBatch)
    activate R
    R -> DB : INSERT incident_quarantine_batches (reason NOT NULL)
    activate DB
    DB --> R : 1 row inserted
    deactivate DB
    R --> S : IncidentQuarantineBatch
    deactivate R
    S -> BP : markQuarantined(batchId, incidentCode, reason, actor.userId)
    activate BP
    BP --> S : ack
    deactivate BP
    S -> DB : INSERT incident_events (type=QUARANTINE)
    activate DB
    DB --> S : 1 row inserted
    deactivate DB
    S -> MQ : publish(BatchQuarantined) -> Service Caterers\n(return the batch to its supplier)
    activate MQ
    MQ --> S : accepted
    deactivate MQ
    S --> C : QuarantineResult{ quarantined:["BATCH-MP-8820"] }
    C --> UI : 200
    UI --> M : batch row marked "Quarantined"
  end
end
deactivate S
deactivate C
deactivate UI
@enduml
```

**Description.** Backs UC-041/UC-009. The candidate list is derived, never stored: the 24-hour investigation window's anchor timestamp falls back through `occurredAt → mealDate → reportedAt` (BR-149, revised 2026-07-27), and the dish list comes from `ServingDishPort` (MF09's actual serving records, not MF05's menu catalog) so the candidates are dishes that were truly served, not merely planned. Quarantine itself is a **one-way** action (ADR-M11-3): the mandatory `reason` is enforced at the API boundary (`IEM-149`) and re-quarantining an already-locked batch is a hard `409` (`IEM-149B`), never a silent no-op — there is deliberately no unlock endpoint after BR-217 replaced "quarantine pending lab result" with "quarantine, then return to supplier immediately."

### 3.4 Sequence Diagram — Error Paths

```plantuml
@startuml MF11_F2_ErrorPaths
title Sequence Diagram: Incident Investigation — Error Paths

skinparam linetype ortho
skinparam backgroundColor #FFFFFF

skinparam stereotype {
    CBackgroundColor #E8F5E9
    CBorderColor #2E7D32
    EBackgroundColor #E8F5E9
    EBorderColor #2E7D32
    ABackgroundColor #E8F5E9
    ABorderColor #2E7D32
    IBackgroundColor #E8F5E9
    IBorderColor #2E7D32
}

skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontName Arial
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontName Arial
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontName Arial
skinparam EntityFontSize 13

skinparam CollectionsBackgroundColor #F4FBF4
skinparam CollectionsBorderColor #2E7D32
skinparam CollectionsFontColor #212121
skinparam CollectionsFontName Arial
skinparam CollectionsFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontName Arial
skinparam DatabaseFontSize 13

skinparam sequence {
    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontName Arial
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontName Arial
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #1B5E20
    GroupHeaderBackgroundColor #C8E6C9
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Medical Staff"                          as M
boundary    ":IncidentInvestigationScreen"           as UI      <<Boundary>>
control     ":IncidentInvestigationServiceImpl"      as S       <<Service>>
collections ":IncidentRepository"                    as R       <<Repository>>
database    ":PostgreSQL"                            as DB

== Case 1: assess a HIGH-severity incident with an out-of-range value ==
M -> UI : set severity = "CRITICAL" -> "Confirm assessment"
activate UI
UI -> S : POST /incidents/INC-2026-015/assess { severity:"CRITICAL" }
activate S
S -> S : validate severity in {LOW, MEDIUM, HIGH}
S --> UI : 400 { code:"IEM-147", details:[{field:"severity",\n  message:"must be one of LOW, MEDIUM, HIGH"}] }
deactivate S
UI --> M : inline field error
deactivate UI

== Case 2: quarantine a batch already quarantined for this incident ==
M -> UI : select "BATCH-MP-8820" -> "Quarantine"
activate UI
UI -> S : POST /incidents/INC-2026-015/quarantine-batches\n[{batchCode:"BATCH-MP-8820", reason:"..."}]
activate S
S -> R : existsByIncidentAndBatch(incidentId, batchId)
activate R
R -> DB : SELECT ... WHERE incident_id=? AND batch_id=?
activate DB
DB --> R : row exists
deactivate DB
R --> S : true
deactivate R
S --> UI : 409 { code:"IEM-149B",\n message:"Batch already quarantined — cannot be undone" }
deactivate S
UI --> M : conflict toast
deactivate UI

== Case 3: any write attempted after the official report has been submitted ==
M -> UI : edit root-cause field -> "Save"
activate UI
UI -> S : POST /incidents/INC-2026-014/root-cause { ... }
activate S
S -> R : findByCode(schoolId, code)
activate R
R -> DB : SELECT report_exported FROM incidents WHERE code=?
activate DB
DB --> R : true
deactivate DB
R --> S : Incident(reportExported=true)
deactivate R
S --> UI : 409 { code:"IEM-RO",\n  message:"Record locked after official report submission — no exceptions except Close" }
deactivate S
UI --> M : blocking banner
deactivate UI
@enduml
```

**Description.** Three rejection paths that share one property: none of them are UI-only checks. `IEM-147` and `IEM-149B` are re-validated server-side even though the mock's original client already blocked them with `toast.error`/`disabled` (CF-M11-9) — a food-safety investigation cannot trust the client to enforce BR-147/BR-217. `IEM-RO` is the broadest guard: once `reportExported = true`, **every** write endpoint on the incident and its child tables returns it, with the sole exception of `IncidentClosureService.close()`, because closing happens logically *after* the report is locked, not before (ADR-M11-8).

### 3.5 State Chart — Incident Lifecycle

Backs UC-041/UC-009/UC-116/UC-120 and every downstream feature that reads `Incident.status`. The six states are strictly forward-moving: severity classification (§3.2) starts the investigation, the CAPA lifecycle of Feature 3 drives the two middle transitions, and the report-then-close pattern of Feature 4 closes the loop.

```plantuml
@startuml MF11_F2_StateChart_Incident
title State Diagram: Incident Lifecycle

skinparam nodesep 90
skinparam ranksep 110
skinparam linetype ortho
skinparam backgroundColor #FFFFFF

skinparam state {
    BackgroundColor #F4FBF4
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 13
}

skinparam arrow {
    Color #2E7D32
    FontColor #1B5E20
    FontName Arial
    FontSize 12
}

skinparam note {
    BackgroundColor #E8F5E9
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 12
}

skinparam startColor #2E7D32
skinparam endColor #2E7D32

state SEVERITY_PENDING
state INVESTIGATING
state CAPA_PENDING
state CAPA_APPROVED
state VALIDATED
state CLOSED

[*] --> SEVERITY_PENDING : openInvestigation()
SEVERITY_PENDING --> INVESTIGATING : assessAndStart() - severity set
INVESTIGATING --> CAPA_PENDING : CorrectiveAction submitted for approval
CAPA_PENDING --> CAPA_APPROVED : CorrectiveAction approved by School Admin
CAPA_APPROVED --> VALIDATED : CorrectiveAction validate() - all affected students recovered
VALIDATED --> VALIDATED : IncidentReportService.generateAndSubmit() - report exported
VALIDATED --> CLOSED : close() - all gates pass (constraint C1)
CLOSED --> [*]
@enduml
```

---

## 4. Feature 3 — Corrective Action Plan (CAPA) Lifecycle

A 6-state workflow shared by three roles: Medical Staff drafts and validates, School Administration approves the plan **before** it is carried out, Service Caterers executes and submits evidence. Backs UC-005, UC-038, UC-082, UC-168.

### 4.1 Class Diagram

```plantuml
@startuml MF11_F3_ClassDiagram
title Class Diagram: Corrective Action Plan (CAPA) Lifecycle

skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 55
skinparam ranksep 85
skinparam minClassWidth 130
skinparam wrapWidth 260
skinparam backgroundColor #FFFFFF

skinparam class {
    BackgroundColor #F4FBF4
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 13

    HeaderBackgroundColor #4CAF50
    HeaderFontColor #FFFFFF

    AttributeFontColor #212121
    AttributeFontName Arial
    AttributeFontSize 12
}

skinparam interface {
    BackgroundColor #f7ed93ff
    BorderColor #000000ff
    FontColor #000000ff
    FontName Arial
}

skinparam arrow {
    Color #162c00ff
    FontColor #162c00ff
    FontName Arial
    FontSize 11
}

skinparam stereotype {
    CBackgroundColor #f6eedcff
    CBorderColor #000000ff
    EBackgroundColor #f6eedcff
    EBorderColor #000000ff
    ABackgroundColor #f6eedcff
    ABorderColor #000000ff
    IBackgroundColor #f6eedcff
    IBorderColor #000000ff
}

hide empty members

package "mf11.domain" {
  class CorrectiveAction <<Entity>> {
    code
    incidentId
    inspectionId
    assigneeSupplierId
    status
  }
  enum CapaStatus {
    DRAFT
    PENDING_ADMIN_APPROVAL
    APPROVED_FOR_IMPLEMENTATION
    IN_PROGRESS
    SUBMITTED_EVIDENCE
    COMPLETED
  }
}

package "mf11.dto" {
  class CreateCapaRequest <<DTO>> {
    incidentCode
    description
    assigneeLabel
    assigneeSupplierId
  }
  class CapaResponse <<DTO>> {
    id
    status
    incidentId
  }
  class RejectRequest <<DTO>> {
    reason
  }
  class SubmitEvidenceRequest <<DTO>> {
    evidenceNote
    files
  }
  class ValidateCapaRequest <<DTO>> {
    kitchenNote
    recoveredStudentIds
  }
  class ReworkRequest <<DTO>> {
    reason
  }
}

package "mf11.web" {
  class CorrectiveActionController <<Controller>> {
    + list()
    + detail()
    + create()
    + approve()
    + reject()
    + accept()
    + submitEvidence()
    + validate()
    + requestRework()
  }
}

package "mf11.service" {
  interface CapaService <<Service>> {
    + list()
    + detail()
    + create()
    + submitForApproval()
    + approvePlan()
    + rejectPlan()
    + accept()
    + submitEvidence()
    + validate()
    + requestRework()
  }
}

package "mf11.repository" {
  interface CorrectiveActionRepository <<Repository>> {
    + findByCodeForUpdate()
    + existsByIncidentId()
    + save()
  }
}

package "mf11.event" {
  class IncidentEventRecorder <<Component>> {
    + record()
  }
}

' ---------- web -> service ----------
CorrectiveActionController ..> CapaService

' ---------- service -> repository / recorder ----------
CapaService ..> CorrectiveActionRepository
CapaService ..> IncidentEventRecorder : CAPA events

' ---------- repository -> domain ----------
CorrectiveActionRepository ..> CorrectiveAction

' ---------- domain -> enum ----------
CorrectiveAction --> CapaStatus

' ---------- service -> dto ----------
CapaService ..> CreateCapaRequest
CapaService ..> RejectRequest
CapaService ..> SubmitEvidenceRequest
CapaService ..> ValidateCapaRequest
CapaService ..> ReworkRequest
CapaService ..> CapaResponse

' ---------- layout hints (balance width vs height) ----------
CreateCapaRequest -[hidden]down- RejectRequest
RejectRequest -[hidden]down- SubmitEvidenceRequest
CapaResponse -[hidden]down- ValidateCapaRequest
ValidateCapaRequest -[hidden]down- ReworkRequest
@enduml
```

**Description.** `ADR-M11-4` is encoded here as two separate mechanisms working together: the DB partial unique index `uq_capa_one_per_incident` makes "1 Incident = 1 CAPA" physically impossible to violate, and `CapaService` exposes **six narrow, role-guarded action endpoints** rather than one generic `PUT` — each one checks both the caller's role and the CAPA's current status before transitioning, closing the gap the mock's original `PUT`-merge design left open (a Service Caterers account could otherwise set `status=COMPLETED` directly, CF-M11-9). `IncidentEventRecorder` is the same append-only writer used across every feature — every CAPA transition also leaves a row in `incident_events`.

### 4.2 Sequence Diagram — CAPA Lifecycle Across Three Roles

```plantuml
@startuml MF11_F3_CapaLifecycle
title Sequence Diagram: CAPA Lifecycle Across Three Roles

skinparam linetype ortho
skinparam backgroundColor #FFFFFF

skinparam stereotype {
    CBackgroundColor #E8F5E9
    CBorderColor #2E7D32
    EBackgroundColor #E8F5E9
    EBorderColor #2E7D32
    ABackgroundColor #E8F5E9
    ABorderColor #2E7D32
    IBackgroundColor #E8F5E9
    IBorderColor #2E7D32
}

skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontName Arial
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontName Arial
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontName Arial
skinparam EntityFontSize 13

skinparam CollectionsBackgroundColor #F4FBF4
skinparam CollectionsBorderColor #2E7D32
skinparam CollectionsFontColor #212121
skinparam CollectionsFontName Arial
skinparam CollectionsFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontName Arial
skinparam DatabaseFontSize 13

skinparam sequence {
    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontName Arial
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontName Arial
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #1B5E20
    GroupHeaderBackgroundColor #C8E6C9
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Medical Staff"                          as M
actor       "School Admin"                           as A
actor       "Service Caterers"                       as SCa
boundary    ":CapaWorkflowScreen"                    as UI      <<Boundary>>
control     ":CorrectiveActionController"            as C       <<Controller>>
control     ":CapaServiceImpl"                       as S       <<Service>>
collections ":CorrectiveActionRepository"            as R       <<Repository>>
entity      ":CorrectiveAction"                      as CAPA    <<Entity>>
database    ":PostgreSQL"                            as DB
queue       ":DomainEventBus"                        as MQ      <<EventBus>>

M -> UI : fill description, assignee -> "Create CAPA"
activate UI
UI -> C : POST /corrective-actions\n{incidentCode:"INC-2026-015", description, assignee}
activate C
C -> S : create(req, actor)
activate S
S -> R : existsByIncidentId(incidentId)
activate R
R -> DB : check uq_capa_one_per_incident
activate DB
DB --> R : exists?
deactivate DB
R --> S : boolean
deactivate R
alt this incident already has a CAPA
  S --> C : throw ConflictException(IEM-216)
  C --> UI : 409 IEM-216 "1 Incident = 1 CAPA"
  UI --> M : conflict toast
else no CAPA yet
  S -> CAPA : new CorrectiveAction(status=PENDING_ADMIN_APPROVAL,\n submittedForApprovalAt=now)
  activate CAPA
  CAPA --> S : capa(PENDING_ADMIN_APPROVAL)
  deactivate CAPA
  S -> R : save(capa)
  activate R
  R -> DB : INSERT corrective_actions (PENDING_ADMIN_APPROVAL,\n submitted_for_approval_at=now)
  activate DB
  DB --> R : 1 row inserted
  deactivate DB
  R --> S : CorrectiveAction(CAPA-2026-034)
  deactivate R
  S -> DB : UPDATE incidents SET status='CAPA_PENDING'
  activate DB
  DB --> S : 1 row updated
  deactivate DB
  S -> DB : INSERT incident_events (CAPA_SUBMITTED)
  activate DB
  DB --> S : 1 row inserted
  deactivate DB
  S -> MQ : publish(CapaSubmittedForApproval) -> School Admin
  activate MQ
  MQ --> S : accepted
  deactivate MQ
  S --> C : CapaResponse{ status:"PENDING_ADMIN_APPROVAL" }
  C --> UI : 201
  UI --> M : "CAPA submitted for approval"
end
deactivate S
deactivate C
deactivate UI

A -> UI : open CAPA-2026-034 -> "Approve"
activate UI
UI -> C : POST /corrective-actions/CAPA-2026-034/approve
activate C
C -> S : approvePlan(code, actor)
activate S
S -> S : guard role=SCHOOL_ADMIN\nguard status=PENDING_ADMIN_APPROVAL
S -> CAPA : approve(now())
activate CAPA
CAPA --> S : status = APPROVED_FOR_IMPLEMENTATION, approvedAt
deactivate CAPA
S -> R : save(capa)
activate R
R -> DB : UPDATE status='APPROVED_FOR_IMPLEMENTATION', approved_at=now
activate DB
DB --> R : 1 row updated
deactivate DB
R --> S : CorrectiveAction(APPROVED_FOR_IMPLEMENTATION)
deactivate R
S -> DB : UPDATE incidents SET status='CAPA_APPROVED'\nINSERT incident_events (CAPA_APPROVED)
activate DB
DB --> S : rows updated
deactivate DB
S -> MQ : publish(CapaApproved) -> Service Caterers
activate MQ
MQ --> S : accepted
deactivate MQ
S --> C : CapaResponse{ status:"APPROVED_FOR_IMPLEMENTATION" }
deactivate S
C --> UI : 200
deactivate C
UI --> A : "Plan approved"
deactivate UI

SCa -> UI : open CAPA-2026-034 -> "Accept"
activate UI
UI -> C : POST /corrective-actions/CAPA-2026-034/accept
activate C
C -> S : accept(code, actor)
activate S
S -> S : guard role=SERVICE_CATERERS\nguard status=APPROVED_FOR_IMPLEMENTATION
S -> CAPA : startWork()
activate CAPA
CAPA --> S : status = IN_PROGRESS
deactivate CAPA
S -> R : save(capa)
activate R
R -> DB : UPDATE status='IN_PROGRESS'
activate DB
DB --> R : 1 row updated
deactivate DB
R --> S : CorrectiveAction(IN_PROGRESS)
deactivate R
S --> C : CapaResponse{ status:"IN_PROGRESS" }
deactivate S
C --> UI : 200
deactivate C
UI --> SCa : "CAPA accepted"
deactivate UI

SCa -> UI : attach evidenceNote, files[] -> "Submit evidence"
activate UI
UI -> C : POST /corrective-actions/CAPA-2026-034/submit-evidence\n{evidenceNote, files[]}
activate C
C -> S : submitEvidence(code, req, actor)
activate S
S -> S : guard files.size >= 1 (UC-082 step 5)
S -> CAPA : submitEvidence(evidenceNote, files, now())
activate CAPA
CAPA --> S : status = SUBMITTED_EVIDENCE, evidenceSubmittedAt
deactivate CAPA
S -> R : save(capa)
activate R
R -> DB : UPDATE status='SUBMITTED_EVIDENCE', evidence_submitted_at=now
activate DB
DB --> R : 1 row updated
deactivate DB
R --> S : CorrectiveAction(SUBMITTED_EVIDENCE)
deactivate R
S -> DB : INSERT incident_events (CAPA_EVIDENCE)
activate DB
DB --> S : 1 row inserted
deactivate DB
S -> MQ : publish(CapaEvidenceSubmitted) -> Medical Staff
activate MQ
MQ --> S : accepted
deactivate MQ
S --> C : CapaResponse{ status:"SUBMITTED_EVIDENCE" }
deactivate S
C --> UI : 200
deactivate C
UI --> SCa : "Evidence submitted"
deactivate UI

M -> UI : review evidence, mark students recovered -> "Validate"
activate UI
UI -> C : POST /corrective-actions/CAPA-2026-034/validate\n{kitchenNote, recoveredStudentIds[]}
activate C
C -> S : validate(code, req, actor)
activate S
S -> DB : guard: are all affected students recovered?
activate DB
DB --> S : notRecoveredCount
deactivate DB
alt students still not recovered
  S --> C : throw ConflictException(IEM-155)
  C --> UI : 409 IEM-155 "N students not yet confirmed recovered"
  UI --> M : conflict toast
else all recovered
  S -> CAPA : validate()
  activate CAPA
  CAPA --> S : status = COMPLETED
  deactivate CAPA
  S -> R : save(capa)
  activate R
  R -> DB : UPDATE status='COMPLETED'; incidents SET status='VALIDATED'\nUPDATE incident_affected_students SET recovered=true
  activate DB
  DB --> R : rows updated
  deactivate DB
  R --> S : CorrectiveAction(COMPLETED)
  deactivate R
  S -> DB : INSERT incident_events (CAPA_VALIDATED)
  activate DB
  DB --> S : 1 row inserted
  deactivate DB
  S --> C : CapaResponse{ status:"COMPLETED" }
  C --> UI : 200
  UI --> M : "CAPA validated"
end
deactivate S
deactivate C
deactivate UI
@enduml
```

**Description.** Backs UC-005 (approve/reject), UC-038 (accept/submit-evidence), UC-082 (submit-evidence detail), UC-168 (validate/rework). The chain enforces BR-153's core requirement — School Administration approves the **plan** before Service Caterers may act on it, not after the fact — by making `accept` require `APPROVED_FOR_IMPLEMENTATION` as a precondition. Rejecting a plan (`AF`, not drawn above) sends the CAPA back to `DRAFT` with a mandatory `rejectReason` (BR-216 step 2); Medical Staff revises and resubmits through the same `submit-for-approval` transition. `validate`'s gate reuses the same recovered-student check that also blocks incident closure in Feature 4, so a CAPA cannot be marked complete while a child is still symptomatic even if every corrective step was executed correctly. The one alternate flow not drawn above (`AF1`, UC-168) is `rework`: it sends a `COMPLETED`/rejected CAPA back to `IN_PROGRESS` with a mandatory `reason`, keeping the already-approved plan as-is — no re-approval loop (BR-216 step 5).

### 4.3 Sequence Diagram — Error Path: Duplicate CAPA & Plan Rejection

```plantuml
@startuml MF11_F3_ErrorPaths
title Sequence Diagram: CAPA — Error Path: Duplicate CAPA & Plan Rejection

skinparam linetype ortho
skinparam backgroundColor #FFFFFF

skinparam stereotype {
    CBackgroundColor #E8F5E9
    CBorderColor #2E7D32
    EBackgroundColor #E8F5E9
    EBorderColor #2E7D32
    ABackgroundColor #E8F5E9
    ABorderColor #2E7D32
    IBackgroundColor #E8F5E9
    IBorderColor #2E7D32
}

skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontName Arial
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontName Arial
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontName Arial
skinparam EntityFontSize 13

skinparam CollectionsBackgroundColor #F4FBF4
skinparam CollectionsBorderColor #2E7D32
skinparam CollectionsFontColor #212121
skinparam CollectionsFontName Arial
skinparam CollectionsFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontName Arial
skinparam DatabaseFontSize 13

skinparam sequence {
    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontName Arial
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontName Arial
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #1B5E20
    GroupHeaderBackgroundColor #C8E6C9
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Medical Staff"                          as M
actor       "School Admin"                           as A
actor       "Service Caterers"                       as A2
boundary    ":CapaWorkflowScreen"                    as UI      <<Boundary>>
control     ":CapaServiceImpl"                       as S       <<Service>>
collections ":CorrectiveActionRepository"            as R       <<Repository>>
database    ":PostgreSQL"                            as DB

== Case 1: a second CAPA is created for an incident that already has one ==
M -> UI : "Create CAPA" for an incident that already has one
activate UI
UI -> S : POST /corrective-actions { incidentCode:"INC-2026-015", ... }
activate S
S -> R : existsByIncidentId(incidentId)
activate R
R -> DB : SELECT 1 FROM corrective_actions WHERE incident_id=?
activate DB
DB --> R : row exists
deactivate DB
R --> S : true
deactivate R
S --> UI : 409 { code:"IEM-216", message:"1 Incident = 1 CAPA" }
deactivate S
UI --> M : conflict toast
deactivate UI

== Case 2: School Admin rejects the plan without a reason ==
A -> UI : "Reject" without entering a reason
activate UI
UI -> S : POST /corrective-actions/CAPA-2026-034/reject { }
activate S
S -> S : validate reason is non-blank (step 2)
S --> UI : 400 { code:"IEM-216", details:[{field:"reason", message:"required"}] }
deactivate S
UI --> A : inline field error
deactivate UI

== Case 3: Service Caterers attempts to self-approve their own CAPA ==
A2 -> UI : "Approve" (JWT role=SERVICE_CATERERS)
activate UI
UI -> S : POST /corrective-actions/CAPA-2026-034/approve\n(JWT role=SERVICE_CATERERS)
activate S
S -> S : guard role in {SCHOOL_ADMIN}
S --> UI : 403 { code:"IEM-005", message:"Access denied" }
deactivate S
UI --> A2 : "Access denied"
deactivate UI
@enduml
```

**Description.** Case 1 shows the two-layer defense typical of MF11: an application-level check that fails fast with a business-readable message, backstopped by a DB constraint that would reject the row even if the service check were bypassed. Case 3 encodes the separation-of-duty requirement explicit in §4.3 of the TDS — the person who drafts a CAPA (Medical Staff), the person who approves it (School Administration), and the person who executes it (Service Caterers) must always be three different roles.

### 4.4 State Chart — CAPA Lifecycle

Backs UC-005/UC-038/UC-082/UC-168 (BR-216). The six forward states are shown across the top row; the two documented exceptions — School Admin's `reject` sending the plan back to `DRAFT`, and Medical Staff's `rework` sending a completed/rejected plan back to `IN_PROGRESS` — are the only backward edges the workflow allows.

```plantuml
@startuml MF11_F3_StateChart_Capa
title State Diagram: CAPA Lifecycle

skinparam nodesep 90
skinparam ranksep 110
skinparam linetype ortho
skinparam backgroundColor #FFFFFF

skinparam state {
    BackgroundColor #F4FBF4
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 13
}

skinparam arrow {
    Color #2E7D32
    FontColor #1B5E20
    FontName Arial
    FontSize 12
}

skinparam note {
    BackgroundColor #E8F5E9
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 12
}

skinparam startColor #2E7D32
skinparam endColor #2E7D32

state DRAFT
state PENDING_ADMIN_APPROVAL
state APPROVED_FOR_IMPLEMENTATION
state IN_PROGRESS
state SUBMITTED_EVIDENCE
state COMPLETED

[*] --> DRAFT : Medical Staff drafts
DRAFT --> PENDING_ADMIN_APPROVAL : submitForApproval()
PENDING_ADMIN_APPROVAL --> APPROVED_FOR_IMPLEMENTATION : approvePlan() - School Admin
PENDING_ADMIN_APPROVAL --> DRAFT : rejectPlan(reason) - mandatory rejectReason
APPROVED_FOR_IMPLEMENTATION --> IN_PROGRESS : accept() - Service Caterers
IN_PROGRESS --> SUBMITTED_EVIDENCE : submitEvidence(files >= 1)
SUBMITTED_EVIDENCE --> COMPLETED : validate() - all affected students recovered
COMPLETED --> IN_PROGRESS : requestRework(reason) - Medical Staff, no re-approval loop
SUBMITTED_EVIDENCE --> IN_PROGRESS : requestRework(reason)
COMPLETED --> [*]
@enduml
```

---

## 5. Feature 4 — Incident Closure & Official Report

The final stage of an investigation: Medical Staff generates the official report (which locks the whole record), then School Administration closes the incident once every CAPA and every affected student is accounted for. Backs UC-013, UC-033, UC-031, UC-121.

### 5.1 Class Diagram

```plantuml
@startuml MF11_F4_ClassDiagram
title Class Diagram: Incident Closure & Official Report

skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 55
skinparam ranksep 85
skinparam minClassWidth 130
skinparam wrapWidth 260
skinparam backgroundColor #FFFFFF

skinparam class {
    BackgroundColor #F4FBF4
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 13

    HeaderBackgroundColor #4CAF50
    HeaderFontColor #FFFFFF

    AttributeFontColor #212121
    AttributeFontName Arial
    AttributeFontSize 12
}

skinparam interface {
    BackgroundColor #f7ed93ff
    BorderColor #000000ff
    FontColor #000000ff
    FontName Arial
}

skinparam arrow {
    Color #162c00ff
    FontColor #162c00ff
    FontName Arial
    FontSize 11
}

skinparam stereotype {
    CBackgroundColor #f6eedcff
    CBorderColor #000000ff
    EBackgroundColor #f6eedcff
    EBorderColor #000000ff
    ABackgroundColor #f6eedcff
    ABorderColor #000000ff
    IBackgroundColor #f6eedcff
    IBorderColor #000000ff
}

hide empty members

package "mf11.dto" {
  class CloseIncidentRequest <<DTO>> {
    closingStatement
  }
  class ClosureReadiness <<DTO>> {
    pendingCapa
    notRecovered
    openBatches
    ready
  }
  class SubmitReportRequest <<DTO>> {
    officialClinicalNotes
  }
  class IncidentReportResponse <<DTO>> {
    reportExported
    generatedAt
    content
  }
  class IncidentExportQuery <<DTO>> {
    format
    from
    to
  }
  class ExportResult <<DTO>> {
    fileName
    contentType
    size
  }
  enum ExportFormat {
    PDF
    EXCEL
  }
}

package "mf11.web" {
  class IncidentClosureController <<Controller>> {
    + close()
    + readiness()
  }
  class IncidentReportController <<Controller>> {
    + generate()
    + view()
    + export()
  }
}

package "mf11.service" {
  interface IncidentClosureService <<Service>> {
    + close()
    + readiness()
  }
  interface IncidentReportService <<Service>> {
    + generateAndSubmit()
    + view()
    + export()
  }
}

package "mf11.repository" {
  interface IncidentRepository <<Repository>> {
    + findByCodeForUpdate()
    + save()
  }
  interface CorrectiveActionRepository <<Repository>> {
    + findByIncidentIdAndStatusNot()
  }
}

package "mf11.port" {
  interface InventoryBatchPort <<Port>> {
    + isAtTerminalState()
  }
}

' ---------- web -> service ----------
IncidentClosureController ..> IncidentClosureService
IncidentReportController ..> IncidentReportService

' ---------- service -> repository / port ----------
IncidentClosureService ..> IncidentRepository
IncidentClosureService ..> CorrectiveActionRepository : Constraint C1
IncidentClosureService ..> InventoryBatchPort : terminal-state check
IncidentReportService ..> IncidentRepository : locks record

' ---------- service -> dto ----------
IncidentClosureService ..> CloseIncidentRequest
IncidentClosureService ..> ClosureReadiness
IncidentReportService ..> SubmitReportRequest
IncidentReportService ..> IncidentReportResponse
IncidentReportService ..> IncidentExportQuery
IncidentReportService ..> ExportResult
IncidentExportQuery --> ExportFormat

' ---------- layout hints (balance width vs height) ----------
CloseIncidentRequest -[hidden]down- ClosureReadiness
ClosureReadiness -[hidden]down- SubmitReportRequest
IncidentReportResponse -[hidden]down- IncidentExportQuery
IncidentExportQuery -[hidden]down- ExportResult
ExportResult -[hidden]down- ExportFormat
@enduml
```

**Description.** `IncidentClosureService.close()` and `IncidentReportService.generateAndSubmit()` are two independent locks on the same aggregate root, applied in a specific order: the official report can be submitted while the incident is still `VALIDATED` (locking the record for edits — ADR-M11-8), and closing happens *after* that, as the one write endpoint explicitly exempted from the read-only lock. `readiness()` exists purely so the School Admin UI can render a pre-flight checklist instead of discovering the blocking conditions only after clicking "Close".

### 5.2 Sequence Diagram — Close Incident (Guarded, Constraint C1)

```plantuml
@startuml MF11_F4_CloseIncident
title Sequence Diagram: Close Incident (Guarded, Constraint C1)

skinparam linetype ortho
skinparam backgroundColor #FFFFFF

skinparam stereotype {
    CBackgroundColor #E8F5E9
    CBorderColor #2E7D32
    EBackgroundColor #E8F5E9
    EBorderColor #2E7D32
    ABackgroundColor #E8F5E9
    ABorderColor #2E7D32
    IBackgroundColor #E8F5E9
    IBorderColor #2E7D32
}

skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontName Arial
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontName Arial
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontName Arial
skinparam EntityFontSize 13

skinparam CollectionsBackgroundColor #F4FBF4
skinparam CollectionsBorderColor #2E7D32
skinparam CollectionsFontColor #212121
skinparam CollectionsFontName Arial
skinparam CollectionsFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontName Arial
skinparam DatabaseFontSize 13

skinparam sequence {
    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontName Arial
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontName Arial
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #1B5E20
    GroupHeaderBackgroundColor #C8E6C9
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "School Admin"                           as A
boundary    ":IncidentClosureScreen"                 as UI      <<Boundary>>
control     ":IncidentClosureServiceImpl"            as S       <<Service>>
collections ":IncidentRepository"                    as R       <<Repository>>
collections ":CorrectiveActionRepository"            as CR      <<Repository>>
boundary    ":InventoryBatchPort"                    as BP      <<Port>>
entity      ":Incident"                              as INC     <<Entity>>
database    ":PostgreSQL"                            as DB

A -> UI : review readiness checklist -> "Close incident"\n{ closingStatement }
activate UI
UI -> S : POST /incidents/INC-2026-014/close\n{ closingStatement }
activate S
S -> S : guard role=SCHOOL_ADMIN (Medical Staff cannot close)
alt not School Admin
  S --> UI : 403 IEM-005
  UI --> A : "Access denied"
else School Admin
  S -> CR : findByIncidentIdAndStatusNot(incidentId, COMPLETED)
  activate CR
  CR -> DB : SELECT ... WHERE incident_id=? AND status <> 'COMPLETED'
  activate DB
  DB --> CR : rows
  deactivate DB
  CR --> S : List<CorrectiveAction>
  deactivate CR
  alt any CAPA is not COMPLETED
    S --> UI : 409 IEM-155 { pendingCapa:[{code:"CAPA-2026-033", status:"SUBMITTED_EVIDENCE"}] }
    UI --> A : checklist highlights pending CAPA
  else all CAPA COMPLETED
    S -> R : findAffectedStudentsNotRecovered(incidentId)
    activate R
    R -> DB : SELECT affected students WHERE recovered=false
    activate DB
    DB --> R : rows
    deactivate DB
    R --> S : List<IncidentAffectedStudent>
    deactivate R
    alt any student not recovered
      S --> UI : 409 IEM-155 { notRecovered:[{studentId, studentName}] }
      UI --> A : checklist highlights unrecovered students
    else all recovered
      S -> BP : isAtTerminalState(batchId) for every quarantined batch
      activate BP
      BP --> S : terminal? (RETURNED_TO_SUPPLIER / DISPOSED)
      deactivate BP
      alt any batch not at a terminal state
        S --> UI : 409 IEM-217 { openBatches:["BATCH-MP-8820"] }
        UI --> A : checklist highlights open batches
      else all conditions satisfied
        S -> INC : close(closingStatement, actor.userId, now())
        activate INC
        INC --> S : status = CLOSED, closedAt, closedById
        deactivate INC
        S -> R : save(incident)
        activate R
        R -> DB : UPDATE incidents SET status='CLOSED',\n closed_at=now, closed_by_id, closing_statement
        activate DB
        DB --> R : 1 row updated
        deactivate DB
        R --> S : Incident(CLOSED)
        deactivate R
        S -> DB : INSERT incident_events (type=CLOSED)
        activate DB
        DB --> S : 1 row inserted
        deactivate DB
        S --> UI : 200
        UI --> A : "Incident closed"
      end
    end
  end
end
deactivate S
deactivate UI
@enduml
```

**Description.** Backs UC-013. Three independent gates must all pass, evaluated server-side regardless of what the client already checked: no open CAPA (constraint C1), no unrecovered student (BR-155), and no quarantined batch still short of a terminal disposition at MF06 (BR-217 — this replaced an older audit rule that wrongly assumed quarantined batches get released before closure, CF-M11-11). All three conditions can coexist on the same incident, so the response carries independent detail arrays rather than a single generic message.

### 5.3 Sequence Diagram — Generate & Submit Official Incident Report

```plantuml
@startuml MF11_F4_SubmitReport
title Sequence Diagram: Generate & Submit Official Incident Report

skinparam linetype ortho
skinparam backgroundColor #FFFFFF

skinparam stereotype {
    CBackgroundColor #E8F5E9
    CBorderColor #2E7D32
    EBackgroundColor #E8F5E9
    EBorderColor #2E7D32
    ABackgroundColor #E8F5E9
    ABorderColor #2E7D32
    IBackgroundColor #E8F5E9
    IBorderColor #2E7D32
}

skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontName Arial
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontName Arial
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontName Arial
skinparam EntityFontSize 13

skinparam CollectionsBackgroundColor #F4FBF4
skinparam CollectionsBorderColor #2E7D32
skinparam CollectionsFontColor #212121
skinparam CollectionsFontName Arial
skinparam CollectionsFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontName Arial
skinparam DatabaseFontSize 13

skinparam sequence {
    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontName Arial
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontName Arial
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #1B5E20
    GroupHeaderBackgroundColor #C8E6C9
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Medical Staff"                          as M
boundary    ":IncidentClosureScreen"                 as UI      <<Boundary>>
control     ":IncidentReportServiceImpl"             as S       <<Service>>
collections ":IncidentRepository"                    as R       <<Repository>>
entity      ":Incident"                              as INC     <<Entity>>
database    ":PostgreSQL"                            as DB
queue       ":DomainEventBus"                        as MQ      <<EventBus>>

M -> UI : "Generate official report"
activate UI
UI -> S : POST /incidents/INC-2026-015/report
activate S
S -> S : guard role=MEDICAL_STAFF
S -> R : findByCode(schoolId, code)
activate R
R -> DB : SELECT status FROM incidents WHERE code=?
activate DB
DB --> R : row (status)
deactivate DB
R --> S : Incident
deactivate R
alt status <> VALIDATED
  S --> UI : 409 "Report can only be generated once the incident is Validated"
  UI --> M : conflict toast
else status = VALIDATED
  S -> DB : render official report content from Incident +\n children (affected students, root cause, CAPA, events)
  activate DB
  DB --> S : report content
  deactivate DB
  S -> INC : markReportExported()
  activate INC
  INC --> S : reportExported = true
  deactivate INC
  S -> R : save(incident)
  activate R
  R -> DB : UPDATE incidents SET report_exported=true
  activate DB
  DB --> R : 1 row updated
  deactivate DB
  R --> S : Incident(reportExported=true)
  deactivate R
  S -> DB : INSERT incident_events (type=REPORT_SENT)
  activate DB
  DB --> S : 1 row inserted
  deactivate DB
  S -> MQ : publish(IncidentReportSubmitted) -> downstream consumers
  activate MQ
  MQ --> S : accepted
  deactivate MQ
  S --> UI : 200 { reportExported:true, generatedAt }
  UI --> M : "Report submitted and locked"
end
deactivate S
deactivate UI
@enduml
```

**Description.** Backs UC-033 (generate, Medical Staff) / UC-031 (export PDF/Excel, School Admin) / UC-121 (view, School Admin). The decision recorded in ADR-M11-8 is deliberately blunt: submitting the official report is a **whole-record**, **all-actor**, **no-unlock** lock, matching the real-world fact that a document already filed with a regulator or the school board cannot be quietly edited afterward — any correction has to happen through a supplementary record outside the system, which is the correct behavior for administrative paperwork with legal weight, not a system limitation to work around.

---

## 6. Feature 5 — Violation Management (Unit-Level)

Records violations committed by the Service Caterers **unit** (never an individual — BR-183, revised 2026-07-26), tracks their resolution through a severity-based approval branch, and gates the resolution behind a hand-signed, scanned record (BR-211). Backs UC-179, UC-180, UC-181, UC-182.

### 6.1 Class Diagram

```plantuml
@startuml MF11_F5_ClassDiagram
title Class Diagram: Violation Management (Unit-Level)

skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 55
skinparam ranksep 85
skinparam minClassWidth 130
skinparam wrapWidth 260
skinparam backgroundColor #FFFFFF

skinparam class {
    BackgroundColor #F4FBF4
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 13

    HeaderBackgroundColor #4CAF50
    HeaderFontColor #FFFFFF

    AttributeFontColor #212121
    AttributeFontName Arial
    AttributeFontSize 12
}

skinparam interface {
    BackgroundColor #f7ed93ff
    BorderColor #000000ff
    FontColor #000000ff
    FontName Arial
}

skinparam arrow {
    Color #162c00ff
    FontColor #162c00ff
    FontName Arial
    FontSize 11
}

skinparam stereotype {
    CBackgroundColor #f6eedcff
    CBorderColor #000000ff
    EBackgroundColor #f6eedcff
    EBorderColor #000000ff
    ABackgroundColor #f6eedcff
    ABorderColor #000000ff
    IBackgroundColor #f6eedcff
    IBorderColor #000000ff
}

hide empty members

package "mf11.domain" {
  class ViolationRecord <<Entity>> {
    code
    severity
    resolution
    status
    caterersRepresentativeId
    repeatSuspect
  }
  class ViolationFile <<Entity, Immutable>> {
    kind
    name
  }
  enum ViolationSeverity {
    LOW
    MEDIUM
    HIGH
  }
  enum ResolutionStatus {
    PENDING
    PENDING_APPROVAL
    APPROVED
    REJECTED
  }
  enum ViolationStatus {
    OPEN
    CLOSED
  }
  enum ViolationType {
    HYGIENE_VIOLATION
    PROCEDURE_SKIP
    OVERRIDE_ABUSE
    UNAUTHORIZED_ACTION
    OTHER
  }
  enum UnitSanctionLevel {
    WRITTEN_REMINDER
    CONTRACT_WARNING
    CONTRACT_PENALTY
    TERMINATION_PROPOSAL
  }
  enum FileKind {
    EVIDENCE
    SIGNED_RECORD
  }

  ViolationRecord "1" *-- "n" ViolationFile
}

package "mf11.dto" {
  class RecordViolationRequest <<DTO>> {
    subject
    description
    severity
    violationType
    occurredAt
    locationOfIncident
    caterersRepresentativeId
    witnessLabel
    evidenceFiles
  }
  class ViolationResponse <<DTO>> {
    id
    severity
    resolution
    status
    repeatSuspect
  }
  class SubmitResolutionRequest <<DTO>> {
    actionTaken
    unitSanctionLevel
  }
  class RejectRequest <<DTO>> {
    reason
  }
  class ViolationPdfResult <<DTO>> {
    fileName
    pdfVersion
    generatedAt
  }
}

package "mf11.web" {
  class ViolationController <<Controller>> {
    + registry()
    + detail()
    + record()
    + exportPdf()
    + uploadSignedRecord()
    + submitResolution()
    + approve()
    + reject()
  }
}

package "mf11.service" {
  interface ViolationService <<Service>> {
    + registry()
    + detail()
    + record()
    + isRepeatSuspect()
    + exportPdf()
    + uploadSignedRecord()
    + summary()
  }
  class RepeatViolationDetector <<Component>> {
    + isSuspect()
  }
  class ViolationPdfService <<Component>> {
    + render()
  }
  interface ViolationResolutionService <<Service>> {
    + submit()
    + approve()
    + reject()
  }
}

package "mf11.repository" {
  interface ViolationRecordRepository <<Repository>> {
    + findByCodeForUpdate()
    + findRecentSimilar()
    + save()
  }
}

' ---------- web -> service ----------
ViolationController ..> ViolationService
ViolationController ..> ViolationResolutionService

' ---------- service -> repository / component ----------
ViolationService ..> ViolationRecordRepository
ViolationService ..> RepeatViolationDetector : repeat-suspect hint
ViolationService ..> ViolationPdfService : signed-record export
ViolationResolutionService ..> ViolationRecordRepository

' ---------- repository -> domain ----------
ViolationRecordRepository ..> ViolationRecord
RepeatViolationDetector ..> ViolationRecordRepository : 90-day lookback

' ---------- domain -> enum ----------
ViolationRecord --> ViolationSeverity
ViolationRecord --> ResolutionStatus
ViolationRecord --> ViolationStatus
ViolationRecord --> ViolationType
ViolationRecord --> UnitSanctionLevel
ViolationFile --> FileKind

' ---------- service -> dto ----------
ViolationService ..> RecordViolationRequest
ViolationService ..> ViolationResponse
ViolationService ..> ViolationPdfResult
ViolationResolutionService ..> SubmitResolutionRequest
ViolationResolutionService ..> RejectRequest
ViolationResolutionService ..> ViolationResponse

' ---------- layout hints (balance width vs height) ----------
RecordViolationRequest -[hidden]down- SubmitResolutionRequest
SubmitResolutionRequest -[hidden]down- RejectRequest
ViolationResponse -[hidden]down- ViolationPdfResult
ViolationSeverity -[hidden]down- ResolutionStatus
ResolutionStatus -[hidden]down- ViolationStatus
ViolationType -[hidden]down- UnitSanctionLevel
UnitSanctionLevel -[hidden]down- FileKind
@enduml
```

**Description.** Every field that used to identify an individual staff member (`staffName`, `staffPosition`, `disciplinaryLevel`) is now read-only and absent from `RecordViolationRequest` — BR-183's 2026-07-26 revision moved the unit of accountability from person to Service Caterers **unit**, keyed by `caterersRepresentativeId`, and the three legacy columns exist only so the two historical rows recorded before the change (`VIO-2026-005`, `VIO-2026-007`) stay readable. `RepeatViolationDetector` is intentionally advisory: it sets `repeatSuspect=true` and returns a warning (`MSG-088`) but never blocks the save, matching BR-187's explicit "best-effort, no auto-escalation" wording.

### 6.2 Sequence Diagram — Record a Unit Violation

```plantuml
@startuml MF11_F5_RecordViolation
title Sequence Diagram: Record a Unit Violation

skinparam linetype ortho
skinparam backgroundColor #FFFFFF

skinparam stereotype {
    CBackgroundColor #E8F5E9
    CBorderColor #2E7D32
    EBackgroundColor #E8F5E9
    EBorderColor #2E7D32
    ABackgroundColor #E8F5E9
    ABorderColor #2E7D32
    IBackgroundColor #E8F5E9
    IBorderColor #2E7D32
}

skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontName Arial
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontName Arial
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontName Arial
skinparam EntityFontSize 13

skinparam CollectionsBackgroundColor #F4FBF4
skinparam CollectionsBorderColor #2E7D32
skinparam CollectionsFontColor #212121
skinparam CollectionsFontName Arial
skinparam CollectionsFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontName Arial
skinparam DatabaseFontSize 13

skinparam sequence {
    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontName Arial
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontName Arial
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #1B5E20
    GroupHeaderBackgroundColor #C8E6C9
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Medical Staff / Service Caterers"       as U
boundary    ":ViolationRegistryScreen"               as UI      <<Boundary>>
control     ":ViolationController"                   as C       <<Controller>>
control     ":ViolationServiceImpl"                  as S       <<Service>>
control     ":RepeatViolationDetector"               as RD      <<Component>>
collections ":ViolationRecordRepository"             as R       <<Repository>>
entity      ":ViolationRecord"                       as VIO     <<Entity>>
database    ":PostgreSQL"                            as DB

U -> UI : fill subject, description, severity, violationType,\noccurredAt, locationOfIncident, caterersRepresentativeId,\nwitness, evidenceFiles[] -> "Save"
activate UI
UI -> C : POST /api/v1/violations\n{subject, description, severity, violationType,\n occurredAt, locationOfIncident, caterersRepresentativeId,\n witness, evidenceFiles[], relatedIncidentId?, sourceInspectionId?}
activate C
C -> S : record(req, actor)
activate S
S -> S : validate required fields (description, >=1 evidence photo,\n locationOfIncident)
alt missing required field
  S --> C : throw ValidationException(IEM-085)
  C --> UI : 400
  UI --> U : inline field errors
else fields complete
  S -> RD : isSuspect(caterersRepresentativeId, severity, schoolId)
  activate RD
  RD -> R : existsRecentSimilar(schoolId, repId, since = date - 90 days)
  activate R
  R -> DB : SELECT 1 FROM violation_records\n WHERE caterers_representative_id=? AND date > ?
  activate DB
  DB --> R : found?
  deactivate DB
  R --> RD : boolean
  deactivate R
  RD --> S : repeatSuspect (best-effort suggestion)
  deactivate RD

  S -> VIO : new ViolationRecord(code, type='Quy trinh',\n resolution='PENDING', status='OPEN')
  activate VIO
  VIO --> S : violation(OPEN, PENDING)
  deactivate VIO
  S -> R : save(violation)
  activate R
  R -> DB : INSERT violation_records\n(code=VIO-2026-010, type='Quy trinh', resolution='PENDING', status='OPEN')
  activate DB
  DB --> R : 1 row inserted
  deactivate DB
  R --> S : ViolationRecord(VIO-2026-010)
  deactivate R
  S -> DB : INSERT violation_files (kind=EVIDENCE, ...)
  activate DB
  DB --> S : n rows inserted
  deactivate DB
  S --> C : ViolationResponse{ id:"VIO-2026-010", resolution:"PENDING",\n repeatSuspect:true, warnings:[{...}] }
  C --> UI : 201 Created (+ warning if repeatSuspect)
  UI --> U : "Violation recorded" (+ repeat-suspect warning banner)
end
deactivate S
deactivate C
deactivate UI
@enduml
```

**Description.** Backs UC-179 (a single, unified dialog covering what used to be two separate use cases — UC-178 is merged in). The repeat-suspect check is a pure read against 90 days of history keyed on `caterersRepresentativeId` (unit), not on any free-text staff name; it never prevents the save, it only rides along in the `warnings` array of a normal `201` response, so the caller can act on it or ignore it.

### 6.3 Sequence Diagram — Violation Resolution & Approval Workflow

```plantuml
@startuml MF11_F5_Resolution
title Sequence Diagram: Violation Resolution & Approval Workflow

skinparam linetype ortho
skinparam backgroundColor #FFFFFF

skinparam stereotype {
    CBackgroundColor #E8F5E9
    CBorderColor #2E7D32
    EBackgroundColor #E8F5E9
    EBorderColor #2E7D32
    ABackgroundColor #E8F5E9
    ABorderColor #2E7D32
    IBackgroundColor #E8F5E9
    IBorderColor #2E7D32
}

skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontName Arial
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontName Arial
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontName Arial
skinparam EntityFontSize 13

skinparam CollectionsBackgroundColor #F4FBF4
skinparam CollectionsBorderColor #2E7D32
skinparam CollectionsFontColor #212121
skinparam CollectionsFontName Arial
skinparam CollectionsFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontName Arial
skinparam DatabaseFontSize 13

skinparam sequence {
    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontName Arial
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontName Arial
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #1B5E20
    GroupHeaderBackgroundColor #C8E6C9
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Medical Staff"                          as M
actor       "School Admin"                           as A
boundary    ":ViolationRegistryScreen"               as UI      <<Boundary>>
control     ":ViolationResolutionServiceImpl"        as S       <<Service>>
collections ":ViolationRecordRepository"             as R       <<Repository>>
entity      ":ViolationRecord"                       as VIO     <<Entity>>
database    ":PostgreSQL"                            as DB
queue       ":DomainEventBus"                        as MQ      <<EventBus>>

M -> UI : enter actionTaken, unitSanctionLevel:"CONTRACT_WARNING" -> "Submit resolution"
activate UI
UI -> S : POST /violations/VIO-2026-010/resolution\n{actionTaken, unitSanctionLevel:"CONTRACT_WARNING"}
activate S
S -> R : findSignedRecordFiles(violationId)
activate R
R -> DB : SELECT signed_record_files WHERE violation_id=? AND kind='SIGNED_RECORD'
activate DB
DB --> R : rows
deactivate DB
R --> S : List<ViolationFile>
deactivate R
alt no signed record uploaded yet
  S --> UI : 409 IEM-095 "Upload the signed 3-party record before closing/submitting"
  UI --> M : conflict toast
else has signed record
  S -> VIO : setResolutionDetails(actionTaken, unitSanctionLevel, handledById)
  activate VIO
  VIO --> S : ok
  deactivate VIO
  S -> R : save(violation)
  activate R
  R -> DB : UPDATE violation_records SET action_taken, unit_sanction_level, handled_by_id
  activate DB
  DB --> R : 1 row updated
  deactivate DB
  R --> S : ViolationRecord
  deactivate R
  alt severity = LOW
    S -> VIO : approveAndClose()
    activate VIO
    VIO --> S : resolution=APPROVED, status=CLOSED
    deactivate VIO
    S -> R : save(violation)
    activate R
    R -> DB : UPDATE resolution='APPROVED', status='CLOSED' (Medical Staff self-closes)
    activate DB
    DB --> R : 1 row updated
    deactivate DB
    R --> S : ViolationRecord(CLOSED)
    deactivate R
    S --> UI : 200
    UI --> M : "Violation closed"
  else severity in {MEDIUM, HIGH}
    S -> VIO : submitForApproval()
    activate VIO
    VIO --> S : resolution=PENDING_APPROVAL
    deactivate VIO
    S -> R : save(violation)
    activate R
    R -> DB : UPDATE resolution='PENDING_APPROVAL'
    activate DB
    DB --> R : 1 row updated
    deactivate DB
    R --> S : ViolationRecord(PENDING_APPROVAL)
    deactivate R
    S -> MQ : publish(ViolationSubmittedForApproval) -> School Admin
    activate MQ
    MQ --> S : accepted
    deactivate MQ
    S --> UI : 200
    UI --> M : "Sent to School Admin for approval"
  end
end
deactivate S
deactivate UI

A -> UI : open VIO-2026-010 -> "Approve"
activate UI
UI -> S : POST /violations/VIO-2026-010/resolution/approve
activate S
S -> S : guard role=SCHOOL_ADMIN, resolution=PENDING_APPROVAL
S -> VIO : approve()
activate VIO
VIO --> S : resolution=APPROVED, status=CLOSED
deactivate VIO
S -> R : save(violation)
activate R
R -> DB : UPDATE resolution='APPROVED', status='CLOSED'
activate DB
DB --> R : 1 row updated
deactivate DB
R --> S : ViolationRecord(CLOSED)
deactivate R
S --> UI : 200
deactivate S
UI --> A : "Violation approved and closed"
deactivate UI
@enduml
```

**Description.** Backs UC-180 (submit) / UC-182 (approve/reject). BR-211's gate is checked **before** anything else: a violation resolution can be saved as a draft without a signed record, but it can never leave `PENDING` — neither by direct self-closure (`LOW`) nor by being sent for approval (`MEDIUM`/`HIGH`) — without at least one `SIGNED_RECORD` file already uploaded (`IEM-095`, `MSG-095`). Once past that gate, BR-186 branches purely on `severity`: `LOW` lets Medical Staff close the loop directly, while `MEDIUM`/`HIGH` always requires a second, independent School Administration decision — the same separation-of-duty principle used for CAPA approval in Feature 3. The alternate flow not drawn above (`AF`, UC-182) is `reject`: it sets `resolution='REJECTED'` with a mandatory reason and leaves `status` at `OPEN` so Medical Staff can resubmit.

### 6.4 State Chart — Violation & Resolution Lifecycle

Backs UC-179/180/181/182. `ViolationStatus` has only two values, so the interesting lifecycle is the `resolution` sub-state a violation carries while it is still `OPEN` — modeled here as nested states, with `CLOSED` reached only once `resolution` reaches `APPROVED` (BR-186), whether directly (`LOW`, self-close) or through School Admin approval (`MEDIUM`/`HIGH`, BR-211 gate first).

```plantuml
@startuml MF11_F5_StateChart_Violation
title State Diagram: Violation & Resolution Lifecycle

skinparam nodesep 90
skinparam ranksep 110
skinparam linetype ortho
skinparam backgroundColor #FFFFFF

skinparam state {
    BackgroundColor #F4FBF4
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 13
}

skinparam arrow {
    Color #2E7D32
    FontColor #1B5E20
    FontName Arial
    FontSize 12
}

skinparam note {
    BackgroundColor #E8F5E9
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 12
}

skinparam startColor #2E7D32
skinparam endColor #2E7D32

state OPEN {
  state PENDING
  state PENDING_APPROVAL
  state REJECTED

  [*] --> PENDING : record()
 PENDING --> PENDING_APPROVAL : submit() - severity MEDIUM/HIGH,\nsigned record present
  PENDING_APPROVAL --> REJECTED : reject(reason) - School Admin
  REJECTED --> PENDING : Medical Staff resubmits
}
state CLOSED

OPEN --> CLOSED : resolution = APPROVED\n(PENDING -> CLOSED direct for LOW;\nPENDING_APPROVAL -> CLOSED via approve())
CLOSED --> [*]
@enduml
```

---

## 7. Feature 6 — Weekly Override-Abuse Report (Derived, Read-Only)

Surfaces manual-override abuse (e.g. bypassing a FEFO warning during stock withdrawal) as a weekly, per-caterer-unit rollup — computed on read, never stored as its own table. Backs UC-183.

### 7.1 Class Diagram

```plantuml
@startuml MF11_F6_ClassDiagram
title Class Diagram: Weekly Override-Abuse Report

skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 55
skinparam ranksep 85
skinparam minClassWidth 130
skinparam wrapWidth 260
skinparam backgroundColor #FFFFFF

skinparam class {
    BackgroundColor #F4FBF4
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 13

    HeaderBackgroundColor #4CAF50
    HeaderFontColor #FFFFFF

    AttributeFontColor #212121
    AttributeFontName Arial
    AttributeFontSize 12
}

skinparam interface {
    BackgroundColor #f7ed93ff
    BorderColor #000000ff
    FontColor #000000ff
    FontName Arial
}

skinparam arrow {
    Color #162c00ff
    FontColor #162c00ff
    FontName Arial
    FontSize 11
}

skinparam stereotype {
    CBackgroundColor #f6eedcff
    CBorderColor #000000ff
    EBackgroundColor #f6eedcff
    EBorderColor #000000ff
    ABackgroundColor #f6eedcff
    ABorderColor #000000ff
    IBackgroundColor #f6eedcff
    IBorderColor #000000ff
}

hide empty members

package "mf11.projection" {
  class OverrideAbuseRow <<Projection>> {
    week
    caterersRepresentativeLabel
    overrideCount
    severity
    violationCode
  }
}

package "mf11.dto" {
  class OverrideAbuseResponse <<DTO>> {
    items
    total
    page
    pageSize
  }
  OverrideAbuseResponse ..> OverrideAbuseRow : mapped from
}

package "mf11.web" {
  class OverrideAbuseController <<Controller>> {
    + weekly()
    + export()
  }
}

package "mf11.service" {
  interface OverrideAbuseReportService <<Service>> {
    + weekly()
    + export()
  }
  class OverrideAbuseWeeklyJob <<Scheduled>> {
    + run()
  }
  interface ViolationService <<Service>> {
    + record()
  }
}

package "mf11.repository" {
  interface ViolationRecordRepository <<Repository>> {
    + aggregateOverrideAbuse()
  }
}

package "mf11.port" {
  interface InventoryOverridePort <<Port>> {
    + weeklyOverrideSignals()
  }
}

' ---------- web -> service ----------
OverrideAbuseController ..> OverrideAbuseReportService

' ---------- service -> repository / port ----------
OverrideAbuseReportService ..> ViolationRecordRepository : weekly aggregation
OverrideAbuseWeeklyJob ..> InventoryOverridePort : MF06 override signals
OverrideAbuseWeeklyJob ..> ViolationService : creates OVERRIDE_ABUSE violation
ViolationService ..> ViolationRecordRepository

' ---------- service -> dto / projection ----------
OverrideAbuseReportService ..> OverrideAbuseRow
OverrideAbuseReportService ..> OverrideAbuseResponse
@enduml
```

**Description.** ADR-M11-6 is the whole point of this feature: there is **no** `override_abuse` table. `OverrideAbuseReportService.weekly()` is a pure `GROUP BY` aggregation over `violation_records WHERE violation_type = 'OVERRIDE_ABUSE'`, and the only thing that ever *writes* is `OverrideAbuseWeeklyJob` — a `@Scheduled` job (Monday, weekly) that reads MF06's override signals and creates ordinary, unit-level `ViolationRecord` rows through the same `ViolationService` used by Feature 5, so the report and the registry (UC-181) always agree — a single source of truth, correcting an earlier design where a separate static table counted the same thing differently (CF-M11-10).

### 7.2 Sequence Diagram — View Weekly Override-Abuse Report

```plantuml
@startuml MF11_F6_WeeklyReport
title Sequence Diagram: View Weekly Override-Abuse Report

skinparam linetype ortho
skinparam backgroundColor #FFFFFF

skinparam stereotype {
    CBackgroundColor #E8F5E9
    CBorderColor #2E7D32
    EBackgroundColor #E8F5E9
    EBorderColor #2E7D32
    ABackgroundColor #E8F5E9
    ABorderColor #2E7D32
    IBackgroundColor #E8F5E9
    IBorderColor #2E7D32
}

skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontName Arial
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontName Arial
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontName Arial
skinparam EntityFontSize 13

skinparam CollectionsBackgroundColor #F4FBF4
skinparam CollectionsBorderColor #2E7D32
skinparam CollectionsFontColor #212121
skinparam CollectionsFontName Arial
skinparam CollectionsFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontName Arial
skinparam DatabaseFontSize 13

skinparam sequence {
    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontName Arial
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontName Arial
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #1B5E20
    GroupHeaderBackgroundColor #C8E6C9
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "School Admin"                           as A
boundary    ":OverrideAbuseReportScreen"             as UI      <<Boundary>>
control     ":OverrideAbuseController"               as C       <<Controller>>
control     ":OverrideAbuseReportServiceImpl"        as S       <<Service>>
collections ":ViolationRecordRepository"             as R       <<Repository>>
database    ":PostgreSQL"                            as DB

A -> UI : select week "2026-W27" -> view report
activate UI
UI -> C : GET /violations/override-abuse-report?week=2026-W27
activate C
C -> S : weekly(weekIso, schoolId, actor)
activate S
S -> S : parse week: ISO `2026-Wnn` accepted directly;\ndisplay-label strings resolved via the school's week-label table (transitional)
S -> R : aggregateOverrideAbuse(schoolId, weekIso, from, to)
activate R
R -> DB : SELECT caterers_representative_label, count(*), max(severity), max(code)\nFROM violation_records\nWHERE violation_type='OVERRIDE_ABUSE' AND date BETWEEN :from AND :to\nGROUP BY caterers_representative_label
activate DB
DB --> R : rows
deactivate DB
R --> S : List<OverrideAbuseRow>
deactivate R
S --> C : PagedResponse<OverrideAbuseRow>
deactivate S
C --> UI : 200 { items, total, page, pageSize, pageCount }
deactivate C
UI --> A : weekly table rendered
deactivate UI
@enduml
```

**Description.** The `week` parameter carries a documented, transitional dual-parsing rule: the backend accepts the canonical ISO form (`2026-W27`) but also recognizes the free-text label the current mock frontend sends (e.g. ` "Tuần 27 (29/06–05/07)"`), until the frontend is migrated to send ISO directly. Every number in the response is computed at request time from `violation_records`; nothing about this endpoint is cached state that could drift from the registry. Drilling down from a row (UC-183 step 4) is a plain filter into the Feature 5 Violation Registry (`?violationType=OVERRIDE_ABUSE&dateFrom=&dateTo=`), not a join into a second data source.

---

## Appendix — Cross-Feature Notes

- **Three locks, three different shapes.** MF11 uses locking at three distinct granularities and it matters which one applies where: `incident_quarantine_batches` (row-level, one-way, DB `RULE` blocks `DELETE` — ADR-M11-3/BR-217), `incident_events` (table-level, append-only, DB `RULE` blocks `UPDATE`/`DELETE` — ADR-M11-2), and `incidents.report_exported` (aggregate-level, one-way, service-enforced across every child table — ADR-M11-8). Confusing one for another is the most common design mistake this module guards against.
- **Cross-module reads are always by id, through a port or `ReferenceResolver`, never a JPA relation.** Every arrow from an MF11 service to another module in the diagrams above goes through `InventoryBatchPort` (MF06), `ServingDishPort` (MF09), `FoodSamplePort` (MF08), `NotificationPort` (MF10), or `ReferenceResolver` (MF01/MF03/MF04/MF07 lookups) — consistent with ADR-M11-7. Only `schools`, `inspection_sessions` and `partners` get a real foreign key in §1, because those three module boundaries are stable enough to commit to.
- **Read scope is derived, never a stored owner column** (ADR-M11-10). Teacher sees an `Incident` only if it has ≥1 affected student in their homeroom class; Service Caterers sees it only if their unit has an assigned CAPA **or** a quarantined batch traced to their supply — both conditions are evaluated with `EXISTS` at query time, and deliberately kept as an `OR` rather than narrowed to one, because seed data shows a supplier whose own batch was quarantined is not always the same unit assigned the CAPA (`M11-D19`).
- **The same endpoint returns a different field set depending on role** (`D11`, `M11-D15`) — `GET /incidents` and `GET /incidents/{code}` strip `symptoms`, `firstAidNotes`, `clinicalRiskAssessment`, `rootCause`, `officialClinicalNotes` and student names for Service Caterers and Teacher, because that is child health data under PDPD, not because those roles see fewer rows.
- **No `MealSession`-style shortcuts here.** Unlike MF09, MF11's aggregate root (`Incident`) is a long-lived case file that can stay open for weeks; every one of its six lifecycle features (Report/Triage, Investigation, CAPA, Closure/Report, Violation, Override-Abuse) writes to the same `incident_events` append-only log, which is why that table — not the `status` column of any single entity — is the closest thing this module has to a single source of truth for "what actually happened, and when."
