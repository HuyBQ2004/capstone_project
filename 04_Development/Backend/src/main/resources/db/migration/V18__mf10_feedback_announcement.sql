-- V18__mf10_feedback_announcement.sql (Flyway) — M10-D05. KHÔNG sửa V1__init_schema.sql.
-- TDS-MF10_Backend.md §5.2. M10-D01: cột `code` + UNIQUE lộ qua path param (D1).
-- M10-D02: 4 cột audit của D9 (DDL tạo, BaseEntity điền, Entity KHÔNG khai lại).
-- M10-D04: mọi FK người dùng trỏ user_accounts(id) — bảng `users` CHƯA BAO GIỜ tồn tại.
--
-- ⚠️ Khác biệt có chủ đích so với TDS §5.2 (ghi rõ ở Bước 2 để không lặp lại điểm mù):
--  · KHÔNG bật extension pg_trgm/unaccent (chưa module nào khác dùng, tránh thêm hạ tầng
--    riêng cho MF10) — index search dùng B-Tree thường qua ILIKE (SpecSupport, giống MF06/07/09),
--    và so khớp từ khóa bệnh lý "không dấu" thực hiện ở TẦNG JAVA (java.text.Normalizer),
--    KHÔNG ở DB. Đây là simplification, ghi vào TDS §2.5 khi có ai cần trigram thật.

CREATE TABLE feedbacks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(32) NOT NULL,                                 -- D1/M10-D01: 'FB-2026-8809', JSON trả dưới tên "id"
  school_id UUID NOT NULL REFERENCES schools(id),
  submitter_user_id UUID NOT NULL REFERENCES user_accounts(id),   -- M10-D04 (C2)
  submitter_role VARCHAR(10) NOT NULL,                       -- PARENT | TEACHER
  scope VARCHAR(10) NOT NULL,                                -- DISH | SESSION | CLASS
  student_id UUID REFERENCES students(id),                   -- NULL khi scope = CLASS
  classroom_id UUID REFERENCES classrooms(id),
  class_name VARCHAR(20) NOT NULL,
  meal_date DATE NOT NULL,
  meal_type VARCHAR(12) NOT NULL                             -- M10-D09: common/enums/MealType
    CHECK (meal_type IN ('BREAKFAST','LUNCH','SNACK')),
  -- MF09 đã merge (V16) ⇒ FK CỨNG được, không còn là "ID lỏng liên module" như bản v1.0 ghi.
  meal_session_id UUID NOT NULL REFERENCES student_distributions(id) ON DELETE RESTRICT,
  dish_id UUID REFERENCES dishes(id),                        -- MF05 đã merge (V8) ⇒ FK cứng
  dish_name VARCHAR(160),
  rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
  tags JSONB NOT NULL DEFAULT '[]',
  comment TEXT,
  portion_size VARCHAR(12),                                  -- UC-086
  illness_keyword BOOLEAN NOT NULL DEFAULT FALSE,            -- BR-142 — server tính lại (L9/M10-D14)
  submitted_at TIMESTAMPTZ NOT NULL DEFAULT now(),           -- mốc NGHIỆP VỤ BR-139, khác created_at
  deleted_at TIMESTAMPTZ,
  -- D9 / M10-D02: 4 cột audit
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(180),
  updated_by VARCHAR(180),
  CONSTRAINT uq_fb_school_code UNIQUE (school_id, code),     -- D1 / M10-D01
  -- BR-140: dưới 3 sao BẮT BUỘC có tag quan tâm chất lượng (chốt chặn ở DB, không chỉ ở service)
  CONSTRAINT ck_fb_low_rating_needs_tag
    CHECK (rating >= 3 OR submitter_role <> 'PARENT' OR jsonb_array_length(tags) > 0),
  -- Một người chỉ phản hồi MỘT lần cho một (phiên ăn × món); sửa dùng PATCH (UC-046 AF1)
  CONSTRAINT uq_fb_once UNIQUE (submitter_user_id, meal_session_id, dish_id, student_id)
);
CREATE INDEX ix_fb_school_date ON feedbacks(school_id, meal_date DESC);
CREATE INDEX ix_fb_submitter ON feedbacks(submitter_user_id);
CREATE INDEX ix_fb_illness ON feedbacks(school_id, illness_keyword) WHERE illness_keyword;
CREATE INDEX ix_fb_classroom ON feedbacks(classroom_id);

