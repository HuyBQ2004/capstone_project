# MF02 — School & Class Management (+ Academic Year)
## Design Diagrams (Database, Class, Sequence, State)

> **Stack:** Spring Boot 4.1.x (Java 25) · PostgreSQL 16 · Flyway · Spring Security + JWT (RBAC, 7 roles) · springdoc-OpenAPI · MapStruct
> **Bounded Context:** Organization & Academic Structure
> **Based on:** TDS `SFMS-MF02-IMP-001 v1.0` (Approved 2026-07-31)

---

## Database Architecture (DBML)

> Reflects Flyway `V1__init_schema.sql`, MF02 section. All tables use UUID primary keys generated server-side (`gen_random_uuid()`). Every business entity also exposes an immutable `code` (D1) — the value returned as `"id"` in the JSON API and used as the path parameter; the UUID `id` never leaves the backend. `classrooms.homeroom_teacher_id` is the **only** MF02 column with a real foreign key into another module's table (`user_accounts`, owned by MF01) — kept because both tables share the same physical database; all other cross-module links (e.g. student/staff counts) are UUID-only references resolved through ports, with no FK.

```dbml
// Use DBML to define your database structure
// Docs: https://dbml.dbdiagram.io/docs
// Module: MF02 — School & Class Management + Academic Year
// Source: TDS SFMS-MF02-IMP-001 v1.0 | Flyway V1__init_schema.sql

Table schools {
  id                uuid         [pk, default: `gen_random_uuid()`, note: "Internal PK — never exposed in JSON/path"]
  code              varchar(32)  [unique, not null, note: "D1: business identifier e.g. SCH-HN-0042, returned as \"id\" in API"]
  name              varchar(180) [not null, note: "SEARCH_FIELDS (ADR-M02-1)"]
  address           varchar(255) [not null, note: "SEARCH_FIELDS"]
  tax_id            varchar(20)  [unique, not null, note: "BR-028: format validated on register (SCH-007 if invalid)"]
  email             varchar(180) [not null, note: "SEARCH_FIELDS"]
  lat               decimal(9,6) [not null, note: "BR-028: must be in [-90, 90]"]
  lng               decimal(9,6) [not null, note: "BR-028: must be in [-180, 180]"]
  coordinate_locked boolean      [not null, default: false, note: "BR-028: set true after school becomes ACTIVE"]
  operating_hours   varchar(80)  [null]
  emergency_contact varchar(120) [null]
  meals_per_day     int          [not null, default: 0]
  status            varchar(16)  [not null, default: 'PENDING', note: "PENDING | ACTIVE | INACTIVE (UC-070/UC-001)"]
  registered_at     timestamptz  [not null, default: `now()`]
  created_by        varchar(180) [null]

  indexes {
    status  [name: "ix_school_status"]
    tax_id  [unique, name: "ix_school_taxid"]
    code    [unique, name: "ix_school_code"]
  }

  Note: "Derived counters (students/classes/staff) are NEVER stored — computed at runtime via COUNT (ADR-M02-2)."
}

Table school_config {
  id               uuid         [pk, default: `gen_random_uuid()`]
  school_id        uuid         [unique, not null, ref: > schools.id, note: "1:1 with school — one config row per school"]
  cost_ceiling     decimal(12,2) [null, note: "BR-080 — owned by MF05, referenced read-only here"]
  geofence_radius  int          [not null, default: 200, note: "BR-028: bounded 50..500 meters"]
  report_emails    jsonb        [not null, default: '[]']
  updated_at       timestamptz  [null]
  updated_by       varchar(180) [null]

  Note: "Addressed only via /schools/{code}/config (child of School) — no standalone /school-config/{id} endpoint."
}

Table academic_years {
  id             uuid        [pk, default: `gen_random_uuid()`]
  code           varchar(32) [not null, note: "D1: e.g. AY-2025, returned as \"id\" in API"]
  school_id      uuid        [not null, ref: > schools.id, note: "BR-191: lifecycle is enforced per school (not global)"]
  name           varchar(60) [not null, note: "e.g. \"School Year 2026-2027\""]
  start_date     date        [not null]
  end_date       date        [not null]
  status         varchar(16) [not null, default: 'DRAFT', note: "DRAFT | ACTIVE | CLOSED | ARCHIVED"]
  copy_structure boolean     [not null, default: false, note: "BR-194: whether classroom structure was copied from the previous year"]
  created_at     timestamptz [not null, default: `now()`]

  indexes {
    (school_id, code)   [unique, name: "uq_ay_school_code"]
    (school_id, name)   [unique, name: "uq_ay_school_name"]
    (school_id, status) [name: "ix_ay_school_status"]
  }

  Note: "BR-191 invariant enforced by DB: UNIQUE INDEX uq_ay_one_active_per_school ON academic_years(school_id) WHERE status='ACTIVE'. Archived rows remain queryable read-only forever (BR-195)."
}

Table classrooms {
  id                  uuid        [pk, default: `gen_random_uuid()`]
  code                varchar(20) [not null, note: "D1: e.g. \"3A\", immutable identifier, returned as \"id\" in API"]
  school_id           uuid        [not null, ref: > schools.id]
  academic_year_id    uuid        [not null, ref: > academic_years.id, note: "BR-192: classroom cannot exist without a parent academic year"]
  name                varchar(20) [not null, note: "Display label — editable independently of code (chốt 2026-08-01: kept as 2 distinct fields, never merged)"]
  grade               int         [not null, note: "BR-024: CHECK grade BETWEEN 1 AND 12"]
  capacity            int         [not null, default: 50, note: "BR-025: CHECK capacity BETWEEN 1 AND 50"]
  homeroom_teacher_id uuid        [null, ref: > user_accounts.id, note: "BR-020: resolved from code OR staff_code via native query (ADR-M02-5) — real cross-DB FK, MF01-owned table"]
  read_only           boolean     [not null, default: false, note: "BR-193: set true when parent academic year is CLOSED/ARCHIVED"]
  created_at          timestamptz [not null, default: `now()`]

  indexes {
    (academic_year_id, code) [unique, name: "uq_class_ay_code"]
    (academic_year_id, name) [unique, name: "uq_class_ay_name"]
    (academic_year_id, grade) [name: "ix_class_ay_grade"]
    (academic_year_id, homeroom_teacher_id) [unique, name: "uq_homeroom_teacher_per_year", note: "Partial unique WHERE homeroom_teacher_id IS NOT NULL — BR-020: one teacher homerooms at most one classroom per academic year"]
  }

  Note: "Derived counters (students/allergies) are NEVER stored — computed at runtime (ADR-M02-2)."
}

Table user_accounts {
  id uuid [pk, note: "Owned by MF01 — shown here only as the FK target of classrooms.homeroom_teacher_id (ADR-M02-5). Full definition lives in the MF01 design doc."]
}

// ── Relationships summary ──────────────────────────────────────────────────
// school_config.school_id       → schools.id            (1:1, UC-095)
// academic_years.school_id      → schools.id             (BR-191, per-school lifecycle)
// classrooms.school_id          → schools.id
// classrooms.academic_year_id   → academic_years.id      (BR-192)
// classrooms.homeroom_teacher_id→ user_accounts.id        (MF01, BR-020, ADR-M02-5)
```

---

## 1. School Registration & Profile Management

### 1.1 Class Diagram

