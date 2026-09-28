"use client";

import { PartnershipWorkspace } from "@/components/app/partnership-workspace";

/**
 * Trường hợp tác — vai Đơn vị suất ăn (UC-156/158/159).
 *
 * THÊM 08/08/2026. Trước đó vai này **không có màn nào để liên kết với trường**: backend đã
 * cấp quyền `SERVICE_CATERERS` ngang `SUPPLIER` ở `PartnershipApplicationController` (xem
 * cổng nộp · nộp đơn · nộp lại) và `PartnerApprovalServiceImpl` tự tạo `SupplierPartnership`
 * khi Ban giám hiệu duyệt — nhưng FE chỉ có màn cho NCC.
 *
 * Liên kết duy nhất từng tồn tại là một dòng cắm sẵn ở seed (`V38`, status APPROVED), nên
 * chưa ai đi qua luồng thật. Xoá seed (08/08/2026) là lộ ra: đăng ký tài khoản xong không
 * vào được trường nào để lập thực đơn — chặn ngay đầu chuỗi demo.
 *
 * KHÔNG có `catalogHref`: đơn vị suất ăn nấu ăn chứ không chào bán mặt hàng, danh mục
 * sản phẩm-giá là việc của NCC.
 */
export default function ServiceCaterersPartnershipsPage() {
  return (
    <PartnershipWorkspace
      exclusive
      description="Nộp hồ sơ xin phục vụ suất ăn cho trường; Ban giám hiệu phê duyệt trước khi bắt đầu (BR-040/BR-041). Giấy phép kinh doanh và chứng nhận ATTP phải còn hiệu lực (BR-046/BR-047)"
    />
  );
}
