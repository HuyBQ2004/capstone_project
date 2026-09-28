-- =========================================================
-- MF07 — Food Safety Inspection & Monitoring
-- Nguồn: TDS-MF07_Backend.md v1.3 §5.2 (M07-D01..D04, D07..D09 — Bước 0 + Bước 1, 2026-08-03)
--   · PK  UUID + code VARCHAR(32) + UNIQUE(school_id, code)   (D1)
--   · 4 cột audit created_at/updated_at/created_by/updated_by (D9 — BaseEntity điền, Entity KHÔNG khai lại)
--   · FK người dùng -> user_accounts(id) UUID, KHÔNG phải users(id)                      (M07-D03)
--   · FK schools/partners/menus/dishes/inventory_batches -> UUID                          (M07-D04)
--   · KHÔNG tạo violation_records/corrective_actions (MF11) / compliance-report (MF13)     (M07-D07/D08/D09)
-- =========================================================

CREATE TABLE inspection_sessions (
    id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),   -- D1
    code                  VARCHAR(32)  NOT NULL,                                -- D1 — "INSP-20260702-L"
    school_id             UUID         NOT NULL REFERENCES schools(id),
    partner_id            UUID         NOT NULL REFERENCES partners(id),
    date                  DATE         NOT NULL,
    meal_type             VARCHAR(16)  NOT NULL CHECK (meal_type IN ('BREAKFAST','LUNCH','SNACK')),
    menu_id               UUID         REFERENCES menus(id),
    dish_id               UUID         REFERENCES dishes(id),
    step1                 VARCHAR(12)  NOT NULL DEFAULT 'PENDING' CHECK (step1 IN ('PENDING','PASSED','FAILED')),
    step2                 VARCHAR(12)  NOT NULL DEFAULT 'PENDING' CHECK (step2 IN ('PENDING','PASSED','FAILED')),
    step3                 VARCHAR(12)  NOT NULL DEFAULT 'PENDING' CHECK (step3 IN ('PENDING','PASSED','FAILED')),
    -- CF-23: khởi tạo và giữ 'IN_PROGRESS' cho tới khi bước 3 ĐẠT
    overall               VARCHAR(12)  NOT NULL DEFAULT 'PENDING'
                          CHECK (overall IN ('PENDING','IN_PROGRESS','PASSED','FAILED')),
    -- CF-25 (ĐÓNG 2026-08-03): lý do + mức độ + người chứng kiến khi một bước KHÔNG ĐẠT (BR-095/BR-183)
    fail_step             INT          CHECK (fail_step BETWEEN 1 AND 3),
    fail_reason           VARCHAR(1000),
    fail_severity         VARCHAR(8)   CHECK (fail_severity IN ('LOW','MEDIUM','HIGH')),
    fail_witness          VARCHAR(120),
    decided_at            TIMESTAMPTZ,
    core_temp             NUMERIC(5,2),
    holding_temp          NUMERIC(5,2),
    inspector_user_id     UUID         NOT NULL REFERENCES user_accounts(id),   -- M07-D03
    co_signer_user_id     UUID         REFERENCES user_accounts(id),            -- M07-D03
    co_signed_at          TIMESTAMPTZ,
    gps                   VARCHAR(64),
    staff_notes           VARCHAR(2000),
    -- BR-222 (mới 2026-08-03, chính thức hoá CF-10): đánh giá ĐỘC LẬP của Medical Staff,
    -- KHÔNG ghi đè overall, KHÔNG đổi kết quả BR-094/BR-098.
    medical_review_result VARCHAR(12) CHECK (medical_review_result IN ('PASSED','FAILED')),
    medical_review_reason VARCHAR(1000),
    medical_review_by     UUID         REFERENCES user_accounts(id),            -- M07-D03
    medical_review_at     TIMESTAMPTZ,
    -- D9 / M07-D02: 4 cột audit — DDL tạo, BaseEntity điền, Entity KHÔNG khai lại
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by            VARCHAR(180),
    updated_by            VARCHAR(180),
    -- BR-098: overall PASSED chỉ khi cả 3 bước PASSED
    CONSTRAINT ck_insp_overall_passed
      CHECK (overall <> 'PASSED' OR (step1='PASSED' AND step2='PASSED' AND step3='PASSED')),
    -- BR-090: đã chốt Bước 2 PASSED thì phải có nhiệt độ lõi >= 75
    CONSTRAINT ck_insp_core_temp
      CHECK (step2 <> 'PASSED' OR (core_temp IS NOT NULL AND core_temp >= 75)),
    -- BR-091: đã chốt Bước 3 PASSED thì phải có nhiệt độ giữ ấm >= 60.
    -- ⚠️ CF-24: FE Bước 3 hiện KHÔNG có ô nhập holdingTemp — CHƯA enable CHECK này ở bản này,
    --    chỉ enforce ở service layer (InspectionSessionServiceImpl) để không chặn phiên hợp lệ hiện tại.
    -- CONSTRAINT ck_insp_holding_temp
    --   CHECK (step3 <> 'PASSED' OR (holding_temp IS NOT NULL AND holding_temp >= 60)),
    -- BR-222: đánh giá Y tế "Không đạt" bắt buộc có lý do
    CONSTRAINT ck_insp_medical_reason
      CHECK (medical_review_result <> 'FAILED' OR medical_review_reason IS NOT NULL)
);
CREATE UNIQUE INDEX uq_insp_code  ON inspection_sessions (school_id, code);
CREATE INDEX ix_insp_school_date ON inspection_sessions (school_id, date);
CREATE INDEX ix_insp_overall     ON inspection_sessions (overall);
CREATE INDEX ix_insp_steps       ON inspection_sessions (step1, step2, step3);
-- BR-079: một bữa (ngày × buổi × đơn vị) chỉ có 1 phiên gốc còn hiệu lực; tái kiểm tra là phiên MỚI
CREATE UNIQUE INDEX ux_insp_slot ON inspection_sessions (school_id, date, meal_type, partner_id)
    WHERE overall <> 'FAILED';

