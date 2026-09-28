-- ============ V30__mf13_report_views.sql — 7 view read-only (ADR-M13-1) ============
-- BR-182: mọi số liệu tính TRỰC TIẾP từ bảng nguồn. BR-181: view KHÔNG chọn cột PII học sinh.
--
-- M13-D05 (2026-08-04) — đối chiếu trực tiếp với DDL đã merge: V1 (user_accounts, roles),
-- V6 (partners, partner_documents, supplier_partnerships), V10 (receiving_sessions,
-- receiving_lines, inventory_batches), V12 (inspection_sessions, kitchen_inspections),
-- V14 (food_samples), V8 (dishes).

-- slug three-step (MF07) — bảng thật là inspection_sessions, KHÔNG phải "inspections"
CREATE OR REPLACE VIEW v_rpt_three_step AS
SELECT i.school_id,
       i.code            AS id,
       i.date,
       i.meal_type,
       i.step1, i.step2, i.step3,
       i.core_temp, i.holding_temp,
       i.overall,
       u.name            AS inspector,
       d.name            AS dish
FROM   inspection_sessions i
JOIN   user_accounts u ON u.id = i.inspector_user_id
LEFT   JOIN dishes  d  ON d.id = i.dish_id;

-- slug sample-retention (MF08)
CREATE OR REPLACE VIEW v_rpt_sample_retention AS
SELECT s.school_id,
       s.code              AS id,
       s.dish_name         AS dish,
       s.meal_type, s.meal_shift,
       s.sample_amount, s.sample_unit,
       s.retention_deadline, s.fridge_temp, s.status, s.collected_at
FROM   food_samples s;

-- slug receiving (MF06)
CREATE OR REPLACE VIEW v_rpt_receiving AS
SELECT r.school_id,
       r.code                     AS id,
       p.name                     AS supplier,
       r.received_at::date        AS delivery_date,
       COUNT(l.id)                AS items,
       r.gps_verified, r.photos_count, r.status
FROM   receiving_sessions r
JOIN   partners p        ON p.id = r.supplier_id
LEFT   JOIN receiving_lines l ON l.session_id = r.id
GROUP  BY r.school_id, r.code, p.name, r.received_at,
          r.gps_verified, r.photos_count, r.status;

-- slug supplier-certificates (MF04) — M13-D13
-- partners KHÔNG có school_id (bảng cấp nền tảng). Phạm vi trường đến từ supplier_partnerships;
-- bỏ nó đi là BR-176 mất chỗ thi hành.
CREATE OR REPLACE VIEW v_rpt_supplier_cert AS
SELECT sp.school_id,                                     -- KHÔNG phải p.school_id
       p.code           AS id,
       p.name,
       p.partner_type,
       doc.expiry_date  AS cert_expiry,
       CASE
         WHEN doc.expiry_date IS NULL                              THEN 'MISSING'
         WHEN doc.expiry_date <  CURRENT_DATE                      THEN 'EXPIRED'
         WHEN doc.expiry_date <= CURRENT_DATE + INTERVAL '30 days' THEN 'EXPIRING_SOON'
         ELSE 'VALID'
       END              AS cert_status,
       p.status
FROM   partners p
JOIN   supplier_partnerships sp
         ON sp.partner_id = p.id AND sp.status = 'APPROVED'
LEFT   JOIN LATERAL (
         SELECT d.expiry_date
         FROM   partner_documents d
         WHERE  d.partner_id = p.id
           AND  d.document_type = 'FOOD_SAFETY_CERT'
           AND  d.superseded_by_document_id IS NULL
         ORDER  BY d.expiry_date DESC NULLS LAST
         LIMIT  1
       ) doc ON TRUE;

-- slug hygiene (MF07 UC-014)
CREATE OR REPLACE VIEW v_rpt_hygiene AS
SELECT k.school_id,
       k.code   AS id,
       k.date, k.score, k.staff_health_valid, k.pest_control_valid,
       u.name   AS inspector,
       k.result
FROM   kitchen_inspections k
JOIN   user_accounts u ON u.id = k.inspector_user_id;

-- slug traceability (MF06, BR-151)
CREATE OR REPLACE VIEW v_rpt_traceability AS
SELECT b.school_id,
       b.code               AS id,
       b.ingredient_label   AS ingredient,
       p.name               AS supplier,
       b.received_date, b.expiry_date, b.storage_zone,
       CASE
         WHEN b.quarantine_at IS NOT NULL   THEN 'QUARANTINED'
         WHEN b.expiry_date < CURRENT_DATE  THEN 'EXPIRED'
         ELSE 'ACTIVE'
       END                  AS status
FROM   inventory_batches b
JOIN   partners p ON p.id = b.supplier_id;

-- slug system-usage (MF01, BR-176) — nhân sự, KHÔNG phải HS → hợp BR-181
CREATE OR REPLACE VIEW v_rpt_system_usage AS
SELECT u.school_id,
       u.code       AS id,
       u.name,
       r.role_key   AS role,
       u.status,
       u.last_login
FROM   user_accounts u
JOIN   roles r ON r.id = u.role_id;

-- Bộ đếm mã (cùng khuôn V21 M11-D01/D02) — 'RSC-2026-001' seed ở trên không qua CodeGenerator,
-- đăng ký trước để lần sinh mã đầu tiên qua API không cấp trùng 'RSC-2026-001'.
INSERT INTO code_sequences (scope, next_value) VALUES ('report_schedules_2026', 2)
ON CONFLICT (scope) DO UPDATE SET next_value = GREATEST(code_sequences.next_value, EXCLUDED.next_value);
