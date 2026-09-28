# MF06 — Ingredient & Inventory Management
## Design Diagrams (Database, Class, Sequence)

> **Stack:** Spring Boot 4.1.x (Java 25) · PostgreSQL 16 · Flyway · Spring Security + JWT (RBAC 7 roles) · springdoc-OpenAPI · MapStruct
> **Bounded Context:** Procurement → Delivery → Receiving → Warehouse → Kitchen Consumption (Farm-to-Table Traceability)
> **Based on:** TDS `SFMS-MF06-IMP-001 v1.1` (Approved 2026-08-03)

---

## Database Architecture (DBML)

> Reflects Flyway `V10__mf06_ingredient_inventory.sql` / `V11__seed_mf06.sql`, owned by MF06. All tables use UUID primary keys generated server-side; a human-readable `code` (unique per school/partner) is the public business identifier (D1). Four audit columns (`created_at`, `updated_at`, `created_by`, `updated_by`) repeat on every table via `BaseEntity` (D9) and are omitted from Java field declarations. Cross-module references (`school_id`, `partner_id`, `menu_id`, `dish_id`, `user_accounts.id`) are UUID-only foreign keys — no cross-module business logic lives in MF06.

```dbml
// Use DBML to define your database structure
// Docs: https://dbml.dbdiagram.io/docs
// Module: MF06 — Ingredient & Inventory Management
// Source: TDS SFMS-MF06-IMP-001 v1.1 | Flyway V10__mf06_ingredient_inventory.sql | V11__seed_mf06.sql

Table catalog_ingredients {
  id             uuid         [pk, default: `gen_random_uuid()`, note: "Internal PK"]
  code           varchar(64)  [unique, not null, note: "Global catalog key e.g. ING-THIT-LON-VAI. Referenced by dish_ingredients.ingredient_code (MF05) and every MF06 table via ingredient_id"]
  name           varchar(180) [not null]
  unit           varchar(12)  [not null, note: "kg | liter | piece | box"]
  storage_zone   varchar(10)  [not null, note: "BR-202: REQUIRED zone. CHECK IN ('DRY','CHILLED','FROZEN')"]
  refuse_factor  numeric(4,3) [not null, default: 0, note: "C7: edible-yield ratio used in BOM conversion. CHECK 0 <= x < 1"]
  active         boolean      [not null, default: true]

  Note: "System-wide catalog (no school_id) — code is UNIQUE globally. MF06 is the rightful owner (was previously referenced by MF05 without a FK target, M06-D03)."
}

Table supplier_products {
  id            uuid         [pk, default: `gen_random_uuid()`]
  code          varchar(32)  [not null, note: "e.g. SP-GF-014"]
  partner_id    uuid         [not null, note: "UUID ref -> partners(id) in MF04"]
  ingredient_id uuid         [not null, ref: > catalog_ingredients.id]
  unit_price    numeric(14,2)
  active        boolean      [not null, default: true]

  indexes {
    (partner_id, code) [unique, name: "uq_sp_partner_code"]
  }

  Note: "Supplier's offered product catalog. MF06-owned (M06-D03) — never existed before MF06."
}

Table purchase_requests {
  id                    uuid         [pk, default: `gen_random_uuid()`]
  code                  varchar(32)  [not null, note: "e.g. PR-2026-0815 (D1)"]
  school_id             uuid         [not null, note: "UUID ref -> schools(id) in MF02"]
  supplier_id           uuid         [note: "UUID ref -> partners(id) in MF04"]
  requested_by_user_id  uuid         [note: "D9: business field, NOT created_by. UUID ref -> user_accounts(id) in MF01"]
  requested_date        date         [not null]
  delivery_date         date         [not null]
  total_value           numeric(14,2) [note: "NULL until supplier quotes"]
  status                varchar(20)  [not null, default: "PENDING", note: "CHECK IN (PENDING,QUOTED,PRICE_ACCEPTED,APPROVED,COMPLETED,CANCELLED,REJECTED)"]
  source_menu_id        uuid         [note: "BR-077: set when auto-generated from a shortage. UUID ref -> menus(id) in MF05"]
  source_meal_type      varchar(12)
  service_date          date
  quoted_at             timestamptz
  quote_note            varchar(500)
  price_accepted_at     timestamptz
  accepted_at           timestamptz
  confirmed_delivery_at timestamptz  [note: "UC-015"]
  rejected_at           timestamptz
  cancelled_at          timestamptz
  cancel_reason         varchar(500) [note: "CHECK: required when status = CANCELLED"]

  indexes {
    (school_id, code)         [unique, name: "uq_pr_school_code"]
    (supplier_id, status)     [name: "ix_pr_supplier_status"]
    (school_id, requested_date) [name: "ix_pr_school_date"]
  }

  Note: "CF-M06-5 single-round pricing: PENDING -> QUOTED -> PRICE_ACCEPTED -> APPROVED -> COMPLETED, with REJECTED/CANCELLED side exits."
}

Table purchase_items {
  id                  uuid          [pk, default: `gen_random_uuid()`]
  purchase_request_id uuid          [not null, ref: > purchase_requests.id, note: "ON DELETE CASCADE"]
  ingredient_id       uuid          [not null, ref: > catalog_ingredients.id, note: "C6: keyed, not by name"]
  ingredient_label    varchar(180)  [not null, note: "Snapshot label at request time"]
  quantity            numeric(12,3) [not null, note: "CHECK > 0"]
  unit                varchar(12)   [not null]
  unit_price          numeric(14,2) [note: "Filled by supplier at quote step"]
  note                varchar(255)
  supplier_batch_id   uuid          [ref: > supplier_batches.id, note: "BR-071: assigned at confirm-schedule (nullable until then)"]

  Note: "Line item of a purchase request. lineTotal() = quantity * unit_price (derived, not persisted)."
}

Table supplier_batches {
  id                 uuid          [pk, default: `gen_random_uuid()`]
  code               varchar(32)   [not null, note: "e.g. BATCH-GF-4412"]
  partner_id         uuid          [not null, note: "UUID ref -> partners(id) in MF04"]
  product_id         uuid          [not null, ref: > supplier_products.id]
  lot_number         varchar(60)   [not null]
  manufacturing_date date          [not null]
  packaging_date     date          [not null, note: "CHECK >= manufacturing_date"]
  expiration_date    date          [not null, note: "CHECK > manufacturing_date"]
  quantity           numeric(12,3) [not null, note: "CHECK > 0"]
  unit               varchar(12)   [not null]
  status              varchar(16)  [not null, default: "ACTIVE", note: "CHECK IN (ACTIVE,DELIVERED,EXPIRED)"]

  indexes {
    (partner_id, code)   [unique, name: "uq_sb_partner_code"]
    (product_id, lot_number) [unique, name: "uq_sb_product_lot"]
  }

  Note: "BR-071: supplier-side registered batch, source of certification/traceability data."
}

Table batch_certificates {
  batch_id    uuid [not null, ref: > supplier_batches.id, note: "ON DELETE CASCADE"]
  document_id uuid [not null, note: "M06-D03: real table is partner_documents (MF04), not supplier_documents. UUID ref -> partner_documents(id) in MF04"]

  indexes {
    (batch_id, document_id) [pk]
  }

  Note: "M:N join — BR-071/UC-100 certificate attachments per batch."
}

Table deliveries {
  id                  uuid        [pk, default: `gen_random_uuid()`]
  code                varchar(32) [not null, note: "e.g. DLV-2026-1105"]
  school_id           uuid        [not null, note: "UUID ref -> schools(id) in MF02"]
  purchase_request_id uuid        [ref: > purchase_requests.id]
  scheduled_at        timestamptz [not null]
  driver_id           uuid        [note: "UUID ref -> delivery_persons(id) in MF04"]
  vehicle_plate       varchar(20) [not null]
  status              varchar(16) [not null, default: "SCHEDULED", note: "CHECK IN (SCHEDULED,DISPATCHED,IN_TRANSIT,DELAYED,DELIVERED,CANCELLED,COMPLETED)"]
  status_updated_at   timestamptz [note: "UC-092 audit trail"]

  indexes {
    (school_id, code)          [unique, name: "uq_dlv_school_code"]
    (school_id, scheduled_at)  [name: "ix_dlv_school_sched"]
  }

  Note: "UC-083/039/092/109. Deviation vs scheduled_at drives the delay alert (BR-... config delivery.deviation-alert-minutes)."
}

Table delivery_batches {
  delivery_id       uuid [not null, ref: > deliveries.id, note: "ON DELETE CASCADE"]
  supplier_batch_id uuid [not null, ref: > supplier_batches.id]
  ingredient_id     uuid [not null, ref: > catalog_ingredients.id]

  indexes {
    (delivery_id, supplier_batch_id, ingredient_id) [pk]
  }

  Note: "BR-071 Linked Batches — which supplier batches ride on this delivery."
}

Table receiving_sessions {
  id                         uuid        [pk, default: `gen_random_uuid()`]
  code                       varchar(32) [not null, note: "e.g. RCV-20260702-02"]
  school_id                  uuid        [not null, note: "UUID ref -> schools(id) in MF02"]
  delivery_id                uuid        [not null, ref: > deliveries.id]
  supplier_id                uuid        [not null, note: "UUID ref -> partners(id) in MF04"]
  supplier_status_at_receipt varchar(16) [not null, note: "BR-070: server-derived snapshot, never client-supplied"]
  received_at                timestamptz [not null, default: `now()`, note: "BR-073: SERVER writes this, not the client"]
  received_by_user_id        uuid        [not null, note: "UUID ref -> user_accounts(id) in MF01"]
  driver_verification_id     varchar(80) [note: "Face Recognition match id (CF-M06-8)"]
  lat                        numeric(9,6)
  lng                        numeric(9,6) [note: "BR-074: geofence check"]
  gps_verified               boolean     [not null, default: false]
  photos_count                int        [not null, default: 0, note: "CHECK >= 0"]
  device_info                varchar(200) [note: "BR-073"]
  status                     varchar(20) [not null, note: "CHECK IN (IN_PROGRESS,RECEIVED,RECEIVED_PARTIAL,REJECTED)"]

  indexes {
    (school_id, code)   [unique, name: "uq_rcv_school_code"]
    (school_id, received_at) [name: "ix_rcv_school_date"]
  }

  Note: "ADR-M06-5: created as ONE atomic backend transaction covering session + lines + inventory batch upsert + delivery status update. CHECK: status=IN_PROGRESS OR photos_count>=1 (BR-072)."
}

Table receiving_lines {
  id                UUID          [pk, default: `gen_random_uuid()`]
  session_id        uuid          [not null, ref: > receiving_sessions.id, note: "ON DELETE CASCADE"]
  supplier_batch_id uuid          [not null, ref: > supplier_batches.id]
  ingredient_id     uuid          [not null, ref: > catalog_ingredients.id]
  ingredient_label  varchar(180)  [not null]
  storage_zone      varchar(10)   [not null, note: "BR-202. CHECK IN (DRY,CHILLED,FROZEN)"]
  rejected          boolean       [not null, default: false]
  reject_reason     varchar(500)
  reject_photo_keys jsonb         [not null, default: "[]", note: "D10-BIS.2: private document ids, not public URLs"]

  Note: "BR-072/078: CHECK — rejected=false OR (reject_reason IS NOT NULL AND >=1 reject_photo_keys)."
}

Table inventory_batches {
  id                     uuid          [pk, default: `gen_random_uuid()`]
  code                   varchar(32)   [not null, note: "Supplier-assigned batch code (BR-076), public id — NOT the surrogate PK"]
  school_id              uuid          [not null, note: "UUID ref -> schools(id) in MF02"]
  ingredient_id          uuid          [not null, ref: > catalog_ingredients.id]
  ingredient_label       varchar(180)  [not null, note: "Snapshot label"]
  supplier_id            uuid          [not null, note: "UUID ref -> partners(id) in MF04"]
  initial_quantity       numeric(12,3) [not null, note: "ADR-M06-1: IMMUTABLE after receipt. CHECK > 0"]
  unit                   varchar(12)   [not null]
  storage_zone           varchar(10)   [not null, note: "BR-202. CHECK IN (DRY,CHILLED,FROZEN)"]
  received_date          date          [not null]
  expiry_date            date          [not null, note: "CHECK > received_date"]
  receiving_session_id   uuid          [ref: > receiving_sessions.id, note: "BR-076: traceability root"]
  quarantine_at          timestamptz   [note: "BR-149, ADR-M06-2: independent axis from expiry"]
  quarantine_by_user_id  uuid          [note: "UUID ref -> user_accounts(id) in MF01"]
  quarantine_reason      varchar(500)
  quarantine_incident_id uuid          [note: "ref MF11, ID-only"]

  indexes {
    (school_id, code)                        [unique, name: "uq_batch_school_code"]
    (school_id, ingredient_id, expiry_date)  [name: "ix_batch_fefo"]
    (receiving_session_id)                    [name: "ix_batch_session"]
  }

  Note: "ADR-M06-1: available quantity is NEVER stored — computed at runtime as initial_quantity + SUM(inventory_transactions.delta). shelfStatus() and isLocked() (EXPIRED or quarantined) are derived, not columns. CHECK: quarantine_at IS NULL OR (quarantine_by_user_id AND quarantine_reason NOT NULL)."
}

Table issue_slips {
  id                 uuid        [pk, default: `gen_random_uuid()`]
  code               varchar(32) [not null, note: "e.g. ISS-2026-0031 (D1)"]
  school_id          uuid        [not null, note: "UUID ref -> schools(id) in MF02"]
  service_date       date        [not null]
  meal_type          varchar(12) [not null]
  menu_ids           "uuid[]"    [not null]
  issued_by_user_id  uuid        [not null, note: "D9: NOT created_by (reserved BaseEntity column). UUID ref -> user_accounts(id) in MF01"]
  status             varchar(12) [not null, default: "DRAFT", note: "CHECK IN (DRAFT,CONFIRMED,POSTED)"]
  note               varchar(500)

  indexes {
    (school_id, code)                       [unique, name: "uq_slip_school_code"]
    (school_id, service_date, meal_type)    [unique, name: "uq_slip_school_date_meal"]
  }

  Note: "ADR-M06-6: real parent table for issue lines (not an implicit join by date+mealType). Current service always creates it directly as POSTED."
}

Table inventory_transactions {
  id                    uuid          [pk, default: `gen_random_uuid()`]
  code                  varchar(32)   [not null, note: "e.g. TXN-20260728-01 (D1)"]
  school_id             uuid          [not null, note: "UUID ref -> schools(id) in MF02"]
  occurred_at           timestamptz   [not null, default: `now()`]
  type                  varchar(12)   [not null, note: "CHECK IN (RECEIPT,ISSUE,ADJUSTMENT)"]
  batch_id              uuid          [not null, ref: > inventory_batches.id]
  ingredient_id         uuid          [not null, ref: > catalog_ingredients.id]
  delta                 numeric(12,3) [not null, note: "Signed. CHECK <> 0"]
  unit                  varchar(12)   [not null]
  performed_by_user_id  uuid          [not null, note: "D9: business field 'who performed it', NOT actor_id. UUID ref -> user_accounts(id) in MF01"]
  reason_code           varchar(24)   [note: "BR-204: CHECK IN (SPOILAGE,COUNT_CORRECTION,DAMAGE,RETURN_TO_SUPPLIER), required for ADJUSTMENT"]
  manager_signature     varchar(180)  [note: "BR-204: required for ADJUSTMENT"]
  issue_slip_id         uuid          [ref: > issue_slips.id]
  menu_id               uuid          [note: "UUID ref -> menus(id) in MF05"]
  dish_id               uuid          [note: "C6: keyed. UUID ref -> dishes(id) in MF05"]
  dish_label             varchar(180) [note: "Snapshot label"]
  meal_type              varchar(12)
  service_date           date

  indexes {
    (school_id, code)          [unique, name: "uq_tx_school_code"]
    (batch_id)                 [name: "ix_tx_batch"]
    (issue_slip_id)            [name: "ix_tx_slip"]
    (service_date, meal_type)  [name: "ix_tx_service"]
    (menu_id, dish_id)         [name: "ix_tx_trace"]
  }

  Note: "APPEND-ONLY (ADR-M06-1) — no UPDATE/DELETE (ING-405 on those verbs). CHECK ck_tx_adjustment: ADJUSTMENT requires reason_code+manager_signature. CHECK ck_tx_issue_trace: ISSUE requires issue_slip_id+dish_id+service_date+meal_type (BR-076). CHECK ck_tx_sign: RECEIPT delta>0, ISSUE delta<0."
}

Table preparation_records {
  id           uuid          [pk, default: `gen_random_uuid()`]
  code         varchar(32)   [not null, note: "e.g. PREP-20260707-02"]
  school_id    uuid          [not null, note: "UUID ref -> schools(id) in MF02"]
  date         date          [not null]
  dish_id      uuid          [not null, note: "UUID ref -> dishes(id) in MF05"]
  dish_label   varchar(180)  [not null]
  menu_id      uuid          [note: "UUID ref -> menus(id) in MF05"]
  meal_type    varchar(12)
  start_time   time          [not null]
  end_time     time          [not null, note: "CHECK > start_time"]
  cook_user_id uuid          [not null, note: "UUID ref -> user_accounts(id) in MF01"]
  notes        varchar(1000)

  indexes {
    (school_id, code) [unique, name: "uq_prep_school_code"]
  }

  Note: "UC-066: kitchen prep log, links back to the batches actually consumed."
}

Table preparation_batches {
  preparation_record_id uuid    [not null, ref: > preparation_records.id, note: "ON DELETE CASCADE"]
  batch_id               uuid   [not null, ref: > inventory_batches.id]
  extra                  boolean [not null, default: false, note: "true = batch added manually outside the issue slip, for audit"]

  indexes {
    (preparation_record_id, batch_id) [pk]
  }
}

Table receiving_evidence_documents {
  id           uuid        [pk, default: `gen_random_uuid()`]
  code         varchar(32) [unique, not null]
  school_id    uuid        [not null, note: "UUID ref -> schools(id) in MF02"]
  file_key     varchar(255) [not null, note: "D10-BIS.2: private storage key, not a public URL"]
  content_type varchar(100)
  file_size    "bigint"

  Note: "Rejection-evidence photos referenced by receiving_lines.reject_photo_keys via document id/code — never inline URLs."
}

// ── Relationships summary ──────────────────────────────────────────────────
// purchase_items.purchase_request_id  → purchase_requests.id  (CASCADE)
// purchase_items.supplier_batch_id    → supplier_batches.id   (BR-071, set at confirm-schedule)
// supplier_batches.product_id         → supplier_products.id
// batch_certificates.*                → supplier_batches.id / partner_documents.id (MF04)
// deliveries.purchase_request_id      → purchase_requests.id
// delivery_batches.*                  → deliveries.id / supplier_batches.id / catalog_ingredients.id
// receiving_sessions.delivery_id      → deliveries.id
// receiving_lines.session_id          → receiving_sessions.id (CASCADE)
// inventory_batches.receiving_session_id → receiving_sessions.id (BR-076 traceability root)
// inventory_transactions.batch_id     → inventory_batches.id
// inventory_transactions.issue_slip_id→ issue_slips.id (ADR-M06-6)
// preparation_batches.*               → preparation_records.id (CASCADE) / inventory_batches.id
// catalog_ingredients.code            → dish_ingredients.ingredient_code (MF05, added in V11 after seed — M06-D14)
```

