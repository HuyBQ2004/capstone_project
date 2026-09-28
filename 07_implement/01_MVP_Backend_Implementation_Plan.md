# 01 — Kế hoạch hiện thực Backend (Spring Boot, AI-accelerated, song song đủ 13 module)

**Ngày lập:** 2026-07-09 · **Cập nhật:** 2026-07-28 (nâng version stack — xem §3) · **Trạng thái:** Đề xuất — theo hướng hợp nhất đã chốt ở `00_Team_Work_Assignment_Plan.md`
**Nguồn kiến trúc:** `03_Design/02_ADR/ADR-000.md` (dev env) + `ADR-001` (prod).
**Nguồn phân công/RACI/lịch song song:** `00_Team_Work_Assignment_Plan.md` — file này **không lặp lại** RACI hay bảng module ở đó, chỉ trỏ tới và biến thành lịch tuần + checklist backend cụ thể. Sửa RACI/tier thì sửa ở `00_` trước, không sửa ở đây.

> **Thay đổi so với bản 09/07:** bản cũ scope hẹp theo kiểu MVP (chỉ 9/13 module "Core/Slim", bỏ TDS để chạy nhanh trong 3–4 tuần). `00_Team_Work_Assignment_Plan.md` (chốt 23/07) đã thay bằng hướng hợp nhất: **viết TDS/TestSpec đầy đủ cho cả 13 module** (lấy Frontend Mock làm input chính, BR/UC/MSG vẫn bắt buộc trích từ `02_Requirements/`), rồi mới sinh backend từ TDS đã Approved — **không hoãn module nào**, chỉ khác nhau ở **thứ tự ưu tiên** P0/P1/P2. Bản này viết lại toàn bộ để khớp hướng đó.

---

## 1. Nguyên tắc

- AI nén phần **viết code** (entity/repo/service/controller/DTO/mapper/test skeleton sinh từ TDS + schema đã chốt) và phần **viết TDS** (đọc trực tiếp mock FE thay vì suy diễn từ văn xuôi — xem `00_` §0, §2a).
- AI **không** nén: auth/RBAC đúng-an toàn, tích hợp FE↔BE, debug code "compile được nhưng sai", review chéo, đối chiếu UC/BR/MSG không bịa số.
- **Không bỏ TDS để chạy nhanh** — TDS là input bắt buộc cho report và là điểm chốt hợp đồng trước khi 5 người code song song không đụng nhau.
- External services vẫn **stub ở dev** theo ADR-000 (không đổi so với bản cũ) — xem bảng ở §3.
- **Không ước lượng cứng "X tuần"** như bản cũ (con số đó sai vì bỏ qua TDS). Lịch ở §4 là lịch theo **tuần calendar tương đối** (Tuần 0, 1, 2…), tốc độ thật phụ thuộc team; dùng cổng cuối tuần (Definition of Done từng tuần) để đo tiến độ, không đo theo cảm giác.

---

## 2. Phạm vi — đủ 13 module, khác nhau ở thứ tự làm

Bảng chủ/ưu tiên đầy đủ nằm ở `00_Team_Work_Assignment_Plan.md` §3 — dưới đây chỉ tóm tắt để đọc nhanh khi lập lịch backend. **Không dùng bảng này để tranh cãi chủ module** — lấy `00_` làm nguồn duy nhất.

| Tier | Module | Chủ |
| --- | --- | --- |
| **P0 — nền tảng, chặn cứng** | MF01 User & Access Control, MF02 School/Class (+Academic Year) | Nam |
| **P1 — trục xương sống** | MF03 Student & Parent | Huy |
| | MF04 Supplier & Partner | Mạnh |
| | MF05 Menu & Diet | Thảo |
| | MF06 Ingredient & Inventory | Huy |
| | MF08 Food Sample Retention | Mạnh |
| | MF09 Meal Serving & Distribution | Minh |
| | MF11 Incident & Emergency | Minh |
| **P2 — làm song song nửa sau, không dồn về cuối** | MF07 Inspection (+Violation-Process) | Thảo |
| | MF10 Feedback & Announcement | Mạnh |
| | MF12 AI Supporting Services | Thảo |
| | MF13 Reporting & Analytics | Nam |

**Trục xương sống demo/report (Definition of Success):**
`Login theo role → xem Menu theo khối (Parent) → phân suất & thống kê theo món (Distribution) → tạo sự cố → điều tra: sự cố → HS bị ảnh hưởng → món nghi vấn → lô → NCC → mẫu lưu` — chạy thật qua backend, dữ liệu nối bằng **ID**, không phải tên.

---

## 3. Stack & quy ước codegen

