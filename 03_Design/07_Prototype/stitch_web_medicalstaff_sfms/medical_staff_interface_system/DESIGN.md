---
name: Medical Staff Interface System
colors:
  surface: '#f8f9ff'
  surface-dim: '#d0dbed'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e6eeff'
  surface-container-high: '#dee9fc'
  surface-container-highest: '#d9e3f6'
  on-surface: '#121c2a'
  on-surface-variant: '#3f484c'
  inverse-surface: '#27313f'
  inverse-on-surface: '#eaf1ff'
  outline: '#6f787d'
  outline-variant: '#bec8cd'
  surface-tint: '#006781'
  primary: '#005a71'
  on-primary: '#ffffff'
  primary-container: '#0e7490'
  on-primary-container: '#d3f1ff'
  inverse-primary: '#81d1f0'
  secondary: '#505f76'
  on-secondary: '#ffffff'
  secondary-container: '#d0e1fb'
  on-secondary-container: '#54647a'
  tertiary: '#505355'
  on-tertiary: '#ffffff'
  tertiary-container: '#686b6d'
  on-tertiary-container: '#eaecee'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#b9eaff'
  primary-fixed-dim: '#81d1f0'
  on-primary-fixed: '#001f29'
  on-primary-fixed-variant: '#004d62'
  secondary-fixed: '#d3e4fe'
  secondary-fixed-dim: '#b7c8e1'
  on-secondary-fixed: '#0b1c30'
  on-secondary-fixed-variant: '#38485d'
  tertiary-fixed: '#e0e3e5'
  tertiary-fixed-dim: '#c4c7c9'
  on-tertiary-fixed: '#191c1e'
  on-tertiary-fixed-variant: '#444749'
  background: '#f8f9ff'
  on-background: '#121c2a'
  surface-variant: '#d9e3f6'
typography:
  h1:
    fontFamily: Be Vietnam Pro
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.02em
  h2:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  h3:
    fontFamily: Be Vietnam Pro
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
  label-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  sidebar-width: 280px
  topbar-height: 64px
  container-padding: 24px
  gutter: 16px
  section-gap: 32px
---

## Thương hiệu & Phong cách
Hệ thống thiết kế này tập trung vào trải nghiệm của nhân viên y tế, hướng tới sự tin cậy, hiệu quả và chính xác. Phong cách chủ đạo kết hợp giữa sự tối giản của **Minimalism** và cấu trúc hệ thống của **Corporate / Modern**, dựa trên nền tảng shadcn/ui và Tailwind CSS.

Mục tiêu là giảm thiểu sự mệt mỏi về thị giác (visual fatigue) cho người dùng phải làm việc với dữ liệu y tế cường độ cao. Giao diện sử dụng không gian trắng rộng rãi, phân cấp thông tin rõ ràng và các hiệu ứng tương tác mượt mà để tạo ra một môi trường làm việc chuyên nghiệp, điềm tĩnh.

