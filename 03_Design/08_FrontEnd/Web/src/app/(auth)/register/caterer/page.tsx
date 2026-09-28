import { RegisterForm } from "@/components/app/register-form";

/**
 * Register Service Caterers — 13_Guest_Common_Screen_Flow.md mục 4 (Report 3 §3.3.2.1.4).
 * Field: Caterer Name, Business Reg No, Office Address, Contact Person, Email, Phone.
 * Thành công → MSG-028; hồ sơ năng lực chờ School Administration duyệt hợp tác (BR-040+).
 * Review 07/2026: thêm upload ảnh GPKD; chứng nhận ATTP là BẮT BUỘC (bỏ "nếu có").
 */
export default function RegisterCatererPage() {
  return (
    <RegisterForm
      title="Đăng ký Đơn vị Suất ăn"
      ucLabel="Service Caterers"
      description="Hồ sơ năng lực sẽ được School Administration xem xét khi thiết lập hợp tác."
      fields={[
        { id: "catererName", label: "Tên đơn vị (Caterer Name)", placeholder: "VD: Cty TNHH Suất ăn An Bình" },
        { id: "businessRegNo", label: "Số đăng ký kinh doanh (Business Reg No)", placeholder: "VD: 0109876543" },
        { id: "officeAddress", label: "Địa chỉ văn phòng (Office Address)", placeholder: "Số nhà, phường/xã, quận/huyện, tỉnh/thành" },
        { id: "contactPerson", label: "Người liên hệ (Contact Person)", placeholder: "Họ tên người phụ trách" },
        { id: "email", label: "Email", kind: "email", placeholder: "lienhe@congty.vn" },
        { id: "phone", label: "Số điện thoại (Phone)", kind: "phone", placeholder: "+84xxxxxxxxx hoặc 0xxxxxxxxx" },
      ]}
      docs={[
        { id: "businessLicense", label: "Ảnh giấy phép kinh doanh", required: true },
        { id: "foodSafetyCert", label: "Giấy chứng nhận đủ điều kiện ATTP", required: true },
      ]}
      successMessage="Đã tạo hồ sơ đơn vị suất ăn ở trạng thái chờ xác minh."
    />
  );
}