-- ---- Mẫu số 1 phần I: thực phẩm tươi sống (QĐ 1246 Điều 3) ----
CREATE TABLE step1_fresh_rows (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id            UUID NOT NULL REFERENCES inspection_sessions(id) ON DELETE CASCADE,
    row_order             INT NOT NULL DEFAULT 0,
    name                  VARCHAR(160) NOT NULL,
    batch_id              UUID REFERENCES inventory_batches(id),  -- BR-092/093
    received_at           VARCHAR(32)  NOT NULL,
    quantity              VARCHAR(32)  NOT NULL,
    supplier_name         VARCHAR(160) NOT NULL,
    supplier_contact      VARCHAR(200),
    deliverer             VARCHAR(120),
    has_invoice           BOOLEAN NOT NULL DEFAULT FALSE,
    has_vet_cert          BOOLEAN NOT NULL DEFAULT FALSE,
    has_quarantine_cert   BOOLEAN NOT NULL DEFAULT FALSE,
    sensory_pass          BOOLEAN NOT NULL,
    quick_test            VARCHAR(8) NOT NULL DEFAULT 'NONE' CHECK (quick_test IN ('PASS','FAIL','NONE')),
    action                VARCHAR(500),
    photo_count           INT NOT NULL DEFAULT 0,
    CONSTRAINT ck_s1f_evidence CHECK (sensory_pass = TRUE OR photo_count >= 1)
);
CREATE INDEX ix_s1f_session ON step1_fresh_rows (session_id);

-- ---- Mẫu số 1 phần II: thực phẩm khô/bao gói sẵn ----
CREATE TABLE step1_dry_rows (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id       UUID NOT NULL REFERENCES inspection_sessions(id) ON DELETE CASCADE,
    row_order        INT NOT NULL DEFAULT 0,
    name             VARCHAR(160) NOT NULL,
    batch_id         UUID REFERENCES inventory_batches(id),
    producer         VARCHAR(160),
    producer_address VARCHAR(240),
    received_at      VARCHAR(32) NOT NULL,
    quantity         VARCHAR(32) NOT NULL,
    supplier_name    VARCHAR(160) NOT NULL,
    deliverer        VARCHAR(120),
    supplier_contact VARCHAR(200),
    expiry           DATE,                                     -- BR-092
    storage          VARCHAR(16) CHECK (storage IN ('THUONG','LANH','DONG')),
    has_invoice      BOOLEAN NOT NULL DEFAULT FALSE,
    sensory_pass     BOOLEAN NOT NULL,
    action           VARCHAR(500),
    photo_count      INT NOT NULL DEFAULT 0,
    CONSTRAINT ck_s1d_evidence CHECK (sensory_pass = TRUE OR photo_count >= 1)
);
CREATE INDEX ix_s1d_session ON step1_dry_rows (session_id);

