-- ═══════════════════════════════════════════════════════════════════════════════
-- V47_1__restore_mf07_inspection.sql — KHÔI PHỤC FIXTURE NGHIỆP VỤ, CHỈ CHO MÔI TRƯỜNG TEST
--
-- ⚠️ Nằm ở src/test/resources ⇒ KHÔNG BAO GIỜ lên classpath của prod.
--
-- Nguồn: nguyên văn các câu lệnh bị cắt khỏi `V47__seed_mf07_three_step_inspection.sql` ở commit a272a7d
-- (08/08/2026 — "xoa du lieu nghiep vu khoi seed migration, dung du lieu bang tay").
-- Quyết định với PRODUCTION giữ nguyên, KHÔNG bị đảo: file này chỉ tồn tại trong test.
--
-- Vì sao cần: suite Integration/Security dựng trên chính đám fixture đó, mà
-- `AbstractIamIntegrationTest` clean() + migrate() lại schema TRƯỚC MỖI test — nên từ
-- 08/08 mọi IT chạy trên DB rỗng và đỏ hàng loạt ở bước setup.
--
-- Vì sao đánh số 47_1 (không phải 9xx): Flyway trộn `db/migration` và `db/testseed` rồi sắp
-- theo version, nên số này đặt file vào ĐÚNG vị trí cũ trong dãy — chạy trên schema đương
-- thời và để các migration sau tự xử lý (V44 backfill `grade_id`, V71 backfill `code`…).
-- Đặt ở cuối dãy sẽ phải sửa tay từng câu lệnh cho khớp schema cuối, dễ sai hơn nhiều.
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO menus (
    id, code, school_id, week_label, name, grade_id, menu_date, meal_type,
    status, audience, estimated_cost, cost_ceiling, servings, approved_by_user_id, approved_at
)
SELECT
    v.id, v.code, s.id, v.week_label, v.name, g.id, v.menu_date, v.meal_type,
    'PUBLISHED', 'ALL', 25000.00, 30000.00, v.servings, u.id, TIMESTAMPTZ '2026-08-01T00:00:00Z'
FROM (VALUES
    -- Menu Hôm nay (06/08/2026) — Bữa trưa
    ('77777777-7777-7777-7777-777777777101'::uuid, 'MENU-20260806-L', 'Tuần 1 Tháng 8', 'Thực đơn Bữa Trưa 06/08', DATE '2026-08-06', 'LUNCH', 45),
    -- Menu Hôm nay (06/08/2026) — Bữa sáng
    ('77777777-7777-7777-7777-777777777102'::uuid, 'MENU-20260806-B', 'Tuần 1 Tháng 8', 'Thực đơn Bữa Sáng 06/08', DATE '2026-08-06', 'BREAKFAST', 30),
    -- Menu Ngày mai (07/08/2026) — Bữa trưa
    ('77777777-7777-7777-7777-777777777103'::uuid, 'MENU-20260807-L', 'Tuần 1 Tháng 8', 'Thực đơn Bữa Trưa 07/08', DATE '2026-08-07', 'LUNCH', 45),
    -- Menu Ngày mai (07/08/2026) — Bữa sáng
    ('77777777-7777-7777-7777-777777777104'::uuid, 'MENU-20260807-B', 'Tuần 1 Tháng 8', 'Thực đơn Bữa Sáng 07/08', DATE '2026-08-07', 'BREAKFAST', 30),
    -- Menu Hôm qua (05/08/2026) — Bữa trưa
    ('77777777-7777-7777-7777-777777777105'::uuid, 'MENU-20260805-L', 'Tuần 1 Tháng 8', 'Thực đơn Bữa Trưa 05/08', DATE '2026-08-05', 'LUNCH', 45)
) AS v(id, code, week_label, name, menu_date, meal_type, servings)
CROSS JOIN (SELECT id FROM schools WHERE code IN ('SCH-MF03-0001','SCH-MF05-0001','SCH-MF07-0001') LIMIT 1) s
JOIN grades g ON g.school_id = s.id AND g.name = 'Khối 1'
CROSS JOIN (SELECT id FROM user_accounts WHERE code IN ('U-9902','U-0001') LIMIT 1) u
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO menu_dishes (menu_id, dish_id, serving_order, portions)
SELECT (SELECT id FROM menus WHERE code = 'MENU-20260806-L' AND school_id = s.id LIMIT 1), d.id, v.ord, 45
FROM (VALUES
    ('com-trang', 1),
    ('thit-kho-trung-cut', 2),
    ('canh-bi-do-nau-tom', 3),
    ('su-su-xao', 4)
) AS v(code, ord)
JOIN dishes d ON d.code = v.code
CROSS JOIN (SELECT id FROM schools WHERE code IN ('SCH-MF03-0001','SCH-MF05-0001','SCH-MF07-0001') LIMIT 1) s
ON CONFLICT (menu_id, dish_id) DO NOTHING;

