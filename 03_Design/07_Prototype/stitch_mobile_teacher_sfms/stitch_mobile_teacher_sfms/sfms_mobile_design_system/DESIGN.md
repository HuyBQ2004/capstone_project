---
name: SFMS Mobile Design System
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#3f484c'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#6f787d'
  outline-variant: '#bec8cd'
  surface-tint: '#006781'
  primary: '#005a71'
  on-primary: '#ffffff'
  primary-container: '#0e7490'
  on-primary-container: '#d3f1ff'
  inverse-primary: '#81d1f0'
  secondary: '#006877'
  on-secondary: '#ffffff'
  secondary-container: '#3fe1fd'
  on-secondary-container: '#00616f'
  tertiary: '#4b5459'
  on-tertiary: '#ffffff'
  tertiary-container: '#636c71'
  on-tertiary-container: '#e5eef4'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#b9eaff'
  primary-fixed-dim: '#81d1f0'
  on-primary-fixed: '#001f29'
  on-primary-fixed-variant: '#004d62'
  secondary-fixed: '#a2eeff'
  secondary-fixed-dim: '#2fd9f4'
  on-secondary-fixed: '#001f25'
  on-secondary-fixed-variant: '#004e5a'
  tertiary-fixed: '#dbe4ea'
  tertiary-fixed-dim: '#bfc8ce'
  on-tertiary-fixed: '#141d21'
  on-tertiary-fixed-variant: '#3f484d'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  headline-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
  headline-md:
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
  label-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
  label-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
  headline-lg-mobile:
    fontFamily: Be Vietnam Pro
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 30px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  container-padding: 16px
  stack-gap: 12px
  section-margin: 24px
  gutter: 12px
---

## Brand & Style
Hệ thống thiết kế này tập trung vào tính chuyên nghiệp, sự tin cậy và hiệu quả tối ưu dành cho đội ngũ giáo viên trong môi trường y tế/học đường. Phong cách chủ đạo kết hợp giữa **Minimalism (Tối giản)** và **Corporate Modern (Hiện đại)**, nhấn mạnh vào sự sạch sẽ, không gian trắng rộng rãi để giảm bớt căng thẳng thị giác khi xử lý dữ liệu học sinh. Giao diện sử dụng các lớp layer tinh tế, phân tách thông tin rõ ràng bằng khoảng cách và màu sắc nhẹ nhàng, tạo cảm giác an tâm và chính xác.

## Colors
Bảng màu được xây dựng dựa trên tông màu xanh mòng két (Teal) chuyên nghiệp, mang lại cảm giác y tế hiện đại và sạch sẽ.
- **Primary (#0E7490):** Sử dụng cho các hành động chính, nhận diện thương hiệu và các thành phần điều hướng quan trọng.
- **Secondary (#22D3EE):** Màu nhấn bổ trợ, dùng cho các chỉ số quan trọng hoặc trạng thái tích cực.
- **Surface/Tertiary (#F0F9FF):** Màu nền cho các thẻ (Card) hoặc vùng chứa để phân tách nội dung mà không gây nặng nề.
- **Neutral (#64748B):** Dành cho văn bản phụ và các biểu tượng không ưu tiên.
- **Semantic Colors:** 
  - Đỏ (Destructive/Allergy): #E11D48
  - Cam (Warning/Diet): #F59E0B
  - Xanh lá (Success): #10B981

## Typography
Sử dụng **Be Vietnam Pro** làm phông chữ duy nhất để đảm bảo tính đồng nhất và khả năng đọc tốt tiếng Việt.
- **Headlines:** Sử dụng Bold (700) cho các tiêu đề trang và SemiBold (600) cho tiêu đề các phần trong Dashboard.
- **Body:** Sử dụng Regular (400) cho các nội dung ghi chú về học sinh, đảm bảo độ giãn dòng thoáng (1.5x) để dễ theo dõi.
- **Labels:** Sử dụng Medium/SemiBold cho các nhãn Badge (Dị ứng, Ăn kiêng) để tăng khả năng nhận diện tức thì.

## Layout & Spacing
Hệ thống sử dụng mô hình **Fluid Grid** linh hoạt cho di động với lề an toàn (Safe margins) là 16px.
- **Rhythm:** Áp dụng hệ thống lưới 4px (8-12-16-24-32).
- **Cards:** Các thẻ trên Dashboard có khoảng cách (Gap) là 12px để tận dụng tối đa diện tích màn hình điện thoại mà vẫn đảm bảo sự rõ ràng.
- **Touch Targets:** Tất cả các thành phần tương tác (Button, Checkbox) phải có kích thước tối thiểu 44x44px.

## Elevation & Depth
Trong môi trường y tế, sự rõ ràng là tối thượng. Hệ thống sử dụng phương pháp **Tonal Layers** kết hợp với **Soft Shadows**:
- **Level 0 (Background):** Màu xám cực nhạt (#F8FAFC).
- **Level 1 (Cards/Surface):** Màu trắng tinh khiết với đổ bóng rất nhẹ (Blur 10px, Opacity 4%, Color #0E7490) để tạo cảm giác nổi khối nhẹ nhàng.
- **Level 2 (Active Elements):** Các thành phần đang được chọn sẽ có đường viền (Outline) mỏng 1px màu Primary thay vì đổ bóng đậm.

## Shapes
Theo yêu cầu, hệ thống sử dụng bán kính góc **8px (Rounded)** làm tiêu chuẩn cơ bản:
- **Buttons & Input Fields:** 8px.
- **Cards:** 12px (áp dụng `rounded-lg`) để tạo sự mềm mại và hiện đại hơn cho các mảng lớn.
- **Badges/Chips:** Sử dụng kiểu Pill-shaped (Full rounded) để phân biệt rõ ràng với các nút hành động.

## Components
Các thành phần được thiết kế để tối ưu hóa việc quản lý thông tin học sinh:

### 1. Buttons
- **Primary Button:** Nền #0E7490, chữ trắng, chiều cao 48px.
- **Secondary Button:** Viền #0E7490, nền trong suốt hoặc trắng, dùng cho các hành động phụ.

### 2. Dashboard Cards
- Cấu trúc: Icon góc trên trái (màu Primary nhạt), Tiêu đề (Headline-md), Chỉ số lớn (Headline-lg).
- Nền trắng, bo góc 12px, shadow nhẹ.

### 3. Status Badges (Đặc thù y tế)
- **Badge Dị ứng:** Nền đỏ nhạt, chữ đỏ đậm (#E11D48), icon cảnh báo nhỏ.
- **Badge Ăn kiêng:** Nền cam nhạt, chữ cam đậm (#F59E0B), icon đồng hồ hoặc muỗng nệm.
- **Badge Sức khỏe:** Nền xanh lá nhạt, chữ xanh lá đậm.

### 4. Input Fields
- Viền xám nhạt, khi focus chuyển sang màu Primary #0E7490 với độ dày viền 2px. Label nằm phía trên, font size 14px.

### 5. Bottom Navigation
- Nền trắng, có lớp mờ phía sau (Backdrop blur).
- Icon Active: Màu #0E7490 kèm label phía dưới.
- Icon Inactive: Màu #94A3B8.
- Chiều cao cố định: 56px + Safe Area Bottom.

### 6. Lists (Danh sách học sinh)
- Mỗi item cao tối thiểu 64px, bao gồm Avatar tròn (hoặc Initial name), Tên học sinh (Bold), và các Badge trạng thái hiển thị ngang hàng.