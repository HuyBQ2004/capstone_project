# 07_implement — Phân công & Quy trình: Viết TDS/TestSpec từ Frontend Mock rồi sinh Backend

**Ngày lập:** 2026-07-02 · **Cập nhật:** 2026-08-02 (xem changelog ngay dưới) · 2026-07-30 · 2026-07-28 (thêm §2c prompt mẫu, §2c-4 chọn model, nâng stack Java 25 LTS/Spring Boot 4.1.x, §2c-5 prompt sinh lại MF01 kèm CF-1/CF-2 + Google OAuth2)

> **Changelog 2026-08-02 — kế hoạch bám theo code thật, không còn bám giả định:**
>
> Từ 31/07 MF01 + MF02 đã merge vào `dev` và chạy được ở `04_Development/Backend/`. Kế hoạch này viết khi chưa có dòng code nào, nên vài chỗ đã lạc hậu và đang **dạy sai** cho 10 module còn lại.
>
> 1. **Thêm nguồn `-1` = CODE THẬT** vào cả hai prompt (§2c-2 sinh TDS, §2c-3b sinh TestSpec) và §6. Ưu tiên **cao hơn tài liệu**: tài liệu mô tả một đằng mà code làm một nẻo thì **code đúng**. Trước bản này AI đọc TDS rồi sinh code theo giả định 28/07 — đúng nguyên nhân khiến TDS-MF03 mắc đủ 4 lỗi (xem `00_Common_Conventions.md` §0-BIS).
> 2. **`00_Common_Conventions.md` §0-BIS (mới)** — chốt vị trí project `04_Development/Backend/`, package `com.sfms.mf<NN>`, luật đánh số Flyway, bảng 11 hạ tầng dùng chung, MF02 làm module mẫu, và 4 lỗi kèm lệnh grep tự kiểm.
> 3. **Sửa "Flyway V1 = toàn schema, Nam giữ"** ở **14 file** (13 TDS + `01_MVP_Backend_Implementation_Plan.md` §113) và §5 mục này. Mô hình đó đã bị bỏ từ khi MF02 dùng `V3`; để nguyên thì module thứ 4 trở đi sẽ sửa file V đã merge → Flyway checksum mismatch trên máy cả nhóm.
> 4. **D8 chuyển ⏳ → 🟡** — không phải Nam đã ký, mà là 11/11 controller MF01+MF02 đã mang đủ annotation. Tài liệu để ⏳ nữa là mâu thuẫn với code.
> 5. **D1 nói rõ áp cho cả khóa ngoại trong JSON** (`"classroomId": "3A"`, không phải UUID) — chỗ TDS-MF03 v1.0 làm sai và nhiều TDS khác nhiều khả năng cũng vậy.
>
> **Changelog 2026-07-30 — đóng lỗ hổng khiến tài liệu sinh mới sẽ lặp lại 76 mismatch của `review.md`:**
>
> 1. **§2c-2 prompt mẫu** — thêm **nguồn số 0** = `00_Common_Conventions.md` (D1–D7, Approved). Trước bản này prompt **không hề nhắc** file quy ước, nên ai sinh TDS mới sẽ đọc mock làm hợp đồng và tái tạo đúng 3 quy ước định danh mâu thuẫn mà D1 vừa dẹp. Đây là lỗi **cơ chế**: prompt cũ buộc AI làm sai.
> 2. **§2c-2 nguồn số 5** — thu hẹp vai trò mock: vẫn là nguồn cho *path/method/tên field/luồng*, **không còn** là nguồn cho *định dạng ngày / giá trị enum / shape lỗi / chiến lược `id`*.
> 3. **§2b bảng `SEARCH_FIELDS`** — sửa **13/16 dòng sai**. Bảng cũ đoán tên field theo lối đặt tên thông dụng thay vì đọc `mock-data.ts`; MF06 CF-M06-9 và MF07 CF-17 chỉ nêu được một phần. Bản mới verify trực tiếp từ code.
> 4. **§2b-2** — `mealPrefix` thuộc **MF08** không phải MF09 (đã grep xác nhận: 2 màn `samples` dùng nó để lọc `FoodSample.meal`); `step`/`stepStatus` **0 call-site**, bỏ khỏi backend.
> 5. **§0 quy tắc chống lệch** — thêm ngoại lệ đã chốt: mock **cố ý đóng băng** với D1–D7, không sửa theo, vì nó sẽ bị xóa khi backend chạy.
> 6. **§2c-3, §2a, §5, §6** — bổ sung checklist D1–D7 và trỏ `00_Common_Conventions.md` làm nguồn ưu tiên cao nhất.
> 7. **Swagger từ "tên stack" thành ràng buộc kiểm được** — thêm **D8** vào `00_Common_Conventions.md` (8 luật S1–S8) + lan sang §2a/§2c-2/§2c-3/§2c-3b/§5/§6. Lý do: grep `@Operation|@Schema|@ApiResponse|@Tag(` trên `07_implement/` ra **0 kết quả**, trong khi Swagger đang là **cổng nghiệm thu** (`01_MVP…` §Nghiệm thu: *"52 resource CRUD qua Swagger"*) và **nguồn sinh type FE** (T3.4 `openapi-typescript`). Spec không đặc tả → type FE sinh sai theo → lệch D1/D2/D4/D6 quay lại qua file sinh tự động. ⏳ **D8 chờ Nam approve.**
**Trạng thái:** Đề xuất (bản hợp nhất) — Leader (Nam) đã chốt hướng; chờ 4 thành viên xác nhận.
**Nguồn:** `Report 1.md` §3.1.2 (nhóm), `Report 2.md` §3.4 (RACI), `01_MVP_Backend_Implementation_Plan.md`, **bản mock Frontend tại `03_Design/08_FrontEnd/Web/`** — nguồn hợp đồng và luồng chính, **template tài liệu tại `06_Document/Templates/`**.

---

## 0. Hướng làm (chốt 2026-07-23) — hợp nhất bản gốc + bản mock

Có hai bản kế hoạch trước đây:

- **Bản gốc (02/07):** viết trọn bộ TDS/TestSpec cho 13 module, lấy nguồn từ `02_Requirements/`, `03_Design/`. Đúng hướng nhưng thiếu nguồn "sống" phản ánh nghiệp vụ đã được kiểm chứng.
- **Bản mock (09/07):** sinh nhanh backend từ mock FE trong ~2 ngày, bỏ TDS. Nhanh nhưng thiếu tài liệu cho report.

**Hướng hiện tại (hợp nhất):**

> **Viết TDS/TestSpec ĐẦY ĐỦ cho cả 13 module, lấy Frontend Mock làm nguồn input chính, rồi mới sinh Backend dựa trên tài liệu đó.**

- TDS/TestSpec vẫn phải viết đủ theo template — **không bỏ** vì cần cho report nộp.
- Nhưng thay vì suy diễn từ tài liệu văn xuôi, AI đọc trực tiếp **mock Frontend** (đã đóng băng luồng nghiệp vụ, hợp đồng dữ liệu, ràng buộc) để điền TDS nhanh và chính xác hơn.
- Backend được sinh sau khi TDS đã Approved — đảm bảo khớp cả hợp đồng mock lẫn tài liệu kỹ thuật.

**Nguồn Frontend đã có sẵn (`03_Design/08_FrontEnd/Web/`) — AI dùng làm input để viết TDS:**

| Tài sản | Vai trò khi viết TDS |
| --- | --- |
| `src/lib/api/mock-server.ts` + `db.ts` | **§9 API Spec** trong TDS — 52 resource, path/method/JSON shape. Backend phải trả đúng JSON này. |
| `src/lib/mock-data.ts` (~6.000 dòng) | **§2 Traceability + §5 Static Modeling** — hàm nghiệp vụ thật, seed dữ liệu. Là nguồn seed Flyway. |
| `src/app/(app)/<role>/<module>/` | **§6 Dynamic Modeling + §8 Interface Spec** — luồng màn hình, props, state, gọi API ở đâu. |
| `03_Design/01_Diagrams/01_Class_Diagram/data_model.puml` (57 thực thể) | **§5 Data Structure** — schema ER với `<<PK>>`/`<<FK>>`, 7 ràng buộc C1–C7. |
| `scripts/semantic-audit.ts` (54 luật) | **§13 Test Scenarios** — luật nghiệp vụ port thành integration test. |

**Nguồn tài liệu nghiệp vụ (vẫn bắt buộc trích dẫn trong §2 Traceability):**

| Nguồn | Vai trò |
| --- | --- |
| `02_Requirements/02_UseCases/MFxx_*.md` | UC chuẩn — KHÔNG dùng số từ `Report 3.md` appendix |
| `02_Requirements/00_BusinessRules/business_rules.md` | BR chuẩn |
| `02_Requirements/00_BusinessRules/error_message.md` | Mã MSG chuẩn cho §10 Error Codes |
| `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` | Đối chiếu UC↔BR↔MSG |
| `03_Design/01_Diagrams/01_Class_Diagram/class_diagram_description.md` | OOP/thiết kế cho báo cáo |
| `06_Document/01_Report/Report 3.md` | Tham khảo phạm vi tổng thể |
| `03_Design/02_ADR/ADR-000.md` · `ADR-001.md` | Kiến trúc nền, stub external service |

**Quy tắc chống lệch (sửa 30/07/2026 — sau review TDS↔FE):**

Quy tắc gốc là *"nếu backend thấy cần khác hợp đồng mock, sửa **đồng thời** cả mock FE + backend"*. Vẫn giữ, **nhưng có một ngoại lệ đã được chốt**:

> Với **D1–D7** của `00_Common_Conventions.md`, mock được **cố ý đóng băng** ở bản cũ và **không** sửa theo. Lý do: `mock-server.ts` + `db.ts` + giá trị seed sẽ bị **xóa** khi backend thật chạy, nên sửa chúng cho khớp D1–D7 là làm hai lần. Việc chuyển đổi (`dd/MM/yyyy` → ISO, nhãn tiếng Việt → enum) làm **một lần** ở bước sinh Flyway seed. Chi tiết: `07_implement/review_fix_plan.md` mục "KHÔNG làm".

Ngoài phạm vi D1–D7, quy tắc gốc vẫn áp dụng nguyên: lệch tên field, lệch luồng, lệch resource ⇒ sửa cả hai phía cùng lúc.

**Hệ quả thực tế:** trong giai đoạn từ nay tới khi backend chạy, bản demo FE sẽ hiển thị dữ liệu **không khớp TDS** (ngày `dd/MM/yyyy`, enum tiếng Việt). Đây là **có chủ đích**, không phải bug. Nếu có buổi demo/nộp cần khớp, sửa seed **riêng màn đem demo**.

---

## 1. Team & RACI (giữ nguyên — chủ sở hữu module không đổi)