-- ---- Mẫu số 2: kiểm tra khi chế biến (QĐ 1246 Điều 4) ----
CREATE TABLE step2_rows (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id        UUID NOT NULL REFERENCES inspection_sessions(id) ON DELETE CASCADE,
    row_order         INT NOT NULL DEFAULT 0,
    shift             VARCHAR(48) NOT NULL,
    dish_id           UUID REFERENCES dishes(id),
    dish_name         VARCHAR(160) NOT NULL,
    main_ingredients  VARCHAR(500),
    servings          VARCHAR(16),
    prep_done_at      VARCHAR(32),
    cook_done_at      VARCHAR(32),
    hygiene_staff     BOOLEAN NOT NULL,
    hygiene_equipment BOOLEAN NOT NULL,
    hygiene_area      BOOLEAN NOT NULL,
    sensory_pass      BOOLEAN NOT NULL,
    core_temp         NUMERIC(5,2),
    action            VARCHAR(500),
    photo_count       INT NOT NULL DEFAULT 0,
    CONSTRAINT ck_s2_evidence CHECK (sensory_pass = TRUE OR photo_count >= 1)
);
CREATE INDEX ix_s2_session ON step2_rows (session_id);

-- ---- Mẫu số 3: kiểm tra trước khi ăn (QĐ 1246 Điều 5) ----
CREATE TABLE step3_rows (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id        UUID NOT NULL REFERENCES inspection_sessions(id) ON DELETE CASCADE,
    row_order         INT NOT NULL DEFAULT 0,
    shift             VARCHAR(48) NOT NULL,
    dish_id           UUID REFERENCES dishes(id),
    dish_name         VARCHAR(160) NOT NULL,
    servings          VARCHAR(16),
    divided_done_at   VARCHAR(32),
    eat_start_at      VARCHAR(32),
    utensils          VARCHAR(300),
    holding_temp      NUMERIC(5,2),
    sensory_pass      BOOLEAN NOT NULL,
    action            VARCHAR(500),
    photo_count       INT NOT NULL DEFAULT 0,
    CONSTRAINT ck_s3_evidence CHECK (sensory_pass = TRUE OR photo_count >= 1)
);
CREATE INDEX ix_s3_session ON step3_rows (session_id);

-- ---- Ảnh bằng chứng dùng chung (BR-083, BR-207, BR-211) — D10-BIS: tài liệu RIÊNG TƯ ----
CREATE TABLE inspection_evidences (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_type  VARCHAR(32)  NOT NULL,   -- SESSION | STEP1_FRESH | STEP1_DRY | STEP2 | STEP3 | KITCHEN_ITEM
    owner_id    UUID         NOT NULL,   -- PK nội bộ của bảng chủ
    storage_key VARCHAR(500) NOT NULL,   -- D10-BIS: lưu key, KHÔNG lưu URL công khai
    file_name   VARCHAR(200) NOT NULL,
    size_bytes  BIGINT       NOT NULL,
    sha256      VARCHAR(64)  NOT NULL,   -- ADR-M07-04: chống sửa ảnh bằng chứng
    gps         VARCHAR(64),
    uploaded_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    uploaded_by UUID         NOT NULL REFERENCES user_accounts(id)
);
CREATE INDEX ix_evidence_owner ON inspection_evidences (owner_type, owner_id);

-- ---- Kiểm tra an toàn & vệ sinh bếp (UC-014) ----
CREATE TABLE kitchen_inspections (
    id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),   -- D1
    code                  VARCHAR(32)  NOT NULL,                                -- D1 — "KIN-2026-018"
    school_id             UUID         NOT NULL REFERENCES schools(id),
    partner_id            UUID         REFERENCES partners(id),
    date                  DATE         NOT NULL,
    score                 INT          NOT NULL CHECK (score BETWEEN 0 AND 100),   -- BR-100/BR-104
    inspector_user_id     UUID         NOT NULL REFERENCES user_accounts(id),
    pest_control_valid    BOOLEAN      NOT NULL DEFAULT TRUE,
    staff_health_valid    BOOLEAN      NOT NULL DEFAULT TRUE,   -- BR-103
    result                VARCHAR(12)  NOT NULL CHECK (result IN ('PASSED','FAILED')),
    confirm_status        VARCHAR(32)  NOT NULL DEFAULT 'AWAITING_CATERERS_CONFIRM'
                          CHECK (confirm_status IN ('AWAITING_CATERERS_CONFIRM','CONFIRMED')),  -- BR-221
    confirmed_by          UUID         REFERENCES user_accounts(id),
    confirmed_at          TIMESTAMPTZ,
    record_caterers_rep   UUID         REFERENCES user_accounts(id),
    record_pdf_exported_at TIMESTAMPTZ,
    record_pdf_version    INT,
    record_signed_at      TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by            VARCHAR(180),
    updated_by            VARCHAR(180),
    -- ADR-M07-03: <60 điểm => FAILED (cảnh báo, KHÔNG khóa bếp)
    CONSTRAINT ck_kin_result_score CHECK ((score >= 60) = (result = 'PASSED'))
);
CREATE UNIQUE INDEX uq_kin_code   ON kitchen_inspections (school_id, code);
CREATE INDEX ix_kin_school_date ON kitchen_inspections (school_id, date);

