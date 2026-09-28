-- ═══════════════════════════════════════════════════════════════════════════════
-- V15_1__restore_mf08.sql — KHÔI PHỤC FIXTURE NGHIỆP VỤ, CHỈ CHO MÔI TRƯỜNG TEST
--
-- ⚠️ Nằm ở src/test/resources ⇒ KHÔNG BAO GIỜ lên classpath của prod.
--
-- Nguồn: nguyên văn các câu lệnh bị cắt khỏi `V15__seed_mf08.sql` ở commit a272a7d
-- (08/08/2026 — "xoa du lieu nghiep vu khoi seed migration, dung du lieu bang tay").
-- Quyết định với PRODUCTION giữ nguyên, KHÔNG bị đảo: file này chỉ tồn tại trong test.
--
-- Vì sao cần: suite Integration/Security dựng trên chính đám fixture đó, mà
-- `AbstractIamIntegrationTest` clean() + migrate() lại schema TRƯỚC MỖI test — nên từ
-- 08/08 mọi IT chạy trên DB rỗng và đỏ hàng loạt ở bước setup.
--
-- Vì sao đánh số 15_1 (không phải 9xx): Flyway trộn `db/migration` và `db/testseed` rồi sắp
-- theo version, nên số này đặt file vào ĐÚNG vị trí cũ trong dãy — chạy trên schema đương
-- thời và để các migration sau tự xử lý (V44 backfill `grade_id`, V71 backfill `code`…).
-- Đặt ở cuối dãy sẽ phải sửa tay từng câu lệnh cho khớp schema cuối, dễ sai hơn nhiều.
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO dishes (id, code, name, category, kcal, protein, fat, carb, manual_allergens, is_active)
VALUES
 ('88888888-8888-8888-8888-888888888101', 'thit-kho-trung-cut', 'Thịt kho trứng cút', 'MON_MAN', 210, 15, 14, 3, '[]', TRUE),
 ('88888888-8888-8888-8888-888888888102', 'ga-rang-gung',       'Gà rang gừng',        'MON_MAN', 230, 20, 13, 4, '[]', TRUE),
 ('88888888-8888-8888-8888-888888888103', 'thit-bo-xao-rau-cu', 'Thịt bò xào rau củ',  'MON_MAN', 195, 16, 11, 6, '[]', TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO inspection_sessions (id, code, school_id, partner_id, date, meal_type,
                                  step1, step2, step3, overall, core_temp, inspector_user_id, decided_at)
SELECT v.id, v.code, s.id, p.id, v.date, 'LUNCH',
       'PASSED', 'PASSED', 'PASSED', 'PASSED', 76.00, u.id, v.date::timestamptz
FROM (VALUES
    ('88888888-8888-8888-8888-888888888201'::uuid, 'INSP-20260706-L', DATE '2026-07-06'),
    ('88888888-8888-8888-8888-888888888202'::uuid, 'INSP-20260702-L', DATE '2026-07-02'),
    ('88888888-8888-8888-8888-888888888203'::uuid, 'INSP-20260701-L', DATE '2026-07-01'),
    ('88888888-8888-8888-8888-888888888204'::uuid, 'INSP-20260630-L', DATE '2026-06-30'),
    ('88888888-8888-8888-8888-888888888205'::uuid, 'INSP-20260628-L', DATE '2026-06-28')
) AS v(id, code, date)
CROSS JOIN (SELECT id FROM schools WHERE code = 'SCH-MF03-0001') s
CROSS JOIN (SELECT id FROM partners WHERE code = 'CAT-101') p
CROSS JOIN (SELECT id FROM user_accounts WHERE code = 'U-9906') u
ON CONFLICT (school_id, code) DO NOTHING;
INSERT INTO sample_lab_requests (id, code, school_id, incident_id, authority_id, lab_destination,
                                  handover_type, test_criteria, requested_by, requested_at,
                                  delivered_by, sent_to_lab_at, lab_representative, status)
SELECT '88888888-8888-8888-8888-888888888401', 'LABREQ-2026-001', s.id,
       '88888888-8888-8888-8888-888888888601',  -- placeholder INC-2026-014 (MF11 chưa merge)
       a.id, 'CDC Hà Nội', 'LAB', 'Salmonella, S.aureus, tổng vi sinh vật hiếu khí',
       'Ngô Thị Hà', TIMESTAMPTZ '2026-06-30T07:35:00Z',
       'Ngô Thị Hà', TIMESTAMPTZ '2026-06-30T08:00:00Z', 'Nguyễn Thị Vân (CDC Hà Nội)', 'SENT_TO_LAB'
FROM (SELECT id FROM schools WHERE code = 'SCH-MF03-0001') s
CROSS JOIN (SELECT id FROM receiving_authorities WHERE code = 'LAB-CDC-HN') a
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO food_samples (id, code, school_id, inspection_id, dish_id, dish_name, meal_type, meal_shift,
                          servings, sample_amount, sample_unit, container, sanitized, sealed, fridge_temp,
                          temp_out_of_range, collected_at, collected_by, storage_location, retention_deadline,
                          status, incident_id, disposed_at, disposed_by, disposal_witness, disposal_note,
                          lab_request_id, retrieved_at, sent_to_lab_at)
SELECT
    v.id, v.code, s.id,
    (SELECT id FROM inspection_sessions WHERE code = v.insp_code AND school_id = s.id),
    (SELECT id FROM dishes WHERE code = v.dish_code),
    v.dish_name, 'LUNCH', v.meal_shift,
    v.servings, v.amount, v.unit, v.container, TRUE, TRUE, v.fridge_temp, FALSE,
    v.collected_at, v.collected_by, v.storage_location, v.collected_at + INTERVAL '24 hours',
    v.status, v.incident_id, v.disposed_at, v.disposed_by, v.disposal_witness, v.disposal_note,
    v.lab_request_id, v.retrieved_at, v.sent_to_lab_at
FROM (VALUES
    -- id, code, insp_code, dish_code, dish_name, meal_shift, servings, amount, unit, container,
    -- fridge_temp, collected_at, collected_by, storage_location, status,
    -- incident_id, disposed_at, disposed_by, disposal_witness, disposal_note,
    -- lab_request_id, retrieved_at, sent_to_lab_at
    ('88888888-8888-8888-8888-888888888501'::uuid, 'SMP-20260706-01', 'INSP-20260706-L', 'com-trang',
     'Cơm trắng', 'Bữa trưa — 10:15', 1850, 120.00, 'G', 'Hộp inox có nắp kín', 4.2,
     TIMESTAMPTZ '2026-07-06T03:15:00Z', 'Lê Thị Mai', 'Tủ lưu mẫu số 1 — ngăn 1', 'RETAINED',
     NULL::uuid, NULL::timestamptz, NULL, NULL, NULL, NULL::uuid, NULL::timestamptz, NULL::timestamptz),
    ('88888888-8888-8888-8888-888888888502'::uuid, 'SMP-20260706-02', 'INSP-20260706-L', 'thit-kho-trung-cut',
     'Thịt kho trứng cút', 'Bữa trưa — 10:17', 1850, 115.00, 'G', 'Hộp inox có nắp kín', 4.2,
     TIMESTAMPTZ '2026-07-06T03:17:00Z', 'Lê Thị Mai', 'Tủ lưu mẫu số 1 — ngăn 1', 'RETAINED',
     NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
    ('88888888-8888-8888-8888-888888888503'::uuid, 'SMP-20260706-03', 'INSP-20260706-L', 'canh-bi-do-nau-tom',
     'Canh bí đỏ nấu tôm', 'Bữa trưa — 10:19', 1850, 180.00, 'ML', 'Hộp thủy tinh có nắp', 4.3,
     TIMESTAMPTZ '2026-07-06T03:19:00Z', 'Lê Thị Mai', 'Tủ lưu mẫu số 1 — ngăn 2', 'RETAINED',
     NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
    ('88888888-8888-8888-8888-888888888504'::uuid, 'SMP-20260706-04', 'INSP-20260706-L', 'su-su-xao',
     'Su su xào', 'Bữa trưa — 10:21', 1850, 110.00, 'G', 'Hộp inox có nắp kín', 4.3,
     TIMESTAMPTZ '2026-07-06T03:21:00Z', 'Lê Thị Mai', 'Tủ lưu mẫu số 1 — ngăn 2', 'RETAINED',
     NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
    -- FX-003 (TestSpec §TDS-05): quá hạn lưu, KHÔNG vướng sự cố -> nhánh hủy hợp lệ (UC-030 happy path)
    ('88888888-8888-8888-8888-888888888505'::uuid, 'SMP-20260702-01', 'INSP-20260702-L', 'thit-kho-trung-cut',
     'Thịt kho trứng cút', 'Bữa trưa — 10:15', 1850, 120.00, 'G', 'Hộp inox có nắp kín', 4.2,
     TIMESTAMPTZ '2026-07-02T03:15:00Z', 'Lê Thị Mai', 'Tủ lưu mẫu số 1 — ngăn 2', 'RETAINED',
     NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
    ('88888888-8888-8888-8888-888888888506'::uuid, 'SMP-20260701-01', 'INSP-20260701-L', 'ca-basa-sot-ca-chua',
     'Cá basa sốt cà chua', 'Bữa trưa — 10:20', 1841, 110.00, 'G', 'Hộp inox có nắp kín', 4.5,
     TIMESTAMPTZ '2026-07-01T03:20:00Z', 'Trần Văn Hùng', 'Tủ lưu mẫu số 1 — ngăn 1', 'RETAINED',
     NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
    -- FX-004 (TestSpec): quá hạn lưu, incident_id gắn INC-2026-014 (OPEN) -> nhánh BR-112 chặn hủy
    ('88888888-8888-8888-8888-888888888507'::uuid, 'SMP-20260630-01', 'INSP-20260630-L', 'com-trang',
     'Cơm trắng', 'Bữa trưa — 10:20', 1820, 120.00, 'G', 'Hộp inox có nắp kín', 4.1,
     TIMESTAMPTZ '2026-06-30T03:20:00Z', 'Trần Văn Hùng', 'Tủ lưu mẫu số 1 — ngăn 3', 'RETAINED',
     '88888888-8888-8888-8888-888888888601'::uuid, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
    ('88888888-8888-8888-8888-888888888508'::uuid, 'SMP-20260630-02', 'INSP-20260630-L', 'su-su-xao',
     'Su su xào', 'Bữa trưa — 10:22', 1820, 180.00, 'ML', 'Hộp thủy tinh có nắp', 4.1,
     TIMESTAMPTZ '2026-06-30T03:22:00Z', 'Trần Văn Hùng', 'Tủ lưu mẫu số 1 — ngăn 3', 'RETAINED',
     '88888888-8888-8888-8888-888888888601'::uuid, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
    -- FX-005 (TestSpec): đã gửi lab, gắn LABREQ-2026-001 -> nhánh SMP-113 (không bàn giao lần hai) + FSM cuối
    ('88888888-8888-8888-8888-888888888509'::uuid, 'SMP-20260630-03', 'INSP-20260630-L', 'ga-rang-gung',
     'Gà rang gừng', 'Bữa trưa — 10:25', 1820, 105.00, 'G', 'Hộp inox có nắp kín', 4.1,
     TIMESTAMPTZ '2026-06-30T03:25:00Z', 'Trần Văn Hùng', 'Tủ lưu mẫu số 1 — ngăn 3', 'SENT_TO_LAB',
     '88888888-8888-8888-8888-888888888601'::uuid, NULL, NULL, NULL, NULL,
     '88888888-8888-8888-8888-888888888401'::uuid,
     TIMESTAMPTZ '2026-06-30T08:00:00Z', TIMESTAMPTZ '2026-06-30T08:00:00Z'),
    -- FX-006 (TestSpec): DISPOSED — UC-076 AF1 (liên kết mẫu đã hủy) + kiểm cột 9/10/12 Mẫu số 5
    ('88888888-8888-8888-8888-888888888510'::uuid, 'SMP-20260628-01', 'INSP-20260628-L', 'thit-bo-xao-rau-cu',
     'Thịt bò xào rau củ', 'Bữa trưa — 10:30', 1835, 115.00, 'G', 'Hộp inox có nắp kín', 4.3,
     TIMESTAMPTZ '2026-06-28T03:30:00Z', 'Lê Thị Mai', 'Tủ lưu mẫu số 1 — ngăn 1', 'DISPOSED',
     NULL, TIMESTAMPTZ '2026-07-01T04:00:00Z', 'Lê Thị Mai', 'Ngô Thị Hà',
     'Mẫu đạt, không phát sinh sự cố — hủy sau khi hết hạn lưu 24h (BR-111)',
     NULL, NULL, NULL)
) AS v(id, code, insp_code, dish_code, dish_name, meal_shift, servings, amount, unit, container,
       fridge_temp, collected_at, collected_by, storage_location, status,
       incident_id, disposed_at, disposed_by, disposal_witness, disposal_note,
       lab_request_id, retrieved_at, sent_to_lab_at)
CROSS JOIN (SELECT id FROM schools WHERE code = 'SCH-MF03-0001') s
ON CONFLICT (school_id, code) DO NOTHING;
