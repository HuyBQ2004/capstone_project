---
name: Clinical Clarity
colors:
  surface: '#f9f9ff'
  surface-dim: '#d3daef'
  surface-bright: '#f9f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f1f3ff'
  surface-container: '#e9edff'
  surface-container-high: '#e1e8fd'
  surface-container-highest: '#dce2f7'
  on-surface: '#141b2b'
  on-surface-variant: '#3f484c'
  inverse-surface: '#293040'
  inverse-on-surface: '#edf0ff'
  outline: '#6f787d'
  outline-variant: '#bec8cd'
  surface-tint: '#006781'
  primary: '#005a71'
  on-primary: '#ffffff'
  primary-container: '#0e7490'
  on-primary-container: '#d3f1ff'
  inverse-primary: '#81d1f0'
  secondary: '#21667d'
  on-secondary: '#ffffff'
  secondary-container: '#a5e3fe'
  on-secondary-container: '#22667e'
  tertiary: '#4b5459'
  on-tertiary: '#ffffff'
  tertiary-container: '#636c71'
  on-tertiary-container: '#e5eef4'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#b9eaff'
  primary-fixed-dim: '#81d1f0'
  on-primary-fixed: '#001f29'
  on-primary-fixed-variant: '#004d62'
  secondary-fixed: '#baeaff'
  secondary-fixed-dim: '#91cfea'
  on-secondary-fixed: '#001f29'
  on-secondary-fixed-variant: '#004d62'
  tertiary-fixed: '#dbe4ea'
  tertiary-fixed-dim: '#bfc8ce'
  on-tertiary-fixed: '#141d21'
  on-tertiary-fixed-variant: '#3f484d'
  background: '#f9f9ff'
  on-background: '#141b2b'
  surface-variant: '#dce2f7'
typography:
  headline-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
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
  headline-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
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
  label-md:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
  label-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 12px
    fontWeight: '700'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  gutter-desktop: 24px
  margin-desktop: 40px
  gutter-mobile: 16px
  margin-mobile: 16px
  touch-target: 44px
---

## Brand & Style
The design system is engineered for the SFMS Clinical Educator Interface, prioritizing extreme legibility and rapid information processing in high-activity environments like school canteens. The brand personality is authoritative yet approachable, blending medical precision with pedagogical warmth. 

The aesthetic follows a **Corporate / Modern** direction with a focus on high-contrast accessibility. It utilizes generous white space to reduce cognitive load and clear structural divisions to separate clinical data from educational tracking. The visual tone is stable and reliable, ensuring educators feel supported by a professional-grade tool during busy clinical rotations.

## Colors
The palette is anchored by a deep Teal (#0E7490), chosen for its association with healthcare and its high visibility against white backgrounds. 

- **Primary (#0E7490):** Used for primary actions, active navigation states, and critical clinical indicators.
- **Secondary (#155E75):** A darker shade for hover states and secondary emphasis to maintain contrast ratios.
- **Surface/Tertiary (#F0F9FF):** A very light blue tint used for card backgrounds and section grouping to reduce eye strain without losing structure.
- **Neutral (#111827):** A near-black for typography and borders, ensuring maximum readability in bright, high-glare environments like canteens.
- **Semantic Colors:** Success (Emerald 700), Warning (Amber 600), and Error (Rose 700) must be used with high-contrast icons to assist in color-blind accessibility.

## Typography
This design system utilizes **Be Vietnam Pro** exclusively to provide native support for Vietnamese diacritics while maintaining a modern, professional appearance. 

The type scale is intentionally oversized to accommodate readability in "glance-and-go" scenarios. Body text never drops below 14px, and 16px is the standard for data entry. For Vietnamese text, line-heights are increased by 10-15% compared to standard English presets to ensure that stacked diacritics (e.g., "ễ", "ổ") do not clash with the lines above or below.

## Layout & Spacing
The layout follows a **Fixed Grid** model on desktop (1280px max-width) and a **Fluid** model on mobile devices. 

- **Grid:** A 12-column grid is used for desktop, collapsing to 4 columns on mobile. 
- **Rhythm:** An 8px baseline grid governs all vertical spacing. Elements should be spaced in increments of 8px (8, 16, 24, 32, 48, 64).
- **Touch Targets:** In a canteen setting, educators may be standing or moving. All interactive elements (buttons, checkboxes) must maintain a minimum touch target of 44x44px.
- **Mobile Reflow:** Complex data tables must reflow into "List Cards" on mobile devices to prevent horizontal scrolling and data obfuscation.

## Elevation & Depth
To maintain high contrast and clarity, this design system avoids soft ambient shadows which can appear "muddy" on lower-quality mobile screens or in bright light. 

Instead, depth is conveyed through **Tonal Layers** and **Low-contrast Outlines**:
- **Level 0 (Background):** The base page background is Neutral White (#FFFFFF).
- **Level 1 (Cards/Containers):** Surfaces use a 1px border (#E5E7EB) or the Tertiary tint (#F0F9FF) to define boundaries.
- **Level 2 (Modals/Popovers):** A crisp, high-contrast shadow (Opacity 15%, Blur 10px, Y-Offset 4px) is used only for temporary overlays to separate them from the content beneath.

## Shapes
The shape language is defined by the **8px (0.5rem) base radius**, categorized as "Rounded" in this design system.

- **Standard Elements:** Buttons, Input fields, and Small Cards use the 8px radius.
- **Large Containers:** Modals and Dashboard panels use `rounded-lg` (16px).
- **Status Indicators:** Chips and Tags use a fully rounded/pill shape to distinguish them from interactive buttons.
- **Visual Consistency:** Iconic symbols must be enclosed in rounded-square containers to match the component language.

## Components
- **Buttons:** Primary buttons use a solid #0E7490 fill with white text. Secondary buttons use a #0E7490 border and text. Minimum height is 48px for accessibility.
- **Input Fields:** Use a 1px border (#D1D5DB). Label text must be persistently visible (no floating labels that disappear) to assist in quick data entry. Focus state uses a 2px #0E7490 ring.
- **Chips:** Used for "Student Status" or "Rotation Type." These use light background tints (10% opacity of the semantic color) with high-contrast bold text.
- **Cards:** The primary container for student profiles. Cards should have a 1px #E5E7EB border and no shadow. The header of the card should use the Tertiary tint (#F0F9FF) to create clear visual separation.
- **Checkboxes & Radios:** Scaled to 20x20px with the Primary color used for the checked state. They must be accompanied by a label that is also a hit target.
- **Clinical Data Tables:** High-density rows (40px height) with zebra-striping (using #F9FAFB) to assist the eye in tracking data across wide screens.