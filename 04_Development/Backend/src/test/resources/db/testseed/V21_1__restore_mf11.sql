-- ═══════════════════════════════════════════════════════════════════════════════
-- V21_1__restore_mf11.sql — KHÔI PHỤC FIXTURE NGHIỆP VỤ, CHỈ CHO MÔI TRƯỜNG TEST
--
-- ⚠️ Nằm ở src/test/resources ⇒ KHÔNG BAO GIỜ lên classpath của prod.
--
-- Nguồn: nguyên văn các câu lệnh bị cắt khỏi `V21__seed_mf11.sql` ở commit a272a7d
-- (08/08/2026 — "xoa du lieu nghiep vu khoi seed migration, dung du lieu bang tay").
-- Quyết định với PRODUCTION giữ nguyên, KHÔNG bị đảo: file này chỉ tồn tại trong test.
--
-- Vì sao cần: suite Integration/Security dựng trên chính đám fixture đó, mà
-- `AbstractIamIntegrationTest` clean() + migrate() lại schema TRƯỚC MỖI test — nên từ
-- 08/08 mọi IT chạy trên DB rỗng và đỏ hàng loạt ở bước setup.
--
-- Vì sao đánh số 21_1 (không phải 9xx): Flyway trộn `db/migration` và `db/testseed` rồi sắp
-- theo version, nên số này đặt file vào ĐÚNG vị trí cũ trong dãy — chạy trên schema đương
-- thời và để các migration sau tự xử lý (V44 backfill `grade_id`, V71 backfill `code`…).
-- Đặt ở cuối dãy sẽ phải sửa tay từng câu lệnh cho khớp schema cuối, dễ sai hơn nhiều.
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO incident_source_reports
 (code, school_id, reported_by_id, reported_by_label, reported_at, class_name, symptoms,
  suspected_meal_text, meal_date, description, evidence_photos, severity_level, status)
SELECT 'RPT-2026-001', s.id, u.id, 'Nguyễn Văn Bình (Phụ huynh)',
       TIMESTAMPTZ '2026-07-08T11:20:00Z', '3A', '["Đau bụng","Buồn nôn"]'::jsonb,
       'Cơm trắng, món mặn trưa 08/07', DATE '2026-07-08',
       'Con về nhà kêu đau bụng nhẹ sau bữa trưa ở trường, chưa rõ nguyên nhân.', 1, NULL,
       'PENDING_TRIAGE'
FROM schools s JOIN user_accounts u ON u.code = 'U-9905'
WHERE s.code = 'SCH-MF03-0001'
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO incident_source_reports
 (code, school_id, reported_by_id, reported_by_label, reported_at, class_name, symptoms,
  suspected_meal_text, meal_date, description, evidence_photos, severity_level, status)
SELECT 'RPT-2026-002', s.id, u.id, 'Đào Thị Quỳnh (GVCN 3A)',
       TIMESTAMPTZ '2026-07-06T08:10:00Z', '3A', '["Đau bụng","Nôn"]'::jsonb,
       'Thịt kho hành trưa 06/07', DATE '2026-07-06',
       'Nhiều học sinh lớp 3A kêu đau bụng, một em nôn ngay sau giờ ăn trưa.', 2, 'HIGH',
       -- ck_rpt_merged đòi merged_into_incident_id NOT NULL khi status='MERGED' — chưa có
       -- Incident lúc INSERT này; khởi tạo DRAFTED rồi chuyển MERGED cùng lúc gán
       -- merged_into_incident_id ở UPDATE bên dưới.
       'DRAFTED'
FROM schools s JOIN user_accounts u ON u.code = 'U-9907'
WHERE s.code = 'SCH-MF03-0001'
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO incident_source_reports
 (code, school_id, reported_by_id, reported_by_label, reported_at, class_name, symptoms,
  suspected_meal_text, meal_date, description, evidence_photos, severity_level, status)
