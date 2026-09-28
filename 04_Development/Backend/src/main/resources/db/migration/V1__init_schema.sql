-- ═══════════════════════════════════════════════════════════════════════════════
-- V1__init_schema.sql — TOÀN BỘ SCHEMA SFMS
--
-- ⚠️ File này do NAM GIỮ (01_MVP_Backend_Implementation_Plan.md §3, Team Plan §4b
--    bảng va chạm). Module khác KHÔNG sửa V1 — thêm bảng của mình bằng V3, V4…
--
-- Bản này chứa phần MF01 (User Management & Access Control) — nguồn: TDS-MF01 §5.2.
-- 12 module còn lại nối tiếp trong các migration sau.
--
-- Quy ước bắt buộc (00_Common_Conventions.md):
--   D1  PK là UUID nội bộ + cột `code VARCHAR(32)` là mã nghiệp vụ công khai,
--       UNIQUE(school_id, code) — hoặc UNIQUE(code) toàn cục với bảng không thuộc trường.
--   D4  Mọi mốc thời gian là TIMESTAMPTZ (JSON serialize ISO-8601), ngày thuần là DATE.
--   D6  Giá trị enum lưu SCREAMING_SNAKE (riêng RoleKey là kebab-case — đồng thời là URL segment).
--
-- Bất biến DB (KHÔNG chỉ kiểm ở tầng Service — TestSpec §6 Exit Criteria):
--   · UNIQUE email                      (BR-012)
--   · UNIQUE google_sub                 (BR-218)
--   · CHECK password_hash LIKE '$2%'    (BR-001 — không bao giờ plain text)
--   · CHECK status ∈ 4 giá trị          (ADR-M03)
--   · CHECK role_key ∈ 7 giá trị        (BR-007, D6)
--   · UNIQUE 1 refresh token còn hiệu lực / user (BR-004/BR-005 single session)
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- gen_random_uuid()

-- ───────────────────────────────────────────────────────────────────────────────
-- Sinh mã nghiệp vụ có khóa (D1) — dùng INSERT ... RETURNING thay vì đọc-rồi-ghi,
-- tránh trùng mã khi 2 request song song (ADR-M11-5).
-- ───────────────────────────────────────────────────────────────────────────────
CREATE TABLE code_sequences (
    scope       VARCHAR(64)  PRIMARY KEY,     -- 'users' | 'staff' | 'roles' | ...
    next_value  BIGINT       NOT NULL DEFAULT 1
);

-- ───────────────────────────────────────────────────────────────────────────────
-- MF02 (rút gọn) — chỉ phần MF01 cần để có khóa ngoại school_id.
-- MF02 sẽ mở rộng bảng này trong migration của mình.
-- ───────────────────────────────────────────────────────────────────────────────
CREATE TABLE schools (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code           VARCHAR(32)  NOT NULL UNIQUE,     -- D1: 'SCH-HN-0042'
    name           VARCHAR(200) NOT NULL,
    address        VARCHAR(255),
    email          VARCHAR(180),
    registered_at  DATE,                             -- D4: ngày thuần
    status         VARCHAR(24)  NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ
);

-- ───────────────────────────────────────────────────────────────────────────────
-- MF04 (rút gọn) — chỉ phần MF01 cần để có khóa ngoại partner_id.
-- partner_id là khóa cách ly dữ liệu đa-tenant của NCC + là cơ chế DUY NHẤT để
-- cascade đình chỉ đối tác kéo theo khóa tài khoản (MF04 ADR-M04-6).
-- ───────────────────────────────────────────────────────────────────────────────
CREATE TABLE partners (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code         VARCHAR(32)  NOT NULL UNIQUE,       -- D1: 'SUP-001'
    name         VARCHAR(200) NOT NULL,
    partner_type VARCHAR(32)  NOT NULL,              -- D6 PartnerType
    status       VARCHAR(24)  NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ,
    CONSTRAINT ck_partner_type CHECK (partner_type IN ('RAW_FOOD_SUPPLIER', 'SERVICE_CATERER'))
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- MF01 — Tài khoản & Phân quyền
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── roles (BR-007, BR-008) ────────────────────────────────────────────────────
CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(32)  NOT NULL UNIQUE,        -- D1: 'ROLE-01'..'ROLE-07', 'ROLE-08'...
    name        VARCHAR(100) NOT NULL UNIQUE,        -- UC-098: tên role phải duy nhất
    description VARCHAR(255) NOT NULL,
    -- role_key: chỉ 7 role hệ thống mới có; role custom = NULL (không map vào RBAC nền)
    role_key    VARCHAR(32),
    system      BOOLEAN      NOT NULL DEFAULT FALSE, -- BR-008 bất khả xóa/sửa tên
    permissions JSONB        NOT NULL DEFAULT '[]',  -- ADR-M04: list PERM-id
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ,
    created_by  VARCHAR(180),                        -- audit chung (BaseEntity/AuditorAware)
    updated_by  VARCHAR(180),
    -- BR-007 / D6: đúng 7 RoleKey kebab-case, không nhãn hiển thị
    CONSTRAINT ck_role_key CHECK (
        role_key IS NULL OR role_key IN (
            'system-admin', 'school-admin', 'teacher', 'parent',
            'medical-staff', 'service-caterers', 'supplier')
    ),
    -- Role hệ thống BẮT BUỘC có role_key; role custom BẮT BUỘC không có
    CONSTRAINT ck_role_system_has_key CHECK (
        (system = TRUE AND role_key IS NOT NULL) OR (system = FALSE AND role_key IS NULL)
    )
);
CREATE UNIQUE INDEX ux_role_key ON roles(role_key) WHERE role_key IS NOT NULL;

