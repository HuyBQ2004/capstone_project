-- ═══════════════════════════════════════════════════════════════════════════════
-- V2__seed_roles_permissions_policy.sql
--
-- Seed BẮT BUỘC của MF01 (TDS §11.2 Pre-Migration Checklist):
--   · 7 role hệ thống (system=true) — khớp ROLE_RECORDS của mock-data.ts
--   · 16 permission (PERM-001…PERM-016) — danh mục cố định, chỉ đọc (UC-129)
--   · 1 SecurityPolicy mặc định SEC-DEFAULT (5 / 15' / 30' / 8, các cờ = true)
--
-- ⚠️ Chuyển đổi mock → hợp đồng làm MỘT LẦN tại đây (Team Plan §0, TestSpec §2 L12):
--     nhãn BR-007 "School Administration" → RoleKey kebab-case "school-admin" (D6)
--     "01/07/2026 08:15" (dd/MM/yyyy) → '2026-07-01T01:15:00Z' (ISO-8601, D4)
--
-- KHÔNG seed user ở đây. Dữ liệu tài khoản là PII; seed test nằm ở db/testseed
-- (chỉ nạp với profile `test`), seed dev nằm ở V900 (tùy chọn, không chạy trên prod).
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── 16 Permission (UC-129 — danh mục cố định) ────────────────────────────────
INSERT INTO permissions (code, name, module, description) VALUES
 ('PERM-001', 'user.create',           'User Management',       'Tạo tài khoản người dùng (UC-026)'),
 ('PERM-002', 'user.toggle-status',    'User Management',       'Kích hoạt / vô hiệu hóa tài khoản (UC-003)'),
 ('PERM-003', 'role.manage',           'User Management',       'Tạo/sửa/xóa vai trò tùy chỉnh (UC-022/098/029)'),
 ('PERM-004', 'school.view',           'School Management',     'Xem danh sách & hồ sơ trường (UC-140/122)'),
 ('PERM-005', 'school.update',         'School Management',     'Cập nhật hồ sơ trường, mở khóa tọa độ GPS (UC-095, BR-028)'),
 ('PERM-006', 'school.toggle-status',  'School Management',     'Kích hoạt / vô hiệu hóa trường — cascade (UC-001, BR-021)'),
 ('PERM-007', 'classroom.manage',      'School Management',     'Quản lý lớp học, phân công GVCN (UC-018/090/008)'),
 ('PERM-008', 'student.manage',        'Student & Parent',      'Quản lý hồ sơ học sinh, liên kết phụ huynh (UC-025/099, BR-030)'),
 ('PERM-009', 'menu.approve',          'Menu Management',       'Phê duyệt / từ chối thực đơn (UC-006, BR-068)'),
 ('PERM-010', 'partnership.approve',   'Partnership',           'Duyệt hồ sơ NCC / đơn vị suất ăn (UC-007/004)'),
 ('PERM-011', 'incident.close',        'Incident Management',   'Đóng sự cố sau khi CAPA hoàn thành (UC-013, BR-155)'),
 ('PERM-012', 'violation.approve',     'Violation Management',  'Phê duyệt biên bản xử lý vi phạm (UC-182, BR-186)'),
 ('PERM-013', 'report.view',           'Reporting & Analytics', 'Xem báo cáo trong phạm vi được cấp (UC-171, BR-175/176)'),
 ('PERM-014', 'report.export',         'Reporting & Analytics', 'Xuất báo cáo PDF/Excel (UC-172, BR-178)'),
 ('PERM-015', 'audit.view',            'System',                'Truy vấn audit log bất biến (UC-106, BR-162)'),
 ('PERM-016', 'system.configure',      'System',                'Cấu hình bảo mật, tích hợp ngoài (UC-184/040)');

-- ─── 7 Role hệ thống (BR-007, BR-008 system=true bất khả xóa/sửa tên) ─────────
-- Cột `name` giữ nhãn BR-007 (hiển thị); cột `role_key` là giá trị D6 kebab-case
-- dùng trong JWT/JSON/URL. Hai thứ khác nhau — TestSpec §2 L4.
INSERT INTO roles (code, name, role_key, description, system, permissions) VALUES
 ('ROLE-01', 'System Admin',          'system-admin',     'Quản trị toàn nền tảng', TRUE,
  '["PERM-001","PERM-002","PERM-003","PERM-004","PERM-005","PERM-006","PERM-015","PERM-016"]'::jsonb),
 ('ROLE-02', 'School Administration', 'school-admin',     'Ban giám hiệu — phê duyệt & quản lý cấp trường', TRUE,
  '["PERM-007","PERM-008","PERM-009","PERM-010","PERM-011","PERM-012","PERM-013","PERM-014","PERM-015"]'::jsonb),
 ('ROLE-03', 'Medical Staff',         'medical-staff',    'Y tế trường — xác minh sức khỏe, giám sát ATTP', TRUE,
  '["PERM-008","PERM-013"]'::jsonb),
 ('ROLE-04', 'Service Caterers',      'service-caterers', 'Đơn vị suất ăn — vận hành bếp', TRUE,
  '["PERM-013"]'::jsonb),
 ('ROLE-05', 'Teacher',               'teacher',          'Giáo viên chủ nhiệm — phân phát bữa ăn', TRUE,
  '["PERM-013"]'::jsonb),
 ('ROLE-06', 'Parent',                'parent',           'Phụ huynh — khai báo & theo dõi', TRUE,
  '[]'::jsonb),
 ('ROLE-07', 'Raw Food Supplier',     'supplier',         'Nhà cung cấp nguyên liệu', TRUE,
  '[]'::jsonb);

-- ─── SecurityPolicy mặc định (TDS §11.1: SEC-DEFAULT 5/15/30/8, các cờ = true) ─
-- Mọi tham số BR-002/004/006 đọc từ bảng này. TDS §17 C3: KHÔNG hardcode trong code.
INSERT INTO security_policies (
    code, max_login_attempts, lockout_minutes, session_timeout_minutes,
    password_min_length, require_special_char, require_number, require_mixed_case,
    no_reuse_last3, active, updated_at, updated_by
) VALUES (
    'SEC-DEFAULT', 5, 15, 30, 8, TRUE, TRUE, TRUE, TRUE, TRUE,
    -- mock ghi "01/07/2026 08:15" (giờ VN) → ISO-8601 UTC (D4)
    '2026-07-01T01:15:00Z', 'system'
);

-- ─── Bộ đếm sinh mã nghiệp vụ (D1) ────────────────────────────────────────────
INSERT INTO code_sequences (scope, next_value) VALUES
 ('users', 1),
 ('staff', 1),
 ('roles', 9);   -- ROLE-01..ROLE-08 đã dùng; role custom tiếp theo là ROLE-09
