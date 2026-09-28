-- ═══════════════════════════════════════════════════════════════════════════════
-- V900__test_fixtures.sql — SEED CHỈ CHO MÔI TRƯỜNG TEST
--
-- ⚠️ Nằm ở src/test/resources ⇒ KHÔNG BAO GIỜ lên classpath của prod.
--
-- Nguồn: TestSpec-MF01 §3 TDS-05 (bảng Test Data Requirements).
-- Test data = SYNTHETIC. Mã bản ghi lấy từ mock-data.ts; ĐỊNH DẠNG và GIÁ TRỊ đã chuyển đổi
-- theo D4 (ISO-8601) và D6 (RoleKey kebab-case) — chuyển đổi làm MỘT LẦN tại đây
-- (Team Plan §0, TestSpec §2 L12).
--
-- Mật khẩu chung của mọi tài khoản ACTIVE: "P@ssw0rd123" (FX-PWD-VALID), BCrypt cost 12.
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── Trường (MF02 — TestSpec-MF02 §3 TDS-05, FX-001..FX-004) ──────────────────
-- (a)/(b) chốt 2026-07-31: registered_at nay là TIMESTAMPTZ; status mặc định PENDING
-- ở cột (DEFAULT), nhưng seed dưới đây set tường minh cho từng trường theo TestSpec.
INSERT INTO schools
 (id, code, name, address, email, registered_at, status,
  tax_id, lat, lng, coordinate_locked, operating_hours, emergency_contact, meals_per_day) VALUES
 -- FX-001 — trường chính, happy path, chủ thể JWT School Admin
 ('00000000-0000-0000-0000-0000000a0042', 'SCH-HN-0042', 'TH Đoàn Thị Điểm',
  'Nam Từ Liêm, Hà Nội', 'contact@dtd.edu.vn', TIMESTAMPTZ '2026-01-05T00:00:00Z', 'ACTIVE',
  '0101245678', 21.031200, 105.763800, TRUE,
  '07:00 - 17:00 (T2-T6)', 'Phạm Quốc Toản - 0912345678', 2),
 -- FX-002 — trường thứ hai, cross-school isolation + BR-191 per-school (INT-002)
 ('00000000-0000-0000-0000-0000000a0051', 'SCH-HN-0051', 'TH Nguyễn Siêu',
  'Cầu Giấy, Hà Nội', 'contact@ns.edu.vn', TIMESTAMPTZ '2026-01-05T00:00:00Z', 'ACTIVE',
  '0102387654', 21.036500, 105.796100, TRUE,
  '07:00 - 17:00 (T2-T6)', 'Lê Quang Vinh - 0917111222', 2),
 -- FX-003 — trường CHƯA active, tọa độ CHƯA khóa (BR-028 lock lifecycle + BR-192)
 ('00000000-0000-0000-0000-0000000a0063', 'SCH-HN-0063', 'TH Vinschool Times City',
  'Hai Bà Trưng, Hà Nội', 'admin@vinschool-tc.edu.vn', TIMESTAMPTZ '2026-07-28T00:00:00Z', 'PENDING',
  '0109988776', 20.995500, 105.868300, FALSE, NULL, NULL, 0),
 -- FX-004 — trường thứ ba, phân trang (total=4) + RBAC "All" của System Admin
 ('00000000-0000-0000-0000-0000000a0107', 'SCH-HCM-0107', 'TH Nguyễn Bỉnh Khiêm',
  'Quận 1, TP.HCM', 'contact@nbk.edu.vn', TIMESTAMPTZ '2026-01-05T00:00:00Z', 'ACTIVE',
  '0312456789', 10.776500, 106.700900, TRUE, NULL, NULL, 3);

-- ─── Đối tác (MF04) ─────────────────────────────────────────────────────────
-- ⚠️ Sửa 2026-08-02 (MF04 V6 migration): `status='ACTIVE'` của stub V1 KHÔNG còn hợp lệ
-- sau khi MF04 thêm ck_partner_status (PENDING/APPROVED/REJECTED/SUSPENDED) — đổi SUP-001
-- sang APPROVED (BR-041, khớp ngữ nghĩa "đối tác đang hoạt động" mà giá trị ACTIVE ám chỉ).
-- Thêm 4 cột NOT NULL mới (tax_id/address/contact/email) do V6 ALTER TABLE partners.
-- KHÔNG đổi id/code/name/partner_type — MF01-TC-INT-014/TenantIsolationIT dựa vào chúng.
INSERT INTO partners (id, code, name, partner_type, status, tax_id, address, contact, email, applied_at, suspend_reason) VALUES
 ('00000000-0000-0000-0000-0000000b0001', 'SUP-001', 'Green Farm', 'RAW_FOOD_SUPPLIER', 'APPROVED',
  '0107712345', 'KCN Phú Nghĩa, Chương Mỹ, Hà Nội', 'Lý Thái Sơn — 0913 222 333', 'sonlt@greenfarm.vn',
  TIMESTAMPTZ '2026-01-05T00:00:00Z', NULL),
 ('00000000-0000-0000-0000-0000000b0004', 'SUP-004', 'Đại Dương Food', 'RAW_FOOD_SUPPLIER', 'SUSPENDED',
  '0107799887', 'Cụm CN Hải An, Hải Phòng', 'Đặng Hải Yến — 0912 345 607', 'yendh@daiduong.vn',
  TIMESTAMPTZ '2026-01-05T00:00:00Z', 'Lô tôm BATCH-DD-1092 không đạt kiểm tra tiếp nhận 30/06; chứng nhận ATTP hết hiệu lực 22/06/2026.');

