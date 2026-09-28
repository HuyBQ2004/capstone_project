# MF03 — Student & Parent Management
## Design Diagrams (Database, Class, Sequence, State)

> **Stack:** Spring Boot 4.1.x (Java 25) · PostgreSQL 16 · Flyway · Spring Security + JWT · springdoc-OpenAPI · MapStruct
> **Bounded Context:** Student Profile & Health Records
> **Data Classification:** `Restricted` — child PII + health data (allergies, chronic conditions, clinical records)
> **Based on:** TDS `SFMS-MF03-IMP-001 v1.1` (Approved 2026-08-02)

---

## Database Architecture (DBML)

> Reflects Flyway `V4__mf03_student_parent.sql` (owned by MF03; `V1` = MF01, `V3` = MF02 — MF03 never edits those files). All tables use UUID primary keys generated server-side (`gen_random_uuid()`). Cross-module references (`school_id`, `classroom_id`, `parent_user_id`) are UUID-only, resolved through `ReferenceResolver` — no foreign key import of another module's entity (design rule §4b). Derived fields (`healthStatus`, `allergies`, `specialDiet`, BMI) are **never** persisted as columns (ADR-M03-3) — they are computed at query time and appear only in API responses.

```dbml
// Use DBML to define your database structure
// Docs: https://dbml.dbdiagram.io/docs
// Module: MF03 — Student & Parent Management
// Source: TDS SFMS-MF03-IMP-001 v1.1 | Flyway V4__mf03_student_parent.sql

Table students {
  id              uuid         [pk, default: `gen_random_uuid()`, note: "Internal PK"]
  code            varchar(32)  [not null, note: "Business code e.g. HS-3A-001 (exposed as \"id\" in JSON, design rule D1)"]
  registration_no varchar(32)  [not null, unique, note: "System-wide unique admission/registration number (UC-025 step 6)"]
  name            varchar(120) [not null, note: "Search field"]
  dob             date         [not null]
  school_id       uuid         [not null, note: "UUID ref -> schools(id) in MF02, no cross-module FK"]
  classroom_id    uuid         [not null, note: "UUID ref -> classrooms(id) in MF02. BR-025: capacity checked via port"]
  status          varchar(16)  [not null, default: 'ACTIVE', note: "ACTIVE | SUSPENDED | TRANSFERRED"]
  created_at      timestamptz  [not null, default: `now()`]
  updated_at      timestamptz
  created_by      varchar(180)
  updated_by      varchar(180)

  indexes {
    (school_id, classroom_id) [name: "ix_student_school_class"]
    (school_id, code) [unique, name: "uq_student_school_code"]
  }

  Note: "Core student profile. Derived-only fields NOT stored as columns: allergies, specialDiet, healthStatus, allergySeverity, hasAllergy, hasSpecialDiet, parent, class (ADR-M03-3)."
}

Table student_parents {
  id             uuid        [pk, default: `gen_random_uuid()`]
  student_id     uuid        [not null, ref: > students.id, note: "ON DELETE CASCADE"]
  parent_user_id uuid        [not null, note: "UUID ref -> user_accounts(id) in MF01, role Parent"]
  relationship   varchar(40) [note: "e.g. Father | Mother | Guardian"]
  is_primary     boolean     [not null, default: false]
  linked_at      timestamptz [not null, default: `now()`]

  indexes {
    (student_id, parent_user_id) [unique, name: "uq_student_parent"]
    parent_user_id [name: "ix_sp_parent"]
    student_id [name: "ix_sp_student"]
  }

  Note: "Join table replacing the mock's free-text Student.parent string (ADR-M03-1). Enforces BR-030 (>=1 parent per student) and BR-201 (<=5 students per parent, guarded by a BEFORE INSERT trigger)."
}

Table allergy_declarations {
  id                   uuid         [pk, default: `gen_random_uuid()`]
  student_id           uuid         [not null, ref: > students.id, note: "ON DELETE CASCADE"]
  category             varchar(80)  [not null]
  allergen             varchar(80)  [not null]
  severity             varchar(16)  [not null, note: "MILD | MODERATE | SEVERE (common enum, D6)"]
  declared_at          timestamptz  [not null, default: `now()`]
  declared_by_user_id  uuid         [not null]
  status               varchar(16)  [not null, default: 'PENDING', note: "PENDING | VERIFIED | NEEDS_INFO — BR-031 gate"]
  proof_file_id        uuid         [ref: > health_documents.id]
  review_note          varchar(500)
  verified_by_user_id  uuid
  verified_at          timestamptz

  indexes {
    (student_id, status) [name: "ix_alg_student_status"]
  }

  Note: "Source of truth for verified allergies (ADR-M03-2, per-allergen verification, not a single flag). CHECK ck_severe_needs_proof: SEVERE + VERIFIED requires proof_file_id (BR-031). Only status=VERIFIED rows feed MF05 (menu conflict) and MF09 (BR-123 serving alert)."
}

Table chronic_condition_declarations {
  id                   uuid         [pk, default: `gen_random_uuid()`]
  student_id           uuid         [not null, ref: > students.id, note: "ON DELETE CASCADE"]
  category             varchar(80)  [not null]
  condition_name       varchar(160) [not null]
  severity             varchar(16)  [not null, note: "MILD | MODERATE | SEVERE"]
  notes                varchar(1000)
  declared_at          timestamptz  [not null, default: `now()`]
  declared_by_user_id  uuid         [not null]
  status               varchar(16)  [not null, default: 'PENDING', note: "PENDING | VERIFIED | NEEDS_INFO"]
  proof_file_id        uuid         [ref: > health_documents.id]
  review_note          varchar(500)
  verified_by_user_id  uuid
  verified_at          timestamptz

  Note: "Structurally symmetric with allergy_declarations. Same CHECK constraint pattern (ck_chronic_severe_needs_proof)."
}

Table special_meal_requests {
  id                    uuid         [pk, default: `gen_random_uuid()`]
  student_id            uuid         [not null, ref: > students.id, note: "ON DELETE CASCADE"]
  dietary_type          varchar(60)  [not null, note: "BR-066 open catalog (Halal, Vegetarian, Gluten-Free, Nut-Free, Lactose-Free, ...)"]
  duration              varchar(120) [not null]
  justification         varchar(1000) [not null]
  document_id           uuid         [ref: > health_documents.id]
  requested_by_user_id  uuid         [not null]
  requested_at          timestamptz  [not null, default: `now()`]
  status                varchar(16)  [not null, default: 'PENDING', note: "PENDING | APPROVED | REJECTED | NEEDS_INFO"]
  review_note           varchar(500)
  reviewed_by_user_id   uuid
  reviewed_at           timestamptz

  indexes {
    (student_id, status) [name: "ix_smr_student_status"]
  }

  Note: "Latest APPROVED row per student is the derived source of Student.specialDiet (ADR-M03-3/CF-M03-11). APPROVED triggers SpecialMealApproved event -> MF05 must create a dedicated meal plan (BR-065). dietaryType outside the standard 5 values -> NEEDS_INFO, never a silent reject (BR-220)."
}

Table student_health_records {
  id            uuid         [pk, default: `gen_random_uuid()`]
  student_id    uuid         [not null, unique, ref: > students.id, note: "ON DELETE CASCADE, 1-1"]
  height_cm     decimal(5,1) [note: "CHECK BETWEEN 50 AND 220"]
  weight_kg     decimal(5,1) [note: "CHECK BETWEEN 5 AND 150"]
  blood_type    varchar(4)
  status        varchar(16)  [not null, default: 'PENDING', note: "PENDING | VERIFIED | NEEDS_INFO"]
  proof_file_id uuid         [ref: > health_documents.id]
  created_at    timestamptz  [not null, default: `now()`]
  updated_at    timestamptz
  created_by    varchar(180)
  updated_by    varchar(180)

  Note: "New resource vs. the mock (CF-M03-9). BMI is NOT stored — derived as weightKg / (heightCm/100)^2 (ADR-M03-3)."
}

Table student_clinical_files {
  id                       uuid          [pk, default: `gen_random_uuid()`]
  student_id               uuid          [not null, unique, ref: > students.id, note: "ON DELETE CASCADE, 1-1"]
  medical_conditions       varchar(2000)
  medications              varchar(2000)
  emergency_contact        varchar(200)
  emergency_instructions   varchar(2000)
  created_at               timestamptz   [not null, default: `now()`]
  updated_at                timestamptz
  created_by                varchar(180)
  updated_by                varchar(180)

  Note: "UC-094 edits ONLY these 3-4 clinical fields — never touches allergy/diet verification status (invariant C8). Separated from Student to keep the medical-record write path narrow."
}

Table health_history_events {
  id         uuid        [pk, default: `gen_random_uuid()`]
  student_id uuid        [not null, ref: > students.id]
  at         timestamptz [not null, default: `now()`]
  type       varchar(24) [not null, note: "DECLARED | VERIFIED | CLINICAL_NOTE | INCIDENT | MEDICATION_UPDATE"]
  event      varchar(1000) [not null]
  by_user_id uuid        [not null]

  indexes {
    (student_id, `at desc`) [name: "ix_hh_student_at"]
  }

  Note: "APPEND-ONLY (BR-172, ADR-M03-4). DB-level RULE blocks UPDATE/DELETE and the app role has UPDATE/DELETE REVOKEd — correction is a new event, never an edit of an old one."
}

Table health_documents {
  id                   uuid         [pk, default: `gen_random_uuid()`]
  student_id           uuid         [not null, ref: > students.id]
  file_name            varchar(255) [not null]
  storage_key          varchar(512) [not null, note: "Object storage key — no public URL (ADR-M03-5, PDPD/BR-173)"]
  content_type         varchar(80)  [not null]
  size_bytes           bigint       [not null]
  uploaded_by_user_id  uuid         [not null]
  uploaded_at          timestamptz  [not null, default: `now()`]

  Note: "Backing store for proof_file_id / document_id references. Downloaded only via GET /health-documents/{id}/content with RBAC + audit — never a direct public link."
}

// -- Relationships summary --------------------------------------------------
// student_parents.student_id           -> students.id                (BR-030, BR-201)
// allergy_declarations.student_id      -> students.id                (C4 — mandatory FK)
// chronic_condition_declarations.student_id -> students.id
// special_meal_requests.student_id     -> students.id
// student_health_records.student_id    -> students.id (1-1)
// student_clinical_files.student_id    -> students.id (1-1)
// health_history_events.student_id     -> students.id (append-only)
// health_documents.student_id          -> students.id
// *_declarations/*_requests.proof_file_id / document_id -> health_documents.id
// students.school_id      -> schools.id (MF02, UUID-only, no FK)
// students.classroom_id   -> classrooms.id (MF02, UUID-only, no FK)
// student_parents.parent_user_id -> user_accounts.id (MF01, UUID-only, no FK)
```