SELECT 'RPT-2026-003', s.id, u.id, 'Đào Thị Quỳnh (GVCN 3A)',
       TIMESTAMPTZ '2026-07-06T08:25:00Z', '3A', '["Đau bụng"]'::jsonb,
       'Thịt kho hành trưa 06/07', DATE '2026-07-06',
       'Thêm 1 học sinh cùng lớp báo đau bụng, cùng triệu chứng RPT-2026-002.', 1, 'HIGH',
       'DRAFTED'
FROM schools s JOIN user_accounts u ON u.code = 'U-9907'
WHERE s.code = 'SCH-MF03-0001'
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO incident_records
 (code, school_id, content, status, created_by_id, location, occurred_at, pdf_exported)
SELECT 'REC-2026-001', s.id,
       'Ghi nhận 4 học sinh lớp 3A có triệu chứng đau bụng/nôn sau bữa trưa 06/07/2026. '
       || 'Nghi vấn món Thịt kho hành. Đã mở phiên điều tra theo mẫu specialreport2173_1.',
       -- ck_rec_opened đòi incident_id NOT NULL khi status='OPENED' — chưa có Incident lúc
       -- INSERT này (tạo sau), nên khởi tạo COMPLETED rồi chuyển OPENED cùng lúc gán incident_id
       -- ở UPDATE bên dưới (một CHECK không thể "tạm vi phạm" giữa 2 câu lệnh).
       'COMPLETED', u.id, 'Phòng Y tế — TH Đoàn Thị Điểm', TIMESTAMPTZ '2026-07-06T08:15:00Z', FALSE
FROM schools s JOIN user_accounts u ON u.code = 'U-9903'
WHERE s.code = 'SCH-MF03-0001'
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO incident_record_sources (record_id, report_id)
SELECT rec.id, rpt.id FROM incident_records rec, incident_source_reports rpt
WHERE rec.code = 'REC-2026-001' AND rpt.code IN ('RPT-2026-002', 'RPT-2026-003')
ON CONFLICT DO NOTHING;

INSERT INTO incidents
 (code, school_id, incident_record_id, title, reported_at, occurred_at, reported_by_label,
  severity, status, symptoms, suspected_meal_text, meal_date, meal_time_label, first_aid_notes,
  clinical_risk_assessment, root_cause, root_cause_status, traceability_map_status)
SELECT 'INC-2026-001', s.id, rec.id,
       'Nghi ngộ độc thực phẩm sau bữa trưa lớp 3A (06/07/2026)',
       TIMESTAMPTZ '2026-07-06T08:15:00Z', TIMESTAMPTZ '2026-07-06T04:15:00Z',
       'Đào Thị Quỳnh (GVCN 3A)', 'HIGH', 'INVESTIGATING',
       '["Đau bụng","Nôn"]'::jsonb, 'Thịt kho hành trưa 06/07', DATE '2026-07-06', '11:30 - 12:15',
       'Đã cho 3 học sinh nghỉ ngơi tại phòng Y tế, theo dõi mạch/nhiệt độ, thông báo phụ huynh.',
       'Triệu chứng khởi phát trong vòng 2 giờ sau ăn, phù hợp nghi vấn ngộ độc thực phẩm cấp.',
       'Nghi lô thịt lợn BATCH-MF06-0001 không đạt yêu cầu bảo quản trước khi chế biến.',
       'PRELIMINARY', 'COMPILING'
