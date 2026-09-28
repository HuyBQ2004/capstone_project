# MF10 — Feedback & Announcement Management
## Design Diagrams (Database, Class, Sequence, State)

> **Stack:** Spring Boot 4.1.x (Java 25) · PostgreSQL 16 · Flyway · Spring Security + JWT · MapStruct-free manual mappers · STOMP/WebSocket broadcast · FCM push
> **Bounded Context:** Meal Feedback · School Announcements · In-App/Push Notification — **one** Java package `com.sfms.mf10` (`M10-D03`)
> **Based on:** TDS `TDS-MF10_Backend.md` (`M10-D01` … `M10-D14`) · Flyway `V18__mf10_feedback_announcement.sql` + `V19__seed_mf10.sql` + `V34__mf10_device_tokens.sql` · merged code under `04_Development/Backend/src/main/java/com/sfms/mf10/`

> **Diagram conventions** — class and state charts follow `MF04_Supplier_Partner_Management/Design-Diagram-Review-Guide.md` v1.2:
> - **Class diagrams** — Rational-Rose palette, `linetype ortho`, **flat layout with no `package` blocks** (layers are shown by stereotype and by `[hidden]` ranking edges only). One stereotype per class, no aggregation `o--`, no `JpaRepository`, no getters/setters. Rendered aspect ratio kept inside 0.7 – 1.2.
> - **Sequence diagrams** — UML *robustness* lifelines: `actor` (human) → `boundary` (screen or subscribed browser) → `control` (controller, service, policy, port) → `entity` (repository) → `database` (PostgreSQL, object storage). Every call shows its return message and every lifeline is explicitly `activate`/`deactivate`d. Fail-fast guards are self-messages carrying `⇒ status CODE (MSG-xxx)`; `alt` is reserved for real forks and never nests deeper than two. No `note` blocks — anything worth saying is in the message label or the description.
> - **State charts** — drawn only for an enum whose values the code actually writes from an explicit business action (guide section 5.1b). Minimal form: short verb labels, no `entry`/`do`, no notes, no guards on the arrows.

---

## 1. Database Architecture (DBML)

> Reflects `V18__mf10_feedback_announcement.sql` (10 tables) + `V34__mf10_device_tokens.sql` (1 table) = **11 tables**. Cross-module references that have a hard FK in the current schema (`meal_session_id → student_distributions.id` from MF09, `dish_id → dishes.id` from MF05, `student_id`/`classroom_id` from MF03/MF02) are modeled as real foreign keys. All user references point to **`user_accounts.id`** — the table `users` has never existed (`M10-D04`). `notifications.code` is the **only** exception to the module's `(school_id, code)` uniqueness pattern, because `school_id` is nullable for platform-wide notices and `UNIQUE` cannot police duplicates across a nullable column (`M10-D01`).

```dbml
// Use DBML to define your database structure
// Docs: https://dbml.dbdiagram.io/docs
// Module: MF10 — Feedback & Announcement Management
// Source: TDS-MF10_Backend.md | Flyway V18 + V19 + V34

Table feedbacks {
  id                    uuid         [pk, default: `gen_random_uuid()`, note: "Internal PK, never exposed in API"]
  code                  varchar(32)  [not null, note: "M10-D01/D12: business code FB-2026-8809; returned in JSON as \"id\""]
  school_id             uuid         [not null, ref: > schools.id, note: "Always derived from JWT, never from request body"]
  submitter_user_id     uuid         [not null, ref: > user_accounts.id, note: "Parent or Teacher who submitted"]
  submitter_role        varchar(10)  [not null, note: "CHECK: PARENT | TEACHER"]
  scope                 varchar(10)  [not null, note: "CHECK: DISH | SESSION | CLASS"]
  student_id            uuid         [null, ref: > students.id, note: "NULL when scope = CLASS"]
  classroom_id          uuid         [null, ref: > classrooms.id]
  class_name            varchar(20)  [not null, note: "Denormalized display label, e.g. \"3A\" — also searchable"]
  meal_date             date         [not null, note: "Date of the meal being reviewed"]
  meal_type             varchar(12)  [not null, note: "CHECK: BREAKFAST | LUNCH | SNACK (common/enums MealType, M10-D09)"]
  meal_session_id       uuid         [not null, ref: > student_distributions.id, note: "ON DELETE RESTRICT; hard FK to MF09 distribution session"]
  dish_id               uuid         [null, ref: > dishes.id, note: "NULL when scope = SESSION"]
  dish_name             varchar(160) [null, note: "Denormalized display/search field"]
  rating                int          [not null, note: "CHECK: 1..5"]
  tags                  jsonb        [not null, default: '[]', note: "BR-140: quality-concern tags"]
  comment               text         [null, note: "May contain health-related text — never logged verbatim (§4.3)"]
  portion_size          varchar(12)  [null, note: "TOO_SMALL | ADEQUATE | TOO_LARGE — Teacher-only field (UC-086)"]
  illness_keyword       boolean      [not null, default: false, note: "BR-142: server-derived, client value ignored (M10-D14)"]
  submitted_at          timestamptz  [not null, default: `now()`, note: "Business timestamp (BR-139 window), distinct from created_at"]
  deleted_at            timestamptz  [null, note: "Soft delete (UC-046 AF2); excluded from all reads except audit"]
  created_at            timestamptz  [not null, default: `now()`, note: "BaseEntity audit column (M10-D02)"]
  updated_at            timestamptz  [not null, default: `now()`]
  created_by            varchar(180) [null]
  updated_by            varchar(180) [null]

  indexes {
    (school_id, meal_date) [name: "ix_fb_school_date"]
    submitter_user_id [name: "ix_fb_submitter"]
    (school_id, illness_keyword) [name: "ix_fb_illness"]
    classroom_id [name: "ix_fb_classroom"]
    (school_id, code) [unique, name: "uq_fb_school_code"]
    (submitter_user_id, meal_session_id, dish_id, student_id) [unique, name: "uq_fb_once"]
  }

  Note: "One feedback per (submitter x meal session x dish x student) — uq_fb_once. Editing/deleting is only allowed within 24h of the meal being served (BR-139), enforced in FeedbackWindowPolicy rather than the DB because it depends on now() and on MF09's served_at. ck_fb_low_rating_needs_tag enforces BR-140 at DB level: a Parent rating below 3 requires at least one tag."
}

Table feedback_responses {
  id                  uuid         [pk, default: `gen_random_uuid()`]
  code                varchar(32)  [not null, note: "M10-D01: FBR-2026-001"]
  school_id           uuid         [not null, ref: > schools.id]
  feedback_id         uuid         [not null, ref: > feedbacks.id]
  responder_user_id   uuid         [not null, ref: > user_accounts.id, note: "Service Caterers actor who replied"]
  content             text         [not null, note: "CHECK: non-blank (UC-163 EX1)"]
  responded_at        timestamptz  [not null, default: `now()`]
  late_response       boolean      [not null, default: false, note: "responded_at > feedback.submitted_at + 48h (UC-163 EX2) — recorded, never blocked"]
  created_at          timestamptz  [not null, default: `now()`]
  updated_at          timestamptz  [not null, default: `now()`]
  created_by          varchar(180) [null]
  updated_by          varchar(180) [null]

  indexes {
    feedback_id [name: "ix_fbr_feedback"]
    (school_id, code) [unique, name: "uq_fbr_school_code"]
  }

  Note: "BR-143: a caterer may reply any number of times (append semantics); each row is one reply, ordered by responded_at."
}

Table feedback_attachments {
  id                    uuid         [pk, default: `gen_random_uuid()`]
  code                  varchar(32)  [not null, note: "FBATT-2026-001"]
  school_id             uuid         [not null, ref: > schools.id]
  feedback_id           uuid         [not null, ref: > feedbacks.id, note: "ON DELETE CASCADE"]
  storage_key           varchar(255) [not null, note: "Private key issued by common StorageService — never a public URL (M10-D11)"]
  original_name         varchar(255) [not null]
  content_type          varchar(100) [not null, note: "CHECK: image/jpeg | image/png | image/webp | application/pdf — verified by MAGIC BYTES, client Content-Type never trusted"]
  size_bytes            bigint       [not null, note: "CHECK: 0 < size <= 5242880 (5 MB)"]
  uploaded_by_user_id   uuid         [not null, ref: > user_accounts.id]
  created_at            timestamptz  [not null, default: `now()`]
  updated_at            timestamptz  [not null, default: `now()`]
  created_by            varchar(180) [null]
  updated_by            varchar(180) [null]

  indexes {
    feedback_id [name: "ix_fbatt_feedback"]
    (school_id, code) [unique, name: "uq_fbatt_school_code"]
  }

  Note: "Photo evidence attached to a feedback item. Downloaded only through an authorized, audited endpoint — never a pre-signed/public link (§4.3)."
}

Table illness_keywords {
  id           uuid         [pk, default: `gen_random_uuid()`]
  keyword      varchar(60)  [not null, unique, note: "BR-142: business-configurable list, not hardcoded in Java"]
  active       boolean      [not null, default: true]
  created_at   timestamptz  [not null, default: `now()`]
  updated_at   timestamptz  [not null, default: `now()`]
  created_by   varchar(180) [null]
  updated_by   varchar(180) [null]

  Note: "Configuration table, no business code (never addressed by a path param). Matching is diacritic- and case-insensitive, performed in the Java layer (java.text.Normalizer) — no pg_trgm/unaccent extension required."
}

Table announcements {
  id               uuid         [pk, default: `gen_random_uuid()`]
  code             varchar(32)  [not null, note: "M10-D01/D12: ANN-2026-113"]
  school_id        uuid         [not null, ref: > schools.id]
  title            varchar(255) [not null]
  content          text         [not null, note: "HTML sanitized before persisting — largest XSS surface in the system (§4.3)"]
  audience_codes   jsonb        [not null, default: '[]', note: "Normalized filter codes, e.g. PARENT_GRADE_3, TEACHER (M10-D08)"]
  audience_label   varchar(255) [not null, note: "Derived display string — read-only for clients (M10-D14)"]
  status           varchar(12)  [not null, default: 'DRAFT', note: "CHECK: DRAFT | PUBLISHED"]
  urgent           boolean      [not null, default: false, note: "BR-138: bypasses quiet hours, dispatched within 60s"]
  author_user_id   uuid         [not null, ref: > user_accounts.id, note: "Business field \"who authored\" — distinct from BaseEntity.created_by (D9)"]
  author_name      varchar(180) [not null]
  published_at     timestamptz  [null, note: "NULL while DRAFT (never an empty string, M10-D10); set once on first publish"]
  created_at       timestamptz  [not null, default: `now()`]
  updated_at       timestamptz  [not null, default: `now()`]
  created_by       varchar(180) [null]
  updated_by       varchar(180) [null]

  indexes {
    (school_id, status, published_at) [name: "ix_ann_school_status"]
    audience_codes [name: "ix_ann_audience", note: "GIN index for JSONB containment filtering"]
    (school_id, code) [unique, name: "uq_ann_school_code"]
  }

  Note: "ck_ann_published_at enforces the state invariant at DB level: PUBLISHED requires published_at NOT NULL, DRAFT requires NULL. PUBLISHED -> DRAFT is refused by the service with FBA-STATE — see state machine §6.1."
}

Table announcement_revisions {
  id                    uuid         [pk, default: `gen_random_uuid()`]
  announcement_id       uuid         [not null, ref: > announcements.id]
  revised_by_user_id    uuid         [not null, ref: > user_accounts.id, note: "BR-144: identity of the actor, not just a timestamp"]
  revised_by_name       varchar(180) [not null]
  note                  varchar(500) [not null]
  revised_at            timestamptz  [not null, default: `now()`]
  created_at            timestamptz  [not null, default: `now()`]
  updated_at            timestamptz  [not null, default: `now()`]
  created_by            varchar(180) [null]
  updated_by            varchar(180) [null]

  indexes {
    (announcement_id, revised_at) [name: "ix_annrev_ann"]
  }

  Note: "APPEND-ONLY (BR-144, ADR-M10-3): DB RULEs r_annrev_no_update / r_annrev_no_delete silently discard any UPDATE/DELETE, so edit history cannot be rewritten even by a buggy caller. No business code — read only nested inside the announcement detail response."
}

Table notifications {
  id                 uuid         [pk, default: `gen_random_uuid()`]
  code               varchar(32)  [not null, unique, note: "M10-D01 exception: UNIQUE on code alone, because school_id is nullable for platform-wide notices"]
  school_id          uuid         [null, ref: > schools.id, note: "NULL = platform-wide (e.g. maintenance notice)"]
  audience           varchar(8)   [not null, note: "CHECK: ALL | ROLES"]
  recipient_roles    jsonb        [null, note: "Kebab-case RoleKey list (M10-D08), meaningful only when audience = ROLES"]
  type               varchar(20)  [not null, note: "CHECK: one of the 7 shared NotificationType values (M10-D07)"]
  title              varchar(255) [not null]
  content            text         [not null]
  link               varchar(255) [null, note: "Internal path only (starts with /) — never an external URL or javascript: scheme (§4.3)"]
  link_label         varchar(80)  [null]
  sent_at            timestamptz  [not null, default: `now()`, note: "Business timestamp"]
  created_at         timestamptz  [not null, default: `now()`]
  updated_at         timestamptz  [not null, default: `now()`]
  created_by         varchar(180) [null]
  updated_by         varchar(180) [null]

  indexes {
    (school_id, sent_at) [name: "ix_ntf_school_sent"]
  }

  Note: "One row per dispatch EVENT, not one per recipient (audience-based, no fan-out). `critical` is NOT a column — derived at runtime from type IN (INCIDENT_ALERT, FOOD_RECALL, ALLERGY_ALERT) per M10-D07/BR-138."
}

Table notification_reads {
  id                uuid         [pk, default: `gen_random_uuid()`]
  notification_id   uuid         [not null, ref: > notifications.id]
  user_id           uuid         [not null, ref: > user_accounts.id]
  read_at           timestamptz  [not null, default: `now()`]
  created_at        timestamptz  [not null, default: `now()`]
  updated_at        timestamptz  [not null, default: `now()`]
  created_by        varchar(180) [null]
  updated_by        varchar(180) [null]

  indexes {
    (notification_id, user_id) [unique, name: "uq_ntfread"]
  }

  Note: "CF-M10-10: read state is per-user, not a global flag on notifications, so one notification row can be independently marked read by every recipient. markRead() is idempotent via uq_ntfread."
}

Table notification_preferences {
  user_id           uuid         [pk, ref: > user_accounts.id, note: "One row per user — PK is the user id, not a generated UUID"]
  type_enabled      jsonb        [not null, default: '{}', note: "Per-NotificationType opt-in map, exactly the 7 D6 keys"]
  email_fallback    boolean      [not null, default: true, note: "BR-159"]
  quiet_start       time         [not null, default: '22:00', note: "BR-158 default quiet-hours start"]
  quiet_end         time         [not null, default: '06:00']
  created_at        timestamptz  [not null, default: `now()`]
  updated_at        timestamptz  [not null, default: `now()`]
  created_by        varchar(180) [null]
  updated_by        varchar(180) [null]

  Note: "UC-088. Quiet hours AND the per-type opt-out are both ignored for critical notifications (BR-138/158)."
}

Table notification_deliveries {
  id               uuid         [pk, default: `gen_random_uuid()`]
  notification_id  uuid         [not null, ref: > notifications.id]
  user_id          uuid         [not null, ref: > user_accounts.id]
  channel          varchar(8)   [not null, note: "CHECK: PUSH | EMAIL"]
  attempt          int          [not null, default: 0, note: "CHECK: attempt <= 3 (BR-159 retry cap)"]
  status           varchar(16)  [not null, note: "CHECK: QUEUED | DEFERRED | SENT | FAILED | FALLBACK_EMAIL"]
  next_retry_at    timestamptz  [null]
  last_error       varchar(500) [null]
  created_at       timestamptz  [not null, default: `now()`]
  updated_at       timestamptz  [not null, default: `now()`]
  created_by       varchar(180) [null]
  updated_by       varchar(180) [null]

  indexes {
    (status, next_retry_at) [name: "ix_ntfdlv_retry"]
  }

  Note: "BR-159: up to 3 retries with backoff before falling back to email. Table exists since V18; the DeliveryRetryScheduler pipeline is DESIGNED but NOT yet wired in the MVP build — it needs the per-(school, role) user list that MF01 has not exposed yet (TDS §2.5 debt). See state machine §6.3."
}

Table device_tokens {
  id             uuid         [pk, default: `gen_random_uuid()`]
  user_id        uuid         [not null, ref: > user_accounts.id]
  token          varchar(400) [not null, unique, note: "uq_devtok_token — one physical device/browser maps to one user at a time"]
  platform       varchar(8)   [not null, note: "CHECK: ANDROID | IOS | WEB"]
  last_used_at   timestamptz  [not null, default: `now()`]
  created_at     timestamptz  [not null, default: `now()`]
  updated_at     timestamptz  [not null, default: `now()`]
  created_by     varchar(180) [null]
  updated_by     varchar(180) [null]

  indexes {
    user_id [name: "ix_devtok_user"]
  }

  Note: "V34 — FCM push registry (mobile + web). Logging in as a different account on the same device upserts user_id (handled in DeviceTokenService, not by a DB constraint). No business code: never addressed by a path param."
}

// ============================================================
// External tables — owned by other modules, referenced by id only
// ============================================================

Table schools {
  id uuid [pk]
  Note: "Owned by MF01/MF02. Tenant/school scoping for feedbacks, feedback_responses, feedback_attachments, announcements, notifications."
}

Table user_accounts {
  id uuid [pk]
  Note: "Owned by MF01. Every *_user_id column in MF10 (submitter, responder, uploader, author, revisor, reader, recipient) points here."
}

Table students {
  id uuid [pk]
  Note: "Owned by MF03. feedbacks.student_id — NULL when scope = CLASS."
}

Table classrooms {
  id uuid [pk]
  Note: "Owned by MF02. feedbacks.classroom_id."
}

Table dishes {
  id uuid [pk]
  Note: "Owned by MF05. feedbacks.dish_id — NULL when scope = SESSION."
}

Table student_distributions {
  id uuid [pk]
  Note: "Owned by MF09. feedbacks.meal_session_id — ON DELETE RESTRICT hard FK to the distribution session."
}

// ── Relationships summary ──────────────────────────────────────────────────
// feedbacks.school_id                     → schools.id
// feedbacks.submitter_user_id             → user_accounts.id
// feedbacks.student_id                    → students.id (nullable, MF03)
// feedbacks.classroom_id                  → classrooms.id (nullable, MF02)
// feedbacks.meal_session_id               → student_distributions.id (MF09, ON DELETE RESTRICT)
// feedbacks.dish_id                       → dishes.id (MF05, nullable)
// feedback_responses.feedback_id          → feedbacks.id      (1 : 0..n)
// feedback_attachments.feedback_id        → feedbacks.id      (1 : 0..n, ON DELETE CASCADE)
// announcements.author_user_id            → user_accounts.id
// announcement_revisions.announcement_id  → announcements.id  (1 : 0..n, append-only)
// notifications.school_id                 → schools.id (nullable)
// notification_reads.notification_id      → notifications.id  (1 : 0..n, per-user)
// notification_deliveries.notification_id → notifications.id  (1 : 0..n, per-user/channel)
// notification_preferences.user_id        → user_accounts.id  (1 : 1)
// device_tokens.user_id                   → user_accounts.id  (1 : 0..n)
```

