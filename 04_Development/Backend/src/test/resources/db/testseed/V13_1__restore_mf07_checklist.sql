-- ═══════════════════════════════════════════════════════════════════════════════
-- V13_1__restore_mf07_checklist.sql — KHÔI PHỤC FIXTURE NGHIỆP VỤ, CHỈ CHO MÔI TRƯỜNG TEST
--
-- ⚠️ Nằm ở src/test/resources ⇒ KHÔNG BAO GIỜ lên classpath của prod.
--
-- Nguồn: nguyên văn các câu lệnh bị cắt khỏi `V13__mf07_seed_checklist.sql` ở commit a272a7d
-- (08/08/2026 — "xoa du lieu nghiep vu khoi seed migration, dung du lieu bang tay").
-- Quyết định với PRODUCTION giữ nguyên, KHÔNG bị đảo: file này chỉ tồn tại trong test.
--
-- Vì sao cần: suite Integration/Security dựng trên chính đám fixture đó, mà
-- `AbstractIamIntegrationTest` clean() + migrate() lại schema TRƯỚC MỖI test — nên từ
-- 08/08 mọi IT chạy trên DB rỗng và đỏ hàng loạt ở bước setup.
--
-- Vì sao đánh số 13_1 (không phải 9xx): Flyway trộn `db/migration` và `db/testseed` rồi sắp
-- theo version, nên số này đặt file vào ĐÚNG vị trí cũ trong dãy — chạy trên schema đương
-- thời và để các migration sau tự xử lý (V44 backfill `grade_id`, V71 backfill `code`…).
-- Đặt ở cuối dãy sẽ phải sửa tay từng câu lệnh cho khớp schema cuối, dễ sai hơn nhiều.
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO schools (id, code, name, tax_id, status)
VALUES ('77777777-7777-7777-7777-777777777900', 'SCH-MF07-0001', 'TH Đoàn Thị Điểm (MF07 seed)', '0100000007', 'ACTIVE')
ON CONFLICT (code) DO NOTHING;

INSERT INTO checklist_groups (id, code, school_id, name, display_order, is_active) VALUES
 ('77777777-7777-7777-7777-777777777001', 'CLG-1', '77777777-7777-7777-7777-777777777900', 'Nhóm 1 — Hồ sơ', 1, TRUE),
 ('77777777-7777-7777-7777-777777777002', 'CLG-2', '77777777-7777-7777-7777-777777777900', 'Nhóm 2 — Yêu cầu đối với người tiếp xúc trực tiếp sản phẩm', 2, TRUE),
 ('77777777-7777-7777-7777-777777777003', 'CLG-3', '77777777-7777-7777-7777-777777777900', 'Nhóm 3 — Điều kiện bảo quản thực phẩm', 3, TRUE),
 ('77777777-7777-7777-7777-777777777004', 'CLG-4', '77777777-7777-7777-7777-777777777900', 'Nhóm 4 — Điều kiện khu vực sơ chế, chế biến thực phẩm', 4, TRUE),
 ('77777777-7777-7777-7777-777777777005', 'CLG-5', '77777777-7777-7777-7777-777777777900', 'Nhóm 5 — An toàn thực phẩm', 5, TRUE),
 ('77777777-7777-7777-7777-777777777006', 'CLG-6', '77777777-7777-7777-7777-777777777900', 'Nhóm 6 — Sổ kiểm thực 3 bước và lưu mẫu (kiểm tra chéo)', 6, TRUE);

