-- ═══════════════════════════════════════════════════════════════════════════════
-- V11_1__restore_mf06.sql — KHÔI PHỤC FIXTURE NGHIỆP VỤ, CHỈ CHO MÔI TRƯỜNG TEST
--
-- ⚠️ Nằm ở src/test/resources ⇒ KHÔNG BAO GIỜ lên classpath của prod.
--
-- Nguồn: nguyên văn các câu lệnh bị cắt khỏi `V11__seed_mf06.sql` ở commit a272a7d
-- (08/08/2026 — "xoa du lieu nghiep vu khoi seed migration, dung du lieu bang tay").
-- Quyết định với PRODUCTION giữ nguyên, KHÔNG bị đảo: file này chỉ tồn tại trong test.
--
-- Vì sao cần: suite Integration/Security dựng trên chính đám fixture đó, mà
-- `AbstractIamIntegrationTest` clean() + migrate() lại schema TRƯỚC MỖI test — nên từ
-- 08/08 mọi IT chạy trên DB rỗng và đỏ hàng loạt ở bước setup.
--
-- Vì sao đánh số 11_1 (không phải 9xx): Flyway trộn `db/migration` và `db/testseed` rồi sắp
-- theo version, nên số này đặt file vào ĐÚNG vị trí cũ trong dãy — chạy trên schema đương
-- thời và để các migration sau tự xử lý (V44 backfill `grade_id`, V71 backfill `code`…).
-- Đặt ở cuối dãy sẽ phải sửa tay từng câu lệnh cho khớp schema cuối, dễ sai hơn nhiều.
--
-- ⚠️ 12/08/2026 — seed `catalog_ingredients` ĐÃ CHUYỂN sang `V10_1__restore_catalog_
-- ingredients_mf06.sql` (đứng TRƯỚC V11). Lý do: V11 có `ALTER TABLE dish_ingredients ADD
-- CONSTRAINT fk_dish_ing_catalog` là câu lệnh ĐẦU TIÊN của nó — nếu seed `catalog_ingredients`
-- vẫn nằm ở V11_1 (sau V11), FK đó fail ngay vì bảng còn rỗng lúc ALTER chạy (trong khi
-- `V9_1__restore_mf05.sql` đã insert `dish_ingredients` tham chiếu 4 mã ING-* từ trước). Chặn
-- TOÀN BỘ `*IT.java` của dự án ở bước `@BeforeEach`, không riêng MF05/MF06. KHÔNG insert lại
-- ở đây — một nguồn sự thật duy nhất ở V10_1.
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO user_accounts (id, code, name, email, password_hash, role_id, school_id, status, email_verified)
SELECT '66666666-6666-6666-6666-666666666701', 'U-9906', 'Hoàng Minh Đức (MF06 synthetic)',
       'duchm.caterer.synthetic@example.test',
       '$2a$12$K19BeUNTqLNKiuAsHIkEsu8GBU6eRu.V0UmpNU5wFGevBNHMJuIo.', r.id,
       '11111111-1111-1111-1111-111111111111', 'ACTIVE', true
FROM roles r WHERE r.role_key = 'service-caterers'
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO user_accounts (id, code, name, email, password_hash, role_id, school_id, status, email_verified)
SELECT '66666666-6666-6666-6666-666666666702', 'U-9907', 'Lý Thái Sơn (MF06 synthetic)',
       'sonlt.supplier.synthetic@example.test',
       '$2a$12$K19BeUNTqLNKiuAsHIkEsu8GBU6eRu.V0UmpNU5wFGevBNHMJuIo.', r.id, NULL, 'ACTIVE', true
FROM roles r WHERE r.role_key = 'supplier'
ON CONFLICT (code) WHERE school_id IS NULL DO NOTHING;
INSERT INTO supplier_products (id, code, partner_id, ingredient_id, unit_price) VALUES
 ('66666666-6666-6666-6666-666666666801', 'SP-MF06-001', '99999999-9999-9999-9999-999999990001',
  '66666666-6666-6666-6666-666666666605', 125000),
 ('66666666-6666-6666-6666-666666666802', 'SP-MF06-002', '99999999-9999-9999-9999-999999990001',
  '66666666-6666-6666-6666-666666666609', 42000),
 ('66666666-6666-6666-6666-666666666803', 'SP-MF06-003', '99999999-9999-9999-9999-999999990001',
  '66666666-6666-6666-6666-666666666607', 210000)
ON CONFLICT (partner_id, code) DO NOTHING;