```plantuml
@startuml MF02_School_ClassDiagram
title Design-level Class Diagram: School Registration & Profile Management

' ==========================================
' CẤU HÌNH BỐ CỤC VÀ MÀU SẮC RATIONAL ROSE
' ==========================================
skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF

' Định dạng cho Class / Interface / Abstract Class
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

' Định dạng Mũi tên & Liên kết (Relationships & Arrows)
skinparam arrow {
    Color #162c00ff
    FontColor #162c00ff
    FontName Arial
    FontSize 11
}

' Định dạng Icon Stereotype (C, I, A, E)
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

enum SchoolStatus {
  PENDING
  ACTIVE
  INACTIVE
}

class School {
  +id : UUID <<PK>>
  +code : String <<unique>>
  +name : String
  +address : String
  +taxId : String <<unique>>
  +email : String
  +lat : BigDecimal
  +lng : BigDecimal
  +coordinateLocked : boolean
  +operatingHours : String
  +emergencyContact : String
  +mealsPerDay : int
  +status : SchoolStatus
  +registeredAt : Instant
  +createdBy : String
  +isCoordinateEditable() : boolean
  +lockCoordinate() : void
}

class SchoolConfig {
  +id : UUID <<PK>>
  +schoolId : UUID <<FK>>
  +costCeiling : BigDecimal
  +geofenceRadius : int
  +reportEmails : List<String>
  +updatedAt : Instant
  +updatedBy : String
}

class RegisterSchoolRequest <<DTO>> {
  +name : String
  +taxId : String
  +email : String
  +address : String
  +lat : BigDecimal
  +lng : BigDecimal
  +operatingHours : String
  +emergencyContact : String
}

class UpdateSchoolRequest <<DTO>> {
  +name : String
  +address : String
  +email : String
  +lat : BigDecimal
  +lng : BigDecimal
  +operatingHours : String
  +emergencyContact : String
}

class UnlockCoordinateRequest <<DTO>> {
  +reason : String
}

class SchoolResponse <<DTO>> {
  +id : String
  +name : String
  +status : SchoolStatus
  +coordinateLocked : boolean
  +registeredAt : Instant
  +classes : int
  +staff : int
  +students : int
}

class SchoolConfigResponse <<DTO>> {
  +costCeiling : BigDecimal
  +geofenceRadius : int
  +reportEmails : List<String>
}

interface SchoolMapper <<mapper>> {
  +toResponse(entity : School) : SchoolResponse
  +toEntity(req : RegisterSchoolRequest) : School
}

interface SchoolService {
  +list(params : Map<String,String>) : PagedResponse<SchoolResponse>
  +getById(code : String) : SchoolResponse
  +register(req : RegisterSchoolRequest) : SchoolResponse
  +update(code : String, req : UpdateSchoolRequest, actor : AuthUser) : SchoolResponse
  +changeStatus(code : String, status : SchoolStatus, actor : AuthUser) : SchoolResponse
  +unlockCoordinate(code : String, reason : String, actor : AuthUser) : SchoolResponse
}

class SchoolServiceImpl {
  -schoolRepo : SchoolRepository
  -classroomPort : ClassroomSuspensionPort
  -accountPort : AccountSuspensionPort
  -auditService : AuditService
  -eventPublisher : ApplicationEventPublisher
  +list(params) : PagedResponse<SchoolResponse>
  +getById(code) : SchoolResponse
  +register(req) : SchoolResponse
  +update(code, req, actor) : SchoolResponse
  +changeStatus(code, status, actor) : SchoolResponse
  +unlockCoordinate(code, reason, actor) : SchoolResponse
  -validateCoordinate(lat, lng) : void
}

interface SchoolConfigService {
  +get(schoolCode : String) : SchoolConfigResponse
  +update(schoolCode : String, req : UpdateSchoolConfigRequest, actor : AuthUser) : SchoolConfigResponse
}

class SchoolConfigServiceImpl {
  -configRepo : SchoolConfigRepository
  -schoolRepo : SchoolRepository
  +get(schoolCode) : SchoolConfigResponse
  +update(schoolCode, req, actor) : SchoolConfigResponse
}

class SchoolController <<@RestController>> {
  -schoolService : SchoolService
  -schoolConfigService : SchoolConfigService
  +register(req : RegisterSchoolRequest) : ResponseEntity<SchoolResponse>
  +list(params) : ResponseEntity<PagedResponse<SchoolResponse>>
  +getById(code : String) : ResponseEntity<SchoolResponse>
  +update(code : String, req : UpdateSchoolRequest) : ResponseEntity<SchoolResponse>
  +changeStatus(code : String, status : SchoolStatus) : ResponseEntity<SchoolResponse>
  +unlockCoordinate(code : String, req : UnlockCoordinateRequest) : ResponseEntity<SchoolResponse>
  +getConfig(code : String) : ResponseEntity<SchoolConfigResponse>
  +updateConfig(code : String, req : UpdateSchoolConfigRequest) : ResponseEntity<SchoolConfigResponse>
}

interface SchoolRepository {
  +findByCode(code : String) : Optional<School>
  +existsByTaxId(taxId : String) : boolean
}

interface SchoolConfigRepository {
  +findBySchoolId(schoolId : UUID) : Optional<SchoolConfig>
}

SchoolService <|.. SchoolServiceImpl
SchoolConfigService <|.. SchoolConfigServiceImpl

SchoolController --> SchoolService : delegates
SchoolController --> SchoolConfigService : delegates
SchoolController ..> RegisterSchoolRequest
SchoolController ..> UpdateSchoolRequest
SchoolController ..> UnlockCoordinateRequest
SchoolController ..> SchoolResponse
SchoolController ..> SchoolConfigResponse

SchoolServiceImpl --> SchoolRepository
SchoolServiceImpl --> SchoolMapper
SchoolServiceImpl --> School : creates/updates
SchoolConfigServiceImpl --> SchoolConfigRepository
SchoolConfigServiceImpl --> SchoolConfig : reads/updates

School "1" -- "1" SchoolConfig
SchoolMapper ..> School
SchoolMapper ..> SchoolResponse
@enduml
```

**Description:**
`School` is the aggregate root of MF02; derived counters (`classes`, `staff`, `students`) are never persisted — `SchoolServiceImpl` computes them at read time (ADR-M02-2), and `SchoolResponse` merely carries the resulting numbers to the client. `SchoolConfig` is a 1:1 satellite table holding operational parameters (`geofenceRadius`, BR-028) addressed only as a child resource of `School` (`/schools/{code}/config`). The `SchoolController` exposes both resources but delegates all business logic to `SchoolService`/`SchoolConfigService` — controllers stay thin, DTO ⇄ entity conversion is centralized in `SchoolMapper` (MapStruct). GPS coordinate mutation, cascade status changes and rate limiting are pure service-layer concerns, kept out of the DTOs.

---

### 1.2 Sequence Diagram — Register School (UC-070)

```plantuml
@startuml MF02_RegisterSchool
title Sequence Diagram: Register School

' Bật chế độ liên kết vuông góc (Orthogonal lines)
skinparam linetype ortho

' ==========================================
' CẤU HÌNH MÀU SẮC TÔNG XANH LÁ
' ==========================================
skinparam backgroundColor #FFFFFF

' Định dạng chung cho Stereotype
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
' Lifeline theo stereotype robustness UML
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

    ' ==========================================
    ' Actor & Participant
    ' ==========================================
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

    ' ==========================================
    ' Message
    ' ==========================================
    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    ' ==========================================
    ' Activation (Execution)
    ' ==========================================
    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    ' ==========================================
    ' Group (alt / opt / loop / par...)
    ' ==========================================
    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial

}

actor Guest as G
boundary ":SchoolRegistrationUI" as UI
control ":SchoolController" as C
control ":SchoolServiceImpl" as S
control ":RateLimiter" as RL
entity ":SchoolRepository" as SR
database ":PostgreSQL" as DB
boundary ":EmailPort" as E

G -> UI : fill registration form (name, taxId, email,\naddress, GPS) and click "Register"
activate UI
UI -> C : POST /api/v1/schools/register\n{ name, taxId, email, address, lat, lng,\n  operatingHours, emergencyContact }
activate C
C -> S : register(RegisterSchoolRequest)
activate S

S -> RL : checkQuota(clientIp) — 5 requests / min / IP
activate RL
RL --> S : allowed : boolean
deactivate RL

alt Rate limit exceeded
  S --> C : 429 SCH-014 (MSG-141)\n"Too many requests. Please try again later."
else Under limit
  S -> S : validateTaxIdFormat(taxId) — BR-028
  alt Invalid taxId format
    S --> C : 400 SCH-007 (MSG-007)\n"Invalid tax ID format."
  else Valid format
    S -> SR : existsByTaxId(taxId)
    activate SR
    SR -> DB : SELECT count(*) FROM schools WHERE tax_id = ?
    activate DB
    DB --> SR : count
    deactivate DB
    SR --> S : exists : boolean
    deactivate SR

    alt taxId already registered
      S --> C : 409 SCH-409 (MSG-139)\n"A record with this value already exists."
    else taxId is free
      S -> S : validateCoordinate(lat, lng)\nlat ∈ [-90,90], lng ∈ [-180,180] — BR-028
      alt Coordinates out of range
        S --> C : 400 SCH-013 (MSG-107)\n"Invalid GPS coordinates."
      else Coordinates valid
        S -> S : code = generateCode() — D1, e.g. "SCH-HN-0063"
        S -> SR : save(school with status = PENDING,\ncoordinateLocked = false)
        activate SR
        SR -> DB : INSERT INTO schools (code, name, tax_id, ...,\nstatus='PENDING', coordinate_locked=false)
        activate DB
        DB --> SR : schoolId (uuid)
        deactivate DB
        SR --> S : School (id + code)
        deactivate SR

        S -> E : sendRegistrationConfirmation(email) [async]
        activate E
        E --> S : accepted (queued)
        deactivate E

        S -> S : publish SchoolRegistered event
        S --> C : 201 SchoolResponse\n{ id: code, name, status: "PENDING",\n  coordinateLocked: false, registeredAt,\n  classes: 0, staff: 0, students: 0 } MSG-032
      end
    end
  end
end
deactivate S

C --> UI : 201 Created / 400 / 409 / 429
deactivate C
UI --> G : show "Registration submitted — awaiting approval"\nor the blocking error message
deactivate UI
@enduml
```

