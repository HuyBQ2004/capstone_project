"use client";

import { PartnershipWorkspace } from "@/components/app/partnership-workspace";

/**
 * Trường hợp tác — vai Nhà cung cấp (UC-152/169/170).
 *
 * Toàn bộ nội dung nay ở `components/app/partnership-workspace.tsx`, dùng chung với Đơn vị
 * suất ăn (`service-caterers/partnerships`): cùng endpoint, cùng quy trình duyệt, chỉ khác
 * ở chỗ NCC có danh mục sản phẩm-giá để chào bán.
 */
export default function SupplierPartnershipsPage() {
  return (
    <PartnershipWorkspace
      description="Hồ sơ hợp tác cần School Administration phê duyệt trước khi giao hàng (BR-040/BR-041); tài liệu tuân thủ phải còn hiệu lực (BR-046/BR-047)"
      catalogHref={(id) => `/supplier/partnerships/${id}/catalog`}
    />
  );
}