---

## 1. Student Profile Management

### 1.1 Class Diagram

```plantuml
@startuml MF03_StudentProfile_ClassDiagram
title Design-level Class Diagram: Student Profile & Parent Linking

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

class StudentController <<control>> {
  -studentService : IStudentService
  +list(params, actor) : PagedResponse<StudentResponse>
  +getById(id, actor) : StudentResponse
  +create(req : CreateStudentRequest, actor) : StudentResponse
  +update(id, req : UpdateStudentRequest, actor) : StudentResponse
  +getAllergyAlerts(id, actor) : AllergyAlertResponse
}

class StudentImportController <<control>> {
  -importService : IStudentImportService
  +downloadTemplate() : Resource
  +validate(file : MultipartFile, actor) : ImportPreviewResponse
  +commit(importId : UUID, actor) : ImportResultResponse
}

interface IStudentService <<interface>> {
  +list(params, actor : AuthUser) : PagedResponse<StudentResponse>
  +getById(id : UUID, actor : AuthUser) : StudentResponse
  +create(req : CreateStudentRequest, actor : AuthUser) : StudentResponse
  +update(id : UUID, req : UpdateStudentRequest, actor : AuthUser) : StudentResponse
  +allergyAlerts(studentId : UUID, actor : AuthUser) : List<AllergyAlert>
}

interface IParentLinkService <<interface>> {
  +linkOrCreateParent(studentId : UUID, info : ParentInfo, actor : AuthUser) : UUID
  +unlink(studentId : UUID, parentUserId : UUID, actor : AuthUser) : void
  +countLinkedStudents(parentUserId : UUID) : long
  +listChildren(parentUserId : UUID, params) : PagedResponse<StudentResponse>
}

interface IStudentImportService <<interface>> {
  +validate(file : MultipartFile, actor : AuthUser) : ImportPreviewResponse
  +commit(importId : UUID, actor : AuthUser) : ImportResultResponse
  +downloadTemplate() : Resource
}

class StudentService <<control>> {
  -studentRepo : StudentRepository
  -parentLinkService : IParentLinkService
  -classroomPort : ClassroomCapacityPort
  -allergyRepo : AllergyDeclarationRepository
  -healthHistoryService : IHealthHistoryService
  +list(params, actor) : PagedResponse<StudentResponse>
  +getById(id, actor) : StudentResponse
  +create(req, actor) : StudentResponse
  +update(id, req, actor) : StudentResponse
  +allergyAlerts(studentId, actor) : List<AllergyAlert>
  -generateStudentCode() : String
  -generateRegistrationNo() : String
  -deriveHealthStatus(studentId) : String
}

class ParentLinkService <<control>> {
  -linkRepo : StudentParentRepository
  -parentAccountPort : ParentAccountPort
  +linkOrCreateParent(studentId, info, actor) : UUID
  +unlink(studentId, parentUserId, actor) : void
  +countLinkedStudents(parentUserId) : long
  +listChildren(parentUserId, params) : PagedResponse<StudentResponse>
}

class StudentImportService <<control>> {
  -studentRepo : StudentRepository
  -linkRepo : StudentParentRepository
  -parentAccountPort : ParentAccountPort
  +validate(file, actor) : ImportPreviewResponse
  +commit(importId, actor) : ImportResultResponse
  +downloadTemplate() : Resource
}

interface StudentRepository <<repository>> {
  +findById(id : UUID) : Optional<Student>
  +findAll(spec, pageable) : Page<Student>
  +existsBySchoolIdAndCode(schoolId, code) : boolean
  +save(entity : Student) : Student
}

interface StudentParentRepository <<repository>> {
  +findByStudentId(studentId : UUID) : List<StudentParent>
  +countByParentUserId(parentUserId : UUID) : long
  +existsByStudentIdAndParentUserId(studentId, parentUserId) : boolean
  +save(entity : StudentParent) : StudentParent
  +delete(entity : StudentParent) : void
}

interface ParentAccountPort <<port to MF01>> {
  +findParentByEmail(email : String) : Optional<UUID>
  +createParentAccount(fullName, email, phone : String) : UUID
  +displayName(parentUserId : UUID) : String
}

interface ClassroomCapacityPort <<port to MF02>> {
  +assertHasRoom(classroomId : UUID) : void
  +isReadOnly(classroomId : UUID) : boolean
  +classDisplayName(classroomId : UUID) : String
}

class Student <<entity>> {
  +id : UUID <<PK>>
  +code : String
  +registrationNo : String
  +name : String
  +dob : LocalDate
  +schoolId : UUID
  +classroomId : UUID
  +status : StudentStatus
  +createdAt : Instant
}

class StudentParent <<entity>> {
  +id : UUID <<PK>>
  +studentId : UUID <<FK>>
  +parentUserId : UUID <<FK>>
  +relationship : String
  +isPrimary : boolean
  +linkedAt : Instant
}

enum StudentStatus <<enumeration>> {
  ACTIVE
  SUSPENDED
  TRANSFERRED
}

class CreateStudentRequest <<DTO>> {
  +name : String
  +dob : LocalDate
  +classroomId : String
  +parent : ParentInfo
}

class ParentInfo <<DTO>> {
  +fullName : String
  +email : String
  +phone : String
  +relationship : String
}

class StudentResponse <<DTO>> {
  +id : String
  +studentCode : String
  +registrationNo : String
  +name : String
  +class : String
  +classroomId : String
  +dob : LocalDate
  +allergies : List<String>
  +specialDiet : String
  +healthStatus : String
  +parent : String
  +parentId : String
  +parents : List<ParentSummary>
}

class ImportPreviewResponse <<DTO>> {
  +importId : String
  +total : int
  +validCount : int
  +invalidCount : int
  +rows : List<ImportRow>
}

StudentController --> IStudentService : uses
StudentImportController --> IStudentImportService : uses
IStudentService <|.. StudentService
IParentLinkService <|.. ParentLinkService
IStudentImportService <|.. StudentImportService

StudentService --> StudentRepository : reads/writes
StudentService --> IParentLinkService : delegates linking
StudentService --> ClassroomCapacityPort : checks capacity (BR-025)
StudentService ..> CreateStudentRequest : consumes
StudentService ..> StudentResponse : produces

ParentLinkService --> StudentParentRepository : manages links
ParentLinkService --> ParentAccountPort : find/create parent (BR-010/012)

StudentImportService --> StudentRepository : bulk creates
StudentImportService --> StudentParentRepository : bulk links
StudentImportService --> ParentAccountPort : find/create parent
StudentImportService ..> ImportPreviewResponse : produces

StudentRepository ..> Student : manages
StudentParentRepository ..> StudentParent : manages
Student "1" *-- "1..*" StudentParent
Student "n" --> "1" StudentStatus
CreateStudentRequest "1" *-- "1" ParentInfo
@enduml
```

