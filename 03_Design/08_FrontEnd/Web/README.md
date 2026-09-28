# SFMS Web Frontend (Next.js)

Frontend web cho **School Food Safety Management Support System** (SEP490 – G8), dựng theo:

- `03_Design/00_Foundation/Design_System.md` — shadcn/ui-style + Tailwind CSS v4, token OKLCH, brand seed `#2563EB` (blue-600 — đổi từ `#0E7490` ngày 25/07/2026 theo Minh; Design_System.md chưa cập nhật theo), font **Be Vietnam Pro**, light mode only (MVP)
- `03_Design/04_ScreenFlow/` — screen flow theo 7 actor
- `03_Design/05_ScreenSpecifications/Web/` — đặt tên màn hình/route bám theo Screen Spec
- `02_Requirements/00_BusinessRules/business_rules.md` + `error_message.md` — BR/MSG code được gắn trực tiếp vào UI (alert, validation, tooltip mô tả)

## Chạy dự án

```bash
npm install
npm run dev   # http://localhost:3000
npm run build # kiểm tra production build
```

## Trạng thái hiện tại

**Mock API — chưa nối backend thật.** Kiến trúc dữ liệu:

- **axios** (`src/lib/api/client.ts`) — HTTP client chuẩn, baseURL `/api`
- **axios-mock-adapter** (`src/lib/api/mock-server.ts`) — chặn mọi request và phục vụ
  dữ liệu in-memory (`src/lib/api/db.ts`, seed từ `mock-data.ts`). Create/update
  thấy ngay trong phiên; reload trang reset về seed. Mọi mutation tự ghi vào
  Audit Logs (BR-170).
- **TanStack Query** (`src/lib/api/hooks.ts`) — `usePagedList` / `useDetail` /
  `useCreate` / `useUpdate` / `useAction`
- **TanStack Table** (`src/components/app/data-table.tsx`) — `DataTable` dùng chung:
  server-style pagination + search cho MỌI trang danh sách
- **Zustand** (`src/lib/stores/auth-store.ts`) — phiên đăng nhập mock; Login chọn
  vai trò → lưu store → Topbar hiển thị user, nút Đăng xuất (MSG-027)

Khi nối backend Spring Boot: xóa `setupMockServer()` trong
`src/components/app/providers.tsx` và trỏ `baseURL` sang API thật — toàn bộ hooks
và trang giữ nguyên.

## Cấu trúc

```
src/
├── app/
│   ├── (auth)/            # Login, Forgot/Reset Password, Verify OTP
│   ├── (app)/             # App shell (sidebar + topbar) — route theo vai trò
│   │   ├── system-admin/      # Dashboard, Users, Roles, Schools, Audit Logs, Backup, System Status
│   │   ├── school-admin/      # Dashboard, Partnerships, Menu Approval, Incidents, Classrooms,
│   │   │                      # Students, Academic Years, Violations, Announcements, Reports
│   │   ├── medical-staff/     # Dashboard, Health Reviews/Records, Special Meals, Inspections,
│   │   │                      # Samples, Incidents
│   │   ├── service-caterers/  # Dashboard, Menus, Special Meals, Receiving, Inspections (3 bước),
│   │   │                      # Samples, Inventory, Meal Service, Corrective Actions, Incidents
│   │   ├── teacher/           # Dashboard, Students, Distribution, Incidents
│   │   ├── parent/            # Dashboard, Weekly Menu, Meal History, Allergies, Feedback
│   │   └── supplier/          # Dashboard, Purchase Requests, Batches, Deliveries, Certificates,
│   │                          # Partnerships
│   ├── globals.css        # Design tokens (OKLCH) theo Design_System.md
│   └── layout.tsx         # Be Vietnam Pro font
├── components/
│   ├── ui/                # Button, Card, Badge, Input, Table, Alert, Dialog, Tabs (shadcn-style)
│   └── app/               # AppShell, Sidebar, Topbar, PageHeader, StatCard, StatusBadge,
│                          # DataTable (TanStack Table), Pagination, Providers,
│                          # announcements/ (list + detail dùng chung mọi vai trò — UC-125/UC-105)
└── lib/
    ├── api/               # client.ts (axios) · db.ts (in-memory) · mock-server.ts · hooks.ts
    ├── stores/            # auth-store.ts (Zustand)
    ├── roles.ts           # Cấu hình 7 vai trò + navigation
    ├── mock-data.ts       # Seed data + STATUS_BADGE map (enum → màu ngữ nghĩa)
    └── utils.ts           # cn()
```

## Quy ước quan trọng

- **Màu ngữ nghĩa cố định** (BR-123): `success`/`warning`/`danger`(destructive)/`critical`/`info` — soft badge pattern (nền shade 100 + chữ shade 700). KHÔNG dùng danger/critical cho mục đích ngoài cảnh báo an toàn.
- `critical` (tím) dành riêng cho Quarantine/khóa hệ thống (BR-149) — khác với `danger` (đỏ) cấp bản ghi.
- Trạng thái nghiệp vụ dùng `<StatusBadge status="..." />` với key trong `STATUS_BADGE` (`src/lib/mock-data.ts`).
- Route mới: thêm nav item vào `src/lib/roles.ts` + tạo `page.tsx` trong thư mục vai trò tương ứng.

## Việc tiếp theo (khi nối backend)

1. Thay mock session bằng JWT + RBAC thật (BR-007), middleware chặn route theo vai trò
2. Thay `mock-data.ts` bằng API client (Spring Boot backend theo ADR)
3. Bổ sung các màn chi tiết/form còn lại theo `05_ScreenSpecifications/Web/` (~190 màn trong Authorization Matrix)
4. i18n nếu cần song ngữ Việt/Anh