## Màu sắc
Bảng màu được xây dựng dựa trên mã màu chủ đạo Cyan-700 (#0E7490), mang lại cảm giác sạch sẽ và y tế.

- **Primary (Chủ đạo):** Cyan-700 được dùng cho các hành động chính, trạng thái active và nhận diện thương hiệu.
- **Secondary (Phụ):** Slate-500 được dùng cho các thành phần bổ trợ và icon không ưu tiên.
- **Background (Nền):** Sử dụng Slate-50 làm nền tảng hệ thống để tách biệt các lớp thẻ (Card).
- **Status (Trạng thái):** Các màu Semantic được tinh chỉnh để đảm bảo độ tương phản cao trên nền trắng, giúp nhân viên y tế nhận diện nhanh các cảnh báo hoặc tình trạng bệnh nhân.

## Hệ thống chữ (Typography)
Sử dụng **Be Vietnam Pro** làm phông chữ duy nhất để đảm bảo tính đồng nhất và hỗ trợ tiếng Việt hoàn hảo.

- **Tiêu đề (Headlines):** Sử dụng trọng lượng SemiBold (600) hoặc Bold (700) với khoảng cách chữ hẹp để tạo sự vững chãi.
- **Nội dung (Body):** Ưu tiên kích thước 14px cho dữ liệu bảng và 16px cho văn bản dài để tối ưu khả năng đọc.
- **Nhãn (Labels):** Sử dụng Medium (500) để phân biệt rõ ràng giữa nhãn trường dữ liệu và nội dung nhập liệu.

## Bố cục & Khoảng cách
Hệ thống sử dụng mô hình **Fluid Grid** kết hợp với các khu vực cố định (Sidebar/Topbar).

- **Sidebar:** Cố định bên trái, độ rộng 280px để chứa đủ các danh mục quản lý y tế phức tạp.
- **Topbar:** Cố định phía trên, cao 64px, chứa breadcrumbs và hồ sơ người dùng.
- **Cấu trúc lưới:** 12 cột cho Desktop, 1 cột cho Mobile. Khoảng cách giữa các phần tử (Gutter) mặc định là 16px.
- **Padding:** Các Card nội dung sử dụng padding đồng nhất 24px để tạo không gian thở cho dữ liệu.

## Độ nổi & Chiều sâu (Elevation)
Theo phong cách shadcn/ui, hệ thống sử dụng **Low-contrast outlines** kết hợp với bóng đổ cực nhẹ để tạo chiều sâu mà không làm nặng giao diện.

- **Cấp 0 (Nền):** Màu trắng hoặc Slate-50.
- **Cấp 1 (Card/Table):** Viền Slate-200 (1px) và bóng đổ `shadow-sm`.
- **Cấp 2 (Popovers/Dropdowns):** Viền Slate-200 và bóng đổ `shadow-md`.
- **Cấp 3 (Side Sheets/Modals):** Bóng đổ `shadow-lg` để tập trung sự chú ý tuyệt đối.

## Hình khối
Toàn bộ hệ thống áp dụng bo góc **8px (Rounded-lg)** theo yêu cầu thiết kế. Điều này áp dụng cho Buttons, Cards, Input Fields, và Side Sheets. Các thành phần nhỏ hơn như Checkbox hoặc Badge sử dụng bo góc 4px hoặc 6px để duy trì sự cân đối thị giác.

## Thành phần (Components)

### 1. Sidebar & Topbar
- **Sidebar:** Nền trắng, viền phải `border-r`. Các mục menu có trạng thái `hover:bg-slate-100` và `active:text-cyan-700`. Icon kích thước 20px.
- **Topbar:** Hiệu ứng kính mờ nhẹ (`backdrop-blur`) với độ đục cao để đảm bảo nội dung bên dưới không gây nhiễu khi cuộn.

### 2. Table (Bảng dữ liệu y tế)
- Dòng tiêu đề có nền `bg-slate-50/50`.
- Các dòng dữ liệu có hiệu ứng `hover:bg-slate-50` để dễ theo dõi dòng.
- Font chữ trong bảng sử dụng `body-sm` (14px).

### 3. Badge (Nhãn trạng thái)
- **Success:** Nền xanh lá nhạt, chữ xanh lá đậm (vd: Hoàn thành).
- **Danger:** Nền đỏ nhạt, chữ đỏ đậm (vd: Khẩn cấp).
- **Warning:** Nền vàng nhạt, chữ nâu đậm (vd: Đang chờ).

### 4. Side Sheet (Ngăn kéo bên)
- Sử dụng để xem nhanh hồ sơ bệnh nhân hoặc chi tiết bệnh án mà không rời khỏi trang danh sách.
- Bo góc trái 8px, overlay tối màu nhẹ (`bg-black/20`).

### 5. Stepper (Tiến trình điều trị/nhập liệu)
- Đường nối giữa các bước sử dụng màu `slate-200`.
- Bước hoàn thành: Màu `cyan-700` kèm icon check.
- Bước hiện tại: Viền `cyan-700` với số thứ tự đậm.

### 6. Card & Input
- **Card:** Viền mỏng, không shadow quá đậm. Tiêu đề Card luôn dùng `h3`.
- **Input:** Khi focus sử dụng `ring-2 ring-cyan-700/20` và `border-cyan-700`.