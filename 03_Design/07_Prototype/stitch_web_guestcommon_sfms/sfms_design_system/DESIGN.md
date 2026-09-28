---
name: SFMS Design System
colors:
  surface: '#faf8ff'
  surface-dim: '#d2d9f4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#eaedff'
  surface-container-high: '#e2e7ff'
  surface-container-highest: '#dae2fd'
  on-surface: '#131b2e'
  on-surface-variant: '#3f484c'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
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
  tertiary: '#4e5356'
  on-tertiary: '#ffffff'
  tertiary-container: '#666b6f'
  on-tertiary-container: '#e8edf1'
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
  tertiary-fixed: '#dfe3e7'
  tertiary-fixed-dim: '#c3c7cb'
  on-tertiary-fixed: '#171c1f'
  on-tertiary-fixed-variant: '#43474b'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  h1:
    fontFamily: Be Vietnam Pro
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  h2:
    fontFamily: Be Vietnam Pro
    fontSize: 30px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.01em
  h3:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  h1-mobile:
    fontFamily: Be Vietnam Pro
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 34px
  body-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-base:
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
    fontWeight: '500'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base-unit: 4px
  container-max-width: 1280px
  gutter: 24px
  auth-card-width: 448px
---

## Brand & Style
Hệ thống thiết kế này tập trung vào sự chuyên nghiệp, tin cậy và hiện đại, lấy cảm hứng từ triết lý của **Shadcn/UI** và **Tailwind CSS**. Phong cách chủ đạo là sự tối giản tinh tế (Minimalism) kết hợp với các thành phần giao diện có cấu trúc rõ ràng, khoảng trắng rộng rãi và hệ thống phân cấp thị giác chặt chẽ. 

Đối tượng người dùng hướng tới sự hiệu quả trong công việc, do đó giao diện cần mang lại cảm giác sạch sẽ, dễ điều hướng và phản hồi nhanh chóng. Trải nghiệm người dùng được tối ưu hóa thông qua các hiệu ứng chuyển cảnh nhẹ nhàng và sự nhất quán trong mọi tương tác.

## Colors
Hệ thống sử dụng bảng màu **Light Mode** làm mặc định, tập trung vào độ tương phản cao và tính dễ đọc.

- **Primary (#0E7490):** Màu Cyan-700 đóng vai trò là màu nhận diện chính, được sử dụng cho các hành động quan trọng (CTA), trạng thái hoạt động và các điểm nhấn thương hiệu.
- **Secondary (#64748B):** Màu Slate-500 dành cho các thông tin bổ trợ, icon và trạng thái ít ưu tiên hơn.
- **Background (#FFFFFF):** Nền trắng tinh khiết giúp tối ưu hóa không gian và sự tập trung.
- **Surface/Card (#F8FAFC):** Sử dụng các sắc độ xám cực nhẹ để phân tách các lớp nội dung.
- **Destructive (#E11D48):** Màu đỏ dành cho các hành động nguy hiểm hoặc lỗi.
- **Success (#10B981):** Màu xanh lá cho các thông báo thành công.

## Typography
Sử dụng **Be Vietnam Pro**, một font chữ không chân hiện đại, được tối ưu hóa cực tốt cho tiếng Việt với các dấu thanh được thiết kế tinh tế.

- **Headlines:** Sử dụng độ đậm Bold hoặc SemiBold để tạo sự vững chãi. Khoảng cách chữ (letter-spacing) hơi thu hẹp ở các kích thước lớn để tăng tính thẩm mỹ.
- **Body Text:** Sử dụng Regular để đảm bảo khả năng đọc tốt nhất trên màn hình kỹ thuật số.
- **Labels/UI Elements:** Sử dụng Medium để phân biệt với nội dung văn bản thông thường, giúp người dùng dễ dàng nhận diện các thành phần điều hướng.

## Layout & Spacing
Hệ thống áp dụng hệ lưới 8pt (8-point grid) để đảm bảo sự nhất quán tuyệt đối.

- **Grid System:** Sử dụng hệ thống 12 cột cho Desktop với margin 24px và gutter 24px.
- **Centered Card Layout:** Đặc biệt dành cho các trang xác thực (Login, Register). Nội dung được đặt trong một thẻ (Card) có chiều rộng cố định (max-width: 448px), căn giữa hoàn toàn theo cả hai chiều ngang và dọc của màn hình.
- **Responsive:** 
  - **Mobile (<640px):** Padding 16px, layout 1 cột.
  - **Tablet (640px - 1024px):** Padding 24px, layout linh hoạt.
  - **Desktop (>1024px):** Container tối đa 1280px.

## Elevation & Depth
Hệ thống sử dụng **Soft Shadows** (đổ bóng mềm) để tạo chiều sâu mà không làm nặng nề giao diện.

- **Level 1 (Default):** Đường viền mỏng 1px (#E2E8F0) không shadow cho các thành phần tĩnh.
- **Level 2 (Cards/Dropdowns):** Shadow rất mờ, độ khuếch tán rộng: `0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)`.
- **Level 3 (Modals/Popovers):** Shadow đậm hơn để tách biệt hẳn với lớp nền: `0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1)`.
- **Tonal Layers:** Sử dụng các lớp màu nền khác nhau (Trắng, Slate-50) để phân cấp thông tin thay vì chỉ dựa vào shadow.

## Shapes
Ngôn ngữ hình khối chủ đạo là **Round Eight** (Bo góc 8px), tạo cảm giác hiện đại và thân thiện nhưng vẫn giữ được sự chuyên nghiệp của một công cụ quản lý.

- **Cơ bản (Default):** 8px cho Button, Input, Card nhỏ.
- **Lớn (Large):** 16px cho các Modal hoặc Container lớn.
- **Tròn (Full):** Dành cho Avatar hoặc các Badge trạng thái đặc biệt.

## Components
Các thành phần được xây dựng dựa trên cấu trúc của Tailwind CSS và Radix UI:

- **Buttons:** 
  - **Primary:** Nền Cyan-700, chữ trắng, hiệu ứng hover giảm độ sáng 10%.
  - **Outline:** Viền 1px, màu Primary, hover có nền Primary nhạt.
- **Input Fields:** Chiều cao tiêu chuẩn 40px, bo góc 8px, viền màu Slate-200. Khi focus, viền chuyển sang màu Primary với hiệu ứng ring mờ.
- **Centered Cards (Auth):** Thẻ có nền trắng, bo góc 8px, shadow mức 2. Header của thẻ chứa Logo và tiêu đề căn giữa.
- **Lists:** Sử dụng đường kẻ chia ngăn (dividers) mỏng màu Slate-100, padding dọc 12px cho mỗi item.
- **Checkboxes/Radios:** Sử dụng màu Primary khi được chọn, kích thước chuẩn 16x16px.
- **Chips/Badges:** Bo góc 6px hoặc Full, sử dụng màu nền nhạt (tint) của các trạng thái tương ứng (Success, Warning, Error).