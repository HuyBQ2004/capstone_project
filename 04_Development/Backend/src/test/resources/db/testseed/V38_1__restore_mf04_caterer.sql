-- ═══════════════════════════════════════════════════════════════════════════════
-- V38_1__restore_mf04_caterer.sql — KHÔI PHỤC FIXTURE NGHIỆP VỤ, CHỈ CHO MÔI TRƯỜNG TEST
--
-- ⚠️ Nằm ở src/test/resources ⇒ KHÔNG BAO GIỜ lên classpath của prod.
--
-- Nguồn: nguyên văn các câu lệnh bị cắt khỏi `V38__mf04_seed_caterer_partnership.sql` ở commit a272a7d
-- (08/08/2026 — "xoa du lieu nghiep vu khoi seed migration, dung du lieu bang tay").
-- Quyết định với PRODUCTION giữ nguyên, KHÔNG bị đảo: file này chỉ tồn tại trong test.
--
-- Vì sao cần: suite Integration/Security dựng trên chính đám fixture đó, mà
-- `AbstractIamIntegrationTest` clean() + migrate() lại schema TRƯỚC MỖI test — nên từ
-- 08/08 mọi IT chạy trên DB rỗng và đỏ hàng loạt ở bước setup.
--
-- Vì sao đánh số 38_1 (không phải 9xx): Flyway trộn `db/migration` và `db/testseed` rồi sắp
-- theo version, nên số này đặt file vào ĐÚNG vị trí cũ trong dãy — chạy trên schema đương
-- thời và để các migration sau tự xử lý (V44 backfill `grade_id`, V71 backfill `code`…).
-- Đặt ở cuối dãy sẽ phải sửa tay từng câu lệnh cho khớp schema cuối, dễ sai hơn nhiều.
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO supplier_partnerships (id, code, partner_id, school_id, delivery_address, since, until, status)
SELECT '99999999-9999-9999-9999-999999990402', 'PTN-102', p.id, s.id,
       NULL, DATE '2026-01-01', NULL, 'APPROVED'
FROM partners p, schools s
WHERE p.code = 'CAT-101' AND s.code = 'SCH-MF03-0001'
ON CONFLICT (code) DO NOTHING;