**Description:**
Any Guest may self-register a school (UC-070); it always starts in `PENDING` status pending System Admin approval (UC-001). The endpoint is public and therefore rate-limited to 5 requests/minute/IP (SCH-014/MSG-141) to prevent abuse. Validation runs in order: rate limit → tax ID format (BR-028) → tax ID uniqueness (SCH-409) → GPS coordinate range (BR-028, SCH-013). A successful registration never sets `coordinateLocked=true` — the lock is only applied once the school transitions to `ACTIVE` (see §1.4).

---

### 1.3 Sequence Diagram — Update School Info & GPS Coordinate Lock (UC-095, BR-028)

```plantuml
@startuml MF02_UpdateSchool
title Sequence Diagram: Update School Info & GPS Coordinate Lock

' Bật chế độ liên kết vuông góc (Orthogonal lines)
skinparam linetype ortho

' ==========================================
' CẤU HÌNH MÀU SẮC TÔNG XANH LÁ
' ==========================================
skinparam backgroundColor #FFFFFF

' Định dạng chung cho Stereotype
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
' Lifeline theo stereotype robustness UML
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

    ' ==========================================
    ' Actor & Participant
    ' ==========================================
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

    ' ==========================================
    ' Message
    ' ==========================================
    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    ' ==========================================
    ' Activation (Execution)
    ' ==========================================
    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    ' ==========================================
    ' Group (alt / opt / loop / par...)
    ' ==========================================
    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial

}

actor "System Admin" as SA
boundary ":SchoolProfileUI" as UI
control ":SchoolController" as C
control ":SchoolServiceImpl" as S
entity ":SchoolRepository" as SR
database ":PostgreSQL" as DB
control ":AuditService" as A

SA -> UI : edit school profile fields and click "Save"
activate UI
UI -> C : PATCH /api/v1/schools/{code}\nAuthorization: Bearer <JWT>\n{ name, address, email, lat, lng, ... }
activate C
C -> S : update(code, UpdateSchoolRequest, actor)
activate S

S -> SR : findByCode(code)
activate SR
SR -> DB : SELECT * FROM schools WHERE code = ?
activate DB
DB --> SR : schoolRow
deactivate DB
SR --> S : Optional<School>
deactivate SR

alt School not found
  S --> C : 404 SCH-404 (MSG-137)
else School found
  S -> S : isCoordinateChanged(payload.lat, payload.lng)
  alt Coordinates changed AND coordinateLocked == true
    S --> C : 403 SCH-013 (MSG-108)\n"School coordinates are locked.\nOnly a System Administrator can change them."
    note right of S: BR-028 — use POST /schools/{code}/coordinate/unlock first
  else Allowed to proceed
    opt Coordinates changed
      S -> S : validateCoordinate(lat, lng)\nlat ∈ [-90,90], lng ∈ [-180,180]
      alt Out of range
        S --> C : 400 SCH-013 (MSG-107)
      else Valid
        S -> SR : save(school with lat, lng,\ncoordinateLocked = true)
        activate SR
        SR -> DB : UPDATE schools SET lat = ?, lng = ?,\ncoordinate_locked = true WHERE id = ?
        activate DB
        DB --> SR : 1 row updated
        deactivate DB
        SR --> S : School (coordinateLocked = true)
        deactivate SR
        note right of S: BR-028 — re-lock immediately after edit
      end
    end

    S -> SR : save(school with name, address, email,\noperatingHours, emergencyContact)
    activate SR
    SR -> DB : UPDATE schools SET name = ?, address = ?,\nemail = ?, operating_hours = ?, emergency_contact = ?
    activate DB
    DB --> SR : 1 row updated
    deactivate DB
    SR --> S : School (updated)
    deactivate SR

    S -> A : audit(actor, UPDATE_SCHOOL, SUCCESS)
    activate A
    A --> S : auditId
    deactivate A

    S --> C : 200 SchoolResponse { ...updatedSchool } MSG-029
  end
end
deactivate S

C --> UI : 200 / 400 / 403 / 404 response
deactivate C
UI --> SA : show updated profile or blocking error message
deactivate UI

== Unlock Coordinate (System Admin only, BR-028) ==

SA -> UI : click "Unlock GPS coordinates" and enter reason
activate UI
UI -> C : POST /api/v1/schools/{code}/coordinate/unlock\n{ reason: "GPS re-survey requested by principal" }
activate C
C -> S : unlockCoordinate(code, reason, actor)
activate S

S -> S : @PreAuthorize("hasRole('SYSTEM_ADMIN')")

S -> SR : findByCode(code)
activate SR
SR -> DB : SELECT * FROM schools WHERE code = ?
activate DB
DB --> SR : schoolRow
deactivate DB
SR --> S : Optional<School>
deactivate SR

S -> SR : save(school with coordinateLocked = false)
activate SR
SR -> DB : UPDATE schools SET coordinate_locked = false\nWHERE id = ?
activate DB
DB --> SR : 1 row updated
deactivate DB
SR --> S : School (coordinateLocked = false)
deactivate SR

S -> A : audit(actor, UNLOCK_COORDINATE, SUCCESS, details = reason)
activate A
A --> S : auditId
deactivate A

S -> S : publish SchoolCoordinateUnlocked event
S --> C : 200 SchoolResponse { ...school,\n  coordinateLocked: false }
deactivate S
C --> UI : 200 OK
deactivate C
UI --> SA : show "GPS coordinates unlocked — you may edit them now"
deactivate UI
@enduml
```

**Description:**
`UpdateSchool` (UC-095) allows editing any profile field, but a GPS coordinate change is only accepted while `coordinateLocked=false`; once a school is `ACTIVE`, the coordinates are locked automatically. Any change that does slip through re-locks the record immediately (`coordinate_locked = true`) so the lock is self-healing. To edit coordinates on a locked school, a System Admin must call the dedicated unlock endpoint with a written `reason` (BR-028) — every unlock is audited and emits `SchoolCoordinateUnlocked` so the change is traceable.

---

### 1.4 Sequence Diagram — Deactivate School Cascade (UC-001, BR-021)

```plantuml
@startuml MF02_DeactivateSchool
title Sequence Diagram: Deactivate School Cascade

' Bật chế độ liên kết vuông góc (Orthogonal lines)
skinparam linetype ortho

' ==========================================
' CẤU HÌNH MÀU SẮC TÔNG XANH LÁ
' ==========================================
skinparam backgroundColor #FFFFFF

' Định dạng chung cho Stereotype
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
' Lifeline theo stereotype robustness UML
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

    ' ==========================================
    ' Actor & Participant
    ' ==========================================
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

    ' ==========================================
    ' Message
    ' ==========================================
    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    ' ==========================================
    ' Activation (Execution)
    ' ==========================================
    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    ' ==========================================
    ' Group (alt / opt / loop / par...)
    ' ==========================================
    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial

}

actor "System Admin" as SA
boundary ":SchoolAdminUI" as UI
control ":SchoolController" as C
control ":SchoolServiceImpl" as S
entity ":SchoolRepository" as SR
boundary ":ClassroomSuspensionPort" as CP
boundary ":AccountSuspensionPort" as AP
database ":PostgreSQL" as DB
control ":AuditService" as A

SA -> UI : select school and confirm "Deactivate"
activate UI
UI -> C : PATCH /api/v1/schools/{code}/status\nAuthorization: Bearer <JWT>\n{ status: "INACTIVE" }
activate C
C -> S : changeStatus(code, INACTIVE, actor)
activate S

S -> SR : findByCode(code)
activate SR
SR -> DB : SELECT * FROM schools WHERE code = ?
activate DB
DB --> SR : schoolRow
deactivate DB
SR --> S : Optional<School>
deactivate SR

alt School not found
  S --> C : 404 SCH-404 (MSG-137)
else School found
  S -> SR : save(school with status = INACTIVE)
  activate SR
  SR -> DB : UPDATE schools SET status = 'INACTIVE' WHERE id = ?
  activate DB
  DB --> SR : 1 row updated
  deactivate DB
  SR --> S : School (status = INACTIVE)
  deactivate SR

  note right of S: BR-021 — cascade, no data is deleted, only status changes

  S -> CP : suspendAllClassrooms(schoolId)\n[by UUID, no entity import]
  activate CP
  CP -> DB : UPDATE classrooms SET read_only = true\nWHERE school_id = ?
  activate DB
  DB --> CP : n rows updated
  deactivate DB
  CP --> S : suspendedClassrooms : int
  deactivate CP

  S -> AP : suspendStaffAndStudentAccounts(schoolId)\n[via port, MF01/MF03]
  activate AP
  AP -> DB : UPDATE user_accounts SET status = 'SUSPENDED'\nWHERE school_id = ?
  activate DB
  DB --> AP : n rows updated
  deactivate DB
  AP --> S : suspendedAccounts : int
  deactivate AP

  S -> A : audit(actor, DEACTIVATE_SCHOOL, SUCCESS)
  activate A
  A --> S : auditId
  deactivate A

  S -> S : publish SchoolDeactivated event\n(consumed async by MF01, MF03, Audit)
  S --> C : 200 SchoolResponse { ...school,\n  status: "INACTIVE" } MSG-001
end
deactivate S

C --> UI : 200 / 404 response
deactivate C
UI --> SA : show "School deactivated" summary\n(classrooms read-only, accounts suspended)
deactivate UI
@enduml
```

