-- V14__mf08_food_sample_retention.sql — MF08 Food Sample Retention (Lưu mẫu thực phẩm)
-- Nguồn: TDS-MF08_Backend.md v1.1 §5.2 (M08-D01..D05, D09 — Bước 0 + Bước 1, 2026-08-03)
--   · PK UUID + code VARCHAR(32) + UNIQUE(school_id, code)                      (D1, M08-D03)
--   · receiving_authorities là danh mục TOÀN HỆ THỐNG (không school_id) => UNIQUE(code) toàn cục
--   · 4 cột audit created_at/updated_at/created_by/updated_by (D9 — BaseEntity điền, Entity KHÔNG khai lại)
--   · FK schools(id) UUID (V1); inspection_id/dish_id/menu_id/incident_id là ID THUẦN, KHÔNG FK
--     cứng cross-module (ADR-M08-5) vì MF11 (incident) chưa merge và tránh khoá thứ tự migrate
-- =========================================================

CREATE TABLE receiving_authorities (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(32)  NOT NULL UNIQUE,                  -- D1 (M08-D03) — 'LAB-CDC-HN'
    name        VARCHAR(180) NOT NULL UNIQUE,
    type        VARCHAR(16)  NOT NULL CHECK (type IN ('LAB','AUTHORITY')),
    contact     VARCHAR(180),
    address     VARCHAR(255),
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    note        VARCHAR(400),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),           -- D9
    updated_at  TIMESTAMPTZ,
    created_by  VARCHAR(180),
    updated_by  VARCHAR(180)
);

CREATE TABLE sample_lab_requests (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                VARCHAR(32)  NOT NULL,                          -- D1 — 'LABREQ-2026-001'
    school_id           UUID         NOT NULL REFERENCES schools(id),  -- schools.id UUID (V1) OK
    incident_id         UUID,                                          -- MF11 (chưa merge) — BR-112/114
    authority_id        UUID         REFERENCES receiving_authorities(id),
    lab_destination     VARCHAR(180) NOT NULL,
    handover_type       VARCHAR(16)  NOT NULL DEFAULT 'LAB' CHECK (handover_type IN ('LAB','AUTHORITY')),
    test_criteria       VARCHAR(400),
    requested_by        VARCHAR(180) NOT NULL,                         -- BR-114 (JWT)
    requested_at        TIMESTAMPTZ  NOT NULL,
    delivered_by        VARCHAR(180) NOT NULL,                         -- BR-113
    sent_to_lab_at      TIMESTAMPTZ  NOT NULL,
    lab_representative  VARCHAR(180),
    status              VARCHAR(24)  NOT NULL DEFAULT 'SENT_TO_LAB',
    note                VARCHAR(400),
    -- legacy — chỉ đọc dữ liệu di trú, service KHÔNG sinh mới (ADR-M08-2)
    cancelled_at        TIMESTAMPTZ,
    cancelled_by        VARCHAR(180),
    cancel_reason       VARCHAR(400),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),           -- D9
    updated_at          TIMESTAMPTZ,
    created_by          VARCHAR(180),
    updated_by          VARCHAR(180),
    -- BR-212: gửi xét nghiệm (LAB) bắt buộc có chỉ tiêu; thu giữ (AUTHORITY) thì không
    CONSTRAINT ck_labreq_criteria CHECK (handover_type <> 'LAB' OR test_criteria IS NOT NULL),
    -- semantic-audit PHIEU_THU_GIU_THIEU_CO_QUAN
    CONSTRAINT ck_labreq_authority CHECK (handover_type <> 'AUTHORITY' OR authority_id IS NOT NULL)
);
CREATE UNIQUE INDEX uq_labreq_school_code ON sample_lab_requests(school_id, code);  -- D1 (M08-D03)
CREATE INDEX ix_labreq_incident ON sample_lab_requests(incident_id);