| Tên | Vai trò (Report 1) | RACI chính (Report 2) | Vai trò ở 07_implement |
| --- | --- | --- | --- |
| Nguyễn Văn Nam | Leader | System Architecture, Auth, Project Setup, CI/CD (A/R) | **Chủ** MF01, MF02, MF13; dựng nền chung (skeleton/auth/base) + Final approve & System Architecture Reviewer toàn bộ. |
| Nguyễn Đức Mạnh | Member | Database/ERD, Backend, API, Auth (R) | **Chủ** MF04, MF08, MF10; Lead Backend & DB Reviewer toàn bộ. |
| Bùi Quang Huy | Member | Requirement, Use Case Spec (R), Documentation (A/R) | **Chủ** MF03, MF06; Lead Requirements & Traceability Reviewer toàn bộ. |
| Trần Tuấn Minh | Member | UI/UX, Frontend, Flutter, Role-based UI (A/R) | **Chủ** MF09, MF11; Lead FE↔BE contract & UI/UX Reviewer toàn bộ. |
| Nguyễn Phương Thảo | Member | Test Planning/Unit/Integration/System/UAT (A/R) | **Chủ** MF05, MF07, MF12; Lead Test/QA Reviewer toàn bộ. |

Mỗi người viết TDS/TestSpec cho module mình phụ trách, đồng thời là Lead Reviewer đúng theo RACI chuyên môn trên tất cả module.

**Cơ chế review chéo:**
- **Mạnh** review Backend & Database (§5 Static Modeling, §9 API Spec, §10 Error Codes) toàn bộ.
- **Minh** review FE↔BE contract (JSON shape khớp `mock-server.ts`, §8 Interface Spec) toàn bộ.
- **Thảo** review Test (§13 Kịch bản kiểm thử, port `semantic-audit.ts` → integration test) toàn bộ.
- **Huy** review Traceability (§2 Ma trận Truy vết — UC↔BR↔MSG không bịa mã) toàn bộ.
- **Nam** review kiến trúc (§3 ADR, nền chung, auth) và Final Approval toàn bộ.

---

## 2. Cấu trúc thư mục & bộ tài liệu mỗi module

```
07_implement/
├── 00_Team_Work_Assignment_Plan.md      ← file này
├── MF01_User_Management_Access_Control/
│   ├── ADR-MF01_<TenQuyetDinh>.md       (chỉ viết nếu có quyết định kiến trúc mới)
│   ├── TDS-MF01_Backend.md              (17 mục theo PHASE-3_TDS.md)
│   ├── TDS-MF01_Frontend_Web.md         (8 mục — xem §2b dưới)
│   └── TestSpec-MF01_User_Management.md (theo PHASE-4_Test-Spec.md)
├── MF02_School_Class_Management/        (gộp Academic Year BR-191–195)
├── MF03_Student_Parent_Management/
├── MF04_Supplier_Partner_Management/
├── MF05_Menu_Diet_Management/
├── MF06_Ingredient_Inventory_Management/
├── MF07_Food_Safety_Inspection/         (gộp Violation-Process BR-183–187, 189–190)
├── MF08_Food_Sample_Retention/
├── MF09_Meal_Serving_Distribution/
├── MF10_Feedback_Announcement_Management/
├── MF11_Incident_Emergency_Management/  (gộp Violation-Staff BR-184, 186, 188)
├── MF12_AI_Supporting_Services/
└── MF13_Reporting_Analytics/
```

**Quy tắc đặt tên:** `ADR-MFxx_<TenQuyetDinh>.md`, `TDS-MFxx_Backend.md`, `TDS-MFxx_Frontend_Web.md`, `TestSpec-MFxx_<TenModule>.md`.

### 2a. Template tài liệu & cách AI điền từ mock

**Backend-TDS** (`06_Document/Templates/PHASE-3_TDS.md` — 17 mục EDS v2.0):

| Mục trong TDS | Nguồn AI đọc để điền |
|---|---|
| §1 Tổng quan Module | `mock-server.ts` comment đầu file + `class_diagram_description.md` |
| §2 Ma trận Truy vết (UC↔BR↔MSG) | `traceability_matrix.md` + `business_rules.md` + `error_message.md` |
| §3 ADR | **`07_implement/00_Common_Conventions.md` (D1–D7) — trỏ tới, BẮT BUỘC**, rồi `03_Design/02_ADR/ADR-000.md`, `ADR-001.md` — trích lại, không viết lại |
| §4 NFR/SLA | Mức chuẩn hệ thống trường học (p99 < 300ms, uptime 99.9%) |
| §5 Static Modeling (Class + Data) | `data_model.puml` (schema) + `class_diagram.puml` (OOP) |
| §6 Dynamic Modeling (Sequence + State) | `src/app/(app)/<role>/<module>/` — luồng màn hình → sequence diagram |
| §7 Domain Event Catalog | `mock-server.ts` các handler POST/PUT có audit() |
| §8 Interface Spec | `db.ts` + `hooks.ts` (usePagedList, useDetail, useCreate, useUpdate) |
| §9 API Spec | `mock-server.ts` — path, method, JSON shape đầy đủ. **Kèm annotation springdoc theo D8** (`@Tag`/`@Operation` có UC+BR/`@ApiResponse` trỏ `ErrorResponse`) — xem `00_Common_Conventions.md` D8 |
| §10 Error Codes | `error_message.md` **v1.5** — mã MSG tương ứng module. Mọi mã PHẢI trỏ một MSG có thật; **không** tự đặt "mã cục bộ TDS" |
| §11 Deployment Steps | Flyway migration từ `data_model.puml` |
| §12 Rollback Runbook | Chuẩn chung |
| §13 Test Scenarios | `semantic-audit.ts` — 54 luật port thành Gherkin |
| §16 Auth Matrix | `mock-server.ts` guard + `business_rules.md` RBAC 7 role |
| §17 AI Prompt Constraints | Tổng hợp từ tất cả nguồn trên |

**Frontend-Web-TDS** (8 mục rút gọn — Minh làm cho 13 module):
1. Tổng quan + danh sách màn hình liên quan (trỏ `src/app/(app)/<role>/<module>/`)
2. Component breakdown Next.js (props/state từ file `page.tsx`, `*-list.tsx`, v.v.)
3. State management & API integration mapping (endpoint nào gọi ở màn nào, khớp TDS §9)
4. Role-based rendering rules (khớp RBAC 7 role, BR-007)
5. Pagination/Search/Filter UI (khớp hợp đồng mục 2b — `usePagedList` params)
6. Error/loading/empty state mapping (khớp `error_message.md`)
7. Form validation rules (khớp DTO validation trong Backend-TDS §9)
8. Test hooks cho Thảo (`data-testid` convention)

**TestSpec** (`06_Document/Templates/PHASE-4_Test-Spec.md`) — **prompt mẫu ở §2c-3b**:
- Unit test: service layer (port logic từ `semantic-audit.ts`)
- Integration test: API endpoint → DB (happy path + error path theo `error_message.md` **v1.5**)
- Security test: RBAC guard (7 role × endpoint, theo §16 của TDS)
- **Nguồn chính là `TDS-{MF}_Backend.md` §13** (đã có sẵn ở cả 12 module) — mở rộng, không viết lại.
- **Nguồn oracle:** mọi expected value PHẢI trích từ BR/UC/MSG — không dùng giả định AI.
- **Định dạng expected value** theo D1–D7, **không** theo mock (mock đang cố ý đóng băng — xem §0).
- **Contract test OpenAPI (D8):** ≥1 test đọc `/v3/api-docs` assert 3 điều — không field `id` nào `format: uuid` (D1) · mọi 4xx/5xx trỏ `ErrorResponse` (D2) · mọi enum khớp bảng D6.

**ADR** (`06_Document/Templates/PHASE-2_ADR.md`):
- Chỉ viết khi có quyết định kiến trúc MỚI thực sự.
- Nếu module chỉ áp dụng lại ADR-001, ghi 1 dòng: *"Không có quyết định mới, áp dụng nguyên ADR-001 §X"* — không bịa quyết định.

---

## 2b. Hợp đồng phân trang / Search / Filter — bắt buộc đồng nhất toàn bộ module

> Mục này **dịch nguyên** hàm `paginate()` trong `mock-server.ts` (dòng 27–92) thành đặc tả backend.
> Nam dựng **một lần** trong `common/` (hoặc base controller); mọi module kế thừa — không tự làm lại.

### 2b-1. Response shape phân trang (bất biến cho mọi `GET /:resource`)

FE gọi qua `usePagedList<T>(resource, params)` → `GET /api/<resource>?page=&pageSize=&...`
Backend **phải** trả đúng JSON dưới đây, **không thiếu field nào**, **không thêm wrapper** (`data`, `result`…):

```jsonc
{
  "items":     [ /* mảng phần tử của trang hiện tại */ ],
  "total":     42,          // tổng bản ghi sau khi lọc (dùng để FE hiện "42 kết quả")
  "page":      1,           // trang hiện tại (1-indexed, đã clamp ≤ pageCount)
  "pageSize":  8,           // kích thước trang
  "pageCount": 6            // = ceil(total / pageSize), tối thiểu 1
}
```

**Interface TypeScript tham chiếu** (`client.ts` — không sửa):
```typescript
export interface Paged<T> {
  items:     T[];
  total:     number;
  page:      number;
  pageSize:  number;
  pageCount: number;
}
```

### 2b-2. Query params — backend phải đọc và xử lý

| Param | Kiểu | Default | Ý nghĩa |
|---|---|---|---|
| `page` | int ≥ 1 | `1` | Trang hiện tại (clamp về `pageCount` nếu vượt). |
| `pageSize` | int ≥ 1 | `8` | Số bản ghi/trang. |
| `search` | string | `""` | **Tìm mờ** trên `SEARCH_FIELDS` của module — `ILIKE %value%`. |
| `dateFrom` | `yyyy-MM-dd` | — | Lọc ≥ ngày bắt đầu trên field `date` / `collectedAt` / `sentAt` / `deliveryDate`. |
| `dateTo` | `yyyy-MM-dd` | — | Lọc ≤ ngày kết thúc (cùng field nhóm trên). |
| ~~`step`~~ | `1`\|`2`\|`3` | — | ⚠️ **KHÔNG hiện thực** — grep toàn FE ra **0 call-site** (MF07 CF-17, xác nhận lại 30/07/2026). Chỉ tồn tại trong `mock-server.ts`. Bỏ khỏi `CONTROL` set của backend. |
| ~~`stepStatus`~~ | string | — | ⚠️ **KHÔNG hiện thực** — cùng lý do trên. |
| `mealPrefix` | string | — | Lọc field `meal` dạng `startsWith` — **module MF08 (Food Sample Retention)**, không phải MF09. Dùng bởi `medical-staff/samples` và `service-caterers/samples` để lọc `FoodSample.meal` (VD `"Trưa 06/07"`). Sửa 30/07/2026 theo MF07 CF-17. |
| `<field>=<value>` | string | — | **Lọc chính xác** (exact match) theo field bất kỳ của entity (VD `status=ACTIVE`). |
| `not:<field>=<value>` | string | — | **Lọc phủ định** (VD tab "Chờ xử lý" = `not:healthStatus=VERIFIED`). |

> **"Các key điều khiển"** (không được dùng làm lọc field): `page`, `pageSize`, `search`, `dateFrom`, `dateTo`, `mealPrefix` — khớp hằng `control` trong mock **trừ** `step`/`stepStatus` đã bỏ (xem 2 dòng gạch ngang ở trên). Mock vẫn giữ 2 key đó trong `control` set; backend không cần.

### 2b-3. Thứ tự áp dụng bộ lọc (phải giữ đúng thứ tự này)