-- ─── permissions (UC-129 — danh mục cố định, chỉ đọc) ──────────────────────────
CREATE TABLE permissions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(32)  NOT NULL UNIQUE,        -- D1: 'PERM-001'..'PERM-016'
    name        VARCHAR(80)  NOT NULL UNIQUE,
    module      VARCHAR(60)  NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ,
    created_by  VARCHAR(180),                        -- audit chung (BaseEntity/AuditorAware)
    updated_by  VARCHAR(180)
);

-- ─── user_accounts ─────────────────────────────────────────────────────────────
-- ⚠️ QUYẾT ĐỊNH SCHEMA — hai không gian mã cho cùng một con người
--
-- Mock có `USER_ACCOUNTS` (U-0001…U-0020) và `SCHOOL_STAFF` (STF-001…STF-011) là HAI mảng,
-- nhưng chúng mô tả cùng những người: `STF-001` và `U-0004` đều là Nguyễn Thu Trang,
-- email `trangnt@dtd.edu.vn`. Tách thành 2 bảng sẽ nhân đôi email ⇒ vỡ UNIQUE(email) của
-- BR-012; gộp thành 1 mã sẽ làm hỏng fixture của TestSpec (SEC-006 gọi `/staff/STF-001`,
-- INT-006 gọi `/users/U-0004`).
--
-- Giải: MỘT hàng, HAI mã.
--   · `code`       — mã trên resource /users   (D1, path param của UserController)
--   · `staff_code` — mã trên resource /staff   (D1, path param của StaffController), NULL nếu
--                    người này không phải nhân viên cấp trường
--   · `account_scope` — hàng có xuất hiện ở danh sách /users hay không.
--                    `SCHOOL_STAFF` = lời mời nhân viên chưa thành tài khoản nền tảng
--                    (VD STF-009 Hà Thị Lan, PENDING_VERIFICATION) ⇒ chỉ có ở /staff.
--                    Nhờ vậy `GET /users` trả đúng 22 hàng như MF01-TC-INT-011 (a) kỳ vọng.
CREATE TABLE user_accounts (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code               VARCHAR(32)  NOT NULL,        -- D1: 'U-0001'
    staff_code         VARCHAR(32),                  -- D1: 'STF-001', NULL nếu không phải staff
    account_scope      VARCHAR(16)  NOT NULL DEFAULT 'PLATFORM',
    name               VARCHAR(150) NOT NULL,
    email              VARCHAR(180) NOT NULL UNIQUE, -- BR-012 (bất biến DB, không chỉ ở Service)
    phone              VARCHAR(20),                  -- BR-012 (+84 / 0xx)
    password_hash      VARCHAR(72),                  -- BR-001; NULL khi PENDING hoặc Google-only
    google_sub         VARCHAR(64) UNIQUE,           -- BR-218
    role_id            UUID         NOT NULL REFERENCES roles(id),          -- BR-007
    school_id          UUID         REFERENCES schools(id),                 -- BR-013
    partner_id         UUID         REFERENCES partners(id),                -- MF04 ADR-M04-6
    status             VARCHAR(24)  NOT NULL DEFAULT 'PENDING_VERIFICATION',
    email_verified     BOOLEAN      NOT NULL DEFAULT FALSE,                 -- BR-010
    failed_login_count INT          NOT NULL DEFAULT 0,                     -- BR-002
    locked_until       TIMESTAMPTZ,                                         -- BR-002
    terms_accepted_at  TIMESTAMPTZ,                                         -- BR-009
    last_login         TIMESTAMPTZ,
    -- UC-003: tài khoản System Admin gốc — không bao giờ được vô hiệu hóa
    primary_admin      BOOLEAN      NOT NULL DEFAULT FALSE,
    avatar_url         VARCHAR(500),                                        -- UC-097
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ,
    created_by         VARCHAR(180),
    updated_by         VARCHAR(180),                                          -- audit chung (BaseEntity/AuditorAware)

    -- D1: mã nghiệp vụ duy nhất trong phạm vi trường. Tài khoản không thuộc trường
    -- (System Admin, Supplier) có school_id NULL ⇒ UNIQUE partial bên dưới lo phần đó.
    CONSTRAINT ux_user_school_code UNIQUE (school_id, code),

    -- ADR-M03: đúng 4 trạng thái
    CONSTRAINT ck_user_status CHECK (
        status IN ('PENDING_VERIFICATION', 'ACTIVE', 'INACTIVE', 'SUSPENDED')
    ),
    -- BR-001: nếu có mật khẩu thì PHẢI là hash BCrypt. Chặn plain text ở tầng DB.
    CONSTRAINT ck_password_is_bcrypt CHECK (
        password_hash IS NULL OR password_hash LIKE '$2%'
    ),
    -- TDS §6.3 invariant (1): không set password lúc tạo — PENDING_VERIFICATION luôn NULL hash
    CONSTRAINT ck_pending_has_no_password CHECK (
        status <> 'PENDING_VERIFICATION' OR password_hash IS NULL
    ),
    -- BR-002: counter không âm
    CONSTRAINT ck_failed_login_count CHECK (failed_login_count >= 0),

    CONSTRAINT ck_account_scope CHECK (account_scope IN ('PLATFORM', 'SCHOOL_STAFF'))
);

