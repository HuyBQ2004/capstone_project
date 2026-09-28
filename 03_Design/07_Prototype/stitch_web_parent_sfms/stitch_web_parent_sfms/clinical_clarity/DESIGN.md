---
name: Clinical Clarity
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
  secondary: '#576065'
  on-secondary: '#ffffff'
  secondary-container: '#dbe4ea'
  on-secondary-container: '#5d666b'
  tertiary: '#764900'
  on-tertiary: '#ffffff'
  tertiary-container: '#965f00'
  on-tertiary-container: '#ffe9d4'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#b9eaff'
  primary-fixed-dim: '#81d1f0'
  on-primary-fixed: '#001f29'
  on-primary-fixed-variant: '#004d62'
  secondary-fixed: '#dbe4ea'
  secondary-fixed-dim: '#bfc8ce'
  on-secondary-fixed: '#141d21'
  on-secondary-fixed-variant: '#3f484d'
  tertiary-fixed: '#ffddb8'
  tertiary-fixed-dim: '#ffb95f'
  on-tertiary-fixed: '#2a1700'
  on-tertiary-fixed-variant: '#653e00'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  display-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
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
  label-bold:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
  label-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
  headline-lg-mobile:
    fontFamily: Be Vietnam Pro
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
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
  2xl: 48px
  gutter: 16px
  margin-mobile: 16px
  margin-desktop: 64px
---

## Brand & Style
The design system for SFMS focuses on building parental trust through a clinical, hygienic, and highly structured visual language. The target audience is parents who require immediate, unambiguous information regarding their children's nutrition and safety.

The style is **Corporate / Modern** with a lean toward **Minimalism**. It prioritizes high legibility and a sense of "medical-grade" reliability. Every element is designed to evoke a feeling of safety, cleanliness, and professional oversight, ensuring that health-critical information—such as allergens—is never obscured by decorative elements.

## Colors
The palette is anchored by a medical teal-blue (`#0E7490`), symbolizing hygiene and professional care. 

- **Primary**: Used for core branding, primary actions, and headers.
- **Secondary**: A very light blue tint used for subtle backgrounds and grouping related content without adding visual weight.
- **Neutral**: Slate tones are used for text and iconography to maintain a soft but high-contrast readability.
- **Functional Colors**: 
    - **Warning**: Specifically utilized for allergen alerts and dietary restrictions. It uses a high-visibility amber to ensure parent attention.
    - **Success/Error**: Standardized green and red for form validations and meal booking confirmations.

## Typography
This design system utilizes **Be Vietnam Pro** across all levels to maintain a contemporary, friendly yet professional tone. 

The type scale is generous to accommodate detailed nutritional information. For medical and allergen warnings, `label-bold` should be used with high-contrast color backgrounds. `body-lg` is preferred for meal descriptions to ensure they are easily skimmable for busy parents on mobile devices.

## Layout & Spacing
The design employs a **fluid grid system** to ensure parents can manage food schedules on both mobile phones and tablets.

- **Mobile**: 4-column grid with 16px margins.
- **Desktop**: 12-column grid with a max-width of 1280px.
- **Rhythm**: A 4px baseline grid governs all spacing. Components like meal cards use `lg` (24px) padding to create a breathable, "clean" aesthetic. Elements are grouped using `sm` (8px) gaps to maintain a clear relationship between labels and data.

## Elevation & Depth
To maintain a professional and sterile look, the design system avoids heavy shadows. Instead, it uses:

1.  **Low-contrast Outlines**: Cards and input fields use a 1px border (`#E2E8F0`).
2.  **Tonal Layering**: The main background uses a very faint gray (`#F8FAFC`), while interactive cards and containers use pure white (`#FFFFFF`) to appear "lifted."
3.  **Subtle Depth**: Only primary action buttons and active modal overlays use a soft, highly diffused shadow (0px 4px 12px, 5% opacity) to signify interactability without cluttering the interface.

## Shapes
The design system uses a consistent **8px (0.5rem)** corner radius for most UI elements. This "Rounded" setting balances the professional nature of medical data with the approachability required for a school-related product.

- **Standard Elements**: Buttons, Input fields, and Small cards use 8px.
- **Large Containers**: Main content areas or featured meal cards use `rounded-lg` (16px).
- **Status Tags**: Small badges (e.g., "Nut-free", "Paid") use `rounded-xl` (24px) or full pill-shape to distinguish them from actionable buttons.

## Components
- **Buttons**: Primary buttons are solid `#0E7490` with white text. Secondary buttons use an outline style. All buttons have a height of 48px for easy touch targets on mobile.
- **Allergen Chips**: High-visibility tags using `warning_allergy` background with dark text. These must include a warning icon (e.g., an exclamation triangle).
- **Input Fields**: Labeled clearly above the field. Errors are signaled by a 2px red border and a sub-label in `label-sm` font size.
- **Meal Cards**: White background, 1px border, 8px rounded corners. Contains a clear hierarchy: Image (top), Title (Bold), Nutritional Summary (Body), and Action (Bottom).
- **Status Indicators**: For "Payment Success" or "Order Error," use full-width banners at the top of the viewport or centered modals with clear iconography.
- **Health Lists**: For listing medical history or dietary needs, use a "List Group" with `sm` spacing and subtle horizontal dividers to keep the information scan-friendly.