```
1. Lọc chính xác theo field (exact / not:field)       ← áp TRƯỚC search để thu nhỏ tập
2. Lọc khoảng ngày (dateFrom / dateTo)
3. Lọc bước kiểm thực (step + stepStatus)             ← chỉ module MF07
4. Lọc mealPrefix                                      ← chỉ module MF09
5. Tìm mờ search trên SEARCH_FIELDS của module         ← áp CUỐI, KHÔNG phải mọi cột
6. Đếm total (sau toàn bộ lọc)
7. Tính pageCount = max(1, ceil(total/pageSize))
8. Clamp page = min(page, pageCount)
9. Slice: OFFSET (page-1)*pageSize LIMIT pageSize
```

> **⚠️ Về bước 5 — tại sao KHÔNG search mọi cột String:**
> Mock (`mock-server.ts`) tìm mờ trên `Object.values(r)` vì in-memory, không tốn kém.
> Backend thật với PostgreSQL: `OR ILIKE %q%` trên 10–20 cột = **full table scan**, không dùng được index.
> **Quy tắc:** Mỗi module khai danh sách `SEARCH_FIELDS` cố định (3–6 cột có ý nghĩa với người dùng);
> search chỉ chạy trên các cột đó. FE không phụ thuộc vào cột cụ thể nào → không cần đổi hợp đồng.

**Danh sách `SEARCH_FIELDS` theo module — bản SỬA 30/07/2026:**

> ⚠️ **Bảng cũ sai 13/16 dòng.** Nó được viết bằng cách *đoán* tên field theo lối đặt tên thông dụng
> (`fullName`, `supplierName`, `batchCode`…) thay vì đọc `mock-data.ts`. MF06 **CF-M06-9** và MF07
> **CF-17** đã nêu một phần; rà lại toàn bảng bằng cách grep từng `interface` thì thấy sai gần hết.
> Hậu quả nếu để nguyên: `search` sinh SQL trên **cột không tồn tại** → lỗi runtime, hoặc im lặng
> không tìm ra gì.
>
> Bảng dưới đây được **verify trực tiếp** từ `03_Design/08_FrontEnd/Web/src/lib/mock-data.ts`.

| Module | Resource (`db.ts`) | Entity | SEARCH_FIELDS (đã verify) | Bảng cũ ghi sai |
|---|---|---|---|---|
| MF01 | `users` | `UserAccount` | `name`, `email`, `phone` | ❌ `username`, `fullName` không tồn tại |
| MF02 | `schools` | `SchoolRecord` | `name`, `address`, `email` | ❌ `principalName` không tồn tại |
| MF02 | `classrooms` | `Classroom` | `id`, `teacher` | ❌ `name` không tồn tại (**`id` chính là tên lớp**); `teacherName` → `teacher` |
| MF03 | `students` | `Student` | `id`, `name`, `class` | ❌ sai cả 3 (`fullName`/`studentCode`/`className`). `studentCode` chỉ có **sau** khi áp TDS-MF03 §9.2 |
| MF04 | `partners` | `Partner` | `name`, `contact`, `taxId` | ❌ `contactPerson`/`phone`/`email` không tồn tại — Partner gộp thành **`contact`** ("Tên — SĐT") |
| MF05 | `menus` | `MenuItem` | `name`, `grade`, `week` | ❌ `description` không tồn tại |
| MF05 | `dishes` | `Dish` | `name`, `category` | ✅ đúng |
| MF06 | `receiving` | `ReceivingSession` | `id`, `supplier`, `receivedBy` | ❌ `invoiceNo`/`supplierName` không tồn tại — **CF-M06-9** |
| MF06 | `inventory` | `InventoryBatch` | `id`, `ingredient`, `supplier` | ❌ `ingredientName`/`batchCode` không tồn tại; **mã lô chính là `id`** — CF-M06-9 |
| MF07 | `inspections` | `InspectionSession` | `id`, `inspector`, `dish` | ❌ `note` không tồn tại (có `staffNotes`) |
| MF08 | `samples` | `FoodSample` | `id`, `dish`, `meal`, `collectedBy` | ❌ `mealName` không tồn tại |
| MF09 | `student-distributions` | `StudentDistribution` | `name`, `class`, `assignedMeal` | ❌ `studentName`/`className` không tồn tại |
| MF10 | `feedbacks` | `Feedback` | `dish`, `comment` | ❌ `title`/`senderName`/`content` không tồn tại. ⚠️ **KHÔNG** đưa `student`/`studentId` vào SEARCH_FIELDS — **BR-145 ẩn danh** (ADR-M10-5) |
| MF11 | `incidents` | `Incident` | `id`, `title`, `reportedBy` | ❌ `description` không tồn tại (có `symptoms`); `reporterName` → `reportedBy` |
| MF12 | `error-logs` | `ErrorLog` | `message`, `module`, `path` | ❌ **không có entity `AIRequest`** trong mock — `prompt` không tồn tại ở đâu cả |
| MF12 | `audit-logs` | `AuditLog` | `actor`, `action`, `resource` | *(thiếu ở bảng cũ)* |
| MF13 | `sent-reports` | `SentReport` | `recipient`, `sentBy`, `rangeLabel` | ❌ `title`/`createdBy` không tồn tại (có `sentBy`) |

> **Lưu ý D1:** sau khi áp D1, `id` trong các dòng trên là **cột `code`** ở tầng DB. `SEARCH_FIELDS`
> khai theo tên field JSON; Specification map sang `code` khi build predicate.

### 2b-4. Cách implement trong Spring Boot (gợi ý chuẩn)

**Option A — Spring Data `Pageable` + `Specification<T>` (khuyên dùng, Nam dựng mẫu ở MF02):**

```java
// PagedResponse.java — record response, Nam đặt ở common/dto/
public record PagedResponse<T>(
    List<T> items,
    long    total,
    int     page,
    int     pageSize,
    int     pageCount
) {
    public static <T> PagedResponse<T> of(Page<T> p, int reqPage, int reqSize) {
        int pageCount = Math.max(1, p.getTotalPages());
        int safePage  = Math.min(reqPage, pageCount);
        return new PagedResponse<>(
            p.getContent(),
            p.getTotalElements(),
            safePage,
            reqSize,
            pageCount
        );
    }
}
```

```java
// Ví dụ controller (MF02 — School):
@GetMapping("/schools")
public PagedResponse<SchoolResponse> list(
    @RequestParam(defaultValue = "1")  int page,
    @RequestParam(defaultValue = "8")  int pageSize,
    @RequestParam(defaultValue = "")   String search,
    @RequestParam                      Map<String, String> allParams
) {
    Pageable pageable = PageRequest.of(page - 1, pageSize);   // Spring: 0-indexed
    Specification<School> spec = SchoolSpec.build(search, allParams);
    Page<School> result = schoolRepo.findAll(spec, pageable);
    return PagedResponse.of(result, page, pageSize);
}
```

**Xây `Specification` cho `search` + lọc field:**

```java
public class SchoolSpec {
  // Field ĐÃ VERIFY từ SchoolRecord trong mock-data.ts (xem bảng SEARCH_FIELDS ở trên).
  // KHÔNG dùng "principalName" — field đó không tồn tại (bản trước của tài liệu này ghi sai).
  private static final List<String> SEARCH_FIELDS = List.of("name", "address", "email");
  // "step"/"stepStatus" đã bỏ — 0 call-site trong FE (MF07 CF-17).
  private static final Set<String> CONTROL = Set.of(
      "page", "pageSize", "search", "dateFrom", "dateTo", "mealPrefix"
  );

  public static Specification<School> build(String search, Map<String, String> params) {
    return (root, query, cb) -> {
      List<Predicate> preds = new ArrayList<>();
      // 1. Lọc chính xác / phủ định
      for (var entry : params.entrySet()) {
        String key = entry.getKey(), val = entry.getValue();
        if (CONTROL.contains(key) || val == null || val.isBlank()) continue;
        if (key.startsWith("not:")) {
          preds.add(cb.notEqual(root.get(key.substring(4)).as(String.class), val));
        } else {
          preds.add(cb.equal(root.get(key).as(String.class), val));
        }
      }
      // 2. Tìm mờ search — OR trên SEARCH_FIELDS
      if (!search.isBlank()) {
        String like = "%" + search.toLowerCase() + "%";
        Predicate[] orPreds = SEARCH_FIELDS.stream()
            .map(f -> cb.like(cb.lower(root.get(f).as(String.class)), like))
            .toArray(Predicate[]::new);
        preds.add(cb.or(orPreds));
      }
      return cb.and(preds.toArray(new Predicate[0]));
    };
  }
}
```

**Option B — JPQL/native nếu Specification quá phức tạp (MF11, MF13):**
Dùng JPQL với `WHERE (:search = '' OR LOWER(e.name) LIKE %:search%)` — vẫn trả `PagedResponse` cùng shape.

### 2b-5. Lọc khoảng ngày (`dateFrom` / `dateTo`)

| Resource (mock key) | Module | Field ngày trong entity | Kiểu Java |
|---|---|---|---|
| `receiving` | MF06 | `deliveryDate` | `LocalDate` |
| `inspections` | MF07 | `date` | `LocalDate` |
| `kitchen-inspections` | MF07 | `date` | `LocalDate` |
| `samples` | MF08 | `collectedAt` | `Instant` |
| `sent-reports` | MF13 | `sentAt` | `Instant` |
| `student-distributions` | MF09 | `date` | `LocalDate` |

### 2b-6. Edge cases

| Tình huống | Hành vi mock | Backend phải làm tương tự |
|---|---|---|
| `page` vượt `pageCount` | clamp về `pageCount`, trả trang cuối | `safePage = min(page, pageCount)` sau khi đếm total. |
| Tập rỗng sau lọc | `pageCount=1`, `page=1`, `items=[]`, `total=0` | Không trả 404 — vẫn trả 200 với shape đầy đủ. |
| `pageSize=0` hoặc âm | clamp về 1 | `pageSize = Math.max(1, pageSize)`. |
| `search=""` | bỏ qua | `if (!search.isBlank())` trước khi xây predicate. |
| `<field>=` (value rỗng) | bỏ qua | `if (val == null \|\| val.isBlank()) continue`. |

### 2b-7. Checklist review cho Mạnh (Lead Backend reviewer)

Mỗi module P1/P0 khi review endpoint danh sách, kiểm tra:
- [ ] Response có đủ 5 field: `items`, `total`, `page`, `pageSize`, `pageCount`?
- [ ] `page` trong response đã clamp (không lớn hơn `pageCount`)?
- [ ] `pageCount` tối thiểu là `1` kể cả khi `total=0`?
- [ ] Param `page` truyền vào Spring là `page - 1` (0-indexed)?
- [ ] Lọc exact field dùng `Map<String, String> allParams` (không hardcode từng param lẻ)?
- [ ] `search` **chỉ** chạy trên `SEARCH_FIELDS` đã khai — không OR toàn bộ cột String?
- [ ] `search` dùng `ILIKE` / `cb.like(cb.lower(...))` — không case-sensitive?
- [ ] Tập rỗng sau lọc → trả `200 { items:[], total:0, page:1, pageSize:8, pageCount:1 }`, không phải `404`?

