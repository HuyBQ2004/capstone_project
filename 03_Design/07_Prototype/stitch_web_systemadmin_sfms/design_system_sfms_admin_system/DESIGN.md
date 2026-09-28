---
name: SFMS Admin System
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
  h1-mobile:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  container-margin: 24px
  gutter: 16px
  section-gap: 32px
---

## Brand & Style
Hệ thống được thiết kế hướng tới đối tượng Quản trị viên hệ thống (System Admin), nơi sự chính xác và tốc độ xử lý thông tin là ưu tiên hàng đầu. Lấy cảm hứng từ phong cách "New York" của shadcn/ui, thiết kế tập trung vào sự tối giản, cấu trúc lưới chặt chẽ và độ tương phản sắc nét.

Phong cách chủ đạo là **Modern Corporate**:
- **Sạch sẽ:** Sử dụng khoảng trắng thông minh để phân tách các khối dữ liệu phức tạp.
- **Chuyên nghiệp:** Loại bỏ các chi tiết trang trí không cần thiết, tập trung vào chức năng và khả năng đọc.
- **Tính hệ thống:** Mọi thành phần đều tuân thủ các quy tắc nhất quán về khoảng cách và tỷ lệ.

## Colors
Bảng màu được tối ưu hóa cho giao diện quản trị với cường độ làm việc cao:
- **Primary (#0E7490):** Màu Cyan-700 đóng vai trò định hướng, sử dụng cho các hành động chính và trạng thái active.
- **Surface (#F8FAFC):** Màu nền nhẹ nhàng giúp giảm mỏi mắt, tạo lớp nền tách biệt cho các thẻ (cards) nội dung màu trắng.
- **Semantic Palette:**
    - **Hoạt động (Success):** Xanh lá cây, biểu thị trạng thái vận hành ổn định.
    - **Tạm dừng (Error):** Đỏ, cảnh báo các lỗi nghiêm trọng cần xử lý ngay.
    - **Chờ duyệt (Warning):** Vàng/Cam, lưu ý các tác vụ đang treo hoặc cần kiểm tra.
- **Neutral:** Sử dụng dải xám của Slate và Zinc để phân cấp văn bản và đường kẻ border mỏng (1px).

## Typography
Sử dụng duy nhất font **Be Vietnam Pro** để đảm bảo tính đồng nhất và khả năng hiển thị tiếng Việt hoàn hảo.
- **Cấu trúc:** Phân cấp rõ ràng giữa tiêu đề (Bold/SemiBold) và nội dung (Regular). 
- **Chỉ số:** Kích thước cơ bản cho nội dung bảng và form là 14px (body-md) để tối ưu mật độ thông tin.
- **Tính đọc:** Khoảng cách dòng (Line height) được thiết lập rộng rãi để người dùng không bị rối khi đọc các bảng dữ liệu lớn.

## Layout & Spacing
Hệ thống áp dụng mô hình **Fixed-Fluid Hybrid Layout**:
- **Sidebar:** Cố định ở bên trái (256px) trên desktop, có thể thu gọn thành icon.
- **Main Content:** Co giãn linh hoạt theo chiều rộng màn hình.
- **Grid:** Sử dụng hệ thống 12 cột cho các dashboard widgets. 
- **Spacing Rhythm:** Dựa trên hệ số 4px. Khoảng cách mặc định giữa các card là 16px (gutter). Lề ngoài của trang là 24px để tạo cảm giác thoáng đãng.

## Elevation & Depth
Tuân thủ triết lý "Flat Design 2.0" của shadcn/ui:
- **Tonal Layers:** Sử dụng màu sắc để phân cấp thay vì đổ bóng nặng. Background chính là `#F8FAFC`, các Card nội dung là `#FFFFFF`.
- **Outlines:** Sử dụng border mỏng 1px màu `#E2E8F0` cho tất cả các container, input và button để tạo sự sắc nét.
- **Shadows:** Chỉ sử dụng bóng đổ cực nhẹ (Soft Ambient Shadow) cho các phần tử nổi hẳn lên như Dropdown Menu, Modal hoặc Popover để phân biệt với mặt phẳng nền.

## Shapes
Hệ thống sử dụng bo góc đồng nhất là **8px (Round 2)**:
- **Components:** Button, Input, Card, và Badge đều áp dụng chỉ số 8px để tạo cảm giác hiện đại, dễ tiếp cận nhưng vẫn giữ được sự nghiêm túc của một công cụ quản trị.
- **Đặc lệ:** Các ô chọn Checkbox có bo góc nhỏ hơn (4px) để giữ tính biểu tượng truyền thống.

## Components
Thiết kế các thành phần tuân thủ chặt chẽ phong cách New York:

- **Buttons:** 
    - *Primary:* Nền `#0E7490`, chữ trắng. 
    - *Outline:* Border `#E2E8F0`, hover nền nhạt.
- **Badges (Trạng thái):** 
    - *Hoạt động:* Nền xanh lá nhạt (Alpha 10%), border xanh lá, chữ xanh lá đậm.
    - *Tạm dừng:* Nền đỏ nhạt, border đỏ, chữ đỏ đậm.
    - *Chờ duyệt:* Nền vàng nhạt, border vàng, chữ nâu đậm.
- **Tables:** Là thành phần quan trọng nhất. Header có nền nhẹ `#F1F5F9`, các hàng ngăn cách bởi đường kẻ ngang mỏng. Padding hàng vừa phải để tối ưu diện tích hiển thị.
- **Inputs:** Viền `#E2E8F0`, focus chuyển sang Primary `#0E7490` với hiệu ứng ring mờ.
- **Cards:** Nền trắng, bo góc 8px, có viền bao quanh, tiêu đề card nằm tách biệt phía trên bởi một đường kẻ ngang.