---

## 2. Feature 1 — Submit & Manage Meal Feedback (UC-085, UC-086, UC-046)

### 2.1 Class Diagram

```plantuml
@startuml MF10_F1_ClassDiagram
title Design-level Class Diagram: Submit and Manage Meal Feedback

' ==========================================
' LAYOUT AND RATIONAL ROSE COLOUR SCHEME
' Top-level skinparam only — the nested block form
' is not used in class diagrams (guide section 6).
' ==========================================
skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF

' Vertical layout: web -> service -> repository -> domain
top to bottom direction
hide empty members

' Class / Abstract Class
skinparam classBackgroundColor #F4FBF4
skinparam classBorderColor #2E7D32
skinparam classFontColor #212121
skinparam classFontSize 13
skinparam classHeaderBackgroundColor #4CAF50
skinparam classAttributeFontColor #212121
skinparam classAttributeFontSize 12

' Interface
skinparam interfaceBackgroundColor #f7ed93ff
skinparam interfaceBorderColor #000000ff
skinparam interfaceFontColor #000000ff

' Relationships & Arrows
skinparam arrowColor #162c00ff
skinparam arrowFontColor #162c00ff
skinparam arrowFontSize 11

' Stereotype icons
skinparam stereotypeCBackgroundColor #f6eedcff
skinparam stereotypeCBorderColor #000000ff
skinparam stereotypeEBackgroundColor #f6eedcff
skinparam stereotypeEBorderColor #000000ff
skinparam stereotypeABackgroundColor #f6eedcff
skinparam stereotypeABorderColor #000000ff
skinparam stereotypeIBackgroundColor #f6eedcff
skinparam stereotypeIBorderColor #000000ff

' ────────── mf10.web ──────────
class FeedbackController <<Controller>> {
  +list(query, actor) : PagedResponse
  +getDetail(code, actor) : Object
  +submit(req, actor) : FeedbackOwnerResponse
  +update(code, req, actor) : FeedbackOwnerResponse
  +retract(code, actor) : void
  +eligibleMeals(actor, studentCode) : List
  +uploadAttachment(code, file, actor) : FeedbackAttachmentResponse
  +downloadAttachment(code, attCode, actor) : Resource
}

' ────────── mf10.service ──────────
interface FeedbackService <<Service>> {
  +submit(req : SubmitFeedbackRequest, actor : AuthUser) : FeedbackOwnerResponse
  +update(code : String, req : UpdateFeedbackRequest, actor : AuthUser) : FeedbackOwnerResponse
  +retract(code : String, actor : AuthUser) : void
  +resolveSchoolId(actor : AuthUser) : UUID
}
class FeedbackServiceImpl <<Service>> {
  -feedbackRepository : FeedbackRepository
  -feedbackResponseRepository : FeedbackResponseRepository
  -attachmentRepository : FeedbackAttachmentRepository
  -attachmentSupport : FeedbackAttachmentSupport
  -referenceResolver : ReferenceResolver
  -codeGenerator : CodeGenerator
  -windowPolicy : FeedbackWindowPolicy
  -illnessKeywordDetector : IllnessKeywordDetector
  -notificationPort : NotificationPort
  -storageService : StorageService
}
class FeedbackWindowPolicy <<Component>> {
  +WINDOW : Duration = PT24H
  -referenceResolver : ReferenceResolver
  -clock : Clock
  +assertOpen(mealSessionId : UUID) : Instant
  +editableUntil(servedAt : Instant) : Instant
}
class IllnessKeywordDetector <<Component>> {
  -repository : IllnessKeywordRepository
  +detect(text : String) : boolean
}
class FeedbackAttachmentSupport <<Component>> {
  -storageService : StorageService
  -attachmentRepository : FeedbackAttachmentRepository
  -codeGenerator : CodeGenerator
}
class FeedbackSpec <<Specification>> {
  +SEARCH_FIELDS : List = comment, dishName, className
  +build(query, schoolId, submitterUserId) : Specification
}

' ────────── mf10.repository ──────────
interface FeedbackRepository <<Repository>> {
  +findBySchoolIdAndCodeAndDeletedAtIsNull(schoolId, code) : Optional
  +existsBySubmitterUserIdAndMealSessionIdAndDishIdAndStudentIdAndDeletedAtIsNull(...) : boolean
}
interface FeedbackAttachmentRepository <<Repository>> {
  +findByFeedbackId(feedbackId) : List
  +findByFeedbackIdAndCode(feedbackId, code) : Optional
}
interface IllnessKeywordRepository <<Repository>> {
  +findByActiveTrue() : List
}

' ────────── common: shared kernel ──────────
interface ReferenceResolver <<Port>> {
  +distributionIdByCode(code, schoolId) : Optional
  +servedAtOf(distributionId) : Optional
  +servedMealsWithin(studentId, window) : List
  +studentByCode(code, schoolId) : Optional
  +isParentLinkedToStudent(userId, studentId) : boolean
  +isHomeroomTeacher(classroomId, userId) : boolean
  +dishIdByCode(code) : Optional
}
interface StorageService <<Port>> {
  +store(key : String, file : MultipartFile) : String
  +load(key : String) : Resource
}
interface CodeGenerator <<Port>> {
  +next(scope : String, prefix : String, width : int) : String
}
interface NotificationPort <<Port>> {
  +publish(command : NotificationCommand) : String
}

' ────────── mf10.domain ──────────
abstract class BaseEntity <<MappedSuperclass>> {
  +id : UUID <<PK>>
  +createdAt : Instant
  +updatedAt : Instant
  +createdBy : String
  +updatedBy : String
}
class Feedback <<Entity>> {
  +code : String <<business id>>
  +schoolId : UUID <<FK>>
  +submitterUserId : UUID <<FK>>
  +submitterRole : SubmitterRole
  +scope : FeedbackScope
  +studentId : UUID <<FK>>
  +classroomId : UUID <<FK>>
  +className : String
  +mealDate : LocalDate
  +mealType : MealType
  +mealSessionId : UUID <<FK>>
  +dishId : UUID <<FK>>
  +dishName : String
  +rating : int
  +tags : List
  +comment : String
  +portionSize : PortionSize
  +illnessKeyword : boolean <<derived>>
  +submittedAt : Instant
  +deletedAt : Instant
}
class FeedbackAttachment <<Entity>> {
  +code : String <<business id>>
  +schoolId : UUID <<FK>>
  +feedbackId : UUID <<FK>>
  +storageKey : String
  +originalName : String
  +contentType : String
  +sizeBytes : long
  +uploadedByUserId : UUID <<FK>>
}
class IllnessKeyword <<Entity>> {
  +keyword : String
  +active : boolean
}
enum SubmitterRole {
  PARENT
  TEACHER
}
enum FeedbackScope {
  DISH
  SESSION
  CLASS
}
enum PortionSize {
  TOO_SMALL
  ADEQUATE
  TOO_LARGE
}
enum MealType <<external>> {
  BREAKFAST
  LUNCH
  SNACK
}

' ═══════════ Relationships ═══════════
FeedbackController        ..> FeedbackService            : «use»
FeedbackService           <|.. FeedbackServiceImpl
FeedbackServiceImpl       ..> FeedbackRepository         : «use»
FeedbackServiceImpl       ..> FeedbackAttachmentRepository : «use»
FeedbackServiceImpl       ..> FeedbackWindowPolicy       : «check»
FeedbackServiceImpl       ..> IllnessKeywordDetector     : «screen»
FeedbackServiceImpl       ..> FeedbackAttachmentSupport  : «store»
FeedbackServiceImpl       ..> FeedbackSpec               : «filter»
FeedbackServiceImpl       ..> ReferenceResolver          : «resolve»
FeedbackServiceImpl       ..> CodeGenerator              : «issue code»
FeedbackServiceImpl       ..> NotificationPort           : «alert»
FeedbackServiceImpl       ..> StorageService             : «read file»
FeedbackWindowPolicy      ..> ReferenceResolver          : «served at»
IllnessKeywordDetector    ..> IllnessKeywordRepository   : «use»
FeedbackAttachmentSupport ..> StorageService             : «store»
FeedbackRepository        ..> Feedback                   : «manage»
FeedbackAttachmentRepository ..> FeedbackAttachment      : «manage»
IllnessKeywordRepository  ..> IllnessKeyword             : «manage»

BaseEntity <|-- Feedback
BaseEntity <|-- FeedbackAttachment
BaseEntity <|-- IllnessKeyword

Feedback "1" *-- "0..*" FeedbackAttachment : owns >
Feedback "1" --> "1" SubmitterRole  : sent by >
Feedback "1" --> "1" FeedbackScope  : scoped as >
Feedback "1" --> "0..1" PortionSize : rates >
Feedback "1" --> "1" MealType       : for meal >

' ═══════════════════════════════════════════════════════════════
' Layout: [hidden] links only — invisible on the rendered diagram.
' ═══════════════════════════════════════════════════════════════
FeedbackController  -[hidden]-> FeedbackService
FeedbackService     -[hidden]-> FeedbackServiceImpl
FeedbackServiceImpl -[hidden]-> FeedbackRepository
FeedbackRepository  -[hidden]-> BaseEntity

' each tier is capped at two boxes per row, then split downwards
ReferenceResolver    -[hidden]right-> StorageService
CodeGenerator        -[hidden]right-> NotificationPort
StorageService       -[hidden]-> CodeGenerator

FeedbackWindowPolicy -[hidden]right-> IllnessKeywordDetector
FeedbackAttachmentSupport -[hidden]right-> FeedbackSpec
IllnessKeywordDetector -[hidden]-> FeedbackAttachmentSupport

FeedbackRepository   -[hidden]-> FeedbackAttachmentRepository
FeedbackAttachmentRepository -[hidden]-> IllnessKeywordRepository

Feedback             -[hidden]right-> FeedbackAttachment
IllnessKeyword       -[hidden]right-> SubmitterRole
FeedbackAttachment   -[hidden]-> IllnessKeyword

FeedbackScope        -[hidden]right-> PortionSize
MealType             -[hidden]right-> PortionSize
SubmitterRole        -[hidden]-> FeedbackScope
@enduml
```

