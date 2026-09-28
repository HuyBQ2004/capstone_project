# 07_implement — Mục lục tài liệu triển khai

## Bắt đầu từ đâu

| Bạn là… | Đọc theo thứ tự |
|---|---|
| **Người mới nhận việc** | `09_Dev_Runbook.md` → `Handoff-E2E-Flow-Readiness.md` → `08_E2E_Findings_Backlog.md` |
| **Làm tiếp hai luồng chính** | `07_E2E_Flow_Readiness_Plan.md` (roadmap) + `08_E2E_Findings_Backlog.md` (việc còn lại, có `file:line`) |
| **Viết code mới** | `00_Common_Conventions.md` + mục 8 của `09_Dev_Runbook.md` |

---

## Tài liệu theo nhóm

### Nền tảng — đọc trước khi viết code

| File | Nội dung |
|---|---|
| `00_Common_Conventions.md` | Quy ước chung D1–D12 (mã nghiệp vụ vs UUID, thời gian ISO, audit, storage…) |
| `00_Team_Work_Assignment_Plan.md` | Phân công module theo người |
| `02_Module_Completion_Workflow.md` | Quy trình hoàn thành một module |
| **`09_Dev_Runbook.md`** | **Dựng môi trường, chạy, xác minh, các bẫy hay mắc** |

### Thiết kế theo miền

| File | Nội dung |
|---|---|
| `01_MVP_Backend_Implementation_Plan.md` | Kế hoạch backend MVP |
| `03_FE_Full_Wiring_Master_Plan.md` | Kế hoạch nối FE với backend thật |
| `04_Meal_Substitution_Portion_Pipeline_Design.md` | Món thay thế + chia suất |
| `05_Menu_Grade_Diet_Coverage_Dish_Media_Design.md` | Thực đơn × khối × nhóm chế độ ăn |
| `06_Sent_Reports_Redesign_Design.md` | Báo cáo đã gửi |

### Đợt "thông hai luồng nghiệp vụ trước demo" (07–08/08/2026)

| File | Vai trò |
|---|---|
| **`07_E2E_Flow_Readiness_Plan.md`** | **KẾ HOẠCH** — 6 giai đoạn, đã được duyệt. Không phải nhật ký |
| **`Handoff-E2E-Flow-Readiness.md`** | **ĐÃ LÀM TỚI ĐÂU** — nhật ký tiến độ + baseline đo được |
| **`08_E2E_Findings_Backlog.md`** | **CÒN LẠI** — mọi phát hiện chưa sửa, kèm `file:line` và bằng chứng |
| **`10_E2E_API_Run_Report.md`** | **ĐO THẬT bằng API** — chạy trọn hai luồng trên dữ liệu tự dựng, 6 lỗi chặn cứng tìm ra nhờ đo |
| **`13_Dead_Pages_Check.md`** | **SOI TRANG CHẾT / LINK GÃY** — `npm run check:routes`, lớp lỗi mà `tsc`/`lint`/`build`/`mvn test` đều không bắt được. Tìm ra `menu-approval/[id]` (màn duyệt thực đơn 550 dòng không có đường vào); kèm phát hiện `npm run audit` đã hỏng sẵn |
| **`12_AI_Assistant_Findings.md`** | **TRỢ LÝ AI + gợi ý thực đơn** — 3 lỗi chồng nhau khiến chat luôn hỏng (stub `localhost:8001`, axios 10s, nginx 60s), và AI **bịa nguyên liệu tự do** cho tới khi ép theo danh mục hợp đồng đã duyệt (`GET /catalog-items` mới); và màn soạn thực đơn `/menus/create` **không có nút nào dẫn vào** |
| **`11_UI_Audit_Report.md`** | **RÀ GIAO DIỆN bằng trình duyệt thật** (Playwright MCP, 6 vai) — 3 lỗi P0 sai lệch nghiệp vụ + mẫu lỗi "card ≠ bảng" lặp ở 3 vai. **ĐÃ SỬA HẾT P0/P1 cùng ngày** (mục "✅ ĐÃ SỬA"), kèm 3 lỗi gốc lộ ra khi sửa: không có route guard, `incidentId` trả UUID sai D1, `toPageable()` unsorted |

### Các đợt trước

| File | Nội dung |
|---|---|
| `Handoff-System-Audit-Gaps.md` | Rà gap phân quyền/UX: lỗi mutation im lặng (58/131 lời gọi), trạng thái loading (24 màn) |
| `Handoff-Mock-Data-Removal.md` | Gỡ mock data khỏi FE qua nhiều phiên (dài nhất, tra cứu theo phiên) |
| `review.md`, `review_fix_plan.md` | Ghi chú review cũ |

### Thư mục con

- `seed/` — **script dựng dữ liệu demo chỉ bằng API thật** (không SQL). 8 pha, chạy lại được;
  xem `seed/README.md`.
- `audit-scripts/` — script rà soát, chạy lại ra đúng số liệu trong doc. Bảng mô tả ở mục 7
  của `09_Dev_Runbook.md`.