**Description:**
`StudentController` exposes the read/write endpoints for the student roster (UC-147 list, UC-025 create, UC-099 update, BR-123 allergy alerts); `StudentImportController` is split out for the two-phase bulk-import flow (UC-010). `StudentService` never talks to MF01/MF02 directly — it goes through `ParentAccountPort` and `ClassroomCapacityPort`, which reference the other modules purely by UUID (design rule §4b, no cross-module entity import). `ParentLinkService` owns the `student_parents` join table introduced to replace the mock's free-text `parent` field (ADR-M03-1), enforcing BR-030 (≥1 parent per student) and BR-201 (≤5 students per parent). `StudentResponse` intentionally carries several derived-only fields (`allergies`, `specialDiet`, `healthStatus`, `parent`) that do not exist as columns on `Student` (ADR-M03-3) — they are computed by `StudentService` at read time.

---

### 1.2 Sequence Diagram — Create Student & Link Parent (UC-025, BR-030 / BR-201)

```plantuml
@startuml MF03_CreateStudent
title Sequence Diagram: Create Student & Link Parent

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

autonumber

actor "School Administration" as SA
boundary ":StudentFormUI" as UI
control ":StudentController" as C
control ":StudentService" as S
control ":ClassroomCapacityPort\n(-> MF02)" as CL
control ":ParentLinkService" as PL
control ":ParentAccountPort\n(-> MF01)" as P
entity ":StudentParentRepository" as SPR
entity ":StudentRepository" as R
control ":HealthHistoryService" as H
database ":PostgreSQL" as DB

SA -> UI : enter student profile + parent info,\nclick "Save"
activate UI
UI -> UI : validate form (name, dob, classroomId, parent.email)
UI -> C : POST /api/v1/students\n{ name, dob, classroomId, parent }
activate C
C -> C : AuthUser{userId, role, schoolId} <- JWT
C -> S : create(CreateStudentRequest, actor)
activate S

S -> CL : assertHasRoom(classroomId)
activate CL

alt Classroom already at capacity
  CL --> S : throw CapacityExceededException
  deactivate CL
  S --> C : throw ConflictException(STU-010)
  C --> UI : 409 { code: "STU-010", message: MSG-142 }
  UI --> SA : show error "This classroom has reached its capacity."
else Room available
  CL --> S : void (room available)

  S -> PL : linkOrCreateParent(studentId, parentInfo, actor)
  activate PL
  PL -> P : findParentByEmail(email)
  activate P
  alt Parent account already exists
    P --> PL : Optional<UUID> parentUserId
  else No account yet
    P --> PL : Optional.empty()
    PL -> P : createParentAccount(fullName, email, phone)
    P --> PL : UUID parentUserId (PENDING_VERIFICATION)
  end
  deactivate P

  PL -> SPR : countByParentUserId(parentUserId)
  activate SPR
  SPR -> DB : SELECT count(*) FROM student_parents\nWHERE parent_user_id = ?
  activate DB
  DB --> SPR : ResultSet { count }
  deactivate DB
  SPR --> PL : long count
  deactivate SPR

  alt count >= 5 (BR-201)
    PL --> S : throw ConflictException(STU-201)
    deactivate PL
    S --> C : throw ConflictException(STU-201)
    C --> UI : 409 { code: "STU-201" }
    UI --> SA : show "Parent account already linked to 5 students."
  else count < 5
    PL --> S : UUID parentUserId
    S -> S : generateStudentCode() + generateRegistrationNo()

    group Single @Transactional boundary (BR-030)
      S -> R : save(Student{status=ACTIVE})
      activate R
      R -> DB : INSERT INTO students (...)
      activate DB
      DB --> R : 1 row inserted
      deactivate DB
      R --> S : Student{id, code, registrationNo}
      deactivate R

      S -> SPR : save(StudentParent{studentId, parentUserId, isPrimary=true})
      activate SPR
      SPR -> DB : INSERT INTO student_parents (...)
      activate DB
      DB --> SPR : 1 row inserted
      deactivate DB
      SPR --> S : StudentParent{id}
      deactivate SPR

      S -> H : append(studentId, DECLARED,\n"Student profile created", actor.userId)
      activate H
      H -> DB : INSERT INTO health_history_events (...)
      activate DB
      DB --> H : 1 row inserted
      deactivate DB
      H --> S : void
      deactivate H
    end

    S --> C : StudentResponse{ id, studentCode, registrationNo,\n healthStatus="VERIFIED", parent }
    deactivate S
    C --> UI : 201 Created { StudentResponse } (MSG-031)
    deactivate C
    UI --> SA : show the created profile + student code
    deactivate UI
  end
end

@enduml
```

**Description:**
Creating a student is a single transaction that spans two module boundaries. Classroom capacity (BR-025) is verified first via `ClassroomCapacityPort` so no orphaned student row is ever created against a full class. Parent linking is delegated to `ParentLinkService`, which resolves an existing parent account by email or asks MF01 to create one (MF01 owns account creation and email dispatch — MF03 never sends mail itself). The 5-student cap (BR-201) is checked in the service *and* re-validated by a database trigger (`check_parent_link_limit`) so the invariant survives even a bug in the service layer. Every successful creation appends one `health_history_events` row, satisfying the append-only audit requirement (BR-172).

---

### 1.3 Sequence Diagram — Bulk Import Students & Parents (UC-010, BR-032)

```plantuml
@startuml MF03_BulkImport
title Sequence Diagram: Bulk Import Students & Parents

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

autonumber

actor "School Administration" as SA
boundary ":StudentImportUI" as UI
control ":StudentImportController" as C
control ":StudentImportService" as S
control ":ParentAccountPort\n(-> MF01)" as P
entity ":StudentRepository" as R
entity ":StudentParentRepository" as SPR
database ":PostgreSQL" as DB

== Phase 1 — Validate (no business data written) ==

SA -> UI : choose a .xlsx/.csv file matching the template,\nclick "Validate"
activate UI
UI -> C : POST /api/v1/students/import/validate\n(multipart file)
activate C
C -> S : validate(file, actor)
activate S
S -> S : parse rows against the template\n(code | name | dob | classroom | parentName | parentEmail | parentPhone)

loop for each row
  S -> R : existsBySchoolIdAndCode(schoolId, code)
  activate R
  R -> DB : SELECT 1 FROM students\nWHERE school_id = ? AND code = ?
  activate DB
  DB --> R : ResultSet { exists }
  deactivate DB
  R --> S : boolean exists (duplicate -> MSG-012)
  deactivate R

  S -> P : findParentByEmail(parentEmail)
  activate P
  P --> S : Optional<UUID> parentUserId
  deactivate P

  opt parentUserId already exists
    S -> SPR : countByParentUserId(parentUserId)
    activate SPR
    SPR -> DB : SELECT count(*) FROM student_parents\nWHERE parent_user_id = ?
    activate DB
    DB --> SPR : ResultSet { count }
    deactivate DB
    SPR --> S : long count (>= 5 -> BR-201 error)
    deactivate SPR
  end

  S -> S : validate email/phone format\n(BR-012, rule delegated from MF01)
end

S --> C : ImportPreviewResponse{ importId, total,\n validCount, invalidCount, rows[](rowNo, code, message) }
deactivate S
C --> UI : 200 { ImportPreviewResponse }
deactivate C
UI --> SA : show the per-row error table (AF1)\n+ "Confirm import" button
deactivate UI

== Phase 2 — Commit (single transaction, all-or-nothing) ==

SA -> UI : click "Confirm import"
activate UI
UI -> C : POST /api/v1/students/import/{importId}/commit
activate C
C -> S : commit(importId, actor)
activate S

group @Transactional — all-or-nothing (ADR-M03-6)
  loop for each valid row
    S -> P : findParentByEmail(email)
    activate P
    P --> S : Optional<UUID> parentUserId
    opt parentUserId is empty
      S -> P : createParentAccount(fullName, email, phone)
      P --> S : UUID parentUserId
    end
    deactivate P

    S -> R : save(Student{status=ACTIVE})
    activate R
    R -> DB : INSERT INTO students (...)
    activate DB
    DB --> R : 1 row inserted
    deactivate DB
    R --> S : Student{id}
    deactivate R

    S -> SPR : save(StudentParent{studentId, parentUserId})
    activate SPR
    SPR -> DB : INSERT INTO student_parents (...)
    activate DB
    DB --> SPR : 1 row inserted
    deactivate DB
    SPR --> S : StudentParent{id}
    deactivate SPR
  end
end

alt A row fails midway
  S -> DB : ROLLBACK
  activate DB
  DB --> S : transaction rolled back
  deactivate DB
  S --> C : throw ImportFailedException(STU-032)
  C --> UI : 422 { code: "STU-032", failedRow }
  UI --> SA : show "No row was imported — please fix the file."
else All rows valid
  S -> DB : COMMIT
  activate DB
  DB --> S : transaction committed
  deactivate DB
  S --> C : ImportResultResponse{ imported, skipped, failed }
  deactivate S
  C --> UI : 200 { ImportResultResponse } (MSG-035)
  deactivate C
  UI --> SA : show summary "Imported <imported> students"
  deactivate UI
end

@enduml
```

