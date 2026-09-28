-- V20__mf11_incident.sql — MF11 Incident & Emergency Management (+ Violation Management)
-- Nguồn: TDS-MF11_Backend.md v1.2 §5.2 (M11-D01..D13 — Bước 0 + Bước 1, 2026-08-04)
--   · PK UUID + code VARCHAR(24) + UNIQUE(school_id, code)                       (D1)
--   · 4 cột audit created_at/updated_at/created_by/updated_by (D9 — BaseEntity điền, Entity KHÔNG khai lại)
--   · KHÔNG tạo bảng code_sequences riêng — bảng đã tồn tại ở V1:31, dùng scope "mf11_*:<năm>" (M11-D01/D02)
--   · FK inspection_sessions(id)/partners(id) đều UUID, V12/V6 đã merge trước V20 (M11-D09/D10)
--   · incident_affected_students/incident_suspected_dishes/incident_quarantine_batches: KHÔNG FK
--     tới students/dishes/inventory_batches — có chủ ý (M11-D11), hồ sơ pháp lý giữ 10 năm

-- ── 13 bảng nghiệp vụ MF11 ──────────────────────────────────────────────────────

CREATE TABLE incident_source_reports (
  id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code                   VARCHAR(24) NOT NULL,            -- RPT-2026-033
  school_id              UUID NOT NULL REFERENCES schools(id),
  reported_by_id         UUID NOT NULL,                   -- -> user_accounts(id), MF01 (PK UUID) — không khai FK, M11-D06
  reported_by_label      VARCHAR(120) NOT NULL,
  reported_at            TIMESTAMPTZ NOT NULL,
  class_name             VARCHAR(20),
  -- symptoms: JSONB thay vì TEXT[] (khác TDS §5.2 nguyên văn) — dự án chưa có tiền lệ map
  -- Postgres native array sang List<String> (mọi list-of-string khác dùng JSONB, VD
  -- SchoolConfig.reportEmails/RoleRecord.permissions), giữ nhất quán @JdbcTypeCode(SqlTypes.JSON).
  symptoms               JSONB NOT NULL DEFAULT '[]',
  suspected_meal_text    VARCHAR(300),                    -- THÔ, không binding (BR-151)
  meal_date              DATE,
  description            TEXT NOT NULL,
  evidence_photos        INTEGER NOT NULL DEFAULT 0,
  severity_level         VARCHAR(8) CHECK (severity_level IN ('LOW','MEDIUM','HIGH')),
  status                 VARCHAR(28) NOT NULL DEFAULT 'PENDING_TRIAGE'
                         CHECK (status IN ('PENDING_TRIAGE','DRAFTING','DRAFTED',
                                           'MERGED','CLOSED_NOT_FOOD_SAFETY')),
  merged_into_incident_id UUID,
  draft_record_id        UUID,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),          -- D9
  updated_at  TIMESTAMPTZ,
  created_by  VARCHAR(180),
  updated_by  VARCHAR(180),
  CONSTRAINT uq_rpt_code UNIQUE (school_id, code),
  CONSTRAINT ck_rpt_merged CHECK (
    status <> 'MERGED' OR merged_into_incident_id IS NOT NULL),
  CONSTRAINT ck_rpt_after_meal CHECK (
    meal_date IS NULL OR reported_at >= meal_date::timestamptz)
);
CREATE INDEX ix_rpt_status ON incident_source_reports(school_id, status, reported_at DESC);

CREATE TABLE incident_records (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code          VARCHAR(24) NOT NULL,                     -- REC-2026-021
  school_id     UUID NOT NULL REFERENCES schools(id),
  content       TEXT NOT NULL,                            -- BR-154
  status        VARCHAR(28) NOT NULL DEFAULT 'DRAFT'
                CHECK (status IN ('DRAFT','COMPLETED','OPENED','CLOSED_NOT_FOOD_SAFETY')),
  created_by_id UUID NOT NULL,
  incident_id   UUID,
  location      VARCHAR(200),
  occurred_at   TIMESTAMPTZ,
  pdf_exported  BOOLEAN NOT NULL DEFAULT FALSE,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),        -- D9
  updated_at    TIMESTAMPTZ,
  created_by    VARCHAR(180),
  updated_by    VARCHAR(180),
  CONSTRAINT uq_rec_code UNIQUE (school_id, code),
  CONSTRAINT ck_rec_opened CHECK (status <> 'OPENED' OR incident_id IS NOT NULL)
);