INSERT INTO menu_dishes (menu_id, dish_id, serving_order, portions)
SELECT (SELECT id FROM menus WHERE code = 'MENU-20260807-L' AND school_id = s.id LIMIT 1), d.id, v.ord, 45
FROM (VALUES
    ('com-trang', 1),
    ('ga-rang-gung', 2),
    ('canh-bi-do-nau-tom', 3),
    ('su-su-xao', 4)
) AS v(code, ord)
JOIN dishes d ON d.code = v.code
CROSS JOIN (SELECT id FROM schools WHERE code IN ('SCH-MF03-0001','SCH-MF05-0001','SCH-MF07-0001') LIMIT 1) s
ON CONFLICT (menu_id, dish_id) DO NOTHING;


INSERT INTO inventory_batches (
    id, code, school_id, ingredient_id, ingredient_label, supplier_id,
    initial_quantity, unit, storage_zone, received_date, expiry_date
)
SELECT
    v.id, v.code, s.id, i.id, i.name, p.id,
    v.qty, i.unit, i.storage_zone, DATE '2026-08-01', v.exp_date
FROM (VALUES
    ('77777777-7777-7777-7777-777777777201'::uuid, 'BATCH-20260801-GAO',   'ING-GAO',          500.00, DATE '2027-06-01'),
    ('77777777-7777-7777-7777-777777777202'::uuid, 'BATCH-20260801-LON',   'ING-THIT-LON-VAI', 150.00, DATE '2026-08-20'),
    ('77777777-7777-7777-7777-777777777203'::uuid, 'BATCH-20260801-GA',    'ING-THIT-GA',      100.00, DATE '2026-08-20'),
    ('77777777-7777-7777-7777-777777777204'::uuid, 'BATCH-20260801-BASA',  'ING-CA-BASA',      120.00, DATE '2026-09-01'),
    ('77777777-7777-7777-7777-777777777205'::uuid, 'BATCH-20260801-TOM',   'ING-TOM',           50.00, DATE '2026-08-25'),
    ('77777777-7777-7777-7777-777777777206'::uuid, 'BATCH-20260801-TRUNG', 'ING-TRUNG-CUT',   2000.00, DATE '2026-08-25'),
    ('77777777-7777-7777-7777-777777777207'::uuid, 'BATCH-20260801-RAU',   'ING-RAU-CAI-NGOT', 150.00, DATE '2026-08-15'),
    ('77777777-7777-7777-7777-777777777208'::uuid, 'BATCH-20260801-DAUAN', 'ING-DAU-AN',       200.00, DATE '2027-12-01'),
    ('77777777-7777-7777-7777-777777777209'::uuid, 'BATCH-20260801-DAUPHU','ING-DAU-PHU',       80.00, DATE '2026-08-15')
) AS v(id, code, ing_code, qty, exp_date)
JOIN catalog_ingredients i ON i.code = v.ing_code
CROSS JOIN (SELECT id FROM schools WHERE code IN ('SCH-MF03-0001','SCH-MF05-0001','SCH-MF07-0001') LIMIT 1) s
CROSS JOIN (SELECT id FROM partners WHERE code IN ('SUP-101','CAT-101') LIMIT 1) p
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO inventory_transactions (
    id, code, school_id, occurred_at, type, batch_id, ingredient_id, delta, unit, performed_by_user_id
)
SELECT
    gen_random_uuid(), 'TXN-RECV-' || b.code, b.school_id, TIMESTAMPTZ '2026-08-01T06:00:00Z',
    'RECEIPT', b.id, b.ingredient_id, b.initial_quantity, b.unit, u.id