---

## 1. Purchasing & Supplier Quoting

### 1.1 Class Diagram

```plantuml
@startuml MF06_Purchasing_ClassDiagram
title Design-level Class Diagram: Purchasing & Supplier Quoting

' ==========================================
' LAYOUT AND COLOR CONFIGURATION (RATIONAL ROSE)
' ==========================================
skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF
hide empty members

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

enum PurchaseRequestStatus {
  PENDING
  QUOTED
  PRICE_ACCEPTED
  APPROVED
  COMPLETED
  CANCELLED
  REJECTED
}

class PurchaseRequest <<entity>> {
  +id : UUID <<PK>>
  +code : String <<business id>>
  +schoolId : UUID
  +supplierId : UUID
  +requestedByUserId : UUID
  +requestedDate : LocalDate
  +deliveryDate : LocalDate
  +totalValue : BigDecimal
  +status : PurchaseRequestStatus
  +sourceMenuId : UUID
  +sourceMealType : MealType
  +serviceDate : LocalDate
  +quotedAt : Instant
  +quoteNote : String
  +priceAcceptedAt : Instant
  +confirmedDeliveryAt : Instant
  +cancelReason : String
}

class PurchaseItem <<entity>> {
  +id : UUID <<PK>>
  +purchaseRequestId : UUID <<FK>>
  +ingredientId : UUID <<FK>>
  +ingredientLabel : String
  +quantity : BigDecimal
  +unit : String
  +unitPrice : BigDecimal
  +note : String
  +supplierBatchId : UUID <<FK>>
  +lineTotal() : BigDecimal
}

class CatalogIngredient <<entity>> {
  +id : UUID <<PK>>
  +code : String <<unique>>
  +name : String
  +unit : String
  +storageZone : StorageZone
  +refuseFactor : BigDecimal
  +active : boolean
}

interface IPurchaseRequestService <<interface>> {
  +create(cmd : CreateRequest, actor : AuthUser) : PurchaseRequestResponse
  +quote(code : String, cmd : QuoteRequest, actor : AuthUser) : PurchaseRequestResponse
  +acceptPrice(code : String, actor : AuthUser) : PurchaseRequestResponse
  +confirmSchedule(code : String, cmd : ConfirmScheduleRequest, actor : AuthUser) : PurchaseRequestResponse
  +cancel(code : String, cmd : CancelRequest, actor : AuthUser) : PurchaseRequestResponse
  +reject(code : String, cmd : RejectRequest, actor : AuthUser) : PurchaseRequestResponse
  +list(query : ListQuery, actor : AuthUser) : PagedResponse<PurchaseRequestResponse>
  +detail(code : String, actor : AuthUser) : PurchaseRequestResponse
}

class PurchaseRequestService <<control>> {
  -requestRepository : PurchaseRequestRepository
  -itemRepository : PurchaseItemRepository
  -supplierBatchRepository : SupplierBatchRepository
  -referenceResolver : ReferenceResolver
  -codeGenerator : CodeGenerator
  -clock : Clock
  +create(cmd, actor) : PurchaseRequestResponse
  +quote(code, cmd, actor) : PurchaseRequestResponse
  +acceptPrice(code, actor) : PurchaseRequestResponse
  +confirmSchedule(code, cmd, actor) : PurchaseRequestResponse
  +cancel(code, cmd, actor) : PurchaseRequestResponse
  +reject(code, cmd, actor) : PurchaseRequestResponse
  -loadInScope(code, actor) : PurchaseRequest
  -toResponse(pr) : PurchaseRequestResponse
}

class PurchaseRequestController <<boundary>> {
  -service : IPurchaseRequestService
  +list(query, actor) : PagedResponse<PurchaseRequestResponse>
  +detail(code, actor) : PurchaseRequestResponse
  +create(body, actor) : PurchaseRequestResponse
  +quote(code, body, actor) : PurchaseRequestResponse
  +acceptPrice(code, actor) : PurchaseRequestResponse
  +confirmSchedule(code, body, actor) : PurchaseRequestResponse
  +cancel(code, body, actor) : PurchaseRequestResponse
  +reject(code, body, actor) : PurchaseRequestResponse
}

interface PurchaseRequestRepository <<repository>> {
  +findByCodeAndSchoolId(code : String, schoolId : UUID) : Optional<PurchaseRequest>
  +findBySupplierIdAndStatus(supplierId : UUID, status : PurchaseRequestStatus) : Page<PurchaseRequest>
  +save(pr : PurchaseRequest) : PurchaseRequest
}

interface PurchaseItemRepository <<repository>> {
  +findByPurchaseRequestId(prId : UUID) : List<PurchaseItem>
  +saveAll(items : List<PurchaseItem>) : List<PurchaseItem>
}

interface CatalogIngredientRepository <<repository>> {
  +findByCode(code : String) : Optional<CatalogIngredient>
  +findAllByCodeIn(codes : List<String>) : List<CatalogIngredient>
}

interface SupplierBatchRepository <<repository>> {
  +findByCodeAndPartnerId(code : String, partnerId : UUID) : Optional<SupplierBatch>
}

class ReferenceResolver <<control>> {
  +partnerStatus(partnerId : UUID) : String
  +schoolIdOf(actor : AuthUser) : UUID
}

class CodeGenerator <<utility>> {
  +next(prefix : String, schoolId : UUID) : String
}

IPurchaseRequestService <|.. PurchaseRequestService
PurchaseRequestController --> IPurchaseRequestService : delegates

PurchaseRequestService --> PurchaseRequestRepository : reads/writes
PurchaseRequestService --> PurchaseItemRepository : reads/writes
PurchaseRequestService --> CatalogIngredientRepository : resolves by code (C6)
PurchaseRequestService --> SupplierBatchRepository : resolves at confirm-schedule (BR-071)
PurchaseRequestService --> ReferenceResolver : scope + partner status
PurchaseRequestService --> CodeGenerator : generates PR code (D1)

PurchaseRequestRepository ..> PurchaseRequest : manages
PurchaseItemRepository ..> PurchaseItem : manages
CatalogIngredientRepository ..> CatalogIngredient : manages

PurchaseRequest "1" *-- "n" PurchaseItem : contains
PurchaseItem "n" --> "1" CatalogIngredient : keyed by ingredientId
PurchaseRequest --> PurchaseRequestStatus : status

@enduml
```

**Description:**
`PurchaseRequestService` implements the CF-M06-5 single-round pricing workflow: Service Caterers create a `PurchaseRequest` with `PurchaseItem` lines resolved against `CatalogIngredient` (by key, not name — C6). The supplier fills `unitPrice` per line at `quote()`, and the school accepts the total once via `acceptPrice()` (no re-quote loop). `confirmSchedule()` (BR-071) optionally attaches a `supplierBatchId` per line, tying the purchase directly to registered supplier batches ahead of delivery. `cancel()`/`reject()` are terminal side exits available at different points in the state machine (see §8.1). All persistence goes through Spring Data repositories — the service never touches the `EntityManager` directly.

---

### 1.2 Sequence Diagram — Create Purchase Request & Supplier Quote (UC-021, CF-M06-5)