INSERT INTO checklist_items (id, code, group_id, school_id, name, legal_note, display_order, is_active) VALUES
 ('77777777-7777-7777-7777-777777771101', 'CLI-1-1', '77777777-7777-7777-7777-777777777001', '77777777-7777-7777-7777-777777777900',
  'Hợp đồng cung cấp hàng hóa, hồ sơ nguồn gốc nguyên liệu',
  'Luật ATTP 55/2010/QH12 Điều 19.1.đ; TTLT 13/2016 Phụ lục 04 Mục IV.4.2.', 1, TRUE),
 ('77777777-7777-7777-7777-777777771102', 'CLI-1-2', '77777777-7777-7777-7777-777777777001', '77777777-7777-7777-7777-777777777900',
  'Hóa đơn chứng từ chứng minh nguồn gốc nguyên liệu thực phẩm',
  'QĐ 1246/QĐ-BYT, Điều 3.1.a.', 2, TRUE),

 ('77777777-7777-7777-7777-777777772101', 'CLI-2-1', '77777777-7777-7777-7777-777777777002', '77777777-7777-7777-7777-777777777900',
  'Mặc đồ bảo hộ, khẩu trang trong quá trình chế biến, cung cấp đồ ăn',
  'TTLT 13/2016, Phụ lục 04, Mục IV.4.4; QĐ 1246/QĐ-BYT, Điều 4.1.a.', 1, TRUE),
 ('77777777-7777-7777-7777-777777772102', 'CLI-2-2', '77777777-7777-7777-7777-777777777002', '77777777-7777-7777-7777-777777777900',
  'Đảm bảo vệ sinh cá nhân sạch sẽ, gọn gàng tuân thủ quy định VSATTP',
  'Luật ATTP, Điều 29.4.', 2, TRUE),
 ('77777777-7777-7777-7777-777777772103', 'CLI-2-3', '77777777-7777-7777-7777-777777777002', '77777777-7777-7777-7777-777777777900',
  'Không ăn uống, nhai kẹo cao su, hút thuốc lá trong khu vực nhà bếp',
  '[cần xác nhận thêm] — chưa tìm thấy văn bản gốc trong 00_Policies/.', 3, TRUE),
 ('77777777-7777-7777-7777-777777772104', 'CLI-2-4', '77777777-7777-7777-7777-777777777002', '77777777-7777-7777-7777-777777777900',
  'Sử dụng găng tay nilon khi tiếp xúc với thực phẩm chín, lau đũa thìa',
  'QĐ 1246/QĐ-BYT, Điều 4.1.a.', 4, TRUE),

 ('77777777-7777-7777-7777-777777773101', 'CLI-3-1', '77777777-7777-7777-7777-777777777003', '77777777-7777-7777-7777-777777777900',
  'Khu nhập hàng, kho bảo quản đảm bảo vệ sinh; thực phẩm cách nền, tường, trần theo quy định',
  'Luật ATTP Điều 20.1.a; TTLT 13/2016 Phụ lục 04 Mục IV.4.3.', 1, TRUE),
 ('77777777-7777-7777-7777-777777773102', 'CLI-3-2', '77777777-7777-7777-7777-777777777003', '77777777-7777-7777-7777-777777777900',
  'Thực phẩm có đầy đủ thông tin ghi nhãn theo quy định',
  'NĐ 15/2018/NĐ-CP, Điều 24.', 2, TRUE),
 ('77777777-7777-7777-7777-777777773103', 'CLI-3-3', '77777777-7777-7777-7777-777777777003', '77777777-7777-7777-7777-777777777900',
  'Bảo quản đúng cách, đúng nhiệt độ, ngăn ngừa lây nhiễm chéo (sống chín tách biệt)',
  'Luật ATTP, Điều 28.1, Điều 29.1.', 3, TRUE),
 ('77777777-7777-7777-7777-777777773104', 'CLI-3-4', '77777777-7777-7777-7777-777777777003', '77777777-7777-7777-7777-777777777900',
  'Dụng cụ bảo quản tách biệt giữa thực phẩm tươi sống và đã qua chế biến',
  'Luật ATTP, Điều 29.1.', 4, TRUE),
 ('77777777-7777-7777-7777-777777773105', 'CLI-3-5', '77777777-7777-7777-7777-777777777003', '77777777-7777-7777-7777-777777777900',
  'Chế độ theo dõi nhiệt độ bảo quản phù hợp',
  'Luật ATTP, Điều 20.1.b. Tủ lưu mẫu áp dụng thêm 2–8°C của BR-110.', 5, TRUE),
 ('77777777-7777-7777-7777-777777773106', 'CLI-3-6', '77777777-7777-7777-7777-777777777003', '77777777-7777-7777-7777-777777777900',
  'Chế độ vệ sinh khu vực bảo quản định kỳ đảm bảo sạch',
  'Luật ATTP, Điều 20.1.c.', 6, TRUE),
 ('77777777-7777-7777-7777-777777773107', 'CLI-3-7', '77777777-7777-7777-7777-777777777003', '77777777-7777-7777-7777-777777777900',
  'Khu vực bảo quản sắp xếp gọn gàng, ngăn nắp',
  'Luật ATTP, Điều 20.1.a; TTLT 13/2016, Phụ lục 04, Mục IV.4.3.', 7, TRUE),
 -- BR-102 (mới, CF-5) — nơi duy nhất ghi nhận côn trùng cùng CLI-4-6
 ('77777777-7777-7777-7777-777777773108', 'CLI-3-8', '77777777-7777-7777-7777-777777777003', '77777777-7777-7777-7777-777777777900',
  'Không có dấu vết của côn trùng, động vật gây hại',
  'Luật ATTP, Điều 19.1.c, Điều 20.1.b.', 8, TRUE),

 ('77777777-7777-7777-7777-777777774101', 'CLI-4-1', '77777777-7777-7777-7777-777777777004', '77777777-7777-7777-7777-777777777900',
  'Bố trí sắp xếp khu vực bếp theo nguyên tắc 1 chiều',
  'TTLT 13/2016, Phụ lục 04, Mục IV.4.2.', 1, TRUE),
 ('77777777-7777-7777-7777-777777774102', 'CLI-4-2', '77777777-7777-7777-7777-777777777004', '77777777-7777-7777-7777-777777777900',
  'Khu sơ chế có đầy đủ trang thiết bị đảm bảo an toàn sơ chế',
  'Luật ATTP, Điều 19.1.c.', 2, TRUE),
 ('77777777-7777-7777-7777-777777774103', 'CLI-4-3', '77777777-7777-7777-7777-777777777004', '77777777-7777-7777-7777-777777777900',
  'Cống rãnh thông thoát nước, không ứ đọng và được che đậy',
  'Luật ATTP, Điều 28.4.', 3, TRUE),
 ('77777777-7777-7777-7777-777777774104', 'CLI-4-4', '77777777-7777-7777-7777-777777777004', '77777777-7777-7777-7777-777777777900',
  'Dụng cụ thu gom rác thải có nắp đậy',
  'Luật ATTP, Điều 28.3.', 4, TRUE),
 ('77777777-7777-7777-7777-777777774105', 'CLI-4-5', '77777777-7777-7777-7777-777777777004', '77777777-7777-7777-7777-777777777900',
  'Dụng cụ sơ chế riêng biệt đối với thực phẩm tươi sống',
  'Luật ATTP, Điều 29.1.', 5, TRUE),
 -- BR-102 (mới, CF-5) — cùng CLI-3-8
 ('77777777-7777-7777-7777-777777774106', 'CLI-4-6', '77777777-7777-7777-7777-777777777004', '77777777-7777-7777-7777-777777777900',
  'Khu chế biến có phương tiện phòng chống côn trùng và động vật gây hại',
  'Luật ATTP, Điều 28.5; TTLT 13/2016 Phụ lục 04 Mục IV.4.1.', 6, TRUE),
 ('77777777-7777-7777-7777-777777774107', 'CLI-4-7', '77777777-7777-7777-7777-777777777004', '77777777-7777-7777-7777-777777777900',
  'Dụng cụ, thiết bị chế biến đảm bảo vệ sinh, dùng riêng cho thực phẩm đã qua chế biến',
  'Luật ATTP, Điều 29.1, 29.2.', 7, TRUE),
 ('77777777-7777-7777-7777-777777774108', 'CLI-4-8', '77777777-7777-7777-7777-777777777004', '77777777-7777-7777-7777-777777777900',
  'Chế biến trên bàn cao mặt đất tối thiểu 60 cm',
  'Luật ATTP, Điều 30.3.', 8, TRUE),
 ('77777777-7777-7777-7777-777777774109', 'CLI-4-9', '77777777-7777-7777-7777-777777777004', '77777777-7777-7777-7777-777777777900',
  'Có đầy đủ phương tiện rửa tay, hóa chất diệt khuẩn cho nhân viên',
  'Luật ATTP, Điều 28.6; TTLT 13/2016, Phụ lục 04, Mục IV.4.1.', 9, TRUE),
 ('77777777-7777-7777-7777-777777774110', 'CLI-4-10', '77777777-7777-7777-7777-777777777004', '77777777-7777-7777-7777-777777777900',
  'Chất tẩy rửa có nguồn gốc, không dùng sản phẩm trôi nổi',
  'Luật ATTP, Điều 23.1.đ.', 10, TRUE),
 ('77777777-7777-7777-7777-777777774111', 'CLI-4-11', '77777777-7777-7777-7777-777777777004', '77777777-7777-7777-7777-777777777900',
  'Đảm bảo các biện pháp phòng cháy, chữa cháy theo quy định',
  '[cần xác nhận thêm] — pháp luật PCCC, ngoài Luật ATTP.', 11, TRUE),

 ('77777777-7777-7777-7777-777777775101', 'CLI-5-1', '77777777-7777-7777-7777-777777777005', '77777777-7777-7777-7777-777777777900',
  'Nguyên liệu có nguồn gốc, kiểm định đúng quy định, rửa sạch trước khi chế biến',
  'Luật ATTP Điều 26.1; QĐ 1246/QĐ-BYT Điều 3.', 1, TRUE),
 ('77777777-7777-7777-7777-777777775102', 'CLI-5-2', '77777777-7777-7777-7777-777777777005', '77777777-7777-7777-7777-777777777900',
  'Không dùng thực phẩm quá hạn, ôi, thiu, hỏng để chế biến',
  'Luật ATTP Điều 26.1; QĐ 1246/QĐ-BYT Điều 3.1.c.', 2, TRUE),
 ('77777777-7777-7777-7777-777777775103', 'CLI-5-3', '77777777-7777-7777-7777-777777777005', '77777777-7777-7777-7777-777777777900',
  'Thức ăn, đồ uống được nấu chín/đủ điều kiện chín trước khi cung cấp',
  'Luật ATTP Điều 30.2; QĐ 1246/QĐ-BYT Điều 5.', 3, TRUE),
 ('77777777-7777-7777-7777-777777775104', 'CLI-5-4', '77777777-7777-7777-7777-777777777005', '77777777-7777-7777-7777-777777777900',
  'Không dùng phụ gia, phẩm màu, chất ngọt tổng hợp gây hại',
  'Luật ATTP Điều 26.2; NĐ 15/2018/NĐ-CP Điều 33.2.', 4, TRUE),

 ('77777777-7777-7777-7777-777777776101', 'CLI-6-1', '77777777-7777-7777-7777-777777777006', '77777777-7777-7777-7777-777777777900',
  'Lưu mẫu đúng quy định (số lượng, thời gian, dụng cụ, bảo quản) và ghi sổ 3 bước đúng quy định',
  'QĐ 1246/QĐ-BYT, Điều 3–8. Kiểm tra chéo — dữ liệu chính quản lý tại UC-079/UC-076/UC-118 (BR-105–114, BR-110).', 1, TRUE),
 ('77777777-7777-7777-7777-777777776102', 'CLI-6-2', '77777777-7777-7777-7777-777777777006', '77777777-7777-7777-7777-777777777900',
  'Lưu đủ mẫu, đúng tên thực phẩm, đủ định lượng mẫu lưu',
  'QĐ 1246/QĐ-BYT, Điều 1.2, Điều 7.2.', 2, TRUE);