---

## 2c. Prompt mẫu để nhờ AI/người sinh TDS cho từng module (copy-paste, kèm trích dẫn UC/BR)

> Mục này giải quyết vấn đề: khi nhờ AI (hoặc thành viên khác) sinh TDS cho **một MF cụ thể**, cần
> (1) trỏ đúng nguồn của riêng module đó — không lẫn module khác, và
> (2) **BẮT BUỘC** trích dẫn đúng mã UC/BR để dùng cho report nộp — không để AI tự bịa số.
>
> Nguồn UC↔BR chuẩn duy nhất: `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` §1.x
> (không dùng số UC từ `Report 3.md` phụ lục hay từ file `UC-MF01-001-Parent-Login.md` — file này là bản
> nháp cũ, đã được thay bằng UC-034 "Login" trong bảng §1.1 của traceability matrix).

### 2c-0. CÁCH DÙNG — bảng điền sẵn, copy đúng dòng của mình (thêm 30/07/2026)

> **Đọc mục này trước.** Nó tồn tại để bạn **không phải tra chéo hai bảng** và **không có cơ hội suy sai** `{§ TRACE_SECTION}`.

**Ba bước:**

1. Tìm dòng module của mình ở bảng dưới.
2. Copy nguyên khối prompt — **§2c-2** nếu viết Backend-TDS, **§2c-3b** nếu viết TestSpec — rồi thay 3 placeholder bằng đúng 3 giá trị ở dòng đó.
3. Dán vào **một phiên AI mới, sạch**. Dùng **Opus**. **Một module một phiên** — không ôm nhiều MF (xem §2c-4).

**⚠️ Cái bẫy mà bảng này tồn tại để chặn:** thứ tự `traceability_matrix.md` §1.x **lệch** với thứ tự MFxx ở **đúng một cặp** — `MF05` là **§1.6**, `MF06` là **§1.5** (đảo nhau). Suy `MF05 → §1.5` là suy hợp lý nhất và **sai**: bạn sẽ lấy UC/BR của *kho nguyên liệu* gán vào tài liệu *thực đơn*. Mã UC trông hợp lệ, tra được trong matrix, nhưng thuộc module khác — loại lỗi khó bắt nhất khi review, và nó đi thẳng vào tài liệu nộp báo cáo.

| MF | Chủ | `{MF}` | `{Tên module}` | `{§ TRACE_SECTION}` | File output TestSpec |
|---|---|---|---|---|---|
| MF01 | Nam | `MF01` | User Management & Access Control | `§1.1` | `TestSpec-MF01_User_Management_Access_Control.md` |
| MF02 | Nam | `MF02` | School & Class Management | `§1.2` | `TestSpec-MF02_School_Class_Management.md` |
| MF03 | Huy | `MF03` | Student & Parent Management | `§1.3` | `TestSpec-MF03_Student_Parent_Management.md` |
| MF04 | Mạnh | `MF04` | Supplier & Partner Management | `§1.4` | `TestSpec-MF04_Supplier_Partner_Management.md` |
| **MF05** | Thảo | `MF05` | Menu & Diet Management | **`§1.6`** ⚠️ *không phải §1.5* | `TestSpec-MF05_Menu_Diet_Management.md` |
| **MF06** | Huy | `MF06` | Ingredient & Inventory Management | **`§1.5`** ⚠️ *không phải §1.6* | `TestSpec-MF06_Ingredient_Inventory_Management.md` |
| MF07 | Thảo | `MF07` | Food Safety Inspection & Monitoring | `§1.7` | `TestSpec-MF07_Food_Safety_Inspection.md` |
| MF08 | Mạnh | `MF08` | Food Sample Retention | `§1.8` | `TestSpec-MF08_Food_Sample_Retention.md` |
| MF09 | Minh | `MF09` | Meal Serving & Distribution | `§1.9` | `TestSpec-MF09_Meal_Serving_Distribution.md` |
| MF10 | Mạnh | `MF10` | Feedback & Announcement Management | `§1.10` | `TestSpec-MF10_Feedback_Announcement_Management.md` |
| MF11 | Minh | `MF11` | Incident & Emergency Management | `§1.11` | `TestSpec-MF11_Incident_Emergency_Management.md` |
| MF12 | Thảo | `MF12` | AI Supporting Services | `§1.12` | `TestSpec-MF12_AI_Supporting_Services.md` |
| MF13 | Nam | `MF13` | Reporting & Analytics | `§1.13` | `TestSpec-MF13_Reporting_Analytics.md` — ✅ **ĐÃ CÓ, Approved 2026-08-04** *(ô cũ ghi ⛔ "chưa làm được" là **SAI** — xem ghi chú dưới)* |

> File output đặt trong thư mục module: `07_implement/<thư mục MF>/`. Tên file dùng **hậu tố trùng tên thư mục** (ví dụ `TestSpec-MF01_User_Management_Access_Control.md`) — đây là cách đọc đúng của quy ước `TestSpec-MFxx_<TenModule>.md` ở §2; ví dụ viết tắt `TestSpec-MF01_User_Management.md` ở §2a chỉ là minh họa, dùng bảng này làm chuẩn.
>
> **Hai lệch tên cần biết** (đã đối chiếu với đĩa, không phải lỗi):
> - Thư mục MF07 là `MF07_Food_Safety_Inspection` nhưng **file UC** là `MF07_Food_Safety_Inspection_Monitoring.md`.
> - Tên module MF02/MF07/MF11 ở §2c-1 có kèm phần gộp trong ngoặc (`+ Academic Year`, `+ Violation-Process`, `+ Violation-Staff`) — không đưa vào `{Tên module}`, giữ tên gọn như bảng trên.

~~**⛔ MF13 chưa viết TestSpec được.** TestSpec lấy expected value từ `TDS-{MF}_Backend.md` §13/§9/§10/§16, mà **MF13 chưa có Backend TDS** (12/13 module có). Phải viết `TDS-MF13_Backend.md` trước… Trước khi viết, cần chốt **CF-M11-20**: MF11 đã kết luận `sent-reports` thuộc **MF13** chứ không phải MF11, và cần Huy xác nhận **UC-189** nằm ở §1.13.~~

> ✅ **ĐOẠN TRÊN ĐÃ LỖI THỜI Ở CẢ HAI VẾ — sửa 2026-08-04 (Nam, Bước 1 của MF13).** Giữ nguyên văn theo Policy 4.4 vì nó minh hoạ đúng lớp lỗi mà chính file này cảnh báo: *tài liệu mô tả một đằng, code/đĩa làm một nẻo*.
>
> 1. **Vế "chưa có Backend TDS" — sai từ 30/07.** `TDS-MF13_Backend.md` **có tồn tại** (~1.900 dòng, 3 bảng, 20 path, `CF-M13-1…9`) và `TestSpec-MF13_Reporting_Analytics.md` cũng vậy (~1.900 dòng, 37 TC). `02_Module_Completion_Workflow.md` §3 đã ghi nhận điều này khi làm Bước 0 của MF12. Cả hai nay **Approved 2026-08-04** (v1.1 / v1.1).
> 2. **Vế "cần chốt CF-M11-20" — không còn gì để chốt.** **Code đã trả lời, không cần cuộc họp nào:** MF12 merge `V23__mf12_platform_services.sql:115` `CREATE TABLE sent_reports` + `mf12/domain/SentReport` + `mf12/web/SentReportController` (6 endpoint trên `/api/v1/sent-reports`). ⇒ **`sent-reports` thuộc MF12**, đúng như `traceability_matrix.md` §1.12 và `TDS-MF13` `ADR-M13-2` (viết 30/07) đã kết luận. **`CF-M11-20` ĐÓNG** — kết luận của nó ("thuộc MF13") là **sai**. MF13 sở hữu `report_exports` + `report_schedules` + `report_schedule_runs` — ba khái niệm khác.
>
> Quyết định đầy đủ: `TDS-MF13_Backend.md` §2.4 (`M13-D01`…`M13-D13`).

**Nhắc lại thứ tự ưu tiên nguồn** (cả hai prompt đều đã ghi, để đây cho khỏi quên):
`00_Common_Conventions.md` (D1–D7) **>** `TDS-{MF}_Backend.md` **>** BR/UC/MSG **>** mock FE *(mock chỉ còn là nguồn tên field / luồng / seed — **không** phải nguồn định dạng ngày, giá trị enum, shape lỗi, chiến lược `id`)*.

---

### 2c-1. Bảng tra cứu MF → UC file → mục Traceability Matrix

> ⚠️ **Thứ tự trong `traceability_matrix.md` §1.x LỆCH với thứ tự MFxx** ở mục 2 — đã đối chiếu và ghi rõ dưới đây. Dùng đúng cột "§ Traceability", KHÔNG suy luận `MF05 → §1.5`.

| MF | Tên module | UC file (`02_Requirements/02_UseCases/`) | § Traceability (`traceability_matrix.md`) |
|---|---|---|---|
| MF01 | User Management & Access Control | `MF01_User_Management_Access_Control.md` | §1.1 |
| MF02 | School & Class Management (+ Academic Year) | `MF02_School_Class_Management.md` | §1.2 |
| MF03 | Student & Parent Management | `MF03_Student_Parent_Management.md` | §1.3 |
| MF04 | Supplier & Partner Management | `MF04_Supplier_Partner_Management.md` | §1.4 |
| MF05 | Menu & Diet Management | `MF05_Menu_Diet_Management.md` | **§1.6** (không phải §1.5) |
| MF06 | Ingredient & Inventory Management | `MF06_Ingredient_Inventory_Management.md` | **§1.5** (không phải §1.6) |
| MF07 | Food Safety Inspection (+ Violation-Process) | `MF07_Food_Safety_Inspection_Monitoring.md` | §1.7 |
| MF08 | Food Sample Retention | `MF08_Food_Sample_Retention.md` | §1.8 |
| MF09 | Meal Serving & Distribution | `MF09_Meal_Serving_Distribution.md` | §1.9 |
| MF10 | Feedback & Announcement Management | `MF10_Feedback_Announcement_Management.md` | §1.10 |
| MF11 | Incident & Emergency Management (+ Violation-Staff) | `MF11_Incident_Emergency_Management.md` | §1.11 |
| MF12 | AI Supporting Services | `MF12_AI_Supporting_Services.md` | §1.12 |
| MF13 | Reporting & Analytics | `MF13_Reporting_Analytics.md` | §1.13 |

> **Sửa 30/07/2026:** dòng MF13 trước đây ghi *"không có file UC riêng — dùng thẳng §1.13"* — **sai**. File `02_Requirements/02_UseCases/MF13_Reporting_Analytics.md` **có tồn tại** (48 dòng, UC-171/UC-172/UC-173). Vẫn đọc file UC như 12 module còn lại.

### 2c-2. Prompt mẫu (điền `{MF}` rồi copy nguyên khối cho AI)