INSERT INTO purchase_requests (id, code, school_id, supplier_id, requested_date, delivery_date,
                                total_value, status, quoted_at, price_accepted_at, confirmed_delivery_at)
VALUES
 ('66666666-6666-6666-6666-666666666901', 'PR-MF06-0001', '11111111-1111-1111-1111-111111111111',
  '99999999-9999-9999-9999-999999990001', DATE '2026-06-28', DATE '2026-07-02',
  5000000, 'APPROVED', TIMESTAMPTZ '2026-06-28T09:00:00Z', TIMESTAMPTZ '2026-06-28T10:00:00Z',
  TIMESTAMPTZ '2026-06-29T08:00:00Z')
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO purchase_items (id, purchase_request_id, ingredient_id, ingredient_label, quantity, unit, unit_price)
VALUES
 ('66666666-6666-6666-6666-666666666a01', '66666666-6666-6666-6666-666666666901',
  '66666666-6666-6666-6666-666666666605', 'Thịt lợn nạc vai', 40, 'kg', 125000);

INSERT INTO supplier_batches (id, code, partner_id, product_id, lot_number, manufacturing_date,
                               packaging_date, expiration_date, quantity, unit, status)
VALUES
 ('66666666-6666-6666-6666-666666666b01', 'BATCH-MF06-0001', '99999999-9999-9999-9999-999999990001',
  '66666666-6666-6666-6666-666666666801', 'GF-TL-260702', DATE '2026-07-02', DATE '2026-07-02',
  DATE '2026-07-08', 40, 'kg', 'DELIVERED')
ON CONFLICT (partner_id, code) DO NOTHING;

INSERT INTO deliveries (id, code, school_id, purchase_request_id, scheduled_at, vehicle_plate,
                         status, status_updated_at)
VALUES
 ('66666666-6666-6666-6666-666666666c01', 'DLV-MF06-0001', '11111111-1111-1111-1111-111111111111',
  '66666666-6666-6666-6666-666666666901', TIMESTAMPTZ '2026-07-02T05:40:00Z', '29C-123.45',
  'DELIVERED', TIMESTAMPTZ '2026-07-02T05:45:00Z')
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO delivery_batches (delivery_id, supplier_batch_id, ingredient_id) VALUES
 ('66666666-6666-6666-6666-666666666c01', '66666666-6666-6666-6666-666666666b01',
  '66666666-6666-6666-6666-666666666605')
ON CONFLICT DO NOTHING;

INSERT INTO receiving_sessions (id, code, school_id, delivery_id, supplier_id,
                                 supplier_status_at_receipt, received_at, received_by_user_id,
                                 lat, lng, gps_verified, photos_count, device_info, status)
VALUES
 ('66666666-6666-6666-6666-666666666d01', 'RCV-MF06-0001', '11111111-1111-1111-1111-111111111111',
  '66666666-6666-6666-6666-666666666c01', '99999999-9999-9999-9999-999999990001', 'APPROVED',
  TIMESTAMPTZ '2026-07-02T05:50:00Z', '66666666-6666-6666-6666-666666666701',
  21.031200, 105.763800, TRUE, 6, 'Android 14 / SFMS-Mobile 1.0.3', 'RECEIVED')
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO receiving_lines (id, session_id, supplier_batch_id, ingredient_id, ingredient_label,
                              storage_zone, rejected)
VALUES
 ('66666666-6666-6666-6666-666666666e01', '66666666-6666-6666-6666-666666666d01',
  '66666666-6666-6666-6666-666666666b01', '66666666-6666-6666-6666-666666666605',
  'Thịt lợn nạc vai', 'CHILLED', FALSE)
ON CONFLICT DO NOTHING;

INSERT INTO inventory_batches (id, code, school_id, ingredient_id, ingredient_label, supplier_id,
                                initial_quantity, unit, storage_zone, received_date, expiry_date,
                                receiving_session_id)
VALUES
 ('66666666-6666-6666-6666-666666666f01', 'BATCH-MF06-0001', '11111111-1111-1111-1111-111111111111',
  '66666666-6666-6666-6666-666666666605', 'Thịt lợn nạc vai', '99999999-9999-9999-9999-999999990001',
  40, 'kg', 'CHILLED', DATE '2026-07-02', DATE '2026-07-08',
  '66666666-6666-6666-6666-666666666d01')
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO inventory_batches (id, code, school_id, ingredient_id, ingredient_label, supplier_id,
                                initial_quantity, unit, storage_zone, received_date, expiry_date)
