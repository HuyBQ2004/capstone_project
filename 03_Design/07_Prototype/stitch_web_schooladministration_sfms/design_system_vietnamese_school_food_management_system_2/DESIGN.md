---
name: Vietnamese School Food Management System
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
    letterSpacing: -0.01em
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
  caption:
    fontFamily: Be Vietnam Pro
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  container-max: 1400px
  gutter: 1rem
  margin-mobile: 1rem
  margin-desktop: 2rem
  stack-gap: 1.5rem
  element-gap: 0.5rem
---

## Brand & Style

The design system is engineered for school administrators, prioritizing clarity, efficiency, and professional trust. It adopts a refined **Corporate/Modern** aesthetic inspired by the "New York" style—characterized by high information density, subtle borders, and a systematic approach to whitespace. 

The interface aims to evoke a sense of organized stability. Given its function in managing student nutrition and classroom logistics, the UI remains understated to allow critical data and status alerts to stand out. The experience is functional and utilitarian, ensuring that administrative tasks are completed with minimal cognitive load.

## Colors

The palette is anchored by a deep Cyan (#0E7490), representing the professional and institutional nature of educational management. 

- **Primary**: Used for main actions, active states, and primary navigation elements.
- **Secondary**: A muted slate used for sub-navigation and secondary metadata.
- **Neutral**: Deep charcoal for text and high-contrast borders, following the "New York" aesthetic of sharp, dark strokes.
- **Backgrounds**: A mix of pure white for primary surfaces and a very light grey (Tertiary) for section backgrounds to create subtle containment.
- **Status States**: Highly saturated tones for alerts. **Warning** (Amber) is used for impactful administrative changes like teacher reassignment, while **Error** (Red) signals blocking issues such as missing academic year configurations.

## Typography

This design system uses **Be Vietnam Pro** exclusively to ensure excellent legibility for Vietnamese diacritics. The type scale is compact, favoring a 14px base to accommodate data-heavy tables and forms common in school administration.

Headlines use semi-bold and bold weights with tighter letter spacing to create a strong visual hierarchy. Labels and UI accents use medium weights to distinguish them from body text. For mobile views, H1 and H2 sizes scale down to 24px and 20px respectively to maintain balance on smaller screens.

## Layout & Spacing

The layout follows a **Fixed Grid** model on desktop, centered within a 1400px container, and a **Fluid** model on mobile devices.

- **Desktop**: A 12-column system with 16px gutters. Sidebars are fixed at 240px or 280px.
- **Padding**: Large containers (Cards) use 24px padding. Smaller components use a strict 4px or 8px increment (0.25rem/0.5rem).
- **Mobile Adaptation**: On screens smaller than 768px, the sidebar collapses into a sheet/drawer, and horizontal padding reduces to 16px. Tables transition to a card-based list format or a horizontally scrollable view if data density is high.

## Elevation & Depth

This design system utilizes **Low-contrast outlines** and **Tonal layers** instead of heavy shadows. 

- **Surfaces**: Level 0 is the page background (#F8FAFC). Level 1 are primary cards and containers (#FFFFFF).
- **Borders**: Elements are separated by 1px borders (#E2E8F0). Active or focused states use a 1px border of the primary color or a subtle 2px ring.
- **Shadows**: Only used for floating elements like popovers, dropdowns, and modals. These shadows are very soft, with a large blur and low opacity (Alpha 5-10%), ensuring they feel like they belong to the same plane.

## Shapes

The design system uses a **Rounded** shape language with a base radius of 8px (0.5rem). 

- **Standard**: Buttons, Input fields, and small containers use 8px.
- **Large**: Main dashboard cards and modals use 12px (rounded-lg).
- **Extra Large**: Feature highlights or promotional banners may use 24px (rounded-xl).
- **Interactive**: Checkboxes use a smaller 4px radius to maintain a crisp, professional look.

## Components

- **Buttons**: Primary buttons use a solid fill (#0E7490) with white text. Secondary buttons use a ghost style with a thin border. Destructive actions (e.g., deleting a class) use a red ghost or solid style.
- **Input Fields**: Standardized with an 8px radius and a 1px slate border. Focus states use a primary color ring. Labels are always positioned above the input.
- **Alerts (Cảnh báo)**: Essential for the Classroom Management module. 
    - *Warning (Thay đổi giáo viên)*: A light amber background with a dark amber left-border and icon. 
    - *Error (Thiếu năm học)*: A light red background with a dark red left-border and icon.
- **Tables (Danh sách lớp học)**: Clean rows with subtle hover states. Headers are styled in a light grey with uppercase labels for maximum clarity.
- **Chips/Badges**: Used for "Sĩ số" (Enrollment) or "Trạng thái" (Status). Rounded-full (pill) shapes with low-saturation backgrounds.
- **Classroom Cards**: Provide a summary of class details, assigned teacher, and meal count status, utilizing the 12px rounded corner style.