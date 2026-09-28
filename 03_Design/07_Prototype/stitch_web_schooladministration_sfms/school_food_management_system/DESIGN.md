---
name: School Food Management System
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
  secondary: '#5c5f61'
  on-secondary: '#ffffff'
  secondary-container: '#e0e3e5'
  on-secondary-container: '#626567'
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
  secondary-fixed: '#e0e3e5'
  secondary-fixed-dim: '#c4c7c9'
  on-secondary-fixed: '#191c1e'
  on-secondary-fixed-variant: '#444749'
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
  table-header:
    fontFamily: Be Vietnam Pro
    fontSize: 13px
    fontWeight: '600'
    lineHeight: 18px
  label:
    fontFamily: Be Vietnam Pro
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
  h1-mobile:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
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
  container-max: 1440px
  sidebar-width: 260px
  gutter: 1rem
  section-gap: 1.5rem
  edge-margin: 2rem
---

## Brand & Style
The brand personality of the design system is professional, efficient, and dependable. As a tool for school administrators, it prioritizes clarity and high data density without feeling overwhelming. The aesthetic is inspired by the "New York" style of modern UI libraries: a clean, utilitarian foundation characterized by subtle borders, ample whitespace, and a monochromatic base punctuated by a strong primary color.

The emotional response should be one of "controlled precision." The interface disappears to let the data lead, ensuring that school staff can manage nutrition, inventory, and student records with minimal cognitive load. The style is **Corporate / Modern**, leaning into a refined minimalism that feels both institutional and contemporary.

## Colors
This design system utilizes a structured palette designed for administrative clarity. 

- **Primary (#0E7490):** Used for primary actions, active navigation states, and branding elements. It conveys trust and authority.
- **Secondary/Background (#F8FAFC):** This light slate shade is the foundation for the application canvas, providing a soft contrast against white cards.
- **Surface Colors:** Pure white (#FFFFFF) is reserved for interactive cards, modals, and input fields to create a clear "layer" above the background.
- **Semantic Colors:** Red, Yellow, and Green are used strictly for status indicators (e.g., low stock, pending payments, or successful meal distribution).
- **Neutral/Border:** A range of slates (from #0F172A for text to #E2E8F0 for borders) ensures the UI remains grounded and legible.

## Typography
The system uses **Be Vietnam Pro** for all roles. This typeface was selected for its exceptional legibility in the Vietnamese language, specifically handling diacritics gracefully at smaller sizes common in data tables.

- **Headlines:** Use Bold or SemiBold weights with slight negative letter-spacing to create a compact, modern feel.
- **Body Text:** Standardized at 14px for most administrative tasks to allow for higher information density.
- **Data Tables:** Headers use a slightly smaller, semi-bold, uppercase style to differentiate them from the row content.
- **Language Support:** All styles are optimized for Vietnamese character sets, ensuring line heights accommodate tone marks without clipping.

## Layout & Spacing
The design system employs a **Fixed Sidebar + Fluid Content** layout model. 

- **Sidebar:** A permanent left-hand fixture (260px width) handles top-level navigation. It uses a light-gray background (#F1F5F9) to subtly separate it from the main workspace.
- **Grid:** The main content area follows a 12-column fluid grid system but is often organized into "Sections" or "Cards" that span 4, 6, or 12 columns.
- **Spacing Rhythm:** Based on an 8px (0.5rem) scale. Standard inner-card padding is 24px (1.5rem), while the gap between dashboard widgets is 16px (1rem).
- **Responsive Behavior:** 
  - **Desktop (>1024px):** Sidebar is expanded.
  - **Tablet (768px - 1024px):** Sidebar collapses to icons only or becomes a hidden drawer.
  - **Mobile (<768px):** Margins reduce to 16px; cards stack vertically.

## Elevation & Depth
This design system avoids heavy shadows and skeuomorphism, opting for **Tonal Layers** and **Low-Contrast Outlines**.

- **Level 0 (Background):** The Slate-50 background (#F8FAFC) is the lowest layer.
- **Level 1 (Cards/Surface):** White surfaces feature a 1px border (#E2E8F0). A very soft, diffused shadow (0px 1px 3px rgba(0,0,0,0.1)) is used only to provide a subtle lift.
- **Level 2 (Modals/Popovers):** These use a slightly more pronounced shadow (0px 10px 15px -3px rgba(0,0,0,0.1)) to indicate they are floating above the main interface.
- **Active States:** Elements being interacted with (like a clicked button) may lose their shadow to appear "pressed" into the surface.

## Shapes
The shape language is consistent and "Soft-Squared." 

- **Standard Radius:** 8px (0.5rem) is applied to all primary components including buttons, input fields, and cards.
- **Small Components:** Checkboxes and tags use a 4px radius for a sharper, more precise look.
- **Consistency:** Avoid pill-shaped buttons; the 8px radius maintains the "New York" architectural feel of the design system.

## Components
Consistent implementation of these components ensures the system feels unified:

- **Buttons:** 
  - *Primary:* Solid #0E7490 with white text.
  - *Outline:* 1px border (#E2E8F0) with primary color text.
  - *Destructive:* Solid #EF4444 for deletions/alerts.
- **Tables:** Essential for "sfms." Use a white background, 1px horizontal borders (#F1F5F9), and a hover state that highlights the entire row in #F8FAFC. Headers should be sticky.
- **Status Badges (Chips):** Use a "soft" style—a light tint of the semantic color for the background with the dark saturated version for text (e.g., Success: Light Green bg, Dark Green text).
- **Input Fields:** 8px radius, 1px border (#E2E8F0). On focus, the border changes to the primary color with a subtle 2px outer ring of the same color at 20% opacity.
- **Sidebar Items:** Active states use a small vertical bar (4px width) of the primary color on the far left of the menu item and a subtle background tint.
- **Cards:** Always include a header section with a title in `h3` or `body-lg` (SemiBold) and a separator line before the content body.