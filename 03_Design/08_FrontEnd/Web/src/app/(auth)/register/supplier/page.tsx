import { RegisterForm } from "@/components/app/register-form";

/**
 * Register Supplier — UC-071 (13_Guest_Common_Screen_Flow.md mục 3, Report 3 §3.3.2.1.3).
 * Field: Company Name, Business License No, Address, Contact Person, Email, Phone
 * + ảnh giấy phép kinh doanh + chứng nhận ATTP (BR-046/BR-047). Thành công → MSG-028.
 * Review 07/2026: thêm Address, thêm upload ảnh GPKD; chứng nhận là BẮT BUỘC.
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
        { id: "businessLicense", label: "Ảnh giấy phép kinh doanh", required: true },
        { id: "foodSafetyCert", label: "Chứng nhận an toàn thực phẩm còn hiệu lực (BR-047)", required: true },
      ]}
      successMessage="Đã tạo tài khoản nhà cung cấp ở trạng thái chờ xác minh."
    />
  );
}