-- MF04 — 2 đối tác MỚI (không đụng SUP-001/SUP-004 đã dùng ở MF01-TC-INT-014/TenantIsolationIT).
-- SUP-005: PENDING, dùng cho luồng duyệt/từ chối (UC-007). CAT-001: Service Caterer, APPROVED.
INSERT INTO partners (id, code, name, partner_type, status, tax_id, address, contact, email, applied_at) VALUES
 ('00000000-0000-0000-0000-0000000b0005', 'SUP-005', 'Cty TNHH Nông sản Tây Bắc', 'RAW_FOOD_SUPPLIER', 'PENDING',
  '0109210987', 'Mộc Châu, Sơn La', 'Vàng A Chứ — 0966 010 203', 'lienhe@taybac.vn',
  TIMESTAMPTZ '2026-07-28T02:15:00Z');
INSERT INTO partners (id, code, name, partner_type, status, tax_id, address, contact, email, applied_at, site_inspection_score) VALUES
 ('00000000-0000-0000-0000-0000000b0006', 'CAT-001', 'Đơn vị suất ăn An Bình', 'SERVICE_CATERER', 'APPROVED',
  '0108812233', '25 Láng Hạ, Đống Đa, Hà Nội', 'Hoàng Minh Đức — 0915 555 666', 'lienhe@anbinhcatering.vn',
  TIMESTAMPTZ '2026-02-01T00:00:00Z', 92.00);

-- ─── FX-ROLE-CUSTOM-EMPTY / FX-ROLE-CUSTOM-USED ───────────────────────────────
-- ROLE-08 có 0 user (xóa được — UC-029 happy path).
-- ROLE-09 là fixture MỚI: 3 user gán, trong đó 1 INACTIVE — nhánh mà mock KHÔNG phủ được,
-- dùng để chứng minh BR-008 đếm "kể cả đã vô hiệu hóa" (TestSpec §2 L10).
INSERT INTO roles (code, name, role_key, description, system, permissions) VALUES
 ('ROLE-08', 'Kitchen Auditor', NULL, 'Vai trò tùy chỉnh — kiểm toán bếp độc lập', FALSE,
  '["PERM-013","PERM-015"]'::jsonb),
 ('ROLE-09', 'Ad-hoc Auditor',  NULL, 'Vai trò tùy chỉnh — kiểm toán đột xuất', FALSE,
  '["PERM-013"]'::jsonb);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 22 tài khoản PLATFORM — MF01-TC-INT-011 (a) kỳ vọng GET /users trả total = 22
--   = 20 user U-0001…U-0020 (mock USER_ACCOUNTS) + 2 fixture System Admin (§2 L10)
--
-- `staff_code` khác NULL ở những người đồng thời là nhân viên cấp trường: mock giữ hai
-- không gian mã cho cùng con người (STF-001 ≡ U-0004, cùng email) — xem V1__init_schema.sql.
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO user_accounts
 (code, staff_code, account_scope, name, email, phone, password_hash, google_sub,
  role_id, school_id, partner_id, status, email_verified, failed_login_count,
  terms_accepted_at, last_login, primary_admin)
SELECT v.code, v.staff_code, v.account_scope, v.name, v.email, v.phone,
       -- BCrypt("P@ssw0rd123", cost 12) — chỉ cho tài khoản đã kích hoạt (BR-001).
       -- ⚠️ Hash này ĐÃ được xác minh round-trip bằng chính BCryptPasswordEncoder(12) thật
       -- (không phải chuỗi bịa) — sai 1 ký tự ở đây làm MỌI login trong Integration/Security
       -- suite thất bại ngay từ bước đầu.
       CASE WHEN v.has_password THEN '$2a$12$K19BeUNTqLNKiuAsHIkEsu8GBU6eRu.V0UmpNU5wFGevBNHMJuIo.' END,
       v.google_sub,
       r.id, s.id, p.id, v.status, v.email_verified, 0,
       v.terms_accepted_at, v.last_login, v.primary_admin
