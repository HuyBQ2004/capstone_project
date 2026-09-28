---
name: School Healthcare Management System
colors:
  surface: '#f7f9fb'
  surface-dim: '#d8dadc'
  surface-bright: '#f7f9fb'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f4f6'
  surface-container: '#eceef0'
  surface-container-high: '#e6e8ea'
  surface-container-highest: '#e0e3e5'
  on-surface: '#191c1e'
  on-surface-variant: '#3f484c'
  inverse-surface: '#2d3133'
  inverse-on-surface: '#eff1f3'
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
  tertiary: '#495553'
  on-tertiary: '#ffffff'
  tertiary-container: '#616d6b'
  on-tertiary-container: '#e2efec'
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
  tertiary-fixed: '#d8e5e2'
  tertiary-fixed-dim: '#bcc9c6'
  on-tertiary-fixed: '#121e1c'
  on-tertiary-fixed-variant: '#3d4947'
  background: '#f7f9fb'
  on-background: '#191c1e'
  surface-variant: '#e0e3e5'
typography:
  headline-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Be Vietnam Pro
    fontSize: 26px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.02em
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
    letterSpacing: 0.1px
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
  base: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  margin-mobile: 16px
  margin-tablet: 24px
  gutter: 16px
---

## Brand & Style

This design system is engineered for a school medical environment, prioritizing clarity, trust, and professional efficiency. The visual direction follows a **Corporate Modern** approach, heavily influenced by **Material Design 3** principles but refined for a medical context.

The target audience includes school nurses, administrators, and parents. The UI evokes a sense of organized calm and reliability through generous whitespace and a systematic use of the primary teal palette. By utilizing soft edges and a light-filled interface, the design reduces the inherent stress associated with medical monitoring while maintaining the authority of a healthcare platform.

## Colors

The palette is anchored by a deep **Cyan-Teal (#0E7490)**, representing hygiene and clinical precision. This is supported by a neutral Slate secondary color for non-critical information.

- **Primary**: Used for key actions, active states, and branding elements.
- **Surface**: A clean, near-white background (#F8FAFC) ensures content remains the focus and minimizes visual noise.
- **Semantic Colors**: Traditional healthcare signaling is maintained—Success (Emerald), Danger (Red), and Warning (Amber)—to ensure critical health alerts are immediately recognizable.

## Typography

The design system utilizes **Be Vietnam Pro** across all levels. This font choice balances a friendly, approachable character with a modern, contemporary structure that remains highly legible in data-dense medical environments.

- **Headlines**: Use Bold and SemiBold weights to establish a clear information hierarchy.
- **Body**: Standardized at 16px for primary reading to ensure accessibility for all age groups.
- **Labels**: Used for button text and form headers, utilizing medium and semi-bold weights for better scannability.

## Layout & Spacing

This design system employs a **Fluid Grid** model with a base-8 rhythm. On mobile devices, the layout uses a 4-column grid with 16px side margins.

- **Vertical Rhythm**: Spacing between sections should follow the 16px (md) or 24px (lg) increments to maintain a structured, clinical feel.
- **Safe Areas**: Ensure all interactive elements are within the safe area of modern mobile displays, with a minimum touch target size of 44x44px.
- **Padding**: Internal card padding is standardized at 16px to ensure readability without making the interface feel cramped.

## Elevation & Depth

In alignment with Material Design 3, this design system uses **Tonal Layers** rather than heavy drop shadows to define depth.

- **Level 0 (Surface)**: Background #F8FAFC.
- **Level 1 (Cards)**: White (#FFFFFF) with a very subtle 1px border (#E2E8F0) or a soft 4% opacity shadow.
- **Level 2 (Modals/Overlays)**: Elevated with a soft, diffused shadow (0px 4px 12px rgba(14, 116, 144, 0.08)) to indicate priority.
- **Interactions**: On-tap states use a subtle tonal shift (darkening the primary color by 10%) rather than an elevation change.

## Shapes

The design system adopts a **Rounded (8px/0.5rem)** strategy for all standard UI components. This choice strikes a balance between the rigid "sharp" professional look and the overly "soft" consumer look.

- **Small Components**: Checkboxes and small tags use a 4px (Soft) radius.
- **Standard Components**: Buttons, Input fields, and Cards use the 8px (Rounded) radius.
- **Large Components**: Bottom sheets and dialogue containers use 16px (Rounded-LG) on top corners.

## Components

### Buttons
- **Primary**: Solid #0E7490 with white text. 8px corner radius. Minimum height 48px for mobile accessibility.
- **Secondary**: Outlined with #0E7490 border and text.
- **Ghost**: Transparent background with teal text for low-emphasis actions like "Cancel."

### Input Fields
- Outlined style with 8px radius. 
- Label sits on the border or above the field. 
- Active state uses a 2px teal border.
- Error states clearly highlight the border in #EF4444 with helper text below.

### Cards
- Pure white background.
- 8px corner radius.
- Used for student profiles, medical records, and appointment summaries.
- Padding should be a consistent 16px.

### Chips & Badges
- Used for status (e.g., "Allergy," "Medication," "Stable").
- Pill-shaped (fully rounded) to differentiate from action buttons.
- Use low-saturation background tints of the semantic colors with high-saturation text for readability.

### Lists
- Clean, divider-based layout.
- Icons should be simplified, 24px size, using the primary teal or secondary slate.
- Standard list item height: 56px for single line, 72px for double line.