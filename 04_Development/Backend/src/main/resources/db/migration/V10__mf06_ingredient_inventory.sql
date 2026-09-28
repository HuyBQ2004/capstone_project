-- ═══════════════════════════════════════════════════════════════════════════════
-- V10__mf06_ingredient_inventory.sql — MF06 Ingredient & Inventory Management
--
-- Nguồn: TDS-MF06_Backend.md v1.1 §5.2 (M06-D01..M06-D16). KHÔNG sửa V1..V9 đã merge
-- (M06-D14). 4 cột audit (D9 — BaseEntity) lặp lại ở MỌI bảng dưới đây:
--     created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
--     updated_at  TIMESTAMPTZ,
--     created_by  VARCHAR(180),
--     updated_by  VARCHAR(180),
-- Entity `extends BaseEntity` KHÔNG khai lại 4 field này ở Java (D9).
-- ═══════════════════════════════════════════════════════════════════════════════

-- ================= Danh mục nguyên liệu — M06-D04 (C3 §2.1 Workflow) =================
-- Bảng này KHÔNG TDS nào tạo trước MF06; MF05 (`dish_ingredients.ingredient_code`,
-- `V8:103`) và toàn bộ MF06 đều tham chiếu. MF06 là chủ hợp lý.
CREATE TABLE catalog_ingredients (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),        -- D1
  code           VARCHAR(64)  NOT NULL UNIQUE,                      -- 'ING-THIT-LON-VAI'
  name           VARCHAR(180) NOT NULL,
  unit           VARCHAR(12)  NOT NULL,                             -- kg | lít | quả | hộp
  storage_zone   VARCHAR(10)  NOT NULL                              -- BR-202: khu YÊU CẦU
                 CHECK (storage_zone IN ('DRY','CHILLED','FROZEN')),
  refuse_factor  NUMERIC(4,3) NOT NULL DEFAULT 0                    -- ràng buộc C7
                 CHECK (refuse_factor >= 0 AND refuse_factor < 1),
  active         BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),                   -- D9
  updated_at  TIMESTAMPTZ,
  created_by  VARCHAR(180),
  updated_by  VARCHAR(180)
);
-- Danh mục dùng chung toàn hệ thống (không gắn school_id): `code` UNIQUE toàn cục là ĐÚNG.

-- MF05 đang chờ: `dish_ingredients.ingredient_code` khai không FK vì bảng trên chưa có
-- (`V8:103`, M05-D15). Nay nối lại — FK trỏ `code` (VARCHAR(64) bên MF05), KHÔNG trỏ `id`.
--
-- ⚠️ MÂU THUẪN PHÁT HIỆN TRONG BƯỚC 2 (không có trong TDS v1.1, báo lại ở report): TDS §5.2 đặt
-- nguyên khối ALTER này ngay trong V10 (cùng chỗ CREATE TABLE catalog_ingredients), nhưng
-- catalog_ingredients CHỈ được TẠO ở V10 — dữ liệu chỉ được SEED ở V11 (M06-D14: "V10 = schema,
-- V11 = seed"). Tại thời điểm V10 chạy, `dish_ingredients` (từ V9, đã merge) đã có sẵn 4 dòng
-- ingredient_code IN ('ING-CA-BASA','ING-DAU-PHU','ING-GAO','ING-TOM') nhưng catalog_ingredients
-- còn RỖNG ⇒ ADD CONSTRAINT ở đây FAIL ngay lệnh đầu tiên (đúng lớp lỗi mà chính TDS cảnh báo ở
-- Pre-Migration Checklist §11.2, nhưng tự mâu thuẫn với chỗ đặt câu lệnh). Xử lý: dời ALTER này
-- sang CUỐI V11 (SAU khi seed đủ catalog_ingredients) — giữ nguyên ý định TDS (FK trỏ `code`,
-- không trỏ `id`), chỉ đổi thứ tự file. Xem V11__seed_mf06.sql.

-- Danh mục mặt hàng NCC chào bán. M06-D03: `supplier_products` chưa từng tồn tại — MF06 tạo.
CREATE TABLE supplier_products (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code          VARCHAR(32) NOT NULL,                               -- 'SP-GF-014'
  partner_id    UUID NOT NULL REFERENCES partners(id),              -- MF04, PK UUID (V1:58)
  ingredient_id UUID NOT NULL REFERENCES catalog_ingredients(id),   -- C6: khoá, không tên
  unit_price    NUMERIC(14,2),
  active        BOOLEAN NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),                   -- D9
  updated_at  TIMESTAMPTZ,
  created_by  VARCHAR(180),
  updated_by  VARCHAR(180),
  CONSTRAINT uq_sp_partner_code UNIQUE (partner_id, code)
);