FROM schools s JOIN incident_records rec ON rec.code = 'REC-2026-001' AND rec.school_id = s.id
WHERE s.code = 'SCH-MF03-0001'
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO incident_affected_students (incident_id, student_id, student_name, class_name, recovered, recovered_at)
SELECT inc.id, st.id, st.name, '3A', TRUE, TIMESTAMPTZ '2026-07-06T14:00:00Z'
FROM incidents inc, students st WHERE inc.code = 'INC-2026-001' AND st.code = 'HS-3A-001'
ON CONFLICT (incident_id, student_id) DO NOTHING;
INSERT INTO incident_affected_students (incident_id, student_id, student_name, class_name, recovered, recovered_at)
SELECT inc.id, st.id, st.name, '3A', TRUE, TIMESTAMPTZ '2026-07-06T15:30:00Z'
FROM incidents inc, students st WHERE inc.code = 'INC-2026-001' AND st.code = 'HS-3A-002'
ON CONFLICT (incident_id, student_id) DO NOTHING;
INSERT INTO incident_affected_students (incident_id, student_id, student_name, class_name, recovered, recovered_at)
SELECT inc.id, st.id, st.name, '3A', FALSE, NULL
FROM incidents inc, students st WHERE inc.code = 'INC-2026-001' AND st.code = 'HS-3A-005'
ON CONFLICT (incident_id, student_id) DO NOTHING;

INSERT INTO incident_suspected_dishes (incident_id, dish_id, dish_name)
SELECT inc.id, d.id, d.name FROM incidents inc, dishes d
WHERE inc.code = 'INC-2026-001' AND d.code = 'thit-kho-hanh'
ON CONFLICT (incident_id, dish_id) DO NOTHING;

INSERT INTO incident_quarantine_batches (incident_id, batch_id, batch_code, reason, quarantined_at, quarantined_by_id)
SELECT inc.id, b.id, b.code,
       'Trùng khớp nguyên liệu món nghi vấn (Thịt kho hành) trong cửa sổ điều tra 24h — BR-149.',
       TIMESTAMPTZ '2026-07-06T08:20:00Z', u.id
FROM incidents inc, inventory_batches b, user_accounts u
WHERE inc.code = 'INC-2026-001' AND b.code = 'BATCH-MF06-0001' AND u.code = 'U-9903'
ON CONFLICT (incident_id, batch_id) DO NOTHING;

INSERT INTO incident_evidence (incident_id, type, description, file_count, captured_at)
SELECT inc.id, 'Ảnh hiện trường', 'Ảnh khu vực bếp và mẫu thức ăn còn lại lúc phát hiện sự việc.', 3,
       TIMESTAMPTZ '2026-07-06T08:30:00Z'
FROM incidents inc WHERE inc.code = 'INC-2026-001';

INSERT INTO incident_events (code, incident_id, at, type, description, actor_label)
SELECT 'IEV-001-01', inc.id, TIMESTAMPTZ '2026-07-06T08:15:00Z', 'REPORTED',
       'Mở phiên điều tra từ 2 báo cáo thô RPT-2026-002/003.', 'Ngô Thị Hà (Y tế)'
FROM incidents inc WHERE inc.code = 'INC-2026-001';
INSERT INTO incident_events (code, incident_id, at, type, description, actor_label)
SELECT 'IEV-001-02', inc.id, TIMESTAMPTZ '2026-07-06T08:18:00Z', 'SEVERITY_SET',
       'Phân loại mức độ HIGH — ≥3 học sinh cùng triệu chứng trong 24h.', 'Ngô Thị Hà (Y tế)'
FROM incidents inc WHERE inc.code = 'INC-2026-001';
INSERT INTO incident_events (code, incident_id, at, type, description, actor_label)
SELECT 'IEV-001-03', inc.id, TIMESTAMPTZ '2026-07-06T08:20:00Z', 'QUARANTINE',
       'Cách ly lô BATCH-MF06-0001 (Thịt lợn nạc vai) — nghi vấn nguyên liệu.', 'Ngô Thị Hà (Y tế)'
FROM incidents inc WHERE inc.code = 'INC-2026-001';

INSERT INTO corrective_actions
 (code, school_id, source_label, incident_id, description, assignee_label, assignee_supplier_id,
  status, submitted_for_approval_at, approved_at, evidence_note, evidence_photos)