**Description.** Screens are not drawn here — they appear as `boundary` lifelines in the sequence charts, which is where the interaction actually happens. `FeedbackController` only adapts HTTP to `FeedbackService`; every rule lives one layer down. `FeedbackServiceImpl` composes four narrow collaborators instead of holding the logic itself: `FeedbackWindowPolicy` owns BR-139 (the 24-hour window, resolved from MF09's `served_at` through the shared `ReferenceResolver`, per `M10-D06`), `IllnessKeywordDetector` owns BR-142 (matching against the `illness_keywords` table, never a hardcoded list), `FeedbackAttachmentSupport` owns the `M10-D11` upload path (magic-byte sniffing plus the common `StorageService`), and `FeedbackSpec` owns the `§2b` list contract with its three fixed `SEARCH_FIELDS`. Persistence goes exclusively through the three repositories. `MealType` carries `<<external>>` because D6 keeps it in `common/enums` — the module must not redeclare it — while `SubmitterRole`, `FeedbackScope` and `PortionSize` are MF10's own. All three entities inherit `BaseEntity`, which is why none of them declares `id` or the four audit columns (D9). The most consequential design point is invisible in the boxes and stated in the notes of the sequence charts instead: one `Feedback` entity is mapped to **two different DTO families**, and the choice is made by the caller's role, which is what turns BR-145 from a UI convention into an API guarantee.

---

### 2.2 Sequence Diagram — Submit Meal Feedback (Happy Path)

```plantuml
@startuml MF10_F1_SubmitFeedback_Happy
title Sequence Diagram: Submit Meal Feedback

' Orthogonal connector mode
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME
' ==========================================
skinparam backgroundColor #FFFFFF

' Stereotype icons
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
' Lifelines by UML robustness stereotype
' ==========================================
skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontSize 13

skinparam sequence {

    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121

}

actor    "Parent"                                  as U
boundary ":SubmitMealFeedbackPage"                 as UI
control  ":FeedbackController"                     as C
control  ":FeedbackService"                        as S
control  ":ReferenceResolver"                      as RR
control  ":FeedbackWindowPolicy"                   as W
control  ":IllnessKeywordDetector"                 as K
control  ":CodeGenerator"                          as CG
control  ":NotificationPort"                       as NP
entity   ":FeedbackRepository"                     as R
database ":PostgreSQL"                             as DB

U -> UI : pick a served meal, set 2 stars,\nchoose a quality tag, write a comment
activate UI
UI -> C : POST /api/v1/feedbacks\nAuthorization Bearer JWT
activate C
C -> S : submit(req, actor)
activate S

S -> S : role switch on actor.role\nneither PARENT nor TEACHER ⇒ 403 FBA-005 (MSG-005)
S -> RR : distributionIdByCode(req.mealSessionId, schoolId)
activate RR
RR -> DB : SELECT id FROM student_distributions WHERE code = ? AND school_id = ?
activate DB
DB --> RR : row
deactivate DB
RR --> S : empty ⇒ 404 FBA-404 (MSG-137)
deactivate RR

S -> W : assertOpen(mealSessionId)
activate W
W -> RR : servedAtOf(mealSessionId)
activate RR
RR -> DB : SELECT served_at FROM student_distributions WHERE id = ?
activate DB
DB --> RR : served_at
deactivate DB
RR --> W : servedAt
deactivate RR
W --> S : now after servedAt plus 24h ⇒ 409 FBA-022 (MSG-022)
deactivate W

S -> RR : studentByCode(req.studentId, schoolId)
activate RR
RR -> DB : SELECT id, classroom_id FROM students WHERE code = ? AND school_id = ?
activate DB
DB --> RR : StudentRef
deactivate DB
RR --> S : empty ⇒ 404 FBA-404
deactivate RR
S -> RR : isParentLinkedToStudent(actor.userId, studentId)
activate RR
RR -> DB : SELECT 1 FROM parent_children WHERE parent_user_id = ? AND student_id = ?
activate DB
DB --> RR : row
deactivate DB
RR --> S : false ⇒ 403 FBA-005
deactivate RR

S -> RR : dishIdByCode(req.dishId)
activate RR
RR -> DB : SELECT id, name FROM dishes WHERE code = ?
activate DB
DB --> RR : dish row
deactivate DB
RR --> S : dishId and dishName
deactivate RR

S -> S : PARENT and rating below 3 and tags empty\n⇒ 422 FBA-140 (MSG-134)
S -> R : existsBySubmitterUserIdAndMealSessionIdAndDishIdAndStudentIdAndDeletedAtIsNull
activate R
R -> DB : SELECT 1 FROM feedbacks WHERE submitter_user_id = ? AND meal_session_id = ?\nAND dish_id = ? AND student_id = ? AND deleted_at IS NULL
activate DB
DB --> R : row
deactivate DB
R --> S : true ⇒ 409 FBA-DUP (MSG-135)
deactivate R

S -> K : detect(req.comment)
activate K
K -> DB : SELECT keyword FROM illness_keywords WHERE active = true
activate DB
DB --> K : keyword list
deactivate DB
K --> S : illnessKeyword true or false
deactivate K
S -> CG : next(mf10_feedback:2026, FB-2026, 4)
activate CG
CG -> DB : INSERT INTO code_sequences ... RETURNING next_value
activate DB
DB --> CG : 8809
deactivate DB
CG --> S : FB-2026-8809
deactivate CG

S -> R : saveAndFlush(feedback)
activate R
R -> DB : INSERT INTO feedbacks ... — ck_fb_low_rating_needs_tag, uq_fb_once
activate DB
DB --> R : 1 row inserted
deactivate DB
R --> S : Feedback persisted
deactivate R

opt illnessKeyword is true — BR-142
  S -> NP : publish(ALLERGY_ALERT to medical-staff and school-admin, MSG-058)
  activate NP
  NP --> S : NTF-2026-482913
  deactivate NP
end

S --> C : FeedbackOwnerResponse
deactivate S
C --> UI : 201 Created (MSG-046)
deactivate C
UI --> U : show the confirmation and the edit deadline
deactivate UI
@enduml
```

**Description.** The guards appear in the order the method runs them, cheapest first: the role switch costs nothing, then the meal-session code is resolved, then the BR-139 window, then the two ownership checks, then the BR-140 field rule, and only last the duplicate probe. Each is drawn as a self-message or as a `⇒ code` on a return arrow rather than an `alt` branch, because none of them is a fork — they are fail-fast exits, and translating them into `alt/else` is exactly what pushed the previous revision of this diagram to four levels of nesting. Every external identifier arriving in the body (`mealSessionId`, `studentId`, `dishId`) is a **business code** that `ReferenceResolver` translates into an internal UUID, so a caller cannot smuggle in a UUID for a row it should not see (`M10-D01`), and `isParentLinkedToStudent` re-checks ownership on the server instead of trusting the payload. The single genuine branch is the last one: the BR-142 alert is an `opt`, because `notifyAfterSubmit` publishes nothing when the comment is clean.

---

### 2.3 Sequence Diagram — Upload and Download Feedback Evidence (M10-D11)

```plantuml
@startuml MF10_F1_Attachment
title Sequence Diagram: Upload and Download Feedback Evidence

' Orthogonal connector mode
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME
' ==========================================
skinparam backgroundColor #FFFFFF

' Stereotype icons
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
' Lifelines by UML robustness stereotype
' ==========================================
skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontSize 13

skinparam sequence {

    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121

}

actor    "Parent"                          as U
boundary ":FeedbackDetailPage"             as UI
control  ":FeedbackController"             as C
control  ":FeedbackService"                as S
control  ":FeedbackAttachmentSupport"      as SUP
control  ":StorageService"                 as ST
control  ":CodeGenerator"                  as CG
entity   ":FeedbackRepository"             as R
entity   ":FeedbackAttachmentRepository"   as AR
database ":PostgreSQL"                     as DB
database ":Object storage"                 as OS

U -> UI : attach a photo to an existing feedback
activate UI
UI -> C : POST /api/v1/feedbacks/FB-2026-8809/attachments\nmultipart file
activate C
C -> S : uploadAttachment(code, file, actor)
activate S
S -> R : findBySchoolIdAndCodeAndDeletedAtIsNull(schoolId, code)
activate R
R -> DB : SELECT * FROM feedbacks WHERE school_id = ? AND code = ? AND deleted_at IS NULL
activate DB
DB --> R : row
deactivate DB
R --> S : empty ⇒ 404 FBA-404 (MSG-137)
deactivate R
S -> S : submitterUserId not equal actor.userId ⇒ 403 FBA-005 (MSG-005)

S -> SUP : store(schoolId, feedbackId, file, actor.userId)
activate SUP
SUP -> SUP : size above 5 MB ⇒ 413 FBA-SIZE
SUP -> SUP : detectContentType by magic bytes\nnot jpeg, png, webp or pdf ⇒ 415 FBA-MEDIA
SUP -> CG : next(mf10_feedback_attachment:2026, FBATT-2026, 3)
activate CG
CG --> SUP : FBATT-2026-001
deactivate CG
SUP -> ST : store(key, file)
activate ST
ST -> OS : write object under feedbacks prefix
activate OS
OS --> ST : stored
deactivate OS
ST --> SUP : storageKey
deactivate ST
SUP -> AR : saveAndFlush(attachment)
activate AR
AR -> DB : INSERT INTO feedback_attachments ...
activate DB
DB --> AR : 1 row inserted
deactivate DB
AR --> SUP : FeedbackAttachment
deactivate AR
SUP --> S : FeedbackAttachment
deactivate SUP
S --> C : FeedbackAttachmentResponse
deactivate S
C --> UI : 201 Created
deactivate C
UI --> U : show the attachment in the thread
deactivate UI

... download — authorised and audited, never a public link ...

U -> UI : open the attachment
activate UI
UI -> C : GET /api/v1/feedbacks/FB-2026-8809/attachments/FBATT-2026-001
activate C
C -> S : downloadAttachment(code, attachmentCode, actor)
activate S
S -> R : findBySchoolIdAndCodeAndDeletedAtIsNull(schoolId, code)
activate R
R --> S : empty ⇒ 404 FBA-404
deactivate R
S -> S : allowed when owner, or SCHOOL_ADMIN,\nor MEDICAL_STAFF and illnessKeyword ⇒ otherwise 403 FBA-005
S -> AR : findBySchoolIdAndFeedbackIdAndCode(schoolId, feedbackId, attachmentCode)
activate AR
AR -> DB : SELECT * FROM feedback_attachments\nWHERE school_id = ? AND feedback_id = ? AND code = ?
activate DB
DB --> AR : row
deactivate DB
AR --> S : empty ⇒ 404 FBA-404
deactivate AR
S -> ST : load(attachment.storageKey)
activate ST
ST -> OS : read object
activate OS
OS --> ST : bytes
deactivate OS
ST --> S : Resource
deactivate ST
S --> C : Resource
deactivate S
C --> UI : 200 OK with the file stream
deactivate C
UI --> U : render the photo
deactivate UI
@enduml
```

**Description.** Evidence photos are the only place MF10 touches object storage, and `M10-D11` routes them through the shared `StorageService` so the module never writes files itself. Two checks live inside `FeedbackAttachmentSupport` rather than in the controller: the 5 MB ceiling and the content type — and the type is decided by **sniffing magic bytes**, never by trusting the client header, so renaming an executable to `.jpg` fails here. The download half is the more interesting one: the file is not a public or pre-signed URL but an authorised endpoint, and its permission test is deliberately broader than the edit rule — the submitter, any School Administration user, and Medical Staff on illness-flagged rows only. That asymmetry exists because a photo attached to a suspected food-poisoning report is evidence the school nurse needs, while an ordinary complaint photo stays with its author.

---

### 2.4 Sequence Diagram — Edit & Retract Feedback (UC-046 AF1/AF2/EX1)

```plantuml
@startuml MF10_F1_EditRetract
title Sequence Diagram: Edit and Retract Feedback

' Orthogonal connector mode
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME
' ==========================================
skinparam backgroundColor #FFFFFF

' Stereotype icons
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
' Lifelines by UML robustness stereotype
' ==========================================
skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontSize 13

skinparam sequence {

    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121

}

actor    "Parent"                     as U
boundary ":FeedbackHistoryPage"       as UI
control  ":FeedbackController"        as C
control  ":FeedbackService"           as S
control  ":FeedbackWindowPolicy"      as W
control  ":ReferenceResolver"         as RR
control  ":IllnessKeywordDetector"    as K
entity   ":FeedbackRepository"        as R
database ":PostgreSQL"                as DB

U -> UI : open my feedback list and choose one entry
activate UI

alt AF1 — edit the entry
  UI -> C : PATCH /api/v1/feedbacks/FB-2026-8809
  activate C
  C -> S : update(code, req, actor)
  activate S
  S -> R : findBySchoolIdAndCodeAndDeletedAtIsNull(schoolId, code)
  activate R
  R -> DB : SELECT * FROM feedbacks WHERE school_id = ? AND code = ? AND deleted_at IS NULL
  activate DB
  DB --> R : row
  deactivate DB
  R --> S : empty ⇒ 404 FBA-404 (MSG-137)
  deactivate R
  S -> S : submitterUserId not equal actor.userId ⇒ 403 FBA-005 (MSG-005)
  S -> W : assertOpen(fb.mealSessionId)
  activate W
  W -> RR : servedAtOf(mealSessionId)
  activate RR
  RR -> DB : SELECT served_at FROM student_distributions WHERE id = ?
  activate DB
  DB --> RR : served_at
  deactivate DB
  RR --> W : servedAt
  deactivate RR
  W --> S : window elapsed ⇒ 409 FBA-022 (MSG-022)
  deactivate W
  S -> S : PARENT and new rating below 3 and tags empty\n⇒ 422 FBA-140 (MSG-134)
  S -> K : detect(req.comment) when the comment changed
  activate K
  K --> S : illnessKeyword recomputed on the server
  deactivate K
  S -> R : saveAndFlush(feedback)
  activate R
  R -> DB : UPDATE feedbacks SET rating = ?, tags = ?, comment = ?,\nillness_keyword = ?, updated_at = now() WHERE id = ?
  activate DB
  DB --> R : 1 row updated
  deactivate DB
  R --> S : Feedback updated
  deactivate R
  S --> C : FeedbackOwnerResponse
  deactivate S
  C --> UI : 200 OK
  deactivate C

else AF2 — retract the entry
  UI -> C : DELETE /api/v1/feedbacks/FB-2026-8809
  activate C
  C -> S : retract(code, actor)
  activate S
  S -> R : findBySchoolIdAndCodeAndDeletedAtIsNull(schoolId, code)
  activate R
  R --> S : empty ⇒ 404 FBA-404
  deactivate R
  S -> S : not the submitter ⇒ 403 FBA-005
  S -> W : assertOpen(fb.mealSessionId)
  activate W
  W --> S : window elapsed ⇒ 409 FBA-022
  deactivate W
  S -> R : saveAndFlush(feedback with deletedAt set)
  activate R
  R -> DB : UPDATE feedbacks SET deleted_at = now() WHERE id = ?\nsoft delete — the row stays for audit
  activate DB
  DB --> R : 1 row updated
  deactivate DB
  R --> S : Feedback retracted
  deactivate R
  S --> C : void
  deactivate S
  C --> UI : 204 No Content
  deactivate C
end
UI --> U : refresh the list
deactivate UI
@enduml
```

**Description.** All three branches share the same two guards in the same order — ownership first (`submitterUserId == actor.userId`), then `FeedbackWindowPolicy` — which is precisely why the diagram shows one policy lifeline serving `submit`, `update` and `retract` rather than three copies of a 24-hour comparison. `PATCH` also re-runs BR-140 and the BR-142 detector on the *new* text, so a parent cannot edit a flagged comment into an unflagged one (or vice versa) without the server re-deciding. `DELETE` sets `deleted_at` instead of removing the row: the record may already be feeding the weekly aggregate or an open incident investigation, so it disappears from every read path while remaining available for audit.

---

### 2.5 Sequence Diagram — View Feedback List & Detail with Anonymization (UC-161/162, BR-145)

```plantuml
@startuml MF10_F1_ViewAnonymized
title Sequence Diagram: Read Feedback with Role-dependent Shape

' Orthogonal connector mode
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME
' ==========================================
skinparam backgroundColor #FFFFFF

' Stereotype icons
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
' Lifelines by UML robustness stereotype
' ==========================================
skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontSize 13

skinparam sequence {

    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121

}

actor    "School Admin or Caterer or Medical Staff" as A
boundary ":SchoolFeedbackListPage"    as UI
control  ":FeedbackController"        as C
control  ":FeedbackService"           as S
control  ":FeedbackSpec"              as SP
entity   ":FeedbackRepository"        as R
database ":PostgreSQL"                as DB

A -> UI : open the feedback board and filter illness flagged
activate UI
UI -> C : GET /api/v1/feedbacks?page=1&pageSize=8&illnessKeyword=true
activate C
C -> S : list(query, actor)
activate S
S -> S : owner is PARENT or TEACHER\nowner sees only rows where submitterUserId equals actor.userId
S -> SP : build(query, schoolId, submitterUserId)
activate SP
SP --> S : Specification with school scope, filters and SEARCH_FIELDS
deactivate SP
S -> R : findAll(spec, pageable sorted by submittedAt desc)
activate R
R -> DB : SELECT * FROM feedbacks WHERE school_id = ? AND illness_keyword = true\nAND deleted_at IS NULL ORDER BY submitted_at DESC LIMIT ? OFFSET ?
activate DB
DB --> R : rows and total
deactivate DB
R --> S : Page of Feedback
deactivate R

alt caller is the submitter role
  S --> C : PagedResponse of FeedbackOwnerListItem
else caller is school side
  S --> C : PagedResponse of FeedbackAnonymizedListItem\nno studentId, student or submitterName — BR-145
end
deactivate S
C --> UI : 200 OK
deactivate C
UI --> A : render class labels only, never a pupil name

... detail — the identity exception of BR-142 ...

A -> UI : open one flagged entry
UI -> C : GET /api/v1/feedbacks/FB-2026-8809
activate C
C -> S : getDetail(code, actor)
activate S
S -> R : findBySchoolIdAndCodeAndDeletedAtIsNull(schoolId, code)
activate R
R -> DB : SELECT * FROM feedbacks WHERE school_id = ? AND code = ? AND deleted_at IS NULL
activate DB
DB --> R : row
deactivate DB
R --> S : empty ⇒ 404 FBA-404 (MSG-137)
deactivate R
S -> S : role not SCHOOL_ADMIN, SERVICE_CATERERS or MEDICAL_STAFF\nand not the submitter ⇒ 403 FBA-005 (MSG-005)
alt caller is the submitter
  S --> C : FeedbackOwnerResponse with full identity
else MEDICAL_STAFF and illnessKeyword is true
  S --> C : FeedbackAnonymizedResponse with revealIdentity true
else any other permitted role
  S --> C : FeedbackAnonymizedResponse with revealIdentity false
end
deactivate S
C --> UI : 200 OK
deactivate C
UI --> A : identity visible only on the medical branch
deactivate UI
@enduml
```

**Description.** One endpoint, two response shapes: the decision is taken inside the service from the JWT role, never from a query parameter, so there is no "give me the full version" switch a client could flip. Parents and teachers are additionally *row*-restricted (the spec receives their `submitterUserId`), while school-side roles are *field*-restricted — two different privacy mechanisms that are easy to confuse and are therefore kept visibly separate in the diagram. The one exception, Medical Staff reading a row that BR-142 already flagged, is granted inside `getDetail` as a `revealIdentity` flag on the anonymised DTO. Note what the diagram does **not** show: there is no audit write on that path. The TDS asks for one, and an earlier revision of this diagram drew an `AuditLogger` lifeline that does not exist in the merged code — deviation 9 below.

---

## 3. Feature 2 — Caterer Response to Feedback (UC-163, BR-143)

### 3.1 Class Diagram

```plantuml
@startuml MF10_F2_ClassDiagram
title Design-level Class Diagram: Respond to Meal Feedback

' ==========================================
' LAYOUT AND RATIONAL ROSE COLOUR SCHEME
' Top-level skinparam only — the nested block form
' is not used in class diagrams (guide section 6).
' ==========================================
skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF

' Vertical layout: web -> service -> repository -> domain
top to bottom direction
hide empty members

' Class / Abstract Class
skinparam classBackgroundColor #F4FBF4
skinparam classBorderColor #2E7D32
skinparam classFontColor #212121
skinparam classFontSize 13
skinparam classHeaderBackgroundColor #4CAF50
skinparam classAttributeFontColor #212121
skinparam classAttributeFontSize 12

' Interface
skinparam interfaceBackgroundColor #f7ed93ff
skinparam interfaceBorderColor #000000ff
skinparam interfaceFontColor #000000ff

' Relationships & Arrows
skinparam arrowColor #162c00ff
skinparam arrowFontColor #162c00ff
skinparam arrowFontSize 11

' Stereotype icons
skinparam stereotypeCBackgroundColor #f6eedcff
skinparam stereotypeCBorderColor #000000ff
skinparam stereotypeEBackgroundColor #f6eedcff
skinparam stereotypeEBorderColor #000000ff
skinparam stereotypeABackgroundColor #f6eedcff
skinparam stereotypeABorderColor #000000ff
skinparam stereotypeIBackgroundColor #f6eedcff
skinparam stereotypeIBorderColor #000000ff

' ────────── mf10.web ──────────
class FeedbackController <<Controller>> {
  +respond(code, req, actor) : FeedbackResponseView
}

' ────────── mf10.service ──────────
interface FeedbackResponseService <<Service>> {
  +respond(feedbackCode : String, req : RespondRequest, actor : AuthUser) : FeedbackResponseView
}
class FeedbackResponseServiceImpl <<Service>> {
  -feedbackRepository : FeedbackRepository
  -responseRepository : FeedbackResponseRepository
  -feedbackService : FeedbackService
  -referenceResolver : ReferenceResolver
  -codeGenerator : CodeGenerator
}
interface FeedbackDashboardService <<Service>> {
  +weeklyAggregate(actor : AuthUser, weekStart : LocalDate) : FeedbackWeeklyAggregate
}
class FeedbackDashboardServiceImpl <<Service>> {
  -feedbackRepository : FeedbackRepository
  -responseRepository : FeedbackResponseRepository
}

' ────────── mf10.repository ──────────
interface FeedbackRepository <<Repository>> {
  +findBySchoolIdAndCodeAndDeletedAtIsNull(schoolId, code) : Optional
}
interface FeedbackResponseRepository <<Repository>> {
  +findByFeedbackIdOrderByRespondedAtAsc(feedbackId) : List
  +findByFeedbackIdIn(feedbackIds) : List
}

' ────────── common: shared kernel ──────────
interface ReferenceResolver <<Port>> {
  +schoolIdByCode(code) : Optional
}
interface CodeGenerator <<Port>> {
  +next(scope : String, prefix : String, width : int) : String
}

' ────────── mf10.domain ──────────
abstract class BaseEntity <<MappedSuperclass>> {
  +id : UUID <<PK>>
  +createdAt : Instant
  +createdBy : String
}
class Feedback <<Entity>> {
  +code : String <<business id>>
  +submitterUserId : UUID <<FK>>
  +rating : int
  +submittedAt : Instant
  +deletedAt : Instant
}
class FeedbackResponse <<Entity>> {
  +code : String <<business id>>
  +schoolId : UUID <<FK>>
  +feedbackId : UUID <<FK>>
  +responderUserId : UUID <<FK>>
  +content : String
  +respondedAt : Instant
  +lateResponse : boolean <<derived>>
}

' ═══════════ Relationships ═══════════
FeedbackController          ..> FeedbackResponseService     : «use»
FeedbackController          ..> FeedbackDashboardService    : «use»
FeedbackResponseService     <|.. FeedbackResponseServiceImpl
FeedbackDashboardService    <|.. FeedbackDashboardServiceImpl
FeedbackResponseServiceImpl ..> FeedbackRepository          : «use»
FeedbackResponseServiceImpl ..> FeedbackResponseRepository  : «use»
FeedbackResponseServiceImpl ..> ReferenceResolver           : «resolve»
FeedbackResponseServiceImpl ..> CodeGenerator               : «issue code»
FeedbackDashboardServiceImpl ..> FeedbackRepository         : «aggregate»
FeedbackDashboardServiceImpl ..> FeedbackResponseRepository : «aggregate»
FeedbackRepository          ..> Feedback                    : «manage»
FeedbackResponseRepository  ..> FeedbackResponse            : «manage»

BaseEntity <|-- Feedback
BaseEntity <|-- FeedbackResponse

Feedback "1" *-- "0..*" FeedbackResponse : answered by >

' ═══════════════════════════════════════════════════════════════
' Layout: [hidden] links only.
' ═══════════════════════════════════════════════════════════════
FeedbackController          -[hidden]-> FeedbackResponseService
FeedbackResponseService     -[hidden]-> FeedbackResponseServiceImpl
FeedbackResponseServiceImpl -[hidden]-> FeedbackRepository
FeedbackRepository          -[hidden]-> BaseEntity

' rule L2: to break a fan-out, link the two TARGETS to each other
FeedbackResponseService     -[hidden]-> FeedbackDashboardService
FeedbackResponseServiceImpl -[hidden]-> FeedbackDashboardServiceImpl
' rule L4: no hidden edge between Feedback and FeedbackResponse — the
' real composition already fixes their relative rank
FeedbackRepository          -[hidden]-> FeedbackResponseRepository
ReferenceResolver           -[hidden]right-> CodeGenerator
@enduml
```

**Description.** Two services share this diagram because they share both repositories: `FeedbackResponseServiceImpl` writes replies and `FeedbackDashboardServiceImpl` reads the same two tables to build the weekly aggregate (BR-141). The reply path reuses `FeedbackRepository` only to *load* the target and never to mutate it — a reply is a new row in `feedback_responses`, so the original feedback stays byte-identical to what the parent wrote. Two rules from UC-163 land in different places: the "no blank reply" rule is a hard rejection (service `FBA-REQ` plus a DB `CHECK`), while the "48-hour" rule is only a *recording* (`lateResponse = true`) because the use case asks the system to *log the delay*, not to prevent a late apology. The absence of `FeedbackWindowPolicy` is intentional and is the module's clearest example of two business rules protecting two different actors: BR-139 closes the parent's edit window after 24 hours, BR-143 leaves the caterer's reply channel open indefinitely. `FeedbackResponseServiceImpl` depending on the `FeedbackService` **interface** is drawn as it is in the code (deviation 6) — an interface dependency, so no layering rule is broken.

---

### 3.2 Sequence Diagram — Respond to a Feedback Item

```plantuml
@startuml MF10_F2_RespondFeedback
title Sequence Diagram: Respond to Meal Feedback

' Orthogonal connector mode
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME
' ==========================================
skinparam backgroundColor #FFFFFF

' Stereotype icons
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
' Lifelines by UML robustness stereotype
' ==========================================
skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontSize 13

skinparam sequence {

    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121

}

actor    "Service Caterers"              as SC
boundary ":CatererFeedbackBoard"         as UI
control  ":FeedbackController"           as C
control  ":FeedbackResponseService"      as S
control  ":FeedbackService"              as FS
control  ":ReferenceResolver"            as RR
control  ":CodeGenerator"                as CG
entity   ":FeedbackRepository"           as R
entity   ":FeedbackResponseRepository"   as RR2
database ":PostgreSQL"                   as DB

SC -> UI : open a two star feedback and write the reply
activate UI
UI -> C : POST /api/v1/feedbacks/FB-2026-8809/response
activate C
C -> S : respond(feedbackCode, req, actor)
activate S

S -> S : role not SERVICE_CATERERS ⇒ 403 FBA-005 (MSG-005)
S -> S : content blank ⇒ 422 FBA-REQ (MSG-138)
S -> FS : resolveSchoolId(actor)
activate FS
FS --> S : schoolId
deactivate FS
S -> R : findBySchoolIdAndCodeAndDeletedAtIsNull(schoolId, feedbackCode)
activate R
R -> DB : SELECT * FROM feedbacks WHERE school_id = ? AND code = ? AND deleted_at IS NULL
activate DB
DB --> R : row
deactivate DB
R --> S : empty ⇒ 404 FBA-404 (MSG-137)
deactivate R

S -> CG : next(mf10_feedback_response:2026, FBR-2026, 3)
activate CG
CG -> DB : INSERT INTO code_sequences ... RETURNING next_value
activate DB
DB --> CG : 1
deactivate DB
CG --> S : FBR-2026-001
deactivate CG
S -> S : lateResponse when rating below 3\nand the reply is later than 48h after submittedAt
S -> RR2 : saveAndFlush(response)
activate RR2
RR2 -> DB : INSERT INTO feedback_responses ... — content must not be blank
activate DB
DB --> RR2 : 1 row inserted
deactivate DB
RR2 --> S : FeedbackResponse persisted
deactivate RR2
S -> RR : userById(actor.userId)
activate RR
RR -> DB : SELECT code, email, full_name FROM user_accounts WHERE id = ?
activate DB
DB --> RR : responder row
deactivate DB
RR --> S : responder display name
deactivate RR

S --> C : FeedbackResponseView
deactivate S
C --> UI : 201 Created
deactivate C
UI --> SC : append the reply and flag it when lateResponse is true
deactivate UI
@enduml
```

**Description.** The reply flow reads the target feedback for two reasons only — to prove it exists inside the caller's school and to read `rating` and `submittedAt` for the 48-hour lateness computation — and then writes a brand-new row. Because `lateResponse` is derived from the *original* submission timestamp rather than from a stored SLA field, it stays correct even if the reply arrives months later. What the code does **not** do is notify the parent: `respond()` writes the reply row and returns, so the submitter only finds the answer by re-opening the app. UC-046 promises the opposite — that the parent can view the caterer response — and the diagram shows the gap rather than an aspirational `NotificationPort` call (deviation 10 below).

---

## 4. Feature 3 — Announcement Management (UC-017, UC-089, UC-105, UC-125, UC-126)

### 4.1 Class Diagram

```plantuml
@startuml MF10_F3_ClassDiagram
title Design-level Class Diagram: Announcement Management

' ==========================================
' LAYOUT AND RATIONAL ROSE COLOUR SCHEME
' Top-level skinparam only — the nested block form
' is not used in class diagrams (guide section 6).
' ==========================================
skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF

' Vertical layout: web -> service -> repository -> domain
top to bottom direction
hide empty members

' Class / Abstract Class
skinparam classBackgroundColor #F4FBF4
skinparam classBorderColor #2E7D32
skinparam classFontColor #212121
skinparam classFontSize 13
skinparam classHeaderBackgroundColor #4CAF50
skinparam classAttributeFontColor #212121
skinparam classAttributeFontSize 12

' Interface
skinparam interfaceBackgroundColor #f7ed93ff
skinparam interfaceBorderColor #000000ff
skinparam interfaceFontColor #000000ff

' Relationships & Arrows
skinparam arrowColor #162c00ff
skinparam arrowFontColor #162c00ff
skinparam arrowFontSize 11

' Stereotype icons
skinparam stereotypeCBackgroundColor #f6eedcff
skinparam stereotypeCBorderColor #000000ff
skinparam stereotypeEBackgroundColor #f6eedcff
skinparam stereotypeEBorderColor #000000ff
skinparam stereotypeABackgroundColor #f6eedcff
skinparam stereotypeABorderColor #000000ff
skinparam stereotypeIBackgroundColor #f6eedcff
skinparam stereotypeIBorderColor #000000ff

' ────────── mf10.web ──────────
class AnnouncementController <<Controller>> {
  +list(query, actor) : PagedResponse
  +getDetail(code, actor) : AnnouncementDetailResponse
  +create(req, actor) : AnnouncementDetailResponse
  +update(code, req, actor) : AnnouncementDetailResponse
}

' ────────── mf10.service ──────────
interface AnnouncementService <<Service>> {
  +create(req : CreateAnnouncementRequest, actor : AuthUser) : AnnouncementDetailResponse
  +update(code : String, req : UpdateAnnouncementRequest, actor : AuthUser) : AnnouncementDetailResponse
  +getDetail(code : String, actor : AuthUser) : AnnouncementDetailResponse
  +list(query : ListQuery, actor : AuthUser) : PagedResponse
}
class AnnouncementServiceImpl <<Service>> {
  -announcementRepository : AnnouncementRepository
  -revisionRepository : AnnouncementRevisionRepository
  -referenceResolver : ReferenceResolver
  -codeGenerator : CodeGenerator
}
class AnnouncementSpec <<Specification>> {
  +SEARCH_FIELDS : List = title, content, audienceLabel
  +build(query, schoolId, onlyPublished) : Specification
}

' ────────── mf10.repository ──────────
interface AnnouncementRepository <<Repository>> {
  +findBySchoolIdAndCode(schoolId, code) : Optional
}
interface AnnouncementRevisionRepository <<Repository>> {
  +findByAnnouncementIdOrderByRevisedAtAsc(announcementId) : List
  +countByAnnouncementId(announcementId) : long
}

' ────────── common: shared kernel ──────────
interface ReferenceResolver <<Port>> {
  +schoolIdByCode(code) : Optional
}
interface CodeGenerator <<Port>> {
  +next(scope : String, prefix : String, width : int) : String
}

' ────────── mf10.domain ──────────
abstract class BaseEntity <<MappedSuperclass>> {
  +id : UUID <<PK>>
  +createdAt : Instant
  +createdBy : String
}
class Announcement <<Entity>> {
  +code : String <<business id>>
  +schoolId : UUID <<FK>>
  +title : String
  +content : String
  +audienceCodes : List
  +audienceLabel : String <<derived>>
  +status : AnnouncementStatus
  +urgent : boolean
  +authorUserId : UUID <<FK>>
  +authorName : String
  +publishedAt : Instant
}
class AnnouncementRevision <<Entity>> {
  +announcementId : UUID <<FK>>
  +revisedByUserId : UUID <<FK>>
  +revisedByName : String
  +note : String
  +revisedAt : Instant
}
enum AnnouncementStatus {
  DRAFT
  PUBLISHED
}

' ═══════════ Relationships ═══════════
AnnouncementController  ..> AnnouncementService           : «use»
AnnouncementService     <|.. AnnouncementServiceImpl
AnnouncementServiceImpl ..> AnnouncementRepository        : «use»
AnnouncementServiceImpl ..> AnnouncementRevisionRepository : «append»
AnnouncementServiceImpl ..> AnnouncementSpec              : «filter»
AnnouncementServiceImpl ..> ReferenceResolver             : «resolve»
AnnouncementServiceImpl ..> CodeGenerator                 : «issue code»
AnnouncementRepository  ..> Announcement                  : «manage»
AnnouncementRevisionRepository ..> AnnouncementRevision    : «manage»

BaseEntity <|-- Announcement
BaseEntity <|-- AnnouncementRevision

Announcement "1" *-- "0..*" AnnouncementRevision : logs >
Announcement "1" --> "1" AnnouncementStatus : in state >

' ═══════════════════════════════════════════════════════════════
' Layout: [hidden] links only.
' ═══════════════════════════════════════════════════════════════
AnnouncementController  -[hidden]-> AnnouncementService
AnnouncementService     -[hidden]-> AnnouncementServiceImpl
AnnouncementServiceImpl -[hidden]-> AnnouncementRepository
AnnouncementRepository  -[hidden]-> BaseEntity

AnnouncementService     -[hidden]right-> ReferenceResolver
ReferenceResolver       -[hidden]right-> CodeGenerator
AnnouncementServiceImpl -[hidden]right-> AnnouncementSpec
AnnouncementRepository  -[hidden]right-> AnnouncementRevisionRepository
Announcement            -[hidden]right-> AnnouncementRevision
AnnouncementRevision    -[hidden]right-> AnnouncementStatus
@enduml
```

**Description.** `AnnouncementServiceImpl` is the only class allowed to write either table, and it writes them together: no code path updates an announcement without appending a revision. The split between `audienceCodes` (a JSONB list with a GIN index) and `audienceLabel` (a derived display string) is what makes role/grade-scoped visibility implementable at all — the mock's single free-text `audience` field is why every role currently sees every announcement (`review.md` H7). `AnnouncementSpec` takes an `onlyPublished` flag rather than reading the role itself, keeping the filter object testable in isolation while the service decides policy. Note what is **not** on this diagram: `AnnouncementServiceImpl` has no edge to `NotificationPort`, because the merged code injects only four dependencies and publishes nothing when an announcement goes live. That contradicts BR-138 and is recorded as deviation 2 rather than drawn as an aspiration.

---

### 4.2 Sequence Diagram — Create Announcement (Draft or Publish) (UC-017, BR-138/144)

```plantuml
@startuml MF10_F3_CreateAnnouncement
title Sequence Diagram: Create Announcement

' Orthogonal connector mode
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME
' ==========================================
skinparam backgroundColor #FFFFFF

' Stereotype icons
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
' Lifelines by UML robustness stereotype
' ==========================================
skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontSize 13

skinparam sequence {

    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121

}

actor    "School Administration"            as A
boundary ":AnnouncementEditorPage"          as UI
control  ":AnnouncementController"          as C
control  ":AnnouncementService"             as S
control  ":ReferenceResolver"               as RR
control  ":CodeGenerator"                   as CG
entity   ":AnnouncementRepository"          as R
entity   ":AnnouncementRevisionRepository"  as RV
database ":PostgreSQL"                      as DB

A -> UI : write the title and body, tick the audience groups, press publish
activate UI
UI -> C : POST /api/v1/announcements
activate C
C -> S : create(req, actor)
activate S

S -> S : requireSchoolAdmin — other roles ⇒ 403 FBA-005 (MSG-005)
S -> S : title, content or audienceCodes missing ⇒ 422 FBA-REQ (MSG-138)
S -> S : parseStatus(req.status) — unknown value ⇒ 422 FBA-ENUM\nabsent status defaults to DRAFT
S -> CG : next(mf10_announcement:2026, ANN-2026, 3)
activate CG
CG -> DB : INSERT INTO code_sequences ... RETURNING next_value
activate DB
DB --> CG : 113
deactivate DB
CG --> S : ANN-2026-113
deactivate CG
S -> RR : userById(actor.userId) for the author display name
activate RR
RR -> DB : SELECT code, email, full_name FROM user_accounts WHERE id = ?
activate DB
DB --> RR : author row
deactivate DB
RR --> S : author name
deactivate RR
S -> S : audienceLabel derived from audienceCodes\npublishedAt set only when status is PUBLISHED
S -> R : saveAndFlush(announcement)
activate R
R -> DB : INSERT INTO announcements ... — ck_ann_published_at
activate DB
DB --> R : 1 row inserted
deactivate DB
R --> S : Announcement persisted
deactivate R
S -> RV : saveAndFlush(revision with note first publication)
activate RV
RV -> DB : INSERT INTO announcement_revisions ...
activate DB
DB --> RV : 1 row inserted
deactivate DB
RV --> S : AnnouncementRevision persisted
deactivate RV

S --> C : AnnouncementDetailResponse
deactivate S
C --> UI : 201 Created (MSG-047)
deactivate C
UI --> A : open the detail page
deactivate UI
@enduml
```

**Description.** Creation and publication are one call, distinguished only by the `status` field, which is why `publishedAt` is set as the row is built rather than by a separate publish endpoint — there is no window in which a `PUBLISHED` row exists without its timestamp, and `ck_ann_published_at` makes that structural. A missing `status` defaults to `DRAFT`, so a client that forgets the field cannot accidentally broadcast to parents. The first revision is written even for a draft, so the BR-144 trail starts at creation rather than at the first edit. Two things the TDS expects are absent from the code and therefore from this diagram: HTML sanitisation of `content` (deviation 11) and the BR-138 dispatch on publish (deviation 2).

---

### 4.3 Sequence Diagram — Update & Publish Announcement (UC-089, BR-144)

```plantuml
@startuml MF10_F3_UpdateAnnouncement
title Sequence Diagram: Update and Publish Announcement

' Orthogonal connector mode
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME
' ==========================================
skinparam backgroundColor #FFFFFF

' Stereotype icons
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
' Lifelines by UML robustness stereotype
' ==========================================
skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontSize 13

skinparam sequence {

    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121

}

actor    "School Administration"            as A
boundary ":AnnouncementEditorPage"          as UI
control  ":AnnouncementController"          as C
control  ":AnnouncementService"             as S
control  ":ReferenceResolver"               as RR
entity   ":AnnouncementRepository"          as R
entity   ":AnnouncementRevisionRepository"  as RV
database ":PostgreSQL"                      as DB

A -> UI : edit the body, write a revision note, press save
activate UI
UI -> C : PATCH /api/v1/announcements/ANN-2026-113
activate C
C -> S : update(code, req, actor)
activate S

S -> S : requireSchoolAdmin — other roles ⇒ 403 FBA-005 (MSG-005)
S -> R : findBySchoolIdAndCode(actor.schoolId, code)
activate R
R -> DB : SELECT * FROM announcements WHERE school_id = ? AND code = ?
activate DB
DB --> R : row
deactivate DB
R --> S : empty ⇒ 404 FBA-404 (MSG-137)
deactivate R
S -> S : current PUBLISHED and requested DRAFT ⇒ 409 FBA-STATE (MSG-136)
S -> S : apply title, content, audienceCodes and urgent when present\naudienceLabel recomputed from audienceCodes
S -> S : publishedAt set once, only when it is still null

S -> R : saveAndFlush(announcement)
activate R
R -> DB : UPDATE announcements SET title = ?, content = ?, audience_codes = ?,\naudience_label = ?, status = ?, published_at = ?, updated_at = now() WHERE id = ?
activate DB
DB --> R : 1 row updated
deactivate DB
R --> S : Announcement updated
deactivate R

S -> RR : userById(actor.userId) for the reviser display name
activate RR
RR --> S : reviser name
deactivate RR
S -> RV : saveAndFlush(revision, note defaults to content update)
activate RV
RV -> DB : INSERT INTO announcement_revisions ...\nappend only — r_annrev_no_update and r_annrev_no_delete
activate DB
DB --> RV : 1 row inserted
deactivate DB
RV --> S : AnnouncementRevision persisted
deactivate RV
S -> RV : findByAnnouncementIdOrderByRevisedAtAsc(announcementId)
activate RV
RV -> DB : SELECT * FROM announcement_revisions WHERE announcement_id = ? ORDER BY revised_at
activate DB
DB --> RV : revision rows
deactivate DB
RV --> S : revision list
deactivate RV

S --> C : AnnouncementDetailResponse with revisions
deactivate S
C --> UI : 200 OK (MSG-029)
deactivate C
UI --> A : show the updated body and the full revision history
deactivate UI
@enduml
```

**Description.** Every successful `PATCH` writes exactly two rows — the updated announcement and one new revision — and the revision's author is read from the JWT rather than from the request body, which is what makes the BR-144 trail non-forgeable (the mock takes `editor` from the payload, so history there can be written under any name). `publishedAt` is set the first time the status becomes `PUBLISHED` and never touched again, so later typo fixes do not silently move the moment parents were notified. The `PUBLISHED → DRAFT` request is refused explicitly with `FBA-STATE` rather than ignored, because a silent no-op would leave the administrator believing the notice had been withdrawn.

---

### 4.4 Sequence Diagram — View Announcements Filtered by Role & Audience (UC-125/126/105, CF-M10-6)

```plantuml
@startuml MF10_F3_ListAnnouncements
title Sequence Diagram: Read Announcements by Role and Audience

' Orthogonal connector mode
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME
' ==========================================
skinparam backgroundColor #FFFFFF

' Stereotype icons
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
' Lifelines by UML robustness stereotype
' ==========================================
skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontSize 13

skinparam sequence {

    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121

}

actor    "Parent or Teacher or Caterer"     as U
boundary ":AnnouncementListPage"            as UI
control  ":AnnouncementController"          as C
control  ":AnnouncementService"             as S
control  ":AnnouncementSpec"                as SP
entity   ":AnnouncementRepository"          as R
entity   ":AnnouncementRevisionRepository"  as RV
database ":PostgreSQL"                      as DB

U -> UI : open school announcements
activate UI
UI -> C : GET /api/v1/announcements?page=1&pageSize=5
activate C
C -> S : list(query, actor)
activate S
S -> SP : build(query, schoolId, onlyPublished is true for non admins)
activate SP
SP --> S : Specification
deactivate SP
S -> R : findAll(spec, pageable sorted by createdAt desc)
activate R
R -> DB : SELECT * FROM announcements WHERE school_id = ?\nAND status = 'PUBLISHED' for non admins ORDER BY created_at DESC LIMIT ? OFFSET ?
activate DB
DB --> R : rows and total
deactivate DB
R --> S : Page of Announcement
deactivate R

alt caller is School Administration
  S -> RV : countByAnnouncementId for each row
  activate RV
  RV -> DB : SELECT count(*) FROM announcement_revisions WHERE announcement_id = ?
  activate DB
  DB --> RV : counts
  deactivate DB
  RV --> S : revisionCount per row
  deactivate RV
  S --> C : PagedResponse of AnnouncementAdminListItem including drafts
else any other role
  S -> S : audienceMatches — role key or grade code must be in audienceCodes
  S --> C : PagedResponse of AnnouncementAudienceListItem without revisionCount
end
deactivate S
C --> UI : 200 OK
deactivate C
UI --> U : drafts and other audiences never appear

... detail — revision history is admin only ...

U -> UI : open one announcement
UI -> C : GET /api/v1/announcements/ANN-2026-113
activate C
C -> S : getDetail(code, actor)
activate S
S -> R : findBySchoolIdAndCode(schoolId, code)
activate R
R -> DB : SELECT * FROM announcements WHERE school_id = ? AND code = ?
activate DB
DB --> R : row
deactivate DB
R --> S : empty ⇒ 404 FBA-404 (MSG-137)
deactivate R
S -> S : status DRAFT and caller not School Administration ⇒ 404 FBA-404
opt caller is School Administration
  S -> RV : findByAnnouncementIdOrderByRevisedAtAsc(announcementId)
  activate RV
  RV -> DB : SELECT * FROM announcement_revisions WHERE announcement_id = ? ORDER BY revised_at
  activate DB
  DB --> RV : revision rows
  deactivate DB
  RV --> S : revision list
  deactivate RV
end
S --> C : AnnouncementDetailResponse
deactivate S
C --> UI : 200 OK
deactivate C
UI --> U : body always, revision history only for school administration
deactivate UI
@enduml
```

**Description.** Two orthogonal filters decide visibility, and the diagram keeps them separate on purpose: `publishedOnly` is pushed into the `Specification` (a SQL `WHERE`), while `audienceMatches` currently runs in Java after paging. Both must hold — a test that only verifies the draft rule would still allow a grade-1 parent to read a grade-3 notice. Revision history is a third, finer-grained decision: it is not merely omitted from the payload for non-administrators, it is never even queried, so the extra `count`/`select` cost is only paid by the one role entitled to see it (UC-105 step 5).

---

## 5. Feature 4 — In-App, Push & Email Notification (UC-058, UC-136, UC-088)

### 5.1 Class Diagram

```plantuml
@startuml MF10_F4_ClassDiagram
title Design-level Class Diagram: Notification Dispatch and Inbox

' ==========================================
' LAYOUT AND RATIONAL ROSE COLOUR SCHEME
' Top-level skinparam only — the nested block form
' is not used in class diagrams (guide section 6).
' ==========================================
skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF

' Vertical layout: web -> service -> repository -> domain
top to bottom direction
hide empty members

' Class / Abstract Class
skinparam classBackgroundColor #F4FBF4
skinparam classBorderColor #2E7D32
skinparam classFontColor #212121
skinparam classFontSize 13
skinparam classHeaderBackgroundColor #4CAF50
skinparam classAttributeFontColor #212121
skinparam classAttributeFontSize 12

' Interface
skinparam interfaceBackgroundColor #f7ed93ff
skinparam interfaceBorderColor #000000ff
skinparam interfaceFontColor #000000ff

' Relationships & Arrows
skinparam arrowColor #162c00ff
skinparam arrowFontColor #162c00ff
skinparam arrowFontSize 11

' Stereotype icons
skinparam stereotypeCBackgroundColor #f6eedcff
skinparam stereotypeCBorderColor #000000ff
skinparam stereotypeEBackgroundColor #f6eedcff
skinparam stereotypeEBorderColor #000000ff
skinparam stereotypeABackgroundColor #f6eedcff
skinparam stereotypeABorderColor #000000ff
skinparam stereotypeIBackgroundColor #f6eedcff
skinparam stereotypeIBorderColor #000000ff

' ────────── mf10.web ──────────
class NotificationController <<Controller>> {
  +list(query, actor) : PagedResponse
  +markRead(code, actor) : void
}
class NotificationPreferenceController <<Controller>> {
  +getMine(actor) : NotificationPreferenceResponse
  +updateMine(req, actor) : NotificationPreferenceResponse
}
class DeviceTokenController <<Controller>> {
  +register(req, actor) : void
  +unregister(token, actor) : void
}

' ────────── mf10.port ──────────
interface NotificationPort <<Port>> {
  +publish(command : NotificationCommand) : String
}
class NotificationPortImpl <<Adapter>> {
  -notificationRepository : NotificationRepository
  -codeGenerator : CodeGenerator
  -broadcaster : NotificationBroadcaster
  +publish(command) : String
}
interface PushGatewayPort <<Port>> {
  +sendToTokens(tokens : List, title : String, body : String, data : Map) : List
}

' ────────── mf10.service ──────────
interface NotificationService <<Service>> {
  +listForCurrentUser(query : ListQuery, actor : AuthUser) : PagedResponse
  +markRead(code : String, actor : AuthUser) : void
  +getMyPreference(actor : AuthUser) : NotificationPreferenceResponse
  +updateMyPreference(req : UpdatePreferenceRequest, actor : AuthUser) : NotificationPreferenceResponse
}
class NotificationServiceImpl <<Service>> {
  -notificationRepository : NotificationRepository
  -readRepository : NotificationReadRepository
  -preferenceRepository : NotificationPreferenceRepository
  -referenceResolver : ReferenceResolver
}
class NotificationBroadcaster <<Component>> {
  -messagingTemplate : SimpMessagingTemplate
  +broadcast(n : UserNotification) : void
}
class PushNotificationServiceImpl <<Service>> {
  -deviceTokenRepository : DeviceTokenRepository
  -pushGatewayPort : PushGatewayPort
  +sendToUser(userId, title, body, data) : void
}
class DeviceTokenServiceImpl <<Service>> {
  -repository : DeviceTokenRepository
  +register(request, actor) : void
  +unregister(token, actor) : void
}

' ────────── mf10.repository ──────────
interface NotificationRepository <<Repository>> {
  +findVisibleToSchool(schoolId) : List
  +findByCode(code) : Optional
}
interface NotificationReadRepository <<Repository>> {
  +findByUserIdAndNotificationIdIn(userId, ids) : List
  +existsByNotificationIdAndUserId(notificationId, userId) : boolean
}
interface NotificationPreferenceRepository <<Repository>> {
  +findById(userId) : Optional
}
interface NotificationDeliveryRepository <<Repository>> {
  +findByStatusInAndNextRetryAtBefore(statuses, now) : List
}
interface DeviceTokenRepository <<Repository>> {
  +findByUserId(userId) : List
  +findByToken(token) : Optional
}

' ────────── mf10.domain ──────────
abstract class BaseEntity <<MappedSuperclass>> {
  +id : UUID <<PK>>
  +createdAt : Instant
  +createdBy : String
}
class UserNotification <<Entity>> {
  +code : String <<business id>>
  +schoolId : UUID <<FK>>
  +audience : NotificationAudience
  +recipientRoles : List
  +type : NotificationType
  +title : String
  +content : String
  +link : String
  +linkLabel : String
  +sentAt : Instant
  +isCritical() : boolean <<derived>>
}
class NotificationRead <<Entity>> {
  +notificationId : UUID <<FK>>
  +userId : UUID <<FK>>
  +readAt : Instant
}
class NotificationDelivery <<Entity>> {
  +notificationId : UUID <<FK>>
  +userId : UUID <<FK>>
  +channel : Channel
  +attempt : int
  +status : DeliveryStatus
  +nextRetryAt : Instant
  +lastError : String
}
class NotificationPreference <<Entity>> {
  +userId : UUID <<PK>>
  +typeEnabled : Map
  +emailFallback : boolean
  +quietStart : LocalTime
  +quietEnd : LocalTime
  +createdAt : Instant
  +updatedAt : Instant
}
class DeviceToken <<Entity>> {
  +userId : UUID <<FK>>
  +token : String
  +platform : DevicePlatform
  +lastUsedAt : Instant
}
enum NotificationType <<external>> {
  MENU_PUBLISHED
  INSPECTION_ALERT
  CAPA_ASSIGNED
  EXPIRY_ALERT
  INCIDENT_ALERT
  FOOD_RECALL
  ALLERGY_ALERT
}
enum NotificationAudience {
  ALL
  ROLES
}
enum DeliveryStatus {
  QUEUED
  DEFERRED
  SENT
  FAILED
  FALLBACK_EMAIL
}
enum Channel {
  PUSH
  EMAIL
}
enum DevicePlatform {
  ANDROID
  IOS
  WEB
}

' ═══════════ Relationships ═══════════
NotificationController           ..> NotificationService     : «use»
NotificationPreferenceController ..> NotificationService     : «use»
DeviceTokenController            ..> DeviceTokenServiceImpl  : «use»
NotificationPort  <|.. NotificationPortImpl
NotificationService <|.. NotificationServiceImpl
NotificationPortImpl    ..> NotificationRepository   : «use»
NotificationPortImpl    ..> NotificationBroadcaster  : «broadcast»
NotificationServiceImpl ..> NotificationRepository   : «use»
NotificationServiceImpl ..> NotificationReadRepository : «use»
NotificationServiceImpl ..> NotificationPreferenceRepository : «use»
PushNotificationServiceImpl ..> DeviceTokenRepository : «use»
PushNotificationServiceImpl ..> PushGatewayPort       : «send»
DeviceTokenServiceImpl  ..> DeviceTokenRepository     : «use»
NotificationRepository  ..> UserNotification          : «manage»
NotificationReadRepository ..> NotificationRead       : «manage»
NotificationPreferenceRepository ..> NotificationPreference : «manage»
NotificationDeliveryRepository ..> NotificationDelivery : «manage»
DeviceTokenRepository   ..> DeviceToken               : «manage»

BaseEntity <|-- UserNotification
BaseEntity <|-- NotificationRead
BaseEntity <|-- NotificationDelivery
BaseEntity <|-- DeviceToken

UserNotification "1" --> "0..*" NotificationRead     : read by >
UserNotification "1" --> "0..*" NotificationDelivery : delivered by >
UserNotification "1" --> "1" NotificationType     : typed as >
UserNotification "1" --> "1" NotificationAudience : targets >
NotificationDelivery "1" --> "1" DeliveryStatus : in state >
NotificationDelivery "1" --> "1" Channel        : over >
DeviceToken "1" --> "1" DevicePlatform : on >

' ═══════════════════════════════════════════════════════════════
' Layout: [hidden] links only.
' ═══════════════════════════════════════════════════════════════
NotificationController  -[hidden]-> NotificationPort
NotificationPort        -[hidden]-> NotificationService
NotificationService     -[hidden]-> NotificationRepository
NotificationRepository  -[hidden]-> BaseEntity

NotificationController  -[hidden]right-> NotificationPreferenceController
NotificationPreferenceController -[hidden]right-> DeviceTokenController
NotificationPort        -[hidden]right-> PushGatewayPort
NotificationPortImpl    -[hidden]right-> NotificationServiceImpl
NotificationServiceImpl -[hidden]right-> NotificationBroadcaster
NotificationBroadcaster -[hidden]right-> PushNotificationServiceImpl
PushNotificationServiceImpl -[hidden]right-> DeviceTokenServiceImpl
NotificationRepository  -[hidden]right-> NotificationReadRepository
NotificationReadRepository -[hidden]right-> NotificationPreferenceRepository
NotificationPreferenceRepository -[hidden]right-> NotificationDeliveryRepository
NotificationDeliveryRepository -[hidden]right-> DeviceTokenRepository
UserNotification    -[hidden]right-> NotificationRead
NotificationRead    -[hidden]right-> NotificationDelivery
NotificationDelivery -[hidden]right-> NotificationPreference
NotificationPreference -[hidden]right-> DeviceToken
NotificationType    -[hidden]right-> NotificationAudience
NotificationAudience -[hidden]right-> DeliveryStatus
DeliveryStatus      -[hidden]right-> Channel
Channel             -[hidden]right-> DevicePlatform
DeviceToken         -[hidden]-> NotificationType
@enduml
```

**Description.** The module exposes exactly one write door — `NotificationPort` — and everything behind it is private to MF10, which is the structural reason quiet hours (BR-158), per-type opt-out (BR-158b) and retry/fallback (BR-159) *can* be one implementation rather than five copies. Reading is split from writing: `NotificationServiceImpl` serves the bell and the preferences screen, while `NotificationPortImpl` persists the row and hands off to `NotificationBroadcaster` for the live STOMP push. Two facts are deliberately not stored — `critical` (derived from `type`) and `read` (derived from a per-user row in `notification_reads`) — because storing either would let one caller's or one recipient's action be mistaken for a global truth. `NotificationDelivery` and `DeliveryStatus` are drawn because the table and enum exist in `V18`, but nothing writes them yet: `NotificationDeliveryRepository` has no incoming dependency edge, which is the diagram's honest way of showing deviation 4. `NotificationPreference` is the one entity that does not inherit `BaseEntity` — its PK is `user_id`, so it declares its own audit columns (deviation 7).

---

### 5.2 Sequence Diagram — Publish a Notification through NotificationPort (UC-058)

```plantuml
@startuml MF10_F4_DispatchNotification
title Sequence Diagram: Publish a Notification through NotificationPort

' Orthogonal connector mode
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME
' ==========================================
skinparam backgroundColor #FFFFFF

' Stereotype icons
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
' Lifelines by UML robustness stereotype
' ==========================================
skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontSize 13

skinparam sequence {

    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121

}

control  ":FeedbackService or another module"  as CALLER
control  ":NotificationPort"                   as P
control  ":CodeGenerator"                      as CG
control  ":NotificationBroadcaster"            as B
entity   ":NotificationRepository"             as R
database ":PostgreSQL"                         as DB
boundary ":Subscribed browser"                 as UI

CALLER -> P : publish(NotificationCommand)
activate P
P -> CG : next(mf10_notification:2026, NTF-2026, 6)
activate CG
CG -> DB : INSERT INTO code_sequences ... RETURNING next_value
activate DB
DB --> CG : 482913
deactivate DB
CG --> P : NTF-2026-482913
deactivate CG
P -> P : audience is ALL when toAllRoles, otherwise ROLES\nrecipientRoles kept only for the ROLES case — ck_ntf_roles
P -> R : saveAndFlush(notification)
activate R
R -> DB : INSERT INTO notifications ...\none row per event, no per recipient fan out
activate DB
DB --> R : 1 row inserted
deactivate DB
R --> P : UserNotification persisted
deactivate R
P -> B : broadcast(notification)
activate B
B -> B : schoolId null ⇒ skip, platform wide notices are not broadcast
B -> UI : convertAndSend to /topic/notifications/{schoolId}
activate UI
UI --> B : delivered to the open sessions
deactivate UI
B --> P : void
deactivate B
P --> CALLER : notification code
deactivate P
@enduml
```

**Description.** This is the whole of what `publish()` does today, and it is deliberately short: issue a code, write **one** `notifications` row per event — not one per recipient — and hand it to `NotificationBroadcaster`, which pushes it to the school STOMP topic so open browsers update without polling. `NotificationPort` is the only write door into that table, which is what would let the BR-158/158b/159 policy live in a single place. Would, not does: no per-recipient pipeline exists yet. `notification_deliveries`, `QuietHoursPolicy` and the retry scheduler are in the TDS and the table has existed since `V18`, but nothing writes it, so drawing them here would describe behaviour the system does not have (deviation 4). The one conditional that is real sits inside the broadcaster: a platform-wide notice has no `schoolId` and therefore no topic, so it is stored and never broadcast.

---

### 5.3 Sequence Diagram — Notification Inbox & Per-User Read State (UC-136, CF-M10-10)

```plantuml
@startuml MF10_F4_InboxMarkRead
title Sequence Diagram: Notification Inbox and Mark as Read

' Orthogonal connector mode
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME
' ==========================================
skinparam backgroundColor #FFFFFF

' Stereotype icons
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
' Lifelines by UML robustness stereotype
' ==========================================
skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontSize 13

skinparam sequence {

    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121

}

actor    "Medical Staff"                    as UA
boundary ":NotificationBell"                as UI
control  ":NotificationController"          as C
control  ":NotificationService"             as S
entity   ":NotificationRepository"          as R
entity   ":NotificationReadRepository"      as RR
database ":PostgreSQL"                      as DB

UA -> UI : click the bell
activate UI
UI -> C : GET /api/v1/notifications?page=1&pageSize=50
activate C
C -> S : listForCurrentUser(query, actor)
activate S
S -> R : findVisibleToSchool(schoolId)
activate R
R -> DB : SELECT * FROM notifications\nWHERE school_id = ? OR school_id IS NULL ORDER BY sent_at DESC
activate DB
DB --> R : rows
deactivate DB
R --> S : list of UserNotification
deactivate R
S -> S : isVisibleToRole — audience ALL, or recipientRoles contains the caller role
S -> RR : findByUserIdAndNotificationIdIn(actor.userId, visibleIds)
activate RR
RR -> DB : SELECT notification_id FROM notification_reads\nWHERE user_id = ? AND notification_id IN (...)
activate DB
DB --> RR : read ids for this user only
deactivate DB
RR --> S : read rows
deactivate RR
S -> S : read flag computed per user, then paginate in memory
S --> C : PagedResponse of NotificationResponse
deactivate S
C --> UI : 200 OK
deactivate C
UI --> UA : badge shows the unread count for this account

... mark as read — idempotent, per recipient ...

UA -> UI : open one notification
UI -> C : PATCH /api/v1/notifications/NTF-2026-482913/read
activate C
C -> S : markRead(code, actor)
activate S
S -> R : findByCode(code)
activate R
R -> DB : SELECT * FROM notifications WHERE code = ?
activate DB
DB --> R : row
deactivate DB
R --> S : empty ⇒ 404 FBA-404 (MSG-137)
deactivate R
S -> S : not visible to the caller role ⇒ 403 FBA-005 (MSG-005)
S -> RR : existsByNotificationIdAndUserId(notificationId, actor.userId)
activate RR
RR -> DB : SELECT 1 FROM notification_reads WHERE notification_id = ? AND user_id = ?
activate DB
DB --> RR : row
deactivate DB
RR --> S : true ⇒ return without writing, the call is idempotent
deactivate RR
S -> RR : saveAndFlush(read)
activate RR
RR -> DB : INSERT INTO notification_reads ... — uq_ntfread
activate DB
DB --> RR : 1 row inserted
deactivate DB
RR --> S : NotificationRead persisted
deactivate RR
S --> C : void
deactivate S
C --> UI : 204 No Content
deactivate C
UI --> UA : badge decreases for this account only
deactivate UI
@enduml
```

**Description.** The last three interactions exist to make one invariant visible: user B's view is unaffected by user A marking the same notification read, because "read" is a row in `notification_reads` rather than a column on `notifications`. `markRead` is idempotent by construction — the service checks first and the `uq_ntfread` unique index catches the race — so a double-click or a retried request cannot create duplicates. Role visibility is re-evaluated on both read and write paths, so a user cannot mark read (and thereby confirm the existence of) a notification addressed to a different role.

---

### 5.4 Sequence Diagram — Manage Notification Preferences (UC-088, BR-158b)

```plantuml
@startuml MF10_F4_Preferences
title Sequence Diagram: Manage Notification Preferences

' Orthogonal connector mode
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME
' ==========================================
skinparam backgroundColor #FFFFFF

' Stereotype icons
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
' Lifelines by UML robustness stereotype
' ==========================================
skinparam BoundaryBackgroundColor #F4FBF4
skinparam BoundaryBorderColor #2E7D32
skinparam BoundaryFontColor #212121
skinparam BoundaryFontSize 13

skinparam ControlBackgroundColor #F4FBF4
skinparam ControlBorderColor #2E7D32
skinparam ControlFontColor #212121
skinparam ControlFontSize 13

skinparam EntityBackgroundColor #F4FBF4
skinparam EntityBorderColor #2E7D32
skinparam EntityFontColor #212121
skinparam EntityFontSize 13

skinparam DatabaseBackgroundColor #F4FBF4
skinparam DatabaseBorderColor #2E7D32
skinparam DatabaseFontColor #212121
skinparam DatabaseFontSize 13

skinparam sequence {

    ActorBackgroundColor #E8F5E9
    ActorBorderColor #2E7D32
    ActorFontColor #1B5E20
    ActorFontSize 13

    ParticipantBackgroundColor #F4FBF4
    ParticipantBorderColor #2E7D32
    ParticipantFontColor #212121
    ParticipantFontSize 13

    ArrowColor #2E7D32
    ArrowFontColor #1B5E20
    ArrowFontSize 12

    LifeLineBackgroundColor #C8E6C9
    LifeLineBorderColor #2E7D32

    GroupBackgroundColor #F4FBF4
    GroupBorderColor #2E7D32
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121

}

actor    "Any signed in user"                   as U
boundary ":NotificationSettingsPage"            as UI
control  ":NotificationPreferenceController"    as C
control  ":NotificationService"                 as S
entity   ":NotificationPreferenceRepository"    as PR
database ":PostgreSQL"                          as DB

U -> UI : open notification settings
activate UI
UI -> C : GET /api/v1/notification-preferences/me
activate C
C -> S : getMyPreference(actor)
activate S
S -> PR : findById(actor.userId)
activate PR
PR -> DB : SELECT * FROM notification_preferences WHERE user_id = ?
activate DB
DB --> PR : no row on the first visit
deactivate DB
PR --> S : empty ⇒ fall back to the BR-158 defaults, never 404
deactivate PR
S --> C : NotificationPreferenceResponse\nseven type switches, quiet hours 22:00 to 06:00, criticalTypes
deactivate S
C --> UI : 200 OK
deactivate C
UI --> U : render seven switches, the three critical ones locked

... save — upsert keyed by user_id ...

U -> UI : switch EXPIRY_ALERT off, set quiet hours 21:00 to 07:00, save
UI -> C : PUT /api/v1/notification-preferences/me
activate C
C -> S : updateMyPreference(req, actor)
activate S
S -> PR : findById(actor.userId)
activate PR
PR -> DB : SELECT * FROM notification_preferences WHERE user_id = ?
activate DB
DB --> PR : row or none
deactivate DB
PR --> S : existing row, otherwise a new one with the default map
deactivate PR
S -> S : apply typeEnabled, emailFallback, quietStart and quietEnd when present
S -> PR : saveAndFlush(preference)
activate PR
PR -> DB : INSERT INTO notification_preferences ... ON CONFLICT (user_id) DO UPDATE
activate DB
DB --> PR : 1 row upserted
deactivate DB
PR --> S : NotificationPreference persisted
deactivate PR
S --> C : NotificationPreferenceResponse echoing the stored state
deactivate S
C --> UI : 200 OK
deactivate C
UI --> U : confirm the save
deactivate UI
@enduml
```

**Description.** The endpoint answers with the BR-158 defaults when no row exists yet, so a first-time user and a configured user are indistinguishable to the client — no empty-state branch, and no chance of the frontend inventing a second set of defaults. Saving is an upsert keyed by `user_id`, matching the table primary key, and each field is applied only when present so a partial payload cannot silently reset the others. The response carries `criticalTypes` so the UI can lock those three switches; the API still accepts a request that turns one off, because refusing it would misreport what was stored. Whether that stored preference is ever honoured is a different question — the dispatcher that would read it is not built (deviation 4).

---

## 6. Entity Lifecycles (State Charts)

> **One state chart, not four.** The decision follows the review guide section 5.1b — count the values the
> code actually writes, do not read the TDS. `grep -rn "setStatus(|setActive(|setState(" com/sfms/mf10`
> returns exactly three call sites outside the setters themselves, and all three are
> `AnnouncementServiceImpl` writing `AnnouncementStatus`. Everything else fails the test; section 6.2
> records why, because a removed diagram is a design statement too.

### 6.1 State Chart — Announcement (UC-017 / UC-089)

```plantuml
@startuml MF10_S1_AnnouncementState
title State Diagram: Announcement Lifecycle

' Orthogonal connector mode
skinparam linetype ortho

' ==========================================
' GREEN COLOUR SCHEME
' ==========================================
skinparam backgroundColor #FFFFFF

' ==========================================
' State
' ==========================================
skinparam state {
    BackgroundColor #F4FBF4
    BorderColor #2E7D32
    FontColor #212121
    FontSize 13
}

' ==========================================
' Arrow
' ==========================================
skinparam arrow {
    Color #2E7D32
    FontColor #1B5E20
    FontSize 12
}

' ==========================================
' Start / End
' ==========================================
skinparam startColor #2E7D32
skinparam endColor #2E7D32

[*] --> DRAFT : save as draft
[*] --> PUBLISHED : create and publish

DRAFT --> PUBLISHED : publish
@enduml
```

**Description.** Two states and three transitions is the whole lifecycle, because `create` accepts either
status and `update` is the only other writer. Content edits are **not** drawn as self-transitions: they
change `title`, `content`, `audienceCodes` and append a revision, but they never touch `status`, so under
the guide they belong in prose rather than on the chart. `PUBLISHED -> DRAFT` is likewise absent — the
service rejects it with `FBA-STATE`, and a transition the code refuses to perform is not part of the
machine. There is no final `[*]`: the module exposes no delete endpoint, so `PUBLISHED` is a legitimate
terminal resting state that simply never ends.

### 6.2 Lifecycles deliberately not drawn

| Candidate | What the code shows | Why no chart |
|---|---|---|
| `Feedback` "editable / locked / retracted" | No status column at all. `setDeletedAt` is called from exactly one method, `retract()` | `EDITABLE` and `LOCKED` are a **guard inside** `FeedbackWindowPolicy.assertOpen` comparing `now()` against `servedAt + 24h` — a condition, not a position in a lifecycle. Drawing them repeats the mistake the guide records for MF08's `RETAINED{WithinHold/Eligible/Locked}`. The 24-hour rule is visible where it actually runs: the `assertOpen` self-message in the sequence charts and `FeedbackWindowPolicy.WINDOW` in the class diagram |
| `DeliveryStatus` (`QUEUED`/`DEFERRED`/`SENT`/`FAILED`/`FALLBACK_EMAIL`) | `setStatus` exists on `NotificationDelivery` but **no service, port or scheduler calls it** — zero write sites | Zero writes means no lifecycle to chart yet. The enum, the table (`V18`) and the BR-158/159 intent are all real, but the dispatcher that would drive them is not built (deviation 4 below). Charting it would describe behaviour the system does not have |
| Per-recipient read state | Not an enum and not a column — "read" is the existence of a `notification_reads` row | A two-value derived predicate. The design decision it encodes (per-user rather than global) is already carried by the `notification_reads` entity and its `uq_ntfread` unique key in the class diagram |
| `IllnessKeyword.active`, `NotificationAudience`, `Channel`, `DevicePlatform`, `SubmitterRole`, `FeedbackScope`, `PortionSize` | Set once at creation, or a static classification label | Category flags, not lifecycles |

---

## Appendix — Cross-Feature Notes

### A. Where each business rule is enforced

| Rule | Enforcement point | Second line of defence |
|---|---|---|
| **BR-138** urgent notice, no approval step | `NotificationPortImpl.publish` + `isCritical()` derived from `type` | `critical` is not a column, so no caller can self-declare it (`M10-D07`) |
| **BR-139** 24h feedback window | `FeedbackWindowPolicy.assertOpen` — used by `submit`, `update`, `retract` | none possible at DB level (depends on `now()` + MF09 `served_at`) → §2.2/§2.4 tests |
| **BR-140** low rating needs a tag | `FeedbackServiceImpl.submit/update` → `FBA-140` | `ck_fb_low_rating_needs_tag` |
| **BR-141** weekly aggregate | `FeedbackDashboardService.weeklyAggregate` | cron job not yet implemented (TDS §2.5 debt) |
| **BR-142** illness keyword screening | `IllnessKeywordDetector` + `illness_keywords` table | client-sent `illnessKeyword` discarded (`M10-D14`) |
| **BR-143** caterer reply | `FeedbackResponseServiceImpl.respond` (append) | `content` non-blank `CHECK` |
| **BR-144** revision history | `AnnouncementServiceImpl.addRevision` on every create/update | DB RULEs `r_annrev_no_update` / `r_annrev_no_delete` |
| **BR-145** anonymized aggregates | two DTO families chosen by JWT role | `M10-D13` `oneOf` in `/v3/api-docs` so the contract is visible to FE codegen |
| **BR-158/158b** quiet hours | `QuietHoursPolicy.shouldDefer` (per-recipient preference) | defaults returned when no row exists |
| **BR-159** retry then email | `DeliveryRetryScheduler` | `attempt <= 3` `CHECK` |

### B. Identifier and shape conventions used in every diagram

- **`id` in JSON is the business code** (`FB-2026-8809`, `ANN-2026-113`, `NTF-2026-482913`, `FBR-2026-001`); UUIDs never leave the server (`M10-D01`). Path params use the code.
- **Foreign keys in JSON are the target's code** (`studentId: "HS-3A-001"`, `dishId: "DISH-018"`); foreign keys in the database stay UUID.
- **Dates are ISO-8601** — `"2026-07-01"` (`LocalDate`) / `"2026-07-01T13:15:00Z"` (`Instant`); `publishedAt` is `null`, never `""` (`M10-D10`).
- **Errors use the D2 envelope** `{ error: { code, message, details[] } }` with English messages (D7). MF10 codes: `FBA-005` (403), `FBA-022` (409), `FBA-140` (422), `FBA-REQ` (422), `FBA-DUP` (409), `FBA-STATE` (409), `FBA-404` (404), `FBA-409` (409).
- **List endpoints return** `{ items, total, page, pageSize, pageCount }` with `pageCount >= 1` and an empty result as `200`, never `404` (Team Plan §2b).

### C. Known deviations between the merged code and the TDS

| # | TDS says | Merged code does | Handling |
|---|---|---|---|
| 1 | `MealServingPort` is an MF10-declared port for MF09's `servedAt` (TDS ADR-M10-1, section 8.1) | No such port — `FeedbackWindowPolicy` and `FeedbackServiceImpl` call the shared `ReferenceResolver` | **Cosmetic.** `M10-D06` already supersedes the ADR; diagrams drawn from the code |
| 2 | Publishing an announcement raises a notification through `NotificationPort` (BR-138, TDS section 7.1 `AnnouncementPublished`) | `AnnouncementServiceImpl` injects only 4 dependencies — **no `NotificationPort`**; nothing is published | **Doc gap.** Root cause is real: none of the 7 shared `NotificationType` values means "announcement published" (D6). Endpoint not built; the class diagram shows no such edge |
| 3 | A negative rating raises MSG-057 to the caterers (TDS section 7.1 `NegativeFeedbackRaised`) | `notifyAfterSubmit` publishes the BR-142 alert only; the `rating < 3` branch is commented out for the same missing enum value | **Doc gap.** Awaiting Huy's decision on extending the shared enum |
| 4 | BR-158/158b/159 dispatch pipeline — quiet hours, retry cap, email fallback (TDS section 6.4) | `notification_deliveries` table exists since `V18`; **no code writes it**. `NotificationPortImpl` persists the notification row and broadcasts over STOMP, nothing more | **Doc gap.** Blocked on the per-school-and-role user list that MF01 does not expose. No state chart drawn (section 6.2) |
| 5 | TDS section 7 lists 10 domain events | `grep publishEvent com/sfms/mf10` returns **0** | **Cosmetic.** No queue or outbox lifeline appears in any diagram |
| 6 | `FeedbackResponseService` is independent of `FeedbackService` | `FeedbackResponseServiceImpl` injects the `FeedbackService` **interface** to reuse school resolution | **Cosmetic.** Interface, not implementation, so no ADR breach; drawn as a dependency |
| 7 | Every MF10 entity extends `BaseEntity` (`M10-D02`) | Nine do; **`NotificationPreference` does not** — its PK is `user_id`, so it declares its own four audit columns | **Cosmetic.** Diagram omits the inheritance edge for that one entity, matching the code |
| 8 | `IllnessKeywordDetector.detect(comment, tags)` (TDS section 8.1) | Signature is `detect(String text)` — a single argument | **Cosmetic.** Class diagram follows the code |
| 9 | ADR-M10-5: a Medical Staff read of an illness-flagged row reveals the pupil identity **and is audited** | `getDetail` sets `revealIdentity` and returns; **no audit record is written** | **Raise issue.** The exception is the module's only lawful PII disclosure, and without a trail it cannot be distinguished from a leak after the fact. Drawn as the code behaves |
| 10 | TDS section 7.1 event `FeedbackResponded`; UC-046 says the submitter can view the caterer reply | `respond()` persists the reply and returns — no `NotificationPort` call | **Doc gap.** The parent is never told. Endpoint not built; no notification arrow in the diagram |
| 11 | TDS section 4.3: announcement `content` is sanitised before persisting — the largest stored-XSS surface in the system | `grep -rn "sanitiz|Jsoup|escapeHtml" com/sfms/mf10` returns **0**; `create` and `update` store the body verbatim | **Raise issue.** Announcements are broadcast to every parent of a school, so this is the highest-value XSS sink in the module. Diagram shows no sanitise step |


---

### D. Scope note — how this file was verified

All sixteen diagrams were reworked against `MF04_Supplier_Partner_Management/Design-Diagram-Review-Guide.md`
v1.2 and verified with the guide's own tooling, not by eye:

| Check | Result |
|---|---|
| Guide section 8.2 script, all 16 diagrams | **OK 16/16** — no `package`, no `o--`, no nested `skinparam` in class diagrams, no double stereotype, no `FontName Arial`, no white font, no Vietnamese, no `note`, no `collections` or `queue` lifelines, `alt` nesting at most 2, `activate` and `deactivate` balanced |
| Guide section 8.1 render, PlantUML 1.2024.7 with smetana | **16/16 clean**, no warnings |
| Guide section 8.3 aspect ratio, class diagrams and state chart | `F1` 1.06 · `F2` 1.06 · `F3` 1.12 · `F4` 1.12 · state chart 1.04 — all inside 0.7 – 1.2 |
| State charts | 4 → **1** (guide section 5.1b); the three removed are justified in section 6.2 |
| Sequence diagrams | 11 kept. The former *Submit Feedback — Error Paths* diagram was dropped once its guards became `⇒ code` self-messages on the main flow, and its slot reused for the attachment upload and download flow, which had no diagram before (guide section 4.6) |

Three statements in the previous revision described behaviour the merged code does not have — an
`AuditLogger` lifeline, a `NotificationPort` call after a caterer reply, and a `sanitize(content)` step.
All three were removed from the diagrams and recorded as deviations 9, 10 and 11 instead of being drawn
as if they existed.

---|---|
| Guide section 8.2 script, class + state charts | **OK 5/5** — no `package`, no `o--`, no nested `skinparam`, no double stereotype, no `Arial`, no white font, no Vietnamese, no `entry`/`do`, no notes |
| Guide section 8.1 render, PlantUML 1.2024.7 + smetana | **5/5 clean**, no warnings |
| Guide section 8.3 aspect ratio | `F1` 1.06 · `F2` 1.06 · `F3` 1.12 · `F4` 1.12 · state chart 1.04 — all inside 0.7 – 1.2 |
| State charts | 4 → **1** (guide section 5.1b); the three removed are justified in section 6.2 |

The **eleven sequence diagrams were left untouched** and still fail the guide's section 4 rules — they
carry `skinparam FontName Arial`, white group-header text, Vietnamese labels, `note` blocks, and
`MF10_F4_DispatchNotification` nests `alt` four deep against a limit of two. They also draw repositories
as `collections` where the guide fixes `entity`, and a `queue` lifeline for a STOMP topic. Reworking them
is a separate pass.

---

*Diagrams derived from `TDS-MF10_Backend.md` (`M10-D01` … `M10-D14`), Flyway `V18` / `V19` / `V34`, and the merged implementation in `com.sfms.mf10`. House style matches `MF08_Design_Diagrams.md`: Rational-Rose class palette, robustness-stereotype sequence lifelines with explicit returns, and green-tone state charts.*
