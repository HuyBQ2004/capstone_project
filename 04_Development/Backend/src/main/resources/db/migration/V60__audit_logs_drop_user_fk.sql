-- Bỏ khoá ngoại `audit_logs.actor_user_id -> user_accounts(id)` (09/08/2026).
--
-- ══ VÌ SAO ═════════════════════════════════════════════════════════════════════════════════
--
-- Khoá ngoại này làm ĐĂNG NHẬP GOOGLE LẦN ĐẦU treo vô hạn, và nó không thể sửa được bằng cách
-- đổi thứ tự lệnh trong code.
--
-- Cơ chế đo được (pg_stat_activity + stack trace):
--   · Mỗi lần chèn `audit_logs`, Postgres phải kiểm khoá ngoại bằng
--     `SELECT 1 FROM user_accounts WHERE id=$1 FOR KEY SHARE`.
--   · `AuditService` là `REQUIRES_NEW` ⇒ chạy ở transaction và KẾT NỐI KHÁC.
--   · Luồng đăng nhập Google cập nhật `user_accounts.google_sub` — cột có UNIQUE INDEX
--     (`user_accounts_google_sub_key`). Sửa cột nằm trong unique index là *key update* ⇒ Postgres
--     giữ khoá mạnh trên dòng đó **cho tới hết transaction**, và khoá đó XUNG ĐỘT với
--     `FOR KEY SHARE`.
--   · Transaction ngoài lại đang đứng chờ chính lời gọi audit trả về ⇒ hai bên chờ nhau.
--     Postgres KHÔNG phát hiện được vì phiên ngoài ở trạng thái `idle in transaction`
--     (chờ client, không phải chờ khoá) nên không thành chu trình trong đồ thị khoá.
--
-- Đăng nhập bằng mật khẩu thoát nạn hoàn toàn do may: nó chỉ đổi `last_login_at`,
-- `failed_login_count` — cột thường ⇒ chỉ lấy `FOR NO KEY UPDATE`, không xung đột.
--
-- ĐÃ THỬ và KHÔNG ĐỦ: đảo thứ tự "ghi audit trước, update sau" trong `OAuth2LoginService`.
-- Khoá mạnh vẫn bị giữ từ bước liên kết `google_sub`, nên lần ghi audit tiếp theo trong
-- `AuthService.completeLogin` (đường dùng chung của mọi kiểu đăng nhập) lại kẹt y hệt.
--
-- ══ VÌ SAO BỎ HẲN LÀ ĐÚNG, KHÔNG PHẢI CHỮA CHÁY ════════════════════════════════════════════
--
-- 1. Nhật ký kiểm toán là LỊCH SỬ BẤT BIẾN. Nó phải tồn tại kể cả khi tài khoản bị xoá — đó
--    chính là lúc người ta cần đọc nó nhất. Khoá ngoại ép ta chọn giữa hai điều đều sai:
--    `ON DELETE CASCADE` (xoá luôn dấu vết) hoặc chặn không cho xoá tài khoản.
-- 2. `actor_user_id` chỉ để tra cứu; `ix_audit_actor_user` đã lo phần truy vấn, và cột `actor`
--    (email) vẫn giữ danh tính đọc được kể cả khi dòng người dùng không còn.
-- 3. Ràng buộc toàn vẹn ở đây không mua được gì: giá trị do chính server tra ra ngay trước khi
--    ghi, không phải dữ liệu client gửi lên.
--
-- Hàng rào kèm theo (không thay thế bản sửa này): `lock_timeout = 10s` ở
-- `application.yml` — lần sau lớp lỗi này sẽ ném lỗi kèm stack trace trong 10 giây thay vì
-- treo câm lặng cho tới khi nginx trả 504.
ALTER TABLE audit_logs DROP CONSTRAINT IF EXISTS audit_logs_actor_user_id_fkey;

COMMENT ON COLUMN audit_logs.actor_user_id IS
    'ID người thực hiện — CỐ Ý không có khoá ngoại tới user_accounts (V60): nhật ký kiểm toán là '
    'lịch sử bất biến, phải sống sót khi tài khoản bị xoá, và khoá ngoại gây kẹt khoá với các '
    'transaction đang sửa cột unique của user_accounts. Tra cứu qua ix_audit_actor_user.';
