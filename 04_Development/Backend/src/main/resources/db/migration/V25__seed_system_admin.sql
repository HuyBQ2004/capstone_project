-- D13 (00_Common_Conventions.md) — seed 1 tài khoản đăng nhập được cho MỖI role dùng ở
-- @PreAuthorize. `system-admin` (ROLE-01) chưa từng có tài khoản nào từ V1 tới V24, dù
-- nhiều @PreAuthorize("hasRole('SYSTEM_ADMIN')") đã merge (MF01 UserController/RoleController,
-- MF02 SchoolController, và cả loạt endpoint MF12: SystemController/IntegrationController/
-- ErrorLogController/SecurityPolicyController) — phát hiện lúc Bước 3 MF12 (04/08/2026) khi
-- cần đăng nhập thật để kiểm curl, không phải khoản nợ do MF12 tạo ra.
-- Cùng mật khẩu/hash với các tài khoản synthetic khác (`P@ssw0rd123`, xem V5).
-- 'U-9001' KHÔNG dùng — trùng fixture test-only `src/test/resources/db/testseed/V900__test_fixtures.sql`
-- (chỉ nạp khi chạy test, không có trong DB dev/prod thật — đúng lý do dev DB rỗng system-admin).
INSERT INTO user_accounts (id, code, name, email, password_hash, role_id, school_id, status, email_verified)
SELECT '44444444-4444-4444-4444-444444444450', 'U-9900', 'Nguyễn Hải Đăng (synthetic)',
       'dangnh.sysadmin.synthetic@example.test',
       '$2a$12$K19BeUNTqLNKiuAsHIkEsu8GBU6eRu.V0UmpNU5wFGevBNHMJuIo.', r.id, NULL,
       'ACTIVE', true
FROM roles r WHERE r.role_key = 'system-admin';
