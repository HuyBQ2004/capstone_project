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
  display:
    fontFamily: Be Vietnam Pro
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Be Vietnam Pro
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  title-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 18px
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
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
  label-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
  headline-lg-mobile:
    fontFamily: Be Vietnam Pro
    fontSize: 20px
    fontWeight: '700'
    lineHeight: 28px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  container-padding: 24px
  gutter: 16px
  sidebar-width: 260px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 24px
---

## Brand & Style
The design system is engineered for professional school administration, focusing on high-fidelity utility and systematic clarity. The personality is authoritative yet modern, utilizing the "New York" variant style of shadcn/ui to provide a clean, corporate aesthetic that prioritizes data density and task efficiency.

The target audience consists of school administrators, nutritionists, and logistics coordinators who require a reliable tool to manage large-scale food programs. The emotional response is one of organized control and professional trust. This design system blends **Minimalism** with **Corporate/Modern** influences, utilizing subtle borders, refined typography, and a structured layout to handle complex Vietnamese text strings and extensive data tables without visual fatigue.

## Colors
The palette is rooted in a deep Cyan (#0E7490) primary color, evoking health, cleanliness, and administrative professionalism. The background uses a soft Slate (#F8FAFC) to reduce glare during long periods of use. 

Semantic colors follow standard professional patterns for clarity:
- **Primary:** Actions and primary navigation indicators.
- **Secondary:** Supporting metadata and subtle UI elements.
- **Semantic (Error/Success/Warning/Info):** Clear status indicators for food safety, payment status, and inventory alerts.
- **Neutral:** Used for high-contrast text and structural lines.

## Typography
This design system uses **Be Vietnam Pro** across all levels. This font is specifically chosen for its exceptional legibility with Vietnamese diacritics, ensuring that complex tonal marks do not clash with line heights or adjacent characters.

- **Headlines:** Use semi-bold weights with slight negative letter-spacing for a modern, compact corporate feel.
- **Body:** Set at 14px (body-md) as the default for data-heavy views to maximize information density.
- **Labels:** Used for table headers and form labels, emphasizing structural clarity.

## Layout & Spacing
The system utilizes a **Fixed-Fluid Hybrid** model. The sidebar remains fixed at 260px, while the main content area utilizes a fluid grid to accommodate expansive data tables. 

- **Grid:** A 12-column grid with 16px gutters is used for dashboard widgets and form layouts.
- **Density:** High density is achieved through 8px and 12px internal paddings within components.
- **Breakpoints:**
  - **Desktop (1280px+):** Full sidebar and multi-column tables.
  - **Tablet (768px - 1279px):** Collapsed sidebar (icons only), reflowing grid to 2 columns.
  - **Mobile (<768px):** Single column stack, hidden sidebar with hamburger menu, and horizontal scroll for data tables.

## Elevation & Depth
In alignment with the "New York" variant of shadcn/ui, the design system avoids heavy shadows. Instead, it uses **Low-Contrast Outlines** and subtle **Tonal Layers** to define depth.

- **Level 0 (Background):** Surface color (#F8FAFC).
- **Level 1 (Cards/Tables):** White background with a 1px border (#E2E8F0).
- **Level 2 (Popovers/Modals):** White background with a very soft, diffused ambient shadow (0px 4px 12px rgba(0,0,0,0.05)) to separate the element from the main interface.
- **Interactive:** Hover states on buttons and list items use a slight tonal shift (Slate-100) rather than an elevation increase.

## Shapes
The design system employs a consistent 8px (0.5rem) corner radius for most UI elements. This provides a professional balance—softer than a purely sharp aesthetic but more disciplined than a pill-shaped approach.

- **Small Components:** Buttons, inputs, and checkboxes use 8px.
- **Large Components:** Cards and modals use 12px (rounded-lg) for a more structured frame.
- **Status Badges:** Use a larger 16px (rounded-xl) or full-round radius to distinguish them from interactive buttons.

## Components
- **Buttons:** Primary buttons use a solid #0E7490 fill with white text. Secondary buttons use a ghost or outline style with #64748B.
- **Data Tables:** The core of the system. Use sticky headers, zebra striping (subtle Slate-50), and 1px borders. Typography in tables should be `body-md` for data and `label-sm` for headers (all caps with 0.05em tracking).
- **Status Badges:** Compact labels with light background tints and saturated text (e.g., Success: Green-100 background with Green-700 text).
- **Input Fields:** 1px border (#E2E8F0) that transitions to #0E7490 on focus. Labels use `label-md` positioned above the input.
- **Cards:** White background, 1px border, 24px internal padding. Used to group dashboard metrics and form sections.
- **Filters:** Horizontal bar above tables using select menus and search inputs to allow rapid data drilling.
- **Navigation:** Vertical sidebar with icons and `label-md` text; active states indicated by a left-hand primary color accent and a subtle background tint.