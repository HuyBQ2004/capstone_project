-- ═══════════════════════════════════════════════════════════════════════════════
-- V48__mf04_partnership_catalog_proposed.sql — Danh mục sản phẩm & giá theo hợp đồng
-- partner × school (PROPOSED — chưa duyệt chính thức).
--
-- Nguồn: 07_implement/MF04_Supplier_Partner_Management/Proposal-Partnership-Catalog-
-- Contract.md — KHÔNG phải TDS-MF04_Backend.md đã Approved. Không có BR/UC chính thức
-- đứng sau 2 bảng này; chờ BA/PM cấp mã. Hậu tố `_proposed` để phân biệt với migration
-- đã qua review chuẩn — đổi tên khi đề xuất được duyệt.
--
-- 00_Common_Conventions.md: D1 (code = mã nghiệp vụ), D4 (TIMESTAMPTZ/DATE),
-- D6 (enum SCREAMING_SNAKE), D9 (created_by/updated_by = BaseEntity),
-- CF-M04-6 (partner_id/school_id trực tiếp trên bảng, không bắt buộc join để lọc tenant).
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE partnership_catalog_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(32) NOT NULL UNIQUE,                                     -- D1
  partnership_id UUID NOT NULL REFERENCES supplier_partnerships(id),
  partner_id UUID NOT NULL REFERENCES partners(id),                     -- CF-M04-6 (denormalized)
  school_id  UUID NOT NULL REFERENCES schools(id),                      -- CF-M04-6 (denormalized)
  ingredient_id UUID NOT NULL,
  unit VARCHAR(20) NOT NULL,
  approved_unit_price NUMERIC(14,2) NOT NULL,
  packaging VARCHAR(120),
  effective_from DATE NOT NULL,
  effective_to DATE,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
    CHECK (status IN ('ACTIVE','INACTIVE')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ,     -- D9 BaseEntity
  created_by VARCHAR(180), updated_by VARCHAR(180),                          -- D9 BaseEntity
  CONSTRAINT ck_pci_date_order CHECK (effective_to IS NULL OR effective_to >= effective_from)
);
CREATE INDEX ix_pci_partnership ON partnership_catalog_items(partnership_id, status);
CREATE INDEX ix_pci_partner     ON partnership_catalog_items(partner_id);
CREATE INDEX ix_pci_school      ON partnership_catalog_items(school_id);

CREATE TABLE catalog_change_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(32) NOT NULL UNIQUE,                                     -- D1
  partnership_id UUID NOT NULL REFERENCES supplier_partnerships(id),
  partner_id UUID NOT NULL REFERENCES partners(id),                     -- CF-M04-6 (denormalized)
  school_id  UUID NOT NULL REFERENCES schools(id),                      -- CF-M04-6 (denormalized)
  catalog_item_id UUID REFERENCES partnership_catalog_items(id),        -- NULL = đề xuất sản phẩm mới
  ingredient_id UUID NOT NULL,
  requested_unit VARCHAR(20) NOT NULL,
  requested_unit_price NUMERIC(14,2) NOT NULL,
  requested_packaging VARCHAR(120),
  reason VARCHAR(500) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING'
    CHECK (status IN ('PENDING','APPROVED','REJECTED')),
  decided_by_user_id UUID,
  decided_at TIMESTAMPTZ,
  decision_note VARCHAR(500),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ,     -- D9 BaseEntity
  created_by VARCHAR(180), updated_by VARCHAR(180),                          -- D9 BaseEntity
  CONSTRAINT ck_ccr_decision CHECK (
    (status = 'PENDING'  AND decided_by_user_id IS NULL AND decided_at IS NULL) OR
    (status <> 'PENDING' AND decided_by_user_id IS NOT NULL AND decided_at IS NOT NULL)
  ),
  CONSTRAINT ck_ccr_reject_note CHECK (status <> 'REJECTED' OR decision_note IS NOT NULL)
);
CREATE INDEX ix_ccr_partnership ON catalog_change_requests(partnership_id, status);
CREATE INDEX ix_ccr_partner     ON catalog_change_requests(partner_id, status);
CREATE INDEX ix_ccr_school      ON catalog_change_requests(school_id, status);

INSERT INTO code_sequences (scope, next_value) VALUES ('partnership_catalog_items', 1)
  ON CONFLICT (scope) DO NOTHING;
INSERT INTO code_sequences (scope, next_value) VALUES ('catalog_change_requests', 1)
  ON CONFLICT (scope) DO NOTHING;
