-- ═══════════════════════════════════════════════════════════════════════════════
-- V9_1__restore_mf05.sql — KHÔI PHỤC FIXTURE NGHIỆP VỤ, CHỈ CHO MÔI TRƯỜNG TEST
--
-- ⚠️ Nằm ở src/test/resources ⇒ KHÔNG BAO GIỜ lên classpath của prod.
--
-- Nguồn: nguyên văn các câu lệnh bị cắt khỏi `V9__seed_mf05.sql` ở commit a272a7d
-- (08/08/2026 — "xoa du lieu nghiep vu khoi seed migration, dung du lieu bang tay").
-- Quyết định với PRODUCTION giữ nguyên, KHÔNG bị đảo: file này chỉ tồn tại trong test.
--
-- Vì sao cần: suite Integration/Security dựng trên chính đám fixture đó, mà
-- `AbstractIamIntegrationTest` clean() + migrate() lại schema TRƯỚC MỖI test — nên từ
-- 08/08 mọi IT chạy trên DB rỗng và đỏ hàng loạt ở bước setup.
--
-- Vì sao đánh số 9_1 (không phải 9xx): Flyway trộn `db/migration` và `db/testseed` rồi sắp
-- theo version, nên số này đặt file vào ĐÚNG vị trí cũ trong dãy — chạy trên schema đương
-- thời và để các migration sau tự xử lý (V44 backfill `grade_id`, V71 backfill `code`…).
-- Đặt ở cuối dãy sẽ phải sửa tay từng câu lệnh cho khớp schema cuối, dễ sai hơn nhiều.
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO dishes (id, code, name, category, kcal, protein, fat, carb, manual_allergens, is_active) VALUES
 ('55555555-5555-5555-5555-555555555101', 'com-trang',           'Cơm trắng',                'MON_CHINH', 130, 2.7, 0.3, 28, '[]', TRUE),
 ('55555555-5555-5555-5555-555555555102', 'canh-mong-toi',       'Canh mồng tơi',            'CANH',      35,  2.0, 0.5, 5,  '[]', TRUE),
 ('55555555-5555-5555-5555-555555555103', 'thit-kho-hanh',       'Thịt kho hành',            'MON_MAN',   250, 18,  17,  4,  '[]', TRUE),
 ('55555555-5555-5555-5555-555555555104', 'su-su-xao',           'Su su xào',                'RAU',       60,  1.5, 3,   7,  '[]', TRUE),
 ('55555555-5555-5555-5555-555555555106', 'dau-phu-sot-ca-chua', 'Đậu phụ sốt cà chua',      'MON_MAN',   170, 9,   8,   12, '["Đậu nành"]', TRUE),
 ('55555555-5555-5555-5555-555555555107', 'canh-bi-do-nau-tom',  'Canh bí đỏ nấu tôm',       'CANH',      70,  4,   2,   9,  '["Tôm"]', TRUE),
 ('55555555-5555-5555-5555-555555555108', 'canh-bi-do-chay',     'Canh bí đỏ chay',          'CANH',      45,  1.5, 1,   9,  '[]', TRUE),
 ('55555555-5555-5555-5555-555555555109', 'trai-cay-theo-mua',   'Trái cây theo mùa',        'TRANG_MIENG', 60, 0.5, 0.2, 15, '[]', TRUE);

INSERT INTO dishes (id, code, name, category, kcal, protein, fat, carb, manual_allergens, is_active, default_substitute_id) VALUES
 ('55555555-5555-5555-5555-555555555105', 'ca-basa-sot-ca-chua', 'Cá basa sốt cà chua', 'MON_MAN', 190, 17, 11, 5, '["Cá"]', TRUE,
  '55555555-5555-5555-5555-555555555106');