CREATE TABLE feedback_responses (                            -- BR-143 / UC-163 (MỚI — CF-M10-4)
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(32) NOT NULL,                                 -- D1/M10-D01: 'FBR-2026-001'
  school_id UUID NOT NULL REFERENCES schools(id),
  feedback_id UUID NOT NULL REFERENCES feedbacks(id),
  responder_user_id UUID NOT NULL REFERENCES user_accounts(id),   -- M10-D04 (C2)
  content TEXT NOT NULL CHECK (length(btrim(content)) > 0), -- UC-163 EX1: không nhận nội dung rỗng
  responded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  late_response BOOLEAN NOT NULL DEFAULT FALSE,             -- UC-163 EX2: >48h chỉ GHI NHẬN, không chặn
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),            -- D9 / M10-D02
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(180),
  updated_by VARCHAR(180),
  CONSTRAINT uq_fbr_school_code UNIQUE (school_id, code)
);
CREATE INDEX ix_fbr_feedback ON feedback_responses(feedback_id);

-- M10-D11 (D10/D10-BIS, L8/F2) — ảnh đính kèm phản ánh. RIÊNG TƯ: lưu storage_key, JSON trả `code`,
-- KHÔNG trả URL công khai, KHÔNG presign. Tải xuống qua endpoint có @PreAuthorize + audit.
CREATE TABLE feedback_attachments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(32) NOT NULL,                                 -- 'FBATT-2026-001'
  school_id UUID NOT NULL REFERENCES schools(id),
  feedback_id UUID NOT NULL REFERENCES feedbacks(id) ON DELETE CASCADE,
  storage_key VARCHAR(255) NOT NULL,                         -- do common/storage/StorageService cấp
  original_name VARCHAR(255) NOT NULL,
  content_type VARCHAR(100) NOT NULL                         -- đã kiểm MAGIC BYTES, không tin client
    CHECK (content_type IN ('image/jpeg','image/png','image/webp','application/pdf')),
  size_bytes BIGINT NOT NULL CHECK (size_bytes > 0 AND size_bytes <= 5242880),  -- ≤ 5 MB
  uploaded_by_user_id UUID NOT NULL REFERENCES user_accounts(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),            -- D9 / M10-D02
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(180),
  updated_by VARCHAR(180),
  CONSTRAINT uq_fbatt_school_code UNIQUE (school_id, code)
);
CREATE INDEX ix_fbatt_feedback ON feedback_attachments(feedback_id);

CREATE TABLE illness_keywords (                              -- BR-142 (ADR-M10-6). Bảng cấu hình ⇒ không `code`.
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  keyword VARCHAR(60) NOT NULL UNIQUE,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),            -- D9 / M10-D02
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(180),
  updated_by VARCHAR(180)
);

CREATE TABLE announcements (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(32) NOT NULL,                                 -- D1/M10-D01: 'ANN-2026-113'
  school_id UUID NOT NULL REFERENCES schools(id),
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  audience_codes JSONB NOT NULL DEFAULT '[]',                -- CF-M10-7 — thứ LỌC được
  audience_label VARCHAR(255) NOT NULL,                      -- chuỗi HIỂN THỊ, dẫn xuất (M10-D14)
  status VARCHAR(12) NOT NULL DEFAULT 'DRAFT'                -- DRAFT | PUBLISHED
    CHECK (status IN ('DRAFT','PUBLISHED')),
  urgent BOOLEAN NOT NULL DEFAULT FALSE,                     -- BR-138
  author_user_id UUID NOT NULL REFERENCES user_accounts(id), -- M10-D04 (C2); D9/L16: ≠ created_by
  author_name VARCHAR(180) NOT NULL,
  published_at TIMESTAMPTZ,                                  -- M10-D10: NULL khi DRAFT, KHÔNG phải ''
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),             -- D9 / M10-D02
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(180),
  updated_by VARCHAR(180),
  CONSTRAINT uq_ann_school_code UNIQUE (school_id, code),
  -- PUBLISHED bắt buộc có mốc công bố; DRAFT bắt buộc chưa có
  CONSTRAINT ck_ann_published_at
    CHECK ((status = 'PUBLISHED' AND published_at IS NOT NULL)
        OR (status = 'DRAFT'     AND published_at IS NULL))
);
CREATE INDEX ix_ann_school_status ON announcements(school_id, status, published_at DESC);
CREATE INDEX ix_ann_audience ON announcements USING gin (audience_codes);

CREATE TABLE announcement_revisions (                        -- BR-144, append-only (ADR-M10-3). Không lộ path ⇒ không `code`.
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  announcement_id UUID NOT NULL REFERENCES announcements(id),
  revised_by_user_id UUID NOT NULL REFERENCES user_accounts(id),   -- M10-D04 (C2)
  revised_by_name VARCHAR(180) NOT NULL,
  note VARCHAR(500) NOT NULL,
  revised_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  -- D9 / M10-D02: 4 cột audit (updated_* đứng yên vì RULE append-only — đúng thiết kế, giống serving_dishes của MF09)
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(180),
  updated_by VARCHAR(180)
);
CREATE INDEX ix_annrev_ann ON announcement_revisions(announcement_id, revised_at);
-- Bất biến BR-144: không sửa/xóa lịch sử (đặt ở DB, không trông vào kỷ luật code)
CREATE RULE r_annrev_no_update AS ON UPDATE TO announcement_revisions DO INSTEAD NOTHING;
CREATE RULE r_annrev_no_delete AS ON DELETE TO announcement_revisions DO INSTEAD NOTHING;