FROM inventory_batches b
CROSS JOIN (SELECT id FROM user_accounts WHERE code IN ('U-9906','U-0003') LIMIT 1) u
WHERE b.code LIKE 'BATCH-20260801-%'
ON CONFLICT (school_id, code) DO NOTHING;


INSERT INTO inspection_sessions (
    id, code, school_id, partner_id, date, meal_type, menu_id,
    step1, step2, step3, overall, core_temp, holding_temp,
    inspector_user_id, decided_at, staff_notes,
    medical_review_result, medical_review_reason, medical_review_at
)
SELECT
    v.id, v.code, s.id, p.id, v.date, v.meal_type,
    (SELECT id FROM menus WHERE code = v.menu_code AND school_id = s.id LIMIT 1),
    v.step1, v.step2, v.step3, v.overall, v.core_temp, v.holding_temp,
    u.id, v.decided_at, v.staff_notes,
    v.medical_review_result, v.medical_review_reason, v.medical_review_at
FROM (VALUES
    -- Session 1: Hôm nay (2026-08-06) — Bữa trưa — ĐẠT toàn bộ 3 bước (PASSED)
    ('77777777-7777-7777-7777-777777777701'::uuid, 'INSP-20260806-L', DATE '2026-08-06', 'LUNCH', 'MENU-20260806-L',
     'PASSED', 'PASSED', 'PASSED', 'PASSED', 85.50, 68.50,
     TIMESTAMPTZ '2026-08-06T04:15:00Z', 'Nguyên liệu tươi ngon, chế biến đúng nhiệt độ, chia suất đảm bảo vệ sinh 1 chiều.',
     'PASSED', 'Đạt yêu cầu vệ sinh an toàn thực phẩm bữa trưa.', TIMESTAMPTZ '2026-08-06T04:30:00Z'),

    -- Session 2: Hôm nay (2026-08-06) — Bữa sáng — ĐẠT toàn bộ 3 bước (PASSED)
    ('77777777-7777-7777-7777-777777777702'::uuid, 'INSP-20260806-B', DATE '2026-08-06', 'BREAKFAST', 'MENU-20260806-B',
     'PASSED', 'PASSED', 'PASSED', 'PASSED', 82.00, 65.00,
     TIMESTAMPTZ '2026-08-06T00:30:00Z', 'Phở gà bữa sáng thơm ngon, nóng sốt.',
     'PASSED', 'Đã kiểm tra đạt chất lượng bữa sáng.', TIMESTAMPTZ '2026-08-06T01:00:00Z'),

    -- Session 3: Ngày mai (2026-08-07) — Bữa trưa — Đang kiểm thực (IN_PROGRESS: xong Bước 1 + Bước 2)
    ('77777777-7777-7777-7777-777777777703'::uuid, 'INSP-20260807-L', DATE '2026-08-07', 'LUNCH', 'MENU-20260807-L',
     'PASSED', 'PASSED', 'PENDING', 'IN_PROGRESS', 84.00, NULL,
     NULL, 'Đã hoàn thành Bước 1 Nhập kho và Bước 2 Nấu chín. Đang chờ chia suất kiểm thực Bước 3.',
     NULL, NULL, NULL),

    -- Session 4: Ngày mai (2026-08-07) — Bữa sáng — Mới khởi tạo (PENDING)
    ('77777777-7777-7777-7777-777777777704'::uuid, 'INSP-20260807-B', DATE '2026-08-07', 'BREAKFAST', 'MENU-20260807-B',
     'PENDING', 'PENDING', 'PENDING', 'PENDING', NULL, NULL,
     NULL, 'Chuẩn bị kiểm thực bữa sáng ngày 07/08.',
     NULL, NULL, NULL),

    -- Session 5: Hôm qua (2026-08-05) — Bữa trưa — KHÔNG ĐẠT tại Bước 2 (FAILED)
    ('77777777-7777-7777-7777-777777777705'::uuid, 'INSP-20260805-L', DATE '2026-08-05', 'LUNCH', 'MENU-20260805-L',
     'PASSED', 'FAILED', 'PENDING', 'FAILED', 71.00, NULL,
     TIMESTAMPTZ '2026-08-05T03:30:00Z', 'Phát hiện nhiệt độ nấu chín món gà chưa đạt 75°C. Đã yêu cầu đun lại bổ sung 15 phút.',
     'FAILED', 'Yêu cầu đun lại nhiệt độ tâm sản phẩm đạt trên 75°C trước khi cho phép chia suất.', TIMESTAMPTZ '2026-08-05T03:45:00Z')
) AS v(id, code, date, meal_type, menu_code, step1, step2, step3, overall, core_temp, holding_temp, decided_at, staff_notes, medical_review_result, medical_review_reason, medical_review_at)
CROSS JOIN (SELECT id FROM schools WHERE code IN ('SCH-MF03-0001','SCH-MF05-0001','SCH-MF07-0001') LIMIT 1) s
CROSS JOIN (SELECT id FROM partners WHERE code = 'CAT-101' LIMIT 1) p
CROSS JOIN (SELECT id FROM user_accounts WHERE code IN ('U-9906','U-0003') LIMIT 1) u
ON CONFLICT (school_id, code) DO UPDATE SET
    step1 = EXCLUDED.step1,
    step2 = EXCLUDED.step2,
    step3 = EXCLUDED.step3,
    overall = EXCLUDED.overall,
    menu_id = EXCLUDED.menu_id,
    core_temp = EXCLUDED.core_temp,
    holding_temp = EXCLUDED.holding_temp,
    decided_at = EXCLUDED.decided_at,
    staff_notes = EXCLUDED.staff_notes;


