# Design System — SFMS (Web: shadcn/ui + Tailwind CSS · Mobile: Material Design 3)

**Trạng thái**: Confirmed (2026-07-01) — thay thế TODO #1 [BLOCKING] "Brand identity / Design Tokens" trong `00_UI_Design_Documentation_Plan.md`.
**Áp dụng cho**: Web (Next.js + shadcn/ui + Tailwind CSS) và Mobile (Flutter + Material Design 3), theo `03_Design/02_ADR/ADR-001.md`.
**Nguồn nghiên cứu**: tài liệu theming chính thức của shadcn/ui (ui.shadcn.com/docs/theming — Tailwind v4 mặc định dùng không gian màu OKLCH), tài liệu ColorScheme/Material 3 của Flutter (api.flutter.dev, docs.flutter.dev), WebAIM/WCAG 2.2 contrast guidelines.

---

## 1. Nguyên tắc nền tảng: 2 hệ thống thiết kế, 1 thương hiệu

SFMS dùng **2 ngôn ngữ thiết kế khác nhau theo nền tảng** (không dùng chung 1 thư viện UI):

| Nền tảng | Hệ thống thiết kế | Cơ chế màu |
|---|---|---|
| Web (Next.js) | **shadcn/ui + Tailwind CSS** | CSS variables định nghĩa thủ công từng token (OKLCH), theo quy ước "New York/Default style" của shadcn |
| Mobile (Flutter) | **Material Design 3** | `ColorScheme.fromSeed(seedColor)` — Flutter **tự sinh** toàn bộ tonal palette (surface, container, on-*) từ 1 màu seed duy nhất |

**Vấn đề cần giải quyết**: hai cơ chế sinh màu khác nhau hoàn toàn (thủ công vs tự động) — nếu không neo chung, Web và Mobile sẽ trôi dạt thành 2 thương hiệu khác nhau theo thời gian.

**Quy tắc bắt buộc để giữ đồng nhất thương hiệu**:
1. **Một màu seed duy nhất** (`#0E7490`, xem mục 2) dùng làm `--primary` bên Web (chuyển sang OKLCH) **và** làm `seedColor` cho `ColorScheme.fromSeed()` bên Mobile. Hai bên sẽ không giống nhau 100% về từng shade (vì Material 3 tự sinh tonal ramp riêng), nhưng cùng "họ màu" và cùng cảm nhận thương hiệu.
2. **Màu ngữ nghĩa (semantic/status — Success/Warning/Danger/Info/Critical, mục 3) là hằng số cố định**, khai báo thủ công ở **cả 2 nền tảng**, KHÔNG để Material 3 tự sinh — vì các màu này mang ý nghĩa nghiệp vụ bắt buộc theo BR (VD BR-123: cảnh báo dị ứng khi quét ID học sinh phải kích hoạt cảnh báo trực quan màu đỏ), tự sinh tonal có thể lệch sắc và phá vỡ ý nghĩa an toàn. *(Sửa 2026-07-01: mã cũ "BR-038" là ghost code từ appendix cũ của Report 3, không tồn tại trong business_rules.md canonical — áp dụng đúng nghị quyết đã có ở `02_Requirements/00_BusinessRules/BR_Citation_Audit_Report.md` mục 5 Bucket B, map sang BR-123.)*
3. Mọi Design Token (màu, font, spacing) chỉ định nghĩa **một lần** trong tài liệu này — Screen Specification và Component Guidelines không được tự ý thêm màu/font mới ngoài bảng dưới đây.

---

## 2. Bảng màu thương hiệu (Brand Palette)

| Token | Vai trò | Hex (nguồn) | Ghi chú |
|---|---|---|---|
| **Primary** | Hành động chính, link, nút CTA, active state | `#0E7490` (Tailwind cyan-700) | Xanh lam-ngọc (teal-blue) — gợi cảm giác vệ sinh/y tế/tin cậy, không trùng với xanh lá "Success" hay đỏ "Danger" bên dưới để tránh mơ hồ ý nghĩa trong app an toàn thực phẩm |
| **Primary-hover/dark** | Hover/pressed state | `#155E75` (cyan-800) | |
| **Secondary** | Hành động phụ, badge trung tính | `#64748B` (slate-500) | Xám-xanh trung tính, dùng cho thông tin không mang ý nghĩa trạng thái |
| **Neutral background** | Nền trang, card | `#FFFFFF` / `#F8FAFC` (slate-50) | Light mode làm mặc định (đối tượng dùng thực tế là cô nuôi dưỡng/y tế trường — ưu tiên rõ ràng, tránh dark mode gây hiểu lầm màu cảnh báo) |
| **Neutral text** | Văn bản chính | `#0F172A` (slate-900) | Contrast ratio với nền trắng ≈ 17.6:1 — vượt xa WCAG AA |

