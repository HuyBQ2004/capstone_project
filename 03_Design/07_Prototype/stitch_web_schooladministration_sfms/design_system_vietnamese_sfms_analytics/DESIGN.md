---
name: Vietnamese SFMS Analytics
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
  secondary: '#5a5f62'
  on-secondary: '#ffffff'
  secondary-container: '#dce0e4'
  on-secondary-container: '#5e6367'
  tertiary: '#44546a'
  on-tertiary: '#ffffff'
  tertiary-container: '#5c6c83'
  on-tertiary-container: '#e3edff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#b9eaff'
  primary-fixed-dim: '#81d1f0'
  on-primary-fixed: '#001f29'
  on-primary-fixed-variant: '#004d62'
  secondary-fixed: '#dfe3e7'
  secondary-fixed-dim: '#c3c7cb'
  on-secondary-fixed: '#171c1f'
  on-secondary-fixed-variant: '#43474b'
  tertiary-fixed: '#d3e4fe'
  tertiary-fixed-dim: '#b7c8e1'
  on-tertiary-fixed: '#0b1c30'
  on-tertiary-fixed-variant: '#38485d'
  background: '#f7f9fb'
  on-background: '#191c1e'
  surface-variant: '#e0e3e5'
  success: '#10B981'
  warning: '#F59E0B'
  danger: '#EF4444'
  info: '#3B82F6'
  border: '#E2E8F0'
typography:
  display:
    fontFamily: Be Vietnam Pro
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 30px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  headline-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  title-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 28px
  title-md:
    fontFamily: Be Vietnam Pro
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
  body-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 26px
  body-md:
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
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  sidebar-width: 280px
  topbar-height: 64px
  container-max: 1440px
  gutter: 24px
  margin-mobile: 16px
  margin-desktop: 32px
  unit: 4px
---

## Brand & Style

The design system is engineered for the **Vietnamese School Food Management System (SFMS)**, specifically tailored for the Analytics & Reporting module. The brand personality is **authoritative, transparent, and highly efficient**. It serves school administrators and nutritionists who require a high-density data environment that remains legible and stress-free.

The visual style is a refined **Corporate Modern** aesthetic, heavily influenced by the "New York" style of Shadcn/UI. It prioritizes clarity and precision, utilizing a "system-first" approach where the interface recedes to let data and insights take center stage. The atmosphere is professional and institutional, evoking a sense of trust and meticulous care in student nutrition management.

## Colors

The palette is anchored by **Deep Teal (#0E7490)**, providing a sophisticated and stable primary anchor that differentiates the system from generic corporate blues. 

- **Primary:** Used for key actions, active navigation states, and primary data highlights.
- **Surface/Neutral:** The background uses a very light cool gray (#F8FAFC) to reduce eye strain during long reporting sessions.
- **Accent:** A soft slate (#F1F5F9) is reserved for secondary buttons, ghost states, and subtle section nesting.
- **Data States:** Semantic colors follow industry standards (Green/Amber/Red/Blue) but are calibrated for high legibility against white surfaces, ensuring accessibility in high-density tables.

## Typography

The design system utilizes **Be Vietnam Pro** exclusively to provide a contemporary, geometric feel that supports full Vietnamese diacritics with perfect balance. 

For the Analytics module, typography is used to create a clear hierarchy within complex data views. Headlines use tighter letter spacing and heavier weights to anchor pages, while body text (14px) is optimized for tabular data density. Small labels (11px-12px) use increased letter spacing and medium weights for micro-copy and table headers to maintain readability at small scales.

## Layout & Spacing

This design system employs a **Fixed Grid** model for administrative clarity. 

- **Navigation:** A fixed sidebar (280px) sits on the left, featuring nested menu items with a 4px indentation scale.
- **Top Bar:** A 64px tall utility bar provides global search, notifications, and profile management.
- **Content Area:** A spacious central canvas with 32px of internal padding. For large reports, the grid utilizes a 12-column system to align data cards and charts.
- **Rhythm:** Spacing follows a 4px/8px baseline grid. Components like cards and tables are separated by 24px (6 units) to allow the data-heavy interface to "breathe."

## Elevation & Depth

To maintain the clean "New York" aesthetic, the design system avoids heavy shadows. Instead, it utilizes **Tonal Layers** and **Low-contrast Outlines**:

- **Level 0 (Background):** The Surface color (#F8FAFC).
- **Level 1 (Cards/Sidebar):** White (#FFFFFF) surfaces with a 1px solid border (#E2E8F0).
- **Interactive Elevation:** Subtle, diffused ambient shadows (0px 1px 3px rgba(0,0,0,0.05)) are used only for active dropdowns, popovers, and modals to separate them from the primary interface.
- **Focus States:** High-visibility 2px rings using the Primary Deep Teal color with a 2px offset.

## Shapes

The design system adheres to a consistent **8px (0.5rem)** radius for all primary UI components, such as buttons, input fields, and cards. This creates a balanced, approachable but professional look. 

- Small components (Checkboxes, Tags): 4px (Soft).
- Large containers (Modals, Large Dashboard Cards): 12px.
- Navigation active-state indicators: 6px.

## Components

### Buttons
- **Primary:** Solid Deep Teal with white text. High-contrast, 8px radius.
- **Secondary:** Surface slate background with Deep Teal text.
- **Ghost:** No background, slate text, appears on hover for low-priority actions in tables.

### Data Tables
- **Header:** 12px Bold, uppercase, slate text with a light gray bottom border.
- **Rows:** 14px text, alternating subtle zebra striping (#F8FAFC) only in very large datasets; otherwise, use simple 1px dividers.
- **Cells:** Vertical padding of 12px for high density, 16px for comfortable reading.

### Input Fields & Controls
- **Fields:** 1px slate border, 8px radius. Use a 2px Deep Teal border for focus.
- **Checkboxes:** Square with a 4px radius, filling with Deep Teal on selection.

### Cards & Analytics
- **Summary Cards:** White background, 1px border. Contain large "Value" typography (Headline-lg) and a small trend indicator (Success/Danger text).
- **Charts:** Use a custom-calibrated palette of Primary Deep Teal, Info Blue, and Slate for categorical data.

### Navigation
- **Sidebar Items:** Clear 14px medium-weight text. Active state uses a Primary color left-edge "pill" and a light slate background tint.