-- ================= Mua hàng (UC-021/UC-139/UC-015) =================
CREATE TABLE purchase_requests (
  id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- D1/M06-D01
  code                  VARCHAR(32) NOT NULL,                        -- "PR-2026-0815"
  school_id             UUID NOT NULL REFERENCES schools(id),
  supplier_id           UUID REFERENCES partners(id),                -- ref MF04
  requested_by_user_id  UUID REFERENCES user_accounts(id),           -- D9: KHÔNG đặt `created_by`
  requested_date        DATE NOT NULL,
  delivery_date         DATE NOT NULL,
  total_value           NUMERIC(14,2),                               -- NULL khi chưa báo giá
  status                VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  source_menu_id        UUID REFERENCES menus(id),                   -- BR-077 (đơn sinh từ shortage)
  source_meal_type      VARCHAR(12),
  service_date          DATE,
  quoted_at             TIMESTAMPTZ, quote_note   VARCHAR(500),
  price_accepted_at     TIMESTAMPTZ,
  accepted_at           TIMESTAMPTZ, confirmed_delivery_at TIMESTAMPTZ,  -- UC-015
  rejected_at           TIMESTAMPTZ,
  cancelled_at          TIMESTAMPTZ, cancel_reason VARCHAR(500),
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),                    -- D9
  updated_at  TIMESTAMPTZ,
  created_by  VARCHAR(180),
  updated_by  VARCHAR(180),
  CONSTRAINT uq_pr_school_code UNIQUE (school_id, code),             -- D1
  CONSTRAINT ck_pr_status CHECK (status IN
    ('PENDING','QUOTED','PRICE_ACCEPTED','APPROVED','COMPLETED','CANCELLED','REJECTED')),
  CONSTRAINT ck_pr_cancel_reason CHECK (status <> 'CANCELLED' OR cancel_reason IS NOT NULL)
);
CREATE INDEX ix_pr_supplier_status ON purchase_requests(supplier_id, status);
CREATE INDEX ix_pr_school_date     ON purchase_requests(school_id, requested_date);

CREATE TABLE purchase_items (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  purchase_request_id UUID NOT NULL REFERENCES purchase_requests(id) ON DELETE CASCADE,
  ingredient_id       UUID NOT NULL REFERENCES catalog_ingredients(id),   -- C6: khoá, không tên
  ingredient_label    VARCHAR(180) NOT NULL,
  quantity            NUMERIC(12,3) NOT NULL CHECK (quantity > 0),
  unit                VARCHAR(12)  NOT NULL,
  unit_price          NUMERIC(14,2),                                      -- từ bảng giá hợp đồng
  note                VARCHAR(255),
  -- BR-071: gán lô hàng NCC cho từng dòng nguyên liệu lúc xác nhận lịch giao
  -- (confirm-schedule). NULL trước lúc đó. Không có FK inline: supplier_batches
  -- được tạo SAU trong chính file này — ALTER TABLE ADD CONSTRAINT bên dưới.
  supplier_batch_id   UUID,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),                         -- D9
  updated_at  TIMESTAMPTZ,
  created_by  VARCHAR(180),
  updated_by  VARCHAR(180)
);