> **Không dùng Dark Mode cho bản MVP**: vì các màu cảnh báo an toàn (đỏ dị ứng, vàng hết hạn) cần độ tương phản và cảm nhận nhất quán tuyệt đối trên mọi thiết bị hiện trường (bếp, cổng trường, ánh sáng mạnh) — dark mode dời sau khi có phản hồi thực tế từ UAT (Report 1, Phase 8).

---

## 3. Bảng màu ngữ nghĩa (Semantic / Status Colors) — hằng số cố định, dùng chung 2 nền tảng

> Nguồn gốc: `class_diagram_description.md` (các enum trạng thái: `MenuStatus`, `InspectionStatus`, `IncidentStatus`, `BatchStatus`, `DocStatus`, `SeverityLevel`, `CAPAStatus`), và các BR quy định màu tường minh (BR-123 — cảnh báo dị ứng khi quét ID học sinh).

| Ý nghĩa | Token | Hex (dùng cho text/icon trên nền trắng) | Hex nền nhạt (dùng cho badge/chip) | Enum/BR liên quan |
|---|---|---|---|---|
| **Success** (Approved/Passed/Active/Synced) | `success` | `#15803D` (green-700, contrast ≈ 6:1) | `#DCFCE7` (green-100) | `MenuStatus.APPROVED`, `InspectionStatus.PASSED`, `BatchStatus.RELEASED` |
| **Warning** (Pending/Expiring/Cần đồng bộ lại) | `warning` | `#B45309` (amber-700, contrast ≈ 5.2:1) | `#FEF3C7` (amber-100) | `DocStatus.EXPIRING`, MSG-052/053/079 |
| **Danger** (Rejected/Failed/Cảnh báo dị ứng) | `danger` | `#B91C1C` (red-700, contrast ≈ 6.9:1) | `#FEE2E2` (red-100) | BR-123 (cảnh báo dị ứng **bắt buộc đỏ + âm thanh**), `InspectionStatus.FAILED`, MSG-077 |
| **Critical / Quarantine** (khoá toàn hệ thống, sự cố ≥3 học sinh) | `critical` | `#7E22CE` (violet-700, contrast ≈ 6.6:1) | `#F3E8FF` (violet-100) | BR-149 (Quarantine), BR-146/198 (tự động tổng hợp sự cố) — **cố ý khác tông với Danger** để phân biệt "cảnh báo cấp bản ghi" (đỏ) và "khoá cấp hệ thống" (tím), tránh người dùng bị "mù màu đỏ" do quá nhiều cảnh báo đỏ thường nhật |
| **Info / Draft / Neutral status** | `info` | `#1D4ED8` (blue-700, contrast ≈ 7.1:1) | `#DBEAFE` (blue-100) | `MenuStatus.DRAFT`, thông báo hệ thống trung tính |

**Quy tắc phối màu bắt buộc**:
- Dùng shade **600–700** cho chữ/icon trên nền trắng (đạt WCAG AA 4.5:1 cho chữ thường — xem `Accessibility.md`).
- Dùng shade **50–100** làm nền cho badge/chip/alert-box, kết hợp chữ shade 700 cùng họ màu ("soft badge" pattern) — không dùng chữ trắng trên nền đậm cho badge trạng thái (khó đọc khi in/chụp màn hình báo cáo).
- **Không được** tái sử dụng `danger`/`critical` cho bất kỳ ý nghĩa nào khác ngoài cảnh báo an toàn/lỗi — đây là ràng buộc từ BR-123, không phải chỉ gợi ý thẩm mỹ.

---

## 4. Áp dụng cho Web (shadcn/ui + Tailwind CSS — OKLCH)

Tailwind v4 và shadcn/ui mặc định dùng không gian màu **OKLCH** cho toàn bộ token. Khai báo trong `globals.css`:

```css
:root {
  --background: oklch(1 0 0);
  --foreground: oklch(0.145 0.02 260);       /* slate-900 tương đương */

  --primary: oklch(0.55 0.09 220);            /* #0E7490 cyan-700 */
  --primary-foreground: oklch(0.985 0 0);

  --secondary: oklch(0.55 0.03 250);          /* #64748B slate-500 */
  --secondary-foreground: oklch(0.985 0 0);

  --success: oklch(0.53 0.14 145);            /* #15803D green-700 */
  --success-foreground: oklch(0.985 0 0);

  --warning: oklch(0.55 0.13 70);             /* #B45309 amber-700 */
  --warning-foreground: oklch(0.985 0 0);

  --destructive: oklch(0.55 0.19 25);         /* #B91C1C red-700 — dùng làm "danger" */
  --destructive-foreground: oklch(0.985 0 0);

  --critical: oklch(0.48 0.18 305);           /* #7E22CE violet-700 — token tuỳ biến thêm */
  --critical-foreground: oklch(0.985 0 0);

  --info: oklch(0.48 0.17 260);               /* #1D4ED8 blue-700 — token tuỳ biến thêm */
  --info-foreground: oklch(0.985 0 0);

  --border: oklch(0.92 0.005 260);
  --radius: 0.5rem;
}
```

> `--critical` và `--info` là 2 token **mở rộng ngoài bộ mặc định của shadcn** (bộ gốc chỉ có `destructive`) — khai báo thêm trong `tailwind.config` / `globals.css` và map vào `theme.extend.colors` để dùng như `bg-critical`, `text-info` v.v. Giá trị OKLCH ở trên là gần đúng — khi implement cần dùng công cụ chuyển đổi hex→OKLCH chính xác (VD trang `ui.shadcn.com/colors`) và kiểm tra lại bằng công cụ đo contrast trước khi merge.

**Component nguồn**: mọi component shadcn (`Button`, `Badge`, `Alert`, `Toast`...) chỉ được style qua các token trên (`bg-primary`, `text-destructive`...), không hardcode mã màu trong component — xem `Component_Guidelines.md`.

---

## 5. Áp dụng cho Mobile (Flutter + Material Design 3)

```dart
final ColorScheme sfmsColorScheme = ColorScheme.fromSeed(
  seedColor: const Color(0xFF0E7490), // cùng brand seed với Web
  brightness: Brightness.light,
);

final ThemeData sfmsTheme = ThemeData(
  useMaterial3: true,
  colorScheme: sfmsColorScheme,
  extensions: <ThemeExtension<dynamic>>[
    const SfmsStatusColors(
      success: Color(0xFF15803D),
      warning: Color(0xFFB45309),
      danger:  Color(0xFFB91C1C),
      critical: Color(0xFF7E22CE),
      info:    Color(0xFF1D4ED8),
    ),
  ],
);

// Custom ThemeExtension để giữ màu ngữ nghĩa CỐ ĐỊNH, không bị Material 3 tự sinh tonal đè lên
class SfmsStatusColors extends ThemeExtension<SfmsStatusColors> {
  final Color success, warning, danger, critical, info;
  const SfmsStatusColors({required this.success, required this.warning,
      required this.danger, required this.critical, required this.info});

  @override
  SfmsStatusColors copyWith({...}) => ...;
  @override
  SfmsStatusColors lerp(ThemeExtension<SfmsStatusColors>? other, double t) => this;
}

// Sử dụng trong widget:
final statusColors = Theme.of(context).extension<SfmsStatusColors>()!;
Icon(Icons.warning, color: statusColors.danger)
```

**Lý do dùng `ThemeExtension` thay vì `ColorScheme.error`**: Material 3 chỉ có sẵn 1 role màu lỗi (`error`/`errorContainer`) — không đủ 5 mức ngữ nghĩa (success/warning/danger/critical/info) mà SFMS cần theo BR. `ThemeExtension` cho phép khai báo thêm role màu tuỳ biến mà vẫn tương thích `Theme.of(context)` chuẩn Flutter.

**`ColorScheme.fromSeed()` sinh ra gì**: `primary`, `onPrimary`, `primaryContainer`, `secondary`, `tertiary`, và các `surface`/`surfaceContainer*` tone — dùng cho nền, card, AppBar, FAB, BottomNavigationBar theo đúng chuẩn Material 3, **không** dùng các role này để thể hiện trạng thái nghiệp vụ (đó là việc của `SfmsStatusColors`).

---

## 6. Typography

**Font chính thức (dùng chung Web + Mobile)**: **Be Vietnam Pro** (Google Fonts, giấy phép Open Font License, miễn phí thương mại).