> **Cập nhật 2026-07-28 (Nam chốt):** nâng từ Spring Boot 3 (Java 17+) lên **Spring Boot 4.1.x (Java 25 LTS)** —
> framework mới nhất, JVM hiệu năng tốt hơn, hỗ trợ Virtual Threads tốt, vòng đời hỗ trợ dài (giảm nâng cấp lớn
> về sau). Không đổi phần còn lại của stack (PostgreSQL/Flyway/Spring Security/Swagger/MapStruct/Docker Compose).

**Stack (ADR-000):** Spring Boot 4.1.x (Java 25 LTS) · PostgreSQL 16 · Flyway · Spring Security + JWT · springdoc-OpenAPI (Swagger) · MapStruct · `docker compose -f docker-compose.dev.yml up`.

**External services — STUB ở dev (contract JSON = production, theo ADR-000 §Mock Strategy):**

| Dịch vụ | Stub |
| --- | --- |
| Face Recognition | endpoint trả `{ match:true, faceId:"mock-uuid" }` |
| GPS | backend nhận lat/lng bất kỳ (validate geofence là no-op ở dev) |
| Gemini AI | trả mock JSON đúng schema |
| SMTP | Mailpit optional (`:8025`) hoặc log |
| Cloudflare R2 | lưu file vào `./data/uploads` |
| Redis | tắt cache (`caffeine` in-memory) |
| PDF / Real-time | JSON thô / polling ở giai đoạn đầu; PDFBox/WebSocket khi có nhu cầu thật |

**Package layout chuẩn (mọi module theo đúng khuôn — khớp `00_` §5):**
```
com.sfms.<module>/
  domain/        Entity JPA + enum status riêng theo thực thể (data_model.puml — KHÔNG union phẳng)
  repository/    Spring Data JPA
  dto/           Request/Response records — khớp JSON §9 TDS
  mapper/        MapStruct
  service/       Business logic + validate BR (port từ semantic-audit.ts)
  web/           Controller REST /api/... — path & shape khớp TDS §9
```

**Nền chung (Nam dựng 1 lần, cả team dùng — khớp `00_` §2b, §5):**
- Base entity: `id`, `createdAt`/`updatedAt` kiểu **Instant**.
- `@RestControllerAdvice` global exception + error-code catalog khớp `error_message.md`.
- `PagedResponse<T>` + pattern `Specification` cho phân trang/search/filter — **theo đúng đặc tả `00_` §2b** (shape `{items,total,page,pageSize,pageCount}`, thứ tự áp bộ lọc, `SEARCH_FIELDS` cố định từng module — không OR toàn bộ cột String).
- `nextId()`/sequence tập trung (thay `Date.now().slice(-n)` phía mock).
- Flyway **V1 = toàn bộ schema**, sinh một lần từ `data_model.puml` — module chỉ thêm seed/logic, không tự tạo migration mới đè lên nhau (xem rủi ro va chạm ở `00_` §4b).
- **Audit trail (dựng 2026-07-31, cùng MF01):** `BaseEntity` có sẵn `createdBy`/`updatedBy` (`String`, email actor), tự điền qua Spring Data `AuditorAware` (`auditorAwareRef = "auditorAware"`, bean ở `iam/security/AuditorAwareConfig`) — module con `extends BaseEntity` là có ngay, **không** tự `set` trong service, **không** tự khai lại 2 tên cột này (reserved — xem `00_Common_Conventions.md` D9).
- **Object storage (dựng 2026-07-31, cùng MF01):** `common/storage/StorageService` — `LocalDiskStorageService` (mặc định, dev, ghi `./data/uploads`, phục vụ qua `/media/**`) / `R2StorageService` (`sfms.storage.provider=r2`, prod, ADR-001 mục 6). Module nào upload file/ảnh thì inject interface này, không tự viết I/O (D10).
- **Secrets local (dựng 2026-07-31):** nạp qua file `.env` ở root `Backend/` (thư viện `spring-dotenv`), **không phải** `application-dev.yml` như ADR-000 bản gốc ghi — xem ADR-000 mục "Cập nhật 2026-07-31". Copy `.env.example` → `.env`, không commit `.env` thật.
- **Version pin — không hạ xuống:** `testcontainers=1.21.4` (1.20.4 vỡ trên Docker Desktop mới — npipe proxy trả stub response), `jacoco=0.8.15` (0.8.12 không đọc được bytecode Java 25, class file major version 69). Ai thêm `*IT.java` mới mà thấy `ContainerFetchException` hoặc `Unsupported class file major version` thì đây là nguyên nhân đã biết, không phải máy dev bị lỗi.

**Nguyên tắc dữ liệu (áp ngay khi gen):**
- Món tham chiếu **`dishId`** (dùng `Dish.id` / `DishNutrition.id` sẵn có), không phải tên.
- Ngày = **ISO/Instant**; tiền/lượng = **number + unit/currency riêng**.
- **Không** lưu field dẫn xuất (`hoursRemaining`, `allergyConflicts`, `dishes` count…) — tính runtime.
- `status` **union riêng theo thực thể**, `STATUS_BADGE`-style chỉ tra nhãn/màu ở tầng hiển thị.

