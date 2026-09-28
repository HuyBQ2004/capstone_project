"use client";

import { RegisterForm } from "@/components/app/register-form";
import { api } from "@/lib/api/client";

/**
 * Register Service Caterers — 13_Guest_Common_Screen_Flow.md mục 4 (Report 3 §3.3.2.1.4).
 * `POST /partners/register/caterer` (permitAll) — cùng DTO `RegisterPartnerRequest` với Supplier
 * (chỉ khác endpoint/partnerType do backend tự set), field đổi tên cho khớp label caterer.
 */
export default function RegisterCatererPage() {
  return (
    <RegisterForm
      title="Đăng ký Đơn vị Suất ăn"
      ucLabel="Service Caterers"
      description="Hồ sơ năng lực sẽ được School Administration xem xét khi thiết lập hợp tác."
      fields={[
        { id: "companyName", label: "Tên đơn vị (Caterer Name)", placeholder: "VD: Cty TNHH Suất ăn An Bình" },
        { id: "licenseNo", label: "Số đăng ký kinh doanh (Business Reg No)", placeholder: "VD: 0109876543" },
        { id: "address", label: "Địa chỉ văn phòng (Office Address)", placeholder: "Số nhà, phường/xã, quận/huyện, tỉnh/thành" },
        { id: "contactPerson", label: "Người liên hệ (Contact Person)", placeholder: "Họ tên người phụ trách" },
        { id: "email", label: "Email", kind: "email", placeholder: "lienhe@congty.vn" },
        { id: "phone", label: "Số điện thoại (Phone)", kind: "phone", placeholder: "+84xxxxxxxxx hoặc 0xxxxxxxxx" },
      ]}
      docs={[
        { id: "businessLicense", label: "Ảnh giấy phép kinh doanh" },
        { id: "foodSafetyCert", label: "Giấy chứng nhận đủ điều kiện ATTP" },
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
        const { data } = await api.post<{ id: string }>("/partners/register/caterer", form);
        return { code: data.id };
      }}
    />
  );
}
