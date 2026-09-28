-- ═══════════════════════════════════════════════════════════════════════════════
-- V6__mf04_supplier_partner.sql — MF04 Supplier & Partner Management
--
-- Nguồn: TDS-MF04_Backend.md §5.2 (M04-D14). Bảng `partners` ĐÃ TỒN TẠI ở
-- V1__init_schema.sql (dòng 57-66, stub cho FK user_accounts.partner_id) — KHÔNG
-- CREATE TABLE lại (đổi checksum V1 làm migrate fail trên máy người khác). Giữ
-- nguyên tên cột stub `partner_type` (không đổi thành `type`: phá ck_partner_type),
-- `code VARCHAR(32)`, `name VARCHAR(200)`. Cùng cách V3 (MF02) đã ALTER `schools`.
--
-- 00_Common_Conventions.md áp dụng: D1 (code = mã nghiệp vụ), D4 (ISO-8601/TIMESTAMPTZ),
-- D6 (enum SCREAMING_SNAKE — PartnerType/BiometricStatus dùng chung common/enums),
-- D9 (created_by/updated_by reserved cho BaseEntity), D10 (file lưu qua StorageService).
-- ═══════════════════════════════════════════════════════════════════════════════

-- ───────────────────────── ALTER partners (stub V1) ─────────────────────────
ALTER TABLE partners
  ALTER COLUMN status SET DEFAULT 'PENDING',                          -- stub V1 để 'ACTIVE'
  ADD COLUMN tax_id VARCHAR(20) NOT NULL DEFAULT '',
  ADD COLUMN address VARCHAR(255),
  ADD COLUMN contact VARCHAR(180),                                    -- "Lý Thái Sơn — 0913 222 333"
  ADD COLUMN email VARCHAR(180),                                      -- MSG-006 (BR-012)
  ADD COLUMN phone VARCHAR(20),                                       -- BR-012
  ADD COLUMN description VARCHAR(1000),                               -- UC-049
  ADD COLUMN performance_score NUMERIC(5,2),                          -- CF-M04-4: chỉ lưu, không tự tính
  ADD COLUMN site_inspection_score NUMERIC(5,2),                      -- UC-108 (chỉ Caterer)
  ADD COLUMN applied_at TIMESTAMPTZ NOT NULL DEFAULT now(),           -- D4
  ADD COLUMN reject_reason  VARCHAR(1000),                            -- BR-048
  ADD COLUMN suspend_reason VARCHAR(1000),                            -- ADR-M04-6
  ADD COLUMN created_by VARCHAR(180),                                 -- D9 BaseEntity (stub thiếu)
  ADD COLUMN updated_by VARCHAR(180);                                 -- D9 BaseEntity (stub thiếu)

-- Bảng rỗng ở thời điểm migration này chạy trên môi trường thật (V1 chỉ tạo cấu
-- trúc) ⇒ siết NOT NULL an toàn. Môi trường test có thêm seed ở db/testseed/V900
-- (chạy SAU migration này) — seed đó phải tự cung cấp giá trị cho các cột này.
ALTER TABLE partners ALTER COLUMN tax_id DROP DEFAULT;
ALTER TABLE partners
  ALTER COLUMN address SET NOT NULL,
  ALTER COLUMN contact SET NOT NULL,
  ALTER COLUMN email   SET NOT NULL;

CREATE UNIQUE INDEX ux_partner_tax_id ON partners(tax_id) WHERE tax_id <> '';
CREATE UNIQUE INDEX ux_partner_email  ON partners(email);             -- MSG-006

ALTER TABLE partners
  -- BR-041 + ADR-M04-6 (stub V1 không có CHECK trên status)
  ADD CONSTRAINT ck_partner_status CHECK (status IN ('PENDING','APPROVED','REJECTED','SUSPENDED')),
  -- BR-048 / ADR-M04-6: trạng thái loại trừ luôn phải kèm lý do
  ADD CONSTRAINT ck_partner_reject_reason  CHECK (status <> 'REJECTED'  OR reject_reason  IS NOT NULL),
  ADD CONSTRAINT ck_partner_suspend_reason CHECK (status <> 'SUSPENDED' OR suspend_reason IS NOT NULL),
  -- siteInspectionScore chỉ có nghĩa với Service Caterer (UC-108)
  ADD CONSTRAINT ck_partner_site_score
      CHECK (site_inspection_score IS NULL OR partner_type = 'SERVICE_CATERER');

