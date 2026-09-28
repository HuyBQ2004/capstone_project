-- ═══════════════════════════════════════════════════════════════════════════════
-- V5_1__restore_mf03.sql — KHÔI PHỤC FIXTURE NGHIỆP VỤ, CHỈ CHO MÔI TRƯỜNG TEST
--
-- ⚠️ Nằm ở src/test/resources ⇒ KHÔNG BAO GIỜ lên classpath của prod.
--
-- Nguồn: nguyên văn các câu lệnh bị cắt khỏi `V5__seed_mf03.sql` ở commit a272a7d
-- (08/08/2026 — "xoa du lieu nghiep vu khoi seed migration, dung du lieu bang tay").
-- Quyết định với PRODUCTION giữ nguyên, KHÔNG bị đảo: file này chỉ tồn tại trong test.
--
-- Vì sao cần: suite Integration/Security dựng trên chính đám fixture đó, mà
-- `AbstractIamIntegrationTest` clean() + migrate() lại schema TRƯỚC MỖI test — nên từ
-- 08/08 mọi IT chạy trên DB rỗng và đỏ hàng loạt ở bước setup.
--
-- Vì sao đánh số 5_1 (không phải 9xx): Flyway trộn `db/migration` và `db/testseed` rồi sắp
-- theo version, nên số này đặt file vào ĐÚNG vị trí cũ trong dãy — chạy trên schema đương
-- thời và để các migration sau tự xử lý (V44 backfill `grade_id`, V71 backfill `code`…).
-- Đặt ở cuối dãy sẽ phải sửa tay từng câu lệnh cho khớp schema cuối, dễ sai hơn nhiều.
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO schools (id, code, name, tax_id, status)
VALUES ('11111111-1111-1111-1111-111111111111', 'SCH-MF03-0001', 'TH Đoàn Thị Điểm', '0100000001', 'ACTIVE');

INSERT INTO academic_years (id, code, school_id, name, start_date, end_date, status)
VALUES ('22222222-2222-2222-2222-222222222222', 'AY-MF03-2026',
        '11111111-1111-1111-1111-111111111111', 'Năm học 2026-2027',
        '2026-09-01', '2027-05-31', 'DRAFT');

INSERT INTO classrooms (id, code, school_id, academic_year_id, name, grade, capacity)
VALUES
  ('33333333-3333-3333-3333-333333333331', '3A', '11111111-1111-1111-1111-111111111111',
   '22222222-2222-2222-2222-222222222222', '3A', 3, 45),
  ('33333333-3333-3333-3333-333333333332', '4A', '11111111-1111-1111-1111-111111111111',
   '22222222-2222-2222-2222-222222222222', '4A', 4, 50);

INSERT INTO user_accounts (id, code, name, email, password_hash, role_id, school_id, status, email_verified)
SELECT '44444444-4444-4444-4444-444444444441', 'U-9905', 'Nguyễn Văn Bình',
       'binhnv.parent.synthetic@example.test', '$2a$12$K19BeUNTqLNKiuAsHIkEsu8GBU6eRu.V0UmpNU5wFGevBNHMJuIo.', r.id, '11111111-1111-1111-1111-111111111111',
       'ACTIVE', true
FROM roles r WHERE r.role_key = 'parent';

INSERT INTO user_accounts (id, code, name, email, password_hash, role_id, school_id, status, email_verified)
SELECT '44444444-4444-4444-4444-444444444442', 'U-9903', 'Ngô Thị Hà (synthetic)',
       'ngoth.medical.synthetic@example.test', '$2a$12$K19BeUNTqLNKiuAsHIkEsu8GBU6eRu.V0UmpNU5wFGevBNHMJuIo.', r.id, '11111111-1111-1111-1111-111111111111',
       'ACTIVE', true
FROM roles r WHERE r.role_key = 'medical-staff';

INSERT INTO user_accounts (id, code, name, email, password_hash, role_id, school_id, status, email_verified)
SELECT '44444444-4444-4444-4444-444444444443', 'U-9902', 'Trần Thu Trang (synthetic)',
       'tranth.admin.synthetic@example.test', '$2a$12$K19BeUNTqLNKiuAsHIkEsu8GBU6eRu.V0UmpNU5wFGevBNHMJuIo.', r.id, '11111111-1111-1111-1111-111111111111',
       'ACTIVE', true
FROM roles r WHERE r.role_key = 'school-admin';