**Description:**
Bulk import runs in two explicit phases (ADR-M03-6). Phase 1 (`validate`) never writes business data — it only parses the uploaded file, checks for duplicate admission codes (MSG-012), unknown classrooms, and the BR-201 parent-link cap, returning a per-row error report so the operator can fix and re-upload. Phase 2 (`commit`) replays only the previously-validated rows inside a single database transaction — either every row is inserted or none are, closing the gap left by row-by-row inserts that could fail halfway through.

---

## 2. Health & Allergy Declaration

### 2.1 Class Diagram

```plantuml
@startuml MF03_HealthDeclaration_ClassDiagram
title Design-level Class Diagram: Health & Allergy Declaration Review

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

class AllergyDeclarationController <<control>> {
  -declarationService : IAllergyDeclarationService
  +list(params, actor) : PagedResponse<AllergyDeclarationResponse>
  +submit(req : SubmitAllergyRequest, actor) : AllergyDeclarationResponse
  +resubmit(id, req : SubmitAllergyRequest, actor) : AllergyDeclarationResponse
}

class HealthReviewController <<control>> {
  -reviewService : IHealthReviewService
  +listAllergyDeclarations(params) : PagedResponse<AllergyDeclarationResponse>
  +verifyAllergy(id) : AllergyDeclarationResponse
  +requestAllergyInfo(id, req : ReviewNoteRequest) : AllergyDeclarationResponse
  +verifyChronic(id) : ChronicConditionResponse
  +requestChronicInfo(id, req : ReviewNoteRequest) : ChronicConditionResponse
}

interface IAllergyDeclarationService <<interface>> {
  +list(params, actor : AuthUser) : PagedResponse<AllergyDeclarationResponse>
  +submit(req : SubmitAllergyRequest, parent : AuthUser) : AllergyDeclarationResponse
  +resubmit(id : UUID, req : SubmitAllergyRequest, parent : AuthUser) : AllergyDeclarationResponse
}

interface IHealthReviewService <<interface>> {
  +listAllergyDeclarations(params, actor : AuthUser) : PagedResponse<AllergyDeclarationResponse>
  +verifyAllergy(id : UUID, actor : AuthUser) : AllergyDeclarationResponse
  +requestAllergyInfo(id : UUID, reason : String, actor : AuthUser) : AllergyDeclarationResponse
  +verifyChronic(id : UUID, actor : AuthUser) : ChronicConditionResponse
  +requestChronicInfo(id : UUID, reason : String, actor : AuthUser) : ChronicConditionResponse
}

class AllergyDeclarationService <<control>> {
  -declRepo : AllergyDeclarationRepository
  -studentParentRepo : StudentParentRepository
  -historyService : IHealthHistoryService
  +list(params, actor) : PagedResponse<AllergyDeclarationResponse>
  +submit(req, parent) : AllergyDeclarationResponse
  +resubmit(id, req, parent) : AllergyDeclarationResponse
  -assertParentOwnsStudent(studentId, parent) : void
}

class HealthReviewService <<control>> {
  -declRepo : AllergyDeclarationRepository
  -chronicRepo : ChronicConditionDeclarationRepository
  -historyService : IHealthHistoryService
  -eventPublisher : EventPublisher
  +listAllergyDeclarations(params, actor) : PagedResponse<AllergyDeclarationResponse>
  +verifyAllergy(id, actor) : AllergyDeclarationResponse
  +requestAllergyInfo(id, reason, actor) : AllergyDeclarationResponse
  +verifyChronic(id, actor) : ChronicConditionResponse
  +requestChronicInfo(id, reason, actor) : ChronicConditionResponse
  -assertHasProofIfSevere(decl) : void
}

interface AllergyDeclarationRepository <<repository>> {
  +findById(id : UUID) : Optional<AllergyDeclaration>
  +findByIdWithStudent(id : UUID) : Optional<AllergyDeclaration>
  +findByStudentIdAndStatus(studentId, status) : List<AllergyDeclaration>
  +save(entity : AllergyDeclaration) : AllergyDeclaration
}

interface ChronicConditionDeclarationRepository <<repository>> {
  +findById(id : UUID) : Optional<ChronicConditionDeclaration>
  +save(entity : ChronicConditionDeclaration) : ChronicConditionDeclaration
}

interface StudentParentRepository <<repository>> {
  +existsByStudentIdAndParentUserId(studentId, parentUserId) : boolean
}

class EventPublisher <<control>> {
  +publish(event : DomainEvent) : void
}

class AllergyDeclaration <<entity>> {
  +id : UUID <<PK>>
  +studentId : UUID <<FK>>
  +category : String
  +allergen : String
  +severity : AllergySeverity
  +declaredAt : Instant
  +declaredByUserId : UUID
  +status : DeclarationStatus
  +proofFileId : UUID
  +reviewNote : String
  +verifiedByUserId : UUID
  +verifiedAt : Instant
}

class ChronicConditionDeclaration <<entity>> {
  +id : UUID <<PK>>
  +studentId : UUID <<FK>>
  +category : String
  +conditionName : String
  +severity : AllergySeverity
  +status : DeclarationStatus
  +proofFileId : UUID
}

enum AllergySeverity <<enumeration>> {
  MILD
  MODERATE
  SEVERE
}

enum DeclarationStatus <<enumeration>> {
  PENDING
  VERIFIED
  NEEDS_INFO
}

class SubmitAllergyRequest <<DTO>> {
  +studentId : String
  +category : String
  +allergen : String
  +severity : AllergySeverity
  +proofFileId : String
}

class AllergyDeclarationResponse <<DTO>> {
  +id : String
  +studentId : String
  +allergen : String
  +severity : AllergySeverity
  +status : DeclarationStatus
  +verifiedBy : String
  +verifiedAt : Instant
}

class ReviewNoteRequest <<DTO>> {
  +reason : String
}

interface IHealthHistoryService <<interface>> {
  +append(studentId : UUID, type : HistoryType, event : String, byUserId : UUID) : void
}

AllergyDeclarationController --> IAllergyDeclarationService : uses
HealthReviewController --> IHealthReviewService : uses
IAllergyDeclarationService <|.. AllergyDeclarationService
IHealthReviewService <|.. HealthReviewService

AllergyDeclarationService --> AllergyDeclarationRepository : creates/updates
AllergyDeclarationService --> StudentParentRepository : checks ownership (STU-005)
AllergyDeclarationService --> IHealthHistoryService : appends DECLARED event
AllergyDeclarationService ..> SubmitAllergyRequest : consumes
AllergyDeclarationService ..> AllergyDeclarationResponse : produces

HealthReviewService --> AllergyDeclarationRepository : updates status (BR-031 gate)
HealthReviewService --> ChronicConditionDeclarationRepository : updates status
HealthReviewService --> IHealthHistoryService : appends VERIFIED event
HealthReviewService --> EventPublisher : publishes AllergyVerified
HealthReviewService ..> AllergyDeclarationResponse : produces
HealthReviewService ..> ReviewNoteRequest : consumes

AllergyDeclarationRepository ..> AllergyDeclaration : manages
ChronicConditionDeclarationRepository ..> ChronicConditionDeclaration : manages
AllergyDeclaration "n" --> "1" AllergySeverity
AllergyDeclaration "n" --> "1" DeclarationStatus
ChronicConditionDeclaration "n" --> "1" DeclarationStatus
@enduml
```

