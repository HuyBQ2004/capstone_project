---
name: Quản trị Hệ thống sfms
colors:
  surface: '#f7fafc'
  surface-dim: '#d7dadd'
  surface-bright: '#f7fafc'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f1f4f6'
  surface-container: '#ebeef0'
  surface-container-high: '#e6e8eb'
  surface-container-highest: '#e0e3e5'
  on-surface: '#181c1e'
  on-surface-variant: '#3f484c'
  inverse-surface: '#2d3133'
  inverse-on-surface: '#eef1f3'
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
  tertiary: '#794602'
  on-tertiary: '#ffffff'
  tertiary-container: '#965e1c'
  on-tertiary-container: '#ffe8d6'
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
  tertiary-fixed: '#ffdcbd'
  tertiary-fixed-dim: '#ffb86f'
  on-tertiary-fixed: '#2c1600'
  on-tertiary-fixed-variant: '#693c00'
  background: '#f7fafc'
  on-background: '#181c1e'
  surface-variant: '#e0e3e5'
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
    letterSpacing: -0.02em
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
  body-md:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
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
  base: 4px
  container-padding: 24px
  element-gap: 16px
  grid-gutter: 20px
  sidebar-width: 260px
---

## Brand & Style
Thiết kế hướng tới sự chuyên nghiệp, tin cậy và hiệu suất cao cho các quản trị viên hệ thống. Phong cách chủ đạo là **Modern Minimalism**, lấy cảm hứng từ chủ đề "New York" của shadcn/ui. 

Hệ thống tập trung vào tính thực dụng với mật độ thông tin cao (high-density), giúp người dùng bao quát dữ liệu nhanh chóng. Giao diện sử dụng các đường kẻ mảnh, bảng màu trung tính và khoảng trắng có tính toán để giảm bớt sự căng thẳng khi làm việc với các thông số kỹ thuật phức tạp. Trải nghiệm người dùng phải mang lại cảm giác sạch sẽ, chính xác và hiện đại.

## Colors
Bảng màu được tối ưu hóa cho môi trường làm việc tập trung cao độ. 
- **Primary (#0E7490):** Một sắc xanh Teal đậm đà, dùng cho các hành động chính và trạng thái tích cực.
- **Background (#F8FAFC):** Màu xám xanh rất nhạt, tạo độ tương phản nhẹ nhàng với các thẻ (cards) màu trắng.
- **Surface (#FFFFFF):** Sử dụng cho các container nội dung, bảng dữ liệu và form.
- **Text (#0F172A):** Màu xanh đen đậm cho độ đọc (readability) tối ưu.
- **Status Colors:** Sử dụng các sắc độ tiêu chuẩn cho Error (Red), Warning (Amber), và Success (Emerald) nhưng được điều chỉnh để hài hòa với tone màu Primary.

## Typography
Sử dụng **Be Vietnam Pro** làm font chữ duy nhất để đảm bảo tính đồng nhất và hỗ trợ tiếng Việt hoàn hảo. 
- **Cấu trúc:** Ưu tiên sử dụng kích thước `14px (body-md)` làm tiêu chuẩn cho các bảng dữ liệu và form nhập liệu để tăng mật độ thông tin.
- **Tiêu đề:** Sử dụng `fontWeight: 600` hoặc `700` với `letterSpacing` âm nhẹ để tạo cảm giác hiện đại, chặt chẽ.
- **Nhãn (Labels):** Các nhãn trường dữ liệu nên sử dụng `label-md` để phân biệt rõ với nội dung nhập liệu.

## Layout & Spacing
Hệ thống sử dụng mô hình **Fixed Sidebar + Fluid Content**.
- **Sidebar:** Cố định ở bên trái với chiều rộng 260px, sử dụng màu nền Surface hoặc hơi tối hơn để phân tách khu vực điều hướng.
- **Grid:** Hệ thống 12 cột cho phần nội dung chính. Trên Desktop, margin ngoài cùng là 24px.
- **Density:** Sử dụng hệ thống spacing dựa trên bội số của 4px. Khoảng cách giữa các card là 20px. Nội dung bên trong form sử dụng gap 16px.
- **Responsive:** Trên Tablet, sidebar có thể thu gọn thành biểu tượng (64px). Trên Mobile, sử dụng ngăn kéo (drawer) cho menu điều hướng.

## Elevation & Depth
Để duy trì phong cách sạch sẽ của "New York" theme, độ nổi của các thành phần được tối giản:
- **Borders:** Sử dụng viền mảnh 1px màu `#E2E8F0` cho hầu hết các container và card thay vì đổ bóng đậm.
- **Shadows:** Chỉ sử dụng shadow cực kỳ nhẹ (low-opacity, large blur) cho các thành phần nổi như Dropdown menu, Popover hoặc Modal để tách biệt chúng khỏi bề mặt bên dưới.
- **Tonal Layers:** Sử dụng sự khác biệt giữa `#F8FAFC` (nền) và `#FFFFFF` (thẻ nội dung) để tạo chiều sâu mà không cần hiệu ứng phức tạp.

## Shapes
Hệ thống sử dụng chỉ số **Rounded Eight (8px)** làm tiêu chuẩn. 
- **Buttons & Inputs:** Bo góc 8px (0.5rem).
- **Cards:** Bo góc 8px để tạo sự gọn gàng.
- **Badges/Chips:** Có thể sử dụng bo góc hoàn toàn (pill) hoặc 6px để tạo sự khác biệt về loại dữ liệu.
- **Checkboxes:** Bo góc nhẹ 4px để duy trì tính hình khối.

## Components
- **Buttons:** Sử dụng `primary` cho hành động chính, `outline` cho hành động phụ. Trạng thái `hover` sẽ làm đậm màu nền một chút. 
- **Form Fields:** Border 1px, padding 8px 12px. Khi `focus`, sử dụng vòng ring màu Primary với độ mờ (opacity) thấp.
- **Side Navigation:** Sử dụng các biểu tượng dạng nét mảnh (outline icons). Mục đang chọn (Active) sẽ có nền màu Primary nhạt hoặc một đường chỉ báo (indicator) ở cạnh trái.
- **Tabs:** Phong cách "Segmented Control" với nền xám nhạt và item active có màu trắng kèm shadow nhẹ.
- **Badges:** Sử dụng background nhạt (tint) kết hợp với chữ đậm màu tương ứng để hiển thị trạng thái hệ thống (Ví dụ: Đang chạy, Lỗi, Chờ).
- **Cards:** Viền mờ, không shadow, tiêu đề card phân cách với nội dung bởi một đường kẻ ngang mảnh.