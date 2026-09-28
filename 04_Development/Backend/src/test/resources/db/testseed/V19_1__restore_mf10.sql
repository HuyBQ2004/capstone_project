-- ═══════════════════════════════════════════════════════════════════════════════
-- V19_1__restore_mf10.sql — KHÔI PHỤC FIXTURE NGHIỆP VỤ, CHỈ CHO MÔI TRƯỜNG TEST
--
-- ⚠️ Nằm ở src/test/resources ⇒ KHÔNG BAO GIỜ lên classpath của prod.
--
-- Nguồn: nguyên văn các câu lệnh bị cắt khỏi `V19__seed_mf10.sql` ở commit a272a7d
-- (08/08/2026 — "xoa du lieu nghiep vu khoi seed migration, dung du lieu bang tay").
-- Quyết định với PRODUCTION giữ nguyên, KHÔNG bị đảo: file này chỉ tồn tại trong test.
--
-- Vì sao cần: suite Integration/Security dựng trên chính đám fixture đó, mà
-- `AbstractIamIntegrationTest` clean() + migrate() lại schema TRƯỚC MỖI test — nên từ
-- 08/08 mọi IT chạy trên DB rỗng và đỏ hàng loạt ở bước setup.
--
-- Vì sao đánh số 19_1 (không phải 9xx): Flyway trộn `db/migration` và `db/testseed` rồi sắp
-- theo version, nên số này đặt file vào ĐÚNG vị trí cũ trong dãy — chạy trên schema đương
-- thời và để các migration sau tự xử lý (V44 backfill `grade_id`, V71 backfill `code`…).
-- Đặt ở cuối dãy sẽ phải sửa tay từng câu lệnh cho khớp schema cuối, dễ sai hơn nhiều.
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO user_accounts (id, code, name, email, password_hash, role_id, school_id, status, email_verified)
SELECT '44444444-4444-4444-4444-444444444447', 'U-9910', 'Đào Thị Quỳnh (synthetic)',
       'quynhdt.teacher.synthetic@example.test',
       '$2a$12$K19BeUNTqLNKiuAsHIkEsu8GBU6eRu.V0UmpNU5wFGevBNHMJuIo.', r.id,
       '11111111-1111-1111-1111-111111111111', 'ACTIVE', true
FROM roles r WHERE r.role_key = 'teacher';

INSERT INTO user_accounts (id, code, name, email, password_hash, role_id, school_id, status, email_verified)
SELECT '44444444-4444-4444-4444-444444444448', 'U-9911', 'Cty TNHH Suất ăn An Bình (synthetic)',
       'catering.synthetic@anbinhcatering.vn',
       '$2a$12$K19BeUNTqLNKiuAsHIkEsu8GBU6eRu.V0UmpNU5wFGevBNHMJuIo.', r.id,
       '11111111-1111-1111-1111-111111111111', 'ACTIVE', true
FROM roles r WHERE r.role_key = 'service-caterers';
INSERT INTO feedbacks (id, code, school_id, submitter_user_id, submitter_role, scope,
                        student_id, classroom_id, class_name, meal_date, meal_type,
                        meal_session_id, dish_id, dish_name, rating, tags, comment,
                        illness_keyword, submitted_at)
SELECT '10101010-1010-1010-1010-101010100601', 'FB-2026-8809',
       s.id, u.id, 'PARENT', 'DISH', st.id, st.classroom_id, '3A',
       DATE '2026-07-06', 'LUNCH', d.id, dh.id, dh.name, 2,
       '["Món ăn nguội","Khẩu phần ít"]'::jsonb,
       'Con về kêu đau bụng nhẹ buổi chiều.', TRUE, TIMESTAMPTZ '2026-07-06T13:15:00Z'
FROM schools s
JOIN students st ON st.code = 'HS-3A-001' AND st.school_id = s.id
JOIN student_distributions d ON d.code = 'DSTU-0706A-001' AND d.school_id = s.id
JOIN dishes dh ON dh.code = 'com-trang'
JOIN user_accounts u ON u.code = 'U-9905'
WHERE s.code = 'SCH-MF03-0001';

INSERT INTO announcements (id, code, school_id, title, content, audience_codes, audience_label,
                            status, urgent, author_user_id, author_name, published_at)
SELECT '10101010-1010-1010-1010-101010100701', 'ANN-2026-113', s.id,
       'Thông báo thực đơn tuần 30', 'Kính gửi Quý phụ huynh, thực đơn tuần 30 đã được cập nhật.',
       '["PARENT_GRADE_3","TEACHER"]'::jsonb, 'Phụ huynh khối 3, Giáo viên',
       'PUBLISHED', FALSE, u.id, 'Phạm Quốc Toản (BGH)', TIMESTAMPTZ '2026-07-28T02:00:00Z'
FROM schools s JOIN user_accounts u ON u.code = 'U-9902'
WHERE s.code = 'SCH-MF03-0001';

INSERT INTO announcement_revisions (announcement_id, revised_by_user_id, revised_by_name, note, revised_at)
SELECT '10101010-1010-1010-1010-101010100701', u.id, 'Phạm Quốc Toản', 'Công bố lần đầu',
       TIMESTAMPTZ '2026-07-28T02:00:00Z'
FROM user_accounts u WHERE u.code = 'U-9902';

INSERT INTO notifications (id, code, school_id, audience, recipient_roles, type, title, content,
                            link, link_label, sent_at)
SELECT '10101010-1010-1010-1010-101010100801', 'NTF-2026-482913', s.id, 'ROLES',
       '["medical-staff","school-admin"]'::jsonb, 'ALLERGY_ALERT',
       'Phản hồi có dấu hiệu bệnh lý',
       'Phụ huynh lớp 3A báo con đau bụng sau bữa trưa 06/07.',
       '/school-admin/feedback', 'Xem phản hồi', TIMESTAMPTZ '2026-07-06T13:15:00Z'
FROM schools s WHERE s.code = 'SCH-MF03-0001';
