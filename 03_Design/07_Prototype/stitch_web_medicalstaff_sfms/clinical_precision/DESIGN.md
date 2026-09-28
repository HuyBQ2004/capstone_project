---
name: Clinical Precision
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
  display-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Be Vietnam Pro
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 34px
  headline-md:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  title-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-md:
    fontFamily: Be Vietnam Pro
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-caps:
    fontFamily: Be Vietnam Pro
    fontSize: 12px
    fontWeight: '700'
    lineHeight: 16px
    letterSpacing: 0.05em
  data-tabular:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
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
  container-padding: 24px
  column-gutter: 16px
---

## Brand & Style
The design system is engineered for high-stakes medical environments where clarity and rapid information processing are paramount. The personality is clinical, authoritative, and impeccably organized. 

The aesthetic blends **Modern Corporate** efficiency with **Minimalist** restraint. It prioritizes a "content-first" hierarchy, using significant white space to reduce cognitive load during long shifts. The emotional response is one of reliability and calm—ensuring medical staff feel supported by a tool that is as precise as their own practice. High contrast and clean borders are used to delineate critical data points without the visual noise of decorative elements.

## Colors
The palette is anchored by a deep Teal (`#0e7490`), chosen for its associations with hygiene, focus, and the medical profession. 

- **Primary (Teal):** Used for primary actions, active states, and brand identifiers. 
- **Secondary (Slate):** Utilized for metadata, secondary icons, and de-emphasized text to maintain a clear hierarchy.
- **Surface (Ghost Gray):** A range of cool grays (`#f8fafc` to `#f1f5f9`) defines container backgrounds and table headers to separate data modules.
- **Status Colors:** High-occupancy semantic colors are critical. Use `#dc2626` for urgent alerts/vitals, `#f59e0b` for pending reviews, and `#10b981` for stable/completed statuses.

## Typography
This design system utilizes **Be Vietnam Pro** across all levels to ensure a friendly yet professional contemporary feel. 

For clinical data management, the system emphasizes **Tabular Figures** (tnum) for all numeric values in patient charts and medical readings to ensure vertical alignment and easy scanning. Headlines use a tighter letter-spacing for a modern, compact look in dense dashboards. Body text maintains generous line-heights to prevent eye fatigue during intensive record-keeping.

## Layout & Spacing
The layout employs a **12-column fluid grid** for desktop dashboards, collapsing to a single column for mobile triage views. 

Spacing follows a strict **4px baseline rhythm**.
- **Clinical Density:** In data-heavy views (like EHR tables), use `sm` (8px) padding for rows.
- **Administrative Content:** For patient profiles and settings, use `md` (16px) or `lg` (24px) spacing to increase legibility.
- **Breakpoints:** Mobile (<640px), Tablet (640px-1024px), Desktop (>1024px). Sidebar navigation remains fixed on desktop and transitions to a bottom bar or hamburger menu on mobile.

## Elevation & Depth
This design system uses **Low-Contrast Outlines** combined with **Ambient Shadows** to create a structured, professional hierarchy.

- **Flat Surfaces:** Primary background is `#ffffff`. 
- **Subtle Borders:** Use 1px solid `#e2e8f0` for all card and section boundaries to maintain a structured "grid" feel without heavy shadows.
- **Soft Depth:** Only use shadows for interactive elements like dropdowns, modals, and hovering cards. The shadow should be highly diffused: `0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)`.
- **Tonal Layering:** Use a slight background tint (`#f8fafc`) for table headers or "read-only" sections to distinguish them from editable fields.

## Shapes
The shape language follows the `Rounded` logic (8px/0.5rem base) to provide a soft, approachable edge to the otherwise rigid clinical data. 

- **Standard Elements:** Buttons, Input fields, and Cards use the 8px radius.
- **Large Elements:** Modals and large dashboard panels use `rounded-lg` (16px).
- **Small Elements:** Tooltips and Status Badges use `rounded-sm` (4px).
- **Selection Indicators:** Checkboxes and Radio buttons retain a slightly softer edge but avoid the pill-shape to maintain a professional, administrative tone.

## Components
- **Buttons:** Primary buttons use a solid Teal (`#0e7490`) background with white text. Secondary buttons use a Slate border with Teal text. All buttons have a height of 40px for desktop and 48px for touch-targets.
- **Input Fields:** Use 1px solid `#cbd5e1` borders. On focus, the border transitions to Teal with a 2px outer glow. Labels are always persistent; never use floating placeholders in medical contexts to avoid confusion.
- **Status Chips:** Use a subtle background (10% opacity of the semantic color) with a bold 1px left-border or a small leading dot to indicate status without overwhelming the text.
- **Data Tables:** These are the core of the system. Use "Zebra striping" sparingly; instead, use thin 1px horizontal dividers. Cell padding should be tight (`8px 12px`).
- **Patient Cards:** Use a white background, 1px gray border, and a 8px corner radius. Summary vitals should be displayed in a horizontal sub-grid within the card.
- **Alerts/Toasts:** Positioned in the top-right, featuring a heavy left-border color-coded to the severity of the alert.