**Description:**
Deactivating a school (System Admin only) triggers a cascade defined by BR-021: every classroom of that school becomes read-only and every staff/student account tied to the school is suspended. Cross-module effects are performed through ports (`ClassroomSuspensionPort`, `AccountSuspensionPort`) referencing entities by UUID only — MF02 never imports MF01/MF03 JPA entities directly (AP-AI-005). No row is ever deleted; the cascade is purely a status transition, fully reversible by reactivating the school (`status: "ACTIVE"`).

---

## 2. School Policy (Read-Only, Cross-Role)

### 2.1 Class Diagram

```plantuml
@startuml MF02_Policy_ClassDiagram
title Design-level Class Diagram: School Policy (Read-Only, Cross-Role)

' ==========================================
' CẤU HÌNH BỐ CỤC VÀ MÀU SẮC RATIONAL ROSE
' ==========================================
skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF

' Định dạng cho Class / Interface / Abstract Class
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

' Định dạng Mũi tên & Liên kết (Relationships & Arrows)
skinparam arrow {
    Color #162c00ff
    FontColor #162c00ff
    FontName Arial
    FontSize 11
}

' Định dạng Icon Stereotype (C, I, A, E)
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

class SchoolPolicyResponse <<DTO>> {
  +schoolCode : String
  +schoolName : String
  +operatingHours : String
  +geofenceRadius : int
  +costCeiling : BigDecimal
  +mealsPerDay : int
}

interface SchoolPolicyService {
  +getForCurrentUser(actor : AuthUser) : SchoolPolicyResponse
}

class SchoolPolicyServiceImpl {
  -schoolRepo : SchoolRepository
  -configRepo : SchoolConfigRepository
  +getForCurrentUser(actor) : SchoolPolicyResponse
}

class SchoolPolicyController <<@RestController>> {
  -policyService : SchoolPolicyService
  +getMyPolicy() : ResponseEntity<SchoolPolicyResponse>
}

SchoolPolicyService <|.. SchoolPolicyServiceImpl
SchoolPolicyController --> SchoolPolicyService : delegates
SchoolPolicyController ..> SchoolPolicyResponse
SchoolPolicyServiceImpl --> School : reads (school_id from JWT only)
SchoolPolicyServiceImpl --> SchoolConfig : reads
@enduml
```

**Description:**
`GET /schools/me/policy` (UC-137) is available to Teacher, Medical Staff and Service Caterers. `SchoolPolicyServiceImpl` never accepts a school identifier from the client — `schoolId` is read exclusively from the JWT claims (BR-022), which structurally rules out cross-school access rather than relying on a runtime check alone.

---

### 2.2 Sequence Diagram — View School Policy (UC-137, BR-022)

```plantuml
@startuml MF02_ViewPolicy
title Sequence Diagram: View School Policy

' Bật chế độ liên kết vuông góc (Orthogonal lines)
skinparam linetype ortho

' ==========================================
' CẤU HÌNH MÀU SẮC TÔNG XANH LÁ
' ==========================================
skinparam backgroundColor #FFFFFF

' Định dạng chung cho Stereotype
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
' Lifeline theo stereotype robustness UML
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

    ' ==========================================
    ' Actor & Participant
    ' ==========================================
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

    ' ==========================================
    ' Message
    ' ==========================================
    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    ' ==========================================
    ' Activation (Execution)
    ' ==========================================
    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    ' ==========================================
    ' Group (alt / opt / loop / par...)
    ' ==========================================
    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial

}

actor "Teacher / Medical Staff / Caterer" as U
boundary ":SchoolPolicyUI" as UI
control ":SchoolPolicyController" as C
control ":SchoolPolicyServiceImpl" as S
entity ":SchoolRepository" as SR
entity ":SchoolConfigRepository" as CR
database ":PostgreSQL" as DB

U -> UI : open "School policy" screen
activate UI
UI -> C : GET /api/v1/schools/me/policy\nAuthorization: Bearer <JWT>
activate C
C -> S : getForCurrentUser(actor)
activate S

S -> S : schoolId = actor.schoolId\n' from JWT claims only — BR-022

S -> SR : findById(schoolId)
activate SR
SR -> DB : SELECT * FROM schools WHERE id = ?
activate DB
DB --> SR : schoolRow
deactivate DB
SR --> S : Optional<School>
deactivate SR

alt School not found / not ACTIVE
  S --> C : 404 SCH-404 (MSG-137)
else School found
  S -> CR : findBySchoolId(schoolId)
  activate CR
  CR -> DB : SELECT * FROM school_config WHERE school_id = ?
  activate DB
  DB --> CR : configRow
  deactivate DB
  CR --> S : Optional<SchoolConfig>
  deactivate CR

  S --> C : 200 SchoolPolicyResponse\n{ schoolCode, schoolName, operatingHours,\n  geofenceRadius, costCeiling, mealsPerDay } MSG-004
end
deactivate S

C --> UI : 200 / 404 response
deactivate C
UI --> U : render read-only policy card
deactivate UI
@enduml
```

**Description:**
The `schoolId` used for the lookup always comes from the caller's JWT, never from a request parameter — a caller cannot request another school's policy even by guessing an ID, since no ID parameter exists on this endpoint. This satisfies BR-022's cross-school isolation requirement by construction.

---

## 3. Classroom Management

### 3.1 Class Diagram

```plantuml
@startuml MF02_Classroom_ClassDiagram
title Design-level Class Diagram: Classroom Management

' ==========================================
' CẤU HÌNH BỐ CỤC VÀ MÀU SẮC RATIONAL ROSE
' ==========================================
skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF

' Định dạng cho Class / Interface / Abstract Class
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

' Định dạng Mũi tên & Liên kết (Relationships & Arrows)
skinparam arrow {
    Color #162c00ff
    FontColor #162c00ff
    FontName Arial
    FontSize 11
}

' Định dạng Icon Stereotype (C, I, A, E)
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

class Classroom {
  +id : UUID <<PK>>
  +code : String <<unique per year>>
  +schoolId : UUID <<FK>>
  +academicYearId : UUID <<FK>>
  +name : String
  +grade : int
  +capacity : int
  +homeroomTeacherId : UUID
  +readOnly : boolean
  +createdAt : Instant
  +hasCapacityFor(count : int) : boolean
}

class CreateClassroomRequest <<DTO>> {
  +name : String
  +grade : int
  +capacity : int
  +academicYearId : String
}

class UpdateClassroomRequest <<DTO>> {
  +name : String
  +grade : int
  +capacity : int
}

class AssignHomeroomTeacherRequest <<DTO>> {
  +teacherId : String
}

class ClassroomResponse <<DTO>> {
  +id : String
  +name : String
  +grade : int
  +capacity : int
  +academicYearId : String
  +teacher : String
  +teacherId : String
  +students : int
  +allergies : int
}

class ClassroomDetailResponse <<DTO>> {
  +id : String
  +name : String
  +grade : int
  +capacity : int
  +readOnly : boolean
  +teacher : String
  +students : int
  +allergies : int
}

interface TeacherRef <<external>> {
  +id : UUID
  +code : String
  +staffCode : String
  +role : String
  +status : String
  +schoolId : UUID
}

interface ClassroomService {
  +list(schoolId : UUID, params : Map<String,String>) : PagedResponse<ClassroomResponse>
  +getDetail(code : String, schoolId : UUID) : ClassroomDetailResponse
  +create(req : CreateClassroomRequest, actor : AuthUser) : ClassroomResponse
  +update(code : String, req : UpdateClassroomRequest, actor : AuthUser) : ClassroomResponse
  +assignHomeroomTeacher(code : String, teacherId : String, actor : AuthUser) : ClassroomResponse
}

class ClassroomServiceImpl {
  -classroomRepo : ClassroomRepository
  -academicYearRepo : AcademicYearRepository
  -referenceResolver : ReferenceResolver
  -auditService : AuditService
  +list(schoolId, params) : PagedResponse<ClassroomResponse>
  +getDetail(code, schoolId) : ClassroomDetailResponse
  +create(req, actor) : ClassroomResponse
  +update(code, req, actor) : ClassroomResponse
  +assignHomeroomTeacher(code, teacherId, actor) : ClassroomResponse
  -validateGradeAndCapacity(grade, capacity) : void
}

class ReferenceResolver {
  +teacherIdByCode(code : String) : Optional<TeacherRef>
}

class ClassroomController <<@RestController>> {
  -classroomService : ClassroomService
  +list(params) : ResponseEntity<PagedResponse<ClassroomResponse>>
  +getDetail(code : String) : ResponseEntity<ClassroomDetailResponse>
  +create(req : CreateClassroomRequest) : ResponseEntity<ClassroomResponse>
  +update(code : String, req : UpdateClassroomRequest) : ResponseEntity<ClassroomResponse>
  +assignHomeroomTeacher(code : String, req : AssignHomeroomTeacherRequest) : ResponseEntity<ClassroomResponse>
}

interface ClassroomRepository {
  +countByAcademicYearId(academicYearId : UUID) : long
  +existsByAcademicYearIdAndHomeroomTeacherId(ayId : UUID, teacherId : UUID) : boolean
  +existsByAcademicYearIdAndNameIgnoreCase(ayId : UUID, name : String) : boolean
  +findByCode(code : String) : Optional<Classroom>
  +findByCodeAndSchoolId(code : String, schoolId : UUID) : Optional<Classroom>
  +findBySchoolIdAndAcademicYearId(schoolId : UUID, ayId : UUID) : List<Classroom>
  +markReadOnlyByAcademicYearId(ayId : UUID) : int
}

ClassroomService <|.. ClassroomServiceImpl
ClassroomController --> ClassroomService : delegates
ClassroomController ..> CreateClassroomRequest
ClassroomController ..> UpdateClassroomRequest
ClassroomController ..> AssignHomeroomTeacherRequest
ClassroomController ..> ClassroomResponse
ClassroomController ..> ClassroomDetailResponse

ClassroomServiceImpl --> ClassroomRepository
ClassroomServiceImpl --> ReferenceResolver : resolves teacher by code/staffCode\n(no MF01 entity import — ADR-M02-5)
ClassroomServiceImpl --> Classroom : creates/updates
ReferenceResolver ..> TeacherRef : native query result

Classroom "n" ..> "1" TeacherRef : homeroomTeacherId (ID only)
@enduml
```