```plantuml
@startuml MF06_CreatePR_Quote
title Sequence Diagram: Create Purchase Request & Supplier Quote

' Enable orthogonal (right-angle) connector lines
skinparam linetype ortho

' ==========================================
' GREEN COLOR THEME CONFIGURATION
' ==========================================
skinparam backgroundColor #FFFFFF
skinparam sequenceMessageAlign center
skinparam responseMessageBelowArrow true

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
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

actor "Service Caterers" as SC
actor "Raw Food Supplier" as SUP
boundary ": PurchaseRequestScreen" as UI <<boundary>>
control ": PurchaseRequestController" as C <<control>>
control ": PurchaseRequestService" as S <<control>>
entity "pr : PurchaseRequest" as PR <<entity>>
entity ": CatalogIngredientRepository" as CIR <<repository>>
entity ": PurchaseRequestRepository" as PRR <<repository>>
entity ": PurchaseItemRepository" as PIR <<repository>>
database ": PostgreSQL" as DB <<database>>

== Create Purchase Request (School side) ==

SC -> UI : open "New Purchase Request"
activate UI
UI --> SC : render supplier / ingredient / quantity form

SC -> UI : submit { supplierId, deliveryDate, items[] }
UI -> C : POST /api/v1/purchase-requests
activate C
C -> S : create(CreateRequest, actor)
activate S

loop for each item in items[]
  S -> CIR : findByCode(ingredientCode)
  activate CIR
  CIR -> DB : SELECT * FROM catalog_ingredients WHERE code = ?
  activate DB
  DB --> CIR : row
  deactivate DB
  CIR --> S : Optional<CatalogIngredient> — C6 keyed resolve
  deactivate CIR
end

S -> PR : new PurchaseRequest(status = PENDING,\nrequestedByUserId = actor.userId)
activate PR
PR --> S : pr
deactivate PR

S -> PRR : save(pr)
activate PRR
PRR -> DB : INSERT INTO purchase_requests
activate DB
DB --> PRR : 1 row (id)
deactivate DB
PRR --> S : PurchaseRequest (persisted)
deactivate PRR

S -> PIR : saveAll(items[])
activate PIR
PIR -> DB : INSERT INTO purchase_items\n{ ingredientId, ingredientLabel snapshot, quantity, unit }
activate DB
DB --> PIR : n rows
deactivate DB
PIR --> S : List<PurchaseItem>
deactivate PIR

S --> C : PurchaseRequestResponse
deactivate S
C --> UI : 201 Created + PurchaseRequestResponse
deactivate C
UI --> SC : show "PR created" + PR code (MSG-041)
deactivate UI

... Supplier reviews pending requests ...

== Supplier Quote (Supplier side) ==

SUP -> UI : open pending PR {code} and enter unit prices
activate UI
UI -> C : POST /api/v1/purchase-requests/{code}/quote\n{ items:[{ ingredientId, unitPrice }], quoteNote }
activate C
C -> S : quote(code, QuoteRequest, actor)
activate S

S -> PRR : findByCodeAndSupplierId(code, actor.partnerId) — BR-007
activate PRR
PRR -> DB : SELECT * FROM purchase_requests WHERE code = ?
activate DB
DB --> PRR : row
deactivate DB
PRR --> S : PurchaseRequest
deactivate PRR

alt status != PENDING
  S --> C : throw IngExceptions.conflict()
  C --> UI : 409 ING-409 "Request is not in PENDING state"
  UI --> SUP : show error message
else status == PENDING
  S -> PIR : saveAll(items with unitPrice)
  activate PIR
  PIR -> DB : UPDATE purchase_items SET unit_price = ?
  activate DB
  DB --> PIR : n rows
  deactivate DB
  PIR --> S : List<PurchaseItem>
  deactivate PIR

  S -> PR : applyQuote(totalValue = SUM(quantity * unitPrice),\nquotedAt = now(), status = QUOTED)
  activate PR
  PR --> S : pr (QUOTED)
  deactivate PR

  S -> PRR : save(pr)
  activate PRR
  PRR -> DB : UPDATE purchase_requests\nSET total_value, quoted_at, quote_note, status
  activate DB
  DB --> PRR : 1 row
  deactivate DB
  PRR --> S : PurchaseRequest
  deactivate PRR

  S --> C : PurchaseRequestResponse
  C --> UI : 200 OK + PurchaseRequestResponse
  UI --> SUP : show "Quote submitted" + totalValue
end
deactivate S
deactivate C
deactivate UI
@enduml
```

**Description:**
Two actors interact with the same aggregate across time, always through the UI boundary → controller → service → repository chain. The school creates the request in `PENDING`; the supplier is the only one who may fill unit prices via `quote()`, which is blocked unless the request is still `PENDING` (idempotency/race guard). `totalValue` is computed server-side from the line items — never trusted from the client — and the request transitions to `QUOTED`. Every repository call returns its result explicitly so the read/write boundary between the service and PostgreSQL stays visible.

---

### 1.3 Sequence Diagram — Accept Price & Confirm Delivery Schedule (UC-015, BR-071)

```plantuml
@startuml MF06_AcceptPrice_ConfirmSchedule
title Sequence Diagram: Accept Price & Confirm Delivery Schedule

skinparam linetype ortho
skinparam backgroundColor #FFFFFF
skinparam sequenceMessageAlign center
skinparam responseMessageBelowArrow true

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
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

actor "Service Caterers" as SC
actor "Raw Food Supplier" as SUP
boundary ": PurchaseRequestScreen" as UI <<boundary>>
control ": PurchaseRequestController" as C <<control>>
control ": PurchaseRequestService" as S <<control>>
entity "pr : PurchaseRequest" as PR <<entity>>
entity ": PurchaseRequestRepository" as PRR <<repository>>
entity ": PurchaseItemRepository" as PIR <<repository>>
entity ": SupplierBatchRepository" as SBR <<repository>>
database ": PostgreSQL" as DB <<database>>

== Accept Price (School side) ==

SC -> UI : open QUOTED request {code}, review totalValue
activate UI
UI --> SC : show quoted lines + total
SC -> UI : click "Accept price"
UI -> C : POST /api/v1/purchase-requests/{code}/accept-price
activate C
C -> S : acceptPrice(code, actor)
activate S

S -> PRR : findByCodeAndSchoolId(code, actor.schoolId)
activate PRR
PRR -> DB : SELECT * FROM purchase_requests WHERE code = ?
activate DB
DB --> PRR : row
deactivate DB
PRR --> S : PurchaseRequest
deactivate PRR

alt status != QUOTED
  S --> C : throw IngExceptions.conflict()
  C --> UI : 409 ING-409 "Request is not in QUOTED state"
  UI --> SC : show error message
else status == QUOTED
  S -> PR : acceptPrice(priceAcceptedAt = now(),\nstatus = PRICE_ACCEPTED)
  activate PR
  PR --> S : pr (PRICE_ACCEPTED)
  deactivate PR
  S -> PRR : save(pr)
  activate PRR
  PRR -> DB : UPDATE purchase_requests\nSET price_accepted_at, status
  activate DB
  DB --> PRR : 1 row
  deactivate DB
  PRR --> S : PurchaseRequest
  deactivate PRR
  S --> C : PurchaseRequestResponse
  C --> UI : 200 OK + PurchaseRequestResponse
  UI --> SC : show "Price accepted"
end
deactivate S
deactivate C
deactivate UI

... Supplier schedules the delivery ...

== Confirm Schedule + Batch Assignment (Supplier side) ==

SUP -> UI : open PRICE_ACCEPTED request, map each line\nto one of its registered batches
activate UI
UI -> C : POST /api/v1/purchase-requests/{code}/confirm-schedule\n{ batchAssignments: { ingredientCode: supplierBatchCode } }
activate C
C -> S : confirmSchedule(code, ConfirmScheduleRequest, actor)
activate S

S -> PRR : findByCodeAndSupplierId(code, actor.partnerId)
activate PRR
PRR -> DB : SELECT * FROM purchase_requests WHERE code = ?
activate DB
DB --> PRR : row
deactivate DB
PRR --> S : PurchaseRequest
deactivate PRR

alt status != PRICE_ACCEPTED
  S --> C : throw IngExceptions.conflict()
  C --> UI : 409 ING-409 "Request is not in PRICE_ACCEPTED state"
  UI --> SUP : show error message
else status == PRICE_ACCEPTED
  loop for each entry in batchAssignments
    S -> SBR : findByCodeAndPartnerId(batchCode, actor.partnerId)
    activate SBR
    SBR -> DB : SELECT * FROM supplier_batches WHERE code = ?
    activate DB
    DB --> SBR : row
    deactivate DB
    SBR --> S : SupplierBatch
    deactivate SBR

    S -> PIR : assignBatch(purchaseRequestId, ingredientId, batch.id)
    activate PIR
    PIR -> DB : UPDATE purchase_items SET supplier_batch_id = ?\nWHERE purchase_request_id = ? AND ingredient_id = ?
    activate DB
    DB --> PIR : 1 row
    deactivate DB
    PIR --> S : PurchaseItem (updated)
    deactivate PIR
  end

  S -> PR : approve(confirmedDeliveryAt = now(), status = APPROVED)
  activate PR
  PR --> S : pr (APPROVED)
  deactivate PR
  S -> PRR : save(pr)
  activate PRR
  PRR -> DB : UPDATE purchase_requests\nSET confirmed_delivery_at, status
  activate DB
  DB --> PRR : 1 row
  deactivate DB
  PRR --> S : PurchaseRequest
  deactivate PRR
  S --> C : PurchaseRequestResponse
  C --> UI : 200 OK + PurchaseRequestResponse
  UI --> SUP : show "Delivery schedule confirmed"
end
deactivate S
deactivate C
deactivate UI
@enduml
```

**Description:**
`acceptPrice()` is a one-way gate from `QUOTED` to `PRICE_ACCEPTED` — there is no re-quote step (CF-M06-5 deliberately has a single pricing round). `confirmSchedule()` is where BR-071 traceability begins: the supplier binds each purchase line to a specific, already-certified `SupplierBatch` resolved through `SupplierBatchRepository` under its own `partnerId`, so that when the physical delivery later arrives, the receiving flow (§4) can trust the batch's certification data. The request becomes `APPROVED`, ready for a `Delivery` to be registered against it (§3).

---

## 2. Supplier Batch & Certification Management

### 2.1 Class Diagram

```plantuml
@startuml MF06_SupplierBatch_ClassDiagram
title Design-level Class Diagram: Supplier Batch & Certification

skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF
hide empty members

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

enum SupplierBatchStatus {
  ACTIVE
  DELIVERED
  EXPIRED
}

class SupplierProduct <<entity>> {
  +id : UUID <<PK>>
  +code : String
  +partnerId : UUID <<FK>>
  +ingredientId : UUID <<FK>>
  +unitPrice : BigDecimal
  +active : boolean
}

class SupplierBatch <<entity>> {
  +id : UUID <<PK>>
  +code : String <<business id>>
  +partnerId : UUID <<FK>>
  +productId : UUID <<FK>>
  +lotNumber : String
  +manufacturingDate : LocalDate
  +packagingDate : LocalDate
  +expirationDate : LocalDate
  +quantity : BigDecimal
  +unit : String
  +status : SupplierBatchStatus
}

class BatchCertificate <<join>> {
  +batchId : UUID <<FK>>
  +documentId : UUID <<FK>>
}

interface ISupplierBatchService <<interface>> {
  +create(cmd : CreateRequest, actor : AuthUser) : SupplierBatchResponse
  +update(code : String, cmd : UpdateRequest, actor : AuthUser) : SupplierBatchResponse
  +uploadCertificate(code : String, file : MultipartFile, actor : AuthUser) : String
  +list(query : ListQuery, actor : AuthUser) : PagedResponse<SupplierBatchResponse>
  +detail(code : String, actor : AuthUser) : SupplierBatchResponse
}

class SupplierBatchService <<control>> {
  -batchRepository : SupplierBatchRepository
  -productRepository : SupplierProductRepository
  -referenceResolver : ReferenceResolver
  -storageService : StorageService
  -fileValidation : FileValidationSupport
  -jdbc : JdbcTemplate
  +create(cmd, actor) : SupplierBatchResponse
  +update(code, cmd, actor) : SupplierBatchResponse
  +uploadCertificate(code, file, actor) : String
  -validateCertDates(cmd) : void
}

class SupplierBatchController <<boundary>> {
  -service : ISupplierBatchService
  +list(query, actor) : PagedResponse<SupplierBatchResponse>
  +detail(code, actor) : SupplierBatchResponse
  +create(body, actor) : SupplierBatchResponse
  +update(code, body, actor) : SupplierBatchResponse
  +uploadCertificate(code, file, actor) : String
}

interface SupplierBatchRepository <<repository>> {
  +findByCodeAndPartnerId(code : String, partnerId : UUID) : Optional<SupplierBatch>
  +existsByProductIdAndLotNumber(productId : UUID, lot : String) : boolean
  +save(batch : SupplierBatch) : SupplierBatch
}

interface SupplierProductRepository <<repository>> {
  +findByIdAndPartnerId(id : UUID, partnerId : UUID) : Optional<SupplierProduct>
}

interface StorageService <<interface>> {
  +store(file : MultipartFile) : String
}

class FileValidationSupport <<utility>> {
  +validate(file : MultipartFile) : void
}

ISupplierBatchService <|.. SupplierBatchService
SupplierBatchController --> ISupplierBatchService : delegates

SupplierBatchService --> SupplierBatchRepository : reads/writes
SupplierBatchService --> SupplierProductRepository : resolves product
SupplierBatchService --> StorageService : stores certificate file
SupplierBatchService --> FileValidationSupport : validates upload
SupplierBatchService ..> BatchCertificate : inserts (JdbcTemplate)

SupplierBatchRepository ..> SupplierBatch : manages
SupplierProductRepository ..> SupplierProduct : manages

SupplierBatch "1" --> "n" BatchCertificate : certified by
SupplierProduct "1" --> "n" SupplierBatch : produces
SupplierBatch --> SupplierBatchStatus : status

@enduml
```

**Description:**
`SupplierBatchService.create()` enforces BR-071 at the database boundary before persistence: `expirationDate > manufacturingDate` and `packagingDate >= manufacturingDate`, else `IngExceptions.batchCertInvalid()` (ING-071). `uploadCertificate()` stores the file via `StorageService`, inserts a `partner_documents` row (type `PRODUCT_ORIGIN`, MF04-owned table) and links it through the `batch_certificates` join — no certificate content is duplicated into MF06.

---

### 2.2 Sequence Diagram — Register Supplier Batch & Upload Certificate (UC-081/100, BR-071)

