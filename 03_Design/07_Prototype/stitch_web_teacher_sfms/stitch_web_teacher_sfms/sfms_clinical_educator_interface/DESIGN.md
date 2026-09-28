---
name: SFMS Clinical Educator Interface
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#3f484c'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#6f787d'
  outline-variant: '#bec8cd'
  surface-tint: '#006781'
  primary: '#005a71'
  on-primary: '#ffffff'
  primary-container: '#0e7490'
  on-primary-container: '#d3f1ff'
  inverse-primary: '#81d1f0'
  secondary: '#545f73'
  on-secondary: '#ffffff'
  secondary-container: '#d5e0f8'
  on-secondary-container: '#586377'
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
  secondary-fixed: '#d8e3fb'
  secondary-fixed-dim: '#bcc7de'
  on-secondary-fixed: '#111c2d'
  on-secondary-fixed-variant: '#3c475a'
  tertiary-fixed: '#e0e3e5'
  tertiary-fixed-dim: '#c4c7c9'
  on-tertiary-fixed: '#191c1e'
  on-tertiary-fixed-variant: '#444749'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
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
    lineHeight: 36px
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
  unit: 4px
  container-padding: 24px
  gutter: 16px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
  max-width-desktop: 1440px
---

## Brand & Style
The design system is engineered for the SFMS Teacher Web App, prioritizing clinical precision, administrative efficiency, and trust. The visual language is rooted in **Corporate Modernism** with a specialized focus on healthcare and student wellbeing.

The UI targets educators and school medical staff, requiring a high level of legibility and a calm emotional response. By utilizing expansive whitespace and a structured information hierarchy, the design system minimizes cognitive load during high-stakes health monitoring (e.g., managing allergies and special diets). The aesthetic is "Clinical-Clean"—avoiding unnecessary decoration in favor of functional clarity and professional reliability.

## Colors
The palette is anchored by a sophisticated **Teal (#0E7490)**, selected for its association with health, hygiene, and technological precision. 

- **Primary:** Used for key actions, active states, and navigational accents.
- **Deep Navy (#1E293B):** Reserved for high-contrast headings to ensure authoritative readability.
- **Neutral Grays:** A range of slate and cool grays provides soft body text and UI borders without the harshness of pure black.
- **Health Semantic Colors:**
    - **Success:** Positive health check-ins and normal status.
    - **Warning:** Special diet requirements and non-life-threatening observations.
    - **Danger:** Critical allergy alerts and immediate medical attention required.

## Typography
This design system utilizes **Be Vietnam Pro** across all levels. This font provides excellent support for Vietnamese diacritics and maintains a modern, approachable yet professional feel.

- **Headings:** Use bold weights and Deep Navy to establish a strong hierarchy.
- **Data Tables:** Use a medium weight (`data-tabular`) for cell content to ensure clarity when scanning student names and medical statuses.
- **Labels:** Small, uppercase labels are used for metadata like "LAST UPDATED" or "CLASS SECTION."
- **Line Height:** Generous line heights are maintained (1.5x for body) to facilitate quick reading in busy classroom environments.

## Layout & Spacing
The system follows a **4px base grid** for tight alignment and a **12-column fluid grid** for dashboard views.

- **Content Reflow:** On desktop, the layout utilizes a fixed side navigation (280px). On tablet/mobile, the navigation collapses into a bottom bar or hamburger menu.
- **Data Density:** Tables should use a "Comfortable" vertical padding (16px) by default, with a "Compact" toggle (8px) for high-volume data entry.
- **Margins:** Standard page margins are 24px on desktop, scaling down to 16px on mobile devices.

## Elevation & Depth
Depth is communicated through **Tonal Layers** and extremely subtle **Ambient Shadows**. This approach keeps the interface feeling "flat" and professional, rather than heavy or dated.

- **Level 0 (Background):** Light gray (#F1F5F9). Used for the base canvas.
- **Level 1 (Cards/Tables):** Pure white surface with a 1px border (#E2E8F0) and a soft 4px blur shadow at 5% opacity.
- **Level 2 (Modals/Popovers):** Pure white surface with an 8px blur shadow at 10% opacity.
- **Interactions:** Hover states on rows or buttons should involve a slight tonal shift (e.g., Primary 600 to Primary 700) rather than an increase in shadow depth.

## Shapes
The design system utilizes **Round Eight** (8px/0.5rem) as its core radius. This strikes a balance between the sterile sharpness of traditional medical software and the overly "bubbly" feel of consumer apps.

- **Standard (8px):** Buttons, Input Fields, Cards, and Data Tables.
- **Large (16px):** Modal containers and large metric panels.
- **Full (Pill):** Used exclusively for status badges (e.g., "Dị ứng," "Chế độ ăn đặc biệt") to distinguish them from actionable buttons.

## Components

### Data Tables
Tables are the core of the SFMS experience. 
- **Header:** Sticky headers with a light gray background (#F8FAFC) and subtle bottom border.
- **Rows:** Alternating zebra striping is prohibited; use hover highlights instead.
- **Columns:** Specifically dedicated columns for health alerts using icons + text badges.

### Status Badges (Huy hiệu trạng thái)
- **Allergy (Dị ứng):** Red background (low opacity) with bold red text. Include a warning icon.
- **Special Diet (Chế độ ăn):** Amber background (low opacity) with dark amber text.
- **Normal (Bình thường):** Transparent with a neutral border.

### Metric Cards
Used for quick tallies (e.g., "Students Present").
- **Style:** Subtle shadow, centered typography, and a 4px left-border accent using the Primary Teal color.

### Input Fields
- **State:** 1px neutral border. On focus, the border transitions to Primary Teal with a 2px outer glow (Primary at 20% opacity).
- **Labels:** Vietnamese labels must be positioned above the field, never as placeholder text alone.

### Health Icons
Use medical-themed iconography (crosses, stethoscopes, warning triangles, dietary icons) with a consistent 2px stroke weight. Avoid solid "filled" icons to maintain a lightweight, professional look.