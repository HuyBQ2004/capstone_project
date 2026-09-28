-- ═══════════════════════════════════════════════════════════════════════════════
-- V16_1__restore_teacher_mf09.sql — KHÔI PHỤC FIXTURE, CHỈ MÔI TRƯỜNG TEST
--
-- ⚠️ Nằm ở src/test/resources ⇒ KHÔNG BAO GIỜ lên classpath của prod.
--
-- Vì sao tách khỏi V17_1__restore_mf09.sql (đứng SAU V17): V17 có
-- `UPDATE classrooms SET homeroom_teacher_id = '44444444-4444-4444-4444-444444444446' ...`
-- — tài khoản GVCN đó (U-9907) chỉ được insert ở V17_1, đứng SAU V17 trong thứ tự Flyway
-- (17 < 17.1) ⇒ UPDATE ở V17 fail `FK classrooms_homeroom_teacher_id_fkey` vì user_accounts
-- chưa có dòng đó. Cùng lớp lỗi/cùng cách sửa như `V10_1__restore_catalog_ingredients_mf06.sql`
-- (seed phải nằm TRƯỚC migration tham chiếu nó, không phải SAU). Nội dung INSERT dưới đây
-- nguyên văn từ `V17_1__restore_mf09.sql` (đã xoá khỏi đó để tránh 2 nguồn sự thật) —
-- `ON CONFLICT (id) DO NOTHING` nên an toàn dù chạy nhiều lần.
--
-- Phụ thuộc: `schools.code = 'SCH-MF03-0001'` đã tồn tại từ `V5_1__restore_mf03.sql`
-- (5.1 < 16.1), `roles.role_key = 'teacher'` đã tồn tại từ V2 (seed roles cố định).
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO user_accounts (id, code, name, email, password_hash, role_id, school_id, status, email_verified)
SELECT '44444444-4444-4444-4444-444444444446', 'U-9907', 'Lê Thị Hương (synthetic)',
       'lethihuong.teacher.synthetic@example.test',
       '$2a$12$K19BeUNTqLNKiuAsHIkEsu8GBU6eRu.V0UmpNU5wFGevBNHMJuIo.', r.id, s.id, 'ACTIVE', true
FROM roles r, schools s
WHERE r.role_key = 'teacher' AND s.code = 'SCH-MF03-0001'
ON CONFLICT (id) DO NOTHING;
