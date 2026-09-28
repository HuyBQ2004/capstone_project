"use client";

import { RegisterForm } from "@/components/app/register-form";
import { api } from "@/lib/api/client";

/**
 * Register School — UC-070 (13_Guest_Common_Screen_Flow.md mục 2, Report 3 §3.3.2.1.2).
 * `POST /schools/register` (permitAll, SecurityConfig) — field khớp `SchoolDtos.RegisterSchoolRequest`:
 * name/taxId/email/address/lat/lng bắt buộc; operatingHours/emergencyContact tùy chọn.
 * Thành công (201) → MSG-032, hồ sơ PENDING chờ System Admin phê duyệt.
 */
export default function RegisterSchoolPage() {
  return (
    <RegisterForm
      title="Đăng ký Trường học"
      ucLabel="UC-070"
      description="Hồ sơ sẽ ở trạng thái Pending chờ System Admin phê duyệt (MSG-032)."
      withLocation
      fields={[
        { id: "name", label: "Tên trường (School Name)", placeholder: "VD: Trường Tiểu học Đoàn Thị Điểm" },
        { id: "taxId", label: "Mã số thuế (Tax ID)", placeholder: "VD: 0109988776" },
        { id: "address", label: "Địa chỉ (Address)", placeholder: "Số nhà, phường/xã, quận/huyện, tỉnh/thành" },
        { id: "email", label: "Email", kind: "email", placeholder: "ten@truong.edu.vn" },
      ]}
      docs={[
        { id: "license", label: "Giấy phép hoạt động & tài liệu đăng ký (UC-070 bước 2)" },
      ]}
      onSubmit={async (values, files) => {
        const payload = {
          name: values.name,
          taxId: values.taxId,
          email: values.email,
          address: values.address,
          lat: parseFloat(values.lat),
          lng: parseFloat(values.lng),
        };
        const form = new FormData();
        form.append("payload", new Blob([JSON.stringify(payload)], { type: "application/json" }));
        for (const file of files) form.append("documents", file);
        const { data } = await api.post<{ id: string }>("/schools/register", form);
        return { code: data.id };
      }}
    />
  );
}