SELECT 'CAPA-2026-001', s.id, 'Sự cố INC-2026-001', inc.id,
       'Rà soát quy trình bảo quản lạnh nguyên liệu thịt tại bếp; kiểm tra nhiệt độ tủ lạnh 2 lần/ca.',
       'Đơn vị suất ăn Hoa Mai', p.id, 'APPROVED_FOR_IMPLEMENTATION',
       TIMESTAMPTZ '2026-07-06T09:00:00Z', TIMESTAMPTZ '2026-07-06T10:30:00Z', NULL, 0
FROM schools s
JOIN incidents inc ON inc.code = 'INC-2026-001' AND inc.school_id = s.id
JOIN partners p ON p.code = 'CAT-101'
WHERE s.code = 'SCH-MF03-0001'
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO incident_events (code, incident_id, at, type, description, actor_label)
SELECT 'IEV-001-04', inc.id, TIMESTAMPTZ '2026-07-06T09:00:00Z', 'CAPA_SUBMITTED',
       'Gửi kế hoạch khắc phục CAPA-2026-001 để Ban giám hiệu duyệt.', 'Ngô Thị Hà (Y tế)'
FROM incidents inc WHERE inc.code = 'INC-2026-001';
INSERT INTO incident_events (code, incident_id, at, type, description, actor_label)
SELECT 'IEV-001-05', inc.id, TIMESTAMPTZ '2026-07-06T10:30:00Z', 'CAPA_APPROVED',
       'Ban giám hiệu duyệt CAPA-2026-001 — chuyển đơn vị triển khai.', 'Phạm Quốc Toản (BGH)'
FROM incidents inc WHERE inc.code = 'INC-2026-001';

INSERT INTO violation_records
 (code, school_id, subject, recorded_by_id, recorded_by_label, date, severity, resolution, status,
  violation_type, description, occurred_at, location_of_incident, caterers_representative_id,
  caterers_representative_label, self_reported, related_incident_id, pdf_exported, pdf_generated_at,
  pdf_version, action_taken, unit_sanction_level, handled_by_id)
SELECT 'VIO-2026-001', s.id, 'Bảo quản nguyên liệu không đúng quy trình', u.id, 'Ngô Thị Hà (Y tế)',
       DATE '2026-07-06', 'MEDIUM', 'APPROVED', 'CLOSED', 'HYGIENE_VIOLATION',
       'Lô thịt lợn không được bảo quản lạnh đúng nhiệt độ trước khi chế biến, dẫn tới nghi vấn '
       || 'liên quan sự cố INC-2026-001.',
       TIMESTAMPTZ '2026-07-06T08:20:00Z', 'Khu vực bếp — TH Đoàn Thị Điểm', rep.id,
       rep.name, FALSE, inc.id, TRUE,
       TIMESTAMPTZ '2026-07-07T02:00:00Z', 1,
       'Yêu cầu đơn vị nhắc nhở bằng văn bản và bổ sung nhiệt kế tủ lạnh tại khu sơ chế.',
       'WRITTEN_REMINDER', admin.id
FROM schools s
JOIN incidents inc ON inc.code = 'INC-2026-001' AND inc.school_id = s.id
JOIN user_accounts u ON u.code = 'U-9903'
JOIN user_accounts admin ON admin.code = 'U-9902'
JOIN user_accounts rep ON rep.code = 'U-9911'
WHERE s.code = 'SCH-MF03-0001'
ON CONFLICT (school_id, code) DO NOTHING;

INSERT INTO violation_files (code, violation_id, kind, name, storage_key, size_label, uploaded_at)
SELECT 'VF-2026-001', v.id, 'SIGNED_RECORD', 'VIO-2026-001-signed.pdf',
       'violation/VIO-2026-001-signed.pdf', '412 KB', TIMESTAMPTZ '2026-07-07T02:30:00Z'
FROM violation_records v WHERE v.code = 'VIO-2026-001';