-- ================= Lô phía NCC (BR-071) =================
CREATE TABLE supplier_batches (
  id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),   -- D1/M06-D01
  code               VARCHAR(32)   NOT NULL,                       -- "BATCH-GF-4412"
  partner_id         UUID          NOT NULL REFERENCES partners(id),
  product_id         UUID          NOT NULL REFERENCES supplier_products(id),
  lot_number         VARCHAR(60)   NOT NULL,
  manufacturing_date DATE          NOT NULL,
  packaging_date     DATE          NOT NULL,
  expiration_date    DATE          NOT NULL,
  quantity           NUMERIC(12,3) NOT NULL CHECK (quantity > 0),
  unit               VARCHAR(12)   NOT NULL,
  status             VARCHAR(16)   NOT NULL DEFAULT 'ACTIVE',
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),                  -- D9
  updated_at  TIMESTAMPTZ,
  created_by  VARCHAR(180),
  updated_by  VARCHAR(180),
  CONSTRAINT uq_sb_partner_code UNIQUE (partner_id, code),
  CONSTRAINT ck_sb_status CHECK (status IN ('ACTIVE','DELIVERED','EXPIRED')),
  CONSTRAINT ck_sb_exp_after_mfg  CHECK (expiration_date > manufacturing_date),
  CONSTRAINT ck_sb_pack_after_mfg CHECK (packaging_date >= manufacturing_date)
);
CREATE UNIQUE INDEX uq_sb_product_lot ON supplier_batches(product_id, lot_number);

ALTER TABLE purchase_items
  ADD CONSTRAINT fk_purchase_items_batch FOREIGN KEY (supplier_batch_id) REFERENCES supplier_batches(id);

CREATE TABLE batch_certificates (                                   -- BR-071 (UC-100)
  batch_id    UUID NOT NULL REFERENCES supplier_batches(id) ON DELETE CASCADE,
  -- M06-D03: bảng thật tên `partner_documents` (`V6:61`). `supplier_documents` chưa từng tồn tại.
  document_id UUID NOT NULL REFERENCES partner_documents(id),
  PRIMARY KEY (batch_id, document_id)
);

-- ================= Giao hàng (UC-083/039/092/109) =================
CREATE TABLE deliveries (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- D1/M06-D01
  code                VARCHAR(32) NOT NULL,                        -- "DLV-2026-1105"
  school_id           UUID NOT NULL REFERENCES schools(id),
  purchase_request_id UUID REFERENCES purchase_requests(id),
  scheduled_at        TIMESTAMPTZ NOT NULL,
  driver_id           UUID REFERENCES delivery_persons(id),        -- MF04 `V6:89`
  vehicle_plate       VARCHAR(20) NOT NULL,
  status              VARCHAR(16) NOT NULL DEFAULT 'SCHEDULED',
  status_updated_at   TIMESTAMPTZ,                                 -- UC-092 audit trail
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),                  -- D9
  updated_at  TIMESTAMPTZ,
  created_by  VARCHAR(180),
  updated_by  VARCHAR(180),
  CONSTRAINT uq_dlv_school_code UNIQUE (school_id, code),          -- D1
  CONSTRAINT ck_dlv_status CHECK (status IN
    ('SCHEDULED','DISPATCHED','IN_TRANSIT','DELAYED','DELIVERED','CANCELLED','COMPLETED'))
);
CREATE INDEX ix_dlv_school_sched ON deliveries(school_id, scheduled_at);

CREATE TABLE delivery_batches (                                     -- BR-071 Linked Batches
  delivery_id       UUID NOT NULL REFERENCES deliveries(id) ON DELETE CASCADE,
  supplier_batch_id UUID NOT NULL REFERENCES supplier_batches(id),
  ingredient_id     UUID NOT NULL REFERENCES catalog_ingredients(id),
  PRIMARY KEY (delivery_id, supplier_batch_id, ingredient_id)
);

