-- ═══════════════════════════════════════════════════════════════════════════════
-- V30_1__restore_mf13.sql — KHÔI PHỤC FIXTURE NGHIỆP VỤ, CHỈ CHO MÔI TRƯỜNG TEST
--
-- ⚠️ Nằm ở src/test/resources ⇒ KHÔNG BAO GIỜ lên classpath của prod.
--
-- Nguồn: nguyên văn các câu lệnh bị cắt khỏi `V30__mf13_report_views.sql` ở commit a272a7d
-- (08/08/2026 — "xoa du lieu nghiep vu khoi seed migration, dung du lieu bang tay").
-- Quyết định với PRODUCTION giữ nguyên, KHÔNG bị đảo: file này chỉ tồn tại trong test.
--
-- Vì sao cần: suite Integration/Security dựng trên chính đám fixture đó, mà
-- `AbstractIamIntegrationTest` clean() + migrate() lại schema TRƯỚC MỖI test — nên từ
-- 08/08 mọi IT chạy trên DB rỗng và đỏ hàng loạt ở bước setup.
--
-- Vì sao đánh số 30_1 (không phải 9xx): Flyway trộn `db/migration` và `db/testseed` rồi sắp
-- theo version, nên số này đặt file vào ĐÚNG vị trí cũ trong dãy — chạy trên schema đương
-- thời và để các migration sau tự xử lý (V44 backfill `grade_id`, V71 backfill `code`…).
-- Đặt ở cuối dãy sẽ phải sửa tay từng câu lệnh cho khớp schema cuối, dễ sai hơn nhiều.
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO report_schedules (id, code, school_id, report_slug, frequency, format, time_of_day,
                               day_of_week, zone_id, active, next_run_at, created_by_user_id)
SELECT gen_random_uuid(), 'RSC-2026-001', s.id, 'hygiene', 'WEEKLY', 'XLSX', TIME '07:30',
       1, 'Asia/Ho_Chi_Minh', TRUE, now() + INTERVAL '7 days', u.id
FROM   schools s, user_accounts u
WHERE  s.code = 'SCH-MF03-0001'
  AND  u.id = '44444444-4444-4444-4444-444444444443'::uuid
ON CONFLICT DO NOTHING;
