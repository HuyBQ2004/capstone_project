-- ═══════════════════════════════════════════════════════════════════════════════
-- V7_1__restore_mf04.sql — KHÔI PHỤC FIXTURE NGHIỆP VỤ, CHỈ CHO MÔI TRƯỜNG TEST
--
-- ⚠️ Nằm ở src/test/resources ⇒ KHÔNG BAO GIỜ lên classpath của prod.
--
-- Nguồn: nguyên văn các câu lệnh bị cắt khỏi `V7__seed_mf04.sql` ở commit a272a7d
-- (08/08/2026 — "xoa du lieu nghiep vu khoi seed migration, dung du lieu bang tay").
-- Quyết định với PRODUCTION giữ nguyên, KHÔNG bị đảo: file này chỉ tồn tại trong test.
--
-- Vì sao cần: suite Integration/Security dựng trên chính đám fixture đó, mà
-- `AbstractIamIntegrationTest` clean() + migrate() lại schema TRƯỚC MỖI test — nên từ
-- 08/08 mọi IT chạy trên DB rỗng và đỏ hàng loạt ở bước setup.
--
-- Vì sao đánh số 7_1 (không phải 9xx): Flyway trộn `db/migration` và `db/testseed` rồi sắp
-- theo version, nên số này đặt file vào ĐÚNG vị trí cũ trong dãy — chạy trên schema đương
-- thời và để các migration sau tự xử lý (V44 backfill `grade_id`, V71 backfill `code`…).
-- Đặt ở cuối dãy sẽ phải sửa tay từng câu lệnh cho khớp schema cuối, dễ sai hơn nhiều.
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO partners (id, code, name, partner_type, status, tax_id, address, contact, email,
                       applied_at, reject_reason)
VALUES
 ('99999999-9999-9999-9999-999999990001', 'SUP-101', 'Cty CP Thực phẩm sạch GreenValley',
  'RAW_FOOD_SUPPLIER', 'APPROVED', '0108800001', 'Đông Anh, Hà Nội',
  'Nguyễn Văn Phúc — 0901 234 501', 'phucnv@greenvalley.example.test',
  TIMESTAMPTZ '2026-05-01T02:00:00Z', NULL),
 ('99999999-9999-9999-9999-999999990002', 'SUP-102', 'Cty TNHH Nông sản Sông Hồng',
  'RAW_FOOD_SUPPLIER', 'PENDING', '0108800002', 'Gia Lâm, Hà Nội',
  'Trần Thị Hạnh — 0901 234 502', 'hanhtt@songhong.example.test',
  TIMESTAMPTZ '2026-07-20T02:00:00Z', NULL),
 ('99999999-9999-9999-9999-999999990003', 'CAT-101', 'Đơn vị suất ăn Hoa Mai',
  'SERVICE_CATERER', 'APPROVED', '0108800003', '10 Kim Mã, Ba Đình, Hà Nội',
  'Lê Văn Sơn — 0901 234 503', 'sonlv@hoamai.example.test',
  TIMESTAMPTZ '2026-04-15T00:00:00Z', NULL)
ON CONFLICT (code) DO NOTHING;
INSERT INTO partner_documents (id, code, partner_id, name, document_type, number, issued_by,
                                issued_date, expiry_date, file_key, file_size, content_type, uploaded_at)
SELECT '99999999-9999-9999-9999-999999990101', 'DOC-GV-01', p.id,
       'Giấy chứng nhận cơ sở đủ điều kiện ATTP', 'FOOD_SAFETY_CERT', 'ATTP-HN-2025-0771',
       'Chi cục ATVSTP Hà Nội', DATE '2025-05-01', DATE '2028-05-01',
       'partner-documents/DOC-GV-01.pdf', 204800, 'application/pdf', TIMESTAMPTZ '2026-05-01T02:00:00Z'
FROM partners p WHERE p.code = 'SUP-101'
ON CONFLICT (code) DO NOTHING;

INSERT INTO delivery_persons (id, code, partner_id, full_name, national_id_enc, national_id_hash,
                               phone, vehicle_plate, biometric_status, face_id, biometric_registered_at)
SELECT '99999999-9999-9999-9999-999999990201', 'DP-101', p.id, 'Phạm Văn Long',
       decode('DEADBEEF', 'hex'), encode(digest('001099230101', 'sha256'), 'hex'),
       '0901234599', '30A-999.01', 'NOT_REGISTERED', NULL, NULL
FROM partners p WHERE p.code = 'SUP-101'
ON CONFLICT (code) DO NOTHING;

INSERT INTO partnership_applications (id, code, partner_id, school_id, company_name, tax_id, address,
                                       submitted_at, status)
SELECT '99999999-9999-9999-9999-999999990301', 'APP-2026-101', p.id, s.id,
       p.name, p.tax_id, p.address, TIMESTAMPTZ '2026-07-20T02:00:00Z', 'PENDING'
FROM partners p, schools s
WHERE p.code = 'SUP-102' AND s.code = 'SCH-MF03-0001'
ON CONFLICT (code) DO NOTHING;

INSERT INTO supplier_partnerships (id, code, partner_id, school_id, delivery_address, since, until, status)
SELECT '99999999-9999-9999-9999-999999990401', 'PTN-101', p.id, s.id,
       'Kho trung chuyển Đông Anh, Hà Nội', DATE '2026-05-05', NULL, 'APPROVED'
FROM partners p, schools s
WHERE p.code = 'SUP-101' AND s.code = 'SCH-MF03-0001'
ON CONFLICT (code) DO NOTHING;