-- ================= Nhận hàng (BR-070/072/073/074/078) =================
CREATE TABLE receiving_sessions (
  id                         UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- D1/M06-D01
  code                       VARCHAR(32) NOT NULL,                  -- "RCV-20260702-02"
  school_id                  UUID NOT NULL REFERENCES schools(id),
  delivery_id                UUID NOT NULL REFERENCES deliveries(id),
  supplier_id                UUID NOT NULL REFERENCES partners(id),
  supplier_status_at_receipt VARCHAR(16) NOT NULL,                  -- BR-070
  received_at                TIMESTAMPTZ NOT NULL DEFAULT now(),    -- BR-073 SERVER ghi
  received_by_user_id        UUID NOT NULL REFERENCES user_accounts(id),  -- D9: field nghiệp vụ
  driver_verification_id     VARCHAR(80),                           -- Face Recognition (CF-M06-8)
  lat NUMERIC(9,6), lng NUMERIC(9,6),                               -- BR-074
  gps_verified               BOOLEAN NOT NULL DEFAULT FALSE,
  photos_count               INT     NOT NULL DEFAULT 0 CHECK (photos_count >= 0),
  device_info                VARCHAR(200),                          -- BR-073
  status                     VARCHAR(20) NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),                   -- D9
  updated_at  TIMESTAMPTZ,
  created_by  VARCHAR(180),
  updated_by  VARCHAR(180),
  CONSTRAINT uq_rcv_school_code UNIQUE (school_id, code),           -- D1
  CONSTRAINT ck_rcv_status CHECK (status IN
    ('IN_PROGRESS','RECEIVED','RECEIVED_PARTIAL','REJECTED')),
  CONSTRAINT ck_rcv_photos CHECK (status = 'IN_PROGRESS' OR photos_count >= 1)
);
CREATE INDEX ix_rcv_school_date ON receiving_sessions(school_id, received_at);

CREATE TABLE receiving_lines (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  session_id        UUID NOT NULL REFERENCES receiving_sessions(id) ON DELETE CASCADE,
  supplier_batch_id UUID NOT NULL REFERENCES supplier_batches(id),
  ingredient_id     UUID NOT NULL REFERENCES catalog_ingredients(id),
  ingredient_label  VARCHAR(180) NOT NULL,
  storage_zone      VARCHAR(10) NOT NULL
                    CHECK (storage_zone IN ('DRY','CHILLED','FROZEN')),   -- BR-202
  rejected          BOOLEAN NOT NULL DEFAULT FALSE,
  reject_reason     VARCHAR(500),
  -- D10-BIS.2: ảnh bằng chứng từ chối là tài liệu RIÊNG TƯ ⇒ lưu `storageKey`, KHÔNG URL công khai.
  reject_photo_keys JSONB NOT NULL DEFAULT '[]',
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),                   -- D9
  updated_at  TIMESTAMPTZ,
  created_by  VARCHAR(180),
  updated_by  VARCHAR(180),
  CONSTRAINT ck_line_reject CHECK (
    rejected = FALSE
    OR (reject_reason IS NOT NULL AND jsonb_array_length(reject_photo_keys) >= 1))
);