```
Vai trò: bạn viết Backend-TDS cho module {MF} — {Tên module}, theo template
06_Document/Templates/PHASE-3_TDS.md (17 mục, EDS v2.0).

NGUỒN BẮT BUỘC ĐỌC TRƯỚC KHI VIẾT (không tự suy diễn ngoài các nguồn này):
-1. 🔴 CODE THẬT — ƯU TIÊN CAO NHẤT, CAO HƠN CẢ TÀI LIỆU (từ 02/08/2026, khi MF01+MF02 đã chạy):
    Đọc 00_Common_Conventions.md §0-BIS TRƯỚC, rồi MỞ THẬT các file sau, đừng chỉ tin mô tả:
      04_Development/Backend/src/main/java/com/sfms/common/   (11 hạ tầng dùng chung — dùng, đừng viết lại)
      04_Development/Backend/src/main/java/com/sfms/mf02/     (module mẫu: web/ dto/ domain/)
      04_Development/Backend/src/main/resources/db/migration/ (ls để biết số V nào còn trống)
    Quy tắc: tài liệu mô tả một đằng mà code làm một nẻo ⇒ CODE ĐÚNG, sửa tài liệu.
    Package = com.sfms.mf<NN> (com.sfms.iam của MF01 là ngoại lệ lịch sử, KHÔNG bắt chước).
    Entity extends BaseEntity — KHÔNG tự khai created_at/updated_at/created_by/updated_by (D9),
    nhưng DDL VẪN phải tạo đủ 4 cột đó. Đây là lỗi số 1 làm build đỏ ngay.
0. ⚠️ QUY ƯỚC CHUNG: 07_implement/00_Common_Conventions.md (D1–D10; D1–D7 Approved 30/07, D9–D10 Approved 31/07)
   Đây là HỢP ĐỒNG ĐÃ CHỐT cho cả 13 module, ưu tiên CAO NHẤT:
     D1 định danh (`id` JSON + path param = MÃ NGHIỆP VỤ, UUID chỉ là PK nội bộ)
     D2 envelope lỗi `{ "error": { code, message, details } }`
     D3 tiền tố `/api/v1`
     D4 ngày/giờ JSON LUÔN ISO-8601 (KHÔNG `@JsonFormat("dd/MM/yyyy")`)
     D5 nhận `body.id` client gửi → kiểm trùng → 409
     D6 8 enum dùng chung ở `common/enums/`, tất cả SCREAMING_SNAKE
     D7 thông điệp lỗi tiếng Anh nguyên văn `error_message.md`
     D8 annotation OpenAPI/springdoc — 8 luật S1–S8 (⏳ đề xuất 30/07, chờ Nam approve; VẪN LÀM,
        vì annotation không xung đột gì với D1–D7)
   — Thứ tự ưu tiên khi xung đột: 00_Common_Conventions.md > ADR module (§3) > hợp đồng mock FE.
   — TUYỆT ĐỐI KHÔNG tự khai lại 8 enum của D6 trong module; trích dẫn `common/enums/`.
1. UC chuẩn:  02_Requirements/02_UseCases/{UC_FILE}
2. UC↔BR↔MSG: 02_Requirements/03_TraceabilityMatrix/traceability_matrix.md {§ TRACE_SECTION}
   — CHỈ dùng đúng UC ID, BR ID, MSG ID xuất hiện trong bảng của mục này.
   — Nếu cần một UC/BR không có trong bảng, DỪNG LẠI và báo cho tôi, KHÔNG tự đặt mã mới.
3. BR nội dung đầy đủ: 02_Requirements/00_BusinessRules/business_rules.md
   — tra theo đúng mã BR đã lấy ở bước 2, KHÔNG chép nhầm BR của module khác.
4. Mã lỗi:     02_Requirements/00_BusinessRules/error_message.md (**v1.5** — đã có MSG-104–138)
   — Nếu cần một mã lỗi chưa có MSG, DỪNG LẠI và báo cho tôi. KHÔNG tự đặt "mã cục bộ TDS"
     như các bản trước (đã phải gom lại 35 mã ở v1.5 — xem 07_implement/review.md mục A dòng 7).
5. Hợp đồng API mock: 03_Design/08_FrontEnd/Web/src/lib/api/mock-server.ts + db.ts
   — lọc đúng resource của module {MF} (path bắt đầu bằng nhóm tài nguyên tương ứng).
   ⚠️ PHẠM VI CỦA MOCK ĐÃ THU HẸP TỪ 30/07/2026 — mock là nguồn cho:
       ✅ path/method, TÊN field, luồng nghiệp vụ, quan hệ giữa các resource
     mock KHÔNG CÒN là nguồn cho (D1–D7 thắng, mock đang cố ý bị đóng băng ở bản cũ):
       ❌ định dạng ngày — mock là `dd/MM/yyyy`, hợp đồng là ISO (D4)
       ❌ giá trị enum — mock có nhãn tiếng Việt/tiếng Anh lẫn lộn, hợp đồng là SCREAMING_SNAKE (D6)
       ❌ shape lỗi — mock trả `{message}` phẳng, hợp đồng là envelope (D2)
       ❌ chiến lược định danh — theo D1
   — Khi mock lệch D1–D7: theo D1–D7 và GHI RÕ chỗ lệch thành 1 dòng trong §17, KHÔNG im lặng theo mock.
6. Luồng màn hình: 03_Design/08_FrontEnd/Web/src/app/(app)/<role>/<module>/ — liệt kê rõ role/thư mục đã đọc.
7. Schema:     03_Design/01_Diagrams/01_Class_Diagram/data_model.puml (chỉ các entity liên quan module {MF})
   ⚠️ File này CHƯA được cập nhật theo một số ADR đã chốt (VD `IssueSlip` của ADR-M06-6 chưa có trong
     puml). Nếu thấy ADR module mâu thuẫn puml, theo ADR và ghi rõ để Huy/Mạnh sync puml.
8. Test rule:  03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts (chỉ luật liên quan module {MF})
9. ADR nền:    03_Design/02_ADR/ADR-000.md, ADR-001.md (trích lại, không viết lại)
10. Stack thật của dự án: 07_implement/01_MVP_Backend_Implementation_Plan.md §Stack (dòng "Stack (ADR-000)")

⚠️ RÀNG BUỘC STACK — BẮT BUỘC (PHASE-3_TDS.md chỉ là template minh họa, KHÔNG dùng nguyên cú pháp mẫu của nó):
Dự án dùng Spring Boot 4.1.x (Java 25 LTS) · PostgreSQL 16 · Flyway · Spring Security + JWT · springdoc-OpenAPI (Swagger) · MapStruct.
- §5.2 Data Structure: viết Entity JPA (`@Entity`, `@Table`, `@Id`...) + đoạn SQL Flyway migration — TUYỆT ĐỐI KHÔNG dùng
  Prisma schema (`model X { ... }` là cú pháp Node.js/Prisma, sai stack).
- §7.3 Payload Schema, §8.1/§8.2 Interface Spec: viết bằng Java (interface/class, `Optional<T>`, kiểu dữ liệu Java) —
  TUYỆT ĐỐI KHÔNG dùng TypeScript `export interface ...`.
- §11.3 Implementation Steps: lệnh migration dùng Flyway (`V{n}__<mo_ta>.sql`, chạy qua `mvn flyway:migrate` hoặc tự
  chạy khi Spring Boot khởi động) — TUYỆT ĐỐI KHÔNG dùng `npx prisma migrate`.
- Nếu bất kỳ mục nào trong 17 mục của template có ví dụ mẫu bằng ngôn ngữ khác Java/SQL, PHẢI dịch sang Java/SQL
  trước khi điền — không copy nguyên cú pháp minh họa của template.

YÊU CẦU BẮT BUỘC (để dùng làm tài liệu nộp báo cáo):
- §3 PHẢI mở đầu bằng khối trỏ `07_implement/00_Common_Conventions.md` (D1–D7) — copy đúng khuôn đã có
  ở §3 của 12 TDS hiện tại. ADR module chỉ ghi phần RIÊNG của module.
- §2 Ma trận Truy vết PHẢI liệt kê từng UC ID kèm TÊN UC (đúng nguyên văn nguồn 1), cột Business Rules
  PHẢI ghi đúng mã BR (đúng nguyên văn nguồn 2/3) — KHÔNG được để trống, KHÔNG được bịa mã.
- Mỗi mục TDS lấy TÊN FIELD THẬT từ mock — không đặt tên field tưởng tượng. Nhưng ĐỊNH DẠNG và GIÁ TRỊ
  (ngày, enum, envelope lỗi, id) theo D1–D7, không theo mock (xem nguồn 5).
- §5.2 mỗi bảng PHẢI có `code VARCHAR(32)` + `UNIQUE(school_id, code)` và `@JsonProperty("id")` (D1).
- §10 mọi mã lỗi PHẢI ánh xạ về một MSG có thật trong `error_message.md` v1.5 — cột "MSG nguồn" không
  được để `*(dẫn xuất BR-xxx)*` hay bỏ trống.
- §9.2 mỗi endpoint PHẢI kèm annotation springdoc theo D8: `@Tag` cấp controller · `@Operation` có
  `description` chứa UC ID + BR ID · một `@ApiResponse` cho MỖI mã lỗi khai ở §10, `content` trỏ
  `ErrorResponse` dùng chung. Field ngày khai `format = "date"/"date-time"`; enum khai bằng kiểu enum
  thật ở `common/enums/` (KHÔNG `@Schema(allowableValues = ...)`); `example` của `id` là mã nghiệp vụ,
  KHÔNG UUID. §10 phải có cột HTTP status khớp 1-1 với các `@ApiResponse` ở §9.2.
- §11 Deployment PHẢI có 1 bước tải `/v3/api-docs` lưu kèm tài liệu nộp (bằng chứng nghiệm thu D8).
- Nếu mock và UC/BR mâu thuẫn nhau (mock làm khác BR mô tả), GHI RÕ mâu thuẫn đó thành 1 dòng riêng
  trong §2 hoặc §17, không tự ý chọn bên nào đúng.
- Cuối tài liệu, liệt kê lại đầy đủ danh sách mã UC + mã BR đã trích dẫn (để đối chiếu nhanh khi review).

Output: file TDS-{MF}_Backend.md, đặt tại 07_implement/{MF}_<TenModule>/
```

### 2c-3. Checklist nhận trước khi coi TDS module là "đã điền xong"

**Quy ước chung D1–D7 (bắt buộc — thêm 30/07/2026, Nam xác nhận):**

- [ ] §3 có khối trỏ `00_Common_Conventions.md` ở đầu mục.
- [ ] **D1** — §5.2 mọi bảng có `code VARCHAR(32)` + `UNIQUE(school_id, code)`; §9.2 path param là `{code}`; JSON `id` là mã nghiệp vụ, **không** trả kèm UUID.
- [ ] **D4** — grep `dd/MM/yyyy` và `@JsonFormat` trong tài liệu → phải ra **0** kết quả ở §9.
- [ ] **D6** — grep 8 enum của D6 → mọi giá trị là SCREAMING_SNAKE; module **không** tự khai lại enum đã có ở D6.
- [ ] **D2 + D7** — §10 mọi dòng có "MSG nguồn" trỏ một MSG **có thật** trong `error_message.md` v1.5; nội dung là **tiếng Anh** nguyên văn; không còn `*(dẫn xuất BR-xxx)*`.
- [ ] Mọi chỗ tài liệu cố ý làm khác mock (do D1–D7) đã được ghi thành 1 dòng ở §17 — không im lặng.
- [ ] **D8** *(⏳ chờ Nam approve — kiểm nhưng chưa reject vì mục này)* — §9.2 mỗi endpoint có `@Tag` + `@Operation` (description chứa UC+BR) + `@ApiResponse` cho mọi mã lỗi §10; §10 có cột HTTP status. Grep 3 cái sau trong tài liệu:
  - `@Schema(allowableValues` → phải ra **0** (enum khai bằng kiểu thật, S5)
  - `example` của field `id` → **không** dòng nào là UUID (S6)
  - `@JsonFormat` → vẫn phải **0** (D4, đã có ở trên)

