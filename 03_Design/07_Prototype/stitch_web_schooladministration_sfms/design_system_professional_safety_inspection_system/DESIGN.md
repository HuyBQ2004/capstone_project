---
name: Professional Safety & Inspection System
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
  body-base:
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
  component-padding-x: 1rem
  component-padding-y: 0.5rem
---

## Brand & Style
The design system for the Incident Management & Inspection module is built on the pillars of **reliability, clarity, and rapid response**. Given the critical nature of safety inspections, the UI minimizes cognitive load through a refined "Modern Corporate" aesthetic inspired by shadcn/ui. 

The style prioritizes functional density and information hierarchy. It uses a clean, systematic approach with subtle borders, generous whitespace, and high-contrast typography to ensure that safety officers and inspectors can identify risks and status updates at a glance. The emotional response is one of calm authority and structured efficiency.

## Colors
This design system utilizes a palette centered around the primary Teal-700 (`#0E7490`) to convey stability and professionalism. 

- **Primary:** Used for main actions, active states, and branding.
- **Status Tokens (Severity):** 
  - **High:** Red-600 (`#DC2626`) for immediate hazards.
  - **Medium:** Amber-500 (`#F59E0B`) for cautionary items.
  - **Low:** Emerald-600 (`#059669`) for minor observations.
- **Investigation States:** 
  - **Active:** Blue-500 for ongoing inspections.
  - **Investigating:** Violet-500 for deep-dive analysis.
  - **Closed:** Slate-600 for resolved/archived records.
- **Neutral:** A range of Slate grays from 50 to 950 is used for surfaces, borders, and text to maintain a clean, shadcn-like interface.

## Typography
The system uses **Be Vietnam Pro** exclusively to provide a contemporary and legible feel. 
- **Headlines:** Use semi-bold and bold weights with tighter letter spacing for a professional, "newsreader" impact on dashboards.
- **Body:** Standardized on 14px (sm) for data-heavy tables and 16px (base) for descriptive content.
- **Data Labels:** Medium weights (500) are used for form labels and table headers to distinguish them from input data.

## Layout & Spacing
The layout follows a **fluid-to-fixed grid** hybrid. 
- **Desktop:** A 12-column grid with a max-width of 1440px for content areas.
- **Gaps:** A base 4px (0.25rem) scaling system is used. Most components utilize 16px (1rem) spacing between elements.
- **Navigation:** A fixed sidebar (256px) for module navigation, with a fluid content area that reflows based on screen width.
- **Mobile:** Margins shrink to 16px, and multi-column forms collapse into a single-column stack to maintain focus on the inspection inputs.

## Elevation & Depth
In alignment with the shadcn/ui philosophy, depth is achieved through **low-contrast outlines** and subtle tonal layering rather than heavy shadows.
- **Level 0 (Base):** Background color (Slate-50) for the main application area.
- **Level 1 (Card):** White background with a 1px border (Slate-200).
- **Level 2 (Popovers/Modals):** White background with a very soft, diffused shadow (`0 10px 15px -3px rgb(0 0 0 / 0.1)`) and a 1px border.
- **Interactive:** Hover states on interactive elements use a subtle gray background shift rather than elevation change.

## Shapes
The shape language is strictly geometric and consistent. 
- **Standard Radius:** All buttons, input fields, and cards use an **8px (0.5rem)** corner radius to balance approachability with professional structure.
- **Small Elements:** Checkboxes and small tags use a 4px radius.
- **Status Pills:** Severity and investigation badges use a fully rounded (pill) shape to distinguish them from interactive buttons.

## Components
- **Buttons:** Primary buttons use the `#0E7490` fill with white text. Ghost buttons are used for secondary actions to maintain hierarchy.
- **Status Badges:** 
    - *Severity:* Use a light tinted background (10% opacity) with a solid 1px border and high-contrast text of the same hue (e.g., Red-600 for High).
    - *Investigation:* Use solid colors for "Active" and "Investigating," and a neutral Slate for "Closed."
- **Input Fields:** 1px solid border (Slate-200) that shifts to Primary (`#0E7490`) on focus with a subtle ring offset.
- **Data Tables:** Clean rows with 1px border-bottom. High-severity incidents should have a subtle 4px left-border accent in Red-600 for immediate visual scanning.
- **Inspection Cards:** Grouped information with clear labels and 8px rounded corners, using whitespace to separate different inspection sections.