INSERT INTO step1_fresh_rows (
    id, session_id, row_order, name, batch_id, received_at, quantity,
    supplier_name, supplier_contact, deliverer,
    has_invoice, has_vet_cert, has_quarantine_cert, sensory_pass, quick_test, action, photo_count
)
SELECT
    v.id,
    (SELECT id FROM inspection_sessions WHERE code = v.insp_code AND school_id = s.id LIMIT 1),
    v.row_order, v.name,
    (SELECT id FROM inventory_batches WHERE code = v.batch_code AND school_id = s.id LIMIT 1),
    v.received_at, v.quantity, v.supplier_name, v.supplier_contact, v.deliverer,
    v.has_invoice, v.has_vet_cert, v.has_quarantine_cert, v.sensory_pass, v.quick_test, v.action, v.photo_count
FROM (VALUES
  ('77777777-7777-7777-7777-777777778101'::uuid, 'INSP-20260806-L', 1,
   'Thịt lợn nạc mông tươi', 'BATCH-20260801-LON', '06:00 06/08/2026', '25 kg',
   'Cty CP Thực phẩm sạch GreenValley', 'Phạm Văn Long — 0901234599', 'Phạm Văn Long',
   TRUE, TRUE, TRUE, TRUE, 'PASS', 'Đủ điều kiện nhập kho sơ chế', 1),

  ('77777777-7777-7777-7777-777777778102'::uuid, 'INSP-20260806-L', 2,
   'Cá basa phi lê đông lạnh', 'BATCH-20260801-BASA', '06:15 06/08/2026', '18 kg',
   'Cty CP Thực phẩm sạch GreenValley', 'Phạm Văn Long — 0901234599', 'Phạm Văn Long',
   TRUE, TRUE, TRUE, TRUE, 'PASS', 'Bảo quản tủ đông -18°C ngay sau nhận', 1),

  ('77777777-7777-7777-7777-777777778103'::uuid, 'INSP-20260806-L', 3,
   'Rau cải ngọt an toàn', 'BATCH-20260801-RAU', '06:20 06/08/2026', '30 kg',
   'Cty TNHH Nông sản Sông Hồng', 'Trần Thị Hạnh — 0901234502', 'Nguyễn Văn Nam',
   TRUE, FALSE, TRUE, TRUE, 'PASS', 'Ngâm rửa nước muối 3 lần trước khi cắt', 1),

  ('77777777-7777-7777-7777-777777778104'::uuid, 'INSP-20260806-L', 4,
   'Tôm đồng tươi sống', 'BATCH-20260801-TOM', '06:30 06/08/2026', '12 kg',
   'Cty CP Thực phẩm sạch GreenValley', 'Phạm Văn Long — 0901234599', 'Phạm Văn Long',
   TRUE, TRUE, TRUE, TRUE, 'PASS', 'Sơ chế loại bỏ vỏ tôm ngay', 1),

  -- Tươi sống cho Session 3 (07/08/2026)
  ('77777777-7777-7777-7777-777777778105'::uuid, 'INSP-20260807-L', 1,
   'Thịt gà đùi tươi lọc xương', 'BATCH-20260801-GA', '06:00 07/08/2026', '22 kg',
   'Cty CP Thực phẩm sạch GreenValley', 'Phạm Văn Long — 0901234599', 'Phạm Văn Long',
   TRUE, TRUE, TRUE, TRUE, 'PASS', 'Nhập kho bảo quản lạnh 2°C', 1)
) AS v(id, insp_code, row_order, name, batch_code, received_at, quantity, supplier_name, supplier_contact, deliverer, has_invoice, has_vet_cert, has_quarantine_cert, sensory_pass, quick_test, action, photo_count)
CROSS JOIN (SELECT id FROM schools WHERE code IN ('SCH-MF03-0001','SCH-MF05-0001','SCH-MF07-0001') LIMIT 1) s
ON CONFLICT (id) DO NOTHING;


