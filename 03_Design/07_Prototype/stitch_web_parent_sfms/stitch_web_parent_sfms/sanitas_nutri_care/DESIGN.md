---
name: Sanitas Nutri-Care
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
  secondary: '#55615f'
  on-secondary: '#ffffff'
  secondary-container: '#d8e5e2'
  on-secondary-container: '#5b6765'
  tertiary: '#045e45'
  on-tertiary: '#ffffff'
  tertiary-container: '#2b775c'
  on-tertiary-container: '#affcda'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#b9eaff'
  primary-fixed-dim: '#81d1f0'
  on-primary-fixed: '#001f29'
  on-primary-fixed-variant: '#004d62'
  secondary-fixed: '#d8e5e2'
  secondary-fixed-dim: '#bcc9c6'
  on-secondary-fixed: '#121e1c'
  on-secondary-fixed-variant: '#3d4947'
  tertiary-fixed: '#a6f2d1'
  tertiary-fixed-dim: '#8bd6b6'
  on-tertiary-fixed: '#002116'
  on-tertiary-fixed-variant: '#00513b'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  display-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 48px
    fontWeight: '700'
    lineHeight: '1.2'
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 32px
    fontWeight: '600'
    lineHeight: '1.3'
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.3'
  headline-md:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.4'
  headline-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 20px
    fontWeight: '600'
    lineHeight: '1.4'
  body-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
  body-md:
    fontFamily: Be Vietnam Pro
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.6'
  body-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
  label-md:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '600'
    lineHeight: '1'
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 12px
    fontWeight: '500'
    lineHeight: '1'
    letterSpacing: 0.01em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  base: 4px
  xs: 8px
  sm: 16px
  md: 24px
  lg: 32px
  xl: 48px
  sidebar-width: 280px
  container-max: 1280px
---

## Brand & Style

The brand personality is rooted in institutional trust, hygiene, and parental peace of mind. As a tool for monitoring School Food Management Services (SFMS), the design system must feel clinically clean yet approachable. It addresses parents who are concerned about their children's nutritional safety and health outcomes.

The design style is **Corporate / Modern** with a strong leaning toward **Medical** clarity. It prioritizes information density and legibility over decorative elements. The visual language uses high whitespace, a systematic grid, and a calming professional palette to reduce cognitive load when viewing health data and dietary alerts. The interface should feel "safe"—predictable, stable, and meticulously organized.

## Colors

The palette is anchored by a deep Medical Teal (`#0E7490`), symbolizing hygiene and professional care. 

- **Primary**: Used for core actions, active navigation states, and branding accents.
- **Secondary**: A very light teal tint used for large background areas, card headers, and soft grouping to maintain a fresh, airy feel.
- **Tertiary**: A deep forest green reserved for health-positive indicators (e.g., "Safe," "Nutritious").
- **Neutral**: A slate-based neutral palette ensures that text and structural elements feel modern and cool-toned rather than muddy.
- **Semantic Colors**: Critical for food safety alerts. High-contrast reds and ambers are used sparingly to ensure immediate attention to allergy warnings or missed meal alerts.

## Typography

**Be Vietnam Pro** is used throughout for its exceptional legibility in Vietnamese and its friendly yet professional tone.

- **Weight Usage**: Use `700` (Bold) only for major section headers. Use `600` (SemiBold) for sub-headers and labels to maintain a structured hierarchy without appearing aggressive.
- **Vietnamese Diacritics**: Ensure line-height is never tighter than `1.3` for headlines to accommodate stacked diacritics in the Vietnamese language.
- **Data Display**: For numerical data (calories, nutritional counts), use `Medium (500)` weight to ensure clarity against descriptive text.

## Layout & Spacing

The design system utilizes a **Fixed Grid** on desktop to maintain a professional, dashboard-like control center feel.

- **Grid System**: A 12-column grid with 24px gutters.
- **Sidebar**: A persistent 280px sidebar on the left for primary navigation (Dashboard, Menus, Health Reports, Payments).
- **Top Bar**: Contains a "Student Switcher" (Avatar + Name dropdown) and high-level global alerts.
- **Mobile Adaption**: On mobile, the sidebar collapses into a bottom navigation bar or a hamburger menu. Margins reduce to 16px. Cards stack vertically, and the student switcher remains sticky at the top for quick context changes.

## Elevation & Depth

To maintain the medical aesthetic, depth is created through **Tonal Layers** and **Low-contrast outlines** rather than heavy shadows.

- **Background**: The main canvas uses a very subtle off-white or the `secondary_color_hex` tint.
- **Cards**: Surface-level cards are pure white with a 1px border in a light neutral shade (`#E2E8F0`). 
- **Active States**: Subtle ambient shadows (Blur 8px, Spread 0, Opacity 4%) are used only for active or hovered interactive elements to indicate clickability.
- **Layering**: Modals and dropdowns use a slightly more pronounced shadow to separate them from the content plane, but should still feel light and non-intrusive.

## Shapes

The shape language is **Soft** (0.25rem - 0.75rem), balancing the clinical nature of medical data with the approachability of a parent-facing app. 

- **Containers/Cards**: Use `0.5rem` (8px) for standard cards. 
- **Buttons**: Use `0.25rem` (4px) for a more structured, institutional appearance.
- **Avatars**: Always circular to denote personhood and distinguish from UI components.
- **Alerts**: Use the same radius as cards to ensure they feel like part of the systematic layout.

## Components

- **Clean Cards**: Every card must have a clear title in `headline-sm` and a thin separator or distinct header background. Content should be padded with `24px` internally.
- **Clear Alerts**: Safety alerts use high-saturation left borders (4px width) to denote severity. Red for allergen presence, Amber for low balance, Teal for nutritional milestones.
- **Student Switcher**: A prominent component in the top-right or top-left. It features a circular child photo, name, and grade. On click, it reveals a clean list of other children linked to the parent.
- **Data Visualization**: Charts (for calorie intake or macro-nutrients) should use the primary teal and its tints. Avoid multi-color "rainbow" charts; use monochromatic scales to maintain professional focus.
- **Input Fields**: Labeled clearly above the field. Border-based with a focus state that utilizes a 2px primary teal ring.
- **Buttons**: Primary buttons are solid Teal (`#0E7490`) with white text. Secondary buttons are outline-only. Tertiary buttons (for less common actions) are text-only with the primary color.
- **Lists**: Nutritional logs and transaction histories should use alternating row tints or subtle dividers for high legibility in data-dense views.