**Description:**
Two services split submission from review, matching who is allowed to do what: `AllergyDeclarationService` handles the parent-facing submit/resubmit path (UC-084/UC-093), while `HealthReviewService` is the Medical Staff gate (UC-077) that flips `status` from `PENDING`/`NEEDS_INFO` to `VERIFIED` — the only condition under which a declaration counts toward BR-123 alerts or BR-063 menu conflicts (ADR-M03-2, per-allergen verification, not a single student-level flag). `HealthReviewService.assertHasProofIfSevere()` mirrors the database CHECK constraint (`ck_severe_needs_proof`) at the service layer so the 422 error can carry a clear message before the INSERT/UPDATE ever reaches PostgreSQL. Both services call the shared `IHealthHistoryService.append()` so every state transition leaves exactly one immutable audit row (BR-172).

---

### 2.2 Sequence Diagram — Submit Allergy Declaration (UC-084, BR-031)

```plantuml
@startuml MF03_SubmitAllergy
title Sequence Diagram: Submit Allergy Declaration

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

autonumber

actor "Parent" as PA
boundary ":AllergyDeclarationUI" as UI
control ":AllergyDeclarationController" as C
control ":AllergyDeclarationService" as S
entity ":StudentParentRepository" as SP
entity ":AllergyDeclarationRepository" as R
control ":HealthHistoryService" as H
database ":PostgreSQL" as DB

PA -> UI : select child, enter allergy\n(category, allergen, severity, proof file)
activate UI
UI -> UI : require a proof attachment when severity = SEVERE
UI -> C : POST /api/v1/allergy-declarations\n{ studentId, category, allergen, severity, proofFileId }
activate C
C -> C : AuthUser{userId, role=Parent} <- JWT
C -> S : submit(SubmitAllergyRequest, parent)
activate S

S -> SP : existsByStudentIdAndParentUserId(studentId, parent.userId)
activate SP
SP -> DB : SELECT 1 FROM student_parents\nWHERE student_id = ? AND parent_user_id = ?
activate DB
DB --> SP : ResultSet { exists }
deactivate DB
SP --> S : boolean linked
deactivate SP

alt linked = false
  S --> C : throw ForbiddenException(STU-005)
  C --> UI : 403 { code: "STU-005" }
  UI --> PA : show "You do not have access to this student's records."
else linked = true
  S -> S : assertHasProofIfSevere(severity, proofFileId)

  alt severity = SEVERE and proofFileId = null
    S --> C : throw ValidationException(STU-031)
    C --> UI : 422 { code: "STU-031" }
    UI --> PA : show "A severe allergy requires medical documentation."
  else Valid
    S -> R : save(AllergyDeclaration{status=PENDING})
    activate R
    R -> DB : INSERT INTO allergy_declarations (...)
    activate DB
    DB --> R : 1 row inserted
    deactivate DB
    R --> S : AllergyDeclaration{id, status=PENDING}
    deactivate R

    S -> H : append(studentId, DECLARED,\n"Allergy declared: <allergen>", parent.userId)
    activate H
    H -> DB : INSERT INTO health_history_events (...)
    activate DB
    DB --> H : 1 row inserted
    deactivate DB
    H --> S : void
    deactivate H

    S --> C : AllergyDeclarationResponse{ id, allergen,\n severity, status=PENDING }
    deactivate S
    C --> UI : 201 Created { AllergyDeclarationResponse } (MSG-036)
    deactivate C
    UI --> PA : show "Declaration submitted — awaiting verification"
    deactivate UI
  end
end

@enduml
```

**Description:**
A parent may only declare allergies for a student they are linked to — enforced by checking `student_parents` before any write, not merely by role. A `SEVERE` declaration without a proof document is rejected client-side by the service before it ever reaches the database, giving a precise 422 instead of surfacing a raw constraint violation. Successful submission starts the declaration at `PENDING` and appends one history event; the Medical Staff review queue picks it up from there (§2.3).

---

### 2.3 Sequence Diagram — Medical Staff Verifies Allergy Declaration (UC-077, BR-031 → BR-123)

```plantuml
@startuml MF03_VerifyAllergy
title Sequence Diagram: Medical Staff Verifies Allergy Declaration

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

autonumber

actor "Medical Staff" as MS
boundary ":HealthReviewQueueUI" as UI
control ":HealthReviewController" as C
control ":HealthReviewService" as S
entity ":AllergyDeclarationRepository" as R
control ":HealthHistoryService" as H
control ":EventPublisher" as E
database ":PostgreSQL" as DB

MS -> UI : open the review queue, select a declaration,\nclick "Verify"
activate UI
UI -> C : POST /api/v1/allergy-declarations/{id}/verify
activate C
C -> C : AuthUser{userId, role=MedicalStaff, schoolId} <- JWT
C -> S : verifyAllergy(id, actor)
activate S

S -> R : findByIdWithStudent(id)
activate R
R -> DB : SELECT * FROM allergy_declarations d\nJOIN students s ON s.id = d.student_id\nWHERE d.id = ? AND s.school_id = ?
activate DB
DB --> R : ResultSet { declaration row }
deactivate DB
R --> S : Optional<AllergyDeclaration>
deactivate R

alt Not found / different school
  S --> C : throw NotFoundException(STU-404)
  C --> UI : 404 { code: "STU-404" }
  UI --> MS : show "Declaration not found."
else severity = SEVERE and proofFileId = null
  S -> S : assertHasProofIfSevere(decl)
  S --> C : throw ValidationException(STU-031)
  C --> UI : 422 { code: "STU-031" }
  UI --> MS : show "Severe declaration requires medical documentation (BR-031)."
else Eligible for verification
  S -> R : save(decl{status=VERIFIED,\n verifiedByUserId, verifiedAt})
  activate R
  R -> DB : UPDATE allergy_declarations\nSET status='VERIFIED', verified_by_user_id=?, verified_at=?
  activate DB
  DB --> R : 1 row updated
  deactivate DB
  R --> S : AllergyDeclaration{status=VERIFIED}
  deactivate R

  S -> H : append(studentId, VERIFIED,\n"Allergy verified: <allergen>", actor.userId)
  activate H
  H -> DB : INSERT INTO health_history_events (...)
  activate DB
  DB --> H : 1 row inserted
  deactivate DB
  H --> S : void
  deactivate H

  S -> E : publish(AllergyVerified{studentId, allergen, severity})
  activate E
  E --> S : void (synchronous — safe path)
  deactivate E

  S --> C : AllergyDeclarationResponse{ id, status=VERIFIED,\n verifiedBy, verifiedAt }
  deactivate S
  C --> UI : 200 OK { AllergyDeclarationResponse } (MSG-036)
  deactivate C
  UI --> MS : refresh the queue, mark as "Verified"
  deactivate UI
end

@enduml
```

**Description:**
Verification is the single event that makes an allergy declaration "count" downstream — before this call, MF05 and MF09 never see the row. The same severe/proof-required rule from submission is re-checked here because a declaration can also arrive at review already missing a document (e.g. a resubmission edge case), and the database CHECK constraint is the final backstop either way. Publishing `AllergyVerified` is intentionally synchronous ("safe path") because MF09's serving-time alert (BR-123) is safety-critical — the review action should not be reported as successful before downstream systems can react to it.

---

## 3. Special Meal Request Management

### 3.1 Class Diagram