-- N-1: nhiều RPT gộp vào 1 biên bản (ADR-M11-1)
CREATE TABLE incident_record_sources (
  record_id UUID NOT NULL REFERENCES incident_records(id) ON DELETE CASCADE,
  report_id UUID NOT NULL REFERENCES incident_source_reports(id) ON DELETE RESTRICT,
  PRIMARY KEY (record_id, report_id)
);

CREATE TABLE incidents (
  id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code                    VARCHAR(24) NOT NULL,           -- INC-2026-014
  school_id               UUID NOT NULL REFERENCES schools(id),
  incident_record_id      UUID NOT NULL REFERENCES incident_records(id),   -- ADR-M11-1
  title                   VARCHAR(200) NOT NULL,
  reported_at             TIMESTAMPTZ NOT NULL,
  occurred_at             TIMESTAMPTZ,                    -- mốc #1 cửa sổ 24h (BR-149)
  reported_by_label       VARCHAR(200) NOT NULL,
  severity                VARCHAR(8) CHECK (severity IN ('LOW','MEDIUM','HIGH')),  -- NULL = chưa phân loại
  status                  VARCHAR(20) NOT NULL DEFAULT 'SEVERITY_PENDING'
                          CHECK (status IN ('SEVERITY_PENDING','INVESTIGATING','CAPA_PENDING',
                                            'CAPA_APPROVED','VALIDATED','CLOSED')),
  symptoms                JSONB NOT NULL DEFAULT '[]',    -- JSONB, xem ghi chú ở incident_source_reports
  suspected_meal_text     VARCHAR(300),
  meal_date               DATE,
  meal_time_label         VARCHAR(60),
  first_aid_notes         TEXT,
  clinical_risk_assessment TEXT,                          -- UC-009
  investigation_lab_result TEXT,
  investigation_lab_result_files INTEGER NOT NULL DEFAULT 0,
  root_cause              TEXT,                           -- BR-150
  root_cause_status       VARCHAR(12) CHECK (root_cause_status IN ('PRELIMINARY','FINAL')),
  evidence_final          BOOLEAN NOT NULL DEFAULT FALSE,
  measures_draft          TEXT,
  official_clinical_notes TEXT,
  traceability_map_status VARCHAR(12) NOT NULL DEFAULT 'COMPILING'
                          CHECK (traceability_map_status IN ('COMPILING','READY')),  -- BR-151
  report_exported         BOOLEAN NOT NULL DEFAULT FALSE,                 -- ADR-M11-8
  closed_at               TIMESTAMPTZ,
  closed_by_id            UUID,
  closing_statement       TEXT,
  created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at              TIMESTAMPTZ,
  created_by              VARCHAR(180),
  updated_by              VARCHAR(180),
  CONSTRAINT uq_inc_code   UNIQUE (school_id, code),
  CONSTRAINT uq_inc_record UNIQUE (incident_record_id),      -- 1 biên bản = tối đa 1 sự cố
  CONSTRAINT ck_inc_severity_set CHECK (
    status = 'SEVERITY_PENDING' OR severity IS NOT NULL),
  CONSTRAINT ck_inc_closed CHECK (
    status <> 'CLOSED' OR (closed_at IS NOT NULL AND closed_by_id IS NOT NULL
                           AND closing_statement IS NOT NULL)),
  CONSTRAINT ck_inc_after_meal CHECK (
    meal_date IS NULL OR reported_at >= meal_date::timestamptz)
);
CREATE INDEX ix_inc_school_status ON incidents(school_id, status);
CREATE INDEX ix_inc_reported_at   ON incidents(reported_at DESC);
CREATE INDEX ix_inc_severity      ON incidents(school_id, severity);

CREATE TABLE incident_affected_students (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  incident_id  UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
  student_id   UUID NOT NULL,                             -- -> students(id), MF03 (PK UUID) — không FK, M11-D11
  student_name VARCHAR(120) NOT NULL,                     -- snapshot (ADR-M11-7)
  class_name   VARCHAR(20)  NOT NULL,
  recovered    BOOLEAN NOT NULL DEFAULT FALSE,            -- gate BR-155
  recovered_at TIMESTAMPTZ,
  CONSTRAINT uq_ias_inc_student UNIQUE (incident_id, student_id),
  CONSTRAINT ck_ias_recovered CHECK (recovered = FALSE OR recovered_at IS NOT NULL)
);
CREATE INDEX ix_ias_student ON incident_affected_students(student_id);
CREATE INDEX ix_ias_class   ON incident_affected_students(class_name);

