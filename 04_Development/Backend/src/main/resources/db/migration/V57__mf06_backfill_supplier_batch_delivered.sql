-- ═══════════════════════════════════════════════════════════════════════════════
-- MF06 — đóng những lô NCC đã thực sự được nhận vào kho.
--
-- Bối cảnh (08/08/2026, mục A8 backlog): `SupplierBatchStatus.DELIVERED` chưa từng
-- được gán ở BẤT KỲ đâu trong backend — lô NCC vĩnh viễn `ACTIVE` với đủ số lượng
-- ban đầu, kể cả sau khi đã giao và nhập kho xong. Hệ quả: màn "Lô nguyên liệu" của
-- NCC không phản ánh được lô nào đã đi, và chốt chặn "một lô chỉ nhận một lần" (thêm
-- cùng ngày ở `ReceivingSessionService`) không có tác dụng với dữ liệu cũ vì nó dựa
-- đúng vào trạng thái này.
--
-- Backfill theo BẰNG CHỨNG, không đoán: chỉ đóng lô nào có ít nhất một dòng phiếu
-- nhận KHÔNG bị từ chối trỏ tới. Lô bị từ chối toàn bộ thì vẫn còn hàng ở NCC ⇒ giữ
-- `ACTIVE`. Lô chưa ai nhận cũng giữ `ACTIVE`.
-- ═══════════════════════════════════════════════════════════════════════════════

UPDATE supplier_batches sb
SET status = 'DELIVERED'
WHERE sb.status = 'ACTIVE'
  AND EXISTS (
      SELECT 1
      FROM receiving_lines rl
      WHERE rl.supplier_batch_id = sb.id
        AND rl.rejected = FALSE
  );