- `MF01_*` … `MF13_*` — tài liệu thiết kế từng module (TDS).

---

## Trạng thái hiện tại (cập nhật 08/08/2026)

Nhánh `namnvhe180863`. Hai luồng bắt buộc chạy được để demo:

1. **Luồng bếp** — tạo món → mẫu thực đơn → lịch thực đơn → mua hàng → nhập kho → xuất kho →
   kiểm thực 3 bước + lưu mẫu → phân phát.
2. **Luồng sự cố** — báo cáo → y tế điều tra → khoanh vùng + giữ mẫu → CAPA → trường duyệt →
   nộp bằng chứng → đóng.

| Giai đoạn | Trạng thái |
|---|---|
| 0 — Chuẩn dinh dưỡng theo trường×khối, quyền `/diet-groups`, dọn dữ liệu giả | ✅ Xong |
| 1 — Thông luồng bếp (7 mục P0) | 🟡 **7/7 phía backend** — `menu-form` fan-out xong; `batchId` xong ở BE, **FE `step1RowsOfIssue()` chưa gửi** |
| 2 — Thông luồng sự cố (6 mục P0, **4 chặn cứng**) | 🟡 **Backend xong và ĐÃ ĐO THẬT trọn đường (sự cố đóng được)**, **FE chưa nối**; B5/B6 chưa làm |
| 3 — Độ mịn danh mục nguyên liệu | ⬜ |
| 4 — Tách seed khỏi migration production | ⬜ |
| 5 — Dọn UI | ⬜ |

> **08/08/2026 (phiên 2).** `mvn test` sau khi xoá seed: **352 pass** — không test nào phụ
> thuộc dữ liệu đã cắt. Đã gỡ 4 chốt chặn cứng của luồng sự cố ở backend (B1–B4), sửa
> `menu-form` fan-out (A2), và xoá `InspectionServiceImpl.parseUuid()` — hàm nuốt lỗi này làm
> **Giai đoạn 1.4 chưa từng thật sự thông** dù đã được ghi ✅ (chi tiết ở Handoff).

> **08/08/2026 — DB đã xoá sạch dữ liệu nghiệp vụ seed.** Chỉ còn cấu hình hệ thống + 1 tài
> khoản System Admin; mọi thứ khác tạo bằng tay qua UI. Sửa file migration ⇒ **phải drop +
> create lại DB** (cả stack compose lẫn chạy tay). Xem `09_Dev_Runbook.md` mục 9 và 11.

> **08/08/2026 (phiên 3) — đo thật bằng API, xem `10_E2E_API_Run_Report.md`.** Dựng đủ dữ liệu
> hai luồng **chỉ bằng API** (`seed/`), chạy tới **`INC-2026-002` status = CLOSED**. Ba chốt
> chặn cứng cũ (không đóng được sự cố · giữ mẫu 500 · y tế không tra được suất ăn) **đã được
> xác nhận gỡ trên dữ liệu thật**. Đo lộ thêm **6 lỗi** chặn cứng khác, đã sửa: MF06 403 với tài
> khoản Đơn vị suất ăn · MF11 CAPA không ai nhận được · Bước 1 kiểm thực 500 khi khai lô thật
> · 500 thay vì 415/405/422 · **mọi lỗi màn danh mục nguyên liệu hiện thành 500** ·
> `batchId` trả UUID thô (D1).

> **08/08/2026 (phiên 4) — rà UI rồi SỬA, xem `11_UI_Audit_Report.md`.** Điều khiển trình duyệt
> thật qua Playwright MCP, đi 6 vai trên dữ liệu vừa dựng. Ra **3 lỗi P0 sai lệch nghiệp vụ**,
> đã sửa hết trong ngày cùng nhóm P1/P2 (`mvn test` 352 pass · `next build` sạch · đo lại bằng
> API sau khi rebuild compose). Ba **lỗi gốc** chỉ lộ ra lúc sửa, đáng nhớ hơn cả triệu chứng:
> (1) **`(app)` chưa từng có route guard** — store dọn phiên nhưng không ai đọc `status`, nên
> mất phiên hiện y hệt "dữ liệu rỗng"; (2) **`FoodSampleResponse.incidentId` trả UUID nội bộ**
> và `FoodSampleSpec` so chuỗi mã với cột UUID ⇒ `GET /samples?incidentId=INC-…` **luôn rỗng,
> im lặng**; (3) **`ListQuery.toPageable()` trả `Sort.unsorted()`** ⇒ danh sách sự cố lộn xộn —
> **đã chốt luật**: danh sách đọc theo thời gian phải tự truyền `Sort` ở call-site.
>
> Kèm yêu cầu của Nam: **ô nhập tay có ràng buộc phải là dropdown**. Rà toàn FE đối chiếu
> `CHECK` constraint — enum hầu hết đã là `Select`; riêng **đơn vị đo** còn nhập tay 2 chỗ và
> đọc hằng khai tay 2 chỗ, nay tất cả đọc `GET /measure-units` qua `lib/measure-units.ts`
> (backlog D5-FE, lần thứ 4 của lớp lỗi "hằng số nhân đôi").
