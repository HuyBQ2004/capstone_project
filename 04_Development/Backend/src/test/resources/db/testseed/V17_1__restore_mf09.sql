-- ═══════════════════════════════════════════════════════════════════════════════
-- V17_1__restore_mf09.sql — KHÔI PHỤC FIXTURE NGHIỆP VỤ, CHỈ CHO MÔI TRƯỜNG TEST
--
-- ⚠️ Nằm ở src/test/resources ⇒ KHÔNG BAO GIỜ lên classpath của prod.
--
-- Nguồn: nguyên văn các câu lệnh bị cắt khỏi `V17__seed_mf09.sql` ở commit a272a7d
-- (08/08/2026 — "xoa du lieu nghiep vu khoi seed migration, dung du lieu bang tay").
-- Quyết định với PRODUCTION giữ nguyên, KHÔNG bị đảo: file này chỉ tồn tại trong test.
--
-- Vì sao cần: suite Integration/Security dựng trên chính đám fixture đó, mà
-- `AbstractIamIntegrationTest` clean() + migrate() lại schema TRƯỚC MỖI test — nên từ
-- 08/08 mọi IT chạy trên DB rỗng và đỏ hàng loạt ở bước setup.
--
-- Vì sao đánh số 17_1 (không phải 9xx): Flyway trộn `db/migration` và `db/testseed` rồi sắp
-- theo version, nên số này đặt file vào ĐÚNG vị trí cũ trong dãy — chạy trên schema đương
-- thời và để các migration sau tự xử lý (V44 backfill `grade_id`, V71 backfill `code`…).
-- Đặt ở cuối dãy sẽ phải sửa tay từng câu lệnh cho khớp schema cuối, dễ sai hơn nhiều.
--
-- ⚠️ 12/08/2026 — tài khoản GVCN synthetic (`U-9907`, id `...446`) ĐÃ CHUYỂN sang
-- `V16_1__restore_teacher_mf09.sql` (đứng TRƯỚC V17). Lý do: V17 có
-- `UPDATE classrooms SET homeroom_teacher_id = '...446' ...` — nếu tài khoản đó vẫn insert ở
-- đây (sau V17), UPDATE đó fail FK vì bảng còn chưa có dòng. Chặn TOÀN BỘ `*IT.java` của dự án
-- ở bước `@BeforeEach`, không riêng MF09. KHÔNG insert lại ở đây — một nguồn sự thật ở V16_1.
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO menus (id, code, school_id, week_label, name, grade, menu_date, meal_type, status,
                    audience, approved_by_user_id, approved_at)
SELECT '99999999-9999-9999-9999-999999990902'::uuid, 'MENU-2026-0902', s.id, 'Tuần 27',
       'Thực đơn trưa 07/07', 'Khối 3', DATE '2026-07-07', 'LUNCH', 'PUBLISHED', 'ALL', u.id,
       TIMESTAMPTZ '2026-07-07T00:00:00Z'
FROM schools s, user_accounts u
WHERE s.code = 'SCH-MF03-0001' AND u.code = 'U-9902'
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO menu_dishes (menu_id, dish_id, serving_order, portions)
SELECT m.id, d.id, v.serving_order, 45
FROM (VALUES
    ('MENU-MF06-DEMO', 'com-trang', 1),
    ('MENU-MF06-DEMO', 'canh-bi-do-nau-tom', 2),
    ('MENU-2026-0902', 'com-trang', 1),
    ('MENU-2026-0902', 'canh-bi-do-chay', 2)
) AS v(menu_code, dish_code, serving_order)
JOIN menus m ON m.code = v.menu_code
JOIN dishes d ON d.code = v.dish_code
ON CONFLICT (menu_id, dish_id) DO NOTHING;

INSERT INTO student_distributions
    (id, code, school_id, date, meal_type, student_id, student_name, class_name, classroom_id,
     menu_id, caterer_partner_id, caterer_name, status, served_at, served_by_user_id)
SELECT v.id, v.code, s.id, v.date, 'LUNCH', st.id, st.name, '3A', st.classroom_id,
       m.id, p.id, p.name, v.status, v.served_at,
       CASE WHEN v.status = 'SERVED' THEN t.id ELSE NULL END
FROM (VALUES
    ('99999999-9999-9999-9999-999999990911'::uuid, 'DSTU-0706A-001', 'HS-3A-001', DATE '2026-07-06',
        'MENU-MF06-DEMO', 'SERVED', TIMESTAMPTZ '2026-07-06T03:53:00Z'),
    ('99999999-9999-9999-9999-999999990912'::uuid, 'DSTU-0706A-002', 'HS-3A-002', DATE '2026-07-06',
        'MENU-MF06-DEMO', 'PENDING', NULL),
    ('99999999-9999-9999-9999-999999990913'::uuid, 'DSTU-0707A-005', 'HS-3A-005', DATE '2026-07-07',
        'MENU-2026-0902', 'PENDING', NULL)
) AS v(id, code, student_code, date, menu_code, status, served_at)
JOIN students st ON st.code = v.student_code
CROSS JOIN (SELECT id FROM schools WHERE code = 'SCH-MF03-0001') s
JOIN menus m ON m.code = v.menu_code
CROSS JOIN (SELECT id, name FROM partners WHERE code = 'CAT-101') p
CROSS JOIN (SELECT id FROM user_accounts WHERE code = 'U-9907') t
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO serving_dishes (id, code, school_id, distribution_id, dish_id, dish_name,
                             is_substitute, substituted_for_dish_name, reason)
SELECT v.id, v.code, s.id, dist.id, d.id, d.name, v.is_substitute, v.substituted_for, v.reason
FROM (VALUES
    ('99999999-9999-9999-9999-999999990921'::uuid, 'SDSH-0706A-001-1', 'com-trang', FALSE, NULL, NULL),
    ('99999999-9999-9999-9999-999999990922'::uuid, 'SDSH-0706A-001-2', 'canh-bi-do-chay', TRUE,
        'Canh bí đỏ nấu tôm', 'Dị ứng Tôm')
) AS v(id, code, dish_code, is_substitute, substituted_for, reason)
CROSS JOIN (SELECT id FROM schools WHERE code = 'SCH-MF03-0001') s
JOIN student_distributions dist ON dist.code = 'DSTU-0706A-001' AND dist.school_id = s.id
JOIN dishes d ON d.code = v.dish_code;

INSERT INTO meal_consumption_records
    (id, code, school_id, date, meal_type, student_id, student_name, class_name, dishes_label,
     special_diet, special_meal_served, consumption, recorded_at, recorded_by_user_id)
SELECT v.id, v.code, s.id, DATE '2026-07-06', 'LUNCH', st.id, st.name, '3A', v.dishes_label,
       v.special_diet, v.special_meal_served, v.consumption, v.recorded_at, t.id
FROM (VALUES
    ('99999999-9999-9999-9999-999999990931'::uuid, 'MR-TD-3A-004', 'HS-3A-001',
        'Cơm trắng, Canh bí đỏ chay (suất thay)', 'Không tôm', 'Canh bí đỏ chay',
        'FULLY', TIMESTAMPTZ '2026-07-06T04:10:00Z')
) AS v(id, code, student_code, dishes_label, special_diet, special_meal_served, consumption, recorded_at)
JOIN students st ON st.code = v.student_code
CROSS JOIN (SELECT id FROM schools WHERE code = 'SCH-MF03-0001') s
CROSS JOIN (SELECT id FROM user_accounts WHERE code = 'U-9907') t
ON CONFLICT (school_id, code) DO NOTHING;
