import { RegisterForm } from "@/components/app/register-form";

/**
 * Register School — UC-070 (13_Guest_Common_Screen_Flow.md mục 2, Report 3 §3.3.2.1.2).
 * Field: School Name, Address, School Level, Representative Name, Email, Phone
 * + upload giấy phép (UC-070 bước 2). Thành công → MSG-032 + MSG-028.
 * Review 07/2026: thêm trường School Level (mầm non, tiểu học, ...).
 */
export default function RegisterSchoolPage() {
  return (
    <RegisterForm
      title="Đăng ký Trường học"
      ucLabel="UC-070"
      description="Hồ sơ sẽ ở trạng thái Pending chờ System Admin phê duyệt (MSG-032)."
      fields={[
        { id: "schoolName", label: "Tên trường (School Name)", placeholder: "VD: Trường Tiểu học Đoàn Thị Điểm" },
        { id: "address", label: "Địa chỉ (Address)", placeholder: "Số nhà, phường/xã, quận/huyện, tỉnh/thành" },
        {
          id: "schoolLevel",
          label: "Cấp trường (School Level)",
          kind: "select",
          options: ["Mầm non", "Tiểu học", "Trung học cơ sở", "Trung học phổ thông", "Liên cấp"],
        },
        { id: "representative", label: "Người đại diện (Representative Name)", placeholder: "Họ tên hiệu trưởng/người đại diện" },
        { id: "email", label: "Email", kind: "email", placeholder: "ten@truong.edu.vn" },
        { id: "phone", label: "Số điện thoại (Phone)", kind: "phone", placeholder: "+84xxxxxxxxx hoặc 0xxxxxxxxx" },
      ]}
      docs={[
        { id: "license", label: "Giấy phép hoạt động & tài liệu đăng ký (UC-070 bước 2)", required: true },
      ]}
      successMessage="School registered successfully. Pending System Admin approval. (MSG-032)"
    />
  );
}