CREATE TABLE notifications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  -- M10-D01: UNIQUE trên MỘT cột `code`, KHÔNG phải (school_id, code) — school_id nullable,
  -- mà UNIQUE không chặn trùng khi một cột NULL. Đây là ngoại lệ DUY NHẤT của D1 trong MF10.
  code VARCHAR(32) NOT NULL UNIQUE,                          -- 'NTF-2026-482913'
  school_id UUID REFERENCES schools(id),                     -- NULL = toàn nền tảng (VD bảo trì)
  audience VARCHAR(8) NOT NULL CHECK (audience IN ('ALL','ROLES')),
  recipient_roles JSONB,                                     -- M10-D08: RoleKey kebab-case, chỉ có nghĩa khi 'ROLES'
  -- M10-D07 (D6): 7 giá trị của common/enums/NotificationType. Bộ 4 giá trị cũ đã bị loại (L2).
  type VARCHAR(20) NOT NULL CHECK (type IN
    ('MENU_PUBLISHED','INSPECTION_ALERT','CAPA_ASSIGNED','EXPIRY_ALERT',
     'INCIDENT_ALERT','FOOD_RECALL','ALLERGY_ALERT')),
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  link VARCHAR(255),
  link_label VARCHAR(80),
  -- M10-D07: cột `critical` ĐÃ BỎ — dẫn xuất từ `type`, tính runtime (xem UserNotification.isCritical()).
  sent_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),             -- D9 / M10-D02
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(180),
  updated_by VARCHAR(180),
  -- audience=ROLES thì PHẢI khai vai nhận (không để "thiếu dữ liệu" mang nghĩa nghiệp vụ)
  CONSTRAINT ck_ntf_roles
    CHECK (audience = 'ALL' OR (recipient_roles IS NOT NULL AND jsonb_array_length(recipient_roles) > 0))
);
CREATE INDEX ix_ntf_school_sent ON notifications(school_id, sent_at DESC);

CREATE TABLE notification_reads (                            -- CF-M10-10. Không lộ qua path ⇒ không `code`.
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  notification_id UUID NOT NULL REFERENCES notifications(id),
  user_id UUID NOT NULL REFERENCES user_accounts(id),        -- M10-D04 (C2)
  read_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),             -- D9 / M10-D02
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(180),
  updated_by VARCHAR(180),
  CONSTRAINT uq_ntfread UNIQUE (notification_id, user_id)
);

CREATE TABLE notification_preferences (                      -- UC-088, BR-158b. PK = user_id ⇒ không `code`.
  user_id UUID PRIMARY KEY REFERENCES user_accounts(id),     -- M10-D04 (C2)
  -- M10-D07: đúng 7 khóa của common/enums/NotificationType
  type_enabled JSONB NOT NULL DEFAULT '{}',
  email_fallback BOOLEAN NOT NULL DEFAULT TRUE,              -- BR-159
  quiet_start TIME NOT NULL DEFAULT '22:00',                 -- BR-158 mặc định
  quiet_end   TIME NOT NULL DEFAULT '06:00',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),             -- D9 / M10-D02
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(180),
  updated_by VARCHAR(180)
);

CREATE TABLE notification_deliveries (                       -- BR-159. Không lộ qua path ⇒ không `code`.
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  notification_id UUID NOT NULL REFERENCES notifications(id),
  user_id UUID NOT NULL REFERENCES user_accounts(id),        -- M10-D04 (C2)
  channel VARCHAR(8) NOT NULL CHECK (channel IN ('PUSH','EMAIL')),
  attempt INT NOT NULL DEFAULT 0 CHECK (attempt <= 3),       -- BR-159: retry ≤ 3 rồi mới FALLBACK_EMAIL
  status VARCHAR(16) NOT NULL
    CHECK (status IN ('QUEUED','DEFERRED','SENT','FAILED','FALLBACK_EMAIL')),
  next_retry_at TIMESTAMPTZ,
  last_error VARCHAR(500),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),             -- D9 / M10-D02
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(180),
  updated_by VARCHAR(180)
);
CREATE INDEX ix_ntfdlv_retry ON notification_deliveries(status, next_retry_at);