-- ================= Kho (BR-075/076/202/204/205) =================
CREATE TABLE inventory_batches (
  id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- D1/M06-D01
  code                   VARCHAR(32) NOT NULL,                      -- mã lô của NCC (BR-076)
  school_id              UUID NOT NULL REFERENCES schools(id),
  ingredient_id          UUID NOT NULL REFERENCES catalog_ingredients(id),
  ingredient_label       VARCHAR(180) NOT NULL,                     -- nhãn snapshot
  supplier_id            UUID NOT NULL REFERENCES partners(id),
  initial_quantity       NUMERIC(12,3) NOT NULL CHECK (initial_quantity > 0),
  unit                   VARCHAR(12) NOT NULL,
  storage_zone           VARCHAR(10) NOT NULL
                         CHECK (storage_zone IN ('DRY','CHILLED','FROZEN')),
  received_date          DATE NOT NULL,
  expiry_date            DATE NOT NULL,
  receiving_session_id   UUID REFERENCES receiving_sessions(id),    -- BR-076 gốc truy xuất
  quarantine_at          TIMESTAMPTZ,
  quarantine_by_user_id  UUID REFERENCES user_accounts(id),         -- D9: field nghiệp vụ
  quarantine_reason      VARCHAR(500),
  quarantine_incident_id UUID,                                      -- ref MF11 bằng ID
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),                   -- D9
  updated_at  TIMESTAMPTZ,
  created_by  VARCHAR(180),
  updated_by  VARCHAR(180),
  CONSTRAINT uq_batch_school_code UNIQUE (school_id, code),         -- D1
  CONSTRAINT ck_batch_exp_after_recv CHECK (expiry_date > received_date),
  CONSTRAINT ck_batch_quarantine_complete CHECK (
    quarantine_at IS NULL
    OR (quarantine_by_user_id IS NOT NULL AND quarantine_reason IS NOT NULL))
);
CREATE INDEX ix_batch_fefo    ON inventory_batches(school_id, ingredient_id, expiry_date); -- BR-075
CREATE INDEX ix_batch_session ON inventory_batches(receiving_session_id);

CREATE TABLE issue_slips (                                          -- ADR-M06-6 (MỚI)
  id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code               VARCHAR(32) NOT NULL,                          -- "ISS-2026-0031" (D1)
  school_id          UUID NOT NULL REFERENCES schools(id),
  service_date       DATE NOT NULL,
  meal_type          VARCHAR(12) NOT NULL,
  menu_ids           UUID[] NOT NULL,
  -- D9/M06-D02: KHÔNG dùng tên `created_by` (cột reserved của BaseEntity).
  issued_by_user_id  UUID NOT NULL REFERENCES user_accounts(id),
  status             VARCHAR(12) NOT NULL DEFAULT 'DRAFT'
                     CHECK (status IN ('DRAFT','CONFIRMED','POSTED')),
  note               VARCHAR(500),
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),                   -- D9
  updated_at  TIMESTAMPTZ,
  created_by  VARCHAR(180),
  updated_by  VARCHAR(180),
  CONSTRAINT uq_slip_school_code UNIQUE (school_id, code),          -- D1
  CONSTRAINT uq_slip_school_date_meal UNIQUE (school_id, service_date, meal_type)
);

