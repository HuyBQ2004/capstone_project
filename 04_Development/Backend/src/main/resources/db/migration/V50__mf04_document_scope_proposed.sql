-- ═══════════════════════════════════════════════════════════════════════════════
-- V50__mf04_document_scope_proposed.sql — hồ sơ CÁ NHÂN nhân viên giao hàng
-- (PROPOSED — chưa duyệt chính thức).
--
-- Nguồn: 07_implement/MF04_Supplier_Partner_Management/Proposal-Partnership-Catalog-
-- Contract.md §"Phase 3". Lưu ý: "chứng từ theo LÔ HÀNG" đã có cơ chế THẬT riêng
-- (UC-100/BR-071 — bảng nối `batch_certificates`, xem `SupplierBatchService.
-- uploadCertificate()`) — KHÔNG dựng thêm cột `supplier_batch_id` ở đây để tránh 2 cơ
-- chế song song cho cùng một việc (rà lại 07/08/2026, bỏ cột này khỏi bản đầu của
-- migration). Migration này CHỈ còn phần hồ sơ cá nhân, vốn chưa có cơ chế nào.
-- ═══════════════════════════════════════════════════════════════════════════════

ALTER TABLE partner_documents
  ADD COLUMN delivery_person_id UUID REFERENCES delivery_persons(id);

CREATE INDEX ix_pd_person ON partner_documents(delivery_person_id);

-- Mở rộng document_type cho hồ sơ CÁ NHÂN (giấy khám sức khoẻ, xác nhận kiến thức ATTP) —
-- constraint gốc là CHECK không tên trên cột (đặt ở V6/V37, Postgres tự đặt tên
-- `partner_documents_document_type_check`).
ALTER TABLE partner_documents DROP CONSTRAINT partner_documents_document_type_check;
ALTER TABLE partner_documents ADD CONSTRAINT ck_pd_document_type CHECK (
  document_type IN (
    'FOOD_SAFETY_CERT', 'PRODUCT_ORIGIN', 'BUSINESS_LICENSE', 'SELF_DECLARATION',
    'HEALTH_CERTIFICATE', 'FOOD_SAFETY_KNOWLEDGE_CERT'
  )
);
