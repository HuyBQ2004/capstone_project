---
name: Care & Nutri
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
  secondary: '#006c49'
  on-secondary: '#ffffff'
  secondary-container: '#6cf8bb'
  on-secondary-container: '#00714d'
  tertiary: '#764900'
  on-tertiary: '#ffffff'
  tertiary-container: '#965f00'
  on-tertiary-container: '#ffe9d4'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#b9eaff'
  primary-fixed-dim: '#81d1f0'
  on-primary-fixed: '#001f29'
  on-primary-fixed-variant: '#004d62'
  secondary-fixed: '#6ffbbe'
  secondary-fixed-dim: '#4edea3'
  on-secondary-fixed: '#002113'
  on-secondary-fixed-variant: '#005236'
  tertiary-fixed: '#ffddb8'
  tertiary-fixed-dim: '#ffb95f'
  on-tertiary-fixed: '#2a1700'
  on-tertiary-fixed-variant: '#653e00'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  headline-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 38px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.01em
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
  label-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
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
  element-gap: 12px
  section-margin: 24px
  grid-gutter: 16px
---

## Brand & Style

This design system is built for a School Food Management System (SFMS) tailored for parents, emphasizing medical-grade reliability, safety, and academic professionalism. The brand personality is "Vigilant yet Warm"—it must convey the precision of a healthcare application while maintaining the approachability of a school-oriented service.

The design style follows **Modern Corporate** principles with a focus on high legibility and clear information hierarchy. By utilizing a "Medical-Grade" aesthetic, the system employs ample whitespace, structured data presentation, and a disciplined use of color to ensure that critical information—such as allergy alerts and nutritional intake—is never overlooked. The emotional response should be one of profound trust, organized calm, and parental reassurance.

## Colors

The palette is strategically weighted to prioritize safety and clarity. 

- **Primary (Cyan-700):** Used for core navigation, primary actions, and branding elements. It evokes professionalism and clinical cleanliness.
- **Success (Emerald-500):** Reserved for "Safe" food indicators, successful payment confirmations, and completed nutritional goals.
- **Warning (Amber-500):** Used for low-balance alerts or non-critical dietary deviations.
- **Danger (Red-500):** Strictly reserved for high-priority allergy alerts and urgent medical incidents. This color should be used sparingly but with high visibility.
- **Neutral (Slate-500):** Used for secondary text and decorative elements to maintain a grounded, sophisticated feel.

## Typography

The design system utilizes **Be Vietnam Pro** across all touchpoints. This font was selected for its exceptional support for Vietnamese diacritics and its clean, contemporary humanist geometry.

- **Headlines:** Use Bold (700) or SemiBold (600) weights to establish clear hierarchy. Use tighter letter-spacing on larger titles to maintain a professional, "newsreader" density.
- **Body Text:** Use Regular (400) for all descriptive content to ensure high readability during long-form reading of menu ingredients or medical reports.
- **Labels:** Use SemiBold (600) and uppercase styling for small metadata or button labels to differentiate them from body content.

## Layout & Spacing

This design system uses a **Fluid Grid** model with strict adherence to an 8px spacing scale. 

- **Mobile:** 4-column grid with 16px side margins and 16px gutters.
- **Desktop:** 12-column centered grid with a max-width of 1200px.
- **Spacing Philosophy:** Use generous vertical rhythm (24px+) between distinct content blocks (e.g., separating "Thực đơn hôm nay" from "Thông tin dị ứng"). Smaller elements within a card should use 8px or 12px gaps to maintain visual grouping.

## Elevation & Depth

To maintain a "medical" aesthetic, elevation is handled through **Tonal Layering** and **Low-Contrast Outlines** rather than heavy shadows.

- **Level 0 (Background):** #F8FAFC (Slate-50).
- **Level 1 (Cards/Containers):** Pure white surface with a 1px border of #E2E8F0. 
- **Level 2 (Active States/Popovers):** A very soft, diffused shadow (0px 4px 12px rgba(0, 0, 0, 0.05)) is used to indicate depth only when an element is interactive or floating.

This approach keeps the interface looking flat, clean, and organized, preventing the "clutter" that heavy shadows can create in data-dense medical applications.

## Shapes

The shape language is defined by **Rounded (8px)** corners. This radius provides a perfect balance between the rigid "seriousness" of a 0px corner and the "playfulness" of a fully rounded pill. 

- **Containers:** 8px (`rounded-lg`) for all medical cards, input fields, and alert banners.
- **Inner Elements:** 4px for small tags or checkboxes nested inside larger containers.
- **Buttons:** 8px to match the container language, maintaining a consistent structural identity.

## Components

### Medical Cards
Cards containing student health data or meal details should use the Level 1 Elevation (White surface, 1px Slate-200 border). The header of the card should use `headline-md`. If a student has an active allergy, the card border should change to `Danger (#EF4444)`.

### Alert Banners
Banners for "Cảnh báo dị ứng" (Allergy Alerts) must use a high-saturation background of the status color with white text. Banners should be pinned to the top of the view or the top of the relevant card.

### Nutrition Charts
Use simplified bar or ring charts. Use `Primary (#0E7490)` for target values and `Neutral (#CBD5E1)` for empty states. Labels within charts must use `label-sm`.

### Tab Bars
Bottom navigation for mobile should use a clear white background with a top border. Active icons/labels use `Primary (#0E7490)`. Labels include: "Trang chủ", "Thực đơn", "Sức khỏe", "Tài khoản".

### Input Fields
Inputs use an 8px radius with a 1px Slate-300 border. Upon focus, the border shifts to `Primary (#0E7490)` with a 2px outer glow. Labels (e.g., "Tên học sinh") must always be visible above the field.

### Buttons
- **Primary:** Solid `#0E7490` with white text for main actions (e.g., "Nạp tiền", "Xác nhận").
- **Secondary:** White background with `#0E7490` border and text for "Xem chi tiết" or "Lịch sử".