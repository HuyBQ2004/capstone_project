# Naming Convention — SFMS Design & Frontend

**Trạng thái**: Confirmed (2026-07-01)
**Mục đích**: Quy ước đặt tên nhất quán giữa tài liệu thiết kế, code Web (Next.js), code Mobile (Flutter), giúp trích dẫn chéo BR/UC/MSG/Screen không bị lệch (kế thừa tinh thần đã áp dụng cho UC-ID/BR-ID trong `traceability_matrix.md`).

---

## 1. Định danh nghiệp vụ (đã có sẵn — nhắc lại để không đặt trùng)

| Loại | Định dạng | Ví dụ | Nguồn chuẩn |
|---|---|---|---|
| Use Case | `UC-###` | UC-052 | UC ID trong Report 3 §3.2 (nguồn chuẩn, không phải bảng tổng hợp) |
| Business Rule | `BR-###` | BR-196 | `business_rules.md` |
| System Message | `MSG-###` | MSG-077 | `error_message.md` |
| ADR hệ thống | `ADR-###.md` (số trần) | `ADR-002.md` | `03_Design/02_ADR/` |
| ADR module | `ADR-###_TênMôTả.md` (số + hậu tố mô tả, namespace riêng theo module) | `ADR-001_User_Auth_Architecture.md` | `07_implement/<Module>/` |

## 2. Tên file Screen Specification / Wireframe

Định dạng: `<SốThứTự>_<TênMànHình_PascalCase>.md`, nhóm theo `<Nền tảng>/<Actor>/`.

```
05_ScreenSpecifications/
├── Web/
│   ├── SystemAdmin/01_ManageSchools.md
│   └── ServiceCaterers/01_ThreeStepInspection.md
└── Mobile/
    └── ServiceCaterers/01_ThreeStepInspection.md   ← cùng số + tên với bản Web tương ứng
```

**Quy tắc bắt buộc**: Screen Spec Web và Mobile của **cùng 1 UC** phải giữ **cùng tên file** (chỉ khác nhánh `Web/`/`Mobile/`) — giúp dò cặp tương ứng nhanh khi maintain 2 bản song song (theo hướng "all web + Mobile song song").

## 3. Component & File code

### Web (Next.js/React + shadcn/ui)
| Đối tượng | Convention | Ví dụ |
|---|---|---|
| Component file | PascalCase | `AllergyAlertBadge.tsx` |
| Component tuỳ biến dựa trên shadcn | Giữ tiền tố tên gốc + hậu tố mục đích | `BadgeStatus.tsx` (extend từ `Badge`) |
| Hook | camelCase, tiền tố `use` | `useOfflineSyncQueue.ts` |
| Route (App Router) | kebab-case | `/app/service-caterers/three-step-inspection/page.tsx` |
| Tailwind custom token | kebab-case, tiền tố ngữ nghĩa | `--color-critical`, `bg-critical` |

### Mobile (Flutter)
| Đối tượng | Convention | Ví dụ |
|---|---|---|
| Widget file/class | PascalCase | `AllergyAlertBadge.dart` |
| File Dart khác (service, model) | snake_case | `offline_sync_queue.dart` |
| Route/Screen | PascalCase + hậu tố `Screen` | `ThreeStepInspectionScreen` |
| Theme extension token | camelCase | `statusColors.danger` |

## 4. Đặt tên trạng thái/enum hiển thị UI

- Enum nghiệp vụ giữ nguyên tên tiếng Anh từ `class_diagram_description.md` (VD `MenuStatus.APPROVED`) trong code.
- **Label hiển thị cho người dùng luôn bằng tiếng Việt** (quyết định i18n — xem TODO #10 trong `00_UI_Design_Documentation_Plan.md`, tài liệu này áp dụng mặc định tiếng Việt cho UI trong lúc chờ team chốt chính thức song ngữ hay không): map 1-1 `enum → nhãn hiển thị` trong 1 file constants dùng chung, không hardcode chuỗi rải rác nhiều nơi.

## 5. Asset & Icon

- Icon: `kebab-case`, tiền tố nhóm chức năng — VD `icon-gps-pin.svg`, `icon-camera-evidence.svg`, `icon-allergy-warning.svg`.
- Ảnh placeholder: theo đúng mô tả gốc Report 3 ("placeholder image frame with X mark") — tên file `placeholder-<mục đích>.png`.

## Nguồn dữ liệu
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` (quy ước UC/BR/MSG hiện có).
- `03_Design/02_ADR/ADR-001.md`, `ADR-001_User_Auth_Architecture.md` (quy ước namespace ADR, xác nhận 2026-07-01).
- Next.js App Router conventions, Flutter style guide (dart.dev/effective-dart/style).