VALUES
 ('66666666-6666-6666-6666-666666667001', 'BATCH-MF06-EXP-01', '11111111-1111-1111-1111-111111111111',
  '66666666-6666-6666-6666-666666666609', 'Dầu ăn thực vật', '99999999-9999-9999-9999-999999990001',
  40, 'lít', 'DRY', DATE '2026-06-20', DATE '2026-06-28')
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO inventory_batches (id, code, school_id, ingredient_id, ingredient_label, supplier_id,
                                initial_quantity, unit, storage_zone, received_date, expiry_date,
                                quarantine_at, quarantine_by_user_id, quarantine_reason)
VALUES
 ('66666666-6666-6666-6666-666666667002', 'BATCH-MF06-QRT-01', '11111111-1111-1111-1111-111111111111',
  '66666666-6666-6666-6666-666666666607', 'Tôm sú', '99999999-9999-9999-9999-999999990001',
  18, 'kg', 'FROZEN', DATE '2026-06-21', DATE '2026-09-28',
  TIMESTAMPTZ '2026-06-30T14:35:00Z', '44444444-4444-4444-4444-444444444443',
  'Nghi lô không đạt kiểm tra khi chế biến — giữ chờ kết luận điều tra (BR-149).')
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO menus (id, code, school_id, week_label, name, grade, menu_date, meal_type, status,
                    approved_by_user_id, approved_at)
SELECT '66666666-6666-6666-6666-666666667101', 'MENU-MF06-DEMO', '11111111-1111-1111-1111-111111111111',
       'Tuan demo MF06', 'Thuc don demo MF06', 'Khối 3', DATE '2026-07-06', 'LUNCH', 'PUBLISHED',
       u.id, TIMESTAMPTZ '2026-07-01T00:00:00Z'
FROM user_accounts u WHERE u.code = 'U-9902'
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO inventory_transactions (id, code, school_id, occurred_at, type, batch_id, ingredient_id,
                                     delta, unit, performed_by_user_id)
VALUES
 ('66666666-6666-6666-6666-666666667201', 'TXN-MF06-0001', '11111111-1111-1111-1111-111111111111',
  TIMESTAMPTZ '2026-07-02T05:50:00Z', 'RECEIPT', '66666666-6666-6666-6666-666666666f01',
  '66666666-6666-6666-6666-666666666605', 40, 'kg', '66666666-6666-6666-6666-666666666701')
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO issue_slips (id, code, school_id, service_date, meal_type, menu_ids, issued_by_user_id, status)
VALUES
 ('66666666-6666-6666-6666-666666667301', 'ISS-MF06-0001', '11111111-1111-1111-1111-111111111111',
  DATE '2026-07-06', 'LUNCH', ARRAY['66666666-6666-6666-6666-666666667101']::uuid[],
  '66666666-6666-6666-6666-666666666701', 'POSTED')
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO inventory_transactions (id, code, school_id, occurred_at, type, batch_id, ingredient_id,
                                     delta, unit, performed_by_user_id, issue_slip_id, menu_id, dish_id,
                                     dish_label, meal_type, service_date)
SELECT '66666666-6666-6666-6666-666666667401', 'TXN-MF06-0002', '11111111-1111-1111-1111-111111111111',
       TIMESTAMPTZ '2026-07-06T02:30:00Z', 'ISSUE', '66666666-6666-6666-6666-666666666f01',
       '66666666-6666-6666-6666-666666666605', -12.5, 'kg', '66666666-6666-6666-6666-666666666701',
       '66666666-6666-6666-6666-666666667301', '66666666-6666-6666-6666-666666667101',
       d.id, d.name, 'LUNCH', DATE '2026-07-06'
FROM dishes d WHERE d.code = 'thit-kho-hanh'
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO preparation_records (id, code, school_id, date, dish_id, dish_label, menu_id, meal_type,
                                  start_time, end_time, cook_user_id)
SELECT '66666666-6666-6666-6666-666666667501', 'PREP-MF06-0001', '11111111-1111-1111-1111-111111111111',
       DATE '2026-07-06', d.id, d.name, '66666666-6666-6666-6666-666666667101', 'LUNCH',
       TIME '09:30:00', TIME '10:15:00', '66666666-6666-6666-6666-666666666701'
FROM dishes d WHERE d.code = 'thit-kho-hanh'
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO preparation_batches (preparation_record_id, batch_id, extra) VALUES
 ('66666666-6666-6666-6666-666666667501', '66666666-6666-6666-6666-666666666f01', FALSE)
ON CONFLICT DO NOTHING;
