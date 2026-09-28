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
  body-base:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
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
  container-padding: 2rem
  gutter: 1rem
  section-gap: 1.5rem
  element-gap: 0.5rem
---

## Brand & Style
Hệ thống thiết kế này được xây dựng cho ứng dụng quản lý thực phẩm trường học (SFMS), hướng tới đối tượng người dùng là Ban Giám hiệu và các cán bộ quản lý. 

Phong cách chủ đạo kế thừa tinh thần của **Modern Corporate** kết hợp với sự tinh gọn của **shadcn/ui (New York style)**. Giao diện tập trung vào tính hiệu năng, sự minh bạch và chuyên nghiệp. Mục tiêu là tạo ra một môi trường làm việc ít xao nhãng, giúp người điều hành nhanh chóng đưa ra quyết định dựa trên dữ liệu.

Cảm xúc chủ đạo:
- **Tin cậy:** Cấu trúc chặt chẽ, các thành phần giao diện nhất quán.
- **Hiện đại:** Sử dụng khoảng trắng thông minh, typography sắc nét và các đường kẻ mảnh.
- **Sạch sẽ:** Loại bỏ các chi tiết trang trí thừa, tập trung vào nội dung và chức năng.

## Colors
Hệ thống sử dụng bảng màu tập trung vào sự chuyên nghiệp của khối giáo dục và quản lý nhà nước.

- **Primary (#0E7490):** Màu xanh Teal đậm, đại diện cho sự ổn định, tin cậy và thẩm quyền. Được sử dụng cho các hành động chính, trạng thái active và nhận diện thương hiệu.
- **Surface:** Sử dụng màu trắng (#FFFFFF) cho các card và vùng làm việc chính. Màu xám nhạt (#F8FAFC) được dùng cho nền ứng dụng để tạo chiều sâu lớp (layering).
- **Semantic (Vi phạm):**
  - **Nghiêm trọng:** `#991B1B` (Đỏ đậm) - Cảnh báo mức độ ưu tiên cao nhất.
  - **Cao:** `#EF4444` (Đỏ tươi) - Yêu cầu xử lý ngay.
  - **Trung bình:** `#F59E0B` (Vàng hổ phách) - Cần lưu ý.
  - **Thấp:** `#94A3B8` (Xám) - Các vấn đề nhỏ hoặc thông tin bổ sung.
- **Status (Trạng thái):**
  - **Chờ xử lý:** Xanh dương nhạt hoặc Xám.
  - **Đang khắc phục:** Cam/Vàng.
  - **Đã giải quyết:** Xanh lá cây (#10B981).

## Typography
Sử dụng **Be Vietnam Pro** xuyên suốt hệ thống. Đây là font chữ hiện đại, được tối ưu hóa cho tiếng Việt, mang lại cảm giác thân thiện nhưng vẫn giữ được sự chỉn chu cần thiết cho một ứng dụng quản lý.

- **Headlines:** Sử dụng độ đậm (Bold/SemiBold) và letter-spacing hẹp để tạo sự vững chãi.
- **Body:** Ưu tiên kích thước 14px cho nội dung chính để tối ưu mật độ thông tin trên desktop.
- **Labels:** Sử dụng Medium weight cho các nhãn input và tiêu đề cột trong bảng.

## Layout & Spacing
Hệ thống áp dụng mô hình **Fixed Grid** cho các trang dashboard với chiều rộng tối đa (max-width) được kiểm soát để đảm bảo khả năng đọc.

- **Cấu trúc:** Sử dụng sidebar cố định bên trái (240px - 280px) và vùng nội dung cuộn bên phải.
- **Rhythm:** Hệ thống khoảng cách dựa trên bội số của 4px (Base-4).
- **Padding:** Các Card nội dung sử dụng padding đồng nhất là 24px (1.5rem).
- **Responsive:** Trên mobile, sidebar chuyển thành ngăn kéo (drawer), padding container giảm xuống còn 16px.

## Elevation & Depth
Tuân thủ phong cách shadcn/ui "New York", hệ thống hạn chế sử dụng shadow đổ bóng lớn. Thay vào đó, chiều sâu được tạo ra bởi:

- **Lớp nền (Layering):** Sử dụng các tone xám khác nhau để phân biệt giữa nền trang và bề mặt component.
- **Đường viền (Borders):** Sử dụng các đường kẻ 1px màu `#E2E8F0` để phân tách các khu vực.
- **Shadow tinh tế:** Chỉ sử dụng shadow rất mờ (`0 1px 2px 0 rgb(0 0 0 / 0.05)`) cho các card và dropdown để tạo cảm giác nổi nhẹ so với bề mặt.

## Shapes
Hệ thống sử dụng quy chuẩn **Round Eight** (Bo góc 8px) làm chủ đạo để tạo sự cân bằng giữa nét hiện đại và sự chuyên nghiệp.

- **Mặc định (Rounded):** 8px cho Button, Input, Card và các Badge lớn.
- **Nhỏ (Small):** 4px cho Checkbox và các tooltip mini.
- **Lớn (Large):** 12px cho các Modal hoặc Menu nổi.

## Components

### 1. Bảng (Table)
- **Thiết kế:** Dòng kẻ ngang mảnh, không có đường kẻ dọc. Header bảng sử dụng nền xám nhạt hoặc màu trắng với chữ in đậm/medium.
- **Tương tác:** Hiệu ứng hover dòng (background thay đổi nhẹ sang `#F1F5F9`).
- **Mật độ:** Padding dọc của cell là 12px để đảm bảo sự thoáng đãng nhưng vẫn chứa được nhiều dữ liệu.

### 2. Huy hiệu (Badge)
Được sử dụng để hiển thị mức độ vi phạm và trạng thái.
- **Mức độ vi phạm:**
  - **Nghiêm trọng:** Nền `#FEE2E2`, chữ `#991B1B`, viền nhạt.
  - **Cao:** Nền `#FEF2F2`, chữ `#EF4444`.
  - **Trung bình:** Nền `#FEF3C7`, chữ `#92400E`.
  - **Thấp:** Nền `#F1F5F9`, chữ `#475569`.
- **Trạng thái:**
  - **Chờ xử lý:** Kiểu Ghost (chỉ có viền) hoặc màu xám trung tính.
  - **Đang khắc phục:** Màu xanh dương (Primary nhạt) hoặc màu cam.
  - **Đã giải quyết:** Nền `#D1FAE5`, chữ `#065F46`.

### 3. Nút (Button)
- **Primary:** Nền `#0E7490`, chữ trắng. Không có gradient.
- **Secondary:** Nền xám nhạt, chữ đen.
- **Outline:** Viền 1px, nền trong suốt, chữ màu Primary.

### 4. Nhập liệu (Input & Form)
- Border mặc định `#E2E8F0`. Khi focus, sử dụng ring màu Primary với độ mờ (opacity) thấp.
- Label luôn nằm phía trên trường nhập liệu, cỡ chữ 14px, Medium weight.