INSERT INTO step1_dry_rows (
    id, session_id, row_order, name, batch_id, producer, producer_address,
    received_at, quantity, supplier_name, deliverer, supplier_contact,
    expiry, storage, has_invoice, sensory_pass, action, photo_count
)
SELECT
    v.id,
    (SELECT id FROM inspection_sessions WHERE code = v.insp_code AND school_id = s.id LIMIT 1),
    v.row_order, v.name,
    (SELECT id FROM inventory_batches WHERE code = v.batch_code AND school_id = s.id LIMIT 1),
    v.producer, v.producer_address, v.received_at, v.quantity, v.supplier_name, v.deliverer, v.supplier_contact,
    v.expiry, v.storage, v.has_invoice, v.sensory_pass, v.action, v.photo_count
FROM (VALUES
  ('77777777-7777-7777-7777-777777778201'::uuid, 'INSP-20260806-L', 1,
   'Gạo Tám Thơm ST25 đóng bao 25kg', 'BATCH-20260801-GAO', 'Cty THNN Lương thực Sông Hồng', 'Gia Lâm, Hà Nội',
   '06:00 06/08/2026', '50 kg (2 bao)', 'Cty TNHH Nông sản Sông Hồng', 'Nguyễn Văn Nam', '0901234502',
   DATE '2027-06-01', 'THUONG', TRUE, TRUE, 'Bao gói nguyên niêm phong, để trên kệ cao 30cm', 1),

  ('77777777-7777-7777-7777-777777778202'::uuid, 'INSP-20260806-L', 2,
   'Dầu ăn thực vật Tường An Gold 5L', 'BATCH-20260801-DAUAN', 'Cty CP Dầu thực vật Tường An', 'TP Hồ Chí Minh',
   '06:00 06/08/2026', '10 lít (2 can)', 'Cty CP Thực phẩm sạch GreenValley', 'Phạm Văn Long', '0901234501',
   DATE '2027-12-01', 'THUONG', TRUE, TRUE, 'Can nhựa niêm phong kín, để kho khô ráo', 1)
) AS v(id, insp_code, row_order, name, batch_code, producer, producer_address, received_at, quantity, supplier_name, deliverer, supplier_contact, expiry, storage, has_invoice, sensory_pass, action, photo_count)
CROSS JOIN (SELECT id FROM schools WHERE code IN ('SCH-MF03-0001','SCH-MF05-0001','SCH-MF07-0001') LIMIT 1) s
ON CONFLICT (id) DO NOTHING;