```plantuml
@startuml MF03_SpecialMeal_ClassDiagram
title Design-level Class Diagram: Special Meal Request Management

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

class SpecialMealRequestController <<control>> {
  -requestService : ISpecialMealRequestService
  +list(params, actor) : PagedResponse<SpecialMealRequestResponse>
  +submit(req : SubmitSpecialMealRequest, actor) : SpecialMealRequestResponse
  +approve(id, req : ReviewNoteRequest, actor) : SpecialMealRequestResponse
  +reject(id, req : ReviewNoteRequest, actor) : SpecialMealRequestResponse
}

interface ISpecialMealRequestService <<interface>> {
  +list(params, actor : AuthUser) : PagedResponse<SpecialMealRequestResponse>
  +submit(req : SubmitSpecialMealRequest, parent : AuthUser) : SpecialMealRequestResponse
  +approve(id : UUID, note : String, medical : AuthUser) : SpecialMealRequestResponse
  +reject(id : UUID, reason : String, medical : AuthUser) : SpecialMealRequestResponse
  +latestApproved(studentId : UUID) : Optional<SpecialMealRequestResponse>
}

class SpecialMealRequestService <<control>> {
  -requestRepo : SpecialMealRequestRepository
  -historyService : IHealthHistoryService
  -eventPublisher : EventPublisher
  +list(params, actor) : PagedResponse<SpecialMealRequestResponse>
  +submit(req, parent) : SpecialMealRequestResponse
  +approve(id, note, medical) : SpecialMealRequestResponse
  +reject(id, reason, medical) : SpecialMealRequestResponse
  +latestApproved(studentId) : Optional<SpecialMealRequestResponse>
  -isStandardDiet(dietaryType) : boolean
}

interface SpecialMealRequestRepository <<repository>> {
  +findById(id : UUID) : Optional<SpecialMealRequest>
  +findFirstByStudentIdAndStatusOrderByReviewedAtDesc(studentId, status) : Optional<SpecialMealRequest>
  +findAll(spec, pageable) : Page<SpecialMealRequest>
  +save(entity : SpecialMealRequest) : SpecialMealRequest
}

interface IHealthHistoryService <<interface>> {
  +append(studentId : UUID, type : HistoryType, event : String, byUserId : UUID) : void
}

class EventPublisher <<control>> {
  +publish(event : DomainEvent) : void
}

class SpecialMealRequest <<entity>> {
  +id : UUID <<PK>>
  +studentId : UUID <<FK>>
  +dietaryType : String
  +duration : String
  +justification : String
  +documentId : UUID
  +requestedByUserId : UUID
  +requestedAt : Instant
  +status : RequestStatus
  +reviewNote : String
  +reviewedByUserId : UUID
  +reviewedAt : Instant
}

enum RequestStatus <<enumeration>> {
  PENDING
  APPROVED
  REJECTED
  NEEDS_INFO
}

class ReviewNoteRequest <<DTO>> {
  +reason : String
}

class SubmitSpecialMealRequest <<DTO>> {
  +studentId : String
  +dietaryType : String
  +duration : String
  +justification : String
  +documentId : String
}

class SpecialMealRequestResponse <<DTO>> {
  +id : String
  +studentId : String
  +dietaryType : String
  +status : RequestStatus
  +reviewedBy : String
}

SpecialMealRequestController --> ISpecialMealRequestService : uses
ISpecialMealRequestService <|.. SpecialMealRequestService
SpecialMealRequestService --> SpecialMealRequestRepository : manages
SpecialMealRequestService --> IHealthHistoryService : appends audit event
SpecialMealRequestService --> EventPublisher : publishes SpecialMealApproved
SpecialMealRequestService ..> SubmitSpecialMealRequest : consumes
SpecialMealRequestService ..> ReviewNoteRequest : consumes
SpecialMealRequestService ..> SpecialMealRequestResponse : produces
SpecialMealRequestRepository ..> SpecialMealRequest : manages
SpecialMealRequest "n" --> "1" RequestStatus
@enduml
```

**Description:**
`SpecialMealRequestService.isStandardDiet()` checks `dietaryType` against the BR-066 catalog (Halal, Vegetarian, Gluten-Free, Nut-Free, Lactose-Free). Per BR-220, a non-standard value is **not** a hard rejection — approval is redirected to `NEEDS_INFO`, requiring the medical justification before it can be approved. `latestApproved(studentId)` backs the derived `Student.specialDiet` field (ADR-M03-3/CF-M03-11): there is no `special_diet` column anywhere — the current diet is always the most recent `APPROVED` row for that student.

---

### 3.2 Sequence Diagram — Approve Special Meal Request (UC-078, BR-065 / BR-220)

```plantuml
@startuml MF03_ApproveSpecialMeal
title Sequence Diagram: Approve Special Meal Request

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

autonumber

actor "Medical Staff" as MS
boundary ":SpecialMealReviewUI" as UI
control ":SpecialMealRequestController" as C
control ":SpecialMealRequestService" as S
entity ":SpecialMealRequestRepository" as R
control ":HealthHistoryService" as H
control ":EventPublisher" as E
database ":PostgreSQL" as DB

MS -> UI : open the special meal request,\nenter a note, click "Approve"
activate UI
UI -> C : POST /api/v1/special-meals/{id}/approve\n{ note }
activate C
C -> C : AuthUser{userId, role=MedicalStaff, schoolId} <- JWT
C -> S : approve(id, note, actor)
activate S

S -> R : findById(id)
activate R
R -> DB : SELECT * FROM special_meal_requests WHERE id = ?
activate DB
DB --> R : ResultSet { request row }
deactivate DB
R --> S : Optional<SpecialMealRequest>
deactivate R

S -> S : isStandardDiet(dietaryType)\n(BR-066: Halal | Vegetarian | Gluten-Free | Nut-Free | Lactose-Free)

alt Outside the standard catalog (BR-220)
  S -> R : save(req{status=NEEDS_INFO, reviewNote})
  activate R
  R -> DB : UPDATE special_meal_requests SET status='NEEDS_INFO'
  activate DB
  DB --> R : 1 row updated
  deactivate DB
  R --> S : SpecialMealRequest{status=NEEDS_INFO}
  deactivate R
  S --> C : throw ValidationException(STU-066)
  C --> UI : 422 { code: "STU-066",\n status: "NEEDS_INFO" }
  UI --> MS : show "Outside the standard diet list — medical\njustification required (not rejected)"
else Standard diet type
  S -> R : save(req{status=APPROVED, reviewedByUserId,\n reviewedAt, reviewNote})
  activate R
  R -> DB : UPDATE special_meal_requests\nSET status='APPROVED', reviewed_by_user_id=?, reviewed_at=?
  activate DB
  DB --> R : 1 row updated
  deactivate DB
  R --> S : SpecialMealRequest{status=APPROVED}
  deactivate R

  S -> H : append(studentId, VERIFIED,\n"Special meal approved: <dietaryType>", actor.userId)
  activate H
  H -> DB : INSERT INTO health_history_events (...)
  activate DB
  DB --> H : 1 row inserted
  deactivate DB
  H --> S : void
  deactivate H

  S -> E : publish(SpecialMealApproved{studentId, dietaryType, duration})
  activate E
  E --> S : void
  deactivate E

  S --> C : SpecialMealRequestResponse{ id, dietaryType,\n status=APPROVED, reviewedBy }
  deactivate S
  C --> UI : 200 OK { SpecialMealRequestResponse } (MSG-036)
  deactivate C
  UI --> MS : refresh the list, mark as "Approved"
  deactivate UI
end

@enduml
```

**Description:**
The BR-066/BR-220 branch is the key decision point: an out-of-catalog diet type never silently becomes `REJECTED` — it becomes `NEEDS_INFO`, sending the request back for additional medical justification before a Medical Staff member can approve it. Only a genuine `APPROVED` transition publishes `SpecialMealApproved`, which is the trigger MF05 relies on to build a dedicated meal plan (BR-065) and instruct the caterer to prepare it separately (BR-082).

---

## 4. Student Health Record & Clinical File

### 4.1 Class Diagram

```plantuml
@startuml MF03_HealthFile_ClassDiagram
title Design-level Class Diagram: Student Health Record & Clinical File

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

class StudentHealthFileController <<control>> {
  -healthFileService : IStudentHealthFileService
  +getFile(studentId, actor) : StudentHealthFileResponse
  +appendClinical(studentId, req : ClinicalUpdateRequest, actor) : StudentHealthFileResponse
  +getHealthRecord(studentId, actor) : StudentHealthRecordResponse
  +upsertHealthRecord(studentId, req : HealthRecordRequest, actor) : StudentHealthRecordResponse
}

interface IStudentHealthFileService <<interface>> {
  +getFile(studentId : UUID, actor : AuthUser) : StudentHealthFileResponse
  +appendClinical(studentId : UUID, req : ClinicalUpdateRequest, medical : AuthUser) : StudentHealthFileResponse
  +getHealthRecord(studentId : UUID, actor : AuthUser) : StudentHealthRecordResponse
  +upsertHealthRecord(studentId : UUID, req : HealthRecordRequest, actor : AuthUser) : StudentHealthRecordResponse
}

class StudentHealthFileService <<control>> {
  -clinicalRepo : StudentClinicalFileRepository
  -recordRepo : StudentHealthRecordRepository
  -allergyRepo : AllergyDeclarationRepository
  -historyService : IHealthHistoryService
  +getFile(studentId, actor) : StudentHealthFileResponse
  +appendClinical(studentId, req, medical) : StudentHealthFileResponse
  +getHealthRecord(studentId, actor) : StudentHealthRecordResponse
  +upsertHealthRecord(studentId, req, actor) : StudentHealthRecordResponse
  -computeBmi(heightCm, weightKg) : BigDecimal
}

interface StudentClinicalFileRepository <<repository>> {
  +findByStudentId(studentId : UUID) : Optional<StudentClinicalFile>
  +save(entity : StudentClinicalFile) : StudentClinicalFile
}

interface StudentHealthRecordRepository <<repository>> {
  +findByStudentId(studentId : UUID) : Optional<StudentHealthRecord>
  +save(entity : StudentHealthRecord) : StudentHealthRecord
}

interface IHealthHistoryService <<interface>> {
  +append(studentId : UUID, type : HistoryType, event : String, byUserId : UUID) : void
}

class StudentClinicalFile <<entity>> {
  +id : UUID <<PK>>
  +studentId : UUID <<FK, unique>>
  +medicalConditions : String
  +medications : String
  +emergencyContact : String
  +emergencyInstructions : String
  +updatedAt : Instant
}

class StudentHealthRecord <<entity>> {
  +id : UUID <<PK>>
  +studentId : UUID <<FK, unique>>
  +heightCm : BigDecimal
  +weightKg : BigDecimal
  +bloodType : String
  +status : DeclarationStatus
  +proofFileId : UUID
  +updatedAt : Instant
}

class ClinicalUpdateRequest <<DTO>> {
  +medicalConditions : String
  +medications : String
  +emergencyContact : String
  +emergencyInstructions : String
}

class StudentHealthFileResponse <<DTO>> {
  +studentId : String
  +medicalConditions : String
  +medications : String
  +emergencyContact : String
  +emergencyInstructions : String
  +medicalDocuments : List<String>
}

class HealthRecordRequest <<DTO>> {
  +heightCm : BigDecimal
  +weightKg : BigDecimal
  +bloodType : String
}

class StudentHealthRecordResponse <<DTO>> {
  +studentId : String
  +heightCm : BigDecimal
  +weightKg : BigDecimal
  +bmi : BigDecimal
  +bloodType : String
  +status : DeclarationStatus
}

StudentHealthFileController --> IStudentHealthFileService : uses
IStudentHealthFileService <|.. StudentHealthFileService
StudentHealthFileService --> StudentClinicalFileRepository : reads/updates
StudentHealthFileService --> StudentHealthRecordRepository : reads/updates
StudentHealthFileService --> IHealthHistoryService : appends CLINICAL_NOTE event
StudentHealthFileService ..> ClinicalUpdateRequest : consumes (only 4 fields)
StudentHealthFileService ..> HealthRecordRequest : consumes
StudentHealthFileService ..> StudentHealthFileResponse : produces
StudentHealthFileService ..> StudentHealthRecordResponse : produces (bmi derived)
StudentClinicalFileRepository ..> StudentClinicalFile : manages
StudentHealthRecordRepository ..> StudentHealthRecord : manages
@enduml
```