**Lý do chọn**: font Việt hoá chuyên biệt — có bộ dấu tiếng Việt được thiết kế riêng (adaptive diacritics), không bị lỗi giãn dòng/chồng dấu như nhiều font Latin thông thường khi gõ tiếng Việt có dấu — quan trọng vì người dùng thực tế là nhân viên trường học Việt Nam (Report 1, Field Survey). Phong cách Neo-Grotesque hiện đại, hợp với thẩm mỹ hành chính/giáo dục nhưng vẫn thân thiện với phụ huynh.

**Fallback stack**: `"Be Vietnam Pro", "Inter", "Noto Sans", system-ui, sans-serif` — Inter/Noto Sans hỗ trợ tiếng Việt tốt, dùng làm dự phòng nếu Be Vietnam Pro không tải kịp (Web) hoặc bị thiết bị chặn tải font ngoài (Mobile).

### Web (Next.js `next/font/google`)
```ts
import { Be_Vietnam_Pro } from "next/font/google";
const beVietnamPro = Be_Vietnam_Pro({
  subsets: ["latin", "vietnamese"],
  weight: ["400", "500", "600", "700"],
  variable: "--font-sans",
});
```
Map vào Tailwind: `fontFamily: { sans: ["var(--font-sans)", "system-ui", "sans-serif"] }`.

### Mobile (Flutter `google_fonts` package)
```dart
textTheme: GoogleFonts.beVietnamProTextTheme(baseTextTheme)
```

### Type Scale — map theo Material 3 (5 nhóm: Display/Headline/Title/Body/Label) sang Tailwind text-size

| Material 3 role | Kích thước (Mobile) | Tailwind tương đương (Web) | Dùng cho |
|---|---|---|---|
| Display | 36–57sp | `text-4xl`/`text-5xl` | Trang chủ dashboard, số liệu lớn |
| Headline | 24–32sp | `text-2xl`/`text-3xl` | Tiêu đề trang, tên màn hình |
| Title | 14–22sp | `text-lg`/`text-xl` | Tiêu đề card, section header |
| Body | 12–16sp | `text-sm`/`text-base` | Nội dung chính, form label |
| Label | 11–14sp | `text-xs`/`text-sm` | Badge, caption, nút nhỏ |

Weight dùng: 400 (Regular) cho Body, 500 (Medium) cho Label/nút, 600–700 (SemiBold/Bold) cho Title/Headline — không dùng weight 300 hoặc thấp hơn (khó đọc trên màn hình nhỏ ngoài trời/bếp).

---

## 7. Spacing, Radius, Elevation

| Token | Giá trị | Web (Tailwind) | Mobile (Flutter) |
|---|---|---|---|
| Spacing unit | 4px cơ số | mặc định Tailwind (`p-1`=4px...) | `EdgeInsets` bội số 4.0 |
| Radius mặc định | 8px (card/button), 12px (modal/sheet) | `--radius: 0.5rem` (mục 4) | `BorderRadius.circular(8)` / `(12)` |
| Elevation | Web dùng `shadow-sm/md/lg` (không dùng z-index tuỳ tiện); Mobile dùng Material 3 tone-based elevation (surface tint, không dùng shadow cứng) | Tailwind `shadow-*` | `Card(elevation: ...)` theo M3 surface tint |

---

## 8. Kiểm tra Accessibility

Toàn bộ token màu ở mục 2–3 đã được chọn để đạt tối thiểu **WCAG 2.2 AA (contrast ratio ≥ 4.5:1 cho chữ thường, ≥ 3:1 cho chữ lớn/icon)** khi dùng đúng theo quy tắc phối màu ở mục 3. Chi tiết đầy đủ, bao gồm touch target size và kiểm tra riêng cho từng cặp màu, xem `Accessibility.md`.

---

## Nguồn tham khảo
- shadcn/ui Theming docs (ui.shadcn.com/docs/theming) — quy ước OKLCH, token mặc định.
- Flutter `ColorScheme` / Material 3 docs (api.flutter.dev, docs.flutter.dev/release/breaking-changes/new-color-scheme-roles).
- WebAIM Contrast Checker guidelines (webaim.org/articles/contrast).
- Be Vietnam Pro — Google Fonts (fonts.google.com/specimen/Be+Vietnam+Pro).
- `class_diagram_description.md`, `02_Requirements/00_BusinessRules/business_rules.md` (BR-123, BR-146, BR-149, BR-198).
- `02_Requirements/00_BusinessRules/BR_Citation_Audit_Report.md` mục 5 (Bucket B — nguồn cho quyết định map BR-038 (ghost code) → BR-123, áp dụng 2026-07-01 sau khi 3 thành viên (Thảo, Huy, Nam) độc lập phát hiện khi viết Screen Specification).