**Traceability & stack (đã có từ trước):**

- [ ] §2 Traceability: mọi UC ID đều tồn tại trong đúng mục § của `traceability_matrix.md` ở bảng 2c-1 (Huy xác nhận).
- [ ] Không có UC/BR nào lấy từ `Report 3.md` phụ lục hoặc file UC rời rạc ngoài `02_UseCases/MFxx_*.md`.
- [ ] Danh sách UC+BR trích dẫn cuối tài liệu khớp 100% với những gì dùng trong §2 (không thiếu, không thừa).
- [ ] §9 API Spec khớp đúng **resource + tên field** của module trong `mock-server.ts` (Minh xác nhận) — định dạng/giá trị theo D1–D7.
- [ ] §5.2/§7.3/§8/§11 dùng đúng Java + Flyway (KHÔNG còn sót TypeScript `interface` hay Prisma `model { }` copy
  nguyên từ template PHASE-3_TDS.md — Mạnh xác nhận).

### 2c-3b. Prompt mẫu sinh `TestSpec-{MF}_<TenModule>.md` (thêm 30/07/2026)

> Trước bản này **không có** prompt cho TestSpec — chỉ có prompt Backend-TDS (§2c-2). Điền `{MF}` rồi copy nguyên khối.
>
> Khác biệt cốt lõi so với §2c-2: TestSpec **không đọc lại mock để suy ra hợp đồng**. Hợp đồng đã chốt ở TDS §9 và `00_Common_Conventions.md`. Mock chỉ dùng làm **nguồn seed/fixture**.

```
Vai trò: bạn viết TestSpec cho module {MF} — {Tên module}, theo template
06_Document/Templates/PHASE-4_Test-Spec.md.

NGUỒN BẮT BUỘC (thứ tự ưu tiên khi xung đột — cao xuống thấp):
-1. 🔴 CODE THẬT ở 04_Development/Backend/ — cao hơn tài liệu (xem 00_Common_Conventions.md §0-BIS).
    Đường dẫn test viết ĐỦ TỪ GỐC REPO: 04_Development/Backend/src/test/java/com/sfms/mf<NN>/...
    Package com.sfms.mf<NN>. Lệnh chạy: mvn -f 04_Development/Backend/pom.xml verify
    Testcontainers chạy Flyway THẬT: V1(MF01) + V2 + V3(MF02) + V<n> của module bạn.
    Trước khi viết fixture, MỞ src/main/resources/db/migration/ xem bảng nào đã tồn tại.
0. 07_implement/00_Common_Conventions.md — D1–D10. Mọi expected value về
   ĐỊNH DẠNG phải theo đây: ngày ISO-8601 (D4), enum SCREAMING_SNAKE (D6), envelope lỗi
   { "error": { code, message, details } } (D2), `id` = mã nghiệp vụ (D1), path `/api/v1` (D3).
   D1 áp cho CẢ KHÓA NGOẠI trong JSON: "classroomId": "3A", KHÔNG phải UUID.
   + D8 annotation OpenAPI (S1–S8) — đã hiện diện trong code MF01/MF02, dùng cho contract test dưới.
   + D9 audit tự động qua BaseEntity — test assert created_by tự điền, service KHÔNG gán tay.
1. 07_implement/{MF}_<TenModule>/TDS-{MF}_Backend.md — ĐÂY LÀ NGUỒN CHÍNH:
   §13 Kịch bản Kiểm thử (đã có sẵn, dùng làm xương sống — mở rộng, không viết lại từ đầu)
   §9  API Spec  → expected request/response của integration test
   §10 Bảng mã lỗi → expected của error path (mã module + MSG)
   §16 Authorization Matrix → ma trận security test (7 role × endpoint)
   §5.2 DDL + CHECK constraint → assertion tầng DB
2. 02_Requirements/00_BusinessRules/business_rules.md — nội dung BR làm ORACLE.
3. 02_Requirements/00_BusinessRules/error_message.md (v1.5) — nội dung MSG nguyên văn làm expected
   message. Mã MSG-104–138 là mã mới cấp cho các mã lỗi cục bộ; dùng đúng, không tự đặt mã khác.
4. 02_Requirements/03_TraceabilityMatrix/traceability_matrix.md {§ TRACE_SECTION} — mỗi test case
   phải truy được về ≥1 UC ID.
5. 03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts — port luật liên quan {MF} sang Gherkin.
   ⚠️ Script có 54 luật đăng ký qua check() + 3 luật chỉ có add() không có check()
     (PHIEU_TRO_MAU_KHONG_TON_TAI, MOC_TRO_SU_CO_KHONG_TON_TAI, LO_TRO_MAT_HANG_KHONG_TON_TAI)
     — cả 57 đều là luật thật, con số "54" chỉ là số check() đăng ký.
   ⚠️ Tên luật PHẢI copy nguyên văn từ script, KHÔNG lấy từ TDS §13 (TDS từng trích tên lạc hậu —
     VD HAN_LUU_KHONG_DU_72H đã đổi thành HAN_LUU_KHONG_DU_24H ngày 30/07/2026).
6. 03_Design/08_FrontEnd/Web/src/lib/mock-data.ts — CHỈ dùng làm nguồn SEED/FIXTURE (mã bản ghi
   thật như HS-3A-001, SUP-001, SMP-20260706-01 để test case chạy được trên dữ liệu có thật).
   ⚠️ KHÔNG lấy định dạng ngày / giá trị enum từ đây — mock đang cố ý đóng băng ở bản cũ, xem §0.

YÊU CẦU BẮT BUỘC:
- Mỗi test case phải có ORACLE trích dẫn được: ghi rõ BR/UC/MSG nào quy định expected value đó.
  KHÔNG dùng giả định. Nếu TDS §13 nêu kịch bản mà không có BR/MSG chống lưng, GHI RÕ là "thiếu
  oracle" và báo lại, KHÔNG tự bịa expected.
- Bắt buộc có ĐỦ 4 lớp: Unit (service layer) · Integration (endpoint → DB, happy + error path)
  · Security (RBAC theo §16 TDS, 7 role) · Contract (OpenAPI, xem dưới).
- Lớp Contract (D8) — ít nhất 1 test tải GET /v3/api-docs rồi assert:
    (a) không field tên `id` nào có `format: uuid`            → D1
    (b) mọi response 4xx/5xx đều $ref tới schema ErrorResponse → D2
    (c) mọi enum trong spec khớp đúng bảng 8 enum của D6       → D6
    (d) mọi field ngày có format "date" hoặc "date-time"       → D4
  Ba/bốn assertion này thay được phần lớn checklist tay, và là thứ chặn lệch quay lại qua
  file type FE sinh tự động (review_fix_plan.md T3.4 `openapi-typescript`).
  Nếu endpoint của {MF} chưa có annotation, GHI RÕ là "thiếu annotation D8" và báo lại.
- Error path: expected body PHẢI đúng envelope D2 và `code` PHẢI khớp §10 của TDS.
- Với module có GATE nghiệp vụ enforce phía server, PHẢI có test "gọi API trực tiếp bỏ qua UI":
  đặc biệt MF07 (BR-094/095/100/211), MF08 (BR-105 inspectionId, BR-203 amount/unit),
  MF02 (BR-025 capacity), MF10 (BR-139 mealSessionId, BR-142 illnessKeyword tính ở server).
  Đây là nhóm lỗi mà review 29/07 xếp nghiêm trọng nhất: gate chỉ tồn tại ở client thì gọi API
  trực tiếp là lách được toàn bộ luật ATTP.
- Cuối tài liệu liệt kê: danh sách UC/BR/MSG đã trích + danh sách tên luật semantic-audit đã port.

Output: file TestSpec-{MF}_<TenModule>.md tại 07_implement/{MF}_<TenModule>/
```

### 2c-4. Chọn model AI khi sinh tài liệu

> Sinh Backend-TDS/TestSpec là việc rủi ro cao (không được bịa mã UC/BR, phải đối chiếu nhiều nguồn dài cùng lúc) — ưu tiên model mạnh nhất cho phần này, hạ xuống model rẻ hơn ở phần ít rủi ro.

| Tài liệu | Model đề xuất | Lý do |
|---|---|---|
| `TDS-MFxx_Backend.md` (17 mục, §2 Traceability, §9 API Spec) | **Opus** (mạnh nhất hiện có) | Rủi ro bịa mã UC/BR cao nhất; phải đọc đồng thời nhiều nguồn dài (`mock-data.ts` ~6.000 dòng, `business_rules.md`, `traceability_matrix.md`, `mock-server.ts`, `semantic-audit.ts`) |
| `TestSpec-MFxx_<Module>.md` (port `semantic-audit.ts` → Gherkin) | **Opus** | Oracle phải khớp đúng BR/MSG — sai là test vô nghĩa |
| `TDS-MFxx_Frontend_Web.md` (8 mục rút gọn) | Sonnet đủ dùng | Chủ yếu mô tả lại code FE đã có sẵn, ít trích dẫn UC/BR |
| `ADR-MFxx_<Decision>.md` (chỉ khi có quyết định mới) | Sonnet đủ dùng | Ngắn, ít nguồn phải đối chiếu |

**Lưu ý khi chạy:** không ôm nhiều module trong 1 phiên AI — dễ vượt context hoặc lẫn nguồn giữa các MF (đặc biệt cặp MF05/MF06 đã lệch thứ tự so với `traceability_matrix.md`, xem bảng 2c-1). Mỗi phiên nên bắt đầu sạch, chỉ nạp đúng nguồn của 1 module theo prompt mẫu ở §2c-2.

### 2c-5. ~~Prompt sinh lại `TDS-MF01_Backend.md`~~ — ĐÃ DÙNG XONG, KHÔNG CHẠY LẠI

> ⛔ **Lỗi thời từ 2026-07-30 — giữ lại chỉ để lưu vết.**
>
> Prompt này đã được dùng, `TDS-MF01_Backend.md` đã sinh và **đã commit**, sau đó còn được sửa tiếp theo `review.md` (bổ sung `partner_id` vào `user_accounts`, schema `GET /roles`, `POST /users` trả cả `schoolId` lẫn `school`, ánh xạ MSG-104/105/106, khối D1–D7 ở §3).
>
> **Chạy lại prompt này sẽ ghi đè toàn bộ những sửa đổi đó** — dòng cuối của nó ghi *"ghi đè file … thay thế hoàn toàn, không giữ lại nội dung cũ"*, và ghi chú "chưa từng commit" trong đầu mục nay đã sai.
>
> Cần sửa MF01? Sửa trực tiếp file, hoặc dùng prompt mẫu **§2c-2** (đã có nguồn số 0 = `00_Common_Conventions.md`). Nội dung bên dưới cũng **thiếu** nguồn số 0 nên tự nó đã không còn dùng được.