CREATE TABLE incident_suspected_dishes (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
  dish_id     UUID NOT NULL,                              -- -> dishes(id), MF05 — KHOÁ, không phải tên
  dish_name   VARCHAR(180) NOT NULL,                      -- snapshot
  CONSTRAINT uq_isd_inc_dish UNIQUE (incident_id, dish_id)
);

CREATE TABLE incident_quarantine_batches (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  incident_id       UUID NOT NULL REFERENCES incidents(id) ON DELETE RESTRICT,
  batch_id          UUID NOT NULL,                        -- -> inventory_batches(id), MF06
  batch_code        VARCHAR(40) NOT NULL,
  reason            VARCHAR(300) NOT NULL,                -- BR-149 sửa 27/07: BẮT BUỘC
  quarantined_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  quarantined_by_id UUID NOT NULL,
  CONSTRAINT uq_iqb_inc_batch UNIQUE (incident_id, batch_id),
  CONSTRAINT ck_iqb_reason CHECK (length(btrim(reason)) > 0)
);
CREATE INDEX ix_iqb_batch ON incident_quarantine_batches(batch_id);
-- MỘT CHIỀU (BR-217, ADR-M11-3): không có cột released_at, chặn DELETE
CREATE RULE iqb_no_delete AS ON DELETE TO incident_quarantine_batches DO INSTEAD NOTHING;

-- Log nhả lô LỊCH SỬ (19–27/07/2026) — read-only, không ghi mới (CF-M11-2)
CREATE TABLE incident_quarantine_release_log (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  incident_id UUID NOT NULL REFERENCES incidents(id),
  batch_codes JSONB NOT NULL,                             -- JSONB, không TEXT[] — xem ghi chú trên
  dish_label  VARCHAR(180),
  reason      VARCHAR(300) NOT NULL,
  released_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE incident_evidence (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE RESTRICT,
  type        VARCHAR(60)  NOT NULL,
  description VARCHAR(500) NOT NULL,
  file_count  INTEGER NOT NULL DEFAULT 0,
  captured_at TIMESTAMPTZ NOT NULL DEFAULT now()          -- BR-150
);
CREATE INDEX ix_iev_evidence_inc ON incident_evidence(incident_id);

CREATE TABLE incident_events (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code        VARCHAR(24) NOT NULL,                       -- IEV-014-01
  incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE RESTRICT,
  at          TIMESTAMPTZ NOT NULL,
  type        VARCHAR(20) NOT NULL CHECK (type IN ('REPORTED','SEVERITY_SET','QUARANTINE',
                'RELEASE','SAMPLE_SENT','TRACE_READY','CAPA_SUBMITTED','CAPA_APPROVED',
                'CAPA_EVIDENCE','CAPA_VALIDATED','REPORT_SENT','CLOSED')),
  description VARCHAR(500) NOT NULL,
  actor_label VARCHAR(120) NOT NULL
);
CREATE INDEX ix_iev_incident_at ON incident_events(incident_id, at);
-- APPEND-ONLY (ADR-M11-2): chặn ở tầng DB, không trông vào kỷ luật code
CREATE RULE incident_events_no_update AS ON UPDATE TO incident_events DO INSTEAD NOTHING;
CREATE RULE incident_events_no_delete AS ON DELETE TO incident_events DO INSTEAD NOTHING;

CREATE TABLE corrective_actions (
  id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code                      VARCHAR(24) NOT NULL,          -- CAPA-2026-033
  school_id                 UUID NOT NULL REFERENCES schools(id),
  source_label              VARCHAR(200) NOT NULL,         -- nhãn hiển thị, KHÔNG là khoá
  incident_id               UUID REFERENCES incidents(id),
  inspection_id             UUID REFERENCES inspection_sessions(id),        -- MF07, BR-095 (M11-D09)
  description               TEXT NOT NULL,
  assignee_label            VARCHAR(120) NOT NULL,
  assignee_supplier_id      UUID REFERENCES partners(id),                   -- MF04 (M11-D10)
  status                    VARCHAR(30) NOT NULL DEFAULT 'DRAFT'
                            CHECK (status IN ('DRAFT','PENDING_ADMIN_APPROVAL',
                              'APPROVED_FOR_IMPLEMENTATION','IN_PROGRESS',
                              'SUBMITTED_EVIDENCE','COMPLETED')),               -- BR-216
  submitted_for_approval_at TIMESTAMPTZ,
  approved_at               TIMESTAMPTZ,
  evidence_submitted_at     TIMESTAMPTZ,
  reject_reason             VARCHAR(500),
  evidence_note             TEXT,
  evidence_photos           INTEGER NOT NULL DEFAULT 0,
  rework_reason             VARCHAR(500),
  rework_requested_at       TIMESTAMPTZ,
  deadline                  VARCHAR(40),   -- deprecated BR-216 (bỏ SLA 12h) — chỉ đọc, CF-M11-21(d)
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),          -- D9
  updated_at  TIMESTAMPTZ,
  created_by  VARCHAR(180),
  updated_by  VARCHAR(180),
  CONSTRAINT uq_capa_code UNIQUE (school_id, code),
  CONSTRAINT ck_capa_source CHECK (
    (incident_id IS NOT NULL) <> (inspection_id IS NOT NULL)),
  CONSTRAINT ck_capa_approved CHECK (
    approved_at IS NULL OR (submitted_for_approval_at IS NOT NULL
                            AND approved_at >= submitted_for_approval_at)),
  CONSTRAINT ck_capa_reject CHECK (
    status <> 'DRAFT' OR reject_reason IS NULL OR length(btrim(reject_reason)) > 0),
  CONSTRAINT ck_capa_evidence CHECK (
    status NOT IN ('SUBMITTED_EVIDENCE','COMPLETED')
    OR (evidence_submitted_at IS NOT NULL AND evidence_photos > 0))
);
-- BR-216: 1 Incident = 1 CAPA (partial unique — CAPA từ kiểm thực không dính)
CREATE UNIQUE INDEX uq_capa_one_per_incident
  ON corrective_actions(incident_id) WHERE incident_id IS NOT NULL;
