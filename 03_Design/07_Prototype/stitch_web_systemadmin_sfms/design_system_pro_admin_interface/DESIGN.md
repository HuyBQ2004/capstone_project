---
name: Pro-Admin Interface
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
  background: '#f7f9fb'
  on-background: '#191c1e'
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
  section-gap: 1.5rem
  element-gap: 0.75rem
  table-cell-padding: 0.75rem 1rem
  grid-gutter: 1rem
---

## Brand & Style
This design system is engineered for high-density administrative tasks, prioritizing clarity, efficiency, and professional rigor. The brand personality is dependable and systematic, focusing on a "New York" inspired aesthetic: clean lines, generous white space, and high-contrast information hierarchies.

The visual style leans heavily into **Modern Minimalism** with a technical edge. It utilizes a structured grid, subtle borders instead of heavy shadows, and a sophisticated use of teal to guide user attention without overwhelming the functional purpose of the interface. The emotional response should be one of control, precision, and ease of navigation within complex data environments.

## Colors
The palette is rooted in a professional slate and teal spectrum. 
- **Primary (#0E7490):** Used for primary actions, active states, and critical progress indicators. It provides a calm yet distinct focal point.
- **Surface (#F8FAFC):** A clean Slate-50 background that reduces eye strain during long working sessions.
- **Borders (#E2E8F0):** Defining the structural "New York" style, these Slate-200 borders create clear containment without visual noise.
- **Destructive (#EF4444):** Reserved strictly for high-risk actions like deletions or system errors.
- **Text Hierarchy:** Use `Slate-900` for headings, `Slate-600` for body text, and `Slate-400` for deactivated or placeholder states.

## Typography
**Be Vietnam Pro** is selected for its exceptional legibility in both Vietnamese and English, featuring a friendly yet contemporary geometric structure.

- **Scale:** We use a tight typographic scale (14px base) to accommodate high-density data tables and complex forms typical of admin systems.
- **Weights:** Semi-bold (600) and Bold (700) are used for semantic signaling and headers. Regular (400) is used for all long-form reading and data entry.
- **Alignment:** Consistent left-alignment is preferred for all Vietnamese text to maintain a clean vertical rhythm.

## Layout & Spacing
The layout follows a **Fixed-Fluid hybrid grid**. Sidebars and navigation elements remain fixed in width, while data content areas expand fluidly to maximize screen real estate.

- **Layout Model:** A 12-column grid is used for dashboard layouts, with a 16px (1rem) gutter. 
- **Breakpoints:**
  - **Mobile (<768px):** Single column, 1rem margins, hidden sidebar (accessible via Sheet component).
  - **Tablet (768px - 1024px):** Condensed sidebar, 1.5rem margins.
  - **Desktop (>1024px):** Full sidebar, 2rem margins, multi-column dashboard cards.
- **Rhythm:** Spacing follows a 4px base unit (0.25rem), ensuring all components align to a predictable vertical and horizontal flow.

## Elevation & Depth
This design system utilizes **Tonal Layers** and **Low-contrast Outlines** rather than traditional shadows to convey depth, adhering to the shadcn "New York" philosophy.

- **Level 0 (Background):** Slate-50 (#F8FAFC).
- **Level 1 (Cards/Sheets):** White (#FFFFFF) with a 1px border of Slate-200 (#E2E8F0).
- **Level 2 (Popovers/Modals):** White (#FFFFFF) with a subtle 1px border and a very soft, diffused ambient shadow (Alpha 0.05) to separate the element from the card layer.
- **Interactive States:** On hover, buttons and interactive cards should transition their border color or background color slightly (e.g., Slate-100 background) rather than increasing shadow depth.

## Shapes
The shape language is defined by the **Round Eight (8px)** standard. This provides a balance between a modern, approachable feel and a structured, professional container.

- **Standard Elements:** Buttons, Input fields, and Checkboxes use 8px (0.5rem) corner radii.
- **Large Elements:** Cards and Modals use 12px (0.75rem) or 16px (1rem) radii to soften large surface areas.
- **Small Elements:** Badges/Tags use 6px or a full pill shape depending on the context of the data.

## Components
Consistent styling across shadcn/ui components ensures a unified admin experience:

- **Buttons:** 
  - *Primary:* Solid Teal (#0E7490) with white text. 
  - *Secondary:* Ghost or Outline styles using Slate-200 borders.
- **Tables:** Use a minimal header with Slate-50 background and Slate-900 bold text. Rows should have a subtle hover effect (Slate-50) and 1px bottom borders.
- **Input Fields:** 8px rounded, 1px Slate-200 border. Focus state uses a 2px Teal ring with an offset.
- **Badges:** Use a soft background (Teal-50) with dark Teal text for "Active" or "Success" states. Use Slate-100 for neutral states.
- **Cards:** White background, Slate-200 border, no shadow. Title should be H3 (Semi-bold, Slate-900).
- **Navigation (Sidebar):** High contrast. Active links should use a Teal vertical accent bar on the left and a subtle background tint.