```plantuml
@startuml MF06_RegisterBatch
title Sequence Diagram: Register Supplier Batch & Upload Certificate

skinparam linetype ortho
skinparam backgroundColor #FFFFFF
skinparam sequenceMessageAlign center
skinparam responseMessageBelowArrow true

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
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

actor "Raw Food Supplier" as SUP
boundary ": SupplierBatchScreen" as UI <<boundary>>
control ": SupplierBatchController" as C <<control>>
control ": SupplierBatchService" as S <<control>>
entity "batch : SupplierBatch" as SB <<entity>>
entity ": SupplierProductRepository" as SPR <<repository>>
entity ": SupplierBatchRepository" as SBR <<repository>>
control ": StorageService" as ST <<interface>>
database ": PostgreSQL" as DB <<database>>

== Register batch ==

SUP -> UI : open "Register batch" form
activate UI
UI --> SUP : render product / lot / date fields
SUP -> UI : submit { productId, lotNumber, manufacturingDate,\npackagingDate, expirationDate, quantity, unit }
UI -> C : POST /api/v1/supplier-batches
activate C
C -> S : create(CreateRequest, actor)
activate S

S -> S : validateCertDates(cmd)\nexpirationDate > manufacturingDate\n&& packagingDate >= manufacturingDate

alt Invalid dates
  S --> C : throw IngExceptions.batchCertInvalid()
  C --> UI : 422 ING-071 (MSG-149) "Batch certification data invalid" — BR-071
  UI --> SUP : highlight invalid date fields
else Valid dates
  S -> SPR : findByIdAndPartnerId(productId, actor.partnerId)
  activate SPR
  SPR -> DB : SELECT * FROM supplier_products WHERE id = ?
  activate DB
  DB --> SPR : row
  deactivate DB
  SPR --> S : SupplierProduct
  deactivate SPR

  S -> S : code = "BATCH-" + random8Char() [if not supplied]
  S -> SB : new SupplierBatch(status = ACTIVE)
  activate SB
  SB --> S : batch
  deactivate SB

  S -> SBR : save(batch)
  activate SBR
  SBR -> DB : INSERT INTO supplier_batches
  activate DB
  DB --> SBR : 1 row (id)
  deactivate DB
  SBR --> S : SupplierBatch (persisted)
  deactivate SBR

  S --> C : SupplierBatchResponse
  C --> UI : 201 Created + SupplierBatchResponse
  UI --> SUP : show batch code "BATCH-…"
end
deactivate S
deactivate C
deactivate UI

... Supplier attaches origin certificate (UC-100) ...

== Upload certificate ==

SUP -> UI : choose certificate file for batch {code}
activate UI
UI -> C : POST /api/v1/supplier-batches/{code}/certificates\n(multipart file)
activate C
C -> S : uploadCertificate(code, file, actor)
activate S

S -> SBR : findByCodeAndPartnerId(code, actor.partnerId)
activate SBR
SBR -> DB : SELECT * FROM supplier_batches WHERE code = ?
activate DB
DB --> SBR : row
deactivate DB
SBR --> S : SupplierBatch
deactivate SBR

S -> S : fileValidation.validate(file)
S -> ST : store(file)
activate ST
ST --> S : fileKey
deactivate ST

S -> DB : INSERT INTO partner_documents\n{ type = PRODUCT_ORIGIN, fileKey } (JdbcTemplate)
activate DB
DB --> S : documentId
deactivate DB

S -> DB : INSERT INTO batch_certificates { batchId, documentId }
activate DB
DB --> S : 1 row
deactivate DB

S --> C : documentId
deactivate S
C --> UI : 200 OK { documentId }
deactivate C
UI --> SUP : show certificate attached
deactivate UI
@enduml
```

**Description:**
Certification is validated purely on structural dates before the batch even exists in the system — a batch that fails BR-071 never reaches the database. The certificate upload is a separate call so a supplier can register the batch first and attach paperwork afterward (UC-100), matching the actual two-step supplier workflow. Both `SupplierBatch` and its certificates are the traceability foundation that `ReceivingSessionService` (§4) and `TraceabilityService` (§7) later depend on. The batch lifecycle itself is shown in §8.3.

---

## 3. Delivery Management

### 3.1 Class Diagram

```plantuml
@startuml MF06_Delivery_ClassDiagram
title Design-level Class Diagram: Delivery Management

skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF
hide empty members

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

enum DeliveryStatus {
  SCHEDULED
  DISPATCHED
  IN_TRANSIT
  DELAYED
  DELIVERED
  CANCELLED
  COMPLETED
}

class Delivery <<entity>> {
  +id : UUID <<PK>>
  +code : String <<business id>>
  +schoolId : UUID
  +purchaseRequestId : UUID <<FK>>
  +scheduledAt : Instant
  +driverId : UUID
  +vehiclePlate : String
  +status : DeliveryStatus
  +statusUpdatedAt : Instant
}

interface IDeliveryService <<interface>> {
  +create(cmd : CreateRequest, actor : AuthUser) : DeliveryResponse
  +updateStatus(code : String, status : String, actor : AuthUser) : DeliveryResponse
  +list(query : ListQuery, actor : AuthUser) : PagedResponse<DeliveryResponse>
  +detail(code : String, actor : AuthUser) : DeliveryResponse
}

class DeliveryService <<control>> {
  -deliveryRepository : DeliveryRepository
  -purchaseRequestRepository : PurchaseRequestRepository
  -supplierBatchRepository : SupplierBatchRepository
  -supplierProductRepository : SupplierProductRepository
  -referenceResolver : ReferenceResolver
  -codeGenerator : CodeGenerator
  -jdbc : JdbcTemplate
  -clock : Clock
  -deviationAlertMinutes : int
  +create(cmd, actor) : DeliveryResponse
  +updateStatus(code, status, actor) : DeliveryResponse
  -computeDeviation(scheduledAt, now) : Duration
  -toResponse(delivery) : DeliveryResponse
}

class DeliveryController <<boundary>> {
  -service : IDeliveryService
  -faceRecognitionPort : FaceRecognitionPort
  +list(query, actor) : PagedResponse<DeliveryResponse>
  +detail(code, actor) : DeliveryResponse
  +create(body, actor) : DeliveryResponse
  +updateStatus(code, body, actor) : DeliveryResponse
  +verifyDriverFace(code, image, actor) : Map
}

interface DeliveryRepository <<repository>> {
  +findByCodeAndSchoolId(code : String, schoolId : UUID) : Optional<Delivery>
  +findBySchoolIdAndScheduledAtBetween(schoolId : UUID, from : Instant, to : Instant) : List<Delivery>
  +save(delivery : Delivery) : Delivery
}

interface FaceRecognitionPort <<interface>> {
  +verify(referenceId : String, image : byte[]) : boolean
}

class AlertView <<value object>> {
  +code : String
  +message : String
}

IDeliveryService <|.. DeliveryService
DeliveryController --> IDeliveryService : delegates
DeliveryController --> FaceRecognitionPort : verifies driver (UC-060, BR-050/051)

DeliveryService --> DeliveryRepository : reads/writes
DeliveryService --> PurchaseRequestRepository : requires APPROVED PR
DeliveryService --> SupplierBatchRepository : links carried batches (BR-071)
DeliveryService ..> AlertView : builds when deviation > threshold

DeliveryRepository ..> Delivery : manages
Delivery "n" --> "0..1" PurchaseRequest : fulfills
Delivery --> DeliveryStatus : status

@enduml
```

**Description:**
`DeliveryService.create()` requires an `APPROVED` purchase request to derive `schoolId`, and links the delivery to the supplier batches carried on it (`delivery_batches`). `updateStatus()` recomputes the elapsed deviation against `scheduledAt`; if it exceeds the configurable `delivery.deviation-alert-minutes` (default 30), an `AlertView` (MSG-065) is attached to the response for the FE to surface. Driver identity verification (UC-060, face recognition at the school gate) is a separate endpoint (`verifyDriverFace`) that calls `FaceRecognitionPort` directly from the controller rather than through `DeliveryService` — it does not mutate `Delivery` state, it only gates physical entry.

---

### 3.2 Sequence Diagram — Register Delivery & Track Status (UC-083/092, BR-071)

```plantuml
@startuml MF06_RegisterDelivery
title Sequence Diagram: Register Delivery & Track Status

skinparam linetype ortho
skinparam backgroundColor #FFFFFF
skinparam sequenceMessageAlign center
skinparam responseMessageBelowArrow true

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
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

actor "Raw Food Supplier" as SUP
boundary ": DeliveryScreen" as UI <<boundary>>
control ": DeliveryController" as C <<control>>
control ": DeliveryService" as S <<control>>
entity "delivery : Delivery" as D <<entity>>
entity ": PurchaseRequestRepository" as PRR <<repository>>
entity ": DeliveryRepository" as DR <<repository>>
database ": PostgreSQL" as DB <<database>>

== Register delivery ==

SUP -> UI : open "Schedule delivery" for an APPROVED PR
activate UI
UI --> SUP : render driver / vehicle / batch pickers
SUP -> UI : submit { purchaseRequestId, scheduledAt,\ndriverId, vehiclePlate, supplierBatchIds[] }
UI -> C : POST /api/v1/deliveries
activate C
C -> S : create(CreateRequest, actor)
activate S

S -> PRR : findByCode(purchaseRequestCode)
activate PRR
PRR -> DB : SELECT * FROM purchase_requests WHERE code = ?
activate DB
DB --> PRR : row
deactivate DB
PRR --> S : PurchaseRequest (schoolId, status)
deactivate PRR

alt PR.status != APPROVED
  S --> C : throw IngExceptions.conflict()
  C --> UI : 409 ING-409 "Purchase request is not APPROVED"
  UI --> SUP : show error message
else PR.status == APPROVED
  S -> S : code = "DLV-" + year + "-####" [if not supplied]
  S -> D : new Delivery(status = SCHEDULED, schoolId)
  activate D
  D --> S : delivery
  deactivate D

  S -> DR : save(delivery)
  activate DR
  DR -> DB : INSERT INTO deliveries
  activate DB
  DB --> DR : 1 row (id)
  deactivate DB
  DR --> S : Delivery (persisted)
  deactivate DR

  S -> DB : INSERT INTO delivery_batches[]\n{ deliveryId, supplierBatchId, ingredientId } (JdbcTemplate)
  activate DB
  DB --> S : n rows
  deactivate DB

  S --> C : DeliveryResponse
  C --> UI : 201 Created + DeliveryResponse
  UI --> SUP : show delivery code "DLV-…"
end
deactivate S
deactivate C
deactivate UI

... En route, driver / dispatch updates status ...

== Track status (UC-092) ==

SUP -> UI : set status "IN_TRANSIT" on delivery {code}
activate UI
UI -> C : PATCH /api/v1/deliveries/{code}/status { status }
activate C
C -> S : updateStatus(code, "IN_TRANSIT", actor)
activate S

S -> DR : findByCodeAndSchoolId(code, schoolId)
activate DR
DR -> DB : SELECT * FROM deliveries WHERE code = ?
activate DB
DB --> DR : row
deactivate DB
DR --> S : Delivery
deactivate DR

alt transition not allowed (see §8.2) or DELIVERED requested
  S --> C : throw IngExceptions.conflict()
  C --> UI : 409 ING-409 "Illegal delivery status transition"
  UI --> SUP : show error message
else transition allowed
  S -> D : changeStatus(IN_TRANSIT, statusUpdatedAt = now())
  activate D
  D --> S : delivery (IN_TRANSIT)
  deactivate D

  S -> DR : save(delivery)
  activate DR
  DR -> DB : UPDATE deliveries SET status, status_updated_at
  activate DB
  DB --> DR : 1 row
  deactivate DB
  DR --> S : Delivery
  deactivate DR

  S -> S : deviation = now() - scheduledAt
  opt deviation > deviationAlertMinutes (default 30)
    S -> S : alert = new AlertView(DLY-DELAY, MSG-065)
  end
  S --> C : DeliveryResponse { ..., alert? }
  C --> UI : 200 OK + DeliveryResponse
  UI --> SUP : show new status (+ delay warning banner)
end
deactivate S
deactivate C
deactivate UI
@enduml
```

**Description:**
Delivery creation cannot happen ahead of an `APPROVED` purchase request — this enforces that every physical shipment traces back to an accepted price and a certified supplier batch. Status transitions are supplier-driven and purely observational except for the deviation check, which is computed server-side on every update so the delay alert cannot be spoofed or suppressed by the client. `DELIVERED` is deliberately not reachable from this endpoint (§8.2).

---

### 3.3 Sequence Diagram — Driver Face Verification at Gate (UC-060 pre-check, BR-050/051)

```plantuml
@startuml MF06_DriverFaceVerify
title Sequence Diagram: Driver Face Verification at Gate

skinparam linetype ortho
skinparam backgroundColor #FFFFFF
skinparam sequenceMessageAlign center
skinparam responseMessageBelowArrow true

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
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

actor "Service Caterers\n(Gate Staff)" as SC
boundary ": GateCheckScreen" as UI <<boundary>>
control ": DeliveryController" as C <<control>>
entity ": DeliveryRepository" as DR <<repository>>
control ": FaceRecognitionPort" as F <<interface>>
database ": PostgreSQL" as DB <<database>>

SC -> UI : open gate check for delivery {code}
activate UI
UI --> SC : open camera, capture driver photo
SC -> UI : confirm captured image
UI -> C : POST /api/v1/deliveries/{code}/verify-driver\n(multipart image)
activate C

C -> DR : findByCodeAndSchoolId(code, schoolId)
activate DR
DR -> DB : SELECT driver_id FROM deliveries WHERE code = ?
activate DB
DB --> DR : row
deactivate DB
DR --> C : Delivery (driverId)
deactivate DR

C -> F : verify(referenceId = driverId, image)
activate F
F --> C : matched : boolean
deactivate F

alt matched == false
  C --> UI : throw FaceRecognitionException\n-> 401/422 ING-014 (MSG-014)
  UI --> SC : show "Driver not recognised" — deny entry
else matched == true
  C --> UI : 200 OK { verified: true, deliveryCode, message }
  UI --> SC : show "Driver verified" — allow entry,\ncarry driverVerificationId into receiving (§4.2)
end
deactivate C
deactivate UI

@enduml
```