CREATE INDEX ix_capa_status   ON corrective_actions(school_id, status);
CREATE INDEX ix_capa_assignee ON corrective_actions(assignee_supplier_id);

CREATE TABLE violation_records (
  id                            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code                          VARCHAR(24) NOT NULL,      -- VIO-2026-008
  school_id                     UUID NOT NULL REFERENCES schools(id),
  type                          VARCHAR(20) NOT NULL DEFAULT 'Quy trình',
  subject                       VARCHAR(200) NOT NULL,
  recorded_by_id                UUID NOT NULL,
  recorded_by_label             VARCHAR(120) NOT NULL,
  date                          DATE NOT NULL,
  severity                      VARCHAR(8) NOT NULL CHECK (severity IN ('LOW','MEDIUM','HIGH')),
  resolution                    VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                                CHECK (resolution IN ('PENDING','PENDING_APPROVAL','APPROVED','REJECTED')),
  status                        VARCHAR(8) NOT NULL DEFAULT 'OPEN'
                                CHECK (status IN ('OPEN','CLOSED')),
  violation_type                VARCHAR(24) CHECK (violation_type IN ('HYGIENE_VIOLATION',
                                  'PROCEDURE_SKIP','OVERRIDE_ABUSE','UNAUTHORIZED_ACTION','OTHER')),
  description                   TEXT NOT NULL,
  occurred_at                   TIMESTAMPTZ NOT NULL,                       -- BR-211
  location_of_incident          VARCHAR(200) NOT NULL,                      -- BR-211
  caterers_representative_id    UUID NOT NULL,                              -- BR-183/187
  caterers_representative_label VARCHAR(160) NOT NULL,
  witness_label                 VARCHAR(120),
  self_reported                 BOOLEAN NOT NULL DEFAULT FALSE,             -- BR-189
  repeat_suspect                BOOLEAN NOT NULL DEFAULT FALSE,             -- BR-187 (MSG-088)
  related_incident_id           UUID REFERENCES incidents(id),
  source_inspection_id          UUID REFERENCES inspection_sessions(id),    -- MF07 (M11-D09)
  pdf_exported                  BOOLEAN NOT NULL DEFAULT FALSE,             -- BR-211
  pdf_generated_at              TIMESTAMPTZ,
  pdf_version                   INTEGER NOT NULL DEFAULT 0,
  action_taken                  TEXT,                                       -- BR-185
  unit_sanction_level           VARCHAR(30) CHECK (unit_sanction_level IN ('WRITTEN_REMINDER',
                                  'CONTRACT_WARNING','CONTRACT_PENALTY','TERMINATION_PROPOSAL')),
  handled_by_id                 UUID,
  reject_reason                 VARCHAR(500),
  -- deprecated 26/07/2026 (CF-M11-4): giữ dữ liệu cũ, KHÔNG ghi mới
  staff_name                    VARCHAR(120),
  staff_position                VARCHAR(60),
  disciplinary_level            VARCHAR(40),
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),          -- D9
  updated_at  TIMESTAMPTZ,
  created_by  VARCHAR(180),
  updated_by  VARCHAR(180),
  CONSTRAINT uq_vio_code UNIQUE (school_id, code),
  CONSTRAINT ck_vio_resolution CHECK (
    resolution = 'PENDING'
    OR (action_taken IS NOT NULL AND handled_by_id IS NOT NULL
        AND unit_sanction_level IS NOT NULL)),
  CONSTRAINT ck_vio_closed CHECK (status <> 'CLOSED' OR resolution IN ('APPROVED','REJECTED')),
  CONSTRAINT ck_vio_reject CHECK (resolution <> 'REJECTED' OR length(btrim(coalesce(reject_reason,''))) > 0)
);
CREATE INDEX ix_vio_school_date  ON violation_records(school_id, date DESC);
CREATE INDEX ix_vio_type_date    ON violation_records(violation_type, date DESC);   -- BR-190
CREATE INDEX ix_vio_rep_severity ON violation_records(caterers_representative_id, severity, date DESC); -- BR-187
CREATE INDEX ix_vio_incident     ON violation_records(related_incident_id);