CREATE INDEX ix_partner_status ON partners(status);
CREATE INDEX ix_partner_type   ON partners(partner_type);

-- ───────────────────────── 5 bảng mới (CREATE) ─────────────────────────
-- created_at/updated_at/created_by/updated_by có mặt ở MỌI bảng vì entity extends
-- BaseEntity (D9, M04-D12).

CREATE TABLE partner_documents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(32) NOT NULL UNIQUE,                                   -- D1
  partner_id UUID NOT NULL REFERENCES partners(id),                   -- CF-M04-6
  name VARCHAR(180) NOT NULL,
  document_type VARCHAR(24) NOT NULL
    CHECK (document_type IN ('FOOD_SAFETY_CERT','PRODUCT_ORIGIN','BUSINESS_LICENSE','SELF_DECLARATION')),
  number VARCHAR(60) NOT NULL,
  issued_by VARCHAR(180) NOT NULL,
  issued_date DATE NOT NULL,
  expiry_date DATE,                                                   -- NULL = "Không thời hạn"
  file_key VARCHAR(255) NOT NULL,                                     -- D10-BIS: storageKey riêng tư, KHÔNG phải URL công khai
  file_size BIGINT NOT NULL CHECK (file_size <= 10485760),            -- MSG-008: 10MB
  content_type VARCHAR(60) NOT NULL
    CHECK (content_type IN ('application/pdf','image/jpeg','image/png')),  -- MSG-009
  superseded_by_document_id UUID REFERENCES partner_documents(id),    -- UC-091
  uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ,   -- D9 BaseEntity
  created_by  VARCHAR(180), updated_by VARCHAR(180),                        -- D9 BaseEntity
  -- UC-101 AF1: chứng nhận ATTP bắt buộc có hạn và KHÔNG được nhập hạn đã qua
  CONSTRAINT ck_doc_cert_expiry CHECK (document_type <> 'FOOD_SAFETY_CERT' OR expiry_date IS NOT NULL),
  CONSTRAINT ck_doc_date_order  CHECK (expiry_date IS NULL OR expiry_date > issued_date),
  -- UC-101 AF3: trùng (loại + cơ quan cấp + số hiệu) trong cùng 1 partner
  CONSTRAINT uq_doc_partner_number UNIQUE (partner_id, document_type, number)
);
CREATE INDEX ix_doc_partner_type ON partner_documents(partner_id, document_type);
CREATE INDEX ix_doc_expiry       ON partner_documents(expiry_date);  -- job BR-047

CREATE TABLE delivery_persons (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(32) NOT NULL UNIQUE,                                   -- D1
  partner_id UUID NOT NULL REFERENCES partners(id),                   -- CF-M04-6
  full_name VARCHAR(120) NOT NULL,
  national_id_enc BYTEA NOT NULL,                                     -- pgcrypto (PDPD, ADR-M04-5)
  national_id_hash VARCHAR(64) NOT NULL UNIQUE,                       -- BR-050: duy nhất toàn hệ thống
  phone VARCHAR(20) NOT NULL,
  vehicle_plate VARCHAR(16) NOT NULL,
  biometric_status VARCHAR(24) NOT NULL DEFAULT 'NOT_REGISTERED'
    CHECK (biometric_status IN ('NOT_REGISTERED','PENDING_REGISTRATION','REGISTERED')),  -- BR-051
  face_id VARCHAR(64),
  biometric_registered_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ,     -- D9 BaseEntity
  created_by VARCHAR(180), updated_by VARCHAR(180),                          -- D9 BaseEntity
  -- BR-051 (M04-D03): face_id do FRS cấp ⇒ CHỈ tồn tại ở REGISTERED.
  --   NOT_REGISTERED        : chưa gửi ảnh, hoặc đã xóa (UC-028)      → face_id NULL
  --   PENDING_REGISTRATION  : đã gửi ảnh, FRS CHƯA trả faceId         → face_id NULL
  --   REGISTERED            : FRS đã trả faceId (MSG-038)             → face_id NOT NULL
  CONSTRAINT ck_dp_face CHECK (
    (biometric_status =  'REGISTERED' AND face_id IS NOT NULL AND biometric_registered_at IS NOT NULL) OR
    (biometric_status <> 'REGISTERED' AND face_id IS NULL     AND biometric_registered_at IS NULL)
  )
);
CREATE INDEX ix_dp_partner ON delivery_persons(partner_id);

