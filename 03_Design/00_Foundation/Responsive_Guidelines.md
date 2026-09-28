# Responsive Guidelines — SFMS

**Trạng thái**: Confirmed (2026-07-01)
**Nguồn breakpoint gốc**: `Report 3.md` mục Non-Functional Requirements → Usability (dòng ~6207–6209): *Desktop ≥1280px, Mobile/Tablet ≥360px.*

---

## 1. Breakpoint chính thức

| Tên | Chiều rộng | Dùng cho |
|---|---|---|
| `xs` | ≥360px | Mobile app Flutter (không phải Web responsive — Flutter build native, không "co giãn" theo nghĩa web) |
| `sm` | ≥640px | Web — điện thoại lớn/phablet xem Web (trường hợp phụ huynh mở link Web trên điện thoại) |
| `md` | ≥768px | Web — tablet, Sidebar thu gọn icon-only (xem `Layout_Guidelines.md` mục 1.1) |
| `lg` | ≥1024px | Web — tablet ngang / laptop nhỏ |
| `xl` | ≥1280px | Web — Desktop chính thức (ngưỡng tối thiểu theo NFR Report 3) |
| `2xl` | ≥1536px | Web — màn hình lớn, dùng để tăng số cột grid dashboard (không tăng font size) |

> Map trực tiếp vào Tailwind CSS breakpoint mặc định (`sm/md/lg/xl/2xl`) — không định nghĩa breakpoint tuỳ chỉnh khác để tránh lệch với component shadcn có sẵn.

## 2. Quy tắc responsive cho Web

- **Dưới `md` (768px)**: Sidebar ẩn thành Drawer, bảng dữ liệu chuyển từ dạng `<table>` sang danh sách `Card` xếp dọc (mỗi row → 1 card, tránh cuộn ngang).
- **`md`–`lg`**: Sidebar thu gọn icon-only (không chữ), Content Area vẫn giữ layout dạng bảng nếu ≤6 cột.
- **≥`xl`**: layout đầy đủ như thiết kế gốc (Sidebar mở rộng có chữ, bảng đầy đủ cột).
- **Không hỗ trợ chính thức dưới 360px bề rộng** — dưới ngưỡng này khuyến nghị dùng app Mobile thay vì Web (thông báo gợi ý nếu phát hiện viewport quá nhỏ và user-agent là mobile).
- **Trang Guest/Common (Login, OTP)**: layout căn giữa cố định max-width 420px ở mọi breakpoint — không cần rule responsive riêng vì đã tối giản sẵn.

## 3. Quy tắc responsive cho Mobile (Flutter)

Flutter không "responsive" theo nghĩa web (không co giãn breakpoint liên tục) mà dùng **adaptive layout theo loại thiết bị**:

| Loại thiết bị | Chiều rộng tham chiếu | Xử lý |
|---|---|---|
| Phone (chính thức hỗ trợ) | 360–430px logical | Layout 1 cột chuẩn theo `Layout_Guidelines.md` mục 2 |
| Tablet (hỗ trợ có giới hạn — xem TODO) | ≥600px logical | Cân nhắc layout 2 cột (list + detail side-by-side) — **chưa quyết định chính thức, xem TODO #9 Device Support Matrix trong `00_UI_Design_Documentation_Plan.md`** |
| Foldable | Biến động | Ngoài phạm vi MVP, không thiết kế riêng |

- Dùng `LayoutBuilder`/`MediaQuery.of(context).size.width` với 1 ngưỡng duy nhất (600px) để chuyển 1 cột ↔ 2 cột, tránh nhiều ngưỡng gây phức tạp code không cần thiết cho team sinh viên.
- Text scale: tôn trọng `MediaQuery.textScaleFactor` của hệ điều hành (accessibility — người dùng lớn tuổi/phụ huynh có thể tăng cỡ chữ hệ thống) — không khoá cứng font size tuyệt đối.

## 4. Ảnh & Media responsive

- Ảnh bằng chứng/upload luôn dùng tỷ lệ khung hình cố định (`aspect-ratio: 4/3` hoặc `1/1`) ở cả 2 nền tảng, tránh layout shift khi ảnh tải xong.
- Web dùng `next/image` với `sizes` khai báo theo breakpoint để tối ưu tải ảnh trên dashboard nhiều ảnh (VD danh sách bằng chứng kiểm tra).

## 5. TODO còn mở
- Xác nhận có cần thiết kế layout 2 cột riêng cho Mobile Tablet hay không (phụ thuộc Device Support Matrix, TODO #9 trong plan chính) — hiện tại mặc định 1 cột an toàn cho mọi kích thước ≥360px.
- Làm rõ liệu NFR "Mobile/Tablet ≥360px" trong Report 3 đang mô tả Web co giãn xuống 360px hay đang mô tả app Flutter (đã nêu ở `00_UI_Design_Documentation_Plan.md` mục 5) — cách hiểu ở tài liệu này: áp dụng ngưỡng 360px cho **cả 2** (Web tối thiểu kỹ thuật, Mobile là baseline thiết bị).