CREATE TABLE violation_files (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  -- D1 — path param DELETE /violations/{code}/signed-record/{fileId} phải dùng mã nghiệp vụ,
  -- không phải UUID nội bộ (chỗ này TDS §9.2 không nêu rõ, bổ sung khi hiện thực Bước 2).
  code         VARCHAR(24) NOT NULL,
  violation_id UUID NOT NULL REFERENCES violation_records(id) ON DELETE RESTRICT,
  kind         VARCHAR(16) NOT NULL CHECK (kind IN ('EVIDENCE','SIGNED_RECORD')),
  name         VARCHAR(200) NOT NULL,
  storage_key  VARCHAR(400) NOT NULL,
  size_label   VARCHAR(20),
  uploaded_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_vfile_code UNIQUE (violation_id, code)
);
CREATE INDEX ix_vfile_violation ON violation_files(violation_id, kind);

-- ── Bất biến DB (không chỉ ở service, xem TDS §5.2):
--    uq_inc_record + incident_record_id NOT NULL (ADR-M11-1) · ck_inc_severity_set (BR-147)
--    ck_inc_closed (BR-155) · ck_iqb_reason + RULE iqb_no_delete (BR-149/BR-217)
--    RULE append-only trên incident_events (ADR-M11-2)
--    uq_capa_one_per_incident + ck_capa_source + ck_capa_approved (BR-216/BR-153)
--    ck_vio_resolution (BR-185)
-- KHÔNG có bảng override_abuse / bản đồ truy xuất (dẫn xuất — ADR-M11-6, CF-M11-10/13).
-- KHÔNG có cột released_at/released_by trên lô cách ly (BR-217 — CF-M11-2).

-- ── ALTER inventory_batches (MF06, V10 đã merge) — gate đóng sự cố BR-217/C7 cần biết
--    lô cách ly đã "xử lý xong" (trả NCC / huỷ bỏ) hay chưa. MF06 KHÔNG có cột này (V10
--    chỉ theo dõi quarantine_at/quarantine_reason/quarantine_incident_id, không có trạng
--    thái cuối). Theo Bước 0 §2.1: "bảng đã tồn tại mà thiếu cột ⇒ ALTER TABLE". Nullable,
--    KHÔNG đổi hành vi MF06 hiện có.
ALTER TABLE inventory_batches
  ADD COLUMN quarantine_disposition VARCHAR(24)
    CHECK (quarantine_disposition IN ('RETURNED_TO_SUPPLIER','DISPOSED'));
ALTER TABLE inventory_batches
  ADD COLUMN quarantine_disposition_at TIMESTAMPTZ;