INSERT INTO dish_ingredients (dish_id, ingredient_code, name, quantity, unit) VALUES
 ('55555555-5555-5555-5555-555555555105', 'ING-CA-BASA', 'Cá basa phi lê', '60', 'g'),
 ('55555555-5555-5555-5555-555555555105', NULL,          'Cà chua',        '30', 'g'),
 ('55555555-5555-5555-5555-555555555105', NULL,          'Hành lá',        '2',  'g'),
 ('55555555-5555-5555-5555-555555555106', 'ING-DAU-PHU', 'Đậu phụ',        '80', 'g'),
 ('55555555-5555-5555-5555-555555555106', NULL,          'Cà chua',        '30', 'g'),
 ('55555555-5555-5555-5555-555555555101', 'ING-GAO',     'Gạo tẻ',         '100','g'),
 ('55555555-5555-5555-5555-555555555102', NULL,          'Rau mồng tơi',   '100','g'),
 ('55555555-5555-5555-5555-555555555103', NULL,          'Thịt ba chỉ',    '80', 'g'),
 ('55555555-5555-5555-5555-555555555103', NULL,          'Hành khô',       '10', 'g'),
 ('55555555-5555-5555-5555-555555555104', NULL,          'Su su',          '150','g'),
 ('55555555-5555-5555-5555-555555555107', NULL,          'Bí đỏ',          '100','g'),
 ('55555555-5555-5555-5555-555555555107', 'ING-TOM',     'Tôm',            '30', 'g'),
 ('55555555-5555-5555-5555-555555555108', NULL,          'Bí đỏ',          '100','g'),
 ('55555555-5555-5555-5555-555555555109', NULL,          'Trái cây',       '100','g');

INSERT INTO schools (id, code, name, tax_id, status)
VALUES ('55555555-5555-5555-5555-555555555900', 'SCH-MF05-0001', 'TH Đoàn Thị Điểm (MF05 seed)', '0100000005', 'ACTIVE')
ON CONFLICT (code) DO NOTHING;

INSERT INTO menu_templates (id, code, school_id, name, meal_type, audience, note, is_active) VALUES
 ('55555555-5555-5555-5555-555555555201', 'TPL-STD-A', '55555555-5555-5555-5555-555555555900', 'Thực đơn tiêu chuẩn A', 'LUNCH', 'ALL', 'Thực đơn trưa tiêu chuẩn', TRUE),
 ('55555555-5555-5555-5555-555555555202', 'TPL-STD-B', '55555555-5555-5555-5555-555555555900', 'Thực đơn tiêu chuẩn B', 'LUNCH', 'ALL', 'Nấu nồi riêng, không dùng chung muôi với món mặn.', TRUE),
 ('55555555-5555-5555-5555-555555555203', 'TPL-STD-C', '55555555-5555-5555-5555-555555555900', 'Thực đơn tiêu chuẩn C', 'LUNCH', 'ALL', NULL, TRUE),
 ('55555555-5555-5555-5555-555555555204', 'TPL-STD-D', '55555555-5555-5555-5555-555555555900', 'Thực đơn tiêu chuẩn D', 'LUNCH', 'ALL', NULL, TRUE),
 ('55555555-5555-5555-5555-555555555205', 'TPL-BREAKFAST-A', '55555555-5555-5555-5555-555555555900', 'Bữa sáng tiêu chuẩn A', 'BREAKFAST', 'ALL', NULL, TRUE),
 ('55555555-5555-5555-5555-555555555206', 'TPL-SNACK-A', '55555555-5555-5555-5555-555555555900', 'Bữa phụ tiêu chuẩn A', 'SNACK', 'ALL', NULL, TRUE),
 ('55555555-5555-5555-5555-555555555207', 'TPL-VEGETARIAN-A', '55555555-5555-5555-5555-555555555900', 'Thực đơn chay A', 'LUNCH', 'VEGETARIAN', 'Dành riêng cho nhóm ăn chay', TRUE),
 ('55555555-5555-5555-5555-555555555208', 'TPL-STD-OLD', '55555555-5555-5555-5555-555555555900', 'Thực đơn cũ (ngừng dùng)', 'LUNCH', 'ALL', 'Ngừng sử dụng — giữ lại để tra cứu lịch sử', FALSE);

INSERT INTO menu_template_dishes (template_id, dish_id, serving_order) VALUES
 ('55555555-5555-5555-5555-555555555202', '55555555-5555-5555-5555-555555555105', 1),
 ('55555555-5555-5555-5555-555555555202', '55555555-5555-5555-5555-555555555101', 2),
 ('55555555-5555-5555-5555-555555555202', '55555555-5555-5555-5555-555555555102', 3),
 ('55555555-5555-5555-5555-555555555201', '55555555-5555-5555-5555-555555555101', 1),
 ('55555555-5555-5555-5555-555555555201', '55555555-5555-5555-5555-555555555103', 2),
 ('55555555-5555-5555-5555-555555555201', '55555555-5555-5555-5555-555555555104', 3),
 ('55555555-5555-5555-5555-555555555207', '55555555-5555-5555-5555-555555555101', 1),
 ('55555555-5555-5555-5555-555555555207', '55555555-5555-5555-5555-555555555108', 2),
 ('55555555-5555-5555-5555-555555555207', '55555555-5555-5555-5555-555555555104', 3);