**Description:**
`appendClinical()` accepts **only** `medicalConditions`, `medications`, `emergencyContact`, `emergencyInstructions` — any other field (e.g. an attempt to set `allergies` or `healthStatus` through this endpoint) is rejected with `400 STU-094`, because those values are owned exclusively by the allergy/special-meal review flows (§2, §3). `getFile()` still returns a derived `medicalDocuments[]` array (joined from `allergy_declarations.proof_file_id`) for display purposes, but that array is read-only through this service — documents can only be attached via a declaration.

---

### 4.2 Sequence Diagram — Update Clinical Record (UC-094, BR-172)

```plantuml
@startuml MF03_UpdateClinical
title Sequence Diagram: Update Clinical Record

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

autonumber

actor "Medical Staff" as MS
boundary ":StudentHealthFileUI" as UI
control ":StudentHealthFileController" as C
control ":StudentHealthFileService" as S
entity ":StudentClinicalFileRepository" as R
control ":HealthHistoryService" as H
database ":PostgreSQL" as DB

MS -> UI : open the student health file, edit the 4 clinical fields,\nclick "Save"
activate UI
UI -> C : PATCH /api/v1/students/{id}/health-file\n{ medicalConditions, medications,\n emergencyContact, emergencyInstructions }
activate C
C -> C : AuthUser{userId, role=MedicalStaff, schoolId} <- JWT
C -> S : appendClinical(studentId, ClinicalUpdateRequest, actor)
activate S

S -> S : reject unknown fields\n(allergies, healthStatus, specialDiet...)

alt A field outside the allowed scope is present (invariant C8)
  S --> C : throw ValidationException(STU-094)
  C --> UI : 400 { code: "STU-094" }
  UI --> MS : show "Only the 4 clinical fields may be edited."
else Only the 4 clinical fields
  S -> R : findByStudentId(studentId)
  activate R
  R -> DB : SELECT * FROM student_clinical_files\nWHERE student_id = ?
  activate DB
  DB --> R : ResultSet { row | empty }
  deactivate DB
  R --> S : Optional<StudentClinicalFile>
  deactivate R

  alt No clinical file yet
    S -> R : save(new StudentClinicalFile{studentId, ...})
    activate R
    R -> DB : INSERT INTO student_clinical_files (...)
    activate DB
    DB --> R : 1 row inserted
    deactivate DB
    R --> S : StudentClinicalFile{id}
    deactivate R
  else Existing file
    S -> R : save(file{medicalConditions, medications,\n emergencyContact, emergencyInstructions})
    activate R
    R -> DB : UPDATE student_clinical_files SET ... WHERE student_id = ?
    activate DB
    DB --> R : 1 row updated
    deactivate DB
    R --> S : StudentClinicalFile{id, updatedAt}
    deactivate R
  end

  S -> H : append(studentId, CLINICAL_NOTE,\n"Clinical record updated", actor.userId)
  activate H
  H -> DB : INSERT INTO health_history_events (...)
  activate DB
  DB --> H : 1 row inserted
  deactivate DB
  H --> S : void
  deactivate H

  S --> C : StudentHealthFileResponse{ studentId, medicalConditions,\n medications, emergencyContact, emergencyInstructions,\n medicalDocuments[] }
  deactivate S
  C --> UI : 200 OK { StudentHealthFileResponse } (MSG-036)
  deactivate C
  UI --> MS : show the updated health file
  deactivate UI
end

@enduml
```

**Description:**
The service explicitly rejects any field outside the four clinical attributes before touching the repository, so a caller cannot accidentally (or intentionally) flip `healthStatus` or `allergies` through this narrower endpoint — those remain gated by the declaration review flows (§2.3, §3.2). Every update, not just the first one, appends a new `health_history_events` row rather than overwriting the previous state, keeping the full edit history available for UC-146.

---

## 5. Health History (Audit Trail)

### 5.1 Class Diagram

```plantuml
@startuml MF03_HealthHistory_ClassDiagram
title Design-level Class Diagram: Health History (Append-only Audit Trail)

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

class HealthHistoryController <<control>> {
  -historyService : IHealthHistoryService
  +timeline(studentId, params, actor) : PagedResponse<HealthHistoryEventResponse>
}

interface IHealthHistoryService <<interface>> {
  +timeline(studentId : UUID, params, actor : AuthUser) : PagedResponse<HealthHistoryEventResponse>
  +append(studentId : UUID, type : HistoryType, event : String, byUserId : UUID) : void
}

class HealthHistoryService <<control>> {
  -eventRepo : HealthHistoryEventRepository
  -referenceResolver : ReferenceResolver
  +timeline(studentId, params, actor) : PagedResponse<HealthHistoryEventResponse>
  +append(studentId, type, event, byUserId) : void
  -assertDataScope(studentId, actor) : void
}

interface HealthHistoryEventRepository <<repository>> {
  +findByStudentIdOrderByAtDesc(studentId : UUID, pageable) : Page<HealthHistoryEvent>
  +save(entity : HealthHistoryEvent) : HealthHistoryEvent
}

class ReferenceResolver <<common>> {
  +displayName(userId : UUID) : String
}

class HealthHistoryEvent <<entity>> {
  +id : UUID <<PK>>
  +studentId : UUID <<FK>>
  +at : Instant
  +type : HistoryType
  +event : String
  +byUserId : UUID
}

enum HistoryType <<enumeration>> {
  DECLARED
  VERIFIED
  CLINICAL_NOTE
  INCIDENT
  MEDICATION_UPDATE
}

class HealthHistoryEventResponse <<DTO>> {
  +id : String
  +studentId : String
  +at : Instant
  +type : HistoryType
  +event : String
  +by : String
}

HealthHistoryController --> IHealthHistoryService : uses
HealthHistoryController ..> HealthHistoryEventResponse : returns
IHealthHistoryService <|.. HealthHistoryService
HealthHistoryService --> HealthHistoryEventRepository : reads (timeline) / inserts (append)
HealthHistoryService --> ReferenceResolver : resolves actor display name
HealthHistoryService ..> HealthHistoryEventResponse : produces
HealthHistoryEventRepository ..> HealthHistoryEvent : manages
HealthHistoryEvent "n" --> "1" HistoryType

@enduml
```

**Description:**
`HealthHistoryService.append()` is the single internal choke point every other MF03 service goes through to record an event — there is deliberately no `POST /health-history` endpoint exposed to clients (BR-172, ADR-M03-4). `HealthHistoryController.timeline()` is strictly read-only (UC-146, UC-051); the underlying `health_history_events` table has database-level `RULE`s that turn `UPDATE`/`DELETE` into no-ops and the application role has those grants revoked, so immutability does not depend on every future engineer remembering not to call `save()` on an existing row.