**Description:**
`ClassroomServiceImpl` never depends on a MF01 JPA entity or repository to resolve a homeroom teacher: `ReferenceResolver.teacherIdByCode()` runs a native SQL query against `user_accounts` and returns a lightweight `TeacherRef` projection (id, role, status, schoolId), which is validated in-service (role = Teacher, status = ACTIVE, same school) before the UUID is stored on `Classroom.homeroomTeacherId` (ADR-M02-5). This keeps the module boundary intact while still allowing a real foreign key in the schema, since both tables live in the same physical database. `CreateClassroomRequest`/`UpdateClassroomRequest` intentionally omit `schoolId` — it is always taken from the authenticated actor, never trusted from the request body.

---

### 3.2 Sequence Diagram — Create Classroom (UC-018, BR-024/025/192)

```plantuml
@startuml MF02_CreateClassroom
title Sequence Diagram: Create Classroom

' Bật chế độ liên kết vuông góc (Orthogonal lines)
skinparam linetype ortho

' ==========================================
' CẤU HÌNH MÀU SẮC TÔNG XANH LÁ
' ==========================================
skinparam backgroundColor #FFFFFF

' Định dạng chung cho Stereotype
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
' Lifeline theo stereotype robustness UML
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

    ' ==========================================
    ' Actor & Participant
    ' ==========================================
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

    ' ==========================================
    ' Message
    ' ==========================================
    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    ' ==========================================
    ' Activation (Execution)
    ' ==========================================
    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    ' ==========================================
    ' Group (alt / opt / loop / par...)
    ' ==========================================
    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial

}

actor "School Administration" as SA
boundary ":ClassroomManagementUI" as UI
control ":ClassroomController" as C
control ":ClassroomServiceImpl" as S
entity ":AcademicYearRepository" as AYR
entity ":ClassroomRepository" as CR
database ":PostgreSQL" as DB
control ":AuditService" as A

SA -> UI : fill classroom form (name, grade, capacity,\nacademic year) and click "Create"
activate UI
UI -> C : POST /api/v1/classrooms\nAuthorization: Bearer <JWT>\n{ name, grade, capacity, academicYearId }
activate C
C -> S : create(CreateClassroomRequest, actor)
activate S

S -> S : validateGradeAndCapacity(grade, capacity)\ngrade ∈ [1,12] — BR-024
alt Grade out of range
  S --> C : 422 SCH-024 (MSG-109)\n"Grade level must be between 1 and 12."
else Grade valid
  alt Capacity exceeds 50 — BR-025
    S --> C : 409 SCH-010 (MSG-010)\n"Classroom capacity exceeded.\nMaximum allowed is 50 students."
  else Capacity valid
    S -> AYR : findByCodeAndSchoolId(academicYearId, actor.schoolId)
    activate AYR
    AYR -> DB : SELECT * FROM academic_years\nWHERE code = ? AND school_id = ?
    activate DB
    DB --> AYR : academicYearRow
    deactivate DB
    AYR --> S : Optional<AcademicYear>
    deactivate AYR

    alt Academic year not found / not ACTIVE
      S --> C : 409 SCH-081 (MSG-081)\n"Cannot create classroom: this school has no\nActive academic year." — BR-192
    else Academic year is ACTIVE
      S -> CR : existsByAcademicYearIdAndNameIgnoreCase(ayId, name)
      activate CR
      CR -> DB : SELECT count(*) FROM classrooms\nWHERE academic_year_id = ? AND lower(name) = lower(?)
      activate DB
      DB --> CR : count
      deactivate DB
      CR --> S : exists : boolean
      deactivate CR

      alt Name already used in this academic year
        S --> C : 409 SCH-409 (MSG-139)
      else Name is free
        S -> S : code = generateCode(name) — D1
        S -> CR : save(classroom with readOnly = false,\nhomeroomTeacherId = null)
        activate CR
        CR -> DB : INSERT INTO classrooms (code, school_id,\nacademic_year_id, name, grade, capacity, read_only)
        activate DB
        DB --> CR : classroomId (uuid)
        deactivate DB
        CR --> S : Classroom (id + code)
        deactivate CR

        S -> A : audit(actor, CREATE_CLASSROOM, SUCCESS)
        activate A
        A --> S : auditId
        deactivate A

        S --> C : 201 ClassroomResponse\n{ id: code, name, grade, capacity,\n  academicYearId, teacher: null,\n  students: 0, allergies: 0 } MSG-033
      end
    end
  end
end
deactivate S

C --> UI : 201 Created / 409 / 422 response
deactivate C
UI --> SA : show new classroom in the list\nor the blocking error message
deactivate UI
@enduml
```

**Description:**
Validation runs strictly in the order specified by traceability: grade range (BR-024) → capacity ceiling (BR-025) → the parent academic year must exist and be `ACTIVE` for this school (BR-192, SCH-081) → classroom name uniqueness within that academic year. A new classroom always starts with `readOnly=false` and no assigned teacher; `students`/`allergies` are always `0` at creation since they are derived counters (ADR-M02-2), never seeded values.

---

### 3.3 Sequence Diagram — Assign Homeroom Teacher (UC-008, BR-020/026)

