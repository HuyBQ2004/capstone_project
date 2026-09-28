---
name: SFMS Admin
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
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  display:
    fontFamily: Be Vietnam Pro
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 30px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  title-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  title-md:
    fontFamily: Be Vietnam Pro
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
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
  headline-lg-mobile:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  container-margin: 2rem
  gutter: 1rem
  section-gap: 1.5rem
  component-padding-x: 1rem
  component-padding-y: 0.5rem
---

## Brand & Style
Hệ thống thiết kế này được xây dựng trên triết lý "Chức năng là ưu tiên hàng đầu" (Functional-first), lấy cảm hứng từ phong cách "New York" của shadcn/ui. Mục tiêu là tạo ra một môi trường quản trị chuyên nghiệp, tinh gọn và cực kỳ hiệu quả cho người dùng hệ thống sfms.

Phong cách chủ đạo là **Minimalism (Tối giản)** kết hợp với **High-Contrast (Độ tương phản cao)**. Giao diện sử dụng không gian trắng rộng rãi, các đường kẻ mảnh (hairline borders) và sự phân cấp rõ ràng để giảm bớt sự mệt mỏi về thị giác khi xử lý dữ liệu phức tạp. Cảm giác tổng thể mang lại sự tin cậy, chính xác và tốc độ.

## Colors
Bảng màu tập trung vào sự rõ ràng và phân cấp thông tin. Màu chủ đạo (Teal/Cyan) được sử dụng một cách có chừng mực cho các hành động chính và trạng thái tích cực.

- **Primary (#0E7490):** Sử dụng cho nút bấm chính (Primary Button), trạng thái đang chọn (Active states) và các điểm nhấn nhận diện thương hiệu.
- **Surface & Background:** Sử dụng màu trắng tinh khiết cho nền trang và các lớp container để tối đa hóa độ tương phản với văn bản.
- **Accents:** Các màu thông báo (Success, Warning, Error) được giữ ở độ bão hòa vừa phải để không gây xao nhãng nhưng vẫn đảm bảo tính cảnh báo tức thì.
- **Borders:** Sử dụng màu xám nhạt (`#E2E8F0`) cho các đường kẻ phân cách, tạo sự ngăn nắp mà không gây đứt gãy luồng đọc của mắt.

## Typography
Sử dụng duy nhất bộ font **Be Vietnam Pro** để đảm bảo tính nhất quán và khả năng hiển thị tiếng Việt hoàn hảo. 

- **Tiêu đề (Headlines):** Sử dụng trọng lượng SemiBold hoặc Bold với khoảng cách chữ hơi thu hẹp (`-0.01em` đến `-0.02em`) để tạo cảm giác hiện đại và chặt chẽ.
- **Nội dung (Body):** Ưu tiên kích thước 14px cho các bảng dữ liệu và form để tối ưu diện tích hiển thị thông tin.
- **Nhãn (Labels):** Sử dụng Medium weight (500) để phân biệt rõ ràng giữa nhãn trường dữ liệu và nội dung nhập liệu.

## Layout & Spacing
Hệ thống sử dụng mô hình **Fluid Grid** kết hợp với các khoảng cách cố định (Fixed spacing) để duy trì sự cân đối.

- **Cấu trúc trang:** Sử dụng Sidebar cố định bên trái (240px - 280px) và khu vực nội dung chính co giãn.
- **Data Grid:** Sử dụng khoảng cách 16px (1rem) giữa các cột trong bảng dữ liệu để đảm bảo tính dễ đọc.
- **Padding:** Các thẻ (Cards) và Modals sử dụng padding đồng nhất là 24px (1.5rem).
- **Phân cấp:** Sử dụng khoảng cách lớn hơn giữa các khối chức năng khác nhau (section-gap) để tạo sự phân tách logic mà không cần dùng quá nhiều đường kẻ.

## Elevation & Depth
Tuân thủ phong cách phẳng của "New York" shadcn, hệ thống này hạn chế tối đa việc sử dụng bóng đổ (shadows).

- **Tầng 0 (Background):** Màu nền xám cực nhẹ (`#F8FAFC`) để làm nổi bật các khối nội dung.
- **Tầng 1 (Surface):** Các thẻ (Cards), Bảng (Tables) sử dụng nền trắng tuyệt đối với đường viền 1px màu `#E2E8F0`. Không sử dụng bóng đổ ở tầng này.
- **Tầng 2 (Popovers/Modals):** Sử dụng bóng đổ cực nhẹ, mờ và tán rộng (Ambient shadow) để tạo cảm giác nổi lên trên bề mặt, kết hợp với đường viền để giữ độ sắc nét.
- **Trạng thái Hover:** Thay đổi màu nền nhẹ (Light Gray) thay vì thay đổi độ cao bóng đổ.

## Shapes
Hệ thống sử dụng bo góc đồng nhất là **8px** cho hầu hết các thành phần giao diện.

- **Nút bấm & Input:** Bo góc 8px tạo cảm giác chuyên nghiệp, không quá cứng nhắc như góc vuông nhưng không quá mềm mỏng như các ứng dụng giải trí.
- **Cards & Modals:** Áp dụng `rounded-lg` (16px) cho các container lớn để tạo sự bao bọc và phân biệt rõ với các phần tử nhỏ bên trong.
- **Status Badges:** Sử dụng bo góc hoàn toàn (Pill-shaped) để dễ dàng phân biệt giữa nhãn trạng thái và các nút hành động.

## Components
Các thành phần được thiết kế để tối ưu hóa việc quản lý dữ liệu:

- **Data Tables:** Sử dụng hàng có chiều cao cố định, hỗ trợ sắp xếp, lọc và phân trang. Đường kẻ ngang mảnh giữa các hàng, không dùng đường kẻ dọc. Hàng tiêu đề có nền xám nhẹ.
- **Status Badges:** 
    - *Thành công:* Nền xanh lá nhạt, chữ xanh lá đậm.
    - *Cảnh báo:* Nền hổ phách nhạt, chữ hổ phách đậm.
    - *Lỗi:* Nền đỏ nhạt, chữ đỏ đậm.
- **Statistic Cards:** Hiển thị con số lớn, in đậm kèm theo biểu đồ mini (sparklines) hoặc chỉ số phần trăm thay đổi (+/-).
- **Forms:** Nhãn nằm trên trường nhập liệu. Trường nhập liệu có viền rõ ràng, khi focus sẽ có vòng highlight màu Primary mờ.
- **Modals:** Căn giữa màn hình, có lớp phủ (overlay) màu đen mờ (opacity 50%). Tiêu đề modal luôn nằm bên trái, nút hành động nằm bên phải phía chân trang.
- **Charts:** Sử dụng bảng màu bổ trợ (Cool tones) như Blue, Indigo, Violet để đảm bảo tính thẩm mỹ chuyên nghiệp cho các biểu đồ đường và cột.