INSERT INTO step2_rows (
    id, session_id, row_order, shift, dish_name, main_ingredients, servings,
    prep_done_at, cook_done_at, hygiene_staff, hygiene_equipment, hygiene_area,
    sensory_pass, core_temp, action, photo_count
)
SELECT
    v.id,
    (SELECT id FROM inspection_sessions WHERE code = v.insp_code AND school_id = s.id LIMIT 1),
    v.row_order, v.shift, v.dish_name, v.main_ingredients, v.servings,
    v.prep_done_at, v.cook_done_at, v.hygiene_staff, v.hygiene_equipment, v.hygiene_area,
    v.sensory_pass, v.core_temp, v.action, v.photo_count
FROM (VALUES
  ('77777777-7777-7777-7777-777777778301'::uuid, 'INSP-20260806-L', 1,
   'Bữa trưa — Ca 1 (10:15)', 'Cơm trắng chín tới', 'Gạo ST25, nước sạch', '1850 suất',
   '08:30 06/08/2026', '10:15 06/08/2026', TRUE, TRUE, TRUE,
   TRUE, 88.50, 'Nấu chín dẻo thơm, chuyển sang tủ giữ nhiệt', 1),

  ('77777777-7777-7777-7777-777777778302', 'INSP-20260806-L', 2,
   'Bữa trưa — Ca 1 (10:20)', 'Thịt kho trứng cút', 'Thịt lợn nạc, trứng cút, gia vị', '45 suất',
   '08:45 06/08/2026', '10:20 06/08/2026', TRUE, TRUE, TRUE,
   TRUE, 86.00, 'Nhiệt độ tâm đun sôi đạt 86°C (>75°C), màu vàng nâu hấp dẫn', 1),

  ('77777777-7777-7777-7777-777777778303', 'INSP-20260806-L', 3,
   'Bữa trưa — Ca 1 (10:25)', 'Canh bí đỏ nấu tôm', 'Bí đỏ tươi, tôm tươi băm, hành ngò', '45 suất',
   '09:00 06/08/2026', '10:25 06/08/2026', TRUE, TRUE, TRUE,
   TRUE, 92.00, 'Đun sôi bùng đạt 92°C, nêm nếm vừa ăn', 1),

  ('77777777-7777-7777-7777-777777778304', 'INSP-20260806-L', 4,
   'Bữa trưa — Ca 1 (10:28)', 'Su su xào tỏi', 'Su su tươi, tỏi, dầu ăn', '45 suất',
   '09:15 06/08/2026', '10:28 06/08/2026', TRUE, TRUE, TRUE,
   TRUE, 82.50, 'Xào chín tới trên bếp công nghiệp đạt 82.5°C', 1),

  -- Step 2 cho Session 3 (07/08/2026)
  ('77777777-7777-7777-7777-777777778305', 'INSP-20260807-L', 1,
   'Bữa trưa — Ca 1 (10:20)', 'Gà rang gừng', 'Thịt gà đùi, gừng tươi, nước mắm', '45 suất',
   '08:40 07/08/2026', '10:20 07/08/2026', TRUE, TRUE, TRUE,
   TRUE, 85.00, 'Đun chín kỹ đạt 85°C', 1)
) AS v(id, insp_code, row_order, shift, dish_name, main_ingredients, servings, prep_done_at, cook_done_at, hygiene_staff, hygiene_equipment, hygiene_area, sensory_pass, core_temp, action, photo_count)
CROSS JOIN (SELECT id FROM schools WHERE code IN ('SCH-MF03-0001','SCH-MF05-0001','SCH-MF07-0001') LIMIT 1) s
ON CONFLICT (id) DO NOTHING;


INSERT INTO step3_rows (
    id, session_id, row_order, shift, dish_name, servings,
    divided_done_at, eat_start_at, utensils, holding_temp,
    sensory_pass, action, photo_count
)
SELECT
    v.id,
    (SELECT id FROM inspection_sessions WHERE code = v.insp_code AND school_id = s.id LIMIT 1),
    v.row_order, v.shift, v.dish_name, v.servings,
    v.divided_done_at, v.eat_start_at, v.utensils, v.holding_temp,
    v.sensory_pass, v.action, v.photo_count
