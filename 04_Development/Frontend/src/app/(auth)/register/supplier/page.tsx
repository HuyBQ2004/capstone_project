"use client";

import { RegisterForm } from "@/components/app/register-form";
import { api } from "@/lib/api/client";

/**
 * Register Supplier — UC-071 (13_Guest_Common_Screen_Flow.md mục 3, Report 3 §3.3.2.1.3).
 * `POST /partners/register/supplier` (permitAll) — field khớp `PartnerDtos.RegisterPartnerRequest`.
 */
export default function RegisterSupplierPage() {
  return (
    <RegisterForm
      title="Đăng ký Nhà cung cấp"
      ucLabel="UC-071"
      description="Hồ sơ + chứng nhận ATTP sẽ được School Administration xem xét khi xin hợp tác (BR-046/BR-047)."
      fields={[
        { id: "companyName", label: "Tên công ty (Company Name)", placeholder: "VD: Cty CP Thực phẩm sạch GreenFarm" },
        { id: "licenseNo", label: "Số giấy phép kinh doanh (Business License No)", placeholder: "VD: 0101234567" },
        { id: "address", label: "Địa chỉ (Address)", placeholder: "Số nhà, phường/xã, quận/huyện, tỉnh/thành" },
        { id: "contactPerson", label: "Người liên hệ (Contact Person)", placeholder: "Họ tên người phụ trách" },
        { id: "email", label: "Email", kind: "email", placeholder: "lienhe@congty.vn" },
        { id: "phone", label: "Số điện thoại (Phone)", kind: "phone", placeholder: "+84xxxxxxxxx hoặc 0xxxxxxxxx" },
      ]}
      docs={[
        { id: "businessLicense", label: "Ảnh giấy phép kinh doanh" },
        { id: "foodSafetyCert", label: "Chứng nhận an toàn thực phẩm còn hiệu lực (BR-047)" },
      ]}
      onSubmit={async (values, files) => {
        const payload = {
          companyName: values.companyName,
          licenseNo: values.licenseNo,
          address: values.address,
          contactPerson: values.contactPerson,
          email: values.email,
          phone: values.phone,
        };
        const form = new FormData();
        form.append("payload", new Blob([JSON.stringify(payload)], { type: "application/json" }));
        for (const file of files) form.append("documents", file);
        const { data } = await api.post<{ id: string }>("/partners/register/supplier", form);
        return { code: data.id };
      }}
    />
  );
}