**Quy tắc chống lệch (khớp `00_` §0):** nếu backend cần khác hợp đồng mock, sửa **đồng thời** cả mock FE + backend trong cùng một thay đổi — không để hai bên nói hai chuyện khác nhau.

---

## 4. Lịch theo tuần — TDS trước, backend sau, song song sau một nút cổ chai

Lịch ngày chi tiết (Ngày 1–5) và bảng "3 chỗ dễ va khi song song" nằm ở `00_` §4b — dưới đây quy đổi thành nhịp tuần để theo dõi tiến độ dài hơi, kèm cổng nghiệm thu (Definition of Done) mỗi tuần.

### Tuần 0 — Chốt nền dữ liệu (chặn cứng, làm trước khi viết TDS)
- **Nam + Mạnh:** rà `data_model.puml` / `class_diagram.puml` / ERD (`02_ERD/erd_crowsfoot.puml`) khớp mock FE **mới nhất** (không phải bản 22/07 — kiểm tra lại các thay đổi mock-data.ts phát sinh sau đó, vd IssueSlip, DietGap, CookSheet, menu-approval theo khối×ngày, partnership applications).
- **Nam:** viết TDS mẫu **MF01 + MF02** theo `PHASE-3_TDS.md` (17 mục), dùng làm convention cho 4 người còn lại copy (đặt tên, format §2, shape §9 chuẩn).
- **Cổng cuối tuần:** schema chốt (nguồn duy nhất cho AI gen TDS + backend) · TDS MF01/MF02 xong bản nháp.

### Tuần 1 — TDS song song đủ 13 module
- Sau khi có TDS mẫu MF01/MF02: **Mạnh** TDS MF04, MF08 · **Huy** TDS MF03, MF06 · **Minh** TDS MF09, MF11 · **Thảo** TDS MF05 · **Nam** hoàn thiện MF01/MF02, bắt đầu MF13.
- Mỗi TDS đọc đúng nguồn theo `00_` §2a (mock-server.ts/db.ts → §9, mock-data.ts → §2+§5, màn hình FE → §6/§8, `data_model.puml` → §5, `semantic-audit.ts` → §13) và **trích UC/BR/MSG từ `02_Requirements/`**, không suy diễn từ FE.
- **Review chéo theo RACI:** Huy (§2 Traceability) · Mạnh (§5 Schema + §9 API) · Minh (§8 Interface + §9 JSON shape) · Thảo (§13 Test) · Nam (Final Approval).
- **Cổng cuối tuần:** TDS + TestSpec của MF01, MF02, MF03, MF04, MF05, MF06, MF08, MF09, MF11 (P0+P1) ở trạng thái **Approved**.

### Tuần 2 — Backend nền chung + P0/P1 song song
- **Nam (đầu tuần, mở khoá cho cả team):** `docker-compose.dev.yml`, Spring Security + JWT, RBAC 7 role, base entity, global exception, **Flyway V1 = schema MF01** (mô hình "V1 = toàn schema" đã bị bỏ từ MF02 — mỗi module một file V, xem `00_Common_Conventions.md` §0-BIS), Swagger, `PagedResponse`/`Specification` mẫu (§3) + backend **MF01** chạy được + **MF02** như vertical slice mẫu.
- **Cổng giữa tuần:** auth chạy + MF02 CRUD qua Swagger — bắt buộc xong trước khi 4 người còn lại bắt đầu gen, vì Flyway V1 và pattern chung do Nam giữ (tránh đụng migration, xem `00_` §4b bảng va chạm).
- **Sau nút cổ chai, chạy song song trên nền chung:** Mạnh backend MF04 + MF08 · Huy backend MF03 + MF06 · Minh backend MF09 + MF11 · Thảo backend MF05.
- Tham chiếu chéo module bằng **ID (String/UUID)**, không import entity của module khác; enum/error-code dùng chung đặt ở `common/` do Nam sở hữu.
- **Cổng cuối tuần:** mỗi module P0/P1 CRUD được qua Swagger; trục ID (`dishId`, `studentId`, `batchId`, `menuId`) thông giữa các module.

### Tuần 3 — P2 song song + tích hợp FE↔BE trục xương sống
- **TDS + backend song song cho P2:** Mạnh MF10 · Thảo MF07 + MF12 · Nam MF13.
- Thay `mock-server.ts` bằng API thật **theo từng resource** (ưu tiên resource thuộc trục xương sống trước).
- Seed dữ liệu demo nhất quán (1 trường, vài lớp/HS, 1–2 tuần menu theo khối, 1 sự cố đủ chuỗi truy xuất).
- Auth theo role trên FE (mỗi actor thấy đúng màn).
- **Cổng cuối tuần:** cả 13 module có backend chạy qua Swagger; trục xương sống chạy được end-to-end ít nhất ở môi trường dev.

