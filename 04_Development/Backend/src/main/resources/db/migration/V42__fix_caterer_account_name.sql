-- Sửa dữ liệu: tài khoản Service Caterers seed ở V19 (user_accounts.id
-- 44444444-4444-4444-4444-444444444448, code U-9911, email
-- catering.synthetic@anbinhcatering.vn) mang tên hiển thị "Cty TNHH Suất ăn An Bình
-- (synthetic)", nhưng partner nghiệp vụ nó thực sự gắn (CAT-101, xem `MenuAssembler
-- .assertCatererOwnership` — khoá theo `partnerId`, KHÔNG phải theo tên tài khoản) lại tên
-- "Đơn vị suất ăn Hoa Mai" (V6/seed MF04). Một trường chỉ hợp tác với ĐÚNG 1 nhà thầu suất ăn
-- (xác nhận nghiệp vụ 06/08/2026) nên đây chắc chắn là một thực thể — hai cái tên khác nhau là
-- seed sai, không phải 2 công ty thật. Đổi tên tài khoản khớp với partners.name (nguồn nghiệp
-- vụ chính) thay vì đổi ngược lại, để không phải sửa thêm mọi chỗ đã tham chiếu CAT-101.
UPDATE user_accounts
SET name = 'Đơn vị suất ăn Hoa Mai (synthetic)'
WHERE id = '44444444-4444-4444-4444-444444444448'
  AND email = 'catering.synthetic@anbinhcatering.vn';
