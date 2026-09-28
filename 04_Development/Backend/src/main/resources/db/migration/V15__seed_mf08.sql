

-- ───────────────────────── Nơi tiếp nhận mẫu (RECEIVING_AUTHORITIES) ─────────────────────────
INSERT INTO receiving_authorities (id, code, name, type, contact, address, is_active, note)
VALUES
 ('88888888-8888-8888-8888-888888888301', 'LAB-CDC-HN', 'CDC Hà Nội', 'LAB',
  'Nguyễn Thị Vân — 024 3736 1211', '70 Nguyễn Chí Thanh, Đống Đa, Hà Nội', TRUE,
  'Cử cán bộ tới trường lấy mẫu trong ngày làm việc'),
 ('88888888-8888-8888-8888-888888888302', 'LAB-NIFC', 'Viện Kiểm nghiệm ATVSTP Quốc gia', 'LAB',
  'Phòng tiếp nhận mẫu — 024 3971 4045', '65 Phạm Thận Duật, Cầu Giấy, Hà Nội', TRUE, NULL),
 ('88888888-8888-8888-8888-888888888303', 'LAB-TTYT-QUAN', 'Trung tâm Y tế quận Nam Từ Liêm', 'LAB',
  'Khoa Xét nghiệm — 024 3837 2222', 'Mỹ Đình 2, Nam Từ Liêm, Hà Nội', TRUE, NULL),
 ('88888888-8888-8888-8888-888888888304', 'AUTH-CA-QUAN', 'Công an quận Nam Từ Liêm', 'AUTHORITY',
  'Đội Cảnh sát kinh tế — 069 219 5555', NULL, TRUE,
  'Thu giữ mẫu làm vật chứng; trường giữ biên bản thu giữ, không nhận kết quả xét nghiệm'),
 ('88888888-8888-8888-8888-888888888305', 'AUTH-TT-SYT', 'Thanh tra Sở Y tế Hà Nội', 'AUTHORITY',
  'Phòng Thanh tra — 024 3998 5765', NULL, TRUE, 'Thu mẫu phục vụ thanh tra ATTP'),
 -- Thêm mới, KHÔNG có trong mock — TestSpec-MF08 FX-008 cần ≥1 bản ghi is_active=false để
 -- viết được test lọc `isActive`. Mock chỉ có 5 dòng, tất cả active=true.
 ('88888888-8888-8888-8888-888888888306', 'LAB-NGUNG-HOP-TAC', 'Phòng xét nghiệm Ánh Dương (ngừng hợp tác)',
  'LAB', 'Đã chấm dứt hợp đồng 2026-05', NULL, FALSE,
  'Ngừng hợp tác từ 2026-05 — chỉ giữ lại để đọc dữ liệu phiếu bàn giao cũ')
ON CONFLICT (code) DO NOTHING;

-- ⚠️ KHÔNG seed sample_attachments (ảnh bằng chứng): seed bỏ qua validate BR-107 ở tầng API
-- (giống cách các module khác không seed file nhị phân thật). Không ảnh hưởng BR-203/BR-111/
-- BR-112/BR-113 vì không CHECK constraint nào ở DB đòi ≥1 attachment cho bản ghi lịch sử.