### Tuần 4 — Hardening + Review cuối + nghiệm thu (theo `00_` §7)
- Huy đối chiếu tổng thể UC/BR/MSG trong mọi TDS — không mã bịa.
- Minh xác nhận §9 API Spec: đổi baseURL FE sang backend thật, các màn vẫn chạy đúng.
- Thảo chạy integration test từ §13 TestSpec mỗi module P1 (P0/P2 tối thiểu smoke test); lập danh sách điểm lệch gửi chủ module sửa.
- Nam final approve toàn bộ TDS + kiến trúc.
- `docker compose up` sạch từ máy trắng; buffer cho rủi ro tích hợp phát sinh.

> Lịch trên giả định nhịp độ ổn định của cả 5 người; nếu vướng ở nút cổ chai Tuần 0/2 (schema hoặc nền chung trễ), toàn bộ lịch dịch theo — không rút ngắn Tuần 1 (TDS) để bù, vì TDS ẩu là nguồn lỗi lan ra cả 13 module.

---

## 5. Definition of Done — theo `00_` §7, áp cho từng module

- [ ] TDS-MFxx_Backend.md (17 mục) + TDS-MFxx_Frontend_Web.md (8 mục) + TestSpec-MFxx đều **Approved** (ký Nam), review chéo đủ 4 vai (Huy/Mạnh/Minh/Thảo).
- [ ] Entity + Flyway migration khớp `data_model.puml` đã chốt ở Tuần 0.
- [ ] Repo/Service/Controller + DTO; validate BR cốt lõi port từ `semantic-audit.ts`.
- [ ] Endpoint trả đúng contract mà `mock-server.ts` đang giả lập (đối chiếu TDS §9); phân trang/search/filter khớp `00_` §2b.
- [ ] RBAC 7 role áp đúng theo `business_rules.md`.
- [ ] ≥1 integration test happy path + 1 error path cho module P0/P1 (P2 tối thiểu smoke test).
- [ ] Không lưu field dẫn xuất; ID/ngày/tiền theo chuẩn §3.
- [ ] Review chéo theo RACI + Nam final approve.

**Nghiệm thu toàn dự án (cả 13 module):** `docker compose up` sạch từ máy trắng · **52 resource** CRUD qua Swagger khớp hợp đồng mock · RBAC 7 role đúng · trục xương sống nối thông bằng ID · TDS/TestSpec tất cả module "Approved".

---

## 6. Rủi ro & giảm nhẹ

| Rủi ro | Giảm nhẹ |
| --- | --- |
| Bỏ qua Tuần 0 → viết TDS/gen backend trên model lỗi | **Không viết TDS trước khi schema (data_model.puml/ERD) chốt.** |
| Nút cổ chai Tuần 0/2 (Nam) trễ → cả team nghẽn | Nam ưu tiên tuyệt đối TDS mẫu + nền chung; các module khác đọc mock/data_model.puml trước để chuẩn bị song song ngay khi nút cổ chai mở. |
| Hai người cùng tạo Flyway migration mới | Chỉ Nam giữ **V1 = toàn bộ schema**; module khác chỉ thêm seed/logic trong migration riêng có thứ tự rõ, không sửa V1. |
| Tham chiếu chéo module qua import entity trực tiếp | Bắt buộc tham chiếu bằng **ID**, không import class entity của module khác (xem `00_` §4b). |
| Lệch contract FE↔BE | Giữ đúng JSON schema `mock-server.ts` làm hợp đồng; đổi hai đầu cùng lúc; Minh review §9 mọi TDS. |
| TDS viết ẩu để chạy nhanh, UC/BR/MSG bịa số | Huy review Traceability bắt buộc trước khi Nam approve; **không dùng số từ `Report 3.md` appendix**, chỉ `02_Requirements/` là nguồn chuẩn. |
| Code AI "compile được nhưng sai" | Review chéo bắt buộc theo RACI + integration test cho trục xương sống trước khi coi module là xong. |
| Ảo giác "AI nén cả tiến độ" | Đo bằng cổng cuối tuần (Definition of Done §5), không đo theo cảm giác tốc độ gõ code. |

---

*Bản này là kế hoạch **thực thi backend** rút ra từ hướng hợp nhất ở `00_Team_Work_Assignment_Plan.md`. RACI, bảng module đầy đủ, lịch ngày chi tiết và hợp đồng phân trang/search/filter là nguồn ở `00_` — sửa ở đó trước, rồi đồng bộ lại file này nếu lịch tuần thay đổi.*