```plantuml
@startuml MF02_AssignHomeroomTeacher
title Sequence Diagram: Assign Homeroom Teacher

' Bật chế độ liên kết vuông góc (Orthogonal lines)
skinparam linetype ortho

' ==========================================
' CẤU HÌNH MÀU SẮC TÔNG XANH LÁ
' ==========================================
skinparam backgroundColor #FFFFFF

' Định dạng chung cho Stereotype
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
' Lifeline theo stereotype robustness UML
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

    ' ==========================================
    ' Actor & Participant
    ' ==========================================
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

    ' ==========================================
    ' Message
    ' ==========================================
    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    ' ==========================================
    ' Activation (Execution)
    ' ==========================================
    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    ' ==========================================
    ' Group (alt / opt / loop / par...)
    ' ==========================================
    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial

}

actor "School Administration" as SA
boundary ":ClassroomDetailUI" as UI
control ":ClassroomController" as C
control ":ClassroomServiceImpl" as S
control ":ReferenceResolver" as R
entity ":ClassroomRepository" as CR
database ":PostgreSQL" as DB
control ":AuditService" as A

SA -> UI : open classroom detail, pick a teacher\nand click "Assign homeroom teacher"
activate UI
UI -> C : PATCH /api/v1/classrooms/{code}/homeroom-teacher\nAuthorization: Bearer <JWT>\n{ teacherId: "STF-003" }
activate C
C -> S : assignHomeroomTeacher(code, "STF-003", actor)
activate S

S -> CR : findByCodeAndSchoolId(code, actor.schoolId)
activate CR
CR -> DB : SELECT * FROM classrooms\nWHERE code = ? AND school_id = ?
activate DB
DB --> CR : classroomRow
deactivate DB
CR --> S : Optional<Classroom>
deactivate CR

alt Classroom not found / cross-school
  S --> C : 404 SCH-404 (MSG-137)
else Classroom found
  alt Classroom is read-only (year CLOSED) — BR-193
    S --> C : 409 SCH-193 (MSG-010)\n"Classroom is read-only for this academic year."
  else Classroom is editable
    S -> R : teacherIdByCode("STF-003")\n(native query: code OR staff_code — ADR-M02-5)
    activate R
    R -> DB : SELECT id, code, staff_code, role, status, school_id\nFROM user_accounts WHERE code = ? OR staff_code = ?
    activate DB
    DB --> R : teacherRow
    deactivate DB
    R --> S : Optional<TeacherRef>
    deactivate R

    alt Not found / role != Teacher / status != ACTIVE / different school
      S --> C : 404 SCH-404 (MSG-137)\n"Resource not found."
    else Valid Teacher in same school
      S -> CR : existsByAcademicYearIdAndHomeroomTeacherId(ayId, teacherUuid)
      activate CR
      CR -> DB : SELECT count(*) FROM classrooms\nWHERE academic_year_id = ? AND homeroom_teacher_id = ?
      activate DB
      DB --> CR : count
      deactivate DB
      CR --> S : exists : boolean
      deactivate CR

      alt Teacher already homerooms another classroom this year
        S --> C : 409 SCH-011 (MSG-011)\n"Homeroom teacher is already assigned\nto another classroom."
      else Teacher is free
        S -> CR : save(classroom with\nhomeroomTeacherId = teacherUuid)
        activate CR
        CR -> DB : UPDATE classrooms SET homeroom_teacher_id = ?\nWHERE code = ?\n— guarded by uq_homeroom_teacher_per_year
        activate DB
        DB --> CR : 1 row updated
        deactivate DB
        CR --> S : Classroom (homeroomTeacherId set)
        deactivate CR

        note right of S: BR-020 — previous homeroom link on THIS\nclassroom is atomically replaced

        S -> A : audit(actor, ASSIGN_HOMEROOM_TEACHER, SUCCESS) — BR-026
        activate A
        A --> S : auditId
        deactivate A

        S --> C : 200 ClassroomResponse\n{ id: code, name, teacher: "Đào Văn Quân",\n  teacherId: "STF-003" } MSG-034
      end
    end
  end
end
deactivate S

C --> UI : 200 / 404 / 409 response
deactivate C
UI --> SA : show assigned teacher on the classroom card\nor the blocking error message
deactivate UI
@enduml
```

**Description:**
`teacherId` in the request accepts either the platform `code` (`U-0010`) or the domain-specific `staffCode` (`STF-003`) — both resolve to the same `user_accounts` row via `ReferenceResolver` (ADR-M02-5), never through a direct MF01 entity import. Assignment is guarded by the partial unique index `uq_homeroom_teacher_per_year`, so a teacher can never end up homerooming two classrooms in the same academic year (BR-020); reassigning a classroom's teacher simply overwrites the FK on that one row, and every assignment is written to the audit trail with actor and timestamp (BR-026).

---

## 4. Academic Year Management

### 4.1 Class Diagram

```plantuml
@startuml MF02_AcademicYear_ClassDiagram
title Design-level Class Diagram: Academic Year Management

' ==========================================
' CẤU HÌNH BỐ CỤC VÀ MÀU SẮC RATIONAL ROSE
' ==========================================
skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF

' Định dạng cho Class / Interface / Abstract Class
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

' Định dạng Mũi tên & Liên kết (Relationships & Arrows)
skinparam arrow {
    Color #162c00ff
    FontColor #162c00ff
    FontName Arial
    FontSize 11
}

' Định dạng Icon Stereotype (C, I, A, E)
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

enum AcademicYearStatus {
  DRAFT
  ACTIVE
  CLOSED
  ARCHIVED
}

class AcademicYear {
  +id : UUID <<PK>>
  +code : String <<unique per school>>
  +schoolId : UUID <<FK>>
  +name : String
  +startDate : LocalDate
  +endDate : LocalDate
  +status : AcademicYearStatus
  +copyStructure : boolean
  +createdAt : Instant
  +isActive() : boolean
}

class CreateAcademicYearRequest <<DTO>> {
  +name : String
  +startDate : LocalDate
  +endDate : LocalDate
  +copyStructure : boolean
}

class AcademicYearResponse <<DTO>> {
  +id : String
  +name : String
  +status : AcademicYearStatus
  +startDate : LocalDate
  +endDate : LocalDate
  +copyStructure : boolean
  +classes : int
  +students : int
}

class PromoteStudentsRequest <<DTO>> {
  +sourceAcademicYearId : String
  +mappings : List<ClassroomMapping>
  +excludeStudentIds : List<String>
}

class ClassroomMapping <<DTO>> {
  +sourceClassroomId : String
  +targetClassroomId : String
}

class PromotionResult <<DTO>> {
  +promoted : int
  +excluded : int
  +targetAcademicYearId : String
}

interface AcademicYearService {
  +list(schoolId : UUID, params : Map<String,String>) : PagedResponse<AcademicYearResponse>
  +create(req : CreateAcademicYearRequest, actor : AuthUser) : AcademicYearResponse
  +activate(code : String, actor : AuthUser) : AcademicYearResponse
  +close(code : String, actor : AuthUser) : AcademicYearResponse
  +promoteStudents(code : String, req : PromoteStudentsRequest, actor : AuthUser) : PromotionResult
}

class AcademicYearServiceImpl {
  -academicYearRepo : AcademicYearRepository
  -classroomRepo : ClassroomRepository
  -studentPromotionPort : StudentPromotionPort
  -auditService : AuditService
  +list(schoolId, params) : PagedResponse<AcademicYearResponse>
  +create(req, actor) : AcademicYearResponse
  +activate(code, actor) : AcademicYearResponse
  +close(code, actor) : AcademicYearResponse
  +promoteStudents(code, req, actor) : PromotionResult
  -copyClassroomStructure(sourceYearId, targetYearId) : void
}

class AcademicYearController <<@RestController>> {
  -academicYearService : AcademicYearService
  +list(params) : ResponseEntity<PagedResponse<AcademicYearResponse>>
  +create(req : CreateAcademicYearRequest) : ResponseEntity<AcademicYearResponse>
  +activate(code : String) : ResponseEntity<AcademicYearResponse>
  +close(code : String) : ResponseEntity<AcademicYearResponse>
  +promote(code : String, req : PromoteStudentsRequest) : ResponseEntity<PromotionResult>
}

interface AcademicYearRepository {
  +findByCodeAndSchoolId(code : String, schoolId : UUID) : Optional<AcademicYear>
  +findBySchoolIdAndStatus(schoolId : UUID, status : AcademicYearStatus) : List<AcademicYear>
  +existsBySchoolIdAndName(schoolId : UUID, name : String) : boolean
}

interface StudentPromotionPort <<external>> {
  +promote(mappings : List<ClassroomMapping>, excludeIds : List<String>) : int
}

AcademicYearService <|.. AcademicYearServiceImpl
AcademicYearController --> AcademicYearService : delegates
AcademicYearController ..> CreateAcademicYearRequest
AcademicYearController ..> PromoteStudentsRequest
AcademicYearController ..> AcademicYearResponse
AcademicYearController ..> PromotionResult

AcademicYearServiceImpl --> AcademicYearRepository
AcademicYearServiceImpl --> ClassroomRepository : copies structure / sets read-only
AcademicYearServiceImpl --> StudentPromotionPort : delegates enrollment to MF03 (by ID)
AcademicYearServiceImpl --> AcademicYear : creates/updates

AcademicYear "1" *-- "n" Classroom
PromoteStudentsRequest --> ClassroomMapping
@enduml
```

**Description:**
`AcademicYearServiceImpl.activate()` and `close()` both run inside a transaction with `PESSIMISTIC_WRITE` locking on the school's academic-year rows to prevent two concurrent activation requests from both passing the "no other ACTIVE year" check (BR-191) — the DB-level partial unique index `uq_ay_one_active_per_school` is the final backstop. Promoting students (UC-177, BR-194) is delegated to `StudentPromotionPort`, an ID-only interface into MF03 — MF02 never touches MF03's `Student` entity directly, keeping the module boundary intact even though promotion is initiated from this module.

---