-- ---- Checklist động (BR-207/BR-210) — M07-D10: MF07 sở hữu bảng + controller, MF12 chỉ đọc ----
CREATE TABLE checklist_groups (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),   -- D1
    code          VARCHAR(32)  NOT NULL,                                -- D1 — "CLG-1"
    school_id     UUID         NOT NULL REFERENCES schools(id),         -- BR-210: phạm vi TỪNG TRƯỜNG
    name          VARCHAR(200) NOT NULL,
    display_order INT          NOT NULL DEFAULT 0,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,                   -- BR-210: xoá = ẩn, giữ lịch sử
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(180),
    updated_by    VARCHAR(180)
);
CREATE UNIQUE INDEX uq_clg_code ON checklist_groups (school_id, code);

CREATE TABLE checklist_items (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),   -- D1
    code          VARCHAR(32)  NOT NULL,                                -- D1 — "CLI-1-1"
    group_id      UUID         NOT NULL REFERENCES checklist_groups(id),
    school_id     UUID         NOT NULL REFERENCES schools(id),         -- cần cho UNIQUE(school_id, code)
    name          VARCHAR(300) NOT NULL,
    legal_note    TEXT,                        -- BR-207
    display_order INT          NOT NULL DEFAULT 0,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(180),
    updated_by    VARCHAR(180)
);
CREATE UNIQUE INDEX uq_cli_code ON checklist_items (school_id, code);
CREATE INDEX ix_cli_group ON checklist_items (group_id);

CREATE TABLE checklist_item_results (
    inspection_id UUID NOT NULL REFERENCES kitchen_inspections(id) ON DELETE CASCADE,
    item_id       UUID NOT NULL REFERENCES checklist_items(id),
    passed        BOOLEAN     NOT NULL,
    reason        VARCHAR(1000),
    photo_count   INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (inspection_id, item_id),
    -- BR-207: "Không đạt" bắt buộc ≥1 ảnh gắn đúng hạng mục + lý do
    CONSTRAINT ck_cir_evidence CHECK (passed = TRUE OR (photo_count >= 1 AND reason IS NOT NULL))
);

-- =========================================================
-- ⛔ BIÊN BẢN VI PHẠM (violation_records/corrective_actions) — KHÔNG TẠO Ở ĐÂY.
-- Sở hữu bởi MF11 (M07-D07/D08, chốt 2026-08-03) — xem TDS-MF07_Backend.md §5.2 để có đủ cột
-- tham khảo (13 cột CF-7/CF-28/CF-31). MF07 đọc qua ViolationQueryPort do MF11 expose.
-- =========================================================

-- ---- Ký xác nhận an toàn hằng ngày của BGH (UC-027) ----
CREATE TABLE safety_signoffs (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),   -- D1
    code           VARCHAR(32) NOT NULL,                                -- D1 — "SGN-2026-014"
    school_id      UUID        NOT NULL REFERENCES schools(id),
    date           DATE        NOT NULL,
    signed_by      UUID        NOT NULL REFERENCES user_accounts(id),
    signed_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    note           VARCHAR(1000),
    -- CF-32: snapshot căn cứ tại thời điểm ký — không đổi theo dữ liệu về sau
    hygiene_score  INT         CHECK (hygiene_score BETWEEN 0 AND 100),
    failed_steps   INT         NOT NULL DEFAULT 0 CHECK (failed_steps >= 0),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by     VARCHAR(180),
    updated_by     VARCHAR(180),
    CONSTRAINT uq_signoff_day UNIQUE (school_id, date)
);
CREATE UNIQUE INDEX uq_sgn_code ON safety_signoffs (school_id, code);
