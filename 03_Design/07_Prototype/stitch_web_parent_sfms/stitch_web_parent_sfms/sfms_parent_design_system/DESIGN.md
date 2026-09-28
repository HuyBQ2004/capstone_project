---
name: SFMS Parent Design System
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
  secondary: '#006877'
  on-secondary: '#ffffff'
  secondary-container: '#3fe1fd'
  on-secondary-container: '#00616f'
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
  secondary-fixed: '#a2eeff'
  secondary-fixed-dim: '#2fd9f4'
  on-secondary-fixed: '#001f25'
  on-secondary-fixed-variant: '#004e5a'
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
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 60px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
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
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
  data-mono:
    fontFamily: monospace
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
  base: 8px
  xs: 4px
  sm: 12px
  md: 16px
  lg: 24px
  xl: 32px
  container-max: 1200px
  gutter: 16px
---

## Brand & Style
The brand personality is rooted in trust, clinical precision, and parental reassurance. This design system serves parents who require absolute transparency regarding their children's nutritional intake and food safety. The emotional response should be one of "calm confidence"—knowing that every meal is tracked, verified, and safe.

The design style utilizes a **Modern Corporate** approach with a heavy emphasis on **Minimalism**. By prioritizing white space and a structured hierarchy, the UI minimizes cognitive load for busy parents. High-quality iconography and clear data visualizations are used to communicate complex nutritional data at a glance, ensuring that "transparency" is not just a value, but a functional attribute of the interface.

## Colors
The palette is centered around a professional medical teal (#0E7490), chosen for its association with health, cleanliness, and institutional stability.

- **Primary (#0E7490):** Used for primary actions, brand presence, and key semantic indicators related to food safety certification.
- **Secondary (#22D3EE):** A lighter cyan used for data visualization highlights (e.g., vitamin levels) and soft UI accents.
- **Surface/Tertiary (#F8FAFC):** A cool-toned off-white used for page backgrounds to reduce eye strain and maintain a sterile, clean environment.
- **Neutral (#64748B):** A slate grey used for secondary text and supportive UI elements like borders and inactive icons.
- **Semantic Colors:** Green (Success/Safe), Amber (Warning/Allergen), and Red (Danger/Contamination) must be used strictly for food safety status.

## Typography
This design system utilizes **Be Vietnam Pro** across all levels to maintain a friendly yet contemporary and professional feel. 

- **Headlines:** Use Bold (700) or SemiBold (600) weights to establish a clear information hierarchy. Large display sizes should use slight negative letter spacing for a more compact, modern look.
- **Body Text:** Use Regular (400) weight for maximum readability. Line heights are purposefully generous (1.5x font size) to ensure that dense nutritional information remains legible.
- **Data Display:** For calorie counts, protein grams, and safety timestamps, consider using a high-legibility weight to ensure these critical figures stand out.

## Layout & Spacing
The spacing rhythm is built on an **8px grid system**, ensuring mathematical harmony across all components.

- **Grid System:** A 12-column fluid grid is used for desktop, 8-column for tablet, and a 4-column fluid grid for mobile.
- **Safe Zones:** Mobile screens must maintain a minimum 16px lateral margin.
- **Information Density:** Use 'lg' (24px) or 'xl' (32px) vertical spacing between major sections (e.g., separating "Meal Summary" from "Safety Certifications") to prevent the UI from feeling cluttered. Smaller 8px or 12px gaps should be used to group related data points together.

## Elevation & Depth
Depth is communicated through **Tonal Layering** and **Subtle Ambient Shadows**. The goal is to make the interface feel organized and clinical, not overly "designed."

- **Level 0 (Background):** #F8FAFC.
- **Level 1 (Cards/Containers):** Pure white (#FFFFFF) with a very soft, diffused shadow (0px 4px 12px rgba(14, 116, 144, 0.05)).
- **Level 2 (Modals/Dropdowns):** Pure white with a more defined shadow (0px 8px 24px rgba(14, 116, 144, 0.1)).
- **Outlines:** Use low-contrast borders (1px solid #E2E8F0) for secondary elements to define boundaries without adding visual weight.

## Shapes
In accordance with the "Round 8" requirement, the shape language uses a **Rounded** philosophy.

- **Standard Components:** Buttons, Input Fields, and Cards use a **0.5rem (8px)** corner radius.
- **Large Containers:** Modals and bottom sheets use a **1rem (16px)** radius to soften the presentation of significant information blocks.
- **Small Elements:** Checkboxes and status tags use a **0.25rem (4px)** radius to maintain a crisp look at small scales.

## Components
- **Buttons:** Primary buttons use the #0E7490 background with white text. Secondary buttons use a teal outline with a transparent background. 8px corner radius is mandatory.
- **Safety Tags (Chips):** Small badges used to denote "Organic," "Halal," or "Nut-Free." These use a light tint of the semantic color (e.g., light green background with dark green text) and 4px corner radius.
- **Nutritional Cards:** Elevated white cards containing progress bars for daily intake. Use high-contrast text for the most important metrics (e.g., Calories).
- **Input Fields:** 8px rounded corners with a 1px border. On focus, the border transitions to Primary Teal with a subtle glow.
- **Transparency Indicators:** Specialized list items that show the source of ingredients, utilizing a "Verified" icon (shield or checkmark) in the primary color.
- **Alerts:** Full-width banners for critical safety notices (e.g., allergy warnings) using high-contrast bold colors.