CREATE TABLE food_samples (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                VARCHAR(32)  NOT NULL,                         -- D1 — SMP-yyyyMMdd-NN
    school_id           UUID         NOT NULL REFERENCES schools(id),
    -- Bước 0 (2026-08-03) đã đối chiếu KIỂU PK của cả 4 bảng đích: schools/dishes/menus/
    -- inspection_sessions đều PK UUID => 4 cột dưới khai UUID là ĐÚNG. Không FK cứng
    -- cross-module (ADR-M08-5) — dishes/menus/inspection_sessions ở module khác, tránh
    -- khoá thứ tự migrate và giữ đúng nguyên tắc "tham chiếu bằng ID, không import entity".
    inspection_id       UUID         NOT NULL,                         -- MF07 inspection_sessions.id — BR-105
    dish_id             UUID         NOT NULL,                         -- MF05 dishes.id — BR-114
    menu_id             UUID,                                          -- MF05 menus.id — BR-114
    incident_id         UUID,                                          -- MF11 (chưa merge) — BR-112/114
    dish_name           VARCHAR(180) NOT NULL,                         -- Mẫu 4 (snapshot)
    meal_type           VARCHAR(16)  NOT NULL                          -- D6 (M08-D05); meal_label là dẫn xuất
                        CHECK (meal_type IN ('BREAKFAST','LUNCH','SNACK')),
    meal_shift          VARCHAR(60)  NOT NULL,                         -- cột 3 (giờ ăn, text tự do)
    servings            INT          NOT NULL CHECK (servings > 0),    -- cột 4
    sample_amount       NUMERIC(7,2) NOT NULL,                         -- cột 5
    sample_unit         VARCHAR(4)   NOT NULL CHECK (sample_unit IN ('G','ML')),
    container           VARCHAR(120) NOT NULL,                         -- cột 6
    sanitized           BOOLEAN      NOT NULL,                         -- Điều 6.2
    sealed              BOOLEAN      NOT NULL,                         -- Điều 7.1
    fridge_temp         NUMERIC(4,1) NOT NULL,                         -- cột 7
    temp_out_of_range   BOOLEAN      NOT NULL DEFAULT FALSE,           -- MSG-020
    collected_at        TIMESTAMPTZ  NOT NULL,                         -- cột 8
    collected_by        VARCHAR(180) NOT NULL,                         -- cột 11
    storage_location    VARCHAR(120) NOT NULL,
    retention_deadline  TIMESTAMPTZ  NOT NULL,                         -- BR-111 snapshot (ADR-M08-3)
    status              VARCHAR(24)  NOT NULL DEFAULT 'RETAINED',
    disposed_at         TIMESTAMPTZ,                                   -- cột 9
    disposed_by         VARCHAR(180),                                  -- cột 12
    disposal_witness    VARCHAR(180),                                  -- CF-M08-9
    disposal_note       VARCHAR(500),                                  -- cột 10
    lab_request_id      UUID REFERENCES sample_lab_requests(id),
    retrieved_at        TIMESTAMPTZ,
    sent_to_lab_at      TIMESTAMPTZ,
    gps_lat             NUMERIC(9,6),
    gps_lng             NUMERIC(9,6),
    device_info         VARCHAR(200),                                  -- BR-108
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),           -- D9 (entity KHÔNG khai lại)
    updated_at          TIMESTAMPTZ,
    created_by          VARCHAR(180),                                  -- D9 — AuditorAware điền
    updated_by          VARCHAR(180),                                  -- D9

    -- BR-203: >=100g đặc / >=150ml lỏng (chốt chặn cuối ở DB, service vẫn kiểm trước để trả MSG-019)
    CONSTRAINT ck_smp_amount CHECK (
        (sample_unit = 'G'  AND sample_amount >= 100) OR
        (sample_unit = 'ML' AND sample_amount >= 150)),
    -- BR-203 / Điều 6.2 + 7.1: dụng cụ tiệt trùng + niêm phong là điều kiện đăng ký
    CONSTRAINT ck_smp_container_state CHECK (sanitized AND sealed),
    -- BR-111: không hủy trước hạn lưu tối thiểu
    CONSTRAINT ck_smp_dispose_after_deadline CHECK (disposed_at IS NULL OR disposed_at >= retention_deadline),
    -- BR-112 / Mẫu 5 cột 9,10,12: hủy phải ghi đủ thời gian, người hủy, nhân chứng, ghi chú
    CONSTRAINT ck_smp_dispose_complete CHECK (
        status <> 'DISPOSED' OR (disposed_at IS NOT NULL AND disposed_by IS NOT NULL
                                 AND disposal_witness IS NOT NULL AND disposal_note IS NOT NULL)),
    -- BR-113: mẫu đã gửi lab bắt buộc gắn phiếu bàn giao (ADR-M08-1)
    CONSTRAINT ck_smp_sent_has_request CHECK (
        status <> 'SENT_TO_LAB' OR (lab_request_id IS NOT NULL AND sent_to_lab_at IS NOT NULL)),
    -- semantic-audit GUI_LAB_TRUOC_KHI_LAY / BAN_GIAO_TRUOC_KHI_LAY_MAU
    CONSTRAINT ck_smp_sent_after_collect CHECK (sent_to_lab_at IS NULL OR sent_to_lab_at >= collected_at)
);
CREATE UNIQUE INDEX uq_smp_school_code ON food_samples(school_id, code);  -- D1 (M08-D03)
CREATE INDEX ix_smp_school_collected ON food_samples(school_id, collected_at DESC);
CREATE INDEX ix_smp_status           ON food_samples(status);
CREATE INDEX ix_smp_incident         ON food_samples(incident_id);
CREATE INDEX ix_smp_lab_request      ON food_samples(lab_request_id);
-- Điều 7.1: mỗi món của một phiên kiểm thực chỉ lưu 1 mẫu (1 dụng cụ riêng/món)
CREATE UNIQUE INDEX uq_smp_inspection_dish ON food_samples(inspection_id, dish_id);

CREATE TABLE sample_attachments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_type      VARCHAR(16)  NOT NULL CHECK (owner_type IN ('SAMPLE','LAB_REQUEST')),
    owner_id        UUID         NOT NULL,
    kind            VARCHAR(24)  NOT NULL CHECK (kind IN ('EVIDENCE_PHOTO','SEAL_PHOTO','HANDOVER_RECORD','LAB_RECEIPT')),
    file_name       VARCHAR(255) NOT NULL,
    storage_key     VARCHAR(255) NOT NULL,
    size_bytes      BIGINT       NOT NULL,
    checksum_sha256 VARCHAR(64)  NOT NULL,
    -- uploaded_at/uploaded_by của TDS v1.0 trùng nghĩa created_at/created_by (M08-D02) — bỏ,
    -- DTO map uploadedAt <- created_at, uploadedBy <- created_by.
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),             -- D9
    updated_at      TIMESTAMPTZ,
    created_by      VARCHAR(180),
    updated_by      VARCHAR(180)
);
CREATE INDEX ix_att_owner ON sample_attachments(owner_type, owner_id, kind);
