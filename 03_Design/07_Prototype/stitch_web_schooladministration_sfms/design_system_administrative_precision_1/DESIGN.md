---
name: Administrative Precision
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
  body-sm:
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
  code:
    fontFamily: monospace
    fontSize: 14px
    fontWeight: '400'
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
  container-padding: 24px
  gutter: 16px
  section-gap: 32px
  component-padding-x: 12px
  component-padding-y: 8px
---

## Brand & Style

This design system is engineered for the Vietnamese School Food Management System, specifically the Student Management Module. The aesthetic follows a **Modern Corporate** direction, heavily inspired by the "New York" style of shadcn/ui. The goal is to provide a high-density, professional environment that minimizes cognitive load for administrative staff.

The visual narrative is built on three pillars:
1.  **Clarity:** Systematic layouts with rigorous alignment and intentional whitespace.
2.  **Efficiency:** High-information density without visual clutter, utilizing subtle borders and tonal shifts.
3.  **Trust:** A stable, teal-centric palette that communicates reliability and institutional authority.

The interface prioritizes utility and speed, ensuring that complex data entry and student record management feel effortless and structured.

## Colors

The palette is anchored by **Deep Teal (#0E7490)**, a color that suggests professionalism and calm. 

- **Primary:** Used for key actions, active states, and primary navigation highlights.
- **Surface & Background:** The background remains pure white to maintain a "paper" feel, while `#F8FAFC` (Slate 50) is used for layout containers, sidebar backgrounds, and table headers to provide subtle structural separation.
- **System States:** Standard semantic colors are used for status indicators (e.g., meal plan status, payment alerts). These should be used with low-opacity backgrounds (10-15%) for tags/badges to ensure text legibility.
- **Neutral Scale:** Utilizes the Slate palette for text and borders to maintain a cool, contemporary temperature throughout the UI.

## Typography

The typography system uses **Be Vietnam Pro** exclusively to provide a contemporary, approachable, yet professional feel. It is optimized for legibility in data-heavy environments.

- **Headlines:** Use a tighter letter-spacing and heavier weights to establish clear hierarchy.
- **Body Text:** The standard size is `14px` (body-sm) for data tables and forms to allow for higher information density, while `16px` is reserved for marketing or empty state descriptions.
- **Labels:** Semi-bold weights are used for form labels and table headers to distinguish them from dynamic data.
- **Vietnamese Support:** Special attention is paid to line-heights to ensure Vietnamese diacritics are never clipped, even in tight vertical spaces like buttons and tags.

## Layout & Spacing

This design system utilizes a **Fixed-Fluid Hybrid** layout. 
- **Side Navigation:** Fixed at 280px for desktop, providing a persistent structural anchor.
- **Main Content:** A fluid container with a maximum width of 1440px to prevent excessive line lengths on ultra-wide monitors.
- **Grid:** A 12-column grid system is used for dashboard layouts. 
- **Spacing Rhythm:** Based on a 4px scale. Components typically use 8px (sm) or 16px (md) internal padding.
- **Mobile Adaptivity:** On mobile devices, the sidebar collapses into a sheet (drawer), and container padding reduces from 24px to 16px. Tables transition to card-based layouts or enable horizontal scrolling with fixed first columns.

## Elevation & Depth

In alignment with the "New York" style, the design system avoids heavy shadows, opting instead for **Tonal Layers** and **Low-Contrast Outlines**.

- **Level 0 (Background):** Pure white (#FFFFFF).
- **Level 1 (Card/Surface):** White background with a 1px border of Slate-200 (#E2E8F0). No shadow.
- **Level 2 (Popovers/Modals):** White background with a subtle, diffused shadow: `0 10px 15px -3px rgba(0, 0, 0, 0.1)`.
- **Interactive States:** Hovering over interactive elements (like table rows or menu items) should trigger a background shift to Slate-50 (#F8FAFC) rather than a shadow increase. 

This "flat-plus" approach ensures the UI feels lightweight and modern.

## Shapes

The shape language is strictly defined to maintain a geometric and professional appearance.

- **Standard Radius:** All buttons, input fields, and cards utilize a **8px (0.5rem)** corner radius. This balance provides a modern touch without appearing overly "bubbly" or casual.
- **Small Elements:** Checkboxes and small tags use a **4px (0.25rem)** radius to maintain visual weight.
- **Search Bars:** May optionally use a fully rounded (pill) shape to differentiate global search from form inputs.

## Components

### Buttons
- **Primary:** Solid #0E7490 with white text.
- **Secondary:** Slate-100 background with Slate-900 text.
- **Ghost:** No background, Slate-600 text; appears on hover with Slate-100 background.
- **Sizes:** Standard height is 40px; small buttons for table actions are 32px.

### Tables (The Core of SFMS)
- **Header:** Slate-50 background, semi-bold text, 1px bottom border.
- **Rows:** 1px bottom border (#E2E8F0). Hover state: #F8FAFC.
- **Density:** "Default" (12px vertical padding) and "Compact" (8px vertical padding) modes.

### Forms
- **Inputs:** 1px border (#E2E8F0), 8px radius. Active/Focus state uses a 2px ring of Primary color with an offset.
- **Steppers:** Horizontal lines for desktop, vertical for mobile. Active steps use the Primary Teal; completed steps use Success Green.

### Navigation
- **Sidebar:** Clean list of icons + labels. Active state uses a subtle background (#F1F5F9) and a 2px vertical primary-color bar on the left edge.
- **Breadcrumbs:** Used on all sub-pages for clear path-finding within the student management hierarchy.

### Cards
- Simple 1px border, 8px radius, no shadow. Used to group related student information (e.g., Health Records, Guardian Info).