**Description:**
This check is intentionally decoupled from `DeliveryService` — it is a physical-access gate (does the person at the door match the registered driver on record) rather than a business-state transition, so it never writes to `deliveries`. A failed match raises `FaceRecognitionException`, surfaced as ING-014, and the gate staff denies entry; `driverVerificationId` is later stored on the `ReceivingSession` (§4) as evidence that the check happened, not re-run inside receiving itself.

---

## 4. Receiving

### 4.1 Class Diagram

```plantuml
@startuml MF06_Receiving_ClassDiagram
title Design-level Class Diagram: Receiving

skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF
hide empty members

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

enum ReceivingStatus {
  IN_PROGRESS
  RECEIVED
  RECEIVED_PARTIAL
  REJECTED
}

class ReceivingSession <<entity>> {
  +id : UUID <<PK>>
  +code : String <<business id>>
  +schoolId : UUID
  +deliveryId : UUID <<FK>>
  +supplierId : UUID <<FK>>
  +supplierStatusAtReceipt : String
  +receivedAt : Instant
  +receivedByUserId : UUID
  +driverVerificationId : String
  +lat : BigDecimal
  +lng : BigDecimal
  +gpsVerified : boolean
  +photosCount : int
  +deviceInfo : String
  +status : ReceivingStatus
  +lines : List<ReceivingLine>
}

class ReceivingLine <<entity>> {
  +id : UUID <<PK>>
  +sessionId : UUID <<FK>>
  +supplierBatchId : UUID <<FK>>
  +ingredientId : UUID <<FK>>
  +ingredientLabel : String
  +storageZone : StorageZone
  +rejected : boolean
  +rejectReason : String
  +rejectPhotoKeys : List<String>
  +batchCode : String <<transient, response-only>>
}

class InventoryBatch <<entity>> {
  +id : UUID <<PK>>
  +code : String
  +initialQuantity : BigDecimal
  +expiryDate : LocalDate
  +receivingSessionId : UUID <<FK>>
}

class InventoryTransaction <<entity, append-only>> {
  +type : TransactionType
  +batchId : UUID <<FK>>
  +delta : BigDecimal
}

interface IReceivingSessionService <<interface>> {
  +create(cmd : ReceivingSessionCreateRequest, actor : AuthUser) : ReceivingSessionResponse
  +uploadEvidence(file : MultipartFile, actor : AuthUser) : String
  +dueDeliveries(actor : AuthUser) : List<String>
  +list(query : ListQuery, actor : AuthUser) : PagedResponse<ReceivingSessionResponse>
  +detail(code : String, actor : AuthUser) : ReceivingSessionResponse
}

class ReceivingSessionService <<control>> {
  -sessionRepository : ReceivingSessionRepository
  -deliveryRepository : DeliveryRepository
  -supplierBatchRepository : SupplierBatchRepository
  -inventoryBatchRepository : InventoryBatchRepository
  -transactionRepository : InventoryTransactionRepository
  -catalogRepository : CatalogIngredientRepository
  -referenceResolver : ReferenceResolver
  -geofenceValidator : GeofenceValidator
  -storageService : StorageService
  -codeGenerator : CodeGenerator
  -clock : Clock
  +create(cmd, actor) : ReceivingSessionResponse
  +uploadEvidence(file, actor) : String
  -upsertInventoryBatch(line, session) : InventoryBatch
  -computeOverallStatus(lines) : ReceivingStatus
}

class ReferenceResolver <<control>> {
  +partnerStatus(partnerId : UUID) : String
  +schoolGeofence(schoolId : UUID) : GeofenceConfig
}

class GeofenceValidator <<control>> {
  +verify(schoolId : UUID, lat : BigDecimal, lng : BigDecimal) : boolean
  +isEnforced() : boolean
}

interface ReceivingSessionRepository <<repository>> {
  +findByCodeAndSchoolId(code : String, schoolId : UUID) : Optional<ReceivingSession>
  +save(session : ReceivingSession) : ReceivingSession
}

interface InventoryBatchRepository <<repository>> {
  +findBySchoolIdAndCode(schoolId : UUID, code : String) : Optional<InventoryBatch>
  +save(batch : InventoryBatch) : InventoryBatch
}

interface InventoryTransactionRepository <<repository>> {
  +save(tx : InventoryTransaction) : InventoryTransaction
}

interface DeliveryRepository <<repository>> {
  +findByIdAndSchoolId(id : UUID, schoolId : UUID) : Optional<Delivery>
  +save(delivery : Delivery) : Delivery
}

IReceivingSessionService <|.. ReceivingSessionService
ReceivingSessionService --> ReferenceResolver : checks supplier APPROVED (BR-070)
ReceivingSessionService --> GeofenceValidator : checks location (BR-074)
ReceivingSessionService --> ReceivingSessionRepository : writes session (@Transactional)
ReceivingSessionService --> InventoryBatchRepository : upserts batch (BR-076)
ReceivingSessionService --> InventoryTransactionRepository : inserts RECEIPT
ReceivingSessionService --> DeliveryRepository : marks DELIVERED

ReceivingSessionRepository ..> ReceivingSession : manages
InventoryBatchRepository ..> InventoryBatch : manages
InventoryTransactionRepository ..> InventoryTransaction : manages

ReceivingSession "1" *-- "n" ReceivingLine : cascade ALL
ReceivingSession "1" --> "n" InventoryBatch : traceability root (BR-076)
InventoryBatch "1" --> "n" InventoryTransaction : ledger
ReceivingSession --> ReceivingStatus : status

@enduml
```