-- D1 — mã /staff duy nhất toàn hệ thống
CREATE UNIQUE INDEX ux_user_staff_code ON user_accounts(staff_code) WHERE staff_code IS NOT NULL;

-- D1: với tài khoản không thuộc trường nào, UNIQUE(school_id, code) không ràng buộc được
-- (NULL <> NULL trong UNIQUE của PostgreSQL) ⇒ thêm partial index.
CREATE UNIQUE INDEX ux_user_code_global ON user_accounts(code) WHERE school_id IS NULL;

CREATE INDEX ix_user_role       ON user_accounts(role_id);
CREATE INDEX ix_user_status     ON user_accounts(status);
CREATE INDEX ix_user_school     ON user_accounts(school_id);
CREATE INDEX ix_user_partner    ON user_accounts(partner_id);   -- cascade đình chỉ NCC
CREATE INDEX ix_user_email_lower ON user_accounts(lower(email)); -- login tra không phân biệt hoa/thường

-- ─── security_policies (BR-002/004/006 — tham số đọc từ DB, KHÔNG hardcode) ────
CREATE TABLE security_policies (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                    VARCHAR(32) NOT NULL UNIQUE,   -- D1: 'SEC-DEFAULT'
    max_login_attempts      INT     NOT NULL,              -- BR-002
    lockout_minutes         INT     NOT NULL,              -- BR-002
    session_timeout_minutes INT     NOT NULL,              -- BR-004 access TTL
    password_min_length     INT     NOT NULL,              -- BR-006
    require_special_char    BOOLEAN NOT NULL,
    require_number          BOOLEAN NOT NULL,
    require_mixed_case      BOOLEAN NOT NULL,
    no_reuse_last3          BOOLEAN NOT NULL,              -- BR-015 (thân BR-006)
    active                  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ,
    created_by              VARCHAR(180),                                     -- audit chung (BaseEntity/AuditorAware)
    updated_by              VARCHAR(180),
    CONSTRAINT ck_policy_positive CHECK (
        max_login_attempts > 0 AND lockout_minutes > 0
        AND session_timeout_minutes > 0 AND password_min_length > 0
    )
);
-- Đúng 1 chính sách đang áp dụng tại một thời điểm
CREATE UNIQUE INDEX ux_policy_single_active ON security_policies(active) WHERE active = TRUE;

