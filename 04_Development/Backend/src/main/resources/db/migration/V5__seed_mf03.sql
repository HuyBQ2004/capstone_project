

-- registration_no ở trên dùng thủ công DTD-2026-0001..0004 (KHÔNG qua CodeGenerator) — nếu
-- không tiến bộ đếm 'mf03_registration_no:2026' trong code_sequences thì lần POST /students
-- đầu tiên qua API sẽ sinh lại "DTD-2026-0001" và vỡ UNIQUE (bug thấy được khi chạy thật
-- StudentCreateIT/PublicIdContractIT). registrationNo giữ khuôn "<prefix>-<số 4 chữ số>" của
-- CodeGenerator.next(scope, prefix, width) — prefix = "DTD-2026" (đã gồm năm).
INSERT INTO code_sequences (scope, next_value) VALUES ('mf03_registration_no:2026', 5)
ON CONFLICT (scope) DO UPDATE SET next_value = GREATEST(code_sequences.next_value, 5);

-- ROOT CAUSE tìm được bằng cách chạy thật (Testcontainers) — không phải giả định trước: MF01
-- seed (db/testseed/V900, chạy CÙNG db/migration trong AbstractIamIntegrationTest) chèn tay
-- ~21 user_accounts (U-0001..U-0021, U-9000, U-9001) KHÔNG qua CodeGenerator, nên bộ đếm
-- code_sequences scope='users' vẫn ở giá trị mặc định (1). Khi MF03 tạo tài khoản PH mới qua
-- ParentAccountPort -> UserService.create() -> CodeGenerator.nextUserCode(), mã sinh ra ĐẦU
-- TIÊN trùng "U-0001" — user_accounts.code chỉ UNIQUE theo (school_id, code) (V1 DDL), nên 2
-- dòng "U-0001" ở 2 trường KHÁC NHAU được chèn thành công, và UserAccountRepository.findByCode()
-- (tra cứu KHÔNG lọc theo trường) sau đó ném NonUniqueResultException khi tra lại user vừa tạo
-- (ParentAccountPortImpl.createParentAccount) — vỡ toàn bộ luồng POST /students có tạo PH mới.
-- Không sửa V900 (thuộc MF01/MF02) — chỉ tiến bộ đếm chung 'users' lên trên dải đã dùng.
INSERT INTO code_sequences (scope, next_value) VALUES ('users', 100)
ON CONFLICT (scope) DO UPDATE SET next_value = GREATEST(code_sequences.next_value, 100);
