-- ═══════════════════════════════════════════════════════════════════════════════
-- MF06 — cho phép MỘT lô NCC được giao NHIỀU ĐỢT / NHIỀU TRƯỜNG (10/08/2026).
--
-- Bối cảnh: chốt chặn 08/08/2026 ("một lô chỉ nhận một lần", xem V57 + comment cũ ở
-- `ReceivingSessionService`) là đúng để chặn cộng khống tồn kho NGAY LÚC ĐÓ, vì phiếu
-- nhận không có ô số lượng nên mọi lần nhận đều cộng NGUYÊN `supplier_batches.quantity`.
-- Ngoài đời thật một lô sản xuất của NCC thường được CHIA giao cho nhiều trường, hoặc
-- giao nhiều đợt cho cùng một trường — chốt chặn cũ chặn luôn cả các trường hợp hợp lệ.
--
-- Hướng xử lý ĐÚNG như comment cũ đã cảnh báo: thêm cột số lượng nhận vào từng dòng
-- phiếu nhận, rồi đổi luật thành "tổng đã nhận (kể cả lần này) <= số lượng lô đăng ký".
-- ═══════════════════════════════════════════════════════════════════════════════

-- Lượng nhận THỰC TẾ của từng dòng phiên nhận — NULL cho dòng bị từ chối (không nhận gì).
ALTER TABLE receiving_lines ADD COLUMN received_quantity NUMERIC(12,3);
ALTER TABLE receiving_lines ADD CONSTRAINT ck_line_received_qty CHECK (
  (rejected = TRUE AND received_quantity IS NULL)
  OR (rejected = FALSE AND received_quantity > 0));

-- Backfill: dữ liệu cũ nhận "trọn lô" mỗi lần (đúng mô hình all-or-nothing trước đây) —
-- lượng nhận của dòng KHÔNG bị từ chối = số lượng đăng ký của chính lô đó.
UPDATE receiving_lines rl
SET received_quantity = sb.quantity
FROM supplier_batches sb
WHERE rl.supplier_batch_id = sb.id
  AND rl.rejected = FALSE;

-- Số lượng CÒN LẠI của lô, giảm dần theo từng lần nhận — nguồn sự thật cho luật
-- "tổng đã nhận <= số lượng đăng ký". Lô mới tạo: remaining_quantity = quantity
-- (gán ở tầng service, không đặt DEFAULT tham chiếu cột khác được ở Postgres).
ALTER TABLE supplier_batches ADD COLUMN remaining_quantity NUMERIC(12,3) NOT NULL DEFAULT 0;
ALTER TABLE supplier_batches ADD CONSTRAINT ck_sb_remaining_range
  CHECK (remaining_quantity >= 0 AND remaining_quantity <= quantity);

-- Backfill: quantity trừ đi tổng đã nhận (dòng không bị từ chối), không âm.
UPDATE supplier_batches sb
SET remaining_quantity = GREATEST(sb.quantity - COALESCE(recv.total, 0), 0)
FROM (
    SELECT supplier_batch_id, SUM(received_quantity) AS total
    FROM receiving_lines
    WHERE rejected = FALSE
    GROUP BY supplier_batch_id
) recv
WHERE sb.id = recv.supplier_batch_id;

-- Lô chưa từng được nhận (không có dòng receiving_lines nào trỏ tới) vẫn còn ACTIVE
-- với đủ số lượng ban đầu — remaining_quantity = quantity.
UPDATE supplier_batches sb
SET remaining_quantity = sb.quantity
WHERE NOT EXISTS (
    SELECT 1 FROM receiving_lines rl WHERE rl.supplier_batch_id = sb.id AND rl.rejected = FALSE
);
