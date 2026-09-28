/**
 * Nhãn tiếng Việt cho mã lỗi API.
 *
 * Quy ước D7 (`07_implement/00_Common_Conventions.md`): backend trả `code` +
 * `message` TIẾNG ANH (nguyên văn `error_message.md`); FE tra bảng dưới đây để
 * hiển thị. Không dùng `Accept-Language` ở MVP.
 *
 * `code` là mã module (`SCH-010`, `SMP-105`…), không phải mã MSG — xem cột
 * "Mã TDS" ở `error_message.md` §1.1 để đối chiếu.
 */

import { ApiError } from "./api/client";

/** Mã module → nhãn tiếng Việt. Chưa có trong bảng thì dùng `message` của server. */
const ERROR_LABELS: Record<string, string> = {
  // MF01 — User Management (MSG-002/003/005/006/076/104/105/106)
  "IAM-002": "Email hoặc mật khẩu không đúng.",
  "IAM-003": "Tài khoản đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.",
  "IAM-004": "Bạn không có quyền thực hiện thao tác này.",
  "IAM-005": "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.",
  "IAM-006": "Email này đã được sử dụng.",
  "IAM-006policy":
    "Mật khẩu chưa đạt yêu cầu bảo mật: cần chữ hoa, chữ thường, số và ký tự đặc biệt.",
  "IAM-006reuse": "Không được dùng lại 3 mật khẩu gần nhất.",
  "IAM-007": "Tài khoản tạm khóa do đăng nhập sai quá nhiều lần.",
  "IAM-008": "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.",
  "IAM-009": "Không thể xóa vai trò: vẫn còn người dùng được gán. Cần chuyển họ sang vai trò khác trước.",
  "IAM-102": "Tài khoản Google chưa được liên kết. Vui lòng liên hệ quản trị viên.",
  "IAM-103": "Đăng nhập Google thất bại. Vui lòng thử lại.",

  // MF02 — School & Class (MSG-010/011/080/081/107/108/109)
  "SCH-010": "Lớp đã đạt sức chứa tối đa 50 học sinh.",
  "SCH-011": "Giáo viên này đã chủ nhiệm một lớp khác.",
  "SCH-013": "Tọa độ GPS không hợp lệ hoặc đang bị khóa chỉnh sửa.",
  "SCH-024": "Khối lớp phải nằm trong khoảng 1–12.",
  "SCH-080": "Trường đã có năm học đang hoạt động. Cần đóng năm học đó trước.",
  "SCH-081": "Chưa có năm học nào đang hoạt động. Vui lòng kích hoạt năm học trước.",

  // MF03 — Student & Parent (MSG-110…116)
  "STU-010": "Lớp đích đã đủ 50 học sinh.",
  "STU-012": "Mã học sinh không hợp lệ hoặc đã được sử dụng.",
  "STU-030": "Mỗi học sinh phải liên kết với ít nhất một tài khoản phụ huynh.",
  "STU-031": "Khai báo dị ứng mức Nặng bắt buộc phải có tài liệu y tế kèm theo.",
  "STU-032": "Vui lòng nhập lý do.",
  "STU-066": "Chế độ ăn không nằm trong danh mục chuẩn.",
  "STU-094": "Trường dữ liệu này không sửa được qua chức năng hiện tại.",
  "STU-172": "Lịch sử sức khỏe chỉ ghi thêm, không sửa hoặc xóa được.",
  "STU-193": "Lớp thuộc năm học đã đóng, chỉ đọc.",
  "STU-201": "Tài khoản phụ huynh đã liên kết đủ 5 học sinh. Cần BGH xác minh.",

  // MF04 — Supplier & Partner (MSG-113/117/118)
  "SUP-013": "Chứng nhận an toàn thực phẩm của nhà cung cấp đã hết hạn.",
  "SUP-014": "Xác thực khuôn mặt thất bại.",
  "SUP-048": "Vui lòng nhập lý do.",
  "SUP-050": "Số CCCD này đã đăng ký cho nhân viên giao hàng khác.",
  "SUP-409": "Đã tồn tại đơn đang xử lý, hoặc tài liệu này đã được nộp.",

  // MF08 — Food Sample Retention (MSG-119…128)
  "SMP-019": "Khối lượng mẫu lưu chưa đạt tối thiểu (100g đặc / 150ml lỏng).",
  "SMP-023": "Mã sự cố không tồn tại hoặc sự cố đã đóng.",
  "SMP-030": "Thiếu người hủy, nhân chứng hoặc ghi chú chất lượng.",
  "SMP-105": "Chỉ được lưu mẫu sau khi lô bữa ăn qua đủ cả 3 bước kiểm thực.",
  "SMP-107": "Bắt buộc có ảnh bằng chứng khi lưu mẫu.",
  "SMP-109": "Vị trí lưu mẫu nằm ngoài khu vực bếp/kho được chỉ định.",
  "SMP-111": "Chưa hết thời hạn lưu tối thiểu 24 giờ.",
  "SMP-112": "Mẫu đang bị khóa vì liên kết với một sự cố chưa đóng.",
  "SMP-113": "Mẫu không còn ở trạng thái Đang lưu, không thể bàn giao lần hai.",
  "SMP-203": "Dụng cụ lưu mẫu phải kín, đã tiệt trùng và niêm phong riêng từng món.",
  "SMP-212": "Thiếu chỉ tiêu kiểm nghiệm hoặc biên bản giao nhận mẫu.",

  // MF09 — Meal Serving (MSG-129…133)
  "SRV-021": "Học sinh này đã được xác nhận phát suất trong phiên hiện tại.",
  "SRV-022": "Chưa có thực đơn đã duyệt cho buổi ăn này.",
  "SRV-023": "Còn sự cố phát suất chưa xử lý, không thể hoàn tất.",
  "SRV-024": "Không thể thu hồi xác nhận của ngày trước.",
  "SRV-025": "Ghi nhận tiêu thụ đã khóa sau ngày phục vụ.",
  "SRV-094": "Không được phục vụ bữa ăn: có bước kiểm thực không đạt.",

  // MF10 — Feedback & Announcement (MSG-134…136)
  "FBA-022": "Đã quá thời hạn 24 giờ để gửi phản hồi cho bữa ăn này.",
  "FBA-140": "Đánh giá dưới 3 sao cần chọn ít nhất một nhãn vấn đề chất lượng.",
  "FBA-DUP": "Bạn đã gửi phản hồi cho bữa ăn này rồi.",
  "FBA-STATE": "Thông báo đã công bố không thể chuyển về bản nháp.",

  // Dùng chung mọi module (MSG-137/138)
  "SCH-404": "Không tìm thấy dữ liệu.",
  "STU-404": "Không tìm thấy dữ liệu.",
  "SUP-404": "Không tìm thấy dữ liệu.",
  "SMP-404": "Không tìm thấy dữ liệu.",
  "SRV-404": "Không tìm thấy dữ liệu.",
  "FBA-404": "Không tìm thấy dữ liệu.",
  "SRV-001": "Dữ liệu nhập chưa hợp lệ.",
  "FBA-REQ": "Vui lòng điền đầy đủ các trường bắt buộc.",
};

/**
 * Lấy thông điệp hiển thị cho một lỗi bất kỳ ném ra từ mutation/query.
 *
 * Thứ tự: nhãn tiếng Việt theo `code` → `message` của server (tiếng Anh) →
 * chuỗi dự phòng do màn hình truyền vào.
 */
export function apiErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof ApiError) {
    return (error.code ? ERROR_LABELS[error.code] : undefined) ?? error.message ?? fallback;
  }
  if (error instanceof Error && error.message) return error.message;
  return fallback;
}