### 4.2 Sequence Diagram — Activate Academic Year (UC-175, BR-191)

```plantuml
@startuml MF02_ActivateAcademicYear
title Sequence Diagram: Activate Academic Year

' Bật chế độ liên kết vuông góc (Orthogonal lines)
skinparam linetype ortho

' ==========================================
' CẤU HÌNH MÀU SẮC TÔNG XANH LÁ
' ==========================================
skinparam backgroundColor #FFFFFF

' Định dạng chung cho Stereotype
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
' Lifeline theo stereotype robustness UML
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

    ' ==========================================
    ' Actor & Participant
    ' ==========================================
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

    ' ==========================================
    ' Message
    ' ==========================================
    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    ' ==========================================
    ' Activation (Execution)
    ' ==========================================
    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    ' ==========================================
    ' Group (alt / opt / loop / par...)
    ' ==========================================
    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial

}

actor "School Administration" as SA
boundary ":AcademicYearUI" as UI
control ":AcademicYearController" as C
control ":AcademicYearServiceImpl" as S
entity ":AcademicYearRepository" as AYR
database ":PostgreSQL" as DB
control ":AuditService" as A

SA -> UI : select a DRAFT academic year\nand click "Activate"
activate UI
UI -> C : POST /api/v1/academic-years/{code}/activate\nAuthorization: Bearer <JWT>
activate C
C -> S : activate(code, actor)
activate S

note right of S: @Transactional — cả 2 truy vấn dưới đây\nchạy trong cùng một transaction (PESSIMISTIC_WRITE)

S -> AYR : findByCodeAndSchoolId(code, actor.schoolId)
activate AYR
AYR -> DB : SELECT * FROM academic_years\nWHERE code = ? AND school_id = ? FOR UPDATE
activate DB
DB --> AYR : academicYearRow (locked)
deactivate DB
AYR --> S : Optional<AcademicYear>
deactivate AYR

alt Not found / cross-school
  S --> C : 404 SCH-404 (MSG-137)
else Found (status = DRAFT)
  S -> AYR : findBySchoolIdAndStatus(actor.schoolId, ACTIVE)
  activate AYR
  AYR -> DB : SELECT * FROM academic_years\nWHERE school_id = ? AND status = 'ACTIVE' FOR UPDATE
  activate DB
  DB --> AYR : rows[] (locked)
  deactivate DB
  AYR --> S : List<AcademicYear>
  deactivate AYR

  alt Another year is already ACTIVE
    S --> C : 409 SCH-080 (MSG-080)\n"Cannot create Active academic year: an Active\nacademic year already exists. Please close it first."
  else No ACTIVE year exists
    S -> AYR : save(academicYear with status = ACTIVE)
    activate AYR
    AYR -> DB : UPDATE academic_years SET status = 'ACTIVE'\nWHERE id = ?\n— protected by uq_ay_one_active_per_school
    activate DB
    DB --> AYR : 1 row updated
    deactivate DB
    AYR --> S : AcademicYear (status = ACTIVE)
    deactivate AYR

    S -> A : audit(actor, ACTIVATE_ACADEMIC_YEAR, SUCCESS)
    activate A
    A --> S : auditId
    deactivate A

    S -> S : publish AcademicYearActivated event\n(consumed by MF13 — report period switch)
    S --> C : 200 AcademicYearResponse\n{ id: code, name, status: "ACTIVE" } MSG-083
  end
end
deactivate S

C --> UI : 200 / 404 / 409 response
deactivate C
UI --> SA : show the year badge as "Active"\nor the blocking error message
deactivate UI
@enduml
```

**Description:**
Both the existence lookup and the "any ACTIVE year" check use `SELECT ... FOR UPDATE`, and the entire method runs inside a single `@Transactional` boundary — this closes the race window where two simultaneous activate calls could both read "no ACTIVE year" and both proceed. Even if the row-level lock were bypassed, the DB-level partial unique index `uq_ay_one_active_per_school` guarantees BR-191 holds at the storage layer as the final invariant.

---

### 4.3 Sequence Diagram — Close Academic Year Cascade (UC-175, BR-193)

```plantuml
@startuml MF02_CloseAcademicYear
title Sequence Diagram: Close Academic Year Cascade

' Bật chế độ liên kết vuông góc (Orthogonal lines)
skinparam linetype ortho

' ==========================================
' CẤU HÌNH MÀU SẮC TÔNG XANH LÁ
' ==========================================
skinparam backgroundColor #FFFFFF

' Định dạng chung cho Stereotype
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
' Lifeline theo stereotype robustness UML
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

    ' ==========================================
    ' Actor & Participant
    ' ==========================================
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

    ' ==========================================
    ' Message
    ' ==========================================
    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    ' ==========================================
    ' Activation (Execution)
    ' ==========================================
    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    ' ==========================================
    ' Group (alt / opt / loop / par...)
    ' ==========================================
    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial

}

actor "School Administration" as SA
boundary ":AcademicYearUI" as UI
control ":AcademicYearController" as C
control ":AcademicYearServiceImpl" as S
entity ":AcademicYearRepository" as AYR
entity ":ClassroomRepository" as CR
database ":PostgreSQL" as DB
control ":AuditService" as A

SA -> UI : select the ACTIVE academic year\nand confirm "Close year"
activate UI
UI -> C : POST /api/v1/academic-years/{code}/close\nAuthorization: Bearer <JWT>
activate C
C -> S : close(code, actor)
activate S

S -> AYR : findByCodeAndSchoolId(code, actor.schoolId)
activate AYR
AYR -> DB : SELECT * FROM academic_years\nWHERE code = ? AND school_id = ?
activate DB
DB --> AYR : academicYearRow
deactivate DB
AYR --> S : Optional<AcademicYear>
deactivate AYR

alt Not found / cross-school
  S --> C : 404 SCH-404 (MSG-137)
else Found (status = ACTIVE)
  S -> AYR : save(academicYear with status = CLOSED)
  activate AYR
  AYR -> DB : UPDATE academic_years SET status = 'CLOSED'\nWHERE id = ?
  activate DB
  DB --> AYR : 1 row updated
  deactivate DB
  AYR --> S : AcademicYear (status = CLOSED)
  deactivate AYR

  S -> CR : markReadOnlyByAcademicYearId(ayId) — BR-193
  activate CR
  CR -> DB : UPDATE classrooms SET read_only = true\nWHERE academic_year_id = ?
  activate DB
  DB --> CR : n rows updated
  deactivate DB
  CR --> S : affectedClassrooms : int
  deactivate CR

  note right of S: Cascade — no row deleted, historical data\nkept intact (same principle as BR-021)

  S -> A : audit(actor, CLOSE_ACADEMIC_YEAR, SUCCESS)
  activate A
  A --> S : auditId
  deactivate A

  S -> S : publish AcademicYearClosed event
  S --> C : 200 AcademicYearResponse\n{ id: code, name, status: "CLOSED" } MSG-083
end
deactivate S

C --> UI : 200 / 404 response
deactivate C
UI --> SA : show the year as "Closed" and its classrooms\nas read-only
deactivate UI
@enduml
```

**Description:**
Closing an academic year cascades a `read_only=true` flag onto every classroom of that year (BR-193) — new students cannot be enrolled and the homeroom teacher cannot be reassigned (rejected by `assignHomeroomTeacher`, see §3.3). No data is deleted; a closed year eventually transitions to `ARCHIVED` and remains permanently queryable in read-only mode (BR-195).

---

### 4.4 Sequence Diagram — Promote Students to Next Academic Year (UC-177, BR-194)