CREATE TABLE partnership_applications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(32) NOT NULL UNIQUE,                                   -- D1
  partner_id UUID NOT NULL REFERENCES partners(id),                   -- CF-M04-9
  school_id  UUID NOT NULL REFERENCES schools(id),                    -- CF-M04-9 (MF02)
  company_name VARCHAR(180) NOT NULL,
  tax_id VARCHAR(20) NOT NULL,
  address VARCHAR(255) NOT NULL,
  submitted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
    CHECK (status IN ('PENDING','APPROVED','REJECTED','ACTION_REQUIRED')),
  comment VARCHAR(1000),                                              -- BR-048
  -- reviewed_by là field NGHIỆP VỤ (School Admin nào duyệt), khác `updated_by`
  -- reserved của BaseEntity (D9) nên không đụng nhau.
  reviewed_by UUID, reviewed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ,   -- D9 BaseEntity
  created_by VARCHAR(180), updated_by VARCHAR(180),                        -- D9 BaseEntity
  -- BR-048: từ chối / yêu cầu bổ sung luôn phải có phản hồi
  CONSTRAINT ck_app_comment CHECK (status NOT IN ('REJECTED','ACTION_REQUIRED') OR comment IS NOT NULL)
);
-- UC-152/156 Preconditions: "does not have an active application" — 1 đơn đang mở / (partner, school)
CREATE UNIQUE INDEX uq_app_open_per_partner_school
  ON partnership_applications(partner_id, school_id)
  WHERE status IN ('PENDING','ACTION_REQUIRED');
CREATE INDEX ix_app_school_status ON partnership_applications(school_id, status);

-- Tài liệu đính kèm đơn (form nộp kèm certificateDocumentIds — CF-M04-9)
CREATE TABLE partnership_application_documents (
  application_id UUID NOT NULL REFERENCES partnership_applications(id) ON DELETE CASCADE,
  document_id    UUID NOT NULL REFERENCES partner_documents(id),
  PRIMARY KEY (application_id, document_id)
);

CREATE TABLE supplier_partnerships (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(32) NOT NULL UNIQUE,                                   -- D1
  partner_id UUID NOT NULL REFERENCES partners(id),                   -- CF-M04-6
  school_id  UUID NOT NULL REFERENCES schools(id),
  delivery_address VARCHAR(255),
  since DATE,                                                         -- JSON: startDate (D4)
  until DATE,                                                         -- JSON: endDate; NULL = còn hiệu lực (M04-D04)
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING'
    CHECK (status IN ('PENDING','APPROVED','SUSPENDED')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ,   -- D9 BaseEntity
  created_by VARCHAR(180), updated_by VARCHAR(180),                        -- D9 BaseEntity
  CONSTRAINT ck_ptn_date_order CHECK (until IS NULL OR since IS NULL OR until >= since),
  CONSTRAINT uq_ptn_partner_school UNIQUE (partner_id, school_id)     -- 1 quan hệ / cặp
);

-- code_sequences cho D5 (client không gửi id) — khuôn fallback, không ép các mã đã tồn tại
INSERT INTO code_sequences (scope, next_value) VALUES ('partners', 1)
  ON CONFLICT (scope) DO NOTHING;
INSERT INTO code_sequences (scope, next_value) VALUES ('partner_documents', 1)
  ON CONFLICT (scope) DO NOTHING;
INSERT INTO code_sequences (scope, next_value) VALUES ('delivery_persons', 1)
  ON CONFLICT (scope) DO NOTHING;
INSERT INTO code_sequences (scope, next_value) VALUES ('partnership_applications', 1)
  ON CONFLICT (scope) DO NOTHING;
INSERT INTO code_sequences (scope, next_value) VALUES ('supplier_partnerships', 1)
  ON CONFLICT (scope) DO NOTHING;