> Bản draft cũ tại `07_implement/MF01_User_Management_Access_Control/TDS-MF01_Backend.md` dùng sai stack
> (TypeScript/Prisma) và có 2 xung đột traceability chưa xử lý (CF-1, CF-2). Dùng nguyên khối dưới đây để
> sinh lại từ đầu — đã gộp sẵn ràng buộc stack (§2c-2), cách xử lý CF-1/CF-2, quyết định không tách ADR
> module riêng (xem thảo luận "có nên sinh ADR cho từng MF" — giữ quyết định kiến trúc *inline* trong §3
> của TDS, chỉ tách file `ADR-MFxx_*.md` riêng khi quyết định đó ảnh hưởng >1 module), và phần Google
> OAuth2 login vừa bổ sung (BR-004 v1.9, BR-218, UC-191).

```
Vai trò: bạn viết Backend-TDS cho module MF01 — User Management & Access Control, theo template
06_Document/Templates/PHASE-3_TDS.md (17 mục, EDS v2.0).

NGUỒN BẮT BUỘC ĐỌC TRƯỚC KHI VIẾT (không tự suy diễn ngoài các nguồn này):
1. UC chuẩn:  02_Requirements/02_UseCases/MF01_User_Management_Access_Control.md
   (đã sync lại 2026-07-28, gồm cả UC-191 "Login with Google (OAuth2)" mới thêm — đọc bản MỚI NHẤT trên đĩa).
2. UC↔BR↔MSG: 02_Requirements/03_TraceabilityMatrix/traceability_matrix.md §1.1
   — CHỈ dùng đúng UC ID, BR ID, MSG ID xuất hiện trong bảng của mục này.
   — Nếu cần một UC/BR không có trong bảng, DỪNG LẠI và báo cho tôi, KHÔNG tự đặt mã mới.
3. BR nội dung đầy đủ: 02_Requirements/00_BusinessRules/business_rules.md
   — chú ý BR-004 đã sửa (v1.9, 2026-07-28): access token 30 phút KHÔNG đổi + THÊM refresh token
     sliding 14 ngày (tự gia hạn ngầm, không bắt user login lại thường xuyên).
   — chú ý BR-218 (mới): Google OAuth2 login — chỉ liên kết tài khoản đã tồn tại, KHÔNG tự tạo account mới.
4. Mã lỗi:     02_Requirements/00_BusinessRules/error_message.md
   — chú ý MSG-102 (Google account chưa liên kết), MSG-103 (Google auth thất bại) — mới thêm.
5. Hợp đồng API mock: 03_Design/08_FrontEnd/Web/src/lib/api/mock-server.ts + db.ts
   — lọc đúng resource auth/user/role của MF01. LƯU Ý: mock hiện KHÔNG có nút/luồng Google login
     (chỉ có role-picker giả lập) — đây là tính năng MỚI chưa có trong mock, tự thiết kế theo BR-218/UC-191,
     không suy diễn từ mock cho riêng phần OAuth2 này.
6. Luồng màn hình: 03_Design/08_FrontEnd/Web/src/app/(app)/<role>/<module>/ — liệt kê rõ role/thư mục đã đọc.
7. Schema:     03_Design/01_Diagrams/01_Class_Diagram/data_model.puml (chỉ entity liên quan MF01: User, Role,
   Permission, RefreshToken, AccountActivationToken, PasswordResetToken...).
8. Test rule:  03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts (chỉ luật liên quan MF01).
9. ADR nền:    03_Design/02_ADR/ADR-000.md (trích lại stack/kiến trúc chung, không viết lại).
   — KHÔNG tạo file ADR-MF01_*.md riêng. Nếu có quyết định kiến trúc riêng cho MF01 (vd: chọn JWT+refresh
     token thay vì session thuần, cách lưu refresh token, thiết kế OAuth2 Authorization Code flow cho Google),
     viết INLINE trong TDS §3 (Architecture Decisions) dưới dạng bảng Option/Decision/Rationale ngắn gọn —
     chỉ tách file ADR riêng nếu quyết định đó ảnh hưởng ngoài phạm vi MF01.
10. Stack thật của dự án: 07_implement/01_MVP_Backend_Implementation_Plan.md §Stack (dòng "Stack (ADR-000)").

⚠️ RÀNG BUỘC STACK — BẮT BUỘC (PHASE-3_TDS.md chỉ là template minh họa, KHÔNG dùng nguyên cú pháp mẫu của nó):
Dự án dùng Spring Boot 4.1.x (Java 25 LTS) · PostgreSQL 16 · Flyway · Spring Security + JWT · springdoc-OpenAPI (Swagger) · MapStruct.
- §5.2 Data Structure: viết Entity JPA (`@Entity`, `@Table`, `@Id`...) + đoạn SQL Flyway migration — TUYỆT ĐỐI KHÔNG dùng
  Prisma schema (`model X { ... }` là cú pháp Node.js/Prisma, sai stack).
- §7.3 Payload Schema, §8.1/§8.2 Interface Spec: viết bằng Java (interface/class, `Optional<T>`, kiểu dữ liệu Java) —
  TUYỆT ĐỐI KHÔNG dùng TypeScript `export interface ...`.
- §11.3 Implementation Steps: lệnh migration dùng Flyway (`V{n}__<mo_ta>.sql`) — TUYỆT ĐỐI KHÔNG dùng `npx prisma migrate`.
- Google OAuth2: dùng Spring Security OAuth2 Client (`spring-boot-starter-oauth2-client`), endpoint chuẩn
  `/oauth2/authorization/google` + callback `/login/oauth2/code/google` — KHÔNG tự viết OAuth2 flow tay,
  KHÔNG dùng thư viện Node.js (passport, next-auth...).
- Nếu bất kỳ mục nào trong 17 mục của template có ví dụ mẫu bằng ngôn ngữ khác Java/SQL, PHẢI dịch sang Java/SQL
  trước khi điền — không copy nguyên cú pháp minh họa của template.

XỬ LÝ 2 XUNG ĐỘT TRACEABILITY ĐÃ BIẾT (CF-1, CF-2) — không tự ý bỏ qua:
- CF-1: BR-015 (không dùng lại 3 mật khẩu gần nhất) áp dụng cho UC-011/UC-032 (đổi mật khẩu) nhưng KHÔNG
  xuất hiện trong cột Business Rules của UC-011/UC-032 tại `traceability_matrix.md` §1.1. → GHI RÕ mâu thuẫn
  này thành 1 dòng trong TDS §2 hoặc §17 (không tự thêm BR-015 vào traceability_matrix.md, không tự xóa
  khỏi TDS — đây là việc của Huy/Lead Traceability xác nhận sau).
- CF-2: UC-154 có danh sách BR/MSG khác nhau giữa file UC (`MF01_User_Management_Access_Control.md`) và
  `traceability_matrix.md` §1.1. → dùng bảng `traceability_matrix.md` làm nguồn CHUẨN (theo quy tắc đầu mục
  2c), nhưng GHI RÕ sai khác đó thành 1 dòng trong TDS §2 hoặc §17 để Huy đối chiếu lại.

YÊU CẦU BẮT BUỘC (để dùng làm tài liệu nộp báo cáo):
- §2 Ma trận Truy vết PHẢI liệt kê từng UC ID kèm TÊN UC (đúng nguyên văn nguồn 1), cột Business Rules
  PHẢI ghi đúng mã BR (đúng nguyên văn nguồn 2/3) — KHÔNG được để trống, KHÔNG được bịa mã.
- Mỗi mục TDS lấy dữ liệu THẬT từ mock (path/method/JSON/field) — không đặt tên field tưởng tượng; riêng
  phần Google OAuth2 (chưa có trong mock) phải tự thiết kế theo BR-218/UC-191 và ghi rõ là phần MỚI, không
  có trong mock.
- Nếu mock và UC/BR mâu thuẫn nhau (mock làm khác BR mô tả), GHI RÕ mâu thuẫn đó thành 1 dòng riêng
  trong §2 hoặc §17, không tự ý chọn bên nào đúng.
- Cuối tài liệu, liệt kê lại đầy đủ danh sách mã UC + mã BR đã trích dẫn (để đối chiếu nhanh khi review).

Output: ghi đè file 07_implement/MF01_User_Management_Access_Control/TDS-MF01_Backend.md (bản cũ dùng
TypeScript/Prisma sai stack, chưa từng commit — thay thế hoàn toàn, không giữ lại nội dung cũ).
```

---

## 3. Chia việc theo module — cả 13 đều làm, khác nhau ở THỨ TỰ ưu tiên

Không hoãn module nào. Cột **Ưu tiên** quyết định làm cái nào TRƯỚC, không phải làm hay bỏ:
- **P0 — nền tảng** (chặn cứng, cả team chờ): TDS + backend phải xong trước tiên.
- **P1 — trục xương sống**: TDS + backend làm ngay sau nền, demo/report dựa vào chuỗi này.
- **P2 — còn lại**: làm song song ở nửa sau, cùng đợt, không để sang sau report.

| Module | Chủ | Số UC | Ưu tiên | Ghi chú phạm vi |
| --- | --- | --- | --- | --- |
| MF01 User & Access Control | **Nam** | 19 | **P0** | Login JWT/BCrypt + RBAC 7 role. Nền tảng — làm trước tiên. |
| MF02 School/Class (+Academic Year) | **Nam** | 13 | **P0** | Vertical slice mẫu chốt convention để cả nhóm copy. |
| MF03 Student & Parent | **Huy** | 15 | **P1** | HS + phụ huynh + khai báo dị ứng/chế độ ăn. |
| MF04 Supplier & Partner | **Mạnh** | 25 | **P1** | NCC Approved + lô cho chuỗi truy xuất. |
| MF05 Menu & Diet | **Thảo** | 10 | **P1** | Menu theo khối · MenuVariant · suất riêng chế độ ăn. |
| MF06 Ingredient & Inventory | **Huy** | 19 | **P1** | Batch + liên kết món→lô + FEFO/cách ly. |
| MF08 Food Sample Retention | **Mạnh** | 5 | **P1** | Mẫu lưu + link `incidentId`/`batchIds`. |
| MF09 Meal Serving & Distribution | **Minh** | 12 | **P1** | `StudentDistribution` + `servedDishes[]` (thống kê theo món). |
| MF11 Incident & Emergency | **Minh** | 20 | **P1** | Trục điều tra: sự cố ↔ RPT ↔ HS ↔ món ↔ lô ↔ mẫu. |
| MF07 Inspection (+Violation-Process) | **Thảo** | 13 | **P2** | Kiểm thực 3 bước + vi phạm quy trình. |
| MF10 Feedback & Announcement | **Mạnh** | 7 | **P2** | Phản hồi + thông báo. |
| MF12 AI Supporting Services | **Thảo** | 12 | **P2** | Endpoint gọi Gemini stub (JSON theo ADR-000). |
| MF13 Reporting & Analytics | **Nam** | 3 | **P2** | Báo cáo/thống kê + truy xuất theo `incidentId`. |

