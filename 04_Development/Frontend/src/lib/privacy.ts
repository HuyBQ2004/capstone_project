/**
 * Hằng số dùng chung cho khối bảo vệ dữ liệu cá nhân — trang chính sách (`/privacy`), mẫu văn
 * bản đồng ý (`/privacy/consent`) và hộp thoại chấp nhận điều khoản lần đăng nhập đầu (BR-009).
 *
 * <p><b>Vì sao tách ra đây.</b> Ba nơi trên phải nói CÙNG một phiên bản chính sách: người dùng
 * bấm "Tôi đồng ý" ở hộp thoại thì mốc `terms_accepted_at` lưu trong DB chỉ có ý nghĩa pháp lý
 * khi biết họ đã đồng ý với BẢN NÀO. Để số phiên bản nằm rải rác ở 3 file là cách chắc chắn
 * nhất để chúng lệch nhau sau vài lần sửa nội dung.
 *
 * <p><b>Khi sửa nội dung chính sách</b>: tăng {@link PRIVACY_POLICY_VERSION} và cập nhật
 * {@link PRIVACY_POLICY_EFFECTIVE_DATE}. Theo Điều 19 của chính sách, bản cập nhật phải được
 * thông báo tới chủ thể dữ liệu trước khi áp dụng.
 */

/** Căn cứ pháp lý — trích dẫn nguyên văn số hiệu để mọi màn nói giống nhau. */
export const PDPL_LAW = "Luật Bảo vệ dữ liệu cá nhân số 91/2025/QH15 ngày 26/6/2025";
export const PDPL_DECREE = "Nghị định 356/2025/NĐ-CP ngày 31/12/2025";

export const PRIVACY_POLICY_VERSION = "1.0";
export const PRIVACY_POLICY_EFFECTIVE_DATE = "14/08/2026";

/**
 * Đầu mối tiếp nhận yêu cầu của chủ thể dữ liệu (Điều 13 — DPO). Dùng chính miền của hệ thống;
 * trường triển khai thật cần trỏ hộp thư này về cán bộ bảo vệ dữ liệu do mình bổ nhiệm.
 */
export const DPO_EMAIL = "dpo@sfsm.io.vn";

/**
 * Tóm tắt hiển thị trong hộp thoại đồng ý — người dùng phải nắm được điều gì trước khi bấm
 * "Tôi đồng ý", mà không phải đọc hết 20 điều. Mỗi gạch đầu dòng ứng với một điều trong chính
 * sách; giữ ngắn và nói đúng thứ hệ thống thực sự làm, không hứa chung chung.
 */
export const CONSENT_SUMMARY: { label: string; detail: string }[] = [
  {
    label: "Dữ liệu được thu thập",
    detail:
      "Thông tin định danh, liên lạc và tài khoản của bạn; với người học còn có dị ứng thực phẩm, bệnh lý mạn tính và chỉ số sức khỏe do gia đình khai báo (Điều 3).",
  },
  {
    label: "Mục đích sử dụng",
    detail:
      "Bảo đảm an toàn thực phẩm bữa ăn học đường: lập thực đơn phù hợp thể trạng, chuẩn bị suất ăn riêng, kiểm thực ba bước, lưu mẫu, truy vết khi có sự cố (Điều 5).",
  },
  {
    label: "Chia sẻ dữ liệu",
    detail:
      "Chỉ chia sẻ ở mức tối thiểu cần thiết với đơn vị suất ăn, nhà cung cấp và cơ quan quản lý nhà nước có thẩm quyền. Hệ thống không bán dữ liệu cá nhân (Điều 6).",
  },
  {
    label: "Quyền của bạn",
    detail:
      "Được biết, xem, chỉnh sửa, yêu cầu xóa, rút lại sự đồng ý, hạn chế hoặc phản đối việc xử lý dữ liệu bất cứ lúc nào (Điều 7), theo thời hạn tại Điều 9.",
  },
];