---

### 5.2 Sequence Diagram — View Student Health History Timeline (UC-146, BR-172)

```plantuml
@startuml MF03_HealthHistoryTimeline
title Sequence Diagram: View Student Health History Timeline

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

autonumber

actor "Medical Staff" as MS
boundary ":HealthHistoryTimelineUI" as UI
control ":HealthHistoryController" as C
control ":HealthHistoryService" as S
entity ":HealthHistoryEventRepository" as R
control ":ReferenceResolver" as REF
database ":PostgreSQL" as DB

MS -> UI : open the student's "Health history" tab
activate UI
UI -> C : GET /api/v1/health-history\n?studentId={id}&page=1&pageSize=8
activate C
C -> C : AuthUser{userId, role, schoolId} <- JWT
C -> S : timeline(studentId, params, actor)
activate S

S -> S : assertDataScope(studentId, actor)\n(Medical Staff: same school; Parent: linked child only)

alt Outside the data scope
  S --> C : throw ForbiddenException(STU-005)
  C --> UI : 403 { code: "STU-005" }
  UI --> MS : show "You do not have access to this record."
else Within scope
  S -> R : findByStudentIdOrderByAtDesc(studentId, pageable)
  activate R
  R -> DB : SELECT * FROM health_history_events\nWHERE student_id = ? ORDER BY at DESC\nLIMIT ? OFFSET ?
  activate DB
  DB --> R : ResultSet { rows, totalCount }
  deactivate DB
  R --> S : Page<HealthHistoryEvent>
  deactivate R

  loop for each event
    S -> REF : displayName(byUserId)
    activate REF
    REF --> S : String by
    deactivate REF
  end

  S --> C : PagedResponse<HealthHistoryEventResponse>\n{ items[](at, type, event, by), total, page,\n pageSize, pageCount }
  deactivate S
  C --> UI : 200 OK { PagedResponse }
  deactivate C
  UI --> MS : render the timeline, newest first
  deactivate UI
end

@enduml
```

**Description:**
The timeline endpoint is a plain, scoped read: Medical Staff see any student in their school, Parents see only their linked children. Because the table is append-only, the response always reflects the true, unaltered chronology of declarations, verifications, clinical notes, and incidents for a student — there is no code path, authorized or not, that can rewrite history after the fact.

---

## 6. State Machines

### 6.1 Declaration Lifecycle — AllergyDeclaration / ChronicConditionDeclaration (BR-031)

```plantuml
@startuml MF03_DeclarationState
title State Diagram: Declaration Lifecycle

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

[*] --> PENDING : submit() [SEVERE => proofFile]

PENDING --> VERIFIED : verify() [proof requirement met]
PENDING --> NEEDS_INFO : requestInfo() [reviewNote required]
NEEDS_INFO --> PENDING : resubmit()
VERIFIED --> [*] : view-only — no re-approval

@enduml
```

**Description:**
`VERIFIED` is a terminal, one-way state reachable only through explicit Medical Staff review — nothing in MF03 ever un-verifies a declaration through the UI. Any subsequent correction (e.g. discovering the allergen was misrecorded) is captured as a brand-new history event rather than a mutation of the existing row, preserving the append-only guarantee (BR-172) end to end.

---

### 6.2 Request Lifecycle — SpecialMealRequest (UC-078, BR-065)

```plantuml
@startuml MF03_SpecialMealState
title State Diagram: Special Meal Request Lifecycle

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

[*] --> PENDING : submit()

PENDING --> APPROVED : approve() [standard dietaryType — BR-220]
PENDING --> NEEDS_INFO : approve() [outside BR-066 catalog]\nor requestInfo()
PENDING --> REJECTED : reject() [reason required]
NEEDS_INFO --> PENDING : resubmit() [add medical justification]

APPROVED --> [*]
REJECTED --> [*]


@enduml
```

**Description:**
The distinguishing rule versus a typical approve/reject flow is BR-220: an out-of-catalog `dietaryType` is deliberately routed to `NEEDS_INFO` rather than `REJECTED`, since BR-066's diet catalog is open-ended and a novel value may simply need supporting medical justification rather than outright denial. Only the `APPROVED` transition has downstream consequences — it is the sole event that obligates MF05 to build a dedicated meal plan.

---

### 6.3 Student Lifecycle — `Student.status` (UC-025 / UC-099, BR-025)

```plantuml
@startuml MF03_StudentState
title State Diagram: Student Enrollment Lifecycle

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

[*] --> ACTIVE : create() [room available BR-025, >=1 parent BR-030]

ACTIVE --> SUSPENDED : suspend() [reason required]
SUSPENDED --> ACTIVE : reactivate() [classroom has room BR-025]
ACTIVE --> TRANSFERRED : transfer() [to another school/class]
SUSPENDED --> TRANSFERRED : transfer()

TRANSFERRED --> [*]

@enduml
```

**Description:**
A student is born `ACTIVE` only after both gates pass — classroom capacity (BR-025) and at least one linked parent (BR-030). `SUSPENDED` is reversible and is the state used for temporary leave: the roster hides the student from serving lists (MF09) while every declaration, clinical field, and history row stays intact. `TRANSFERRED` is terminal because MF03 never deletes a student record — the health history must remain readable after departure (BR-172), so "removal" is modelled as a final state rather than a `DELETE`.

---

### 6.4 Health Record Lifecycle — `StudentHealthRecord.status` (UC-094, CF-M03-9)

```plantuml
@startuml MF03_HealthRecordState
title State Diagram: Student Health Record Lifecycle

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

[*] --> NO_RECORD : student just created

NO_RECORD --> PENDING : upsertHealthRecord()\n[heightCm 50..220, weightKg 5..150]
PENDING --> VERIFIED : verify() by Medical Staff
PENDING --> NEEDS_INFO : requestInfo() [proof missing]
NEEDS_INFO --> PENDING : upsertHealthRecord() [re-submitted]
VERIFIED --> PENDING : upsertHealthRecord()\n[re-measured height/weight -> needs re-verification]


@enduml
```

**Description:**
Unlike a declaration, a health record is *re-measurable*: a new height/weight in a later school term pushes a `VERIFIED` record back to `PENDING` rather than creating a second row, because `student_health_records` is 1-1 with the student. `NO_RECORD` is an explicit modelled state, not an accident — the API answers `204 No Content` instead of `404`, which is what lets the FE distinguish "not measured yet" from "student does not exist". Each transition still appends its own `health_history_events` row, so the measurement history survives even though the record itself is overwritten in place.

---

### 6.5 Import Session Lifecycle — Bulk Import (UC-010, ADR-M03-6)

```plantuml
@startuml MF03_ImportSessionState
title State Diagram: Bulk Import Session Lifecycle

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

[*] --> VALIDATING : upload file (.xlsx/.csv)

VALIDATING --> VALIDATED : parsed [invalidCount = 0]
VALIDATING --> PARTIALLY_INVALID : parsed [invalidCount > 0]
PARTIALLY_INVALID --> VALIDATING : re-upload corrected file
VALIDATED --> COMMITTING : commit(importId)

COMMITTING --> COMMITTED : COMMIT [all rows written]
COMMITTING --> ROLLED_BACK : ROLLBACK [any row fails]
ROLLED_BACK --> VALIDATING : re-upload corrected file

VALIDATED --> EXPIRED : session expired (importId TTL)
PARTIALLY_INVALID --> EXPIRED : session expired

COMMITTED --> [*]
EXPIRED --> [*]

@enduml
```

**Description:**
The import session is the state that makes the two-phase design visible: no student row exists until `COMMITTING` succeeds, so `PARTIALLY_INVALID` is a safe, repeatable place for the operator to iterate on the spreadsheet. `ROLLED_BACK` and `COMMITTED` are mutually exclusive by construction (single transaction), which is precisely the guarantee the row-by-row mock could not offer. `EXPIRED` exists because `importId` only holds a parsed, in-memory preview — an abandoned preview must not stay committable indefinitely.

---

*Document derived from TDS `SFMS-MF03-IMP-001 v1.1` (Approved 2026-08-02). The five Section 6 state machines and the DBML schema mirror the entity model in TDS §5.1–5.2; all business rule references (BR-xxx) and error codes (STU-xxx) are unchanged from the source TDS. Sequence diagrams use UML robustness stereotypes — `boundary` (UI), `control` (controller/service/port), `entity` (repository), `database` — with `:ObjectName` lifelines, explicit return messages, and `autonumber`.*