FROM (VALUES
  ('77777777-7777-7777-7777-777777778401'::uuid, 'INSP-20260806-L', 1,
   'Bữa trưa — Khối 1 (11:00)', 'Cơm trắng chín tới', '45 suất',
   '10:45 06/08/2026', '11:00 06/08/2026', 'Khay inox 5 ngăn tiệt trùng bằng tủ sấy UV', 68.50,
   TRUE, 'Giữ ấm trên bàn chia khay nóng 68.5°C (>60°C)', 1),

  ('77777777-7777-7777-7777-777777778402'::uuid, 'INSP-20260806-L', 2,
   'Bữa trưa — Khối 1 (11:00)', 'Thịt kho trứng cút', '45 suất',
   '10:50 06/08/2026', '11:00 06/08/2026', 'Thìa múc inox, khay chia nắp đậy kín', 66.00,
   TRUE, 'Chia đều mỗi suất 3 quả trứng cút + 60g thịt kho', 1),

  ('77777777-7777-7777-7777-777777778403'::uuid, 'INSP-20260806-L', 3,
   'Bữa trưa — Khối 1 (11:00)', 'Canh bí đỏ nấu tôm', '45 suất',
   '10:52 06/08/2026', '11:00 06/08/2026', 'Bát canh cách nhiệt inox', 71.00,
   TRUE, 'Múc nóng sốt tại bàn ăn của từng lớp', 1),

  ('77777777-7777-7777-7777-777777778404'::uuid, 'INSP-20260806-L', 4,
   'Bữa trưa — Khối 1 (11:00)', 'Su su xào tỏi', '45 suất',
   '10:55 06/08/2026', '11:00 06/08/2026', 'Gắp nhựa nguyên sinh thực phẩm', 64.50,
   TRUE, 'Giữ độ giòn ngọt, ấm nóng đạt 64.5°C', 1)
) AS v(id, insp_code, row_order, shift, dish_name, servings, divided_done_at, eat_start_at, utensils, holding_temp, sensory_pass, action, photo_count)
CROSS JOIN (SELECT id FROM schools WHERE code IN ('SCH-MF03-0001','SCH-MF05-0001','SCH-MF07-0001') LIMIT 1) s
ON CONFLICT (id) DO NOTHING;


INSERT INTO kitchen_inspections (
    id, code, school_id, partner_id, date, score,
    inspector_user_id, pest_control_valid, staff_health_valid,
    result, confirm_status, confirmed_at, created_at
)
SELECT
    '77777777-7777-7777-7777-777777778501'::uuid, 'KIN-2026-0806', s.id, p.id, DATE '2026-08-06', 94,
    u.id, TRUE, TRUE,
    'PASSED', 'CONFIRMED', TIMESTAMPTZ '2026-08-06T02:00:00Z', TIMESTAMPTZ '2026-08-06T01:30:00Z'
FROM (SELECT id FROM schools WHERE code IN ('SCH-MF03-0001','SCH-MF05-0001','SCH-MF07-0001') LIMIT 1) s
CROSS JOIN (SELECT id FROM partners WHERE code = 'CAT-101' LIMIT 1) p
CROSS JOIN (SELECT id FROM user_accounts WHERE code IN ('U-9903','U-0002') LIMIT 1) u
ON CONFLICT (school_id, code) DO NOTHING;


INSERT INTO safety_signoffs (
    id, code, school_id, date, signed_by, signed_at, note, hygiene_score, failed_steps
)
SELECT
    '77777777-7777-7777-7777-777777778601'::uuid, 'SGN-2026-0806', s.id, DATE '2026-08-06',
    u.id, TIMESTAMPTZ '2026-08-06T05:00:00Z',
    'BGH đã duyệt toàn bộ quy trình kiểm thực 3 bước bữa trưa ngày 06/08. Đảm bảo an toàn 100%.',
    94, 0
FROM (SELECT id FROM schools WHERE code IN ('SCH-MF03-0001','SCH-MF05-0001','SCH-MF07-0001') LIMIT 1) s
CROSS JOIN (SELECT id FROM user_accounts WHERE code IN ('U-9902','U-0001') LIMIT 1) u
ON CONFLICT (school_id, date) DO NOTHING;
