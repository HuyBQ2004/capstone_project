---
name: Administrative Precision
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
  tertiary: '#4e5356'
  on-tertiary: '#ffffff'
  tertiary-container: '#666b6f'
  on-tertiary-container: '#e8edf1'
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
  tertiary-fixed: '#dfe3e7'
  tertiary-fixed-dim: '#c3c7cb'
  on-tertiary-fixed: '#171c1f'
  on-tertiary-fixed-variant: '#43474b'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  headline-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
  headline-md:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.02em
  headline-sm:
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
  mono-sm:
    fontFamily: monospace
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
  container-max: 1440px
  gutter: 1rem
  margin-mobile: 1rem
  margin-desktop: 2rem
  stack-xs: 0.25rem
  stack-sm: 0.5rem
  stack-md: 1rem
  stack-lg: 1.5rem
---

## Brand & Style
The design system is engineered for the Professional School Food Management System (SFMS), prioritizing clarity, efficiency, and administrative rigor. The target audience includes school administrators and nutrition directors who require high-density information display and precise control over complex logistics.

The aesthetic follows a "Modern Corporate" style, heavily influenced by the "New York" variant of high-end component libraries. It features a clean, high-contrast interface with a white-label professional feel. The emotional response is one of reliability and systematic order, achieved through ample whitespace, crisp borders, and a rigorous adherence to a structured grid.

## Colors
The color palette is anchored by a deep Cyan (#0E7490), chosen for its association with health, cleanliness, and professionalism.

- **Primary:** Used for actionable elements, active states, and primary navigation.
- **Secondary:** A neutral slate used for iconography, supporting text, and subtle UI borders.
- **Backgrounds:** Pure white (#FFFFFF) for primary surfaces, with an off-white (#F8FAFC) for page backgrounds to provide subtle contrast for cards and containers.
- **Accents:** Semantic colors are strictly enforced—Success (Emerald), Destructive (Rose), and Warning (Amber)—to indicate system status without ambiguity.

## Typography
This design system utilizes **Be Vietnam Pro** across all levels to maintain a contemporary and approachable yet professional tone. The typographic scale is tightened for administrative density.

Headlines use semi-bold and bold weights with slight negative letter-spacing to appear more disciplined. Body text is optimized for legibility in data-heavy environments, utilizing a 14px base for standard administrative views and 16px for prose or focused settings. Label styles are used for form headers and button text to provide a clear distinction from content.

## Layout & Spacing
The system employs a **fixed-fluid hybrid grid**. Content is contained within a 1440px max-width container, centered on the screen.

- **Desktop (1280px+):** 12-column grid with 24px gutters.
- **Tablet (768px - 1279px):** 8-column grid with 16px gutters.
- **Mobile (< 768px):** 4-column grid with 16px gutters and 16px side margins.

A strict 4px baseline shift is used for all internal component spacing (padding/margins). Administrative tables should use "Condensed" vertical padding (8px) to maximize data visibility without sacrificing touch targets.

## Elevation & Depth
The design system utilizes **Tonal Layering** and **Low-Contrast Outlines** rather than heavy shadows. 

Depth is communicated through a layering stack:
1.  **Background (Level 0):** #F8FAFC - The base canvas.
2.  **Cards/Surfaces (Level 1):** #FFFFFF - Primary content areas with a 1px solid #E2E8F0 border.
3.  **Popovers/Modals (Level 2):** #FFFFFF - Elevated with a very soft, diffused shadow (0 10px 15px -3px rgba(0, 0, 0, 0.1)) and a 1px border.

Interaction states (hover) are indicated by subtle background shifts (e.g., #F1F5F9) rather than movement or elevation changes.

## Shapes
Following the requested 8px (ROUND_EIGHT) standard, the shape language is consistently rounded to provide a modern, "soft-corporate" feel that balances the high-contrast professional lines.

- **Standard Elements:** 0.5rem (8px) for buttons, input fields, and small cards.
- **Large Elements:** 1rem (16px) for main dashboard containers or modal windows.
- **Circular:** Used exclusively for user avatars or icon buttons.

## Components
- **Buttons:** Primary buttons use a solid #0E7490 fill with white text. Secondary buttons use a ghost style (transparent background) with a 1px border.
- **Badges:** Essential for SFMS status tracking.
    - *Draft:* Slate background, Slate text.
    - *Active:* Emerald background (low opacity), Emerald text.
    - *Closed:* Amber background (low opacity), Amber text.
    - *Archived:* Zinc background, Zinc text.
- **Tables:** Professional grade with a sticky header. Rows feature a 1px bottom border (#F1F5F9). Alternate row striping is not used; instead, use a hover state highlight (#F8FAFC).
- **Form Fields:** Inputs must include a clear 1px border that darkens on focus. Labels are always positioned above the input in `label-sm` bold.
- **Cards:** White background, 8px corner radius, 1px slate-200 border. No shadow unless the card is interactive or floating.
- **Data Grids:** Use high-density layouts for inventory and menu management, utilizing monospaced fonts for numerical values to ensure column alignment.