CREATE TABLE inventory_transactions (                               -- APPEND-ONLY (ADR-M06-1)
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code              VARCHAR(32) NOT NULL,                           -- "TXN-20260728-01" (D1)
  school_id         UUID NOT NULL REFERENCES schools(id),
  occurred_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  type              VARCHAR(12) NOT NULL CHECK (type IN ('RECEIPT','ISSUE','ADJUSTMENT')),
  batch_id          UUID NOT NULL REFERENCES inventory_batches(id),
  ingredient_id     UUID NOT NULL REFERENCES catalog_ingredients(id),
  delta             NUMERIC(12,3) NOT NULL CHECK (delta <> 0),
  unit              VARCHAR(12) NOT NULL,
  -- D9/M06-D02: KHÔNG dùng tên `actor_id` — field nghiệp vụ "ai thực hiện giao dịch kho".
  performed_by_user_id UUID NOT NULL REFERENCES user_accounts(id),
  reason_code       VARCHAR(24) CHECK (reason_code IN
                      ('SPOILAGE','COUNT_CORRECTION','DAMAGE','RETURN_TO_SUPPLIER')),
  manager_signature VARCHAR(180),
  issue_slip_id     UUID REFERENCES issue_slips(id),
  menu_id           UUID REFERENCES menus(id),
  dish_id           UUID REFERENCES dishes(id),                     -- C6: khoá
  dish_label        VARCHAR(180),                                   -- nhãn snapshot
  meal_type         VARCHAR(12),
  service_date      DATE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),                   -- D9
  updated_at  TIMESTAMPTZ,
  created_by  VARCHAR(180),
  updated_by  VARCHAR(180),
  CONSTRAINT uq_tx_school_code UNIQUE (school_id, code),            -- D1
  CONSTRAINT ck_tx_adjustment CHECK (
    type <> 'ADJUSTMENT'
    OR (reason_code IS NOT NULL AND manager_signature IS NOT NULL)),
  CONSTRAINT ck_tx_issue_trace CHECK (
    type <> 'ISSUE'
    OR (issue_slip_id IS NOT NULL AND dish_id IS NOT NULL
        AND service_date IS NOT NULL AND meal_type IS NOT NULL)),
  CONSTRAINT ck_tx_sign CHECK (
    (type = 'RECEIPT' AND delta > 0) OR (type = 'ISSUE' AND delta < 0) OR type = 'ADJUSTMENT')
);
CREATE INDEX ix_tx_batch   ON inventory_transactions(batch_id);
CREATE INDEX ix_tx_slip    ON inventory_transactions(issue_slip_id);
CREATE INDEX ix_tx_service ON inventory_transactions(service_date, meal_type);
CREATE INDEX ix_tx_trace   ON inventory_transactions(menu_id, dish_id);

-- ================= Sổ chế biến (UC-066) =================
CREATE TABLE preparation_records (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),          -- D1/M06-D01
  code         VARCHAR(32) NOT NULL,                                -- "PREP-20260707-02"
  school_id    UUID NOT NULL REFERENCES schools(id),
  date         DATE NOT NULL,
  dish_id      UUID NOT NULL REFERENCES dishes(id),                 -- C6
  dish_label   VARCHAR(180) NOT NULL,
  menu_id      UUID REFERENCES menus(id),
  meal_type    VARCHAR(12),
  start_time   TIME NOT NULL,
  end_time     TIME NOT NULL,
  cook_user_id UUID NOT NULL REFERENCES user_accounts(id),          -- D9: field nghiệp vụ
  notes        VARCHAR(1000),
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),                   -- D9
  updated_at  TIMESTAMPTZ,
  created_by  VARCHAR(180),
  updated_by  VARCHAR(180),
  CONSTRAINT uq_prep_school_code UNIQUE (school_id, code),          -- D1
  CONSTRAINT ck_prep_time CHECK (end_time > start_time)
);
CREATE TABLE preparation_batches (
  preparation_record_id UUID NOT NULL
                        REFERENCES preparation_records(id) ON DELETE CASCADE,
  batch_id UUID NOT NULL REFERENCES inventory_batches(id),
  extra    BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (preparation_record_id, batch_id)
);

-- ================= Bằng chứng từ chối lô lúc nhận hàng (BR-072/078, D10-BIS.2) =================
-- Tài liệu RIÊNG TƯ (không CDN/URL công khai) — receiving-sessions tham chiếu bằng `code`,
-- KHÔNG lưu key/URL trực tiếp trong receiving_lines.reject_photo_keys (đã là document id).
CREATE TABLE receiving_evidence_documents (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code         VARCHAR(32) NOT NULL UNIQUE,
  school_id    UUID NOT NULL REFERENCES schools(id),
  file_key     VARCHAR(255) NOT NULL,
  content_type VARCHAR(100),
  file_size    BIGINT,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),                  -- D9
  created_by   VARCHAR(180)
);

-- ================= code_sequences — M06-D05: KHÔNG ALTER, chỉ dùng scope có sẵn =================
-- (không cần INSERT — CodeGenerator.next() tự INSERT ... ON CONFLICT lần gọi đầu tiên)