```plantuml
@startuml MF02_PromoteStudents
title Sequence Diagram: Promote Students to Next Academic Year

' Bật chế độ liên kết vuông góc (Orthogonal lines)
skinparam linetype ortho

' ==========================================
' CẤU HÌNH MÀU SẮC TÔNG XANH LÁ
' ==========================================
skinparam backgroundColor #FFFFFF

' Định dạng chung cho Stereotype
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
' Lifeline theo stereotype robustness UML
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

    ' ==========================================
    ' Actor & Participant
    ' ==========================================
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

    ' ==========================================
    ' Message
    ' ==========================================
    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontName Arial
    ArrowFontSize 12

    ' ==========================================
    ' Activation (Execution)
    ' ==========================================
    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    ' ==========================================
    ' Group (alt / opt / loop / par...)
    ' ==========================================
    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial

}

actor "School Administration" as SA
boundary ":StudentPromotionUI" as UI
control ":AcademicYearController" as C
control ":AcademicYearServiceImpl" as S
entity ":AcademicYearRepository" as AYR
entity ":ClassroomRepository" as CR
boundary ":StudentPromotionPort" as SP
database ":PostgreSQL" as DB
control ":AuditService" as A

SA -> UI : map source → target classrooms,\ntick students to exclude, click "Promote"
activate UI
UI -> C : POST /api/v1/academic-years/{code}/promote\nAuthorization: Bearer <JWT>\n{ sourceAcademicYearId, mappings, excludeStudentIds }
activate C
C -> S : promoteStudents(code, PromoteStudentsRequest, actor)
activate S

S -> AYR : findByCodeAndSchoolId(code, actor.schoolId)
activate AYR
AYR -> DB : SELECT * FROM academic_years\nWHERE code = ? AND school_id = ?
activate DB
DB --> AYR : academicYearRow
deactivate DB
AYR --> S : Optional<AcademicYear>
deactivate AYR

alt Target year not found / not DRAFT-or-ACTIVE
  S --> C : 404 SCH-404 (MSG-137)
else Target year valid
  loop for each classroom mapping
    S -> CR : findByCode(mapping.targetClassroomId)
    activate CR
    CR -> DB : SELECT * FROM classrooms WHERE code = ?
    activate DB
    DB --> CR : classroomRow
    deactivate DB
    CR --> S : Classroom (capacity)
    deactivate CR

    S -> S : projectedCount = currentCount\n+ incomingCount - excluded
    alt projectedCount > capacity
      S --> C : 409 SCH-010 (MSG-010)\n"Target classroom capacity would be exceeded."
    end
  end

  S -> SP : promote(mappings, excludeStudentIds)\n[delegates to MF03 — by student/classroom ID only]
  activate SP
  SP -> DB : (MF03) INSERT enrollment rows\nfor the target academic year
  activate DB
  DB --> SP : n rows inserted
  deactivate DB
  SP --> S : promotedCount : int
  deactivate SP

  note right of S: BR-194 — health/allergy records untouched;\npromotion only changes enrollment, not medical data

  S -> A : audit(actor, PROMOTE_STUDENTS, SUCCESS, batch)
  activate A
  A --> S : auditId
  deactivate A

  S -> S : publish StudentsPromoted event [async]
  S --> C : 200 PromotionResult\n{ promoted: 44, excluded: 1,\n  targetAcademicYearId: code } MSG-084
end
deactivate S

C --> UI : 200 / 404 / 409 response
deactivate C
UI --> SA : show promotion summary\n(44 promoted, 1 excluded)
deactivate UI
@enduml
```

**Description:**
Promotion is an explicit, batch, opt-out operation per BR-194 — creating a new academic year with `copyStructure=true` only clones classroom shells (name/grade/capacity), it never moves students. `promoteStudents()` is the only action that actually re-enrolls students, grade by grade, with the ability to exclude specific students (e.g. those repeating a grade or leaving the school). Each target classroom's capacity is checked before delegating to MF03's `StudentPromotionPort`, and student health/allergy records are explicitly out of scope for this operation — they remain owned by MF03 and are unaffected by an academic year change.

---

## 5. Lifecycle State Machines

### 5.1 School State Machine (UC-070/UC-001, BR-021/BR-028)

```plantuml
@startuml MF02_SchoolState
title State Diagram: School Lifecycle

' Bật chế độ liên kết vuông góc (Orthogonal lines)
skinparam linetype ortho

' ==========================================
' CẤU HÌNH MÀU SẮC TÔNG XANH LÁ
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
' Note
' ==========================================
skinparam note {
    BackgroundColor #E8F5E9
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 12
}

' ==========================================
' Start / End
' ==========================================
skinparam startColor #2E7D32
skinparam endColor #2E7D32

[*] --> PENDING : register

PENDING --> ACTIVE : approve

ACTIVE --> INACTIVE : deactivate

INACTIVE --> ACTIVE : reactivate
@enduml
```

**Description:**
A school always enters at `PENDING` (self-registration, UC-070) and only System Admin approval moves it to `ACTIVE`, which is also the moment its GPS coordinates become locked (BR-028). `ACTIVE ⇄ INACTIVE` is fully reversible; the `INACTIVE` cascade (BR-021) never deletes data, it only flips status flags on dependent classrooms and accounts, which are restored automatically on reactivation.

### 5.2 Academic Year State Machine (BR-191/BR-193/BR-195)

```plantuml
@startuml MF02_AcademicYearState
title State Diagram: Academic Year Lifecycle

' Bật chế độ liên kết vuông góc (Orthogonal lines)
skinparam linetype ortho

' ==========================================
' CẤU HÌNH MÀU SẮC TÔNG XANH LÁ
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
' Note
' ==========================================
skinparam note {
    BackgroundColor #E8F5E9
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 12
}

' ==========================================
' Start / End
' ==========================================
skinparam startColor #2E7D32
skinparam endColor #2E7D32

[*] --> DRAFT : create

DRAFT --> ACTIVE : activate\n[no other ACTIVE year]
DRAFT --> [*] : discard

ACTIVE --> CLOSED : close

CLOSED --> ARCHIVED : archive
@enduml
```

**Description:**
No transition ever deletes an academic year or its classrooms — the model is append-only by design once a year leaves `DRAFT`. The `≤ 1 ACTIVE` invariant (BR-191) is the single most safety-critical rule in MF02 and is therefore enforced redundantly at both the service layer (pessimistic locking) and the database layer (partial unique index), so it cannot be violated even by a bug in the service code.

### 5.3 Classroom State Machine (UC-018, BR-021/BR-192/BR-193)

```plantuml
@startuml MF02_ClassroomState
title State Diagram: Classroom Lifecycle

' Bật chế độ liên kết vuông góc (Orthogonal lines)
skinparam linetype ortho

' ==========================================
' CẤU HÌNH MÀU SẮC TÔNG XANH LÁ
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
' Note
' ==========================================
skinparam note {
    BackgroundColor #E8F5E9
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 12
}

' ==========================================
' Start / End
' ==========================================
skinparam startColor #2E7D32
skinparam endColor #2E7D32

state EDITABLE : readOnly = false
state READ_ONLY : readOnly = true

[*] --> EDITABLE : create\n[year is ACTIVE]

EDITABLE --> EDITABLE : update
EDITABLE --> EDITABLE : assign teacher

EDITABLE --> READ_ONLY : close year
EDITABLE --> READ_ONLY : deactivate school

READ_ONLY --> EDITABLE : reactivate school\n[year still ACTIVE]

READ_ONLY --> [*] : archive year
@enduml
```

**Description:**
A classroom has no `status` column of its own — its lifecycle is expressed entirely by the boolean `read_only`, which is a *derived* flag driven by two parents: the academic year (BR-193, §4.3) and the school (BR-021, §1.4). This is why the diagram has only two stable states. A classroom can only be created while its parent academic year is `ACTIVE` (BR-192), and re-editability is possible only via the school path — once the academic year is `CLOSED` the flag is permanent for that year, since a closed year never returns to `ACTIVE` (§5.2). All rejected writes share the same error contract (409 `SCH-193` / MSG-010), which is what `assignHomeroomTeacher` checks first in §3.3.

### 5.4 GPS Coordinate Lock State Machine (BR-028)

```plantuml
@startuml MF02_CoordinateLockState
title State Diagram: School GPS Coordinate Lock

' Bật chế độ liên kết vuông góc (Orthogonal lines)
skinparam linetype ortho

' ==========================================
' CẤU HÌNH MÀU SẮC TÔNG XANH LÁ
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
' Note
' ==========================================
skinparam note {
    BackgroundColor #E8F5E9
    BorderColor #2E7D32
    FontColor #212121
    FontName Arial
    FontSize 12
}

' ==========================================
' Start / End
' ==========================================
skinparam startColor #2E7D32
skinparam endColor #2E7D32

state UNLOCKED : coordinateLocked = false
state LOCKED : coordinateLocked = true

[*] --> UNLOCKED : register school

UNLOCKED --> UNLOCKED : edit coordinates\n[valid range]

UNLOCKED --> LOCKED : activate school
UNLOCKED --> LOCKED : save coordinates

LOCKED --> UNLOCKED : unlock\n[System Admin + reason]

LOCKED --> LOCKED : edit rejected (403)
@enduml
```

**Description:**
`coordinateLocked` is a second, independent state machine living on the same `School` row as §5.1 — a school can be `ACTIVE` while its coordinates are momentarily `UNLOCKED`. Modelling it separately makes the BR-028 invariant explicit: the `LOCKED → UNLOCKED` edge is the only privileged transition in MF02 that requires both a role check (`SYSTEM_ADMIN`) and a mandatory free-text `reason`, and it is always audited. Because every successful coordinate write re-locks the record, an unlock is effectively a one-shot permission rather than a lasting mode — which is what the self-transition on `LOCKED` (403 `SCH-013`) and the re-lock edge in §1.3 jointly enforce.

---

*Document generated from TDS `SFMS-MF02-IMP-001 v1.0` (Approved 2026-07-31).*