**Description:**
`ReceivingSessionService.create()` is the single atomic transaction of the whole module (ADR-M06-5): it validates BR-070 (supplier must be `APPROVED` at the moment of receipt), BR-074 (GPS inside the school's geofence), BR-072/078 (≥1 evidence photo overall, and reason+photo for every rejected line) before writing anything, then in one `@Transactional` block persists the session and its lines, **upserts** `InventoryBatch` per accepted line (creating it on first receipt of a given batch code, or appending an additional `RECEIPT` transaction on a re-delivery of the same code — preventing duplicate batch rows), and finally marks the source `Delivery` as `DELIVERED`. `receivedAt`, `deviceInfo` and the geofence check are all server-derived — never trusted from the client (BR-073, ADR-M06-7).

---

### 4.2 Sequence Diagram — Record Receiving, Happy Path (UC-060, ADR-M06-5)

```plantuml
@startuml MF06_RecordReceiving
title Sequence Diagram: Record Receiving (Happy Path)

skinparam linetype ortho
skinparam backgroundColor #FFFFFF
skinparam sequenceMessageAlign center
skinparam responseMessageBelowArrow true

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
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

actor "Service Caterers" as SC
boundary ": ReceivingScreen" as UI <<boundary>>
control ": ReceivingSessionController" as C <<control>>
control ": ReceivingSessionService" as S <<control>>
control ": ReferenceResolver" as RR <<control>>
control ": GeofenceValidator" as GV <<control>>
entity "session : ReceivingSession" as RS <<entity>>
entity "batch : InventoryBatch" as IB <<entity>>
entity ": DeliveryRepository" as DR <<repository>>
entity ": ReceivingSessionRepository" as RSR <<repository>>
entity ": InventoryBatchRepository" as IBR <<repository>>
entity ": InventoryTransactionRepository" as ITR <<repository>>
database ": PostgreSQL" as DB <<database>>

SC -> UI : open "Receive delivery" for {deliveryCode}
activate UI
UI --> SC : list expected lines, request GPS + photos
SC -> UI : submit { deliveryId, driverVerificationId, lat, lng,\nphotosCount, deviceInfo, lines[] }
UI -> C : POST /api/v1/receiving-sessions
activate C
C -> S : create(ReceivingSessionCreateRequest, actor)
activate S

S -> S : check photosCount >= 1 — BR-072
alt photosCount == 0
  S --> C : throw IngExceptions.evidenceRequired()
  C --> UI : 400 ING-072 (MSG-146) "Evidence photos required"
  UI --> SC : ask staff to attach photos
else photosCount >= 1

  S -> DR : findByIdAndSchoolId(deliveryId, schoolId)
  activate DR
  DR -> DB : SELECT * FROM deliveries WHERE id = ?
  activate DB
  DB --> DR : row
  deactivate DB
  DR --> S : Delivery
  deactivate DR

  S -> S : supplierId = first line's SupplierBatch.partnerId
  S -> RR : partnerStatus(supplierId) — BR-070
  activate RR
  RR --> S : status : String
  deactivate RR

  alt status != APPROVED
    S --> C : throw IngExceptions.supplierNotApproved()
    C --> UI : 409 ING-070 (MSG-145) "Supplier not Approved"
    UI --> SC : block receipt, show supplier status
  else status == APPROVED

    S -> GV : verify(schoolId, lat, lng) — BR-074
    activate GV
    GV --> S : withinGeofence : boolean
    deactivate GV

    alt isEnforced() && !withinGeofence
      S --> C : throw IngExceptions.geofenceFailed()
      C --> UI : 422 ING-015 (MSG-015) "Geofence check failed"
      UI --> SC : show "You are outside the school area"
    else geofence OK

      group @Transactional — one atomic write (ADR-M06-5)
        S -> S : computeOverallStatus(lines) = RECEIVED
        S -> RS : new ReceivingSession(receivedAt = clock.instant(),\ncode = "RCV-{yyyymmdd}-##", gpsVerified = true) — BR-073
        activate RS
        RS --> S : session
        deactivate RS

        S -> RSR : save(session + lines cascade)
        activate RSR
        RSR -> DB : INSERT INTO receiving_sessions\nINSERT INTO receiving_lines[]
        activate DB
        DB --> RSR : 1 + n rows
        deactivate DB
        RSR --> S : ReceivingSession (persisted)
        deactivate RSR

        loop for each accepted line
          S -> IBR : findBySchoolIdAndCode(schoolId, supplierBatchCode)
          activate IBR
          IBR -> DB : SELECT * FROM inventory_batches\nWHERE school_id = ? AND code = ?
          activate DB
          DB --> IBR : row | empty
          deactivate DB
          IBR --> S : Optional<InventoryBatch>
          deactivate IBR

          alt batch not found (first receipt)
            S -> IB : new InventoryBatch(initialQuantity = supplierBatch.quantity,\nreceivingSessionId = session.id) — BR-076
            activate IB
            IB --> S : batch
            deactivate IB
            S -> IBR : save(batch)
            activate IBR
            IBR -> DB : INSERT INTO inventory_batches
            activate DB
            DB --> IBR : 1 row (id)
            deactivate DB
            IBR --> S : InventoryBatch (persisted)
            deactivate IBR
          else batch code already exists (re-delivery)
            S -> S : reuse existing batch — skip INSERT\n(avoid double-count of initialQuantity)
          end

          S -> ITR : save(RECEIPT tx, delta = +quantity, batchId)
          activate ITR
          ITR -> DB : INSERT INTO inventory_transactions\n{ type = RECEIPT, delta > 0 }
          activate DB
          DB --> ITR : 1 row
          deactivate DB
          ITR --> S : InventoryTransaction
          deactivate ITR
        end

        S -> DR : save(delivery.status = DELIVERED)
        activate DR
        DR -> DB : UPDATE deliveries SET status = 'DELIVERED'
        activate DB
        DB --> DR : 1 row
        deactivate DB
        DR --> S : Delivery (DELIVERED)
        deactivate DR
      end

      S --> C : ReceivingSessionResponse
      C --> UI : 201 Created + ReceivingSessionResponse (MSG-041)
      UI --> SC : show receipt summary + RCV code
    end
  end
end
deactivate S
deactivate C
deactivate UI
@enduml
```

**Description:**
Every guard runs before any write — BR-072, BR-070, BR-074 in that order — so a rejected request leaves zero rows behind. The batch-upsert logic is the key traceability link (BR-076): `InventoryBatch.receivingSessionId` lets `TraceabilityService` (§7) later walk backward from a served dish all the way to this exact receiving event and its GPS/photo evidence. The whole write block is one `@Transactional` unit, so the session, its lines, the batches, the RECEIPT ledger rows and the delivery status either all land or none do.

---

### 4.3 Sequence Diagram — Reject a Batch During Receiving (UC-072, BR-078)

```plantuml
@startuml MF06_RejectDuringReceiving
title Sequence Diagram: Reject a Batch During Receiving

skinparam linetype ortho
skinparam backgroundColor #FFFFFF
skinparam sequenceMessageAlign center
skinparam responseMessageBelowArrow true

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
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

actor "Service Caterers" as SC
boundary ": ReceivingScreen" as UI <<boundary>>
control ": ReceivingSessionController" as C <<control>>
control ": ReceivingSessionService" as S <<control>>
control ": StorageService" as ST <<interface>>
entity ": ReceivingSessionRepository" as RSR <<repository>>
entity ": InventoryBatchRepository" as IBR <<repository>>
entity ": InventoryTransactionRepository" as ITR <<repository>>
database ": PostgreSQL" as DB <<database>>

== Step 1 — upload rejection evidence ==

SC -> UI : take photo of the damaged goods
activate UI
UI -> C : POST /api/v1/receiving-sessions/reject-documents\n(multipart photo)
activate C
C -> S : uploadEvidence(file, actor)
activate S
S -> S : fileValidation.validate(file)
S -> ST : store(file)
activate ST
ST --> S : fileKey
deactivate ST
S -> DB : INSERT INTO receiving_evidence_documents\n{ code = "RCVDOC-{epochMilli}", fileKey }
activate DB
DB --> S : documentId
deactivate DB
S --> C : documentId
deactivate S
C --> UI : 200 OK { documentId }
deactivate C
UI --> SC : show attached photo thumbnail
deactivate UI

== Step 2 — submit the session with rejected lines ==

SC -> UI : mark line as rejected + enter reason, submit
activate UI
UI -> C : POST /api/v1/receiving-sessions\n{ lines: [{ ..., rejected: true, rejectReason,\nrejectDocumentIds: [documentId] }] }
activate C
C -> S : create(ReceivingSessionCreateRequest, actor)
activate S

loop for each rejected line
  S -> S : check rejectReason != null\n&& rejectDocumentIds.size() >= 1 — BR-078
end

alt Missing reason or photo
  S --> C : throw IngExceptions.rejectEvidenceRequired()
  C --> UI : 400 ING-078 (MSG-148) "Rejected line missing reason/photo"
  UI --> SC : highlight incomplete rejected line
else All rejected lines complete
  S -> S : computeOverallStatus(lines)\n= REJECTED (all) | RECEIVED_PARTIAL (some) | RECEIVED (none)

  group @Transactional — same atomic write as §4.2
    S -> RSR : save(session + lines, status = computed)
    activate RSR
    RSR -> DB : INSERT INTO receiving_sessions / receiving_lines
    activate DB
    DB --> RSR : 1 + n rows
    deactivate DB
    RSR --> S : ReceivingSession
    deactivate RSR

    loop for each ACCEPTED line only
      S -> IBR : upsert InventoryBatch(schoolId, batchCode)
      activate IBR
      IBR -> DB : SELECT / INSERT inventory_batches
      activate DB
      DB --> IBR : row
      deactivate DB
      IBR --> S : InventoryBatch
      deactivate IBR

      S -> ITR : save(RECEIPT tx, delta = +quantity)
      activate ITR
      ITR -> DB : INSERT INTO inventory_transactions
      activate DB
      DB --> ITR : 1 row
      deactivate DB
      ITR --> S : InventoryTransaction
      deactivate ITR
    end
  end

  S --> C : ReceivingSessionResponse
  C --> UI : 201 Created + ReceivingSessionResponse
  UI --> SC : show partial-receipt summary\n(accepted vs rejected lines)
end
deactivate S
deactivate C
deactivate UI
@enduml
```

**Description:**
Evidence photos are uploaded first as standalone documents (returning a `documentId`), then referenced by id in the rejected line — never as an inline file, and never as a public URL (D10-BIS.2/3). A session can mix accepted and rejected lines in the same delivery, yielding `RECEIVED_PARTIAL` (UC-072); only accepted lines ever create an `InventoryBatch`, so rejected goods never enter the warehouse's available stock. The resulting session statuses are summarised in §8.4.

---

## 5. Inventory & FEFO Issue

### 5.1 Class Diagram

```plantuml
@startuml MF06_Inventory_ClassDiagram
title Design-level Class Diagram: Inventory & FEFO Issue

skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF
hide empty members

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

enum ShelfStatus {
  IN_STOCK
  EXPIRING_SOON
  EXPIRED
}

enum TransactionType {
  RECEIPT
  ISSUE
  ADJUSTMENT
}

enum ReasonCode {
  SPOILAGE
  COUNT_CORRECTION
  DAMAGE
  RETURN_TO_SUPPLIER
}

enum IssueSlipStatus {
  DRAFT
  CONFIRMED
  POSTED
}

class QuarantineInfo <<embeddable>> {
  +at : Instant
  +byUserId : UUID
  +reason : String
  +incidentId : UUID
}

class InventoryBatch <<entity>> {
  +id : UUID <<PK>>
  +code : String
  +schoolId : UUID
  +ingredientId : UUID <<FK>>
  +initialQuantity : BigDecimal
  +storageZone : StorageZone
  +expiryDate : LocalDate
  +quarantine : QuarantineInfo
  +shelfStatus(today : LocalDate) : ShelfStatus
  +isLocked(today : LocalDate) : boolean
}

class InventoryTransaction <<entity, append-only>> {
  +id : UUID <<PK>>
  +type : TransactionType
  +batchId : UUID <<FK>>
  +delta : BigDecimal
  +reasonCode : ReasonCode
  +managerSignature : String
  +issueSlipId : UUID <<FK>>
  +dishId : UUID
  +serviceDate : LocalDate
  +mealType : MealType
}

class IssueSlip <<entity>> {
  +id : UUID <<PK>>
  +code : String
  +serviceDate : LocalDate
  +mealType : MealType
  +menuIds : List<UUID>
  +issuedByUserId : UUID
  +status : IssueSlipStatus
}

class FefoAllocator <<static utility>> {
  +{static} allocate(candidates : List<InventoryBatch>,\n  available : Function<InventoryBatch,BigDecimal>,\n  needed : BigDecimal, today : LocalDate) : Result
}

interface IInventoryService <<interface>> {
  +issuePlan(serviceDate, mealType, demand : Map<String,BigDecimal>, actor) : IssuePlanResponse
  +createIssueSlip(cmd : IssueSlipCreateRequest, actor) : IssueSlipResponse
  +updateStorageZone(code, storageZone, actor) : InventoryBatchResponse
  +quarantine(code, cmd : QuarantineRequest, actor) : InventoryBatchResponse
  +unquarantine(code, actor) : InventoryBatchResponse
  +list(query, actor) : PagedResponse<InventoryBatchResponse>
}

class InventoryService <<control>> {
  -batchRepository : InventoryBatchRepository
  -transactionRepository : InventoryTransactionRepository
  -issueSlipRepository : IssueSlipRepository
  -catalogRepository : CatalogIngredientRepository
  -referenceResolver : ReferenceResolver
  -codeGenerator : CodeGenerator
  -clock : Clock
  +issuePlan(serviceDate, mealType, demand, actor) : IssuePlanResponse
  +createIssueSlip(cmd, actor) : IssueSlipResponse
  +quarantine(code, cmd, actor) : InventoryBatchResponse
  +unquarantine(code, actor) : InventoryBatchResponse
  -remainingOf(batch) : BigDecimal
}

interface IInventoryTransactionService <<interface>> {
  +adjust(cmd : ManualAdjustmentRequest, actor) : InventoryTransactionResponse
  +list(query, actor) : PagedResponse<InventoryTransactionResponse>
}

class InventoryTransactionService <<control>> {
  -transactionRepository : InventoryTransactionRepository
  -batchRepository : InventoryBatchRepository
  -referenceResolver : ReferenceResolver
  -codeGenerator : CodeGenerator
  -clock : Clock
  +adjust(cmd, actor) : InventoryTransactionResponse
}

class InventoryController <<boundary>> {
  -service : IInventoryService
  +issuePlan(query, actor) : IssuePlanResponse
  +createIssueSlip(body, actor) : IssueSlipResponse
  +quarantine(code, body, actor) : InventoryBatchResponse
  +unquarantine(code, actor) : InventoryBatchResponse
}

class InventoryTransactionController <<boundary>> {
  -service : IInventoryTransactionService
  +adjust(body, actor) : InventoryTransactionResponse
  +list(query, actor) : PagedResponse<InventoryTransactionResponse>
}

interface InventoryBatchRepository <<repository>> {
  +findCandidates(schoolId : UUID, ingredientId : UUID) : List<InventoryBatch>
  +findBySchoolIdAndCode(schoolId : UUID, code : String) : Optional<InventoryBatch>
  +save(batch : InventoryBatch) : InventoryBatch
}

interface InventoryTransactionRepository <<repository>> {
  +sumDeltaByBatchId(batchId : UUID) : BigDecimal
  +save(tx : InventoryTransaction) : InventoryTransaction
}

interface IssueSlipRepository <<repository>> {
  +existsBySchoolIdAndServiceDateAndMealType(schoolId : UUID, date : LocalDate, meal : MealType) : boolean
  +save(slip : IssueSlip) : IssueSlip
}

IInventoryService <|.. InventoryService
IInventoryTransactionService <|.. InventoryTransactionService
InventoryController --> IInventoryService : delegates
InventoryTransactionController --> IInventoryTransactionService : delegates

InventoryService --> FefoAllocator : delegates allocation
InventoryService --> InventoryBatchRepository : reads candidates
InventoryService --> IssueSlipRepository : creates slip
InventoryService --> InventoryTransactionRepository : inserts ISSUE
InventoryTransactionService --> InventoryTransactionRepository : inserts ADJUSTMENT
InventoryTransactionService --> InventoryBatchRepository : re-checks isLocked()

InventoryBatchRepository ..> InventoryBatch : manages
InventoryTransactionRepository ..> InventoryTransaction : manages
IssueSlipRepository ..> IssueSlip : manages

InventoryBatch "1" *-- "0..1" QuarantineInfo
InventoryBatch "1" --> "n" InventoryTransaction : ledger
IssueSlip "1" --> "n" InventoryTransaction : issue_slip_id
IssueSlip --> IssueSlipStatus : status
InventoryTransaction --> TransactionType : type
InventoryTransaction --> ReasonCode : reasonCode

@enduml
```

**Description:**
Available quantity is never a stored column (ADR-M06-1): `InventoryService` derives it as `initialQuantity + SUM(inventory_transactions.delta)` per batch. `FefoAllocator` is a pure, dependency-free static function (ADR-M06-4) — it excludes any `isLocked()` batch (expired, per `ShelfStatus`, or quarantined via the independent `QuarantineInfo` value object, ADR-M06-2) from the candidate pool entirely, then greedily consumes the earliest-expiry batches first. `createIssueSlip()` writes a real `IssueSlip` row (ADR-M06-6) and one negative-`delta` `ISSUE` transaction per line, each mandatorily carrying `dishId`/`menuId`/`serviceDate`/`mealType` (BR-076) — the DB `ck_tx_issue_trace` check makes this non-optional. `InventoryTransactionService.adjust()` is the only way to write a non-FEFO, non-receiving delta, and BR-204 requires both a `reasonCode` and a `managerSignature`.

---

### 5.2 Sequence Diagram — FEFO Issue Plan & Create Issue Slip (UC-045, ADR-M06-4/6, BR-075/076/077)

```plantuml
@startuml MF06_FefoIssue
title Sequence Diagram: FEFO Issue Plan & Create Issue Slip

skinparam linetype ortho
skinparam backgroundColor #FFFFFF
skinparam sequenceMessageAlign center
skinparam responseMessageBelowArrow true

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
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

actor "Service Caterers\n(Kitchen)" as SC
boundary ": IssueSlipScreen" as UI <<boundary>>
control ": InventoryController" as C <<control>>
control ": InventoryService" as S <<control>>
control ": FefoAllocator" as F <<utility>>
entity "slip : IssueSlip" as SL <<entity>>
entity ": InventoryBatchRepository" as IBR <<repository>>
entity ": InventoryTransactionRepository" as ITR <<repository>>
entity ": IssueSlipRepository" as ISR <<repository>>
database ": PostgreSQL" as DB <<database>>

== Preview the FEFO plan (read-only) ==

SC -> UI : choose serviceDate + mealType, request issue plan
activate UI
UI -> C : GET /api/v1/inventory/issue-plan\n?serviceDate&mealType&demand[ING-GAO]=12.5
activate C
C -> S : issuePlan(serviceDate, mealType, demand, actor)
activate S

loop for each ingredientCode in demand
  S -> IBR : findCandidates(schoolId, ingredientId)\nORDER BY expiry_date ASC — BR-075
  activate IBR
  IBR -> DB : SELECT * FROM inventory_batches\nWHERE school_id = ? AND ingredient_id = ?
  activate DB
  DB --> IBR : rows
  deactivate DB
  IBR --> S : List<InventoryBatch>
  deactivate IBR

  S -> ITR : sumDeltaByBatchId(batchId) (per candidate)
  activate ITR
  ITR -> DB : SELECT SUM(delta) FROM inventory_transactions\nWHERE batch_id = ?
  activate DB
  DB --> ITR : sum
  deactivate DB
  ITR --> S : BigDecimal — available = initialQuantity + SUM(delta)
  deactivate ITR

  S -> F : allocate(candidates, available, needed, today)
  activate F
  F -> F : exclude isLocked(today) batches\n(EXPIRED or quarantined) — ADR-M06-4
  F -> F : greedily take from earliest-expiry first
  F --> S : Result(allocations[], shortfall)
  deactivate F

  opt shortfall > 0
    S -> S : shortages[].add(ingredientCode, shortfall) — BR-077
  end
end

S --> C : IssuePlanResponse { lines[], shortages[], unknownIngredients[] }
deactivate S
C --> UI : 200 OK + IssuePlanResponse
deactivate C
UI --> SC : show proposed allocation + shortage warnings
deactivate UI

... Kitchen confirms the plan ...

== Post the issue slip (writes the ledger) ==

SC -> UI : confirm plan and submit
activate UI
UI -> C : POST /api/v1/inventory/issue-slips\n{ serviceDate, mealType, menuIds,\nlines: [{ batchId, dishId, quantity, unit }] }
activate C
C -> S : createIssueSlip(IssueSlipCreateRequest, actor)
activate S

S -> ISR : existsBySchoolIdAndServiceDateAndMealType(...)
activate ISR
ISR -> DB : SELECT 1 FROM issue_slips WHERE ...
activate DB
DB --> ISR : exists : boolean
deactivate DB
ISR --> S : boolean
deactivate ISR

alt slip already exists
  S --> C : throw IngExceptions.conflict()
  C --> UI : 409 ING-409 "Issue slip already exists for this service"
  UI --> SC : show error message
else new slip
  group @Transactional
    S -> SL : new IssueSlip(status = POSTED,\nissuedByUserId = actor.userId) — ADR-M06-6
    activate SL
    SL --> S : slip
    deactivate SL
    S -> ISR : save(slip)
    activate ISR
    ISR -> DB : INSERT INTO issue_slips
    activate DB
    DB --> ISR : 1 row (id)
    deactivate DB
    ISR --> S : IssueSlip (persisted)
    deactivate ISR

    loop for each line
      S -> IBR : findBySchoolIdAndCode(schoolId, batchCode)
      activate IBR
      IBR -> DB : SELECT * FROM inventory_batches WHERE code = ?
      activate DB
      DB --> IBR : row
      deactivate DB
      IBR --> S : InventoryBatch
      deactivate IBR

      alt batch.isLocked(today)
        S --> C : throw IngExceptions.batchLocked()
        C --> UI : 409/422 ING-149 | ING-205\n"Batch quarantined/expired" — BR-149/BR-205
        UI --> SC : mark the offending line
      else batch usable
        alt dishId unresolved
          S --> C : throw IngExceptions.traceUnresolved()
          C --> UI : 422 ING-076 (MSG-147) "Ingredient/dish unresolved" — BR-076
          UI --> SC : mark the offending line
        else dishId resolved
          S -> ITR : save(ISSUE tx, delta = -quantity, issueSlipId,\ndishId, menuId, mealType, serviceDate)
          activate ITR
          ITR -> DB : INSERT INTO inventory_transactions\n(ck_tx_issue_trace enforced)
          activate DB
          DB --> ITR : 1 row
          deactivate DB
          ITR --> S : InventoryTransaction
          deactivate ITR
        end
      end
    end
  end
  S --> C : IssueSlipResponse
  C --> UI : 201 Created + IssueSlipResponse (MSG-042)
  UI --> SC : show posted slip + consumed batches
end
deactivate S
deactivate C
deactivate UI
@enduml
```

**Description:**
`issuePlan()` is a read-only preview — it runs `FefoAllocator` per requested ingredient and returns proposed allocations plus any `shortfall` (BR-077 auto shortage detection), without writing anything. Only `createIssueSlip()` persists: it re-validates every batch is still unlocked at commit time (guarding against a race between preview and confirmation) and writes one immutable `ISSUE` transaction per line, permanently linking the consumption to a dish, menu, service date and meal type — the backbone of farm-to-table traceability (§7). The slip's own lifecycle is in §8.5.

---

### 5.3 Sequence Diagram — Manual Stock Adjustment (UC-064, BR-204)

```plantuml
@startuml MF06_ManualAdjustment
title Sequence Diagram: Manual Stock Adjustment

skinparam linetype ortho
skinparam backgroundColor #FFFFFF
skinparam sequenceMessageAlign center
skinparam responseMessageBelowArrow true

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
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

actor "Service Caterers" as SC
boundary ": StockAdjustmentScreen" as UI <<boundary>>
control ": InventoryTransactionController" as C <<control>>
control ": InventoryTransactionService" as S <<control>>
entity "tx : InventoryTransaction" as TX <<entity>>
entity ": InventoryBatchRepository" as IBR <<repository>>
entity ": InventoryTransactionRepository" as ITR <<repository>>
database ": PostgreSQL" as DB <<database>>

SC -> UI : open "Adjust stock" for a batch
activate UI
UI --> SC : render delta / reasonCode / signature fields
SC -> UI : submit { batchId, delta, unit, reasonCode, managerSignature }
UI -> C : POST /api/v1/inventory-transactions/adjustments
activate C
C -> C : Bean Validation @NotNull reasonCode / @NotBlank signature
C -> S : adjust(ManualAdjustmentRequest, actor)
activate S

S -> S : validate reasonCode != null\n&& managerSignature not blank — BR-204

S -> IBR : findBySchoolIdAndCode(schoolId, batchCode)
activate IBR
IBR -> DB : SELECT * FROM inventory_batches WHERE code = ?
activate DB
DB --> IBR : row
deactivate DB
IBR --> S : InventoryBatch
deactivate IBR

alt batch.isLocked(today)
  S --> C : throw IngExceptions.batchLocked()
  C --> UI : 409 ING-149 | ING-205 "Batch quarantined/expired"
  UI --> SC : show error message
else batch usable
  S -> TX : new InventoryTransaction(type = ADJUSTMENT, delta,\nreasonCode, managerSignature, performedByUserId = actor.userId)
  activate TX
  TX --> S : tx
  deactivate TX

  S -> ITR : save(tx)
  activate ITR
  ITR -> DB : INSERT INTO inventory_transactions (append-only)
  activate DB
  DB --> ITR : 1 row
  deactivate DB
  ITR --> S : InventoryTransaction (persisted)
  deactivate ITR

  S --> C : InventoryTransactionResponse
  C --> UI : 201 Created + InventoryTransactionResponse (MSG-042)
  UI --> SC : show new available quantity
end
deactivate S
deactivate C
deactivate UI
@enduml
```

**Description:**
Manual adjustments are the only transaction type not tied to a receiving or issuing event, so BR-204 layers three independent enforcement points (request validation, service check, DB `CHECK` constraint) requiring a `reasonCode` drawn from a fixed list and a `managerSignature` — accountability for every off-cycle stock change (spoilage, count correction, damage, return to supplier).

---

### 5.4 Sequence Diagram — Quarantine / Un-quarantine a Batch (BR-149)

```plantuml
@startuml MF06_Quarantine
title Sequence Diagram: Quarantine / Un-quarantine a Batch

skinparam linetype ortho
skinparam backgroundColor #FFFFFF
skinparam sequenceMessageAlign center
skinparam responseMessageBelowArrow true

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
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

actor "Medical Staff" as MS
boundary ": InventoryBatchScreen" as UI <<boundary>>
control ": InventoryController" as C <<control>>
control ": InventoryService" as S <<control>>
entity "batch : InventoryBatch" as IB <<entity>>
entity ": InventoryBatchRepository" as IBR <<repository>>
database ": PostgreSQL" as DB <<database>>

== Quarantine (BR-149) ==

MS -> UI : open batch {code}, click "Quarantine"
activate UI
UI --> MS : ask for reason + incidentId
MS -> UI : submit { reason, incidentId }
UI -> C : PATCH /api/v1/inventory/{code}/quarantine
activate C
C -> C : @PreAuthorize("hasRole('MEDICAL_STAFF')")
C -> S : quarantine(code, QuarantineRequest, actor)
activate S

S -> IBR : findBySchoolIdAndCode(schoolId, code)
activate IBR
IBR -> DB : SELECT * FROM inventory_batches WHERE code = ?
activate DB
DB --> IBR : row
deactivate DB
IBR --> S : InventoryBatch
deactivate IBR

S -> IB : quarantine(at = now(), byUserId = actor.userId,\nreason, incidentId)
activate IB
IB --> S : batch (isLocked = true)
deactivate IB

S -> IBR : save(batch)
activate IBR
IBR -> DB : UPDATE inventory_batches\nSET quarantine_at, quarantine_by_user_id,\nquarantine_reason, quarantine_incident_id
activate DB
DB --> IBR : 1 row
deactivate DB
IBR --> S : InventoryBatch (persisted)
deactivate IBR

S --> C : InventoryBatchResponse { isLocked: true }
deactivate S
C --> UI : 200 OK + InventoryBatchResponse
deactivate C
UI --> MS : show batch marked "QUARANTINED"
deactivate UI

... Later, the food-safety issue is resolved ...

== Un-quarantine ==

MS -> UI : click "Release quarantine" on batch {code}
activate UI
UI -> C : DELETE /api/v1/inventory/{code}/quarantine
activate C
C -> S : unquarantine(code, actor)
activate S

S -> IBR : findBySchoolIdAndCode(schoolId, code)
activate IBR
IBR -> DB : SELECT * FROM inventory_batches WHERE code = ?
activate DB
DB --> IBR : row
deactivate DB
IBR --> S : InventoryBatch
deactivate IBR

S -> IB : releaseQuarantine()
activate IB
IB --> S : batch (quarantine = null)
deactivate IB

S -> IBR : save(batch)
activate IBR
IBR -> DB : UPDATE inventory_batches\nSET quarantine_at = NULL, quarantine_by_user_id = NULL,\nquarantine_reason = NULL, quarantine_incident_id = NULL
activate DB
DB --> IBR : 1 row
deactivate DB
IBR --> S : InventoryBatch (persisted)
deactivate IBR

S --> C : InventoryBatchResponse { isLocked: false }
deactivate S
C --> UI : 200 OK + InventoryBatchResponse
deactivate C
UI --> MS : show batch back in stock
deactivate UI
@enduml
```

**Description:**
Only `MEDICAL_STAFF` may quarantine or lift quarantine (enforced by `@PreAuthorize` at the controller) — kitchen staff cannot self-clear a food-safety hold. Un-quarantine is a dedicated `DELETE` endpoint rather than a `PUT` with null fields (ADR-M06-2), keeping the intent explicit in the API surface and audit trail. Both transitions are the quarantine axis of §8.6.

---

## 6. Preparation Record

### 6.1 Class Diagram

```plantuml
@startuml MF06_PreparationRecord_ClassDiagram
title Design-level Class Diagram: Preparation Record

skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF
hide empty members

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

class PreparationRecord <<entity>> {
  +id : UUID <<PK>>
  +code : String <<business id>>
  +schoolId : UUID
  +date : LocalDate
  +dishId : UUID
  +dishLabel : String
  +menuId : UUID
  +mealType : MealType
  +startTime : LocalTime
  +endTime : LocalTime
  +cookUserId : UUID
  +notes : String
}

class PreparationBatch <<join>> {
  +preparationRecordId : UUID <<FK>>
  +batchId : UUID <<FK>>
  +extra : boolean
}

interface IPreparationRecordService <<interface>> {
  +create(cmd : CreateRequest, actor : AuthUser) : PreparationRecordResponse
  +list(query : ListQuery, actor : AuthUser) : PagedResponse<PreparationRecordResponse>
  +detail(code : String, actor : AuthUser) : PreparationRecordResponse
}

class PreparationRecordService <<control>> {
  -recordRepository : PreparationRecordRepository
  -batchRepository : InventoryBatchRepository
  -referenceResolver : ReferenceResolver
  -codeGenerator : CodeGenerator
  +create(cmd, actor) : PreparationRecordResponse
  +detail(code, actor) : PreparationRecordResponse
  -toResponse(record) : PreparationRecordResponse
}

class PreparationRecordController <<boundary>> {
  -service : IPreparationRecordService
  +list(query, actor) : PagedResponse<PreparationRecordResponse>
  +detail(code, actor) : PreparationRecordResponse
  +create(body, actor) : PreparationRecordResponse
}

interface PreparationRecordRepository <<repository>> {
  +findByCodeAndSchoolId(code : String, schoolId : UUID) : Optional<PreparationRecord>
  +save(record : PreparationRecord) : PreparationRecord
}

interface InventoryBatchRepository <<repository>> {
  +findBySchoolIdAndCode(schoolId : UUID, code : String) : Optional<InventoryBatch>
}

IPreparationRecordService <|.. PreparationRecordService
PreparationRecordController --> IPreparationRecordService : delegates

PreparationRecordService --> PreparationRecordRepository : reads/writes
PreparationRecordService --> InventoryBatchRepository : resolves used batches
PreparationRecordService ..> PreparationBatch : inserts (batchesUsed + extraBatches)

PreparationRecordRepository ..> PreparationRecord : manages

PreparationRecord "1" *-- "n" PreparationBatch : consumed
PreparationBatch "n" --> "1" InventoryBatch : references

@enduml
```

**Description:**
`PreparationRecordService.create()` resolves `dishId` from its code, generates a `PREP-{yyyymmdd}-{seq}` code, and inserts a `preparation_batches` join row per batch actually used in the kitchen — including `extraBatches`, batches consumed outside the original issue slip (e.g. topping up a shortfall), flagged `extra=true` for audit visibility. This closes the loop between what was issued (§5) and what was actually cooked (UC-066).

---

### 6.2 Sequence Diagram — Log Preparation Record (UC-066, BR-076)

```plantuml
@startuml MF06_LogPreparation
title Sequence Diagram: Log Preparation Record

skinparam linetype ortho
skinparam backgroundColor #FFFFFF
skinparam sequenceMessageAlign center
skinparam responseMessageBelowArrow true

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
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

actor "Service Caterers\n(Kitchen Staff)" as SC
boundary ": PreparationLogScreen" as UI <<boundary>>
control ": PreparationRecordController" as C <<control>>
control ": PreparationRecordService" as S <<control>>
control ": ReferenceResolver" as RR <<control>>
entity "record : PreparationRecord" as PR <<entity>>
entity ": InventoryBatchRepository" as IBR <<repository>>
entity ": PreparationRecordRepository" as PRR <<repository>>
database ": PostgreSQL" as DB <<database>>

SC -> UI : open "Log preparation" for today's dish
activate UI
UI --> SC : render dish / time window / batch pickers
SC -> UI : submit { dishId, menuId, mealType, date,\nstartTime, endTime, batchesUsed[], extraBatches[], notes }
UI -> C : POST /api/v1/preparation-records
activate C
C -> S : create(CreateRequest, actor)
activate S

S -> RR : dishLabel(dishId)
activate RR
RR --> S : dishLabel : String (snapshot)
deactivate RR

alt endTime <= startTime
  S --> C : throw IngExceptions.invalidTimeWindow()
  C --> UI : 422 "endTime must be after startTime"
  UI --> SC : highlight time fields
else valid time window
  S -> S : code = "PREP-" + yyyymmdd + "-" + seq%99
  S -> PR : new PreparationRecord(cookUserId = actor.userId)
  activate PR
  PR --> S : record
  deactivate PR

  S -> PRR : save(record)
  activate PRR
  PRR -> DB : INSERT INTO preparation_records
  activate DB
  DB --> PRR : 1 row (id)
  deactivate DB
  PRR --> S : PreparationRecord (persisted)
  deactivate PRR

  loop for each code in batchesUsed
    S -> IBR : findBySchoolIdAndCode(schoolId, code)
    activate IBR
    IBR -> DB : SELECT * FROM inventory_batches WHERE code = ?
    activate DB
    DB --> IBR : row
    deactivate DB
    IBR --> S : InventoryBatch
    deactivate IBR
    S -> DB : INSERT INTO preparation_batches { extra = false }
    activate DB
    DB --> S : 1 row
    deactivate DB
  end

  loop for each code in extraBatches
    S -> DB : INSERT INTO preparation_batches { extra = true }
    activate DB
    DB --> S : 1 row
    deactivate DB
  end

  S --> C : PreparationRecordResponse
  C --> UI : 201 Created + PreparationRecordResponse
  UI --> SC : show logged prep record + PREP code
end
deactivate S
deactivate C
deactivate UI
@enduml
```

**Description:**
The record is a simple audit log tying a cook, a time window, a dish and the physical batches consumed together — no inventory transaction is written here (that already happened at issue time, §5.2); this is purely the kitchen's operational record for food-safety inspection (MF07) and traceability (§7).

---

## 7. Traceability

### 7.1 Class Diagram

```plantuml
@startuml MF06_Traceability_ClassDiagram
title Design-level Class Diagram: Traceability

skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 60
skinparam ranksep 60
skinparam backgroundColor #FFFFFF
hide empty members

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

class BatchTrace <<value object>> {
  +batchId : UUID
  +ingredientId : UUID
  +ingredient : String
  +supplierId : UUID
  +supplier : String
  +expiryDate : LocalDate
  +isLocked : boolean
}

class DishBatchesResponse <<DTO>> {
  +dishId : UUID
  +dish : String
  +menuId : UUID
  +serviceDate : LocalDate
  +mealType : MealType
  +batches : List<BatchTrace>
}

interface ITraceabilityService <<interface>> {
  +dishBatches(dishCode : String, serviceDate : LocalDate,\n  mealType : MealType, actor : AuthUser) : DishBatchesResponse
}

class TraceabilityService <<control>> {
  -jdbc : JdbcTemplate
  -referenceResolver : ReferenceResolver
  -clock : Clock
  +dishBatches(dishCode, serviceDate, mealType, actor) : DishBatchesResponse
}

class TraceabilityController <<boundary>> {
  -service : ITraceabilityService
  +dishBatches(dishId, serviceDate, mealType, actor) : DishBatchesResponse
}

interface InventoryTransactionRepository <<repository>> {
  +findIssuedBatchIds(schoolId : UUID, dishId : UUID,\n  serviceDate : LocalDate, mealType : MealType) : List<UUID>
}

interface InventoryBatchRepository <<repository>> {
  +findAllById(ids : List<UUID>) : List<InventoryBatch>
}

ITraceabilityService <|.. TraceabilityService
TraceabilityController --> ITraceabilityService : delegates

TraceabilityService --> InventoryTransactionRepository : joins ISSUE rows by key (BR-076)
TraceabilityService --> InventoryBatchRepository : resolves batch + supplier + expiry
TraceabilityService ..> DishBatchesResponse : builds
DishBatchesResponse "1" *-- "n" BatchTrace : batches

@enduml
```

**Description:**
`TraceabilityService.dishBatches()` is a pure read path implementing ADR-M06-3's key-based traceability: it joins `inventory_transactions` (filtered to `type = ISSUE`, matching `dishId`/`serviceDate`/`mealType`) back to `inventory_batches` to reconstruct exactly which supplier batches fed a specific dish on a specific service — every join is by UUID foreign key, never by matching label strings. This is the query MF11 (incident management) and MF07/MF08/MF09/MF13 consume when a food-safety recall must identify affected batches and, from there, affected supplier shipments.

---

### 7.2 Sequence Diagram — Dish → Batch Traceability Lookup

```plantuml
@startuml MF06_TraceabilityLookup
title Sequence Diagram: Dish to Batch Traceability Lookup

skinparam linetype ortho
skinparam backgroundColor #FFFFFF
skinparam sequenceMessageAlign center
skinparam responseMessageBelowArrow true

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
    GroupHeaderFontColor #FFFFFF
    GroupHeaderBackgroundColor #4CAF50
    GroupFontColor #212121
    GroupFontName Arial
}

actor "School Admin /\nService Caterers /\nMedical Staff" as U
boundary ": TraceabilityScreen" as UI <<boundary>>
control ": TraceabilityController" as C <<control>>
control ": TraceabilityService" as S <<control>>
entity "trace : BatchTrace" as BT <<entity>>
entity ": InventoryTransactionRepository" as ITR <<repository>>
entity ": InventoryBatchRepository" as IBR <<repository>>
database ": PostgreSQL" as DB <<database>>

U -> UI : choose dish + serviceDate + mealType, click "Trace"
activate UI
UI -> C : GET /api/v1/traceability/dish-batches\n?dishId=DISH-CANH-CHUA&serviceDate=2026-08-05&mealType=LUNCH
activate C
C -> S : dishBatches(dishCode, serviceDate, mealType, actor)
activate S

S -> ITR : findIssuedBatchIds(schoolId, dishId, serviceDate, mealType)
activate ITR
ITR -> DB : SELECT tx.batch_id, tx.menu_id\nFROM inventory_transactions tx\nWHERE tx.type = 'ISSUE' AND tx.dish_id = ?\nAND tx.service_date = ? AND tx.meal_type = ?\nAND tx.school_id = ? — BR-076 key join
activate DB
DB --> ITR : rows
deactivate DB
ITR --> S : List<UUID> batchIds
deactivate ITR

alt batchIds is empty
  S --> C : DishBatchesResponse { batches: [] }
  C --> UI : 200 OK (empty trace)
  UI --> U : show "No consumption recorded for this service"
else batchIds found
  S -> IBR : findAllById(batchIds)
  activate IBR
  IBR -> DB : SELECT * FROM inventory_batches WHERE id IN (...)
  activate DB
  DB --> IBR : rows
  deactivate DB
  IBR --> S : List<InventoryBatch>
  deactivate IBR

  loop for each batch
    S -> S : isLocked = expiryDate.isBefore(today) OR quarantined
    S -> BT : new BatchTrace(batchId, ingredient, supplier,\nexpiryDate, isLocked)
    activate BT
    BT --> S : trace
    deactivate BT
  end

  S --> C : DishBatchesResponse { batches[] }
  C --> UI : 200 OK + DishBatchesResponse
  UI --> U : render farm-to-table trace table
end
deactivate S
deactivate C
deactivate UI
@enduml
```

**Description:**
This endpoint answers "which supplier batches ended up in this dish, on this service date and meal" purely from the append-only ledger — no cache, no denormalized traceability table. Because `ck_tx_issue_trace` guarantees every `ISSUE` transaction carries `dishId`/`serviceDate`/`mealType`, this join can never return an incomplete trace for data written after MF06's rollout.

---

## 8. State Machines

### 8.1 Purchase Request Lifecycle (CF-M06-5)

```plantuml
@startuml MF06_PurchaseRequest_StateMachine
title State Diagram: Purchase Request Lifecycle

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

[*] --> PENDING : create()

PENDING --> QUOTED : quote() [supplier] / set price
PENDING --> REJECTED : reject() [supplier]
PENDING --> CANCELLED : cancel(reason)

QUOTED --> PRICE_ACCEPTED : acceptPrice() [school]
QUOTED --> CANCELLED : cancel(reason)

PRICE_ACCEPTED --> APPROVED : confirmSchedule() [supplier]\n/ bind supplierBatch (BR-071)
PRICE_ACCEPTED --> REJECTED : reject() [supplier]
PRICE_ACCEPTED --> CANCELLED : cancel(reason)

APPROVED --> COMPLETED : delivery received (§4.2)
APPROVED --> CANCELLED : cancel(reason) [before delivery]

REJECTED --> [*]
CANCELLED --> [*]
COMPLETED --> [*]

@enduml
```

**Description:**
The single-round pricing model (CF-M06-5) means there is exactly one `QUOTED → PRICE_ACCEPTED` transition — no negotiation loop back to `PENDING`. `CANCELLED` requires a `cancelReason` at the database level (`ck_pr_cancel_reason`). `COMPLETED` is reached indirectly: no `PurchaseRequestService` method sets it directly in the current implementation — it is inferred from the linked `Delivery`/`ReceivingSession` reaching `DELIVERED`, matching the TDS intent though not yet wired as an explicit transition (flagged as an implementation gap worth tracking).

---

### 8.2 Delivery Lifecycle

```plantuml
@startuml MF06_Delivery_StateMachine
title State Diagram: Delivery Lifecycle

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

[*] --> SCHEDULED : create() [PR APPROVED]

SCHEDULED --> DISPATCHED : updateStatus(DISPATCHED) [supplier]
SCHEDULED --> CANCELLED : updateStatus(CANCELLED) [supplier]

DISPATCHED --> IN_TRANSIT : updateStatus(IN_TRANSIT)
DISPATCHED --> CANCELLED : updateStatus(CANCELLED)

IN_TRANSIT --> DELAYED : delay > threshold / alert (MSG-065)
DELAYED --> IN_TRANSIT : back on track

IN_TRANSIT --> DELIVERED : receiving session created (§4.2)
DELAYED --> DELIVERED : receiving session created (§4.2)

DELIVERED --> COMPLETED : reconciled

CANCELLED --> [*]
COMPLETED --> [*]

@enduml
```

**Description:**
`DELIVERED` is deliberately not one of the statuses `DeliveryController.updateStatus()` can set directly — it is a *side effect* of a successful `ReceivingSessionService.create()` transaction (§4.2), ensuring physical receipt (with GPS, photos, supplier-status check) is the only path to that state, closing the loop between the supplier's claim and the school's confirmation.

---

### 8.3 Supplier Batch Lifecycle (BR-071)

```plantuml
@startuml MF06_SupplierBatch_StateMachine
title State Diagram: Supplier Batch Lifecycle

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

[*] --> ACTIVE : create() [cert valid, BR-071]

ACTIVE --> ACTIVE : uploadCertificate()
ACTIVE --> ACTIVE : confirmSchedule() / bind purchase_item

ACTIVE --> DELIVERED : batch accepted (§4.2)\n/ create inventory_batch
ACTIVE --> EXPIRED : today > expirationDate

DELIVERED --> [*]
EXPIRED --> [*]

@enduml
```

**Description:**
A supplier batch is created only if its certification dates pass BR-071, so `ACTIVE` is the sole entry state. It stays `ACTIVE` while certificates are attached and while it is bound to purchase lines (self-transitions — the status does not change). It leaves that state one of two ways: physically received at a school, which turns it into `DELIVERED` and creates the school-side `InventoryBatch`, or reaching its `expirationDate` untouched, which turns it into `EXPIRED` and removes it from the pool a supplier may schedule.

---

### 8.4 Receiving Session Lifecycle (BR-072/078, ADR-M06-5)

```plantuml
@startuml MF06_ReceivingSession_StateMachine
title State Diagram: Receiving Session Lifecycle

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

[*] --> IN_PROGRESS : open form (draft)

IN_PROGRESS --> RECEIVED : submit [all accepted]\n/ batches + RECEIPT txs, delivery = DELIVERED
IN_PROGRESS --> RECEIVED_PARTIAL : submit [some rejected]\n/ accepted lines only
IN_PROGRESS --> REJECTED : submit [all rejected] / no batch

RECEIVED --> [*]
RECEIVED_PARTIAL --> [*]
REJECTED --> [*]

@enduml
```

**Description:**
`IN_PROGRESS` exists only while the staff member is filling the form client-side; the backend writes the session already in its final state, computed by `computeOverallStatus(lines)`. All three end states are terminal because the session is an evidence record (GPS, photos, device, server timestamp) — corrections are made through a manual `ADJUSTMENT` transaction (§5.3), never by editing history.

---

### 8.5 Issue Slip Lifecycle (ADR-M06-6)

```plantuml
@startuml MF06_IssueSlip_StateMachine
title State Diagram: Issue Slip Lifecycle

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

[*] --> DRAFT : createIssueSlip() / FEFO plan (§5.2)

DRAFT --> CONFIRMED : kitchen confirms [planned]
CONFIRMED --> POSTED : post() / ISSUE txs written [planned]

DRAFT --> POSTED : auto-post [current impl]\n/ ISSUE txs in same tx

POSTED --> [*]

@enduml
```

**Description:**
`IssueSlipStatus` declares three states, but the current `InventoryService.createIssueSlip()` writes the slip directly as `POSTED` inside the same transaction as the `ISSUE` ledger rows — the `DRAFT → CONFIRMED → POSTED` path is reserved for the planned two-step kitchen approval. `POSTED` is terminal in every case: because `inventory_transactions` is append-only (ADR-M06-1), an over-issue is corrected by a compensating `ADJUSTMENT`, never by editing or deleting the slip.

---

### 8.6 Inventory Batch — Dual-Axis Lock Model (ADR-M06-2, BR-149/205)

```plantuml
@startuml MF06_InventoryBatch_DualAxis
title State Diagram: Inventory Batch Dual-Axis Lock Model

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

state "Shelf-life axis (derived, BR-205)" as Shelf {
  [*] --> IN_STOCK : line accepted (§4.2)
  IN_STOCK --> EXPIRING_SOON : expiryDate - today <= 2d
  EXPIRING_SOON --> EXPIRED : today >= expiryDate
  EXPIRED --> [*]
}

state "Quarantine axis (BR-149, Medical Staff only)" as Quar {
  [*] --> NOT_QUARANTINED : batch created
  NOT_QUARANTINED --> QUARANTINED : quarantine(reason)
  QUARANTINED --> NOT_QUARANTINED : unquarantine()
}

@enduml
```

**Description:**
`shelfStatus` and quarantine status are two fully independent axes (ADR-M06-2) — neither is derived from the other, and both are computed/read live rather than cached: shelf status from `expiryDate` vs. today, quarantine from the nullable `QuarantineInfo` embeddable. `isLocked()` is simply their logical OR, and every write path that consumes a batch (`FefoAllocator`, `createIssueSlip`, `adjust`) re-checks it at the moment of the write, not just at read time — closing the race between two staff members trying to use the same near-expiry or just-quarantined batch.

---

*Document generated from TDS `SFMS-MF06-IMP-001 v1.1` (Approved 2026-08-03). Class/sequence content verified against the actual implementation in `04_Development/Backend/src/main/java/com/sfms/mf06/**`, which in places (participant names, `ReferenceResolver` in place of ports, `IssueSlip` created directly as `POSTED`) diverges from the earlier TDS narrative — code is treated as ground truth throughout.*
