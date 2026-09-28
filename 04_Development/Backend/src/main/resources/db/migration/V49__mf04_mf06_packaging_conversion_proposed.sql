-- ═══════════════════════════════════════════════════════════════════════════════
-- V49__mf04_mf06_packaging_conversion_proposed.sql — quy đổi số lượng đặt hàng theo
-- quy cách đóng gói (PROPOSED — chưa duyệt chính thức).
--
-- Nguồn: 07_implement/MF04_Supplier_Partner_Management/Proposal-Partnership-Catalog-
-- Contract.md §"Phase 2". `packaging_size` = số lượng/1 kiện theo `unit` đặt hàng
-- (VD 50 = bao 50kg). `allow_partial_package = false` ⇒ chỉ được đặt bội số
-- `packaging_size` (VD sữa hộp bán nguyên thùng). `purchase_items.requested_quantity`
-- giữ số lượng THỰC TẾ trường cần trước khi quy đổi; `quantity` (cột đã có) trở thành
-- số lượng ĐÃ QUY ĐỔI/thực đặt — không đổi ngữ nghĩa cột cũ ở nơi khác đang đọc nó.
-- ═══════════════════════════════════════════════════════════════════════════════

ALTER TABLE partnership_catalog_items
  ADD COLUMN packaging_size NUMERIC(12,3),
  ADD COLUMN allow_partial_package BOOLEAN NOT NULL DEFAULT true;

ALTER TABLE catalog_change_requests
  ADD COLUMN requested_packaging_size NUMERIC(12,3),
  ADD COLUMN requested_allow_partial_package BOOLEAN NOT NULL DEFAULT true;

ALTER TABLE purchase_items
  ADD COLUMN requested_quantity NUMERIC(12,3);