**Trục xương sống (P1) phải thông:** `Login theo role → Menu theo khối → phân suất & thống kê theo món → tạo sự cố → điều tra: sự cố → HS → món → lô → NCC → mẫu` — nối bằng **ID**, không phải tên.

---

## 4. Quy trình cho 1 module — viết TDS trước, sinh backend sau

```
BƯỚC 1: Đọc nguồn (KHÔNG viết lại đặc tả từ đầu — AI đọc vào)
   a. mock-server.ts + db.ts  → path, method, JSON shape của module (→ §9 API Spec)
   b. src/app/(app)/<role>/<module>/ → màn hình, luồng (→ §6 Dynamic Modeling)
   c. data_model.puml         → entity, quan hệ, ràng buộc (→ §5 Static Modeling)
   d. mock-data.ts            → hàm nghiệp vụ + seed (→ §13 Test Scenarios)
   e. semantic-audit.ts       → 54 luật port thành Gherkin (→ §13 Test Scenarios)
   f. traceability_matrix.md + business_rules.md + error_message.md → §2 + §10

BƯỚC 2: AI-gen TDS/TestSpec theo template
   - Backend-TDS: dùng PHASE-3_TDS.md (17 mục) — điền từ nguồn bước 1
   - Frontend-Web-TDS: 8 mục rút gọn (mục 2a) — Minh điền sau khi §9 ổn định
   - TestSpec: dùng PHASE-4_Test-Spec.md — oracle từ BR/MSG/UC, không giả định

BƯỚC 3: Review chéo (trước khi viết code)
   - Huy xác nhận §2 Traceability (UC/BR/MSG đúng nguồn, không bịa)
   - Mạnh xác nhận §5 Schema + §9 API Spec (khớp data_model.puml + mock)
   - Minh xác nhận §8 Interface + §9 JSON shape (đổi baseURL FE là chạy được)
   - Thảo xác nhận §13 TestSpec (đủ coverage happy + error path)
   - Nam Final Approval → chuyển trạng thái TDS sang "Approved"

BƯỚC 4: Sinh Backend trên nền chung (SAU KHI TDS Approved)
   - entity→repo→dto→mapper→service→controller
   - DTO phải khớp shape §9 API Spec; validate cốt lõi port từ semantic-audit
   - Chạy qua Swagger, đối chiếu từng endpoint với TDS §9 (Minh xác nhận)

BƯỚC 5: Review backend chéo → chủ module sửa → Nam final approve
```

**Thứ tự bắt buộc trong giai đoạn TDS:**
Nam làm TDS MF01 + MF02 TRƯỚC (làm mẫu convention cho 4 người còn lại copy); sau đó song song theo Tier.

---

## 4b. Làm song song được không? — Được, sau MỘT nút cổ chai

**Nút cổ chai (cả team chờ Nam):**
- **TDS:** TDS MF01 + MF02 của Nam xong trước → các thành viên khác copy convention (mục đặt tên, §2 format, §9 shape chuẩn) để không mỗi người một kiểu.
- **Backend:** nền chung + MF01 (auth) + MF02 (vertical slice) của Nam xong → 4 người còn lại gen module của mình trên nền đó.

**Sau nút cổ chai → song song được**, vì code chia theo package (`com.sfms.<module>/`) và tài liệu chia theo thư mục (`MFxx/`) — không đụng conflict.

**Ba chỗ dễ va khi làm song song — thỏa thuận TRƯỚC:**

| Chỗ va | Vì sao | Cách tránh |
| --- | --- | --- |
| **Flyway migration** | Migration là tài nguyên chung, có thứ tự — hai người cùng tạo `V2__` là đụng. | Nam giữ **V1 = toàn bộ schema** (sinh 1 lần từ `data_model.puml`). Module chỉ thêm seed/logic. |
| **Tham chiếu chéo module** | Trục xương sống nối nhiều module (MF11 trỏ HS/món/lô/mẫu/NCC). | Tham chiếu bằng **ID (cột String/UUID)**, KHÔNG import class entity module khác. |
| **Enum/error-code dùng chung** | Nhiều module cùng dùng status, mã MSG. | Để ở **module `common`** Nam sở hữu, sinh 1 lần từ `error_message.md`. |

**Lịch gợi ý:**
```
Ngày 1 sáng   Nam: TDS MF01+MF02 (mẫu) + nền chung skeleton + Flyway V1    ← cả team CHỜ mẫu
Ngày 1 chiều  ┌ Mạnh: TDS MF04, MF08
   → Ngày 2   ├ Huy:  TDS MF03, MF06
              ├ Minh: TDS MF09, MF11
              ├ Thảo: TDS MF05
              └ Nam:  TDS MF13 + Backend MF01, MF02
Ngày 2-3      Review chéo TDS (Huy §2, Mạnh §5+§9, Minh §8, Thảo §13)
Ngày 3-4      Backend song song: Mạnh MF04+MF08 · Huy MF03+MF06 · Minh MF09+MF11 · Thảo MF05
Ngày 4-5      P2 song song: Mạnh MF10 · Thảo MF07+MF12 · Nam MF13
```

---

## 5. Deliverable mỗi module — code + tài liệu

**Tài liệu (viết trước, dùng cho report):**
```
MFxx/
  TDS-MFxx_Backend.md         (17 mục EDS v2.0 — PHASE-3_TDS.md)
  TDS-MFxx_Frontend_Web.md    (8 mục rút gọn — Minh điền)
  TestSpec-MFxx_<Module>.md   (PHASE-4_Test-Spec.md)
  ADR-MFxx_<Decision>.md      (chỉ nếu có quyết định kiến trúc mới)
```

**Code Spring Boot (sinh sau TDS Approved):**
```
com.sfms.<module>/
  domain/        Entity JPA + enum status (khớp data_model.puml)
  repository/    Spring Data JPA
  dto/           Request/Response records — khớp JSON §9 TDS
  mapper/        MapStruct
  service/       Business logic + validate BR (port từ semantic-audit.ts)
  web/           Controller REST /api/v1/... — path & shape khớp TDS §9,
                 kèm @Tag + @Operation(UC/BR) + @ApiResponse theo D8
```

**Nền chung (Nam dựng 1 lần, cả team dùng):**
- Base entity (`id`, `createdAt`/`updatedAt` kiểu `Instant`)
- `@RestControllerAdvice` global exception + error-code khớp `error_message.md`
- `PagedResponse<T>` + `SchoolSpec` pattern (mục 2b-4) làm mẫu
- ~~Flyway V1 (schema) + seed từ `mock-data.ts`~~ → **đã đổi 02/08**: mỗi module một file `V<n>__mf<NN>_*.sql` riêng, Nam chỉ giữ `V1` (MF01) + `V2` (seed role/permission). Xem `00_Common_Conventions.md` §0-BIS.
- **OpenAPI nền (D8):** `common/config/OpenApiConfig.java` (`@OpenAPIDefinition` + `@SecurityScheme` bearer JWT) · `ErrorResponse` có `@Schema` (D2) · `PagedResponse<T>` khai schema generic một lần · `/v3/api-docs` + `/swagger-ui/**` `permitAll`. Module khác **không** khai lại 3 thứ này.

**Chuẩn dữ liệu** *(nay là D1–D7 của `00_Common_Conventions.md` — mục này chỉ tóm tắt)*: món tham chiếu `dishId` không phải tên · ngày = **ISO/Instant** (D4) · `id` JSON + path param = **mã nghiệp vụ**, UUID là PK nội bộ (D1) · enum **SCREAMING_SNAKE** ở `common/enums/` (D6) · envelope lỗi `{error:{code,message,details}}` (D2) · tiền/lượng = number + đơn vị riêng · **không** lưu field dẫn xuất · `status` union riêng theo thực thể.

---

## 6. Nguồn bắt buộc trích dẫn (thứ tự ưu tiên)

-1. 🔴 **CODE THẬT — `04_Development/Backend/`** (từ 02/08/2026). Ưu tiên **cao hơn mọi tài liệu**: tài liệu nói một đằng mà code làm một nẻo thì **code đúng**. Bắt đầu bằng `00_Common_Conventions.md` **§0-BIS**, rồi mở thật `common/` (11 hạ tầng dùng chung) + `mf02/` (module mẫu) + `db/migration/` (số V còn trống).
0. **`07_implement/00_Common_Conventions.md`** — **D1–D10** (D1–D7 Approved 30/07, D9–D10 Approved 31/07, D8 🟡 đã áp dụng thực tế chờ Nam ký). Thắng cả mock và ADR module khi xung đột.
1. **`03_Design/08_FrontEnd/Web/src/lib/api/mock-server.ts` + `db.ts`** — hợp đồng REST (52 resource): backend khớp **path/method/tên field/luồng**. **KHÔNG** khớp định dạng ngày, giá trị enum, shape lỗi, chiến lược `id` — 4 thứ đó theo D1–D7 (mock đang cố ý đóng băng ở bản cũ, xem §0).
2. **`03_Design/08_FrontEnd/Web/src/lib/mock-data.ts`** — tham chiếu nghiệp vụ + nguồn seed.
3. **`03_Design/08_FrontEnd/Web/src/app/(app)/<role>/<module>/`** — luồng màn hình FE.
4. **`03_Design/01_Diagrams/01_Class_Diagram/data_model.puml`** — schema CSDL (ER).
5. **`03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts`** — 54 luật nghiệp vụ → test.
6. `02_Requirements/02_UseCases/MFxx_*.md` — UC chuẩn (không lấy số từ `Report 3.md`).
7. `02_Requirements/00_BusinessRules/business_rules.md` — BR chuẩn.
8. `02_Requirements/00_BusinessRules/error_message.md` — mã MSG chuẩn.
9. `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` — đối chiếu UC↔BR↔MSG.
10. `03_Design/01_Diagrams/01_Class_Diagram/class_diagram_description.md` — OOP/thiết kế báo cáo.
11. `06_Document/01_Report/Report 3.md` — tham khảo phạm vi tổng thể.
12. `03_Design/02_ADR/ADR-000.md` (dev/stub) · `ADR-001.md` (prod) — kiến trúc nền.

---

## 7. Review cuối & Nghiệm thu — Thảo chủ trì

Sau khi **cả 13 module** có TDS Approved + backend chạy được:

**Kiểm tra TDS:**
- Huy chạy đối chiếu tổng thể: mọi UC/BR/MSG trích dẫn trong TDS đều khớp đúng nguồn, không có mã bịa.
- Minh xác nhận §9 API Spec của từng TDS: đổi baseURL FE sang backend thật, các màn vẫn chạy.

**Kiểm tra Backend:**
- Thảo chạy integration test từ §13 TestSpec; xác nhận backend chặn được lớp "khoá đúng nhưng sai dòng".
- Lập danh sách điểm lệch (nếu có) gửi chủ module sửa trước khi Nam final approve.

**Định nghĩa hoàn tất (cả 13 module):**
- TDS + TestSpec tất cả module: trạng thái **"Approved"** (ký bởi Nam).
- `docker compose up` sạch từ máy trắng.
- **52 resource** CRUD qua Swagger khớp hợp đồng mock.
- RBAC 7 role áp đúng.
- Trục xương sống (mục 3) nối thông bằng ID.
- ≥1 integration test happy + error path cho **mỗi module P1** (P0/P2 tối thiểu smoke test).
