# Mobile App — Task Scope & Tiến độ

Phạm vi rút gọn: chỉ 4 role có giá trị lớn khi dùng trên điện thoại (thao tác tại
hiện trường: lớp học / bếp / khu kiểm tra), mỗi role chỉ giữ các màn hình
dùng **hàng ngày**. Không làm mobile cho School Admin, System Admin, Supplier
(nhóm này làm việc bàn giấy, ưu tiên thấp — xem thảo luận trong hội thoại).
Không có màn Đăng ký — tài khoản do Web tạo sẵn, mobile chỉ cần Login/Quên mật khẩu.

Quy ước đánh dấu: `[ ]` chưa làm · `[~]` đang làm · `[x]` xong.

## 0. Auth & Common (nền tảng, dùng chung mọi role)

- [x] Login (email/password + Google id_token — backend đã thêm `POST /auth/google`)
- [x] Quên mật khẩu (Forgot Password) — gọi thật `POST /auth/forgot-password`
- [x] Đặt lại mật khẩu (Reset Password) — gọi thật `POST /auth/reset-password` +
      `GET /public/password-policy`. Bỏ màn "Xác thực OTP": backend UC-032 dùng
      token qua link email (BR-003), không có bước OTP 6 số — màn OTP bên web
      (`(auth)/verify-otp`) là mock chưa nối API thật, không có nghiệp vụ thật để
      lấy khuôn theo hướng dẫn "bám API thật". Token dán tay vì mobile chưa có
      deep link mở app từ email.
- [x] Hồ sơ cá nhân (Profile) + Đổi mật khẩu — `GET/PATCH /profile/me`,
      `POST /profile/me/photo`, `PATCH /account/password`. Lối vào: icon ở
      AppBar (`ProfileAvatarButton`) đã gắn vào cả 4 dashboard role. Không port
      "Cài đặt thông báo" trong `user-menu.tsx` (bản web) — mục đó chỉ `toast`
      giả, chưa có API thật.
- [x] Danh sách thông báo + Chi tiết thông báo — `GET /notifications` (phân
      trang kiểu "Tải thêm"), `PATCH /notifications/{id}/read` (⚠️ `id` trong
      DTO thực chất là mã nghiệp vụ `code`, không phải UUID — đối chiếu
      `NotificationServiceImpl.toResponse`/`markRead`). Bản web CHƯA có UI
      nhóm này để lấy khuôn (chỉ có endpoint) — dựng thẳng theo
      `NotificationDtos`. Lối vào: icon chuông cạnh icon hồ sơ ở AppBar cả 4
      dashboard. Không làm "Cài đặt thông báo" (Quiet Hours/toggle loại) —
      backend chưa có endpoint nào cho nhóm này (đã grep xác nhận), khác với
      Notification Settings đã có sẵn field ở UI web (vẫn là mock).

## 1. Giáo viên (Teacher)

- [x] Dashboard
- [x] Nhận suất ăn từ bếp (Class Deliveries)
- [x] Phân phát bữa ăn (Meal Distribution — checklist học sinh, cảnh báo dị ứng)
- [x] Ghi nhận bữa ăn (Meal Records)
- [x] Báo cáo sự cố khẩn cấp tại lớp (Health Incident Report)

## 2. Phụ huynh (Parent)

- [x] Dashboard
- [x] Thực đơn tuần / Bữa ăn hôm nay
- [x] Lịch sử bữa ăn của con
- [x] Khai báo dị ứng
- [x] Báo sự cố ATTP (Safety Concern)

## 3. Cán bộ Y tế (Medical Staff)

- [x] Dashboard
- [x] Kiểm tra an toàn bếp hàng ngày (Conduct Inspection)
- [x] Sổ lưu mẫu thực phẩm (Food Sample Records)
- [x] Sự cố & Điều tra (Incidents)
- [x] Duyệt nhanh khai báo sức khỏe / suất ăn đặc biệt

## 4. Đơn vị Suất ăn (Service Caterers)

- [x] Dashboard
- [x] Kiểm thực ba bước (Three-Step Inspection)
- [x] Lưu mẫu thực phẩm
- [x] Nhận nguyên liệu (Receiving)
- [x] Giao suất tới lớp (Class Deliveries)

---
**Ghi chú:** cập nhật trạng thái `[ ]/[~]/[x]` trực tiếp trong file này mỗi khi
hoàn thành một màn hình; không tạo file theo dõi tiến độ khác.

**Nguồn tham chiếu khi implement:** KHÔNG bám theo các file prototype ở
`03_Design/07_Prototype/Mobile_*.md` (Stitch). Lấy lại nghiệp vụ, field, luồng
API từ chính trang Next.js tương ứng trong `04_Development/Frontend/src/app/(app)/<role>/...`
làm khuôn, rồi bố cục lại cho hợp mobile (1 cột, bottom nav, touch target lớn,
card thay vì bảng) và chăm chút UI đẹp hơn bản web.
