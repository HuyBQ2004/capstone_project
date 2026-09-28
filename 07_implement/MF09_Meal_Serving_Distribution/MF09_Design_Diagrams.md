# MF09 — Meal Serving & Distribution — Design Diagrams

| Field | Value |
|---|---|
| **Module** | MF09 — Meal Serving & Distribution |
| **Derived from** | `TDS-MF09_Backend.md` (v2.0, Approved) — §5 Static Modeling, §6 Dynamic Modeling, §8 Interface Specification, §9 API Specification |
| **Notation** | DBML (database), PlantUML (class · sequence · state machine diagrams), Rational Rose colour scheme |
| **Language** | English (source TDS is Vietnamese; identifiers, comments and business-rule references are kept consistent with the source) |
| **Scope** | 4 owned tables, 5 service interfaces, 13 use cases (UC-016, UC-047, UC-053, UC-055, UC-056, UC-079\*, UC-080, UC-103, UC-132, UC-133, UC-141\*, UC-142, UC-143 — \*UC-079/UC-141 are out of MF09's implementation scope, see TDS §2.3 CF-M09-1/CF-M09-13) |

This document restates the approved backend design as four standard artifacts: (1) a DBML schema for the module's owned tables, (2) per-feature class diagrams that show the entity/DTO/controller/service/repository layering, (3) per-feature sequence diagrams for the interactions that carry real business logic, and (4) state machine diagrams for every object in MF09 that has a status lifecycle. Read-only list/detail endpoints without branching business logic are covered by the class diagrams alone and are not repeated as sequence diagrams.

### Notation legend

Sequence diagrams use **UML robustness notation**: lifelines are named `:ClassName` (object notation) and carry both a stereotype and the matching PlantUML shape, along the fixed chain `Actor → «Boundary» screen → «Controller» → «Service» → «Repository» → database`, with a dashed return message for every call.

| Shape | Stereotype | Stands for |
|---|---|---|
| `actor` | — | The human role from the use case (the only lifeline without a `:` prefix). |
| `boundary` | `<<Boundary>>` | A screen of the Next.js/Flutter client. |
| `boundary` | `<<Port>>` / `<<Facade>>` | Read-only doors into other modules — `MenuQueryPort` (MF05), `ReferenceResolver` (MF02/03/04/07). |
| `control` | `<<Controller>>` / `<<Service>>` / `<<Handler>>` | REST entry points and transaction-scoped business logic. |
| `collections` | `<<Repository>>` | A Spring Data repository (a collection of entities). |
| `entity` | `<<Entity>>` / `<<Projection>>` | An MF09 domain object mutated during the flow. |
| `database` | — | PostgreSQL — reached only by repositories, never by a service. |
| `queue` | `<<EventBus>>` | Asynchronous domain events consumed by MF10/MF11/MF13 and the audit trail. |

State charts label every transition `trigger [guard] / effect`, so each guard maps one-to-one onto an `alt` branch and an `SRV-xxx` code in the sequence diagrams.

---

## 1. Database Architecture (DBML)

> Only the 4 tables below are **owned** by MF09 (Flyway `V16__mf09_meal_serving.sql`). Tables from other modules (`schools`, `classrooms`, `students`, `menus`, `dishes`, `partners`, `user_accounts`) are declared as **stub tables** (id only) purely to render the foreign-key arrows — MF09 never joins into them at the ORM level (no `@ManyToOne` across modules); all cross-module reads go through `ReferenceResolver`/`MenuQueryPort` by id (see class diagrams in §2–§6).

```dbml
Project sfms_mf09 {
  database_type: 'PostgreSQL'
  Note: '''
    MF09 — Meal Serving & Distribution.
    Migration: V16__mf09_meal_serving.sql (schema) + V17__seed_mf09.sql (seed).
    All 4 owned tables carry a business `code` (UNIQUE per school) that is
    exposed as JSON field "id"; the UUID primary key never leaves the API.
  '''
}

// ============================================================
// Owned by MF09
// ============================================================

Table student_distributions {
  id                  uuid        [pk, default: `gen_random_uuid()`, note: 'internal PK, never exposed in JSON']
  code                varchar(32) [not null, note: 'business code, exposed as JSON "id", e.g. DSTU-0706A-001']
  school_id           uuid        [not null, ref: > schools.id]
  date                date        [not null]
  meal_type           meal_type   [not null, note: 'part of the "serving session" key together with date']
  student_id          uuid        [not null, ref: > students.id, note: 'read by id only, no ORM relation (AP-M09-07)']
  student_name        varchar(120) [not null, note: 'snapshot taken when the session was generated']
  class_name          varchar(20) [not null]
  classroom_id        uuid        [not null, ref: > classrooms.id, note: 'backs the BR-120 homeroom-teacher guard']
  menu_id             uuid        [ref: > menus.id, note: 'source menu for (date, grade, meal) — constraint C3']
  caterer_partner_id  uuid        [ref: > partners.id, note: 'frozen snapshot at session creation — ADR-M09-8']
  caterer_name        varchar(180) [note: 'frozen display name, never re-looked-up']
  status              distribution_status [not null, default: 'PENDING']
  served_at           timestamptz [note: 'NOT NULL iff status = SERVED']
  served_by_user_id   uuid        [note: 'Teacher who confirmed — BR-120']
  planned_serve_time  time        [note: 'nullable, reserved for BR-135 (not implemented in v1) — CF-M09-3']
  created_at          timestamptz [not null, default: `now()`]
  updated_at          timestamptz [not null, default: `now()`]
  created_by          varchar(180)
  updated_by          varchar(180)

  indexes {
    (school_id, code)              [unique, name: 'uq_dist_school_code']
    (student_id, date, meal_type)  [unique, name: 'uq_dist_student_date_meal']
    (date, class_name, meal_type)  [name: 'ix_dist_date_class_meal']
    (student_id, date)             [name: 'ix_dist_student_date']
    (school_id, date)              [name: 'ix_dist_school_date']
  }

  Note: '''
    BR-130 (no duplicate serving) is enforced at the DB level by
    uq_dist_student_date_meal, not only in the service layer.
    CHECK ck_dist_served_at: status = SERVED requires served_at AND served_by_user_id.
    CHECK ck_dist_served_same_day: DATE(served_at) must equal date.
    Derived/computed display fields (assignedMeal, specialMeal, allergyWarning,
    isSpecial, dietTag) have NO physical column — ADR-M09-2.
  '''
}

Table serving_dishes {
  id                          uuid        [pk, default: `gen_random_uuid()`]
  code                        varchar(32) [not null, note: 'e.g. SDSH-0706A-001-1']
  school_id                   uuid        [not null, ref: > schools.id]
  distribution_id             uuid        [not null, ref: > student_distributions.id]
  dish_id                     uuid        [not null, ref: > dishes.id, note: 'MF11 incident-traceability key']
  dish_name                   varchar(180) [not null, note: 'frozen snapshot — the dish may later be renamed/removed']
  is_substitute               boolean     [not null, default: false, note: 'BR-065 special-diet substitution']
  substituted_for_dish_name   varchar(180)
  reason                      varchar(200) [note: 'e.g. "Shrimp allergy"']
  created_at                  timestamptz [not null, default: `now()`]
  updated_at                  timestamptz [not null, default: `now()`]
  created_by                  varchar(180)
  updated_by                  varchar(180)

  indexes {
    (school_id, code)   [unique, name: 'uq_sdish_school_code']
    (distribution_id)   [name: 'ix_sdish_dist']
    (dish_id)           [name: 'ix_sdish_dish']
  }

  Note: '''
    ADR-M09-1 — APPEND-ONLY. UPDATE/DELETE are blocked at the database level
    with `CREATE RULE ... DO INSTEAD NOTHING`, not by application discipline alone.
    Each row is a frozen "as-served" event, not a derived value: it answers
    "which dish did this student actually eat?" for MF11 incident investigation.
    CHECK ck_sdish_substitute: is_substitute = true requires
    substituted_for_dish_name AND reason to be present.
  '''
}

Table meal_consumption_records {
  id                   uuid        [pk, default: `gen_random_uuid()`]
  code                 varchar(32) [not null, note: 'e.g. MR-TD-3A-004']
  school_id            uuid        [not null, ref: > schools.id]
  date                 date        [not null]
  meal_type            meal_type   [not null]
  student_id           uuid        [not null]
  student_name         varchar(120) [not null]
  class_name           varchar(20) [not null]
  dishes_label         varchar(500) [note: 'display label, JSON field "dishes"']
  special_diet         varchar(60)
  special_meal_served  varchar(255) [note: 'BR-125']
  consumption          consumption_status [not null, default: 'PENDING']
  recorded_at          timestamptz
  recorded_by_user_id  uuid
  created_at           timestamptz [not null, default: `now()`]
  updated_at           timestamptz [not null, default: `now()`]
  created_by           varchar(180)
  updated_by           varchar(180)

  indexes {
    (school_id, code)             [unique, name: 'uq_mcr_school_code']
    (student_id, date, meal_type) [unique, name: 'uq_mcr_student_date_meal']
    (date, class_name)            [name: 'ix_mcr_date_class']
    (student_id, date)            [name: 'ix_mcr_student_date']
  }

  Note: '''
    BR-121 consumption tracking (UC-080/UC-056). Editable only on the serving
    day; after that it is locked (409 SRV-025) because values already feed the
    BR-133 daily parent summary job.
  '''
}

Table distribution_error_reports {
  id                    uuid        [pk, default: `gen_random_uuid()`]
  code                  varchar(32) [not null, note: 'e.g. DERR-2026-0007']
  school_id             uuid        [not null, ref: > schools.id]
  distribution_id       uuid        [not null, ref: > student_distributions.id]
  reported_by_user_id   uuid        [not null, ref: > user_accounts.id]
  reason                varchar(500) [not null]
  resolved              boolean     [not null, default: false]
  reported_at           timestamptz [not null, default: `now()`, note: 'business timestamp, distinct from created_at']
  created_at            timestamptz [not null, default: `now()`]
  updated_at            timestamptz [not null, default: `now()`]
  created_by            varchar(180)
  updated_by            varchar(180)

  indexes {
    (school_id, code)   [unique, name: 'uq_der_school_code']
    (distribution_id)   [name: 'ix_der_dist']
  }

  Note: 'UC-016 AF1 — wrong-portion report. An unresolved report blocks class-level distribution completion (BR-065, error code SRV-023).'
}

// ============================================================
// Enums
// ============================================================

Enum meal_type {
  BREAKFAST
  LUNCH
  SNACK
}

Enum distribution_status {
  PENDING
  SERVED
}

Enum consumption_status {
  PENDING
  FULLY
  PARTIALLY
  NOT_CONSUMED
}

// ============================================================
// External tables — owned by other modules, referenced by id only
// ============================================================

Table schools {
  id uuid [pk]
  Note: 'Owned by MF01. Used for tenant/school scoping only.'
}

Table classrooms {
  id uuid [pk]
  homeroom_teacher_id uuid
  Note: 'Owned by MF02. homeroom_teacher_id backs the BR-120 write guard.'
}

Table students {
  id uuid [pk]
  Note: 'Owned by MF03. Read strictly by id — no cross-module JPA relation is created (AP-M09-07).'
}

Table menus {
  id uuid [pk]
  Note: 'Owned by MF05. Source of the (date, grade, meal) block resolved via MenuQueryPort — constraint C3.'
}

Table dishes {
  id uuid [pk]
  Note: 'Owned by MF05. dish_id on serving_dishes is the MF11 incident-traceability key.'
}

Table partners {
  id uuid [pk]
  Note: 'Owned by MF04. Source of the caterer snapshot frozen on student_distributions — ADR-M09-8.'
}

Table user_accounts {
  id uuid [pk]
  Note: 'Owned by MF01. Referenced by served_by_user_id / recorded_by_user_id / reported_by_user_id.'
}
```

**Description.** MF09 owns four tables of two kinds. `student_distributions` and `meal_consumption_records` are **mutable state** moving through the status lifecycles of §2.8 and §3.3, guarded by unique constraints that make BR-130 impossible to violate even if the service layer has a bug; `serving_dishes` is an **append-only event log** (Postgres `RULE` rejects `UPDATE`/`DELETE`) because it is the only place that can answer "did this student eat the recalled dish?" during an MF11 investigation. Anything recomputable — `assignedMeal`, `allergyWarning`, `isSpecial`, daily summaries — deliberately has **no column** (ADR-M09-2), and every foreign key into another module is a plain UUID resolved through `ReferenceResolver`/`MenuQueryPort` rather than an ORM association (AP-M09-07).

---

## 2. Feature 1 — Distribution Management

Owns the core teacher/caterer workflow: list & inspect today's serving sessions, confirm a portion has been handed over (single or batch), undo same-day mistakes, report a wrong portion, and read a runtime roll-up per class. Backs UC-016, UC-047, UC-132.

### 2.1 Class Diagram

```plantuml
@startuml MF09_F1_ClassDiagram
title Class Diagram: MF09 — Feature 1 — Distribution Management

' ==========================================
' RATIONAL ROSE LAYOUT AND COLOUR CONFIGURATION
' ==========================================
skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF

' Formatting for Class / Interface / Abstract Class
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

' Relationship and arrow formatting
skinparam arrow {
    Color #162c00ff
    FontColor #162c00ff
    FontName Arial
    FontSize 11
}

' Stereotype icon formatting (C, I, A, E)
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

package "mealserving.web" {
  class DistributionController <<Controller>> {
    + list()
    + serve()
    + confirm()
    + revoke()
    + reportError()
    + summary()
  }
}

package "mealserving.service" {
  interface DistributionService {
    + list()
    + confirmServed()
    + confirmBatch()
    + revokeServed()
    + reportError()
    + summary()
  }

  class DistributionServiceImpl <<Service>>
  class DistributionPlanHandler <<Handler>> {
    + onMenuPublished()
  }

  DistributionServiceImpl ..|> DistributionService
}

package "mealserving.repository" {
  interface StudentDistributionRepository <<Repository>> {
    + findByIdForUpdate()
    + findByDateAndClassAndMeal()
    + summarize()
  }
  interface ServingDishRepository <<Repository>> {
    + findByDistributionId()
  }
  interface DistributionErrorReportRepository <<Repository>> {
    + findByDistributionIdAndResolvedFalse()
  }
}

package "mealserving.domain" {
  class StudentDistribution <<Entity>> {
    code
    date
    mealType
    status
    studentId
    classroomId
    menuId
    catererName
    servedAt
    + markServed()
    + revoke()
  }

  class ServingDish <<Entity, Immutable>> {
    code
    dishId
    dishName
    isSubstitute
    reason
  }

  class DistributionErrorReport <<Entity>> {
    code
    distributionId
    reason
    resolved
  }

  enum DistributionStatus {
    PENDING
    SERVED
  }
}

package "mealserving.dto" {
  class DistributionResponse <<DTO>> {
    id
    status
    servedDishes
  }
  class DistributionDetailResponse <<DTO>> {
    assignedMeal
    allergyWarning
  }
  class BatchConfirmRequest <<DTO>> {
    ids
    atomic
  }
  class BatchConfirmResult <<DTO>> {
    confirmed
    failed
    results
  }
  class DistributionErrorResponse <<DTO>> {
    distributionId
    reason
    resolved
  }
  class DistributionSummaryResponse <<DTO>> {
    className
    planned
    distributed
  }

  DistributionDetailResponse --|> DistributionResponse
}

package "mealserving.common.reference" {
  class ReferenceResolver <<Facade>> {
    + inspectionGate()
    + isHomeroomTeacher()
    + verifiedAllergensOfStudent()
    + currentCatererOf()
  }
  interface MenuQueryPort <<Port>> {
    + publishedBlock()
  }
}

DistributionController ..> DistributionService
DistributionServiceImpl ..> StudentDistributionRepository
DistributionServiceImpl ..> ServingDishRepository
DistributionServiceImpl ..> DistributionErrorReportRepository
DistributionServiceImpl ..> ReferenceResolver
DistributionServiceImpl ..> MenuQueryPort
DistributionPlanHandler ..> StudentDistributionRepository
DistributionPlanHandler ..> ReferenceResolver

StudentDistribution "1" *-- "0..*" ServingDish : frozen (ADR-M09-1)
StudentDistribution "1" o-- "0..*" DistributionErrorReport
StudentDistributionRepository ..> StudentDistribution
ServingDishRepository ..> ServingDish
DistributionErrorReportRepository ..> DistributionErrorReport
@enduml
```

**Description.** `StudentDistribution` is the aggregate root of the serving session and `ServingDish` its append-only child collection; rows are never created by a REST call but by `DistributionPlanHandler` on MF05's `MenuPublished` event (§2.2). `DistributionController` never touches a repository: every write goes through `DistributionServiceImpl`, which combines MF09's own repositories with the read-only `ReferenceResolver`/`MenuQueryPort` for facts owned by MF02–MF05/MF07. The repository operations listed here are exactly the ones called by the sequence diagrams §2.2–§2.7, and each guard shown there is one of the transitions in the state chart §2.8.

### 2.2 Sequence Diagram — Generate Serving Sessions (`MenuPublished` → `PENDING`)

```plantuml
@startuml MF09_F1_GenerateSessions
title Sequence Diagram: Generate Serving Sessions on MenuPublished

' Enable orthogonal lines
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME CONFIGURATION
' ==========================================
skinparam backgroundColor #FFFFFF

' Common stereotype formatting
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

' ==========================================
' Note
' ==========================================
skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

' ==========================================
' Lifelines by UML robustness stereotype
' (boundary / control / entity / database)
' ==========================================
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
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Nutritionist\n(MF05 — upstream)"      as Nutri
boundary    ":MenuApprovalScreen\n(MF05)"           as MenuUI      <<Boundary>>
control     ":MenuService\n(MF05)"                  as MenuSvc     <<Service>>
queue       ":DomainEventBus"                       as Bus         <<EventBus>>
control     ":DistributionPlanHandler"              as Handler     <<Handler>>
boundary    ":ReferenceResolver"                    as Ref         <<Facade>>
entity      ":StudentDistribution"                  as Dist        <<Entity>>
entity      ":MealConsumptionRecord"                as Rec         <<Entity>>
collections ":StudentDistributionRepository"        as DistRepo    <<Repository>>
collections ":MealConsumptionRepository"            as RecRepo     <<Repository>>
database    ":PostgreSQL"                           as DB

Nutri -> MenuUI : publish the approved menu\nfor (date, grade, mealType)
activate MenuUI
MenuUI -> MenuSvc : publishMenu(menuId)
activate MenuSvc
MenuSvc -> Bus : publish(MenuPublished{menuId, date, grade, mealType, schoolId})
activate Bus
Bus --> MenuSvc : accepted
MenuSvc --> MenuUI : 200 OK
deactivate MenuSvc
MenuUI --> Nutri : "Menu published"
deactivate MenuUI

Bus -> Handler : onMenuPublished(event)
activate Handler
deactivate Bus

Handler -> Ref : currentCatererOf(schoolId, date)
activate Ref
Ref --> Handler : Optional<CatererRef{partnerId, code, name}>
deactivate Ref
note right of Handler
  ADR-M09-8 — called EXACTLY ONCE per event.
  The caterer name is then frozen on every row so a
  later change of contract never rewrites history.
end note

Handler -> Ref : roster of (grade, date) — students of the grade
activate Ref
Ref --> Handler : List<StudentRef{studentId, name, className, classroomId}>
deactivate Ref

loop for each student in roster
  Handler -> Dist : new StudentDistribution(code, schoolId, date, mealType,\nstudentRef, menuId, catererSnapshot, status = PENDING)
  activate Dist
  Dist --> Handler : distribution(PENDING)
  deactivate Dist
  Handler -> Rec : new MealConsumptionRecord(code, ..., consumption = PENDING)
  activate Rec
  Rec --> Handler : record(PENDING)
  deactivate Rec
end

Handler -> DistRepo : saveAll(distributions)
activate DistRepo
DistRepo -> DB : INSERT INTO student_distributions
activate DB
DB --> DistRepo : n rows\n(uq_dist_student_date_meal guarantees idempotency)
deactivate DB
DistRepo --> Handler : List<StudentDistribution>
deactivate DistRepo

Handler -> RecRepo : saveAll(consumptionRecords)
activate RecRepo
RecRepo -> DB : INSERT INTO meal_consumption_records
activate DB
DB --> RecRepo : n rows
deactivate DB
RecRepo --> Handler : List<MealConsumptionRecord>
deactivate RecRepo
deactivate Handler
@enduml
```

### 2.3 Sequence Diagram — Confirm Meal Distribution (Happy Path)

```plantuml
@startuml MF09_F1_ConfirmDistribution_Happy
title Sequence Diagram: Confirm Meal Distribution (Happy Path) — UC-016

' Enable orthogonal lines
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME CONFIGURATION
' ==========================================
skinparam backgroundColor #FFFFFF

' Common stereotype formatting
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

' ==========================================
' Note
' ==========================================
skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

' ==========================================
' Lifelines by UML robustness stereotype
' (boundary / control / entity / database)
' ==========================================
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
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Teacher\n(Homeroom — GVCN)"      as Teacher
boundary    ":MealDistributionScreen"         as UI          <<Boundary>>
control     ":DistributionController"         as Ctrl        <<Controller>>
control     ":DistributionServiceImpl"        as Svc         <<Service>>
collections ":StudentDistributionRepository"  as DistRepo    <<Repository>>
collections ":ServingDishRepository"          as DishRepo    <<Repository>>
entity      ":StudentDistribution"            as Dist        <<Entity>>
entity      ":ServingDish"                    as SDish       <<Entity>>
boundary    ":ReferenceResolver"              as Ref         <<Facade>>
boundary    ":MenuQueryPort"                  as Menu        <<Port>>
database    ":PostgreSQL"                     as DB
queue       ":DomainEventBus"                 as Bus         <<EventBus>>

Teacher -> UI : tap "Confirm served"\non the student's row
activate UI
UI -> Ctrl : POST /api/v1/student-distributions/{code}/serve\n(JWT: role = TEACHER, userId, schoolId)
activate Ctrl
Ctrl -> Ctrl : resolve {code} → id, build AuthUser from JWT
Ctrl -> Svc : confirmServed(id, actor)
activate Svc

Svc -> DistRepo : findByIdForUpdate(id)
activate DistRepo
DistRepo -> DB : SELECT * FROM student_distributions\nWHERE id = ? FOR UPDATE
activate DB
DB --> DistRepo : row (status = PENDING)
deactivate DB
DistRepo --> Svc : Optional<StudentDistribution>
deactivate DistRepo

Svc -> Dist : getClassroomId(), getStatus(), getDate(), getMealType()
activate Dist
Dist --> Svc : classroomId, PENDING, date, mealType
deactivate Dist

group Guard 1 — homeroom scope
  Svc -> Ref : isHomeroomTeacher(classroomId, actor.userId)
  activate Ref
  Ref --> Svc : true
  deactivate Ref
end

group Guard 2 — no duplicate serving
  Svc -> Svc : assert status != SERVED
end

group Guard 3 — inspection gate
  Svc -> Ref : inspectionGate(date, mealType, schoolId)
  activate Ref
  Ref --> Svc : Optional<InspectionGate{blocked = false, failedSteps = [], sessionId, sessionCode}>
  deactivate Ref
end

group Guard 4 — published menu block
  Svc -> Menu : publishedBlock(date, gradeOf(className), mealType, schoolId)
  activate Menu
  Menu --> Svc : Optional<MenuBlockView{menuId, dishes[]}>
  deactivate Menu
end

Svc -> Ref : verifiedAllergensOfStudent(studentId)
activate Ref
Ref --> Svc : List<String> allergens
deactivate Ref
Svc -> Ref : specialDietOfStudent(studentId)
activate Ref
Ref --> Svc : Optional<String> dietTag
deactivate Ref

Svc -> Svc : personalize(block, allergens, dietTag)
Svc -> SDish : new ServingDish(dishId, dishName,\nisSubstitute, substitutedForDishName, reason)
activate SDish
SDish --> Svc : List<ServingDish> servingDishes
deactivate SDish

Svc -> Dist : markServed(now(), actor.userId)
activate Dist
Dist --> Svc : status = SERVED, servedAt = now()
deactivate Dist

Svc -> DistRepo : save(distribution)
activate DistRepo
DistRepo -> DB : UPDATE student_distributions\nSET status, served_at, served_by_user_id
activate DB
DB --> DistRepo : 1 row updated
deactivate DB
DistRepo --> Svc : StudentDistribution(SERVED)
deactivate DistRepo

Svc -> DishRepo : saveAll(servingDishes)
activate DishRepo
DishRepo -> DB : INSERT INTO serving_dishes\n(frozen event write, append-only)
activate DB
DB --> DishRepo : n rows inserted
deactivate DB
DishRepo --> Svc : List<ServingDish>
deactivate DishRepo
note over DistRepo, DB
  Both writes run inside the SAME @Transactional boundary:
  either the status flip and the frozen dishes are both
  committed, or neither is.
end note

Svc -> Bus : publish(MealServedToStudent{distributionId, studentId, servedDishes})
activate Bus
Bus --> Svc : accepted (async — MF11 traceability, MF13 statistics)
deactivate Bus

Svc -> Svc : toResponse(distribution)
Svc --> Ctrl : DistributionResponse{status = SERVED, servedDishes[]}
deactivate Svc
Ctrl --> UI : 200 OK\nApiResponse<DistributionResponse> — MSG-045
deactivate Ctrl
UI --> Teacher : row turns "Served" + toast\n"Meal distribution confirmed"
deactivate UI
@enduml
```

### 2.4 Sequence Diagram — Confirm Meal Distribution (Error Paths)

```plantuml
@startuml MF09_F1_ConfirmDistribution_Error
title Sequence Diagram: Confirm Meal Distribution (Error Paths) — UC-016

' Enable orthogonal lines
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME CONFIGURATION
' ==========================================
skinparam backgroundColor #FFFFFF

' Common stereotype formatting
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

' ==========================================
' Note
' ==========================================
skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

' ==========================================
' Lifelines by UML robustness stereotype
' (boundary / control / entity / database)
' ==========================================
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
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

actor       "Teacher"                       as Teacher
boundary    ":MealDistributionScreen"        as UI          <<Boundary>>
control     ":DistributionController"        as Ctrl        <<Controller>>
control     ":DistributionServiceImpl"       as Svc         <<Service>>
collections ":StudentDistributionRepository" as DistRepo    <<Repository>>
entity      ":StudentDistribution"           as Dist        <<Entity>>
boundary    ":ReferenceResolver"             as Ref         <<Facade>>
boundary    ":MenuQueryPort"                 as Menu        <<Port>>
database    ":PostgreSQL"                    as DB

== Case 1 — the caller is not the homeroom teacher of the class ==
Teacher -> UI : tap "Confirm served" on a row of another class
activate UI
UI -> Ctrl : POST /api/v1/student-distributions/DSTU-0707A-001/serve
activate Ctrl
Ctrl -> Svc : confirmServed(id, actor)
activate Svc
Svc -> DistRepo : findByIdForUpdate(id)
activate DistRepo
DistRepo -> DB : SELECT ... FOR UPDATE
activate DB
DB --> DistRepo : row (classroomId = 3B)
deactivate DB
DistRepo --> Svc : Optional<StudentDistribution>
deactivate DistRepo
Svc -> Ref : isHomeroomTeacher(classroomId, actor.userId)
activate Ref
Ref --> Svc : false
deactivate Ref
Svc --> Ctrl : throw ForbiddenException(SRV-005)
deactivate Svc
Ctrl --> UI : 403 { code: "SRV-005", message: MSG-005 }
deactivate Ctrl
UI --> Teacher : "You are not allowed to act on this class"
deactivate UI

== Case 2 — confirming the same session twice ==
Teacher -> UI : tap "Confirm served" again
activate UI
UI -> Ctrl : POST /api/v1/student-distributions/DSTU-0706A-002/serve
activate Ctrl
Ctrl -> Svc : confirmServed(id, actor)
activate Svc
Svc -> DistRepo : findByIdForUpdate(id)
activate DistRepo
DistRepo -> DB : SELECT ... FOR UPDATE
activate DB
DB --> DistRepo : row (status = SERVED)
deactivate DB
DistRepo --> Svc : Optional<StudentDistribution>
deactivate DistRepo
Svc -> Dist : isServed()
activate Dist
Dist --> Svc : true
deactivate Dist
Svc --> Ctrl : throw ConflictException(SRV-021)\n+ publish DuplicateDistributionBlocked (audit)
deactivate Svc
Ctrl --> UI : 409 { code: "SRV-021",\nmessage: "Duplicate meal distribution: Student has already\nscanned for this session." } — MSG-021
deactivate Ctrl
UI --> Teacher : conflict toast, the row stays "Served"
deactivate UI

== Case 3 — two devices confirm the same student at the same time ==
Teacher -> UI : request A (tablet) and request B (phone) fired together
activate UI
UI -> Ctrl : POST .../serve  ×2
activate Ctrl
Ctrl -> Svc : confirmServed(id, actor)  ×2
activate Svc
Svc -> DistRepo : findByIdForUpdate(id)  — A wins the row lock, B blocks
activate DistRepo
DistRepo -> DB : SELECT ... FOR UPDATE
activate DB
DB --> DistRepo : A → PENDING (lock held) ; B waits for A to commit
deactivate DB
DistRepo --> Svc : A: Optional<StudentDistribution>
deactivate DistRepo
Svc --> Ctrl : A → DistributionResponse(SERVED)
Ctrl --> UI : A → 200 OK
Svc -> DistRepo : B re-reads after A committed
activate DistRepo
DistRepo -> DB : SELECT ... FOR UPDATE (lock released)
activate DB
DB --> DistRepo : row (status = SERVED)
deactivate DB
DistRepo --> Svc : Optional<StudentDistribution>
deactivate DistRepo
Svc --> Ctrl : B → throw ConflictException(SRV-021)
deactivate Svc
Ctrl --> UI : B → 409 SRV-021
deactivate Ctrl
UI --> Teacher : exactly one confirmation is recorded
deactivate UI
note over DistRepo, DB
  Even if the pessimistic lock were removed, the unique index
  uq_dist_student_date_meal would still reject the second write.
  Two independent mechanisms guard BR-130.
end note

== Case 4 — meal blocked by a failed inspection step ==
Teacher -> UI : tap "Confirm served"
activate UI
UI -> Ctrl : POST .../serve
activate Ctrl
Ctrl -> Svc : confirmServed(id, actor)
activate Svc
Svc -> Ref : inspectionGate(date, mealType, schoolId)
activate Ref
Ref -> DB : native SELECT on inspection_sessions (read-only, by id)
activate DB
DB --> Ref : step2 = FAILED
deactivate DB
Ref --> Svc : Optional<InspectionGate{blocked = true,\nfailedSteps = ["Step 2 - during preparation"]}>
deactivate Ref
Svc --> Ctrl : throw ForbiddenException(SRV-094)
deactivate Svc
Ctrl --> UI : 403 { code: "SRV-094",\nmessage: "Meal cannot be served: inspection step\n[Step 2] did not pass (BR-094)." } — MSG-129
deactivate Ctrl
UI --> Teacher : confirm button disabled + blocking banner
deactivate UI

== Case 5 — no approved/published menu for (date, grade, meal) ==
Teacher -> UI : tap "Confirm served"
activate UI
UI -> Ctrl : POST .../serve
activate Ctrl
Ctrl -> Svc : confirmServed(id, actor)
activate Svc
Svc -> Menu : publishedBlock(date, grade, mealType, schoolId)
activate Menu
Menu --> Svc : Optional.empty()
deactivate Menu
Svc --> Ctrl : throw ValidationException(SRV-022)
deactivate Svc
Ctrl --> UI : 422 { code: "SRV-022",\nmessage: "No approved or published menu found." } — MSG-130
deactivate Ctrl
UI --> Teacher : "No approved menu for this meal yet"
deactivate UI
@enduml
```

### 2.5 Sequence Diagram — Batch Confirm ("Save Distribution List")

```plantuml
@startuml MF09_F1_BatchConfirm
title Sequence Diagram: Batch Confirm Distribution ("Save Distribution List") — UC-016

' Enable orthogonal lines
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME CONFIGURATION
' ==========================================
skinparam backgroundColor #FFFFFF

' Common stereotype formatting
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

' ==========================================
' Note
' ==========================================
skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

' ==========================================
' Lifelines by UML robustness stereotype
' (boundary / control / entity / database)
' ==========================================
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
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Teacher"                             as Teacher
boundary    ":MealDistributionScreen"              as UI       <<Boundary>>
control     ":DistributionController"              as Ctrl     <<Controller>>
control     ":DistributionServiceImpl"             as Svc      <<Service>>
collections ":DistributionErrorReportRepository"   as ErrRepo  <<Repository>>
collections ":StudentDistributionRepository"       as DistRepo <<Repository>>
collections ":ServingDishRepository"               as DishRepo <<Repository>>
database    ":PostgreSQL"                          as DB

Teacher -> UI : tick the served students, press "Save distribution list"
activate UI
UI -> Ctrl : POST /api/v1/student-distributions/confirm\n{ ids: [...], atomic: true }
activate Ctrl
Ctrl -> Svc : confirmBatch(req, actor)
activate Svc
Svc -> Svc : open @Transactional boundary

group Pre-check — unresolved error reports
  loop for each id in req.ids
    Svc -> ErrRepo : findByDistributionIdAndResolvedFalse(id)
    activate ErrRepo
    ErrRepo -> DB : SELECT * FROM distribution_error_reports\nWHERE distribution_id = ? AND resolved = false
    activate DB
    DB --> ErrRepo : rows
    deactivate DB
    ErrRepo --> Svc : List<DistributionErrorReport>
    deactivate ErrRepo
  end
  alt at least one unresolved report exists
    Svc -> DB : ROLLBACK
    Svc --> Ctrl : throw ConflictException(SRV-023)
    Ctrl --> UI : 409 { code: "SRV-023",\nmessage: "Distribution completion is blocked: an unresolved\ndistribution error was reported." } — MSG-131
    UI --> Teacher : "Cannot complete - an unresolved error report remains"
  end
end

loop for each id in req.ids
  Svc -> Svc : confirmServed(id, actor)\n(reuses every guard of §2.3 / §2.4)
  alt all guards pass
    Svc -> DistRepo : save(status = SERVED, servedAt, servedByUserId)
    activate DistRepo
    DistRepo -> DB : UPDATE student_distributions
    activate DB
    DB --> DistRepo : 1 row
    deactivate DB
    DistRepo --> Svc : StudentDistribution(SERVED)
    deactivate DistRepo
    Svc -> DishRepo : saveAll(servingDishes)
    activate DishRepo
    DishRepo -> DB : INSERT INTO serving_dishes
    activate DB
    DB --> DishRepo : n rows
    deactivate DB
    DishRepo --> Svc : List<ServingDish>
    deactivate DishRepo
    Svc -> Svc : append BatchConfirmResultLine(id, SERVED, servedAt)
  else business-rule violation (SRV-005 / SRV-021 / SRV-094 / SRV-022)
    alt req.atomic == true
      Svc -> Svc : setRollbackOnly()
    else req.atomic == false
      Svc -> Svc : append BatchConfirmResultLine(id, PENDING, errorCode)
    end
  end
end

alt atomic == true and at least one line failed
  Svc -> DB : ROLLBACK — not a single row changed
  activate DB
  DB --> Svc : rolled back
  deactivate DB
  Svc --> Ctrl : BatchConfirmResult{ confirmed: 0, failed: N, results[] }
else
  Svc -> DB : COMMIT
  activate DB
  DB --> Svc : committed
  deactivate DB
  Svc --> Ctrl : BatchConfirmResult{ confirmed, failed, results[] }
end
deactivate Svc
Ctrl --> UI : 200 OK ApiResponse<BatchConfirmResult> — MSG-045
deactivate Ctrl
UI --> Teacher : list refreshed, per-row result badges
deactivate UI
@enduml
```

### 2.6 Sequence Diagram — Revoke Meal Distribution

```plantuml
@startuml MF09_F1_Revoke
title Sequence Diagram: Revoke Meal Distribution (same day only)

' Enable orthogonal lines
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME CONFIGURATION
' ==========================================
skinparam backgroundColor #FFFFFF

' Common stereotype formatting
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

' ==========================================
' Note
' ==========================================
skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

' ==========================================
' Lifelines by UML robustness stereotype
' (boundary / control / entity / database)
' ==========================================
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
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Teacher\n(Homeroom — GVCN)"     as Teacher
boundary    ":MealDistributionScreen"        as UI       <<Boundary>>
control     ":DistributionController"        as Ctrl     <<Controller>>
control     ":DistributionServiceImpl"       as Svc      <<Service>>
collections ":StudentDistributionRepository" as DistRepo <<Repository>>
entity      ":StudentDistribution"           as Dist     <<Entity>>
boundary    ":ReferenceResolver"             as Ref      <<Facade>>
database    ":PostgreSQL"                    as DB
queue       ":DomainEventBus / Audit"        as Bus      <<EventBus>>

Teacher -> UI : tap "Undo" on a row already marked served
activate UI
UI -> Ctrl : POST /api/v1/student-distributions/{code}/revoke
activate Ctrl
Ctrl -> Svc : revokeServed(id, actor)
activate Svc

Svc -> DistRepo : findByIdForUpdate(id)
activate DistRepo
DistRepo -> DB : SELECT ... FOR UPDATE
activate DB
DB --> DistRepo : row (status, servedAt)
deactivate DB
DistRepo --> Svc : Optional<StudentDistribution>
deactivate DistRepo

Svc -> Ref : isHomeroomTeacher(classroomId, actor.userId)
activate Ref
Ref --> Svc : boolean
deactivate Ref

alt not the homeroom teacher of this class
  Svc --> Ctrl : throw ForbiddenException(SRV-005)
  Ctrl --> UI : 403 MSG-005
  UI --> Teacher : "You are not allowed to act on this class"
else homeroom teacher confirmed
  Svc -> Dist : getStatus()
  activate Dist
  Dist --> Svc : status
  deactivate Dist

  alt status != SERVED (already PENDING)
    Svc --> Ctrl : DistributionResponse{ status: PENDING } (unchanged)
    Ctrl --> UI : 200 OK
    UI --> Teacher : row already shows "Not served"
  else status == SERVED
    Svc -> Dist : isSameDay(today)
    activate Dist
    Dist --> Svc : boolean
    deactivate Dist

    alt servedAt is on a previous day
      Svc --> Ctrl : throw ConflictException(SRV-024)
      Ctrl --> UI : 409 { code: "SRV-024",\nmessage: "Cannot revoke a distribution confirmed\non a previous day." } — MSG-132
      UI --> Teacher : "Cannot undo a portion served on a previous day"
    else servedAt is today
      Svc -> Dist : revoke()
      activate Dist
      Dist --> Svc : status = PENDING, servedAt = null, servedByUserId = null
      deactivate Dist
      Svc -> DistRepo : save(distribution)
      activate DistRepo
      DistRepo -> DB : UPDATE student_distributions\n(serving_dishes are NOT touched)
      activate DB
      DB --> DistRepo : 1 row updated
      deactivate DB
      DistRepo --> Svc : StudentDistribution(PENDING)
      deactivate DistRepo
      Svc -> Bus : record(actor, action = REVOKE, distributionId, previousServedAt)
      activate Bus
      Bus --> Svc : audit entry written
      deactivate Bus
      Svc --> Ctrl : DistributionResponse{ status: PENDING }
      Ctrl --> UI : 200 OK
      UI --> Teacher : row returns to "Not served"
    end
  end
end
deactivate Svc
deactivate Ctrl
deactivate UI

note over Dist, DB
  The already-written serving_dishes rows survive the revoke.
  A later re-confirmation APPENDS a fresh set; the earlier rows
  become orphaned history rather than being erased (ADR-M09-3).
end note
@enduml
```

### 2.7 Sequence Diagram — Report Wrong Portion (UC-016 AF1)

```plantuml
@startuml MF09_F1_ReportError
title Sequence Diagram: Report Wrong Portion — UC-016 AF1

' Enable orthogonal lines
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME CONFIGURATION
' ==========================================
skinparam backgroundColor #FFFFFF

' Common stereotype formatting
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

' ==========================================
' Note
' ==========================================
skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

' ==========================================
' Lifelines by UML robustness stereotype
' (boundary / control / entity / database)
' ==========================================
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
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Teacher"                             as Teacher
boundary    ":MealDistributionScreen"              as UI       <<Boundary>>
control     ":DistributionController"              as Ctrl     <<Controller>>
control     ":DistributionServiceImpl"             as Svc      <<Service>>
collections ":StudentDistributionRepository"       as DistRepo <<Repository>>
collections ":DistributionErrorReportRepository"   as ErrRepo  <<Repository>>
entity      ":DistributionErrorReport"             as Report   <<Entity>>
boundary    ":ReferenceResolver"                   as Ref      <<Facade>>
database    ":PostgreSQL"                          as DB
queue       ":DomainEventBus"                      as Bus      <<EventBus>>
actor       "Service Caterers"                    as Caterer

Teacher -> UI : open "Report wrong portion", type the reason
activate UI
UI -> Ctrl : POST /api/v1/student-distributions/{code}/error-report\n{ reason: "Vegetarian portion given to an unregistered student" }
activate Ctrl
Ctrl -> Ctrl : validate reason (not blank, ≤ 500 chars)
alt reason is blank or too long
  Ctrl --> UI : 400 { code: "SRV-001", message: MSG-138 }
  UI --> Teacher : inline field error
end
Ctrl -> Svc : reportError(id, req, actor)
activate Svc

Svc -> DistRepo : findById(id)
activate DistRepo
DistRepo -> DB : SELECT * FROM student_distributions WHERE id = ?
activate DB
DB --> DistRepo : row
deactivate DB
DistRepo --> Svc : Optional<StudentDistribution>
deactivate DistRepo
alt session not found in the caller's school
  Svc --> Ctrl : throw NotFoundException(SRV-004)
  Ctrl --> UI : 404 MSG-004
  UI --> Teacher : "Serving session not found"
end

Svc -> Ref : isHomeroomTeacher(classroomId, actor.userId)
activate Ref
Ref --> Svc : true
deactivate Ref

Svc -> Report : new DistributionErrorReport(code, distributionId,\nreportedByUserId = actor.userId, reason, resolved = false, reportedAt = now())
activate Report
Report --> Svc : report(resolved = false)
deactivate Report

Svc -> ErrRepo : save(report)
activate ErrRepo
ErrRepo -> DB : INSERT INTO distribution_error_reports
activate DB
DB --> ErrRepo : 1 row inserted
deactivate DB
ErrRepo --> Svc : DistributionErrorReport
deactivate ErrRepo

Svc -> Bus : publish(DistributionErrorReported{distributionId, reason})
activate Bus
Bus -> Caterer : notify — re-issue the correct portion
Bus --> Svc : accepted
deactivate Bus

Svc --> Ctrl : DistributionErrorResponse{ resolved: false }
deactivate Svc
Ctrl --> UI : 201 Created ApiResponse<DistributionErrorResponse>
deactivate Ctrl
UI --> Teacher : "Error report sent - the class cannot be completed yet"
deactivate UI

note over ErrRepo, DB
  From this moment confirmBatch() on any session of the class
  returns 409 SRV-023 (MSG-131) until resolved = true — see §2.5.
end note
@enduml
```

### 2.8 State Chart — Student Distribution Lifecycle

```plantuml
@startuml MF09_F1_StateChart_Distribution
title State Diagram: Student Distribution Lifecycle

skinparam nodesep 90
skinparam ranksep 110

' ==========================================
' GREEN COLOUR SCHEME CONFIGURATION
' ==========================================
skinparam backgroundColor #FFFFFF

' ==========================================
' State
' ==========================================
skinparam state {
    BackgroundColor #F4FBF4
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 13
}

' ==========================================
' Arrow
' ==========================================
skinparam arrow {
    Color #2E7D32
    FontColor #1B5E20
    FontName Arial
    FontSize 12
}

' ==========================================
' Start / End
' ==========================================
skinparam startColor #2E7D32
skinparam endColor #2E7D32

state PENDING
state SERVED

[*] --> PENDING : MenuPublished
PENDING --> SERVED : confirmServed() - guards pass
PENDING --> PENDING : confirmServed() - guard failed
SERVED --> PENDING : revokeServed() - same day
SERVED --> SERVED : confirmServed() - duplicate
SERVED --> SERVED : revokeServed() - too late
PENDING --> [*]
SERVED --> [*]
@enduml
```

### 2.9 State Chart — Distribution Error Report Lifecycle

```plantuml
@startuml MF09_F1_StateChart_ErrorReport
title State Diagram: Distribution Error Report Lifecycle (UC-016 AF1)

skinparam nodesep 90
skinparam ranksep 110

' ==========================================
' GREEN COLOUR SCHEME CONFIGURATION
' ==========================================
skinparam backgroundColor #FFFFFF

' ==========================================
' State
' ==========================================
skinparam state {
    BackgroundColor #F4FBF4
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 13
}

' ==========================================
' Arrow
' ==========================================
skinparam arrow {
    Color #2E7D32
    FontColor #1B5E20
    FontName Arial
    FontSize 12
}

' ==========================================
' Start / End
' ==========================================
skinparam startColor #2E7D32
skinparam endColor #2E7D32

state OPEN
state RESOLVED

[*] --> OPEN : reportError()
OPEN --> OPEN : another portion reported
OPEN --> RESOLVED : correction verified
RESOLVED --> [*]
@enduml
```

### 2.10 State Chart — Class Distribution Progress (derived, UC-132)

```plantuml
@startuml MF09_F1_StateChart_ClassProgress
title State Diagram: Class Distribution Progress (computed at read time)

skinparam nodesep 90
skinparam ranksep 110

' ==========================================
' GREEN COLOUR SCHEME CONFIGURATION
' ==========================================
skinparam backgroundColor #FFFFFF

' ==========================================
' State
' ==========================================
skinparam state {
    BackgroundColor #F4FBF4
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 13
}

' ==========================================
' Arrow
' ==========================================
skinparam arrow {
    Color #2E7D32
    FontColor #1B5E20
    FontName Arial
    FontSize 12
}

' ==========================================
' Start / End
' ==========================================
skinparam startColor #2E7D32
skinparam endColor #2E7D32

state NOT_STARTED
state IN_PROGRESS
state BLOCKED
state COMPLETED

[*] -down-> NOT_STARTED : sessions generated
NOT_STARTED -down-> IN_PROGRESS : first confirmServed()
NOT_STARTED -left-> BLOCKED : inspection gate blocked
IN_PROGRESS -down-> COMPLETED : all distributed
IN_PROGRESS -left-> BLOCKED : error reported or gate blocked
BLOCKED -up-> IN_PROGRESS : resolved / reopened
COMPLETED -up-> IN_PROGRESS : revokeServed() same day
COMPLETED -down-> [*]
@enduml
```

---

## 3. Feature 2 — Meal Consumption Recording

Lets the homeroom teacher record how much of the served meal each student actually ate (BR-121), individually or in batch. Backs UC-080, UC-056.

### 3.1 Class Diagram

```plantuml
@startuml MF09_F2_ClassDiagram
title Class Diagram: MF09 — Feature 2 — Meal Consumption Recording

' ==========================================
' RATIONAL ROSE LAYOUT AND COLOUR CONFIGURATION
' ==========================================
skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF

' Formatting for Class / Interface / Abstract Class
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

' Relationship and arrow formatting
skinparam arrow {
    Color #162c00ff
    FontColor #162c00ff
    FontName Arial
    FontSize 11
}

' Stereotype icon formatting (C, I, A, E)
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

package "mealserving.web" {
  class MealConsumptionController <<Controller>> {
    + list()
    + record()
    + recordBatch()
  }
}

package "mealserving.service" {
  interface MealConsumptionService {
    + list()
    + record()
    + recordBatch()
    + historyOfStudent()
  }

  class MealConsumptionServiceImpl <<Service>>
  MealConsumptionServiceImpl ..|> MealConsumptionService
}

package "mealserving.repository" {
  interface MealConsumptionRepository <<Repository>> {
    + findByStudentAndDateAndMeal()
    + findByStudentAndDateBetween()
  }
}

package "mealserving.domain" {
  class MealConsumptionRecord <<Entity>> {
    code
    date
    mealType
    studentId
    consumption
    recordedAt
    + record()
    + isEditableOn()
  }

  enum ConsumptionStatus {
    PENDING
    FULLY
    PARTIALLY
    NOT_CONSUMED
  }
}

package "mealserving.dto" {
  class MealRecordResponse <<DTO>> {
    id
    student
    dishes
    consumption
  }
  class RecordConsumptionRequest <<DTO>> {
    consumption
  }
  class BatchRecordRequest <<DTO>> {
    items
    atomic
  }
  class BatchRecordResult <<DTO>> {
    recorded
    failed
  }

  BatchRecordRequest "1" *-- "0..*" RecordConsumptionRequest
}

package "mealserving.common.reference" {
  class ReferenceResolver <<Facade>> {
    + isHomeroomTeacher()
  }
}

MealConsumptionController ..> MealConsumptionService
MealConsumptionServiceImpl ..> MealConsumptionRepository
MealConsumptionServiceImpl ..> ReferenceResolver
MealConsumptionRepository ..> MealConsumptionRecord
MealConsumptionRecord ..> ConsumptionStatus
@enduml
```

**Description.** `MealConsumptionRecord` is created in `PENDING` together with the serving session (§2.2), is unique per (student, date, meal), and is editable only on the serving day (§3.3). The DTO layer maps the entity's Java-safe names (`studentName`, `className`) onto the mock-compatible JSON keys via `@JsonProperty` — `class` is a Java keyword (ADR-M09-5 / CF-M09-6).

### 3.2 Sequence Diagram — Record Meal Consumption

```plantuml
@startuml MF09_F2_RecordConsumption
title Sequence Diagram: Record Meal Consumption — UC-080 (BR-121)

' Enable orthogonal lines
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME CONFIGURATION
' ==========================================
skinparam backgroundColor #FFFFFF

' Common stereotype formatting
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

' ==========================================
' Note
' ==========================================
skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

' ==========================================
' Lifelines by UML robustness stereotype
' (boundary / control / entity / database)
' ==========================================
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
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Teacher\n(Homeroom — GVCN)"      as Teacher
boundary    ":MealConsumptionScreen"          as UI       <<Boundary>>
control     ":MealConsumptionController"      as Ctrl     <<Controller>>
control     ":MealConsumptionServiceImpl"     as Svc      <<Service>>
collections ":MealConsumptionRepository"      as RecRepo  <<Repository>>
entity      ":MealConsumptionRecord"          as Rec      <<Entity>>
boundary    ":ReferenceResolver"              as Ref      <<Facade>>
database    ":PostgreSQL"                     as DB
queue       ":DomainEventBus"                 as Bus      <<EventBus>>

Teacher -> UI : pick "Partially eaten" for a student
activate UI
UI -> Ctrl : PATCH /api/v1/meal-records/{code}\n{ "consumption": "PARTIALLY" }
activate Ctrl
Ctrl -> Ctrl : bind + @Valid RecordConsumptionRequest
alt consumption not in {PENDING, FULLY, PARTIALLY, NOT_CONSUMED}
  Ctrl --> UI : 400 { code: "SRV-001",\nmessage: "Validation failed: consumption must be one of\nPENDING, FULLY, PARTIALLY, NOT_CONSUMED" } — MSG-138
  UI --> Teacher : inline validation error
end
Ctrl -> Svc : record(id, req, actor)
activate Svc

Svc -> RecRepo : findById(id)
activate RecRepo
RecRepo -> DB : SELECT * FROM meal_consumption_records WHERE id = ?
activate DB
DB --> RecRepo : row
deactivate DB
RecRepo --> Svc : Optional<MealConsumptionRecord>
deactivate RecRepo
alt record not found in the caller's school
  Svc --> Ctrl : throw NotFoundException(SRV-004)
  Ctrl --> UI : 404 MSG-004
  UI --> Teacher : "Record not found"
end

Svc -> Ref : isHomeroomTeacher(classroomId, actor.userId)
activate Ref
Ref --> Svc : boolean
deactivate Ref
alt not the homeroom teacher of this class
  Svc --> Ctrl : throw ForbiddenException(SRV-005)
  Ctrl --> UI : 403 MSG-005
  UI --> Teacher : "You are not allowed to record for this class"
end

Svc -> Rec : isEditableOn(today)
activate Rec
Rec --> Svc : boolean
deactivate Rec
alt record.date is before today
  Svc --> Ctrl : throw ConflictException(SRV-025)
  Ctrl --> UI : 409 { code: "SRV-025",\nmessage: "Consumption record is locked after\nthe serving day." } — MSG-133
  UI --> Teacher : the row is shown read-only
else still the serving day
  Svc -> Rec : record(req.consumption, now(), actor.userId)
  activate Rec
  Rec --> Svc : consumption = PARTIALLY, recordedAt = now()
  deactivate Rec
  Svc -> RecRepo : save(record)
  activate RecRepo
  RecRepo -> DB : UPDATE meal_consumption_records\nSET consumption, recorded_at, recorded_by_user_id
  activate DB
  DB --> RecRepo : 1 row updated
  deactivate DB
  RecRepo --> Svc : MealConsumptionRecord
  deactivate RecRepo
  Svc -> Bus : publish(MealConsumptionRecorded{studentId, date, consumption})
  activate Bus
  Bus --> Svc : accepted
  deactivate Bus
  Svc -> Svc : toResponse(record)
  Svc --> Ctrl : MealRecordResponse
  Ctrl --> UI : 200 OK ApiResponse<MealRecordResponse>
  UI --> Teacher : row shows "Partially eaten" + recorded time
end
deactivate Svc
deactivate Ctrl
deactivate UI
@enduml
```

### 3.3 State Chart — Meal Consumption Record Lifecycle

```plantuml
@startuml MF09_F2_StateChart_Consumption
title State Diagram: Meal Consumption Record Lifecycle (BR-121)

skinparam nodesep 90
skinparam ranksep 110

' ==========================================
' GREEN COLOUR SCHEME CONFIGURATION
' ==========================================
skinparam backgroundColor #FFFFFF

' ==========================================
' State
' ==========================================
skinparam state {
    BackgroundColor #F4FBF4
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 13
}

' ==========================================
' Arrow
' ==========================================
skinparam arrow {
    Color #2E7D32
    FontColor #1B5E20
    FontName Arial
    FontSize 12
}

' ==========================================
' Start / End
' ==========================================
skinparam startColor #2E7D32
skinparam endColor #2E7D32

state PENDING
state "RECORDED" as RECORDED {
  state FULLY
  state PARTIALLY
  state NOT_CONSUMED
}

[*] --> PENDING : session created
PENDING --> RECORDED : record(value)
RECORDED --> RECORDED : record(otherValue) - re-edit
RECORDED --> PENDING : record(PENDING)
PENDING --> PENDING : record() - invalid or unauthorized
PENDING --> [*]
RECORDED --> [*] : locked next day
@enduml
```

---

## 4. Feature 3 — Meal History (Parent / Teacher, read-only)

A pure read projection over data already owned by Feature 1 and Feature 2 — there is no dedicated `meal_history` table (CF-M09-7). Backs UC-133, UC-053, UC-056 (teacher's per-student view).

### 4.1 Class Diagram

```plantuml
@startuml MF09_F3_ClassDiagram
title Class Diagram: MF09 — Feature 3 — Meal History (read-only projection)

' ==========================================
' RATIONAL ROSE LAYOUT AND COLOUR CONFIGURATION
' ==========================================
skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF

' Formatting for Class / Interface / Abstract Class
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

' Relationship and arrow formatting
skinparam arrow {
    Color #162c00ff
    FontColor #162c00ff
    FontName Arial
    FontSize 11
}

' Stereotype icon formatting (C, I, A, E)
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

package "mealserving.web" {
  class MealHistoryController <<Controller>> {
    + listForChild()
    + detail()
  }
}

package "mealserving.service" {
  interface MealHistoryService {
    + listForChild()
    + detail()
  }

  class MealHistoryServiceImpl <<Service>>
  MealHistoryServiceImpl ..|> MealHistoryService
}

package "mealserving.repository" {
  interface StudentDistributionRepository <<Repository>>
  interface ServingDishRepository <<Repository>>
  interface MealConsumptionRepository <<Repository>>
}

package "mealserving.projection" {
  class MealHistoryEntry <<Projection>> {
    date
    mealType
    kcal
    consumption
    caterer
    dishes
  }
  class MealHistoryDishView <<Projection>> {
    dishId
    name
    kcal
  }
  MealHistoryEntry "1" o-- "0..*" MealHistoryDishView
}

package "mealserving.dto" {
  class MealHistoryEntryResponse <<DTO>> {
    date
    kcal
    consumption
    caterer
  }
  class MealHistoryDetailResponse <<DTO>> {
    dishes
    specialMealServed
  }
  MealHistoryEntryResponse <.. MealHistoryEntry : mapped from
}

package "mealserving.common.reference" {
  class ReferenceResolver <<Facade>> {
    + isParentLinkedToStudent()
    + linkedStudentIds()
  }
  interface MenuQueryPort <<Port>>
}

MealHistoryController ..> MealHistoryService
MealHistoryServiceImpl ..> StudentDistributionRepository : join
MealHistoryServiceImpl ..> ServingDishRepository : join
MealHistoryServiceImpl ..> MealConsumptionRepository : join
MealHistoryServiceImpl ..> ReferenceResolver
MealHistoryServiceImpl ..> MenuQueryPort
MealHistoryServiceImpl ..> MealHistoryEntry : builds
@enduml
```

**Description.** `MealHistoryEntry`/`MealHistoryDishView` are pure projections, never persisted: the service joins `student_distributions` ⋈ `serving_dishes` ⋈ `meal_consumption_records` ⋈ `Dish.kcal` (MF05, by frozen `dish_id`) at read time (ADR-M09-2, CF-M09-7). `caterer` is the one field read straight from a frozen column instead of recomputed (ADR-M09-8), so a later change of caterer never rewrites a family's past history.

### 4.2 Sequence Diagram — View Child Meal History

```plantuml
@startuml MF09_F3_ViewMealHistory
title Sequence Diagram: View Child Meal History — UC-133 / UC-053

' Enable orthogonal lines
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME CONFIGURATION
' ==========================================
skinparam backgroundColor #FFFFFF

' Common stereotype formatting
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

' ==========================================
' Note
' ==========================================
skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

' ==========================================
' Lifelines by UML robustness stereotype
' (boundary / control / entity / database)
' ==========================================
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
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Parent"                             as Parent
boundary    ":ChildMealHistoryScreen\n(Mobile)"   as UI       <<Boundary>>
control     ":MealHistoryController"              as Ctrl     <<Controller>>
control     ":MealHistoryServiceImpl"             as Svc      <<Service>>
boundary    ":ReferenceResolver"                  as Ref      <<Facade>>
collections ":StudentDistributionRepository"      as DistRepo <<Repository>>
collections ":ServingDishRepository"              as DishRepo <<Repository>>
collections ":MealConsumptionRepository"          as RecRepo  <<Repository>>
boundary    ":MenuQueryPort"                      as Menu     <<Port>>
entity      ":MealHistoryEntry"                   as Entry    <<Projection>>
database    ":PostgreSQL"                         as DB

Parent -> UI : open "Child meal history",\npick a date range
activate UI
UI -> Ctrl : GET /api/v1/students/{code}/meal-history\n?dateFrom=...&dateTo=...
activate Ctrl
Ctrl -> Ctrl : resolve {code} → studentId, bind MealHistoryFilter
Ctrl -> Svc : listForChild(studentId, filter, actor)
activate Svc

group Scope guard
  Svc -> Ref : isParentLinkedToStudent(actor.userId, studentId)
  activate Ref
  Ref -> DB : native SELECT on student_parents (MF03, by id)
  activate DB
  DB --> Ref : link row / none
  deactivate DB
  Ref --> Svc : boolean linked
  deactivate Ref
  alt not linked and not the homeroom teacher of the student's class
    Svc --> Ctrl : throw ForbiddenException(SRV-005)
    Ctrl --> UI : 403 MSG-005
    UI --> Parent : "You are not allowed to view this student's record"
  end
end

Svc -> DistRepo : findByStudentIdAndDateBetween(studentId, dateFrom, dateTo)
activate DistRepo
DistRepo -> DB : SELECT * FROM student_distributions\nWHERE student_id = ? AND date BETWEEN ? AND ?
activate DB
DB --> DistRepo : rows
deactivate DB
DistRepo --> Svc : List<StudentDistribution>
deactivate DistRepo

loop for each distribution
  Svc -> DishRepo : findByDistributionId(distribution.id)
  activate DishRepo
  DishRepo -> DB : SELECT * FROM serving_dishes WHERE distribution_id = ?
  activate DB
  DB --> DishRepo : rows
  deactivate DB
  DishRepo --> Svc : List<ServingDish>
  deactivate DishRepo
end

Svc -> RecRepo : findByStudentIdAndDateBetweenOrderByDateDesc(studentId, dateFrom, dateTo)
activate RecRepo
RecRepo -> DB : SELECT * FROM meal_consumption_records\nWHERE student_id = ? AND date BETWEEN ? AND ?
activate DB
DB --> RecRepo : rows
deactivate DB
RecRepo --> Svc : List<MealConsumptionRecord>
deactivate RecRepo

Svc -> Menu : kcal of each frozen dishId (MF05, by id)
activate Menu
Menu --> Svc : Map<dishId, kcal>
deactivate Menu

Svc -> Entry : build(distribution, servingDishes,\nconsumption, kcal, frozen catererName)
activate Entry
Entry --> Svc : List<MealHistoryEntry>
deactivate Entry
note right of Svc
  ADR-M09-2 — kcal and the dish list are recomputed at read time.
  ADR-M09-8 — caterer is the FROZEN column, never re-looked-up,
  so a later change of caterer cannot rewrite past history.
end note

Svc --> Ctrl : PagedResponse<MealHistoryEntryResponse>
deactivate Svc
Ctrl --> UI : 200 OK ApiResponse<PagedResponse<...>>
deactivate Ctrl
UI --> Parent : timeline of meals with dishes, kcal\nand consumption per day
deactivate UI
@enduml
```

---

## 5. Feature 4 — Special Meal View (read-only)

Read-only visibility into special/substitute meal plans for students with a verified allergy or diet requirement, split across two roles: teachers see their class's requirements before/during service (BR-125), parents see the history of what was actually arranged for their child. Backs UC-142, UC-143 (UC-141 is out of MF09's scope — see CF-M09-13).

### 5.1 Class Diagram

```plantuml
@startuml MF09_F4_ClassDiagram
title Class Diagram: MF09 — Feature 4 — Special Meal View (read-only)

' ==========================================
' RATIONAL ROSE LAYOUT AND COLOUR CONFIGURATION
' ==========================================
skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF

' Formatting for Class / Interface / Abstract Class
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

' Relationship and arrow formatting
skinparam arrow {
    Color #162c00ff
    FontColor #162c00ff
    FontName Arial
    FontSize 11
}

' Stereotype icon formatting (C, I, A, E)
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

package "mealserving.web" {
  class SpecialMealViewController <<Controller>> {
    + requirementsOfClass()
    + recordsOfChild()
  }
}

package "mealserving.service" {
  interface SpecialMealViewService {
    + requirementsOfClass()
    + arrangementsOfChild()
    + recordsOfChild()
  }

  class SpecialMealViewServiceImpl <<Service>>
  SpecialMealViewServiceImpl ..|> SpecialMealViewService
}

package "mealserving.repository" {
  interface StudentDistributionRepository <<Repository>>
  interface ServingDishRepository <<Repository>>
}

package "mealserving.dto" {
  class SpecialMealRequirementResponse <<DTO>> {
    studentId
    dietTag
    allergens
    requiredSubstitution
  }
  class SpecialMealRecordResponse <<DTO>> {
    distributionId
    date
    substitutedDishes
    reason
  }
}

package "mealserving.common.reference" {
  class ReferenceResolver <<Facade>> {
    + specialDietOfStudent()
    + verifiedAllergensOfStudent()
    + isHomeroomTeacher()
    + isParentLinkedToStudent()
  }
}

SpecialMealViewController ..> SpecialMealViewService
SpecialMealViewServiceImpl ..> StudentDistributionRepository : isSpecial=yes filter
SpecialMealViewServiceImpl ..> ServingDishRepository : substituted dishes
SpecialMealViewServiceImpl ..> ReferenceResolver
@enduml
```

**Description.** `SpecialMealRecordResponse` (UC-142, parent-facing) needs no new table — it is `student_distributions` filtered to sessions that produced at least one substitute `ServingDish`. `SpecialMealRequirementResponse` (UC-143, teacher-facing) reads MF03's `special_meals` by native query instead, following the same "read by id, never by ORM relation" discipline (AP-M09-07, M09-D06).

### 5.2 Sequence Diagram — View Special Meal Requirements of Class

```plantuml
@startuml MF09_F4_ViewRequirements
title Sequence Diagram: View Special Meal Requirements of Class — UC-143 (BR-125)

' Enable orthogonal lines
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME CONFIGURATION
' ==========================================
skinparam backgroundColor #FFFFFF

' Common stereotype formatting
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

' ==========================================
' Note
' ==========================================
skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

' ==========================================
' Lifelines by UML robustness stereotype
' (boundary / control / entity / database)
' ==========================================
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
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Teacher /\nService Caterers"        as User
boundary    ":SpecialMealRequirementScreen"      as UI       <<Boundary>>
control     ":SpecialMealViewController"         as Ctrl     <<Controller>>
control     ":SpecialMealViewServiceImpl"        as Svc      <<Service>>
collections ":StudentDistributionRepository"     as DistRepo <<Repository>>
boundary    ":ReferenceResolver"                 as Ref      <<Facade>>
database    ":PostgreSQL"                        as DB

User -> UI : open "Special meals of the class" before service
activate UI
UI -> Ctrl : GET requirements of the class (className)
activate Ctrl
Ctrl -> Svc : requirementsOfClass(className, actor)
activate Svc

group Scope guard
  Svc -> Svc : assert actor.role ∈ {TEACHER, SERVICE_CATERERS}
  alt actor.role = TEACHER
    Svc -> Ref : isHomeroomTeacher(classroomId, actor.userId)
    activate Ref
    Ref --> Svc : boolean
    deactivate Ref
  end
  alt not authorized for this class
    Svc --> Ctrl : throw ForbiddenException(SRV-005)
    Ctrl --> UI : 403 MSG-005
    UI --> User : "You are not allowed to view this class"
  end
end

Svc -> DistRepo : findByDateAndClassNameAndMealType(today, className, mealType)
activate DistRepo
DistRepo -> DB : SELECT ... — the class roster of the session
activate DB
DB --> DistRepo : rows
deactivate DB
DistRepo --> Svc : List<StudentDistribution>
deactivate DistRepo

loop for each student of the roster
  Svc -> Ref : specialDietOfStudent(studentId)
  activate Ref
  Ref -> DB : native read-only SELECT on MF03 special_meals\n(by id, no entity import — AP-M09-07)
  activate DB
  DB --> Ref : row / none
  deactivate DB
  Ref --> Svc : Optional<String> dietTag
  deactivate Ref

  Svc -> Ref : verifiedAllergensOfStudent(studentId)
  activate Ref
  Ref -> DB : native read-only SELECT on MF03 allergy_declarations\n(verified only)
  activate DB
  DB --> Ref : rows
  deactivate DB
  Ref --> Svc : List<String> allergens
  deactivate Ref

  Svc -> Svc : build SpecialMealRequirementResponse\n{studentId, dietTag, allergens, requiredSubstitution}
end

Svc --> Ctrl : List<SpecialMealRequirementResponse>
deactivate Svc
Ctrl --> UI : 200 OK ApiResponse<List<...>>
deactivate Ctrl
UI --> User : requirement list shown before and during service
deactivate UI

note over Ctrl, Ref
  M09-D06 — MF09 does NOT open a controller on /api/v1/special-meals:
  that resource belongs to MF03. MF09 only re-exposes the requirement
  at serving time for the roles that need it (BR-125).
end note
@enduml
```

### 5.3 Sequence Diagram — View Special Meal Records of a Child (UC-142)

```plantuml
@startuml MF09_F4_ViewRecordsOfChild
title Sequence Diagram: View Special Meal Records of a Child — UC-142

' Enable orthogonal lines
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME CONFIGURATION
' ==========================================
skinparam backgroundColor #FFFFFF

' Common stereotype formatting
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

' ==========================================
' Note
' ==========================================
skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

' ==========================================
' Lifelines by UML robustness stereotype
' (boundary / control / entity / database)
' ==========================================
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
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Parent"                          as Parent
boundary    ":SpecialMealRecordScreen\n(Mobile)" as UI       <<Boundary>>
control     ":SpecialMealViewController"       as Ctrl     <<Controller>>
control     ":SpecialMealViewServiceImpl"      as Svc      <<Service>>
boundary    ":ReferenceResolver"               as Ref      <<Facade>>
collections ":StudentDistributionRepository"   as DistRepo <<Repository>>
collections ":ServingDishRepository"           as DishRepo <<Repository>>
database    ":PostgreSQL"                      as DB

Parent -> UI : open "Special meals of my child"
activate UI
UI -> Ctrl : GET /api/v1/students/{code}/special-meal-records\n?page=0&pageSize=10
activate Ctrl
Ctrl -> Ctrl : resolve {code} → studentId, bind PageRequestDto
Ctrl -> Svc : recordsOfChild(studentId, page, actor)
activate Svc

Svc -> Ref : isParentLinkedToStudent(actor.userId, studentId)
activate Ref
Ref -> DB : native SELECT on student_parents (MF03)
activate DB
DB --> Ref : link row / none
deactivate DB
Ref --> Svc : boolean linked
deactivate Ref
alt not linked to this student
  Svc --> Ctrl : throw ForbiddenException(SRV-005)
  Ctrl --> UI : 403 MSG-005
  UI --> Parent : "You are not allowed to view this student's record"
end

Svc -> DistRepo : findByStudentIdAndDateBetween(studentId, from, to)
activate DistRepo
DistRepo -> DB : SELECT * FROM student_distributions\nWHERE student_id = ? AND status = 'SERVED'
activate DB
DB --> DistRepo : rows
deactivate DB
DistRepo --> Svc : List<StudentDistribution>
deactivate DistRepo

loop for each served distribution
  Svc -> DishRepo : findByDistributionId(distribution.id)
  activate DishRepo
  DishRepo -> DB : SELECT * FROM serving_dishes WHERE distribution_id = ?
  activate DB
  DB --> DishRepo : rows
  deactivate DB
  DishRepo --> Svc : List<ServingDish>
  deactivate DishRepo
  Svc -> Svc : keep the session only if any dish\nhas isSubstitute = true (isSpecial = yes)
end

Svc --> Ctrl : PagedResponse<SpecialMealRecordResponse>
deactivate Svc
Ctrl --> UI : 200 OK ApiResponse<PagedResponse<...>>
deactivate Ctrl
UI --> Parent : list of days where a substitute portion\nwas actually served, with its reason
deactivate UI

note over DistRepo, DB
  No new table: "special meal record" is just
  student_distributions filtered to sessions that produced
  at least one substitute ServingDish (CF-M09-7).
end note
@enduml
```

---

## 6. Feature 5 — Allergy Alert (read-only)

Surfaces, per serving session, which students have a *verified* allergy that conflicts with today's menu, so caterers and teachers can double-check before handing out portions (BR-123). Backs UC-103.

### 6.1 Class Diagram

```plantuml
@startuml MF09_F5_ClassDiagram
title Class Diagram: MF09 — Feature 5 — Allergy Alert (read-only projection)

' ==========================================
' RATIONAL ROSE LAYOUT AND COLOUR CONFIGURATION
' ==========================================
skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF

' Formatting for Class / Interface / Abstract Class
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

' Relationship and arrow formatting
skinparam arrow {
    Color #162c00ff
    FontColor #162c00ff
    FontName Arial
    FontSize 11
}

' Stereotype icon formatting (C, I, A, E)
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

package "mealserving.web" {
  class AllergyAlertController <<Controller>> {
    + list()
  }
}

package "mealserving.service" {
  interface AllergyAlertService {
    + listForSession()
  }

  class AllergyAlertServiceImpl <<Service>>
  AllergyAlertServiceImpl ..|> AllergyAlertService
}

package "mealserving.repository" {
  interface StudentDistributionRepository <<Repository>>
}

package "mealserving.projection" {
  class AllergyAlert <<Projection>> {
    studentId
    allergens
    severity
    conflictWithTodayMenu
    distributionId
  }
}

package "mealserving.dto" {
  class AllergyAlertResponse <<DTO>> {
    allergens
    severity
    conflictWithTodayMenu
  }
  AllergyAlertResponse <.. AllergyAlert : mapped from
}

package "mealserving.common.reference" {
  class ReferenceResolver <<Facade>> {
    + verifiedAllergensOfStudent()
  }
  interface MenuQueryPort <<Port>> {
    + publishedBlock()
  }
}

AllergyAlertController ..> AllergyAlertService
AllergyAlertServiceImpl ..> StudentDistributionRepository : session roster
AllergyAlertServiceImpl ..> ReferenceResolver : verified allergens (MF03)
AllergyAlertServiceImpl ..> MenuQueryPort : today's menu allergens (MF05)
AllergyAlertServiceImpl ..> AllergyAlert : builds
@enduml
```

**Description.** `AllergyAlert` is a projection, not a table: for every session in the roster the service intersects the student's *verified* allergens (BR-031) with the allergen tags of today's published menu, and sets `conflictWithTodayMenu = true` only on a real intersection (ADR-M09-2, CF-M09-8). The endpoint is advisory — the actual block-and-substitute enforcement stays in `confirmServed()` (§2.3).

### 6.2 Sequence Diagram — List Allergy Alerts for a Session

```plantuml
@startuml MF09_F5_ListAllergyAlerts
title Sequence Diagram: List Allergy Alerts for a Session — UC-103 (BR-123)

' Enable orthogonal lines
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME CONFIGURATION
' ==========================================
skinparam backgroundColor #FFFFFF

' Common stereotype formatting
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

' ==========================================
' Note
' ==========================================
skinparam NoteBackgroundColor #E8F5E9
skinparam NoteBorderColor #2E7D32
skinparam NoteFontColor #212121
skinparam NoteFontName Arial

' ==========================================
' Lifelines by UML robustness stereotype
' (boundary / control / entity / database)
' ==========================================
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
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

autonumber

actor       "Service Caterers /\nTeacher"       as User
boundary    ":AllergyAlertPanel"                as UI       <<Boundary>>
control     ":AllergyAlertController"           as Ctrl     <<Controller>>
control     ":AllergyAlertServiceImpl"          as Svc      <<Service>>
collections ":StudentDistributionRepository"    as DistRepo <<Repository>>
boundary    ":MenuQueryPort"                    as Menu     <<Port>>
boundary    ":ReferenceResolver"                as Ref      <<Facade>>
entity      ":AllergyAlert"                     as Alert    <<Projection>>
database    ":PostgreSQL"                       as DB
queue       ":DomainEventBus"                   as Bus      <<EventBus>>

User -> UI : open the serving screen for class 3A
activate UI
UI -> Ctrl : GET /api/v1/allergy-alerts\n?date=...&mealType=LUNCH&class=3A
activate Ctrl
Ctrl -> Svc : listForSession(date, mealType, params, actor)
activate Svc

Svc -> DistRepo : findByDateAndClassNameAndMealType(date, class, mealType)
activate DistRepo
DistRepo -> DB : SELECT * FROM student_distributions\nWHERE date = ? AND class_name = ? AND meal_type = ?
activate DB
DB --> DistRepo : rows (roster of the session)
deactivate DB
DistRepo --> Svc : List<StudentDistribution>
deactivate DistRepo

Svc -> Menu : publishedBlock(date, gradeOf(class), mealType, schoolId)
activate Menu
Menu --> Svc : Optional<MenuBlockView{dishes[] with allergen tags}>
deactivate Menu

loop for each distribution in the roster
  Svc -> Ref : verifiedAllergensOfStudent(studentId)
  activate Ref
  Ref -> DB : native SELECT on allergy_declarations (MF03)\nverified rows only
  activate DB
  DB --> Ref : rows
  deactivate DB
  Ref --> Svc : List<String> allergens
  deactivate Ref

  Svc -> Svc : conflict = allergens ∩ menu.allergens ≠ ∅\nseverity = classify(allergens)
  Svc -> Alert : new AllergyAlert{studentId, allergens, severity,\nconflictWithTodayMenu = conflict, distributionId}
  activate Alert
  Alert --> Svc : alert
  deactivate Alert

  opt conflict = true
    Svc -> Bus : publish(AllergyConflictDetectedAtServing)
    activate Bus
    Bus --> Svc : accepted (→ Teacher, Service Caterers, Medical, Audit)
    deactivate Bus
  end
end

Svc --> Ctrl : PagedResponse<AllergyAlertResponse>
deactivate Svc
Ctrl --> UI : 200 OK ApiResponse<PagedResponse<...>>
deactivate Ctrl
UI --> User : at-risk students pre-flagged in red\nbefore any portion is handed over
deactivate UI

note over Svc, Alert
  ADR-M09-2 / CF-M09-8 — allergens are ALWAYS listed, but
  conflictWithTodayMenu is true only on a real intersection with
  today's published menu. The flag is derived, never stored.
end note
@enduml
```

---

## Appendix — Cross-Feature Notes

- **No `MealSession` entity.** A "serving session" is always the composite key `(date, mealType)` scoped by `schoolId`/`className`, matched to how the mock UI represents it (button-tap confirmation, not a physical scan device) — TDS §2.3 CF-M09-2, ADR-M09-3. This is why every sequence diagram above resolves the session by `(date, mealType)` rather than by a session id.
- **Cross-module reads are always by id, never by JPA relation.** Every arrow from an MF09 `Service`/`ServiceImpl` class to another module in the diagrams above goes through `ReferenceResolver` or a narrowly-scoped `Port` interface (`MenuQueryPort`), consistent with the "no `@ManyToOne` across module boundaries" rule (AP-M09-07) reflected in the DBML's external stub tables in §1.
- **Biometric/face-scan flows are intentionally absent.** UC-079 appears in the traceability matrix under Meal Serving but BR-051 explicitly forbids biometric capture for children; it is not modeled here and belongs to MF04's delivery-staff verification instead (CF-M09-1).
- **Only two objects in MF09 carry persisted state.** `StudentDistribution` (§2.8) and `MealConsumptionRecord` (§3.3), plus the auxiliary `DistributionErrorReport` (§2.9). `ServingDish` has no state chart on purpose: it is append-only, so it has exactly one state and no transition (ADR-M09-1). The class-progress chart in §2.10 is derived at read time and is included only because it is where the other lifecycles interact.
- **Guards and error codes are the join between the artifacts.** Every `[guard]` in a state chart is the same condition as an `alt` branch in the matching sequence diagram and the same `SRV-xxx` code in TDS §10 — so the three views can be reviewed against each other rather than independently. Codes used above: `SRV-001` (validation), `SRV-004` (not found), `SRV-005` (scope), `SRV-021` (duplicate serving), `SRV-022` (no menu), `SRV-023` (unresolved error report), `SRV-024` (revoke after the day), `SRV-025` (consumption locked), `SRV-094` (inspection gate).
- **Two lifecycle rules are enforced by the database, not only by the diagrams.** `uq_dist_student_date_meal`/`uq_mcr_student_date_meal` make a second session or record for the same `(student, date, meal)` impossible, and `ck_dist_served_at`/`ck_dist_served_same_day` make an inconsistent `SERVED` row impossible — so the state charts above hold even if a service method is bypassed.
