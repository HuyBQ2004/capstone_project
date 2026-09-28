-- ─────────────────────────────────────────────────────────────────────────────
-- V76 (13/08/2026, Nam) — bù dòng RECEIPT còn thiếu trong sổ kho.
--
-- BỐI CẢNH: tồn khả dụng của một lô đổi từ `initial_quantity + SUM(delta)` sang `SUM(delta)`
-- thuần (đúng ADR-M06-1, xem InventoryService.remainingOf). Lý do: từ 10/08/2026
-- ReceivingSessionService ghi RECEIPT cho cả lần nhận ĐẦU TIÊN, nên công thức cũ đếm lần nhận
-- đó HAI LẦN (đo trên production: BATCH-B7F8E062 nhập 52 hộp, sổ có +52, hệ thống báo tồn 104).
--
-- Nhưng các lô nhận TRƯỚC 10/08 lại KHÔNG có dòng RECEIPT nào cho lần nhận đầu — nếu đổi công
-- thức mà không bù, tồn của chúng tụt về 0. Migration này dựng lại phần còn thiếu.
--
-- CÁCH DỰNG (chính xác cho cả lô nhận NHIỀU ĐỢT, BR-071):
--   ky_vong = TỔNG receiving_lines.received_quantity của lô (nguồn sự thật: biên bản nhận hàng)
--             — nếu lô không sinh ra từ phiên nhận nào (dữ liệu seed/demo) thì lấy
--               initial_quantity.
--   da_ghi  = TỔNG delta của các dòng RECEIPT đang có trong sổ.
--   thieu   = ky_vong - da_ghi;  thieu > 0 thì ghi bù MỘT dòng đúng phần chênh.
--
-- Cách này KHÔNG giả định "trước/sau ngày X", nên chạy lại nhiều lần vẫn đúng (idempotent):
-- lần thứ hai `thieu` = 0, không ghi gì thêm.
--
-- Nối receiving_lines với inventory_batches qua supplier_batches.code + school_id vì
-- receiving_lines KHÔNG có cột batch_code (nó giữ supplier_batch_id).
-- ─────────────────────────────────────────────────────────────────────────────

INSERT INTO inventory_transactions
    (code, school_id, occurred_at, type, batch_id, ingredient_id, delta, unit, performed_by_user_id)
SELECT
    -- ≤32 ký tự và duy nhất theo batch (uq_tx_school_code). Dùng md5 chứ KHÔNG cắt 25 ký tự đầu
    -- của chính uuid: uuid trong seed được đánh tay theo dãy ('...101', '...102') nên chỉ khác
    -- nhau ở các ký tự CUỐI — cắt đầu là đụng uq_tx_school_code ngay (đã vấp khi chạy test).
    'TXN-BF-' || substring(md5(b.id::text), 1, 25),
    b.school_id,
    b.created_at,
    'RECEIPT',
    b.id,
    b.ingredient_id,
    calc.thieu,
    b.unit,
    -- Ưu tiên người đã ký phiên nhận của chính lô này; không có thì lấy một tài khoản bất kỳ
    -- của trường (cột NOT NULL, chỉ mang tính truy vết cho dòng dựng lại).
    COALESCE(
        (SELECT rs.received_by_user_id FROM receiving_sessions rs WHERE rs.id = b.receiving_session_id),
        (SELECT ua.id FROM user_accounts ua WHERE ua.school_id = b.school_id ORDER BY ua.created_at LIMIT 1),
        (SELECT ua.id FROM user_accounts ua ORDER BY ua.created_at LIMIT 1)
    )
FROM inventory_batches b
CROSS JOIN LATERAL (
    SELECT COALESCE(
               (SELECT SUM(rl.received_quantity)
                FROM receiving_lines rl
                JOIN receiving_sessions rs2 ON rs2.id = rl.session_id
                JOIN supplier_batches sb    ON sb.id = rl.supplier_batch_id
                WHERE rl.rejected = FALSE
                  AND rs2.school_id = b.school_id
                  AND sb.code = b.code),
               b.initial_quantity)
           - COALESCE((SELECT SUM(t.delta) FROM inventory_transactions t
                       WHERE t.batch_id = b.id AND t.type = 'RECEIPT'), 0)
           AS thieu
) AS calc
WHERE calc.thieu > 0;