-- ─── refresh_tokens (BR-004 v1.9, BR-005) ─────────────────────────────────────
CREATE TABLE refresh_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    token_hash VARCHAR(88) NOT NULL UNIQUE,     -- CHỈ lưu hash, không bao giờ token thô
    expires_at TIMESTAMPTZ NOT NULL,            -- sliding: mỗi lần rotate = now + 14 ngày
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by VARCHAR(180),                    -- audit chung (BaseEntity/AuditorAware)
    updated_by VARCHAR(180)
);
CREATE INDEX ix_rt_user ON refresh_tokens(user_id);
-- BR-005 single session — bất biến TDS §14.1: ≤ 1 refresh token chưa revoke mỗi user.
-- Ràng buộc ở tầng DB để "quên revoke trước khi insert" thành lỗi ngay, không âm thầm.
CREATE UNIQUE INDEX ux_rt_one_active_per_user
    ON refresh_tokens(user_id) WHERE revoked_at IS NULL;

-- ─── account_activation_tokens (BR-011 — 48h, dùng 1 lần) ─────────────────────
CREATE TABLE account_activation_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    token_hash VARCHAR(88) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by VARCHAR(180),                    -- audit chung (BaseEntity/AuditorAware)
    updated_by VARCHAR(180)
);
CREATE INDEX ix_act_user ON account_activation_tokens(user_id);

-- ─── password_reset_tokens (BR-003 — 1h, dùng 1 lần) ──────────────────────────
CREATE TABLE password_reset_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    token_hash VARCHAR(88) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by VARCHAR(180),                    -- audit chung (BaseEntity/AuditorAware)
    updated_by VARCHAR(180)
);
CREATE INDEX ix_prt_user ON password_reset_tokens(user_id);
-- BR-003 rate-limit 1 req/15'/email: tra bản ghi gần nhất theo user
CREATE INDEX ix_prt_user_created ON password_reset_tokens(user_id, created_at DESC);

-- ─── password_history (BR-006 thân mục / BR-015 — giữ 3 hash gần nhất) ────────
CREATE TABLE password_history (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID        NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    password_hash VARCHAR(72) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ,
    created_by    VARCHAR(180),                -- audit chung (BaseEntity/AuditorAware)
    updated_by    VARCHAR(180),
    CONSTRAINT ck_history_is_bcrypt CHECK (password_hash LIKE '$2%')
);
CREATE INDEX ix_ph_user ON password_history(user_id, created_at DESC);

-- ─── audit_logs (UC-106; BR-162 downstream: append-only) ──────────────────────
CREATE TABLE audit_logs (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code       VARCHAR(32),                       -- D1 (tùy chọn, MF12 sẽ dùng)
    timestamp  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actor      VARCHAR(180) NOT NULL,
    ip         VARCHAR(45),                       -- UC-106: cột IP bắt buộc có
    action     VARCHAR(255) NOT NULL,
    resource   VARCHAR(120) NOT NULL,
    result     VARCHAR(16)  NOT NULL,             -- SUCCESS | BLOCKED
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by VARCHAR(180),                      -- audit chung (BaseEntity/AuditorAware)
    updated_by VARCHAR(180),
    CONSTRAINT ck_audit_result CHECK (result IN ('SUCCESS', 'BLOCKED'))
);
CREATE INDEX ix_audit_actor  ON audit_logs(actor);
CREATE INDEX ix_audit_ts     ON audit_logs(timestamp DESC);
CREATE INDEX ix_audit_action ON audit_logs(action);

-- ═══════════════════════════════════════════════════════════════════════════════
-- Ghi chú cho reviewer (Mạnh — Lead Backend & DB):
--
-- 1. Bảng `staff` KHÔNG tồn tại riêng. `StaffMember` của mock là **view nghiệp vụ** của
--    `user_accounts` lọc theo (school_id = trường của JWT) AND role_key ∈ (teacher,
--    medical-staff, service-caterers) — UC-024 bước 4 + UC-144. Tách bảng riêng sẽ
--    nhân đôi email và làm vỡ UNIQUE(email) của BR-012.
--
-- 2. `roles.permissions` là JSONB chứ không phải bảng nối, vì ADR-M04 chốt "giữ mô hình
--    mock: RoleRecord.permissions = list PERM-id". Danh mục 16 permission là cố định,
--    chỉ đọc (UC-129) ⇒ không cần toàn vẹn tham chiếu ở tầng DB.
--
-- 3. KHÔNG có cột `users`/`user_count` trên bảng `roles` — đó là **field dẫn xuất**, tính
--    runtime bằng COUNT(user_accounts WHERE role_id = ...). MF01-TC-INT-013 bước 5 assert
--    đúng điều này qua information_schema.columns.
-- ═══════════════════════════════════════════════════════════════════════════════