FROM (VALUES
  -- code    , staff_code, scope       , name                , email                        , phone       , has_pwd, google_sub          , role_key           , school_code   , partner_code, status                 , verified, terms_accepted_at             , last_login                    , primary
  ('U-0001', 'STF-010', 'PLATFORM', 'Phạm Quốc Toản'    , 'toanpq@dtd.edu.vn'          , '0912345678', TRUE , NULL                , 'school-admin'    , 'SCH-HN-0042', NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', TIMESTAMPTZ '2026-07-02T00:45:00Z', FALSE),
  ('U-0002', 'STF-011', 'PLATFORM', 'Ngô Thị Hà'        , 'hant@dtd.edu.vn'            , '0912345602', TRUE , NULL                , 'medical-staff'   , 'SCH-HN-0042', NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  ('U-0003', NULL     , 'PLATFORM', 'Trần Văn Hùng'     , 'hungtv@anbinhcatering.vn'   , '0912345603', TRUE , NULL                , 'service-caterers', 'SCH-HN-0042', NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  ('U-0004', 'STF-001', 'PLATFORM', 'Nguyễn Thu Trang'  , 'trangnt@dtd.edu.vn'         , '0912111222', TRUE , NULL                , 'teacher'         , 'SCH-HN-0042', NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  ('U-0005', NULL     , 'PLATFORM', 'Nguyễn Văn Bình'   , 'binhnv.parent@gmail.com'    , '0912345605', TRUE , NULL                , 'parent'          , NULL         , NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  ('U-0006', NULL     , 'PLATFORM', 'Lý Thái Sơn'       , 'sonlt@greenfarm.vn'         , '0912345606', TRUE , NULL                , 'supplier'        , NULL         , 'SUP-001', 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  ('U-0007', NULL     , 'PLATFORM', 'Đặng Hải Yến'      , 'yendh@daiduong.vn'          , '0912345607', TRUE , NULL                , 'supplier'        , NULL         , 'SUP-004', 'SUSPENDED'           , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  -- Registrant của SUP-005 (partner PENDING) — cần cho BR-010 mở rộng (11/08/2026):
  -- PartnerApprovalServiceImpl.approve() chặn 409 nếu registrant chưa email_verified.
  ('U-0023', NULL     , 'PLATFORM', 'Đỗ Minh Tâm'       , 'tamdm@tayback.vn'           , '0912345623', TRUE , NULL                , 'supplier'        , NULL         , 'SUP-005', 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  ('U-0008', 'STF-003', 'PLATFORM', 'Đào Văn Quân'      , 'quandv@dtd.edu.vn'          , '0913555666', TRUE , NULL                , 'teacher'         , 'SCH-HN-0042', NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  ('U-0009', 'STF-002', 'PLATFORM', 'Phan Thị Hồng'     , 'hongpt@dtd.edu.vn'          , '0912333444', TRUE , 'google-sub-0009'   , 'teacher'         , 'SCH-HN-0042', NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  ('U-0010', 'STF-004', 'PLATFORM', 'Lưu Thị Bích'      , 'bichlt@dtd.edu.vn'          , '0914777888', TRUE , NULL                , 'teacher'         , 'SCH-HN-0042', NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  ('U-0011', 'STF-005', 'PLATFORM', 'Trịnh Văn Khoa'    , 'khoatv@dtd.edu.vn'          , '0915111222', TRUE , NULL                , 'teacher'         , 'SCH-HN-0042', NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  ('U-0012', 'STF-006', 'PLATFORM', 'Bùi Thị Mai'       , 'maibt@dtd.edu.vn'           , '0915333444', TRUE , NULL                , 'medical-staff'   , 'SCH-HN-0042', NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  ('U-0013', 'STF-007', 'PLATFORM', 'Hoàng Minh Đức'    , 'duchm@dtd.edu.vn'           , '0915555666', TRUE , NULL                , 'service-caterers', 'SCH-HN-0042', NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  ('U-0014', 'STF-008', 'PLATFORM', 'Vương Thị Nga'     , 'ngavt@dtd.edu.vn'           , '0915777888', TRUE , NULL                , 'service-caterers', 'SCH-HN-0042', NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  ('U-0015', NULL     , 'PLATFORM', 'Chu Văn An'        , 'ancv.parent@gmail.com'      , '0916111222', TRUE , NULL                , 'parent'          , NULL         , NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  ('U-0016', NULL     , 'PLATFORM', 'Vũ Đình Phong'     , 'phongvd.parent@gmail.com'   , '0916333444', TRUE , NULL                , 'parent'          , NULL         , NULL     , 'INACTIVE'            , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  ('U-0017', NULL     , 'PLATFORM', 'Tạ Thị Lành'       , 'lanhtt.parent@gmail.com'    , '0916555666', TRUE , NULL                , 'parent'          , NULL         , NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  -- Trường B — dùng cho MF01-TC-SEC-006 (cách ly schoolId)
  ('U-0018', NULL     , 'PLATFORM', 'Lê Quang Vinh'     , 'vinhlq@ns.edu.vn'           , '0917111222', TRUE , NULL                , 'school-admin'    , 'SCH-HN-0051', NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  ('U-0019', 'STF-101', 'PLATFORM', 'Đỗ Thị Vân'        , 'vandt@ns.edu.vn'            , '0917333444', TRUE , NULL                , 'teacher'         , 'SCH-HN-0051', NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  -- Cùng partnerId SUP-001 với U-0006 — MF01-TC-INT-014 assert cả hai bị SUSPENDED
  ('U-0020', NULL     , 'PLATFORM', 'Ngô Bá Khá'        , 'khanb@greenfarm.vn'         , '0917555666', TRUE , NULL                , 'supplier'        , NULL         , 'SUP-001', 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  -- MF02 — School Admin của SCH-HN-0063 (PENDING, KHÔNG có năm học Active nào — TestSpec FX-003)
  -- dùng cho MF02-TC-INT-002 (★): activate ở trường KHÔNG có năm Active trong khi trường khác có.
  -- ⚠️ account_scope='SCHOOL_STAFF' (không 'PLATFORM') để KHÔNG đổi total=22 của MF01-TC-INT-011(a)
  -- (GET /users chỉ đếm PLATFORM) — vẫn login được bình thường qua tokenFor("U-0021").
  ('U-0021', NULL     , 'SCHOOL_STAFF', 'Bùi Thu Hường' , 'huongbt@vinschool-tc.edu.vn', '0918333444', TRUE , NULL                , 'school-admin'    , 'SCH-HN-0063', NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  -- FX-U-SYSADMIN / FX-U-SYSADMIN-PRIMARY (fixture MỚI — §2 L10)
  ('U-9000', NULL     , 'PLATFORM', 'SFMS Root'         , 'root@sfms.vn'               , '0900000000', TRUE , NULL                , 'system-admin'    , NULL         , NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , TRUE ),
  ('U-9001', NULL     , 'PLATFORM', 'SFMS System Admin' , 'sysadmin@sfms.vn'           , '0900000001', TRUE , NULL                , 'system-admin'    , NULL         , NULL     , 'ACTIVE'              , TRUE , TIMESTAMPTZ '2026-01-10T02:00:00Z', NULL                              , FALSE),
  -- FX-U-PENDING — lời mời nhân viên chưa kích hoạt: CHỈ có ở /staff, KHÔNG tính vào 22 của /users
  ('STF-009','STF-009', 'SCHOOL_STAFF', 'Hà Thị Lan'    , 'lanht@dtd.edu.vn'           , '0918111222', FALSE, NULL                , 'teacher'         , 'SCH-HN-0042', NULL     , 'PENDING_VERIFICATION', FALSE, NULL                              , NULL                              , FALSE)
) AS v(code, staff_code, account_scope, name, email, phone, has_password, google_sub,
       role_key, school_code, partner_code, status, email_verified,
       terms_accepted_at, last_login, primary_admin)
JOIN roles r ON r.role_key = v.role_key
LEFT JOIN schools s ON s.code = v.school_code
LEFT JOIN partners p ON p.code = v.partner_code;

-- ─── FX-ROLE-CUSTOM-USED — gán ROLE-09 cho 3 user, 1 trong đó INACTIVE ────────
-- BR-008 nhấn mạnh "kể cả đã vô hiệu hóa"; mock chỉ có role custom rỗng nên không phủ được.
UPDATE user_accounts SET role_id = (SELECT id FROM roles WHERE code = 'ROLE-09')
 WHERE code IN ('U-0015', 'U-0017', 'U-0016');

-- ─── FX-PWD-HISTORY — 3 mật khẩu gần nhất của U-0001 (BR-006 / MSG-105) ───────
-- Hash THẬT của "Old@Pass1", "Old@Pass2", "Old@Pass3" — đã xác minh round-trip bằng
-- BCryptPasswordEncoder(12) thật. ⚠️ Không thay bằng chuỗi tự bịa: chuỗi không phải hash
-- thật của đúng mật khẩu sẽ khiến PasswordHistoryService.assertNotReused() KHÔNG BAO GIỜ
-- phát hiện trùng, làm test reuse-password (BR-006/MSG-105) không bao giờ bắt được lỗi.
INSERT INTO password_history (user_id, password_hash, created_at)
SELECT u.id, h.hash, h.created_at
FROM user_accounts u
CROSS JOIN (VALUES
  ('$2a$12$ou3WvvGTY9OBOowHWqwne.4vIQ8140oSU7B1oU2XWYFGvn4rRPRZi', TIMESTAMPTZ '2026-04-01T00:00:00Z'), -- Old@Pass1
  ('$2a$12$rjexl5kRKo.moJcKVefZeOtxJ7mHOmqQftY00PEUrf3K1hdfrpsya', TIMESTAMPTZ '2026-05-01T00:00:00Z'), -- Old@Pass2
  ('$2a$12$qd1UaZAsjOKBbI1ocO1sjeGwkIbgFlNgWwpeKKLLh.qpuRA02Oj7u', TIMESTAMPTZ '2026-06-01T00:00:00Z')  -- Old@Pass3
) AS h(hash, created_at)
WHERE u.code = 'U-0001';

-- ═══════════════════════════════════════════════════════════════════════════════
-- MF02 — Năm học (TestSpec-MF02 §3 TDS-05, FX-005..FX-010) — CÓ school_id
-- (khác mock — CF-M02-3 / L4), bắt buộc cho BR-191 per-school.
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO academic_years (id, code, school_id, name, start_date, end_date, status, copy_structure)
SELECT v.id, v.code, s.id, v.name, v.start_date, v.end_date, v.status, FALSE
FROM (VALUES
  -- id                                    , code         , school_code  , name                    , start_date   , end_date     , status
  ('00000000-0000-0000-0000-0000000b0005'::uuid, 'AY-2025'  , 'SCH-HN-0042', 'Năm học 2025–2026', DATE '2025-09-05', DATE '2026-05-31', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000b0006'::uuid, 'AY-2026'  , 'SCH-HN-0042', 'Năm học 2026–2027', DATE '2026-09-05', DATE '2027-05-31', 'DRAFT'),
  ('00000000-0000-0000-0000-0000000b0007'::uuid, 'AY-2024'  , 'SCH-HN-0042', 'Năm học 2024–2025', DATE '2024-09-05', DATE '2025-05-31', 'CLOSED'),
  ('00000000-0000-0000-0000-0000000b0008'::uuid, 'AY-2023'  , 'SCH-HN-0042', 'Năm học 2023–2024', DATE '2023-09-05', DATE '2024-05-31', 'ARCHIVED'),
  ('00000000-0000-0000-0000-0000000b0009'::uuid, 'AY-2025-B', 'SCH-HN-0051', 'Năm học 2025–2026', DATE '2025-09-05', DATE '2026-05-31', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000b0010'::uuid, 'AY-2026-B', 'SCH-HN-0051', 'Năm học 2026–2027', DATE '2026-09-05', DATE '2027-05-31', 'DRAFT'),
  -- MF02-TC-INT-002 (★): SCH-HN-0063 KHÔNG có năm ACTIVE nào — activate ở đây phải 200 dù
  -- SCH-HN-0042/0051 đang có năm ACTIVE (per-school, không phải toàn cục — CF-M02-3/ADR-M02-4).
  ('00000000-0000-0000-0000-0000000b0011'::uuid, 'AY-2026-C', 'SCH-HN-0063', 'Năm học 2026–2027', DATE '2026-09-05', DATE '2027-05-31', 'DRAFT')
) AS v(id, code, school_code, name, start_date, end_date, status)
JOIN schools s ON s.code = v.school_code;

-- ═══════════════════════════════════════════════════════════════════════════════
-- MF02 — grades (V44) — 12 khối/trường cho MỌI trường đã seed ở trên, cùng khuôn backfill
-- của V44 (name = 'Khối ' || n). Bắt buộc TRƯỚC classrooms vì classrooms.grade_id NOT NULL.
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO grades (school_id, name, sort_order)
SELECT s.id, 'Khối ' || g.n, g.n
FROM schools s
CROSS JOIN generate_series(1, 12) AS g(n)
ON CONFLICT (school_id, name) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════════════
-- MF02 — Lớp học (TestSpec-MF02 §3 TDS-05, FX-011..FX-018) — code = tên lớp (D1/L2).
-- GVCN trỏ user_accounts.id đã seed ở trên (ADR-M02-5): STF-001/002/003/004/006.
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO classrooms
 (code, school_id, academic_year_id, name, grade_id, capacity, homeroom_teacher_id, read_only)
SELECT v.code, s.id, ay.id, v.code, g.id, v.capacity, u.id, v.read_only
FROM (VALUES
  -- code, school_code  , ay_code  , grade, capacity, teacher_staff_code, read_only
  ('3A', 'SCH-HN-0042', 'AY-2025', 3, 45, 'STF-001', FALSE),  -- FX-011 happy path, đích reassign
  ('3B', 'SCH-HN-0042', 'AY-2025', 3, 45, 'STF-002', FALSE),  -- FX-012 GVCN đang chủ nhiệm khác
  ('4A', 'SCH-HN-0042', 'AY-2025', 4, 50, 'STF-003', FALSE),  -- FX-013 nguồn xung đột BR-020
  ('5A', 'SCH-HN-0042', 'AY-2025', 5, 50, NULL      , FALSE), -- FX-014 khối 5 — nguồn promote
  ('5B', 'SCH-HN-0042', 'AY-2025', 5, 50, NULL      , FALSE),
  ('1A', 'SCH-HN-0042', 'AY-2025', 1, 45, NULL      , FALSE), -- FX-015 đủ 10 lớp cho phân trang
  ('1B', 'SCH-HN-0042', 'AY-2025', 1, 45, NULL      , FALSE),
  ('2A', 'SCH-HN-0042', 'AY-2025', 2, 45, NULL      , FALSE),
  ('2B', 'SCH-HN-0042', 'AY-2025', 2, 45, NULL      , FALSE),
  ('4B', 'SCH-HN-0042', 'AY-2025', 4, 50, NULL      , FALSE),
  ('9Z', 'SCH-HN-0051', 'AY-2025-B', 9, 45, NULL    , FALSE)  -- FX-018 trường B — cross-school IDOR
) AS v(code, school_code, ay_code, grade, capacity, teacher_staff_code, read_only)
JOIN schools s ON s.code = v.school_code
JOIN academic_years ay ON ay.code = v.ay_code AND ay.school_id = s.id
JOIN grades g ON g.school_id = s.id AND g.name = 'Khối ' || v.grade
LEFT JOIN user_accounts u ON u.staff_code = v.teacher_staff_code;

-- FX-016/FX-017 — lớp đích của promote (UC-177), thuộc năm DRAFT AY-2026
INSERT INTO classrooms
 (code, school_id, academic_year_id, name, grade_id, capacity, read_only)
SELECT v.code, s.id, ay.id, v.code, g.id, v.capacity, FALSE
FROM (VALUES
  ('6A', 'SCH-HN-0042', 'AY-2026', 6, 50),  -- FX-016 lớp đích còn trống
  ('6B', 'SCH-HN-0042', 'AY-2026', 6, 45)   -- FX-017 lớp đích gần đầy (test vượt sức chứa)
) AS v(code, school_code, ay_code, grade, capacity)
JOIN schools s ON s.code = v.school_code
JOIN academic_years ay ON ay.code = v.ay_code AND ay.school_id = s.id
JOIN grades g ON g.school_id = s.id AND g.name = 'Khối ' || v.grade;

-- ═══════════════════════════════════════════════════════════════════════════════
-- MF02 — school_config (TestSpec-MF02 §3 TDS-05, FX-026) — baseline BR-028 geofence.
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO school_config (school_id, cost_ceiling, geofence_radius, report_emails)
SELECT s.id, 30000, 200, '["toanpq@dtd.edu.vn","hant@dtd.edu.vn"]'::jsonb
FROM schools s WHERE s.code = 'SCH-HN-0042';

-- Cập nhật bộ đếm code_sequences để tránh trùng với seed data U-0001..U-0020 và STF-001..STF-011
UPDATE code_sequences SET next_value = 21 WHERE scope = 'users';
UPDATE code_sequences SET next_value = 12 WHERE scope = 'staff';

-- MF02 — tránh trùng mã khi test tạo school mới không gửi `id` (D5)
INSERT INTO code_sequences (scope, next_value) VALUES ('schools', 200)
ON CONFLICT (scope) DO UPDATE SET next_value = EXCLUDED.next_value;

-- ═══════════════════════════════════════════════════════════════════════════════
-- MF04 — Supplier & Partner Management (TestSpec-MF04 §3 TDS-05, FX-001..FX-016)
-- Đối tác SUP-001/SUP-004 seed ở trên (MF01). Thêm SUP-005 (PENDING)/CAT-001 (Service
-- Caterer) + tài khoản NCC/Đơn vị suất ăn tương ứng + 4 bảng con MF04.
-- ═══════════════════════════════════════════════════════════════════════════════

-- FX-JWT — tài khoản Service Caterer (partnerId=CAT-001, KHÔNG gắn trường nào).
INSERT INTO user_accounts
 (code, staff_code, account_scope, name, email, phone, password_hash, role_id, school_id, partner_id,
  status, email_verified, failed_login_count, terms_accepted_at)
SELECT 'U-0022', NULL, 'PLATFORM', 'Hoàng Minh Đức', 'lienhe@anbinhcatering.vn', '0915555666',
       '$2a$12$K19BeUNTqLNKiuAsHIkEsu8GBU6eRu.V0UmpNU5wFGevBNHMJuIo.', r.id, NULL, p.id,
       'ACTIVE', TRUE, 0, TIMESTAMPTZ '2026-02-01T00:00:00Z'
FROM roles r, partners p
WHERE r.role_key = 'service-caterers' AND p.code = 'CAT-001';

UPDATE code_sequences SET next_value = GREATEST(next_value, 23) WHERE scope = 'users';

-- FX-005 — partner_documents. `today` cố định của AbstractIamIntegrationTest = 2026-07-30
-- (MutableClock) ⇒ 4 lớp tương đương BR-047: VALID / EXPIRING(180d) / URGENT(30d) / EXPIRED.
INSERT INTO partner_documents
 (id, code, partner_id, name, document_type, number, issued_by, issued_date, expiry_date,
  file_key, file_size, content_type, uploaded_at)
SELECT v.id::uuid, v.code, p.id, v.name, v.document_type, v.number, v.issued_by,
       v.issued_date::date, v.expiry_date::date, v.file_key, v.file_size::bigint, v.content_type,
       v.issued_date::timestamptz
FROM (VALUES
  -- id                                    , code         , partner_code, name                                            , document_type      , number              , issued_by                 , issued_date , expiry_date , file_key                              , file_size, content_type
  ('00000000-0000-0000-0000-0000000c0001', 'DOC-GF-01', 'SUP-001', 'Giấy chứng nhận cơ sở đủ điều kiện ATTP', 'FOOD_SAFETY_CERT', 'ATTP-HN-2024-1182', 'Chi cục ATVSTP Hà Nội',    '2024-03-15', '2027-03-15', 'partner-documents/DOC-GF-01.pdf', '204800',  'application/pdf'),
  ('00000000-0000-0000-0000-0000000c0002', 'DOC-GF-02', 'SUP-001', 'Giấy phép kinh doanh',                    'BUSINESS_LICENSE', 'GPKD-0107712345',  'Sở KH&ĐT Hà Nội',         '2023-01-10', NULL,         'partner-documents/DOC-GF-02.pdf', '153600',  'application/pdf'),
  ('00000000-0000-0000-0000-0000000c0003', 'DOC-GF-04', 'SUP-001', 'Chứng nhận VietGAP',                      'PRODUCT_ORIGIN',   'VG-2026-0041',     'TT Chứng nhận VietCert',  '2025-08-01', '2026-08-15', 'partner-documents/DOC-GF-04.pdf', '184320',  'image/jpeg'),
  ('00000000-0000-0000-0000-0000000c0004', 'DOC-DD-01', 'SUP-004', 'Giấy chứng nhận cơ sở đủ điều kiện ATTP', 'FOOD_SAFETY_CERT', 'ATTP-HP-2023-0771', 'Chi cục ATVSTP Hải Phòng', '2023-06-22', '2026-06-22', 'partner-documents/DOC-DD-01.pdf', '204800',  'application/pdf'),
  ('00000000-0000-0000-0000-0000000c0005', 'DOC-TB-01', 'SUP-005', 'Giấy phép kinh doanh',                    'BUSINESS_LICENSE', 'GPKD-0109210987',  'Sở KH&ĐT Sơn La',         '2024-05-01', NULL,         'partner-documents/DOC-TB-01.pdf', '153600',  'application/pdf')
) AS v(id, code, partner_code, name, document_type, number, issued_by, issued_date, expiry_date, file_key, file_size, content_type)
JOIN partners p ON p.code = v.partner_code;
UPDATE code_sequences SET next_value = GREATEST(next_value, 10) WHERE scope = 'partner_documents';

-- FX-006 — delivery_persons. national_id_enc là placeholder (seed SQL thô, không qua
-- StorageService/mã hóa thật của app) — KHÔNG dùng để test giải mã, chỉ để cột NOT NULL
-- có giá trị. national_id_hash dùng digest() thật (pgcrypto) để giữ đúng ngữ nghĩa UNIQUE (BR-050).
INSERT INTO delivery_persons
 (id, code, partner_id, full_name, national_id_enc, national_id_hash, phone, vehicle_plate,
  biometric_status, face_id, biometric_registered_at)
SELECT v.id::uuid, v.code, p.id, v.full_name, decode(v.enc_hex, 'hex'),
       encode(digest(v.national_id, 'sha256'), 'hex'), v.phone, v.vehicle_plate,
       v.biometric_status, v.face_id, v.biometric_registered_at::timestamptz
FROM (VALUES
  -- id                                    , code    , partner_code, full_name         , national_id    , enc_hex, phone         , vehicle_plate , biometric_status      , face_id        , biometric_registered_at
  ('00000000-0000-0000-0000-0000000c0101', 'DP-01', 'SUP-001', 'Đỗ Văn Tám',      '001099210001', 'DEADBEEF', '0912345678', '29C-123.45', 'REGISTERED',           'mock-uuid-dp01', '2026-03-12T00:00:00Z'),
  ('00000000-0000-0000-0000-0000000c0102', 'DP-03', 'SUP-001', 'Lê Thị Hòa',      '001099210003', 'DEADBEEF', '0905112233', '29E-246.80', 'PENDING_REGISTRATION', NULL,             NULL),
  ('00000000-0000-0000-0000-0000000c0103', 'DP-04', 'SUP-004', 'Nguyễn Văn Kiên', '001099210004', 'DEADBEEF', '0977889900', '15A-678.90', 'NOT_REGISTERED',       NULL,             NULL)
) AS v(id, code, partner_code, full_name, national_id, enc_hex, phone, vehicle_plate, biometric_status, face_id, biometric_registered_at)
JOIN partners p ON p.code = v.partner_code;
UPDATE code_sequences SET next_value = GREATEST(next_value, 10) WHERE scope = 'delivery_persons';

-- FX-007 — partnership_applications (CF-M04-9: đủ dữ liệu form, gắn partner_id + school_id thật).
INSERT INTO partnership_applications
 (id, code, partner_id, school_id, company_name, tax_id, address, submitted_at, status, comment)
SELECT v.id::uuid, v.code, p.id, s.id, v.company_name, v.tax_id, v.address,
       v.submitted_at::timestamptz, v.status, v.comment
FROM (VALUES
  -- id                                    , code         , partner_code, school_code  , company_name                       , tax_id      , address                      , submitted_at            , status           , comment
  ('00000000-0000-0000-0000-0000000c0201', 'APP-2026-01', 'SUP-001', 'SCH-HN-0051', 'Cty CP Thực phẩm sạch GreenFarm', '0107712345', 'KCN Phú Nghĩa, Chương Mỹ, Hà Nội', '2026-06-20T02:00:00Z', 'REJECTED',       'Giấy chứng nhận ATTP đính kèm đã hết hạn — bổ sung bản còn hiệu lực trước khi nộp lại.'),
  ('00000000-0000-0000-0000-0000000c0202', 'APP-2026-05', 'SUP-005', 'SCH-HN-0042', 'Cty TNHH Nông sản Tây Bắc',       '0109210987', 'Mộc Châu, Sơn La',                 '2026-07-28T02:15:00Z', 'PENDING',        NULL)
) AS v(id, code, partner_code, school_code, company_name, tax_id, address, submitted_at, status, comment)
JOIN partners p ON p.code = v.partner_code
JOIN schools  s ON s.code = v.school_code;
UPDATE code_sequences SET next_value = GREATEST(next_value, 10) WHERE scope = 'partnership_applications';

-- FX-003/FX-012 — supplier_partnerships (quan hệ hợp tác ĐANG hiệu lực, sinh ra khi đơn duyệt).
INSERT INTO supplier_partnerships
 (id, code, partner_id, school_id, delivery_address, since, until, status)
SELECT v.id::uuid, v.code, p.id, s.id, v.delivery_address, v.since::date, v.until::date, v.status
FROM (VALUES
  ('00000000-0000-0000-0000-0000000c0301', 'PTN-01', 'SUP-001', 'SCH-HN-0042', '15 Nguyễn Chí Thanh, Ba Đình, Hà Nội', '2026-01-15', NULL, 'APPROVED'),
  ('00000000-0000-0000-0000-0000000c0302', 'PTN-02', 'SUP-004', 'SCH-HN-0051', 'Số 8 Trần Duy Hưng, Cầu Giấy, Hà Nội', '2025-11-01', NULL, 'SUSPENDED')
) AS v(id, code, partner_code, school_code, delivery_address, since, until, status)
JOIN partners p ON p.code = v.partner_code
JOIN schools  s ON s.code = v.school_code;
UPDATE code_sequences SET next_value = GREATEST(next_value, 10) WHERE scope = 'supplier_partnerships';

-- MF07 — KHÔNG thêm supplier_partnerships(CAT-001, SCH-HN-0042) tĩnh ở đây: MF05
-- (Mf05Fixtures.linkCatererToSchool, @BeforeEach) đã tự chèn đúng cặp này lúc chạy test,
-- và uq_ptn_partner_school (V6:161) là UNIQUE(partner_id, school_id) ⇒ 2 nguồn cùng chèn
-- ⇒ DuplicateKey. MF07 tự link trong @BeforeEach của test mình (cùng khuôn MF05), xem
-- InspectionSessionFlowIT.

-- MF04 — tránh trùng mã khi test POST không gửi `id` (D5)
INSERT INTO code_sequences (scope, next_value) VALUES ('partners', 200)
ON CONFLICT (scope) DO UPDATE SET next_value = EXCLUDED.next_value;

-- ═══════════════════════════════════════════════════════════════════════════════
-- MF12 — Medical Staff của trường B (SCH-HN-0051). Trước đây trường B chỉ có
-- school-admin (U-0018)/teacher (U-0019) — không đủ tài khoản để test D11 role-shape
-- parity xuyên 2 trường của `sent-reports` (MF12-TC-CON-007) hay các TC boundary khác
-- cần "cùng vai trò, khác trường". Mã đặt XA watermark hiện có (23 sau MF04) để không
-- va với mã do CodeGenerator sinh động trong bất kỳ test nào khác.
-- ⚠️ account_scope='SCHOOL_STAFF' (không 'PLATFORM') để KHÔNG đổi total của
-- MF01 UserListIT (cùng lý do U-0021 ở trên) — GET /users chỉ đếm PLATFORM.
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO user_accounts
 (code, staff_code, account_scope, name, email, phone, password_hash, role_id, school_id,
  status, email_verified, failed_login_count, terms_accepted_at)
SELECT 'U-0090', 'STF-190', 'SCHOOL_STAFF', 'Đinh Thị Nhung', 'nhungdt@ns.edu.vn', '0917444555',
       '$2a$12$K19BeUNTqLNKiuAsHIkEsu8GBU6eRu.V0UmpNU5wFGevBNHMJuIo.', r.id, s.id,
       'ACTIVE', TRUE, 0, TIMESTAMPTZ '2026-01-10T02:00:00Z'
FROM roles r, schools s
WHERE r.role_key = 'medical-staff' AND s.code = 'SCH-HN-0051';

UPDATE code_sequences SET next_value = GREATEST(next_value, 91) WHERE scope = 'users';