INSERT INTO students (id, code, registration_no, name, dob, school_id, classroom_id, status)
VALUES
  ('55555555-5555-5555-5555-555555555501', 'HS-3A-001', 'DTD-2026-0001',
   'Nguyễn Minh Khang (synthetic)', '2018-03-12',
   '11111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333331', 'ACTIVE'),
  ('55555555-5555-5555-5555-555555555502', 'HS-3A-002', 'DTD-2026-0002',
   'Trần Bảo An (synthetic)', '2018-05-02',
   '11111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333331', 'ACTIVE'),
  ('55555555-5555-5555-5555-555555555503', 'HS-3A-005', 'DTD-2026-0003',
   'Lê Thu Hà (synthetic)', '2018-01-20',
   '11111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333331', 'ACTIVE'),
  ('55555555-5555-5555-5555-555555555504', 'HS-4A-001', 'DTD-2026-0004',
   'Phạm Gia Bảo (synthetic)', '2017-07-09',
   '11111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333332', 'ACTIVE');

INSERT INTO user_accounts (id, code, name, email, password_hash, role_id, school_id, status, email_verified)
SELECT '44444444-4444-4444-4444-444444444444', 'U-9908', 'Trần Bảo Long (synthetic)',
       'tranbl.parent.synthetic@example.test',
       '$2a$12$K19BeUNTqLNKiuAsHIkEsu8GBU6eRu.V0UmpNU5wFGevBNHMJuIo.', r.id,
       '11111111-1111-1111-1111-111111111111', 'ACTIVE', true
FROM roles r WHERE r.role_key = 'parent';

INSERT INTO student_parents (student_id, parent_user_id, relationship, is_primary)
VALUES
  ('55555555-5555-5555-5555-555555555501', '44444444-4444-4444-4444-444444444441', 'Bố', true),
  ('55555555-5555-5555-5555-555555555504', '44444444-4444-4444-4444-444444444441', 'Bố', false),
  ('55555555-5555-5555-5555-555555555502', '44444444-4444-4444-4444-444444444444', 'Bố', true),
  ('55555555-5555-5555-5555-555555555503', '44444444-4444-4444-4444-444444444444', 'Mẹ', true);

INSERT INTO health_documents (id, student_id, file_name, storage_key, content_type, size_bytes, uploaded_by_user_id)
VALUES ('66666666-6666-6666-6666-666666666601', '55555555-5555-5555-5555-555555555501',
        'don-bac-si-di-ung-tom-cua.pdf', 'mf03/2026/07/doc-771.pdf', 'application/pdf', 102400,
        '44444444-4444-4444-4444-444444444441');

INSERT INTO allergy_declarations (id, student_id, category, allergen, severity, declared_by_user_id,
                                   status, proof_file_id, verified_at)
VALUES
  ('77777777-7777-7777-7777-777777777701', '55555555-5555-5555-5555-555555555501',
   'Hải sản (tôm, cua, sò, ốc)', 'Tôm', 'SEVERE', '44444444-4444-4444-4444-444444444441',
   'VERIFIED', '66666666-6666-6666-6666-666666666601', now()),
  ('77777777-7777-7777-7777-777777777702', '55555555-5555-5555-5555-555555555501',
   'Hải sản (tôm, cua, sò, ốc)', 'Cua', 'SEVERE', '44444444-4444-4444-4444-444444444441',
   'VERIFIED', '66666666-6666-6666-6666-666666666601', now());

INSERT INTO allergy_declarations (id, student_id, category, allergen, severity, declared_by_user_id, status, review_note)
VALUES ('77777777-7777-7777-7777-777777777704', '55555555-5555-5555-5555-555555555503',
        'Sữa & chế phẩm', 'Sữa bò', 'MODERATE', '44444444-4444-4444-4444-444444444441',
        'NEEDS_INFO', 'Cần bổ sung đơn bác sĩ');

INSERT INTO allergy_declarations (id, student_id, category, allergen, severity, declared_by_user_id, status)
VALUES ('77777777-7777-7777-7777-777777777707', '55555555-5555-5555-5555-555555555504',
        'Đậu & hạt', 'Đậu nành', 'MODERATE', '44444444-4444-4444-4444-444444444441', 'PENDING');

INSERT INTO health_history_events (id, student_id, at, type, event, by_user_id)
VALUES
  ('88888888-8888-8888-8888-888888888801', '55555555-5555-5555-5555-555555555501',
   now() - interval '10 days', 'KHAI_BAO', 'Khai báo dị ứng Tôm, Cua (synthetic)',
   '44444444-4444-4444-4444-444444444441'),
  ('88888888-8888-8888-8888-888888888802', '55555555-5555-5555-5555-555555555501',
   now() - interval '9 days', 'XAC_MINH', 'Xác minh dị ứng Tôm, Cua — mức Nặng, có minh chứng (synthetic)',
   '44444444-4444-4444-4444-444444444441');
